package org.bimserver.ifcvalidator.checks;

import java.util.List;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Translator;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class IfcSiteLongitude extends ModelCheck {

	public IfcSiteLongitude() {
		super("SITE", "LONGITUDE");
	}

	@Override
	public boolean check(IfcModelInterface model, IssueContainer issueContainer, Translator translator) throws IssueException {
		List<IfcSite> sites = model.getAll(IfcSite.class);
		boolean valid = sites.size() > 0;
		for (IfcSite ifcSite : model.getAll(IfcSite.class)) {
			// Only checking whether this data is available
			
			if (ifcSite.eIsSet(Ifc2x3tc1Package.eINSTANCE.getIfcSite_RefLongitude())) {
				// TODO check whether this is a valid WSG84
				issueContainer.add(Type.SUCCESS, ifcSite.eClass().getName(), ifcSite.getGlobalId(), ifcSite.getOid(), "RefLongitude", ifcSite.getRefLongitude(), "Not null");
			} else {
				issueContainer.add(Type.ERROR, ifcSite.eClass().getName(), ifcSite.getGlobalId(), ifcSite.getOid(), "RefLongitude", null, "Not null");
				valid = false;
			}
		}
		return valid;
	}
}
