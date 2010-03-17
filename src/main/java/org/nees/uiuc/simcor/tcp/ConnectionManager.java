package org.nees.uiuc.simcor.tcp;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionStatus;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;

public class ConnectionManager {

	private Connection connection;
	private final Logger log = Logger.getLogger(ConnectionManager.class);
	private TcpParameters params;
	private TcpError savedError = new TcpError();

	public TcpError checkForErrors() {
		TcpError result = new TcpError();
		if (connection != null) {
			result = connection.getFromRemoteMsg().getError();
		}
		return result;
	}

	public TcpError checkForErrors(Connection c) {
		return c.getFromRemoteMsg().getError();
	}
	public void clearError() {
		savedError = new TcpError();
	}

	public boolean closeConnection() {
		boolean result = closeConnection(connection);
		if(result) {
			connection = null;
		}
		return result;
	}

	private boolean closeConnection(Connection c) {
		if (c == null) {
			return true;
		}
		if (c.getState().equals(ConnectionStatus.BUSY) == false && c.isAlive()) {
			TcpActionsDto cmd = new TcpActionsDto();
			cmd.setAction(ActionsType.CLOSE);
			c.setToRemoteMsg(cmd);
		}
		return c.getConnectionState().equals(ConnectionStatus.CLOSED) || (c.isAlive() == false);
	}

	public boolean deleteConnectionHandle(Connection c) {
		if (c.getState().equals(ConnectionStatus.CLOSED) == false) {
			log.error("Connection is still" + c.getRemoteHost() + " is still"
					+ c.getState());
			return false;
		}
		
		return c.isAlive() == false;
	}

	/**
	 * 
	 * @return For connections to a remote host, returns the connection. For
	 *         connections that listen on a local port, returns a connection
	 *         request or null.
	 * @throws Exception
	 */
	public Connection getConnection()  {
		return connection;
	}

	public TcpParameters getParams() {
		return params;
	}

	public TcpError getSavedError() {
		return savedError;
	}

	public void openConnection() {
		connection = new Connection();
		try {
			connection.setParams(params);
			connection.start();
			Thread.sleep(300); // Give the connection time to start
		} catch (Exception e) {
			log.error("Open Connection failed for " + connection.getRemoteHost(),e);
		}
		TcpActionsDto cmd = new TcpActionsDto();
		cmd.setAction(ActionsType.CONNECT);
		connection.setToRemoteMsg(cmd);
	}

	public void saveError() {
		savedError = checkForErrors();
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

}
