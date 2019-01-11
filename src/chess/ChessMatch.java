package chess;

import boardgame.Board;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

    private Board board;

    public ChessMatch() {
        this.board = new Board(8, 8);
        this.initialSetup();
    }

    private void planeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
    }

    private void initialSetup() {
        this.planeNewPiece('c', 1, new Rook(this.board, Color.RED));
        this.planeNewPiece('c', 2, new Rook(this.board, Color.RED));
        this.planeNewPiece('d', 2, new Rook(this.board, Color.RED));
        this.planeNewPiece('e', 2, new Rook(this.board, Color.RED));
        this.planeNewPiece('e', 1, new Rook(this.board, Color.RED));
        this.planeNewPiece('d', 1, new King(this.board, Color.RED));

        this.planeNewPiece('c', 7, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('c', 8, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('d', 7, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('e', 7, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('e', 8, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('d', 8, new King(this.board, Color.BLUE));
    }

    public ChessPiece[][] getPieces() {

        ChessPiece[][] matriz = new ChessPiece[this.board.getRows()][this.board.getColumns()];
        for (int i = 0; i < board.getRows(); i++)
            for (int j = 0; j < board.getColumns(); j++)
                matriz[i][j] = (ChessPiece) this.board.piece(i, j);

        return matriz;
    }
}
