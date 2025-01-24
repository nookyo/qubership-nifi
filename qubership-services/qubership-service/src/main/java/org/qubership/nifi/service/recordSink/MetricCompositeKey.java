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

package org.qubership.nifi.service.recordSink;

import java.util.Arrays;
import java.util.Objects;

public class MetricCompositeKey {

    private String metricName;
    private String[] labelsName;
    private String[] labelsValue;

    public MetricCompositeKey(String metricName, String[] labelsName, String[] labelsValue) {
        this.metricName = metricName;
        this.labelsName = labelsName;
        this.labelsValue = labelsValue;
    }

    public String getMetricName() {
        return metricName;
    }

    public String[] getLabelsName() {
        return labelsName;
    }

    public String[] getLabelsValue() {
        return labelsValue;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setLabelsName(String[] labelsName) {
        this.labelsName = labelsName;
    }

    public void setLabelsValue(String[] labelsValue) {
        this.labelsValue = labelsValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricCompositeKey that = (MetricCompositeKey) o;
        return metricName.equals(that.metricName) && Arrays.equals(labelsName, that.labelsName) && Arrays.equals(labelsValue, that.labelsValue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(metricName);
        result = 31 * result + Arrays.hashCode(labelsName);
        result = 31 * result + Arrays.hashCode(labelsValue);
        return result;
    }
}