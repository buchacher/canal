public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(0);
        }

        BayesNet bn1 = new BayesNet(args[0]);
        BayesNet bn2 = new BayesNet(args[1]);
        BayesMerger merger = new BayesMerger(bn1, bn2);

        bn1.printNet();
        bn2.printNet();
        merger.getBNT().printNet();
    }

    private static void printUsage() {
        System.out.println("Usage: java A4main <BN1> <BN2>");
    }
}

