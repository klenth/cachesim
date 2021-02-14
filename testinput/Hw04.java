import java.util.Random;
import edu.westminstercollege.cmpt328.cachesim.annotations.*;

@MemoryAware
@Memory(
    ram = @RAM(
        name = "Main memory",
        accessTime = 200
    ),
    caches = {
        @Cache(
            name = "L3",
            accessTime = 80,
            lines = 64,
            mapping = MappingAlgorithm.SetAssociative,
            ways = 4
        ),
        @Cache(
            name = "L2",
            accessTime = 42,
            lines = 16,
            mapping = MappingAlgorithm.SetAssociative,
            ways = 4
        ),
        @Cache(
            name = "L1",
            accessTime = 20,
            lines = 8,
            mapping = MappingAlgorithm.Direct
        )
    }
)
public class Hw04 {

    static double[] data;

    public static void main(String... args) {
        double[] data = new double[1000];
        String[] s = new String[25];
        // Fill the array with data and sort it. Keep track of the total system memory access
        // time so we can see how many cycles of memory access were needed.
        fillArray(data);
        quickSort(data);
        
        // Print the data out so you can confirm that it is sorted
        System.out.println("After sort:");
        printArray(data);
        System.out.println();

        edu.westminstercollege.cmpt328.cachesim.Runtime.viewStatistics();
    }
    
    // Rewrite this code to use the MemorySystem (you can leave the Random alone)
    // (int variables should become IntValues, double[] becomes DoubleArrayValue, etc.)
    private static void fillArray(double[] data) {
        // Don't forget to use sys.allocateXxx() to allocate local variables!
        int i;
        Random random = new Random(0);
        for (i = 0; i < data.length; ++i)
            data[i] = random.nextDouble();
    }
    
    // Rewrite this code to use the MemorySystem
    // (int variables should become IntValues, double[] becomes DoubleArrayValue, etc.)
    private static void printArray(double[] data) {
        // Don't forget to use sys.allocateXxx() to allocate local variables!
        int i;
        for (i = 0; i < data.length; ++i)
            System.out.println(data[i]);
    }
    
    // Rewrite this code to use the MemorySystem
    // (int variables should become IntValues, double[] becomes DoubleArrayValue, etc.)
    private static void insertionSort(double[] data) {
        // Don't forget to use sys.allocateXxx() to allocate local variables!
        int i, j;
        double tmp;
        
        // Starting with the second one, move every array element to the left until it's in the right slot
        for (i = 1; i < 1000; ++i) {
            for (j = i; j > 0 && data[j] < data[j - 1]; --j) {
                // Swap data[j] with data[j - 1]
                tmp = data[j];
                data[j] = data[j - 1];
                data[j - 1] = tmp;
            }
        }
    }
    
    // Rewrite this code to use the MemorySystem
    // (int variables should become IntValues, double[] becomes DoubleArrayValue, etc.)
    private static void quickSort(double[] data) {
        // Don't forget to use sys.allocateXxx() to allocate local variables!
        int a, b;
        
        a = 0;
        b = data.length;
        quickSort(data, a, b);
    }
  
    // Rewrite this code to use the MemorySystem
    // (int variables should become IntValues, double[] becomes DoubleArrayValue, etc.)
    private static void quickSort(double[] data, int a, int b) {
        if (a >= b)
            return;
        
        // Don't forget to use sys.allocateXxx() to allocate local variables!
        int i, piv;
        double tmp;
        
        // March through the array, moving everything smaller than the pivot to its left and
        // leaving everything larger on the right
        piv = a;
        for (i = a; i < b; ++i) {
            if (data[i] < data[piv]) {
                // swap data[i] with data[piv + 1]
                tmp = data[i];
                data[i] = data[piv + 1];
                data[piv + 1] = tmp;
                
                // swap data[piv] with data[piv + 1]
                tmp = data[piv];
                data[piv] = data[piv + 1];
                data[piv + 1] = tmp;
                
                ++piv;
            }
        }
        
        // Recursively sort everything above and below the pivot
        quickSort(data, a, piv);
        ++piv;
        quickSort(data, piv, b);
    }
}

