package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import javax.swing.*;
import java.util.Random;


public class Tetris extends ApplicationAdapter {
	SpriteBatch batch;
    BitmapFont font;
    protected static final int BOARD_HEIGHT = 27;
    protected static final int BOARD_WIDTH = 10;
    protected String[][] tetrisGrid = new String[BOARD_HEIGHT][BOARD_WIDTH];
    private int playerScore = 0;
    private int playerLevel = 1;
    private TetrisBlock blockCurrent = null;
    private TetrisBlock blockNext = null;
    private String nextBlockString = "";
    private boolean blockSet = false;
    private Random randBlock = new Random();
    private int gravity = 1;
    private int gravityModifier = 1;
    private long oldTime;
    private long gravityTime;
    private boolean paused = false;
    private boolean startScreen = true;

    @Override
	public void create() {
		batch = new SpriteBatch();
        FileHandle fontFile = new FileHandle("fonts/novamono/novamono.fnt");
        font = new BitmapFont(fontFile);
        font.setColor(Color.LIGHT_GRAY);
        fillBoard();
        oldTime = System.currentTimeMillis();
        gravityTime = System.currentTimeMillis();
        assignTetrisBlocks();
	}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override
	public void render() {
        Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
        if (startScreen) {
            drawWelcome();
            if ((System.currentTimeMillis() - oldTime) > 150) {
                if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                    startScreen = false;
                }
            }
        } else if (paused) {
            if ((System.currentTimeMillis() - oldTime) > 500) {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    paused = false;
                    oldTime = System.currentTimeMillis();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    System.exit(0);
                }
            }
            font.setScale(2f, 2f);
            font.drawMultiLine(batch, "PAUSED\n\nPush Escape to unpause\n\nPush Q to Quit", 0,
                               Gdx.graphics.getHeight()*0.70F, Gdx.graphics.getWidth(),
                               HAlignment.CENTER);
        } else {
            gameLoop();
        }
		batch.end();
	}

    private void gameLoop() {
        if (playerScore > playerLevel * 700) {
            playerLevel++;
            gravity++;
        }
        if ((System.currentTimeMillis() - oldTime) > 150 && !blockSet) {
            handleInput();
        }
        if (blockSet) {
            if (tetrisGrid[2][4].equals("[]")) {
                JOptionPane.showMessageDialog(null, "Game Over!\nYou scored: " + playerScore);
                //TODO Have highscore logic here
                System.exit(0);
            }
            blockSet = false;
            checkCompleteRows();
        }
        drawHeading();
        drawBoard();
        drawUI();
        if ((System.currentTimeMillis() - gravityTime) > (800 / (gravity * gravityModifier)) &&
            !blockSet) {
            doGravity();
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
            if (checkFull(tetrisGrid[i].clone())) {
                eraseRow(i);
                rowsRemoved++;
                playerScore += (50 * rowsRemoved);
            }
        }

        for (int i = 0; i < rowsRemoved; i++) {
            for (int k = BOARD_HEIGHT - 1; k > 0; k--) {
                if (checkEmpty(tetrisGrid[k].clone())) {
                    tetrisGrid[k] = tetrisGrid[k - 1].clone();
                    eraseRow(k - 1);
                }
            }
        }
        updateBlockPosition();
    }

    private boolean checkFull(String[] row) {
        for (String rowItem: row) {
            if (!rowItem.equals("[]")) {
                return false;
            }
        }
        return true;
    }

    private boolean checkEmpty(String[] row) {
        for (String rowItem: row) {
            if (rowItem.equals("[]") || rowItem.equals("==")) {
                return false;
            }
        }
        return true;
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            doRotate();
            oldTime = System.currentTimeMillis();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            gravityModifier = 10;
        } else {
            gravityModifier = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveBlockLeft();
            oldTime = System.currentTimeMillis();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveBlockRight();
            oldTime = System.currentTimeMillis();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            hardDrop();
            oldTime = System.currentTimeMillis();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            paused = true;
            oldTime = System.currentTimeMillis();
        }

    }

    private boolean canRotate() {
        int[][] nextRotation = blockCurrent.getNextRotation();
        for (int i = 0; i < nextRotation.length; i++) {
            try {
                if (nextRotation[i][0] < 0 || nextRotation[i][0] > BOARD_HEIGHT ||
                        nextRotation[i][1] < 0 || nextRotation[i][1] > BOARD_WIDTH ||
                        tetrisGrid[nextRotation[i][0]][nextRotation[i][1]].equals("[]") ||
                        tetrisGrid[nextRotation[i][0]][nextRotation[i][1]].equals("==")) {
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
                if (tetrisGrid[block[0]][block[1] - 1].equals("[]") ||
                        tetrisGrid[block[0]][block[1] - 1].equals("==")) {
                    updateBlockPosition();
                    return;
                }
            } catch (Exception e) {
                updateBlockPosition();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0], blockCurrent.getCenter()[1] - 1});
    }

    private void moveBlockRight() {
        clearCurrentBlock();
        for (int[] block: blockCurrent.getBlockArray()) {
            try {
                if (tetrisGrid[block[0]][block[1] + 1].equals("[]") ||
                        tetrisGrid[block[0]][block[1] + 1].equals("==")) {
                    updateBlockPosition();
                    return;
                }
            } catch (Exception e) {
                updateBlockPosition();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0], blockCurrent.getCenter()[1] + 1});
    }

    private void doGravity() {
        clearCurrentBlock();
        for (int[] block: blockCurrent.getBlockArray()) {
            if (tetrisGrid[block[0] + 1][block[1]].equals("[]") ||
                tetrisGrid[block[0] + 1][block[1]].equals("==") || blockSet) {
                updateBlockPosition();
                setBlock();
                return;
            }
        }
        updateBlockPosition(new int[] {blockCurrent.getCenter()[0] + 1, blockCurrent.getCenter()[1]});
        gravityTime = System.currentTimeMillis();
    }

    private void setBlock() {
        blockSet = true;
        blockCurrent = blockNext.clone();
        blockNext = new TetrisBlock(randBlock.nextInt(7));
        playerScore += (20 * gravity);
    }

    private void drawHeading() {
        font.setScale(2f, 2f);
        font.drawMultiLine(batch, "TETRIS", 0, Gdx.graphics.getHeight() - 20,
                           Gdx.graphics.getWidth()*0.60F, HAlignment.CENTER);
    }

    private TextBounds drawWelcome() {
        font.setScale(2f, 2f);
        font.drawMultiLine(batch, "TETRIS", 0, Gdx.graphics.getHeight() - 20,
                           Gdx.graphics.getWidth(), HAlignment.CENTER);

        font.setScale(1.5f, 1.5f);
        String welcomeText = "Welcome.\nThis is tetris.\n\nPress Enter to start.";
        TextBounds welcomeBounds = font.getMultiLineBounds(welcomeText);
        font.drawMultiLine(batch, welcomeText, 0, Gdx.graphics.getHeight() - 100,
                           Gdx.graphics.getWidth(), HAlignment.CENTER);
        return welcomeBounds;
    }

    private void drawBoard() {
        font.setScale(1f, 1f);
        String board = getBoardAsString();
        font.drawMultiLine(batch, board, 0, Gdx.graphics.getHeight()*0.90F,
                           Gdx.graphics.getWidth()*0.60F, HAlignment.CENTER);
    }

    private void drawUI() {
        int LEFTMOST_BORDER = 500;

        if (!nextBlockString.equals("") || ! blockNext.toString().equals(nextBlockString)) {
            nextBlockString = blockNext.toString();
        }

        String score = "Score: " + playerScore;
        font.draw(batch, score, LEFTMOST_BORDER, 725);

        String level = "Level: " + playerLevel;
        font.draw(batch, level, LEFTMOST_BORDER, 700);

        String nextPiece = "Next Piece:";
        font.drawMultiLine(batch, nextPiece, LEFTMOST_BORDER, 675);
        font.drawMultiLine(batch, nextBlockString, LEFTMOST_BORDER + 50, 625);
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
}
