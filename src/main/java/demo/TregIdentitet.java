package demo;

import java.util.concurrent.Callable;

public class TregIdentitet {
    public static <T> Callable<T> tregIdentitet(T verdi) {
        return () -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                System.out.println("Tråd avbrutt!");
                throw new RuntimeException(e);
            }
            return verdi;
        };
    }

    public static <T> Callable<T> tregIdentitet(T verdi, Long treghet) {
        return () -> {
            try {
                Thread.sleep(treghet);
            } catch (InterruptedException e) {
                System.out.println("Tråd avbrutt!");
                throw new RuntimeException(e);
            }
            return verdi;
        };
    }

    public static void main(String[] args) throws Exception {
        // Returner seg selv, bare tregt!
        System.out.println(tregIdentitet("Hello, world!").call());
    }
}
