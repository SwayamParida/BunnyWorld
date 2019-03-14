package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Action implements BunnyWorldConstants {
    private String verb, modifier;

    public static Action parseAction(String actionString) {
        if (actionString == null || actionString.isEmpty()) return null;
        Scanner verbScanner = new Scanner(actionString);
        verbScanner.useDelimiter(VERB_MODIFIER_DELIMITER);
        String verb = verbScanner.next();
        String modifier = verbScanner.next();
        verbScanner.close();

        Scanner modifierScanner = new Scanner(modifier);
        modifierScanner.useDelimiter(ACTION_DELIMITER);
        modifier = modifierScanner.next();
        modifierScanner.close();

        return new Action(verb, modifier);
    }
    public static List<Action> parseActionList(String actionListString) {
        Scanner listScanner = new Scanner(actionListString);
        listScanner.useDelimiter(ACTION_DELIMITER);

        List<String> actionStrings = new ArrayList<>();
        while (listScanner.hasNext())
            actionStrings.add(listScanner.next());
        listScanner.close();

        List<Action> actions = new ArrayList<>();
        actionStrings.forEach(actionString -> actions.add(parseAction(actionString)));
        return actions;
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

