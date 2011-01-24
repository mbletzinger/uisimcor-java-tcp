package org.nees.uiuc.simcor.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.matlab.StringListUtils;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;

public class ReadTcpAction {
	public enum TcpReadStatus {
		DONE, STILL_READING, ERRORED
	};

	public ReadTcpAction(TcpLinkDto link) {
		super();
		this.link = link;
		reset();
	}
	private int msgTimeout;

	private byte[] eom = new byte[2];

	private TcpError error;

	private final TcpLinkDto link;
	private final StringListUtils slu = new StringListUtils();

	private final Logger log = Logger.getLogger(ReadTcpAction.class);

	private Msg2Tcp message;

	InputStream in = null;
	StringBuffer soFar;
	byte[] buf;
	byte crByte;
	boolean stillReading;
	long start;

	public TcpReadStatus readMessage() {
		if (stillReading == false) {
			TcpReadStatus strt = startReading();
			if (strt.equals(TcpReadStatus.ERRORED)) {
				reset();
				return strt;
			}
		}

		// collect all the bytes waiting on the input stream
		int avail = availableBytes();
		if(avail < 0) {
			reset();
			return TcpReadStatus.ERRORED;
		}

		while (avail > 0) {
			TcpReadStatus result = readBytes(avail);
			if(result.equals(TcpReadStatus.STILL_READING) == false) {
				return result;
			}
			avail = availableBytes();
			if(avail < 0) {
				reset();
				return TcpReadStatus.ERRORED;
			}
		}
		TcpReadStatus result = checkTimeOut();
		return result;
	}

	private TcpReadStatus checkTimeOut() {
		long time = (System.currentTimeMillis() - start);
		error = new TcpError();
		message = new Msg2Tcp();
		if (time > msgTimeout) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " timed out > "
					+ msgTimeout;
			log.error(msg);
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return TcpReadStatus.ERRORED;
		} else {
			error.setType(TcpErrorTypes.NONE);
			return TcpReadStatus.STILL_READING;
		}
	}

	private TcpReadStatus readBytes(int avail) {
		int amt = avail;
		if (amt > buf.length)
			amt = buf.length;

		try {
			amt = in.read(buf, 0, amt);
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			message = new Msg2Tcp();
			return TcpReadStatus.ERRORED;
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
				message = new Msg2Tcp();
				message.parse(soFar.substring(0, soFar.length())); // Make
																	// sure
																	// CR
																	// is
																	// gone
				log.debug("Received [" + soFar.toString() + "]");
				error = new TcpError();
				error.setType(TcpErrorTypes.NONE);
				return TcpReadStatus.DONE;
			}
			crByte = buf[amt - 1];
		}
		if (marker < amt) {
			// save all so far, still waiting for the final EOM
			soFar.append(new String(buf, marker, amt - marker));
		}
		return TcpReadStatus.STILL_READING;
	}

	private int availableBytes() {
		int result;
		try {
			result = in.available();
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return -1;
		}
		return result;
	}

	private TcpReadStatus startReading() {
		try {
			in = link.getSocket().getInputStream();
		} catch (IOException e) {
			String remoteHost = link.getRemoteHost();
			String msg = "Reading message from " + remoteHost + " failed";
			log.error(msg, e);
			error = new TcpError();
			error.setText(msg);
			error.setType(TcpErrorTypes.IO_ERROR);
			return TcpReadStatus.ERRORED;
		}
		soFar = new StringBuffer();
		buf = new byte[1024];
		crByte = 0;
		stillReading = true;
		// loop until message is completed
		start = System.currentTimeMillis();
		return TcpReadStatus.STILL_READING;
	}
	private void reset() {
		stillReading = false;
	}
}
