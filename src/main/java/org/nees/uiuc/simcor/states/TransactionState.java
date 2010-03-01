package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public abstract class TransactionState {

	protected TransactionStateNames state;

	public abstract void execute(Transaction transaction, Connection connection);

	public TransactionState(TransactionStateNames state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return state.toString();
	}

	protected void setStatus(Transaction transaction, TcpError error, TransactionStateNames state) {
		TcpError err = error;
		transaction.setError(error);
		if(err.getType() != TcpErrorTypes.NONE) {
			transaction.setState(TransactionStateNames.ERRORS_EXIST);
		} else {
			transaction.setState(state);			
		}
	}

}