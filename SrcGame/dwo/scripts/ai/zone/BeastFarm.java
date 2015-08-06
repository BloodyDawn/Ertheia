package dwo.scripts.ai.zone;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2TamedBeastInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._00020_BringUpWithLove;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

/**
 * Growth-capable mobs: Polymorphing upon successful feeding.
 * @author Fulminus
 * Updated to Freya by Gigiikun
 */
public class BeastFarm extends Quest
{
	protected static final int[] SPECIAL_SPICE_CHANCES = {33, 75};
	private static final int GOLDEN_SPICE = 15474;
	private static final int CRYSTAL_SPICE = 15475;
	private static final int SKILL_GOLDEN_SPICE = 9049;
	private static final int SKILL_CRYSTAL_SPICE = 9050;
	private static final int SKILL_BLESSED_GOLDEN_SPICE = 9051;
	private static final int SKILL_BLESSED_CRYSTAL_SPICE = 9052;
	private static final int SKILL_SGRADE_GOLDEN_SPICE = 9053;
	private static final int SKILL_SGRADE_CRYSTAL_SPICE = 9054;
	private static final int[] TAMED_BEASTS = {18869, 18870, 18871, 18872};
	private static final int TAME_CHANCE = 20;
	// all mobs that can eat...
	private static final int[] FEEDABLE_BEASTS = {
		18873, 18874, 18875, 18876, 18877, 18878, 18879, 18880, 18881, 18882, 18883, 18884, 18885, 18886, 18887, 18888,
		18889, 18890, 18891, 18892, 18893, 18894, 18895, 18896, 18897, 18898, 18899, 18900
	};

	private static TIntIntHashMap _FeedInfo = new TIntIntHashMap();
	private static TIntObjectHashMap<GrowthCapableMob> _GrowthCapableMobs = new TIntObjectHashMap<>();
	private static Map<String, SkillHolder[]> _TamedBeastsData = new FastMap<>();

	public BeastFarm()
	{
		registerMobs(FEEDABLE_BEASTS, QuestEventType.ON_KILL, QuestEventType.ON_SKILL_SEE);

		GrowthCapableMob temp;

		// Kookabura
		temp = new GrowthCapableMob(100, 0, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18874);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18875);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18869);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18869);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18878);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18879);
		_GrowthCapableMobs.put(18873, temp);

		temp = new GrowthCapableMob(40, 1, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18876);
		_GrowthCapableMobs.put(18874, temp);

		temp = new GrowthCapableMob(40, 1, 18869);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18877);
		_GrowthCapableMobs.put(18875, temp);

		temp = new GrowthCapableMob(25, 2, 18869);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18878);
		_GrowthCapableMobs.put(18876, temp);

		temp = new GrowthCapableMob(25, 2, 18869);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18879);
		_GrowthCapableMobs.put(18877, temp);

		// Cougar
		temp = new GrowthCapableMob(100, 0, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18881);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18882);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18870);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18870);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18885);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18886);
		_GrowthCapableMobs.put(18880, temp);

		temp = new GrowthCapableMob(40, 1, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18883);
		_GrowthCapableMobs.put(18881, temp);

		temp = new GrowthCapableMob(40, 1, 18870);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18884);
		_GrowthCapableMobs.put(18882, temp);

		temp = new GrowthCapableMob(25, 2, 18870);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18885);
		_GrowthCapableMobs.put(18883, temp);

		temp = new GrowthCapableMob(25, 2, 18870);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18886);
		_GrowthCapableMobs.put(18884, temp);

		// Buffalo
		temp = new GrowthCapableMob(100, 0, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18888);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18889);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18871);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18871);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18892);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18893);
		_GrowthCapableMobs.put(18887, temp);

		temp = new GrowthCapableMob(40, 1, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18890);
		_GrowthCapableMobs.put(18888, temp);

		temp = new GrowthCapableMob(40, 1, 18871);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18891);
		_GrowthCapableMobs.put(18889, temp);

		temp = new GrowthCapableMob(25, 2, 18871);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18892);
		_GrowthCapableMobs.put(18890, temp);

		temp = new GrowthCapableMob(25, 2, 18871);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18893);
		_GrowthCapableMobs.put(18891, temp);

		// Grendel
		temp = new GrowthCapableMob(100, 0, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18895);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18896);
		temp.addNpcIdForSkillId(SKILL_BLESSED_GOLDEN_SPICE, 18872);
		temp.addNpcIdForSkillId(SKILL_BLESSED_CRYSTAL_SPICE, 18872);
		temp.addNpcIdForSkillId(SKILL_SGRADE_GOLDEN_SPICE, 18899);
		temp.addNpcIdForSkillId(SKILL_SGRADE_CRYSTAL_SPICE, 18900);
		_GrowthCapableMobs.put(18894, temp);

		temp = new GrowthCapableMob(40, 1, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18897);
		_GrowthCapableMobs.put(18895, temp);

		temp = new GrowthCapableMob(40, 1, 18872);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18898);
		_GrowthCapableMobs.put(18896, temp);

		temp = new GrowthCapableMob(25, 2, 18872);
		temp.addNpcIdForSkillId(SKILL_GOLDEN_SPICE, 18899);
		_GrowthCapableMobs.put(18897, temp);

		temp = new GrowthCapableMob(25, 2, 18872);
		temp.addNpcIdForSkillId(SKILL_CRYSTAL_SPICE, 18900);
		_GrowthCapableMobs.put(18898, temp);

		// Tamed beasts data
		SkillHolder[] stemp = new SkillHolder[2];
		stemp[0] = new SkillHolder(6432, 1);
		stemp[1] = new SkillHolder(6668, 1);
		_TamedBeastsData.put("%name% of Focus", stemp);

		stemp = new SkillHolder[2];
		stemp[0] = new SkillHolder(6433, 1);
		stemp[1] = new SkillHolder(6670, 1);
		_TamedBeastsData.put("%name% of Guiding", stemp);

		stemp = new SkillHolder[2];
		stemp[0] = new SkillHolder(6434, 1);
		stemp[1] = new SkillHolder(6667, 1);
		_TamedBeastsData.put("%name% of Swifth", stemp);

		stemp = new SkillHolder[1];
		stemp[0] = new SkillHolder(6671, 1);
		_TamedBeastsData.put("Berserker %name%", stemp);

		stemp = new SkillHolder[2];
		stemp[0] = new SkillHolder(6669, 1);
		stemp[1] = new SkillHolder(6672, 1);
		_TamedBeastsData.put("%name% of Protect", stemp);

		stemp = new SkillHolder[2];
		stemp[0] = new SkillHolder(6431, 1);
		stemp[1] = new SkillHolder(6666, 1);
		_TamedBeastsData.put("%name% of Vigor", stemp);
	}

	public static void main(String[] args)
	{
		new BeastFarm();
	}

	public void spawnNext(L2Npc npc, L2PcInstance player, int nextNpcId, int food)
	{
		// remove the feedinfo of the mob that got despawned, if any
		if(_FeedInfo.containsKey(npc.getObjectId()))
		{
			if(_FeedInfo.get(npc.getObjectId()) == player.getObjectId())
			{
				_FeedInfo.remove(npc.getObjectId());
			}
		}
		// despawn the old mob
		//TODO: same code? FIXED?
		/*if (_GrowthCapableMobs.get(npc.getNpcId()).getGrowthLevel() == 0)
		{
			npc.deleteMe();
		}
		else
		{*/
		npc.getLocationController().delete();
		//}

		// if this is finally a trained mob, then despawn any other trained mobs that the
		// player might have and initialize the Tamed Beast.
		if(ArrayUtils.contains(TAMED_BEASTS, nextNpcId))
		{
			L2TamedBeastInstance nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), player, food, npc.getX(), npc.getY(), npc.getZ(), true);

			String name = _TamedBeastsData.keySet().toArray(new String[_TamedBeastsData.keySet().size()])[Rnd.get(_TamedBeastsData.size())];
			SkillHolder[] skillList = _TamedBeastsData.get(name);
			switch(nextNpcId)
			{
				case 18869:
					name = name.replace("%name%", "Alpine Kookaburra");
					break;
				case 18870:
					name = name.replace("%name%", "Alpine Cougar");
					break;
				case 18871:
					name = name.replace("%name%", "Alpine Buffalo");
					break;
				case 18872:
					name = name.replace("%name%", "Alpine Grendel");
					break;
			}
			nextNpc.setName(name);
			nextNpc.broadcastPacket(new NpcInfo(nextNpc));
			for(SkillHolder sh : skillList)
			{
				nextNpc.addBeastSkill(SkillTable.getInstance().getInfo(sh.getSkillId(), sh.getSkillLvl()));
			}
			nextNpc.setRunning();

			QuestState st = player.getQuestState(_00020_BringUpWithLove.class);
			if(st != null && st.getCond() == 1 && !st.hasQuestItems(7185) && Rnd.get(10) == 1)
			{
				//if player has quest 20 going, give quest item
				//it's easier to hardcode it in here than to try and repeat this stuff in the quest
				st.giveItems(7185, 1);
				st.setCond(2);
			}
		}
		else
		{
			// if not trained, the newly spawned mob will automatically be agro against its feeder
			// (what happened to "never bite the hand that feeds you" anyway?!)
			L2Attackable nextNpc = (L2Attackable) addSpawn(nextNpcId, npc);

			// register the player in the feedinfo for the mob that just spawned
			_FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
			nextNpc.setRunning();
			nextNpc.addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);

			player.sendPacket(new MyTargetSelected(nextNpc.getObjectId(), player.getLevel() - nextNpc.getLevel()));
			StatusUpdate su = new StatusUpdate(nextNpc);
			su.addAttribute(StatusUpdate.CUR_HP, (int) nextNpc.getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, nextNpc.getMaxHp());
			player.sendPacket(su);
			player.setTarget(nextNpc);
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		// remove the feedinfo of the mob that got killed, if any
		if(_FeedInfo.containsKey(npc.getObjectId()))
		{
			_FeedInfo.remove(npc.getObjectId());
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		// this behavior is only run when the target of skill is the passed npc (chest)
		// i.e. when the player is attempting to open the chest using a skill
		if(!ArrayUtils.contains(targets, npc))
		{
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		}
		// gather some values on local variables
		int npcId = npc.getNpcId();
		int skillId = skill.getId();
		// check if the npc and skills used are valid for this script.  Exit if invalid.
		if(!ArrayUtils.contains(FEEDABLE_BEASTS, npcId) || skillId != SKILL_GOLDEN_SPICE && skillId != SKILL_CRYSTAL_SPICE && skillId != SKILL_BLESSED_GOLDEN_SPICE && skillId != SKILL_BLESSED_CRYSTAL_SPICE && skillId != SKILL_SGRADE_GOLDEN_SPICE && skillId != SKILL_SGRADE_CRYSTAL_SPICE)
		{
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		}

		// first gather some values on local variables
		int objectId = npc.getObjectId();
		int growthLevel = 3;  // if a mob is in FEEDABLE_BEASTS but not in _GrowthCapableMobs, then it's at max growth (3)
		if(_GrowthCapableMobs.containsKey(npcId))
		{
			growthLevel = _GrowthCapableMobs.get(npcId).getGrowthLevel();
		}

		// prevent exploit which allows 2 players to simultaneously raise the same 0-growth beast
		// If the mob is at 0th level (when it still listens to all feeders) lock it to the first feeder!
		if(growthLevel == 0 && _FeedInfo.containsKey(objectId))
		{
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		}
		_FeedInfo.put(objectId, caster.getObjectId());

		// display the social action of the beast eating the food.
		npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));

		int food = 0;
		if(skillId == SKILL_GOLDEN_SPICE || skillId == SKILL_BLESSED_GOLDEN_SPICE)
		{
			food = GOLDEN_SPICE;
		}
		else if(skillId == SKILL_CRYSTAL_SPICE || skillId == SKILL_BLESSED_CRYSTAL_SPICE)
		{
			food = CRYSTAL_SPICE;
		}

		// if this pet can't grow, it's all done.
		if(_GrowthCapableMobs.containsKey(npcId))
		{
			// do nothing if this mob doesn't eat the specified food (food gets consumed but has no effect).
			int newNpcId = _GrowthCapableMobs.get(npcId).getLeveledNpcId(skillId);
			if(newNpcId == -1)
			{
				if(growthLevel == 0)
				{
					_FeedInfo.remove(objectId);
					npc.setRunning();
					((L2Attackable) npc).addDamageHate(caster, 0, 1);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
				}
				return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			if(growthLevel > 0 && _FeedInfo.get(objectId) != caster.getObjectId())
			{
				// check if this is the same player as the one who raised it from growth 0.
				// if no, then do not allow a chance to raise the pet (food gets consumed but has no effect).
				return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			spawnNext(npc, caster, newNpcId, food);
		}
		else
		{
			caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The beast spit out the feed instead of eating it."));
			((L2Attackable) npc).dropItem(caster, food, 1);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	// all mobs that grow by eating
	private static class GrowthCapableMob
	{
		private int _chance;
		private int _growthLevel;
		private int _tameNpcId;
		private TIntIntHashMap _skillSuccessNpcIdList = new TIntIntHashMap();

		public GrowthCapableMob(int chance, int growthLevel, int tameNpcId)
		{
			_chance = chance;
			_growthLevel = growthLevel;
			_tameNpcId = tameNpcId;
		}

		public void addNpcIdForSkillId(int skillId, int npcId)
		{
			_skillSuccessNpcIdList.put(skillId, npcId);
		}

		public int getGrowthLevel()
		{
			return _growthLevel;
		}

		public int getLeveledNpcId(int skillId)
		{
			if(!_skillSuccessNpcIdList.containsKey(skillId))
			{
				return -1;
			}
			else if(skillId == SKILL_BLESSED_GOLDEN_SPICE || skillId == SKILL_BLESSED_CRYSTAL_SPICE || skillId == SKILL_SGRADE_GOLDEN_SPICE || skillId == SKILL_SGRADE_CRYSTAL_SPICE)
			{
				if(Rnd.getChance(SPECIAL_SPICE_CHANCES[0]))
				{
					if(Rnd.getChance(SPECIAL_SPICE_CHANCES[1]))
					{
						return _skillSuccessNpcIdList.get(skillId);
					}
					else
					{
						return skillId == SKILL_BLESSED_GOLDEN_SPICE || skillId == SKILL_SGRADE_GOLDEN_SPICE ? _skillSuccessNpcIdList.get(SKILL_GOLDEN_SPICE) : _skillSuccessNpcIdList.get(SKILL_CRYSTAL_SPICE);
					}
				}
				else
				{
					return -1;
				}
			}
			else if(_growthLevel == 2 && Rnd.getChance(TAME_CHANCE))
			{
				return _tameNpcId;
			}
			else
			{
				return Rnd.getChance(_chance) ? _skillSuccessNpcIdList.get(skillId) : -1;
			}
		}
	}
}