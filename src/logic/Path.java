package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {
	private List<RGVertex> pathVertices;
	private double totalWeight;

	public Path() {
		pathVertices = new ArrayList<RGVertex>();
		totalWeight = 0;
	}

	public void appendVertex(RGVertex nextVertex) {
		pathVertices.add(nextVertex);
		if (pathVertices.size() > 1) {
			totalWeight += nextVertex.getWeightToAdjacentVertex(pathVertices
					.get(pathVertices.size() - 2));
		}
	}

	public double getTotalWeight() {
		return totalWeight;
	}

	public RGVertex getVertex1() {
		return pathVertices.get(0);
	}

	public RGVertex getVertex2() {
		return pathVertices.get(pathVertices.size() - 1);
	}

	public List<RGVertex> getPathVertices() {
		return Collections.unmodifiableList(pathVertices);
	}
}
