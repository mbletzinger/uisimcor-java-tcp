package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class OpeningConnection extends ConnectionState {

	private Logger log = Logger.getLogger(OpeningConnection.class);

	public OpeningConnection(ConnectionFactory cf) {
		super(Transaction.TransactionStateNames.OPENING_CONNECTION, cf);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		if(cf.getParams().isListener()) {
			connection = cf.checkForListenerConnection();
		}
		if(connection == null || connection.getConnectionState().equals(org.nees.uiuc.simcor.tcp.Connection.ConnectionState.BUSY)) {
			return;
		}
		TransactionStateNames state = TransactionStateNames.TRANSACTION_DONE;
		TcpError er = cf.checkForErrors();
		saveStatus(transaction, er, state,TransactionStateNames.CLOSING_CONNECTION);
	}

}
