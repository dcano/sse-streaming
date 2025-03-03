package io.twba.sses;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record EventPayload(@NotNull @NotEmpty String content,
                    @NotNull @NotEmpty String type,
                    String referenceId) {
}
