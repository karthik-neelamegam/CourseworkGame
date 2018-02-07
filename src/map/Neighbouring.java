package map;

import logic.Direction;

public class Neighbouring {
	private final Cell neighbouringCell;
	private final Direction directionToNeighbouringCell;
	public Neighbouring(Cell neighbouringCell, Direction directionToCell) {
		this.neighbouringCell = neighbouringCell;
		this.directionToNeighbouringCell = directionToCell;
	}
	public Cell getNeighbouringCell() {
		return neighbouringCell;
	}
	public Direction getDirectionToNeighbouringCell() {
		return directionToNeighbouringCell;
	}
}
