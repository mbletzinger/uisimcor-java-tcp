package org.nees.uiuc.simcor.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.states.common.AssembleCommand;
import org.nees.uiuc.simcor.states.common.AssembleResponse;
import org.nees.uiuc.simcor.states.common.CloseConnection;
import org.nees.uiuc.simcor.states.common.SendingCommand;
import org.nees.uiuc.simcor.states.common.SendingResponse;
import org.nees.uiuc.simcor.states.common.SetupReadMessage;
import org.nees.uiuc.simcor.states.common.TransactionDone;
import org.nees.uiuc.simcor.states.common.WaitForOpenCommand;
import org.nees.uiuc.simcor.states.common.WaitForOpenResponse;
import org.nees.uiuc.simcor.states.common.AssembleCommand.AssembleCommandType;
import org.nees.uiuc.simcor.states.listener.ListenForConnections;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Transaction;

public class ListenerStateMachine extends Thread {
	private final ClientConnections cc;
	private TransactionStateNames currentState = TransactionStateNames.START_LISTENING;
	private TcpError error;
	private final boolean isP2P;
	private boolean isRunning;
	protected Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();
	private ClientId oneClient = null;
	private StateActionsProcessor sap = new StateActionsProcessor();
	private final Logger log = Logger.getLogger(ListenerStateMachine.class);

	public ListenerStateMachine(ClientConnections cc, boolean isP2P) {
		super();
		this.cc = cc;
		this.isP2P = isP2P;
		setRunning(false);
	}

	public synchronized TransactionStateNames getCurrentState() {
		return currentState;
	}

	public synchronized TcpError getError() {
		return error;
	}

	public synchronized ClientId getOneClient() {
		return oneClient;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	public Transaction initialize() {
		if (isP2P) {
			machine.put(TransactionStateNames.LISTEN_FOR_CONNECTIONS,
					new ListenForConnections(sap,
							TransactionStateNames.SETUP_READ_COMMAND));
			machine.put(TransactionStateNames.SETUP_READ_COMMAND,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_COMMAND, sap,
							true, TransactionStateNames.WAIT_FOR_OPEN_COMMAND));
			machine.put(TransactionStateNames.WAIT_FOR_OPEN_COMMAND,
					new WaitForOpenCommand(sap));
			machine.put(TransactionStateNames.ASSEMBLE_OPEN_RESPONSE,
					new AssembleResponse(
							TransactionStateNames.ASSEMBLE_OPEN_RESPONSE, sap,
							true));
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse(sap));
		} else {
			machine.put(TransactionStateNames.LISTEN_FOR_CONNECTIONS,
					new ListenForConnections(sap,
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND));
			machine.put(TransactionStateNames.ASSEMBLE_OPEN_COMMAND,
					new AssembleCommand(
							TransactionStateNames.ASSEMBLE_OPEN_COMMAND, sap,
							AssembleCommandType.OPEN));
			machine.put(TransactionStateNames.SENDING_COMMAND,
					new SendingCommand(sap,
							TransactionStateNames.WAIT_FOR_RESPONSE));
			machine.put(TransactionStateNames.SETUP_READ_RESPONSE,
					new SetupReadMessage(
							TransactionStateNames.SETUP_READ_RESPONSE, sap,
							false, TransactionStateNames.TRANSACTION_DONE));
			machine.put(TransactionStateNames.SETUP_READ_RESPONSE,
					new WaitForOpenResponse(sap));
		}
		machine.put(TransactionStateNames.TRANSACTION_DONE,
				new TransactionDone(sap,
						TransactionStateNames.LISTEN_FOR_CONNECTIONS));
		machine.put(TransactionStateNames.CLOSING_CONNECTION,
				new CloseConnection(sap));
		Transaction transaction = sap.getTf().createTransaction(null);
		sap.startListening(transaction);
		return transaction;
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public synchronized ClientId pickupOneClient() {
		ClientId result = oneClient;
		oneClient = null;
		return result;
	}

	@Override
	public void run() {
		Transaction transaction = initialize();
		setError(transaction.getError());
		transaction.setState(TransactionStateNames.LISTEN_FOR_CONNECTIONS);
		setCurrentState(transaction.getState());
		if (transaction.getError().getType().equals(TcpErrorTypes.NONE) == false) {
			return;
		}
		setRunning(true);
		while (isRunning()) {
			log.debug("LSM state:" + transaction.getState() + " client "
					+ sap.getRemoteClient() + "  error "
					+ transaction.getError());
			machine.get(getCurrentState()).execute(transaction);
			if(transaction.getState().equals(TransactionStateNames.TRANSACTION_DONE)) {
				updateClient(transaction);
			}
			setCurrentState(transaction.getState());
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}
		}
		log.debug("Stopped LSM state:" + transaction.getState() + " client "
				+ sap.getRemoteClient() + "  error " + transaction.getError());

		sap.stopListening(transaction);
	}

	private void updateClient(Transaction transaction) {
		setError(transaction.getError());
		if (isP2P) {
			oneClient = sap.getRemoteClient();
		} else {
			cc.addClient(sap.getRemoteClient());
		}

	}
	public synchronized void setCurrentState(TransactionStateNames currentState) {
		this.currentState = currentState;
	}

	public synchronized void setError(TcpError error) {
		this.error = error;
	}

	public synchronized void setOneClient(ClientId oneClient) {
		this.oneClient = oneClient;
	}

	public synchronized void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
