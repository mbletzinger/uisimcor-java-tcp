package org.nees.uiuc.simcor.states;

import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;

public class StateActionsProcessorWithLcf extends StateActionsProcessor {
	private ListenerConnectionFactory lcf;
	public StateActionsProcessorWithLcf() {
		super();
		lcf = new ListenerConnectionFactory();
	}

	public StateActionsProcessorWithLcf(ListenerConnectionFactory cf, TransactionFactory tf,
			ConnectionManager cm, Archiving archive) {
		super();
		this.lcf = cf;
	}

	public ListenerConnectionFactory getLcf() {
		return lcf;
	}

	public void listenForConnection(SimpleTransaction transaction,
			TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = lcf.checkForListenerConnection();
		if (connection == null
				|| connection.getConnectionState().equals(
						Connection.ConnectionStatus.BUSY)) {
			return;
		}
		er = lcf.checkForErrors();
		cm.setConnection(connection);
		connection.setMsgTimeout(transaction.getTimeout());
		saveStatus(transaction, er, next);
	}

	@Override
	public void recordTransaction(SimpleTransaction transaction, TransactionStateNames next) {
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(cm.getSavedError()); // capture connection
			// errors
		}
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(lcf.getSavedError()); // capture listener errors
		}
		if (archive.isArchivingEnabled()) {
			log.debug("Handling: " + transaction);
			archive.logTransaction(transaction);
		}
		if(next.equals(TransactionStateNames.READY)) {
		cm.clearError();
		}
		setStatus(transaction, new TcpError(), next);
	}

	public void setLcf(ListenerConnectionFactory cf) {
		this.lcf = cf;
	}

	public void startListening(SimpleTransaction transaction) {
		log.debug("Start listening");
		cm.setParams(params);
		lcf.setParams(params);
		lcf.startListener();
		saveStatus(transaction, new TcpError(),
				TransactionStateNames.OPENING_CONNECTION);
	}
	
	public void stopListening(SimpleTransaction transaction) {
		lcf.stopListener();
		TcpError er = lcf.checkForErrors();
		TransactionStateNames state = TransactionStateNames.STOP_LISTENER;
		state = TransactionStateNames.TRANSACTION_DONE;
		setStatus(transaction, er, state);
	}

}
