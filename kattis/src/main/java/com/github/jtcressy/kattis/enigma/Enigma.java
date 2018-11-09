package com.github.jtcressy.kattis.enigma;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Enigma {
    private char[][] grid;
    private ArrayList<String> words;
    private int rows;
    private int cols;

    public static void main(String[] args) {
        System.out.print(handleInput(System.in));
    }

    public static String handleInput(InputStream input) {
        Scanner sc = new Scanner(input);
        int R, C;
        ArrayList<String> G = new ArrayList<String>();
        ArrayList<String> WORDS = new ArrayList<String>();
        R = sc.nextInt();
        C = sc.nextInt();
        for (int i = 0; i < R; i++) {
            G.add(sc.nextLine());
        }
        int N;
        N = sc.nextInt();
        for (int i = 0; i < N; i++) {
            WORDS.add(sc.nextLine());
        }
        Enigma enigma = new Enigma(G, WORDS, R, C);
        return enigma.process();
    }

    public Enigma(ArrayList<String> grid, ArrayList<String> words, int rows, int cols) {
        this.grid = new char[grid.size()][];
        for (String row : grid) {
            this.grid[grid.indexOf(row)] = row.toCharArray();
        }
        this.words = words;
        this.rows = rows;
        this.cols = cols;
    }

    public String process() {
        StringBuilder sb = new StringBuilder();
        //do work here and return output
        return sb.toString();
    }
}
