package com.fs.tabulaplus;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * This class is the base class for any PDF Parser. To parse a new type of PDF documents,
 * you need to add a new class that extends this class (PdfParser) and add transform
 * functions to the 'transformFuncs' property.
 * The steps for the parsing process is:
 * 1. Read the schema file, parse it and get the root PDF section
 * 2. Extract data from section in recursive fashion, meaning go from parent section to child sections
 *    2.1 For a section, retrieve all page areas that the section is on
 *    2.2 Create a normalized table from data extracted from all these page areas
 *    2.3 Do post-processing on the normalized table to correct data
 *    2.4 Change data to JSON-like form, and add data from child sections as values of fields in the parent section
 *
 * Created by hailegia on 27/10/16.
 */
public class PdfParser {
    private Logger logger = LoggerFactory.getLogger(PdfParser.class);

    /**
     * The root section that contains other section
     */
    private PdfSection[] pdfSections;

    /**
     * Constructor
     * @throws IOException
     */
    public PdfParser(PdfSection[] pdfSections) throws IOException {
        this.pdfSections = pdfSections;
    }

    /**
     * Main parsing function to parse pdf files
     * @param pdfFile
     * @return
     * @throws IOException
     * @throws CryptographyException
     */
    public NormalizedTable[] parse(String pdfFile) throws IOException, CryptographyException {
        if (pdfFile.contains(".pdf"))
            return parse(new File(pdfFile));
        return null;
    }

    public NormalizedTable[] parse(File pdfFile) throws IOException, CryptographyException {
        return parse(new FileInputStream(pdfFile));
    }

    public NormalizedTable[] parse(InputStream pdfFile) throws IOException, CryptographyException {
        PDDocument document = PDDocument.load(pdfFile);
        try {
            return parse(document);
        } finally {
            logger.info("Close the PDF file");
            pdfFile.close();
        }
    }

    public NormalizedTable[] parse(PDDocument document) throws IOException, CryptographyException {
        if (document.isEncrypted()) {
            document.decrypt("");
        }
        ObjectExtractor oe = new ObjectExtractor(document);
        List<NormalizedTable> tables = new ArrayList<>();
        for (PdfSection section : this.pdfSections)
        {
            try {
                NormalizedTable resultTable = extractData(document, oe, section);
                tables.add(resultTable);
            } catch (Exception e) {
                logger.info("Exception: ", e);
            }
        }
        document.close();
        oe.close();

        NormalizedTable[] normalizedTables = new NormalizedTable[tables.size()];
        for (int i=0; i<tables.size(); i++)
        {
            normalizedTables[i] = tables.get(i);
        }
        return normalizedTables;
    }

    /**
     * This is a recursive function that extracts all information from the current section and all its child sections
     * @param document
     * @param oe
     * @param section
     * @return
     */
    private NormalizedTable extractData(PDDocument document, ObjectExtractor oe, PdfSection section) {
        String top = section.getTopIdentifier();
        String left = section.getLeftIdentifier();
        String[] bottoms = section.getBottomIdentifiers();
        String right = section.getRightIdentifier();
        if (top != null || left != null || bottoms != null || right != null) {
            // get page areas that the current section is on
            PdfSectionLocator sectionLocator = new PdfSectionLocator(oe, section);
            List<Page> pages = null;
            try {
                pages = sectionLocator.locateSection(document, oe.extract(1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) { // catch all other exceptions
                logger.error("Exception: ", e);
            }

            if (pages != null && !pages.isEmpty())
            {
                // create a normalized table with the data extracted from pages
                NormalizedTable normalizedTable = new NormalizedTable(pages, NormalizedTable.TEXT_ALGORITHM);
                normalizedTable.setTableName(section.getName());
                return normalizedTable;
            }
            else
            {
                logger.info(section.getName() + " does not exist!");
                return new NormalizedTable();
            }
        }
        else
        {
            logger.info(section.getName() + " does not exist!");
            return new NormalizedTable();
        }
    }
}
