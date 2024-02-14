package io.github.H20man13.DeClan.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;

public class MyCompilerDriver {
    private static Config parseConfig(String[] args){
        Config conf = new Config();
        int index = 0;
        while(index < args.length){
            String argAtIndex = args[index];
            switch(argAtIndex){
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
                    conf.addFlag("optimized", "TRUE");
                    index++;
                    break;
                case "-O":
                    conf.addFlag("optimized", "TRUE");
                    index++;
                    break;
                case "--Optimize":
                    conf.addFlag("optimized", "TRUE");
                    index++;
                    break;
                case "--optimize":
                    conf.addFlag("optimized", "TRUE");
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
        if(!cfg.containsFlag("optimized")){
            cfg.addFlag("optimized", "FALSE");
        }

        if(!cfg.containsFlag("output")){
            cfg.addFlag("output", "a.out");
        }
    }
    public static void main(String[] args) throws FileNotFoundException{
        Config cfg = parseConfig(args);
        Source source = new ReaderSource(new FileReader(args[0]));
        ErrorLog errLog = new ErrorLog();
        MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        Program prog = parser.parseProgram();
        
        if(prog != null){
            MyTypeChecker typeChecker = new MyTypeChecker(errLog);
            prog.accept(typeChecker);
        }
        
        IrRegisterGenerator gen = new IrRegisterGenerator();
        if(prog != null){
            MyICodeGenerator codeGen = new MyICodeGenerator(errLog, gen);
            Prog program = codeGen.generateProgramIr(prog);
        }
    }
}
