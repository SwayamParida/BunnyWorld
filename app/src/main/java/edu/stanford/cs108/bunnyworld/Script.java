package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Script implements BunnyWorldConstants {
    private boolean onClick, onEnter, onDrop;
    private Shape onDropShape;
    private List<Action> onClickActions, onEnterActions, onDropActions;

    public Script() {
        onClickActions = new ArrayList<>();
        onDropActions = new ArrayList<>();
        onEnterActions = new ArrayList<>();
    }

    public List<Action> getOnClickActions() {
        return onClickActions;
    }
    public List<Action> getOnDropActions() {
        return onDropActions;
    }
    public List<Action> getOnEnterActions() {
        return onEnterActions;
    }
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        actions.addAll(onClickActions);
        actions.addAll(onDropActions);
        actions.addAll(onEnterActions);
        return actions;
    }

    public void addAction(String event, Action action) {
        switch (event) {
            case "onClick": addOnClickAction(action); break;
            case "onDrop": addOnDropAction(action); break;
            case "onEnter": addOnEnterAction(action); break;
        }
    }
    public void addOnClickAction(Action onClickAction) {
        onClick = true;
        onClickActions.add(onClickAction);
    }
    public void addOnDropAction(Action onDropAction) {
        onDrop = true;
        onDropActions.add(onDropAction);
    }
    public void addOnEnterAction(Action onEnterAction) {
        onEnter = true;
        onEnterActions.add(onEnterAction);
    }
    public void addOnClickAction(List<Action> onClickAction) {
        onClick = true;
        onClickActions.addAll(onClickAction);
    }
    public void addOnDropAction(List<Action> onDropAction) {
        onDrop = true;
        onDropActions.addAll(onDropAction);
    }
    public void addOnEnterAction(List<Action> onEnterAction) {
        onEnter = true;
        onEnterActions.addAll(onEnterAction);
    }

    public void setOnDropShape(Shape onDropShape) {
        this.onDropShape = onDropShape;
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
            List<Action> actions = Action.parseActionList(eventScanner.next());

            switch (event) {
                case "onClick":
                    script.addOnClickAction(actions);
                    break;
                case "onDrop":
                    script.addOnDropAction(actions);
                    break;
                case "onEnter":
                    script.addOnEnterAction(actions);
                    break;
            }
        }
        return script;
    }

    @Override
    public String toString() {
        StringBuilder scriptBuilder = new StringBuilder();
        add(onClick, TRIGGER_EVENTS[0], onClickActions, scriptBuilder);
        add(onDrop, TRIGGER_EVENTS[1], onDropActions, scriptBuilder);
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
}
