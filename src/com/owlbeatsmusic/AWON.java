package com.owlbeatsmusic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AWON {


    private static final String template = "<type>\s<name>\s=\s<value>";
    static int count = 0;
    static StringBuilder typeString = new StringBuilder();
    static ArrayList<Integer> lengths = new ArrayList<>();


    private enum listDetectionMode {
        TYPE,
        VALUE
    }
    private static void remove(int index) {
        if (count >= 0) {
            lengths.set(count, lengths.get(count) - 1);
            if (lengths.get(count) == 0) {
                lengths.remove(count);
                count--;
                typeString.append("]");
                if (count >= 0) {
                    if (lengths.get(count) != 1) {
                        typeString.append(", ");
                    }
                }
                remove(index);
            }
        }
    }
    private static void detectListDimensions(Object variable, listDetectionMode mode) {
        if (count == 0) {
            typeString.append("[");
            lengths.add(((Object[]) variable).length+1);
        }
        for (int index = 0; index < ((Object[]) variable).length; index++) {
            try {
                String.valueOf(((Object[]) ((Object[]) variable)[index]).length);
                typeString.append("[");
                remove(index);
                lengths.add(((Object[]) ((Object[]) variable)[index]).length+1);
                count++;
                detectListDimensions(((Object[]) variable)[index], mode);
            }
            catch (ClassCastException ignored) {
                remove(index);
                switch (mode) {
                    case TYPE -> typeString.append(String.valueOf(((Object[]) variable)[index].getClass()).replace("class java.lang.", ""));
                    case VALUE -> typeString.append(((Object[]) variable)[index]);
                }
                if (index != ((Object[]) variable).length - 1) typeString.append(", ");
            }
        }
    }


    private static void resetListDetection() {
        typeString = new StringBuilder();
        lengths = new ArrayList<>();
        count = 0;
    }
    private static String listToString(Object variable) {
        resetListDetection();
        detectListDimensions(variable, listDetectionMode.TYPE);
        remove(-1);
        return typeString.toString();
    }
    private static String listValuesToString(Object variable) {
        resetListDetection();
        detectListDimensions(variable, listDetectionMode.VALUE);
        remove(-1);
        return typeString.toString();
    }


    public static void setFileToList(ArrayList<String> inputList, File outputFile) {                        // From OLib.Fil
        try {
            Files.write(Paths.get(outputFile.getAbsolutePath()), inputList, StandardCharsets.UTF_8);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    public static ArrayList<String> fileToLines(File inputFile) {                                           // From OLib.Fil
        ArrayList<String> outputList = new ArrayList<>();
        try {
            outputList = (ArrayList<String>) Files.readAllLines(inputFile.toPath(), Charset.defaultCharset());
        } catch (IOException io) {
            io.printStackTrace();
        }
        return outputList;
    }
    private static ArrayList<String> readAllVariableNames(File awonFile) {
        ArrayList<String> lines = fileToLines(awonFile);
        for (int l = 0; l < lines.size(); l++) {
            try {
                lines.set(l, lines.get(l).split(" ")[1]);
            } catch (ArrayIndexOutOfBoundsException emptyLineIgnored) {}
        }
        return lines;
    }


    private static ArrayList<String> readAllVariables(File awonFile) {

        return null;
    }
    public static String createVariableString(String name, Object variable) {
        String type = String.valueOf(variable.getClass()).replace("class java.lang.", "");
        String value = String.valueOf(variable);
        try {
            String.valueOf(variable);
            type = listToString(variable);
            value = listValuesToString(variable);
        } catch (ClassCastException ignored) {};

        // detect if list or not, create string

        String output = template;
        output = output.replace("<type>", type);
        output = output.replace("<name>", name);
        output = output.replace("<value>", value);
        return output;
    }


    public static void newVariable(String name, Object variable, File awonFile) {
        ArrayList<String> lines = fileToLines(awonFile);
        lines.add(createVariableString(name, variable));
        setFileToList(lines, awonFile);
        System.out.println("NEW");
    }
    private static void updateVariable(String name, Object variable, File awonFile) {
        ArrayList<String> lines = readAllVariableNames(awonFile);
        int l = 0;
        while (l < lines.size()) {
            if (lines.get(l).contains(" " + name + "")) break;
            l++;
        }
        lines.set(l-1, createVariableString(name, variable));
        setFileToList(lines, awonFile);
        System.out.println("UPDATED");
    }
    public static void writeVariable(String name, Object variable, File awonFile) {
        ArrayList<String> variableNames = readAllVariableNames(awonFile);

        boolean isSet = false;
        for (String variableName : variableNames) {
            if (variableName.equals(name)) {
                updateVariable(name, variable, awonFile);
                isSet = true;
            }
        }
        if (!isSet) newVariable(name, variable, awonFile);
    }


    public static Object readVariable(String requestedName, File awonFile) {
        Map<String, Object> allVariables = new HashMap<>(); // {requestedName, object}

        ArrayList<String> lines = new ArrayList<>();
        for (int l = 0; l < lines.size(); l++) {
            Object variable = lines.get(l).split(" = ")[1];

            try {
                String.valueOf(variable);
                String type = listToString(variable);
                String value = listValuesToString(variable);

                // TODO: Fortsätt här med att parsa list-strängen

            } catch (ClassCastException ignored) {};



            allVariables.put(lines.get(l).split(" ")[1], variable);
        }

        for (String variableName : allVariables.keySet()) {
            if (variableName.equals(requestedName)){
                return allVariables.get(variableName);
            }
        }
        return null;
    }

}
