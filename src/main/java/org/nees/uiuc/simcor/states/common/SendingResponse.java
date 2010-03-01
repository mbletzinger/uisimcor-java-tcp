package org.nees.uiuc.simcor.states.common;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class SendingResponse extends TransactionState {

	private final Logger log = Logger.getLogger(SendingResponse.class);
	public SendingResponse(StateActionsProcessor sap) {
		super(TransactionStateNames.SENDING_RESPONSE,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForSend(transaction, TransactionStateNames.TRANSACTION_DONE);
	}

}
