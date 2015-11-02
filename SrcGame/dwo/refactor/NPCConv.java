package dwo.refactor;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class NPCConv {
    private static final Map<String, String> KEY_MAPPING = new HashMap<>();
    private static final Map<String, String> VALUE_MAPPING = new HashMap<>();

    static {
        KEY_MAPPING.put("type", "type");
        KEY_MAPPING.put("level", "level");

        KEY_MAPPING.put("baseHpMax", "org_hp");
        KEY_MAPPING.put("baseHpReg", "org_hp_regen");
        KEY_MAPPING.put("baseMpMax", "org_mp");
        KEY_MAPPING.put("baseMpReg", "org_mp_regen");

        KEY_MAPPING.put("basePAtk", "base_physical_attack");
        KEY_MAPPING.put("baseMAtk", "base_magic_attack");
        KEY_MAPPING.put("basePDef", "base_defend");
        KEY_MAPPING.put("baseMDef", "base_magic_defend");

        KEY_MAPPING.put("basePAtkSpd", "base_attack_speed");
        KEY_MAPPING.put("baseAtkRange", "base_attack_range");
        KEY_MAPPING.put("basePCritRate", "base_critical");

        KEY_MAPPING.put("baseSTR", "str");
        KEY_MAPPING.put("baseCON", "con");
        KEY_MAPPING.put("baseDEX", "dex");
        KEY_MAPPING.put("baseINT", "int");
        KEY_MAPPING.put("baseWIT", "wit");
        KEY_MAPPING.put("baseMEN", "men");

        KEY_MAPPING.put("collision_radius", "collision_radius");
        KEY_MAPPING.put("collision_height", "collision_height");

        KEY_MAPPING.put("rewardExp", "exp");
        KEY_MAPPING.put("rewardSp", "sp");

        VALUE_MAPPING.put("Npc", "L2Npc");
        VALUE_MAPPING.put("Warehouse", "L2Npc");
        VALUE_MAPPING.put("NewbieGuide", "L2Npc");
        VALUE_MAPPING.put("Merchant", "L2Npc");
        VALUE_MAPPING.put("Warpgate", "L2Npc");
        VALUE_MAPPING.put("Guard", "L2Guard");
    }

    public static void main(String[] args) throws Exception {
        File l2s = new File("/home/alf/Desktop/19500-19599.xml");
        File original = new File("/home/alf/Desktop/L2WT-Ertheia/Data/game/data/stats/npc/data/19500-19599.xml");

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(l2s);
        Element rootElement = document.getRootElement();


        Element listElement = new Element("list");
        Document l2wtDocument = new Document(listElement);

        for (Element npcElement : rootElement.getChildren()) {
            Element l2wtElement = convL2sToL2wt(npcElement);
            listElement.addContent(l2wtElement);
        }

        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat().setIndent("\t"));
        xmlOutput.output(l2wtDocument, new FileWriter(original));
    }

    private static Element convL2sToL2wt(Element l2s) {
        Element l2wt = new Element("npc");
        l2wt.setAttribute("id", l2s.getAttributeValue("id"));
        l2wt.setAttribute("name", l2s.getAttributeValue("name"));
        l2wt.setAttribute("title", l2s.getAttributeValue("title"));

        l2wt.addContent(set("sex", "male"));

        for (Element elt : l2s.getChildren()) {
            final String name = elt.getName();
            switch (name) {
                case "set":
                    String k = elt.getAttributeValue("name");
                    String v = elt.getAttributeValue("value");
                    if (KEY_MAPPING.containsKey(k)) {
                        l2wt.addContent(set(KEY_MAPPING.get(k),
                                VALUE_MAPPING.containsKey(v) ? VALUE_MAPPING.get(v) : v));
                    }
                    break;
            }
        }

        l2wt.addContent(set("baseFireRes", "160"));
        l2wt.addContent(set("baseWaterRes", "160"));
        l2wt.addContent(set("baseWindRes", "160"));
        l2wt.addContent(set("baseEarthRes", "160"));
        l2wt.addContent(set("baseDivineRes", "160"));
        l2wt.addContent(set("baseDarkRes", "160"));

        Element ai_params = new Element("ai_params");
        ai_params.addContent(set("ai_type", "fighter"));

        l2wt.addContent(ai_params);

        Element skills = new Element("skills");

        l2wt.addContent(skills);

        return l2wt;
    }

    private static Element set(String name, String value) {
        Element set = new Element("set");
        set.setAttribute("name", name);
        set.setAttribute("value", value);
        return set;
    }

}
