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
