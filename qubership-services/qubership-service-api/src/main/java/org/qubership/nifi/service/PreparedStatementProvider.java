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

import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.ProcessContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public interface PreparedStatementProvider extends ControllerService {
    
    PreparedStatement createPreparedStatement(
            String query,
            ProcessContext context,
            Collection<String> ids,
            Connection con
    ) throws SQLException;

    PreparedStatement createPreparedStatement(
            String query,
            ProcessContext context,
            Collection<String> ids,
            Connection con,
            DBElementType type
    ) throws SQLException;
    
    PreparedStatement createPreparedStatement(
            String query,
            ProcessContext context,
            Collection<String> ids,
            Connection con,
            int numberOfBinds,
            int bindsOffset
    ) throws SQLException;
    
    PreparedStatement createPreparedStatement(
            String query,
            ProcessContext context,
            Collection<String> ids,
            Connection con,
            DBElementType type,
            int numberOfBinds,
            int bindsOffset
    ) throws SQLException;
}
