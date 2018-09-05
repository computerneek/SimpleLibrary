package simplelibrary.numbers;
public class HugeLong {
    private static final DecimalHugeLong zero = DecimalHugeLong.parse("0");
    private static final DecimalHugeLong one = DecimalHugeLong.parse("1");
    public static final HugeLong ZERO = new HugeLong();
    private final boolean[] digits = new boolean[4_096];
    private boolean negative = false;
    public static HugeLong parse(String str){
        return parse(DecimalHugeLong.parse(str));
    }
    public static HugeLong parse(DecimalHugeLong decimal){
        if(decimal==null){
            return new HugeLong();
        }
        HugeLong val = new HugeLong();
        val.negative = decimal.isNegative();
        if(val.negative){
            decimal = DecimalHugeLong.subtract(zero, decimal);
        }
        while (DecimalHugeLong.isGreater(decimal, zero)) {
            DecimalHugeLong binary = one;
            DecimalHugeLong lastBin = binary;
            int digit = 0;
            while(!DecimalHugeLong.isGreater(binary, decimal)){
                lastBin = binary;
                binary = DecimalHugeLong.add(binary, binary);
                digit++;
            }
            val.digits[4_096 - digit] = true;
            decimal = DecimalHugeLong.subtract(decimal, lastBin==one?lastBin.copy():lastBin);
        }
        return val;
    }
    public static HugeLong parse(long value){
        HugeLong val = new HugeLong();
        for(int i = 0; i<64; i++){
            val.digits[4_095 - i] = value%2==1;
            value>>=1;
        }
        return val;
    }
    /**
     * Computes a+b
     */
    public static HugeLong add(HugeLong a, HugeLong b){
        if(a==null){
            a = new HugeLong();
        }
        if(b==null){
            b = new HugeLong();
        }
        if(a.negative==b.negative){
            return do_add(a, b);
        }else{
            boolean aNeg = a.negative;
            a.negative = b.negative = false;
            boolean aGreater = isGreater(a, b);
            HugeLong c;
            if(aGreater){
                c = do_subtract(a, b);
                c.negative = aNeg;
            }else{
                c = do_subtract(b, a);
                c.negative = !aNeg;
            }
            b.negative = !(a.negative = aNeg);
            return c;
        }
    }
    public static HugeLong subtract(HugeLong a, HugeLong b){
        if(a==null){
            a = new HugeLong();
        }
        if(b==null){
            b = new HugeLong();
        }
        b.negative = !b.negative;
        HugeLong c = add(a, b);
        b.negative = !b.negative;
        return c;
    }
    private static HugeLong do_add(HugeLong a, HugeLong b){
        HugeLong c = a.copy();
        for (int i = 4_095; i>=0; i--) {
            if(b.digits[i]){
                for(int j = i; j>=0; j--){
                    if(c.digits[j] = !c.digits[j]){
                        break;
                    }
                }
            }
        }
        return c;
    }
    private static HugeLong do_subtract(HugeLong a, HugeLong b){
        HugeLong c = a.copy();
        for(int i = 4_095; i>=0; i--){
            if(b.digits[i]){
                for(int j = i; j>=0; j--){
                    if(!(c.digits[j] = !c.digits[j])){
                        break;
                    }
                }
            }
        }
        return c;
    }
    public static boolean isGreater(HugeLong a, HugeLong b){
        if(a==null){
            a = new HugeLong();
        }
        if(b==null){
            b = new HugeLong();
        }
        if (a.negative&&!b.negative) {
            return false;
        } else if (!a.negative&&b.negative) {
            return true;
        } else {
            for (int i = 0; i < 4_096; i++) {
                if(a.digits[i]!=b.digits[i]){
                    if(a.digits[i]){
                        return !a.negative;
                    }else{
                        return a.negative;
                    }
                }
            }
        }
        return false;
    }

    public static HugeLong multiply(HugeLong a, HugeLong b){
        if(a==null){
            a = new HugeLong();
        }
        if(b==null){
            b = new HugeLong();
        }
        HugeLong c = new HugeLong();
        for (int i = 4_095; i>=0; i--) {
            if (a.digits[i]) {
                c = add(c, b.shiftLeft(4_095 - i));
            }
        }
        return c;
    }
    public static HugeLong divide(HugeLong a, HugeLong b){
        if(a==null){
            a = new HugeLong();
        }
        if(b==null){
            b = new HugeLong();
        }
        boolean aNeg = a.negative;
        boolean negative = a.negative!=b.negative;
        a.negative = b.negative = false;
        if(!isGreater(b, ZERO)){
            a.negative = aNeg;
            b.negative = false;
            throw new IllegalArgumentException("Cannot devide by zero!");
        }
        int d = 0;
        for (int i = 0; i < 4_096; i++) {
            if(b.digits[i]){
                d = i;
                break;
            }
        }
        int limit = 0;
        for (int i = 0; i < 4_096; i++) {
            if(a.digits[i]){
                limit = i;
                break;
            }
        }
        HugeLong num = b.shiftLeft(d-limit);
        int digit = 4_095 - (d-limit);
        HugeLong c = new HugeLong();
        while (!isGreater(b, a) && digit < 4_096) {
            if(!HugeLong.isGreater(num, a)){
                c.digits[digit] = true;
                a = HugeLong.subtract(a, num);
            }
            num = num.shiftRight(1);
            digit++;
        }
        a.negative = aNeg;
        b.negative = negative?!aNeg:aNeg;
        c.negative = negative;
        return c;
    }
    private HugeLong(){}
    public boolean isNegative(){
        return negative;
    }
    private HugeLong shiftLeft(int distance){
        if(distance==0){
            return this;
        }
        if(distance<0){
            return shiftRight(-distance);
        }
        HugeLong c = copy();
        for (int i = 0; i < 4_096; i++) {
            c.digits[i] = i+distance < 4_096 ? digits[i+distance] : false;
        }
        return c;
    }
    private HugeLong shiftRight(int distance){
        if(distance==0){
            return this;
        }
        if(distance<0){
            return shiftLeft(-distance);
        }
        HugeLong c = copy();
        for (int i = 4_095; i>=0; i--) {
            c.digits[i] = i-distance>=0?digits[i-distance]:false;
        }
        return c;
    }
    @Override
    public String toString(){
        DecimalHugeLong val = zero;
        DecimalHugeLong bin = one;
        int sig = 0;
        for(int i = 0; i<digits.length; i++){
            if(digits[i]){
                sig = digits.length-i;
                break;
            }
        }
        for (int i = 0; i<sig; i++) {
            if (digits[4_095 - i]) {
                val = DecimalHugeLong.add(val, bin);
            }
            bin = DecimalHugeLong.add(bin, bin);
        }
        if(negative){
            val = DecimalHugeLong.subtract(zero, val);
        }
        return val.toString();
    }
    public HugeLong copy(){
        HugeLong c = new HugeLong();
        c.negative = negative;
        System.arraycopy(digits, 0, c.digits, 0, digits.length);
        return c;
    }
}
