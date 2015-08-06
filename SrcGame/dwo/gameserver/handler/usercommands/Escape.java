package dwo.gameserver.handler.usercommands;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.actor.restriction.RestrictionResponse;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SetupGauge;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

/**
 * Escape command handler.
 * The escape is casting about 5 minutes but it's very helpful if player had stuck.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Escape extends CommandHandler<Integer>
{
	@NumericCommand(52)
	public boolean escape(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(!EventManager.onEscapeUse(activeChar))
		{
			activeChar.sendActionFailed();
			return false;
		}

		int unstuckTimer = Config.UNSTUCK_INTERVAL * 1000;

		RestrictionResponse response;
		if(!(response = activeChar.getRestrictionController().check(RestrictionChain.USE_SCROLL)).passed())
		{
			switch(response.getReason())
			{
				case PRISONER:
					activeChar.sendMessage("You can not escape from jail.");
					break;
				case IN_BOSS_ZONE:
					activeChar.sendMessage("You may not use an escape command in a Boss Zone.");
					break;
			}
			return false;
		}

		activeChar.forceIsCasting(GameTimeController.getInstance().getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);

		L2Skill escape = SkillTable.getInstance().getInfo(2099, 1); // 5 minutes escape
		if(Config.UNSTUCK_INTERVAL == 300 && escape != null)
		{
			activeChar.doCast(escape);
			return true;
		}
		if(Config.UNSTUCK_INTERVAL > 100)
		{
			activeChar.sendMessage("You use Escape: " + unstuckTimer / 60000 + " minutes.");
		}
		else
		{
			activeChar.sendMessage("You use Escape: " + unstuckTimer / 1000 + " seconds.");
		}
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();

		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
		activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE_DUAL, unstuckTimer));
		//End SoE Animation section

		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(activeChar.isDead())
			{
				return;
			}

			activeChar.enableAllSkills();
			activeChar.setIsCastingNow(false);
			activeChar.setIsDoubleCastingNow(false);
			activeChar.getInstanceController().setInstanceId(0);

			try
			{
				activeChar.teleToLocation(TeleportWhereType.TOWN);
			}
			catch(Exception e)
			{
				log.log(Level.ERROR, "", e);
			}
		}, unstuckTimer));

		return true;
	}
}