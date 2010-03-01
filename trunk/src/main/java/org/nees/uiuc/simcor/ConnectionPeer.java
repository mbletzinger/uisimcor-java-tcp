package org.nees.uiuc.simcor;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.logging.ExitTransaction;
import org.nees.uiuc.simcor.states.ClosingConnection;
import org.nees.uiuc.simcor.states.CommandAvailable;
import org.nees.uiuc.simcor.states.ErrorsExist;
import org.nees.uiuc.simcor.states.OpeningConnection;
import org.nees.uiuc.simcor.states.ReadCommand;
import org.nees.uiuc.simcor.states.ReadResponse;
import org.nees.uiuc.simcor.states.Ready;
import org.nees.uiuc.simcor.states.ReceiveCommandWaitForCommand;
import org.nees.uiuc.simcor.states.ReceiveCommandWaitForResponse;
import org.nees.uiuc.simcor.states.ResponseAvailable;
import org.nees.uiuc.simcor.states.SendingCommand;
import org.nees.uiuc.simcor.states.SendingResponse;
import org.nees.uiuc.simcor.states.StartListening;
import org.nees.uiuc.simcor.states.StopListening;
import org.nees.uiuc.simcor.states.TransactionDone;
import org.nees.uiuc.simcor.states.TransmitCommandWaitForCommand;
import org.nees.uiuc.simcor.states.TransmitCommandWaitForResponse;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.ConnectionFactory;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.Transaction;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;
import org.nees.uiuc.simcor.transaction.Transaction.TransactionStateNames;

/**
 * 
 * This class is the main class used for point-to-point communications. The
 * direction flag indicates whether the communication starts with a send command
 * or a receive command.
 * 
 * Transmit Command Sequence:
 * 
 * simcor.startTransaction(command_transaction) to send the command
 * 
 * simcor.isReady() until the state returned is RESPONSE_AVAILABLE
 * 
 * simcor.pickupTransaction() To get the response
 * 
 * simcor.isReady() until the state returned is TRANSACTION_DONE
 * 
 * 
 * Receive Command Sequence:
 * 
 * simcor.startTransaction() to start listening for a command.
 * 
 * simcor.isReady() until the state returned is COMMAND_AVAILABLE
 * 
 * simcor.pickupTransaction() to get the command.
 * 
 * simcor.continueTransaction(response_msg) to set the response
 * 
 * simcor.isReady() until the state returned is TRANSACTION_DONE
 * 
 * 
 * @author Michael Bletzinger
 */
public class ConnectionPeer extends UiSimCorTcp {
	final Logger log = Logger.getLogger(ConnectionPeer.class);

	/**
	 * 
	 * @param dir
	 *            - flag to indicate which direction the commands are going.
	 * @param params
	 *            - network parameters
	 */
	public ConnectionPeer(DirectionType dir, TcpParameters params) {
		initialize(dir, params);
	}

	public ConnectionPeer(String dirS, TcpParameters params) {
		initialize(DirectionType.valueOf(dirS), params);
	}

	@Override
	protected void initialize(DirectionType dir, TcpParameters params) {
		connectionFactory.setParams(params);
		transactionFactory.setDirection(dir);
		if (dir == DirectionType.RECEIVE_COMMAND) {
			machine.put(TransactionStateNames.START_LISTENING,
					new StartListening(connectionFactory));
			machine.put(TransactionStateNames.STOP_LISTENING,
					new StopListening(connectionFactory));
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new ReceiveCommandWaitForCommand());
			machine.put(TransactionStateNames.COMMAND_AVAILABLE,
					new CommandAvailable());
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
					new ReceiveCommandWaitForResponse());
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse());
		} else {
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new TransmitCommandWaitForCommand());
			machine.put(TransactionStateNames.SENDING_COMMAND,
					new SendingCommand());
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
					new TransmitCommandWaitForResponse());
			machine.put(TransactionStateNames.RESPONSE_AVAILABLE,
					new ResponseAvailable());

		}
		machine.put(TransactionStateNames.TRANSACTION_DONE,
				new TransactionDone(connectionFactory, archive));
		machine.put(TransactionStateNames.ERRORS_EXIST, new ErrorsExist(
				connectionFactory, archive));
		machine.put(TransactionStateNames.READY, new Ready());
		machine.put(TransactionStateNames.OPENING_CONNECTION,
				new OpeningConnection(connectionFactory));
		machine.put(TransactionStateNames.CLOSING_CONNECTION,
				new ClosingConnection(connectionFactory));
		machine.put(TransactionStateNames.READ_COMMAND,
				new ReadCommand());
		machine.put(TransactionStateNames.READ_RESPONSE,
				new ReadResponse());
		transaction = transactionFactory.createTransaction(null);
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);

	}

	/**
	 * For the receive command direction. This is called when a response is
	 * ready to be sent.
	 * 
	 * @param response
	 */
	@Override
	public void continueTransaction(SimCorMsg response) {
		transaction.setResponse(response);
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.WAIT_FOR_RESPONSE);
		execute();

	}

	/**
	 * 
	 * @return returns a copy of the transaction and tells the state machine
	 *         that the message has been offloaded
	 */
	@Override
	public Transaction pickupTransaction() {
		Transaction result = new Transaction(transaction);
		transaction.setPickedUp(true);
		return result;
	}

	/**
	 * Start a Transmit command transaction
	 * 
	 * @param command
	 *            - transaction containing the command. This should be created
	 *            with the transactionFactory.
	 */
	@Override
	public void startTransaction(Transaction command) {

		transaction = command;
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.WAIT_FOR_COMMAND);
		execute();
	}

	/**
	 * Starts a Receive command transaction.
	 */
	@Override
	public void startTransaction() {
		transaction.setState(TransactionStateNames.READ_COMMAND);
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
	 * @see {@link ConnectionFactory#getConnection()}
	 */
	@Override
	public void startup() {
		transaction = transactionFactory.createTransaction(null);
		transaction.setPosted(true);
		if (connectionFactory.getParams().isListener()) {
			transaction.setState(TransactionStateNames.START_LISTENING);
			log.debug("Start listening");
			connectionFactory.startListener();
		} else {
			transaction.setState(TransactionStateNames.OPENING_CONNECTION);
			log.debug("Starting connection");
			connectionFactory.openConnection();
		}
		if (archive.isAlive() == false) {
			archive.start();
		}
	}

	@Override
	public void shutdown() {
		transaction = new ExitTransaction();
		transaction.setDirection(transactionFactory.getDirection());
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		log.info("Closing connection");
		Connection connection = connectionFactory.getConnection();
		if (connection != null) {
			connectionFactory.closeConnection(connection);
		}
		log.info("Shutting down network logger");
		archive.logTransaction(transaction);
	}
}
