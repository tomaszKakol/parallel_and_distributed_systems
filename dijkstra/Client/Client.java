package Client;

import Shared.*;
import java.rmi.*;
import java.util.concurrent.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Client
{
    public static void main( String args[] ) throws Exception
    {
        System.out.println("\nClient started... \n " +
                "The following number of arguments have been entered: " + args.length);

        //In the same way as in the example from lesson 11. Java RMI / JNI

     //   if (args.length < 3)
     //  {
     //     System.out.println("\nYou should enter the following arguments:\n" +
     //             " 'testcaseIssue' & 'hostName' &  more then one 'serverPort'\n" +
     //            "For example: make runclient Testcase=0 hostIP=127.0.0.1 Ports=\"0001 0002\" \n");
     //      return;
     //   }

        String testcaseIssue = args[0];
        String hostName = args[1];
        String[] ports = new String[args.length - 2];
        //all arguments minus testcaseIssue & hostName

        for(int i=2; i<args.length; ++i)//set ports
            ports[i-2] = args[i];

        try
        {
            System.out.println("\nLoading the graph...\n");
            Graph myCase = Graph.fromFile("testcases/" + testcaseIssue);//handle a test case from a file
            myCase.printAdjacencyMatrix();

            System.out.println("\nWe're just starting the Dijkstra algorithm...\n");
            new Dijkstra(myCase, hostName, ports).run();//use main method

            System.out.println("\nEnd of main function in class 'Client'.");
        }
        catch(Exception e)
        {
            System.out.println("\nError! An exception was thrown ...\n");
            e.printStackTrace();
        }
    }
}
