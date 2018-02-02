package map;

import java.awt.Color;


public enum Surface {
	SLOW(0.5, GameConstants.SLOW_SURFACE_COLOR), NORMAL(1, GameConstants.NORMAL_SURFACE_COLOR), FAST(2, GameConstants.FAST_SURFACE_COLOR);
	private final double speedMultiplier;
	private final Color color;
	private Surface(double speedMultiplier, Color color) {
		this.speedMultiplier = speedMultiplier;
		this.color = color;
	}
	public double getSpeedMultiplier() {
		return speedMultiplier;
	}
	public Color getColor() {
		return color;
	}
}
