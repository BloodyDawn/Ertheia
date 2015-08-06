package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.world.residence.ResidenceType;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.13
 * Time: 22:12
 */

public class ResidenceSiegeMusicList extends XmlDocumentParser
{
    private static final Map<ResidenceType, Map<Integer, String>> _musicData = new HashMap<>();

    protected static ResidenceSiegeMusicList instance;

    private ResidenceSiegeMusicList()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ResidenceSiegeMusicList getInstance()
    {
        return instance == null ? instance = new ResidenceSiegeMusicList() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _musicData.clear();
        parseFile(FilePath.RESIDENCE_SIEGE_MUSIC_LIST);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Playlist for " + _musicData.size() + " residence type sieges loaded.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("musicList"))
            {
                ResidenceType type = ResidenceType.valueOf(element.getAttributeValue("type"));
                Map<Integer, String> musicData = new HashMap<>();
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("music"))
                    {
                        musicData.put(Integer.parseInt(element1.getAttributeValue("startTime")), element1.getAttributeValue("name"));
                    }
                }
                _musicData.put(type, musicData);
            }
        }
    }

    /***
     * @param residenceType тип резиденции
     * @return "плейлист" для указанного типа резиденции
     */
    public Map<Integer, String> getSiegeMusicFor(ResidenceType residenceType)
    {
        return _musicData.get(residenceType);
    }
}