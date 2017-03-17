package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class OnlyOneIfcProject extends ModelCheck {
	public OnlyOneIfcProject() {
		super("PROJECT", "ONLY_ONE_IFC_PROJECT");
	}
	
	public void check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcProject> projects = model.getAll(IfcProject.class);
		
		IfcProject ifcProject = projects.size() == 1 ? projects.get(0) : null;
		issueContainer.builder().type(projects.size() == 1 ? Type.SUCCESS : Type.ERROR)
		.object(ifcProject).message(translator.translate("NUMBER_OF_PROJECTS")).is(projects.size() + " " + translator.translate(projects.size() == 1 ? "PROJECT" : "PROJECTS")).shouldBe(translator.translate("EXACTLY_ONE_PROJECT")).add();;
	}
}