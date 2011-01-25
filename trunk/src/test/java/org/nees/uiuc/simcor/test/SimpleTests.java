package org.nees.uiuc.simcor.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SimpleTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(SimpleTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(HexTest.class);
		suite.addTestSuite(SimCorMsgTest.class);
		//$JUnit-END$
		return suite;
	}

}
