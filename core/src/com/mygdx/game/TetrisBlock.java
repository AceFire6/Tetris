package com.mygdx.game;

public class TetrisBlock {
    private static final int[][][][] blockStates = {
            {{{-1, 0}, {1, 0}, {2, 0}}, {{0, -1}, {0, 1}, {0, 2}}}, //i //0
            {{{0, -1}, {0, 1}, {-1, 1}}, {{-1, 0}, {1, 0}, {1, 1}}, {{1, -1}, {0, -1}, {0, 1}}, {{-1, -1}, {-1, 0}, {1, 0}}}, //l //1
            {{{0, -1}, {0, 1}, {1, 1}}, {{1, -1}, {1, 0}, {-1, 0}}, {{-1, -1}, {0, -1}, {0, 1}}, {{-1, 0}, {1, 0}, {-1, 1}}}, //j //2
            {{{0, 1}, {1, 0}, {1, 1}}}, //o //3
            {{{0, 1}, {-1, 1}, {1, 0}}, {{0, -1}, {1, 0}, {1, 1}}}, //s //4
            {{{-1, 0}, {0, 1}, {1, 1}}, {{1, -1}, {1, 0}, {0, 1}}}, //z //5
            {{{-1, 0}, {1, 0}, {0, 1}}, {{0, -1}, {1, 0}, {0, 1}}, {{-1, 0}, {1, 0}, {0, -1}}, {{0, -1}, {-1, 0}, {0, 1}}},  //t //6
    };
    private int[][] blockArray;
    protected int blockType;
    private int[] center;
    protected int rotationState = 0;
    private boolean placed = false;


    public TetrisBlock(int blockType) {
        blockArray = blockStates[blockType][rotationState].clone();
        this.blockType = blockType;
        center = new int[] {2, 4};
        updateBlockArray();
    }

    public void place() {
        placed = true;
    }

    public boolean isPlaced() {
        return placed;
    }

    public TetrisBlock(int blockType, int[] center, int rotationState) {
        blockArray = blockStates[blockType][rotationState].clone();
        this.blockType = blockType;
        this.center = center.clone();
        updateBlockArray();
    }

    public void updateBlockArray() {
        for (int i = 0; i < blockArray.length; i++) {
            blockArray[i] = new int[] {center[0] + blockStates[blockType][rotationState][i][0],
                                       center[1] + blockStates[blockType][rotationState][i][1]};
        }
    }

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

    public void rotateBlock() {
        if (blockType != 3) { //The Square
            rotationState = (rotationState + 1) % blockStates[blockType].length;
            blockArray = blockStates[blockType][rotationState].clone();
            updateBlockArray();
        }
    }

    @Override
    public TetrisBlock clone() {
        return new TetrisBlock(this.blockType, this.center.clone(), this.rotationState);
    }

    @Override
    public String toString() {
        String blockString = "";
        String blockGrid[][] = new String[4][4];
        blockGrid[1][1] = "[]";
        for (int[] blockCoords: blockStates[blockType][rotationState]) {
            blockGrid[1 + blockCoords[0]][1 + blockCoords[1]] = "[]";
        }

        for (String[] blockRow: blockGrid) {
            for (String block: blockRow) {
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

    public int[][] getBlockArray() {
        return blockArray.clone();
    }

    public int[] getCenter() {
        return center.clone();
    }

    public void setCenter(int[] center) {
        this.center = center.clone();
        updateBlockArray();
    }
}
