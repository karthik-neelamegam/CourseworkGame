package map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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

	//REFACTOR THIS 
	//don't need this
	//private Map<Cell, Direction> neighbouringCellsToDirectionsMap; //need to lookup both ways, bimap overkill because only 4 values, enummap essentially an array anyway, better than hashmap in terms of space?
	//private Map<Direction, Cell> directionstoNeighbouringCellsMap;
	private Map<Cell, Direction> adjacentCellsToDirectionsMap; // explain why map vs list (constant look up), never need to look up cell from direction
	//private Map<Cell, Direction> nonAdjacentCellsMap;
	//private Map<Direction, Cell> directionsToNeighbouringCellsMap;
	private Map<Direction, Cell> directionsToAdjacentCellsMap;
	private Map<Cell, Wall> cellsToWallsMap;
	//NEED TO ARGUE FOR WHY I CHOSE THESE DATA STRUCTURES, TALK ABOUT MEMORY AND ACCESS TIME COMPLEXITY AND WHEN, WHERE AND HOW OFTEN CERTAIN ACCESSES ARE MADE etc.
	private int I, J;
	private Checkpoint checkpoint;
	private Surface surface;
	private double wallProportionOfCellDimensions;
	private double checkpointProportionOfCellDimensions;
	private Color wallColor;

	public Cell(double x, double y, double side, Surface surface, double wallProportionOfCellDimensions, Color wallColor, double checkpointProportionOfCellDimensions) {
		super(x, y, side, side);
		adjacentCellsToDirectionsMap = new HashMap<Cell, Direction>();
		directionsToAdjacentCellsMap = new EnumMap<Direction, Cell>(Direction.class);
		cellsToWallsMap = new HashMap<Cell, Wall>();
		this.surface = surface;
		this.wallProportionOfCellDimensions = wallProportionOfCellDimensions;
		this.checkpointProportionOfCellDimensions = checkpointProportionOfCellDimensions;
		this.wallColor = wallColor;
	}
		
	public Direction getDirectionTo(Cell cell) {
		return adjacentCellsToDirectionsMap.get(cell);
	}

	public Point getMidpoint(Cell cell) {
		int midX = (int) ((float) ((x + cell.x + (float) width / 2 + (float) cell.width / 2)) / 2);
		int midY = (int) ((float) ((y + cell.y + (float) height / 2 + (float) cell.height / 2)) / 2);
		return new Point(midX, midY);
	}

	public void addCheckpoint(Color checkpointColor) {
		if (checkpoint == null) {
			checkpoint = new Checkpoint(x, y, width, height, checkpointColor, checkpointProportionOfCellDimensions);
		} else {
			System.out.println("Cell already has checkpoint");
		}
	}
	
	public Checkpoint getCheckpoint() {
		return checkpoint;
	}
	
	void addNeighbouringCell(Cell neighbouringCell, Direction directionFromThisCellToNeighbouringCell) {
		Wall wall = new Wall(this, neighbouringCell, directionFromThisCellToNeighbouringCell);
		cellsToWallsMap.put(neighbouringCell, wall);
		neighbouringCell.cellsToWallsMap.put(this, wall);
		//nonAdjacentCellsMap.put(cell, direction);
		//cell.nonAdjacentCellsMap.put(this, direction.getOpposite());
/*		neighbouringCellsToDirectionsMap.put(cell, direction);
		cell.neighbouringCellsToDirectionsMap.put(this, direction.getOpposite());
		directionstoNeighbouringCellsMap.put(direction, cell);
		cell.directionstoNeighbouringCellsMap.put(direction.getOpposite(), this);
*/	}

	public Set<Cell> getNeighbouringCells() {
		return Collections.unmodifiableSet(cellsToWallsMap.keySet());
	}
	
	public Collection<Wall> getWalls() {
		return Collections.unmodifiableCollection(cellsToWallsMap.values());
	}
	
	public void setAdjacentTo(Cell cell) {
		//nullpointerexception could occur, but if it does i want to know why
		Wall wall = cellsToWallsMap.get(cell);
		Direction direction = wall.getDirectionFromCell1ToCell2();
		if(wall.getCell2() == this) {
			direction = direction.getOpposite();
		}
		cellsToWallsMap.put(cell, null);
		cell.cellsToWallsMap.put(this, null);
		adjacentCellsToDirectionsMap.put(cell, direction);
		cell.adjacentCellsToDirectionsMap.put(this, direction.getOpposite());
		directionsToAdjacentCellsMap.put(direction, cell);
		cell.directionsToAdjacentCellsMap.put(direction.getOpposite(), this);
	}
	
	// talk about this somewhere in technical solution (unmodifiable)
	public Set<Cell> getAdjacentCells() {
		return Collections.unmodifiableSet(adjacentCellsToDirectionsMap.keySet());
	}
	
	public Cell getRandomNeighbouringWalledCell() {
		List<Cell> walledCells = new ArrayList<Cell>();
		for(Entry<Cell,Wall> entry : cellsToWallsMap.entrySet()) {
			if(entry.getValue() != null) {
				walledCells.add(entry.getKey());
			}
		}
		int randIndex = Application.rng.nextInt(walledCells.size());
		return walledCells.get(randIndex);
	}
	
	// O(n) but set max 4 size so essentially constant time
	public Cell getRandomAdjacentCell() {
		Set<Cell> adjacentCells = getAdjacentCells();
		int size = adjacentCells.size();
		int randIndex = Application.rng.nextInt(size);
		int i = 0;
		Cell selectedCell = null;
		for(Cell cell : adjacentCells) {
			if(i == randIndex) {
				selectedCell = cell;
				break;
			}
			i++;
		}
		return selectedCell;
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
		return adjacentCellsToDirectionsMap.containsKey(other);
	}
	
	public Cell getAdjacentCell(Direction direction) {
		return directionsToAdjacentCellsMap.get(direction);
		//throws error but wanna know 
	}

	public boolean isInCell(double px, double py) {
		return px >= x && px <= x+width && py >= y && py <= y+height;  
	}
	
	public double getWeightedDistanceToAdjacentCell(Cell cell) {
		double distance;
		Direction direction = adjacentCellsToDirectionsMap.get(cell);
		//remove, do throw exception etc.
		if(direction == null) {
			System.out.println("FUCKKKKKKKK");
			System.exit(0);
		}
		if(direction == Direction.NORTH || direction == Direction.SOUTH) {
			distance = height;
		} else {
			distance = width;
		}
		double weightedDistance = (distance/2)/getSpeedMultiplier() + (distance/2)/cell.getSpeedMultiplier();
		return weightedDistance;

/*		double euclideanDistance = getEuclideanDistanceBetweenCentres(cell);
		double weightedDistance = euclideanDistance*getSpeedMultiplier(cell);
		return weightedDistance;
*/	}
	
	public boolean hasCheckpoint() {
		return checkpoint != null;
	}

	public int getOrder() {
		return adjacentCellsToDirectionsMap.size();
	}

	@Override
	public void update(double delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(surface.getColor());
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		if (checkpoint != null) {
			checkpoint.render(g);
		}
		g.setColor(lastColor);
	}
	
	public void renderWalls(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(wallColor);
		for(Entry<Cell, Wall> entry : cellsToWallsMap.entrySet()) {
			Wall wall;
			if((wall = entry.getValue()) != null) {
				Direction direction = wall.getDirectionFromCell1ToCell2();
				if(wall.getCell2() == this) {
					direction = direction.getOpposite();
				}
				renderWall(g, direction);
			}
		}
		g.setColor(lastColor);
	}
	
	private void renderWall(Graphics g, Direction dir) {
		double wallY = (dir != Direction.SOUTH) ? y : y+height;
		double wallX = (dir != Direction.EAST) ? x : x+width;
		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = g2d.getStroke();
		double strokeWeight = wallProportionOfCellDimensions*(height); 
	    Stroke stroke = new BasicStroke((int)strokeWeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	    g2d.setStroke(stroke);
	    g.setColor(wallColor);
		if(dir == Direction.NORTH || dir == Direction.SOUTH) {
			g.drawLine((int)wallX, (int)wallY, (int)(wallX+width), (int)wallY);
		} else {
			g.drawLine((int)wallX, (int)wallY, (int)(wallX), (int)(wallY+height));
		}
		g2d.setStroke(oldStroke);
	}

}
