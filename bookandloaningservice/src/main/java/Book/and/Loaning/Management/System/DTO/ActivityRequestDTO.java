package Book.and.Loaning.Management.System.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDTO {
    private UUID userId;
    private String actionType;
    private String description;
}
