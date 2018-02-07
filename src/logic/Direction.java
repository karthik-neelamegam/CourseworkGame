package logic;

import user_interface.Application;

public enum Direction {
	UP, DOWN, RIGHT, LEFT;
	private final static Direction[] directions = Direction.values();
	public Direction getOpposite() {
		Direction opposite = null;
		switch (this) {
		case UP:
			opposite = DOWN;
			break;
		case DOWN:
			opposite = UP;
			break;
		case RIGHT:
			opposite = LEFT;
			break;
		case LEFT:
			opposite = RIGHT;
			break;
		}
		return opposite;
	}
	public final static Direction getRandom() {
		return directions[Application.rng.nextInt(directions.length)];
	}
}
