package org.cat.util;

import java.util.ArrayList;
import java.util.List;

//跨行读取符号（若干，全部）（到达结尾，或结尾只剩空白时，返回END）
//不跨行读取符号（若干，全部）
//忽略本行剩余字符
//读取下一行
//以下方法不可重复调用（因为会进入新的一行）：剩余符号，本行剩余符号，剩余字符串，本行剩余字符串，忽略本行剩余字符
public class StringParser {
    private static final char R = '\r';
    private static final char N = '\n';
    public static final String[] END = new String[0];
    private static final String _end = null;
    //
    private CharSequence str;
    private int end;
    private int mark;
    private int curIndex;
    private int lastTokenCount;
    private boolean isLastTokenNotComplete;
    //tokens，自动跳过空行，自动进入下一行
    private char[] brackets;
    private boolean includeBracket;
    private boolean removeEmptyInBracket;
    private boolean withNextLine;
    //line
    private boolean keepEmptyLine;
    //tokens and line
    private char[] wps;
    private boolean withDefaultWps = true;

    public StringParser() {
    }

    public StringParser(CharSequence str) {
        setStr(str);
    }

    //=============
    public String[] remainTokens() {
        String[] result = remainTokens_impl();
        lastTokenCount = result.length;
        curIndex = end;
        return result;
    }

    public String remainString() {
        if (curIndex == end) {
            return _end;
        }
        String result = str.subSequence(curIndex, end).toString();
        curIndex = end;
        return result;
    }

    public String[] nextTokensCurLine() {
        return remainTokensCurLine_impl();
    }

    public String remainStringCurLine() {
        return remainStringCurLine_impl();
    }

    public String[] nextTokens(int size) {
        return nextTokens(size, withNextLine);
    }

    public String[] nextTokens(int size, boolean withNextLine) {
        return withNextLine ? nextTokens_withNextLine(size)//
                : nextTokens_notWithNextLine(size);
    }

    public String nextToken() {
        return nextToken(withNextLine);
    }

    public String nextToken(boolean withNextLine) {
        String[] result = nextTokens(1, withNextLine);
        return result == END ? _end : result[0];
    }

    //============
    //会自动进入下一行（且跳过空行）
    private String[] nextTokens_notWithNextLine(int size) {
        skipWhitespace();
        if (curIndex == end) {
            lastTokenCount = 0;
            return END;
        }
        String[] result = new String[size];
        lastTokenCount = 0;
        int start = curIndex;
        int b = -1;
        while (lastTokenCount < size) {
            if (b != -1) {
                String token = nextToken_inBracket(b);
                start = curIndex;
                b = -1;
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                continue;
            }
            if (curIndex == end) {
                String token = nextToken_remainInCurIndex(start);
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                break;
            }
            char c = str.charAt(curIndex);
            if (c == R || c == N) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                rn(c);
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                break;
            }
            int bc = isBracket(c, true);
            if (bc != -1) {
                String token = nextToken_remainInCurIndex(start);
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                b = bc;
                continue;
            }
            if (isWhitespace(c)) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                start = curIndex;
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                continue;
            }
            curIndex++;
        }
        return result;
    }

    private String nextToken_inBracket(int b) {
        isLastTokenNotComplete = true;
        curIndex++;
        int start = curIndex;
        String token = null;
        for (; curIndex < end; curIndex++) {
            char c = str.charAt(curIndex);
            int bc = isBracket(c, false);
            if (bc == b + 1) {
                isLastTokenNotComplete = false;
                if (start != curIndex || !removeEmptyInBracket) {
                    if (includeBracket) {
                        token = str.subSequence(start - 1, curIndex + 1).toString();
                    } else {
                        token = str.subSequence(start, curIndex).toString();
                    }
                }
                break;
            }
        }
        if (token != null) {
            curIndex++;
        }
        return token;
    }

    private String nextToken_remainInCurIndex(int start) {
        if (start != curIndex) {
            return str.subSequence(start, curIndex).toString();
        }
        return null;
    }

    private void rn(char c) {
        if (c == R && curIndex < end) {
            if (str.charAt(curIndex) == N) {
                curIndex++;
            }
        }
        if (c == N && curIndex < end) {
            if (str.charAt(curIndex) == R) {
                curIndex++;
            }
        }
    }

    private void skipWhitespace() {
        for (; curIndex < end && isWhitespace(str.charAt(curIndex)); curIndex++) {
        }
    }

    private String[] nextTokens_withNextLine(int size) {
        skipWhitespace();
        if (curIndex == end) {
            lastTokenCount = 0;
            return END;
        }
        String[] result = new String[size];
        lastTokenCount = 0;
        int start = curIndex;
        int b = -1;
        while (lastTokenCount < size) {
            if (b != -1) {
                String token = nextToken_inBracket(b);
                start = curIndex;
                b = -1;
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                continue;
            }
            if (curIndex == end) {
                String token = nextToken_remainInCurIndex(start);
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                break;
            }
            char c = str.charAt(curIndex);
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                continue;
            }
            if (isWhitespace(c)) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                start = curIndex;
                if (token != null) {
                    result[lastTokenCount] = token;
                    lastTokenCount++;
                }
                continue;
            }
            curIndex++;
        }
        return result;
    }

    private String[] remainTokens_impl() {
        skipWhitespace();
        if (curIndex == end) {
            lastTokenCount = 0;
            return END;
        }
        List<String> result = new ArrayList<String>();
        int start = curIndex;
        int b = -1;
        while (true) {
            if (b != -1) {
                String token = nextToken_inBracket(b);
                start = curIndex;
                b = -1;
                if (token != null) {
                    result.add(token);
                }
                continue;
            }
            if (curIndex == end) {
                String token = nextToken_remainInCurIndex(start);
                if (token != null) {
                    result.add(token);
                }
                break;
            }
            char c = str.charAt(curIndex);
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                continue;
            }
            if (isWhitespace(c)) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                start = curIndex;
                if (token != null) {
                    result.add(token);
                }
                continue;
            }
            curIndex++;
        }

        lastTokenCount = result.size();
        return result.toArray(new String[result.size()]);
    }

    private String[] remainTokensCurLine_impl() {
        skipWhitespace();
        if (curIndex == end) {
            lastTokenCount = 0;
            return END;
        }
        List<String> result = new ArrayList<String>();
        int start = curIndex;
        int b = -1;
        while (true) {
            if (b != -1) {
                String token = nextToken_inBracket(b);
                start = curIndex;
                b = -1;
                if (token != null) {
                    result.add(token);
                }
                continue;
            }
            if (curIndex == end) {
                String token = nextToken_remainInCurIndex(start);
                if (token != null) {
                    result.add(token);
                }
                break;
            }
            char c = str.charAt(curIndex);
            if (c == R || c == N) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                rn(c);
                if (token != null) {
                    result.add(token);
                }
                break;
            }
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                continue;
            }
            if (isWhitespace(c)) {
                String token = nextToken_remainInCurIndex(start);
                curIndex++;
                start = curIndex;
                if (token != null) {
                    result.add(token);
                }
                continue;
            }
            curIndex++;
        }

        lastTokenCount = result.size();
        return result.toArray(new String[lastTokenCount]);
    }

    //===============
    public void ignoreRemainCurLine() {
        ignoreRemainCurLine(false);
    }

    public void ignoreRemainCurLine(boolean dontBreakInBracket) {
        if (brackets != null && dontBreakInBracket) {
            ignoreRemainCurLine_impl2();
        } else {
            ignoreRemainCurLine_impl1();
        }
    }

    private void ignoreRemainCurLine_impl1() {
        if (isNewLineBegin()) {
            //本行恰好读完，已进入下一行
            return;
        }
        while (curIndex < end) {
            char c = str.charAt(curIndex);
            if (c == R || c == N) {
                curIndex++;
                rn(c);
                break;
            }
            curIndex++;
        }
    }

    private void ignoreRemainCurLine_impl2() {
        if (isNewLineBegin()) {
            //本行恰好读完，已进入下一行
            return;
        }
        int b = -1;
        while (curIndex < end) {
            if (b != -1) {
                findRightBracket(b);
                b = -1;
                continue;
            }
            char c = str.charAt(curIndex);
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                continue;
            }
            if (c == R || c == N) {
                curIndex++;
                rn(c);
                break;
            }
            curIndex++;
        }
    }

    private String remainStringCurLine_impl() {
        int start = curIndex;
        int b = -1;
        while (true) {
            if (b != -1) {
                findRightBracket(b);
                b = -1;
                continue;
            }
            if (curIndex == end) {
                return str.subSequence(start, end).toString();
            }

            char c = str.charAt(curIndex);
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                continue;
            }
            if (c == R || c == N) {
                int lineEnd = curIndex;
                curIndex++;
                rn(c);
                return str.subSequence(start, lineEnd).toString();
            }
            curIndex++;
        }
    }

    private void findRightBracket(int b) {
        isLastTokenNotComplete = true;
        curIndex++;
        for (; curIndex < end; curIndex++) {
            char c = str.charAt(curIndex);
            int bc = isBracket(c, false);
            if (bc == b + 1) {
                isLastTokenNotComplete = false;
                curIndex++;
                break;
            }
        }
    }

    //==========================
    public String nextLine() {
        return nextLine(keepEmptyLine);
    }

    //达到文件末尾时，返回null
    public String nextLine(boolean keepEmptyLine) {
        return nextLine_impl(keepEmptyLine);
    }

    private String nextLine_impl(boolean keepEmptyLine) {
        if (isEnd()) {
            return _end;
        }
        boolean isEmptyLine = !keepEmptyLine;
        int lineStart = curIndex;
        int b = -1;
        while (true) {
            if (b != -1) {
                findRightBracket(b);
                b = -1;
                continue;
            }
            if (curIndex == end) {
                if (!isEmptyLine) {
                    return str.subSequence(lineStart, end).toString();
                }
                return _end;
            }
            char c = str.charAt(curIndex);
            int bc = isBracket(c, true);
            if (bc != -1) {
                b = bc;
                isEmptyLine = false;
                continue;
            }
            if (c == R || c == N) {
                int lineEnd = curIndex;
                curIndex++;
                rn(c);
                if (!isEmptyLine) {
                    return str.subSequence(lineStart, lineEnd).toString();
                }
                isEmptyLine = !keepEmptyLine;
                lineStart = curIndex;
                continue;
            }
            if (isEmptyLine && !isWhitespace(c)) {
                isEmptyLine = false;
            }
            curIndex++;
        }
    }

    private boolean isWhitespace(char c) {
        return StringUtil.isWhitespace(c, wps, withDefaultWps);
    }

    private int isBracket(char c, boolean isStart) {
        return StringUtil.isBracket(c, brackets, isStart);
    }

    //=================
    public boolean isEnd() {
        return curIndex == end;
    }

    //已达文件末尾时，视为有新的“空行”（但读取会返回null或END）
    public boolean isNewLineBegin() {
        int c = str.charAt(curIndex - 1);
        return c == R || c == N;
    }

    public void mark() {
        mark = curIndex;
    }

    public void reset() {
        curIndex = mark;
    }

    //=====================
    public void setStr(CharSequence str) {
        this.str = str;
        end = str.length();
        mark = 0;
        curIndex = 0;
    }

    public CharSequence getStr() {
        return str;
    }

    public boolean isIncludeBracket() {
        return includeBracket;
    }

    public void setIncludeBracket(boolean includeBracket) {
        this.includeBracket = includeBracket;
    }

    public char[] getBrackets() {
        return brackets;
    }

    public void setBrackets(char[] brackets) {
        this.brackets = brackets;
    }

    public char[] getWps() {
        return wps;
    }

    public void setWps(char[] wps) {
        this.wps = wps;
    }

    public boolean isWithDefaultWps() {
        return withDefaultWps;
    }

    public void setWithDefaultWps(boolean withDefaultWps) {
        this.withDefaultWps = withDefaultWps;
    }

    public boolean isRemoveEmptyInBracket() {
        return removeEmptyInBracket;
    }

    public void setRemoveEmptyInBracket(boolean removeEmptyInBracket) {
        this.removeEmptyInBracket = removeEmptyInBracket;
    }

    public boolean isWithNextLine() {
        return withNextLine;
    }

    public void setWithNextLine(boolean withNextLine) {
        this.withNextLine = withNextLine;
    }

    public boolean isKeepEmptyLine() {
        return keepEmptyLine;
    }

    public void setKeepEmptyLine(boolean keepEmptyLine) {
        this.keepEmptyLine = keepEmptyLine;
    }

    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getLastTokenCount() {
        return lastTokenCount;
    }

    public void setLastTokenCount(int lastTokenCount) {
        //do nothing
    }

    public boolean isLastTokenNotComplete() {
        return isLastTokenNotComplete;
    }

    public void setLastTokenNotComplete(boolean isLastTokenNotComplete) {
        //do nothing
    }

}
