package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.matlab.StringListUtils;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class TcpActions {

	private byte[] eom = new byte[2];
	private TcpLinkDto link = new TcpLinkDto();
	private final Logger log = Logger.getLogger(TcpActions.class);
	private TcpParameters parameters;
	private final StringListUtils slu = new StringListUtils();

	public TcpActions() {
		super();
		String endOfMsgS = "\r\n";
		eom = endOfMsgS.getBytes();
		log.debug("eom is [" + eom + "]");
		if (log.isDebugEnabled()) {
			String str = slu.Byte2HexString(eom);
			log.debug("EOM[" + str + "] length " + eom.length);
		}

	}

	public TcpActionsDto closeConnection() {
		TcpActionsDto result = new TcpActionsDto();
		Msg2Tcp message = new Msg2Tcp();
		result.setMsg(message);
		if (link.getSocket() == null) {
			TcpError error = new TcpError();
			error.setText("Connection does not exist");
			error.setType(TcpErrorTypes.UNKNOWN_REMOTE_HOST);
			log.error(error.getText());
			result.setError(error);
			return result;
		}
		java.net.Socket socket = link.getSocket();
		String remoteHost = link.getRemoteHost();
		try {
			socket.close();
		} catch (IOException e) {
			String msg = "Closing connection to " + remoteHost + " failed";
			log.error(msg, e);
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
		}
		TcpError error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		result.setError(error);
		return result;
	}

	public TcpActionsDto connect() {
		TcpActionsDto result = new TcpActionsDto();
		Msg2Tcp message = new Msg2Tcp();
		result.setMsg(message);
		Socket socket = null;
		try {
			socket = new Socket(parameters.getRemoteHost(), parameters
					.getRemotePort());
		} catch (UnknownHostException e) {
			String emsg = "Unknown host " + parameters.getRemoteHost() + ":"
					+ parameters.getRemotePort();
			log.error(emsg, e);
			TcpError error = new TcpError();
			error.setText(emsg);
			error.setType(TcpErrorTypes.UNKNOWN_REMOTE_HOST);
			result.setError(error);
			return result;

		} catch (IOException e) {
			String emsg = "Host " + parameters.getRemoteHost() + ":"
					+ parameters.getRemotePort() + " returned an I/O error";
			log.error(emsg, e);
			TcpError error = new TcpError();
			error.setText(emsg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
			return result;
		}
		link.setSocket(socket);
		link.setRemoteHost(parameters.getRemoteHost());
		log.debug("Connect to Host " + parameters.getRemoteHost() + ":"
				+ parameters.getRemotePort() + " successful");
		TcpError error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		result.setError(error);
		return result;
	}

	public TcpLinkDto getLink() {
		return link;
	}

	public TcpParameters getParameters() {
		return parameters;
	}

	public TcpActionsDto readMessage(int msgTimeout) {
		TcpActionsDto result = new TcpActionsDto();
		String remoteHost = link.getRemoteHost();
		InputStream in = null;
		try {
			in = link.getSocket().getInputStream();
		} catch (IOException e) {
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
			return result;
		}

		StringBuffer soFar = new StringBuffer();
		byte[] buf = new byte[1024];
		byte crByte = 0;
		boolean stillReading = true;
		// loop until message is completed
		long start = System.currentTimeMillis();
		while (stillReading) {
			// play nice with the other threads and surrender the CPU
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				log.debug("Read response was interrupted", e);
			}
			// collect all the bytes waiting on the input stream
			int avail = 0;
			try {
				avail = in.available();
			} catch (IOException e) {
				String msg = "Reading message from " + remoteHost + " failed";
				log.error(msg, e);
				TcpError error = new TcpError();
				error.setText(msg);
				error.setType(TcpErrorTypes.IO_ERROR);
				result.setError(error);
				Msg2Tcp message = new Msg2Tcp();
				result.setMsg(message);
				return result;
			}

			while (avail > 0) {
				int amt = avail;
				if (amt > buf.length)
					amt = buf.length;

				try {
					amt = in.read(buf, 0, amt);
				} catch (IOException e) {
					String msg = "Reading message from " + remoteHost
							+ " failed";
					log.error(msg, e);
					TcpError error = new TcpError();
					error.setText(msg);
					error.setType(TcpErrorTypes.IO_ERROR);
					result.setError(error);
					Msg2Tcp message = new Msg2Tcp();
					result.setMsg(message);
					return result;
				}
				log.debug("READ: [" + slu.Byte2HexString(buf) + "]");
				int marker = 0;
				for (int i = 0; i < amt; i++) {
					// scan for the CRLF characters which delineate messages
					if (buf[i] == eom[1]) {
						marker = i - 1;
						log.debug("found CR at " + i);
						if (i > 0) {
							crByte = buf[i - 1];
						}
						if (crByte != eom[0]) {
							marker = i;
						}
						String tmp = new String(buf, 0, marker);
						log.debug("found chars " + tmp);
						soFar.append(tmp);
						Msg2Tcp message = new Msg2Tcp();
						message.parse(soFar.substring(0, soFar.length())); // Make
																			// sure
																			// CR
																			// is
																			// gone
						result.setMsg(message);
						log.debug("Received [" + soFar.toString() + "]");
						TcpError error = new TcpError();
						error.setType(TcpErrorTypes.NONE);
						result.setError(error);
						return result;
					}
					crByte = buf[amt - 1];
				}
				if (marker < amt) {
					// save all so far, still waiting for the final EOM
					soFar.append(new String(buf, marker, amt - marker));
				}
				try {
					avail = in.available();
				} catch (IOException e) {
					String msg = "Reading message from " + remoteHost
							+ " failed";
					log.error(msg, e);
					TcpError error = new TcpError();
					error.setText(msg);
					error.setType(TcpErrorTypes.IO_ERROR);
					result.setError(error);
					Msg2Tcp message = new Msg2Tcp();
					result.setMsg(message);
					return result;
				}
			}
			long time = (System.currentTimeMillis() - start);
			if (time > msgTimeout) {
				String msg = "Reading message from " + remoteHost
						+ " timed out > " + msgTimeout;
				log.error(msg);
				TcpError error = new TcpError();
				error.setText(msg);
				error.setType(TcpErrorTypes.IO_ERROR);
				result.setError(error);
				Msg2Tcp message = new Msg2Tcp();
				result.setMsg(message);
				return result;
			} else {
				// log.debug("Timeout " + time + " < " +
				// parameters.getTcpTimeout());
			}

		}
		TcpError error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		result.setError(error);
		return result;
	}

	public TcpActionsDto sendMessage(TcpActionsDto state) {
		TcpActionsDto result = new TcpActionsDto();
		result.setMsg(state.getMsg());
		byte[] sendeom;
		if (parameters.isLfcrSendEom()) {
			sendeom = eom;
		} else {
			String endOfMsgS = "\n";
			sendeom = endOfMsgS.getBytes();
		}
		OutputStream out = null;
		String remoteHost = link.getRemoteHost();
		String outMsg = state.getMsg().assemble();
		if (link.getSocket() == null) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed socket was not initialized";
			log.error(msg);
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
			return result;

		}

		if (link.getSocket().isOutputShutdown() || link.getSocket().isClosed()
				|| link.getSocket().isInputShutdown()
				|| (link.getSocket().isConnected() == false)) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed socket is closed";
			log.error(msg);
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
			return result;

		}
		try {
			out = link.getSocket().getOutputStream();
			int lng = outMsg.length();
			byte[] mbuf = outMsg.getBytes();
			byte[] buf = new byte[lng + sendeom.length];

			for (int b = 0; b < lng; b++) {
				buf[b] = mbuf[b];
			}

			buf[lng] = sendeom[0];

			if (parameters.isLfcrSendEom()) {
				buf[lng + 1] = eom[1];
			}
			log.debug("WRITE: [" + slu.Byte2HexString(buf) + "]");
			out.write(buf);
			out.flush();
		} catch (Exception e) {
			String msg = "Send message [" + outMsg + "] to " + remoteHost
					+ " failed";
			log.error(msg, e);
			TcpError error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			result.setError(error);
			return result;
		}
		log.debug("Sent [" + outMsg + "]");
		TcpError error = new TcpError();
		error.setType(TcpErrorTypes.NONE);
		result.setError(error);
		return result;
	}

	public void setLink(TcpLinkDto link) {
		this.link = link;
	}

	public void setParameters(TcpParameters parameters) {
		this.parameters = parameters;
	}

}
