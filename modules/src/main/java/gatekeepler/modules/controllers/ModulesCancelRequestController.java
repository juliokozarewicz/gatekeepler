package gatekeepler.modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import gatekeepler.modules.dtos.ModulesCancelRequestDTO;
import gatekeepler.modules.dtos.UUIDValidationDTO;
import gatekeepler.modules.services.ModulesCancelRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesCancelRequestController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

     private final ModulesCancelRequestService modulesCancelRequestService;

    public ModulesCancelRequestController(
        ModulesCancelRequestService modulesCancelRequestService
    ) {
        this.modulesCancelRequestService = modulesCancelRequestService;
    }

    // ===================================================== ( constructor end )

    @PostMapping("/${MODULES_BASE_URL}/cancel-request/{idCancelRequest}")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @PathVariable UUIDValidationDTO idCancelRequest,

        @Valid @RequestBody ModulesCancelRequestDTO modulesCancelRequestDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesCancelRequestService.execute(
            credentialsData,
            idCancelRequest,
            modulesCancelRequestDTO
        );

    }

}