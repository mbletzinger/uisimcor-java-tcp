package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReadResponse extends TransactionState {

	public ReadResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.READ_RESPONSE, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.setUpRead(transaction, false, TransactionStateNames.WAIT_FOR_RESPONSE);
	}

}
