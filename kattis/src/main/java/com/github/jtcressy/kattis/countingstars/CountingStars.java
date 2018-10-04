package com.github.jtcressy.kattis.countingstars;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CountingStars {
    public CountingStars(int m, int n, char[][] picture) {
        this.m = m;
        this.n = n;
        this.picture = picture;
    }

    public static void main(String[] args) throws Exception{
        List<CountingStars> cases = CountingStars.consumeInput(System.in);
        for (int i = 0; i < cases.size(); i++) {
            System.out.println("Case " + (i+1) + ": " + cases.get(i).go());
        }
    }

    static List<CountingStars> consumeInput(String in) throws Exception{
        InputStream sstream = new ByteArrayInputStream(in.getBytes());
        return consumeInput(sstream);
    }

    static List<CountingStars> consumeInput(InputStream in) throws Exception{
        List<CountingStars> cslist = new ArrayList<CountingStars>();
        Scanner sc = new Scanner(in);
        while(sc.hasNext() && sc.hasNextInt()) {
            int m = sc.nextInt();
            int n = sc.nextInt();
            sc.nextLine();
            char[][] image = new char[m][n];
            int line = 0;
            do {
                if (line >= m) {
                    break;
                }
                image[line] = sc.nextLine().toCharArray();
                line++;
                boolean nextline = sc.hasNextLine();
                boolean nextint = sc.hasNextInt();
            } while(sc.hasNext());
            int output;
            CountingStars cs = new CountingStars(m, n, image);
            cslist.add(cs);
        }
        return cslist;
    }

    private char[][] picture;
    int m;
    int n;

    int go() throws Exception {
        int star_count = 0;
        for (int i = 0; i < m-1; i++) {
            for (int j = 0; j < n-1; j++) {
                if (picture[i][j] == '-') {
                    star_count++;
                    picture = remove_star(i, j, picture);
                }
            }
        }
        return star_count;
    }

    private char[][] remove_star(int m, int n, char[][] image) throws Exception {
        if (image[m][n] == '#') {
            return image;
        }
        image[m][n] = '#';
        if (m > 0) {
            image = remove_star(m-1, n, image);
        }
        if (m < image.length - 1) {
            image = remove_star(m + 1, n, image);
        }
        if (n > 0) {
            image = remove_star(m, n-1, image);
        }
        if (n < image[m].length - 1) {
            image = remove_star(m, n+1, image);
        }
        return image;
    }
}
