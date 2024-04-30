import java.awt.*;

public class Blinky extends Ghost {
	public Blinky(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new SerializableImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/main/resources/redGhost.png")));
		setStrategy(new BlinkyStrategy());
		setName("Blinky");
	}
}
