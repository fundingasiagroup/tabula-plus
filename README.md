
# tabula-plus
tabula-plus is a library that let tabular data from PDF files be extracted in a simple way. It is written on top of [tabula-java](https://github.com/tabulapdf/tabula-java), which is also a tool to extract tables from PDF file.

# How tabula-plus works
To be able to extract a table from a PDF file with tabula-plus, a top identifier and a bottom identifier need to be defined whereas the left identifier and the right identifier are optional. The top identifier can be thought of as a word or phrase that marks the start of the table in the PDF file. Similarly, the bottom identifier can be thought of as a word or phrase that marks the end of the table. After identifiers has been defined, data extraction can start. The result returned will be a two dimensional array that contains data from the table.

This approach make it easier for developers to indicate which table they are interested in. With [tabula-java](https://github.com/tabulapdf/tabula-java), developers need to define the area of the interested table, which is not going to be apparent. 

# How to use tabula-plus library
The sample code below extracts data for a table named *Table 7* from *sample-   tables.pdf* file.

    PdfSection section = new PdfSection("Table 7");
    String[] topIdentifiers = {"Table 7:"};
    section.setTopIdentifiers(topIdentifiers);
    section.setTopIncluded(false);
    String[] bottomIdentifiers = {"Table 8:"};
	section.setBottomIdentifiers(bottomIdentifiers_1);
    section.setBottomIncluded(false);
    PdfSection[] sections = {section};
    PdfParser pdfParser = new PdfParser(sections);
    NormalizedTable[] tables = pdfParser.parse("sample-tables.pdf")

In the code snippet above, the top identifiers and the bottom identifiers are defined as lists, which means that there might be multiple top identifiers and bottom identifiers. This makes sense when multiple PDF files need to do extraction for a table, but there are differences in the top identifiers and the bottom identifiers for that table in different PDF files. In that case, the table is detected as soon as any top identifier is encountered and the table is ended as soon as any bottom identifier is encountered.

tabula-plus also let developers to indicate if identifiers are parts of the table. For example, to indicate that the top identifier is not a part of the table, developers can do `section.setTopIncluded(false);`.

# How to run the sample code
This library requires a Java Runtime Environment compatible with Java 7 (i.e. Java 7, 8 or higher). 

 - Mac OS X, Linux
	 - Go to the project directory, and build the project by running the command `./gradlew build`
	 - Run the sample code with the command `./gradlew execute`
 - Windows
	 - Go to the project directory, and build the project by running the command `./gradlew.bat build`
	 - Run the sample code with the command `./gradlew.bat execute`

What the sample code does is that it defines identifiers for two tables named *Table 7* and *Table 10*, extracts data for these two tables and then prints out the result.

# License
tabula-plus is released under MIT License.
