package gatekeepler.modules.services;

import modules.dtos.ModulesCreateRequestDTO;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import modules.services.ModulesUtilsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ModulesUtilsServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @InjectMocks
    private ModulesUtilsService modulesUtilsService;

    private Locale locale;
    private final String MODULE_A = "ModULo A";
    private final String MODULE_B = "MODulo B";
    private final String PROTOCOL_NUMBER = "SOL-20251123-ABCD";
    private final String ID_USER = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locale = LocaleContextHolder.getLocale();

        when(messageSource.getMessage(
            eq("response_reason_renew_success"),
            eq(null),
            eq(locale)
        )).thenReturn("Renovado com o protocolo");
    }

    @Test
    void testGenerateProtocolNumber_ShouldReturnCorrectFormat() {
        Instant fixedInstant = ZonedDateTime.of(2025, 11, 23, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String protocol = ModulesUtilsService.generateProtocolNumber(fixedInstant);

        assertTrue(protocol.startsWith("SOL-20251123-"));

        assertEquals(17, protocol.length());

        String uniquePart = protocol.substring(13);
        assertEquals(4, uniquePart.length());
        assertTrue(uniquePart.matches("[A-Z0-9]+"));
    }

    @Test
    void testCreateCommitRequestStatus_Success_ShouldSaveNewRequest() {
        ArgumentCaptor<ModulesRequestEntity> requestCaptor = ArgumentCaptor.forClass(ModulesRequestEntity.class);

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of(MODULE_A, MODULE_B),
            "justificativa de criação",
            true
        );
        String status = "ativo";
        String denialReason = null;

        modulesUtilsService.createCommitRequestStatus(
            PROTOCOL_NUMBER,
            status,
            denialReason,
            dto,
            ID_USER
        );

        verify(modulesRequestRepository, times(1)).save(requestCaptor.capture());

        ModulesRequestEntity savedRequest = requestCaptor.getValue();

        assertNotNull(savedRequest.getId());
        assertNotNull(savedRequest.getCreatedAt());
        assertNotNull(savedRequest.getUpdatedAt());
        assertEquals(PROTOCOL_NUMBER, savedRequest.getProtocolNumber());
        assertEquals(ID_USER, savedRequest.getIdUser());
        assertEquals(status, savedRequest.getStatus());
        assertEquals(denialReason, savedRequest.getDenialReason());
        assertEquals(dto.justification(), savedRequest.getJustification());
        assertTrue(savedRequest.isUrgent());

        assertEquals(2, savedRequest.getModuleNamesRequested().size());
        assertTrue(savedRequest.getModuleNamesRequested().contains(MODULE_A.toLowerCase()));
        assertTrue(savedRequest.getModuleNamesRequested().contains(MODULE_B.toLowerCase()));
    }

    @Test
    void testRenewRequestStatus_Success_ShouldUpdateOldAndSaveNewRequest() {
        ArgumentCaptor<ModulesRequestEntity> oldRequestCaptor = ArgumentCaptor.forClass(ModulesRequestEntity.class);

        ModulesRequestEntity existingRequest = new ModulesRequestEntity();
        existingRequest.setId(UUID.randomUUID());
        existingRequest.setProtocolNumber("OLD-PROTO-001");
        existingRequest.setStatus("ativo");
        existingRequest.setJustification("justificativa antiga");
        existingRequest.setUrgent(false);
        existingRequest.setModuleNamesRequested(List.of(MODULE_A.toLowerCase()));
        existingRequest.setIdUser(ID_USER);

        Instant oldCreationTime = ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60).toInstant();
        existingRequest.setCreatedAt(oldCreationTime);
        existingRequest.setUpdatedAt(oldCreationTime);

        String newStatus = "ativo";
        String newDenialReason = null;

        String messageKey = "response_reason_renew_success";
        String cancellationMessage = "Renovado com o protocolo";
        when(messageSource.getMessage(
            eq(messageKey),
            eq(null),
            eq(locale)
        )).thenReturn(cancellationMessage);

        modulesUtilsService.renewRequestStatus(
            PROTOCOL_NUMBER,
            newStatus,
            newDenialReason,
            existingRequest,
            ID_USER
        );

        verify(modulesRequestRepository, times(2)).save(oldRequestCaptor.capture());

        List<ModulesRequestEntity> savedRequests = oldRequestCaptor.getAllValues();
        ModulesRequestEntity updatedOldRequest = savedRequests.get(0);
        ModulesRequestEntity newRenewRequest = savedRequests.get(1);

        assertEquals("cancelado", updatedOldRequest.getStatus());
        assertNotNull(updatedOldRequest.getCancelReason());
        assertEquals(cancellationMessage + " " + PROTOCOL_NUMBER, updatedOldRequest.getCancelReason());

        assertTrue(updatedOldRequest.getUpdatedAt().isAfter(oldCreationTime));

        assertNotNull(newRenewRequest.getId());
        assertNotNull(newRenewRequest.getCreatedAt());
        assertEquals(PROTOCOL_NUMBER, newRenewRequest.getProtocolNumber());
        assertEquals(newStatus, newRenewRequest.getStatus());
        assertEquals(existingRequest.getJustification(), newRenewRequest.getJustification());
        assertEquals(existingRequest.isUrgent(), newRenewRequest.isUrgent());
        assertEquals(existingRequest.getModuleNamesRequested(), newRenewRequest.getModuleNamesRequested());
        assertEquals(existingRequest.getIdUser(), newRenewRequest.getIdUser());

        assertEquals(existingRequest.getProtocolNumber(), newRenewRequest.getLinkedProtocol());
    }

    @Test
    void testRenewRequestStatus_DenialCase_ShouldSaveNewRequestWithDenialReason() {
        ArgumentCaptor<ModulesRequestEntity> newRequestCaptor = ArgumentCaptor.forClass(ModulesRequestEntity.class);

        ModulesRequestEntity existingRequest = new ModulesRequestEntity();
        existingRequest.setId(UUID.randomUUID());
        existingRequest.setProtocolNumber("OLD-PROTO-002");
        existingRequest.setStatus("ativo");
        existingRequest.setModuleNamesRequested(List.of(MODULE_A.toLowerCase()));
        existingRequest.setIdUser(ID_USER);

        String newStatus = "negado";
        String newDenialReason = "Módulos indisponíveis";

        modulesUtilsService.renewRequestStatus(
            PROTOCOL_NUMBER,
            newStatus,
            newDenialReason,
            existingRequest,
            ID_USER
        );

        verify(modulesRequestRepository, times(2)).save(newRequestCaptor.capture());

        ModulesRequestEntity newRenewRequest = newRequestCaptor.getAllValues().get(1);

        assertEquals(PROTOCOL_NUMBER, newRenewRequest.getProtocolNumber());
        assertEquals(newStatus, newRenewRequest.getStatus());
        assertEquals(newDenialReason, newRenewRequest.getDenialReason());
        assertEquals(existingRequest.getProtocolNumber(), newRenewRequest.getLinkedProtocol());
    }

}