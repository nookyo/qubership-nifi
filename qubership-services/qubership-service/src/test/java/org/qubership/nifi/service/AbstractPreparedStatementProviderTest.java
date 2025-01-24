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

package org.qubership.nifi.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.nifi.service.AbstractPreparedStatementProvider;
import org.qubership.nifi.service.PostgresPreparedStatementWithArrayProvider;
import org.qubership.nifi.service.DBElementType;

public class AbstractPreparedStatementProviderTest {
    
    private AbstractPreparedStatementProvider createPreparedStatementProvider() {
        return new PostgresPreparedStatementWithArrayProvider();
    }
    
    @Test
    public void testConvertStringArray() {
        AbstractPreparedStatementProvider provider = createPreparedStatementProvider();
        List<String> list = Arrays.asList("1111", "null", "2222", null, "3333", null);
        Object[] res = provider.convertArray(list, DBElementType.CHAR);
        Assertions.assertArrayEquals(new String[]{"1111", "2222", "3333"}, res);
    }
    
    @Test
    public void testConvertNumArray() {
        AbstractPreparedStatementProvider provider = createPreparedStatementProvider();
        List<String> list = Arrays.asList("1111", "null", "2222", null, "3333", null);
        Object[] res = provider.convertArray(list, DBElementType.NUMERIC);
        Assertions.assertArrayEquals(new BigDecimal[]{
                new BigDecimal("1111"), 
                new BigDecimal("2222"), 
                new BigDecimal("3333")
            }, 
            res);
    }
    
    @Test
    public void testGetArrayTypeChar() {
        AbstractPreparedStatementProvider provider = createPreparedStatementProvider();
        provider.charArrayType = "text";
        provider.numArrayType = "numeric";
        Assertions.assertEquals("text", provider.getArrayType(DBElementType.CHAR));
    }
    
    @Test
    public void testGetArrayTypeNum() {
        AbstractPreparedStatementProvider provider = createPreparedStatementProvider();
        provider.charArrayType = "text";
        provider.numArrayType = "numeric";
        Assertions.assertEquals("numeric", provider.getArrayType(DBElementType.NUMERIC));
    }
    
    @Test
    public void testGetArrayTypeDefault() {
        AbstractPreparedStatementProvider provider = createPreparedStatementProvider();
        provider.charArrayType = "text";
        provider.numArrayType = "numeric";
        Assertions.assertEquals("text", provider.getArrayType(null));
    }
    
}
