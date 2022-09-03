package org.sourcegrade.jagr.api;

public class Foo {

    public int bar(int[] arr) {
        int max = arr[0];
        int i;
        for (i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    private static String BAR_BYTECODE = """
        BB1:
            max = arr[0];
            i = 0;
            i = 1;
        BB2:
            if (i > arr.length) goto BB6;
        BB3:
            if (arr[i] > max) goto BB5;
        BB4:
            max = arr[i];
        BB5:
            i = i + 1;
            goto BB2;
        BB6:
            return max;
        """;
}
