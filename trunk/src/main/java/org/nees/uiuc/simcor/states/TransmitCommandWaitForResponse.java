package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TransmitCommandWaitForResponse extends TransactionState {
private final Logger log = Logger
		.getLogger(TransmitCommandWaitForResponse.class);
	public TransmitCommandWaitForResponse() {
		super(TransactionStateNames.WAIT_FOR_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if (connection.getConnectionState() == ConnectionState.BUSY) {
			return;
		}

		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg rsp = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Received response:" + rsp + " id: " + id);
		transaction.setResponse(rsp);
		transaction.setId(id);
		setStatus(transaction, result.getError(),
				TransactionStateNames.RESPONSE_AVAILABLE);
		transaction.setPickedUp(false);

	}

}
