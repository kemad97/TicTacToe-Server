package tictactoe.server.referee;
public class Referee {

    public static final int HAVE_NEXT_MOVE = 0;
    public static final int DRAW = 1;
    public static final int X_WIN = 2;
    public static final int O_WIN = 3;

    //0:: have next move
    //1:: draw
    //2:: x win
    //3:: o win
    public static int checkTicTacToeGameBoard(char[][] gameBoard) {

        int emptyCells = getEmptyCells(gameBoard);

        char winner = checkWinner(gameBoard);

        switch (winner) {
            case 'x':
                return X_WIN;
            case 'o':
                return O_WIN;
            default:
                if (emptyCells == 0) {
                    return DRAW;
                }
                return HAVE_NEXT_MOVE;
        }


    }

    private static int getEmptyCells(char[][] gameBoard) {
        int emptyCells = 0;

        for (char[] raw : gameBoard) {
            for (char cell : raw) {
                if (cell == '_') {
                    emptyCells++;
                }
            }
        }
        return emptyCells;
    }

    private static char checkWinner(char[][] gameBoard) {
        char colsResult = checkRows(gameBoard);
        if (colsResult == 'x' || colsResult == 'o') {
            return colsResult;
        }

        char rowsResult = checkCols(gameBoard);
        if (rowsResult == 'x' || rowsResult == 'o') {
            return rowsResult;
        }

        char diagonalsResult = checkdiagonals(gameBoard);
        if (diagonalsResult == 'x' || diagonalsResult == 'o') {
            return diagonalsResult;
        }
        return '_';
    }

    private static char checkRows(char[][] gameBoard) {
        for (char[] row : gameBoard) {
            if (row[0] != '_' && row[0] == row[1] && row[1] == row[2]) {
                return row[0];
            }
        }
        return '_';
    }

    private static char checkCols(char[][] gameBoard) {
        for (int i = 0; i < gameBoard.length; i++) {
            if (gameBoard[0][i] != '_' && gameBoard[0][i] == gameBoard[1][i] && gameBoard[1][i] == gameBoard[2][i]) {
                return gameBoard[0][i];
            }
        }

        return '_';
    }

    private static char checkdiagonals(char[][] gameBoard) {
        if (gameBoard[0][0] != '_' && gameBoard[0][0] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][2]) {
            return gameBoard[0][0];
        }
        if (gameBoard[0][2] != '_' && gameBoard[0][2] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][0]) {
            return gameBoard[0][2];
        }
        return '_';
    }
}
