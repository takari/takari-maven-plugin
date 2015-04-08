# Takari Maven Plugin

## Install the Maven Wrapper in your Maven Project

```
cd yourmavenproject
mvn io.takari:maven:0.2.1:wrapper
```

This will create a mvnw and a mvnw.bat file as well as a .mvn folder in your project.
You can now use mvnw instead of mvn in all project builds, which will download the 
configured Maven installation as required. 