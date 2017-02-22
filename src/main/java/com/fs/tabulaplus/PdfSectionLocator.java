package com.fundingsocieties.skeletalpdfparser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Rectangle;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class will locate a section in a PDF file by finding all the page areas that contain parts of the section.
 * It finds the start page that contains the start identifier of the section and the coordinates of the start identifier
 * on the page. It then finds the end page that contains the bottom identifier of the section and the coordinates of the bottom identifier.
 *
 * Created by hailegia on 18/9/16.
 */
public class PdfSectionLocator {
    private PdfSection pdfSection;

    private final ObjectExtractor oe;

    /**
     * These are 2D positions of the identifiers of the section
     */
    private TextPosition leftPosition;
    private TextPosition rightPosition;
    private TextPosition topPosition;
    private TextPosition bottomPosition;

    /**
     * This stripper helps to find the page area that contains the top identifier of the section, the page area
     * that contains the bottom identifier of the section and positions of top, bottom, left and right identifiers.
     */
    private TextStripper stripper;

    /**
     * This variable keeps track of the number of page turns needed to go from the start of the PDF section to
     * the end of the PDF section. For example, if the start of a section is on page 1 and the end of the section is on
     * page 2, then the numOfPageTurns = 1
     */
    private int numOfPageTurns;

    /**
     * This variable maintains the top margins for different pages.
     * The search process is that it starts from a start page, scan page after page until the end identifier
     * of the PDF section is found. When multiple pages are scanned, the top margins of these pages are collected.
     */
    private List<Float> topMargins = new ArrayList<>();

    /**
     * The value of this variable is the average of top margins from above
     */
    private float marginTop;

    private class TextStripper extends PDFTextStripper
    {
        /**
         * This variable keeps track of whether the scanner has just started on a new page
         */
        private boolean pageStarted;

        /**
         * This variable keeps the page number of the page that contains the start of the PDF section
         */
        private int actualStartPageNumber;

        public TextStripper(int startPageNumber) throws IOException
        {
            super();
            super.setSortByPosition(true);
            super.setStartPage(startPageNumber);
            this.actualStartPageNumber = startPageNumber;
        }

        /**
         * Every time a new page is scanned, this function is called
         */
        @Override
        protected void writePageStart() throws IOException
        {
            super.writePageStart();
            pageStarted = true;
        }

        /**
         * When the scanner reaches the end of a page, this function gets called
         * @throws IOException
         */
        @Override
        protected void writePageEnd() throws IOException
        {
            super.writePageEnd();
            if (topPosition == null)
            {
                this.actualStartPageNumber++;
            }
            if ((pdfSection.getTopIdentifier() == null || topPosition != null) && (bottomPosition == null))
            {
                numOfPageTurns++;
            }
        }

        /**
         * This function gets called whenever a string needs to be written to the output stream
         * @param text
         * @param textPositions
         * @throws IOException
         */
        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException
        {
            if (pageStarted)
            {
                // a new page has just started, we need to collect the top margin of this page
                pageStarted = false;
                topMargins.add(textPositions.get(0).getY());
            }

            // determine the top boundary
            if (pdfSection.getTopIdentifier() != null && topPosition == null)
            {
                if (text.equals(pdfSection.getTopIdentifier()) || text.startsWith(pdfSection.getTopIdentifier())
                        || text.endsWith(pdfSection.getTopIdentifier()))
                {
                    topPosition = textPositions.get(0);
                }
            }

            // determine the left boundary
            if (pdfSection.getLeftIdentifier() != null && leftPosition == null)
            {
                if (text.equals(pdfSection.getLeftIdentifier()) || text.startsWith(pdfSection.getLeftIdentifier())
                        || text.endsWith(pdfSection.getLeftIdentifier()))
                {
                    leftPosition = pdfSection.isLeftIncluded() ? textPositions.get(0) :
                            textPositions.get(textPositions.size() - 1);
                }
            }

            // determine the bottom boundary
            if ((pdfSection.getTopIdentifier() == null || topPosition != null) && pdfSection.getBottomIdentifiers() != null
                    && bottomPosition == null)
            {
                for (String bottomText : pdfSection.getBottomIdentifiers())
                {
                    if (text.equals(bottomText) || text.startsWith(bottomText) || text.endsWith(bottomText))
                    {
                        bottomPosition = textPositions.get(0);
                        break;
                    }
                }
            }

            // determine the right boundary
            if ((pdfSection.getLeftIdentifier() == null || leftPosition != null) && pdfSection.getRightIdentifier() != null
                    && rightPosition == null) {
                if (text.equals(pdfSection.getRightIdentifier()) || text.startsWith(pdfSection.getRightIdentifier())
                        || text.endsWith(pdfSection.getRightIdentifier())) {
                    rightPosition = pdfSection.isRightIncluded() ? textPositions.get(textPositions.size() - 1) :
                            textPositions.get(0);
                }
            }

            super.writeString(text, textPositions);
        }

    }

    /**
     * Constructor
     * @param oe
     * @param section
     */
    public PdfSectionLocator(ObjectExtractor oe, PdfSection section)
    {
        this.oe = oe;
        this.pdfSection = section;
    }

    /**
     * This function tries to find pages that contain a PDF section
     * @param document
     * @param startPage
     * @return
     * @throws IOException
     */
    public List<Page> locateSection(PDDocument document, Page startPage) throws IOException
    {
        int startPageNumber = startPage.getPageNumber();

        stripper = new TextStripper(startPageNumber);
        stripper.getText(document);

        // This is the page that contains the start of the PDF section
        Page actualStartPage;

        if (stripper.actualStartPageNumber <= oe.getPageCount())
        {
            actualStartPage = oe.extract(stripper.actualStartPageNumber);
        }
        else
        {
            // Can't locate the specified PDF section
            return Collections.emptyList();
        }

        // calculate the top margin from collected top margins. the value of marginTop now is equal to average of
        // top margins
        marginTop = (float) Math.floor(topMargins.stream().reduce(0.0f, (curSum, elem) -> curSum + elem) / topMargins.size());
        // we need to compare the value of marginTop and customTopMargin of the PdfSection object. we'll choose the
        // bigger value
        marginTop = Math.max(marginTop, pdfSection.getCustomTopMargin());

        List<Page> pageAreas = new ArrayList<>();

        // Here we'll have start position, end position and number of pages between them
        if (numOfPageTurns == 0)
        {
            // Same page
            Page foundArea = actualStartPage.getArea(getDetectedRectangle(actualStartPage));
            pageAreas.add(foundArea);
        } else
        {
            // use this variable to determine the bottom position of pages.
            float pageBottom = actualStartPage.getBottom() - pdfSection.getCustomBottomMargin();

            // Extract first page
            Point2D topLeft = getTopLeft(actualStartPage);
            Page firstPageArea = actualStartPage.getArea((float) topLeft.getY(), (float) topLeft.getX(),
                    pageBottom, actualStartPage.getRight());
            pageAreas.add(firstPageArea);

            // Extract middle pages
            for (int currentPageDelta = 1; currentPageDelta < numOfPageTurns; currentPageDelta++)
            {
                Page currentPage = oe.extract(startPageNumber + currentPageDelta);
                pageBottom = currentPage.getBottom() - pdfSection.getCustomBottomMargin();
                currentPage = currentPage.getArea(marginTop, (float) topLeft.getX(), pageBottom,
                        actualStartPage.getRight());
                pageAreas.add(currentPage);
            }

            // Extract last page
            int lastPageNumber = startPageNumber + numOfPageTurns;
            if (lastPageNumber <= oe.getPageCount())
            {
                Page lastPage = oe.extract(startPageNumber + numOfPageTurns);
                Point2D bottomRight = getBottomRight(lastPage);
                float bottom = (float) bottomRight.getY();
                if (!pdfSection.isBottomIncluded())
                {
                    bottom -= 10;
                }
                Page lastPageArea = lastPage.getArea(marginTop, (float) topLeft.getX(), bottom,
                        (float) bottomRight.getX());
                pageAreas.add(lastPageArea);
            }

        }

        return pageAreas;
    }

    /**
     * This function gets the top left information from topPosition and leftPosition or the page itself
     * @param page
     * @return
     */
    private Point2D.Float getTopLeft(Page page)
    {
        float top = -1, left = -1;
        if (topPosition != null)
        {
            top = pdfSection.isTopIncluded() ? topPosition.getY() - topPosition.getHeight() : topPosition.getY();
        }
        if (leftPosition != null)
        {
            left = pdfSection.isLeftIncluded() ? leftPosition.getX() : leftPosition.getX() + leftPosition.getWidth();
        }
        if (top < 0)
        {
            top = (float) page.getMinY();
        }
        if (left < 0)
        {
            left = (float) page.getMinX();
        }

        return new Point2D.Float((float) Math.floor(left), (float) Math.floor(top));
    }

    /**
     * This function gets the bottom right information from bottomPosition and rightPosition or the page itself
     * @param page
     * @return
     */
    private Point2D getBottomRight(Page page)
    {
        float bottom = -1, right = -1;
        if (bottomPosition != null)
        {
            bottom = pdfSection.isBottomIncluded() ? bottomPosition.getY() + bottomPosition.getHeight() :
                    bottomPosition.getY() - 1; // minus 1 from the bottom position to completely detach the bottom
                                               // identifier
        }
        if (rightPosition != null)
        {
            right = pdfSection.isRightIncluded() ? rightPosition.getX() + rightPosition.getWidth() : rightPosition.getX();
        }
        if (bottom < 0)
        {
            bottom = (float) page.getMaxY();
        }
        if (right < 0)
        {
            right = (float) page.getMaxX();
        }

        return new Point2D.Float((float) Math.ceil(right), (float) Math.ceil(bottom));
    }

    /**
     * This function gets the rectangle that contains the PDF section. This applies only when the top left and
     * bottom right are on the same page
     * @param page
     * @return
     */
    private Rectangle getDetectedRectangle(Page page)
    {
        Point2D topLeft = getTopLeft(page);
        Point2D bottomRight = getBottomRight(page);
        Rectangle result = new Rectangle((float) topLeft.getY(), (float) topLeft.getX(),
                (float) (bottomRight.getX() - topLeft.getX()), (float) (bottomRight.getY() - topLeft.getY()));
        return result;
    }

}
