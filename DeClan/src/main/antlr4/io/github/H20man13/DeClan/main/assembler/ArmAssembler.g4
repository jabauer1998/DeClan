grammar ArmAssembler;

program: instructionOrDirective+;

instructionOrDirective: LABEL? (instruction | wordDirective | byteDirective);

instruction : bInstr
		 	| blInstr
			| bxInstr
			| ldmInstr
			| ldrSignedInstr
			| ldrDefInstr
			| mlaInstr
			| mrsInstr
			| msrDefInstr
			| msrPrivInstr
			| mulInstr
			| stmInstr
			| strSignedInstr
			| strDefInstr
			| swiInstr
			| swpInstr
			| addInstr
			| andInstr
			| eorInstr
			| subInstr
			| rsbInstr
			| adcInstr
			| sbcInstr
			| rscInstr
			| tstInstr
			| teqInstr
			| cmpInstr
			| cmnInstr
			| orrInstr
			| movInstr
			| bicInstr
			| mvnInstr
			| stopInstr
			;

wordDirective: DOT_WORD number;
byteDirective: DOT_BYTE number;

bInstr : BRANCH expression;
blInstr : BRANCH_WITH_LINK expression;
bxInstr : BRANCH_WITH_EXCHANGE REG;
ldmInstr : LOAD_MEMORY REG EXP? COMMA rList BXOR?;
ldrSignedInstr : LOAD_SIGNED_REGISTER REG COMMA address;
ldrDefInstr : LOAD_REGISTER REG COMMA address;
mlaInstr : MULTIPLY_AND_ACUMULATE REG COMMA REG COMMA REG COMMA REG;
mrsInstr : MRS_INSTR REG COMMA psr;
msrDefInstr : MSR_INSTR psr COMMA REG;
msrPrivInstr : MSR_INSTR (psrf COMMA (REG | poundExpression));
mulInstr : MULTIPLY REG COMMA REG COMMA REG;
stmInstr : STORE_MEMORY REG EXP? COMMA rList BXOR?;
strSignedInstr : STORE_SIGNED_REGISTER REG COMMA address;
strDefInstr : STORE_REGISTER REG COMMA address;
swiInstr : SOFTWARE_INTERRUPT expression;
swpInstr : SWAP REG COMMA REG COMMA LBRACK REG RBRACK;
addInstr : ADDITION REG COMMA REG COMMA op2;
andInstr : LOGICAL_AND REG COMMA REG COMMA op2;
eorInstr : EXCLUSIVE_OR REG COMMA REG COMMA op2;
subInstr : SUBTRACTION REG COMMA REG COMMA op2;
rsbInstr : REVERSE_SUBTRACTION REG COMMA REG COMMA op2;
adcInstr : ADDITION_WITH_CARRY REG COMMA REG COMMA op2;
sbcInstr : SUBTRACTION_WITH_CARRY REG COMMA REG COMMA op2;
rscInstr : REVERSE_SUBTRACTION_WITH_CARRY REG COMMA REG COMMA op2;
orrInstr : LOGICAL_OR_INSTRUCTION REG COMMA REG COMMA op2;
bicInstr : BIT_CLEAR_INSTRUCTION REG COMMA REG COMMA op2;
tstInstr : TEST_BITS REG COMMA op2;
teqInstr : TEST_EQUALITY REG COMMA op2;
cmpInstr : COMPARE REG COMMA op2;
cmnInstr : COMPARE_NEGATIVE REG COMMA op2;
movInstr : MOVE REG COMMA op2;
mvnInstr : MOVE_NEGATIVE REG COMMA op2;
stopInstr: STOP;

op2 : REG (COMMA shift)?
    | poundExpression
    ;

shift : shiftName REG
      | shiftName poundExpression
      | RPX
      ;

rList : LCURL rValue (COMMA rValue)*  RCURL;
rValue : REG (MINUS REG)?;

/*
 * Below is code for dealing with expressions
 */
poundExpression: HASH expression;
expression : andExpr (LOR expression)?;
andExpr : relational (LAND andExpr)?;
relational : primary ((REQ|RNE|RLT|RGT|RLE|RGE) primary)?;
primary : bitwise ((PLUS|MINUS) primary)?;
bitwise : term ((BOR|BAND|BXOR) bitwise)?;
term: unary ((TIMES|DIV|MOD|LSHIFT|RSHIFT) term)?;
unary: (PLUS|MINUS)? single;
single: number
	  | identifier
	  | realNumber
	  ;
identifier: IDENT
		  | BRANCH
		  | BRANCH_WITH_LINK
		  | BRANCH_WITH_EXCHANGE
		  | LOAD_MEMORY
		  | LOAD_SIGNED_REGISTER
		  | LOAD_REGISTER
		  | MULTIPLY_AND_ACUMULATE
		  | MRS_INSTR
		  | MSR_INSTR
		  | MULTIPLY
		  | STORE_MEMORY
		  | STORE_SIGNED_REGISTER
		  | STORE_REGISTER
		  | SOFTWARE_INTERRUPT
		  | SWAP
		  | ADDITION
		  | LOGICAL_AND
		  | EXCLUSIVE_OR
		  | SUBTRACTION
		  | REVERSE_SUBTRACTION
		  | ADDITION
		  | ADDITION_WITH_CARRY
		  | SUBTRACTION_WITH_CARRY
		  | REVERSE_SUBTRACTION_WITH_CARRY
		  | LOGICAL_OR_INSTRUCTION
		  | BIT_CLEAR_INSTRUCTION
		  | TEST_BITS
		  | TEST_EQUALITY
		  | COMPARE
		  | COMPARE_NEGATIVE
		  | MOVE
		  | MOVE_NEGATIVE
		  | STOP
		  | shiftName
		  | psr
		  | psrf
		  | REG
		  | RPX
		  ;
number: NUMBER;
realNumber: NUMBER PERIOD NUMBER;

/*
 * Below is the code for dealing with addresses
 */

address : expression
	    | preIndexedAddressing
	    | postIndexedAddressing
	    ;

preIndexedAddressing: LBRACK REG RBRACK
				  	| LBRACK REG COMMA poundExpression RBRACK EXP?
				  	| LBRACK REG COMMA (PLUS | MINUS)? REG (COMMA shift)? RBRACK EXP?
				  	;

postIndexedAddressing: LBRACK REG RBRACK COMMA poundExpression
					 | LBRACK REG RBRACK COMMA (PLUS | MINUS)? REG (COMMA shift)?
					 ;

shiftName: LSL
	     | LSR
	     | ASR
	     | ROR
	     ;

psr: CPSR
   | CPSR_ALL
   | SPSR
   | SPSR_ALL
   ;

psrf: CPSR_FLG
    | SPSR_FLG
    ;

/*
 * Below are the shift name variables
 */

ASL : A S L;
LSL : L S L;
LSR : L S R;
ASR : A S R;
ROR : R O R;

RPX : R P X;

DOT_WORD: PERIOD WORD;
DOT_BYTE: PERIOD BYTE;

/*
 * Below is code for condition codes
 */

BRANCH: B CONDITION_CODE?;
BRANCH_WITH_LINK: BL CONDITION_CODE?;
BRANCH_WITH_EXCHANGE: BX CONDITION_CODE?;
LOAD_MEMORY: LDM CONDITION_CODE? ADDRESSING_MODE;
LOAD_REGISTER: LDR CONDITION_CODE? B? T?;
LOAD_SIGNED_REGISTER: LDR CONDITION_CODE? TRANSFER_TYPE;
MULTIPLY_AND_ACUMULATE: MLA CONDITION_CODE? S?;
MRS_INSTR: MRS CONDITION_CODE?;
MSR_INSTR: MSR CONDITION_CODE?; 
MULTIPLY: MUL CONDITION_CODE? S?;
STORE_MEMORY: STM CONDITION_CODE? ADDRESSING_MODE?;
STORE_REGISTER: STR CONDITION_CODE? B? T?;
STORE_SIGNED_REGISTER: STR CONDITION_CODE? TRANSFER_TYPE;
SOFTWARE_INTERRUPT: SWI CONDITION_CODE?;
SWAP: SWP CONDITION_CODE? B?;
ADDITION: ADD CONDITION_CODE? S?;
LOGICAL_AND: AND CONDITION_CODE? S?;
EXCLUSIVE_OR: EOR CONDITION_CODE? S?;
SUBTRACTION: SUB CONDITION_CODE? S?;
REVERSE_SUBTRACTION: RSB CONDITION_CODE? S?;
ADDITION_WITH_CARRY: ADC CONDITION_CODE? S?;
SUBTRACTION_WITH_CARRY: SBC CONDITION_CODE? S?;
REVERSE_SUBTRACTION_WITH_CARRY: RSC CONDITION_CODE? S?;
LOGICAL_OR_INSTRUCTION: ORR CONDITION_CODE? S?;
BIT_CLEAR_INSTRUCTION: BIC CONDITION_CODE? S?;
TEST_BITS: TST CONDITION_CODE?;
TEST_EQUALITY: TEQ CONDITION_CODE?;
COMPARE: CMP CONDITION_CODE?;
COMPARE_NEGATIVE: CMN CONDITION_CODE?;
MOVE: MOV CONDITION_CODE? S?;
MOVE_NEGATIVE: MVN CONDITION_CODE? S?;
STOP: STP;

REG : R DIGIT DIGIT?;
LABEL: IDENT WS* COLON;
IDENT: LETTER LETTER_OR_UNDERSCORE_OR_NUMBER*;


/*
 * Below is the code for dealing with REGs
 */
NUMBER : DIGIT+;

CPSR: C P S R;
CPSR_ALL: C P S R '_' A L L;
CPSR_FLG: C P S R '_' F L G;
SPSR: S P S R;
SPSR_ALL: S P S R '_' A L L;
SPSR_FLG: S P S R '_' F L G;	

EXP : '!';

WS : [ \t\r\n]+ -> skip;

//And here are some operators

COMMA : ',';
LCURL : '{';
RCURL : '}';
LBRACK: '[';
RBRACK: ']';
REQ : '==';
RNE : '!=';
RLE : '<=';
RLT : '<';
RGE : '>=';
RGT : '>';
TIMES : '*';
MINUS : '-';
PLUS : '+';
MOD : '%';
DIV : '/';
LSHIFT : '<<';
RSHIFT : '>>';
BAND : '&';
BOR : '|';
BXOR : '^';
LAND : '&&';
LOR : '||';
HASH : '#';
COLON: ':';
PERIOD: [.];

/*
 * The following are used for ldm and store memory instructions 
 */

 /*
 * Below is definitions of all of the tokens to be used
 * B is also declared bit it is declared later at the bottom
 */

fragment STP: S T P;
fragment ADC : A D C;
fragment ADD : A D D;
fragment AND : A N D;
fragment BIC : B I C;
fragment BL  : B L;
fragment BX  : B X;
fragment CMP : C M P;
fragment CMN : C M N;
fragment EOR : E O R;
fragment LDC : L D C;
fragment LDM : L D M;
fragment LDR : L D R;
fragment MCR : M C R;
fragment MLA : M L A;
fragment MOV : M O V;
fragment MRC : M R C;
fragment MRS : M R S;
fragment MSR : M S R;
fragment MUL : M U L;
fragment MVN : M V N;
fragment ORR : O R R;
fragment RSB : R S B;
fragment RSC : R S C;
fragment SBC : S B C;
fragment STC : S T C;
fragment STM : S T M;
fragment STR : S T R;
fragment SUB : S U B;
fragment SWI : S W I;
fragment SWP : S W P;
fragment TEQ : T E Q;  
fragment TST : T S T;
fragment BYTE: B Y T E;
fragment WORD: W O R D;

fragment SB: S B;
fragment SH: S H;

fragment FD: F D;
fragment ED: E D;
fragment FA: F A;
fragment EA: E A;
fragment IA: I A;
fragment IB: I B;
fragment DA: D A;
fragment DB: D B;

fragment TRANSFER_TYPE: H
			 		  | SB
			 		  | SH
			 		  ;

fragment ADDRESSING_MODE: FD
			   			| ED
			   			| FA
			   			| EA
			   			| IA
			   			| IB
			   			| DA
			   			| DB
			   			;

fragment CONDITION_CODE: EQ 
			  		   | NE 
			  		   | CS 
			  		   | CC 
			  		   | MI 
					   | PL 
					   | VS 
					   | VC 
					   | HI 
					   | LS 
					   | GE 
					   | LT 
					   | GT 
					   | LE 
					   | AL
					   ;

fragment EQ : E Q;
fragment NE : N E;
fragment CS : C S;
fragment CC : C C;
fragment MI : M I;
fragment PL : P L;
fragment VS : V S;
fragment VC : V C;
fragment HI : H I;
fragment LS : L S;
fragment GE : G E;
fragment LT : L T;
fragment GT : G T;
fragment LE : L E;
fragment AL : A L;

fragment LETTER_OR_UNDERSCORE_OR_NUMBER: LETTER | '_' | DIGIT;
fragment LETTER: (A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z);
fragment DIGIT : [0-9];

fragment A : ('A'|'a');
fragment B : ('B'|'b');
fragment C : ('C'|'c');
fragment D : ('D'|'d');
fragment E : ('E'|'e');
fragment F : ('F'|'f');
fragment G : ('G'|'g');
fragment H : ('H'|'h');
fragment I : ('I'|'i');
fragment J : ('J'|'j');
fragment K : ('K'|'k');
fragment L : ('L'|'l');
fragment M : ('M'|'m');
fragment N : ('N'|'n');
fragment O : ('O'|'o');
fragment P : ('P'|'p');
fragment Q : ('Q'|'q');
fragment R : ('R'|'r');
fragment S : ('S'|'s');
fragment T : ('T'|'t');
fragment U : ('U'|'u');
fragment V : ('V'|'v');
fragment W : ('W'|'w');
fragment X : ('X'|'x');
fragment Y : ('Y'|'y');
fragment Z : ('Z'|'z');






