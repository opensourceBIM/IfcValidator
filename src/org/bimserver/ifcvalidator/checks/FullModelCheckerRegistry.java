package org.bimserver.ifcvalidator.checks;

public class FullModelCheckerRegistry extends ModelCheckerRegistry {
	
	public FullModelCheckerRegistry() {
		addCheck(new OnlyOneIfcProject());

		addCheck(new AtLeastOneBuilding());

		addCheck(new AtLeastOneBuildingStorey());

		addCheck(new CheckLengthUnit());
		addCheck(new CheckAreaUnit());
		addCheck(new CheckVolumeUnit());
		
		addCheck(new HasTrueNorthSet());

		addCheck(new OnlyOneIfcSite());
		addCheck(new IfcSiteKadastaleAanduiding());
		addCheck(new IfcSiteLatitude());
		addCheck(new IfcSiteLongitude());
		addCheck(new IfcSiteElevation());

		addCheck(new CarparkAccessability(new CarparkAccessibilityConfiguration()));
		addCheck(new ExteriorWindowSizeSpaceRatio(new WindowSpaceRatioConfiguration()));
		addCheck(new UnidentifiedSpaces());
		addCheck(new UnclassifiedSpaces());

		addCheck(new AllObjectsInBuildingStorey());
		
		addCheck(new BuildingStoreyNamesAndZOrder());
	}
}