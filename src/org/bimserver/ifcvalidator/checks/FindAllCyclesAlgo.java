package org.bimserver.ifcvalidator.checks;

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
		List<Set<V>> result = new ArrayList<>();
		
		threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 1, TimeUnit.HOURS, new ArrayBlockingQueue<>(graph.vertexSet().size()));
		
		System.out.println("Looking for cycles in " + graph.vertexSet().size() + " vertices");
		
		for (V v : graph.vertexSet()) {
			threadPoolExecutor.submit(new CycleFinder<>(graph, v, cycles));
		}
		
		threadPoolExecutor.shutdown();
		try {
			threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
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
		
		return result;
	}
}
