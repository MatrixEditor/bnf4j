package concat; //@date 01.04.2022

import com.bnf.BNF;
import com.bnf.BNFElement;

import java.io.IOException;
import java.text.ParseException;

public class BNFParserTest {

    public static void main(String[] args) throws IOException, ParseException {
        BNF bnf = BNF.getInstance();

        BNFElement<?> youOrWorld = bnf.rule("'You'").or(bnf.rule("'World'"));

        BNFElement<?> example = bnf.rule("example", bnf.rule("'Hello'").append(youOrWorld));

        System.out.println(bnf.is(example, "Hello World"));
        System.out.println(bnf.is(example, "Hello You"));
    }
}
