package misplaced_class.second;

import misplaced_class.first.Cliente;
import java.util.ArrayList;

public class Gestione{

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
			if(clienti.get(contatore).getEtà()<clienti.get(i).getEtà()){contatore=i;}
		}	
		return clienti.get(contatore);
	}

public Cliente getMobilePhoneNumber(){
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
			if(clienti.get(contatore).getEtà()<clienti.get(i).getEtà()){contatore=i;}
		}

		System.out.println(c.getName());
		return clienti.get(contatore);

}


}