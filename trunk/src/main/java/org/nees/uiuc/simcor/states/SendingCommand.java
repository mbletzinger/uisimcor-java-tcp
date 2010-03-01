package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendingCommand extends TransactionState {
	private final Logger log = Logger.getLogger(SendingCommand.class);

	public SendingCommand() {
		super(TransactionStateNames.SENDING_COMMAND);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		// Check if command has been sent
		if (connection.getConnectionState() == ConnectionState.BUSY) {
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg cmd = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Sent command:" + cmd + " id: " + id);
		setStatus(transaction, result.getError(),
				TransactionStateNames.READ_RESPONSE);

	}

}
