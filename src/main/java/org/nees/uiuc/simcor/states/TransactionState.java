package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public abstract class TransactionState {

	protected TransactionStateNames state;
	protected StateActionsProcessor sap;

	public abstract void execute(Transaction transaction);

	public TransactionState(TransactionStateNames state, StateActionsProcessor sap) {
		this.state = state;
		this.sap = sap;
	}

	@Override
	public String toString() {
		return state.toString();
	}

	protected void setStatus(Transaction transaction, TcpError error, TransactionStateNames state) {
		TcpError err = error;
		transaction.setError(error);
		if(err.getType() != TcpErrorTypes.NONE) {
			transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		} else {
			transaction.setState(state);			
		}
	}

}