package user_interface;
import java.awt.Graphics;
import java.awt.event.KeyListener;

public interface Screen extends KeyListener{
	public void enter();
	public void leave();
	public void update(double delta);
	public void render(Graphics g);
}
