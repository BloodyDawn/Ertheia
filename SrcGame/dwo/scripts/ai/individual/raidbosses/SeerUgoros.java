package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

import java.util.concurrent.ScheduledFuture;

public class SeerUgoros extends Quest
{
	// Item
	private static final int _ugoros_pass = 15496;
	private static final int _mid_scale = 15498;
	private static final int _high_scale = 15497;
	// Zone ID
	private static final int _ugoros_zone = 70307;
	// NPC ID
	private static final int _seer_ugoros = 18863;
	private static final int _batracos = 32740;
	private static final int _weed_id = 18867;
	// State
	private static final byte ALIVE = 0;
	private static final byte FIGHTING = 1;
	private static final byte DEAD = 2;
	// State
	private static byte STATE = DEAD;
	// Skill
	private static final SkillHolder _ugoros_skill = new SkillHolder(6426, 1);
	// Ugoros
	private static L2Npc _ugoros;
	// Weed
	private static L2Npc _weed;
	// State
	private static boolean _weed_attack;
	// Killer
	private static boolean _weed_killed_by_player;
	private static boolean _killed_one_weed;
	// Player
	private static L2PcInstance _player;
	// Boss attack task
	ScheduledFuture<?> _thinkTask;

	public SeerUgoros()
	{

		addStartNpc(_batracos);
		addTalkId(_batracos);
		addKillId(_seer_ugoros);
		addAttackId(_weed_id);

		startQuestTimer("ugoros_respawn", 60000, null, null);
	}

	public static void main(String[] args)
	{
		new SeerUgoros();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.isDead())
		{
			return null;
		}

		if(npc.getNpcId() == _weed_id)
		{
			if(_ugoros != null && _weed != null && npc.equals(_weed))
			{
				// Reset weed
				_weed = null;
				// Reset attack state
				_weed_attack = false;
				// Set it
				_weed_killed_by_player = true;
				// Complain
				_ugoros.broadcastPacket(new NS(_ugoros.getObjectId(), ChatType.ALL, _ugoros.getNpcId(), NpcStringId.NO_HOW_DARE_YOU_STOP_ME_FROM_USING_THE_ABYSS_WEED_DO_YOU_KNOW_WHAT_YOU_HAVE_DONE));
				// Cancel current think-task
				if(_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}
				// Re-setup task to re-think attack again
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 500, 3000);
			}

			npc.doDie(attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("ugoros_respawn") && _ugoros == null)
		{
			_ugoros = addSpawn(_seer_ugoros, 96804, 85604, -3720, 34360, false, 0);

			broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_I_HAVE_RETURNED_THE_PROPHET_YUGOROS_OF_THE_BLACK_ABYSS_IS_WITH_ME_SO_DO_NOT_BE_AFRAID, null);

			STATE = ALIVE;

			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if(event.equalsIgnoreCase("ugoros_shout"))
		{
			if(STATE == FIGHTING)
			{
				L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);
				if(_player == null)
				{
					STATE = ALIVE;
				}
				else if(!_zone.isCharacterInZone(_player))
				{
					STATE = ALIVE;
					_player = null;
				}
			}
			else if(STATE == ALIVE)
			{
				broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_THE_BLACK_ABYSS_IS_FAMISHED_FIND_SOME_FRESH_OFFERINGS, null);
			}
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if(event.equalsIgnoreCase("ugoros_attack"))
		{
			if(_player != null)
			{
				changeAttackTarget(_player);

				broadcastInRegion(_ugoros, NpcStringId.WELCOME_S1_LET_US_SEE_IF_YOU_HAVE_BROUGHT_A_WORTHY_OFFERING_FOR_THE_BLACK_ABYSS, _player.getName());

				if(_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}

				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 1000, 3000);
			}
		}
		else if(event.equalsIgnoreCase("weed_check"))
		{
			if(_weed_attack && _ugoros != null && _weed != null)
			{
				if(_weed.isDead() && !_weed_killed_by_player)
				{
					_killed_one_weed = true;
					_weed = null;
					_weed_attack = false;
					_ugoros.getStatus().setCurrentHp(_ugoros.getStatus().getCurrentHp() + _ugoros.getMaxHp() * 0.2);
					_ugoros.broadcastPacket(new NS(_ugoros.getObjectId(), ChatType.ALL, _ugoros.getNpcId(), NpcStringId.WHAT_A_FORMIDABLE_FOE_BUT_I_HAVE_THE_ABYSS_WEED_GIVEN_TO_ME_BY_THE_BLACK_ABYSS_LET_ME_SEE));
				}
				else
				{
					startQuestTimer("weed_check", 2000, null, null);
				}
			}
			else
			{
				_weed = null;
				_weed_attack = false;
			}
		}
		else if(event.equalsIgnoreCase("ugoros_expel"))
		{
			if(_player != null)
			{
				_player.teleToLocation(94701, 83053, -3580);
				_player = null;
			}
		}
		else if(event.equalsIgnoreCase("teleport_inside"))
		{
			if(player != null && STATE == ALIVE)
			{
				if(player.getInventory().getItemByItemId(_ugoros_pass) != null)
				{
					STATE = FIGHTING;

					_player = player;
					_killed_one_weed = false;

					player.teleToLocation(95984, 85692, -3720);
					player.destroyItemByItemId(ProcessType.QUEST, _ugoros_pass, 1, npc, true);

					startQuestTimer("ugoros_attack", 2000, null, null);

					// TODO: Lindvior: Новый квест
					/*QuestState st = player.getQuestState(_00288_HandleWithCare.class);
					if (st != null)
						st.set("drop", "1"); */
				}
				else
				{
					// TODO: Lindvior: Новый квест
					/*QuestState st = player.getQuestState(_00423_TakeYourBestShot.class);
					if(st == null)
						return "<html><body>Gatekeeper Batracos:<br>You look too inexperienced to make a journey to see Tanta Seer Ugoros. If you can convince Chief Investigator Johnny that you should go, then I will let you pass. Johnny has been everywhere and done everything. He may not be of my people but he has my respect, and anyone who has his will in turn have mine as well.<br></body></html>";
					else
						return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";*/
				}
			}
			else
			{
				return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
			}
		}
		else if(event.equalsIgnoreCase("teleport_back"))
		{
			if(player != null)
			{
				player.teleToLocation(94701, 83053, -3580);
				_player = null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == _seer_ugoros)
		{
			if(_thinkTask != null)
			{
				_thinkTask.cancel(true);
				_thinkTask = null;
			}

			STATE = DEAD;

			broadcastInRegion(_ugoros, NpcStringId.AH_HOW_COULD_I_LOSE_OH_BLACK_ABYSS_RECEIVE_ME, null);

			_ugoros = null;

			addSpawn(_batracos, 96782, 85918, -3720, 34360, false, 50000);

			startQuestTimer("ugoros_expel", 50000, null, null);
			startQuestTimer("ugoros_respawn", 60000, null, null);
			startQuestTimer("teleport_back", 10000, null, null);

			// TODO: Lindvior: Новый квест
			/*QuestState st = player.getQuestState("288_HandleWithCare");
			if (st != null && st.getCond() == 1 && st.getInt("drop") == 1)
			{
				if(_killed_one_weed)
				{
					player.addItem(ProcessType.QUEST, _mid_scale, 1, npc, true);
					st.setCond(2);
				}
				else
				{
					player.addItem(ProcessType.QUEST, _high_scale, 1, npc, true);
					st.setCond(3);
				}
				st.unset("drop");
			}*/
		}
		return null;
	}

	private void broadcastInRegion(L2Npc npc, NpcStringId npcString, String playerName)
	{
		NS cs;
		if(npc == null)
		{
			return;
		}
		cs = playerName != null ? new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), npcString).addStringParameter(playerName) : new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), npcString);

		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			if(player != null && Util.checkIfInRange(6000, npc, player, false))
			{
				player.sendPacket(cs);
			}
		}
	}

	private void changeAttackTarget(L2Character _attack)
	{
		_ugoros.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		((L2Attackable) _ugoros).clearAggroList();
		_ugoros.setTarget(_attack);

		if(_attack instanceof L2Attackable)
		{
			_weed_killed_by_player = false;

			_ugoros.disableSkill(_ugoros_skill.getSkill(), 100000);

			_ugoros.setIsRunning(true);
			((L2Attackable) _ugoros).addDamageHate(_attack, 0, Integer.MAX_VALUE);
		}
		else
		{
			_ugoros.enableSkill(_ugoros_skill.getSkill());

			((L2Attackable) _ugoros).addDamageHate(_attack, 0, 99);
			_ugoros.setIsRunning(false);
		}
		_ugoros.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _attack);
	}

	private class ThinkTask implements Runnable
	{
		protected ThinkTask()
		{
		}

		@Override
		public void run()
		{
			L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);

			if(STATE == FIGHTING && _player != null && _zone.isCharacterInZone(_player) && !_player.isDead())
			{
				if(_weed_attack && _weed != null)
				{
					// Dummy, just wait
				}
				else if(Rnd.get(10) < 6)
				{
					_weed = null;

					for(L2Character _char : _ugoros.getKnownList().getKnownCharactersInRadius(2000))
					{
						if(_char instanceof L2Attackable && !_char.isDead() && ((L2Attackable) _char).getNpcId() == _weed_id)
						{
							_weed_attack = true;
							_weed = (L2Attackable) _char;
							changeAttackTarget(_weed);
							startQuestTimer("weed_check", 1000, null, null);
							break;
						}
					}
					if(_weed == null)
					{
						changeAttackTarget(_player);
					}
				}
				else
				{
					changeAttackTarget(_player);
				}
			}
			else
			{
				STATE = ALIVE;

				_player = null;

				if(_thinkTask != null)
				{
					_thinkTask.cancel(true);
					_thinkTask = null;
				}
			}
		}
	}
}