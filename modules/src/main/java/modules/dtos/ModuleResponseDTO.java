package modules.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ModuleResponseDTO {

    private String moduleName;
    private String description;
    private boolean isActive;
    private List<String> allowedDepartments;
    private List<String> incompatibleModules;

}
