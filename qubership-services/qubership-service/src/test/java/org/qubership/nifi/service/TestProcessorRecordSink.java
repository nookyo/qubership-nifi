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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.record.sink.RecordSinkService;

import java.util.ArrayList;
import java.util.List;

public class TestProcessorRecordSink extends AbstractProcessor {

    public static final PropertyDescriptor RECORD_SINK = new PropertyDescriptor.Builder()
            .name("record-sink")
            .displayName("Record Sink")
            .description("Record Sink Controller Service")
            .identifiesControllerService(RecordSinkService.class)
            .required(true)
            .build();


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> propDescriptors = new ArrayList<>();
        propDescriptors.add(RECORD_SINK);
        return propDescriptors;
    }
}
