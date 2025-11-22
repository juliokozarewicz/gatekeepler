package modules.persistence.specifications;

import jakarta.persistence.criteria.Predicate;
import modules.persistence.entities.ModulesRequestEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ModulesReadRequestsSpecification {

    public static Specification<ModulesRequestEntity> filter(
        String protocolNumber,
        String moduleName,
        String status,
        Boolean urgent,
        LocalDate initDate,
        LocalDate endDate,
        String idUser
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("idUser"), idUser));

            // Protocolo
            if (protocolNumber != null && !protocolNumber.isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("protocolNumber")),
                    "%" + protocolNumber.toLowerCase() + "%"
                ));
            }

            if (moduleName != null && !moduleName.isBlank()) {
                predicates.add(cb.isMember(moduleName.toLowerCase(), root.get("moduleNamesRequested")));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            if (urgent != null) {
                predicates.add(cb.equal(root.get("urgent"), urgent));
            }

            if (initDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),initDate)
                );
            }

            if (endDate != null) {predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
