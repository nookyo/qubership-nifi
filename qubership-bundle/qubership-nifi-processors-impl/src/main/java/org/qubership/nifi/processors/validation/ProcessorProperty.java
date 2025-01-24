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

package org.qubership.nifi.processors.validation;

import com.networknt.schema.JsonSchema;
import java.util.regex.Pattern;

public class ProcessorProperty {
    private String beTypeAttrName;
    private String sourceIdAttrName;
    private String errorCode;
    private JsonSchema schema;
    private Pattern regex;

    public ProcessorProperty(String beTypeAttrName, String sourceIdAttrName, String errorCode, JsonSchema schema, String regex) {
        this.beTypeAttrName = beTypeAttrName;
        this.sourceIdAttrName = sourceIdAttrName;
        this.errorCode = errorCode;
        this.schema = schema;
        if (regex != null) this.regex = Pattern.compile(regex);
    }

    public String getBeTypeAttrName() {
        return beTypeAttrName;
    }

    public String getSourceIdAttrName() {
        return sourceIdAttrName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public JsonSchema getSchema() {
        return schema;
    }

    public Pattern getRegex() {
        return regex;
    }
}
