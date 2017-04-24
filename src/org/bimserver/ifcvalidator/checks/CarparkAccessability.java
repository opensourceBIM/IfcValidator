package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.utils.IfcTools2D;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
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
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		scaleToMm = IfcUtils.getLengthUnitPrefix(model);
		int regularSpaces = 0;
		int handicappedSpaces = 0;
		int unidentifiedCarparks = 0;
		int unidentifiedSpaces = 0;
		int totalCarparks = 0;
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			if (ifcSpace.getObjectType() != null && ifcSpace.getObjectType().equalsIgnoreCase("parking")) {
				totalCarparks++;
				CarparkVote psetVote = checkPset(ifcSpace);
				CarparkVote geometryVote = checkGeometry(ifcSpace);
				if (psetVote.equals(geometryVote)) {
					if (psetVote.carparkVoteType == CarparkVoteType.REGULAR_CARPARK) {
						issueContainer.builder().type(Type.SUCCESS).object(ifcSpace).message("Both pset and geometry agree that this is a regular carpark").add();
						regularSpaces++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.HANDICAPPED_CARPARK) {
						issueContainer.builder().type(Type.SUCCESS).object(ifcSpace).message("Both pset and geometry agree that this is a handicapped carpark").add();
						handicappedSpaces++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.NOT_A_CARPARK) {
						// Both agree this is not a carpark, so do nothing
					} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_CARPARK) {
						issueContainer.builder().type(Type.ERROR).object(ifcSpace).message("Both pset and geometry check did not lead to identifying the nature of this carpark").add();
						unidentifiedCarparks++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_SPACE) {
						issueContainer.builder().type(Type.ERROR).object(ifcSpace).message("Both pset and geometry check did not lead to identifying the nature of this space").add();
						unidentifiedSpaces++;
					}
				} else {
					// Both checks do not agree
					if (psetVote.carparkVoteType == CarparkVoteType.REGULAR_CARPARK) {
						issueContainer.builder().type(Type.SUCCESS).object(ifcSpace).message("This is a regular carpark according to " + psetVote.getType() + ", the geometry does not agree").add();
						regularSpaces++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.HANDICAPPED_CARPARK) {
						issueContainer.builder().type(Type.SUCCESS).object(ifcSpace).message("This is a handicapped carpark according to " + psetVote.getType() + ", the geometry does not agree").add();
						handicappedSpaces++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.NOT_A_CARPARK) {
//					issueContainer.add(Type.SUCCESS, ifcSpace.eClass().getName(), ifcSpace.getGlobalId(), ifcSpace.getOid(), "This is not a carpark according to " + mostCertain.getType(), "", "");
					} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_CARPARK) {
						issueContainer.builder().type(Type.ERROR).object(ifcSpace).message("The nature of this carpark could not be identified semantically").add();
						unidentifiedCarparks++;
					} else if (psetVote.carparkVoteType == CarparkVoteType.UNIDENTIFIED_SPACE) {
						issueContainer.builder().type(Type.ERROR).object(ifcSpace).message("Both pset and geometry check did not lead to identifying the nature of this space").add();
						unidentifiedSpaces++;
					}
				}
			}
		}
		if (unidentifiedSpaces > 0) {
//			issueContainer.add(Type.ERROR, "The amount of unidentified spaces is too high", "" + unidentifiedSpaces, "" + 0);
			issueContainer.builder().type(Type.ERROR).message("The amount of unidentified spaces is too high").is(unidentifiedSpaces).shouldBe(0).add();
		}
		if (unidentifiedCarparks > 0) {
			issueContainer.builder().type(Type.ERROR).message("The amount of unidentified carparks is too high").is(unidentifiedCarparks).shouldBe(0).add();
//			issueContainer.add(Type.ERROR, "The amount of unidentified carparks is too high", "" + unidentifiedCarparks, "" + 0);
		}
		if (regularSpaces > handicappedSpaces * conf.getRatioHandicappedToRegularParking()) {
			issueContainer.builder().type(Type.ERROR).message("The amount of handicapped carparks should be higher").is(handicappedSpaces).shouldBe(regularSpaces / conf.getRatioHandicappedToRegularParking()).add();
//			issueContainer.add(Type.ERROR, "The amount of handicapped carparks should be higher", "" + handicappedSpaces, "" + (regularSpaces / conf.getRatioHandicappedToRegularParking()));
		}
		if (totalCarparks == 0) {
			issueContainer.builder().type(Type.CANNOT_CHECK).message("No carparks found, not checking").is(0).shouldBe("not 0").add();
//			issueContainer.add(Type.CANNOT_CHECK, "No carparks found, not checking", "0", "> 0");
		}
	}
	
	private CarparkVote checkGeometry(IfcSpace ifcSpace) {
		CarparkVote carparkVote = new CarparkVote(CheckType.GEOMETRY);

		Area area = IfcTools2D.get2D(ifcSpace, scaleToMm);
		Rectangle2D bounds2d = area.getBounds2D();
		
		float xDim = (float) bounds2d.getWidth();
		float yDim = (float) bounds2d.getHeight();
		
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
				// TODO what to return here, assume it's a regular carpark? Or assume this is an error
				carparkVote.setCarparkVoteType(CarparkVoteType.REGULAR_CARPARK);
//				carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_CARPARK);
			}
		} else {
			carparkVote.setCarparkVoteType(CarparkVoteType.UNIDENTIFIED_SPACE);
		}
		return carparkVote;
	}
}