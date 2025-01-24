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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.qubership.nifi.service.PreparedStatementProvider;

import java.sql.Connection;
import java.util.*;

import static org.qubership.nifi.NiFiUtils.MAPPER;

public abstract class AbstractQueryDatabaseToJson extends AbstractProcessor {

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain a connection to the database.")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    public static final PropertyDescriptor PS_PROVIDER_SERVICE = new PropertyDescriptor.Builder()
            .name("prepared-statement-provider-service")
            .displayName("Prepared Statement Provider")
            .description("The Controller Service that is used to create a prepared statement.")
            .required(true)
            .identifiesControllerService(PreparedStatementProvider.class)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created FlowFile from SQL query result set.")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("This relationship is only used when SQL query execution (using an incoming FlowFile) failed. The incoming FlowFile will be penalized and routed to this relationship. "
                    + "If no incoming connection(s) are specified, this relationship is unused.")
            .build();

    public static final String EXTRACTION_ERROR = "extraction.error";

    private PreparedStatementProvider statementProducer;

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> propDescriptors;

    public AbstractQueryDatabaseToJson() {
        final Set<Relationship> rel = new HashSet<>();
        rel.add(REL_SUCCESS);
        rel.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(rel);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(DBCP_SERVICE);
        pds.add(PS_PROVIDER_SERVICE);
        propDescriptors = Collections.unmodifiableList(pds);
    }

    @OnScheduled
    public void onScheduled(ProcessContext context) throws Exception {
        this.statementProducer = context
                .getProperty(PS_PROVIDER_SERVICE)
                .asControllerService(PreparedStatementProvider.class);
    }

    protected FlowFile writeResultToFlowFile(JsonNode value, ProcessSession session, FlowFile batchFlowFile) {
        return session.write(
                batchFlowFile,
                out -> MAPPER.writeValue(out, value)
        );
    }

    protected Connection createConnection(ProcessContext context) {
        return getDbcpService(context).getConnection(Collections.emptyMap());
    }

    protected DBCPService getDbcpService(ProcessContext context) {
        return context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
    }

    protected PreparedStatementProvider getStatementProducer() {
        return statementProducer;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }
}
