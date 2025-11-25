package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesCancelRequestDTO;
import gatekeepler.modules.dtos.UUIDValidationDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesCancelRequestServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @InjectMocks
    private ModulesCancelRequestService service;

    private UUID userId;
    private UUID requestId;
    private Map<String, Object> credentials;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        credentials = Map.of(
            "id", userId.toString(),
            "email", "user@test.com",
            "department", "ti"
        );

        when(messageSource.getMessage(eq("response_request_dont_exist"), eq(null), eq(Locale.getDefault())))
            .thenReturn("not found");
        when(messageSource.getMessage(eq("response_request_not_active"), eq(null), eq(Locale.getDefault())))
            .thenReturn("not active");
        when(messageSource.getMessage(eq("response_request_cancel_success"), eq(null), eq(Locale.getDefault())))
            .thenReturn("cancel ok");
    }

    @Test
    void testCancelActiveRequest() {
        ModulesRequestEntity entity = new ModulesRequestEntity();
        entity.setId(requestId);
        entity.setIdUser(userId.toString());
        entity.setStatus("ativo");
        entity.setCreatedAt(Instant.now());

        when(modulesRequestRepository.findByIdAndIdUser(requestId, userId.toString()))
            .thenReturn(Optional.of(entity));
        when(modulesRequestRepository.save(entity)).thenReturn(entity);

        UUIDValidationDTO uuidDTO = new UUIDValidationDTO(requestId.toString());
        ModulesCancelRequestDTO cancelDTO = new ModulesCancelRequestDTO("Justificativa XYZ");

        ResponseEntity response = service.execute(credentials, uuidDTO, cancelDTO);

        assertEquals(200, response.getStatusCodeValue());
        StandardResponseService body = (StandardResponseService) response.getBody();
        assertNotNull(body);
        assertEquals("cancel ok", body.getMessage());

        assertEquals("cancelado", entity.getStatus());
        assertEquals("Justificativa XYZ", entity.getCancelReason());
        assertNotNull(entity.getUpdatedAt());

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(requestId, userId.toString());
        verify(modulesRequestRepository, times(1)).save(entity);
    }

    @Test
    void testRequestNotFound() {
        UUIDValidationDTO uuidDTO = new UUIDValidationDTO(requestId.toString());
        ModulesCancelRequestDTO cancelDTO = new ModulesCancelRequestDTO("Motivo AAA");

        when(modulesRequestRepository.findByIdAndIdUser(requestId, userId.toString()))
            .thenReturn(Optional.empty());
        doThrow(new RuntimeException("not found"))
            .when(errorHandler).customErrorThrow(eq(404), eq("not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            service.execute(credentials, uuidDTO, cancelDTO)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(errorHandler, times(1)).customErrorThrow(eq(404), eq("not found"));
        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(requestId, userId.toString());
    }

    @Test
    void testExecute_RequestNotActive_ShouldThrow400() {
        ModulesRequestEntity entity = new ModulesRequestEntity();
        entity.setId(requestId);
        entity.setIdUser(userId.toString());
        entity.setStatus("negado");

        when(modulesRequestRepository.findByIdAndIdUser(requestId, userId.toString()))
            .thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("not active"))
            .when(errorHandler).customErrorThrow(eq(400), eq("not active"));

        UUIDValidationDTO uuidDTO = new UUIDValidationDTO(requestId.toString());
        ModulesCancelRequestDTO cancelDTO = new ModulesCancelRequestDTO("Motivo BBB");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            service.execute(credentials, uuidDTO, cancelDTO)
        );

        assertTrue(exception.getMessage().contains("not active"));
        verify(errorHandler, times(1)).customErrorThrow(eq(400), eq("not active"));
        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(requestId, userId.toString());
    }
}
