package org.nees.uiuc.simcor;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.Archiving;
import org.nees.uiuc.simcor.states.StateActionsProcessor;
import org.nees.uiuc.simcor.states.TransactionState;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionManager;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.TransactionIdentity.StepTypes;

public abstract class UiSimCorTcp {

	protected Archiving archive;
	
	private TcpError errors;

	private final Logger log = Logger.getLogger(UiSimCorTcp.class);

	protected Map<TransactionStateNames, TransactionState> machine = new HashMap<TransactionStateNames, TransactionState>();

	protected StateActionsProcessor sap;

	protected Transaction transaction;

	public UiSimCorTcp() {
		sap = new StateActionsProcessor();
		archive = sap.getArchive();
	}

	public abstract void continueTransaction(SimCorMsg response);

	protected void execute() {
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

	protected abstract void initialize(DirectionType dir, String mdl);
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

	public abstract Transaction pickupTransaction();

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

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public abstract void shutdown();

	public abstract void startTransaction();

	public abstract void startTransaction(Transaction command);

	public abstract void startup(TcpParameters params);

}