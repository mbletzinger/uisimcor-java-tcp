package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;

public class OpeningConnection extends TransactionState {

	public OpeningConnection(StateActionsProcessor sap) {
		super(Transaction.TransactionStateNames.OPENING_CONNECTION, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.openConnection(transaction);
	}

}
