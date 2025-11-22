package modules.services;

import modules.dtos.UUIDValidationDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesEntity;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import modules.persistence.repositories.ModulesRepository;
import modules.persistence.repositories.ModulesRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModulesRenewRequestService {

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

    public ModulesRenewRequestService(

        MessageSource messageSource,
        ModulesRequestRepository modulesRequestRepository,
        ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository,
        ModulesRepository modulesRepository,
        ErrorHandler errorHandler

    ) {
        this.messageSource = messageSource;
        this.modulesRequestRepository = modulesRequestRepository;
        this.modulesAllowedDepartmentsRepository = modulesAllowedDepartmentsRepository;
        this.modulesRepository = modulesRepository;
        this.errorHandler = errorHandler;
    }

    // ===================================================== ( constructor end )

    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        UUIDValidationDTO UUIDValidationDTO

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

        // find request
        UUID idRenewRequest = UUID.fromString(UUIDValidationDTO.id());

        Optional<ModulesRequestEntity> existingRequest = modulesRequestRepository
            .findByIdAndIdUser(idRenewRequest, idUser.toString());

        // check request
        if (existingRequest.isEmpty()) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_request_dont_exist", null, locale
                )
            );

        }

        // request status
        if (!existingRequest.get().getStatus().equalsIgnoreCase("ativo")) {

            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_request_not_active", null, locale
                )
            );

        }

        // expiration time
        Instant expirationDate = existingRequest.get().getCreatedAt().plus(180, ChronoUnit.DAYS);
        long daysUntilExpiration = ChronoUnit.DAYS.between(ZonedDateTime.now(), expirationDate);

        if (daysUntilExpiration > 30) {
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_request_too_far_to_renew", null, locale
                )
            );
        }

        // Validating if Modules Exist and Are Active
        List<ModulesEntity> modules = existingRequest.get().getModuleNamesRequested().stream()
            .map(moduleName -> {
                Optional<ModulesEntity> moduleOpt = modulesRepository.findByName(moduleName.toLowerCase());
                return moduleOpt.filter(module -> module.isActive()).orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if ( modules.size() != existingRequest.get().getModuleNamesRequested().size() ) {

            commitRequestStatus(
                protocolNumber,
                "negado",
                messageSource.getMessage(
                    "request_modules_dont_exist",
                    null,
                    locale
                ),
                existingRequest.get(),
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
        for (String moduleName : existingRequest.get().getModuleNamesRequested()) {

            boolean hasAccess = modulesAllowedDepartmentsRepository
                .existsByModuleNameAndDepartment(
                    moduleName.toLowerCase(),
                    departmentUser.toLowerCase()
                );

            if (!hasAccess) {

                commitRequestStatus(
                    protocolNumber,
                    "negado",
                    messageSource.getMessage(
                        "request_module_not_allowed_error",
                        null,
                        locale
                    ) + " " + moduleName.toUpperCase(),
                    existingRequest.get(),
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
        commitRequestStatus(
            protocolNumber,
            "ativo",
            null,
            existingRequest.get(),
            idUser.toString()
        );
        // ---------------------------------------------------------------------

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/renew-request/" + UUIDValidationDTO.id() );

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

    // Unified method for both active and denied requests
    private void commitRequestStatus(

        String protocolNumber,
        String status,
        String denialReason,
        ModulesRequestEntity existingRequest,
        String idUser

    ) {

        ModulesRequestEntity renewRequest = new ModulesRequestEntity();
        renewRequest.setId(UUID.randomUUID());
        renewRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        renewRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        renewRequest.setProtocolNumber(protocolNumber);
        renewRequest.setModuleNamesRequested(existingRequest.getModuleNamesRequested());
        renewRequest.setJustification(existingRequest.getJustification());
        renewRequest.setUrgent(existingRequest.isUrgent());
        renewRequest.setStatus(status);
        renewRequest.setDenialReason(denialReason);
        renewRequest.setIdUser(idUser);

        if (existingRequest.getProtocolNumber() != null) {
            renewRequest.setLinkedProtocol(existingRequest.getProtocolNumber());
        }

        modulesRequestRepository.save(renewRequest);
        
    }

}