
# tabula-plus
tabula-plus is a library that let tabular data from PDF files be extracted in a simple way. It is written on top of [tabula-java](https://github.com/tabulapdf/tabula-java).

# How tabula-plus works
Similar to *tabula-java*, *tabula-plus* also focuses on getting data of tables in PDF files. To be able to extract data of a table in a PDF file with *tabula-plus*, it needs to be provided with necessary information so that it can determine where the table starts and where the table ends. The top identifiers and the bottom identifiers need to be defined whereas the left identifiers and the right identifiers are optional. A top identifier can be thought of as a word or phrase that marks the start of the table in the PDF file. Similarly, the bottom identifier can be thought of as a word or phrase that marks the end of the table. After identifiers has been defined, *tabula-plus* can start the extraction. The result returned will be a two dimensional array that contains data from the table.

This approach make it easier for developers to indicate which table they are interested in and where it is. With [tabula-java](https://github.com/tabulapdf/tabula-java), developers need to define the area of the interested table, which is not going to be apparent. 

# How to use tabula-plus library
To demonstrate how to use *tabula-plus*, we will go through an example of extracting data of a table named *Table 7* from *sample-tables.pdf*. 

<img src="http://i.imgur.com/JU3qZVK.png" width="600">

The sample code below extracts data for the aforementioned table.

    PdfSection section = new PdfSection("Table 7");
    String[] topIdentifiers = {"Table 7:"};
    section.setTopIdentifiers(topIdentifiers);
    section.setTopIncluded(false);
    String[] bottomIdentifiers = {"Table 8:"};
	section.setBottomIdentifiers(bottomIdentifiers_1);
    section.setBottomIncluded(false);
    PdfSection[] sections = {section};
    PdfParser pdfParser = new PdfParser(sections);
    Map<String, NormalizedTable> tableMap = pdfParser.parse("sample-tables.pdf");

In the code snippet above, the top identifiers and the bottom identifiers are defined as lists, which means that there might be multiple top identifiers and bottom identifiers. This makes sense when data from multiple PDF files need to be extracted for the same type of  table, but there are differences in the top identifiers and the bottom identifiers for that table in different PDF files. In that case, the table is detected as soon as any top identifier is encountered and the table is ended as soon as any bottom identifier is encountered.

tabula-plus also lets developers to indicate if identifiers are parts of the table. For example, to indicate that the top identifier is not a part of the table, developers can do `section.setTopIncluded(false);`. 

The extracted data for *table 7* is as follow:

<img src="http://i.imgur.com/NJY5ghl.png" width="400">


Besides defining identifiers programmatically, *tabula-plus* also lets developers to defined them with a schema file. Below is a sample schema file:

    Table_7:
      top: Table 7 | false
      bottom: Table 8 | false
    
    Table_10:
      top: layout problems) | self-contained year-end | false
      bottom: Table 11 | false

For *Table_10*, it has two top identifiers *layout problems)* and *self-contained year-end*. *false* indicates that these top identifiers are not parts of the table's data.

To let *tabula-plus* knows that it should collect identifiers from a schema file, do as following: 

    PdfParser pdfParser = new PdfParser("example_2.schema");
        Map<String, NormalizedTable> tableMap = pdfParser.parse("sample-tables.pdf");

The result returned is the mapping between tables' names and tables' data. 

# How to run the sample code
This library requires a Java Runtime Environment compatible with Java 7 (i.e. Java 7, 8 or higher). 

 - Mac OS X, Linux
	 - Go to the project directory, and build the project by running the command `./gradlew build`
	 - Run the sample code with the command `./gradlew run`
 - Windows
	 - Go to the project directory, and build the project by running the command `./gradlew.bat build`
	 - Run the sample code with the command `./gradlew.bat run`

What the sample code does is that it defines identifiers for two tables named *Table 7* and *Table 10*, extracts data for these two tables and then prints out the result.

# License
tabula-plus is released under MIT License.
