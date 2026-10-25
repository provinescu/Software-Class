HPchaos : UGen {
	*ar { arg freq = 689.0625, chaos = 3.951, data = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, chaos, data).madd(mul, add)
	}
	*kr { arg freq = 100.0, chaos = 3.951, data = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, chaos, data).madd(mul, add)
	}	
}

HPneurones : UGen {
	*ar { arg freq = 22055, datain = 0.5, dataout = 0.5, mode = 1.0, apprentissage = 0.5, temperature = 0.5, neurones = 4, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, datain, dataout, mode, apprentissage, temperature, neurones).madd(mul, add)
	}
	*kr { arg freq = 689.0625, datain = 0.5, dataout = 0.5, mode = 1.0, apprentissage = 0.5, temperature = 0.5, neurones = 4, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, datain, dataout, mode, apprentissage, temperature, neurones).madd(mul, add)
	}
}

HPgenetiques : UGen {
	*ar { arg freq = 22055, datain = 0.5, croisement = 0.5, mutation = 0.01, population = 64, mode = 1.0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', freq, datain, croisement, mutation, population, mode).madd(mul, add)
	}
	*kr { arg freq = 689.0625, datain = 0.5, croisement = 0.5, mutation = 0.01, population = 64, mode = 1.0, mul  = 1.0, add = 0.0;
		^this.multiNew('control', freq, datain, croisement, mutation, population, mode).madd(mul, add)
	}	
}

HPneurones3D : MultiOutUGen {
	*kr { arg freq = 689.0625, datainF = 0.5, datainA = 0.5, datainD = 0.5, dataoutF = 0.5, dataoutA = 0.5, dataoutD = 0.5, mode = 1.0, apprentissage = 0.5, temperature = 0.5, neurones = 8, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, datainF, datainA, datainD, dataoutF, dataoutA, dataoutD, mode, apprentissage, temperature, neurones).madd(mul, add)
	}
	init { arg ... theInputs;
	inputs = theInputs;
	^this.initOutputs(3, rate);
	}
}

HPgenetiques3D : MultiOutUGen {
	*kr { arg freq = 689.0625, datainF = 0.5, datainA = 0.5, datainD = 0.5, croisement = 0.5, mutation = 0.01, population = 64, mode = 1.0, mul  = 1.0, add = 0.0;
		^this.multiNew('control', freq, datainF, datainA, datainD, croisement, mutation, population, mode).madd(mul, add)
	}
	init { arg ... theInputs;
	inputs = theInputs;
	^this.initOutputs(3, rate);
	}
}

HPprobability1 : UGen {
	*ar { arg freq = 22055, datain = 0.5, size = 64, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', freq, datain, size).madd(mul, add)
	}
	*kr { arg freq = 689.0625, datain = 0.5, size = 64, mul  = 1.0, add = 0.0;
		^this.multiNew('control', freq, datain, size).madd(mul, add)
	}
}

HPprobability2 : UGen {
	*ar { arg freq = 22055, datain = 0.5, size = 64, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', freq, datain, size).madd(mul, add)
	}
	*kr { arg freq = 689.0625, datain = 0.5, size = 64, mul  = 1.0, add = 0.0;
		^this.multiNew('control', freq, datain, size).madd(mul, add)
	}
}

HPfractal3D : MultiOutUGen {
	*ar { arg freq = 22055, datainF = 0.5, datainA = 0.5, datainD = 0.5, transformations = 4, mode = 0.0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', freq, datainF, datainA, datainD, transformations).madd(mul, add)
	}
	*kr { arg freq = 689.0625, datainF = 0.5, datainA = 0.5, datainD = 0.5, transformations = 4, mode = 0.0, mul  = 1.0, add = 0.0;
		^this.multiNew('control', freq, datainF, datainA, datainD, transformations, mode).madd(mul, add)
	}
	init { arg ... theInputs;
	inputs = theInputs;
	^this.initOutputs(3, rate);
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////

HPfftShift : UGen {
	*ar { arg in1 = 0.0, index = 0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', in1, index).madd(mul, add)
	}
}

HPfftLog : UGen {
	*ar { arg in1 = 0.0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', in1).madd(mul, add)
	}
}

HPfftDiff : UGen {
	*ar { arg in1 = 0.0, in2 = 0.0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', in1, in2).madd(mul, add)
	}
}

HPfftRand : UGen {
	*new { arg in1 = 0.0, in2 = 0.0, mul  = 1.0, add = 0.0;
		^this.multiNew('audio', in1, in2).madd(mul, add)
	}

}
