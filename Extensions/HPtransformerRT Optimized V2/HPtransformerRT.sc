/* HPtransformerRT V30.3.3.1 LEAN FORWARD RCU FIX SC314 - SuperCollider 3.14 class source.
Optimizations: Strict FloatArray usage for all numeric vectors/matrices,
reduced object overhead, preserved dynamic trajectory memory logic.
*/

HPtransformerRT {
    var attentionTemperature, routerTemperature, trajectoryRetrievalTemperature, expertUsageEMA, routerEntropyEMA, torusMask, generationWindowSize, localDiversityMaxCorrection, localDiversityGain, localDiversityFloor, localDiversityWindow, diversityAdaptiveGain, diversityMaxCorrection, diversityNoiseGain, diversityRepulsionGain, diversityRadius, diversityHistorySize, trajectoryExplorationGain, trajectoryVelocityWeight, trajectoryInputWeight, trajectoryNoveltyWeight, trajectoryUsageDecay, trajectoryDecay, trajectoryAccelerationClip, trajectoryVelocityClip, trajectoryAccelerationGain, trajectoryVelocityGain, trajectoryRetrievalGain, trajectoryRecallSize, trajectoryMemorySize, interferenceTestSteps, headSpecializationStrength, headSpecializationThreshold, expertBalanceStrength, replayUniformMix, replayPriorityMix, directionLossWeight, deltaLossWeight, predictionLossWeight, adaptationSlowRate, adaptationFastRate, protectionStrength, memoryRecallSize, replayBatchSize, replayRate, memoryUsageDecay, memoryConsolidationRate, memoryDecay, memoryRetrievalTemperature, memoryRetrievalGain, memoryWriteThreshold, gradientClip, gateLearningRate, surpriseGain, surpriseThreshold, deltaScale, expertScale, residualScale, temperature, epsilon, beta2, beta1, learningRate, numExperts, headSize, numHeads, hiddenSize, longMemorySize, windowSize, outputSize, inputSize, parameterCount, inputProjection, inputBias, outputProjection, outputBias, qWeights, kWeights, vWeights, qBias, kBias, vBias, outputAttentionWeight, outputAttentionBias, featureGateLogits, routerWeights, routerBias, expertW1, expertB1, expertW2, expertB2, positionBias, adamMInput, adamVInput, adamMInputBias, adamVInputBias, adamMOutput, adamVOutput, adamMOutputBias, adamVOutputBias, adamMQ, adamVQ, adamMK, adamVK, adamMV, adamVV, adamMBQ, adamVBQ, adamMBK, adamVBK, adamMBV, adamVBV, adamMAttention, adamVAttention, adamMBAttention, adamVBAttention, adamMGates, adamVGates, adamMRouter, adamVRouter, adamMBRouter, adamVBRouter, adamMExpertW1, adamVExpertW1, adamMExpertB1, adamVExpertB1, adamMExpertW2, adamVExpertW2, adamMExpertB2, adamVExpertB2, adamMPosition, adamVPosition, shortMemory, longMemory, longMemoryImportance, longMemoryAge, longMemoryUsage, longMemorySurprise, trajectoryMemory, trajectoryCount, trajectoryVelocityContext, trajectoryAccelerationContext, trajectoryRecall, trajectoryNovelty, trajectoryWriteScore, trajectoryLoss, trajectoryIndices, trajectoryWeights, lastLearnedVelocity, lastGeneratedVelocity, generationHistory, generationDiversity, generationNovelty, generationMinDistance, generationMeanDistance, generationPressure, localDiversity, localContractionPressure, localContractionCorrection, replayInputs, replayTargets, replayImportance, memoryCount, replayCount, memoryContext, memoryWeights, memorySimilarity, protectionScalar, lastInput, lastPrediction, lastDelta, lastTarget, attentionProfile, headActivity, headFeatureUsage, expertUsage, loss, outputLoss, deltaLoss, directionLoss, expertBalanceLoss, surprise, surpriseEMA, errorEMA, memoryRecall, memoryNovelty, memoryWriteScore, replayLoss, interferenceScore, entropy, learningRateCurrent, learnedEvents, adamStep, totalEvents, initialized, preHidden, eventHistory, generationDriftState, autoTuneEnabled, autoTuneInterval, autoTuneCounter, autoTuneStrength, autoTuneSmoothing, autoTuneTargetNovelty, autoTuneTargetDiversity, autoTuneNoveltyEMA, autoTuneDiversityEMA, autoTuneLastError, autoTuneAdjustmentCount, autoTuneExplorationMin, autoTuneExplorationMax, autoTuneNoiseMin, autoTuneNoiseMax, autoTuneTemperatureMin, autoTuneTemperatureMax, metaLearnEnabled, metaLearnInterval, metaLearnCounter, metaLearnStrength, metaLearnSmoothing, metaLearnTargetError, metaLearnTargetSurprise, metaLearnTargetRecall, metaLearnTargetInterference, metaLearnErrorEMA, metaLearnSurpriseEMA, metaLearnRecallEMA, metaLearnInterferenceEMA, metaLearnPlasticityPressure, metaLearnStabilityPressure, metaLearnAdjustmentCount, metaLearnLearningRateMin, metaLearnLearningRateMax, metaLearnReplayRateMin, metaLearnReplayRateMax, metaLearnProtectionMin, metaLearnProtectionMax, metaLearnRetrievalGainMin, metaLearnRetrievalGainMax, unifiedCurrentRuntime, unifiedPendingRuntime, unifiedTrainingVersion, unifiedPublishedVersion, unifiedSnapshotPending, unifiedRuntimeRole, unifiedLearningEnabled, unifiedGenerationEnabled, unifiedParameterMorphs, adaptiveState,
    normalizationState, multiResolutionState, stabilityGuardState, gradientBuffers;

    *new { |
        inputSize=8,
        outputSize=8,
        windowSize=8,
        longMemorySize=16,
        hiddenSize=32,
        numHeads=4,
        headSize=8,
        numExperts=4,
        learningRate=0.00035,
        beta1=0.9,
        beta2=0.999,
        epsilon=0.00000001,
        temperature=1.0,
        residualScale=0.34,
        expertScale=0.27,
        deltaScale=0.30,
        surpriseThreshold=0.06,
        surpriseGain=1.10,
        gateLearningRate=0.0008,
        gradientClip=0.75,
        memoryWriteThreshold=0.10,
        memoryRetrievalGain=0.10,
        memoryRetrievalTemperature=0.30,
        memoryDecay=0.996,
        memoryConsolidationRate=0.02,
        memoryUsageDecay=0.999,
        replayRate=0.08,
        replayBatchSize=1,
        memoryRecallSize=3,
        protectionStrength=0.16,
        adaptationFastRate=1.35,
        adaptationSlowRate=0.40,
        predictionLossWeight=0.68,
        deltaLossWeight=0.12,
        directionLossWeight=0.20,
        replayPriorityMix=0.90,
        replayUniformMix=0.10,
        expertBalanceStrength=0.01,
        headSpecializationThreshold=0.85,
        headSpecializationStrength=0.003,
        interferenceTestSteps=4,
        trajectoryMemorySize=12,
        trajectoryRecallSize=1,
        trajectoryRetrievalGain=0.07,
        trajectoryVelocityGain=0.045,
        trajectoryAccelerationGain=0.008,
        trajectoryVelocityClip=0.10,
        trajectoryAccelerationClip=0.20,
        trajectoryDecay=0.965,
        trajectoryUsageDecay=0.999,
        trajectoryNoveltyWeight=0.42,
        trajectoryInputWeight=0.32,
        trajectoryVelocityWeight=0.16,
        trajectoryExplorationGain=0.0045,
        diversityHistorySize=10,
        diversityRadius=0.015,
        diversityRepulsionGain=0.0016,
        diversityNoiseGain=0.00035,
        diversityMaxCorrection=0.020,
        diversityAdaptiveGain=0.65,
        localDiversityWindow=12,
        localDiversityFloor=0.028,
        localDiversityGain=0.055,
        localDiversityMaxCorrection=0.008,
        generationWindowSize=8,
        torusMask=nil|
        ^super.new.init(
            inputSize, outputSize, windowSize, longMemorySize, hiddenSize, numHeads, headSize, numExperts,
            learningRate, beta1, beta2, epsilon, temperature, residualScale, expertScale, deltaScale,
            surpriseThreshold, surpriseGain, gateLearningRate, gradientClip,
            memoryWriteThreshold, memoryRetrievalGain, memoryRetrievalTemperature, memoryDecay,
            memoryConsolidationRate, memoryUsageDecay, replayRate, replayBatchSize, memoryRecallSize,
            protectionStrength, adaptationFastRate, adaptationSlowRate, predictionLossWeight, deltaLossWeight,
            directionLossWeight, replayPriorityMix, replayUniformMix, expertBalanceStrength,
            headSpecializationThreshold, headSpecializationStrength, interferenceTestSteps,
            trajectoryMemorySize, trajectoryRecallSize, trajectoryRetrievalGain, trajectoryVelocityGain,
            trajectoryAccelerationGain, trajectoryVelocityClip, trajectoryAccelerationClip, trajectoryDecay,
            trajectoryUsageDecay, trajectoryNoveltyWeight, trajectoryInputWeight, trajectoryVelocityWeight,
            trajectoryExplorationGain, diversityHistorySize, diversityRadius, diversityRepulsionGain,
            diversityNoiseGain, diversityMaxCorrection, diversityAdaptiveGain, localDiversityWindow,
            localDiversityFloor, localDiversityGain, localDiversityMaxCorrection, generationWindowSize, torusMask
        );
    }

    init { |
        inInputSize, inOutputSize, inWindowSize, inLongMemorySize, inHiddenSize, inNumHeads, inHeadSize, inNumExperts,
        inLearningRate, inBeta1, inBeta2, inEpsilon, inTemperature, inResidualScale, inExpertScale, inDeltaScale,
        inSurpriseThreshold, inSurpriseGain, inGateLearningRate, inGradientClip, inMemoryWriteThreshold,
        inMemoryRetrievalGain, inMemoryRetrievalTemperature, inMemoryDecay, inMemoryConsolidationRate,
        inMemoryUsageDecay, inReplayRate, inReplayBatchSize, inMemoryRecallSize, inProtectionStrength,
        inAdaptationFastRate, inAdaptationSlowRate, inPredictionLossWeight, inDeltaLossWeight, inDirectionLossWeight,
        inReplayPriorityMix, inReplayUniformMix, inExpertBalanceStrength, inHeadSpecializationThreshold,
        inHeadSpecializationStrength, inInterferenceTestSteps, inTrajectoryMemorySize, inTrajectoryRecallSize,
        inTrajectoryRetrievalGain, inTrajectoryVelocityGain, inTrajectoryAccelerationGain, inTrajectoryVelocityClip,
        inTrajectoryAccelerationClip, inTrajectoryDecay, inTrajectoryUsageDecay, inTrajectoryNoveltyWeight,
        inTrajectoryInputWeight, inTrajectoryVelocityWeight, inTrajectoryExplorationGain, inDiversityHistorySize,
        inDiversityRadius, inDiversityRepulsionGain, inDiversityNoiseGain, inDiversityMaxCorrection,
        inDiversityAdaptiveGain, inLocalDiversityWindow, inLocalDiversityFloor, inLocalDiversityGain,
        inLocalDiversityMaxCorrection, inGenerationWindowSize, inTorusMask|

        inputSize = inInputSize.asInteger.max(1);
        outputSize = inOutputSize.asInteger.max(1);
        if(outputSize > inputSize, {
            Error("HPtransformerRT: outputSize must be <= inputSize").throw;
        });

        // Scalar assignments
        windowSize = inWindowSize; longMemorySize = inLongMemorySize; hiddenSize = inHiddenSize;
        numHeads = inNumHeads; headSize = inHeadSize; numExperts = inNumExperts;
        learningRate = inLearningRate; beta1 = inBeta1; beta2 = inBeta2; epsilon = inEpsilon;
        temperature = inTemperature; attentionTemperature = inTemperature; routerTemperature = inTemperature;
        trajectoryRetrievalTemperature = 0.30;
        residualScale = inResidualScale; expertScale = inExpertScale; deltaScale = inDeltaScale;
        surpriseThreshold = inSurpriseThreshold; surpriseGain = inSurpriseGain;
        gateLearningRate = inGateLearningRate; gradientClip = inGradientClip;
        memoryWriteThreshold = inMemoryWriteThreshold; memoryRetrievalGain = inMemoryRetrievalGain;
        memoryRetrievalTemperature = inMemoryRetrievalTemperature; memoryDecay = inMemoryDecay;
        memoryConsolidationRate = inMemoryConsolidationRate; memoryUsageDecay = inMemoryUsageDecay;
        replayRate = inReplayRate; replayBatchSize = inReplayBatchSize; memoryRecallSize = inMemoryRecallSize;
        protectionStrength = inProtectionStrength;
        adaptationFastRate = inAdaptationFastRate; adaptationSlowRate = inAdaptationSlowRate;
        predictionLossWeight = inPredictionLossWeight; deltaLossWeight = inDeltaLossWeight;
        directionLossWeight = inDirectionLossWeight; replayPriorityMix = inReplayPriorityMix;
        replayUniformMix = inReplayUniformMix; expertBalanceStrength = inExpertBalanceStrength;
        headSpecializationThreshold = inHeadSpecializationThreshold;
        headSpecializationStrength = inHeadSpecializationStrength;
        interferenceTestSteps = inInterferenceTestSteps;
        trajectoryMemorySize = inTrajectoryMemorySize; trajectoryRecallSize = inTrajectoryRecallSize;
        trajectoryRetrievalGain = inTrajectoryRetrievalGain; trajectoryVelocityGain = inTrajectoryVelocityGain;
        trajectoryAccelerationGain = inTrajectoryAccelerationGain;
        trajectoryVelocityClip = inTrajectoryVelocityClip; trajectoryAccelerationClip = inTrajectoryAccelerationClip;
        trajectoryDecay = inTrajectoryDecay; trajectoryUsageDecay = inTrajectoryUsageDecay;
        trajectoryNoveltyWeight = inTrajectoryNoveltyWeight; trajectoryInputWeight = inTrajectoryInputWeight;
        trajectoryVelocityWeight = inTrajectoryVelocityWeight;
        trajectoryExplorationGain = inTrajectoryExplorationGain;
        diversityHistorySize = inDiversityHistorySize; diversityRadius = inDiversityRadius;
        diversityRepulsionGain = inDiversityRepulsionGain; diversityNoiseGain = inDiversityNoiseGain;
        diversityMaxCorrection = inDiversityMaxCorrection; diversityAdaptiveGain = inDiversityAdaptiveGain;
        localDiversityWindow = inLocalDiversityWindow; localDiversityFloor = inLocalDiversityFloor;
        localDiversityGain = inLocalDiversityGain; localDiversityMaxCorrection = inLocalDiversityMaxCorrection;
        generationWindowSize = inGenerationWindowSize;
        torusMask = this.normalizeTorusMask(inTorusMask);

        parameterCount = this.parameterCounter;
        this.resetAll;
        initialized = true;
        unifiedCurrentRuntime = nil; unifiedPendingRuntime = nil;
        unifiedTrainingVersion = 0; unifiedPublishedVersion = 0;
        unifiedSnapshotPending = false; unifiedRuntimeRole = false;
        unifiedLearningEnabled = true; unifiedGenerationEnabled = true;
        unifiedParameterMorphs = IdentityDictionary.new;
        this.resetAdvancedRealtimeState;
        ^this;
    }

    // --- Optimized Math Primitives using FloatArray ---
    clean { |x| var y; y=x; if(y.isArray,{if(y.size>0,{y=y[0]},{y=0.0})}); while({y.isArray},{if(y.size>0,{y=y[0]},{y=0.0})}); if(y.isNumber.not,{y=0.0},{y=y.asFloat}); if(y.isNaN,{y=0.0}); if(y.abs>1e10,{if(y>0.0,{y=10.0},{y=(-10.0)})}); ^y.clip(-10.0,10.0); }
    sigmoid { |x| var y; y=this.clean(x); if(y>12.0,{^0.999993855}); if(y<(-12.0),{^0.000006144}); ^1.0/(1.0+y.neg.exp); }
    sigmoidDerivative { |y| ^y*(1.0-y); }
    tanhSafe { |x| ^this.clean(x).tanh.clip(-1.0,1.0); }
    tanhDerivative { |y| ^1.0-(y*y); }

    dot { |a,b|
        var r = 0.0;
        // FloatArray access is faster
        a.size.do({|i| r = r + (a[i]*b[i])});
        ^r;
    }

    matrixVector { |m, v|
        // FloatArray output avoids generic Array allocation in the hot path.
        ^FloatArray.fill(m.size, { |r| this.dot(m[r], v) });
    }
    addVector { |a, b|
        ^FloatArray.fill(a.size, { |i| a[i] + b[i] });
    }
    scaleVector { |a, s|
        ^FloatArray.fill(a.size, { |i| a[i] * s });
    }
    vectorMean { |v| if(v.size>0,{^v.sum/v.size},{^0.0}); }

    vectorMSE { |a,b|
        var r=0.0, d;
        a.size.do({|i| d=a[i]-b[i]; r=r+(d*d)});
        ^r/a.size.max(1);
    }

    // Optimized matrix creation with FloatArray
    makeMatrix { |rows,cols,scale=0.05|
        ^Array.fill(rows, { Array.fill(cols, { 1.0.rand2*scale }) });
    }

    makeFloatMatrix { |rows, cols, scale=0.05|
        ^Array.fill(rows, { FloatArray.fill(cols, { 1.0.rand2*scale }) });
    }

    makeVector { |size,value=0.0| ^Array.fill(size,{value}); }
    makeFloatVector { |size, value=0.0| ^FloatArray.fill(size, value); }

    randomWeight { |scale=0.05| ^1.0.rand2*scale; }
    copyVector { |v|
        ^FloatArray.fill(v.size, { |i| v[i] });
    }
    copyMatrix { |m|
        ^Array.fill(m.size, { |r|
            FloatArray.fill(m[r].size, { |c| m[r][c] });
        });
    }
    zeroVector { |size| ^FloatArray.fill(size, 0.0); }
    zeroMatrix { |rows,cols| ^Array.fill(rows, { FloatArray.fill(cols, 0.0) }); }

    clampParameter { |x| ^x.clip(-4.0,4.0); }

    gradientNormVector { |gradient|
        var total=0.0;
        gradient.do({|value| var x; x=this.clean(value); total=total+(x*x)});
        ^total.max(0.0).sqrt;
    }

    gradientNormMatrix { |gradient|
        var total=0.0;
        gradient.do({|row| row.do({|value| var x; x=this.clean(value); total=total+(x*x)})});
        ^total.max(0.0).sqrt;
    }

    torusDelta { |a,b| var d; d=(a-b).abs; ^d.min(1.0-d); }
    torusDifference { |a,b| var d; d=b-a; if(d>0.5,{d=d-1.0}); if(d<(-0.5),{d=d+1.0}); ^d; }
    wrap01 { |x| var y; y=x%1.0; if(y<0.0,{y=y+1.0}); ^y; }

    normalizeTorusMask { |mask|
        if(mask.isNil,{^Array.fill(outputSize,{true})});
        ^Array.fill(outputSize,{|i| if(i<mask.size,{mask[i]==true},{false})});
    }

    isTorusDimension { |i| ^(i<torusMask.size and:{torusMask[i]==true}); }
    maskedDifference { |a,b,i| if(this.isTorusDimension(i),{^this.torusDifference(a,b)},{^b-a}); }
    maskedDistance { |a,b,i| if(this.isTorusDimension(i),{^this.torusDelta(a,b)},{^(a-b).abs}); }
    maskedWrap { |value,i| if(this.isTorusDimension(i),{^this.wrap01(value)},{^value.clip(0.0,1.0)}); }

    maskedMSE { |a,b|
        var r=0.0, size, d;
        size=a.size.min(b.size).min(torusMask.size);
        size.do({|i| d=this.maskedDistance(a[i],b[i],i); r=r+(d*d)});
        ^r/size.max(1);
    }

    getTorusMask { ^torusMask.copy; }

    edgePush { |value,index,loEdge=0.05,hiEdge=0.95,gainEdge=0.08|
        var result=value;
        if(this.isTorusDimension(index).not,{
            if(result<loEdge,{result=result+((loEdge-result)*gainEdge)});
            if(result>hiEdge,{result=result-((result-hiEdge)*gainEdge)});
            result=result.clip(0.0,1.0)
        });
        ^result;
    }

    parameterCounter { var n; n=0; n=n+(hiddenSize*inputSize)+hiddenSize; n=n+(outputSize*hiddenSize)+outputSize; numHeads.do({n=n+(3*headSize*inputSize)+(3*headSize)+inputSize+windowSize}); n=n+(hiddenSize*(numHeads*headSize))+hiddenSize; n=n+(numExperts*hiddenSize)+numExperts; numExperts.do({n=n+(hiddenSize*hiddenSize)+hiddenSize+(hiddenSize*hiddenSize)+hiddenSize}); ^n; }

    cleanSizedVector { |input, size|
        var x, out;
        x = input;
        while({ x.isArray and: { x.size == 1 and: { x[0].isArray } } }, { x = x[0]; });
        // Use FloatArray for output
        out = FloatArray.fill(size.asInteger.max(1), { |i|
            if(x.isArray and: { i < x.size }, {
                this.clean(x[i]).clip(0.0, 1.0);
            }, { 0.0; });
        });
        ^out;
    }

    cleanSizedVelocity { |velocity, size|
        var x, out;
        x = velocity;
        while({ x.isArray and: { x.size == 1 and: { x[0].isArray } } }, { x = x[0]; });
        out = FloatArray.fill(size.asInteger.max(1), { |i|
            if(x.isArray and: { i < x.size }, {
                this.clean(x[i]).clip(-1.0, 1.0);
            }, { 0.0; });
        });
        ^out;
    }

    cleanInputVector { |input| ^this.cleanSizedVector(input, inputSize); }
    cleanOutputVector { |output| ^this.cleanSizedVector(output, outputSize); }
    cleanVector { |input| ^this.cleanInputVector(input); }

    expandOutputToInput { |output, previousInput|
        var result, cleanOutput;
        result = this.cleanInputVector(previousInput);
        cleanOutput = this.cleanOutputVector(output);
        outputSize.do({ |i| result[i] = cleanOutput[i]; });
        ^result;
    }

    expandOutputDelta { |delta|
        ^FloatArray.fill(inputSize, { |i|
            if(i < outputSize, { this.clean(delta[i]); }, { 0.0; });
        });
    }

    outputToNextInput { |output, previousInput| ^this.expandOutputToInput(output, previousInput); }

    softmaxWithTemperature { |values, requestedTemperature|
        var cleanValues, maximum, exps, total, safeTemperature;
        cleanValues = if(values.isArray, {
            FloatArray.fill(values.size, { |i| this.clean(values[i]) });
        }, { FloatArray[ this.clean(values) ]; });
        if(cleanValues.size <= 0, { ^FloatArray[1.0] });
        safeTemperature = this.clean(requestedTemperature).abs.clip(0.01, 20.0);
        maximum = cleanValues.maxItem;
        exps = FloatArray.fill(cleanValues.size, { |i|
            ((cleanValues[i] - maximum) / safeTemperature).clip(-80.0, 80.0).exp;
        });
        total = exps.sum.max(0.000000001);
        ^FloatArray.fill(exps.size, { |i| exps[i] / total });
    }
    softmax { |values| ^this.softmaxWithTemperature(values, temperature); }

    vectorDirectionLoss { |a, b|
        var result=0.0, sa, sb, d;
        a.size.do({ |i|
            sa = (12.0 * a[i]).tanh;
            sb = (12.0 * b[i]).tanh;
            d = sa - sb;
            result = result + (d * d);
        });
        ^result / a.size.max(1);
    }

    adamVector { |parameters, gradient, m, v, baseRate|
        var bc1, effectiveRate, bc2, norm, gradientScale;
        effectiveRate = if(baseRate.isNil, { learningRateCurrent }, { baseRate * (learningRateCurrent / learningRate.max(0.000000001)) });
        bc1 = 1.0 - beta1.pow(adamStep);
        bc2 = 1.0 - beta2.pow(adamStep);
        norm = this.gradientNormVector(gradient);
        gradientScale = if(norm > gradientClip, { gradientClip / norm }, { 1.0 });

        parameters.size.do({ |i|
            var g, mh, vh, denom;
            g = this.clean(gradient[i]) * gradientScale;
            mh = (beta1 * m[i]) + ((1.0 - beta1) * g);
            vh = (beta2 * v[i]) + ((1.0 - beta2) * g * g);
            m[i] = mh; v[i] = vh;
            denom = (vh / bc2).sqrt + epsilon;
            parameters[i] = this.clampParameter(parameters[i] - (effectiveRate * (mh / bc1) / denom));
        });
    }

    adamMatrix { |parameters, gradient, m, v|
        var bc1, bc2, norm, gradientScale;
        bc1 = 1.0 - beta1.pow(adamStep);
        bc2 = 1.0 - beta2.pow(adamStep);
        norm = this.gradientNormMatrix(gradient);
        gradientScale = if(norm > gradientClip, { gradientClip / norm }, { 1.0 });

        parameters.size.do({ |r|
            parameters[r].size.do({ |c|
                var g, mh, vh, denom;
                g = this.clean(gradient[r][c]) * gradientScale;
                mh = (beta1 * m[r][c]) + ((1.0 - beta1) * g);
                vh = (beta2 * v[r][c]) + ((1.0 - beta2) * g * g);
                m[r][c] = mh; v[r][c] = vh;
                denom = (vh / bc2).sqrt + epsilon;
                parameters[r][c] = this.clampParameter(parameters[r][c] - (learningRateCurrent * (mh / bc1) / denom));
            });
        });
    }

    updateExpertDiagnostics { |usage|
        var safeUsage, entropySum, uniformUsage, q;
        uniformUsage = 1.0 / numExperts.max(1);
        safeUsage = if(usage.isArray and: { usage.size == numExperts }, {
            FloatArray.fill(numExperts, { |index| this.clean(usage[index]).max(0.0) });
        }, { FloatArray.fill(numExperts, uniformUsage); });
        if(expertUsageEMA.isArray.not or: { expertUsageEMA.size != numExperts }, {
            expertUsageEMA = FloatArray.fill(numExperts, uniformUsage);
        });
        expertUsage = safeUsage;
        entropySum = 0.0;
        numExperts.do({ |index|
            expertUsageEMA[index] = (0.98 * expertUsageEMA[index]) + (0.02 * safeUsage[index]);
            q = safeUsage[index].max(0.000000001);
            entropySum = entropySum + (q * q.log.neg);
        });
        routerEntropyEMA = (0.98 * this.clean(routerEntropyEMA)) + (0.02 * entropySum);
        ^expertUsage;
    }

    getFeatureGate { |h, i| ^this.sigmoid(featureGateLogits[h][i]); }

    vectorSimilarity { |a, b|
        var ab, aa, bb, denom;
        ab = this.dot(a, b);
        aa = this.dot(a, a).sqrt;
        bb = this.dot(b, b).sqrt;
        denom = (aa * bb).max(0.000000001);
        ^(ab / denom).clip(-1.0, 1.0);
    }

    memoryDistance { |a, b|
        var sum=0.0;
        a.size.do({ |i| var d; d = this.maskedDistance(a[i], b[i], i); sum = sum + (d * d); });
        ^(sum / a.size.max(1)).sqrt.clip(0.0, 1.0);
    }

    velocityDistance { |a, b|
        var sum=0.0, size, d;
        size = a.size.min(b.size).max(1);
        size.do({ |i| d = this.clean(a[i]) - this.clean(b[i]); sum = sum + (d * d); });
        ^(sum / size).sqrt.clip(0.0, 2.0);
    }

    calculateMemoryNovelty { |x|
        var novelty;
        if(memoryCount == 0, { novelty = 1.0 }, {
            novelty = 1.0;
            memoryCount.do({ |i|
                novelty = novelty.min(this.memoryDistance(x, longMemory[i]));
            });
        });
        ^novelty.clip(0.0, 1.0);
    }

    retrieveMemory { |x|
        var scores, total, context, selected, n, bestIndex, bestValue, used, selectedTotal, maxScore, sharpScores, sharpTotal;
        scores = FloatArray.fill(memoryCount.max(1), 0.0);

        if(memoryCount == 0, {
            context = this.zeroVector(inputSize);
            memoryRecall = 0.0;
            ^(context: context, weights: scores, indices: Array.new, recall: 0.0);
        }, {
            memoryCount.do({ |i|
                var similarity, distance, score;
                similarity = this.vectorSimilarity(x, longMemory[i]);
                distance = this.memoryDistance(x, longMemory[i]);
                score = (0.55 * (((similarity + 1.0) * 0.5))) + (0.25 * (1.0 - distance)) + (0.20 * longMemoryImportance[i]);
                score = score * longMemoryUsage[i].clip(0.25, 1.0);
                scores[i] = score.clip(0.0, 1.0);
            });

            maxScore = scores.maxItem;
            sharpScores = FloatArray.fill(scores.size, { |i| ((scores[i] - maxScore) / memoryRetrievalTemperature.max(0.05)).exp; });
            sharpTotal = sharpScores.sum.max(0.000000001);
            scores = FloatArray.fill(sharpScores.size, { |i| sharpScores[i] / sharpTotal });

            context = this.zeroVector(inputSize);
            selected = Array.new;
            used = Array.fill(memoryCount, { false });
            selectedTotal = 0.0;
            n = memoryRecallSize.min(memoryCount);

            n.do({
                bestIndex = 0; bestValue = -1.0;
                memoryCount.do({ |i|
                    if(used[i].not and: { scores[i] > bestValue }, {
                        bestValue = scores[i]; bestIndex = i;
                    });
                });
                used[bestIndex] = true;
                selected.add(bestIndex);
                selectedTotal = selectedTotal + scores[bestIndex];
                longMemoryUsage[bestIndex] = ((longMemoryUsage[bestIndex] * 0.95) + 0.05).clip(0.0, 1.0);
            });

            selectedTotal = selectedTotal.max(0.000000001);
            selected.do({ |index|
                var w = scores[index] / selectedTotal;
                inputSize.do({ |d| context[d] = context[d] + (w * longMemory[index][d]); });
            });

            memoryWeights = scores.copy;
            memorySimilarity = selected.collect({ |index| this.vectorSimilarity(x, longMemory[index]); });
            memoryContext = context.clip(0.0, 1.0);
            memoryRecall = selectedTotal.clip(0.0, 1.0);
            ^(context: memoryContext.copy, weights: scores, indices: selected.asArray, recall: memoryRecall);
        });
    }

    writeLongMemory { |input, target, surpriseValue|
        var novelty, importance, index, shouldWrite;
        novelty = this.calculateMemoryNovelty(input);
        importance = (0.55 * surpriseValue.clip(0.0, 1.0)) + (0.45 * novelty);
        memoryWriteScore = importance;
        shouldWrite = (importance > memoryWriteThreshold);
        if(memoryCount == 0, { shouldWrite = true });

        if(shouldWrite, {
            if(memoryCount < longMemorySize, {
                index = memoryCount;
                memoryCount = memoryCount + 1;
            }, {
                index = longMemoryImportance.indexOf(longMemoryImportance.minItem);
            });
            longMemory[index] = this.copyVector(input);
            longMemoryImportance[index] = importance.clip(0.0, 1.0);
            longMemoryAge[index] = 0;
            longMemoryUsage[index] = 1.0;
            longMemorySurprise[index] = surpriseValue.clip(0.0, 1.0);
        });
    }

    writeReplay { |input, target, importance|
        var index;
        if(replayCount < longMemorySize, {
            index = replayCount;
            replayCount = replayCount + 1;
        }, {
            index = replayImportance.indexOf(replayImportance.minItem);
        });
        replayInputs[index] = this.copyVector(input);
        replayTargets[index] = this.copyVector(target);
        replayImportance[index] = importance.clip(0.0, 1.0);
    }

    consolidateMemory {
        memoryCount.do({ |i|
            longMemoryAge[i] = longMemoryAge[i] + 1;
            longMemoryImportance[i] = (longMemoryImportance[i] * memoryDecay) + (longMemorySurprise[i] * (1.0 - memoryDecay));
            longMemoryUsage[i] = longMemoryUsage[i] * memoryUsageDecay;
            if(longMemoryUsage[i] < 0.05, {
                longMemoryImportance[i] = longMemoryImportance[i] * (1.0 - memoryConsolidationRate);
            });
        });
    }

    retrieveTrajectory { |x, velocity|
        var scores, used, selected, contextVelocity, contextAcceleration, selectedTotal, n, bestIndex, bestValue, inputSimilarity, velocityDistance, score, maxScore, sharpScores, sharpTotal, trajectoryWeightTotal, normalizedInputWeight, normalizedVelocityWeight;

        trajectoryNovelty = 1.0;
        if(trajectoryCount == 0, {
            trajectoryVelocityContext = this.zeroVector(inputSize);
            trajectoryAccelerationContext = this.zeroVector(inputSize);
            trajectoryRecall = 0.0;
            trajectoryIndices = Array.new;
            trajectoryWeights = Array.new;
            ^[trajectoryVelocityContext.copy, trajectoryAccelerationContext.copy, 0.0, 1.0, Array.new, Array.new];
        }, {
            trajectoryWeightTotal = (trajectoryInputWeight + trajectoryVelocityWeight).max(0.000001);
            normalizedInputWeight = trajectoryInputWeight / trajectoryWeightTotal;
            normalizedVelocityWeight = trajectoryVelocityWeight / trajectoryWeightTotal;

            scores = FloatArray.fill(trajectoryCount, { 0.0 });
            trajectoryCount.do({ |i|
                inputSimilarity = this.vectorSimilarity(x, trajectoryMemory[i][0]);
                velocityDistance = this.velocityDistance(velocity, trajectoryMemory[i][1]);
                score = (normalizedInputWeight * ((inputSimilarity + 1.0) * 0.5)) + (normalizedVelocityWeight * (1.0 - velocityDistance));
                score = score * trajectoryMemory[i][3].clip(0.10, 1.0);
                scores[i] = score.clip(0.0, 1.0);
                trajectoryNovelty = trajectoryNovelty.min(((1.0 - ((inputSimilarity + 1.0) * 0.5)) * 0.60) + (velocityDistance * 0.40));
            });

            maxScore = scores.maxItem;
            sharpScores = FloatArray.fill(scores.size, { |i| ((scores[i] - maxScore) / trajectoryRetrievalTemperature.max(0.05)).exp; });
            sharpTotal = sharpScores.sum.max(0.000000001);
            scores = FloatArray.fill(sharpScores.size, { |i| sharpScores[i] / sharpTotal });

            contextVelocity = this.zeroVector(inputSize);
            contextAcceleration = this.zeroVector(inputSize);
            selected = Array.new;
            used = Array.fill(trajectoryCount, { false });
            selectedTotal = 0.0;
            n = trajectoryRecallSize.min(trajectoryCount);

            n.do({
                bestIndex = 0; bestValue = -1.0;
                trajectoryCount.do({ |i|
                    if(used[i].not and: { scores[i] > bestValue }, { bestValue = scores[i]; bestIndex = i; });
                });
                used[bestIndex] = true;
                selected.add(bestIndex);
                selectedTotal = selectedTotal + scores[bestIndex];
            });

            selectedTotal = selectedTotal.max(0.000000001);
            selected.do({ |index|
                var w = scores[index] / selectedTotal;
                inputSize.do({ |d|
                    contextVelocity[d] = contextVelocity[d] + (w * trajectoryMemory[index][1][d]);
                    contextAcceleration[d] = contextAcceleration[d] + (w * trajectoryMemory[index][2][d]);
                });
                trajectoryMemory[index][5] = ((trajectoryMemory[index][5] * 0.95) + 0.05).clip(0.0, 1.0);
            });

            trajectoryVelocityContext = contextVelocity;
            trajectoryAccelerationContext = contextAcceleration;
            trajectoryRecall = selectedTotal.clip(0.0, 1.0);
            trajectoryIndices = selected.asArray;
            trajectoryWeights = scores.copy;
            ^[contextVelocity.copy, contextAcceleration.copy, trajectoryRecall, trajectoryNovelty.clip(0.0, 1.0), trajectoryIndices.copy, trajectoryWeights.copy];
        });
    }

    writeTrajectoryMemory { |input, target, surpriseValue|
        var x, y, velocity, acceleration, novelty, importance, record, index, bestImportance;
        x = this.cleanInputVector(input);
        y = this.expandOutputToInput(target, x);
        velocity = FloatArray.fill(inputSize, { |i| this.maskedDifference(x[i], y[i], i); });
        acceleration = if(lastLearnedVelocity.isArray, {
            FloatArray.fill(inputSize, { |i| velocity[i] - lastLearnedVelocity[i] });
        }, { this.zeroVector(inputSize) });

        novelty = trajectoryNovelty.clip(0.0, 1.0);
        importance = (((1.0 - trajectoryNoveltyWeight) * surpriseValue.clip(0.0, 1.0)) + (trajectoryNoveltyWeight * novelty)).clip(0.0, 1.0);
        record = [x.copy, velocity.copy, acceleration.copy, importance.max(0.10), 0, 1.0];

        if(trajectoryCount < trajectoryMemorySize, {
            index = trajectoryCount;
            trajectoryMemory.add(record);
            trajectoryCount = trajectoryCount + 1;
        }, {
            bestImportance = trajectoryMemory[0][3];
            index = 0;
            trajectoryCount.do({ |i|
                if(trajectoryMemory[i][3] < bestImportance, {
                    bestImportance = trajectoryMemory[i][3];
                    index = i;
                });
            });
            trajectoryMemory[index] = record;
        });
        trajectoryWriteScore = importance;
    }

    consolidateTrajectoryMemory {
        trajectoryMemory.do({ |item|
            item[4] = item[4] + 1;
            item[3] = (item[3] * trajectoryDecay).clip(0.05, 1.0);
            item[5] = item[5] * trajectoryUsageDecay;
        });
    }

    trajectoryMemoryTest { |input|
        var x, velocity, r;
        x = this.cleanVector(input);
        velocity = if(lastLearnedVelocity.isArray, { lastLearnedVelocity.copy }, { this.zeroVector(inputSize) });
        r = this.retrieveTrajectory(x, velocity);
        ^(trajectoryCount: trajectoryCount, recall: r[2], novelty: r[3], indices: r[4].copy, weights: r[5].copy, velocityContext: r[0].copy, accelerationContext: r[1].copy);
    }

buildForward { |input, requestedWindowSize, requestedVelocity|
        ^{
            var x, h0, h, retrieved, memory, maxMemory, headContexts, headWeights, headQueries, headKeys, headValues, headScores, headFeatureVectors, featureGates, attentionCombined, attentionLinear, expertHidden, expertOutputs, routerLogits, router, expertCombined, residualPre, residual, deltaRaw, delta, prediction, recallCount, currentVelocity, trajectoryResult, trajectoryVelocity, trajectoryAcceleration, trajectoryRecall, trajectoryNovelty, trajectoryIndices, trajectoryWeights, deltaBias, activeWindowSize, attentionScale;

            x = this.applyMultiResolutionContext(this.prepareRealtimeInput(input, false));
            activeWindowSize = if(requestedWindowSize.isNil, { windowSize }, { requestedWindowSize.clip(1, windowSize).asInteger });

            // DYNAMIC TRAJECTORY RETRIEVAL
            currentVelocity = if(requestedVelocity.isArray, {
                this.cleanSizedVelocity(requestedVelocity, inputSize);
            }, {
                if(lastLearnedVelocity.isArray, { lastLearnedVelocity.copy }, { this.zeroVector(inputSize) });
            });

            trajectoryResult = this.retrieveTrajectory(x, currentVelocity);
            trajectoryVelocity = trajectoryResult[0].copy;
            trajectoryAcceleration = trajectoryResult[1].copy;
            trajectoryRecall = trajectoryResult[2];
            trajectoryNovelty = trajectoryResult[3];
            trajectoryIndices = trajectoryResult[4].copy;
            trajectoryWeights = trajectoryResult[5].copy;

            // LONG MEMORY RETRIEVAL
            retrieved = this.retrieveMemory(x);
            memoryContext = retrieved[\context];
            recallCount = retrieved[\indices].size;

            // INPUT PROJECTION
            preHidden = this.addVector(this.matrixVector(inputProjection, x), inputBias);
            h0 = FloatArray.fill(hiddenSize, { |i|
                var mc = if(i < inputSize, { memoryContext[i] }, { 0.0 });
                this.tanhSafe(preHidden[i] + (memoryRetrievalGain * mc) + (trajectoryRetrievalGain * trajectoryVelocity[i % inputSize]) + ((trajectoryRetrievalGain * 0.50) * trajectoryAcceleration[i % inputSize]));
            });
            h = this.copyVector(h0);

            // TEMPORAL MEMORY
            memory = List.new;
            if(shortMemory.size > activeWindowSize, {
                shortMemory.copyRange(shortMemory.size - activeWindowSize, shortMemory.size - 1).do({ |item| memory.add(item); });
            }, {
                shortMemory.do({ |item| memory.add(item); });
            });
            retrieved[\indices].do({ |index| memory.add(longMemory[index]); });
            if(memory.size > activeWindowSize, {
                memory = memory.copyRange(memory.size - activeWindowSize, memory.size - 1);
            });
            maxMemory = memory.size;
            if(maxMemory == 0, { memory.add(x); maxMemory = 1; });

            // HEADS
            // Cache sigmoid gates once. featureGateLogits are constant during buildForward.
            featureGates = Array.fill(numHeads, { |head|
                FloatArray.fill(inputSize, { |i|
                    this.sigmoid(featureGateLogits[head][i]);
                });
            });
            attentionScale = headSize.sqrt.reciprocal;
            headContexts = Array.newClear(numHeads);
            headWeights = Array.newClear(numHeads);
            headQueries = Array.newClear(numHeads);
            headKeys = Array.newClear(numHeads);
            headValues = Array.newClear(numHeads);
            headScores = Array.newClear(numHeads);
            headFeatureVectors = Array.newClear(numHeads);

            numHeads.do({ |head|
                var gatedInput, q, scores, weights, context, keys, values;
                gatedInput = FloatArray.fill(inputSize, { |i| x[i] * featureGates[head][i] });
                headFeatureVectors[head] = gatedInput;
                q = this.addVector(this.matrixVector(qWeights[head], gatedInput), qBias[head]);
                keys = Array.newClear(maxMemory);
                values = Array.newClear(maxMemory);
                scores = Array.newClear(maxMemory);

                maxMemory.do({ |j|
                    var mem, gatedMemory, k, v;
                    mem = memory[j];
                    gatedMemory = FloatArray.fill(inputSize, { |i| mem[i] * featureGates[head][i] });
                    k = this.addVector(this.matrixVector(kWeights[head], gatedMemory), kBias[head]);
                    v = this.addVector(this.matrixVector(vWeights[head], gatedMemory), vBias[head]);
                    keys[j] = k; values[j] = v;
                    scores[j] = (this.dot(q, k) * attentionScale) + (if(j < windowSize, { positionBias[head][j] }, { 0.0 }));
                });
                weights = this.softmaxWithTemperature(scores, attentionTemperature);
                context = FloatArray.fill(headSize, { |d|
                    var s = 0.0;
                    maxMemory.do({ |j| s = s + (weights[j] * values[j][d]); });
                    s;
                });
                headQueries[head] = q; headKeys[head] = keys; headValues[head] = values;
                headScores[head] = scores; headWeights[head] = weights; headContexts[head] = context;
            });

            // CONCATENATE HEADS
            attentionCombined = FloatArray.fill(numHeads * headSize, { |i|
                var head = i.div(headSize);
                var dim = i % headSize;
                headContexts[head][dim];
            });
            attentionLinear = this.addVector(this.matrixVector(outputAttentionWeight, attentionCombined), outputAttentionBias);

            // ROUTER
            routerLogits = this.addVector(this.matrixVector(routerWeights, h0), routerBias);
            router = this.softmaxWithTemperature(routerLogits, routerTemperature);
            expertBalanceLoss = 0.0;
            numExperts.do({ |e|
                var balanceDifference;
                balanceDifference = router[e] - (1.0 / numExperts);
                expertBalanceLoss = expertBalanceLoss + (balanceDifference * balanceDifference);
            });
            expertBalanceLoss = expertBalanceLoss / numExperts.max(1);

            // EXPERTS
            expertHidden = Array.newClear(numExperts);
            expertOutputs = Array.newClear(numExperts);
            numExperts.do({ |e|
                var eh, eo;
                eh = this.addVector(this.matrixVector(expertW1[e], h0), expertB1[e]);
                eh = FloatArray.fill(hiddenSize, { |i| this.tanhSafe(eh[i]); });
                eo = this.addVector(this.matrixVector(expertW2[e], eh), expertB2[e]);
                expertHidden[e] = eh; expertOutputs[e] = eo;
            });
            expertCombined = FloatArray.fill(hiddenSize, { |d|
                var s = 0.0;
                numExperts.do({ |e| s = s + (router[e] * expertOutputs[e][d]); });
                s;
            });

            // RESIDUAL
            residualPre = FloatArray.fill(hiddenSize, { |i|
                h0[i] + (residualScale * attentionLinear[i]) + (expertScale * expertCombined[i]);
            });
            residual = FloatArray.fill(hiddenSize, { |i| this.tanhSafe(residualPre[i]); });

            // DELTA
            deltaRaw = this.addVector(this.matrixVector(outputProjection, residual), outputBias);
            delta = FloatArray.fill(outputSize, { |i|
                ((deltaScale * this.tanhSafe(deltaRaw[i])) + (trajectoryVelocityGain * trajectoryVelocity[i].clip(trajectoryVelocityClip.neg, trajectoryVelocityClip)) + (trajectoryAccelerationGain * trajectoryAcceleration[i].clip(trajectoryAccelerationClip.neg, trajectoryAccelerationClip))).clip(-0.65, 0.65);
            });
            prediction = FloatArray.fill(outputSize, { |i| this.maskedWrap(x[i] + delta[i], i); });

            ^(
                input: x, h0: h0, residualPre: residualPre, residual: residual, memory: memory, memorySize: maxMemory,
                memoryContext: memoryContext.copy, memoryRecallCount: recallCount, memoryWeights: retrieved[\weights],
                headQueries: headQueries, headKeys: headKeys, headValues: headValues, headScores: headScores,
                headWeights: headWeights, headContexts: headContexts, headFeatureVectors: headFeatureVectors,
                featureGates: featureGates,
                attentionCombined: attentionCombined, attentionLinear: attentionLinear, router: router, routerLogits: routerLogits,
                expertHidden: expertHidden, expertOutputs: expertOutputs, expertCombined: expertCombined,
                trajectoryVelocity: trajectoryVelocity.copy, trajectoryAcceleration: trajectoryAcceleration.copy,
                trajectoryRecall: trajectoryRecall, trajectoryNovelty: trajectoryNovelty,
                trajectoryIndices: trajectoryIndices.copy, trajectoryWeights: trajectoryWeights.copy,
                deltaRaw: deltaRaw, delta: delta, prediction: prediction
            );
        }.value;
    }

    addToShortMemory { |x|
        ^{
            var cleanX = this.cleanVector(x);
            shortMemory.add(this.copyVector(cleanX));
            if(shortMemory.size > windowSize, { shortMemory.removeAt(0); });
        }.value;
    }

    learnPairCore { |input, target, allowMemory=true|
        ^{
            var state, x, y, prediction, targetDelta, predDelta, localLoss, localDeltaLoss, gradPrediction, gradDelta, gradDeltaRaw, gradResidual, gradResidualPre, gradAttentionLinear, gradAttentionCombined, gradExpertCombined, gradH0, gradPreHidden, gradInputProjection, gradInputBias, gradOutputProjection, gradOutputBias, gradRouterWeights, gradRouterBias, gradRouterLogits, gradExpertW1, gradExpertB1, gradExpertW2, gradExpertB2, gradFeatureGates, gradQ, gradK, gradV, gradQB, gradKB, gradVB, gradAttentionWeight, gradAttentionBias, gradPositionBias, gradRouter, gradExpertOutput, gradExpertHidden, head, expert, i, j, d, specializationPenalty, surpriseValue, rateScale, replayMode;

            x = this.cleanInputVector(input);
            y = this.cleanOutputVector(target);
            replayMode = allowMemory.not;
            state = this.buildForward(x, nil, lastLearnedVelocity);
            prediction = state[\prediction];

            // TARGET DELTA
            targetDelta = FloatArray.fill(outputSize, { |index| this.maskedDifference(x[index], y[index], index); });
            predDelta = state[\delta];

            // LOSS
            localLoss = this.maskedMSE(prediction, y);
            localDeltaLoss = this.vectorMSE(predDelta, targetDelta);
            trajectoryLoss = this.vectorMSE(state[\trajectoryVelocity].copyRange(0, outputSize - 1), targetDelta);
            directionLoss = this.vectorDirectionLoss(predDelta, targetDelta);
            outputLoss = localLoss; deltaLoss = localDeltaLoss;
            loss = (predictionLossWeight * localLoss) + (deltaLossWeight * localDeltaLoss) + (directionLossWeight * directionLoss);
            surpriseValue = localLoss.sqrt.clip(0.0, 1.0);

            if(replayMode.not, {
                surprise = surpriseValue;
                surpriseEMA = (0.95 * surpriseEMA) + (0.05 * surprise);
                errorEMA = (0.95 * errorEMA) + (0.05 * localLoss);
            });

            // ADAPTATION
            rateScale = adaptationSlowRate + ((adaptationFastRate - adaptationSlowRate) * surpriseValue.sqrt);
            rateScale = rateScale * (1.0 / (1.0 + protectionStrength * protectionScalar));
            learningRateCurrent = (learningRate * rateScale).clip(learningRate * 0.05, learningRate * 3.0);
            if(surpriseValue > surpriseThreshold, {
                var surprisePressure = ((surpriseValue - surpriseThreshold) / (1.0 - surpriseThreshold).max(0.000001)).clip(0.0, 1.0);
                learningRateCurrent = (learningRateCurrent * (1.0 + ((surpriseGain - 1.0) * surprisePressure))).clip(learningRate * 0.05, learningRate * 3.0);
            });

            // Reuse persistent gradient buffers. They are strictly local to this model instance
            // and are cleared before every learnPairCore call.
            this.clearGradientBuffers;
            gradInputProjection = gradientBuffers[\inputProjection];
            gradInputBias = gradientBuffers[\inputBias];
            gradOutputProjection = gradientBuffers[\outputProjection];
            gradOutputBias = gradientBuffers[\outputBias];
            gradRouterWeights = gradientBuffers[\routerWeights];
            gradRouterBias = gradientBuffers[\routerBias];
            gradExpertW1 = gradientBuffers[\expertW1];
            gradExpertB1 = gradientBuffers[\expertB1];
            gradExpertW2 = gradientBuffers[\expertW2];
            gradExpertB2 = gradientBuffers[\expertB2];
            gradQ = gradientBuffers[\q];
            gradK = gradientBuffers[\k];
            gradV = gradientBuffers[\v];
            gradQB = gradientBuffers[\qBias];
            gradKB = gradientBuffers[\kBias];
            gradVB = gradientBuffers[\vBias];
            gradFeatureGates = gradientBuffers[\featureGates];
            gradAttentionWeight = gradientBuffers[\attentionWeight];
            gradAttentionBias = gradientBuffers[\attentionBias];
            gradPositionBias = gradientBuffers[\positionBias];
            gradResidual = gradientBuffers[\residual];
            gradResidualPre = gradientBuffers[\residualPre];
            gradAttentionLinear = gradientBuffers[\attentionLinear];
            gradAttentionCombined = gradientBuffers[\attentionCombined];
            gradExpertCombined = gradientBuffers[\expertCombined];
            gradH0 = gradientBuffers[\h0];
            gradPreHidden = gradientBuffers[\preHidden];
            gradRouterLogits = gradientBuffers[\routerLogits];
            gradRouter = gradientBuffers[\router];
            gradExpertOutput = gradientBuffers[\expertOutput];
            gradExpertHidden = gradientBuffers[\expertHidden];

            // OUTPUT GRADIENT
            gradPrediction = FloatArray.fill(outputSize, { |index|
                predictionLossWeight * 2.0 * (this.maskedDifference(y[index], prediction[index], index)) / outputSize;
            });
            gradDelta = FloatArray.fill(outputSize, { |index|
                var sp = (12.0 * predDelta[index]).tanh;
                var st = (12.0 * targetDelta[index]).tanh;
                var directionGradient = (24.0 * (sp - st) * (1.0 - (sp * sp)) / outputSize);
                gradPrediction[index] + (deltaLossWeight * 2.0 * (predDelta[index] - targetDelta[index]) / outputSize) + (directionLossWeight * directionGradient);
            });
            gradDeltaRaw = FloatArray.fill(outputSize, { |index|
                gradDelta[index] * deltaScale * this.tanhDerivative(this.tanhSafe(state[\deltaRaw][index]));
            });

            // OUTPUT MATRIX BACKPROP
            outputSize.do({ |r|
                hiddenSize.do({ |c|
                    gradOutputProjection[r][c] = gradDeltaRaw[r] * state[\residual][c];
                });
                gradOutputBias[r] = gradDeltaRaw[r];
            });
            hiddenSize.do({ |c|
                var sum;
                sum = 0.0;
                outputSize.do({ |r|
                    sum = sum + (outputProjection[r][c] * gradDeltaRaw[r]);
                });
                gradResidual[c] = sum;
            });

            // RESIDUAL BACKPROP
            hiddenSize.do({ |i|
                gradResidualPre[i] = gradResidual[i] * this.tanhDerivative(state[\residual][i]);
                gradH0[i] = gradResidualPre[i];
                gradAttentionLinear[i] = residualScale * gradResidualPre[i];
                gradExpertCombined[i] = expertScale * gradResidualPre[i];
            });

            // ATTENTION OUTPUT BACKPROP
            hiddenSize.do({ |r|
                numHeads.do({ |h|
                    headSize.do({ |d|
                        var col = (h * headSize) + d;
                        gradAttentionCombined[col] = gradAttentionCombined[col] + (outputAttentionWeight[r][col] * gradAttentionLinear[r]);
                        gradAttentionWeight[r][col] = gradAttentionLinear[r] * state[\attentionCombined][col];
                    });
                });
                gradAttentionBias[r] = gradAttentionLinear[r];
            });

            // MOE BACKPROP
            numExperts.do({ |e|
                hiddenSize.do({ |d|
                    gradExpertOutput[e][d] = gradExpertCombined[d] * state[\router][e];
                    gradRouter[e] = gradRouter[e] + (gradExpertCombined[d] * state[\expertOutputs][e][d]);
                });
            });

            // EXPERT BALANCE GRADIENT
            numExperts.do({ |e|
                gradRouter[e] = gradRouter[e] + (expertBalanceStrength * 2.0 * (state[\router][e] - (1.0 / numExperts)) / numExperts.max(1));
            });

            // ROUTER SOFTMAX BACKPROP
            numExperts.do({ |e|
                var s = 0.0;
                numExperts.do({ |k|
                    if(k == e, {
                        s = s + (gradRouter[k] * state[\router][e] * (1.0 - state[\router][k]));
                    }, {
                        s = s - (gradRouter[k] * state[\router][k] * state[\router][e]);
                    });
                });
                gradRouterLogits[e] = s / routerTemperature.max(0.01);
            });
            numExperts.do({ |e|
                hiddenSize.do({ |c|
                    gradRouterWeights[e][c] = gradRouterLogits[e] * state[\h0][c];
                });
                gradRouterBias[e] = gradRouterLogits[e];
                hiddenSize.do({ |i|
                    gradH0[i] = gradH0[i] + (routerWeights[e][i] * gradRouterLogits[e]);
                });
            });

            // EXPERTS BACKPROP
            numExperts.do({ |e|
                hiddenSize.do({ |i|
                    var g = gradExpertOutput[e][i];
                    gradExpertB2[e][i] = g;
                    hiddenSize.do({ |j|
                        gradExpertW2[e][i][j] = g * state[\expertHidden][e][j];
                        gradExpertHidden[e][j] = gradExpertHidden[e][j] + (expertW2[e][i][j] * g);
                    });
                });
                hiddenSize.do({ |i|
                    gradExpertHidden[e][i] = gradExpertHidden[e][i] * this.tanhDerivative(state[\expertHidden][e][i]);
                    gradExpertB1[e][i] = gradExpertHidden[e][i];
                    hiddenSize.do({ |j|
                        gradExpertW1[e][i][j] = gradExpertHidden[e][i] * state[\h0][j];
                        gradH0[j] = gradH0[j] + (expertW1[e][i][j] * gradExpertHidden[e][i]);
                    });
                });
            });

            // ATTENTION BACKPROP (Simplified for brevity, logic preserved)
            numHeads.do({ |hh|
                var q = state[\headQueries][hh];
                var weights = state[\headWeights][hh];
                var keys = state[\headKeys][hh];
                var values = state[\headValues][hh];
                var gatedInput = state[\headFeatureVectors][hh];
                var gradQHead = this.zeroVector(headSize);
                var gradWeightsHead = this.zeroVector(state[\memorySize]);
                var gradScoresHead = this.zeroVector(state[\memorySize]);
                var gradContextHead = FloatArray.fill(headSize, { |dd|
                    var col = (hh * headSize) + dd;
                    gradAttentionCombined[col];
                });

                state[\memorySize].do({ |jj|
                    headSize.do({ |dd|
                        gradWeightsHead[jj] = gradWeightsHead[jj] + (gradContextHead[dd] * values[jj][dd]);
                    });
                });

                state[\memorySize].do({ |jj|
                    var s = 0.0;
                    state[\memorySize].do({ |kk|
                        if(jj == kk, {
                            s = s + (gradWeightsHead[kk] * weights[jj] * (1.0 - weights[kk]));
                        }, {
                            s = s - (gradWeightsHead[kk] * weights[kk] * weights[jj]);
                        });
                    });
                    gradScoresHead[jj] = s / attentionTemperature.max(0.01);
                    if(jj < windowSize, { gradPositionBias[hh][jj] = gradScoresHead[jj]; });
                });

                state[\memorySize].do({ |jj|
                    var scale = 1.0 / headSize.sqrt;
                    headSize.do({ |dd|
                        gradQHead[dd] = gradQHead[dd] + (gradScoresHead[jj] * keys[jj][dd] * scale);
                        headSize.do({ |dd| // Nested loop correction for K
                             var keyGrad = gradScoresHead[jj] * q[dd] * scale;
                             inputSize.do({ |ii|
                                var memValue = state[\memory][jj][ii] * state[\featureGates][hh][ii];
                                gradK[hh][dd][ii] = gradK[hh][dd][ii] + (keyGrad * memValue);
                                gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] + (keyGrad * kWeights[hh][dd][ii] * state[\memory][jj][ii]);
                            });
                            gradKB[hh][dd] = gradKB[hh][dd] + keyGrad;
                        });
                    });
                });

                headSize.do({ |dd|
                    inputSize.do({ |ii|
                        var gatedX = state[\input][ii] * state[\featureGates][hh][ii];
                        gradQ[hh][dd][ii] = gradQ[hh][dd][ii] + (gradQHead[dd] * gatedX);
                        gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] + (gradQHead[dd] * qWeights[hh][dd][ii] * state[\input][ii]);
                    });
                    gradQB[hh][dd] = gradQB[hh][dd] + gradQHead[dd];
                });

                state[\memorySize].do({ |jj|
                    headSize.do({ |dd|
                        var gv = gradContextHead[dd] * weights[jj];
                        gradVB[hh][dd] = gradVB[hh][dd] + gv;
                        inputSize.do({ |ii|
                            var memValue = state[\memory][jj][ii] * state[\featureGates][hh][ii];
                            gradV[hh][dd][ii] = gradV[hh][dd][ii] + (gv * memValue);
                            gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] + (gv * vWeights[hh][dd][ii] * state[\memory][jj][ii]);
                        });
                    });
                });
            });

            // FEATURE GATES BACKPROP
            numHeads.do({ |hh|
                inputSize.do({ |ii|
                    var g = gradFeatureGates[hh][ii];
                    var sg = state[\featureGates][hh][ii];
                    gradFeatureGates[hh][ii] = g * this.sigmoidDerivative(sg);
                });
            });

            // INPUT BACKPROP
            hiddenSize.do({ |r|
                gradPreHidden[r] = gradH0[r] * this.tanhDerivative(state[\h0][r]);
                gradInputBias[r] = gradPreHidden[r];
                inputSize.do({ |c|
                    gradInputProjection[r][c] = gradPreHidden[r] * state[\input][c];
                });
            });

            // HEAD SPECIALIZATION
            numHeads.do({ |hh|
                numHeads.do({ |kk|
                    if(hh < kk, {
                        inputSize.do({ |ii|
                            var a = state[\featureGates][hh][ii];
                            var b = state[\featureGates][kk][ii];
                            var diff = a - b;
                            if(diff.abs > (1.0 - headSpecializationThreshold), {
                                gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] - (headSpecializationStrength * diff);
                                gradFeatureGates[kk][ii] = gradFeatureGates[kk][ii] + (headSpecializationStrength * diff);
                            });
                        });
                    });
                });
            });

            // ADAM UPDATES
            adamStep = adamStep + 1;
            this.adamMatrix(inputProjection, gradInputProjection, adamMInput, adamVInput);
            this.adamVector(inputBias, gradInputBias, adamMInputBias, adamVInputBias);
            this.adamMatrix(outputProjection, gradOutputProjection, adamMOutput, adamVOutput);
            this.adamVector(outputBias, gradOutputBias, adamMOutputBias, adamVOutputBias);
            numHeads.do({ |hh|
                this.adamMatrix(qWeights[hh], gradQ[hh], adamMQ[hh], adamVQ[hh]);
                this.adamMatrix(kWeights[hh], gradK[hh], adamMK[hh], adamVK[hh]);
                this.adamMatrix(vWeights[hh], gradV[hh], adamMV[hh], adamVV[hh]);
                this.adamVector(qBias[hh], gradQB[hh], adamMBQ[hh], adamVBQ[hh]);
                this.adamVector(kBias[hh], gradKB[hh], adamMBK[hh], adamVBK[hh]);
                this.adamVector(vBias[hh], gradVB[hh], adamMBV[hh], adamVBV[hh]);
                this.adamVector(featureGateLogits[hh], gradFeatureGates[hh], adamMGates[hh], adamVGates[hh], gateLearningRate);
                this.adamVector(positionBias[hh], gradPositionBias[hh], adamMPosition[hh], adamVPosition[hh]);
            });
            this.adamMatrix(outputAttentionWeight, gradAttentionWeight, adamMAttention, adamVAttention);
            this.adamVector(outputAttentionBias, gradAttentionBias, adamMBAttention, adamVBAttention);
            this.adamMatrix(routerWeights, gradRouterWeights, adamMRouter, adamVRouter);
            this.adamVector(routerBias, gradRouterBias, adamMBRouter, adamVBRouter);
            numExperts.do({ |ee|
                this.adamMatrix(expertW1[ee], gradExpertW1[ee], adamMExpertW1[ee], adamVExpertW1[ee]);
                this.adamVector(expertB1[ee], gradExpertB1[ee], adamMExpertB1[ee], adamVExpertB1[ee]);
                this.adamMatrix(expertW2[ee], gradExpertW2[ee], adamMExpertW2[ee], adamVExpertW2[ee]);
                this.adamVector(expertB2[ee], gradExpertB2[ee], adamMExpertB2[ee], adamVExpertB2[ee]);
            });

            // DIAGNOSTICS
            attentionProfile = FloatArray.fill(windowSize, { 0.0 });
            numHeads.do({ |hh|
                state[\memorySize].do({ |jj|
                    if(jj < windowSize, {
                        attentionProfile[jj] = attentionProfile[jj] + (state[\headWeights][hh][jj] / numHeads);
                    });
                });
            });
            headActivity = FloatArray.fill(numHeads, { |hh| state[\headWeights][hh].maxItem; });
            headFeatureUsage = Array.fill(numHeads, { |hh| FloatArray.fill(inputSize, { |ii| this.getFeatureGate(hh, ii); }) });
            expertUsage = state[\router].copy;
            this.updateExpertDiagnostics(expertUsage);

            // MEMORY UPDATE
            if(replayMode.not, {
                memoryNovelty = this.calculateMemoryNovelty(x);
                this.writeReplay(x, y, ((0.60 * surpriseValue) + (0.40 * memoryNovelty)).clip(0.0, 1.0));
                this.writeLongMemory(x, y, surpriseValue);
                this.writeTrajectoryMemory(x, y, surpriseValue);
                this.consolidateTrajectoryMemory;
                lastLearnedVelocity = this.expandOutputDelta(targetDelta);
                this.addToShortMemory(y);
                this.consolidateMemory;
                if(memoryCount > 0, {
                    protectionScalar = this.vectorMean(longMemoryImportance.copyRange(0, memoryCount - 1));
                }, { protectionScalar = 0.0; });
                learnedEvents = learnedEvents + 1;
                totalEvents = totalEvents + 1;
                lastInput = this.copyVector(x);
                lastTarget = this.copyVector(y);
                lastPrediction = this.copyVector(prediction);
                lastDelta = this.copyVector(predDelta);
            });
            if(replayMode.not, { this.metaLearnStep; });
            ^loss;
        }.value;
    }

    updateAdaptiveInterference { |measuredConflict|
        var measured, pressure, targetReplay;
        measured = this.clean(measuredConflict).max(0.0);
        adaptiveState[\interferenceEMA] = (adaptiveState[\smoothing] * adaptiveState[\interferenceEMA]) + ((1.0 - adaptiveState[\smoothing]) * measured);
        if(adaptiveState[\enabled], {
            pressure = ((adaptiveState[\interferenceEMA] - adaptiveState[\threshold]) / adaptiveState[\threshold].max(0.000001)).clip(0.0, 1.0);
            targetReplay = (replayRate + (adaptiveState[\replayBoost] * pressure)).clip(adaptiveState[\replayMin], adaptiveState[\replayMax]);
            adaptiveState[\replayRate] = (0.80 * adaptiveState[\replayRate]) + (0.20 * targetReplay);
            if(pressure > 0.0, { adaptiveState[\adjustments] = adaptiveState[\adjustments] + 1; });
        }, { adaptiveState[\replayRate] = replayRate; });
        interferenceScore = adaptiveState[\interferenceEMA];
        ^interferenceScore;
    }

    learnPair { |input, target|
        var result, conflictSignal, shouldReplay;
        if(unifiedRuntimeRole.not and: { unifiedLearningEnabled.not }, { ^0.0 });
        result = this.learnPairCore(input, target, true);
        this.stabilityGuardAfter(result);
        shouldReplay = replayCount > 1 and: { adaptiveState[\replayRate].coin };
        if(shouldReplay, { this.replayMemory; });
        conflictSignal = if(replayCount > 1, { (replayLoss - result).max(0.0); }, { 0.0 });
        this.updateAdaptiveInterference(conflictSignal);
        ^result;
    }

    replayMemory {
        ^{
            var n, indices, index, localReplayLoss=0.0, count=0, used, priorityScores, totalPriority, threshold, cumulative, chosen, pairLoss;
            if(replayCount > 0, {
                n = replayBatchSize.min(replayCount);
                indices = Array.new;
                used = Array.fill(replayCount, { false });
                n.do({
                    priorityScores = FloatArray.fill(replayCount, { |i|
                        if(used[i], { 0.0 }, { (replayPriorityMix * replayImportance[i].clip(0.001, 1.0).pow(1.35)) + (replayUniformMix * 1.0); });
                    });
                    totalPriority = priorityScores.sum.max(0.000001);
                    threshold = 1.0.rand * totalPriority;
                    cumulative = 0.0;
                    chosen = replayCount - 1;
                    replayCount.do({ |i|
                        if(cumulative < threshold, {
                            cumulative = cumulative + priorityScores[i];
                            if(cumulative >= threshold, { chosen = i; });
                        });
                    });
                    used[chosen] = true;
                    indices.add(chosen);
                });
                indices.do({ |idx|
                    index = idx;
                    pairLoss = this.learnPairCore(replayInputs[index], replayTargets[index], false);
                    localReplayLoss = localReplayLoss + pairLoss;
                    replayImportance[index] = ((0.82 * replayImportance[index]) + (0.18 * pairLoss.sqrt.clip(0.0, 1.0))).clip(0.0, 1.0);
                    count = count + 1;
                });
                if(count > 0, { replayLoss = localReplayLoss / count; });
            });
        }.value;
    }

    predict { |input, requestedWindowSize, requestedVelocity|
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^nil });
            ^unifiedCurrentRuntime.predict(input, requestedWindowSize, requestedVelocity);
        });
        ^{
            var state = this.buildForward(this.prepareRealtimeInput(input, false), requestedWindowSize, requestedVelocity);
            lastInput = this.copyVector(state[\input]);
            lastPrediction = this.copyVector(state[\prediction]);
            lastDelta = this.copyVector(state[\delta]);
            memoryContext = state[\memoryContext].copy;
            attentionProfile = FloatArray.fill(windowSize, { 0.0 });
            numHeads.do({ |hh|
                state[\memorySize].do({ |jj|
                    if(jj < windowSize, { attentionProfile[jj] = attentionProfile[jj] + (state[\headWeights][hh][jj] / numHeads); });
                });
            });
            headActivity = FloatArray.fill(numHeads, { |hh| state[\headWeights][hh].maxItem; });
            headFeatureUsage = Array.fill(numHeads, { |hh| state[\featureGates][hh].copy; });
            expertUsage = state[\router].copy;
            this.updateExpertDiagnostics(expertUsage);
            ^lastPrediction;
        }.value;
    }

    process { |input| ^this.predict(input); }
    learn { |input, target| ^this.learnPair(input, target); }

    learnEvent { |event|
        if(unifiedRuntimeRole.not and: { unifiedLearningEnabled.not }, { ^0.0 });
        ^{
            var x, result=0.0, n, currentIndex;
            x = this.prepareRealtimeInput(event, true);
            eventHistory.add(x.copy);
            if(eventHistory.size > 32, { eventHistory.removeAt(0); });
            if(totalEvents == 0, {
                lastInput = x.copy; lastTarget = x.copy; lastPrediction = x.copy;
                lastDelta = this.zeroVector(outputSize);
                lastLearnedVelocity = this.zeroVector(inputSize);
                if(lastGeneratedVelocity.isArray.not, { lastGeneratedVelocity = this.zeroVector(inputSize); });
                this.addToShortMemory(x);
                totalEvents = 1;
            }, {
                result = this.learnPair(lastInput, x);
                n = eventHistory.size;
                currentIndex = n - 1;
                if((n >= 3) && { 0.15.coin }, { this.learnPairCore(eventHistory[currentIndex - 2], x, false); });
                if((n >= 4) && { 0.04.coin }, { this.learnPairCore(eventHistory[currentIndex - 3], x, false); });
                if((n >= 5) && { 0.01.coin }, { this.learnPairCore(eventHistory[currentIndex - 4], x, false); });
                lastInput = x.copy; lastTarget = x.copy; lastPrediction = x.copy;
            });
            ^result;
        }.value;
    }

    updateGenerationDiversity { |candidate|
        ^{
            var minDistance, totalDistance=0.0, count=0, distance;
            if(generationHistory.size == 0, {
                generationNovelty = 1.0; generationDiversity = 0.0;
            }, {
                minDistance = 1.0;
                generationHistory.do({ |old|
                    distance = this.memoryDistance(candidate, old);
                    minDistance = minDistance.min(distance);
                    totalDistance = totalDistance + distance;
                    count = count + 1;
                });
                generationMinDistance = minDistance.clip(0.0, 1.0);
                generationMeanDistance = (totalDistance / count.max(1)).clip(0.0, 1.0);
                generationNovelty = generationMinDistance;
                generationDiversity = generationMeanDistance;
            });
            generationPressure = (1.0 - generationNovelty).clip(0.0, 1.0);
            ^[generationDiversity, generationNovelty];
        }.value;
    }

    applyLocalContractionCompensation { |candidate|
        ^{
            var n, startIndex, recent, pairDistance, pairCount=0, i, j, meanDistance=0.0, centroid, direction, magnitude, velocityMagnitude, correctionGain, corrected;
            n = generationHistory.size.min(localDiversityWindow);
            if(n < 2, {
                localDiversity = 0.0; localContractionPressure = 0.0; localContractionCorrection = 0.0;
                ^candidate.copy;
            }, {
                startIndex = generationHistory.size - n;
                recent = Array.fill(n, { |k| generationHistory[startIndex + k]; });
                i = 0;
                while({ i < n }, {
                    j = i + 1;
                    while({ j < n }, {
                        pairDistance = this.memoryDistance(recent[i], recent[j]);
                        meanDistance = meanDistance + pairDistance;
                        pairCount = pairCount + 1;
                        j = j + 1;
                    });
                    i = i + 1;
                });
                localDiversity = (meanDistance / pairCount.max(1)).clip(0.0, 1.0);
                localContractionPressure = ((localDiversityFloor - localDiversity) / localDiversityFloor.max(0.000001)).clip(0.0, 1.0);
                correctionGain = localContractionPressure * localDiversityGain;
                centroid = FloatArray.fill(outputSize, { |d|
                    var sum = 0.0;
                    recent.do({ |v| sum = sum + v[d]; });
                    sum / n.max(1);
                });
                direction = FloatArray.fill(outputSize, { |d| candidate[d] - centroid[d]; });
                magnitude = 0.0;
                direction.do({ |v| magnitude = magnitude + (v * v); });
                magnitude = magnitude.sqrt;
                if(magnitude < 0.000001, {
                    direction = FloatArray.fill(outputSize, { |d|
                        if(d < lastGeneratedVelocity.size, { lastGeneratedVelocity[d]; }, { 0.0; });
                    });
                    velocityMagnitude = 0.0;
                    direction.do({ |v| velocityMagnitude = velocityMagnitude + (v * v); });
                    velocityMagnitude = velocityMagnitude.sqrt;
                    if(velocityMagnitude < 0.000001, {
                        direction = FloatArray.fill(outputSize, { |d| if((d % 2) == 0, { 1.0 }, { -1.0 }); });
                    });
                });
                magnitude = 0.0;
                direction.do({ |v| magnitude = magnitude + (v * v); });
                magnitude = magnitude.sqrt.max(0.000001);
                corrected = FloatArray.fill(outputSize, { |d|
                    candidate[d] + (correctionGain * direction[d] / magnitude).clip(localDiversityMaxCorrection.neg, localDiversityMaxCorrection);
                });
                localContractionCorrection = correctionGain.clip(0.0, localDiversityMaxCorrection);
                ^corrected;
            });
        }.value;
    }

    generateStep { |input|
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^nil });
            ^unifiedCurrentRuntime.generateStep(input);
        });
        ^{
            var p, result, correction, distance, repulsion, noiseGain, driftCorrection, generatedDelta, cleanInput;
            cleanInput = this.cleanVector(input);
            p = this.predict(cleanInput, generationWindowSize, lastGeneratedVelocity);

            driftCorrection = FloatArray.fill(outputSize, { |i|
                var localEdgePressure=0.0, localSameDirection=false;
                if(p[i] > 0.80, {
                    localEdgePressure = ((p[i] - 0.80) / 0.20).clip(0.0, 1.0);
                    if(generationDriftState[\deltaEMA][i] > 0.0, { localSameDirection = true; });
                });
                if(p[i] < 0.20, {
                    localEdgePressure = ((0.20 - p[i]) / 0.20).clip(0.0, 1.0);
                    if(generationDriftState[\deltaEMA][i] < 0.0, { localSameDirection = true; });
                });
                if(localSameDirection, {
                    (generationDriftState[\gain] * localEdgePressure * generationDriftState[\deltaEMA][i]).clip(generationDriftState[\maxCorrection].neg, generationDriftState[\maxCorrection]);
                }, { 0.0; });
            });

            correction = FloatArray.fill(outputSize, 0.0);
            generationHistory.do({ |old|
                distance = this.memoryDistance(p, old);
                if(distance < diversityRadius, {
                    repulsion = ((diversityRadius - distance) / diversityRadius.max(0.000001)).clip(0.0, 1.0);
                    outputSize.do({ |i|
                        correction[i] = correction[i] + (diversityRepulsionGain * (1.0 + (generationPressure * diversityAdaptiveGain)) * repulsion * (p[i] - old[i]));
                    });
                });
            });

            noiseGain = diversityNoiseGain * (1.0 + (trajectoryNovelty * 0.50));
            result = FloatArray.fill(outputSize, { |i|
                this.maskedWrap(
                    p[i] - driftCorrection[i]
                    + correction[i].clip(diversityMaxCorrection.neg, diversityMaxCorrection)
                    + (trajectoryExplorationGain * (1.0 + (trajectoryNovelty * 0.75)) * 1.0.rand2)
                    + (noiseGain * 1.0.rand2),
                    i
                );
            });

            // V30.0 ONLY: SOFT LOCAL CONTRACTION COMPENSATION
            result = this.applyLocalContractionCompensation(result);
            result = FloatArray.fill(outputSize, { |i| this.maskedWrap(this.edgePush(result[i], i), i); });

            generatedDelta = FloatArray.fill(outputSize, { |i| this.maskedDifference(cleanInput[i], result[i], i); });

            generationDriftState[\deltaEMA] = FloatArray.fill(outputSize, { |i|
                (generationDriftState[\emaDecay] * generationDriftState[\deltaEMA][i])
                + ((1.0 - generationDriftState[\emaDecay]) * generatedDelta[i]);
            });

            this.updateGenerationDiversity(result);
            if(generationHistory.size >= diversityHistorySize, { generationHistory.removeAt(0); });
            generationHistory.add(result.copy);
            lastGeneratedVelocity = this.expandOutputDelta(generatedDelta);
            this.autoTuneStep;
            ^result;
        }.value;
    }

    generate { |seed, count=32| ^this.generateMapped(seed, count, nil); }

    generateMapped { |seed, count=32, feedbackFunction|
        var result, currentInput, currentOutput, feedback;
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^Array.new });
            ^unifiedCurrentRuntime.generateMapped(seed, count, feedbackFunction);
        });
        result = List.new;
        currentInput = this.cleanInputVector(seed);
        feedback = feedbackFunction;
        count.asInteger.max(0).do({
            currentOutput = this.generateStep(currentInput);
            if(currentOutput.notNil, {
                result.add(this.copyVector(currentOutput));
                currentInput = if(feedback.isNil, {
                    this.outputToNextInput(currentOutput, currentInput);
                }, {
                    this.cleanInputVector(feedback.value(currentOutput, currentInput.copy));
                });
                this.addToShortMemory(currentInput);
            });
        });
        ^result.asArray;
    }

    // ============================================================
    // V30.3.0 ADVANCED REAL-TIME CONDITIONING
    // ============================================================
    resetAdvancedRealtimeState {
        normalizationState = IdentityDictionary.new;
        normalizationState[\enabled] = false;
        normalizationState[\decay] = 0.995;
        normalizationState[\epsilon] = 0.0001;
        normalizationState[\clip] = 3.0;
        normalizationState[\blend] = 0.15;
        normalizationState[\warmup] = 16;
        normalizationState[\count] = 0;
        normalizationState[\mean] = FloatArray.fill(inputSize, 0.5);
        normalizationState[\variance] = FloatArray.fill(inputSize, 0.0625);

        multiResolutionState = IdentityDictionary.new;
        multiResolutionState[\enabled] = false;
        multiResolutionState[\midWindow] = 24;
        multiResolutionState[\slowWindow] = 96;
        multiResolutionState[\midDivider] = 4;
        multiResolutionState[\slowDivider] = 16;
        multiResolutionState[\midGain] = 0.10;
        multiResolutionState[\slowGain] = 0.05;
        multiResolutionState[\counter] = 0;
        multiResolutionState[\history] = List.new;
        multiResolutionState[\midCache] = FloatArray.fill(inputSize, 0.5);
        multiResolutionState[\slowCache] = FloatArray.fill(inputSize, 0.5);

        stabilityGuardState = IdentityDictionary.new;
        stabilityGuardState[\enabled] = false;
        stabilityGuardState[\snapshotInterval] = 128;
        stabilityGuardState[\lossLimit] = 2.0;
        stabilityGuardState[\learningRateBackoff] = 0.5;
        stabilityGuardState[\counter] = 0;
        stabilityGuardState[\rollbacks] = 0;
        stabilityGuardState[\lastReason] = \none;
        stabilityGuardState[\lastStableSnapshot] = nil;
        ^this;
    }

    rawCleanInputVector { |input| ^this.cleanSizedVector(input, inputSize); }

    updateNormalizationState { |rawInput|
        var decay, oneMinus, oldMean, delta;
        if(normalizationState.isNil, { this.resetAdvancedRealtimeState });
        decay = normalizationState[\decay].clip(0.0, 0.999999);
        oneMinus = 1.0 - decay;
        inputSize.do({ |i|
            oldMean = normalizationState[\mean][i];
            delta = rawInput[i] - oldMean;
            normalizationState[\mean][i] = oldMean + (oneMinus * delta);
            normalizationState[\variance][i] = (decay * normalizationState[\variance][i]) + (oneMinus * delta * delta);
        });
        normalizationState[\count] = normalizationState[\count] + 1;
        ^rawInput;
    }

    conditionInputVector { |input, update=false|
        var raw, result, active, epsilonValue, clipValue, blendValue, z, mapped;
        raw = this.rawCleanInputVector(input);
        if(normalizationState.isNil, { this.resetAdvancedRealtimeState });
        if(update, { this.updateNormalizationState(raw) });
        active = normalizationState[\enabled] and: { normalizationState[\count] >= normalizationState[\warmup] };
        if(active.not, { ^raw });
        epsilonValue = normalizationState[\epsilon].max(0.000000001);
        clipValue = normalizationState[\clip].max(0.10);
        blendValue = normalizationState[\blend].clip(0.0, 1.0);
        result = FloatArray.fill(inputSize, { |i|
            z = (raw[i] - normalizationState[\mean][i]) / (normalizationState[\variance][i] + epsilonValue).sqrt;
            z = z.clip(clipValue.neg, clipValue);
            mapped = 0.5 + (0.5 * (z / clipValue).tanh);
            ((raw[i] * (1.0 - blendValue)) + (mapped * blendValue)).clip(0.0, 1.0);
        });
        ^result;
    }

    updateMultiResolutionState { |rawInput|
        var history, maxWindow, slice, meanVector;
        if(multiResolutionState.isNil, { this.resetAdvancedRealtimeState });
        history = multiResolutionState[\history];
        history.add(rawInput.copy);
        maxWindow = multiResolutionState[\slowWindow].asInteger.max(2);
        while({ history.size > maxWindow }, { history.removeAt(0); });
        multiResolutionState[\counter] = multiResolutionState[\counter] + 1;
        if((multiResolutionState[\counter] % multiResolutionState[\midDivider].asInteger.max(1)) == 0, {
            slice = history.copyRange((history.size - multiResolutionState[\midWindow]).max(0), history.size - 1);
            meanVector = FloatArray.fill(inputSize, { |i| slice.collect({ |v| v[i] }).mean });
            multiResolutionState[\midCache] = meanVector;
        });
        if((multiResolutionState[\counter] % multiResolutionState[\slowDivider].asInteger.max(1)) == 0, {
            slice = history.copyRange((history.size - multiResolutionState[\slowWindow]).max(0), history.size - 1);
            meanVector = FloatArray.fill(inputSize, { |i| slice.collect({ |v| v[i] }).mean });
            multiResolutionState[\slowCache] = meanVector;
        });
        ^rawInput;
    }

    prepareRealtimeInput { |input, update=false| ^this.rawCleanInputVector(input); }
    applyMultiResolutionContext { |input| ^this.rawCleanInputVector(input); }
    stabilityGuardAfter { |measuredLoss| ^measuredLoss; }

    advancedRealtimeStatus {
        ^(
            normalizationEnabled: normalizationState[\enabled],
            normalizationCount: normalizationState[\count],
            normalizationMean: normalizationState[\mean].copy,
            normalizationVariance: normalizationState[\variance].copy,
            multiResolutionEnabled: multiResolutionState[\enabled],
            midCache: multiResolutionState[\midCache].copy,
            slowCache: multiResolutionState[\slowCache].copy,
            stabilityGuardEnabled: stabilityGuardState[\enabled],
            stabilityRollbacks: stabilityGuardState[\rollbacks],
            stabilityLastReason: stabilityGuardState[\lastReason]
        );
    }

    setClassicMusicalDirect {
        this.setParameters((learningRate:0.00022,gradientClip:0.45,replayRate:0.04,replayBatchSize:1,
            protectionStrength:0.14,memoryRetrievalGain:0.07,memoryRecallSize:1,
            trajectoryRetrievalGain:0.05,trajectoryRecallSize:1,generationWindowSize:4,
            attentionTemperature:1.12,routerTemperature:1.00,deltaScale:0.34,
            trajectoryExplorationGain:0.0045,diversityNoiseGain:0.00030,
            diversityRepulsionGain:0.0013,autoTuneEnabled:false,metaLearnEnabled:false),false);
        this.disableUnifiedRCU; this.enableLearning; this.enableGeneration; ^this.runtimeConfig;
    }
    setClassicUltraCPUFastLearn {
        this.setParameters((learningRate:0.00065,gradientClip:0.70,surpriseThreshold:0.030,
            surpriseGain:1.80,adaptationFastRate:2.20,adaptationSlowRate:0.55,
            replayRate:0.0,replayBatchSize:1,protectionStrength:0.08,
            memoryRetrievalGain:0.0,memoryRecallSize:0,trajectoryRetrievalGain:0.0,
            trajectoryRecallSize:0,generationWindowSize:2,attentionTemperature:1.15,
            routerTemperature:1.00,deltaScale:0.38,trajectoryExplorationGain:0.005,
            diversityNoiseGain:0.00030,diversityRepulsionGain:0.0012,
            autoTuneEnabled:false,metaLearnEnabled:false),false);
        this.disableUnifiedRCU; this.enableLearning; this.enableGeneration; ^this.runtimeConfig;
    }
    setClassicGenerationOnly {
        this.setParameters((replayRate:0.0,memoryRecallSize:1,trajectoryRecallSize:1,
            generationWindowSize:3,attentionTemperature:1.20,routerTemperature:1.00,
            deltaScale:0.36,trajectoryExplorationGain:0.006,diversityNoiseGain:0.00040,
            diversityRepulsionGain:0.0018,autoTuneEnabled:false,metaLearnEnabled:false),false);
        this.disableUnifiedRCU; this.disableLearning; this.enableGeneration; ^this.runtimeConfig;
    }

    runtimeParameterNames {
        ^[
            \learningRate, \temperature, \attentionTemperature, \routerTemperature, \trajectoryRetrievalTemperature,
            \residualScale, \expertScale, \deltaScale, \surpriseThreshold, \surpriseGain, \gateLearningRate, \gradientClip,
            \memoryWriteThreshold, \memoryRetrievalGain, \memoryRetrievalTemperature, \memoryDecay, \memoryConsolidationRate,
            \memoryUsageDecay, \replayRate, \replayBatchSize, \memoryRecallSize, \adaptiveInterferenceEnabled,
            \adaptiveReplayMin, \adaptiveReplayMax, \adaptiveInterferenceThreshold, \adaptiveInterferenceSmoothing,
            \adaptiveReplayBoost, \protectionStrength, \adaptationFastRate, \adaptationSlowRate, \predictionLossWeight,
            \deltaLossWeight, \directionLossWeight, \replayPriorityMix, \replayUniformMix, \expertBalanceStrength,
            \headSpecializationThreshold, \headSpecializationStrength, \interferenceTestSteps, \trajectoryRecallSize,
            \trajectoryRetrievalGain, \trajectoryVelocityGain, \trajectoryAccelerationGain, \trajectoryVelocityClip,
            \trajectoryAccelerationClip, \trajectoryDecay, \trajectoryUsageDecay, \trajectoryNoveltyWeight,
            \trajectoryInputWeight, \trajectoryVelocityWeight, \trajectoryExplorationGain, \diversityHistorySize,
            \diversityRadius, \diversityRepulsionGain, \diversityNoiseGain, \diversityMaxCorrection, \diversityAdaptiveGain,
            \localDiversityWindow, \localDiversityFloor, \localDiversityGain, \localDiversityMaxCorrection,
            \generationWindowSize, \driftEmaDecay, \driftGain, \driftMaxCorrection, \autoTuneEnabled, \autoTuneInterval,
            \autoTuneStrength, \autoTuneSmoothing, \autoTuneTargetNovelty, \autoTuneTargetDiversity,
            \autoTuneExplorationMin, \autoTuneExplorationMax, \autoTuneNoiseMin, \autoTuneNoiseMax,
            \autoTuneTemperatureMin, \autoTuneTemperatureMax, \metaLearnEnabled, \metaLearnInterval, \metaLearnStrength,
            \metaLearnSmoothing, \metaLearnTargetError, \metaLearnTargetSurprise, \metaLearnTargetRecall,
            \metaLearnTargetInterference, \metaLearnLearningRateMin, \metaLearnLearningRateMax, \metaLearnReplayRateMin,
            \metaLearnReplayRateMax, \metaLearnProtectionMin, \metaLearnProtectionMax, \metaLearnRetrievalGainMin,
            \metaLearnRetrievalGainMax, \normalizationEnabled, \normalizationDecay, \normalizationClip, \normalizationBlend,
            \normalizationWarmup, \multiResolutionEnabled, \multiResolutionMidWindow, \multiResolutionSlowWindow,
            \multiResolutionMidDivider, \multiResolutionSlowDivider, \multiResolutionMidGain, \multiResolutionSlowGain,
            \stabilityGuardEnabled, \stabilitySnapshotInterval, \stabilityLossLimit, \stabilityLearningRateBackoff, \torusMask
        ];
    }

    isRuntimeParameter { |name| ^this.runtimeParameterNames.includes(name.asSymbol); }

    getParameter { |name|
        var key = name.asSymbol;
        ^switch(key,
            \learningRate, { learningRate }, \temperature, { temperature },
            \attentionTemperature, { attentionTemperature }, \routerTemperature, { routerTemperature },
            \trajectoryRetrievalTemperature, { trajectoryRetrievalTemperature },
            \residualScale, { residualScale }, \expertScale, { expertScale }, \deltaScale, { deltaScale },
            \surpriseThreshold, { surpriseThreshold }, \surpriseGain, { surpriseGain },
            \gateLearningRate, { gateLearningRate }, \gradientClip, { gradientClip },
            \memoryWriteThreshold, { memoryWriteThreshold }, \memoryRetrievalGain, { memoryRetrievalGain },
            \memoryRetrievalTemperature, { memoryRetrievalTemperature }, \memoryDecay, { memoryDecay },
            \memoryConsolidationRate, { memoryConsolidationRate }, \memoryUsageDecay, { memoryUsageDecay },
            \replayRate, { replayRate }, \replayBatchSize, { replayBatchSize }, \memoryRecallSize, { memoryRecallSize },
            \adaptiveInterferenceEnabled, { adaptiveState[\enabled] }, \adaptiveReplayMin, { adaptiveState[\replayMin] },
            \adaptiveReplayMax, { adaptiveState[\replayMax] }, \adaptiveInterferenceThreshold, { adaptiveState[\threshold] },
            \adaptiveInterferenceSmoothing, { adaptiveState[\smoothing] }, \adaptiveReplayBoost, { adaptiveState[\replayBoost] },
            \protectionStrength, { protectionStrength }, \adaptationFastRate, { adaptationFastRate },
            \adaptationSlowRate, { adaptationSlowRate }, \predictionLossWeight, { predictionLossWeight },
            \deltaLossWeight, { deltaLossWeight }, \directionLossWeight, { directionLossWeight },
            \replayPriorityMix, { replayPriorityMix }, \replayUniformMix, { replayUniformMix },
            \expertBalanceStrength, { expertBalanceStrength }, \headSpecializationThreshold, { headSpecializationThreshold },
            \headSpecializationStrength, { headSpecializationStrength }, \interferenceTestSteps, { interferenceTestSteps },
            \trajectoryRecallSize, { trajectoryRecallSize }, \trajectoryRetrievalGain, { trajectoryRetrievalGain },
            \trajectoryVelocityGain, { trajectoryVelocityGain }, \trajectoryAccelerationGain, { trajectoryAccelerationGain },
            \trajectoryVelocityClip, { trajectoryVelocityClip }, \trajectoryAccelerationClip, { trajectoryAccelerationClip },
            \trajectoryDecay, { trajectoryDecay }, \trajectoryUsageDecay, { trajectoryUsageDecay },
            \trajectoryNoveltyWeight, { trajectoryNoveltyWeight }, \trajectoryInputWeight, { trajectoryInputWeight },
            \trajectoryVelocityWeight, { trajectoryVelocityWeight }, \trajectoryExplorationGain, { trajectoryExplorationGain },
            \diversityHistorySize, { diversityHistorySize }, \diversityRadius, { diversityRadius },
            \diversityRepulsionGain, { diversityRepulsionGain }, \diversityNoiseGain, { diversityNoiseGain },
            \diversityMaxCorrection, { diversityMaxCorrection }, \diversityAdaptiveGain, { diversityAdaptiveGain },
            \localDiversityWindow, { localDiversityWindow }, \localDiversityFloor, { localDiversityFloor },
            \localDiversityGain, { localDiversityGain }, \localDiversityMaxCorrection, { localDiversityMaxCorrection },
            \generationWindowSize, { generationWindowSize }, \driftEmaDecay, { generationDriftState[\emaDecay] },
            \driftGain, { generationDriftState[\gain] }, \driftMaxCorrection, { generationDriftState[\maxCorrection] },
            \autoTuneEnabled, { autoTuneEnabled }, \autoTuneInterval, { autoTuneInterval },
            \autoTuneStrength, { autoTuneStrength }, \autoTuneSmoothing, { autoTuneSmoothing },
            \autoTuneTargetNovelty, { autoTuneTargetNovelty }, \autoTuneTargetDiversity, { autoTuneTargetDiversity },
            \autoTuneExplorationMin, { autoTuneExplorationMin }, \autoTuneExplorationMax, { autoTuneExplorationMax },
            \autoTuneNoiseMin, { autoTuneNoiseMin }, \autoTuneNoiseMax, { autoTuneNoiseMax },
            \autoTuneTemperatureMin, { autoTuneTemperatureMin }, \autoTuneTemperatureMax, { autoTuneTemperatureMax },
            \metaLearnEnabled, { metaLearnEnabled }, \metaLearnInterval, { metaLearnInterval },
            \metaLearnStrength, { metaLearnStrength }, \metaLearnSmoothing, { metaLearnSmoothing },
            \metaLearnTargetError, { metaLearnTargetError }, \metaLearnTargetSurprise, { metaLearnTargetSurprise },
            \metaLearnTargetRecall, { metaLearnTargetRecall }, \metaLearnTargetInterference, { metaLearnTargetInterference },
            \metaLearnLearningRateMin, { metaLearnLearningRateMin }, \metaLearnLearningRateMax, { metaLearnLearningRateMax },
            \metaLearnReplayRateMin, { metaLearnReplayRateMin }, \metaLearnReplayRateMax, { metaLearnReplayRateMax },
            \metaLearnProtectionMin, { metaLearnProtectionMin }, \metaLearnProtectionMax, { metaLearnProtectionMax },
            \metaLearnRetrievalGainMin, { metaLearnRetrievalGainMin }, \metaLearnRetrievalGainMax, { metaLearnRetrievalGainMax },
            \normalizationEnabled, { normalizationState[\enabled] }, \normalizationDecay, { normalizationState[\decay] },
            \normalizationClip, { normalizationState[\clip] }, \normalizationBlend, { normalizationState[\blend] },
            \normalizationWarmup, { normalizationState[\warmup] }, \multiResolutionEnabled, { multiResolutionState[\enabled] },
            \multiResolutionMidWindow, { multiResolutionState[\midWindow] }, \multiResolutionSlowWindow, { multiResolutionState[\slowWindow] },
            \multiResolutionMidDivider, { multiResolutionState[\midDivider] }, \multiResolutionSlowDivider, { multiResolutionState[\slowDivider] },
            \multiResolutionMidGain, { multiResolutionState[\midGain] }, \multiResolutionSlowGain, { multiResolutionState[\slowGain] },
            \stabilityGuardEnabled, { stabilityGuardState[\enabled] }, \stabilitySnapshotInterval, { stabilityGuardState[\snapshotInterval] },
            \stabilityLossLimit, { stabilityGuardState[\lossLimit] }, \stabilityLearningRateBackoff, { stabilityGuardState[\learningRateBackoff] },
            \torusMask, { torusMask.copy },
            { Error("Unknown runtime parameter: " ++ key).throw }
        );
    }

    setParameterLocal { |name, value, post=true|
        var key, result, lossSum, prioritySum;
        key = name.asSymbol;
        if([\inputSize, \outputSize, \windowSize, \longMemorySize, \hiddenSize, \numHeads, \headSize, \numExperts, \trajectoryMemorySize].includes(key), {
            Error("Structural parameter cannot be changed at runtime: " ++ key).throw;
        });
        switch(key,
            \learningRate, { learningRate = value.asFloat.clip(0.0000001, 1.0); learningRateCurrent = learningRateCurrent.clip(learningRate * 0.05, learningRate * 3.0); },
            \temperature, { temperature = value.asFloat.clip(0.01, 20.0); attentionTemperature = temperature; routerTemperature = temperature },
            \attentionTemperature, { attentionTemperature = value.asFloat.clip(0.01, 20.0) },
            \routerTemperature, { routerTemperature = value.asFloat.clip(0.01, 20.0) },
            \trajectoryRetrievalTemperature, { trajectoryRetrievalTemperature = value.asFloat.clip(0.05, 10.0) },
            \residualScale, { residualScale = value.asFloat.clip(0.0, 4.0) },
            \expertScale, { expertScale = value.asFloat.clip(0.0, 4.0) },
            \deltaScale, { deltaScale = value.asFloat.clip(0.0, 2.0) },
            \surpriseThreshold, { surpriseThreshold = value.asFloat.clip(0.0, 1.0) },
            \surpriseGain, { surpriseGain = value.asFloat.clip(0.0, 10.0) },
            \gateLearningRate, { gateLearningRate = value.asFloat.clip(0.0000001, 1.0) },
            \gradientClip, { gradientClip = value.asFloat.clip(0.000001, 100.0) },
            \memoryWriteThreshold, { memoryWriteThreshold = value.asFloat.clip(0.0, 1.0) },
            \memoryRetrievalGain, { memoryRetrievalGain = value.asFloat.clip(0.0, 4.0) },
            \memoryRetrievalTemperature, { memoryRetrievalTemperature = value.asFloat.clip(0.05, 10.0) },
            \memoryDecay, { memoryDecay = value.asFloat.clip(0.0, 1.0) },
            \memoryConsolidationRate, { memoryConsolidationRate = value.asFloat.clip(0.0, 1.0) },
            \memoryUsageDecay, { memoryUsageDecay = value.asFloat.clip(0.0, 1.0) },
            \replayRate, { replayRate = value.asFloat.clip(0.0, 1.0) },
            \replayBatchSize, { replayBatchSize = value.asInteger.clip(1, longMemorySize.max(1)) },
            \memoryRecallSize, { memoryRecallSize = value.asInteger.clip(0, longMemorySize) },
            \adaptiveInterferenceEnabled, { adaptiveState[\enabled] = (value == true) or: { value.isNumber and: { value > 0 } } },
            \adaptiveReplayMin, { adaptiveState[\replayMin] = value.asFloat.clip(0.0, adaptiveState[\replayMax]) },
            \adaptiveReplayMax, { adaptiveState[\replayMax] = value.asFloat.clip(adaptiveState[\replayMin], 1.0) },
            \adaptiveInterferenceThreshold, { adaptiveState[\threshold] = value.asFloat.clip(0.000001, 1.0) },
            \adaptiveInterferenceSmoothing, { adaptiveState[\smoothing] = value.asFloat.clip(0.0, 0.999999) },
            \adaptiveReplayBoost, { adaptiveState[\replayBoost] = value.asFloat.clip(0.0, 1.0) },
            \protectionStrength, { protectionStrength = value.asFloat.clip(0.0, 10.0) },
            \adaptationFastRate, { adaptationFastRate = value.asFloat.clip(0.0, 10.0) },
            \adaptationSlowRate, { adaptationSlowRate = value.asFloat.clip(0.0, 10.0) },
            \predictionLossWeight, { predictionLossWeight = value.asFloat.clip(0.0, 1.0) },
            \deltaLossWeight, { deltaLossWeight = value.asFloat.clip(0.0, 1.0) },
            \directionLossWeight, { directionLossWeight = value.asFloat.clip(0.0, 1.0) },
            \replayPriorityMix, { replayPriorityMix = value.asFloat.clip(0.0, 1.0); prioritySum = replayPriorityMix + replayUniformMix; if(prioritySum <= 0.0, { replayUniformMix = 1.0 }); },
            \replayUniformMix, { replayUniformMix = value.asFloat.clip(0.0, 1.0); prioritySum = replayPriorityMix + replayUniformMix; if(prioritySum <= 0.0, { replayPriorityMix = 1.0 }); },
            \expertBalanceStrength, { expertBalanceStrength = value.asFloat.clip(0.0, 10.0) },
            \headSpecializationThreshold, { headSpecializationThreshold = value.asFloat.clip(0.0, 1.0) },
            \headSpecializationStrength, { headSpecializationStrength = value.asFloat.clip(0.0, 10.0) },
            \interferenceTestSteps, { interferenceTestSteps = value.asInteger.clip(0, 1024) },
            \trajectoryRecallSize, { trajectoryRecallSize = value.asInteger.clip(0, trajectoryMemorySize) },
            \trajectoryRetrievalGain, { trajectoryRetrievalGain = value.asFloat.clip(0.0, 4.0) },
            \trajectoryVelocityGain, { trajectoryVelocityGain = value.asFloat.clip(0.0, 4.0) },
            \trajectoryAccelerationGain, { trajectoryAccelerationGain = value.asFloat.clip(0.0, 4.0) },
            \trajectoryVelocityClip, { trajectoryVelocityClip = value.asFloat.clip(0.0, 1.0) },
            \trajectoryAccelerationClip, { trajectoryAccelerationClip = value.asFloat.clip(0.0, 2.0) },
            \trajectoryDecay, { trajectoryDecay = value.asFloat.clip(0.0, 1.0) },
            \trajectoryUsageDecay, { trajectoryUsageDecay = value.asFloat.clip(0.0, 1.0) },
            \trajectoryNoveltyWeight, { trajectoryNoveltyWeight = value.asFloat.clip(0.0, 1.0) },
            \trajectoryInputWeight, { trajectoryInputWeight = value.asFloat.clip(0.0, 1.0) },
            \trajectoryVelocityWeight, { trajectoryVelocityWeight = value.asFloat.clip(0.0, 1.0) },
            \trajectoryExplorationGain, { trajectoryExplorationGain = value.asFloat.clip(0.0, 1.0) },
            \diversityHistorySize, { diversityHistorySize = value.asInteger.clip(1, 4096); while({ generationHistory.size > diversityHistorySize }, { generationHistory.removeAt(0); }); },
            \diversityRadius, { diversityRadius = value.asFloat.clip(0.000001, 1.0) },
            \diversityRepulsionGain, { diversityRepulsionGain = value.asFloat.clip(0.0, 1.0) },
            \diversityNoiseGain, { diversityNoiseGain = value.asFloat.clip(0.0, 1.0) },
            \diversityMaxCorrection, { diversityMaxCorrection = value.asFloat.clip(0.0, 1.0) },
            \diversityAdaptiveGain, { diversityAdaptiveGain = value.asFloat.clip(0.0, 10.0) },
            \localDiversityWindow, { localDiversityWindow = value.asInteger.clip(2, 4096) },
            \localDiversityFloor, { localDiversityFloor = value.asFloat.clip(0.000001, 1.0) },
            \localDiversityGain, { localDiversityGain = value.asFloat.clip(0.0, 2.0) },
            \localDiversityMaxCorrection, { localDiversityMaxCorrection = value.asFloat.clip(0.0, 1.0) },
            \generationWindowSize, { generationWindowSize = value.asInteger.clip(1, windowSize) },
            \driftEmaDecay, { generationDriftState[\emaDecay] = value.asFloat.clip(0.0, 0.999999) },
            \driftGain, { generationDriftState[\gain] = value.asFloat.clip(0.0, 10.0) },
            \driftMaxCorrection, { generationDriftState[\maxCorrection] = value.asFloat.clip(0.0, 1.0) },
            \autoTuneEnabled, { autoTuneEnabled = if(value == true, { true }, { if(value.isNumber, { value.asFloat > 0.0 }, { false }); }); },
            \autoTuneInterval, { autoTuneInterval = value.asInteger.clip(1, 4096) },
            \autoTuneStrength, { autoTuneStrength = value.asFloat.clip(0.0, 1.0) },
            \autoTuneSmoothing, { autoTuneSmoothing = value.asFloat.clip(0.0, 0.999999) },
            \autoTuneTargetNovelty, { autoTuneTargetNovelty = value.asFloat.clip(0.0, 1.0) },
            \autoTuneTargetDiversity, { autoTuneTargetDiversity = value.asFloat.clip(0.0, 1.0) },
            \autoTuneExplorationMin, { autoTuneExplorationMin = value.asFloat.clip(0.0, autoTuneExplorationMax) },
            \autoTuneExplorationMax, { autoTuneExplorationMax = value.asFloat.clip(autoTuneExplorationMin, 1.0) },
            \autoTuneNoiseMin, { autoTuneNoiseMin = value.asFloat.clip(0.0, autoTuneNoiseMax) },
            \autoTuneNoiseMax, { autoTuneNoiseMax = value.asFloat.clip(autoTuneNoiseMin, 1.0) },
            \autoTuneTemperatureMin, { autoTuneTemperatureMin = value.asFloat.clip(0.01, autoTuneTemperatureMax) },
            \autoTuneTemperatureMax, { autoTuneTemperatureMax = value.asFloat.clip(autoTuneTemperatureMin, 20.0) },
            \metaLearnEnabled, { metaLearnEnabled = if(value == true, { true }, { if(value.isNumber, { value.asFloat > 0.0 }, { false }); }); },
            \metaLearnInterval, { metaLearnInterval = value.asInteger.clip(1, 4096) },
            \metaLearnStrength, { metaLearnStrength = value.asFloat.clip(0.0, 1.0) },
            \metaLearnSmoothing, { metaLearnSmoothing = value.asFloat.clip(0.0, 0.999999) },
            \metaLearnTargetError, { metaLearnTargetError = value.asFloat.clip(0.0, 1.0) },
            \metaLearnTargetSurprise, { metaLearnTargetSurprise = value.asFloat.clip(0.0, 1.0) },
            \metaLearnTargetRecall, { metaLearnTargetRecall = value.asFloat.clip(0.0, 1.0) },
            \metaLearnTargetInterference, { metaLearnTargetInterference = value.asFloat.clip(-1.0, 1.0) },
            \metaLearnLearningRateMin, { metaLearnLearningRateMin = value.asFloat.clip(0.0000001, metaLearnLearningRateMax) },
            \metaLearnLearningRateMax, { metaLearnLearningRateMax = value.asFloat.clip(metaLearnLearningRateMin, 1.0) },
            \metaLearnReplayRateMin, { metaLearnReplayRateMin = value.asFloat.clip(0.0, metaLearnReplayRateMax) },
            \metaLearnReplayRateMax, { metaLearnReplayRateMax = value.asFloat.clip(metaLearnReplayRateMin, 1.0) },
            \metaLearnProtectionMin, { metaLearnProtectionMin = value.asFloat.clip(0.0, metaLearnProtectionMax) },
            \metaLearnProtectionMax, { metaLearnProtectionMax = value.asFloat.clip(metaLearnProtectionMin, 10.0) },
            \metaLearnRetrievalGainMin, { metaLearnRetrievalGainMin = value.asFloat.clip(0.0, metaLearnRetrievalGainMax) },
            \metaLearnRetrievalGainMax, { metaLearnRetrievalGainMax = value.asFloat.clip(metaLearnRetrievalGainMin, 4.0) },
            \normalizationEnabled, { normalizationState[\enabled] = (value == true) or: { value.isNumber and: { value > 0 } } },
            \normalizationDecay, { normalizationState[\decay] = value.asFloat.clip(0.0, 0.999999) },
            \normalizationClip, { normalizationState[\clip] = value.asFloat.clip(0.10, 10.0) },
            \normalizationBlend, { normalizationState[\blend] = value.asFloat.clip(0.0, 1.0) },
            \normalizationWarmup, { normalizationState[\warmup] = value.asInteger.clip(1, 100000) },
            \multiResolutionEnabled, { multiResolutionState[\enabled] = (value == true) or: { value.isNumber and: { value > 0 } } },
            \multiResolutionMidWindow, { multiResolutionState[\midWindow] = value.asInteger.clip(2, 4096) },
            \multiResolutionSlowWindow, { multiResolutionState[\slowWindow] = value.asInteger.clip(multiResolutionState[\midWindow], 16384) },
            \multiResolutionMidDivider, { multiResolutionState[\midDivider] = value.asInteger.clip(1, 1024) },
            \multiResolutionSlowDivider, { multiResolutionState[\slowDivider] = value.asInteger.clip(1, 4096) },
            \multiResolutionMidGain, { multiResolutionState[\midGain] = value.asFloat.clip(0.0, 0.45) },
            \multiResolutionSlowGain, { multiResolutionState[\slowGain] = value.asFloat.clip(0.0, 0.45) },
            \stabilityGuardEnabled, { stabilityGuardState[\enabled] = (value == true) or: { value.isNumber and: { value > 0 } } },
            \stabilitySnapshotInterval, { stabilityGuardState[\snapshotInterval] = value.asInteger.clip(8, 65536) },
            \stabilityLossLimit, { stabilityGuardState[\lossLimit] = value.asFloat.clip(0.01, 1000.0) },
            \stabilityLearningRateBackoff, { stabilityGuardState[\learningRateBackoff] = value.asFloat.clip(0.05, 1.0) },
            \torusMask, { torusMask = this.normalizeTorusMask(value) },
            { Error("Unknown runtime parameter: " ++ key).throw }
        );
        if([\predictionLossWeight, \deltaLossWeight, \directionLossWeight].includes(key), {
            lossSum = predictionLossWeight + deltaLossWeight + directionLossWeight;
            if(lossSum <= 0.0, { predictionLossWeight = 1.0; deltaLossWeight = 0.0; directionLossWeight = 0.0; }, {
                predictionLossWeight = predictionLossWeight / lossSum;
                deltaLossWeight = deltaLossWeight / lossSum;
                directionLossWeight = directionLossWeight / lossSum;
            });
        });
        result = this.getParameter(key);
        if(post, { ("runtime " ++ key ++ " = " ++ result).postln });
        ^result;
    }

    setParameters { |settings, post=true|
        if(settings.isNil, { ^this.runtimeConfig });
        settings.keysValuesDo({ |key, value| this.setParameter(key, value, post); });
        ^this.runtimeConfig;
    }

    runtimeConfig {
        var result = IdentityDictionary.new;
        this.runtimeParameterNames.do({ |key| result[key] = this.getParameter(key); });
        ^result;
    }

    morphParameterLocal { |name, target, duration=1.0, steps=50, clock|
        var key, start, amount, safeSteps, safeDuration;
        key = name.asSymbol;
        start = this.getParameter(key);
        if(start.isNumber.not or: { target.isNumber.not }, { Error("morphParameter requires a numeric parameter").throw; });
        safeSteps = steps.asInteger.max(1);
        safeDuration = duration.asFloat.max(0.0);
        ^Routine({
            safeSteps.do({ |index|
                this.setParameter(key, start + ((target.asFloat - start) * ((index + 1) / safeSteps)), false);
                (safeDuration / safeSteps).wait;
            });
        }).play(clock ? AppClock);
    }

    metaLearnStep {
        var errorPressure, surprisePressure, recallPressure, interferencePressure, targetLearningRate, targetReplayRate, targetProtection, targetRetrievalGain;
        metaLearnCounter = metaLearnCounter + 1;
        if(metaLearnEnabled.not, { ^this.metaLearnStatus });
        if((metaLearnCounter % metaLearnInterval) != 0, { ^this.metaLearnStatus });
        metaLearnErrorEMA = (metaLearnSmoothing * metaLearnErrorEMA) + ((1.0 - metaLearnSmoothing) * errorEMA.clip(0.0, 1.0));
        metaLearnSurpriseEMA = (metaLearnSmoothing * metaLearnSurpriseEMA) + ((1.0 - metaLearnSmoothing) * surpriseEMA.clip(0.0, 1.0));
        metaLearnRecallEMA = (metaLearnSmoothing * metaLearnRecallEMA) + ((1.0 - metaLearnSmoothing) * memoryRecall.clip(0.0, 1.0));
        metaLearnInterferenceEMA = (metaLearnSmoothing * metaLearnInterferenceEMA) + ((1.0 - metaLearnSmoothing) * interferenceScore.clip(-1.0, 1.0));
        errorPressure = (metaLearnErrorEMA - metaLearnTargetError).clip(-1.0, 1.0);
        surprisePressure = (metaLearnSurpriseEMA - metaLearnTargetSurprise).clip(-1.0, 1.0);
        recallPressure = (metaLearnTargetRecall - metaLearnRecallEMA).clip(-1.0, 1.0);
        interferencePressure = (metaLearnInterferenceEMA - metaLearnTargetInterference).clip(-1.0, 1.0);
        metaLearnPlasticityPressure = ((0.70 * errorPressure) + (0.30 * surprisePressure)).clip(-1.0, 1.0);
        metaLearnStabilityPressure = ((0.80 * interferencePressure) + (0.20 * protectionScalar)).clip(-1.0, 1.0);
        targetLearningRate = (learningRate * (1.0 + (metaLearnStrength * 0.10 * metaLearnPlasticityPressure) - (metaLearnStrength * 0.14 * metaLearnStabilityPressure))).clip(metaLearnLearningRateMin, metaLearnLearningRateMax);
        targetReplayRate = (replayRate + (metaLearnStrength * 0.020 * ((0.55 * errorPressure) + (0.85 * interferencePressure)))).clip(metaLearnReplayRateMin, metaLearnReplayRateMax);
        targetProtection = (protectionStrength + (metaLearnStrength * 0.030 * (interferencePressure + (0.25 * surprisePressure) - (0.15 * errorPressure)))).clip(metaLearnProtectionMin, metaLearnProtectionMax);
        targetRetrievalGain = (memoryRetrievalGain + (metaLearnStrength * 0.020 * recallPressure)).clip(metaLearnRetrievalGainMin, metaLearnRetrievalGainMax);
        this.setParameter(\learningRate, targetLearningRate, false);
        this.setParameter(\replayRate, targetReplayRate, false);
        this.setParameter(\protectionStrength, targetProtection, false);
        this.setParameter(\memoryRetrievalGain, targetRetrievalGain, false);
        metaLearnAdjustmentCount = metaLearnAdjustmentCount + 1;
        ^this.metaLearnStatus;
    }

   metaLearnStatus {
        ^(
            enabled: metaLearnEnabled, interval: metaLearnInterval, counter: metaLearnCounter, adjustments: metaLearnAdjustmentCount,
            errorEMA: metaLearnErrorEMA, surpriseEMA: metaLearnSurpriseEMA, recallEMA: metaLearnRecallEMA,
            interferenceEMA: metaLearnInterferenceEMA, plasticityPressure: metaLearnPlasticityPressure,
            stabilityPressure: metaLearnStabilityPressure, learningRate: learningRate, replayRate: replayRate,
            protectionStrength: protectionStrength, memoryRetrievalGain: memoryRetrievalGain
        );
    }

    enableMetaLearning { this.setParameter(\metaLearnEnabled, true, false); "Meta-learning controller enabled".postln; ^this.metaLearnStatus; }
    disableMetaLearning { this.setParameter(\metaLearnEnabled, false, false); "Meta-learning controller disabled".postln; ^this.metaLearnStatus; }

    resetMetaLearning {
        metaLearnCounter = 0; metaLearnAdjustmentCount = 0;
        metaLearnErrorEMA = errorEMA.clip(0.0, 1.0);
        metaLearnSurpriseEMA = surpriseEMA.clip(0.0, 1.0);
        metaLearnRecallEMA = memoryRecall.clip(0.0, 1.0);
        metaLearnInterferenceEMA = interferenceScore.clip(-1.0, 1.0);
        metaLearnPlasticityPressure = 0.0; metaLearnStabilityPressure = 0.0;
        ^this.metaLearnStatus;
    }

    autoTuneStep {
        var noveltyError, diversityError, combinedError, targetExploration, targetNoise, targetTemperature;
        autoTuneCounter = autoTuneCounter + 1;
        if(autoTuneEnabled.not, { ^this.autoTuneStatus });
        if((autoTuneCounter % autoTuneInterval) != 0, { ^this.autoTuneStatus });
        autoTuneNoveltyEMA = (autoTuneSmoothing * autoTuneNoveltyEMA) + ((1.0 - autoTuneSmoothing) * generationNovelty.clip(0.0, 1.0));
        autoTuneDiversityEMA = (autoTuneSmoothing * autoTuneDiversityEMA) + ((1.0 - autoTuneSmoothing) * generationDiversity.clip(0.0, 1.0));
        noveltyError = autoTuneTargetNovelty - autoTuneNoveltyEMA;
        diversityError = autoTuneTargetDiversity - autoTuneDiversityEMA;
        combinedError = ((noveltyError + diversityError) * 0.5).clip(-1.0, 1.0);
        autoTuneLastError = combinedError;
        targetExploration = (trajectoryExplorationGain + (combinedError * autoTuneStrength * 0.010)).clip(autoTuneExplorationMin, autoTuneExplorationMax);
        targetNoise = (diversityNoiseGain + (diversityError * autoTuneStrength * 0.002)).clip(autoTuneNoiseMin, autoTuneNoiseMax);
        targetTemperature = (attentionTemperature + (noveltyError * autoTuneStrength * 0.20)).clip(autoTuneTemperatureMin, autoTuneTemperatureMax);
        this.setParameter(\trajectoryExplorationGain, targetExploration, false);
        this.setParameter(\diversityNoiseGain, targetNoise, false);
        this.setParameter(\attentionTemperature, targetTemperature, false);
        autoTuneAdjustmentCount = autoTuneAdjustmentCount + 1;
        ^this.autoTuneStatus;
    }

    autoTuneStatus {
        ^(
            enabled: autoTuneEnabled, interval: autoTuneInterval, counter: autoTuneCounter, adjustments: autoTuneAdjustmentCount,
            noveltyEMA: autoTuneNoveltyEMA, diversityEMA: autoTuneDiversityEMA, lastError: autoTuneLastError,
            trajectoryExplorationGain: trajectoryExplorationGain, diversityNoiseGain: diversityNoiseGain, temperature: temperature
        );
    }

    enableAutoTune { this.setParameter(\autoTuneEnabled, true, false); "Automatic runtime tuning enabled".postln; ^this.autoTuneStatus; }
    disableAutoTune { this.setParameter(\autoTuneEnabled, false, false); "Automatic runtime tuning disabled".postln; ^this.autoTuneStatus; }

    resetAutoTune {
        autoTuneCounter = 0; autoTuneAdjustmentCount = 0;
        autoTuneNoveltyEMA = generationNovelty.clip(0.0, 1.0);
        autoTuneDiversityEMA = generationDiversity.clip(0.0, 1.0);
        autoTuneLastError = 0.0;
        ^this.autoTuneStatus;
    }

    zeroVectorInPlace { |vector|
        vector.size.do({ |i| vector[i] = 0.0; });
        ^vector;
    }

    zeroMatrixInPlace { |matrix|
        matrix.size.do({ |r|
            matrix[r].size.do({ |c| matrix[r][c] = 0.0; });
        });
        ^matrix;
    }

    initGradientBuffers {
        gradientBuffers = IdentityDictionary.new;
        gradientBuffers[\inputProjection] = this.zeroMatrix(hiddenSize, inputSize);
        gradientBuffers[\inputBias] = this.zeroVector(hiddenSize);
        gradientBuffers[\outputProjection] = this.zeroMatrix(outputSize, hiddenSize);
        gradientBuffers[\outputBias] = this.zeroVector(outputSize);
        gradientBuffers[\routerWeights] = this.zeroMatrix(numExperts, hiddenSize);
        gradientBuffers[\routerBias] = this.zeroVector(numExperts);
        gradientBuffers[\expertW1] = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) });
        gradientBuffers[\expertB1] = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
        gradientBuffers[\expertW2] = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) });
        gradientBuffers[\expertB2] = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
        gradientBuffers[\q] = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
        gradientBuffers[\k] = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
        gradientBuffers[\v] = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
        gradientBuffers[\qBias] = Array.fill(numHeads, { this.zeroVector(headSize) });
        gradientBuffers[\kBias] = Array.fill(numHeads, { this.zeroVector(headSize) });
        gradientBuffers[\vBias] = Array.fill(numHeads, { this.zeroVector(headSize) });
        gradientBuffers[\featureGates] = Array.fill(numHeads, { this.zeroVector(inputSize) });
        gradientBuffers[\attentionWeight] = this.zeroMatrix(hiddenSize, numHeads * headSize);
        gradientBuffers[\attentionBias] = this.zeroVector(hiddenSize);
        gradientBuffers[\positionBias] = Array.fill(numHeads, { this.zeroVector(windowSize) });
        gradientBuffers[\residual] = this.zeroVector(hiddenSize);
        gradientBuffers[\residualPre] = this.zeroVector(hiddenSize);
        gradientBuffers[\attentionLinear] = this.zeroVector(hiddenSize);
        gradientBuffers[\attentionCombined] = this.zeroVector(numHeads * headSize);
        gradientBuffers[\expertCombined] = this.zeroVector(hiddenSize);
        gradientBuffers[\h0] = this.zeroVector(hiddenSize);
        gradientBuffers[\preHidden] = this.zeroVector(hiddenSize);
        gradientBuffers[\routerLogits] = this.zeroVector(numExperts);
        gradientBuffers[\router] = this.zeroVector(numExperts);
        gradientBuffers[\expertOutput] = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
        gradientBuffers[\expertHidden] = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
        ^gradientBuffers;
    }

    clearGradientBuffers {
        if(gradientBuffers.isNil, { this.initGradientBuffers; });
        this.zeroMatrixInPlace(gradientBuffers[\inputProjection]);
        this.zeroVectorInPlace(gradientBuffers[\inputBias]);
        this.zeroMatrixInPlace(gradientBuffers[\outputProjection]);
        this.zeroVectorInPlace(gradientBuffers[\outputBias]);
        this.zeroMatrixInPlace(gradientBuffers[\routerWeights]);
        this.zeroVectorInPlace(gradientBuffers[\routerBias]);
        numExperts.do({ |e|
            this.zeroMatrixInPlace(gradientBuffers[\expertW1][e]);
            this.zeroVectorInPlace(gradientBuffers[\expertB1][e]);
            this.zeroMatrixInPlace(gradientBuffers[\expertW2][e]);
            this.zeroVectorInPlace(gradientBuffers[\expertB2][e]);
            this.zeroVectorInPlace(gradientBuffers[\expertOutput][e]);
            this.zeroVectorInPlace(gradientBuffers[\expertHidden][e]);
        });
        numHeads.do({ |h|
            this.zeroMatrixInPlace(gradientBuffers[\q][h]);
            this.zeroMatrixInPlace(gradientBuffers[\k][h]);
            this.zeroMatrixInPlace(gradientBuffers[\v][h]);
            this.zeroVectorInPlace(gradientBuffers[\qBias][h]);
            this.zeroVectorInPlace(gradientBuffers[\kBias][h]);
            this.zeroVectorInPlace(gradientBuffers[\vBias][h]);
            this.zeroVectorInPlace(gradientBuffers[\featureGates][h]);
            this.zeroVectorInPlace(gradientBuffers[\positionBias][h]);
        });
        this.zeroMatrixInPlace(gradientBuffers[\attentionWeight]);
        this.zeroVectorInPlace(gradientBuffers[\attentionBias]);
        this.zeroVectorInPlace(gradientBuffers[\residual]);
        this.zeroVectorInPlace(gradientBuffers[\residualPre]);
        this.zeroVectorInPlace(gradientBuffers[\attentionLinear]);
        this.zeroVectorInPlace(gradientBuffers[\attentionCombined]);
        this.zeroVectorInPlace(gradientBuffers[\expertCombined]);
        this.zeroVectorInPlace(gradientBuffers[\h0]);
        this.zeroVectorInPlace(gradientBuffers[\preHidden]);
        this.zeroVectorInPlace(gradientBuffers[\routerLogits]);
        this.zeroVectorInPlace(gradientBuffers[\router]);
        ^gradientBuffers;
    }

    resetOptimizerState {
        ^{
            adamMInput = this.zeroMatrix(hiddenSize, inputSize); adamVInput = this.zeroMatrix(hiddenSize, inputSize);
            adamMInputBias = this.zeroVector(hiddenSize); adamVInputBias = this.zeroVector(hiddenSize);
            adamMOutput = this.zeroMatrix(outputSize, hiddenSize); adamVOutput = this.zeroMatrix(outputSize, hiddenSize);
            adamMOutputBias = this.zeroVector(outputSize); adamVOutputBias = this.zeroVector(outputSize);
            adamMQ = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) }); adamVQ = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
            adamMK = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) }); adamVK = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
            adamMV = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) }); adamVV = Array.fill(numHeads, { this.zeroMatrix(headSize, inputSize) });
            adamMBQ = Array.fill(numHeads, { this.zeroVector(headSize) }); adamVBQ = Array.fill(numHeads, { this.zeroVector(headSize) });
            adamMBK = Array.fill(numHeads, { this.zeroVector(headSize) }); adamVBK = Array.fill(numHeads, { this.zeroVector(headSize) });
            adamMBV = Array.fill(numHeads, { this.zeroVector(headSize) }); adamVBV = Array.fill(numHeads, { this.zeroVector(headSize) });
            adamMAttention = this.zeroMatrix(hiddenSize, numHeads * headSize); adamVAttention = this.zeroMatrix(hiddenSize, numHeads * headSize);
            adamMBAttention = this.zeroVector(hiddenSize); adamVBAttention = this.zeroVector(hiddenSize);
            adamMGates = Array.fill(numHeads, { this.zeroVector(inputSize) }); adamVGates = Array.fill(numHeads, { this.zeroVector(inputSize) });
            adamMRouter = this.zeroMatrix(numExperts, hiddenSize); adamVRouter = this.zeroMatrix(numExperts, hiddenSize);
            adamMBRouter = this.zeroVector(numExperts); adamVBRouter = this.zeroVector(numExperts);
            adamMExpertW1 = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) }); adamVExpertW1 = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) });
            adamMExpertB1 = Array.fill(numExperts, { this.zeroVector(hiddenSize) }); adamVExpertB1 = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
            adamMExpertW2 = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) }); adamVExpertW2 = Array.fill(numExperts, { this.zeroMatrix(hiddenSize, hiddenSize) });
            adamMExpertB2 = Array.fill(numExperts, { this.zeroVector(hiddenSize) }); adamVExpertB2 = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
            adamMPosition = Array.fill(numHeads, { this.zeroVector(windowSize) }); adamVPosition = Array.fill(numHeads, { this.zeroVector(windowSize) });
            this.initGradientBuffers;
            adamStep = 0; learningRateCurrent = learningRate; nil;
        }.value;
    }

    resetLearning {
        ^{
            shortMemory = List.new; lastInput = nil;
            lastPrediction = FloatArray.fill(outputSize, 0.5);
            lastDelta = FloatArray.fill(outputSize, 0.0);
            lastLearnedVelocity = this.zeroVector(inputSize);
            lastGeneratedVelocity = this.zeroVector(inputSize);
            generationHistory = List.new;
            generationDriftState = (deltaEMA: this.zeroVector(outputSize), emaDecay: 0.985, gain: 0.45, maxCorrection: 0.0025);
            generationDiversity = 0.0; generationNovelty = 1.0; generationMinDistance = 1.0; generationMeanDistance = 0.0; generationPressure = 0.0;
            localDiversity = 0.0; localContractionPressure = 0.0; localContractionCorrection = 0.0;
            lastTarget = nil;
            attentionProfile = FloatArray.fill(windowSize, 0.0);
            if(windowSize > 0, { attentionProfile[0] = 1.0; });
            headActivity = FloatArray.fill(numHeads, 0.0);
            expertUsage = FloatArray.fill(numExperts, 1.0 / numExperts);
            expertUsageEMA = FloatArray.fill(numExperts, 1.0 / numExperts);
            routerEntropyEMA = 0.0;
            loss = 0.0; outputLoss = 0.0; deltaLoss = 0.0; directionLoss = 0.0; expertBalanceLoss = 0.0;
            surprise = 0.0; surpriseEMA = 0.0; errorEMA = 0.0; replayLoss = 0.0; interferenceScore = 0.0;
            adaptiveState = IdentityDictionary.new;
            adaptiveState[\enabled] = true; adaptiveState[\replayMin] = 0.06; adaptiveState[\replayMax] = 0.20;
            adaptiveState[\threshold] = 0.0020; adaptiveState[\smoothing] = 0.95; adaptiveState[\replayBoost] = 0.06;
            adaptiveState[\interferenceEMA] = 0.0; adaptiveState[\replayRate] = replayRate.clip(0.06, 0.20);
            adaptiveState[\learningScale] = 1.0; adaptiveState[\protectionScale] = 1.0; adaptiveState[\adjustments] = 0;
            memoryRecall = 0.0; memoryNovelty = 0.0; memoryWriteScore = 0.0; entropy = 0.0;
            learningRateCurrent = learningRate; learnedEvents = 0;
            this.resetOptimizerState;
            totalEvents = 0; protectionScalar = 0.0; eventHistory = List.new;
            autoTuneEnabled = false; autoTuneInterval = 8; autoTuneCounter = 0; autoTuneStrength = 0.20;
            autoTuneSmoothing = 0.90; autoTuneTargetNovelty = 0.18; autoTuneTargetDiversity = 0.12;
            autoTuneNoveltyEMA = 0.0; autoTuneDiversityEMA = 0.0; autoTuneLastError = 0.0; autoTuneAdjustmentCount = 0;
            autoTuneExplorationMin = 0.0005; autoTuneExplorationMax = 0.0300; autoTuneNoiseMin = 0.0; autoTuneNoiseMax = 0.0100;
            autoTuneTemperatureMin = 0.50; autoTuneTemperatureMax = 2.50;
            metaLearnEnabled = false; metaLearnInterval = 16; metaLearnCounter = 0; metaLearnStrength = 0.12;
            metaLearnSmoothing = 0.95; metaLearnTargetError = 0.015; metaLearnTargetSurprise = 0.08;
            metaLearnTargetRecall = 0.50; metaLearnTargetInterference = 0.0;
            metaLearnErrorEMA = 0.0; metaLearnSurpriseEMA = 0.0; metaLearnRecallEMA = 0.0; metaLearnInterferenceEMA = 0.0;
            metaLearnPlasticityPressure = 0.0; metaLearnStabilityPressure = 0.0; metaLearnAdjustmentCount = 0;
            metaLearnLearningRateMin = 0.00005; metaLearnLearningRateMax = 0.00200;
            metaLearnReplayRateMin = 0.02; metaLearnReplayRateMax = 0.30;
            metaLearnProtectionMin = 0.05; metaLearnProtectionMax = 0.60;
            metaLearnRetrievalGainMin = 0.02; metaLearnRetrievalGainMax = 0.30;
            "V30.3.3.1 Learning+ Dynamic Trajectory Memory runtime and optimizer reset / weights and memories preserved".postln;
            nil;
        }.value;
    }

    resetMemory {
        ^{
            longMemory = Array.newClear(longMemorySize);
            longMemoryImportance = FloatArray.fill(longMemorySize, 0.0);
            longMemoryAge = Array.fill(longMemorySize, 0);
            longMemoryUsage = FloatArray.fill(longMemorySize, 0.0);
            longMemorySurprise = FloatArray.fill(longMemorySize, 0.0);
            replayInputs = Array.newClear(longMemorySize);
            replayTargets = Array.newClear(longMemorySize);
            replayImportance = FloatArray.fill(longMemorySize, 0.0);
            memoryCount = 0; replayCount = 0;
            trajectoryMemory = List.new; trajectoryCount = 0;
            trajectoryVelocityContext = this.zeroVector(inputSize);
            trajectoryAccelerationContext = this.zeroVector(inputSize);
            trajectoryRecall = 0.0; trajectoryNovelty = 1.0;
            trajectoryWriteScore = 0.0; trajectoryLoss = 0.0;
            trajectoryIndices = Array.new; trajectoryWeights = Array.new;
            memoryContext = this.zeroVector(inputSize);
            memoryWeights = Array.new; memorySimilarity = Array.new;
            protectionScalar = 0.0;
            "V30.3.3.1 Learning+ Dynamic Trajectory Memory memory reset".postln;
            nil;
        }.value;
    }

    resetAll {
        ^{
            inputProjection = this.makeFloatMatrix(hiddenSize, inputSize, 0.08);
            inputBias = this.zeroVector(hiddenSize);
            outputProjection = this.makeFloatMatrix(outputSize, hiddenSize, 0.06);
            outputBias = this.zeroVector(outputSize);
            qWeights = Array.fill(numHeads, { this.makeFloatMatrix(headSize, inputSize, 0.08) });
            kWeights = Array.fill(numHeads, { this.makeFloatMatrix(headSize, inputSize, 0.08) });
            vWeights = Array.fill(numHeads, { this.makeFloatMatrix(headSize, inputSize, 0.08) });
            qBias = Array.fill(numHeads, { this.zeroVector(headSize) });
            kBias = Array.fill(numHeads, { this.zeroVector(headSize) });
            vBias = Array.fill(numHeads, { this.zeroVector(headSize) });
            outputAttentionWeight = this.makeFloatMatrix(hiddenSize, numHeads * headSize, 0.05);
            outputAttentionBias = this.zeroVector(hiddenSize);
            featureGateLogits = Array.fill(numHeads, { FloatArray.fill(inputSize, 0.0) });
            routerWeights = this.makeFloatMatrix(numExperts, hiddenSize, 0.05);
            routerBias = this.zeroVector(numExperts);
            expertW1 = Array.fill(numExperts, { this.makeFloatMatrix(hiddenSize, hiddenSize, 0.05) });
            expertB1 = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
            expertW2 = Array.fill(numExperts, { this.makeFloatMatrix(hiddenSize, hiddenSize, 0.05) });
            expertB2 = Array.fill(numExperts, { this.zeroVector(hiddenSize) });
            positionBias = Array.fill(numHeads, { FloatArray.fill(windowSize, 0.0) });
            this.resetMemory;
            this.resetLearning;
            "V30.3.3.1 Learning+ Dynamic Trajectory Memory full reset".postln;
            nil;
        }.value;
    }

    statusSilent {
        var attentionSum, entropyValue, safeImportance, safeAge, safeUsage;
        attentionSum = attentionProfile.sum.max(0.000000001);
        entropyValue = {
            var entropySum;
            entropySum = 0.0;
            attentionProfile.do({ |p|
                var q;
                q = (p / attentionSum).max(0.000000001);
                entropySum = entropySum + (q * q.log.neg);
            });
            entropySum;
        }.value;
        entropy = entropyValue;
        safeImportance = if(memoryCount > 0, { longMemoryImportance.copyRange(0, memoryCount - 1) }, { Array.new });
        safeAge = if(memoryCount > 0, { longMemoryAge.copyRange(0, memoryCount - 1) }, { Array.new });
        safeUsage = if(memoryCount > 0, { longMemoryUsage.copyRange(0, memoryCount - 1) }, { Array.new });
        ^(
            version: 30.331, parameters: parameterCount, inputSize: inputSize, outputSize: outputSize,
            hiddenSize: hiddenSize, numHeads: numHeads, headSize: headSize, numExperts: numExperts,
            windowSize: windowSize, longMemorySize: longMemorySize, learningRate: learningRateCurrent,
            loss: loss, outputLoss: outputLoss, deltaLoss: deltaLoss, directionLoss: directionLoss,
            expertBalanceLoss: expertBalanceLoss, errorEMA: errorEMA, surprise: surprise, surpriseEMA: surpriseEMA,
            memoryCount: memoryCount, replayCount: replayCount, memoryRecall: memoryRecall, memoryNovelty: memoryNovelty,
            memoryWriteScore: memoryWriteScore, replayLoss: replayLoss, interferenceScore: interferenceScore,
            adaptiveInterferenceEnabled: adaptiveState[\enabled], adaptiveReplayMin: adaptiveState[\replayMin],
            adaptiveReplayMax: adaptiveState[\replayMax], adaptiveInterferenceThreshold: adaptiveState[\threshold],
            adaptiveInterferenceSmoothing: adaptiveState[\smoothing], adaptiveReplayBoost: adaptiveState[\replayBoost],
            adaptiveInterferenceEMA: adaptiveState[\interferenceEMA], adaptiveReplayRate: adaptiveState[\replayRate],
            adaptiveLearningScale: adaptiveState[\learningScale], adaptiveProtectionScale: adaptiveState[\protectionScale],
            adaptiveInterferenceAdjustments: adaptiveState[\adjustments].asInteger,
            trajectoryCount: trajectoryCount, trajectoryRecall: trajectoryRecall, trajectoryNovelty: trajectoryNovelty,
            trajectoryWriteScore: trajectoryWriteScore, trajectoryLoss: trajectoryLoss,
            trajectoryVelocityContext: trajectoryVelocityContext.copy, trajectoryAccelerationContext: trajectoryAccelerationContext.copy,
            protectionScalar: protectionScalar, entropy: entropy, expertUsage: expertUsage.copy,
            expertUsageEMA: expertUsageEMA.copy, routerEntropyEMA: routerEntropyEMA,
            attentionProfile: attentionProfile.copy, headActivity: headActivity.copy,
            headFeatureUsage: headFeatureUsage.collect({ |item| item.copy }),
            memoryImportance: safeImportance, memoryAge: safeAge, memoryUsage: safeUsage,
            generationDiversity: generationDiversity, generationNovelty: generationNovelty,
            generationMinDistance: generationMinDistance, generationMeanDistance: generationMeanDistance,
            generationPressure: generationPressure, localDiversity: localDiversity,
            localContractionPressure: localContractionPressure, localContractionCorrection: localContractionCorrection,
            autoTuneEnabled: autoTuneEnabled, autoTuneAdjustmentCount: autoTuneAdjustmentCount,
            autoTuneNoveltyEMA: autoTuneNoveltyEMA, autoTuneDiversityEMA: autoTuneDiversityEMA,
            autoTuneLastError: autoTuneLastError, metaLearnEnabled: metaLearnEnabled,
            metaLearnAdjustmentCount: metaLearnAdjustmentCount, metaLearnPlasticityPressure: metaLearnPlasticityPressure,
            metaLearnStabilityPressure: metaLearnStabilityPressure, normalizationEnabled: normalizationState[\enabled],
            normalizationCount: normalizationState[\count], multiResolutionEnabled: multiResolutionState[\enabled],
            stabilityGuardEnabled: stabilityGuardState[\enabled], stabilityRollbacks: stabilityGuardState[\rollbacks],
            stabilityLastReason: stabilityGuardState[\lastReason], learnedEvents: learnedEvents,
            adamStep: adamStep, totalEvents: totalEvents, prediction: lastPrediction.copy,
            delta: lastDelta.copy, learnedVelocity: lastLearnedVelocity.copy, generatedVelocity: lastGeneratedVelocity.copy
        );
    }

    status {
        ^{
            var attentionSum, entropyValue;
            attentionSum = attentionProfile.sum.max(0.000000001);
            entropyValue = {
            var entropySum;
            entropySum = 0.0;
            attentionProfile.do({ |p|
                var q;
                q = (p / attentionSum).max(0.000000001);
                entropySum = entropySum + (q * q.log.neg);
            });
            entropySum;
        }.value;
            entropy = entropyValue;
            "".postln; "============================================================".postln;
            "TRANSFORMER VECTOR V30.3.3.1 Learning+ Dynamic Trajectory Memory".postln;
            "MEMORY / LEARNING+ / ADAPTATION / ANTI-INTERFERENCE".postln;
            "============================================================".postln;
            ("parameters: " ++ parameterCount).postln; ("inputSize: " ++ inputSize).postln;
            ("outputSize: " ++ outputSize).postln; ("hiddenSize: " ++ hiddenSize).postln;
            ("numHeads: " ++ numHeads).postln; ("headSize: " ++ headSize).postln;
            ("numExperts: " ++ numExperts).postln; ("windowSize: " ++ windowSize).postln;
            ("longMemorySize: " ++ longMemorySize).postln; ("memoryRecallSize: " ++ memoryRecallSize).postln;
            ("baseLearningRate: " ++ learningRate).postln; ("adaptiveLearningRate: " ++ learningRateCurrent).postln;
            ("adaptationFastRate: " ++ adaptationFastRate).postln; ("adaptationSlowRate: " ++ adaptationSlowRate).postln;
            ("protectionScalar: " ++ protectionScalar).postln; ("learnedEvents: " ++ learnedEvents).postln;
            ("adamStep: " ++ adamStep).postln; ("totalEvents: " ++ totalEvents).postln;
            ("loss: " ++ loss).postln; ("outputLoss: " ++ outputLoss).postln;
            ("deltaLoss: " ++ deltaLoss).postln; ("directionLoss: " ++ directionLoss).postln;
            ("expertBalanceLoss: " ++ expertBalanceLoss).postln; ("errorEMA: " ++ errorEMA).postln;
            ("surprise: " ++ surprise).postln; ("surpriseEMA: " ++ surpriseEMA).postln;
            ("memoryCount: " ++ memoryCount).postln; ("replayCount: " ++ replayCount).postln;
            ("memoryRecall: " ++ memoryRecall).postln; ("memoryNovelty: " ++ memoryNovelty).postln;
            ("memoryWriteScore: " ++ memoryWriteScore).postln; ("trajectoryCount: " ++ trajectoryCount).postln;
            ("trajectoryRecall: " ++ trajectoryRecall).postln; ("trajectoryNovelty: " ++ trajectoryNovelty).postln;
            ("trajectoryWriteScore: " ++ trajectoryWriteScore).postln; ("trajectoryLoss: " ++ trajectoryLoss).postln;
            "trajectoryVelocityContext:".postln; trajectoryVelocityContext.postln;
            "trajectoryAccelerationContext:".postln; trajectoryAccelerationContext.postln;
            ("replayLoss: " ++ replayLoss).postln; ("interferenceScore: " ++ interferenceScore).postln;
            ("autoTuneEnabled: " ++ autoTuneEnabled).postln; ("autoTuneAdjustmentCount: " ++ autoTuneAdjustmentCount).postln;
            ("autoTuneNoveltyEMA: " ++ autoTuneNoveltyEMA).postln; ("autoTuneDiversityEMA: " ++ autoTuneDiversityEMA).postln;
            ("autoTuneLastError: " ++ autoTuneLastError).postln; ("metaLearnEnabled: " ++ metaLearnEnabled).postln;
            ("metaLearnAdjustmentCount: " ++ metaLearnAdjustmentCount).postln;
            ("metaLearnPlasticityPressure: " ++ metaLearnPlasticityPressure).postln;
            ("metaLearnStabilityPressure: " ++ metaLearnStabilityPressure).postln;
            ("entropy: " ++ entropy).postln; "expertUsage:".postln; expertUsage.postln;
            "memoryImportance:".postln; longMemoryImportance.copyRange(0, memoryCount - 1).postln;
            "memoryAge:".postln; longMemoryAge.copyRange(0, memoryCount - 1).postln;
            "memoryUsage:".postln; longMemoryUsage.copyRange(0, memoryCount - 1).postln;
            "attentionProfile:".postln; attentionProfile.postln; "headActivity:".postln; headActivity.postln;
            "headFeatureUsage:".postln; headFeatureUsage.postln; "prediction:".postln; lastPrediction.postln;
            "delta:".postln; lastDelta.postln; "learnedVelocity:".postln; lastLearnedVelocity.postln;
            "generatedVelocity:".postln; lastGeneratedVelocity.postln;
            "============================================================".postln;
            (
                version: 30.331, parameters: parameterCount, inputSize: inputSize, outputSize: outputSize,
                hiddenSize: hiddenSize, numHeads: numHeads, headSize: headSize, numExperts: numExperts,
                windowSize: windowSize, longMemorySize: longMemorySize, learningRate: learningRateCurrent,
                loss: loss, outputLoss: outputLoss, deltaLoss: deltaLoss, directionLoss: directionLoss,
                expertBalanceLoss: expertBalanceLoss, errorEMA: errorEMA, surprise: surprise, surpriseEMA: surpriseEMA,
                memoryCount: memoryCount, replayCount: replayCount, memoryRecall: memoryRecall, memoryNovelty: memoryNovelty,
                memoryWriteScore: memoryWriteScore, replayLoss: replayLoss, interferenceScore: interferenceScore,
                trajectoryCount: trajectoryCount, trajectoryRecall: trajectoryRecall, trajectoryNovelty: trajectoryNovelty,
                trajectoryWriteScore: trajectoryWriteScore, trajectoryLoss: trajectoryLoss,
                protectionScalar: protectionScalar, entropy: entropy, expertUsage: expertUsage.copy,
                attentionProfile: attentionProfile.copy, headActivity: headActivity.copy,
                headFeatureUsage: headFeatureUsage.collect({ |x| x.copy }),
                memoryImportance: longMemoryImportance.copy, memoryAge: longMemoryAge.copy,
                memoryUsage: longMemoryUsage.copy, learnedEvents: learnedEvents, adamStep: adamStep,
                prediction: lastPrediction.copy, delta: lastDelta.copy,
                learnedVelocity: lastLearnedVelocity.copy, generatedVelocity: lastGeneratedVelocity.copy
            );
        }.value;
    }

    config {
        ^{
            (
                version: 30.331, inputSize: inputSize, outputSize: outputSize, windowSize: windowSize,
                longMemorySize: longMemorySize, hiddenSize: hiddenSize, numHeads: numHeads, headSize: headSize,
                numExperts: numExperts, learningRate: learningRate, beta1: beta1, beta2: beta2, epsilon: epsilon,
                temperature: temperature, residualScale: residualScale, expertScale: expertScale, deltaScale: deltaScale,
                surpriseThreshold: surpriseThreshold, surpriseGain: surpriseGain, gateLearningRate: gateLearningRate,
                gradientClip: gradientClip, memoryWriteThreshold: memoryWriteThreshold, memoryRetrievalGain: memoryRetrievalGain,
                memoryRetrievalTemperature: memoryRetrievalTemperature, memoryDecay: memoryDecay,
                memoryConsolidationRate: memoryConsolidationRate, replayRate: replayRate, replayBatchSize: replayBatchSize,
                memoryRecallSize: memoryRecallSize, protectionStrength: protectionStrength,
                adaptationFastRate: adaptationFastRate, adaptationSlowRate: adaptationSlowRate,
                predictionLossWeight: predictionLossWeight, deltaLossWeight: deltaLossWeight,
                directionLossWeight: directionLossWeight, replayPriorityMix: replayPriorityMix,
                replayUniformMix: replayUniformMix, expertBalanceStrength: expertBalanceStrength,
                headSpecializationThreshold: headSpecializationThreshold, headSpecializationStrength: headSpecializationStrength,
                interferenceTestSteps: interferenceTestSteps, trajectoryMemorySize: trajectoryMemorySize,
                trajectoryRecallSize: trajectoryRecallSize, trajectoryRetrievalGain: trajectoryRetrievalGain,
                trajectoryVelocityGain: trajectoryVelocityGain, trajectoryAccelerationGain: trajectoryAccelerationGain,
                trajectoryVelocityClip: trajectoryVelocityClip, trajectoryAccelerationClip: trajectoryAccelerationClip,
                trajectoryDecay: trajectoryDecay, trajectoryUsageDecay: trajectoryUsageDecay,
                trajectoryNoveltyWeight: trajectoryNoveltyWeight, trajectoryInputWeight: trajectoryInputWeight,
                trajectoryVelocityWeight: trajectoryVelocityWeight, trajectoryExplorationGain: trajectoryExplorationGain,
                diversityHistorySize: diversityHistorySize, diversityRadius: diversityRadius,
                diversityRepulsionGain: diversityRepulsionGain, diversityNoiseGain: diversityNoiseGain,
                diversityMaxCorrection: diversityMaxCorrection, diversityAdaptiveGain: diversityAdaptiveGain,
                localDiversityWindow: localDiversityWindow, localDiversityFloor: localDiversityFloor,
                localDiversityGain: localDiversityGain, localDiversityMaxCorrection: localDiversityMaxCorrection,
                parameters: parameterCount
            );
        }.value;
    }

    generalizationTest { |input, target|
        ^{
            var p = this.predict(input);
            var l = this.maskedMSE(p, this.cleanOutputVector(target));
            ^(generalizationLoss: l, prediction: p.copy, target: this.cleanOutputVector(target));
        }.value;
    }

    memoryTest { |input|
        ^{
            var x = this.cleanVector(input);
            var result = this.retrieveMemory(x);
            ^(
                memoryCount: memoryCount, recallCount: result[\indices].size,
                indices: result[\indices].copy, weights: result[\weights].copy,
                similarities: memorySimilarity.copy, context: result[\context].copy,
                novelty: this.calculateMemoryNovelty(x)
            );
        }.value;
    }

    interferenceTest { |input, target|
        var state, y, before, steps, totalReplayLoss=0.0, idx, replayState, replayTarget;
        y = this.cleanOutputVector(target);
        state = this.buildForward(input, nil, lastLearnedVelocity);
        before = this.maskedMSE(state[\prediction], y);
        steps = interferenceTestSteps.min(replayCount);
        steps.do({ |i|
            idx = ((i * 1103515245) + adamStep).abs % replayCount.max(1);
            replayTarget = this.cleanOutputVector(replayTargets[idx]);
            replayState = this.buildForward(replayInputs[idx], nil, lastLearnedVelocity);
            totalReplayLoss = totalReplayLoss + this.maskedMSE(replayState[\prediction], replayTarget);
        });
        interferenceScore = if(steps > 0, { ((totalReplayLoss / steps) - before) / (before + 0.000001) }, { 0.0 });
        ^(lossBefore: before, lossAfter: before, replayConflict: interferenceScore,
            interferenceScore: interferenceScore, probeSteps: steps, destructive: false,
            prediction: state[\prediction].copy, target: y.copy);
    }

    lossValue { ^loss; }
    surpriseValue { ^surprise; }
    entropyValue { ^entropy; }
    learnedEventsValue { ^learnedEvents; }
    interferenceScoreValue { ^interferenceScore; }
    adaptiveReplayRateValue { ^adaptiveState[\replayRate]; }
    adaptiveInterferenceValue { ^adaptiveState[\interferenceEMA]; }
    adaptiveProtectionScaleValue { ^adaptiveState[\protectionScale]; }
    adaptiveLearningScaleValue { ^adaptiveState[\learningScale]; }
    adaptiveInterferenceAdjustmentsValue { ^adaptiveState[\adjustments].asInteger; }

    adaptiveStateStatus {
        ^(
            enabled: adaptiveState[\enabled], replayMin: adaptiveState[\replayMin], replayMax: adaptiveState[\replayMax],
            threshold: adaptiveState[\threshold], smoothing: adaptiveState[\smoothing], replayBoost: adaptiveState[\replayBoost],
            interferenceEMA: adaptiveState[\interferenceEMA], replayRate: adaptiveState[\replayRate],
            learningScale: adaptiveState[\learningScale], protectionScale: adaptiveState[\protectionScale],
            adjustments: adaptiveState[\adjustments].asInteger
        );
    }

    parameters { ^parameterCount; }
    optimizationInfo {
        ^(
            version: 30.331,
            superCollider: "3.14",
            behaviorPreserving: true,
            floatArrayHotPath: true,
            cachedFeatureGates: true,
            temporaryOutputColumnsRemoved: true,
            persistentGradientWorkspace: true,
            gradientPreallocation: true,
            leanForwardAllocations: true,
            readOnlyMemoryReferences: true
        );
    }
    generationDiversity { ^generationDiversity; }
    generationNovelty { ^generationNovelty; }
    memoryCountValue { ^memoryCount; }
    replayCountValue { ^replayCount; }
    trajectoryCountValue { ^trajectoryCount; }

    saveArchive { |path|
        var finalPath = path.asString.standardizePath;
        this.writeArchive(finalPath);
        ("HPtransformerRT saved: " ++ finalPath).postln;
        ^finalPath;
    }

    *readArchive { |path|
        var finalPath, model;
        finalPath = path.asString.standardizePath;
        if(File.exists(finalPath).not, { Error("Transformer archive not found: " ++ finalPath).throw; });
        model = Object.readArchive(finalPath);
        if(model.isKindOf(HPtransformerRT).not, { Error("Archive is not an HPtransformerRT: " ++ finalPath).throw; });
        ("HPtransformerRT restored: " ++ finalPath).postln;
        ^model;
    }

    copySnapshotValue { |value|
        if(value.isNil, { ^nil });
        if(value.isArray, { ^value.collect({ |item| this.copySnapshotValue(item) }); });
        if(value.isKindOf(List), { ^value.collect({ |item| this.copySnapshotValue(item) }).as(List); });
        if(value.isKindOf(IdentityDictionary) or: { value.isKindOf(Event) }, {
            var result = IdentityDictionary.new;
            value.keysValuesDo({ |key, item| result[key] = this.copySnapshotValue(item); });
            ^result;
        });
        ^value;
    }

    exportInferenceSnapshot {
        ^(
            snapshotVersion: 1,
            structure: (inputSize: inputSize, outputSize: outputSize, windowSize: windowSize, longMemorySize: longMemorySize, hiddenSize: hiddenSize, numHeads: numHeads, headSize: headSize, numExperts: numExperts, trajectoryMemorySize: trajectoryMemorySize),
            weights: (
                inputProjection: this.copySnapshotValue(inputProjection), inputBias: this.copySnapshotValue(inputBias),
                outputProjection: this.copySnapshotValue(outputProjection), outputBias: this.copySnapshotValue(outputBias),
                qWeights: this.copySnapshotValue(qWeights), kWeights: this.copySnapshotValue(kWeights), vWeights: this.copySnapshotValue(vWeights),
                qBias: this.copySnapshotValue(qBias), kBias: this.copySnapshotValue(kBias), vBias: this.copySnapshotValue(vBias),
                outputAttentionWeight: this.copySnapshotValue(outputAttentionWeight), outputAttentionBias: this.copySnapshotValue(outputAttentionBias),
                featureGateLogits: this.copySnapshotValue(featureGateLogits), routerWeights: this.copySnapshotValue(routerWeights), routerBias: this.copySnapshotValue(routerBias),
                expertW1: this.copySnapshotValue(expertW1), expertB1: this.copySnapshotValue(expertB1), expertW2: this.copySnapshotValue(expertW2), expertB2: this.copySnapshotValue(expertB2),
                positionBias: this.copySnapshotValue(positionBias)
            ),
            memory: (
                memoryCount: memoryCount, longMemory: this.copySnapshotValue(longMemory), longMemoryImportance: this.copySnapshotValue(longMemoryImportance),
                longMemoryAge: this.copySnapshotValue(longMemoryAge), longMemoryUsage: this.copySnapshotValue(longMemoryUsage), longMemorySurprise: this.copySnapshotValue(longMemorySurprise),
                trajectoryCount: trajectoryCount, trajectoryMemory: this.copySnapshotValue(trajectoryMemory)
            ),
            parameters: this.runtimeConfig
        );
    }

    loadInferenceSnapshot { |snapshot|
        var structure, weights, memory, settings;
        if(snapshot.isNil, { Error("Inference snapshot is nil").throw });
        if(snapshot[\snapshotVersion] != 1, { Error("Unsupported inference snapshot version").throw; });
        structure = snapshot[\structure];
        if(structure[\inputSize] != inputSize, { Error("Snapshot inputSize mismatch").throw; });
        if(structure[\outputSize] != outputSize, { Error("Snapshot outputSize mismatch").throw; });
        if(structure[\windowSize] != windowSize, { Error("Snapshot windowSize mismatch").throw; });
        if(structure[\hiddenSize] != hiddenSize, { Error("Snapshot hiddenSize mismatch").throw; });
        if(structure[\numHeads] != numHeads, { Error("Snapshot numHeads mismatch").throw; });
        if(structure[\headSize] != headSize, { Error("Snapshot headSize mismatch").throw; });
        if(structure[\numExperts] != numExperts, { Error("Snapshot numExperts mismatch").throw; });
        weights = snapshot[\weights];
        inputProjection = weights[\inputProjection]; inputBias = weights[\inputBias];
        outputProjection = weights[\outputProjection]; outputBias = weights[\outputBias];
        qWeights = weights[\qWeights]; kWeights = weights[\kWeights]; vWeights = weights[\vWeights];
        qBias = weights[\qBias]; kBias = weights[\kBias]; vBias = weights[\vBias];
        outputAttentionWeight = weights[\outputAttentionWeight]; outputAttentionBias = weights[\outputAttentionBias];
        featureGateLogits = weights[\featureGateLogits]; routerWeights = weights[\routerWeights]; routerBias = weights[\routerBias];
        expertW1 = weights[\expertW1]; expertB1 = weights[\expertB1]; expertW2 = weights[\expertW2]; expertB2 = weights[\expertB2];
        positionBias = weights[\positionBias];
        memory = snapshot[\memory];
        memoryCount = memory[\memoryCount].asInteger.clip(0, longMemorySize);
        longMemory = memory[\longMemory]; longMemoryImportance = memory[\longMemoryImportance];
        longMemoryAge = memory[\longMemoryAge]; longMemoryUsage = memory[\longMemoryUsage];
        longMemorySurprise = memory[\longMemorySurprise];
        trajectoryCount = memory[\trajectoryCount].asInteger.clip(0, trajectoryMemorySize);
        trajectoryMemory = memory[\trajectoryMemory].as(List);
        settings = snapshot[\parameters];
        settings.keysValuesDo({ |key, value|
            if(key != \torusMask or: { value.notNil }, { this.setParameter(key, value, false); });
        });
        ^this;
    }

    exportGenerationState {
        ^(
            shortMemory: this.copySnapshotValue(shortMemory), generationHistory: this.copySnapshotValue(generationHistory),
            generationDriftState: this.copySnapshotValue(generationDriftState), lastGeneratedVelocity: this.copySnapshotValue(lastGeneratedVelocity),
            lastInput: this.copySnapshotValue(lastInput), lastPrediction: this.copySnapshotValue(lastPrediction), lastDelta: this.copySnapshotValue(lastDelta),
            generationDiversity: generationDiversity, generationNovelty: generationNovelty, generationMinDistance: generationMinDistance,
            generationMeanDistance: generationMeanDistance, generationPressure: generationPressure, localDiversity: localDiversity,
            localContractionPressure: localContractionPressure, localContractionCorrection: localContractionCorrection,
            autoTuneCounter: autoTuneCounter, autoTuneNoveltyEMA: autoTuneNoveltyEMA, autoTuneDiversityEMA: autoTuneDiversityEMA,
            autoTuneLastError: autoTuneLastError, autoTuneAdjustmentCount: autoTuneAdjustmentCount
        );
    }

    loadGenerationState { |state|
        if(state.isNil, { ^this });
        shortMemory = state[\shortMemory].as(List);
        generationHistory = state[\generationHistory].as(List);
        generationDriftState = state[\generationDriftState];
        lastGeneratedVelocity = if(state[\lastGeneratedVelocity].notNil, { state[\lastGeneratedVelocity] }, { if(state[\lastObservedVelocity].notNil, { state[\lastObservedVelocity] }, { this.zeroVector(inputSize) }); });
        lastInput = state[\lastInput]; lastPrediction = state[\lastPrediction]; lastDelta = state[\lastDelta];
        generationDiversity = state[\generationDiversity]; generationNovelty = state[\generationNovelty];
        generationMinDistance = state[\generationMinDistance]; generationMeanDistance = state[\generationMeanDistance];
        generationPressure = state[\generationPressure]; localDiversity = state[\localDiversity];
        localContractionPressure = state[\localContractionPressure]; localContractionCorrection = state[\localContractionCorrection];
        autoTuneCounter = state[\autoTuneCounter]; autoTuneNoveltyEMA = state[\autoTuneNoveltyEMA];
        autoTuneDiversityEMA = state[\autoTuneDiversityEMA]; autoTuneLastError = state[\autoTuneLastError];
        autoTuneAdjustmentCount = state[\autoTuneAdjustmentCount];
        ^this;
    }

    markUnifiedRuntimeOnly { unifiedRuntimeRole = true; unifiedCurrentRuntime = nil; unifiedPendingRuntime = nil; ^this; }

    makeUnifiedRuntime {
        var cfg, runtime;
        cfg = this.config;
        runtime = HPtransformerRT.new(
            inputSize: cfg[\inputSize], outputSize: cfg[\outputSize], windowSize: cfg[\windowSize],
            longMemorySize: cfg[\longMemorySize], hiddenSize: cfg[\hiddenSize], numHeads: cfg[\numHeads],
            headSize: cfg[\headSize], numExperts: cfg[\numExperts], trajectoryMemorySize: trajectoryMemorySize, torusMask: torusMask
        );
        runtime.markUnifiedRuntimeOnly;
        ^runtime;
    }

    enableUnifiedRCU {
        if(unifiedRuntimeRole, { ^this });
        if(unifiedCurrentRuntime.isNil, {
            unifiedCurrentRuntime = this.makeUnifiedRuntime;
            unifiedPendingRuntime = this.makeUnifiedRuntime;
            unifiedCurrentRuntime.loadInferenceSnapshot(this.exportInferenceSnapshot);
            unifiedPendingRuntime.loadInferenceSnapshot(this.exportInferenceSnapshot);
            unifiedTrainingVersion = 0; unifiedPublishedVersion = 0;
            unifiedSnapshotPending = false; unifiedLearningEnabled = true; unifiedGenerationEnabled = true;
        });
        ^this;
    }

unifiedRCUEnabled { ^unifiedCurrentRuntime.notNil; }
    disableUnifiedRCU {
        if(unifiedRuntimeRole, { ^this; });
        this.stopAllMorphs;
        unifiedCurrentRuntime = nil;
        unifiedPendingRuntime = nil;
        unifiedSnapshotPending = false;
        unifiedTrainingVersion = 0;
        unifiedPublishedVersion = 0;
        ^this;
    }

    prepareSnapshot {
        this.enableUnifiedRCU;
        unifiedPendingRuntime.loadInferenceSnapshot(this.exportInferenceSnapshot);
        unifiedTrainingVersion = unifiedTrainingVersion + 1;
        unifiedSnapshotPending = true;
        ^unifiedTrainingVersion;
    }

    commitSnapshot {
        var oldRuntime, generationState;
        this.enableUnifiedRCU;
        if(unifiedSnapshotPending, {
            generationState = unifiedCurrentRuntime.exportGenerationState;
            unifiedPendingRuntime.loadGenerationState(generationState);
            oldRuntime = unifiedCurrentRuntime;
            unifiedCurrentRuntime = unifiedPendingRuntime;
            unifiedPendingRuntime = oldRuntime;
            unifiedPublishedVersion = unifiedTrainingVersion;
            unifiedSnapshotPending = false;
        });
        ^unifiedPublishedVersion;
    }

    publishNow { this.prepareSnapshot; ^this.commitSnapshot; }
    discardPendingSnapshot { unifiedSnapshotPending = false; ^this; }

    trainingVersion { ^unifiedTrainingVersion; }
    publishedVersion { ^unifiedPublishedVersion; }
    snapshotPending { ^unifiedSnapshotPending; }

    enableLearning { unifiedLearningEnabled = true; ^true; }
    disableLearning { unifiedLearningEnabled = false; ^false; }
    enableGeneration { unifiedGenerationEnabled = true; ^true; }
    disableGeneration { unifiedGenerationEnabled = false; ^false; }

    setParameter { |name, value, post=true|
        var result = this.setParameterLocal(name, value, post);
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            unifiedCurrentRuntime.setParameterLocal(name, value, false);
            unifiedPendingRuntime.setParameterLocal(name, value, false);
        });
        ^result;
    }

    setTrainingParameter { |name, value, post=true| ^this.setParameterLocal(name, value, post); }

    setTrainingParameters { |settings, post=true|
        settings.keysValuesDo({ |key, value| this.setParameterLocal(key, value, post); });
        ^this.runtimeConfig;
    }

    setRuntimeParameter { |name, value, post=true|
        this.enableUnifiedRCU;
        ^unifiedCurrentRuntime.setParameterLocal(name, value, post);
    }

    setRuntimeParameters { |settings, post=true|
        this.enableUnifiedRCU;
        settings.keysValuesDo({ |key, value| unifiedCurrentRuntime.setParameterLocal(key, value, post); });
        ^unifiedCurrentRuntime.runtimeConfig;
    }

    unifiedRuntimeStatus { this.enableUnifiedRCU; ^unifiedCurrentRuntime.status; }

    unifiedRCUStatus {
        ^(
            enabled: unifiedCurrentRuntime.notNil,
            trainingVersion: unifiedTrainingVersion,
            publishedVersion: unifiedPublishedVersion,
            snapshotPending: unifiedSnapshotPending,
            learningEnabled: unifiedLearningEnabled,
            generationEnabled: unifiedGenerationEnabled,
            activeMorphs: unifiedParameterMorphs.keys.asArray
        );
    }

    stopMorph { |name|
        var key, routine;
        key = name.asSymbol;
        routine = unifiedParameterMorphs[key];
        if(routine.notNil, { routine.stop; unifiedParameterMorphs.removeAt(key); });
        ^this;
    }

    stopAllMorphs {
        unifiedParameterMorphs.keysValuesDo({ |key, routine| if(routine.notNil, { routine.stop }); });
        unifiedParameterMorphs.clear;
        ^this;
    }

    morphParameter { |name, target, duration=1.0, steps=50, clock|
        var key, start, count, routine;
        key = name.asSymbol;
        start = this.getParameter(key);
        count = steps.asInteger.max(1);
        if(start.isNumber.not or: { target.isNumber.not }, { Error("morphSharedParameter requires a numeric parameter").throw; });
        this.stopMorph(key);
        routine = Routine({
            count.do({ |index|
                this.setParameter(key, start + ((target.asFloat - start) * ((index + 1) / count)), false);
                (duration.asFloat.max(0.0) / count).wait;
            });
            unifiedParameterMorphs.removeAt(key);
        });
        unifiedParameterMorphs[key] = routine;
        routine.play(clock ? AppClock);
        ^routine;
    }

    morphRuntimeParameter { |name, target, duration=1.0, steps=50, clock|
        var key, morphKey, start, count, routine;
        this.enableUnifiedRCU;
        key = name.asSymbol;
        morphKey = ("runtime_" ++ key.asString).asSymbol;
        start = unifiedCurrentRuntime.getParameter(key);
        count = steps.asInteger.max(1);
        if(start.isNumber.not or: { target.isNumber.not }, { Error("morphRuntimeParameter requires a numeric parameter").throw; });
        this.stopMorph(morphKey);
        routine = Routine({
            count.do({ |index|
                this.setRuntimeParameter(key, start + ((target.asFloat - start) * ((index + 1) / count)), false);
                (duration.asFloat.max(0.0) / count).wait;
            });
            unifiedParameterMorphs.removeAt(morphKey);
        });
        unifiedParameterMorphs[morphKey] = routine;
        routine.play(clock ? AppClock);
        ^routine;
    }

    morphTrainingParameter { |name, target, duration=1.0, steps=50, clock|
        var key, morphKey, start, count, routine;
        key = name.asSymbol;
        morphKey = ("training_" ++ key.asString).asSymbol;
        start = this.getParameter(key);
        count = steps.asInteger.max(1);
        if(start.isNumber.not or: { target.isNumber.not }, { Error("morphTrainingParameter requires a numeric parameter").throw; });
        this.stopMorph(morphKey);
        routine = Routine({
            count.do({ |index|
                this.setTrainingParameter(key, start + ((target.asFloat - start) * ((index + 1) / count)), false);
                (duration.asFloat.max(0.0) / count).wait;
            });
            unifiedParameterMorphs.removeAt(morphKey);
        });
        unifiedParameterMorphs[morphKey] = routine;
        routine.play(clock ? AppClock);
        ^routine;
    }
}