import java.awt.*;

public class Clyde extends Ghost {
	public Clyde(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new SerializableImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/main/resources/orangeGhost.png")));
		setStrategy(new BlinkyStrategy());
		setName("Clyde");
	}
}
