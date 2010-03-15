package org.nees.uiuc.simcor.states;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientIdWithConnection;
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
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class StateActionsProcessor {

	protected Archiving archive;
	protected ConnectionManager cm;
	protected final Logger log = Logger.getLogger(StateActionsProcessorWithLcf.class);
	protected TcpParameters params;
	private ClientIdWithConnection remoteClient;
	protected TransactionFactory tf;

	public StateActionsProcessor() {
		super();
		tf = new TransactionFactory();
		cm = new ConnectionManager();
		archive = new Archiving();
	}

	public StateActionsProcessor(TransactionFactory tf,
			ConnectionManager cm, Archiving archive) {
		super();
		this.archive = archive;
		this.cm = cm;
		this.tf = tf;
	}

	public void assembleSessionMessage(SimpleTransaction transaction, boolean isOpen,
			boolean isCommand, TransactionStateNames next) {
				SimCorMsg cnt;
				if (isCommand) {
					cnt = tf.createSessionCommand(isOpen);
					transaction.setCommand(cnt);
				} else {
					cnt = tf.createSessionResponse(transaction.getCommand());
					transaction.setResponse(cnt);
				}
				log.debug("Assembled " + transaction);
				setUpWrite(transaction, isCommand, next);
			}

	public void checkOpenConnection(SimpleTransaction transaction, TransactionStateNames next) {
		TcpError er = new TcpError();
		Connection connection = null;
		connection = cm.getConnection();
		if (connection.getConnectionState().equals(
				Connection.ConnectionStatus.BUSY)) {
			return;
		}
		er = cm.checkForErrors();
		connection.setMsgTimeout(transaction.getTimeout());
		saveStatus(transaction, er, next);
	}

	public void closingConnection(SimpleTransaction transaction, TransactionStateNames next) {
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

	public ConnectionManager getCm() {
		return cm;
	}

	public TcpParameters getParams() {
		return params;
	}

	public ClientIdWithConnection getRemoteClient() {
		return remoteClient;
	}

	public TransactionFactory getTf() {
		return tf;
	}

	public void openConnection(SimpleTransaction transaction) {
		log.debug("Starting connection");
		cm.setParams(params);
		cm.openConnection();
		saveStatus(transaction, new TcpError(),
				TransactionStateNames.CHECK_OPEN_CONNECTION);
	}

	public void recordTransaction(SimpleTransaction transaction, TransactionStateNames next) {
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE)) {
			transaction.setError(cm.getSavedError()); // capture connection
			// errors
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

	protected void saveStatus(SimpleTransaction transaction, TcpError error, TransactionStateNames state) {
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

	protected void setStatus(Transaction transaction, TcpError error, TransactionStateNames state, TransactionStateNames errstate) {
		TcpError err = error;
		if(transaction instanceof SimpleTransaction) {
			((SimpleTransaction)transaction).setError(error);
		}
		if (err.getType() != TcpErrorTypes.NONE) {
			transaction.setState(errstate);
		} else {
			transaction.setState(state);
		}
	}
	protected void setStatus(Transaction transaction, TcpError error, TransactionStateNames state) {
		setStatus(transaction, error, state,TransactionStateNames.TRANSACTION_DONE);
	}

	public void setTf(TransactionFactory tf) {
		this.tf = tf;
	}

	public void setUpRead(Transaction transaction, boolean isCommand, TransactionStateNames next) {
		Connection c = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.READ);
		c.setMsgTimeout(transaction.getTimeout());
		c.setToRemoteMsg(action);
		transaction.setState(next);
	}

	public void setUpWrite(SimpleTransaction transaction, boolean isCommand, TransactionStateNames next) {
		Connection connection = cm.getConnection();
		TcpActionsDto action = new TcpActionsDto();
		action.setAction(ActionsType.WRITE);
		Msg2Tcp msg = action.getMsg();
		msg.setId(transaction.getId());
		if (isCommand) {
			msg.setMsg(transaction.getCommand());
		} else {
			msg.setMsg(transaction.getResponse());
		}
		log.debug("Sending: " + transaction);
		connection.setToRemoteMsg(action);
		transaction.setPosted(false);
		transaction.setState(next);
	
	}

	public void waitForPickUp(SimpleTransaction transaction, TransactionStateNames next) {
		if (transaction.isPickedUp() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPickedUp(false);
	}

	public void waitForPosted(SimpleTransaction transaction, TransactionStateNames next) {
		if (transaction.isPosted() == false) {// Still waiting
			return;
		}
		setStatus(transaction, new TcpError(), next);
		transaction.setPosted(false);
	}

	public void waitForRead(SimpleTransaction transaction, boolean isCommand, TransactionStateNames next) {
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

	public void waitForSend(SimpleTransaction transaction, TransactionStateNames next) {
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

	public void waitForSessionMsgRead(SimpleTransaction transaction, boolean isCommand, TransactionStateNames next) {
		waitForRead(transaction, true, next);
		if (transaction.getState().equals(next)) {
			Connection connection = cm.getConnection();
			String system = transaction.getCommand().getContent();
			remoteClient = new ClientIdWithConnection(connection, system, connection
					.getRemoteHost());
		}
	}

}