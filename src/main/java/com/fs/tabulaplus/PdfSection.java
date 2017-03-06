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

    private String sectionName;
    private String topIdentifier;
    private String[] bottomIdentifiers;
    private String leftIdentifier;
    private String rightIdentifier;
    private boolean topIncluded;
    private boolean leftIncluded;
    private boolean bottomIncluded;
    private boolean rightIncluded;

    /**
     * A customized top margin of pages that contain the section.
     * This only applies to a section that is on multiple pages and PDF files that have headers on top of each page
     * When we don't want the page headers to interfere the parsing result, we set the customTopMargin to be
     * bigger than the real top margin.
     */
    private float customTopMargin;

    /**
     * The bottom margin for pages that contain the section.
     * This only applies to a section that is on multiple pages and PDF files that have footers on top of each page
     * When we don't want the page footer to interfere the parsing result, we set the customBottomMargin to be
     * bigger than the real bottom margin.
     */
    private float customBottomMargin;

    /**
     * tableType is an integer. 0 represents horizontal table, and 1 represents vertical table
     */
    private Integer tableType;

    /**
     * A section can have multiple child sections. For example, the root section (PDF File section) can have multiple
     * child sections.
     */
    private List<PdfSection> childSections;

    public PdfSection(String sectionName)
    {
        this.sectionName = sectionName;
    }

    public PdfSection(String sectionName,
                      String top, String left, String[] bottoms, String right,
                      boolean topIncluded, boolean leftIncluded, boolean bottomIncluded, boolean rightIncluded,
                      float topMargin, float bottomMargin, Integer tableType, List<PdfSection> children) {
        this.sectionName = sectionName;
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

    public String getNameWithoutSpaces() {
        return sectionName.replace(' ', '_');
    }

    public String getTopIdentifier() {
        return topIdentifier;
    }

    public void setTopIdentifier(String topId) {
        this.topIdentifier = topId;
    }

    public String[] getBottomIdentifiers() {
        return bottomIdentifiers;
    }

    public void setBottomIdentifiers(String[] bottomIds)
    {
        this.bottomIdentifiers = bottomIds;
    }

    public String getLeftIdentifier() {
        return leftIdentifier;
    }

    public void setLeftIdentifier(String leftId)
    {
        this.leftIdentifier = leftId;
    }

    public String getRightIdentifier() {
        return rightIdentifier;
    }

    public void setRightIdentifier(String rightId)
    {
        this.rightIdentifier = rightId;
    }

    public List<PdfSection> getChildSections() {
        return childSections;
    }

    public boolean isTopIncluded() {
        return topIncluded;
    }

    public void setTopIncluded(boolean topIncluded)
    {
        this.topIncluded = topIncluded;
    }

    public boolean isLeftIncluded() {
        return leftIncluded;
    }

    public void setLeftIncluded(boolean leftIncluded)
    {
        this.leftIncluded = leftIncluded;
    }

    public boolean isBottomIncluded() {
        return bottomIncluded;
    }

    public void setBottomIncluded(boolean bottomIncluded)
    {
        this.bottomIncluded = bottomIncluded;
    }

    public boolean isRightIncluded() {
        return rightIncluded;
    }

    public void setRightIncluded(boolean rightIncluded)
    {
        this.rightIncluded = rightIncluded;
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
