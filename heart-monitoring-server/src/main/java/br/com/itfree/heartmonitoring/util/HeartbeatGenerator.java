package br.com.itfree.heartmonitoring.util;

import java.util.concurrent.ThreadLocalRandom;

public class HeartbeatGenerator {

    public static int generateRandomBpm() {
        return ThreadLocalRandom.current().nextInt(50, 91);
    }

}