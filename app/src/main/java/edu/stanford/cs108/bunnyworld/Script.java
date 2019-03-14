package edu.stanford.cs108.bunnyworld;

import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class Script implements BunnyWorldConstants {
    private boolean onClick, onEnter, onDrop;
    private List<Action> onClickActions, onEnterActions, onDropActions;

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
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        actions.addAll(onClickActions);
        actions.addAll(onDropActions);
        actions.addAll(onEnterActions);
        return actions;
    }

    public static Script parseScript(String scriptString) {
        return null; //TODO: Figure out how to use RegEx to parse String
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
