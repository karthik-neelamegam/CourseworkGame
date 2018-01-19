package map;

import java.awt.Color;


public enum Surface {
	SLOW(0.5, Color.ORANGE), NORMAL(1, Color.WHITE), FAST(2, Color.CYAN);
	private final double speedMultiplier;
	private final Color color;
	private Surface(double speedMultiplier, Color color) { //String imageFilename
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
