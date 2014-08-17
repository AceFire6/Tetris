package com.mygdx.tetris;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Jethro Muller - MLLJET001
 * @version 1.0.0
 *
 * Holds the information about a tetris block.
 * Its type, colour, rotation state, center and an array of offsets
 * that can be used to construct the block.
 */

public class TetrisBlock {
    /**
     * An array that contains the blocks and their rotation states.
     * Each block has a set of offset values for each rotation state that are the positions of
     * the smaller squares that make up a block relative to the block's center.
     */
    private static final int[][][][] blockStates =
            {{{{- 1, 0}, {1, 0}, {2, 0}}, {{0, - 1}, {0, 1}, {0, 2}}}, //i //0
             {{{0, - 1}, {0, 1}, {- 1, 1}}, {{- 1, 0}, {1, 0}, {1, 1}},
              {{1, - 1}, {0, - 1}, {0, 1}}, {{- 1, - 1}, {- 1, 0}, {1, 0}}}, //l //1
             {{{0, - 1}, {0, 1}, {1, 1}}, {{1, - 1}, {1, 0}, {- 1, 0}},
              {{- 1, - 1}, {0, - 1}, {0, 1}}, {{- 1, 0}, {1, 0}, {- 1, 1}}}, //j //2
             {{{0, 1}, {1, 0}, {1, 1}}}, //o //3
             {{{0, 1}, {- 1, 1}, {1, 0}}, {{0, - 1}, {1, 0}, {1, 1}}}, //s //4
             {{{- 1, 0}, {0, 1}, {1, 1}}, {{1, - 1}, {1, 0}, {0, 1}}}, //z //5
             {{{- 1, 0}, {1, 0}, {0, 1}}, {{0, - 1}, {1, 0}, {0, 1}}, {{- 1, 0}, {1, 0}, {0, - 1}},
              {{0, - 1}, {- 1, 0}, {0, 1}}},  //t //6
            };

    /**
     * Each block type has it's own colour. This array stores the colours and is used to retrieve
     * them.
     */
    private static final Color[]  blockColours = {new Color(0.35F, 0.47F, 0.98F, 1F), Color.CYAN,
                                                  Color.GREEN,
                                                  Color.MAGENTA, new Color(1F, 0.39F, 0.13F, 1F),
                                                  Color.YELLOW,
                                                  new Color(0.93F, 0.72F, 0.31F, 1F)};
    /**
     * What type of block this block is.
     */
    protected int blockType;
    /**
     * Which state of rotation the block is in.
     */
    protected int rotationState = 0;
    private int[][] blockArray;
    /**
     * The coordinates for the center of the block. Also used to determine where in the tetris
     * grid the block is located.
     */
    private int[] center;
    /**
     * The block's colour.
     */
    private Color blockColour;

    /**
     * Returns the colour of the block.
     * @return Colour of the block.
     */
    public Color getBlockColour() {
        return blockColour;
    }

    /**
     * Parameterized constructor.
     * @param blockType    The type of block to be made.
     */
    public TetrisBlock(int blockType) {
        blockArray = blockStates[blockType][rotationState].clone();
        this.blockType = blockType;
        center = new int[] {2, 4};
        updateBlockArray();
        blockColour = blockColours.clone()[blockType];
    }

    /**
     * Updates the block's sub-blocks coordinates based on the block's current center.
     */
    public void updateBlockArray() {
        for (int i = 0; i < blockArray.length; i++) {
            blockArray[i] = new int[] {center[0] + blockStates[blockType][rotationState][i][0],
                                       center[1] + blockStates[blockType][rotationState][i][1]};
        }
    }

    /**
     * Copy constructor. Recreates the block from the given information.
     * @param blockType        The type of block to be made.
     * @param center           The coordinates for the center of the block.
     * @param rotationState    The state of rotation the block was in.
     */
    public TetrisBlock(int blockType, int[] center, int rotationState) {
        blockArray = blockStates[blockType][rotationState].clone();
        this.blockType = blockType;
        this.center = center.clone();
        updateBlockArray();
        blockColour = blockColours.clone()[blockType];
    }

    /**
     * The next rotation state's offsets.
     * @return int[][] containing the next rotation state's offsets.
     */
    public int[][] getNextRotation() {
        int tempRotation = (rotationState + 1) % blockStates[blockType].length;
        int[][] temp = blockStates[blockType][tempRotation].clone();
        int[][] tempReturn = new int[temp.length][2];
        for (int i = 0; i < temp.length; i++) {
            tempReturn[i] = new int[] {center[0] + blockStates[blockType][tempRotation][i][0],
                                       center[1] + blockStates[blockType][tempRotation][i][1]};
        }
        return tempReturn.clone();
    }

    /**
     * Rotates the block by changing its rotation state. The rotation state wraps once it reaches
     * the final rotation state.
     */
    public void rotateBlock() {
        if (blockType != 3) { //The Square
            rotationState = (rotationState + 1) % blockStates[blockType].length;
            blockArray = blockStates[blockType][rotationState].clone();
            updateBlockArray();
        }
    }

    /**
     * Gets and returns a copy of the current block.
     * @return Returns a copy of the current block.
     */
    public TetrisBlock cloneBlock() {
        return new TetrisBlock(this.blockType, this.center.clone(), this.rotationState);
    }

    /**
     * Returns a string representation of the current block.
     * Used for displaying the current block and stored block in the UI.
     *
     * @return String representation of the current block.
     */
    @Override
    public String toString() {
        String blockString = "";
        String blockGrid[][] = new String[4][4];
        blockGrid[1][1] = "[#" + blockColour + "]" + "[[]" + "[]";
        for (int[] blockCoords : blockStates[blockType][rotationState]) {
            blockGrid[1 + blockCoords[0]][1 + blockCoords[1]] = "[#" + blockColour + "]" + "[[]" + "[]";
        }

        for (String[] blockRow : blockGrid) {
            for (String block : blockRow) {
                if (block != null) {
                    blockString += block;
                } else {
                    blockString += "  ";
                }
            }
            blockString += "\n";
        }
        return blockString;
    }

    /**
     * Returns a copy of the current set of offsets.
     * @return Copy of the current set of offsets.
     */
    public int[][] getBlockArray() {
        return blockArray.clone();
    }

    /**
     * Returns the center of the tetris block, also its position in the tetris grid.
     * @return int[] containing the center of the tetris block, also its position in the tetris
     * grid.
     */
    public int[] getCenter() {
        return center.clone();
    }

    /**
     * Sets the center of the tetris block, also its position in the tetris grid.
     * @param center The new center of the tetris block, also its new position in the tetris grid.
     */
    public void setCenter(int[] center) {
        this.center = center.clone();
        updateBlockArray();
    }
}
