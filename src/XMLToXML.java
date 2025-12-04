import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class XMLToXML {

    public static void parse(String inPath, String outPath) throws Exception {

        //factory instantiation
        //factory API that enables applications to obtain a parser that
        //produces DOM object trees from XML documents
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //validation and namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        //parser instantiation
        //API to obtain DOM document instances from XML documents
        DocumentBuilder builder = factory.newDocumentBuilder();

        //install ErrorHandler
        builder.setErrorHandler(new MyErrorHandler());

        //parsing instantiation
        Document doc = builder.parse(inPath);
        doc.getDocumentElement().normalize();

        //actual parsing now
        StringBuilder out = new StringBuilder();
        out.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        out.append("<title>Takeout View</title>");
        out.append("<style>body{font-family:sans-serif;} .elem{margin-left:20px;} ");
        out.append(".tag{font-weight:bold;} .attr{color:#555;} .text{color:#333;}</style>");
        out.append("</head><body>");
        processNode(doc, out);
        out.append("</body></html>");

        //output result to file
        try (PrintWriter pw = new PrintWriter(outPath, StandardCharsets.UTF_8)) {
            pw.println(out);
        }

    }

    private static void processNode(Document doc, StringBuilder out) {
        if (doc == null) {
            return;
        }

        // TOP LEVEL INFO

        String slogan = doc.getElementsByTagName("slogan").item(0).getTextContent();
        String year = doc.getElementsByTagName("year").item(0).getTextContent();

        out.append("<h1>").append("Olympics ").append(year).append(" - \"").append(slogan).append("\"").append("</h1>");

        // MEDAL AVERAGES

        out.append("<h2>Medal Averages per Country</h2>");

        NodeList countries = doc.getElementsByTagName("country");
        for (int i = 0; i < countries.getLength(); i++) {
            Node countryNode = countries.item(i);
            if (countryNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element countryElement = (Element) countryNode;
            String countryCode = countryElement.getAttribute("code");

            NodeList athletes = countryElement.getElementsByTagName("athlete");
            int totalGold = 0, totalSilver = 0, totalBronze = 0;
            int athleteCount = athletes.getLength();

            for (int j = 0; j < athleteCount; j++) {
                Node athleteNode = athletes.item(j);
                if (athleteNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element athleteElement = (Element) athleteNode;
                Element medalsElement = (Element) athleteElement.getElementsByTagName("medals").item(0);

                int gold = Integer.parseInt(medalsElement.getElementsByTagName("gold").item(0).getTextContent());
                int silver = Integer.parseInt(medalsElement.getElementsByTagName("silver").item(0).getTextContent());
                int bronze = Integer.parseInt(medalsElement.getElementsByTagName("bronze").item(0).getTextContent());

                totalGold += gold;
                totalSilver += silver;
                totalBronze += bronze;
            }

            double avgGold = (double) totalGold / athleteCount;
            double avgSilver = (double) totalSilver / athleteCount;
            double avgBronze = (double) totalBronze / athleteCount;

            out.append("<p>Country: ").append(countryCode).append("</p>");
            out.append("<p>Average Gold: ").append(String.format("%.2f", avgGold)).append("</p>");
            out.append("<p>Average Silver: ").append(String.format("%.2f", avgSilver)).append("</p>");
            out.append("<p>Average Bronze: ").append(String.format("%.2f", avgBronze)).append("</p>");
            out.append("<br>");
        }

    }

    private static class MyErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException ex) throws org.xml.sax.SAXException {
            printError("WARNING", ex);
        }

        @Override
        public void error(SAXParseException ex) throws org.xml.sax.SAXException {
            printError("ERROR", ex);
        }

        @Override
        public void fatalError(SAXParseException ex) throws org.xml.sax.SAXException {
            printError("FATAL ERROR", ex);
        }

        private void printError(String err, SAXParseException ex) {
            System.out.printf("%s at %3d, %3d: %s \n",
                    err,
                    ex.getLineNumber(),
                    ex.getColumnNumber(),
                    ex.getMessage());
        }
    }

}
