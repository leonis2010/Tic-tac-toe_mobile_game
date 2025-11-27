package org.web.model;

public class GameBoardDto {
    private int[][] board;
    private int size;

    public GameBoardDto() {
        this.size = 3;
        this.board = new int[3][3];
    }

    public GameBoardDto(int[][] board) {
        this.size = board.length;
        this.board = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, size);
        }
    }

    public org.domain.model.GameBoard toDomain() {
        return new org.domain.model.GameBoard(this.board);
    }

    // Геттеры и сеттеры
    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, int value) {
        board[row][col] = value;
    }
}