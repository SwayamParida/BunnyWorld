package edu.stanford.cs108.bunnyworld;

import android.media.MediaPlayer;

import java.util.List;

public class Script {
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

    @Override
    public String toString() {
        StringBuilder scriptBuilder = new StringBuilder();
        addIf(onClick, onClickActions, scriptBuilder);
        addIf(onDrop, onDropActions, scriptBuilder);
        addIf(onEnter, onEnterActions, scriptBuilder);
        return scriptBuilder.toString();
    }

    private void addIf (boolean shouldAdd, Object toAdd, StringBuilder stringBuilder) {
        if (shouldAdd)
            stringBuilder.append(toAdd);
    }
    private void addIf(boolean shouldAdd, List toAdd, StringBuilder stringBuilder) {
        toAdd.forEach(elem -> addIf(shouldAdd, elem, stringBuilder));
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
            addIf(goTo, gotoPage, scriptBuilder);
            addIf(play, audio, scriptBuilder);
            addIf(hide, hideShape, scriptBuilder);
            return scriptBuilder.toString();
        }
    }

}
