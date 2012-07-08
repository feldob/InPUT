package se.miun.itm.input.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.ga.BitPresentation;
import se.miun.itm.ga.LocalSearch;
import se.miun.itm.ga.Mutation;
import se.miun.itm.ga.Representation;
import se.miun.itm.ga.Selection;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class DesignSpaceTest {

	public static final String METAINPUT_XML_TESTFILE = "geneticAlgorithm.xml";
	private IDesignSpace designSpace;

	@Before
	public void setUp() throws Exception {
		designSpace = new DesignSpace(METAINPUT_XML_TESTFILE);
	}

	@Test
	public void testDesignSpace() {
		boolean flag = true;
		try {
			designSpace = new DesignSpace(METAINPUT_XML_TESTFILE);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		assertTrue(flag);
	}

	@Test
	public void testRandom() throws Exception {
//		 test decimal (is not really popsize but that does not matter here)
		BigDecimal pop;
		BigDecimal inclMin = new BigDecimal(-12.23434);
				BigDecimal inclMax = new BigDecimal(-12.23433);
		for (int i = 0; i < 10; i++) {
			pop = (BigDecimal) designSpace.next("popSize");
			if (pop.compareTo(inclMin) >= 0 && pop.compareTo(inclMax) <= 0) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		}
		
		Object[] objects = {null, 3, null};
		LocalSearch value = designSpace.next("localSearch", objects);
		assertEquals(3, value.getValue());
		// test integer
		int size, winner;
		Map<String, Object> vars = new HashMap<String, Object>();
		for (int i = 0; i < 10; i++) {
			size = (Integer) designSpace.next("selection.tournament.size");
			vars.put("selection.tournament.size", size);
			winner = (Integer) designSpace.next("selection.tournament.winner", vars);
			if (winner > 0 && winner < size)
				assertTrue(true);
			else
				assertTrue(false);
		}

		// test double
		Object[] mr, mr2 = null;
		for (int i = 0; i < 10; i++) {
			mr = (Object[]) designSpace.next("mutationRate");
			for (int j = 0; j < mr.length; j++) {
				mr2 = (Object[]) mr[j];
				for (int j2 = 0; j2 < mr2.length; j2++){
					if (((Double) mr2[j2] > 0.0001) && ((Double) mr2[j2] <= 0.4)) {
						assertTrue(true);
					} else {
						assertTrue(false);
					}
				}
			}
		}
		
		Representation bitRep = new BitPresentation(true);
		// testWrapper
		vars.put("popSize", 10);
		Mutation mutation = (Mutation)((Object[])designSpace.next("mutation", vars, new Object[]{null,bitRep}))[0];
		mutation.getWrapper().getTheValue();
		
		// test complex
		Selection sel;
		for (int i = 0; i < 10; i++) {
			sel = (Selection) designSpace.next("selection");
			if (sel == null) {
				assertTrue(false);
			} else {
				assertTrue(true);
			}
		}

		Integer[] sizeArray = { 3, 2, 4 };
		objects = (Object[]) designSpace.next("selection", sizeArray);
		assertTrue(objects.length == 3);
		for (int i = 0; i < objects.length; i++) {
			Object[] objectsSecond = (Object[]) objects[i];
			assertTrue(objectsSecond.length == 2);
			for (int j = 0; j < objectsSecond.length; j++) {
				Object[] objectsThird = (Object[]) objectsSecond[j];
				assertTrue(objectsThird.length == 4);
				if (objectsThird[j] instanceof Selection) {
					assertTrue(true);
				} else {
					assertTrue(false);
				}
			}
		}
	}
}
