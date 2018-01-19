package map;

import java.awt.Color;
import java.awt.Graphics;

import logic.Entity;

//don't need to extend entity, a cell IS a checkpoint
public class Checkpoint extends Entity {
	
	public Checkpoint(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	@Override
	public void update(double delta) {
		
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.YELLOW);
		g.fillOval((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12));
		g.setColor(Color.BLACK);
		g.drawOval((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12));
	}
	
}
