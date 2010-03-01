package org.nees.uiuc.simcor.tcp;

import java.util.ArrayList;
import java.util.List;


public class TcpListenerDto extends TcpActionsDto {

	public enum TcpListenerState {NEW_CLIENTS,RUNNING,STOPPED }
	private TcpListenerState listenerState;
	public TcpListenerDto() {
		super();
	}
	public TcpListenerDto(TcpListenerDto dto) {
		super(dto);
		listenerState = dto.listenerState;
	}
	public TcpListenerState getListenerState() {
		return listenerState;
	}
	public void setListenerState(TcpListenerState listenerState) {
		this.listenerState = listenerState;
	}
	@Override
	public String toString() {
		String result =  super.toString();
		result += "/state=" + listenerState;
		return result;
	}
	
}
