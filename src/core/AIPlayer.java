package core;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Collections2;

public class AIPlayer extends Player {
	/*
	 * This class is used to represent Player objects that are controlled by the
	 * computer (as opposed to a human).
	 */

	/*
	 * This class extends the Player class (inheritance). The changeDirection
	 * method is inherited, allowing the AIPlayer object to change direction in
	 * response to key presses. The inherited update method is overriden
	 * (polymorphism) to enable the AIPlayer to change direction when required.
	 */

	private class CheckpointVertexPair {
		/*
		 * This inner class consists of two checkpoint RGVertex objects, to be
		 * used as a key for the shortestPathsBetweenCheckpointVertices hash
		 * table in the AIPlayer class.
		 */

		/*
		 * The two checkpoint RGVertex objects. This is aggregation as the
		 * CheckpointVertexPair class has a HAS-A relationship with the RGVertex
		 * class but the checkpointVertex1 and checkpointVertex2 objects will
		 * not be destroyed if the CheckpointVertexPair object is destroyed.
		 */
		private final RGVertex checkpointVertex1;
		private final RGVertex checkpointVertex2;

		/*
		 * Constructor.
		 */
		public CheckpointVertexPair(RGVertex checkpointVertex1,
				RGVertex checkpointVertex2) {
			this.checkpointVertex1 = checkpointVertex1;
			this.checkpointVertex2 = checkpointVertex2;
		}

		/*
		 * Generates the hash code for the CheckpointVertexPair object. Always
		 * gives the same hash code given a CheckpointVertexPair object with the
		 * same two RGVertex objects.
		 */
		@Override
		public int hashCode() {

			/*
			 * Multiplication is a commutative operation, the order of the Cell
			 * objects does not matter, so given the same two Cell objects, the
			 * hash code will always be the same, regardless of the order.
			 */
			return checkpointVertex1.hashCode() * checkpointVertex2.hashCode();
		}

		/*
		 * Checks if two CheckpointVertexPair objects are equal (i.e. if they
		 * both have the same RGVertex objects).
		 */
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

		/*
		 * Getters.
		 */

		public RGVertex getCheckpointVertex1() {
			return checkpointVertex1;
		}

		public RGVertex getCheckpointVertex2() {
			return checkpointVertex2;
		}
	}

	/*
	 * The reduced graph representation of the cell-based maze in which the AI
	 * player will be. This is aggregation as the AIPlayer class has a HAS-A
	 * relationship with the ReducedGraph class but the ReducedGraph object will
	 * not be destroyed if the AIPlayer object is destroyed.
	 */
	private final ReducedGraph reducedGraph;

	/*
	 * The RGPath objects (values) representing the shortest paths (consisting
	 * of RGVertex objects) between every pair of RGVertex objects (representing
	 * all the pairs of checkpoint Cell objects in the maze), which are found in
	 * the CheckpointVertexPair objects (keys). This is effectively an adjacency
	 * matrix for a weighted, undirected, complete graph (the checkpoint graph),
	 * where the vertices are all of the checkpoint RGVertex objects in the
	 * reduced graph and the edges are the RGPath objects representing the
	 * shortest paths between them; the weight of an edge (an RGPath object) is
	 * the total weight of all of the RGEdges in the path of RGVertex objects
	 * represented by the RGPath object. The Map interface is used rather than a
	 * concrete class such as HashMap because it separates the actual
	 * implementation of the Map interface from this class's use of the
	 * interface's methods, allowing the implementation to change (say, from
	 * HashMap to Hashtable) in the future. This is composition as the AIPlayer
	 * class has a HAS-A relationship with the CheckpointVertexPair class and
	 * the CheckpointVertexPair objects in the
	 * shortestPathsBetweenCheckpointVertices hash table will be destroyed if
	 * the AIPlayer object is destroyed. This is composition as the AIPlayer
	 * class has a HAS-A relationship with the RGPath class and the RGPath
	 * objects in the shortestPathsBetweenCheckpointVertices hash table will be
	 * destroyed if the AIPlayer object is destroyed.
	 */
	private final Map<CheckpointVertexPair, RGPath> shortestPathsBetweenCheckpointVertices;

	/*
	 * The ordered list of Cell objects representing the path that the AI will
	 * follow through the maze. This is aggregation as the AIPlayer class has a
	 * HAS-A relationship with the Cell class but the Cell objects in the
	 * cellRoute list will not be destroyed if the AIPlayer object is destroyed.
	 */
	private final List<Cell> cellRoute;

	/*
	 * The index in the cellRoute list of the current Cell object where this
	 * AIPlayer object is.
	 */
	private int currentCellIndex;

	/*
	 * Constructor. startCell is the Cell object where the AIPlayer object will
	 * start and endCell is the Cell object where the AIPlayer will need to end
	 * up after visiting all the checkpoint Cell objects.
	 */
	public AIPlayer(Cell startCell, Cell endCell, double baseVel,
			double toleranceConstant, Color color, String name,
			double playerProportionOfCellDimensions, int numCheckpointsToReach,
			ReducedGraph reducedGraph) {
		/*
		 * The superclass's constructor must be called first.
		 */
		super(startCell, endCell, baseVel, toleranceConstant, color, name,
				playerProportionOfCellDimensions, numCheckpointsToReach);
		this.reducedGraph = reducedGraph;

		/*
		 * The shortestPathsBetweenCheckpointVertices table should be made
		 * before generating the final route because these shortest paths are
		 * required for the latter part.
		 */
		shortestPathsBetweenCheckpointVertices = initShortestPathsBetweenCheckpointVertices();
		cellRoute = generateCellRoute(reducedGraph.getVertex(startCell),
				reducedGraph.getVertex(endCell));

		/*
		 * The AIPlayer starts at the first Cell object in the cellRoute list
		 * (which should be startCell).
		 */
		currentCellIndex = 0;
	}

	/*
	 * Computes the RGPath objects representing the shortest paths (consisting
	 * of RGVertex objects) between every pair of RGVertex objects in the
	 * reduced graph that represent checkpoint Cell objects. Puts these RGPath
	 * objects in a hash table as values with the key being the
	 * checkpointVertexPair object consisting of the two checkpoint RGVertex
	 * objects at either end of the RGPath object. Returns this hash table.
	 */
	private Map<CheckpointVertexPair, RGPath> initShortestPathsBetweenCheckpointVertices() {
		/*
		 * All the traversable RGVertex objects in the reduced graph.
		 */
		Collection<RGVertex> vertices = reducedGraph.getVertices();

		/*
		 * All the checkpoint RGVertex objects in the reduced graph between
		 * which shortest paths need to be found
		 */
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();

		/*
		 * This hash table is to store the RGPath objects (values) representing
		 * the shortest paths (consisting of RGVertex objects) between every
		 * pair of RGVertex objects (representing all the pairs of checkpoint
		 * Cell objects in the maze), which are found in the
		 * CheckpointVertexPair objects (keys).
		 */
		Map<CheckpointVertexPair, RGPath> shortestPathsBetweenCheckpointVertices = new HashMap<CheckpointVertexPair, RGPath>();

		/*
		 * This set is to store the checkpoint RGVertex objects which have acted
		 * as source vertices for Dijkstra’s algorithm
		 */
		Set<RGVertex> doneCheckpointVertices = new HashSet<RGVertex>();

		/*
		 * Dijkstra’s algorithm is carried out from each checkpoint RGVertex
		 * object (called sourceVertex here) in checkpointVertices
		 */
		for (RGVertex sourceVertex : checkpointVertices) {

			/*
			 * This hash table is to store the weight of the of the minimum
			 * weight path found, at any given time, from sourceVertex to each
			 * RGVertex object in reducedGraph.
			 */
			Map<RGVertex, Double> weightsFromSourceMap = new HashMap<RGVertex, Double>();

			/*
			 * This hash table is to store the RGVertex that is just before the
			 * key RGVertex object in the minimum weight path found, at any
			 * given time, from sourceVertex to each RGVertex object in
			 * reducedGraph.
			 */
			Map<RGVertex, RGVertex> previousVerticesMap = new HashMap<RGVertex, RGVertex>();

			/*
			 * FibonacciHeap is a class (source:
			 * http://www.keithschwarz.com/interesting
			 * /code/fibonacci-heap/FibonacciHeap.java.html) implementing a
			 * Fibonacci heap, which is a data structure that can be used as a
			 * minimum priority queue. This queue is to store the RGVertex
			 * objects for which shortest paths from sourceVertex have not been
			 * found yet. The priority of RGVertex objects in the queue should
			 * represent the value given by weightsFromSourceMap.get for the
			 * RGVertex object.
			 */
			FibonacciHeap<RGVertex> vertexQueue = new FibonacciHeap<RGVertex>();

			/*
			 * The particular implementation of a Fibonacci heap stores RGVertex
			 * objects with their priorities using a class called Entry. Thus,
			 * in order for us to access the corresponding entry for a RGVertex
			 * object, we need a dictionary data structure to store the RGVertex
			 * objects as keys and the Entry objects as values. A hash table is
			 * used because it has constant look-up time complexity, which makes
			 * it an efficient data structure to use for this purpose.
			 */
			Map<RGVertex, FibonacciHeap.Entry<RGVertex>> queueEntriesMap = new HashMap<RGVertex, FibonacciHeap.Entry<RGVertex>>();

			/*
			 * This set is to store the RGVertex objects that have been visited.
			 */
			Set<RGVertex> visitedVertices = new HashSet<RGVertex>();

			/*
			 * This integer counts how many checkpoint RGVertex objects in
			 * reducedGraph either do not need shortest paths from sourceVertex
			 * calculated again (as it would have been done in a previous
			 * iteration) or have been visited in this Dijkstra’s algorithm.
			 */
			int numVisitedCheckpointVertices = doneCheckpointVertices.size();

			/*
			 * Populates weightsFromSourceMap and vertexQueue.
			 */
			for (RGVertex vertex : vertices) {

				/*
				 * No path has been found yet from each RGVertex object to
				 * sourceVertex, so weights should not exist, but rather than
				 * using null references for the weights, we can simply use a
				 * very large weight (e.g. 1 billion) that will be greater than
				 * the weight of any possible path in reducedGraph. This means
				 * that when checking if alternativeWeight is less than this
				 * weight later on in the algorithm, we don’t need to check
				 * whether the weight is null. Instead, any value for
				 * alternativeWeight would be less than this very large value
				 * and so the weight stored in the entry in weightFromSourceMap
				 * hash table would be updated to alternativeWeight.
				 */
				weightsFromSourceMap.put(vertex, 1000000000d);
				FibonacciHeap.Entry<RGVertex> entry = vertexQueue.enqueue(
						vertex, 1000000000d);
				queueEntriesMap.put(vertex, entry);
			}

			/*
			 * However, the minimum path from sourceVertex to sourceVertex is
			 * clearly 0. Rather than checking every iteration whether the
			 * RGVertex object is sourceVertex in the loop above, we just change
			 * the weight value to 0 here, as it uses fewer operations, making
			 * the algorithm slightly more efficient
			 */
			weightsFromSourceMap.put(sourceVertex, 0.0);
			vertexQueue.decreaseKey(queueEntriesMap.get(sourceVertex), 0.0);

			/*
			 * This is the main loop of Dijkstra’s algorithm. Once vertexQueue
			 * is empty or once numVisitedCheckpointVertices = number of
			 * RGVertex objects in the CheckpointVertices set, shortest paths
			 * will have been found from sourceVertex to all the checkpoint
			 * RGVertex objects in reducedGraph, so we can terminate the loop
			 * below.
			 */
			while (!vertexQueue.isEmpty()
					&& checkpointVertices.size() != numVisitedCheckpointVertices) {

				/*
				 * We select the RGVertex object whose weight of the minimum
				 * weight path found, at this stage, from sourceVertex to the
				 * RGVertex object, is the lowest, because the optimum minimum
				 * weight path to this RGVertex object has been found (and so we
				 * also put it in the visitedVertices set because we do not need
				 * to check it again).
				 */
				FibonacciHeap.Entry<RGVertex> currentVertexEntry = vertexQueue
						.dequeueMin();
				RGVertex currentVertex = currentVertexEntry.getValue();
				visitedVertices.add(currentVertex);

				/*
				 * Checks if currentVertex is a checkpoint RGVertex object and
				 * updates the counting variable numVisitedCheckpointVertices.
				 */
				if (!doneCheckpointVertices.contains(currentVertex)
						&& checkpointVertices.contains(currentVertex)) {
					numVisitedCheckpointVertices++;
				}

				/*
				 * We look at each unvisited adjacent RGVertex object of
				 * currentVertex and check whether the minimum weight path to
				 * currentVertex from sourceVertex (which we know cannot be made
				 * shorter) added to the weight between the unvisited adjacent
				 * RGVertex object and currentVertex (this sum being called
				 * alternativeWeight) is less than the minimum weight path found
				 * so far to that adjacent RGVertex object from sourceVertex.
				 */
				for (RGAdjacency adjacency : currentVertex.getAdjacencies()) {
					RGVertex adjacentVertex = adjacency.getAdjacentVertex();
					if (!visitedVertices.contains(adjacentVertex)) {

						double alternativeWeight = weightsFromSourceMap
								.get(currentVertex)
								+ currentVertex
										.getWeightToAdjacentVertex(adjacentVertex);
						if (alternativeWeight < weightsFromSourceMap
								.get(adjacentVertex)) {

							/*
							 * This means we have found a shorter path to
							 * adjacentVertex from sourceVertex and we know that
							 * it goes through currentVertex, so we update the
							 * relevant hash tables and decrease the priority of
							 * adjacentVertex in vertexQueue.
							 */
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

			/*
			 * Once vertexQueue is empty, all the RGVertex objects in
			 * reducedGraph have been visited so we can be sure that the
			 * shortest paths from sourceVertex to all the RGVertex objects have
			 * been found. We can then update the
			 * shortestPathsBetweenCheckpointVertices to add the paths from
			 * sourceVertex to all of the checkpoint RGVertex object.
			 */
			for (RGVertex checkpointVertex : checkpointVertices) {
				if (checkpointVertex != sourceVertex) {
					CheckpointVertexPair pair = new CheckpointVertexPair(
							sourceVertex, checkpointVertex);
					/*
					 * If the shortest path between the RGVertex objects of pair
					 * has already been found and added to the
					 * shortestPathsBetweenCheckpointVertices hash table
					 * (because Dijkstra’s algorithm has already been applied
					 * using checkpointVertex as the source), then we do not
					 * need to add the shortest path again.
					 */
					if (!shortestPathsBetweenCheckpointVertices
							.containsKey(pair)) {
						RGPath pairPath = new RGPath();
						RGVertex currentVertex = checkpointVertex;

						/*
						 * pairPath is iteratively built up starting from
						 * checkpointVertex. When currentVertex is null, it
						 * means sourceVertex has been added (as it has no
						 * previous RGVertex object) and pairPath has been
						 * built.
						 */
						while (currentVertex != null) {
							pairPath.appendVertex(currentVertex);
							currentVertex = previousVerticesMap
									.get(currentVertex);
						}

						if (!(pair.getCheckpointVertex1() == pairPath
								.getVertex1() && pair.getCheckpointVertex2() == pairPath
								.getVertex2())
								&& !(pair.getCheckpointVertex1() == pairPath
										.getVertex2() && pair
										.getCheckpointVertex2() == pairPath
										.getVertex1())) {
							/*
							 * If the RGVertex objects on either end of pairPath
							 * are not the same as the two RGVertex objects in
							 * pair, then there is a logical error elsewhere in
							 * the code, so a RuntimeException is thrown to quit
							 * the program and make debugging and tracing the
							 * error easier. A try-catch block would not be
							 * useful here as the issue cannot be fixed without
							 * changing the code.
							 */
							throw new RuntimeException();
						}
						shortestPathsBetweenCheckpointVertices.put(pair,
								pairPath);
					}
				}
			}

			/*
			 * We have computed all shortest paths from sourceVertex.
			 */
			doneCheckpointVertices.add(sourceVertex);
		}
		return shortestPathsBetweenCheckpointVertices;
	}

	/*
	 * Returns the weight of the shortest path RGPath object between the two
	 * RGVertex objects in the parameters (which should represent checkpoint
	 * cells) as found in the shortestPathsBetweenCheckpointVertices hash table.
	 */
	private double getShortestPathWeightBetweenCheckpointVertices(RGVertex v1,
			RGVertex v2) {
		return shortestPathsBetweenCheckpointVertices.get(
				new CheckpointVertexPair(v1, v2)).getTotalWeight();
	}

	/*
	 * Calls generateGreedyCheckpointVertexRoute and improves the returned list
	 * using the twoOpt method on it. The final improved list of RGVertex
	 * objects (representing all the checkpoint Cell objects in the maze) is
	 * converted to the full list of Cell objects (such that consecutive Cell
	 * objects are adjacent), representing a route of Cell objects which the AI
	 * player can actually traverse through in the maze. This list will be an
	 * optimal or near-optimal ordered list of Cell objects for the AI player to
	 * visit, which starts at the Cell object represented by startVertex, visits
	 * all the checkpoints and finishes at the Cell object represented by
	 * endVertex.
	 */
	private List<Cell> generateCellRoute(RGVertex startVertex,
			RGVertex endVertex) {

		/*
		 * The stores the ordered list of checkpoint RGVertex objects
		 * representing the order in which the AIPlayer object will visit the
		 * checkpoints of the maze. This route is initially generated using
		 * generateGreedyCheckpointVertexRoute and is improved by using the
		 * twoOpt method.
		 */
		List<RGVertex> checkpointVertexRoute = twoOpt(generateGreedyCheckpointVertexRoute(
				startVertex, endVertex));

		/*
		 * This list is to store the ordered lists of all the Cell objects that
		 * the AIPlayer object will visit and in the order it will visit them
		 * in.
		 */
		List<Cell> cellRoute = new ArrayList<Cell>();

		/*
		 * To convert checkpointVertexRoute into a list of Cell objects, we
		 * iterate over the checkpoint RGVertex objects in
		 * checkpointVertexRoute. For each pair of consecutive checkpoint
		 * RGVertex objects, we get the shortest path (made up of RGVertex
		 * objects) between them. We then iterate over these RGVertex objects
		 * making up the shortest path. For each pair of consecutive RGVertex
		 * objects in the shortest path, we add the Cell objects making up the
		 * RGEdge object between them to cellRoute. We do this in a way that
		 * order is preserved.
		 */

		for (int i = 0; i < checkpointVertexRoute.size() - 1; i++) {

			/*
			 * For each pair of consecutive checkpoint RGVertex objects in
			 * checkpointVertexRoute, we retrieve the list of RGVertex objects
			 * making up the shortest path (i.e. an RGPath object) between them.
			 */
			RGVertex currentCheckpointVertex = checkpointVertexRoute.get(i);
			RGVertex nextCheckpointVertex = checkpointVertexRoute.get(i + 1);
			List<RGVertex> pathVertices = shortestPathsBetweenCheckpointVertices
					.get(new CheckpointVertexPair(currentCheckpointVertex,
							nextCheckpointVertex)).getPathVertices();

			/*
			 * We need to iterate over pathVertices in such a way that we start
			 * at currentCheckpointVertex and end at nextCheckpointVertex. This
			 * will let us add the Cell objects to cellRoute in the correct
			 * order (rather than in reverse order).
			 */

			/*
			 * If currentCheckpointVertex is at the start of pathVertices, then
			 * we need to iterate over pathVertices in the natural order from
			 * the start to the end.
			 */
			int inclusiveStartIndex = 0, exclusiveEndIndex = pathVertices
					.size() - 1, increment = 1;

			/*
			 * If currentCheckpointVertex is at the end of pathVertices, then we
			 * need to iterate over pathVertices in reverse order from the end
			 * to the start.
			 */
			if (pathVertices.get(pathVertices.size() - 1) == currentCheckpointVertex) {
				inclusiveStartIndex = pathVertices.size() - 1;
				exclusiveEndIndex = 0;
				increment = -1;
			} else if (pathVertices.get(0) != currentCheckpointVertex) {
				/*
				 * This shouldn't happen. Thus, if it does, there is a logical
				 * error elsewhere in the program, so a RuntimeException is
				 * thrown to quit the program and make debugging and tracing the
				 * error easier. A try-catch block would not be useful here as
				 * the issue cannot be fixed without changing the code.
				 */
				throw new RuntimeException();
			}

			for (int index = inclusiveStartIndex; index != exclusiveEndIndex; index += increment) {

				/*
				 * For each pair of consecutive RGVertex objects in
				 * pathVertices, we retrieve the list of Cell objects making up
				 * the RGEdge object between them.
				 */
				RGVertex currentVertex = pathVertices.get(index);
				RGVertex nextVertex = pathVertices.get(index + increment);
				List<Cell> edgeCells = currentVertex.getEdgeTo(nextVertex)
						.getCells();

				/*
				 * We need to iterate over edgeCells in such a way that we start
				 * at currentVertex and end at nextVertex. This will let us add
				 * the Cell objects to cellRoute in the correct order (rather
				 * than in reverse order).
				 */

				/*
				 * If currentVertex is at the start of edgeCells, then we need
				 * to iterate over edgeCells in the natural order from the start
				 * to the end.
				 */
				int inclusiveStartIndex2 = 0, exclusiveEndIndex2 = edgeCells
						.size(), increment2 = 1;

				/*
				 * If currentVertex is at the end of edgeCells, then we need to
				 * iterate over edgeCells in reverse order from the end to the
				 * start.
				 */
				if (currentVertex.getSuperCell() == edgeCells.get(edgeCells
						.size() - 1)) {
					inclusiveStartIndex2 = edgeCells.size() - 1;
					exclusiveEndIndex2 = -1;
					increment2 = -1;
				} else if (edgeCells.get(0) != currentVertex.getSuperCell()) {

					/*
					 * This shouldn't happen. Thus, if it does, there is a
					 * logical error elsewhere in the program, so a
					 * RuntimeException is thrown to quit the program and make
					 * debugging and tracing the error easier. A try-catch block
					 * would not be useful here as the issue cannot be fixed
					 * without changing the code.
					 */
					throw new RuntimeException();
				}

				/*
				 * This loop iterates over edgeCells, adding the Cell objects to
				 * cellRoute.
				 */
				for (int index2 = inclusiveStartIndex2; index2 != exclusiveEndIndex2; index2 += increment2) {
					cellRoute.add(edgeCells.get(index2));
				}
			}
		}
		return cellRoute;
	}

	/*
	 * Uses the greedy tour construction heuristic to generate an ordered list
	 * of RGVertex objects, representing the route of super cells that the AI
	 * player should visit, which starts at startVertex, visits all the
	 * checkpoint RGVertex objects, and finishes at endVertex.
	 */
	private List<RGVertex> generateGreedyCheckpointVertexRoute(
			RGVertex startVertex, RGVertex endVertex) {

		/*
		 * If startVertex and endVertex don’t represent checkpoints, then the
		 * algorithm won’t work as the shortestPathsBetweenCheckpointVertices
		 * hash table would not have keys that include the startVertex and
		 * endVertex. This should not happen and must indicate a logical error
		 * elsewhere in the code, and a try-catch block would not be useful as
		 * the issue cannot be fixed without changing the code. Throwing a
		 * runtime exception here would make debugging and tracing the error
		 * easier.
		 */
		if (!startVertex.getSuperCell().isCheckpoint()
				|| !endVertex.getSuperCell().isCheckpoint()) {
			throw new RuntimeException();
		}

		/*
		 * All the traversable checkpoint RGVertex objects in the reduced graph.
		 */
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();

		/*
		 * The list of all the possible RGPath objects that can be part of the
		 * route
		 */
		List<RGPath> paths = new ArrayList<RGPath>(
				shortestPathsBetweenCheckpointVertices.values());

		/*
		 * In order to repeatedly choose the shortest RGPath object (which is
		 * how the greedy algorithm works), we need to sort a list of all the
		 * RGPath objects in ascending order of weight.
		 */
		mergeSort(paths, 0, paths.size());

		/*
		 * This disjoint-set is needed to efficiently check if, in the route at
		 * any given time, a path exists along adjacent RGVertex objects between
		 * two RGVertex objects, when deciding whether to add an RGEdge object
		 * to the route.
		 */
		DisjointSet<RGVertex> checkpointVerticesDisjointSet = new DisjointSet<RGVertex>(
				checkpointVertices);

		/*
		 * This table is to store the (one or two) checkpoint RGVertex objects
		 * (in a list) that are connected (via an RGPath) to each checkpoint
		 * RGVertex object in the route as the route is being built up. This is
		 * effectively a table of adjacency lists for the route (a subgraph of
		 * the checkpoint graph).
		 */
		Map<RGVertex, ArrayList<RGVertex>> routeAdjacencyListMap = new HashMap<RGVertex, ArrayList<RGVertex>>();
		for (RGVertex checkpointVertex : checkpointVertices) {
			routeAdjacencyListMap.put(checkpointVertex,
					new ArrayList<RGVertex>());
		}

		int pathsAdded = 0;
		int i = 0;

		/*
		 * We keep adding RGPath objects until pathsAdded =
		 * checkpointVertices.size() – 3 because at this point, only the two
		 * endpoint vertices would be left to connect to the route.
		 */
		for (i = 0; i < paths.size()
				&& pathsAdded < checkpointVertices.size() - 3; i++) {
			RGPath path = paths.get(i);
			RGVertex checkpointVertex1 = path.getVertex1();
			RGVertex checkpointVertex2 = path.getVertex2();

			/*
			 * The startVertex and endVertex will be connected to the route
			 * afterwards (otherwise incomplete routes would be created) so we
			 * skip RGPath objects that have startVertex or endVertex as their
			 * endpoints.
			 */
			if (checkpointVertex1 != startVertex
					&& checkpointVertex1 != endVertex
					&& checkpointVertex2 != startVertex
					&& checkpointVertex2 != endVertex) {

				/*
				 * Here, “order” refers to the number of checkpoint RGVertex
				 * objects next to checkpointVertex1 and checkpointVertex2 in
				 * the route.
				 */
				int checkpointVertex1Order = routeAdjacencyListMap.get(
						checkpointVertex1).size();
				int checkpointVertex2Order = routeAdjacencyListMap.get(
						checkpointVertex2).size();

				/*
				 * If either of checkpointVertex1Order or checkpointVertex2Order
				 * is equal to 2, then we can’t add Path to the route because
				 * then one of the checkpoint RGVertex objects would have order
				 * 3, which should not happen in a route. If checkpointVertex1
				 * and checkpointVertex2 are joined in
				 * checkpointVerticesDisjointSet, then this indicates that
				 * checkpointVertex1 and checkpointVertex2 are already
				 * indirectly connected, so adding Path to the route would
				 * create a cycle, which should not happen either.
				 */
				if (checkpointVertex1Order != 2
						&& checkpointVertex2Order != 2
						&& !checkpointVerticesDisjointSet.areJoined(
								checkpointVertex1, checkpointVertex2)) {
					/*
					 * Then we can add Path to the route. We do this by making
					 * checkpointVertex2 and checkpointVertex1 adjacent to each
					 * other in the route subgraph.
					 */
					routeAdjacencyListMap.get(checkpointVertex1).add(
							checkpointVertex2);
					routeAdjacencyListMap.get(checkpointVertex2).add(
							checkpointVertex1);
					pathsAdded++;

					/*
					 * checkpointVertex1 and checkpointVertex2 are now connected
					 * in the route by Path so these checkpoint RGVertex objects
					 * should be in the same subset in
					 */
					checkpointVerticesDisjointSet.join(checkpointVertex1,
							checkpointVertex2);

				}
			}
		}

		RGVertex orderOneCheckpointVertex1 = null, orderOneCheckpointVertex2 = null;
		/*
		 * This loop tries to find the two checkpoint RGVertex objects at the
		 * ends of the route built so far (as these are the only RGVertex
		 * objects with order 1 and so startVertex and endVertex can only be
		 * connected to these)
		 */
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

		/*
		 * There are only two ways to connect the endpoint vertices (i.e.
		 * startVertex and endVertex) to these two order-1 checkpoint RGVertex
		 * objects in the route. This If statement compares both ways and
		 * selects the one which results in a shorter route.
		 */
		if (getShortestPathWeightBetweenCheckpointVertices(
				orderOneCheckpointVertex1, startVertex)
				+ getShortestPathWeightBetweenCheckpointVertices(
						orderOneCheckpointVertex2, endVertex) < getShortestPathWeightBetweenCheckpointVertices(
				orderOneCheckpointVertex2, startVertex)
				+ getShortestPathWeightBetweenCheckpointVertices(
						orderOneCheckpointVertex1, endVertex)) {
			/*
			 * Then connect startVertex to orderOneCheckpointVertex1 and connect
			 * endVertex to orderOneCheckpointVertex2.
			 */
			routeAdjacencyListMap.get(orderOneCheckpointVertex1).add(
					startVertex);
			routeAdjacencyListMap.get(startVertex).add(
					orderOneCheckpointVertex1);
			routeAdjacencyListMap.get(orderOneCheckpointVertex2).add(endVertex);
			routeAdjacencyListMap.get(endVertex).add(orderOneCheckpointVertex2);
		} else {
			/*
			 * Then connect startVertex to orderOneCheckpointVertex2 and connect
			 * endVertex to orderOneCheckpointVertex1.
			 */
			routeAdjacencyListMap.get(orderOneCheckpointVertex2).add(
					startVertex);
			routeAdjacencyListMap.get(startVertex).add(
					orderOneCheckpointVertex2);
			routeAdjacencyListMap.get(orderOneCheckpointVertex1).add(endVertex);
			routeAdjacencyListMap.get(endVertex).add(orderOneCheckpointVertex1);
		}

		/*
		 * This list is to store the RGVertex objects that make up the route
		 * that the AIPlayer object will follow in order.
		 */
		List<RGVertex> checkpointVertexRoute = new ArrayList<RGVertex>();

		/*
		 * To generate an ordered list of checkpoint RGVertex objects (i.e. in
		 * the order that they should be visited by the AI) from this hash table
		 * structure, we start off with startVertex and add it to the route list
		 * (which is going to build up to be this ordered list).
		 */

		checkpointVertexRoute.add(startVertex);
		RGVertex previousVertex = null;
		RGVertex currentVertex = startVertex;

		/*
		 * We can then retrieve the adjacency list for it from the
		 * routeAdjacencyListMap hash table. This adjacency list would only have
		 * one other checkpoint RGVertex object in it because startVertex will
		 * only be connected to one other checkpoint RGVertex object in the
		 * route (as it is an endpoint of the route). We add this sole adjacent
		 * checkpoint RGVertex object to the checkpointVertexRoute list. Then we
		 * retrieve the adjacency list for this RGVertex object from the
		 * routeAdjacencyListMap hash table. This adjacency list would have two
		 * other checkpoint RGVertex objects in it, but one of them has already
		 * been added to the checkpointVertexRoute list, so we add the other
		 * checkpoint RGVertex object to the checkpointVertexRoute list and then
		 * repeat from that checkpoint RGVertex object, and so on, until all the
		 * checkpoint RGVertex objects have been added to the
		 * checkpointVertexRoute list. In effect, we are traversing along the
		 * greedy checkpoint route subgraph.
		 */
		while (checkpointVertexRoute.size() < checkpointVertices.size()) {
			ArrayList<RGVertex> adjacencyList = routeAdjacencyListMap
					.get(currentVertex);
			for (RGVertex nextVertex : adjacencyList) {
				if (nextVertex != previousVertex) {
					checkpointVertexRoute.add(nextVertex);
					previousVertex = currentVertex;
					currentVertex = nextVertex;
					break;
				}
			}
		}

		return checkpointVertexRoute;
	}

	/*
	 * Sorts the elements in paths between the indices startIndex and endIndex
	 * into ascending order of path weight using the merge sort algorithm.
	 */
	private void mergeSort(List<RGPath> paths, int startIndex, int endIndex) {
		/*
		 * Paths is the main list of RGPath objects to be sorted. startIndex is
		 * the index of the first RGPath object in the sublist to be sorted.
		 * endIndex is the index of the last RGPath object in the sublist.
		 */

		/*
		 * If startIndex is equal to endIndex – 1, then that means the sublist
		 * to be sorted is of length 1 and a list of length 1 is already sorted,
		 * and so merge sort does not need to be used to sort it.
		 */
		if (startIndex < endIndex - 1) {
			/*
			 * midIndex is the middle index of the sublist. It is used to split
			 * the sublist in half into two smaller sublists.
			 */
			int midIndex = (startIndex + endIndex) / 2;

			/*
			 * Recursively sorts the first half of the sublist.
			 */
			mergeSort(paths, startIndex, midIndex);

			/*
			 * Recursively sorts the second half of the sublist.
			 */
			mergeSort(paths, midIndex, endIndex);

			/*
			 * Combines the two sorted halves of the sublist together to form a
			 * sorted sublist.
			 */
			merge(paths, startIndex, midIndex, endIndex);
		}
	}

	/*
	 * Used in the mergeSort method to systematically combine the two sorted
	 * halves of a sublist to form a fully sorted sublist.
	 */
	private void merge(List<RGPath> paths, int startIndex, int midIndex,
			int endIndex) {
		/*
		 * These lists are created to temporarily store the two store halves of
		 * the sublist
		 */
		List<RGPath> leftHalf = new ArrayList<RGPath>();
		List<RGPath> rightHalf = new ArrayList<RGPath>();
		for (int i = startIndex; i < midIndex; i++) {
			leftHalf.add(paths.get(i));
		}
		for (int i = midIndex; i < endIndex; i++) {
			rightHalf.add(paths.get(i));
		}

		/*
		 * leftIndex is used to keep track of the first index of the first half
		 * of the sublist whose RGPath object has not been inserted into the
		 * main list yet. rightIndex is used for the same purpose for the second
		 * half.
		 */
		int leftIndex = 0;
		int rightIndex = 0;

		/*
		 * We iterate over paths from startIndex to endIndex (the indices
		 * bounding the sublist), adding RGPath objects from the two halves of
		 * the sublist. insertIndex is used to keep track of the next index of
		 * the main list into which the RGPath object with the smallest weight
		 * from the two halves of the sublist will be inserted.
		 */
		for (int insertIndex = startIndex; insertIndex < endIndex; insertIndex++) {

			/*
			 * The selection structure below checks which half of the sublist
			 * (out of leftHalf and rightHalf) has the smallest weight RGPath
			 * object at the front (because the sublists are sorted, only the
			 * front has to be checked) and then inserts it into the next index
			 * of the main list. If leftIndex has exceeded the length of
			 * leftHalf list, then all the elements of the leftHalf list have
			 * been inserted into the main list, and so we can insert the rest
			 * of the rightHalf list into the main list without having to
			 * compare anything. The same is true for when rightIndex exceeds
			 * the rightHalf list.
			 */
			if (leftIndex < leftHalf.size()
					&& (rightIndex >= rightHalf.size() || leftHalf.get(
							leftIndex).getTotalWeight() < rightHalf.get(
							rightIndex).getTotalWeight())) {
				paths.set(insertIndex, leftHalf.get(leftIndex));
				leftIndex++;
			} else {
				paths.set(insertIndex, rightHalf.get(rightIndex));
				rightIndex++;
			}
		}
	}

	/*
	 * Returns the result of performing the 2-opt tour improvement heuristic on
	 * a given ordered list of RGVertex objects, representing the route of super
	 * Cell objects that the AI player should visit, which starts at
	 * startVertex, visits all the All the checkpoint RGVertex objects, and
	 * finishes at endVertex.
	 */
	private List<RGVertex> twoOpt(List<RGVertex> checkpointVertexRoute) {
		/*
		 * This list stores the route of checkpoint RGVertex objects as it is
		 * being improved. A copy of checkpointVertexRoute is created so that it
		 * does not get directly altered.
		 */
		List<RGVertex> improvedCheckpointVertexRoute = new ArrayList<RGVertex>(
				checkpointVertexRoute);

		double minChange;

		/*
		 * In every iteration of the following loop, we select the two pairs of
		 * adjacent checkpoint RGVertex objects (where adjacent means next to
		 * each other in the improvedCheckpointVertexRoute list) such that
		 * performing a 2-opt swap on them decreases the total weight of the
		 * route the most. We then perform the 2-opt swap and repeat until no
		 * 2-opt swap can be made that decreases the total weight of the route.
		 */
		do {
			minChange = 0;
			int minIndex1 = -1;
			int minIndex2 = -1;

			/*
			 * To select the two pairs of adjacent checkpoint RGVertex objects
			 * to perform a 2-opt swap on, only two checkpoint RGVertex objects
			 * need to be selected. The following nested For loop is used to
			 * find two different checkpoint RGVertex objects (checkpointVertexB
			 * and checkpointVertexC) in the improvedCheckpointVertexRoute list
			 * (that are not the endpoint checkpoint RGVertex objects, which are
			 * at the first and last index of the list). The other two
			 * checkpoint RGVertex objects (checkpointVertexA and
			 * checkpointVertexD are found from subtracting 1 from the index of
			 * checkpointVertexB and adding 1 to the index of checkpointVertexC
			 * respectively. At any point during the iterations, the indices of
			 * the two different checkpoint RGVertex objects (checkpointVertexB
			 * and checkpointVertexC) for which a 2-opt swap gives the least
			 * weight is stored in the minIndex1 and minIndex2 variables. At the
			 * end of the loop, if any 2-opt swap would reduce the weight of the
			 * path, a 2-opt swap is performed for these two indices using the
			 * twoOptSwap method. The lower bound for index1 and index2 is 1 and
			 * the upper bound is the number of elements in
			 * checkpointVertexRoute - 2 because we do not want to change the
			 * position of the start and end RGVertex objects.
			 */
			for (int index1 = 1; index1 < checkpointVertexRoute.size() - 2; index1++) {
				for (int index2 = index1 + 1; index2 < checkpointVertexRoute
						.size() - 1; index2++) {
					RGVertex checkpointVertexA = improvedCheckpointVertexRoute
							.get(index1 - 1);
					RGVertex checkpointVertexB = improvedCheckpointVertexRoute
							.get(index1);
					RGVertex checkpointVertexC = improvedCheckpointVertexRoute
							.get(index2);
					RGVertex checkpointVertexD = improvedCheckpointVertexRoute
							.get(index2 + 1);
					double previousWeight = getShortestPathWeightBetweenCheckpointVertices(
							checkpointVertexA, checkpointVertexB)
							+ getShortestPathWeightBetweenCheckpointVertices(
									checkpointVertexC, checkpointVertexD);
					double afterWeight = getShortestPathWeightBetweenCheckpointVertices(
							checkpointVertexA, checkpointVertexC)
							+ getShortestPathWeightBetweenCheckpointVertices(
									checkpointVertexB, checkpointVertexD);
					double change = afterWeight - previousWeight;
					if (change < minChange) {
						minChange = change;
						minIndex1 = index1;
						minIndex2 = index2;
					}
				}
			}

			/*
			 * Performs a 2-opt swap if there are two pairs of adjacent
			 * checkpoint RGVertex objects for which a 2-opt swap would decrease
			 * the total weight of the route. If minChange is 0, then no such
			 * pairs exist so we terminate the loop.
			 */
			if (minChange != 0) {
				twoOptSwap(improvedCheckpointVertexRoute, minIndex1, minIndex2);
			}
		} while (minChange != 0);
		return improvedCheckpointVertexRoute;
	}

	/*
	 * Used in the twoOpt method to perform a 2-opt swap on
	 * checkpointVertexRoute. The indices in the parameters represent the
	 * position of the four checkpoint RGVertex objects in the list (the two
	 * checkpoint RGVertex objects at the indices startIndex and endIndex, the
	 * RGVertex object at index startIndex-1, and the RGVertex object at index
	 * endIndex+1) that are involved in the 2-opt swap.
	 */
	private void twoOptSwap(List<RGVertex> checkpointVertexRoute,
			int startIndex, int endIndex) {

		/*
		 * Performs a 2-opt swap by reversing the order of the
		 * checkpointVertexRoute list between indices startIndex and endIndex
		 * inclusive by swapping the elements at indices startIndex and
		 * endIndex, the elements at startIndex + 1 and endIndex – 1, and so on
		 * until all the required elements have been swapped.
		 */
		for (int i = 0; i < (endIndex - startIndex + 1) / 2; i++) {
			Collections.swap(checkpointVertexRoute, startIndex + i, endIndex
					- i);
		}
	}

	/*
	 * Inherited and overridden from the Player class (polymorphism). It is
	 * called every game cycle. It uses the cellRoute list and and the
	 * currentCellIndex variable to determine the next cell to which the AI
	 * player must move and then attempts to change direction if needed. Updates
	 * currentCellIndex once it moves to the next cell.
	 */
	@Override
	public void update() {
		Cell nextRouteCell = null;

		/*
		 * Once currentCellIndex = cellRoute.size() - 1, the AIPlayer object has
		 * reached the final Cell object so no more changes of directions are
		 * needed.
		 */
		if (currentCellIndex < cellRoute.size() - 1) {
			Cell currentRouteCell = cellRoute.get(currentCellIndex);
			nextRouteCell = cellRoute.get(currentCellIndex + 1);

			/*
			 * If currentRouteCell = nextRouteCell, then that means calling
			 * getDirectionToNeighbouringCell will result in an error (as a Cell
			 * object does not neighbour itself).
			 */
			if (currentRouteCell != nextRouteCell) {

				/*
				 * The target direction required to move from currentCell to
				 * nextRouteCell is retrieved and the inherited changeDirection
				 * method is called to change the AIPlayer object's direction to
				 * this target direction if required.
				 */
				Direction targetDirection = currentRouteCell
						.getDirectionToNeighbouringCell(nextRouteCell);
				if (targetDirection != currentDirection) {
					changeDirection(targetDirection);
				}
			}
		}
		/*
		 * Calls the superclass method (which handles the moving and queuing of
		 * directions).
		 */
		super.update();

		/*
		 * Updates currentCellIndex once the AIPlayer has successfully moved to
		 * the next Cell object in the route.
		 */
		if (currentCell == nextRouteCell) {
			currentCellIndex++;
		}
	}

	/*
	 * Tests.
	 */

	private List<RGVertex> generateOptimalCheckpointVertexRoute(
			RGVertex startVertex, RGVertex endVertex) {

		/*
		 * The list of all the checkpoint RGVertex objects in the reduced graph.
		 */
		List<RGVertex> checkpointVertices = new ArrayList<RGVertex>(
				reducedGraph.getCheckpointVertices());

		/*
		 * Remove the start and end RGVertex objects as these should not change
		 * position in the permutations.
		 */
		checkpointVertices.remove(startVertex);
		checkpointVertices.remove(endVertex);

		/*
		 * Iterate over all the possible permutations of the remaining
		 * checkpoint RGVertex objects and find the one resulting in a route
		 * with the minimum total weight.
		 */
		double minWeight = Double.MAX_VALUE;
		List<RGVertex> minPerm = null;
		for (List<RGVertex> perm : Collections2
				.permutations(checkpointVertices)) {
			/*
			 * The weight is calculated taking into account the startVertex and
			 * endVertex.
			 */
			double weight = calculateTotalCheckpointVertexRouteWeight(perm,
					startVertex, endVertex);
			if (weight < minWeight) {
				minPerm = perm;
				minWeight = weight;
			}
		}

		/*
		 * The optimal checkpoint route list is returned.
		 */
		List<RGVertex> route = new ArrayList<RGVertex>();
		route.add(startVertex);
		route.addAll(minPerm);
		route.add(endVertex);
		return route;
	}

	private double calculatePercentageDifferenceBetweenTwoOptAndOptimal(
			RGVertex startVertex, RGVertex endVertex) {
		/*
		 * The greedy checkpoint route list.
		 */
		List<RGVertex> greedyCheckpointVertexRoute = generateGreedyCheckpointVertexRoute(
				startVertex, endVertex);

		/*
		 * The checkpoint route list after the twoOpt method has been applied to
		 * the above route.
		 */
		List<RGVertex> twoOptCheckpointVertexRoute = twoOpt(greedyCheckpointVertexRoute);

		/*
		 * The best possible checkpoint route computed using brute force.
		 */
		List<RGVertex> optimalCheckpointVertexRoute = generateOptimalCheckpointVertexRoute(
				startVertex, endVertex);

		/*
		 * The weight of the checkpoint route generated using the greedy
		 * algorithm and 2-opt algorithm.
		 */
		double twoOptWeight = calculateTotalCheckpointVertexRouteWeight(twoOptCheckpointVertexRoute);

		/*
		 * The weight of the optimal checkpoint route.
		 */
		double optimalWeight = calculateTotalCheckpointVertexRouteWeight(optimalCheckpointVertexRoute);

		/*
		 * The percentage by which twoOptWeight is less than optimalWeight is
		 * returned.
		 */
		return 100 * (twoOptWeight - optimalWeight) / optimalWeight;
	}

	public double calculatePercentageDropInWeightAfterTwoOpt(
			RGVertex startVertex, RGVertex endVertex) {
		/*
		 * The greedy checkpoint route list.
		 */
		List<RGVertex> greedyCheckpointVertexRoute = generateGreedyCheckpointVertexRoute(
				startVertex, endVertex);

		/*
		 * The checkpoint route list after the twoOpt method has been applied to
		 * the above route.
		 */
		List<RGVertex> twoOptCheckpointVertexRoute = twoOpt(greedyCheckpointVertexRoute);

		/*
		 * The weight of the greedy checkpoint route list before the twoOpt
		 * method was applied.
		 */
		double beforeWeight = calculateTotalCheckpointVertexRouteWeight(greedyCheckpointVertexRoute);

		/*
		 * The weight of the greedy checkpoint route list after the twoOpt
		 * method was applied.
		 */
		double afterWeight = calculateTotalCheckpointVertexRouteWeight(twoOptCheckpointVertexRoute);

		/*
		 * The percentage by which afterWeight is less than beforeWeight is
		 * returned. This will be negative if afterWeight is greater than
		 * beforeWeight (i.e. if the twoOpt method makes the route longer).
		 */
		return 100 * (beforeWeight - afterWeight) / beforeWeight;
	}

	public boolean isMergeSortFunctional() {

		/*
		 * This list contains all the RGPath objects that need to be sorted in
		 * the generateGreedyCheckpointVertexRoute method.
		 */
		List<RGPath> paths = new ArrayList<RGPath>(
				shortestPathsBetweenCheckpointVertices.values());

		mergeSort(paths, 0, paths.size());

		/*
		 * Iterate over the paths list, checking if it is sorted in ascending
		 * order of weight after the mergeSort method has been applied.
		 */
		for (int i = 0; i < paths.size() - 1; i++) {

			/*
			 * If the next RGPath object in the list has a lower weight than the
			 * current one, then the list is not sorted in ascending order of
			 * weight, so return false.
			 */
			if (paths.get(i).getTotalWeight() > paths.get(i + 1)
					.getTotalWeight()) {
				return false;
			}
		}

		/*
		 * If the program reaches here, then the list is in ascending order of
		 * weight, so return true.
		 */
		return true;
	}

	private List<RGVertex> generateRandomCheckpointVertexRoute(
			RGVertex startVertex, RGVertex endVertex) {
		/*
		 * If startVertex and endVertex don’t represent checkpoints, then the
		 * algorithm won’t work as the ShortestPathsBetweenCheckpointVertices
		 * hash table would not have keys that include the StartVertex and
		 * EndVertex. This should not happen and must indicate a logical error
		 * elsewhere in the code, and a try-catch block would not be useful as
		 * the issue cannot be fixed without changing the code. Throwing a
		 * runtime exception here would make debugging and tracing the error
		 * easier.
		 */
		if (!startVertex.getSuperCell().isCheckpoint()
				|| !endVertex.getSuperCell().isCheckpoint()) {
			throw new RuntimeException();
		}

		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();
		List<RGVertex> checkpointVertexRoute = new ArrayList<RGVertex>(
				checkpointVertices);

		/*
		 * Randomly shuffle the checkpointVertexRoute list to generate a
		 * randomly ordered checkpoint route.
		 */
		Collections.shuffle(checkpointVertexRoute,
				Application.randomNumberGenerator);

		/*
		 * Ensure that startVertex is the first element of the
		 * checkpointVertexRoute list and that endVertex is the last element.
		 */
		Collections.swap(checkpointVertexRoute, 0,
				checkpointVertexRoute.indexOf(startVertex));
		Collections.swap(checkpointVertexRoute,
				checkpointVertexRoute.size() - 1,
				checkpointVertexRoute.indexOf(endVertex));

		return checkpointVertexRoute;
	}

	private double calculateTotalCheckpointVertexRouteWeight(
			List<RGVertex> checkpointVertexRouteExcludingEndpoints,
			RGVertex startVertex, RGVertex endVertex) {
		double totalWeight = 0;

		/*
		 * Iterates over the checkpoint RGVertex objects in the
		 * checkpointVertexRoute list, adding the shortest path weight between
		 * each pair of consecutive checkpoint RGVertex objects in the list to
		 * the totalWeight variable.
		 */

		for (int i = 0; i < checkpointVertexRouteExcludingEndpoints.size() - 1; i++) {
			RGVertex currentVertex = checkpointVertexRouteExcludingEndpoints
					.get(i);
			RGVertex nextVertex = checkpointVertexRouteExcludingEndpoints
					.get(i + 1);
			totalWeight += getShortestPathWeightBetweenCheckpointVertices(
					currentVertex, nextVertex);
		}

		/*
		 * Adds the shortest path weight between startVertex and the second
		 * RGVertex object in the checkpoint route to totalWeight. Also adds the
		 * shortest path weight between endVertex and the second-last RGVertex
		 * object in the checkpoint route to totalWeight
		 */
		return totalWeight
				+ getShortestPathWeightBetweenCheckpointVertices(
						checkpointVertexRouteExcludingEndpoints.get(0),
						startVertex)
				+ getShortestPathWeightBetweenCheckpointVertices(
						checkpointVertexRouteExcludingEndpoints.get(checkpointVertexRouteExcludingEndpoints
								.size() - 1), endVertex);
	}

	private double calculateTotalCheckpointVertexRouteWeight(
			List<RGVertex> checkpointVertexRoute) {
		double totalWeight = 0;

		/*
		 * Iterates over the checkpoint RGVertex objects in the
		 * checkpointVertexRoute list, adding the shortest path weight between
		 * each pair of consecutive checkpoint RGVertex objects in the list to
		 * the totalWeight variable.
		 */
		for (int i = 0; i < checkpointVertexRoute.size() - 1; i++) {
			RGVertex currentVertex = checkpointVertexRoute.get(i);
			RGVertex nextVertex = checkpointVertexRoute.get(i + 1);
			totalWeight += getShortestPathWeightBetweenCheckpointVertices(
					currentVertex, nextVertex);
		}
		return totalWeight;
	}

	public double calculatePercentageDifferenceBetweenRandomWeightAndGreedyWeight(
			RGVertex startVertex, RGVertex endVertex) {

		/*
		 * 100 randomly generated routes is a large enough sample size for the
		 * test results to be reliable and valid.
		 */
		int numRandomRoutes = 100;

		double sumOfTotalWeights = 0;
		/*
		 * This for loop generates a number (equal to numRandomRoutes) of
		 * randomly generated checkpoint routes and finds the sum of all of the
		 * routes' total weights.
		 */
		for (int i = 0; i < numRandomRoutes; i++) {
			sumOfTotalWeights += calculateTotalCheckpointVertexRouteWeight(generateRandomCheckpointVertexRoute(
					startVertex, endVertex));
		}

		/*
		 * This sum is divided by the number of randomly generated checkpoint
		 * routes to find the total weight of a randomly generated checkpoint
		 * route on average.
		 */
		double averageRandomWeight = sumOfTotalWeights / numRandomRoutes;

		/*
		 * This is the weight of the checkpoint route generated using the greedy
		 * algorithm.
		 */
		double greedyWeight = calculateTotalCheckpointVertexRouteWeight(generateGreedyCheckpointVertexRoute(
				startVertex, endVertex));

		/*
		 * The percentage by which greedyWeight is less than averageRandomWeight
		 * is returned. This will be negative if greedyWeight is greater than
		 * averageRandomWeight.
		 */
		return 100 * (averageRandomWeight - greedyWeight) / averageRandomWeight;
	}

	public boolean isGreedyCheckpointVertexRouteValid(RGVertex startVertex,
			RGVertex endVertex) {
		/*
		 * The list returned by the generateGreedyCheckpointVertexRoute method.
		 */
		List<RGVertex> greedyCheckpointVertexRoute = generateGreedyCheckpointVertexRoute(
				startVertex, endVertex);

		/*
		 * The list containing all of the checkpoint RGVertex objects in the
		 * reduced graph.
		 */
		List<RGVertex> checkpointVertices = reducedGraph
				.getCheckpointVertices();

		/*
		 * If the first and last RGVertex objects of the checkpoint route are
		 * not startVertex and endVertex respectively, then the checkpoint route
		 * is invalid.
		 */
		if (!(greedyCheckpointVertexRoute.get(0) == startVertex && greedyCheckpointVertexRoute
				.get(greedyCheckpointVertexRoute.size() - 1) == endVertex)) {
			return false;
		}

		/*
		 * This set is to store all the RGVertex objects found in the
		 * greedyCheckpointVertexRoute list as we iterate over it.
		 */
		Set<RGVertex> checkpointVerticesInGreedyCheckpointVertexRoute = new HashSet<RGVertex>();
		for (RGVertex rgVertex : greedyCheckpointVertexRoute) {
			/*
			 * If rgVertex is not in the checkpointVertices list, then
			 * greedyCheckpointVertexRoute contains RGVertex objects that are
			 * not checkpoint RGVertex objects, so it is an invalid checkpoint
			 * route.
			 */
			if (!checkpointVertices.contains(rgVertex)) {
				return false;
			}
			/*
			 * If we have already come across rgVertex (i.e. if it is already in
			 * the checkpointVerticesInGreedyCheckpointVertexRoute set), then
			 * the checkpoint route is invalid as it has duplicate checkpoint
			 * RGVertex objects.
			 */
			if (checkpointVerticesInGreedyCheckpointVertexRoute
					.contains(rgVertex)) {
				return false;
			}

			/*
			 * We add RGVertex objects that we come across to this set.
			 */
			checkpointVerticesInGreedyCheckpointVertexRoute.add(rgVertex);
		}

		/*
		 * After this loop, checkpointVerticesInGreedyCheckpointVertexRoute will
		 * only contain RGVertex objects that are in both the checkpointVertices
		 * list and the greedyCheckpointVertexRoute list, with no duplicates. If
		 * the size of this set is equal to the size of the checkpointVertices
		 * list, then that means that the checkpoint route contains all of the
		 * checkpoint RGVertex objects in the reduced graph and contains each of
		 * them only once and so the checkpoint route is valid. Otherwise, it is
		 * false.
		 */
		return checkpointVertices.size() == checkpointVerticesInGreedyCheckpointVertexRoute
				.size();
	}

	private void printShortestPaths() {
		/*
		 * The index of each RGVertex object in the rgVertices list will be its
		 * unique ID.
		 */
		List<RGVertex> rgVertices = new ArrayList<RGVertex>(
				reducedGraph.getVertices());

		/*
		 * Iterate over all the shortest paths in the
		 * shortestPathsBetweenCheckpointVertices hash table and print them in
		 * the appropriate format.
		 */
		for (Entry<CheckpointVertexPair, RGPath> entry : shortestPathsBetweenCheckpointVertices
				.entrySet()) {
			StringBuilder shortestPathStringBuilder = new StringBuilder();

			/*
			 * Append the IDs of each of the pair of checkpoint RGVertex objects
			 * to the string.
			 */
			shortestPathStringBuilder.append("(");
			shortestPathStringBuilder.append(rgVertices.indexOf(entry.getKey()
					.getCheckpointVertex1()));
			shortestPathStringBuilder.append(",");
			shortestPathStringBuilder.append(rgVertices.indexOf(entry.getKey()
					.getCheckpointVertex2()));
			shortestPathStringBuilder.append("): ");

			/*
			 * Iterate over the RGVertex objects in the RGPath object
			 * representing the shortest path between the pair of checkpoint
			 * RGVertex objects and append their IDs to the string.
			 */
			for (RGVertex pathVertex : entry.getValue().getPathVertices()) {
				shortestPathStringBuilder
						.append(rgVertices.indexOf(pathVertex));
				shortestPathStringBuilder.append(", ");
			}

			/*
			 * Output the shortest path string.
			 */
			System.out.println(shortestPathStringBuilder.toString());
		}
	}

	private void renderReducedGraphLabels(Graphics graphics) {
		/*
		 * Only render the labels after the AIPlayer is on its 10th Cell object
		 * to give time for me to screenshot the unlabelled maze (for me to
		 * manually draw the reduced graph without being influenced by the
		 * labels).
		 */
		if (currentCellIndex >= 10) {
			/*
			 * The index of each RGVertex object in the rgVertices list will be
			 * its unique ID.
			 */
			List<RGVertex> rgVertices = new ArrayList<RGVertex>(
					reducedGraph.getVertices());
			for (int i = 0; i < rgVertices.size(); i++) {
				RGVertex rgVertex = rgVertices.get(i);
				Cell cell = rgVertex.getSuperCell();
				int thisRGVertexIndex = i;

				/*
				 * This label will contain this RGVertex object's unique ID,
				 * followed by the IDs of all its adjacent RGVertex objects.
				 */
				StringBuilder rgVertexLabelBuilder = new StringBuilder();
				rgVertexLabelBuilder.append(thisRGVertexIndex);
				rgVertexLabelBuilder.append(": ");

				/*
				 * This loop iterates over this RGVertex object's adjacent
				 * RGVertex objects and append their IDs to the label, while
				 * also drawing labels for the Cell objects in the RGEdge object
				 * connecting the pair of adjacent RGVertex objects.
				 */
				for (RGAdjacency rgAdjacency : rgVertex.getAdjacencies()) {
					RGVertex adjRGVertex = rgAdjacency.getAdjacentVertex();
					int adjRGVertexIndex = rgVertices.indexOf(adjRGVertex);
					rgVertexLabelBuilder.append(adjRGVertexIndex);
					rgVertexLabelBuilder.append(", ");
					RGEdge edge = rgVertex.getEdgeTo(adjRGVertex);

					/*
					 * This is the label for the Cell object in the RGEdge
					 * object connecting the two RGVertex objects. The smaller
					 * ID is always first and the larger ID is always last (for
					 * consistency).
					 */
					String edgeCellLabel = "("
							+ Math.min(thisRGVertexIndex, adjRGVertexIndex)
							+ ", "
							+ Math.max(thisRGVertexIndex, adjRGVertexIndex)
							+ ")";

					/*
					 * Draw edgeCellLabel over all the Cell objects in the
					 * RGEdge object apart from the first and last Cell objects
					 * (as these are the super Cell objects represented by the
					 * RGVertex object and so will already be labelled).
					 */
					List<Cell> rgEdgeCells = edge.getCells();
					for (int j = 1; j < rgEdgeCells.size() - 1; j++) {
						Cell rgEdgeCell = rgEdgeCells.get(j);
						graphics.drawString(edgeCellLabel,
								(int) (rgEdgeCell.x + rgEdgeCell.width / 6),
								(int) (rgEdgeCell.y + rgEdgeCell.height / 2));

					}
				}

				/*
				 * Draw the label for the RGVertex object.
				 */
				graphics.drawString(rgVertexLabelBuilder.toString(),
						(int) (cell.x + cell.width / 6),
						(int) (cell.y + cell.height / 2));
			}
		}
	}

	private void printCellRoute() {
		for (Cell cell : cellRoute) {
			System.out.print(cell.testID + ", ");
		}
	}

	@Override
	public void render(Graphics graphics) {
		super.render(graphics);
	}

}
