package org.nees.uiuc.simcor.test.util;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.factories.ListenerConnectionFactory;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.transaction.Address;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;

public class RemoteConnection extends Thread {
	public enum RemoteConnectionStatus {
		LISTENING, READING, STOPPED, WRITING
	}
	private Connection connection;

	private TransactionIdentity id;

	private ListenerConnectionFactory listener;

	private final Logger log = Logger.getLogger(RemoteConnection.class);

	private String message;

	private TcpParameters params;

	private boolean running = false;

	private RemoteConnectionStatus status = RemoteConnectionStatus.STOPPED;

	public RemoteConnection(TcpParameters params) {
		super();
		this.params = params;
	}

	/**
	 * @return the params
	 */
	public synchronized TcpParameters getParams() {
		return params;
	}

	/**
	 * @return the status
	 */
	public synchronized RemoteConnectionStatus getStatus() {
		return status;
	}
	/**
	 * @return the running
	 */
	public synchronized boolean isRunning() {
		return running;
	}
	private RemoteConnectionStatus readMessage() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.READ);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on read");
			}
			count++;
		}
		ConnectionStatus stat = connection.getConnectionStatus();
		dto = connection.getFromRemoteMsg();

		if (stat.equals(ConnectionStatus.IN_ERROR)) {
			log.error("Read has an error" + dto.getError());
			return shutdownConnection();
		}
		message = dto.getMsg().assemble();
		id = dto.getMsg().getId();
		return RemoteConnectionStatus.WRITING;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		listener = new ListenerConnectionFactory();
		listener.setParams(params);
		running = true;
		status = RemoteConnectionStatus.LISTENING;
		listener.startListener();
		log.debug("Starting");
		while (status.equals(RemoteConnectionStatus.STOPPED) == false) {
			if (running == false) {
				if(status.equals(RemoteConnectionStatus.LISTENING) ==false) {
					log.debug("Disconnecting");
					shutdownConnection();
				}
				log.debug("Stopping Listener");
				shutdownListener();
				status = RemoteConnectionStatus.STOPPED;
				continue;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			if (status.equals(RemoteConnectionStatus.LISTENING)) {
				log.debug("Checking for a connection");
				connection = listener.checkForListenerConnection();
				if (connection == null) {
					continue;
				}
				status = RemoteConnectionStatus.READING;
				continue;
			}
			if(status.equals(RemoteConnectionStatus.READING)) {
				log.debug("Reading");
				status = readMessage();
				continue;
			}
			if(status.equals(RemoteConnectionStatus.WRITING)) {
				log.debug("Writing");
				status = writeMessage();
				continue;
			}
		}
		log.info("Remote Connection Stopped");
	}
	/**
	 * @param params
	 *            the params to set
	 */
	public synchronized void setParams(TcpParameters params) {
		this.params = params;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public synchronized void setRunning(boolean running) {
		this.running = running;
	};

	private RemoteConnectionStatus shutdownConnection() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.EXIT);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusyOrErrored()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on disconnect");
			}
			count++;
		}
		return RemoteConnectionStatus.LISTENING;
	}

	private void shutdownListener() {
		int count = 1;
		while (listener.stopListener() == false) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on listener shutdown");
			}
			count++;
		}
	}

	private RemoteConnectionStatus writeMessage() {
		TcpActionsDto dto = new TcpActionsDto();
		dto.setAction(ActionsType.WRITE);
		Msg2Tcp m2t = new Msg2Tcp();
		m2t.setId(id);
		SimCorMsg msg = new SimCorMsg();
		msg.setContent("\"" + message + "\" was sent");
		msg.setAddress(new Address("MDL-Response"));
		m2t.setMsg(msg);
		dto.setMsg(m2t);
		connection.setToRemoteMsg(dto);
		int count = 1;
		while (connection.isBusy()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
			if (count == 70) {
				count = 0;
				log.debug("Still waiting on write");
			}
			count++;
		}
		ConnectionStatus stat = connection.getConnectionStatus();

		if (stat.equals(ConnectionStatus.IN_ERROR)) {
			log.error("Read has an error" + dto.getError());
			return shutdownConnection();
		}
		message = dto.getMsg().assemble();
		return RemoteConnectionStatus.READING;
	}

}
