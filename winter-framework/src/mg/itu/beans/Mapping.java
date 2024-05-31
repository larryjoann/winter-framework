package mg.itu.beans;

public class Mapping {
    
    protected String class_name;
    protected String methode_name;

    public Mapping (String classs, String method) {
        this.class_name = classs;
        this.methode_name = method;
    }

    @Override
    public String toString() {
        return "class:["+this.class_name+"] methode:["+this.methode_name+"]";
    }

}
