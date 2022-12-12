import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class Level implements Serializable {
	private int[][]          mapOfWall;
	private CellOfRoute[][]  mapOfRoute;
	private Creature[]       creature;
	private String           name;
	
	public Level(int[][] walls, CellOfRoute[][] routes, Creature[] cr, String n) {
		mapOfWall = walls;
		mapOfRoute = routes;
		creature = cr;
		name = n;
	}
	
	public int[][] getMapOfWall() {
		return mapOfWall;
	}
	
	public CellOfRoute[][] getMapOfRoute() {
		return mapOfRoute;
	}
	
	public Creature[] getCreature() {
		return creature;
	}
	
	public String getMapName() {
		return name;
	}
}