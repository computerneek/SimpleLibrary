package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class PacketData implements Packet{
    private static PacketData baseInstance;
    public String tag;
    public int packetNumber;
    public int totalPacketCount;
    public byte[] data;
    public PacketData(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketData(String tag, int packetNumber, int totalPacketCount, InputStream input) throws IOException{
        this.tag = tag;
        this.packetNumber = packetNumber;
        this.totalPacketCount = totalPacketCount;
        data = new byte[1_024];
        int read = input==null?0:input.read(data);
        if(read<data.length){
            byte[] filedata2 = new byte[read];
            System.arraycopy(data, 0, filedata2, 0, filedata2.length);
            data = filedata2;
        }
    }
    public PacketData(String tag, int packetNumber, int totalPacketCount, byte[] inputData){
        if(inputData.length>102400){
            throw new IllegalArgumentException("Data packet size cannot be greater than 100kb (102,400 bytes)!");
        }
        this.tag = tag;
        this.packetNumber = packetNumber;
        this.totalPacketCount = totalPacketCount;
        this.data = inputData;
    }
    public void writeData(OutputStream out) throws IOException{
        out.write(data);
    }
    @Override
    public Packet newInstance(){
        return new PacketData();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        tag = in.readUTF();
        packetNumber = in.readInt();
        totalPacketCount = in.readInt();
        data = new byte[in.readInt()];
        int read = in.read(data, 0, data.length);
        while(read<data.length){
            read+=in.read(data, read, data.length-read);
        }
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out.writeUTF(tag);
        out.writeInt(packetNumber);
        out.writeInt(totalPacketCount);
        out.writeInt(data.length);
        out.write(data);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(tag="+tag+"&packetNumber="+packetNumber+"&totalPacketCount="+totalPacketCount+"&data="+(data==null?"null":"byte["+data.length+"]")+")";
    }
}
