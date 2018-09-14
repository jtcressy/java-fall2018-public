package com.github.jtcressy.kattis;
// Kattis Problem: https://open.kattis.com/problems/addingwords
// Accepted Submission: https://open.kattis.com/submissions/3056533
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

interface AWFns {
    void calc(String[] args);
    void def(String[] args);
    void clear(String[] args);
    int sum(int a, int b);
    int diff(int a, int b);
}

public class AddingWords implements AWFns {
    HashMap<String, Integer> vars = new HashMap<String, Integer>();

    public AddingWords() {
        result = "";
    }

    public String getResult() {
        return result;
    }

    private String result;

    public void calc(String[] args) {
        String expression = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        // array slicing
        // java: Arrays.copyOfRange(array, 1, array.length);
        // python: array[1:]
        // I love python.
        String invalid = expression + " unknown";
        if (!vars.containsKey(args[1])) {
            result = invalid;
            return;
        }
        int a = vars.get(args[1]);
        int i = 2;
        while (!args[i].equals("=")) {
            if (!vars.containsKey(args[i+1])) {
                result = invalid;
                return;
            }
            int b = vars.get(args[i+1]);
            switch (args[i]) {
                case "+":
                    a = sum(a, b);
                    break;
                case "-":
                    a = diff(a, b);
                    break;
            }
            i += 2;
        }
        for (String var : vars.keySet() ) {
            if (vars.get(var).equals(a)) {
                result = expression + " " + var;
                return;
            }
        }
        result = invalid;
    }

    public void def(String[] args) {
        vars.put(args[1], Integer.parseInt(args[2]));
        result = "";
    }

    public void clear(String[] args) {
        vars.clear();
        result = "";
    }

    public int sum(int a, int b) {
        return a + b;
    }

    public int diff(int a, int b) {
        return a - b;
    }

    public static void main(String[] args) throws Exception {
        AddingWords aw = new AddingWords();
        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()) {
            aw.process(sc.nextLine());
            if (!aw.toString().equals("")) {
                System.out.println(aw.toString());
            }
        }
    }

    void process(String s) {
        String[] args = s.split(" ");
        switch (args[0]) {
            case "calc":
                calc(args);
                break;
            case "def":
                def(args);
                break;
            case "clear":
                clear(args);
                break;
        }
    }

    @Override
    public String toString() {
        return result;
    }
}
