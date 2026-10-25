PV_HPshiftDown : UGen 
{
	*new { arg buffer, shift=0;
		^this.multiNew('control', buffer, shift)
	}
}

PV_HPecartType : UGen 
{
	*new { arg buffer, mulEcartType=1.0;
		^this.multiNew('control', buffer, mulEcartType)
	}
}

PV_HPshimes : UGen 
{
	*new { arg buffer1, index=1;
		^this.multiNew('control', buffer1, index)
	}
}

PV_HPfiltre : UGen 
{
	*new { arg buffer, index=0, size=2048;
		^this.multiNew('control', buffer, index, size)
	}
}

PV_HPinverse : UGen 
{
	*new { arg buffer, index=0, size=2048;
		^this.multiNew('control', buffer, index, size)
	}
}

//PV_HPneurones : UGen 
//{
//	*new { arg buffer1, buffer2, neurones=3, apprentissage=0.1, temperature=1.0, mode=1.0;
//		^this.multiNew('control', buffer1, buffer2, neurones, apprentissage, temperature, mode)
//	}
//}
//
//PV_HPdifference : UGen 
//{
//	*new { arg buffer1, buffer2;
//		^this.multiNew('control', buffer1, buffer2)
//	}
//}
//
//PV_HPrandom : UGen 
//{
//	*new { arg buffer1, buffer2;
//		^this.multiNew('control', buffer1, buffer2)
//	}
//}

