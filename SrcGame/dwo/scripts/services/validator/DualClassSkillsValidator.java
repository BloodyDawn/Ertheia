package dwo.scripts.services.validator;

import dwo.config.Config;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * @author DS
 */
public class DualClassSkillsValidator extends Quest
{
	// arrays must be sorted
	private static final int[] _allCertSkillIds = {
		1962, 1963, 1964, 1965, 1966, 1967, 1968, 1969, 1970, 1971, 1972, 1973, 1974, 1976, 1978, 1980, 1982, 1983,
		1984, 1985
	};
	private static final int _certItemId = 36078;

	private static final String[] VARS = {
		"EmergentAbility85", "EmergentAbility90", "EmergentAbility95", "EmergentAbility99"
	};

	public DualClassSkillsValidator()
	{
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new DualClassSkillsValidator();
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(!Config.SKILL_CHECK_ENABLE)
		{
			//return;
		}

		if(player.isGM() && !Config.SKILL_CHECK_GM)
		{
			//return;
		}

		L2Skill[] certSkills = getCertSkills(player);
		boolean hasCertSkills = certSkills != null;

		L2Skill skill;
		int[][] cSkills = null; // skillId/skillLvl
		if(hasCertSkills)
		{
			cSkills = new int[certSkills.length][2];
			for(int i = certSkills.length; --i >= 0; )
			{
				skill = certSkills[i];
				cSkills[i][0] = skill.getId();
				cSkills[i][1] = skill.getLevel();
			}
		}

		L2ItemInstance item;
		int[][] cItems = null; // objectId/number
		L2ItemInstance[] certItems = getCertItems(player);
		boolean hasCertItems = certItems != null;
		if(hasCertItems)
		{
			cItems = new int[certItems.length][2];
			for(int i = certItems.length; --i >= 0; )
			{
				item = certItems[i];
				cItems[i][0] = item.getObjectId();
				cItems[i][1] = (int) Math.min(item.getCount(), Integer.MAX_VALUE);
			}
		}

		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}

		String qValue;
		int id;
		int index;
		for(String qName : VARS)
		{
			qValue = st.getGlobalQuestVar(qName);
			if(qValue == null || qValue.isEmpty())
			{
				continue;
			}

			if(!qValue.isEmpty() && qValue.charAt(qValue.length() - 1) == ';') // found skill
			{
				try
				{
					id = Integer.parseInt(qValue.replace(";", ""));

					skill = null;
					if(hasCertSkills)
					{
						// searching skill in test array
						for(index = certSkills.length; --index >= 0; )
						{
							if(cSkills[index][0] == id)
							{
								skill = certSkills[index];
								cSkills[index][1]--;
								break;
							}
						}
						if(skill != null)
						{
							if(!ArrayUtils.contains(_allCertSkillIds, id))
							{
								// should remove this skill ?
								Util.handleIllegalPlayerAction(player, "Invalid cert variable WITH skill:" + qName + '=' + qValue + " - skill does not match certificate level", 0);
							}
						}
						else
						{
							Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - skill not found", 0);
						}
					}
					else
					{
						Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - no certified skills found", 0);
					}
				}
				catch(NumberFormatException e)
				{
					Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - not a number", 0);
				}
			}
			else // found item
			{
				try
				{
					id = Integer.parseInt(qValue);
					if(id == 0) // canceled skill, no item
					{
						continue;
					}

					item = null;
					if(hasCertItems)
					{
						// searching item in test array
						for(index = certItems.length; --index >= 0; )
						{
							if(cItems[index][0] == id)
							{
								item = certItems[index];
								cItems[index][1]--;
								break;
							}
						}
						if(item != null)
						{
							if(item.getItemId() != _certItemId)
							{
								Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - item found but does not match certificate level", 0);
							}
						}
						else
						{
							Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - item not found", 0);
						}
					}
					else
					{
						Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - no cert item found in inventory", 0);
					}

				}
				catch(NumberFormatException e)
				{
					Util.handleIllegalPlayerAction(player, "Invalid cert variable:" + qName + '=' + qValue + " - not a number", 0);
				}
			}
		}

		if(hasCertSkills)
		{
			for(int i = cSkills.length; --i >= 0; )
			{
				if(cSkills[i][1] <= 0)
				{
					continue;
				}

				skill = certSkills[i];
				if(cSkills[i][1] > 0)
				{
					if(cSkills[i][1] == skill.getLevel())
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has invalid cert skill :" + skill.getName() + '(' + skill.getId() + '/' + skill.getLevel() + ')', 0);
					}
					else
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has invalid cert skill :" + skill.getName() + '(' + skill.getId() + '/' + skill.getLevel() + "), level too high", 0);
					}

					if(Config.SKILL_CHECK_REMOVE)
					{
						player.removeSkill(skill);
					}
				}
				else
				{
					Util.handleIllegalPlayerAction(player, "Invalid cert skill :" + skill.getName() + '(' + skill.getId() + '/' + skill.getLevel() + "), level too low", 0);
				}
			}
		}

		if(hasCertItems)
		{
			for(int i = cItems.length; --i >= 0; )
			{
				if(cItems[i][1] == 0)
				{
					continue;
				}

				item = certItems[i];
				Util.handleIllegalPlayerAction(player, "Invalid cert item without variable or with wrong count:" + item.getObjectId(), 0);
			}
		}
	}

	private L2Skill[] getCertSkills(L2PcInstance player)
	{
		FastList<L2Skill> tmp = null;
		for(L2Skill s : player.getAllSkills())
		{
			if(s != null && Arrays.binarySearch(_allCertSkillIds, s.getId()) >= 0)
			{
				if(tmp == null)
				{
					tmp = FastList.newInstance();
				}

				tmp.add(s);
			}
		}
		if(tmp == null)
		{
			return null;
		}

		L2Skill[] result = tmp.toArray(new L2Skill[tmp.size()]);
		FastList.recycle(tmp);
		return result;
	}

	private L2ItemInstance[] getCertItems(L2PcInstance player)
	{
		FastList<L2ItemInstance> tmp = null;
		for(L2ItemInstance i : player.getInventory().getItems())
		{
			if(i != null && i.getItemId() == _certItemId)
			{
				if(tmp == null)
				{
					tmp = FastList.newInstance();
				}

				tmp.add(i);
			}
		}
		if(tmp == null)
		{
			return null;
		}

		L2ItemInstance[] result = tmp.toArray(new L2ItemInstance[tmp.size()]);
		FastList.recycle(tmp);
		return result;
	}
}