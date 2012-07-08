package se.miun.itm.input.test;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.ga.BitPresentation;
import se.miun.itm.ga.DecisionValue1;
import se.miun.itm.ga.DecisionValue2;
import se.miun.itm.ga.FlipMutation;
import se.miun.itm.ga.Mutation;
import se.miun.itm.ga.OPT2;
import se.miun.itm.ga.RealRepresentation;
import se.miun.itm.ga.SlideMutation;
import se.miun.itm.ga.TheChoice;
import se.miun.itm.ga.Tournament;
import se.miun.itm.ga.Wrapper;
import se.miun.itm.input.export.ByteArrayExporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class DesignTest {

	private IDesign design;

	private IDesignSpace designSpace;

	private XMLFileExporter fileExp;

	private XMLFileImporter fileImp;

	@Before
	public void setUp() throws Exception {
		designSpace = new DesignSpace(DesignSpaceTest.METAINPUT_XML_TESTFILE);
		design = designSpace.nextEmptyDesign("instanceTest");
		fileExp = new XMLFileExporter("test.xml");
		fileImp = new XMLFileImporter("test2.xml");
	}

	@Test
	public void testExperiment() {
		boolean flag = true;
		try {
			design = designSpace.nextEmptyDesign("test");
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		assertTrue(flag);
	}

	@Test
	public void testGetSet() throws Exception {
		
		Set<String> paramIds = new HashSet<String>();
		paramIds.add("popSize");
		paramIds.add("featureX");
		paramIds.add("mutationRate");
		paramIds.add("representation");
		paramIds.add("selection");
		paramIds.add("mutation");
		paramIds.add("localSearch");
		paramIds.add("mutation.length");
		paramIds.add("mutation.positions");
		paramIds.add("selection.tournament.someDecision");
		paramIds.add("selection.tournament.size");
		paramIds.add("selection.tournament.winner");

		fileImp.resetFileName("design.xml");
		IDesign inst = designSpace.impOrt(fileImp);

		TheChoice choice = TheChoice.second;
		assertEquals(choice, inst.getValue("theChoice"));

		Object value;
		
		value = inst.getValue("representation");
		if (!(value instanceof BitPresentation))
			fail();
		value = inst.getValue("network");
		
		value = inst.getValue("representation.color");
		assertEquals(1, (Double)value, 0.0001);
		inst.setValue("representation", new RealRepresentation());
		inst.setValue("representation.color", 2d);
		
		value = inst.getValue("representation");
		if (!(value instanceof RealRepresentation))
			fail();
		
		value = inst.getValue("representation.color");
		assertEquals(2, (Double)value, 0.0001);

		value = inst.getValue("mutation");
		if (value != null) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}

		Object[] params = { null, 3, null };
		value = inst.getValue("localSearch", params);
		inst.setValue("localSearch", new OPT2(null,1,null));

		value = inst.getValue("featureX");
		if (value instanceof Boolean) {
			assertTrue(true);
			if (((Boolean) value) == true) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} else {
			assertTrue(false);
		}

		value = inst.getValue("popSize");
		BigDecimal comp = new BigDecimal(10);
		if (value instanceof BigDecimal) {
			assertTrue(true);
			if (((BigDecimal) value).compareTo(comp) == 0) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} else {
			assertTrue(false);
		}
		Tournament t;
		value = inst.getValue("selection");
		if (value instanceof Tournament) {
			assertTrue(true);
			t = (Tournament) value;
			if (t.getsize() == 4) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}

			if (t.getwinner() == 2) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} else {
			assertTrue(false);
		}

		inst.setValue("selection.tournament.size", 13);
				try {
			
			inst.setValue("selection.tournament.someDecision", new DecisionValue2());
		} catch (InPUTException e) {
			assertTrue(true);
		}

		value = inst.getValue("selection.tournament.someDecision");
		if (value instanceof DecisionValue1)
			assertTrue(true);
		else
			assertTrue(false);

		value = inst.getValue("selection.tournament.size");
		if (value instanceof Integer) {
			assertTrue(true);
			if ((Integer) value == 13) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} else {
			assertTrue(false);
		}

		t = (Tournament) inst.getValue("selection");
		value = t.getsize();
		if (value instanceof Integer) {
			assertTrue(true);
			if ((Integer) value == 13)
				assertTrue(true);
			else
				assertTrue(false);

		} else
			assertTrue(false);

		value = t.getsomeDecision();
		if (value instanceof DecisionValue1)
			assertTrue(true);
		else
			assertTrue(false);
		
		value = inst.getValue("mutationRate");
		if (value instanceof Object[]) {
			assertTrue(true);

			Object value2;
			Double value11, value12, value21, value22;
			value2 = ((Object[]) value)[0];
			value11 = (Double) ((Object[]) value2)[0];
			value12 = (Double) ((Object[]) value2)[1];

			value2 = ((Object[]) value)[1];
			value21 = (Double) ((Object[]) value2)[0];
			value22 = (Double) ((Object[]) value2)[1];

			if (value11 == 0.09)
				assertTrue(true);
			else
				assertTrue(false);

			if (value12 == 0.11)
				assertTrue(true);
			else
				assertTrue(false);

			if (value21 == 0.08)
				assertTrue(true);
			else
				assertTrue(false);

			if (value22 == 0.12)
				assertTrue(true);
			else
				assertTrue(false);
		} else {
			assertTrue(false);
		}

		Tournament t2 = new Tournament(new DecisionValue2());
		t2.setsize(12);
		t2.setwinner(3);
		inst.setValue("selection", t2);

		value = inst.getValue("selection.tournament.someDecision");
		if (value instanceof DecisionValue2)
			assertTrue(true);
		else
			assertTrue(false);

		value = inst.getValue("selection.tournament.size");
		if (value instanceof Integer) {
			assertTrue(true);
			if ((Integer) value == 12) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} else {
			assertTrue(false);
		}

		t = (Tournament) inst.getValue("selection");
		value = t.getsize();
		if (value instanceof Integer) {
			assertTrue(true);
			if ((Integer) value == 12)
				assertTrue(true);
			else
				assertTrue(false);

		} else
			assertTrue(false);

		value = t.getsomeDecision();
		if (value instanceof DecisionValue2)
			assertTrue(true);
		else
			assertTrue(false);

		{
			Object[] m = inst.getValue("mutation");
			if (m instanceof Object[]) {
				assertTrue(true);
				Object[] ms = (Object[]) m;

				Mutation first, second;
				first = (Mutation) ms[0];
				second = (Mutation) ms[1];

				assertEquals(first.getWrapper().getTheValue(), 10d, 0.001);

				if (first instanceof SlideMutation
						&& first.getLength().equals(new BigDecimal(4)))
					assertTrue(true);
				else
					assertTrue(false);

				if (second instanceof FlipMutation
						&& second.getLength().equals(new BigDecimal(6)))
					assertTrue(true);
				else
					assertTrue(false);
			} else {
				assertTrue(false);
			}

			// wrapper test case
			try {
				Wrapper wr = new Wrapper(11d);
				Wrapper wr2;
				inst.setValue("mutation.1.wrapped", wr);
				wr2 = inst.getValue("mutation.1.wrapped");
				assertEquals(wr.getTheValue(), wr2.getTheValue(), 0.0001);
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
			assertTrue(true);
		}
	}

	@Test
	public void testWrite() {
		try {
			design.export(fileExp);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		File testFile = new File("test.xml");
		assertTrue(testFile.exists());

		testFile.delete();
	}

	@Test
	public void testRandomInit() {
		IDesign inst2;
		boolean flag = true;
		int size, winner;
		BigDecimal popSize, length, var;
		var = new BigDecimal(20);
		for (int i = 0; i < 10; i++) {
			try {
				design.export(fileExp);
				design = designSpace.nextDesign("test");
				design.export(fileExp);
				if (design.getValue("selection.tournament.size") != null) {

					size = design.getValue("selection.tournament.size");
					winner = design.getValue("selection.tournament.winner");

					if (winner >= size)
						assertTrue(false);
					else
						assertTrue(true);
				}
				if ((Boolean)design.getValue("featureX"))
					fail();

				//TODO somehow this calculation is wrong now!! what can that be related to!???
				length = design.getValue("mutation.1.length");
				popSize = design.getValue("popSize");

				if (length.compareTo(popSize.subtract(var)) >= 0
						&& length.compareTo(popSize.add(var)) <= 0)
					assertTrue(true);
				else
				{
					System.out.println(length);
					System.out.println(popSize);
					assertTrue(false);
				}

				design.export(new ByteArrayExporter());
				fileExp.resetExportFileName("test2.xml");
				design.export(fileExp);
				fileImp.resetFileName("test2.xml");
				inst2 = designSpace.impOrt(fileImp);
				inst2.attachEnvironmentInfo();
				inst2.export(fileExp);
			} catch (Exception e) {
				e.printStackTrace();
				flag = false;
			} finally {
				assertTrue(flag);
			}

			flag = true;
			new File("test2.xml").delete();
			new File("test.xml").delete();
		}
	}
}
