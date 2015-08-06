package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2ExtractableProductItem;
import dwo.gameserver.model.skills.L2ExtractableSkill;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Restoration Random effect.<br>
 * This effect is present in item skills that "extract" new items upon usage.<br>
 * This effect has been unhardcoded in order to work on targets as well.
 * @author Zoey76
 */

public class RestorationRandom extends L2Effect
{
	public RestorationRandom(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RESTORATION_RANDOM;
	}

	@Override
	public boolean onStart()
	{
		if(getEffector() == null || getEffected() == null || !getEffector().isPlayer() || !getEffected().isPlayer())
		{
			return false;
		}

		L2ExtractableSkill exSkill = getSkill().getExtractableSkill();
		if(exSkill == null)
		{
			return false;
		}

		if(exSkill.getProductItems().isEmpty())
		{
			_log.log(Level.WARN, "Extractable Skill with no data, probably wrong/empty table in Skill Id: " + getSkill().getId());
			return false;
		}

		double rndNum = 100 * Rnd.nextDouble();
		double chance = 0;
		double chanceFrom = 0;
		List<ItemHolder> creationList = new ArrayList<>();
		boolean isFish = getSkill().getName().contains("Fish");

		// Explanation for future changes:
		// You get one chance for the current skill, then you can fall into
		// one of the "areas" like in a roulette.
		// Example: for an item like Id1,A1,30;Id2,A2,50;Id3,A3,20;
		// #---#-----#--#
		// 0--30----80-100
		// If you get chance equal 45% you fall into the second zone 30-80.
		// Meaning you get the second production list.
		// Calculate extraction
		for(L2ExtractableProductItem expi : exSkill.getProductItems())
		{
			chance = expi.getChance();
			if(rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
			{
				creationList.addAll(expi.getItems());
				break;
			}
			chanceFrom += chance;
		}

		L2PcInstance player = getEffected().getActingPlayer();
		if(creationList.isEmpty())
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return false;
		}

		long count;
		for(ItemHolder item : creationList)
		{
			if(item.getId() <= 0 || item.getCount() <= 0)
			{
				continue;
			}
			count = (long) (isFish ? item.getCount() * Config.RATE_EXTR_FISH : item.getCount());
			player.addItem(ProcessType.EXTRACTABLES, item.getId(), count, getEffector(), true);
		}
		return true;
	}
}