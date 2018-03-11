# min-jar   
   
The tool creates a minimized shaded jar (/fat jar/shadow jar/uber jar) file based on the verbose:class information. It removes all unused classes from the uber jar and generates a new minimized jar (old-filename.min.jar). 

## Steps to use it
**prequisite**  
(1) package your file into a shaded jar (/fat jar/shadow jar/uber jar).  
(2) run your application with the java argument `-verbose:class`, e.g.,   
`java -cp my-shaded.jar -verbose:class com.example.xxx.Main > verbose.log`
  
**run this tool**   
`java -cp . liwey.minjar.Main verboseLogFilePath srcShadedJarFilePath`

##Risk
You need to run your application with all kinds of configurations to make sure all potential ly used classes are loaded.   

You can use `>>` to append out to log file if you run your app more than once, e.g., `java -cp my-shaded.jar -verbose:class com.example.xxx.Main >> verbose.log`

