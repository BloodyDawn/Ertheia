package dwo.scripts.ai.individual;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.scripts.instances.RB_Tauti;

/**
 * User: GenCloud
 * Date: 16.03.2015
 * Team: La2Era Team
 */
public class Zahak extends Quest
{
    //Npcs
    public static final int ZAHAK = 19287;
    // Skill
    private static final SkillHolder HEAL_TO_TAUTI = new SkillHolder(14625, 1);

    public Zahak()
    {
        super();
        addSpawnId(ZAHAK);
        addKillId(ZAHAK);
        addAttackId(ZAHAK);
        addAggroRangeEnterId(ZAHAK);
    }

    public static void main(String[] args)
    {
        new Zahak();
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        RB_Tauti.TautiWorld world = InstanceManager.getInstance().getInstanceWorld(npc.getCharacter(), RB_Tauti.TautiWorld.class);

        if (npc.getNpcId() == ZAHAK)
        {
            npc.setIsNoRndWalk(true);
            npc.setTarget(world.tauti);
            npc.doCast(HEAL_TO_TAUTI.getSkill());
        }
        return super.onSpawn(npc);
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (npc.getNpcId() == ZAHAK)
        {
            if(!npc.isCastingNow() && !npc.isAttackingNow() && !npc.isInCombat() && !player.isDead())
            {
                ((L2Attackable) npc).addDamageHate(player, 0, 999);
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                npc.enableAllSkills();
            }
        }
        return super.onAggroRangeEnter(npc, player, isPet);
    }
}
