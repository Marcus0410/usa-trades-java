import com.usatrades.Trade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;

public class OutputTests {
    @Test
    public void testOutputMethod() {
        LocalDate td = LocalDate.parse("2024-09-04");
        LocalDate sd = LocalDate.parse("2024-09-05");
        Trade trade = new Trade(7000, td, sd, "US46125A1007", 43.75, 'S', "LUNR", 0.96, 34325.29, 43.75, 400860);

        double outputPrice = trade.get_output_price();
        assertEquals(4.9038, outputPrice);
    }
}
