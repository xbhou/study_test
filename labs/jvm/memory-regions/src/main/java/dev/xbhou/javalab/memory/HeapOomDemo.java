package dev.xbhou.javalab.memory;

import java.util.ArrayList;
import java.util.List;

public class HeapOomDemo {

    private static final int BLOCK_SIZE = 1024 * 1024;

    public static void main(String[] args) {
        List<byte[]> blocks = new ArrayList<>();
        int allocatedMiB = 0;

        while (true) {
            blocks.add(new byte[BLOCK_SIZE]);
            allocatedMiB++;

            if (allocatedMiB % 4 == 0) {
                System.out.println(
                        "retained heap allocation=" + allocatedMiB + " MiB"
                );
            }
        }
    }
}
