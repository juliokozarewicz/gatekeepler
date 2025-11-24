package gatekeepler.modules.controllers;

import jakarta.servlet.http.HttpServletRequest;
import gatekeepler.modules.services.ModulesReadModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity handle(

        HttpServletRequest request

    ) {

        return modulesReadModuleService.execute();

    }

}