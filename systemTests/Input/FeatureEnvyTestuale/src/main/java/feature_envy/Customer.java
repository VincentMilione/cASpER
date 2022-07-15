package feature_envy;

public class Customer {
private String name;

public Customer(String name)
        {    this.name=name;
        }

public String getName()
        {    return name;
        }

public String getMobilePhoneNumber(Phone p) {
        return tel.getAreaCode()+tel.getPrefix()+tel.getNumber();    }}