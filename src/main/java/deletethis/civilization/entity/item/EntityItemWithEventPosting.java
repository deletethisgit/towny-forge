package deletethis.civilization.entity.item;

import deletethis.civilization.event.entity.ItemDestructionEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class EntityItemWithEventPosting extends EntityItem	
{
	public EntityItemWithEventPosting(World world, double x, double y, double z, ItemStack stack)
	{
		super(world, x, y, z, stack);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		boolean returnValue = super.attackEntityFrom(source, amount);
		
		if(this.isDead)
		{
			ItemDestructionEvent event = new ItemDestructionEvent(this);
			MinecraftForge.EVENT_BUS.post(event);
		}
		
		return returnValue;
	}
}
