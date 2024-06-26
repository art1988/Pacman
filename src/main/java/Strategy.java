import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedHashMap;

abstract class Strategy implements Const, Serializable
{
	private static final long serialVersionUID = 500l;

	/** Did we reach destination or still in process ? */
	boolean execution;
	
	int countOfIteration, valueOfWaveFront;
	
	CellOfRoute[][] waveMap;
	
	LinkedHashMap<Point, Direction> pointsOfTurn = new LinkedHashMap<>(); // Points where ghost will turn
	
	abstract void launchWave(int xPacmanCoord, int yPacmanCoord, int xGhostCoord, int yGhostCoord); // From pacman coords to ghost coords
	
	abstract void layPath(int xGhostCoord, int yGhostCoord); // From ghost coords to pacman coords
	
	void clearVariables() {
		countOfIteration = 0;
		valueOfWaveFront = 0;
		
		for(int y = 1; y < MAP_H - 2; y++) {
			for(int x = 1; x < MAP_W - 2; x++) {
				waveMap[y][x].setVisited(false);
				waveMap[y][x].setWaveValue(0);
			}
		}
	}

	void cleanPointsOfTurn() {
		pointsOfTurn.clear();
	}

	void setWaveMap(CellOfRoute[][] map) {
		waveMap = map;
	}

	protected void getCountOfIteration() {
		for(int y = 1; y < MAP_H - 1; y++)
			for(int x = 1; x < MAP_W - 1; x++)
				if(waveMap[y][x].getAccess() == true) countOfIteration++;
	}

	protected void increaseWaveValue(int y, int x) {
		if(waveMap[y][x].getAccess() == true && waveMap[y][x].isVisited() == false)
			waveMap[y][x].setWaveValue(valueOfWaveFront + 1);
	}
}