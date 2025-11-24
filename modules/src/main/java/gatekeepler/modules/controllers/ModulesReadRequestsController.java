package gatekeepler.modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import gatekeepler.modules.dtos.ModulesReadRequestsDTO;
import gatekeepler.modules.services.ModulesReadRequestsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesReadRequestsController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final ModulesReadRequestsService modulesReadRequestsService;

    public ModulesReadRequestsController(
        ModulesReadRequestsService modulesReadRequestsService
    ) {
        this.modulesReadRequestsService = modulesReadRequestsService;
    }

    // ===================================================== ( constructor end )

    @GetMapping("/${MODULES_BASE_URL}/read-requests")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid ModulesReadRequestsDTO modulesReadRequestsDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesReadRequestsService.execute(
            credentialsData,
            modulesReadRequestsDTO
        );

    }

}