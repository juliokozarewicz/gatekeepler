package modules.persistence.specifications;



import jakarta.persistence.criteria.Predicate;
import modules.persistence.entities.ModulesRequestEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModulesReadRequestsSpecification {

    public static Specification<ModulesRequestEntity> filter(

        String protocolNumber,
        String moduleName,
        String status,
        String urgency,
        String userId

    ) {

        return (
            root,
            query,
            criteriaBuilder
        ) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (protocolNumber != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("protocolNumber")),
                        "%" + protocolNumber + "%"
                    )
                );
            }

            if (moduleName != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("moduleName")),
                        "%" + moduleName + "%"
                    )
                );
            }

            if (urgency != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("urgency")),
                        "%" + urgency + "%"
                    )
                );
            }

            if (status != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("status")),
                        "%" + status.toLowerCase() + "%"
                    )
                );
            }

            if (userId != null) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userId")),
                        "%" + userId + "%"
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        };

    }

}
