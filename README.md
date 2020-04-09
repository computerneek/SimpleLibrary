# SimpleLibrary

Unless otherwise noted, the library must be initialized (Sys.init functions) BEFORE functionality is usable.<br>
Unless otherwise noted, all capability is as of Version 10.0.<br>

SimpleLibrary is designed to work alongside some other libraries.  When any given functionality relies on/uses one of these libraries, this WILL be noted.  SimpleLibrary was built with:<br>
-LWJGL 2.8.3 (www.lwjgl.org)<br>
-JLayer 1.0.1 (www.javazoom.net/javalayer/javalayer.html)<br>

### AUDIO PACKAGE
Basic, WAV-playing soundsystem, using base Java APIs.  "AudioManager" class.<br>

### CONFIG PACKAGE
Basic, key=value property system.  Comments are not supported in the save file; values are String-only.  "Config" class; use Config.loadConfig() or constructor.  Not to be confused with Config2.<br>

### CONFIG2 PACKAGE
Advanced, key-and-value tree-based configuration system.  Comments are not supported; resulting file is NOT human-readable.   Supports Boolean, Double, Float, Integer, Long, and String types; also has index-key "ConfigList" construct.  "Config" class; use Config.newConfig().  No-param overload is equivalent to the public constructor.<br>

### ENCRYPTION PACKAGE
Encryption system; no encryptions natively supported.  To implement your own encryption, implement "Encryption" class, create an instance.  Forgotten instances can be looked up with Encryption.getEncryption().  To use an encryption, use Encryption.ready() with your key- then call encrypt() or decrypt() on the resulting ReadyEncryption.<br>

### ERROR PACKAGE
For error handling.  SimpleLibrary automatically takes over uncaught exceptions when initialized; use Sys.error() to invoke this system yourself.<br>

### FONT PACKAGE
Basic Unicode/ASCII font system.  Designed for use with LWJGL (using getFontImage()); should work without (using getCharacterImage()).  Default font is incomplete, but provided (see font.info and font.png in main package); access by:<br>
	FontManager.addFont("/simplelibrary/font");<br>
	FontManager.setFont("font");<br>

### GAME PACKAGE
Requires LWJGL.<br>
Facilities designed for use by games; fully usable by non-game applications.<br>
"GameHelper" class will create a display window with an OpenGL render environment; will also run tick & render loops for you.  To use, create with constructor, use various setters, and finally call start().  SimpLib does NOT have to be initialized until start() is called; an alternative to start() is to feed it into one of the Sys.initLWJGLGame functions.<br>
"Framebuffer" class requires an active GameHelper to function.  Designed to allow easy rendering of dynamic textures.  Example:  A portal, in the game Portal 2, could be drawn by drawing the world on a Framebuffer- then drawing the resulting graphic underneath the portal frame.<br>

### LANG PACKAGE
Basic LanguageManager system, intended for easy localization.  Once language is set, feed a key and it will return language-specific strings- ex. button labels.  NOTE:  ALL language data MUST be supplied through language files (none provided).<br>

### NET PACKAGE
Packetized networking system with high-level datatypes (even including entire Config structures (Config2 package)), automatic keep-alives, optional authentication, and integrated encryption support (through Encryption package).  Use "ConnectionManager" class.<br>
To open server, use ConnectionManager.createServerSide().<br>
To connect to server, use ConnectionManager.createClientSide().  If connection is unsuccessfull, an exception will result.<br>
ConnectionManager also supports "sending to" and "receiving from" files; use ConnectionManager.createFileOut() and ConnectionManager.createFileIn() respectively.<br>
If you already have a connection established, ConnectionManager.manageInput() and ConnectionManager.manageOutput() can accept any InputStream or OutputStream for packetized data.<br>
	On a server (createServerSide):<br>
Access "connections" variable (ArrayList) for connected clients (when authentication is disabled, all clients skip to here).  I recommend you keep it empty, to allow for easy discovery of new connections.<br>
Call makeDiscoverable() to initiate UDP broadcast.  Helpful for quick-connect on LAN, in my experience.  Can be discovered by an instance of ConnectionManager.Discoverer created with the same port and keycode.<br>
Use createLoopback() to create a loopback connection; other side will be treated as a new client connecting.  Alternately, without the server, ConnectionManager.createLoopbacks() can be used for the same purpose.<br>
	On an active connection:<br>
Use send() to send packets.<br>
Use sendUrgent() to send an urgent packet.  Urgent packets skip the send queue.<br>
Use receive() to receive a packet; alternately, draw from/look at "inboundPackets" variable (Queue).<br>
Use sendFile() to send a file.  Other end will receive PacketFileTransmission updates; file will be added to receivedFiles variable (ArrayList) when transfer is complete.<br>

### NUMBERS PACKAGE
Some unnecessarily complicated very large number classes.  "DecimalHugeLong" does not support multiplication or division; neither HugeLong supports true decimal numbers (as integral-only).<br>
"HugeDecimal" supports decimal values, stored to 8192 significant digits; ranging from 10^-9,223,372,036,854,783,999 to 10^9,223,372,036,854,767,615; both negative and positive.  That's +-Long.MAX_VALUE-8192 on the exponent; numbers are stored as X*10^Y, where X is an 8192-digit integer (may have leading zeroes, may be negative) and Y is a 64-bit (signed) integer.<br>

### OPENAL PACKAGE
Requires LWJGL.<br>
OpenAL-based soundsystem; does NOT natively support 3-D positioning.  You can use SoundStash to push audio data to the hardware and refer to it.  SoundStash uses base Java APIs to read audio files; uses Texturepack system (Texture package) to locate files.<br>
I recommend using the "SoundSystem" class; handles audiosystem w/ master volume automatically- even including autoplay and/or repeating channels, like background music!  SoundSystem uses SoundStash for all audio data; additionally, SoundSystem supports a secondary sound file decoding system (data is pushed into SoundStash); add decoders through SoundSystem.addDecoder().<br>
In 10.0, no additional encodings are supported through this system.<br>
In 10.1, MP3 is supported through this system, using JLayer.  Library detection- and feature activation- is automatic on class initialization.<br>

### OPENGL PACKAGE
Requires LWJGL.<br>
OpenGL-based image system.  Contains ImageStash, as a texture-based version of SoundStash.  Uses base Java APIs to read image files; uses Texturepack system (Texture package) to locate files.<br>

### OPENGL.GUI PACKAGE
Yes, a subpackage.<br>
Also requires LWJGL.  "GUI" class; full-featured GUI system, designed to work with a GameHelper.  Allows drawing of 2-D GUI elements in a 3-D environment; also allows structured (similar to Java2D) menus.  This system is advanced/powerful enough it has even been used as a world engine for a game (It's not designed for that, so it's not very efficient with it, but there's no denying it works.)  I use this in every LWJGL application I write.<br>

### TEXTURE PACKAGE
Texturepack system; use "TexturePackManager" class.  New instance of TexturePackManager can be ignored; stored as TexturePackManager.instance.  Allows for seamless integration with potentially varying texturepack- or resourcepack- locations.  Unoverridden "TexturePack" class searches inside jarfile only; "ExternalTexturePack", created for discovered texture packs in specified texturepack directory, searches the ZIP-packaged texturepack before resorting to the default texturepack.<br>

### WINDOW PACKAGE
Utility used for accurately centering a frame on the screen.<br>

### BASE PACKAGE
	CircularStream:  An OutputStream that loops back to an associated InputStream.  Rewritten in 10.1 to fix a data corruption issue.<br>
	ErrorList:  A list of errors.  Used internally by Sys.suppress().<br>
	Queue:  An implementation of a Queue; backed by a linked list.<br>
	Stack:  An implementation of a Stack; backed by a linked list.<br>
	Sys:  Core class; contains init, program restart, error handling, single instance, and string generation utilities.<br>
	VersionManager:  A version system to simplify version management.<br>
