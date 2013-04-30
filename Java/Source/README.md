### InPUT4j

The *core module*, providing the API. Classes of main interest are : (I)DesignSpace, (I)Design, (I)InPUT, and (I)Experiment. All dependencies are included in the lib folder. Find the javadoc [here](http://feldob.github.io/InPUT/javadoc/InPUT4j/index.html).

### Examples

The *examples module* contains a couple of minimal examples, presenting some of the features that InPUT offers. Some examples are also expressed in plain Java to reveal the differences to the user. It depends on the *core module*. To run the examples, execute the respective main methods.

Another example of using InPUT4j can be found in the core module, under src/se/miun/itm/input/ . InPUT4j uses itself for configuration. It defines it's configurational space in *configSpace.xml*, and it's current configuration in *config.xml*. *configMapping.xml* defines how the general concepts are mapped to the InPUT4j implementation.

### Injection

The *injection module* is an extension of the core API. It allows the definition of InPUT inputs and outputs via annotations. It uses AspectJ, and requires projects that use its features to add it to the AspectJ Path.
The module is set-up with Eclipse and [AJDT: The AspectJ Development Tools](http://www.eclipse.org/aaspectj/). The *injection module* depends on the *core module*.

### Injection_Examples

Examples, illustrating the use of the simplified annotation-based API. The module depends on both, the core and the injection modules. To run the examples, execute the respective main methods.

### Tests

JUnit tests for the InPUT4j core module. Depends on the InPUT4j module.

### StackoverflowExample

The [source code](https://github.com/feldob/InPUT/tree/master/Java/Source/StackoverflowExample) that gives a possible solution to the question posted [here](http://stackoverflow.com/questions/6863317/java-configuration-and-dependency-injection-akin-to-springs-ioc-vs-weld-guic) using InPUT.

### PerformanceAnalyzerExample

The performance analysis example module identifies an appropriate thread pool size for a given repetitive job. Without touching the code, the amount of tasks, repetitions, and even the task itself can be altered via a descriptor. The project contains different implementations, illustrating the differences between using InPUT, InPUT injection, plain Java, and java properties files. The module depends on the core and inject projects.

### WatchmakerHelloWorldExample

A more complete example of how InPUT can abstract a whole framework for Evolutionary Algorithms \([The Watchmaker Framework](http://watchmaker.uncommons.org/)\), identifying an objective String. The code for the algorithm is minimal and does not require changes, because the StringIdentification.inp file describes the experimental context, while the HelloWorld.exp file describes experimental design, including problem features and algorithm parameters. Both StringIdentification.inp and HelloWorld.exp are archives that contain the necessary descriptors, imported programatically at runtime. Updating the content of HelloWorld.exp effects the search. E.g., changing the *TargetString* in HelloWorld.exp/problemFeatures.xml changes the objective of the next search.
