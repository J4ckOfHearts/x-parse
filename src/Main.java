// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        try {
            String inPath = "takeout.xml";
            String outPath = "takeout.html";
            XMLToHTML.parse(inPath, outPath);
        } catch (Exception e) {
            System.out.println("An error occurred while parsing to HTML: " + e.getMessage());
        }

        try {
            String inPath = "ex5.xml";
            String outPath = "ex5-out.html";
            XMLToXML.parse(inPath, outPath);
        } catch (Exception e) {
            System.out.println("An error occurred while parsing to XML: " + e.getMessage());
        }

    }
}