# minimize-jar   
   
The tool removes unused classes and generates a minimized uber jar (or called "fat jar", "shadow jar", "shaded jar") based on the verbose:class information.  

## How to Use It
(1) Package your application into a jar.  
  
(2) Run your application with the java argument `-verbose:class`, e.g.,   
`java -cp my-app.jar:libs/a.jr:libs/b.jar:libs2/* -verbose:class com.example.xxx.Main > /tmp/verbose.txt`    
Test everything to make sure all classed are loaded.

(3)Run this tool:     
`java -cp . liwey.minjar.Minimizer verboseLogFilePath destShadedJarFilePath`  
e.g., 
`java -cp . liwey.minjar.Minimizer /tmp/verbose.txt /tmp/my-shaded-min.jar`  


## Notice
This tool automatically merges files with the same path name in different jars. 


## Sample Output of the Tool

    1519 classes are used in the following jars:
    D:/.m2/log4j/log4j/1.2.17/log4j-1.2.17.jar
    D:/.m2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar
    D:/.m2/org/springframework/spring-aop/5.0.4.RELEASE/spring-aop-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-beans/5.0.4.RELEASE/spring-beans-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-context/5.0.4.RELEASE/spring-context-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-core/5.0.4.RELEASE/spring-core-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-expression/5.0.4.RELEASE/spring-expression-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-jcl/5.0.4.RELEASE/spring-jcl-5.0.4.RELEASE.jar
    D:/.m2/org/springframework/spring-jdbc/5.0.4.RELEASE/spring-jdbc-5.0.4.RELEASE.jar
    ...
    
    Following files will be merged:
    META-INF/LICENSE
    META-INF/MANIFEST.MF
    META-INF/NOTICE
    META-INF/services/java.sql.Driver
    META-INF/spring.handlers
    META-INF/spring.schemas
    META-INF/spring.tooling
    
    Generated minimized jar file d:/temp/min.jar
