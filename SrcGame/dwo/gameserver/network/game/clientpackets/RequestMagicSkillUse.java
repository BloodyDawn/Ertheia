package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.controller.player.LocationController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;

public class RequestMagicSkillUse extends L2GameClientPacket
{
    private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_skillId = readD();              // Identifier of the used skill
		_ctrlPressed = readD() != 0;         // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0;         // True if Shift pressed
	}

	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		// Get the level of the used skill
		int level = activeChar.getSkillLevel(_skillId);
		if(level <= 0)
		{
			// Player doesn't know this skill, maybe it's the display Id.
			SkillHolder customSkill = activeChar.getCustomSkills().get(_skillId);
			if(customSkill != null)
			{
				_skillId = customSkill.getSkillId();
				level = customSkill.getSkillLvl();
			}
			else
			{
				activeChar.sendActionFailed();
				return;
			}
		}

		// Get the L2Skill template corresponding to the skillID received from the client
        L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

		// Получаем заменяемый скил если он есть.
		skill = skill.getReplaceableSkills(activeChar);

		// Check the validity of the skill
		if(skill != null)
		{
			if((activeChar.isTransformed() || activeChar.isInStance()) && !activeChar.containsAllowedTransformSkill(skill.getId()))
			{
				activeChar.sendActionFailed();
				return;
			}

			// If Alternate rule Reputation punishment is set to true, forbid skill Return to player with Karma
			if(skill.getSkillType() == L2SkillType.RECALL && activeChar.hasBadReputation())
			{
				return;
			}

			// players mounted on pets cannot use any toggle skills
			if(skill.isToggle() && activeChar.isMounted())
			{
				return;
			}

			// TODO переделать!!
			if(skill.isReplaceableSkills())
			{
				activeChar.addChanceTrigger(skill);
			}

			// Stop if use self-buff (except if on AirShip or Boat).
			if(skill.isMoveStop() && skill.getSkillType() == L2SkillType.BUFF && skill.getTargetType() == L2TargetType.TARGET_SELF && (!activeChar.isInAirShip() || !activeChar.isInBoat() || !activeChar.isInShuttle()))
			{
				LocationController charPos = activeChar.getLocationController();
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(charPos.getX(), charPos.getY(), charPos.getZ(), charPos.getHeading()));
			}

			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendActionFailed();
			_log.log(Level.ERROR, "No skill found with id " + _skillId + " and level " + level + " !!");
		}
	}

	@Override
	public String getType()
	{
		return "[C] 2F RequestMagicSkillUse";
	}
}
