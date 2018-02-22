package core;


public class Neighbouring {
	/*
	 * This class is essentially a container storing two variables: a
	 * neighbouring Cell object and the direction to it from the parent Cell object
	 * where this Neighbouring object would have been created and stored.
	 */

	/*
	 * The neighbouring Cell object. This is aggregation as the Neighbouring
	 * class has a HAS-A relationship with the Cell class but the
	 * neighbouringCell object will not be destroyed if the Neighbouring object
	 * is destroyed.
	 */
	private final Cell neighbouringCell;

	/*
	 * The direction to neighbouringCell from the parent Cell object. This is
	 * aggregation as the Neighbouring class has a HAS-A relationship with the
	 * Direction enum type but the directionToNeighbouringCell enum will not be
	 * destroyed if the Neighbouring object is destroyed.
	 */
	private final Direction directionToNeighbouringCell;

	/*
	 * Constructor.
	 */
	public Neighbouring(Cell neighbouringCell, Direction directionToCell) {
		this.neighbouringCell = neighbouringCell;
		this.directionToNeighbouringCell = directionToCell;
	}

	/*
	 * Getters.
	 */

	public Cell getNeighbouringCell() {
		return neighbouringCell;
	}

	public Direction getDirectionToNeighbouringCell() {
		return directionToNeighbouringCell;
	}
}
