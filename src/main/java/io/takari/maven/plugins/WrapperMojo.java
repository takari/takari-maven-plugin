/**
 * Copyright (c) 2014 Takari, Inc. All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.apache.maven.wrapper.DefaultDownloader;
import org.apache.maven.wrapper.Downloader;

import io.tesla.proviso.archive.UnArchiver;

/**
 * WrapperMojo install the Maven Wrapper files in the current project.
 *
 * @author Jason van Zyl
 * @author Manfred Moser
 */
@Mojo(name = "wrapper", requiresProject = false, aggregator = true)
public class WrapperMojo extends AbstractMojo {
  private static final String DEFAULT_DOWNLOAD_BASE_URL="https://repo.maven.apache.org/maven2";
  private static final String DEFAULT_MAVEN_VER = "3.6.0";

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  @Parameter(defaultValue = "0.5.5-SNAPSHOT", property = "version")
  private String version;

  @Parameter(defaultValue = DEFAULT_MAVEN_VER, property = "maven")
  private String maven;

  // Overriding Base URL for all external downloads of Maven Wrapper and Wrapper Plugin
  @Parameter(property = "downloadBaseUrl")
  private String downloadBaseUrl;

  @Parameter(property = "distributionUrl")
  private String distributionUrl;

  @Parameter( defaultValue = "${settings}", readonly = true )
  private Settings settings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    File localRepository = new File(settings.getLocalRepository());
    String artifactPath = String.format("io/takari/maven-wrapper/%s/maven-wrapper-%s.tar.gz", version, version);
    String distroPath = String.format("org/apache/maven/apache-maven/%s/apache-maven-%s-bin.zip", maven, maven); 

    String repoUrl = getRepoUrl();

    String wrapperUrl = String.format("%s/%s", repoUrl, artifactPath);
    String distroUrl = String.format("%s/%s", repoUrl, distroPath);
    File destination = new File(localRepository, artifactPath);

    getLog().debug("Attempting to");
    getLog().debug(" Download maven-wrapper from " + wrapperUrl);
    getLog().debug(" Write maven-wrapper to " + destination.getAbsolutePath());

    Downloader downloader = new DefaultDownloader("mvnw", version);
    try {
      downloader.download(new URI(wrapperUrl), destination);
      getLog().debug("Downloaded maven-wrapper from " + wrapperUrl);

      Path rootDirectory = Paths.get(session.getExecutionRootDirectory());
      UnArchiver unarchiver = UnArchiver.builder().useRoot(false).build();
      unarchiver.unarchive(destination, rootDirectory.toFile());
      getLog().debug("Extracted maven-wrapper successfully.");

      updateMavenWrapperProperties(rootDirectory, wrapperUrl, distroUrl);

      getLog().info("");
      getLog().info("Maven Wrapper version " + version + " has been successfully set up for your project.");
      getLog().info("Using Apache Maven: " + maven);
      getLog().info("Repo URL in properties file: " + repoUrl);
      getLog().info("");
    } catch (Exception e) {
      throw new MojoExecutionException("Error installing the Maven Wrapper.", e);
    }
  }

  /**
   * Update the extracted properties file to used parameters.
   */
  private void updateMavenWrapperProperties(Path rootDirectory, String wrapperUrl, String distroUrl)
      throws IOException {
    String wrapperJarUrl = wrapperUrl.replace("tar.gz", "jar");
    List<String> props = new ArrayList<>();
    props.add("distributionUrl=" + distroUrl);
    props.add("wrapperUrl=" + wrapperJarUrl);

    Path wrapperProperties = rootDirectory.resolve(Paths.get(".mvn", "wrapper", "maven-wrapper.properties"));
    if (Files.isWritable(wrapperProperties)) {
      Files.write(wrapperProperties, props, Charset.forName("UTF-8"));
      getLog().debug("Properties file updated, located at " + wrapperProperties);
    }
    else
    {
      getLog().debug("Left existing properties file untouched. " + wrapperProperties);
    }
  }

  /**
   * Determine the repository URL to download wrapper and maven from.
   */
  private String getRepoUrl() {
    // default
    String result = DEFAULT_DOWNLOAD_BASE_URL;
    // user property has precedence
    if (!isNullOrEmpty(downloadBaseUrl)) {
      result = downloadBaseUrl;
      getLog().debug("Setting repo URL from property.");
    }
    // adpapt to also support MVNW_REPOURL as supported by mvnw scripts from maven-wrapper
    String mvnwRepoUrl = System.getenv("MVNW_REPOURL");
    if (!isNullOrEmpty(mvnwRepoUrl)) {
      result = mvnwRepoUrl;
      getLog().debug("Setting repo URL from environment variable.");
    }
    // otherwise mirror from settings
    else if (settings.getMirrors() != null && settings.getMirrors().size() > 0) {
      for (Mirror current : settings.getMirrors()) {
        if ("*".equals(current.getMirrorOf())) {
          result = current.getUrl();
          break;
        }
      }
      getLog().debug("Setting repo URL from mirro in settings file.");
    }
    getLog().debug("Determined repo URL to use as " + result);
    return result;
  }

  private static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
