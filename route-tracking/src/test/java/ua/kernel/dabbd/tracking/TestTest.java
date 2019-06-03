package ua.kernel.dabbd.tracking;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestTest {

    @Test
    public void converTimestampToLocalDateTime() {
        long timestamp = 1544048665322l;
        LocalDateTime triggerTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId());
        System.out.println("Result: " + triggerTime);
        assertThat(triggerTime.toString(), is("2018-12-06T00:24:25.322"));
//        1544048665322

    }
}
