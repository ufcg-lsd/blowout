package org.fogbowcloud.blowout.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFogbowResourceDataStore {
	private static final Logger LOGGER = Logger.getLogger(TestFogbowResourceDataStore.class);

	private final String DATASTORE_PATH = "src/test/resources/persistance/";
	private final String FAKE_REQUEST_ID1 = "fakerequestid1";
	private final String FAKE_REQUEST_ID2 = "fakerequestid2";
	
	Properties properties = null;
	ResourceIdDatastore db = null; 

	@Before
	public void initialize() {		
		LOGGER.debug("Creating data store.");
		new File(DATASTORE_PATH).mkdir();
		properties = new Properties();
		properties.put(AppPropertiesConstants.DB_DATASTORE_URL, "jdbc:sqlite:"
				+ new File(DATASTORE_PATH).getAbsolutePath() + "/resourceRequests");

		db = spy(new ResourceIdDatastore(properties));
	}

	@After
	public void tearDown() throws IOException{
		FileUtils.cleanDirectory(new File (DATASTORE_PATH));
	}
	
	@Test
	public void testAddResourceId() throws SQLException, InterruptedException {

		db.addResourceId(FAKE_REQUEST_ID1);
		List<String> returnedResourceIds = db.getResourceIds();
		
		assertEquals(1, returnedResourceIds.size());
		for(String resourceId : returnedResourceIds){
			assertEquals(FAKE_REQUEST_ID1, resourceId);
		}
	}

	@Test
	public void testAddListOfResourcesId() throws SQLException, InterruptedException {
		
		List<String> resourceIds = new ArrayList<String>();
		resourceIds.add(FAKE_REQUEST_ID1);
		resourceIds.add(FAKE_REQUEST_ID2);

		db.addResourceIds(resourceIds);
		List<String> returnedResourceIds = db.getResourceIds();
		
		assertEquals(resourceIds.size(), returnedResourceIds.size());
		for(String resourceId : returnedResourceIds){
			assertTrue(resourceIds.contains(resourceId));
		}
	}
	
	@Test
	public void testDeleteResourceId() throws SQLException, InterruptedException {
		
		List<String> resourceIds = new ArrayList<String>();
		resourceIds.add(FAKE_REQUEST_ID1);
		resourceIds.add(FAKE_REQUEST_ID2);

		db.addResourceIds(resourceIds);
		db.deleteResourceId(FAKE_REQUEST_ID1);
		List<String> returnedResourceIds = db.getResourceIds();
		
		assertEquals(1, returnedResourceIds.size());
		assertFalse(returnedResourceIds.contains(FAKE_REQUEST_ID1));
		assertTrue(returnedResourceIds.contains(FAKE_REQUEST_ID2));
	}
	
	@Test
	public void testDeleteAll() throws SQLException, InterruptedException {
		
		List<String> resourceIds = new ArrayList<String>();
		resourceIds.add(FAKE_REQUEST_ID1);
		resourceIds.add(FAKE_REQUEST_ID2);

		db.addResourceIds(resourceIds);
		db.deleteAll();
		List<String> returnedResourceIds = db.getResourceIds();
		
		assertEquals(0, returnedResourceIds.size());
	}
	
	
}
