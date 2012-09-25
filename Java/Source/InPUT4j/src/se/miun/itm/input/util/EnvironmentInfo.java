/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input.util;

import java.util.Calendar;

import se.miun.itm.input.InPUTConfig;

/**
 * A util that collects environmental information via Java.
 * @author Felix Dobslaw
 *
 */
public class EnvironmentInfo {

	/**
	 * language=Java version="java.runtime.version"
	 * architecture="os.arch"
	 * os="os.name"
	 * memory="Runtime.getRuntime().freeMemory()"
	 * 
	 * Environment: Language="java" Version="1.6.0_20-b20" Architecture="amd64" Processors="2" OS="Linux" Memory="183339892736"
	 */
	public static String getInfo() {
		StringBuilder b = new StringBuilder();
		b.append( Calendar.getInstance().getTime());
		b.append("\n");
		b.append("Environment: Language=\"java\" Version=\"");
		b.append(System.getProperty("java.version"));
		b.append("\" Architecture=\"");
		b.append(System.getProperty("os.arch"));
		b.append("\" Processors=\"");
		b.append(Runtime.getRuntime().availableProcessors());
		b.append("\" OS=\"");
		b.append(System.getProperty("os.name"));
		b.append("\" Memory=\"");
		b.append( Runtime.getRuntime().totalMemory());
		b.append("\"");
		return b.toString();
	}

	/**
	 * returns the configuration file in an appropriate structure.
	 * @return
	 */
	public static String getConfig() {
		return InPUTConfig.getPrettyConfig();
	}
}
