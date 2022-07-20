package it.prova;

public class Piece {
    private int x, y;
    private String name;
    private MoveSet move;

    public Piece (String name, MoveSet move) {
        x = y = 0;
        this.move = move;
        this.name = name;
    }

    public boolean move (int finalX, int finalY) {
        if (move.validateMove(x, y, finalX, finalY))
            return false;
        x = finalX;
        y = finalY;
        return true;
    }
}
