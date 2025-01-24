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

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.qubership.nifi.processors.query.AbstractRsToJsonWriter;
import org.qubership.nifi.processors.query.RsToJsonWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSingleQueryDatabaseToJson extends AbstractQueryDatabaseToJson {

    public static final PropertyDescriptor BATCH_SIZE = new PropertyDescriptor.Builder()
            .name("internal-batch-size")
            .displayName("Batch Size")
            .description("The maximum number of rows from the result set to be saved in single FlowFile.")
            .defaultValue("100")
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .build();

    public static final PropertyDescriptor FETCH_SIZE = new PropertyDescriptor.Builder()
            .name("Fetch Size")
            .description("The number of result rows to be fetched from the result set at a time. This is a hint to the database driver and may not be "
                    + "honored and/or exact. If the value specified is zero, then the hint is ignored.")
            .defaultValue("1000")
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .build();

    private RsToJsonWriter writer = new RsToJsonWriter();
    protected int fetchSize = AbstractRsToJsonWriter.DEFAULT_FETCH_SIZE;

    private List<PropertyDescriptor> propDescriptors;

    public AbstractSingleQueryDatabaseToJson() {
        super();

        List<PropertyDescriptor> descriptors = new ArrayList<>(super.getSupportedPropertyDescriptors());
        descriptors.add(BATCH_SIZE);
        descriptors.add(FETCH_SIZE);

        this.propDescriptors = Collections.unmodifiableList(descriptors);
    }

    @OnScheduled
    public void onScheduled(ProcessContext context) throws Exception {
        super.onScheduled(context);

        getWriter().setBatchSize(context.getProperty(BATCH_SIZE).asInteger());
        this.fetchSize = context.getProperty(FETCH_SIZE).asInteger();
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }

    protected RsToJsonWriter getWriter() {
        return writer;
    }
}
