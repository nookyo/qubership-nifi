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

package org.qubership.nifi.processors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.MalformedRecordException;
import org.apache.nifi.serialization.RecordReader;
import org.apache.nifi.serialization.RecordReaderFactory;
import org.apache.nifi.serialization.SimpleRecordSchema;
import org.apache.nifi.serialization.record.MapRecord;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordField;
import org.apache.nifi.serialization.record.RecordFieldType;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.apache.nifi.util.file.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.nifi.processors.PutSQLRecord;

public class PutSQLRecordTest {
    private final static String DB_LOCATION = "target/db_ldt";
    private TestRunner testRunner;
    private Connection connection;
    private TestRecordReader recordReader;
    private TestRecordReaderFactory recordReaderFactory;

    @BeforeEach
    public void init() throws InitializationException, ClassNotFoundException, SQLException {
        testRunner = TestRunners.newTestRunner(PutSQLRecord.class);
        testRunner.setValidateExpressionUsage(false);

        testRunner.setProperty(PutSQLRecord.DBCP_SERVICE, "dbcp");
        testRunner.setProperty(PutSQLRecord.RECORD_READER, "recordReaderFactory");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "10");

        DBCPService dbcp = new DBCPServiceSimpleImpl();
        final Map<String, String> dbcpProperties = new HashMap<>();
        testRunner.addControllerService("dbcp", dbcp, dbcpProperties);
        testRunner.enableControllerService(dbcp);
        testRunner.assertValid(dbcp);
        
        recordReader = new TestRecordReader();
        recordReaderFactory = new TestRecordReaderFactory(recordReader);
        
        testRunner.addControllerService("recordReaderFactory", recordReaderFactory, Collections.emptyMap());
        testRunner.enableControllerService(recordReaderFactory);
        testRunner.assertValid(recordReaderFactory);
        
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        connection = DriverManager.getConnection("jdbc:derby:" + DB_LOCATION + ";create=true");
        try (PreparedStatement ps = connection.prepareStatement(
                "create table testTable1 (col1 varchar(255), col2 varchar(255)" + 
                ", num1 numeric(20), num2 numeric(20)" + 
                ", num3 numeric(20), ch4 varchar(10), num5 numeric(20)" + 
                ", num6 numeric(20), num7 numeric(20)" + 
                ", num8 numeric(20), time1 time, ts1 timestamp, date1 date, bool1 boolean, ch5 varchar(500)" +
                ")");) {
            ps.executeUpdate();
        }
    }
    
    @AfterEach
    public void cleanUp() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:derby:" + DB_LOCATION + ";shutdown=true");
        } catch (SQLNonTransientConnectionException e) {
            // Do nothing, this is what happens at Derby shutdown
        }
        final File dbLocation = new File(DB_LOCATION);
        try {
            FileUtils.deleteFile(dbLocation, true);
        } catch (IOException ioe) {
            // Do nothing, may not have existed
        }
    }
    
    @Test
    public void testSuccess() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("byte1", RecordFieldType.BYTE.getDataType()));
        fields.add(new RecordField("char3", RecordFieldType.CHAR.getDataType()));
        fields.add(new RecordField("double1", RecordFieldType.DOUBLE.getDataType()));
        fields.add(new RecordField("float1", RecordFieldType.FLOAT.getDataType()));
        fields.add(new RecordField("int1", RecordFieldType.INT.getDataType()));
        fields.add(new RecordField("short1", RecordFieldType.SHORT.getDataType()));
        fields.add(new RecordField("time1", RecordFieldType.TIME.getDataType()));
        fields.add(new RecordField("ts1", RecordFieldType.TIMESTAMP.getDataType()));
        fields.add(new RecordField("date1", RecordFieldType.DATE.getDataType()));
        fields.add(new RecordField("boolean1", RecordFieldType.BOOLEAN.getDataType()));
        fields.add(new RecordField("char4", RecordFieldType.STRING.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("byte1", (byte)111);
        values.put("short1", (short)111);
        values.put("boolean1", true);
        values.put("char3", '0');
        values.put("double1", Double.valueOf("1.1"));
        values.put("float1", Float.valueOf("1.1"));
        values.put("int1", 90);
        values.put("time1", new Time(10000));
        values.put("date1", new Date(10000));
        values.put("ts1", new Timestamp(100000));
        values.put("char4", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("byte1", (byte)111);
        values.put("short1", (short)111);
        values.put("boolean1", true);
        values.put("char3", '0');
        values.put("double1", Double.valueOf("1.1"));
        values.put("float1", Float.valueOf("1.1"));
        values.put("int1", 90);
        values.put("time1", new Time(10000));
        values.put("date1", new Date(10000));
        values.put("ts1", new Timestamp(100000));
        values.put("char4", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2, num3, ch4, num5, num6, num7, num8, time1, ts1, date1, bool1, ch5) "
            + "values (?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testInvalidParameters() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1) values (?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        AssertionError ex = Assertions.assertThrows(AssertionError.class, 
            () -> testRunner.run(),
            "Expected failure on run"
        );
        Assertions.assertNotNull(ex.getCause(), "AssertionError is expected to have caused by");
        Assertions.assertEquals("Failed to execute PutSQLRecord service actions", ex.getCause().getMessage());
        Assertions.assertNotNull(ex.getCause().getCause(), "ProcessException is expected to have caused by");
        Assertions.assertEquals("Number of fields in record (4) does not match number of parameters (3)", ex.getCause().getCause().getMessage());
    }

    @Test
    public void testArrayType() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.ARRAY.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, num3) values (?,?,?,?,?)");

        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        AssertionError ex = Assertions.assertThrows(AssertionError.class,
                () -> testRunner.run(),
                "Expected failure on run"
        );
        Assertions.assertNotNull(ex.getCause(), "AssertionError is expected to have caused by");
        Assertions.assertEquals("Failed to execute PutSQLRecord service actions", ex.getCause().getMessage());
        Assertions.assertNotNull(ex.getCause().getCause(), "ProcessException is expected to have caused by");
        Assertions.assertEquals("Record contains unsupported type = ARRAY", ex.getCause().getCause().getMessage());
    }

    @Test
    public void testChoiceType() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.CHOICE.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, num3) values (?,?,?,?,?)");

        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        AssertionError ex = Assertions.assertThrows(AssertionError.class,
                () -> testRunner.run(),
                "Expected failure on run"
        );
        Assertions.assertNotNull(ex.getCause(), "AssertionError is expected to have caused by");
        Assertions.assertEquals("Failed to execute PutSQLRecord service actions", ex.getCause().getMessage());
        Assertions.assertNotNull(ex.getCause().getCause(), "ProcessException is expected to have caused by");
        Assertions.assertEquals("Record contains unsupported type = CHOICE", ex.getCause().getCause().getMessage());
    }

    @Test
    public void testMapType() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.MAP.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, num3) values (?,?,?,?,?)");

        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        AssertionError ex = Assertions.assertThrows(AssertionError.class,
                () -> testRunner.run(),
                "Expected failure on run"
        );
        Assertions.assertNotNull(ex.getCause(), "AssertionError is expected to have caused by");
        Assertions.assertEquals("Failed to execute PutSQLRecord service actions", ex.getCause().getMessage());
        Assertions.assertNotNull(ex.getCause().getCause(), "ProcessException is expected to have caused by");
        Assertions.assertEquals("Record contains unsupported type = MAP", ex.getCause().getCause().getMessage());
    }

    @Test
    public void testRecordType() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.RECORD.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, num3) values (?,?,?,?,?)");

        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        AssertionError ex = Assertions.assertThrows(AssertionError.class,
                () -> testRunner.run(),
                "Expected failure on run"
        );
        Assertions.assertNotNull(ex.getCause(), "AssertionError is expected to have caused by");
        Assertions.assertEquals("Failed to execute PutSQLRecord service actions", ex.getCause().getMessage());
        Assertions.assertNotNull(ex.getCause().getCause(), "ProcessException is expected to have caused by");
        Assertions.assertEquals("Record contains unsupported type = RECORD", ex.getCause().getCause().getMessage());
    }

    @Test
    public void testArrayTypeWithPayloadConversion() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.ARRAY.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", new String[]{"X","Y","Z","XX"});
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.CONVERT_PAYLOAD, "true");
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, ch5) values (?,?,?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();

        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);

        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testChoiceTypeWithPayloadConversion() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.CHOICE.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.CONVERT_PAYLOAD, "true");
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, ch5) values (?,?,?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();

        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);

        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testMapTypeWithPayloadConversion() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.MAP.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", null);
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.CONVERT_PAYLOAD, "true");
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, ch5) values (?,?,?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();

        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);

        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testRecordTypeWithPayloadConversion() throws ClassNotFoundException, SQLException {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        fields.add(new RecordField("array1", RecordFieldType.RECORD.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("char1", "val1");
        rec1.put("char2", "val1");
        rec1.put("bigint1", new BigInteger("444"));
        rec1.put("long1", Long.valueOf(444));
        rec1.put("array1", null);

        Map<String, Object> recNest2 = new HashMap<>();
        recNest2.put("char1", "val3332");
        recNest2.put("char2", "val3332");
        recNest2.put("bigint1", new BigInteger("3332"));
        recNest2.put("long1", Long.valueOf(3332));
        recNest2.put("array1", null);

        Map<String, Object> recNest1 = new HashMap<>();
        recNest1.put("char1", "val3333");
        recNest1.put("char2", "val3333");
        recNest1.put("bigint1", new BigInteger("3333"));
        recNest1.put("long1", Long.valueOf(3333));
        recNest1.put("array1", recNest2);

        Map<String, Object> recNest = new HashMap<>();
        recNest.put("char1", "val3331");
        recNest.put("char2", "val3331");
        recNest.put("bigint1", new BigInteger("3331"));
        recNest.put("long1", Long.valueOf(3331));
        recNest.put("array1", recNest1);

        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("char1", "val333");
        rec2.put("char2", "val333");
        rec2.put("bigint1", new BigInteger("333"));
        rec2.put("long1", Long.valueOf(333));
        rec2.put("array1", new MapRecord(schema,recNest));

        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val111");
        values.put("char2", "val111");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        values.put("array1", new MapRecord(schema, rec1));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val222");
        values.put("char2", "val222");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        values.put("array1", new MapRecord(schema, rec2));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.CONVERT_PAYLOAD, "true");
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, "insert into testTable1(col1, col2, num1, num2, ch5) values (?,?,?,?,?)");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        connection = DriverManager.getConnection("jdbc:derby:" + DB_LOCATION + ";create=true");
        try (PreparedStatement ps = connection.prepareStatement("select * from testTable1");) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String ch5 = resultSet.getString("ch5");
                System.out.println(ch5);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);

        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testBatchRemaining() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val3");
        values.put("char2", "val3");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val4");
        values.put("char2", "val4");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val5");
        values.put("char2", "val5");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testBatchExact() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val3");
        values.put("char2", "val3");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val4");
        values.put("char2", "val4");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val5");
        values.put("char2", "val5");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val6");
        values.put("char2", "val6");
        values.put("bigint1", new BigInteger("222"));
        values.put("long1", Long.valueOf(222));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(1, successFF.size());
    }
    
    @Test
    public void testEmpty() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run w/o input
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(0, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
    }
    
    @Test
    public void testInvalidSQL() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable2(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
        
        Assertions.assertEquals("Table/View 'TESTTABLE2' does not exist.", failureFF.get(0).getAttribute(PutSQLRecord.ERROR_MSG_ATTR));
    }
    
    @Test
    public void testSQLError() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(num1, col1, col2, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
        
        Assertions.assertEquals("Invalid character string format for type DECIMAL.", failureFF.get(0).getAttribute(PutSQLRecord.ERROR_MSG_ATTR));
    }
    
    @Test
    public void testSchemaNotFound() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        recordReaderFactory.setThrowSchemaNotFound(true);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
        
        Assertions.assertEquals("Test Schema not found", failureFF.get(0).getAttribute(PutSQLRecord.ERROR_MSG_ATTR));
    }
    
    @Test
    public void testMalformedRecord() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        recordReader.setThrowMalformedRecord(true);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
        
        Assertions.assertEquals("Test malformed record error", failureFF.get(0).getAttribute(PutSQLRecord.ERROR_MSG_ATTR));
    }
    
    @Test
    public void testIOException4Record() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);        
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        recordReader.setThrowIOException(true);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT, 
            "insert into testTable1(col1, col2, num1, num2) "
            + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");
        
        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);
        
        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());
        
        Assertions.assertEquals("Test io error", failureFF.get(0).getAttribute(PutSQLRecord.ERROR_MSG_ATTR));
    }

    @Test
    public void testParseError() {
        //prepare schema:
        List<RecordField> fields = new ArrayList<>();
        fields.add(new RecordField("char1", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("char2", RecordFieldType.STRING.getDataType()));
        fields.add(new RecordField("bigint1", RecordFieldType.BIGINT.getDataType()));
        fields.add(new RecordField("long1", RecordFieldType.LONG.getDataType()));
        RecordSchema schema = new SimpleRecordSchema(fields);
        recordReader.setSchema(schema);
        //prepara data:
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put("char1", "val1");
        values.put("char2", "val1");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        values = new HashMap<>();
        values.put("char1", "val2");
        values.put("char2", "val2");
        values.put("bigint1", new BigInteger("111"));
        values.put("long1", Long.valueOf(111));
        records.add(new MapRecord(schema, values));
        recordReader.setRecords(records);
        recordReader.setThrowMalformedRecord(true);
        //configure processor:
        testRunner.setProperty(PutSQLRecord.SQL_STATEMENT,
                "insert into testTable1(col1, col2, num1, num2) "
                        + "values (?,?,?,?)");
        testRunner.setProperty(PutSQLRecord.MAX_BATCH_SIZE, "3");

        //run for empty file:
        testRunner.enqueue("{\"char1\":\"val1\"}");
        testRunner.run();
        List<MockFlowFile> successFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_SUCCESS);
        List<MockFlowFile> retryFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_RETRY);
        List<MockFlowFile> failureFF = testRunner.getFlowFilesForRelationship(PutSQLRecord.REL_FAILURE);

        Assertions.assertEquals(0, retryFF.size());
        Assertions.assertEquals(1, failureFF.size());
        Assertions.assertEquals(0, successFF.size());

        Assertions.assertEquals("Test malformed record error", failureFF.get(0).getAttribute(PutSQLRecord.PARSING_ERROR_MSG_ATTR));
    }
    
    private class DBCPServiceSimpleImpl extends AbstractControllerService implements DBCPService {
        @Override
        public String getIdentifier() {
            return "dbcp";
        }

        @Override
        public Connection getConnection() throws ProcessException {
            try {
                return connection;
            } catch (final Exception e) {
                throw new ProcessException("getConnection failed: " + e);
            }
        }
    }
    
    private class TestRecordReader implements RecordReader {
        private RecordSchema schema;
        private List<Record> records;
        private int pos;
        private boolean throwMalformedRecord;
        private boolean throwIOException;
        
        public TestRecordReader() {
            this.pos = 0;
        }

        public TestRecordReader(RecordSchema schema, List<Record> records) {
            this.schema = schema;
            this.records = records;
            this.pos = 0;
        }
        
        public void resetPosition(){
            this.pos = 0;
        }

        public List<Record> getRecords() {
            return records;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }

        public void setSchema(RecordSchema schema) {
            this.schema = schema;
        }

        public boolean isThrowMalformedRecord() {
            return throwMalformedRecord;
        }

        public void setThrowMalformedRecord(boolean throwMalformedRecord) {
            this.throwMalformedRecord = throwMalformedRecord;
        }

        public boolean isThrowIOException() {
            return throwIOException;
        }

        public void setThrowIOException(boolean throwIOException) {
            this.throwIOException = throwIOException;
        }
        
        

        @Override
        public Record nextRecord(boolean bln, boolean bln1) throws IOException, MalformedRecordException {
            if (pos >= records.size()) {
                return null;
            }
            
            //throw on second:
            if (pos > 0 && throwMalformedRecord) {
                throw new MalformedRecordException("Test malformed record error");
            }
            if (throwIOException) {
                throw new IOException("Test io error");
            }
            Record newRecord = records.get(pos);
            pos++;
            return newRecord;
        }

        @Override
        public RecordSchema getSchema() throws MalformedRecordException {
            return this.schema;
        }

        @Override
        public void close() throws IOException {
        }
        
    }
    
    private class TestRecordReaderFactory extends AbstractControllerService 
            implements RecordReaderFactory {
        private RecordReader rr;
        private boolean throwSchemaNotFound;

        public boolean isThrowSchemaNotFound() {
            return throwSchemaNotFound;
        }

        public void setThrowSchemaNotFound(boolean throwSchemaNotFound) {
            this.throwSchemaNotFound = throwSchemaNotFound;
        }
        
        
        public TestRecordReaderFactory(RecordReader rr) {
            this.rr = rr;
        }
        
        @OnEnabled
        public void onConfigured(final ConfigurationContext context) throws InitializationException {
        }

        @Override
        public RecordReader createRecordReader(Map<String, String> map, InputStream in, long l, ComponentLog cl) 
                throws MalformedRecordException, IOException, SchemaNotFoundException {
            if (throwSchemaNotFound) {
                throw new SchemaNotFoundException("Test Schema not found");
            }
            return rr;
        }
    }
}
