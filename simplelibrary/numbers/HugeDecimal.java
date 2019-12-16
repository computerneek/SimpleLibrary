package simplelibrary.numbers;
/**
 * A HugeDecimal system, capable of supporting exact decimal numbers with up to 8192 digits of precision.
 * The supported range is, both negative and positive, from 10^-9,223,372,036,854,783,999 to 10^9,223,372,036,854,767,615.
 * That's +-Long.MAX_VALUE-8192 on the exponent.
 * Note that the most significant and least significant nonzero digits must be within 8192 digits of each other.
 * That's because numbers are stored as if by X*10^Y, where X is those 8192 digits
 * (regardless of leading or terminating zeroes) and Y is the negative of the precision.
 * @author Bryan
 */
public class HugeDecimal implements Comparable<HugeDecimal>, Cloneable{
    private static final short DIGIT_COUNT = 8192;
    private final byte[] digits = new byte[DIGIT_COUNT];
    private long firstDecimal = DIGIT_COUNT;
    private boolean negative = false;
    private static final String pi = "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811174502841027019385211055596446229489549303819644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094330572703657595919530921861173819326117931051185480744623799627495673518857527248912279381830119491298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051320005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420199561121290219608640344181598136297747713099605187072113499999983729780499510597317328160963185950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904287554687311595628638823537875937519577818577805321712268066130019278766111959092164201989380952572010654858632788659361533818279682303019520353018529689957736225994138912497217752834791315155748572424541506959508295331168617278558890750983817546374649393192550604009277016711390098488240128583616035637076601047101819429555961989467678374494482553797747268471040475346462080466842590694912933136770289891521047521620569660240580381501935112533824300355876402474964732639141992726042699227967823547816360093417216412199245863150302861829745557067498385054945885869269956909272107975093029553211653449872027559602364806654991198818347977535663698074265425278625518184175746728909777727938000816470600161452491921732172147723501414419735685481613611573525521334757418494684385233239073941433345477624168625189835694855620992192221842725502542568876717904946016534668049886272327917860857843838279679766814541009538837863609506800642251252051173929848960841284886269456042419652850222106611863067442786220391949450471237137869609563643719172874677646575739624138908658326459958133904780275900994657640789512694683983525957098258226205224894077267194782684826014769909026401363944374553050682034962524517493996514314298091906592509372216964615157098583874105978859597729754989301617539284681382686838689427741559918559252459539594310499725246808459872736446958486538367362226260991246080512438843904512441365497627807977156914359977001296160894416948685558484063534220722258284886481584560285060168427394522674676788952521385225499546667278239864565961163548862305774564980355936345681743241125150760694794510965960940252288797108931456691368672287489405601015033086179286809208747609178249385890097149096759852613655497818931297848216829989487226588048575640142704775551323796414515237462343645428584447952658678210511413547357395231134271661021359695362314429524849371871101457654035902799344037420073105785390621983874478084784896833214457138687519435064302184531910484810053706146806749192781911979399520614196634287544406437451237181921799983910159195618146751426912397489409071864942319615679452080951465502252316038819301420937621378559566389377870830390697920773467221825625996615014215030680384477345492026054146659252014974428507325186660021324340881907104863317346496514539057962685610055081066587969981635747363840525714591028970641401109712062804390397595156771577004203378699360072305587631763594218731251471205329281918261861258673215791984148488291644706095752706957220917567116722910981690915280173506712748583222871835209353965725121083579151369882091444210067510334671103141267111369908658516398315019701651511685171437657618351556508849099898599823873455283316355076479185358932261854896321329330898570642046752590709154814165498594616371802709819943099244889575712828905923233260972997120844335732654893823911932597463667305836041428138830320382490375898524374417029132765618093773444030707469211201913020330380197621101100449293215160842444859637669838952286847831235526582131449576857262433441893039686426243410773226978028073189154411010446823252716201052652272111660396665573092547110557853763466820653109896526918620564769312570586356620185581007293606598764861179104533488503461136576867532494416680396265797877185560845529654126654085306143444318586769751456614068007002378776591344017127494704205622305389945613140711270004078547332699390814546646458807972708266830634328587856983052358089330657574067954571637752542021149557615814002501262285941302164715509792592309907965473761255176567513575178296664547791745011299614890304639947132962107340437518957359614589019389713111790429782856475032031986915140287080859904801094121472213179476477726224142548545403321571853061422881375850430633217518297986622371721591607716692547487389866549494501146540628433663937900397692656721463853067360965712091807638327166416274888800786925602902284721040317211860820419000422966171196377921337575114959501566049631862947265473642523081770367515906735023507283540567040386743513622224771589150495309844489333096340878076932599397805419341447377441842631298608099888687413260472156951623965864573021631598193195167353812974167729478672422924654366800980676928238280689964004824354037014163149658979409243237896907069779422362508221688957383798623001593776471651228935786015881617557829735233446042815126272037343146531977774160319906655418763979293344195215413418994854447345673831624993419131814809277771038638773431772075456545322077709212019051660962804909263601975988281613323166636528619326686336062735676303544776280350450777235547105859548702790814356240145171806246436267945612753181340783303362542327839449753824372058353114771199260638133467768796959703098339130771098704085913374641442822772634659470474587847787201927715280731767907707157213444730605700733492436931138350493163128404251219256517980694113528013147013047816437885185290928545201165839341965621349143415956258658655705526904965209858033850722426482939728584783163057777560688876446248246857926039535277348030480290058760758251047470916439613626760449256274204208320856611906254543372131535958450687724602901618766795240616342522577195429162991930645537799140373404328752628889639958794757291746426357455254079091451357111369410911939325191076020825202618798531887705842972591677813149699009019211697173727847684726860849003377024242916513005005168323364350389517029893922334517220138128069650117844087451960121228599371623130171144484640903890644954440061986907548516026327505298349187407866808818338510228334508504860825039302133219715518430635455007668282949304137765527939751754613953984683393638304746119966538581538420568533862186725233402830871123282789212507712629463229563989898935821167456270102183564622013496715188190973038119800497340723961036854066431939509790190699639552453005450580685501956730229219139339185680344903982059551002263535361920419947455385938102343955449597783779023742161727111723643435439478221818528624085140066604433258885698670543154706965747458550332323342107301545940516553790686627333799585115625784322988273723198987571415957811196358330059408730681216028764962867446047746491599505497374256269010490377819868359381465741268049256487985561453723478673303904688383436346553794986419270563872931748723320837601123029911367938627089438799362016295154133714248928307220126901475466847653576164773794675200490757155527819653621323926406160136358155907422020203187277605277219005561484255518792530343513984425322341576233610642506390497500865627109535919465897514131034822769306247435363256916078154781811528436679570611086153315044521274739245449454236828860613408414863776700961207151249140430272538607648236341433462351897576645216413767969031495019108575984423919862916421939949072362346468441173940326591840443780513338945257423995082965912285085558215725031071257012668302402929525220118726767562204154205161841634847565169998116141010029960783869092916030288400269104140792886215078424516709087000699282120660418371806535567252532567532861291042487761825829765157959847035622262934860034158722980534989650226291748788202734209222245339856264766914905562842503912757710284027998066365825488926488025456610172967026640765590429099456815065265305371829412703369313785178609040708667114965583434347693385781711386455873678123014587687126603489139095620099393610310";
    private static HugeDecimal PI = parse(pi);
    public static HugeDecimal PI(int precision){
        if(precision<0){
            throw new IllegalArgumentException("PI cannot have a precision less than zero!");
        }else if(precision>PI.getPrecision()){
            throw new IllegalArgumentException("PI cannot have a precision greater than "+PI.getPrecision()+"!");
        }
        return PI.clone().setPrecision(precision);
    }
    /**
     * Parses a string version of a long decimal number.
     * It can parse any number;
     * the first 8192 significant digits will be transferred accurately, with the lowest non-negative precision that holds the number completely.
     * Any further digits will be treated as zeroes and, in the case of decimal numbers, completely ignored.
     * @param str
     * @return
     */
    public static HugeDecimal parse(String str) throws NumberFormatException{
        HugeDecimal val = new HugeDecimal();
        byte[] digits = val.digits;
        boolean next = str.contains(".");
        if(str.startsWith("-")){
            val.negative = true;
            str = str.substring(1);
        }
        if(next){
            //Trim off leading zeroes first
            while(str.length()>DIGIT_COUNT+1&&str.startsWith("0")){
                str = str.substring(1);
            }
            //Trim the decimal next, then shorten further, as necessary
            if(str.startsWith(".")&&str.length()>DIGIT_COUNT){
                str = str.substring(1);
                val.firstDecimal = 0;
                next = false;
                while(str.length()>DIGIT_COUNT&&str.startsWith("0")){
                    str = str.substring(1);
                    val.firstDecimal--;
                }
                //Now either the first digit is nonzero or the string is short enough, so we make sure it's the second
                if(str.length()>DIGIT_COUNT){
                    str = str.substring(0, DIGIT_COUNT);
                }
                //Now we know the string is short enough and can let the rest fall down to the main parse algorithm
            }
            //If we could not trim the decimal, we must trim the end off and test for the decimal point
            if(next&&str.length()>DIGIT_COUNT+1){
                //Before we trim, we record the decimal point's location
                val.firstDecimal = str.indexOf(".");
                str = str.substring(0, DIGIT_COUNT+1);
                //If we still have the decimal point, we can continue processing as if it had started this way- this is valid for mere decimal removal
                //However, if the decimal is missing (or the last character), we must trim another character off the end, clear the
                //decimal state, and proceed with a non-decimal number
                if(!str.contains(".")||str.endsWith(".")){
                    str = str.substring(0, DIGIT_COUNT);
                    next = false;
                }
            }
            //If the decimal is the first digit (Could happen, if the string is short enough to begin with), we add a 0
            //so our decimal location & removal routine doesn't even risk failing
            if(str.startsWith(".")){
                str = "0"+str;
            }
            if(next&&str.length()<=DIGIT_COUNT+1){
                val.firstDecimal = (short) (digits.length-str.length()+str.indexOf(".")+1);
                str = str.substring(0, str.indexOf("."))+str.substring(str.indexOf(".")+1);
            }
        }else{
            //We know it's not a decimal, but how many digits do we have?  We have to make sure it's a legal quantity.
            //So we trim off leading zeroes.
            while(str.length()>DIGIT_COUNT&&str.startsWith("0")){
                str = str.substring(1);
            }
            //If there weren't enough leading zeroes, we first record the magnitude of the number (by setting firstDecimal equal to it)
            //then we trim off excess digits.
            if(str.length()>DIGIT_COUNT){
                val.firstDecimal = str.length();
                str = str.substring(0, DIGIT_COUNT);
            }else{
                //If we don't have too many digits, we just set firstDecimal to the maximum legal number of digits-
                //which clamps the number to the high end of the array and sets the precision to 0 decimal digits.
                val.firstDecimal = DIGIT_COUNT;
            }
        }
        //Now we get to parsing the actual digits- which is easy, since we know the string is no more than 8192 characters.
        //If it contains an illegal character, we allow Integer.parseInt() to propogate its NumberFormatException up past us.
        for(int i = 0; i<str.length(); i++){
            digits[digits.length-str.length()+i] = (byte)(str.charAt(i)-'0');
        }
        return val;
    }
    /**
     * Compares two HugeDecimal numbers
     * @param a The first HugeDecimal
     * @param b The second HugeDecimal
     * @return The one that is greater, or <code>null</code> if they are equal
     */
    public static HugeDecimal compare(HugeDecimal a, HugeDecimal b){
        long magA = a.getMagnitude();
        long magB = b.getMagnitude();
        //Negative-check because magnitude-checking also zero-checks
        if(a.negative&&!b.negative){
            return b;
        }else if(b.negative&&!a.negative){
            return a;
        }
        //If one magnitude is greather than the other, it's obvious- and magnitude getting does zero-checking.
        //If they're negative, the smaller number is actually greater.
        if(magA>magB){
            return a.negative?b:a;
        }else if(magB>magA){
            return a.negative?a:b;
        }
        short indexA = 0;
        short indexB = 0;
        byte A, B;
        //Find the start point.  Since we know they have the same magnitude, regardless of their precision,
        //we know the first nonzero digit of each number is in the same spot- like the ten thousandths spot.
        for(;indexA<DIGIT_COUNT&&a.digits[indexA]==0;indexA++);
        for(;indexB<DIGIT_COUNT&&b.digits[indexB]==0;indexB++);
        while(indexA<DIGIT_COUNT&&indexB<DIGIT_COUNT){
            A = a.digits[indexA];
            B = b.digits[indexB];
            if(A>B){
                return a.negative?b:a;
            }else if(B>A){
                return a.negative?a:b;
            }
            indexA++;
            indexB++;
        }
        //One or both of the numbers has run to its end.  On the chance their precision doesn't match but the matching places do, we
        //search down the more precise one (only one loop will run- one or both of the conditions will already be FALSE at the start)
        //to find a nonzero digit that will trip it into being the bigger number.
        //Again, negative-checking; when they're negative, the bigger number is actually less!
        for(short i = indexA; i<DIGIT_COUNT; i++){
            if(a.digits[i]>0){
                return a.negative?b:a;
            }
        }
        for(short i = indexB; i<DIGIT_COUNT; i++){
            if(b.digits[i]>0){
                return a.negative?a:b;
            }
        }
        return null;
    }
    @Override
    public int compareTo(HugeDecimal other){
        HugeDecimal a = compare(this, other);
        if(a==this){
            return 1;
        }else if(a==other){
            return -1;
        }else{
            return 0;
        }
    }
    private HugeDecimal(){}
    public static HugeDecimal Zero(){
        return new HugeDecimal();
    }
    public static HugeDecimal One(){
        return parse("1");
    }
    /**
     * @return Whether this HugeDecimal is negative
     */
    public boolean isNegative(){
        return negative;
    }
    @Override
    public String toString(){
        boolean hitYet = false;
        boolean hit = false;
        String val = "";
        for(int i = 0; i<DIGIT_COUNT; i++){
            if(hitYet||digits[i]>0||firstDecimal-1<=i){
                hitYet = true;
                if(i==firstDecimal&&i>0){
                    val+=".";
                }
                val+=digits[i];
                hit |= digits[i] > 0;
            }
        }
        if(!hit){
            val = "0";
            negative = false;
            firstDecimal = DIGIT_COUNT;
        }
        String lead = negative?"-":"";
        if(firstDecimal<=0){
            hitYet = true;
            lead+="0.";
            for(long i = 0; i<-firstDecimal; i++){
                lead+="0";
            }
        }
        val = lead + val;
        for(long i = 0; i<firstDecimal-DIGIT_COUNT; i++){
            val+="0";
        }
        return val;
    }
    public String toReadableString(){
        boolean hitYet = false;
        boolean hit = false;
        String val = "";
        for(int i = 0; i<DIGIT_COUNT; i++){
            if(hitYet||digits[i]>0||firstDecimal-1<=i){
                if(hitYet&&i<firstDecimal&&(i-firstDecimal)%3==0){
                    val+=",";
                }else if(hitYet&&i>firstDecimal&&(firstDecimal-i)%3==0){
                    val+=",";
                }
                hitYet = true;
                if(i==firstDecimal&&i>0){
                    val+=".";
                }
                val+=digits[i];
                hit |= digits[i] > 0;
            }
        }
        if(!hit){
            val = "0";
            negative = false;
            firstDecimal = DIGIT_COUNT;
        }
        String lead = negative?"-":"";
        if(firstDecimal<=0){
            hitYet = true;
            lead+="0.";
            for(long i = 0; i<-firstDecimal; i++){
                lead+="0";
            }
        }
        val = lead + val;
        for(long i = 0; i<firstDecimal-DIGIT_COUNT; i++){
            if((DIGIT_COUNT-firstDecimal+i)%3==0){
                val+=",";
            }
            val+="0";
        }
        return val;
    }
    /**
     * Gets the precision of the number.
     * The precision is the number of digits after the decimal point.
     * For example, 42.275 has a precision of 3, 27.0 a precision of 1, and 7 a precision of 0.
     * Powers of 10 (and their multiples) are exceptions.  700 could have a precision from 0 to -2 inclusive.
     * NOTE:  If the precision of the number is beyond Long.MAX_VALUE, (values below 10^Long.MIN_VALUE) this function will be subject to arithmetic overflow.  The stored value is not affected when this occurs.
     * @return The precision of the number
     */
    public long getPrecision(){
        return DIGIT_COUNT - firstDecimal;
    }
    /**
     * Gets the magnitude of the number.
     * The magnitude is the number of nonzero digits before the decimal point if positive,
     * or the negative of the number of zeroes after the decimal point if negative.
     * For example, 100.57 has a magnitude of 3, 2.6 a magnitude of 1, and 0.03 a magnitude of -1.
     * @return The magnitude of the number
     */
    public long getMagnitude(){
        short mag = DIGIT_COUNT;
        for(short i = 0; i<DIGIT_COUNT; i++){
            if(digits[i]>0){
                mag = i;
                break;
            }
        }
        if(mag==DIGIT_COUNT){//The number is 0
            firstDecimal = DIGIT_COUNT;
            negative = false;
        }
        return firstDecimal-mag;
    }
    /**
     * @return Whether this HugeDecimal is equal to zero
     */
    public boolean isZero(){
        //If the magnitude is 0, we know it is <1.  Since magnitude checking also resets a zero-equal HugeDecimal,
        //the precision will also be zero, if and only if the whole HugeDecimal is 0.
        return getMagnitude()==0&&getPrecision()==0;
    }
    /**
     * Zeroes the HugeDecimal, setting both the magnitude and the precision to 0
     * @return itself, to allow chained calls
     */
    public HugeDecimal zero(){
        for(int i = 0; i<digits.length; i++){
            digits[i] = 0;
        }
        negative = false;
        firstDecimal = DIGIT_COUNT;
        return this;
    }
    /**
     * Sets the precision of this HugeDecimal.  May round or clip leading digits as necessary.  Trailing zeroes will be inserted to match the precision.
     * If the procedure would set the HugeDecimal to zero due to clipping the entire contents out, the HugeDecimal is zeroed and the precision is set.
     * See getPrecision()
     * @param precision The desired precision
     * @return itself, to allow chained calls
     */
    public HugeDecimal setPrecision(long precision){
        long mag = getMagnitude();//Will cause zeroing of an already zero-equal number
        long current = getPrecision();
        short digitsUsed = (short) (mag+current);//Guaranteed to be within the range 0-8192
        long desiredDigits = mag+precision;
        long precisionChange = precision-current;//The amount of increase in precision
        boolean willZero = false;
        if(precisionChange>8192||precisionChange<-8192){
            //If they're wiping the whole thing, we might as well finish the job
            willZero = true;
        }else if(precisionChange>0&&desiredDigits>DIGIT_COUNT){
            willZero = true;
            //Digits from the beginning will be dropped off.  We check to make sure there's still nonzero digits.
            for(int i = (int) precisionChange; i<DIGIT_COUNT; i++){
                if(digits[i]>0){
                    //We know there's still going to be a surviving digit, and won't zero it
                    willZero = false;
                    break;
                }
            }
        }else if(precisionChange<0&&mag<=-precision){
            //If we're reducing the precision to be looser than the most significant digit,
            //it's like rounding ten to the nearest thousand, so we throw it out.
            //If mag==-precision, it's like rounding ten (or ninety) to the nearest hundred, so we could end up rounding up, so we check for that
            willZero = !(mag==-precision&&digits[DIGIT_COUNT+(int)precisionChange]>4);
        }
        if(willZero){
            zero();
            firstDecimal = DIGIT_COUNT-precision;
        }else if(precisionChange>0){
            shiftLeft((short) precisionChange);
        }else if(precisionChange<0){
            byte digit = digits[DIGIT_COUNT+(int)precisionChange];
            shiftRight((short) -precisionChange);
            if(digit>4){
                digits[DIGIT_COUNT-1]++;
                finishAddition();
            }
        }
        return this;
    }
    public HugeDecimal capPrecision(long precision){
        minimizePrecision();
        if(getPrecision()>precision){
            setPrecision(precision);
            minimizePrecision();
        }
        return this;
    }
    /**
     * Increases the precision to as high as it possibly can without altering the value.
     * A zero HugeDecimal will not be effected.
     * @return Itself, for chained operations
     */
    public HugeDecimal maximizePrecision(){
        if(isZero()){
            return this;
        }
        long digits = getMagnitude()+getPrecision();
        shiftLeft((short) (DIGIT_COUNT-digits));
        return this;
    }
    /**
     * Reduces the precision to as low as it possibly can without altering the value.
     * A zero HugeDecimal will not be effected.
     * @return Itself, for chained operations
     */
    public HugeDecimal minimizePrecision(){
        if(isZero()){
            return this;
        }
        short count = 0;
        for(int i = DIGIT_COUNT-1; i>=0; i--){
            if(digits[i]==0){
                count++;
            }else{
                break;
            }
        }
        shiftRight(count);
        return this;
    }
    private HugeDecimal shiftLeft(short change){
        if(change<1){
            return this;
        }
        for(int i = 0; i<DIGIT_COUNT-change; i++){
            digits[i] = digits[i+change];
        }
        for(int i = DIGIT_COUNT-change; i<DIGIT_COUNT; i++){
            digits[i] = 0;
        }
        firstDecimal-=change;
        return this;
    }
    private HugeDecimal shiftRight(short change){
        if(change<1){
            return this;
        }
        for(int i = DIGIT_COUNT-1; i>=change; i--){
            digits[i] = digits[i-change];
        }
        for(int i = 0; i<change; i++){
            digits[i] = 0;
        }
        firstDecimal+=change;
        return this;
    }
    private void finishAddition(){
        for(int i = DIGIT_COUNT-1; i>0; i--){
            while(digits[i]>9){
                digits[i]-=10;
                digits[i-1]++;
            }
        }
        if(digits[0]>9){
            shiftRight((short)1);
            finishAddition();
        }
    }
    private void finishSubtraction(){
        for(int i = DIGIT_COUNT-1; i>0; i--){
            if(digits[i]<0){
                digits[i]+=10;
                digits[i-1]--;
            }
        }
        if(digits[0]<0){
            digits[0] = 1;
            for(int i = 1; i<DIGIT_COUNT; i++){
                digits[i] = (byte) -digits[i];
            }
            negative = !negative;
            finishSubtraction();
        }
    }
    @Override
    public HugeDecimal clone(){
        return new HugeDecimal().write(this);
    }
    /**
     * Overwrites a HugeDecimal with the provided HugeDecimal's information
     * Operates as if by <code>this = a.clone()</code>
     * @param a The HugeDecimal to copy to this one
     * @return Itself, for chained operations
     */
    public HugeDecimal write(HugeDecimal a){
        firstDecimal = a.firstDecimal;
        negative = a.negative;
        System.arraycopy(a.digits, 0, digits, 0, DIGIT_COUNT);
        return this;
    }
    /**
     * Adds the specified HugeDecimal to this one- similar to the += operator
     * @param a The HugeDecimal to add
     * @return Itself, for chained operations
     */
    public HugeDecimal add(HugeDecimal a){
        if(a.negative!=negative){
            a = a.clone();
            a.negative = negative;
            return subtract(a);
        }
        long mag = getMagnitude();
        long mag2 = a.getMagnitude();
        long precision = getPrecision();
        long precision2 = a.getPrecision();
        long maxPrecision = Math.min(DIGIT_COUNT-mag, DIGIT_COUNT-mag2);
        long newPrecision = Math.min(maxPrecision, Math.max(precision, precision2));
        setPrecision(newPrecision);
        long offset = newPrecision-precision2;
        for(long i = (offset<0?-offset:0); i<DIGIT_COUNT-(offset>0?offset:0); i++){
            digits[(int)i]+=a.digits[(int)(i+offset)];
        }
        finishAddition();
        return this;
    }
    /**
     * Subtracts the specified HugeDecimal from this one- similar to the -= operator
     * @param a The HugeDecimal to subtract
     * @return Itself, for chained operations
     */
    public HugeDecimal subtract(HugeDecimal a){
        if(a.negative!=negative){
            a = a.clone();
            a.negative = negative;
            return add(a);
        }
        long mag = getMagnitude();
        long mag2 = a.getMagnitude();
        long precision = getPrecision();
        long precision2 = a.getPrecision();
        long maxPrecision = Math.min(DIGIT_COUNT-mag, DIGIT_COUNT-mag2);
        long newPrecision = Math.min(maxPrecision, Math.max(precision, precision2));
        setPrecision(newPrecision);
        long offset = newPrecision-precision2;
        for(long i = (offset<0?-offset:0); i<DIGIT_COUNT-(offset>0?offset:0); i++){
            digits[(int)i]-=a.digits[(int)(i+offset)];
        }
        finishSubtraction();
        return this;
    }
    /**
     * Multiplies this HugeDecimal by the specified one- similar to the *= operator
     * 
     * NOTE:  When multiplying with more than 8192 significant digits across both operands,
     * rounding errors may cause the answer to be off by a couple units in the least significant digit.
     * @param a The HugeDecimal to multiply by
     * @return Itself, for chained operations
     */
    public HugeDecimal multiply(HugeDecimal a){
        boolean willNegative = a.negative != negative;
        negative = a.negative;
        HugeDecimal b = clone();
        HugeDecimal temp = new HugeDecimal();
        zero();
        long digits = a.getMagnitude()+a.getPrecision();
        for(int i = 0; i<digits; i++){
            temp.write(b).multiply(a.digits[DIGIT_COUNT-1-i]).firstDecimal+=i-a.getPrecision();
            add(temp);
        }
        negative = willNegative;
        return this;
    }
    private HugeDecimal multiply(byte a){
        for(int i = 0; i<digits.length; i++){
            digits[i]*=a;
        }
        finishAddition();
        return this;
    }
    private HugeDecimal setDigit(long magnitude, byte value){
        long mag = getMagnitude();
        long precision = getPrecision();
        long maxPrecision = DIGIT_COUNT-magnitude;
        long minPrecision = 1-magnitude;
        if(precision<minPrecision){
            long maxPermissible = DIGIT_COUNT-mag;
            if(maxPermissible<minPrecision){
                return this;
            }
            setPrecision(minPrecision);
            precision = getPrecision();
        }else if(precision>maxPrecision){
            setPrecision(maxPrecision);
            precision = getPrecision();
        }
        long digit = precision+magnitude;
        digits[(int)(DIGIT_COUNT-digit)] = value;
        return this;
    }
    /**
     * Divides this HugeDecimal by the specified one- similar to the /= operator
     * 
     * NOTE:  When the answer is an irrational number, this function may take a long time (as in, a few seconds) to compute the first 8192 digits of the answer.
     * @param a The HugeDecimal to divide by
     * @return Itself, for chained operations
     */
    public HugeDecimal divide(HugeDecimal a){
        if(a.isZero()){
            throw new IllegalArgumentException("Cannot devide by zero");
        }else if(isZero()){
            return this;
        }
        boolean willNegative = a.negative!=negative;
        negative = a.negative;
        HugeDecimal c = a.clone();
        HugeDecimal b = clone();
        long origMag = c.getMagnitude();
        zero();
        setPrecision(b.getPrecision());
        HugeDecimal temp = new HugeDecimal();
        long magb, magc;
        byte val;
        while(!b.isZero()&&(isZero()||b.getMagnitude()+DIGIT_COUNT>=getMagnitude())){
            b.maximizePrecision();
            magb = b.getMagnitude();
            magc = c.getMagnitude();
            long difference = magb-magc;
            long digit = magb-origMag;
            c.firstDecimal+=difference;
            if(compare(b,c)!=c){
                c.firstDecimal++;
                digit++;
            }
            temp.write(c);
            c.firstDecimal--;
            val = 10;
            do{
                temp.subtract(c);
                val--;
            }while(compare(b,temp)==temp);
            b.subtract(temp);
            setDigit(digit, val);
        }
        negative = willNegative;
        return this;
    }
    /**
     * Finds the remainder of dividing this HugeDecimal by the specified one- similar to the %= operator
     * This HugeDecimal is set to the answer
     * 
     * NOTE:  Long division is performed until the remainder is less than the specified HugeDecimal.
     * This means that the result could be anything between 0 and the supplied HugeDecimal, depending on values.
     * @param a The HugeDecimal to divide by
     * @return Itself, for chained operations
     */
    public HugeDecimal remainder(HugeDecimal a){
        if(a.isZero()){
            throw new IllegalArgumentException("Cannot devide by zero");
        }
        boolean willNegative = a.negative!=negative;
        negative = a.negative;
        HugeDecimal c = a.clone();
        HugeDecimal b = clone();
        long origPrecision = getPrecision();
        zero();
        setPrecision(b.getPrecision());
        HugeDecimal temp = new HugeDecimal();
        long magb, magc;
        byte val;
        while(!b.isZero()&&b.getPrecision()-DIGIT_COUNT<getPrecision()){
            b.maximizePrecision();
            magb = b.getMagnitude();
            magc = c.getMagnitude();
            long difference = magb-magc;
            long digit = magb-magc;
            c.firstDecimal+=difference;
            if(compare(b,c)!=c){
                c.firstDecimal++;
                digit++;
            }
            temp.write(c);
            c.firstDecimal--;
            val = 10;
            do{
                temp.subtract(c);
                val--;
            }while(compare(b,temp)==temp);
            if(digit<1){
                break;
            }
            b.subtract(temp);
            setDigit(digit, val);
        }
        b.maximizePrecision();
        long p1 = b.getPrecision();
        b.minimizePrecision();
        long p2 = b.getPrecision();
        write(b);
        if(!isZero()){
            setPrecision(Math.max(p2, Math.min(p1,Math.max(origPrecision,a.getPrecision()))));
        }
        return this;
    }
    public static HugeDecimal add(HugeDecimal a, HugeDecimal b){
        return a.clone().add(b);
    }
    public static HugeDecimal subtract(HugeDecimal a, HugeDecimal b){
        return a.clone().subtract(b);
    }
    public static HugeDecimal multiply(HugeDecimal a, HugeDecimal b){
        return a.clone().multiply(b);
    }
    public static HugeDecimal divide(HugeDecimal a, HugeDecimal b){
        return a.clone().divide(b);
    }
    public static HugeDecimal remainder(HugeDecimal a, HugeDecimal b){
        return a.clone().remainder(b);
    }
}
