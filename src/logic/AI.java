package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import dsa.CommonAlgorithms;
import dsa.DisjointSet;
import map.Cell;
import user_interface.Application;

public class AI {

	// need to sort out checkpoint/cell/vertex relationship
	private class ReducedGraphVertex {
		// no need for constant time check if a vertex is adjacent in dijksta's
		// so don't need hashmap/hashset
		private HashMap<ReducedGraphVertex, ReducedGraphEdge> adjacentVertices;
		private Cell cell;

		public ReducedGraphVertex(Cell cell) {
			this.cell = cell;
			adjacentVertices = new HashMap<ReducedGraphVertex, ReducedGraphEdge>();
		}

		public void addAdjacentNode(ReducedGraphVertex other, ReducedGraphEdge edge) {
			if (other != this) {
				adjacentVertices.put(other, edge);
				other.adjacentVertices.put(this, edge);
			}
		}

		public boolean hasCheckpoint() {
			return cell.hasCheckpoint();
		}

		public Cell getCell() {
			return cell;
		}

		public Set<ReducedGraphVertex> getAdjacentVertices() {
			return Collections.unmodifiableSet(adjacentVertices.keySet());
		}

		// reminder: make everything distance or weight (consistent)
		// throwing rather than catching so that program stops and you know something
		// has gone wrong (it's never meant to go wrong)
		public double getDistanceToAdjacentReducedGraphVertex(ReducedGraphVertex vertex) {
			return adjacentVertices.get(vertex).getTotalWeight();
		}

		public ReducedGraphEdge getReducedGraphEdgeTo(ReducedGraphVertex vertex) {
			return adjacentVertices.get(vertex);
		}

		public int getOrder() {
			return adjacentVertices.size();
		}
	}

	private class NoCheckpointException extends Exception {

	}

	private class ReducedGraphCheckpointVertex extends ReducedGraphVertex {
		public ReducedGraphCheckpointVertex(Cell cell) throws NoCheckpointException {
			super(cell);
			if (!cell.hasCheckpoint()) {
				throw new NoCheckpointException();
			}
		}
	}

	private class ReducedGraphEdge {
		private List<Cell> edgeCells;
		private double totalWeight;

		public ReducedGraphEdge() {
			edgeCells = new ArrayList<Cell>();
			totalWeight = 0;
		}

		public double getTotalWeight() {
			return totalWeight;
		}

		public void appendCell(Cell cell) {
			edgeCells.add(cell);
			if (edgeCells.size() > 0) {
				totalWeight += edgeCells.get(edgeCells.size() - 1).getWeightedDistanceBetweenCentres(cell);
			}
		}

		public List<Cell> getCells() {
			return Collections.unmodifiableList(edgeCells);
		}
	}

	private class CheckpointVertexPair {
		private ReducedGraphCheckpointVertex vertex1;
		private ReducedGraphCheckpointVertex vertex2;

		public CheckpointVertexPair(ReducedGraphCheckpointVertex vertex1, ReducedGraphCheckpointVertex vertex2) {
			this.vertex1 = vertex1;
			this.vertex2 = vertex2;
		}

		// need to check if it works
		@Override
		public int hashCode() {
			return vertex1.hashCode() + vertex2.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			boolean equals = false;
			if (o instanceof CheckpointVertexPair) {
				CheckpointVertexPair otherPair = (CheckpointVertexPair) o;
				equals = (otherPair.vertex1 == vertex1 && otherPair.vertex2 == vertex2)
						|| (otherPair.vertex1 == vertex2 && otherPair.vertex2 == vertex1);
			}
			return equals;
		}
	}

	private class ReducedGraphPath {
		private List<ReducedGraphVertex> pathVertices;
		private double totalWeight;

		public ReducedGraphPath() {
			pathVertices = new ArrayList<ReducedGraphVertex>();
			totalWeight = 0;
		}

		public void appendVertex(ReducedGraphVertex vertex) {
			pathVertices.add(vertex);
			if (pathVertices.size() > 0) {
				totalWeight += vertex
						.getDistanceToAdjacentReducedGraphVertex(pathVertices.get(pathVertices.size() - 1));
			}
		}

		public double getTotalWeight() {
			return totalWeight;
		}

		public ReducedGraphVertex getReducedGraphVertex1() {
			return pathVertices.get(0);
		}

		public ReducedGraphVertex getReducedGraphVertex2() {
			return pathVertices.get(pathVertices.size() - 1);
		}

		public List<Cell> getCellPath() {
			List<Cell> cellPath = new ArrayList<Cell>();
			for (int i = 0; i < pathVertices.size() - 1; i++) {
				ReducedGraphVertex currentVertex = pathVertices.get(i);
				ReducedGraphVertex nextVertex = pathVertices.get(i + 1);
				ReducedGraphEdge edge = currentVertex.getReducedGraphEdgeTo(nextVertex);
				List<Cell> edgeCells = edge.getCells();
				for (Cell cell : edgeCells) {
					cellPath.add(cell);
				}
			}
			return cellPath;
		}
	}

	// startCell and endCell must have checkpoints
	public class EndpointCellsException extends Exception {

	}

	public AI(Cell startCell, Cell endCell, int numRNNs, int numRNNCands) throws EndpointCellsException {
		if (!startCell.hasCheckpoint() || !endCell.hasCheckpoint()) {
			throw new EndpointCellsException();
		}
		List<Cell> semiOptimalJourney = getSemiOptimalJourney(startCell, endCell, numRNNs, numRNNCands);
	}

	//could potentially make all this static
	private List<Cell> getSemiOptimalJourney(Cell startCell, Cell endCell, int numRNNs, int numRNNCands)
			throws EndpointCellsException {
		Set<ReducedGraphVertex> reducedGraphVertices = getReducedGraph(startCell);
		Set<ReducedGraphCheckpointVertex> checkpointVertices = new HashSet<ReducedGraphCheckpointVertex>();
		ReducedGraphCheckpointVertex startVertex = null, endVertex = null;
		for (ReducedGraphVertex vertex : reducedGraphVertices) {
			if (vertex instanceof ReducedGraphCheckpointVertex) {
				if (vertex.getCell() == startCell) {
					startVertex = (ReducedGraphCheckpointVertex) vertex;
				}
				if (vertex.getCell() == endCell) {
					endVertex = (ReducedGraphCheckpointVertex) vertex;
				}
				checkpointVertices.add((ReducedGraphCheckpointVertex) vertex);
			}
		}
		if (startVertex == null || endVertex == null) {
			throw new EndpointCellsException();
		}
		Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths = calculateShortestPathsBetweenCheckpoints(
				reducedGraphVertices, checkpointVertices);

		List<ReducedGraphCheckpointVertex> twoOptGreedyJourney = twoOpt(
				greedyJourney(checkpointVertices, shortestPaths, startVertex, endVertex), shortestPaths);

		double minTotalDistance = calculateTotalJourneyDistance(twoOptGreedyJourney, shortestPaths);
		List<ReducedGraphCheckpointVertex> bestCheckpointVertexJourney = twoOptGreedyJourney;

		for (int i = 0; i < numRNNs; i++) {
			List<ReducedGraphCheckpointVertex> twoOptRNNJourney = twoOpt(
					randomisedNearestNeighbourJourney(checkpointVertices, shortestPaths, startVertex, endVertex,
							Application.rng, numRNNCands),
					shortestPaths);
			double totalDistance = calculateTotalJourneyDistance(twoOptRNNJourney, shortestPaths);
			if (totalDistance < minTotalDistance) {
				minTotalDistance = totalDistance;
				bestCheckpointVertexJourney = twoOptRNNJourney;
			}
		}

		List<Cell> bestCellJourney = new ArrayList<Cell>();
		for (int i = 0; i < bestCheckpointVertexJourney.size() - 1; i++) {
			ReducedGraphCheckpointVertex currentCheckpointVertex = bestCheckpointVertexJourney.get(i);
			ReducedGraphCheckpointVertex nextCheckpointVertex = bestCheckpointVertexJourney.get(i + 1);
			ReducedGraphPath path = shortestPaths
					.get(new CheckpointVertexPair(currentCheckpointVertex, nextCheckpointVertex));
			bestCellJourney.addAll(path.getCellPath());
		}
		return bestCellJourney;
	}

	private double calculateTotalJourneyDistance(List<ReducedGraphCheckpointVertex> journey,
			Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths) {
		double totalDistance = 0;
		for (int i = 0; i < journey.size() - 1; i++) {
			ReducedGraphCheckpointVertex currentVertex = journey.get(i);
			ReducedGraphCheckpointVertex nextVertex = journey.get(i + 1);
			totalDistance += shortestPaths.get(new CheckpointVertexPair(currentVertex, nextVertex)).getTotalWeight();
		}
		return totalDistance;
	}

	// nodes are the midpoints of shared sides of cells, edges are
	private Set<ReducedGraphVertex> getReducedGraph(Cell startCell) {
		HashMap<Cell, ReducedGraphVertex> discoveredVerticesMap = new HashMap<Cell, ReducedGraphVertex>();
		reduceGraph(discoveredVerticesMap, new HashSet<Cell>(), new ReducedGraphVertex(startCell));
		return new HashSet<ReducedGraphVertex>(discoveredVerticesMap.values());
	}

	// graph must be connected
	// using cell/vertex map rather than vertex set for faster look up time when
	// getting the initialised vertex of a cell
	private void reduceGraph(Map<Cell, ReducedGraphVertex> discoveredVerticesMap, Set<Cell> discoveredCells,
			ReducedGraphVertex currentVertex) {
		discoveredVerticesMap.put(currentVertex.getCell(), currentVertex);
		discoveredCells.add(currentVertex.getCell());
		for (Cell adjacentCell : currentVertex.getCell().getAdjacentCells()) {
			if (!discoveredCells.contains(adjacentCell)) {
				ReducedGraphEdge edge = new ReducedGraphEdge();
				Cell previousCell = currentVertex.getCell();
				Cell nextCell = adjacentCell;
				edge.appendCell(previousCell);
				while (nextCell.getOrder() == 2 && !nextCell.hasCheckpoint()) {
					discoveredCells.add(nextCell);
					edge.appendCell(nextCell);
					for (Cell nextCell2 : nextCell.getAdjacentCells()) {
						if (nextCell != currentVertex.getCell()) {
							previousCell = nextCell;
							nextCell = nextCell2;
							break;
						}
					}
				}
				if (nextCell != currentVertex.getCell()) {
					edge.appendCell(nextCell);
					ReducedGraphVertex nextVertex = null;
					if (discoveredVerticesMap.containsKey(nextCell)) {
						nextVertex = discoveredVerticesMap.get(nextCell);
						currentVertex.addAdjacentNode(nextVertex, edge);
					} else {
						if (nextCell.hasCheckpoint()) {
							try {
								nextVertex = new ReducedGraphCheckpointVertex(nextCell);
							} catch (NoCheckpointException e) {
								e.printStackTrace();
							}
						} else {
							nextVertex = new ReducedGraphVertex(nextCell);
						}
						currentVertex.addAdjacentNode(nextVertex, edge);
						reduceGraph(discoveredVerticesMap, discoveredCells, nextVertex);
					}
				}
			}
		}
	}

	// could account for turning rate slowing stuff down at junctions? could
	// require the reimplementation of directions to know if there is a turn
	// being done
	// could also account for the fact that the shortest path isnt straight down
	// the middle but a diagonal between points
	// might not need checkpointpair and could use cellpair instead
	private Map<CheckpointVertexPair, ReducedGraphPath> calculateShortestPathsBetweenCheckpoints(
			Set<ReducedGraphVertex> reducedGraphVertices, Set<ReducedGraphCheckpointVertex> checkpointVertices) {

		Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths = new HashMap<CheckpointVertexPair, ReducedGraphPath>();
		int numDoneCheckpointVertices = 0;
		int numCheckpoints = checkpointVertices.size();
		for (ReducedGraphCheckpointVertex sourceVertex : checkpointVertices) {
			final Map<ReducedGraphVertex, Double> distancesFromSourceMap = new HashMap<ReducedGraphVertex, Double>();
			Map<ReducedGraphVertex, ReducedGraphVertex> previousVerticesMap = new HashMap<ReducedGraphVertex, ReducedGraphVertex>();
			class ReducedGraphVertexComparator implements Comparator<ReducedGraphVertex> {
				@Override
				public int compare(ReducedGraphVertex arg0, ReducedGraphVertex arg1) {
					return Double.compare(distancesFromSourceMap.get(arg0), distancesFromSourceMap.get(arg1));
				}
			}
			PriorityQueue<ReducedGraphVertex> vertexQueue = new PriorityQueue<ReducedGraphVertex>(
					new ReducedGraphVertexComparator());
			distancesFromSourceMap.put(sourceVertex, 0.0);
			for (ReducedGraphVertex vertex : reducedGraphVertices) {
				distancesFromSourceMap.put(vertex, Double.MAX_VALUE);
				previousVerticesMap.put(vertex, null);
			}
			vertexQueue.add(sourceVertex);
			int numVisitedcheckpointVertices = 0;
			while (!vertexQueue.isEmpty()
					&& numVisitedcheckpointVertices < numCheckpoints - numDoneCheckpointVertices) {
				ReducedGraphVertex currentVertex = vertexQueue.poll();
				if (currentVertex instanceof ReducedGraphCheckpointVertex) {
					if (!shortestPaths.containsKey(
							new CheckpointVertexPair(sourceVertex, (ReducedGraphCheckpointVertex) currentVertex))) {
						numVisitedcheckpointVertices++;
					}
				}
				for (ReducedGraphVertex neighbour : currentVertex.getAdjacentVertices()) {
					double altWeight = distancesFromSourceMap.get(currentVertex)
							+ currentVertex.getDistanceToAdjacentReducedGraphVertex(neighbour);
					if (altWeight < distancesFromSourceMap.get(neighbour)) {
						distancesFromSourceMap.put(neighbour, altWeight);
						previousVerticesMap.put(neighbour, currentVertex);
						vertexQueue.add(neighbour);
					}
				}
			}
			for (ReducedGraphVertex vertex : reducedGraphVertices) {

				if (vertex instanceof ReducedGraphCheckpointVertex) {
					if (!shortestPaths.containsKey(
							new CheckpointVertexPair(sourceVertex, (ReducedGraphCheckpointVertex) vertex))) {
						ReducedGraphPath path = new ReducedGraphPath();
						ReducedGraphVertex currentVertex = vertex;
						while (currentVertex != null) {
							path.appendVertex(currentVertex);
							currentVertex = previousVerticesMap.get(currentVertex);
						}
						shortestPaths.put(new CheckpointVertexPair(sourceVertex, (ReducedGraphCheckpointVertex) vertex),
								path);
					}
				}
			}
			numDoneCheckpointVertices++;
		}
		return shortestPaths;
	}

	private List<ReducedGraphCheckpointVertex> randomisedNearestNeighbourJourney(
			Set<ReducedGraphCheckpointVertex> checkpointVertices,
			Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths, ReducedGraphCheckpointVertex startVertex,
			ReducedGraphCheckpointVertex endVertex, Random rng, int numCands) throws EndpointCellsException {
		// defensive programming (ctrl-f Exception or throw)
		if (!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		}
		Set<ReducedGraphCheckpointVertex> unselectedVertices = new HashSet<ReducedGraphCheckpointVertex>(
				checkpointVertices);
		List<ReducedGraphCheckpointVertex> journey = new ArrayList<ReducedGraphCheckpointVertex>();
		unselectedVertices.remove(startVertex);
		unselectedVertices.remove(endVertex);
		journey.add(startVertex);
		ReducedGraphCheckpointVertex currentVertex = startVertex;
		class Candidate implements Comparable<Candidate> {
			ReducedGraphCheckpointVertex vertex;
			double weight;

			public Candidate(ReducedGraphCheckpointVertex node, double weight) {
				this.vertex = node;
				this.weight = weight;
			}

			public ReducedGraphCheckpointVertex getReducedGraphCheckpointVertex() {
				return vertex;
			}

			@Override
			public int compareTo(Candidate other) {
				return Double.compare(other.weight, weight);
			}
		}

		while (unselectedVertices.size() > 0) {
			PriorityQueue<Candidate> candidates = new PriorityQueue<Candidate>();
			for (ReducedGraphCheckpointVertex vertex : unselectedVertices) {
				double distance = shortestPaths.get(new CheckpointVertexPair(currentVertex, vertex)).getTotalWeight();
				Candidate cand = new Candidate(vertex, distance);
				if (candidates.size() < numCands) {
					candidates.add(cand);
				} else if (cand.compareTo(candidates.peek()) > 0) {
					candidates.poll();
					candidates.add(cand);
				}
			}
			int randIndex = rng.nextInt(candidates.size());
			Iterator<Candidate> candidatesIterator = candidates.iterator();
			int i = 0;
			while (i < randIndex) {
				i++;
				candidatesIterator.next();
			}
			Candidate selectedCandidate = candidatesIterator.next();
			ReducedGraphCheckpointVertex selectedVertex = selectedCandidate.getReducedGraphCheckpointVertex();
			unselectedVertices.remove(selectedVertex);
			journey.add(selectedVertex);
			currentVertex = selectedVertex;
		}
		journey.add(endVertex);
		return journey;
	}

	// may create
	private List<ReducedGraphCheckpointVertex> greedyJourney(Set<ReducedGraphCheckpointVertex> checkpointVertices,
			Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths, ReducedGraphCheckpointVertex startVertex,
			ReducedGraphCheckpointVertex endVertex) throws EndpointCellsException {
		if (!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		}
		List<ReducedGraphPath> paths = new ArrayList<ReducedGraphPath>(shortestPaths.values());
		class ReducedGraphPathComparator implements Comparator<ReducedGraphPath> {

			@Override
			public int compare(ReducedGraphPath o1, ReducedGraphPath o2) {
				return Double.compare(o1.getTotalWeight(), o2.getTotalWeight());
			}

		}
		CommonAlgorithms.mergeSort(paths, new ReducedGraphPathComparator());
		DisjointSet<ReducedGraphCheckpointVertex> verticesDisjointSet = new DisjointSet<ReducedGraphCheckpointVertex>(
				checkpointVertices);
		List<ReducedGraphCheckpointVertex> journey = new ArrayList<ReducedGraphCheckpointVertex>();
		List<ReducedGraphPath> addedPaths = new ArrayList<ReducedGraphPath>();
		HashMap<ReducedGraphCheckpointVertex, ArrayList<ReducedGraphCheckpointVertex>> adjacencyListMap = new HashMap<ReducedGraphCheckpointVertex, ArrayList<ReducedGraphCheckpointVertex>>();
		for (ReducedGraphCheckpointVertex vertex : checkpointVertices) {
			adjacencyListMap.put(vertex, new ArrayList<ReducedGraphCheckpointVertex>());
		}
		for (int i = 0; i < paths.size() && addedPaths.size() < checkpointVertices.size(); i++) {
			ReducedGraphPath path = paths.get(i);
			ReducedGraphCheckpointVertex vertex1 = (ReducedGraphCheckpointVertex) path.getReducedGraphVertex1();
			ReducedGraphCheckpointVertex vertex2 = (ReducedGraphCheckpointVertex) path.getReducedGraphVertex2();
			int vertex1Order = vertex1.getOrder();
			int vertex2Order = vertex2.getOrder();
			boolean vertex1IsEndpoint = vertex1 == startVertex || vertex1 == endVertex;
			boolean vertex2IsEndpoint = vertex2 == startVertex || vertex2 == endVertex;
			boolean vertex1IsMaxOrder = vertex1Order == 2 || (vertex1IsEndpoint && vertex1Order == 1);
			boolean vertex2IsMaxOrder = vertex2Order == 2 || (vertex2IsEndpoint && vertex2Order == 1);
			boolean verticesAreJoined = verticesDisjointSet.areJoined(vertex1, vertex2);
			if (!vertex1IsMaxOrder && !vertex2IsMaxOrder && !verticesAreJoined) {
				verticesDisjointSet.union(vertex1, vertex2);
				addedPaths.add(path);
				adjacencyListMap.get(vertex1).add(vertex2);
				adjacencyListMap.get(vertex2).add(vertex1);
			}
		}
		journey.add(startVertex);
		ReducedGraphCheckpointVertex previousVertex = null;
		ReducedGraphCheckpointVertex currentVertex = startVertex;
		while (journey.size() < checkpointVertices.size()) {
			ArrayList<ReducedGraphCheckpointVertex> adjacencyList = adjacencyListMap.get(currentVertex);
			for (ReducedGraphCheckpointVertex vertex : adjacencyList) {
				if (vertex != previousVertex) {
					journey.add(vertex);
					previousVertex = currentVertex;
					currentVertex = vertex;
					break;
				}
			}
		}
		return journey;
	}

	private List<ReducedGraphCheckpointVertex> twoOptSwap(List<ReducedGraphCheckpointVertex> journey, int a, int b) {
		List<ReducedGraphCheckpointVertex> newJourney = new ArrayList<ReducedGraphCheckpointVertex>();
		for (int i = 0; i < a; i++) {
			newJourney.add(journey.get(i));
		}
		for (int i = b; i >= a; i--) {
			newJourney.add(journey.get(i));
		}
		for (int i = b + 1; i < journey.size(); i++) {
			newJourney.add(journey.get(i));
		}
		return newJourney;
	}

	private List<ReducedGraphCheckpointVertex> twoOpt(List<ReducedGraphCheckpointVertex> journey,
			Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths) {
		List<ReducedGraphCheckpointVertex> currentJourney = journey;
		double minChange;
		do {
			minChange = 0;
			int minI = -1, minJ = -1;
			for (int i = 1; i < journey.size() - 2; i++) {
				for (int j = i + 1; j < journey.size() - 1; j++) {
					ReducedGraphCheckpointVertex a = currentJourney.get(i - 1);
					ReducedGraphCheckpointVertex b = currentJourney.get(i);
					ReducedGraphCheckpointVertex c = currentJourney.get(j);
					ReducedGraphCheckpointVertex d = currentJourney.get(j + 1);
					double previousWeight = shortestPaths.get(new CheckpointVertexPair(a, b)).getTotalWeight()
							+ shortestPaths.get(new CheckpointVertexPair(c, d)).getTotalWeight();
					double afterWeight = shortestPaths.get(new CheckpointVertexPair(a, d)).getTotalWeight()
							+ shortestPaths.get(new CheckpointVertexPair(c, b)).getTotalWeight();
					double change = afterWeight - previousWeight;
					if (change < minChange) {
						minChange = change;
						minI = i;
						minJ = j;
					}
				}
			}
			currentJourney = twoOptSwap(currentJourney, minI, minJ);
		} while (minChange != 0);
		return currentJourney;
	}

}