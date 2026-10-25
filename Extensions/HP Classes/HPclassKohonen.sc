HPclassKohonen {
    var dataSOM, dimVecteurIn, distanceNeighbourhood, learningRate, initRate, initDistance;

    *new { arg d = 2, l = 12, vIn = 3;
        ^super.new.init(d, l, vIn);
    }

    init { arg d, l, vIn;
        var dimensions;

        dimVecteurIn = vIn;
        distanceNeighbourhood = initDistance = l / 6;
        learningRate = initRate = 0.1666;
        dimensions = Array.fill(d, l);

        dataSOM = Array.fillND(dimensions, { |...args|
            var position = args;
            [position, position.squared.sum.sqrt, Array.fill(dimVecteurIn, { 1.0.rand })]
        }).flatten(d - 1);
    }

    setSomWeight { arg weight;
        dataSOM = weight;
    }

    getSomWeight { arg weight;
        ^dataSOM;
    }

    training { arg vecteurInput, time = 1, trainingTime = 1, gate = 1;
        var node, weights, d, delta, minDistance = inf;
        var winnerIndices = List.new, neighbourhoodIndices = List.new;
        var nodesOutVoisins = List.new;
        var winnerIndex, winnerRadius, neighbourDistance, deltaRate;
        var neighbourhoodSquared, safeTrainingTime;

        // Recherche BMU en une passe. Les ex aequo restent tires au hasard.
        dataSOM.do({ |currentNode, i|
            weights = currentNode[2];
            d = 0.0;
            dimVecteurIn.do({ |j|
                delta = vecteurInput[j] - weights[j];
                d = d + (delta * delta);
            });
            d = d.sqrt;

            if(d < minDistance, {
                minDistance = d;
                winnerIndices.clear;
                winnerIndices.add(i);
            }, {
                if(d == minDistance, { winnerIndices.add(i) });
            });
        });

        winnerIndex = winnerIndices[winnerIndices.size.rand];
        winnerRadius = dataSOM[winnerIndex][1];
        neighbourhoodSquared = distanceNeighbourhood.squared;

        // Recherche des voisins et apprentissage dans la meme passe.
        dataSOM.do({ |currentNode, i|
            neighbourDistance = (winnerRadius - currentNode[1]).abs;
            if(neighbourDistance <= distanceNeighbourhood, {
                neighbourhoodIndices.add(i);

                if(gate == 1, {
                    weights = currentNode[2];
                    deltaRate = if(neighbourhoodSquared > 0, {
                        exp((neighbourDistance.squared / neighbourhoodSquared).neg)
                    }, {
                        1.0
                    });

                    dimVecteurIn.do({ |j|
                        weights[j] = weights[j]
                            + (deltaRate * learningRate * (vecteurInput[j] - weights[j]));
                    });
                });
            });
        });

        if(gate == 1, {
            safeTrainingTime = trainingTime.max(1.0e-12);
            learningRate = initRate * exp(time.neg / safeTrainingTime);
            distanceNeighbourhood = initDistance * exp(time.neg / safeTrainingTime);
            time = time + 1;
            if(time > trainingTime, { gate = 0 });
        });

        neighbourhoodIndices.do({ |neighbourIndex|
            if(neighbourIndex != winnerIndex, {
                node = dataSOM[neighbourIndex][0];
                if(node.size != 0, { nodesOutVoisins.add(node) });
            });
        });

        if(nodesOutVoisins.isEmpty, {
            nodesOutVoisins.add(dataSOM[winnerIndex][0]);
        });

        ^[dataSOM[winnerIndex], nodesOutVoisins.asArray];
    }
}
