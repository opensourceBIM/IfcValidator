package org.bimserver.ifcvalidator.tests;

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
import java.util.Set;

import org.bimserver.ifcvalidator.checks.Cycle;
import org.bimserver.ifcvalidator.checks.FindAllCyclesAlgo;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

public class TestCycles {
	
	public static final class V implements Comparable<V> {

		private String name;

		public V(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}

		@Override
		public int compareTo(V o) {
			return name.compareTo(o.name);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			V other = (V) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	public static final class E {
		
	}
	
	@Test
	public void simpleTest() {
		EdgeFactory<V, E> factory = new ClassBasedEdgeFactory<>(E.class);
		Pseudograph<V, E> graph = new Pseudograph<>(factory);
		
		V a = new V("a");
		V b = new V("b");
		V c = new V("c");
		V d = new V("d");
	
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, a);
		
		FindAllCyclesAlgo<V, E> algo = new FindAllCyclesAlgo<>(graph);
		List<Set<V>> cycles = algo.findAllCycles();
	}
	
	@Test
	public void testCanonical() {
		V a = new V("a");
		V b = new V("b");
		V c = new V("c");
		V d = new V("d");
		V e = new V("e");
		V f = new V("f");
		
		Cycle<V> cycle = new Cycle<>(d, c, b, a);
		System.out.println(cycle.getCanonical());
		
		cycle = new Cycle<>(b, c, d, a);
		System.out.println(cycle.getCanonical());

		cycle = new Cycle<>(c, b, a, d);
		System.out.println(cycle.getCanonical());
	}
	
	@Test
	public void simpleTest2() {
		EdgeFactory<V, E> factory = new ClassBasedEdgeFactory<>(E.class);
		Pseudograph<V, E> graph = new Pseudograph<>(factory);
		
		V a = new V("a");
		V b = new V("b");
		V c = new V("c");
		V d = new V("d");
		V e = new V("e");
		V f = new V("f");
		
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		graph.addVertex(e);
		graph.addVertex(f);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, a);
		graph.addEdge(b, e);
		graph.addEdge(e, f);
		graph.addEdge(f, c);
		
		FindAllCyclesAlgo<V, E> algo = new FindAllCyclesAlgo<>(graph);
		List<Set<V>> cycles = algo.findAllCycles();
	}
	
	@Test
	public void test() {
		EdgeFactory<V, E> factory = new ClassBasedEdgeFactory<>(E.class);
		Pseudograph<V, E> graph = new Pseudograph<>(factory);

		V a = new V("a");
		V b = new V("b");
		V c = new V("c");
		V d = new V("d");
		V e = new V("e");
		V f = new V("f");
		V g = new V("g");
		V h = new V("h");
		V i = new V("i");
		V j = new V("j");
		V k = new V("k");
		V l = new V("l");
		V m = new V("m");
		V n = new V("n");
		V o = new V("o");
		V p = new V("p");
		V q = new V("q");
		V r = new V("r");
		V s = new V("s");
		V t = new V("t");
		V u = new V("u");
		V v = new V("v");
		V w = new V("w");
		V x = new V("x");
		V y = new V("y");
		
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		graph.addVertex(e);
		graph.addVertex(f);
		graph.addVertex(g);
		graph.addVertex(h);
		graph.addVertex(i);
		graph.addVertex(j);
		graph.addVertex(k);
		graph.addVertex(l);
		graph.addVertex(m);
		graph.addVertex(n);
		graph.addVertex(o);
		graph.addVertex(p);
		graph.addVertex(q);
		graph.addVertex(r);
		graph.addVertex(s);
		graph.addVertex(t);
		graph.addVertex(u);
		graph.addVertex(v);
		graph.addVertex(w);
		graph.addVertex(x);
		graph.addVertex(y);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(a, e);
		graph.addEdge(c, q);
		graph.addEdge(d, p);
		graph.addEdge(q, p);
		graph.addEdge(q, r);
		graph.addEdge(p, o);
		graph.addEdge(r, o);
		graph.addEdge(o, n);
		graph.addEdge(n, m);
		graph.addEdge(e, j);
		graph.addEdge(j, i);
		graph.addEdge(i, k);
		graph.addEdge(k, l);
		graph.addEdge(m, l);
		graph.addEdge(m, y);
		graph.addEdge(y, x);
		graph.addEdge(x, v);
		graph.addEdge(v, w);
		graph.addEdge(v, s);
		graph.addEdge(s, t);
		graph.addEdge(f, g);
		graph.addEdge(f, u);
		graph.addEdge(u, t);
		graph.addEdge(e, f);
		graph.addEdge(b, f);
		graph.addEdge(h, i);
		graph.addEdge(s, r);
		
		FindAllCyclesAlgo<V, E> algo = new FindAllCyclesAlgo<>(graph);
		List<Set<V>> cycles = algo.findAllCycles();
	}
	
	@Test
	public void containtTest() {
		EdgeFactory<V, E> factory = new ClassBasedEdgeFactory<>(E.class);
		Pseudograph<V, E> graph = new Pseudograph<>(factory);

		V a = new V("a");
		V b = new V("b");
		V c = new V("c");
		V d = new V("d");
		V e = new V("e");
		V f = new V("f");
		V g = new V("g");
		
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		graph.addVertex(e);
		graph.addVertex(f);
		graph.addVertex(g);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, g);
		graph.addEdge(g, d);
		graph.addEdge(d, e);
		graph.addEdge(e, a);
		graph.addEdge(e, f);
		graph.addEdge(f, g);
		
		FindAllCyclesAlgo<V, E> algo = new FindAllCyclesAlgo<>(graph);
		List<Set<V>> cycles = algo.findAllCycles();
		
	}
}
