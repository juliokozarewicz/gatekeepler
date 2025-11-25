package gatekeepler.modules.services;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class StandardResponseServiceTest {

    private final int STATUS_CODE = 200;
    private final String STATUS_MESSAGE = "success";
    private final String MESSAGE = "Operação realizada com sucesso.";
    private final String FIELD = "username";
    private final String DATA_VALUE = "TestPayload";
    private final Map<String, Object> META_DATA = Map.of("version", 1.0);
    private final Map<String, String> LINKS_DATA = Map.of("self", "/api/resource/1");

    @Test
    void testBuild_FullConfiguration_ShouldSetAllFieldsCorrectly() {
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(STATUS_CODE)
            .statusMessage(STATUS_MESSAGE)
            .message(MESSAGE)
            .field(FIELD)
            .data(DATA_VALUE)
            .meta(META_DATA)
            .links(LINKS_DATA)
            .build();

        assertEquals(STATUS_CODE, response.getStatusCode());
        assertEquals(STATUS_MESSAGE, response.getStatusMessage());
        assertEquals(MESSAGE, response.getMessage());
        assertEquals(FIELD, response.getField());
        assertEquals(DATA_VALUE, response.getData());
        assertEquals(META_DATA, response.getMeta());
        assertEquals(LINKS_DATA, response.getLinks());

        assertNotNull(response.getMessage());
        assertNotNull(response.getField());
        assertNotNull(response.getMeta());
        assertNotNull(response.getLinks());
    }

    @Test
    void testBuild_MinimumConfiguration_ShouldHandleOptionalFieldsAsNull() {
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(STATUS_CODE)
            .statusMessage(STATUS_MESSAGE)
            .build();

        assertEquals(STATUS_CODE, response.getStatusCode());
        assertEquals(STATUS_MESSAGE, response.getStatusMessage());

        assertNull(response.getMessage());
        assertNull(response.getField());
        assertNull(response.getData());
        assertNull(response.getMeta());
        assertNull(response.getLinks());
    }

    @Test
    void testBuild_EmptyAndNullFields_ShouldBeTreatedAsNull() {
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("created")
            .message("")
            .field("")
            .data(null)
            .meta(new HashMap<>())
            .links(new HashMap<>())
            .build();

        assertNull(response.getMessage());
        assertNull(response.getField());
        assertNull(response.getMeta());
        assertNull(response.getLinks());
        assertNull(response.getData());
    }
}