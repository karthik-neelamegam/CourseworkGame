package logic;

public enum Direction {
	/*
	 * The predefined constants, representing the four possible directions that
	 * each cell can be from adjacent cells and also the four directions a
	 * player can move.
	 */
	UP, DOWN, RIGHT, LEFT;

	/*
	 * Returns the opposite Direction constant (self-explanatory) for each of
	 * the four cases.
	 */
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
}
