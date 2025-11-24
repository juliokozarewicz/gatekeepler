package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesCreateRequestDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesEntity;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesCreateRequestServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @Mock
    private ModulesAllowedDepartmentsRepository allowedRepo;

    @Mock
    private ModulesRepository modulesRepository;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesUtilsService utilsService;

    @InjectMocks
    private ModulesCreateRequestService service;

    private UUID userId;
    private Map<String, Object> credentials;
    private Locale locale;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        locale = Locale.getDefault();

        credentials = Map.of(
            "id", userId.toString(),
            "email", "email@empresa.com",
            "department", "ti"
        );

        when(messageSource.getMessage(eq("response_many_modules_error"), eq(null), eq(locale)))
            .thenReturn("Quantidade inválida");

        when(messageSource.getMessage(eq("response_already_requested_error"), eq(null), eq(locale)))
            .thenReturn("Já solicitado");

        when(messageSource.getMessage(eq("request_modules_dont_exist"), eq(null), eq(locale)))
            .thenReturn("Módulo inexistente");

        when(messageSource.getMessage(eq("request_module_not_allowed_error"), eq(null), eq(locale)))
            .thenReturn("Sem permissão");

        when(messageSource.getMessage(eq("response_request_aproved"), eq(null), eq(locale)))
            .thenReturn("Criado");
    }

    @Test
    void testInvalidModulesThrows() {

        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of(), "JUST", false);

        doThrow(new RuntimeException("Quantidade inválida"))
            .when(errorHandler).customErrorThrow(400, "Quantidade inválida");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(utilsService, times(1))
            .createCommitRequestStatus(
                argThat(value -> value != null && value.length() > 0),
                eq("negado"),
                eq("Quantidade inválida"),
                eq(dto),
                eq(userId.toString())
            );

        verify(errorHandler, times(1))
            .customErrorThrow(400, "Quantidade inválida");
    }

    @Test
    void testActiveRequestAlreadyExists() {

        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of("portal"), "JUST", false);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setModuleNamesRequested(List.of("portal"));
        req.setStatus("ativo");

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of(req));

        doThrow(new RuntimeException("Já solicitado PORTAL"))
            .when(errorHandler).customErrorThrow(400, "Já solicitado PORTAL");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(errorHandler).customErrorThrow(400, "Já solicitado PORTAL");
    }

    @Test
    void testModuleDoesNotExist() {

        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of("fin"), "JUST", false);

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        when(modulesRepository.findByName("fin"))
            .thenReturn(Optional.empty());

        doThrow(new RuntimeException("Módulo inexistente"))
            .when(errorHandler).customErrorThrow(400, "Módulo inexistente");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(errorHandler).customErrorThrow(400, "Módulo inexistente");
    }

    @Test
    void testDepartmentNotAllowed() {

        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of("portal"), "JUST", false);

        ModulesEntity entity = new ModulesEntity();
        entity.setName("portal");
        entity.setActive(true);

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        when(modulesRepository.findByName("portal"))
            .thenReturn(Optional.of(entity));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti"))
            .thenReturn(false);

        doThrow(new RuntimeException("Sem permissão PORTAL"))
            .when(errorHandler).customErrorThrow(400, "Sem permissão PORTAL");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(errorHandler).customErrorThrow(400, "Sem permissão PORTAL");
    }

    @Test
    void testSuccess() {

        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of("portal"), "JUST", false);

        ModulesEntity entity = new ModulesEntity();
        entity.setName("portal");
        entity.setActive(true);

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        when(modulesRepository.findByName("portal"))
            .thenReturn(Optional.of(entity));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti"))
            .thenReturn(true);

        ResponseEntity response = service.execute(credentials, dto);

        assertEquals(201, response.getStatusCodeValue());

        verify(utilsService).createCommitRequestStatus(
            argThat(v -> v != null && v.length() > 0),
            eq("ativo"),
            eq(null),
            eq(dto),
            eq(userId.toString())
        );
    }
}
