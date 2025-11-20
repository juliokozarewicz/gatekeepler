package modules.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "modules_mutually_exclusive",
    uniqueConstraints = @UniqueConstraint(columnNames = {"module_a_name", "module_b_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModulesMutuallyExclusiveEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "module_a_name", nullable = false, length = 255)
    private String moduleAName;

    @Column(name = "module_b_name", nullable = false, length = 255)
    private String moduleBName;
}