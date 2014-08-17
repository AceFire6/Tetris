package com.mygdx.tetris;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Date;
import java.util.Random;

/**
 * @author Jethro Muller - MLLJET001
 * @version 1.0.0
 *
 * Tetris clone for Game Development course(CSC2003S, UCT).
 */

public class Tetris extends ApplicationAdapter {
    protected static final int BOARD_HEIGHT = 27;
    protected static final int BOARD_WIDTH = 10;
    /**
     * The 2D array that represents the tetris board.
     */
    protected String[][] tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
    private static Preferences prefs;
    private static HighScoreTable highScores;
    private SpriteBatch batch;
    private BitmapFont font;
    private int playerScore = 0;
    private int playerLevel = 1;
    private TetrisBlock blockCurrent = null;
    private TetrisBlock blockNext = null;
    private TetrisBlock storedBlock = null;
    private String nextBlockString = "";
    /**
     * Whether or not the block has landed and can no longer be moved.
     */
    private boolean blockSet = false;
    private Random randBlock = new Random();
    private double gravity = 1;
    private int gravityModifier = 1;
    private long movementTimer;
    private long gravityTimer;
    private boolean paused;
    private boolean startScreen;
    private boolean controlsScreen;
    private boolean gameOverScreen;
    private boolean inGame;
    /**
     * Whether or not the block has been switched this cycle.
     */
    private boolean swapped;
    private int lowestHighScore;
    private String playerName;
    private Sound lineClearSound;
    private Sound rotateSound;
    private Music backgroundMusic;
    /**
     * Whether or not the sound effects are muted.
     */
    private boolean soundsMuted;
    /**
     * The tetris heading with added colour.
     */
    private String colourTetris = "[RED]T[CYAN]E[MAGENTA]T[ORANGE]R[BLUE]I[GREEN]S[LIGHT_GRAY]";

    @Override
    public void create() {
        rotateSound = Gdx.audio.newSound(Gdx.files.internal("sound/button-press.mp3"));
        lineClearSound = Gdx.audio.newSound(Gdx.files.internal("sound/beep.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/chiptune_does_dubstep.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.8F);
        backgroundMusic.play();
        soundsMuted = false;
        batch = new SpriteBatch();
        FileHandle fontFile = Gdx.files.getFileHandle("font/novamono.fnt", Files.FileType.Local);
        font = new BitmapFont(fontFile);
        font.setMarkupEnabled(true);
        font.setColor(Color.LIGHT_GRAY);
        setInputHandler();

        start();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.25F, 0.25F, 0.25F, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        if (startScreen) { // The main menu
            drawWelcome();
        } else if (controlsScreen) { // The controls screen
            String controls = "        [RED]UP[] - Rotate block clockwise\n" +
                              "[RED]LEFT/RIGHT[] - Move block\n" +
                              "      [RED]DOWN[] - Speed up block\n" +
                              "     [RED]SPACE[] - Drop block\n" +
                              "     [RED]SHIFT[] - Store Block\n" +
                              "         [RED]M[] - Mute Music\n" +
                              "         [RED]N[] - Mute Sounds\n" +
                              "    [RED]Escape[] - Pause/Unpause game";
            String finalControl = "[RED]Escape[] - Main Menu";
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, "[CYAN]Controls[]", 0, Gdx.graphics.getHeight() -
                                                     20, Gdx.graphics.getWidth(),
                               HAlignment.CENTER);
            font.setScale(1F, 1F);
            font.drawMultiLine(batch, controls, 130, Gdx.graphics.getHeight() - 100);
            font.drawMultiLine(batch, finalControl, 0, Gdx.graphics.getHeight() -
                                                   350, Gdx.graphics.getWidth(), HAlignment.CENTER);
        } else if (paused) { // When the player pauses the game
            String pausedHeading = "[CYAN]PAUSED[]\n\n";
            String pausedText = "[RED]Escape[] - Unpause\n" +
                                "     [RED]R[] - Restart\n" +
                                "     [RED]Q[] - Main Menu";

            font.setScale(2F, 2F);
            font.drawWrapped(batch, pausedHeading, 0, Gdx.graphics.getHeight() *
                                                     0.90F, Gdx.graphics.getWidth(),
                               HAlignment.CENTER);
            font.setScale(1.5F, 1.5F);
            font.drawMultiLine(batch, pausedText, 175, Gdx.graphics.getHeight() * 0.80F);
        } else if (gameOverScreen) { // When the player loses
            String gameOver = "[RED]Game Over![]" +
                              "\nYou Scored: " + playerScore;
            String gameOverControls = "[RED]R[] - Restart" +
                                     "\n[RED]Q[] - Quit";
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, gameOver, 0, Gdx.graphics.getHeight() *
                                                   0.80F, Gdx.graphics.getWidth(),
                               HAlignment.CENTER);
            font.drawMultiLine(batch, gameOverControls, 250, Gdx.graphics.getHeight() * 0.60F);
        } else if (inGame) { // When the player is in game
            gameLoop();
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        lineClearSound.dispose();
        rotateSound.dispose();
        backgroundMusic.dispose();
    }

    /**
     * Initializes all the necessary variables.
     * Is used to restart the game.
     */
    public void start() {
        if (prefs != null) {
            prefs.putString("highscores", highScores.getAsString());
            prefs.flush();
        }

        highScores = new HighScoreTable();
        tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
        playerScore = 0;
        playerLevel = 1;
        getPreferences();
        tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
        fillBoard();
        gravityModifier = 1;
        gravity = 1;

        startScreen = true;
        if (gameOverScreen || paused) {
            paused = false;
            startScreen = false;
            controlsScreen = false;
            gameOverScreen = false;
            inGame = true;
        }
        blockSet = false;
        swapped = false;
        blockCurrent = null;
        blockNext = null;
        storedBlock = null;
        assignTetrisBlocks();
        resetTimers();
    }

    /**
     * Sets the input handler to one that has all the necessary input handling.
     * Handles the input for all events that happen with a single key press.
     * Like rotating or dropping the block.
     *
     * Handles the input of this type for all game states.
     */
    private void setInputHandler() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.M) {
                    if (backgroundMusic.getVolume() > 0F) {
                        backgroundMusic.setVolume(0);
                    } else {
                        backgroundMusic.setVolume(0.8F);
                    }
                    return true;
                }

                if (keycode == Input.Keys.N) {
                    soundsMuted = !soundsMuted;
                    return true;
                }

                if (inGame) {
                    if (keycode == Input.Keys.SPACE) {
                        hardDrop();
                        setBlock();
                        return true;
                    } else if (keycode == Input.Keys.SHIFT_LEFT) {
                        if (!swapped) {
                            swapped = true;
                            storeBlock();
                            return true;
                        }
                    } else if (keycode == Input.Keys.ESCAPE) {
                        inGame = false;
                        paused = true;
                        return true;
                    } else if (keycode == Input.Keys.UP) {
                        doRotate();
                        return true;
                    }
                } else if (startScreen) {
                    if (keycode == Input.Keys.ENTER) {
                        start();
                        startScreen = false;
                        inGame = true;
                        return true;
                    } else if (keycode == Input.Keys.H) {
                        startScreen = false;
                        controlsScreen = true;
                        return true;
                    } else if (keycode == Input.Keys.C) {
                        highScores = new HighScoreTable();
                        prefs.clear();
                        prefs.flush();
                        return true;
                    } else if (keycode == Input.Keys.Q) {
                        exit();
                        return true;
                    }
                } else if (paused) {
                    if (keycode == Input.Keys.ESCAPE) {
                        paused = false;
                        inGame = true;
                        return true;
                    } else if (keycode == Input.Keys.Q) {
                        paused = false;
                        startScreen = true;
                        return true;
                    } else if (keycode == Input.Keys.R) {
                        start();
                        return true;
                    }
                } else if (controlsScreen) {
                    if (keycode == Input.Keys.ESCAPE) {
                        startScreen = true;
                        controlsScreen = false;
                        return true;
                    }
                } else if (gameOverScreen) {
                    if (keycode == Input.Keys.Q) {
                        exit();
                        return true;
                    }
                    if (keycode == Input.Keys.R) {
                        start();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Resets the key-press timers.
     */
    private void resetTimers() {
        movementTimer = System.currentTimeMillis();
        gravityTimer = System.currentTimeMillis();
    }

    /**
     * Handles exiting the game. Outputs the current highscores using Gdx's built in
     * settings-file handler.
     *
     * Makes the proper Gdx exit call.
     */
    private void exit() {
        prefs.putString("highscores", highScores.getAsString());
        prefs.flush();
        Gdx.app.exit();
    }

    /**
     * Reads the highscores in from the local filesystem.
     *
     * If any of the highscores are malformed, it resets the local highscores.
     */
    private void getPreferences() {
        prefs = Gdx.app.getPreferences("Tetris");
        if (prefs.contains("highscores")) {
            String highScoresList = prefs.getString("highscores");
            for (String highscore : highScoresList.split("\n")) {
                if (highscore.split(": ").length == 2) {
                    try {
                        highScores.add(highscore);
                    } catch (NullPointerException npe) {
                        prefs.clear();
                        getPreferences();
                        return;
                    }
                } else {
                    highScores = new HighScoreTable();
                    lowestHighScore = 0;
                    return;
                }
            }
            lowestHighScore = highScores.peek();
        } else {
            highScores = new HighScoreTable();
            lowestHighScore = 0;
        }
    }

    /**
     * Adds the player to the high score list.
     * @param name    The name the player entered.
     */
    private void addToHighScores(String name) {
        highScores.addScore(name, playerScore, new Date());
    }

    /**
     * The main game loop that handles all the ui drawing and timed events as well
     * as the movement controls.
     */
    private void gameLoop() {
        drawHeading();
        drawBoard();
        drawUI();

        if ((System.currentTimeMillis() - gravityTimer) > (800 / (gravity * gravityModifier))) {
            if (!doGravity())  {
                setBlock();
            }
        }
        if (playerScore > playerLevel * 1000) {
            playerLevel++;
            gravity += 0.3;
        }

        handleInput();

        if (blockSet) {
            if (isCollision(0, 0)) {
                lowestHighScore = highScores.peek();
                if (playerScore > lowestHighScore || highScores.size() < 5) {
                    HighScoreInputListener listener = new HighScoreInputListener();
                    Gdx.input.getTextInput(listener, "You've got a high score!", playerName);
                }
                inGame = false;
                gameOverScreen = true;
            } else {
                blockSet = false;
            }
            checkCompleteRows();
        }
    }

    /**
     * Drops the block until it can't go any further.
     * Adds one to the player's score for each block gone through.
     */
    private void hardDrop() {
        while (doGravity()) {
            playerScore += 1;
        }
    }

    /**
     * Checks the tetris grid for any rows that had been completed upon the block becoming set.
     * If there are any rows that have been completed, they are cleared and the rows above them
     * are shifted down.
     */
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
        for (int j = 0; j < rowsRemoved; j++) {
            if (!soundsMuted) {
                lineClearSound.play();
            } else {
                break;
            }
        }
        for (int i = 0; i < rowsRemoved; i++) {
            for (int k = BOARD_HEIGHT - 1; k > 0; k--) {
                if (checkEmptyRow(tetrisGrid[k].clone())) {
                    tetrisGrid[k] = tetrisGrid[k - 1].clone();
                    eraseRow(k - 1);
                }
            }
        }
        updateBlockPosition();
        drawBoard();
    }

    /**
     * Checks if the given row is filled.
     * @param row    The row to be checked.
     * @return boolean Whether or not the row was full.
     */
    private boolean checkFullRow(String[] row) {
        for (String rowItem : row) {
            if (! rowItem.contains("[[]")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the row is empty.
     * @param row    The row to be checked.
     * @return boolean Whether or not the row is empty.
     */
    private boolean checkEmptyRow(String[] row) {
        for (String rowItem : row) {
            if (rowItem.contains("[[]") || rowItem.equals("==")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Handles movement input. The input that can be held down.
     */
    private void handleInput() {
        if ((System.currentTimeMillis() - movementTimer) > 120 && ! blockSet) {
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                gravityModifier = 10;
            } else {
                gravityModifier = 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && ! blockSet) {
                moveBlockLeft();
                movementTimer = System.currentTimeMillis();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && ! blockSet) {
                moveBlockRight();
                movementTimer = System.currentTimeMillis();
            }
        }
    }

    /**
     * Stores the currently active block for later use.
     * If there is already a block stored, it swaps the current block for the stored block.
     */
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
    }

    /**
     * Checks if the block can be rotated.
     * @return boolean Whether or not the block can be rotated.
     */
    private boolean canRotate() {
        int[][] nextRotation = blockCurrent.getNextRotation();
        for (int[] nextRotationCoord : nextRotation) {
            try {
                if (nextRotationCoord[0] < 0 || nextRotationCoord[0] > BOARD_HEIGHT ||
                    nextRotationCoord[1] < 0 || nextRotationCoord[1] > BOARD_WIDTH ||
                    tetrisGrid[nextRotationCoord[0]][nextRotationCoord[1]].contains("[[]") ||
                    tetrisGrid[nextRotationCoord[0]][nextRotationCoord[1]].equals("==")) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rotate the current block.
     */
    private void doRotate() {
        clearCurrentBlock();
        if (canRotate() && ! blockSet) {
            blockCurrent.rotateBlock();
            if (!soundsMuted) {
                rotateSound.play();
            }
        }
        updateBlockPosition();
    }

    /**
     * Updates the current blocks position in the tetris grid.
     */
    private void updateBlockPosition() {
        for (int[] blockCoords : blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = "[#" + blockCurrent.getBlockColour() +
                                                         "]" + "[[]" + "[]";
        }
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]]
                = "[#" + blockCurrent.getBlockColour() + "]" + "[[]" + "[]";
    }

    /**
     * Deletes the '[]' for the current block from the block grid to prevent collision detection
     * errors.
     */
    private void clearCurrentBlock() {
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]] = ". ";
        for (int[] blockCoords : blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = ". ";
        }
    }

    /**
     * Gives the current block a new center and then updates its position, thereby moving it.
     * @param newCenter The blocks new center.
     */
    private void updateBlockPosition(int[] newCenter) {
        blockCurrent.setCenter(newCenter.clone());
        updateBlockPosition();
    }

    /**
     * Moves the block to the left.
     */
    private void moveBlockLeft() {
        clearCurrentBlock();

        if (isCollision(0, - 1)) {
            updateBlockPosition();
            return;
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                                       blockCurrent.getCenter()[1] - 1});
    }

    /**
     * Moves the block to the right.
     */
    private void moveBlockRight() {
        clearCurrentBlock();

        if (isCollision(0, 1)) {
            updateBlockPosition();
            return;
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                                       blockCurrent.getCenter()[1] + 1});
    }

    /**
     * Moves the block down by one place if possible.
     * @return Whether or not there was a collision while moving the block down.
     */
    private boolean doGravity() {
        clearCurrentBlock();
        if (isCollision(1, 0)) {
            updateBlockPosition();
            return false;
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0] + 1,
                                       blockCurrent.getCenter()[1]});
        gravityTimer = System.currentTimeMillis();
        return true;
    }

    /**
     * Checks to see if there will be a collision if the block is moved according to the given
     * offsets.
     * @param xOffset The offset in the x-direction
     * @param yOffset The offset in the y-direction
     * @return boolean Whether or not there will be a collision.
     */
    private boolean isCollision(int xOffset, int yOffset) {
        int[] center = blockCurrent.getCenter().clone();
        try {
            if (tetrisGrid[center[0] + xOffset][center[1] + yOffset].contains("[[]") ||
                tetrisGrid[center[0] + xOffset][center[1] + yOffset].equals("==")) {
                return true;
            }
            for (int[] block : blockCurrent.getBlockArray()) {
                if (tetrisGrid[block[0] + xOffset][block[1] + yOffset].contains("[[]") ||
                    tetrisGrid[block[0] + xOffset][block[1] + yOffset].equals("==")) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ae) {
            return true;
        }
        return false;
    }

    /**
     * Sets the block in place, handles the assignment on the next block and the new next block.
     * Resets the swapped variable and adds to the score.
     */
    private void setBlock() {
        blockSet = true;
        blockCurrent = blockNext.cloneBlock();
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        playerScore += 25;
        swapped = false;

        if (!soundsMuted) {
            lineClearSound.play(1F, 1.5F, 0F);
        }
    }

    /**
     * Draws the heading during the game, above the grid.
     */
    private void drawHeading() {
        font.setScale(2F, 2F);
        font.drawMultiLine(batch, colourTetris, 0,
                           Gdx.graphics.getHeight() - 20,
                           Gdx.graphics.getWidth() * 0.60F, HAlignment.CENTER);
    }

    /**
     * Draws the main menu.
     */
    private void drawWelcome() {
        font.setScale(5F, 5F);
        font.drawMultiLine(batch, colourTetris, 0,
                           Gdx.graphics.getHeight() -
                                               100, Gdx.graphics.getWidth(), HAlignment.CENTER);

        String welcomeText = "[RED]Enter[] - Start";
        String homeControls = "[RED]H[] - Controls\n" +
                              "[RED]C[] - Clear High Scores\n" +
                              "[RED]M[] - Mute Music\n" +
                              "[RED]N[] - Mute Sounds\n" +
                              "[RED]Q[] - Quit";
        font.setScale(2.5F, 2.5F);
        font.drawMultiLine(batch, welcomeText, 0, Gdx.graphics.getHeight() -
                                                  250, Gdx.graphics.getWidth(), HAlignment.CENTER);
        font.setScale(1.5F, 1.5F);
        font.drawMultiLine(batch, homeControls, 200, Gdx.graphics.getHeight() - 350);
    }

    /**
     * Draws the tetris grid.
     */
    private void drawBoard() {
        font.setScale(1F, 1F);
        String board = getBoardAsString();
        font.drawMultiLine(batch, board, 0,
                           Gdx.graphics.getHeight() * 0.90F,
                           Gdx.graphics.getWidth() * 0.60F, HAlignment.CENTER);
    }

    /**
     * Draws all the UI elements on the right of the screen.
     */
    private void drawUI() {
        int LEFTMOST_BORDER = 450;
        String storedBlockString = "";

        if (! nextBlockString.equals("") || ! blockNext.toString().equals(nextBlockString)) {
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
        font.drawMultiLine(batch, nextPiece, LEFTMOST_BORDER, 650);
        font.drawMultiLine(batch, nextBlockString, LEFTMOST_BORDER + 50, 600);

        String storedPiece = "Stored Piece:";
        font.drawMultiLine(batch, storedPiece, LEFTMOST_BORDER, 475);
        font.drawMultiLine(batch, storedBlockString, LEFTMOST_BORDER + 50, 425);

        String highScore = "Top 5 High Scores:";
        font.drawMultiLine(batch, highScore, LEFTMOST_BORDER, 325);
        font.setScale(0.8F);
        font.drawMultiLine(batch, highScores.getAsString(), LEFTMOST_BORDER, 275);
    }

    /**
     * Fills the board so that it's blank when the player starts.
     *
     * The board is filled with '. 's everywhere except the last row, which is filled with '=='s.
     */
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

    /**
     * Erases the given row by replacing anything in the row cells with '. '.
     * @param rowIndex The row to be erased.
     */
    private void eraseRow(int rowIndex) {
        for (int i = 0; i < tetrisGrid[rowIndex].length; i++) {
            tetrisGrid[rowIndex][i] = ". ";
        }
    }

    /**
     * Returns the board as a string so it can be displayed.
     * @return String representation of the tetris grid.
     */
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

    /**
     * Assigns tetris blocks to the current and next block objects.
     */
    private void assignTetrisBlocks() {
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        blockCurrent = new TetrisBlock(randBlock.nextInt(7));
    }

    /**
     * Handles the highscore input. Formats the names to the appropriate length and does basic
     * sensitization on each name to prevent a name entry that wipes the highscore table.
     */
    public class HighScoreInputListener implements Input.TextInputListener {
        @Override
        public void input(String text) {
            text = text.trim();
            text = text.replaceAll(":", "");
            if (text.equals("")) {
                playerName = "Anon";
                highScores.addScore(playerName, playerScore, new Date());
            } else if (text.length() <= 6) {
                playerName = text;
                highScores.addScore(playerName, playerScore, new Date());
            } else {
                playerName = text.substring(0, 5);
                highScores.addScore(playerName, playerScore, new Date());
            }
        }

        @Override
        public void canceled() {
            playerName = "Anon";
            addToHighScores(playerName);
        }
    }
}
