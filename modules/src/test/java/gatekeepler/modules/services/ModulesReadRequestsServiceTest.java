package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesReadRequestsDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import gatekeepler.modules.persistence.specifications.ModulesReadRequestsSpecification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesReadRequestsServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesRequestRepository modulesRequestRepository;

    @InjectMocks
    private ModulesReadRequestsService service;

    private UUID userId;
    private Map<String, Object> credentials;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();

        credentials = Map.of(
            "id", userId.toString(),
            "email", "usuario@empresa.com",
            "department", "ti"
        );

        when(
            messageSource.getMessage(
                eq("response_get_data_success"),
                eq(null),
                eq(Locale.getDefault())
            )
        ).thenReturn("sucesso");
    }

    @Test
    void testExecute_ShouldReturnActiveRequest() {

        ModulesReadRequestsDTO dto = new ModulesReadRequestsDTO(
            "SOL-123",
            "portal",
            "ativo",
            false,
            0,
            null,
            null
        );

        ModulesRequestEntity req = new ModulesRequestEntity();
        UUID reqId = UUID.randomUUID();
        req.setId(reqId);
        req.setProtocolNumber("SOL-123");
        req.setModuleNamesRequested(List.of("portal"));
        req.setStatus("ativo");
        req.setJustification("justificação ok");
        req.setUrgent(false);
        req.setLinkedProtocol("OLD-11");
        req.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt"), 1);

        Specification<ModulesRequestEntity> spec =
            ModulesReadRequestsSpecification.filter(
                "SOL-123",
                "portal",
                "ativo",
                false,
                null,
                null,
                userId.toString()
            );

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");

        when(modulesRequestRepository.findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        )).thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        assertEquals(200, response.getStatusCodeValue());

        StandardResponseService body = (StandardResponseService) response.getBody();
        assertEquals("sucesso", body.getMessage());

        List<Map<String, Object>> data = (List<Map<String, Object>>) body.getData();
        Map<String, Object> item = data.get(0);

        assertEquals("SOL-123", item.get("protocol"));
        assertEquals("ativo", item.get("status"));
        assertNull(item.get("denialReason"));
        assertNull(item.get("cancelReason"));

        verify(modulesRequestRepository, times(1)).findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        );
    }

    @Test
    void testExecute_ShouldReturnDeniedRequest() {

        ModulesReadRequestsDTO dto = new ModulesReadRequestsDTO(
            "P-001",
            "mod",
            "negado",
            false,
            0,
            null,
            null
        );

        ModulesRequestEntity req = new ModulesRequestEntity();
        UUID reqId = UUID.randomUUID();
        req.setId(reqId);
        req.setProtocolNumber("P-001");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("negado");
        req.setDenialReason("Negado por X");
        req.setUrgent(false);
        req.setJustification("abc");
        req.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        Specification<ModulesRequestEntity> spec =
            ModulesReadRequestsSpecification.filter(
                "P-001",
                "mod",
                "negado",
                false,
                null,
                null,
                userId.toString()
            );

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable, 1);

        when(modulesRequestRepository.findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        )).thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        StandardResponseService body = (StandardResponseService) response.getBody();

        Map<String, Object> item = ((List<Map<String, Object>>) body.getData()).get(0);

        assertEquals("negado", item.get("status"));
        assertEquals("Negado por X", item.get("denialReason"));
        assertNull(item.get("cancelReason"));
        assertNull(item.get("linkedProtocol"));

        verify(modulesRequestRepository, times(1)).findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        );
    }

    @Test
    void testExecute_ShouldReturnCanceledRequest() {

        ModulesReadRequestsDTO dto = new ModulesReadRequestsDTO(
            "XX-22",
            "mod",
            "cancelado",
            true,
            0,
            null,
            null
        );

        ModulesRequestEntity req = new ModulesRequestEntity();
        UUID reqId = UUID.randomUUID();
        req.setId(reqId);
        req.setProtocolNumber("XX-22");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("cancelado");
        req.setCancelReason("Cancelado Y");
        req.setUrgent(true);
        req.setJustification("zzz");
        req.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        Specification<ModulesRequestEntity> spec =
            ModulesReadRequestsSpecification.filter(
                "XX-22",
                "mod",
                "cancelado",
                true,
                null,
                null,
                userId.toString()
            );

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable, 1);

        when(modulesRequestRepository.findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        )).thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        StandardResponseService body = (StandardResponseService) response.getBody();
        Map<String, Object> item = ((List<Map<String, Object>>) body.getData()).get(0);

        assertEquals("cancelado", item.get("status"));
        assertEquals("Cancelado Y", item.get("cancelReason"));
        assertNull(item.get("denialReason"));
        assertNull(item.get("linkedProtocol"));

        verify(modulesRequestRepository, times(1)).findAll(
            (Specification<ModulesRequestEntity>) argThat(s -> s != null),
            eq(pageable)
        );
    }

}
