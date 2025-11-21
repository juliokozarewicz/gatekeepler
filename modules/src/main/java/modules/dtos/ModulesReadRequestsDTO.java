package modules.dtos;

import jakarta.validation.constraints.Size;

public record ModulesReadRequestsDTO(

    @Size(max = 50, message = "{validation_many_characters}")
    String protocolNumber,

    @Size(max = 255, message = "{validation_many_characters}")
    String moduleName,

    @Size(max = 255, message = "{validation_many_characters}")
    String status,

    @Size(max = 50, message = "{validation_many_characters}")
    String period,

    @Size(max = 10, message = "{validation_many_characters}")
    String urgency

) {}