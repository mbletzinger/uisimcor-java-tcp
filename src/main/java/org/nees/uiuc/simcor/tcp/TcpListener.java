package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.tcp.TcpListenerDto.TcpListenerState;

public class TcpListener extends Thread {
	private TcpListenerDto dto = new TcpListenerDto();
	private boolean shutdown = false;
	private final Logger log = Logger.getLogger(TcpListener.class);
	private List<TcpLinkDto> socketList = new ArrayList<TcpLinkDto>();
	private TcpParameters params;
	private boolean running = false;

	public TcpListener() {
		super();
	}

	public TcpListener(TcpParameters p) {
		super();
		this.params = p;
	}

	public  synchronized TcpListenerDto getDto() {
		TcpListenerDto result =  new TcpListenerDto(dto);
		return result;
	}

	public synchronized TcpParameters getParams() {
		return params;
	}

	@Override
	public void run() {
		ServerSocket server = null;
		TcpListenerDto myDto = getDto();
		running = true;
		try {
			server = new ServerSocket(params.getLocalPort());
			server.setSoTimeout(params.getTcpTimeout());
			server.setReuseAddress(true);
		} catch (IOException e) {
			log.error("Listening at " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Listen " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			myDto.setError(error);
			myDto.setListenerState(TcpListenerState.RUNNING);
			setDto(myDto);
			return;
		}
		myDto.setListenerState(TcpListenerState.RUNNING);
		setDto(myDto);
		while (myDto.getListenerState() != TcpListenerState.STOPPED) {

			Socket clientSocket = null;
			if (isShutdown()) {
				myDto.setListenerState(TcpListenerState.STOPPED);
				setDto(myDto);
				continue;
			}
			try {
				clientSocket = server.accept();
			} catch (IOException e) {
				if (e instanceof SocketTimeoutException) {
					log.debug("socket accept timed out");
					continue;
				}
				log.error("Accept " + ":" + params.getLocalPort()
						+ " failed because", e);
				String msg = "Host " + ":" + params.getLocalPort()
						+ " returned an I/O error";
				TcpError error = new TcpError();
				error.setText(msg);
				error.setType(TcpErrorTypes.IO_ERROR);
				myDto.setError(error);
				myDto.setListenerState(TcpListenerState.STOPPED);
				setDto(myDto);
				return;
			}
			TcpLinkDto clientConnection = new TcpLinkDto();
			clientConnection.setSocket(clientSocket);
			clientConnection.extractRemoteHost();
			log.info("Accepting connection from "
					+ clientConnection.getRemoteHost());
			addClient(clientConnection);
			myDto.setListenerState(TcpListenerState.NEW_CLIENTS);
			setDto(myDto);
		}
		try {
			server.close();
		} catch (IOException e) {
			log.error("Listener close " + ":" + params.getLocalPort()
					+ " failed because", e);
			String msg = "Host " + ":" + params.getLocalPort()
					+ " returned an I/O error";
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			myDto.setError(error);
			myDto.setListenerState(TcpListenerState.STOPPED);
			setDto(myDto);
		}
		running = false;
		log.info("Listener signing off");
	}

	public synchronized boolean isShutdown() {
		return shutdown;
	}

	public synchronized void setShutdown(boolean shutdown) {
		log.debug("Shutdown is set to " + shutdown);
		this.shutdown = shutdown;
	}

	public synchronized void setDto(TcpListenerDto dto) {
		this.dto = dto;
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
