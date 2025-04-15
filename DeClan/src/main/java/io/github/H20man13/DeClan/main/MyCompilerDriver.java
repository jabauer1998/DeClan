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

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ast.Library;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.source.ElaborateReaderSource;
import io.github.H20man13.DeClan.common.source.ReaderSource;
import io.github.H20man13.DeClan.common.source.Source;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser.ProgramContext;
import io.github.H20man13.DeClan.main.assembler.AssemblerVisitor;

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
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index + 1];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--library":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index + 1];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--lib":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index + 1];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "--Library":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
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
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index + 1];
                        index += 2;
                        conf.addFlag("library", val4);
                    }
                    break;
                case "-L":
                    if(conf.containsFlag("library")){
                        StringBuilder sb = new StringBuilder();
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        String val4 = args[index + 1];
                        index += 2;
                        sb.append(val4);
                        conf.addFlag("library", sb.toString());
                    } else {
                        String val4 = args[index + 1];
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
                        String curValue = conf.getValueFromFlag("library");
                        conf.removeFlag("library");
                        sb.append(curValue);
                        sb.append("#");
                        index+=2;
                        sb.append(argAtIndex);
                        conf.addFlag("library", sb.toString());
                    } else {
                        index++;
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
        if(!cfg.containsFlag("program") &&  !cfg.containsFlag("library") &&debugEnabled){
            while(true){
                System.out.println("Error no program or library specified");
                System.out.println("Would you like to compile a program or a library?");
                System.out.print("Please specify program or library here[program/library]: ");
                String line = scanner.nextLine();
                line = line.replace("\r", "");
                line = line.replace("\n", "");
                if(line.equals("library")){
                    System.out.print("Please specify the library name here: ");
                    line = scanner.nextLine();
                    line = line.replace("\r", "");
                    line = line.replace("\n", "");
                    cfg.addFlag("library", line);
                    break;
                } else if (line.equals("program")) {
                    System.out.print("Please specify the program name here: ");
                    line = scanner.nextLine();
                    line = line.replace("\r", "");
                    line = line.replace("\n", "");
                    cfg.addFlag("program", line);
                    break;
                } else {
                    System.out.println("Error expected literal text [program/library] here!!!");
                }
            }
        }

        if(debugEnabled){
            while(true){
                System.out.print("Would you like to add any additional libraries to the build?[Y/N]: ");
                String line = scanner.nextLine();
                line = line.replace("\r", "");
                line = line.replace("\n", "");
                if(line.equals("Y")){
                    int libraryCount = 0;
                    StringBuilder libList = new StringBuilder();
                    if(cfg.containsFlag("library")){
                        libList.append(cfg.getValueFromFlag("library"));
                    }
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

        if(debugEnabled && !cfg.containsFlag("output")){
            System.out.println("Error no output file was specified");
            System.out.print("Please specify output file name here: ");
            String line = scanner.nextLine();
            line = line.replace("\r", "");
            line = line.replace("\n", "");
            cfg.addFlag("output", line);
        } else if(!cfg.containsFlag("output")){
            cfg.addFlag("output", "a.out");
        }

        scanner.close();
    }

    private static Program parseDeclanProgram(String programString, Config cfg, ErrorLog errLog) throws Exception{
        File file = new File(programString);
        if(file.exists()){
            System.out.println("Parsing program at path-");
            System.out.println(file.getAbsolutePath());
            FileReader reader = new FileReader(programString);
            ElaborateReaderSource source = new ElaborateReaderSource(programString, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, cfg, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            parser.close();
            return prog;
        } else {
            System.err.println("Error: program file at path-\r\n" + file.getAbsolutePath() + "\r\n not found!!!\r\n");
            throw new Exception("Error: program file at path-\r\n" + file.getAbsolutePath() + "\r\n not found!!!\r\n");
        }
    }

    private static Prog generateProgram(Program prog, Config cfg, ErrorLog errLog){
        MyICodeGenerator iGen = new MyICodeGenerator(cfg, errLog);
        return iGen.generateProgramIr(prog);
    }

    private static Lib generateLibrary(Library library, Config cfg, ErrorLog errLog){
        MyICodeGenerator iGen = new MyICodeGenerator(cfg, errLog);
        return iGen.generateLibraryIr(library);
    }

    private static Library parseLibrary(String libString, Config cfg, ErrorLog errLog) throws Exception{
        File file = new File(libString);
        if(file.exists()){
            System.out.println("Parsing library at path-");
            System.out.println(file.getAbsolutePath());
            FileReader reader = new FileReader(file);
            ElaborateReaderSource source = new ElaborateReaderSource(libString, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, cfg, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Library lib = parser.parseLibrary();
            parser.close();
            return lib;
        } else {
            System.err.println("Error: library file at path-\r\n" +  file.getAbsolutePath() + "\r\n not found!!!\r\n");
            throw new Exception("Error: library file at path-\r\n" +  file.getAbsolutePath() + "\r\n not found!!!\r\n");   
        }
    }

    private static Lib[] listAsArray(List<Lib> libs){
        Lib[] toRet = new Lib[libs.size()];
        int i = 0;
        for(Lib lib: libs){
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
            Program program = parseDeclanProgram(programString, cfg, errLog);

            //We need to compile a Program into an executable
            List<Lib> libs = new LinkedList<Lib>();
            if(cfg.containsFlag("library")){
                String libString = cfg.getValueFromFlag("library");
                String[] libraries = libString.split("#");
                for(String libPath: libraries){
                    Library declanLib = parseLibrary(libPath, null, errLog);
                    Lib icodeLib = generateLibrary(declanLib, null, errLog);
                    libs.add(icodeLib);
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
                        Prog prog = generateProgram(program, cfg, errLog);
                        FileWriter writer = new FileWriter(outputDestination);
                        writer.write(prog.toString());
                        writer.close();
                    } else {
                        if(cfg.containsFlag("std")){
                            String isStd = cfg.getValueFromFlag("std");
                            if(isStd.equals("TRUE")){
                                MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
                                libs.add(stdLib.irIoLibrary());
                                libs.add(stdLib.irIntLibrary());
                                libs.add(stdLib.irRealLibrary());
                                libs.add(stdLib.irConversionsLibrary());
                                libs.add(stdLib.irMathLibrary());
                                libs.add(stdLib.irUtilsLibrary());
                            }
                        }

                        MyIrLinker linker = new MyIrLinker(cfg, errLog);
                        Lib[] libsArray = listAsArray(libs);
                        Prog myProg = generateProgram(program, cfg,  errLog);
                        Prog prog = linker.performLinkage(myProg, libsArray);

                        if(cfg.containsFlag("optimize")){
                            boolean shouldOptimize = cfg.getValueFromFlag("optimize").equals("TRUE");
                            if(shouldOptimize){
                                MyOptimizer optimizer = new MyOptimizer(cfg, prog);
                                optimizer.performCommonSubExpressionElimination();
                                optimizer.performConstantPropogation();
                                optimizer.performDeadCodeElimination();
                                optimizer.performPartialRedundancyElimination();
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
                    MyIrLinker linker = new MyIrLinker(cfg, errLog);
                    Prog myProg = generateProgram(program, cfg, errLog);
                    
                    if(cfg.containsFlag("std")){
                        String isStd = cfg.getValueFromFlag("std");
                        if(isStd.equals("TRUE")){
                            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
                            libs.add(stdLib.irIoLibrary());
                            libs.add(stdLib.irIntLibrary());
                            libs.add(stdLib.irRealLibrary());
                            libs.add(stdLib.irConversionsLibrary());
                            libs.add(stdLib.irMathLibrary());
                            libs.add(stdLib.irUtilsLibrary());
                        }
                    }
                    
                    Lib[] newLibs = listAsArray(libs);
                    Prog prog = linker.performLinkage(myProg, newLibs);

                    MyOptimizer optimizer = new MyOptimizer(cfg, prog);
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
                    
                	for(LogItem item: errLog) {
                    	System.err.println(item);
                    }
                }
            } else {
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    MyIrLinker linker = new MyIrLinker(cfg, errLog);
                    Lib[] libArray = listAsArray(libs);
                    Prog myProg = generateProgram(program, cfg, errLog);
                    Prog prog = linker.performLinkage(myProg, libArray);

                    MyOptimizer optimizer = new MyOptimizer(cfg, prog);
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
            List<Lib> libs = new LinkedList<Lib>();
            String libString = cfg.getValueFromFlag("library");
            boolean emitIr = cfg.getValueFromFlag("ir").equals("TRUE");
            boolean noLink = false;
            if(cfg.containsFlag("nolink")){
                noLink = cfg.getValueFromFlag("nolink").equals("TRUE");
            }
            

            if(emitIr){
                if(cfg.containsFlag("output")){
                    String outputDestination = cfg.getValueFromFlag("output");
                    if(noLink){
                        String library = libString;
                        Library lib = parseLibrary(library, cfg, errLog);
                        	
                        MyICodeGenerator iCodeGenerator = new MyICodeGenerator(cfg, errLog);
                        Lib genedLib = iCodeGenerator.generateLibraryIr(lib);
                        File outputFile = new File(outputDestination);
                        if(outputFile.exists()){
                            outputFile.delete();
                        }
                        outputFile.createNewFile();
                        FileWriter writer = new FileWriter(outputFile);
                        writer.append(genedLib.toString());
                        writer.close();
                    } else {
                    	String[] libraries = libString.split("#");
                        for(String libPath: libraries){
                            Library lib = parseLibrary(libPath, null, errLog);
                            Lib lib2 = generateLibrary(lib, null, errLog);
                            libs.add(lib2);
                        }
                        
                        if(cfg.containsFlag("std")){
                            String isStd = cfg.getValueFromFlag("std");
                            if(isStd.equals("TRUE")){
                                MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
                                libs.add(stdLib.irIoLibrary());
                                libs.add(stdLib.irIntLibrary());
                                libs.add(stdLib.irRealLibrary());
                                libs.add(stdLib.irConversionsLibrary());
                                libs.add(stdLib.irMathLibrary());
                                libs.add(stdLib.irUtilsLibrary());
                            }
                        }
                        
                        MyIrLinker linker = new MyIrLinker(cfg, errLog);
                        Lib startingLibrary = libs.remove(0);
                        Lib[] libsAsArray = listAsArray(libs);
                        Lib library = linker.performLinkage(startingLibrary, libsAsArray);

                        FileWriter writer = new FileWriter(outputDestination);
                        writer.write(library.toString());
                        writer.close();
                    }
                }
            }
        }

        for(LogItem err: errLog){
            System.err.println(err);
        }
    }
}
