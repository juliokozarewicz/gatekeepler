package modules.services;

import jakarta.transaction.Transactional;
import modules.dtos.ModulesCreateRequestDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModuleRequestEntity;
import modules.persistence.entities.ModulesEntity;
import modules.persistence.repositories.ModuleRequestRepository;
import modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import modules.persistence.repositories.ModulesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModulesCreateRequestService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ModuleRequestRepository moduleRequestRepository;
    private final ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository;
    private final ModulesRepository modulesRepository;
    private final ErrorHandler errorHandler;

    public ModulesCreateRequestService(

        MessageSource messageSource,
        ModuleRequestRepository moduleRequestRepository,
        ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository,
        ModulesRepository modulesRepository,
        ErrorHandler errorHandler

    ) {
        this.messageSource = messageSource;
        this.moduleRequestRepository = moduleRequestRepository;
        this.modulesAllowedDepartmentsRepository = modulesAllowedDepartmentsRepository;
        this.modulesRepository = modulesRepository;
        this.errorHandler = errorHandler;
    }

    // ===================================================== ( constructor end )

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        ModulesCreateRequestDTO modulesCreateRequestDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
        String emailUser = (String) credentialsData.get("email".toLowerCase());
        String departmentUser = (String) credentialsData.get("department".toLowerCase());

        // Protocol number
        String protocolNumber = generateProtocolNumber(
            ZonedDateTime.now(ZoneOffset.UTC).toInstant()
        );

        // Validating Modules (minimum 1, maximum 3)
        List<String> requestedModules = modulesCreateRequestDTO.modules();
        if (
            requestedModules == null ||
            requestedModules.size() < 1 ||
            requestedModules.size() > 3
        ) {

            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_many_modules_error",
                    null,
                    locale
                )
            );

        }

        // Checking if User Already Has Active Request for the Same Modules
        for (String moduleName : requestedModules) {

            List<ModuleRequestEntity> existingRequests = moduleRequestRepository
                .findByIdUser(idUser.toString());

            boolean hasActiveRequestForModule = existingRequests.stream()
                .anyMatch(request -> request
                    .getModuleNamesRequested()
                    .contains(
                        moduleName.toLowerCase()) && "ativo"
                        .equals(request.getStatus().toLowerCase())
                    );

            if (hasActiveRequestForModule) {

                errorHandler.customErrorThrow(
                    400,
                    messageSource.getMessage(
                        "response_already_requested_error",
                        null,
                        locale
                    ) + " " + moduleName.toLowerCase()
                );

            }

        }

        // Validating if Modules Exist and Are Active
        List<ModulesEntity> modules = modulesCreateRequestDTO.modules().stream()
            .map(moduleName -> {
                Optional<ModulesEntity> moduleOpt = modulesRepository.findByName(moduleName.toLowerCase());
                return moduleOpt.filter(module -> module.isActive()).orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if ( modules.size() != requestedModules.size() ) {

            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "request_modules_dont_exist",
                    null,
                    locale
                )
            );

        }

        // Checking if User Already Has Access to Modules
        for (String moduleName : requestedModules) {

            boolean hasAccess = modulesAllowedDepartmentsRepository
                .existsByModuleNameAndDepartment(
                    moduleName.toLowerCase(),
                    departmentUser.toLowerCase()
                );

            if (!hasAccess) {

                errorHandler.customErrorThrow(
                    400,
                    messageSource.getMessage(
                        "request_module_not_allowed_error",
                        null,
                        locale
                    ) + " " + moduleName.toLowerCase()
                );

            }

        }

        // Commit DB
        // ---------------------------------------------------------------------
        ModuleRequestEntity newRequest = new ModuleRequestEntity();
        newRequest.setId(UUID.randomUUID());
        newRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        newRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        newRequest.setProtocolNumber(protocolNumber);
        newRequest.setModuleNamesRequested(
            modulesCreateRequestDTO.modules().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList())
        );
        newRequest.setJustification(modulesCreateRequestDTO.justification());
        newRequest.setUrgent(modulesCreateRequestDTO.urgent());
        newRequest.setStatus("ativo");
        newRequest.setDenialReason(null);
        newRequest.setIdUser(idUser.toString());

        moduleRequestRepository.save(newRequest);
        // ---------------------------------------------------------------------

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/create-request");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_request_aproved",
                    null,
                    locale
                ) + " " + protocolNumber
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

    // Generate protocol number method
    private String generateProtocolNumber(Instant nowUtc) {
        String prefix = "SOL";
        String date = nowUtc.toString().substring(0, 10).replace("-", "");
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("%s-%s-%s", prefix, date, uniqueId);
    }

}