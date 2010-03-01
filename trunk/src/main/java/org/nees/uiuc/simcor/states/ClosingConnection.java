package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class ClosingConnection extends ConnectionState {

	private Logger log = Logger.getLogger(ClosingConnection.class);

	public ClosingConnection(ConnectionFactory cf) {
		super(Transaction.TransactionStateNames.CLOSING_CONNECTION, cf);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		Connection c = connection;
		boolean closed = cf.closeConnection();
		TcpError er = new TcpError();
		TransactionStateNames state = TransactionStateNames.CLOSING_CONNECTION;
		if (closed) {
			if (er.getType().equals(TcpErrorTypes.NONE)) {
				er = cf.checkForErrors();
			}
			if (cf.getParams().isListener()) {
				state = TransactionStateNames.STOP_LISTENING;
				cf.stopListener();
			} else {
				if(cf.getSavedError().getType().equals(TcpErrorTypes.NONE)) {
				state = TransactionStateNames.TRANSACTION_DONE;
				} else {
					state = TransactionStateNames.ERRORS_EXIST;					
				}
			}
		}
		setStatus(transaction, er, state);
	}
}
