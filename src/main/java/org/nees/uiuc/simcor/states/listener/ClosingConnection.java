package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ClosingConnection extends TransactionState {


	public ClosingConnection(StateActionsProcessor sap) {
		super(Transaction.TransactionStateNames.CLOSING_CONNECTION, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.closingConnection(transaction, TransactionStateNames.STOP_LISTENING);
	}
}
