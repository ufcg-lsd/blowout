package org.fogbowcloud.blowout.core.model;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;


public class TestSpecification {

	private static final String PRIVATE_KEY_PATH = "privateKeyPath";
	private static final String PUBLIC_KEY = "publicKey";
	private static final String USERNAME = "username";
	private static final String IMAGE = "image";
	private static final String OTHER_VALUE = "otherValue";
	private static final String SECOND_REQUIREMENTE = "secondRequiremente";
	private static final String THIS_VALUE = "thisValue";
	private static final String FIRST_REQUIREMENT = "firstRequirement";

	@Test
	public void testToAndFromJSon() {
		Specification spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
		spec.addRequirement(FIRST_REQUIREMENT, THIS_VALUE);
		spec.addRequirement(SECOND_REQUIREMENTE, OTHER_VALUE);
		
		JSONObject jsonObject = spec.toJSON();
		
		Specification recoveredSpec = Specification.fromJSON(jsonObject);
		JSONObject jsonObject2 = recoveredSpec.toJSON();
		
		Specification recoveredSpec2 = Specification.fromJSON(jsonObject2);
	    assert(recoveredSpec2.equals(recoveredSpec));
	    assert(spec.equals(recoveredSpec));
		assertEquals(spec.getRequirementValue(FIRST_REQUIREMENT), recoveredSpec.getRequirementValue(FIRST_REQUIREMENT));
		assertEquals(spec.getRequirementValue(SECOND_REQUIREMENTE), recoveredSpec.getRequirementValue(SECOND_REQUIREMENTE));
	}

}
