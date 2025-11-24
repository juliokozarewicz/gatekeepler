package gatekeepler.accounts.services;

import gatekeepler.accounts.dtos.AccountsCreateDTO;
import gatekeepler.accounts.enums.AccountsUpdateEnum;
import gatekeepler.accounts.enums.UserLevelEnum;
import gatekeepler.accounts.exceptions.ErrorHandler;
import gatekeepler.accounts.persistence.entities.AccountsEntity;
import gatekeepler.accounts.persistence.entities.AccountsProfileEntity;
import gatekeepler.accounts.persistence.repositories.AccountsProfileRepository;
import gatekeepler.accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsCreateService {

    // ==================================================== ( constructor init )

    // Env
    // -------------------------------------------------------------------------
    @Value("${ACCOUNTS_BASE_URL}")
    private String accountsBaseURL;

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final AccountsProfileRepository accountsProfileRepository;
    private final AccountsManagementService accountsManagementService;
    private final CacheManager cacheManager;
    private final Cache notActivatedAccountCache;

    public AccountsCreateService (

        MessageSource messageSource,
        ErrorHandler errorHandler,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;
        this.cacheManager = cacheManager;
        this.notActivatedAccountCache = cacheManager.getCache("notActivatedAccountCache");

    }

    // ===================================================== ( constructor end )

    @Transactional
    public ResponseEntity execute(

        AccountsCreateDTO accountsCreateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Encrypted email
        String encryptedEmail = encryptionService.encrypt(
            accountsCreateDTO.email().toLowerCase()
        );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateDTO.email().toLowerCase()
        );

        // ID and Timestamp
        UUID generatedUniqueId = accountsManagementService.createUniqueId();
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // user not find
        // ---------------------------------------------------------------------
        if ( findUser.isEmpty() ) {

            // Create Account
            AccountsEntity newAccount = new AccountsEntity();
            newAccount.setId(generatedUniqueId);
            newAccount.setCreatedAt(nowUtc);
            newAccount.setUpdatedAt(nowUtc);
            newAccount.setLevel(UserLevelEnum.USER);
            newAccount.setDepartment(accountsCreateDTO.department().toLowerCase());
            newAccount.setEmail(accountsCreateDTO.email().toLowerCase());
            newAccount.setPassword(
                encryptionService.hashPassword(
                    accountsCreateDTO.password()
                )
            );
            newAccount.setActive(true);
            newAccount.setBanned(false);
            accountsRepository.save(newAccount);

            // Create profile
            AccountsProfileEntity newProfile = new AccountsProfileEntity();
            newProfile.setId(generatedUniqueId);
            newProfile.setCreatedAt(nowUtc);
            newProfile.setUpdatedAt(nowUtc);
            newProfile.setName(accountsCreateDTO.name());
            accountsProfileRepository.save(newProfile);

            // Create token
            String tokenGenerated = accountsManagementService.createVerificationToken(
                findUser.isPresent() ? findUser.get().getId() : generatedUniqueId,
                AccountsUpdateEnum.ACTIVATE_ACCOUNT
            );

            // Set cache
            notActivatedAccountCache.put(generatedUniqueId, nowUtc);

        }
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/" + accountsBaseURL + "/signup");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_account_created_successfully",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);
        // ---------------------------------------------------------------------

    }

}