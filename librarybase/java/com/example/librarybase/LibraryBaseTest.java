package com.example.librarybase;

/**
 * User: HW
 * Date: 2020/4/23
 * Description: 底层库测试类，用来测试各种底层方法功能
 */
public class LibraryBaseTest extends LibraryBase {

    public void runTest() {
        nativeRunTest();
    }

    private static native void nativeRunTest();
}
