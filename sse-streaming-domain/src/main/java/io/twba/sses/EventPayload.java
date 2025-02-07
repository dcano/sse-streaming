package io.twba.sses;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record EventPayload(@NotNull @NotEmpty String payload, @NotNull @NotEmpty String type) {
}
