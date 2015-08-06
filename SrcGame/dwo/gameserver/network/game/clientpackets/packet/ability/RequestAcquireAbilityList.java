package dwo.gameserver.network.game.clientpackets.packet.ability;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Bacek
 * Date: 06.05.13
 * Time: 22:03

 */
public class RequestAcquireAbilityList extends L2GameClientPacket
{
    private final List<SkillHolder> _skills = new ArrayList<>();

    @Override
	protected void readImpl()
	{
        readD();
        for (int i = 0; i < 3; i++)
        {
            int size = readD();
            for (int j = 0; j < size; j++)
            {
                _skills.add(new SkillHolder(readD(), readD()));
            }
        }
	}

	@Override
	protected void runImpl()
	{
        L2PcInstance _activeChar = getClient().getActiveChar();

        if (_activeChar == null)
        {
            return;
        }

        if ((_activeChar.getAbilityPoints() == 0) || (_activeChar.getAbilityPoints() == _activeChar.getAbilityPointsUsed()))
        {
            _log.warn(getClass().getSimpleName() + ": Player " + _activeChar + " is trying to learn ability without ability points!");
            return;
        }

        if ((_activeChar.getLevel() < 99) || !_activeChar.isNoble())
        {
            _activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
            return;
        }

        for (SkillHolder holder : _skills)
        {
            L2SkillLearn learn = SkillTreesData.getInstance().getAbilitySkill(holder.getSkillId(), holder.getSkillLvl());
            if (learn == null)
            {
                _log.warn(getClass().getSimpleName() + ": SkillLearn " + holder.getSkillId() + "(" + holder.getSkillLvl() + ") not found!");
                _activeChar.sendActionFailed();
                break;
            }

            L2Skill skill = holder.getSkill();
            if (skill == null)
            {
                _log.warn(getClass().getSimpleName() + ": SkillLearn " + holder.getSkillId() + "(" + holder.getSkillLvl() + ") not found!");
                _activeChar.sendActionFailed();
                break;
            }

            int points;
            int knownLevel = _activeChar.getSkillLevel(holder.getSkillId());

            if (knownLevel == -1)
            {
                points = holder.getSkillLvl();
            }
            else
            {
                points = holder.getSkillLvl() - knownLevel;
            }

            if ((_activeChar.getAbilityPoints() - _activeChar.getAbilityPointsUsed()) < points)
            {
                _log.warn(getClass().getSimpleName() + ": Player " + _activeChar + " is trying to learn ability without ability points!");
                _activeChar.sendActionFailed();
                return;
            }

            _activeChar.addSkill(skill, true);
            _activeChar.setAbilityPointsUsed(_activeChar.getAbilityPointsUsed() + points);
        }
        _activeChar.sendPacket(new ExAcquireAPSkillList(_activeChar));
    }

	@Override
	public String getType()
	{
		return null;
	}
}
