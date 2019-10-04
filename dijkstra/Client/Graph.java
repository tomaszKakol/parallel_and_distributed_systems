package Client;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Graph
{
    private int[][] G;
    private int nodesCount;
    private String[] nodesNames;
    static final int noConnection = -1;
    final static String separator = ",";

    private Graph(int verticesCount)
    {
        nodesNames = new String[verticesCount];
        G = new int[verticesCount][verticesCount];
        this.nodesCount = verticesCount;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    int[][] getWeights() {
        return G;
    }

    String[] getNodesNames() {
        return nodesNames;
    }

    public static Graph fromFile(String filename) throws Exception
    {
        System.out.println("The beginning of the 'fromFile' static Graph method.");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String readVerticesCount = br.readLine();
            //validator of the number of vertices in the adjacency matrix,
            //take only the number, remove other characters

            int verticesCount = Integer.parseInt(readVerticesCount.replaceAll("[^0-9]", ""));
            Graph graph = new Graph(verticesCount);

            for (int i = 0; i < verticesCount; ++i) {
                String line = br.readLine();
                String[] cases = line.split(separator);

                for (int j = 0; j < verticesCount; ++j) {
                    String numstr = cases[j].trim();
                    if (numstr.contains("-"))
                        graph.G[i][j] = noConnection;
                    else
                        graph.G[i][j] = Integer.parseInt(numstr);
                }
            }

            for (int i = 0; i < verticesCount; ++i) {
                String nodeName = String.valueOf((char)('A' + i));
                System.out.println("A new node has been added: " + nodeName);
                graph.nodesNames[i] = nodeName;
            }

            return graph;
        }
        catch (Exception e) {
            System.out.println("Error! An exception was thrown ...\n" + e.getMessage());
            throw e;
        }
        //System.out.println("End of the 'fromFile' static Graph method");
    }

    void printAdjacencyMatrix()
    {
        System.out.println("\nAdjacencyMatrix:");
        for (int i = 0; i < nodesCount; ++i) {
            for (int j = 0; j < nodesCount; ++j) {
                if (G[i][j] != noConnection)
                    System.out.print(G[i][j] + " ");
                else
                    System.out.print("-" + " ");
            }
            System.out.println();
        }
    }
}

