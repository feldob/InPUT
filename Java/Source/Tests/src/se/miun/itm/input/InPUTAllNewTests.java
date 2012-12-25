package se.miun.itm.input;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.miun.itm.input.model.design.DesignSpaceTest;
import se.miun.itm.input.model.design.DesignTest;

@RunWith(Suite.class)
@SuiteClasses({ ExperimentTest.class, InPUTConfigTest.class, InPUTTest.class, DesignSpaceTest.class, DesignTest.class })
public class InPUTAllNewTests {

}