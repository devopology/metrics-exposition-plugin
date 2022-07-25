import io.prometheus.client.Gauge;

public class TestApplication {

    public static void main(String[] args) throws Exception {
        System.out.println("application is running");
        System.out.println("creating gauge [fake]");

        Gauge gauge = Gauge.build().name("fake").help("fake help").register();

        System.out.println("creating gauge [fake] by 1 every 10 seconds");

        while (true) {
            Thread.currentThread().sleep(10000);
            System.out.println("incrementing gauge [fake]");
            gauge.inc();
            System.out.println("gauge [fake] = [" + gauge.get() + "]");
        }
    }
}
