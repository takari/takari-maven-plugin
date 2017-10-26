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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wrapper.DefaultDownloader;
import org.apache.maven.wrapper.Downloader;

import io.tesla.proviso.archive.UnArchiver;

/**
 * @author Jason van Zyl
 */
@Mojo(name = "wrapper", requiresProject = false, aggregator = true)
public class WrapperMojo extends AbstractMojo {

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  @Parameter(defaultValue = "0.3.0", property = "version")
  private String version;

  @Parameter(defaultValue = "3.5.2", property = "maven")
  private String maven;

  @Parameter(property = "distributionUrl")
  private String distributionUrl;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    //
    // Fetch the latest wrapper archive
    // Unpack it in the current working project
    // Possibly interpolate the latest version of Maven in the wrapper properties
    //
    File localRepository = new File(System.getProperty("user.home"), ".m2/repository");
    String artifactPath = String.format("io/takari/maven-wrapper/%s/maven-wrapper-%s.tar.gz", version, version);
    String wrapperUrl = String.format("https://repo1.maven.org/maven2/%s", artifactPath);
    File destination = new File(localRepository, artifactPath);
    Downloader downloader = new DefaultDownloader("mvnw", version);
    try {
      downloader.download(new URI(wrapperUrl), destination);
      UnArchiver unarchiver = UnArchiver.builder().useRoot(false).build();
      Path rootDirectory = Paths.get(session.getExecutionRootDirectory());
      unarchiver.unarchive(destination, rootDirectory.toFile());
      overwriteDistributionUrl(rootDirectory, getDistributionUrl());
      getLog().info("");
      getLog().info("The Maven Wrapper version " + version + " has been successfully setup for your project.");
      getLog().info("Using Apache Maven " + maven);
      getLog().info("");
    } catch (Exception e) {
      throw new MojoExecutionException("Error installing the maven-wrapper archive.", e);
    }
  }

  private void overwriteDistributionUrl(Path rootDirectory, String distributionUrl) throws IOException {
    if (!isNullOrEmpty(distributionUrl)) {
      Path wrapperProperties = rootDirectory.resolve(Paths.get(".mvn", "wrapper", "maven-wrapper.properties"));
      if (Files.isWritable(wrapperProperties)) {
        String distroKeyValue = "distributionUrl=" + distributionUrl;
        Files.write(wrapperProperties, distroKeyValue.getBytes(Charset.forName("UTF-8")));
      }
    }
  }

  protected String getDistributionUrl() {
    if (isNullOrEmpty(distributionUrl) && !isNullOrEmpty(maven)) {
      distributionUrl = String.format("https://repo1.maven.org/maven2/org/apache/maven/apache-maven/%s/apache-maven-%s-bin.zip", maven, maven);
    }
    return distributionUrl;
  }

  private static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

}
