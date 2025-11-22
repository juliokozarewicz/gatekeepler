package modules.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
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
public class ModulesRequestEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(updatable = false, nullable = false)
    private String protocolNumber;

    @ElementCollection
    @CollectionTable(name = "module_names_requested", joinColumns = @JoinColumn(name = "module_request_id"))
    @Column(name = "module_name")
    private List<String> moduleNamesRequested;

    @Column(nullable = false, length = 500)
    private String justification;

    @Column(nullable = false)
    private boolean urgent;

    @Column(nullable = false)
    private String status;

    @Column(nullable = true, length = 255)
    private String linkedProtocol;

    @Column(nullable = true, length = 500)
    private String denialReason;

    @Column(nullable = true, length = 200)
    private String cancelReason;

    @Column(updatable = false, nullable = false)
    private String idUser;
}
