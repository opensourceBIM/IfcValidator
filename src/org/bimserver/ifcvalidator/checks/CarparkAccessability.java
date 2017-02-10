package org.bimserver.ifcvalidator.checks;

import java.util.Map;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcExtrudedAreaSolid;
import org.bimserver.models.ifc2x3tc1.IfcProductRepresentation;
import org.bimserver.models.ifc2x3tc1.IfcProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcRectangleProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcRepresentation;
import org.bimserver.models.ifc2x3tc1.IfcRepresentationItem;
import org.bimserver.models.ifc2x3tc1.IfcShapeRepresentation;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

/*
 * http://www.buildingsmart-tech.org/ifc/IFC2x3/TC1/html/psd/IfcProductExtension/Pset_SpaceParking.xml
 * 
 */
public class CarparkAccessability extends ModelCheck {

	private CarparkAccessibilityConfiguration conf;
	private float scaleToMm;

	public CarparkAccessability(CarparkAccessibilityConfiguration carparkAccessibilityConfiguration) {
		super("ACCESSIBILITY", "CARPARKS");
		this.conf = carparkAccessibilityConfiguration;
	}

	private enum CarparkVoteType {
		UNIDENTIFIED_SPACE,
		UNIDENTIFIED_CARPARK,
		NOT_A_CARPARK,
		REGULAR_CARPARK,
		HANDICAPPED_CARPARK;
	}
	
	private enum CheckType {
		GEOMETRY("Geometry"),
		PSET("Pset_SpaceParking");

		private String humanReadable;

		CheckType(String humanReadable) {
			this.humanReadable = humanReadable;
		}
		
		@Override
		public String toString() {
			return humanReadable;
		}
	}

	private class CarparkVote {
		
		private CarparkVoteType carparkVoteType;
		private CheckType checkType;

		public CarparkVote(CheckType checkType) {
			this.checkType = checkType;
		}
		
		public void setCarparkVoteType(CarparkVoteType carparkVoteType) {
			this.carparkVoteType = carparkVoteType;
		}
		
		public CarparkVoteType getCarparkVoteType() {
			return carparkVoteType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((carparkVoteType == null) ? 0 : carparkVoteType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CarparkVote other = (CarparkVote) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (carparkVoteType != other.carparkVoteType)
				return false;
			return true;
		}

		private CarparkAccessability getOuterType() {
			return CarparkAccessability.this;
		}

		public String getType() {
			return checkType.toString();
		}
	}
	
	@Override
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		scaleToMm = IfcUtils.getLengthUnitPrefix(model);
		int regularSpaces = 0;
		int handicappedSpaces = 0;
		int unidentifiedCarparks = 0;
		int unidentifiedSpaces = 0;
		issueInterface.addHeader("Carpark disabled check");
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			CarparkVote psetVote = checkPset(ifcSpace);
			CarparkVote geometryVote = checkGeometry(ifcSpace);
			if (psetVote.equals(geometryVote)) {
				if (psetVote.carparkVoteType == CarparkVoteType.REGULAR_CARPARK) {
					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Both pset and geometry agree that this is a regular carpark", "", "");
					regularSpaces++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.HANDICAPPED_CARPARK) {
					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Both pset and geometry agree that this is a handicapped carpark", "", "");
					handicappedSpaces++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.NOT_A_CARPARK) {
					// Both agree this is not a carpark, so do nothing
				} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_CARPARK) {
					issueInterface.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Both pset and geometry check did not lead to identifying the nature of this carpark", "", "");
					unidentifiedCarparks++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_SPACE) {
					issueInterface.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Both pset and geometry check did not lead to identifying the nature of this space", "", "");
					unidentifiedSpaces++;
				}
			} else {
				// Both checks do not agree
				if (psetVote.carparkVoteType == CarparkVoteType.REGULAR_CARPARK) {
					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "This is a regular carpark according to " + psetVote.getType() + ", the geometry does not agree", "", "");
					regularSpaces++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.HANDICAPPED_CARPARK) {
					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "This is a handicapped carpark according to " + psetVote.getType() + ", the geometry does not agree", "", "");
					handicappedSpaces++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.NOT_A_CARPARK) {
//					issueInterface.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "This is not a carpark according to " + mostCertain.getType(), "", "");
				} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_CARPARK) {
					issueInterface.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "The nature of this carpark could not be identified semantically", "", "");
					unidentifiedCarparks++;
				} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_SPACE) {
					issueInterface.add(Type.ERROR, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "Both pset and geometry check did not lead to identifying the nature of this space", "", "");
					unidentifiedSpaces++;
				}
			}
		}
		if (unidentifiedSpaces > 0) {
			issueInterface.add(Type.ERROR, "The amount of unidentified spaces is too high", "" + unidentifiedSpaces, "" + 0);
		}
		if (unidentifiedCarparks > 0) {
			issueInterface.add(Type.ERROR, "The amount of unidentified carparks is too high", "" + unidentifiedCarparks, "" + 0);
		}
		if (regularSpaces > handicappedSpaces * conf.getRatioHandicappedToRegularParking()) {
			issueInterface.add(Type.ERROR, "The amount of handicapped carparks should be higher", "" + handicappedSpaces, "" + (regularSpaces / conf.getRatioHandicappedToRegularParking()));
		}
		return false;
	}
	
	private CarparkVote checkGeometry(IfcSpace ifcSpace) {
		CarparkVote carparkVote = new CarparkVote(CheckType.GEOMETRY);
		IfcProductRepresentation representation = ifcSpace.getRepresentation();
		for (IfcRepresentation ifcRepresentation : representation.getRepresentations()) {
			if (ifcRepresentation instanceof IfcShapeRepresentation) {
				IfcShapeRepresentation ifcShapeRepresentation = (IfcShapeRepresentation) ifcRepresentation;
				for (IfcRepresentationItem ifcRepresentationItem : ifcShapeRepresentation.getItems()) {
					if (ifcRepresentationItem instanceof IfcExtrudedAreaSolid) {
						IfcExtrudedAreaSolid ifcExtrudedAreaSolid = (IfcExtrudedAreaSolid) ifcRepresentationItem;
						IfcProfileDef ifcProfileDef = ifcExtrudedAreaSolid.getSweptArea();
						if (ifcProfileDef instanceof IfcRectangleProfileDef) {
							IfcRectangleProfileDef ifcRectangleProfileDef = (IfcRectangleProfileDef)ifcProfileDef;
							double xDim = ifcRectangleProfileDef.getXDim() * scaleToMm;
							double yDim = ifcRectangleProfileDef.getYDim() * scaleToMm;
							
							if (xDim > conf.getHandicappedCarparkWidth() - conf.getHandicappedCarparkVariation() && xDim < conf.getHandicappedCarparkWidth() + conf.getHandicappedCarparkVariation() &&
								yDim > conf.getHandicappedCarparkDepth() - conf.getHandicappedCarparkVariation() && yDim < conf.getHandicappedCarparkDepth() + conf.getHandicappedCarparkVariation()) {
								carparkVote.setCarparkVoteType(CarparkVoteType.HANDICAPPED_CARPARK);
							} else if (xDim > conf.getHandicappedCarparkDepth() - conf.getHandicappedCarparkVariation() && xDim < conf.getHandicappedCarparkDepth() + conf.getHandicappedCarparkVariation() &&
									yDim > conf.getHandicappedCarparkWidth() - conf.getHandicappedCarparkVariation() && yDim < conf.getHandicappedCarparkWidth() + conf.getHandicappedCarparkVariation()) {
								carparkVote.setCarparkVoteType(CarparkVoteType.HANDICAPPED_CARPARK);
							} else if (xDim > conf.getRegularCarparkDepth() - conf.getRegularCarparkVariation() && xDim < conf.getRegularCarparkDepth() + conf.getRegularCarparkVariation() &&
									yDim > conf.getRegularCarparkWidth() - conf.getRegularCarparkVariation() && yDim < conf.getRegularCarparkWidth() + conf.getRegularCarparkVariation()) {
								carparkVote.setCarparkVoteType(CarparkVoteType.REGULAR_CARPARK);
							} else if (xDim > conf.getRegularCarparkWidth() - conf.getRegularCarparkVariation() && xDim < conf.getRegularCarparkWidth() + conf.getRegularCarparkVariation() &&
									yDim > conf.getRegularCarparkDepth() - conf.getRegularCarparkVariation() && yDim < conf.getRegularCarparkDepth() + conf.getRegularCarparkVariation()) {
								carparkVote.setCarparkVoteType(CarparkVoteType.REGULAR_CARPARK);
							} else {
								carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_CARPARK);
							}
						}
					}
				}
			}
		}
		
		if (carparkVote.getCarparkVoteType() == null) {
			carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_SPACE);
		}
		
		return carparkVote;
	}

	public CarparkVote checkPset(IfcSpace ifcSpace) {
		CarparkVote carparkVote = new CarparkVote(CheckType.PSET);
		if (ifcSpace.getObjectType() != null && ifcSpace.getObjectType().equals("Parking")) {
			Map<String, Object> properties = IfcUtils.listProperties(ifcSpace, "Pset_SpaceParking");
			if (properties.containsKey("HandicapAccessible")) {
				if (properties.get("HandicapAccessible") == Boolean.TRUE) {
					carparkVote.setCarparkVoteType(CarparkVoteType.HANDICAPPED_CARPARK);
				} else {
					carparkVote.setCarparkVoteType(CarparkVoteType.REGULAR_CARPARK);
				}
			} else {
				carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_CARPARK);
			}
		} else {
			carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_SPACE);
		}
		return carparkVote;
	}
}