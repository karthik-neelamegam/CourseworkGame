package map;

import java.awt.Color;

public enum Surface {
	/*
	 * The predefined constants, representing the three surfaces that are
	 * required.
	 */
	SLOW(0.5, GameConstants.SLOW_SURFACE_COLOR), NORMAL(1,
			GameConstants.NORMAL_SURFACE_COLOR), FAST(2,
			GameConstants.FAST_SURFACE_COLOR);

	/*
	 * The value by which the speed of Player objects moving over Cell objects
	 * with this Surface is multiplied by. If this value is 1, the speed doesn't
	 * change. If this value is less than 1, the speed decreases. If this value
	 * is greater than 1, the speed increases.
	 */
	private final double speedMultiplier;

	/*
	 * The colour used to render a Cell object with this Surface. Used for
	 * rendering purposes only.
	 */
	private final Color color;
	
	/*
	 * Constructor. Used to create the predefined constants with the given arguments.
	 */
	private Surface(double speedMultiplier, Color color) {
		this.speedMultiplier = speedMultiplier;
		this.color = color;
	}

	/*
	 * Getters.
	 */
	
	public double getSpeedMultiplier() {
		return speedMultiplier;
	}

	public Color getColor() {
		return color;
	}

}
