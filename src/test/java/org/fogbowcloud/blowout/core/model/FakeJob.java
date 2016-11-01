package org.fogbowcloud.blowout.core.model;

import java.util.List;

public class FakeJob extends Job{

	public FakeJob(List<Task> tasks) {
		super(tasks);
	}

	@Override
	public void finish(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fail(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
