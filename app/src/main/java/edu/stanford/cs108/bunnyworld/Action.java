package edu.stanford.cs108.bunnyworld;

import java.util.Scanner;

public class Action implements BunnyWorldConstants {
    private String verb, modifier;

    public static Action parseAction(String actionString) {
        Scanner stringScanner = new Scanner(actionString);
        stringScanner.useDelimiter(VERB_MODIFIER_DELIMITER);
        String verb = stringScanner.next();
        stringScanner.useDelimiter(ACTION_DELIMITER);
        String modifier = stringScanner.next();
        stringScanner.close();
        return new Action(verb, modifier);
    }
    public static String createActionString(String verb, Object modifier) {
        return verb + VERB_MODIFIER_DELIMITER + modifier + ACTION_DELIMITER;
    }

    private Action(String verb, String modifier) {
        this.verb = verb;
        this.modifier = modifier;
    }

    public String getVerb() {
        return verb;
    }
    public String getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        return createActionString(verb, modifier);
    }
    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof Action)) return false;
        Action action = (Action) toCompare;
        return verb.equals(action.getVerb()) && modifier.equals(action.getModifier());
    }
}

