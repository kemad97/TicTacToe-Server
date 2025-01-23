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
    public static int checkTicTacToeGameBoard(String[][] gameBoard) {

        int emptyCells = getEmptyCells(gameBoard);

        String winner = checkWinner(gameBoard);

        switch (winner) {
            case "X":
                return X_WIN;
            case "O":
                return O_WIN;
            default:
                if (emptyCells == 0) {
                    return DRAW;
                }
                return HAVE_NEXT_MOVE;
        }


    }

    private static int getEmptyCells(String[][] gameBoard) {
        int emptyCells = 0;

        for (String[] raw : gameBoard) {
            for (String cell : raw) {
                if ("".equals(cell)) {
                    emptyCells++;
                }
            }
        }
        return emptyCells;
    }

    private static String checkWinner(String[][] gameBoard) {
        String colsResult = checkRows(gameBoard);
        if ("X".equals(colsResult) || "O".equals(colsResult)) {
            return colsResult;
        }

        String rowsResult = checkCols(gameBoard);
        if ("X".equals(rowsResult) || "O".equals(rowsResult)) {
            return rowsResult;
        }

        String diagonalsResult = checkdiagonals(gameBoard);
        if ("X".equals(diagonalsResult) || "O".equals(diagonalsResult)) {
            return diagonalsResult;
        }
        return "";
    }

    private static String checkRows(String[][] gameBoard) {
        for (String[] row : gameBoard) {
            if (!"".equals(row[0]) && row[0].equals(row[1]) && row[1].equals(row[2])) {
                return row[0];
            }
        }
        return "";
    }

    private static String checkCols(String[][] gameBoard) {
        for (int i = 0; i < gameBoard.length; i++) {
            if (!"".equals(gameBoard[0][i]) && gameBoard[0][i].equals(gameBoard[1][i]) && gameBoard[1][i].equals(gameBoard[2][i])) {
                return gameBoard[0][i];
            }
        }

        return "";
    }

    private static String checkdiagonals(String[][] gameBoard) {
        if (!"".equals(gameBoard[0][0]) && gameBoard[0][0].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][2])) {
            return gameBoard[0][0];
        }
        if (!"".equals(gameBoard[0][2]) && gameBoard[0][2].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][0])) {
            return gameBoard[0][2];
        }
        return "";
    }
}
