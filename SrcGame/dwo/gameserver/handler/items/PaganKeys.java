package dwo.gameserver.handler.items;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;

/**
 * @author chris
 */

public class PaganKeys implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		int itemId = item.getItemId();
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();

		if(!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendActionFailed();
			return false;
		}
		L2DoorInstance door = (L2DoorInstance) target;

		if(!activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			activeChar.sendMessage("Цель слишком далеко.");
			activeChar.sendActionFailed();
			return false;
		}
		if(!activeChar.getAbnormalEffects().isEmpty() || activeChar.isInCombat())
		{
			activeChar.sendMessage("Вы не можете использовать ключ сейчас.");
			activeChar.sendActionFailed();
			return false;
		}

		if(!playable.destroyItem(ProcessType.SKILL, item.getObjectId(), 1, null, false))
		{
			return false;
		}

		int openChance = 35;

		switch(itemId)
		{
			case 9698:
				if(door.getDoorId() == 24220020)
				{
					if(activeChar.getInstanceId() == door.getInstanceId())
					{
						door.openMe();
					}
					else
					{
						InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors().stream().filter(instanceDoor -> instanceDoor.getDoorId() == door.getDoorId()).forEach(L2DoorInstance::openMe);
					}
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
			case 9699:
				if(door.getDoorId() == 24220022)
				{
					if(activeChar.getInstanceId() == door.getInstanceId())
					{
						door.openMe();
					}
					else
					{
						InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors().stream().filter(instanceDoor -> instanceDoor.getDoorId() == door.getDoorId()).forEach(L2DoorInstance::openMe);
					}
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
			case 8056: // Дверь Анаиса
				if(door.getDoorId() == 23150004 || door.getDoorId() == 23150003)
				{
					DoorGeoEngine.getInstance().getDoor(23150003).openMe(60000);
					DoorGeoEngine.getInstance().getDoor(23150004).openMe(60000);
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
			case 8273: //AnteroomKey
				if(door.getDoorId() == 19160002 || door.getDoorId() == 19160003 || door.getDoorId() == 19160004 ||
					door.getDoorId() == 19160005 || door.getDoorId() == 19160006 || door.getDoorId() == 19160007 ||
					door.getDoorId() == 19160008 || door.getDoorId() == 19160009)
				{
					if(Rnd.getChance(openChance))
					{
						door.openMe(60000);
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					}
					else
					{
						activeChar.sendMessage("You failed to open Anterooms Door.");
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
			case 8274: //Chapel key
				if(door.getDoorId() == 19160010 || door.getDoorId() == 19160011)
				{
					DoorGeoEngine.getInstance().getDoor(19160010).openMe(60000);
					DoorGeoEngine.getInstance().getDoor(19160011).openMe(60000);
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
			case 8275: //Key of Darkness
				if(door.getDoorId() == 19160012 || door.getDoorId() == 19160013)
				{
					if(Rnd.getChance(openChance))
					{
						door.openMe(60000);
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					}
					else
					{
						activeChar.sendMessage("Не удалось открыть Дверь Тьмы.");
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Неверная дверь.");
				}
				break;
		}
		return true;
	}
}
