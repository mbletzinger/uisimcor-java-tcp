package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class SendingCommand extends TransactionState {

	public SendingCommand(
			StateActionsProcessorWithLcf sap, TransactionStateNames next) {
		super(TransactionStateNames.SENDING_COMMAND, sap, next);
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.waitForSend(transaction, next);
	}

}
