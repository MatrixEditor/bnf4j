# Project: bnf4j

![author](https://img.shields.io:/static/v1?label=Author:&message=MatrixEditor&color=grey)
![build](https://img.shields.io:/static/v1?label=Build&message=passing&color=green)
![Issues](https://img.shields.io:/static/v1?label=Issues&message=Infinity&color=orange)


The Project `lang4j` was supposed to be a library or API to generate individual parsers for different filetypes. Though, this option is not implemented yet, the described parsers can be created using an event-based parsing model located in the `staf` (STreaming Api for Files) module.

### STAF (Streaming API for Files)

---

The `staf` module contains a streaming API used to create event-based parsers. To create such a parser you have to implement the following classes: `FragmentFileScanner` for reading and resolving events, `FragmentDriver` to do the actual reading, `EventAllocator` to create Events and optional an `InputFactory` if you want to have a factory that creates the instances of the both classes described before.

<strong>Note:</strong> The implemented CSV-Parser will be updated in the future to be more configurable (set new `COMMA` value for example).

Using a `LangStream` to iterate over every `LangEvent` or implemented features from the own stream. The basic usage example can be seen from the implemented CSV-Parser:

````java
CSVParser parser = CSVParser.stream().setSource(new File("someData.csv"));
CSVTable table = parser.toObject();
````

It is possible to reuse the parser on a different source. **Important:** You have to use the `COMMA` specified in `CSVCharSet`, which is `,`.
````java
CSVTable = CSVParser.stream()
                    .setSource("col1,col2,col3\nvalue1,value2,value3")
                    .toObject();

CSVParser.stream().setSource(new File("someData.csv"))
         .forEach(event -> {
             //...
        });
````

### ABNF (Augmented BNF)

---
The module named `abnf` makes use of the event-based parsing model above and creates grammars according to the defined rules. Yet, it is not possible to parse `.bnf` files, because the rule-naming is not handled.

The example below shows the usage of this module. Let's consider we want to match a simple mail format. First we think of some rules that are necessary:

    name      pseudocode                                BNF-Rule
    ------    ---------------------------------------   ----------------------------
    CHAR   := A-Z OR a-z                              = %x41-5A / %x61-7A
    DOMAIN := . AND ( de OR com OR gmx )              = "." ( "de" / "com" / "gmx" )
    MAIL   := *<CHAR> AND @ AND *<CHAR> AND <DOMAIN>  = 1*CHAR "@" 1*CHAR DOMAIN

This definition can be converted into the java-format:
````java
BNF bnf = BNF.getInstance();
bnf.rule("CHAR", "%x41-5A / %x61-7A"); // Characters from A-Z or a-z
bnf.rule("DOMAIN", "'.' ( 'de' / 'com' / 'gmx' )") // e.g.: '.com', '.de' or '.gmx'

BNFElement<?> mail = bnf.rule("mail", "1*CHAR '@' 1*CHAR DOMAIN");
assert bnf.is(mail, "hello@world.com");
````

