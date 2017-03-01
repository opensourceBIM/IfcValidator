package org.bimserver.ifcvalidator.checks;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

public class Cycle<V extends Comparable<V>> {

	private LinkedHashSet<V> vertices;

	public Cycle() {
		vertices = new LinkedHashSet<>();
	}

	public Cycle(LinkedHashSet<V> path) {
		this.vertices = path;
	}

	public Cycle(V...vs) {
		vertices = new LinkedHashSet<>();
		for (V v : vs) {
			add(v);
		}
	}

	public void add(V v) {
		vertices.add(v);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cycle<V> other = (Cycle<V>) obj;
		if (vertices == null) {
			if (other.vertices != null)
				return false;
		} else if (!vertices.equals(other.vertices))
			return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public Cycle<V> getCanonical() {
		V max = null;
		for (V v : vertices) {
			if (max == null || v.compareTo(max) < 0) {
				max = v;
			}
		}
		Object[] array = vertices.toArray();
		int start = 0;
		for (int i=0; i<array.length; i++) {
			if (array[i] == max) {
				start = i;
				break;
			}
		}
		LinkedHashSet<V> result = new LinkedHashSet<>();
		result.add((V) array[start]);

		boolean reverse = ((V)(array[(start + 1) % array.length])).compareTo((V)(array[(start + 2) % array.length])) > 0;
		if (reverse) {
			for (int i=start - 1 + array.length; i>start; i--) {
				result.add((V) array[i % array.length]);
			}
		} else {
			for (int i=start + 1; i<start + array.length; i++) {
				result.add((V) array[i % array.length]);
			}
		}
		
		return new Cycle<>(result);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (V v : vertices) {
			sb.append(v.toString() + " ");
		}
		return sb.toString();
	}

	public int size() {
		return vertices.size();
	}

	public boolean isPartOf(Cycle<V> cycle) {
		for (V v : vertices) {
			if (!cycle.contains(v)) {
				return false;
			}
		}
		return true;
	}

	private boolean contains(V v) {
		return vertices.contains(v);
	}

	public Set<V> asSet() {
		return vertices;
	}

	public static <V extends Comparable<V>> Cycle<V> canonical(Stack<V> path) {
		V max = null;
		int index = 0;
		int maxIndex = 0;
		for (V v : path) {
			if (max == null || v.compareTo(max) < 0) {
				max = v;
				maxIndex = index;
			}
			index++;
		}
		Object[] array = path.toArray();
		LinkedHashSet<V> result = new LinkedHashSet<>();
		result.add((V) array[maxIndex]);

		boolean reverse = ((V)(array[(maxIndex + 1) % array.length])).compareTo((V)(array[(maxIndex + 2) % array.length])) > 0;
		if (reverse) {
			for (int i=maxIndex - 1 + array.length; i>maxIndex; i--) {
				result.add((V) array[i % array.length]);
			}
		} else {
			for (int i=maxIndex + 1; i<maxIndex + array.length; i++) {
				result.add((V) array[i % array.length]);
			}
		}
		return new Cycle<>(result);
	}
}