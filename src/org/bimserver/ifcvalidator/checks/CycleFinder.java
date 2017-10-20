package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.graph.Pseudograph;

public class CycleFinder<V extends Comparable<V>, E> implements Runnable {

	private Pseudograph<V, E> graph;
	private V v;
	private Set<Cycle<V>> cycles;

	public CycleFinder(Pseudograph<V, E> graph, V v, Set<Cycle<V>> cycles) {
		this.graph = graph;
		this.v = v;
		this.cycles = cycles;
	}

	@Override
	public void run() {
		branchOut(v, v, null, new HashSet<>(), new Stack<V>());
	}
	
	private void branchOut(V start, V current, E previousEdge, Set<V> done, Stack<V> path) {
		path.push(current);
		done.add(current);
		for (E e : graph.edgesOf(current)) {
			if (e != previousEdge) {
				V target = getOpposed(current, e);
				if (target == start) {
					Cycle<V> canonical = Cycle.canonical(path);
					cycles.add(canonical);
				} else {
					if (!done.contains(target)) {
						branchOut(start, target, e, done, path);
					}
				}
			}
		}
		done.remove(current);
		path.pop();
	}

	private V getOpposed(V from, E e) {
		if (graph.getEdgeSource(e) == from) {
			return graph.getEdgeTarget(e);
		} else if (graph.getEdgeTarget(e) == from) {
			return graph.getEdgeSource(e);
		}
		return null;
	}
}