package modules.controllers;

import modules.dtos.ModulesCreateRequestDTO;
import modules.services.ModulesCreateRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody ModulesCreateRequestDTO modulesCreateRequestDTO,
        BindingResult bindingResult

    ) {

        return modulesCreateRequestService.execute(modulesCreateRequestDTO);

    }

}