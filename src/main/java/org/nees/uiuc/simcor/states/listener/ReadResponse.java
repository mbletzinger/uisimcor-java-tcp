package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ReadResponse extends TransactionState {

	public ReadResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.READ_RESPONSE, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, false, TransactionStateNames.WAIT_FOR_RESPONSE);
	}

}
