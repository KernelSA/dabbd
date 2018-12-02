package ua.kernel.dabbd.commons.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("ua.kernel.dabbd.commons.*")
@EnableJpaRepositories("ua.kernel.dabbd.commons.*")
public class RepositoryConfig {


}
