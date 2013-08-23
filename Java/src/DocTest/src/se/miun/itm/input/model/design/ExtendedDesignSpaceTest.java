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
*/
package se.miun.itm.input.model.design;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.ParamStore;

/**
 * Felix: resolved.
 * 
 * The following tests are explicitly meant as documentation tests.
 * AKA executable documentation. It is not necessarily the case that
 * the tested behavior is the right behavior, in the sense that this
 * is what the program is supposed to do. These tests are intended to
 * document what the program in fact does, given some corner cases.
 * <p>
 * See the configuration files for more detailed comments.
 * 
 * @author Christoffer Fink
 */
public class ExtendedDesignSpaceTest {
	@After
	public void cleanup() {
		ParamStore.releaseAllParamStores();
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test fails to create a DesignSpace from the configuration
	 * due to a circular dependency. The name of the first parameter
	 * is parsed as a literal value rather than a reference to a param.
	 * This results in a NumberFormatException.
	 * @see #longerCircularDependencyCausesDifferentException()
	 * @see #mixedCircularDependencyCausesException()
	 * @see #twoUnrelatedGroupsOfParametersCauseError()
	 * @throws InPUTException never
	 */
	@Test(expected=InPUTException.class)
	public void shortCircularDependencyCausesException()
			throws InPUTException {
		final String designSpaceFile = "circularDependencySpace01.xml";
		new DesignSpace(designSpaceFile);
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test is similar to the shortCircularDependency test. The only
	 * difference is that the chain consists of three parameters rather
	 * than two. Creating the DesignSpace works fine in this case, but
	 * using the space to create a Design does not work.
	 * @see #shortCircularDependencyCausesException()
	 * @see #mixedCircularDependencyCausesException()
	 * @see #twoUnrelatedGroupsOfParametersCauseError()
	 * @throws InPUTException never
	 */
	@Test(expected=InPUTException.class)
	public void longerCircularDependencyCausesDifferentException()
			throws InPUTException {
		final String designSpaceFile = "circularDependencySpace02.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		fail("Trying to create the Design was supposed to fail.");
//			space.nextDesign("design");
	}

	/**
	 * Felix: resolved.
	 * 
	 * While the short and long circularDependency tests only used exclMin
	 * as a relation, this circular dependency mixes all four kinds. The
	 * chain is therefore four parameters long. The result is the same as
	 * for the longerCircularDependency test.
	 * @see #shortCircularDependencyCausesException()
	 * @see #longerCircularDependencyCausesDifferentException()
	 * @throws InPUTException never
	 */
	@Test(expected=InPUTException.class)
	public void mixedCircularDependencyCausesException()
			throws InPUTException {
		final String designSpaceFile = "circularDependencySpace03.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		try {
			space.nextDesign("design");
			fail("Trying to create the Design was supposed to fail.");
		} catch(InPUTException e) { }
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test combines 02 and 03. In both tests, creating a DesignSpace
	 * works. The tests only fail when trying to create a Design. However,
	 * including both groups of parameters prevents even the DesignSpace
	 * from being created.
	 * @see #shortCircularDependencyCausesException()
	 * @see #longerCircularDependencyCausesDifferentException()
	 * @throws InPUTException never
	 */
	@Test(expected=Exception.class)
	public void twoUnrelatedGroupsOfParametersCauseError()
			throws InPUTException {
		final String designSpaceFile = "circularDependencySpace04.xml";
		new DesignSpace(designSpaceFile);
	}

	/**
	 * Felix: expected behavior. It could be discussed to have a default init method that initializes everything fixed, after the empty init.
	 * 
	 * This test shows that even a fixed parameter will be null in an empty
	 * Design.
	 * @throws InPUTException never
	 */
	@Test
	public void fixedValueInEmptyDesignIsAlsoNull() throws InPUTException {
		final String designSpaceFile = "testSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);

		// When generating a normal, non-empty Design, F should be 43.
		IDesign nonEmpty = space.nextDesign("nonempty");
		final int f = nonEmpty.getValue("F");
		assertEquals("F is supposed to be fixed to 43", 43, f);

		// However, when generating an empty design, even F is null.
		IDesign empty = space.nextEmptyDesign("empty");
		assertNull("F must be null in an empty Design.", empty.getValue("F"));
		
		empty.setValue("F", 43);
		
		// Confirm that F is indeed fixed.
		try {
			empty.setValue("F", 2);
			fail("Setting F to anything but the fixed value should fail.");
		} catch(InPUTException e) { }
	}

	/**
	 * Felix: expected behavior.
	 * 
	 * This test confirms that it is impossible to create a DesignSpace
	 * where a parameter has explicit overlapping restrictions
	 * (the min/max limits leave no valid values).
	 * @throws InPUTException never
	 */
	@Test
	public void creatingImpossibleDesignSpaceFails() throws InPUTException {
		// All of these should fail when trying to create the DesignSpace.
		final String[] designSpaceFiles = {
			"impossibleSpace01.xml", "impossibleSpace02.xml",
			"impossibleSpace03.xml",
		};
		for(String file : designSpaceFiles) {
			try {
				new DesignSpace(file);
				fail(file + " should cause an exception.");
			} catch(InPUTException e) { }
		}
	}

	/**
	 * Felix: acceptable but undesirable behavior. Requires to check for range-violations among the relations, which also has to consider transitivity.
	 * 
	 * This test demonstrates a bug.
	 * The DesignSpace defines A such that the only valid value is 2.
	 * B is then defined to be at least 2, but strictly less than A, which
	 * is impossible. Not only can a DesignSpace be created from this
	 * configuration, a Design can also be created based on the DesignSpace.
	 * A is initialized as expected, but B is initialized to an illegal value.
	 * @see #setFixedBypassesRanges()
	 * @see #designDependsOnSpace()
	 * @throws InPUTException never
	 */
	@Test
	public void creatingDesignFromImpossibleDesignSpaceUnexpectedlyWorks()
			throws InPUTException {
		final String designSpaceFile = "impossibleSpace04.xml";
		
		try {
			DesignSpace space = new DesignSpace(designSpaceFile);
			fail("invalid range design space should not be able to get created.");
			// Because the set of possible values for B is empty, one would
			// expect that a design cannot be created.
			IDesign design = space.nextDesign("impossibleDesign");
			
			// B is explicitly defined not to be able to take on the same
			// value as A, yet the two are equal.
			final int b = design.getValue("B");
			final int a = design.getValue("A");
			assertEquals("A and B can only be equal.", a, b);
			// Since B could only be set to an invalid value, trying to set
			// it to the same value again fails as expected.
			try {
				design.setValue("B", b);
				fail("Cannot re-set B to the same value.");
			} catch(IllegalArgumentException e) { }
		} catch (Exception e) {
			
		}
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test confirms that inclMin=x and inclMax=x is a valid range.
	 * @throws InPUTException never
	 */
	@Test
	public void creatingPossibleDesignSpace() throws InPUTException {
		final String designSpaceFile = "possibleSpace.xml";
		new DesignSpace(designSpaceFile);
	}

	/**
	 * This test demonstrates that the referenced parameters are initialized
	 * independent of the ranges of the dependent parameters. In this design
	 * space, the D parameter can only be initialized to a valid value if A
	 * and B are both set to 0. C, on the other hand, can never be set to
	 * a valid value.
	 * <p>
	 * This test is similar to the
	 * creatingDesignFromImpossibleDesignSpaceUnexpectedlyWorks test above.
	 * The difference is that the ranges are much bigger. In that test, A
	 * can only be set to a single value in any case. Here, A and B can take
	 * on many values, but should they?
	 * @see #creatingDesignFromImpossibleDesignSpaceUnexpectedlyWorks()
	 * @throws InPUTException never
	 */
	@Test
	public void initializingParameterWithTinyRange() throws InPUTException {
		final String designSpaceFile = "overlapSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");
		final long a = design.getValue("A");
		final long b = design.getValue("B");
		assertEquals(0,a);
		assertEquals(0,b);
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test demonstrates that a parameter ID is allowed to contain
	 * a dot.
	 * @see #anArrayElementWithTheSameIdAsAnotherParameterTakesPrecedence()
	 * @see #duplicateIDsAreLegal()
	 * @throws InPUTException never
	 */
	@Test(expected=IllegalArgumentException.class)
	public void singleParameterWithDotInTheIdIsLegal() throws InPUTException {
		final String designSpaceFile = "dottedNameSpace01.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
//		IDesign design = space.nextDesign("design");
//		assertEquals(43, design.getValue("A.1.2.3"));
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test demonstrates that it is legal for an array element to
	 * have the same ID as another parameter. Getting the value for such
	 * a shared ID returns the array element.
	 * @see #singleParameterWithDotInTheIdIsLegal()
	 * @see #duplicateIDsAreLegal()
	 * @throws InPUTException never
	 */
	@Test(expected=IllegalArgumentException.class)
	public void anArrayElementWithTheSameIdAsAnotherParameterTakesPrecedence()
			throws InPUTException {
		final String designSpaceFile = "dottedNameSpace02.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
//		IDesign design = space.nextDesign("design");
//		assertEquals(10, design.getValue("A.1"));
	}

	/**
	 * This test demonstrates that duplicate IDs are legal.
	 * In other words, a DesignSpace can contain multiple parameters with
	 * the same ID. It seems to be the case that the one that is declared
	 * last is the one that remains.
	 * @see #singleParameterWithDotInTheIdIsLegal()
	 * @see #anArrayElementWithTheSameIdAsAnotherParameterTakesPrecedence()
	 * @throws InPUTException never
	 */
	@Test
	public void duplicateIDsAreLegal() throws InPUTException {
		final String designSpaceFile = "duplicateIdSpace01.xml";
		
		try {
			DesignSpace space = new DesignSpace(designSpaceFile);
			fail("illegal design space definition.");
			IDesign design = space.nextDesign("design");
			assertEquals(10, design.getValue("A"));
			final int params = design.getSupportedParamIds().size();
			assertEquals("Expected only 1 parameter in the design.", 1, params);
			
		} catch (Exception e) {
			
		}
	}

	/**
	 * Felix: valid behavior.
	 * 
	 * This test demonstrates that an empty string is a valid Design id.
	 * @throws InPUTException never
	 */
	@Test
	public void creatingDesignWithEmptyIdIsLegal() throws InPUTException {
		final String designSpaceFile = "possibleSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("");
		assertEquals("", design.getId());
	}

	/**
	 * Felix: valid behavior, because unnecessary.
	 * 
	 * This test demonstrates that simple arithmetic expressions (in this
	 * case "1 + 2") are not evaluated. Instead they are parsed as
	 * numbers, causing a NumberFormatException.
	 * @throws InPUTException never
	 */
	@Test
	public void simpleArithmeticExpressionsCannotBeEvaluated()
			throws InPUTException {
		final String designSpaceFile = "simpleArithmeticSpace.xml";
		try {
			new DesignSpace(designSpaceFile);
			fail("Creating the design space is expected to fail.");
		} catch(NumberFormatException e) { }
	}

	/**
	 * This test demonstrates that evaluating a single parameter reference
	 * will fail. In other words, if an expression consists only of a
	 * parameter name and no other operators, then the expression
	 * cannot be evaluated.
	 * <p>
	 * This means that, if a parameter should be defined simply in terms
	 * of another parameter, then it must still be part of some larger
	 * NOP expression. "C + 0" is one solution. "C * 1" is another.
	 * @throws InPUTException never
	 */
	@Test
	public void bareParameterReferencesAreNotEvaluated() throws InPUTException {
		final String designSpaceFile = "referenceSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		try {
			// inclMax="C" does not work.
			int value = space.next("A");
			fail("Expected: could not process the expression 'C' exception");
		} catch(InPUTException e) { }
		// inclMax="B+0" does work.
		assertEquals(1, space.next("B"));
		// Primitive limits. This is always expected to work.
		assertEquals(1, space.next("C"));
	}

	/**
	 * This test demonstrates that fixed parameters cannot be set to an
	 * expression. The expression isn't evaluated and thus causes a
	 * NumberFormatException.
	 * @throws InPUTException never
	 */
	@Test
	public void fixedParametersAreNotEvaluated() throws InPUTException {
		final String designSpaceFile = "fixedRelativeSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		try {
			IDesign design = space.nextDesign("design id");
			int a = design.getValue("A");
			int c = design.getValue("C");
			
			assertEquals(43, a);
			assertEquals(86, c);
		} catch(NumberFormatException e) { }
	}

	/**
	 * This test demonstrates a bug.
	 * While a design is correctly initialized according to the design
	 * space, the design space does not return the correct value when
	 * calling {@code next}.
	 * <p>
	 * The design space will gladly generate illegal values. Any parameter
	 * references seem to evaluate to 0. Then the rest of the expression
	 * is evaluated accordingly.
	 * <p>
	 * In both cases, the design is properly initialized and will always
	 * return the expected values.
	 * @throws InPUTException never
	 */
	@Test
	public void nextDoesNotProduceTheCorrectValueForExpressions()
			throws InPUTException {
		final String designSpaceFile = "relativeSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");

		// Confirming that both are "fixed".
		assertEquals("X should be fixed", 2, space.next("X"));
		assertEquals("Y should be limited", 2, space.next("Y"));

		// Both are defined to be twice as large as X and Y.
		// Yet both are 0.
		assertEquals("Expected A to be the wrong value.", 0, space.next("A"));
		assertEquals("Expected B to be the wrong value.", 0, space.next("B"));

		// These two are defined with an expression and a literal.
		// Their range is limited to 4, yet they generate random values.
		checkRandomness(space, "C");
		checkRandomness(space, "D");

		// This works (using design) but using space.next() does not.
		assertEquals("A should be limited to X*2", 4, design.getValue("A"));
		assertEquals("B should be limited to Y*2", 4, design.getValue("B"));
		assertEquals("C should always be 4", 4, design.getValue("C"));
		assertEquals("D should always be 4", 4, design.getValue("D"));
	}

	/**
	 * This test demonstrates that spaces are allowed in IDs, and that
	 * the DesignSpace can be created. However, an expression involving
	 * a parameter ID with white space cannot be evaluated.
	 * @throws InPUTException never
	 */
	@Test
	public void spaceInIdIsLegal() throws InPUTException {
		final String designSpaceFile = "spaceInIdSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		try {
			space.nextDesign("design");
			fail("Creating the design is expected to fail.");
		} catch(InPUTException e) { }
	}

	/**
	 * This test demonstrates that fixed parameters can be fixed to
	 * new values (being fixed doesn't prevent them from getting fixed).
	 * The parameter was initially unfixed in the design space configuration.
	 * @throws InPUTException never
	 */
	@Test
	public void setFixedMultipleTimesIsLegal() throws InPUTException {
		final String designSpaceFile = "possibleSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		space.setFixed("A", "2");
		space.setFixed("A", "2");
	}

	/**
	 * Felix: This is a wanted feature. However, there should be two modi, one where 
	 * the design space is read only, and one where it can be changed.
	 * 
	 * This test demonstrates that fixed parameters can be fixed to new
	 * values even when they were defined as fixed in the design space
	 * configuration.
	 * @throws InPUTException never
	 */
	@Test
	public void fixedParametersCanBeFixedToNewValues() throws InPUTException {
		final String designSpaceFile = "fixedRelativeSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		space.setFixed("A", "2");
	}

	/**
	 * This test demonstrates that setting a fixed value bypasses range
	 * checks. That is, a parameter can be set to an out-of-range value.
	 * The only legal value for A is 2, but we can set it to 100.
	 * @see #designDependsOnSpace()
	 * @throws InPUTException never
	 */
	@Test
	public void setFixedBypassesRanges() throws InPUTException {
		final String designSpaceFile = "possibleSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		space.setFixed("A", "100");
		IDesign design = space.nextDesign("design");
		assertEquals(100, design.getValue("A"));
	}

	/**
	 * This test demonstrates that an integer parameter can be fixed to
	 * a floating point value. The value will simply be truncated.
	 * @throws InPUTException never
	 */
	@Test
	public void integerParameterFixedToFloatIsTruncated()
			throws InPUTException {
		final String designSpaceFile = "typeMismatchSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		assertEquals("Expected A to be truncated to 2.", 2, space.next("A"));
	}

	/**
	 * This test demonstrates that an integer parameter can be defined
	 * by a range of floating point values. They will both be truncated.
	 * @throws InPUTException never
	 */
	@Test
	public void floatRangeIsTruncatedForIntegerParameter()
			throws InPUTException {
		final String designSpaceFile = "floatRangeSpace.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		assertEquals("The only legal value should be 2.", 2, space.next("A"));
	}

	/**
	 * This test demonstrates that the boolean literals are case insensitive.
	 * @see #anythingButTrueEvaluatesToFalse()
	 * @throws InPUTException never
	 */
	@Test
	public void boolLiteralsAreCaseInsensitive() throws InPUTException {
		final String designSpaceFile = "boolParamSpace01.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		String[] trueIds = { "A", "B", "C", "D", };
		String[] falseIds = { "Z", "Y", "X", "W", };
		allTrue(space, trueIds);
		allFalse(space, falseIds);
	}

	/**
	 * This test demonstrates that the only literal that evaluates to
	 * {@code true} is "true" (ignoring case). Any other values evaluate
	 * to {@code false}.
	 * @see #boolLiteralsAreCaseInsensitive()
	 * @throws InPUTException never
	 */
	@Test
	public void anythingButTrueEvaluatesToFalse() throws InPUTException {
		final String designSpaceFile = "boolParamSpace02.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		String[] falseIds = { "A", "B", "C", "D", "E", "F", };
		allFalse(space, falseIds);
	}

	/**
	 * This test demonstrates that boolean parameters can be defined
	 * using a min and max range like any numeric type.
	 * @throws InPUTException never
	 */
	@Test
	public void minAndMaxAreLegalForBooleans() throws InPUTException {
		final String designSpaceFile = "boolParamSpace03.xml";
		DesignSpace space = new DesignSpace(designSpaceFile);
		space.next("A");
	}

	/**
	 * This test demonstrates that two DesignSpace objects created
	 * in exactly the same way are not considered equal.
	 * @see ExtendedDesignTest#getSpaceReturnsOriginalSpace()
	 * @throws InPUTException never
	 */
	@Test
	public void designSpaceEquality() throws InPUTException {
		final String designSpaceFile = "testSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesignSpace space2 = new DesignSpace(designSpaceFile);
		// While they are really the same, the two design spaces are not
		// identical (the same object), and they are not considered equal.
		assertEquals(space.getId(), space2.getId());
		assertEquals(space.getSupportedParamIds(),
				space2.getSupportedParamIds());
		assertEquals(space.toString(), space2.toString());
		assertFalse(space.equals(space2));
	}

	/**
	 * This test demonstrates that the design space of a design will be
	 * modified if the design space that was used to create it is modified.
	 * In other words, the two design spaces are kept in-sync.
	 * @see ExtendedDesignTest#getSpaceReturnsOriginalSpace()
	 * @throws InPUTException never
	 */
	@Test
	public void changingDesignSpaceAffectsDesignsDesignSpace()
			throws InPUTException {
		final String designSpaceFile = "testSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");
		// This is what we expect from the original design. (B > 3)
		assertTrue(3 < (int) space.next("B"));
		space.setFixed("B", "0");
		// With the fixed value, B should always be 0.
		assertEquals(0, space.next("B"));
		// The design space inside the design has also been changed.
		// This is true even though design doesn't return the same
		// DesignSpace object that was used to create it.
		space = design.getSpace();
		assertEquals(0, space.next("B"));
	}

	/**
	 * It is unclear what this test demonstrates.
	 * It seems to show a disagreement between Design and DesignSpace
	 * when it comes to interpreting the rules.
	 * <p>
	 * It is legal to fix a value to an out-of-range value.
	 * It is generally the case (whether due to a fixed parameter or not)
	 * that a design can be initialized with illegal values.
	 * Changing a DesignSpace <em>after</em> a Design has been created
	 * changes the Design as well. New values become legal or illegal.
	 * However, only the values are taken into account. As far as "rules"
	 * are concerned (in terms of the parameter definitions), these can
	 * never be changed, which means that the Design always respects them
	 * when trying to set the value of a parameter.
	 * <p>
	 * It seems like the best strategy would be to either not allow
	 * parameters to be fixed to arbitrary values, or make Design refuse
	 * to be initialized with illegal values, the same way it refuses to
	 * set illegal values once it has been initialized.
	 * <p>
	 * If it is indeed supposed to be possible to fix parameters to arbitrary
	 * values, then Design should recognize this and disregard the rules
	 * that would otherwise apply to that parameter (as long as it has a
	 * fixed value).
	 * @see #setFixedBypassesRanges()
	 * @throws InPUTException never
	 */
	@Test
	public void designDependsOnSpace() throws InPUTException {
		final String designSpaceFile = "testSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("pre-fix");
		IDesignSpace space2 = design.getSpace();

		// Need to fetch at least B here. Otherwise we get
		// "could not process the expression 'B'."
		// when doing setValue("A", 3) later.
		int a = design.getValue("A");
		int b = design.getValue("B");
		// A is defined to be larger than B, which is larger than 3
		// so it should be impossible to set it to 3. (3 is too small.)
		try {
			design.setValue("A", 3);
			fail("Setting A to 3 should be impossible.");
		} catch(IllegalArgumentException e) { }
		// B was larger than 3, so A must be larger than 3 as well.
		space.setFixed("B", "0");
		// But now B isn't larger than 3 anymore.
		// Setting A to 1 would conform to the rule that A > B.
		try {
			design.setValue("A", 1);
			fail("Setting A to 1 is expected to fail even though A > B.");
		} catch(IllegalArgumentException e) { }

		// Let's do the same thing but using the DesignSpace from design.
		space2.setFixed("B", "0");
		// It still doesn't work.
		try {
			design.setValue("A", 1);
			fail("Setting A to 1 is expected to fail even though A > B.");
		} catch(IllegalArgumentException e) { }

		// It looks like the design uses the parameter definitions it was
		// created with, so the new fixed value of B in the design space
		// is irrelevant. What matters is the initialized value. Then
		// it would be possible to set the actual value instead, and let
		// the original rules apply.
		// We know that A is larger than B. (let's just check...)
		assertTrue(a > b);
		// Yep. So setting B to A and A to whatever larger than B should
		// work just fine. However, B is now fixed, so that's a no go.
		try {
			design.setValue("B", a);
		} catch(InPUTException e) {
			String s = e.getMessage();
			assertTrue(s.contains("not allowed by this fixed parameter"));
		}
		// The operation is illegal because B is fixed. So the changes
		// to the design space are in effect after all.

		// Then updating the design manually to match the design space
		// should work. That would make B fixed to 0, and setting A to
		// 1 would now be legal both according to the design space and
		// according to the initialized values in the design.
		// However, setting B to 0 violates the minimum limit.
		try {
			design.setValue("B", 0);
		} catch(IllegalArgumentException e) { }
		// So the original design space rules are in effect after all.
		// In any case, the illegal value of 0 is exactly what the
		// parameter would be initialized to.
		design = space.nextDesign("post-fix");
		assertEquals(0, design.getValue("B"));
		// The general problem seems to be that design and design space
		// aren't always on the same page when figuring out what values
		// are legal. According to DesignSpace, 0 is legal. According to
		// Design, 0 is not legal. B can be initialized to 0 but not set.
		try {
			design.setValue("B", 0);
		} catch(IllegalArgumentException e) { }
	}

	/**
	 * This test demonstrates that it is illegal to define a parameter
	 * with multiple ranges if one or more min limits are missing.
	 * Creating such a DesignSpace will throw an
	 * ArrayIndexOutOfBoundsException.
	 * @see #rangesWithMissingMaxFailRandomly()
	 * @throws InPUTException never
	 */
	@Test
	public void rangesWithMissingMinIsIllegal() throws InPUTException {
		final String designSpaceFile = "multirangeSpace01.xml";
		try {
			new DesignSpace(designSpaceFile);
			fail("Expected DesignSpace creation with missing max to fail.");
		} catch(ArrayIndexOutOfBoundsException e) { }
	}

	/**
	 * This test demonstrates that a DesignSpace can be created if
	 * one or more max limits are missing when defining a parameter with
	 * multiple ranges.
	 * <p>
	 * Generating a value for such a parameter may either succeed or
	 * throw an ArrayIndexOutOfBoundsException depending on chance.
	 * @see #rangesWithMissingMinIsIllegal()
	 * @throws InPUTException never
	 */
	@Test
	public void rangesWithMissingMaxFailRandomly() throws InPUTException {
		final String designSpaceFile = "multirangeSpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);

		String msg = "Success rate out of range." +
				"Try increasing the number of generated values.";
		int values = 200;
		int successA = countSuccess(space, "A", values);
		int successB = countSuccess(space, "B", values);
		// Expect roughly 20%. Check 10% < A < 30%.
		int minA = 10 * values / 100;
		int maxA = 30 * values / 100;
		assertTrue(msg, successA > minA && successA < maxA);
		// Expect roughly 80%. Check 70% < B < 90%.
		int minB = 70 * values / 100;
		int maxB = 90 * values / 100;
		assertTrue(msg, successB > minB && successB < maxB);
	}

	/**
	 * This test demonstrates that it is not possible to define a parameter
	 * with multiple ranges ("1,5", "2,7") with references to other
	 * parameters. So defining A using inclMin="1,B" is illegal.
	 * @see #multiRangesCannotIncludeExpressions()
	 * @throws InPUTException never
	 */
	@Test
	public void multiRangesCannotIncludeDependencies() throws InPUTException {
		final String designSpaceFile = "multirangeSpace03.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		try {
			space.next("A");
			fail("Ranges cannot include references to other parameters.");
		} catch(InPUTException e) { }
	}

	/**
	 * This test demonstrates that it is not possible to define a parameter
	 * with multiple ranges using expressions, even if they do not
	 * reference any other parameters.
	 * @see #multiRangesCannotIncludeDependencies()
	 * @throws InPUTException
	 */
	@Test
	public void multiRangesCannotIncludeExpressions() throws InPUTException {
		final String designSpaceFile = "multirangeSpace04.xml";
		try {
			new DesignSpace(designSpaceFile);
		} catch(NumberFormatException e) { }
	}

	/**
	 * This test demonstrates that the array elements can have exactly
	 * the same restrictions as regular parameters.
	 * <p>
	 * Array elements can be defined using expressions or multiple ranges.
	 * @see #nextDoesNotProduceTheCorrectValueForExpressions()
	 * @throws InPUTException
	 */
	@Test
	public void integerArraysWithExpressionsAndMultipleRanges()
			throws InPUTException {
		final String designSpaceFile = "arraySpace01.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		int a[] = space.next("A"); // OK
		int b[] = space.next("B"); // OK
		int c[] = space.next("C"); // OK
		int d[] = space.next("D"); // Wrong!

		// All of these tests demonstrate expected behavior.
		assertEquals("Z has the wrong value.", 5, space.next("Z"));
		for(int n : a) { assertEquals(1, n); }
		for(int n : b) { assertEquals(2, n); }
		int oneCount = 0;
		int twoCount = 0;
		for(int n : c) {
			if(n == 1) {
				oneCount++;
			} else if(n == 2) {
				twoCount++;
			} else {
				fail(n + " is neither 1 nor 2!");
			}
		}
		assertTrue("Got no value from the first range",	oneCount > 0);
		assertTrue("Got no value from the second range", twoCount > 0);

		// These tests demonstrate unexpected behavior.
		// space.next() doesn't work for expressions.
		for(int n : d) {
			assertEquals("D is expected to be wrong.", -2, n);
		}
		// Need to create a design to get D properly initialized.
		IDesign design = space.nextDesign("D");
		d = design.getValue("D");

		// Once a design has been created, everything works.
		for(int n : d) {
			assertEquals("Elements of D have the wrong value.", 3, n);
		}
	}

	/**
	 * Felix: this is expected behavior.
	 * 
	 * This test demonstrates that the set of supported parameter IDs that
	 * a DesignSpace returns does not include the IDs of array elements.
	 * @see ExtendedDesignTest#supportedParamIdsIncludeArrayElements()
	 * @throws InPUTException never
	 */
	@Test(expected=IllegalArgumentException.class)
	public void supportedParamIdsDoNotIncludeArrayElements()
			throws InPUTException {
		final String designSpaceFile = "arraySpace03.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		Set<String> ids = space.getSupportedParamIds();

		assertTrue(ids.contains("A"));			// Array.
		assertFalse(ids.contains("A.1"));		// Array and element.
		assertFalse(ids.contains("A.1.1"));		// Element.
		assertTrue(ids.contains("A.1.1.1"));	// Regular parameter.
	}

	/**
	 * This test demonstrates that calling {@code next()} on a
	 * DesignSpace does not work for array elements. It does work for
	 * top-level arrays, however (but not for arrays that are themselves
	 * elements of another array).
	 * @throws InPUTException never
	 */
	@Test
	public void nextDoesNotGenerateArrayElements() throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		assertNotNull(space.next("A"));
		assertNull(space.next("A.1"));
		assertNull(space.next("A.1.1"));
	}

	/**
	 * This test demonstrates that the documentation for IDesignSpace
	 * is not entirely accurate regarding {@link IDesignSpace#isFile()}.
	 * @throws InPUTException never
	 * @throws IOException never
	 */
	@Test
	public void isFileWorksTooSensiblyColonP()
			throws InPUTException, IOException {
		final String designSpaceFile = "testSpace.xml";
		FileInputStream in = new FileInputStream(designSpaceFile);
		// Create design space from a file.
		IDesignSpace space = new DesignSpace(in);

		// This sort of makes sense, but it's not really what the
		// interface specification says.
		assertFalse(space.isFile());
		final String tmpFile = "tmp.xml";
		XMLFileExporter exporter = new XMLFileExporter(tmpFile);
		space.export(exporter);
		// Surely this design space is backed by a file now?
		assertFalse(space.isFile());
	}

	// Generate values for id and count the successes.
	// Calls space.next(id) values number of times. Returns the number
	// of calls that did not throw an exception.
	private int countSuccess(IDesignSpace space, String id, int values) {
		int count = 0;
		for(int i = 0; i < values; i++) {
			try {
				space.next(id);
				count++;
			} catch(Exception e) { }
		}
		return count;
	}

	private void allTrue(DesignSpace space, String[] ids)
			throws InPUTException {
		for(String id : ids) {
			assertTrue((boolean) space.next(id));
		}
	}
	private void allFalse(DesignSpace space, String[] ids)
			throws InPUTException {
		for(String id : ids) {
			assertFalse((boolean) space.next(id));
		}
	}

	// Fetch a parameter multiple times and assert that it will be
	// different at least once.
	private void checkRandomness(IDesignSpace space, String id)
			throws InPUTException {
		checkRandomness(space, id, 10);
	}
	private void checkRandomness(IDesignSpace space, String id, int tests)
			throws InPUTException {
		assertTrue("Run more than one test!", tests > 1);
		Object oldValue = space.next(id);
		for(int i = 1; i < tests; i++) {
			Object value = space.next(id);
			if(!value.equals(oldValue)) {
				return;
			}
			oldValue = value;
		}
		fail("Got the same value " + tests + " times.");
	}
}
