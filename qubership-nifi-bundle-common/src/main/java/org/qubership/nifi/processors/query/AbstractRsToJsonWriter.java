/*
 * Copyright 2020-2025 NetCracker Technology Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qubership.nifi.processors.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.util.Tuple;
import org.qubership.nifi.service.dbjson.DBToJsonMappingTable;
import org.qubership.nifi.service.dbjson.type.DBType;
import org.qubership.nifi.service.dbjson.type.JsonType;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.qubership.nifi.NiFiUtils.MAPPER;

@NoArgsConstructor
public abstract class AbstractRsToJsonWriter<T> {

    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int DEFAULT_FETCH_SIZE = 1000;

    private int batchSize = DEFAULT_BATCH_SIZE;
    private int fetchSize = DEFAULT_FETCH_SIZE;

    DBToJsonMappingTable dbJsonMapping;

    public AbstractRsToJsonWriter(DBToJsonMappingTable dbJsonMapping) {
        this.dbJsonMapping = dbJsonMapping;
    }

    public long write(ResultSet rs, T out, ComponentLog logger) throws SQLException, IOException {
        return write(rs, "", out, logger);
    }

    public long write(ResultSet rs, String tableName, T out, ComponentLog logger) throws SQLException, IOException {
        ArrayNode nodes = JsonNodeFactory.instance.arrayNode();
        List<Tuple<String, Integer>> columns = extractColumns(rs.getMetaData());
        List<JsonType> jsonTypes = getJsonTypes(rs.getMetaData(), columns, tableName);
        int batchCounter = 0;
        long rowCounter = 0;

        while (rs.next()) {
            rowCounter++;
            nodes.add(convertToJson(rs, columns, jsonTypes, logger));
            batchCounter++;

            if (batchSize <= batchCounter) {
                writeJson(nodes, out);
                batchCounter = 0;
                nodes = JsonNodeFactory.instance.arrayNode();
            }
        }

        if (nodes.size() > 0) writeJson(nodes, out);

        return rowCounter;
    }

    private List<Tuple<String, Integer>> extractColumns(ResultSetMetaData metaData) throws SQLException {
        List<Tuple<String, Integer>> result = new ArrayList<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            result.add(new Tuple<>(metaData.getColumnName(i), metaData.getColumnType(i)));
        }
        return Collections.unmodifiableList(result);
    }
    
    private List<JsonType> getJsonTypes(ResultSetMetaData metaData, List<Tuple<String, Integer>> columns, String tableName) throws SQLException {
        List<JsonType> jsonTypes = new ArrayList<>(columns.size());
        if (dbJsonMapping != null) {
            for (int i = 0; i < columns.size(); i++) {
                String hypothesisTableName = metaData.getTableName(i + 1);
                if (hypothesisTableName.isEmpty()) {
                    hypothesisTableName = tableName;
                }
                JsonType fullJsonPath = dbJsonMapping.transform(new DBType(hypothesisTableName, columns.get(i).getKey()));
                jsonTypes.add(i, fullJsonPath);
            }
        }
        return jsonTypes;
    }

    private ObjectNode convertToJson(ResultSet rs, List<Tuple<String, Integer>> columns, List<JsonType> jsonTypes, ComponentLog logger) throws SQLException, IOException {
        ObjectNode objNode = JsonNodeFactory.instance.objectNode();
        if (dbJsonMapping != null) {
            for (int i = 0; i < columns.size(); i++) {
                Tuple<String, Integer> curColumn = columns.get(i);
                JsonType fullJsonPath = jsonTypes.get(i);
                if (fullJsonPath.isIgnore())
                    continue;
                String attributeName = fullJsonPath.getAttrName();
                
                if (isOrdinaryTimestamp(curColumn.getValue()) ||
                        isOrdinaryDate(curColumn.getValue()) ||
                        isOracleTimestamp(curColumn.getValue())) {
                    /*implicit transform 'date-as-string' */
                    Timestamp tm = rs.getTimestamp(i + 1);

                    if (tm != null && (curColumn.getValue() == Types.TIME || curColumn.getValue() == Types.TIMESTAMP)){
                        tm = rs.getTimestamp(i + 1, dbJsonMapping.getCalendar());
                    }

                    if (tm != null) {
                        objNode.put(attributeName, dbJsonMapping.formatTimestamp(tm));
                    } else {
                        objNode.put(attributeName, (String)null);
                    }
                } else {
                    String rawValue = rs.getString(i + 1);
                    if (rawValue == null && fullJsonPath.getSpecialType() == null) {    // updated for GetBoolean to handle null value as false
                        objNode.put(attributeName, rawValue);
                        continue;
                    }
                    if (fullJsonPath.getSpecialType() != null) {
                        try {
                            switch (fullJsonPath.getSpecialType()) {
                                case "boolean":
                                    objNode.put(attributeName, rs.getBoolean(i+1));
                                    break;
                                case "Boolean":
                                    Boolean boolVal = Boolean.valueOf(rs.getBoolean(i+1));
                                    if (rs.wasNull()) {
                                        boolVal = null;
                                    }
                                    objNode.put(attributeName, boolVal);
                                    break;
                                case "object":
                                    if (rawValue == null) {
                                        objNode.put(attributeName, rawValue);
                                    } else {
                                        objNode.set(attributeName, MAPPER.readTree(rawValue));
                                    }
                                    break;
                                case "integer":
                                    objNode.put(attributeName, Integer.valueOf(rawValue));
                                    break;
                                case "number":
                                    objNode.put(attributeName, Double.valueOf(rawValue));
                                    break;
                                default:
                                    objNode.put(attributeName, rawValue);
                            }
                        } catch (Exception e) {
                            logger.warn("Can't parse '" + rawValue + "' as " + fullJsonPath.getSpecialType() + ", the value will be written as string");
                            objNode.put(attributeName, rawValue);
                        }
                    } else {
                        objNode.put(attributeName, rawValue);
                    }
                }
            }
        } else {
            /*for back compatibility*/
            for (int i = 0; i < columns.size(); i++)
                objNode.put(columns.get(i).getKey(), rs.getString(i + 1));
        }

        return objNode;
    }

    private boolean isOracleTimestamp(int type){
        return (type == /* OracleTypes.TIMESTAMPTZ */ -101) || (type == /* OracleTypes.TIMESTAMPLTZ */ -102);
    }

    private boolean isOrdinaryTimestamp(int type) {
        return (type == Types.TIME) ||
                (type == Types.TIMESTAMP) ||
                (type == Types.TIME_WITH_TIMEZONE) ||
                (type == Types.TIMESTAMP_WITH_TIMEZONE);
    }

    private boolean isOrdinaryDate(int type) {
        return (type == Types.DATE);
    }

    protected abstract void writeJson(JsonNode result, T out);

    public void setBatchSize(int batchSize) {
        if (batchSize > 0) this.batchSize = batchSize;
    }

    @Deprecated
    public void setFetchSize(int fetchSize) {
        if (fetchSize > 0) this.fetchSize = fetchSize;
    }
}
