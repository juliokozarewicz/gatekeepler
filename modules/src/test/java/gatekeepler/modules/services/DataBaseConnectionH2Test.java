package gatekeepler.modules.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
@ActiveProfiles("test")
class DataBaseConnectionH2Test {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testDatabaseConnectionIsSuccessful() {

        assertNotNull(dataSource, "The DataSource should not be null, indicating a failure in the database configuration.");
        assertNotNull(applicationContext, "The ApplicationContext should not be null.");

        try {

            String url = dataSource.getConnection().getMetaData().getURL();
            assertThat(url).contains("h2:mem");

        } catch (Exception e) {

            assertThat(false).as("Failed to get database connection").isTrue();

        }

    }
}