package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import org.apache.commons.lang3.ArrayUtils;

public class _10295_SevenSignsSolinasTomb extends Quest
{
	// NPC
	private static final int Elcadia_Support = 32787;
	private static final int Odd_Globe = 32815;
	private static final int ErisEvilThoughts = 32792;
	private static final int SolinasEvilThoughts = 32793;
	private static final int MovementControlDevice = 32837;
	private static final int TeleportControlDevice = 32842;
	private static final int TeleportControlDevice2 = 32844;
	private static final int Tomb = 32843;

	private static final int PowerfulDevice1 = 32838;
	private static final int PowerfulDevice2 = 32839;
	private static final int PowerfulDevice3 = 32840;
	private static final int PowerfulDevice4 = 32841;

	private static final int AltarOfHallows1 = 32857;
	private static final int AltarOfHallows2 = 32858;
	private static final int AltarOfHallows3 = 32859;
	private static final int AltarOfHallows4 = 32860;
	// Mobs
	private static final int SolinasGuardian1 = 18952;
	private static final int SolinasGuardian2 = 18953;
	private static final int SolinasGuardian3 = 18954;
	private static final int SolinasGuardian4 = 18955;

	private static final int[] _boss = {SolinasGuardian1, SolinasGuardian2, SolinasGuardian3, SolinasGuardian4};

	private static final int GuardiaOfTheTomb1 = 18956;
	private static final int GuardiaOfTheTomb2 = 18957;
	private static final int GuardiaOfTheTomb3 = 18958;
	private static final int GuardiaOfTheTomb4 = 18959;

	private static final int[] _guard = {GuardiaOfTheTomb1, GuardiaOfTheTomb2, GuardiaOfTheTomb3, GuardiaOfTheTomb4};

	// Items
	private static final int ScrollOfAbstinence = 17228;
	private static final int ShieldOfSacrifice = 17229;
	private static final int SwordOfHolySpirit = 17230;
	private static final int StaffOfBlessing = 17231;

	private static final int[] MageBuff = {6725, 6721, 6722, 6717};
	private static final int[] FighterBuff = {6714, 6715, 6716, 6717};

	public _10295_SevenSignsSolinasTomb()
	{
		questItemIds = new int[]{ScrollOfAbstinence, ShieldOfSacrifice, SwordOfHolySpirit, StaffOfBlessing};
		addStartNpc(ErisEvilThoughts);
		addTalkId(ErisEvilThoughts);
		addStartNpc(Odd_Globe);
		addTalkId(Odd_Globe);
		addTalkId(Elcadia_Support);
		addTalkId(PowerfulDevice1);
		addTalkId(PowerfulDevice2);
		addTalkId(PowerfulDevice3);
		addTalkId(PowerfulDevice4);
		addTalkId(AltarOfHallows1);
		addTalkId(AltarOfHallows2);
		addTalkId(AltarOfHallows3);
		addTalkId(AltarOfHallows4);
		addTalkId(MovementControlDevice);
		addKillId(SolinasGuardian1);
		addKillId(SolinasGuardian2);
		addKillId(SolinasGuardian3);
		addKillId(SolinasGuardian4);
		addKillId(GuardiaOfTheTomb1);
		addKillId(GuardiaOfTheTomb2);
		addKillId(GuardiaOfTheTomb3);
		addKillId(GuardiaOfTheTomb4);
		addTalkId(TeleportControlDevice);
		addTalkId(TeleportControlDevice2);
		addTalkId(Tomb);
		addTalkId(SolinasEvilThoughts);
	}

	public static void main(String[] args)
	{
		new _10295_SevenSignsSolinasTomb();
	}

	@Override
	public int getQuestId()
	{
		return 10295;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("ok"))
			{
				st.startQuest();
				return "32792-07.html";
			}
		}
		else if(npc.getNpcId() == Elcadia_Support)
		{
			if(event.equalsIgnoreCase("buff"))
			{
				if(player.isMageClass())
				{
					for(int h : MageBuff)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(SkillTable.getInstance().getInfo(h, 1));
					}
				}
				else
				{
					for(int h : FighterBuff)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(SkillTable.getInstance().getInfo(h, 1));
					}
				}
			}
			return null;
		}
		else if(npc.getNpcId() == AltarOfHallows1)
		{
			if(event.equalsIgnoreCase("take"))
			{
				if(st.hasQuestItems(StaffOfBlessing))
				{
					return "32857-03.html";
				}
				else
				{
					st.giveItems(StaffOfBlessing, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32857-02.html";
				}
			}
		}
		else if(npc.getNpcId() == AltarOfHallows2)
		{
			if(event.equalsIgnoreCase("take"))
			{
				if(st.hasQuestItems(SwordOfHolySpirit))
				{
					return "32858-03.html";
				}
				else
				{
					st.giveItems(SwordOfHolySpirit, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32858-02.html";
				}
			}
		}
		else if(npc.getNpcId() == AltarOfHallows3)
		{
			if(event.equalsIgnoreCase("take"))
			{
				if(st.hasQuestItems(ScrollOfAbstinence))
				{
					return "32859-03.html";
				}
				else
				{
					st.giveItems(ScrollOfAbstinence, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32859-02.html";
				}
			}
		}
		else if(npc.getNpcId() == AltarOfHallows4)
		{
			if(event.equalsIgnoreCase("take"))
			{
				if(st.hasQuestItems(ShieldOfSacrifice))
				{
					return "32860-03.html";
				}
				else
				{
					st.giveItems(ShieldOfSacrifice, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32860-02.html";
				}
			}
		}
		else if(npc.getNpcId() == SolinasEvilThoughts)
		{
			if(event.equalsIgnoreCase("cond3"))
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "32793-08.html";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(npc.getNpcId() == SolinasGuardian1)
		{
			if(st.getInt("boss1") != 1)
			{
				st.set("boss1", "1");
			}
		}
		else if(npc.getNpcId() == SolinasGuardian2)
		{
			if(st.getInt("boss2") != 1)
			{
				st.set("boss2", "1");
			}
		}
		else if(npc.getNpcId() == SolinasGuardian3)
		{
			if(st.getInt("boss3") != 1)
			{
				st.set("boss3", "1");
			}
		}
		else if(npc.getNpcId() == SolinasGuardian4)
		{
			if(st.getInt("boss4") != 1)
			{
				st.set("boss4", "1");
			}
		}
		else if(npc.getNpcId() == GuardiaOfTheTomb1)
		{
			if(st.getInt("guard1") != 1)
			{
				st.set("guard1", "1");
			}
		}
		else if(npc.getNpcId() == GuardiaOfTheTomb2)
		{
			if(st.getInt("guard2") != 1)
			{
				st.set("guard2", "1");
			}
		}
		else if(npc.getNpcId() == GuardiaOfTheTomb3)
		{
			if(st.getInt("guard3") != 1)
			{
				st.set("guard3", "1");
			}
		}
		else if(npc.getNpcId() == GuardiaOfTheTomb4)
		{
			if(st.getInt("guard4") != 1)
			{
				st.set("guard4", "1");
			}
		}
		if(ArrayUtils.contains(_boss, npc.getNpcId()) && st.getInt("boss1") == 1 && st.getInt("boss2") == 1 && st.getInt("boss3") == 1 && st.getInt("boss4") == 1)
		{
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_SOLINA_TOMB_CLOSING);
		}
		if(ArrayUtils.contains(_guard, npc.getNpcId()) && st.getInt("guard1") == 1 && st.getInt("guard2") == 1 && st.getInt("guard3") == 1 && st.getInt("guard4") == 1)
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(st.isCompleted())
			{
				return "32792-02.html";
			}
			else if(player.getLevel() < 81)
			{
				return "32792-12.html";
			}
			else if(player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class) == null || !player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class).isCompleted())
			{
				return "32792-12.html";
			}
			else if(st.isCreated())
			{
				return "32792-01.html";
			}
			else if(st.getCond() < 3)
			{
				return "32792-08.html";
			}
			else if(st.getCond() == 3)
			{
				if(player.isSubClassActive())
				{
					return "32792-12.html";
				}
				else
				{
					st.unset("cond");
					st.unset("boss1");
					st.unset("boss2");
					st.unset("boss3");
					st.unset("boss4");
					st.unset("guard1");
					st.unset("guard2");
					st.unset("guard3");
					st.unset("guard4");
					st.addExpAndSp(125000000, 12500000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "32792-09.html";
				}
			}
		}
		else if(npc.getNpcId() == Odd_Globe)
		{
			return "32815-01.html";
		}
		else if(npc.getNpcId() == MovementControlDevice)
		{
			return st.getInt("boss1") != 1 || st.getInt("boss2") != 1 || st.getInt("boss3") != 1 || st.getInt("boss4") != 1 ? "32837-01.html" : "32837-02.html";
		}
		else if(npc.getNpcId() == AltarOfHallows1)
		{
			return "32857-01.html";
		}
		else if(npc.getNpcId() == AltarOfHallows2)
		{
			return "32858-01.html";
		}
		else if(npc.getNpcId() == AltarOfHallows3)
		{
			return "32859-01.html";
		}
		else if(npc.getNpcId() == AltarOfHallows4)
		{
			return "32860-01.html";
		}
		else if(npc.getNpcId() == PowerfulDevice1)
		{
			return "32838-01.html";
		}
		else if(npc.getNpcId() == PowerfulDevice2)
		{
			return "32839-01.html";
		}
		else if(npc.getNpcId() == PowerfulDevice3)
		{
			return "32840-01.html";
		}
		else if(npc.getNpcId() == PowerfulDevice4)
		{
			return "32841-01.html";
		}
		else if(npc.getNpcId() == Tomb)
		{
			return st.getInt("guard1") == 1 && st.getInt("guard2") == 1 && st.getInt("guard3") == 1 && st.getInt("guard4") == 1 ? "32843-03.html" : "32843-01.html";
		}
		else if(npc.getNpcId() == TeleportControlDevice)
		{
			return "32842-01.html";
		}
		else if(npc.getNpcId() == TeleportControlDevice2)
		{
			return "32844-01.html";
		}
		else if(npc.getNpcId() == SolinasEvilThoughts)
		{
			return "32793-01.html";
		}
		return null;
	}
}
