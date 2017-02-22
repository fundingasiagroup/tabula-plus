package com.fundingsocieties.skeletalpdfparser;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.function.Function;
import java.util.stream.Collectors;


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
     * An ObjectMapper is needed to parse the schema file, which is in YAML format
     */
    private static final ObjectMapper om = new ObjectMapper();

    /**
     * The root section that contains other section
     */
    private final PdfSection rootPdfSection;

    /**
     * The number of the currently processing page
     */
    private int currentPageNumber;

    /**
     * List of functions that are used to correct data of sections
     */
    protected Map<String, Function<List<NormalizedRow>, List<List<String>>>> transformFuncs;

    /**
     * Constructor
     * @param schemaFile
     * @throws IOException
     */
    public PdfParser(String schemaFile) throws IOException {
        this(schemaFile, new HashMap<>());
    }

    /**
     * Constructor
     * @param schemaFile
     * @param transformFuncs
     * @throws IOException
     */
    public PdfParser(String schemaFile, Map<String, Function<List<NormalizedRow>,
            List<List<String>>>> transformFuncs) throws IOException {
        schemaReader = new PdfSchemaReader();
        rootPdfSection = schemaReader.read(schemaFile);
        this.transformFuncs = transformFuncs;
    }

    /**
     * Main parsing function to parse pdf files
     * @param pdfFile
     * @return
     * @throws IOException
     * @throws CryptographyException
     */
    public String parse(String pdfFile) throws IOException, CryptographyException {
        if (pdfFile.contains(".pdf"))
            return parse(new File(pdfFile));
        return "{}";
    }

    public String parse(File pdfFile) throws IOException, CryptographyException {
        return parse(new FileInputStream(pdfFile));
    }

    public String parse(InputStream pdfFile) throws IOException, CryptographyException {
        PDDocument document = PDDocument.load(pdfFile);
        try {
            return parse(document);
        } finally {
            logger.info("Close the PDF file");
            pdfFile.close();
        }
    }

    public String parse(PDDocument document) throws IOException, CryptographyException {
        currentPageNumber = 1;

        if (document.isEncrypted()) {
            document.decrypt("");
        }
        ObjectExtractor oe = new ObjectExtractor(document);
        try {
            Object result = extractData(document, oe, rootPdfSection);
            return om.writeValueAsString(result);
        } catch (Exception e) {
            logger.info("Exception: ", e);
            return "";
        } finally {
            document.close();
            oe.close();
        }
    }

    /**
     * This is a recursive function that extracts all information from the current section and all its child sections
     * @param document
     * @param oe
     * @param section
     * @return
     */
    private Object extractData(PDDocument document, ObjectExtractor oe, PdfSection section) {
        Map<String, Object> mapResult = new LinkedHashMap<>();
        List<Map<String, String>> listResult = new ArrayList<>();
        String top = section.getTopIdentifier();
        String left = section.getLeftIdentifier();
        String[] bottoms = section.getBottomIdentifiers();
        String right = section.getRightIdentifier();
        if (top != null || left != null || bottoms != null || right != null) {
            // get page areas that the current section is on
            PdfSectionLocator sectionLocator = new PdfSectionLocator(oe, section);
            List<Page> pages = null;
            try {
                pages = sectionLocator.locateSection(document, oe.extract(currentPageNumber));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) { // catch all other exceptions
                logger.error("Exception: ", e);
            }

            if (pages != null && !pages.isEmpty()) {
                Function<List<NormalizedRow>, List<List<String>>> transformFunc = transformFuncs != null ?
                        transformFuncs.get(section.getName()) : null;
                // create a normalized table with the data extracted from pages
                NormalizedTable normalizedTable = new NormalizedTable(pages, NormalizedTable.TEXT_ALGORITHM,
                        transformFunc);
                // do post processing to correct data
                List<List<String>> finalRows = normalizedTable.postProcess();

                switch (section.getTableType()) {
                    case PdfSection.HORIZONTAL_TABLE:
                        handleHorizontalTable(finalRows, section, mapResult);
                        break;
                    case PdfSection.VERTICAL_TABLE:
                        handleVerticalTable(finalRows, section, listResult);
                        break;
                }
            }

            if (pages != null && pages.size() > 0) {
                currentPageNumber = pages.get(pages.size() - 1).getPageNumber();
            } else {
                logger.info(section.getName() + " does not exist!");
            }
        }

        section.getChildSections().forEach(child -> mapResult.put(child.getName(), extractData(document, oe, child)));
        return mapResult.isEmpty() ? listResult : mapResult;
    }

    /**
     * Converts the horizontal table-like data (with headers row and data rows) to JSON data
     * @param finalRows
     * @param section
     * @param mapResult
     */
    private void handleHorizontalTable(List<List<String>> finalRows, PdfSection section, Map<String, Object> mapResult)
    {
        for (List<String> row : finalRows) {
            String wholeRow = String.join(" ", row);
            if (section.getTopIdentifier() != null && wholeRow.contains(section.getTopIdentifier()) &&
                    !section.isTopIncluded()) {
                continue;
            }
            if (section.getBottomIdentifiers() != null && !section.isBottomIncluded()) {
                boolean ignore = false;
                for (String bottom : section.getBottomIdentifiers()) {
                    if (wholeRow.contains(bottom)) {
                        ignore = true;
                        break;
                    }
                }
                if (ignore) {
                    continue;
                }
            }
            String key = null;
            StringBuilder valueBuilder = new StringBuilder();
            for (String text : row) {
                text = text.trim();
                if (text != null && !text.isEmpty()) {
                    if (key == null) {
                        key = text;
                    } else {
                        valueBuilder.append(text.trim()).append(" ");
                    }
                }
            }
            if (key != null) {
                key = key.replaceAll(" ", "_");
                mapResult.put(key, valueBuilder.toString().trim());
            }
        }
    }

    /**
     * Converts the horizontal table-like data (with headers column and data columns) to JSON data
     * @param finalRows
     * @param section
     * @param listResult
     */
    private void handleVerticalTable(List<List<String>> finalRows, PdfSection section, List<Map<String, String>> listResult)
    {
        if (!finalRows.isEmpty()) {
            List<String> headers = null;
            int currentRow = 0;
            while (headers == null) {
                List<String> headerCandidate = finalRows.get(currentRow++);
                boolean inValid = true;
                for (String header : headerCandidate) {
                    if (!header.isEmpty()) {
                        inValid = false;
                        break;
                    }
                }
                if (!inValid) {
                    headers = headerCandidate.stream().map(header ->
                            header.trim().replaceAll(" ", "_")).collect(Collectors.toList());
                }
            }
            String headersTxt = String.join(",", headers);
            logger.info("Header for {} is {}", section.getName(), headersTxt);
            for (int i = currentRow; i < finalRows.size(); i++) {
                List<String> itemData = finalRows.get(i);
                String wholeRow = String.join(" ", itemData);
                if (section.getTopIdentifier() != null && wholeRow.contains(section.getTopIdentifier()) && !section.isTopIncluded()) {
                    continue;
                }
                if (section.getBottomIdentifiers() != null && !section.isBottomIncluded()) {
                    boolean ignore = false;
                    for (String bottom : section.getBottomIdentifiers()) {
                        if (wholeRow.contains(bottom)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore) {
                        continue;
                    }
                }
                if (itemData.size() != headers.size()) {
                    logger.warn("Mismatch detected: {}", String.join(",", itemData));
                    continue;
                } else {
                    Map<String, String> item = new LinkedHashMap<>();
                    for (int column = 0; column < headers.size(); column++) {
                        item.put(headers.get(column), itemData.get(column));
                    }
                    listResult.add(item);
                }
            }
        }
    }
}
