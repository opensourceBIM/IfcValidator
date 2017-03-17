package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
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
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcSite> sites = model.getAll(IfcSite.class);
		boolean valid = sites.size() > 0;
		for (IfcSite ifcSite : sites) {
			try {
				checkKadastraleAanduidingen(ifcSite);
				issueContainer.add(Type.SUCCESS, ifcSite.eClass().getName(), ifcSite.getGlobalId(), ifcSite.getOid(), "Kadastrale aanduiding", "Valid", "Valid");
			} catch (ValidationException e) {
				issueContainer.add(Type.ERROR, ifcSite.eClass().getName(), ifcSite.getGlobalId(), ifcSite.getOid(), e.getMessage(), ifcSite.getName(), "Valid");
				valid = false;
			}
		}
		return valid;
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
