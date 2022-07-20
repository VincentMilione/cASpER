package it.prova;

import java.util.Objects;

public class Cliente {
    public Cliente () { }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getCodFiscale() {
        return codFiscale;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", codFiscale='" + codFiscale + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(name, cliente.name) &&
                Objects.equals(surname, cliente.surname) &&
                Objects.equals(codFiscale, cliente.codFiscale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, codFiscale);
    }

    public Cliente(String name, String surname, String codFiscale) {
        this.name = name;
        this.surname = surname;
        this.codFiscale = codFiscale;
    }

    private String name;
    private String surname;
    private String codFiscale;
}

