package modules.services;

import modules.persistence.entities.ModulesRequestEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ModulesUtilsService {

    public static String generateProtocolNumber(Instant nowUtc) {
        String prefix = "SOL";
        String date = nowUtc.toString().substring(0, 10).replace("-", "");
        String uniqueId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return String.format("%s-%s-%s", prefix, date, uniqueId);
    }

    public static ModulesRequestEntity createModulesRequest(
        String protocolNumber,
        String status,
        String denialReason,
        List<String> moduleNamesRequested,
        String justification,
        boolean urgent,
        String idUser,
        String linkedProtocol) {

        ModulesRequestEntity newRequest = new ModulesRequestEntity();
        newRequest.setId(UUID.randomUUID());
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        newRequest.setCreatedAt(now.toInstant());
        newRequest.setUpdatedAt(now.toInstant());
        newRequest.setProtocolNumber(protocolNumber);
        newRequest.setModuleNamesRequested(moduleNamesRequested);
        newRequest.setJustification(justification);
        newRequest.setUrgent(urgent);
        newRequest.setStatus(status);
        newRequest.setDenialReason(denialReason);
        newRequest.setIdUser(idUser);

        if (linkedProtocol != null) {
            newRequest.setLinkedProtocol(linkedProtocol);
        }

        return newRequest;
    }

}
