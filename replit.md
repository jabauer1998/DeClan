# DeClan Compiler

## Overview
DeClan (Depauw Compilers Language) is a compiler for a subset of the Oberon programming language, designed for the CSC 426 class at DePauw University. It compiles DeClan source code down to Single Static Assignment (SSA) Intermediate Representation (IR) and supports ARM7TDMIS assembly code generation.

## Features
- Lexer, Parser, and Type Checker for the DeClan language
- SSA-based IR generation
- Optimization passes: Dead Code Elimination, Common Sub-Expression Elimination, Constant Folding
- ARM7TDMIS code generation backend
- IR Interpreter for testing

## Project Architecture
- `src/java/` - Java source files organized by compiler phase:
  - `declan/frontend/` - Lexer, parser, AST, type checker, IR code generator
  - `declan/middleware/` - Optimizer, analysis passes, flow graphs, DAGs
  - `declan/backend/` - ARM code generator, assembler
  - `declan/driver/` - Main compiler driver and config
  - `declan/utils/` - Utilities, symbol tables, error logging
- `src/antlr/` - ANTLR grammar files
- `src/declan/` - DeClan source files (test programs and standard library)
- `src/ir/` - Intermediate representation test files
- `test/java/` - JUnit test files
- `lib/` - Dependencies (ANTLR 4.13.2, JUnit 6.0.3)
- `build/` - Build scripts (Linux shell, Windows PowerShell)
- `tmp/` - Compiled output directory

## Build & Run
- **Language**: Java 19 (GraalVM CE 22.3.1)
- **Build**: `bash build/LinuxBuild.sh build` (generates ANTLR sources from `src/antlr/*.g4`, then compiles all Java)
- **Test**: `bash build/LinuxBuild.sh test` (builds first, then compiles and runs JUnit tests)
- **Run**: `java -cp 'tmp:lib/*' declan.driver.MyCompilerDriver`
- **Clean**: `bash build/LinuxBuild.sh clean`
- **Environment**: Set `DECLIB=src/declan/std/lib` for standard library resolution

## Package Structure
All packages now use the `declan.*` prefix matching the directory layout:
- `declan.frontend.*` - Lexer, parser, AST, type checker, code generators
- `declan.middleware.*` - Optimizer, analysis, DAG, DFST, icode, region, interfere
- `declan.backend.*` - ARM code generator, assembler
- `declan.driver.*` - Main driver and config
- `declan.utils.*` - Exceptions, flow, matcher, position, source, symboltable

## Recent Changes
- 2026-02-18: Updated tools/Updater.ps1 to use direct `java -cp` calls to MyCompilerDriver instead of Maven (`mvn exec:java`)
- 2026-02-18: Added ANTLR code generation step to build scripts (generates from `src/antlr/*.g4` into `declan.backend.assembler`)
- 2026-02-18: Added test command to build scripts using JUnit Platform Console Standalone
- 2026-02-18: Updated test file paths from `test/` to `src/declan/test/declan` and `src/ir/` directories
- 2026-02-18: Fixed Windows backslash path separators in MyStandardLibrary.java and MyStandardLibTest.java for Linux compatibility
- 2026-02-18: Refactored all package paths from `io.github.h20man13.DeClan.*` to `declan.*` to match directory structure
- 2026-02-18: Fixed Java 21+ API compatibility (replaced `getFirst()`, `getLast()`, `reversed()` with Java 19 compatible alternatives)
- 2026-02-18: Initial Replit environment setup
