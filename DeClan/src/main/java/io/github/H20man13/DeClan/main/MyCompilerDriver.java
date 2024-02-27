package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.ElaborateReaderSource;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser;
import io.github.H20man13.DeClan.main.assembler.AssemblerVisitor;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ProgramContext;

public class MyCompilerDriver {
    private static Config parseConfig(String[] args){
        Config conf = new Config();
        int index = 0;
        while(index < args.length){
            String argAtIndex = args[index];
            switch(argAtIndex){
                case "-d":
                    conf.addFlag("debug", "TRUE");
                    index++;
                    break;
                case "-D":
                    conf.addFlag("debug", "TRUE");
                    index++;
                    break;
                case "--debug":
                    conf.addFlag("debug", "TRUE");
                    index++;
                    break;
                case "--Debug":
                    conf.addFlag("debug", "TRUE");
                    index++;
                    break;
                case "-f":
                    String value = args[index + 1];
                    index += 2;
                    conf.addFlag("output", value);
                    break;
                case "--file":
                    String val = args[index + 1];
                    index += 2;
                    conf.addFlag("output", val);
                    break;
                case "-F":
                    String val2 = args[index + 1];
                    index += 2;
                    conf.addFlag("output", val2);
                    break;
                case "--File":
                    String val3 = args[index + 1];
                    index += 2;
                    conf.addFlag("output", val3);
                    break; 
                case "-A":
                    conf.addFlag("assemble", "TRUE");
                    index++;
                    break;
                case "--assemble":
                    conf.addFlag("assemble", "TRUE");
                    index++;
                    break;
                case "--Assemble":
                    conf.addFlag("assemble", "TRUE");
                    index++;
                    break;
                case "-a": 
                    conf.addFlag("assemble", "TRUE");
                    index++;
                    break;
                case "-e":
                    conf.addFlag("ir", "TRUE");
                    index++;
                    break;
                case "-E":
                    conf.addFlag("ir", "TRUE");
                    index++;
                    break;
                case "--emit":
                    conf.addFlag("ir", "TRUE");
                    index++;
                    break;
                case "--Emit":
                    conf.addFlag("ir", "TRUE");
                    index++;
                    break;
                case "-P":
                    String val5 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val5);
                    break;
                case "--Program":
                    String val6 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val6);
                    break;
                case "--Prog":
                    String val7 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val7);
                    break;
                case "-p":
                    String val8 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val8);
                    break;
                case "--program":
                    String val9 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val9);
                    break;
                case "--prog":
                    String val10 = args[index + 1];
                    index += 2;
                    conf.addFlag("program", val10);
                    break;
                case "-l":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--library":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--lib":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--Library":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--Lib":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "-L":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "-o":
                    conf.addFlag("optimize", "TRUE");
                    index++;
                    break;
                case "-O":
                    conf.addFlag("optimize", "TRUE");
                    index++;
                    break;
                case "--Optimize":
                    conf.addFlag("optimize", "TRUE");
                    index++;
                    break;
                case "--optimize":
                    conf.addFlag("optimize", "TRUE");
                    index++;
                    break;
                case "-n":
                    conf.addFlag("nolink", "TRUE");
                    index++;
                    break;
                case "-N":
                    conf.addFlag("nolink", "TRUE");
                    index++;
                    break;
                case "--Nolink":
                    conf.addFlag("nolink", "TRUE");
                    index++;
                    break;
                case "--nolink":
                    conf.addFlag("nolink", "TRUE");
                    index++;
                    break;
                case "-std":
                    conf.addFlag("std", "TRUE");
                    index++;
                    break;
                case "--Std":
                    conf.addFlag("std", "TRUE");
                    index++;
                    break;
                case "--Standard":
                    conf.addFlag("std", "TRUE");
                    index++;
                    break;
                case "--standard":
                    conf.addFlag("std", "TRUE");
                    index++;
                    break;
                default:
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("input");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        index++;
                        sb.append(argAtIndex);
                        conf.addFlag("library", sb.toString());
                    } else {
                        index ++;
                        conf.addFlag("library", argAtIndex);
                    }
                    break;
            }
        }

        setDefaults(conf);

        return conf;
    }

    private static void setDefaults(Config cfg){
        Scanner scanner = new Scanner(System.in);
        if(!cfg.containsFlag("optimize")){
            cfg.addFlag("optimize", "FALSE");
        }

        if(!cfg.containsFlag("nolink")){
            cfg.addFlag("nolink", "FALSE");
        }

        if(!cfg.containsFlag("output")){
            cfg.addFlag("output", "a.out");
        }

        if(!cfg.containsFlag("std")){
            cfg.addFlag("std", "FALSE");
        }

        if(!cfg.containsFlag("assemble")){
            cfg.addFlag("assemble", "FALSE");
        }

        if(!cfg.containsFlag("ir")){
            cfg.addFlag("ir", "FALSE");
        }

        if(!cfg.containsFlag("debug")){
            cfg.addFlag("debug", "FALSE");
        }

        boolean debugEnabled = cfg.getValueFromFlag("debug").equals("TRUE");
        if(!cfg.containsFlag("program") && debugEnabled){
            System.out.println("Error no program specified");
            System.out.print("Please place path here: ");
            String line = scanner.nextLine();
            line = line.replace("\r", "");
            line = line.replace("\n", "");
            cfg.addFlag("program", line);
        }

        if(!cfg.containsFlag("library") && debugEnabled){
            System.out.println("No libraries were specified...");
            while(true){
                System.out.print("Would you like to add any libraries to the build?[Y/N]: ");
                String line = scanner.nextLine();
                line = line.replace("\r", "");
                line = line.replace("\n", "");
                if(line.equals("Y")){
                    int libraryCount = 0;
                    StringBuilder libList = new StringBuilder();
                    outer: while(true){
                        System.out.println("Enter path of library number " + libraryCount + "-");
                        System.out.print("here: ");
                        String libLine = scanner.nextLine();
                        libLine = libLine.replace("\r", "");
                        libLine = libLine.replace("\n", "");
                        if(libList.toString().equals("")){
                            libList.append(libLine);
                        } else {
                            libList.append('#');
                            libList.append(libLine);
                        }

                        while(true){
                            System.out.print("Would you like to add another library?[Y/N]: ");
                            String exitConfirm = scanner.nextLine();
                            exitConfirm = exitConfirm.replace("\r", "");
                            exitConfirm = exitConfirm.replace("\n", "");

                            if(exitConfirm.equals("N")){
                                break outer;
                            } else if(exitConfirm.equals("Y")){
                                break;
                            }
                        }
                    }
                    cfg.addFlag("library", libList.toString());
                    break;
                } else if(line.equals("N")){
                    break;
                }
            }
        }

        scanner.close();
    }

    private static Program parseDeclanProgram(String programString, ErrorLog errLog) throws Exception{
        File file = new File(programString);
        if(file.exists()){
            FileReader reader = new FileReader(programString);
            ElaborateReaderSource source = new ElaborateReaderSource(programString, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            parser.close();
            return prog;
        } else {
            throw new Exception("Error: program file at path-\r\n" + file.getAbsolutePath() + "\r\n not found!!!\r\n");
        }
    }

    private static Library parseLibrary(String libString, ErrorLog errLog) throws Exception{
        File file = new File(libString);
        if(file.exists()){
            FileReader reader = new FileReader(file);
            ElaborateReaderSource source = new ElaborateReaderSource(libString, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Library lib = parser.parseLibrary();
            parser.close();
            return lib;
        } else {
            throw new Exception("Error: library file at path-\r\n" +  file.getAbsolutePath() + "\r\n not found!!!\r\n");   
        }
    }

    private static Library[] listAsArray(List<Library> libs){
        Library[] toRet = new Library[libs.size()];
        int i = 0;
        for(Library lib: libs){
            toRet[i] = lib;
            i++;
        }
        return toRet;
    }
    public static void main(String[] args) throws Exception{
        Config cfg = parseConfig(args);
        
        ErrorLog errLog = new ErrorLog();
        if(cfg.containsFlag("program")){
            String programString = cfg.getValueFromFlag("program");
            Program program = parseDeclanProgram(programString, errLog);

            //We need to compile a Program into an executable
            List<Library> libs = new LinkedList<Library>();
            if(cfg.containsFlag("library")){
                String libString = cfg.getValueFromFlag("library");
                String[] libraries = libString.split("#");
                for(String libPath: libraries){
                    Library lib = parseLibrary(libPath, errLog);
                    libs.add(lib);
                }
            }

            if(cfg.containsFlag("std")){
                String isStd = cfg.getValueFromFlag("std");
                if(isStd.equals("TRUE")){
                    MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
                    libs.add(stdLib.ioLibrary());
                    libs.add(stdLib.intLibrary());
                    libs.add(stdLib.realLibrary());
                    libs.add(stdLib.conversionsLibrary());
                    libs.add(stdLib.mathLibrary());
                    libs.add(stdLib.utilsLibrary());
                }
            }

            boolean emitIr = cfg.getValueFromFlag("ir").equals("TRUE");
            boolean emitAssembly = cfg.getValueFromFlag("assemble").equals("TRUE");
            if(emitIr){
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    boolean noLink = false;
                    if(cfg.containsFlag("nolink")){
                        noLink = cfg.getValueFromFlag("nolink").equals("TRUE");
                    }

                    if(noLink){
                        MyICodeGenerator iCodeGenerator = new MyICodeGenerator(errLog);
                        Prog prog = iCodeGenerator.generateProgramIr(program);

                        FileWriter writer = new FileWriter(outputDestination);
                        writer.write(prog.toString());
                        writer.close();
                    } else {
                        MyIrLinker linker = new MyIrLinker(errLog);
                        Library[] libsArray = listAsArray(libs);
                        Prog prog = linker.performLinkage(program, libsArray);

                        if(cfg.containsFlag("optimize")){
                            boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                            if(shouldOptimize){
                                MyOptimizer optimizer = new MyOptimizer(prog);
                                optimizer.performCommonSubExpressionElimination();
                                optimizer.performConstantPropogation();
                                optimizer.performDeadCodeElimination();
                                prog = optimizer.getICode();
                            }
                        }

                        FileWriter writer = new FileWriter(outputDestination);
                        writer.write(prog.toString());
                        writer.close();
                    }
                }
            } else if(emitAssembly){
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(errLog);
                    
                    Library[] newLibs = listAsArray(libs);
                    Prog prog = linker.performLinkage(program, newLibs);

                    MyOptimizer optimizer = new MyOptimizer(prog);
                    if(cfg.containsFlag("optimize")){
                        boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                        if(shouldOptimize){
                            optimizer.performCommonSubExpressionElimination();
                            optimizer.performConstantPropogation();
                            optimizer.performDeadCodeElimination();
                            prog = optimizer.getICode();
                        } else {
                            optimizer.runLiveVariableAnalysis();
                        }
                    }

                    MyCodeGenerator cGen = new MyCodeGenerator(outputDestination, optimizer.getLiveVariableAnalysis(), prog, errLog);
                    cGen.codeGen();
                }
            } else {
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(errLog);
                    Library[] libArray = listAsArray(libs);
                    Prog prog = linker.performLinkage(program, libArray);

                    MyOptimizer optimizer = new MyOptimizer(prog);
                    if(cfg.containsFlag("optimize")){
                        boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                        if(shouldOptimize){
                            optimizer.performCommonSubExpressionElimination();
                            optimizer.performConstantPropogation();
                            optimizer.performDeadCodeElimination();
                            prog = optimizer.getICode();
                        } else {
                            optimizer.runLiveVariableAnalysis();
                        }
                    }

                    String tempOutput = outputDestination.replace(".bin", ".a.temp");
                    MyCodeGenerator cGen = new MyCodeGenerator(tempOutput, optimizer.getLiveVariableAnalysis(), prog, errLog);
                    cGen.codeGen();

                    FileReader tempReader = new FileReader(tempOutput);
                    ANTLRInputStream inputStream = new ANTLRInputStream(tempReader);
                    ArmAssemblerLexer lexer = new ArmAssemblerLexer(inputStream);
                    CommonTokenStream stream = new CommonTokenStream(lexer);
                    ArmAssemblerParser parser = new ArmAssemblerParser(stream);
                    ProgramContext armProgram = parser.program();
                    tempReader.close();

                    int numSyntaxErrors = parser.getNumberOfSyntaxErrors();
                    if(numSyntaxErrors > 0){
                        System.err.println("Found " + numSyntaxErrors + " syntax errors");
                    } else {
                        AssemblerVisitor assembler = new AssemblerVisitor();
                        List<Integer> assembledCode = assembler.assembleCode(armProgram);

                        FileWriter binaryWriter = new FileWriter(outputDestination);
                        for(Integer binaryLine : assembledCode){
                            String lineText = Utils.to32BitBinary(binaryLine);
                            binaryWriter.append(lineText);
                            binaryWriter.append("\r\n");
                        }

                        binaryWriter.close();
                    }

                    Utils.deleteFile(tempOutput);
                }
            }
        } else if(cfg.containsFlag("library")){
            //We need to compile a Program into an executable
            List<Library> libs = new LinkedList<Library>();
            String libString = cfg.getValueFromFlag("library");
            String[] libraries = libString.split("#");
            for(String libPath: libraries){
                Library lib = parseLibrary(libPath, errLog);
                libs.add(lib);
            }

            if(cfg.containsFlag("std")){
                String isStd = cfg.getValueFromFlag("std");
                if(isStd.equals("TRUE")){
                    MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
                    libs.add(stdLib.ioLibrary());
                    libs.add(stdLib.intLibrary());
                    libs.add(stdLib.realLibrary());
                    libs.add(stdLib.conversionsLibrary());
                    libs.add(stdLib.mathLibrary());
                    libs.add(stdLib.utilsLibrary());
                }
            }

            boolean emitIr = cfg.getValueFromFlag("ir").equals("TRUE");
            boolean emitAssembly = cfg.getValueFromFlag("assemble").equals("TRUE");

            if(emitIr){
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(errLog);
                    Library startingLibrary = libs.remove(0);
                    Library[] libsAsArray = listAsArray(libs);
                    Lib library = linker.performLinkage(startingLibrary, libsAsArray);

                    if(cfg.containsFlag("optimize")){
                        boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                        /* 
                        if(shouldOptimize){
                            MyOptimizer optimizer = new MyOptimizer(library);
                            optimizer.eliminateCommonSubExpressions();
                            optimizer.runDataFlowAnalysis();
                            optimizer.performConstantPropogation();
                            optimizer.performDeadCodeElimination();
                            library = optimizer.getICode();
                        }
                        */
                    }

                    FileWriter writer = new FileWriter(outputDestination);
                    writer.write(library.toString());
                    writer.close();
                }
            } else if(emitAssembly){
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(errLog);
                    
                    Library startingLibrary = libs.remove(0);
                    Library[] libsAsArray = listAsArray(libs);
                    Lib lib = linker.performLinkage(startingLibrary, libsAsArray);

                    /*
                    MyOptimizer optimizer = new MyOptimizer(prog);
                    if(cfg.containsFlag("optimize")){
                        boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                        if(shouldOptimize){
                            optimizer.eliminateCommonSubExpressions();
                            optimizer.runDataFlowAnalysis();
                            optimizer.performConstantPropogation();
                            optimizer.performDeadCodeElimination();
                            prog = optimizer.getICode();
                        } else {
                            optimizer.runDataFlowAnalysis();
                        }
                    }
                    */

                    //FileWriter outputWriter = new FileWriter(outputDestination);
                    //MyCodeGenerator cGen = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), prog, iGen, errLog);
                    //cGen.codeGen(outputWriter);
                }
            } else {
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(errLog);
                    
                    Library startingLibrary = libs.remove(0);
                    Library[] libsAsArray = listAsArray(libs);
                    Lib library = linker.performLinkage(startingLibrary, libsAsArray);

                    /*
                    MyOptimizer optimizer = new MyOptimizer(prog);
                    if(cfg.containsFlag("optimize")){
                        boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                        if(shouldOptimize){
                            optimizer.eliminateCommonSubExpressions();
                            optimizer.runDataFlowAnalysis();
                            optimizer.performConstantPropogation();
                            optimizer.performDeadCodeElimination();
                            prog = optimizer.getICode();
                        } else {
                            optimizer.runDataFlowAnalysis();
                        }
                    }

                    */

                    String tempOutput = outputDestination.replace(".bin", ".a.temp");
                    //FileWriter outputWriter = new FileWriter(tempOutput);
                    //MyCodeGenerator cGen = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), prog, iGen, errLog);
                    //cGen.codeGen(outputWriter);
                    //outputWriter.close();

                    /* 
                    ANTLRInputStream inputStream = new ANTLRInputStream(tempOutput);
                    ArmAssemblerLexer lexer = new ArmAssemblerLexer(inputStream);
                    CommonTokenStream stream = new CommonTokenStream(lexer);
                    ArmAssemblerParser parser = new ArmAssemblerParser(stream);
                    ProgramContext armProgram = parser.program();
                    AssemblerVisitor assembler = new AssemblerVisitor();
                    List<Integer> assembledCode = assembler.assembleCode(armProgram);

                    FileWriter binaryWriter = new FileWriter(outputDestination);
                    for(Integer binaryLine : assembledCode){
                        String lineText = Utils.to32BitBinary(binaryLine);
                        binaryWriter.append(lineText);
                        binaryWriter.append("\r\n");
                    }

                    binaryWriter.close();
                    */
                }
            }
        }

        for(LogItem err: errLog){
            System.err.println(err);
        }
    }
}
