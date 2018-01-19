package logic;

public enum Direction {
	NORTH, SOUTH, EAST, WEST;
	public Direction getOpposite() {
		Direction opposite = null;
		switch (this) {
		case NORTH:
			opposite = SOUTH;
			break;
		case SOUTH:
			opposite = NORTH;
			break;
		case EAST:
			opposite = WEST;
			break;
		case WEST:
			opposite = EAST;
			break;
		}
		return opposite;
	}
}
