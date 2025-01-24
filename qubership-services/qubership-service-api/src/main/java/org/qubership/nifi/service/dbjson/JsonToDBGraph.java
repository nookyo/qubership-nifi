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

package org.qubership.nifi.service.dbjson;

import org.qubership.nifi.service.dbjson.type.DBTypeI;

import java.util.List;

public interface JsonToDBGraph extends Transform<String, DBTypeI> {

    void appendChildren(List<? extends JsonToDBGraph> newborns);
    void appendChild(JsonToDBGraph newborn);
    
    String getJsonPath();
    String getColumnName();
    String getTableName();
    String getSourceSystem();
    List<? extends JsonToDBGraph> getChildren();
    
    JsonToDBGraph findNodeByPath(String searchJsonPath);
    List<JsonToDBGraph> findNodesByPath(String searchJsonPath);
    JsonToDBGraph getParent();
    
    @Override
    DBTypeI transform(String jsonPath) throws Exception;

    @Override
    String convertColumn(String attributeName);
}
