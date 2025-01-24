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

public class BulletinSummary {

    private String sourceId;
    private String sourceName;
    private String level;
    private ComponentType sourceType;
    private int count;
    private String groupId;
    private String groupName;
    private String groupPath;
    private String category;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public ComponentType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ComponentType sourceType) {
        this.sourceType = sourceType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "BulletinSummary{" +
                "sourceId='" + sourceId + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", level='" + level + '\'' +
                ", sourceType=" + sourceType +
                ", count=" + count +
                ", groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupPath='" + groupPath + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
