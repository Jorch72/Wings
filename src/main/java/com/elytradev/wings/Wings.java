package com.elytradev.wings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.wings.block.BlockConverter;
import com.elytradev.wings.item.ItemGoggles;
import com.elytradev.wings.item.ItemLeatherElytra;
import com.elytradev.wings.item.ItemMetalElytra;
import com.elytradev.wings.item.ItemMetalJetElytra;
import com.elytradev.wings.network.PlayerWingsUpdateMessage;
import com.elytradev.wings.network.SetFlightStateMessage;
import com.elytradev.wings.network.SetRotationAndSpeedMessage;
import com.elytradev.wings.network.SetThrusterMessage;
import com.elytradev.wings.network.SonicBoomEffectMessage;
import com.elytradev.wings.tile.TileEntityConverter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid=Wings.MODID, name=Wings.NAME, version=Wings.VERSION)
public class Wings {
	public static final Logger log = LogManager.getLogger("Wings");
	
	public static final String MODID = "wings";
	public static final String NAME = "Wings";
	public static final String VERSION = "@VERSION@";
	
	public static BlockConverter CONVERTER;
	
	public static ItemLeatherElytra LEATHER_ELYTRA;
	public static ItemMetalElytra METAL_ELYTRA;
	public static ItemMetalJetElytra METAL_JET_ELYTRA;
	public static ItemGoggles GOGGLES;
	
	public static Fluid JET_FUEL;
	
	public static SoundEvent THRUST;
	public static SoundEvent AFTERBURNER_START;
	public static SoundEvent AFTERBURNER;
	public static SoundEvent SONIC_BOOM;
	public static SoundEvent SONIC_BOOM_SELF;
	public static SoundEvent SONIC_BOOM_SELF_START;
	
	public static DamageSource SUPERSONIC_NO_GOGGLES = new DamageSource("wings.supersonic_no_goggles");
	
	@SidedProxy(clientSide="com.elytradev.wings.client.ClientProxy", serverSide="com.elytradev.wings.Proxy")
	public static Proxy proxy;
	@Instance
	public static Wings inst;
	
	
	public NetworkContext network;
	
	static {
		FluidRegistry.enableUniversalBucket();
	}
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		
		JET_FUEL = new Fluid("wings.jetfuel", new ResourceLocation("wings", "blocks/fuel_still"), new ResourceLocation("wings", "blocks/fuel_flow"))
				.setViscosity(750);
		FluidRegistry.registerFluid(JET_FUEL);
		
		FluidRegistry.addBucketForFluid(JET_FUEL);
		
		GameRegistry.registerTileEntity(TileEntityConverter.class, "wings:converter");
		
		network = NetworkContext.forChannel("wings");
		network.register(SetFlightStateMessage.class);
		network.register(SetThrusterMessage.class);
		network.register(SetRotationAndSpeedMessage.class);
		network.register(PlayerWingsUpdateMessage.class);
		network.register(SonicBoomEffectMessage.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new WingsGuiHandler());
		
		OreDictionary.registerOre("coal", new ItemStack(Items.COAL, 1, 0));
		OreDictionary.registerOre("charcoal", new ItemStack(Items.COAL, 1, 1));
		
		OreDictionary.registerOre("blockCoal", Blocks.COAL_BLOCK);
		
		
		proxy.preInit();
	}
	
	@EventHandler
	public void onInit(FMLInitializationEvent e) {
		ConverterRecipes.registerFluid("lava", 150);
		ConverterRecipes.registerFluid("oil", 500);
		ConverterRecipes.registerFluid("fuel", 750);
		ConverterRecipes.registerFluid("gasoline", 750);
		ConverterRecipes.registerFluid("pyrotheum", 640);
		ConverterRecipes.registerFluid("aerotheum", 800);
		ConverterRecipes.registerFluid("crude_oil", 350);
		ConverterRecipes.registerFluid("tree_oil", 250);
		ConverterRecipes.registerFluid("creosote", 100);
		ConverterRecipes.registerFluid("refined_oil", 650);
		ConverterRecipes.registerFluid("refined_fuel", 850);
		ConverterRecipes.registerFluid("coal", 500);
		ConverterRecipes.registerFluid("canolaoil", 250);
		ConverterRecipes.registerFluid("crystaloil", 500);
		ConverterRecipes.registerFluid("empoweredoil", 750);
		ConverterRecipes.registerFluid("kerosene", 950);
		ConverterRecipes.registerFluid("pain", 10);
		ConverterRecipes.registerFluid("soylent", 5);
		
		
		ConverterRecipes.registerItem("coal", 12);
		ConverterRecipes.registerItem("blockCoal", 120);
		ConverterRecipes.registerItem("charcoal", 10);
		ConverterRecipes.registerItem("blockCharcoal", 100);
		ConverterRecipes.registerItem("fuelCoke", 32);
		ConverterRecipes.registerItem("plankWood", 2);
		ConverterRecipes.registerItem("logWood", 10);
		ConverterRecipes.registerItem("dustPyrotheum", 160);
		ConverterRecipes.registerItem("dustAerotheum", 200);
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}
	
	@SubscribeEvent
	public void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
		e.getRegistry().register(new RecipeWings(null, new ItemStack(LEATHER_ELYTRA),
				" l ",
				"bBb",
				'l', Items.LEATHER,
				'b', Items.BANNER,
				'B', Blocks.BEDROCK)
				.setRegistryName("create_leather_elytra"));
		e.getRegistry().register(new RecipeWings(null, new ItemStack(METAL_ELYTRA),
				"ili",
				"bBb",
				'i', "ingotIron",
				'l', Items.LEATHER,
				'b', Items.BANNER,
				'B', Blocks.BEDROCK)
				.setRegistryName("create_metal_elytra"));
		e.getRegistry().register(new RecipeWings(null, new ItemStack(METAL_JET_ELYTRA),
				"ili",
				"bBb",
				"B B",
				'i', "ingotIron",
				'l', Items.LEATHER,
				'b', Items.BANNER,
				'B', Blocks.BEDROCK)
				.setRegistryName("create_metal_jet_elytra"));
		
		e.getRegistry().register(new RecipeWings(null, new ItemStack(METAL_ELYTRA),
				"iwi",
				'w', LEATHER_ELYTRA,
				'i', "ingotIron")
				.setRegistryName("upgrade_leather_to_metal"));
		e.getRegistry().register(new RecipeWings(null, new ItemStack(METAL_JET_ELYTRA),
				"BwB",
				'w', METAL_ELYTRA,
				'B', Blocks.BEDROCK)
				.setRegistryName("upgrade_metal_to_jet"));
	}
	
	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> e) {
		e.getRegistry().register((CONVERTER = new BlockConverter())
				.setUnlocalizedName("wings.converter")
				.setHardness(2)
				.setRegistryName("converter"));
		
		e.getRegistry().register(new BlockFluidClassic(JET_FUEL, Material.WATER)
				.setRegistryName("fuel_block"));
	}
	
	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> e) {
		e.getRegistry().register((LEATHER_ELYTRA = new ItemLeatherElytra())
				.setMaxDamage(324)
				.setUnlocalizedName("wings.leather_elytra")
				.setRegistryName("leather_elytra"));
		
		e.getRegistry().register((METAL_ELYTRA = new ItemMetalElytra())
				.setMaxDamage(648)
				.setUnlocalizedName("wings.metal_elytra")
				.setRegistryName("metal_elytra"));
		
		e.getRegistry().register((METAL_JET_ELYTRA = new ItemMetalJetElytra())
				.setUnlocalizedName("wings.metal_jet_elytra")
				.setRegistryName("metal_jet_elytra"));
		
		
		e.getRegistry().register((GOGGLES = new ItemGoggles())
				.setUnlocalizedName("wings.goggles")
				.setRegistryName("goggles"));
		
		e.getRegistry().register(new ItemBlock(CONVERTER).setRegistryName("converter"));
	}
	
	@SubscribeEvent
	public void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
		e.getRegistry().register(THRUST = new SoundEvent(new ResourceLocation("wings", "thrust"))
				.setRegistryName("thrust"));
		e.getRegistry().register(AFTERBURNER = new SoundEvent(new ResourceLocation("wings", "afterburner"))
				.setRegistryName("afterburner"));
		e.getRegistry().register(AFTERBURNER_START = new SoundEvent(new ResourceLocation("wings", "afterburner_start"))
				.setRegistryName("afterburner_start"));
		e.getRegistry().register(SONIC_BOOM = new SoundEvent(new ResourceLocation("wings", "sonic_boom"))
				.setRegistryName("sonic_boom"));
		e.getRegistry().register(SONIC_BOOM_SELF = new SoundEvent(new ResourceLocation("wings", "sonic_boom_self"))
				.setRegistryName("sonic_boom_self"));
		e.getRegistry().register(SONIC_BOOM_SELF_START = new SoundEvent(new ResourceLocation("wings", "sonic_boom_self_start"))
				.setRegistryName("sonic_boom_self_start"));
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		if (e.phase == Phase.END) {
			WingsPlayer.getIfExists(e.player).ifPresent(WingsPlayer::update);
		}
	}
	
}
