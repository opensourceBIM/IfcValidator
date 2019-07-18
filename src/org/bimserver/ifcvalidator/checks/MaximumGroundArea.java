package org.bimserver.ifcvalidator.checks;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.geometry.Bounds;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.utils.Display;
import org.bimserver.utils.IfcTools2D;
import org.bimserver.utils.IfcUtils;
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
		// TODO use only products that are on the ground level (building storey)
		// TODO use a more precise area than boundsMm
		
		Area area = new Area();
		
		Set<IfcBuildingStorey> stories = new TreeSet<>(new Comparator<IfcBuildingStorey>() {
			@Override
			public int compare(IfcBuildingStorey o1, IfcBuildingStorey o2) {
				return Double.compare(o1.getElevation(), o2.getElevation());
			}});
		
		stories.addAll(model.getAll(IfcBuildingStorey.class));
		
		IfcBuildingStorey groundLevel = null;
		
		for (IfcBuildingStorey ifcBuildingStorey : stories) {
			if (ifcBuildingStorey.getElevation() >= 0) {
				// This must be ground level
				groundLevel = ifcBuildingStorey;
				break;
			}
		}
		
		if (groundLevel == null) {
			issueContainer.builder().message("No groundlevel found in model").type(Type.ERROR).add();
			return;
		}
		
		for (IfcProduct ifcProduct : IfcUtils.getDecompositionAndContainmentRecursive(groundLevel)) {
			if (ifcProduct instanceof IfcSite) {
				continue;
			}
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo == null) {
				continue;
			}
			Bounds boundsMm = geometryInfo.getBoundsMm();
			Area newArea = new Area(new Rectangle((int)boundsMm.getMin().getX(), (int)boundsMm.getMin().getY(), (int)(boundsMm.getMax().getX() - boundsMm.getMin().getX()), (int)(boundsMm.getMax().getY() - boundsMm.getMin().getY())));
			area.add(newArea);
		}

		float areaM2 = IfcTools2D.getArea(area) / 1000000;
		
//		new Display("test", 1024, 768, area);
		
		IssueBuilder builder = issueContainer.builder().is(areaM2 + "m2").shouldBe("<= " + MAX_GROUND_AREA_M2 + "m2");
		if (areaM2 > MAX_GROUND_AREA_M2) {
			builder.type(Type.ERROR).message("Ground area is too large");
		} else {
			builder.type(Type.SUCCESS).message("Ground area is within the limits");
		}
		builder.add();
	}
}