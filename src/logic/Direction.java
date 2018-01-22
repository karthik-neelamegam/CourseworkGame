package logic;

import user_interface.Application;

public enum Direction {
	NORTH, SOUTH, EAST, WEST;
	private final static Direction[] directions = Direction.values();
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
	public static Direction getRandom() {
		return directions[Application.rng.nextInt(directions.length)];
	}
}
