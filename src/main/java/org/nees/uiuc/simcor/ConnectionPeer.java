package org.nees.uiuc.simcor;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.factories.TransactionFactory;
import org.nees.uiuc.simcor.logging.ExitTransaction;
import org.nees.uiuc.simcor.states.TransactionStateNames;
import org.nees.uiuc.simcor.states.listener.ClosingConnection;
import org.nees.uiuc.simcor.states.listener.ReadResponse;
import org.nees.uiuc.simcor.states.listener.SendingCommand;
import org.nees.uiuc.simcor.states.listener.SendingResponse;
import org.nees.uiuc.simcor.states.listener.StartListening;
import org.nees.uiuc.simcor.states.listener.StopListening;
import org.nees.uiuc.simcor.states.listener.WaitForResponse;
import org.nees.uiuc.simcor.states.old.CommandAvailable;
import org.nees.uiuc.simcor.states.old.OpeningConnection;
import org.nees.uiuc.simcor.states.old.ReadCommand;
import org.nees.uiuc.simcor.states.old.Ready;
import org.nees.uiuc.simcor.states.old.ReceiveCommandWaitForCommand;
import org.nees.uiuc.simcor.states.old.ReceiveCommandWaitForResponse;
import org.nees.uiuc.simcor.states.old.ResponseAvailable;
import org.nees.uiuc.simcor.states.old.SetUpCommand;
import org.nees.uiuc.simcor.states.old.TransactionDone;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.SimpleTransaction.DirectionType;

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
	public ConnectionPeer(DirectionType dir,String mdl) {
		initialize(dir, mdl);
	}

	public ConnectionPeer(String dirS,String mdl) {
		initialize(DirectionType.valueOf(dirS), mdl);
	}

	@Override
	protected void initialize(DirectionType dir,String mdl) {
		if (dir == DirectionType.RECEIVE_COMMAND) {
			machine.put(TransactionStateNames.START_LISTENING,
					new StartListening(sap));
			machine.put(TransactionStateNames.STOP_LISTENER,
					new StopListening(sap));
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new ReceiveCommandWaitForCommand(sap));
			machine.put(TransactionStateNames.COMMAND_AVAILABLE,
					new CommandAvailable(sap));
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
					new ReceiveCommandWaitForResponse(sap));
			machine.put(TransactionStateNames.SENDING_RESPONSE,
					new SendingResponse(sap));
		} else {
			machine.put(TransactionStateNames.WAIT_FOR_COMMAND,
					new SetUpCommand(sap));
			machine.put(TransactionStateNames.SENDING_COMMAND,
					new SendingCommand(sap));
			machine.put(TransactionStateNames.WAIT_FOR_RESPONSE,
					new SetupReadMessage(sap));
			machine.put(TransactionStateNames.RESPONSE_AVAILABLE,
					new ResponseAvailable(sap));

		}
		machine.put(TransactionStateNames.TRANSACTION_DONE,
				new TransactionDone(sap));
		machine.put(TransactionStateNames.READY, new Ready(sap));
		machine.put(TransactionStateNames.OPENING_CONNECTION,
				new OpeningConnection(sap));
		machine.put(TransactionStateNames.CLOSING_CONNECTION,
				new ClosingConnection(sap));
		machine.put(TransactionStateNames.READ_COMMAND,
				new ReadCommand(sap));
		machine.put(TransactionStateNames.READ_RESPONSE,
				new WaitForOpenResponse(sap));
		transaction = sap.getTf().createSimpleTransaction(null);
		transaction.setState(TransactionStateNames.TRANSACTION_DONE);
		sap.getTf().setMdl(mdl);

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
	public SimpleTransaction pickupTransaction() {
		SimpleTransaction result = new SimpleTransaction(transaction);
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
	public void startTransaction(SimpleTransaction command) {

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
	 * @see {@link ListenerConnectionFactory#getConnection()}
	 */
	@Override
	public void startup(TcpParameters params) {
		TransactionFactory transactionFactory = sap.getTf();
		if(params.isListener()) {
			transactionFactory.setDirection(DirectionType.RECEIVE_COMMAND);
		} else {
			transactionFactory.setDirection(DirectionType.SEND_COMMAND);			
		}
		transaction = transactionFactory.createSimpleTransaction(null);
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


	@Override
	public void shutdown() {
		TransactionFactory transactionFactory = sap.getTf();
		transaction = new ExitTransaction();
		transaction.setDirection(transactionFactory.getDirection());
		transaction.setPosted(true);
		transaction.setState(TransactionStateNames.CLOSING_CONNECTION);
		log.info("Closing connection");
//		Connection connection = connectionManager.getConnection();
//		if (connection != null) {
//			connectionManager.closeConnection();
//		}
		log.info("Shutting down network logger");
		archive.logTransaction(transaction);
	}
}
