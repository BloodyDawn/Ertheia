package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.acquire.AcquireSkillDone;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import dwo.gameserver.network.game.serverpackets.packet.alchemy.ExAlchemySkillList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStorageMaxCount;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeSkillList;
import dwo.gameserver.util.Util;
import dwo.scripts.npc.town.CertificationMaster;
import dwo.scripts.npc.town.TransformManager;
import dwo.scripts.services.validator.DualClassSkillsValidator;
import dwo.scripts.services.validator.SubClassSkillsValidator;
import org.apache.log4j.Level;

import java.util.List;

/**
 * @author Zoey76
 */
public class RequestAcquireSkill extends L2GameClientPacket
{
    private int _id;
    private int _level;
    private AcquireSkillType _skillType;
    private int _subType;

    /**
     * this displays PledgeSkillList to the player.
     */
    private static void showPledgeSkillList(L2PcInstance player)
    {
        if(player.isClanLeader())
        {
            List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailablePledgeSkills(player.getClan());
            ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Pledge);
            int counts = 0;

            for(L2SkillLearn s : skills)
            {
                asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), s.getSocialClass().ordinal());
                counts++;
            }
            if(counts == 0)
            {
                if(player.getClan().getLevel() < 5)
                {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(5));
                }
                else
                {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(player.getClan().getLevel() + 1));
                }
            }
            else
            {
                player.sendPacket(asl);
            }
        }
        else
        {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(player.getLang(), "pl017.htm");
            player.sendPacket(html);
            player.sendActionFailed();
        }
    }

    @Override
    protected void readImpl()
    {
        _id = readD();
        _level = readD();
        _skillType = AcquireSkillType.getAcquireSkillType(readD());
        if (_skillType == AcquireSkillType.SubPledge)
        {
            _subType = readD();
        }
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null)
        {
            return;
        }

        if(_level < 1 || _level > 1000 || _id < 1 || _id > 32000)
        {
            Util.handleIllegalPlayerAction(activeChar, "Wrong Packet Data in Aquired Skill", Config.DEFAULT_PUNISH);
            _log.log(Level.WARN, "Recived Wrong Packet Data in Aquired Skill - id: " + _id + " level: " + _level + " for " + activeChar);
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
            return;
        }

        // Hack check. Doesn't apply to all Skill Types
        int prevSkillLevel = activeChar.getSkillLevel(_id);
        if(prevSkillLevel > 0 && !(_skillType == AcquireSkillType.Transfer || _skillType == AcquireSkillType.SubPledge))
        {
            if(prevSkillLevel == _level)
            {
                _log.log(Level.WARN, "Player " + activeChar.getName() + " is trying to learn a skill that already knows, Id: " + _id + " level: " + _level + '!');
                return;
            }
            else if(prevSkillLevel != _level - 1)
            {
                // The previous level skill has not been learned.
                activeChar.sendPacket(SystemMessageId.PREVIOUS_LEVEL_SKILL_NOT_LEARNED);
                Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", 0);
                return;
            }
        }

        switch(_skillType)
        {
            case Race:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getRaceActiveSkills(activeChar.isSubClassActive()).get(SkillTable.getSkillHashCode(_id, _level));
                if(checkPlayerSkill(activeChar, trainer, s))
                {
                    giveSkill(activeChar, trainer, 0, skill, false, null);
                }
                break;
            }
            case Transform:
                //If players is learning transformations:
                if(trainer.getNpcId() == TransformManager.master_transformation)
                {
                    // Hack check.
                    if(!TransformManager.canTransform(activeChar))
                    {
                        activeChar.sendPacket(SystemMessageId.NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION);
                        Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without required quests!", 0);
                        return;
                    }

                    L2SkillLearn s = SkillTreesData.getInstance().getTransformSkill(_id, _level);

                    //Required skills:
                    if(!s.getPreReqSkills().isEmpty() && activeChar.getKnownSkill(s.getPreReqSkills().get(0).getSkillId()) == null)
                    {
                        activeChar.sendPacket(SystemMessageId.YOU_MUST_LEARN_ONYX_BEAST_SKILL);
                        return;
                    }

                    if(checkPlayerSkill(activeChar, trainer, s))
                    {
                        giveSkill(activeChar, trainer, 0, skill, false, null);
                    }
                    break;
                }
            case Class:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getClassSkill(_id, _level, activeChar.getClassId());
                if(s != null)
                {
                    List<L2SkillLearn> availableSkills = SkillTreesData.getInstance().getAvailableSkills(activeChar, activeChar.getClassId(), true, false, true);

                    boolean pass = false;
                    for(L2SkillLearn learn : availableSkills)
                    {
                        if(learn.getSkillId() == s.getSkillId() && learn.getSkillLevel() == s.getSkillLevel())
                        {
                            pass = true;
                        }
                    }

                    if(!pass)
                    {
                        _log.warn("Player " + activeChar.getName() + " tries to learn skill, that he does not allowed to learn. Skill ID: " + s.getSkillId() + ", level: " + s.getSkillLevel());
                        return;
                    }

                    List<SkillHolder> skillsToReplace = s.getPrequisiteSkills(activeChar, true);
                    int levelUpSp = s.getLevelUpSp();
                    boolean NeedSkills = false;
                    boolean remove = false;
                    int t = 0;

                    if(skillsToReplace != null)
                    {
                        remove = true;
                        NeedSkills = true;
                        for(SkillHolder sk : skillsToReplace)
                        {
                            L2Skill oldSkill = activeChar.getSkills().get(sk.getSkillId());
                            if(oldSkill != null && oldSkill.getLevel() == sk.getSkillLvl())
                            {
                                t++;
                            }
                        }
                        if(skillsToReplace.size() == t)
                        {
                            NeedSkills = false;
                        }
                    }

                    //TODO: Нужен вывод что нету нужных скилов
                    if(activeChar.getSp() >= levelUpSp && !NeedSkills)
                    {
                        if(checkPlayerSkill(activeChar, null, s))
                        {
                            giveSkill(activeChar, null, levelUpSp, skill, remove, skillsToReplace);
                        }
                    }
                    else
                    {
                        if(NeedSkills)
                        {
                            activeChar.sendMessage("Нет умений для изучения.");
                            return;
                        }
                        else
                        {
                            activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
                            return;
                        }
                    }
                }
                break;
            }
            case Fishing:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getFishingSkill(_id, _level);
                if(checkPlayerSkill(activeChar, trainer, s))
                {
                    giveSkill(activeChar, trainer, 0, skill, false, null);

                    // Показываем заного окно
                    List<Quest> quests = trainer.getTemplate().getEventQuests(Quest.QuestEventType.ON_LEARN_SKILL);
                    if(quests != null)
                    {
                        for(Quest quest : quests)
                        {
                            quest.notifyLearnSkill(trainer, activeChar);
                        }
                    }
                }
                break;
            }
            case Pledge:
            {
                if(!activeChar.isClanLeader())
                {
                    return;
                }

                L2Clan clan = activeChar.getClan();

                int itemId = -1;
                long itemCount = -1;
                int repCost = 100000000;

                L2SkillLearn s = SkillTreesData.getInstance().getPledgeSkill(_id, _level);
                if(s != null)
                {
                    repCost = s.getLevelUpSp();

                    if(clan.getReputationScore() >= repCost)
                    {
                        if(Config.LIFE_CRYSTAL_NEEDED && !s.getRequiredItems().isEmpty())
                        {
                            for(ItemHolder item : s.getRequiredItems())
                            {
                                itemId = item.getId();
                                itemCount = item.getCount();

                                if(itemId > 0 && itemCount > 0)
                                {
                                    if(!activeChar.destroyItemByItemId(ProcessType.CONSUME, itemId, itemCount, trainer, false))
                                    {
                                        //Doesn't have required item.
                                        activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                                        showPledgeSkillList(activeChar);
                                        return;
                                    }
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(itemCount));
                                }
                            }
                        }

                        clan.takeReputationScore(repCost, true);

                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(repCost));

                        clan.addNewSkill(skill);

                        clan.broadcastToOnlineMembers(new PledgeSkillList(clan));

                        activeChar.sendPacket(new AcquireSkillDone());

                        showPledgeSkillList(activeChar);
                    }
                    else
                    {
                        activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
                        showPledgeSkillList(activeChar);
                    }
                    return;
                }
                break;
            }
            case SubPledge:
                if(!activeChar.isClanLeader())
                {
                    return;
                }

                L2Clan clan = activeChar.getClan();

                if(clan.getFortId() == 0 && clan.getCastleId() == 0)
                {
                    return;
                }

                if(trainer.isMyLord(activeChar, true))
                {
                    int itemId = -1;
                    long itemCount = -1;
                    int rep = 100000000;

                    L2SkillLearn s = SkillTreesData.getInstance().getSubPledgeSkill(_id, _level);
                    if(s != null)
                    {
                        //Hack check. Check if SubPledge can accept the new skill:
                        if(!clan.isLearnableSubPledgeSkill(skill, _subType))
                        {
                            activeChar.sendPacket(SystemMessageId.SQUAD_SKILL_ALREADY_ACQUIRED);
                            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", 0);
                            return;
                        }

                        rep = s.getLevelUpSp();

                        if(clan.getReputationScore() < rep)
                        {
                            activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
                            return;
                        }

                        for(ItemHolder item : s.getRequiredItems())
                        {
                            itemId = item.getId();
                            itemCount = item.getCount();

                            if(activeChar.destroyItemByItemId(ProcessType.SKILL, itemId, itemCount, trainer, false))
                            {
                                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(itemCount));
                            }
                            else
                            {
                                activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                                return;
                            }
                        }

                        if(rep > 0)
                        {
                            clan.takeReputationScore(rep, true);
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(rep));
                        }

                        clan.addNewSkill(skill, _subType);

                        clan.broadcastToOnlineMembers(new PledgeSkillList(clan));

                        activeChar.sendPacket(new AcquireSkillDone());

                        // Показываем заного список скилов
                        List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSubPledgeSkills(activeChar.getClan());
                        ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.SubPledge);
                        int count = 0;

                        for(L2SkillLearn sk : skills)
                        {
                            if(SkillTable.getInstance().getInfo(sk.getSkillId(), sk.getSkillLevel()) != null)
                            {
                                asl.addSkill(sk.getSkillId(), sk.getSkillLevel(), sk.getSkillLevel(), sk.getLevelUpSp(), 0);
                                count++;
                            }
                        }

                        if(count == 0)
                        {
                            activeChar.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
                        }
                        else
                        {
                            activeChar.sendPacket(asl);
                        }
                        return;
                    }
                }
                break;
            case Transfer:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getTransferSkill(_id, _level, activeChar.getClassId());
                if(checkPlayerSkill(activeChar, trainer, s))
                {
                    giveSkill(activeChar, trainer, 0, skill, false, null);
                }
                break;
            }
            case SubClass:
            {
                //Hack check.
                if(activeChar.isSubClassActive())
                {
                    activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
                    Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", 0);
                    return;
                }

                L2SkillLearn s = SkillTreesData.getInstance().getSubClassSkill(_id, _level);

                QuestState st = activeChar.getQuestState(SubClassSkillsValidator.class);
                if(st == null)
                {
                    Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(SubClassSkillsValidator.class);
                    if(subClassSkilllsQuest != null)
                    {
                        st = subClassSkilllsQuest.newQuestState(activeChar);
                    }
                    else
                    {
                        _log.log(Level.WARN, "Null SubClassSkills quest, for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                        return;
                    }
                }

                for(String varName : CertificationMaster._questVarNames)
                {
                    for(int i = 1; i <= Config.MAX_SUBCLASS; i++)
                    {
                        String itemOID = st.getGlobalQuestVar(varName + i);
                        if(!itemOID.isEmpty() && !(itemOID.charAt(itemOID.length() - 1) == ';') && !itemOID.equals("0"))
                        {
                            if(Util.isDigit(itemOID))
                            {
                                int itemObjId = Integer.parseInt(itemOID);
                                L2ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjId);
                                if(item != null)
                                {
                                    for(ItemHolder itemIdCount : s.getRequiredItems())
                                    {
                                        if(item.getItemId() == itemIdCount.getId())
                                        {
                                            if(checkPlayerSkill(activeChar, trainer, s))
                                            {
                                                giveSkill(activeChar, trainer, 0, skill, false, null);
                                                // Logging the given skill.
                                                st.saveGlobalQuestVar(varName + i, skill.getId() + ";");
                                            }
                                        }
                                    }
                                    return;
                                }
                                else
                                {
                                    _log.log(Level.WARN, "Inexistent item for object Id " + itemObjId + ", for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                                }
                            }
                            else
                            {
                                _log.log(Level.WARN, "Invalid item object Id for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                            }
                        }
                    }
                }

                //Player doesn't have required item.
                activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                showSkillList(trainer, activeChar);
                break;
            }
            case Dual:
            {
                //Hack check.
                if(activeChar.isSubClassActive())
                {
                    activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
                    Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Dual-Class is active!", 0);
                    return;
                }

                L2SkillLearn s = SkillTreesData.getInstance().getDualClassSkill(_id, _level);

                QuestState st = activeChar.getQuestState(DualClassSkillsValidator.class);
                if(st == null)
                {
                    Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest(DualClassSkillsValidator.class);
                    if(subClassSkilllsQuest != null)
                    {
                        st = subClassSkilllsQuest.newQuestState(activeChar);
                    }
                    else
                    {
                        _log.log(Level.WARN, "Null SubClassSkills quest, for Dual-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                        return;
                    }
                }

                for(String varName : CertificationMaster._questVarNamesDual)
                {
                    String itemOID = st.getGlobalQuestVar(varName);
                    if(!itemOID.isEmpty() && !(itemOID.charAt(itemOID.length() - 1) == ';') && !itemOID.equals("0"))
                    {
                        if(Util.isDigit(itemOID))
                        {
                            int itemObjId = Integer.parseInt(itemOID);
                            L2ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjId);
                            if(item != null)
                            {
                                for(ItemHolder itemIdCount : s.getRequiredItems())
                                {
                                    if(item.getItemId() == itemIdCount.getId())
                                    {
                                        if(checkPlayerSkill(activeChar, trainer, s))
                                        {
                                            giveSkill(activeChar, trainer, 0, skill, false, null);
                                            // Logging the given skill.
                                            long count = itemIdCount.getCount();
                                            int i = 0;
                                            for(String var : CertificationMaster._questVarNamesDual)
                                            {
                                                String OID = st.getGlobalQuestVar(var);
                                                if(!OID.isEmpty() && !(OID.charAt(OID.length() - 1) == ';') && !OID.equals("0") && Util.isDigit(OID))
                                                {
                                                    if(itemOID.equals(OID) && count > i)
                                                    {
                                                        st.saveGlobalQuestVar(var, skill.getId() + ";");
                                                        i++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                return;
                            }
                            else
                            {
                                _log.log(Level.WARN, "Inexistent item for object Id " + itemObjId + ", for Dual-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                            }
                        }
                        else
                        {
                            _log.log(Level.WARN, "Invalid item object Id for Dual-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + '!');
                        }
                    }
                }

                //Player doesn't have required item.
                activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                showSkillList(trainer, activeChar);
                break;
            }
			case Collect:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getCollectSkill(_id, _level);
                if (checkPlayerSkill(activeChar, trainer, s)) {
                    giveSkill(activeChar, trainer, 0, skill, false, null);
                }
                break;
            }
            case Alchemy:
            {
                L2SkillLearn s = SkillTreesData.getInstance().getAlchemySkill(_id, _level);
                if(checkPlayerSkill(activeChar, trainer, s))
                {
                    giveSkill(activeChar, trainer, 0, skill, false, null);

                    // Показываем заного окно
                    List<Quest> quests = trainer.getTemplate().getEventQuests(Quest.QuestEventType.ON_LEARN_SKILL);
                    if(quests != null)
                    {
                        for(Quest quest : quests)
                        {
                            quest.notifyLearnSkill(trainer, activeChar);
                        }
                    }
                }
                break;
            }
			default:
				_log.info("Recived Wrong Packet Data in Aquired Skill, unknown skill type:" + _skillType);
				break;
		}
	}

	@Override
	public String getType()
	{
		return "[C] 7C RequestAcquireSkill";
	}

	/**
	 * Perform a simple check for current player and skill.<br>
	 * Used for skills that require items, not Sp.
	 *
	 * @param player  the skill learning player.
	 * @param trainer the skills teaching Npc.
	 * @param s       the skill to be learn.
	 */
	private boolean checkPlayerSkill(L2PcInstance player, L2Npc trainer, L2SkillLearn s)
	{
		if(s != null && s.getSkillId() == _id && s.getSkillLevel() == _level)
		{
			// Проверка на уровень дуала
			if(s.getMinDualLevel() > 0)
			{
				SubClass dual = player.getDualSubclass();
				if(dual == null || s.getMinDualLevel() > dual.getLevel())
				{
					player.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
					return false;
				}
			}

			// Проверка на хак
			if(s.getMinLevel() > player.getLevel())
			{
				player.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + ", level " + player.getLevel() + " is requesting skill Id: " + _id + " level " + _level + " without having minimum required level, " + s.getMinLevel() + '!', 0);
				return false;
			}

			int itemId = -1;
			long itemCount = -1;

			if(!Config.DIVINE_SP_BOOK_NEEDED && _id == L2Skill.SKILL_DIVINE_INSPIRATION)
			{
				return true;
			}

			// Check for required skills.
			if(!s.getPreReqSkills().isEmpty())
			{
				for(SkillHolder skill : s.getPreReqSkills())
				{
					if(player.getSkillLevel(skill.getSkillId()) != skill.getSkillLvl())
					{
						if(skill.getSkillId() == L2Skill.SKILL_ONYX_BEAST_TRANSFORMATION)
						{
							player.sendPacket(SystemMessageId.YOU_MUST_LEARN_ONYX_BEAST_SKILL);
						}
						else
						{
							// TODO: Find retail message.
							player.sendMessage("You must learn the " + skill.getSkill().getName() + " skill before you can acquire further skills.");
						}
						return false;
					}
				}
			}

			// Проверяем игрока на наличие необходимых для изучения умения предметов
			if(!s.getRequiredItems().isEmpty())
			{

				for(ItemHolder item : s.getRequiredItems())
				{
					itemId = item.getId();
					itemCount = item.getCount();

					if(player.destroyItemByItemId(ProcessType.CONSUME, itemId, itemCount, trainer, false))
					{
						if(itemCount > 1)
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(itemCount));
						}
						else
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
						}
					}
					else
					{
						// У игрока не обнаружено нужного предмета
						player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
						showSkillList(trainer, player);
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param player
	 * @param levelUpSp
	 * @param skill
	 * @param remove
	 * @param skillToReplace
	 */
	private void giveSkill(L2PcInstance player, L2Npc trainer, int levelUpSp, L2Skill skill, boolean remove, List<SkillHolder> skillToReplace)
	{
		// Если скилл требует SP - забираем его у игрока
		if(levelUpSp > 0)
		{
			player.setSp(player.getSp() - levelUpSp);

            UI ui = new UI(player);
            ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
            player.sendPacket(ui);
		}

		//Send message.
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill));
		player.sendPacket(new AcquireSkillDone());
        player.addSkill(skill, true);
        player.sendSkillList(skill.getId());
        
        if(_skillType == AcquireSkillType.Alchemy)
        {
            player.sendPacket(new ExAlchemySkillList(player));
        }

		if(remove)
		{
			for(SkillHolder sk : skillToReplace)
			{
				player.removeSkill(sk.getSkillId());
			}
		}

		// Если умение касается расширения инвентаря, шлем соответсвующий пакет на обновление кол-ва слотов
		if(_id >= 1368 && _id <= 1372)
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}
		updateShortCuts(player);
		showSkillList(trainer, player);
	}

	/**
	 * Updates the shortcut bars with the new acquired skill.
	 *
	 * @param player
	 */
	private void updateShortCuts(L2PcInstance player)
	{
		player.getShortcutController().updateShortcuts(_id, _level, L2ShortCut.ShortcutType.SKILL);
	}

	/**
	 * Wrapper for returning the skill list to the player after it's done with current skill.
	 *
	 * @param trainer the Npc which the {@code player} is interacting.
	 * @param player  the active character.
	 */
    private void showSkillList(L2Npc trainer, L2PcInstance player)
    {
        if(trainer != null)
        {
            if(trainer.getNpcId() == TransformManager.master_transformation && _skillType == AcquireSkillType.Transform)
            {
                TransformManager.showTransformSkillList(player);
            }
            else if(_skillType == AcquireSkillType.SubClass)
            {
                SkillTreesData.showSubClassSkillList(player, false);
            }
            else if(_skillType == AcquireSkillType.Dual)
            {
                SkillTreesData.showSubClassSkillList(player, true);
            }
        }
	}
}