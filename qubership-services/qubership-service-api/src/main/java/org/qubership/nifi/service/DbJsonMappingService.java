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

import org.qubership.nifi.service.dbjson.DBToJsonMappingTable;
import org.qubership.nifi.service.dbjson.JsonToDBGraph;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.Validator;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.Map;

public interface DbJsonMappingService extends ControllerService {

    PropertyDescriptor BE_TYPE = new PropertyDescriptor.Builder()
            .name("be-type")
            .displayName("Business Entity type")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    PropertyDescriptor SOURCE_SYSTEM = new PropertyDescriptor.Builder()
            .name("source-system")
            .displayName("Source System")
            .required(false)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(Validator.VALID)
            .build();

    DBToJsonMappingTable db2Json();
    /**
     * For lookup
     * attributes be.type = <real be type>
     */
    DBToJsonMappingTable db2Json(Map<String, String> attributes);

    JsonToDBGraph json2DB();
    /**
     * For lookup
     * attributes be.type = <real be type>
     */
    JsonToDBGraph json2DB(Map<String, String> attributes);

    String getBeType();

    String getSourceSystem();

    boolean isLookup();

}
