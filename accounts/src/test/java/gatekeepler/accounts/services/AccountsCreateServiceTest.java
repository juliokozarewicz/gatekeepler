package gatekeepler.accounts.services;

import gatekeepler.accounts.dtos.AccountsCreateDTO;
import gatekeepler.accounts.enums.AccountsUpdateEnum;
import gatekeepler.accounts.enums.UserLevelEnum;
import gatekeepler.accounts.exceptions.ErrorHandler;
import gatekeepler.accounts.persistence.entities.AccountsEntity;
import gatekeepler.accounts.persistence.entities.AccountsProfileEntity;
import gatekeepler.accounts.persistence.repositories.AccountsProfileRepository;
import gatekeepler.accounts.persistence.repositories.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountsCreateServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private AccountsRepository accountsRepository;
    @Mock private AccountsProfileRepository accountsProfileRepository;
    @Mock private AccountsManagementService accountsManagementService;
    @Mock private CacheManager cacheManager;
    @Mock private Cache notActivatedAccountCache;
    @Mock private EncryptionService encryptionService;
    @Mock private ErrorHandler errorHandler;

    private AccountsCreateService accountsCreateService;

    private final String TEST_EMAIL = "ti@email.com";
    private final String TEST_NAME = "Test User";
    private final String TEST_DEPARTMENT = "TI";
    private final String TEST_PASSWORD = "Teste123456!";
    private final String HASHED_PASSWORD = "hashed_password";
    private final UUID GENERATED_ID = UUID.randomUUID();
    private final String SUCCESS_MESSAGE = "Conta criada com sucesso.";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(encryptionService.hashPassword(eq(TEST_PASSWORD))).thenReturn(HASHED_PASSWORD);

        when(cacheManager.getCache(eq("notActivatedAccountCache"))).thenReturn(notActivatedAccountCache);

        when(messageSource.getMessage(eq("response_account_created_successfully"), eq(null), eq(Locale.getDefault())))
            .thenReturn(SUCCESS_MESSAGE);

        when(accountsManagementService.createUniqueId()).thenReturn(GENERATED_ID);
        when(accountsManagementService.createVerificationToken(eq(GENERATED_ID), eq(AccountsUpdateEnum.ACTIVATE_ACCOUNT)))
            .thenReturn("TEST_TOKEN");

        accountsCreateService = new AccountsCreateService(
            messageSource,
            errorHandler,
            encryptionService,
            accountsRepository,
            accountsProfileRepository,
            accountsManagementService,
            cacheManager
        );
    }

    @Test
    void testExecute_NewUser_ShouldCreateAccountAndProfileAndReturn201() {
        AccountsCreateDTO dto = new AccountsCreateDTO(TEST_NAME, TEST_DEPARTMENT, TEST_EMAIL, TEST_PASSWORD);

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.empty());

        ArgumentCaptor<AccountsEntity> accountCaptor = ArgumentCaptor.forClass(AccountsEntity.class);
        ArgumentCaptor<AccountsProfileEntity> profileCaptor = ArgumentCaptor.forClass(AccountsProfileEntity.class);
        ArgumentCaptor<Instant> cacheCaptor = ArgumentCaptor.forClass(Instant.class);

        ResponseEntity<?> response = accountsCreateService.execute(dto);

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(accountsRepository, times(1)).save(accountCaptor.capture());
        verify(accountsProfileRepository, times(1)).save(profileCaptor.capture());
        verify(notActivatedAccountCache, times(1)).put(eq(GENERATED_ID), cacheCaptor.capture());
        verify(accountsManagementService, times(1)).createVerificationToken(eq(GENERATED_ID), eq(AccountsUpdateEnum.ACTIVATE_ACCOUNT));

        AccountsEntity savedAccount = accountCaptor.getValue();
        assertEquals(GENERATED_ID, savedAccount.getId());
        assertEquals(TEST_EMAIL.toLowerCase(), savedAccount.getEmail());
        assertEquals(HASHED_PASSWORD, savedAccount.getPassword());
        assertEquals(UserLevelEnum.USER, savedAccount.getLevel());

        AccountsProfileEntity savedProfile = profileCaptor.getValue();
        assertEquals(GENERATED_ID, savedProfile.getId());
        assertEquals(TEST_NAME, savedProfile.getName());

        StandardResponseService body = (StandardResponseService) response.getBody();
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(SUCCESS_MESSAGE, body.getMessage());
    }

    @Test
    void testExecute_ExistingUser_ShouldSkipCreationAndReturn201() {
        AccountsCreateDTO dto = new AccountsCreateDTO(TEST_NAME, TEST_DEPARTMENT, TEST_EMAIL, TEST_PASSWORD);

        AccountsEntity existing = new AccountsEntity();
        existing.setId(GENERATED_ID);

        when(accountsRepository.findByEmail(eq(TEST_EMAIL.toLowerCase()))).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = accountsCreateService.execute(dto);

        verify(accountsRepository, times(1)).findByEmail(eq(TEST_EMAIL.toLowerCase()));
        verify(accountsRepository, never()).save(argThat(account -> account.getId().equals(GENERATED_ID)));
        verify(accountsProfileRepository, never()).save(argThat(profile -> profile.getId().equals(GENERATED_ID)));
        verify(notActivatedAccountCache, never()).put(eq(GENERATED_ID), argThat(timestamp -> timestamp != null));

        StandardResponseService body = (StandardResponseService) response.getBody();
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(SUCCESS_MESSAGE, body.getMessage());
    }

}