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

  @Parameter(defaultValue = "0.5.0-SNAPSHOT", property = "version")
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
    //
    // Fetch the latest wrapper archive
    // Unpack it in the current working project
    // Possibly interpolate the latest version of Maven in the wrapper properties
    //
    File localRepository = new File(settings.getLocalRepository());
    String artifactPath = String.format("io/takari/maven-wrapper/%s/maven-wrapper-%s.tar.gz", version, version);
    String distroPath = String.format("org/apache/maven/apache-maven/%s/apache-maven-%s-bin.zip", maven, maven); 

    String repoUrl = getMirrorAllOrCentralURL();

    String wrapperUrl = String.format("%s/%s", repoUrl, artifactPath);
    String distroUrl = String.format("%s/%s", repoUrl, distroPath);
    File destination = new File(localRepository, artifactPath);

    getLog().debug("Attempting to");
    getLog().debug(" Download maven-wrapper jar from " + wrapperUrl);
    getLog().debug(" Write maven-wrapper jar to " + destination.getAbsolutePath());

    Downloader downloader = new DefaultDownloader("mvnw", version);
    try {
      getLog().info("Downloading wrapper from " + wrapperUrl);
      downloader.download(new URI(wrapperUrl), destination);

      Path rootDirectory = Paths.get(session.getExecutionRootDirectory());

      UnArchiver unarchiver = UnArchiver.builder().useRoot(false).build();
      unarchiver.unarchive(destination, rootDirectory.toFile());
      getLog().debug("Installed maven-wrapper jar successfully.");

      overwriteMavenWrapperProperties(rootDirectory, wrapperUrl, distroUrl);

      getLog().info("");
      getLog().info("The Maven Wrapper version " + version + " has been successfully setup for your project.");
      getLog().info("Using Apache Maven " + maven);
      getLog().info("Repo URL in properties file set to " + repoUrl);
      getLog().info("");
    } catch (Exception e) {
      throw new MojoExecutionException("Error installing the maven-wrapper archive.", e);
    }
  }

  private void overwriteMavenWrapperProperties(Path rootDirectory, String wrapperUrl, String distroUrl) 
      throws IOException {
    List<String> props = new ArrayList<>();
    props.add("distributionUrl=" + distroUrl);
    props.add("wrapperUrl=" + wrapperUrl);

    if (!props.isEmpty()) {
      Path wrapperProperties = rootDirectory.resolve(Paths.get(".mvn", "wrapper", "maven-wrapper.properties"));
      if (Files.isWritable(wrapperProperties)) {
        Files.write(wrapperProperties, props, Charset.forName("UTF-8"));
      }
    }
  }

  private static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  private String getMirrorAllOrCentralURL() {
    // default
    String answer = DEFAULT_DOWNLOAD_BASE_URL;
    // user property has precedence
    if (!isNullOrEmpty(downloadBaseUrl)) {
      answer = downloadBaseUrl;
    } // otherwise mirror from settings
    else if (settings.getMirrors() != null && settings.getMirrors().size() > 0) {
      for (Mirror current : settings.getMirrors()) {
        if ("*".equals(current.getMirrorOf())) {
          answer = current.getUrl();
          break;
        }
      }
    }
    return answer;
  }
}
