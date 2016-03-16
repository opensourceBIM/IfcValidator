package org.bimserver.modelcheck.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.validationreport.ValidationReport;

public interface ModelCheck {

	void check(IfcModelInterface model, ValidationReport validationReport);
}
