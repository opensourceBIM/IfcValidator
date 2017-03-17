package org.bimserver.ifcvalidator.checks;

import java.util.Set;

import org.bimserver.models.ifc2x3tc1.IfcRelConnectsPathElements;
import org.jgrapht.graph.Pseudograph;

public class Simplyfier {

	public void simplify(Pseudograph<IfcBuildingElementWrapper, IfcRelConnectsPathElements> graph) {
		for (IfcBuildingElementWrapper wrapper : graph.vertexSet()) {
			Set<IfcRelConnectsPathElements> edges = graph.edgesOf(wrapper);
			if (edges.size() == 1) {
				graph.removeVertex(wrapper);
			} else if (edges.size() == 2) {
				
			}
		}
	}
}
