package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
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

public class IlsModelCheckerRegistry extends ModelCheckerRegistry {
	
	public IlsModelCheckerRegistry() {
		addCheck(new AllObjectsInBuildingStorey());
		addCheck(new AtLeastOneBuilding());
		addCheck(new AtLeastOneBuildingStorey());
		addCheck(new BuildingStoreyNamesAndZOrder());
		addCheck(new HasTrueNorthSet());
		addCheck(new IfcSiteKadastaleAanduiding());
		addCheck(new IfcSiteLatitude());
		addCheck(new IfcSiteLongitude());
		addCheck(new OnlyOneIfcProject());
		addCheck(new OnlyOneIfcSite());
		addCheck(new FireratingDoorsSameAsWalls());
	}
}