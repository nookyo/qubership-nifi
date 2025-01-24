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

package org.qubership.nifi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class TestUtils {

  public static InputStream readFile(
      final Object testObject,
      final String filePath) throws IOException {

    final URL resourceURL = testObject.getClass().getClassLoader().getResource(filePath);
    if (resourceURL == null) {
      throw new IllegalArgumentException("File not found by path " + filePath);
    }

    return Objects.requireNonNull(testObject.getClass().getClassLoader().getResourceAsStream(filePath));
  }
}
