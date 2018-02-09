package logic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import map.Cell;
import user_interface.Application;
import dsa.DisjointSet;
import dsa.FibonacciHeap;

public class AIPlayer extends Player {
	private final ReducedGraph reducedGraph; // separating reduced graph from
												// aiplayer so that multiple AI
												// (monsters) could be added in
												// the future; cohesive modules
	// instance variable because I can then use it for random walks

	private final Map<CheckpointVertexPair, Path> shortestPathsBetweenCheckpointVertices; // complete
																							// graph,
																							// adj
																							// matrix
																							// better
																							// than
																							// adj
																							// list
																							// in
																							// terms
																							// of
																							// space
																							// and
																							// lookup
	// key accounts for both orders so memory space halved with this map and the
	// checkpointvertices set vs a map of maps

	private final List<Cell> cellRoute;
	private int currentCellIndex;

	public AIPlayer(Cell startCell, Cell endCell, double baseVel,
			double toleranceConstant, Color color, String name,
			double playerProportionOfCellDimensions, int numCheckpointsToReach,
			ReducedGraph reducedGraph) {
		super(startCell, endCell, baseVel, toleranceConstant, color, name,
				playerProportionOfCellDimensions, numCheckpointsToReach);
		this.reducedGraph = reducedGraph;
		shortestPathsBetweenCheckpointVertices = initShortestPathsBetweenCheckpointVertices();
		cellRoute = generateCellRoute(reducedGraph.getVertex(startCell),
				reducedGraph.getVertex(endCell));
		currentCellIndex = 0;
	}

	private Map<CheckpointVertexPair, Path> initShortestPathsBetweenCheckpointVertices() {
		Collection<RGVertex> vertices = reducedGraph.getVertices();
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();
		Map<CheckpointVertexPair, Path> shortestPathsBetweenCheckpointVertices = new HashMap<CheckpointVertexPair, Path>();
		// Set<Vertex> doneCheckpoints = new HashSet<Vertex>();
		for (RGVertex sourceVertex : checkpointVertices) {
			// Dijkstra's from each checkpointVertex
			Map<RGVertex, Double> weightsFromSourceMap = new HashMap<RGVertex, Double>();
			Map<RGVertex, RGVertex> previousVerticesMap = new HashMap<RGVertex, RGVertex>();
			Map<RGVertex, FibonacciHeap.Entry<RGVertex>> queueEntriesMap = new HashMap<RGVertex, FibonacciHeap.Entry<RGVertex>>();
			FibonacciHeap<RGVertex> vertexQueue = new FibonacciHeap<RGVertex>();
			Set<RGVertex> visitedVertices = new HashSet<RGVertex>();
			// Set<Vertex> visitedCheckpoints = new
			// HashSet<Vertex>(doneCheckpoints);
			for (RGVertex vertex : vertices) {
				weightsFromSourceMap.put(vertex, 1000000000d);
				queueEntriesMap.put(
						vertex,
						vertexQueue.enqueue(vertex,
								weightsFromSourceMap.get(vertex)));
			}
			weightsFromSourceMap.put(sourceVertex, 0.0);
			vertexQueue.decreaseKey(queueEntriesMap.get(sourceVertex),
					weightsFromSourceMap.get(sourceVertex));
			while (!vertexQueue.isEmpty()) {// && checkpointVertices.size() !=
											// visitedCheckpoints.size()) {
				FibonacciHeap.Entry<RGVertex> currentVertexEntry = vertexQueue
						.dequeueMin();
				RGVertex currentVertex = currentVertexEntry.getValue();
				visitedVertices.add(currentVertex);
				/*
				 * if(checkpointVertices.contains(currentVertex)) {
				 * visitedCheckpoints.add(currentVertex); }
				 */for (RGAdjacency adjacency : currentVertex.getAdjacencies()) {
					RGVertex adjacentVertex = adjacency.getAdjacentVertex();
					if (!visitedVertices.contains(adjacentVertex)) {
						double alternativeWeight = weightsFromSourceMap
								.get(currentVertex)
								+ currentVertex
										.getWeightToAdjacentVertex(adjacentVertex);
						if (alternativeWeight < weightsFromSourceMap
								.get(adjacentVertex)) {
							weightsFromSourceMap.put(adjacentVertex,
									alternativeWeight);
							previousVerticesMap.put(adjacentVertex,
									currentVertex);
							vertexQueue.decreaseKey(
									queueEntriesMap.get(adjacentVertex),
									alternativeWeight);
						}
					}

				}
			}
			for (RGVertex checkpointVertex : checkpointVertices) {
				if (checkpointVertex != sourceVertex) {
					if (!shortestPathsBetweenCheckpointVertices
							.containsKey(new CheckpointVertexPair(sourceVertex,
									checkpointVertex))) {
						Path pairPath = new Path();
						RGVertex currentVertex = checkpointVertex;
						while (currentVertex != null) {
							pairPath.appendVertex(currentVertex);
							currentVertex = previousVerticesMap
									.get(currentVertex);
						}
						shortestPathsBetweenCheckpointVertices.put(
								new CheckpointVertexPair(sourceVertex,
										checkpointVertex), pairPath);
					}
				}
			}
			// doneCheckpoints.add(sourceVertex);
		}
		return shortestPathsBetweenCheckpointVertices;
	}

	private double getShortestPathWeightBetweenCheckpointVertices(RGVertex v1,
			RGVertex v2) {
		return shortestPathsBetweenCheckpointVertices.get(
				new CheckpointVertexPair(v1, v2)).getTotalWeight();
	}

/*	private double calculateTotalCheckpointVertexRouteWeight(
			List<RGVertex> checkpointVertexRoute) {
		double totalWeight = 0;
		for (int i = 0; i < checkpointVertexRoute.size() - 1; i++) {
			RGVertex currentVertex = checkpointVertexRoute.get(i);
			RGVertex nextVertex = checkpointVertexRoute.get(i + 1);
			totalWeight += getShortestPathWeightBetweenCheckpointVertices(
					currentVertex, nextVertex);
		}
		return totalWeight;
	}
*/
	private List<Cell> generateCellRoute(RGVertex startVertex,
			RGVertex endVertex) {
		List<RGVertex> checkpointVertexRoute = twoOpt(generateGreedyCheckpointVertexRoute(
				startVertex, endVertex));
		List<Cell> cellRoute = new ArrayList<Cell>();
		for (int i = 0; i < checkpointVertexRoute.size() - 1; i++) {
			RGVertex currentCheckpointVertex = checkpointVertexRoute.get(i);
			RGVertex nextCheckpointVertex = checkpointVertexRoute.get(i + 1);
			List<RGVertex> pathVertices = shortestPathsBetweenCheckpointVertices
					.get(new CheckpointVertexPair(currentCheckpointVertex,
							nextCheckpointVertex)).getPathVertices();
			int inclusiveStartIndex = 0, exclusiveEndIndex = pathVertices
					.size() - 1, increment = 1;
			if (pathVertices.get(pathVertices.size() - 1) == currentCheckpointVertex) {
				inclusiveStartIndex = pathVertices.size() - 1;
				exclusiveEndIndex = 0;
				increment = -1;
			} else if (pathVertices.get(0) != currentCheckpointVertex) {
				return null;
			}
			for (int index = inclusiveStartIndex; index != exclusiveEndIndex; index += increment) {
				RGVertex currentVertex = pathVertices.get(index);
				RGVertex nextVertex = pathVertices.get(index + increment);
				RGEdge edge = currentVertex.getEdgeTo(nextVertex);
				List<Cell> edgeCells = edge.getCells();
				int inclusiveStartIndex2 = 0, exclusiveEndIndex2 = edgeCells
						.size(), increment2 = 1;
				if (currentVertex.getSuperCell() == edgeCells.get(edgeCells
						.size() - 1)) {
					inclusiveStartIndex2 = edgeCells.size() - 1;
					exclusiveEndIndex2 = -1;
					increment2 = -1;
				} else if (edgeCells.get(0) != currentVertex.getSuperCell()) {
					return null;
				}
				for (int index2 = inclusiveStartIndex2; index2 != exclusiveEndIndex2; index2 += increment2) {
					cellRoute.add(edgeCells.get(index2));
				}
			}
		}
		if(cellRoute.get(0) == startVertex.getSuperCell() && cellRoute.get(cellRoute.size()-1) == endVertex.getSuperCell()) {
			System.out.println("WORKS");
		}
		return cellRoute;
	}

/*	private List<RGVertex> generateCheckpointVertexRoute(RGVertex startVertex,
			RGVertex endVertex) {
		List<RGVertex> greedyRoute = twoOpt(generateGreedyCheckpointVertexRoute(
				startVertex, endVertex));
		double minTotalWeight = calculateTotalCheckpointVertexRouteWeight(greedyRoute);
		List<RGVertex> bestRoute = greedyRoute;
		// need to move these into parameters/constants
		final int numRNNCands = 2;
		final int numRNNs = 100;
		for (int i = 0; i < numRNNs; i++) {
			List<RGVertex> rNNRoute = twoOpt(generateRandomisedNearestNeighbourCheckpointVertexRoute(
					startVertex, endVertex, numRNNCands));
			double totalDistance = calculateTotalCheckpointVertexRouteWeight(rNNRoute);
			if (totalDistance < minTotalWeight) {
				minTotalWeight = totalDistance;
				bestRoute = rNNRoute;
			}
		}
		return bestRoute;
	}
*/
	/*private List<RGVertex> generateRandomisedNearestNeighbourCheckpointVertexRoute(
			RGVertex startVertex, RGVertex endVertex, int numCands) {
		// defensive programming (ctrl-f Exception or throw), prob can get rid
		// of it, if you decide to keep it, check for it elsewhere
		List<RGVertex> route = null;
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();
		if (startVertex.getSuperCell().isCheckpoint()
				&& endVertex.getSuperCell().isCheckpoint()) {
			Set<RGVertex> unselectedVertices = new HashSet<RGVertex>(
					checkpointVertices);
			route = new ArrayList<RGVertex>();
			unselectedVertices.remove(startVertex);
			unselectedVertices.remove(endVertex);
			route.add(startVertex);
			RGVertex currentVertex = startVertex;
			class Candidate implements Comparable<Candidate> {
				private final RGVertex vertex;
				private final double weight;

				public Candidate(RGVertex node, double weight) {
					this.vertex = node;
					this.weight = weight;
				}

				public RGVertex getVertex() {
					return vertex;
				}

				@Override
				public int compareTo(Candidate other) {
					return Double.compare(other.weight, weight);
				}
			}

			while (unselectedVertices.size() > 0) {
				PriorityQueue<Candidate> candidates = new PriorityQueue<Candidate>();
				for (RGVertex vertex : unselectedVertices) {
					double distance = getShortestPathWeightBetweenCheckpointVertices(
							currentVertex, vertex);
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
				for (Candidate cand : candidates) {
					if (i == randIndex) {
						selectedCandidate = cand;
						break;
					}
					i++;
				}
				RGVertex selectedVertex = selectedCandidate.getVertex();
				unselectedVertices.remove(selectedVertex);
				route.add(selectedVertex);
				currentVertex = selectedVertex;
			}
			route.add(endVertex);
		}
		return route;
	}
*/
	private List<RGVertex> generateGreedyCheckpointVertexRoute(
			RGVertex startVertex, RGVertex endVertex) {
		List<RGVertex> route = null;
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();
		if (startVertex.getSuperCell().isCheckpoint()
				&& endVertex.getSuperCell().isCheckpoint()) {
			List<Path> paths = new ArrayList<Path>(
					shortestPathsBetweenCheckpointVertices.values());
			mergeSort(paths, 0, paths.size());
			DisjointSet<RGVertex> checkpointVerticesDisjointSet = new DisjointSet<RGVertex>(
					checkpointVertices);
			route = new ArrayList<RGVertex>();

			// use arraylist because can either have 2 or 1 adjacent cells
			// (variable number)
			HashMap<RGVertex, ArrayList<RGVertex>> routeAdjacencyListMap = new HashMap<RGVertex, ArrayList<RGVertex>>();
			for (RGVertex checkpointVertex : checkpointVertices) {
				routeAdjacencyListMap.put(checkpointVertex, new ArrayList<RGVertex>());
			}
			int i = 0;
			int pathsAdded = 0;
			for (i = 0; i < paths.size()
					&& pathsAdded < checkpointVertices.size() - 3; i++) {
				Path path = paths.get(i);
				List<RGVertex> pathVertices = path.getPathVertices();
				RGVertex vertex1 = pathVertices.get(0);
				RGVertex vertex2 = pathVertices.get(pathVertices.size()-1);
				if (vertex1 != startVertex && vertex1 != endVertex
						&& vertex2 != startVertex && vertex2 != endVertex) {
					if (routeAdjacencyListMap.get(vertex1).size() != 2 && routeAdjacencyListMap.get(vertex2).size() != 2
							&& !checkpointVerticesDisjointSet.areJoined(
									vertex1, vertex2)) {
						checkpointVerticesDisjointSet.join(vertex1, vertex2);
						pathsAdded++;
						routeAdjacencyListMap.get(vertex1).add(vertex2);
						routeAdjacencyListMap.get(vertex2).add(vertex1);
					}
				}
			}
			RGVertex orderOneCheckpointVertex1 = null, orderOneCheckpointVertex2 = null;
			for (RGVertex checkpointVertex : checkpointVertices) {
				if (routeAdjacencyListMap.get(checkpointVertex).size() == 1) {
					if (orderOneCheckpointVertex1 == null) {
						orderOneCheckpointVertex1 = checkpointVertex;
					} else {
						orderOneCheckpointVertex2 = checkpointVertex;
						break;
					}
				}
			}
			if (getShortestPathWeightBetweenCheckpointVertices(orderOneCheckpointVertex1,
					startVertex)
					+ getShortestPathWeightBetweenCheckpointVertices(orderOneCheckpointVertex2,
							endVertex) < getShortestPathWeightBetweenCheckpointVertices(
					orderOneCheckpointVertex2, startVertex)
					+ getShortestPathWeightBetweenCheckpointVertices(orderOneCheckpointVertex1,
							endVertex)) {
				routeAdjacencyListMap.get(orderOneCheckpointVertex1).add(startVertex);
				routeAdjacencyListMap.get(startVertex).add(orderOneCheckpointVertex1);
				routeAdjacencyListMap.get(orderOneCheckpointVertex2).add(endVertex);
				routeAdjacencyListMap.get(endVertex).add(orderOneCheckpointVertex2);
			} else {
				routeAdjacencyListMap.get(orderOneCheckpointVertex2).add(startVertex);
				routeAdjacencyListMap.get(startVertex).add(orderOneCheckpointVertex2);
				routeAdjacencyListMap.get(orderOneCheckpointVertex1).add(endVertex);
				routeAdjacencyListMap.get(endVertex).add(orderOneCheckpointVertex1);
			}
			route.add(startVertex);
			RGVertex previousVertex = null;
			RGVertex currentVertex = startVertex;
			while (route.size() < checkpointVertices.size()) {
				ArrayList<RGVertex> adjacencyList = routeAdjacencyListMap
						.get(currentVertex);
				for (RGVertex nextVertex : adjacencyList) {
					if (nextVertex != previousVertex) {
						route.add(nextVertex);
						previousVertex = currentVertex;
						currentVertex = nextVertex;
						break;
					}
				}
			}
		}
		return route;
	}

	private void mergeSort(List<Path> paths, int startIndex, int endIndex) {
		if (startIndex < endIndex - 1) {
			int midIndex = (startIndex + endIndex) / 2;
			mergeSort(paths, startIndex, midIndex);
			mergeSort(paths, midIndex, endIndex);
			merge(paths, startIndex, midIndex, endIndex);
		}
	}

	private void merge(List<Path> paths, int startIndex, int midIndex,
			int endIndex) {
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
		for(int insertIndex = startIndex; insertIndex < endIndex; insertIndex++) {
			if(leftIndex < leftHalf.size() && (rightIndex >= rightHalf.size() || leftHalf.get(leftIndex).getTotalWeight() < rightHalf.get(
					rightIndex).getTotalWeight())) {
				paths.set(insertIndex, leftHalf.get(leftIndex));
				leftIndex++;
			}  else {
				paths.set(insertIndex, rightHalf.get(rightIndex));
				rightIndex++;
			}
		}
	}

	private void twoOptSwap(List<RGVertex> checkpointVertexRoute,
			int startIndex, int endIndex) {
		for (int i = 0; i < (endIndex - startIndex + 1) / 2; i++) {
			Collections.swap(checkpointVertexRoute, startIndex + i, endIndex
					- i);
		}
	}

	private List<RGVertex> twoOpt(List<RGVertex> checkpointVertexRoute) {
		List<RGVertex> improvedCheckpointVertexRoute = checkpointVertexRoute;
		double minChange;
		do {
			minChange = 0;
			int minIndex1 = -1;
			int minIndex2 = -1;
			for (int index1 = 2; index1 < checkpointVertexRoute.size() - 3; index1++) {
				for (int index2 = index1 + 1; index2 < checkpointVertexRoute.size() - 2; index2++) {
					RGVertex a = improvedCheckpointVertexRoute.get(index1 - 1);
					RGVertex b = improvedCheckpointVertexRoute.get(index1);
					RGVertex c = improvedCheckpointVertexRoute.get(index2);
					RGVertex d = improvedCheckpointVertexRoute.get(index2 + 1);
					double previousWeight = getShortestPathWeightBetweenCheckpointVertices(
							a, b)
							+ getShortestPathWeightBetweenCheckpointVertices(c,
									d);
					double afterWeight = getShortestPathWeightBetweenCheckpointVertices(
							a, c)
							+ getShortestPathWeightBetweenCheckpointVertices(b,
									d);
					double change = afterWeight - previousWeight;
					if (change < minChange) {
						minChange = change;
						minIndex1 = index1;
						minIndex2 = index2;
					}
				}
			}
			if (minChange != 0) {
				twoOptSwap(improvedCheckpointVertexRoute, minIndex1, minIndex2);
			}
		} while (minChange != 0);
		return improvedCheckpointVertexRoute;
	}

	@Override
	public void update() {
		Cell nextRouteCell = null;
		if (currentCellIndex < cellRoute.size()-1) {
			Cell currentRouteCell = cellRoute.get(currentCellIndex);
			nextRouteCell = cellRoute.get(currentCellIndex + 1);
			if (currentRouteCell != nextRouteCell) {
				Direction targetDirection = currentRouteCell
						.getDirectionToNeighbouringCell(nextRouteCell);
				if (targetDirection != currentDirection) {
					changeDirection(targetDirection);
				}
			}
		} else {
			System.out.println("FINISHED");
		}
		super.update();
		if (currentCell == nextRouteCell) {
			currentCellIndex++;
		}
	}

}
