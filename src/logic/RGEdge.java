package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import map.Cell;

public class RGEdge {
	/*
	 * This class is used to represent an edge (i.e. an ordered list consisting
	 * of the two super cells on either end and the path of order-2 cells
	 * between them) in a reduced graph.
	 */

	/*
	 * The Cell objects in the edge (although the Cell objects could be
	 * anything, they should be the two super Cell objects on either end and the
	 * path of order-2 Cell objects between them). The List interface is used
	 * rather than a concrete class such as ArrayList because it separates the
	 * actual implementation of the List interface from this class's use of the
	 * interface's methods, allowing the implementation to change (say, from
	 * ArrayList to LinkedList) in the future. This is aggregation as the RGEdge
	 * class has a HAS-A relationship with the Cell class but the Cell objects
	 * in the edgeCells list will not be destroyed if the RGEdge object is
	 * destroyed.
	 */
	private final List<Cell> edgeCells;

	/*
	 * The weight of the edge, i.e the sum of the weighted distances between the
	 * centres of each pair of consecutive cells in the edgeCells list.
	 */
	private double totalWeight;

	/*
	 * Constructor.
	 */
	public RGEdge() {
		/*
		 * An ArrayList implementation is used because it is efficient with
		 * respect to memory and iteration time complexity.
		 */
		edgeCells = new ArrayList<Cell>();
		totalWeight = 0;
	}

	/*
	 * Adds NextCell to the end of the EdgeCells list and increases TotalWeight
	 * by the weighted distance between the centre of NextCell and the centre of
	 * the previous cell in the EdgeCells list.
	 */
	public void appendCell(Cell nextCell) {
		edgeCells.add(nextCell);
		if (edgeCells.size() > 1) {
			totalWeight += edgeCells.get(edgeCells.size() - 2)
					.getWeightedDistanceToAdjacentCell(nextCell);
		}
	}

	public double getTotalWeight() {
		return totalWeight;
	}

	public List<Cell> getCells() {
		return Collections.unmodifiableList(edgeCells);
	}

}
