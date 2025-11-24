package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModuleResponseDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesAllowedDepartmentsEntity;
import gatekeepler.modules.persistence.entities.ModulesEntity;
import gatekeepler.modules.persistence.entities.ModulesMutuallyExclusiveEntity;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesMutuallyExclusiveRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulesReadModuleServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ModulesRepository modulesRepository;

    @Mock
    private ModulesMutuallyExclusiveRepository mutuallyExclusiveRepository;

    @Mock
    private ModulesAllowedDepartmentsRepository allowedDepartmentsRepository;

    @InjectMocks
    private ModulesReadModuleService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(
            messageSource.getMessage(
                eq("response_get_data_success"),
                isNull(),
                eq(Locale.getDefault())
            )
        ).thenReturn("sucesso");
    }

    @Test
    void testExecute_ShouldReturnListOfModules() {

        ModulesEntity module = new ModulesEntity();
        module.setName("portal do colaborador");
        module.setDescription("descrição teste");
        module.setActive(true);

        when(modulesRepository.findAll()).thenReturn(List.of(module));

        ModulesAllowedDepartmentsEntity allowedDept = new ModulesAllowedDepartmentsEntity();
        allowedDept.setDepartment("ti");

        when(allowedDepartmentsRepository.findByModuleName("portal do colaborador"))
            .thenReturn(List.of(allowedDept));

        ModulesMutuallyExclusiveEntity exclusive = new ModulesMutuallyExclusiveEntity();
        exclusive.setModuleAName("portal do colaborador");
        exclusive.setModuleBName("relatórios gerenciais");

        when(mutuallyExclusiveRepository.findByModuleANameOrModuleBName(
            "portal do colaborador",
            "portal do colaborador"
        )).thenReturn(List.of(exclusive));

        ResponseEntity response = service.execute();

        assertEquals(200, response.getStatusCode().value());

        Object body = response.getBody();
        assertNotNull(body);

        var dataList = (List<ModuleResponseDTO>)
            ((StandardResponseService) body).getData();

        assertEquals(1, dataList.size());

        ModuleResponseDTO dto = dataList.get(0);

        assertEquals("portal do colaborador", dto.getModuleName());
        assertEquals("descrição teste", dto.getDescription());
        assertTrue(dto.isActive());
        assertEquals(List.of("ti"), dto.getAllowedDepartments());
        assertEquals(List.of("relatórios gerenciais"), dto.getIncompatibleModules());

        verify(modulesRepository, times(1)).findAll();
        verify(allowedDepartmentsRepository, times(1))
            .findByModuleName("portal do colaborador");
        verify(mutuallyExclusiveRepository, times(1))
            .findByModuleANameOrModuleBName("portal do colaborador", "portal do colaborador");
    }
}
