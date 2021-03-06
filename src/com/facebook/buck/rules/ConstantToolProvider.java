/*
 * Copyright 2016-present Facebook, Inc.
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

package com.facebook.buck.rules;

import com.facebook.buck.model.BuildTarget;
import com.google.common.collect.ImmutableList;
import java.util.function.Supplier;

public class ConstantToolProvider implements ToolProvider {

  private final Supplier<Tool> tool;

  public ConstantToolProvider(Supplier<Tool> tool) {
    this.tool = tool;
  }

  public ConstantToolProvider(Tool tool) {
    this(() -> tool);
  }

  @Override
  public Tool resolve(BuildRuleResolver resolver) {
    return tool.get();
  }

  @Override
  public Iterable<BuildTarget> getParseTimeDeps() {
    return ImmutableList.of();
  }
}
