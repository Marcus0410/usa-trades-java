import com.usatrades.Trade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;

public class OutputTests {
    private LocalDate td = LocalDate.parse("2024-09-03");
    private LocalDate sd = LocalDate.parse("2024-09-04");
    private Trade lunr = new Trade(7000, td, sd, "US46125A1007", 43.75, 'S', "LUNR", 0.96, 34325.29, 43.75, 400860);

    @Test
    public void outputPriceTest() {
        double outputPrice = lunr.get_output_price();
        assertEquals(4.9038, outputPrice);
    }

    @Test
    public void convertionTest() {
        // find smid
        lunr.setSmid(585277);
        String convertion = lunr.output();
        String correct = "	0		21	585277	7000	4.9038	03/09/2024	04/09/2024\n"
                + "	400860		21	585277	-7000	4.9038	03/09/2024	04/09/2024";

        assertEquals(correct, convertion);
    }
}
// -Kunde- 21 585277 7000 4.9038 03/09/2024 04/09/2024
// 400860 21 585277 -7000 4.9038 03/09/2024 04/09/2024
