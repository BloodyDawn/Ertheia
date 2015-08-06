package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WeddingManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.controller.object.InstanceController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionResponse;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SetupGauge;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

import java.util.Collections;

/**
 * Wedding commands handler.
 * Wedding allows: engaging, divorce & go to love action what means teleporting lovely to itself.
 *
 * @author evill33t
 * @author Yorie
 */
public class Wedding extends CommandHandler<String>
{
	@TextCommand
	public boolean engage(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		// check target
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage("You have no one targeted.");
			return false;
		}

		// check if target is a l2pcinstance
		if(!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("You can only ask another player to engage you.");

			return false;
		}

		// check if player is already engaged
		if(activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			if(Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD); // give player a Big Head
				// lets recycle the sevensigns debuffs
				int skillId;

				int skillLevel = 1;

				if(activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}

				skillId = activeChar.isMageClass() ? 4362 : 4361;

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if(activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar);
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				}
			}
			return false;
		}

		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();

		// check if player target himself
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Is there something wrong with you, are you trying to go out with youself?");
			return false;
		}
		if(ptarget.isMarried())
		{
			activeChar.sendMessage("Player already married.");
			return false;
		}
		if(ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already asked by someone else.");
			return false;
		}
		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Player already engaged with someone else.");
			return false;
		}
		if(ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage("Gay marriage is not allowed on this server!");
			return false;
		}
		// check if target has player on friendlist
		if(!RelationListManager.getInstance().isInFriendList(activeChar, ptarget))
		{
			activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
			return false;
		}

		ptarget.setEngageRequest(true, activeChar.getObjectId());
		// $s1
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId()).addString(activeChar.getName() + " is asking to engage you. Do you want to start a new relationship?");
		ptarget.sendPacket(dlg);

		return true;
	}

	@TextCommand
	public boolean divorce(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(activeChar.getPartnerId() == 0)
		{
			return false;
		}

		int _partnerId = activeChar.getPartnerId();
		int _coupleId = activeChar.getCoupleId();
		long AdenaAmount = 0;

		if(activeChar.isMarried())
		{
			activeChar.sendMessage("You are now divorced.");

			AdenaAmount = activeChar.getAdenaCount() / 100 * Config.WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena(ProcessType.EVENT, AdenaAmount, activeChar, null);

		}
		else
		{
			activeChar.sendMessage("You have broken up as a couple.");
		}

		L2PcInstance partner;
		partner = WorldManager.getInstance().getPlayer(_partnerId);

		if(partner != null)
		{
			partner.setPartnerId(0);
			if(partner.isMarried())
			{
				partner.sendMessage("Your spouse has decided to divorce you.");
			}
			else
			{
				partner.sendMessage("Your fiance has decided to break the engagement with you.");
			}

			// give adena
			if(AdenaAmount > 0)
			{
				partner.addAdena(ProcessType.QUEST, AdenaAmount, null, false);
			}
		}

		WeddingManager.getInstance().deleteCouple(_coupleId);
		return true;
	}

	@TextCommand
	public boolean goToLove(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(!activeChar.isMarried())
		{
			activeChar.sendMessage("You're not married.");
			return false;
		}

		if(activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
			log.log(Level.ERROR, "Married but couldn't find parter for " + activeChar.getName());
			return false;
		}

		if(GrandBossManager.getInstance().getZone(activeChar) != null)
		{
			activeChar.sendMessage("You are inside a Boss Zone.");
			return false;
		}
		if(activeChar.isCombatFlagEquipped())
		{
			activeChar.sendMessage("While you are holding a Combat Flag or Territory Ward you can't go to your love!");
			return false;
		}

		L2PcInstance partner = WorldManager.getInstance().getPlayer(activeChar.getPartnerId());

		if(partner == null)
		{
			activeChar.sendMessage("Your partner is not online.");
			return false;
		}

		RestrictionResponse response;

		if(!(response = activeChar.getRestrictionController().check(RestrictionChain.FELL_IN_LOVE, Collections.<RestrictionCheck, Object>singletonMap(RestrictionCheck.PARTICIPATING_SAME_INSTANCE, new InstanceController.SameInstanceCheckEntity(partner)))).passed())
		{
			switch(response.getReason())
			{
				case MARRIED:
					activeChar.sendMessage("You're not married.");
					break;
				case PARTICIPATING_SAME_INSTANCE:
					activeChar.sendMessage("Your partner is in another World!");
					break;
				case IN_BOSS_ZONE:
					activeChar.sendMessage("You are inside a Boss Zone.");
					break;
				case COMBAT_FLAG_EQUPPIED:
					activeChar.sendMessage("While you are holding a Combat Flag or Territory Ward you can't go to your love!");
					break;
				case PRISONER:
					activeChar.sendMessage("You are in Jail!");
					break;
				case PARTICIPATING_DUEL:
					break;
				case PARTICIPATING_OLYMPIAD:
					activeChar.sendMessage("You are in the Olympiad now.");
					break;
				case OBSERVING:
					activeChar.sendMessage("You are in the observation.");
					break;
				case PARTICIPATING_SIEGE:
					activeChar.sendMessage("You are in a siege, you cannot go to your partner.");
					break;
				case IN_NO_SUMMON_FRIEND_ZONE:
					activeChar.sendMessage("You are in area which blocks summoning.");
					break;
				case PARTICIPATING_EVENT:
					activeChar.sendMessage("You are in an event.");
					break;
			}
			return false;
		}

		if(!(response = partner.getRestrictionController().check(RestrictionChain.FELL_IN_LOVE)).passed())
		{
			switch(response.getReason())
			{
				case ONLINE:
					activeChar.sendMessage("Your partner is not online.");
					break;
				case PRISONER:
					activeChar.sendMessage("Your partner is in Jail.");
					break;
				case IN_BOSS_ZONE:
					activeChar.sendMessage("Your partner is inside a Boss Zone.");
					break;
				case PARTICIPATING_OLYMPIAD:
					activeChar.sendMessage("Your partner is in the Olympiad now.");
					break;
				case PARTICIPATING_EVENT:
					activeChar.sendMessage("Your partner is in an event.");
					break;
				case PARTICIPATING_DUEL:
					activeChar.sendMessage("Your partner is in a duel.");
					break;
				case OBSERVING:
					activeChar.sendMessage("Your partner is in the observation.");
					break;
				case PARTICIPATING_SIEGE:
					activeChar.sendMessage("Your partner is in a siege, you cannot go to your partner.");
					break;
				case IN_NO_SUMMON_FRIEND_ZONE:
					activeChar.sendMessage("Your partner is in area which blocks summoning.");
					break;
			}
			return false;
		}

		if(!partner.isOnline())
		{
			activeChar.sendMessage("Your partner is not online.");
			return false;
		}
		if(activeChar.getInstanceId() != partner.getInstanceId())
		{
			activeChar.sendMessage("Your partner is in another World!");
			return false;
		}
		if(partner.isInJail())
		{
			activeChar.sendMessage("Your partner is in Jail.");
			return false;
		}
		if(GrandBossManager.getInstance().getZone(partner) != null)
		{
			activeChar.sendMessage("Your partner is inside a Boss Zone.");
			return false;
		}
		if(partner.getOlympiadController().isParticipating())
		{
			activeChar.sendMessage("Your partner is in the Olympiad now.");
			return false;
		}
		// TODO
		if(!EventManager.onEscapeUse(activeChar))
		{
			activeChar.sendActionFailed();
			return false;
		}
		if(partner.getEventController().isParticipant())
		{
			activeChar.sendMessage("Your partner is in an event.");
			return false;
		}
		if(partner.isInDuel())
		{
			activeChar.sendMessage("Your partner is in a duel.");
			return false;
		}
		if(partner.getObserverController().isObserving())
		{
			activeChar.sendMessage("Your partner is in the observation.");
			return false;
		}
		if(CastleSiegeManager.getInstance().getSiege(partner) != null && CastleSiegeManager.getInstance().getSiege(partner).isInProgress())
		{
			activeChar.sendMessage("Your partner is in a siege, you cannot go to your partner.");
			return false;
		}
		if(partner.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			activeChar.sendMessage("Your partner is in area which blocks summoning.");
			return false;
		}
		if(activeChar.isInJail())
		{
			activeChar.sendMessage("You are in Jail!");
			return false;
		}
		if(activeChar.getOlympiadController().isParticipating())
		{
			activeChar.sendMessage("You are in the Olympiad now.");
			return false;
		}
		if(activeChar.getEventController().isParticipant())
		{
			activeChar.sendMessage("You are in an event.");
			return false;
		}
		if(activeChar.isInDuel())
		{
			activeChar.sendMessage("You are in a duel!");
			return false;
		}
		if(activeChar.getObserverController().isObserving())
		{
			activeChar.sendMessage("You are in the observation.");
			return false;
		}
		if(CastleSiegeManager.getInstance().getSiege(activeChar) != null && CastleSiegeManager.getInstance().getSiege(activeChar).isInProgress())
		{
			activeChar.sendMessage("You are in a siege, you cannot go to your partner.");
			return false;
		}
		if(activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			activeChar.sendMessage("You are in area which blocks summoning.");
			return false;
		}

		int teleportTimer = Config.WEDDING_TELEPORT_DURATION * 1000;

		activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your partner.");
		activeChar.getInventory().reduceAdena(ProcessType.EVENT, Config.WEDDING_TELEPORT_PRICE, activeChar, null);

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();

		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
		activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE_DUAL, teleportTimer));
		//End SoE Animation section

		EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ());
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.forceIsCasting(GameTimeController.getInstance().getGameTicks() + teleportTimer / GameTimeController.MILLIS_IN_TICK);

		return true;
	}

	class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private int _partnerx;
		private int _partnery;
		private int _partnerz;
		private boolean _to7sDungeon;

		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z)
		{
			_activeChar = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
		}

		@Override
		public void run()
		{
			if(_activeChar.isDead())
			{
				return;
			}

			if(CastleSiegeManager.getInstance().getSiege(_partnerx, _partnery, _partnerz) != null && CastleSiegeManager.getInstance().getSiege(_partnerx, _partnery, _partnerz).isInProgress())
			{
				_activeChar.sendMessage("Your partner is in siege, you can't go to your partner.");
				return;
			}

			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			_activeChar.setIsDoubleCastingNow(false);

			try
			{
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch(Exception e)
			{
				log.log(Level.ERROR, "", e);
			}
		}
	}
}
