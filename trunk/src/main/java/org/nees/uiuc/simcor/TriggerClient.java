package org.nees.uiuc.simcor;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ConnectionFactory;
import org.nees.uiuc.simcor.states.listener.SendingCommand;
import org.nees.uiuc.simcor.states.listener.WaitForResponse;
import org.nees.uiuc.simcor.states.old.ResponseAvailable;
import org.nees.uiuc.simcor.states.old.SetUpCommand;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

/**
 * 
 * This class is the main class used for a trigger message recipient.
 * 
 * Transmit Command Sequence:
 * 
 * trigger.startTransaction(command_transaction) to send the command
 * 
 * trigger.isReady() until the state returned is RESPONSE_AVAILABLE
 * 
 * trigger.pickupTransaction() To get the response
 * 
 * trigger.isReady() until the state returned is TRANSACTION_DONE
 * 
 * @author Michael Bletzinger
 */
public class TriggerClient extends UiSimCorTcp {
	private final Logger log = Logger.getLogger(TriggerClient.class);

/**
 * 
 * @param dir - flag to indicate which direction the commands are going.
 * @param params - network parameters
 */
	public TriggerClient(ConnectionFactory cf) {
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
				new SetUpCommand());
		machine
				.put(TransactionStateNames.SENDING_COMMAND,
						new SendingCommand());
		machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
				new WaitForResponse());
		machine.put(TransactionStateNames.RESPONSE_AVAILABLE,
				new ResponseAvailable());
//		machine.put(TransactionStateNames.TRANSACTION_DONE,
//				new TransactionDone(archive));
//		machine.put(TransactionStateNames.ERRORS_EXIST, new ErrorsExist(connarchive));
//		this.connection = connection;
	}
//	public Connection getConnection() {
//		return connection;
//	}
//	public TcpError getErrors() {
//		return errors;
//	}
	/**
	 * 
	 * @return - returns the current transaction as a reference
	 */
	public Transaction getTransaction() {
		return transaction;
	}
	/**
	 * 
	 * @return returns a copy of the transaction and tells the state machine that the message has been offloaded
	 */
	public Transaction pickupTransaction() {
		Transaction result = new Transaction(transaction);
		transaction.setPickedUp(true);
		return result;
	}
	/**
	 * Executes the next state of the state machine
	 * @return the resulting state
	 */
	public TransactionStateNames isReady() {
		execute();
		log.debug("Current transaction: " + transaction);
		return transaction.getState();
	}
	@Override
	public void continueTransaction(SimCorMsg response) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startTransaction(Transaction command) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void initialize(DirectionType dir) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startTransaction() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startup(TcpParameters params) {
		// TODO Auto-generated method stub
		
	}
}
