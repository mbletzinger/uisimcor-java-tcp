package org.nees.uiuc.simcor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.runner.notification.StoppedByUserException;
import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.listener.ClientConnections;
import org.nees.uiuc.simcor.listener.ListenerStateMachine;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.logging.ExitTransaction;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.StateActionsProcessorWithLsm;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public abstract class UiSimCorTcp {

	public enum ConnectType {
		P2P_RECEIVE_COMMAND, P2P_SEND_COMMAND, TRIGGER_CLIENT
	}

	private ConnectType connectType;

	private Archiving archive;

	private TcpError errors;

	private final Logger log = Logger.getLogger(UiSimCorTcp.class);

	private Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();

	private StateActionsProcessor sap;

	private Transaction transaction;

	public UiSimCorTcp(String type, String mdl) {
		initialize(ConnectType.valueOf(type), mdl);
	}

	public UiSimCorTcp(ConnectType type, String mdl) {
		initialize(type, mdl);
	}

	private void execute() {
		TransactionState state = machine.get(transaction.getState());
		log.debug("Executing state: " + state);
		state.execute(transaction);
	}

	public Archiving getArchive() {
		return archive;
	}

	public TcpError getErrors() {
		return errors;
	}

	public StateActionsProcessor getSap() {
		return sap;
	}

	/**
	 * 
	 * @return - returns the current transaction as a reference
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * Executes the next state of the state machine
	 * 
	 * @return the resulting state
	 */
	public TransactionStateNames isReady() {
		execute();
		log.debug("Current transaction: " + transaction);
		return transaction.getState();
	}

	public void setArchiveFilename(String filename) {
		archive.setFilename(filename);
		archive.setArchivingEnabled(true);
	}

	/**
	 * Set the current step or substep
	 * 
	 * @param s
	 * @param type
	 */
	public void setStep(int s, StepTypes type) {
		sap.getTf().setStep(s, type);
	}

	public void setTransaction(SimpleTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * For the receive command direction. This is called when a response is
	 * ready to be sent.
	 * 
	 * @param response
	 */
	public void continueTransaction(SimCorMsg response) {
		transaction.setResponse(response);
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.WAIT_FOR_RESPONSE);
		execute();

	}

	public void initialize(ConnectType type, String mdl) {
		this.connectType = type;
		if (connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			ListenerStateMachine lsm = new ListenerStateMachine(
					new ClientConnections(), true);
			this.sap = new StateActionsProcessorWithLsm(lsm);
		} else {
			this.sap = new StateActionsProcessor();
		}
		if(connectType.equals(ConnectType.P2P_RECEIVE_COMMAND)) {
			machine.put(TransactionStateNames.STOP_LISTENER)
		}
		
		this.archive = sap.getArchive();
		transaction = sap.getTf().createSendCommandTransaction(null);
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.getTf().setMdl(mdl);

	}

	/**
	 * 
	 * @return returns a copy of the transaction and tells the state machine
	 *         that the message has been offloaded
	 */
	public SimpleTransaction pickupTransaction() {
		SimpleTransaction result = new SimpleTransaction(transaction);
		transaction.setPickedUp(true);
		return result;
	}

	public void shutdown() {
		TransactionFactory transactionFactory = sap.getTf();
		transaction = new ExitTransaction();
		transaction.setDirection(transactionFactory.getDirection());
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		log.info("Closing connection");
		// Connection connection = connectionManager.getConnection();
		// if (connection != null) {
		// connectionManager.closeConnection();
		// }
		log.info("Shutting down network logger");
		archive.logTransaction(transaction);
	}

	/**
	 * Starts a Receive command transaction.
	 */
	public void startTransaction() {
		transaction.setState(TransactionStateNames.SETUP_READ_COMMAND);
		execute();
	}

	/**
	 * Start a Transmit command transaction
	 * 
	 * @param command
	 *            - transaction containing the command. This should be created
	 *            with the transactionFactory.
	 */

	public void startTransaction(SimpleTransaction command) {

		transaction = command;
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.WAIT_FOR_COMMAND);
		execute();
	}

	/**
	 * Sets up the connection used for transactions. For connections that
	 * connect to a remote host. This function will connected to the host. Use
	 * {@link Connection#getConnectionState()} to determine when the connection
	 * is ready. For connections that listen on a local port, This function will
	 * start the listener. However the listener will need to be monitored to get
	 * an incoming connection request.
	 * 
	 * @see {@link ListenerConnectionFactory#getConnection()}
	 */

	public void startup(TcpParameters params) {
		TransactionFactory transactionFactory = sap.getTf();
		if (params.isListener()) {
			transactionFactory.setDirection(DirectionType.RECEIVE_COMMAND);
		} else {
			transactionFactory.setDirection(DirectionType.SEND_COMMAND);
		}
		transaction = transactionFactory.createSendCommandTransaction(null);
		transaction.setPosted(true);
		if (params.isListener()) {
			transaction.setState(TransactionStateNames.START_LISTENING);
		} else {
			transaction.setState(TransactionStateNames.OPENING_CONNECTION);
		}
		if (archive.isAlive() == false) {
			archive.start();
		}
	}

}