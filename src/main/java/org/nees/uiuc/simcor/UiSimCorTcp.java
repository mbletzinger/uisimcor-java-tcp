package org.nees.uiuc.simcor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public abstract class UiSimCorTcp {

	protected ConnectionManager connectionManager = new ConnectionManager();

	public abstract void shutdown();

	public abstract void startup(TcpParameters params);

	public abstract void startTransaction();

	public abstract void startTransaction(Transaction command);

	public abstract Transaction pickupTransaction();

	public abstract void continueTransaction(SimCorMsg response);

	protected abstract void initialize(DirectionType dir, String mdl);

	private TcpError errors;
	protected Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();
	protected TransactionFactory transactionFactory = new TransactionFactory();
	protected Transaction transaction;
	protected Archiving archive = new Archiving();
	private final Logger log = Logger.getLogger(UiSimCorTcp.class);

	public UiSimCorTcp() {
		super();
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public TcpError getErrors() {
		return errors;
	}

	public TransactionFactory getTransactionFactory() {
		return transactionFactory;
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

	/**
	 * Set the current step or substep
	 * 
	 * @param s
	 * @param type
	 */
	public void setStep(int s, StepTypes type) {
		transactionFactory.setStep(s, type);
	}

	protected void execute() {
		TransactionState state = machine.get(transaction.getState());
		log.debug("Executing state: " + state);
		state.execute(transaction, connectionManager);
	}

	public void setArchiveFilename(String filename) {
		archive.setFilename(filename);
		archive.setArchivingEnabled(true);
	}

}