import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Process {
    String id;
    Long start;
    Long end;
}
