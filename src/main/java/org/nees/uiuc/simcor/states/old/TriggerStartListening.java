package org.nees.uiuc.simcor.states.old;

import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.states.ConnectionState;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

public class TriggerStartListening extends ConnectionState {

//	private Logger log = Logger.getLogger(StartListening.class);

	public TriggerStartListening(ConnectionFactory cf) {
		super(Transaction.TransactionStateNames.START_LISTENING, cf);
	}

	@Override
	public void execute(Transaction transaction, ConnectionManager cm) {
		TcpError er = new TcpError();
		
		TransactionStateNames state = TransactionStateNames.OPENING_CONNECTION;
		
		saveStatus(transaction, er, state, TransactionStateNames.STOP_LISTENING,cm);
	}

}
