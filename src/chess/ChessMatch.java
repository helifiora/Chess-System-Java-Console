package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn;
    private boolean check;
    private boolean checkMate;
    private Color currentPlayer;
    private Board board;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard;
    private List<Piece> capturedPieces;

    public ChessMatch() {
        this.piecesOnTheBoard = new ArrayList<>();
        this.capturedPieces = new ArrayList<>();
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

        Piece capturedPiece = this.makeMove(source, target);
        if (testCheck(currentPlayer)) {
            this.undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece) board.piece(target);

        // #specialmove promotion
        promoted = null;
        if (movedPiece instanceof Pawn) {

            if ((movedPiece.getColor() == Color.RED && target.getRow() == 0) || (movedPiece.getColor() == Color.BLUE && target.getRow() == 7)) {

                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer)))
            checkMate = true;
        else
            this.nextTurn();

        // #specialmove en passant
        if (movedPiece instanceof Pawn && target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)
            enPassantVulnerable = movedPiece;
        else
            enPassantVulnerable = null;

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

    public ChessPiece replacePromotedPiece(String type) {

        if (promoted == null)
            throw new IllegalStateException("There is no piece to be promoted");

        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q"))
            throw new InvalidParameterException("Invalid type for promotion");

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece nPiece = newPiece(type, promoted.getColor());
        board.placePiece(nPiece, pos);
        piecesOnTheBoard.add(nPiece);

        return nPiece;
    }

    private ChessPiece newPiece(String type, Color color) {

        if (type.equals("B"))
            return new Bishop(board, color);

        if (type.equals("N"))
            return new Knight(board, color);

        if (type.equals("Q"))
            return new Queen(board, color);

        return new Rook(board, color);
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
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null) {
            this.piecesOnTheBoard.remove(capturedPiece);
            this.capturedPieces.add(capturedPiece);
        }

        // #specialmove castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        // #specialmove castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {

                Position pawnPosition;
                if (p.getColor() == Color.RED)
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                else
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());

                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {

        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        this.board.placePiece(p, source);

        if (capturedPiece != null) {
            this.board.placePiece(capturedPiece, target);
            this.capturedPieces.remove(capturedPiece);
            this.piecesOnTheBoard.add(capturedPiece);
        }

        // #specialmove castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        // #specialmove castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {

                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.RED)
                    pawnPosition = new Position(3, target.getColumn());
                else
                    pawnPosition = new Position(4, target.getColumn());

                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private Color opponent(Color color) {

        return (color == Color.RED) ? Color.BLUE : Color.RED;
    }

    private ChessPiece king(Color color) {

        List<Piece> list = this.piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board");
    }

    private boolean testCheck(Color color) {

        Position kingPosition = this.king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = this.piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == this.opponent(color)).collect(Collectors.toList());

        for (Piece p : opponentPieces) {

            boolean[][] matrix = p.possibleMoves();
            if (matrix[kingPosition.getRow()][kingPosition.getColumn()])
                return true;
        }

        return false;
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color))
            return false;

        List<Piece> list = this.piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {

            boolean[][] matrix = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++)
                for (int j = 0; j < board.getColumns(); j++)
                    if (matrix[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean tCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!tCheck)
                            return false;

                    }
        }

        return true;
    }

    private void planeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        this.piecesOnTheBoard.add(piece);
    }

    private void nextTurn() {
        this.turn++;
        this.currentPlayer = (currentPlayer == Color.RED) ? Color.BLUE : Color.RED;
    }

    private void initialSetup() {
        this.planeNewPiece('a', 1, new Rook(this.board, Color.RED));
        this.planeNewPiece('b', 1, new Knight(this.board, Color.RED));
        this.planeNewPiece('c', 1, new Bishop(this.board, Color.RED));
        this.planeNewPiece('d', 1, new Queen(this.board, Color.RED));
        this.planeNewPiece('e', 1, new King(this.board, Color.RED, this));
        this.planeNewPiece('f', 1, new Bishop(this.board, Color.RED));
        this.planeNewPiece('g', 1, new Knight(this.board, Color.RED));
        this.planeNewPiece('h', 1, new Rook(this.board, Color.RED));
        this.planeNewPiece('a', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('b', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('c', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('d', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('e', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('f', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('g', 2, new Pawn(this.board, Color.RED, this));
        this.planeNewPiece('h', 2, new Pawn(this.board, Color.RED, this));

        this.planeNewPiece('a', 8, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('b', 8, new Knight(this.board, Color.BLUE));
        this.planeNewPiece('c', 8, new Bishop(this.board, Color.BLUE));
        this.planeNewPiece('d', 8, new Queen(this.board, Color.BLUE));
        this.planeNewPiece('e', 8, new King(this.board, Color.BLUE, this));
        this.planeNewPiece('f', 8, new Bishop(this.board, Color.BLUE));
        this.planeNewPiece('g', 8, new Knight(this.board, Color.BLUE));
        this.planeNewPiece('h', 8, new Rook(this.board, Color.BLUE));
        this.planeNewPiece('a', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('b', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('c', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('d', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('e', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('f', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('g', 7, new Pawn(this.board, Color.BLUE, this));
        this.planeNewPiece('h', 7, new Pawn(this.board, Color.BLUE, this));

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

    public boolean getCheck() {
        return this.check;
    }

    public boolean getCheckMate() {
        return this.checkMate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }
}
