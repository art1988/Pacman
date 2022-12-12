import javax.swing.*;

public class Pinky extends Ghost {
	public Pinky(int xFocus, int yFocus) {
		super(xFocus, yFocus);
		
		setImage(new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/pinkGhost.png"));
		setStrategy(new BlinkyStrategy()); // TODO: Change strategy
		setName("Pinky");
	}
}
