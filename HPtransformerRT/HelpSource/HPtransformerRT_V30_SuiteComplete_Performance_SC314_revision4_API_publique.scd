(
// ============================================================================
// HPtransformerRT V30 - SUITE DE TEST AUTONOME SC 3.14 REVISION 4 API PUBLIQUE
// SuperCollider 3.14 strict
// ============================================================================
// Prerequis :
//   1. La classe HPtransformerRT doit etre installee et compilee.
//   2. Recompiler la bibliotheque de classes avant le test.
//   3. Executer la totalite de ce bloc, parentheses comprises.
//
// Ce fichier teste :
//   - construction et API fondamentale ;
//   - dimensions et parametres structurels ;
//   - prediction et valeurs limites ;
//   - apprentissage par paire et par evenement ;
//   - memoire, generalisation et interference ;
//   - generation simple et autoregressive ;
//   - diversite de trajectoire ;
//   - modification/restauration des parametres runtime ;
//   - robustesse aux entrees incorrectes ;
//   - stabilite apres apprentissage prolonge ;
//   - mesure simple du temps de prediction et de generation.
//
// IMPORTANT : les tests de parametres restaurent les valeurs d'origine.
// ============================================================================

var model;
var total = 0, passed = 0, failed = 0, skipped = 0;
var failures = List.new, warnings = List.new;
var test, skipTest, section, safeCall;
var finite, validVector, distance, makeInput, makeTarget;
var structuralKeys, runtimeKeys, originalRuntime;
var prediction, generated, sequence;
var memoryResult, generalizationResult, interferenceResult;
var startTime, elapsed, benchmarkStart, benchmarkCount = 200;
var perfRuns = 20, perfBatchSize = 100, perfTimes;
var perfMean, perfMin, perfMax, perfThroughput;
var minDistance, meanDistance, maxDistance, changedCount;
var longErrors = 0, allValid, result;

// ----------------------------------------------------------------------------
// OUTILS DE RAPPORT
// ----------------------------------------------------------------------------
section = { |title|
    "".postln;
    "================================================================".postln;
    title.postln;
    "================================================================".postln;
};

test = { |name, function|
    var answer;
    total = total + 1;
    answer = function.try { |error|
        failures.add(name ++ " :: " ++ error.asString);
        false
    };

    if(answer == true) {
        passed = passed + 1;
        ("PASS | " ++ name).postln;
    } {
        failed = failed + 1;
        if(answer != false) {
            failures.add(name ++ " :: le test n'a pas retourne true");
        };
        ("FAIL | " ++ name).postln;
    };
    answer
};

skipTest = { |name, reason = "indisponible"|
    total = total + 1;
    skipped = skipped + 1;
    ("SKIP | " ++ name ++ " | " ++ reason).postln;
    nil
};

safeCall = { |function, label = "appel"|
    function.try { |error|
        warnings.add(label ++ " :: " ++ error.asString);
        nil
    }
};

// ----------------------------------------------------------------------------
// VALIDATION DES DONNEES
// ----------------------------------------------------------------------------
finite = { |value|
    // SC 3.14 : Float ne repond pas a isInfinite.
    // NaN est exclu ici. Les vecteurs sont ensuite controles dans [0, 1],
    // ce qui exclut egalement les valeurs infinies.
    value.isNumber and: { value.isNaN.not }
};

validVector = { |vector, expectedSize = 8|
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

distance = { |a, b|
    var size, sum = 0.0;
    size = a.size.min(b.size);
    size.do { |index|
        sum = sum + (a[index] - b[index]).abs;
    };
    sum / size.max(1)
};

makeInput = { |index = 0|
    var k;
    k = index % 32;
    [
        (0.07 + (k * 0.031)).wrap(0.0, 1.0),
        (0.19 + (k * 0.047)).wrap(0.0, 1.0),
        (0.41 + ((k % 7) * 0.071)).clip(0.0, 1.0),
        (0.13 + (k * 0.053)).wrap(0.0, 1.0),
        (0.91 - (k * 0.029)).clip(0.0, 1.0),
        (0.31 + (k * 0.061)).wrap(0.0, 1.0),
        (0.03 + (k * 0.089)).wrap(0.0, 1.0),
        (0.67 + (k * 0.037)).wrap(0.0, 1.0)
    ]
};

makeTarget = { |index = 0|
    var x;
    x = makeInput.value(index);
    [
        (x[0] + 0.035).wrap(0.0, 1.0),
        (x[1] - 0.025).wrap(0.0, 1.0),
        (x[2] + 0.020).clip(0.0, 1.0),
        (x[3] + 0.040).wrap(0.0, 1.0),
        (x[4] - 0.030).wrap(0.0, 1.0),
        (x[5] + 0.015).clip(0.0, 1.0),
        (x[6] + 0.025).wrap(0.0, 1.0),
        (x[7] - 0.020).wrap(0.0, 1.0)
    ]
};

startTime = Main.elapsedTime;
section.value("HPtransformerRT V30 - DEMARRAGE DES TESTS");

// ----------------------------------------------------------------------------
// 01. CONSTRUCTION
// ----------------------------------------------------------------------------
section.value("01. CONSTRUCTION");
test.value("HPtransformerRT.new construit une instance", {
    model = HPtransformerRT.new;
    model.notNil
});

if(model.isNil) {
    "ARRET : impossible de poursuivre sans instance du modele.".postln;
} {
    // ------------------------------------------------------------------------
    // 02. STRUCTURE
    // ------------------------------------------------------------------------
    section.value("02. STRUCTURE DU MODELE");
    structuralKeys = [
        [\inputSize, 8],
        [\outputSize, 8],
        [\hiddenSize, 32],
        [\numHeads, 4],
        [\headSize, 8],
        [\numExperts, 4]
    ];

    structuralKeys.do { |pair|
        test.value(pair[0].asString ++ " = " ++ pair[1].asString, {
            model.getParameter(pair[0]) == pair[1]
        });
    };

    // ------------------------------------------------------------------------
    // 03. API RUNTIME
    // ------------------------------------------------------------------------
    section.value("03. PARAMETRES RUNTIME");
    runtimeKeys = [
        \learningRate, \temperature, \deltaScale, \gradientClip,
        \memoryRetrievalGain, \replayRate, \trajectoryExplorationGain,
        \diversityRadius, \diversityRepulsionGain, \diversityNoiseGain,
        \diversityMaxCorrection, \localDiversityFloor,
        \localDiversityGain, \localDiversityMaxCorrection,
        \generationWindowSize, \torusMask
    ];

    test.value("runtimeParameterNames retourne une collection", {
        model.runtimeParameterNames.notNil and: {
            model.runtimeParameterNames.size > 0
        }
    });

    runtimeKeys.do { |parameter|
        test.value("parametre runtime " ++ parameter.asString, {
            model.isRuntimeParameter(parameter) == true
        });
    };

    // ------------------------------------------------------------------------
    // 04. PREDICTION
    // ------------------------------------------------------------------------
    section.value("04. PREDICTION");
    prediction = safeCall.value({ model.predict(makeInput.value(0)) }, "predict initial");
    test.value("predict retourne un vecteur 8D valide", {
        validVector.value(prediction)
    });

    [0.0, 0.001, 0.25, 0.5, 0.75, 0.999, 1.0].do { |constant|
        test.value("predict entree constante " ++ constant.asString, {
            validVector.value(model.predict(Array.fill(8, constant)))
        });
    };

    32.do { |index|
        test.value("predict entree deterministe " ++ index.asString, {
            validVector.value(model.predict(makeInput.value(index)))
        });
    };

    // ------------------------------------------------------------------------
    // 05. APPRENTISSAGE
    // ------------------------------------------------------------------------
    section.value("05. APPRENTISSAGE");
    test.value("learnPair accepte une paire", {
        model.learnPair(makeInput.value(1), makeTarget.value(1)).notNil
    });

    16.do { |index|
        test.value("learnEvent " ++ index.asString, {
            model.learnEvent(makeTarget.value(index + 2)).notNil
        });
    };

    // Ces statistiques existent comme variables internes, mais ne font pas
    // partie de l'API publique de cette version de HPtransformerRT.
    skipTest.value("learnedEvents", "statistique interne sans accesseur public");
    skipTest.value("loss", "statistique interne sans accesseur public");
    skipTest.value("surprise", "statistique interne sans accesseur public");
    skipTest.value("entropy", "statistique interne sans accesseur public");

    // ------------------------------------------------------------------------
    // 06. MEMOIRE, GENERALISATION, INTERFERENCE
    // ------------------------------------------------------------------------
    section.value("06. MEMOIRE, GENERALISATION, INTERFERENCE");
    memoryResult = safeCall.value(
        { model.memoryTest(makeInput.value(20)) },
        "memoryTest"
    );
    test.value("memoryTest retourne un resultat", { memoryResult.notNil });

    generalizationResult = safeCall.value(
        { model.generalizationTest(makeInput.value(21), makeTarget.value(21)) },
        "generalizationTest"
    );
    test.value("generalizationLoss est finie", {
        generalizationResult.notNil and: {
            finite.value(generalizationResult[\generalizationLoss])
        }
    });

    interferenceResult = safeCall.value(
        { model.interferenceTest(makeInput.value(22), makeTarget.value(22)) },
        "interferenceTest"
    );
    test.value("interferenceScore est fini", {
        interferenceResult.notNil and: {
            finite.value(interferenceResult[\interferenceScore])
        }
    });

    skipTest.value("memoryCount", "statistique interne sans accesseur public");
    skipTest.value("replayCount", "statistique interne sans accesseur public");
    skipTest.value("trajectoryCount", "statistique interne sans accesseur public");

    // ------------------------------------------------------------------------
    // 07. GENERATION
    // ------------------------------------------------------------------------
    section.value("07. GENERATION");
    generated = safeCall.value(
        { model.generateStep(makeInput.value(70)) },
        "generateStep"
    );
    test.value("generateStep retourne un vecteur 8D valide", {
        validVector.value(generated)
    });

    sequence = safeCall.value(
        { model.generate(makeInput.value(100), 64) },
        "generate 64"
    );
    test.value("generate retourne exactement 64 sorties", {
        sequence.notNil and: {
            sequence.isArray and: { sequence.size == 64 }
        }
    });

    allValid = sequence.notNil;
    if(sequence.notNil) {
        sequence.do { |vector|
            if(validVector.value(vector).not) { allValid = false };
        };
    };
    test.value("toutes les sorties generees sont valides", { allValid });

    // ------------------------------------------------------------------------
    // 08. DIVERSITE DE TRAJECTOIRE
    // ------------------------------------------------------------------------
    section.value("08. DIVERSITE DE TRAJECTOIRE");
    if(sequence.notNil and: { sequence.size > 1 }) {
        minDistance = 999999.0;
        meanDistance = 0.0;
        maxDistance = 0.0;
        changedCount = 0;

        (sequence.size - 1).do { |index|
            var currentDistance;
            currentDistance = distance.value(sequence[index], sequence[index + 1]);
            minDistance = minDistance.min(currentDistance);
            meanDistance = meanDistance + currentDistance;
            maxDistance = maxDistance.max(currentDistance);
            if(currentDistance > 0.000001) {
                changedCount = changedCount + 1;
            };
        };
        meanDistance = meanDistance / (sequence.size - 1);

        ("Distance minimale  : " ++ minDistance).postln;
        ("Distance moyenne   : " ++ meanDistance).postln;
        ("Distance maximale  : " ++ maxDistance).postln;
        ("Transitions actives: " ++ changedCount ++ "/" ++ (sequence.size - 1)).postln;

        test.value("les distances sont finies", {
            finite.value(minDistance) and: {
                finite.value(meanDistance) and: { finite.value(maxDistance) }
            }
        });
        test.value("la trajectoire n'est pas constante", { changedCount > 0 });
    } {
        skipTest.value("diversite de trajectoire", "sequence indisponible");
    };

    // ------------------------------------------------------------------------
    // 09. ENTREES AUX LIMITES
    // ------------------------------------------------------------------------
    section.value("09. ENTREES AUX LIMITES");
    [
        Array.fill(8, 0.0),
        Array.fill(8, 1.0),
        [0.001, 0.999, 0.001, 0.999, 0.001, 0.999, 0.001, 0.999],
        [0.999, 0.001, 0.999, 0.001, 0.999, 0.001, 0.999, 0.001]
    ].do { |seed, index|
        test.value("generation limite " ++ index.asString, {
            validVector.value(model.generateStep(seed))
        });
    };

    // ------------------------------------------------------------------------
    // 10. MODIFICATION ET RESTAURATION DES PARAMETRES
    // ------------------------------------------------------------------------
    section.value("10. MODIFICATION ET RESTAURATION DES PARAMETRES");
    originalRuntime = IdentityDictionary.new;

    [
        \deltaScale,
        \diversityRadius,
        \diversityRepulsionGain,
        \diversityNoiseGain,
        \diversityMaxCorrection,
        \localDiversityFloor,
        \localDiversityGain,
        \localDiversityMaxCorrection
    ].do { |parameter|
        var oldValue, testValue;
        oldValue = model.getParameter(parameter);
        originalRuntime[parameter] = oldValue;
        testValue = oldValue.asFloat * 0.5;

        result = safeCall.value({
            model.setParameter(parameter, testValue, false);
            model.getParameter(parameter)
        }, "setParameter " ++ parameter.asString);

        test.value("modification " ++ parameter.asString, {
            result.notNil and: { (result - testValue).abs < 0.000001 }
        });

        safeCall.value({
            model.setParameter(parameter, oldValue, false)
        }, "restauration " ++ parameter.asString);

        test.value("restauration " ++ parameter.asString, {
            (model.getParameter(parameter) - oldValue).abs < 0.000001
        });
    };

    // ------------------------------------------------------------------------
    // 11. ROBUSTESSE AUX ENTREES INVALIDES
    // ------------------------------------------------------------------------
    section.value("11. ROBUSTESSE AUX ENTREES INVALIDES");
    // Ces tests reussissent si le moteur refuse proprement l'entree,
    // c'est-a-dire s'il leve une erreur interceptee ou retourne nil.
    test.value("predict refuse un vecteur trop court", {
        var accepted = false;
        var value;
        value = { model.predict([0.1, 0.2]) }.try { |error|
            accepted = true;
            nil
        };
        accepted or: { value.isNil }
    });

    test.value("predict refuse nil", {
        var accepted = false;
        var value;
        value = { model.predict(nil) }.try { |error|
            accepted = true;
            nil
        };
        accepted or: { value.isNil }
    });

    test.value("learnPair refuse des dimensions incompatibles", {
        var accepted = false;
        var value;
        value = { model.learnPair([0.1, 0.2], Array.fill(8, 0.5)) }.try { |error|
            accepted = true;
            nil
        };
        accepted or: { value.isNil }
    });

    // ------------------------------------------------------------------------
    // 12. STABILITE PROLONGEE
    // ------------------------------------------------------------------------
    section.value("12. STABILITE PROLONGEE");
    longErrors = 0;
    500.do { |index|
        var x, y, pp, gg;
        x = makeInput.value(index + 200);
        y = makeTarget.value(index + 200);

        safeCall.value({ model.learnPair(x, y) }, "long learn " ++ index.asString);
        pp = safeCall.value({ model.predict(x) }, "long predict " ++ index.asString);
        gg = safeCall.value({ model.generateStep(x) }, "long generate " ++ index.asString);

        if(validVector.value(pp).not) { longErrors = longErrors + 1 };
        if(validVector.value(gg).not) { longErrors = longErrors + 1 };

        if((index % 50) == 0) {
            ("Etape " ++ index ++ "/500"
                ++ " | sorties invalides=" ++ longErrors).postln;
        };
    };

    test.value("aucune sortie invalide pendant 500 etapes", { longErrors == 0 });
    test.value("prediction finale valide", {
        validVector.value(model.predict(makeInput.value(9999)))
    });
    test.value("generation finale valide", {
        validVector.value(model.generateStep(makeInput.value(9999)))
    });

    // ------------------------------------------------------------------------
    // 13. TEST DE PERFORMANCE TEMPS REEL
    // ------------------------------------------------------------------------
    section.value("13. TEST DE PERFORMANCE TEMPS REEL");

    // Echauffement : evite de mesurer principalement le premier appel.
    50.do { |index|
        model.predict(makeInput.value(index));
        model.generateStep(makeInput.value(index));
    };

    // Mesure predict : 20 lots de 100 appels.
    perfTimes = Array.newClear(perfRuns);
    perfRuns.do { |run|
        benchmarkStart = Main.elapsedTime;
        perfBatchSize.do { |index|
            model.predict(makeInput.value((run * perfBatchSize) + index));
        };
        perfTimes[run] = Main.elapsedTime - benchmarkStart;
    };
    perfMean = (perfTimes.sum / (perfRuns * perfBatchSize)) * 1000.0;
    perfMin = (perfTimes.minItem / perfBatchSize) * 1000.0;
    perfMax = (perfTimes.maxItem / perfBatchSize) * 1000.0;
    perfThroughput = 1000.0 / perfMean.max(0.000001);
    ("predict       | moyenne=" ++ perfMean.round(0.001)
        ++ " ms | min lot=" ++ perfMin.round(0.001)
        ++ " ms | max lot=" ++ perfMax.round(0.001)
        ++ " ms | " ++ perfThroughput.round(0.1) ++ " appels/s").postln;
    test.value("predict performance mesurable", {
        finite.value(perfMean) and: { perfMean > 0.0 }
    });
    test.value("predict compatible interaction < 10 ms", { perfMean < 10.0 });

    // Mesure generateStep : inclut les controles de generation.
    perfTimes = Array.newClear(perfRuns);
    perfRuns.do { |run|
        benchmarkStart = Main.elapsedTime;
        perfBatchSize.do { |index|
            model.generateStep(makeInput.value((run * perfBatchSize) + index));
        };
        perfTimes[run] = Main.elapsedTime - benchmarkStart;
    };
    perfMean = (perfTimes.sum / (perfRuns * perfBatchSize)) * 1000.0;
    perfMin = (perfTimes.minItem / perfBatchSize) * 1000.0;
    perfMax = (perfTimes.maxItem / perfBatchSize) * 1000.0;
    perfThroughput = 1000.0 / perfMean.max(0.000001);
    ("generateStep  | moyenne=" ++ perfMean.round(0.001)
        ++ " ms | min lot=" ++ perfMin.round(0.001)
        ++ " ms | max lot=" ++ perfMax.round(0.001)
        ++ " ms | " ++ perfThroughput.round(0.1) ++ " appels/s").postln;
    test.value("generateStep performance mesurable", {
        finite.value(perfMean) and: { perfMean > 0.0 }
    });
    test.value("generateStep compatible interaction < 20 ms", { perfMean < 20.0 });

    // Mesure learnPair : cout de l'apprentissage interactif.
    perfTimes = Array.newClear(perfRuns);
    perfRuns.do { |run|
        benchmarkStart = Main.elapsedTime;
        perfBatchSize.do { |index|
            var n;
            n = (run * perfBatchSize) + index;
            model.learnPair(makeInput.value(n), makeTarget.value(n));
        };
        perfTimes[run] = Main.elapsedTime - benchmarkStart;
    };
    perfMean = (perfTimes.sum / (perfRuns * perfBatchSize)) * 1000.0;
    perfMin = (perfTimes.minItem / perfBatchSize) * 1000.0;
    perfMax = (perfTimes.maxItem / perfBatchSize) * 1000.0;
    perfThroughput = 1000.0 / perfMean.max(0.000001);
    ("learnPair     | moyenne=" ++ perfMean.round(0.001)
        ++ " ms | min lot=" ++ perfMin.round(0.001)
        ++ " ms | max lot=" ++ perfMax.round(0.001)
        ++ " ms | " ++ perfThroughput.round(0.1) ++ " appels/s").postln;
    test.value("learnPair performance mesurable", {
        finite.value(perfMean) and: { perfMean > 0.0 }
    });
    test.value("learnPair compatible interaction < 50 ms", { perfMean < 50.0 });

    // Test de charge mixte proche d'un usage interactif.
    benchmarkStart = Main.elapsedTime;
    benchmarkCount.do { |index|
        var x;
        x = makeInput.value(index + 5000);
        model.predict(x);
        model.generateStep(x);
        if((index % 4) == 0) {
            model.learnPair(x, makeTarget.value(index + 5000));
        };
    };
    elapsed = Main.elapsedTime - benchmarkStart;
    ("charge mixte  | " ++ benchmarkCount ++ " cycles en "
        ++ elapsed.round(0.001) ++ " s | moyenne="
        ++ ((elapsed / benchmarkCount) * 1000.0).round(0.001)
        ++ " ms/cycle").postln;
    test.value("charge mixte reste finie", {
        finite.value(elapsed) and: { elapsed > 0.0 }
    });

    // ------------------------------------------------------------------------
    // 14. RAPPORT FINAL
    // ------------------------------------------------------------------------
    section.value("14. RAPPORT FINAL");
    elapsed = Main.elapsedTime - startTime;
    ("Duree totale : " ++ elapsed.round(0.01) ++ " s").postln;
    ("TOTAL         : " ++ total).postln;
    ("PASS          : " ++ passed).postln;
    ("FAIL          : " ++ failed).postln;
    ("SKIP          : " ++ skipped).postln;
    ("AVERTISSEMENTS: " ++ warnings.size).postln;

    if(failures.notEmpty) {
        "".postln;
        "DETAIL DES ECHECS".postln;
        failures.do { |item, index|
            ((index + 1).asString ++ ". " ++ item).postln;
        };
    };

    if(warnings.notEmpty) {
        "".postln;
        "DETAIL DES AVERTISSEMENTS".postln;
        warnings.do { |item, index|
            ((index + 1).asString ++ ". " ++ item).postln;
        };
    };

    "".postln;
    if(failed == 0) {
        "RESULTAT GLOBAL : SUCCES".postln;
    } {
        "RESULTAT GLOBAL : ECHEC, consultez le detail ci-dessus".postln;
    };
    "================================================================".postln;
};
)
