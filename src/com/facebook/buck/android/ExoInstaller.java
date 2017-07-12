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

package com.facebook.buck.android;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.AddToRuleKey;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.HasRuntimeDeps;
import com.facebook.buck.rules.InstallTrigger;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.step.AbstractExecutionStep;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.StepExecutionResult;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class ExoInstaller extends AbstractBuildRule implements HasRuntimeDeps {
  @AddToRuleKey private final InstallTrigger trigger;
  @AddToRuleKey private final ImmutableList<SourcePath> exoSourcePaths;
  @AddToRuleKey private final HasInstallableApk apk;

  private final Supplier<ImmutableSortedSet<BuildRule>> depsSupplier;

  protected ExoInstaller(
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      SourcePathRuleFinder ruleFinder,
      HasInstallableApk apk) {
    super(buildTarget, projectFilesystem);
    this.trigger = new InstallTrigger(projectFilesystem);
    this.apk = apk;
    this.exoSourcePaths = getApkInfoSourcePaths(apk.getApkInfo());
    this.depsSupplier =
        Suppliers.memoize(
            () -> ImmutableSortedSet.copyOf(ruleFinder.filterBuildRuleInputs(exoSourcePaths)));
  }

  private static ImmutableList<SourcePath> getApkInfoSourcePaths(ApkInfo apkInfo) {
    ImmutableList.Builder<SourcePath> builder = ImmutableList.builder();
    builder.add(apkInfo.getApkPath());
    builder.add(apkInfo.getManifestPath());
    apkInfo
        .getExopackageInfo()
        .ifPresent(
            exoInfo -> {
              exoInfo
                  .getDexInfo()
                  .ifPresent(
                      dexInfo -> {
                        builder.add(dexInfo.getDirectory(), dexInfo.getMetadata());
                      });
              exoInfo
                  .getNativeLibsInfo()
                  .ifPresent(
                      nativeLibsInfo -> {
                        builder.add(nativeLibsInfo.getDirectory());
                        builder.add(nativeLibsInfo.getMetadata());
                      });
              exoInfo
                  .getResourcesInfo()
                  .ifPresent(resourcesInfo -> builder.addAll(resourcesInfo.getResourcesPaths()));
            });
    return builder.build();
  }

  @Override
  public SortedSet<BuildRule> getBuildDeps() {
    return depsSupplier.get();
  }

  @Override
  public ImmutableList<? extends Step> getBuildSteps(
      BuildContext buildContext, BuildableContext buildableContext) {
    return ImmutableList.of(
        new AbstractExecutionStep("install_apk") {
          @Override
          public StepExecutionResult execute(ExecutionContext context)
              throws IOException, InterruptedException {
            trigger.verify(context);
            boolean result =
                context
                    .getAndroidDevicesHelper()
                    .get()
                    .installApk(buildContext.getSourcePathResolver(), apk, false, true, null);
            return result ? StepExecutionResult.SUCCESS : StepExecutionResult.ERROR;
          }
        });
  }

  @Override
  public boolean isCacheable() {
    return false;
  }

  @Nullable
  @Override
  public SourcePath getSourcePathToOutput() {
    return null;
  }

  @Override
  public Stream<BuildTarget> getRuntimeDeps(SourcePathRuleFinder ruleFinder) {
    return getBuildDeps().stream().map(BuildRule::getBuildTarget);
  }
}