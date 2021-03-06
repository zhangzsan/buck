/*
 * Copyright 2017-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.java.abi;

import com.facebook.buck.util.function.ThrowingSupplier;
import com.facebook.buck.zip.CustomZipEntry;
import com.facebook.buck.zip.JarBuilder;
import com.facebook.buck.zip.JarEntrySupplier;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class JarBuilderStubJarWriter implements StubJarWriter {
  private final JarBuilder jarBuilder;

  public JarBuilderStubJarWriter(JarBuilder jarBuilder) {
    this.jarBuilder = jarBuilder;
    jarBuilder.setShouldMergeManifests(true).setShouldHashEntries(true);
  }

  @Override
  public void writeEntry(
      Path relativePath, ThrowingSupplier<InputStream, IOException> streamSupplier)
      throws IOException {
    jarBuilder.addEntry(
        new JarEntrySupplier(new CustomZipEntry(relativePath), "me", streamSupplier));
  }

  @Override
  public void close() throws IOException {}
}
