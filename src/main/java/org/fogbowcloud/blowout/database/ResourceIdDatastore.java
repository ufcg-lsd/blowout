package org.fogbowcloud.blowout.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;

public class ResourceIdDatastore {


	private static final Logger LOGGER = Logger.getLogger(ResourceIdDatastore.class);

	protected static final String RESOURCE_IDS_TABLE_NAME = "resource_id_store";

	protected static final String RESOURCE_ID = "resource_id";
	
	protected static final String MANAGER_DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";
	
	protected static final String PREFIX_DATASTORE_URL = "jdbc:sqlite:";
	
	//SQLs
	private static final String INSERT_MEMBER_USAGE_SQL = "INSERT INTO " + RESOURCE_IDS_TABLE_NAME
			+ " VALUES(?)";

	private static final String SELECT_REQUEST_ID = "SELECT * FROM " + RESOURCE_IDS_TABLE_NAME;
	private static final String DELETE_ALL_CONTENT_SQL = "DELETE FROM " + RESOURCE_IDS_TABLE_NAME;
	private static final String DELETE_BY_RESOURCE_ID_SQL = DELETE_ALL_CONTENT_SQL + " WHERE "+RESOURCE_ID+"=? ";

	private String dataStoreURL;

	public ResourceIdDatastore(Properties properties) {
		this.dataStoreURL = properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL);

		Statement statement = null;
		Connection connection = null;
		try {
			LOGGER.debug("DatastoreURL: " + dataStoreURL);

			Class.forName(MANAGER_DATASTORE_SQLITE_DRIVER);
			
			connection = getConnection();
			statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS " + RESOURCE_IDS_TABLE_NAME
					+ "(" + RESOURCE_ID+ " VARCHAR(255) PRIMARY KEY)");
			statement.close();

		} catch (Exception e) {
			LOGGER.error("Error while initializing the DataStore.", e);
		} finally {
			close(statement, connection);
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
				LOGGER.error("Couldn't close statement");
			}
		}

		if (conn != null) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Couldn't close connection");
			}
		}
	}

	

	public boolean addResourceId(String resourceId){
		LOGGER.debug("Adding resource id: "+resourceId);
		PreparedStatement updateRequestList = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		
			updateRequestList = prepare(connection, INSERT_MEMBER_USAGE_SQL);
			updateRequestList.setString(1, resourceId);
			boolean result = updateRequestList.execute();
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
			close(updateRequestList, connection);
		}
	}
	
	public boolean addResourceIds(List<String> resourceIds){
		LOGGER.debug("Adding resource ids");
		PreparedStatement updateRequestList = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			updateRequestList = prepare(connection, INSERT_MEMBER_USAGE_SQL);
			for (String resourceID : resourceIds){
				updateRequestList.setString(1, resourceID);
				updateRequestList.addBatch();
			}
			
			if (hasBatchExecutionError(updateRequestList.executeBatch())){
				connection.rollback();
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
			close(updateRequestList, connection);
		}
	}
	
	protected PreparedStatement prepare(Connection connection, String statement) throws SQLException {
		return connection.prepareStatement(statement);
	}

	

	public List<String> getResourceIds() {
		List<String> resourceIds = new ArrayList<String>();
		Statement getRequestIdStatement = null;
		Connection connection = null;
		try {
			connection =  getConnection();
			getRequestIdStatement = connection.createStatement();
			getRequestIdStatement.execute(SELECT_REQUEST_ID);
			ResultSet result = getRequestIdStatement.getResultSet();
			while(result.next()){
				resourceIds.add(result.getString(RESOURCE_ID));
			}
			return resourceIds;
		} catch (SQLException e){
			LOGGER.error("Couldn't recover request Ids from DB", e);	
			return null;
		} finally {
			close(getRequestIdStatement, connection);
		}
	}
	
	
	public boolean deleteResourceId(String resourceId){
		
		LOGGER.debug("Deleting resource id: "+resourceId);
		PreparedStatement deleteResourceId = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		
			deleteResourceId = prepare(connection, DELETE_BY_RESOURCE_ID_SQL);
			deleteResourceId.setString(1, resourceId);
			boolean result = deleteResourceId.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't delete the resource "+resourceId, e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			close(deleteResourceId, connection);
		}
	}
	
	public boolean deleteAll(){
		
		LOGGER.debug("Deleting all resources");
		PreparedStatement deleteOldContent = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			deleteOldContent = connection.prepareStatement(DELETE_ALL_CONTENT_SQL);
			deleteOldContent.execute();
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
			close(deleteOldContent, connection);

		}
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
