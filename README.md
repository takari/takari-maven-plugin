# Takari Maven Plugin

## Install the Maven Wrapper in your Maven Project

```
cd yourmavenproject
mvn -N io.takari:maven:0.3.0:wrapper
```

You can set a specific Maven release (but not SNAPSHOT) by adding a `maven` parameter :

```
cd yourmavenproject
mvn -N io.takari:maven:0.3.0:wrapper -Dmaven=3.3.3
```

Alternatively, you can set a specific download url (which could point to a SNAPSHOT version of Maven) by using the `distributionUrl` parameter :

```
cd yourmavenproject
mvn -N io.takari:maven:0.3.0:wrapper -DdistributionUrl=http://server/path/to/maven/distro.zip
```


This will create a mvnw and a mvnw.bat file as well as a .mvn folder in your project.
You can now use mvnw instead of mvn in all project builds, which will download the
configured Maven installation as required.

More details about the Maven Wrapper can be found at <a href="https://github.com/takari/maven-wrapper">https://github.com/takari/maven-wrapper</a>
