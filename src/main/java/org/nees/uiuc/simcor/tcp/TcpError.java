package org.nees.uiuc.simcor.tcp;


public class TcpError {

	public enum TcpErrorTypes {
		IO_ERROR,
		NONE,
		TIMEOUT,
		UNKNOWN_REMOTE_HOST,
		THREAD_DIED
	}

	private String text = "";
	private String remoteHost;

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	private TcpErrorTypes type = TcpErrorTypes.NONE;
	public TcpError(TcpError e) {
		type = e.type;
		text = new String(e.text);
	}

	public void clearError() {
		type = TcpErrorTypes.NONE;
		text = "";
	}

	public TcpError() {
		super();
	}

	public  String getText() {
		return text;
	}

	public  TcpErrorTypes getType() {
		return type;
	}

	public  void setText(String errorMsg) {
		this.text = errorMsg;
	}

	public  void setType(TcpErrorTypes error) {
		this.type = error;
	}

	@Override
	public String toString() {
		String result = "/type=" + type;
		if(text != null) {
			result += "/msg=" + text;
		}
		return result;
	}

}
