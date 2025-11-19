package accounts.controllers;

import accounts.services.AccountsProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class AccountsProfileController {

    // ==================================================== ( constructor init )

    private final AccountsProfileService accountsProfileService;

    public AccountsProfileController(

        AccountsProfileService accountsProfileService

    ) {

        this.accountsProfileService = accountsProfileService;

    }

    // ===================================================== ( constructor end )

    @GetMapping("/${ACCOUNTS_BASE_URL}/get-profile")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return accountsProfileService.execute(credentialsData);

    }

}