package it.prova;

public class ProdottoScontato extends Prodotto {
    public ProdottoScontato(String name, double price, int percentage) {
        super(name, price);
        this.percentage = percentage;
    }

    @Override
    public double getPrice() {
        return (super.getPrice() * percentage) / 100.0;
    }

    private int percentage;
}
