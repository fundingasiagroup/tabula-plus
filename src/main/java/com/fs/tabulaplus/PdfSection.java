package com.fs.tabulaplus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class defines objects that represent sections in a PDF file. A section can be as big as the whole
 * PDF file itself, but it can  also be as small as just a block of text. A section can contain other sections.
 * A section is identified by a top identifier, several bottom identifiers, a left identifier (optional)
 * and a right identifier (optional). These identifiers are all in string. We can specify whether these identifiers
 * are parts of the section by setting flag variables such as 'topIncluded' and 'leftIncluded'.
 *
 * Created by hailegia on 27/10/16.
 */
public class PdfSection {
    public static final int HORIZONTAL_TABLE = 0;
    public static final int VERTICAL_TABLE = 1;

    private final String sectionName;
    private final String topIdentifier;
    private final String[] bottomIdentifiers;
    private final String leftIdentifier;
    private final String rightIdentifier;
    private final boolean topIncluded;
    private final boolean leftIncluded;
    private final boolean bottomIncluded;
    private final boolean rightIncluded;

    /**
     * A customized top margin of pages that contain the section.
     * This only applies to a section that is on multiple pages and PDF files that have headers on top of each page
     * When we don't want the page headers to interfere the parsing result, we set the customTopMargin to be
     * bigger than the real top margin.
     */
    private final float customTopMargin;

    /**
     * The bottom margin for pages that contain the section.
     * This only applies to a section that is on multiple pages and PDF files that have footers on top of each page
     * When we don't want the page footer to interfere the parsing result, we set the customBottomMargin to be
     * bigger than the real bottom margin.
     */
    private final float customBottomMargin;

    /**
     * tableType is an integer. 0 represents horizontal table, and 1 represents vertical table
     */
    private final Integer tableType;

    /**
     * A section can have multiple child sections. For example, the root section (PDF File section) can have multiple
     * child sections.
     */
    private final List<PdfSection> childSections;

    public PdfSection(String name,
                      String top, String left, String[] bottoms, String right,
                      boolean topIncluded, boolean leftIncluded, boolean bottomIncluded, boolean rightIncluded,
                      float topMargin, float bottomMargin, Integer tableType, List<PdfSection> children) {
        this.sectionName = name;
        this.topIdentifier = top;
        this.bottomIdentifiers = bottoms;
        this.leftIdentifier = left;
        this.rightIdentifier = right;
        this.topIncluded = topIncluded;
        this.leftIncluded = leftIncluded;
        this.bottomIncluded = bottomIncluded;
        this.rightIncluded = rightIncluded;
        this.customTopMargin = topMargin;
        this.customBottomMargin = bottomMargin;
        this.tableType = tableType;
        this.childSections = children;
        if (bottoms != null) {
            for (int i = 0; i < bottoms.length; i++) {
                bottoms[i] = bottoms[i].trim();
            }
        }
    }

    public String getName() {
        return sectionName;
    }

    public String getTopIdentifier() {
        return topIdentifier;
    }

    public String[] getBottomIdentifiers() {
        return bottomIdentifiers;
    }

    public String getLeftIdentifier() {
        return leftIdentifier;
    }

    public String getRightIdentifier() {
        return rightIdentifier;
    }

    public List<PdfSection> getChildSections() {
        return childSections;
    }

    public boolean isTopIncluded() {
        return topIncluded;
    }

    public boolean isLeftIncluded() {
        return leftIncluded;
    }

    public boolean isBottomIncluded() {
        return bottomIncluded;
    }

    public boolean isRightIncluded() {
        return rightIncluded;
    }

    public float getCustomTopMargin() {
        return customTopMargin;
    }

    public float getCustomBottomMargin() {
        return customBottomMargin;
    }

    public Integer getTableType() {
        return tableType;
    }

    @Override
    public String toString() {
        StringBuilder childrenBuilder = new StringBuilder();
        childrenBuilder.append("[");
        if (childSections != null && !childSections.isEmpty()) {
            for (PdfSection child : childSections) {
                childrenBuilder.append(child.toString()).append(",");
            }
            childrenBuilder.deleteCharAt(childrenBuilder.length() - 1);
        }
        childrenBuilder.append("]");

        String bottomIdentifiersStr = bottomIdentifiers == null ? "" :
                String.join("|", Arrays.stream(bottomIdentifiers).filter(item -> item != null).collect(Collectors.toList()));
        return "{" +
                "\"name\":\"" + sectionName + '"' +
                ",\"top\":\"" + topIdentifier + '"' +
                ",\"left\":\"" + leftIdentifier + '"' +
                ",\"bottom\":\"" + bottomIdentifiersStr + '"' +
                ",\"right\":\"" + rightIdentifier + '"' +
                "\"type\":\"" + tableType + '"' +
                ",\"childSections\":" + childrenBuilder.toString() +
                '}';
    }
}
