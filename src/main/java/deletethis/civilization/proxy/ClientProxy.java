package deletethis.civilization.proxy;

import deletethis.civilization.item.ModItems;

public class ClientProxy extends CommonProxy
{
	public void preInit()
	{
		super.preInit();
	}
	
	public void init()
	{	
		super.init();
		ModItems.registerItemModels();
	}
	
	public void postInit()
	{		
		super.postInit();
	}
}