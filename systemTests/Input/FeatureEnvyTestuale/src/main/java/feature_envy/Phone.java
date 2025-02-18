package feature_envy;
public class Phone {
    private final String operatore="tim";
    private String unformattedNumber;
    public Phone(String unformattedNumber) {
        this.unformattedNumber = unformattedNumber;
    }
    public String getAreaCode() {
        return unformattedNumber.substring(0,3);
    }
    public String getPrefix() {
        return unformattedNumber.substring(3,6);
    }
    public String getNumber() {
        return unformattedNumber.substring(6,10);
    }
    public String getOperatore() {
        return operatore+getAreaCode()+getPrefix()+getNumber();
    }
    public String reverceNumber() {
        return getNumber()+getPrefix()+getAreaCode();
    }
    public String italianNumber() {
        return "39+"+getNumber()+getPrefix()+getAreaCode();
    }
    public boolean pushNumber(){
        if(getAreaCode().equals(getPrefix()))
            if(getPrefix().equals(getNumber())){return false;}
            else{ unformattedNumber.replace(getNumber(),"lol");
                return true;
            }
        else{return true;}

    }}
