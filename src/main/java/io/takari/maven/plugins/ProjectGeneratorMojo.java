/**
 * Copyright (c) 2014 Takari, Inc. All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.plugins;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.takari.project.generator.ProjectGenerator;
import io.takari.project.generator.ProjectGeneratorWithAntlr4;

/**
 * @author Jason van Zyl
 */
@Mojo(name = "generate", requiresProject = false)
public class ProjectGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "project.dot", property = "dotFile")
  private String dotFile;

  @Override
  public void execute() throws MojoExecutionException {
    File dot = new File(new File("").getAbsolutePath(), dotFile);
    ProjectGenerator generator = new ProjectGeneratorWithAntlr4(dot);
    File outputDirectory = new File(new File("").getAbsolutePath());
    try {
      generator.generate(outputDirectory);
    } catch (Exception e) {
      throw new MojoExecutionException("Error generating project.", e);
    }
  }
}
