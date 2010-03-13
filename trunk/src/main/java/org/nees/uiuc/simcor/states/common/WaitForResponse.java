package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForResponse extends TransactionState {

	public WaitForResponse( StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE, sap, TransactionStateNames.RESPONSE_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, next);
	}

}
