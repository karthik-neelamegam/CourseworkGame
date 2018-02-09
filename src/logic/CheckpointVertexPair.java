package logic;

public class CheckpointVertexPair {
	private final RGVertex checkpointVertex1;
	private final RGVertex checkpointVertex2;

	public CheckpointVertexPair(RGVertex checkpointVertex1, RGVertex checkpointVertex2) {
		this.checkpointVertex1 = checkpointVertex1;
		this.checkpointVertex2 = checkpointVertex2;
	}

	// need to check if it works
	@Override
	public int hashCode() {
		return checkpointVertex1.hashCode() * checkpointVertex2.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean equals = false;
		if (o instanceof CheckpointVertexPair) {
			CheckpointVertexPair otherPair = (CheckpointVertexPair) o;
			equals = (otherPair.checkpointVertex1 == checkpointVertex1 && otherPair.checkpointVertex2 == checkpointVertex2)
					|| (otherPair.checkpointVertex1 == checkpointVertex2 && otherPair.checkpointVertex2 == checkpointVertex1);
		}
		return equals;
	}

}
