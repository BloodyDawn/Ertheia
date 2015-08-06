package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleMercTicketManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;

public class RequestPetGetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		WorldManager world = WorldManager.getInstance();
		L2ItemInstance item = (L2ItemInstance) world.findObject(_objectId);
		L2PcInstance player = getClient().getActiveChar();
		if(item == null || player == null)
		{
			return;
		}

		int castleId = CastleMercTicketManager.getInstance().getTicketCastleId(item.getItemId());
		if(castleId > 0)
		{
			player.sendActionFailed();
			return;
		}

		L2PetInstance pet = (L2PetInstance) getClient().getActiveChar().getItemPet();
		if(pet == null || pet.isDead() || pet.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		if(FortSiegeManager.getInstance().isCombatFlag(item.getItemId()))
		{
			player.sendActionFailed();
			return;
		}
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}

	@Override
	public String getType()
	{
		return "[C] 8F RequestPetGetItem";
	}
}
