package fh.swen.paperless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating a tag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String name;
}
