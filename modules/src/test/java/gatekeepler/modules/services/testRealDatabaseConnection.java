package gatekeepler.modules.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class testRealDatabaseConnection {

    @Autowired
    private DataSource dataSource;

    @Test
    void testRealDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");

        try (Connection conn = dataSource.getConnection()) {
            assertFalse(conn.isClosed(), "Connection should be open");

            ResultSet rs = conn.getMetaData().getSchemas();
            boolean foundSchema = false;
            while (rs.next()) {
                if ("modules".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                    foundSchema = true;
                    break;
                }
            }
            assertTrue(foundSchema, "Schema 'modules' should exist in the database");
        }
    }

}
