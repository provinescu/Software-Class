HPkohonen3 : UGen {
	*ar { arg rate = 100.0, in1 = 0, in2 = 0, in3 = 0, learningRate = 0.16, distanceNeighbourhood = 127/6, gate = 1,mul = 1.0, add = 0.0;
		^this.multiNew('audio', rate, in1, in2, in3, learningRate, distanceNeighbourhood, gate).madd(mul, add)
	}

	*kr { arg rate = 100.0, in1 = 0, in2 = 0, in3 = 0, learningRate = 0.16, distanceNeighbourhood = 127/6, gate = 1,mul = 1.0, add = 0.0;
		^this.multiNew('control', rate, in1, in2, in3, learningRate, distanceNeighbourhood, gate).madd(mul, add)
	}

}
