package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.world.residence.function.FunctionData;
import dwo.gameserver.model.world.residence.function.FunctionType;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.01.13
 * Time: 20:09
 */

public class ResidenceFunctionData extends XmlDocumentParser
{
    private Map<Integer, FunctionData> _decos = new HashMap<>();
    private int[][] _ids;

    protected static ResidenceFunctionData instance;

    private ResidenceFunctionData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ResidenceFunctionData getInstance()
    {
        return instance == null ? instance = new ResidenceFunctionData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _decos.clear();
        _ids = new int[13][31];
        parseFile(FilePath.RESIDENCE_FUNCTION_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _decos.size() + " ClanHall function data's.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equals("function"))
            {
                int id = Integer.parseInt(element.getAttributeValue("id"));
                _decos.put(id, new FunctionData(id, element));

                int type = Integer.parseInt(element.getAttributeValue("type"));
                int level = Integer.parseInt(element.getAttributeValue("level"));
                _ids[type][level] = id;
            }
        }
    }

    public FunctionData getDeco(int id)
    {
        return _decos.get(id);
    }

    public FunctionData getDeco(FunctionType type, int level)
    {
        return _decos.get(_ids[type.ordinal()][level]);
    }
}