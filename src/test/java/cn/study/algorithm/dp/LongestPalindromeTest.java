package cn.study.algorithm.dp;

import org.junit.jupiter.api.Test;

public class LongestPalindromeTest {

    @Test
    public void dpTest(){
        LongestPalindrome solution = new LongestPalindrome();
        System.out.println(solution.dpMethod("cbbd"));
    }
}
