package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RGPath {
	/*
	 * This class is used in the AIPlayer class to represent paths (i.e. an
	 * ordered list consisting of the two checkpoint RGVertex objects on either
	 * end and the path of RGVertex objects between them) connecting two
	 * checkpoint RGVertex objects together in a reduced graph.
	 */

	/*
	 * The RGVertex objects that make up the path ordered in the order they are
	 * in the path (although the RGVertex objects could be anything, they should
	 * be the checkpoint RGVertex objects on either end and the path of RGVertex
	 * objects between them). The List interface is used rather than a concrete
	 * class such as ArrayList because it separates the actual implementation of
	 * the List interface from this class's use of the interface's methods,
	 * allowing the implementation to change (say, from ArrayList to LinkedList)
	 * in the future. This is aggregation as the RGPath class has a HAS-A
	 * relationship with the RGVertex class but the RGVertex objects in the
	 * pathVertices list will not be destroyed if the RGPath object is
	 * destroyed.
	 */
	private List<RGVertex> pathVertices;

	/*
	 * The weight of the path, i.e the sum of the weights of the RGEdge objects
	 * between each pair of consecutive RGVertex objects in the pathVertices
	 * list.
	 */
	private double totalWeight;

	/*
	 * Constructor.
	 */
	public RGPath() {

		/*
		 * An ArrayList implementation is used because it is efficient with
		 * respect to memory and iteration time complexity.
		 */
		pathVertices = new ArrayList<RGVertex>();
		totalWeight = 0;
	}

	/*
	 * Adds nextVertex to the end of the pathVertices list and increases
	 * totalWeight by the weight of the RGEdge object between nextVertex and the
	 * previous RGVertex object in the pathVertices list (if there is one).
	 */
	public void appendVertex(RGVertex nextVertex) {
		pathVertices.add(nextVertex);
		if (pathVertices.size() > 1) {
			totalWeight += nextVertex.getWeightToAdjacentVertex(pathVertices
					.get(pathVertices.size() - 2));
		}
	}

	/*
	 * Getters.
	 */
	
	public double getTotalWeight() {
		return totalWeight;
	}

	public RGVertex getVertex1() {
		return pathVertices.get(0);
	}

	public RGVertex getVertex2() {
		return pathVertices.get(pathVertices.size() - 1);
	}

	public List<RGVertex> getPathVertices() {
		return Collections.unmodifiableList(pathVertices);
	}
}
