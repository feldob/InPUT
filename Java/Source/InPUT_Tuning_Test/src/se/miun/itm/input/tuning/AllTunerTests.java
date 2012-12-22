package se.miun.itm.input.tuning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.miun.itm.input.tuning.sequential.ISequentialTunerTest;

@RunWith(Suite.class)
@SuiteClasses({ ISequentialTunerTest.class, ITunerTest.class, RandomTunerTest.class })
public class AllTunerTests {

}
