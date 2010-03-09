package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ClosingConnection extends TransactionState {


	public ClosingConnection(StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.CLOSING_CONNECTION, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.closingConnection(transaction, TransactionStateNames.STOP_LISTENING);
	}
}
