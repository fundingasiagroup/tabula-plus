package com.fs.tabulaplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is a simplified version of tabula Table, where it keeps only a table's content
 * and some information about the table. The information is such as whether the table is
 * horizontal or vertical, and the algorithm that is used to extract data from page areas.
 *
 * Created by hailegia on 18/9/16.
 */
public class NormalizedTable {
    public static final int TEXT_ALGORITHM = 0;
    public static final int SPREADSHEET_ALGORITHM = 1;
    public static final int AUTO_ALGORITHM = 2;

    private String tableName;

    protected List<NormalizedRow> rows;
    private int algorithm;

    public NormalizedTable()
    {
        rows = new ArrayList<>();
    }

    public NormalizedTable(List<Page> pageAreas, int algorithm) {
        this.algorithm = algorithm;
        rows = new ArrayList<>();
        process(pageAreas);
    }


    public List<NormalizedRow> getRows() {
        return rows;
    }

    /**
     * Collect rows for the normalized table from different page areas
     */
    private void process(List<Page> pageAreas) {
        for (Page page : pageAreas) {
            List<? extends Table> pageTables = null;
            SpreadsheetExtractionAlgorithm spreadsheetExtractionAlgorithm;
            BasicExtractionAlgorithm basicExtractionAlgorithm;
            switch (algorithm) {
                case TEXT_ALGORITHM:
                    basicExtractionAlgorithm = new BasicExtractionAlgorithm();
                    pageTables = basicExtractionAlgorithm.extract(page);
                    break;
                case SPREADSHEET_ALGORITHM:
                    spreadsheetExtractionAlgorithm = new SpreadsheetExtractionAlgorithm();
                    pageTables = spreadsheetExtractionAlgorithm.extract(page);
                    break;
                case AUTO_ALGORITHM:
                    spreadsheetExtractionAlgorithm = new SpreadsheetExtractionAlgorithm();
                    if (spreadsheetExtractionAlgorithm.isTabular(page)) {
                        pageTables = spreadsheetExtractionAlgorithm.extract(page);
                    } else {
                        basicExtractionAlgorithm = new BasicExtractionAlgorithm();
                        pageTables = basicExtractionAlgorithm.extract(page);
                    }
                    break;
            }
            for (Table pageTable : pageTables) {
                List<List<RectangularTextContainer>> rows = pageTable.getRows();
                for (List<RectangularTextContainer> row : rows) {
                    List<String> cells = row.stream().map(cell -> cell.getText()).collect(Collectors.toList());
                    NormalizedRow normalizedRow = new NormalizedRow(cells);
                    this.rows.add(normalizedRow);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        String resultString = "";
        for (NormalizedRow row : this.rows)
        {
            resultString += ('\n' + row.toString());
        }
        return resultString;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getTableName()
    {
        return this.tableName;
    }
}
