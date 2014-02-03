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

import com.google.common.collect.Lists;
import com.intellij.ant.PseudoClassLoader;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Utility class to create pseudo classloader
 */
public final class Util {

  /**
   * Create {@link com.intellij.ant.PseudoClassLoader} based on the resolved dependencies.
   * @return
   * @throws org.apache.maven.plugin.MojoFailureException
   */
  public static PseudoClassLoader createPseudoClassLoader(MavenProject project) throws MojoFailureException {
    List<URL> classpath = Lists.newArrayList();
    try {

      for (String entry : project.getCompileClasspathElements()) {
        File file = new File(entry);
        classpath.add(file.toURI().toURL());
      }
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException("Cannot resolve runtime classpath dependency", e);
    } catch (MalformedURLException e) {
      throw new MojoFailureException("Cannot resolve runtime classpath dependency", e);
    }
    return new PseudoClassLoader(classpath.toArray(new URL[0]));
  }

  private Util() {
    // No instances.
  }
}
