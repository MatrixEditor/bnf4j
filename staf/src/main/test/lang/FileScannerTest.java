package lang; //@date 30.03.2022

import com.file.impl.csv.CSVParser;
import com.file.impl.csv.CSVTable;

import java.io.IOException;
import java.text.ParseException;

public class FileScannerTest {

    public static void main(String[] args) throws IOException, ParseException {

        CSVTable table = CSVParser.stream()
                                  .setSource("test.txt")
                                  .toObject();


    }
}
