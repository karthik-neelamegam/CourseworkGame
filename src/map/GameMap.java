package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import logic.Direction;
import logic.Entity;
import user_interface.Application;
import dsa.DisjointSet;

public class GameMap extends Entity {

	private Cell[][] cells;
	private Color groundColor;
	private int numCheckpoints;
	public GameMap(int numCellsWide, int numCellsHigh, double x, double y, double cellSideLength, double deadEndProbability, double wallProportionOfCellDimensions, double checkpointProportionOfCellDimensions, int numCheckpointsExcludingEndpoints, SurfacePicker surfacePicker, Color checkpointColor, Color wallColor, Color groundColor, GameMapType mapType) {
		super(x, y, numCellsWide*cellSideLength, numCellsHigh*cellSideLength);
		this.groundColor = groundColor;
		this.numCheckpoints = numCheckpointsExcludingEndpoints+2;
		initCells(numCellsWide, numCellsHigh, cellSideLength, wallColor, wallProportionOfCellDimensions, checkpointProportionOfCellDimensions, surfacePicker);
		System.out.println("Cells initialised");
		switch(mapType) {
		case KRUSKAL:
			initKruskalPerfectMaze();
			break;
		case DFS:
			initDepthFirstPerfectMaze();
			break;
		}
		removeDeadEnds(deadEndProbability);
		placeCheckpoints(numCheckpointsExcludingEndpoints,
				checkpointColor);
	}

	public Cell getStartCell() {
		return cells[0][0];
	}
	
	public Cell getEndCell() {
		return cells[cells.length-1][cells[0].length-1];
	}
	
	public int getNumCheckpoints() {
		return numCheckpoints;
	}
	
	private void initCells(int numCellsWide, int numCellsHigh, double cellSideLength, Color wallColor, double wallProportionOfCellDimensions, double checkpointProportionOfCellDimensions, SurfacePicker surfacePicker) {
		cells = new Cell[numCellsWide][numCellsHigh];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = new Cell(x + i * cellSideLength, y + j * cellSideLength, cellSideLength, surfacePicker.getRandomSurface(), wallProportionOfCellDimensions, wallColor, checkpointProportionOfCellDimensions);
				cells[i][j] = currentCell;
				if (j > 0) {
					Cell neighbouringCell = cells[i][j - 1];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.NORTH);
				}
				if (i > 0) {
					Cell neighbouringCell = cells[i - 1][j];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.WEST);
				}
			}
		}
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void initDepthFirstPerfectMaze() {
		Stack<Cell> stack = new Stack<Cell>();
		Set<Cell> visitedCells = new HashSet<Cell>();
		Cell currentCell = cells[0][0];
		visitedCells.add(currentCell);
		do {
			List<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();
			for(Cell neighbouringCell : currentCell.getNeighbouringCells()) {
				if(!visitedCells.contains(neighbouringCell)) {
					unvisitedNeighbouringCells.add(neighbouringCell);
				}
			}
			if(!unvisitedNeighbouringCells.isEmpty()) {
				Cell randomUnvisitedNeighbouringCell = unvisitedNeighbouringCells.get(Application.rng.nextInt(unvisitedNeighbouringCells.size()));
				stack.push(currentCell);
				currentCell.setAdjacentTo(randomUnvisitedNeighbouringCell);
				currentCell = randomUnvisitedNeighbouringCell;
				visitedCells.add(randomUnvisitedNeighbouringCell);
			} else if (!stack.isEmpty()) {
				currentCell = stack.pop();
			}
		} while (!stack.isEmpty());
	}	
	
	private void initKruskalPerfectMaze() {
		DisjointSet<Cell> cellDisjointSet = new DisjointSet<Cell>();
		Set<Wall> wallsSet = new HashSet<Wall>();
		for(int i = 0; i < cells.length; i++) {
			for(int j = 0; j < cells[i].length; j++) {
				Cell cell = cells[i][j];
				cellDisjointSet.add(cell);
				wallsSet.addAll(cell.getWalls());
			}
		}
		List<Wall> wallsList = new ArrayList<Wall>(wallsSet);
		Collections.shuffle(wallsList, Application.rng);
		List<Wall> wallsToKeep = new ArrayList<Wall>();
		for(Wall wall : wallsList) {
			Cell cell1 = wall.getCell1();
			Cell cell2 = wall.getCell2();
			if(cell1 == null || cell2 == null) {
				System.out.println("NULL");
			}
			if(!cellDisjointSet.areJoined(cell1, cell2)) {
				cell1.setAdjacentTo(cell2);
				cellDisjointSet.union(cell1, cell2);
			} else {
				wallsToKeep.add(wall);
			}
		}
	}
	
	private void removeDeadEnds(double deadEndProbability) {
		List<Cell> cellsList = new ArrayList<Cell>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cellsList.add(cells[i][j]);
			}
		}
		Collections.shuffle(cellsList, Application.rng);
		for (Cell cell : cellsList) {
			if (cell.getOrder() <= 1) {
				System.out.println("REMOVING");
				if (Application.rng.nextFloat() < deadEndProbability) {
					Cell walledCell = cell.getRandomNeighbouringWalledCell();
					cell.setAdjacentTo(walledCell);
				}
			}
		}
	}

	private void placeCheckpoints(int numCheckpointsExcludingEndpoints, Color checkpointColor) {
		getStartCell().addCheckpoint(checkpointColor);
		getEndCell().addCheckpoint(checkpointColor);
		for (int i = 0; i < numCheckpointsExcludingEndpoints; i++) {
			int x = Application.rng.nextInt(cells.length);
			int y = Application.rng.nextInt(cells[x].length);
			Cell checkpointCell = cells[x][y];
			if(checkpointCell.hasCheckpoint()) {
				i--;
			} else {
				checkpointCell.addCheckpoint(checkpointColor);
			}
		}
	}

	public double getCellSide() {
		return cells[0][0].getHeight();
	}

	@Override
	public void update(double delta) {

	}

	@Override
	public void render(Graphics g) {
		g.setColor(groundColor);
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.render(g);
			}
		}
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.renderWalls(g);
			}
		}
	}

}
