package org.nees.uiuc.simcor.states.listener;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendingCommand extends TransactionState {

	public SendingCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.SENDING_COMMAND, sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSend(transaction, TransactionStateNames.READ_RESPONSE);
	}

}
