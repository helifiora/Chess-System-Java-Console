package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;

import javax.sound.midi.Soundbank;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UI {

    // https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static void printMatch(ChessMatch chessMatch, List<ChessPiece> captured) {

        printBoard(chessMatch.getPieces());
        System.out.println();
        printCapturedPieces(captured);
        System.out.println();
        System.out.println("Turn: " + chessMatch.getTurn());

        System.out.print("Waiting player: " );

        if (chessMatch.getCurrentPlayer() == Color.RED)
            System.out.print(ANSI_RED);
        else
            System.out.print(ANSI_BLUE);

        System.out.println(chessMatch.getCurrentPlayer());

        System.out.print(ANSI_RESET);
    }

    public static void printBoard(ChessPiece[][] pieces) {

        for (int i = 0; i < pieces.length; i++) {

            System.out.print((8 - i) + " ");
            for (int j = 0; j < pieces[i].length; j++)
                printPiece(pieces[i][j], false);

            System.out.println();
        }

        System.out.println("  a b c d e f g h");
    }

    public static void printBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {

        for (int i = 0; i < pieces.length; i++) {

            System.out.print((8 - i) + " ");
            for (int j = 0; j < pieces[i].length; j++)
                printPiece(pieces[i][j], possibleMoves[i][j]);

            System.out.println();
        }

        System.out.println("  a b c d e f g h");
    }

    private static void printPiece(ChessPiece piece, boolean background) {

        if (background)
            System.out.print(ANSI_GREEN_BACKGROUND);

        if (piece == null)
            System.out.print("-");
        else {

            if (piece.getColor() == Color.RED)
                System.out.print(ANSI_RED + piece);
            else
                System.out.print(ANSI_CYAN + piece);
        }

        System.out.print(ANSI_RESET);
        System.out.print(" ");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static ChessPosition readChessPosition(Scanner sc) {

        try {

            String s = sc.nextLine();
            char colummn = s.charAt(0);
            int row = Integer.parseInt(s.substring(1));
            return new ChessPosition(colummn, row);

        } catch (RuntimeException e) {
            throw new InputMismatchException("Error reading ChessPosition. Valid value are from a1 to h8");
        }
    }

    private static void printCapturedPieces(List<ChessPiece> captured) {
        List<ChessPiece> red = captured.stream().filter(x -> x.getColor() == Color.RED).collect(Collectors.toList());
        List<ChessPiece> blue = captured.stream().filter(x -> x.getColor() == Color.BLUE).collect(Collectors.toList());

        System.out.println("Captured pieces: ");

        System.out.print("Red: ");
        System.out.print(ANSI_RED);
        System.out.println(Arrays.toString(red.toArray()));

        System.out.print(ANSI_RESET);

        System.out.print("Blue: ");
        System.out.print(ANSI_BLUE);
        System.out.println(Arrays.toString(blue.toArray()));

        System.out.print(ANSI_RESET);
    }
}
