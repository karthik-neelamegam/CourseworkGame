package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import logic.Entity;
import logic.Immutable2DArrayList;
import user_interface.Application;

public class Maze extends Entity {

	private ArrayList<ArrayList<Cell>> cells;
	private List<Wall> walls;
	
	private List<Cell> checkpointCells; //may not need this at all
	
	public Maze(int numCellsWide, int numCellsHigh, int x, int y,
			int maxWidth, int maxHeight, float deadEndProbability,
			int numCheckpoints, SurfacePicker surfacePicker) {
		super(x, y, numCellsWide
				* Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide),
				numCellsHigh
						* Math.min(maxHeight / numCellsHigh, maxWidth
								/ numCellsWide));
		int cellSide = Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide);
		initCellMatrix(numCellsWide, numCellsHigh, cellSide, surfacePicker);
		System.out.println("Cell matrix initialised");
		generateDepthFirstPerfectMaze();
		System.out.println("Generated perfect maze");
		removeDeadEnds(deadEndProbability);
		System.out.println("Removed dead ends");
		initWallsList();
		System.out.println("Physical walls initialised");
		placeCheckpoints(numCheckpoints);
		System.out.println("Checkpoints placed");
	}

	private void initCellMatrix(int numCellsWide, int numCellsHigh, int cellSide, SurfacePicker surfacePicker) {
		cells = new ArrayList<ArrayList<Cell>>(); 
		for (int i = 0; i < numCellsWide; i++) {
			cells.add(new ArrayList<Cell>());
			for (int j = 0; j < numCellsHigh; j++) {
				Cell currentCell = new Cell(getX() + i * cellSide, getY() + j
						* cellSide, cellSide, surfacePicker.getRandomSurface());
				cells.get(i).add(currentCell);
				if (j > 0) {
					Cell neighbouringCell = cells.get(i).get(j-1);
					currentCell.addNeighbouringCell(neighbouringCell);
				}
				if (i > 0) {
					Cell neighbouringCell = cells.get(i-1).get(j);
					currentCell.addNeighbouringCell(neighbouringCell);
				}
			}
		}
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void generateDepthFirstPerfectMaze() {
		Stack<Cell> stack = new Stack<Cell>();
		Cell currentCell = cells.get(0).get(0);
		int unvisitedCells = cells.size() * cells.get(0).size();
		currentCell.setVisited();
		unvisitedCells--;
		do {
			Cell neighbouringCell;
			if ((neighbouringCell = currentCell
					.getRandomUnvisitedNeighbouringCell()) != null) {
				stack.push(currentCell);
				currentCell.removeWall(neighbouringCell);
				currentCell = neighbouringCell;
				neighbouringCell.setVisited();
				unvisitedCells--;
			} else if (!stack.isEmpty()) {
				currentCell = stack.pop();
			}
		} while (unvisitedCells > 0);
	}
	
	private void generateKruskalPerfectMaze() {
		
	}

	private void removeDeadEnds(float deadEndProbability) {
		List<Cell> cellsList = new ArrayList<Cell>();
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.get(i).size(); j++) {
				cellsList.add(cells.get(i).get(j));
			}
		}
		Collections.shuffle(cellsList, Application.rng);
		for (Cell cell : cellsList) {
			if (cell.getOrder() <= 1) {
				System.out.println("REMOVING");
				if (Application.rng.nextFloat() < deadEndProbability) {
					Cell adjacentCell = cell.getRandomAdjacentCell();
					cell.removeWall(adjacentCell);
				}
			}
		}
	}

	private void initWallsList() {
		walls = new ArrayList<Wall>();
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.get(i).size(); j++) {
				Cell currentCell = cells.get(i).get(j);
				if (j > 0) {
					Cell leftCell = cells.get(i).get(j-1);
					if (!currentCell.isAdjacent(leftCell)) {
						walls.add(Wall.createWall(currentCell, leftCell, 0.1));
					}
				}
				if (i > 0) {
					Cell upCell = cells.get(i-1).get(j);
					if (!currentCell.isAdjacent(upCell)) {
						walls.add(Wall.createWall(currentCell, upCell, 0.1));
					}
				}
			}
		}
	}

	//REMEMBER TO MAKE START AND END CHECKPOINTS AS WELL!!!
	private void placeCheckpoints(int numCheckpoints) {
		final int NUM_ATTEMPTS = 10;
		checkpointCells = new ArrayList<Cell>();
		for (int checkpointsPlaced = 0; checkpointsPlaced  < numCheckpoints; checkpointsPlaced ++) {
			float minTotalSquaredDistance = Float.MAX_VALUE;
			Cell minCell = null;
			for (int attempt = 0; attempt < NUM_ATTEMPTS; attempt++) {
				int x = Application.rng.nextInt(cells.size());
				int y = Application.rng.nextInt(cells.size());
				float totalSquaredDistance = 0;
				Cell candidateCell = cells.get(x).get(y);
				for(Cell checkpointCell : checkpointCells) {
					totalSquaredDistance += candidateCell.getEuclideanDistanceBetweenCentres(checkpointCell);
				}
				if (totalSquaredDistance < minTotalSquaredDistance) {
					minTotalSquaredDistance = totalSquaredDistance;
					minCell = candidateCell;
				}
			}
			Checkpoint checkpoint = new Checkpoint(minCell.getX()+minCell.getWidth()/4, minCell.getY()+minCell.getHeight()/4, minCell.getWidth()/2, minCell.getHeight()/2);
			minCell.addCheckpoint(checkpoint);
			checkpointCells.add(minCell);
		}
	}
	
	public List<Cell> getCheckpointCells() {
		return Collections.unmodifiableList(checkpointCells);
	}
		
	public Immutable2DArrayList<Cell> getCells() {
		return new Immutable2DArrayList<Cell>(cells);
	}
	
	@Override
	public void update(double delta) {

	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		for (List<Cell> cellsSubList : cells) {
			for (Cell cell : cellsSubList) {
				cell.render(g);
			}
		}
		for (Wall wall : walls) {
			wall.render(g);
		}
	}

}
