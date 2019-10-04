package Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashSet;
import Shared.*;

public class Server extends UnicastRemoteObject implements IServer{
    public Server() throws RemoteException {
        super();
    }


    public static void main(String args[]) throws Exception {
        System.out.println("Server started...");
        
      
       // if (args.length < 1) {
        //    System.out.println("You should enter the following arguments:\n" +
        //            "'hostName' &  more then one 'serverPort'\n" +
        //            "For example: make server hostIP=127.0.0.1 Ports=\"0001 0002\"\n");
        //    return;
       // }

        String hostName = args[0];
        int[] portsCount = new int[args.length-1];

        for(int i=1; i<args.length; ++i) {
            try {
                portsCount[i-1] = i;
                Server obj = new Server();
                //IServer  stub = (IServer)UnicastRemoteObject.exportObject(obj, 0);

                String port = args[i];
                
                System.setProperty("java.rmi.server.hostname", hostName);
                //This is item A.1 in the RMI FAQ. You need to either fix your /etc/hosts file or set the java.rmi.server.hostname property at the server.
                //The value of this property represents the host name string that should be associated with remote stubs for locally created remote objects, in order to allow clients to invoke methods on the remote object. The default value of this property is the IP address of the local host, in "dotted-quad" format.
               
                // System.setProperty("java.rmi.server.codebase");

                Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
                
                reg.rebind("server", obj);
                //reg.rebind("server" +  String.valueOf(portsCount[i-1]), obj);
                //Rebinds the specified name to a new remote object.
                
                System.out.println("Server started on " + hostName + ":" + port);
               
                UnicastRemoteObject.unexportObject(obj,false);
                //Used for exporting a remote object with JRMP and obtaining a stub that communicates to the remote object.
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        System.out.println("End of main function in class 'Server'.");
        //System.exit(0);
    }

    private int[][] G;
    private int prevVertex;
    private int nextVertex;
    private int workerId;
    private int nodesCount;
    private int[] dist;
    private int[] pred;
    private HashSet<Integer> visitedNodes;
    
    public void initialData(int workerId, int nodesCount, int[] ranges, int[][] G) throws RemoteException {
        this.G = G;
        this.workerId = workerId;
        this.prevVertex = ranges[0];
        this.nextVertex = ranges[1];
        this.nodesCount = nodesCount;
        this.visitedNodes = new HashSet<>();
        this.dist = new int[nodesCount];
        this.pred = new int[nodesCount];
        
        for(int i=0; i<nodesCount; ++i)
            this.dist[i] = this.pred[i] = 9999;
    }
    
    public int[] calculateDistances(Integer currentNode, int distToCurrentNode) throws RemoteException {
        System.out.println("The beginning of the 'calculateDistances' method");

        dist[currentNode] = distToCurrentNode;
        
        for(int node=this.prevVertex; node<=this.nextVertex; ++node) {
            if (visitedNodes.contains(node)) {
                System.out.println("Worker '" + this.workerId + "'\n The node: '" + node + "'  was already visited.");
                continue;
            }
            
            if (connectionNodesExists(currentNode, node)) {
                int nodeDistance = this.G[currentNode][node];
                int totalDistToNode = dist[currentNode] + nodeDistance;
                
                System.out.println("Case:\n " +
                        "Worker '" + workerId + "', node '" + currentNode + "' is connected to " + node + "\n" +
                        " distance = " + nodeDistance + " & totalDistToNode = " + totalDistToNode + ".");
                if (totalDistToNode < dist[node]) {
                    dist[node] = totalDistToNode;
                    pred[node] = currentNode;
                    System.out.println("The total cost of distance to this vertex is smaller. We replace the value.");
                }
            }
        }
        
        visitedNodes.add(currentNode);
        System.out.println("End of the 'calculateDistances' method");
        return this.getWorkerDistancesPart();
    }

    public int[] getWorkerPrevNodesPart() throws RemoteException {
        return this.getWorkerMatrixPart(this.pred);
    }

    private boolean connectionNodesExists(int prevVertex, int nextVertex) {
        return this.G[prevVertex][nextVertex] != -1;
    }

    private int[] getWorkerDistancesPart() {
        return this.getWorkerMatrixPart(this.dist);
    }

    private int[] getWorkerMatrixPart(int[] array) {
        int[] boundWeights = new int[this.nextVertex - this.prevVertex + 1];
        for(int i=this.prevVertex; i<=this.nextVertex; ++i)
            boundWeights[i-this.prevVertex] = array[i];
        return boundWeights;
    }
}
