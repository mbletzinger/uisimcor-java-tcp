package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class StartListening extends ConnectionState {

//	private Logger log = Logger.getLogger(StartListening.class);

	public StartListening(ConnectionFactory cf) {
		super(Transaction.TransactionStateNames.START_LISTENING, cf);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		TcpError er = new TcpError();
		
		TransactionStateNames state = TransactionStateNames.OPENING_CONNECTION;
		
		saveStatus(transaction, er, state, TransactionStateNames.STOP_LISTENING);
	}

}
