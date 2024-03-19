import javax.swing.text.MaskFormatter;
import java.text.ParseException;

public class NumberANdCapitalLetterFormatter extends MaskFormatter {
    public NumberANdCapitalLetterFormatter() throws ParseException {
        super("##");
        setAllowsInvalid(false);
        setValidCharacters("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

    }
}
