package cn.study.algorithm.dp;


public class LongestPalindrome {

    /**
     * 字符串的问题一般都转化成二维数组
     * @param s
     * @return
     */
    public String dpMethod(String s){
        int len = s.length();
        if (len < 2){
            return s;
        }

        int maxLen = 0;
        int begin = 0;
        // 初始化
        boolean[][] dp = new boolean[len][len];
        // 单独一个字符都是
        for(int i = 0;i < len;i++){
            dp[i][i] = true;
        }

        char[] charArray = s.toCharArray();
        // 子串长度
        for (int L = 2;L <= len;L++){
            for(int i = 0;i < len;i++){
                int j = i + L -1;
                if (j >= len){
                    break;
                }
                if (charArray[i] != charArray[j]){
                    dp[i][j] = false;
                }else {
                    if (j-i < 3){
                        dp[i][j] = true;
                    }else {
                        dp[i][j] = dp[i+1][j-1];
                    }
                }

                if (dp[i][j] && j-i+1 > maxLen){
                    maxLen = j-i+1;
                    begin = i;
                }
            }
        }


        return s.substring(begin,begin+maxLen);
    }
}
