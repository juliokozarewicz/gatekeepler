package documentation.exceptions;

import documentation.services.StandardResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<StandardResponseService> handleError(HttpServletRequest request) {
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(404)
            .statusMessage("error")
            .build();

        return ResponseEntity.status(400).body(response);
    }
}