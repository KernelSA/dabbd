package ua.kernel.dabbd.commons;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DabBdUtils {

    public static void someUtil() {
        log.info("UTILS!");
    }

}
