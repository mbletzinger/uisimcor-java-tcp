package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class Ready extends TransactionState {

	public Ready() {
		super(TransactionStateNames.READY);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
	}

}
