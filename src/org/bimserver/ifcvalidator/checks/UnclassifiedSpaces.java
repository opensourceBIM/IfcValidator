package org.bimserver.ifcvalidator.checks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcClassificationNotationSelect;
import org.bimserver.models.ifc2x3tc1.IfcClassificationReference;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class UnclassifiedSpaces extends ModelCheck {

	public UnclassifiedSpaces() {
		super("UNCLASSIFIED_SPACES", "UNCLASSIFIED");
	}
	
	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		Set<String> availableClasses = new HashSet<>();
		try (Scanner scanner = new Scanner(checkerContext.getResource("omniclass14.txt"))) {
			while (scanner.hasNext()) {
				availableClasses.add(scanner.nextLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (IfcSpace ifcSpace : model.getAll(IfcSpace.class)) {
			boolean valid = false;
			List<IfcClassificationNotationSelect> classifications = IfcUtils.getClassifications(ifcSpace, model);
			for (IfcClassificationNotationSelect ifcClassificationNotationSelect : classifications) {
				if (ifcClassificationNotationSelect instanceof IfcClassificationReference) {
					IfcClassificationReference ifcClassificationReference = (IfcClassificationReference)ifcClassificationNotationSelect;
					if (availableClasses.contains(((IfcClassificationReference) ifcClassificationNotationSelect).getItemReference())) {
						valid = true;
						issueContainer.builder().object(ifcSpace).message("IfcSpace classified with valid OmniClass").type(Type.SUCCESS).is(ifcClassificationReference.getItemReference()).shouldBe("OmniClass Table 14").add();
					}
				}
			}
			if (!valid) {
				issueContainer.builder().object(ifcSpace).message("IfcSpace not classified with valid OmniClass").type(Type.ERROR).shouldBe("OmniClass Table 14").add();
			}
		}
	}
}