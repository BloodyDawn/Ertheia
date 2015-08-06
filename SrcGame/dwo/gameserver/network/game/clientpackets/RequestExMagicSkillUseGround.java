package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.geometry.Point3D;
import org.apache.log4j.Level;

/**
 * @author  -Wooden-
 */

public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
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
			activeChar.sendActionFailed();
			return;
		}

		// Get the L2Skill template corresponding to the skillID received from the client
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

		// Check the validity of the skill
		if(skill != null)
		{
			if(activeChar.isTransformed() && !activeChar.containsAllowedTransformSkill(skill.getId()))
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isDebug())
			{
				activeChar.sendMessage("RequestExMagicSkillUseGround: WorldPostion: " + _z);
			}
			activeChar.setCurrentSkillWorldPosition(new Point3D(_x, _y, _z));

			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
			activeChar.broadcastPacket(new ValidateLocation(activeChar));
			activeChar.broadcastUserInfo();

			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendActionFailed();
			_log.log(Level.WARN, "No skill found with id " + _skillId + " and level " + level + " !!");
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:2F RequestExMagicSkillUseGround";
	}
}