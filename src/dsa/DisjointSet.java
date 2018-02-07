package dsa;

import java.util.Collection;
import java.util.HashMap;

public class DisjointSet<T> {
	private class TreeVertex {
		private TreeVertex parent;
		private int rank;

		public TreeVertex() {
			parent = this;
			rank = 0;
		}

		public TreeVertex findRoot() {
			if (parent != this) {
				parent = parent.findRoot();
			}
			return parent;
		}

		public void join(TreeVertex other) {
			TreeVertex thisRoot = findRoot();
			TreeVertex otherRoot = other.findRoot();
			if (thisRoot != otherRoot) {
				if (thisRoot.rank < otherRoot.rank) {
					thisRoot.parent = otherRoot;
				} else if (thisRoot.rank > otherRoot.rank) {
					otherRoot.parent = thisRoot;
				} else {
					thisRoot.parent = otherRoot;
					otherRoot.rank++;
				}
			}
		}
	}

	private HashMap<T, TreeVertex> vertices;

	public DisjointSet() {
		vertices = new HashMap<T, TreeVertex>();
	}

	public DisjointSet(Collection<T> items) {
		vertices = new HashMap<T, TreeVertex>();
		for (T item : items) {
			add(item);
		}
	}

	public void add(T item) {
		vertices.put(item, new TreeVertex());
	}

	public void join(T t1, T t2) {
		vertices.get(t1).join(vertices.get(t2));
	}

	public boolean areJoined(T t1, T t2) {
		return vertices.get(t1).findRoot() == vertices.get(t2).findRoot();
	}
}
