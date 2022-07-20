package it.prova;

import java.util.ArrayList;

public class Board {
    private ArrayList<Piece> list;
    private int width, length;

    public Board(ArrayList<Piece> lst, int i, int i1) {
        list = lst;
        length = i;
        width = i1;
    }

    public static Board organizeDefaultBoard () {
        ArrayList<Piece> lst = new ArrayList<>();
        Piece king1 = new Piece("kingWhite", new TrueMove());
        Piece king2 = new Piece("kingBlack", new TrueMove());

        lst.add(king1);
        lst.add(king2);
        return new Board(lst, 8, 8);
    }

    public void move (Piece p, int i, int y) {
        p.move(i, y);
    }

}
