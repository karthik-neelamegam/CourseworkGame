package map;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import logic.Entity;
import user_interface.Application;

public class Cell extends Entity {
	
	//need to refactor this whole thing
	
	// using adjacency lists because sparse
	//CAN CHANGE TO ARRAYLIST IF THE NEED ARISES! I.E. IF DON'T NEED TO STORE WALL OBJECTS
	//private HashMap<Cell, Wall> neighbouringCellss; // list because might change
													// shape and cells in
													// edge/corner have fewer
													// neighbours
	private ArrayList<Cell> neighbouringCells;
	private HashSet<Cell> adjacentCells; //explain why set vs list (constant look up)
	private boolean visited;
	private Surface surface;
	private Checkpoint checkpoint;
	
	public Cell(int x, int y, int side, Surface surface) {
		super(x, y, side, side);
		visited = false;
		neighbouringCells = new ArrayList<Cell>();
		adjacentCells = new HashSet<Cell>();
		this.surface = surface;	
	}
	
	public Point getMidpoint(Cell cell) {
		int midX = (int)((float)((getX() + cell.getX() + (float)getWidth()/2 + (float)cell.getWidth()/2))/2);
		int midY = (int)((float)((getY() + cell.getY() + (float)getHeight()/2 + (float)cell.getHeight()/2))/2);
		return new Point(midX, midY);
	}
	
	public void addCheckpoint() {
		if(checkpoint == null) {
			checkpoint = new Checkpoint(getX(), getY(), getWidth(), getHeight());
		} else {
			System.out.println("Cell already has checkpoint");
		}
	}
	
	public void setSurface(Surface surface) {
		this.surface = surface;
	}

	public double getSpeedMultiplier() {
		return surface.getSpeedMultiplier();
	}
		
	void addNeighbouringCell(Cell cell) {
		neighbouringCells.add(cell);
		cell.neighbouringCells.add(this);
	}

	void removeWall(Cell cell) {
		if(neighbouringCells.contains(cell)) {
			adjacentCells.add(cell);
			cell.adjacentCells.add(this);
		}
		else {
			throw new NullPointerException();
		}
	}
	
	//talk about this somewhere in technical solution
	public Set<Cell> getAdjacentCells() {
		return Collections.unmodifiableSet(adjacentCells);
	}

	public Cell getRandomUnvisitedNeighbouringCell() {
		ArrayList<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();
		Cell neighbouringCell = null;
		for (Cell cell : neighbouringCells) {
			if (!cell.isVisited()) {
				unvisitedNeighbouringCells.add(cell);
			}
		}
		if (unvisitedNeighbouringCells.size() != 0) {
			int random = Application.rng.nextInt(unvisitedNeighbouringCells.size());
			neighbouringCell = unvisitedNeighbouringCells.get(random);
		}
		return neighbouringCell;
	}
	
	//O(n) but set is really small so doesn't matter
	public Cell getRandomAdjacentCell() {
		int size = adjacentCells.size();
		int randomIndex = Application.rng.nextInt(size);
		int i = 0;
		Iterator<Cell> adjacentCellsIterator = adjacentCells.iterator();
		while(i < randomIndex) {
			i++;
			adjacentCellsIterator.next();
		}
		return adjacentCellsIterator.next();
	}
	
	public boolean isAdjacent(Cell other) {
		return adjacentCells.contains(other);
	}
	
	public double getWeightedDistanceBetweenCentres(Cell other) {
		double euclideanDistance = getEuclideanDistanceBetweenCentres(other);
		double halfEuclideanDistance = euclideanDistance/2;
		double weightedDistance = (halfEuclideanDistance)/getSpeedMultiplier() + (halfEuclideanDistance/2)/other.getSpeedMultiplier();
		return weightedDistance;
	}

	public boolean hasCheckpoint() {
		return checkpoint != null;
	}
	
	public int getOrder() {
		return adjacentCells.size();
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
		if(checkpoint != null) {
			checkpoint.render(g);
		}
		g.setColor(Color.BLACK);
		g.drawString(""+adjacentCells.size(),getX()+getWidth()/2,getY()+getHeight()/2);
	}

}
