(
// HPtransformerRT V30 - DIAGNOSTIC REEL TORUS-AWARE - SC 3.14 strict
// Mesure la qualite, les cas extremes, l'arret anticipe et l'interference.
var trainN=48, valN=16, epochs=25, warmup=8, patience=6, minGain=0.0001;
var threshold=0.25, model, mask, finite, valid, compErr, evaluate, worstCase;
var makeInput, targetA, targetB, trainX, trainY, valX, valY, aX, aY, bX, bY;
var bestMAE=999999.0, bestEpoch=0, stale=0, suggestedStop=nil;
var trainCurve, valCurve, rmseCurve, maxCurve, info, met, prev=nil;
var testConfig, configs, results, bestConfig=0, bestScore=999999.0;
var repeats=3, scores, mean, variance=0.0, std, start, elapsed;
var test, pass=0, fail=0, failures=List.new;

finite = { |x| x.isNumber and: { x.isNaN.not } };
valid = { |v|
    var ok = v.notNil and: { v.isArray and: { v.size == 8 } };
    if(ok) {
        v.do { |x|
            if(finite.value(x).not or: { x < 0.0 or: { x > 1.0 } }) { ok=false };
        };
    };
    ok
};

// Distance lineaire pour une dimension normale, circulaire pour une dimension
// activee dans torusMask. Ex.: 0.97 contre 0.10 donne 0.13, et non 0.87.
compErr = { |prediction, target, torus=false|
    var e=(prediction-target).abs;
    if(torus == true or: { torus == 1 }) { e.min(1.0-e) } { e }
};

test = { |name, fn|
    var r=fn.try { |err| failures.add(name++" :: "++err.asString); false };
    if(r == true) { pass=pass+1; ("PASS | "++name).postln }
    { fail=fail+1; failures.add(name++" :: condition non satisfaite"); ("FAIL | "++name).postln };
};

evaluate = { |m, xs, ys, torusMask|
    var absSum=0.0, sqSum=0.0, maxE=0.0, count=0, validCount=0;
    xs.size.do { |i|
        var p=m.predict(xs[i]);
        if(valid.value(p)) {
            8.do { |d|
                var e=compErr.value(p[d],ys[i][d],torusMask[d]);
                absSum=absSum+e; sqSum=sqSum+(e**2); maxE=maxE.max(e); count=count+1;
            };
            validCount=validCount+1;
        };
    };
    (mae:absSum/count.max(1), rmse:(sqSum/count.max(1)).sqrt,
        maxError:maxE, validCount:validCount)
};

worstCase = { |m, xs, ys, torusMask, limit=0.25|
    var maxE=(-1.0), sample=(-1), dim=(-1), pBest, xBest, yBest;
    var outliers=0, perDim=Array.fill(8,0.0), linearAtWorst=0.0;
    xs.size.do { |i|
        var p=m.predict(xs[i]);
        if(valid.value(p)) {
            8.do { |d|
                var linear=(p[d]-ys[i][d]).abs;
                var e=compErr.value(p[d],ys[i][d],torusMask[d]);
                perDim[d]=perDim[d]+e;
                if(e > limit) { outliers=outliers+1 };
                if(e > maxE) {
                    maxE=e; sample=i; dim=d; pBest=p.copy;
                    xBest=xs[i].copy; yBest=ys[i].copy; linearAtWorst=linear;
                };
            };
        };
    };
    (error:maxE, linearError:linearAtWorst, sample:sample, dimension:dim,
        isTorus:torusMask[dim], input:xBest, target:yBest, prediction:pBest,
        outliers:outliers, dimensionMAE:perDim/xs.size.max(1))
};

makeInput = { |k=0|
    [(0.07+k*0.037).wrap(0,1),(0.19+k*0.053).wrap(0,1),
    (0.41+(k%11)*0.047).clip(0,1),(0.13+k*0.071).wrap(0,1),
    (0.91-(k%23)*0.031).clip(0,1),(0.31+k*0.061).wrap(0,1),
    (0.03+k*0.089).wrap(0,1),(0.67+k*0.043).wrap(0,1)]
};
targetA = { |x|
    [x[0]*0.72+x[1]*0.18+0.05,x[1]*0.65+x[2]*0.25+0.04,
    x[2]*0.74+(x[3]-0.5)*0.12+0.08,x[3]*0.68+x[4]*0.22+0.03,
    x[4]*0.70+(1-x[5])*0.20+0.02,x[5]*0.76+x[6]*0.14+0.05,
    x[6]*0.67+x[7]*0.23+0.04,x[7]*0.73+x[0]*0.17+0.05].clip(0,1)
};
targetB = { |x|
    [x[0]*0.52+x[7]*0.33+0.07,x[1]*0.50+(1-x[0])*0.35+0.05,
    x[2]*0.55+x[5]*0.30+0.06,x[3]*0.48+x[6]*0.37+0.05,
    x[4]*0.50+x[2]*0.34+0.06,x[5]*0.54+(1-x[3])*0.31+0.05,
    x[6]*0.51+x[1]*0.34+0.06,x[7]*0.53+x[4]*0.32+0.06].clip(0,1)
};

trainX=Array.fill(trainN,{|i|makeInput.value(i)}); trainY=trainX.collect(targetA);
valX=Array.fill(valN,{|i|makeInput.value(i+1048)}); valY=valX.collect(targetA);
aX=Array.fill(32,{|i|makeInput.value(i+2000)}); aY=aX.collect(targetA);
bX=Array.fill(32,{|i|makeInput.value(i+3000)}); bY=bX.collect(targetB);

start=Main.elapsedTime;
"============================================================".postln;
"HPtransformerRT V30 - DIAGNOSTIC TORUS-AWARE".postln;
"============================================================".postln;
model=HPtransformerRT.new; mask=model.getParameter(\torusMask);
("torusMask utilise = "++mask).postln;
trainCurve=Array.newClear(epochs+1); valCurve=Array.newClear(epochs+1);
rmseCurve=Array.newClear(epochs+1); maxCurve=Array.newClear(epochs+1);

"01. COURBE ET CAS EXTREMES CORRIGES".postln;
(epochs+1).do { |epoch|
    if(epoch>0) {
        trainN.do { |step| var i=(step+epoch-1)%trainN; model.learnPair(trainX[i],trainY[i]) };
    };
    trainCurve[epoch]=evaluate.value(model,trainX,trainY,mask)[\mae];
    met=evaluate.value(model,valX,valY,mask); info=worstCase.value(model,valX,valY,mask,threshold);
    valCurve[epoch]=met[\mae]; rmseCurve[epoch]=met[\rmse]; maxCurve[epoch]=info[\error];
    if(met[\mae] < (bestMAE-minGain)) {
        bestMAE=met[\mae]; bestEpoch=epoch; stale=0;
    } {
        if(epoch>=warmup) { stale=stale+1 };
    };
    if(epoch==warmup) { stale=0 };
    if((epoch>=warmup) and: { stale>=patience } and: { suggestedStop.isNil }) {
        suggestedStop=epoch;
    };
    ("Epoque "++epoch++" | train="++trainCurve[epoch].round(0.000001)
        ++" | val="++met[\mae].round(0.000001)
        ++" | rmse="++met[\rmse].round(0.000001)
        ++" | maxTorus="++info[\error].round(0.000001)
        ++" | maxLineaire="++info[\linearError].round(0.000001)
        ++" | sample="++info[\sample]++" | dim="++info[\dimension]
        ++" | torus="++info[\isTorus]++" | outliers="++info[\outliers]
        ++" | delta="++if(prev.isNil,{0.0},{met[\mae]-prev}).round(0.000001)).postln;
    prev=met[\mae];
};
("Meilleure epoque="++bestEpoch++" | MAE="++bestMAE.round(0.000001)).postln;
if(suggestedStop.notNil) { ("Arret anticipe suggere="++suggestedStop).postln }
{ "Aucun arret anticipe suggere dans cette fenetre.".postln };
test.value("validation amelioree",{bestMAE<valCurve[0]});

info=worstCase.value(model,valX,valY,mask,threshold);
"02. PIRE CAS FINAL REEL".postln;
("sample="++info[\sample]++" | dim="++info[\dimension]++" | torus="++info[\isTorus]
    ++" | erreurReelle="++info[\error].round(0.000001)
    ++" | erreurLineaire="++info[\linearError].round(0.000001)).postln;
("Entree="++info[\input]).postln; ("Cible="++info[\target]).postln;
("Prediction="++info[\prediction]).postln;
8.do{|d|("Dimension "++d++" | MAE torus-aware="++info[\dimensionMAE][d].round(0.000001)).postln};

// Chaque configuration est testee sur une instance neuve.
testConfig = { |replay, recall, label|
    var m=HPtransformerRT.new, localMask=m.getParameter(\torusMask);
    var oldReplay=m.getParameter(\replayRate), oldRecall=m.getParameter(\memoryRetrievalGain);
    var aBefore,aAfter,bBefore,bAfter,forget,learnB,score;
    m.setParameter(\replayRate,replay,false); m.setParameter(\memoryRetrievalGain,recall,false);
    10.do{aX.size.do{|i|m.learnPair(aX[i],aY[i])}};
    aBefore=evaluate.value(m,aX,aY,localMask); bBefore=evaluate.value(m,bX,bY,localMask);
    10.do{bX.size.do{|i|m.learnPair(bX[i],bY[i])}};
    aAfter=evaluate.value(m,aX,aY,localMask); bAfter=evaluate.value(m,bX,bY,localMask);
    forget=aAfter[\mae]-aBefore[\mae]; learnB=bBefore[\mae]-bAfter[\mae];
    score=bAfter[\mae]+(forget.max(0.0)*2.0);
    m.setParameter(\replayRate,oldReplay,false); m.setParameter(\memoryRetrievalGain,oldRecall,false);
    (label:label,replay:replay,recall:recall,aBefore:aBefore[\mae],aAfter:aAfter[\mae],
        forgetting:forget,bBefore:bBefore[\mae],bAfter:bAfter[\mae],learningB:learnB,
        ratio:forget/learnB.max(0.000001),score:score)
};

"03. INTERFERENCE TORUS-AWARE".postln;
configs=[[0.04,0.05,"faible"],[0.08,0.10,"nominal"],[0.12,0.10,"modere"],
    [0.16,0.12,"renforce"],[0.20,0.15,"fort"]];
results=Array.newClear(configs.size);
configs.do{|c,i|
    var r=testConfig.value(c[0],c[1],c[2]); results[i]=r;
    ("Config "++i++" "++r[\label]++" | oubli="++r[\forgetting].round(0.000001)
        ++" | gainB="++r[\learningB].round(0.000001)
        ++" | Bfinal="++r[\bAfter].round(0.000001)
        ++" | ratio="++r[\ratio].round(0.001)++" | score="++r[\score].round(0.000001)).postln;
    if(r[\score]<bestScore){bestScore=r[\score];bestConfig=i};
};
("Meilleur compromis="++bestConfig++" "++results[bestConfig][\label]).postln;
test.value("le meilleur reglage apprend B",{results[bestConfig][\learningB]>0});

"04. REPRODUCTIBILITE".postln;
scores=Array.newClear(repeats);
repeats.do{|i|
    var c=configs[bestConfig],r=testConfig.value(c[0],c[1],"repeat"); scores[i]=r[\score];
    ("Essai "++(i+1)++" | oubli="++r[\forgetting].round(0.000001)
        ++" | gainB="++r[\learningB].round(0.000001)++" | score="++r[\score].round(0.000001)).postln;
};
mean=scores.sum/repeats; scores.do{|x|variance=variance+((x-mean)**2)};
std=(variance/repeats).sqrt;

elapsed=Main.elapsedTime-start;
"============================================================".postln;
"RAPPORT FINAL TORUS-AWARE".postln;
("Duree="++elapsed.round(0.01)++" s | PASS="++pass++" | FAIL="++fail).postln;
("torusMask="++mask).postln;
("Meilleure epoque="++bestEpoch++" | MAE="++bestMAE.round(0.000001)).postln;
if(suggestedStop.notNil){("Arret suggere="++suggestedStop++" | warmup="++warmup++" | patience="++patience).postln};
("Pire erreur finale reelle="++info[\error].round(0.000001)
    ++" | erreur lineaire correspondante="++info[\linearError].round(0.000001)).postln;
("Meilleur replay="++results[bestConfig][\replay]++" | rappel="++results[bestConfig][\recall]
    ++" | oubli="++results[bestConfig][\forgetting].round(0.000001)
    ++" | gainB="++results[bestConfig][\learningB].round(0.000001)).postln;
("Reproductibilite score | moyenne="++mean.round(0.000001)++" | ecart-type="++std.round(0.000001)).postln;
if(failures.notEmpty){"DETAIL ECHECS".postln;failures.do{|x,i|((i+1)++". "++x).postln}};
"============================================================".postln;
)
