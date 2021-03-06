package deletethis.townyforge.eventhandler;

import deletethis.townyforge.event.entity.ItemDestructionEvent;
import deletethis.townyforge.item.ItemTownBook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemDestructionEventHandler
{
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onItemDestroyed(ItemDestructionEvent event)
	{
		ItemStack stack = event.entityItem.getEntityItem();
		Item stackItem = stack.getItem();
		
		if(stackItem instanceof ItemTownBook)
		{
			System.out.println("Town book destroyed!");
		}
	}
}
