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

package org.qubership.nifi.service.dbjson.type;

import lombok.Data;

@Data
public class JsonType {
    String path;
    String attrName;
    String specialType;
    boolean ignore = false;

    public JsonType(String path) {
        this.path = path;
        this.attrName = path != null ? path.substring(path.lastIndexOf(".") + 1) : null;
    }
 
    public JsonType(String path, String specialType, boolean ignore) {
        this(path);
        this.specialType = specialType;
        this.ignore = ignore;
    }
}
