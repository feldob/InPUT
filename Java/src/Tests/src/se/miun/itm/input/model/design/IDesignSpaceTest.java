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
 */package se.miun.itm.input.model.design;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.AndYetAnotherSecondChoice;
import model.AnotherStructural;
import model.AnotherStructuralParent;
import model.SecondSingleComplexChoice;
import model.SomeAbstractComplexStructural;
import model.SomeChoiceSpecificStructuralSub;
import model.SomeCommonStructural;
import model.SomeComplexStructural;
import model.SomeFirstChoice;
import model.SomeSecondChoice;
import model.SomeStructural;
import model.SomeStructuralParent;
import model.Wrapper;
import model.YetAnotherSecondChoice;
import model.YetAnotherThirdChoice;

import org.junit.Test;

import se.miun.itm.input.AbstractInPUTTest;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.Q;

public abstract class IDesignSpaceTest extends AbstractInPUTTest {

	public static final String DESIGN_SPACE_FILE = "testSpace.xml";

	protected IDesignSpace space;

	@Test
	public void testNextPrimitive() {
		try {
			// primitive as well as auto-boxing should work for all of them!
			boolean someBoolean = space.next("SomeBoolean");
			Integer someInteger = space.next("SomeInteger");
			short someShort = space.next("SomeShort");
			Long someLong = space.next("SomeLong");
			double someDouble = space.next("SomeDouble");
			Float someFloat = space.next("SomeFloat");
			BigDecimal someDecimal = space.next("SomeDecimal");

			assertNotNull(someBoolean);
			assertNotNull(someInteger);
			assertNotNull(someShort);
			assertNotNull(someLong);
			assertNotNull(someDouble);
			assertNotNull(someFloat);
			assertNotNull(someDecimal);

		} catch (Exception e) {
			e.printStackTrace();
			fail("The random primitive type retrieval is not type safe.");
		}

	}

	@Test
	public void testNextString() {
		String value;
		try {
			value = space.next("SomeStringCustomizedByTheUser");
			assertEquals("The default String is expected.",
					"SomeStringCustomizedByTheUser", value);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Strings are not randomly created, a default String is expected in that case.");
		}

		try {
			value = space.next("AnotherStringCustomizedByTheUser");
			assertTrue(value.equals("someFile.xml")
					|| value.equals("someFile.txt")
					|| value.equals("anotherFile.xml"));
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("A problem instantiating the String occured.");
		}

		try {
			String[][] values = space
					.next("SomeStringArrayCustomizedByTheUser");
			assertEquals(10, values.length);
			assertEquals(5, values[0].length);
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[0].length; j++) {
					assertEquals("SomeStringArrayCustomizedByTheUser",
							values[i][j]);
				}
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("A problem instantiating the array String occured.");
		}
	}

	@Test
	public void testNextStructural() {

		try {

			SomeStructural someStructural = space.next("SomeStructural"); // check
																			// interface
			AnotherStructural anotherStructural = space
					.next("AnotherStructural"); // check enum
			SomeStructuralParent someStructuralParent = space
					.next("SomeStructuralParent"); // check abstract class
			AnotherStructuralParent deepStructuralParent = space
					.next("AnotherStructuralParent");

			assertNotNull(someStructural);
			assertNotNull(anotherStructural);
			assertNotNull(someStructuralParent);
			assertNotNull(deepStructuralParent);

		} catch (Exception e) {
			e.printStackTrace();
			fail("The random structural type retrieval is not type safe.");
		}
	}

	@Test
	public void testNextArray() {
		int[] primitiveUnspecifiedArray;
		try {
			primitiveUnspecifiedArray = space
					.next("SomePrimitiveArrayOfUnspecifiedSize");
			assertEquals(1, primitiveUnspecifiedArray.length);
			double[] primitiveSpecifiedArray = space
					.next("SomePrimitiveArrayOfSpecifiedSize");
			assertEquals(42, primitiveSpecifiedArray.length);

			long[][][][] primitiveArray = space.next("SomeLargePrimitiveArray");
			assertEquals(42, primitiveArray.length);
			assertEquals(1, primitiveArray[0].length);
			assertEquals(42, primitiveArray[0][0].length);
			assertEquals(1, primitiveArray[0][0][0].length);

			SomeStructural[][][] structuralArray = space
					.next("SomeStructuralArrayOfUnspecifiedSize");
			assertEquals(1, structuralArray.length);
			assertEquals(1, structuralArray[0].length);
			assertEquals(1, structuralArray[0][0].length);
			

		} catch (InPUTException e) {
			e.printStackTrace();
			fail("Array initialization didn't work out properly.");
		}

	}

	/**
	 * negative test
	 */
	@SuppressWarnings("unused")
	@Test
	public void testNextNegative() {

		try {
			assertNull(space.next("ParamThatDoesNotExist"));
			assertNull(space.next(null));
		} catch (InPUTException e1) {
			e1.printStackTrace();
			fail("The API should handle false input.");
		}

		try {
			int someBoolean = space.next("SomeBoolean");
			fail("A boolean should not be directly castable to an int.");
		} catch (Exception e) {
		}

		try {
			AnotherStructural anotherStructural = space.next("SomeStructural");
			fail("random structural types cannot be freely casted.");
		} catch (Exception e) {

		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testNextSubParameter() {
		try {
			int primitive = space
					.next("SomeStructuralParent.SomeSharedPrimitiveSub");
			SomeChoiceSpecificStructuralSub structural = space
					.next("AnotherStructuralParent.AndYetAnotherSecondChoice.SomeChoiceSpecificStructuralSub.AlsoSingleChoicesAreValid");
			assertNotNull(structural);
		} catch (InPUTException e) {
			fail("Something went wrong with the sub parameter random creation");
		}
	}

	@Test
	public void testNextChoiceParameter() {
		try {
			AndYetAnotherSecondChoice choice = space
					.next("AnotherStructuralParent.AndYetAnotherSecondChoice");
			assertNotNull(choice);
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("Something went wrong with the random creation of a choice");
		}
	}

	@Test
	public void testNextRestriction() {
		short restrictedValue;
		double anotherRestrictedValue;
		try {
			for (int i = 0; i < 10; i++) {
				restrictedValue = space.next("SomeRestrictedPrimitive");
				if (restrictedValue > 42 || restrictedValue < -42)
					fail();

				anotherRestrictedValue = space
						.next("AnotherRestrictedPrimitive");
				if (anotherRestrictedValue < .1d
						|| (anotherRestrictedValue > .4 && anotherRestrictedValue < .8)
						|| anotherRestrictedValue > .9)
					fail();

			}

			BigDecimal exclMin = new BigDecimal(0.42222222222);
			BigDecimal exclMax = new BigDecimal(0.422222222221);
			BigDecimal veryRestricted;
			for (int i = 0; i < 10; i++) {
				veryRestricted = space.next("SomeVeryRestrictedPrimitive");
				if (veryRestricted.compareTo(exclMin) <= 0
						|| veryRestricted.compareTo(exclMax) >= 0)
					assertTrue(false);
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The restrictions do not work as they are supposed to.");
		}

	}

	@Test
	public void testNextFixed() {
		int fixed;
		int[] moreFixed;
		try {
			fixed = space.next("SomeFixed");
			assertEquals(42, fixed);
			moreFixed = space.next("SomeFixedArray");
			for (int i = 0; i < 42; i++) {
				assertEquals(42, moreFixed[i]);
			}
		} catch (InPUTException e) {
			fail("Fixed entries must be retrievable.");
		}
	}

	@Test
	public void testNextEmptyDesign() {
		try {
			IDesign design = space.nextEmptyDesign("designId");

			assertEquals("designId", design.getId());
			for (String paramId : space.getSupportedParamIds()) {
				assertNull(design.getValue(paramId));
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The initialization of the empty design went wrong.");
		}
	}

	@Test
	public void testNextDesign() {
		try {
			IDesign design = space.nextDesign("designId");

			assertEquals("designId", design.getId());

			int[] array = design.getValue("SomeFixedArray");
			for (int value : array)
				assertEquals(42, value);

			for (String paramId : space.getSupportedParamIds()) {
				if (!paramId.contains(".")) {
					assertNotNull("No entry exists for parameter " + paramId,
							design.getValue(paramId));
				}
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The random initialization of the design went wrong.");
		}
	}

	@Test
	public void testNextReadOnlyDesign() {
		try {
			IDesign design = space.nextDesign("designId", true);
			design.setValue("SomeBoolean", false);
			fail("The design is supposed to be read-only.");

		} catch (InPUTException e) {
		}
	}

	@Test
	public void testNextWithDimensions() {
		try {
			int[] dimensions = { 3, 4, 1 };
			boolean[][][] someBooleans = space.next("SomeBoolean", dimensions);

			assertEquals(dimensions[0], someBooleans.length);
			assertEquals(dimensions[1], someBooleans[0].length);
			assertEquals(dimensions[2], someBooleans[0][0].length);

			SomeStructural[][][] someStructural = space.next("SomeStructural",
					dimensions);

			assertEquals(dimensions[0], someStructural.length);
			assertEquals(dimensions[1], someStructural[0].length);
			assertEquals(dimensions[2], someStructural[0][0].length);

		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The dimensional random value creation went wrong.");
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testNegativeNextWithDimensions() {
		try {
			int[][] dimensions = { { 1, 4, -3 }, { 0 } }; // all default values
															// are 1 : no empty
															// arrays are
															// returned
			boolean[][][] someBooleans = space.next("SomeBoolean",
					dimensions[0]);

			assertEquals(dimensions[0][0], someBooleans.length);
			assertEquals(dimensions[0][1], someBooleans[0].length);
			assertEquals(1, someBooleans[0][0].length);

			boolean singleBoolean = space.next("SomeBoolean", dimensions[1]); // single
																				// entry
																				// is
																				// expected
																				// for
																				// 0
																				// entries
																				// of
																				// higher
																				// dimension

			SomeStructural[][][] someStructural = space.next("SomeStructural",
					dimensions[0]);

			SomeStructural singleStructural = space.next("SomeStructural",
					dimensions[1]);
			assertNotNull(someStructural);

		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The API should cope with wrong dimensional input.");
		}
	}

	@Test
	public void testNextInjectCustomizedParameters() {

		Map<String, Object> subParams = new HashMap<String, Object>();
		subParams.put("AnotherSharedPrimitiveSub", 24l);
		subParams.put("SomeThingThatShouldNotHaveAnyImpact", "Foobar");

		try {
			SomeStructuralParent parent;
			for (int i = 0; i < 20; i++) {
				parent = space.next("SomeStructuralParent", subParams);
				assertEquals(subParams.get("AnotherSharedPrimitiveSub"),
						parent.getAnotherSharedPrimitiveSub());
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("A sub parameter should be possible to get injected as a constant by the user.");
		}
	}

	@Test
	public void testNextInjectCustomizedParametersAndConstructorOverwrite() {

		Object[] actualParams = { 14, 10f };
		Map<String, Object> subParams = new HashMap<String, Object>();
		subParams.put("AnotherSharedPrimitiveSub", 24l);
		subParams.put("SomeThingThatShouldNotHaveAnyImpact", "Foobar");

		try {
			SomeStructuralParent parent;
			for (int i = 0; i < 20; i++) {
				parent = space.next("SomeStructuralParent", subParams,
						actualParams);
				assertEquals(subParams.get("AnotherSharedPrimitiveSub"),
						parent.getAnotherSharedPrimitiveSub());

				if (parent instanceof YetAnotherSecondChoice)
					assertEquals(42, parent.getSomeSharedPrimitiveSub());
				else
					assertEquals(actualParams[0],
							parent.getSomeSharedPrimitiveSub());
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("A sub parameter should be possible to get injected as a constant by the user, and constructor parameters can be injected at the same time.");
		}
	}

	@Test
	public void testNextInjectCustomizedParametersWithDimensions() {
		int[] dimensions = { 3, 4 };

		Map<String, Object> subParams = new HashMap<String, Object>();
		subParams.put("AnotherSharedPrimitiveSub", 24l);
		subParams.put("SomeThingThatShouldNotHaveAnyImpact", "Foobar");

		try {
			SomeStructuralParent[][] parent;
			for (int i = 0; i < 20; i++) {
				parent = space.next("SomeStructuralParent", dimensions,
						subParams);
				for (int j = 0; j < parent.length; j++) {
					for (int k = 0; k < parent[j].length; k++) {
						assertEquals(
								subParams.get("AnotherSharedPrimitiveSub"),
								parent[j][k].getAnotherSharedPrimitiveSub());
					}
				}
			}
		} catch (InPUTException e) {
			e.printStackTrace();
			fail("A sub parameter should be possible to get injected as a constant by the user; also for high-dimensional randomization.");
		}
	}

	@Test
	public void testNextParameterConstructorOverwrite() {
		Object[] actualParams = { 14, 10f };
		try {
			SomeStructuralParent parent = space.next("SomeStructuralParent",
					actualParams);
			if (parent instanceof YetAnotherSecondChoice) {
				assertEquals(42, parent.getSomeSharedPrimitiveSub());
			} else {
				assertEquals(actualParams[0],
						parent.getSomeSharedPrimitiveSub());
			}

			if (parent instanceof YetAnotherThirdChoice) {
				assertEquals(((Float) actualParams[1]).floatValue(),
						((YetAnotherThirdChoice) parent)
								.getSomeChoiceSpecificPrimitiveSub(), 0.000001);
			}

			Object[] actualParamsWithBlank = { Q.BLANK, 10f };
			parent = space.next("SomeStructuralParent", actualParamsWithBlank);
			if (parent instanceof YetAnotherThirdChoice) {
				assertEquals(((Float) actualParams[1]).floatValue(),
						((YetAnotherThirdChoice) parent)
								.getSomeChoiceSpecificPrimitiveSub(), 0.000001);
			}

		} catch (InPUTException e) {
			e.printStackTrace();
			fail("The constructor entries should get injected, in cases where the constructor allows the setting (in the order they are given)");
		}
	}

	@Test
	public void testNegativeNextParameterConstructorOverride() {
		Object[] actualParams = { "SomeWrongInput", 10f };

		SomeStructuralParent result = null;
		try {
			result = space.next("SomeStructuralParent", actualParams);
			if (!(result instanceof YetAnotherSecondChoice)) {
				fail("Wrong input should not be tolerated and lead to a quick exception.");
			}
		} catch (InPUTException e) {
		}
	}

	@Test
	public void testIsFile() {
		assertTrue(space.isFile());
	}

	@Test
	public void testGetFileName() {
		assertEquals(DESIGN_SPACE_FILE, space.getFileName());
	}

	@Test
	public void testGetId() {
		assertEquals("testSpace", space.getId());
	}

	@Test
	public void testGetSupportedParamIds() {
		Set<String> paramIds = space.getSupportedParamIds();
		assertTrue(paramIds.contains("SomeStructural"));
		assertTrue(paramIds.contains("SomeFloat"));
		assertTrue(paramIds.contains("SomeDecimal"));

		assertFalse(paramIds.contains("IDontExist"));
		assertFalse(paramIds.contains("somedecimal"));
		assertFalse(paramIds.contains(null));
	}

	@Test
	public void testRelativeNumericConsistency() throws InPUTException {
		IDesign design;
		long someLong, aBiggerLong, aSmallerLong, aStrangeLong;

		for (int i = 0; i < 10; i++) {
			design = space.nextDesign("someId");
			someLong = design.getValue("SomeLong");
			aBiggerLong = design.getValue("ABiggerLong");
			aSmallerLong = design.getValue("ASmallerLong");
			aStrangeLong = design.getValue("AStrangeLong");

			assertTrue(aBiggerLong > someLong);
			assertTrue(someLong >= aSmallerLong);
			assertTrue(aStrangeLong >= someLong / aSmallerLong - aBiggerLong);
		}
	}

	@Test
	public void testCustomizableInput() throws InPUTException {
		// test setter getter customization
		SomeCommonStructural structural = space
				.next("CustomizableInputDemonstrator");

		// test wrapper
		Wrapper wrapper = structural.getPrimitive();
		assertNotNull(wrapper);
		assertNotNull(wrapper.toValue());

	}

	@Test
	public void testComplexStructural() throws InPUTException {
		SomeAbstractComplexStructural complex = space
				.next("SomeComplexStructural");
		SomeComplexStructural str = ((SomeComplexStructural) complex);
		assertTrue(str.getEntry(0) instanceof SecondSingleComplexChoice);
		assertFalse(str.getEntry(1) instanceof SecondSingleComplexChoice);
		assertEquals(3, str.size());
	}

	@Test
	public void testSetFixedNegative() throws InPUTException {
		try {
			space.setFixed("whatever", "2");
			space.setFixed("SomeBoolean", "whatever");
			space.setFixed("SomeInteger", "whatever");
			space.setFixed("SomeStructural", "whatever");
			fail();

		} catch (InPUTException e) {
			// the values should not be possible to get set
		}
	}

	@Test
	public void testSetFixedNumerical() throws InPUTException {
		space.setFixed("SomeInteger", "2");
		space.setFixed("SomeBoolean", "true");
		int integer;
		boolean b;
		for (int i = 0; i < 10; i++) {
			integer = space.next("SomeInteger");
			b = space.next("SomeBoolean");
			assertEquals(2, integer);
			assertEquals(true, b);
		}

		space.setFixed("SomeInteger", null);
		int counter = 0;
		for (int i = 0; i < 100; i++) {
			integer = space.next("SomeInteger");
			if (integer == 2)
				counter++;
			assertTrue(counter < 10);
		}
	}

	@Test
	public void testSetFixedStructural() throws InPUTException {
		ParamStore store = ParamStore.getInstance(space.getId());

		space.setFixed("SomeStructural", "SomeFirstChoice");
		assertTrue(store.getParam("SomeStructural").isFixed());

		SomeStructural value;
		for (int i = 0; i < 10; i++) {
			value = space.next("SomeStructural");
			assertTrue(value instanceof SomeFirstChoice);
		}

		space.setFixed("SomeStructural", "SomeSecondChoice");
		assertTrue(store.getParam("SomeStructural").isFixed());

		for (int i = 0; i < 10; i++) {
			value = space.next("SomeStructural");
			assertTrue(value instanceof SomeSecondChoice);
		}

		space.setFixed("SomeStructural", null);
		assertFalse(store.getParam("SomeStructural").isFixed());

		SomeStructural[][][] values = space.next("SomeStructuralArrayOfUnspecifiedSize");
		space.setFixed("SomeStructuralArrayOfUnspecifiedSize", "FirstChoice");
		assertTrue(store.getParam("SomeStructuralArrayOfUnspecifiedSize").isFixed());
		for (int k = 0; k < 10; k++) {
			values = space.next("SomeStructuralArrayOfUnspecifiedSize");
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					for (int l = 0; l < values[i][j].length; l++) {
						assertTrue(values[i][j][l] instanceof SomeFirstChoice);
					}
				}
			}
		}
		
		space.setFixed("SomeStructuralArrayOfUnspecifiedSize", null);
		assertFalse(store.getParam("SomeStructuralArrayOfUnspecifiedSize").isFixed());
		
		space.setFixed("SomeStructuralArrayOfUnspecifiedSize", "SecondChoice");
		assertTrue(store.getParam("SomeStructuralArrayOfUnspecifiedSize").isFixed());
		for (int k = 0; k < 10; k++) {
			values = space.next("SomeStructuralArrayOfUnspecifiedSize");
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					for (int l = 0; l < values[i][j].length; l++) {
						assertTrue(values[i][j][l] instanceof SomeSecondChoice);
					}
				}
			}
		}
	}

	@Test
	public void testSetFixedComplex() throws InPUTException {
		// /TODO
		fail();
	}
}