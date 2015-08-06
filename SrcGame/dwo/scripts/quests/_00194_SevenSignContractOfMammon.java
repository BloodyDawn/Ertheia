package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00194_SevenSignContractOfMammon extends Quest
{
	// Квестовые НПЦ
	private static final int ATHEBALDT = 30760;
	private static final int COLIN = 32571;
	private static final int FROG = 32572;
	private static final int TESS = 32573;
	private static final int KUTA = 32574;
	private static final int CLAUDIA = 31001;

	// Квестовые Предметы
	private static final int INTRODUCTION = 13818;
	private static final int FROG_KING_BEAD = 13820;
	private static final int CANDY_POUCH = 13821;
	private static final int NATIVES_GLOVE = 13819;

	// Скилы трансформации и их ID
	private static final int ID_FROG = 6201;
	private static final SkillHolder SKILLFROG = new SkillHolder(ID_FROG, 1);
	private static final int ID_CHILD = 6202;
	private static final SkillHolder SKILLCHILD = new SkillHolder(ID_CHILD, 1);
	private static final int ID_NATIVE = 6203;
	private static final SkillHolder SKILLNATIVE = new SkillHolder(ID_NATIVE, 1);

	public _00194_SevenSignContractOfMammon()
	{
		questItemIds = new int[]{INTRODUCTION, FROG_KING_BEAD, CANDY_POUCH, NATIVES_GLOVE};
		addStartNpc(ATHEBALDT);
		addTalkId(ATHEBALDT, COLIN, FROG, TESS, KUTA, CLAUDIA);
	}

	public static void main(String[] args)
	{
		new _00194_SevenSignContractOfMammon();
	}

	private void transformPlayer(L2Npc npc, L2PcInstance player, SkillHolder skill)
	{
		if(player.isTransformed() || player.isInStance())
		{
			player.untransform(true);
		}
		if(player.isMounted())
		{
			player.dismount();
		}

		for(L2Effect effect : player.getAllEffects())
		{
			if(effect != null && effect.getEffectTemplate() != null)
			{
				for(FuncTemplate func : effect.getEffectTemplate().funcTemplates)
				{
					if(func.stat == Stats.RUN_SPEED)
					{
						effect.exit();
						break;
					}
				}
			}
		}
		npc.setTarget(player);
		npc.doCast(skill.getSkill());
	}

	private boolean checkEffect(L2PcInstance player, int effect)
	{
		return player.getFirstEffect(effect) != null;

	}

	@Override
	public int getQuestId()
	{
		return 194;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == ATHEBALDT)
		{
			if(event.equalsIgnoreCase("30760-02.htm"))
			{
				st.startQuest();
			}
			else if(event.equalsIgnoreCase("movie"))
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				player.showQuestMovie(10);
				return "";
			}
			else if(event.equalsIgnoreCase("30760-07.htm"))
			{
				st.giveItems(INTRODUCTION, 1);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == COLIN)
		{
			if(event.equalsIgnoreCase("32571-04.htm"))
			{
				st.setCond(4);
				st.takeItems(INTRODUCTION, -1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				transformPlayer(npc, player, SKILLFROG);
			}
			else if(event.equalsIgnoreCase("32571-06.htm") || event.equalsIgnoreCase("32571-14.htm") || event.equalsIgnoreCase("32571-22.htm"))
			{
				player.untransform(true);
			}
			else if(event.equalsIgnoreCase("32571-08.htm"))
			{
				transformPlayer(npc, player, SKILLFROG);
			}
			else if(event.equalsIgnoreCase("32571-16.htm"))
			{
				transformPlayer(npc, player, SKILLCHILD);
			}
			else if(event.equalsIgnoreCase("32571-24.htm"))
			{
				transformPlayer(npc, player, SKILLNATIVE);
			}
			else if(event.equalsIgnoreCase("32571-10.htm"))
			{
				st.setCond(6);
				st.takeItems(FROG_KING_BEAD, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(event.equalsIgnoreCase("32571-12.htm"))
			{
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				transformPlayer(npc, player, SKILLCHILD);
			}
			else if(event.equalsIgnoreCase("32571-18.htm"))
			{
				st.setCond(9);
				st.takeItems(CANDY_POUCH, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(event.equalsIgnoreCase("32571-20.htm"))
			{
				st.setCond(10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				transformPlayer(npc, player, SKILLNATIVE);
			}
			else if(event.equalsIgnoreCase("32571-26.htm"))
			{
				st.setCond(12);
				st.takeItems(NATIVES_GLOVE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == FROG && event.equalsIgnoreCase("32572-04.htm"))
		{
			st.setCond(5);
			st.giveItems(FROG_KING_BEAD, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(npc.getNpcId() == TESS && event.equalsIgnoreCase("32573-03.htm"))
		{
			st.setCond(8);
			st.giveItems(CANDY_POUCH, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(npc.getNpcId() == KUTA && event.equalsIgnoreCase("32574-04.htm"))
		{
			st.setCond(11);
			st.giveItems(NATIVES_GLOVE, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(npc.getNpcId() == CLAUDIA && event.equalsIgnoreCase("reward"))
		{
			if(player.getLevel() < 79)
			{
				return "31001-nolvl.htm";
			}
			else
			{
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				else
				{
					st.addExpAndSp(10000000, 2500000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "31001-03.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.getState() == COMPLETED)
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}

		if(npc.getNpcId() == ATHEBALDT)
		{
			if(player.getLevel() < 79)
			{
				return "30760-00.htm";
			}
			else if(player.getQuestState(_00193_SevenSignDyingMessage.class) == null || player.getQuestState(_00193_SevenSignDyingMessage.class).getState() != COMPLETED)
			{
				return "30760-00.htm";
			}
			else if(st.getState() == CREATED)
			{
				return "30760-01.htm";
			}
			else if(st.getCond() == 1)
			{
				return "30760-03.htm";
			}
			else if(st.getCond() == 2)
			{
				return "30760-05.htm";
			}
			else if(st.getCond() == 3)
			{
				return "30760-08.htm";
			}
		}
		else if(npc.getNpcId() == COLIN)
		{
			if(st.getCond() == 3)
			{
				return "32571-01.htm";
			}
			else if(st.getCond() == 4)
			{
				return checkEffect(player, ID_FROG) ? "32571-05.htm" : "32571-07.htm";
			}
			else if(st.getCond() == 5)
			{
				return "32571-09.htm";
			}
			else if(st.getCond() == 6)
			{
				return "32571-11.htm";
			}
			else if(st.getCond() == 7)
			{
				return checkEffect(player, ID_CHILD) ? "32571-13.htm" : "32571-15.htm";
			}
			else if(st.getCond() == 8)
			{
				return "32571-17.htm";
			}
			else if(st.getCond() == 9)
			{
				return "32571-19.htm";
			}
			else if(st.getCond() == 10)
			{
				return checkEffect(player, ID_NATIVE) ? "32571-21.htm" : "32571-23.htm";
			}
			else if(st.getCond() == 11)
			{
				return "32571-25.htm";
			}
			else if(st.getCond() == 12)
			{
				return "32571-27.htm";
			}
		}
		else if(npc.getNpcId() == FROG)
		{
			if(checkEffect(player, ID_FROG))
			{
				if(st.getCond() == 4)
				{
					return "32572-01.htm";
				}
				else if(st.getCond() == 5)
				{
					return "32572-05.htm";
				}
			}
			else
			{
				return "32572-00.htm";
			}
		}
		else if(npc.getNpcId() == TESS)
		{
			if(checkEffect(player, ID_CHILD))
			{
				if(st.getCond() == 7)
				{
					return "32573-01.htm";
				}
				else if(st.getCond() == 8)
				{
					return "32573-04.htm";
				}
			}
			else
			{
				return "32573-00.htm";
			}
		}
		else if(npc.getNpcId() == KUTA)
		{
			if(checkEffect(player, ID_NATIVE))
			{
				if(st.getCond() == 10)
				{
					return "32574-01.htm";
				}
				else if(st.getCond() == 11)
				{
					return "32574-05.htm";
				}
			}
			else
			{
				return "32573-00.htm";
			}
		}
		else if(npc.getNpcId() == CLAUDIA)
		{
			if(st.getCond() == 12)
			{
				return "31001-01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(_00193_SevenSignDyingMessage.class);
		return !(st == null || !st.isCompleted() || player.getLevel() < 79);
	}
}