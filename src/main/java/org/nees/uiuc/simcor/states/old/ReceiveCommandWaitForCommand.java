package org.nees.uiuc.simcor.states.old;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ReceiveCommandWaitForCommand extends TransactionState {
	private final Logger log = Logger
			.getLogger(ReceiveCommandWaitForCommand.class);

	public ReceiveCommandWaitForCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_COMMAND,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, TransactionStateNames.COMMAND_AVAILABLE);
	}

}
