package org.bimserver.ifcvalidator.checks;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.ifcvalidator.ValidationException;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;

public class IfcSiteKadastaleAanduiding extends ModelCheck {

	public IfcSiteKadastaleAanduiding() {
		super("SITE", "KADASTRALE_AANDUIDING");
	}

	@Override
	public void check(IfcModelInterface model, ValidationReport validationReport, Translator translator) {
		for (IfcSite ifcSite : model.getAll(IfcSite.class)) {
			try {
				checkKadastraleAanduidingen(ifcSite);
				validationReport.add(Type.SUCCESS, ifcSite.getOid(), "Kadastrale aanduiding", "Valid", "Valid");
			} catch (ValidationException e) {
				validationReport.add(Type.ERROR, ifcSite.getOid(), "Kadastrale aanduiding", "Invalid", "Valid");
			}
		}
	}
	
	private void checkKadastraleAanduidingen(IfcSite ifcSite) throws ValidationException {
		String name = ifcSite.getName();
		String[] split = name.split("-");
		for (String part : split) {
			if (part.contains(" ")) {
				String[] spacesSplit = part.split(" ");
				String number = spacesSplit[spacesSplit.length - 1];
				try {
					Integer.parseInt(number);
				} catch (NumberFormatException e) {
					throw new ValidationException("perceelsnummer not a number");
				}
			}
		}
	}
}
