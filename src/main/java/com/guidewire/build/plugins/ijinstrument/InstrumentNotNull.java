/**
 * Copyright 2013-2014 Guidewire Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guidewire.build.plugins.ijinstrument;

import com.intellij.ant.InstrumentationUtil;
import com.intellij.ant.PseudoClassLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Mojo to instrument @NotNull annotations.
 */
@Mojo(name = "instrument-notnull",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE)
public class InstrumentNotNull extends AbstractMojo {
  public static final String CLASS_SUFFIX = ".class";
  /**
   * Set to <code>true</code> to skip the execution of the mojo.
   */
  @Parameter
  private boolean skip;

  @Parameter(property = "project.build.outputDirectory")
  private File outputDirectory;

  /**
   * The Maven project.
   */
  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  private PseudoClassLoader loader;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().debug("Skipping IntelliJ @NotNull instrumentation.");
      return;
    }

    loader = Util.createPseudoClassLoader(project);
    instrumentRecursive(outputDirectory);
  }

  private void instrumentRecursive(File file) throws MojoExecutionException, MojoFailureException {
    if (file.isFile() && file.getName().endsWith(CLASS_SUFFIX)) {
      try {
        InstrumentationUtil.instrumentNotNull(file, loader);
      } catch (IOException e) {
        throw new MojoFailureException("Cannot instrument class", e);
      }
    } else if (file.isDirectory()) {
      for (File child : file.listFiles(ClassFilter.INSTANCE)) {
        instrumentRecursive(child);
      }
    }
  }

  /**
   * Filter to select directories & form files only.
   */
  private enum ClassFilter implements FileFilter {
    INSTANCE;

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || (f.isFile() && f.getName().endsWith(CLASS_SUFFIX));
    }
  }
}
