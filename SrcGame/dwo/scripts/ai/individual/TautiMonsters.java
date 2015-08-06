package dwo.scripts.ai.individual;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.RB_Tauti;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class TautiMonsters extends Quest
{
    public static final int[] KUNDAS = {19262, 19263, 19264};
    public static final int[] SOFAS = {33680, 33679};

    public static final int KUNDA_MINION = 19265;
    public static final int ZAHAQ = 19266;

    private static final int WAR_LOC_MIN_X = -148180;
    private static final int WAR_LOC_MIN_Y = 181492;
    private static final int WAR_LOC_MAX_X = -147096;
    private static final int WAR_LOC_MAX_Y = 182240;

    public TautiMonsters()
    {
        addSpawnId(KUNDAS);
        addSpawnId(SOFAS);
    }

    public static boolean isKunda(L2Npc npc)
    {
        return ArrayUtils.contains(KUNDAS, npc.getNpcId());
    }

    public static boolean isSofa(L2Npc npc)
    {
        return ArrayUtils.contains(SOFAS, npc.getNpcId());
    }

    public static void main(String[] args)
    {
        new TautiMonsters();
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        RB_Tauti.TautiWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Tauti.TautiWorld.class);

        if(world != null)
        {
            if(world.isKundaAttacker(npc) || world.isSofaDefender(npc) || world.spawningWarMonsters)
            {
                if (world.isSofaDefender(npc))
                {
                    for (L2Npc sofa : world.sofas)
                    {
                        ((L2GuardInstance) sofa).setCanAttackPlayer(false);
                        ((L2GuardInstance) npc).setCanAttackGuard(false);
                    }
                }


                Location loc = new Location(Rnd.get(WAR_LOC_MIN_X, WAR_LOC_MAX_X), Rnd.get(WAR_LOC_MIN_Y, WAR_LOC_MAX_Y), -11579);
                npc.setRunning();
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);

                ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                    for(L2Npc sofa : world.sofas)
                    {
                        L2MonsterInstance attacker = (L2MonsterInstance) world.kundas.get(Rnd.get(world.kundas.size()));
                        ((L2MonsterInstance) sofa).addDamageHate(attacker, 1, 1);
                        ((L2MonsterInstance) sofa).attackCharacter(attacker);
                        attacker.addDamageHate(sofa, 1, 1);
                        attacker.attackCharacter(sofa);
                    }
                }, 5000);
            }
        }

        return null;
    }
}
