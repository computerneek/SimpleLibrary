package simplelibrary.numbers;
public class DecimalHugeLong {
    private final byte[] digits = new byte[4_096];
    private boolean negative = false;
    public static DecimalHugeLong parse(String str){
        DecimalHugeLong val = new DecimalHugeLong();
        byte[] digits = val.digits;
        if(str.startsWith("-")){
            val.negative = true;
            str = str.substring(1);
        }
        while(!str.isEmpty()){
            digits[digits.length-str.length()] = (byte)Integer.parseInt(str.substring(0,1));
            str = str.substring(1);
        }
        return val;
    }
    public static DecimalHugeLong add(DecimalHugeLong a, DecimalHugeLong b){
        if(a.negative==b.negative){
            return do_add(a, b);
        }else{
            boolean aNeg = a.negative;
            a.negative = b.negative = false;
            boolean aGreater = isGreater(a, b);
            DecimalHugeLong c;
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
    public static DecimalHugeLong subtract(DecimalHugeLong a, DecimalHugeLong b){
        if(a==b){
            b = a.copy();
        }
        b.negative = !b.negative;
        DecimalHugeLong c = add(a, b);
        b.negative = !b.negative;
        return c;
    }
    private static DecimalHugeLong do_add(DecimalHugeLong a, DecimalHugeLong b){
        DecimalHugeLong c = new DecimalHugeLong();
        c.negative = a.negative;
        for (int i = 0; i < 4_096; i++) {
            c.digits[4_095 - i] += (byte) (a.digits[4_095 - i] + b.digits[4_095 - i]);
            if (c.digits[4_095 - i] > 0) {
                while (c.digits[4_095 - i] > 9) {
                    if (i >= 4_095) {
                        for(int j = 0; j<c.digits.length; j++){
                            c.digits[j] = 9;
                        }
                        break;
                    }
                    c.digits[4_095 - i] -= 10;
                    c.digits[4_095 - i - 1]++;
                }
            }
        }
        return c;
    }
    private static DecimalHugeLong do_subtract(DecimalHugeLong a, DecimalHugeLong b) {
        DecimalHugeLong c = new DecimalHugeLong();
        c.negative = a.negative;
        for (int i = 0; i < 4_096; i++) {
            c.digits[4_095 - i] += (byte) (a.digits[4_095 - i] - b.digits[4_095 - i]);
            if (c.digits[4_095 - i] != 0) {
                while (c.digits[4_095 - i] < 0) {
                    if (i >= 4_095) {
                        for(int j = 0; j<c.digits.length; j++){
                            c.digits[j] = 0;
                        }
                        break;
                    }
                    c.digits[4_095 - i] += 10;
                    c.digits[4_095 - i - 1]--;
                }
            }
        }
        return c;
    }
    public static boolean isGreater(DecimalHugeLong a, DecimalHugeLong b) {
        if (a.negative&&!b.negative) {
            return false;
        } else if (a.negative!=b.negative) {
            return true;
        } else {
            for (int i = 0; i < 4_096; i++) {
                if(a.digits[i]>b.digits[i]){
                    return !a.negative;
                }else if(a.digits[i]<b.digits[i]){
                    return a.negative;
                }
            }
        }
        return false;
    }
    private DecimalHugeLong() {
    }
    public boolean isNegative() {
        return negative;
    }
    public DecimalHugeLong copy() {
        DecimalHugeLong c = new DecimalHugeLong();
        c.negative = negative;
        System.arraycopy(digits, 0, c.digits, 0, digits.length);
        return c;
    }
    @Override
    public String toString() {
        String val = "";
        for (int i = 0; i < 4_096; i++) {
            if(i<0){
                continue;
            }
            if(!val.isEmpty()||digits[i]>0){
                val+=digits[i];
            }
        }
        if(val.isEmpty()){
            val = "0";
            negative = false;
        }
        return (negative?"-":"")+val;
    }
}
