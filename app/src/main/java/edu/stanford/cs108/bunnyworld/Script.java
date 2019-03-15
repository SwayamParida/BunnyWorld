package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Script implements BunnyWorldConstants {
    private boolean onClick, onEnter, onDrop;
    private List<Action> onClickActions, onEnterActions;
    private Map<String, List<Action>> onDropMap;

    public Script() {
        onClickActions = new ArrayList<>();
        onDropMap = new HashMap<>();
        onEnterActions = new ArrayList<>();
    }

    public List<Action> getOnClickActions() {
        return onClickActions;
    }
    public Map<String, List<Action>> getOnDropMap() {
        return onDropMap;
    }
    public List<Action> getOnEnterActions() {
        return onEnterActions;
    }
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        actions.addAll(onClickActions);
        actions.addAll(onEnterActions);

        Collection<List<Action>> onDropActionsLists = onDropMap.values();
        List<Action> onDropActions = new ArrayList<>();
        onDropActionsLists.forEach(onDropActions::addAll);
        actions.addAll(onDropActions);

        return actions;
    }

    public void addAction(String event, String shape, Action action) {
        switch (event) {
            case "onClick": addOnClickAction(action); break;
            case "onDrop": addOnDropAction(shape, action); break;
            case "onEnter": addOnEnterAction(action); break;
        }
    }
    public void addOnClickAction(Action onClickAction) {
        onClick = true;
        onClickActions.add(onClickAction);
    }
    public void addOnDropAction(String shape, Action onDropAction) {
        onDrop = true;
        List<Action> shapeOnDropActions = onDropMap.getOrDefault(shape, new ArrayList<>());
        shapeOnDropActions.add(onDropAction);
        onDropMap.put(shape, shapeOnDropActions);
    }
    public void addOnEnterAction(Action onEnterAction) {
        onEnter = true;
        onEnterActions.add(onEnterAction);
    }
    public void addOnClickAction(List<Action> onClickAction) {
        onClick = true;
        onClickActions.addAll(onClickAction);
    }
    public void addOnDropAction(Map<String, List<Action>> onDropActions) {
        onDrop = true;
        for (String shape : onDropActions.keySet()) {
            List<Action> actions = onDropActions.get(shape);
            actions.forEach(action -> addOnDropAction(shape, action));
        }
    }
    public void addOnEnterAction(List<Action> onEnterAction) {
        onEnter = true;
        onEnterActions.addAll(onEnterAction);
    }

    public static Script parseScript(String scriptString) {
        Script script = new Script();

        if (scriptString == null || scriptString.isEmpty()) return script;

        ArrayList<String> triggers = new ArrayList<>();
        Scanner triggerScanner = new Scanner(scriptString);
        triggerScanner.useDelimiter(TRIGGER_DELIMITER);
        while (triggerScanner.hasNext())
            triggers.add(triggerScanner.next());
        triggerScanner.close();

        for (String trigger : triggers) {
            trigger = trigger + EVENT_ACTION_DELIMITER;
            Scanner eventScanner = new Scanner(trigger);
            eventScanner.useDelimiter(EVENT_ACTION_DELIMITER);
            String event = eventScanner.next();

            List<Action> actions;
            switch (event) {
                case "onClick":
                    actions = Action.parseActionList(eventScanner.next());
                    script.addOnClickAction(actions);
                    break;
                case "onDrop":
                    Map<String, List<Action>> onDropActions = parseOnDropActionList(eventScanner.next());
                    script.addOnDropAction(onDropActions);
                    break;
                case "onEnter":
                    actions = Action.parseActionList(eventScanner.next());
                    script.addOnEnterAction(actions);
                    break;
            }
        }
        return script;
    }

    private static Map<String, List<Action>> parseOnDropActionList(String onDropActionsList) {
        Scanner onDropScanner = new Scanner(onDropActionsList);
        onDropScanner.useDelimiter(SHAPE_ACTION_DELIMITER);

        Map<String, List<Action>> onDropActions = new HashMap<>();
        while (onDropScanner.hasNext()) {
            String shape = onDropScanner.next();
            List<Action> actions = Action.parseActionList(onDropScanner.next());
            onDropActions.put(shape, actions);
        }

        return onDropActions;
    }

    @Override
    public String toString() {
        StringBuilder scriptBuilder = new StringBuilder();
        add(onClick, TRIGGER_EVENTS[0], onClickActions, scriptBuilder);
        add(onDrop, TRIGGER_EVENTS[1], onDropMap, scriptBuilder);
        add(onEnter, TRIGGER_EVENTS[2], onEnterActions, scriptBuilder);
        return scriptBuilder.toString();
    }

    private void add(boolean shouldAdd, String triggerLabel, List<Action> toAdd, StringBuilder stringBuilder) {
        if (!shouldAdd) return;

        stringBuilder.append(triggerLabel);
        stringBuilder.append(EVENT_ACTION_DELIMITER);
        toAdd.forEach(action -> stringBuilder.append(action.toString()));
        stringBuilder.append(TRIGGER_DELIMITER);
    }

    private void add(boolean shouldAdd, String triggerLabel, Map<String, List<Action>> toAdd, StringBuilder stringBuilder) {
        if (!shouldAdd) return;

        stringBuilder.append(triggerLabel);
        stringBuilder.append(EVENT_ACTION_DELIMITER);
        for (String shape : toAdd.keySet()) {
            stringBuilder.append(shape);
            stringBuilder.append(SHAPE_ACTION_DELIMITER);
            List<Action> actionList = toAdd.get(shape);
            actionList.forEach(action -> stringBuilder.append(action.toString()));
            stringBuilder.append(SHAPE_ACTION_DELIMITER);
        }
        stringBuilder.append(TRIGGER_DELIMITER);
    }

    public static void main(String[] args) {
        Script script = new Script();
        script.addOnClickAction(Action.parseAction("hide Shape1;"));
        script.addOnDropAction("Shape1", Action.parseAction("play woof;"));
        script.addOnDropAction("Shape1", Action.parseAction("goto Page2;"));
        script.addOnDropAction("Shape2", Action.parseAction("hide Shape5;"));
        script.addOnEnterAction(Action.parseAction("show Shape3;"));
        System.out.println(script);

        Script parsedScript = Script.parseScript(script.toString());
        System.out.println("Parsed script:\n" + parsedScript.toString());
    }
}
