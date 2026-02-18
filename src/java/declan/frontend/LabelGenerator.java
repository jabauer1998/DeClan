package io.github.h20man13.DeClan.common.gen;

public class LabelGenerator implements Generator{
    private int labelNumber;
    private String labelRoot;

    public LabelGenerator(String labelRoot){
        this.labelRoot = labelRoot;
        this.labelNumber = 0;
    }

    @Override
    public String genNext() {
        StringBuilder toRet = new StringBuilder();
        toRet.append(labelRoot);
        toRet.append('_');
        toRet.append(labelNumber);
        labelNumber++;
        return toRet.toString();
    }
}
