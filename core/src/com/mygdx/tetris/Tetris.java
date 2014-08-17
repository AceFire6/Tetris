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


public class Tetris extends ApplicationAdapter {
    protected static final int BOARD_HEIGHT = 27;
    protected static final int BOARD_WIDTH = 10;
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
    private boolean swapped;
    private int smallestHighScore;
    private String playerName;
    private Sound lineClearSound;
    private Sound rotateSound;
    private Music bgMusic;
    private boolean soundsMuted;
    private String colorTetris = "[RED]T[CYAN]E[MAGENTA]T[ORANGE]R[BLUE]I[GREEN]S[LIGHT_GRAY]";

    @Override
    public void create() {
        rotateSound = Gdx.audio.newSound(Gdx.files.internal("sound/button-press.mp3"));
        lineClearSound = Gdx.audio.newSound(Gdx.files.internal("sound/beep.mp3"));
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/chiptune_does_dubstep.mp3"));
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.8F);
        bgMusic.play();
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
        if (startScreen) {
            drawWelcome();
        } else if (controlsScreen) {
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
        } else if (paused) {
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
        } else if (gameOverScreen) {
            String gameOver = "[RED]Game Over![]" +
                              "\nYou Scored: " + playerScore;
            String gameOverControls = "[RED]R[] - Restart" +
                                     "\n[RED]Q[] - Quit";
            font.setScale(2F, 2F);
            font.drawMultiLine(batch, gameOver, 0, Gdx.graphics.getHeight() *
                                                   0.80F, Gdx.graphics.getWidth(),
                               HAlignment.CENTER);
            font.drawMultiLine(batch, gameOverControls, 250, Gdx.graphics.getHeight() * 0.60F);
        } else if (inGame) {
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
        bgMusic.dispose();
    }

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

    private void setInputHandler() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.M) {
                    if (bgMusic.getVolume() > 0F) {
                        bgMusic.setVolume(0);
                    } else {
                        bgMusic.setVolume(0.8F);
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

    private void resetTimers() {
        movementTimer = System.currentTimeMillis();
        gravityTimer = System.currentTimeMillis();
    }

    private void exit() {
        prefs.putString("highscores", highScores.getAsString());
        prefs.flush();
        Gdx.app.exit();
    }

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
                    smallestHighScore = 0;
                    return;
                }
            }
            smallestHighScore = highScores.peek();
        } else {
            highScores = new HighScoreTable();
            smallestHighScore = 0;
        }
    }

    private void addToHighScores(String name) {
        highScores.addScore(name, playerScore, new Date());
    }

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
                smallestHighScore = highScores.peek();
                if (playerScore > smallestHighScore || highScores.size() < 5) {
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

    private void hardDrop() {
        while (doGravity()) {
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

    private boolean checkFullRow(String[] row) {
        for (String rowItem : row) {
            if (! rowItem.contains("[[]")) {
                return false;
            }
        }
        return true;
    }

    private boolean checkEmptyRow(String[] row) {
        for (String rowItem : row) {
            if (rowItem.contains("[[]") || rowItem.equals("==")) {
                return false;
            }
        }
        return true;
    }

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

    private void updateBlockPosition() {
        for (int[] blockCoords : blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = "[#" + blockCurrent.getBlockColour() +
                                                         "]" + "[[]" + "[]";
        }
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]]
                = "[#" + blockCurrent.getBlockColour() + "]" + "[[]" + "[]";
        if (! blockCurrent.isPlaced()) {
            blockCurrent.place();
        }
    }

    private void clearCurrentBlock() {
        tetrisGrid[blockCurrent.getCenter()[0]][blockCurrent.getCenter()[1]] = ". ";
        for (int[] blockCoords : blockCurrent.getBlockArray()) {
            tetrisGrid[blockCoords[0]][blockCoords[1]] = ". ";
        }
    }

    private void updateBlockPosition(int[] newCenter) {
        blockCurrent.setCenter(newCenter.clone());
        updateBlockPosition();
    }

    private void moveBlockLeft() {
        clearCurrentBlock();

        if (isCollision(0, - 1)) {
            updateBlockPosition();
            return;
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                                       blockCurrent.getCenter()[1] - 1});
    }

    private void moveBlockRight() {
        clearCurrentBlock();

        if (isCollision(0, 1)) {
            updateBlockPosition();
            return;
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0],
                                       blockCurrent.getCenter()[1] + 1});
    }

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

    private void setBlock() {
        blockSet = true;
        blockCurrent = blockNext.cloneBlock();
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        playerScore += 25;
        swapped = false;
        lineClearSound.play(1F, 1.5F, 0F);
    }

    private void drawHeading() {
        font.setScale(2F, 2F);
        font.drawMultiLine(batch, colorTetris, 0,
                           Gdx.graphics.getHeight() - 20,
                           Gdx.graphics.getWidth() * 0.60F, HAlignment.CENTER);
    }

    private void drawWelcome() {
        font.setScale(5F, 5F);
        font.drawMultiLine(batch, colorTetris, 0,
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

    private void drawBoard() {
        font.setScale(1F, 1F);
        String board = getBoardAsString();
        font.drawMultiLine(batch, board, 0,
                           Gdx.graphics.getHeight() * 0.90F,
                           Gdx.graphics.getWidth() * 0.60F, HAlignment.CENTER);
    }

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
