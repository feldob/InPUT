# What is InPUT?

InPUT offers a descriptive and programming language independent format and API for the **configuration and design of computer experiments**.

It allows you to reproduce third party results by running experiments based on their descriptors; you choose programming language and the implementation of the algorithm. Thus, describing experiments using InPUT simplifies documentation as well as the collaboration between researchers and/or practitioners.
InPUT offers adapters for different programming languages, reduces coding to a minimum, and induces clean code.
In that sense, InPUT realizes the distinction between the specification ( *design spaces*, *design*), implementation ( *code mappings*), and use (InPUT API) of configuration, similar to how the [web service architecture](http://en.wikipedia.org/wiki/Web_service) differentiates between specification (WSDL), implementation (programming language of choice), and consumption (REST, SOAP) of services.
For more info, see [wiki](https://github.com/feldob/InPUT/wiki), [scientific publication](http://dl.acm.org/citation.cfm?id=2330784.2330807), and [presentation slides](http://theinput.org/InPUT/input_gecco2012.pdf).

# Who benefits from InPUT?
Software developers that have to make many (complex) choices for their algorithms and who like clean code. This includes practitioners and researchers in operational research or computational intelligence as well as practitioners who are keen on finding well performing configurations for their systems (database pools, multi-threaded application, etc.  ).

# Example (Java):
InPUT induces clean code. Lets assume you want to run an algorithm and collect some data about its performance. Instead of 

<pre>
int var = 5;
Property foo = new FooProperty(var);
Option bar = new BarOption(foo,20);
double d = .33;
Algorithm a = new SomeAlgorithm(d,bar);
a.run();
... // record some statistics
</pre>

with InPUT you write

<pre>
IDesign design = new Design("design1.xml"); // validate and import a configuration
Algorithm a = design.getValue("Algorithm"); // retrieve the fully initialized object
a.run(); // run the algorithm
... // record some statistics
</pre>

, with the advantage being that all configuration is externalized, and can entirely be handled descriptively without code changes. You could write changes back to the design, run the experiment again, and export the configuration:

<pre>
design.setValue("Algorithm.Option.Property.Var", 6); // deep parameter change (using reflection)
a.run(); // run with new setup
... // record some statistics
design.export(new XMLFileExporter("design2.xml")); // export the new configuration
</pre>

The content of the resulting, importable, design file could look as follows:

<pre>
&lt;Design ...&gt;
	&lt;SValue id="Algorithm" value="SomeAlgorithm"&gt;
		&lt;NValue id="D" value=".33"/&gt;
		&lt;SValue id="Option" value="BarOption"&gt;
			&lt;SValue id="Property" value="FooProperty"&gt;
				&lt;NValue id="Var" value="6"/&gt;
			&lt;/SValue&gt;
			&lt;NValue id="FooBarVar" value="20"/&gt;
		&lt;/SValue&gt;
	&lt;/SValue&gt;
&lt;/Design&gt;
</pre>

This configuration is programming language independent and a so called *code mapping* realizes the translation to the used Java implementation. Once it is finalized, this code snippet can be imported to C++ using InPUT4cpp.

You can also treat output, and entire experimental investigations, randomly instantiate designs and use cascaded array parameters. This was just a very basic example. There are plenty of code examples for Java available (see *Java* [tutorials](https://github.com/feldob/InPUT/wiki/Tutorials) or [example folder](https://github.com/feldob/InPUT/tree/master/Java/Source#input4j_examples)).

# How to use InPUT?

Each programming language offers a language specific Readme in the respective folder. Currently, only Java is supported. C++ is coming soon. A first tutorial on how to use InPUT4j can be viewed [here](http://mutubehd.miun.se/video/742/InPUT+Tutorial+%28Step+1%29).

# Good to know...

When working offline, and input validation is desired, the schemaPath parameter in the [config.xml](https://github.com/feldob/InPUT/blob/master/Java/Source/InPUT4j/src/se/miun/itm/input/config.xml) must be set to a reachable location in the local network,
containing the InPUT XSD descriptors (DesignSpace, Design, or CodeMappings). The latest schemata can always be downloaded from here:

* [design](https://github.com/feldob/InPUT/blob/master/Design.xsd)
* [designSpace](https://github.com/feldob/InPUT/blob/master/DesignSpace.xsd)
* [codeMappings](https://github.com/feldob/InPUT/blob/master/CodeMappings.xsd)

#License

Copyright (C) 2012 Felix Dobslaw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

