package io.github.H20man13.DeClan.common.pat;

import java.lang.reflect.InaccessibleObjectException;
import java.util.Objects;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;

public abstract class P {
    private static class IADD extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IADD;
        }
    }

    private static class ISUB extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ISUB;
        }
    }

    private static class GE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof GE;
        }
    }
    private static class GT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof GT;
        }
    }
    private static class LT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof LT;
        }
    }
    private static class LE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof LE;
        }
    }
    private static class INE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof INE;
        }
    }
    private static class IEQ extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IEQ;
        }
    }
    private static class BNE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BNE;
        }
    }
    private static class BEQ extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BEQ;
        }
    }
    private static class ASSIGN extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ASSIGN;
        }
    }
    private static class BNOT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BNOT;
        }
    }
    private static class BAND extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BAND;
        }
    }
    private static class BOR extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BOR;
        }
    }

    private static class IAND extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IAND;
        }
    }

    private static class IOR extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IOR;
        }
    }

    private static class IXOR extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IXOR;
        }
    }

    private static class LSHIFT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof LSHIFT;
        }
    }

    private static class RSHIFT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RSHIFT;
        }
    }

    private static class INOT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof INOT;
        }
    }
    private static class IF extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IF;
        }
    }
    private static class BOOL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BOOL;
        }
    }
    private static class INT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof INT;
        }
    }
    private static class REAL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof REAL;
        }
    }
    private static class STR extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof STR;
        }
    }
    private static class ID extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PAT) {
            	PAT objPattern = (PAT)obj;
            	P[] innerPattern = objPattern.pattern;
            	if(innerPattern.length == 2) {
            		P first = innerPattern[0];
            		if(first instanceof GLOBAL || 
            		   first instanceof RETURN || 
            		   first instanceof PARAM) {
            			return this.equals(innerPattern[1]);
            		}
            	}
            }
            return obj instanceof ID;
        }
    }
    private static class THEN extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof THEN;
        }
    }
    private static class ELSE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ELSE;
        }
    }
    private static class END extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof END;
        }
    }
    private static class LABEL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof LABEL;
        }
    }
    private static class GOTO extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof GOTO;
        }
    }
    private static class RETURN extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RETURN;
        }   
    }
    private static class PROC extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof PROC;
        }
    }
    private static class CALL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof CALL;
        }
    }

    private static class INLINE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof INLINE;
        }
    }

    private static class PARAM extends P{

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PARAM;
        }
    }

    private static class INTERNAL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof INTERNAL;
        }
    }

    private static class EXTERNAL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof EXTERNAL;
        }
    }

    private static class PLACE extends P{

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PLACE;
        }
    }

    private static class PUBLIC extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof PUBLIC;
        }
    }

    private static class PRIVATE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof PRIVATE;
        }
    }

    private static class CONST extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof CONST;
        }
    }

    private static class DEF extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof DEF;
        }
    }

    private static class GLOBAL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof GLOBAL;
        }
    }

    private static class ARGUMENT extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ARGUMENT;
        }
    }

    private static class SECTION extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SECTION;
        }
    }

    private static class BSS extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BSS;
        }
    }

    private static class DATA extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof DATA;
        }
    }

    private static class CODE extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof CODE;
        }
    }

    private static class SYMBOL extends P{
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SYMBOL;
        }
    }

    private static class PAT extends P{
        private P[] pattern;

        private PAT(P... pattern){
            this.pattern = pattern;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PAT){
                PAT other = (PAT)obj;
                if(other.pattern.length == pattern.length){
                    for(int i = 0; i < pattern.length; i++){
                        P otherElem = other.pattern[i];
                        P thisElem = this.pattern[i];
                        if(!thisElem.equals(otherElem)){
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            } else if(obj instanceof ID) {
                if(this.pattern.length == 2) {
                	P first = this.pattern[0];
                	if(first instanceof GLOBAL || first instanceof RETURN || first instanceof PARAM){
                		return this.pattern[1].equals(obj);
                	}
                }
            }
            return false;
        }

        @Override
        public int hashCode(){
            return Objects.hash((Object[])pattern);
        }

        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            builder.append(" PAT");
            builder.append('(');

            for(int i = 0; i < pattern.length; i++){
                builder.append(pattern[i].toString());
                if(i < pattern.length - 1){
                    builder.append(", ");
                }
            }
            builder.append(") ");

            return builder.toString();
        }
    }

    public static IADD IADD(){
        return new IADD();
    }
    public static ISUB ISUB() {
    	return new ISUB();
    }
    public static GE GE(){
        return new GE();
    }
    public static GT GT(){
        return new GT();
    }
    public static LT LT(){
        return new LT();
    }
    public static LE LE(){
        return new LE();
    }
    public static INE INE(){
        return new INE();
    }
    public static IEQ IEQ(){
        return new IEQ();
    }
    public static BNE BNE(){
        return new BNE();
    }
    public static BEQ BEQ(){
        return new BEQ();
    }
    public static ASSIGN ASSIGN(){
        return new ASSIGN();
    }
    public static BNOT BNOT(){
        return new BNOT();
    }
    public static INOT INOT(){
        return new INOT();
    }
    public static BAND BAND(){
        return new BAND();
    }
    public static IAND IAND(){
        return new IAND();
    }
    public static BOR BOR(){
        return new BOR();
    }
    public static IOR IOR(){
        return new IOR();
    }
    public static IXOR IXOR(){
        return new IXOR();
    }
    public static LSHIFT LSHIFT(){
        return new LSHIFT();
    }
    public static RSHIFT RSHIFT(){
        return new RSHIFT();
    }
    public static IF IF(){
        return new IF();
    }
    public static BOOL BOOL(){
        return new BOOL();
    }
    public static INT INT(){
        return new INT();
    }
    public static REAL REAL(){
        return new REAL();
    }
    public static STR STR(){
        return new STR();
    }
    public static ID ID(){
        return new ID();
    }
    public static THEN THEN(){
        return new THEN();
    }
    public static ELSE ELSE(){
        return new ELSE();
    }
    public static END END(){
        return new END();
    }
    public static LABEL LABEL(){
        return new LABEL();
    }
    public static GOTO GOTO(){
        return new GOTO();
    }
    public static RETURN RETURN(){
        return new RETURN();
    }
    public static PROC PROC(){
        return new PROC();
    }
    public static CALL CALL(){
        return new CALL();
    }
    public static INLINE INLINE(){
        return new INLINE();
    }
    public static PAT PAT(P... inputs){
        return new PAT(inputs);
    }
    public static PARAM PARAM(){
        return new PARAM();
    }
    public static INTERNAL INTERNAL(){
        return new INTERNAL();
    }
    public static EXTERNAL EXTERNAL(){
        return new EXTERNAL();
    }
    public static PLACE PLACE(){
        return new PLACE();
    }
    public static CONST CONST(){
        return new CONST();
    }
    public static PUBLIC PUBLIC(){
        return new PUBLIC();
    }
    public static PRIVATE PRIVATE(){
        return new PRIVATE();
    }
    public static DEF DEF(){
        return new DEF();
    }
    public static GLOBAL GLOBAL(){
        return new GLOBAL();
    }
    public static ARGUMENT ARGUMENT(){
        return new ARGUMENT();
    }
    public static BSS BSS(){
        return new BSS();
    }
    public static DATA DATA(){
        return new DATA();
    }
    public static SYMBOL SYMBOL(){
        return new SYMBOL();
    }
    public static CODE CODE(){
        return new CODE();
    }
    public static SECTION SECTION(){
        return new SECTION();
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public String toString() {
    	return this.getClass().getSimpleName();
    }

    @Override
    public int hashCode(){
        return this.toString().hashCode();
    }
}
