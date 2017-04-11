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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ModelCheckerRegistry {
	private final Map<String, Map<String, ModelCheck>> modelChecks = new LinkedHashMap<>();
	
	private void addCheck(ModelCheck modelCheck) {
		Map<String, ModelCheck> map = modelChecks.get(modelCheck.getGroupIdentifier());
		if (map == null) {
			map = new LinkedHashMap<String, ModelCheck>();
			modelChecks.put(modelCheck.getGroupIdentifier(), map);
		}
		map.put(modelCheck.getIdentifier(), modelCheck);
	}
	
	public ModelCheckerRegistry() {
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
	
	public Set<String> getGroupIdentifiers() {
		return modelChecks.keySet();
	}

	public ModelCheck getModelCheck(String groupIdentifer, String identifier) {
		return modelChecks.get(groupIdentifer).get(identifier);
	}

	public Set<String> getIdentifiers(String groupIdentifier) {
		return modelChecks.get(groupIdentifier).keySet();
	}
}