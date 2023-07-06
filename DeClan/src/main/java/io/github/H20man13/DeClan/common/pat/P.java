package io.github.H20man13.DeClan.common.pat;

import java.lang.reflect.InaccessibleObjectException;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.Exp;

public abstract class P {
    private static class IADD extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IADD){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "IADD";
        }
    }
    private static class ISUB extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ISUB){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "ISUB";
        }
        
    }
    private static class IMUL extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IMUL){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "IMUL";
        }
        
    }
    private static class IDIV extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IDIV){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "IDIV";
        }
    }
    private static class IMOD extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IMOD){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "IMOD";
        }
    }
    public static class GE extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof GE){
                return true;
            } else {
                return false;
            }
        }
        @Override
        public String toString(){
            return "GE";
        }
    }
    public static class GT extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof GT){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "GT";
        }
    }
    public static class LT extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LT){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "LT";
        }
    }
    public static class LE extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LE){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "LE";
        }
    }
    public static class NE extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof NE){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "NE";
        }
    }
    public static class EQ extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof EQ){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "EQ";
        }
    }
    public static class ASSIGN extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ASSIGN){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "ASSIGN";
        }  
    }
    public static class INEG extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof INEG){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "INEG";
        }
    }
    public static class BNOT extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BNOT){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "BNOT";
        }
    }
    public static class BAND extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BAND){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "BAND";
        }
    }
    public static class BOR extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BOR){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "BOR";
        }
    }
    public static class IF extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IF){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "IF";
        }
        
    }
    public static class BOOL extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BOOL){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "BOOL";
        }
    }
    public static class INT extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof INT){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "INT";
        }
        
    }
    public static class REAL extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof REAL){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "REAL";
        }
        
    }
    public static class STR extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof STR){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "STR";
        }
    }
    public static class ID extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ID){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "ID";
        } 
    }
    public static class THEN extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof THEN){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "THEN";
        }
        
    }
    public static class ELSE extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ELSE){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "ELSE";
        }  
    }
    public static class END extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof END){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "END";
        }
    }
    public static class LABEL extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LABEL){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "LABEL";
        } 
    }
    public static class GOTO extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof GOTO){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "GOTO";
        }
    }
    public static class RETURN extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof RETURN){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "RETURN";
        }
        
    }
    public static class PROC extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PROC){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "PROC";
        }
        
    }
    public static class CALL extends P{
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CALL){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString(){
            return "CALL";
        }
    }

    public static class PAT extends P{
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
            } else {
                return false;
            }
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
    public static ISUB ISUB(){
        return new ISUB();
    }
    public static IMUL IMUL(){
        return new IMUL();
    }
    public static IDIV IDIV(){
        return new IDIV();
    }
    public static IMOD IMOD(){
        return new IMOD();
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
    public static NE NE(){
        return new NE();
    }
    public static EQ EQ(){
        return new EQ();
    }
    public static ASSIGN ASSIGN(){
        return new ASSIGN();
    }
    public static INEG INEG(){
        return new INEG();
    }
    public static BNOT BNOT(){
        return new BNOT();
    }
    public static BAND BAND(){
        return new BAND();
    }
    public static BOR BOR(){
        return new BOR();
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
    public static PAT PAT(P... inputs){
        return new PAT(inputs);
    }

    public abstract boolean equals(Object obj);

    public abstract String toString();
}
