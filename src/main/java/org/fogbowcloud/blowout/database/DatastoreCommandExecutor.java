package org.fogbowcloud.blowout.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;

public class DatastoreCommandExecutor {

	private static final Logger LOGGER = Logger.getLogger(FogbowResourceDatastore.class);
	//retornar o statement?
	public void executeSQLCommand(final String datastoreUrl, final String sqlCommand)
			throws SQLException {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = this.getConnection(datastoreUrl);
			connection.setAutoCommit(false);

			statement = connection.createStatement();
			statement.execute(sqlCommand);
			connection.commit();
		} catch (SQLException e) {
			LOGGER.error("Error while trying to execute SQL command into Datastore.");
			throw e;
		} finally {
			this.close(statement, connection);
		}
	}

	public boolean executeSQLCommandWithStatements(final String datastoreUrl,
			final String SQLCommand, final String... commandStatements) throws SQLException {
		Connection connection = null;
		PreparedStatement statements = null;
		try {
			connection = this.getConnection(datastoreUrl);
			connection.setAutoCommit(false);

			statements = this.prepare(connection, SQLCommand);
			
			this.setStatements(statements, commandStatements);

			boolean result = statements.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error(
					"Error while trying to execute SQL command with statements into Datastore");
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException eRollBack) {
				LOGGER.error("Couldn't rollback transaction.", eRollBack);
			}
			throw e;
		} finally {
			this.close(statements, connection);
		}
	}

	protected PreparedStatement prepare(Connection connection, String statement)
			throws SQLException {
		return connection.prepareStatement(statement);
	}

	public Connection getConnection(final String datastoreUrl) throws SQLException {
		try {
			return DriverManager.getConnection(datastoreUrl);
		} catch (SQLException e) {
			LOGGER.error("Error while getting a new connection from the connection pool.", e);
			throw e;
		}
	}

	private void close(Statement statement, Connection connection) {
		if (statement != null) {
			try {
				if (!statement.isClosed()) {
					statement.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close statement of Fogbow Resource Datastore", e);
			}
		}
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close connection of Fogbow Resource Datastore", e);
			}
		}
	}
	
	private void setStatements(PreparedStatement statements, String[] commandStatements)
			throws SQLException {
		for (int i = 0; i < commandStatements.length; i++) {
			String commandStatement = commandStatements[i];
			if (commandStatement == null) {
				statements.setNull(i + 1, Types.VARCHAR);
			} else {
				statements.setString(i + 1, commandStatement);
			}
		}
	}
}
