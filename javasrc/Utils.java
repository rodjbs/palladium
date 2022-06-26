import java.util.HashMap;
import java.util.ArrayList;

class Utils {

    public static HashMap<String, String> strings;

    public static HashMap<String, Double> numbers;

    public static HashMap<String, ArrayList<String>> stringArrays;

    public static HashMap<String, ArrayList<Double>> numberArrays;

    public static void init() {
        strings = new HashMap<String, String>();
        numbers = new HashMap<String, Double>();
        stringArrays = new HashMap<String, ArrayList<String>>();
        numberArrays = new HashMap<String, ArrayList<Double>>();
    }

    public static double not(double x) {
        return x == 0 ? 1 : 0;
    }

    public static double lteq(double a, double b) {
        return a <= b ? 1 : 0;
    }

    public static double gteq(double a, double b) {
        return a >= b ? 1 : 0;
    }

    public static double lt(double a, double b) {
        return a < b ? 1 : 0;
    }

    public static double gt(double a, double b) {
        return a > b ? 1 : 0;
    }

    public static double eq(double a, double b) {
        return a == b ? 1 : 0;
    }

    public static double neq(double a, double b) {
        return a != b ? 1 : 0;
    }

    public static double eq(String a, String b) {
        return a.equals(b) ? 1 : 0;
    }

    public static double lteq(String a, String b) {
        return (!a.equals(b)) ? 1 : 0;
    }

    public static double and(double a, double b) {
        return (a != 0 && b != 0) ? 1 : 0;
    }

    public static double or(double a, double b) {
        return (a != 0 || b != 0) ? 1 : 0;
    }
}