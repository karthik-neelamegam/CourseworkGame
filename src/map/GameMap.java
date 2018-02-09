package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import logic.Direction;
import logic.Entity;
import user_interface.Application;
import dsa.DisjointSet;

public class GameMap extends Entity {

	private Cell[][] cells;
	private Color groundColor;
	private int numCheckpoints;

	public GameMap(int numCellsWide, int numCellsHigh, double x, double y,
			double cellSideLength, double deadEndProbability,
			double wallProportionOfCellDimensions,
			double checkpointProportionOfCellDimensions,
			int numCheckpointsExcludingEndpoints, EnumMap<Surface, Double> surfaceRatios,
			Color checkpointColor, Color wallColor, Color groundColor,
			GameMapType gameMapType) {
		super(x, y, numCellsWide * cellSideLength, numCellsHigh
				* cellSideLength);
		this.groundColor = groundColor;
		this.numCheckpoints = numCheckpointsExcludingEndpoints + 2;
		cells = initCells(numCellsWide, numCellsHigh, cellSideLength,
				wallProportionOfCellDimensions, wallColor,
				checkpointProportionOfCellDimensions, checkpointColor, surfaceRatios);
		switch (gameMapType) {
		case KRUSKAL:
			initKruskalPerfectMaze();
			break;
		case DFS:
			initDepthFirstSearchPerfectMaze();
			break;
		}
		removeDeadEnds(deadEndProbability);
		placeCheckpoints(numCheckpointsExcludingEndpoints);
	}

	private Surface getRandomSurface(EnumMap<Surface, Double> surfaceRatios) {
		double totalRatio = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			double ratio = entry.getValue();
			totalRatio += ratio;
		}

		double rand = Application.rng.nextDouble() * totalRatio;
		double cumulativeRatios = 0;
		Iterator<Entry<Surface, Double>> surfaceRatiosIterator = surfaceRatios
				.entrySet().iterator();
		Entry<Surface, Double> entry = null;
		do {
			entry = surfaceRatiosIterator.next();
			cumulativeRatios += entry.getValue();
		} while(cumulativeRatios < rand);
		return entry.getKey();
	}

	
	private Cell[][] initCells(int numCellsWide, int numCellsHigh,
			double cellSideLength,
			double wallProportionOfCellDimensions, Color wallColor,
			double checkpointProportionOfCellDimensions, Color checkpointColor,
			EnumMap<Surface, Double> surfaceRatios) {
		Cell[][] cells = new Cell[numCellsWide][numCellsHigh];
		for (int column = 0; column < cells.length; column++) {
			for (int row = 0; row < cells[column].length; row++) {
				Cell currentCell = new Cell(x + column * cellSideLength, y
						+ row * cellSideLength, cellSideLength,
						getRandomSurface(surfaceRatios),
						wallProportionOfCellDimensions, wallColor,
						checkpointProportionOfCellDimensions, checkpointColor);
				cells[column][row] = currentCell;
				if (row > 0) {
					Cell neighbouringCell = cells[column][row - 1];
					currentCell.addNeighbouringCell(neighbouringCell,
							Direction.UP);
				}
				if (column > 0) {
					Cell neighbouringCell = cells[column - 1][row];
					currentCell.addNeighbouringCell(neighbouringCell,
							Direction.LEFT);
				}
			}
		}
		return cells;
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void initDepthFirstSearchPerfectMaze() {
		Stack<Cell> cellStack = new Stack<Cell>();
		Set<Cell> visitedCells = new HashSet<Cell>();
		Cell currentCell = cells[0][0];
		visitedCells.add(currentCell);
		do {
			List<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();
			for (Neighbouring neighbouring : currentCell.getNeighbourings()) {
				Cell neighbouringCell = neighbouring.getNeighbouringCell();
				if (!visitedCells.contains(neighbouringCell)) {
					unvisitedNeighbouringCells.add(neighbouringCell);
				}
			}
			if (!unvisitedNeighbouringCells.isEmpty()) {
				Cell randomUnvisitedNeighbouringCell = unvisitedNeighbouringCells
						.get(Application.rng.nextInt(unvisitedNeighbouringCells
								.size()));
				cellStack.push(currentCell);
				currentCell.setAdjacentTo(randomUnvisitedNeighbouringCell);
				currentCell = randomUnvisitedNeighbouringCell;
				visitedCells.add(randomUnvisitedNeighbouringCell);
			} else if (!cellStack.isEmpty()) {
				currentCell = cellStack.pop();
			}
		} while (!cellStack.isEmpty());
	}
	
/*	private void initRecursiveBacktrackerPerfectMaze() {
		recurse(cells[0][0], new HashSet<Cell>());
	}
	private void recurse(Cell currentCell, Set<Cell> visitedCells) {
		List<Neighbouring> neighbourings = currentCell.getNeighbourings();
		List<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < neighbourings.size(); i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);
		for(Integer index : indices) {
			Cell neighbouringCell = neighbourings.get(index).getCell();
			if (!visitedCells.contains(neighbouringCell)) {
				currentCell.setAdjacentTo(neighbouringCell);
				visitedCells.add(neighbouringCell);
				recurse(neighbouringCell, visitedCells);
			}
		}
	}
*/
	
	private void initKruskalPerfectMaze() {
		DisjointSet<Cell> cellDisjointSet = new DisjointSet<Cell>();
		class Wall {
			private Cell cell1;
			private Cell cell2;

			public Wall(Cell c1, Cell c2) {
				cell1 = c1;
				cell2 = c2;
			}

			// hashing really?
			@Override
			public int hashCode() {
				return cell1.hashCode() * cell2.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (o instanceof Wall) {
					Wall other = (Wall) o;
					return (other.cell1 == cell2 && other.cell2 == cell1)
							|| (other.cell1 == cell1 && other.cell2 == cell2);
				}
				return false;
			}
			
			public Cell getCell1() {
				return cell1;
			}
			
			public Cell getCell2() {
				return cell2;
			}
		}
		Set<Wall> wallsSet = new HashSet<Wall>();
		// created wall set rather than having two lists (cells and neighbouring
		// cells) because only want one wall per pair, and contains is O(n) for
		// list but O(1) for set

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell cell = cells[i][j];
				cellDisjointSet.add(cell);
				for (Neighbouring neighbouring : cell.getNeighbourings()) {
					Cell neighbouringCell = neighbouring.getNeighbouringCell();
					if (!cell.isAdjacentTo(neighbouringCell)) {
						wallsSet.add(new Wall(cell, neighbouringCell));
					}
				}
			}
		}

		List<Wall> wallsList = new ArrayList<Wall>(wallsSet);
		Collections.shuffle(wallsList, Application.rng);
		for (Wall wall : wallsList) {
			Cell cell1 = wall.getCell1();
			Cell cell2 = wall.getCell2();
			if (!cellDisjointSet.areJoined(cell1, cell2)) {
				cell1.setAdjacentTo(cell2);
				cellDisjointSet.join(cell1, cell2);
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
				if (Application.rng.nextFloat() < deadEndProbability) {
					Cell neighbouringNonAdjacentCell = cell.getRandomNeighbouringNonAdjacentCell();
					cell.setAdjacentTo(neighbouringNonAdjacentCell);
				}
			}
		}
	}

	private void placeCheckpoints(int numCheckpointsExcludingEndpoints) {
		getStartCell().setCheckpoint();
		getEndCell().setCheckpoint();
		for (int i = 0; i < numCheckpointsExcludingEndpoints; i++) {
			int x = Application.rng.nextInt(cells.length);
			int y = Application.rng.nextInt(cells[x].length);
			Cell checkpointCell = cells[x][y];
			if (checkpointCell.isCheckpoint()) {
				i--;
			} else {
				checkpointCell.setCheckpoint();
			}
		}
	}

	@Override
	public void update() {

	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
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
		g.setColor(lastColor);
	}

	public Cell getStartCell() {
		return cells[0][0];
	}

	public Cell getEndCell() {
		return cells[cells.length - 1][cells[0].length - 1];
	}

	public double getCellSideLength() {
		return cells[0][0].getHeight();
	}

	public int getNumCheckpoints() {
		return numCheckpoints;
	}

}
