package misplaced_class.second;

import misplaced_class.first.Cliente;
import java.util.*;

public class Ristorante {

    private ArrayList<Cliente> clienti;

    public Ristorante(ArrayList<Cliente> clienti){
        this.clienti=clienti;
    }

    public void printClienti(){
        for(Cliente c:clienti)
            System.out.println(c.getName());
    }

}
