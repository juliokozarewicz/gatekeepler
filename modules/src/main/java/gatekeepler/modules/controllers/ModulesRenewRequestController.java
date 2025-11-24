package gatekeepler.modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import gatekeepler.modules.dtos.UUIDValidationDTO;
import gatekeepler.modules.services.ModulesRenewRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesRenewRequestController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final ModulesRenewRequestService modulesRenewRequestService;

    public ModulesRenewRequestController(
        ModulesRenewRequestService modulesRenewRequestService
    ) {
        this.modulesRenewRequestService = modulesRenewRequestService;
    }

    // ===================================================== ( constructor end )

    @PostMapping("/${MODULES_BASE_URL}/renew-request/{idRenewRequest}")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @PathVariable UUIDValidationDTO idRenewRequest,
        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesRenewRequestService.execute(
            credentialsData,
            idRenewRequest
        );

    }

}