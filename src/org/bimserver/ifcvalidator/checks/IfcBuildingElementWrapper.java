package org.bimserver.ifcvalidator.checks;

import org.bimserver.models.ifc2x3tc1.IfcBuildingElement;

public class IfcBuildingElementWrapper implements Comparable<IfcBuildingElementWrapper> {

	private IfcBuildingElement ifcBuildingElement;

	public IfcBuildingElementWrapper(IfcBuildingElement ifcBuildingElement) {
		this.ifcBuildingElement = ifcBuildingElement;
	}
	
	@Override
	public int compareTo(IfcBuildingElementWrapper o) {
		return (int) (ifcBuildingElement.getOid() - o.ifcBuildingElement.getOid());
	}
	
	public IfcBuildingElement get() {
		return ifcBuildingElement;
	}
	
	@Override
	public String toString() {
		return ifcBuildingElement.getName();
	}
}