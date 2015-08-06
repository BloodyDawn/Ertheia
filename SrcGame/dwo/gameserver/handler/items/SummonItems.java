package dwo.gameserver.handler.items;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SummonItemsData;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2XmassTreeInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.skills.L2SummonItem;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SetupGauge;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

import java.util.Collection;

public class SummonItems implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;

		if(!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction(FloodAction.SUMMON_ITEMS))
		{
			return false;
		}

		if(!EventManager.onItemSummon(activeChar))
		{
			return false;
		}

		if(activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return false;
		}

		if(activeChar.getEventController().isInHandysBlockCheckerEventArena())
		{
			return false;
		}

		if(activeChar.getObserverController().isObserving())
		{
			return false;
		}

		if(activeChar.getOlympiadController().isParticipating())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		if(OlympiadManager.getInstance().isRegistered(activeChar))
		{
			activeChar.sendMessage("Вы не можете призывать питомцев, находясь в списке ожидания Олимпийских Игр.");
			return false;
		}

		if(activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
		{
			return false;
		}

		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if((activeChar.hasPet() || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return false;
		}

		if(activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return false;
		}

		int npcId = sitem.getNpcId();
		if(npcId == 0)
		{
			return false;
		}

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if(npcTemplate == null)
		{
			return false;
		}

		activeChar.stopMove(null, false);

		switch(sitem.getType())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					Collection<L2Character> characters = activeChar.getKnownList().getKnownCharactersInRadius(1200);
					for(L2Character ch : characters)
					{
						if(ch instanceof L2XmassTreeInstance && npcTemplate.isSpecialTree())
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return false;
						}
					}

					if(activeChar.destroyItem(ProcessType.SKILL, item.getObjectId(), 1, null, false))
					{
						L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setLocx(activeChar.getX());
						spawn.setLocy(activeChar.getY());
						spawn.setLocz(activeChar.getZ());
						spawn.setInstanceId(activeChar.getInstanceId());
						spawn.stopRespawn();
						L2Npc npc = spawn.spawnOne(true);
						npc.setTitle(activeChar.getName());
						npc.setIsRunning(false); // broadcast info
						if(sitem.getDespawnDelay() > 0)
						{
							npc.scheduleDespawn(sitem.getDespawnDelay());
						}
					}
				}
				catch(Exception e)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				L2Object oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE_DUAL, 5000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				activeChar.setIsCastingNow(true);

				activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000));
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
			case 3: // Great Wolf
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
				break;
		}
		return true;
	}

	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		@Override
		public void run()
		{
			try
			{
				if(_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.getLocationController().decay();
				}
				else
				{
					_petSummon.startFeed();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	// TODO: this should be inside skill handler

	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ItemInstance _item;
		private final L2NpcTemplate _npcTemplate;

		PetSummonFinalizer(L2PcInstance activeChar, L2NpcTemplate npcTemplate, L2ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);

				// check for summon item validity
				if(_item == null || _item.getOwnerId() != _activeChar.getObjectId() || _item.getItemLocation() != L2ItemInstance.ItemLocation.INVENTORY)
				{
					_activeChar.sendMessage("!check for summon item validity");
					return;
				}

				L2PetInstance petSummon = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if(petSummon == null)
				{
					_activeChar.sendMessage("Нельзя вызвать питомца, когда у Вас уже есть призванное существо.");
					return;
				}

				petSummon.setShowSummonAnimation(true);
				petSummon.setTitle(_activeChar.getName());

				if(!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if(!petSummon.isRespawned())
				{
					petSummon.store();
				}

				_activeChar.addPet(petSummon);

				petSummon.getLocationController().spawn(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				petSummon.startFeed();
				_item.setEnchantLevel(petSummon.getLevel());

				if(petSummon.getCurrentFed() <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(_activeChar, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed();
				}

				petSummon.setFollowStatus(true);

				petSummon.sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}