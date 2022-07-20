package it.prova;

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

    public Cliente(String name, String surname, String codFiscale) {
        this.name = name;
        this.surname = surname;
        this.codFiscale = codFiscale;
    }

    private String name;
    private String surname;
    private String codFiscale;
}
