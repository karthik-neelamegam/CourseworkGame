package logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import map.Cell;

public class ReducedGraph {

	private final Map<Cell, RGVertex> cellsToVerticesMap;
	private final List<RGVertex> checkpointVertices;

	// the cell parameter is always made a vertex
	public ReducedGraph(Cell cell) {
		cellsToVerticesMap = new HashMap<Cell, RGVertex>();
		checkpointVertices = new ArrayList<RGVertex>();
		reduceGraph(new HashSet<Cell>(), new RGVertex(cell));
	}

	// graph must be connected
	// using cell/vertex map rather than vertex set for faster look up time when
	// getting the initialised vertex of a cell

	private void reduceGraph(Set<Cell> visitedCells, RGVertex currentVertex) {
		cellsToVerticesMap.put(currentVertex.getSuperCell(), currentVertex);
		if (currentVertex.getSuperCell().isCheckpoint()) {
			checkpointVertices.add(currentVertex);
		}
		visitedCells.add(currentVertex.getSuperCell());

		// iterates through each adjacent cell and recurses (DFS)
		for (Cell adjacentCell : currentVertex.getSuperCell()
				.getAdjacentCells()) {

			// if a cell has been discovered, then it has already been dealt
			// with in the DFS, so ignore it
			// comment on why the part after || is needed
			if (!visitedCells.contains(adjacentCell)
					|| (cellsToVerticesMap.containsKey(adjacentCell) && !currentVertex
							.isAdjacentTo(cellsToVerticesMap.get(adjacentCell)))) {
				// build the edge from this vertex to the next vertex
				// (junction/checkpoint)
				RGEdge edge = new RGEdge();
				Cell previousCell = currentVertex.getSuperCell();
				Cell nextCell = adjacentCell;
				edge.appendCell(previousCell);
				while (nextCell.getOrder() == 2 && !nextCell.isCheckpoint()) {
					visitedCells.add(nextCell);
					edge.appendCell(nextCell);
					for (Cell nextCell2 : nextCell.getAdjacentCells()) {
						if (nextCell2 != previousCell) {
							previousCell = nextCell;
							nextCell = nextCell2;
							break;
						}
					}
				}
				visitedCells.add(nextCell);
				// check if not a loop (redundant, don't need edge)
				if (nextCell != currentVertex.getSuperCell()) {
					edge.appendCell(nextCell);
					RGVertex nextVertex;
					boolean recurse = false;
					if (cellsToVerticesMap.containsKey(nextCell)) {
						nextVertex = cellsToVerticesMap.get(nextCell);
						if (nextVertex.isAdjacentTo(currentVertex)) {
							if (edge.getTotalWeight() < nextVertex
									.getWeightToAdjacentVertex(currentVertex)) {
								nextVertex.setEdgeTo(currentVertex, edge);
							}
						}
					} else {
						nextVertex = new RGVertex(nextCell);
						recurse = true;
					}
					currentVertex.addAdjacentVertex(nextVertex, edge);
					if (recurse) {
						reduceGraph(visitedCells, nextVertex);
					}
				}
			}
		}
	}

	public Collection<RGVertex> getVertices() {
		return Collections.unmodifiableCollection(cellsToVerticesMap.values());
	}

	public List<RGVertex> getCheckpointVertices() {
		return Collections.unmodifiableList(checkpointVertices);
	}

	public RGVertex getVertex(Cell cell) {
		// may throw error, but i wanna know
		return cellsToVerticesMap.get(cell);
	}

}