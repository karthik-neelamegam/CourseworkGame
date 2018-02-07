package logic;


public class RGAdjacency {
	private final RGVertex adjacentVertex;
	private final RGEdge edge;
	public RGAdjacency(RGVertex adjacentVertex, RGEdge edge) {
		this.adjacentVertex = adjacentVertex;
		this.edge = edge;
	}
	public RGVertex getAdjacentVertex() {
		return adjacentVertex;
	}
	public RGEdge getEdge() {
		return edge;
	}
}
