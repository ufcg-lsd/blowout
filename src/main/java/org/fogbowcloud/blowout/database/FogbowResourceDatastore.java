package org.fogbowcloud.blowout.database;

import java.sql.Connection;
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
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.JSONException;
import org.json.JSONObject;

public class FogbowResourceDatastore {


	private static final Logger LOGGER = Logger.getLogger(FogbowResourceDatastore.class);

	protected static final String FOGBOW_RESOURCE_TABLE_NAME = "fogbow_resource_id_store";

	protected static final String RESOURCE_ID = "resource_id";
	protected static final String ORDER_ID = "order_id";
	protected static final String INSTANCE_ID = "instance_id";
	protected static final String SPEC = "specification";
	
	//SQLs
	private static final String INSERT_FOGBOW_RESOURCE_SQL = "INSERT INTO " + FOGBOW_RESOURCE_TABLE_NAME
			+ " VALUES(?,?,?)";
	private static final String UPDATE_FOGBOW_RESOURCE = "UPDATE " + FOGBOW_RESOURCE_TABLE_NAME
			+ " SET "+ORDER_ID+"=? , "+INSTANCE_ID+"=? WHERE "+RESOURCE_ID+"=?";
	private static final String SELECT_REQUEST_ID = "SELECT * FROM " + FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_ALL_CONTENT_SQL = "DELETE FROM " + FOGBOW_RESOURCE_TABLE_NAME;
	private static final String DELETE_BY_RESOURCE_ID_SQL = DELETE_ALL_CONTENT_SQL + " WHERE "+RESOURCE_ID+"=? ";
	

	private String dataStoreURL;
	private JdbcConnectionPool cp;
	private Properties properties;

	public FogbowResourceDatastore(Properties properties) {
		this.properties = properties;
		this.dataStoreURL = this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL);

		Statement statement = null;
		Connection connection = null;
		try {
			LOGGER.debug("DatastoreURL: " + dataStoreURL);

			Class.forName("org.h2.Driver");
			this.cp = JdbcConnectionPool.create(dataStoreURL, "sa", "");

			connection = getConnection();
			statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS " + FOGBOW_RESOURCE_TABLE_NAME
					+ "(" 
					+ RESOURCE_ID+ " VARCHAR(255) PRIMARY KEY,"
					+ ORDER_ID+ " VARCHAR(255),"
					+ INSTANCE_ID+ " VARCHAR(255),"
					+ SPEC+ " TEXT "
					+ ")");
			statement.close();

		} catch (Exception e) {
			LOGGER.error("Error while initializing the DataStore.", e);
		} finally {
			close(statement, connection);
		}
	}


	public Connection getConnection() throws SQLException {
		try {
			return cp.getConnection();
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



	public boolean addFogbowResource(FogbowResource fogbowResource){
		LOGGER.debug("Adding resource id: "+fogbowResource.getId());
		PreparedStatement insertResourceStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			
			String spec = null;
			
			if(fogbowResource.getRequestedSpec()!= null){
				JSONObject json = fogbowResource.getRequestedSpec().toJSON();
				spec = json.toString();
			}
		
			insertResourceStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);
			insertResourceStatement.setString(1, fogbowResource.getId());
			insertResourceStatement.setString(2, fogbowResource.getOrderId());
			insertResourceStatement.setString(3, fogbowResource.getInstanceId());
			if(spec == null){
				insertResourceStatement.setNull(4, Types.VARCHAR);
			}else{
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
			close(insertResourceStatement, connection);
		}
	}
	
	public boolean addResourceIds(List<FogbowResource> fogbowResources){
		LOGGER.debug("Adding resource ids");
		PreparedStatement insertResourcesStatement = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			insertResourcesStatement = prepare(connection, INSERT_FOGBOW_RESOURCE_SQL);
			for (FogbowResource fogbowResource : fogbowResources){
				
				String spec = null;
				
				if(fogbowResource.getRequestedSpec()!= null){
					JSONObject json = fogbowResource.getRequestedSpec().toJSON();
					spec = json.toString();
				}
				
				insertResourcesStatement.setString(1, fogbowResource.getId());
				insertResourcesStatement.setString(2, fogbowResource.getOrderId());
				insertResourcesStatement.setString(3, fogbowResource.getInstanceId());
				if(spec == null){
					insertResourcesStatement.setNull(4, Types.VARCHAR);
				}else{
					insertResourcesStatement.setString(4, spec);
				}
				insertResourcesStatement.addBatch();
			}
			
			if (hasBatchExecutionError(insertResourcesStatement.executeBatch())){
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
			close(insertResourcesStatement, connection);
		}
	}
	
	public boolean updateFogbowResource(FogbowResource fogbowResource){
		LOGGER.debug("Updating resource id: "+fogbowResource.getId());
		PreparedStatement updateFogbowResourceStatment = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		
			updateFogbowResourceStatment = prepare(connection, UPDATE_FOGBOW_RESOURCE);
			updateFogbowResourceStatment.setString(1, fogbowResource.getOrderId());
			updateFogbowResourceStatment.setString(2, fogbowResource.getInstanceId());
			updateFogbowResourceStatment.setString(3, fogbowResource.getId());
			boolean result = updateFogbowResourceStatment.executeUpdate() > 0 ? true : false;
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't update fogbow resource "+fogbowResource.getId(), e);
			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error("Couldn't rollback transaction.", e1);
			}
			return false;
		} finally {
			close(updateFogbowResourceStatment, connection);
		}
	}
	
	protected PreparedStatement prepare(Connection connection, String statement) throws SQLException {
		return connection.prepareStatement(statement);
	}

	public List<FogbowResource> getAllFogbowResources() {
		List<FogbowResource> fogbowResources = new ArrayList<FogbowResource>();
		Statement getRequestIdStatement = null;
		Connection connection = null;
		try {
			connection =  getConnection();
			getRequestIdStatement = connection.createStatement();
			getRequestIdStatement.execute(SELECT_REQUEST_ID);
			ResultSet result = getRequestIdStatement.getResultSet();

			while(result.next()){
				FogbowResource fogbowresource = createFogbowResource(result); 
				fogbowResources.add(fogbowresource);
			}
			
			return fogbowResources;
			
		} catch (Exception e){
			LOGGER.error("Couldn't recover request Ids from DB", e);	
			return null;
		} finally {
			close(getRequestIdStatement, connection);
		}
	}
	
	public boolean deleteFogbowResourceById(FogbowResource fogbowResource){
		
		LOGGER.debug("Deleting resource id: "+fogbowResource.getId());
		PreparedStatement deleteResourceId = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
		
			deleteResourceId = prepare(connection, DELETE_BY_RESOURCE_ID_SQL);
			deleteResourceId.setString(1, fogbowResource.getId());
			boolean result = deleteResourceId.execute();
			connection.commit();
			return result;
		} catch (SQLException e) {
			LOGGER.error("Couldn't delete the resource "+fogbowResource.getId(), e);
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
			deleteOldContent.addBatch();
			if (hasBatchExecutionError(deleteOldContent.executeBatch())){
				connection.rollback();
				return false;
			}
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
	
	private FogbowResource createFogbowResource(ResultSet result) throws SQLException, JSONException {
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


	public void dispose() {
		cp.dispose();
	}

}
