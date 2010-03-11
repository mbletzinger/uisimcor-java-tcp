package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class TransactionDone extends TransactionState {

	public TransactionDone(
			StateActionsProcessor sap, TransactionStateNames next) {
		super(TransactionStateNames.TRANSACTION_DONE, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.recordTransaction(transaction);
	}

}
