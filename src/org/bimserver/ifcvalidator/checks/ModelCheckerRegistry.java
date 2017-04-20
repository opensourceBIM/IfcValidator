package org.bimserver.ifcvalidator.checks;

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