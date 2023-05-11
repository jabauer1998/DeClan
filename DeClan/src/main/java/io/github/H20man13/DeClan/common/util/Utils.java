package io.github.H20man13.DeClan.common.util;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;

public class Utils {
    public static List<ICode> stripFromListExcept(List<ICode> list, ICode item){
        List<ICode> linkedList = new LinkedList<ICode>();

        for(ICode listItem : list){
            if(listItem.hashCode() != item.hashCode()){
                linkedList.add(listItem);
            }
        }

        return linkedList;
    }

    public static String getPlace(ICode icode){
        if(icode instanceof LetVar){
            return ((LetVar)icode).place;
        } else if(icode instanceof LetUn){
            return ((LetUn)icode).place;
        } else if(icode instanceof LetBin){
            return ((LetBin)icode).place;
        } else if(icode instanceof LetBool){
            return ((LetBool)icode).place;
        } else if(icode instanceof LetReal){
            return ((LetReal)icode).place;
        } else if(icode instanceof LetInt){
            return ((LetInt)icode).place;
        } else if(icode instanceof LetString){
            return ((LetString)icode).place;
        } else {
            return null;
        }
    }
}
