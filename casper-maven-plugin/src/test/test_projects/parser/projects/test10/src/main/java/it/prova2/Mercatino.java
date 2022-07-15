package it.prova2;

import it.prova.Cliente;
import it.prova.Prodotto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Mercatino {

    public Mercatino (String name, String partitaIVA) {
        this.name = name;
        this.partitaIVA = partitaIVA;
        this.lst = new ArrayList<Cliente>();
        this.lstProdotti = new HashSet<Prodotto>();
    }

    public void addCliente (Cliente c) {
        if (!lst.contains(c))
            lst.add(c);
    }

    public void addProdotto (Prodotto p) {
        lstProdotti.add(p);
    }

    public void removeProduct (Prodotto p) {
        lstProdotti.remove(p);
    }

    private ArrayList<Cliente> lst;
    private String name;
    private String partitaIVA;
    private Set<Prodotto> lstProdotti;
}
