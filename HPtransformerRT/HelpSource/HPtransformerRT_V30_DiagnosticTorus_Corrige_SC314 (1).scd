(
// ============================================================================
// HPtransformerRT V30 - DIAGNOSTIC D'APPRENTISSAGE TORUS-AWARE CORRIGE
// SuperCollider 3.14 strict - fichier unique
// ============================================================================
// Corrections :
// - priorite arithmetique explicite dans toutes les cibles ;
// - clip applique a chaque composante ;
// - verification automatique des donnees avant apprentissage ;
// - distance circulaire selon torusMask ;
// - affichage des erreurs circulaire et lineaire ;
// - arret anticipe avec warmup et patience ;
// - interference A/B et balayage replayRate / memoryRetrievalGain.

var trainN = 48, valN = 16, epochs = 50;
var warmup = 8, patience = 6, minGain = 0.0001;
var outlierThreshold = 0.25, repeats = 3;
var model, torusMask, finite, validVector, componentError;
var evaluateSet, inspectWorst, makeInput, targetA, targetB;
var trainX, trainY, valX, valY, groupAX, groupAY, groupBX, groupBY;
var bestMAE = 999999.0, bestEpoch = 0, stale = 0, suggestedStop;
var previousMAE, trainMetrics, valMetrics, worst;
var testConfig, configs, results, bestConfig = 0, bestScore = 999999.0;
var scores, scoreMean, scoreVariance = 0.0, scoreStd;
var test, pass = 0, fail = 0, failures = List.new;
var expectedA00, expectedB00, startTime, elapsed;

finite = { |value|
    value.isNumber and: { value.isNaN.not }
};

validVector = { |vector|
    var ok;
    ok = vector.notNil and: { vector.isArray and: { vector.size == 8 } };
    if(ok) {
        vector.do { |value|
            if(finite.value(value).not or: {
                value < 0.0 or: { value > 1.0 }
            }) { ok = false };
        };
    };
    ok
};

componentError = { |prediction, target, isTorus = false|
    var linearError;
    linearError = (prediction - target).abs;
    if(isTorus == true or: { isTorus == 1 }) {
        linearError.min(1.0 - linearError)
    } {
        linearError
    }
};

test = { |name, function|
    var answer;
    answer = function.try { |error|
        failures.add(name ++ " :: " ++ error.asString);
        false
    };
    if(answer == true) {
        pass = pass + 1;
        ("PASS | " ++ name).postln;
    } {
        fail = fail + 1;
        if(answer == false) {
            failures.add(name ++ " :: condition non satisfaite");
        };
        ("FAIL | " ++ name).postln;
    };
    answer
};

evaluateSet = { |currentModel, inputs, targets, mask|
    var absSum = 0.0, squareSum = 0.0, maximum = 0.0;
    var valueCount = 0, validCount = 0;
    inputs.size.do { |sample|
        var prediction;
        prediction = currentModel.predict(inputs[sample]);
        if(validVector.value(prediction)) {
            8.do { |dimension|
                var error;
                error = componentError.value(
                    prediction[dimension],
                    targets[sample][dimension],
                    mask[dimension]
                );
                absSum = absSum + error;
                squareSum = squareSum + (error ** 2);
                maximum = maximum.max(error);
                valueCount = valueCount + 1;
            };
            validCount = validCount + 1;
        };
    };
    (
        mae: absSum / valueCount.max(1),
        rmse: (squareSum / valueCount.max(1)).sqrt,
        maxError: maximum,
        validCount: validCount
    )
};

inspectWorst = { |currentModel, inputs, targets, mask, threshold = 0.25|
    var maximum = -1.0, sampleIndex = -1, dimensionIndex = -1;
    var selectedPrediction, selectedInput, selectedTarget;
    var selectedLinearError = 0.0, outlierCount = 0;
    var dimensionMAE = Array.fill(8, 0.0);

    inputs.size.do { |sample|
        var prediction;
        prediction = currentModel.predict(inputs[sample]);
        if(validVector.value(prediction)) {
            8.do { |dimension|
                var linearError, realError;
                linearError = (
                    prediction[dimension] - targets[sample][dimension]
                ).abs;
                realError = componentError.value(
                    prediction[dimension],
                    targets[sample][dimension],
                    mask[dimension]
                );
                dimensionMAE[dimension] = dimensionMAE[dimension] + realError;
                if(realError > threshold) { outlierCount = outlierCount + 1 };
                if(realError > maximum) {
                    maximum = realError;
                    sampleIndex = sample;
                    dimensionIndex = dimension;
                    selectedPrediction = prediction.copy;
                    selectedInput = inputs[sample].copy;
                    selectedTarget = targets[sample].copy;
                    selectedLinearError = linearError;
                };
            };
        };
    };

    8.do { |dimension|
        dimensionMAE[dimension] = dimensionMAE[dimension] / inputs.size.max(1);
    };

    (
        error: maximum,
        linearError: selectedLinearError,
        sample: sampleIndex,
        dimension: dimensionIndex,
        isTorus: mask[dimensionIndex],
        input: selectedInput,
        target: selectedTarget,
        prediction: selectedPrediction,
        outliers: outlierCount,
        dimensionMAE: dimensionMAE
    )
};

makeInput = { |index = 0|
    var k;
    k = index;
    [
        (0.07 + (k * 0.037)).wrap(0.0, 1.0),
        (0.19 + (k * 0.053)).wrap(0.0, 1.0),
        (0.41 + ((k % 11) * 0.047)).clip(0.0, 1.0),
        (0.13 + (k * 0.071)).wrap(0.0, 1.0),
        (0.91 - ((k % 23) * 0.031)).clip(0.0, 1.0),
        (0.31 + (k * 0.061)).wrap(0.0, 1.0),
        (0.03 + (k * 0.089)).wrap(0.0, 1.0),
        (0.67 + (k * 0.043)).wrap(0.0, 1.0)
    ]
};

targetA = { |x|
    [
        ((x[0] * 0.72) + (x[1] * 0.18) + 0.05).clip(0.0, 1.0),
        ((x[1] * 0.65) + (x[2] * 0.25) + 0.04).clip(0.0, 1.0),
        ((x[2] * 0.74) + ((x[3] - 0.5) * 0.12) + 0.08).clip(0.0, 1.0),
        ((x[3] * 0.68) + (x[4] * 0.22) + 0.03).clip(0.0, 1.0),
        ((x[4] * 0.70) + ((1.0 - x[5]) * 0.20) + 0.02).clip(0.0, 1.0),
        ((x[5] * 0.76) + (x[6] * 0.14) + 0.05).clip(0.0, 1.0),
        ((x[6] * 0.67) + (x[7] * 0.23) + 0.04).clip(0.0, 1.0),
        ((x[7] * 0.73) + (x[0] * 0.17) + 0.05).clip(0.0, 1.0)
    ]
};

targetB = { |x|
    [
        ((x[0] * 0.52) + (x[7] * 0.33) + 0.07).clip(0.0, 1.0),
        ((x[1] * 0.50) + ((1.0 - x[0]) * 0.35) + 0.05).clip(0.0, 1.0),
        ((x[2] * 0.55) + (x[5] * 0.30) + 0.06).clip(0.0, 1.0),
        ((x[3] * 0.48) + (x[6] * 0.37) + 0.05).clip(0.0, 1.0),
        ((x[4] * 0.50) + (x[2] * 0.34) + 0.06).clip(0.0, 1.0),
        ((x[5] * 0.54) + ((1.0 - x[3]) * 0.31) + 0.05).clip(0.0, 1.0),
        ((x[6] * 0.51) + (x[1] * 0.34) + 0.06).clip(0.0, 1.0),
        ((x[7] * 0.53) + (x[4] * 0.32) + 0.06).clip(0.0, 1.0)
    ]
};

trainX = Array.fill(trainN, { |index| makeInput.value(index) });
trainY = trainX.collect { |input| targetA.value(input) };
valX = Array.fill(valN, { |index| makeInput.value(index + 1048) });
valY = valX.collect { |input| targetA.value(input) };
groupAX = Array.fill(32, { |index| makeInput.value(index + 2000) });
groupAY = groupAX.collect { |input| targetA.value(input) };
groupBX = Array.fill(32, { |index| makeInput.value(index + 3000) });
groupBY = groupBX.collect { |input| targetB.value(input) };

startTime = Main.elapsedTime;
"============================================================".postln;
"HPtransformerRT V30 - DIAGNOSTIC TORUS-AWARE CORRIGE".postln;
"============================================================".postln;

// Verification independante de la premiere composante de chaque relation.
expectedA00 = (
    (trainX[0][0] * 0.72) + (trainX[0][1] * 0.18) + 0.05
).clip(0.0, 1.0);
expectedB00 = (
    (groupBX[0][0] * 0.52) + (groupBX[0][7] * 0.33) + 0.07
).clip(0.0, 1.0);

"00. CONTROLE DES DONNEES".postln;
("Entree A[0]=" ++ trainX[0]).postln;
("Cible A[0]=" ++ trainY[0]).postln;
("A[0][0] attendue=" ++ expectedA00
    ++ " | obtenue=" ++ trainY[0][0]).postln;
("B[0][0] attendue=" ++ expectedB00
    ++ " | obtenue=" ++ groupBY[0][0]).postln;
test.value("cible A correctement calculee", {
    (trainY[0][0] - expectedA00).abs < 0.000001
});
test.value("cible B correctement calculee", {
    (groupBY[0][0] - expectedB00).abs < 0.000001
});
test.value("toutes les cibles A sont valides", {
    var ok = true;
    trainY.do { |target| if(validVector.value(target).not) { ok = false } };
    valY.do { |target| if(validVector.value(target).not) { ok = false } };
    ok
});
test.value("toutes les cibles B sont valides", {
    var ok = true;
    groupBY.do { |target| if(validVector.value(target).not) { ok = false } };
    ok
});

model = HPtransformerRT.new;
torusMask = model.getParameter(\torusMask);
("torusMask utilise=" ++ torusMask).postln;
test.value("torusMask contient 8 valeurs", {
    torusMask.notNil and: { torusMask.size == 8 }
});

"01. COURBE D'APPRENTISSAGE".postln;
(epochs + 1).do { |epoch|
    if(epoch > 0) {
        trainN.do { |step|
            var index;
            index = (step + epoch - 1) % trainN;
            model.learnPair(trainX[index], trainY[index]);
        };
    };

    trainMetrics = evaluateSet.value(model, trainX, trainY, torusMask);
    valMetrics = evaluateSet.value(model, valX, valY, torusMask);
    worst = inspectWorst.value(
        model, valX, valY, torusMask, outlierThreshold
    );

    if(valMetrics[\mae] < (bestMAE - minGain)) {
        bestMAE = valMetrics[\mae];
        bestEpoch = epoch;
        stale = 0;
    } {
        if(epoch >= warmup) { stale = stale + 1 };
    };
    if(epoch == warmup) { stale = 0 };
    if((epoch >= warmup) and: {
        stale >= patience and: { suggestedStop.isNil }
    }) { suggestedStop = epoch };

    ("Epoque " ++ epoch
        ++ " | train=" ++ trainMetrics[\mae].round(0.000001)
        ++ " | val=" ++ valMetrics[\mae].round(0.000001)
        ++ " | rmse=" ++ valMetrics[\rmse].round(0.000001)
        ++ " | maxReel=" ++ worst[\error].round(0.000001)
        ++ " | maxLineaire=" ++ worst[\linearError].round(0.000001)
        ++ " | sample=" ++ worst[\sample]
        ++ " | dim=" ++ worst[\dimension]
        ++ " | torus=" ++ worst[\isTorus]
        ++ " | outliers=" ++ worst[\outliers]
        ++ " | delta=" ++ if(previousMAE.isNil) {
            0.0
        } {
            valMetrics[\mae] - previousMAE
        }.round(0.000001)).postln;
    previousMAE = valMetrics[\mae];
};

("Meilleure epoque=" ++ bestEpoch
    ++ " | MAE=" ++ bestMAE.round(0.000001)).postln;
if(suggestedStop.notNil) {
    ("Arret anticipe suggere=" ++ suggestedStop
        ++ " | warmup=" ++ warmup
        ++ " | patience=" ++ patience).postln;
} {
    "Aucun arret anticipe suggere dans cette fenetre.".postln;
};
test.value("validation amelioree", {
    bestMAE < evaluateSet.value(
        HPtransformerRT.new, valX, valY, torusMask
    )[\mae]
});

"02. PIRE CAS FINAL ET DIMENSIONS".postln;
worst = inspectWorst.value(model, valX, valY, torusMask, outlierThreshold);
("sample=" ++ worst[\sample]
    ++ " | dim=" ++ worst[\dimension]
    ++ " | torus=" ++ worst[\isTorus]
    ++ " | erreurReelle=" ++ worst[\error].round(0.000001)
    ++ " | erreurLineaire=" ++ worst[\linearError].round(0.000001)).postln;
("Entree=" ++ worst[\input]).postln;
("Cible=" ++ worst[\target]).postln;
("Prediction=" ++ worst[\prediction]).postln;
8.do { |dimension|
    ("Dimension " ++ dimension ++ " | MAE torus-aware="
        ++ worst[\dimensionMAE][dimension].round(0.000001)).postln;
};

// Retourne les mesures d'interference pour une configuration runtime.
testConfig = { |replayRate, retrievalGain, label|
    var currentModel, currentMask, originalReplay, originalGain;
    var aBefore, aAfter, bBefore, bAfter, forgetting, learningB, score;

    currentModel = HPtransformerRT.new;
    currentMask = currentModel.getParameter(\torusMask);
    originalReplay = currentModel.getParameter(\replayRate);
    originalGain = currentModel.getParameter(\memoryRetrievalGain);
    currentModel.setParameter(\replayRate, replayRate, false);
    currentModel.setParameter(\memoryRetrievalGain, retrievalGain, false);

    10.do {
        groupAX.size.do { |index|
            currentModel.learnPair(groupAX[index], groupAY[index]);
        };
    };
    aBefore = evaluateSet.value(currentModel, groupAX, groupAY, currentMask);
    bBefore = evaluateSet.value(currentModel, groupBX, groupBY, currentMask);

    10.do {
        groupBX.size.do { |index|
            currentModel.learnPair(groupBX[index], groupBY[index]);
        };
    };
    aAfter = evaluateSet.value(currentModel, groupAX, groupAY, currentMask);
    bAfter = evaluateSet.value(currentModel, groupBX, groupBY, currentMask);

    forgetting = aAfter[\mae] - aBefore[\mae];
    learningB = bBefore[\mae] - bAfter[\mae];
    score = bAfter[\mae] + (forgetting.max(0.0) * 2.0);

    currentModel.setParameter(\replayRate, originalReplay, false);
    currentModel.setParameter(\memoryRetrievalGain, originalGain, false);

    (
        label: label,
        replay: replayRate,
        recall: retrievalGain,
        aBefore: aBefore[\mae],
        aAfter: aAfter[\mae],
        forgetting: forgetting,
        bBefore: bBefore[\mae],
        bAfter: bAfter[\mae],
        learningB: learningB,
        ratio: forgetting / learningB.max(0.000001),
        score: score
    )
};

"03. INTERFERENCE TORUS-AWARE".postln;
configs = [
    [0.04, 0.05, "faible"],
    [0.08, 0.10, "nominal"],
    [0.12, 0.10, "modere"],
    [0.16, 0.12, "renforce"],
    [0.20, 0.15, "fort"]
];
results = Array.newClear(configs.size);
configs.do { |configuration, index|
    var result;
    result = testConfig.value(
        configuration[0], configuration[1], configuration[2]
    );
    results[index] = result;
    ("Config " ++ index ++ " " ++ result[\label]
        ++ " | oubli=" ++ result[\forgetting].round(0.000001)
        ++ " | gainB=" ++ result[\learningB].round(0.000001)
        ++ " | Bfinal=" ++ result[\bAfter].round(0.000001)
        ++ " | ratio=" ++ result[\ratio].round(0.001)
        ++ " | score=" ++ result[\score].round(0.000001)).postln;
    if(result[\score] < bestScore) {
        bestScore = result[\score];
        bestConfig = index;
    };
};
("Meilleur compromis=" ++ bestConfig
    ++ " " ++ results[bestConfig][\label]).postln;
test.value("le meilleur reglage apprend B", {
    results[bestConfig][\learningB] > 0.0
});

"04. REPRODUCTIBILITE".postln;
scores = Array.newClear(repeats);
repeats.do { |index|
    var configuration, result;
    configuration = configs[bestConfig];
    result = testConfig.value(
        configuration[0], configuration[1], "repetition"
    );
    scores[index] = result[\score];
    ("Essai " ++ (index + 1)
        ++ " | oubli=" ++ result[\forgetting].round(0.000001)
        ++ " | gainB=" ++ result[\learningB].round(0.000001)
        ++ " | score=" ++ result[\score].round(0.000001)).postln;
};
scoreMean = scores.sum / repeats;
scores.do { |score|
    scoreVariance = scoreVariance + ((score - scoreMean) ** 2);
};
scoreStd = (scoreVariance / repeats).sqrt;

elapsed = Main.elapsedTime - startTime;
"============================================================".postln;
"RAPPORT FINAL TORUS-AWARE CORRIGE".postln;
("Duree=" ++ elapsed.round(0.01) ++ " s"
    ++ " | PASS=" ++ pass ++ " | FAIL=" ++ fail).postln;
("torusMask=" ++ torusMask).postln;
("Meilleure epoque=" ++ bestEpoch
    ++ " | MAE=" ++ bestMAE.round(0.000001)).postln;
("Pire erreur finale reelle=" ++ worst[\error].round(0.000001)
    ++ " | erreur lineaire=" ++ worst[\linearError].round(0.000001)).postln;
("Meilleur replay=" ++ results[bestConfig][\replay]
    ++ " | rappel=" ++ results[bestConfig][\recall]
    ++ " | oubli=" ++ results[bestConfig][\forgetting].round(0.000001)
    ++ " | gainB=" ++ results[bestConfig][\learningB].round(0.000001)).postln;
("Reproductibilite | moyenne=" ++ scoreMean.round(0.000001)
    ++ " | ecart-type=" ++ scoreStd.round(0.000001)).postln;
if(failures.notEmpty) {
    "DETAIL DES ECHECS".postln;
    failures.do { |item, index| ((index + 1) ++ ". " ++ item).postln };
};
"============================================================".postln;
)
