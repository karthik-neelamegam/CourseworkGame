package map;

import java.util.EnumMap;

public enum Level {
	ONE(5, 5, 5, GameMapType.DFS, 0, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 5d);
					put(Surface.NORMAL, 90d);
					put(Surface.FAST, 5d);
				}
			})), TWO(7, 7, 7, GameMapType.DFS, 0.1, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 10d);
					put(Surface.NORMAL, 80d);
					put(Surface.FAST, 10d);
				}
			})), THREE(10, 10, 10, GameMapType.DFS, 0.2, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 15d);
					put(Surface.NORMAL, 70d);
					put(Surface.FAST, 15d);
				}
			})), FOUR(15, 15, 15, GameMapType.KRUSKAL, 0.3, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 20d);
					put(Surface.NORMAL, 60d);
					put(Surface.FAST, 20d);
				}
			})), FIVE(20, 20, 20, GameMapType.KRUSKAL, 0.4, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 25d);
					put(Surface.NORMAL, 50d);
					put(Surface.FAST, 25d);
				}
			})), SIX(30, 30, 30, GameMapType.KRUSKAL, 0.5, new SurfacePicker(
			new EnumMap<Surface, Double>(Surface.class) {
				{
					put(Surface.SLOW, 30d);
					put(Surface.NORMAL, 40d);
					put(Surface.FAST, 30d);
				}
			}));

	// can easily add more, ease of maintainenance/extensibility

	private int numCellsWide, numCellsHigh, numCheckpointsExcludingEndpoints;
	private double deadEndProbability; // the more deadends, the harder because
										// u will keep getting stuck;
	private GameMapType mapType;
	private SurfacePicker surfacePicker;

	private Level(int numCellsWide, int numCellsHigh, int numCheckpointsExcludingEndpoints,
			GameMapType mapType, double deadEndProbability,
			SurfacePicker surfacePicker) {
		this.numCellsWide = numCellsWide;
		this.numCellsHigh = numCellsHigh;
		this.numCheckpointsExcludingEndpoints = numCheckpointsExcludingEndpoints;
		this.mapType = mapType;
		this.deadEndProbability = deadEndProbability;
		this.surfacePicker = surfacePicker;
	}

	public int getNumCellsWide() {
		return numCellsWide;
	}

	public int getNumCellsHigh() {
		return numCellsHigh;
	}

	public int getNumCheckpointsExcludingEndpoints() {
		return numCheckpointsExcludingEndpoints;
	}

	public GameMapType getMapType() {
		return mapType;
	}

	public double getDeadEndProbability() {
		return deadEndProbability;
	}

	public SurfacePicker getSurfacePicker() {
		return surfacePicker;
	}

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
}
