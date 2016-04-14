package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.Type;

public class OnlyOneIfcProject extends ModelCheck {
	public OnlyOneIfcProject() {
		super("PROJECT", "ONLY_ONE_IFC_PROJECT");
	}
	
	public boolean check(IfcModelInterface model, IssueInterface issueInterface, Translator translator) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		
		IfcProject ifcProject = projects.size() == 1 ? projects.get(0) : null;
		issueInterface.add(projects.size() == 1 ? Type.SUCCESS : Type.ERROR, "IfcProject", ifcProject == null ? null : ifcProject.getGlobalId(), ifcProject == null ? null : ifcProject.getOid(), translator.translate("NUMBER_OF_PROJECTS"), projects.size() + " " + translator.translate(projects.size() == 1 ? "PROJECT" : "PROJECTS"), translator.translate("EXACTLY_ONE_PROJECT"));
		
		return projects.size() == 1;
	}
}