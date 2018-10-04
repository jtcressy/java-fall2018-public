package com.github.jtcressy.kattis.addingwords;

import com.github.jtcressy.kattis.addingwords.AddingWords;
import org.junit.Test;


import static org.junit.Assert.*;

public class AddingWordsTest {

    @Test
    public void def() {
        AddingWords aw = new AddingWords();
        aw.def(new String[]{"def", "foo", "3"});
        assertTrue(aw.vars.containsKey("foo"));
        assertEquals(new Integer(3), aw.vars.get("foo"));
        aw.def(new String[]{"def", "bar", "3"});
        assertTrue(aw.vars.containsKey("bar"));
        assertEquals(new Integer(3), aw.vars.get("bar"));
        aw.def(new String[]{"def", "wow", "6"});
        assertTrue(aw.vars.containsKey("wow"));
        assertEquals(new Integer(6), aw.vars.get("wow"));
    }

    @Test
    public void clear() {
        AddingWords aw = new AddingWords();
        aw.process("calc foo + bar ="); // fills result with standard "invalid" statement
        assertEquals("foo + bar = unknown", aw.getResult());
        aw.clear(null);
        assertEquals("", aw.getResult());
    }

    @Test
    public void sum() {
        AddingWords aw = new AddingWords();
        int a = (int) Math.random();
        int b = (int) Math.random();
        assertEquals((a + b), aw.sum(a, b));
    }

    @Test
    public void diff() {
        AddingWords aw = new AddingWords();
        int a = (int) Math.random();
        int b = (int) Math.random();
        assertEquals((a - b), aw.sum(a, b));
    }

    @Test
    public void process() { // Also tests AddingWords.calc
        AddingWords aw = new AddingWords();
        aw.process("def foo 3");
        aw.process("calc foo + bar =");
        assertEquals("foo + bar = unknown", aw.getResult());
        aw.process("def bar 7");
        aw.process("def programming 10");
        aw.process("calc foo + bar =");
        assertEquals("foo + bar = programming", aw.getResult());
        aw.process("def is 4");
        aw.process("def fun 8");
        aw.process("calc programming - is + fun =");
        assertEquals("programming - is + fun = unknown", aw.getResult());
        aw.process("def fun 1");
        aw.process("calc programming - is + fun =");
        assertEquals("programming - is + fun = bar", aw.getResult());
        aw.process("clear");
        assertEquals("", aw.getResult());
    }
}