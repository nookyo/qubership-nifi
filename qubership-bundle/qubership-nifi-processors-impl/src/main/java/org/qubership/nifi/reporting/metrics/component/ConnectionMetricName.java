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

package org.qubership.nifi.reporting.metrics.component;

public enum ConnectionMetricName {

    QUEUED_COUNT_METRIC_NAME("nc_nifi_connection_queued_count"),
    QUEUED_BYTES_METRIC_NAME("nc_nifi_connection_queued_bytes"),
    PERCENT_USED_COUNT_METRIC_NAME("nc_nifi_connection_percent_used_count"),
    PERCENT_USED_BYTES_METRIC_NAME("nc_nifi_connection_percent_used_bytes");

    private final String name;

    ConnectionMetricName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
