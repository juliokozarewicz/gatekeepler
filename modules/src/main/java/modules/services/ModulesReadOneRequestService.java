package modules.services;

import modules.dtos.UUIDValidationDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ModulesReadOneRequestService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final ModulesRequestRepository modulesRequestRepository;

    public ModulesReadOneRequestService(

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
        UUIDValidationDTO UUIDValidationDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
        String emailUser = (String) credentialsData.get("email".toLowerCase());
        String departmentUser = (String) credentialsData.get("department".toLowerCase());

        UUID parsedUUID = UUID.fromString(UUIDValidationDTO.id());

        Optional<ModulesRequestEntity> existingId = modulesRequestRepository
            .findByIdAndIdUser(parsedUUID, idUser.toString());

        if (existingId.isEmpty()) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_request_dont_exist", null, locale
                )
            );

        }

        Map<String, Object> sanitizedResponse = new LinkedHashMap<>();
        sanitizedResponse.put("id", existingId.get().getId());
        sanitizedResponse.put("createdAt", existingId.get().getCreatedAt());
        sanitizedResponse.put("updatedAt", existingId.get().getUpdatedAt());
        sanitizedResponse.put("expirationDate", existingId.get().getCreatedAt().plus(180, ChronoUnit.DAYS));
        sanitizedResponse.put("protocolNumber", existingId.get().getProtocolNumber());
        sanitizedResponse.put("moduleNamesRequested", existingId.get().getModuleNamesRequested());
        sanitizedResponse.put("justification", existingId.get().getJustification());
        sanitizedResponse.put("urgent", existingId.get().isUrgent());
        sanitizedResponse.put("status", existingId.get().getStatus());
        sanitizedResponse.put("denialReason", existingId.get().getDenialReason());

        // Apply conditional field logic based on status
        switch (existingId.get().getStatus()) {
            case "ativo":
                sanitizedResponse.put(
                    "linkedProtocol", existingId.get().getLinkedProtocol()
                );
                sanitizedResponse.remove("denialReason");
                sanitizedResponse.remove("cancelReason");
                break;
            case "negado":
                sanitizedResponse.put(
                    "denialReason", existingId.get().getDenialReason()
                );
                sanitizedResponse.remove("linkedProtocol");
                sanitizedResponse.remove("cancelReason");
                break;
            case "cancelado":
                sanitizedResponse.put(
                    "cancelReason", existingId.get().getCancelReason()
                );
                sanitizedResponse.remove("denialReason");
                sanitizedResponse.remove("linkedProtocol");
                break;
            default:
                sanitizedResponse.remove("linkedProtocol");
                sanitizedResponse.remove("denialReason");
                sanitizedResponse.remove("cancelReason");
                break;
        }

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/read-one-request/" + UUIDValidationDTO.id());

        // response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_get_data_success",
                    null,
                    locale
                )
            )
            .data(sanitizedResponse)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}