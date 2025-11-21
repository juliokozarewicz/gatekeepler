package documentation.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentationJson {

    // Env
    // -------------------------------------------------------------------------

    @Value("${APPLICATION_TITLE}")
    private String applicationTitle;

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    @Value("${HELLOWORLD_BASE_URL}")
    private String helloWorldBaseURL;

    @Value("${ACCOUNTS_BASE_URL}")
    private String accountsBaseURL;

    @Value("${MODULES_BASE_URL}")
    private String modulesBaseURL;
    // -------------------------------------------------------------------------

    private static final ObjectMapper mapper = new ObjectMapper();

    private String descriptionText() {
        return """
            This project implements the complete solution for a Mid-Level Java Developer technical challenge. The application exposes a RESTful API for requesting, renewing, canceling, and retrieving access to corporate modules, featuring JWT authentication with refresh tokens, strict validations, and automated business rules.

            
            
            ## Base URL

            ```
            https://PUBLIC_DOMAIN_REPLACE
            ```

            
            
            ## Localization (Translation)

            Any response containing the "message" field in the body will have 
            its message translated server-side, based on the language specified 
            in the request header, for the supported languages.

            
            
            ## Common responses from services

            **Authentication Error (401):**
            If the user is not authenticated (e.g., missing or invalid token), the response will be:

            ```json
            {
                "status": 401,
                "statusMessage": "error",
                "message": "Invalid credentials."
            }
            ```

            **Form field validation error (422):**
            If there are validation errors in the form fields, the response will include the fields and their respective error messages:

            ```json
            {
                "statusCode": 422,
                "statusMessage": "error",
                "fieldErrors": [
                    { "field": "field name", "message": "This field is required." },
                    { "field": "field name", "message": "This field is required." }
                ]
            }
            ```

            **Bad request (400):**
            If the request is malformed or invalid, the response will be:

            ```json
            {
                "statusCode": 400,
                "statusMessage": "error",
                "message": "The request has an error, check."
            }
            ```

            
            
            ## API Gateway Errors (No translation support)

            **Service Unavailable (503):**
            The service is temporarily unavailable, often due to maintenance or overload.

            ```json
            {
                "statusCode": 503,
                "statusMessage": "error",
                "detail": "Service Unavailable (API Gateway)"
            }
            ```

            **Rate limit exceeded (429):**
            If the user exceeds the allowed number of requests, the response will be:

            ```json
            {
                "statusCode": 429,
                "statusMessage": "error",
                "detail": "Access blocked by rate limiter (API Gateway)"
            }
            ```

            **Internal server error (500):**
            If there's an unexpected condition preventing the server from fulfilling the request:

            ```json
            {
                "statusCode": 500,
                "statusMessage": "error",
                "detail": "Server error (API Gateway)"
            }
            ```
            """;

    }

    public String documentationText() {

        String safeDescription = mapper.valueToTree(descriptionText()).toString();

        String docs = new StringBuilder()

            .append(
                """
                {
                "openapi":"3.0.0",
                "info": {
                    "title": "TITLE_REPLACE",
                    "version": "1.0",
                    "description":
                """
            )

            .append(safeDescription)

            .append(
                """
                },
                "components":{
                    "securitySchemes":{
                        "BearerAuth":{
                            "type":"http",
                            "scheme":"bearer",
                            "bearerFormat":"JWT"
                        }
                    }
                },
                "paths":{
                """
            )

            .append(
                """
                # HELLOWORLD
                # ==============================================================
                "/HELLOWORLD_BASE_URL_REPLACE":{
                    "get":{
                        "summary":"Get hello world message",
                        "description":"Retrieves a hello world message. You can optionally provide a custom message via query parameter.",
                        "tags":[
                            "HELLO WORLD"
                        ],
                        "parameters":[
                            {
                                "name":"message",
                                "in":"query",
                                "required":false,
                                "description":"Custom message to be returned. Defaults to 'Hello World!' if not provided.",
                                "schema":{
                                    "type":"string",
                                    "example":"Hello from the API!"
                                }
                            }
                        ],
                        "responses":{
                            "200":{
                                "description":"Successful response with hello world message.",
                                "content":{
                                    "application/json":{
                                        "schema":{
                                            "type":"object",
                                            "properties":{
                                                "statusCode":{
                                                    "type":"integer",
                                                    "example":200
                                                },
                                                "statusMessage":{
                                                    "type":"string",
                                                    "example":"success"
                                                },
                                                "message":{
                                                    "type":"string",
                                                    "example":"Data received successfully. (Hello World!)"
                                                },
                                                "links":{
                                                    "type":"object",
                                                    "properties":{
                                                        "self":{
                                                            "type":"string",
                                                            "example":"/HELLOWORLD_BASE_URL_REPLACE"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                # ACCOUNTS
                # ==============================================================
                "/ACCOUNTS_BASE_URL_REPLACE/signup": {
                    "post": {
                        "summary": "Create a new user account",
                        "description": "Creates a new user account with the provided details such as name, email, password, and department.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "description": "The name of the user.",
                                                "example": "My Name"
                                            },
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user. Must be a valid email format.",
                                                "example": "Email@hotmail.com"
                                            },
                                            "password": {
                                                "type": "string",
                                                "description": "The password for the new account. Must contain at least one uppercase letter, one number, and one special character.",
                                                "example": "Teste1234!"
                                            },
                                            "department": {
                                                "type": "string",
                                                "description": "A string representing a valid department with a maximum of 255 characters.",
                                                "example": "financeiro"
                                            }
                                        },
                                        "required": [
                                            "name",
                                            "email",
                                            "password",
                                            "department"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "201": {
                                "description": "Account successfully created.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 201
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Account created successfully, please activate your account through the link sent to your email."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/signup"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/activate-email"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                # ==============================================================
                "/ACCOUNTS_BASE_URL_REPLACE/login": {
                    "post": {
                        "summary": "Authenticate user login",
                        "description": "This endpoint allows users to log in using their email and password. If the credentials are valid and the account is active, access and refresh tokens are returned. Otherwise, an appropriate error is returned based on the account status.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user attempting to log in.",
                                                "example": "user@example.com"
                                            },
                                            "password": {
                                                "type": "string",
                                                "description": "The password associated with the user's account.",
                                                "example": "SecurePass123!"
                                            }
                                        },
                                        "required": [
                                            "email",
                                            "password"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "User logged in successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "You are logged in."
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "access": {
                                                            "type": "string",
                                                            "example": "ACCESS_TOKEN_STRING"
                                                        },
                                                        "refresh": {
                                                            "type": "string",
                                                            "example": "REFRESH_TOKEN_STRING"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/login"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "403": {
                                "description": "Account is banned or deactivated.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 403
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "We couldn't complete your login. More information has been sent to your email."
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "404": {
                                "description": "User not found or credentials are incorrect.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 404
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Invalid credentials."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                # ==============================================================
                "/ACCOUNTS_BASE_URL_REPLACE/refresh-login": {
                    "post": {
                        "summary": "Refresh user access credentials",
                        "description": "This endpoint allows users to obtain new access and refresh tokens by providing a valid refresh token. If the token is valid and associated with an active account, the system generates new tokens. Errors are returned if the token is invalid, the account is banned, or the account is deactivated.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "refreshToken": {
                                                "type": "string",
                                                "description": "The base64-encoded refresh token issued during the last login.",
                                                "example": "REFRESH_TOKEN_STRING"
                                            }
                                        },
                                        "required": [
                                            "refreshToken"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "New tokens issued successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "You are logged in."
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "access": {
                                                            "type": "string",
                                                            "example": "ACCESS_TOKEN_STRING"
                                                        },
                                                        "refresh": {
                                                            "type": "string",
                                                            "example": "REFRESH_TOKEN_STRING"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/refresh-login"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "403": {
                                "description": "Account is banned or deactivated.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 403
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "We couldn't complete your login. More information has been sent to your email."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                # ==============================================================
                "/ACCOUNTS_BASE_URL_REPLACE/get-profile": {
                    "get": {
                        "summary": "Retrieve user profile information",
                        "description": "This endpoint returns the authenticated user's profile information, including personal details and language preferences. The request must include a valid Bearer access token in the Authorization header.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "responses": {
                            "200": {
                                "description": "Profile retrieved successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "profileImage": {
                                                            "type": "string",
                                                            "example": "https://example.com/image.jpg"
                                                        },
                                                        "name": {
                                                            "type": "string",
                                                            "example": "John Doe"
                                                        },
                                                        "email": {
                                                            "type": "string",
                                                            "example": "john.doe@example.com"
                                                        },
                                                        "phone": {
                                                            "type": "string",
                                                            "example": "+123456789"
                                                        },
                                                        "identityDocument": {
                                                            "type": "string",
                                                            "example": "1234567890"
                                                        },
                                                        "gender": {
                                                            "type": "string",
                                                            "example": "male"
                                                        },
                                                        "birthdate": {
                                                            "type": "string",
                                                            "example": "1990-01-01"
                                                        },
                                                        "biography": {
                                                            "type": "string",
                                                            "example": "I'm John Doe. I live everywhere. I am anyone."
                                                        },
                                                        "language": {
                                                            "type": "string",
                                                            "example": "en"
                                                        },
                                                        "theme": {
                                                            "type": "string",
                                                            "example": "dark mode"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/get-profile"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/update-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                # ==============================================================
                "/ACCOUNTS_BASE_URL_REPLACE/update-profile": {
                    "put": {
                        "summary": "Update user profile information",
                        "description": "This endpoint allows authenticated users to update their profile details such as name, phone, identity document, gender, birthdate, biography, and language. The fields are not required, but if provided in the request body, they must contain valid non-empty values. A valid Bearer token must be provided in the Authorization header.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "maxLength": 255,
                                                "description": "Full name of the user.",
                                                "example": "John Doe"
                                            },
                                            "phone": {
                                                "type": "string",
                                                "maxLength": 25,
                                                "description": "User's contact number.",
                                                "example": "+1 (123) 456-7890"
                                            },
                                            "identityDocument": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Government-issued identity document number.",
                                                "example": "AB1234567"
                                            },
                                            "gender": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Gender identification.",
                                                "example": "male"
                                            },
                                            "birthdate": {
                                                "type": "string",
                                                "description": "Date of birth in YYYY-MM-DD format.",
                                                "example": "1990-05-15"
                                            },
                                            "biography": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Short biography of the user.",
                                                "example": "A passionate developer who loves open-source."
                                            },
                                            "language": {
                                                "type": "string",
                                                "maxLength": 50,
                                                "description": "Language code in ISO 639-1 format (e.g., en, pt-BR).",
                                                "example": "en"
                                            },
                                            "theme": {
                                                "type": "string",
                                                "maxLength": 100,
                                                "description": "The theme name, which represents the colors and fonts chosen by the user.",
                                                "example": "dark mode"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Profile updated successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Profile updated successfully."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/update-profile"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/ACCOUNTS_BASE_URL_REPLACE/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                "/MODULES_BASE_URL_REPLACE/create-request": {
                    "post": {
                        "summary": "Submit module access request",
                        "description": "This endpoint allows authenticated users to request access to one or more system modules. A justification must be provided, and the request may be marked as urgent. The request is validated against business rules, existing active requests, allowed departments, and module availability. If valid, a protocol number is generated and the request becomes active. Otherwise, it is denied and stored with a rejection reason.",
                        "tags": [
                            "MODULES"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "modules": {
                                                "type": "array",
                                                "description": "List of requested modules (minimum 1, maximum 3). Each module must be a valid string.",
                                                "items": {
                                                    "type": "string",
                                                    "example": "portal do colaborador"
                                                }
                                            },
                                            "justification": {
                                                "type": "string",
                                                "description": "Text justification with 20 to 500 characters following validation rules.",
                                                "example": "I need access to these modules to perform my daily activities."
                                            },
                                            "urgent": {
                                                "type": "boolean",
                                                "description": "Whether the request is urgent.",
                                                "example": false
                                            }
                                        }
                                    }
                                }
                            }
                        },
                
                        "responses": {
                
                            "201": {
                                "description": "Request created successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 201
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Your request has been successfully created, and your access is now available! Your protocol number is: SOL-20251121-E18F"
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/MODULES_BASE_URL_REPLACE/create-request"
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        "examples": {
                                            "requestCreated": {
                                                "summary": "Request created successfully",
                                                "value": {
                                                    "statusCode": 201,
                                                    "statusMessage": "success",
                                                    "message": "Your request has been successfully created, and your access is now available! Your protocol number is: SOL-20251121-E18F",
                                                    "links": {
                                                        "self": "/MODULES_BASE_URL_REPLACE/create-request"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                
                            "400": {
                                "description": "Validation errors or business rule violations.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 400
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Request denied. Reason: You already have an active request for the module: PORTAL DO COLABORADOR"
                                                }
                                            }
                                        },
                
                                        "examples": {
                
                                            "manyModules": {
                                                "summary": "Modules list invalid",
                                                "value": {
                                                    "statusCode": 400,
                                                    "statusMessage": "error",
                                                    "message": "Request denied. Reason: You must request between 1 and 3 modules."
                                                }
                                            },
                
                                            "alreadyRequested": {
                                                "summary": "User already has active request for the module",
                                                "value": {
                                                    "statusCode": 400,
                                                    "statusMessage": "error",
                                                    "message": "Request denied. Reason: You already have an active request for the module: PORTAL DO COLABORADOR"
                                                }
                                            },
                
                                            "modulesDontExist": {
                                                "summary": "Modules do not exist or are inactive",
                                                "value": {
                                                    "statusCode": 400,
                                                    "statusMessage": "error",
                                                    "message": "Request denied. Reason: Some of the requested modules do not exist or are not available."
                                                }
                                            },
                
                                            "moduleNotAllowed": {
                                                "summary": "User department not allowed to request a module",
                                                "value": {
                                                    "statusCode": 400,
                                                    "statusMessage": "error",
                                                    "message": "Request denied. You do not have access to the module: PORTAL DO COLABORADOR"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                """
            )

            .append(
                """
                }}
                # ==============================================================
                """
            ).toString();

        // Replace strings
        // ---------------------------------------------------------------------
        return docs
            .replace("TITLE_REPLACE", applicationTitle.toUpperCase())
            .replace("PUBLIC_DOMAIN_REPLACE", publicDomain.split(",")[0].trim())
            .replace("HELLOWORLD_BASE_URL_REPLACE", helloWorldBaseURL)
            .replace("ACCOUNTS_BASE_URL_REPLACE", accountsBaseURL)
            .replace("MODULES_BASE_URL_REPLACE", modulesBaseURL);
        // ---------------------------------------------------------------------

    }

}