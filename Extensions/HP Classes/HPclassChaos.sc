HPclassChaos {var <> kchaos, <> data;
	
*new {arg k, d;
^super.new.init(k,d);
}

init {arg k,d;
if(k == nil ,{k=1.0.rand+3});
if(d == nil ,{d=1.0.rand});
kchaos=k;
data=d;
}

next {
data=kchaos*data*(1-data);// quadratique
^data;
}

}

