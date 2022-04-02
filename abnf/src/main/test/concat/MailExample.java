package concat; //@date 01.04.2022

import com.bnf.BNF;
import com.bnf.BNFElement;

import java.io.IOException;
import java.text.ParseException;

public class MailExample {

    public static void main(String[] args) throws IOException, ParseException {
        BNF bnf = BNF.getInstance();

        bnf.rule("CHAR", "%x41-5A / %x61-7A");
        bnf.rule("DOMAIN", "'.' ( 'com' / 'de' / 'gmx' )");
        BNFElement<?> mail = bnf.rule("mail", "1*CHAR '@' 1*CHAR DOMAIN");

        System.out.println(bnf.is(mail, "hello@world.com"));
    }

}
