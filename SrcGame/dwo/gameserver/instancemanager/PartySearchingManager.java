package dwo.gameserver.instancemanager;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExWaitWaitingSubStituteInfo;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: Bacek, ANZO
 * Date: 19.10.11
 * Time: 19:55
 */

public class PartySearchingManager
{
	private static final PartySearchManagerHook playerHook = new PartySearchManagerHook();
	private static final Logger _log = LogManager.getLogger(PartySearchingManager.class);
	private static FastMap<Integer, L2PcInstance> _waitingList = new FastMap<Integer, L2PcInstance>().shared();

	public static PartySearchingManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Добавлет игрока в лист ожидания поиска партии
	 * @param player игрок на добавление
	 */
	public void addToWaitingList(L2PcInstance player)
	{
		if(_waitingList.containsKey(player.getObjectId()))
		{
			_log.log(Level.WARN, "PartySearchingManager: Waiting list already contain ObjectID: " + player.getObjectId());
		}
		else
		{
			_waitingList.put(player.getObjectId(), player);
			player.setIsInPartyWaitingList(true);
			player.getHookContainer().addHook(HookType.ON_ENTER_INSTANCE, playerHook);
			player.sendPacket(new ExWaitWaitingSubStituteInfo(1));
			player.sendPacket(SystemMessageId.YOU_ARE_REGISTERED_ON_THE_WAITING_LIST);
		}
	}

	/**
	 * Удаляет из листа ожидания игрока
	 * @param player игрок на удаление
	 * @param fromInstance если игрок удаляется из листа при входе в инст - системное сообщение другое
	 */
	public void deleteFromWaitingList(L2PcInstance player, boolean fromInstance)
	{
		if(_waitingList.containsKey(player.getObjectId()))
		{
			_waitingList.remove(player.getObjectId());
			player.setIsInPartyWaitingList(false);
			player.sendPacket(new ExWaitWaitingSubStituteInfo(0));
			player.getHookContainer().removeHook(HookType.ON_ENTER_INSTANCE, playerHook);
			if(fromInstance)
			{
				player.sendPacket(SystemMessageId.REGISTRATION_WILL_BE_CANCELLED_WHILE_USING_THE_INSTANCE_ZONE);
			}
			else
			{
				player.sendPacket(SystemMessageId.STOPPED_SEARCHING_THE_PARTY);
			}
		}
		else
		{
			_log.log(Level.WARN, "PartySearchingManager: Waiting list did'nt contain ObjectID: " + player.getObjectId() + (fromInstance ? "(Auto from Instance)" : ""));
		}
	}

	/**
	 * Берем игрока из листа по objectId
	 * @param objectId глобальный идентификатор игрока
	 * @return L2PcInstance игрока
	 */
	public L2PcInstance getPlayerFromWaitingList(int objectId)
	{
		return _waitingList.containsKey(objectId) ? _waitingList.get(objectId) : null;
	}

	public FastMap<Integer, L2PcInstance> getWaitingList()
	{
		return _waitingList;
	}

	private static class PartySearchManagerHook extends AbstractHookImpl
	{
		@Override
		public void onEnterInstance(L2PcInstance player, Instance instance)
		{
			getInstance().deleteFromWaitingList(player, true);
		}
	}

	private static class SingletonHolder
	{
		protected static final PartySearchingManager _instance = new PartySearchingManager();
	}
}