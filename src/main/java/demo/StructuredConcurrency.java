import static demo.TregIdentitet.tregIdentitet;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public static void main(String[] args) {
    System.out.println(gammeldagsConcurrency());
    
    joineFlereOppgaver();

    stoppHvisEnOppgaveFeiler();

    stoppDersomEnOppgaveLykkes();

    System.out.println(STR."Prosesserte \{joinMangeOppgaver().size()} tall");

    oppgaverPaaRad();

    System.out.println(aksepterFeilendeJobber());
}

// ExecutorService
private static ArrayList<Integer> gammeldagsConcurrency() {
    List<Callable<Integer>> funs = IntStream.range(0, 40).mapToObj(x -> tregIdentitet(x, 100L)).toList();
    try (var service = newFixedThreadPool(4)){
        var futures = service.invokeAll(funs);
        var resultater = new ArrayList<Integer>();
        futures.forEach(x -> {
            try {
                resultater.add(x.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return resultater;
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}

// Kjør tre oppgaver samtidig
private static void joineFlereOppgaver() {
    try (var scope = new StructuredTaskScope()) {
        StructuredTaskScope.Subtask<Integer> subtask1 = scope.fork(tregIdentitet(1, 100L));

        StructuredTaskScope.Subtask<Integer> subtask2 = scope.fork(tregIdentitet(2, 100L));

        StructuredTaskScope.Subtask<Integer> subtask3 = scope.fork(tregIdentitet(3, 100L));

        scope.join();
        System.out.println(List.of(subtask1.get(), subtask2.get(), subtask3.get()));
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}

// Hvis en oppgave feiler, så stoppes andre oppgaver
private static void stoppHvisEnOppgaveFeiler() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var slowTask = scope.fork(() -> {
            Thread.sleep(2000L);
            System.out.println("Burde ikke kjøre!");
            return "OK";
        });
        var failingTask = scope.fork(() -> {
            Thread.sleep(500L);
            throw new RuntimeException("Oppgaven feiler");
        });
        scope.join().throwIfFailed();
        System.out.println(slowTask.get());
    } catch (InterruptedException | ExecutionException e) {
        System.out.println(STR."Feilmelding: \{e.getMessage()}");
    }
}

// Hvis en oppgave fullfører, stopper andre tråder
private static void stoppDersomEnOppgaveLykkes() {
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess()) {
        var fastTask = scope.fork(tregIdentitet("OK", 500L));
        var failingTask = scope.fork(() -> {
            Thread.sleep(200L);
            throw new RuntimeException("Feil påvirker ikke andre oppgave");
        });
        var slowFailingTask = scope.fork(() -> {
            Thread.sleep(2000L);
            throw new RuntimeException("Feil påvirker ikke andre oppgave");
        });
        System.out.println(STR."Resultat fra oppgave v1: \{scope.join().result()}");
        System.out.println(STR."Resultat fra oppgave v2: \{fastTask.get()}");
        System.out.println(STR."Feilende oppgave: \{failingTask.state()}");
        System.out.println(STR."Feilende treg oppgave: \{slowFailingTask.state()}");
    } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
    }
}

// Kjør 10000 trege oppgaver parallelt
private static List<Integer> joinMangeOppgaver() {
    List<Callable<Integer>> funs = IntStream.range(0, 10000).mapToObj(x -> tregIdentitet(x, 500L)).toList();
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var tasks = funs.stream().map(scope::fork).toList();
        scope.join().throwIfFailed();
        return tasks.stream().map(StructuredTaskScope.Subtask::get).toList();
    } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}

// Kjør i maks ett sekund, samle verdiene vi kan samle og null ut resten
private static List<Integer> aksepterFeilendeJobber() {
    List<Callable<Integer>> funs = new java.util.ArrayList<>(IntStream.range(0, 10).mapToObj(x -> tregIdentitet(x, 500L)).toList());
    funs.add(tregIdentitet(11, 2000L));
    funs.add(() -> {
        Thread.sleep(2000L);
        throw new RuntimeException("Orker ikke mer");
    });
    try (var scope = new StructuredTaskScope()) {
        var tasks = funs.stream().map(x -> scope.fork(x)).toList();
        try {
            scope.joinUntil(Instant.now().plusSeconds(1));
        } catch (TimeoutException e) {
            scope.shutdown();
            scope.join();
        }
        return tasks.stream().map(x -> x.state().equals(StructuredTaskScope.Subtask.State.SUCCESS) ? (Integer) x.get() : null).toList();
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}

private static String klokkeslett() {
    return DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(ZonedDateTime.now());
}

// Nøstet structured task for å kjøre to raske oppgaver på rad samtidig som en treg oppgave
private static void oppgaverPaaRad() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        System.out.println(STR."\{klokkeslett()}\tStartet");
        StructuredTaskScope.Subtask<Integer> task10 = scope.fork(() -> {
            Thread.sleep(500L);
            System.out.println(STR."\{klokkeslett()}\tDeloppgave 1 ferdig");
            try (var subscope = new StructuredTaskScope.ShutdownOnFailure()) {
                var tasks = IntStream.range(0, 20).mapToObj(_ -> tregIdentitet(1, 500L)).map(subscope::fork)
                        .toList();
                subscope.join().throwIfFailed();
                System.out.println(STR."\{klokkeslett()}\tDeloppgave 2 ferdig");
                return tasks.stream().map(StructuredTaskScope.Subtask::get).mapToInt(x -> x).sum();
            }
        });

        var slowTask = scope.fork(() -> {
            Thread.sleep(2000L);
            System.out.println(STR."\{klokkeslett()}\tDeloppgave 3 ferdig");
            return 20;
        });
        scope.join().throwIfFailed();
        System.out.println(STR."\{klokkeslett()}\tAlt ferdig");
        System.out.println(STR."\{task10.get()} = \{slowTask.get()}");
    } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
