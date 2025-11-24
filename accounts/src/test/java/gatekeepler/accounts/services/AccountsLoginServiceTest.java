package gatekeepler.accounts.services;

import gatekeepler.accounts.dtos.AccountsLoginDTO;
import gatekeepler.accounts.exceptions.ErrorHandler;
import gatekeepler.accounts.persistence.entities.AccountsEntity;
import gatekeepler.accounts.persistence.repositories.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountsLoginServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private ErrorHandler errorHandler;
    @Mock private EncryptionService encryptionService;
    @Mock private AccountsRepository accountsRepository;
    @Mock private AccountsManagementService accountsManagementService;

    private AccountsLoginService accountsLoginService;

    private final String TEST_EMAIL = "test@email.com";
    private final String TEST_PASSWORD = "ValidPassword123";
    private final String HASHED_PASSWORD = "hashed_valid_password";
    private final String INVALID_PASSWORD = "WrongPassword123";
    private final String USER_IP = "192.168.1.1";
    private final String USER_AGENT = "TestAgent";
    private final UUID USER_ID = UUID.randomUUID();
    private final String ACCESS_TOKEN = "jwt-access-token";
    private final String REFRESH_TOKEN = "refresh-token-uuid";
    private final String LOGIN_SUCCESS_MESSAGE = "Login realizado com sucesso.";
    private final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas.";
    private final String LOGIN_ERROR_MESSAGE = "Erro no login.";
    private final String ACCOUNTS_BASE_URL = "v1/accounts";
    private final Locale DEFAULT_LOCALE = Locale.getDefault();

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        accountsLoginService = new AccountsLoginService(
            messageSource,
            errorHandler,
            encryptionService,
            accountsRepository,
            accountsManagementService
        );

        java.lang.reflect.Field field = AccountsLoginService.class.getDeclaredField("accountsBaseURL");
        field.setAccessible(true);
        field.set(accountsLoginService, ACCOUNTS_BASE_URL);

        when(messageSource.getMessage(eq("response_login_success"), isNull(), eq(DEFAULT_LOCALE)))
            .thenReturn(LOGIN_SUCCESS_MESSAGE);
        when(messageSource.getMessage(eq("response_invalid_credentials"), isNull(), eq(DEFAULT_LOCALE)))
            .thenReturn(INVALID_CREDENTIALS_MESSAGE);
        when(messageSource.getMessage(eq("response_login_error"), isNull(), eq(DEFAULT_LOCALE)))
            .thenReturn(LOGIN_ERROR_MESSAGE);
    }

    private AccountsLoginDTO createLoginDTO(String email, String password) {
        return new AccountsLoginDTO(email, password);
    }

    private AccountsEntity createActiveUser() {
        AccountsEntity user = new AccountsEntity();
        user.setId(USER_ID);
        user.setEmail(TEST_EMAIL.toLowerCase());
        user.setPassword(HASHED_PASSWORD);
        user.setActive(true);
        user.setBanned(false);
        return user;
    }

    @Test
    void testExecute_ValidCredentials_ShouldReturnSuccessAndTokens() {
        AccountsLoginDTO dto = createLoginDTO(TEST_EMAIL, TEST_PASSWORD);
        AccountsEntity activeUser = createActiveUser();

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.of(activeUser));
        when(encryptionService.matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(true);

        when(accountsManagementService.createCredentialJWT(eq(TEST_EMAIL.toLowerCase()))).thenReturn(ACCESS_TOKEN);
        when(accountsManagementService.createRefreshLogin(
            eq(USER_ID),
            eq(USER_IP),
            eq(USER_AGENT),
            isNull()
        )).thenReturn(REFRESH_TOKEN);

        ResponseEntity responseEntity = accountsLoginService.execute(USER_IP, USER_AGENT, dto);

        assertEquals(200, responseEntity.getStatusCodeValue());

        StandardResponseService responseBody = (StandardResponseService) responseEntity.getBody();
        assertEquals(LOGIN_SUCCESS_MESSAGE, responseBody.getMessage());

        assertEquals(ACCESS_TOKEN, ((Map)responseBody.getData()).get("access"));
        assertEquals(REFRESH_TOKEN, ((Map)responseBody.getData()).get("refresh"));

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(encryptionService, times(1)).matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD));
        verify(accountsManagementService, times(1)).createCredentialJWT(eq(TEST_EMAIL.toLowerCase()));
        verify(accountsManagementService, times(1)).createRefreshLogin(eq(USER_ID), eq(USER_IP), eq(USER_AGENT), isNull());
        verify(accountsManagementService, times(1)).deleteExpiredRefreshTokensListById(eq(USER_ID));
        verify(errorHandler, never()).customErrorThrow(eq(401), eq(INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    void testExecute_UserNotFound_ShouldThrow401() {
        AccountsLoginDTO dto = createLoginDTO(TEST_EMAIL, TEST_PASSWORD);

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.empty());

        doThrow(new RuntimeException(INVALID_CREDENTIALS_MESSAGE))
            .when(errorHandler)
            .customErrorThrow(eq(401), eq(INVALID_CREDENTIALS_MESSAGE));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            accountsLoginService.execute(USER_IP, USER_AGENT, dto)
        );

        assertEquals(INVALID_CREDENTIALS_MESSAGE, exception.getMessage());

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(errorHandler, times(1)).customErrorThrow(eq(401), eq(INVALID_CREDENTIALS_MESSAGE));

        verify(encryptionService, never()).matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD));
        verify(accountsManagementService, never()).createCredentialJWT(eq(TEST_EMAIL.toLowerCase()));
    }

    @Test
    void testExecute_InvalidPassword_ShouldThrow401() {
        AccountsLoginDTO dto = createLoginDTO(TEST_EMAIL, INVALID_PASSWORD);
        AccountsEntity activeUser = createActiveUser();

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.of(activeUser));
        when(encryptionService.matchPasswords(eq(INVALID_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(false);

        doThrow(new RuntimeException(INVALID_CREDENTIALS_MESSAGE))
            .when(errorHandler)
            .customErrorThrow(eq(401), eq(INVALID_CREDENTIALS_MESSAGE));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            accountsLoginService.execute(USER_IP, USER_AGENT, dto)
        );

        assertEquals(INVALID_CREDENTIALS_MESSAGE, exception.getMessage());

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(encryptionService, times(1)).matchPasswords(eq(INVALID_PASSWORD), eq(HASHED_PASSWORD));
        verify(errorHandler, times(1)).customErrorThrow(eq(401), eq(INVALID_CREDENTIALS_MESSAGE));
        verify(accountsManagementService, never()).createCredentialJWT(eq(TEST_EMAIL.toLowerCase()));
    }

    @Test
    void testExecute_BannedAccount_ShouldRevokeTokensAndThrow403() {
        AccountsLoginDTO dto = createLoginDTO(TEST_EMAIL, TEST_PASSWORD);
        AccountsEntity bannedUser = createActiveUser();
        bannedUser.setBanned(true);

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.of(bannedUser));
        when(encryptionService.matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(true);

        doThrow(new RuntimeException(LOGIN_ERROR_MESSAGE))
            .when(errorHandler)
            .customErrorThrow(eq(403), eq(LOGIN_ERROR_MESSAGE));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            accountsLoginService.execute(USER_IP, USER_AGENT, dto)
        );

        assertEquals(LOGIN_ERROR_MESSAGE, exception.getMessage());

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(encryptionService, times(1)).matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD));
        verify(accountsManagementService, times(1)).deleteAllRefreshTokensByIdNewTransaction(eq(USER_ID));
        verify(errorHandler, times(1)).customErrorThrow(eq(403), eq(LOGIN_ERROR_MESSAGE));
        verify(accountsManagementService, never()).createCredentialJWT(eq(TEST_EMAIL.toLowerCase()));
    }

    @Test
    void testExecute_DeactivatedAccount_ShouldRevokeTokensAndThrow403() {
        AccountsLoginDTO dto = createLoginDTO(TEST_EMAIL, TEST_PASSWORD);
        AccountsEntity deactivatedUser = createActiveUser();
        deactivatedUser.setActive(false);

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.of(deactivatedUser));
        when(encryptionService.matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(true);

        doThrow(new RuntimeException(LOGIN_ERROR_MESSAGE))
            .when(errorHandler)
            .customErrorThrow(eq(403), eq(LOGIN_ERROR_MESSAGE));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            accountsLoginService.execute(USER_IP, USER_AGENT, dto)
        );

        assertEquals(LOGIN_ERROR_MESSAGE, exception.getMessage());

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(encryptionService, times(1)).matchPasswords(eq(TEST_PASSWORD), eq(HASHED_PASSWORD));
        verify(accountsManagementService, times(1)).deleteAllRefreshTokensByIdNewTransaction(eq(USER_ID));
        verify(errorHandler, times(1)).customErrorThrow(eq(403), eq(LOGIN_ERROR_MESSAGE));

        verify(accountsManagementService, never()).createCredentialJWT(eq(TEST_EMAIL.toLowerCase()));
    }
}