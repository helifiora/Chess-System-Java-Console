package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;

    public ChessMatch() {
        this.board = new Board(8, 8);
        this.turn = 1;
        this.currentPlayer = Color.RED;
        this.initialSetup();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {

        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        this.validateSourcePosition(source);
        this.validateTargetPosition(source, target);

        this.nextTurn();
        Piece capturedPiece = this.makeMove(source, target);
        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position))
            throw new ChessException("There is no piece on source position");

        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor())
            throw new ChessException("The chosen piece is not yours");

        if (!board.piece(position).isThereAnyPossibleMove())
            throw new ChessException("There is no possible moves for the chosen piece");
    }

    private void validateTargetPosition(Position source, Position target) {

        if (!board.piece(source).possibleMove(target))
            throw new ChessException("The chosen piece can't move to target position");
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {

        Position p = sourcePosition.toPosition();
        validateSourcePosition(p);
        return board.piece(p).possibleMoves();
    }

    private Piece makeMove(Position source, Position target) {
        Piece p = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);
        return capturedPiece;
    }

    private void planeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
    }

    private void nextTurn() {
        this.turn++;
        this.currentPlayer = (currentPlayer == Color.RED) ? Color.BLUE : Color.RED;
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

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public ChessPiece[][] getPieces() {

        ChessPiece[][] matriz = new ChessPiece[this.board.getRows()][this.board.getColumns()];
        for (int i = 0; i < board.getRows(); i++)
            for (int j = 0; j < board.getColumns(); j++)
                matriz[i][j] = (ChessPiece) this.board.piece(i, j);

        return matriz;
    }
}
