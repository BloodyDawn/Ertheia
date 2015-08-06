package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.SoulCrystalData;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Attackable.AbsorberInfo;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.soulcrystal.SoulCrystal;
import dwo.gameserver.model.items.soulcrystal.SoulCrystalAbsorbType;
import dwo.gameserver.model.items.soulcrystal.SoulCrystalLevelingInfo;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

public class _00350_EnhanceYourWeapon extends Quest
{
	private static final int[] STARTING_NPCS = {30115, 30856, 30194};
	private static final int RED_SOUL_CRYSTAL0_ID = 4629;
	private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
	private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;

	public _00350_EnhanceYourWeapon()
	{

		addStartNpc(STARTING_NPCS);
		addTalkId(STARTING_NPCS);

		for(int npcId : SoulCrystalData.getInstance().getNpcLevelInfo().keySet())
		{
			addSkillSeeId(npcId);
			addKillId(npcId);
		}
	}

	public static void main(String[] args)
	{
		new _00350_EnhanceYourWeapon();
	}

	private boolean check(QuestState st)
	{
		for(int i = 4629; i < 4665; i++)
		{
			if(st.hasQuestItems(i))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int getQuestId()
	{
		return 350;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(event.endsWith("-04.htm"))
		{
			st.startQuest();
		}
		else if(event.endsWith("-09.htm"))
		{
			st.giveItems(RED_SOUL_CRYSTAL0_ID, 1);
		}
		else if(event.endsWith("-10.htm"))
		{
			st.giveItems(GREEN_SOUL_CRYSTAL0_ID, 1);
		}
		else if(event.endsWith("-11.htm"))
		{
			st.giveItems(BLUE_SOUL_CRYSTAL0_ID, 1);
		}
		else if(event.equalsIgnoreCase("exit.htm"))
		{
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc instanceof L2Attackable && SoulCrystalData.getInstance().getNpcLevelInfo().containsKey(npc.getNpcId()))
		{
			levelSoulCrystals((L2Attackable) npc, killer);
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}

		if(st.getState() == CREATED)
		{
			st.setCond(0);
		}
		if(st.getCond() == 0)
		{
			htmltext = npc.getNpcId() + "-01.htm";
		}
		else if(check(st))
		{
			htmltext = npc.getNpcId() + "-03.htm";
		}
		else if(!st.hasQuestItems(RED_SOUL_CRYSTAL0_ID) && !st.hasQuestItems(GREEN_SOUL_CRYSTAL0_ID) && !st.hasQuestItems(BLUE_SOUL_CRYSTAL0_ID))
		{
			htmltext = npc.getNpcId() + "-21.htm";
		}
		return htmltext;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		super.onSkillSee(npc, caster, skill, targets, isPet);

		if(skill == null || skill.getId() != 2096)
		{
			return null;
		}
		if(caster == null || caster.isDead())
		{
			return null;
		}
		if(!(npc instanceof L2Attackable) || npc.isDead() || !SoulCrystalData.getInstance().getNpcLevelInfo().containsKey(npc.getNpcId()))
		{
			return null;
		}
		try
		{
			((L2Attackable) npc).addAbsorber(caster);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		return null;
	}

	/**
	 * Calculate the leveling chance of Soul Crystals based on the attacker that killed this L2Attackable
	 *
	 * @param killer The player that last killed this L2Attackable
	 * $ Rewrite 06.12.06 - Yesod
	 * $ Rewrite 08.01.10 - Gigiikun
	 */
	public void levelSoulCrystals(L2Attackable mob, L2PcInstance killer)
	{
		// Only L2PcInstance can absorb a soul
		if(killer == null)
		{
			mob.resetAbsorbList();
			return;
		}

		FastMap<L2PcInstance, SoulCrystal> players = FastMap.newInstance();
		int maxSCLevel = 0;

		//TODO: what if mob support last_hit + party?
		if(isPartyLevelingMonster(mob.getNpcId()) && killer.isInParty())
		{
			// firts get the list of players who has one Soul Cry and the quest
			for(L2PcInstance pl : killer.getParty().getMembers())
			{
				if(pl == null)
				{
					continue;
				}

				SoulCrystal sc = getSCForPlayer(pl);
				if(sc == null)
				{
					continue;
				}

				players.put(pl, sc);
				if(maxSCLevel < sc.getLevel() && SoulCrystalData.getInstance().getNpcLevelInfo().get(mob.getNpcId()).containsKey(sc.getLevel()))
				{
					maxSCLevel = sc.getLevel();
				}
			}
		}
		else
		{
			SoulCrystal sc = getSCForPlayer(killer);
			if(sc != null)
			{
				players.put(killer, sc);
				if(maxSCLevel < sc.getLevel() && SoulCrystalData.getInstance().getNpcLevelInfo().get(mob.getNpcId()).containsKey(sc.getLevel()))
				{
					maxSCLevel = sc.getLevel();
				}
			}
		}
		//Init some useful vars
		SoulCrystalLevelingInfo mainlvlInfoSoulCrystal = SoulCrystalData.getInstance().getNpcLevelInfo().get(mob.getNpcId()).get(maxSCLevel);

		if(mainlvlInfoSoulCrystal == null)
		{
			return;
		}

		// If this mob is not require skill, then skip some checkings
		if(mainlvlInfoSoulCrystal.isSkillNeeded())
		{
			// Fail if this L2Attackable isn't absorbed or there's no one in its _absorbersList
			if(!mob.isAbsorbed() /*|| _absorbersList == null*/)
			{
				mob.resetAbsorbList();
				return;
			}

			// Fail if the killer isn't in the _absorbersList of this L2Attackable and mob is not boss
			AbsorberInfo ai = mob.getAbsorbersList().get(killer.getObjectId());
			boolean isSuccess = true;
			if(ai == null || ai._objId != killer.getObjectId())
			{
				isSuccess = false;
			}

			// Check if the soul crystal was used when HP of this L2Attackable wasn't higher than half of it
			if(ai != null && ai._absorbedHP > mob.getMaxHp() / 2.0)
			{
				isSuccess = false;
			}

			if(!isSuccess)
			{
				mob.resetAbsorbList();
				return;
			}
		}

		switch(mainlvlInfoSoulCrystal.getAbsorbCrystalType())
		{
			case PARTY_ONE_RANDOM:
				// This is a naive method for selecting a random member.	It gets any random party member and
				// then checks if the member has a valid crystal.	It does not select the random party member
				// among those who have crystals, only.	However, this might actually be correct (same as retail).
				if(killer.isInParty())
				{
					L2PcInstance lucky = killer.getParty().getMembers().get(Rnd.get(killer.getParty().getMemberCount()));
					tryToLevelCrystal(lucky, players.get(lucky), mob);
				}
				else
				{
					tryToLevelCrystal(killer, players.get(killer), mob);
				}
				break;
			case PARTY_RANDOM:
				if(killer.isInParty())
				{
					FastList<L2PcInstance> luckyParty = FastList.newInstance();
					luckyParty.addAll(killer.getParty().getMembers());
					while(Rnd.getChance(33) && !luckyParty.isEmpty())
					{
						L2PcInstance lucky = luckyParty.remove(Rnd.get(luckyParty.size()));
						if(players.containsKey(lucky))
						{
							tryToLevelCrystal(lucky, players.get(lucky), mob);
						}
					}
					FastList.recycle(luckyParty);
				}
				else if(Rnd.getChance(33))
				{
					tryToLevelCrystal(killer, players.get(killer), mob);
				}
				break;
			case FULL_PARTY:
				if(killer.isInParty())
				{
					for(L2PcInstance pl : killer.getParty().getMembers())
					{
						tryToLevelCrystal(pl, players.get(pl), mob);
					}
				}
				else
				{
					tryToLevelCrystal(killer, players.get(killer), mob);
				}
				break;
			case LAST_HIT:
				tryToLevelCrystal(killer, players.get(killer), mob);
				break;
		}
		FastMap.recycle(players);
	}

	private boolean isPartyLevelingMonster(int npcId)
	{
		for(SoulCrystalLevelingInfo li : SoulCrystalData.getInstance().getNpcLevelInfo().get(npcId).values())
		{
			if(li.getAbsorbCrystalType() != SoulCrystalAbsorbType.LAST_HIT)
			{
				return true;
			}
		}
		return false;
	}

	private SoulCrystal getSCForPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || st.getState() != STARTED)
		{
			return null;
		}

		L2ItemInstance[] inv = player.getInventory().getItems();
		SoulCrystal ret = null;
		for(L2ItemInstance item : inv)
		{
			int itemId = item.getItemId();
			if(!SoulCrystalData.getInstance().getSoulCrystal().containsKey(itemId))
			{
				continue;
			}

			if(ret != null)
			{
				return null;
			}
			else
			{
				ret = SoulCrystalData.getInstance().getSoulCrystal().get(itemId);
			}
		}
		return ret;
	}

	private void tryToLevelCrystal(L2PcInstance player, SoulCrystal sc, L2Attackable mob)
	{
		if(sc == null || !SoulCrystalData.getInstance().getNpcLevelInfo().containsKey(mob.getNpcId()))
		{
			return;
		}

		// If the crystal level is way too high for this mob, say that we can't increase it
		if(!SoulCrystalData.getInstance().getNpcLevelInfo().get(mob.getNpcId()).containsKey(sc.getLevel()))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}

		if(Rnd.getChance(SoulCrystalData.getInstance().getNpcLevelInfo().get(mob.getNpcId()).get(sc.getLevel()).getChance()))
		{
			exchangeCrystal(player, mob, sc.getItemId(), sc.getLeveledItemId(), false);
		}
		else
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
		}
	}

	private void exchangeCrystal(L2PcInstance player, L2Attackable mob, int takeid, int giveid, boolean broke)
	{
		L2ItemInstance Item = player.getInventory().destroyItemByItemId(ProcessType.SOULCRYSTAL, takeid, 1, player, mob);

		if(Item != null)
		{
			// Prepare inventory update packet
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addRemovedItem(Item);

			// Add new crystal to the killer's inventory
			Item = player.getInventory().addItem(ProcessType.SOULCRYSTAL, giveid, 1, player, mob);
			playerIU.addItem(Item);

			// Send a sound event and text message to the player
			if(broke)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			}

			// Send system message
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(giveid));

			// Send inventory update packet
			player.sendPacket(playerIU);
		}
	}
}