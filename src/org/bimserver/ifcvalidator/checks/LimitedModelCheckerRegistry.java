package org.bimserver.ifcvalidator.checks;

public class LimitedModelCheckerRegistry extends ModelCheckerRegistry {
	public LimitedModelCheckerRegistry() {
		addCheck(new CarparkAccessability(new CarparkAccessibilityConfiguration()));
		addCheck(new ExteriorWindowSizeSpaceRatio(new WindowSpaceRatioConfiguration()));
		addCheck(new UnclassifiedSpaces());
	}
}
