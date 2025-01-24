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

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.nifi.util.MockFlowFile;
import org.qubership.nifi.processors.BackupAttributes;

import static org.junit.jupiter.api.Assertions.*;

public class BackupAttributesTest {
    private TestRunner testRunner;

    @BeforeEach
    public void init() throws Exception {
        testRunner = TestRunners.newTestRunner(new BackupAttributes());
    }

    @Test
    public void testBackupAttributesWOExclude() {
        Map<String, String> attrs = new HashMap<>();
        String sourceId = "CUST#123456789";
        String prefix = sourceId + ".";
        attrs.put("source.id", sourceId);
        attrs.put("migration.phase", "Validation");
        attrs.put("be.type", "Customer");
        attrs.put("request.id", "11111111");
        attrs.put("session.id", "11111111");
        attrs.put("filename", "2222222");
        attrs.put("path", "./2222222");
        attrs.put("uuid", UUID.randomUUID().toString());
        
        testRunner.setProperty(BackupAttributes.PREFIX_ATTR, "source.id");
        testRunner.setProperty(BackupAttributes.EXCLUDED_ATTRS, "");
        testRunner.enqueue("", attrs);
        testRunner.run();
        List<MockFlowFile> res = testRunner.getFlowFilesForRelationship(BackupAttributes.REL_SUCCESS);
        assertEquals(1, res.size());
        MockFlowFile ff = res.get(0);
        Map<String, String> resAttrs = ff.getAttributes();
        
        assertEquals(sourceId, resAttrs.get(prefix + "source.id"));
        assertEquals(attrs.get("migration.phase"), resAttrs.get(prefix + "migration.phase"));
        assertEquals(attrs.get("be.type"), resAttrs.get(prefix + "be.type"));
        assertEquals(attrs.get("request.id"), resAttrs.get(prefix + "request.id"));
        assertFalse(resAttrs.containsKey(prefix + "filename"));
        assertFalse(resAttrs.containsKey(prefix + "path"));
        assertFalse(resAttrs.containsKey(prefix + "uuid"));
    }
    
    
    @Test
    public void testBackupAttributesWithExclude() {
        Map<String, String> attrs = new HashMap<>();
        String sourceId = "CUST#123456789";
        String prefix = sourceId + ".";
        attrs.put("source.id", sourceId);
        attrs.put("migration.phase", "Validation");
        attrs.put("be.type", "Customer");
        attrs.put("request.id", "11111111");
        attrs.put("session.id", "11111111");
        attrs.put("filename", "2222222");
        attrs.put("path", "./2222222");
        attrs.put("uuid", UUID.randomUUID().toString());
        
        testRunner.setProperty(BackupAttributes.PREFIX_ATTR, "source.id");
        testRunner.setProperty(BackupAttributes.EXCLUDED_ATTRS, "session.id|request.id");
        testRunner.enqueue("", attrs);
        testRunner.run();
        
        List<MockFlowFile> res = testRunner.getFlowFilesForRelationship(BackupAttributes.REL_SUCCESS);
        assertEquals(1, res.size());
        MockFlowFile ff = res.get(0);
        Map<String, String> resAttrs = ff.getAttributes();
        
        assertEquals(sourceId, resAttrs.get(prefix + "source.id"));
        assertEquals(attrs.get("migration.phase"), resAttrs.get(prefix + "migration.phase"));
        assertEquals(attrs.get("be.type"), resAttrs.get(prefix + "be.type"));
        assertFalse(resAttrs.containsKey(prefix + "request.id"));
        assertFalse(resAttrs.containsKey(prefix + "session.id"));
        assertFalse(resAttrs.containsKey(prefix + "filename"));
        assertFalse(resAttrs.containsKey(prefix + "path"));
        assertFalse(resAttrs.containsKey(prefix + "uuid"));
    }
}
