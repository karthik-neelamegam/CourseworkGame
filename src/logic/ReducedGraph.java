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

public class ReducedGraph {

	public class Vertex {
		// no need for constant time check if a vertex is adjacent in dijksta's
		// so don't need hashmap/hashset
		private HashMap<Vertex, Edge> adjacentVertices;
		private Cell cell;

		public Vertex(Cell cell) {
			this.cell = cell;
			adjacentVertices = new HashMap<Vertex, Edge>();
		}

		public void addAdjacentVertex(Vertex other, Edge edge) {
			if (other != this) {
				adjacentVertices.put(other, edge);
				other.adjacentVertices.put(this, edge);
			}
		}

		public Cell getCell() {
			return cell;
		}

		public Set<Vertex> getAdjacentVertices() {
			return Collections.unmodifiableSet(adjacentVertices.keySet());
		}

		// reminder: make everything distance or weight (consistent)
		// throwing rather than catching so that program stops and you know something
		// has gone wrong (it's never meant to go wrong)
		public double getDistanceToAdjacentVertex(Vertex vertex) {
			return adjacentVertices.get(vertex).getTotalWeight();
		}

		public Edge getEdgeTo(Vertex vertex) {
			return adjacentVertices.get(vertex);
		}

		public int getOrder() {
			return adjacentVertices.size();
		}
		
		public boolean isAdjacentTo(Vertex vertex) {
			return adjacentVertices.containsKey(vertex);
		}
	}

	public class Edge {
		private List<Cell> edgeCells;
		private double totalWeight;

		public Edge() {
			edgeCells = new ArrayList<Cell>();
			totalWeight = 0;
		}

		public double getTotalWeight() {
			return totalWeight;
		}

		public void appendCell(Cell cell) {
			edgeCells.add(cell);
			if (edgeCells.size() > 0) {
				totalWeight += edgeCells.get(edgeCells.size() - 1).getWeightedDistanceToAdjacentCell(cell);
			}
		}

		public List<Cell> getCells() {
			return Collections.unmodifiableList(edgeCells);
		}
	}

	
	private Map<Cell, Vertex> cellsToVerticesMap;
	private Set<Vertex> vertices; //constant lookup, map.containsValue is O(n)
	private Set<Vertex> checkpointVertices;
	
	public ReducedGraph(Cell cell) {
		cellsToVerticesMap = new HashMap<Cell, Vertex>();
		vertices = new HashSet<Vertex>();
		checkpointVertices = new HashSet<Vertex>();
		System.out.println("Starting to reduce graph");
		reduceGraph(new HashSet<Cell>(), new Vertex(cell));
		System.out.println("ReducedGraph generated");
		System.out.println("ReducedGraph numVertices: " + vertices.size());
		System.out.println("ReducedGraph numCheckpointVertices: " + checkpointVertices.size());
	}
	
	public Set<Vertex> getVertices() {
		return Collections.unmodifiableSet(vertices);
	}
	
	public Set<Vertex> getCheckpointVertices() {
		return Collections.unmodifiableSet(checkpointVertices);
	}
	
	public Vertex getVertex(Cell cell) {
		//may throw error, but i wanna know 
		return cellsToVerticesMap.get(cell);
	}
	   	
	// graph must be connected
	// using cell/vertex map rather than vertex set for faster look up time when
	// getting the initialised vertex of a cell
	private void reduceGraph(Set<Cell> discoveredCells, Vertex currentVertex) {
		//iterates through each adjacent cell and recurses (DFS)
		for (Cell adjacentCell : currentVertex.getCell().getAdjacentCells()) {
			
			//if a cell has been discovered, then it has already been dealt with in the DFS, so ignore it
			if (!discoveredCells.contains(adjacentCell)) {
				
				//build the edge from this vertex to the next vertex (junction/checkpoint)
				Edge edge = new Edge();
				Cell previousCell = currentVertex.getCell();
				Cell nextCell = adjacentCell;
				edge.appendCell(previousCell);
				while (nextCell.getOrder() == 2 && !nextCell.hasCheckpoint()) {
					discoveredCells.add(nextCell);
					edge.appendCell(nextCell);
					for (Cell nextCell2 : nextCell.getAdjacentCells()) {
						if (nextCell2 != previousCell) {
							previousCell = nextCell;
							nextCell = nextCell2;
							break;
						}
					}
				}
				discoveredCells.add(nextCell);
				//check if not a loop (redundant, don't need edge)
				if (nextCell != currentVertex.getCell()) {
					edge.appendCell(nextCell);
					Vertex nextVertex;
					boolean recurse = false;
					if (cellsToVerticesMap.containsKey(nextCell)) {
						nextVertex = cellsToVerticesMap.get(nextCell);
					} else {
						nextVertex = new Vertex(nextCell);
						cellsToVerticesMap.put(nextCell, nextVertex);
						vertices.add(nextVertex);
						if(nextVertex.getCell().hasCheckpoint()) {
							checkpointVertices.add(nextVertex);
						}
						discoveredCells.add(nextCell);
						recurse = true;
					}
					currentVertex.addAdjacentVertex(nextVertex, edge);
					if(recurse) {
						reduceGraph(discoveredCells, nextVertex);
					}
				}
			}
		}
	}

}