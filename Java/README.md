# How to use InPUT4J

## Requirement
Make sure to have Java JDK 1.6 (or higher) installed.

## Download bytecode

If you want to use InPUT4j in your project, download the latest InPUT4j\*.jar from the bin folder, and add all jars from the lib folder to the classpath of your project.
The respective source jar can also bin found in the bin folder. InPUT4j-examples.jar is a collection of examples. You can run the examples by placing the examples jar alongside InPUT4j.jar in a folder and run

java -jar InPUT4j-examples.jar EXAMPLE_CLASS

in the command line. Look into the manifest of InPUT4j-examples.jar to get an overview of the executable classes.

## Download source

The Source folder contains the core source for all InPUT4j modules. Each subfolder of Source is an Eclipse project that can be imported into Eclipse 3. or 4. \(Main Menu File/Import...\), and Netbeans 7. "see here":http://netbeans.org/kb/docs/java/import-eclipse.html. You might have to change the JRE to one available on your system \(Project Explorer rightclick on the Project/Properties/Build-Path ... Libraries ... Add Library ... JRE System Library\). You would want to remove the default JRE in that case once you're at it.

## Examples
The Examples folder is the Eclipse project that contains the code for the latest examples in InPUT4j-examples.jar. It requires the InPUT4j project to be present in the workspace, or alternatively, InPUT4j.jar has to be added to the classpath.
