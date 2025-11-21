package modules.services;

import modules.dtos.ModulesReadRequestsDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesAllowedDepartmentsRepository;
import modules.persistence.repositories.ModulesRequestRepository;
import modules.persistence.specifications.ModulesReadRequestsSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModulesReadRequestsService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository;
    private final ErrorHandler errorHandler;
    private final ModulesRequestRepository modulesRequestRepository;

    public ModulesReadRequestsService(

        MessageSource messageSource,
        ModulesAllowedDepartmentsRepository modulesAllowedDepartmentsRepository,
        ModulesRequestRepository modulesRequestRepository,
        ErrorHandler errorHandler

    ) {
        this.messageSource = messageSource;
        this.modulesAllowedDepartmentsRepository = modulesAllowedDepartmentsRepository;
        this.errorHandler = errorHandler;
        this.modulesRequestRepository = modulesRequestRepository;
    }

    // ===================================================== ( constructor end )

    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        ModulesReadRequestsDTO modulesReadRequestsDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
        String emailUser = (String) credentialsData.get("email".toLowerCase());
        String departmentUser = (String) credentialsData.get("department".toLowerCase());

        // query db
        Specification<ModulesRequestEntity> spec = ModulesReadRequestsSpecification.filter(
            modulesReadRequestsDTO.protocolNumber(),
            modulesReadRequestsDTO.moduleName(),
            modulesReadRequestsDTO.status(),
            modulesReadRequestsDTO.urgency(),
            idUser.toString()
        );

        // list all categories
        List<ModulesRequestEntity> readRequests = modulesRequestRepository.findAll(spec);

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/create-request");

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
            .data(readRequests)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}