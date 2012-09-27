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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helpers class that clones input streams in order to make them readable multiple times.
 * @author Felix Dobslaw
 *
 */
public class InputStreamWrapper {

	private final ByteArrayOutputStream os;

	/**
	 * create a wrapper with a central instance of the input stream.
	 * @param is
	 * @throws IOException
	 */
	public InputStreamWrapper(InputStream is) throws IOException {
		os = init(is);
	}

	/**
	 * convert the input stream to a byte output stream for easy handling.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private ByteArrayOutputStream init(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int chunk = 0;
		byte[] data = new byte[256];

		while ((chunk = is.read(data)) != -1)
			os.write(data, 0, chunk);

		return os;
	}

	/**
	 * return a new, readable clone of the wrapped stream.
	 * @return
	 */
	public InputStream next() {
		return new ByteArrayInputStream(os.toByteArray());
	}
}