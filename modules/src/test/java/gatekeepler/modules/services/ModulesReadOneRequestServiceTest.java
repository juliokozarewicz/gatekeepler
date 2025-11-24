package gatekeepler.modules.services;

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

class ModulesReadOneRequestServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @InjectMocks
    private ModulesReadOneRequestService service;

    private UUID userId;
    private Map<String, Object> credentials;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();

        credentials = Map.of(
            "id", userId.toString(),
            "email", "ti@email.com",
            "department", "ti"
        );

        when(messageSource.getMessage(eq("response_get_data_success"), eq(null), eq(Locale.getDefault())))
            .thenReturn("success");

        when(messageSource.getMessage(eq("response_request_dont_exist"), eq(null), eq(Locale.getDefault())))
            .thenReturn("Não encontrado");
    }

    @Test
    void testExecute_RequestNotFound_ShouldThrowError() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.empty());

        doThrow(new RuntimeException("Não encontrado"))
            .when(errorHandler)
            .customErrorThrow(eq(404), eq("Não encontrado"));

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(modulesRequestRepository).findByIdAndIdUser(eq(reqId), eq(userId.toString()));
        verify(errorHandler).customErrorThrow(eq(404), eq("Não encontrado"));
    }

    private ModulesRequestEntity baseEntity(UUID id, String status) {
        ModulesRequestEntity e = new ModulesRequestEntity();
        e.setId(id);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        e.setProtocolNumber("PROTO");
        e.setModuleNamesRequested(List.of("mod1"));
        e.setJustification("just");
        e.setUrgent(false);
        e.setStatus(status);
        return e;
    }

    @Test
    void testExecute_StatusAtivo_WithLinkedProtocol() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "ativo");
        e.setLinkedProtocol("OLD123");

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        ResponseEntity response = service.execute(credentials, dto);

        StandardResponseService body = (StandardResponseService) response.getBody();
        Map<String, Object> data = (Map<String, Object>) body.getData();

        assertEquals("OLD123", data.get("linkedProtocol"));
        assertFalse(data.containsKey("denialReason"));
        assertFalse(data.containsKey("cancelReason"));

        verify(modulesRequestRepository).findByIdAndIdUser(eq(reqId), eq(userId.toString()));
    }

    @Test
    void testExecute_StatusAtivo_NoLinkedProtocol() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "ativo");
        e.setLinkedProtocol(null);

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertFalse(data.containsKey("linkedProtocol"));
        assertFalse(data.containsKey("denialReason"));
        assertFalse(data.containsKey("cancelReason"));
    }

    @Test
    void testExecute_StatusNegado_WithReason() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "negado");
        e.setDenialReason("motivo X");

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertEquals("motivo X", data.get("denialReason"));
        assertFalse(data.containsKey("cancelReason"));
        assertFalse(data.containsKey("linkedProtocol"));
    }

    @Test
    void testExecute_StatusNegado_NoReason() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "negado");
        e.setDenialReason(null);

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertNull(data.get("denialReason"));
        assertFalse(data.containsKey("cancelReason"));
        assertFalse(data.containsKey("linkedProtocol"));
    }

    @Test
    void testExecute_StatusCancelado_WithReason() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "cancelado");
        e.setCancelReason("cancel X");

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertEquals("cancel X", data.get("cancelReason"));
        assertFalse(data.containsKey("denialReason"));
        assertFalse(data.containsKey("linkedProtocol"));
    }

    @Test
    void testExecute_StatusCancelado_NoReason() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "cancelado");
        e.setCancelReason(null);

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertNull(data.get("cancelReason"));
        assertFalse(data.containsKey("denialReason"));
        assertFalse(data.containsKey("linkedProtocol"));
    }

    @Test
    void testExecute_UnknownStatus() {

        UUID reqId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(reqId.toString());

        ModulesRequestEntity e = baseEntity(reqId, "em_analise");

        when(modulesRequestRepository.findByIdAndIdUser(eq(reqId), eq(userId.toString())))
            .thenReturn(Optional.of(e));

        Map<String, Object> data = (Map<String, Object>)
            ((StandardResponseService) service.execute(credentials, dto).getBody()).getData();

        assertFalse(data.containsKey("linkedProtocol"));
        assertFalse(data.containsKey("denialReason"));
        assertFalse(data.containsKey("cancelReason"));
    }
}
