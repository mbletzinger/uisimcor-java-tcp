package org.nees.uiuc.simcor.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TransactionTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(TransactionTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(T03_StateActionsTest.class);
		suite.addTestSuite(T04_ListenerStateMachineTest.class);
		suite.addTestSuite(T05_TransactionTest.class);
		suite.addTestSuite(T06_TriggerTest.class);
		suite.addTestSuite(T07_BroadcastTest.class);
		//$JUnit-END$
		return suite;
	}

}
