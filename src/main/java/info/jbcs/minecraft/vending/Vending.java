package info.jbcs.minecraft.vending;

import info.jbcs.minecraft.vending.block.BlockVendingMachine;
import info.jbcs.minecraft.vending.gui.GuiAdvancedVendingMachine;
import info.jbcs.minecraft.vending.gui.GuiMultipleVendingMachine;
import info.jbcs.minecraft.vending.gui.GuiVendingMachine;
import info.jbcs.minecraft.vending.gui.GuiWrenchVendingMachine;
import info.jbcs.minecraft.vending.gui.lib.GuiHandler;
import info.jbcs.minecraft.vending.inventory.ContainerAdvancedVendingMachine;
import info.jbcs.minecraft.vending.inventory.ContainerMultipleVendingMachine;
import info.jbcs.minecraft.vending.inventory.ContainerVendingMachine;
import info.jbcs.minecraft.vending.inventory.DummyContainer;
import info.jbcs.minecraft.vending.network.MessagePipeline;
import info.jbcs.minecraft.vending.proxy.CommonProxy;
import info.jbcs.minecraft.vending.tileentity.TileEntityVendingMachine;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

@Mod(modid = Vending.MOD_ID, name = Vending.MOD_NAME, version = Vending.VERSION)
public class Vending {
    public static final String MOD_ID = "vending";
    @SuppressWarnings("WeakerAccess")
    public static final String MOD_NAME = "vending";
    @SuppressWarnings("WeakerAccess")
    public static final String VERSION = "1.3.4";

    @Instance(MOD_ID)
    public static Vending instance;

    public static Block blockVendingMachine;
    public static Block blockAdvancedVendingMachine;
    public static Block blockMultipleVendingMachine;
    public static Item itemWrench;

    public static SoundEvent sound_processed;
    public static SoundEvent sound_forbidden;

    public static GuiHandler guiVending;
    public static GuiHandler guiWrench;

    public static CreativeTabs tabVending;
    public static boolean close_on_partial_sold_out;
    public static boolean close_on_sold_out;
    public static boolean block_placing_next_to_doors;
    public static boolean transfer_to_inventory;
    @SidedProxy(clientSide = "info.jbcs.minecraft.vending.proxy.ClientProxy", serverSide = "info.jbcs.minecraft.vending.proxy.CommonProxy")
    @SuppressWarnings("WeakerAccess")
    public static CommonProxy commonProxy;
    private static Configuration config;
    public MessagePipeline messagePipeline;

    public Vending() {
        messagePipeline = new MessagePipeline();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        blockVendingMachine = new BlockVendingMachine(false, false, "vendingMachine");
        blockAdvancedVendingMachine = new BlockVendingMachine(true, false, "vendingMachineAdvanced");
        blockMultipleVendingMachine = new BlockVendingMachine(false, true, "vendingMachineMultiple");

        itemWrench = new Item().setRegistryName("vendingMachineWrench").setUnlocalizedName("vendingMachineWrench")
                .setCreativeTab(tabVending).setContainerItem(itemWrench);
        GameRegistry.register(itemWrench);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent event) {
        commonProxy.registerPackets(messagePipeline);
        commonProxy.registerEventHandlers();
        commonProxy.registerRenderers();
        commonProxy.registerCraftingRecipes();

        if (config.get("general", "use_custom_creative_tab", true, "Add a new tab to creative mode and put all vending blocks there.").getBoolean(true)) {
            tabVending = new CreativeTabs("tabVending") {
                @Override
                @Nonnull
                public ItemStack getIconItemStack() {
                    return new ItemStack(blockVendingMachine, 1, 4);
                }

                @Override
                @Nonnull
                public Item getTabIconItem() {
                    return new ItemStack(blockVendingMachine, 1, 4).getItem();
                }
            };
        } else {
            tabVending = CreativeTabs.DECORATIONS;
        }
        close_on_sold_out = config.get("general", "close_on_sold_out", false, "Stop accepting items after last item is sold out.").getBoolean(false);
        close_on_partial_sold_out = config.get("general", "close_on_partial_sold_out", false,
                "Stop accepting items after some items were sold out.").getBoolean(false);
        block_placing_next_to_doors = config.get("general", "block_placing_next_to_doors", false,
                "Check for nearby doors when block is placed " +
                        "(Use specialized mod if you want more advanced restrictions)").getBoolean(false);

        transfer_to_inventory = config.get("general", "transfer_to_inventory", false,
                "Transfer sold items directly to player's inventory.").getBoolean(false);

        config.save();
        blockVendingMachine.setCreativeTab(tabVending);
        blockAdvancedVendingMachine.setCreativeTab(tabVending);
        blockMultipleVendingMachine.setCreativeTab(tabVending);
        itemWrench.setCreativeTab(tabVending);

        GameRegistry.registerTileEntity(TileEntityVendingMachine.class, "containerVendingMachine");

        guiVending = new GuiHandler("vending") {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

                if (!(tileEntity instanceof TileEntityVendingMachine))
                    return null;

                TileEntityVendingMachine e = (TileEntityVendingMachine) tileEntity;

                if (e.isAdvanced())
                    return new ContainerAdvancedVendingMachine(player.inventory, e);

                if (e.isMultiple())
                    return new ContainerMultipleVendingMachine(player.inventory, e);

                return new ContainerVendingMachine(player.inventory, e);
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

                if (!(tileEntity instanceof TileEntityVendingMachine))
                    return null;

                TileEntityVendingMachine e = (TileEntityVendingMachine) tileEntity;

                if (e.isAdvanced())
                    return new GuiAdvancedVendingMachine(player.inventory, e);

                if (e.isMultiple())
                    return new GuiMultipleVendingMachine(player.inventory, e);

                return new GuiVendingMachine(player.inventory, e);
            }
        };

        guiWrench = new GuiHandler("wrench") {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                return new DummyContainer();
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

                return new GuiWrenchVendingMachine(world, new BlockPos(x, y, z), player);
            }
        };

        GuiHandler.register(this);

        int soundEventId = SoundEvent.REGISTRY.getKeys().size();
        ResourceLocation resourcelocation = new ResourceLocation("vending", "vending.sound.processed");
        SoundEvent.REGISTRY.register(soundEventId++, resourcelocation, new SoundEvent(resourcelocation));
        sound_processed = SoundEvent.REGISTRY.getObject(resourcelocation);

        resourcelocation = new ResourceLocation("vending", "vending.sound.forbidden");
        SoundEvent.REGISTRY.register(soundEventId, resourcelocation, new SoundEvent(resourcelocation));
        sound_forbidden = SoundEvent.REGISTRY.getObject(resourcelocation);

		/*
        Iterator iterator = soundEventRegistry.iterator();
		while(iterator.hasNext()){
			SoundEvent soundEvent = (SoundEvent) iterator.next();
			System.out.println(soundEvent.getSoundName());
		}*/
    }
}



