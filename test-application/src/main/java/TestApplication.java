/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.prometheus.client.Gauge;

/**
 * Class to implement a simple test application that creates a single Gauge and increments it
 */
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
