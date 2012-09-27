package se.miun.itm.input.test;


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.export.LaTeXFileExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class LatexExportTest {

	private static final String TABLE_EXAMPLE_META = "tableExample_meta.tex";
	private static final String TABLE_EXAMPLE = "tableExample.tex";
	private static final String DESIGN_SPACE = "realPSO.xml";
	private static final String DESIGN = "realPSOinstance.xml";
	private static final String DESIGN2 = "design.xml";
	private static final String OUTPUT_SPACE = "outputSpace.xml";

	private IDesignSpace space;
	private IDesignSpace space2;
	private LaTeXFileExporter exporter;

	@Before
	public void setUp() throws Exception {
		space = new DesignSpace(DesignSpaceTest.METAINPUT_XML_TESTFILE);
		space2 = new DesignSpace(DESIGN_SPACE);
		exporter = new LaTeXFileExporter();
	}

	@Test
	public void testToTableIInput() {
		try {
			exporter.resetFileName(TABLE_EXAMPLE_META);
			space.export(exporter);
			space2.export(exporter);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		} finally {
			File file = new File(TABLE_EXAMPLE_META);
			if (file.exists())
				assertTrue(true);
			else
				assertTrue(false);

			file.delete();
		}
	}

	@Test
	public void testToTableDesign() {
		try {
			space = new DesignSpace(DesignSpaceTest.METAINPUT_XML_TESTFILE);
			IDesign instance = space.nextDesign("whatever");
			exporter.resetFileName(TABLE_EXAMPLE);
			instance.export(exporter);
			IDesign instance2 = space.impOrt(new XMLFileImporter(DESIGN2));
			instance2.export(exporter);
			instance2 = space2.impOrt(new XMLFileImporter(DESIGN));
			instance2.export(exporter);
			space2 = new DesignSpace(OUTPUT_SPACE);
			instance = space2.nextDesign("some", true);
			exporter.resetFileName(TABLE_EXAMPLE);
			instance.export(exporter);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			File file = new File(TABLE_EXAMPLE);
			if (file.exists())
				assertTrue(true);
			else
				assertTrue(false);

			file.delete();
		}
	}
}