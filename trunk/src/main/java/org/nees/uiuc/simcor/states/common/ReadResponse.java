package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ReadResponse extends TransactionState {

	public ReadResponse( StateActionsProcessor sap,
			TransactionStateNames next) {
		super(TransactionStateNames.READ_RESPONSE, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, next);
	}

}
