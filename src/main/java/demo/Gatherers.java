import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

// Stream gatherers https://openjdk.org/jeps/461
// Er dette transducers..?
public static void main(String[] args) {
    System.out.println(prosesserListe());

    System.out.println(mapConcurrent().size());;

    Gatherer<Integer, Void, Integer> inc = Gatherer.of(
            (_, element, downstream) -> downstream.push(element + 1));
    Gatherer<Integer, Void, Integer> mult = Gatherer.of(
            (_, element, downstream) -> downstream.push(element * 2));
    System.out.println(
            Stream.iterate(0, i -> i + 1).limit(10)
                    .gather(inc.andThen(mult).andThen(new First5())).toList()
    );

    System.out.println(Stream.of(1, 2, 2, 3, 4, 5, 5, 6, 7).gather(
            new Distinct()
    ).toList());
}

private static List<List<Integer>> prosesserListe() {
    return Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
            .gather(java.util.stream.Gatherers.windowFixed(2))
            .toList();
}

private static List<Integer> mapConcurrent() {
    return Stream.iterate(0, i -> i + 1).limit(1000).
            gather(java.util.stream.Gatherers.mapConcurrent(100, i -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return i;
            }))
            .toList();
}

// State-objektet til gathererne våre
static class State {
    public Integer value = null;
}

// Fjerner tall som repeterer seg!
static class Distinct implements Gatherer<Integer, State, Integer> {
    @Override public Supplier<State> initializer() {
        return State::new;
    }

    @Override public Integrator<State, Integer, Integer> integrator() {
        return Integrator.of((state, element, downstream) -> {
            if (Objects.equals(state.value, element)) {
                return true;
            } else {
                state.value = element;
                return downstream.push(element);
            }
        });
    }
}

// Tar kun første 5 elementer fra en liste
static class First5 implements Gatherer<Integer, State, Integer> {

    public Supplier<State> initializer() {
        return State::new;
    }

    @Override public Integrator<State, Integer, Integer> integrator() {
        return Integrator.of((state, element, downstream) -> {
            if (Objects.equals(state.value, 4)) {
                return false;
            }
            state.value = state.value == null ? 0 : state.value + 1;
            return downstream.push(element);
        });
    }
}
