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

package org.qubership.nifi.processors.json.context;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Builder;
import lombok.Getter;

@Builder
public class JsonMergeContext {

    @Getter
    private String path;
    @Getter
    private String pathToInsert;
    @Getter
    private ArrayNode nodes;
    @Getter
    @Builder.Default
    private boolean isArray = true;
    private InsertionContext insertionContext;


    public String getKeyFromSourceToTarget() {
        return insertionContext.getJoinKeyParentWithChild();
    }

    public String getKeyFromTargetToSource() {
        return insertionContext.getJoinKeyChildWithParent();
    }

    public String getKeyToInsertTarget() {
        return insertionContext.getKeyToInsert();
    }

    public boolean isNeedToCleanTarget() {
        return insertionContext.isNeedToCleanTarget();
    }
}
