import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to perform a merger between two {@link BayesNet}
 */
public class BayesMerger {
    private final BayesNet BN1; // X
    private final BayesNet BN2; // Y
    private final BayesNet BNT;
    private ArrayList<String> T; // All nodes that are in X, Y
    private ArrayList<String> Z; // Intersection; nodes both in X and Y
    private ArrayList<String> Q; // Non-intersection; nodes in X or Y exclusively
    private ArrayList<String> ZI; // Internal nodes
    private ArrayList<String> ZE; // External nodes

    public BayesMerger(BayesNet BN1, BayesNet BN2) {
        this.BN1 = BN1;
        this.BN2 = BN2;
        this.BNT = new BayesNet();
        this.T = new ArrayList<>();
        this.Z = new ArrayList<>();
        this.Q = new ArrayList<>();
        this.ZI = new ArrayList<>();
        this.ZE = new ArrayList<>();

        setupMerger();
        buildMerger();
        treatNonIntersection();
        treatInternalNodes();
        treatExternalNodes();
    }

    /**
     * Steps 1-3
     * Sets up the merger by categorising nodes.
     */
    private void setupMerger() {
        // T - all nodes that are in X and in Y
        T.addAll(BN1.getNodeNames());
        for (String node : BN2.getNodeNames()) {
            if (!T.contains(node)) {
                T.add(node);
            }
        }

        // Z - intersection; Q - non-intersection
        for (String node : BN1.getNodeNames()) {
            if (BN2.getNodeNames().contains(node)) {
                Z.add(node);
            }
            else {
                Q.add(node);
            }
        }

        // ZI - internal nodes; ZE - external nodes
        for (String node : Z) {
            if (Z.containsAll(BN1.getNode(node).getParents()) || Z.containsAll(BN2.getNode(node).getParents())) {
                ZI.add(node);
            }
            else {
                ZE.add(node);
            }
        }
    }

    /**
     * Creates {@link BayesNet} object with {@link BayesNode}'s
     */
    private void buildMerger() {
        for (String node : T) {
            BNT.addNode(node);
        }
    }

    /**
     * Step 4
     */
    private void treatNonIntersection() {
        for (String node : Q) {
            if (BN1.getNodeNames().contains(node)) {
                BNT.getNode(node).setParents(BN1.getNode(node).getParents());
                BNT.getNode(node).defineCPT(BN1.getNode(node).getCPT());
            }
            else {
                BNT.getNode(node).setParents(BN2.getNode(node).getParents());
                BNT.getNode(node).defineCPT(BN2.getNode(node).getCPT());
            }
        }
    }

    /**
     * Steps 5-8
     * In contrast to lectures, only the respective dependencies are included in the first place as opposed to keeping
     * or removing dependencies.
     */
    private void treatInternalNodes() {
        for (String node : ZI) {
            // Case A
            if (Z.containsAll(BN1.getNode(node).getParents())) {
                BNT.getNode(node).setParents(BN1.getNode(node).getParents());
                BNT.getNode(node).defineCPT(BN1.getNode(node).getCPT());
            }
            // Case B
            else if (Z.containsAll(BN2.getNode(node).getParents())) {
                BNT.getNode(node).setParents(BN2.getNode(node).getParents());
                BNT.getNode(node).defineCPT(BN2.getNode(node).getCPT());
            }
            // Case C
            else {
                if (BN1.getNode(node).getParents().size() >= BN2.getNode(node).getParents().size()) {
                    BNT.getNode(node).setParents(BN1.getNode(node).getParents());
                    BNT.getNode(node).defineCPT(BN1.getNode(node).getCPT());
                }
                else {
                    BNT.getNode(node).setParents(BN2.getNode(node).getParents());
                    BNT.getNode(node).defineCPT(BN2.getNode(node).getCPT());
                }
            }
        }
    }

    /**
     * Steps 9-10
     * Treats external nodes, combines their CPTs
     */
    private void treatExternalNodes() {
        for (String node : ZE) {
            for (String parent1 : BN1.getNode(node).getParents()) {
                BNT.getNode(node).addParent(parent1);
            }
            for (String parent2 : BN2.getNode(node).getParents()) {
                BNT.getNode(node).addParent(parent2);
            }
        }

        for (String node : ZE) {
            double[][] cpt1 = BN1.getNode(node).getCPT();
            double[][] cpt2 = BN2.getNode(node).getCPT();
            double[][] cptT = new double[4][2];

            cptT[0][0] = calculateIntermediateValue(cpt1[0][0], cpt2[0][0]);
            cptT[0][1] = calculateIntermediateValue(cpt1[0][1], cpt2[0][1]);
            cptT[1][0] = calculateIntermediateValue(cpt1[0][0], cpt2[1][0]);
            cptT[1][1] = calculateIntermediateValue(cpt1[0][1], cpt2[1][1]);
            cptT[2][0] = calculateIntermediateValue(cpt1[1][0], cpt2[0][0]);
            cptT[2][1] = calculateIntermediateValue(cpt1[1][1], cpt2[0][1]);
            cptT[3][0] = calculateIntermediateValue(cpt1[1][0], cpt2[1][0]);
            cptT[3][1] = calculateIntermediateValue(cpt1[1][1], cpt2[1][1]);

            // Breaking Kolmogorov's axioms, therefore...
            for (int i = 0; i < cptT.length; i++) {
                double rowSum = Arrays.stream(cptT[i]).sum();
                for (int j = 0; j < cptT[0].length; j++) {
                    cptT[i][j] = cptT[i][j]/rowSum; // ...normalise each row
                }
            }

            BNT.getNode(node).defineCPT(cptT);
        }
    }

    /**
     * Helper method to calculate intermediate values when combining CPTs
     */
    private double calculateIntermediateValue(double x, double y) {
        return (x + y) - (x * y);
    }

    public BayesNet getBNT() {
        return BNT;
    }
}
