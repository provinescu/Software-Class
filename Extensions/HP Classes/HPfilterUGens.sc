HPfilter : UGen {

	*ar { arg in = 0.0, parametre=1.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', in, parametre).madd(mul, add)
	}
	*kr { arg in = 0.0, parametre=1.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', in, parametre).madd(mul, add)
	}
}