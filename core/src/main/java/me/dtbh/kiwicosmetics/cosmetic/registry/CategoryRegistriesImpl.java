/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev, dtbh and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.dtbh.kiwicosmetics.cosmetic.registry;

import org.bukkit.entity.EntityType;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.balloon.BalloonBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.balloon.BalloonType;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerType;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetType;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureType;
import me.dtbh.kiwicosmetics.api.cosmetic.music.MusicBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.music.MusicType;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CategoryRegistries;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticRegistry;
import me.dtbh.kiwicosmetics.cosmetic.arroweffect.ArrowEffectTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.arroweffect.type.*;
import me.dtbh.kiwicosmetics.cosmetic.balloon.BalloonTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.balloon.type.DefaultBalloon;
import me.dtbh.kiwicosmetics.cosmetic.banner.BannerTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.banner.type.DefaultBanner;
import me.dtbh.kiwicosmetics.cosmetic.deatheffect.DeathEffectTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.deatheffect.type.*;
import me.dtbh.kiwicosmetics.cosmetic.gadget.GadgetTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.gadget.type.*;
import me.dtbh.kiwicosmetics.cosmetic.miniature.MiniatureTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.*;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Chicken;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Cow;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Enderman;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.IronGolem;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Sheep;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Skeleton;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Spider;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Witch;
import me.dtbh.kiwicosmetics.cosmetic.miniature.type.Zombie;
import me.dtbh.kiwicosmetics.cosmetic.music.MusicTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.music.type.DefaultMusic;
import me.dtbh.kiwicosmetics.cosmetic.particleeffect.ParticleEffectTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.particleeffect.type.*;
import me.dtbh.kiwicosmetics.cosmetic.particleeffect.type.Bunny;
import me.dtbh.kiwicosmetics.cosmetic.particleeffect.type.Confetti;
import me.dtbh.kiwicosmetics.cosmetic.plushie.PlushieTypeImpl;
import me.dtbh.kiwicosmetics.cosmetic.plushie.type.DefaultPlushie;
import me.dtbh.kiwicosmetics.cosmetic.trail.BlockTrails;
import me.dtbh.kiwicosmetics.menu.CosmeticMenuImpl;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.cosmetic.banner.HatCosmeticType;
import me.dtbh.kiwicosmetics.wardrobe.GuiHat;
import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CategoryRegistriesImpl implements CategoryRegistries {

    private final Map<String, CosmeticCategory<?, ?, ?>> categories = new HashMap<>();

    public CategoryRegistriesImpl(KiwiCosmetics plugin) {
        register(new CosmeticCategoryImpl<>(BuiltIn.ARROW_EFFECTS, ArrowEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.BALLOONS, BalloonTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.BANNERS, BannerTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.DEATH_EFFECTS, DeathEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.GADGETS, GadgetTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.MINIATURES, MiniatureTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.PLUSHIES, PlushieTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.MUSIC, MusicTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.PARTICLE_EFFECTS, ParticleEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.TRAILS, ParticleEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));

        // Hats come from hats.yml, which needs Paper. The category config is hat_cosmetics.yml
        // because hats.yml itself belongs to the wardrobe.
        Wardrobe wardrobe = KiwiCosmeticsPlugin.getPlugin().getWardrobe();

        if (wardrobe != null) {
            register(new CosmeticCategoryImpl<>(BuiltIn.HATS, "hat_cosmetics", BannerTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
            populateHats(wardrobe);
        }

        populateCosmetics();
    }

    private void populateHats(Wardrobe wardrobe) {
        CosmeticCategory<BannerType, BannerBehavior, BannerType.Builder> hats = hats();
        CosmeticRegistry<BannerType, BannerBehavior, BannerType.Builder> registry = hats.getCosmeticRegistry();
        CosmeticRarity rarity = KiwiCosmeticsPlugin.getPlugin().getCosmeticRarityRegistry().getSafely(hats.getConfig().getString("rarity"));

        for (GuiHat hat : wardrobe.guiHats()) {
            registry.register(new HatCosmeticType(hat.id(), hats, rarity, hat.item(), hat.name()));
        }
    }

    private void populateCosmetics() {
        CosmeticRegistry<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> arrowEffects = arrowEffects().getCosmeticRegistry();
        arrowEffects.register(arrowEffects.builder("bloody_arrows").readFromConfig().factory(BloodyArrows::new).build());
        arrowEffects.register(arrowEffects.builder("candy_cane_arrows").readFromConfig().factory(CandyCaneArrows::new).build());
        arrowEffects.register(arrowEffects.builder("confetti_arrows").readFromConfig().factory(ConfettiArrows::new).build());
        arrowEffects.register(arrowEffects.builder("enchanted_arrows").readFromConfig().factory(EnchantedArrows::new).build());
        arrowEffects.register(arrowEffects.builder("flame_arrows").readFromConfig().factory(FlameArrows::new).build());
        arrowEffects.register(arrowEffects.builder("frost_lord_arrows").readFromConfig().factory(FrostLordArrows::new).build());
        arrowEffects.register(arrowEffects.builder("green_arrows").readFromConfig().factory(GreenArrows::new).build());
        arrowEffects.register(arrowEffects.builder("lava_arrows").readFromConfig().factory(LavaArrows::new).build());
        arrowEffects.register(arrowEffects.builder("love_arrows").readFromConfig().factory(LoveArrows::new).build());
        arrowEffects.register(arrowEffects.builder("magical_arrows").readFromConfig().factory(MagicalArrows::new).build());
        arrowEffects.register(arrowEffects.builder("music_arrows").readFromConfig().factory(MusicArrows::new).build());
        arrowEffects.register(arrowEffects.builder("rain_arrows").readFromConfig().factory(RainArrows::new).build());
        arrowEffects.register(arrowEffects.builder("shadow_arrows").readFromConfig().factory(ShadowArrows::new).build());
        arrowEffects.register(arrowEffects.builder("sparkly_arrows").readFromConfig().factory(SparklyArrows::new).build());

        CosmeticRegistry<BalloonType, BalloonBehavior, BalloonType.Builder> balloons = balloons().getCosmeticRegistry();
        for (String key : balloons().getConfig().getSectionKeys("cosmetics")) {
            if (!balloons.isRegistered(key)) {
                balloons.register(balloons.builder(key).readFromConfig().factory(DefaultBalloon::new).build());
            }
        }

        CosmeticRegistry<BannerType, BannerBehavior, BannerType.Builder> banners = banners().getCosmeticRegistry();
        for (String key : banners().getConfig().getSectionKeys("cosmetics")) {
            if (!banners.isRegistered(key)) {
                banners.register(banners.builder(key).readFromConfig().factory(DefaultBanner::new).build());
            }
        }

        CosmeticRegistry<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> deathEffects = deathEffects().getCosmeticRegistry();
        deathEffects.register(deathEffects.builder("bloody_death").readFromConfig().factory(BloodyDeath::new).build());
        deathEffects.register(deathEffects.builder("candy_cane_remains").readFromConfig().factory(CandyCaneRemains::new).build());
        deathEffects.register(deathEffects.builder("confetti_death").readFromConfig().factory(ConfettiDeath::new).build());
        deathEffects.register(deathEffects.builder("enchanted_death").readFromConfig().factory(EnchantedDeath::new).build());
        deathEffects.register(deathEffects.builder("flame_death").readFromConfig().factory(FlameDeath::new).build());
        deathEffects.register(deathEffects.builder("fall_of_the_frost_lord").readFromConfig().factory(FallOfTheFrostLord::new).build());
        deathEffects.register(deathEffects.builder("lava_death").readFromConfig().factory(LavaDeath::new).build());
        deathEffects.register(deathEffects.builder("lightning_death").readFromConfig().factory(LightningDeath::new).build());
        deathEffects.register(deathEffects.builder("magical_death").readFromConfig().factory(MagicalDeath::new).build());
        deathEffects.register(deathEffects.builder("last_love").readFromConfig().factory(LastLove::new).build());
        deathEffects.register(deathEffects.builder("musical_death").readFromConfig().factory(MusicalDeath::new).build());
        deathEffects.register(deathEffects.builder("wet_death").readFromConfig().factory(WetDeath::new).build());
        deathEffects.register(deathEffects.builder("sparkly_death").readFromConfig().factory(SparklyDeath::new).build());
        deathEffects.register(deathEffects.builder("shadow_death").readFromConfig().factory(ShadowDeath::new).build());


        CosmeticRegistry<GadgetType, GadgetBehavior, GadgetType.Builder> gadgets = gadgets().getCosmeticRegistry();
        gadgets.register(gadgets.builder("bat_blaster").readFromConfig().factory(BatBlaster::new).build());
        gadgets.register(gadgets.builder("detonator").readFromConfig().factory(Detonator::new).build());
        gadgets.register(gadgets.builder("cannon").readFromConfig().factory(Cannon::new).build());
        gadgets.register(gadgets.builder("chicken_parachute").readFromConfig().factory(ChickenParachute::new).build());
        gadgets.register(gadgets.builder("confetti").readFromConfig().factory(me.dtbh.kiwicosmetics.cosmetic.gadget.type.Confetti::new).build());
        gadgets.register(gadgets.builder("connect_four").readFromConfig().factory(ConnectFour::new).build());
        gadgets.register(gadgets.builder("cowboy").readFromConfig().factory(Cowboy::new).build());
        gadgets.register(gadgets.builder("disco_ball").readFromConfig().factory(DiscoBall::new).build());
        gadgets.register(gadgets.builder("diving_board").readFromConfig().factory(DivingBoard::new).build());
        gadgets.register(gadgets.builder("ethereal_pearl").readFromConfig().factory(EtherealPearl::new).build());
        gadgets.register(gadgets.builder("explosive_sheep").readFromConfig().factory(ExplosiveSheep::new).build());
        gadgets.register(gadgets.builder("fireworks").readFromConfig().factory(Fireworks::new).build());
        gadgets.register(gadgets.builder("flame_thrower").readFromConfig().factory(FlameThrower::new).build());
        gadgets.register(gadgets.builder("flesh_hook").readFromConfig().factory(FleshHook::new).build());
        gadgets.register(gadgets.builder("grappling_hook").readFromConfig().factory(GrapplingHook::new).build());
        gadgets.register(gadgets.builder("hot_tub").readFromConfig().factory(HotTub::new).build());
        gadgets.register(gadgets.builder("hype_train").readFromConfig().factory(HypeTrain::new).build());
        gadgets.register(gadgets.builder("hot_air_balloon").readFromConfig().factory(HotAirBalloon::new).build());
        gadgets.register(gadgets.builder("melon_launcher").readFromConfig().readFromConfig().readFromConfig().factory(MelonLauncher::new).build());
        gadgets.register(gadgets.builder("merry_go_round").readFromConfig().factory(MerryGoRound::new).build());
        gadgets.register(gadgets.builder("paintball").readFromConfig().factory(Paintball::new).build());
        gadgets.register(gadgets.builder("parkour_spiral").readFromConfig().factory(ParkourSpiral::new).build());
        gadgets.register(gadgets.builder("party_popper").readFromConfig().readFromConfig().factory(PartyPopper::new).build());
        gadgets.register(gadgets.builder("rocket").readFromConfig().factory(Rocket::new).build());
        gadgets.register(gadgets.builder("shower").readFromConfig().factory(Shower::new).build());
        gadgets.register(gadgets.builder("slide").readFromConfig().factory(Slide::new).build());
        gadgets.register(gadgets.builder("soccer_ball").readFromConfig().factory(SoccerBall::new).build());
        gadgets.register(gadgets.builder("swing").readFromConfig().factory(Swing::new).build());
        gadgets.register(gadgets.builder("tnt").readFromConfig().factory(TNT::new).build());
        gadgets.register(gadgets.builder("trampoline").readFromConfig().factory(Trampoline::new).build());
        gadgets.register(gadgets.builder("water_gun").readFromConfig().factory(Watergun::new).build());
        gadgets.register(gadgets.builder("wither_missile").readFromConfig().factory(WitherMissile::new).build());

        CosmeticRegistry<MiniatureType, MiniatureBehavior, MiniatureType.Builder> miniatures = miniatures().getCosmeticRegistry();
        miniatures.register(miniatures.builder("astronaut").readFromConfig().factory(Astronaut::new).build());
        miniatures.register(miniatures.builder("brown_bear").readFromConfig().factory(BrownBear::new).build());
        miniatures.register(miniatures.builder("bunny").readFromConfig().factory(me.dtbh.kiwicosmetics.cosmetic.miniature.type.Bunny::new).build());
        miniatures.register(miniatures.builder("chicken").readFromConfig().factory(Chicken::new).build());
        miniatures.register(miniatures.builder("cow").readFromConfig().factory(Cow::new).build());
        miniatures.register(miniatures.builder("devil").readFromConfig().factory(Devil::new).build());
        miniatures.register(miniatures.builder("doge").readFromConfig().factory(Doge::new).build());
        miniatures.register(miniatures.builder("dolphin").readFromConfig().factory(Dolphin::new).build());
        miniatures.register(miniatures.builder("dwarf").readFromConfig().factory(Dwarf::new).build());
        miniatures.register(miniatures.builder("elephant").readFromConfig().factory(Elephant::new).build());
        miniatures.register(miniatures.builder("enderman").readFromConfig().factory(Enderman::new).build());
        miniatures.register(miniatures.builder("fox").readFromConfig().factory(Fox::new).build());
        miniatures.register(miniatures.builder("ghost").readFromConfig().factory(Ghost::new).build());
        miniatures.register(miniatures.builder("iron_golem").readFromConfig().factory(IronGolem::new).build());
        miniatures.register(miniatures.builder("koala_bear").readFromConfig().factory(KoalaBear::new).build());
        miniatures.register(miniatures.builder("ladybug").readFromConfig().factory(Ladybug::new).build());
        miniatures.register(miniatures.builder("monkey").readFromConfig().factory(Monkey::new).build());
        miniatures.register(miniatures.builder("panda").readFromConfig().factory(Panda::new).build());
        miniatures.register(miniatures.builder("parrot").readFromConfig().factory(Parrot::new).build());
        miniatures.register(miniatures.builder("santa_claus").readFromConfig().factory(me.dtbh.kiwicosmetics.cosmetic.miniature.type.SantaClaus::new).build());
        miniatures.register(miniatures.builder("shark").readFromConfig().readFromConfig().factory(Shark::new).build());
        miniatures.register(miniatures.builder("sheep").readFromConfig().factory(Sheep::new).build());
        miniatures.register(miniatures.builder("skeleton").readFromConfig().factory(Skeleton::new).build());
        miniatures.register(miniatures.builder("snail").readFromConfig().factory(Snail::new).build());
        miniatures.register(miniatures.builder("snowman").readFromConfig().factory(Snowman::new).build());
        miniatures.register(miniatures.builder("spider").readFromConfig().factory(Spider::new).build());
        miniatures.register(miniatures.builder("tiger").readFromConfig().factory(Tiger::new).build());
        miniatures.register(miniatures.builder("tiki").readFromConfig().readFromConfig().factory(Tiki::new).build());
        miniatures.register(miniatures.builder("turtle").readFromConfig().factory(Turtle::new).build());
        miniatures.register(miniatures.builder("vampire").readFromConfig().factory(Vampire::new).build());
        miniatures.register(miniatures.builder("witch").readFromConfig().factory(Witch::new).build());
        miniatures.register(miniatures.builder("zebra").readFromConfig().factory(Zebra::new).build());
        miniatures.register(miniatures.builder("zombie").readFromConfig().factory(Zombie::new).build());

        // Every plushie in plushies.yml is loaded, so server owners can add their own skins
        CosmeticRegistry<PlushieType, PlushieBehavior, PlushieType.Builder> plushies = plushies().getCosmeticRegistry();
        for (String key : plushies().getConfig().getSectionKeys("cosmetics")) {
            if (!plushies.isRegistered(key)) {
                plushies.register(plushies.builder(key).readFromConfig().factory(DefaultPlushie::new).build());
            }
        }



        CosmeticRegistry<MusicType, MusicBehavior, MusicType.Builder> music = music().getCosmeticRegistry();
        for (String key : music().getConfig().getSectionKeys("cosmetics")) {
            if (!music.isRegistered(key)) {
                music.register(music.builder(key).readFromConfig().factory(DefaultMusic::new).build());
            }
        }

        CosmeticRegistry<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> particleEffects = particleEffects().getCosmeticRegistry();
        particleEffects.register(particleEffects.builder("angel_wings").readFromConfig().factory(AngelWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("aura_of_flame").readFromConfig().factory(AuraOfFlame::new).build());
        particleEffects.register(particleEffects.builder("big_heart").readFromConfig().factory(BigHeart::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("black_hole").readFromConfig().factory(BlackHole::new).build());
        particleEffects.register(particleEffects.builder("blood_helix").readFromConfig().factory(BloodHelix::new).build());
        particleEffects.register(particleEffects.builder("bunny").readFromConfig().factory(Bunny::new).build());
        particleEffects.register(particleEffects.builder("christmas_tree").readFromConfig().factory(ChristmasTree::new).build());
        particleEffects.register(particleEffects.builder("colorful_trail").readFromConfig().factory(ColorfulTrail::new).build());
        particleEffects.register(particleEffects.builder("confetti").readFromConfig().factory(Confetti::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("crushed_candy_cane").readFromConfig().factory(CrushedCandyCane::new).build());
        particleEffects.register(particleEffects.builder("demon_twist").readFromConfig().factory(DemonTwist::new).build());
        particleEffects.register(particleEffects.builder("emerald_twirl").readFromConfig().factory(EmeraldTwirl::new).build());
        particleEffects.register(particleEffects.builder("enchanted").readFromConfig().factory(Enchanted::new).build());
        particleEffects.register(particleEffects.builder("flame_fairy").readFromConfig().factory(FlameFairy::new).build());
        particleEffects.register(particleEffects.builder("flame_of_magic").readFromConfig().factory(FlameOfMagic::new).build());
        particleEffects.register(particleEffects.builder("flame_of_the_demons").readFromConfig().factory(FlameOfTheDemons::new).build());
        particleEffects.register(particleEffects.builder("flame_rings").readFromConfig().factory(FlameRings::new).build());
        particleEffects.register(particleEffects.builder("frost_lord").readFromConfig().factory(FrostLord::new).build());
        particleEffects.register(particleEffects.builder("in_love").readFromConfig().factory(InLove::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("jack_o_lantern").readFromConfig().factory(JackOLantern::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("legendary_aura").readFromConfig().factory(LegendaryAura::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("music").readFromConfig().factory(Music::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("party_time").readFromConfig().factory(PartyTime::new).build());
        particleEffects.register(particleEffects.builder("protective_shield").readFromConfig().factory(ProtectiveShield::new).build());
        particleEffects.register(particleEffects.builder("rainbow_wings").readFromConfig().factory(RainbowWings::new).build());
        particleEffects.register(particleEffects.builder("rain_cloud").readFromConfig().factory(RainCloud::new).build());
        particleEffects.register(particleEffects.builder("santa").readFromConfig().factory(Santa::new).build());
        particleEffects.register(particleEffects.builder("shadow_walk").readFromConfig().factory(ShadowWalk::new).repeatDelay(5).build());
        particleEffects.register(particleEffects.builder("skull").readFromConfig().factory(Skull::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("snow_cloud").readFromConfig().factory(SnowCloud::new).build());
        particleEffects.register(particleEffects.builder("snowflake").readFromConfig().factory(Snowflake::new).build());
        particleEffects.register(particleEffects.builder("star").readFromConfig().factory(Star::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("tornado").readFromConfig().factory(Tornado::new).build());
        particleEffects.register(particleEffects.builder("umbrella").readFromConfig().factory(Umbrella::new).build());
        particleEffects.register(particleEffects.builder("vampire_wings").readFromConfig().factory(VampireWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("yin_and_yang").readFromConfig().factory(YinAndYang::new).build());
        particleEffects.register(particleEffects.builder("autumn_halo").readFromConfig().factory(AutumnHalo::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("leaf_vortex").readFromConfig().factory(LeafVortex::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("geyser_fountain").readFromConfig().factory(GeyserFountain::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("toxic_aura").readFromConfig().factory(ToxicAura::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("sulfur_spiral").readFromConfig().factory(SulfurSpiral::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("firefly_swarm").readFromConfig().factory(FireflySwarm::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("copper_crown").readFromConfig().factory(CopperCrown::new).repeatDelay(3).build());
        particleEffects.register(particleEffects.builder("comet_orbit").readFromConfig().factory(CometOrbit::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("dna_helix").readFromConfig().factory(DnaHelix::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("aurora_wings").readFromConfig().factory(AuroraWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("blossom_ring").readFromConfig().factory(BlossomRing::new).repeatDelay(2).build());

        // Block trails reuse the particle effect machinery: a behavior that runs every few ticks
        BlockTrails.load();
        CosmeticRegistry<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> trailRegistry = trails().getCosmeticRegistry();
        trailRegistry.register(trailRegistry.builder("rainbow").readFromConfig().factory(BlockTrails::rainbow).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("pastel").readFromConfig().factory(BlockTrails::pastel).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("candy_cane").readFromConfig().factory(BlockTrails::candyCane).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("frost").readFromConfig().factory(BlockTrails::frost).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("magma").readFromConfig().factory(BlockTrails::magma).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("golden").readFromConfig().factory(BlockTrails::golden).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("gemstone").readFromConfig().factory(BlockTrails::gemstone).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("nether").readFromConfig().factory(BlockTrails::nether).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("ocean").readFromConfig().factory(BlockTrails::ocean).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("galaxy").readFromConfig().factory(BlockTrails::galaxy).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("sculk").readFromConfig().factory(BlockTrails::sculk).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("copper").readFromConfig().factory(BlockTrails::copper).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("cherry").readFromConfig().factory(BlockTrails::cherry).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("amethyst").readFromConfig().factory(BlockTrails::amethyst).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("honey").readFromConfig().factory(BlockTrails::honey).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("cinnabar").readFromConfig().factory(BlockTrails::cinnabar).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("sulfur").readFromConfig().factory(BlockTrails::sulfur).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("resin").readFromConfig().factory(BlockTrails::resin).repeatDelay(2).build());
        trailRegistry.register(trailRegistry.builder("pale_garden").readFromConfig().factory(BlockTrails::paleGarden).repeatDelay(2).build());


    }

    @Override
    public <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>>
    void register(CosmeticCategory<T, B, U> category) {
        categories.put(category.getKey().toLowerCase(), category);
    }

    @Override
    public CosmeticCategory<?, ?, ?> getCategoryRaw(String key) {
        return categories.get(key.toLowerCase());
    }

    @Override
    public Collection<CosmeticCategory<?, ?, ?>> getCategories() {
        return categories.values();
    }
}
