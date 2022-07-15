package blob;

import java.util.ArrayList;
import java.util.Scanner;

public class Prodotto {

	public String uno;
	public String due;
	public double tre;

    public double withdraw(String b) {
            BankAccount n=new BankAccount(b);
            return b.getBalance() - 1000;
        }
    public String getMobilePhoneNumber(Phone mobilePhone) {
          return "(" +
             mobilePhone.getAreaCode() + ") " +
             mobilePhone.getPrefix() + "-" +
             mobilePhone.getNumber();
       }

	public String nuovoNomeRistorante() {
		Scanner in= new Scanner(System.in);
		String ristorante=in.nextLine();
		Ristorante r= new Ristorante(ristorante);
		return ristorante=r.getNome_Ristorante();
	}



	public Cliente scorriListaClienti() {
		
		ArrayList<Cliente> clienti= new ArrayList<Cliente>();
		Cliente c= new Cliente("Lucia",30);
		clienti.add(c);
		c= new Cliente("Ugo",51);
		clienti.add(c);
		c= new Cliente("Maria",16);
		clienti.add(c);
		c= new Cliente("Lucia",20);
		clienti.add(c);

		int contatore=0;

		for(int i=0;i<4;i++) {
			if(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}
		}	
		return clienti.get(contatore);
	}
}