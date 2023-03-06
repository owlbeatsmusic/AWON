package com.owlbeatsmusic;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        AWON.writeVariable("a", new Object[]{"hej", new Object[]{231,123}, 0}, new File("src/com/owlbeatsmusic/test.awon"));// [String, [int, String]
    }

}
