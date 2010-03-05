package org.nees.uiuc.simcor.states.old;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class CheckOpenConnection extends TransactionState {


	public CheckOpenConnection(StateActionsProcessor sap) {
		super(Transaction.TransactionStateNames.CHECK_OPEN_CONNECTION, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.checkOpenConnection(transaction,TransactionStateNames.SEND_OPEN_SESSION);
	}

}