import java.math.BigInteger;

static class Java21Klassen {
    private int tall;
    public Java21Klassen() {
        this.tall = 0;
    }
}

public static class Java22Klassen extends Java21Klassen {
    private String tekst;
    public Java22Klassen() {
        // Statements before super ( https://openjdk.org/jeps/447 )
        var tekst = "Tekst";
        //this.tekst = tekst; // Kan fortsatt ikke gjøres.
        super();
        this.tekst = tekst;
    }
}

static class PositivtStortTall extends BigInteger {
    public PositivtStortTall(Long n) {
        if (n < 0) {
            throw new ArithmeticException("Negativt tall!");
        }
        super(n.toString());
    }
}


void main() {
    try{
        var stortTall = new PositivtStortTall(-1L);
        System.out.println(stortTall);
    } catch(Exception e) {
        // String templates:  https://openjdk.org/jeps/459
        System.out.println(STR."Huffda! \{e}");
    }
}
