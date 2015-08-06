package dwo.scripts.ai.specific;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import javolution.util.FastList;

public class SummonMinions extends Quest
{
	private static final TIntObjectHashMap<int[]> MINIONS = new TIntObjectHashMap<>();

	static
	{
		MINIONS.put(20767, new int[]{20768, 20769, 20770}); //Timak Orc Troop
		//MINIONS.put(22030,new Integer[]{22045,22047,22048}); //Ragna Orc Shaman
		//MINIONS.put(22032,new Integer[]{22036}); //Ragna Orc Warrior - summons shaman but not 22030 ><
		//MINIONS.put(22038,new Integer[]{22037}); //Ragna Orc Hero
		MINIONS.put(21524, new int[]{21525}); //Blade of Splendor
		MINIONS.put(21531, new int[]{21658}); //Punishment of Splendor
		MINIONS.put(21539, new int[]{21540}); //Wailing of Splendor
		MINIONS.put(22257, new int[]{18364, 18364}); //Island Guardian
		MINIONS.put(22258, new int[]{18364, 18364}); //White Sand Mirage
		MINIONS.put(22259, new int[]{18364, 18364}); //Muddy Coral
		MINIONS.put(22260, new int[]{18364, 18364}); //Kleopora
		MINIONS.put(22261, new int[]{18365, 18365}); //Seychelles
		MINIONS.put(22262, new int[]{18365, 18365}); //Naiad
		MINIONS.put(22263, new int[]{18365, 18365}); //Sonneratia
		MINIONS.put(22264, new int[]{18366, 18366}); //Castalia
		MINIONS.put(22265, new int[]{18366, 18366}); //Chrysocolla
		MINIONS.put(22266, new int[]{18366, 18366}); //Pythia
		MINIONS.put(22774, new int[]{22768, 22768}); // Tanta Lizardman Summoner
	}

	private static int HasSpawned;
	private static TIntHashSet myTrackingSet = new TIntHashSet(); //Used to track instances of npcs
	private TIntObjectHashMap<FastList<L2PcInstance>> _attackersList = new TIntObjectHashMap<>();

	public SummonMinions()
	{
		int[] temp = {
			20767, 21524, 21531, 21539, 22257, 22258, 22259, 22260, 22261, 22262, 22263, 22264, 22265, 22266, 22774
		};
		registerMobs(temp, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new SummonMinions();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int npcObjId = npc.getObjectId();
		if(MINIONS.containsKey(npcId))
		{
			if(!myTrackingSet.contains(npcObjId)) //this allows to handle multiple instances of npc
			{
				synchronized(myTrackingSet)
				{
					myTrackingSet.add(npcObjId);
				}

				HasSpawned = npcObjId;
			}
			if(HasSpawned == npcObjId)
			{
				switch(npcId)
				{
					case 22030: //mobs that summon minions only on certain hp
					case 22032:
					case 22038:
						if(npc.getCurrentHp() < npc.getMaxHp() / 2.0)
						{
							HasSpawned = 0;
							if(Rnd.getChance(33)) //mobs that summon minions only on certain chance
							{
								int[] minions = MINIONS.get(npcId);
								for(int val : minions)
								{
									L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
									newNpc.setRunning();
									newNpc.addDamageHate(attacker, 0, 999);
									newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
								}
								minions = null;
							}
						}
						break;
					case 22257:
					case 22258:
					case 22259:
					case 22260:
					case 22261:
					case 22262:
					case 22263:
					case 22264:
					case 22265:
					case 22266:
						if(attacker.getParty() != null)
						{
							for(L2PcInstance member : attacker.getParty().getMembers())
							{
								if(_attackersList.get(npcObjId) == null)
								{
									FastList<L2PcInstance> player = new FastList<>();
									player.add(member);
									_attackersList.put(npcObjId, player);
								}
								else if(!_attackersList.get(npcObjId).contains(member))
								{
									_attackersList.get(npcObjId).add(member);
								}
							}
						}
						else
						{
							if(_attackersList.get(npcObjId) == null)
							{
								FastList<L2PcInstance> player = new FastList<>();
								player.add(attacker);
								_attackersList.put(npcObjId, player);
							}
							else if(!_attackersList.get(npcObjId).contains(attacker))
							{
								_attackersList.get(npcObjId).add(attacker);
							}
						}
						if(attacker.getParty() != null && attacker.getParty().getMemberCount() > 2 || _attackersList.get(npcObjId).size() > 2) //Just to make sure..
						{
							HasSpawned = 0;
							for(int val : MINIONS.get(npcId))
							{
								L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						}
						break;
					default: //mobs without special conditions
						HasSpawned = 0;
						if(npcId == 20767)
						{
							for(int val : MINIONS.get(npcId))
							{
								addSpawn(val, npc.getX() + Rnd.get(-100, 100), npc.getY() + Rnd.get(-100, 100), npc.getZ(), 0, false, 0);
							}
						}
						else
						{
							for(int val : MINIONS.get(npcId))
							{
								L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						}
						if(npcId == 20767)
						{
							npc.broadcastPacket(new NS(npcObjId, ChatType.NPC_ALL, npcId, NpcStringId.COME_OUT_YOU_CHILDREN_OF_DARKNESS)); // Come out, you children of darkness!
						}
						break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int npcObjId = npc.getObjectId();
		if(MINIONS.containsKey(npcId))
		{
			synchronized(myTrackingSet)
			{
				myTrackingSet.remove(npcObjId);
			}
		}
		if(_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}
		return super.onKill(npc, killer, isPet);
	}
}