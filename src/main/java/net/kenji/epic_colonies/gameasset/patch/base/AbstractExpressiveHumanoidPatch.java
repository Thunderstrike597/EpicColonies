package net.kenji.epic_colonies.gameasset.patch.base;

import com.example.minecoloniesmages.entity.ai.guard.MageCombatAI;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.core.entity.ai.combat.AttackMoveAI;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.items.ItemChiefSword;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.kenji.epic_colonies.EpicColonies;
import net.kenji.epic_colonies.api.CitizenPatchData;
import net.kenji.epic_colonies.api.handlers.CitizenGuardHandler;
import net.kenji.epic_colonies.api.managers.BlinkManager;
import net.kenji.epic_colonies.gameasset.EpicColoniesStyles;
import net.kenji.epic_colonies.gameasset.behaviours.CombatBehaviourBase;
import net.kenji.epic_colonies.gameasset.EpicColoniesAnimations;
import net.kenji.epic_colonies.gameasset.EpicColoniesLivingMotions;
import net.kenji.epic_colonies.network.EpicColoniesPacketHandler;
import net.kenji.epic_colonies.network.ServerBowActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.jline.utils.Log;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractExpressiveHumanoidPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {

    private static final ResourceLocation[] EFM_WEAPON_MODIFIER_IDS = {
            EpicFightAttributes.ARMOR_NEGATION_MODIFIER,
            EpicFightAttributes.MAX_STRIKE_MODIFIER,
            EpicFightAttributes.IMPACT_MODIFIER,
            EpicFightAttributes.ATTACK_SPEED_MODIFIER
    };

    protected final CitizenGuardHandler guard = new CitizenGuardHandler(this);
    public CitizenGuardHandler getGuard() { return this.guard; }


    protected static int MAX_COMPOSITE_PLAY_COUNTER = 20;
    protected int compositeAnimPlayCounter = MAX_COMPOSITE_PLAY_COUNTER;
    protected static AnimationManager.AnimationAccessor<? extends StaticAnimation> DEFAULT_BROW_ANIM = null;
    protected static AnimationManager.AnimationAccessor<? extends StaticAnimation> DEFAULT_EYES_ANIM = null;
    protected CitizenPatchData citizenPatchData = new CitizenPatchData();

    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> facialAnim;
    protected AnimationManager.AnimationAccessor<? extends StaticAnimation> eyeMoveAnim;

    protected boolean wasUsingBow = false;
    protected boolean didJump = false;

    protected int bowUseCounter = 0;
    protected int lastYTickCount = 0;
    protected double lastY = -1;
    protected ItemStack lastHeldItem = ItemStack.EMPTY;
    protected ItemStack lastOffhandItem = ItemStack.EMPTY;
    protected static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };
    protected final ItemStack[] lastArmorItems = { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };

    protected static float LOOK_RANGE = 8.0F;
    protected static float FADE_DISTANCE = 2.0F;
    protected static float EYE_ANGLE_SENSITIVITY_DEG = 88F;        // how far off-center = full eye deflection (unchanged)
    protected static float MAX_TRACKING_ANGLE_DEG = 60;  // beyond this angle, citizen can't plausibly "see" the player - eyes go centered
    protected static float ANGLE_FADE_DEG = 15F;
    protected static float MAX_EYE_OFFSET = 0.075F;
    protected static float HEAD_TURN_SPEED_DEG = 8.0F;

    private boolean closeRangeStance;

    public void debugLogNearestPlayer(String log){
        Player player = this.getOriginal().level().getNearestPlayer(getOriginal(), 2.0F);

        if(player != null){
            Log.info(log);
        }
    }
    @Override
    public AttackResult tryHurt(DamageSource source, float amount) {
        if (!this.isLogicalClient()) {
            AttackResult blocked = this.guard.tryBlock(source, amount);
            if (blocked != null) return blocked;
        }
        return super.tryHurt(source, amount);
    }
    @Override
    public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            clearOffhandWeaponModifiers(from, to);
        }

        super.updateHeldItem(fromCap, toCap, from, to, hand);

        if (hand == InteractionHand.OFF_HAND) {
            AttributeInstance negation = this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION);
            if (negation != null) {
                negation.removeModifier(EpicFightAttributes.ATTACK_SPEED_MODIFIER); // misplaced by Epic Fight
            }
        }
    }

    private void clearOffhandWeaponModifiers(ItemStack from, ItemStack to) {
        List<Holder<Attribute>> offhandAttributes = List.of(
                EpicFightAttributes.OFFHAND_ATTACK_SPEED,
                EpicFightAttributes.OFFHAND_ARMOR_NEGATION,
                EpicFightAttributes.OFFHAND_IMPACT,
                EpicFightAttributes.OFFHAND_MAX_STRIKES
        );

        for (Holder<Attribute> attribute : offhandAttributes) {
            AttributeInstance instance = this.getOriginal().getAttribute(attribute);
            if (instance == null) continue;
            for (ResourceLocation id : EFM_WEAPON_MODIFIER_IDS) {
                instance.removeModifier(id); // no-op if absent
            }
        }

        // Vanilla attack speed goes onto OFFHAND_ATTACK_SPEED via raw addTransientModifier too
        AttributeInstance offhandSpeed = this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED);
        if (offhandSpeed != null) {
            for (ItemStack stack : new ItemStack[]{ from, to }) {
                if (!stack.isEmpty()) {
                    stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                        if (attribute == Attributes.ATTACK_SPEED) {
                            offhandSpeed.removeModifier(modifier.id());
                        }
                    });
                }
            }
        }
    }
    public void debugCycleOffhand(ItemStack weapon) {
        CapabilityItem emptyCap = EpicFightCapabilities.getItemStackCapability(ItemStack.EMPTY);
        CapabilityItem weaponCap = EpicFightCapabilities.getItemStackCapability(weapon);

        for (int i = 0; i < 3; i++) {
            updateHeldItem(emptyCap, weaponCap, ItemStack.EMPTY, weapon, InteractionHand.OFF_HAND);
            updateHeldItem(weaponCap, emptyCap, weapon, ItemStack.EMPTY, InteractionHand.OFF_HAND);
        }

        AttributeInstance negation = this.getOriginal().getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION);
        EpicColonies.LOGGER.info("[debugCycleOffhand] OFFHAND_ARMOR_NEGATION after cycling = {} (base {})",
                negation.getValue(), negation.getBaseValue());
    }
    protected Player getNearestPlayer(Entity entity){
        Player nearest = entity.level().getNearestPlayer(entity, LOOK_RANGE);

        if(nearest != null && !nearest.isSpectator() && !nearest.isInvisible()){
            if(this.getTarget() == null)
                return nearest;
        }
        return null;
    }
    public AbstractExpressiveHumanoidPatch(T entity) {
        super(entity, Factions.VILLAGER);
    }

    public float getJogSpeed(){
        return 0.08F;
    }

    public boolean isWasUsingBow(){
        return wasUsingBow;
    }

    public double getLastY() {
        return lastY;
    }

    public boolean actuallyOnGround(){
        Vec3 pos = this.getOriginal().position();
        double offset = -0.1;
        int xPos = Mth.floor(pos.x());
        int yPos = Mth.floor(pos.y() + offset);
        int zPos = Mth.floor(pos.z());
        BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
        BlockState state = this.getOriginal().level().getBlockState(blockPos);
        return state.isSolidRender(this.getOriginal().level(), blockPos) && !state.canBeReplaced();
    }
    public void setWasUsingBow(boolean value){
        if(this.getCurrentLivingMotion() == EpicColoniesLivingMotions.JOG) return;
        this.wasUsingBow = value;
        this.bowUseCounter = 0;
        if(this.getOriginal().level().isClientSide()) {

            EpicColoniesPacketHandler.sendToServer(new ServerBowActionPacket(this.getOriginal().getUUID(), this.wasUsingBow));
        }
    }
    public List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> getBlinkAnimations(){
        return List.of(EpicColoniesAnimations.CITIZEN_BLINK);
    }
    public boolean shouldRun() {
        return this.getTarget() != null;
    }

    public boolean shouldJogWithAnim() {
        return (getCurrentForwardSpeed() > getJogSpeed());
    }

    public double getCurrentForwardSpeed() {
        Vec3 movement = this.getOriginal().getDeltaMovement();
        Vec3 forward = this.getOriginal().getForward();
        return movement.dot(forward);
    }

    public float getAnimForwardSpeed(float minSpeed, float maxSpeed) {

        double forwardSpeed = getCurrentForwardSpeed();

        // Get the horse's actual max walk speed attribute
        double maxWalkSpeed = this.getOriginal().getAttributeValue(Attributes.MOVEMENT_SPEED);

        // Normalize: 0.0 when still, 1.0 at full walk speed
        double normalized = Math.min(forwardSpeed / maxWalkSpeed, 1.0);

        // Scale between your known good minimum and maximum playback speed
        return (float)(minSpeed + normalized * (maxSpeed - minSpeed));
    }

    public CitizenPatchData getCitizenPatchData(){
        return this.citizenPatchData;
    }

    public void setCitizenPatchData(CitizenPatchData citizenPatchData) {
        this.citizenPatchData = citizenPatchData;
    }

    public void setCloseRangeStance(boolean value) {
        if (this.closeRangeStance == value) return;   // only refresh on transitions
        this.closeRangeStance = value;

        if (!this.original.level().isClientSide()) {
            this.initAI();                               // rebuilds AnimatedAttackGoal from the new builder
            this.modifyLivingMotionByCurrentItem(false); // re-applies + syncs living motions
        }
    }

    private boolean useCloseRangeStyle() {
        return this.closeRangeStance
                && this.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() == CapabilityItem.WeaponCategories.DAGGER;
    }
    public boolean shouldCancelVanillaTargeting(){
        if(this.getOriginal().getMainHandItem().getItem() instanceof PotionItem)
            return true;

        if(this.getOriginal().getMainHandItem().getItem() instanceof ProjectileWeaponItem) {
            return true;
        }
        if(EpicColonies.isMinecoloniesMagesLoaded()){
            if(this.getOriginal().getMainHandItem().getItem() instanceof SpellBook)
                return true;
        }

        return false;
    }

    public boolean isRangerMage(AttackMoveAI<EntityCitizen> combatAI){
        if(EpicColonies.isMinecoloniesMagesLoaded()){
            return combatAI instanceof MageCombatAI;
        }
        return false;
    }

    @Override
    protected CombatBehaviors.Builder<HumanoidMobPatch<?>> getHoldingItemWeaponMotionBuilder() {
        if (this.useCloseRangeStyle()) {
            var byStyle = this.weaponAttackMotions.get(CapabilityItem.WeaponCategories.DAGGER);
            if (byStyle != null && byStyle.containsKey(EpicColoniesStyles.ONE_HAND_CLOSE_RANGE)) {
                return byStyle.get(EpicColoniesStyles.ONE_HAND_CLOSE_RANGE);

            }
        }
        return super.getHoldingItemWeaponMotionBuilder();
    }

    @Override
    protected void setWeaponMotions() {
        super.setWeaponMotions();
        super.setWeaponMotions();

        Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>> livingByCategory = new HashMap<>();
        Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> attackByCategory = new HashMap<>();

        for (CombatBehaviourBase.WeaponMotionDetails details : CombatBehaviourBase.behaviourList) {
            for (CombatBehaviourBase.WeaponMotions motions : details.motions()) {
                Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> livingMotionSet = Set.of(
                        Pair.of(LivingMotions.IDLE, motions.idleMotion()),
                        Pair.of(LivingMotions.WALK, motions.walkMotion()),
                        Pair.of(EpicColoniesLivingMotions.JOG, motions.jogMotion()),
                        Pair.of(LivingMotions.CHASE, motions.runMotion())
                );

                livingByCategory
                        .computeIfAbsent(details.category(), c -> new HashMap<>())
                        .put(motions.style(), livingMotionSet); // later registration for the same (category, style) wins

                attackByCategory
                        .computeIfAbsent(details.category(), c -> new HashMap<>())
                        .put(motions.style(), motions.behaviour());
            }
        }

        livingByCategory.forEach((category, byStyle) -> this.weaponLivingMotions.put(category, ImmutableMap.copyOf(byStyle)));
        attackByCategory.forEach((category, byStyle) -> this.weaponAttackMotions.put(category, ImmutableMap.copyOf(byStyle)));
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        if(this.getOriginal().getMainHandItem().getItem() instanceof ProjectileWeaponItem)
            super.commonAggressiveRangedMobUpdateMotion(considerInaction);
        else super.commonMobUpdateMotion(considerInaction);
    }

    @Override
    public void preTick() {
        super.preTick();
        BlinkManager.manageBlinking(this.getOriginal());
        if(needsManualEquipmentTracking())
            tickEquipmentChanges();
        if(lastY == -1 || lastYTickCount <= 0){
            lastY = this.getOriginal().position().y();
            lastYTickCount = 5;
        }
        else {
            lastYTickCount--;
        }
    }

    /**
     * MineColonies' AbstractEntityCitizen (citizens AND visitors) overrides LivingEntity#detectEquipmentUpdates()
     * without calling super, so vanilla never applies held-item attribute modifiers / enchantment location effects
     * and never fires LivingEquipmentChangeEvent for them. Epic Fight relies on both:
     *  - MobPatch#attack -> Mob#doHurtTarget reads ATTACK_DAMAGE (weapon damage was never added)
     *  - VanillaEntityEventHooks#onEquipmentChanged (via NeoForgeEntityEvent) applies weapon/armor capability
     *    modifiers (damage bonus, armor negation, impact, max strikes, weight, stun armor) and stun reduction.
     * Other MineColonies entities (mercenaries, raiders) go through vanilla normally, so they must NOT run this,
     * otherwise every equipment change would be applied/fired twice.
     */
    protected boolean needsManualEquipmentTracking(){
        return this.getOriginal() instanceof AbstractEntityCitizen;
    }

    protected void tickEquipmentChanges(){
        T entity = this.getOriginal();

        // Server only. Epic Fight's updateHeldItem -> modifyLivingMotionByCurrentItem sends SPChangeLivingMotion,
        // which throws on the client. The server already syncs the new living motions to clients through it.
        if(entity.level().isClientSide()) return;

        ItemStack mainStack = entity.getMainHandItem();
        if(entity.equipmentHasChanged(lastHeldItem, mainStack)){
            onHandItemChanged(EquipmentSlot.MAINHAND, InteractionHand.MAIN_HAND, lastHeldItem, mainStack);
            lastHeldItem = mainStack.copy();
        }

        ItemStack offStack = entity.getOffhandItem();
        if(entity.equipmentHasChanged(lastOffhandItem, offStack)){
            onHandItemChanged(EquipmentSlot.OFFHAND, InteractionHand.OFF_HAND, lastOffhandItem, offStack);
            lastOffhandItem = offStack.copy();
        }

        // Armor: attributes are server-authoritative, and only the event is needed here - MineColonies already
        // applies vanilla armor modifiers + enchantment effects itself (AbstractEntityCitizen#onArmorAdd / onArmorRemove).
        if(!entity.level().isClientSide()){
            for(int i = 0; i < ARMOR_SLOTS.length; i++){
                EquipmentSlot slot = ARMOR_SLOTS[i];
                ItemStack current = entity.getItemBySlot(slot);
                if(entity.equipmentHasChanged(lastArmorItems[i], current)){
                    NeoForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(entity, slot, lastArmorItems[i], current));
                    lastArmorItems[i] = current.copy();
                }
            }
        }
    }

    private void onHandItemChanged(EquipmentSlot slot, InteractionHand hand, ItemStack from, ItemStack to){
        T entity = this.getOriginal();


        // Mirrors vanilla 1.21.1 LivingEntity#collectEquipmentChanges for this slot
        AttributeMap attributes = entity.getAttributes();

        if(!from.isEmpty()){
            from.forEachModifier(slot, (attributeHolder, modifier) -> {
                AttributeInstance instance = attributes.getInstance(attributeHolder);
                if(instance != null)
                    instance.removeModifier(modifier);
            });
            EnchantmentHelper.stopLocationBasedEffects(from, entity, slot);
        }

        if(!to.isEmpty()){
            to.forEachModifier(slot, (attributeHolder, modifier) -> {
                AttributeInstance instance = attributes.getInstance(attributeHolder);
                if(instance != null){
                    instance.removeModifier(modifier.id());
                    instance.addTransientModifier(modifier);
                }
            });
            if(entity.level() instanceof ServerLevel serverLevel)
                EnchantmentHelper.runLocationChangedEffects(serverLevel, to, entity, slot);
        }

        // Epic Fight's equipment hook applies weapon capability modifiers and calls updateHeldItem() itself
        NeoForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(entity, slot, from, to));
    }

    public int getCitizenItemSlotOfWeaponCategory(WeaponCategory category){
        if(!(this.getOriginal() instanceof AbstractEntityCitizen citizen))return -1;
        InventoryCitizen inventoryCitizen = citizen.getInventoryCitizen();
        for(int i = 0; i < inventoryCitizen.getSlots(); i++){
            if(i < inventoryCitizen.getSlots()){
                ItemStack stack = inventoryCitizen.getStackInSlot(i);
                if(stack.isEmpty())continue;
                CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
                if(cap == null) continue;
                if(cap.getWeaponCategory() == category){
                    return i;
                }
            }
        }
        return -1;
    }
    public ItemStack getCitizenItemOfWeaponCategory(WeaponCategory category){
        if(!(this.getOriginal() instanceof AbstractEntityCitizen citizen))return ItemStack.EMPTY;
        InventoryCitizen inventoryCitizen = citizen.getInventoryCitizen();
        for(int i = 0; i < inventoryCitizen.getSlots(); i++){
            if(i < inventoryCitizen.getSlots()){
                ItemStack stack = inventoryCitizen.getStackInSlot(i);
                if(stack.isEmpty())continue;
                CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
                if(cap == null) continue;
                if(cap.getWeaponCategory() == category){
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    @Override
    public void preTickServer() {
        super.preTickServer();
        if(wasUsingBow && this.getCurrentLivingMotion() != EpicColoniesLivingMotions.JOG){
            bowUseCounter++;
        }
        else{
            bowUseCounter = 0;
        }
    }
    @Override
    public void setAIAsInfantry(boolean holdingRangedWeapon) {
        if(!holdingRangedWeapon){
            super.setAIAsInfantry(false);
        }
    }
    @Override
    public float getImpact(InteractionHand hand) {
        return 1.5F;
    }
    @Override
    public void initAnimator(Animator animator) {
        // All available living motions are listed in this enum: https://github.com/Epic-Fight/epicfight/blob/1.21.1/src/main/java/yesman/epicfight/api/animation/LivingMotions.java#L4-L6
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.EAT, EpicColoniesAnimations.CITIZEN_EAT);
        animator.addLivingAnimation(LivingMotions.CLIMB, EpicColoniesAnimations.CITIZEN_CLIMB);
        animator.addLivingAnimation(LivingMotions.DIGGING, EpicColoniesAnimations.CITIZEN_DIG);
        animator.addLivingAnimation(EpicColoniesLivingMotions.USE, EpicColoniesAnimations.CITIZEN_DIG);
        animator.addLivingAnimation(EpicColoniesLivingMotions.PLACE, EpicColoniesAnimations.CITIZEN_USE);

        animator.addLivingAnimation(EpicColoniesLivingMotions.SIT_SLEEP, Animations.BIPED_SIT);

        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, EpicColoniesAnimations.CITIZEN_WALK);
        animator.addLivingAnimation(EpicColoniesLivingMotions.JOG, EpicColoniesAnimations.CITIZEN_JOG);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.SIT, Animations.BIPED_SIT);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
        animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
        animator.addLivingAnimation(LivingMotions.SLEEP, Animations.BIPED_SLEEPING);
        animator.addLivingAnimation(LivingMotions.AIM, Animations.BIPED_BOW_AIM);
        animator.addLivingAnimation(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT);
        animator.addLivingAnimation(LivingMotions.DRINK, Animations.BIPED_DRINK);
    }


}