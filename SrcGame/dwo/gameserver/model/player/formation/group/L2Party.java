package dwo.gameserver.model.player.formation.group;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.PcCafePointsManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.duel.DuelManager;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExTacticalSign;
import dwo.gameserver.network.game.serverpackets.packet.party.ExAskModifyPartyLooting;
import dwo.gameserver.network.game.serverpackets.packet.party.ExCloseMPCC;
import dwo.gameserver.network.game.serverpackets.packet.party.ExOpenMPCC;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowAdd;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowDelete;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowAdd;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowAll;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowDelete;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowDeleteAll;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExSetPartyLooting;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import gnu.trove.iterator.TIntObjectIterator;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class L2Party extends AbstractPlayerGroup
{
	private static final Logger _log = LogManager.getLogger(L2Party.class);

	private static final double[] BONUS_EXP_SP = {1, 1.10, 1.20, 1.30, 1.40, 1.50, 2.0};

	private final FastList<L2PcInstance> _members;
	protected L2TIntObjectHashMap<L2Character> _targetSignList;
	private boolean _pendingInvitation;
	private long _pendingInviteTimeout;
	private int _partyLvl;
	private PartyLootType _itemDistribution = PartyLootType.ITEM_LOOTER;
	private int _itemLastLoot;
	private L2CommandChannel _commandChannel;
	private PartyLootType _requestChangeLoot;
	private List<Integer> _changeLootAnswers;
	private long _requestChangeLootTimer;
	private Future<?> _checkTask;
	private Future<?> _positionBroadcastTask;
	private boolean _disbanding;

	/**
	 * Construct a new L2Party object with a single member - the leader.
	 * @param leader the leader of this party
	 * @param itemDistribution the item distribution rule of this party
	 */
	public L2Party(L2PcInstance leader, PartyLootType itemDistribution)
	{
		_members = new FastList<L2PcInstance>().shared();
		_members.add(leader);
		_partyLvl = leader.getLevel();
		_itemDistribution = itemDistribution;
		_targetSignList = new L2TIntObjectHashMap<>();
	}

	/**
	 * Check if another player can start invitation process.
	 * @return {@code true} if this party waits for a response on an invitation, {@code false} otherwise
	 */
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}

	/**
	 * Set invitation process flag and store time for expiration. <br>
	 * Happens when a player joins party or declines to join.
	 * @param val the pending invitation state to set
	 */
	public void setPendingInvitation(boolean val)
	{
		_pendingInvitation = val;
		_pendingInviteTimeout = GameTimeController.getInstance().getGameTicks() + L2PcInstance.REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
	}

	/**
	 * Check if a player invitation request is expired.
	 * @return {@code true} if time is expired, {@code false} otherwise
	 * @see L2PcInstance#isRequestExpired()
	 */
	public boolean isInvitationRequestExpired()
	{
		return !(_pendingInviteTimeout > GameTimeController.getInstance().getGameTicks());
	}

	/**
	 * Get a random member from this party.
	 * @param itemId the ID of the item for which the member must have inventory space
	 * @param target the object of which the member must be within a certain range (must not be null)
	 * @return a random member from this party or {@code null} if none of the members have inventory space for the specified item
	 */
	private L2PcInstance getCheckedRandomMember(int itemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = getMembers().stream().filter(member -> member.getInventory().validateCapacityByItemId(itemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)).collect(Collectors.toCollection(FastList::new));
		if(!availableMembers.isEmpty())
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		return null;
	}

	/**
	 *
	 * @param quest квест
	 * @param cond шаг квеста
	 * @return состояния квеста членов группы, если участник соответсвует cond квеста
	 */
	public FastList<QuestState> getPartyMembersQuestStates(Quest quest, int cond)
	{
		QuestState st;
		FastList<QuestState> states = new FastList<>();
		for(L2PcInstance member : getMembers())
		{
			st = member.getQuestState(quest.getName());
			if(st != null && st.isStarted() && st.getCond() == cond)
			{
				states.add(st);
			}
		}
		return states;
	}

	/**
	 * TODO: Возможно стоит сделать петов отдельным списком в группе, как пати мемберов чтобы постоянно не формировать списки
	 * TODO: а только тогда, когда член группы вызывает\отзывает пета или пета убивают
	 * @return всех питомцев и саммонов группы
	 */
	public FastList<L2Summon> getPartyMembersSummons()
	{
		FastList<L2Summon> allSummons = new FastList<>();
		getMembers().stream().filter(partyMember -> !partyMember.getPets().isEmpty()).forEach(partyMember -> allSummons.addAll(partyMember.getPets()));
		return allSummons;
	}

	/**
	 * @return следующего претендента на лут
	 */
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for(int i = 0; i < getMemberCount(); i++)
		{
			if(++_itemLastLoot >= getMemberCount())
			{
				_itemLastLoot = 0;
			}
			L2PcInstance member;
			try
			{
				member = getMembers().get(_itemLastLoot);
				if(member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				{
					return member;
				}
			}
			catch(Exception ignored)
			{
			}
		}

		return null;
	}

	/**
	 * get next item looter
	 *
	 * @return
	 */
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;

		switch(_itemDistribution)
		{
			case ITEM_RANDOM:
				if(!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if(!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}

		if(looter == null)
		{
			looter = player;
		}
		return looter;
	}

	/**
	 * @param player проверяемый игрок
	 * @return состоит ли игрок в одной партии с указанным игроком
	 */
	public boolean isPartyMemberWith(L2PcInstance player)
	{
		return getMembers().contains(player);
	}

	public void broadcastPacket(SystemMessageId id)
	{
		getMembers().stream().filter(member -> member != null).forEach(member -> member.sendPacket(SystemMessage.getSystemMessage(id)));
	}

	/**
	 * Broadcasts UI update and User Info for new party leader.
	 */
	public void broadcastPacketNewLeader()
	{
		getMembers().stream().filter(member -> member != null).forEach(member -> {
			member.sendPacket(new PartySmallWindowDeleteAll());
			member.sendPacket(new PartySmallWindowAll(member, this));
			member.broadcastUserInfo();
		});
	}

	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
	 */
	public void broadcastPacket(L2PcInstance player, L2GameServerPacket msg)
	{
		getMembers().stream().filter(member -> member != null && member.getObjectId() != player.getObjectId()).forEach(member -> member.sendPacket(msg));
	}

	public L2PcInstance getRandomPartyMember()
	{
		return _members.get(Rnd.get(_members.size()));
	}

	/**
	 * adds new member to party
	 *
	 * @param player
	 */
	public void addPartyMember(L2PcInstance player)
	{
		if(player == null)
		{
			throw new NullPointerException("No null permitted");
		}

		if(getMembers().contains(player))
		{
			return;
		}

		if(_requestChangeLoot != null)
		{
			finishLootRequest(false); // cancel on invite
		}
		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		player.sendPacket(new PartySmallWindowAll(player, this));

		// sends pets/summons of party members
		getMembers().stream().filter(pMember -> pMember != null && !pMember.getPets().isEmpty()).forEach(pMember -> {
			for(L2Summon pet : pMember.getPets())
			{
				player.sendPacket(new ExPartyPetWindowAdd(pet));
			}
		});

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);

		msg = SystemMessage.getSystemMessage(SystemMessageId.C1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastPacket(msg);
		broadcastPacket(new PartySmallWindowAdd(player, this));
		// send the position of all party members to the new party member
		//player.sendPacket(new PartyMemberPosition(this));
		// send the position of the new party member to all party members (except the new one - he knows his own position)
		//broadcastPacket(player, new PartyMemberPosition(this));

		// if member has pet/summon add it to other as well
		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				broadcastPacket(new ExPartyPetWindowAdd(pet));
			}
		}

		//add player to party, adjust party level
		getMembers().add(player);
		if(player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}
		int partySize = getMembers().size();
		// update partySpelled
		// update party icons only
		// Обновляем листы помеченных целей для все партии
		// Выставляем таймеры PARTY_TIME и FULL_PARTY_TIME для статистики
		getMembers().stream().filter(member -> member != null).forEach(member -> {
			member.updateEffectIcons(true); // update party icons only
			member.broadcastUserInfo();
			if(!member.getPets().isEmpty())
			{
				for(L2Summon pet : member.getPets())
				{
					pet.updateEffectIcons();
				}
			}
			// Обновляем листы помеченных целей для все партии
			if(!_targetSignList.isEmpty())
			{
				TIntObjectIterator<L2Character> iter = _targetSignList.iterator();
				while(iter.hasNext())
				{
					iter.advance();
					member.sendPacket(new ExTacticalSign(iter.value(), iter.key()));
				}
			}

			// Выставляем таймеры PARTY_TIME и FULL_PARTY_TIME для статистики
			member.setStartingTimeInParty(System.currentTimeMillis());
			if(partySize == 7)
			{
				member.setStartingTimeInFullParty(System.currentTimeMillis());
			}
		});

		player.updatePartyPosition(true);

		// Если группа находится в командном каннале, шлем его окно.
		if(isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}
	}

	/**
	 * Removes a party member using its name.
	 * @param name player the player to be removed from the party.
	 * @param type the message type {@link PartyExitReason}.
	 */
	public void removePartyMember(String name, PartyExitReason type)
	{
		removePartyMember(getPlayerByName(name), type);
	}

	/**
	 * Removes a party member instance.
	 * @param player the player to be removed from the party.
	 * @param type the message type {@link PartyExitReason}.
	 */
	public void removePartyMember(L2PcInstance player, PartyExitReason type)
	{
		if(getMembers().contains(player))
		{
			// При выходе 1 из участников, останавливаем таймер статистики
			// Удаляемому игроку останавливаем таймер TIME_IN_FULLPARTY и пишем в статистику длительность нахождения в группе
			getMembers().stream().filter(pl -> player.getStartingTimeInFullParty() > 0).forEach(pl -> {
				// Удаляемому игроку останавливаем таймер TIME_IN_FULLPARTY и пишем в статистику длительность нахождения в группе
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					pl.updateWorldStatistic(CategoryType.TIME_IN_FULLPARTY, null, (System.currentTimeMillis() - pl.getStartingTimeInFullParty()) / 1000);
				}
				pl.setStartingTimeInFullParty(0);
			});

			if(player.getStartingTimeInParty() > 0)
			{
				// Удаляемому игроку останавливаем таймер TIME_IN_PARTY и пишем в статистику длительность нахождения в группе
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					player.updateWorldStatistic(CategoryType.TIME_IN_PARTY, null, (System.currentTimeMillis() - player.getStartingTimeInParty()) / 1000);
				}
				player.setStartingTimeInParty(0);
			}

			boolean isLeader = isLeader(player);
			if(!_disbanding)
			{
				if(getMembers().size() == 2 || isLeader && !Config.ALT_LEAVE_PARTY_LEADER && type != PartyExitReason.DISCONNECTED)
				{
					disbandParty();
					return;
				}
			}

			getMembers().remove(player);
			recalculatePartyLevel();

			if(player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}

			try
			{
				if(player.getFusionSkill() != null)
				{
					player.abortCast();
				}

				player.getKnownList().getKnownCharacters().stream().filter(character -> character.getFusionSkill() != null && character.getFusionSkill().getTarget().equals(player)).forEach(L2Character::abortCast);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}

			SystemMessage msg;
			if(type == PartyExitReason.EXPELLED)
			{
				player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY);
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_EXPELLED_FROM_PARTY);
				msg.addString(player.getName());
				broadcastPacket(msg);
			}
			else if(type == PartyExitReason.LEFT || type == PartyExitReason.DISCONNECTED)
			{
				player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_LEFT_PARTY);
				msg.addString(player.getName());
				broadcastPacket(msg);
			}

			// Снимаем персонажу Tactic's Sign's полученные в партии
			if(!_targetSignList.isEmpty())
			{
				for(L2Character cha : _targetSignList.values(new L2Character[_targetSignList.size()]))
				{
					player.sendPacket(new ExTacticalSign(cha, 0));
				}
			}

			//UI update.
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			broadcastPacket(new PartySmallWindowDelete(player));

			if(!player.getPets().isEmpty())
			{
				for(L2Summon pet : player.getPets())
				{
					broadcastPacket(new ExPartyPetWindowDelete(pet));
				}
			}

			// Close the CCInfoWindow
			if(isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}
			if(isLeader && getMembers().size() > 1 && (Config.ALT_LEAVE_PARTY_LEADER || type == PartyExitReason.DISCONNECTED))
			{
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
				msg.addString(getLeader().getName());
				broadcastPacket(msg);
				broadcastPacketNewLeader();
			}
			else if(getMembers().size() == 1)
			{
				if(isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if(_commandChannel.getLeader().getObjectId() == getLeader().getObjectId())
					{
						_commandChannel.disbandChannel();
					}
					else
					{
						_commandChannel.removeParty(this);
					}
				}

				if(getLeader() != null)
				{
					getLeader().setParty(null);
					if(getLeader().isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(getLeader());
					}
				}
				if(_checkTask != null)
				{
					_checkTask.cancel(true);
					_checkTask = null;
				}
				if(_positionBroadcastTask != null)
				{
					_positionBroadcastTask.cancel(false);
					_positionBroadcastTask = null;
				}
				_members.clear();
			}
		}
	}

	/**
	 * Disperse a party and sends a message to all its members.
	 */
	public void disbandParty()
	{
		_disbanding = true;
		if(_members != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_DISPERSED));

			// Сбрасываем все членам пати tacticSign и чистим лист таргетов
			if(!_targetSignList.isEmpty())
			{
				for(L2Character cha : _targetSignList.values(new L2Character[_targetSignList.size()]))
				{
					broadcastPacket(new ExTacticalSign(cha, 0));
				}
				_targetSignList.clear();
			}

			_members.stream().filter(member -> member != null).forEach(member -> removePartyMember(member, PartyExitReason.NONE));
		}
	}

	/**
	 * Change party leader (used for string arguments)
	 * @param name the name of the player to set as the new party leader
	 */
	public void changePartyLeader(String name)
	{
		setLeader(getPlayerByName(name));
	}

	/**
	 * finds a player in the party by name
	 *
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name)
	{
		for(L2PcInstance member : getMembers())
		{
			if(member.getName().equalsIgnoreCase(name))
			{
				return member;
			}
		}
		return null;
	}

	/**
	 * distribute item(s) to party members
	 *
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if(item.getItemId() == PcInventory.ADENA_ID)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem(ProcessType.PARTY, item, player, null);
			return;
		}

		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem(ProcessType.PARTY, item, player, true);

		// Send messages to other party members about reward
		if(item.getCount() > 1)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
			msg.addString(target.getName());
			msg.addItemName(item);
			msg.addItemNumber(item.getCount());
			broadcastPacket(target, msg);
		}
		else
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
			msg.addString(target.getName());
			msg.addItemName(item);
			broadcastPacket(target, msg);
		}
	}

	/**
	 * distribute item(s) to party members
	 *
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, ItemHolder item, boolean spoil, L2Attackable target)
	{
		if(item == null)
		{
			return;
		}

		if(item.getId() == PcInventory.ADENA_ID)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}

		L2PcInstance looter = getActualLooter(player, item.getId(), spoil, target);

		if(ProcessType.SWEEP != null)
		{
			looter.addItem(spoil ? ProcessType.SWEEP : ProcessType.PARTY, item.getId(), item.getCount(), player, true);
		}
		else
		{
			looter.addItem(spoil ? ProcessType.PLUNDER : ProcessType.PARTY, item.getId(), item.getCount(), player, true);
		}

		// Send messages to other aprty members about reward
		if(item.getCount() > 1)
		{
			SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S3_S2) : SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getId());
			msg.addItemNumber(item.getCount());
			broadcastPacket(looter, msg);
		}
		else
		{
			SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S2) : SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getId());
			broadcastPacket(looter, msg);
		}
	}

	/**
	 * distribute adena to party members
	 *
	 * @param adena
	 */
	public void distributeAdena(L2PcInstance player, long adena, L2Character target)
	{
		// Get all the party members
		List<L2PcInstance> membersList = getMembers();

		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> ToReward = FastList.newInstance();
		for(L2PcInstance member : membersList)
		{
			if(!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			ToReward.add(member);
		}

		// Avoid null exceptions, if any
		if(ToReward.isEmpty())
		{
			return;
		}

		// Now we can actually distribute the adena reward
		// (Total adena splitted by the number of party members that are in range and must be rewarded)
		long count = adena / ToReward.size();
		for(L2PcInstance member : ToReward)
		{
			member.addAdena(ProcessType.PARTY, count, player, true);
		}

		FastList.recycle((FastList<?>) ToReward);
	}

	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) </li>
	 * <li>Calculate the Experience and SP reward distribution rate </li>
	 * <li>Add Experience and SP to the L2PcInstance </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 * Exception are L2PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain<BR>
	 *
	 * @param xpReward        The Experience reward to distribute
	 * @param spReward        The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<L2Playable> rewardedMembers, int topLvl, int partyDmg, L2Attackable target)
	{
		L2SummonInstance summon = null;
		List<L2Playable> validMembers = getValidMembers(rewardedMembers, topLvl);

		float penalty;
		double sqLevel;
		double preCalculation;

		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());

		double sqLevelSum = 0;
		for(L2Playable character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}

		float vitalityPoints = target.getVitalityPoints(partyDmg) * Config.RATE_PARTY_XP / validMembers.size();
		boolean useVitalityRate = target.useVitalityRate();

		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		for(L2Character member : rewardedMembers)
		{
			if(member.isDead())
			{
				continue;
			}

			penalty = 0;

			// The L2SummonInstance penalty
			if(!member.getPets().isEmpty())
			{
				for(L2Summon pet : member.getPets())
				{
					if(pet instanceof L2SummonInstance)
					{
						penalty = +((L2SummonInstance) pet).getExpPenalty();
					}
				}
			}
			// Pets that leech xp from the owner (like babypets) do not get rewarded directly
			if(member instanceof L2PetInstance)
			{
				if(((L2PetInstance) member).getPetLevelData().getOwnerExpTaken() > 0)
				{
					continue;
				}
				else // TODO: This is a temporary fix while correct pet xp in party is figured out
				{
					penalty = 0.85f;
				}
			}

			// Calculate and add the EXP and SP reward to the member
			if(validMembers.contains(member))
			{
				sqLevel = member.getLevel() * member.getLevel();
				preCalculation = sqLevel / sqLevelSum * (1 - penalty);

				// Add the XP/SP points to the requested party member
				if(!member.isDead())
				{
					long addexp = Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null));
					int addsp = (int) member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null);
					if(member instanceof L2PcInstance)
					{
						if(member.getSkillLevel(467) > 0)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(467, member.getSkillLevel(467));

							if(skill.getExpNeeded() <= addexp)
							{
								((L2PcInstance) member).absorbSoul(skill, target);
							}
						}
						((L2PcInstance) member).addExpAndSp(addexp, addsp, useVitalityRate);
						if(addexp > 0)
						{
							((L2PcInstance) member).updateVitalityPoints(vitalityPoints, target.getLevel(), true, false);
							if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
							{
								((L2PcInstance) member).updateWorldStatistic(CategoryType.EXP_FROM_MONSTERS, null, (long) (addexp / Config.RATE_XP));
							}
						}
						if(Config.PCBANG_ENABLED)
						{
							PcCafePointsManager.getInstance().givePcCafePoint((L2PcInstance) member, addexp);
						}
					}
					else
					{
						member.addExpAndSp(addexp, addsp);
					}
				}
			}
			else
			{
				member.addExpAndSp(0, 0);
			}
		}
	}

	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for(L2PcInstance member : getMembers())
		{
			if(member == null)
			{
				getMembers().remove(member);
				continue;
			}

			if(member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		_partyLvl = newLevel;
	}

	private List<L2Playable> getValidMembers(List<L2Playable> members, int topLvl)
	{
		List<L2Playable> validMembers = new FastList<>();

		//		Fixed LevelDiff cutoff point
		if(Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			validMembers.addAll(members.stream().filter(member -> topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL).collect(Collectors.toList()));
		}
		//		Fixed MinPercentage cutoff point
		else if(Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for(L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			for(L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if(sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
				{
					validMembers.add(member);
				}
			}
		}
		//		Automatic cutoff method
		else if(Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for(L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			int i = members.size() - 1;
			if(i < 1)
			{
				return members;
			}
			if(i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}

			for(L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if(sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
				{
					validMembers.add(member);
				}
			}
		}
		else if(Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none"))
		{
			validMembers.addAll(members);
		}
		return validMembers;
	}

	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if(i < 1)
		{
			return 1;
		}
		if(i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}

		return BONUS_EXP_SP[i];
	}

	private double getExpBonus(int membersCount)
	{
		return membersCount < 2 ? getBaseExpSpBonus(membersCount) : getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}

	private double getSpBonus(int membersCount)
	{
		return membersCount < 2 ? getBaseExpSpBonus(membersCount) : getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}

	public PartyLootType getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public void requestLootChange(PartyLootType type)
	{
		if(_requestChangeLoot != null)
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
			{
				finishLootRequest(false); // timeout 45sec, guess
			}
			else
			{
				return;
			}
		}
		_requestChangeLoot = type;
		int additionalTime = L2PcInstance.REQUEST_TIMEOUT * 3000;
		_requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
		_changeLootAnswers = FastList.newInstance();
		_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000);
		broadcastPacket(getLeader(), new ExAskModifyPartyLooting(getLeader().getName(), type));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1);
		sm.addSystemString(type.getSysMsgId());
		getLeader().sendPacket(sm);
	}

	public void answerLootChangeRequest(L2PcInstance member, boolean answer)
	{
		synchronized(this)
		{
			if(_requestChangeLoot == null)
			{
				return;
			}
			if(_changeLootAnswers.contains(member.getObjectId()))
			{
				return;
			}
			if(!answer)
			{
				finishLootRequest(false);
				return;
			}
			_changeLootAnswers.add(member.getObjectId());
			if(_changeLootAnswers.size() >= getMemberCount() - 1)
			{
				finishLootRequest(true);
			}
		}
	}

	private void finishLootRequest(boolean success)
	{
		synchronized(this)
		{
			if(_requestChangeLoot == null)
			{
				return;
			}
			if(_checkTask != null)
			{
				_checkTask.cancel(false);
				_checkTask = null;
			}
			if(success)
			{
				broadcastPacket(new ExSetPartyLooting(1, _requestChangeLoot));
				_itemDistribution = _requestChangeLoot;
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_LOOT_CHANGED_S1);
				sm.addSystemString(_requestChangeLoot.getSysMsgId());
				broadcastPacket(sm);
			}
			else
			{
				broadcastPacket(new ExSetPartyLooting(0, PartyLootType.ITEM_LOOTER));
				broadcastPacket(SystemMessageId.PARTY_LOOT_CHANGE_CANCELLED);
			}
			_requestChangeLoot = null;
			FastList.recycle((FastList<?>) _changeLootAnswers);
			_requestChangeLootTimer = 0;
		}
	}

	/**
	 * Замена игрока в партии новым
	 * @param member игрок, которого будем заменять
	 */
	public void tryReplaceMember(L2PcInstance member)
	{
		//TODO: Реализуй меня
	}

	/**
	 * Добавить\заменить значек таргета и заброадкастить его пати
	 * @param sign ID значка
	 * @param target инстанс цели
	 */
	public void addSign(int sign, L2Character target)
	{
		// Не можем маркировать сопартийцев и их саммонов
		if(target instanceof L2Summon && getMembers().contains(((L2Summon) target).getOwner()))
		{
			return;
		}

		synchronized(_targetSignList)
		{
			// Если значек ставящийся уже был установлен на другую цель, предварительно снимаем его с нее
			if(_targetSignList.containsKey(sign))
			{
				boolean removePreviosEqualSign = false;
				TIntObjectIterator<L2Character> iter = _targetSignList.iterator();
				while(iter.hasNext())
				{
					iter.advance();
					if(iter.key() == sign)
					{
						if(!target.equals(iter.value()))
						{
							removePreviosEqualSign = true;
						}
						break;
					}
				}
				if(removePreviosEqualSign)
				{
					L2Character prevTarget = _targetSignList.get(sign);
					broadcastPacket(new ExTacticalSign(prevTarget, 0));
					_targetSignList.remove(sign);
				}
			}
			// Если персонаж уже состоит в списке таргетов, перед тем как его помечать заново - удаляем текущий знак
			if(_targetSignList.containsValue(target))
			{
				int currentSignFortarget = -1;
				TIntObjectIterator<L2Character> iter = _targetSignList.iterator();
				while(iter.hasNext())
				{
					iter.advance();
					if(iter.value().equals(target))
					{
						currentSignFortarget = iter.key();
						break;
					}
				}
				// Если на таргет вешается такой-же знак, который был на нем до этого - просто удаляем его
				if(currentSignFortarget == sign)
				{
					_targetSignList.remove(currentSignFortarget);
					broadcastPacket(new ExTacticalSign(target, 0));
				}
				// Если знак был другой, то вешаем после удаления текущего новый
				else
				{
					_targetSignList.remove(currentSignFortarget);
					_targetSignList.put(sign, target);
					broadcastPacket(new ExTacticalSign(target, sign));
				}
			}
			// Если цель не состоит в списке таргетов, просто добавляем ее
			else
			{
				_targetSignList.put(sign, target);
				TIntObjectIterator<L2Character> iter = _targetSignList.iterator();
				while(iter.hasNext())
				{
					iter.advance();
					broadcastPacket(new ExTacticalSign(iter.value(), iter.key()));
				}
			}
		}
	}

	public L2TIntObjectHashMap<L2Character> getTargetSignList()
	{
		return _targetSignList;
	}

	/**
	 * @return a list of all members of this party
	 */
	@Override
	public List<L2PcInstance> getMembers()
	{
		return _members;
	}

	@Override
	public L2PcInstance getLeader()
	{
		try
		{
			return _members.getFirst();
		}
		catch(NoSuchElementException e)
		{
			return null;
		}
	}

	@Override
	public void setLeader(L2PcInstance player)
	{
		if(player != null && !player.isInDuel())
		{
			if(getMembers().contains(player))
			{
				if(isLeader(player))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
				}
				else
				{
					// Swap party members
					L2PcInstance temp = getLeader();
					int p1 = getMembers().indexOf(player);
					getMembers().set(0, player);
					getMembers().set(p1, temp);

					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastPacket(msg);
					broadcastPacketNewLeader();
					if(isInCommandChannel() && _commandChannel.isLeader(temp))
					{
						_commandChannel.setLeader(getLeader());
						msg = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_C1);
						msg.addString(_commandChannel.getLeader().getName());
						_commandChannel.broadcastPacket(msg);
					}
					if(player.isInPartyMatchRoom())
					{
						PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
			}
		}

	}

	/**
	 * returns number of party members
	 *
	 * @return
	 */
	@Override
	public int getMemberCount()
	{
		return getMembers().size();
	}

	@Override
	public int getLevel()
	{
		return _partyLvl;
	}

	/**
	 * Broadcasts packet to all party member.
	 * @param packet the packet to be broadcasted.
	 */
	@Override
	public void broadcastPacket(L2GameServerPacket packet)
	{
		getMembers().stream().filter(member -> member != null).forEach(member -> member.sendPacket(packet));
	}

	/**
	 * Check whether the leader of this party is the same as the leader of the specified party (which essentially means they're the same group).
	 * @param party the other party to check against
	 * @return {@code true} if this party equals the specified party, {@code false} otherwise
	 */
	public boolean equals(L2Party party)
	{
		return getLeaderObjectId() == party.getLeaderObjectId();
	}

	/**
	 * @param player игрок
	 * @param radius проверяемый радиус
	 * @return список игроков в группе, которые находятся в указанном радиусе
	 */
	public List<L2PcInstance> getMembersInRadius(L2PcInstance player, int radius)
	{
		List<L2PcInstance> temp = new ArrayList<>(7);
		temp.addAll(_members.stream().filter(partyMember -> Util.checkIfInRange(radius, player, partyMember, true)).collect(Collectors.toList()));
		return temp;
	}

	private class ChangeLootCheck implements Runnable
	{
		@Override
		public void run()
		{
			if(System.currentTimeMillis() > _requestChangeLootTimer)
			{
				finishLootRequest(false);
			}
		}
	}
}
