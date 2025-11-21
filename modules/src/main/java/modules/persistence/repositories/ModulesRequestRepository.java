package modules.persistence.repositories;

import modules.persistence.entities.ModulesRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModulesRequestRepository
    extends JpaRepository<ModulesRequestEntity, UUID>,
    JpaSpecificationExecutor<ModulesRequestEntity> {

    List<ModulesRequestEntity> findByIdUser(String idUser);

    Optional<ModulesRequestEntity> findByProtocolNumber(String protocolNumber);

    List<ModulesRequestEntity> findByStatus(String status);

    @Query("SELECT m FROM ModulesRequestEntity m JOIN m.moduleNamesRequested mn WHERE mn = :moduleName")
    List<ModulesRequestEntity> findByModuleNamesRequested(@Param("moduleName") String moduleName);

}
