package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.network.game.serverpackets.packet.acquire.AcquireSkillInfo;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquireSkillInfo;
import dwo.scripts.npc.town.TransformManager;
import org.apache.log4j.Level;

public class RequestAcquireSkillInfo extends L2GameClientPacket
{
    private int _id;
    private int _level;
    private AcquireSkillType _skillType;

    @Override
    protected void readImpl()
    {
        _id = readD();
        _level = readD();
        _skillType = AcquireSkillType.getAcquireSkillType(readD());
    }

    @Override
    protected void runImpl()
    {
        if(_id <= 0 || _level <= 0)
        {
            _log.log(Level.WARN, RequestAcquireSkillInfo.class.getSimpleName() + ": Invalid Id: " + _id + " or level: " + _level + '!');
            return;
        }

        L2PcInstance activeChar = getClient().getActiveChar();

        if(activeChar == null)
        {
            return;
        }

        final L2Npc trainer = activeChar.getLastFolkNPC();
        if (!(trainer instanceof L2NpcInstance) && (_skillType != AcquireSkillType.Class))
        {
            return;
        }

        if ((_skillType != AcquireSkillType.Class) && !trainer.canInteract(activeChar) && !activeChar.isGM())
        {
            return;
        }

        L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

        if(skill == null)
        {
            _log.log(Level.WARN, RequestAcquireSkillInfo.class.getSimpleName() + ": Skill Id: " + _id + " level: " + _level + " is undefined. " + RequestAcquireSkillInfo.class.getName() + " failed.");
            return;
        }

        int prevSkillLevel = activeChar.getSkillLevel(_id);
        if(prevSkillLevel > 0 && !(_skillType == AcquireSkillType.Transfer || _skillType == AcquireSkillType.SubPledge))
        {
            if(prevSkillLevel == _level)
            {
                _log.log(Level.WARN, RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + activeChar.getName() + " is trequesting info for a skill that already knows, Id: " + _id + " level: " + _level + '!');
            }
            else if(prevSkillLevel != _level - 1)
            {
                _log.log(Level.WARN, RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + activeChar.getName() + " is requesting info for skill Id: " + _id + " level " + _level + " without knowing it's previous level!");
            }
        }

        switch(_skillType)
        {
            case Race:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getRaceActiveSkills(activeChar.isSubClassActive()).get(SkillTable.getSkillHashCode(_id, _level));
                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, s.getLevelUpSp(), AcquireSkillType.Race);

                    if(!s.getRequiredItems().isEmpty())
                    {
                        for(ItemHolder item : s.getRequiredItems())
                        {
                            itemId = item.getId();
                            itemCount = (int) item.getCount();

                            if(itemId > 0 && itemCount > 0)
                            {
                                asi.addRequirement(0, itemId, itemCount, 0);
                            }
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case Transform: {
                if (trainer.getNpcId() == TransformManager.master_transformation) {
                    L2SkillLearn s = SkillTreesData.getInstance().getTransformSkill(_id, _level);

                    if (s != null) {
                        int itemId = -1;
                        int itemCount = -1;

                        AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Transform);
                        if (!s.getRequiredItems().isEmpty()) {
                            for (ItemHolder item : s.getRequiredItems()) {
                                itemId = item.getId();
                                itemCount = (int) item.getCount();

                                if (itemId > 0 && itemCount > 0) {
                                    asi.addRequirement(99, itemId, itemCount, 50);
                                }
                            }
                        }
                        sendPacket(asi);
                    }
                    return;
                }
                break;
            }
            case Class:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getClassSkill(_id, _level, activeChar.getClassId());
                if(s != null)
                {
                    ExAcquireSkillInfo asi = new ExAcquireSkillInfo(_id, skill.getLevel(), s.getLevelUpSp(), 0, s.getPrequisiteSkills(activeChar, true), s.getRequiredItems());
                    sendPacket(asi);
                }
                break;
            }
            case Fishing:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getFishingSkill(_id, _level);
                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Fishing);
                    if(!s.getRequiredItems().isEmpty())
                    {
                        for(ItemHolder item : s.getRequiredItems())
                        {
                            itemId = item.getId();
                            itemCount = (int) item.getCount();

                            if(itemId > 0 && itemCount > 0)
                            {
                                asi.addRequirement(4, itemId, itemCount, 0);
                            }
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case Pledge:
            {
                if(!activeChar.isClanLeader())
                {
                    return;
                }

                L2SkillLearn s = SkillTreesData.getInstance().getPledgeSkill(_id, _level);
                if(s != null)
                {
                    int requiredRep = s.getLevelUpSp();
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, requiredRep, AcquireSkillType.Pledge);

                    if(Config.LIFE_CRYSTAL_NEEDED)
                    {
                        if(!s.getRequiredItems().isEmpty())
                        {
                            for(ItemHolder item : s.getRequiredItems())
                            {
                                itemId = item.getId();
                                itemCount = (int) item.getCount();

                                if(itemId > 0 && itemCount > 0)
                                {
                                    asi.addRequirement(1, itemId, itemCount, 0);
                                }
                            }
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case SubPledge: {
                if (!activeChar.isClanLeader()) {
                    return;
                }

                if (trainer.isMyLord(activeChar, true)) {
                    L2SkillLearn s = SkillTreesData.getInstance().getSubPledgeSkill(_id, _level);
                    if (s != null) {
                        int itemId = -1;
                        int itemCount = -1;

                        AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, s.getLevelUpSp(), AcquireSkillType.SubPledge);

                        if (!s.getRequiredItems().isEmpty()) {
                            for (ItemHolder item : s.getRequiredItems()) {
                                itemId = item.getId();
                                itemCount = (int) item.getCount();

                                if (itemId > 0 && itemCount > 0) {
                                    asi.addRequirement(0, itemId, itemCount, 0);
                                }
                            }
                        }
                        sendPacket(asi);
                    }
                }
                break;
            }
            case SubClass:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getSubClassSkill(_id, _level);

                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.SubClass);

                    for(ItemHolder item : s.getRequiredItems())
                    {
                        itemId = item.getId();
                        itemCount = (int) item.getCount();

                        if(itemId > 0 && itemCount > 0)
                        {
                            asi.addRequirement(5, itemId, itemCount, 1);
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case Dual:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getDualClassSkill(_id, _level);

                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Dual);

                    for(ItemHolder item : s.getRequiredItems())
                    {
                        itemId = item.getId();
                        itemCount = (int) item.getCount();

                        if(itemId > 0 && itemCount > 0)
                        {
                            asi.addRequirement(5, itemId, itemCount, 1);
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case Collect:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getCollectSkill(_id, _level);

                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Collect);

                    for(ItemHolder item : s.getRequiredItems())
                    {
                        itemId = item.getId();
                        itemCount = (int) item.getCount();

                        if(itemId > 0 && itemCount > 0)
                        {
                            asi.addRequirement(6, itemId, itemCount, 0);
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
            case Transfer:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getTransferSkill(_id, _level, activeChar.getClassId());
                if (s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Transfer);

                    for (ItemHolder item : s.getRequiredItems())
                    {
                        itemId = item.getId();
                        itemCount = (int) item.getCount();

                        if (itemId > 0 && itemCount > 0)
                        {
                            asi.addRequirement(4, itemId, itemCount, 0);
                        }
                    }
                    sendPacket(asi);
                }
                else
                {
                    _log.log(Level.WARN, RequestAcquireSkillInfo.class.getSimpleName() + ": Null L2SkillLearn for id: " + _id + " and level " + _level + " in Transfer Skill Tree for skill learning class " + activeChar.getClassId() + '!');
                }
                break;
            }
            case Alchemy:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getAlchemySkill(_id, _level);
                if(s != null)
                {
                    int itemId = -1;
                    int itemCount = -1;

                    AcquireSkillInfo asi = new AcquireSkillInfo(_id, _level, 0, AcquireSkillType.Alchemy);
                    if(!s.getRequiredItems().isEmpty())
                    {
                        for(ItemHolder item : s.getRequiredItems())
                        {
                            itemId = item.getId();
                            itemCount = (int) item.getCount();

                            if(itemId > 0 && itemCount > 0)
                            {
                                asi.addRequirement(99, itemId, itemCount, 50);
                            }
                        }
                    }
                    sendPacket(asi);
                }
                break;
            }
        }
    }

    @Override
    public String getType()
    {
        return "[C] 6B RequestAcquireSkillInfo";
    }
}
