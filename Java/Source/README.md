# InPUT4j

The *core module*, providing the API. Classes of main interest are : (I)DesignSpace, (I)Design, (I)InPUT, and (I)Experiment. All dependencies are included in the lib folder.

# InPUT4j_Examples

The *examples module* contains a couple of minimal examples, presenting some of the features that InPUT offers. Some examples are also expressed in plain Java to reveal the differences to the user. It depends on the *core module*. To run the examples, execute the respective main methods.

# InPUT4j_Injection

The *injection module* is an extension of the core API. It allows the definition of InPUT inputs and outputs via annotations. It uses AspectJ, and requires projects that use its features to add it to the AspectJ Path.
The module is set-up with Eclipse and [AJDT: The AspectJ Development Tools](http://www.eclipse.org/aspectj/). The *injection module* depends on the *core module*.

# InPUT4j_Injection_Examples

A collection of examples illustrating the use of the simplified annotation-based API. The module depends on both, the core and the injection modules. To run the examples, execute the respective main methods. The performance analysis example supports the develper in choosing an appropriate amount of processor cores for the thread pool on the local machine, when a certain job has to be executed multiple times. Without touching the code, the amount of tasks, repetitions, and even the task itself can be altered via a descriptor. It consist of two steps, 1) execute the PerformanceTester to retrieve empirical data, and 2) execute the PerformanceAnalyzer, to receive a comparison of mean-runtime. 

# WatchmakerHelloWorldExample

A more complete example of how InPUT can abstract a whole framework for Evolutionary Algorithms, identifying an objective String. The code for the algorithm is minimal and does not require changes, because the StringIdentification.inp file describes the experimental context, while the HelloWorld.exp file describes experimental design, including problem features and algorithm parameters. Both, StringIdentification.inp and HelloWorld.exp are archives that contain the necessary descriptors, imported programatically at runtime. Updating the content of HelloWorld.exp effects the search. E.g., changing the *TargetString* in HelloWorld.exp/problemFeatures.xml changes the objective of the next search.
