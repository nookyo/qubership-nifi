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

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.Map;

public class MockHttpLookupService extends HttpLookupService {
    Response response;
    Headers headers;
    String url;


    @Override
    protected Response executeRequest(Request request) {
        url = request.url().toString();
        this.headers = request.headers();
        return response;
    }

    Map<String, List<String>> getHeaders() {
        return headers.toMultimap();
    }

    String getUrl() {
        return url;
    }
}
