package gatekeepler.modules.services;

import gatekeepler.modules.dtos.UUIDValidationDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesEntity;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesRenewRequestServiceTest {

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
    private ModulesRenewRequestService service;

    private UUID userId;
    private UUID requestId;
    private Map<String, Object> credentials;
    private UUIDValidationDTO uuidValidationDTO;
    private Locale locale;
    private ModulesRequestEntity activeRequestEntity;

    private final String MODULE_A = "portal do colaborador";
    private final String MODULE_B = "relatórios gerenciais";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "modulesBaseURL", "v1/modules");

        userId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        locale = LocaleContextHolder.getLocale();

        credentials = Map.of(
            "id", userId.toString(),
            "email", "ti@email.com",
            "department", "ti"
        );
        uuidValidationDTO = new UUIDValidationDTO(requestId.toString());

        activeRequestEntity = new ModulesRequestEntity();
        activeRequestEntity.setId(requestId);
        activeRequestEntity.setModuleNamesRequested(List.of(MODULE_A, MODULE_B));
        activeRequestEntity.setStatus("ativo");
        activeRequestEntity.setIdUser(userId.toString());

        Instant renewalWindowDate = ZonedDateTime.now(ZoneOffset.UTC)
            .minus(180, ChronoUnit.DAYS)
            .plus(15, ChronoUnit.DAYS)
            .toInstant();
        activeRequestEntity.setCreatedAt(renewalWindowDate);

        when(messageSource.getMessage(eq("response_request_dont_exist"), eq(null), eq(locale)))
            .thenReturn("Requisição não encontrada");
        when(messageSource.getMessage(eq("response_request_not_active"), eq(null), eq(locale)))
            .thenReturn("Requisição não está ativa");
        when(messageSource.getMessage(eq("response_request_too_far_to_renew"), eq(null), eq(locale)))
            .thenReturn("Renovação muito antecipada");
        when(messageSource.getMessage(eq("request_modules_dont_exist"), eq(null), eq(locale)))
            .thenReturn("Módulos inválidos");
        when(messageSource.getMessage(eq("request_module_not_allowed_error"), eq(null), eq(locale)))
            .thenReturn("Módulo não permitido para o departamento");
        when(messageSource.getMessage(eq("response_renew_success"), eq(null), eq(locale)))
            .thenReturn("Renovação realizada com sucesso. Protocolo:");
    }

    @Test
    void testExecute_SuccessCase_ShouldRenewRequestAndReturn201() {
        ArgumentCaptor<String> protocolCaptor = ArgumentCaptor.forClass(String.class);

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString())))
            .thenReturn(Optional.of(activeRequestEntity));

        ModulesEntity moduleAEntity = new ModulesEntity(); moduleAEntity.setActive(true);
        ModulesEntity moduleBEntity = new ModulesEntity(); moduleBEntity.setActive(true);
        when(modulesRepository.findByName(eq(MODULE_A))).thenReturn(Optional.of(moduleAEntity));
        when(modulesRepository.findByName(eq(MODULE_B))).thenReturn(Optional.of(moduleBEntity));

        when(modulesAllowedDepartmentsRepository.existsByModuleNameAndDepartment(eq(MODULE_A), eq("ti"))).thenReturn(true);
        when(modulesAllowedDepartmentsRepository.existsByModuleNameAndDepartment(eq(MODULE_B), eq("ti"))).thenReturn(true);

        doNothing().when(modulesUtilsService).renewRequestStatus(
            protocolCaptor.capture(),
            eq("ativo"),
            eq(null),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );

        ResponseEntity response = service.execute(credentials, uuidValidationDTO);
        String capturedProtocol = protocolCaptor.getValue();

        StandardResponseService responseBody = (StandardResponseService) response.getBody();

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals("Renovação realizada com sucesso. Protocolo: " + capturedProtocol, responseBody.getMessage());
        assertEquals(2, responseBody.getLinks().size());

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_A));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_B));
        verify(modulesAllowedDepartmentsRepository, times(1)).existsByModuleNameAndDepartment(eq(MODULE_A), eq("ti"));
        verify(modulesAllowedDepartmentsRepository, times(1)).existsByModuleNameAndDepartment(eq(MODULE_B), eq("ti"));
        verify(modulesUtilsService, times(1)).renewRequestStatus(
            eq(capturedProtocol), eq("ativo"), eq(null),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
        verify(errorHandler, never()).customErrorThrow(eq(400), eq("QUALQUER MOTIVO"));
    }

    @Test
    void testExecute_RequestNotFound_ShouldThrowError404() {
        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString()))).thenReturn(Optional.empty());

        final String expectedErrorMessage = "Requisição não encontrada";
        doThrow(new RuntimeException(expectedErrorMessage))
            .when(errorHandler)
            .customErrorThrow(eq(404), eq(expectedErrorMessage));

        assertThrows(RuntimeException.class, () -> service.execute(credentials, uuidValidationDTO));

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(errorHandler, times(1)).customErrorThrow(eq(404), eq(expectedErrorMessage));
        verify(modulesUtilsService, never()).renewRequestStatus(
            eq("QUALQUER PROTOCOLO"),
            eq("negado"),
            eq("QUALQUER MOTIVO"),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
    }

    @Test
    void testExecute_RequestNotActive_ShouldThrowError400() {
        ModulesRequestEntity deniedRequest = new ModulesRequestEntity();
        deniedRequest.setStatus("negado");
        deniedRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).minus(10, ChronoUnit.DAYS).toInstant());
        deniedRequest.setModuleNamesRequested(List.of(MODULE_A));

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString()))).thenReturn(Optional.of(deniedRequest));

        final String expectedErrorMessage = "Requisição não está ativa";
        doThrow(new RuntimeException(expectedErrorMessage))
            .when(errorHandler)
            .customErrorThrow(eq(400), eq(expectedErrorMessage));

        assertThrows(RuntimeException.class, () -> service.execute(credentials, uuidValidationDTO));

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(errorHandler, times(1)).customErrorThrow(eq(400), eq(expectedErrorMessage));
        verify(modulesUtilsService, never()).renewRequestStatus(
            eq("QUALQUER PROTOCOLO"),
            eq("negado"),
            eq("QUALQUER MOTIVO"),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
    }

    @Test
    void testExecute_RequestTooFarToRenew_ShouldThrowError400() {
        ModulesRequestEntity farRequest = new ModulesRequestEntity();
        farRequest.setStatus("ativo");
        farRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).minus(10, ChronoUnit.DAYS).toInstant());
        farRequest.setModuleNamesRequested(List.of(MODULE_A));

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString()))).thenReturn(Optional.of(farRequest));

        final String expectedErrorMessage = "Renovação muito antecipada";
        doThrow(new RuntimeException(expectedErrorMessage))
            .when(errorHandler)
            .customErrorThrow(eq(400), eq(expectedErrorMessage));

        assertThrows(RuntimeException.class, () -> service.execute(credentials, uuidValidationDTO));

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(errorHandler, times(1)).customErrorThrow(eq(400), eq(expectedErrorMessage));
        verify(modulesUtilsService, never()).renewRequestStatus(
            eq("QUALQUER PROTOCOLO"),
            eq("negado"),
            eq("QUALQUER MOTIVO"),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
    }

    @Test
    void testExecute_ModulesDontExistOrInactive_ShouldCallRenewStatusDenyAndThrowError400() {
        ArgumentCaptor<String> protocolCaptor = ArgumentCaptor.forClass(String.class);

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString()))).thenReturn(Optional.of(activeRequestEntity));

        ModulesEntity moduleBInactive = new ModulesEntity(); moduleBInactive.setActive(false);
        when(modulesRepository.findByName(eq(MODULE_A))).thenReturn(Optional.empty());
        when(modulesRepository.findByName(eq(MODULE_B))).thenReturn(Optional.of(moduleBInactive));

        final String expectedErrorMessage = "Módulos inválidos";
        doThrow(new RuntimeException(expectedErrorMessage))
            .when(errorHandler)
            .customErrorThrow(eq(400), eq(expectedErrorMessage));

        doNothing().when(modulesUtilsService).renewRequestStatus(
            protocolCaptor.capture(),
            eq("negado"),
            eq(expectedErrorMessage),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );

        assertThrows(RuntimeException.class, () -> service.execute(credentials, uuidValidationDTO));

        String capturedProtocol = protocolCaptor.getValue();

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_A));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_B));
        verify(modulesUtilsService, times(1)).renewRequestStatus(
            eq(capturedProtocol),
            eq("negado"),
            eq(expectedErrorMessage),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
        verify(errorHandler, times(1)).customErrorThrow(eq(400), eq(expectedErrorMessage));
        verify(modulesAllowedDepartmentsRepository, never()).existsByModuleNameAndDepartment(eq(MODULE_A), eq("ti"));
    }

    @Test
    void testExecute_ModuleNotAllowedForDepartment_ShouldCallRenewStatusDenyAndThrowError400() {
        ArgumentCaptor<String> protocolCaptor = ArgumentCaptor.forClass(String.class);

        when(modulesRequestRepository.findByIdAndIdUser(eq(requestId), eq(userId.toString()))).thenReturn(Optional.of(activeRequestEntity));

        ModulesEntity moduleAEntity = new ModulesEntity(); moduleAEntity.setActive(true);
        ModulesEntity moduleBEntity = new ModulesEntity(); moduleBEntity.setActive(true);
        when(modulesRepository.findByName(eq(MODULE_A))).thenReturn(Optional.of(moduleAEntity));
        when(modulesRepository.findByName(eq(MODULE_B))).thenReturn(Optional.of(moduleBEntity));

        when(modulesAllowedDepartmentsRepository.existsByModuleNameAndDepartment(eq(MODULE_A), eq("ti"))).thenReturn(true);
        when(modulesAllowedDepartmentsRepository.existsByModuleNameAndDepartment(eq(MODULE_B), eq("ti"))).thenReturn(false);

        final String expectedDenialMessage = "Módulo não permitido para o departamento " + MODULE_B.toUpperCase();
        doThrow(new RuntimeException(expectedDenialMessage))
            .when(errorHandler)
            .customErrorThrow(eq(400), eq(expectedDenialMessage));

        doNothing().when(modulesUtilsService).renewRequestStatus(
            protocolCaptor.capture(),
            eq("negado"),
            eq(expectedDenialMessage),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );

        assertThrows(RuntimeException.class, () -> service.execute(credentials, uuidValidationDTO));

        String capturedProtocol = protocolCaptor.getValue();

        verify(modulesRequestRepository, times(1)).findByIdAndIdUser(eq(requestId), eq(userId.toString()));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_A));
        verify(modulesRepository, times(1)).findByName(eq(MODULE_B));
        verify(modulesAllowedDepartmentsRepository, times(1)).existsByModuleNameAndDepartment(eq(MODULE_A), eq("ti"));
        verify(modulesAllowedDepartmentsRepository, times(1)).existsByModuleNameAndDepartment(eq(MODULE_B), eq("ti"));
        verify(modulesUtilsService, times(1)).renewRequestStatus(
            eq(capturedProtocol),
            eq("negado"),
            eq(expectedDenialMessage),
            argThat(entity -> entity.getId().equals(requestId)),
            eq(userId.toString())
        );
        verify(errorHandler, times(1)).customErrorThrow(eq(400), eq(expectedDenialMessage));
    }
}