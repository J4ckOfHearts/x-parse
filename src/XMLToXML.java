import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XMLToXML {

    public static void parse(String inPath, String outPath) throws Exception {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        factory.setNamespaceAware(true);


        DocumentBuilder builder = factory.newDocumentBuilder();

        builder.setErrorHandler(new MyErrorHandler());

        Document doc = builder.parse(inPath);
        doc.getDocumentElement().normalize();

        processNode(doc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        DOMSource src = new DOMSource(doc);
        StreamResult res = new StreamResult(new File(outPath));

        transformer.transform(src, res);

    }

    private static void processNode(Document doc) {
        if (doc == null) {
            return;
        }

        NodeList countries = doc.getElementsByTagName("country");
        for (int i = 0; i < countries.getLength(); i++) {
            Node countryNode = countries.item(i);
            if (countryNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element countryElement = (Element) countryNode;

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

            // FIX: Round Doubles
            avgGold = Math.round(avgGold * 100.0) / 100.0;
            avgSilver = Math.round(avgSilver * 100.0) / 100.0;
            avgBronze = Math.round(avgBronze * 100.0) / 100.0;

            Node childGold = doc.createElement("averageGold");
            childGold.appendChild(doc.createTextNode(avgGold+""));
            countries.item(i).appendChild(childGold);

            Node childSilver = doc.createElement("averageSilver");
            childSilver.appendChild(doc.createTextNode(avgSilver+""));
            countries.item(i).appendChild(childSilver);

            Node childBronze = doc.createElement("averageBronze");
            childBronze.appendChild(doc.createTextNode(avgBronze+""));
            countries.item(i).appendChild(childBronze);

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
