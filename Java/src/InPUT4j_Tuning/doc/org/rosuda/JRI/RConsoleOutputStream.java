/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */// RConsoleOutputStream, part of Java/R Interface
//
// (C)Copyright 2007 Simon Urbanek
//
// For licensing terms see LICENSE in the root if the JRI distribution

package org.rosuda.JRI;

import java.io.OutputStream;
import java.io.IOException;

/** RConsoleOutputStream provides an OutputStream which causes its output to be written to the R console. It is a pseudo-stream as there is no real descriptor connected to the R console and thusly it is legal to have multiple console streams open. The synchonization happens at the RNI level.<p>Note that stdout/stderr are not connected to the R console by default, so one way of using this stream is to re-route Java output to R console:<pre>
System.setOut(new PrintStream(new RConsoleOutputStream(engine, 0)));
System.setErr(new PrintStream(new RConsoleOutputStream(engine, 1)));
</pre>

@since JRI 0.4-0
*/
public class RConsoleOutputStream extends OutputStream {
	Rengine eng;
	int oType;
	boolean isOpen;
	
	/** opens a new output stream to R console
		@param eng R engine
		@param oType output type (0=regular, 1=error/warning) */
	public RConsoleOutputStream(Rengine eng, int oType) {
		this.eng = eng;
		this.oType = oType;
		isOpen = true;
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		if (!isOpen) throw new IOException("cannot write to a closed stream");
		if (eng == null) throw new IOException("missing R engine");
		String s = new String(b, off, len);
		eng.rniPrint(s, oType);
	}
	
	public void write(byte[] b) throws IOException { write(b, 0, b.length); }
	public void write(int b) throws IOException { write(new byte[] { (byte)(b&255) }); }
	public void close() throws IOException { isOpen=false; eng=null; }
}
