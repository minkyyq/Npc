package loli.in.my.mind.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class StoryNpcEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_ID =
            SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROLE_ID =
            SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public StoryNpcEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANIMATION_ID, NpcAnimation.MAIN.networkId());
        entityData.define(ROLE_ID, NpcRole.FIRST.networkId());
    }

    public NpcAnimation getNpcAnimation() {
        return NpcAnimation.byNetworkId(entityData.get(ANIMATION_ID));
    }

    public void setNpcAnimation(NpcAnimation animation) {
        entityData.set(ANIMATION_ID, animation.networkId());
    }

    public NpcRole getNpcRole() {
        return NpcRole.byNetworkId(entityData.get(ROLE_ID));
    }

    public void setNpcRole(NpcRole role) {
        entityData.set(ROLE_ID, role.networkId());
        setCustomName(Component.literal(role.displayName()));
        setCustomNameVisible(true);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StoryAnimation", getNpcAnimation().networkId());
        tag.putInt("StoryRole", getNpcRole().networkId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNpcAnimation(NpcAnimation.byNetworkId(tag.getInt("StoryAnimation")));
        setNpcRole(NpcRole.byNetworkId(tag.getInt("StoryRole")));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "story_controller", 2, this::selectAnimation));
    }

    private PlayState selectAnimation(AnimationState<StoryNpcEntity> state) {
        state.getController().setAnimation(getNpcAnimation().rawAnimation());
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
