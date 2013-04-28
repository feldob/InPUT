package se.miun.itm.input.model.design;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;

import model.AnotherStructural;
import model.AnotherStructuralParent;
import model.AnotherSubChoice;
import model.SingleComplexChoice;
import model.SomeAbstractComplexStructural;
import model.SomeCommonStructural;
import model.SomeComplexStructural;
import model.SomeFirstChoice;
import model.SomeSecondChoice;
import model.SomeSharedStructuralSub;
import model.SomeStructural;
import model.SomeStructuralParent;
import model.SomeSubChoice;
import model.Wrapper;

import org.junit.Test;

import se.miun.itm.input.AbstractInPUTTest;
import se.miun.itm.input.export.ByteArrayExporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.InPUTException;

public abstract class IDesignTest extends AbstractInPUTTest {

	private static final double PRECISION = 0.000001;

	public static final String DESIGN_FILE = "testDesign.xml";

	protected IDesign design;

	@Test(expected=InPUTException.class)
	public void testSetReadOnly() throws InPUTException{
		design.setReadOnly();
		design.setValue("SomeBoolean", false);
		fail("only read should disallow the setting of values!");
	}

	@Test
	public void testGetSpace() throws InPUTException {
		IDesignSpace space = design.getSpace();
		assertNotNull(space);
		assertTrue(space.isFile());
		assertEquals("testSpace", space.getId());
	}

	@Test
	public void testExtendScope() throws InPUTException {
		design.extendScope(null);
		design.extendScope(design);
		IDesign anotherDesign = new Design("anotherTestDesign.xml");
		design.extendScope(anotherDesign);

		try {
			int anotherInteger = design.getValue("AnotherInteger");
			assertEquals(42, anotherInteger);
			assertEquals(anotherInteger, anotherDesign.getValue("AnotherInteger"));
		} catch (InPUTException e) {
			fail("The AnotherInteger should now be available from design.");
		}
		
	}

	@Test
	public void testGetId() {
		assertEquals("testDesign", design.getId());
	}

	@Test
	public void testGetPrimitive() throws InPUTException {
		
		testGetSet(false, "SomeBoolean");
		testGetSet(-1966342580, "SomeInteger");
		testGetSet((short)-7448, "SomeShort");
		testGetSet(1700584710333745153l, "SomeLong");
		assertEquals(0.12345778699671628,(Double)design.getValue("SomeDouble"), PRECISION);
		assertEquals(0.73908234,(Float)design.getValue("SomeFloat"), PRECISION);
		assertEquals(new BigDecimal(-7889858943241994240.07228988965664218113715833169408142566680908203125).floatValue(),((BigDecimal)design.getValue("SomeDecimal")).floatValue(), PRECISION);
	}

	private void testGetSet(Object expected, String paramId) throws InPUTException {
		Object value = design.getValue(paramId);
		assertNotNull(value);
		design.setValue(paramId, value);
		value = design.getValue(paramId);
		assertEquals(expected, value);
	}

	@Test
	public void testSetPrimitive() throws InPUTException {
		design.setValue("SomeBoolean", true);
		assertEquals(true,design.getValue("SomeBoolean"));
		design.setValue("SomeInteger", 1);
		assertEquals(1,design.getValue("SomeInteger"));
		design.setValue("SomeShort", (short)42);
		assertEquals((short)42,design.getValue("SomeShort"));
		design.setValue("SomeLong", 1l);
		assertEquals(1l,design.getValue("SomeLong"));
		design.setValue("SomeDouble", .42d);
		assertEquals(0.42d,(Double)design.getValue("SomeDouble"), PRECISION);
		design.setValue("SomeFloat", .84f);
		assertEquals(0.84f,(Float)design.getValue("SomeFloat"), PRECISION);
		design.setValue("SomeDecimal", new BigDecimal(42));
		assertEquals(new BigDecimal(42).floatValue(),((BigDecimal)design.getValue("SomeDecimal")).floatValue(), PRECISION);
	}

	@Test
	public void testSetPrimitiveNegative() throws InPUTException {
		
		try {
			design.setValue("SomeBoolean", 1);
			fail("not allowed to set int for boolean!");
		} catch (Exception e) {
		}
		
		try {
			design.setValue("SomeDouble", true);
			fail("not allowed to set boolean for double!");
		} catch (Exception e) {
		}
		try {
			design.setValue("SomeLong", 0.84f);
			fail("not allowed to set float for long!");
		} catch (Exception e) {
		}
		try {
			design.setValue("SomeFloat", 1l);
			fail("not allowed to set long for float!");
		} catch (Exception e) {
		}
		try {
			design.setValue("SomeDecimal", (short)42);
			fail("not allowed to set short for bigdecimal!");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testGetEnum() throws InPUTException {
		testGetSet(AnotherStructural.FIRST, "AnotherStructural");
	}
	
	@Test
	public void testSetEnum() throws InPUTException {
		design.setValue("AnotherStructural", AnotherStructural.THIRD);
		AnotherStructural anotherStruct = design.getValue("AnotherStructural");
		assertNotNull(anotherStruct);
		assertEquals(AnotherStructural.THIRD, anotherStruct);
	}
	
	@Test
	public void testGetStringParameter() throws InPUTException {
		testGetSet("SomeStringCustomizedByTheUser", "SomeStringCustomizedByTheUser");
		testGetSet("anotherFile.xml", "AnotherStringCustomizedByTheUser");
	}
	
	@Test
	public void testGetInjectedPrimitive() throws InPUTException {
		SomeStructuralParent parent = design.getValue("SomeStructuralParent");
		assertEquals(5938400921453047807l, parent.getAnotherSharedPrimitiveSub());
		
		long primitive = design.getValue("SomeStructuralParent.AnotherSharedPrimitiveSub");
		assertEquals(parent.getAnotherSharedPrimitiveSub(), primitive);
	}

	@Test
	public void testSetRelativePrimitives() throws InPUTException {
		long someLong = design.getValue("SomeLong");
		try {
			design.setValue("ABiggerLong", someLong);
			fail("somelong is excluded from the bigger long range!");
		} catch (Exception e) {
		}
		
		try {
			design.setValue("ABiggerLong", someLong + 1l);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The extended somelong fits into the range!");
		}
	}

	@Test
	public void testGetInjectedStructural() throws InPUTException {
		AnotherStructuralParent parent = design.getValue("AnotherStructuralParent");
		design.setValue("AnotherStructuralParent", parent);
		parent = design.getValue("AnotherStructuralParent");
		
		assertNotNull(parent.getSomeSharedStructuralSub());
		
		SomeSharedStructuralSub structural = design.getValue("AnotherStructuralParent.SomeSharedStructuralSub");
		assertEquals(parent.getSomeSharedStructuralSub(), structural);
	}
	

	@Test
	public void testSetInjectedStructural() throws InPUTException {
		SomeSharedStructuralSub someShared = new SomeSubChoice();
		SomeSharedStructuralSub currentShared = design.getValue("AnotherStructuralParent.SomeSharedStructuralSub");
		assertNotSame(someShared, currentShared);
		try {
			design.setValue("AnotherStructuralParent.SomeSharedStructuralSub", someShared);
			fail("Constructor injected should not be settable that way!");
		} catch (Exception e) {
		}
		
		currentShared = design.getValue("AnotherStructuralParent.SomeSharedStructuralSub");
		
		assertNotSame("The injection only works if the parameter is NOT instantiated by the constructor.", someShared, currentShared);
		
		String currentString = design.getValue("SomeStructuralParent.SomeSharedStructuralSub");
		
		String anotherString = "anotherString";
		
		design.setValue("SomeStructuralParent.SomeSharedStructuralSub", anotherString);
		currentString = design.getValue("SomeStructuralParent.SomeSharedStructuralSub");
		
		assertEquals("The injection works if the parameter is instantiated by setter injection.", anotherString, currentString);
		
		SomeStructuralParent parent = design.getValue("SomeStructuralParent");
		assertEquals(anotherString, parent.getSomeSharedStructuralSub());
	}
	
	@Test
	public void testGetCustomizableGetter() throws InPUTException {
		double value = 2.860933188245651E-4;
		double customizableGetter = design.getValue("CustomizableInputDemonstrator.CustomizableSetGetPrimitive");
		SomeCommonStructural parent = design.getValue("CustomizableInputDemonstrator");
		assertEquals(value, customizableGetter, PRECISION);
		assertEquals(value, parent.andTheCustomizableGetter(), PRECISION);
	}
	

	@Test
	public void testSetCustomizedGetter() throws InPUTException {
		double value = 0.5;
		design.setValue("CustomizableInputDemonstrator.CustomizableSetGetPrimitive", value);
		SomeCommonStructural parent = design.getValue("CustomizableInputDemonstrator");
		assertEquals(value, parent.andTheCustomizableGetter(), PRECISION);
	}
	

	@Test
	public void testGetStructural() throws InPUTException {
		SomeStructural structural = design.getValue("SomeStructural");
		assertTrue(structural instanceof SomeSecondChoice);
	}

	@Test(expected=InPUTException.class)
	public void testSetStructural() throws InPUTException {
		SomeStructural choice = new SomeFirstChoice();
		design.setValue("SomeStructural", choice);
		assertEquals(choice, design.getValue("SomeStructural"));
		choice = new SomeSecondChoice();
		design.setValue("SomeStructural", choice);
		assertEquals(choice, design.getValue("SomeStructural"));
		
		design.setValue("SomeStructural", new AnotherSubChoice());
		fail("This type may not be set to the defined parameter.");
	}

	@Test
	public void testGetWrapper() throws InPUTException {
		Wrapper value = design.getValue("CustomizableInputDemonstrator.WrappedPrimitive");
		assertEquals(0.9369297592420026, value.toValue(), PRECISION);

		SomeCommonStructural parent = design.getValue("CustomizableInputDemonstrator");
		assertEquals(value, parent.getPrimitive());
	}
	

	@Test
	public void testSetWrapper() throws InPUTException {
		Wrapper value = new Wrapper(.3);
		design.setValue("CustomizableInputDemonstrator.WrappedPrimitive", value);
		Wrapper current = design.getValue("CustomizableInputDemonstrator.WrappedPrimitive");
		assertEquals(value, current);

		SomeCommonStructural parent = design.getValue("CustomizableInputDemonstrator");
		assertEquals(value, parent.getPrimitive());
	}
	

	@Test
	public void testGetArray() throws InPUTException {
		int[] array = design.getValue("SomeFixedArray");
		assertEquals(42, array.length);
		for (int value : array)
			assertEquals(42, value);
		
		int value = design.getValue("SomeFixedArray.1");
		assertEquals(42, value);
		value = design.getValue("SomeFixedArray.42");
		assertEquals(42, value);
		
		Object empty = design.getValue("SomeFixedArray.43");
		assertNull("There is no such element 43 allowed for the array.",empty);
		
		long[][][][] largePrimitives = design.getValue("SomeLargePrimitiveArray");
		assertEquals(42, largePrimitives.length);
		assertEquals(42, largePrimitives[0][0].length);
	}

	@Test
	public void testSetArray() throws InPUTException {
		final long value = 13;
		design.setValue("SomeLargePrimitiveArray.1.1.1.1", value);
		long current = design.getValue("SomeLargePrimitiveArray.1.1.1.1");
		
		assertEquals(value, current);
		
		long[] values = {1,2,3};
		
		design.setValue("SomeLargePrimitiveArray.1.1.42", values);
		long[] currentValues = design.getValue("SomeLargePrimitiveArray.1.1.42");
		
		try {
			design.setValue("SomeLargePrimitiveArray.1.1.1.1", values);
			fail();
		} catch (Exception e) {}
		
		try {
			design.setValue("SomeLargePrimitiveArray.1.1.42", value);
			fail();
		} catch (Exception e) {}
		
		assertTrue(Arrays.equals(values, currentValues));
	}

	@Test
	public void testSetArrayNegative() throws InPUTException {
		
		try {
			design.setValue("SomeFixedArray.43", 42);
			fail("There is no such array position");
		} catch (Exception e) {
			
		}
		
		try {
			design.setValue("SomeFixedArray.1", 44);
			fail("another value than the fixed one may not be set");
		} catch (Exception e) {
			
		}
	}

	@Test
	public void testGetComplex() throws InPUTException {
		SomeAbstractComplexStructural complex = design.getValue("SomeComplexStructural");
		if (complex instanceof SomeComplexStructural) {
			SomeComplexStructural struct = (SomeComplexStructural) complex;
			assertEquals(3, struct.size());
			design.setValue("SomeComplexStructural", struct);
			assertEquals(complex, design.getValue("SomeComplexStructural"));
		}else
			fail("The complex type should be as the defined one.");
	}

	@Test
	public void testSetComplex() throws InPUTException {
		SomeComplexStructural complex = new SomeComplexStructural();
		for (int i = 0; i < 4; i++)
			complex.addEntry(new SingleComplexChoice());
		
		design.setValue("SomeComplexStructural", complex);
		
		SomeComplexStructural current = null;
		try {
			current = design.getValue("SomeComplexStructural");
			
		} catch (Exception e) {
			fail("We know that the type cast should be of SomeComplexStructural subtype!");
		}
		
		design.export(new ByteArrayExporter());
		
		assertEquals(complex.size(), current.size());
	}
	
	@Test
	public void testGetNegative() throws InPUTException {
		assertNull(design.getValue("IDoNotExist"));
		assertNull(design.getValue("IDoNotExist", null));
		assertNull(design.getValue("IDoNotExist", new Object[0]));
		
		assertNull(design.getValue(null));
		assertNull(design.getValue(null, null));
		assertNull(design.getValue(null, new Object[0]));
	}
	
	@Test
	public void testSetNegative() throws InPUTException {

		try {
			design.setValue("IDoNotExist", design);
			fail("Setting a not know parameter should result in an exception.");
		} catch (Exception e) {
		}
		
		try {
			design.setValue("IDoNotExist", null);
			fail("Null value setting should result in an exception.");
		} catch (Exception e) {
		}
		
		try {
			design.setValue("SomeStructuralParent", null);
			fail("Null value setting should result in an exception.");
		} catch (Exception e) {
		}
		assertNotNull(design.getValue("SomeStructuralParent"));
	}

	@Test
	public void testSetFixed() throws InPUTException{
		try {
			design.setValue("SomeFixed", 43);
			fail("A fixed value should not be settable, if it already has a value!");
		} catch (InPUTException e) {
		}
//		IDesign anotherDesign = design.getSpace().nextEmptyDesign("test");
		//TODO should those fixed entries already be set in an EMPTY design?!
//		int value = anotherDesign.getValue("SomeFixed");
//		assertEquals(value, 42);
	}
	
	@Test
	public void testExport() throws InPUTException {
		final String designName = "someOtherTestDesign.xml";
		design.export(new XMLFileExporter(designName));
		IDesign design2 = design.getSpace().impOrt(new XMLFileImporter(designName));
		
		if (!design.same(design2)) {
			fail();
		};
		
		
		new File(designName).delete();
	}
}