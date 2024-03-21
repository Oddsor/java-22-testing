import java.util.List;

// Implisitte klasser ( https://openjdk.org/jeps/463 )

private List<Integer> lagListe() {
    return List.of(1,2,3,4);
}

String helloWorld = "Hello, World!";

// Implisitt main-klasse
void main() {
    // variabelen i er ikke i bruk, og vil gi et varsel i IntelliJ
    for (int i: lagListe()) {
        System.out.println(STR."Computer says \{helloWorld}");
    }

    // rename til _ for å unngå varsel ( https://openjdk.org/jeps/456 )
    for (int _: lagListe()) {
        System.out.println(STR."Computer says \{helloWorld}");
    }

    System.out.println(this.getClass().getName());
}
