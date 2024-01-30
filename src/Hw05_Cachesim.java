import edu.westminsteru.cmpt328.cachesim.Runtime;
import edu.westminsteru.cmpt328.cachesim.annotations.*;

import java.util.Random;

// @Memory and child annotations defines the configuration of the memory system. This replaces all the
// "MainMemory ram = " and "Cache cache = " lines in the main method.
// With this new caching library, there is no need to rewrite the Java code to use MemorySystem, IntValue, etc.!
@Memory(
    ram = @RAM(
        accessTime = 200
    ),
    caches = {
        @Cache(
            name = "L3",
            accessTime = 42,
            lines = 256,
            mapping = MappingAlgorithm.Direct
            // other possibilities for mapping:
            //   MappingAlgorithm.FullyAssociative
            //   MappingAlgorithm.SetAssociative
            
            // Other possible options:
            
            // ways = 4 // required if mapping is set-associative
            
            // replacement = ReplacementAlgorithm.LRU
            // other possibilities for replacement:
            //   ReplacementAlgorithm.FIFO
            //   ReplacementAlgorithm.LFU
            //   ReplacementAlgorithm.RANDOM
        ),
        @Cache(
            name = "L2",
            accessTime = 20,
            lines = 64,
            mapping = MappingAlgorithm.Direct
        )
    }
)
public class Hw05_Cachesim {

    public static void main(String... args) {
        double[] data = new double[1000];
        
        // Fill the array with data and sort it. Keep track of the total system memory access
        // time so we can see how many cycles of memory access were needed.
        fillArray(data);
        insertionSort(data);
        
        // Print the data out so you can confirm that it is sorted
        System.out.println("After sort:");
        printArray(data);
        System.out.println();

        // Display cache statistics
        Runtime.viewStatistics();
    }

    // @MemoryExempt tells the cache simulation library not to track memory accesses in this method
    @MemoryExempt
    private static void fillArray(double[] data) {
        int i;
        Random random = new Random(0);
        for (i = 0; i < data.length; ++i)
            data[i] = random.nextDouble();
    }
    
    @MemoryExempt
    private static void printArray(double[] data) {
        int i;
        for (i = 0; i < data.length; ++i)
            System.out.println(data[i]);
    }
    
    private static void insertionSort(double[] data) {
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
    
    private static void quickSort(double[] data) {
        int a, b;
        
        a = 0;
        b = data.length;
        quickSort(data, a, b);
    }
  
    private static void quickSort(double[] data, int a, int b) {
        if (a >= b)
            return;
        
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

