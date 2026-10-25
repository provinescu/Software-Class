HPclassGenetiques {
    var allpopulations, populations, individus, fitness, normalisations, differences;

    *new { arg p, i;
        ^super.new.init(p, i);
    }

    init { arg p, i;
        populations = (p ? 3).clip(1, 64);
        individus = (i ? 64).clip(1, 256);
        differences = 0.01;
        fitness = Array.fill(individus, 1);
        allpopulations = Array.fill(populations, {
            Array.fill(individus, { 1.0.rand })
        });
        normalisations = Array.fill(populations, { [0, 1] });
    }

    load { arg p;
        if(p.notNil, {
            allpopulations = p;
            populations = allpopulations.size;
            normalisations = Array.fill(populations, { [0, 1] });
            differences = 0.01;
        });
    }

    save {
        ^allpopulations;
    }

    next { arg datasIn, mode = 1, croisement = 0.5, mutation = 0.05,
        difference = differences, normalisation = normalisations;

        var enfant1 = Array.newClear(populations);
        var enfant2 = Array.newClear(populations);
        var differencesEnfants = [0, 0];
        var diff = Array.newClear(individus);
        var indexParent, fitnessParent, enfantGagnant;
        var index1, index2, inverseFitness;
        var parentData, inputData, childData, norm, value;

        indexParent = fitness.normalizeSum.windex;
        fitnessParent = fitness[indexParent];

        populations.do({ |p|
            parentData = allpopulations[p][indexParent];
            inputData = datasIn[p];
            norm = normalisation[p];

            if(1.0.rand <= croisement, {
                enfant1[p] = this.mutateValue(parentData, mutation);
                enfant2[p] = this.mutateValue(inputData, mutation);
            }, {
                enfant1[p] = this.mutateValue(inputData, mutation);
                enfant2[p] = this.mutateValue(parentData, mutation);
            });

            // La classe originale re-clipe tous les enfants deja construits
            // avec la normalisation courante. Cette boucle conserve ce comportement.
            (0 .. p).do({ |childIndex|
                enfant1[childIndex] = enfant1[childIndex].clip(norm[0], norm[1]);
                enfant2[childIndex] = enfant2[childIndex].clip(norm[0], norm[1]);
            });
        });

        populations.do({ |p|
            value = (datasIn[p] - enfant1[p]).squared;
            differencesEnfants[0] = differencesEnfants[0] + value;
            value = (datasIn[p] - enfant2[p]).squared;
            differencesEnfants[1] = differencesEnfants[1] + value;
        });

        2.do({ |i|
            value = differencesEnfants[i].sqrt;
            if(value.size > 0, { value = value.mean });
            differencesEnfants[i] = value;
        });
        enfantGagnant = if(differencesEnfants[0] < differencesEnfants[1], 0, 1);

        individus.do({ |i|
            value = 0;
            populations.do({ |p|
                value = value + ((datasIn[p] - allpopulations[p][i]).squared);
            });
            value = value.sqrt;
            if(value.size > 0, { value = value.mean });
            diff[i] = value;
            if(value <= difference, { fitness[i] = fitness[i] + 1 });
        });

        if(mode == 1, {
            // Une seule construction de la distribution de remplacement.
            inverseFitness = (1 / fitness).normalizeSum;
            index1 = inverseFitness.windex;
            index2 = inverseFitness.windex;

            populations.do({ |p|
                allpopulations[p][index1] = enfant1[p];
                allpopulations[p][index2] = enfant2[p];
            });
            fitness[index1] = fitnessParent;
            fitness[index2] = fitnessParent;
        });

        ^if(enfantGagnant == 0, enfant1, enfant2);
    }

    mutateValue { arg source, mutation;
        if(1.0.rand <= mutation, {
            if(source.size > 1, {
                ^Array.fill(source.size, { 1.0.rand });
            }, {
                ^1.0.rand;
            });
        });
        ^source;
    }
}
