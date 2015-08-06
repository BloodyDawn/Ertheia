package dwo.scripts.services.validator;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
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
public class SubClassSkillsValidator extends Quest
{
	// arrays must be sorted
	private static final int[] _allCertSkillIds = {1573, 1574, 1575, 1576, 1577, 1578, 1579, 1580, 1581};
	private static final int _certItemId = 10280;

	private static final String[] VARS = {
		"EmergentAbility65-", "EmergentAbility70-", "EmergentAbility75-", "EmergentAbility80-"
	};

	public SubClassSkillsValidator()
	{
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new SubClassSkillsValidator();
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(!Config.SKILL_CHECK_ENABLE)
		{
			//	return;
		}

		if(player.isGM() && !Config.SKILL_CHECK_GM)
		{
			//	return;
		}

		L2Skill[] allCertSkillsAtChar = getCertSkills(player);
		boolean hasCertSkills = allCertSkillsAtChar != null;
		if(player.isSubClassActive())
		{
			if(hasCertSkills)
			{
				for(L2Skill s : allCertSkillsAtChar)
				{
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has cert skill on subclass :" + s.getName() + '(' + s.getId() + '/' + s.getLevel() + "), class:" + ClassTemplateTable.getInstance().getClass(player.getClassId()).getClassServName(), 0);

					if(Config.SKILL_CHECK_REMOVE)
					{
						player.removeSkill(s);
					}
				}
			}
			return;
		}

		L2Skill skill;
		int[][] copyAllCertSkillsAtChar = null; // skillId/skillLvl
		if(hasCertSkills)
		{
			copyAllCertSkillsAtChar = new int[allCertSkillsAtChar.length][2];
			for(int i = allCertSkillsAtChar.length; --i >= 0; )
			{
				skill = allCertSkillsAtChar[i];
				copyAllCertSkillsAtChar[i][0] = skill.getId();
				copyAllCertSkillsAtChar[i][1] = skill.getLevel();
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

		String qName;
		String qValue;
		int id;
		int index;
		for(int i = VARS.length; --i >= 0; )
		{
			for(int j = Config.MAX_SUBCLASS; j > 0; j--)
			{
				qName = VARS[i] + j;
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
							for(index = allCertSkillsAtChar.length; --index >= 0; )
							{
								if(copyAllCertSkillsAtChar[index][0] == id)
								{
									skill = allCertSkillsAtChar[index];
									copyAllCertSkillsAtChar[index][1]--;
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
		}

		if(hasCertSkills)
		{
			for(int i = copyAllCertSkillsAtChar.length; --i >= 0; )
			{
				if(copyAllCertSkillsAtChar[i][1] == 0)
				{
					continue;
				}

				skill = allCertSkillsAtChar[i];
				if(copyAllCertSkillsAtChar[i][1] > 0)
				{
					if(copyAllCertSkillsAtChar[i][1] == skill.getLevel())
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() +
							" has invalid cert skill :" + skill.getName() +
							'(' + skill.getId() + '/' + skill.getLevel() + ')', 0);
					}
					else
					{
						Util.handleIllegalPlayerAction(player, "Player " + player.getName() +
							" has invalid cert skill :" + skill.getName() +
							'(' + skill.getId() + '/' + skill.getLevel() + "), level too high", 0);
					}

					if(Config.SKILL_CHECK_REMOVE)
					{
						player.removeSkill(skill);
					}
				}
				else
				{
					Util.handleIllegalPlayerAction(player, "Invalid cert skill :" + skill.getName() +
						'(' + skill.getId() + '/' + skill.getLevel() + "), level too low", 0);
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