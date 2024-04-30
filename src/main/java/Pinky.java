import java.awt.*;

public class Pinky extends Ghost {
	public Pinky(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new SerializableImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/main/resources/pinkGhost.png")));
		setStrategy(new BlinkyStrategy()); // TODO: Change strategy
		setName("Pinky");
	}
}
