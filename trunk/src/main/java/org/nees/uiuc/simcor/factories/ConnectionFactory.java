package org.nees.uiuc.simcor.factories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.Connection;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpLinkDto;
import org.nees.uiuc.simcor.tcp.TcpListen;
import org.nees.uiuc.simcor.tcp.TcpListenerDto;
import org.nees.uiuc.simcor.tcp.TcpParameters;
import org.nees.uiuc.simcor.tcp.TcpListenerDto.TcpListenerState;

public class ConnectionFactory {

	private TcpListen listener;
	private final Logger log = Logger.getLogger(ConnectionFactory.class);
	private TcpParameters params;
	private TcpError savedError = new TcpError();

	public TcpError checkForErrors() {
		TcpError result = new TcpError();
		if (listener != null) {
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


	public TcpListen getListener() {
		return listener;
	}

	public TcpParameters getParams() {
		return params;
	}

	public TcpError getSavedError() {
		return savedError;
	}

	public void saveError() {
		savedError = checkForErrors();
	}

	public void setParams(TcpParameters params) {
		this.params = params;
	}

	public void startListener() {
		listener = new TcpListen();
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
