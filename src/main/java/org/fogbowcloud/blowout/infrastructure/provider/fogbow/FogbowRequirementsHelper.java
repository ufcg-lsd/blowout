package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

import condor.classad.AttrRef;
import condor.classad.ClassAdParser;
import condor.classad.Env;
import condor.classad.Expr;
import condor.classad.Op;
import condor.classad.RecordExpr;

public class FogbowRequirementsHelper {

	private static final Logger LOGGER = Logger.getLogger(FogbowRequirementsHelper.class);
	
	private static final String ZERO = "0";

	// TODO: delete this method
	public static boolean validateFogbowRequirementsSyntax(String requirementsString) {
		
		LOGGER.debug("Validating Fogbow Requirements ["+requirementsString+"]");
		
		if (requirementsString == null || requirementsString.isEmpty()) {
			
			LOGGER.debug("Fogbow Requirements ["+requirementsString+"] Validate with success.");
			return true;
		}
		try {
			ClassAdParser adParser = new ClassAdParser(requirementsString);
			if (adParser.parse() != null) {
			
				LOGGER.debug("Fogbow Requirements ["+requirementsString+"] Validate with success.");
				return true;
			}
			LOGGER.info("Fogbow Requirements ["+requirementsString+"] Invalid - Expression not found.");
			return false;
		} catch (Exception e) {
			LOGGER.error("Fogbow Requirements ["+requirementsString+"] Invalid", e);
			return false;
		}
	}

	public static boolean matches(FogbowResource resource, String requirements) {
		
		LOGGER.debug("Matching Fogbow Requirements [" + requirements + "] with Resource [id: "
				+ resource.getId() + "]");
		
		List<String> providedAttributes = new ArrayList<>();
		List<String> foundedAttributes = new ArrayList<>();
		
		try {
			if (requirements == null  || requirements.trim().isEmpty()) {
				return true;
			}
			
			ClassAdParser classAdParser = new ClassAdParser(requirements);
			Op expr = (Op) classAdParser.parse();
			
			foundedAttributes.add(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2vCPU);
			foundedAttributes.add(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2RAM);
			foundedAttributes.add(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2disk);
			foundedAttributes.add(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_1Glue2CloudComputeManagerID);
			
			Env env = new Env();
			String attributeValue = null;
			for (String attribute : foundedAttributes) {
				
				List<ValueAndOperator> findValuesInRequirements = findValuesInRequirements(expr, attribute);
				
				if (findValuesInRequirements.size() > 0) {

					// TODO: Check if you need to refact this
					if (attribute.equals(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2vCPU)) {
						providedAttributes.add(attribute);
						attributeValue = resource.getMetadataValue(AbstractResource.METADATA_VCPU);
					} 
					else if (attribute.equals(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2RAM)) {
						providedAttributes.add(attribute);
						attributeValue = resource.getMetadataValue(AbstractResource.METADATA_MEM_SIZE);
					} 
					else if (attribute.equals(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2disk)) {
						attributeValue = resource.getMetadataValue(AbstractResource.METADATA_DISK_SIZE);
						if (attributeValue != null && !attributeValue.equals(ZERO) ) {
							providedAttributes.add(attribute);
						}
					} 
					else if (attribute.equals(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_1Glue2CloudComputeManagerID)) {
						providedAttributes.add(attribute);
						attributeValue = resource.getMetadataValue(AbstractResource.METADATA_LOCATION);
					}
					
					env.push((RecordExpr) new ClassAdParser("[" + attribute + " = " + attributeValue + "]").parse());
					LOGGER.debug("Matching Requirement [" + attribute + " = " + attributeValue + "]");
				}
			}					
			
			if (providedAttributes.isEmpty()) {
				return true;
			}
			expr = extractVariablesExpression(expr, providedAttributes);
			
			return expr.eval(env).isTrue();
		} catch (Exception e) {
			LOGGER.error("Matching Fogbow Requirements ["+requirements+"] with Resource [id: "+resource.getId()+"] FAILED", e);
			return false;
		}finally {
			providedAttributes.clear();
			foundedAttributes.clear();
		}
	}
	
	private static Op extractVariablesExpression(Op expr, List<String> listAttName) {
		if (expr.arg1 instanceof AttrRef) {
			AttrRef attr = (AttrRef) expr.arg1;
			boolean thereIs = false;
			for (String attName : listAttName) {
				if (attr.name.rawString().equals(attName)) {
					thereIs = true;
				}
			}
			if (thereIs) {
				return expr;				
			}
			return null;
		}
		Expr left = expr.arg1;
		if (left instanceof Op) {
			left = extractVariablesExpression((Op) expr.arg1, listAttName);
		}
		Expr right = expr.arg2;
		if (right instanceof Op) {
			right = extractVariablesExpression((Op) expr.arg2, listAttName);
		}
		try {
			if (left == null) {
				return (Op) right;
			} else if (right == null) {
				return (Op) left;
			}			
		} catch (Exception e) {
			return null;
		}
		return new Op(expr.op, left, right);
	}

	private static List<ValueAndOperator> findValuesInRequirements(Op expr, String attName) {
		List<ValueAndOperator> valuesAndOperator = new ArrayList<ValueAndOperator>();
		if (expr.arg1 instanceof AttrRef) {
			AttrRef attr = (AttrRef) expr.arg1;
			if (attr.name.rawString().equals(attName)) {
				valuesAndOperator.add(new ValueAndOperator(expr.arg2.toString(), expr.op));
			}
			return valuesAndOperator;
		}
		if (expr.arg1 instanceof Op) {
			List<ValueAndOperator> findValuesInRequirements = findValuesInRequirements(
					(Op) expr.arg1, attName);
			if (findValuesInRequirements != null) {
				valuesAndOperator.addAll(findValuesInRequirements);
			}
		}
		if (expr.arg2 instanceof Op) {
			List<ValueAndOperator> findValuesInRequiremets = findValuesInRequirements(
					(Op) expr.arg2, attName);
			if (findValuesInRequiremets != null) {
				valuesAndOperator.addAll(findValuesInRequiremets);
			}
		}
		return valuesAndOperator;
	}


	protected static class ValueAndOperator {
		private String value;
		private int operator;

		public ValueAndOperator(String value, int operator) {
			this.value = value;
			this.operator = operator;
		}

		public int getOperator() {
			return operator;
		}

		public String getValue() {
			return value;
		}
	}
	
}
