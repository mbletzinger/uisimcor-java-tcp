package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ReceiveCommandWaitForCommand extends TransactionState {
	private final Logger log = Logger
			.getLogger(ReceiveCommandWaitForCommand.class);

	public ReceiveCommandWaitForCommand() {
		super(TransactionStateNames.WAIT_FOR_COMMAND);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if (connection.getConnectionState().equals(ConnectionState.BUSY)) {
			return;
		}

		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg cmd = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Received command:" + cmd + " id: " + id);
		transaction.setCommand(cmd);
		transaction.setId(id);
		setStatus(transaction, result.getError(),
				TransactionStateNames.COMMAND_AVAILABLE);
		transaction.setPickedUp(false);
	}

}
