package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForOpenResponse extends TransactionState {

	public WaitForOpenResponse( StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_RESPONSE, sap, TransactionStateNames.TRANSACTION_DONE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSessionMsgRead(transaction, false, next);
	}

}
