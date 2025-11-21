package modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import modules.dtos.UUIDValidationDTO;
import modules.services.ModulesReadOneRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesReadOneRequestController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final ModulesReadOneRequestService modulesReadOneRequestService;

    public ModulesReadOneRequestController(
        ModulesReadOneRequestService modulesReadOneRequestService
    ) {
        this.modulesReadOneRequestService = modulesReadOneRequestService;
    }

    // ===================================================== ( constructor end )

    @GetMapping("/${MODULES_BASE_URL}/read-one-request/{idRequest}")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @PathVariable UUIDValidationDTO idRequest,
        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesReadOneRequestService.execute(
            credentialsData,
            idRequest
        );

    }

}