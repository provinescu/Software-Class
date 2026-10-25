HPclassProbabilite1 {

var grainf, octavef, graina, octavea, graind, octaved, listef, listefo, listefd, listea, listeao, listead, listed, listedo, listedd, lastf=0, lasta=0, lastd=0, listeinf, listeina, listeind, positionf, positiona, positiond, size;

*new {arg s, gf, of, ga, oa, gd, od;
	
	^super.new.init(s, gf, of, ga, oa, gd, od);
}
	
init {arg s, gf, of, ga, oa, gd, od;
	
	if(s == nil,
		{size=64},
			{size=s;
				if(size < 2,
					{size=2});
						if(size > 256,
							{size=256});
	});
	if(gf == nil, {grainf=100},{grainf=gf;if(grainf < 1, {grainf=1});if(grainf >100, {grainf=100})});
	if(of == nil, {octavef=12},{octavef=of;if(octavef < 1, {octavef=1});if(octavef > 12, {octavef=12})});
	if(ga == nil, {graina=100},{graina=ga;if(graina < 1, {graina=1});if(graina > 100, {graina=100})});
	if(oa == nil, {octavea=12},{octavea=oa;if(octavea < 1, {octavea=1});if(octavea > 12, {octavea=12})});
	if(gd == nil, {graind=100},{graind=gd;if(graind < 1, {graind=1});if(graind > 100, {graind=100})});
	if(od == nil, {octaved=2},{octaved=od;if(octaved < 1, {octaved=1});if(octaved > 12, {octaved=12})});
	positionf=0;positiona=0;positiond=0;
	// init listes freq avec grainf positions sur octavef octaves et 3 directions
	(grainf + 1).do({arg i;listef=listef.add(0)});
	((127 / octavef).floor + 1).do({arg i;listefo=listefo.add(0)});
	3.do({arg i;listefd=listefd.add(0)});
	// init listes amp avec graina positions sur octavea octaves et 3 directions
	(graina + 1).do({arg i;listea=listea.add(0)});
	((127 / octavea).floor + 1).do({arg i;listeao=listeao.add(0)});
	3.do({arg i;listead=listead.add(0)});
	// init listes duree avec graind positions sur octaved octaves et 3 directions
	(graind + 1).do({arg i;listed=listed.add(0)});
	(octaved + 1).do({arg i;listedo=listedo.add(0)});
	3.do({arg i;listedd=listedd.add(0)});
		size.do({arg i;
		listeinf=listeinf.add(0);
		listeina=listeina.add(0);
		listeind=listeind.add(0);
		});
}

// algorithme 1
next {arg f, a, d;
var low, high,interval, octave, direction, freq=[], amp=[], duree=[];
// setup population f
if(f.size > 0,{f.size.do({arg i;listeinf.put(positionf, f.at(i));
positionf=positionf+1;if(positionf >= size, {positionf=0})})},
{listeinf.put(positionf, f);positionf=positionf+1;if(positionf >= size, {positionf=0})});
// calcule freq
if(f.size > 0, {f.size.do({arg i;
// calcule des différences entre frequences
lastf=lastf*127;
low=listeinf.minItem*127;
high=listeinf.maxItem*127;
for(1,listeinf.size-1,{arg i;var ff, of, vf;
ff=((listeinf.at(i-1) - listeinf.at(i))*127);
// set direction de l'interval
if(ff == 0 , {listefd.put(1,listefd.at(1)+1)},
{if(ff < 0 ,{listefd.put(0,listefd.at(0)+1)},{listefd.put(2,listefd.at(2)+1)});
});
ff=abs(ff);
// set octave de l'interval
of=(ff / octavef).floor;
listefo.put(of,listefo.at(of)+1);
// set interval
vf=(frac(ff / octavef) * grainf).floor;
listef.put(vf,listef.at(vf)+1);
});
// set new frequence
interval=listef.normalizeSum.windex;
octave=listefo.normalizeSum.windex;
direction=listefd.normalizeSum.windex;
direction=direction-1;
freq=freq.add((lastf+(interval / grainf + octave * octavef * direction))%(high - low) + low);lastf=freq.last/127});
},
{// calcule des différences entre frequences
lastf=lastf*127;
low=listeinf.minItem*127;
high=listeinf.maxItem*127;
for(1,listeinf.size-1,{arg i;var ff, of, vf;
ff=((listeinf.at(i-1) - listeinf.at(i))*127);
// set direction de l'interval
if(ff == 0 , {listefd.put(1,listefd.at(1)+1)},
{if(ff < 0 ,{listefd.put(0,listefd.at(0)+1)},{listefd.put(2,listefd.at(2)+1)});
});
ff=abs(ff);
// set octave de l'interval
of=(ff / octavef).floor;
listefo.put(of,listefo.at(of)+1);
// set interval
vf=(frac(ff / octavef) * grainf).floor;
listef.put(vf,listef.at(vf)+1);
});
// set new frequence
interval=listef.normalizeSum.windex;
octave=listefo.normalizeSum.windex;
direction=listefd.normalizeSum.windex;
direction=direction-1;
freq=(lastf+(interval / grainf + octave * octavef * direction))%(high - low) + low;lastf=freq/127;
});
// setup population a
if(a.size > 0,{a.size.do({arg i;listeina.put(positiona, a.at(i));
positiona=positiona+1;if(positiona >= size, {positiona=0})})},
{listeina.put(positiona, a);positiona=positiona+1;if(positiona >= size, {positiona=0})});
// calcule amp
if(a.size > 0, {a.size.do({arg i;
// calcule des différences entre amplitudes
lasta=lasta*127;
low=listeina.minItem*127;
high=listeina.maxItem*127;
for(1,listeina.size-1,{arg i;var fa, oa, va;
fa=((listeina.at(i-1) - listeina.at(i))*127);
// set direction de l'interval
if(fa ==0 , {listead.put(1,listead.at(1)+1)},
{if(fa < 0 ,{listead.put(0,listead.at(0)+1)},{listead.put(2,listead.at(2)+1)});
});
fa=abs(fa);
// set octave de l'interval
oa=(fa / octavea).floor;
listeao.put(oa,listeao.at(oa)+1);
// set interval
va=(frac(fa / octavea) * graina).floor;
listea.put(va,listea.at(va)+1);
});
// set new amplitude
interval=listea.normalizeSum.windex;
octave=listeao.normalizeSum.windex;
direction=listead.normalizeSum.windex;
direction=direction-1;
amp=amp.add((lasta+(interval / graina + octave * octavea * direction))%(high - low) + low);lasta=amp.last/127});
},
{// calcule des différences entre amplitudes
lasta=lasta*127;
low=listeina.minItem*127;
high=listeina.maxItem*127;
for(1,listeina.size-1,{arg i;var fa, oa, va;
fa=((listeina.at(i-1) - listeina.at(i))*127);
// set direction de l'interval
if(fa ==0 , {listead.put(1,listead.at(1)+1)},
{if(fa < 0 ,{listead.put(0,listead.at(0)+1)},{listead.put(2,listead.at(2)+1)});
});
fa=abs(fa);
// set octave de l'interval
oa=(fa / octavea).floor;
listeao.put(oa,listeao.at(oa)+1);
// set interval
va=(frac(fa / octavea) * graina).floor;
listea.put(va,listea.at(va)+1);
});
// set new amplitude
interval=listea.normalizeSum.windex;
octave=listeao.normalizeSum.windex;
direction=listead.normalizeSum.windex;
direction=direction-1;
amp=(lasta+(interval / graina + octave * octavea * direction))%(high - low) + low;lasta=amp/127;
});
// setup population d
if(d.size > 0,{d.size.do({arg i;listeind.put(positiond, d.at(i));
positiond=positiond+1;if(positiond >= size, {positiond=0})})},
{listeind.put(positiond, d);positiond=positiond+1;if(positiond >= size, {positiond=0})});
// calcule duree
if(d.size > 0, {d.size.do({arg i;
// calcule des différences entre durees
low=listeind.minItem;
high=listeind.maxItem;
for(1,listeind.size-1,{arg i;var fd, od, vd;
fd=(listeind.at(i-1) - listeind.at(i));
// set direction de l'interval
if(fd == 0 , {listedd.put(1,listedd.at(1)+1)},
{if(fd < 0 ,{listedd.put(0,listedd.at(0)+1)},{listedd.put(2,listedd.at(2)+1)});
});
fd=abs(fd);
// set octave de l'interval
od=(fd / octaved).floor;
if(od >= listedo.size ,{od=listedo.size -1});
listedo.put(od,listedo.at(od)+1);
// set interval
vd=(frac(fd / octaved) * graind).floor;
listed.put(vd,listed.at(vd)+1);
});
// set new duree
interval=listed.normalizeSum.windex;
octave=listedo.normalizeSum.windex;
direction=listedd.normalizeSum.windex;
direction=direction-1;
if(direction != 0, {duree=duree.add((lastd+(interval / graind + octave * graind * direction)));
duree=duree%(high-low)+low},
{duree=duree.add(lastd%(high-low)+low)});
lastd=duree.last})},
{// calcule des différences entre durees
low=listeind.minItem;
high=listeind.maxItem;
for(1,listeind.size-1,{arg i;var fd, od, vd;
fd=(listeind.at(i-1) - listeind.at(i));
// set direction de l'interval
if(fd == 0 , {listedd.put(1,listedd.at(1)+1)},
{if(fd < 0 ,{listedd.put(0,listedd.at(0)+1)},{listedd.put(2,listedd.at(2)+1)});
});
fd=abs(fd);
// set octave de l'interval
od=(fd / octaved).floor;
if(od >= listedo.size ,{od=listedo.size -1});
listedo.put(od,listedo.at(od)+1);
// set interval
vd=(frac(fd / octaved) * graind).floor;
listed.put(vd,listed.at(vd)+1);
});
// set new duree
interval=listed.normalizeSum.windex;
octave=listedo.normalizeSum.windex;
direction=listedd.normalizeSum.windex;
direction=direction-1;
if(direction != 0, {duree=(lastd+(interval / graind + octave * graind * direction));
duree=duree%(high-low)+low},
{duree=lastd%(high-low)+low});
lastd=duree;
});
// sortie Algo
^[freq/127, amp/127, duree]

}

}
