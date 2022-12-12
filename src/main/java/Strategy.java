import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedHashMap;

abstract class Strategy implements Const, Serializable {
	boolean execution; // Did we reach destination or still in process ?
	
	int countOfIteration, valueOfWaveFront;
	
	CellOfRoute[][] waveMap;
	
	LinkedHashMap<Point, Direction> pointsOfTurn = new LinkedHashMap<Point, Direction>(); // Points where ghost will turn
	
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

	void setWaveMap(CellOfRoute[][] map) {
		waveMap = map;
	}
}