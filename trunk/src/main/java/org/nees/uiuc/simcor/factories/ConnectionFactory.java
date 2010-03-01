package org.nees.uiuc.simcor.tcp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection.ConnectionState;
import org.nees.uiuc.simcor.tcp.TcpActionsDto.ActionsType;
import org.nees.uiuc.simcor.tcp.TcpListenerDto.TcpListenerState;

public class ConnectionFactory {

	private Connection connection;
	private TcpListener listener;
	private final Logger log = Logger.getLogger(ConnectionFactory.class);
	private TcpParameters params;
	private TcpError savedError = new TcpError();

	public TcpError checkForErrors() {
		TcpError result = new TcpError();
		if (connection != null) {
			result = connection.getFromRemoteMsg().getError();
		} else if (listener != null) {
			result = listener.getDto().getError();
		}
		return result;
	}
	public TcpError checkForErrors(Connection c) {
		return c.getFromRemoteMsg().getError();
	}
	public Connection checkForListenerConnection() {
		TcpListenerDto serverDto = listener.getDto();
		TcpListenerState state = serverDto.getListenerState();
		if (state == TcpListenerState.STOPPED) {
			return null;
		}
		if (state == TcpListenerState.NEW_CLIENTS) {
			Connection c = new Connection(listener.flushList().get(0));
			try {
				c.setParams(params);
				c.start();
				Thread.sleep(300); // Give the connection time to start
			} catch (Exception e) {
				log.error("Check for Listener Connection " + c.getRemoteHost() + " failed",e);
			}
			connection = c;
			return c;
		}
		return null;
	}

	public List<Connection> checkForListenerConnections() {
		List<Connection> result = new ArrayList<Connection>();
		List<TcpLinkDto> sockets = listener.flushList();
		for (Iterator<TcpLinkDto> s = sockets.iterator(); s.hasNext();) {
			Connection cn = new Connection(s.next());
			try {
				cn.setParams(params);
			} catch (Exception e) {
				log.error("Check for Listener Connection " + cn.getRemoteHost() + " failed",e);
			}
			cn.start();
			result.add(cn);
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		} // Give the connection time to start
		return result;
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

	public boolean closeConnection(Connection c) {
		if (c == null) {
			return true;
		}
		if (c.getState().equals(ConnectionState.BUSY) == false && c.isAlive()) {
			TcpActionsDto cmd = new TcpActionsDto();
			cmd.setAction(ActionsType.CLOSE);
			c.setToRemoteMsg(cmd);
		}
		return c.getConnectionState().equals(ConnectionState.CLOSED) || (c.isAlive() == false);
	}

	public boolean deleteConnectionHandle(Connection c) {
		if (c.getState().equals(ConnectionState.CLOSED) == false) {
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

	public TcpListener getListener() {
		return listener;
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

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	public void startListener() {
		listener = new TcpListener();
		TcpListenerDto serverDto = new TcpListenerDto();
		try {
			listener.setParams(params);
			listener.start();
			Thread.sleep(300); // Give the lisstener time to start
		} catch (Exception e) {
			log.error("Start Listener failed",e);
		}
	}

	public boolean stopListener() {
		listener.setShutdown(true);
		return listener.isAlive() == false;
	}
}
