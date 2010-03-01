package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public abstract class ConnectionState extends TransactionState {

	protected ConnectionFactory cf;
	public ConnectionState(TransactionStateNames state, ConnectionFactory cf) {
		super(state);
		this.cf = cf;
	}

	protected void saveStatus(Transaction transaction, TcpError error, TransactionStateNames state, TransactionStateNames errState) {
		transaction.setError(error);
		if(error.getType().equals(TcpErrorTypes.NONE) == false) {
			cf.saveError();
			transaction.setState(errState);
		} else {
			transaction.setState(state);			
		}
	}

	@Override
	protected void setStatus(Transaction transaction, TcpError error,
			TransactionStateNames state) {
		TcpError err = error;
		if(err.getType().equals(TcpErrorTypes.NONE) == true) {
			err = cf.getSavedError();
		}
		super.setStatus(transaction, err, state);
	}

}
