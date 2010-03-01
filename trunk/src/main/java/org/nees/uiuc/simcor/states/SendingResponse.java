package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendingResponse extends TransactionState {

	private final Logger log = Logger.getLogger(SendingResponse.class);
	public SendingResponse() {
		super(TransactionStateNames.SENDING_RESPONSE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		//Check if response has been sent
		if (connection.getConnectionState() == ConnectionState.BUSY) {
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg rsp = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Sent response:" + rsp + " id: " + id);
		setStatus(transaction, result.getError(),
				TransactionStateNames.TRANSACTION_DONE);
	}

}
