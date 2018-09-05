package simplelibrary.net.packet;
public class PacketPingTime extends PacketLong{
    private static PacketPingTime baseInstance;
    public PacketPingTime(long value){
        super(value);
    }
    public PacketPingTime(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    @Override
    public Packet newInstance(){
        return new PacketPingTime();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
