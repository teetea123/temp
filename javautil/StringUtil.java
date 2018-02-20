import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    public static String escapeSql(String input) {
        input = StringUtils.replace(input, "'", "''");
//        input = StringUtils.replace(input, "%", "\\%");
//        input = StringUtils.replace(input, "_", "\\_");
        return input;
    }

    public static String strip(String origString) {
        return StringUtils.strip(origString);
    }

    //2E80-2EFF CJK 部首补充 
    //2F00-2FDF 康熙字典部首
    //3000-303F：CJK 符号和标点 (CJK Symbols and Punctuation)
    //31C0-31EF CJK 笔画
    //3200-32FF 封闭式 CJK 文字和月份 
    //3300-33FF CJK 兼容
    //3400-4DBF CJK 统一表意符号扩展 A 
    //4DC0-4DFF 易经六十四卦符号
    //4E00-9FBF：CJK 统一表意符号 (CJK Unified Ideographs)
    //F900-FAFF CJK 兼容象形文字
    //FE30-FE4F CJK 兼容形式 
    //FF00-FFEF 全角ASCII、全角标点
    public static String removeNonChinese(String orig) {
        //return orig.replaceAll("[^\\u4e00-\\u9fbb]+", "");
        //return orig.replaceAll("[^\u4e00-\u9fcc]+", "");
        return orig.replaceAll("[^\u4e00-\u9fa5]+", "");
    }

    public static String[] simpleSplit(CharSequence orig) {
        return simpleSplit(orig, false, '"', '"', false);
    }

    public static String[] simpleSplit(CharSequence orig, boolean removeEmptyInBracket) {
        return simpleSplit(orig, removeEmptyInBracket, '"', '"', false);
    }

    public static String[] simpleSplit(CharSequence orig, boolean removeEmptyInBracket,//
            char bs, char be) {
        return simpleSplit(orig, removeEmptyInBracket, bs, be, false);
    }

    public static String[] simpleSplit(CharSequence orig, char bs, char be) {
        return simpleSplit(orig, false, bs, be, false);
    }

    public static String[] simpleSplit(CharSequence orig, boolean removeEmptyInBracket,//
            char bs, char be, boolean includeBracket) {
        List<String> result = new ArrayList<String>();
        int start = 0, end = orig.length();
        for (; start < end && Character.isWhitespace(orig.charAt(start)); start++) {

        }
        if (start == end) {
            return new String[0];
        }
        boolean inBracket = false;
        for (int i = start; i < end; i++) {
            char c = orig.charAt(i);
            if (inBracket) {
                if (c == be) {
                    if (!removeEmptyInBracket || start != i) {
                        if (includeBracket) {
                            result.add(orig.subSequence(start - 1, i + 1).toString());
                        } else {
                            result.add(orig.subSequence(start, i).toString());
                        }
                    }
                    start = i + 1;
                    inBracket = false;
                }
            } else {
                if (c == bs) {
                    inBracket = true;
                    start++;

                } else if (Character.isWhitespace(c)) {
                    if (start != i) {
                        result.add(orig.subSequence(start, i).toString());
                    }
                    start = i + 1;
                }
            }
        }
        if (start != end) {
            result.add(orig.subSequence(start, end).toString());
        } else if (inBracket && !removeEmptyInBracket) {
            result.add("");//以左括号结尾时
        }
        return result.toArray(new String[result.size()]);
    }

    public static String[] split(CharSequence orig) {
        StringParser parser = new StringParser(orig);
        return parser.remainTokens();
    }

    public static String[] split(CharSequence orig, char[] wps) {
        StringParser parser = new StringParser(orig);
        parser.setWps(wps);
        return parser.remainTokens();
    }

    public static String[] split(CharSequence orig, char[] wps, boolean withDefaultWps) {
        StringParser parser = new StringParser(orig);
        parser.setWps(wps);
        parser.setWithDefaultWps(withDefaultWps);
        return parser.remainTokens();
    }

    public static String[] split(CharSequence orig, boolean removeEmptyInBracket,//
            char[] brackets, boolean includeBracket, char[] wps) {
        return split(orig, removeEmptyInBracket, brackets, includeBracket, wps, true);
    }

    public static String[] split(CharSequence orig, boolean removeEmptyInBracket,//
            char[] brackets, boolean includeBracket, char[] wps, boolean withDefaultWps) {
        StringParser parser = new StringParser(orig);
        parser.setRemoveEmptyInBracket(removeEmptyInBracket);
        parser.setBrackets(brackets);
        parser.setIncludeBracket(includeBracket);
        parser.setWps(wps);
        parser.setWithDefaultWps(withDefaultWps);
        return parser.remainTokens();
    }

    //wps 额外的空白符
    static boolean isWhitespace(char c, char[] wps) {
        return isWhitespace(c, wps, true);
    }

    static boolean isWhitespace(char c, char[] wps, boolean withDefaultWps) {
        if (wps != null) {
            for (char wp : wps) {
                if (c == wp) {
                    return true;
                }
            }
        }
        return withDefaultWps ? Character.isWhitespace(c) : false;
    }

    static int isBracket(char c, char[] brackets, boolean isStart) {
        if (brackets == null) {
            return -1;
        }
        for (int i = isStart ? 0 : 1; i < brackets.length; i++) {
            if (c == brackets[i]) {
                return i;
            }
        }
        return -1;
    }
}
