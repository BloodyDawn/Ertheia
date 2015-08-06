package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

// TODO: Количество попыток
public class MinigameInstructor extends Quest
{
	private static final int SUMIEL = 32758;
	private static final int FURNACE = 18913;
	private static final int CHEST = 18934;
	// Items
	private static final int UNLIT_TORCH = 15540;
	private static final int TORCH = 15485;
	// Skills
	private static final int TORCH_SKILL = 9059;
	// Configs
	private static final int HOW_MUCH = 10;
	// Event time (in min)
	private static final int EVENT_TIME = 3;
	// Furnace Spawns
	private static final int[][] FURNACE_SPAWNS = {
		{110238, -82249, -1589}, {110240, -82406, -1589}, {110236, -82097, -1590}, {110414, -82091, -1590},
		{110413, -82252, -1591}, {110413, -82410, -1591}, {110568, -82092, -1590}, {110566, -82250, -1592},
		{110560, -82415, -1592},
	};
	private static final int[][] REWARDS = {
		{960, 1}    // Scroll: Enchant Armor (S)
	};
	// NPC
	private static L2Npc sumielNpc;
	private static int _player;
	private static long NEXT_START_TIME;
	// Result
	private boolean playerWin;
	private boolean inProgress;
	// Counter
	private int COUNTER;
	// Furnaces
	private List<L2Npc> furnaceList = new ArrayList<>();
	private List<L2Npc> furnaceRand = new ArrayList<>();

	public MinigameInstructor()
	{
		addSkillSeeId(FURNACE);
		addAttackId(CHEST);

		addAskId(SUMIEL, -1);
		addAskId(SUMIEL, -2);
		addAskId(SUMIEL, -3);
		addAskId(SUMIEL, -7801);
		spawnNPCs();
	}

	public static void main(String[] args)
	{
		new MinigameInstructor();
	}

	private void spawnNPCs()
	{
		sumielNpc = addSpawn(SUMIEL, 110805, -81851, -1588, -25824, false, 0);

		for(int[] SPAWN : FURNACE_SPAWNS)
		{
			L2Npc furnace = addSpawn(FURNACE, SPAWN[0], SPAWN[1], SPAWN[2], 0, false, 0);

			if(furnace != null)
			{
				// By default they are not targetable
				furnace.setTargetable(false);
				// Add to list
				furnaceList.add(furnace);
			}
		}
	}

	private void cleanup()
	{
		COUNTER = 0;
		_player = 0;
		playerWin = false;
		inProgress = false;
		furnaceRand.clear();
	}

	private void timeUp()
	{
		if(!playerWin)
		{
			sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.TIME_IS_UP_AND_YOU_HAVE_FAILED_ANY_MORE_WILL_BE_DIFFICULT));
		}

		cleanup();

		for(L2Npc furnace : furnaceList)
		{
			furnace.setTargetable(false);
		}
		NEXT_START_TIME = System.currentTimeMillis() + EVENT_TIME * 60 * 1000;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == CHEST && !npc.isDead())
		{
			int[] rndReward = REWARDS[Rnd.get(0, REWARDS.length - 1)];

			((L2Attackable) npc).dropItem(attacker, rndReward[0], rndReward[1]);

			npc.doDie(attacker);
			npc.getLocationController().delete();
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("start_game"))
		{
			// Send info
			sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.NOW_LIGHT_THE_FURNACES_FIRE));
			// Make it targetable
			startQuestTimer("target_on_all", 500, null, player);
			// Quest timer
			startQuestTimer("game_end", EVENT_TIME * 60 * 1000, null, player);
			// Quest timer 1 min before end
			startQuestTimer("min_left", (EVENT_TIME - 1) * 60 * 1000, null, player);
			// Quest timer 10 sec before end
			startQuestTimer("ten_sec_left", EVENT_TIME * 60 * 1000 - 1000, null, player);
		}
		else if(event.equalsIgnoreCase("game_end"))
		{
			// Set state
			inProgress = false;
			// Cleanup
			timeUp();
		}
		else if(event.equalsIgnoreCase("min_left"))
		{
			// Send info
			if(!playerWin)
			{
				sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.THERES_ABOUT_1_MINUTE_LEFT));
			}
		}
		else if(event.equalsIgnoreCase("ten_sec_left"))
		{
			// Send info
			if(!playerWin)
			{
				sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.THERES_JUST_10_SECONDS_LEFT));
			}
		}
		else if(event.equalsIgnoreCase("target_on_all"))
		{
			if(!furnaceList.isEmpty())
			{
				for(L2Npc furnace : furnaceList)
				{
					furnace.setTargetable(true);
				}
			}
		}
		else if(event.equalsIgnoreCase("target_off_all"))
		{
			if(!furnaceList.isEmpty())
			{
				for(L2Npc furnace : furnaceList)
				{
					furnace.setTargetable(false);
				}
			}
		}
		else if(event.equalsIgnoreCase("turn_on_all"))
		{
			if(!furnaceList.isEmpty())
			{
				for(L2Npc furnace : furnaceList)
				{
					furnace.setDisplayEffect(1);
				}
			}
		}
		else if(event.equalsIgnoreCase("turn_off_all"))
		{
			if(!furnaceList.isEmpty())
			{
				for(L2Npc furnace : furnaceList)
				{
					furnace.setDisplayEffect(2);
				}
			}
		}
		else if(event.equalsIgnoreCase("turn_off_furnace"))
		{
			if(npc != null)
			{
				npc.setDisplayEffect(2);
			}
		}
		else if(event.equalsIgnoreCase("random_torch"))
		{
			if(furnaceRand.size() == HOW_MUCH)
			{
				startQuestTimer("turn_on_all", 1000, null, player);
				startQuestTimer("turn_off_all", 4000, null, player);
				startQuestTimer("start_game", 4000, null, player);
			}
			else
			{
				L2Npc random = furnaceList.get(Rnd.get(0, furnaceList.size() - 1));
				// Light furnace
				random.setDisplayEffect(1);
				// Schedule turn off
				startQuestTimer("turn_off_furnace", 3000, random, player);
				// Add to list
				furnaceRand.add(random);
				// Start quest timer with rnd next
				startQuestTimer("random_torch", 4000, null, player);
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1 && reply == 1)
		{
			if(inProgress)
			{
				if(System.currentTimeMillis() >= NEXT_START_TIME)
				{
					return player.getObjectId() == _player ? "minigame_instructor007.htm" : "minigame_instructor004.htm";
				}
				else
				{
					return "minigame_instructor008.htm";
				}
			}
			else
			{
				// Setup variables
				cleanup();

				if(player.getInventory().getCountOf(UNLIT_TORCH) > 0)
				{
					// Store player
					_player = player.getObjectId();
					// Set state
					inProgress = true;
					// Bradcast info
					sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.THE_FURNACE_WILL_GO_OUT_WATCH_AND_SEE));

					// Exchange item
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(player.getInventory().destroyItemByItemId(ProcessType.MANUFACTURE, UNLIT_TORCH, 1, player, npc));
					iu.addItem(player.getInventory().addItem(ProcessType.MANUFACTURE, TORCH, 1, player, npc));
					player.sendPacket(iu);

					// Start timers
					startQuestTimer("turn_on_all", 4000, null, player);
					startQuestTimer("turn_off_all", 8000, null, player);
					startQuestTimer("random_torch", 10000, null, player);
				}
				else
				{
					return "minigame_instructor005.htm";
				}
			}
		}
		else if(ask == -7801)
		{
			if(reply == 1)
			{
				player.teleToLocation(110721, -81426, -1590);
			}
			else if(reply == 2)
			{
				player.teleToLocation(118833, -80589, -2688);
			}
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(caster == null || npc == null)
		{
			return null;
		}

		if(npc.getNpcId() == FURNACE && skill.getId() == TORCH_SKILL && ArrayUtils.contains(targets, npc))
		{
			// Set null target (on official skill 5144 is used)
			caster.setTarget(null);
			// Light furnace
			npc.setDisplayEffect(1);
			// Schedule turn off
			startQuestTimer("turn_off_furnace", 3000, npc, caster);
			// Check if player select correct one
			if(furnaceRand.size() > COUNTER && npc.equals(furnaceRand.get(COUNTER)))
			{
				COUNTER++;

				if(furnaceRand.size() == COUNTER)
				{
					// Set status
					playerWin = true;
					// Broadcast info
					sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.OH_YOUVE_SUCCEEDED));
					// Spawn chest
					addSpawn(CHEST, 110772, -82063, -1584, 0, false, 0);
					// Set non-targettable furnace, and flash
					startQuestTimer("target_off_all", 500, null, caster);
					startQuestTimer("turn_on_all", 500, null, caster);
					startQuestTimer("turn_off_all", 3000, null, caster);
				}
			}
			else
			{
				// Broadcast info
				sumielNpc.broadcastPacket(new Say2(sumielNpc.getObjectId(), ChatType.NPC_ALL, sumielNpc.getName(), NpcStringId.AH_IVE_FAILED_GOING_FURTHER_WILL_BE_DIFFICULT));
				// Set non-targettable furnace
				startQuestTimer("target_off_all", 1000, npc, caster);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}
