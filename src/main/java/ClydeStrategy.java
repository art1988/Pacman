import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Moving strategy for orange ghost.
 * Randomly generates point(x,y) on the map and moves creature there.
 */
public class ClydeStrategy extends Strategy
{
    // (xGhostCoord, yGhostCoord) - 'start' point
    // (xPacmanCoord, yPacmanCoord) - 'finish' point
    @Override
    void launchWave(int xPacmanCoord, int yPacmanCoord, int xGhostCoord, int yGhostCoord)
    {
        int xRnd = ThreadLocalRandom.current().nextInt(MAP_W - 2),
            yRnd = ThreadLocalRandom.current().nextInt(MAP_H - 1);

        xPacmanCoord = xRnd;
        yPacmanCoord = yRnd;

        waveMap[yPacmanCoord][xPacmanCoord].setWaveValue(1);

        getCountOfIteration();

        for(int index = 1; index <= countOfIteration; index++) {
            valueOfWaveFront++;

            for(int y = 1; y < MAP_H - 2; y++) {
                for(int x = 1; x < MAP_W - 2; x++) {

                    if(waveMap[y][x].getWaveValue() == valueOfWaveFront && waveMap[y][x].isVisited() == false) {
                        increaseWaveValue(y+1, x);
                        increaseWaveValue(y-1, x);
                        increaseWaveValue(y, x+1);
                        increaseWaveValue(y, x-1);

                        waveMap[y][x].setVisited(true);
                    }
                }
            }
        }

        valueOfWaveFront = waveMap[yGhostCoord][xGhostCoord].getWaveValue();
    }

    @Override
    void layPath(int xGhostCoord, int yGhostCoord)
    {
        Direction presentDirection = Direction.NONE,
                  newDirection     = Direction.NONE;

        while(valueOfWaveFront > 1) {
            int x = xGhostCoord,
                y = yGhostCoord;

            if(waveMap[y][x].getWaveValue() == valueOfWaveFront) {
                valueOfWaveFront--;

                if(waveMap[y+1][x].getWaveValue() == valueOfWaveFront) { // Down
                    yGhostCoord++;
                    presentDirection = Direction.DOWN;
                } else if(waveMap[y-1][x].getWaveValue() == valueOfWaveFront) { // Up
                    yGhostCoord--;
                    presentDirection = Direction.UP;
                } else if(waveMap[y][x+1].getWaveValue() == valueOfWaveFront) { // Right
                    xGhostCoord++;
                    presentDirection = Direction.RIGHT;
                } else if(waveMap[y][x-1].getWaveValue() == valueOfWaveFront){ // Left
                    xGhostCoord--;
                    presentDirection = Direction.LEFT;
                }
            }

            // First step
            if(newDirection.equals(Direction.NONE)) {
                pointsOfTurn.put(new Point(x, y), presentDirection);
                newDirection = presentDirection;
            }

            if(presentDirection.equals(newDirection) == false) {
                pointsOfTurn.put(new Point(x, y), presentDirection);
                newDirection = presentDirection;
            }
            else
            {
                pointsOfTurn.put(new Point(x, y), presentDirection);
            }
        }
    }
}
