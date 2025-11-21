package modules.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UUIDValidationDTO(

    @NotBlank(message = "{validation_not_empty}")
    @Size(min=1, message="{validation_not_empty}")
    @Size(max=50, message="{validation_many_characters}")
    @Pattern(
        regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
        message = "{validation_invalid_uuid}"
    )
    String id

) {}