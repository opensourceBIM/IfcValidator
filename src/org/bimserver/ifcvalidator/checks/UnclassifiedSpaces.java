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