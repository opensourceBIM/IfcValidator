package org.bimserver.ifcvalidator.checks;

import java.awt.Rectangle;
import java.awt.geom.Area;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.Bounds;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.utils.IfcTools2D;
import org.bimserver.validationreport.IssueBuilder;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;

public class MaximumGroundArea extends ModelCheck {

	private static final double MAX_GROUND_AREA_M2 = 1000;
	
	public MaximumGroundArea() {
		super("GROUND", "MAX_GROUND_AREA");
	}

	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		Area area = new Area();
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo == null) {
				continue;
			}
			Bounds boundsMm = geometryInfo.getBoundsMm();
			area.add(new Area(new Rectangle((int)boundsMm.getMin().getX(), (int)boundsMm.getMin().getY(), (int)(boundsMm.getMax().getX() - boundsMm.getMin().getX()), (int)(boundsMm.getMax().getY() - boundsMm.getMin().getY()))));
		}

		float areaM2 = IfcTools2D.getArea(area) / 1000000;
		
		IssueBuilder builder = issueContainer.builder().is(areaM2 + "m2").shouldBe("<= " + MAX_GROUND_AREA_M2 + "m2");
		if (areaM2 > MAX_GROUND_AREA_M2) {
			builder.type(Type.ERROR).message("Ground area is too large");
		} else {
			builder.type(Type.SUCCESS).message("Ground area is within the limits");
		}
		builder.add();
	}
}