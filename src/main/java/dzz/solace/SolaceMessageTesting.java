package dzz.solace;

import dzz.solace.jcsmp.SolaceClientJCSMP;
import dzz.solace.reqrep.BasicReplier;
import dzz.solace.reqrep.BasicRequestor;

import java.util.Scanner;

public class SolaceMessageTesting {
    public static void main(String[] args) throws Exception {
        System.out.println("Solace message testing");
        System.out.println("  0. JCSMP");
        System.out.println("  1. Request/Reply");
        System.out.print("Select one: ");

        int option = args.length > 0 ? Integer.parseInt(args[0]) : new Scanner(System.in).nextInt();

        switch (option) {
            case 0:
                SolaceClientJCSMP.main(args);
                break;
            case 1:
                requestReply(args);
                break;
            default:
                System.out.println("Option not supported");
        }

        System.out.println("Done");
    }

    private static void requestReply(String[] args) throws Exception {
        System.out.println("\nRequest/Reply selected");
        System.out.println("  0. Replier");
        System.out.println("  1. Requestor");
        System.out.print("Select one: ");

        int option = args.length > 1 ? Integer.parseInt(args[1]) : new Scanner(System.in).nextInt();

        switch (option) {
            case 0:
                new BasicReplier().run();
                break;
            case 1:
                new BasicRequestor().run();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}
