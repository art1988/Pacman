import java.io.Serializable;

public abstract class Creature implements Serializable {
	private static final long serialVersionUID = 300l;

	private int xCorner, yCorner; // Left upper corner of image for representation
	private int xFocus,  yFocus;  // Center of image for moving
	private Direction dir; 		  // Direction of creature
	
	public Creature(int xFocus, int yFocus) {
		setXFocus(xFocus);
		setYFocus(yFocus);
	}
	
	public int getXFocus() {
		return xFocus;
	}
	
	public void setXFocus(int x) {
		xFocus = x;
	}
	
	public int getXCorner() {
		return xCorner;
	}
	
	public void setXCorner(int x) {
		xCorner = x;
	}
	
	public int getYCorner() {
		return yCorner;
	}
	
	public void setYCorner(int y) {
		yCorner = y;
	}
	
	public int getYFocus() {
		return yFocus;
	}
	
	public void setYFocus(int y) {
		yFocus = y;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	public void setDirection(Direction direction) {
		dir = direction;
	}
}
