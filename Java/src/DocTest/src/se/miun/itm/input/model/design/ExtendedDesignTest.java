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

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.ParamStore;

/**
 * The following tests are explicitly meant as documentation tests.
 * AKA executable documentation. It is not necessarily the case that
 * the tested behavior is the right behavior, in the sense that this
 * is what the program is supposed to do. These tests are intended to
 * document what the program in fact does, given some corner cases.
 * 
 * See the configuration files for more detailed comments.
 * 
 * @author Christoffer Fink
 */
public class ExtendedDesignTest {
	@After
	public void cleanup() {
		ParamStore.releaseAllParamStores();
	}

	/**
	 * Felix : resolved
	 * 
	 * This test arguably demonstrates a bug.
	 * A is defined as being strictly bigger than B, yet A and B have the
	 * same value in the Design that is being imported.
	 * @throws InPUTException never
	 */
	@Test(expected=IllegalArgumentException.class)
	public void importingDesignWithOutOfRangeValueSucceeds()
			throws InPUTException {
		new Design("outOfRangeDesign01.xml");
	}

	/**
	 * Felix : resolved
	 * 
	 * This test arguably demonstrates a bug.
	 * Setting A without first touching B results in an InPUTException
	 * because the expression 'B' cannot be evaluated.
	 * Since all values are within their defined ranges, it seems that
	 * setting A should succeed.
	 * 
	 * @see #settingDependentValueSucceedsOnlyIfFirstTouchingDependee
	 * @throws InPUTException never
	 */
	@Test
	public void settingDependentValueFailsUnlessFirstTouchingDependee()
			throws InPUTException {
		final String designFile = "outOfRangeDesign02.xml";
		Design d = new Design(designFile);
//		try {
			d.setValue("A", 6);
			int value = d.getValue("A");
			assertEquals(6, value);
//			fail("Setting A without first touching B is expected to fail.");
//		} catch(InPUTException e) { }
	}
	
	/**
	 * Felix: resolved
	 * 
	 * This test is almost exactly the same as the fails
	 * test above. The only difference is that the B parameter is first
	 * accessed. (It does not matter whether it is set or only retrieved,
	 * touching it in any way solves the problem.)
	 * 
	 * @see #settingDependentValueFailsUnlessFirstTouchingDependee
	 * @throws InPUTException never
	 */
	@Test
	public void settingDependentValueSucceedsOnlyIfFirstTouchingDependee()
			throws InPUTException {
		final String designFile = "outOfRangeDesign02.xml";
		Design d = new Design(designFile);
		// Get the imported value and discard it.
		d.getValue("B");
		// Now A can be set without problems.
		d.setValue("A", 6);
	}

	/**
	 * Felix: that is true and undesired. This has to be fixed some time, it is low priority though because of the unlikely event that someone will use it that way.
	 * Especially when the dependent value is reset, the change of the dependee is a weird, though correct, move.
	 * 
	 * This test shows that setting the value of a parameter that some other
	 * parameter depends on (dependee) does not affect the dependent parameter.
	 * Technically, setting the dependee to an inappropriate value (as is done
	 * in this test) places the dependent parameter out of range, thereby
	 * invalidating the Design.
	 * @throws InPUTException never
	 */
	@Test
	public void settingDependeeValueDoesNotAffectDependent()
			throws InPUTException {
		final String designFile = "outOfRangeDesign02.xml";
		Design d = new Design(designFile);
		d.setValue("B", 5);
		final int a = d.getValue("A");
		assertEquals("A should be 5 in the imported Design.", 5, a);
		// This is exactly what we would expect.
		// However, A is already out of range before trying to set it.
		try {
			d.setValue("A", a);
			fail("A is now out of range. Cannot re-set the original value.");
		} catch(IllegalArgumentException e) { }
	}

	/**
	 * Felix: resolved
	 * 
	 * This test arguably demonstrates a bug.
	 * By extending the scope of a design, the extending design can have its
	 * parameters set even if the design has been set to read-only.
	 * While the local parameters take precedent, which means that only
	 * parameters that only exist in the extending scope can be set, any
	 * parameter can still be set, simply by extending an empty Design so
	 * that all of the parameters are unique.
	 *
	 * Also note that parameters with the same IDs do not seem to cause
	 * problems, which would contradict the documentation for IDesign.
	 * @throws InPUTException never
	 */
	@Test
	public void extendingScopeCanCircumventReadOnly() throws InPUTException {
		final String designFile = "outOfRangeDesign02.xml";
		final String extendingDesignFile = "extendingDesign.xml";
		Design design = new Design(designFile);
		Design extendingDesign = new Design(extendingDesignFile);
		// Setting both Designs to read-only.
		design.setReadOnly();			// This isn't really relevant.
		extendingDesign.setReadOnly();
		
		final int origZ = extendingDesign.getValue("Z");
		
		// Trying to set a value should now fail.
		try {
			extendingDesign.setValue("Z", 1);
			fail("Cannot set any parameters on a read-only Design.");
		} catch(InPUTException e) { }
		
		// However, now the extendingDesign is used to extend design.
		design.extendScope(extendingDesign);
		// Not only can setValue be called on design (which is read-only),
		// but the extendingDesign (which is also read-only) will have its
		// parameter updated.
		
		try {
			design.setValue("Z", 1);
			fail("design is read only");
		} catch (Exception e) {
		}
		
		final int z = extendingDesign.getValue("Z");
		assertTrue("The read-only design should be changed.", origZ == z);
	}

	/**
	 * Felix: resolved
	 * 
	 * This test demonstrates that the {@link Design.same(Object)} method
	 * returns true if the argument is a superset of {@code this}
	 * and all parameters in their intersection have the same value in
	 * both designs.
	 * @throws InPUTException never
	 */
	@Test
	public void subsetIsSameButSupersetIsNotSame() throws InPUTException {
		final String subsetFile = "subsetDesign.xml";
		final String supersetFile = "supersetDesign01.xml";
		final String supersetFile2 = "supersetDesign02.xml";
		final Design subset = new Design(subsetFile);
		final Design superset = new Design(supersetFile);
		final Design superset2 = new Design(supersetFile2);
		assertFalse(subset.same(superset));
		assertFalse(superset.same(subset));
		assertFalse(subset.same(superset2));
	}

	/**
	 * Felix: this is correct with respect to the semantics. They should be the same, but not equal.
	 * 
	 * This test demonstrates that two designs with the same id and
	 * an identical set of parameters are still not considered equal.
	 * @throws InPUTException never
	 */
	@Test
	public void designEqualityIsDeterminedByDesignSpace()
			throws InPUTException {
		final String subsetFile = "duplicateIdSpace01.xml";
		final String supersetFile = "duplicateIdSpace02.xml";
		DesignSpace subsetSpace = new DesignSpace(subsetFile);
		DesignSpace supersetSpace = new DesignSpace(supersetFile);
		IDesign subsetDesign = subsetSpace.nextDesign("design");
		IDesign supersetDesign = supersetSpace.nextDesign("design");
		final String id1 = subsetDesign.getId();
		final String id2 = supersetDesign.getId();

		// The two designs are subsets of each other.
		// They define exactly the same parameter.
		// They also have the same id.
		assertTrue(subsetDesign.same(supersetDesign));
		assertTrue(supersetDesign.same(subsetDesign));
		assertEquals(id1, id2);
		// Yet, the two designs are not considered equal.
		assertFalse(subsetDesign.equals(supersetDesign));
	}

	/**
	 * Felix: This is according to the desired semantics.
	 * 
	 * This test demonstrates that {@link Design.getSpace()) returns
	 * the same DesignSpace object that was used to create it.
	 * However, this test is subject to some global caching.
	 * Skipping cleanup (releasing ParamStores) makes this test fail.
	 * @throws InPUTException never
	 */
	@Test
	public void getSpaceReturnsOriginalSpace() throws InPUTException {
		final String designSpaceFile = "testSpace.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");
		assertSame(space, design.getSpace());
	}

	/**
	 * Felix: resolved to a large extend. The referential integrity between retrieved params, arrays that get changed by the interface is not guaranteed. Therefore, the parents would have to be retrieved again for use afterwards, as it stands.
	 * 
	 * This test demonstrates that, when an array element is set ("A.1.1"),
	 * this does not update the array ("A.1").
	 * @see #settingAnArrayDoesNotUpdateElements()
	 * @throws InPUTException never
	 */
	@Test
	public void settingAnArrayElementDoesNotUpdateTheArray()
			throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");

		int[][] a = design.getValue("A");

		// Set to a different value.
		int element = a[0][0] == 0 ? 1 : 0;
		design.setValue("A.1.1", element);
		// The array the was returned for "A" has not been updated.
		assertNotSame(element, a[0][0]);
		// Fetch the array again. The new value isn't in this array either.
		a = design.getValue("A");
		assertEquals(element, a[0][0]);
		int[] aPart = design.getValue("A.1");
		assertEquals(element, aPart[0]);
		// Confirm that the "A.1.1" parameter was set.
		assertEquals(element, design.getValue("A.1.1"));
	}

	/**
	 * Felix: valid behavior. Changes in the retrieved array should not be reflected, since they might not be in line with the internal description of the design. This is a protection from external changes, and results in the phenomena in the former test case. 
	 * 
	 * This test demonstrates that, when an array element is assigned
	 * (a[0][0] = 0), this does not update the parameter
	 * ("A" or "A.1.1" are unaffected).
	 * @throws InPUTException never
	 */
	@Test
	public void assigningAnArrayElementDoesNotUpdateTheParameter()
			throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");

		int[][] a = design.getValue("A");

		// Set to a different value.
		int element = a[0][0] == 0 ? 1 : 0;
		a[0][0] = element;
		// Fetch the array again. The new value isn't in this array.
		a = design.getValue("A");
		assertFalse(element == a[0][0]);

		// The "A.1.1" parameter was not set.
		assertFalse(element == (int) design.getValue("A.1.1"));
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test demonstrates that setting an array ("A.1") does not
	 * update the elements of the array ("A.1.1").
	 * @see #settingAnArrayElementDoesNotUpdateTheArray()
	 * @throws InPUTException never
	 */
	@Test
	public void settingAnArrayDoesNotUpdateElements() throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");

		int[] data = { 0, 0, 0, };
		design.setValue("A.1", data);
		// The A.1 array, which is an element of A, has been updated. 
		int[][] a = design.getValue("A");
		for(int n : a[0]) {
			assertEquals("Expected the array to match.", 0, n);
		}
		// That one should happen to be 0 by chance is highly unlikely.
		// That two are 0 is, to a first approximation, impossible.
		// Unless, of course, the A.1.x ID doesn't actually access the
		// A.1 array.
		assertTrue(0 == (int) design.getValue("A.1.1"));
		assertTrue(0 == (int) design.getValue("A.1.2"));
	}

	/**
	 * This test confirms that it is illegal to set an array element to
	 * an out-of-range value.
	 * @see #settingArraysBypassesRanges()
	 * @throws InPUTException never
	 */
	@Test
	public void settingArrayElementsToIllegalValuesIsIllegal()
			throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");
		try {
			design.setValue("A.1.1", -1);
			fail("Should not be able to set an element to an illegal value.");
		} catch(IllegalArgumentException e) { }
	}

	/**
	 * Felix : resolved.
	 * 
	 * This test demonstrates that array elements can be set to
	 * out-of-range values by setting the whole array.
	 * @see #settingArrayElementsToIllegalValuesIsIllegal()
	 * @throws InPUTException never
	 */
	@Test
	public void settingArraysBypassesRanges() throws InPUTException {
		final String designSpaceFile = "arraySpace02.xml";
		IDesignSpace space = new DesignSpace(designSpaceFile);
		IDesign design = space.nextDesign("design");

		// Setting all values in A.1 to illegal values.
		int[] data = { -1, -1, -1, };
		try {
			design.setValue("A.1", data);
			fail("The set of values out of range should not work.");
		} catch (IllegalArgumentException e) {
			
		}
		int[][] a = design.getValue("A");
		// Confirm that the illegal values were set.
		assertNotSame(-1, a[0][0]);
	}

	/**
	 * Felix: resolved.
	 * 
	 * This test demonstrates that parameter access works fine when a
	 * parameter ID starts the same way as the ID of an array element.
	 * @throws InPUTException never
	 */
	@Test
	public void parameterNameWithDotsAndArrayWorksAsExpected()
			throws InPUTException {
		final String designSpaceFile = "arraySpace03.xml";
		try {
			IDesignSpace space = new DesignSpace(designSpaceFile);
			fail("no dots in param ids, and it wasnt validated and found!");
//			IDesign design = space.nextDesign("design");
//			assertNotNull(design.getValue("A.1.1"));
//			assertNotNull(design.getValue("A.1.1.1"));
		} catch (Exception e) {
			
		}
	}

	/**
	 * Felix: expected behavior.
	 * 
	 * This test demonstrates that the set of supported parameter IDs
	 * that a Design returns includes the IDs of array elements.
	 * @see ExtendedDesignSpaceTest#supportedParamIdsDoNotIncludeArrayElements()
	 * @throws InPUTException never
	 */
	@Test
	public void supportedParamIdsIncludeArrayElements() throws InPUTException {
		final String designSpaceFile = "arraySpace03.xml";
		try {
			IDesignSpace space = new DesignSpace(designSpaceFile);
			fail("parameters contain dots and that is not validated!");
//			IDesign design = space.nextDesign("design");
//			Set<String> ids = design.getSupportedParamIds();
//			
//			assertTrue(ids.contains("A"));			// Array.
//			assertTrue(ids.contains("A.1"));		// Array and element.
//			assertTrue(ids.contains("A.1.1"));		// Element.
//			assertTrue(ids.contains("A.1.1.1"));	// Regular parameter.
			
		} catch (Exception e) {
			
		}
		
	}
	
	public static void main(String[] args) {
		Map<String, String> a = new HashMap<>();
		a.put("a", "b");
		a = Collections.unmodifiableMap(a);
		a.put("a", "c");
	}
}
