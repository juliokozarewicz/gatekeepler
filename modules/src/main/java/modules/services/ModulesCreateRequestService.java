package modules.services;

import modules.dtos.ModulesCreateRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ModulesCreateRequestService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${MODULES_BASE_URL}")
    private String helloWorldBaseURL;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;

    public ModulesCreateRequestService(
        MessageSource messageSource
    ) {
        this.messageSource = messageSource;
    }

    // ===================================================== ( constructor end )

    public ResponseEntity execute(
        ModulesCreateRequestDTO modulesCreateRequestDTO
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        System.out.println(modulesCreateRequestDTO);

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + helloWorldBaseURL);

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_get_data_success",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();
        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}