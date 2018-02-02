package logic;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import logic.ReducedGraph.Edge;
import logic.ReducedGraph.Vertex;
import map.Cell;
import user_interface.Application;
import user_interface.Difficulty;
import dsa.DisjointSet;
import dsa.FibonacciHeap;

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
			if (pathVertices.size() > 1) {
				totalWeight += vertex.getDistanceToAdjacentVertex(pathVertices.get(pathVertices.size() - 2));
			}
		}

		public double getTotalWeight() {
			return totalWeight;
		}

		public Vertex getVertex1() {
			return pathVertices.get(0);
		}

		public Vertex getVertex2() {
			return pathVertices.get(pathVertices.size() - 1);
		}
		
		public List<Cell> getCellPath(Vertex startVertex) {
			List<Cell> cellPath = new ArrayList<Cell>();
			int inclusiveStartIndex = 0, exclusiveEndIndex = pathVertices.size()-1, increment = 1;
			if(pathVertices.get(pathVertices.size()-1) == startVertex) {
				inclusiveStartIndex = pathVertices.size()-1;
				exclusiveEndIndex = 0;
				increment = -1;
			} else if(pathVertices.get(0) != startVertex){
				return null;
			}
			for (int i = inclusiveStartIndex; i != exclusiveEndIndex; i+=increment) {
				Vertex currentVertex = pathVertices.get(i);
				Vertex nextVertex = pathVertices.get(i + increment);
				Edge edge = currentVertex.getEdgeTo(nextVertex);
				List<Cell> edgeCells = edge.getCells(currentVertex.getCell());
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
	private int currentCellIndex;
	public AIPlayer(Cell startCell, Cell endCell, double baseVel, Color color, String name, double playerProportionOfCellDimensions, int numCheckpointsToReach,
			ReducedGraph rg, Difficulty difficulty) {
		super(startCell, endCell, baseVel, color, name, playerProportionOfCellDimensions, numCheckpointsToReach);
		this.rg = rg;
		checkpointVertices = rg.getCheckpointVertices();
		initCheckpointGraph();
		System.out.println("Initialised checkpoint graph");
		journey = cellJourney(rg.getVertex(startCell), rg.getVertex(endCell), difficulty);
		System.out.println(journey.size());
		currentCellIndex = 0;
	}
	
	@Override
	public void update(double delta) {		
		Cell nextJourneyCell = null;
		if(currentCellIndex < journey.size()-1) {
			Cell currentJourneyCell = journey.get(currentCellIndex);
			nextJourneyCell = journey.get(currentCellIndex+1);
			if(currentJourneyCell != nextJourneyCell) {
				Direction targetDirection = currentJourneyCell.getDirectionTo(nextJourneyCell);
				if(targetDirection != currentDirection) {
					changeDirection(targetDirection);
				}
			}
		}
		super.update(delta);
		if(currentCell == nextJourneyCell) {
			currentCellIndex++;
		}
	}
	
	private void initCheckpointGraph() {
		shortestPathsBetweenCheckpoints = new HashMap<CheckpointVertexPair, Path>();
		//Set<Vertex> doneCheckpoints = new HashSet<Vertex>();
		for (Vertex sourceVertex : checkpointVertices) {
			//Dijkstra's from each checkpointVertex
			Map<Vertex, Double> distancesFromSourceMap = new HashMap<Vertex, Double>();
			Map<Vertex, Vertex> previousVerticesMap = new HashMap<Vertex, Vertex>();
			Map<Vertex, FibonacciHeap.Entry<Vertex>> queueEntriesMap = new HashMap<Vertex, FibonacciHeap.Entry<Vertex>>();
			FibonacciHeap<Vertex> vertexQueue = new FibonacciHeap<Vertex>();
			Set<Vertex> visitedVertices = new HashSet<Vertex>();
			//Set<Vertex> visitedCheckpoints = new HashSet<Vertex>(doneCheckpoints);
			for (Vertex vertex : rg.getVertices()) {
				distancesFromSourceMap.put(vertex, 1000000000d);
				queueEntriesMap.put(vertex, vertexQueue.enqueue(vertex, distancesFromSourceMap.get(vertex)));
			}
			distancesFromSourceMap.put(sourceVertex, 0.0);
			vertexQueue.decreaseKey(queueEntriesMap.get(sourceVertex), distancesFromSourceMap.get(sourceVertex));
			while (!vertexQueue.isEmpty()) {// && checkpointVertices.size() != visitedCheckpoints.size()) {				
				FibonacciHeap.Entry<Vertex> currentVertexEntry = vertexQueue.dequeueMin();
				Vertex currentVertex = currentVertexEntry.getValue();
				if(!visitedVertices.contains(currentVertex)) {
					visitedVertices.add(currentVertex);
/*					if(checkpointVertices.contains(currentVertex)) {
						visitedCheckpoints.add(currentVertex);
					}
*/					for (Vertex neighbour : currentVertex.getAdjacentVertices()) {
						if(!visitedVertices.contains(neighbour)) {
							double altWeight = distancesFromSourceMap.get(currentVertex)
									+ currentVertex.getDistanceToAdjacentVertex(neighbour);
							if (altWeight < distancesFromSourceMap.get(neighbour)) {
								distancesFromSourceMap.put(neighbour, altWeight);
								previousVerticesMap.put(neighbour, currentVertex);
								vertexQueue.decreaseKey(queueEntriesMap.get(neighbour), altWeight);
							}
						}
					}
				}
			}
			for (Vertex vertex : checkpointVertices) {
				if (vertex != sourceVertex) {
					if (!shortestPathsBetweenCheckpoints.containsKey(
							new CheckpointVertexPair(sourceVertex, vertex))) {
						Path path = new Path();
						Vertex currentVertex = vertex;
						int length = 0;
						while (currentVertex != null) {
							length++;
							path.appendVertex(currentVertex);
							Vertex previousVertex = previousVerticesMap.get(currentVertex);
							currentVertex = previousVertex;
						}
						System.out.println("path length: " +length);
						shortestPathsBetweenCheckpoints.put(new CheckpointVertexPair(sourceVertex, vertex),
								path);
					}
				}
			}
			//doneCheckpoints.add(sourceVertex);
		}
	}
	
	private double getShortestPathDistanceBetweenCheckpoints(Vertex v1, Vertex v2) {
		return shortestPathsBetweenCheckpoints.get(new CheckpointVertexPair(v1, v2)).getTotalWeight();
	}
	
	private double calculateTotalCheckpointVertexJourneyDistance(List<Vertex> journey) {
		double totalDistance = 0;
		for (int i = 0; i < journey.size() - 1; i++) {
			Vertex currentVertex = journey.get(i);
			Vertex nextVertex = journey.get(i + 1);
			totalDistance += getShortestPathDistanceBetweenCheckpoints(currentVertex, nextVertex);
		}
		return totalDistance;
	}
	
	private List<Vertex> easyCheckpointVertexJourney(Vertex startVertex, Vertex endVertex) {
		List<Vertex> journey = null;
		if (checkpointVertices.contains(startVertex) && checkpointVertices.contains(endVertex)) {
			journey = new ArrayList<Vertex>(checkpointVertices);
			Collections.shuffle(journey, Application.rng);
			Collections.swap(journey, 0, journey.indexOf(startVertex));
			Collections.swap(journey, journey.size()-1, journey.indexOf(endVertex));
		}
		return journey;
	}
	
	private List<Vertex> mediumCheckpointVertexJourney(Vertex startVertex, Vertex endVertex) {
		//need to move these into parameters/constants
		final int numRNNCands = 1;
		return randomisedNearestNeighbourCheckpointVertexJourney(startVertex, endVertex, numRNNCands);
	}
	
	private List<Vertex> hardCheckpointVertexJourney(Vertex startVertex, Vertex endVertex) {
		System.out.println("REACHED HARDJOURNEY");
		List<Vertex> greedyJourney = twoOpt(greedyCheckpointVertexJourney(startVertex, endVertex));
		double minTotalDistance = calculateTotalCheckpointVertexJourneyDistance(greedyJourney);
		List<Vertex> bestJourney = greedyJourney;
		//need to move these into parameters/constants
		final int numRNNCands = 2;
		final int numRNNs = 100;
		for(int i = 0; i < numRNNs; i++) {
			List<Vertex> rNNJourney = twoOpt(randomisedNearestNeighbourCheckpointVertexJourney(startVertex, endVertex, numRNNCands));
			double totalDistance = calculateTotalCheckpointVertexJourneyDistance(rNNJourney);
			if(totalDistance < minTotalDistance) {
				minTotalDistance = totalDistance;
				bestJourney = rNNJourney;
			}
		}
		return bestJourney;
	}
	
	private List<Cell> cellJourney(Vertex startVertex, Vertex endVertex, Difficulty difficulty) {
		List<Vertex> checkpointVertexJourney;
		switch(difficulty) {
		case EASY: checkpointVertexJourney = easyCheckpointVertexJourney(startVertex, endVertex); break;
		case MEDIUM: checkpointVertexJourney = mediumCheckpointVertexJourney(startVertex, endVertex); break;
		case HARD: checkpointVertexJourney = hardCheckpointVertexJourney(startVertex, endVertex); break;
		default: checkpointVertexJourney = null;
		}
		List<Cell> cellJourney = new ArrayList<Cell>();
		for (int i = 0; i < checkpointVertexJourney.size() - 1; i++) {
			Vertex currentCheckpointVertex = checkpointVertexJourney.get(i);
			Vertex nextCheckpointVertex = checkpointVertexJourney.get(i + 1);
			List<Cell> cellPath = shortestPathsBetweenCheckpoints.get(new CheckpointVertexPair(currentCheckpointVertex, nextCheckpointVertex)).getCellPath(currentCheckpointVertex);
			cellJourney.addAll(cellPath);
		}
		return cellJourney;
	}

	private List<Vertex> randomisedNearestNeighbourCheckpointVertexJourney(Vertex startVertex, Vertex endVertex, int numCands) {
		// defensive programming (ctrl-f Exception or throw), prob can get rid of it, if you decide to keep it, check for it elsewhere
		List<Vertex> journey = null;
		if (checkpointVertices.contains(startVertex) && checkpointVertices.contains(endVertex)) {
			Set<Vertex> unselectedVertices = new HashSet<Vertex>(checkpointVertices);
			journey = new ArrayList<Vertex>();
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
				int randIndex = Application.rng.nextInt(candidates.size());
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
		}
		return journey;
	}

	private void merge(List<Path> paths, int startIndex, int midIndex, int endIndex) {
		List<Path> leftHalf = new ArrayList<Path>();
		List<Path> rightHalf = new ArrayList<Path>();
		for (int i = startIndex; i < midIndex; i++) {
			leftHalf.add(paths.get(i));
		}
		for (int i = midIndex; i < endIndex; i++) {
			rightHalf.add(paths.get(i));
		}
		int leftIndex = 0;
		int rightIndex = 0;
		int insertIndex = startIndex;
		while (leftIndex < leftHalf.size() && rightIndex < rightHalf.size()) {
			if(leftHalf.get(leftIndex).getTotalWeight() < rightHalf.get(rightIndex).getTotalWeight()) {
				paths.set(insertIndex, leftHalf.get(leftIndex));
				leftIndex++;
			} else {
				paths.set(insertIndex, rightHalf.get(rightIndex));
				rightIndex++;
			}
			insertIndex++;
		}
		while(leftIndex < leftHalf.size()) {
			paths.set(insertIndex, leftHalf.get(leftIndex));
			leftIndex++;
			insertIndex++;
		} 
		while(rightIndex < rightHalf.size()) {
			paths.set(insertIndex, rightHalf.get(rightIndex));
			rightIndex++;
			insertIndex++;
		}
	}

	private void mergeSort(List<Path> paths, int startIndex, int endIndex) {
		if (startIndex < endIndex-1) {
			int midIndex = (startIndex + endIndex) / 2;
			mergeSort(paths, startIndex, midIndex);
			mergeSort(paths, midIndex, endIndex);
			merge(paths, startIndex, midIndex, endIndex);
		}
	}
	
	private List<Vertex> greedyCheckpointVertexJourney(Vertex startVertex, Vertex endVertex) {
		List<Vertex> journey = null;
		if (checkpointVertices.contains(startVertex) && checkpointVertices.contains(endVertex)) {
			List<Path> paths = new ArrayList<Path>(shortestPathsBetweenCheckpoints.values());
			class PathComparator implements Comparator<Path> {
				@Override
				public int compare(Path path1, Path path2) {
					return Double.compare(path1.getTotalWeight(), path2.getTotalWeight());
				}
			}
			Collections.sort(paths, new PathComparator());
			mergeSort(paths, 0, paths.size());
			//CommonAlgorithms.mergeSort(paths, new ReducedGraphPathComparator());
			DisjointSet<Vertex> verticesDisjointSet = new DisjointSet<Vertex>(checkpointVertices);
			journey = new ArrayList<Vertex>();
			
			//use arraylist because can either have 2 or 1 adjacent cells (variable number)
			HashMap<Vertex, ArrayList<Vertex>> adjacencyListMap = new HashMap<Vertex, ArrayList<Vertex>>();
			for (Vertex vertex : checkpointVertices) {
				adjacencyListMap.put(vertex, new ArrayList<Vertex>());
			}
			int i = 0;
			int pathsAdded = 0;
			for (i = 0; i < paths.size() && pathsAdded < checkpointVertices.size()-3; i++) {
				Path path = paths.get(i);
				Vertex vertex1 = path.getVertex1();
				Vertex vertex2 = path.getVertex2();
				if(vertex1 != startVertex && vertex1 != endVertex && vertex2 != startVertex && vertex2 != endVertex) {
					int vertex1Order = adjacencyListMap.get(vertex1).size();
					int vertex2Order = adjacencyListMap.get(vertex2).size();
					boolean vertex1IsMaxOrder = vertex1Order == 2;
					boolean vertex2IsMaxOrder = vertex2Order == 2;
					boolean verticesAreJoined = verticesDisjointSet.areJoined(vertex1, vertex2);
					if (!vertex1IsMaxOrder && !vertex2IsMaxOrder && !verticesAreJoined) {
						verticesDisjointSet.union(vertex1, vertex2);
						pathsAdded++;
						adjacencyListMap.get(vertex1).add(vertex2);
						adjacencyListMap.get(vertex2).add(vertex1);
					}
				}
			}
			Vertex vertex1 = null, vertex2 = null;
			for(Vertex vertex : checkpointVertices) {
				if(adjacencyListMap.get(vertex).size()==1) {
					if(vertex1 == null) {
						vertex1 = vertex;
					} else {
						vertex2 = vertex;
						break;
					}
				}
			}
			if(getShortestPathDistanceBetweenCheckpoints(vertex1, startVertex) + getShortestPathDistanceBetweenCheckpoints(vertex2, endVertex) < getShortestPathDistanceBetweenCheckpoints(vertex2, startVertex) + getShortestPathDistanceBetweenCheckpoints(vertex1, endVertex)) {
				adjacencyListMap.get(vertex1).add(startVertex);
				adjacencyListMap.get(startVertex).add(vertex1);
				adjacencyListMap.get(vertex2).add(endVertex);
				adjacencyListMap.get(endVertex).add(vertex2);
			} else {
				adjacencyListMap.get(vertex2).add(startVertex);
				adjacencyListMap.get(startVertex).add(vertex2);
				adjacencyListMap.get(vertex1).add(endVertex);
				adjacencyListMap.get(endVertex).add(vertex1);
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
		int minI, minJ;
		do {
			minChange = 0;
			minI = -1;
			minJ = -1;
			for (int i = 2; i < journey.size() - 3; i++) {
				for (int j = i + 1; j < journey.size() - 2; j++) {
					Vertex a = currentJourney.get(i - 1);
					Vertex b = currentJourney.get(i);
					Vertex c = currentJourney.get(j);
					Vertex d = currentJourney.get(j + 1);
					double previousWeight = getShortestPathDistanceBetweenCheckpoints(a, b)
							+ getShortestPathDistanceBetweenCheckpoints(c, d);
					double afterWeight = getShortestPathDistanceBetweenCheckpoints(a, c)
							+ getShortestPathDistanceBetweenCheckpoints(b, d);
					double change = afterWeight - previousWeight;
					if (change < minChange) {
						minChange = change;
						minI = i;
						minJ = j;
					}
				}
			}
			if(minI != -1) {
				currentJourney = twoOptSwap(currentJourney, minI, minJ);
			}
		} while (minI != -1);
		return currentJourney;
	}
	
}
