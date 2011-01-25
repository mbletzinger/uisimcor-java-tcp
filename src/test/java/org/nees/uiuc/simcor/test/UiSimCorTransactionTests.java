package org.nees.uiuc.simcor.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class UiSimCorTransactionTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(UiSimCorTransactionTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(StateActionsTest.class);
		suite.addTestSuite(ListenerStateMachineTest.class);
		suite.addTestSuite(TransactionTest.class);
		suite.addTestSuite(TriggerTest.class);
		suite.addTestSuite(BroadcastTest.class);
		//$JUnit-END$
		return suite;
	}

}
