package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionResponse;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Summon Friend/Gate Chant controller.
 *
 * @author Yorie
 */
public class SummonFriendController extends PlayerController
{
	private L2PcInstance summoner;
	private L2Skill skill;

	public SummonFriendController(L2PcInstance player)
	{
		super(player);
	}

	public L2PcInstance getSummoner()
	{
		return summoner;
	}

	public L2Skill getSkill()
	{
		return skill;
	}

	public void clear()
	{
		summoner = null;
		skill = null;
	}

	/**
	 * Sets up summoner for current player.
	 *
	 * @param requester Those who summons.
	 * @param skill Skill, that was used to summon player.
	 */
	public boolean setSummoner(L2PcInstance requester, L2Skill skill)
	{
		if(summoner != null && requester != null)
		{
			return false;
		}

		summoner = requester;
		this.skill = skill;

		return true;
	}

	/**
	 * Teleports current player on summon request accept.
	 *
	 * @param requester Those who requested summon.
	 */
	public void summonMe(L2PcInstance requester)
	{
		if(summoner == null)
		{
			return;
		}

		if(summoner.getObjectId() == requester.getObjectId())
		{
			teleport();
		}

		clear();
	}

	/**
	 * Teleport player to target.
	 */
	public boolean teleport()
	{
		if(summoner == null || skill == null)
		{
			clear();
			return false;
		}

		RestrictionResponse response = player.getRestrictionController().check(RestrictionChain.SUMMON_PLAYER);
		if(!response.passed())
		{
			switch(response.getReason())
			{
				case PARTICIPATING_OLYMPIAD:
					summoner.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
					break;
				case PARTICIPATING_INSTANCE:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA).addString(player.getName()));
					break;
				case OBSERVING:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING).addCharName(player));
					break;
				case IN_NO_SUMMON_FRIEND_ZONE:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA).addString(player.getName()));
					break;
				case DEAD:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addPcName(player));
					break;
				case PARTICIPATING_COMBAT:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addPcName(player));
					break;
				case COMBAT_FLAG_EQUPPIED:
					summoner.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					break;
				case TRADING:
					summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addPcName(player));
					break;
			}

			clear();
			return false;
		}

		response = summoner.getRestrictionController().check(RestrictionChain.SUMMON_PLAYER);
		if(!response.passed())
		{
			switch(response.getReason())
			{
				case PARTICIPATING_OLYMPIAD:
					summoner.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
					break;
				case PARTICIPATING_INSTANCE:
					summoner.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
					break;
				case IN_NO_SUMMON_FRIEND_ZONE:
					summoner.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					break;
			}

			clear();
			return false;
		}

		if(!summoner.getSummonFriendController().checkSummonerStatus())
		{
			clear();
			return false;
		}

		if(!player.getSummonFriendController().checkSummonTargetStatus())
		{
			clear();
			return false;
		}

		if(!skill.consumeTargetItems(summoner, player))
		{
			clear();
			return false;
		}

		player.teleToLocation(summoner.getX(), summoner.getY(), summoner.getZ(), true);
		clear();

		return true;
	}

	/**
	 * The summoner (those who summons another player) status checks.
	 *
	 * The basic conditions are:
	 * - Not in olly;
	 * - Not observing;
	 * - Not inside zone that rejects summoning.
	 */
	public boolean checkSummonerStatus()
	{
		if(!EventManager.onEscapeUse(player))
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		return true;
	}

	/**
	 * Summoning player (those who will be teleported to summoner) checks.
	 *
	 * The basic conditions:
	 * - Not dead;
	 * - Not in store;
	 * - Not rooted and not in combat;
	 * - Not on olly;
	 * - Not observing;
	 * - Not with combat flag;
	 * - Not in zone that rejects summoning;
	 * - Summon target not in instance;
	 * - Summoner in instance where summon is allowed.
	 */
	public boolean checkSummonTargetStatus()
	{
		if(!EventManager.onEscapeUse(player))
		{
			summoner.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		return true;
	}

	/**
	 * @return True if current player is summoner and will summon its target.
	 */
	public boolean isSummoner()
	{
		return summoner != null && summoner.equals(player);
	}

	/**
	 * @return True if current player is summoner target.
	 */
	public boolean isSummonTarget()
	{
		return summoner != null && !summoner.equals(player);
	}
}
