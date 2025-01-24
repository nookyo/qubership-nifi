/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.distributed.cache.client.Deserializer;
import org.apache.nifi.distributed.cache.client.Serializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tags({"BulkDistributedMapCacheClient"})
@CapabilityDescription("BulkDistributedMapCacheClient Service API.")
public interface BulkDistributedMapCacheClient extends ControllerService {

    <K, V> Map<K, V> getAndPutIfAbsent(Map<K, V> values, Serializer<K> keySerializer, Serializer<V> valueSerializer, Deserializer<V> valueDeserializer) throws IOException;

    <K> long remove(final List<K> keys, final Serializer<K> keySerializer) throws IOException ;

    <K, V> boolean putIfAbsent(K key, V value, Serializer<K> keySerializer, Serializer<V> valueSerializer) throws IOException;

    <K> boolean containsKey(K key, Serializer<K> keySerializer) throws IOException;

    <K, V> void put(K key, V value, Serializer<K> keySerializer, Serializer<V> valueSerializer) throws IOException;

    <K, V> V get(K key, Serializer<K> keySerializer, Deserializer<V> valueDeserializer) throws IOException;
}
