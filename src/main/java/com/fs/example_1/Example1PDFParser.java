package com.fs.example_1;

import com.fs.tabulaplus.NormalizedTable;
import com.fs.tabulaplus.PdfParser;
import com.fs.tabulaplus.PdfSection;
import org.apache.pdfbox.exceptions.CryptographyException;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;

/**
 * Created by thomas_nguyen on 2/23/17.
 */
public class Example1PDFParser
{
    public static void main(String[] args) throws IOException, CryptographyException
    {
        PdfSection section_1 = new PdfSection("Table 7");
        String[] topIdentifiers_1 = {"Table 7:"};
        section_1.setTopIdentifiers(topIdentifiers_1);
        section_1.setTopIncluded(false);
        String[] bottomIdentifiers_1 = {"Table 8:"};
        section_1.setBottomIdentifiers(bottomIdentifiers_1);
        section_1.setBottomIncluded(false);

        PdfSection section_2 = new PdfSection("Table 10");
        String[] topIdentifiers_2 = {"layout problems)"};
        section_2.setTopIdentifiers(topIdentifiers_2);
        section_2.setTopIncluded(false);
        String[] bottomIdentifiers_2 = {"Table 11:"};
        section_2.setBottomIdentifiers(bottomIdentifiers_2);
        section_2.setBottomIncluded(false);

        PdfSection[] sections = {section_1, section_2};
        PdfParser pdfParser = new PdfParser(sections);

        Map<String, NormalizedTable> tableMap = pdfParser.parse("sample-tables.pdf");
        Iterator it = tableMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey());
            NormalizedTable table = (NormalizedTable) pair.getValue();
            System.out.println(table.toTabularString());
            System.out.println("\n");
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
