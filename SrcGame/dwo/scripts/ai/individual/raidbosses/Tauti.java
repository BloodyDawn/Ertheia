package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.RB_Tauti;

import java.util.Collection;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Tauti extends Quest
{

    // Npcs
    public static final int TAUTI_LIGHT = 29233;
    public static final int TAUTI_HARD = 29234;
    public static final int TAUTI_NORMAL_AXE = 29236;
    public static final int TAUTI_HARD_AXE = 29237;

    // Skills
    private static final SkillHolder TAUTI_ULTIMATE_CHAIN_STRIKE = new SkillHolder(15168, 1);

    // Normal Mode
    private static final SkillHolder TAUTI_ULTRA_WHIRLWIND_EASY = new SkillHolder(15200, 1);
    private static final SkillHolder TAUTI_ULTRA_TYPHOON_EASY = new SkillHolder(15202, 1);

    // Hard Mode
    private static final SkillHolder TAUTI_ULTRA_WHIRLWIND_HARD = new SkillHolder(15201, 1);
    private static final SkillHolder TAUTI_ULTRA_TYPHOON_HARD = new SkillHolder(15203, 1);

    private static final SkillHolder[] SkillsLight = new SkillHolder[]
            {
                    new SkillHolder(15042, 1),
                    new SkillHolder(15044, 1),
                    new SkillHolder(15046, 1),
                    new SkillHolder(15163, 1)
            };

    private static final SkillHolder[] SkillsHight = new SkillHolder[]
            {
                    new SkillHolder(15043, 1),
                    new SkillHolder(15045, 1),
                    new SkillHolder(15047, 1),
                    new SkillHolder(15064, 1)
            };

    protected OnHpChange _hpChange;

    public Tauti()
    {
        super();
        addKillId(TAUTI_LIGHT, TAUTI_HARD, TAUTI_HARD_AXE, TAUTI_NORMAL_AXE);
        addSpawnId(TAUTI_LIGHT, TAUTI_HARD, TAUTI_HARD_AXE, TAUTI_NORMAL_AXE);
        addAttackId(TAUTI_LIGHT, TAUTI_HARD, TAUTI_HARD_AXE, TAUTI_NORMAL_AXE);
    }

    public static void main(String[] args)
    {
        new Tauti();
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        _hpChange = new OnHpChange();
        switch (npc.getNpcId())
        {
            case TAUTI_LIGHT:
            case TAUTI_NORMAL_AXE:
                if (Rnd.get() <= 0.2 && !npc.isCastingNow())
                {
                    npc.doCast(SkillsLight[Rnd.get(SkillsLight.length)].getSkill());
                }
                npc.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);
                break;

            case TAUTI_HARD:
            case TAUTI_HARD_AXE:
                if (Rnd.get() <= 0.2 && !npc.isCastingNow())
                {
                    npc.doCast(SkillsHight[Rnd.get(SkillsHight.length)].getSkill());
                }
                npc.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);
                break;
        }

        return super.onAttack(npc, attacker, damage, isPet);
    }

    private class OnHpChange extends AbstractHookImpl
    {
        @Override
        public void onHpChange(L2Character character, double damage, double fullDamage)
        {
            RB_Tauti.TautiWorld world = InstanceManager.getInstance().getInstanceWorld(character, RB_Tauti.TautiWorld.class);
            Collection<L2PcInstance> players = character.getKnownList().getKnownPlayers().values();
            SkillHolder skill;

            double percent = ((character.getCurrentHp() - damage) / character.getMaxHp()) * 100;
            if ((percent <= 50) && (percent >= 15) && (world.status == 2 || world.status == 3))
            {
                if ((Rnd.get() <= 0.2) && !world.tauti.isCastingNow())
                {
                    if (world.isHardInstance)
                    {
                        skill = Rnd.getChance(50.) ? TAUTI_ULTRA_WHIRLWIND_EASY : TAUTI_ULTRA_TYPHOON_EASY;
                    }
                    else
                    {
                        skill = Rnd.getChance(50.) ? TAUTI_ULTRA_WHIRLWIND_HARD : TAUTI_ULTRA_TYPHOON_HARD;
                    }

                    world.tauti.setTarget(Rnd.get(players.toArray(new L2PcInstance[players.size()])));
                    world.tauti.doCast(skill.getSkill());

                    if (skill == TAUTI_ULTRA_TYPHOON_EASY && !world.isHardInstance)
                    {
                        world.tauti.doCast(TAUTI_ULTIMATE_CHAIN_STRIKE.getSkill());
                    }

                    if (skill == TAUTI_ULTRA_TYPHOON_HARD && world.isHardInstance)
                    {
                        world.tauti.doCast(TAUTI_ULTIMATE_CHAIN_STRIKE.getSkill());
                    }
                }
            }
        }
    }
}

