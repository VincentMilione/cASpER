package feature_envy;

public class Customer{

   private String name;

   public Customer(String name)
   {    this.name=name;
   }

   public String getName()
   {    return name;
   }

   public Phone getMobilePhoneNumber() {
      Phone tel=new Phone(3333333333);
      string s="(" + tel.getAreaCode()+ ") " ;
      tel=new Phone(4444444444);
      s+=tel.getPrefix() + "-" ;
      tel=new Phone(5555555555);
      s+=tel.getNumber();
      tel=new Phone(6666666666);
      return new Phone(tel.getAreaCode() + ") " +
             tel.getPrefix() + "-" +
             tel.getNumber());
   }
}