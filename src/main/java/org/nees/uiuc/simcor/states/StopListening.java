package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class StopListening extends ConnectionState {

//	private Logger log = Logger.getLogger(StopListening.class);

	public StopListening(ConnectionFactory cf) {
		super(Transaction.TransactionStateNames.STOP_LISTENING, cf);
	}

	@Override
	public void execute(Transaction transaction, Connection connection) {
		boolean stopped = cf.stopListener();
		TcpError er = cf.checkForErrors();
		TransactionStateNames state = TransactionStateNames.STOP_LISTENING;
		if (stopped) {
				state = TransactionStateNames.TRANSACTION_DONE;
		}
		setStatus(transaction, er, state);
	}

}
