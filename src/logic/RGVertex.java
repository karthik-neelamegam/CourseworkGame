package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import map.Cell;

public class RGVertex {
	// no need for constant time check if a vertex is adjacent in dijksta's
	// so don't need hashmap/hashset
	private final List<RGAdjacency> rgAdjacencies;
	private final Cell superCell;

	public RGVertex(Cell superCell) {
		this.superCell = superCell;
		rgAdjacencies = new ArrayList<RGAdjacency>();
	}

	public void addAdjacentVertex(RGVertex otherVertex, RGEdge edge) {
		if (otherVertex != this) {
			rgAdjacencies.add(new RGAdjacency(otherVertex, edge));
			otherVertex.rgAdjacencies.add(new RGAdjacency(this, edge));
		}
	}

	public Cell getSuperCell() {
		return superCell;
	}

	public List<RGAdjacency> getAdjacencies() {
		return Collections.unmodifiableList(rgAdjacencies);
	}

	// reminder: make everything distance or weight (consistent)
	public double getWeightToAdjacentVertex(RGVertex adjacentVertex) {
		return getEdgeTo(adjacentVertex).getTotalWeight();
	}

	public void setEdgeTo(RGVertex adjacentVertex, RGEdge edge) {
		for(RGAdjacency adjacency : rgAdjacencies) {
			if(adjacency.getAdjacentVertex() == adjacentVertex) {
				adjacency.setEdge(edge);
				return;
			}
		}
	}
	
	public RGEdge getEdgeTo(RGVertex adjacentVertex) {
		for(RGAdjacency adjacency : rgAdjacencies) {
			if(adjacency.getAdjacentVertex() == adjacentVertex) {
				return adjacency.getEdge();
			}
		}
		return null;
	}

	public int getOrder() {
		return rgAdjacencies.size();
	}
	
	public boolean isAdjacentTo(RGVertex otherVertex) {
		for(RGAdjacency adjacency : rgAdjacencies) {
			if(adjacency.getAdjacentVertex() == otherVertex) {
				return true;
			}
		}
		return false;
	}
}
