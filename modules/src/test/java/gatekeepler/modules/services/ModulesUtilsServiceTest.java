package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesCreateRequestDTO;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class ModulesUtilsServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    private ModulesUtilsService utilsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        utilsService = new ModulesUtilsService(messageSource, modulesRequestRepository);
        LocaleContextHolder.setLocale(Locale.US);
    }

    @Test
    void generateProtocolNumber_IsWellFormed() {
        Instant now = Instant.parse("2025-11-24T12:34:56Z");

        String protocol = ModulesUtilsService.generateProtocolNumber(now);

        assertNotNull(protocol);
        assertTrue(protocol.startsWith("SOL-"));

        String[] parts = protocol.split("-");
        assertEquals(3, parts.length);
        assertEquals("SOL", parts[0]);
        assertEquals("20251124", parts[1]);
        assertEquals(4, parts[2].length());
    }

    @Test
    void createCommitRequestStatus_SavesEntityWithExpectedFields() {

        ModulesCreateRequestDTO dto = new ModulesCreateRequestDTO(
            List.of("modA", "modB"),
            "justification long enough 20+",
            true
        );

        ArgumentCaptor<ModulesRequestEntity> captor =
            ArgumentCaptor.forClass(ModulesRequestEntity.class);

        utilsService.createCommitRequestStatus(
            "PROTO-1",
            "negado",
            "reason X",
            dto,
            "user-1"
        );

        verify(modulesRequestRepository, times(1)).save(captor.capture());
        ModulesRequestEntity saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals("PROTO-1", saved.getProtocolNumber());
        assertEquals("negado", saved.getStatus());
        assertEquals("reason X", saved.getDenialReason());
        assertEquals("user-1", saved.getIdUser());
        assertEquals(dto.justification(), saved.getJustification());
        assertTrue(saved.isUrgent());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        List<String> mods = saved.getModuleNamesRequested();
        assertEquals(2, mods.size());
        assertTrue(mods.contains("moda"));
        assertTrue(mods.contains("modb"));
    }

    @Test
    void renewRequestStatus_UpdatesOldAndCreatesNewWithLink() {

        when(messageSource.getMessage(
            eq("response_reason_renew_success"),
            eq(null),
            argThat(locale -> locale != null)
        )).thenReturn("Renovado com sucesso:");

        ModulesRequestEntity existing = new ModulesRequestEntity();
        existing.setId(UUID.randomUUID());
        existing.setProtocolNumber("OLD-PROTO");
        existing.setModuleNamesRequested(List.of("a", "b"));
        existing.setJustification("old-just");
        existing.setUrgent(true);
        existing.setStatus("ativo");

        ArgumentCaptor<ModulesRequestEntity> captor =
            ArgumentCaptor.forClass(ModulesRequestEntity.class);

        utilsService.renewRequestStatus(
            "NEW-PROTO",
            "ativo",
            null,
            existing,
            "user-x"
        );

        verify(modulesRequestRepository, times(2)).save(captor.capture());
        List<ModulesRequestEntity> savedList = captor.getAllValues();

        ModulesRequestEntity updatedOld = savedList.get(0);
        ModulesRequestEntity newEntity = savedList.get(1);

        assertEquals("cancelado", updatedOld.getStatus());
        assertEquals("Renovado com sucesso: NEW-PROTO", updatedOld.getCancelReason());
        assertNotNull(updatedOld.getUpdatedAt());

        assertEquals("NEW-PROTO", newEntity.getProtocolNumber());
        assertEquals(existing.getModuleNamesRequested(), newEntity.getModuleNamesRequested());
        assertEquals(existing.getJustification(), newEntity.getJustification());
        assertEquals("user-x", newEntity.getIdUser());
        assertEquals("OLD-PROTO", newEntity.getLinkedProtocol());
        assertEquals("ativo", newEntity.getStatus());
        assertNotNull(newEntity.getCreatedAt());
        assertNotNull(newEntity.getUpdatedAt());
    }

    @Test
    void renewRequestStatus_ShouldNotSetLinkedProtocol_WhenOldHasNoProtocolNumber() {

        when(messageSource.getMessage(
            eq("response_reason_renew_success"),
            eq(null),
            argThat(locale -> locale != null)
        )).thenReturn("Renovado:");

        ModulesRequestEntity existing = new ModulesRequestEntity();
        existing.setId(UUID.randomUUID());
        existing.setProtocolNumber(null);
        existing.setModuleNamesRequested(List.of("x"));
        existing.setJustification("old justification");
        existing.setUrgent(false);
        existing.setStatus("ativo");

        ArgumentCaptor<ModulesRequestEntity> captor =
            ArgumentCaptor.forClass(ModulesRequestEntity.class);

        utilsService.renewRequestStatus(
            "PROTO-XYZ",
            "ativo",
            "reason-123",
            existing,
            "user-Y"
        );

        verify(modulesRequestRepository, times(2)).save(captor.capture());
        List<ModulesRequestEntity> saved = captor.getAllValues();

        ModulesRequestEntity oldUpdated = saved.get(0);
        ModulesRequestEntity newReq = saved.get(1);

        assertEquals("cancelado", oldUpdated.getStatus());
        assertEquals("Renovado: PROTO-XYZ", oldUpdated.getCancelReason());
        assertNull(newReq.getLinkedProtocol());
        assertEquals("PROTO-XYZ", newReq.getProtocolNumber());
        assertEquals("ativo", newReq.getStatus());
        assertEquals("reason-123", newReq.getDenialReason());
        assertEquals("user-Y", newReq.getIdUser());
        assertEquals(List.of("x"), newReq.getModuleNamesRequested());
    }
}
