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
	/*
	 * This class generates and stores a reduced graph representing a cell-based
	 * maze, where vertices (RGVertex objects) are "super" Cell objects
	 * (junction/checkpoint/order-1 Cell objects) and edges (RGEdge objects) are
	 * paths of order-2 adjacent Cell objects connecting two super Cell objects.
	 */

	/*
	 * Maps the super Cell objects of the maze to their corresponding RGVertex
	 * object in the reduced graph. The Map interface is used rather than a
	 * concrete class such as HashMap because it separates the actual
	 * implementation of the Map interface from this class's use of the
	 * interface's methods, allowing the implementation to change (say, from
	 * HashMap to Hashtable) in the future. This is composition as the
	 * ReducedGraph class has a HAS-A relationship with the RGVertex class and
	 * the RGVertex objects in the cellsToVerticesMap hash table will be
	 * destroyed if the ReducedGraph object is destroyed. This is also
	 * aggregation as the ReducedGraph class has a HAS-A relationship with the
	 * Cell class but the Cell objects in the cellsToVerticesMap hash table will
	 * not be destroyed if the ReducedGraph object is destroyed.
	 */
	private final Map<Cell, RGVertex> cellsToVerticesMap;

	/*
	 * The RGVertex objects of the reduced graph that represent super Cell
	 * objects that are checkpoints (checkpoint Cell objects). This is
	 * composition as the ReducedGraph class has a HAS-A relationship with the
	 * RGVertex class and the RGVertex objects in the CheckpointVertices list
	 * will be destroyed if the ReducedGraph object is destroyed.
	 */
	private final List<RGVertex> checkpointVertices;

	/*
	 * Constructor. The cell parameter must be any Cell object in the original
	 * maze and is always made a RGVertex object in the reduced graph.
	 */
	public ReducedGraph(Cell cell) {

		/*
		 * A HashMap implementation is used because it has constant look-up time
		 * complexity.
		 */
		cellsToVerticesMap = new HashMap<Cell, RGVertex>();

		/*
		 * An ArrayList implementation is used because it is efficient with
		 * respect to memory and iteration time complexity.
		 */
		checkpointVertices = new ArrayList<RGVertex>();

		/*
		 * Generates the reduced graph, starting from the cell parameter (which
		 * is made an RGVertex object in the reduced graph).
		 */
		reduceGraph(new HashSet<Cell>(), new RGVertex(cell));
	}

	/*
	 * Recursively creates the reduced graph using depth-first traversal and
	 * populates the cellsToVerticesMap hash table with all the super Cell
	 * objects and their corresponding RGVertex objects. Adjacencies between
	 * RGVertex objects are found and set up as well. Populates the
	 * CheckpointVertices set with RGVertex objects of super Cell objects that
	 * are checkpoints.
	 */
	private void reduceGraph(Set<Cell> visitedCells, RGVertex currentVertex) {
		/*
		 * The visitedCells set is used to keep track of Cell objects that have
		 * already been visited in the traversal, so that we don’t visit these
		 * Cell objects again (there are exceptions explained in the
		 * pseudocode).. The currentVertex parameter is the RGVertex object from
		 * which the algorithm tries to traverse paths of order-2 Cell objects,
		 * building RGEdge objects, to the next super Cell object(s), for which
		 * an RGVertex object will be created and from which the algorithm with
		 * recurse.
		 */

		/*
		 * When the algorithm recurses from a newly visited RGVertex object
		 * (currentVertex), we store it by adding the super Cell object it
		 * represents and currentVertex to cellsToVerticesMap
		 */
		cellsToVerticesMap.put(currentVertex.getSuperCell(), currentVertex);

		/*
		 * We add the RGVertex object to checkpointVertices if it represents a
		 * checkpoint Cell object (i.e. if it is a checkpoint RGVertex object)
		 * so that we do not have to iterate over the cellsToVerticeMap after
		 * creating the ReducedGraph to find the checkpoint RGVertex objects.
		 */
		if (currentVertex.getSuperCell().isCheckpoint()) {
			checkpointVertices.add(currentVertex);
		}

		/*
		 * As the Cell object represented by currentVertex has now been visited,
		 * we add it to visitedCells.
		 */
		visitedCells.add(currentVertex.getSuperCell());

		/*
		 * The algorithm now iterates through each adjacent Cell object of the
		 * super cell represented by currentVertex. It then traverses along the
		 * path of order-2 Cell objects from that adjacent Cell object, building
		 * an RGEdge object, until it reaches a super Cell object, at which
		 * point the algorithm makes currentVertex adjacent to an RGVertex
		 * object representing the reached super Cell object with the edge
		 * between them being the built RGEdge object. The algorithm then
		 * recurses from the RGVertex object representing the reached super Cell
		 * object. After it finishes recursing and backtracks up to this
		 * recursive call, it continues with the iteration.
		 */
		for (Cell adjacentCell : currentVertex.getSuperCell()
				.getAdjacentCells()) {

			/*
			 * If adjacentCell has been visited already, then there is no need
			 * to traverse along this path again as the RGEdge object for this
			 * path of Cell objects would have already been built and
			 * currentVertex would have already been made adjacent with an
			 * RGVertex object (which would have already been visited)
			 * representing the super Cell object at the other end. The only
			 * exception to this is if adjacentCell is a super Cell object
			 * itself. In this case, even if adjacentCell has been visited, the
			 * RGVertex object representing it may not have been made adjacent
			 * with currentVertex or even if it has, a shorter RGEdge could be
			 * built due to the nature of depth-first traversal not traversing
			 * all the possible paths from a super Cell object before recursing.
			 * Thus, in this case, there is a need to traverse along this path.
			 */
			if (!visitedCells.contains(adjacentCell)
					|| cellsToVerticesMap.containsKey(adjacentCell)) {
				/*
				 * The following part of the pseudocode builds an RGEdge object
				 * consisting of the super Cell objects on either end of it and
				 * the order-2 Cell objects between them.
				 */
				RGEdge edge = new RGEdge();
				Cell previousCell = currentVertex.getSuperCell();
				Cell nextCell = adjacentCell;
				edge.appendCell(previousCell);

				/*
				 * This loop iterates along the path of order-2 Cell objects,
				 * appending them onto edge, until a super Cell object is found
				 */
				while (nextCell.getOrder() == 2 && !nextCell.isCheckpoint()) {
					edge.appendCell(nextCell);

					/*
					 * As nextCell has now been visited, we add it to
					 * visitedCells.
					 */
					visitedCells.add(nextCell);

					/*
					 * This loop retrieves the adjacent Cell object of nextCell
					 * that is not previousCell. It makes nextCell this Cell
					 * object and makes previousCell what used to be nextCell,
					 * so that the path can be moved along.
					 */
					for (Cell nextCell2 : nextCell.getAdjacentCells()) {
						if (nextCell2 != previousCell) {
							previousCell = nextCell;
							nextCell = nextCell2;
							break;
						}
					}
				}

				/*
				 * As the loop has terminated, nextCell must be a super Cell
				 * object. If this Cell object is the same as the super Cell
				 * object represented by currentVertex, then we have traversed
				 * along a loop of Cell objects starting and ending at the same
				 * Cell object. If this is the case, we do not need to do
				 * anything as such self-loops are to be omitted from the
				 * reduced graph anyway.
				 */
				if (nextCell != currentVertex.getSuperCell()) {

					/*
					 * After appending nextCell to Edge, we have finished
					 * building the RGEdge object consisting of all the order-2
					 * Cell objects between the two super Cell objects
					 * (currentVertexCell and nextCell)
					 */
					edge.appendCell(nextCell);

					RGVertex nextVertex;

					/*
					 * If the super Cell object nextCell has already been
					 * visited, then an RGVertex object representing it already
					 * exists in the cellsToVerticesMap hash table and we should
					 * use this object as nextVertex instead of creating a new
					 * RGVertex object for nextCell. We also need not recurse
					 * from nextVertex because it has already been visited and
					 * so the algorithm will eventually backtrack to it.
					 */
					if (cellsToVerticesMap.containsKey(nextCell)) {
						nextVertex = cellsToVerticesMap.get(nextCell);

						/*
						 * If currentVertex is already adjacent to nextVertex,
						 * then if edge has a smaller weight than the RGEdge
						 * object already connecting two RGVertex objects, we
						 * need to replace that RGEdge object with edge.
						 */
						if (nextVertex.isAdjacentTo(currentVertex)) {
							if (edge.getTotalWeight() < nextVertex
									.getWeightToAdjacentVertex(currentVertex)) {
								nextVertex.setEdgeTo(currentVertex, edge);
							}
						}

						/*
						 * Otherwise, if currentVertex is not adjacent to
						 * nextVertex, then we can just set nextVertex adjacent
						 * to currentVertex with edge as the RGEdge object
						 * connecting them.
						 */
						else {
							currentVertex.addAdjacentVertex(nextVertex, edge);
						}
					}
					/*
					 * Otherwise, if nextCell has not been visited, then we have
					 * to create a new RGVertex object for it and we need to
					 * recurse from it.
					 */
					else {
						nextVertex = new RGVertex(nextCell);

						/*
						 * We set nextVertex adjacent to currentVertex with edge
						 * as the RGEdge object connecting them.
						 */
						currentVertex.addAdjacentVertex(nextVertex, edge);

						/*
						 * Recurse from NextVertex.
						 */
						reduceGraph(visitedCells, nextVertex);
					}
				}
			}
		}
	}

	/*
	 * Getters.
	 */

	public Collection<RGVertex> getVertices() {
		return Collections.unmodifiableCollection(cellsToVerticesMap.values());
	}

	public List<RGVertex> getCheckpointVertices() {
		return Collections.unmodifiableList(checkpointVertices);
	}

	public RGVertex getVertex(Cell cell) {

		/*
		 * If cell is not in cellsToVerticesMap, then there is a logical error
		 * elsewhere in the program, so a RuntimeException is thrown to quit the
		 * program and make debugging and tracing the error easier. A try-catch
		 * block would not be useful here as the issue cannot be fixed without
		 * changing the code.
		 */
		if (!cellsToVerticesMap.containsKey(cell)) {
			throw new RuntimeException();
		}

		return cellsToVerticesMap.get(cell);
	}

}