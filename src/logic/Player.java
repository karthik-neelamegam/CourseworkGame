package logic;

import java.awt.Color;
import java.awt.Graphics;

import map.Cell;

public class Player extends Sprite {
	private Color color;
	private double baseVel;
	private Direction direction;
	private Cell currentCell;
	public Player(Cell startCell, double width, double height, double baseVel, Color color) {
		super(startCell.x, startCell.y, startCell.width, startCell.height, baseVel);
		this.color = color;
		this.baseVel = baseVel;
	}
	
	protected void setDirection(Direction direction) {
		this.direction = direction;
	}

	public void moveToAdjacentCell(Direction direction) {
		//TODO: need to do the movement shit
	}
	
	@Override
	public void update(double delta) {
		vel = baseVel;
		move(direction, delta);
	}
	@Override
	public void render(Graphics g) {
		g.setColor(color);
		g.fillRoundRect((int)x,  (int)y,  (int)width, (int)height, (int)(width/5), (int)(height/5));
	}
}
