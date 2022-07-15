package it.prova;

import java.util.Objects;

public abstract class Cliente {

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    protected abstract String getAuthentication ();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(name, cliente.name) &&
                Objects.equals(surname, cliente.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname);
    }

    public Cliente(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    private String name;
    private String surname;
}

