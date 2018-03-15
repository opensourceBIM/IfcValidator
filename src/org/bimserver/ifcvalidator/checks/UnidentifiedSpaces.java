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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.CheckerContext;
import org.bimserver.models.ifc2x3tc1.IfcBuildingElement;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcCurtainWall;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcRelConnectsElements;
import org.bimserver.models.ifc2x3tc1.IfcRelConnectsPathElements;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.utils.Display;
import org.bimserver.utils.IfcTools2D;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.Type;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UnidentifiedSpaces extends ModelCheck {
	private final Map<IfcProduct, Area> generatedAreas = new HashMap<>();
	private float lengthUnitPrefix;

	public UnidentifiedSpaces() {
		super("SPACES", "UNIDENTIFIED");
	}

	private IfcBuildingElementWrapper getOrCreateWrapper(Map<IfcBuildingElement, IfcBuildingElementWrapper> mapping, IfcBuildingElement ifcBuildingElement) {
		IfcBuildingElementWrapper ifcBuildingElementWrapper = mapping.get(ifcBuildingElement);
		if (ifcBuildingElementWrapper == null) {
			ifcBuildingElementWrapper = new IfcBuildingElementWrapper(ifcBuildingElement);
			mapping.put(ifcBuildingElement, ifcBuildingElementWrapper);
		}
		return ifcBuildingElementWrapper;
	}
	
	private Area getOrCreateArea(IfcProduct ifcProduct, IfcTools2D ifcTools2D, float multiplierMillimeters) {
		Area area = generatedAreas.get(ifcProduct);
		if (area == null) {
			area = ifcTools2D.get2D(ifcProduct, multiplierMillimeters);
			generatedAreas.put(ifcProduct, area);
		}
		if (area != null) {
			return new Area(area);
		}
		return null;
	}
	
	@Override
	public void check(IfcModelInterface model, IssueContainer issueContainer, CheckerContext checkerContext) throws IssueException {
		boolean debug = false;
		boolean removeAllWalls = true;
		lengthUnitPrefix = IfcUtils.getLengthUnitPrefix(model);

		IfcTools2D ifcTools2D = new IfcTools2D();
		
		System.out.println(model.getAll(IfcRelConnectsPathElements.class).size() + " IfcRelConnectsPathElements found");

		for (IfcBuildingStorey ifcBuildingStorey : model.getAll(IfcBuildingStorey.class)) {
			BufferedImage image = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			
			AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
			flip.translate(-image.getWidth(), 0);
			graphics.transform(flip);

			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 2000, 2000);
			
			Area totalArea = new Area();
			for (IfcProduct ifcProduct : IfcUtils.getDecomposition(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcSpace) {
					Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						totalArea.add(area);
					}
				}
			}
			EdgeFactory<IfcBuildingElementWrapper, IfcRelConnectsPathElements> factory = new ClassBasedEdgeFactory<>(IfcRelConnectsPathElements.class);
			Pseudograph<IfcBuildingElementWrapper, IfcRelConnectsPathElements> graph = new Pseudograph<>(factory);

			Map<IfcBuildingElement, IfcBuildingElementWrapper> mapping = new HashMap<>();
			
			for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
					IfcBuildingElement ifcBuildingElement = (IfcBuildingElement)ifcProduct;
					graph.addVertex(getOrCreateWrapper(mapping, ifcBuildingElement));
					Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						totalArea.add(area);
					}
				}
			}
			
			for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
					IfcElement ifcWall = ((IfcElement)ifcProduct);
					for (IfcRelConnectsElements ifcRelConnectsElements : ifcWall.getConnectedFrom()) {
						if (ifcRelConnectsElements instanceof IfcRelConnectsPathElements) {
							IfcRelConnectsPathElements ifcRelConnectsPathElements = (IfcRelConnectsPathElements)ifcRelConnectsElements;
							IfcBuildingElementWrapper wall1 = getOrCreateWrapper(mapping, (IfcBuildingElement)ifcRelConnectsPathElements.getRelatedElement());
							IfcBuildingElementWrapper wall2 = getOrCreateWrapper(mapping, (IfcBuildingElement)ifcRelConnectsPathElements.getRelatingElement());
							if (!graph.containsVertex(wall1)) {
								graph.addVertex(wall1);
							}
							if (!graph.containsVertex(wall2)) {
								graph.addVertex(wall2);
							}
							if (!graph.addEdge(wall1, wall2, ifcRelConnectsPathElements)) {
								System.out.println("Redundant edge not added");
							}
						}
					}
				}
			}

//			writeToJson(ifcBuildingStorey, graph);
			
//			new Simplyfier().simplify(graph);

			for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
					IfcBuildingElementWrapper wrapper = getOrCreateWrapper(mapping, (IfcBuildingElement) ifcProduct);
					if (graph.edgesOf(wrapper).size() == 1) {
						graph.removeVertex(wrapper);
					}
				}
			}
			
			FindAllCyclesAlgo<IfcBuildingElementWrapper, IfcRelConnectsPathElements> algorighm = new FindAllCyclesAlgo<>(graph);
			List<Set<IfcBuildingElementWrapper>> findSimpleCycles = algorighm.findAllCycles();
			
			double scaleX = 1600 / totalArea.getBounds().getWidth();
			double scaleY = 1600 / totalArea.getBounds().getHeight();
			double scale = Math.min(scaleX, scaleY);
			
			AffineTransform affineTransform = new AffineTransform();
			affineTransform.translate(1000, 1000);
			affineTransform.scale(scale, scale);
			affineTransform.translate(-totalArea.getBounds2D().getCenterX(), -totalArea.getBounds2D().getCenterY());

			List<Set<IfcBuildingElementWrapper>> finalList = new ArrayList<>();

			Concurrent concurrent = new Concurrent(findSimpleCycles.size());
			for (Set<IfcBuildingElementWrapper> list : findSimpleCycles) {
				concurrent.run(new Runnable(){
					public void run() {
						Area cycleArea = new Area();
						for (IfcBuildingElementWrapper ifcWallOutside : list) {
							Area areaOutside = getOrCreateArea(ifcWallOutside.get(), ifcTools2D, lengthUnitPrefix);
							if (areaOutside != null) {
								cycleArea.add(areaOutside);
							}
						}
						
						Area smallest = IfcTools2D.findSmallest(cycleArea);
						
//				if (smallest != null) {
//					graphics.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
//					smallest.transform(affineTransform);
//					graphics.fill(smallest);
//				}
						
						if (smallest != null) {
							boolean foundCompleteFitting = false;
							for (Set<IfcBuildingElementWrapper> insideList : findSimpleCycles) {
								for (IfcBuildingElementWrapper ifcWallInside : insideList) {
									Area areaInside = getOrCreateArea(ifcWallInside.get(), ifcTools2D, lengthUnitPrefix);
									if (areaInside != null) {
										if (IfcTools2D.containsAllPoints(smallest, areaInside)) {
											foundCompleteFitting = true;
										}
									}
								}
							}
							if (!foundCompleteFitting) {
								finalList.add(list);
							}
						}
					}
				});
			}
			concurrent.await();
			System.out.println("Final list: " + finalList.size());

			Area checkArea = new Area();
			for (Set<IfcBuildingElementWrapper> list : finalList) {
//				boolean allExternal = true;
//				for (IfcBuildingElementWrapper ifcBuildingElement : list) {
//					Tristate booleanProperty = IfcUtils.getBooleanProperty(ifcBuildingElement.get(), "IsExternal");
//					if (booleanProperty == null || booleanProperty == Tristate.FALSE) {
//						allExternal = false;
//					}
//				}
//				if (allExternal) {
//					continue;
//				}
				Area cycleArea = new Area();
				for (IfcBuildingElementWrapper ifcWall : list) {
					Area area = getOrCreateArea(ifcWall.get(), ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						cycleArea.add(area);
					}
				}
				
//				graphics.setColor(Color.ORANGE);
//				cycleArea.transform(affineTransform);
//				graphics.fill(cycleArea);
				
				// Cycle area now should have an enclosed area, we should find out what the inside is
				
//				Area outerCurve = getOuterCurve(cycleArea);
//				if (outerCurve != null) {
//					outerCurve.transform(affineTransform);
//					graphics.setColor(Color.ORANGE);
//					graphics.draw(outerCurve);
//				}
//				Area innerCurve = getInnerCurve(cycleArea);
//				if (innerCurve != null) {
//					innerCurve.transform(affineTransform);
//					graphics.setColor(Color.GREEN);
//					graphics.draw(innerCurve);
//				}

				Area innerCurve = getInnerCurve(cycleArea);
				if (innerCurve != null) {
					checkArea.add(innerCurve);
					
//					graphics.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
//					innerCurve.transform(affineTransform);
//					graphics.fill(innerCurve);
				}
				
//				PathIterator pathIterator = cycleArea.getPathIterator(null);
//				if (cycleArea.isSingular()) {
//					System.out.println("Is singular");
//				} else {
//					Path2D.Double tmp = new Path2D.Double();
//					Path2D.Double smallest = new Path2D.Double();
//					Rectangle smallestRectangle = null;
//					while (!pathIterator.isDone()) {
//						double[] coords = new double[6];
//						int type = pathIterator.currentSegment(coords);
//						if (type == 0) {
//							tmp.moveTo(coords[0], coords[1]);
//						} else if (type == 4) {
//							tmp.closePath();
//							
//							// TODO use area, not the containment of aabb's, this only sort of works for rectangular "spaces"
//							if (smallestRectangle == null || smallestRectangle.contains(tmp.getBounds())) {
//								smallestRectangle = tmp.getBounds();
//								smallest = tmp;
//							}
//							tmp = new Path2D.Double();
//						} else if (type == 1) {
//							tmp.lineTo(coords[0], coords[1]);
//						}
//						pathIterator.next();
//					}
//					if (smallest != null) {
//						Area smallestArea = new Area(smallest);
//						
//						AffineTransform aLittleSmaller = new AffineTransform();
//						double centerX = smallestArea.getBounds2D().getCenterX();
//						double centerY = smallestArea.getBounds2D().getCenterY();
//						aLittleSmaller.translate(centerX, centerY);
//						aLittleSmaller.scale(0.2, 0.2);
//						aLittleSmaller.translate(-centerX, -centerY);
//
////						smallestArea.transform(affineTransform);
////						graphics.fill(smallestArea);
//
//						smallestArea.transform(aLittleSmaller);
//						checkArea.add(smallestArea);
//					}
//				}
			}
			
			for (IfcProduct ifcProduct : IfcUtils.getDecomposition(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcSpace) {
					Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						checkArea.subtract(area);
					}
				}
			}
			if (removeAllWalls) {
				for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
					if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
						Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
						if (area != null) {
							checkArea.subtract(area);
						}
					}
				}
			}
			
//			for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
//				if (ifcProduct instanceof IfcWall) {
//					Area area = IfcTools2D.get2D(ifcProduct, lengthUnitPrefix);
//					if (area != null) {
//						totalArea.add(area);
//					}
//				}
//			}
			
			graphics.setColor(Color.decode("#919DFF"));
			for (IfcProduct ifcProduct : IfcUtils.getDecomposition(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcSpace) {
					Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						area.transform(affineTransform);
						graphics.fill(area);
					}
				}
			}
			graphics.setColor(Color.decode("#A4FF9B"));
			for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
				if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
					IfcElement ifcWall = ((IfcElement)ifcProduct);
					Area area = getOrCreateArea(ifcWall, ifcTools2D, lengthUnitPrefix);
					if (area != null) {
						area.transform(affineTransform);
						graphics.fill(area);
					}
				}
			}

			PathIterator pathIterator = checkArea.getPathIterator(null);
			int nrErrors = 0;
			Path2D.Float newPath = new Path2D.Float();
			while (!pathIterator.isDone()) {
				float[] coords = new float[6];
				int currentSegment = pathIterator.currentSegment(coords);
				if (currentSegment == PathIterator.SEG_CLOSE) {
					newPath.closePath();
					float area = Math.abs(IfcTools2D.getArea(new Area(newPath)));
					if (area > 0.001) {
						BufferedImage errorImage = renderImage(ifcBuildingStorey, ifcTools2D, totalArea, newPath);
						issueContainer.builder().type(Type.ERROR).object(ifcBuildingStorey).message("Missing IfcSpace of " + String.format("%.2f", area) + " m2 on \"" + ifcBuildingStorey.getName() + "\"").image(errorImage).add();
						nrErrors++;
					}
					newPath = new Path2D.Float();
				} else if (currentSegment == PathIterator.SEG_LINETO) {
					newPath.lineTo(coords[0], coords[1]);
				} else if (currentSegment == PathIterator.SEG_MOVETO) {
					newPath.moveTo(coords[0], coords[1]);
				} else {
					System.out.println("Unimplemented segment" + currentSegment);
				}
				pathIterator.next();
			}

			if (nrErrors == 0) {
				BufferedImage errorImage = renderImage(ifcBuildingStorey, ifcTools2D, totalArea, null);
				issueContainer.builder().type(Type.SUCCESS).object(ifcBuildingStorey).buildingStorey(ifcBuildingStorey).message("No unidentified spaces found in building storey \"" + ifcBuildingStorey.getName() + "\"").image(errorImage).add();
			}
			
			graphics.setColor(Color.RED);
			checkArea.transform(affineTransform);
			graphics.fill(checkArea);
			
			ifcTools2D.dumpStatistics();
			
			if (debug) {
				Display display = new Display(ifcBuildingStorey.getName(), 2000, 2000);
				display.setImage(image);
			}
		}
	}
	
	public void writeToJson(IfcBuildingStorey ifcBuildingStorey, Pseudograph<IfcBuildingElementWrapper, IfcRelConnectsPathElements> graph) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode graphJson = objectMapper.createObjectNode();

		ArrayNode vertices = objectMapper.createArrayNode();
		ArrayNode edges = objectMapper.createArrayNode();
		
		graphJson.set("vertices", vertices);
		graphJson.set("edges", edges);
		
		for (IfcBuildingElementWrapper ifcBuildingElementWrapper : graph.vertexSet()) {
			vertices.add(ifcBuildingElementWrapper.get().getOid());
		}
		for (IfcRelConnectsPathElements ifcRelConnectsPathElements : graph.edgeSet()) {
			ObjectNode edgeJson = objectMapper.createObjectNode();
			edgeJson.put("id", ifcRelConnectsPathElements.getOid());
			edgeJson.put("from", ifcRelConnectsPathElements.getRelatedElement().getOid());
			edgeJson.put("to", ifcRelConnectsPathElements.getRelatingElement().getOid());
			edges.add(edgeJson);
		}
		
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ifcBuildingStorey.getName() + ".graph.json"), graphJson);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage renderImage(IfcBuildingStorey ifcBuildingStorey, IfcTools2D ifcTools2D, Area totalArea, Path2D.Float newPath) {
		BufferedImage bufferedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
		
		int width = 800;
		int height = 600;

		AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
		flip.translate(-width, 0);
		graphics.transform(flip);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		
		double scaleX = (width * 0.9) / totalArea.getBounds().getWidth();
		double scaleY = (height * 0.9) / totalArea.getBounds().getHeight();
		double scale = Math.min(scaleX, scaleY);
		
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.translate(width / 2f, height / 2f);
		affineTransform.scale(scale, scale);
		affineTransform.translate(-totalArea.getBounds2D().getCenterX(), -totalArea.getBounds2D().getCenterY());
		
		graphics.setColor(Color.decode("#919DFF"));
		for (IfcProduct ifcProduct : IfcUtils.getDecomposition(ifcBuildingStorey)) {
			if (ifcProduct instanceof IfcSpace) {
				Area area = getOrCreateArea(ifcProduct, ifcTools2D, lengthUnitPrefix);
				if (area != null) {
					area.transform(affineTransform);
					graphics.fill(area);
				}
			}
		}
		graphics.setColor(Color.decode("#A4FF9B"));
		for (IfcProduct ifcProduct : IfcUtils.getContains(ifcBuildingStorey)) {
			if (ifcProduct instanceof IfcWall || ifcProduct instanceof IfcCurtainWall) {
				IfcElement ifcWall = ((IfcElement)ifcProduct);
				Area area = getOrCreateArea(ifcWall, ifcTools2D, lengthUnitPrefix);
				if (area != null) {
					area.transform(affineTransform);
					graphics.fill(area);
				}
			}
		}
		
		if (newPath != null) {
			graphics.setColor(Color.RED);
			Area newArea = new Area(newPath);
			newArea.transform(affineTransform);
			graphics.fill(newArea);
		}
		
		return bufferedImage;
	}

	private Area getInnerCurve(Area area) {
		PathIterator pathIterator = area.getPathIterator(null);
		if (area.isSingular()) {
			System.out.println("Is singular");
		} else {
			Path2D.Float tmp = new Path2D.Float();
			Path2D.Float smallest = new Path2D.Float();
			Rectangle smallestRectangle = null;
			while (!pathIterator.isDone()) {
				double[] coords = new double[6];
				int type = pathIterator.currentSegment(coords);
				if (type == 0) {
					tmp.moveTo(coords[0], coords[1]);
				} else if (type == 4) {
					tmp.closePath();
					
					// TODO use area, not the containment of aabb's, this only sort of works for rectangular "spaces"
					if (smallestRectangle == null || smallestRectangle.contains(tmp.getBounds())) {
						smallestRectangle = tmp.getBounds();
						smallest = tmp;
					}
					tmp = new Path2D.Float();
				} else if (type == 1) {
					tmp.lineTo(coords[0], coords[1]);
				}
				pathIterator.next();
			}
			if (smallest != null) {
				Area smallestArea = new Area(smallest);
				return smallestArea;
			}
		}
		return null;
	}
	
	public Area getOuterCurve(Area area) {
		PathIterator pathIterator = area.getPathIterator(null);
		if (area.isSingular()) {
			System.out.println("Is singular");
		} else {
			Path2D.Float tmp = new Path2D.Float();
			Path2D.Float largest = new Path2D.Float();
			Rectangle largestRectangle = null;
			while (!pathIterator.isDone()) {
				double[] coords = new double[6];
				int type = pathIterator.currentSegment(coords);
				if (type == 0) {
					tmp.moveTo(coords[0], coords[1]);
				} else if (type == 4) {
					tmp.closePath();
					
					// TODO use area, not the containment of aabb's, this only sort of works for rectangular "spaces"
					if (largestRectangle == null || tmp.getBounds().contains(largestRectangle)) {
						largestRectangle = tmp.getBounds();
						largest = tmp;
					}
					tmp = new Path2D.Float();
				} else if (type == 1) {
					tmp.lineTo(coords[0], coords[1]);
				}
				pathIterator.next();
			}
			if (largest != null) {
				return new Area(largest);
			}
		}
		return null;
	}
}