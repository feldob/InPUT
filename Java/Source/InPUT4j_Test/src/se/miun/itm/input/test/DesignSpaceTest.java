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
import se.miun.itm.input.model.design.IDesign;
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
		// test decimal (is not really popsize but that does not matter here)
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

		Object[] objects = { null, 3, null };
		LocalSearch value = designSpace.next("localSearch", objects);
		assertEquals(3, value.getValue());
		// test integer
		int size, winner;
		Map<String, Object> vars = new HashMap<String, Object>();
		for (int i = 0; i < 10; i++) {
			size = designSpace.next("selection.tournament.size");
			vars.put("selection.tournament.size", size);
			winner = designSpace.next("selection.tournament.winner", vars);
			if (winner > 0 && winner < size)
				assertTrue(true);
			else
				assertTrue(false);
		}

		// test double
		double[][] mr;
		double[] mr2;
		double mr3;
		IDesign design = designSpace.nextDesign("test");
		for (int i = 0; i < 10; i++) {
			mr = design.getValue("mutationRate");
			for (int j = 0; j < mr.length; j++) {

				for (int k = 0; k < mr[j].length; k++) {
					mr3 = design.getValue("mutationRate." + (j+1) + "." + (k+1));
					if ((mr[j][k] > 0.0001) && mr[j][k] <= 0.4
							&& mr3 == mr[j][k]) {
						assertTrue(true);
					} else {
						assertTrue(false);
					}
				}
				mr2 = design.getValue("mutationRate." + (j+1));
				if (mr[j][0] == mr2[0]) {
					assertTrue(true);
				} else {
					assertTrue(false);
				}
			}
		}

		Representation bitRep = new BitPresentation(true);
		// testWrapper
		vars.put("popSize", 10);
		Mutation[] mutations = designSpace.next("mutation", vars, new Object[] {
				null, bitRep });
		Mutation mutation = mutations[0];
		mutation.getWrapper().getTheValue();

		// test complex
		Selection sel;
		for (int i = 0; i < 10; i++) {
			sel = designSpace.next("selection");
			if (sel == null) {
				assertTrue(false);
			} else {
				assertTrue(true);
			}
		}

		Integer[] sizeArray = { 3, 2, 4 };
		Selection[][][] selections = designSpace.next("selection", sizeArray);

		boolean flag = false;
		if (selections.length == sizeArray[0]) {
			if (selections[0].length == sizeArray[1]) {
				if (selections[0][0].length == sizeArray[2]) {
					flag = true;
				}
			}
		}
		if (!flag) {
			assertTrue(false);
		}
	}
}
