# Takari Maven Plugin

## Install the Maven Wrapper in your Maven Project

```bash
cd yourmavenproject
mvn -N io.takari:maven:0.7.6:wrapper
```

You can set a specific Maven release (but not SNAPSHOT) by adding a `maven`
parameter :

```bash
cd yourmavenproject
mvn -N io.takari:maven:0.7.6:wrapper -Dmaven=3.5.4
```

Alternatively, you can set a specific download url (which could point to a
SNAPSHOT version of Maven) by using the `distributionUrl` parameter :

```bash
cd yourmavenproject
mvn -N io.takari:maven:0.7.6:wrapper -DdistributionUrl=http://server/path/to/maven/distro.zip
```

or you can set the URL to a Maven repository manager with the `MVNW_REPOURL`
environment variable.

This will create a `mvnw` and a `mvnw.cmd` file as well as a `.mvn` folder in
your project. You can now use mvnw instead of mvn in all project builds, which
will download the configured Maven installation as required.

The `.mvn` folder contains the optional wrapper jar and the
`maven-wrapper.properties` configuration file with the URLs to download the
wrapper jar and the maven zip archive from.

## Updating the Maven Wrapper in your Maven Project

In order to upgrade the Maven Wrapper in your project, you simply run the
installation commands as documented above again. This will overwrite the
installed files and you can then proceed to compare old and new files and commit
as desired.

Typically, use the scripts and binaries as updated and only adjust config files,
if needed.

## More Information

More details about the Maven Wrapper including

- URL config changes,
- verbose mode with `MVNW_VERBOSE`,
- no binary usage mode and 
- parameters `MVNW_USERNAME`, `MVNW_PASSWORD` and `MVNW_REPOURL`

can be found with the
[maven-wrapper project](https://github.com/takari/maven-wrapper).
