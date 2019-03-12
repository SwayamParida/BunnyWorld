package edu.stanford.cs108.bunnyworld;

import android.media.MediaPlayer;

import java.util.List;

public class Script {
    private boolean onClick, onEnter, onDrop;
    private List<Action> onClickActions, onEnterActions, onDropActions;

    private final static String VERB_MODIFIER_DELIMITER = " ";
    private final static String ACTION_DELIMITER = "; ";
    private final static String TRIGGER_DELIMITER = "}\n ";
    private final static String regex = "trigger: { (verb modifier; )*}\n";

    public Script(boolean onClick, boolean onEnter, boolean onDrop,
                  List<Action> onClickActions, List<Action> onDropActions, List<Action> onEnterActions) {
        this.onClick = onClick;
        this.onEnter = onEnter;
        this.onDrop = onDrop;
        this.onClickActions = onClickActions;
        this.onDropActions = onDropActions;
        this.onEnterActions = onEnterActions;
    }

    public Script parseScript(String scriptString) {
        return null; //TODO: Figure out how to use RegEx to parse String
    }

    @Override
    public String toString() {
        StringBuilder scriptBuilder = new StringBuilder();
        add(onClick, "onClick: { ", onClickActions, scriptBuilder);
        add(onDrop, "onDrop: { ", onDropActions, scriptBuilder);
        add(onEnter, "onEnter: { ", onEnterActions, scriptBuilder);
        return scriptBuilder.toString();
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
            add(goTo, "goto ", gotoPage, scriptBuilder);
            add(play, "play ", audio, scriptBuilder);
            add(hide, "hide ", hideShape, scriptBuilder);
            add(show, "show ", showShape, scriptBuilder);
            return scriptBuilder.toString();
        }
    }

}
