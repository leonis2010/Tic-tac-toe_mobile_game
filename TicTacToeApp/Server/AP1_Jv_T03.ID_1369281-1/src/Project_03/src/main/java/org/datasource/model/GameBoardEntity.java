package org.datasource.model;

import java.util.Arrays;

public class GameBoardEntity {
    private int[][] board;
    private static final int SIZE = 3;

    public GameBoardEntity() {
        this.board = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(board[i], 0);
        }
    }

    public GameBoardEntity(int[][] board) {
        this.board = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, SIZE);
        }
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getSize() {
        return SIZE;
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, int value) {
        board[row][col] = value;
    }

    public GameBoardEntity copy() {
        return new GameBoardEntity(this.board);
    }
}