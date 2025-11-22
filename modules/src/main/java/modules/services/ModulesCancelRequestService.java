package modules.services;

import modules.dtos.ModulesCancelRequestDTO;
import modules.dtos.UUIDValidationDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ModulesCancelRequestService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final ModulesRequestRepository modulesRequestRepository;

    public ModulesCancelRequestService(

        MessageSource messageSource,
        ModulesRequestRepository modulesRequestRepository,
        ErrorHandler errorHandler

    ) {
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.modulesRequestRepository = modulesRequestRepository;
    }

    // ===================================================== ( constructor end )

    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        UUIDValidationDTO UUIDValidationDTO,
        ModulesCancelRequestDTO modulesCancelRequestDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
        String emailUser = (String) credentialsData.get("email".toLowerCase());
        String departmentUser = (String) credentialsData.get("department".toLowerCase());

        // find request
        UUID parsedUUID = UUID.fromString(UUIDValidationDTO.id());

        Optional<ModulesRequestEntity> existingRequest = modulesRequestRepository
            .findByIdAndIdUser(parsedUUID, idUser.toString());

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

        // Cancel request
        ModulesRequestEntity requestEntity = existingRequest.get();
        requestEntity.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        requestEntity.setStatus("cancelado");
        requestEntity.setCancelReason(modulesCancelRequestDTO.justification());

        // Save the updated request entity
        modulesRequestRepository.save(requestEntity);

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/cancel-request/" + UUIDValidationDTO.id());
        customLinks.put("next", "/" + modulesBaseURL + "/read-requests");

        // reponse (body)
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_request_cancel_success",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}