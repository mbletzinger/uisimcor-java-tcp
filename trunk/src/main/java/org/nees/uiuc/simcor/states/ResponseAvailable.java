package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ResponseAvailable extends TransactionState {

	public ResponseAvailable() {
		super(TransactionStateNames.RESPONSE_AVAILABLE);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if(transaction.isPickedUp() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), TransactionStateNames.TRANSACTION_DONE);
		transaction.setPosted(false);
	}

}
