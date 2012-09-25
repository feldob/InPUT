# What is InPUT?

InPUT offers a descriptive and programming language independent interface for the configuration and design of computer experiments. It allows for the definition of well defined, complex, input and output parameter ranges.
The code mapping concept allows you to repeat experiments from third parties by importing descriptors into your algorithm implementation of choice, thus increases reproducibility and simplifies documentation, as well as the collaboration between researchers, because of descriptor reuse. Descriptors are written in XML. We offer adapters for different programming languages to further simplify the software development aspects of experimental and algorithm design all together. In that sense, InPUT realizes the distinction between the specification (*design spaces*, *design*), implementation (*code mappings*), and use (InPUT API) of experimental or software configurations, similar to how the [web service architecture](http://en.wikipedia.org/wiki/Web_service) differentiates between specification (WSDL), implementation (programming language of choice), and consumption (REST, SOAP) of services.

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
DesignSpace space = new DesignSpace("space.xml"); // import the configuration scope
Design design = space.import("1","design1.xml"); // validate and import a respective configuration
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
&lt;Design/&gt;
</pre>

This configuration is programming language independent and a so called *code mapping* realizes the translation to the used Java implementation. Once it is finalized, this code snippet can be imported to C++ using InPUT4cpp.

You can also treat output, and entire experimental investigations, randomly instantiate designs and use cascaded array parameters. This was just a very basic example. There are plenty of code examples for Java available (see *Java* folder).

# How to use InPUT?

Each programming language offers a language specific Readme in the respective folder. Currently, only Java is supported. C++ is coming soon.

# Good to know...

When working offline, the *schemaLocation* attribute for each InPUT XML descriptor file has to be set to a local version of the respective XSD schema (DesignSpace, Design, or CodeMappings). The schemata for the latest version can always be downloaded from here:

* [design](http://TheInPUT.org/Design.xsd)
* [designSpace](http://TheInPUT.org/DesignSpace.xsd)
* [codeMappings](http://TheInPUT.org/CodeMappings.xsd)

For instance, change <code>xsi:schemaLocation="http://TheInPUT.org/Design http://TheInPUT.org/Design.xsd</code> in the root node to <code>xsi:schemaLocation="http://TheInPUT.org/Design SCHEMA_PATH/Design.xsd"</code>, if the Design.xsd resides on path SCHEMA_PATH of your computer or local network for those descriptors already existing. The *schemaPath* parameter of the InPUT4j\*.jar in file *src/se/miun/itm/input/config.xml* should then be set to SCHEMA_PATH too. Oncethat is done, you can work fully offline, which will have the side effect to boost performance.

#License

Copyright (C) 2012 Felix Dobslaw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
