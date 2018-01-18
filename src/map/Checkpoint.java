package map;

import java.awt.Color;
import java.awt.Graphics;

import logic.Entity;

public class Checkpoint extends Entity {
	
	public Checkpoint(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	public void update(double delta) {
		
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.YELLOW);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
	}
	
}
