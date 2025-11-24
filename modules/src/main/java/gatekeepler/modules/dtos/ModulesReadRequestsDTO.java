package gatekeepler.modules.dtos;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

public record ModulesReadRequestsDTO(

    @Size(max = 50, message = "{validation_many_characters}")
    String protocolNumber,

    @Size(max = 255, message = "{validation_many_characters}")
    String moduleName,

    @Size(max = 255, message = "{validation_many_characters}")
    String status,

    Boolean urgent,

    Integer page,

    LocalDate startDate,

    LocalDate endDate

) {}