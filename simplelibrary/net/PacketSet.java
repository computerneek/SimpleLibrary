package simplelibrary.net;
import java.util.HashMap;
import simplelibrary.net.packet.Packet;
public class PacketSet {
    Packet[] packets = new Packet[0];
    private final HashMap<Packet, Integer> packetIndexes = new HashMap<>();
    PacketSet(){}
    public synchronized void registerPacketClass(Class<? extends Packet> instance) throws InstantiationException, IllegalAccessException{
        registerPacketClass(instance.newInstance());
    }
    public synchronized void registerPacketClass(Packet instance){
        if(instance==null){
            throw new NullPointerException("Packet class cannot be null!");
        }else if(packetIndexes.containsKey(instance)){
            throw new IllegalArgumentException("Packet class already registered!");
        }
        int packetID = packets.length;
        Packet[] classes = new Packet[packets.length+1];
        System.arraycopy(packets, 0, classes, 0, packets.length);
        classes[packetID] = instance;
        packetIndexes.put(instance, packetID);
        packets = classes;
    }
    public synchronized Packet getPacket(int index){
        return index>=0&&index<packets.length?packets[index]:null;
    }
    public synchronized int getIndex(Packet packet){
        return packetIndexes.containsKey(packet)?packetIndexes.get(packet):-1;
    }
    public PacketSet copy() {
        PacketSet s = new PacketSet();
        for(Packet p : packets){
            s.registerPacketClass(p.baseInstance());
        }
        return s;
    }
}
