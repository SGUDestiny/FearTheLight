package destiny.fearthelight.server.entities;

import destiny.fearthelight.server.entities.goals.CustomMeleeAttackGoal;
import destiny.fearthelight.server.registry.EntityRegistry;
import destiny.fearthelight.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RibberEntity extends Monster implements GeoEntity {
    public final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation MOVE_ANIM = RawAnimation.begin().thenLoop("move");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    public RibberEntity(EntityType<? extends RibberEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6)
                .add(Attributes.ATTACK_DAMAGE, 2.0f)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1f)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25f).build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.0D, false, 1.5, 40));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
            target -> target.getType().getCategory() != EntityRegistry.MOLTEN_FLESH));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("attackAnimController", "attack");
        return super.doHurtTarget(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundRegistry.FLESH.get(), 0.25f, 0.5f);
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.playSound(SoundRegistry.FLESH_HIT.get(), 1f, 0.5f);
    }
    
    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
        this.playSound(SoundRegistry.FLESH_DIE.get(), 0.75f, 0.5f);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walkAnimController", 5, this::walkAnimController));
        controllers.add(new AnimationController<GeoAnimatable>(this, "attackAnimController", 0, state -> PlayState.CONTINUE)
            .triggerableAnim("attack", ATTACK_ANIM)
            .setSoundKeyframeHandler(event -> {
                Player player = Minecraft.getInstance().player;

                if (player != null)
                    player.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 0.1f, 1.25f);
            })
        );
    }

    protected <E extends RibberEntity> PlayState walkAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(MOVE_ANIM);

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
