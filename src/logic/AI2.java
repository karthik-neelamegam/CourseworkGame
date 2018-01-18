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

public class AI2 {

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

		public void addAdjacentNode(ReducedGraphVertex other,
				ReducedGraphEdge edge) {
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
		public double getDistanceTo(ReducedGraphVertex vertex) {
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
			if(!cell.hasCheckpoint()) {
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
			if(edgeCells.size() > 0) {
				totalWeight += edgeCells.get(edgeCells.size()-1).getWeightedDistanceBetweenCentres(cell);
			}
		}
		public List<Cell> getCells() {
			return Collections.unmodifiableList(edgeCells);
		}
	}

	private class CheckpointVertexPair {
		private ReducedGraphVertex vertex1;
		private ReducedGraphVertex vertex2;

		public CheckpointVertexPair(ReducedGraphVertex vertex1,
				ReducedGraphVertex vertex2) {
			if (vertex1.hasCheckpoint() && vertex2.hasCheckpoint()) {
				this.vertex1 = vertex1;
				this.vertex2 = vertex2;
			}
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

		public ReducedGraphVertex getVertex1() {
			return vertex1;
		}

		public ReducedGraphVertex getVertex2() {
			return vertex2;
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
				totalWeight += vertex.getDistanceTo(pathVertices
						.get(pathVertices.size() - 1));
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
			for(int i = 0; i < pathVertices.size()-1; i++) {
				ReducedGraphVertex currentVertex = pathVertices.get(i);
				ReducedGraphVertex nextVertex = pathVertices.get(i+1);
				ReducedGraphEdge edge = currentVertex.getReducedGraphEdgeTo(nextVertex);
				List<Cell> edgeCells = edge.getCells();
				for(Cell cell : edgeCells) {
					cellPath.add(cell);
				}
			}
			return cellPath;
		}
	}

	//startCell and endCell must have checkpoints
	public class EndpointCellsException extends Exception {
		
	}
	public AI2(Cell startCell, Cell endCell) throws EndpointCellsException{
		if(!startCell.hasCheckpoint() || !endCell.hasCheckpoint()) {
			throw new EndpointCellsException();
		}
		getSemiOptimalJourney(startCell, endCell);
	}

	private List<Cell> getSemiOptimalJourney(Cell startCell, Cell endCell) throws EndpointCellsException {
		Set<ReducedGraphVertex> reducedGraphVertices = getReducedGraph(startCell);
		Set<ReducedGraphVertex> checkpointVertices = new HashSet<ReducedGraphVertex>();
		ReducedGraphVertex startVertex = null, endVertex = null;
		for (ReducedGraphVertex vertex : reducedGraphVertices) {
			if (vertex.hasCheckpoint()) {
				checkpointVertices.add(vertex);
			}
			if (vertex.getCell() == startCell) {
				startVertex = vertex;
			}
			if (vertex.getCell() == endCell) {
				endVertex = vertex;
			}
		}
		if(startVertex == null || endVertex == null) {
			throw new EndpointCellsException();
		}
		Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths = calculateShortestPathsBetweenCheckpoints(
				reducedGraphVertices, checkpointVertices);
		double minTotalDistance = Double.MAX_VALUE;
		
		List<ReducedGraphVertex> twoOptGreedyJourney = twoOpt(greedyJourney(
				checkpointVertices, shortestPaths, startVertex, endVertex));
		
		List<Cell> cellJourney = new ArrayList<Cell>();
		for(int i = 0; i < twoOptGreedyJourney.size()-1; i++) {
			ReducedGraphVertex currentCheckpointVertex = twoOptGreedyJourney.get(i);
			ReducedGraphVertex nextCheckpointVertex = twoOptGreedyJourney.get(i+1);
			ReducedGraphPath path = shortestPaths.get(new CheckpointVertexPair(currentCheckpointVertex, nextCheckpointVertex));
			cellJourney.addAll(path.getCellPath());
		}
		return cellJourney;
	}
	
	
	
	// nodes are the midpoints of shared sides of cells, edges are
	private Set<ReducedGraphVertex> getReducedGraph(Cell startCell) {
		HashMap<Cell, ReducedGraphVertex> discoveredVerticesMap = new HashMap<Cell, ReducedGraphVertex>();
		reduceGraph(discoveredVerticesMap, new HashSet<Cell>(),
				new ReducedGraphVertex(startCell));
		return new HashSet<ReducedGraphVertex>(discoveredVerticesMap.values());
	}

	// graph must be connected
	// using cell/vertex map rather than vertex set for faster look up time when
	// getting the initialised vertex of a cell
	private void reduceGraph(
			Map<Cell, ReducedGraphVertex> discoveredVerticesMap,
			Set<Cell> discoveredCells, ReducedGraphVertex currentVertex) {
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
					ReducedGraphVertex nextVertex;
					if (discoveredVerticesMap.containsKey(nextCell)) {
						nextVertex = discoveredVerticesMap.get(nextCell);
						currentVertex.addAdjacentNode(nextVertex, edge);
					} else {
						nextVertex = new ReducedGraphVertex(nextCell);
						currentVertex.addAdjacentNode(nextVertex, edge);
						reduceGraph(discoveredVerticesMap, discoveredCells,
								nextVertex);
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
			Set<ReducedGraphVertex> reducedGraphVertices,
			Set<ReducedGraphVertex> checkpointVertices) {

		Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths = new HashMap<CheckpointVertexPair, ReducedGraphPath>();
		int numDoneCheckpointVertices = 0;
		int numCheckpoints = checkpointVertices.size();
		for (ReducedGraphVertex sourceVertex : checkpointVertices) {
			final Map<ReducedGraphVertex, Double> distancesFromSourceMap = new HashMap<ReducedGraphVertex, Double>();
			Map<ReducedGraphVertex, ReducedGraphVertex> previousVerticesMap = new HashMap<ReducedGraphVertex, ReducedGraphVertex>();
			class ReducedGraphVertexComparator implements
					Comparator<ReducedGraphVertex> {
				@Override
				public int compare(ReducedGraphVertex arg0,
						ReducedGraphVertex arg1) {
					return Double.compare(distancesFromSourceMap.get(arg0),
							distancesFromSourceMap.get(arg1));
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
					&& numVisitedcheckpointVertices < numCheckpoints
							- numDoneCheckpointVertices) {
				ReducedGraphVertex currentVertex = vertexQueue.poll();
				if (currentVertex.hasCheckpoint()
						&& !shortestPaths.containsKey(new CheckpointVertexPair(
								sourceVertex, currentVertex))) {
					numVisitedcheckpointVertices++;
				}
				for (ReducedGraphVertex neighbour : currentVertex
						.getAdjacentVertices()) {
					double altWeight = distancesFromSourceMap
							.get(currentVertex)
							+ currentVertex.getDistanceTo(neighbour);
					if (altWeight < distancesFromSourceMap.get(neighbour)) {
						distancesFromSourceMap.put(neighbour, altWeight);
						previousVerticesMap.put(neighbour, currentVertex);
						vertexQueue.add(neighbour);
					}
				}
			}
			for (ReducedGraphVertex vertex : reducedGraphVertices) {
				if (vertex.hasCheckpoint()
						&& !shortestPaths.containsKey(new CheckpointVertexPair(
								sourceVertex, vertex))) {
					ReducedGraphPath path = new ReducedGraphPath();
					ReducedGraphVertex currentVertex = vertex;
					while (currentVertex != null) {
						path.appendVertex(currentVertex);
						currentVertex = previousVerticesMap.get(currentVertex);
					}
					shortestPaths.put(new CheckpointVertexPair(sourceVertex,
							vertex), path);
				}
			}
			numDoneCheckpointVertices++;
		}
		return shortestPaths;
	}

	
	private List<ReducedGraphVertex> randomisedNearestNeighbourJourney(Set<ReducedGraphVertex> checkpointVertices, ReducedGraphVertex startVertex, ReducedGraphVertex endVertex, Random rng, int numCands) throws EndpointCellsException {
		//defensive programming (ctrl-f Exception or throw)
		if(!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		}
		Set<ReducedGraphVertex> unselectedVertices = new HashSet<ReducedGraphVertex>(checkpointVertices);
		List<ReducedGraphVertex> journey = new ArrayList<ReducedGraphVertex>();
		unselectedVertices.remove(startVertex);
		unselectedVertices.remove(endVertex);
		journey.add(startVertex);
		ReducedGraphVertex currentVertex = startVertex;
		class Candidate implements Comparable<Candidate> {
			ReducedGraphVertex vertex;
			double weight;
			public Candidate(ReducedGraphVertex node, double weight) {
				this.vertex = node;
				this.weight = weight;
			}

			public ReducedGraphVertex getReducedGraphVertex() {
				return vertex;
			}
			@Override
			public int compareTo(Candidate other) {
				return Double.compare(other.weight, weight);
			}
		}

		while (unselectedVertices.size() > 0) {
			PriorityQueue<Candidate> candidates = new PriorityQueue<Candidate>();
			for (ReducedGraphVertex vertex : unselectedVertices) {
				double distance = currentVertex.getDistanceTo(vertex);
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
			ReducedGraphVertex selectedVertex = selectedCandidate.getReducedGraphVertex();
			unselectedVertices.remove(selectedVertex);
			journey.add(selectedVertex);
			currentVertex = selectedVertex;
		}
		journey.add(endVertex);
		return journey;
	}

	
	//may create 
	private List<ReducedGraphVertex> greedyJourney(
			Set<ReducedGraphVertex> checkpointVertices,
			Map<CheckpointVertexPair, ReducedGraphPath> shortestPaths,
			ReducedGraphVertex startVertex, ReducedGraphVertex endVertex) throws EndpointCellsException {
		if(!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		}
		List<ReducedGraphPath> paths = new ArrayList<ReducedGraphPath>(
				shortestPaths.values());
		class ReducedGraphPathComparator implements Comparator<ReducedGraphPath> {

			@Override
			public int compare(ReducedGraphPath o1, ReducedGraphPath o2) {
				return Double.compare(o1.getTotalWeight(), o2.getTotalWeight());
			}
			
		}
		CommonAlgorithms.mergeSort(paths, new ReducedGraphPathComparator());
		DisjointSet<ReducedGraphVertex> verticesDisjointSet = new DisjointSet<ReducedGraphVertex>(
				checkpointVertices);
		List<ReducedGraphVertex> journey = new ArrayList<ReducedGraphVertex>();
		List<ReducedGraphPath> addedPaths = new ArrayList<ReducedGraphPath>();
		HashMap<ReducedGraphVertex, ArrayList<ReducedGraphVertex>> adjacencyListMap = new HashMap<ReducedGraphVertex, ArrayList<ReducedGraphVertex>>();
		for (ReducedGraphVertex vertex : checkpointVertices) {
			adjacencyListMap.put(vertex, new ArrayList<ReducedGraphVertex>());
		}
		for (int i = 0; i < paths.size()
				&& addedPaths.size() < checkpointVertices.size(); i++) {
			ReducedGraphPath path = paths.get(i);
			ReducedGraphVertex vertex1 = path.getReducedGraphVertex1();
			ReducedGraphVertex vertex2 = path.getReducedGraphVertex2();
			int vertex1Order = vertex1.getOrder();
			int vertex2Order = vertex2.getOrder();
			boolean vertex1IsEndpoint = vertex1 == startVertex || vertex1 == endVertex;
			boolean vertex2IsEndpoint = vertex2 == startVertex || vertex2 == endVertex;
			boolean vertex1IsMaxOrder = vertex1Order == 2
					|| (vertex1IsEndpoint && vertex1Order == 1);
			boolean vertex2IsMaxOrder = vertex2Order == 2
					|| (vertex2IsEndpoint && vertex2Order == 1);
			boolean verticesAreJoined = verticesDisjointSet.areJoined(vertex1, vertex2);
			if (!vertex1IsMaxOrder && !vertex2IsMaxOrder && !verticesAreJoined) {
				verticesDisjointSet.union(vertex1, vertex2);
				addedPaths.add(path);
				adjacencyListMap.get(vertex1).add(vertex2);
				adjacencyListMap.get(vertex2).add(vertex1);
			}
		}
		journey.add(startVertex);
		ReducedGraphVertex previousVertex = null;
		ReducedGraphVertex currentVertex = startVertex;
		while (journey.size() < checkpointVertices.size()) {
			ArrayList<ReducedGraphVertex> adjacencyList = adjacencyListMap
					.get(currentVertex);
			for (ReducedGraphVertex vertex : adjacencyList) {
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
	
	private List<ReducedGraphVertex> twoOptSwap(
			List<ReducedGraphVertex> journey, int a, int b) {
		List<ReducedGraphVertex> newJourney = new ArrayList<ReducedGraphVertex>();
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

	private List<ReducedGraphVertex> twoOpt(List<ReducedGraphVertex> journey) {
		List<ReducedGraphVertex> currentJourney = journey;
		double minChange;
		do {
			minChange = 0;
			int minI = -1, minJ = -1;
			for (int i = 1; i < journey.size() - 2; i++) {
				for (int j = i + 1; j < journey.size() - 1; j++) {
					ReducedGraphVertex a = currentJourney.get(i - 1);
					ReducedGraphVertex b = currentJourney.get(i);
					ReducedGraphVertex c = currentJourney.get(j);
					ReducedGraphVertex d = currentJourney.get(j + 1);
					double previousWeight = a.getDistanceTo(b)
							+ c.getDistanceTo(d);
					double afterWeight = a.getDistanceTo(d)
							+ b.getDistanceTo(c);
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