package org.fogbowcloud.blowout.scheduler.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.core.DataStore;
import org.fogbowcloud.blowout.core.core.Scheduler;
import org.fogbowcloud.blowout.core.core.model.Order;
import org.fogbowcloud.blowout.core.core.model.Resource;
import org.fogbowcloud.blowout.core.core.model.Specification;
import org.fogbowcloud.blowout.core.core.model.Order.OrderState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataStore {
	private static final Logger LOGGER = Logger.getLogger(TestDataStore.class);

	private final double ACCEPTABLE_ERROR = 0.01; 
	private final String DATASTORE_PATH = "src/test/resources/persistance/";
	private final String FAKE_REQUEST_ID1 = "fakerequestid1";
	private final String FAKE_REQUEST_ID2 = "fakerequestid2";
	private final String FAKE_REQUEST_ID3 = "fakerequestid3";
	private final String FAKE_REQUEST_ID4 = "fakerequestid4";
	private final String FAKE_REQUEST_ID5 = "fakerequestid5";
	
	Properties properties = null;
	DataStore db = null; 

	@Before
	public void initialize() {		
		LOGGER.debug("Creating data store.");
		new File(DATASTORE_PATH).mkdir();
		properties = new Properties();
		properties.put("accounting_datastore_url", "jdbc:h2:mem:"
				+ new File(DATASTORE_PATH).getAbsolutePath() + "orders");

		db = spy(new DataStore(properties));
	}

	@After
	public void tearDown() throws IOException{
		FileUtils.cleanDirectory(new File (DATASTORE_PATH));
		db.dispose();
	}

	@Test
	public void testAddListOfOrders() throws SQLException, InterruptedException {
		Scheduler scheduler = mock(Scheduler.class);
		Specification specification = mock(Specification.class);
		Request order1 = mock(Request.class);
		Request order2 = mock(Request.class);
		doReturn(FAKE_REQUEST_ID1).when(order1).getRequestId();
		doReturn(FAKE_REQUEST_ID2).when(order2).getRequestId();
		List<Request> orders = new ArrayList<Request>();
		List<FogbowResource> resources = new ArrayList<FogbowResource>();
		orders.add(order1);
		orders.add(order2);
		db.updateInfrastructureState(orders, resources);
		verify(db).getConnection();
		verify(db).prepare(any(Connection.class), any(String.class));
		String sql = "select * from " + DataStore.REQUEST_ID_TABLE_NAME;
		ResultSet rs = db.getConnection().createStatement().executeQuery(sql);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID1);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID2);
	}
	
	@Test
	public void testGetRequestIds() throws SQLException{
		Scheduler scheduler = mock(Scheduler.class);
		Specification specification = mock(Specification.class);
		Request order1 = mock(Request.class);
		Request order2 = mock(Request.class);
		Request order3 = mock(Request.class);
		doReturn(FAKE_REQUEST_ID1).when(order1).getRequestId();
		doReturn(FAKE_REQUEST_ID2).when(order2).getRequestId();
		doReturn(FAKE_REQUEST_ID3).when(order3).getRequestId();
		List<Request> orders = new ArrayList<Request>();
		List<Request> orders2 = new ArrayList<Request>();
		List<FogbowResource> resources = new ArrayList<FogbowResource>();
		orders.add(order1);
		orders.add(order2);
		db.updateInfrastructureState(orders, resources);
		verify(db).getConnection();
		verify(db).prepare(any(Connection.class), any(String.class));
		
		List<String> requestIds = db.getRequesId();
		assertEquals(2, requestIds.size());
		assert(requestIds.contains(FAKE_REQUEST_ID1));
		assert(requestIds.contains(FAKE_REQUEST_ID2));
	}
	
	@Test
	public void testAddListOfOrdersAgain() throws SQLException, InterruptedException {
		Scheduler scheduler = mock(Scheduler.class);
		Specification specification = mock(Specification.class);
		Request order1 = mock(Request.class);
		Request order2 = mock(Request.class);
		Request order3 = mock(Request.class);
		doReturn(FAKE_REQUEST_ID1).when(order1).getRequestId();
		doReturn(FAKE_REQUEST_ID2).when(order2).getRequestId();
		doReturn(FAKE_REQUEST_ID3).when(order3).getRequestId();
		List<Request> orders = new ArrayList<Request>();
		List<Request> orders2 = new ArrayList<Request>();
		List<FogbowResource> resources = new ArrayList<FogbowResource>();
		orders.add(order1);
		orders.add(order2);
		db.updateInfrastructureState(orders, resources);
		verify(db).getConnection();
		verify(db).prepare(any(Connection.class), any(String.class));
		String sql = "select * from " + DataStore.REQUEST_ID_TABLE_NAME;
		ResultSet rs = db.getConnection().createStatement().executeQuery(sql);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID1);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID2);
		
		orders2.add(order3);
		db.updateInfrastructureState(orders2, resources);
		verify(db, times(2)).prepare(any(Connection.class), any(String.class));
		rs = db.getConnection().createStatement().executeQuery(sql);
		rs.next();
		assertEquals(FAKE_REQUEST_ID3, rs.getString(DataStore.REQUEST_ID));
		
		assertEquals(1, db.getRequesId().size());
	}
	
	@Test
	public void testAddListOfOrdersAndResources() throws SQLException, InterruptedException {
		Scheduler scheduler = mock(Scheduler.class);
		Specification specification = mock(Specification.class);
		
		Request order1 = mock(Request.class);
		Request order2 = mock(Request.class);
		doReturn(FAKE_REQUEST_ID1).when(order1).getRequestId();
		doReturn(RequestState.ORDERED).when(order1).getState();
		doReturn(FAKE_REQUEST_ID2).when(order2).getRequestId();
		doReturn(RequestState.FULFILLED).when(order2).getState();
		List<Request> orders = new ArrayList<Request>();
		orders.add(order1);
		orders.add(order2);
		
		FogbowResource resource1 = mock(FogbowResource.class);
		doReturn(FAKE_REQUEST_ID4).when(resource1).getId();
		FogbowResource resource2 = mock(FogbowResource.class);
		doReturn(FAKE_REQUEST_ID5).when(resource2).getId();
		List<FogbowResource> resources = new ArrayList<FogbowResource>();
		resources.add(resource1);
		resources.add(resource2);
		
		db.updateInfrastructureState(orders, resources);
		verify(db).getConnection();
		verify(db).prepare(any(Connection.class), any(String.class));
		String sql = "select * from " + DataStore.REQUEST_ID_TABLE_NAME;
		ResultSet rs = db.getConnection().createStatement().executeQuery(sql);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID1);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID2);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID4);
		rs.next();
		assertEquals(rs.getString(DataStore.REQUEST_ID), FAKE_REQUEST_ID5);
	}
	
}
