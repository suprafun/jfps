package trb.fps.net;

public class ChangeLevelPacket {
    public final String levelXml;

    public ChangeLevelPacket() {
        levelXml = "";
    }

    public ChangeLevelPacket(String levelXml)  {
        this.levelXml = levelXml;
    }
}
