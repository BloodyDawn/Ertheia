package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

/**
 * User: GenCloud
 * Date: 23.03.2015
 * Team: La2Era Team
 * * 
 * 8904	1	u,Оседлать Лиса Заступника\0	u,Позволяет оседлать Лиса Заступника.\0	a,none\0	a,none\0
 * *
 * 13440	u,Воин-лис Артеи\0	a,	9C	E8	A9	-1
 * *
 */
public class FoxArteasFighter extends L2Transformation
{
    private static final int[] Skills = {9210, 5491, 9206}; //todo узнать точную инфу какие скилы даются

    public FoxArteasFighter()
    {
        super(154, 12, 22);
    }

    @Override
    public void transformedSkills()
    {
        // Decrease Bow/Crossbow Attack Speed
        getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
        // Dismount
        getPlayer().addSkill(SkillTable.getInstance().getInfo(9210, 1), false);
        // Windwalk
        getPlayer().addSkill(SkillTable.getInstance().getInfo(9206, 1), false);
        getPlayer().setTransformAllowedSkills(Skills);
    }

    @Override
    public void removeSkills()
    {
        // Decrease Bow/Crossbow Attack Speed
        getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
        // Dismount
        getPlayer().removeSkill(SkillTable.getInstance().getInfo(9210, 1), false);
        // Windwalk
        getPlayer().removeSkill(SkillTable.getInstance().getInfo(9206, 1), false);
        getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
    }
}
