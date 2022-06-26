cd ..\javasrc
"C:\Program Files\Java\jdk1.8.0_181\bin\javac" Utils.java
"C:\Program Files\Java\jdk1.8.0_181\bin\javac" StringMethods.java
"C:\Program Files\Java\jdk1.8.0_181\bin\javac" NumberMethods.java
"C:\Program Files\Java\jdk1.8.0_181\bin\javac" Main.java -cp .
"C:\Program Files\Java\jdk1.8.0_181\bin\jar" cvfe build.jar Main *.class
