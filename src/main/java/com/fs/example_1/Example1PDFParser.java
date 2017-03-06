package com.fs.example_1;

import com.fs.tabulaplus.NormalizedTable;
import com.fs.tabulaplus.PdfParser;
import com.fs.tabulaplus.PdfSection;
import org.apache.pdfbox.exceptions.CryptographyException;

import java.io.IOException;

/**
 * Created by thomas_nguyen on 2/23/17.
 */
public class Example1PDFParser
{
    public static void main(String[] args) throws IOException, CryptographyException
    {
        PdfSection section_1 = new PdfSection("Table 7");
        section_1.setTopIdentifier("Table 7:");
        section_1.setTopIncluded(false);
        String[] bottomIdentifiers_1 = {"Table 8:"};
        section_1.setBottomIdentifiers(bottomIdentifiers_1);
        section_1.setBottomIncluded(false);

        PdfSection section_2 = new PdfSection("Table 10");
        section_2.setTopIdentifier("layout problems)");
        section_2.setTopIncluded(false);
        String[] bottomIdentifiers_2 = {"Table 11:"};
        section_2.setBottomIdentifiers(bottomIdentifiers_2);
        section_2.setBottomIncluded(false);

        PdfSection[] sections = {section_1, section_2};
        PdfParser pdfParser = new PdfParser(sections);

        NormalizedTable[] tables = pdfParser.parse("sample-tables.pdf");
        for (NormalizedTable table : tables)
        {
            System.out.println(table.getTableName());
            System.out.println(table.toString());
            System.out.println("\n");
        }
    }
}
