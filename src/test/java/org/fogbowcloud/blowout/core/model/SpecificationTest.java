package org.fogbowcloud.blowout.core.model;

import static org.junit.Assert.*;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import org.fogbowcloud.blowout.core.constants.FogbowConstants;

import org.fogbowcloud.blowout.helpers.Constants;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class SpecificationTest {
	private Specification spec;

	@Before
	public void setUp(){
		 this.spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID,
                 Constants.JSON.Key.USERNAME, Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
	}

	@Test
	public void testToAndFromJSon() {

		this.spec.addRequirement(STR_FIRST_REQUIREMENT, STR_THIS_VALUE);
		this.spec.addRequirement(STR_SECOND_REQUIREMENT, STR_OTHER_VALUE);
		
		JSONObject jsonObject = this.spec.toJSON();
		
		Specification recoveredSpec = Specification.fromJSON(jsonObject);
		JSONObject jsonObject2 = recoveredSpec.toJSON();
		
		Specification recoveredSpec2 = Specification.fromJSON(jsonObject2);
	    assert(recoveredSpec2.equals(recoveredSpec));
		assertEquals(this.spec.getRequirementValue(STR_FIRST_REQUIREMENT),
				recoveredSpec.getRequirementValue(STR_FIRST_REQUIREMENT));
		assertEquals(this.spec.getRequirementValue(STR_SECOND_REQUIREMENT),
				recoveredSpec.getRequirementValue(STR_SECOND_REQUIREMENT));
	}

	@Test
	public void testGetVCPU() {
		List<String> fogbowRequirements = new ArrayList<>();
		fogbowRequirements.add(FOGBOW_REQUIREMENT_A);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

		for (String fogbowRequirement: fogbowRequirements) {
			this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

			assertEquals("1", this.spec.getvCPU());
		}

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getvCPU());

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
		assertNull(spec.getvCPU());
	}

    @Test
    public void testGetMemory() {
        List<String> fogbowRequirements = new ArrayList<>();
        fogbowRequirements.add(FOGBOW_REQUIREMENT_A);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

        Specification spec;
        for (String fogbowRequirement: fogbowRequirements) {
            spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID,
                    Constants.JSON.Key.USERNAME, Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
            spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

            assertEquals("1024", spec.getMemory());
        }

        spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID,
                Constants.JSON.Key.USERNAME, Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getMemory());

        spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID,
                Constants.JSON.Key.USERNAME, Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
	    assertNull(spec.getMemory());
    }

    @Test
    public void testGetDisk() {
        List<String> fogbowRequirements = new ArrayList<>();
        fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

        Specification spec;
        for (String fogbowRequirement: fogbowRequirements) {
            spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID,
                    Constants.JSON.Key.USERNAME, Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
            spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

            assertEquals("20", spec.getDisk());
        }

        spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID, Constants.JSON.Key.USERNAME,
                Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getDisk());

        spec = new Specification(Constants.JSON.Key.CLOUD_NAME, Constants.JSON.Key.IMAGE_ID, Constants.JSON.Key.USERNAME,
                Constants.JSON.Key.PUBLIC_KEY, Constants.JSON.Key.PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
	    assertNull(spec.getDisk());
    }

    @Test
    public void testGetFogbowRequirementValueWhenNoParam() {
        assertNull(this.spec.getvCPU());
    }
}
