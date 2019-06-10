package ua.kernel.dabbd.tracking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kernel.dabbd.route.tracking")
public class RouteTrackingProperties {

    private String bootstrapServers;

    private String eventsTopic;
    private String eventsConsumerGroup;
    private String eventsAutoOffsetResetConfig = "earliest";
    private Integer eventsConcurrency;

    private String waybillTopic;
    private String waybillConsumerGroup;
    private String waybillAutoOffsetResetConfig = "earliest";
    private Integer waybillConcurrency;

    private String triggersTopic;

    static {
        System.out.println("=>>>> RouteTrackingProperties");
    }

}

