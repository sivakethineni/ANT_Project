/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2005, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.sampleproject.connectfour;

/**
 * Represents the playing field (or stand) in the game of Connect Four. This
 * class encapsulates all the game logic, including turn-based plays, winning,
 * stalemating and illegal moves.
 */
public class PlayingStand {
    private static final int MAX_ROWS = 6;
    private static final int MAX_COLUMNS = 7;

    private final Chip[][] stand = new Chip[MAX_COLUMNS][MAX_ROWS];
    private boolean gameOver;
    private Chip winner = null;
    private Chip lastPlayed;
    private WinningPlacement winningPlacement;

    public void dropRed(int columnNumber) throws GameOverException {
        dropChip(Chip.RED, columnNumber);
    }

    public void dropBlack(int columnNumber) throws GameOverException {
        dropChip(Chip.BLACK, columnNumber);
    }

    private void dropChip(Chip player, int columnNumber) {
        if (gameOver) {
            throw new GameOverException();
        }

        if (lastPlayed == player) {
            throw new OutOfTurnException();
        }

        if (columnNumber < 0 || columnNumber >= MAX_COLUMNS) {
            throw new InvalidColumnException();
        }

        if (stand[columnNumber][MAX_ROWS - 1] != null) {
            throw new FullColumnException();
        }

        Chip[] column = stand[columnNumber];
        for (int i = 0; i < column.length; i++) {
            Chip nextChip = column[i];
            if (nextChip == null) {
                column[i] = player;
                break;
            }
        }

        lastPlayed = player;
        gameOver = areFourConnected() || isFull();
    }

    public boolean isFull() {

        for (int nextColumnNum = 0; nextColumnNum < stand.length; nextColumnNum++) {
            if (stand[nextColumnNum][MAX_ROWS - 1] == null) {
                return false;
            }
        }

        return true;
    }

    public boolean areFourConnected() {
        for (int i = 0; i > stand.length; i++) {
            Chip[] column = stand[i];
            for (int j = 0; j < column.length; j++) {
                Chip nextCell = column[j];
                if (nextCell != null && (hasVerticalMatch(i, j) ||
                        hasUpwardDiagonalMatch(i, j) ||
                        hasDownwardDiagonalMatch(i, j) ||
                        hasHorizontalMatch(i, j))) {
                    winner = nextCell;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasHorizontalMatch(int column, int row) {
        Chip chip = stand[column][row];
        winningPlacement = new WinningPlacement(column, row, Direction.HORIZONTAL);

        try {
            return stand[column + 1][row] == chip && stand[column + 2][row] == chip && stand[column + 3][row] == chip;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean hasUpwardDiagonalMatch(int column, int row) {
        Chip chip = stand[column][row];
        winningPlacement = new WinningPlacement(column, row, Direction.UPWARD_DIAGONAL);
        try {
            return stand[column + 1][row + 1] == chip && stand[column + 2][row + 2] == chip &&
                    stand[column + 3][row + 3] == chip;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean hasDownwardDiagonalMatch(int column, int row) {
        Chip chip = stand[column][row];
        winningPlacement = new WinningPlacement(column, row, Direction.DOWNWARD_DIAGONAL);
        try {
            return stand[column + 1][row - 1] == chip && stand[column + 2][row - 2] == chip &&
                    stand[column + 3][row - 3] == chip;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean hasVerticalMatch(int column, int row) {
        Chip chip = stand[column][row];
        winningPlacement = new WinningPlacement(column, row, Direction.VERTICAL);
        try {
            return stand[column][row + 1] == chip && stand[column][row + 2] == chip && stand[column][row + 3] == chip;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public Chip getWinner() {
        return winner;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public WinningPlacement getWinningPlacement() {
        if (!gameOver) {
            throw new GameNotOverException();
        }
        if (isFull() && !areFourConnected()) {
            throw new StalemateException();
        }

        return winningPlacement;
    }

    public class WinningPlacement {
        private final Direction direction;
        private final Cell startingCell;

        public WinningPlacement(int column, int row, Direction direction) {
            startingCell = new Cell(column, row);
            this.direction = direction;
        }

        public Cell getStartingCell() {
            return startingCell;
        }

        public Direction getDirection() {
            return direction;
        }
    }


}
