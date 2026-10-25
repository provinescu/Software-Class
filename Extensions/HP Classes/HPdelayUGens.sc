HPplayBuf : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, rate=1.0, trigger=0.0, startPos=0.0, loop = 1.0, seuil=0.33, sensibilite=0.5, doneAction=0;
		^this.multiNew('audio', numChannels, bufnum, rate, trigger, startPos, loop, seuil, sensibilite, doneAction)
	}

	*kr { arg numChannels, bufnum=0, rate=1.0, trigger=0.0, startPos=0.0, loop = 1.0, seuil=0.33, sensibilite=0.5, doneAction=0;
		^this.multiNew('control', numChannels, bufnum, rate, trigger, startPos, loop, seuil, sensibilite, doneAction)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
}

HPtGrains : MultiOutUGen {
	*ar { arg numChannels, trigger=0, bufnum=0, rate=1, centerPos=0, 
			dur=0.1, pan=0, amp=0.1, seuil=0.33, sensibilite=0.5, interp=4; 
		if (numChannels < 2) {
			 "TGrains needs at least two channels.".error;
			 ^nil
		}
		^this.multiNew('audio', numChannels, trigger, bufnum, rate, centerPos, 
				dur, pan, amp, seuil, sensibilite, interp)
	}
	
		*kr { arg numChannels, trigger=0, bufnum=0, rate=1, centerPos=0, 
			dur=0.1, pan=0, amp=0.1, seuil=0.33, sensibilite=0.5, interp=4; 
		if (numChannels < 2) {
			 "TGrains needs at least two channels.".error;
			 ^nil
		}
		^this.multiNew('control', numChannels, trigger, bufnum, rate, centerPos, 
				dur, pan, amp, seuil, sensibilite, interp)
	}

	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
}

HPbufRd : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, phase=0.0, rate=1.0, loop=1.0, seuil=0.33, sensibilite=0.5, interp=2;
		^this.multiNew('audio', numChannels, bufnum, phase, rate, loop, seuil, sensibilite, interp)
	}

	*kr { arg numChannels, bufnum=0, phase=0.0, rate=1.0, loop=1.0, seuil=0.33, sensibilite=0.5, interp=2;
		^this.multiNew('control', numChannels, bufnum, phase, rate, loop, seuil, sensibilite, interp)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
	checkInputs {
 		if (rate == 'audio' and: {inputs.at(1).rate != 'audio'}, { 
 			^("phase input is not audio rate: " + inputs.at(1) + inputs.at(1).rate);
 		});
 		^this.checkValidInputs
 	}
}
