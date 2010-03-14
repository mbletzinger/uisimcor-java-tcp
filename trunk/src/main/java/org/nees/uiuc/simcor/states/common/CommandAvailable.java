package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class CommandAvailable extends TransactionState {

	public CommandAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.COMMAND_AVAILABLE, sap,
				TransactionStateNames.WAIT_FOR_RESPONSE_POSTING);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.waitForPickUp(transaction, next);
	}

}
