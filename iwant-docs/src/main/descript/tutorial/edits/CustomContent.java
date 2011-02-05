package com.example.wsdef2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.iwant.core.Content;
import net.sf.iwant.core.Path;
import net.sf.iwant.core.RefreshEnvironment;
import net.sf.iwant.core.Target;
import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;

public class CustomContent implements Content {

    private final String value;

    public static CustomContent value(String value) {
        return new CustomContent(value);
    }

    private CustomContent(String value) {
        this.value = value;
    }

    public String definitionDescription() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName()).append(" {\n");
        b.append("  value:").append(value).append("\n");
        b.append("}\n");
        return b.toString();
    }

    public SortedSet<Path> sources() {
        return new TreeSet<Path>();
    }

    public SortedSet<Target> dependencies() {
        return new TreeSet<Target>();
    }

    public void refresh(RefreshEnvironment refresh) throws IOException {
        new FileWriter(refresh.destination()).append(message()).close();
    }

    private String message() {
        try {
            return value + " " + new Fraction(42).intValue() + "\n";
        } catch (FractionConversionException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
