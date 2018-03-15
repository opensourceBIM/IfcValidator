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

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.ifcvalidator.ValidationException;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class IfcSiteKadastaleAanduiding extends ModelCheck {

	public IfcSiteKadastaleAanduiding() {
		super("SITE", "KADASTRALE_AANDUIDING");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		List<IfcSite> sites = model.getAll(IfcSite.class);
		for (IfcSite ifcSite : sites) {
			try {
				checkKadastraleAanduidingen(ifcSite);
				issueContainer.builder().type(Type.SUCCESS).object(ifcSite).message("Kadastrale aanduiding").is("Valid").shouldBe("Valid").add();
			} catch (ValidationException e) {
				issueContainer.builder().type(Type.ERROR).object(ifcSite).message(e.getMessage()).is(ifcSite.getName()).shouldBe("Valid").add();
			}
		}
	}
	
	private void checkKadastraleAanduidingen(IfcSite ifcSite) throws ValidationException {
		String name = ifcSite.getName();
		if (name == null) {
			throw new ValidationException("No name");
		}
		String[] split = name.split("-");
		for (String part : split) {
			if (part.contains(" ")) {
				String[] spacesSplit = part.split(" ");
				String number = spacesSplit[spacesSplit.length - 1];
				try {
					Integer.parseInt(number);
				} catch (NumberFormatException e) {
					throw new ValidationException("Kadastrale aanduiding - Perceelsnummer not a number");
				}
			} else {
				throw new ValidationException("Kadastrale aanduiding - No spaces in name");
			}
		}
	}
}
