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

import org.qubership.nifi.service.dbjson.type.DBType;
import org.qubership.nifi.service.dbjson.type.JsonType;

import java.sql.Timestamp;
import java.util.Calendar;


public interface DBToJsonMappingTable extends Transform<DBType, JsonType> {

    @Override
    JsonType transform(DBType object);

    @Override
    String convertColumn(String columnName);
    
    String formatTimestamp(Timestamp rawTimestamp);

    Calendar getCalendar();

}
