package org.nees.uiuc.simcor.states.old;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SendCloseSession extends TransactionState {

	public SendCloseSession(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_COMMAND, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.assembleSessionMessage(transaction, false, true,
				TransactionStateNames.CLOSING_CONNECTION);
	}

}
