package logic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import logic.ReducedGraph.Edge;
import logic.ReducedGraph.Vertex;
import map.Cell;
import user_interface.Application;
import dsa.CommonAlgorithms;
import dsa.DisjointSet;

public class AIPlayer extends Player {
	public class CheckpointVertexPair {
		private Vertex vertex1;
		private Vertex vertex2;
		public CheckpointVertexPair(Vertex vertex1, Vertex vertex2) {
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
	public class Path {
		private List<Vertex> pathVertices;
		private double totalWeight;

		public Path() {
			pathVertices = new ArrayList<Vertex>();
			totalWeight = 0;
		}

		public void appendVertex(Vertex vertex) {
			pathVertices.add(vertex);
			if (pathVertices.size() > 0) {
				totalWeight += vertex
						.getDistanceToAdjacentReducedGraphVertex(pathVertices.get(pathVertices.size() - 1));
			}
		}

		public double getTotalWeight() {
			return totalWeight;
		}

		public Vertex getReducedGraphVertex1() {
			return pathVertices.get(0);
		}

		public Vertex getReducedGraphVertex2() {
			return pathVertices.get(pathVertices.size() - 1);
		}

		public List<Cell> getCellPath() {
			List<Cell> cellPath = new ArrayList<Cell>();
			for (int i = 0; i < pathVertices.size() - 1; i++) {
				Vertex currentVertex = pathVertices.get(i);
				Vertex nextVertex = pathVertices.get(i + 1);
				Edge edge = currentVertex.getReducedGraphEdgeTo(nextVertex);
				List<Cell> edgeCells = edge.getCells();
				for (Cell cell : edgeCells) {
					cellPath.add(cell);
				}
			}
			return cellPath;
		}
	}


	private Set<Vertex> checkpointVertices;
	private Map<CheckpointVertexPair, Path> shortestPathsBetweenCheckpoints; //complete graph, adj matrix better than adj list in terms of space and lookup
	//key accounts for both orders so memory space halved with this map and the checkpointvertices set vs a map of maps

	private ReducedGraph rg; //separating reduced graph from aiplayer so that multiple AI (monsters) could be added in the future; cohesive modules
	//instance variable because I can then use it for random walks
	
	private List<Cell> journey;
	public AIPlayer(Cell startCell, Cell endCell, double baseVel, Color color,
			int numCheckpointsToReach, ReducedGraph rg, int numRNNs, int numRNNCands) {
		super(startCell, baseVel, color, numCheckpointsToReach);
		initCheckpointGraph();
		journey = getSemiOptimalJourneyThroughCheckpoints(rg.getVertex(startCell), rg.getVertex(endCell), numRNNs, numRNNCands);
	}
	
	private void initCheckpointGraph() {
		Set<Vertex> vertices = rg.getVertices();
		checkpointVertices = new HashSet<Vertex>();
		for(Vertex vertex : vertices) {
			if(vertex.getCell().hasCheckpoint()) {
				checkpointVertices.add(vertex);
			}
		}
		shortestPathsBetweenCheckpoints = new HashMap<CheckpointVertexPair, Path>();
		
		int numDoneCheckpointVertices = 0;
		int numCheckpoints = checkpointVertices.size();
		for (Vertex sourceVertex : checkpointVertices) {
			final Map<Vertex, Double> distancesFromSourceMap = new HashMap<Vertex, Double>();
			Map<Vertex, Vertex> previousVerticesMap = new HashMap<Vertex, Vertex>();
			class ReducedGraphVertexComparator implements Comparator<Vertex> {
				@Override
				public int compare(Vertex arg0, Vertex arg1) {
					return Double.compare(distancesFromSourceMap.get(arg0), distancesFromSourceMap.get(arg1));
				}
			}
			PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>(
					new ReducedGraphVertexComparator());
			distancesFromSourceMap.put(sourceVertex, 0.0);
			for (Vertex vertex : vertices) {
				distancesFromSourceMap.put(vertex, Double.MAX_VALUE);
				previousVerticesMap.put(vertex, null);
			}
			vertexQueue.add(sourceVertex);
			int numVisitedcheckpointVertices = 0;
			while (!vertexQueue.isEmpty()
					&& numVisitedcheckpointVertices < numCheckpoints - numDoneCheckpointVertices) {
				Vertex currentVertex = vertexQueue.poll();
				if (checkpointVertices.contains(currentVertex)) {
					if (!shortestPathsBetweenCheckpoints.containsKey(
							new CheckpointVertexPair(sourceVertex, currentVertex))) {
						numVisitedcheckpointVertices++;
					}
				}
				for (Vertex neighbour : currentVertex.getAdjacentVertices()) {
					double altWeight = distancesFromSourceMap.get(currentVertex)
							+ currentVertex.getDistanceToAdjacentReducedGraphVertex(neighbour);
					if (altWeight < distancesFromSourceMap.get(neighbour)) {
						distancesFromSourceMap.put(neighbour, altWeight);
						previousVerticesMap.put(neighbour, currentVertex);
						vertexQueue.add(neighbour);
					}
				}
			}
			for (Vertex vertex : vertices) {

				if (checkpointVertices.contains(vertex)) {
					if (!shortestPathsBetweenCheckpoints.containsKey(
							new CheckpointVertexPair(sourceVertex, vertex))) {
						Path path = new Path();
						Vertex currentVertex = vertex;
						while (currentVertex != null) {
							path.appendVertex(currentVertex);
							currentVertex = previousVerticesMap.get(currentVertex);
						}
						shortestPathsBetweenCheckpoints.put(new CheckpointVertexPair(sourceVertex, vertex),
								path);
					}
				}
			}
			numDoneCheckpointVertices++;
		}
	}
	
	private double getShortestPathDistanceBetweenCheckpoints(Vertex v1, Vertex v2) {
		return shortestPathsBetweenCheckpoints.get(new CheckpointVertexPair(v1, v2)).getTotalWeight();
	}
	
	private double calculateTotalJourneyDistance(List<Vertex> journey) {
		double totalDistance = 0;
		for (int i = 0; i < journey.size() - 1; i++) {
			Vertex currentVertex = journey.get(i);
			Vertex nextVertex = journey.get(i + 1);
			totalDistance += getShortestPathDistanceBetweenCheckpoints(currentVertex, nextVertex);
		}
		return totalDistance;
	}

	private List<Cell> getSemiOptimalJourneyThroughCheckpoints(Vertex startVertex, Vertex endVertex, int numRNNs, int numRNNCands) {
		List<Vertex> twoOptGreedyJourney = twoOpt(greedyJourney(startVertex, endVertex));
		double minTotalDistance = calculateTotalJourneyDistance(twoOptGreedyJourney);
		List<Vertex> bestCheckpointVertexJourney = twoOptGreedyJourney;
		for (int i = 0; i < numRNNs; i++) {
			List<Vertex> twoOptRNNJourney = twoOpt(randomisedNearestNeighbourJourney(startVertex, endVertex, Application.rng, numRNNCands));
			double totalDistance = calculateTotalJourneyDistance(twoOptRNNJourney);
			if (totalDistance < minTotalDistance) {
				minTotalDistance = totalDistance;
				bestCheckpointVertexJourney = twoOptRNNJourney;
			}
		}
		List<Cell> bestCellJourney = new ArrayList<Cell>();
		for (int i = 0; i < bestCheckpointVertexJourney.size() - 1; i++) {
			Vertex currentCheckpointVertex = bestCheckpointVertexJourney.get(i);
			Vertex nextCheckpointVertex = bestCheckpointVertexJourney.get(i + 1);
			bestCellJourney.addAll(shortestPathsBetweenCheckpoints.get(new CheckpointVertexPair(currentCheckpointVertex, nextCheckpointVertex)).getCellPath());
		}
		return bestCellJourney;
	}

	private List<Vertex> randomisedNearestNeighbourJourney(Vertex startVertex, Vertex endVertex, Random rng, int numCands) {
		// defensive programming (ctrl-f Exception or throw), prob can get rid of it, if you decide to keep it, check for it elsewhere
		/*if (!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		} */
		Set<Vertex> unselectedVertices = new HashSet<Vertex>();
		List<Vertex> journey = new ArrayList<Vertex>();
		unselectedVertices.remove(startVertex);
		unselectedVertices.remove(endVertex);
		journey.add(startVertex);
		Vertex currentVertex = startVertex;
		class Candidate implements Comparable<Candidate> {
			Vertex vertex;
			double weight;

			public Candidate(Vertex node, double weight) {
				this.vertex = node;
				this.weight = weight;
			}

			public Vertex getReducedGraphVertex() {
				return vertex;
			}

			@Override
			public int compareTo(Candidate other) {
				return Double.compare(other.weight, weight);
			}
		}

		while (unselectedVertices.size() > 0) {
			PriorityQueue<Candidate> candidates = new PriorityQueue<Candidate>();
			for (Vertex vertex : unselectedVertices) {
				double distance = getShortestPathDistanceBetweenCheckpoints(currentVertex, vertex);
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
			Candidate selectedCandidate = null;
			for(Candidate cand : candidates) {
				if(i == randIndex) {
					selectedCandidate = cand;
					break;
				}
				i++;
			}
			Vertex selectedVertex = selectedCandidate.getReducedGraphVertex();
			unselectedVertices.remove(selectedVertex);
			journey.add(selectedVertex);
			currentVertex = selectedVertex;
		}
		journey.add(endVertex);
		return journey;
	}

	private List<Vertex> greedyJourney(Vertex startVertex, Vertex endVertex) {
		/*if (!checkpointVertices.contains(startVertex) || !checkpointVertices.contains(endVertex)) {
			throw new EndpointCellsException();
		}*/
		List<Path> paths = new ArrayList<Path>(shortestPathsBetweenCheckpoints.values());
		class ReducedGraphPathComparator implements Comparator<Path> {

			@Override
			public int compare(Path o1, Path o2) {
				return Double.compare(o1.getTotalWeight(), o2.getTotalWeight());
			}

		}
		CommonAlgorithms.mergeSort(paths, new ReducedGraphPathComparator());
		DisjointSet<Vertex> verticesDisjointSet = new DisjointSet<Vertex>(checkpointVertices);
		List<Vertex> journey = new ArrayList<Vertex>();
		List<Path> addedPaths = new ArrayList<Path>();
		//use arraylist because can either have 2 or 1 adjacent cells (variable number)
		HashMap<Vertex, ArrayList<Vertex>> adjacencyListMap = new HashMap<Vertex, ArrayList<Vertex>>();
		for (Vertex vertex : checkpointVertices) {
			adjacencyListMap.put(vertex, new ArrayList<Vertex>());
		}
		for (int i = 0; i < paths.size() && addedPaths.size() < checkpointVertices.size(); i++) {
			Path path = paths.get(i);
			Vertex vertex1 = path.getReducedGraphVertex1();
			Vertex vertex2 = path.getReducedGraphVertex2();
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
		Vertex previousVertex = null;
		Vertex currentVertex = startVertex;
		while (journey.size() < checkpointVertices.size()) {
			ArrayList<Vertex> adjacencyList = adjacencyListMap.get(currentVertex);
			for (Vertex vertex : adjacencyList) {
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

	private List<Vertex> twoOptSwap(List<Vertex> journey, int a, int b) {
		List<Vertex> newJourney = new ArrayList<Vertex>();
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

	private List<Vertex> twoOpt(List<Vertex> journey) {
		List<Vertex> currentJourney = journey;
		double minChange;
		do {
			minChange = 0;
			int minI = -1, minJ = -1;
			for (int i = 1; i < journey.size() - 2; i++) {
				for (int j = i + 1; j < journey.size() - 1; j++) {
					Vertex a = currentJourney.get(i - 1);
					Vertex b = currentJourney.get(i);
					Vertex c = currentJourney.get(j);
					Vertex d = currentJourney.get(j + 1);
					double previousWeight = getShortestPathDistanceBetweenCheckpoints(a, b)
							+ getShortestPathDistanceBetweenCheckpoints(c, d);
					double afterWeight = getShortestPathDistanceBetweenCheckpoints(a, d)
							+ getShortestPathDistanceBetweenCheckpoints(c, b);
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
