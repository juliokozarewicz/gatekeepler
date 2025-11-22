package modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import modules.services.ModulesReadModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class ModulesReadModuleController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private final ModulesReadModuleService modulesReadModuleService;

    public ModulesReadModuleController(
        ModulesReadModuleService modulesReadModuleService
    ) {
        this.modulesReadModuleService = modulesReadModuleService;
    }

    // ===================================================== ( constructor end )

    @GetMapping("/${MODULES_BASE_URL}/read-modules")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return modulesReadModuleService.execute(
            credentialsData
        );

    }

}