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
 */package se.miun.itm.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

public class InPUTConfigTest {

	@Test
	public void testGetValue() throws InPUTException {
		
		assertNotNull(InPUTConfig.getValue(Q.LOGGING));
		assertNotNull(InPUTConfig.getValue(Q.SCHEMA_PATH));
		assertNotNull(InPUTConfig.getValue(Q.EVALUATOR));
		assertNotNull(InPUTConfig.getValue(Q.RUNTIME_VALIDATION));
		assertNotNull(InPUTConfig.getValue(Q.INJECTION));
		assertNotNull(InPUTConfig.getValue(Q.THREAD_SAFE));
		
		Object value = InPUTConfig.getValue(Q.RANDOM);
		if (!(value instanceof Random))
			fail("The random entry should be of type java.lang.Random!");
		assertNotNull(value);
	}

	@Test
	public void testIsLoggingActive() throws InPUTException {
		boolean logging = InPUTConfig.isLoggingActive();
		boolean loggingValue = InPUTConfig.getValue(Q.LOGGING);
		assertEquals(loggingValue, logging);
	}

	@Test
	public void testIsThreadSafe() throws InPUTException {
		boolean threadSafe = InPUTConfig.isThreadSafe();
		boolean threadSafeValue = InPUTConfig.getValue(Q.THREAD_SAFE);
		assertEquals(threadSafeValue, threadSafe);
	}

	@Test
	public void testIsInjectionActive() throws InPUTException {
		boolean injection = InPUTConfig.isInjectionActive();
		boolean injectionActive = InPUTConfig.getValue(Q.INJECTION);
		assertEquals(injectionActive, injection);
	}
}