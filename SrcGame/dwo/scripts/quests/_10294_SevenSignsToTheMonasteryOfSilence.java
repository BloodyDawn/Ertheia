package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import org.apache.commons.lang3.ArrayUtils;

public class _10294_SevenSignsToTheMonasteryOfSilence extends Quest
{
	// NPC
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32787;
	private static final int Odd_Globe = 32815;
	private static final int ErisEvilThoughts = 32792;
	private static final int RelicGuardian = 32803;
	private static final int[] WrongBook = {
		32822, 32823, 32824, 32826, 32827, 32828, 32830, 32831, 32832, 32834, 32835, 32836
	};
	private static final int GoodBook1 = 32821;
	private static final int GoodBook2 = 32825; // good 2
	private static final int GoodBook3 = 32829;  // good 3
	private static final int GoodBook4 = 32833; // good 4
	private static final int SolinaEvilThoughts = 32793;
	private static final int JudeVanEtins = 32797;
	private static final int RelicWatcher = 32804;
	private static final int RelicWatcher1 = 32805; // good 2
	private static final int RelicWatcher2 = 32806; // good 3
	private static final int RelicWatcher3 = 32807; // good 4
	private static final int TeleportControlDevice = 32817;
	private static final int TeleportControlDevice1 = 32818;
	private static final int TeleportControlDevice2 = 32819;
	private static final int TeleportControlDevice3 = 32820;

	public _10294_SevenSignsToTheMonasteryOfSilence()
	{
		addTalkId(Odd_Globe);
		addStartNpc(Elcadia);
		addTalkId(Elcadia);
		addTalkId(GoodBook1);
		addTalkId(GoodBook2);
		addTalkId(GoodBook3);
		addTalkId(GoodBook4);
		addTalkId(RelicGuardian);
		addTalkId(ErisEvilThoughts);
		addTalkId(Elcadia_Support);
		addStartNpc(Elcadia_Support);
		addTalkId(SolinaEvilThoughts);
		addTalkId(RelicWatcher);
		addTalkId(RelicWatcher1);
		addTalkId(RelicWatcher2);
		addTalkId(RelicWatcher3);
		addTalkId(JudeVanEtins);
		addFirstTalkId(TeleportControlDevice);
		addFirstTalkId(TeleportControlDevice1);
		addFirstTalkId(TeleportControlDevice2);
		addFirstTalkId(TeleportControlDevice3);
		addTalkId(TeleportControlDevice);
		addTalkId(TeleportControlDevice1);
		addTalkId(TeleportControlDevice2);
		addTalkId(TeleportControlDevice3);
		addTalkId(WrongBook);
	}

	public static void main(String[] args)
	{
		new _10294_SevenSignsToTheMonasteryOfSilence();
	}

	@Override
	public int getQuestId()
	{
		return 10294;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == Elcadia)
		{
			if(event.equalsIgnoreCase("32784-05.html"))
			{
				st.startQuest();
			}
		}
		else if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("32792-03.html"))
			{
				if(!st.isStarted())
				{
					st.setState(STARTED);
				}
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			if(event.equalsIgnoreCase("buff"))
			{
				if(player.isMageClass())
				{
					L2Skill buff1 = SkillTable.getInstance().getInfo(6714, 1);
					if(buff1 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff1);
					}
					L2Skill buff2 = SkillTable.getInstance().getInfo(6721, 1);
					if(buff2 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff2);
					}
					L2Skill buff3 = SkillTable.getInstance().getInfo(6722, 1);
					if(buff3 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff3);
					}
					L2Skill buff4 = SkillTable.getInstance().getInfo(6717, 1);
					if(buff4 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff4);
					}
				}
				else
				{
					L2Skill buff1 = SkillTable.getInstance().getInfo(6714, 1);
					if(buff1 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff1);
					}
					L2Skill buff2 = SkillTable.getInstance().getInfo(6715, 1);
					if(buff2 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff2);
					}
					L2Skill buff3 = SkillTable.getInstance().getInfo(6716, 1);
					if(buff3 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff3);
					}
					L2Skill buff4 = SkillTable.getInstance().getInfo(6717, 1);
					if(buff4 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff4);
					}
				}
			}
			return null;
		}
		else if(npc.getNpcId() == GoodBook1)
		{
			if(event.equalsIgnoreCase("good.html"))
			{
				st.set("good1", "1");
				npc.setDisplayEffect(1);
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_HOLY_BURIAL_GROUND_CLOSING);
				addSpawn(32888, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(27415, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(22125, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(22125, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
			}
		}
		else if(npc.getNpcId() == GoodBook2)
		{
			if(event.equalsIgnoreCase("good.html"))
			{

				st.set("good2", "1");
				npc.setDisplayEffect(1);
				L2Skill skill = SkillTable.getInstance().getInfo(6727, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
		}
		else if(npc.getNpcId() == GoodBook3)
		{
			if(event.equalsIgnoreCase("good.html"))
			{
				st.set("good3", "1");
				npc.setDisplayEffect(1);
				L2Npc support = addSpawn(JudeVanEtins, 85783, -253471, -8320, 65, false, 0, false, player.getInstanceId());
				L2Skill skill = SkillTable.getInstance().getInfo(6729, 1);
				support.setTarget(player);
				support.doCast(skill);
			}
		}
		else if(npc.getNpcId() == GoodBook4)
		{
			if(event.equalsIgnoreCase("good.html"))
			{
				st.set("good4", "1");
				npc.setDisplayEffect(1);
				L2Npc support = addSpawn(SolinaEvilThoughts, 56097, -250576, -6757, 0, false, 0, false, player.getInstanceId());
				L2Skill skill = SkillTable.getInstance().getInfo(6729, 1);
				support.setTarget(player);
				support.doCast(skill);
			}
		}
		else if(npc.getNpcId() == RelicWatcher)
		{
			if(event.equalsIgnoreCase("truexit"))
			{
				return st.getInt("good1") == 1 ? "32804-05.html" : "32804-03.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher1)
		{
			if(event.equalsIgnoreCase("truexit"))
			{
				return st.getInt("good2") == 1 ? "32805-05.html" : "32805-03.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher2)
		{
			if(event.equalsIgnoreCase("truexit"))
			{
				return st.getInt("good3") == 1 ? "32806-05.html" : "32806-03.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher3)
		{
			if(event.equalsIgnoreCase("truexit"))
			{
				return st.getInt("good4") == 1 ? "32807-05.html" : "32807-03.html";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Elcadia)
		{
			if(st.isCompleted())
			{
				return "32784-02.html";
			}
			else if(player.getLevel() < 81)
			{
				return "32784-12.html";
			}
			else if(player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class) == null || !player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class).isCompleted())
			{
				return "32784-12.html";
			}
			else if(st.isCreated())
			{
				return "32784-01.html";
			}
			else if(st.getCond() == 1)
			{
				return "32784-06.html";
			}
		}
		else if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(player.getLevel() < 81)
			{
				return "32784-12.html";
			}
			else if(player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class) == null || !player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class).isCompleted())
			{
				return "32784-12.html";
			}
			else if(st.getCond() < 3 && !player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class).isCompleted())
			{
				return "32792-01.html";
			}
			else if(st.getCond() == 3 && !player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class).isCompleted())
			{
				if(player.isSubClassActive())
				{
					return "32792-09.html";
				}
				else
				{
					st.unset("cond");
					st.unset("good1");
					st.unset("good2");
					st.unset("good3");
					st.unset("good4");
					st.addExpAndSp(25000000, 2500000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "32792-07.html";
				}
			}
			else
			{
				return "32784-02.html";
			}
		}
		else if(npc.getNpcId() == RelicGuardian)
		{
			if(st.getCond() == 2 && st.getInt("good1") == 1 && st.getInt("good2") == 1 && st.getInt("good3") == 1 && st.getInt("good4") == 1)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32803-04.html";
			}
			else
			{
				return st.getCond() == 3 ? "32803-05.html" : "32803-01.html";
			}
		}
		else if(npc.getNpcId() == Odd_Globe)
		{
			if(st.getCond() < 3)
			{
				return "32815-01.html";
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			switch(st.getCond())
			{
				case 1:
					return "32787-01.html";
				case 2:
					return "32787-02.html";
			}
		}
		else if(ArrayUtils.contains(WrongBook, npc.getNpcId()))
		{
			if(st.getCond() == 2)
			{
				return "wrong.html";
			}
		}
		else if(npc.getNpcId() == GoodBook1)
		{
			return st.getInt("good1") == 1 ? "already.html" : "32821-01.html";
		}
		else if(npc.getNpcId() == GoodBook2)
		{
			return st.getInt("good2") == 1 ? "already.html" : "32825-01.html";
		}
		else if(npc.getNpcId() == GoodBook3)
		{
			return st.getInt("good3") == 1 ? "already.html" : "32829-01.html";
		}
		else if(npc.getNpcId() == GoodBook4)
		{
			return st.getInt("good4") == 1 ? "already.html" : "32833-01.html";
		}
		else if(npc.getNpcId() == SolinaEvilThoughts)
		{
			return "32793-01.html";
		}
		else if(npc.getNpcId() == JudeVanEtins)
		{
			return "32797-01.html";
		}
		else if(npc.getNpcId() == RelicWatcher)
		{
			if(st.getCond() < 3)
			{
				return "32804-01.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher1)
		{
			if(st.getCond() < 3)
			{
				return "32805-01.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher2)
		{
			if(st.getCond() < 3)
			{
				return "32806-01.html";
			}
		}
		else if(npc.getNpcId() == RelicWatcher3)
		{
			if(st.getCond() < 3)
			{
				return "32807-01.html";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == TeleportControlDevice)
		{
			return st.getInt("good2") != 1 || st.getInt("good3") != 1 || st.getInt("good4") != 1 ? "passnotdone.html" : "32817.html";
		}
		if(npc.getNpcId() == TeleportControlDevice1)
		{
			return st.getInt("good2") == 1 ? "passdone.html" : "32818.html";
		}
		if(npc.getNpcId() == TeleportControlDevice2)
		{
			return st.getInt("good3") == 1 ? "passdone.html" : "32819.html";
		}
		if(npc.getNpcId() == TeleportControlDevice3)
		{
			return st.getInt("good4") == 1 ? "passdone.html" : "32820.html";
		}
		return null;
	}
}
