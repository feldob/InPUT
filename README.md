# What is InPUT?

InPUT offers a descriptive and programming language independent interface for the configuration and design of computer experiments. It allows for the definition of well defined, complex, input and output parameter ranges.
The code mapping concept allows you to repeat experiments from third parties by importing descriptors into your algorithm implementation of choice. InPUT increases reproducibility and simplifies documentation, as well as the collaboration between researchers, because computer experiments can be exchanged between different parties, letting them run their own implementation of an algorithm, while \(re-\)using the same descriptors. Descriptors are written in XML. We offer adapters for different programming languages to further simplify the software development aspects of experimental and algorithm design all together.

# How to use InPUT?

Each programming language offers a language specific Readme in the respective folder. Currently, only Java is supported. C++ is coming soon.

# Good to know...

When working offline, you have to change the schemaLocation in your XML files to a local version of the respective XSD schema (DesignSpace, Design, or CodeMapping). The files can be downloaded here:

* [design](TheInPUT.org/Design.xsd)
* [designSpace](TheInPUT.org/DesignSpace.xsd)
* [codeMapping](TheInPUT.org/CodeMapping.xsd)

For instance, change <code>xsi:schemaLocation="http://TheInPUT.org/Design http://TheInPUT.org/Design.xsd</code> in the root node to <code>xsi:schemaLocation="http://TheInPUT.org/Design SCHEMA_PATH/Design.xsd"</code>, if the Design.xsd resides on path SCHEMA_PATH of your computer or local network. You further have to update the *schemaPath* parameter of your InPUT4j\*.jar in file *src/InPUT/config.xml*. Once you have done that, you can work fully offline, which will as a side effect boost performance.

#License

Copyright (C) 2012 Felix Dobslaw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
