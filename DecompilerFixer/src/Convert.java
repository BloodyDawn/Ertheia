import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eugene Chipachenko on 24.12.2014.
 */
public class Convert {
    private static final Map<Integer, Document> map = new HashMap<>();
    private static DocumentBuilderFactory dbFactory;
    private static DocumentBuilder dBuilder;

    public static void main(String[] args) throws Exception {
        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();

        Document document = dBuilder.parse(new File("E:\\java-projects\\L2WT\\data\\stats\\skills\\base\\data.xml"));
        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().equals("skill")) {
                int skillId = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
                Document d = getDocumentElement(skillId);
                Node n = d.importNode(node, true);
                d.getDocumentElement().appendChild(n);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        for (Map.Entry<Integer, Document> docs : map.entrySet()) {
            int id = docs.getKey();
            Document doc = docs.getValue();
            String name = id * 100 + "_" + (id * 100 + 99) + ".xml";
            System.out.println(name);
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("E:\\java-projects\\L2WT\\data\\stats\\skills\\base\\", name));
            transformer.transform(source, result);
        }
    }

    private static Document getDocumentElement(final int id) {
        int id2 = id / 100;
        Document document = map.get(id2);
        if (document == null) {
            document = dBuilder.newDocument();
            document.appendChild(document.createElement("list"));
            map.put(id2, document);
        }
        return document;
    }
}
