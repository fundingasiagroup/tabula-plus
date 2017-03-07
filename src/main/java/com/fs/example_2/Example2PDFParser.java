package com.fs.example_2;

import org.apache.pdfbox.exceptions.CryptographyException;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;

import com.fs.tabulaplus.NormalizedTable;
import com.fs.tabulaplus.PdfParser;

/**
 * Created by thomas_nguyen on 3/6/17.
 */
public class Example2PDFParser
{
    public static void main(String[] args) throws IOException, CryptographyException
    {
        PdfParser pdfParser = new PdfParser("example_2.schema");
        Map<String, NormalizedTable> tableMap = pdfParser.parse("sample-tables.pdf");
        Iterator it = tableMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey());
            System.out.println(pair.getValue());
            System.out.println("\n");

            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
