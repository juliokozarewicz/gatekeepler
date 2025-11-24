package gatekeepler.modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import gatekeepler.modules.dtos.ModulesCreateRequestDTO;
import gatekeepler.modules.services.ModulesCreateRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesCreateRequestController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final ModulesCreateRequestService modulesCreateRequestService;

    public ModulesCreateRequestController(
        ModulesCreateRequestService modulesCreateRequestService
    ) {
        this.modulesCreateRequestService = modulesCreateRequestService;
    }

    // ===================================================== ( constructor end )

    @PostMapping("/${MODULES_BASE_URL}/create-request")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody ModulesCreateRequestDTO modulesCreateRequestDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesCreateRequestService.execute(
            credentialsData,
            modulesCreateRequestDTO
        );

    }

}