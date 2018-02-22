package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DisjointSet<T> {
	/*
	 * This class is an advanced data structure that efficiently maintains a set
	 * of elements split up into subsets that do not have elements in common
	 * (i.e. disjoint subsets). The purpose of using such a data structure is to
	 * be able to efficiently check if two elements are in the same subset (the
	 * areJoined function) and to efficiently combine two subsets together (the
	 * join function).
	 */

	private class TreeVertex {
		/*
		 * Objects of this inner class make up a tree data structure. Each TreeVertex
		 * object is a vertex of a tree and stores a variable pointing to its
		 * parent vertex.
		 */

		/*
		 * The parent TreeVertex of this TreeVertex object in the tree. If
		 * parent points to this TreeVertex object, then this TreeVertex object
		 * is the root of its tree. This is recursive aggregation as the
		 * TreeVertex class has a HAS-A relationship with the TreeVertex class
		 * and the parent object will be destroyed if the TreeVertex object is
		 * destroyed.
		 */
		private TreeVertex parent;

		/*
		 * The rank of this TreeVertex object, which is effectively the height
		 * of the subtree (for which this TreeVertex object is the root)
		 * disregarding path compression (See the join method for more detail).
		 */
		private int rank;

		/*
		 * Constructor. Effectively creates a tree consisting of just this
		 * TreeVertex object.
		 */
		public TreeVertex() {
			parent = this;
			rank = 0;
		}

		/*
		 * Finds the root TreeVertex object of this TreeVertex object’s tree.
		 */
		public TreeVertex findRoot() {

			/*
			 * If this TreeVertex object has a parent that is not itself, then
			 * this TreeVertex object is not the root of the tree, so we
			 * traverse up the tree to the parent and recurse. Otherwise, this
			 * TreeVertex object is the root of the tree and we just return
			 * itself.
			 */
			if (parent != this) {

				/*
				 * We assign the root to the parent variable because each
				 * TreeVertex object visited on the way to the root is part of
				 * the same tree, and so all of these objects can be attached
				 * directly under the root (i.e. the root can be made the parent
				 * of all of these objects). This makes the tree flatter, which
				 * increases the efficiency of future findRoot operations as the
				 * recursion depth would be smaller. This idea is called path
				 * compression.
				 */
				parent = parent.findRoot();
			}

			return parent;
		}

		/*
		 * Joins the trees that this TreeVertex object and otherVertex are in.
		 */
		public void join(TreeVertex otherVertex) {

			/*
			 * The roots of the trees that this TreeVertex object and
			 * otherVertex are in.
			 */
			TreeVertex thisRoot = findRoot();
			TreeVertex otherRoot = otherVertex.findRoot();

			/*
			 * If the two TreeVertex objects are in the same tree (i.e. if their
			 * roots are the same), then we do not need to join them.
			 */
			if (thisRoot != otherRoot) {

				/*
				 * Attaches the shorter tree to the root of the taller tree by
				 * assigning the parent of the root of the shorter tree as the
				 * root of the taller tree.
				 */
				if (thisRoot.rank < otherRoot.rank) {
					thisRoot.parent = otherRoot;
				} else if (thisRoot.rank > otherRoot.rank) {
					otherRoot.parent = thisRoot;
				}
				/*
				 * If the trees are equally tall, then it doesn’t matter which
				 * root is attached to which, but the resulting tree height
				 * increases by 1, so the rank of the root of the resulting tree
				 * needs to be incremented.
				 */
				else {
					thisRoot.parent = otherRoot;
					otherRoot.rank++;
				}
			}
		}
	}

	/*
	 * A Map object is used as a hash table that maps the elements stored in the
	 * data structure to their corresponding TreeVertex objects, which are used
	 * to store the information relating to the subsets in which the elements
	 * are. The Map interface is used rather than a concrete class such as
	 * HashMap because it separates the actual implementation of the Map
	 * interface from this class's use of the interface's methods, allowing the
	 * implementation to change (say, from HashMap to Hashtable) in the
	 * future. This is composition as the DisjointSet class has a HAS-A
	 * relationship with the TreeVertex class and the TreeVertex objects in the
	 * itemsToVerticesMap hash table will be destroyed if the DisjointSet object
	 * is destroyed. This is also aggregation as the DisjointSet class has a
	 * HAS-A relationship with the class of the generic type (T), but these T
	 * objects in the itemsToVerticesMap hash table will not be destroyed if the
	 * DisjointSet object is destroyed.
	 */
	private final Map<T, TreeVertex> itemsToVerticesMap;

	/*
	 * Constructor. Initialises the itemsToVerticesMap hash table. Creates a
	 * TreeVertex object for each element in the items parameter and puts a
	 * corresponding entry in the itemsToVerticesMap hash table.
	 */
	public DisjointSet(Collection<T> items) {
		itemsToVerticesMap = new HashMap<T, TreeVertex>();
		for (T item : items) {
			itemsToVerticesMap.put(item, new TreeVertex());
		}
	}

	/*
	 * Another constructor that only initialises the itemsToVerticesMap hash
	 * table.
	 */
	public DisjointSet() {
		itemsToVerticesMap = new HashMap<T, TreeVertex>();
	}

	/*
	 * Creates a TreeVertex object for the item parameter and puts a
	 * corresponding entry in the itemsToVerticesMap hash table.
	 */
	public void add(T item) {
		itemsToVerticesMap.put(item, new TreeVertex());
	}

	/*
	 * Joins the trees that the TreeVertex objects representing the parameters
	 * are in. See the join method in the TreeVertex class.
	 */
	public void join(T t1, T t2) {
		itemsToVerticesMap.get(t1).join(itemsToVerticesMap.get(t2));
	}

	/*
	 * Checks if the TreeVertex objects representing the parameters are in the
	 * same trees (i.e. have the same roots). See the findRoot method in the
	 * TreeVertex class.
	 */
	public boolean areJoined(T t1, T t2) {
		return itemsToVerticesMap.get(t1).findRoot() == itemsToVerticesMap.get(
				t2).findRoot();
	}
}
