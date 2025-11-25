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
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class ModulesCreateRequestServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private ModulesRequestRepository modulesRequestRepository;
    @Mock private ModulesAllowedDepartmentsRepository allowedRepo;
    @Mock private ModulesRepository modulesRepository;
    @Mock private ErrorHandler errorHandler;
    @Mock private ModulesUtilsService utilsService;

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

    // ============================================================
    // INVALID LIST — NULL
    // ============================================================
    @Test
    void testInvalidModules_NullList_ThrowsAndCreatesDeniedRecord() {
        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(null, "just long enough", false);

        doThrow(new RuntimeException("Quantidade inválida"))
            .when(errorHandler).customErrorThrow(400, "Quantidade inválida");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(utilsService).createCommitRequestStatus(
            argThat(p -> p != null && p.length() > 5),
            eq("negado"),
            eq("Quantidade inválida"),
            eq(dto),
            eq(userId.toString())
        );
    }

    // ============================================================
    // INVALID LIST — EMPTY
    // ============================================================
    @Test
    void testInvalidModules_EmptyList_Throws() {
        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(List.of(), "just long enough", false);

        doThrow(new RuntimeException("Quantidade inválida"))
            .when(errorHandler).customErrorThrow(400, "Quantidade inválida");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));
        verify(errorHandler).customErrorThrow(400, "Quantidade inválida");
    }

    // ============================================================
    // INVALID LIST — >3 MODULES
    // ============================================================
    @Test
    void testInvalidModules_TooManyModules_Throws() {
        ModulesCreateRequestDTO dto =
            new ModulesCreateRequestDTO(List.of("a","b","c","d"), "just long enough", false);

        doThrow(new RuntimeException("Quantidade inválida"))
            .when(errorHandler).customErrorThrow(400, "Quantidade inválida");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));
        verify(errorHandler).customErrorThrow(400, "Quantidade inválida");
    }

    @Test
    void testActiveRequestAlreadyExists_FirstModule() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "financeiro"),
            "justification long enough 20+",
            false
        );

        ModulesRequestEntity existing = new ModulesRequestEntity();
        existing.setModuleNamesRequested(List.of("portal"));
        existing.setStatus("ativo");

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of(existing));

        doThrow(new RuntimeException("Já solicitado PORTAL"))
            .when(errorHandler).customErrorThrow(400, "Já solicitado PORTAL");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));
        verify(errorHandler).customErrorThrow(400, "Já solicitado PORTAL");
    }

    @Test
    void testActiveRequestAlreadyExists_SecondModule() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "relatorios"),
            "just long enough",
            false
        );

        ModulesRequestEntity reqA = new ModulesRequestEntity();
        reqA.setModuleNamesRequested(List.of("portal"));
        reqA.setStatus("inativo");

        ModulesRequestEntity reqB = new ModulesRequestEntity();
        reqB.setModuleNamesRequested(List.of("relatorios"));
        reqB.setStatus("ativo");

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of(reqA, reqB));

        doThrow(new RuntimeException("Já solicitado RELATORIOS"))
            .when(errorHandler).customErrorThrow(400, "Já solicitado RELATORIOS");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(errorHandler).customErrorThrow(400, "Já solicitado RELATORIOS");
    }

    @Test
    void testModuleMixedExistence_OneExistsOneNot_Throws() {
        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "misterio"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity portal = new ModulesEntity();
        portal.setName("portal");
        portal.setActive(true);

        when(modulesRepository.findByName("portal"))
            .thenReturn(Optional.of(portal));

        when(modulesRepository.findByName("misterio"))
            .thenReturn(Optional.empty());

        doThrow(new RuntimeException("Módulo inexistente"))
            .when(errorHandler).customErrorThrow(400, "Módulo inexistente");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));

        verify(modulesRepository).findByName("portal");
        verify(modulesRepository).findByName("misterio");
        verify(errorHandler).customErrorThrow(400, "Módulo inexistente");
    }

    @Test
    void testModuleInactive_Throws() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity inactive = new ModulesEntity();
        inactive.setName("portal");
        inactive.setActive(false);

        when(modulesRepository.findByName("portal"))
            .thenReturn(Optional.of(inactive));

        doThrow(new RuntimeException("Módulo inexistente"))
            .when(errorHandler).customErrorThrow(400, "Módulo inexistente");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));
        verify(errorHandler).customErrorThrow(400, "Módulo inexistente");
    }

    @Test
    void testPermissionError_OnSecondModule() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "relatorios"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity e1 = new ModulesEntity(); e1.setName("portal"); e1.setActive(true);
        ModulesEntity e2 = new ModulesEntity(); e2.setName("relatorios"); e2.setActive(true);

        when(modulesRepository.findByName("portal")).thenReturn(Optional.of(e1));
        when(modulesRepository.findByName("relatorios")).thenReturn(Optional.of(e2));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti"))
            .thenReturn(true);

        when(allowedRepo.existsByModuleNameAndDepartment("relatorios", "ti"))
            .thenReturn(false);

        doThrow(new RuntimeException("Sem permissão RELATORIOS"))
            .when(errorHandler).customErrorThrow(400, "Sem permissão RELATORIOS");

        assertThrows(RuntimeException.class, () -> service.execute(credentials, dto));
    }

    @Test
    void testSuccess_OneModule() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity portal = new ModulesEntity();
        portal.setName("portal");
        portal.setActive(true);

        when(modulesRepository.findByName("portal"))
            .thenReturn(Optional.of(portal));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti"))
            .thenReturn(true);

        ResponseEntity response = service.execute(credentials, dto);

        assertEquals(201, response.getStatusCodeValue());

        verify(utilsService).createCommitRequestStatus(
            argThat(p -> p != null && p.length() > 5),
            eq("ativo"),
            isNull(),
            eq(dto),
            eq(userId.toString())
        );
    }

    @Test
    void testSuccess_TwoModules() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "fin"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity e1 = new ModulesEntity(); e1.setName("portal"); e1.setActive(true);
        ModulesEntity e2 = new ModulesEntity(); e2.setName("fin"); e2.setActive(true);

        when(modulesRepository.findByName("portal")).thenReturn(Optional.of(e1));
        when(modulesRepository.findByName("fin")).thenReturn(Optional.of(e2));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti")).thenReturn(true);
        when(allowedRepo.existsByModuleNameAndDepartment("fin", "ti")).thenReturn(true);

        ResponseEntity response = service.execute(credentials, dto);

        assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void testSuccess_ThreeModules() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("portal", "fin", "rel"),
            "justification long enough",
            false
        );

        when(modulesRequestRepository.findByIdUser(eq(userId.toString())))
            .thenReturn(List.of());

        ModulesEntity e1 = new ModulesEntity(); e1.setName("portal"); e1.setActive(true);
        ModulesEntity e2 = new ModulesEntity(); e2.setName("fin"); e2.setActive(true);
        ModulesEntity e3 = new ModulesEntity(); e3.setName("rel"); e3.setActive(true);

        when(modulesRepository.findByName("portal")).thenReturn(Optional.of(e1));
        when(modulesRepository.findByName("fin")).thenReturn(Optional.of(e2));
        when(modulesRepository.findByName("rel")).thenReturn(Optional.of(e3));

        when(allowedRepo.existsByModuleNameAndDepartment("portal", "ti")).thenReturn(true);
        when(allowedRepo.existsByModuleNameAndDepartment("fin", "ti")).thenReturn(true);
        when(allowedRepo.existsByModuleNameAndDepartment("rel", "ti")).thenReturn(true);

        ResponseEntity response = service.execute(credentials, dto);

        assertEquals(201, response.getStatusCodeValue());
    }
}
