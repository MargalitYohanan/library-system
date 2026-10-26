package Book.and.Loaning.Management.System.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanHistoryDTO {
    private UUID bookId;
    private String bookCategory;
    private UUID ownerId;
}
