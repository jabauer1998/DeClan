package io.github.H20man13.DeClan.common.builder.section;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.section.SymSec;

public class SymbolSectionBuilder implements CompletableBuilder<SymSec>, ResetableBuilder {
    private ErrorLog errorLog;
    private List<SymEntry> symbols;
    
    public SymbolSectionBuilder(ErrorLog errLog){
        this.errorLog = errLog;
        resetBuilder();
    }

    public void addSymEntry(int qualityMask, String irPlace, String declanIdent){
        addSymEntry(new SymEntry(qualityMask, irPlace, declanIdent));
    }

    private void addSymEntry(SymEntry entry){
        this.symbols.add(entry);
    }

    @Override
    public void resetBuilder() {
        this.symbols = new LinkedList<SymEntry>();
    }

    @Override
    public SymSec completeBuild() {
        return new SymSec(symbols);
    }
}
