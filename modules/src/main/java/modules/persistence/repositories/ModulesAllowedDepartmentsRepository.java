package modules.persistence.repositories;

import modules.persistence.entities.ModulesAllowedDepartmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModulesAllowedDepartmentsRepository
    extends JpaRepository<ModulesAllowedDepartmentsEntity, UUID> {

    List<ModulesAllowedDepartmentsEntity> findByModuleName(String moduleName);

    boolean existsByModuleNameAndDepartment(String moduleName, String department);

}