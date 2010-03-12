package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.transaction.Transaction;

public class WaitForOpenCommand extends TransactionState {

	public WaitForOpenCommand(StateActionsProcessor sap) {
		super(TransactionStateNames.WAIT_FOR_OPEN_COMMAND, sap,
				TransactionStateNames.ASSEMBLE_OPEN_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForRead(transaction, true, next);
		if (transaction.getState().equals(next)) {
			Connection connection = sap.getCm().getConnection();
			String system = transaction.getCommand().getContent();
			remoteClient = new ClientId(connection, system, connection
					.getRemoteHost());
		}
	}

}
