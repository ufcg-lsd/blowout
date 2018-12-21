package org.fogbowcloud.blowout.core.model;

import static org.junit.Assert.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestSpecification {

	private static final String PRIVATE_KEY_PATH = "privateKeyPath";
	private static final String PUBLIC_KEY = "publicKey";
	private static final String USERNAME = "username";
	private static final String IMAGE = "image";
	private static final String OTHER_VALUE = "otherValue";
	private static final String SECOND_REQUIREMENTE = "secondRequiremente";
	private static final String THIS_VALUE = "thisValue";
	private static final String FIRST_REQUIREMENT = "firstRequirement";

    public final static String FOGBOW_REQUIREMENT_A = "Glue2vCPU  == 1 && Glue2RAM >= 1024 ";
    public final static String FOGBOW_REQUIREMENT_B = "Glue2vCPU >= 1 &&  Glue2RAM >= 1024 && Glue2disk <= 20 ";
    public final static String FOGBOW_REQUIREMENT_C = "Glue2vCPU <= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_D = "Glue2vCPU >= 1  && Glue2RAM == 1024 || Glue2RAM == 2048 && Glue2disk == 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
    public final static String FOGBOW_REQUIREMENT_E = "";
    public final static String FOGBOW_REQUIREMENT_F = null;

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
		assertEquals(spec.getRequirementValue(FIRST_REQUIREMENT), recoveredSpec.getRequirementValue(FIRST_REQUIREMENT));
		assertEquals(spec.getRequirementValue(SECOND_REQUIREMENTE), recoveredSpec.getRequirementValue(SECOND_REQUIREMENTE));
	}

	@Test
	public void testgetVCPU() {
		List<String> fogbowRequirements = new ArrayList<>();
		fogbowRequirements.add(FOGBOW_REQUIREMENT_A);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
		fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

		Specification spec;
		for (String fogbowRequirement: fogbowRequirements) {
			spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
			spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

			assertEquals("1", spec.getvCPU());
		}

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getvCPU());

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
        assertEquals(null, spec.getvCPU());
	}

    @Test
    public void testgetMemory() {
        List<String> fogbowRequirements = new ArrayList<>();
        fogbowRequirements.add(FOGBOW_REQUIREMENT_A);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

        Specification spec;
        for (String fogbowRequirement: fogbowRequirements) {
            spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
            spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

            assertEquals("1024", spec.getMemory());
        }

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getMemory());

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
        assertEquals(null, spec.getMemory());
    }

    @Test
    public void testgetDisk() {
        List<String> fogbowRequirements = new ArrayList<>();
        fogbowRequirements.add(FOGBOW_REQUIREMENT_B);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_C);
        fogbowRequirements.add(FOGBOW_REQUIREMENT_D);

        Specification spec;
        for (String fogbowRequirement: fogbowRequirements) {
            spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
            spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

            assertEquals("20", spec.getDisk());
        }

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_E);
        assertEquals("", spec.getDisk());

        spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_F);
        assertEquals(null, spec.getDisk());
    }

    @Test
    public void testGetFogbowRequirementValueWhenNoParam() {
        Specification spec = new Specification(IMAGE, USERNAME, PUBLIC_KEY, PRIVATE_KEY_PATH);
        assertNull(spec.getvCPU());
    }
}
