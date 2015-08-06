package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;

public class ZombieGatekeepers extends Quest
{
	private TIntObjectHashMap<FastList<L2Character>> _attackersList = new TIntObjectHashMap<>();

	public ZombieGatekeepers()
	{
		addAttackId(22136);
		addAggroRangeEnterId(22136);
	}

	public static void main(String[] args)
	{
		new ZombieGatekeepers();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int npcObjId = npc.getObjectId();
		L2Character target = isPet ? attacker.getPets().getFirst() : attacker;
		if(_attackersList.get(npcObjId) == null)
		{
			FastList<L2Character> player = new FastList<>();
			player.add(target);
			_attackersList.put(npcObjId, player);
		}
		else if(!_attackersList.get(npcObjId).contains(target))
		{
			_attackersList.get(npcObjId).add(target);
		}
		return onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcObjId = npc.getObjectId();
		if(_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcObjId = npc.getObjectId();
		L2Character target = isPet ? player.getPets().getFirst() : player;
		L2ItemInstance VisitorsMark = player.getInventory().getItemByItemId(8064);
		L2ItemInstance FadedVisitorsMark = player.getInventory().getItemByItemId(8065);
		L2ItemInstance PagansMark = player.getInventory().getItemByItemId(8067);
		long mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
		long mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
		long mark3 = PagansMark == null ? 0 : PagansMark.getCount();
		if(mark1 == 0 && mark2 == 0 && mark3 == 0)
		{
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else
		{
			if(_attackersList.get(npcObjId) == null || !_attackersList.get(npcObjId).contains(target))
			{
				((L2Attackable) npc).getAggroList().remove(target);
			}
			else
			{
				((L2Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}
