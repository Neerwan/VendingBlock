package info.jbcs.minecraft.vending.proxy;

import info.jbcs.minecraft.vending.Vending;
import info.jbcs.minecraft.vending.block.EnumSupports;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.relauncher.Side;
import info.jbcs.minecraft.vending.network.MessagePipeline;
import info.jbcs.minecraft.vending.network.MsgAdvVenSetItem;
import info.jbcs.minecraft.vending.network.MsgWrench;

public class CommonProxy {
	public void registerEventHandlers()
	{
	}

	public void registerPackets(MessagePipeline pipeline)
	{
		pipeline.registerMessage(MsgAdvVenSetItem.Handler.class, MsgAdvVenSetItem.class, 0, Side.SERVER);
		pipeline.registerMessage(MsgWrench.Handler.class, MsgWrench.class, 1, Side.SERVER);
	}

	public void registerRenderers()
	{
	}

	public void registerCraftingRecipes(){
		for(int i=0;i< EnumSupports.length;i++){
			CraftingManager.getInstance().addRecipe(new ItemStack(Vending.blockVendingMachine, 1, i),
					new Object[]{"XXX", "XGX", "*R*",
							'X', Blocks.glass,
							'G', Items.gold_ingot,
							'R', Items.redstone,
							'*', EnumSupports.byMetadata(i).getReagent(),
					});


			CraftingManager.getInstance().addRecipe(new ItemStack(Vending.blockAdvancedVendingMachine, 1, i),
					new Object[]{"XXX", "XGX", "*R*",
							'X', Blocks.glass,
							'G', Items.gold_ingot,
							'R', Items.repeater,
							'*', EnumSupports.byMetadata(i).getReagent(),
					});

			CraftingManager.getInstance().addRecipe(new ItemStack(Vending.blockMultipleVendingMachine, 1, i),
					new Object[]{"XXX", "XGX", "*R*",
							'X', Blocks.glass,
							'G', Items.gold_ingot,
							'R', Blocks.dispenser,
							'*', EnumSupports.byMetadata(i).getReagent(),
					});
		}
	}
}
