package simplelibrary.net;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import simplelibrary.encryption.Encryption;
import simplelibrary.net.authentication.Authentication;
import simplelibrary.net.authentication.Authenticator;
import simplelibrary.net.packet.PacketAuthenticationRequired;
import simplelibrary.net.packet.PacketAuthentication;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplelibrary.Queue;
import simplelibrary.Sys;
import simplelibrary.config2.Config;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.encryption.Encryption.ReadyEncryption;
import simplelibrary.encryption.EncryptionNotFoundException;
import simplelibrary.net.packet.Packet;
import simplelibrary.net.packet.PacketAuthenticated;
import simplelibrary.net.packet.PacketAuthenticationConfirmed;
import simplelibrary.net.packet.PacketAuthenticationFailed;
import simplelibrary.net.packet.PacketBoolean;
import simplelibrary.net.packet.PacketCheckEncryption;
import simplelibrary.net.packet.PacketConfig;
import simplelibrary.net.packet.PacketData;
import simplelibrary.net.packet.PacketEncryptionNotSupported;
import simplelibrary.net.packet.PacketEncryptionStart;
import simplelibrary.net.packet.PacketEncryptionSupported;
import simplelibrary.net.packet.PacketInteger;
import simplelibrary.net.packet.PacketLong;
import simplelibrary.net.packet.PacketPing;
import simplelibrary.net.packet.PacketPingTest;
import simplelibrary.net.packet.PacketPingTime;
import simplelibrary.net.packet.PacketRequestEncrypt;
import simplelibrary.net.packet.PacketRequireEncrypt;
import simplelibrary.net.packet.PacketString;
import simplelibrary.net.packet.PacketValidateDatastream;
import simplelibrary.net.packet.notransmit.PacketConnectionFailed;
import simplelibrary.net.packet.notransmit.PacketEncryptionRequested;
import simplelibrary.net.packet.notransmit.PacketEncryptionRequired;
import simplelibrary.net.packet.notransmit.PacketFileTransmission;
import simplelibrary.net.packet.notransmit.PacketInvalidDatastream;
public class ConnectionManager implements AutoCloseable{
    public static final int TYPE_STREAM = 1;
    public static final int TYPE_PACKET = 1<<1;
    private static final int LISTENER_SERVER = 1<<2;
    private static final int CONNECTION_CLIENT = 1<<3;
    private static final int CONNECTION_SERVER = 1<<4;
    private static final int TYPE_FILE_IN = 1<<5;
    private static final int TYPE_FILE_OUT = 1<<6;
    private static PacketSet defaultPacketSet = new PacketSet();
    private static PacketSet basePacketSet;
    private int invalids;
    private boolean datastreamInvalid;
    private final PacketSet packetSet;
    public final ArrayList<ConnectionManager> connections = new ArrayList<>();
    public final ArrayList<ConnectionManager> connectionsAuthenticating = new ArrayList<>();
    public final Queue<Packet> inboundPackets = new Queue<>();
    private final ArrayList<Packet> encryptionPackets = new ArrayList<>();
    public final ArrayList<File> receivedFiles = new ArrayList<>();
    private final Queue<Packet> outboundPackets=new Queue<>();
    private final Queue<Packet> urgentOutboundPackets=new Queue<>();
    private final String flushWaitor = Sys.generateRandomString(100);
    private int port;
    private int broadcastPort;
    private int waitingConnectionCount;
    private boolean broadcasting = false;
    private ServerSocket listener;
    private int type;
    private Socket socket;
    private DatagramSocket broadcastSocket;
    private String host;
    private Thread monitor;
    private Thread broadcast;
    protected Thread inbound;
    protected Thread outbound;
    private InputStream in;
    private OutputStream out;
    private InputStream realIn;
    private OutputStream realOut;
    private boolean isClosed = false;
    private int timeout;
    private final ArrayList<Thread> fileOps = new ArrayList<>();
    private boolean flushable;
    private boolean multithreadedSendingEnabled = true;
    private boolean multithreadedReceivingEnabled = true;
    private final HashMap<String, Object[]> fileOutputStreamsForRecievedFiles = new HashMap<>();
    private final ArrayList<String> filenames = new ArrayList<>();
    private Authenticator authenticator;
    private ReadyEncryption outboundEncryption;
    private ReadyEncryption inboundEncryption;
    private ReadyEncryption encryptionToForce;
    private ReadyEncryption encryptionRequired;
    private Authentication authentication;
    private ConnectionManager myServer;
    private boolean isAuthenticating;
    static {
        if(!Sys.isInitialized()){
            throw new IllegalStateException("SimpleLibrary must be initialized before the connection manager can be initialized!");
        }
        registerPacketClass(new PacketPing());
        registerPacketClass(new PacketInteger());
        registerPacketClass(new PacketString());
        registerPacketClass(new PacketBoolean());
        registerPacketClass(new PacketData());
        registerPacketClass(new PacketLong());
        registerPacketClass(new PacketConfig());
        registerPacketClass(new PacketAuthenticationRequired());
        registerPacketClass(new PacketAuthentication());
        registerPacketClass(new PacketAuthenticated());
        registerPacketClass(new PacketAuthenticationFailed());
        registerPacketClass(new PacketAuthenticationConfirmed());
        registerPacketClass(new PacketEncryptionStart());
        registerPacketClass(new PacketEncryptionNotSupported());
        registerPacketClass(new PacketEncryptionSupported());
        registerPacketClass(new PacketCheckEncryption());
        registerPacketClass(new PacketRequestEncrypt());
        registerPacketClass(new PacketRequireEncrypt());
        registerPacketClass(new PacketPingTest());
        registerPacketClass(new PacketPingTime());
        registerPacketClass(new PacketValidateDatastream());
        basePacketSet = defaultPacketSet.copy();
    }
    public static ConnectionManager createServerSide(int port, int waitingConnectionCount, int timeout, int type, Authenticator authenticator, ReadyEncryption encryption) throws IOException{
        return createServerSide(port, waitingConnectionCount, timeout, type, authenticator, encryption, defaultPacketSet);
    }
    public static ConnectionManager createServerSide(int port, int waitingConnectionCount, int timeout, int type, Authenticator authenticator, ReadyEncryption encryption, PacketSet set) throws IOException{
        if(port<=0){
            throw new IllegalArgumentException("Port number must be a positive integer!");
        }else if(waitingConnectionCount<0){
            throw new IllegalArgumentException("Waiting connection count must be a positive integer!");
        }else if(type<=0){
            throw new IllegalArgumentException("Type must be a positive integer!");
        }
        if(encryption==null){
            encryption = Encryption.UNENCRYPTED;
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(type|LISTENER_SERVER);
        connection.setPort(port);
        connection.setWaitingConnectionCount(waitingConnectionCount);
        connection.setTimeout(timeout);
        connection.setAuthenticator(authenticator, encryption);
        connection.start();
        return connection;
    }
    public static ConnectionManager createServerSide(int port, int waitingConnectionCount, int timeout, int type) throws IOException{
        return createServerSide(port, waitingConnectionCount, timeout, type, defaultPacketSet);
    }
    public static ConnectionManager createServerSide(int port, int waitingConnectionCount, int timeout, int type, PacketSet set) throws IOException{
        if(port<=0){
            throw new IllegalArgumentException("Port number must be a positive integer!");
        }else if(waitingConnectionCount<0){
            throw new IllegalArgumentException("Waiting connection count must be a positive integer!");
        }else if(type<=0){
            throw new IllegalArgumentException("Type must be a positive integer!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(type|LISTENER_SERVER);
        connection.setPort(port);
        connection.setWaitingConnectionCount(waitingConnectionCount);
        connection.setTimeout(timeout);
        connection.start();
        return connection;
    }
    public static ConnectionManager createClientSide(String host, int port, int timeout, int type) throws IOException{
        return createClientSide(host, port, timeout, type, defaultPacketSet);
    }
    public static ConnectionManager createClientSide(String host, int port, int timeout, int type, PacketSet set) throws IOException{
        if(host==null||host.isEmpty()){
            throw new IllegalArgumentException("A host must be specified!");
        }else if(port<=0){
            throw new IllegalArgumentException("Port number must be a positive integer!");
        }else if(type<=0){
            throw new IllegalArgumentException("Type must be a positive integer!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(type|CONNECTION_CLIENT);
        connection.setHost(host);
        connection.setPort(port);
        connection.setTimeout(timeout);
        connection.start();
        return connection;
    }
    public static ConnectionManager createFileIn(File file) throws IOException{
        return createFileIn(file, defaultPacketSet);
    }
    public static ConnectionManager createFileIn(File file, PacketSet set) throws IOException{
        return createFileIn(file.getAbsolutePath(), set);
    }
    public static ConnectionManager createFileIn(String filepath) throws IOException{
        return createFileIn(filepath, defaultPacketSet);
    }
    public static ConnectionManager createFileIn(String filepath, PacketSet set) throws IOException{
        if(filepath==null||filepath.isEmpty()){
            throw new IllegalArgumentException("Filepath cannot be empty!");
        }else if(!new File(filepath).exists()){
            ConnectionManager manager = new ConnectionManager(set);
            manager.isClosed = true;
            return manager;
        }else if(new File(filepath).isDirectory()){
            throw new IllegalArgumentException("Cannot read a directory!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(TYPE_FILE_IN|TYPE_PACKET);
        connection.setHost(filepath);
        connection.multithreadedReceivingEnabled = false;
        connection.start();
        return connection;
    }
    public static ConnectionManager createFileOut(File file) throws IOException{
        return createFileOut(file, defaultPacketSet);
    }
    public static ConnectionManager createFileOut(File file, PacketSet set) throws IOException{
        return createFileOut(file.getAbsolutePath(), set);
    }
    public static ConnectionManager createFileOut(String filepath) throws IOException{
        return createFileOut(filepath, defaultPacketSet);
    }
    public static ConnectionManager createFileOut(String filepath, PacketSet set) throws IOException{
        if(filepath==null||filepath.isEmpty()){
            throw new IllegalArgumentException("Filepath cannot be empty!");
        }else if(!new File(filepath).exists()){
            new File(filepath).getParentFile().mkdirs();
        }else if(new File(filepath).isDirectory()){
            throw new IllegalArgumentException("Cannot write to a directory!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(TYPE_FILE_OUT|TYPE_PACKET);
        connection.setHost(filepath);
        connection.multithreadedSendingEnabled = false;
        connection.start();
        return connection;
    }
    public static ConnectionManager manageInput(InputStream in) throws IOException{
        return manageInput(in, defaultPacketSet);
    }
    public static ConnectionManager manageInput(InputStream in, PacketSet set) throws IOException{
        if(in==null){
            throw new IllegalArgumentException("Cannot manage nothing!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(TYPE_FILE_IN|TYPE_PACKET);
        connection.in = in;
        connection.start();
        return connection;
    }
    public static ConnectionManager manageOutput(OutputStream out) throws IOException{
        return manageOutput(out, defaultPacketSet);
    }
    public static ConnectionManager manageOutput(OutputStream out, PacketSet set) throws IOException{
        if(out==null){
            throw new IllegalArgumentException("Cannot manage nothing!");
        }
        ConnectionManager connection = new ConnectionManager(set);
        connection.setType(TYPE_FILE_OUT|TYPE_PACKET);
        connection.out = out;
        connection.start();
        return connection;
    }
    public static void sendPacket(OutputStream out, Packet packet) throws IOException{
        sendPacket(out, packet, defaultPacketSet);
    }
    public static void sendPacket(OutputStream out, Packet packet, PacketSet packetSet) throws IOException{
        if(packetSet==null) packetSet = defaultPacketSet;
        try{
            int packetID = packetSet.getIndex(packet.baseInstance());
            if(packetID<0){
                for(Packet p : packetSet.packets){
                    if(p.getClass()==packet.getClass()){
                        Sys.error(ErrorLevel.critical, "Packet type "+packet.getClass().getSimpleName()+" uses inconsistent base instance!", null, ErrorCategory.bug);
                        return;
                    }
                }
                Sys.error(ErrorLevel.severe, "Could not transmit unregistered packet "+packet.getClass().getSimpleName()+"!", null, ErrorCategory.bug);
                return;
            }
            DataOutputStream data = (out instanceof DataOutputStream)?(DataOutputStream)out:new DataOutputStream(out);
            data.writeInt(packetID);
            packet.writePacketData(data);
        }catch(Throwable ex){
            Sys.error(ErrorLevel.severe, "Could not send packet "+packet.toString(), ex, ErrorCategory.InternetIO);
            throw new RuntimeException(ex);
        }
    }
    public static Packet readPacket(InputStream in) throws IOException{
        return readPacket(in, defaultPacketSet);
    }
    public static Packet readPacket(InputStream in, PacketSet packetSet) throws IOException{
        if(packetSet==null) packetSet = defaultPacketSet;
        DataInputStream datastream = (in instanceof DataInputStream)?(DataInputStream)in:new DataInputStream(in);
        int packetNumber = datastream.readInt();
        Packet packet = packetSet.getPacket(packetNumber).newInstance();
        if(packet.getClass()!=packetSet.getPacket(packetNumber).getClass()){
            throw new IllegalArgumentException("BUG DETECTED:  "+packetSet.getPacket(packetNumber).getClass().getName()+".newInstance() returned an instance of "+packet.getClass().getName()+"!");
        }
        packet.readPacketData(datastream);
        return packet;
    }
    public static synchronized void registerPacketClass(Packet instance){
        defaultPacketSet.registerPacketClass(instance);
    }
    protected ConnectionManager(){
        this(defaultPacketSet);
    }
    protected ConnectionManager(PacketSet set){
        packetSet = set.copy();
    }
    public static void setDefaultPacketSet(PacketSet set){
        if(set==null) throw new NullPointerException("Cannot set null as a packet set!");
        defaultPacketSet = set;
    }
    public static PacketSet getDefaultPacketSet(){
        return defaultPacketSet.copy();
    }
    public static PacketSet getBasePacketSet(){
        return basePacketSet.copy();
    }
    public PacketSet getPacketSet(){
        return packetSet.copy();
    }
    public boolean isClosed(){
        return isClosed;
    }
    public synchronized void send(Packet packet){
        if(multithreadedSendingEnabled){
            synchronized(outboundPackets){
                outboundPackets.enqueue(packet);
            }
        }else{
            try{
                sendPacket(out, packet, packetSet);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }
    private synchronized void sendUrgent(Packet packet){
        if(multithreadedSendingEnabled){
            synchronized(outboundPackets){
                urgentOutboundPackets.enqueue(packet);
            }
        }else{
            send(packet);
        }
    }
    public Packet receive(){
        if(multithreadedReceivingEnabled){
            synchronized(inboundPackets){
                while(inboundPackets.isEmpty()&&!isClosed){
                    try{
                        inboundPackets.wait(1);
                    }catch(InterruptedException ex){}
                }
                if(inboundPackets.isEmpty()){
                    return null;
                }
                return inboundPackets.dequeue();
            }
        }else{
            Packet packet = null;
            while(packet==null||packet.getClass()==PacketPing.class){
                while(in==null){
                    try{
                        Thread.sleep(1);
                    }catch(InterruptedException ex){}
                }
                try{
                    packet = readPacket(in, packetSet);
                    if(packet==null){
                        break;
                    }
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
            }
            return packet;
        }
    }
    public void setMultithreadedSending(boolean multithreadedSendingEnabled){
        try{
            flush();
        }catch(IOException ex){
            Sys.error(ErrorLevel.severe, null, ex, ErrorCategory.other);
        }
        this.multithreadedSendingEnabled = multithreadedSendingEnabled||(type&(CONNECTION_CLIENT|CONNECTION_SERVER))>0;
    }
    public OutputStream getOutputStream() throws IOException{
        if((TYPE_STREAM&type)>0){
            return socket.getOutputStream();
        }
        return null;
    }
    public InputStream getInputStream() throws IOException{
        if((TYPE_STREAM&type)>0){
            return socket.getInputStream();
        }
        return null;
    }
    @Override
    public void close() throws IOException{
        while(!fileOps.isEmpty()||(!outboundPackets.isEmpty()&&!isClosed&&outbound!=null&&outbound.isAlive())){
            if(!fileOps.isEmpty()&&!fileOps.get(0).isAlive()){
                fileOps.remove(0);
            }else if(!fileOps.isEmpty()&&fileOps.get(0)==Thread.currentThread()){
                fileOps.remove(0);
            }else{
                try{
                    Thread.sleep(1);
                }catch(InterruptedException ex){}
            }
        }
        while((type&TYPE_FILE_OUT)>0&&out==null){
            try{
                Thread.sleep(1);
            }catch(InterruptedException ex){}
        }
        if(!isClosed&&out!=null){
            flush();
        }
        isClosed = true;
        if((type&TYPE_FILE_IN)>0){
            in.close();
        }else if((type&TYPE_FILE_OUT)>0){
            out.close();
        }else if((type&LISTENER_SERVER)>0){
            listener.close();
        }else if((type&(CONNECTION_SERVER|CONNECTION_CLIENT))>0){
            socket.close();
        }
        if(broadcasting) makeUndiscoverable();
    }
    public void flush() throws IOException{
        while(!flushable){
            try{
                Thread.sleep(1);
            }catch(InterruptedException ex){}
        }
        if(isClosed){
            return;
        }else if(out==null){
            throw new IllegalStateException("Cannot flush a nonexistent connection!");
        }
        try{
            synchronized(flushWaitor){
                flushWaitor.wait();
            }
        }catch(InterruptedException ex){}
        out.flush();
    }
    public String sendFile(final File file) {
        final String name = getNameForFile(file);
        Thread thread = new Thread(){
            @Override
            public void run(){
                FileInputStream in=null;
                try{
                    int packetSize = 10240;
                    in = new FileInputStream(file);
                    long fileLength = file.length();
                    if(fileLength==0){
                        outboundPackets.enqueue(new PacketData("INTERNAL_FILE|"+name, 1, 1, (InputStream)null));
                    }
                    int indexes = (int)((fileLength-(fileLength%packetSize))/packetSize+((fileLength%packetSize)>0?1:0));
                    int packetIndex = 1;
                    while(in.available()>0&&packetIndex<=indexes){
                        while(outboundPackets.size()>=100){
                            try{
                                Thread.sleep(1);
                            }catch(InterruptedException ex){}
                        }
                        outboundPackets.enqueue(new PacketData("INTERNAL_FILE|"+name, packetIndex, indexes, in));
                        packetIndex++;
                    }
                    flush();
                }catch(IOException ex){
                    Sys.error(ErrorLevel.severe, "Error reading file!", ex, ErrorCategory.fileIO);
                }finally{
                    try{
                        if(in!=null){
                            in.close();
                        }
                    }catch(IOException ex){
                        Sys.error(ErrorLevel.severe, "Error closing file!", ex, ErrorCategory.fileIO);
                    }
                    fileOps.remove(Thread.currentThread());
                }
            }
        };
        fileOps.add(thread);
        thread.start();
        return name;
    }
    private void setType(int type) {
        this.type = type;
    }
    private void start() throws IOException {
        if(listener!=null&&listener.isClosed()){
            listener = null;
        }
        if(socket!=null&&socket.isClosed()){
            socket = null;
        }
        if(socket==null){
            inbound = null;
            outbound = null;
        }
        if(listener==null&&(type&LISTENER_SERVER)==LISTENER_SERVER){
            listener = new ServerSocket(port, waitingConnectionCount);
            listener.setSoTimeout(timeout);
            monitor = createMonitor(null);
        }else if(socket!=null&&inbound==null&&outbound==null&&(type&CONNECTION_SERVER)==CONNECTION_SERVER){
            inbound = createMonitor("Inbound");
            outbound = createMonitor("Outbound");
        }else if(socket==null&&(type&CONNECTION_CLIENT)==CONNECTION_CLIENT){
            socket = new Socket();
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(host, port), timeout);
            inbound = createMonitor("Inbound");
            outbound = createMonitor("Outbound");
        }else if((type&TYPE_FILE_IN)>0){
            inbound = createMonitor("Inbound");
        }else if((type&TYPE_FILE_OUT)>0){
            outbound = createMonitor("Outbound");
        }
    }
    private void setPort(int port) {
        this.port = port;
    }
    private void setWaitingConnectionCount(int waitingConnectionCount) {
        this.waitingConnectionCount = waitingConnectionCount;
    }
    private void setHost(String host) {
        this.host = host;
    }
    protected Thread createMonitor(String which) {
        Thread value=null;
        if((type&LISTENER_SERVER)==LISTENER_SERVER){
            value = new Thread(){
                @Override
                public void run(){
                    while(!listener.isClosed()){
                        ConnectionManager manager = null;
                        try{
                            Socket socket = listener.accept();
                            manager = new ConnectionManager(packetSet);
                            manager.socket = socket;
                            manager.setType((type&(TYPE_STREAM|TYPE_PACKET))|CONNECTION_SERVER);
                            manager.myServer = ConnectionManager.this;
                            manager.start();
                            if(authenticator!=null){
                                //Clients require authentication.  So we handle them- first, we encrypt the connection, then we query for authentication.
                                final ConnectionManager theManager = manager;
                                connectionsAuthenticating.add(manager);
                                new Thread(){
                                    public void run(){
                                        if(outboundEncryption!=null&&!outboundEncryption.name.isEmpty()){
                                            theManager.dualEncrypt(outboundEncryption);
                                        }
                                        theManager.send(new PacketAuthenticationRequired());
                                    }
                                }.start();
                            }else{
                                connections.add(manager);
                            }
                        }catch(Exception ex){
                            if(manager!=null){
                                try{
                                    manager.socket.close();
                                }catch(IOException ex1){
                                    Sys.error(ErrorLevel.severe, "Could not close a connection!", ex1, ErrorCategory.InternetIO);
                                }
                            }
                            if(ex.getClass()!=SocketTimeoutException.class&&!"socket closed".equals(ex.getMessage())&&!isClosed){
                                Sys.error(ErrorLevel.log, "Could not accept a connection!", ex, ErrorCategory.InternetIO);
                            }
                        }
                    }
                }
            };
            value.setName("ConnectionManager Server Thread");
        }else if(((type&(CONNECTION_SERVER|CONNECTION_CLIENT))>0&&(type&TYPE_PACKET)==TYPE_PACKET)||(type&(TYPE_FILE_IN|TYPE_FILE_OUT))>0){
            switch (which) {
                case "Inbound":
                    value = new Thread() {
                        @Override
                        public void run() {
                            try {
                                realIn = in = (type&TYPE_FILE_IN)>0?(in==null?new DataInputStream(new FileInputStream(new File(host))):(in instanceof DataInputStream)?in:new DataInputStream(in)):new DataInputStream(socket.getInputStream());
                                while ((socket!=null&&!socket.isClosed())||((type&(TYPE_FILE_IN|TYPE_FILE_OUT))>0&&!isClosed)) {
                                    while (!isClosed&&((socket!=null&&!socket.isClosed())||((type&(TYPE_FILE_IN|TYPE_FILE_OUT))>0)) && (!multithreadedReceivingEnabled || inboundPackets.size()+outboundPackets.size() >= 10_000)) {
                                        try{
                                            Thread.sleep(1);
                                        }catch(InterruptedException ex){
                                            Logger.getLogger(ConnectionManager.class.getName()).
                                                    log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    if(!((socket!=null&&!socket.isClosed())||((type&(TYPE_FILE_IN|TYPE_FILE_OUT))>0&&!isClosed))){
                                        continue;
                                    }
                                    try{
                                        Packet packet = readPacket(in, packetSet);
                                        invalids = 0;
                                        if(packet.getClass()==PacketData.class&&((PacketData)packet).tag.startsWith("INTERNAL_FILE|")){
                                            recieveFilePacket((PacketData)packet);
                                        }else if(packet instanceof PacketValidateDatastream){
                                            datastreamInvalid = true;
                                        }else if(packet instanceof PacketAuthentication){
                                            myServer.authenticate(ConnectionManager.this, (PacketAuthentication) packet);
                                        }else if(packet instanceof PacketAuthenticated){
                                            onAuthenticated(((PacketAuthenticated)packet).getAuth());
                                        }else if(packet instanceof PacketAuthenticationFailed){
                                            isAuthenticating = false;
                                            inboundPackets.enqueue(packet);
                                        }else if(packet instanceof PacketEncryptionStart){
                                            PacketEncryptionStart pes = (PacketEncryptionStart)packet;
                                            if(pes.getTitle().equals("")){
                                                inboundEncryption = null;
                                                if(encryptionToForce!=null&&encryptionToForce.name.equals("")){
                                                    encryptionToForce = null;
                                                }
                                                in = realIn;
                                            }else{
                                                try{
                                                    Encryption e = Encryption.getEncryption(pes.getTitle());
                                                    if(e instanceof Encryption.LayeredEncryption){
                                                        inboundEncryption = ((Encryption.LayeredEncryption)e).readyLayers(pes.getKeys());
                                                    }else{
                                                        inboundEncryption = e.ready(pes.getKeys()[0]);
                                                    }
                                                    if(inboundEncryption.equals(encryptionToForce)){
                                                        encryptionToForce = null;
                                                    }
                                                    in = inboundEncryption.decrypt(realIn);
                                                }catch(EncryptionNotFoundException ex){
                                                    send(new PacketEncryptionNotSupported(ex.getMessage()));
                                                    inboundPackets.enqueue(new PacketConnectionFailed("Unknown Encryption Required:  "+ex.getMessage()));
                                                    close();
                                                }
                                            }
                                        }else if(packet instanceof PacketCheckEncryption){
                                            PacketCheckEncryption p = (PacketCheckEncryption)packet;
                                            if(Encryption.isSupported(p.value)){
                                                send(new PacketEncryptionSupported(p.value));
                                            }else{
                                                send(new PacketEncryptionNotSupported(p.value));
                                            }
                                        }else if(packet instanceof PacketRequestEncrypt){
                                            PacketRequestEncrypt p = (PacketRequestEncrypt)packet;
                                            //The other side has requested we encrypt our output stream.
                                            //So, if we can encrypt, we inform the other side then pass the request on to the application for approval.
                                            //If not, we inform the other side that we cannot, and the application never hears about it.
                                            if(Encryption.isSupported(p.getTitle())){
                                                try{
                                                    Encryption e = Encryption.getEncryption(p.getTitle());
                                                    ReadyEncryption re;
                                                    if(e instanceof Encryption.LayeredEncryption){
                                                        re = ((Encryption.LayeredEncryption)e).readyLayers(p.getKeys());
                                                    }else{
                                                        re = e.ready(p.getKeys()[0]);
                                                    }
                                                    send(new PacketEncryptionSupported(p.getTitle()));
                                                    inboundPackets.enqueue(new PacketEncryptionRequested(ConnectionManager.this, re));
                                                }catch(EncryptionNotFoundException ex){}//this secion of the IF only executes if one won't be thrown
                                            }else{
                                                send(new PacketEncryptionNotSupported(p.getTitle()));
                                            }
                                        }else if(packet instanceof PacketRequireEncrypt){
                                            PacketRequireEncrypt p = (PacketRequireEncrypt)packet;
                                            //The other side has demanded we encrypt our output stream.
                                            //They will ignore all further contact from us until we comply.
                                            //So, if we can encrypt, we pass the request on to the application for approval.
                                            //If not, we disconnect.
                                            if(Encryption.isSupported(p.getTitle())){
                                                try{
                                                    Encryption e = Encryption.getEncryption(p.getTitle());
                                                    if(e instanceof Encryption.LayeredEncryption){
                                                        encryptionRequired = ((Encryption.LayeredEncryption)e).readyLayers(p.getKeys());
                                                    }else{
                                                        encryptionRequired = e.ready(p.getKeys()[0]);
                                                    }
                                                    inboundPackets.enqueue(new PacketEncryptionRequired(ConnectionManager.this, encryptionRequired));
                                                }catch(EncryptionNotFoundException ex){}//this secion of the IF only executes if one won't be thrown
                                            }else{
                                                inboundPackets.enqueue(new PacketConnectionFailed("Unknown encryption required by other side:  "+p.getTitle()));
                                                close();
                                            }
                                        }else if(packet instanceof PacketEncryptionNotSupported||packet instanceof PacketEncryptionSupported){
                                            encryptionPackets.add(packet);
                                            synchronized(encryptionCheckSync){
                                                encryptionCheckSync.notifyAll();
                                            }
                                        }else if(packet.getClass()==PacketPingTest.class){
                                            PacketPingTest t = (PacketPingTest)packet;
                                            if(t.isReflected()){
                                                inboundPackets.enqueue(new PacketPingTime(t.getTime()));
                                                send(new PacketPingTime(t.getTime()));
                                            }else{
                                                sendUrgent(t);
                                            }
                                        }else if(packet.getClass()!=PacketPing.class){
                                            inboundPackets.enqueue(packet);
                                        }
                                    }catch(NullPointerException ex){
                                        invalids++;
                                        if(invalids>2){
                                            revalidateDatastream();
                                        }
                                    }catch(RuntimeException ex){
                                        if(ex.getCause()!=null&&ex.getCause() instanceof SocketException){
                                            throw ex;
                                        }
                                    }catch(SocketException ex){
                                        throw ex;
                                    }catch(Exception ex){
                                    }
                                }
                            }catch(IOException ex){
                                boolean isClosed = ConnectionManager.this.isClosed;
                                try{
                                    close();
                                }catch(IOException ex1){
                                    throw new RuntimeException(ex1);
                                }
                                if(!isClosed) Sys.error(ErrorLevel.log, "Read failed:", ex, ErrorCategory.InternetIO);
                            } finally {
                                try{
                                    if(in!=null){
                                        in.close();
                                    }
                                }catch(IOException ex){
                                    Sys.error(ErrorLevel.warning, "Could not close stream!", ex, ErrorCategory.InternetIO);
                                }
                            }
                        }
                    };
                    value.setName("ConnectionManager Inbound Thread");
                    break;
                case "Outbound":
                    value = new Thread(){
                        @Override
                        public void run(){
                            long millisOfLastPacket = 0;
                            try{
                                realOut = out = (type&TYPE_FILE_OUT)>0?(out==null?new DataOutputStream(new FileOutputStream(new File(host))):(out instanceof DataOutputStream)?out:new DataOutputStream(out)):new DataOutputStream(socket.getOutputStream());
                            }catch(IOException ex){
                                Sys.error(ErrorLevel.severe, "Could not get output stream!", ex, ErrorCategory.fileIO);
                                return;
                            }
                            flushable = true;
                            while((socket!=null&&!socket.isClosed())||((type&(TYPE_FILE_IN|TYPE_FILE_OUT))>0&&!isClosed)){
                                if(datastreamInvalid){
                                    try{
                                        for(int i = 0; i<32; i++) out.write(0);
                                        out.write(1);
                                        datastreamInvalid = false;
                                    }catch(IOException ex){}
                                    continue;
                                }
                                if((urgentOutboundPackets.isEmpty()&&outboundPackets.isEmpty())||(encryptionRequired!=null&&!(outboundPackets.peek() instanceof PacketEncryptionStart))){
                                    if (encryptionRequired!=null&&!outboundPackets.isEmpty()) {
                                        synchronized (outboundPackets) {
                                            ArrayList<Packet> lst = outboundPackets.toList();
                                            for (Iterator<Packet> it = lst.iterator(); it.hasNext();) {
                                                Packet outboundPacket = it.next();
                                                if (outboundPacket instanceof PacketEncryptionStart) {
                                                    it.remove();
                                                    lst.add(0, outboundPacket);
                                                    outboundPackets.clear();
                                                    for (Packet p : lst) {
                                                        outboundPackets.enqueue(p);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    synchronized(flushWaitor){
                                        flushWaitor.notifyAll();
                                    }
                                    if(System.currentTimeMillis()-millisOfLastPacket>=100){
                                        outboundPackets.enqueue(new PacketPing());
                                        millisOfLastPacket = System.currentTimeMillis();
                                    }
                                    try{
                                        Thread.sleep(1);
                                    }catch(InterruptedException ex){
                                        Sys.error(ErrorLevel.warning, "Could not wait for fresh outbound packets!", ex, ErrorCategory.InternetIO);
                                    }
                                    continue;
                                }
                                try{
                                    Packet packet;
                                    synchronized(outboundPackets){
                                        packet = urgentOutboundPackets.peek()==null?outboundPackets.dequeue():urgentOutboundPackets.dequeue();
                                    }
                                    if(packet==null){
                                        continue;
                                    }
                                    sendPacket(out, packet, packetSet);
                                    if(packet instanceof PacketEncryptionStart){
                                        PacketEncryptionStart p = (PacketEncryptionStart) packet;
                                        if(encryptionRequired.equals(p.encryption)){
                                            encryptionRequired = null;
                                        }
                                        if(p.encryption.name.equals("")){
                                            outboundEncryption = null;
                                            out = realOut;
                                        }else{
                                            outboundEncryption = p.encryption;
                                            out = outboundEncryption.encrypt(realOut);
                                        }
                                    }else if(packet instanceof PacketRequireEncrypt){
                                        encryptionToForce = ((PacketRequireEncrypt)packet).encryption;
                                    }
                                }catch(RuntimeException ex){
                                    if(ex.getCause()!=null&&ex.getCause() instanceof SocketException){
                                        isClosed = true;
                                        synchronized(flushWaitor){
                                            flushWaitor.notifyAll();
                                        }
                                        try{
                                            close();
                                        }catch(IOException ex1){}
                                    }
                                }catch(SocketException ex){
                                    isClosed = true;
                                    synchronized(flushWaitor){
                                        flushWaitor.notifyAll();
                                    }
                                    try{
                                        close();
                                    }catch(IOException ex1){}
                                }catch(Exception ex){}
                            }
                        }
                    };
                    value.setName("ConnectionManager Outbound Thread");
                    break;
            }
        }else if((type&(CONNECTION_SERVER|CONNECTION_CLIENT))>0&&(type&TYPE_STREAM)==TYPE_STREAM){
            value = new Thread(){//Dummy thread object, completely useless- basically, a NULL value, since it's a stream type connection.
                @Override
                public void start(){}
            };
        }
        if(value!=null){
            value.start();
        }
        return value;
    }
    private void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    private void recieveFilePacket(PacketData packet) throws IOException {
        Object[] objs = fileOutputStreamsForRecievedFiles.get(packet.tag.substring(14));
        if(objs==null){
            File file = new File(getFilepath(packet));
            file.getParentFile().mkdirs();
            objs = new Object[]{file, new FileOutputStream(file)};
            fileOutputStreamsForRecievedFiles.put(packet.tag.substring(14), objs);
        }
        File file = (File)objs[0];
        OutputStream out = (OutputStream)objs[1];
        packet.writeData(out);
        if(packet.packetNumber==packet.totalPacketCount){
            out.close();
            fileOutputStreamsForRecievedFiles.remove(packet.tag.substring(14));
            receivedFiles.add(file);
        }
        inboundPackets.enqueue(new PacketFileTransmission(packet.tag.substring(14).split("\\|", 2)[1], file.getAbsolutePath(), packet.packetNumber, packet.totalPacketCount));
    }
    private String getFilepath(PacketData packet) {
        String name = Sys.splitString(packet.tag, '|')[2];
        File file = new File(Sys.getRoot(), "Downloaded Files");
        if(!new File(file, name).exists()){
            return new File(file, name).getAbsolutePath();
        }
        int index;
        for(index = 0; new File(file, index+" "+name).exists(); index++);
        return new File(file, index+" "+name).getAbsolutePath();
    }
    private String getNameForFile(File file) {
        int index;
        for(index = 0; filenames.contains(index+"|"+file.getName()); index++);
        filenames.add(index+"|"+file.getName());
        return index+"|"+file.getName();
    }
    /**
     * Creates a pair of ConnectionManagers that loopback to each other, with no actual network activity going on.
     * The client side is returned; the server side is inserted into the <code>connections</code> list, to be treated as any other live connection
     * Only works on packet framework
     * @return The client side
     */
    public ConnectionManager createLoopback(){
        ConnectionManager[] mngrs = createLoopbacks();
        mngrs[0].myServer = this;
        if(authenticator!=null){
            connectionsAuthenticating.add(mngrs[0]);
            mngrs[0].send(new PacketAuthenticationRequired());
        }else{
            connections.add(mngrs[0]);
        }
        return mngrs[1];
    }
    public static ConnectionManager[] createLoopbacks(){
        return createLoopbacks(defaultPacketSet);
    }
    public static ConnectionManager[] createLoopbacks(PacketSet set){
        final ConnectionManager server = new ConnectionManager(set);
        final ConnectionManager client = new ConnectionManager(set);
        server.outbound = client.inbound = new Thread(){
            public void run(){
                while(!server.isClosed&&!client.isClosed){
                    Packet p = server.outboundPackets.dequeue();
                    if(p==null){
                        synchronized(server.outbound){
                            try {
                                server.outbound.wait(1);
                            } catch (InterruptedException ex) {}
                        }
                    }else if(p instanceof PacketAuthentication){
                        if(client.myServer!=null){
                            client.myServer.authenticate(client, (PacketAuthentication)p);
                        }
                    }else if(p instanceof PacketAuthenticated){
                        client.onAuthenticated(((PacketAuthenticated)p).getAuth());
                        client.inboundPackets.enqueue(p);
                    }else{
                        client.inboundPackets.enqueue(p);
                    }
                }
            }
        };
        server.outbound.setName("ConnectionManager Loopback S-C Thread");
        client.outbound = server.inbound = new Thread(){
            public void run(){
                while(!client.isClosed&&!server.isClosed){
                    Packet p = client.outboundPackets.dequeue();
                    if(p==null){
                        synchronized(client.outbound){
                            try {
                                client.outbound.wait(1);
                            } catch (InterruptedException ex) {}
                        }
                    }else if(p instanceof PacketAuthentication){
                        if(server.myServer!=null){
                            server.myServer.authenticate(server, (PacketAuthentication)p);
                        }
                    }else if(p instanceof PacketAuthenticated){
                        server.onAuthenticated(((PacketAuthenticated)p).getAuth());
                        server.inboundPackets.enqueue(p);
                    }else{
                        server.inboundPackets.enqueue(p);
                    }
                }
            }
        };
        client.outbound.setName("ConnectionManager Loopback C-S Thread");
        server.outbound.start();
        client.outbound.start();
        return new ConnectionManager[]{server, client};
    }
    private void setAuthenticator(Authenticator authenticator, ReadyEncryption encryption) {
        this.authenticator = authenticator;
        this.outboundEncryption = encryption;
    }
    public Authentication getAuthentication(){
        return authentication;
    };
    private void authenticate(final ConnectionManager client, final PacketAuthentication packet) {
        if(authenticator==null){
            return;
        }
        new Thread(){
            public void run(){
                Authentication auth = null;
                try{
                    auth = authenticator.authenticate(packet.value, client.outboundEncryption, client.inboundEncryption);
                }catch(Throwable t){
                    Sys.error(ErrorLevel.severe, "Could not authenticate client!", t, ErrorCategory.bug);
                }//If the authentication fails more completely than it should (crashes), don't let it keep us from telling the client!
                if(auth==null){
                    //If auth == null, that means the authentication failed
                    client.send(new PacketAuthenticationFailed());
                }else{
                    //If auth != null, that means the client has been authenticated- and auth now carries its authentication data, to be returned to it.
                    client.authentication = auth;
                    if(connectionsAuthenticating.remove(client)){
                        connections.add(client);
                    }
                    if(auth.requiresEncryption()){
                        try {
                            client.encryptionRequired = auth.getRequiredReadyEncryption();
                        } catch (EncryptionNotFoundException ex) {}
                    }
                    client.send(new PacketAuthenticated(auth));
                }
            }
        }.start();
    }
    private void onAuthenticated(Authentication auth) {
        if(!isAuthenticating){
            return;
        }
        isAuthenticating = false;
        authentication = auth;
        if(auth.requiresEncryption()){
            if(Encryption.isSupported(auth.getRequiredEncryption())){
                try {
                    encryptionRequired = auth.getRequiredReadyEncryption();
                    send(new PacketEncryptionStart(encryptionRequired));
                } catch (EncryptionNotFoundException ex) {}
            }else{
                inboundPackets.enqueue(new PacketConnectionFailed("Authentication requires unsupported encryption "+auth.getRequiredEncryption()));
                try {
                    close();
                    return;
                } catch (IOException ex) {}
            }
        }
        if(auth.requestsEncryption()&&Encryption.isSupported(auth.getRequestedEncryption())){
            try {
                inboundPackets.enqueue(new PacketEncryptionRequested(this, auth.getRequestedReadyEncryption()));
            } catch (EncryptionNotFoundException ex) {}
        }
        inboundPackets.enqueue(new PacketAuthenticated(auth));
        send(new PacketAuthenticationConfirmed());
    }
    public void authenticate(String username, String password){
        if(isAuthenticating||authentication!=null){
            return;
        }
        isAuthenticating = true;
        send(new PacketAuthentication(username, password));
    }
    public void authenticate(Config authData){
        if(isAuthenticating||authentication!=null){
            return;
        }
        isAuthenticating = true;
        send(new PacketAuthentication(authData));
    }
    private final Object encryptionCheckSync = new Object();
    /**
     * Checks with the other side to ensure encryption compatability.  The key is not transmitted, only the encryption algorithm name.
     * Works as if by <code>checkEncrypt(encryption, 5000);</code>
     * @param encryption The encryption to check
     * @return whether or not the other side can encrypt and decrypt this encryption.
     */
    public boolean checkEncrypt(ReadyEncryption encryption){
        return checkEncrypt(encryption, 5000);
    }
    /**
     * Checks with the other side to ensure encryption compatability.  The key is not transmitted, only the encryption algorithm name.
     * @param encryption The encryption to check
     * @param maxWaitMillis The maximum time the method will block to wait for a response
     * @return whether or not the other side can encrypt and decrypt this encryption.
     */
    public boolean checkEncrypt(ReadyEncryption encryption, long maxWaitMillis){
        synchronized(encryptionCheckSync){
            send(new PacketCheckEncryption(encryption.name));
            long over = System.currentTimeMillis()+maxWaitMillis;
            long current;
            while((current = System.currentTimeMillis())<=over){
                for (Iterator<Packet> it = encryptionPackets.iterator(); it.hasNext();) {
                    Packet p = it.next();
                    if(p instanceof PacketEncryptionNotSupported&&encryption.name.equals(((PacketEncryptionNotSupported)p).value)){
                        it.remove();
                        return false;
                    }else if(p instanceof PacketEncryptionSupported&&encryption.name.equals(((PacketEncryptionSupported)p).value)){
                        it.remove();
                        return true;
                    }
                }
                if(over>current){
                    try {
                        encryptionCheckSync.wait(over-current);
                    } catch (InterruptedException ex) {}
                }
            }
        }
        return false;
    }
    /**
     * Offers the supplied encryption.  If the other side confirms it can decrypt, this ConnectionManager switches to the supplied encryption.
     * @param encryption The encryption to switch to
     * @return Whether or not the encryption switch was successful
     */
    public boolean encrypt(ReadyEncryption encryption) {
        if(checkEncrypt(encryption)){
            forceEncrypt(encryption);
            return true;
        }
        return false;
    }
    /**
     * Forces the supplied encryption.  Outbound traffic is encrypted immediately, with only brief notice to the other side, without waiting
     * for a response.  If the other side cannot decrypt, it is left with only one option: disconnect.
     * @param encryption The encryption to force
     */
    public void forceEncrypt(ReadyEncryption encryption){
        send(new PacketEncryptionStart(encryption));
    }
    /**
     * Requests the supplied encryption.  If the other side can encrypt, it will switch immediately to the supplied encryption.
     * Note:  The other side is not required to act on this request.
     * @param encryption The encryption to request for inbound traffic
     * @return Whether or not the other side can support the requested encryption
     */
    public boolean requestEncrypt(ReadyEncryption encryption){
        synchronized(encryptionCheckSync){
            send(new PacketRequestEncrypt(encryption));
            long over = System.currentTimeMillis()+10000;
            long current;
            while((current = System.currentTimeMillis())<=over){
                for (Iterator<Packet> it = encryptionPackets.iterator(); it.hasNext();) {
                    Packet p = it.next();
                    if(p instanceof PacketEncryptionNotSupported&&encryption.name.equals(((PacketEncryptionNotSupported)p).value)){
                        it.remove();
                        return false;
                    }else if(p instanceof PacketEncryptionSupported&&encryption.name.equals(((PacketEncryptionSupported)p).value)){
                        it.remove();
                        return true;
                    }
                }
                if(over>current){
                    try {
                        encryptionCheckSync.wait(over-current);
                    } catch (InterruptedException ex) {}
                }
            }
        }
        return false;
    }
    /**
     * Requires the supplied encryption.  Any inbound traffic received prior to an inbound encryption switch to the supplied encryption is
     * completely ignored.  If the other side cannot encrypt, it is left with only one option: disconnect.
     * @param encryption The encryption to force for inbound traffic
     */
    public void requireEncrypt(ReadyEncryption encryption){
        send(new PacketRequireEncrypt(encryption));
        encryptionToForce = encryption;
    }
    /**
     * Offers and requests the supplied encryption.  If the other side supports the encryption, both directions will switch to this encryption.
     * Note:  The other side is not required to switch to the desired encryption.
     * @param encryption The encryption to request
     * @return Whether or not the encryption switch was successful
     */
    public boolean dualEncrypt(ReadyEncryption encryption){
        if(encrypt(encryption)){
            requestEncrypt(encryption);
            return true;
        }
        return false;
    }
    /**
     * Demands the supplied encryption.  Outbound traffic is immediately encrypted, with only minor notice.
     * Any inbound traffic prior to an encryption switch to the supplied is completely ignored.
     * If the other side does not support the encryption, it is left with only one option: disconnect.
     * @param encryption The encryption to demand
     */
    public void forceDualEncrypt(ReadyEncryption encryption){
        forceEncrypt(encryption);
        requireEncrypt(encryption);
    }
    public void revalidateDatastream() throws IOException{
        inboundPackets.enqueue(new PacketInvalidDatastream());
        send(new PacketValidateDatastream());
        W:while(true){
            for(int i = 0; i<32; i++){//4 0-LONG values
                if(in.read()!=0) continue W;
            }
            break;
        }
        while(in.read()!=1);
    }
    public void requestPingTest(){
        send(new PacketPingTest());
    }
    public boolean isDiscoverable(){
        return broadcasting;
    }
    public int getDiscoverablePort(){
        return broadcastPort;
    }
    public void makeDiscoverable(int portNum, String keycode, String message){
        if(broadcasting) throw new IllegalStateException("Server is alerady discoverable!");
        broadcastPort = portNum;
        broadcasting = true;
        Thread server = new Thread(){
            @Override
            public void run(){
                try {
                    broadcastSocket = new DatagramSocket(broadcastPort);
                    byte[] sendMessage = ("SIMPLIB_"+keycode+"_"+port+(message==null?"":"_"+message)).getBytes();
                    while(broadcasting&&!broadcastSocket.isClosed()){
                        InetAddress group = InetAddress.getByName("224.0.2.0");
                        DatagramPacket p = new DatagramPacket(sendMessage, sendMessage.length, group, broadcastPort);
                        broadcastSocket.send(p);
                        synchronized(keycode){
                            keycode.wait(1000);
                        }
                    }
                    if(!broadcastSocket.isClosed()){
                        broadcastSocket.close();
                    }
                } catch (IOException | InterruptedException ex) {}
            }
        };
        server.setName("SimpLib Broadcasting Thread");
        server.start();
    }
    public void makeUndiscoverable(){
        if(!broadcasting) throw new IllegalStateException("Server is already undiscoverable!");
        broadcasting = false;
        broadcastSocket.close();
    }
    public static class Discoverer{
        private final String keycode;
        MulticastSocket c;
        public final ArrayList<ServerData> servers = new ArrayList<ServerData>();
        private final ActionListener listener;
        public Discoverer(String keycode, int port, ActionListener listener) throws IOException{
            this.keycode = keycode;
            this.listener = listener;
            c = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName("224.0.2.0");
            c.joinGroup(group);
            String expectResponse = "SIMPLIB_"+keycode+"_";
            Thread receive = new Thread(){
                @Override
                public void run(){
                    byte[] recvBuf = new byte[15000];
                    while(!c.isClosed()){
                        try{
                            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                            c.receive(receivePacket);
                            //Check if the message is correct
                            String message = new String(receivePacket.getData()).trim();
                            if(message.startsWith(expectResponse)){
                                String rest = message.substring(expectResponse.length());
                                String p = rest.indexOf('_')>0?rest.substring(0, rest.indexOf('_')):rest;
                                if(rest.indexOf('_')>0) rest = rest.substring(rest.indexOf('_')+1);
                                else rest = null;
                                int port = Integer.parseInt(p);
                                noteServerIP(receivePacket.getAddress().getHostAddress(), port, rest);
                            }
                        } catch (IOException | NumberFormatException ex) {}
                    }
                }
            };
            receive.setName("SimpLib Discoverer:  Receiving Thread");
            receive.start();
        }
        public void close(){
            synchronized(keycode){
                c.close();
            }
        }
        private void noteServerIP(String ip, int port, String message){
            long time = System.currentTimeMillis();
            synchronized(servers){
                for(ServerData s : servers){
                    if(s.ip.equals(ip)&&s.port==port){
                        s.origin=time;
                        s.message = message;
                        return;
                    }
                }
                servers.add(new ServerData(ip, port, time, message));
                if(listener!=null) listener.actionPerformed(new ActionEvent(this, 0, ip+":"+port+(message==null?"":" "+message)));
            }
        }
        public static class ServerData{
            public final String ip;
            public final int port;
            public String message;
            private long origin;
            private ServerData(String ip, int port, long origin, String message){
                this.ip = ip;
                this.port = port;
                this.origin = origin;
                this.message = message;
            }
            public long getAgeInMillis(){
                return System.currentTimeMillis()-origin;
            }
        }
    }
}
