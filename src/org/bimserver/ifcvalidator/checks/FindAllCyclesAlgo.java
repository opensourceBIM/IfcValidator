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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.Pseudograph;

public class FindAllCyclesAlgo<V extends Comparable<V>, E> {

	private Pseudograph<V, E> graph;
	private Set<Cycle<V>> cycles = new HashSet<>();
	private ThreadPoolExecutor threadPoolExecutor;
	
	public FindAllCyclesAlgo(Pseudograph<V, E> graph) {
		this.graph = graph;
	}
	
	public List<Set<V>> findAllCycles() {
		if (graph.vertexSet().size() == 0) {
			return new ArrayList<>();
		}
		List<Set<V>> result = new ArrayList<>();
		
		threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 1, TimeUnit.HOURS, new ArrayBlockingQueue<>(graph.vertexSet().size()));
		
		System.out.println("Looking for cycles in " + graph.vertexSet().size() + " vertices" + ", " + graph.edgeSet().size() + " edges");
		
		for (V v : graph.vertexSet()) {
			threadPoolExecutor.submit(new CycleFinder<>(graph, v, cycles));
		}
		
		threadPoolExecutor.shutdown();
		try {
			threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Found " + cycles.size() + " cycles");
		
//		for (Cycle<V> cycle : cycles) {
//			System.out.println(cycle);
//			int innerCycles = 0;
//			for (Cycle<V> inner : cycles) {
//				if (inner != cycle) {
//					if (inner.isPartOf(cycle)) {
//						System.out.println("\t" + inner);
//						innerCycles++;
//					}
//				}
//			}
//			if (innerCycles == 0) {
//				result.add(cycle.asSet());
//			}
//		}
		
		for (Cycle<V> cycle : cycles) {
			result.add(cycle.asSet());
		}
		
		return result;
	}
}
