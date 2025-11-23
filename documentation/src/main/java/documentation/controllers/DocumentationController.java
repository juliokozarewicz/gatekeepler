package documentation.controllers;


import documentation.documentation.DocumentationJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DocumentationController {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${APPLICATION_TITLE}")
    private String applicationTitle;

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    @Value("${DOCUMENTATION_BASE_URL}")
    private String documentationBaseURL;
    // -------------------------------------------------------------------------

    private final DocumentationJson documentationJson;

    public DocumentationController (
        DocumentationJson documentationJson
    ) {
        this.documentationJson = documentationJson;
    }
    // ===================================================== ( constructor end )

    @GetMapping(
        value = "/${DOCUMENTATION_BASE_URL}/json",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> handle() {

        String docs = documentationJson.documentationText();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(docs);
    }

    @GetMapping("/${DOCUMENTATION_BASE_URL}")
    public String getSwaggerUi() {
        return "<html>\n" +
            "<head>\n" +
            "<title>" + applicationTitle.toUpperCase() + "</title>\n" +
            "<link rel='icon' type='image/x-icon' href='" + "http://" + publicDomain.split(",")[0].trim() + "/" + documentationBaseURL + "/static/public/favicon.ico' />\n" +
            "<script src='https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.52.5/swagger-ui-bundle.js'></script>\n" +
            "<link rel='stylesheet' type='text/css' href='https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.52.5/swagger-ui.css' />\n" +
            "<style>\n" +
            "  #swagger-ui {\n" +
            "    max-width: 80%;\n" +
            "    margin: 0 auto;\n" +
            "  }\n" +
            "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div id='swagger-ui'></div>\n" +
            "<script>\n" +
            "  const ui = SwaggerUIBundle({\n" +
            "    url: '/" + documentationBaseURL + "/json',\n" +
            "    dom_id: '#swagger-ui',\n" +
            "    deepLinking: true,\n" +
            "    presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.presets.sdk],\n" +
            "    layout: 'BaseLayout',\n" +
            "  });\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>";
    }

}