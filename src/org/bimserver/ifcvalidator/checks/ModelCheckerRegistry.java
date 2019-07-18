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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ModelCheckerRegistry {
	private final Map<String, Map<String, ModelCheck>> modelChecks = new LinkedHashMap<>();

	public void addCheck(ModelCheck modelCheck) {
		Map<String, ModelCheck> map = modelChecks.get(modelCheck.getGroupIdentifier());
		if (map == null) {
			map = new LinkedHashMap<String, ModelCheck>();
			modelChecks.put(modelCheck.getGroupIdentifier(), map);
		}
		map.put(modelCheck.getIdentifier(), modelCheck);
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