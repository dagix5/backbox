# Introduction #

BackBox is splitted in two projects:
  1. core (BackBox)
  1. gui (BackBoxGui)

To run it, you need to download both projects's source code.

# Details #

To build BackBox, you need Apache Maven (http://maven.apache.org/) correctly installed.

First of all build the core project:
```
cd BackBox
mvn package
```

After you need to create a local Maven repository and deploy to it the core jar:
```
cd ../BackBoxGui
mvn deploy:deploy-file -DgroupId=it.backbox -DartifactId=backbox -Dversion=0.1 -Dpackaging=jar -Dfile=../Backbox/target/backbox-${project.version}.jar -Durl=file://${project.basedir}/target -DrepositoryId=local-project-libraries
```

Now you can build the gui project:
```
mvn package
```

Copy all dependencies in lib directory:
```
mvn dependency:copy-dependencies -DoutputDirectory=${project.build.directory}/lib
```

And run `backboxGui-*.jar`.