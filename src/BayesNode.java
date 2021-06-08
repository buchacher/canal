import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to represent a node in a {@link BayesNet}
 */
public class BayesNode {
    private final String NAME;
    private ArrayList<String> parents;
    private double[][] cpt;

    public BayesNode(String name) {
        this.NAME = name;
        this.parents = new ArrayList<>();
    }

    public void addParent(String parent) {
        parents.add(parent);
    }

    public void setParents(ArrayList<String> p) {
        parents.addAll(p);
    }

    public ArrayList<String> getParents() {
        return parents;
    }

    public void defineCPT(double[][] cpt) {
        this.cpt = cpt;
    }

    public double[][] getCPT() {
        return cpt;
    }

    public void printNode() {
        System.out.println("-");
        System.out.println("Name: " + NAME);

        System.out.print("Parents:");
        if (parents.isEmpty()) {
            System.out.print(" none\n");
        }
        else {
            System.out.println(parents);
        }

        System.out.println("CPT:");
        try {
            for (double[] doubles : cpt) {
                System.out.println(Arrays.toString(doubles));
            }
        } catch (NullPointerException ignored) {
            // See Part 2 Evaluation
        }
    }
}
