# min-uber
根据`verbose:class`信息删除所有未用到的类，生成一个最小化的单一jar包。

## 使用方法
- 使用`-verbose:class`运行你的程序，将输出结果保存到文本文件， 如：  
`java -cp my-app.jar:libs/a.jr:libs/b.jar:libs2/* -verbose:class com.example.xxx.Main > /tmp/verbose.txt` 

- 尽可能的测试所有情况，以保证所有有用的class都被加载。  

- 运行本工具，根据输出信息生成最小化的uber jar， `java -cp . liwey.minjar.Minimizer verboseLogFilePath destUberJarFilePath`

如： `java -cp . liwey.minjar.Minimizer /tmp/verbose.txt /tmp/my-uber-min.jar`

### 注意
- 本工具自动合并不同Jar中相同路径和文件名的文件，如spring中不同jar包中的spring.handlers文件，否则最小化文件不能运行。
- 如果有用到的类没有加载，以后运行中可能出现ClassNotFoundException。
TODO
合并使用步骤。

## 运行实例
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
    
    生成最小化jar文件 d:/temp/min.jar
    

## TODO
简化运行步骤