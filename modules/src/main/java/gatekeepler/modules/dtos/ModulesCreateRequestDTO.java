package gatekeepler.modules.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ModulesCreateRequestDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 3, message = "{validation_many_modules}")
    List<
            @NotEmpty(message = "{validation_is_required}")
            @Size(max = 255, message = "{validation_many_characters}")
            @Pattern(
                regexp = "^[^<>&'\"/]*$",
                message = "{validation_disallowed_characters}"
            )
                String
            > modules,

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 500, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?!.*(.)\\1{4})[\\p{L}0-9 .,!?:;\\-]{20,500}$",
        message = "{validation_invalid_justification_size}" //
    )
    String justification,

    @NotNull(message = "{validation_is_required}")
    Boolean urgent

) {}