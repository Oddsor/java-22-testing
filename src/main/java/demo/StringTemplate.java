import static java.lang.StringTemplate.RAW;
import static java.util.FormatProcessor.FMT;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

String helloWorld = "Hello, World!";

void main() {
    System.out.println(STR."Computer says \{helloWorld}");

    // Mye kode inni template-blokker! Kanskje ikke så lesbart...?

    System.out.println(STR."""
    Heisann!
    Klokken er \{
            DateTimeFormatter
                    .ofPattern("HH:mm:ss")
                    .format(Instant.now()
                            .atZone(ZoneId.of("Europe/Oslo")))
            }""");

    System.out.println(FMT."""
            Formatere tekst!
            Brusen koster %.2f\{12.16623312}
            %.2f\{12.16623312} %.2f\\{12.16623312}
            """);

    System.out.println(STR."Hei, jeg skal template: \{helloWorld}");
    // Huffda:
    var teller = 3;
    System.out.println(STR."Noen klager på dette eksempelet om \{teller--}, \{teller--}, \{teller--}...");

    var farligNavn = "Smith' OR p.last_name <> 'Smith";
    var farligQuery = RAW."String query = \"SELECT * FROM Person p WHERE p.last_name = '\{farligNavn}'\";";
    System.out.println(farligQuery);
    System.out.println(STR.process(farligQuery));
    var FJERN_FNUTT = StringTemplate.Processor.of((StringTemplate st) -> {
        var output = String.join("XXX", st.fragments());
        var fjernetFnutt = st.values().stream().map(v -> {
            if (v instanceof String s) {
                return s.replaceAll("'", "");
            }
            return v;
        }).toList();
        for (var v: fjernetFnutt) {
            output = output.replaceAll("XXX", v.toString());
        }
        return output;
    });
    System.out.println("Behandlet farligquery:");
    System.out.println(farligQuery.process(FJERN_FNUTT));
}
