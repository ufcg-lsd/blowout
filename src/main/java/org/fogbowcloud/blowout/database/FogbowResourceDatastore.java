package org.fogbowcloud.blowout.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.json.JSONException;
import org.json.JSONObject;

public class FogbowResourceDatastore {

	private static final Logger LOGGER = Logger.getLogger(FogbowResourceDatastore.class);

	protected static final String FOGBOW_RESOURCE_TABLE_NAME = "fogbow_resource_store";

	protected static final String RESOURCE_ID = "resource_id";
	protected static final String ORDER_ID = "order_id";
	protected static final String INSTANCE_ID = "instance_id";
	protected static final String SPEC = "spec";

	protected static final String MANAGER_DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";
	protected static final String PREFIX_DATASTORE_URL = "jdbc:sqlite:";

	private static final String INSERT_FOGBOW_RESOURCE_SQL = "INSERT INTO "
			+ FOGBOW_RESOURCE_TABLE_NAME + " VALUES(?,?,?,?)";
	private static final String UPDATE_FOGBOW_RESOURCE = "UPDATE " + FOGBOW_RESOURCE_TABLE_NAME
			+ " SET " + ORDER_ID + "=? , " + INSTANCE_ID + "=? WHERE " + RESOURCE_ID + "=?";
	private static final String SELECT_REQUEST_ID = "SELECT * FROM " + FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_ALL_CONTENT_SQL = "DELETE FROM "
			+ FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_BY_RESOURCE_ID_SQL = DELETE_ALL_CONTENT_SQL + " WHERE "
			+ RESOURCE_ID + "=? ";

	private String dataStoreURL;
	private Properties properties;
	private DatastoreCommandExecutor datastoreCommandExecutor;

	public FogbowResourceDatastore(Properties properties) {
		this.properties = properties;
		this.dataStoreURL = this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL);
		this.datastoreCommandExecutor = new DatastoreCommandExecutor();

		LOGGER.debug("DatastoreURL: " + this.dataStoreURL);

		String SQLCommand = "CREATE TABLE IF NOT EXISTS " + FOGBOW_RESOURCE_TABLE_NAME + "("
				+ RESOURCE_ID + " VARCHAR(255) PRIMARY KEY," + ORDER_ID + " VARCHAR(255),"
				+ INSTANCE_ID + " VARCHAR(255)," + SPEC + " TEXT " + ")";

		try {
			this.datastoreCommandExecutor.executeSQLCommand(this.dataStoreURL, SQLCommand);
		} catch (SQLException e) {
			LOGGER.error("Error while trying to Create Fogbow Resource Table at Datastore.", e);
		}

		// Statement statement = null;
		// Connection connection = null;
		// try {
		// LOGGER.debug("DatastoreURL: " + this.dataStoreURL);
		//
		// connection = this.getConnection();
		// connection.setAutoCommit(false);
		// statement = connection.createStatement();
		// statement.execute("CREATE TABLE IF NOT EXISTS " + FOGBOW_RESOURCE_TABLE_NAME
		// + "("
		// + RESOURCE_ID + " VARCHAR(255) PRIMARY KEY," + ORDER_ID + " VARCHAR(255),"
		// + INSTANCE_ID + " VARCHAR(255)," + SPEC + " TEXT " + ")");
		// statement.close();
		// connection.commit();
		//
		// } catch (Exception e) {
		// LOGGER.error("Error while initializing the DataStore.", e);
		// } finally {
		// this.close(statement, connection);
		// }
	}

	public boolean addFogbowResource(FogbowResource fogbowResource) {
		LOGGER.debug("Adding resource id: " + fogbowResource.getId());

		String spec = null;
		if (fogbowResource.getRequestedSpec() != null) {
			JSONObject json = fogbowResource.getRequestedSpec().toJSON();
			spec = json.toString();
		}

		try {
			return this.datastoreCommandExecutor.executeSQLCommandWithStatements(this.dataStoreURL,
					INSERT_FOGBOW_RESOURCE_SQL, fogbowResource.getId(), fogbowResource.getOrderId(),
					fogbowResource.getInstanceId(), spec);
		} catch (SQLException e) {
			LOGGER.error("Error while trying to Insert a Fogbow resource at Datastore.", e);
			return false;
		}

		/*PreparedStatement insertResourceStatement = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);

			String spec = null;
			if (fogbowResource.getRequestedSpec() != null) {
				JSONObject json = fogbowResource.getRequestedSpec().toJSON();
				spec = json.toString();
			}

			insertResourceStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);
			insertResourceStatement.setString(1, fogbowResource.getId());
			insertResourceStatement.setString(2, fogbowResource.getOrderId());
			insertResourceStatement.setString(3, fogbowResource.getInstanceId());
			if (spec == null) {
				insertResourceStatement.setNull(4, Types.VARCHAR);
			} else {
				insertResourceStatement.setString(4, spec);
			}

			boolean result = insertResourceStatement.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't store the current resource id", e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			this.close(insertResourceStatement, connection);
		}*/
		 
	}

	public boolean addResourceIds(List<FogbowResource> fogbowResources) {
		LOGGER.debug("Adding resource ids");
		PreparedStatement insertResourcesStatement = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
			insertResourcesStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);
			for (FogbowResource fogbowResource : fogbowResources) {

				String spec = null;
				if (fogbowResource.getRequestedSpec() != null) {
					JSONObject json = fogbowResource.getRequestedSpec().toJSON();
					spec = json.toString();
				}

				insertResourcesStatement.setString(1, fogbowResource.getId());
				insertResourcesStatement.setString(2, fogbowResource.getOrderId());
				insertResourcesStatement.setString(3, fogbowResource.getInstanceId());
				if (spec == null) {
					insertResourcesStatement.setNull(4, Types.VARCHAR);
				} else {
					insertResourcesStatement.setString(4, spec);
				}
				insertResourcesStatement.addBatch();
				connection.rollback();
			}

			if (this.hasBatchExecutionError(insertResourcesStatement.executeBatch())) {
				return false;
			}
			connection.commit();
			return true;
		} catch (SQLException e) {
			LOGGER.error("Couldn't store the current resource id", e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			this.close(insertResourcesStatement, connection);
		}
	}

	public boolean updateFogbowResource(FogbowResource fogbowResource) {
		LOGGER.debug("Updating resource id: " + fogbowResource.getId());
		PreparedStatement updateFogbowResourceStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			updateFogbowResourceStatement = this.prepare(connection, UPDATE_FOGBOW_RESOURCE);
			updateFogbowResourceStatement.setString(1, fogbowResource.getOrderId());
			updateFogbowResourceStatement.setString(2, fogbowResource.getInstanceId());
			updateFogbowResourceStatement.setString(3, fogbowResource.getId());

			boolean result = false;
			if (updateFogbowResourceStatement.executeUpdate() > 0) {
				result = true;
			}

			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't update fogbow resource " + fogbowResource.getId(), e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			this.close(updateFogbowResourceStatement, connection);
		}
	}

	public Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(this.dataStoreURL);
		} catch (SQLException e) {
			LOGGER.error("Error while getting a new connection from the connection pool.", e);
			throw e;
		}
	}

	private void close(Statement statement, Connection conn) {
		if (statement != null) {
			try {
				if (!statement.isClosed()) {
					statement.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close statement of Fogbow Resource Datastore");
			}
		}

		if (conn != null) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close connection of Fogbow Resource Datastore");
			}
		}
	}

	protected PreparedStatement prepare(Connection connection, String statement)
			throws SQLException {
		return connection.prepareStatement(statement);
	}

	public List<FogbowResource> getAllFogbowResources() {
		List<FogbowResource> fogbowResources = new ArrayList<FogbowResource>();
		Statement getRequestIdStatement = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			getRequestIdStatement = connection.createStatement();
			getRequestIdStatement.execute(SELECT_REQUEST_ID);
			ResultSet result = getRequestIdStatement.getResultSet();

			while (result.next()) {
				FogbowResource fogbowresource = createFogbowResource(result);
				fogbowResources.add(fogbowresource);
			}

			return fogbowResources;
		} catch (Exception e) {
			LOGGER.error("Couldn't recover request Ids from DB", e);
			return null;
		} finally {
			this.close(getRequestIdStatement, connection);
		}
	}

	public boolean deleteFogbowResourceById(FogbowResource fogbowResource) {
		LOGGER.debug("Deleting resource id: " + fogbowResource.getId());

		PreparedStatement deleteResourceId = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);

			deleteResourceId = prepare(connection, DELETE_BY_RESOURCE_ID_SQL);
			deleteResourceId.setString(1, fogbowResource.getId());

			boolean result = deleteResourceId.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't delete the resource " + fogbowResource.getId(), e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			this.close(deleteResourceId, connection);
		}
	}

	public boolean deleteAll() {
		LOGGER.debug("Deleting all resources");

		PreparedStatement deleteOldContent = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);

			deleteOldContent = connection.prepareStatement(DELETE_ALL_CONTENT_SQL);
			deleteOldContent.executeUpdate();

			connection.commit();
			return true;
		} catch (SQLException e) {
			LOGGER.error("Couldn't delete all resource ids", e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			this.close(deleteOldContent, connection);
		}
	}

	private FogbowResource createFogbowResource(ResultSet result)
			throws SQLException, JSONException {
		String id = result.getString(RESOURCE_ID);
		String orderId = result.getString(ORDER_ID);
		String instanceId = result.getString(INSTANCE_ID);
		String specification = result.getString(SPEC);

		JSONObject jsonSpec = new JSONObject(specification);
		Specification spec = Specification.fromJSON(jsonSpec);

		FogbowResource fogbowResource = new FogbowResource(id, orderId, spec);
		fogbowResource.setInstanceId(instanceId);
		return fogbowResource;
	}

	private boolean hasBatchExecutionError(int[] executeBatch) {
		for (int i : executeBatch) {
			if (i == PreparedStatement.EXECUTE_FAILED) {
				return true;
			}
		}
		return false;
	}
}
