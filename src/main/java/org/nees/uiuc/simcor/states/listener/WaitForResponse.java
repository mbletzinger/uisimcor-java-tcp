package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class WaitForResponse extends TransactionState {
	public WaitForResponse(StateActionsProcessor sap	) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, false, TransactionStateNames.TRANSACTION_DONE);
	}

}
