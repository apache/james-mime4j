From vm-dev-bounces@lists.squeakfoundation.org Tue Jul 17 22:24:56 2012
Return-path: <vm-dev-bounces@lists.squeakfoundation.org>
Envelope-to: mail-archiver@skryb.info
Delivery-date: Tue, 17 Jul 2012 22:24:56 +0000
Received: from box2.squeakfoundation.org ([85.10.195.197])
	by konrad.skryb.info with smtp (Exim 4.80)
	(envelope-from <vm-dev-bounces@lists.squeakfoundation.org>)
	id 1SrGCO-0003oQ-0H
	for mail-archiver@skryb.info; Tue, 17 Jul 2012 22:24:56 +0000
Received: (qmail 8386 invoked from network); 17 Jul 2012 22:24:59 +0000
Received: from unknown (HELO box2.squeakfoundation.org) (127.0.0.1)
  by localhost with SMTP; 17 Jul 2012 22:24:59 +0000
Delivered-To: list-vm-dev@lists.squeakfoundation.org
Received: (qmail 8350 invoked from network); 17 Jul 2012 22:24:57 +0000
Received: from unknown (127.0.0.1)
	by localhost with SMTP; 17 Jul 2012 22:24:57 +0000
Date: Tue, 17 Jul 2012 22:23:35.882 0000
From: commits@source.squeak.org
To: vm-dev@lists.squeakfoundation.org
Subject: [Vm-dev] VM Maker: VMMaker.oscog-eem.186.mcz
X-BeenThere: vm-dev@lists.squeakfoundation.org
X-Mailman-Version: 2.1.5
Precedence: list
Reply-To: vm-dev@lists.squeakfoundation.org
List-Id: Squeak Virtual Machine Development Discussion
	<vm-dev.lists.squeakfoundation.org>
List-Unsubscribe: <http://lists.squeakfoundation.org/mailman/listinfo/vm-dev>, 
	<mailto:vm-dev-request@lists.squeakfoundation.org?subject=unsubscribe>
List-Archive: <http://lists.squeakfoundation.org/pipermail/vm-dev>
List-Post: <mailto:vm-dev@lists.squeakfoundation.org>
List-Help: <mailto:vm-dev-request@lists.squeakfoundation.org?subject=help>
List-Subscribe: <http://lists.squeakfoundation.org/mailman/listinfo/vm-dev>,
	<mailto:vm-dev-request@lists.squeakfoundation.org?subject=subscribe>
Sender: vm-dev-bounces@lists.squeakfoundation.org
Errors-To: vm-dev-bounces@lists.squeakfoundation.org
Status: RO
Content-Length: 20568
Lines: 454

 
Eliot Miranda uploaded a new version of VMMaker to project VM Maker:
http://source.squeak.org/VMMaker/VMMaker.oscog-eem.186.mcz

==================== Summary ====================

Name: VMMaker.oscog-eem.186
Author: eem
Time: 17 July 2012, 3:22:27.571 pm
UUID: 39e5f171-8ca8-4a8b-9430-bd393687a156
Ancestors: VMMaker.oscog-lw.185

Replace CogAbstractInstruction's NumberOfRegisters and
MachineCodeSize class vars with accessors.  Provide commented
subclass responsibilities for these and genSaveRegisters.
Eliminate IA32-specifics from CogARMCompiler>>dispatchConcretize.
Add a rump CogARMCompiler>>genSaveRegisters for Lars to implement.

=============== Diff against VMMaker.oscog-lw.185 ===============

Item was changed:
  ----- Method: CogARMCompiler class>>initialize (in category 'class initialization') -----
  initialize
  	super initialize.
  	
  	"Initialize various ARM instruction-related constants."
  	"CogARMCompiler initialize"
  	
  	R0 := 0. R1 := 1.
  	R2 := 2. R3 := 3.
  	R4 := 4. R5 := 5.
  	R6 := 6. R7 := 7.
  	R8 := 8. R9 := 9.
  	R10 := 10. R11 := 11.
  	R12 := 12.
  	SP := 13.
  	LR := 14.
+ 	PC := 15!
- 	PC := 15.
- 	
- 	MachineCodeSize := 16. "atm: MoveRAw"
- 	NumberOfRegisters := 16.!

Item was changed:
  ----- Method: CogARMCompiler>>concretizeAt: (in category 'generate machine code') -----
  concretizeAt: actualAddress
+ 	"Generate concrete machine code for the instruction at actualAddress,
+ 	 setting machineCodeSize, and answer the following address."
- 	"Attempt to generate concrete machine code for the instruction at address.
- 	 If possible, generate the relevant machine code, setting machineCodeSize,
- 	 and answer the following address.  Otherwise answer -1."
  
  	self assert: actualAddress \\ 4 = 0.
  	address := actualAddress.
  	self dispatchConcretize.
  	self assert: (maxSize = nil or: [maxSize >= machineCodeSize]).
  	^actualAddress + machineCodeSize!

Item was changed:
  ----- Method: CogARMCompiler>>dispatchConcretize (in category 'generate machine code') -----
  dispatchConcretize
  	"Attempt to generate concrete machine code for the instruction at address.
  	 This is the inner dispatch of concretizeAt: actualAddress which exists only
  	 to get around the branch size limits in the SqueakV3 (blue book derived)
  	 bytecode set."
  
  	opcode caseOf: {
  		"Noops & Pseudo Ops"
  		[Label]					-> [^self concretizeLabel].
  		[AlignmentNops]		-> [^self concretizeAlignmentNops].
  		[Fill16]					-> [^self concretizeFill16].
  		[Fill32]					-> [^self concretizeFill32].
  		[FillFromWord]			-> [^self concretizeFillFromWord].
  		[Nop]					-> [^self concretizeNop].
  		"Specific Control/Data Movement"
+ 		"[LDM]					-> [^self concretizeLDM].
+ 		[STM]					-> [^self concretizeSTM]."
- 		"[CDQ]					-> [^self concretizeCDQ].
- 		[IDIVR]					-> [^self concretizeIDIVR].
- 		[IMULRR]				-> [^self concretizeMulRR].
- 		[CPUID]					-> [^self concretizeCPUID].
- 		[CMPXCHGAwR]			-> [^self concretizeCMPXCHGAwR].
- 		[CMPXCHGMwrR]		-> [^self concretizeCMPXCHGMwrR].
- 		[LFENCE]				-> [^self concretizeFENCE: 5].
- 		[MFENCE]				-> [^self concretizeFENCE: 6].
- 		[SFENCE]				-> [^self concretizeFENCE: 7].
- 		[LOCK]					-> [^self concretizeLOCK].
- 		[XCHGAwR]				-> [^self concretizeXCHGAwR].
- 		[XCHGMwrR]			-> [^self concretizeXCHGMwrR].
- 		[XCHGRR]				-> [^self concretizeXCHGRR]."
  		"Control"
+ 		[Call]						-> [^self concretizeCall].
+ 		[JumpR]						-> [^self concretizeJumpR].
+ 		[JumpLong]					-> [^self concretizeJumpLong].
+ 		"[JumpLongZero]			-> [^self concretizeConditionalJumpLong: 16r??].
+ 		[JumpLongNonZero]		-> [^self concretizeConditionalJumpLong: 16r??]."
+ 		[Jump]						-> [^self concretizeJump].
+ 		"[JumpZero]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpNonZero]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpNegative]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpNonNegative]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpOverflow]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpNoOverflow]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpCarry]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpNoCarry]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpLess]					-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpGreaterOrEqual]		-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpGreater]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpLessOrEqual]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpBelow]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpAboveOrEqual]		-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpAbove]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpBelowOrEqual]		-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPEqual]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPNotEqual]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPLess]				-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPGreaterOrEqual]	-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPGreater]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPLessOrEqual]		-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPOrdered]			-> [^self concretizeConditionalJump: 16r??].
+ 		[JumpFPUnordered]			-> [^self concretizeConditionalJump: 16r??]."
- 		[Call]					-> [^self concretizeCall].
- 		[JumpR]					-> [^self concretizeJumpR].
- 		[JumpLong]				-> [^self concretizeJumpLong].
- 		[JumpLongZero]		-> [^self concretizeConditionalJumpLong: 16r4].
- 		[JumpLongNonZero]	-> [^self concretizeConditionalJumpLong: 16r5].
- 		[Jump]					-> [^self concretizeJump].
- 		"Table B-1 Intel® 64 and IA-32 Architectures Software Developer's Manual Volume 1: Basic Architecture"
- 		[JumpZero]				-> [^self concretizeConditionalJump: 16r4].
- 		[JumpNonZero]			-> [^self concretizeConditionalJump: 16r5].
- 		[JumpNegative]			-> [^self concretizeConditionalJump: 16r8].
- 		[JumpNonNegative]		-> [^self concretizeConditionalJump: 16r9].
- 		[JumpOverflow]			-> [^self concretizeConditionalJump: 16r0].
- 		[JumpNoOverflow]		-> [^self concretizeConditionalJump: 16r1].
- 		[JumpCarry]			-> [^self concretizeConditionalJump: 16r2].
- 		[JumpNoCarry]			-> [^self concretizeConditionalJump: 16r3].
- 		[JumpLess]				-> [^self concretizeConditionalJump: 16rC].
- 		[JumpGreaterOrEqual]	-> [^self concretizeConditionalJump: 16rD].
- 		[JumpGreater]			-> [^self concretizeConditionalJump: 16rF].
- 		[JumpLessOrEqual]		-> [^self concretizeConditionalJump: 16rE].
- 		[JumpBelow]			-> [^self concretizeConditionalJump: 16r2].
- 		[JumpAboveOrEqual]	-> [^self concretizeConditionalJump: 16r3].
- 		[JumpAbove]			-> [^self concretizeConditionalJump: 16r7].
- 		[JumpBelowOrEqual]	-> [^self concretizeConditionalJump: 16r6].
- 		[JumpFPEqual]				-> [^self concretizeConditionalJump: 16r4].
- 		[JumpFPNotEqual]			-> [^self concretizeConditionalJump: 16r5].
- 		[JumpFPLess]				-> [^self concretizeConditionalJump: 16r2].
- 		[JumpFPGreaterOrEqual]	-> [^self concretizeConditionalJump: 16r3].
- 		[JumpFPGreater]			-> [^self concretizeConditionalJump: 16r7].
- 		[JumpFPLessOrEqual]		-> [^self concretizeConditionalJump: 16r6].
- 		[JumpFPOrdered]			-> [^self concretizeConditionalJump: 16rB].
- 		[JumpFPUnordered]			-> [^self concretizeConditionalJump: 16rA].
  		[RetN]						-> [^self concretizeRetN].
  		"Arithmetic"
  		[AddCqR]					-> [^self concretizeAddCqR].
  		[AddCwR]					-> [^self concretizeAddCwR].
  		[AddRR]						-> [^self concretizeAddRR].
+ 		"[AddRdRd]					-> [^self concretizeSEE2OpRdRd: 16r58]."
- 		[AddRdRd]					-> [^self concretizeSEE2OpRdRd: 16r58].
  		[AndCqR]					-> [^self concretizeAndCqR].
  		[AndCwR]					-> [^self concretizeAndCwR].
  		[AndRR]						-> [^self concretizeAndRR].
  		[CmpCqR]					-> [^self concretizeCmpCqR].
  		[CmpCwR]					-> [^self concretizeCmpCwR].
  		[CmpRR]					-> [^self concretizeCmpRR].
  		[CmpRdRd]					-> [^self concretizeCmpRdRd].
+ 		"[DivRdRd]					-> [^self concretizeSEE2OpRdRd: 16r5E].
+ 		[MulRdRd]					-> [^self concretizeSEE2OpRdRd: 16r59]."
- 		[DivRdRd]					-> [^self concretizeSEE2OpRdRd: 16r5E].
- 		[MulRdRd]					-> [^self concretizeSEE2OpRdRd: 16r59].
  		[OrCqR]						-> [^self concretizeOrCqR].
  		[OrCwR]					-> [^self concretizeOrCwR].
  		[OrRR]						-> [^self concretizeOrRR].
  		[SubCqR]					-> [^self concretizeSubCqR].
  		[SubCwR]					-> [^self concretizeSubCwR].
  		[SubRR]						-> [^self concretizeSubRR].
+ 		"[SubRdRd]					-> [^self concretizeSEE2OpRdRd: 16r5C]."
- 		[SubRdRd]					-> [^self concretizeSEE2OpRdRd: 16r5C].
  		[SqrtRd]						-> [^self concretizeSqrtRd].
  		[XorCwR]						-> [^self concretizeXorCwR].
  		[XorRR]							-> [^self concretizeXorRR].
  		[NegateR]						-> [^self concretizeNegateR].
  		[LoadEffectiveAddressMwrR]	-> [^self concretizeLoadEffectiveAddressMwrR].
  		[ArithmeticShiftRightCqR]		-> [^self concretizeArithmeticShiftRightCqR].
  		[LogicalShiftRightCqR]			-> [^self concretizeLogicalShiftRightCqR].
  		[LogicalShiftLeftCqR]			-> [^self concretizeLogicalShiftLeftCqR].
  		[ArithmeticShiftRightRR]			-> [^self concretizeArithmeticShiftRightRR].
  		[LogicalShiftLeftRR]				-> [^self concretizeLogicalShiftLeftRR].
  		"Data Movement"
  		[MoveCqR]			-> [^self concretizeMoveCqR].
  		[MoveCwR]			-> [^self concretizeMoveCwR].
  		[MoveRR]			-> [^self concretizeMoveRR].
  		[MoveAwR]			-> [^self concretizeMoveAwR].
  		[MoveRAw]			-> [^self concretizeMoveRAw].
  		[MoveMbrR]			-> [^self concretizeMoveMbrR].
  		[MoveRMbr]			-> [^self concretizeMoveRMbr].
  		[MoveM16rR]		-> [^self concretizeMoveM16rR].
  		[MoveM64rRd]		-> [^self concretizeMoveM64rRd].
  		[MoveMwrR]		-> [^self concretizeMoveMwrR].
  		[MoveXbrRR]		-> [^self concretizeMoveXbrRR].
  		[MoveXwrRR]		-> [^self concretizeMoveXwrRR].
  		[MoveRXwrR]		-> [^self concretizeMoveRXwrR].
  		[MoveRMwr]		-> [^self concretizeMoveRMwr].
  		[MoveRdM64r]		-> [^self concretizeMoveRdM64r].
  		[PopR]				-> [^self concretizePopR].
  		[PushR]				-> [^self concretizePushR].
  		[PushCw]			-> [^self concretizePushCw].
  		[PrefetchAw]		-> [^self concretizePrefetchAw].
  		"Conversion"
  		[ConvertRRd]		-> [^self concretizeConvertRRd] }!

Item was removed:
- ----- Method: CogARMCompiler>>genAlignCStackSavingRegisters:numArgs:wordAlignment: (in category 'abi') -----
- genAlignCStackSavingRegisters: saveRegs numArgs: numArgs wordAlignment: alignment 
- 	| wordsPushedModAlignment delta |
- 	wordsPushedModAlignment := ((saveRegs ifTrue: [16] ifFalse: [0]) "number of registers saved by genSaveRegisters"
- 								+ numArgs)
- 								\\ alignment.
- 	wordsPushedModAlignment ~= 0 ifTrue:
- 		[delta := alignment - wordsPushedModAlignment.
- 		 cogit SubCq: delta * 4 R: SPReg].
- 	^0!

Item was added:
+ ----- Method: CogARMCompiler>>genSaveRegisters (in category 'abi') -----
+ genSaveRegisters
+ 	"Save the general purpose registers for a trampoline call."
+ 	"eliot => lars, this should probably save r0 through r12, since SP, LR and PC
+ 	have specific purposes and don't need to be saved.  Save r0 through r11 if r12
+ 	is being used as a frame pointer.  This can use the store multiple instruction STM,
+ 	for which you'll need to add a class var to CogARMCompiler, because it is specific
+ 	to ARM. HTH"
+ 	self shouldBeImplemented!

Item was added:
+ ----- Method: CogARMCompiler>>machineCodeBytes (in category 'generate machine code') -----
+ machineCodeBytes
+ 	"Answer the maximum number of bytes of machine code generated for any abstract instruction.
+ 	 e.g. MoveRAw =>
+ 			mov R3, #<addressByte1>, 12
+ 			orr R3, R3, #<addressByte2>, 8
+ 			orr R3, R3, #<addressByte3>, 4
+ 			STR srcReg, [R3, + addressByte0]"
+ 	^16!

Item was added:
+ ----- Method: CogARMCompiler>>numberOfSaveableRegisters (in category 'abi') -----
+ numberOfSaveableRegisters
+ 	"Answer the number of registers to be saved in a trampoline call that saves registers.
+ 	 R0 through R12, See genSaveRegisters."
+ 	<cmacro: '() 13'>
+ 	^13!

Item was changed:
  VMStructType subclass: #CogAbstractInstruction
  	instanceVariableNames: 'opcode machineCodeSize maxSize machineCode operands address dependent cogit objectMemory bcpc'
+ 	classVariableNames: ''
- 	classVariableNames: 'MachineCodeSize NumberOfRegisters'
  	poolDictionaries: 'CogRTLOpcodes'
  	category: 'VMMaker-JIT'!
  
+ !CogAbstractInstruction commentStamp: 'eem 7/17/2012 15:04' prior: 0!
+ I am an abstract instruction generated by the Cogit.  I am subsequently concretized to machine code for the current processor.  A sequence of concretized CogAbstractInstructions are concatenated to form the code for a CogMethod.  I am an abstract class.  My concrete subclasses concretize to the machine code of a specific processor.!
- !CogAbstractInstruction commentStamp: '<historical>' prior: 0!
- I am an abstract instruction generated by the Cogit.  I am subsequently concretized to machine code for the current processor.  A sequence of concretized CogAbstractInstructions are concatenated to form the code for a CogMethod.!

Item was changed:
  ----- Method: CogAbstractInstruction class>>instVarNamesAndTypesForTranslationDo: (in category 'translation') -----
  instVarNamesAndTypesForTranslationDo: aBinaryBlock
  	"enumerate aBinaryBlock with the names and C type strings for the inst vars to include in an AbstractInstruction struct."
+ 	"CogIA32Compiler printTypedefOn: Transcript"
+ 	"CogARMCompiler printTypedefOn: Transcript"
- 	"self printTypedefOn: Transcript"
  	self filteredInstVarNames do:
  		[:ivn|
  		ivn ~= 'bcpc' ifTrue:
  			[aBinaryBlock
  				value: ivn
  				value: (ivn caseOf: {
  							['address']			-> ['unsigned long'].
+ 							['machineCode']	-> [{'unsigned char'. '[', self basicNew machineCodeBytes printString, ']'}].
- 							['machineCode']	-> [{'unsigned char'. '[', MachineCodeSize printString, ']'}].
  							['operands']		-> [#('unsigned long' '[3]')].
  							['dependent']		-> ['struct _AbstractInstruction *']}
  						otherwise:
  							[#char])]]!

Item was changed:
  ----- Method: CogAbstractInstruction>>concretizeAt: (in category 'generate machine code') -----
  concretizeAt: actualAddress
+ 	"Generate concrete machine code for the instruction at actualAddress,
+ 	 setting machineCodeSize, and answer the following address."
- 	"Attempt to generate concrete machine code for the instruction at actualAddress.
- 	 If possible, generate the relevant machine code, setting machineCodeSize,
- 	 and answer the following address.  Otherwise answer -1."
  	self subclassResponsibility!

Item was changed:
  ----- Method: CogAbstractInstruction>>genAlignCStackSavingRegisters:numArgs:wordAlignment: (in category 'abi') -----
  genAlignCStackSavingRegisters: saveRegs numArgs: numArgs wordAlignment: alignment 
  	| wordsPushedModAlignment delta |
+ 	wordsPushedModAlignment := ((saveRegs ifTrue: [self numberOfSaveableRegisters] ifFalse: [0])
+ 									+ numArgs)
+ 									\\ alignment.
- 	wordsPushedModAlignment := ((saveRegs ifTrue: [NumberOfRegisters] ifFalse: [0]) "number of registers saved by genSaveRegisters"
- 								+ numArgs)
- 								\\ alignment.
  	wordsPushedModAlignment ~= 0 ifTrue:
  		[delta := alignment - wordsPushedModAlignment.
  		 cogit SubCq: delta * 4 R: SPReg].
  	^0!

Item was added:
+ ----- Method: CogAbstractInstruction>>genSaveRegisters (in category 'abi') -----
+ genSaveRegisters
+ 	"Save the general purpose registers for a trampoline call."
+ 	self subclassResponsibility!

Item was changed:
  ----- Method: CogAbstractInstruction>>initialize (in category 'initialization') -----
  initialize
  	"This method intializes the Smalltalk instance.  The C instance is merely a struct and doesn't need initialization."
  	<doNotGenerate>
  	operands := CArrayAccessor on: (Array new: 3).
+ 	machineCode := CArrayAccessor on: (ByteArray new: self machineCodeBytes)!
- 	machineCode := CArrayAccessor on: (ByteArray new: MachineCodeSize)!

Item was added:
+ ----- Method: CogAbstractInstruction>>machineCodeBytes (in category 'generate machine code') -----
+ machineCodeBytes
+ 	"Answer the maximum number of bytes of machine code generated for any abstract instruction."
+ 	self subclassResponsibility!

Item was added:
+ ----- Method: CogAbstractInstruction>>numberOfSaveableRegisters (in category 'abi') -----
+ numberOfSaveableRegisters
+ 	"Answer the number of registers to be saved in a trampoline call that saves registers.
+ 	 See genSaveRegisters"
+ 	self subclassResponsibility!

Item was changed:
  ----- Method: CogIA32Compiler class>>initialize (in category 'class initialization') -----
  initialize
  	"Initialize various IA32/x86 instruction-related constants.
  	 [1] IA-32 Intel® Architecture Software Developer's Manual Volume 2A: Instruction Set Reference, A-M"
  
  	"CogIA32Compiler initialize"
  
  	| specificOpcodes refs |
  	self ~~ CogIA32Compiler ifTrue: [^self].
  
  	"N.B. EAX ECX and EDX are caller-save (scratch) registers.
  		EBX ESI and EDI are callee-save; see concreteRegisterFor:"
  	EAX := 0.
  	ECX := 1.  "Were they completely mad or simply sadistic?"
  	EDX := 2.
  	EBX := 3.
  	ESP := 4.
  	EBP := 5.
  	ESI := 6.
  	EDI := 7.
  
  	XMM0L := 0.
  	XMM1L := 2.
  	XMM2L := 4.
  	XMM3L := 6.
  	XMM4L := 8.
  	XMM5L := 10.
  	XMM6L := 12.
  	XMM7L := 14.
  
  	XMM0H := 1.
  	XMM1H := 3.
  	XMM2H := 5.
  	XMM3H := 7.
  	XMM4H := 9.
  	XMM5H := 11.
  	XMM6H := 13.
  	XMM7H := 15.
  
  	"Mod R/M Mod fields.  See [1] Sec 2.4, 2.5 & 2.6 & Table 2-2"
  	ModRegInd := 0.
  		ModRegIndSIB := 4.
  		ModRegIndDisp32 := 5.
  	ModRegRegDisp8 := 1.
  	ModRegRegDisp32 := 2.
  	ModReg := 3.
  
  	"SIB Scaled Index modes.  See [1] Sec 2.4, 2.5 & 2.6 & Table 2-3"
  	SIB1 := 0.
  	SIB2 := 1.
  	SIB4 := 2.
  	SIB8 := 3.
  
  	"Specific instructions"
  	LastRTLCode isNil ifTrue:
  		[CogRTLOpcodes initialize].
  	specificOpcodes := #(CDQ IDIVR IMULRR CPUID LFENCE MFENCE SFENCE LOCK CMPXCHGAwR CMPXCHGMwrR XCHGAwR XCHGMwrR XCHGRR).
  	refs := (thisContext method literals select: [:l| l isVariableBinding and: [classPool includesKey: l key]]) collect:
  				[:ea| ea key].
  	(classPool keys reject: [:k| (specificOpcodes includes: k) or: [refs includes: k]]) do:
  		[:k|
  		Undeclared declare: k from: classPool].
  	specificOpcodes withIndexDo:
  		[:classVarName :value|
  		self classPool
  			declare: classVarName from: Undeclared;
+ 			at: classVarName put: value + LastRTLCode - 1]!
- 			at: classVarName put: value + LastRTLCode - 1].
- 
- 	MachineCodeSize := 10. "e.g. lock movsd  0x400(%esp),%xmm4 => f0 f2 0f 10 a4 24 00 04 00 00"
- 	NumberOfRegisters := 6!

Item was changed:
  ----- Method: CogIA32Compiler>>concretizeAt: (in category 'generate machine code') -----
  concretizeAt: actualAddress
+ 	"Generate concrete machine code for the instruction at actualAddress,
+ 	 setting machineCodeSize, and answer the following address."
- 	"Attempt to generate concrete machine code for the instruction at address.
- 	 If possible, generate the relevant machine code, setting machineCodeSize,
- 	 and answer the following address.  Otherwise answer -1."
  
  	address := actualAddress.
  	self dispatchConcretize.
  	self assert: (maxSize = nil or: [maxSize >= machineCodeSize]).
  	^actualAddress + machineCodeSize!

Item was changed:
  ----- Method: CogIA32Compiler>>genSaveRegisters (in category 'abi') -----
  genSaveRegisters
+ 	"Save the general purpose registers for a trampoline call."
  	cogit
  		PushR: EDI;
  		PushR: ESI;
  		PushR: EDX;
  		PushR: ECX;
  		PushR: EBX;
  		PushR: EAX.
  	^0!

Item was added:
+ ----- Method: CogIA32Compiler>>machineCodeBytes (in category 'generate machine code') -----
+ machineCodeBytes
+ 	"Answer the maximum number of bytes of machine code generated for any abstract instruction.
+ 	 e.g. lock movsd  0x400(%esp),%xmm4 => f0 f2 0f 10 a4 24 00 04 00 00"
+ 	^10!

Item was added:
+ ----- Method: CogIA32Compiler>>numberOfSaveableRegisters (in category 'abi') -----
+ numberOfSaveableRegisters
+ 	"Answer the number of registers to be saved in a trampoline call that saves registers.
+ 	 See genSaveRegisters"
+ 	<cmacro: '() 6'>
+ 	^6!

