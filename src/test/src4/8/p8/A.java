package p8;

public class A {

    public String getBaseTypeName() throws Exception {
        debugCodeCall("getBaseTypeName");
        checkClosed();
        return "NULL";
    }

    public int getMaxFieldSize() throws Exception {
        debugCodeCall("getMaxFieldSize", 10);
        checkClosed();
        return 0;
    }

    private void debugCodeCall(String str) {
    }

    private void debugCodeCall(String str, int m) {
    }

    private void checkClosed() {
    }
}
