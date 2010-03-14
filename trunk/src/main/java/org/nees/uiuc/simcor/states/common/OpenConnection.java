package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class OpenConnection extends TransactionState {

	public OpenConnection(
			StateActionsProcessor sap) {
		super(TransactionStateNames.OPENING_CONNECTION, sap, TransactionStateNames.CHECK_OPEN_CONNECTION);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.checkOpenConnection(transaction, next);
	}

}
