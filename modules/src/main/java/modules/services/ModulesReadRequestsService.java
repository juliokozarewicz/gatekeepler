package modules.services;

import modules.dtos.ModulesReadRequestsDTO;
import modules.exceptions.ErrorHandler;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import modules.persistence.specifications.ModulesReadRequestsSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
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
    private final ErrorHandler errorHandler;
    private final ModulesRequestRepository modulesRequestRepository;

    public ModulesReadRequestsService(

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
        ModulesReadRequestsDTO modulesReadRequestsDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
        String emailUser = (String) credentialsData.get("email".toLowerCase());
        String departmentUser = (String) credentialsData.get("department".toLowerCase());

        // Filter
        Specification<ModulesRequestEntity> spec =
            ModulesReadRequestsSpecification.filter(
                modulesReadRequestsDTO.protocolNumber(),
                modulesReadRequestsDTO.moduleName(),
                modulesReadRequestsDTO.status(),
                modulesReadRequestsDTO.urgent(),
                modulesReadRequestsDTO.startDate(),
                modulesReadRequestsDTO.endDate(),
                idUser.toString()
            );

        // Pagination
        // ---------------------------------------------------------------------
        int pageNumber = modulesReadRequestsDTO.page() != null
        ? modulesReadRequestsDTO.page()
        : 0;

        Pageable pageable = PageRequest.of(
            pageNumber,
            10,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ModulesRequestEntity> page = modulesRequestRepository
            .findAll(spec, pageable);
        // ---------------------------------------------------------------------

        // Entity
        List<Map<String, Object>> result = page.getContent().stream()
            .map(req -> {

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", req.getId());
                map.put("protocol", req.getProtocolNumber());
                map.put("requestedModules", req.getModuleNamesRequested());
                map.put("status", req.getStatus());
                map.put("justification", req.getJustification());
                map.put("urgent", req.isUrgent());
                map.put("requestDate", req.getCreatedAt());
                map.put("expirationDate", req.getCreatedAt().plus(180, ChronoUnit.DAYS));

                // Use ternary operators to decide which fields to include
                if ("ativo".equals(req.getStatus())) {
                    map.put("linkedProtocol", req.getLinkedProtocol() != null ? req.getLinkedProtocol() : null);
                    map.remove("denialReason");
                    map.remove("cancelReason");
                }

                // "negado" - only show denialReason, remove cancelReason and linkedProtocol
                else if ("negado".equals(req.getStatus())) {
                    map.put("denialReason", req.getDenialReason() != null ? req.getDenialReason() : null);
                    map.remove("linkedProtocol");
                    map.remove("cancelReason");
                }

                // "cancelado" - only show cancelReason, remove denialReason and linkedProtocol
                else if ("cancelado".equals(req.getStatus())) {

                    map.put("cancelReason", req.getCancelReason() != null ? req.getCancelReason() : null);
                    map.remove("denialReason");
                    map.remove("linkedProtocol");

                } else {

                    // For any other status, remove all extra fields
                    map.remove("denialReason");
                    map.remove("cancelReason");
                    map.remove("linkedProtocol");

                }

                return map;
            })
            .toList();

        // Meta
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("page", page.getNumber());
        metaData.put("totalPages", page.getTotalPages());
        metaData.put("totalItems", page.getTotalElements());

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + modulesBaseURL + "/read-requests");

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
            .data(result)
            .meta(metaData)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}