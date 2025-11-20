package modules.persistence.repositories;

import modules.persistence.entities.ModuleRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRequestRepository extends JpaRepository<ModuleRequestEntity, UUID> {

    List<ModuleRequestEntity> findByIdUser(String userId);

    Optional<ModuleRequestEntity> findByProtocolNumber(String protocolNumber);

    List<ModuleRequestEntity> findByStatus(String status);

    List<ModuleRequestEntity> findByModuleName(String moduleName);

}