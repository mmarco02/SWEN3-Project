package fh.swen.paperless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart request body for uploading a new document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadDTO {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private Long ownerId;

    @NotNull
    private MultipartFile file;
}
