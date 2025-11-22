package modules.persistence.repositories;

import modules.persistence.entities.ModulesMutuallyExclusiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ModulesMutuallyExclusiveRepository
    extends JpaRepository<ModulesMutuallyExclusiveEntity, UUID> {

    List<ModulesMutuallyExclusiveEntity> findByModuleAName(String moduleAName);

    List<ModulesMutuallyExclusiveEntity> findByModuleBName(String moduleBName);

    boolean existsByModuleANameAndModuleBName(String moduleAName, String moduleBName);

    List<ModulesMutuallyExclusiveEntity> findByModuleANameOrModuleBName(String moduleAName, String moduleBName);

}