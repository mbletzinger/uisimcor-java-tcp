package org.nees.uiuc.simcor.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.tcp.TcpError.TcpErrorTypes;
import org.nees.uiuc.simcor.transaction.Msg2Tcp;
import org.nees.uiuc.simcor.transaction.SimCorMsg;
import org.nees.uiuc.simcor.transaction.SimpleTransaction;
import org.nees.uiuc.simcor.transaction.TransactionIdentity;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;
import org.nees.uiuc.simcor.transaction.Transaction.DirectionType;

public class TransactionLogRecord {
	SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss.SSS");
	private Logger log = Logger.getLogger(TransactionLogRecord.class);
	private String msgToString(SimCorMsg msg, TransactionIdentity id,
			String dstring) {
		if(msg == null) {
			return null;
		}
		if(msg.getType() == MsgType.ERROR) {
			return null;
		}
		Msg2Tcp m2t = new Msg2Tcp();
		m2t.setId(id);
		m2t.setMsg(msg);
		Date ts = msg.getTimestamp() != null ? msg.getTimestamp() : new Date();
		String result = format.format(ts);
		result += '\t' + dstring + "\t" + m2t.assemble();
		return result;
	}

	public String toString(SimpleTransaction t) {
		log.debug("Logging: " + t);
		String result;
		if (t.getDirection().equals(DirectionType.SEND_COMMAND)) {
			result = msgToString(t.getCommand(), t.getId(), "SENT") + "\n";
			result += msgToString(t.getResponse(), t.getId(), "RCVD") + "\n";
		} else {
			result = msgToString(t.getCommand(), t.getId(), "RCVD") + "\n";
			result += msgToString(t.getResponse(), t.getId(), "SENT") + "\n";
		}
		TcpError e = t.getError();
		if(e.getType().equals(TcpErrorTypes.NONE)) {
			return result;
		}
		result += "ERROR:\t"  + e.getText() + "\n";
		return result;
	}
}
