package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;

public class TcpListen {
	private final Logger log = Logger.getLogger(TcpListen.class);
	private List<TcpLinkDto> socketList = new ArrayList<TcpLinkDto>();
	private TcpParameters params;
	private boolean running = false;
	private ServerSocket server = null;
	private TcpError error = new TcpError();


	public TcpError getError() {
		return error;
	}

	public void setError(TcpError error) {
		this.error = error;
	}

	public List<TcpLinkDto> getSocketList() {
		return socketList;
	}

	public boolean isRunning() {
		return running;
	}

	public TcpListen() {
		super();
	}

	public TcpListen(TcpParameters p) {
		super();
		this.params = p;
	}

	public synchronized TcpParameters getParams() {
		return params;
	}

	public boolean listen() {
		if (running == false) {
			try {
				server = new ServerSocket(params.getLocalPort());
				server.setSoTimeout(params.getTcpTimeout());
				server.setReuseAddress(true);
				running = true;
			} catch (IOException e) {
				log.error("Listening at " + ":" + params.getLocalPort()
						+ " failed because", e);
				String msg = "Listen " + ":" + params.getLocalPort()
						+ " returned an I/O error";
				error = new TcpError();
				error.setText(msg);
				error.setType(TcpErrorTypes.IO_ERROR);
				return false;
			}
		}

		Socket clientSocket = null;
		try {
			clientSocket = server.accept();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				log.debug("socket accept timed out");
				return false;
			}
			log.error("Accept " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Host " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return false;
		}
		TcpLinkDto clientConnection = new TcpLinkDto();
		clientConnection.setSocket(clientSocket);
		clientConnection.extractRemoteHost();
		log.info("Accepting connection from "
				+ clientConnection.getRemoteHost());
		addClient(clientConnection);
		return true;
	}

	public boolean stopListening() {
		try {
			server.close();
		} catch (IOException e) {
			log.error("Listener close " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Host " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
		}
		running = false;
		log.info("Listener signing off");
		return true;
	}

	public synchronized void setParams(TcpParameters params) throws Exception {
		if (running) {
			Exception e = new Exception("Listener is already running");
			log.error(e);
			throw e;
		}
		this.params = params;
	}

	public synchronized void addClient(TcpLinkDto client) {
		socketList.add(client);
	}

	public synchronized List<TcpLinkDto> flushList() {
		List<TcpLinkDto> result = new ArrayList<TcpLinkDto>();
		result.addAll(socketList);
		socketList.clear();
		return result;
	}
}
