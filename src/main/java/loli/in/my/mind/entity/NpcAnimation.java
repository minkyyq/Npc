package loli.in.my.mind.entity;

import software.bernie.geckolib.core.animation.RawAnimation;

public enum NpcAnimation {
    MAIN(0, RawAnimation.begin().thenLoop("main")),
    WALK(1, RawAnimation.begin().thenLoop("walking")),
    DISTRUST(2, RawAnimation.begin().thenPlayAndHold("distrust")),
    CORRECTION(3, RawAnimation.begin().thenPlayAndHold("correction")),
    UNCORRECTION(4, RawAnimation.begin().thenPlayAndHold("uncorrection")),
    RIGHT(5, RawAnimation.begin().thenPlayAndHold("right"));

    private final int networkId;
    private final RawAnimation rawAnimation;

    NpcAnimation(int networkId, RawAnimation rawAnimation) {
        this.networkId = networkId;
        this.rawAnimation = rawAnimation;
    }

    public int networkId() {
        return networkId;
    }

    public RawAnimation rawAnimation() {
        return rawAnimation;
    }

    public static NpcAnimation byNetworkId(int id) {
        for (NpcAnimation animation : values()) {
            if (animation.networkId == id) {
                return animation;
            }
        }

        return MAIN;
    }
}
