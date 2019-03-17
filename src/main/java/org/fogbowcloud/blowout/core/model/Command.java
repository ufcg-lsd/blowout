package org.fogbowcloud.blowout.core.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Command implements Serializable {
	private static final Logger LOGGER = Logger.getLogger(Command.class);
	private static final long serialVersionUID = 5281647552435522413L;

	public enum Type {
		LOCAL, REMOTE, EPILOGUE
	}

	public enum State {
		QUEUED, RUNNING, FINISHED, FAILED
	}

	private final String command;
	private final Type type;
	private State state;

	public Command(String command, Type type, State state) {
		this.command = command;
		this.type = type;
		this.state = state;
	}

	public Command(String command, Type type) {
		this(command, type, State.QUEUED);
	}

	public Type getType() {
		return type;
	}

	public String getCommand() {
		return command;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public State getState() {
		return this.state;
	}

	public Command clone() {
		return new Command(this.command, this.type, this.state);
	}

	public JSONObject toJSON() {
		try {
			JSONObject command = new JSONObject();
			command.put("command", this.getCommand());
			command.put("type", this.getType().toString());
			command.put("state", this.getState().toString());
			return command;
		} catch (JSONException e) {
			LOGGER.debug("Error while trying to create a JSON from command", e);
			return null;
		}
	}

	public static Command fromJSON(JSONObject commandJSON) {
		Command command = new Command(commandJSON.optString("command"), 
				Type.valueOf(commandJSON.optString("type")));
		command.setState(State.valueOf(commandJSON.optString("state")));
		return command;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Command command1 = (Command) o;
		return command.equals(command1.command) &&
				state == command1.state;
	}

	@Override
	public int hashCode() {
		return Objects.hash(command, state);
	}
}