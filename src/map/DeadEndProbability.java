package map;

public enum DeadEndProbability {
	MIN(0), MEDIUM(0.5), MAX(1);
	private double probability;
	private DeadEndProbability(double probability) {
		this.probability = probability;
	}
	public double getProbability() {
		return probability;
	}
}
