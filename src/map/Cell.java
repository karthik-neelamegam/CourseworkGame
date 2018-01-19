package map;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logic.Direction;
import logic.Entity;
import user_interface.Application;

public class Cell extends Entity {

	// need to refactor this whole thing

	// using adjacency lists because sparse
	// CAN CHANGE TO ARRAYLIST IF THE NEED ARISES! I.E. IF DON'T NEED TO STORE
	// WALL OBJECTS
	// private HashMap<Cell, Wall> neighbouringCellss; // list because might
	// change
	// shape and cells in
	// edge/corner have fewer
	// neighbours
	private class Adjacency {
		private Direction direction;
		private Surface surface;
		public Adjacency(Direction direction, Surface surface) {
			this.direction = direction;
			this.surface = surface;
		}
		public Direction getDirection() {
			return direction;
		}
		public Surface getSurface() {
			return surface;
		}
	}
	
	private Map<Cell, Direction> neighbouringCellsToDirectionsMap; //need to lookup both ways, bimap overkill because only 4 values, enummap essentially an array anyway, better than hashmap in terms of space?
	private Map<Direction, Cell> directionstoNeighbouringCellsMap;
	private Map<Cell, Adjacency> adjacentCellsMap; // explain why map vs list (constant look up), never need to look up cell from direction
	private boolean visited;
	private Checkpoint checkpoint;

	public Cell(double x, double y, double side) {
		super(x, y, side, side);
		visited = false;
		neighbouringCellsToDirectionsMap = new HashMap<Cell, Direction>();
		directionstoNeighbouringCellsMap = new EnumMap<Direction, Cell>(Direction.class);
		adjacentCellsMap = new HashMap<Cell, Adjacency>();
	}

	public Point getMidpoint(Cell cell) {
		int midX = (int) ((float) ((x + cell.x + (float) width / 2 + (float) cell.width / 2)) / 2);
		int midY = (int) ((float) ((y + cell.y + (float) height / 2 + (float) cell.height / 2)) / 2);
		return new Point(midX, midY);
	}

	public void addCheckpoint() {
		if (checkpoint == null) {
			checkpoint = new Checkpoint(x, y, width, height);
		} else {
			System.out.println("Cell already has checkpoint");
		}
	}

	void addNeighbouringCell(Cell cell, Direction direction) {
		neighbouringCellsToDirectionsMap.put(cell, direction);
		cell.neighbouringCellsToDirectionsMap.put(this, direction.getOpposite());
		directionstoNeighbouringCellsMap.put(direction, cell);
		cell.directionstoNeighbouringCellsMap.put(direction.getOpposite(), this);
	}
	
	public Cell getNeighbouringCell(Direction direction) {
		Cell neighbouringCell = null;
		if(directionstoNeighbouringCellsMap.containsKey(direction)) {
			neighbouringCell = directionstoNeighbouringCellsMap.get(direction);
		} 
		return neighbouringCell;
	}
	
	void setAdjacentTo(Cell cell, Surface surface) {
		//nullpointerexception could occur, but if it does i want to know why
		Direction direction = neighbouringCellsToDirectionsMap.get(cell);
		adjacentCellsMap.put(cell, new Adjacency(direction, surface));
		cell.adjacentCellsMap.put(this, new Adjacency(direction.getOpposite(), surface));
	}

	// talk about this somewhere in technical solution
	public Set<Cell> getAdjacentCells() {
		return Collections.unmodifiableSet(adjacentCellsMap.keySet());
	}
	
	public Cell getRandomUnvisitedNeighbouringCell() {
		ArrayList<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();
		Cell neighbouringCell = null;
		for (Cell cell : neighbouringCellsToDirectionsMap.keySet()) {
			if (!cell.isVisited()) {
				unvisitedNeighbouringCells.add(cell);
			}
		}
		if (unvisitedNeighbouringCells.size() != 0) {
			int random = Application.rng.nextInt(unvisitedNeighbouringCells
					.size());
			neighbouringCell = unvisitedNeighbouringCells.get(random);
		}
		return neighbouringCell;
	}

	// O(n) but set max 4 size so essentially constant time
	public Cell getRandomAdjacentCell() {
		int size = adjacentCellsMap.size();
		int randomIndex = Application.rng.nextInt(size);
		int i = 0;
		Iterator<Cell> adjacentCellsIterator = adjacentCellsMap.keySet().iterator();
		while (i < randomIndex) {
			i++;
			adjacentCellsIterator.next();
		}
		return adjacentCellsIterator.next();
	}

	public double getSpeedMultiplier(Cell adjacentCell) {
		return adjacentCellsMap.get(adjacentCell).getSurface().getSpeedMultiplier();
	}
	
	public boolean isAdjacentTo(Cell other) {
		return adjacentCellsMap.containsKey(other);
	}

	public double getWeightedDistanceToAdjacentCell(Cell cell) {
		double euclideanDistance = getEuclideanDistanceBetweenCentres(cell);
		double weightedDistance = euclideanDistance*getSpeedMultiplier(cell);
		return weightedDistance;
	}
	
	public boolean hasCheckpoint() {
		return checkpoint != null;
	}

	public int getOrder() {
		return adjacentCellsMap.size();
	}

	public boolean isVisited() {
		return visited;
	}

	void setVisited() {
		this.visited = true;
	}

	@Override
	public void update(double delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render(Graphics g) {
		//TODO: render surfaces
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		if (checkpoint != null) {
			checkpoint.render(g);
		}
	}

}
