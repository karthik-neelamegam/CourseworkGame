package map;

import java.awt.Color;
import java.awt.Font;
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
	
	
	//REFACTOR THIS 
	//don't need this
	//private Map<Cell, Direction> neighbouringCellsToDirectionsMap; //need to lookup both ways, bimap overkill because only 4 values, enummap essentially an array anyway, better than hashmap in terms of space?
	//private Map<Direction, Cell> directionstoNeighbouringCellsMap;
	private Map<Cell, Direction> adjacentCellsMap; // explain why map vs list (constant look up), never need to look up cell from direction
	private Map<Cell, Direction> nonAdjacentCellsMap;
	
	private Checkpoint checkpoint;
	private Surface surface;

	public Cell(double x, double y, double side, Surface surface) {
		super(x, y, side, side);
		//neighbouringCellsToDirectionsMap = new HashMap<Cell, Direction>();
		//directionstoNeighbouringCellsMap = new EnumMap<Direction, Cell>(Direction.class);
		adjacentCellsMap = new HashMap<Cell, Direction>();
		nonAdjacentCellsMap = new HashMap<Cell, Direction>();
		adjacentCellsMap = new HashMap<Cell, Direction>();
		this.surface = surface;
	}

	public Point getMidpoint(Cell cell) {
		int midX = (int) ((float) ((x + cell.x + (float) width / 2 + (float) cell.width / 2)) / 2);
		int midY = (int) ((float) ((y + cell.y + (float) height / 2 + (float) cell.height / 2)) / 2);
		return new Point(midX, midY);
	}

	public void addCheckpoint(Color checkpointColor) {
		if (checkpoint == null) {
			checkpoint = new Checkpoint(x, y, width, height, checkpointColor);
		} else {
			System.out.println("Cell already has checkpoint");
		}
	}
	
	public Checkpoint getCheckpoint() {
		return checkpoint;
	}
	
	void addNeighbouringCell(Cell cell, Direction direction) {
		nonAdjacentCellsMap.put(cell, direction);
		cell.nonAdjacentCellsMap.put(this, direction.getOpposite());
/*		neighbouringCellsToDirectionsMap.put(cell, direction);
		cell.neighbouringCellsToDirectionsMap.put(this, direction.getOpposite());
		directionstoNeighbouringCellsMap.put(direction, cell);
		cell.directionstoNeighbouringCellsMap.put(direction.getOpposite(), this);
*/	}

	public Set<Cell> getNonAdjacentCells() {
		return Collections.unmodifiableSet(nonAdjacentCellsMap.keySet());
	}
	
/*	public Cell getNeighbouringCell(Direction direction) {
		//throws error but i wanna know
		return directionstoNeighbouringCellsMap.get(direction);
	}
*/		
	void setAdjacentTo(Cell cell) {
		//nullpointerexception could occur, but if it does i want to know why
		Direction direction = nonAdjacentCellsMap.get(cell);
		adjacentCellsMap.put(cell, direction);
		cell.adjacentCellsMap.put(this, direction.getOpposite());
	}

	// talk about this somewhere in technical solution
	public Set<Cell> getAdjacentCells() {
		return Collections.unmodifiableSet(adjacentCellsMap.keySet());
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

	
	public void setSurface(Surface surface) {
		this.surface = surface;
	}

	public double getSpeedMultiplier() {
		return surface.getSpeedMultiplier();
	}

/*	public double getSpeedMultiplier(Cell adjacentCell) {
		return adjacentCellsMap.get(adjacentCell).getSurface().getSpeedMultiplier();
	}*/
	
	public boolean isAdjacentTo(Cell other) {
		return adjacentCellsMap.containsKey(other);
	}
	
	public Cell getAdjacentCell(Direction direction) {
		Cell neighbouringCell = directionstoNeighbouringCellsMap.get(direction);
		Cell adjacentCell = null;
		if(adjacentCellsMap.containsKey(neighbouringCell)) {
			adjacentCell = neighbouringCell;
		}
		return adjacentCell;
	}

	public boolean isInCell(double px, double py) {
		return px >= x && px <= x+width && py >= y && py <= y+height;  
	}
	
	public double getWeightedDistanceToAdjacentCell(Cell cell) {
		double euclideanDistance = getEuclideanDistanceBetweenCentres(cell);
		double halfEuclideanDistance = euclideanDistance / 2;
		double weightedDistance = (halfEuclideanDistance)
				/ getSpeedMultiplier() + (halfEuclideanDistance / 2)
				/ cell.getSpeedMultiplier();
		return weightedDistance;

/*		double euclideanDistance = getEuclideanDistanceBetweenCentres(cell);
		double weightedDistance = euclideanDistance*getSpeedMultiplier(cell);
		return weightedDistance;
*/	}
	
	public boolean hasCheckpoint() {
		return checkpoint != null;
	}

	public int getOrder() {
		return adjacentCellsMap.size();
	}

	@Override
	public void update(double delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render(Graphics g) {
		//TODO: render surfaces
		g.setColor(surface.getColor());
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		g.setColor(Color.BLACK);
		g.drawString(""+getX(), (int)(x+width/4), (int)(y+height/4));
		g.drawString(""+getY(), (int)(x+width/4), (int)(y+3*height/4));

		if (checkpoint != null) {
			checkpoint.render(g);
		}
	}

}
