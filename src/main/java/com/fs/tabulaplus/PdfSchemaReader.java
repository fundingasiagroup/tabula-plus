package com.fs.tabulaplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class reads a schema file and
 *
 * Created by hailegia on 27/10/16.
 */
public class PdfSchemaReader {
    private final ObjectMapper objectMapper;

    public PdfSchemaReader() {
        objectMapper = new ObjectMapper(new YAMLFactory());
    }

    public PdfSection read(String filename) {
        File schemaFile = new File(filename);
        return read(schemaFile);
    }

    public PdfSection read(File schemaFile) {
        if (schemaFile.exists()) {
            JsonNode jsonRoot = null;
            try {
                jsonRoot = objectMapper.readTree(schemaFile);
                PdfSection rootSection = parse("root", jsonRoot);
                return rootSection;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * This function converts a JsonNode node with name {sectionName} to a PdfSection node. If the JsonNode node
     * doesn't just contain regular fields, including top, bottom, left, right, then it means that the JsonNode
     * node contains a container JsonNode. In that case, we need to recursively call this function again to convert
     * the child JsonNode node to a PdfSection first, then use the result.
     *
     * @param sectionName
     * @param jsonNode
     * @return
     */
    private PdfSection parse(String sectionName, JsonNode jsonNode) {
        // This block of code inspects the JsonNode node to see if it can create a list of children for the
        // final PdfSection node
        List<PdfSection> children = new ArrayList<>();
        Iterator<String> fieldNameIter = jsonNode.fieldNames();
        while (fieldNameIter.hasNext()) {
            String fieldName = fieldNameIter.next();
            JsonNode childJsonNode = jsonNode.get(fieldName);
            if (childJsonNode.isContainerNode()) {
                PdfSection child = parse(fieldName, childJsonNode);
                children.add(child);
            }
        }

        JsonNode type = jsonNode.get("type");
        Integer tableType = null;
        if (type != null && type.isNumber()) {
            tableType = type.asInt();
        }

        // there might be multiple top and bottom identifiers, so we need a special treatment for top and bottom text
        String topText = getPdfNodeIdentifier("top", jsonNode);
        String bottomText = getPdfNodeIdentifier("bottom", jsonNode);

        // process customized top margin and bottom margin
        String customTopMarginStr = getPdfNodeIdentifier("top_margin", jsonNode);
        String customBottomMarginStr = getPdfNodeIdentifier("bottom_margin", jsonNode);
        float customTopMargin = 0, customBottomMargin = 0;
        if (customTopMarginStr != null)
        {
            try {
                customTopMargin = Float.parseFloat(customTopMarginStr);
            }
            catch (NumberFormatException e) {}
        }

        if (customBottomMarginStr != null)
        {
            try {
                customBottomMargin = Float.parseFloat(customBottomMarginStr);
            }
            catch (NumberFormatException e) {}
        }

        return new PdfSection(sectionName,
                topText == null ? null : topText.split("\\|"), getPdfNodeIdentifier("left", jsonNode),
                bottomText == null ? null : bottomText.split("\\|"), getPdfNodeIdentifier("right", jsonNode),
                isIdentifierIncluded("top", jsonNode), isIdentifierIncluded("left", jsonNode),
                isIdentifierIncluded("bottom", jsonNode), isIdentifierIncluded("right", jsonNode),
                customTopMargin, customBottomMargin, tableType, children);
    }

    /**
     * This function retrieves either top identifier, bottom identifier, left identifier or right identifier depending
     * on the {identifierType}
     * @param identifierType
     * @param node
     * @return
     */
    private String getPdfNodeIdentifier(String identifierType, JsonNode node) {
        JsonNode propNode = node.get(identifierType);
        if (propNode != null && propNode != NullNode.instance) {
            String text = propNode.asText().trim();
            String[] items = text.split("\\|");
            if (items.length >= 2) {
                String lastItem = items[items.length - 1].trim();
                if (lastItem.equalsIgnoreCase("true") || lastItem.equalsIgnoreCase("false")) {
                    int lastIndex = text.lastIndexOf("|");
                    return text.substring(0, lastIndex).trim();
                }
            }
            return text;
        }
        return null;
    }

    /**
     * This function checks whether the identifier should be included as prt of the PDF section
     * @param identifierType
     * @param node
     * @return
     */
    private boolean isIdentifierIncluded(String identifierType, JsonNode node) {
        JsonNode propNode = node.get(identifierType);
        if (propNode != null && propNode != NullNode.instance) {
            String[] results = propNode.asText().trim().split("\\|");
            Boolean included = null;
            if (results.length >= 2) {
                String lastParams = results[results.length - 1].trim();
                if (lastParams.equalsIgnoreCase("true")) {
                    included = true;
                } else if (lastParams.equalsIgnoreCase("false")) {
                    included = false;
                }
            }
            if (included == null) {
                included = "left".equalsIgnoreCase(identifierType) || "top".equalsIgnoreCase(identifierType);
            }
            return included;
        }
        return true;
    }
}
