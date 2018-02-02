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
			//set allows for variable number of players (1 or 2, but could be extended to 3+ in future), rather than storing variables for each player
	private Color color;
	private double checkpointProportionOfCellDimensions;
	public Checkpoint(double x, double y, double width, double height, Color color, double checkpointProportionOfCellDimensions) {
		super(x, y, width, height);
		playersEncountered = new HashSet<Player>();
		this.color = color;
		this.checkpointProportionOfCellDimensions = checkpointProportionOfCellDimensions;
	}
	
	public boolean addEncounteredPlayer(Player player) {
		return playersEncountered.add(player);
	}

	@Override
	public void update(double delta) {
		
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(color);
		g.fillOval((int) (x+width*(1-checkpointProportionOfCellDimensions)/2), (int) (y+height*(1-checkpointProportionOfCellDimensions)/2), (int) (width*checkpointProportionOfCellDimensions), (int) (height*checkpointProportionOfCellDimensions));
		int numPlayersEncountered = playersEncountered.size();
		double startAngle = 0;
		double arcAngle = 360/(numPlayersEncountered+1);
		for(Player player : playersEncountered) {
			g.setColor(player.getColor());
			g.fillArc((int) (x+width*(1-checkpointProportionOfCellDimensions)/2), (int) (y+height*(1-checkpointProportionOfCellDimensions)/2), (int) (width*checkpointProportionOfCellDimensions), (int) (height*checkpointProportionOfCellDimensions), (int)startAngle, (int)arcAngle);
			startAngle += arcAngle;
		}
		g.setColor(Color.BLACK);
		g.drawOval((int) (x+width*(1-checkpointProportionOfCellDimensions)/2), (int) (y+height*(1-checkpointProportionOfCellDimensions)/2), (int) (width*checkpointProportionOfCellDimensions), (int) (height*checkpointProportionOfCellDimensions));
		g.setColor(lastColor);
	}
	
}
