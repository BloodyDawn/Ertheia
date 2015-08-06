package dwo.gameserver.util;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SummonItemsData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SummonItem;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Evolve
{
	public static final Logger _log = LogManager.getLogger(Evolve.class);

	public static boolean doEvolve(L2PcInstance player, L2Npc npc, int itemIdtake, int itemIdgive, int petminlvl)
	{
		if(itemIdtake == 0 || itemIdgive == 0 || petminlvl == 0)
		{
			return false;
		}

		if(player.getPets().isEmpty() || player.getPets().size() > 1)
		{
			return false;
		}

		L2Summon summon = player.getItemPet();

		if(summon == null)
		{
			return false;
		}

		L2PetInstance currentPet = (L2PetInstance) summon;

		if(currentPet.isAlikeDead())
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use death pet exploit!", Config.DEFAULT_PUNISH);
			return false;
		}

		L2ItemInstance item = null;
		long petexp = currentPet.getStat().getExp();
		String oldname = currentPet.getName();
		int oldX = currentPet.getX();
		int oldY = currentPet.getY();
		int oldZ = currentPet.getZ();

		L2SummonItem olditem = SummonItemsData.getInstance().getSummonItem(itemIdtake);

		if(olditem == null)
		{
			return false;
		}

		int oldnpcID = olditem.getNpcId();

		if(currentPet.getStat().getLevel() < petminlvl || currentPet.getNpcId() != oldnpcID)
		{
			return false;
		}

		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(itemIdgive);

		if(sitem == null)
		{
			return false;
		}

		int npcID = sitem.getNpcId();

		if(npcID == 0)
		{
			return false;
		}

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

		currentPet.getLocationController().decay();

		//deleting old pet item
		currentPet.destroyControlItem(player, true);

		item = player.getInventory().addItem(ProcessType.EVOLVE, itemIdgive, 1, player, npc);

		//Summoning new pet
		L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, player, item);

		if(petSummon == null)
		{
			return false;
		}

		// Fix for non-linear baby pet exp
		long _minimumexp = petSummon.getStat().getExpForLevel(petminlvl);
		if(petexp < _minimumexp)
		{
			petexp = _minimumexp;
		}

		petSummon.getStat().addExp(petexp);
		petSummon.setCurrentHp(petSummon.getMaxHp());
		petSummon.setCurrentMp(petSummon.getMaxMp());
		petSummon.setCurrentFed(petSummon.getMaxFed());
		petSummon.setTitle(player.getName());
		petSummon.setName(oldname);
		petSummon.setRunning();
		petSummon.store();

		player.addPet(petSummon);

		player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
		player.sendPacket(SystemMessageId.SUMMON_A_PET);
		//L2World.getInstance().storeObject(petSummon);
		petSummon.getLocationController().spawn(oldX, oldY, oldZ);
		petSummon.startFeed();
		item.setEnchantLevel(petSummon.getLevel());

		ThreadPoolManager.getInstance().scheduleGeneral(new EvolveFinalizer(player, petSummon), 900);

		if(petSummon.getCurrentFed() <= 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new EvolveFeedWait(player, petSummon), 60000);
		}
		else
		{
			petSummon.startFeed();
		}

		return true;
	}

	public static boolean doRestore(L2PcInstance player, L2Npc npc, int itemIdtake, int itemIdgive, int petminlvl)
	{
		if(itemIdtake == 0 || itemIdgive == 0 || petminlvl == 0)
		{
			return false;
		}

		L2ItemInstance item = player.getInventory().getItemByItemId(itemIdtake);
		if(item == null)
		{
			return false;
		}

		int oldpetlvl = item.getEnchantLevel();
		if(oldpetlvl < petminlvl)
		{
			oldpetlvl = petminlvl;
		}

		L2SummonItem oldItem = SummonItemsData.getInstance().getSummonItem(itemIdtake);
		if(oldItem == null)
		{
			return false;
		}

		L2SummonItem sItem = SummonItemsData.getInstance().getSummonItem(itemIdgive);
		if(sItem == null)
		{
			return false;
		}

		int npcId = sItem.getNpcId();
		if(npcId == 0)
		{
			return false;
		}

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);

		//deleting old pet item
		L2ItemInstance removedItem = player.getInventory().destroyItem(ProcessType.EVOLVE, item, player, npc);
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(removedItem));

		//Give new pet item
		L2ItemInstance addedItem = player.getInventory().addItem(ProcessType.EVOLVE, itemIdgive, 1, player, npc);

		//Summoning new pet
		L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, player, addedItem);
		if(petSummon == null)
		{
			return false;
		}

		long _maxexp = petSummon.getStat().getExpForLevel(oldpetlvl);

		petSummon.getStat().addExp(_maxexp);
		petSummon.setCurrentHp(petSummon.getMaxHp());
		petSummon.setCurrentMp(petSummon.getMaxMp());
		petSummon.setCurrentFed(petSummon.getMaxFed());
		petSummon.setTitle(player.getName());
		petSummon.setRunning();
		petSummon.store();

		player.addPet(petSummon);

		player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
		player.sendPacket(SystemMessageId.SUMMON_A_PET);
		//L2World.getInstance().storeObject(petSummon);
		petSummon.getLocationController().spawn(player.getX(), player.getY(), player.getZ());
		petSummon.startFeed();
		addedItem.setEnchantLevel(petSummon.getLevel());

		//Inventory update
		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		player.sendPacket(iu);

        player.sendPacket(new ExUserInfoInvenWeight(player));
        player.sendPacket(new ExAdenaInvenCount(player));

		player.broadcastUserInfo();

		WorldManager world = WorldManager.getInstance();
		world.removeObject(removedItem);

		ThreadPoolManager.getInstance().scheduleGeneral(new EvolveFinalizer(player, petSummon), 900);

		if(petSummon.getCurrentFed() <= 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new EvolveFeedWait(player, petSummon), 60000);
		}
		else
		{
			petSummon.startFeed();
		}

		// pet control item no longer exists, delete the pet from the db
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, removedItem.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could'nt restore a pet in Envolve utilites");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		return true;
	}

	static class EvolveFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;

		EvolveFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
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

	static class EvolveFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;

		EvolveFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
				_petSummon.setShowSummonAnimation(false);
			}
			catch(Throwable e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}