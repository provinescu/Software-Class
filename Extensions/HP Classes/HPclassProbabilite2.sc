HPclassProbabilite2 {

var lastf=0, lasta=0, lastd=0, listeinf, listeina, listeind, positionf, positiona, positiond, size;

*new{arg s;
	^super.new.init(s);
}

init {arg s;
if(s == nil, {size=64}, {size=s});if(size < 1, {size=1});if(size > 256, {size=256});
		size.do({arg i;
		listeinf=listeinf.add(0);
		listeina=listeina.add(0);
		listeind=listeind.add(0);
		});
positionf=0;positiona=0;positiond=0;
}

next {arg f, a, d;
var low, high, moyenne, ecarttype, freq=[], amp=[], duree=[];
// algorithme 2
// setup population f
if(f.size > 0,{f.size.do({arg i;listeinf.put(positionf, f.at(i));
positionf=positionf+1;if(positionf >= size, {positionf=0})})},
{listeinf.put(positionf, f);positionf=positionf+1;if(positionf >= size, {positionf=0})});
// calcule freq
if(f.size > 0, {f.size.do({arg i;
low=listeinf.minItem;
high=listeinf.maxItem;
moyenne=listeinf.mean;
ecarttype=listeinf.ecartType;
if(ecarttype != 0 ,{freq=freq.add(lastf - moyenne / ecarttype)},{freq=freq.add(lastf - moyenne)});
freq=freq%(high - low) + low;lastf=freq.last});
},
{low=listeinf.minItem;
high=listeinf.maxItem;
moyenne=listeinf.mean;
ecarttype=listeinf.ecartType;
if(ecarttype != 0 ,{freq=lastf - moyenne / ecarttype},{freq=lastf - moyenne});
freq=freq%(high - low) + low;lastf=freq});
// setup population a
if(a.size > 0,{a.size.do({arg i;listeina.put(positiona, a.at(i));
positiona=positiona+1;if(positiona >= size, {positiona=0})})},
{listeina.put(positiona, a);positiona=positiona+1;if(positiona >= size, {positiona=0})});
// calcule amp
if(a.size > 0, {a.size.do({arg i;
low=listeina.minItem;
high=listeina.maxItem;
moyenne=listeina.mean;
ecarttype=listeina.ecartType;
if(ecarttype != 0 ,{amp=amp.add(lasta - moyenne / ecarttype)},{amp=amp.add(lasta - moyenne)});
amp=amp%(high - low) + low;lasta=amp.last});
},
{low=listeina.minItem;
high=listeina.maxItem;
moyenne=listeina.mean;
ecarttype=listeina.ecartType;
if(ecarttype != 0 ,{amp=lasta - moyenne / ecarttype},{amp=lasta - moyenne});
amp=amp%(high - low) + low;lasta=amp});
// setup population duree
if(d.size > 0,{d.size.do({arg i;listeind.put(positiond, d.at(i));
positiond=positiond+1;if(positiond >= size, {positiond=0})})},
{listeind.put(positiond, d);positiond=positiond+1;if(positiond >= size, {positiond=0})});
// calcule duree
if(d.size > 0, {d.size.do({arg i;
low=listeind.minItem;
high=listeind.maxItem;
moyenne=listeind.mean;
ecarttype=listeind.ecartType;
if(ecarttype != 0 ,{duree=duree.add(lastd - moyenne / ecarttype)},{duree=duree.add(lastd - moyenne)});
duree=duree%(high - low) + low;lastd=duree.last});
},
{low=listeind.minItem;
high=listeind.maxItem;
moyenne=listeind.mean;
ecarttype=listeind.ecartType;
if(ecarttype != 0 ,{duree=lastd - moyenne / ecarttype},{duree=lastd - moyenne});
duree=duree%(high - low) + low;lastd=duree});
// sortie Algo
^[freq, amp, duree]
}

}
