package cr0s.warpdrive.event;

import cr0s.warpdrive.api.IItemBase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemHandler {
	
	@SubscribeEvent
	public void onItemExpireEvent(final ItemExpireEvent event) {
		if (event.getEntityItem() == null) {
			return;
		}
		final ItemStack itemStack = event.getEntityItem().getEntityItem();
		if (itemStack == null) {
			return;
		}
		final Item item = itemStack.getItem();
		if (!(item instanceof IItemBase)) {
			return;
		}
		((IItemBase) item).onEntityExpireEvent(event.getEntityItem(), itemStack);
	}
}
