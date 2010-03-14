package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForCommand extends TransactionState {

	public WaitForCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_COMMAND, sap, TransactionStateNames.COMMAND_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, next);
	}

}
