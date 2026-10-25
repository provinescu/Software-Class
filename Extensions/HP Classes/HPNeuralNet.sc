HPNeuralNet {
    classvar kk;
    var poids, seuils, poidsNodes, numberNodes, numberLayers, numberNodesLayers, indexNodesLayers;

    *new { arg nodesIn = 3, layers = 1, nodesLayers = #[9], nodesOut = 3;
        kk = exp(1);
        ^super.new.init(nodesIn, layers, nodesLayers, nodesOut);
    }

    init { arg nodesIn, layers, nodesLayers, nodesOut;
        var indexNodes = 1;

        numberNodes = nodesIn + nodesLayers.sum + nodesOut;
        numberLayers = layers + 2;
        numberNodesLayers = [nodesIn] ++ nodesLayers.copy ++ [nodesOut];
        indexNodesLayers = Array.newClear(numberLayers);

        numberLayers.do({ |layer|
            indexNodesLayers[layer] = [
                indexNodes,
                indexNodes + numberNodesLayers[layer] - 1
            ];
            indexNodes = indexNodes + numberNodesLayers[layer];
        });

        poids = Array.fill(numberNodes, {
            Array.fill(numberNodes, { 0.5.rand })
        });
        seuils = Array.fill(numberNodes, { 0.5.rand });
    }

    load { arg p, s;
        if(p.notNil, { poids = p });
        if(s.notNil, { seuils = s });
    }

    save {
        ^[poids, seuils];
    }

    next { arg dataIn = #[], dataOut = nil, mode = 1, learning = 0.5, temperature = 1;
        var nodeActivation = Array.newClear(numberNodes);
        var nodeError = Array.newClear(numberNodes);
        var out, sumActivation, sumError;
        var inputRange, outputRange, previousRange, currentRange;
        var activation, error, weightRow;
        var safeTemperature = temperature.max(1.0e-12);

        if(dataOut.isNil, { dataOut = dataIn });

        inputRange = indexNodesLayers[0];
        (inputRange[0] .. inputRange[1]).do({ |node|
            nodeActivation[node - 1] = dataIn[node - 1];
        });

        // Propagation avant, sans allocations intermediaires.
        (numberLayers - 1).do({ |layer|
            previousRange = indexNodesLayers[layer];
            currentRange = indexNodesLayers[layer + 1];

            (currentRange[0] .. currentRange[1]).do({ |j|
                sumActivation = 0.0;
                (previousRange[0] .. previousRange[1]).do({ |i|
                    sumActivation = sumActivation
                        + (nodeActivation[i - 1] * poids[i - 1][j - 1]);
                });
                nodeActivation[j - 1] = 1.0 /
                    (1.0 + (kk ** ((sumActivation - seuils[j - 1]).neg / safeTemperature)));
            });
        });

        if(mode == 1, {
            outputRange = indexNodesLayers.last;

            // Erreur de sortie.
            (outputRange[0] .. outputRange[1]).do({ |i|
                activation = nodeActivation[i - 1];
                error = (dataOut[i - outputRange[0]] - activation)
                    * activation * (1.0 - activation);
                nodeError[i - 1] = error;
                seuils[i - 1] = seuils[i - 1] - (error * learning);
            });

            // Retropropagation, sans inverser les tableaux d'instance a chaque appel.
            (numberLayers - 2 .. 0).do({ |layer|
                currentRange = indexNodesLayers[layer];
                previousRange = indexNodesLayers[layer + 1];

                (currentRange[0] .. currentRange[1]).do({ |i|
                    sumError = 0.0;
                    (previousRange[0] .. previousRange[1]).do({ |j|
                        sumError = sumError + (nodeError[j - 1] * poids[i - 1][j - 1]);
                    });
                    activation = nodeActivation[i - 1];
                    nodeError[i - 1] = sumError * activation * (1.0 - activation);
                });
            });

            // Mise a jour identique a la classe originale : seuils de la couche
            // source, puis poids source -> cible. Les sorties ont deja ete traitees.
            (numberLayers - 2 .. 0).do({ |layer|
                currentRange = indexNodesLayers[layer];
                previousRange = indexNodesLayers[layer + 1];

                (currentRange[0] .. currentRange[1]).do({ |i|
                    seuils[i - 1] = seuils[i - 1] - (nodeError[i - 1] * learning);
                    weightRow = poids[i - 1];
                    (previousRange[0] .. previousRange[1]).do({ |j|
                        weightRow[j - 1] = weightRow[j - 1]
                            + (learning * nodeError[j - 1] * nodeActivation[i - 1]);
                    });
                });
            });
        });

        outputRange = indexNodesLayers.last;
        out = Array.newClear(outputRange[1] - outputRange[0] + 1);
        (outputRange[0] .. outputRange[1]).do({ |i|
            out[i - outputRange[0]] = nodeActivation[i - 1];
        });
        ^out;
    }
}
