package dwo.gameserver.model.world.olympiad;

import dwo.config.Config;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;

public class OlympiadManager
{
	private final List<Integer> _nonClassBasedRegisters;
	private final Map<Integer, List<Integer>> _classBasedRegisters;

	private OlympiadManager()
	{
		_nonClassBasedRegisters = new FastList<Integer>().shared();
		_classBasedRegisters = new FastMap<Integer, List<Integer>>().shared();
	}

	public static OlympiadManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public List<Integer> getRegisteredNonClassBased()
	{
		return _nonClassBasedRegisters;
	}

	public Map<Integer, List<Integer>> getRegisteredClassBased()
	{
		return _classBasedRegisters;
	}

	protected List<List<Integer>> hasEnoughRegisteredClassed()
	{
		List<List<Integer>> result = null;
		for(Map.Entry<Integer, List<Integer>> classList : _classBasedRegisters.entrySet())
		{
			if(classList.getValue() != null && classList.getValue().size() >= Config.ALT_OLY_CLASSED)
			{
				if(result == null)
				{
					result = new FastList<>();
				}

				result.add(classList.getValue());
			}
		}
		return result;
	}

	protected boolean hasEnoughRegisteredNonClassed()
	{
		return _nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED;
	}

	protected void clearRegistered()
	{
		_nonClassBasedRegisters.clear();
		_classBasedRegisters.clear();
	}

	public boolean isRegistered(L2PcInstance noble)
	{
		return isRegistered(noble, noble, false);
	}

	private boolean isRegistered(L2PcInstance noble, L2PcInstance player, boolean showMessage)
	{
		Integer objId = noble.getObjectId();
		// party may be already dispersed

		if(_nonClassBasedRegisters.contains(objId))
		{
			if(showMessage)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST).addPcName(noble));
			}
			return true;
		}

		List<Integer> classed = _classBasedRegisters.get(noble.getBaseClassId());
		if(classed != null && classed.contains(objId))
		{
			if(showMessage)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST).addPcName(noble));
			}
			return true;
		}
		return false;
	}

	public boolean isRegisteredInComp(L2PcInstance noble)
	{
		return isRegistered(noble, noble, false) || isInCompetition(noble, noble, false);
	}

	private boolean isInCompetition(L2PcInstance noble, L2PcInstance player, boolean showMessage)
	{
		if(!Olympiad._inCompPeriod)
		{
			return false;
		}

		AbstractOlympiadGame game;
		for(int i = OlympiadGameManager.getInstance().getCountOfStadiums(); --i >= 0; )
		{
			game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
			if(game == null)
			{
				continue;
			}

			if(game.containsParticipant(noble.getObjectId()))
			{
				if(!showMessage)
				{
					return true;
				}

				switch(game.getType())
				{
					case CLASSED:
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST).addPcName(noble));
						break;
					case NON_CLASSED:
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST).addPcName(noble));
						break;
				}
				return true;
			}
		}
		return false;
	}

	/***
	 * Регистрация игрока на Олимпиадных боях
	 * @param player инстанс игрока
	 * @param type тип соревнования
	 * @return {@code true} если игрок удволетворяет условиям регистрации
	 */
	public boolean registerNoble(L2PcInstance player, CompetitionType type)
	{
		if(!Olympiad._inCompPeriod)
		{
			player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(Olympiad.getInstance().getMillisToCompEnd() < 600000)
		{
			player.sendPacket(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
			return false;
		}

		int charId = player.getObjectId();
		if(Olympiad.getInstance().getRemainingWeeklyMatches(charId) < 1)
		{
			player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED);
			return false;
		}

		switch(type)
		{
			case CLASSED:
				if(!checkNoble(player, player))
				{
					return false;
				}

				if(Olympiad.getInstance().getRemainingWeeklyMatchesClassed(charId) < 1)
				{
					player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM);
					return false;
				}

				List<Integer> classed = _classBasedRegisters.get(player.getBaseClassId());
				if(classed != null)
				{
					classed.add(charId);
				}
				else
				{
					classed = new FastList<Integer>().shared();
					classed.add(charId);
					_classBasedRegisters.put(player.getBaseClassId(), classed);
				}

				player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				break;
			case NON_CLASSED:
				if(!checkNoble(player, player))
				{
					return false;
				}

				if(Olympiad.getInstance().getRemainingWeeklyMatchesNonClassed(charId) < 1)
				{
					player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM);
					return false;
				}

				_nonClassBasedRegisters.add(charId);
				player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
				break;
		}
		return true;
	}

	/***
	 * Отмена регистрации игрока на Олимпиадных боях
	 * @param player инстанс игрока-участника
	 * @return {@code true} если отмена регистрации прошла успешно
	 */
	public boolean unRegisterNoble(L2PcInstance player)
	{
		if(!Olympiad._inCompPeriod)
		{
			player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!player.isNoble())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD).addString(player.getName()));
			return false;
		}

		if(!isRegistered(player, player, false))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			return false;
		}

		if(isInCompetition(player, player, false))
		{
			return false;
		}

		Integer objId = player.getObjectId();
		if(_nonClassBasedRegisters.remove(objId))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
			return true;
		}

		List<Integer> classed = _classBasedRegisters.get(player.getBaseClassId());
		if(classed != null && classed.remove(objId))
		{
			_classBasedRegisters.remove(player.getBaseClassId());
			_classBasedRegisters.put(player.getBaseClassId(), classed);

			player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
			return true;
		}
		return false;
	}

	public void removeDisconnectedCompetitor(L2PcInstance player)
	{
		OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadController().getGameId());
		if(task != null && task.isGameStarted())
		{
			task.getGame().handleDisconnect(player);
		}

		if(_nonClassBasedRegisters.remove((Integer) player.getObjectId()))
		{
			return;
		}

		List<Integer> classed = _classBasedRegisters.get(player.getBaseClassId());
		if(classed != null)
		{
			classed.remove((Integer) player.getObjectId());
		}
	}

	/**
	 * @param noble  - checked noble
	 * @param player - messages will be sent to this L2PcInstance
	 * @return true if all requirements are met
	 */
	// TODO: move to the bypass handler after reworking points system
	private boolean checkNoble(L2PcInstance noble, L2PcInstance player)
	{
		if(!noble.isNoble())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD).addPcName(noble));
			return false;
		}

		if(noble.isSubClassActive())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER).addPcName(noble));
			return false;
		}

		if(!noble.isAwakened())
		{
			player.sendMessage("Ваш класс не является пробудившимся. Приходите, когда будете готовы.");
			return false;
		}
		if(Olympiad.getInstance().getWeeklyBattles(noble.getObjectId()) >= 50)
		{
			player.sendMessage("Вы превысили максимальное допустимое количество боев в неделю.");
			return false;
		}

		if(noble.getLevel() < 85)
		{
			player.sendMessage("Для участия в Великой Олимпиаде Ваш уровень должен быть 85 и выше.");
			return false;
		}

		if(noble.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2).addPcName(noble).addItemName(noble.getCursedWeaponEquippedId()));
			return false;
		}

		if(EventManager.isPlayerParticipant(noble))
		{
			player.sendMessage("Вы не можете одновременно принимать участие в играх Великой Олимпиады и в ивентах.");
			return false;
		}

		if(!noble.isInventoryUnder90(true))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT).addPcName(noble));
			return false;
		}

		if(isRegistered(noble, player, true))
		{
			return false;
		}

		if(isInCompetition(noble, player, true))
		{
			return false;
		}

		int points = Olympiad.getInstance().getNoblePoints(noble.getObjectId());
		if(points <= 0)
		{
			NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile(player.getLang(), "olympiad/noble_nopoints1.htm");
			message.replace("%objectId%", String.valueOf(noble.getTargetId()));
			player.sendPacket(message);
			return false;
		}

		return true;
	}

	private static class SingletonHolder
	{
		protected static final OlympiadManager _instance = new OlympiadManager();
	}
}