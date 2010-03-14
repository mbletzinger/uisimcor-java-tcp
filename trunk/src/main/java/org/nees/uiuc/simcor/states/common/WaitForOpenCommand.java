package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessorWithLcf;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;

public class WaitForOpenCommand extends TransactionState {

	public WaitForOpenCommand(StateActionsProcessorWithLcf sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_COMMAND, sap,
				TransactionStateNames.ASSEMBLE_OPEN_RESPONSE);
	}

	@Override
	public void execute(SimpleTransaction transaction) {
		sap.waitForSessionMsgRead(transaction, true, next);
	}

}
