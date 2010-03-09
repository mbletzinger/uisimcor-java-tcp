package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class StateActionsProcessor {
	private Archiving archive;
	private ConnectionFactory cf;
	private ConnectionManager cm;
	private final Logger log = Logger.getLogger(StateActionsProcessor.class);
	private TcpParameters params;
	private TransactionFactory tf;

	public StateActionsProcessor() {
		super();
		cf = new ConnectionFactory();
		tf = new TransactionFactory();
		cm = new ConnectionManager();
		archive = new Archiving();
	}

	public StateActionsProcessor(ConnectionFactory cf, TransactionFactory tf,
			ConnectionManager cm, Archiving archive) {
		super();
		this.cf = cf;
		this.tf = tf;
		this.cm = cm;
		this.archive = archive;
	}

	public void assembleSessionMessage(Transaction transaction, boolean isOpen,
			boolean isCommand, TransactionStateNames next) {
		SimCorMsg cnt;
		if (isCommand) {
			cnt = tf.createSessionCommand(isOpen);
			transaction.setCommand(cnt);
		} else {
			cnt = tf.createSessionResponse(transaction.getCommand());
			transaction.setResponse(cnt);
		}
		setUpWrite(transaction, isCommand, next);
	}

	public void checkOpenConnection(Transaction transaction,
			TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = cm.getConnection();
		if (connection.getConnectionState().equals(
				Connection.ConnectionStatus.BUSY)) {
			return;
		}
		er = cm.checkForErrors();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		connection.setMsgTimeout(transaction.getTimeout());
		connection.setToRemoteMsg(action);
		saveStatus(transaction, er, next);
	}

	public void closingConnection(Transaction transaction,
			TransactionStateNames next) {
		boolean closed = cm.closeConnection();
		TcpError er = new TcpError();
		TransactionStateNames state = TransactionStateNames.CLOSING_CONNECTION;
		if (closed) {
			state = next;
			er = cm.checkForErrors();
			if (er.getType().equals(TcpErrorTypes.NONE)) {
				er = cm.getSavedError();
			}
		}
		setStatus(transaction, er, state);
	}

	public Archiving getArchive() {
		return archive;
	}

	public ConnectionFactory getCf() {
		return cf;
	}

	public ConnectionManager getCm() {
		return cm;
	}

	public TcpParameters getParams() {
		return params;
	}

	public TransactionFactory getTf() {
		return tf;
	}

	public void ListenerForConnection(Transaction transaction,
			TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = cf.checkForListenerConnection();
		if (connection == null
				|| connection.getConnectionState().equals(
						Connection.ConnectionStatus.BUSY)) {
			return;
		}
		er = cf.checkForErrors();
		cm.setConnection(connection);
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		connection.setMsgTimeout(transaction.getTimeout());
		connection.setToRemoteMsg(action);
		saveStatus(transaction, er, next);
	}

	public void openConnection(Transaction transaction) {
		log.debug("Starting connection");
		cm.setParams(params);
		cm.openConnection();
		saveStatus(transaction, new TcpError(),
				TransactionStateNames.CHECK_OPEN_CONNECTION);
	}

	public void recordTransaction(Transaction transaction) {
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(cm.getSavedError()); // capture connection
			// errors
		}
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(cf.getSavedError()); // capture listener errors
		}
		if (archive.isArchivingEnabled()) {
			log.debug("Handling: " + transaction);
			archive.logTransaction(transaction);
		}
		cm.clearError();
		setStatus(transaction, new TcpError(), TransactionStateNames.READY);
	}

	protected void saveStatus(Transaction transaction, TcpError error,
			TransactionStateNames state) {
		transaction.setError(error);
		if (error.getType().equals(TcpErrorTypes.NONE) == false) {
			cm.saveError();
			transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		} else {
			transaction.setState(state);
		}
	}

	public void setArchive(Archiving archive) {
		this.archive = archive;
	}

	public void setCf(ConnectionFactory cf) {
		this.cf = cf;
	}

	public void setCm(ConnectionManager cm) {
		this.cm = cm;
	}

	public void setIdentity(String mdl, String systemDescription) {
		tf.setMdl(mdl);
		tf.setSystemDescription(systemDescription);
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	protected void setStatus(Transaction transaction, TcpError error,
			TransactionStateNames state) {
		TcpError err = error;
		transaction.setError(error);
		if (err.getType() != TcpErrorTypes.NONE) {
			transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		} else {
			transaction.setState(state);
		}
	}

	public void setTf(TransactionFactory tf) {
		this.tf = tf;
	}

	public void setUpRead(Transaction transaction, boolean isCommand,
			TransactionStateNames next) {
		Connection c = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		c.setMsgTimeout(transaction.getTimeout());
		c.setToRemoteMsg(action);
		transaction.setState(next);
	}

	public void setUpWrite(Transaction transaction, boolean isCommand,
			TransactionStateNames next) {
		Connection connection = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.WRITE);
		Msg2Tcp msg = action.getMsg();
		msg.setId(transaction.getId());
		if (isCommand) {
			msg.setMsg(transaction.getCommand());
		} else {
			msg.setMsg(transaction.getCommand());
		}
		log.debug("Sending: " + transaction);
		connection.setToRemoteMsg(action);
		transaction.setPosted(false);
		transaction.setState(next);

	}

	public void startListening(Transaction transaction) {
		log.debug("Start listening");
		cm.setParams(params);
		cf.setParams(params);
		cf.startListener();
		saveStatus(transaction, new TcpError(),
				TransactionStateNames.OPENING_CONNECTION);
	}

	public void stopListening(Transaction transaction) {
		cf.stopListener();
		TcpError er = cf.checkForErrors();
		TransactionStateNames state = TransactionStateNames.STOP_LISTENER;
		state = TransactionStateNames.TRANSACTION_DONE;
		setStatus(transaction, er, state);
	}

	public void waitForPickUp(Transaction transaction,
			TransactionStateNames next) {
		if (transaction.isPickedUp() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPickedUp(false);
	}

	public void waitForPosted(Transaction transaction,
			TransactionStateNames next) {
		if (transaction.isPosted() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPosted(false);
	}

	public void waitForRead(Transaction transaction, boolean isCommand,
			TransactionStateNames next) {
		Connection connection = cm.getConnection();
		if (connection.getConnectionState().equals(ConnectionStatus.BUSY)) {
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg msg = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Received msg:" + msg + " id: " + id);
		if (isCommand) {
			transaction.setCommand(msg);
		} else {
			transaction.setResponse(msg);
		}
		transaction.setId(id);
		saveStatus(transaction, cm.checkForErrors(), next);
		transaction.setPickedUp(false);
	}

	public void waitForSend(Transaction transaction, TransactionStateNames next) {
		Connection connection = cm.getConnection();
		// Check if command has been sent
		if (connection.getConnectionState() == ConnectionStatus.BUSY) {
			return;
		}
		TcpActionsDto result = connection.getFromRemoteMsg();
		SimCorMsg msg = result.getMsg().getMsg();
		TransactionIdentity id = result.getMsg().getId();
		log.debug("Sent msg:" + msg + " id: " + id);
		saveStatus(transaction, result.getError(), next);

	}

}
