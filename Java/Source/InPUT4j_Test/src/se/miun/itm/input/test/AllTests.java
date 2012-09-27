package se.miun.itm.input.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DesignSpaceTest.class, DesignTest.class, 
	LatexExportTest.class,
	InPUTTest.class, InPUTExperimentTest.class})
public class AllTests {

}