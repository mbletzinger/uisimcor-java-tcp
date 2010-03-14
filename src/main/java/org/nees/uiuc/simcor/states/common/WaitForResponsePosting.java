package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class WaitForResponsePosting extends TransactionState {

	public WaitForResponsePosting(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE_POSTING, sap,
				TransactionStateNames.ASSEMBLE_RESPONSE);
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.waitForPosted(transaction, next);
	}

}
