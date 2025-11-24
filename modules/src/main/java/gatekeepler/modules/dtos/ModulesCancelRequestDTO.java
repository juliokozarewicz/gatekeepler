package gatekeepler.modules.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ModulesCancelRequestDTO(

    @NotEmpty(message = "{validation_is_required}")
    @Size(max = 200, message = "{validation_many_characters}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?!.*(.)\\1{4})[\\p{L}0-9 .,!?:;\\-]{20,200}$",
        message = "{validation_invalid_justification_size}" //
    )
    String justification

) {}