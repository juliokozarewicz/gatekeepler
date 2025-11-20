package modules.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
    name = "module_requests",
    uniqueConstraints = @UniqueConstraint(columnNames = "protocolNumber")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModuleRequestEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(updatable = false, nullable = false)
    private String protocolNumber;

    @Column(nullable = false, length = 255)
    private String moduleName;

    @Column(nullable = false, length = 500)
    private String justification;

    @Column(nullable = false)
    private boolean urgent;

    @Column(nullable = false)
    private String status;

    @Column(nullable = true, length = 500)
    private String denialReason;

    @Column(updatable = false, nullable = false)
    private String idUser;
}
