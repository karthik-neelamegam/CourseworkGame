package map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logic.Direction;
import logic.Entity;
import logic.Player;
import user_interface.Application;

public class Cell extends Entity {

	private List<Neighbouring> neighbourings;
	private List<Cell> adjacentCells;
	private boolean isCheckpoint;
	private List<Player> encounteredPlayers;
	private Surface surface;
	private double wallProportionOfCellDimensions;
	private double checkpointProportionOfCellDimensions;
	private Color wallColor;
	private Color checkpointColor;

	public Cell(double x, double y, double sideLength, Surface surface,
			double wallProportionOfCellDimensions, Color wallColor,
			double checkpointProportionOfCellDimensions, Color checkpointColor) {
		super(x, y, sideLength, sideLength);
		neighbourings = new ArrayList<Neighbouring>();
		adjacentCells = new ArrayList<Cell>();
		isCheckpoint = false;
		this.surface = surface;
		this.wallProportionOfCellDimensions = wallProportionOfCellDimensions;
		this.checkpointProportionOfCellDimensions = checkpointProportionOfCellDimensions;
		this.wallColor = wallColor;
		this.checkpointColor = checkpointColor;
	}

	public void setCheckpoint() {
		if (!isCheckpoint) {
			isCheckpoint = true;
			encounteredPlayers = new ArrayList<Player>();
		}
	}

	public boolean isCheckpoint() {
		return isCheckpoint;
	}

	public boolean addEncounteredPlayer(Player Encounteredplayer) {
		if (!encounteredPlayers.contains(Encounteredplayer)) {
			encounteredPlayers.add(Encounteredplayer);
			return true;
		}
		return false;
	}

	public void addNeighbouringCell(Cell neighbouringCell,
			Direction directionFromThisCellToNeighbouringCell) {
		neighbourings.add(new Neighbouring(neighbouringCell,
				directionFromThisCellToNeighbouringCell));
		neighbouringCell.neighbourings.add(new Neighbouring(this,
				directionFromThisCellToNeighbouringCell.getOpposite()));
	}

	public Direction getDirectionToNeighbouringCell(Cell neighbouringCell) {
		for (Neighbouring neighbouring : neighbourings) {
			if (neighbouring.getNeighbouringCell() == neighbouringCell) {
				return neighbouring.getDirectionToNeighbouringCell();
			}
		}
		return null;
	}

	public List<Neighbouring> getNeighbourings() {
		return Collections.unmodifiableList(neighbourings);
	}

	public Cell getNeighbouringCell(Direction direction) {
		for (Neighbouring neighbouring : neighbourings) {
			if (neighbouring.getDirectionToNeighbouringCell() == direction) {
				return neighbouring.getNeighbouringCell();
			}
		}
		return null;
	}

	public void setAdjacentTo(Cell adjacentCell) {
		adjacentCells.add(adjacentCell);
		adjacentCell.adjacentCells.add(this);
	}

	// talk about this somewhere in technical solution (unmodifiable)
	public List<Cell> getAdjacentCells() {
		return Collections.unmodifiableList(adjacentCells);
	}

	public int getOrder() {
		return adjacentCells.size();
	}

	public Cell getRandomNeighbouringNonAdjacentCell() {
		List<Cell> neighbouringNonAdjacentCells = new ArrayList<Cell>();
		for (Neighbouring neighbouring : neighbourings) {
			Cell cell = neighbouring.getNeighbouringCell();
			if (!isAdjacentTo(cell)) {
				neighbouringNonAdjacentCells.add(cell);
			}
		}
		int randIndex = Application.rng.nextInt(neighbouringNonAdjacentCells
				.size());
		return neighbouringNonAdjacentCells.get(randIndex);
	}

	// O(n) but set max 4 size so essentially constant time
	public Cell getRandomAdjacentCell() {
		int size = adjacentCells.size();
		int randIndex = Application.rng.nextInt(size);
		return adjacentCells.get(randIndex);
	}

	public double getSpeedMultiplier() {
		return surface.getSpeedMultiplier();
	}

	public boolean isAdjacentTo(Cell otherCell) {
		return adjacentCells.contains(otherCell);
	}

	public boolean isInCell(double pointX, double pointY) {
		return pointX >= x && pointX <= x + width && pointY >= y
				&& pointY <= y + height;
	}

	public double getWeightedDistanceToAdjacentCell(Cell adjacentCell) {
		if (!isAdjacentTo(adjacentCell)) {
			return Double.MAX_VALUE; // shouldn't happen;
		}
		return 0.5
				* height
				* (1 / getSpeedMultiplier() + 1 / adjacentCell
						.getSpeedMultiplier());
	}

	@Override
	public void update() {
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(surface.getColor());
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		if (isCheckpoint) {
			g.setColor(checkpointColor);
			g.fillOval((int) (x + width
					* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (y + height
							* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (width * checkpointProportionOfCellDimensions),
					(int) (height * checkpointProportionOfCellDimensions));
			int numPlayersEncountered = encounteredPlayers.size();
			double startAngle = 0;
			double arcAngle = 360 / (numPlayersEncountered + 1);
			for (Player player : encounteredPlayers) {
				g.setColor(player.getColor());
				g.fillArc((int) (x + width
						* (1 - checkpointProportionOfCellDimensions) / 2),
						(int) (y + height
								* (1 - checkpointProportionOfCellDimensions)
								/ 2),
						(int) (width * checkpointProportionOfCellDimensions),
						(int) (height * checkpointProportionOfCellDimensions),
						(int) startAngle, (int) arcAngle);
				startAngle += arcAngle;
			}
			g.setColor(Color.BLACK);
			g.drawOval((int) (x + width
					* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (y + height
							* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (width * checkpointProportionOfCellDimensions),
					(int) (height * checkpointProportionOfCellDimensions));
		}
		renderWalls(g);
		g.setColor(lastColor);
	}

	public void renderWalls(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(wallColor);
		for (Neighbouring neighbouring : neighbourings) {
			Cell cell = neighbouring.getNeighbouringCell();
			if (!isAdjacentTo(cell)) {
				Direction direction = getDirectionToNeighbouringCell(cell);
				double wallY = (direction != Direction.DOWN) ? y : y + height;
				double wallX = (direction != Direction.RIGHT) ? x : x + width;
				Graphics2D g2d = (Graphics2D) g;
				Stroke oldStroke = g2d.getStroke();
				double strokeWeight = wallProportionOfCellDimensions * (height);
				Stroke stroke = new BasicStroke((int) strokeWeight,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				g2d.setStroke(stroke);
				g.setColor(wallColor);
				if (direction == Direction.UP || direction == Direction.DOWN) {
					g.drawLine((int) wallX, (int) wallY, (int) (wallX + width),
							(int) wallY);
				} else {
					g.drawLine((int) wallX, (int) wallY, (int) (wallX),
							(int) (wallY + height));
				}
				g2d.setStroke(oldStroke);
			}
		}
		g.setColor(lastColor);
	}

}
