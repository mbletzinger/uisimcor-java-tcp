package org.nees.uiuc.simcor.transaction;

import org.nees.uiuc.simcor.tcp.TcpError;

public class Transaction {
	public enum ActionType {
		CONNECT, DISCONNECT, NONE, START_TRANSACTION
	};

	public enum DirectionType {
		NONE, RECEIVE_COMMAND, SEND_COMMAND
	};

	public enum TransactionStateNames {
		ASSEMBLE_OPEN_RESPONSE, CHECK_OPEN_CONNECTION, CLOSING_CONNECTION, COMMAND_AVAILABLE, OPENING_CONNECTION, READ_COMMAND, READ_RESPONSE, READY, 
		RESPONSE_AVAILABLE, SEND_CLOSE_SESSION, SEND_OPEN_SESSION, SEND_OPEN_SESSION_RESPONSE, SENDING_COMMAND, SENDING_RESPONSE, SETUP_COMMAND, SETUP_RESPONSE, 
		START_LISTENING, STOP_LISTENING, TRANSACTION_DONE,WAIT_FOR_COMMAND, WAIT_FOR_OPEN_SESSION, WAIT_FOR_RESPONSE
	}

	private ActionType action = ActionType.NONE;

	private SimCorMsg command = null;
	private DirectionType direction = DirectionType.NONE;
	private TcpError error;
	// id is null i not used
	private TransactionIdentity id;
	private boolean pickedUp = false;
	private boolean posted = false;
	private SimCorMsg response = null;
	private TransactionStateNames state = TransactionStateNames.READY;
	private int timeout = 3000;

	public Transaction() {
	}

	public Transaction(Transaction t) {
		action = t.action;
		direction = t.direction;
		error = new TcpError(t.error);
		if (t.id != null) {
			id = new TransactionIdentity(t.id);
		}
		pickedUp = t.pickedUp;
		posted = t.posted;
		state = t.state;
		if (t.command != null) {
			if (t.command instanceof SimCorCompoundMsg) {
				command = new SimCorCompoundMsg((SimCorCompoundMsg) t.command);
			} else {
				command = new SimCorMsg(t.command);
			}
		}
		if (t.response != null) {
			if (t.response instanceof SimCorCompoundMsg) {
				response = new SimCorCompoundMsg((SimCorCompoundMsg) t.response);
			} else {
				response = new SimCorMsg(t.response);
			}
		}
	}

	public ActionType getAction() {
		return action;
	}

	public SimCorMsg getCommand() {
		return command;
	}

	public DirectionType getDirection() {
		return direction;
	}

	public TcpError getError() {
		return error;
	}

	public TransactionIdentity getId() {
		return id;
	}

	public SimCorMsg getResponse() {
		return response;
	}

	public TransactionStateNames getState() {
		return state;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isPickedUp() {
		return pickedUp;
	}

	public boolean isPosted() {
		return posted;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	public void setCommand(SimCorMsg command) {
		this.command = command;
	}

	public void setDirection(DirectionType direction) {
		this.direction = direction;
	}

	public void setError(TcpError error) {
		this.error = error;
	}

	public void setId(TransactionIdentity id) {
		this.id = id;
	}

	public void setPickedUp(boolean pickedUp) {
		this.pickedUp = pickedUp;
	}

	public void setPosted(boolean posted) {
		this.posted = posted;
	}

	public void setResponse(SimCorMsg response) {
		this.response = response;
	}

	public void setState(TransactionStateNames status) {
		this.state = status;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		String result = "/state=" + state + "/dir=" + direction + "/pickedUp="
				+ pickedUp + "/posted=" + posted + "\n";
		result += "/transId=" + id + "/error=" + error + "\n";
		result += "/command=" + command;
		result += "/response=" + response;
		return result;
	}
}
