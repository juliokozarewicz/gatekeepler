package modules.persistence.repositories;

import modules.persistence.entities.ModulesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModulesRepository extends JpaRepository<ModulesEntity, UUID> {

    Optional<ModulesEntity> findByName(String name);

    List<ModulesEntity> findByActiveTrue();

    Optional<ModulesEntity> findById(UUID id);

}
