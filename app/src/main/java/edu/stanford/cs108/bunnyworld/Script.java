package edu.stanford.cs108.bunnyworld;

import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class Script implements BunnyWorldConstants {
    private boolean onClick, onEnter, onDrop;
    private List<Action> onClickActions, onEnterActions, onDropActions;

    public Script(boolean onClick, boolean onEnter, boolean onDrop,
                  List<Action> onClickActions, List<Action> onDropActions, List<Action> onEnterActions) {
        this.onClick = onClick;
        this.onEnter = onEnter;
        this.onDrop = onDrop;
        this.onClickActions = onClickActions;
        this.onDropActions = onDropActions;
        this.onEnterActions = onEnterActions;
    }

    public static Script parseScript(String scriptString) {
        return null; //TODO: Figure out how to use RegEx to parse String
    }

    @Override
    public String toString() {
        StringBuilder scriptBuilder = new StringBuilder();
        add(onClick, triggerEvents[0], onClickActions, scriptBuilder);
        add(onDrop, triggerEvents[1], onDropActions, scriptBuilder);
        add(onEnter, triggerEvents[2], onEnterActions, scriptBuilder);
        return scriptBuilder.toString();
    }
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<>();
        actions.addAll(onClickActions);
        actions.addAll(onDropActions);
        actions.addAll(onEnterActions);
        return actions;
    }

    private void add(boolean shouldAdd, String actionLabel, Object toAdd, StringBuilder stringBuilder) {
        if (!shouldAdd) return;

        stringBuilder.append(actionLabel);
        stringBuilder.append(VERB_MODIFIER_DELIMITER);
        stringBuilder.append(toAdd.toString());
        stringBuilder.append(ACTION_DELIMITER);
    }
    private void add(boolean shouldAdd, String triggerLabel, List<Action> toAdd, StringBuilder stringBuilder) {
        if (!shouldAdd) return;

        stringBuilder.append(triggerLabel);
        stringBuilder.append(EVENT_ACTION_DELIMITER);
        toAdd.forEach(action -> stringBuilder.append(action));
        stringBuilder.append(TRIGGER_DELIMITER);
    }

    public class Action {
        private boolean goTo, play, hide, show;
        private Page gotoPage;
        private MediaPlayer audio;
        private Shape hideShape, showShape;

        public Action(boolean goTo, boolean play, boolean hide, boolean show,
                      Page gotoPage, MediaPlayer audio, Shape hideShape, Shape showShape) {
            this.goTo = goTo;
            this.play = play;
            this.hide = hide;
            this.show = show;

            this.gotoPage = gotoPage;
            this.audio = audio;
            this.hideShape = hideShape;
            this.showShape = showShape;
        }

        @Override
        public String toString() {
            StringBuilder scriptBuilder = new StringBuilder();
            add(goTo, actionVerbs[0], gotoPage, scriptBuilder);
            add(play, actionVerbs[1], audio, scriptBuilder);
            add(hide, actionVerbs[2], hideShape, scriptBuilder);
            add(show, actionVerbs[3], showShape, scriptBuilder);
            return scriptBuilder.toString();
        }
    }

}
