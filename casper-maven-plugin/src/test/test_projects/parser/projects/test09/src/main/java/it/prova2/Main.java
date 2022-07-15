package it.prova2;

import it.prova.Board;

public class Main {

    private static Board board;

    public static void main (String [] args) {
        Board b = Board.organizeDefaultBoard();
        b.toString();
    }
}
