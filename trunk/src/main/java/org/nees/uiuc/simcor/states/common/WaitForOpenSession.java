package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class WaitForOpenSession extends TransactionState {

	public WaitForOpenSession(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_SESSION,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, TransactionStateNames.SEND_OPEN_SESSION_RESPONSE);
	}

}
