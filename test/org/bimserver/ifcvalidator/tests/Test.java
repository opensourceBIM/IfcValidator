package org.bimserver.ifcvalidator.tests;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.bimserver.tests.emf.TestListWalls;
import org.bimserver.utils.Display;
import org.bimserver.utils.IfcTools2D;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

public class Test {
	public static class V {
		private String id;

		public V(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return id;
		}
	}
	
	public static class E {
		
	}
	
	public static void main(String[] args) {
		testWalls();
//		EdgeFactory<V, E> factory = new ClassBasedEdgeFactory<>(E.class);
//		Pseudograph<V, E> graph = new Pseudograph(factory);
//		
//		V v1 = new V("1");
//		V v2 = new V("2");
//		V v3 = new V("3");
//		V v4 = new V("4");
//		V v5 = new V("5");
//		V v6 = new V("6");
//		
//		graph.addVertex(v1);
//		graph.addVertex(v2);
//		graph.addVertex(v3);
//		graph.addVertex(v4);
//		graph.addVertex(v5);
//		graph.addVertex(v6);
//		
//		graph.addEdge(v1, v2);
//		graph.addEdge(v2, v3);
//		graph.addEdge(v3, v4);
//		graph.addEdge(v4, v1);
//		graph.addEdge(v2, v5);
//		graph.addEdge(v5, v6);
//		graph.addEdge(v6, v3);
//		
//		PatonCycleBase<V, E> tarjanSimpleCycles = new PatonCycleBase<>(graph);
//		List<List<V>> findSimpleCycles = tarjanSimpleCycles.findCycleBase();
//		
//		System.out.println(findSimpleCycles.size());
//		for (List<V> list : findSimpleCycles) {
//			for (V v : list) {
//				System.out.println(v);
//			}
//			System.out.println();
//		}
//		
//		System.out.println();
	}
	
	public static void testWalls() {
		Area area = new Area();
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo(1, 1);
		path.lineTo(4, 1);
		path.lineTo(4, 2);
		path.lineTo(1, 2);
		path.closePath();
		
		area.add(new Area(path));
		
		path = new Path2D.Double();
		path.moveTo(3, 1);
		path.lineTo(4, 1);
		path.lineTo(4, 4);
		path.lineTo(3, 4);
		path.closePath();
		
		area.add(new Area(path));
		
		path = new Path2D.Double();
		path.moveTo(1, 3);
		path.lineTo(4, 3);
		path.lineTo(4, 4);
		path.lineTo(1, 4);
		path.closePath();
		
		area.add(new Area(path));

		path = new Path2D.Double();
		path.moveTo(1, 1);
		path.lineTo(2, 1);
		path.lineTo(2, 4);
		path.lineTo(1, 4);
		path.closePath();
		
		area.add(new Area(path));
		
		Area smallest = IfcTools2D.findSmallest(area);
		Display display = new Display("Test", 1000, 1000);
		
		double scaleX = (1000 * 0.9) / area.getBounds().getWidth();
		double scaleY = (1000 * 0.9) / area.getBounds().getHeight();
		double scale = Math.min(scaleX, scaleY);
		
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.translate(area.getBounds2D().getWidth() / 2, area.getBounds2D().getHeight() / 2);
		affineTransform.scale(scale, scale);
		affineTransform.translate(-area.getBounds2D().getCenterX(), -area.getBounds2D().getCenterY());

		area.transform(affineTransform);
		smallest.transform(affineTransform);
		
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();

		graphics.setColor(Color.BLUE);
		graphics.fill(area);
		graphics.setColor(Color.RED);
		graphics.fill(smallest);
		
		display.setImage(image);
	}
}
