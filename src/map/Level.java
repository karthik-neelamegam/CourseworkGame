package map;

import java.util.EnumMap;

public enum Level {
	/*
	 * The predefined constants, representing the six different levels of the
	 * game. The name of the Level constant refers to its position in the level
	 * progression when playing against the AI.
	 */
	ONE(5, 5, 5, MazeType.KRUSKAL, 0, new EnumMap<Surface, Double>(Surface.class) {
		{
			put(Surface.SLOW, 5d);
			put(Surface.NORMAL, 90d);
			put(Surface.FAST, 5d);
		}
	}), TWO(7, 7, 7, MazeType.KRUSKAL, 0.1, new EnumMap<Surface, Double>(
			Surface.class) {
		{
			put(Surface.SLOW, 10d);
			put(Surface.NORMAL, 80d);
			put(Surface.FAST, 10d);
		}
	}), THREE(10, 10, 10, MazeType.KRUSKAL, 0.2, new EnumMap<Surface, Double>(
			Surface.class) {
		{
			put(Surface.SLOW, 15d);
			put(Surface.NORMAL, 70d);
			put(Surface.FAST, 15d);
		}
	}), FOUR(15, 15, 15, MazeType.DFS, 0.3, new EnumMap<Surface, Double>(
			Surface.class) {
		{
			put(Surface.SLOW, 20d);
			put(Surface.NORMAL, 60d);
			put(Surface.FAST, 20d);
		}
	}), FIVE(20, 20, 20, MazeType.DFS, 0.4, new EnumMap<Surface, Double>(
			Surface.class) {
		{
			put(Surface.SLOW, 25d);
			put(Surface.NORMAL, 50d);
			put(Surface.FAST, 25d);
		}
	}), SIX(30, 30, 30, MazeType.DFS, 0.5, new EnumMap<Surface, Double>(
			Surface.class) {
		{
			put(Surface.SLOW, 30d);
			put(Surface.NORMAL, 40d);
			put(Surface.FAST, 30d);
		}
	});

	/*
	 * The number of columns in the maze for this level.
	 */
	private final int numCellsWide;

	/*
	 * The number of rows in the maze for this level.
	 */
	private final int numCellsHigh;

	/*
	 * The number of checkpoint cells in the maze for this level, apart from the
	 * start and end cells (as these are automatically checkpoints).
	 */
	private final int numCheckpointsExcludingEndpoints;

	/*
	 * The frequency in which dead-ends occur in the maze.
	 */
	private final double deadEndProbability;

	/*
	 * The style of maze for this level (i.e. either one generated using
	 * depth-first search generated maze or one generated using randomised
	 * Kruskal’s algorithm).
	 */
	private final MazeType mazeType;

	/*
	 * A dictionary describing how often each Surface in the map’s key set will
	 * come up in the maze. The greater the Double value associated with a
	 * particular Surface key in the map, the more likely it is to occur in the
	 * maze. Java's EnumMap class is used because it is more efficient than
	 * HashMap when enums are used as the keys. This is because Enum maps
	 * are represented internally as arrays, making it a very compact and
	 * efficient representation.
	 */
	private final EnumMap<Surface, Double> surfaceRatios;

	/*
	 * Constructor. Used to create the predefined constants with the given
	 * arguments.
	 */
	private Level(int numCellsWide, int numCellsHigh,
			int numCheckpointsExcludingEndpoints, MazeType mazeType,
			double deadEndProbability, EnumMap<Surface, Double> surfaceRatios) {
		this.numCellsWide = numCellsWide;
		this.numCellsHigh = numCellsHigh;
		this.numCheckpointsExcludingEndpoints = numCheckpointsExcludingEndpoints;
		this.mazeType = mazeType;
		this.deadEndProbability = deadEndProbability;
		this.surfaceRatios = surfaceRatios;
	}

	/*
	 * Returns the Level enum that represents the next level after this
	 * one. If this is the final level, then returns Null. A switch case
	 * statement is used to deal with the individual Level constants rather than
	 * exploiting the natural ordering of the enum constants. This is because it
	 * means that whenever a new Level constant is added, care must be given to
	 * which levels it comes after and before.
	 */
	public Level getNextLevel() {
		Level nextLevel;
		switch (this) {
		case ONE:
			nextLevel = TWO;
			break;
		case TWO:
			nextLevel = THREE;
			break;
		case THREE:
			nextLevel = FOUR;
			break;
		case FOUR:
			nextLevel = FIVE;
			break;
		case FIVE:
			nextLevel = SIX;
			break;
		case SIX:
		default:
			nextLevel = null;
		}
		return nextLevel;
	}

	/*
	 * Getters.
	 */

	public int getNumCellsWide() {
		return numCellsWide;
	}

	public int getNumCellsHigh() {
		return numCellsHigh;
	}

	public int getNumCheckpointsExcludingEndpoints() {
		return numCheckpointsExcludingEndpoints;
	}

	public MazeType getMazeType() {
		return mazeType;
	}

	public double getDeadEndProbability() {
		return deadEndProbability;
	}

	public EnumMap<Surface, Double> getSurfaceRatios() {
		return surfaceRatios;
	}

}
