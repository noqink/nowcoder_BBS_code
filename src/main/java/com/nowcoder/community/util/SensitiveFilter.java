package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "*";

    // 根节点
    private TrieNode root = new TrieNode();

    // 前缀树过滤
    private class TrieNode{

        // 敏感词结束结尾
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNode.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }
    }

    // 初始化
    @PostConstruct
    public void init(){

        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String sensitiveWord;
            while ((sensitiveWord = reader.readLine()) != null){
                // 添加到前缀树
                this.addSensitiveWord(sensitiveWord);
            }
        } catch (IOException e){
            logger.error("加载敏感词文件失败" + e.getMessage());
        }

    }

    private void addSensitiveWord(String sensitiveWord) {

        TrieNode tempNode = root;
        for (int i = 0; i < sensitiveWord.length(); i++) {

            char c = sensitiveWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                // 初始化
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 改变指针
            tempNode = subNode;

            // 设置敏感词结尾标记
            if (i == sensitiveWord.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }

    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String Filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        // 指针1 指向树的根
        TrieNode tempNode = root;
        // 指针2 指向字符串首位
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)){
                // 若指针1处于根节点,将此符号计入结果,不用过滤,指针2向下走一步
                if (tempNode == root){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头、结尾,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                // 以为begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++ begin;
                tempNode = root;
            } else if (tempNode.isKeywordEnd){
                // 发现敏感词,将begin到position字符串替换掉
                for (int i = 0; i < position - begin + 1; i++) {
                    sb.append(REPLACEMENT);
                }
                begin = ++ position;
                tempNode = root;
            } else {
                position ++;
            }
        }
        // 最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c){
        // c < 0x2E80 || c > 0x9FFF 之间为东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


}
