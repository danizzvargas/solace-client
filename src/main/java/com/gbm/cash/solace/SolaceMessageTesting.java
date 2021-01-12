package com.gbm.cash.solace;

import com.gbm.cash.solace.reqrep.BasicReplier;
import com.gbm.cash.solace.reqrep.BasicRequestor;

import java.util.Scanner;

public class SolaceMessageTesting {
    public static void main(String[] args) throws Exception {
        System.out.println("Solace message testing");
        System.out.println("Select one:");
        System.out.println("  0. Request/Reply");
        System.out.println("  1. JMS");
        System.out.println("  2. JCSMP");

        int option = args.length > 0 ? Integer.parseInt(args[0]) : new Scanner(System.in).nextInt();

        switch (option){
            case 0:
                requestReply(args);
                break;
            default:
                System.out.println("Option not supported");
        }

        System.out.println("Done");
    }

    private static void requestReply(String[] args) throws Exception {
        System.out.println("\nRequest/Reply selected");
        System.out.println("Select one:");
        System.out.println("  0. Replier");
        System.out.println("  1. Requestor");

        int option = args.length > 1 ? Integer.parseInt(args[1]) : new Scanner(System.in).nextInt();

        switch (option){
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
