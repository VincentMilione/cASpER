package blob;

public class BankAccount {

    private double balance;

    public BankAccount(double balance, int accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    public double getBalance(){
    return balance;}
}