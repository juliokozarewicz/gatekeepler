package modules.services;

import modules.dtos.ModulesCreateRequestDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.entities.ModulesEntity;
import modules.persistence.repositories.ModulesRequestRepository;
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
    private final ModulesRequestRepository modulesRequestRepository;
    private final ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository;
    private final ModulesRepository modulesRepository;
    private final ErrorHandler errorHandler;
    private final ModulesUtilsService modulesUtilsService;

    public ModulesCreateRequestService(

        MessageSource messageSource,
        ModulesRequestRepository modulesRequestRepository,
        ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository,
        ModulesRepository modulesRepository,
        ErrorHandler errorHandler,
        ModulesUtilsService modulesUtilsService

    ) {
        this.messageSource = messageSource;
        this.modulesRequestRepository = modulesRequestRepository;
        this.modulesAllowedDepartmentsRepository = modulesAllowedDepartmentsRepository;
        this.modulesRepository = modulesRepository;
        this.errorHandler = errorHandler;
        this.modulesUtilsService = modulesUtilsService;
    }

    // ===================================================== ( constructor end )

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
        String protocolNumber = ModulesUtilsService.generateProtocolNumber(
            ZonedDateTime.now(ZoneOffset.UTC).toInstant()
        );

        // Validating Modules (minimum 1, maximum 3)
        List<String> requestedModules = modulesCreateRequestDTO.modules();
        if (
            requestedModules == null ||
            requestedModules.size() < 1 ||
            requestedModules.size() > 3
        ) {

            modulesUtilsService.createCommitRequestStatus(
                protocolNumber,
                "negado",
                messageSource.getMessage(
                    "response_many_modules_error",
                    null,
                    locale
                ),
                modulesCreateRequestDTO,
                idUser.toString()

            );

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

            List<ModulesRequestEntity> existingRequests = modulesRequestRepository
                .findByIdUser(idUser.toString());

            boolean hasActiveRequestForModule = existingRequests.stream()
                .anyMatch(request -> request
                    .getModuleNamesRequested()
                    .contains(
                        moduleName.toLowerCase()) && "ativo"
                        .equals(request.getStatus().toLowerCase())
                    );

            if (hasActiveRequestForModule) {

                modulesUtilsService.createCommitRequestStatus(
                    protocolNumber,
                    "negado",
                    messageSource.getMessage(
                        "response_already_requested_error",
                        null,
                        locale
                    ) + " " + moduleName.toUpperCase(),
                    modulesCreateRequestDTO,
                    idUser.toString()
                );

                errorHandler.customErrorThrow(
                    400,
                    messageSource.getMessage(
                        "response_already_requested_error",
                        null,
                        locale
                    ) + " " + moduleName.toUpperCase()
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

            modulesUtilsService.createCommitRequestStatus(
                protocolNumber,
                "negado",
                messageSource.getMessage(
                    "request_modules_dont_exist",
                    null,
                    locale
                ),
                modulesCreateRequestDTO,
                idUser.toString()
            );

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

                modulesUtilsService.createCommitRequestStatus(
                    protocolNumber,
                    "negado",
                    messageSource.getMessage(
                        "request_module_not_allowed_error",
                        null,
                        locale
                    ) + " " + moduleName.toUpperCase(),
                    modulesCreateRequestDTO,
                    idUser.toString()
                );

                errorHandler.customErrorThrow(
                    400,
                    messageSource.getMessage(
                        "request_module_not_allowed_error",
                        null,
                        locale
                    ) + " " + moduleName.toUpperCase()
                );

            }

        }

        // Commit DB
        // ---------------------------------------------------------------------
        modulesUtilsService.createCommitRequestStatus(
            protocolNumber,
            "ativo",
            null,
            modulesCreateRequestDTO,
            idUser.toString()

        );
        // ---------------------------------------------------------------------

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/create-request");

        // reponse (body)
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

}