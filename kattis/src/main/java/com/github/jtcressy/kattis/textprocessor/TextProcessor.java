package com.github.jtcressy.kattis.textprocessor;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class TextProcessor {
    private char[] text;
    private int width;

    public static void main(String[] args) {
        System.out.print(handleInput(System.in));
    }

    public TextProcessor(String text, int width) {
        this.text = text.toCharArray();
        this.width = width;
    }

    public static String handleInput(InputStream in) {
        Scanner sc = new Scanner(in);
        StringBuffer output = new StringBuffer();
        String input;
        input = sc.nextLine();
        int W, Q;
        Q = sc.nextInt();
        W = sc.nextInt();

        TextProcessor tp = new TextProcessor(input, W);
        for (int i = 0; i < Q; i++) {
            int position = sc.nextInt();
            position--; // make position zero-indexed
            int result = tp.process(position);
            output.append(result);
            output.append('\n');
        }
        return output.toString();
    }

    public long maxTime;

    public int process(int position) {
        char[] substring = new String(text, position, width).toCharArray();
        Set<String> result = new HashSet<String>();
        // Set lets us tally up all substrings, but will omit duplicates.
        // This also counts substrings that are single characters
        // And it also counts substrings which match the original string.
        for(int i = 0; i <= width; i++) {
            for (int j = i + 1; j <= width; j++) {
                result.add(new String(substring, i, j-i));
            }
        }
        return result.size();
    }
}
