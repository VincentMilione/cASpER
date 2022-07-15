package it.prova;

public class ClienteAbituale extends Cliente {

    private String codiceFiscale;

    public ClienteAbituale(String name, String surname, String codiceFiscale) {
        super(name, surname);
        this.codiceFiscale = codiceFiscale;
    }

    @Override
    protected String getAuthentication() {
        return codiceFiscale;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "name='" + super.getName() + '\'' +
                ", surname='" + super.getSurname() + '\'' +
                '}';
    }
}
