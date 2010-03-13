package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class SendingCommand extends TransactionState {

	public SendingCommand(
			StateActionsProcessorWithLcf sap, TransactionStateNames next) {
		super(TransactionStateNames.SENDING_COMMAND, sap, next);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSend(transaction, next);
	}

}
