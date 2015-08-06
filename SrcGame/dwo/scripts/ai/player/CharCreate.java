package dwo.scripts.ai.player;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;
import dwo.scripts.services.Tutorial;
import org.apache.log4j.Level;

public class CharCreate extends Quest
{
	public CharCreate()
	{
		addEventId(HookType.ON_CHAR_CREATE);
	}

	public static void main(String[] args)
	{
		new CharCreate();
	}

	@Override
	public void onCharCreate(L2GameClient client, L2PcInstance newChar)
	{
		WorldManager.getInstance().storeObject(newChar);
		L2PcTemplate template = newChar.getTemplate();
        newChar.setWorldChatPoints(10);
		newChar.addAdena(ProcessType.INIT, Config.STARTING_ADENA, null, false);
		L2CharBaseTemplate.CreationData.StartPoint startPoint = template.getBaseCharTemplate().getCreationData().getRandomStartPoint();
		newChar.setXYZ(startPoint.x(), startPoint.y(), startPoint.z(), false);
		if(Config.ALLOW_NEW_CHARACTER_TITLE)
		{
			newChar.setTitle(Config.NEW_CHARACTER_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		if(Config.STARTING_LEVEL > 1)
		{
			newChar.getStat().addLevel((byte) (Config.STARTING_LEVEL - 1));
		}
		if(Config.STARTING_SP > 0)
		{
			newChar.getStat().addSp(Config.STARTING_SP);
		}

		L2ShortCut shortcut;
		// add attack shortcut
		shortcut = new L2ShortCut(0, 0, 3, 2, 0, 1);
		newChar.getShortcutController().registerShortcut(shortcut);
		// add take shortcut
		shortcut = new L2ShortCut(3, 0, 3, 5, 0, 1);
		newChar.getShortcutController().registerShortcut(shortcut);
		// add sit shortcut
		shortcut = new L2ShortCut(10, 0, 3, 0, 0, 1);
		newChar.getShortcutController().registerShortcut(shortcut);

		if(template.hasInitialEquipment())
		{
			L2ItemInstance item = null;
			for(ItemHolder itemHolder : template.getInitialEquipment())
			{
				switch(itemHolder.getItemLocation())
				{
					case INVENTORY:
						item = newChar.getInventory().addItem(ProcessType.INIT, itemHolder.getId(), itemHolder.getCount(), newChar, null);

						// Делаем ярлык на панельке для Книги Туториала
						if(item.getItemId() == 5588)
						{
							shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), 0, 1);
							newChar.getShortcutController().registerShortcut(shortcut);
						}
						if(item.isEquipable() && itemHolder.isEquipped())
						{
							newChar.getInventory().equipItem(item);
						}
						break;
					case WAREHOUSE:
						item = newChar.getWarehouse().addItem(ProcessType.INIT, itemHolder.getId(), itemHolder.getCount(), newChar, null);
						break;
				}

				if(item == null)
				{
					_log.log(Level.ERROR, getClass().getSimpleName() + ": Could not create item (item == null) during char creation: " + itemHolder);
				}
			}
		}

		for(L2SkillLearn skill : SkillTreesData.getInstance().getAvailableSkills(newChar, newChar.getClassId(), false, true, false))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getSkillId(), skill.getSkillLevel()), true);
			if (newChar.getRace() != Race.Arteas) {
                if (skill.getSkillId() == 1001 || skill.getSkillId() == 1177) {
                    shortcut = new L2ShortCut(1, 0, 2, skill.getSkillId(), skill.getSkillLevel(), 1);
                    newChar.getShortcutController().registerShortcut(shortcut);
                }
                if (skill.getSkillId() == 1216) {
                    shortcut = new L2ShortCut(10, 0, 2, skill.getSkillId(), skill.getSkillLevel(), 1);
                    newChar.getShortcutController().registerShortcut(shortcut);
                }
            }
            else {
                if (skill.getSkillId() == 30001) {
                    shortcut = new L2ShortCut(1, 0, 2, skill.getSkillId(), skill.getSkillLevel(), 1);
                    newChar.getShortcutController().registerShortcut(shortcut);
                }
            }
		}

		if(!Config.DISABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}

		RelationListManager.getInstance().restoreRelationList(newChar.getObjectId());
		newChar.setOnlineStatus(true, false);
		newChar.getLocationController().delete();

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(Tutorial.class);
		Quest q = null;
		if(qs == null)
		{
			q = QuestManager.getInstance().getQuest(Tutorial.class);
		}
		if(q != null)
		{
			q.newQuestState(player).setState(STARTED);
		}
	}
}