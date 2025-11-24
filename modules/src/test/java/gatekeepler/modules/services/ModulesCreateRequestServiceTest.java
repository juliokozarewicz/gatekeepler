package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesCreateRequestDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ModulesCreateRequestServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @Mock
    private ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository;

    @Mock
    private ModulesRepository modulesRepository;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesUtilsService modulesUtilsService;

    @InjectMocks
    private ModulesCreateRequestService service;

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

        when(
            messageSource.getMessage(
                eq("response_many_modules_error"),
                isNull(),
                any(Locale.class)
            )
        ).thenReturn("Quantidade inválida");
    }

    @Test
    void testExecute_InvalidModules_ShouldThrowError() {
        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of(),
            "justificação válida e longa o suficiente",
            false
        );

        doThrow(new RuntimeException("Quantidade inválida"))
            .when(errorHandler)
            .customErrorThrow(eq(400), eq("Quantidade inválida"));

        assertThrows(RuntimeException.class, () ->
            service.execute(credentials, dto)
        );

        verify(modulesUtilsService, times(1))
            .createCommitRequestStatus(
                anyString(),
                eq("negado"),
                eq("Quantidade inválida"),
                eq(dto),
                eq(userId.toString())
            );

        verify(errorHandler, times(1))
            .customErrorThrow(eq(400), eq("Quantidade inválida"));
    }
}
