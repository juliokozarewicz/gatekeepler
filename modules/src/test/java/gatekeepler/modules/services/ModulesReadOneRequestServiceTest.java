package gatekeepler.modules.services;

import modules.dtos.UUIDValidationDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import modules.services.ModulesReadOneRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

        when(messageSource.getMessage(eq("response_get_data_success"), eq(null), any()))
            .thenReturn("success");
        when(messageSource.getMessage(eq("response_request_dont_exist"), eq(null), any()))
            .thenReturn("Não encontrado");
    }

    @Test
    void testExecute_RequestNotFound_ShouldThrowError() {
        UUID requestId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(requestId.toString());

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.empty());

        doThrow(new RuntimeException("Não encontrado"))
            .when(errorHandler)
            .customErrorThrow(eq(404), eq("Não encontrado"));

        assertThrows(RuntimeException.class, () ->
            service.execute(credentials, dto)
        );

        verify(modulesRequestRepository, times(1))
            .findByIdAndIdUser(eq(requestId), eq(userId.toString()));

        verify(errorHandler, times(1))
            .customErrorThrow(eq(404), eq("Não encontrado"));
    }

    @Test
    void testExecute_StatusAtivo_ShouldReturnSanitizedResponse() {
        UUID requestId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(requestId.toString());

        ModulesRequestEntity entity = new ModulesRequestEntity();
        entity.setId(requestId);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setProtocolNumber("PROTO123");
        entity.setModuleNamesRequested(List.of("mod1", "mod2"));
        entity.setJustification("just");
        entity.setUrgent(false);
        entity.setStatus("ativo");
        entity.setDenialReason(null);
        entity.setCancelReason(null);
        entity.setLinkedProtocol("OLD123");

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.of(entity));

        service.execute(credentials, dto);

        verify(modulesRequestRepository, times(1))
            .findByIdAndIdUser(eq(requestId), eq(userId.toString()));
    }

    @Test
    void testExecute_StatusNegado_ShouldReturnProperResponse() {
        UUID requestId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(requestId.toString());

        ModulesRequestEntity entity = new ModulesRequestEntity();
        entity.setId(requestId);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setProtocolNumber("PROTO123");
        entity.setModuleNamesRequested(List.of("mod1"));
        entity.setJustification("just");
        entity.setUrgent(false);
        entity.setStatus("negado");
        entity.setDenialReason("motivo X");

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.of(entity));

        service.execute(credentials, dto);

        verify(modulesRequestRepository, times(1))
            .findByIdAndIdUser(eq(requestId), eq(userId.toString()));
    }

    @Test
    void testExecute_StatusCancelado_ShouldReturnProperResponse() {
        UUID requestId = UUID.randomUUID();
        UUIDValidationDTO dto = new UUIDValidationDTO(requestId.toString());

        ModulesRequestEntity entity = new ModulesRequestEntity();
        entity.setId(requestId);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setProtocolNumber("PROTO123");
        entity.setModuleNamesRequested(List.of("mod1"));
        entity.setJustification("just");
        entity.setUrgent(false);
        entity.setStatus("cancelado");
        entity.setCancelReason("motivo cancel");

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.of(entity));

        service.execute(credentials, dto);

        verify(modulesRequestRepository, times(1))
            .findByIdAndIdUser(eq(requestId), eq(userId.toString()));
    }
}