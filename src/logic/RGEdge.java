package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import map.Cell;

public class RGEdge {
	private final List<Cell> edgeCells;
	private double totalWeight;

	public RGEdge() {
		edgeCells = new ArrayList<Cell>();
		totalWeight = 0;
	}

	public void appendCell(Cell nextCell) {
		edgeCells.add(nextCell);
		if (edgeCells.size() > 1) {				
			totalWeight += edgeCells.get(edgeCells.size() - 2).getWeightedDistanceToAdjacentCell(nextCell);
		}
	}
	
	public double getTotalWeight() {
		return totalWeight;
	}

	public List<Cell> getCells() {
		return Collections.unmodifiableList(edgeCells);
	}

}
