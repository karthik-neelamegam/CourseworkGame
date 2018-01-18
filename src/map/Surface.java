package map;


public enum Surface {
	SLOW(0.5), NORMAL(1), FAST(2);
	private final double speedMultiplier;
	//private final ImageIcon texture;
	Surface(double speedMultiplier) { //String imageFilename
		this.speedMultiplier = speedMultiplier;
	}
	public double getSpeedMultiplier() {
		return this.speedMultiplier;
	}
}
