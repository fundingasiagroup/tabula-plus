package com.fs.example;

import com.fs.tabulaplus.PdfParser;
import com.fs.tabulaplus.PdfSection;
import org.apache.pdfbox.exceptions.CryptographyException;

import java.io.IOException;

/**
 * Created by thomas_nguyen on 2/23/17.
 */
public class ExamplePDFParer
{
    public static void main(String[] args) throws IOException, CryptographyException
    {
        PdfSection section = new PdfSection("Table 7");
        section.setTopIdentifier("Table 7:");
        section.setTopIncluded(false);
        String[] bottomIdentifiers = {"Table 8:"};
        section.setBottomIdentifiers(bottomIdentifiers);
        section.setBottomIncluded(false);
        PdfParser pdfParser = new PdfParser(section);
        System.out.println(pdfParser.parse("sample-tables.pdf"));
    }
}
