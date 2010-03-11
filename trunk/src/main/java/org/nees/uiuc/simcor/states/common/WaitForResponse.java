package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForResponse extends TransactionState {

	public WaitForResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE, sap,
				TransactionStateNames.READ_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, false, next);
	}

}
