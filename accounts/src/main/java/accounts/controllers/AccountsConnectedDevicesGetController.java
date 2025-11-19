package accounts.controllers;


import accounts.services.AccountsConnectedDevicesGetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
class AccountsConnectedDevicesGetController {

    // ==================================================== ( constructor init )

    private final AccountsConnectedDevicesGetService accountsConnectedDevicesGetService;

    public AccountsConnectedDevicesGetController(

        AccountsConnectedDevicesGetService accountsConnectedDevicesGetService

    ) {

        this.accountsConnectedDevicesGetService = accountsConnectedDevicesGetService;

    }

    // ===================================================== ( constructor end )

    @GetMapping("/${ACCOUNTS_BASE_URL}/connected-devices")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        return accountsConnectedDevicesGetService.execute(credentialsData);

    }

}