package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class CommandAvailable extends TransactionState {

	public CommandAvailable() {
		super(TransactionStateNames.COMMAND_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if(transaction.isPickedUp() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), TransactionStateNames.WAIT_FOR_RESPONSE);
		transaction.setPosted(false);

	}

}
