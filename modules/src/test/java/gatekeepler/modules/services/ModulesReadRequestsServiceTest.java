package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModulesReadRequestsDTO;
import gatekeepler.modules.persistence.entities.ModulesRequestEntity;
import gatekeepler.modules.persistence.repositories.ModulesRequestRepository;
import gatekeepler.modules.exceptions.ErrorHandler;
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

        when(messageSource.getMessage(
            eq("response_get_data_success"),
            eq(null),
            eq(Locale.getDefault())
        )).thenReturn("sucesso");
    }

    private Pageable pageable(int page) {
        return PageRequest.of(page, 10, Sort.Direction.DESC, "createdAt");
    }

    private Specification<ModulesRequestEntity> specMatcher() {
        return argThat(s -> s != null);
    }

    @Test
    void testExecute_ShouldReturnActiveRequest() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("SOL-123", "portal", "ativo", false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("SOL-123");
        req.setModuleNamesRequested(List.of("portal"));
        req.setStatus("ativo");
        req.setJustification("ok");
        req.setUrgent(false);
        req.setLinkedProtocol("OLD");
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page = new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        StandardResponseService body = (StandardResponseService) response.getBody();

        Map<String, Object> item = ((List<Map<String, Object>>) body.getData()).get(0);

        assertEquals("ativo", item.get("status"));
        assertFalse(item.containsKey("denialReason"));
        assertFalse(item.containsKey("cancelReason"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_ShouldReturnDeniedRequest() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("P-001", "mod", "negado", false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("P-001");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("negado");
        req.setDenialReason("Razão X");
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertEquals("negado", item.get("status"));
        assertEquals("Razão X", item.get("denialReason"));
        assertFalse(item.containsKey("cancelReason"));
        assertFalse(item.containsKey("linkedProtocol"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_DeniedWithoutReason() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("P-9", "mod", "negado", false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("P-9");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("negado");
        req.setDenialReason(null);
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page = new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertEquals("negado", item.get("status"));
        assertNull(item.get("denialReason"));
        assertFalse(item.containsKey("cancelReason"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_ShouldReturnCanceledRequest() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("XX-22", "mod", "cancelado", true, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("XX-22");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("cancelado");
        req.setCancelReason("Cancelado Y");
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertEquals("cancelado", item.get("status"));
        assertEquals("Cancelado Y", item.get("cancelReason"));
        assertFalse(item.containsKey("denialReason"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_CanceledWithoutReason() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("B-7", "mod", "cancelado", true, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("B-7");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("cancelado");
        req.setCancelReason(null);
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertNull(item.get("cancelReason"));
        assertFalse(item.containsKey("denialReason"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_StatusNull_OtherBranch() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO(null, null, null, null, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("ABC");
        req.setModuleNamesRequested(List.of("x"));
        req.setStatus(null);
        req.setLinkedProtocol(null);
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertFalse(item.containsKey("denialReason"));
        assertFalse(item.containsKey("cancelReason"));
        assertFalse(item.containsKey("linkedProtocol"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_UnknownStatus_ShouldUseFinalElse() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO(null, null, "em_analise", false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("X");
        req.setModuleNamesRequested(List.of("m"));
        req.setStatus("em_analise");
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>) ((StandardResponseService) response.getBody()).getData()).get(0);

        assertFalse(item.containsKey("denialReason"));
        assertFalse(item.containsKey("cancelReason"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_ShouldCoverNullEmailAndDepartmentValues() {

        Map<String, Object> credentialsNullValues = new HashMap<>();
        credentialsNullValues.put("id", userId.toString());

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO(null, null, null, false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("PROTO-X");
        req.setModuleNamesRequested(List.of("abc"));
        req.setStatus("ativo");
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentialsNullValues, dto);

        assertNotNull(response);

        StandardResponseService body =
            (StandardResponseService) response.getBody();

        assertNotNull(body);
        assertEquals(1, ((List<?>) body.getData()).size());

        verify(modulesRequestRepository)
            .findAll(specMatcher(), eq(pageable(0)));
    }

    @Test
    void testExecute_StatusAtivo_LinkedProtocolNull_ShouldCoverMissingBranch() {

        ModulesReadRequestsDTO dto =
            new ModulesReadRequestsDTO("AAA", "mod", "ativo", false, 0, null, null);

        ModulesRequestEntity req = new ModulesRequestEntity();
        req.setId(UUID.randomUUID());
        req.setProtocolNumber("AAA");
        req.setModuleNamesRequested(List.of("mod"));
        req.setStatus("ativo");
        req.setJustification("ok");
        req.setUrgent(false);
        req.setLinkedProtocol(null);
        req.setCreatedAt(Instant.now());

        Page<ModulesRequestEntity> page =
            new PageImpl<>(List.of(req), pageable(0), 1);

        when(modulesRequestRepository.findAll(specMatcher(), eq(pageable(0))))
            .thenReturn(page);

        ResponseEntity response = service.execute(credentials, dto);

        Map<String, Object> item =
            ((List<Map<String, Object>>)
                ((StandardResponseService) response.getBody()).getData()
            ).get(0);

        assertEquals("ativo", item.get("status"));

        assertFalse(item.containsKey("linkedProtocol"));

        verify(modulesRequestRepository).findAll(specMatcher(), eq(pageable(0)));
    }

}