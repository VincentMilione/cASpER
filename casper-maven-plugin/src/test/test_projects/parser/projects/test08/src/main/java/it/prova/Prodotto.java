package it.prova;

public class Prodotto {
    private String name;
    private double price;

    public Prodotto(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
