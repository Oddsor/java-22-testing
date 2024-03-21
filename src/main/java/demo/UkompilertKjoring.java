package demo;

// Kan kjøres fra terminalen med "java -p . UkompilertKjoring.java"
// https://openjdk.org/jeps/458
public class UkompilertKjoring {
    public static void main(String[] args) throws Exception {
        System.out.println("Jeg kan kjøre direkte!");
        System.out.println("Og hente trege tall:");
        System.out.println(TregIdentitet.tregIdentitet("TREG").call());
    }
}
