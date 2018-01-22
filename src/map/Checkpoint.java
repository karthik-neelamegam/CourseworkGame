package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.HashSet;
import java.util.Set;

import logic.Entity;
import logic.Player;

//don't need to extend entity, a cell IS a checkpoint
public class Checkpoint extends Entity {
	private Set<Player> playersEncountered; //polymorphism 
			//set allows for variable number of players, rather than storing variables for each player
	private Color color;
	public Checkpoint(double x, double y, double width, double height, Color color) {
		super(x, y, width, height);
		playersEncountered = new HashSet<Player>();
		this.color = color;
	}
	
	public boolean addEncounteredPlayer(Player player) {
		return playersEncountered.add(player);
	}

	@Override
	public void update(double delta) {
		
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.YELLOW);
		//Graphics2D g2d = (Graphics2D) g;
		//g2d.setColor(Color.YELLOW);
		//Arc2D.Double arc = new Arc2D.Double(x, y, width, height, 0.0, 270, Arc2D.OPEN);
		g.setColor(color);
		g.fillOval((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12));
		int numPlayersEncountered = playersEncountered.size();
		double startAngle = 0;
		double arcAngle = 360/(numPlayersEncountered+2);
		for(Player player : playersEncountered) {
			g.setColor(player.getColor());
			g.fillArc((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12), (int)startAngle, (int)arcAngle);
			startAngle += arcAngle;
		}
		g.setColor(Color.ORANGE);
		g.fillArc((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12), (int)startAngle, (int)arcAngle);
		startAngle += arcAngle;

		g.setColor(Color.BLACK);
		g.drawOval((int)(x+width/12), (int)(y+height/12), (int)(10*width/12), (int)(10*height/12));
	}
	
}
