final static ScopedValue<String> SCOPED = ScopedValue.newInstance();

final static ThreadLocal<String> TLOKAL = new ThreadLocal<>();

static void printScoped() {
    System.out.println(SCOPED.orElse("Ingen verdi"));
}

static void printTLocal() {
    System.out.println(TLOKAL.get());
}

public static void main(String[] args) throws InterruptedException {
    ScopedValue.where(SCOPED, "Hello")
            .run(() -> printScoped());
    printScoped();
    ScopedValue.runWhere(SCOPED, "World!", () -> printScoped());

    var t0 = Thread.startVirtualThread(() -> {
        ScopedValue.runWhere(SCOPED, "Funker", () -> printScoped());
    });

    t0.join();

    // Gammel metode: ThreadLocal
    // Merk setteren!
    var t1 = Thread.startVirtualThread(() -> {
        TLOKAL.set("World!");
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printTLocal();
    });
    var t2 = Thread.startVirtualThread(() -> {
        TLOKAL.set("Hello,");
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        printTLocal();
    });

    t1.join();
    t2.join();
}
