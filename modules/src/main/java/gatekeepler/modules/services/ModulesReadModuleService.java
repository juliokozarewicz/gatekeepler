package gatekeepler.modules.services;

import gatekeepler.modules.dtos.ModuleResponseDTO;
import gatekeepler.modules.exceptions.ErrorHandler;
import gatekeepler.modules.persistence.entities.ModulesAllowedDepartmentsEntity;
import gatekeepler.modules.persistence.entities.ModulesEntity;
import gatekeepler.modules.persistence.entities.ModulesMutuallyExclusiveEntity;
import gatekeepler.modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import gatekeepler.modules.persistence.repositories.ModulesMutuallyExclusiveRepository;
import gatekeepler.modules.persistence.repositories.ModulesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModulesReadModuleService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final ModulesRepository modulesRepository;
    private final ModulesMutuallyExclusiveRepository modulesMutuallyExclusiveRepository;
    private final ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository;

    public ModulesReadModuleService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        ModulesRepository modulesRepository,
        ModulesMutuallyExclusiveRepository modulesMutuallyExclusiveRepository,
        ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository

    ) {
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.modulesRepository = modulesRepository;
        this.modulesMutuallyExclusiveRepository = modulesMutuallyExclusiveRepository;
        this.modulesAllowedDepartmentsRepository = modulesAllowedDepartmentsRepository;
    }

    // ===================================================== ( constructor end )

    public ResponseEntity execute() {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Retrieve all modules
        List<ModulesEntity> modulesList = modulesRepository.findAll();

        // List of module responses
        List<ModuleResponseDTO> moduleResponseList = new ArrayList<>();

        for (ModulesEntity module : modulesList) {

            // Retrieve allowed departments for the module
            List<ModulesAllowedDepartmentsEntity> allowedDepartments =
                modulesAllowedDepartmentsRepository.findByModuleName(module.getName());
            List<String> allowedDepartmentsNames = allowedDepartments.stream()
                .map(ModulesAllowedDepartmentsEntity::getDepartment)
                .collect(Collectors.toList());

            // Retrieve mutually exclusive modules
            List<ModulesMutuallyExclusiveEntity> mutuallyExclusiveModules =
                modulesMutuallyExclusiveRepository.findByModuleANameOrModuleBName(module.getName(), module.getName());
            List<String> incompatibleModules = mutuallyExclusiveModules.stream()
                .map(exclusive -> exclusive.getModuleAName().equals(module.getName())
                    ? exclusive.getModuleBName()
                    : exclusive.getModuleAName())
                .collect(Collectors.toList());

            // Create the DTO for the module
            ModuleResponseDTO moduleResponse = new ModuleResponseDTO(
                module.getName(),
                module.getDescription(),
                module.isActive(),
                allowedDepartmentsNames,
                incompatibleModules
            );

            moduleResponseList.add(moduleResponse);
        }

        // Response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/read-modules");
        customLinks.put("next", "/" + modulesBaseURL + "/read-requests");

        // Meta
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("totalItems", moduleResponseList.size());

        // Response (body)
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(messageSource.getMessage("response_get_data_success", null, locale))
            .data(moduleResponseList)
            .meta(metaData)
            .links(customLinks)
            .build();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}