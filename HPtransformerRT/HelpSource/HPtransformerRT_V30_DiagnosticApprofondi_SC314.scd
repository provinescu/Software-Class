(
// ============================================================================
// HPtransformerRT V30 - DIAGNOSTIC APPROFONDI DE L'APPRENTISSAGE
// SuperCollider 3.14 strict - fichier autonome
// ============================================================================
// Ce test examine les problemes observes lors du test precedent :
//   1. erreur maximale anormalement elevee ;
//   2. degradation de validation apres la meilleure epoque ;
//   3. dimensions de sortie plus difficiles ;
//   4. interference et oubli lors de l'apprentissage A puis B ;
//   5. influence de replayRate et memoryRetrievalGain ;
//   6. compromis entre apprentissage de B et conservation de A ;
//   7. reproductibilite du meilleur reglage.
//
// Ce fichier n'utilise que l'API publique : new, predict, learnPair,
// isRuntimeParameter, getParameter et setParameter.
// Il restaure les parametres modifies dans chaque instance de test.
// ============================================================================

var trainCount = 48, validationCount = 16, epochs = 25;
var patience = 3, outlierThreshold = 0.25;
var interferenceEpochsA = 10, interferenceEpochsB = 10;
var repeatCount = 3;
var model, finite, validVector, mae, evaluateSet, inspectWorst;
var makeInput, makeTargetA, makeTargetB;
var trainInputs, trainTargets, validationInputs, validationTargets;
var groupAInputs, groupATargets, groupBInputs, groupBTargets;
var epochTrainMAE, epochValidationMAE, epochValidationRMSE;
var epochWorstError, epochWorstSample, epochWorstDimension;
var bestEpoch = 0, bestMAE = 999999.0, noImprovement = 0;
var stopEpoch = nil, previousMAE, jump;
var metrics, worst, dimensionMAE, outlierCount;
var testInterferenceConfig, configurations, configResults;
var bestConfigIndex = 0, bestConfigScore = 999999.0;
var repeatScores, repeatMean, repeatVariance, repeatStd;
var failures = List.new, pass = 0, fail = 0, test;
var startTime, elapsed;

finite = { |value|
    value.isNumber and: { value.isNaN.not }
};

validVector = { |vector, size = 8|
    var ok;
    ok = vector.notNil and: { vector.isArray and: { vector.size == size } };
    if(ok) {
        vector.do { |value|
            if(finite.value(value).not or: {
                value < 0.0 or: { value > 1.0 }
            }) { ok = false };
        };
    };
    ok
};

mae = { |predicted, target|
    var sum = 0.0;
    predicted.size.do { |i|
        sum = sum + (predicted[i] - target[i]).abs;
    };
    sum / predicted.size.max(1)
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

// Retourne MAE, RMSE, erreur maximale et nombre de sorties valides.
evaluateSet = { |currentModel, inputs, targets|
    var absSum = 0.0, squareSum = 0.0, maximum = 0.0;
    var validCount = 0, valueCount = 0;
    inputs.size.do { |sample|
        var predicted, target;
        predicted = currentModel.predict(inputs[sample]);
        target = targets[sample];
        if(validVector.value(predicted)) {
            8.do { |dimension|
                var error;
                error = (predicted[dimension] - target[dimension]).abs;
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

// Localise precisement la pire erreur et compte les valeurs aberrantes.
inspectWorst = { |currentModel, inputs, targets, threshold = 0.25|
    var worstError = -1.0, worstSample = -1, worstDimension = -1;
    var worstInput, worstTarget, worstPrediction;
    var count = 0, perDimension;
    perDimension = Array.fill(8, 0.0);

    inputs.size.do { |sample|
        var predicted, target;
        predicted = currentModel.predict(inputs[sample]);
        target = targets[sample];
        if(validVector.value(predicted)) {
            8.do { |dimension|
                var error;
                error = (predicted[dimension] - target[dimension]).abs;
                perDimension[dimension] = perDimension[dimension] + error;
                if(error > threshold) { count = count + 1 };
                if(error > worstError) {
                    worstError = error;
                    worstSample = sample;
                    worstDimension = dimension;
                    worstInput = inputs[sample].copy;
                    worstTarget = target.copy;
                    worstPrediction = predicted.copy;
                };
            };
        };
    };

    perDimension = perDimension / inputs.size.max(1);
    (
        error: worstError,
        sample: worstSample,
        dimension: worstDimension,
        input: worstInput,
        target: worstTarget,
        prediction: worstPrediction,
        outlierCount: count,
        dimensionMAE: perDimension
    )
};

makeInput = { |index = 0|
    var k = index;
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

makeTargetA = { |input|
    [
        ((input[0] * 0.72) + (input[1] * 0.18) + 0.05).clip(0.0, 1.0),
        ((input[1] * 0.65) + (input[2] * 0.25) + 0.04).clip(0.0, 1.0),
        ((input[2] * 0.74) + ((input[3] - 0.5) * 0.12) + 0.08).clip(0.0, 1.0),
        ((input[3] * 0.68) + (input[4] * 0.22) + 0.03).clip(0.0, 1.0),
        ((input[4] * 0.70) + ((1.0 - input[5]) * 0.20) + 0.02).clip(0.0, 1.0),
        ((input[5] * 0.76) + (input[6] * 0.14) + 0.05).clip(0.0, 1.0),
        ((input[6] * 0.67) + (input[7] * 0.23) + 0.04).clip(0.0, 1.0),
        ((input[7] * 0.73) + (input[0] * 0.17) + 0.05).clip(0.0, 1.0)
    ]
};

makeTargetB = { |input|
    [
        ((input[0] * 0.52) + (input[7] * 0.33) + 0.07).clip(0.0, 1.0),
        ((input[1] * 0.50) + ((1.0 - input[0]) * 0.35) + 0.05).clip(0.0, 1.0),
        ((input[2] * 0.55) + (input[5] * 0.30) + 0.06).clip(0.0, 1.0),
        ((input[3] * 0.48) + (input[6] * 0.37) + 0.05).clip(0.0, 1.0),
        ((input[4] * 0.50) + (input[2] * 0.34) + 0.06).clip(0.0, 1.0),
        ((input[5] * 0.54) + ((1.0 - input[3]) * 0.31) + 0.05).clip(0.0, 1.0),
        ((input[6] * 0.51) + (input[1] * 0.34) + 0.06).clip(0.0, 1.0),
        ((input[7] * 0.53) + (input[4] * 0.32) + 0.06).clip(0.0, 1.0)
    ]
};

trainInputs = Array.fill(trainCount, { |i| makeInput.value(i) });
trainTargets = trainInputs.collect { |x| makeTargetA.value(x) };
validationInputs = Array.fill(validationCount, { |i| makeInput.value(i + 1048) });
validationTargets = validationInputs.collect { |x| makeTargetA.value(x) };
groupAInputs = Array.fill(32, { |i| makeInput.value(i + 2000) });
groupATargets = groupAInputs.collect { |x| makeTargetA.value(x) };
groupBInputs = Array.fill(32, { |i| makeInput.value(i + 3000) });
groupBTargets = groupBInputs.collect { |x| makeTargetB.value(x) };

startTime = Main.elapsedTime;
"".postln;
"================================================================".postln;
"HPtransformerRT V30 - DIAGNOSTIC APPROFONDI".postln;
"================================================================".postln;

// ----------------------------------------------------------------------------
// 01. COURBE, SURAPPRENTISSAGE ET ERREURS EXTREMES
// ----------------------------------------------------------------------------
"".postln;
"01. COURBE, ARRET ANTICIPE ET ERREURS EXTREMES".postln;
"----------------------------------------------------------------".postln;
model = HPtransformerRT.new;
epochTrainMAE = Array.newClear(epochs + 1);
epochValidationMAE = Array.newClear(epochs + 1);
epochValidationRMSE = Array.newClear(epochs + 1);
epochWorstError = Array.newClear(epochs + 1);
epochWorstSample = Array.newClear(epochs + 1);
epochWorstDimension = Array.newClear(epochs + 1);

(epochs + 1).do { |epoch|
    if(epoch > 0) {
        trainCount.do { |step|
            var index;
            index = (step + epoch - 1) % trainCount;
            model.learnPair(trainInputs[index], trainTargets[index]);
        };
    };

    metrics = evaluateSet.value(model, trainInputs, trainTargets);
    epochTrainMAE[epoch] = metrics[\mae];
    metrics = evaluateSet.value(model, validationInputs, validationTargets);
    worst = inspectWorst.value(model, validationInputs, validationTargets, outlierThreshold);

    epochValidationMAE[epoch] = metrics[\mae];
    epochValidationRMSE[epoch] = metrics[\rmse];
    epochWorstError[epoch] = worst[\error];
    epochWorstSample[epoch] = worst[\sample];
    epochWorstDimension[epoch] = worst[\dimension];

    if(metrics[\mae] < bestMAE) {
        bestMAE = metrics[\mae];
        bestEpoch = epoch;
        noImprovement = 0;
    } {
        noImprovement = noImprovement + 1;
        if(noImprovement >= patience and: { stopEpoch.isNil }) {
            stopEpoch = epoch;
        };
    };

    jump = 0.0;
    if(epoch > 0) { jump = metrics[\mae] - previousMAE };
    previousMAE = metrics[\mae];

    ("Epoque " ++ epoch
        ++ " | trainMAE=" ++ epochTrainMAE[epoch].round(0.000001)
        ++ " | valMAE=" ++ metrics[\mae].round(0.000001)
        ++ " | valRMSE=" ++ metrics[\rmse].round(0.000001)
        ++ " | max=" ++ worst[\error].round(0.000001)
        ++ " | sample=" ++ worst[\sample]
        ++ " | dim=" ++ worst[\dimension]
        ++ " | outliers>" ++ outlierThreshold ++ "=" ++ worst[\outlierCount]
        ++ " | deltaMAE=" ++ jump.round(0.000001)).postln;
};

("Meilleure epoque=" ++ bestEpoch ++ " | validationMAE="
    ++ bestMAE.round(0.000001)).postln;
if(stopEpoch.notNil) {
    ("Arret anticipe suggere a l'epoque " ++ stopEpoch
        ++ " avec patience=" ++ patience).postln;
} {
    "Aucun arret anticipe suggere dans la fenetre testee.".postln;
};

test.value("la meilleure validation est meilleure que l'epoque 0", {
    bestMAE < epochValidationMAE[0]
});

// Details exacts du pire cas final.
worst = inspectWorst.value(model, validationInputs, validationTargets, outlierThreshold);
"".postln;
"PIRE CAS A LA DERNIERE EPOQUE".postln;
("Index validation=" ++ worst[\sample]
    ++ " | dimension=" ++ worst[\dimension]
    ++ " | erreur=" ++ worst[\error].round(0.000001)).postln;
("Entree     = " ++ worst[\input]).postln;
("Cible      = " ++ worst[\target]).postln;
("Prediction = " ++ worst[\prediction]).postln;
("Valeurs avec erreur > " ++ outlierThreshold ++ " : "
    ++ worst[\outlierCount]).postln;

// ----------------------------------------------------------------------------
// 02. DIAGNOSTIC PAR DIMENSION
// ----------------------------------------------------------------------------
"".postln;
"02. DIAGNOSTIC PAR DIMENSION A LA DERNIERE EPOQUE".postln;
"----------------------------------------------------------------".postln;
dimensionMAE = worst[\dimensionMAE];
8.do { |dimension|
    ("Dimension " ++ dimension ++ " | MAE="
        ++ dimensionMAE[dimension].round(0.000001)).postln;
};
("Dimension la plus difficile=" ++ dimensionMAE.maxIndex
    ++ " | MAE=" ++ dimensionMAE.maxItem.round(0.000001)).postln;
("Dimension la plus facile=" ++ dimensionMAE.minIndex
    ++ " | MAE=" ++ dimensionMAE.minItem.round(0.000001)).postln;

// ----------------------------------------------------------------------------
// 03. FONCTION DE TEST D'INTERFERENCE PARAMETREE
// ----------------------------------------------------------------------------
testInterferenceConfig = { |replayRate, retrievalGain, label|
    var currentModel, oldReplay, oldGain;
    var aBefore, aAfter, bBefore, bAfter;
    var forgetting, learningB, retentionRatio, score;

    currentModel = HPtransformerRT.new;
    oldReplay = currentModel.getParameter(\replayRate);
    oldGain = currentModel.getParameter(\memoryRetrievalGain);
    currentModel.setParameter(\replayRate, replayRate, false);
    currentModel.setParameter(\memoryRetrievalGain, retrievalGain, false);

    interferenceEpochsA.do {
        groupAInputs.size.do { |i|
            currentModel.learnPair(groupAInputs[i], groupATargets[i]);
        };
    };
    aBefore = evaluateSet.value(currentModel, groupAInputs, groupATargets);
    bBefore = evaluateSet.value(currentModel, groupBInputs, groupBTargets);

    interferenceEpochsB.do {
        groupBInputs.size.do { |i|
            currentModel.learnPair(groupBInputs[i], groupBTargets[i]);
        };
    };
    aAfter = evaluateSet.value(currentModel, groupAInputs, groupATargets);
    bAfter = evaluateSet.value(currentModel, groupBInputs, groupBTargets);

    forgetting = aAfter[\mae] - aBefore[\mae];
    learningB = bBefore[\mae] - bAfter[\mae];
    retentionRatio = forgetting / learningB.max(0.000001);
    // Score faible = bon compromis. L'oubli est pondere deux fois.
    score = bAfter[\mae] + (forgetting.max(0.0) * 2.0);

    currentModel.setParameter(\replayRate, oldReplay, false);
    currentModel.setParameter(\memoryRetrievalGain, oldGain, false);

    (
        label: label,
        replayRate: replayRate,
        retrievalGain: retrievalGain,
        aBefore: aBefore[\mae],
        aAfter: aAfter[\mae],
        bBefore: bBefore[\mae],
        bAfter: bAfter[\mae],
        forgetting: forgetting,
        learningB: learningB,
        retentionRatio: retentionRatio,
        score: score
    )
};

// ----------------------------------------------------------------------------
// 04. BALAYAGE REPLAY / MEMOIRE
// ----------------------------------------------------------------------------
"".postln;
"03. BALAYAGE DE L'INTERFERENCE".postln;
"----------------------------------------------------------------".postln;
configurations = [
    [0.04, 0.05, "replay faible / rappel faible"],
    [0.08, 0.10, "reglage nominal"],
    [0.12, 0.10, "replay modere"],
    [0.16, 0.12, "replay renforce"],
    [0.20, 0.15, "replay et rappel renforces"]
];
configResults = Array.newClear(configurations.size);

configurations.do { |configuration, index|
    var result;
    result = testInterferenceConfig.value(
        configuration[0], configuration[1], configuration[2]
    );
    configResults[index] = result;
    ("Config " ++ index ++ " | " ++ result[\label]
        ++ " | replay=" ++ result[\replayRate]
        ++ " | rappel=" ++ result[\retrievalGain]
        ++ " | Aavant=" ++ result[\aBefore].round(0.000001)
        ++ " | Aapres=" ++ result[\aAfter].round(0.000001)
        ++ " | oubli=" ++ result[\forgetting].round(0.000001)
        ++ " | gainB=" ++ result[\learningB].round(0.000001)
        ++ " | Bfinal=" ++ result[\bAfter].round(0.000001)
        ++ " | ratio=" ++ result[\retentionRatio].round(0.001)
        ++ " | score=" ++ result[\score].round(0.000001)).postln;

    if(result[\score] < bestConfigScore) {
        bestConfigScore = result[\score];
        bestConfigIndex = index;
    };
};

("Meilleur compromis=" ++ bestConfigIndex ++ " | "
    ++ configResults[bestConfigIndex][\label]
    ++ " | score=" ++ bestConfigScore.round(0.000001)).postln;

test.value("la meilleure configuration apprend B", {
    configResults[bestConfigIndex][\learningB] > 0.0
});

// ----------------------------------------------------------------------------
// 05. REPRODUCTIBILITE DU MEILLEUR COMPROMIS
// ----------------------------------------------------------------------------
"".postln;
"04. REPRODUCTIBILITE DU MEILLEUR COMPROMIS".postln;
"----------------------------------------------------------------".postln;
repeatScores = Array.newClear(repeatCount);
repeatCount.do { |repeat|
    var configuration, result;
    configuration = configurations[bestConfigIndex];
    result = testInterferenceConfig.value(
        configuration[0], configuration[1],
        "repetition " ++ (repeat + 1)
    );
    repeatScores[repeat] = result[\score];
    ("Repetition " ++ (repeat + 1)
        ++ " | oubli=" ++ result[\forgetting].round(0.000001)
        ++ " | gainB=" ++ result[\learningB].round(0.000001)
        ++ " | score=" ++ result[\score].round(0.000001)).postln;
};
repeatMean = repeatScores.sum / repeatCount;
repeatVariance = 0.0;
repeatScores.do { |value|
    repeatVariance = repeatVariance + ((value - repeatMean) ** 2);
};
repeatVariance = repeatVariance / repeatCount.max(1);
repeatStd = repeatVariance.sqrt;
("Score moyen=" ++ repeatMean.round(0.000001)
    ++ " | ecart-type=" ++ repeatStd.round(0.000001)).postln;

// ----------------------------------------------------------------------------
// 06. RAPPORT FINAL
// ----------------------------------------------------------------------------
"".postln;
"================================================================".postln;
"05. RAPPORT FINAL".postln;
"================================================================".postln;
elapsed = Main.elapsedTime - startTime;
("Duree totale=" ++ elapsed.round(0.01) ++ " s").postln;
("PASS=" ++ pass ++ " | FAIL=" ++ fail).postln;
("Meilleure epoque=" ++ bestEpoch
    ++ " | validationMAE=" ++ bestMAE.round(0.000001)).postln;
if(stopEpoch.notNil) {
    ("Arret anticipe suggere=" ++ stopEpoch).postln;
};
("Pire erreur finale=" ++ worst[\error].round(0.000001)
    ++ " | sample=" ++ worst[\sample]
    ++ " | dimension=" ++ worst[\dimension]).postln;
("Nombre final d'erreurs > " ++ outlierThreshold ++ "="
    ++ worst[\outlierCount]).postln;
("Dimension finale la plus difficile=" ++ dimensionMAE.maxIndex
    ++ " | MAE=" ++ dimensionMAE.maxItem.round(0.000001)).postln;
("Meilleur reglage interference="
    ++ configResults[bestConfigIndex][\label]).postln;
("  replayRate=" ++ configResults[bestConfigIndex][\replayRate]
    ++ " | memoryRetrievalGain="
    ++ configResults[bestConfigIndex][\retrievalGain]).postln;
("  oubli A=" ++ configResults[bestConfigIndex][\forgetting].round(0.000001)
    ++ " | gain B=" ++ configResults[bestConfigIndex][\learningB].round(0.000001)
    ++ " | B final=" ++ configResults[bestConfigIndex][\bAfter].round(0.000001)).postln;
("Reproductibilite du score, ecart-type=" ++ repeatStd.round(0.000001)).postln;

if(failures.notEmpty) {
    "".postln;
    "DETAIL DES ECHECS".postln;
    failures.do { |item, index|
        ((index + 1) ++ ". " ++ item).postln;
    };
};

"".postln;
"LECTURE RAPIDE".postln;
"- Consultez sample/dim pour localiser toute erreur extreme.".postln;
"- L'arret anticipe suggere limite le surapprentissage.".postln;
"- Un oubli A faible et un gain B positif indiquent un bon compromis.".postln;
"- Le score combine B final et deux fois l'oubli positif de A.".postln;
"- Les reglages sont testes sur des instances neuves et ne modifient pas votre modele actif.".postln;
"================================================================".postln;
)
