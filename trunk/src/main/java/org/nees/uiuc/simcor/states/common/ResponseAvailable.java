package org.nees.uiuc.simcor.states.common;

import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ResponseAvailable extends TransactionState {

	public ResponseAvailable(StateActionsProcessor sap) {
		super(TransactionStateNames.RESPONSE_AVAILABLE,sap);
	}

	@Override
	public void execute(Transaction transaction) {
		sap.waitForPickUp(transaction, TransactionStateNames.TRANSACTION_DONE);
	}

}
