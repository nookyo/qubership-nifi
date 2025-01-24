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

package org.qubership.nifi.reporting.metrics.model;

import org.apache.nifi.reporting.ComponentType;
import java.util.Objects;

public class BulletinKey {
    private final String sourceId;
    private final ComponentType sourceType;
    private final String level;

    public BulletinKey(String sourceId, ComponentType sourceType, String level) {
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulletinKey that = (BulletinKey) o;
        if (that.sourceId == null || sourceId == null)
            return sourceType == that.sourceType && level.equals(that.level);
        return sourceId.equals(that.sourceId) && sourceType == that.sourceType && level.equals(that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, sourceType, level);
    }

    @Override
    public String toString() {
        return "BulletinKey{" +
                "sourceId='" + sourceId + '\'' +
                ", sourceType=" + sourceType +
                ", level='" + level + '\'' +
                '}';
    }
}
