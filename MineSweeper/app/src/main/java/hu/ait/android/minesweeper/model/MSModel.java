package hu.ait.android.minesweeper.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * MODEL OF MINESWEEPER GAME
 * <p/>
 * - It is implemented as a 2-d array, with top-left corner starting as index 0.
 * - Each cell can either be empty, flag, or number. Initially they all start out as empty,
 * and changes according to user's input.
 * - The place of bombs are kept track in a separate list
 * - When 0 is clicked, all the neighboring 0s are also expanded.
 */
public class MSModel {

    private static MSModel instance = null; // Single instance

    private short[][] model;                // Index starts from 0
    private int[] bombs;                    // Array of location of bombs
    private int sizeX, sizeY;               // Size of the gameboard
    private int remainingBombCount = -1;      // Keeps track of number of remaining bombs

    private final double BOMB_RATIO = 0.2; // Ratio of bombs out of all the cells
    private Random rand;                    // Random Generator


    public static final short EMPTY = -1;   // Constants to represent cell values
    public static final short FLAG = -2;

    private Queue queue;                    // Queue used to expand when 0 is clicked
    private ArrayList<Coordinate> visited;  // Arraylist used to keep track of visited cells while expanding

    private MSModel() {
    }

    // Returns the current instance
    public static MSModel getInstance() {
        if (instance == null) {
            instance = new MSModel();
        }
        return instance;
    }

    //Sets up a new game
    public void setupGame(int width, int height) {
        initializeVariables(width, height);
        initializeBoard();
        remainingBombCount = (int) (sizeX * sizeY * BOMB_RATIO);
    }

    // Initializes all the necessary variables
    private void initializeVariables(int width, int height) {
        rand = new Random();
        sizeX = width;
        sizeY = height;
        model = new short[sizeX][sizeY];
        queue = new LinkedList();
        visited = new ArrayList<>();
    }

    // Fills the board as all empty
    private void initializeBoard() {
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                model[i][j] = EMPTY;
            }
        }
    }

    //Depending on size of the board, create necessary amount of bombs,
    //add them to the array. Only called after first click.
    //Note : Random location should not include the location of first click
    //       Also, there is no duplicate values
    public void setupBombs(int xPos, int yPos) {
        int numBombs = (int) (sizeX * sizeY * BOMB_RATIO);
        boolean duplicateVal = true;
        bombs = new int[numBombs];
        int firstClickLoc = gridToLinear(xPos, yPos);

        addUniqueBombs(numBombs, duplicateVal, firstClickLoc);
    }

    // Add bombs to unique locations
    private void addUniqueBombs(int numBombs, boolean duplicateVal, int firstClickLoc) {
        for (int i = 0; i < numBombs; i++) {
            int x = 0;
            while (duplicateVal) {
                x = rand.nextInt(sizeX * sizeY);
                for (int j = 0; j <= i; j++) {
                    if (j == i && x != firstClickLoc) {
                        duplicateVal = false;
                        break;
                    }
                    if (x == bombs[j]) {
                        break;
                    }
                }
            }
            bombs[i] = x;
            duplicateVal = true;
        }
    }


    // When user clicks
    // 1. Bomb : game is lost
    // 2. Empty space : number is evaluated
    // 3. None of above : nothing is performed
    public boolean click(int x, int y) {
        if (model[x][y] == EMPTY) {
            if (bombIsAt(x, y))
                return false;
            short number = computeMineCount(x, y);
            model[x][y] = number;
            if (number == 0)
                expand(x, y);
        }
        return true;
    }

    // Attempts to place/remove flag on clicked cell
    public boolean clickFlag(int x, int y) {
        if (model[x][y] == EMPTY) {
            return addFlag(x, y);
        } else if (model[x][y] == FLAG) {
            return removeFlag(x, y);
        }
        return true;
    }

    // Returns true if the operation was successful
    // Returns false if one placed flag on non-bomb spot
    private boolean addFlag(int x, int y) {
        if (bombIsAt(x, y)) {
            model[x][y] = FLAG;
            remainingBombCount--;
            return true;
        } else {
            return false;
        }
    }

    // Remove flag from the cell
    private boolean removeFlag(int x, int y) {
        model[x][y] = EMPTY;
        remainingBombCount++;
        return true;
    }


    //Assumption : we compute this just for numbered cells
    // (No need to exclude case when i=0, j=0)
    public short computeMineCount(int x, int y) {
        short number = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (withinBoundary(x + i, y + j)) {
                    if (bombIsAt(x + i, y + j)) {
                        number++;
                    }
                }
            }
        }
        return number;
    }


    // Private class to represent a coordinate in the game area
    private class Coordinate {
        int xPos, yPos;

        public Coordinate(int x, int y) {
            xPos = x;
            yPos = y;
        }

        public int getxPos() {
            return xPos;
        }
        public int getyPos() {
            return yPos;
        }
    }


    //When 0 is clicked, expands through all the 0s nearby
    /*  1. Add a coordinate to a queue and arraylist
        2. For each coordinate surrounding that coordinate, check the minecount.
           If 0, add that coordianate to queue and arraylist.
           If just number, add to arraylist and keep moving.
        3. Remove another coordinate from queue and repeat #2.
        4. Continue until queue is empty
    */
    private void expand(int xPos, int yPos) {
        queue.add(new Coordinate(xPos, yPos));
        visited.add(new Coordinate(xPos, yPos));

        while (!queue.isEmpty()) {
            Coordinate current = (Coordinate) queue.remove();
            int curXPos = current.getxPos();
            int curYPos = current.getyPos();

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newXPos = curXPos + i;
                    int newYPos = curYPos + j;

                    checkExpansion(newXPos, newYPos);
                }
            }
            model[current.getxPos()][current.getyPos()] = 0;
        }
    }

    // For each given coordinate, check if the minecount is 0 or number
    // If 0, expand. If number, show it on the field.
    private void checkExpansion(int newXPos, int newYPos) {
        if (withinBoundary(newXPos, newYPos)) {
            for (int iter = 0; iter <= visited.size(); iter++) {
                if (iter == visited.size()) {
                    short num = computeMineCount(newXPos, newYPos);

                    if (num == 0) {
                        queue.add(new Coordinate(newXPos, newYPos));
                    } else {
                        model[newXPos][newYPos] = num;
                    }
                    visited.add(new Coordinate(newXPos, newYPos));
                    break;
                }

                if (visited.get(iter).getxPos() == newXPos
                        && visited.get(iter).getyPos() == newYPos) {
                    break;
                }
            }
        }
    }

    private boolean withinBoundary(int x, int y) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY);
    }

    public boolean bombIsAt(int x, int y) {
        for (int bomb : bombs) {
            if (gridToLinear(x, y) == bomb) {
                return true;
            }
        }
        return false;
    }

    public int getRemainingBombCount() {
        return remainingBombCount;
    }

    public boolean gameWon() {
        return remainingBombCount == 0;
    }

    public short getCellContent(int x, int y) {
        return model[x][y];
    }

    // Helper function that converts 2-d index to linear index
    private int gridToLinear(int x, int y) {
        return x + y * sizeX;
    }

    // Start a new game with current dimensions
    public void resetModel() {
        setupGame(sizeX, sizeY);
        visited.clear();
    }

    // Start a new game with given dimensions
    public void resetModel(int x, int y) {
        setupGame(x, y);
        visited.clear();
    }
}
