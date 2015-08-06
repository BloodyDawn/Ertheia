package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.02.12
 * Time: 6:56
 */

public class _10301_TheShadowOfFear extends Quest
{
	// Квестовые персонажи
	private static final int LADA = 33100;
	private static final int SLASKI = 32893;
	private static final int LARGE_VERDANT_WILDS = 33489;
	private static final int WISP = 32915;

	// Квестовые предметы
	private static final int GLIMMER_CRYSTAL = 17604;
	private static final int CAPTURED_WISP = 17588;
	private static final int AGATHION_FAIRY = 17380;

	private static final int LADA_LETTER_START = 17725;
	private static final int LADA_LETTER_END = 17819;

	private static final int _crystallSkill = 12011;

	public _10301_TheShadowOfFear()
	{
		addStartNpc(LADA);
		addTalkId(LADA, SLASKI);
		addSkillSeeId(LARGE_VERDANT_WILDS);
		addAttackId(WISP);
		questItemIds = new int[]{CAPTURED_WISP, GLIMMER_CRYSTAL, LADA_LETTER_END};
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new _10301_TheShadowOfFear();
	}

	@Override
	public int getQuestId()
	{
		return 10301;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onAttack(npc, player, damage, isPet);
		}
		if(Rnd.getChance(15) && npc.getNpcId() == WISP && st.getCond() == 2)
		{
			player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1030101), ExShowScreenMessage.TOP_CENTER, 4500));
			st.takeItems(GLIMMER_CRYSTAL, -1);
			st.giveItem(CAPTURED_WISP);
			st.setCond(3);
		}
		return null;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.setState(STARTED);
			qs.setCond(2);
			qs.giveItems(GLIMMER_CRYSTAL, 10);
			return "rada_q10301_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == LADA)
		{
			if(reply == 1)
			{
				return "rada_q10301_04.htm";
			}
			else if(reply == 2)
			{
				return "rada_q10301_05.htm";
			}
			else if(reply == 3 && cond == 2)
			{
				st.giveItems(GLIMMER_CRYSTAL, 5);
				return "rada_q10301_08.htm";
			}
			else if(reply == 4)
			{
				return "rada_q10301_06b.htm";
			}
			else if(reply == 5)
			{
				st.setState(STARTED);
				st.setCond(2);
				st.giveItems(GLIMMER_CRYSTAL, 10);
				return "rada_q10301_06c.htm";
			}
		}
		else if(npcId == SLASKI)
		{
			if(reply == 1 && cond == 3)
			{
				return "elder_slakie_q10301_03.htm";
			}
			else if(reply == 2 && cond == 3)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(CAPTURED_WISP, -1);
				return "elder_slakie_q10301_04.htm";
			}
			else if(reply == 3 && cond == 3)
			{
				return "elder_slakie_q10301_05.htm";
			}
			else if(reply == 4 && cond == 3)
			{
				st.addExpAndSp(26920620, 11389320);
				st.giveAdena(1863420, true);
				st.giveItem(AGATHION_FAIRY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "elder_slakie_q10301_07.htm";
			}
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		L2PcInstance player = st.getPlayer();
		if(npcId == LADA)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "rada_q10301_02.htm";
				case CREATED:
					if(player.getLevel() >= 88)
					{
						return "rada_q10301_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.ONE_TIME);
						return "rada_q10301_06a.htm";
					}
				case STARTED:
					switch(cond)
					{
						case 1:
							st.takeItems(LADA_LETTER_END, -1);
							return "rada_q10301_01.htm";
						case 2:
							return "rada_q10301_07.htm";
						case 3:
							return "rada_q10301_09.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == SLASKI)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "elder_slakie_q10301_01.htm";
				case STARTED:
					if(cond == 3)
					{
						return st.hasQuestItems(CAPTURED_WISP) ? "elder_slakie_q10301_02.htm" : "elder_slakie_q10301_06.htm";
					}
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		QuestState st = caster.getQuestState(getClass());

		if(st == null)
		{
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		}

		if(skill.getId() == _crystallSkill)
		{
			L2Npc whisp = addSpawn(WISP, caster.getLoc(), 0, true, 10000);
			whisp.setShowName(true);
			whisp.setTargetable(true);
			whisp.setTitle(caster.getName());
			whisp.setIsNoAttackingBack(true);
			whisp.setIsImmobilized(true);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(player.getLevel() >= 88 && Rnd.getChance(45))
		{
			QuestState st = player.getQuestState(getClass());
			if(st == null && player.getWarehouse().getCountOf(LADA_LETTER_START) != 0)
			{
				player.getWarehouse().destroyItemByItemId(ProcessType.QUEST, LADA_LETTER_START, player.getWarehouse().getCountOf(LADA_LETTER_START), player, null);
			}
			if(st == null && player.getItemsCount(LADA_LETTER_START) == 0)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(player.getInventory().addItem(ProcessType.QUEST, LADA_LETTER_START, 1, player, null));
				player.sendPacket(iu);
			}
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 88;
	}

	@Override
	public String onStartFromItem(L2PcInstance player)
	{
		if(player.getLevel() < 88)
		{
			return "q10301_request_of_rada_q10301_03.htm";
		}

		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			st = newQuestState(player);
		}

		else if(st.isCompleted())
		{
			return "q10301_request_of_rada_q10301_01.htm";
		}

		st.startQuest();
		st.takeItems(LADA_LETTER_START, -1);
		if(!st.hasQuestItems(LADA_LETTER_END))
		{
			st.giveItem(LADA_LETTER_END);
		}
		return "q10301_request_of_rada_q10301_02.htm";
	}
}
