package logic;

public class RGAdjacency {
	/*
	 * This class is essentially a container storing two variables: an adjacent
	 * RGVertex object and the RGEdge object to this Cell object.
	 */

	/*
	 * The adjacent RGVertex object. This is aggregation as the RGAdjacency
	 * class has a HAS-A relationship with the RGVertex class but the
	 * adjacentVertex object will not be destroyed if the RGAdjacency object is
	 * destroyed.
	 */
	private final RGVertex adjacentVertex;

	/*
	 * The RGEdge object to adjacentVertex from the RGVertex object where this
	 * RGAdjacency object would be stored. This is aggregation as the
	 * RGAdjacency class has a HAS-A relationship with the RGEdge class but the
	 * edge object will not be destroyed if the RGAdjacency object is destroyed.
	 */
	private RGEdge edge;

	/*
	 * Constructor.
	 */
	public RGAdjacency(RGVertex adjacentVertex, RGEdge edge) {
		this.adjacentVertex = adjacentVertex;
		this.edge = edge;
	}
	
	/*
	 * Getters and setters.
	 */
	
	public RGVertex getAdjacentVertex() {
		return adjacentVertex;
	}

	public RGEdge getEdge() {
		return edge;
	}

	public void setEdge(RGEdge edge) {
		this.edge = edge;
	}
}
