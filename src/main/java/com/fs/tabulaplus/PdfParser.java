package com.fs.tabulaplus;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;


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
     * A schema file is a file that defines the identifiers for sections in a PDF file
     */
    private PdfSchemaReader schemaReader;

    /**
     * True if a schema file is used for parsing PDF documents
     * False otherwise, which means that sections are defined and add to the parser
     */
    private boolean parsedWithSchema;

    /**
     * rootPdfSection is only available when parsing with a schema file
     */
    private PdfSection rootPdfSection;

    /**
     * Constructor
     * @throws IOException
     */
    public PdfParser(PdfSection[] pdfSections) throws IOException {
        this.pdfSections = pdfSections;
        this.parsedWithSchema = false;
    }

    public PdfParser(String schemaFile)
    {
        this.schemaReader = new PdfSchemaReader();
        this.rootPdfSection = schemaReader.read(schemaFile);
        this.parsedWithSchema = true;
    }

    /**
     * Main parsing function to parse pdf files
     * @param pdfFile
     * @return
     * @throws IOException
     * @throws CryptographyException
     */
    public Map<String, NormalizedTable> parse(String pdfFile) throws IOException, CryptographyException {
        if (pdfFile.contains(".pdf"))
            return parse(new File(pdfFile));
        return null;
    }

    public Map<String, NormalizedTable> parse(File pdfFile) throws IOException, CryptographyException {
        return parse(new FileInputStream(pdfFile));
    }

    public Map<String, NormalizedTable> parse(InputStream pdfFile) throws IOException, CryptographyException {
        PDDocument document = PDDocument.load(pdfFile);
        try {
            return parse(document);
        } finally {
            logger.info("Close the PDF file");
            pdfFile.close();
        }
    }

    public Map<String, NormalizedTable> parse(PDDocument document) throws IOException, CryptographyException {
        if (document.isEncrypted()) {
            document.decrypt("");
        }
        ObjectExtractor oe = new ObjectExtractor(document);
        if (this.parsedWithSchema)
        {
            Map<String, NormalizedTable> resultMap = extractDataForSectionAndSubSections(document, oe,
                    this.rootPdfSection);
            document.close();
            oe.close();
            return resultMap;
        }
        else
        {
            Map<String, NormalizedTable> resultMap = new LinkedHashMap<>();
            for (PdfSection section : this.pdfSections) {
                NormalizedTable resultTable = extractDataForOneSection(document, oe, section);
                resultMap.put(section.getNameWithoutSpaces(), resultTable);
            }
            document.close();
            oe.close();
            return resultMap;
        }
    }

    /**
     * Extract data for a section
     * @param document
     * @param oe
     * @param section
     * @return
     */
    private NormalizedTable extractDataForOneSection(PDDocument document, ObjectExtractor oe, PdfSection section) {
        String[] tops = section.getTopIdentifiers();
        String left = section.getLeftIdentifier();
        String[] bottoms = section.getBottomIdentifiers();
        String right = section.getRightIdentifier();
        if (tops != null || left != null || bottoms != null || right != null) {
            // get page areas that the current section is on
            PdfSectionLocator sectionLocator = new PdfSectionLocator(oe, section);
            List<Page> pages = null;
            try {
                pages = sectionLocator.locateSection(document, oe.extract(1));
            } catch (IOException e) {
                logger.error("Exception: ", e);
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

    /**
     * Extract data for a section and all sub-sections
     */
    private Map<String, NormalizedTable> extractDataForSectionAndSubSections(PDDocument document, ObjectExtractor oe,
                                                                             PdfSection section) {
        Map<String, NormalizedTable> mapResult = new LinkedHashMap<>();

        for (PdfSection subSection : section.getChildSections())
        {
            NormalizedTable table = extractDataForOneSection(document, oe, subSection);
            mapResult.put(subSection.getNameWithoutSpaces(), table);
        }

        return mapResult;
    }
}
