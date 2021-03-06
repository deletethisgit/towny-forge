package deletethis.townyforge.item;

import java.util.List;

import deletethis.townyforge.entity.item.EntityItemWithEventPosting;
import deletethis.townyforge.object.Plot;
import deletethis.townyforge.object.Town;
import deletethis.townyforge.util.CivilizationMessageSender;
import deletethis.townyforge.util.CivilizationObjectFactory;
import deletethis.townyforge.util.EnumMessage;
import deletethis.townyforge.world.CivilizationWorldData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;


public class ItemTownBook extends Item
{	
	public static enum EnumVariant
	{
		CREATABLE(0, "town_book_creatable"),
		CREATED(1, "town_book_created");
		
		private int metadata;
		private String unlocalizedName;
		
		public static final EnumVariant[] variants = new EnumVariant[values().length];
		
		static
		{
			for (EnumVariant value : values())
			{
				variants[value.getMetaData()] = value;
			}
		}
		
		public int getMetaData()
		{
			return metadata;
		}
		
		public String getUnlocalizedName()
		{
			return unlocalizedName;
		}
		
		private EnumVariant(int metadata, String unlocalizedName)
		{
			this.metadata = metadata;
			this.unlocalizedName = unlocalizedName;
		}
	}
	
	
	public static EnumVariant getVariantFromMetadata(int metadata)
	{
		return EnumVariant.variants[metadata];
	}
	
	public ItemTownBook()
	{
		setUnlocalizedName("town_book");
		setMaxDamage(0);
		setHasSubtypes(true);
		this.setMaxStackSize(1);
	}
	
	@Override
    public String getItemStackDisplayName(ItemStack stack)
    {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append((stack.getMetadata() == EnumVariant.CREATED.getMetaData() ? EnumChatFormatting.AQUA : ""));
		stringBuilder.append(StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim());
        return stringBuilder.toString();
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		EnumVariant variant = getVariantFromMetadata(stack.getMetadata());
		return "item." + variant.getUnlocalizedName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(Item item, CreativeTabs tab, @SuppressWarnings("rawtypes") List subItems) 
	{
		for(EnumVariant variant : EnumVariant.variants)
		{
			subItems.add(new ItemStack(item, 1, variant.getMetaData()));
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {	
		if(stack.getMetadata() == ItemTownBook.EnumVariant.CREATABLE.getMetaData())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			
			if(nbt == null)
			{
				nbt = new NBTTagCompound();
				stack.setTagCompound(nbt);	
			}

			String townName = nbt.getString("townName");
			
			if(townName.isEmpty())
			{
				CivilizationMessageSender.send(player, EnumMessage.UNSPECIFIED_TOWN_NAME);
				return stack;
			}
			
			CivilizationWorldData data = CivilizationWorldData.get(world);
			
			if(data.townExists(townName))
			{
				CivilizationMessageSender.send(player, EnumMessage.TOWN_ALREADY_EXISTS, townName);
				return stack;
			}
			
			Town town = CivilizationObjectFactory.createTown(townName, player);
			
			BlockPos blockPos = player.getPosition();
	        int x = world.getChunkFromBlockCoords(blockPos).xPosition;
	        int z = world.getChunkFromBlockCoords(blockPos).zPosition;
			Plot plot = data.getPlot(world, x, z);

			if(plot.isOwned())
			{
				CivilizationMessageSender.send(player, EnumMessage.OTHER_ALREADY_OWN_PLOT, plot.getTown().getName());
				return stack;
			}
			
			data.addTown(town);
			
			stack.setItemDamage(EnumVariant.CREATED.getMetaData());
			nbt.setString("founder", player.getGameProfile().getName());
			nbt.setString("plotCount", Integer.toString(town.getPlotCount()));

			if(!world.isRemote)
			{
				@SuppressWarnings("unchecked")
				List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
				for(EntityPlayerMP playerIteration : players)
				{
					CivilizationMessageSender.send(playerIteration, EnumMessage.TOWN_CREATED, townName);
				}
			}
			
			return stack;
		}
		
		if(stack.getMetadata() == ItemTownBook.EnumVariant.CREATED.getMetaData())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			
			if(nbt == null)
			{
				nbt = new NBTTagCompound();
				stack.setTagCompound(nbt);	
			}
			
			String townName = nbt.getString("townName");
			
			if(townName.isEmpty())
				return stack;
			
			CivilizationWorldData data = CivilizationWorldData.get(world);
			Town town = data.getTown(townName);
			
			if(town == null)
				return stack;
			
			BlockPos blockPos = player.getPosition();
	        int x = world.getChunkFromBlockCoords(blockPos).xPosition;
	        int z = world.getChunkFromBlockCoords(blockPos).zPosition;
			Plot plot = data.getPlot(world, x, z);
			
			if(town.hasPlot(plot))
			{
				CivilizationMessageSender.send(player, EnumMessage.YOU_ALREADY_OWN_PLOT);
				return stack;
			}
			
			if(plot.isOwned())
			{
				CivilizationMessageSender.send(player, EnumMessage.OTHER_ALREADY_OWN_PLOT, plot.getTown().getName());
				return stack;
			}
			
			plot.setTown(town);
			town.addPlot(plot);
			nbt.setString("plotCount", Integer.toString(town.getPlotCount()));
			CivilizationMessageSender.send(player, EnumMessage.PLOT_ACQUIRED);
			
			return stack;
		}

		return stack;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, @SuppressWarnings("rawtypes") List tooltip, boolean advanced) 
	{
		NBTTagCompound nbt = stack.getTagCompound();
		
		if(nbt == null)
		{
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);	
		}
		
		String townName = nbt.getString("townName");
		
		if(!townName.isEmpty())
		{
			tooltip.add("Town: " + townName);
		}
		
		String founder = nbt.getString("founder");
		if(!founder.isEmpty())
		{
			tooltip.add("Founder: " + founder);
		}
		
		String plotCount = nbt.getString("plotCount");
		if(!plotCount.isEmpty())
		{
			tooltip.add("Plots: " + plotCount);
		}
	}
	
	@Override
    public boolean hasCustomEntity(ItemStack stack)
    {
        return true;
    }
	
	@Override
    public Entity createEntity(World world, Entity previous, ItemStack stack)
    {
		EntityItemWithEventPosting entity = new EntityItemWithEventPosting(world, previous.posX, previous.posY, previous.posZ, stack);
		return entity;
    }
}
