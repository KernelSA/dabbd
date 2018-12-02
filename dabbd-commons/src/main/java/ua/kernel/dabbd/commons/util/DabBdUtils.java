package ua.kernel.dabbd.commons.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DabBdUtils {

    public static void someUtil() {
        log.info("UTILS!");
    }


    public static void logSystemProperties() {
        log.info("------------------------------------------");
        log.info("System Properties: ");
        System.getProperties().entrySet()
                .forEach(objectObjectEntry ->
                        log.info(">>>>\t" + objectObjectEntry.getKey() + " : " + objectObjectEntry.getValue()));
        log.info("------------------------------------------");
    }


}
