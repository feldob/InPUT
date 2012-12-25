package se.miun.itm.input;

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