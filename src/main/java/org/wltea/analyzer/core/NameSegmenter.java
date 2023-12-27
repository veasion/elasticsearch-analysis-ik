package org.wltea.analyzer.core;

import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;

import java.util.LinkedList;
import java.util.List;


/**
 * 姓名分词器
 */
class NameSegmenter implements ISegmenter {

    // 子分词器标签
    static final String SEGMENTER_NAME = "Name_SEGMENTER";
    // 待处理的分词hit队列
    private List<Hit> tmpHits;
    private List<Hit> surnameHits;

    NameSegmenter() {
        this.tmpHits = new LinkedList<>();
        this.surnameHits = new LinkedList<>();
    }

    public void analyze(AnalyzeContext context) {
        if (CharacterUtil.CHAR_USELESS != context.getCurrentCharType()) {
            if (Dictionary.getSingleton().isStopWord(context.getSegmentBuff(), context.getCursor(), 1)) {
                this.surnameHits.clear();
            } else if (!this.surnameHits.isEmpty()) {
                Hit[] tmpArray = this.surnameHits.toArray(new Hit[this.tmpHits.size()]);
                for (Hit hit : tmpArray) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, Lexeme.TYPE_NAME);
                    context.addLexeme(newLexeme);
                    if (context.getCursor() - hit.getEnd() >= 2) {
                        this.surnameHits.remove(hit);
                    }
                }
            }
            if (!this.tmpHits.isEmpty()) {
                Hit[] tmpArray = this.tmpHits.toArray(new Hit[this.tmpHits.size()]);
                for (Hit hit : tmpArray) {
                    hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
                    if (hit.isMatch()) {
                        // 输出当前的词
                        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, Lexeme.TYPE_NAME);
                        context.addLexeme(newLexeme);
                        if (!hit.isPrefix()) {
                            // 不是前缀，hit不需要继续匹配，移除
                            this.tmpHits.remove(hit);
                            // 添加至姓
                            this.surnameHits.add(hit);
                        }
                    } else if (hit.isUnmatch()) {
                        // hit不是词，移除
                        this.tmpHits.remove(hit);
                    }
                }
            }
            // 对当前指针位置的字符进行单字匹配
            Hit singleCharHit = Dictionary.getSingleton().matchInSurnameDict(context.getSegmentBuff(), context.getCursor(), 1);
            if (singleCharHit.isMatch()) {
                this.surnameHits.add(singleCharHit);
            } else if (singleCharHit.isPrefix()) {
                this.tmpHits.add(singleCharHit);
            }
        } else {
            // 遇到CHAR_USELESS字符，清空队列
            this.tmpHits.clear();
            this.surnameHits.clear();
        }

        // 判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            // 清空队列
            this.tmpHits.clear();
            this.surnameHits.clear();
        }

        // 判断是否锁定缓冲区
        if (this.tmpHits.isEmpty() && this.surnameHits.isEmpty()) {
            context.unlockBuffer(SEGMENTER_NAME);
        } else {
            context.lockBuffer(SEGMENTER_NAME);
        }
    }

    public void reset() {
        // 清空队列
        this.tmpHits.clear();
        this.surnameHits.clear();
    }

}
