package com.guidewire.build.plugins.ijinstrument;

import com.google.common.collect.Lists;
import com.intellij.ant.InstrumentationUtil;
import com.intellij.ant.PrefixedPath;
import com.intellij.ant.PseudoClassLoader;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Mojo to instrument IntelliJ forms.
 */
@Mojo(name = "instrument-forms",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE)
public class InstrumentForms extends AbstractMojo {
  public static final String FORM_SUFFIX = ".form";
  /**
   * Set to <code>true</code> to skip the execution of the mojo.
   */
  @Parameter
  private boolean skip;

  @Parameter(property = "project.build.outputDirectory")
  private File outputDirectory;

  @Parameter(defaultValue = "src/main/forms")
  private File formsDirectory;

  /**
   * The Maven project.
   */
  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  private PseudoClassLoader loader;
  private List<PrefixedPath> nestedForms;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().debug("Skipping IntelliJ forms instrumentation.");
      return;
    }

    // FIXME: No support for nested forms for now...
    nestedForms = Lists.newArrayList();
    loader = createPseudoClassLoader();

    instrumentRecursive(formsDirectory);
  }

  private void instrumentRecursive(File file) throws MojoExecutionException {
    if (file.isFile() && file.getName().endsWith(FORM_SUFFIX)) {
      new Instrumenter(file);
    } else if (file.isDirectory()) {
      for (File child : file.listFiles(FormsFilter.INSTANCE)) {
        instrumentRecursive(child);
      }
    }
  }

  /**
   * Filter to select directories & form files only.
   */
  private enum FormsFilter implements FileFilter {
    INSTANCE;

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || (f.isFile() && f.getName().endsWith(FORM_SUFFIX));
    }
  }

  /**
   * Create {@link PseudoClassLoader} based on the resolved dependencies.
   * @return
   * @throws MojoFailureException
   */
  private PseudoClassLoader createPseudoClassLoader() throws MojoFailureException {
    List<URL> classpath = Lists.newArrayList();
    try {

      for (String entry : project.getCompileClasspathElements()) {
        classpath.add(new URL("file://" + entry));
      }
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException("Cannot resolve runtime classpath dependency", e);
    } catch (MalformedURLException e) {
      throw new MojoFailureException("Cannot resolve runtime classpath dependency", e);
    }
    return new PseudoClassLoader(classpath.toArray(new URL[0]));
  }

  private final class Instrumenter extends InstrumentationUtil.FormInstrumenter {
    private MojoExecutionException exception;

    private Instrumenter(File form) throws MojoExecutionException {
      super(outputDirectory, nestedForms);
      instrumentForm(form, loader);
      if (exception != null) {
        throw exception;
      }
    }

    @Override
    public void log(String msg, int option) {
      switch (option) {
        case Project.MSG_ERR:
          getLog().error(msg);
          break;
        case Project.MSG_WARN:
          getLog().warn(msg);
          break;
        case Project.MSG_INFO:
          getLog().info(msg);
          break;
        case Project.MSG_VERBOSE:
          getLog().debug(msg);
          break;
        case Project.MSG_DEBUG:
          getLog().debug(msg);
          break;
        default:
          getLog().info(msg);
      }
    }

    @Override
    public void fireError(String msg) {
      exception = new MojoExecutionException("Cannot instrument form: " + msg);
    }

    @Override
    public List<File> getFormFiles() {
      return Lists.newArrayList();
    }
  }
}
