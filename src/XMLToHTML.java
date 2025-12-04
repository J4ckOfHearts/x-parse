import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class XMLToHTML {

    public static void parse(String inPath, String outPath) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        builder.setErrorHandler(new MyErrorHandler());

        Document doc = builder.parse(inPath);
        doc.getDocumentElement().normalize();

        StringBuilder out = new StringBuilder();
        out.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        out.append("<title>Takeout View</title>");
        out.append("<style>body{font-family:sans-serif;} .elem{margin-left:20px;} ");
        out.append(".tag{font-weight:bold;} .attr{color:#555;} .text{color:#333;}</style>");
        out.append("</head><body>");
        processNode(doc, out);
        out.append("</body></html>");

        try (PrintWriter pw = new PrintWriter(outPath, StandardCharsets.UTF_8)) {
            pw.println(out);
        }

    }

    private static void processNode(Document doc, StringBuilder out) {
        if (doc == null) {
            return;
        }

        // DISHES

        out.append("<h2>Offered Dishes</h2>");

        NodeList offered = doc.getElementsByTagName("offered-dishes").item(0).getChildNodes();
        for (int i = 0; i < offered.getLength(); i++) {
            if (offered.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (offered.item(i).getAttributes().getNamedItem("vegetarian").getTextContent().equals("true")) {
                out.append("<h4>").append(offered.item(i).getAttributes().getNamedItem("name").getTextContent()).append(" (v) (id:").append(offered.item(i).getAttributes().getNamedItem("id").getTextContent()).append(") </h3>");
            } else {
                out.append("<h4>").append(offered.item(i).getAttributes().getNamedItem("name").getTextContent()).append(" (id:").append(offered.item(i).getAttributes().getNamedItem("id").getTextContent()).append(") </h3>");
            }

            out.append("<div class='block'>");
            out.append("<div class='label'>").append(offered.item(i).getTextContent()).append("</div>");
            out.append("<div class='block'>");
            out.append("<br>");
        }

        // PERSONNEL

        out.append("<h2>Personnel</h2>");

        NodeList personnel = doc.getElementsByTagName("person");
        for (int i = 0; i < personnel.getLength(); i++) {
            if (personnel.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            NodeList person = personnel.item(i).getChildNodes();
            String name = null;
            String transport = null;
            String c_id = ((Element) personnel.item(i)).getAttribute("id");

            for (int j = 0; j < person.getLength(); j++) {
                if (person.item(j).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = person.item(j).getNodeName();
                if ("name".equals(nodeName)) {
                    name = person.item(j).getTextContent() + " (" + c_id + ")";
                } else if ("transport".equals(nodeName)) {
                    transport = person.item(j).getTextContent();
                }
            }

            if (name != null) {
                out.append("<div class='block'>");
                out.append("<div class='label'>").append(name);
                if (transport != null) {
                    out.append(" (").append(transport).append(")");
                }
                out.append("</div>");
                out.append("<div class='block'>");
                out.append("<br>");
            }
        }

        // ORDERS

        out.append("<h2>Current Orders</h2>");

        NodeList orders = doc.getElementsByTagName("order");
        for (int i = 0; i < orders.getLength(); i++) {
            Node order = orders.item(i);
            if (order.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element orderElement = (Element) order;
            String orderId = orderElement.getAttribute("order-id");
            out.append("<div class='order'><h4>Order ID: ").append(orderId).append("</h4>");

            NodeList orderChildren = order.getChildNodes();
            for (int j = 0; j < orderChildren.getLength(); j++) {
                Node child = orderChildren.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                if ("address".equals(nodeName)) {
                    Element addressElement = (Element) child;
                    String deliveredBy = addressElement.getAttribute("deliveredBy");
                    String address = child.getTextContent().trim();
                    out.append("<p>Delivery Address: ").append(address)
                            .append(" (Delivered by: ").append(deliveredBy).append(")</p>");
                } else if ("self-pickup".equals(nodeName)) {
                    Element selfPickupElement = (Element) child;
                    String clientName = selfPickupElement.getAttribute("client-name");
                    out.append("<p>Self-Pickup by: ").append(clientName).append("</p>");
                } else if ("item".equals(nodeName)) {
                    Element itemElement = (Element) child;
                    String dish = itemElement.getAttribute("dish");
                    String price = itemElement.getAttribute("price");
                    out.append("<p>Item: ").append(dish).append(" - Price: $").append(price).append("</p>");
                }
            }

            out.append("</div>");
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
