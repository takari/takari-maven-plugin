/**
 * Copyright (c) 2014 Takari, Inc. All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.plugins;

import java.io.File;
import java.net.URI;

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
@Mojo(name = "wrapper", requiresProject = true, aggregator = true)
public class WrapperMojo extends AbstractMojo {

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  @Parameter(defaultValue = "0.1.2", property = "version")
  private String version;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    //
    // Fetch the latest wrapper archive
    // Unpack it in the current working project
    // Possibly interpolate the latest version of Maven in the wrapper properties
    //
    File localRepository = new File(System.getProperty("user.home"), ".m2/repository");
    String artifactPath = String.format("io/takari/maven-wrapper/%s/maven-wrapper-%s.tar.gz", version, version);
    String wrapperUrl = String.format("http://repo1.maven.org/maven2/%s", artifactPath);    
    File destination = new File(localRepository, artifactPath);
    Downloader downloader = new DefaultDownloader("mvnw", version);
    try {
      downloader.download(new URI(wrapperUrl), destination);     
      UnArchiver unarchiver = UnArchiver.builder().useRoot(false).build();
      unarchiver.unarchive(destination, new File(session.getExecutionRootDirectory()));
    } catch (Exception e) {
      throw new MojoExecutionException("Error fetching maven-wrapper archive.", e);
    }
  }
}
