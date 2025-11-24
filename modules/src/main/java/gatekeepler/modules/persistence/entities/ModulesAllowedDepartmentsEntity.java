package gatekeepler.modules.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "modules_allowed_departments",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_name", "department"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModulesAllowedDepartmentsEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "module_name", nullable = false, length = 255)
    private String moduleName;

    @Column(name = "department", nullable = false, length = 255)
    private String department;
}