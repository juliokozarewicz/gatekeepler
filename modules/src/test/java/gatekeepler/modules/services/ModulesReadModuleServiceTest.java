package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModuleResponseDTO;
import gatekeepler.modules.persistence.entities.ModulesAllowedDepartmentsEntity;
import gatekeepler.modules.persistence.entities.ModulesEntity;
import gatekeepler.modules.persistence.entities.ModulesMutuallyExclusiveEntity;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesMutuallyExclusiveRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import gatekeepler.modules.exceptions.ErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesReadModuleServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private ErrorHandler errorHandler;
    @Mock private ModulesRepository modulesRepository;
    @Mock private ModulesMutuallyExclusiveRepository mutuallyExclusiveRepository;
    @Mock private ModulesAllowedDepartmentsRepository allowedDepartmentsRepository;

    @InjectMocks private ModulesReadModuleService service;

    private Locale locale;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locale = Locale.getDefault();

        when(messageSource.getMessage(eq("response_get_data_success"), eq(null), eq(locale)))
            .thenReturn("Dados obtidos");
    }

    @Test
    void execute_ShouldReturnModulesWithAllowedAndIncompatibleLists() {

        ModulesEntity moduleA = new ModulesEntity();
        moduleA.setName("portal do colaborador");
        moduleA.setDescription("acesso basico");
        moduleA.setActive(true);

        ModulesEntity moduleB = new ModulesEntity();
        moduleB.setName("relatorios gerenciais");
        moduleB.setDescription("relatorios");
        moduleB.setActive(true);

        when(modulesRepository.findAll()).thenReturn(List.of(moduleA, moduleB));

        ModulesAllowedDepartmentsEntity ad1 = new ModulesAllowedDepartmentsEntity();
        ad1.setModuleName("portal do colaborador");
        ad1.setDepartment("ti");

        when(allowedDepartmentsRepository.findByModuleName(eq("portal do colaborador")))
            .thenReturn(List.of(ad1));

        ModulesMutuallyExclusiveEntity me = new ModulesMutuallyExclusiveEntity();
        me.setModuleAName("portal do colaborador");
        me.setModuleBName("relatorios gerenciais");

        when(mutuallyExclusiveRepository.findByModuleANameOrModuleBName(eq("portal do colaborador"), eq("portal do colaborador")))
            .thenReturn(List.of(me));

        when(allowedDepartmentsRepository.findByModuleName(eq("relatorios gerenciais")))
            .thenReturn(List.of());

        when(mutuallyExclusiveRepository.findByModuleANameOrModuleBName(eq("relatorios gerenciais"), eq("relatorios gerenciais")))
            .thenReturn(List.of(me));

        ResponseEntity response = service.execute();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        StandardResponseService body = (StandardResponseService) response.getBody();
        assertEquals("Dados obtidos", body.getMessage());

        @SuppressWarnings("unchecked")
        List<ModuleResponseDTO> dtos = (List<ModuleResponseDTO>) body.getData();
        assertEquals(2, dtos.size());

        ModuleResponseDTO dtoA = dtos.stream()
            .filter(d -> "portal do colaborador".equals(d.getModuleName()))
            .findFirst()
            .orElse(null);

        assertNotNull(dtoA);
        assertEquals("acesso basico", dtoA.getDescription());
        assertTrue(dtoA.isActive());
        assertEquals(1, dtoA.getAllowedDepartments().size());
        assertEquals("ti", dtoA.getAllowedDepartments().get(0));
        assertEquals(1, dtoA.getIncompatibleModules().size());
        assertTrue(dtoA.getIncompatibleModules().contains("relatorios gerenciais"));

        verify(modulesRepository, times(1)).findAll();
        verify(allowedDepartmentsRepository, times(1)).findByModuleName(eq("portal do colaborador"));
        verify(mutuallyExclusiveRepository, times(1)).findByModuleANameOrModuleBName(eq("portal do colaborador"), eq("portal do colaborador"));
    }

    @Test
    void execute_ShouldHandleNoModulesGracefully() {
        when(modulesRepository.findAll()).thenReturn(List.of());

        ResponseEntity response = service.execute();

        StandardResponseService body = (StandardResponseService) response.getBody();
        assertNotNull(body);
        assertEquals(0, ((List<?>) body.getData()).size());

        verify(modulesRepository, times(1)).findAll();
    }
}
