(
// ============================================================================
// HPtransformerRT V30 - TESTS DE QUALITE D'APPRENTISSAGE
// SuperCollider 3.14 strict - un seul fichier
// ============================================================================
// Objectif : mesurer la qualite de l'apprentissage, et non seulement la
// stabilite technique du moteur.
//
// Mesures :
//   1. MAE, RMSE et erreur maximale avant/apres apprentissage
//   2. comparaison a la reference naive : sortie = entree
//   3. apprentissage sur 48 paires et validation sur 16 paires non apprises
//   4. courbe d'apprentissage sur plusieurs epoques
//   5. erreur par dimension
//   6. adaptation immediate apres un seul learnPair
//   7. interference : apprentissage A, puis B, puis nouvelle mesure de A
//   8. reproductibilite sur plusieurs essais independants
//
// Prerequis : HPtransformerRT V30 doit etre installe et compile.
// Executer tout le bloc, parentheses comprises.
// ============================================================================

var trainCount = 48, validationCount = 16, totalCount = 64;
var epochs = 20, repeats = 3;
var mae, rmse, maxError, evaluateSet, printMetrics;
var makeInput, makeTargetA, makeTargetB;
var trainInputs, trainTargets, validationInputs, validationTargets;
var groupAInputs, groupATargets, groupBInputs, groupBTargets;
var model, prediction, metrics, baselineMetrics;
var initialTrain, initialValidation, finalTrain, finalValidation;
var bestValidationMAE = 999999.0, bestEpoch = 0;
var dimensionErrors, before, after, gain, improved, unchanged, degraded;
var gains, gainMean, gainMin, gainMax;
var aBeforeB, aAfterB, bBefore, bAfter, interference;
var repeatScores, repeatMean, repeatVariance, repeatStd;
var startTime, elapsed, failures = List.new;
var requireValidVector, finite, test, pass = 0, fail = 0;

// ----------------------------------------------------------------------------
// OUTILS NUMERIQUES
// ----------------------------------------------------------------------------
finite = { |value|
    value.isNumber and: { value.isNaN.not }
};

requireValidVector = { |vector, expectedSize = 8|
    var ok;
    ok = vector.notNil and: {
        vector.isArray and: { vector.size == expectedSize }
    };
    if(ok) {
        vector.do { |value|
            if(finite.value(value).not or: {
                value < 0.0 or: { value > 1.0 }
            }) {
                ok = false;
            };
        };
    };
    ok
};

mae = { |predicted, target|
    var sum = 0.0;
    predicted.size.do { |index|
        sum = sum + (predicted[index] - target[index]).abs;
    };
    sum / predicted.size.max(1)
};

rmse = { |predicted, target|
    var sum = 0.0;
    predicted.size.do { |index|
        sum = sum + ((predicted[index] - target[index]) ** 2);
    };
    (sum / predicted.size.max(1)).sqrt
};

maxError = { |predicted, target|
    var maximum = 0.0;
    predicted.size.do { |index|
        maximum = maximum.max((predicted[index] - target[index]).abs);
    };
    maximum
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

// Retourne un Event : mae, rmse, maxError et validCount.
evaluateSet = { |currentModel, inputs, targets|
    var maeSum = 0.0, mseSum = 0.0, maximum = 0.0, validCount = 0;
    inputs.size.do { |index|
        var predicted, target;
        predicted = currentModel.predict(inputs[index]);
        target = targets[index];
        if(requireValidVector.value(predicted)) {
            predicted.size.do { |dimension|
                var error;
                error = (predicted[dimension] - target[dimension]).abs;
                maeSum = maeSum + error;
                mseSum = mseSum + (error ** 2);
                maximum = maximum.max(error);
            };
            validCount = validCount + 1;
        };
    };
    (
        mae: maeSum / (validCount * 8).max(1),
        rmse: (mseSum / (validCount * 8).max(1)).sqrt,
        maxError: maximum,
        validCount: validCount
    )
};

printMetrics = { |label, value|
    (label
        ++ " | MAE=" ++ value[\mae].round(0.000001)
        ++ " | RMSE=" ++ value[\rmse].round(0.000001)
        ++ " | MAX=" ++ value[\maxError].round(0.000001)
        ++ " | valides=" ++ value[\validCount]).postln;
};

// ----------------------------------------------------------------------------
// DONNEES DETERMINISTES
// ----------------------------------------------------------------------------
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

// Relation A : non lineaire mais douce, adaptee aux valeurs [0, 1].
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

// Relation B differente, utilisee pour mesurer l'interference.
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

trainInputs = Array.fill(trainCount, { |index| makeInput.value(index) });
trainTargets = trainInputs.collect { |input| makeTargetA.value(input) };
validationInputs = Array.fill(validationCount, { |index|
    makeInput.value(index + trainCount + 1000)
});
validationTargets = validationInputs.collect { |input| makeTargetA.value(input) };

groupAInputs = Array.fill(32, { |index| makeInput.value(index + 2000) });
groupATargets = groupAInputs.collect { |input| makeTargetA.value(input) };
groupBInputs = Array.fill(32, { |index| makeInput.value(index + 3000) });
groupBTargets = groupBInputs.collect { |input| makeTargetB.value(input) };

startTime = Main.elapsedTime;
"".postln;
"================================================================".postln;
"HPtransformerRT V30 - QUALITE DE L'APPRENTISSAGE".postln;
"================================================================".postln;

// ----------------------------------------------------------------------------
// 01. MODELE ET REFERENCE NAIVE
// ----------------------------------------------------------------------------
"".postln;
"01. ERREUR INITIALE ET REFERENCE IDENTITE".postln;
"----------------------------------------------------------------".postln;
model = HPtransformerRT.new;
initialTrain = evaluateSet.value(model, trainInputs, trainTargets);
initialValidation = evaluateSet.value(model, validationInputs, validationTargets);
printMetrics.value("Modele initial - apprentissage", initialTrain);
printMetrics.value("Modele initial - validation", initialValidation);

// La baseline identite utilise directement l'entree comme prediction.
baselineMetrics = (
    mae: 0.0,
    rmse: 0.0,
    maxError: 0.0,
    validCount: validationCount
);
validationInputs.size.do { |index|
    var input, target;
    input = validationInputs[index];
    target = validationTargets[index];
    baselineMetrics[\mae] = baselineMetrics[\mae] + mae.value(input, target);
    baselineMetrics[\rmse] = baselineMetrics[\rmse]
        + (rmse.value(input, target) ** 2);
    baselineMetrics[\maxError] = baselineMetrics[\maxError]
        .max(maxError.value(input, target));
};
baselineMetrics[\mae] = baselineMetrics[\mae] / validationCount;
baselineMetrics[\rmse] = (baselineMetrics[\rmse] / validationCount).sqrt;
printMetrics.value("Reference identite - validation", baselineMetrics);

// ----------------------------------------------------------------------------
// 02. COURBE D'APPRENTISSAGE
// ----------------------------------------------------------------------------
"".postln;
"02. COURBE D'APPRENTISSAGE".postln;
"----------------------------------------------------------------".postln;
("Epoque 0 | trainMAE=" ++ initialTrain[\mae].round(0.000001)
    ++ " | validationMAE=" ++ initialValidation[\mae].round(0.000001)).postln;

bestValidationMAE = initialValidation[\mae];
bestEpoch = 0;

epochs.do { |epoch|
    // Rotation deterministe de l'ordre, sans dependance au hasard.
    trainCount.do { |step|
        var index;
        index = (step + epoch) % trainCount;
        model.learnPair(trainInputs[index], trainTargets[index]);
    };

    metrics = evaluateSet.value(model, trainInputs, trainTargets);
    finalValidation = evaluateSet.value(model, validationInputs, validationTargets);

    if(finalValidation[\mae] < bestValidationMAE) {
        bestValidationMAE = finalValidation[\mae];
        bestEpoch = epoch + 1;
    };

    ("Epoque " ++ (epoch + 1)
        ++ " | trainMAE=" ++ metrics[\mae].round(0.000001)
        ++ " | validationMAE=" ++ finalValidation[\mae].round(0.000001)
        ++ " | validationRMSE=" ++ finalValidation[\rmse].round(0.000001)).postln;
};

finalTrain = evaluateSet.value(model, trainInputs, trainTargets);
finalValidation = evaluateSet.value(model, validationInputs, validationTargets);

"".postln;
printMetrics.value("Final - apprentissage", finalTrain);
printMetrics.value("Final - validation", finalValidation);
("Meilleure validation MAE=" ++ bestValidationMAE.round(0.000001)
    ++ " a l'epoque " ++ bestEpoch).postln;
("Gain validation absolu="
    ++ (initialValidation[\mae] - finalValidation[\mae]).round(0.000001)).postln;
("Gain validation relatif="
    ++ (((initialValidation[\mae] - finalValidation[\mae])
        / initialValidation[\mae].max(0.000001)) * 100.0).round(0.01)
    ++ " %").postln;
("Gain contre reference identite="
    ++ (((baselineMetrics[\mae] - finalValidation[\mae])
        / baselineMetrics[\mae].max(0.000001)) * 100.0).round(0.01)
    ++ " %").postln;
("Ecart de generalisation="
    ++ (finalValidation[\mae] - finalTrain[\mae]).round(0.000001)).postln;

test.value("l'erreur d'apprentissage diminue", {
    finalTrain[\mae] < initialTrain[\mae]
});
test.value("l'erreur de validation diminue", {
    finalValidation[\mae] < initialValidation[\mae]
});
test.value("le modele final bat la reference identite", {
    finalValidation[\mae] < baselineMetrics[\mae]
});

// ----------------------------------------------------------------------------
// 03. ERREUR PAR DIMENSION
// ----------------------------------------------------------------------------
"".postln;
"03. ERREUR DE VALIDATION PAR DIMENSION".postln;
"----------------------------------------------------------------".postln;
dimensionErrors = Array.fill(8, 0.0);
validationInputs.size.do { |index|
    prediction = model.predict(validationInputs[index]);
    8.do { |dimension|
        dimensionErrors[dimension] = dimensionErrors[dimension]
            + (prediction[dimension] - validationTargets[index][dimension]).abs;
    };
};
8.do { |dimension|
    dimensionErrors[dimension] = dimensionErrors[dimension] / validationCount;
    ("Dimension " ++ dimension ++ " | MAE="
        ++ dimensionErrors[dimension].round(0.000001)).postln;
};
("Meilleure dimension=" ++ dimensionErrors.minIndex
    ++ " | MAE=" ++ dimensionErrors.minItem.round(0.000001)).postln;
("Dimension la plus difficile=" ++ dimensionErrors.maxIndex
    ++ " | MAE=" ++ dimensionErrors.maxItem.round(0.000001)).postln;

// ----------------------------------------------------------------------------
// 04. ADAPTATION IMMEDIATE
// ----------------------------------------------------------------------------
"".postln;
"04. ADAPTATION IMMEDIATE APRES UN learnPair".postln;
"----------------------------------------------------------------".postln;
model = HPtransformerRT.new;
improved = 0;
unchanged = 0;
degraded = 0;
gains = List.new;

trainInputs.size.do { |index|
    before = mae.value(model.predict(trainInputs[index]), trainTargets[index]);
    model.learnPair(trainInputs[index], trainTargets[index]);
    after = mae.value(model.predict(trainInputs[index]), trainTargets[index]);
    gain = before - after;
    gains.add(gain);
    if(gain > 0.000001) {
        improved = improved + 1;
    } {
        if(gain < -0.000001) {
            degraded = degraded + 1;
        } {
            unchanged = unchanged + 1;
        };
    };
};

gainMean = gains.sum / gains.size.max(1);
gainMin = gains.minItem;
gainMax = gains.maxItem;
("Paires ameliorees=" ++ improved ++ "/" ++ trainCount).postln;
("Paires inchangees=" ++ unchanged ++ "/" ++ trainCount).postln;
("Paires degradees=" ++ degraded ++ "/" ++ trainCount).postln;
("Gain MAE moyen=" ++ gainMean.round(0.000001)).postln;
("Gain MAE minimal=" ++ gainMin.round(0.000001)).postln;
("Gain MAE maximal=" ++ gainMax.round(0.000001)).postln;
test.value("gain immediat moyen positif", { gainMean > 0.0 });

// ----------------------------------------------------------------------------
// 05. INTERFERENCE A PUIS B
// ----------------------------------------------------------------------------
"".postln;
"05. INTERFERENCE ET OUBLI".postln;
"----------------------------------------------------------------".postln;
model = HPtransformerRT.new;

// Apprentissage du groupe A.
10.do {
    groupAInputs.size.do { |index|
        model.learnPair(groupAInputs[index], groupATargets[index]);
    };
};
aBeforeB = evaluateSet.value(model, groupAInputs, groupATargets);
bBefore = evaluateSet.value(model, groupBInputs, groupBTargets);

// Apprentissage du groupe B.
10.do {
    groupBInputs.size.do { |index|
        model.learnPair(groupBInputs[index], groupBTargets[index]);
    };
};
aAfterB = evaluateSet.value(model, groupAInputs, groupATargets);
bAfter = evaluateSet.value(model, groupBInputs, groupBTargets);
interference = aAfterB[\mae] - aBeforeB[\mae];

printMetrics.value("Groupe A avant apprentissage B", aBeforeB);
printMetrics.value("Groupe A apres apprentissage B", aAfterB);
printMetrics.value("Groupe B avant apprentissage B", bBefore);
printMetrics.value("Groupe B apres apprentissage B", bAfter);
("Interference absolue sur A=" ++ interference.round(0.000001)).postln;
("Apprentissage absolu de B="
    ++ (bBefore[\mae] - bAfter[\mae]).round(0.000001)).postln;
test.value("le groupe B est appris", { bAfter[\mae] < bBefore[\mae] });

// ----------------------------------------------------------------------------
// 06. REPRODUCTIBILITE
// ----------------------------------------------------------------------------
"".postln;
"06. REPRODUCTIBILITE SUR " ++ repeats ++ " ESSAIS".postln;
"----------------------------------------------------------------".postln;
repeatScores = Array.newClear(repeats);
repeats.do { |repeat|
    var currentModel, score;
    currentModel = HPtransformerRT.new;
    10.do { |epoch|
        trainInputs.size.do { |index|
            currentModel.learnPair(trainInputs[index], trainTargets[index]);
        };
    };
    score = evaluateSet.value(currentModel, validationInputs, validationTargets);
    repeatScores[repeat] = score[\mae];
    ("Essai " ++ (repeat + 1) ++ " | validationMAE="
        ++ score[\mae].round(0.000001)).postln;
};
repeatMean = repeatScores.sum / repeats;
repeatVariance = 0.0;
repeatScores.do { |score|
    repeatVariance = repeatVariance + ((score - repeatMean) ** 2);
};
repeatVariance = repeatVariance / repeats.max(1);
repeatStd = repeatVariance.sqrt;
("MAE moyenne=" ++ repeatMean.round(0.000001)).postln;
("Ecart-type=" ++ repeatStd.round(0.000001)).postln;
("Minimum=" ++ repeatScores.minItem.round(0.000001)).postln;
("Maximum=" ++ repeatScores.maxItem.round(0.000001)).postln;

// ----------------------------------------------------------------------------
// 07. RAPPORT FINAL
// ----------------------------------------------------------------------------
"".postln;
"================================================================".postln;
"07. RAPPORT FINAL".postln;
"================================================================".postln;
elapsed = Main.elapsedTime - startTime;
("Duree totale=" ++ elapsed.round(0.01) ++ " s").postln;
("PASS=" ++ pass ++ " | FAIL=" ++ fail).postln;
("MAE validation initiale=" ++ initialValidation[\mae].round(0.000001)).postln;
("MAE validation finale=" ++ finalValidation[\mae].round(0.000001)).postln;
("MAE reference identite=" ++ baselineMetrics[\mae].round(0.000001)).postln;
("Meilleure epoque=" ++ bestEpoch).postln;
("Ecart generalisation="
    ++ (finalValidation[\mae] - finalTrain[\mae]).round(0.000001)).postln;
("Interference sur A=" ++ interference.round(0.000001)).postln;
("Reproductibilite, ecart-type=" ++ repeatStd.round(0.000001)).postln;

if(failures.notEmpty) {
    "".postln;
    "DETAIL DES ECHECS".postln;
    failures.do { |item, index|
        ((index + 1) ++ ". " ++ item).postln;
    };
};

"".postln;
"INTERPRETATION RAPIDE".postln;
"- Si validation finale < validation initiale : apprentissage utile.".postln;
"- Si validation finale < reference identite : le modele bat la copie.".postln;
"- Si validation >> apprentissage : surapprentissage probable.".postln;
"- Si interference > 0 : oubli du groupe A apres apprentissage de B.".postln;
"- Plus l'ecart-type est faible, plus le resultat est reproductible.".postln;
"================================================================".postln;
)
