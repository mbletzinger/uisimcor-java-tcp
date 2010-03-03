package org.nees.uiuc.simcor.states.old;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReceiveCommandWaitForResponse extends TransactionState {
	private final Logger log = Logger
			.getLogger(ReceiveCommandWaitForResponse.class);

	public ReceiveCommandWaitForResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_RESPONSE, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForPosted(transaction, TransactionStateNames.SETUP_RESPONSE);
	}

}
