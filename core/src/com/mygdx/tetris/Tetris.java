package com.mygdx.tetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.File;
import java.util.Random;


public class Tetris extends ApplicationAdapter {
	private SpriteBatch batch;
    private BitmapFont font;
    protected static final int BOARD_HEIGHT = 27;
    protected static final int BOARD_WIDTH = 10;
    protected String[][] tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
    private int playerScore = 0;
    private int playerLevel = 1;
    private TetrisBlock blockCurrent = null;
    private TetrisBlock blockNext = null;
    private TetrisBlock storedBlock = null;
    private String nextBlockString = "";
    private boolean blockSet = false;
    private Random randBlock = new Random();
    private double gravity = 1;
    private int gravityModifier = 1;
    private long movementTimer;
    private long functionTimer;
    private long gravityTime;
    private boolean paused;
    private boolean startScreen;
    private boolean controlsScreen;
    private boolean gameOverScreen;
    private boolean swapped;
    private static Preferences prefs;
    private static HighScoreTable highScores;
    private int smallestHighScore;
    private String playerName;

    @Override
	public void create() {
        getPreferences();
        tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
        playerScore = 0;
        playerLevel = 1;
		batch = new SpriteBatch();
        FileHandle fontFile = new FileHandle(new File("fonts/novamono/novamono.fnt"));
        font = new BitmapFont(fontFile);
        font.setColor(Color.LIGHT_GRAY);
        fillBoard();
        movementTimer = System.currentTimeMillis();
        functionTimer = System.currentTimeMillis();
        gravityTime = System.currentTimeMillis();
        paused = false;
        startScreen = true;
        controlsScreen = false;
        gameOverScreen = false;
        swapped = false;
        assignTetrisBlocks();
	}

    public void restart() {
        prefs.putString("highscores", highScores.toString());
        prefs.flush();
        getPreferences();
        tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
        playerScore = 0;
        playerLevel = 1;
        fillBoard();
        movementTimer = System.currentTimeMillis();
        gravityTime = System.currentTimeMillis();
        paused = false;
        startScreen = false;
        controlsScreen = false;
        gameOverScreen = false;
        swapped = false;
        blockCurrent = null;
        blockNext = null;
        storedBlock = null;
        assignTetrisBlocks();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    private void exit() {
        prefs.putString("highscores", highScores.toString());
        prefs.flush();
        dispose();
        System.exit(0);
    }

    private void getPreferences() {
        prefs = Gdx.app.getPreferences("Tetris");
        if (prefs.contains("highscores")) {
            String highScoresList = prefs.getString("highscores");
            for (String highscore: highScoresList.split("\n")) {
                if (highscore.split(":").length == 2) {
                    highScores.put(highscore.split(":")[0], Integer.parseInt(highscore.split(":")[1]));
                } else {
                    highScores = new HighScoreTable();
                    smallestHighScore = 0;
                    return;
                }
            }
            highScores.sort();
            smallestHighScore = highScores.peek();
        } else {
            highScores = new HighScoreTable();
            smallestHighScore = 0;
        }
    }

    @Override
	public void render() {
        Gdx.gl.glClearColor(0.25F, 0.25F, 0.25F, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
        if (startScreen) {
            drawWelcome();
            if ((System.currentTimeMillis() - functionTimer) > 250) {
                if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                    startScreen = false;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.H)) {
                    startScreen = false;
                    controlsScreen = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    exit();
                }
            }
        } else if (controlsScreen) {
            if ((System.currentTimeMillis() - functionTimer) > 250) {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    startScreen = true;
                    controlsScreen = false;
                    functionTimer = System.currentTimeMillis();
                }
            }
            String controls = "UP - Rotate block clockwise\n" +
                              "LEFT/RIGHT - Move block\n" +
                              "DOWN - Speed up block\n" +
                              "SPACE - Drop block\n" +
                              "SHIFT - Store Block\n" +
                              "ESC - Pause/Unpause game\n\n" +
                              "Press ESC to return to the main menu.";
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, "Controls", 0, Gdx.graphics.getHeight() - 20,
                               Gdx.graphics.getWidth(), HAlignment.CENTER);
            font.setScale(1F, 1F);
            font.drawMultiLine(batch, controls, 0, Gdx.graphics.getHeight() - 100,
                               Gdx.graphics.getWidth(), HAlignment.CENTER);
        } else if (paused) {
            if ((System.currentTimeMillis() - functionTimer) > 250) {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    paused = false;
                    functionTimer = System.currentTimeMillis();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    exit();
                }
            }
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, "PAUSED\n\nPush Escape to unpause\n\nPush Q to Quit", 0,
                    Gdx.graphics.getHeight() * 0.70F, Gdx.graphics.getWidth(),
                    HAlignment.CENTER);
        } else if (gameOverScreen) {
            String gameOver = "Game Over!\nYou scored: " + playerScore +
                              "\n\nPush R to Restart" +
                              "\n\nPush Q to Quit";
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, gameOver, 0, Gdx.graphics.getHeight()*0.70F,
                               Gdx.graphics.getWidth(), HAlignment.CENTER);
            if ((System.currentTimeMillis() - functionTimer) > 250) {
                if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    exit();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.R)) {
                    restart();
                }
            }
        } else {
            gameLoop();
        }
		batch.end();
	}

    private void addToHighScores(String name) {
        highScores.put(name, playerScore);
    }

    private void gameLoop() {
        drawHeading();
        drawBoard();
        drawUI();

        if ((System.currentTimeMillis() - gravityTime) > (800 / (gravity * gravityModifier))) {
            doGravity();
        }
        if (playerScore > playerLevel * 1000) {
            playerLevel++;
            gravity += 0.25;
        }

        handleInput();

        if (blockSet) {
            if (tetrisGrid[2][4].equals("[]")) {
                if (playerScore > smallestHighScore) {
                    HighScoreInputListener listener = new HighScoreInputListener();
                    Gdx.input.getTextInput(listener, "You've got a high score!", playerName);
                }
                gameOverScreen = true;
            }
            blockSet = false;
            checkCompleteRows();
        }
    }

    private void hardDrop() {
        while (!blockSet) {
            doGravity();
            playerScore += 1;
        }
    }

    private void checkCompleteRows() {
        clearCurrentBlock();
        int rowsRemoved = 0;
        for (int i = BOARD_HEIGHT - 1; i > 0; i--) {
            if (checkFullRow(tetrisGrid[i].clone())) {
                eraseRow(i);
                rowsRemoved++;
                playerScore += (50 * rowsRemoved);
            }
        }
        drawBoard();
        for (int i = 0; i < rowsRemoved; i++) {
            for (int k = BOARD_HEIGHT - 1; k > 0; k--) {
                if (checkEmptyRow(tetrisGrid[k].clone())) {
                    tetrisGrid[k] = tetrisGrid[k - 1].clone();
                    eraseRow(k - 1);
                }
                drawBoard();
            }
        }
        updateBlockPosition();
    }

    private boolean checkFullRow(String[] row) {
        for (String rowItem: row) {
            if (!rowItem.equals("[]")) {
                return false;
            }
        }
        return true;
    }

    private boolean checkEmptyRow(String[] row) {
        for (String rowItem: row) {
            if (rowItem.equals("[]") || rowItem.equals("==")) {
                return false;
            }
        }
        return true;
    }

    private void handleInput() {
        if ((System.currentTimeMillis() - movementTimer) > 120 && !blockSet) {
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                gravityModifier = 10;
            } else {
                gravityModifier = 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !blockSet) {
                moveBlockLeft();
                movementTimer = System.currentTimeMillis();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !blockSet) {
                moveBlockRight();
                movementTimer = System.currentTimeMillis();
            }
        }
        if ((System.currentTimeMillis() - functionTimer) > 175 && !blockSet) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && ! swapped) {
                swapped = true;
                storeBlock();
                functionTimer = System.currentTimeMillis();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                hardDrop();
                functionTimer = System.currentTimeMillis();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                paused = true;
                functionTimer = System.currentTimeMillis();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                doRotate();
                functionTimer = System.currentTimeMillis();
            }
        }

    }

    private void storeBlock() {
        clearCurrentBlock();
        if (storedBlock == null) {
            storedBlock = blockCurrent.cloneBlock();
            storedBlock.setCenter(new int[] {2, 4});
            blockCurrent = blockNext.cloneBlock();
            blockNext = new TetrisBlock(randBlock.nextInt(7));
        } else {
            TetrisBlock temp = blockCurrent.cloneBlock();
            blockCurrent = storedBlock.cloneBlock();
            blockCurrent.setCenter(new int[] {2, 4});
            storedBlock = temp.cloneBlock();
            storedBlock.setCenter(new int[] {2, 4});
        }
        updateBlockPosition();
        functionTimer = System.currentTimeMillis();
    }

    private boolean canRotate() {
        int[][] nextRotation = blockCurrent.getNextRotation();
        for (int[] nextRotationCoord: nextRotation) {
            try {
                if (nextRotationCoord[0] < 0 || nextRotationCoord[0] > BOARD_HEIGHT ||
                        nextRotationCoord[1] < 0 || nextRotationCoord[1] > BOARD_WIDTH ||
                        tetrisGrid[nextRotationCoord[0]][nextRotationCoord[1]].equals("[]") ||
                        tetrisGrid[nextRotationCoord[0]][nextRotationCoord[1]].equals("==")) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void doRotate() {
        clearCurrentBlock();
        if (canRotate()) {
            blockCurrent.rotateBlock();
        }
        updateBlockPosition();
    }

    private void updateBlockPosition() {
        for (int[] blockCoords : blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = "[]";
        }
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]] = "[]";
        if (! blockCurrent.isPlaced()) {
            blockCurrent.place();
        }
    }

    private void clearCurrentBlock() {
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]] = ". ";
        for (int[] blockCoords: blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = ". ";
        }
    }

    private void updateBlockPosition(int[] newCenter) {
        blockCurrent.setCenter(newCenter.clone());
        updateBlockPosition();
    }

    private void moveBlockLeft() {
        clearCurrentBlock();
        for (int[] block: blockCurrent.getBlockArray()) {
            try {
                int[] center = blockCurrent.getCenter();
                if (tetrisGrid[block[0]][block[1] - 1].equals("[]") ||
                    tetrisGrid[center[0]][center[1] - 1].equals("[]") ||
                    tetrisGrid[block[0]][block[1] - 1].equals("==")) {
                    updateBlockPosition();
                    return;
                }
            } catch (Exception e) {
                updateBlockPosition();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                            blockCurrent.getCenter()[1] - 1});
    }

    private void moveBlockRight() {
        clearCurrentBlock();
        for (int[] block: blockCurrent.getBlockArray()) {
            try {
                int[] center = blockCurrent.getCenter();
                if (tetrisGrid[block[0]][block[1] + 1].equals("[]") ||
                    tetrisGrid[center[0]][center[1] + 1].equals("[]") ||
                    tetrisGrid[block[0]][block[1] + 1].equals("==")) {
                    updateBlockPosition();
                    return;
                }
            } catch (Exception e) {
                updateBlockPosition();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                            blockCurrent.getCenter()[1] + 1});
    }

    private void doGravity() {
        clearCurrentBlock();
        int[] center = blockCurrent.getCenter();
        if (tetrisGrid[center[0] + 1][center[1]].equals("[]") ||
            tetrisGrid[center[0] + 1][center[1]].equals("==")) {
            updateBlockPosition();
            setBlock();
            return;
        }
        for (int[] block : blockCurrent.getBlockArray()) {
            if (tetrisGrid[block[0] + 1][block[1]].equals("[]") ||
                tetrisGrid[block[0] + 1][block[1]].equals("==") || blockSet) {
                updateBlockPosition();
                setBlock();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0] + 1,
                            blockCurrent.getCenter()[1]});

        gravityTime = System.currentTimeMillis();
    }

    private void setBlock() {
        blockSet = true;
        blockCurrent = blockNext.cloneBlock();
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        playerScore += 25;
        swapped = false;
    }

    private void drawHeading() {
        font.setScale(2F, 2F);
        font.drawMultiLine(batch, "TETRIS", 0, Gdx.graphics.getHeight() - 20,
                Gdx.graphics.getWidth() * 0.60F, HAlignment.CENTER);
    }

    private TextBounds drawWelcome() {
        font.setScale(2F, 2F);
        font.drawMultiLine(batch, "TETRIS", 0, Gdx.graphics.getHeight() - 20,
                           Gdx.graphics.getWidth(), HAlignment.CENTER);

        font.setScale(1.5F, 1.5F);
        String welcomeText = "Welcome.\n" +
                             "This is tetris.\n\n" +
                             "Press Enter to start.\n\n" +
                             "Press H to see the controls.\n\n" +
                             "Push Q to Quit";
        TextBounds welcomeBounds = font.getMultiLineBounds(welcomeText);
        font.drawMultiLine(batch, welcomeText, 0, Gdx.graphics.getHeight() - 100,
                           Gdx.graphics.getWidth(), HAlignment.CENTER);
        return welcomeBounds;
    }

    private void drawBoard() {
        font.setScale(1F, 1F);
        String board = getBoardAsString();
        font.drawMultiLine(batch, board, 0, Gdx.graphics.getHeight()*0.90F,
                           Gdx.graphics.getWidth()*0.60F, HAlignment.CENTER);
    }

    private void drawUI() {
        int LEFTMOST_BORDER = 500;
        String storedBlockString = "";

        if (!nextBlockString.equals("") || ! blockNext.toString().equals(nextBlockString)) {
            nextBlockString = blockNext.toString();
        }
        if (storedBlock != null) {
            storedBlockString = storedBlock.toString();
        }

        String score = "Score: " + playerScore;
        font.draw(batch, score, LEFTMOST_BORDER, 725);

        String level = "Level: " + playerLevel;
        font.draw(batch, level, LEFTMOST_BORDER, 700);

        String nextPiece = "Next Piece:";
        font.drawMultiLine(batch, nextPiece, LEFTMOST_BORDER, 675);
        font.drawMultiLine(batch, nextBlockString, LEFTMOST_BORDER + 50, 625);

        String storedPiece = "Stored Piece:";
        font.drawMultiLine(batch, storedPiece, LEFTMOST_BORDER, 525);
        font.drawMultiLine(batch, storedBlockString, LEFTMOST_BORDER + 50, 475);

        String highScore = "High Scores:";
        font.drawMultiLine(batch, highScore, LEFTMOST_BORDER, 375);
        font.drawMultiLine(batch, highScores.toString(), LEFTMOST_BORDER + 50, 300);
    }

    private void fillBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (i == BOARD_HEIGHT - 1) {
                    tetrisGrid[i][j] = "==";
                } else {
                    tetrisGrid[i][j] = ". ";
                }
            }
        }
    }

    private void eraseRow(int rowIndex) {
        for (int i = 0; i < tetrisGrid[rowIndex].length; i++) {
            tetrisGrid[rowIndex][i] = ". ";
        }
    }

    private String getBoardAsString() {
        String boardAsString = "";
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            boardAsString += "<|";
            for (int j = 0; j < BOARD_WIDTH; j++) {
                boardAsString += tetrisGrid[i][j];

                if (j == BOARD_WIDTH - 1) {
                    boardAsString += "|>\n";
                }
            }
        }
        return boardAsString;
    }

    private void assignTetrisBlocks() {
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        blockCurrent = new TetrisBlock(randBlock.nextInt(7));
    }

    public class HighScoreInputListener implements Input.TextInputListener {
        @Override
        public void input (String text) {
            addToHighScores(text);
        }

        @Override
        public void canceled () {
            playerName = "Anon";
            addToHighScores(playerName);
        }
    }
}
