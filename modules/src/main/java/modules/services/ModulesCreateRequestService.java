package modules.services;

import modules.dtos.ModulesCreateRequestDTO;
import modules.persistence.entities.ModuleRequestEntity;
import modules.persistence.repositories.ModuleRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

    public ModulesCreateRequestService(
        MessageSource messageSource,
        ModuleRequestRepository moduleRequestRepository
    ) {
        this.messageSource = messageSource;
        this.moduleRequestRepository = moduleRequestRepository;
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
        String emailUser = (String) credentialsData.get("email");
        String departmentUser = (String) credentialsData.get("email");

        // Commit DB
        // ---------------------------------------------------------------------
        for ( String moduleName : modulesCreateRequestDTO.modules() ) {

            ModuleRequestEntity newRequest = new ModuleRequestEntity();
            newRequest.setId(UUID.randomUUID());
            newRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            newRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            newRequest.setProtocolNumber(generateProtocolNumber(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));
            newRequest.setModuleName(moduleName);
            newRequest.setJustification(modulesCreateRequestDTO.justification());
            newRequest.setUrgent(modulesCreateRequestDTO.urgent());
            newRequest.setStatus("ATIVO");  // ######
            newRequest.setDenialReason(null); // ######
            newRequest.setIdUser(idUser.toString());

            moduleRequestRepository.save(newRequest);
        }
        // ---------------------------------------------------------------------

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/create-request");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_get_data_success",
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

    private String generateProtocolNumber(Instant nowUtc) {
        String prefix = "SOL";
        String date = nowUtc.toString().substring(0, 10).replace("-", "");  // YYYYMMDD
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();  // Parte única
        return String.format("%s-%s-%s", prefix, date, uniqueId);
    }

}