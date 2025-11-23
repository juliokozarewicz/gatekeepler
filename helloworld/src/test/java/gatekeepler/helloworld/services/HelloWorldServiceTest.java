package gatekeepler.helloworld.services;


import helloworld.services.HelloWorldService;
import helloworld.services.StandardResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;

class HelloWorldServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private HelloWorldService helloWorldService;

    private final String BASE_URL = "test-base-url";
    private final String TEST_MESSAGE = "test-message";
    private final String MOCKED_MESSAGE = "Hello world response";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(messageSource.getMessage(
            eq("response_get_data_success"),
            isNull(),
            any(Locale.class)
        )).thenReturn(MOCKED_MESSAGE);

        try {
            Field field = HelloWorldService.class.getDeclaredField("helloWorldBaseURL");
            field.setAccessible(true);
            field.set(helloWorldService, BASE_URL);
        } catch (Exception e) {

        }
    }


    @Test
    void testExecute_ShouldReturn200WithCorrectResponseStructure() {
        ResponseEntity<?> responseEntity = helloWorldService.execute(TEST_MESSAGE);

        assertEquals(200, responseEntity.getStatusCodeValue());

        Object body = responseEntity.getBody();
        assertNotNull(body);
        assertTrue(body instanceof StandardResponseService);

        StandardResponseService response = (StandardResponseService) body;

        assertEquals(200, response.getStatusCode());
        assertEquals("success", response.getStatusMessage());

        String expectedMessage = MOCKED_MESSAGE + " (" + TEST_MESSAGE + ")";
        assertEquals(expectedMessage, response.getMessage());

        Map<String, String> links = response.getLinks();
        assertNotNull(links);
        assertEquals(1, links.size());
        assertTrue(links.containsKey("self"));
        assertEquals("/" + BASE_URL, links.get("self"));

        assertNull(response.getField());
        assertNull(response.getData());
        assertNull(response.getMeta());
    }

    @Test
    void testExecute_EmptyMessage_ShouldHandleConcatenation() {
        String emptyMessage = "";

        ResponseEntity<?> responseEntity = helloWorldService.execute(emptyMessage);

        StandardResponseService response = (StandardResponseService) responseEntity.getBody();

        String expectedMessage = MOCKED_MESSAGE + " (" + emptyMessage + ")";
        assertEquals(expectedMessage, response.getMessage());

        assertEquals(200, response.getStatusCode());
        assertEquals("/" + BASE_URL, response.getLinks().get("self"));
    }

    @Test
    void testExecute_LinkStructureIsCorrect() {
        ResponseEntity<?> responseEntity = helloWorldService.execute(TEST_MESSAGE);

        StandardResponseService response = (StandardResponseService) responseEntity.getBody();
        Map<String, String> links = response.getLinks();

        assertNotNull(links);
        assertEquals(1, links.size());
        assertTrue(links.containsKey("self"));
        assertEquals("/" + BASE_URL, links.get("self"));
    }
}
