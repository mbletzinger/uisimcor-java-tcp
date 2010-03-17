/**
 * 
 */
package org.nees.uiuc.simcor.states;

public enum TransactionStateNames {
	ASSEMBLE_CLOSE_COMMAND, 
	ASSEMBLE_CLOSE_TRIGGER_COMMANDS, 
	ASSEMBLE_COMMAND, 
	ASSEMBLE_OPEN_COMMAND, 
	ASSEMBLE_OPEN_RESPONSE, 
	ASSEMBLE_RESPONSE, 
	ASSEMBLE_TRIGGER_COMMANDS, 
	BROADCAST_COMMAND, 
	CHECK_LISTENER_OPEN_CONNECTION, 
	CHECK_OPEN_CONNECTION, 
	CLOSE_TRIGGER_CONNECTIONS, 
	CLOSING_CONNECTION, 
	COLLECT_LOST_CLIENTS, 
	COLLECT_TRIGGER_CLIENTS, 
	COMMAND_AVAILABLE, 
	LISTEN_FOR_CONNECTIONS, 
	OPENING_CONNECTION, 
	READY, 
	RESPONSE_AVAILABLE, 
	SEND_CLOSE_TRIGGER_COMMANDS, 
	SENDING_COMMAND, 
	SENDING_RESPONSE,
	SETUP_READ_COMMAND, 
	SETUP_READ_OPEN_COMMAND, 
	SETUP_READ_RESPONSE,
	SETUP_TRIGGER_READ_RESPONSES, 
	START_LISTENER,
	START_LISTENING, 
	STOP_LISTENER, 
	TRANSACTION_DONE, 
	WAIT_FOR_COMMAND, 
	WAIT_FOR_OPEN_COMMAND, 
	WAIT_FOR_OPEN_RESPONSE,
	WAIT_FOR_RESPONSE,
	WAIT_FOR_RESPONSE_POSTING,
	WAIT_FOR_TRIGGER_RESPONSES
}