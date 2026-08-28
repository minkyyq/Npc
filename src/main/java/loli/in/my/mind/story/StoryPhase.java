package loli.in.my.mind.story;

public enum StoryPhase {
    NOT_STARTED,
    SPAWNED,
    APPROACHING,
    READY_FOR_DIALOGUE,
    DIALOGUE,
    COMPLETE;

    public static StoryPhase byId(int id) {
        StoryPhase[] phases = values();
        return id >= 0 && id < phases.length ? phases[id] : NOT_STARTED;
    }
}
