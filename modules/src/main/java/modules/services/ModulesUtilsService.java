package modules.services;

import modules.dtos.ModulesCreateRequestDTO;
import modules.persistence.entities.ModulesRequestEntity;
import modules.persistence.repositories.ModulesRequestRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ModulesUtilsService {

    // ==================================================== ( constructor init )

    private final MessageSource messageSource;
    private final ModulesRequestRepository modulesRequestRepository;

    public ModulesUtilsService(
        MessageSource messageSource,
        ModulesRequestRepository modulesRequestRepository
        ) {
        this.messageSource = messageSource;
        this.modulesRequestRepository = modulesRequestRepository;
    }

    // ===================================================== ( constructor end )

    // Protocol number
    public static String generateProtocolNumber(Instant nowUtc) {
        String prefix = "SOL";
        String date = nowUtc.toString().substring(0, 10).replace("-", "");
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return String.format("%s-%s-%s", prefix, date, uniqueId);
    }

    // Create requests
    public void createCommitRequestStatus(
        String protocolNumber,
        String status,
        String denialReason,
        ModulesCreateRequestDTO modulesCreateRequestDTO,
        String idUser
    ) {
        ModulesRequestEntity newRequest = new ModulesRequestEntity();
        newRequest.setId(UUID.randomUUID());
        newRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        newRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        newRequest.setProtocolNumber(protocolNumber);
        newRequest.setModuleNamesRequested(
            modulesCreateRequestDTO.modules().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList())
        );
        newRequest.setJustification(modulesCreateRequestDTO.justification());
        newRequest.setUrgent(modulesCreateRequestDTO.urgent());
        newRequest.setStatus(status);
        newRequest.setDenialReason(denialReason);
        newRequest.setIdUser(idUser);

        modulesRequestRepository.save(newRequest);
    }

    // Create renew requests
    public void renewRequestStatus(
        String protocolNumber,
        String status,
        String denialReason,
        ModulesRequestEntity existingRequest,
        String idUser
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Old request
        existingRequest.setStatus("cancelado");
        existingRequest.setCancelReason(
            messageSource.getMessage(
                "response_reason_renew_success",
                null,
                locale
            ) + " " + protocolNumber
        );
        existingRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        modulesRequestRepository.save(existingRequest);

        // New request
        ModulesRequestEntity renewRequest = new ModulesRequestEntity();
        renewRequest.setId(UUID.randomUUID());
        renewRequest.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        renewRequest.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        renewRequest.setProtocolNumber(protocolNumber);
        renewRequest.setModuleNamesRequested(existingRequest.getModuleNamesRequested());
        renewRequest.setJustification(existingRequest.getJustification());
        renewRequest.setUrgent(existingRequest.isUrgent());
        renewRequest.setStatus(status);
        renewRequest.setDenialReason(denialReason);
        renewRequest.setIdUser(idUser);

        if (existingRequest.getProtocolNumber() != null) {
            renewRequest.setLinkedProtocol(existingRequest.getProtocolNumber());
        }
        modulesRequestRepository.save(renewRequest);

    }

}