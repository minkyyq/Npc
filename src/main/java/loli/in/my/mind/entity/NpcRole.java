package loli.in.my.mind.entity;

public enum NpcRole {
    FIRST(0, "первый"),
    SECOND(1, "второй");

    private final int networkId;
    private final String displayName;

    NpcRole(int networkId, String displayName) {
        this.networkId = networkId;
        this.displayName = displayName;
    }

    public int networkId() {
        return networkId;
    }

    public String displayName() {
        return displayName;
    }

    public static NpcRole byNetworkId(int id) {
        for (NpcRole role : values()) {
            if (role.networkId == id) {
                return role;
            }
        }

        return FIRST;
    }
}
