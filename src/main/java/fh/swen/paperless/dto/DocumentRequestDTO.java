package fh.swen.paperless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating and updating a document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDTO {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private Long ownerId;
}
