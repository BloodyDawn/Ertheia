package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;

public class RequestGiveItemToPet extends L2GameClientPacket
{
	private int _objectId;

	private long _amount;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.GET_FTOM_PET_ITEM))
		{
			player.sendMessage("Вы передаете вещи питомцу слишком быстро.");
			return;
		}

		if(player.getActiveEnchantItem() != null)
		{
			return;
		}

		// Alt game - Reputation punishment
		if(player.hasBadReputation())
		{
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.sendMessage("Нельзя обмениваться вещами в режиме торговли.");
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);

		if(item == null)
		{
			return;
		}

		if(item.isHeroItem() || !item.isDropable() || item.isAugmented() || !item.isDestroyable() || !item.isTradeable())
		{
			player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}

		L2PetInstance pet = (L2PetInstance) player.getItemPet();
		if(pet == null)
		{
			return;
		}

		if(pet.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
			return;
		}

		if(_amount < 0)
		{
			return;
		}
		if(!pet.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
			return;
		}
		if(!pet.getInventory().validateWeight(item, _amount))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
			return;
		}

		if(player.transferItem(ProcessType.PETTRANSFER, _objectId, _amount, pet.getInventory(), pet) == null)
		{
			_log.log(Level.WARN, "Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}

	@Override
	public String getType()
	{
		return "[C] 8B RequestGiveItemToPet";
	}
}
