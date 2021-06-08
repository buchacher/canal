import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Step 0
 * Class to form a Bayesian network from parsed XML
 */
public class BayesNet {
    private final String FILENAME;
    private HashMap<String, BayesNode> nodes;

    public BayesNet(String filename) {
        this.FILENAME = filename;
        this.nodes = new HashMap<>();

        parseXML("../saved-networks/" + FILENAME + ".xml");
    }

    /**
     * Second constructor for merged network
     */
    public BayesNet() {
        this.FILENAME = "Merger";
        this.nodes = new HashMap<>();
    }

    /**
     *
     * References:
     * - https://youtu.be/HfGWVy-eMRc
     */
    private void parseXML(String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(path);

            // Parse nodes as key(name)-value(BayesNode) pairs
            NodeList variables = doc.getElementsByTagName("VARIABLE");
            for (int i = 0; i < variables.getLength(); i++) {
                Element var = (Element) variables.item(i);
                Node nodeName = var.getElementsByTagName("NAME").item(0);
                String name = nodeName.getTextContent();
                BayesNode node = new BayesNode(name);
                nodes.put(name, node);
            }

            // Parse dependencies and CPTs for each node
            NodeList definitions = doc.getElementsByTagName("DEFINITION");
            for (int i = 0; i < definitions.getLength(); i++) {
                Element def = (Element) definitions.item(i);
                String child = def.getElementsByTagName("FOR").item(0).getTextContent();

                // Dependencies
                NodeList parents = def.getElementsByTagName("GIVEN");
                for (int j = 0; j < parents.getLength(); j++) {
                    String parent = parents.item(j).getTextContent();
                    nodes.get(child).addParent(parent);
                }

                // CPTs
                NodeList tables = def.getElementsByTagName("TABLE");
                String table = tables.item(0).getTextContent();
                nodes.get(child).defineCPT(parseCPT(table));
            }
        } catch (NullPointerException | ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    public double[][] parseCPT(String table) {
        String[] probas = table.split(" ");
        double[][] cpt = new double[probas.length/2][2];
        int c = 0;
        for (int i = 0; i < cpt.length; i++) {
            for (int j = 0; j < cpt[i].length; j++) {
                cpt[i][j] = Double.parseDouble(probas[c]);
                c++;
            }
        }
        return cpt;
    }

    public void addNode(String node) {
        nodes.put(node, new BayesNode(node));
    }

    public BayesNode getNode(String node) {
        return nodes.get(node);
    }

    public ArrayList<String> getNodeNames() {
        ArrayList<String> nodeNames = new ArrayList<>();
        nodes.forEach((name, node) -> nodeNames.add(name));
        return nodeNames;
    }

    public void printNet() {
        System.out.println("\nNet: " + FILENAME);
        nodes.forEach((name, node) -> node.printNode());
        System.out.println("-");
    }
}
