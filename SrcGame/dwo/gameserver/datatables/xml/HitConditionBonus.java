package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Character;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;

public class HitConditionBonus extends XmlDocumentParser
{
    private static int frontBonus;
    private static int sideBonus;
    private static int backBonus;
    private static int highBonus;
    private static int lowBonus;
    private static int darkBonus;
    
    protected static HitConditionBonus _instance;

    private HitConditionBonus()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static HitConditionBonus getInstance()
    {
        return _instance == null ? _instance = new HitConditionBonus() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        parseFile(FilePath.HIT_CONDITION_DATA);
        _log.log(Level.INFO, "Loaded all HitCondition bonuses.");
    }

    @Override
    protected void parseDocument(Element rootElment)
    {
        String name0;
        for(Element element : rootElment.getChildren())
        {
            int bonus = 0;
            name0 = element.getName();
            try
            {
                if(element.hasAttributes())
                {
                    bonus = Integer.parseInt(element.getAttributeValue("val"));
                }
            }
            catch(Exception e)
            {
                _log.log(Level.ERROR, "[HitConditionBonus] Could not parse condition: " + e.getMessage(), e);
            }
            finally
            {
                switch(name0)
                {
                    case "front":
                        frontBonus = bonus;
                        break;
                    case "side":
                        sideBonus = bonus;
                        break;
                    case "back":
                        backBonus = bonus;
                        break;
                    case "high":
                        highBonus = bonus;
                        break;
                    case "low":
                        lowBonus = bonus;
                        break;
                    case "dark":
                        darkBonus = bonus;
                        break;
                }
            }
        }
    }

    /**
     * @param attacker атакующий игрок
     * @param target цель игрока, для которой будут считаться бонусы
     * @return все применимые бонусы в зависимости от положения игрока к цели и времени суток
     */
    public double getConditionBonus(L2Character attacker, L2Character target)
    {
        double mod = 100;

        // Считаем бонус от высоты
        if(attacker.getZ() - target.getZ() > 50)
        {
            mod += highBonus;
        }
        else if(attacker.getZ() - target.getZ() < -50)
        {
            mod += lowBonus;
        }

        // Считаем бонус времени суток
        if(GameTimeController.getInstance().isNight())
        {
            mod += darkBonus;
        }

        // Считаем в зависимости от положения игрока к цели
        if(attacker.isInBehindOf(target))
        {
            mod += backBonus;
        }
        else
        {
            mod += attacker.isInFrontOf(target) ? frontBonus : sideBonus;
        }

        return Math.max(mod / 100, 0);
    }
}