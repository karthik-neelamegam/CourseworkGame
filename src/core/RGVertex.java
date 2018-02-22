package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RGVertex {
	/*
	 * This class is used to represent a vertex (i.e. a super Cell object) in a
	 * reduced graph.
	 */

	/*
	 * A list of the RGAdjacency objects consisting of an RGVertex object (which
	 * is adjacent to this RGVertex object) and the RGEdge to it from this
	 * RGVertex object. This is effectively an adjacency list for the reduced
	 * graph. The List interface is used rather than a concrete class such as
	 * ArrayList because it separates the actual implementation of the List
	 * interface from this class's use of the interface's methods, allowing the
	 * implementation to change (say, from ArrayList to LinkedList) in the
	 * future. This is composition as the RGVertex class has a HAS-A
	 * relationship with the RGAdjacency class and the RGAdjacency objects in
	 * the rgAdjacencies list will be destroyed if the RGVertex object is
	 * destroyed.
	 */
	private final List<RGAdjacency> rgAdjacencies;

	/*
	 * The super Cell object that this object represents. This is aggregation as
	 * the RGVertex class has a HAS-A relationship with the Cell class but the
	 * superCell object will not be destroyed if the RGVertex object is
	 * destroyed.
	 */
	private final Cell superCell;

	/*
	 * Constructor.
	 */
	public RGVertex(Cell superCell) {
		this.superCell = superCell;

		/*
		 * An ArrayList implementation is used because it is efficient with
		 * respect to memory and iteration time complexity.
		 */
		rgAdjacencies = new ArrayList<RGAdjacency>();
	}

	/*
	 * Adds an RGAdjacency object consisting of otherVertex and edge to the
	 * rgAdjacencies list and adds an RGAdjacency object consisting of this
	 * RGvertex object and edge to the rgAdjacencies list of otherVertex (as
	 * this is effectively an undirected graph, so all adjacencies must be
	 * two-way).
	 */
	public void addAdjacentVertex(RGVertex otherVertex, RGEdge edge) {

		/*
		 * If the Cell objects on either end of edge are not superCell and
		 * otherVertex.superCell, then edge cannot be connecting these two
		 * RGVertex objects. Thus, there is a logical error elsewhere in the
		 * program, so a RuntimeException is thrown to quit the program and make
		 * debugging and tracing the error easier. A try-catch block would not
		 * be useful here as the issue cannot be fixed without changing the
		 * code.
		 */
		List<Cell> edgeCells = edge.getCells();
		if (!(edgeCells.get(0) == superCell && edgeCells
				.get(edgeCells.size() - 1) == otherVertex.superCell)
				&& !(edgeCells.get(0) == otherVertex.superCell && edgeCells
						.get(edgeCells.size() - 1) == superCell)) {
			throw new RuntimeException();
		}

		/*
		 * If otherVertex is this RGVertex object, then there is no point making
		 * it adjacent to itself.
		 */
		if (otherVertex != this) {
			rgAdjacencies.add(new RGAdjacency(otherVertex, edge));
			otherVertex.rgAdjacencies.add(new RGAdjacency(this, edge));
		}
	}

	/*
	 * Returns the weight of the RGEdge object connecting this RGVertex object
	 * to adjacentVertex.
	 */
	public double getWeightToAdjacentVertex(RGVertex adjacentVertex) {
		return getEdgeTo(adjacentVertex).getTotalWeight();
	}

	/*
	 * Sets the RGEdge object connecting this RGVertex object and adjacentVertex
	 * (where adjacentVertex is already connected to this object) to edge.
	 */
	public void setEdgeTo(RGVertex adjacentVertex, RGEdge edge) {
		for (RGAdjacency adjacency : rgAdjacencies) {
			if (adjacency.getAdjacentVertex() == adjacentVertex) {
				adjacency.setEdge(edge);
				return;
			}
		}
		for (RGAdjacency adjacency : adjacentVertex.rgAdjacencies) {
			if (adjacency.getAdjacentVertex() == this) {
				adjacency.setEdge(edge);
				return;
			}
		}
		/*
		 * Program flow should only reach here if adjacentVertex is not adjacent
		 * to this RGVertex object, which should not happen. Thus, there is a
		 * logical error elsewhere in the program, so a RuntimeException is
		 * thrown to quit the program and make debugging and tracing the error
		 * easier. A try-catch block would not be useful here as the issue
		 * cannot be fixed without changing the code.
		 */
		throw new RuntimeException();
	}

	/*
	 * Returns the RGEdge object connecting this RGVertex object to
	 * adjacentVertex.
	 */
	public RGEdge getEdgeTo(RGVertex adjacentVertex) {
		for (RGAdjacency adjacency : rgAdjacencies) {
			if (adjacency.getAdjacentVertex() == adjacentVertex) {
				return adjacency.getEdge();
			}
		}

		/*
		 * Program flow should only reach here if adjacentVertex is not adjacent
		 * to this RGVertex object, which should not happen. Thus, there is a
		 * logical error elsewhere in the program, so a RuntimeException is
		 * thrown to quit the program and make debugging and tracing the error
		 * easier. A try-catch block would not be useful here as the issue
		 * cannot be fixed without changing the code.
		 */
		throw new RuntimeException();
	}

	/*
	 * Returns whether otherVertex is in an RGAdjacency object in the
	 * rgAdjacencies list.
	 */
	public boolean isAdjacentTo(RGVertex otherVertex) {
		for (RGAdjacency adjacency : rgAdjacencies) {
			if (adjacency.getAdjacentVertex() == otherVertex) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Getters.
	 */

	public Cell getSuperCell() {
		return superCell;
	}

	public List<RGAdjacency> getAdjacencies() {
		return Collections.unmodifiableList(rgAdjacencies);
	}

	public int getOrder() {
		return rgAdjacencies.size();
	}
}
