package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesCancelRequestDTO;
import gatekeepler.modules.dtos.UUIDValidationDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ModulesCancelRequestServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @InjectMocks
    private ModulesCancelRequestService modulesCancelRequestService;

    private UUID userId;
    private String email;
    private String department;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        email = "ti@email.com";
        department = "TI";
        requestId = UUID.randomUUID();
    }

    @Test
    void testExecute_RequestNotFound_ShouldThrowError() {
        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.empty());

        doThrow(new RuntimeException("not found"))
            .when(errorHandler).customErrorThrow(eq(404), eq("not found"));

        UUIDValidationDTO uuidValidationDTO = new UUIDValidationDTO(requestId.toString());
        ModulesCancelRequestDTO cancelRequestDTO = new ModulesCancelRequestDTO("Justificativa");

        assertThrows(RuntimeException.class, () ->
            modulesCancelRequestService.execute(
                Map.of("id", userId.toString(), "email", email, "department", department),
                uuidValidationDTO,
                cancelRequestDTO
            )
        );

        verify(modulesRequestRepository, times(1))
            .findByIdAndIdUser(eq(requestId), eq(userId.toString()));

        verify(errorHandler, times(1))
            .customErrorThrow(eq(404), isNull());
    }

}