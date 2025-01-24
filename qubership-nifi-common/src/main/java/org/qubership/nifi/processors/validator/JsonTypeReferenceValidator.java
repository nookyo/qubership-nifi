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

package org.qubership.nifi.processors.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonTypeReferenceValidator<T> extends AbstractJsonValidator<T> {

    private final TypeReference<T> typeRef;

    public JsonTypeReferenceValidator(ObjectMapper mapper, TypeReference<T> typeRef) {
        super(mapper, false);
        this.typeRef = typeRef;
    }
    
    public JsonTypeReferenceValidator(ObjectMapper mapper, boolean allowEmpty, TypeReference<T> typeRef) {
        super(mapper, allowEmpty);
        this.typeRef = typeRef;
    }

    @Override
    protected T convert(String json) throws IOException {
       return getMapper().readValue(json, typeRef);
    }
}
