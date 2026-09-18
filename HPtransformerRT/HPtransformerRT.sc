/* HPtransformerRT V30.0 - SuperCollider 3.14 class source.
Revisions: separate temperatures; correct softmax temperature gradients;
Q/K/V feature-gate gradients; signed velocity metric; persistent expert diagnostics;
non-destructive interference probe. The class name differs to coexist with HPtransformer.
*/

HPtransformerRT {
    var attentionTemperature, routerTemperature, trajectoryRetrievalTemperature, expertUsageEMA, routerEntropyEMA, torusMask, generationWindowSize, localDiversityMaxCorrection, localDiversityGain, localDiversityFloor, localDiversityWindow, diversityAdaptiveGain, diversityMaxCorrection, diversityNoiseGain, diversityRepulsionGain, diversityRadius, diversityHistorySize, trajectoryExplorationGain, trajectoryVelocityWeight, trajectoryInputWeight, trajectoryNoveltyWeight, trajectoryUsageDecay, trajectoryDecay, trajectoryAccelerationClip, trajectoryVelocityClip, trajectoryAccelerationGain, trajectoryVelocityGain, trajectoryRetrievalGain, trajectoryRecallSize, trajectoryMemorySize, interferenceTestSteps, headSpecializationStrength, headSpecializationThreshold, expertBalanceStrength, replayUniformMix, replayPriorityMix, directionLossWeight, deltaLossWeight, predictionLossWeight, adaptationSlowRate, adaptationFastRate, protectionStrength, memoryRecallSize, replayBatchSize, replayRate, memoryUsageDecay, memoryConsolidationRate, memoryDecay, memoryRetrievalTemperature, memoryRetrievalGain, memoryWriteThreshold, gradientClip, gateLearningRate, surpriseGain, surpriseThreshold, deltaScale, expertScale, residualScale, temperature, epsilon, beta2, beta1, learningRate, numExperts, headSize, numHeads, hiddenSize, longMemorySize, windowSize, outputSize, inputSize, parameterCount, inputProjection, inputBias, outputProjection, outputBias, qWeights, kWeights, vWeights, qBias, kBias, vBias, outputAttentionWeight, outputAttentionBias, featureGateLogits, routerWeights, routerBias, expertW1, expertB1, expertW2, expertB2, positionBias, adamMInput, adamVInput, adamMInputBias, adamVInputBias, adamMOutput, adamVOutput, adamMOutputBias, adamVOutputBias, adamMQ, adamVQ, adamMK, adamVK, adamMV, adamVV, adamMBQ, adamVBQ, adamMBK, adamVBK, adamMBV, adamVBV, adamMAttention, adamVAttention, adamMBAttention, adamVBAttention, adamMGates, adamVGates, adamMRouter, adamVRouter, adamMBRouter, adamVBRouter, adamMExpertW1, adamVExpertW1, adamMExpertB1, adamVExpertB1, adamMExpertW2, adamVExpertW2, adamMExpertB2, adamVExpertB2, adamMPosition, adamVPosition, shortMemory, longMemory, longMemoryImportance, longMemoryAge, longMemoryUsage, longMemorySurprise, trajectoryMemory, trajectoryCount, trajectoryVelocityContext, trajectoryAccelerationContext, trajectoryRecall, trajectoryNovelty, trajectoryWriteScore, trajectoryLoss, trajectoryIndices, trajectoryWeights, lastObservedVelocity, generationHistory, generationDiversity, generationNovelty, generationMinDistance, generationMeanDistance, generationPressure, localDiversity, localContractionPressure, localContractionCorrection, replayInputs, replayTargets, replayImportance, memoryCount, replayCount, memoryContext, memoryWeights, memorySimilarity, protectionScalar, lastInput, lastPrediction, lastDelta, lastTarget, attentionProfile, headActivity, headFeatureUsage, expertUsage, loss, outputLoss, deltaLoss, directionLoss, expertBalanceLoss, surprise, surpriseEMA, errorEMA, memoryRecall, memoryNovelty, memoryWriteScore, replayLoss, interferenceScore, entropy, learningRateCurrent, learnedEvents, adamStep, totalEvents, initialized, preHidden, eventHistory, generationDriftState, autoTuneEnabled, autoTuneInterval, autoTuneCounter, autoTuneStrength, autoTuneSmoothing, autoTuneTargetNovelty, autoTuneTargetDiversity, autoTuneNoveltyEMA, autoTuneDiversityEMA, autoTuneLastError, autoTuneAdjustmentCount, autoTuneExplorationMin, autoTuneExplorationMax, autoTuneNoiseMin, autoTuneNoiseMax, autoTuneTemperatureMin, autoTuneTemperatureMax, metaLearnEnabled, metaLearnInterval, metaLearnCounter, metaLearnStrength, metaLearnSmoothing, metaLearnTargetError, metaLearnTargetSurprise, metaLearnTargetRecall, metaLearnTargetInterference, metaLearnErrorEMA, metaLearnSurpriseEMA, metaLearnRecallEMA, metaLearnInterferenceEMA, metaLearnPlasticityPressure, metaLearnStabilityPressure, metaLearnAdjustmentCount, metaLearnLearningRateMin, metaLearnLearningRateMax, metaLearnReplayRateMin, metaLearnReplayRateMax, metaLearnProtectionMin, metaLearnProtectionMax, metaLearnRetrievalGainMin, metaLearnRetrievalGainMax, unifiedCurrentRuntime, unifiedPendingRuntime, unifiedTrainingVersion, unifiedPublishedVersion, unifiedSnapshotPending, unifiedRuntimeRole, unifiedLearningEnabled, unifiedGenerationEnabled, unifiedParameterMorphs;

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
		// MEMOIRE LONGUE
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
		// ADAPTATION
        adaptationFastRate=1.35,
        adaptationSlowRate=0.40,
        predictionLossWeight=0.68,
        deltaLossWeight=0.12,
        directionLossWeight=0.20,
        replayPriorityMix=0.90,
        replayUniformMix=0.10,
		// EXPERTS ET TETES
        expertBalanceStrength=0.01,
        headSpecializationThreshold=0.85,
        headSpecializationStrength=0.003,
        interferenceTestSteps=4,
		// MEMOIRE DE TRAJECTOIRE
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
		// DIVERSITE GENERATIVE
        diversityHistorySize=10,
        diversityRadius=0.015,
        diversityRepulsionGain=0.0016,
        diversityNoiseGain=0.00035,
        diversityMaxCorrection=0.020,
        diversityAdaptiveGain=0.65,
		// CONTRACTION LOCALE
        localDiversityWindow=12,
        localDiversityFloor=0.028,
        localDiversityGain=0.055,
        localDiversityMaxCorrection=0.008,
		// FENETRE DE GENERATION
        generationWindowSize=8,
        torusMask=nil|
        ^super.new.init(
            inputSize,
            outputSize,
            windowSize,
            longMemorySize,
            hiddenSize,
            numHeads,
            headSize,
            numExperts,
            learningRate,
            beta1,
            beta2,
            epsilon,
            temperature,
            residualScale,
            expertScale,
            deltaScale,
            surpriseThreshold,
            surpriseGain,
            gateLearningRate,
            gradientClip,
            memoryWriteThreshold,
            memoryRetrievalGain,
            memoryRetrievalTemperature,
            memoryDecay,
            memoryConsolidationRate,
            memoryUsageDecay,
            replayRate,
            replayBatchSize,
            memoryRecallSize,
            protectionStrength,
            adaptationFastRate,
            adaptationSlowRate,
            predictionLossWeight,
            deltaLossWeight,
            directionLossWeight,
            replayPriorityMix,
            replayUniformMix,
            expertBalanceStrength,
            headSpecializationThreshold,
            headSpecializationStrength,
            interferenceTestSteps,
            trajectoryMemorySize,
            trajectoryRecallSize,
            trajectoryRetrievalGain,
            trajectoryVelocityGain,
            trajectoryAccelerationGain,
            trajectoryVelocityClip,
            trajectoryAccelerationClip,
            trajectoryDecay,
            trajectoryUsageDecay,
            trajectoryNoveltyWeight,
            trajectoryInputWeight,
            trajectoryVelocityWeight,
            trajectoryExplorationGain,
            diversityHistorySize,
            diversityRadius,
            diversityRepulsionGain,
            diversityNoiseGain,
            diversityMaxCorrection,
            diversityAdaptiveGain,
            localDiversityWindow,
            localDiversityFloor,
            localDiversityGain,
            localDiversityMaxCorrection,
            generationWindowSize,
            torusMask
        );
    }

    init { |
        inInputSize,
        inOutputSize,
        inWindowSize,
        inLongMemorySize,
        inHiddenSize,
        inNumHeads,
        inHeadSize,
        inNumExperts,
        inLearningRate,
        inBeta1,
        inBeta2,
        inEpsilon,
        inTemperature,
        inResidualScale,
        inExpertScale,
        inDeltaScale,
        inSurpriseThreshold,
        inSurpriseGain,
        inGateLearningRate,
        inGradientClip,
        inMemoryWriteThreshold,
        inMemoryRetrievalGain,
        inMemoryRetrievalTemperature,
        inMemoryDecay,
        inMemoryConsolidationRate,
        inMemoryUsageDecay,
        inReplayRate,
        inReplayBatchSize,
        inMemoryRecallSize,
        inProtectionStrength,
        inAdaptationFastRate,
        inAdaptationSlowRate,
        inPredictionLossWeight,
        inDeltaLossWeight,
        inDirectionLossWeight,
        inReplayPriorityMix,
        inReplayUniformMix,
        inExpertBalanceStrength,
        inHeadSpecializationThreshold,
        inHeadSpecializationStrength,
        inInterferenceTestSteps,
        inTrajectoryMemorySize,
        inTrajectoryRecallSize,
        inTrajectoryRetrievalGain,
        inTrajectoryVelocityGain,
        inTrajectoryAccelerationGain,
        inTrajectoryVelocityClip,
        inTrajectoryAccelerationClip,
        inTrajectoryDecay,
        inTrajectoryUsageDecay,
        inTrajectoryNoveltyWeight,
        inTrajectoryInputWeight,
        inTrajectoryVelocityWeight,
        inTrajectoryExplorationGain,
        inDiversityHistorySize,
        inDiversityRadius,
        inDiversityRepulsionGain,
        inDiversityNoiseGain,
        inDiversityMaxCorrection,
        inDiversityAdaptiveGain,
        inLocalDiversityWindow,
        inLocalDiversityFloor,
        inLocalDiversityGain,
        inLocalDiversityMaxCorrection,
        inGenerationWindowSize,
        inTorusMask|
        inputSize = inInputSize.asInteger.max(1);
        outputSize = inOutputSize.asInteger.max(1);
        if(outputSize > inputSize, {
            Error(
                "HPtransformerRT: outputSize must be less than or equal to inputSize "
                ++ "because outputs are represented as deltas of the first input dimensions."
            ).throw;
        });
        windowSize = inWindowSize;
        longMemorySize = inLongMemorySize;
        hiddenSize = inHiddenSize;
        numHeads = inNumHeads;
        headSize = inHeadSize;
        numExperts = inNumExperts;
        learningRate = inLearningRate;
        beta1 = inBeta1;
        beta2 = inBeta2;
        epsilon = inEpsilon;
        temperature = inTemperature;
        attentionTemperature = inTemperature;
        routerTemperature = inTemperature;
        trajectoryRetrievalTemperature = 0.30;
        residualScale = inResidualScale;
        expertScale = inExpertScale;
        deltaScale = inDeltaScale;
        surpriseThreshold = inSurpriseThreshold;
        surpriseGain = inSurpriseGain;
        gateLearningRate = inGateLearningRate;
        gradientClip = inGradientClip;
        memoryWriteThreshold = inMemoryWriteThreshold;
        memoryRetrievalGain = inMemoryRetrievalGain;
        memoryRetrievalTemperature = inMemoryRetrievalTemperature;
        memoryDecay = inMemoryDecay;
        memoryConsolidationRate = inMemoryConsolidationRate;
        memoryUsageDecay = inMemoryUsageDecay;
        replayRate = inReplayRate;
        replayBatchSize = inReplayBatchSize;
        memoryRecallSize = inMemoryRecallSize;
        protectionStrength = inProtectionStrength;
        adaptationFastRate = inAdaptationFastRate;
        adaptationSlowRate = inAdaptationSlowRate;
        predictionLossWeight = inPredictionLossWeight;
        deltaLossWeight = inDeltaLossWeight;
        directionLossWeight = inDirectionLossWeight;
        replayPriorityMix = inReplayPriorityMix;
        replayUniformMix = inReplayUniformMix;
        expertBalanceStrength = inExpertBalanceStrength;
        headSpecializationThreshold = inHeadSpecializationThreshold;
        headSpecializationStrength = inHeadSpecializationStrength;
        interferenceTestSteps = inInterferenceTestSteps;
        trajectoryMemorySize = inTrajectoryMemorySize;
        trajectoryRecallSize = inTrajectoryRecallSize;
        trajectoryRetrievalGain = inTrajectoryRetrievalGain;
        trajectoryVelocityGain = inTrajectoryVelocityGain;
        trajectoryAccelerationGain = inTrajectoryAccelerationGain;
        trajectoryVelocityClip = inTrajectoryVelocityClip;
        trajectoryAccelerationClip = inTrajectoryAccelerationClip;
        trajectoryDecay = inTrajectoryDecay;
        trajectoryUsageDecay = inTrajectoryUsageDecay;
        trajectoryNoveltyWeight = inTrajectoryNoveltyWeight;
        trajectoryInputWeight = inTrajectoryInputWeight;
        trajectoryVelocityWeight = inTrajectoryVelocityWeight;
        trajectoryExplorationGain = inTrajectoryExplorationGain;
        diversityHistorySize = inDiversityHistorySize;
        diversityRadius = inDiversityRadius;
        diversityRepulsionGain = inDiversityRepulsionGain;
        diversityNoiseGain = inDiversityNoiseGain;
        diversityMaxCorrection = inDiversityMaxCorrection;
        diversityAdaptiveGain = inDiversityAdaptiveGain;
        localDiversityWindow = inLocalDiversityWindow;
        localDiversityFloor = inLocalDiversityFloor;
        localDiversityGain = inLocalDiversityGain;
        localDiversityMaxCorrection = inLocalDiversityMaxCorrection;
        generationWindowSize = inGenerationWindowSize;
        torusMask = this.normalizeTorusMask(inTorusMask);
        parameterCount = this.parameterCounter;
        this.resetAll;
        initialized = true;
        unifiedCurrentRuntime = nil;
        unifiedPendingRuntime = nil;
        unifiedTrainingVersion = 0;
        unifiedPublishedVersion = 0;
        unifiedSnapshotPending = false;
        unifiedRuntimeRole = false;
        unifiedLearningEnabled = true;
        unifiedGenerationEnabled = true;
        unifiedParameterMorphs = IdentityDictionary.new;
        ^this;
    }

    clean { |x| var y; y=x; if(y.isArray,{if(y.size>0,{y=y[0]},{y=0.0})}); while({y.isArray},{if(y.size>0,{y=y[0]},{y=0.0})}); if(y.isNumber.not,{y=0.0},{y=y.asFloat}); if(y.isNaN,{y=0.0}); if(y.abs>1e10,{if(y>0.0,{y=10.0},{y=(-10.0)})}); ^y.clip(-10.0,10.0); }
    sigmoid { |x| var y; y=this.clean(x); if(y>12.0,{^0.999993855}); if(y<(-12.0),{^0.000006144}); ^1.0/(1.0+y.neg.exp); }
    sigmoidDerivative { |y| ^y*(1.0-y); }
    tanhSafe { |x| ^this.clean(x).tanh.clip(-1.0,1.0); }
    tanhDerivative { |y| ^1.0-(y*y); }
    dot { |a,b| var r; r=0.0; a.size.do({|i| r=r+(a[i]*b[i])}); ^r; }
    matrixVector { |m,v| ^Array.fill(m.size,{|r| this.dot(m[r],v)}); }
    addVector { |a,b| ^Array.fill(a.size,{|i| a[i]+b[i]}); }
    scaleVector { |a,s| ^a.collect({|x| x*s}); }
    vectorMean { |v| if(v.size>0,{^v.sum/v.size},{^0.0}); }
    vectorMSE { |a,b| var r,d; r=0.0; a.size.do({|i| d=a[i]-b[i]; r=r+(d*d)}); ^r/a.size.max(1); }
    makeMatrix { |rows,cols,scale=0.05| ^Array.fill(rows,{Array.fill(cols,{1.0.rand2*scale})}); }
    makeVector { |size,value=0.0| ^Array.fill(size,{value}); }
    randomWeight { |scale=0.05| ^1.0.rand2*scale; }
    copyVector { |v| ^v.collect({|x| x}); }
    copyMatrix { |m| ^m.collect({|row| row.collect({|x| x})}); }
    zeroVector { |size| ^Array.fill(size,{0.0}); }
    zeroMatrix { |rows,cols| ^Array.fill(rows,{Array.fill(cols,{0.0})}); }
    clampParameter { |x| ^x.clip(-4.0,4.0); }
    gradientNormVector { |g| ^(g.collect({|x| x*x}).sum.max(0.0)).sqrt; }
    gradientNormMatrix { |g| var n; n=0.0; g.do({|row| n=n+row.collect({|x| x*x}).sum}); ^n.max(0.0).sqrt; }
    torusDelta { |a,b| var d; d=(a-b).abs; ^d.min(1.0-d); }
    torusDifference { |a,b| var d; d=b-a; if(d>0.5,{d=d-1.0}); if(d<(-0.5),{d=d+1.0}); ^d; }
    wrap01 { |x| var y; y=x%1.0; if(y<0.0,{y=y+1.0}); ^y; }
    normalizeTorusMask { |mask| if(mask.isNil,{^Array.fill(outputSize,{true})}); ^Array.fill(outputSize,{|i| if(i<mask.size,{mask[i]==true},{false})}); }
    isTorusDimension { |i| ^(i<torusMask.size and:{torusMask[i]==true}); }
    maskedDifference { |a,b,i| if(this.isTorusDimension(i),{^this.torusDifference(a,b)},{^b-a}); }
    maskedDistance { |a,b,i| if(this.isTorusDimension(i),{^this.torusDelta(a,b)},{^(a-b).abs}); }
    maskedWrap { |value,i| if(this.isTorusDimension(i),{^this.wrap01(value)},{^value.clip(0.0,1.0)}); }
    maskedMSE { |a,b| var r,size,d; r=0.0; size=a.size.min(b.size).min(torusMask.size); size.do({|i| d=this.maskedDistance(a[i],b[i],i); r=r+(d*d)}); ^r/size.max(1); }
    getTorusMask { ^torusMask.copy; }
    edgePush { |value,index,loEdge=0.05,hiEdge=0.95,gainEdge=0.08| var result; result=value; if(this.isTorusDimension(index).not,{if(result<loEdge,{result=result+((loEdge-result)*gainEdge)}); if(result>hiEdge,{result=result-((result-hiEdge)*gainEdge)}); result=result.clip(0.0,1.0)}); ^result; }
    parameterCounter { var n; n=0; n=n+(hiddenSize*inputSize)+hiddenSize; n=n+(outputSize*hiddenSize)+outputSize; numHeads.do({n=n+(3*headSize*inputSize)+(3*headSize)+inputSize+windowSize}); n=n+(hiddenSize*(numHeads*headSize))+hiddenSize; n=n+(numExperts*hiddenSize)+numExperts; numExperts.do({n=n+(hiddenSize*hiddenSize)+hiddenSize+(hiddenSize*hiddenSize)+hiddenSize}); ^n; }

    cleanSizedVector { |input, size|
        var x;
        x = input;
        while({
            x.isArray
            and: {
                x.size == 1
                and: { x[0].isArray }
            }
        }, {
            x = x[0];
        });
        ^Array.fill(size.asInteger.max(1), { |i|
            if(x.isArray and: { i < x.size }, {
                this.clean(x[i]).clip(0.0, 1.0);
            }, {
                0.0;
            });
        });
    }

    cleanInputVector { |input|
        ^this.cleanSizedVector(input, inputSize);
    }

    cleanOutputVector { |output|
        ^this.cleanSizedVector(output, outputSize);
    }

    // Compatibility alias: historical calls expect an input-sized vector.
    cleanVector { |input|
        ^this.cleanInputVector(input);
    }

    expandOutputToInput { |output, previousInput|
        var result;
        var cleanOutput;
        result = this.cleanInputVector(previousInput);
        cleanOutput = this.cleanOutputVector(output);
        outputSize.do({ |i|
            result[i] = cleanOutput[i];
        });
        ^result;
    }

    expandOutputDelta { |delta|
        ^Array.fill(inputSize, { |i|
            if(i < outputSize, {
                this.clean(delta[i]);
            }, {
                0.0;
            });
        });
    }

    outputToNextInput { |output, previousInput|
        ^this.expandOutputToInput(output, previousInput);
    }

    softmaxWithTemperature { |values, requestedTemperature|
        var maximum, exps, total, safeTemperature;
        safeTemperature = requestedTemperature.asFloat.clip(0.01, 20.0);
        maximum = values.maxItem;
        exps = values.collect({ |x| ((x - maximum) / safeTemperature).exp; });
        total = exps.sum.max(0.000000001);
        ^exps.collect({ |x| x / total });
    }
    softmax { |values|
        ^this.softmaxWithTemperature(values, temperature);
    }
    vectorDirectionLoss { |a, b|
        ^{
            		var result;
            		var sa;
            		var sb;
            		var d;
            		result = 0.0;
            		a.size.do({ |i|
            			sa = (12.0 * a[i]).tanh;
            			sb = (12.0 * b[i]).tanh;
            			d = sa - sb;
            			result = result + (d * d);
            		});
            		result / a.size.max(1);

        }.value;
    }

    adamVector { |parameters, gradient, m, v, baseRate|
        ^{
            		var bc1;
            		var effectiveRate;
            		var bc2;
            		var norm;
            		var gradientScale;
            		effectiveRate = if(baseRate.isNil, {
            			learningRateCurrent;
            		}, {
            			baseRate * (learningRateCurrent / learningRate.max(0.000000001));
            		});
            		bc1 =
            		1.0
            		-
            		beta1.pow(
            			adamStep
            		);
            		bc2 =
            		1.0
            		-
            		beta2.pow(
            			adamStep
            		);
            		norm = this.gradientNormVector(gradient);
            		gradientScale = if(norm > gradientClip, { gradientClip / norm }, { 1.0 });
            		parameters.size.do({ |i|
            			var g;
            			var mh;
            			var vh;
            			var denom;
            			g = gradient[i] * gradientScale;
            			mh =
            			(
            				beta1
            				*
            				m[i]
            			)
            			+
            			(
            				(
            					1.0
            					-
            					beta1
            				)
            				*
            				g
            			);
            			vh =
            			(
            				beta2
            				*
            				v[i]
            			)
            			+
            			(
            				(
            					1.0
            					-
            					beta2
            				)
            				*
            				g
            				*
            				g
            			);
            			m[i] = mh;
            			v[i] = vh;
            			denom =
            			(
            				vh
            				/
            				bc2
            			).sqrt
            			+
            			epsilon;
            			parameters[i] =
            			parameters[i]
            			-
            			(
            				effectiveRate
            				*
            				(
            					mh
            					/
            					bc1
            				)
            				/
            				denom
            			);
            			parameters[i] =
            			this.clampParameter(
            				parameters[i]
            			);
            		});

        }.value;
    }

    adamMatrix { |parameters, gradient, m, v|
        ^{
            		var bc1;
            		var bc2;
            		var norm;
            		var gradientScale;
            		bc1 =
            		1.0
            		-
            		beta1.pow(
            			adamStep
            		);
            		bc2 =
            		1.0
            		-
            		beta2.pow(
            			adamStep
            		);
            		norm = this.gradientNormMatrix(gradient);
            		gradientScale = if(norm > gradientClip, { gradientClip / norm }, { 1.0 });
            		parameters.size.do({ |r|
            			parameters[r].size.do({ |c|
            				var g;
            				var mh;
            				var vh;
            				var denom;
            				g = gradient[r][c] * gradientScale;
            				mh =
            				(
            					beta1
            					*
            					m[r][c]
            				)
            				+
            				(
            					(
            						1.0
            						-
            						beta1
            					)
            					*
            					g
            				);
            				vh =
            				(
            					beta2
            					*
            					v[r][c]
            				)
            				+
            				(
            					(
            						1.0
            						-
            						beta2
            					)
            					*
            					g
            					*
            					g
            				);
            				m[r][c] = mh;
            				v[r][c] = vh;
            				denom =
            				(
            					vh
            					/
            					bc2
            				).sqrt
            				+
            				epsilon;
            				parameters[r][c] =
            				parameters[r][c]
            				-
            				(
            					learningRateCurrent
            					*
            					(
            						mh
            						/
            						bc1
            					)
            					/
            					denom
            				);
            				parameters[r][c] =
            				this.clampParameter(
            					parameters[r][c]
            				);
            			});
            		});

        }.value;
    }

    getFeatureGate { |h, i|
        ^{
            		this.sigmoid(
            			featureGateLogits[h][i]
            		);

        }.value;
    }

    vectorSimilarity { |a, b|
        ^{
            		var ab;
            		var aa;
            		var bb;
            		var denom;
            		ab =
            		this.dot(
            			a,
            			b
            		);
            		aa =
            		this.dot(
            			a,
            			a
            		).sqrt;
            		bb =
            		this.dot(
            			b,
            			b
            		).sqrt;
            		denom =
            		(
            			aa
            			*
            			bb
            		).max(
            			0.000000001
            		);
            		(
            			ab
            			/
            			denom
            		).clip(
            			-1.0,
            			1.0
            		);

        }.value;
    }

    memoryDistance { |a, b|
        ^{
            		var sum;
            		sum = 0.0;
            		a.size.do({ |i|
            			var d;
            			d = this.maskedDistance(a[i], b[i], i);
            			sum = sum + (d * d);
            		});
            		(
            			sum
            			/
            			a.size.max(1)
            		).sqrt.clip(
            			0.0,
            			1.0
            		);

        }.value;
    }

    velocityDistance { |a, b|
        var sum, size, d;
        size = a.size.min(b.size).max(1);
        sum = 0.0;
        size.do({ |i| d = this.clean(a[i]) - this.clean(b[i]); sum = sum + (d * d); });
        ^(sum / size).sqrt.clip(0.0, 2.0);
    }
    calculateMemoryNovelty { |x|
        ^{
            		var novelty;
            		if(
            			memoryCount == 0,
            			{
            				novelty = 1.0;
            			},
            			{
            				novelty = 1.0;
            				memoryCount.do({ |i|
            					novelty =
            					novelty.min(
            						this.memoryDistance(
            							x,
            							longMemory[i]
            						)
            					);
            				});
            			}
            		);
            		novelty.clip(
            			0.0,
            			1.0
            		);

        }.value;
    }

    retrieveMemory { |x|
        ^{
            		var scores;
            		var total;
            		var context;
            		var selected;
            		var n;
            		var bestIndex;
            		var bestValue;
            		var used;
            		var selectedTotal;
            		var maxScore;
            		var sharpScores;
            		var sharpTotal;
            		scores = Array.fill(memoryCount.max(1), 0.0);
            		if(memoryCount == 0, {
            			context = this.zeroVector(inputSize);
            			memoryRecall = 0.0;
            			(context: context, weights: scores, indices: Array.new, recall: 0.0);
            		}, {
            			memoryCount.do({ |i|
            				var similarity;
            				var distance;
            				var score;
            				similarity = this.vectorSimilarity(x, longMemory[i]);
            				distance = this.memoryDistance(x, longMemory[i]);
            				score =
            				(0.55 * (((similarity + 1.0) * 0.5)))
            				+ (0.25 * (1.0 - distance))
            				+ (0.20 * longMemoryImportance[i]);
            				score = score * longMemoryUsage[i].clip(0.25, 1.0);
            				scores[i] = score.clip(0.0, 1.0);
            			});
            			// Learning+ : retrieval sharpness.
            			// The most relevant memories receive most of the context mass.
            			maxScore = scores.maxItem;
            			sharpScores = scores.collect({ |v|
            				((v - maxScore) / memoryRetrievalTemperature.max(0.05)).exp;
            			});
            			sharpTotal = sharpScores.sum.max(0.000000001);
            			scores = sharpScores.collect({ |v| v / sharpTotal });
            			context = this.zeroVector(inputSize);
            			selected = Array.new;
            			used = Array.fill(memoryCount, { false });
            			selectedTotal = 0.0;
            			n = memoryRecallSize.min(memoryCount);
            			n.do({
            				bestIndex = 0;
            				bestValue = -1.0;
            				memoryCount.do({ |i|
            					if(used[i].not and: { scores[i] > bestValue }, {
            						bestValue = scores[i];
            						bestIndex = i;
            					});
            				});
            				used[bestIndex] = true;
            				selected.add(bestIndex);
            				selectedTotal = selectedTotal + scores[bestIndex];
            				longMemoryUsage[bestIndex] =
            				((longMemoryUsage[bestIndex] * 0.95) + 0.05).clip(0.0, 1.0);
            			});
            			// Normalize only the selected memories before constructing context.
            			selectedTotal = selectedTotal.max(0.000000001);
            			selected.do({ |index|
            				var w;
            				w = scores[index] / selectedTotal;
            				inputSize.do({ |d|
            					context[d] = context[d] + (w * longMemory[index][d]);
            				});
            			});
            			memoryWeights = scores.copy;
            			memorySimilarity = selected.collect({ |index|
            				this.vectorSimilarity(x, longMemory[index]);
            			});
            			memoryContext = context.clip(0.0, 1.0);
            			memoryRecall = selectedTotal.clip(0.0, 1.0);
            			(context: memoryContext.copy, weights: scores, indices: selected.asArray, recall: memoryRecall);
            		});

        }.value;
    }

    writeLongMemory { |input, target, surpriseValue|
        ^{
            		var novelty;
            		var importance;
            		var index;
            		var shouldWrite;
            		novelty =
            		this.calculateMemoryNovelty(
            			input
            		);
            		importance =
            		(
            			0.55
            			*
            			surpriseValue.clip(
            				0.0,
            				1.0
            			)
            		)
            		+
            		(
            			0.45
            			*
            			novelty
            		);
            		memoryWriteScore =
            		importance;
            		shouldWrite =
            		(
            			importance
            			>
            			memoryWriteThreshold
            		);
            		if(
            			memoryCount == 0,
            			{
            				shouldWrite = true;
            			}
            		);
            		if(
            			shouldWrite,
            			{
            				if(
            					memoryCount
            					<
            					longMemorySize,
            					{
            						index =
            						memoryCount;
            						memoryCount =
            						memoryCount
            						+
            						1;
            					},
            					{
            						index =
            						longMemoryImportance.indexOf(
            							longMemoryImportance.minItem
            						);
            					}
            				);
            				longMemory[index] =
            				this.copyVector(
            					input
            				);
            				longMemoryImportance[index] =
            				importance.clip(
            					0.0,
            					1.0
            				);
            				longMemoryAge[index] =
            				0;
            				longMemoryUsage[index] =
            				1.0;
            				longMemorySurprise[index] =
            				surpriseValue.clip(
            					0.0,
            					1.0
            				);
            				// Replay is written centrally for every real event.
            				// Long-memory selection remains selective.
            			}
            		);

        }.value;
    }

    writeReplay { |input, target, importance|
        ^{
            		var index;
            		if(
            			replayCount
            			<
            			longMemorySize,
            			{
            				index =
            				replayCount;
            				replayCount =
            				replayCount
            				+
            				1;
            			},
            			{
            				index =
            				replayImportance.indexOf(
            					replayImportance.minItem
            				);
            			}
            		);
            		replayInputs[index] =
            		this.copyVector(
            			input
            		);
            		replayTargets[index] =
            		this.copyVector(
            			target
            		);
            		replayImportance[index] =
            		importance.clip(
            			0.0,
            			1.0
            		);

        }.value;
    }

    consolidateMemory {
        ^{
            		memoryCount.do({ |i|
            			longMemoryAge[i] =
            			longMemoryAge[i]
            			+
            			1;
            			longMemoryImportance[i] =
            			(
            				longMemoryImportance[i]
            				*
            				memoryDecay
            			)
            			+
            			(
            				longMemorySurprise[i]
            				*
            				(
            					1.0
            					-
            					memoryDecay
            				)
            			);
            			longMemoryUsage[i] =
            			longMemoryUsage[i]
            			*
            			memoryUsageDecay;
            			if(
            				longMemoryUsage[i]
            				<
            				0.05,
            				{
            					longMemoryImportance[i] =
            					longMemoryImportance[i]
            					*
            					(
            						1.0
            						-
            						memoryConsolidationRate
            					);
            				}
            			);
            		});

        }.value;
    }

    retrieveTrajectory { |x, velocity|
        ^{
            		var scores;
            		var used;
            		var selected;
            		var contextVelocity;
            		var contextAcceleration;
            		var selectedTotal;
            		var n;
            		var bestIndex;
            		var bestValue;
            		var inputSimilarity;
            		var velocityDistance;
            		var score;
            		var maxScore;
            		var sharpScores;
            		var sharpTotal;
            		var trajectoryWeightTotal;
            		var normalizedInputWeight;
            		var normalizedVelocityWeight;
            		trajectoryNovelty = 1.0;
            		if(trajectoryCount == 0, {
            			trajectoryVelocityContext = this.zeroVector(inputSize);
            			trajectoryAccelerationContext = this.zeroVector(inputSize);
            			trajectoryRecall = 0.0;
            			trajectoryIndices = Array.new;
            			trajectoryWeights = Array.new;
            			[
            				trajectoryVelocityContext.copy,
            				trajectoryAccelerationContext.copy,
            				0.0,
            				1.0,
            				Array.new,
            				Array.new
            			];
            		}, {
            		// Normalize retrieval weights so their sum cannot rescale the score.
            		trajectoryWeightTotal = (trajectoryInputWeight + trajectoryVelocityWeight).max(0.000001);
            		normalizedInputWeight = trajectoryInputWeight / trajectoryWeightTotal;
            		normalizedVelocityWeight = trajectoryVelocityWeight / trajectoryWeightTotal;
            			scores = Array.fill(trajectoryCount, { 0.0 });
            			trajectoryCount.do({ |i|
            				inputSimilarity =
            				this.vectorSimilarity(
            					x,
            					trajectoryMemory[i][0]
            				);
            				velocityDistance =
            				this.velocityDistance(velocity, trajectoryMemory[i][1]);
            				score =
            				(normalizedInputWeight * ((inputSimilarity + 1.0) * 0.5))
            				+ (normalizedVelocityWeight * (1.0 - velocityDistance));
            				score =
            				score
            				* trajectoryMemory[i][3].clip(0.10, 1.0);
            				scores[i] = score.clip(0.0, 1.0);
            				trajectoryNovelty =
            				trajectoryNovelty.min(
            					((1.0 - ((inputSimilarity + 1.0) * 0.5)) * 0.60)
            					+ (velocityDistance * 0.40)
            				);
            			});
            			maxScore = scores.maxItem;
            			sharpScores = scores.collect({ |v|
            				((v - maxScore) / trajectoryRetrievalTemperature.max(0.05)).exp;
            			});
            			sharpTotal = sharpScores.sum.max(0.000000001);
            			scores = sharpScores.collect({ |v| v / sharpTotal });
            			contextVelocity = this.zeroVector(inputSize);
            			contextAcceleration = this.zeroVector(inputSize);
            			selected = Array.new;
            			used = Array.fill(trajectoryCount, { false });
            			selectedTotal = 0.0;
            			n = trajectoryRecallSize.min(trajectoryCount);
            			n.do({
            				bestIndex = 0;
            				bestValue = -1.0;
            				trajectoryCount.do({ |i|
            					if(used[i].not and: { scores[i] > bestValue }, {
            						bestValue = scores[i];
            						bestIndex = i;
            					});
            				});
            				used[bestIndex] = true;
            				selected.add(bestIndex);
            				selectedTotal = selectedTotal + scores[bestIndex];
            			});
            			selectedTotal = selectedTotal.max(0.000000001);
            			selected.do({ |index|
            				var w;
            				w = scores[index] / selectedTotal;
            				inputSize.do({ |d|
            					contextVelocity[d] =
            					contextVelocity[d]
            					+ (w * trajectoryMemory[index][1][d]);
            					contextAcceleration[d] =
            					contextAcceleration[d]
            					+ (w * trajectoryMemory[index][2][d]);
            				});
            				trajectoryMemory[index][5] =
            				((trajectoryMemory[index][5] * 0.95) + 0.05).clip(0.0, 1.0);
            			});
            			trajectoryVelocityContext = contextVelocity;
            			trajectoryAccelerationContext = contextAcceleration;
            			trajectoryRecall = selectedTotal.clip(0.0, 1.0);
            			trajectoryIndices = selected.asArray;
            			trajectoryWeights = scores.copy;
            			[
            				contextVelocity.copy,
            				contextAcceleration.copy,
            				trajectoryRecall,
            				trajectoryNovelty.clip(0.0, 1.0),
            				trajectoryIndices.copy,
            				trajectoryWeights.copy
            			];
            		});

        }.value;
    }

    writeTrajectoryMemory { |input, target, surpriseValue|
        ^{
            		var x;
            		var y;
            		var velocity;
            		var acceleration;
            		var novelty;
            		var importance;
            		var record;
            		var index;
            		var importanceArray;
            		var bestImportance;
            		x = this.cleanInputVector(input);
            		y = this.expandOutputToInput(target, x);
            		velocity = Array.fill(
            			inputSize,
            			{ |i|
            				this.maskedDifference(x[i], y[i], i);
            			}
            		);
            		acceleration =
            		if(lastObservedVelocity.isArray, {
            			Array.fill(inputSize, { |i| velocity[i] - lastObservedVelocity[i] });
            		}, {
            			this.zeroVector(inputSize);
            		});
            		novelty = trajectoryNovelty.clip(0.0, 1.0);
            		importance =
            		(((1.0 - trajectoryNoveltyWeight) * surpriseValue.clip(0.0, 1.0))
            			+ (trajectoryNoveltyWeight * novelty)).clip(0.0, 1.0);
            		record = [
            			x.copy,
            			velocity.copy,
            			acceleration.copy,
            			importance.max(0.10),
            			0,
            			1.0
            		];
            		if(trajectoryCount < trajectoryMemorySize, {
            			index = trajectoryCount;
            			trajectoryMemory.add(record);
            			trajectoryCount = trajectoryCount + 1;
            		}, {
            			importanceArray = trajectoryMemory.collect({ |item| item[3] });
            			bestImportance = importanceArray.minItem;
            			index = importanceArray.indexOf(bestImportance);
            			trajectoryMemory[index] = record;
            		});
            		trajectoryWriteScore = importance;

        }.value;
    }

    consolidateTrajectoryMemory {
        ^{
            		trajectoryMemory.do({ |item|
            			item[4] = item[4] + 1;
            			item[3] =
            			(item[3] * trajectoryDecay).clip(0.05, 1.0);
            			item[5] = item[5] * trajectoryUsageDecay;
            		});

        }.value;
    }

    trajectoryMemoryTest { |input|
        ^{
            		var x;
            		var velocity;
            		var r;
            		x = this.cleanVector(input);
            		velocity =
            		if(lastObservedVelocity.isArray, {
            			lastObservedVelocity.copy;
            		}, {
            			this.zeroVector(inputSize);
            		});
            		r = this.retrieveTrajectory(x, velocity);
            		(
            			trajectoryCount: trajectoryCount,
            			recall: r[2],
            			novelty: r[3],
            			indices: r[4].copy,
            			weights: r[5].copy,
            			velocityContext: r[0].copy,
            			accelerationContext: r[1].copy
            		);

        }.value;
    }

    buildForward { |input, requestedWindowSize|
        ^{
            		var x;
            		var h0;
            		var h;
            		var retrieved;
            		var memory;
            		var maxMemory;
            		var headContexts;
            		var headWeights;
            		var headQueries;
            		var headKeys;
            		var headValues;
            		var headScores;
            		var headFeatureVectors;
            		var attentionCombined;
            		var attentionLinear;
            		var expertHidden;
            		var expertOutputs;
            		var routerLogits;
            		var router;
            		var expertCombined;
            		var residualPre;
            		var residual;
            		var deltaRaw;
            		var delta;
            		var prediction;
            		var recallCount;
            		var currentVelocity;
            		var trajectoryResult;
            		var trajectoryVelocity;
            		var trajectoryAcceleration;
            		var trajectoryRecall;
            		var trajectoryNovelty;
            		var trajectoryIndices;
            		var trajectoryWeights;
            		var deltaBias;
            		var activeWindowSize;
            		x =
            		this.cleanVector(
            			input
            		);
            		activeWindowSize =
            		if(
            			requestedWindowSize.isNil,
            			{
            				windowSize;
            			},
            			{
            				requestedWindowSize.clip(
            					1,
            					windowSize
            				).asInteger;
            			}
            		);
            		// ------------------------------------------------------------
            		// DYNAMIC TRAJECTORY RETRIEVAL
            		// ------------------------------------------------------------
            		currentVelocity =
            		if(lastObservedVelocity.isArray, {
            			lastObservedVelocity.copy;
            		}, {
            			this.zeroVector(inputSize);
            		});
            		trajectoryResult =
            		this.retrieveTrajectory(
            			x,
            			currentVelocity
            		);
            		trajectoryVelocity = trajectoryResult[0].copy;
            		trajectoryAcceleration = trajectoryResult[1].copy;
            		trajectoryRecall = trajectoryResult[2];
            		trajectoryNovelty = trajectoryResult[3];
            		trajectoryIndices = trajectoryResult[4].copy;
            		trajectoryWeights = trajectoryResult[5].copy;
            		// ------------------------------------------------------------
            		// LONG MEMORY RETRIEVAL
            		// ------------------------------------------------------------
            		retrieved =
            		this.retrieveMemory(
            			x
            		);
            		memoryContext =
            		retrieved[\context];
            		recallCount =
            		retrieved[\indices].size;
            		// ------------------------------------------------------------
            		// INPUT PROJECTION
            		// ------------------------------------------------------------
            		preHidden =
            		this.addVector(
            			this.matrixVector(
            				inputProjection,
            				x
            			),
            			inputBias
            		);
            		h0 =
            		Array.fill(
            			hiddenSize,
            			{ |i|
            				var mc;
            				if(
            					i < inputSize,
            					{
            						mc =
            						memoryContext[i];
            					},
            					{
            						mc = 0.0;
            					}
            				);
            				this.tanhSafe(
            					preHidden[i]
            					+
            					(
            						memoryRetrievalGain
            						*
            						mc
            					)
            					+
            					(
            						trajectoryRetrievalGain
            						*
            						trajectoryVelocity[i % inputSize]
            					)
            					+
            					(
            						(trajectoryRetrievalGain * 0.50)
            						*
            						trajectoryAcceleration[i % inputSize]
            					)
            				);
            			}
            		);
            		h =
            		this.copyVector(
            			h0
            		);
            		// ------------------------------------------------------------
            		// TEMPORAL MEMORY
            		// ------------------------------------------------------------
            		memory =
            		List.new;
            		if(
            			shortMemory.size > activeWindowSize,
            			{
            				shortMemory.copyRange(
            					shortMemory.size - activeWindowSize,
            					shortMemory.size - 1
            				).do({
            					|item|
            					memory.add(
            						this.copyVector(item)
            					);
            				});
            			},
            			{
            				shortMemory.do({
            					|item|
            					memory.add(
            						this.copyVector(item)
            					);
            				});
            			}
            		);
            		// Ajouter les souvenirs rappel√©s √† l'attention.
            		retrieved[\indices].do({ |index|
            			memory.add(
            				this.copyVector(
            					longMemory[index]
            				)
            			);
            		});
            		if(
            			memory.size > activeWindowSize,
            			{
            				memory =
            				memory.copyRange(
            					memory.size - activeWindowSize,
            					memory.size - 1
            				);
            			}
            		);
            		maxMemory =
            		memory.size;
            		if(
            			maxMemory == 0,
            			{
            				memory.add(
            					this.copyVector(x)
            				);
            				maxMemory = 1;
            			}
            		);
            		// ------------------------------------------------------------
            		// HEADS
            		// ------------------------------------------------------------
            		headContexts =
            		Array.newClear(
            			numHeads
            		);
            		headWeights =
            		Array.newClear(
            			numHeads
            		);
            		headQueries =
            		Array.newClear(
            			numHeads
            		);
            		headKeys =
            		Array.newClear(
            			numHeads
            		);
            		headValues =
            		Array.newClear(
            			numHeads
            		);
            		headScores =
            		Array.newClear(
            			numHeads
            		);
            		headFeatureVectors =
            		Array.newClear(
            			numHeads
            		);
            		numHeads.do({ |head|
            			var gatedInput;
            			var q;
            			var scores;
            			var weights;
            			var context;
            			var keys;
            			var values;
            			gatedInput =
            			Array.fill(
            				inputSize,
            				{ |i|
            					x[i]
            					*
            					this.getFeatureGate(
            						head,
            						i
            					);
            				}
            			);
            			headFeatureVectors[head] =
            			this.copyVector(
            				gatedInput
            			);
            			q =
            			this.addVector(
            				this.matrixVector(
            					qWeights[head],
            					gatedInput
            				),
            				qBias[head]
            			);
            			keys =
            			Array.newClear(
            				maxMemory
            			);
            			values =
            			Array.newClear(
            				maxMemory
            			);
            			scores =
            			Array.newClear(
            				maxMemory
            			);
            			maxMemory.do({ |j|
            				var mem;
            				var gatedMemory;
            				var k;
            				var v;
            				mem =
            				memory[j];
            				gatedMemory =
            				Array.fill(
            					inputSize,
            					{ |i|
            						mem[i]
            						*
            						this.getFeatureGate(
            							head,
            							i
            						);
            					}
            				);
            				k =
            				this.addVector(
            					this.matrixVector(
            						kWeights[head],
            						gatedMemory
            					),
            					kBias[head]
            				);
            				v =
            				this.addVector(
            					this.matrixVector(
            						vWeights[head],
            						gatedMemory
            					),
            					vBias[head]
            				);
            				keys[j] = k;
            				values[j] = v;
            				scores[j] =
            				(
            					this.dot(
            						q,
            						k
            					)
            					/
            					headSize.sqrt
            				)
            				+
            				(
            					if(
            						j < windowSize,
            						{
            							positionBias[head][j];
            						},
            						{
            							0.0;
            						}
            					)
            				);
            			});
            			weights =
            			this.softmaxWithTemperature(scores, attentionTemperature);
            			context =
            			Array.fill(
            				headSize,
            				{ |d|
            					var s;
            					s = 0.0;
            					maxMemory.do({ |j|
            						s =
            						s
            						+
            						(
            							weights[j]
            							*
            							values[j][d]
            						);
            					});
            					s;
            				}
            			);
            			headQueries[head] = q;
            			headKeys[head] = keys;
            			headValues[head] = values;
            			headScores[head] = scores;
            			headWeights[head] = weights;
            			headContexts[head] = context;
            		});
            		// ------------------------------------------------------------
            		// CONCATENATE HEADS
            		// ------------------------------------------------------------
            		attentionCombined =
            		Array.fill(
            			numHeads
            			*
            			headSize,
            			{ |i|
            				var head;
            				var dim;
            				head =
            				i.div(
            					headSize
            				);
            				dim =
            				i
            				%
            				headSize;
            				headContexts[head][dim];
            			}
            		);
            		attentionLinear =
            		this.addVector(
            			this.matrixVector(
            				outputAttentionWeight,
            				attentionCombined
            			),
            			outputAttentionBias
            		);
            		// ------------------------------------------------------------
            		// ROUTER
            		// ------------------------------------------------------------
            		routerLogits =
            		this.addVector(
            			this.matrixVector(
            				routerWeights,
            				h0
            			),
            			routerBias
            		);
            		router =
            		this.softmaxWithTemperature(routerLogits, routerTemperature);
            		expertBalanceLoss =
            		router.collect({ |r|
            			var d;
            			d = r - (1.0 / numExperts);
            			d * d;
            		}).sum
            		/
            		numExperts.max(1);
            		// ------------------------------------------------------------
            		// EXPERTS
            		// ------------------------------------------------------------
            		expertHidden =
            		Array.newClear(
            			numExperts
            		);
            		expertOutputs =
            		Array.newClear(
            			numExperts
            		);
            		numExperts.do({ |e|
            			var eh;
            			var eo;
            			eh =
            			this.addVector(
            				this.matrixVector(
            					expertW1[e],
            					h0
            				),
            				expertB1[e]
            			);
            			eh =
            			eh.collect({ |v|
            				this.tanhSafe(v);
            			});
            			eo =
            			this.addVector(
            				this.matrixVector(
            					expertW2[e],
            					eh
            				),
            				expertB2[e]
            			);
            			expertHidden[e] =
            			eh;
            			expertOutputs[e] =
            			eo;
            		});
            		expertCombined =
            		Array.fill(
            			hiddenSize,
            			{ |d|
            				var s;
            				s = 0.0;
            				numExperts.do({ |e|
            					s =
            					s
            					+
            					(
            						router[e]
            						*
            						expertOutputs[e][d]
            					);
            				});
            				s;
            			}
            		);
            		// ------------------------------------------------------------
            		// RESIDUAL
            		// ------------------------------------------------------------
            		residualPre =
            		Array.fill(
            			hiddenSize,
            			{ |i|
            				h0[i]
            				+
            				(
            					residualScale
            					*
            					attentionLinear[i]
            				)
            				+
            				(
            					expertScale
            					*
            					expertCombined[i]
            				);
            			}
            		);
            		residual =
            		residualPre.collect({ |v|
            			this.tanhSafe(v);
            		});
            		// ------------------------------------------------------------
            		// DELTA
            		// ------------------------------------------------------------
            		deltaRaw =
            		this.addVector(
            			this.matrixVector(
            				outputProjection,
            				residual
            			),
            			outputBias
            		);
            		delta =
            		Array.fill(
            			outputSize,
            			{ |i|
            				(
            					(
            						deltaScale
            						*
            						this.tanhSafe(deltaRaw[i])
            					)
            					+
            					(
            						trajectoryVelocityGain
            						*
            						trajectoryVelocity[i].clip(
            							trajectoryVelocityClip.neg,
            							trajectoryVelocityClip
            						)
            					)
            					+
            					(
            						trajectoryAccelerationGain
            						*
            						trajectoryAcceleration[i].clip(
            							trajectoryAccelerationClip.neg,
            							trajectoryAccelerationClip
            						)
            					)
            				).clip(-0.65, 0.65);
            			}
            		);
            		prediction =
            		Array.fill(
            			outputSize,
            			{ |i|
            				this.maskedWrap(x[i] + delta[i], i);
            			}
            		);
            		(
            			input: x,
            			h0: h0,
            			residualPre: residualPre,
            			residual: residual,
            			memory: memory,
            			memorySize: maxMemory,
            			memoryContext: memoryContext.copy,
            			memoryRecallCount: recallCount,
            			memoryWeights: retrieved[\weights],
            			headQueries: headQueries,
            			headKeys: headKeys,
            			headValues: headValues,
            			headScores: headScores,
            			headWeights: headWeights,
            			headContexts: headContexts,
            			headFeatureVectors: headFeatureVectors,
            			attentionCombined: attentionCombined,
            			attentionLinear: attentionLinear,
            			router: router,
            			routerLogits: routerLogits,
            			expertHidden: expertHidden,
            			expertOutputs: expertOutputs,
            			expertCombined: expertCombined,
            			trajectoryVelocity: trajectoryVelocity.copy,
            			trajectoryAcceleration: trajectoryAcceleration.copy,
            			trajectoryRecall: trajectoryRecall,
            			trajectoryNovelty: trajectoryNovelty,
            			trajectoryIndices: trajectoryIndices.copy,
            			trajectoryWeights: trajectoryWeights.copy,
            			deltaRaw: deltaRaw,
            			delta: delta,
            			prediction: prediction
            		);

        }.value;
    }

    addToShortMemory { |x|
        ^{
            		var cleanX;
            		cleanX =
            		this.cleanVector(
            			x
            		);
            		shortMemory.add(
            			this.copyVector(
            				cleanX
            			)
            		);
            		if(
            			shortMemory.size
            			>
            			windowSize,
            			{
            				shortMemory.removeAt(0);
            			}
            		);

        }.value;
    }

    learnPairCore { |input, target, allowMemory=true|
        ^{
            		var state;
            		var x;
            		var y;
            		var prediction;
            		var targetDelta;
            		var predDelta;
            		var localLoss;
            		var localDeltaLoss;
            		var gradPrediction;
            		var gradDelta;
            		var gradDeltaRaw;
            		var gradResidual;
            		var gradResidualPre;
            		var gradAttentionLinear;
            		var gradAttentionCombined;
            		var gradExpertCombined;
            		var gradH0;
            		var gradPreHidden;
            		var gradInputProjection;
            		var gradInputBias;
            		var gradOutputProjection;
            		var gradOutputBias;
            		var gradRouterWeights;
            		var gradRouterBias;
            		var gradRouterLogits;
            		var gradExpertW1;
            		var gradExpertB1;
            		var gradExpertW2;
            		var gradExpertB2;
            		var gradFeatureGates;
            		var gradQ;
            		var gradK;
            		var gradV;
            		var gradQB;
            		var gradKB;
            		var gradVB;
            		var gradAttentionWeight;
            		var gradAttentionBias;
            		var gradPositionBias;
            		var gradRouter;
            		var gradExpertOutput;
            		var gradExpertHidden;
            		var head;
            		var expert;
            		var i;
            		var j;
            		var d;
            		var specializationPenalty;
            		var surpriseValue;
            		var rateScale;
            		var replayMode;
            		x =
            		this.cleanInputVector(
            			input
            		);
            		y =
            		this.cleanOutputVector(
            			target
            		);
            		replayMode =
            		allowMemory.not;
            		state =
            		this.buildForward(
            			x
            		);
            		prediction =
            		state[\prediction];
            		// ------------------------------------------------------------
            		// TARGET DELTA
            		// ------------------------------------------------------------
            		targetDelta =
            		Array.fill(
            			outputSize,
            			{ |index|
            				this.maskedDifference(x[index], y[index], index);
            			}
            		);
            		predDelta =
            		state[\delta];
            		// ------------------------------------------------------------
            		// LOSS
            		// ------------------------------------------------------------
            		localLoss = this.maskedMSE(prediction, y);
            		localDeltaLoss =
            		this.vectorMSE(
            			predDelta,
            			targetDelta
            		);
            		trajectoryLoss =
            		this.vectorMSE(
            			state[\trajectoryVelocity].copyRange(0, outputSize - 1),
            			targetDelta
            		);
            		directionLoss =
            		this.vectorDirectionLoss(
            			predDelta,
            			targetDelta
            		);
            		outputLoss = localLoss;
            		deltaLoss = localDeltaLoss;
            		loss =
            		(predictionLossWeight * localLoss)
            		+
            		(deltaLossWeight * localDeltaLoss)
            		+
            		(directionLossWeight * directionLoss);
            		surpriseValue =
            		localLoss.sqrt.clip(
            			0.0,
            			1.0
            		);
            		if(
            			replayMode.not,
            			{
            				surprise =
            				surpriseValue;
            				surpriseEMA =
            				(
            					0.95
            					*
            					surpriseEMA
            				)
            				+
            				(
            					0.05
            					*
            					surprise
            				);
            				errorEMA =
            				(
            					0.95
            					*
            					errorEMA
            				)
            				+
            				(
            					0.05
            					*
            					localLoss
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// ADAPTATION
            		//
            		// Surprise gives temporary plasticity.
            		// Protection reduces destructive updates.
            		// ------------------------------------------------------------
            		rateScale =
            		adaptationSlowRate
            		+
            		(
            			(adaptationFastRate - adaptationSlowRate)
            			*
            			surpriseValue.sqrt
            		);
            		rateScale =
            		rateScale
            		*
            		(
            			1.0
            			/
            			(
            				1.0
            				+
            				protectionStrength
            				*
            				protectionScalar
            			)
            		);
            		learningRateCurrent =
            		(
            			learningRate
            			*
            			rateScale
            		).clip(
            			learningRate * 0.05,
            			learningRate * 3.0
            		);
            		if(
            			surpriseValue
            			>
            			surpriseThreshold,
            			{
            				var surprisePressure;
            				surprisePressure =
            				((surpriseValue - surpriseThreshold)
            					/ (1.0 - surpriseThreshold).max(0.000001))
            				.clip(0.0, 1.0);
            				learningRateCurrent =
            				(
            					learningRateCurrent
            					*
            					(1.0 + ((surpriseGain - 1.0) * surprisePressure))
            				).clip(
            					learningRate * 0.05,
            					learningRate * 3.0
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// GRADIENT CONTAINERS
            		// ------------------------------------------------------------
            		gradInputProjection =
            		this.zeroMatrix(
            			hiddenSize,
            			inputSize
            		);
            		gradInputBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradOutputProjection =
            		this.zeroMatrix(
            			outputSize,
            			hiddenSize
            		);
            		gradOutputBias =
            		this.zeroVector(
            			outputSize
            		);
            		gradRouterWeights =
            		this.zeroMatrix(
            			numExperts,
            			hiddenSize
            		);
            		gradRouterBias =
            		this.zeroVector(
            			numExperts
            		);
            		gradExpertW1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		gradExpertB1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		gradExpertW2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		gradExpertB2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		gradQ =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		gradK =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		gradV =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		gradQB =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		gradKB =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		gradVB =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		gradFeatureGates =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					inputSize
            				);
            			}
            		);
            		gradAttentionWeight =
            		this.zeroMatrix(
            			hiddenSize,
            			numHeads * headSize
            		);
            		gradAttentionBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradPositionBias =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					windowSize
            				);
            			}
            		);
            		gradResidual =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradResidualPre =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradAttentionLinear =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradAttentionCombined =
            		this.zeroVector(
            			numHeads * headSize
            		);
            		gradExpertCombined =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradH0 =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradPreHidden =
            		this.zeroVector(
            			hiddenSize
            		);
            		gradRouterLogits =
            		this.zeroVector(
            			numExperts
            		);
            		gradRouter =
            		this.zeroVector(
            			numExperts
            		);
            		gradExpertOutput =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		gradExpertHidden =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// OUTPUT
            		// ------------------------------------------------------------
            		gradPrediction =
            		Array.fill(
            			outputSize,
            			{ |index|
            				predictionLossWeight
            				*
            				2.0
            				*
            				(
            					this.maskedDifference(y[index], prediction[index], index)
            				)
            				/
            				outputSize;
            			}
            		);
            		gradDelta =
            		Array.fill(
            			outputSize,
            			{ |index|
            				var sp;
            				var st;
            				var directionGradient;
            				sp = (12.0 * predDelta[index]).tanh;
            				st = (12.0 * targetDelta[index]).tanh;
            				directionGradient =
            				(
            					24.0
            					*
            					(sp - st)
            					*
            					(1.0 - (sp * sp))
            					/
            					outputSize
            				);
            				gradPrediction[index]
            				+
            				(
            					deltaLossWeight
            					*
            					2.0
            					*
            					(
            						predDelta[index]
            						-
            						targetDelta[index]
            					)
            					/
            					outputSize
            				)
            				+
            				(
            					directionLossWeight
            					*
            					directionGradient
            				);
            			}
            		);
            		gradDeltaRaw =
            		Array.fill(
            			outputSize,
            			{ |index|
            				gradDelta[index]
            				*
            				deltaScale
            				*
            				this.tanhDerivative(
            					this.tanhSafe(
            						state[\deltaRaw][index]
            					)
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// OUTPUT MATRIX
            		// ------------------------------------------------------------
            		outputSize.do({ |r|
            			hiddenSize.do({ |c|
            				gradOutputProjection[r][c] =
            				gradDeltaRaw[r]
            				*
            				state[\residual][c];
            			});
            			gradOutputBias[r] =
            			gradDeltaRaw[r];
            		});
            		hiddenSize.do({ |c|
            			gradResidual[c] =
            			this.dot(
            				outputProjection.collect({ |row|
            					row[c];
            				}),
            				gradDeltaRaw
            			);
            		});
            		// ------------------------------------------------------------
            		// RESIDUAL
            		// ------------------------------------------------------------
            		hiddenSize.do({ |i|
            			gradResidualPre[i] =
            			gradResidual[i]
            			*
            			this.tanhDerivative(
            				state[\residual][i]
            			);
            			gradH0[i] =
            			gradResidualPre[i];
            			gradAttentionLinear[i] =
            			residualScale
            			*
            			gradResidualPre[i];
            			gradExpertCombined[i] =
            			expertScale
            			*
            			gradResidualPre[i];
            		});
            		// ------------------------------------------------------------
            		// ATTENTION OUTPUT
            		// ------------------------------------------------------------
            		hiddenSize.do({ |r|
            			numHeads.do({ |h|
            				headSize.do({ |d|
            					var col;
            					col =
            					(
            						h
            						*
            						headSize
            					)
            					+
            					d;
            					gradAttentionCombined[col] =
            					gradAttentionCombined[col]
            					+
            					(
            						outputAttentionWeight[r][col]
            						*
            						gradAttentionLinear[r]
            					);
            					gradAttentionWeight[r][col] =
            					gradAttentionLinear[r]
            					*
            					state[\attentionCombined][col];
            				});
            			});
            			gradAttentionBias[r] =
            			gradAttentionLinear[r];
            		});
            		// ------------------------------------------------------------
            		// MOE
            		// ------------------------------------------------------------
            		numExperts.do({ |e|
            			hiddenSize.do({ |d|
            				gradExpertOutput[e][d] =
            				gradExpertCombined[d]
            				*
            				state[\router][e];
            				gradRouter[e] =
            				gradRouter[e]
            				+
            				(
            					gradExpertCombined[d]
            					*
            					state[\expertOutputs][e][d]
            				);
            			});
            		});
            		// ------------------------------------------------------------
            		// EXPERT BALANCE GRADIENT
            		// ------------------------------------------------------------
            		numExperts.do({ |e|
            			gradRouter[e] =
            			gradRouter[e]
            			+
            			(
            				expertBalanceStrength
            				*
            				2.0
            				*
            				(
            					state[\router][e]
            					-
            					(1.0 / numExperts)
            				)
            				/
            				numExperts.max(1)
            			);
            		});
            		// ------------------------------------------------------------
            		// ROUTER SOFTMAX
            		// ------------------------------------------------------------
            		numExperts.do({ |e|
            			var s;
            			s = 0.0;
            			numExperts.do({ |k|
            				if(
            					k == e,
            					{
            						s =
            						s
            						+
            						(
            							gradRouter[k]
            							*
            							state[\router][e]
            							*
            							(
            								1.0
            								-
            								state[\router][k]
            							)
            						);
            					},
            					{
            						s =
            						s
            						-
            						(
            							gradRouter[k]
            							*
            							state[\router][k]
            							*
            							state[\router][e]
            						);
            					}
            				);
            			});
            			gradRouterLogits[e] = s / routerTemperature.max(0.01);
            		});
            		numExperts.do({ |e|
            			hiddenSize.do({ |c|
            				gradRouterWeights[e][c] =
            				gradRouterLogits[e]
            				*
            				state[\h0][c];
            			});
            			gradRouterBias[e] =
            			gradRouterLogits[e];
            			hiddenSize.do({ |i|
            				gradH0[i] =
            				gradH0[i]
            				+
            				(
            					routerWeights[e][i]
            					*
            					gradRouterLogits[e]
            				);
            			});
            		});
            		// ------------------------------------------------------------
            		// EXPERTS
            		// ------------------------------------------------------------
            		numExperts.do({ |e|
            			hiddenSize.do({ |i|
            				var g;
            				g =
            				gradExpertOutput[e][i];
            				gradExpertB2[e][i] =
            				g;
            				hiddenSize.do({ |j|
            					gradExpertW2[e][i][j] =
            					g
            					*
            					state[\expertHidden][e][j];
            					gradExpertHidden[e][j] =
            					gradExpertHidden[e][j]
            					+
            					(
            						expertW2[e][i][j]
            						*
            						g
            					);
            				});
            			});
            			hiddenSize.do({ |i|
            				gradExpertHidden[e][i] =
            				gradExpertHidden[e][i]
            				*
            				this.tanhDerivative(
            					state[\expertHidden][e][i]
            				);
            				gradExpertB1[e][i] =
            				gradExpertHidden[e][i];
            				hiddenSize.do({ |j|
            					gradExpertW1[e][i][j] =
            					gradExpertHidden[e][i]
            					*
            					state[\h0][j];
            					gradH0[j] =
            					gradH0[j]
            					+
            					(
            						expertW1[e][i][j]
            						*
            						gradExpertHidden[e][i]
            					);
            				});
            			});
            		});
            		// ------------------------------------------------------------
            		// ATTENTION BACKPROP
            		// ------------------------------------------------------------
            		numHeads.do({ |hh|
            			var q;
            			var weights;
            			var keys;
            			var values;
            			var gatedInput;
            			var gradQHead;
            			var gradWeightsHead;
            			var gradScoresHead;
            			var gradContextHead;
            			q =
            			state[\headQueries][hh];
            			weights =
            			state[\headWeights][hh];
            			keys =
            			state[\headKeys][hh];
            			values =
            			state[\headValues][hh];
            			gatedInput =
            			state[\headFeatureVectors][hh];
            			gradQHead =
            			this.zeroVector(
            				headSize
            			);
            			gradWeightsHead =
            			this.zeroVector(
            				state[\memorySize]
            			);
            			gradScoresHead =
            			this.zeroVector(
            				state[\memorySize]
            			);
            			gradContextHead =
            			Array.fill(
            				headSize,
            				{ |dd|
            					var col;
            					col =
            					(
            						hh
            						*
            						headSize
            					)
            					+
            					dd;
            					gradAttentionCombined[col];
            				}
            			);
            			// context -> values
            			state[\memorySize].do({ |jj|
            				headSize.do({ |dd|
            					gradWeightsHead[jj] =
            					gradWeightsHead[jj]
            					+
            					(
            						gradContextHead[dd]
            						*
            						values[jj][dd]
            					);
            				});
            			});
            			// softmax
            			state[\memorySize].do({ |jj|
            				var s;
            				s = 0.0;
            				state[\memorySize].do({ |kk|
            					if(
            						jj == kk,
            						{
            							s =
            							s
            							+
            							(
            								gradWeightsHead[kk]
            								*
            								weights[jj]
            								*
            								(
            									1.0
            									-
            									weights[kk]
            								)
            							);
            						},
            						{
            							s =
            							s
            							-
            							(
            								gradWeightsHead[kk]
            								*
            								weights[kk]
            								*
            								weights[jj]
            							);
            						}
            					);
            				});
            				gradScoresHead[jj] = s / attentionTemperature.max(0.01);
            				if(
            					jj < windowSize,
            					{
            						gradPositionBias[hh][jj] = gradScoresHead[jj];
            					}
            				);
            			});
            			// q/k
            			state[\memorySize].do({ |jj|
            				var scale;
            				scale =
            				1.0
            				/
            				headSize.sqrt;
            				headSize.do({ |dd|
            					gradQHead[dd] =
            					gradQHead[dd]
            					+
            					(
            						gradScoresHead[jj]
            						*
            						keys[jj][dd]
            						*
            						scale
            					);
            				});
            				headSize.do({ |dd|
            					var keyGrad;
            					keyGrad =
            					gradScoresHead[jj]
            					*
            					q[dd]
            					*
            					scale;
            					inputSize.do({ |ii|
            						var memValue;
            						memValue =
            						state[\memory][jj][ii]
            						*
            						this.getFeatureGate(
            							hh,
            							ii
            						);
            						gradK[hh][dd][ii] =
            						gradK[hh][dd][ii]
            						+
            						(
            							keyGrad
            							*
            							memValue
            						);
                            gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] + (keyGrad * kWeights[hh][dd][ii] * state[\memory][jj][ii]);
            					});
            					gradKB[hh][dd] =
            					gradKB[hh][dd]
            					+
            					keyGrad;
            				});
            			});
            			// Q
            			headSize.do({ |dd|
            				inputSize.do({ |ii|
            					var gatedX;
            					gatedX =
            					state[\input][ii]
            					*
            					this.getFeatureGate(
            						hh,
            						ii
            					);
            					gradQ[hh][dd][ii] =
            					gradQ[hh][dd][ii]
            					+
            					(
            						gradQHead[dd]
            						*
            						gatedX
            					);
            					gradFeatureGates[hh][ii] =
            					gradFeatureGates[hh][ii]
            					+
            					(
            						gradQHead[dd]
            						*
            						qWeights[hh][dd][ii]
            						*
            						state[\input][ii]
            					);
            				});
            				gradQB[hh][dd] =
            				gradQB[hh][dd]
            				+
            				gradQHead[dd];
            			});
            			// V
            			state[\memorySize].do({ |jj|
            				headSize.do({ |dd|
            					var gv;
            					gv =
            					gradContextHead[dd]
            					*
            					weights[jj];
            					gradVB[hh][dd] =
            					gradVB[hh][dd]
            					+
            					gv;
            					inputSize.do({ |ii|
            						var memValue;
            						memValue =
            						state[\memory][jj][ii]
            						*
            						this.getFeatureGate(
            							hh,
            							ii
            						);
            						gradV[hh][dd][ii] =
            						gradV[hh][dd][ii]
            						+
            						(
            							gv
            							*
            							memValue
            						);
                            gradFeatureGates[hh][ii] = gradFeatureGates[hh][ii] + (gv * vWeights[hh][dd][ii] * state[\memory][jj][ii]);
            					});
            				});
            			});
            		});
            		// ------------------------------------------------------------
            		// FEATURE GATES
            		// ------------------------------------------------------------
            		numHeads.do({ |hh|
            			inputSize.do({ |ii|
            				var g;
            				var sg;
            				g =
            				gradFeatureGates[hh][ii];
            				sg =
            				this.getFeatureGate(
            					hh,
            					ii
            				);
            				gradFeatureGates[hh][ii] =
            				g
            				*
            				this.sigmoidDerivative(
            					sg
            				);
            			});
            		});
            		// ------------------------------------------------------------
            		// INPUT BACKPROP
            		// ------------------------------------------------------------
            		hiddenSize.do({ |r|
            			gradPreHidden[r] =
            			gradH0[r]
            			*
            			this.tanhDerivative(
            				state[\h0][r]
            			);
            			gradInputBias[r] =
            			gradPreHidden[r];
            			inputSize.do({ |c|
            				gradInputProjection[r][c] =
            				gradPreHidden[r]
            				*
            				state[\input][c];
            			});
            		});
            		// ------------------------------------------------------------
            		// HEAD SPECIALIZATION
            		// ------------------------------------------------------------
            		numHeads.do({ |hh|
            			numHeads.do({ |kk|
            				if(
            					hh < kk,
            					{
            						inputSize.do({ |ii|
            							var a;
            							var b;
            							var diff;
            							a =
            							this.getFeatureGate(
            								hh,
            								ii
            							);
            							b =
            							this.getFeatureGate(
            								kk,
            								ii
            							);
            							diff =
            							a
            							-
            							b;
            							if(
            								diff.abs
            								>
            								(1.0 - headSpecializationThreshold),
            								{
            									gradFeatureGates[hh][ii] =
            									gradFeatureGates[hh][ii]
            									-
            									(
            										headSpecializationStrength
            										*
            										diff
            									);
            									gradFeatureGates[kk][ii] =
            									gradFeatureGates[kk][ii]
            									+
            									(
            										headSpecializationStrength
            										*
            										diff
            									);
            								}
            							);
            						});
            					}
            				);
            			});
            		});
            		// ------------------------------------------------------------
            		// ADAM
            		// ------------------------------------------------------------
            		adamStep =
            		adamStep
            		+
            		1;
            		this.adamMatrix(
            			inputProjection,
            			gradInputProjection,
            			adamMInput,
            			adamVInput
            		);
            		this.adamVector(
            			inputBias,
            			gradInputBias,
            			adamMInputBias,
            			adamVInputBias
            		);
            		this.adamMatrix(
            			outputProjection,
            			gradOutputProjection,
            			adamMOutput,
            			adamVOutput
            		);
            		this.adamVector(
            			outputBias,
            			gradOutputBias,
            			adamMOutputBias,
            			adamVOutputBias
            		);
            		numHeads.do({ |hh|
            			this.adamMatrix(
            				qWeights[hh],
            				gradQ[hh],
            				adamMQ[hh],
            				adamVQ[hh]
            			);
            			this.adamMatrix(
            				kWeights[hh],
            				gradK[hh],
            				adamMK[hh],
            				adamVK[hh]
            			);
            			this.adamMatrix(
            				vWeights[hh],
            				gradV[hh],
            				adamMV[hh],
            				adamVV[hh]
            			);
            			this.adamVector(
            				qBias[hh],
            				gradQB[hh],
            				adamMBQ[hh],
            				adamVBQ[hh]
            			);
            			this.adamVector(
            				kBias[hh],
            				gradKB[hh],
            				adamMBK[hh],
            				adamVBK[hh]
            			);
            			this.adamVector(
            				vBias[hh],
            				gradVB[hh],
            				adamMBV[hh],
            				adamVBV[hh]
            			);
            			this.adamVector(
            				featureGateLogits[hh],
            				gradFeatureGates[hh],
            				adamMGates[hh],
            				adamVGates[hh],
            				gateLearningRate
            			);
            			this.adamVector(
            				positionBias[hh],
            				gradPositionBias[hh],
            				adamMPosition[hh],
            				adamVPosition[hh]
            			);
            		});
            		this.adamMatrix(
            			outputAttentionWeight,
            			gradAttentionWeight,
            			adamMAttention,
            			adamVAttention
            		);
            		this.adamVector(
            			outputAttentionBias,
            			gradAttentionBias,
            			adamMBAttention,
            			adamVBAttention
            		);
            		this.adamMatrix(
            			routerWeights,
            			gradRouterWeights,
            			adamMRouter,
            			adamVRouter
            		);
            		this.adamVector(
            			routerBias,
            			gradRouterBias,
            			adamMBRouter,
            			adamVBRouter
            		);
            		numExperts.do({ |ee|
            			this.adamMatrix(
            				expertW1[ee],
            				gradExpertW1[ee],
            				adamMExpertW1[ee],
            				adamVExpertW1[ee]
            			);
            			this.adamVector(
            				expertB1[ee],
            				gradExpertB1[ee],
            				adamMExpertB1[ee],
            				adamVExpertB1[ee]
            			);
            			this.adamMatrix(
            				expertW2[ee],
            				gradExpertW2[ee],
            				adamMExpertW2[ee],
            				adamVExpertW2[ee]
            			);
            			this.adamVector(
            				expertB2[ee],
            				gradExpertB2[ee],
            				adamMExpertB2[ee],
            				adamVExpertB2[ee]
            			);
            		});
            		// ------------------------------------------------------------
            		// DIAGNOSTICS
            		// ------------------------------------------------------------
            		attentionProfile =
            		Array.fill(
            			windowSize,
            			{
            				0.0;
            			}
            		);
            		numHeads.do({ |hh|
            			state[\memorySize].do({ |jj|
            				if(
            					jj < windowSize,
            					{
            						attentionProfile[jj] =
            						attentionProfile[jj]
            						+
            						(
            							state[\headWeights][hh][jj]
            							/
            							numHeads
            						);
            					}
            				);
            			});
            		});
            		headActivity =
            		Array.fill(
            			numHeads,
            			{ |hh|
            				state[\headWeights][hh].maxItem;
            			}
            		);
            		headFeatureUsage =
            		Array.fill(
            			numHeads,
            			{ |hh|
            				Array.fill(
            					inputSize,
            					{ |ii|
            						this.getFeatureGate(
            							hh,
            							ii
            						);
            					}
            				);
            			}
            		);
            		expertUsage =
            		state[\router].collect({ |v|
            			v;
            		});
                expertUsageEMA = Array.fill(numExperts, { |e| (0.98 * expertUsageEMA[e]) + (0.02 * expertUsage[e]) });
                routerEntropyEMA = (0.98 * routerEntropyEMA) + (0.02 * expertUsage.collect({ |r| var q; q = r.max(0.000000001); q * q.log.neg; }).sum);
            		// ------------------------------------------------------------
            		// MEMORY UPDATE
            		// ------------------------------------------------------------
            		if(
            			replayMode.not,
            			{
            				memoryNovelty =
            				this.calculateMemoryNovelty(
            					x
            				);
            				this.writeReplay(
            					x,
            					y,
            					(
            						(0.60 * surpriseValue)
            						+
            						(0.40 * memoryNovelty)
            					).clip(0.0, 1.0)
            				);
            				this.writeLongMemory(
            					x,
            					y,
            					surpriseValue
            				);
            				this.writeTrajectoryMemory(
            					x,
            					y,
            					surpriseValue
            				);
            				this.consolidateTrajectoryMemory;
            				lastObservedVelocity =
            				this.expandOutputDelta(
            					targetDelta
            				);
            				this.addToShortMemory(
            					y
            				);
            				this.consolidateMemory;
            				// Protection scalar :
            				// plus les souvenirs sont importants,
            				// plus l'adaptation globale est prudente.
            				if(
            					memoryCount > 0,
            					{
            						protectionScalar =
            						this.vectorMean(
            							longMemoryImportance.copyRange(
            								0,
            								memoryCount - 1
            							)
            						);
            					},
            					{
            						protectionScalar = 0.0;
            					}
            				);
            				learnedEvents =
            				learnedEvents
            				+
            				1;
            				totalEvents =
            				totalEvents
            				+
            				1;
            				lastInput =
            				this.copyVector(
            					x
            				);
            				lastTarget =
            				this.copyVector(
            					y
            				);
            				lastPrediction =
            				this.copyVector(
            					prediction
            				);
            				lastDelta =
            				this.copyVector(
            					predDelta
            				);
            			}
            		);
                if(replayMode.not, {
                    this.metaLearnStep;
                });
            		loss;

        }.value;
    }

    learnPair { |input, target|
        if(unifiedRuntimeRole.not and: { unifiedLearningEnabled.not }, { ^0.0 });
        ^{
            		this.learnPairCore(
            			input,
            			target,
            			true
            		);

        }.value;
    }

    replayMemory {
        ^{
            		var n;
            		var indices;
            		var index;
            		var localReplayLoss;
            		var count;
            		var used;
            		var priorityScores;
            		var totalPriority;
            		var threshold;
            		var cumulative;
            		var chosen;
            		var pairLoss;
            		localReplayLoss = 0.0;
            		count = 0;
            		if(replayCount > 0, {
            			n = replayBatchSize.min(replayCount);
            			indices = Array.new;
            			used = Array.fill(replayCount, { false });
            			n.do({
            				priorityScores = Array.fill(replayCount, { |i|
            					if(used[i], {
            						0.0;
            					}, {
            						(replayPriorityMix * replayImportance[i].clip(0.001, 1.0).pow(1.35))
            						+ (replayUniformMix * 1.0);
            					});
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
            				// Learning+ : difficult memories become more likely to replay.
            				replayImportance[index] =
            				((0.82 * replayImportance[index]) + (0.18 * pairLoss.sqrt.clip(0.0, 1.0)))
            				.clip(0.0, 1.0);
            				count = count + 1;
            			});
            			if(count > 0, {
            				replayLoss = localReplayLoss / count;
            			});
            		});

        }.value;
    }

    predict { |input, requestedWindowSize|
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^nil });
            ^unifiedCurrentRuntime.predict(input, requestedWindowSize);
        });
        ^{
            		var state;
            		state =
            		this.buildForward(
            			this.cleanVector(
            				input
            			),
            			requestedWindowSize
            		);
            		lastInput =
            		this.copyVector(
            			state[\input]
            		);
            		lastPrediction =
            		this.copyVector(
            			state[\prediction]
            		);
            		lastDelta =
            		this.copyVector(
            			state[\delta]
            		);
            		memoryContext =
            		state[\memoryContext].copy;
            		attentionProfile =
            		Array.fill(
            			windowSize,
            			{
            				0.0;
            			}
            		);
            		numHeads.do({ |hh|
            			state[\memorySize].do({ |jj|
            				if(
            					jj < windowSize,
            					{
            						attentionProfile[jj] =
            						attentionProfile[jj]
            						+
            						(
            							state[\headWeights][hh][jj]
            							/
            							numHeads
            						);
            					}
            				);
            			});
            		});
            		headActivity =
            		Array.fill(
            			numHeads,
            			{ |hh|
            				state[\headWeights][hh].maxItem;
            			}
            		);
            		headFeatureUsage =
            		Array.fill(
            			numHeads,
            			{ |hh|
            				Array.fill(
            					inputSize,
            					{ |ii|
            						this.getFeatureGate(
            							hh,
            							ii
            						);
            					}
            				);
            			}
            		);
            		expertUsage =
            		state[\router].collect({ |v|
            			v;
            		});
                expertUsageEMA = Array.fill(numExperts, { |e| (0.98 * expertUsageEMA[e]) + (0.02 * expertUsage[e]) });
                routerEntropyEMA = (0.98 * routerEntropyEMA) + (0.02 * expertUsage.collect({ |r| var q; q = r.max(0.000000001); q * q.log.neg; }).sum);
            		lastPrediction;

        }.value;
    }

    process { |input|
        ^{
            		this.predict(
            			input
            		);

        }.value;
    }

    learn { |input, target|
        ^{
            		this.learnPair(
            			input,
            			target
            		);

        }.value;
    }

    learnEvent { |event|
        if(unifiedRuntimeRole.not and: { unifiedLearningEnabled.not }, { ^0.0 });
        ^{
            		var x;
            		var result;
            		var n;
            		var currentIndex;
            		x =
            		this.cleanVector(
            			event
            		);
            		result = 0.0;
            		// ------------------------------------------
            		// HISTORIQUE
            		// ------------------------------------------
            		eventHistory.add(
            			x.copy
            		);
            		if(
            			eventHistory.size > 32,
            			{
            				eventHistory.removeAt(0);
            			}
            		);
            		// ------------------------------------------
            		// PREMIER EVENEMENT
            		// ------------------------------------------
            		if(
            			totalEvents == 0,
            			{
            				lastInput =
            				x.copy;
            				lastTarget =
            				x.copy;
            				lastPrediction =
            				x.copy;
            				lastDelta =
            				this.zeroVector(
            					outputSize
            				);
            				lastObservedVelocity =
            				this.zeroVector(
            					inputSize
            				);
            				this.addToShortMemory(
            					x
            				);
            				totalEvents = 1;
            			},
            			{
            				result =
            				this.learnPair(
            					lastInput,
            					x
            				);
            				// ----------------------------------
            				// MULTI STEP LEARNING
            				// ----------------------------------
            				n = eventHistory.size;
            				currentIndex = n - 1;
            				// x(t-2) -> x(t)
            				if(
            					(n >= 3)
            					&&
            					{
            						0.15.coin
            					},
            					{
            						this.learnPairCore(
            							eventHistory[
            								currentIndex - 2
            							],
            							x,
            							false
            						);
            					}
            				);
            				// x(t-3) -> x(t)
            				if(
            					(n >= 4)
            					&&
            					{
            						0.04.coin
            					},
            					{
            						this.learnPairCore(
            							eventHistory[
            								currentIndex - 3
            							],
            							x,
            							false
            						);
            					}
            				);
            				// x(t-4) -> x(t)
            				if(
            					(n >= 5)
            					&&
            					{
            						0.01.coin
            					},
            					{
            						this.learnPairCore(
            							eventHistory[
            								currentIndex - 4
            							],
            							x,
            							false
            						);
            					}
            				);
            				lastInput =
            				x.copy;
            				lastTarget =
            				x.copy;
            				lastPrediction =
            				x.copy;
            			}
            		);
            		if(
            			totalEvents > 1
            			&&
            			{
            				replayRate.coin
            			},
            			{
            				this.replayMemory;
            			}
            		);
            		result;

        }.value;
    }

    updateGenerationDiversity { |candidate|
        ^{
            		var minDistance;
            		var totalDistance;
            		var count;
            		var distance;
            		if(
            			generationHistory.size == 0,
            			{
            				generationNovelty = 1.0;
            				generationDiversity = 0.0;
            			},
            			{
            				minDistance = 1.0;
            				totalDistance = 0.0;
            				count = 0;
            				generationHistory.do({ |old|
            					distance =
            					this.memoryDistance(
            						candidate,
            						old
            					);
            					minDistance = minDistance.min(distance);
            					totalDistance = totalDistance + distance;
            					count = count + 1;
            				});
            				generationMinDistance =
            				minDistance.clip(0.0, 1.0);
            				generationMeanDistance =
            				(totalDistance / count.max(1)).clip(0.0, 1.0);
            				generationNovelty =
            				generationMinDistance;
            				generationDiversity =
            				generationMeanDistance;
            			}
            		);
            		generationPressure =
            		(1.0 - generationNovelty).clip(0.0, 1.0);
            		[generationDiversity, generationNovelty];

        }.value;
    }

    applyLocalContractionCompensation { |candidate|
        ^{
            		var n;
            		var startIndex;
            		var recent;
            		var pairDistance;
            		var pairCount;
            		var i;
            		var j;
            		var meanDistance;
            		var centroid;
            		var direction;
            		var magnitude;
            		var velocityMagnitude;
            		var correctionGain;
            		var corrected;
            		n = generationHistory.size.min(localDiversityWindow);
            		if(
            			n < 2,
            			{
            				localDiversity = 0.0;
            				localContractionPressure = 0.0;
            				localContractionCorrection = 0.0;
            				candidate.copy;
            			},
            			{
            				startIndex = generationHistory.size - n;
            				recent = Array.fill(n, { |k|
            					generationHistory[startIndex + k].copy;
            				});
            				meanDistance = 0.0;
            				pairCount = 0;
            				i = 0;
            				while({ i < n }, {
            					j = i + 1;
            					while({ j < n }, {
            						pairDistance = this.memoryDistance(
            							recent[i],
            							recent[j]
            						);
            						meanDistance = meanDistance + pairDistance;
            						pairCount = pairCount + 1;
            						j = j + 1;
            					});
            					i = i + 1;
            				});
            				localDiversity =
            				(meanDistance / pairCount.max(1)).clip(0.0, 1.0);
            				localContractionPressure =
            				((localDiversityFloor - localDiversity)
            					/ localDiversityFloor.max(0.000001))
            				.clip(0.0, 1.0);
            				correctionGain =
            				localContractionPressure * localDiversityGain;
            				centroid = Array.fill(outputSize, { |d|
            					var sum;
            					sum = 0.0;
            					recent.do({ |v|
            						sum = sum + v[d];
            					});
            					sum / n.max(1);
            				});
            				direction = Array.fill(outputSize, { |d|
            					candidate[d] - centroid[d];
            				});
            				magnitude = 0.0;
            				direction.do({ |v|
            					magnitude = magnitude + (v * v);
            				});
            				magnitude = magnitude.sqrt;
            				if(
            					magnitude < 0.000001,
            					{
            						direction = Array.fill(outputSize, { |d|
            							if(
            								d < lastObservedVelocity.size,
            								{
            									lastObservedVelocity[d];
            								},
            								{
            									0.0;
            								}
            							);
            						});
            						velocityMagnitude = 0.0;
            						direction.do({ |v|
            							velocityMagnitude = velocityMagnitude + (v * v);
            						});
            						velocityMagnitude = velocityMagnitude.sqrt;
            						if(
            							velocityMagnitude < 0.000001,
            							{
            								direction = Array.fill(outputSize, { |d|
            									if(
            										(d % 2) == 0,
            										{ 1.0 },
            										{ -1.0 }
            									);
            								});
            							},
            							{}
            						);
            					},
            					{}
            				);
            				magnitude = 0.0;
            				direction.do({ |v|
            					magnitude = magnitude + (v * v);
            				});
            				magnitude = magnitude.sqrt.max(0.000001);
            				corrected = Array.fill(outputSize, { |d|
            					candidate[d]
            					+ (
            						correctionGain
            						* direction[d]
            						/ magnitude
            					).clip(
            						localDiversityMaxCorrection.neg,
            						localDiversityMaxCorrection
            					);
            				});
            				localContractionCorrection =
            				correctionGain.clip(
            					0.0,
            					localDiversityMaxCorrection
            				);
            				corrected;
            			}
            		);

        }.value;
    }

    generateStep { |input|
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^nil });
            ^unifiedCurrentRuntime.generateStep(input);
        });
        ^{
            		var p;
            		var result;
            		var correction;
            		var distance;
            		var repulsion;
            		var noiseGain;
            		var driftCorrection;
            		var generatedDelta;
            		var cleanInput;
            		cleanInput =
            		this.cleanVector(
            			input
            		);
            		p =
            		this.predict(
            			cleanInput,
            			generationWindowSize
            		);
            		driftCorrection =
            		Array.fill(
            			outputSize,
            			{ |i|
            				var localEdgePressure;
            				var localSameDirection;
            				localEdgePressure = 0.0;
            				localSameDirection = false;
            				if(
            					p[i] > 0.80,
            					{
            						localEdgePressure =
            						(
            							(p[i] - 0.80)
            							/
            							0.20
            						).clip(
            							0.0,
            							1.0
            						);
            						if(
            							generationDriftState[\deltaEMA][i] > 0.0,
            							{
            								localSameDirection = true;
            							}
            						);
            					}
            				);
            				if(
            					p[i] < 0.20,
            					{
            						localEdgePressure =
            						(
            							(0.20 - p[i])
            							/
            							0.20
            						).clip(
            							0.0,
            							1.0
            						);
            						if(
            							generationDriftState[\deltaEMA][i] < 0.0,
            							{
            								localSameDirection = true;
            							}
            						);
            					}
            				);
            				if(
            					localSameDirection,
            					{
            						(
            							generationDriftState[\gain]
            							*
            							localEdgePressure
            							*
            							generationDriftState[\deltaEMA][i]
            						).clip(
            							generationDriftState[\maxCorrection].neg,
            							generationDriftState[\maxCorrection]
            						);
            					},
            					{
            						0.0;
            					}
            				);
            			}
            		);
            		correction =
            		Array.fill(
            			outputSize,
            			0.0
            		);
            		// ------------------------------------------------------------
            		// REPULSION FROM RECENT GENERATED VECTORS
            		// ------------------------------------------------------------
            		generationHistory.do({ |old|
            			distance =
            			this.memoryDistance(
            				p,
            				old
            			);
            			if(
            				distance < diversityRadius,
            				{
            					repulsion =
            					(
            						(diversityRadius - distance)
            						/
            						diversityRadius.max(0.000001)
            					).clip(0.0, 1.0);
            					outputSize.do({ |i|
            						correction[i] =
            						correction[i]
            						+
            						(
            							diversityRepulsionGain
            							*
            							(1.0 + (generationPressure * diversityAdaptiveGain))
            							*
            							repulsion
            							*
            							(p[i] - old[i])
            						);
            					});
            				}
            			);
            		});
            		// ------------------------------------------------------------
            		// SMALL STOCHASTIC EXPLORATION
            		// Scaled by trajectory novelty but kept tightly bounded.
            		// ------------------------------------------------------------
            		noiseGain =
            		diversityNoiseGain
            		*
            		(1.0 + (trajectoryNovelty * 0.50));
            		result =
            		Array.fill(
            			outputSize,
            			{ |i|
            				this.maskedWrap(
            					p[i]
            					-
            					driftCorrection[i]
            					+
            					correction[i].clip(
            						diversityMaxCorrection.neg,
            						diversityMaxCorrection
            					)
            					+
            					(
            						trajectoryExplorationGain
            						*
            						(
            							1.0
            							+
            							(
            								trajectoryNovelty
            								* 0.75
            							)
            						)
            						*
            						1.0.rand2
            					)
            					+
            					(
            						noiseGain
            						*
            						1.0.rand2
            					),
            					i
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// V30.0 ONLY: SOFT LOCAL CONTRACTION COMPENSATION
            		// Applied after the existing diversity controller.
            		// ------------------------------------------------------------
            		result =
            		this.applyLocalContractionCompensation(
            			result
            		);
            		result = Array.fill(outputSize, { |i|
            			this.maskedWrap(this.edgePush(result[i], i), i);
            		});
            		generatedDelta =
            		Array.fill(
            			outputSize,
            			{ |i|
            				this.maskedDifference(cleanInput[i], result[i], i);
            			}
            		);
            		generationDriftState[\deltaEMA] =
            		Array.fill(
            			outputSize,
            			{ |i|
            				(
            					generationDriftState[\emaDecay]
            					*
            					generationDriftState[\deltaEMA][i]
            				)
            				+
            				(
            					(
            						1.0
            						-
            						generationDriftState[\emaDecay]
            					)
            					*
            					generatedDelta[i]
            				);
            			}
            		);
            		this.updateGenerationDiversity(
            			result
            		);
            		if(
            			generationHistory.size
            			>=
            			diversityHistorySize,
            			{
            				generationHistory.removeAt(0);
            			}
            		);
            		generationHistory.add(
            			result.copy
            		);
            		lastObservedVelocity =
            		this.expandOutputDelta(generatedDelta);
                this.autoTuneStep;
            		result;

        }.value;
    }

    generate { |seed, count=32|
        ^this.generateMapped(seed, count, nil);
    }

    generateMapped { |seed, count=32, feedbackFunction|
        var result;
        var currentInput;
        var currentOutput;
        var feedback;
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            if(unifiedGenerationEnabled.not, { ^Array.new });
            ^unifiedCurrentRuntime.generateMapped(
                seed,
                count,
                feedbackFunction
            );
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
                    this.cleanInputVector(
                        feedback.value(currentOutput, currentInput.copy)
                    );
                });
                this.addToShortMemory(currentInput);
            });
        });
        ^result.asArray;
    }

    // ============================================================
    // RUNTIME CONTROL API
    // Les param√®tres ci-dessous peuvent √™tre modifi√©s entre deux
    // appels √† generateStep, y compris depuis une Routine, un GUI,
    // MIDI ou OSC. Les dimensions structurelles restent immuables.
    // ============================================================

    runtimeParameterNames {
        ^[
            \learningRate, \temperature, \attentionTemperature, \routerTemperature, \trajectoryRetrievalTemperature,
            \residualScale, \expertScale, \deltaScale,
            \surpriseThreshold, \surpriseGain,
            \gateLearningRate, \gradientClip,
            \memoryWriteThreshold, \memoryRetrievalGain,
            \memoryRetrievalTemperature, \memoryDecay,
            \memoryConsolidationRate, \memoryUsageDecay,
            \replayRate, \replayBatchSize, \memoryRecallSize,
            \protectionStrength,
            \adaptationFastRate, \adaptationSlowRate,
            \predictionLossWeight, \deltaLossWeight,
            \directionLossWeight, \replayPriorityMix,
            \replayUniformMix, \expertBalanceStrength,
            \headSpecializationThreshold,
            \headSpecializationStrength,
            \interferenceTestSteps,
            \trajectoryRecallSize, \trajectoryRetrievalGain,
            \trajectoryVelocityGain, \trajectoryAccelerationGain,
            \trajectoryVelocityClip, \trajectoryAccelerationClip,
            \trajectoryDecay, \trajectoryUsageDecay,
            \trajectoryNoveltyWeight, \trajectoryInputWeight,
            \trajectoryVelocityWeight, \trajectoryExplorationGain,
            \diversityHistorySize, \diversityRadius,
            \diversityRepulsionGain, \diversityNoiseGain,
            \diversityMaxCorrection, \diversityAdaptiveGain,
            \localDiversityWindow, \localDiversityFloor,
            \localDiversityGain, \localDiversityMaxCorrection,
            \generationWindowSize,
            \driftEmaDecay, \driftGain, \driftMaxCorrection,
            \autoTuneEnabled, \autoTuneInterval,
            \autoTuneStrength, \autoTuneSmoothing,
            \autoTuneTargetNovelty, \autoTuneTargetDiversity,
            \autoTuneExplorationMin, \autoTuneExplorationMax,
            \autoTuneNoiseMin, \autoTuneNoiseMax,
            \autoTuneTemperatureMin, \autoTuneTemperatureMax,
            \metaLearnEnabled, \metaLearnInterval,
            \metaLearnStrength, \metaLearnSmoothing,
            \metaLearnTargetError, \metaLearnTargetSurprise,
            \metaLearnTargetRecall, \metaLearnTargetInterference,
            \metaLearnLearningRateMin, \metaLearnLearningRateMax,
            \metaLearnReplayRateMin, \metaLearnReplayRateMax,
            \metaLearnProtectionMin, \metaLearnProtectionMax,
            \metaLearnRetrievalGainMin, \metaLearnRetrievalGainMax,
            \torusMask
        ];
    }

    isRuntimeParameter { |name|
        ^this.runtimeParameterNames.includes(name.asSymbol);
    }

    getParameter { |name|
        var key;
        key = name.asSymbol;
        ^switch(key,
            \learningRate, { learningRate },
            \temperature, { temperature },
            \attentionTemperature, { attentionTemperature },
            \routerTemperature, { routerTemperature },
            \trajectoryRetrievalTemperature, { trajectoryRetrievalTemperature },
            \residualScale, { residualScale },
            \expertScale, { expertScale },
            \deltaScale, { deltaScale },
            \surpriseThreshold, { surpriseThreshold },
            \surpriseGain, { surpriseGain },
            \gateLearningRate, { gateLearningRate },
            \gradientClip, { gradientClip },
            \memoryWriteThreshold, { memoryWriteThreshold },
            \memoryRetrievalGain, { memoryRetrievalGain },
            \memoryRetrievalTemperature, { memoryRetrievalTemperature },
            \memoryDecay, { memoryDecay },
            \memoryConsolidationRate, { memoryConsolidationRate },
            \memoryUsageDecay, { memoryUsageDecay },
            \replayRate, { replayRate },
            \replayBatchSize, { replayBatchSize },
            \memoryRecallSize, { memoryRecallSize },
            \protectionStrength, { protectionStrength },
            \adaptationFastRate, { adaptationFastRate },
            \adaptationSlowRate, { adaptationSlowRate },
            \predictionLossWeight, { predictionLossWeight },
            \deltaLossWeight, { deltaLossWeight },
            \directionLossWeight, { directionLossWeight },
            \replayPriorityMix, { replayPriorityMix },
            \replayUniformMix, { replayUniformMix },
            \expertBalanceStrength, { expertBalanceStrength },
            \headSpecializationThreshold, { headSpecializationThreshold },
            \headSpecializationStrength, { headSpecializationStrength },
            \interferenceTestSteps, { interferenceTestSteps },
            \trajectoryRecallSize, { trajectoryRecallSize },
            \trajectoryRetrievalGain, { trajectoryRetrievalGain },
            \trajectoryVelocityGain, { trajectoryVelocityGain },
            \trajectoryAccelerationGain, { trajectoryAccelerationGain },
            \trajectoryVelocityClip, { trajectoryVelocityClip },
            \trajectoryAccelerationClip, { trajectoryAccelerationClip },
            \trajectoryDecay, { trajectoryDecay },
            \trajectoryUsageDecay, { trajectoryUsageDecay },
            \trajectoryNoveltyWeight, { trajectoryNoveltyWeight },
            \trajectoryInputWeight, { trajectoryInputWeight },
            \trajectoryVelocityWeight, { trajectoryVelocityWeight },
            \trajectoryExplorationGain, { trajectoryExplorationGain },
            \diversityHistorySize, { diversityHistorySize },
            \diversityRadius, { diversityRadius },
            \diversityRepulsionGain, { diversityRepulsionGain },
            \diversityNoiseGain, { diversityNoiseGain },
            \diversityMaxCorrection, { diversityMaxCorrection },
            \diversityAdaptiveGain, { diversityAdaptiveGain },
            \localDiversityWindow, { localDiversityWindow },
            \localDiversityFloor, { localDiversityFloor },
            \localDiversityGain, { localDiversityGain },
            \localDiversityMaxCorrection, { localDiversityMaxCorrection },
            \generationWindowSize, { generationWindowSize },
            \driftEmaDecay, { generationDriftState[\emaDecay] },
            \driftGain, { generationDriftState[\gain] },
            \driftMaxCorrection, { generationDriftState[\maxCorrection] },
            \autoTuneEnabled, { autoTuneEnabled },
            \autoTuneInterval, { autoTuneInterval },
            \autoTuneStrength, { autoTuneStrength },
            \autoTuneSmoothing, { autoTuneSmoothing },
            \autoTuneTargetNovelty, { autoTuneTargetNovelty },
            \autoTuneTargetDiversity, { autoTuneTargetDiversity },
            \autoTuneExplorationMin, { autoTuneExplorationMin },
            \autoTuneExplorationMax, { autoTuneExplorationMax },
            \autoTuneNoiseMin, { autoTuneNoiseMin },
            \autoTuneNoiseMax, { autoTuneNoiseMax },
            \autoTuneTemperatureMin, { autoTuneTemperatureMin },
            \autoTuneTemperatureMax, { autoTuneTemperatureMax },
            \metaLearnEnabled, { metaLearnEnabled },
            \metaLearnInterval, { metaLearnInterval },
            \metaLearnStrength, { metaLearnStrength },
            \metaLearnSmoothing, { metaLearnSmoothing },
            \metaLearnTargetError, { metaLearnTargetError },
            \metaLearnTargetSurprise, { metaLearnTargetSurprise },
            \metaLearnTargetRecall, { metaLearnTargetRecall },
            \metaLearnTargetInterference, { metaLearnTargetInterference },
            \metaLearnLearningRateMin, { metaLearnLearningRateMin },
            \metaLearnLearningRateMax, { metaLearnLearningRateMax },
            \metaLearnReplayRateMin, { metaLearnReplayRateMin },
            \metaLearnReplayRateMax, { metaLearnReplayRateMax },
            \metaLearnProtectionMin, { metaLearnProtectionMin },
            \metaLearnProtectionMax, { metaLearnProtectionMax },
            \metaLearnRetrievalGainMin, { metaLearnRetrievalGainMin },
            \metaLearnRetrievalGainMax, { metaLearnRetrievalGainMax },
            \torusMask, { torusMask.copy },
            { Error("Unknown runtime parameter: " ++ key).throw }
        );
    }

    setParameterLocal { |name, value, post=true|
        var key, result, lossSum, prioritySum;
        key = name.asSymbol;

        // Les tailles de matrices et de m√©moires allou√©es ne doivent
        // pas changer √† chaud. Une nouvelle instance est n√©cessaire.
        if([
            \inputSize, \outputSize, \windowSize, \longMemorySize,
            \hiddenSize, \numHeads, \headSize, \numExperts,
            \trajectoryMemorySize
        ].includes(key), {
            Error("Structural parameter cannot be changed at runtime: " ++ key).throw;
        });

        switch(key,
            \learningRate, {
                learningRate = value.asFloat.clip(0.0000001, 1.0);
                learningRateCurrent = learningRateCurrent.clip(
                    learningRate * 0.05, learningRate * 3.0
                );
            },
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
            \protectionStrength, { protectionStrength = value.asFloat.clip(0.0, 10.0) },
            \adaptationFastRate, { adaptationFastRate = value.asFloat.clip(0.0, 10.0) },
            \adaptationSlowRate, { adaptationSlowRate = value.asFloat.clip(0.0, 10.0) },
            \predictionLossWeight, { predictionLossWeight = value.asFloat.clip(0.0, 1.0) },
            \deltaLossWeight, { deltaLossWeight = value.asFloat.clip(0.0, 1.0) },
            \directionLossWeight, { directionLossWeight = value.asFloat.clip(0.0, 1.0) },
            \replayPriorityMix, {
                replayPriorityMix = value.asFloat.clip(0.0, 1.0);
                prioritySum = replayPriorityMix + replayUniformMix;
                if(prioritySum <= 0.0, { replayUniformMix = 1.0 });
            },
            \replayUniformMix, {
                replayUniformMix = value.asFloat.clip(0.0, 1.0);
                prioritySum = replayPriorityMix + replayUniformMix;
                if(prioritySum <= 0.0, { replayPriorityMix = 1.0 });
            },
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
            \diversityHistorySize, {
                diversityHistorySize = value.asInteger.clip(1, 4096);
                while({ generationHistory.size > diversityHistorySize }, {
                    generationHistory.removeAt(0);
                });
            },
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
            \autoTuneEnabled, {
                autoTuneEnabled = if(value == true, { true }, {
                    if(value.isNumber, { value.asFloat > 0.0 }, { false });
                });
            },
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
            \metaLearnEnabled, {
                metaLearnEnabled = if(value == true, { true }, {
                    if(value.isNumber, { value.asFloat > 0.0 }, { false });
                });
            },
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
            \torusMask, { torusMask = this.normalizeTorusMask(value) },
            { Error("Unknown runtime parameter: " ++ key).throw }
        );

        // Les trois poids de perte sont normalis√©s pour conserver
        // une √©chelle de perte stable lors d'un contr√¥le en direct.
        if([
            \predictionLossWeight, \deltaLossWeight, \directionLossWeight
        ].includes(key), {
            lossSum = predictionLossWeight + deltaLossWeight + directionLossWeight;
            if(lossSum <= 0.0, {
                predictionLossWeight = 1.0;
                deltaLossWeight = 0.0;
                directionLossWeight = 0.0;
            }, {
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
        settings.keysValuesDo({ |key, value|
            this.setParameter(key, value, post);
        });
        ^this.runtimeConfig;
    }

    runtimeConfig {
        var result;
        result = IdentityDictionary.new;
        this.runtimeParameterNames.do({ |key|
            result[key] = this.getParameter(key);
        });
        ^result;
    }

    // Morphing asynchrone d'un param√®tre. √Ä lancer depuis AppClock
    // ou depuis une Routine compatible avec wait.
    morphParameterLocal { |name, target, duration=1.0, steps=50, clock|
        var key, start, amount, safeSteps, safeDuration;
        key = name.asSymbol;
        start = this.getParameter(key);
        if(start.isNumber.not or: { target.isNumber.not }, {
            Error("morphParameter requires a numeric parameter").throw;
        });
        safeSteps = steps.asInteger.max(1);
        safeDuration = duration.asFloat.max(0.0);
        ^Routine({
            safeSteps.do({ |index|
                amount = (index + 1) / safeSteps;
                this.setParameter(
                    key,
                    start + ((target.asFloat - start) * amount),
                    false
                );
                (safeDuration / safeSteps).wait;
            });
        }).play(clock ? AppClock);
    }

    // ============================================================
    // AUTOMATIC RUNTIME PARAMETER TUNING
    // Adjusts generation parameters from measured novelty/diversity.
    // It never changes structural dimensions or learned weights.
    // ============================================================
    // ============================================================
    // META-LEARNING CONTROLLER
    // Regulates the learning strategy, not the network structure.
    // It runs only after real learning events, never during replay.
    // ============================================================
    metaLearnStep {
        var errorPressure, surprisePressure, recallPressure, interferencePressure;
        var targetLearningRate, targetReplayRate;
        var targetProtection, targetRetrievalGain;
        metaLearnCounter = metaLearnCounter + 1;
        if(metaLearnEnabled.not, { ^this.metaLearnStatus });
        if((metaLearnCounter % metaLearnInterval) != 0, { ^this.metaLearnStatus });
        metaLearnErrorEMA =
        (metaLearnSmoothing * metaLearnErrorEMA)
        + ((1.0 - metaLearnSmoothing) * errorEMA.clip(0.0, 1.0));
        metaLearnSurpriseEMA =
        (metaLearnSmoothing * metaLearnSurpriseEMA)
        + ((1.0 - metaLearnSmoothing) * surpriseEMA.clip(0.0, 1.0));
        metaLearnRecallEMA =
        (metaLearnSmoothing * metaLearnRecallEMA)
        + ((1.0 - metaLearnSmoothing) * memoryRecall.clip(0.0, 1.0));
        metaLearnInterferenceEMA =
        (metaLearnSmoothing * metaLearnInterferenceEMA)
        + ((1.0 - metaLearnSmoothing) * interferenceScore.clip(-1.0, 1.0));
        errorPressure = (metaLearnErrorEMA - metaLearnTargetError).clip(-1.0, 1.0);
        surprisePressure = (metaLearnSurpriseEMA - metaLearnTargetSurprise).clip(-1.0, 1.0);
        recallPressure = (metaLearnTargetRecall - metaLearnRecallEMA).clip(-1.0, 1.0);
        interferencePressure =
        (metaLearnInterferenceEMA - metaLearnTargetInterference).clip(-1.0, 1.0);
        metaLearnPlasticityPressure =
        ((0.70 * errorPressure) + (0.30 * surprisePressure)).clip(-1.0, 1.0);
        metaLearnStabilityPressure =
        ((0.80 * interferencePressure) + (0.20 * protectionScalar)).clip(-1.0, 1.0);
        targetLearningRate =
        (learningRate * (
            1.0
            + (metaLearnStrength * 0.10 * metaLearnPlasticityPressure)
            - (metaLearnStrength * 0.14 * metaLearnStabilityPressure)
        )).clip(metaLearnLearningRateMin, metaLearnLearningRateMax);
        targetReplayRate =
        (replayRate + (
            metaLearnStrength * 0.020
            * ((0.55 * errorPressure) + (0.85 * interferencePressure))
        )).clip(metaLearnReplayRateMin, metaLearnReplayRateMax);
        targetProtection =
        (protectionStrength + (
            metaLearnStrength * 0.030
            * (interferencePressure + (0.25 * surprisePressure) - (0.15 * errorPressure))
        )).clip(metaLearnProtectionMin, metaLearnProtectionMax);
        targetRetrievalGain =
        (memoryRetrievalGain + (
            metaLearnStrength * 0.020 * recallPressure
        )).clip(metaLearnRetrievalGainMin, metaLearnRetrievalGainMax);
        this.setParameter(\learningRate, targetLearningRate, false);
        this.setParameter(\replayRate, targetReplayRate, false);
        this.setParameter(\protectionStrength, targetProtection, false);
        this.setParameter(\memoryRetrievalGain, targetRetrievalGain, false);
        metaLearnAdjustmentCount = metaLearnAdjustmentCount + 1;
        ^this.metaLearnStatus;
    }
    metaLearnStatus {
        ^(
            enabled: metaLearnEnabled,
            interval: metaLearnInterval,
            counter: metaLearnCounter,
            adjustments: metaLearnAdjustmentCount,
            errorEMA: metaLearnErrorEMA,
            surpriseEMA: metaLearnSurpriseEMA,
            recallEMA: metaLearnRecallEMA,
            interferenceEMA: metaLearnInterferenceEMA,
            plasticityPressure: metaLearnPlasticityPressure,
            stabilityPressure: metaLearnStabilityPressure,
            learningRate: learningRate,
            replayRate: replayRate,
            protectionStrength: protectionStrength,
            memoryRetrievalGain: memoryRetrievalGain
        );
    }
    enableMetaLearning {
        this.setParameter(\metaLearnEnabled, true, false);
        "Meta-learning controller enabled".postln;
        ^this.metaLearnStatus;
    }
    disableMetaLearning {
        this.setParameter(\metaLearnEnabled, false, false);
        "Meta-learning controller disabled".postln;
        ^this.metaLearnStatus;
    }
    resetMetaLearning {
        metaLearnCounter = 0;
        metaLearnAdjustmentCount = 0;
        metaLearnErrorEMA = errorEMA.clip(0.0, 1.0);
        metaLearnSurpriseEMA = surpriseEMA.clip(0.0, 1.0);
        metaLearnRecallEMA = memoryRecall.clip(0.0, 1.0);
        metaLearnInterferenceEMA = interferenceScore.clip(-1.0, 1.0);
        metaLearnPlasticityPressure = 0.0;
        metaLearnStabilityPressure = 0.0;
        ^this.metaLearnStatus;
    }
    autoTuneStep {
        var noveltyError, diversityError, combinedError;
        var targetExploration, targetNoise, targetTemperature;
        autoTuneCounter = autoTuneCounter + 1;
        if(autoTuneEnabled.not, { ^this.autoTuneStatus });
        if((autoTuneCounter % autoTuneInterval) != 0, { ^this.autoTuneStatus });
        autoTuneNoveltyEMA =
        (autoTuneSmoothing * autoTuneNoveltyEMA)
        + ((1.0 - autoTuneSmoothing) * generationNovelty.clip(0.0, 1.0));
        autoTuneDiversityEMA =
        (autoTuneSmoothing * autoTuneDiversityEMA)
        + ((1.0 - autoTuneSmoothing) * generationDiversity.clip(0.0, 1.0));
        noveltyError = autoTuneTargetNovelty - autoTuneNoveltyEMA;
        diversityError = autoTuneTargetDiversity - autoTuneDiversityEMA;
        combinedError = ((noveltyError + diversityError) * 0.5).clip(-1.0, 1.0);
        autoTuneLastError = combinedError;
        targetExploration =
        (trajectoryExplorationGain + (combinedError * autoTuneStrength * 0.010))
        .clip(autoTuneExplorationMin, autoTuneExplorationMax);
        targetNoise =
        (diversityNoiseGain + (diversityError * autoTuneStrength * 0.002))
        .clip(autoTuneNoiseMin, autoTuneNoiseMax);
        targetTemperature =
        (attentionTemperature + (noveltyError * autoTuneStrength * 0.20))
        .clip(autoTuneTemperatureMin, autoTuneTemperatureMax);
        this.setParameter(\trajectoryExplorationGain, targetExploration, false);
        this.setParameter(\diversityNoiseGain, targetNoise, false);
        this.setParameter(\attentionTemperature, targetTemperature, false);
        autoTuneAdjustmentCount = autoTuneAdjustmentCount + 1;
        ^this.autoTuneStatus;
    }
    autoTuneStatus {
        ^(
            enabled: autoTuneEnabled,
            interval: autoTuneInterval,
            counter: autoTuneCounter,
            adjustments: autoTuneAdjustmentCount,
            noveltyEMA: autoTuneNoveltyEMA,
            diversityEMA: autoTuneDiversityEMA,
            lastError: autoTuneLastError,
            trajectoryExplorationGain: trajectoryExplorationGain,
            diversityNoiseGain: diversityNoiseGain,
            temperature: temperature
        );
    }
    enableAutoTune {
        this.setParameter(\autoTuneEnabled, true, false);
        "Automatic runtime tuning enabled".postln;
        ^this.autoTuneStatus;
    }
    disableAutoTune {
        this.setParameter(\autoTuneEnabled, false, false);
        "Automatic runtime tuning disabled".postln;
        ^this.autoTuneStatus;
    }
    resetAutoTune {
        autoTuneCounter = 0;
        autoTuneAdjustmentCount = 0;
        autoTuneNoveltyEMA = generationNovelty.clip(0.0, 1.0);
        autoTuneDiversityEMA = generationDiversity.clip(0.0, 1.0);
        autoTuneLastError = 0.0;
        ^this.autoTuneStatus;
    }
    resetOptimizerState {
        ^{
            		adamMInput =
            		this.zeroMatrix(
            			hiddenSize,
            			inputSize
            		);
            		adamVInput =
            		this.zeroMatrix(
            			hiddenSize,
            			inputSize
            		);
            		adamMInputBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		adamVInputBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		adamMOutput =
            		this.zeroMatrix(
            			outputSize,
            			hiddenSize
            		);
            		adamVOutput =
            		this.zeroMatrix(
            			outputSize,
            			hiddenSize
            		);
            		adamMOutputBias =
            		this.zeroVector(
            			outputSize
            		);
            		adamVOutputBias =
            		this.zeroVector(
            			outputSize
            		);
            		adamMQ =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamVQ =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamMK =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamVK =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamMV =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamVV =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroMatrix(
            					headSize,
            					inputSize
            				);
            			}
            		);
            		adamMBQ =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamVBQ =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamMBK =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamVBK =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamMBV =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamVBV =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		adamMAttention =
            		this.zeroMatrix(
            			hiddenSize,
            			numHeads
            			*
            			headSize
            		);
            		adamVAttention =
            		this.zeroMatrix(
            			hiddenSize,
            			numHeads
            			*
            			headSize
            		);
            		adamMBAttention =
            		this.zeroVector(
            			hiddenSize
            		);
            		adamVBAttention =
            		this.zeroVector(
            			hiddenSize
            		);
            		adamMGates =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					inputSize
            				);
            			}
            		);
            		adamVGates =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					inputSize
            				);
            			}
            		);
            		adamMRouter =
            		this.zeroMatrix(
            			numExperts,
            			hiddenSize
            		);
            		adamVRouter =
            		this.zeroMatrix(
            			numExperts,
            			hiddenSize
            		);
            		adamMBRouter =
            		this.zeroVector(
            			numExperts
            		);
            		adamVBRouter =
            		this.zeroVector(
            			numExperts
            		);
            		adamMExpertW1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		adamVExpertW1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		adamMExpertB1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		adamVExpertB1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		adamMExpertW2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		adamVExpertW2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroMatrix(
            					hiddenSize,
            					hiddenSize
            				);
            			}
            		);
            		adamMExpertB2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		adamVExpertB2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		adamMPosition =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					windowSize
            				);
            			}
            		);
            		adamVPosition =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					windowSize
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		adamStep = 0;
            		learningRateCurrent = learningRate;
            		nil;

        }.value;
    }

    resetLearning {
        ^{
            		shortMemory =
            		List.new;
            		lastInput =
            		nil;
            		lastPrediction =
            		Array.fill(
            			outputSize,
            			0.5
            		);
            		lastDelta =
            		Array.fill(
            			outputSize,
            			0.0
            		);
            		lastObservedVelocity =
            		this.zeroVector(
            			inputSize
            		);
            		generationHistory =
            		List.new;
            		generationDriftState = (
            			deltaEMA: this.zeroVector(
            				outputSize
            			),
            			emaDecay: 0.985,
            			gain: 0.45,
            			maxCorrection: 0.0025
            		);
            		generationDiversity = 0.0;
            		generationNovelty = 1.0;
            		generationMinDistance = 1.0;
            		generationMeanDistance = 0.0;
            		generationPressure = 0.0;
            		localDiversity = 0.0;
            		localContractionPressure = 0.0;
            		localContractionCorrection = 0.0;
            		lastTarget =
            		nil;
            		attentionProfile =
            		Array.fill(
            			windowSize,
            			0.0
            		);
            		if(
            			windowSize > 0,
            			{
            				attentionProfile[0] =
            				1.0;
            			}
            		);
            		headActivity =
            		Array.fill(
            			numHeads,
            			0.0
            		);
            		expertUsage =
            		Array.fill(
            			numExperts,
            			1.0
            			/
            			numExperts
            		);
                expertUsageEMA = Array.fill(numExperts, 1.0 / numExperts);
                routerEntropyEMA = 0.0;
            		loss = 0.0;
            		outputLoss = 0.0;
            		deltaLoss = 0.0;
            		directionLoss = 0.0;
            		expertBalanceLoss = 0.0;
            		surprise = 0.0;
            		surpriseEMA = 0.0;
            		errorEMA = 0.0;
            		replayLoss = 0.0;
            		interferenceScore = 0.0;
            		memoryRecall = 0.0;
            		memoryNovelty = 0.0;
            		memoryWriteScore = 0.0;
            		entropy = 0.0;
            		learningRateCurrent =
            		learningRate;
            		learnedEvents = 0;
            		this.resetOptimizerState;
            		totalEvents = 0;
            		protectionScalar = 0.0;
            		eventHistory = List.new;
                // Automatic runtime tuning defaults. Disabled by default.
                autoTuneEnabled = false;
                autoTuneInterval = 8;
                autoTuneCounter = 0;
                autoTuneStrength = 0.20;
                autoTuneSmoothing = 0.90;
                autoTuneTargetNovelty = 0.18;
                autoTuneTargetDiversity = 0.12;
                autoTuneNoveltyEMA = 0.0;
                autoTuneDiversityEMA = 0.0;
                autoTuneLastError = 0.0;
                autoTuneAdjustmentCount = 0;
                autoTuneExplorationMin = 0.0005;
                autoTuneExplorationMax = 0.0300;
                autoTuneNoiseMin = 0.0;
                autoTuneNoiseMax = 0.0100;
                autoTuneTemperatureMin = 0.50;
                autoTuneTemperatureMax = 2.50;
                // Meta-learning defaults. Disabled by default.
                metaLearnEnabled = false;
                metaLearnInterval = 16;
                metaLearnCounter = 0;
                metaLearnStrength = 0.12;
                metaLearnSmoothing = 0.95;
                metaLearnTargetError = 0.015;
                metaLearnTargetSurprise = 0.08;
                metaLearnTargetRecall = 0.50;
                metaLearnTargetInterference = 0.0;
                metaLearnErrorEMA = 0.0;
                metaLearnSurpriseEMA = 0.0;
                metaLearnRecallEMA = 0.0;
                metaLearnInterferenceEMA = 0.0;
                metaLearnPlasticityPressure = 0.0;
                metaLearnStabilityPressure = 0.0;
                metaLearnAdjustmentCount = 0;
                metaLearnLearningRateMin = 0.00005;
                metaLearnLearningRateMax = 0.00200;
                metaLearnReplayRateMin = 0.02;
                metaLearnReplayRateMax = 0.30;
                metaLearnProtectionMin = 0.05;
                metaLearnProtectionMax = 0.60;
                metaLearnRetrievalGainMin = 0.02;
                metaLearnRetrievalGainMax = 0.30;
            		"V30.0 Learning+ Dynamic Trajectory Memory runtime and optimizer reset / weights and memories preserved".postln;
            		nil;

        }.value;
    }

    resetMemory {
        ^{
            		longMemory =
            		Array.newClear(
            			longMemorySize
            		);
            		longMemoryImportance =
            		Array.fill(
            			longMemorySize,
            			0.0
            		);
            		longMemoryAge =
            		Array.fill(
            			longMemorySize,
            			0
            		);
            		longMemoryUsage =
            		Array.fill(
            			longMemorySize,
            			0.0
            		);
            		longMemorySurprise =
            		Array.fill(
            			longMemorySize,
            			0.0
            		);
            		replayInputs =
            		Array.newClear(
            			longMemorySize
            		);
            		replayTargets =
            		Array.newClear(
            			longMemorySize
            		);
            		replayImportance =
            		Array.fill(
            			longMemorySize,
            			0.0
            		);
            		memoryCount = 0;
            		replayCount = 0;
            		trajectoryMemory = List.new;
            		trajectoryCount = 0;
            		trajectoryVelocityContext = this.zeroVector(inputSize);
            		trajectoryAccelerationContext = this.zeroVector(inputSize);
            		trajectoryRecall = 0.0;
            		trajectoryNovelty = 1.0;
            		trajectoryWriteScore = 0.0;
            		trajectoryLoss = 0.0;
            		trajectoryIndices = Array.new;
            		trajectoryWeights = Array.new;
            		memoryContext =
            		this.zeroVector(
            			inputSize
            		);
            		memoryWeights =
            		Array.new;
            		memorySimilarity =
            		Array.new;
            		protectionScalar = 0.0;
            		"V30.0 Learning+ Dynamic Trajectory Memory memory reset".postln;
            		nil;

        }.value;
    }

    resetAll {
        ^{
            		// ------------------------------------------------------------
            		// INPUT
            		// ------------------------------------------------------------
            		inputProjection =
            		this.makeMatrix(
            			hiddenSize,
            			inputSize,
            			0.08
            		);
            		inputBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		// ------------------------------------------------------------
            		// OUTPUT
            		// ------------------------------------------------------------
            		outputProjection =
            		this.makeMatrix(
            			outputSize,
            			hiddenSize,
            			0.06
            		);
            		outputBias =
            		this.zeroVector(
            			outputSize
            		);
            		// ------------------------------------------------------------
            		// ATTENTION
            		// ------------------------------------------------------------
            		qWeights =
            		Array.fill(
            			numHeads,
            			{
            				this.makeMatrix(
            					headSize,
            					inputSize,
            					0.08
            				);
            			}
            		);
            		kWeights =
            		Array.fill(
            			numHeads,
            			{
            				this.makeMatrix(
            					headSize,
            					inputSize,
            					0.08
            				);
            			}
            		);
            		vWeights =
            		Array.fill(
            			numHeads,
            			{
            				this.makeMatrix(
            					headSize,
            					inputSize,
            					0.08
            				);
            			}
            		);
            		qBias =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		kBias =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		vBias =
            		Array.fill(
            			numHeads,
            			{
            				this.zeroVector(
            					headSize
            				);
            			}
            		);
            		outputAttentionWeight =
            		this.makeMatrix(
            			hiddenSize,
            			numHeads
            			*
            			headSize,
            			0.05
            		);
            		outputAttentionBias =
            		this.zeroVector(
            			hiddenSize
            		);
            		// ------------------------------------------------------------
            		// FEATURE GATES
            		// ------------------------------------------------------------
            		featureGateLogits =
            		Array.fill(
            			numHeads,
            			{
            				Array.fill(
            					inputSize,
            					0.0
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// ROUTER
            		// ------------------------------------------------------------
            		routerWeights =
            		this.makeMatrix(
            			numExperts,
            			hiddenSize,
            			0.05
            		);
            		routerBias =
            		this.zeroVector(
            			numExperts
            		);
            		// ------------------------------------------------------------
            		// EXPERTS
            		// ------------------------------------------------------------
            		expertW1 =
            		Array.fill(
            			numExperts,
            			{
            				this.makeMatrix(
            					hiddenSize,
            					hiddenSize,
            					0.05
            				);
            			}
            		);
            		expertB1 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		expertW2 =
            		Array.fill(
            			numExperts,
            			{
            				this.makeMatrix(
            					hiddenSize,
            					hiddenSize,
            					0.05
            				);
            			}
            		);
            		expertB2 =
            		Array.fill(
            			numExperts,
            			{
            				this.zeroVector(
            					hiddenSize
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// POSITION
            		// ------------------------------------------------------------
            		positionBias =
            		Array.fill(
            			numHeads,
            			{
            				Array.fill(
            					windowSize,
            					0.0
            				);
            			}
            		);
            		// ------------------------------------------------------------
            		// ADAM state is allocated by resetLearning -> resetOptimizerState.
            		// GENERATION DIVERSITY STATE
            		// ------------------------------------------------------------
            		generationHistory = List.new;
            		generationDiversity = 0.0;
            		generationNovelty = 1.0;
            		generationMinDistance = 1.0;
            		generationMeanDistance = 0.0;
            		generationPressure = 0.0;
            		localDiversity = 0.0;
            		localContractionPressure = 0.0;
            		localContractionCorrection = 0.0;
            		// ------------------------------------------------------------
            		// MEMORY
            		// ------------------------------------------------------------
            		this.resetMemory;
            		// ------------------------------------------------------------
            		// LEARNING STATE
            		// ------------------------------------------------------------
            		this.resetLearning;
            		"V30.0 Learning+ Dynamic Trajectory Memory full reset".postln;
            		nil;

        }.value;
    }

    // Silent diagnostic snapshot for GUIs, OSC monitors and polling.
    // Unlike status, this method never writes to the Post window.
    statusSilent {
        var attentionSum, entropyValue;
        var safeImportance, safeAge, safeUsage;

        attentionSum = attentionProfile.sum.max(0.000000001);
        entropyValue = attentionProfile.collect({ |p|
            var q;
            q = (p / attentionSum).max(0.000000001);
            q * q.log.neg;
        }).sum;
        entropy = entropyValue;

        safeImportance = if(memoryCount > 0, {
            longMemoryImportance.copyRange(0, memoryCount - 1);
        }, {
            Array.new;
        });
        safeAge = if(memoryCount > 0, {
            longMemoryAge.copyRange(0, memoryCount - 1);
        }, {
            Array.new;
        });
        safeUsage = if(memoryCount > 0, {
            longMemoryUsage.copyRange(0, memoryCount - 1);
        }, {
            Array.new;
        });

        ^(
            version: 30.0,
            parameters: parameterCount,
            inputSize: inputSize,
            outputSize: outputSize,
            hiddenSize: hiddenSize,
            numHeads: numHeads,
            headSize: headSize,
            numExperts: numExperts,
            windowSize: windowSize,
            longMemorySize: longMemorySize,
            learningRate: learningRateCurrent,
            loss: loss,
            outputLoss: outputLoss,
            deltaLoss: deltaLoss,
            directionLoss: directionLoss,
            expertBalanceLoss: expertBalanceLoss,
            errorEMA: errorEMA,
            surprise: surprise,
            surpriseEMA: surpriseEMA,
            memoryCount: memoryCount,
            replayCount: replayCount,
            memoryRecall: memoryRecall,
            memoryNovelty: memoryNovelty,
            memoryWriteScore: memoryWriteScore,
            replayLoss: replayLoss,
            interferenceScore: interferenceScore,
            trajectoryCount: trajectoryCount,
            trajectoryRecall: trajectoryRecall,
            trajectoryNovelty: trajectoryNovelty,
            trajectoryWriteScore: trajectoryWriteScore,
            trajectoryLoss: trajectoryLoss,
            trajectoryVelocityContext: trajectoryVelocityContext.copy,
            trajectoryAccelerationContext: trajectoryAccelerationContext.copy,
            protectionScalar: protectionScalar,
            entropy: entropy,
            expertUsage: expertUsage.copy,
            expertUsageEMA: expertUsageEMA.copy,
            routerEntropyEMA: routerEntropyEMA,
            attentionProfile: attentionProfile.copy,
            headActivity: headActivity.copy,
            headFeatureUsage: headFeatureUsage.collect({ |item| item.copy }),
            memoryImportance: safeImportance,
            memoryAge: safeAge,
            memoryUsage: safeUsage,
            generationDiversity: generationDiversity,
            generationNovelty: generationNovelty,
            generationMinDistance: generationMinDistance,
            generationMeanDistance: generationMeanDistance,
            generationPressure: generationPressure,
            localDiversity: localDiversity,
            localContractionPressure: localContractionPressure,
            localContractionCorrection: localContractionCorrection,
            autoTuneEnabled: autoTuneEnabled,
            autoTuneAdjustmentCount: autoTuneAdjustmentCount,
            autoTuneNoveltyEMA: autoTuneNoveltyEMA,
            autoTuneDiversityEMA: autoTuneDiversityEMA,
            autoTuneLastError: autoTuneLastError,
            metaLearnEnabled: metaLearnEnabled,
            metaLearnAdjustmentCount: metaLearnAdjustmentCount,
            metaLearnPlasticityPressure: metaLearnPlasticityPressure,
            metaLearnStabilityPressure: metaLearnStabilityPressure,
            learnedEvents: learnedEvents,
            adamStep: adamStep,
            totalEvents: totalEvents,
            prediction: lastPrediction.copy,
            delta: lastDelta.copy
        );
    }
    status {
        ^{
            		var attentionSum;
            		var entropyValue;
            		attentionSum =
            		attentionProfile.sum.max(
            			0.000000001
            		);
            		entropyValue =
            		attentionProfile.collect({ |p|
            			var q;
            			q =
            			(
            				p
            				/
            				attentionSum
            			).max(
            				0.000000001
            			);
            			q
            			*
            			q.log.neg;
            		}).sum;
            		entropy =
            		entropyValue;
            		"".postln;
            		"============================================================".postln;
            		"TRANSFORMER VECTOR V30.0 Learning+ Dynamic Trajectory Memory".postln;
            		"MEMORY / LEARNING+ / ADAPTATION / ANTI-INTERFERENCE".postln;
            		"============================================================".postln;
            		("parameters: " ++ parameterCount).postln;
            		("inputSize: " ++ inputSize).postln;
            		("outputSize: " ++ outputSize).postln;
            		("hiddenSize: " ++ hiddenSize).postln;
            		("numHeads: " ++ numHeads).postln;
            		("headSize: " ++ headSize).postln;
            		("numExperts: " ++ numExperts).postln;
            		("windowSize: " ++ windowSize).postln;
            		("longMemorySize: " ++ longMemorySize).postln;
            		("memoryRecallSize: " ++ memoryRecallSize).postln;
            		("baseLearningRate: " ++ learningRate).postln;
            		("adaptiveLearningRate: " ++ learningRateCurrent).postln;
            		("adaptationFastRate: " ++ adaptationFastRate).postln;
            		("adaptationSlowRate: " ++ adaptationSlowRate).postln;
            		("protectionScalar: " ++ protectionScalar).postln;
            		("learnedEvents: " ++ learnedEvents).postln;
            		("adamStep: " ++ adamStep).postln;
            		("totalEvents: " ++ totalEvents).postln;
            		("loss: " ++ loss).postln;
            		("outputLoss: " ++ outputLoss).postln;
            		("deltaLoss: " ++ deltaLoss).postln;
            		("directionLoss: " ++ directionLoss).postln;
            		("expertBalanceLoss: " ++ expertBalanceLoss).postln;
            		("errorEMA: " ++ errorEMA).postln;
            		("surprise: " ++ surprise).postln;
            		("surpriseEMA: " ++ surpriseEMA).postln;
            		("memoryCount: " ++ memoryCount).postln;
            		("replayCount: " ++ replayCount).postln;
            		("memoryRecall: " ++ memoryRecall).postln;
            		("memoryNovelty: " ++ memoryNovelty).postln;
            		("memoryWriteScore: " ++ memoryWriteScore).postln;
            		("trajectoryCount: " ++ trajectoryCount).postln;
            		("trajectoryRecall: " ++ trajectoryRecall).postln;
            		("trajectoryNovelty: " ++ trajectoryNovelty).postln;
            		("trajectoryWriteScore: " ++ trajectoryWriteScore).postln;
            		("trajectoryLoss: " ++ trajectoryLoss).postln;
            		"trajectoryVelocityContext:".postln;
            		trajectoryVelocityContext.postln;
            		"trajectoryAccelerationContext:".postln;
            		trajectoryAccelerationContext.postln;
            		("replayLoss: " ++ replayLoss).postln;
            		("interferenceScore: " ++ interferenceScore).postln;
                ("autoTuneEnabled: " ++ autoTuneEnabled).postln;
                ("autoTuneAdjustmentCount: " ++ autoTuneAdjustmentCount).postln;
                ("autoTuneNoveltyEMA: " ++ autoTuneNoveltyEMA).postln;
                ("autoTuneDiversityEMA: " ++ autoTuneDiversityEMA).postln;
                ("autoTuneLastError: " ++ autoTuneLastError).postln;
                ("metaLearnEnabled: " ++ metaLearnEnabled).postln;
                ("metaLearnAdjustmentCount: " ++ metaLearnAdjustmentCount).postln;
                ("metaLearnPlasticityPressure: " ++ metaLearnPlasticityPressure).postln;
                ("metaLearnStabilityPressure: " ++ metaLearnStabilityPressure).postln;
            		("entropy: " ++ entropy).postln;
            		"expertUsage:".postln;
            		expertUsage.postln;
            		"memoryImportance:".postln;
            		longMemoryImportance.copyRange(
            			0,
            			memoryCount - 1
            		).postln;
            		"memoryAge:".postln;
            		longMemoryAge.copyRange(
            			0,
            			memoryCount - 1
            		).postln;
            		"memoryUsage:".postln;
            		longMemoryUsage.copyRange(
            			0,
            			memoryCount - 1
            		).postln;
            		"attentionProfile:".postln;
            		attentionProfile.postln;
            		"headActivity:".postln;
            		headActivity.postln;
            		"headFeatureUsage:".postln;
            		headFeatureUsage.postln;
            		"prediction:".postln;
            		lastPrediction.postln;
            		"delta:".postln;
            		lastDelta.postln;
            		"============================================================".postln;
            		(
            			version: 30.0,
            			parameters: parameterCount,
            			inputSize: inputSize,
            			outputSize: outputSize,
            			hiddenSize: hiddenSize,
            			numHeads: numHeads,
            			headSize: headSize,
            			numExperts: numExperts,
            			windowSize: windowSize,
            			longMemorySize: longMemorySize,
            			learningRate:
            			learningRateCurrent,
            			loss: loss,
            			outputLoss: outputLoss,
            			deltaLoss: deltaLoss,
            			directionLoss: directionLoss,
            			expertBalanceLoss: expertBalanceLoss,
            			errorEMA: errorEMA,
            			surprise: surprise,
            			surpriseEMA: surpriseEMA,
            			memoryCount: memoryCount,
            			replayCount: replayCount,
            			memoryRecall: memoryRecall,
            			memoryNovelty: memoryNovelty,
            			memoryWriteScore: memoryWriteScore,
            			replayLoss: replayLoss,
            			interferenceScore:
            			interferenceScore,
            			trajectoryCount: trajectoryCount,
            			trajectoryRecall: trajectoryRecall,
            			trajectoryNovelty: trajectoryNovelty,
            			trajectoryWriteScore: trajectoryWriteScore,
            			trajectoryLoss: trajectoryLoss,
            			protectionScalar:
            			protectionScalar,
            			entropy: entropy,
            			expertUsage:
            			expertUsage.copy,
            			attentionProfile:
            			attentionProfile.copy,
            			headActivity:
            			headActivity.copy,
            			headFeatureUsage:
            			headFeatureUsage.collect({ |x|
            				x.copy;
            			}),
            			memoryImportance:
            			longMemoryImportance.copy,
            			memoryAge:
            			longMemoryAge.copy,
            			memoryUsage:
            			longMemoryUsage.copy,
            			learnedEvents:
            			learnedEvents,
            			adamStep:
            			adamStep,
            			prediction:
            			lastPrediction.copy,
            			delta:
            			lastDelta.copy
            		);

        }.value;
    }

    config {
        ^{
            		(
            			version: 30.0,
            			inputSize: inputSize,
            			outputSize: outputSize,
            			windowSize: windowSize,
            			longMemorySize: longMemorySize,
            			hiddenSize: hiddenSize,
            			numHeads: numHeads,
            			headSize: headSize,
            			numExperts: numExperts,
            			learningRate: learningRate,
            			beta1: beta1,
            			beta2: beta2,
            			epsilon: epsilon,
            			temperature: temperature,
            			residualScale:
            			residualScale,
            			expertScale:
            			expertScale,
            			deltaScale:
            			deltaScale,
            			surpriseThreshold:
            			surpriseThreshold,
            			surpriseGain:
            			surpriseGain,
            			gateLearningRate:
            			gateLearningRate,
            			gradientClip:
            			gradientClip,
            			memoryWriteThreshold:
            			memoryWriteThreshold,
            			memoryRetrievalGain:
            			memoryRetrievalGain,
            			memoryRetrievalTemperature:
            			memoryRetrievalTemperature,
            			memoryDecay:
            			memoryDecay,
            			memoryConsolidationRate:
            			memoryConsolidationRate,
            			replayRate:
            			replayRate,
            			replayBatchSize:
            			replayBatchSize,
            			memoryRecallSize:
            			memoryRecallSize,
            			protectionStrength:
            			protectionStrength,
            			adaptationFastRate:
            			adaptationFastRate,
            			adaptationSlowRate:
            			adaptationSlowRate,
            			predictionLossWeight: predictionLossWeight,
            			deltaLossWeight: deltaLossWeight,
            			directionLossWeight: directionLossWeight,
            			replayPriorityMix: replayPriorityMix,
            			replayUniformMix: replayUniformMix,
            			expertBalanceStrength: expertBalanceStrength,
            			headSpecializationThreshold: headSpecializationThreshold,
            			headSpecializationStrength: headSpecializationStrength,
            			interferenceTestSteps: interferenceTestSteps,
            			trajectoryMemorySize: trajectoryMemorySize,
            			trajectoryRecallSize: trajectoryRecallSize,
            			trajectoryRetrievalGain: trajectoryRetrievalGain,
            			trajectoryVelocityGain: trajectoryVelocityGain,
            			trajectoryAccelerationGain: trajectoryAccelerationGain,
            			trajectoryVelocityClip: trajectoryVelocityClip,
            			trajectoryAccelerationClip: trajectoryAccelerationClip,
            			trajectoryDecay: trajectoryDecay,
            			trajectoryUsageDecay: trajectoryUsageDecay,
            			trajectoryNoveltyWeight: trajectoryNoveltyWeight,
            			trajectoryInputWeight: trajectoryInputWeight,
            			trajectoryVelocityWeight: trajectoryVelocityWeight,
            			trajectoryExplorationGain: trajectoryExplorationGain,
            			diversityHistorySize: diversityHistorySize,
            			diversityRadius: diversityRadius,
            			diversityRepulsionGain: diversityRepulsionGain,
            			diversityNoiseGain: diversityNoiseGain,
            			diversityMaxCorrection: diversityMaxCorrection,
            			diversityAdaptiveGain: diversityAdaptiveGain,
            			localDiversityWindow: localDiversityWindow,
            			localDiversityFloor: localDiversityFloor,
            			localDiversityGain: localDiversityGain,
            			localDiversityMaxCorrection: localDiversityMaxCorrection,
            			parameters:
            			parameterCount
            		);

        }.value;
    }

    generalizationTest { |input, target|
        ^{
            		var p;
            		var l;
            		p =
            		this.predict(
            			input
            		);
            		l =
            		this.maskedMSE(
            			p,
            			this.cleanOutputVector(
            				target
            			)
            		);
            		(
            			generalizationLoss:
            			l,
            			prediction:
            			p.copy,
            			target:
            			this.cleanOutputVector(
            				target
            			)
            		);

        }.value;
    }

    memoryTest { |input|
        ^{
            		var x;
            		var result;
            		x =
            		this.cleanVector(
            			input
            		);
            		result =
            		this.retrieveMemory(
            			x
            		);
            		(
            			memoryCount:
            			memoryCount,
            			recallCount:
            			result[\indices].size,
            			indices:
            			result[\indices].copy,
            			weights:
            			result[\weights].copy,
            			similarities:
            			memorySimilarity.copy,
            			context:
            			result[\context].copy,
            			novelty:
            			this.calculateMemoryNovelty(
            				x
            			)
            		);

        }.value;
    }

    // Non-destructive interference proxy. No optimizer state or weight is changed.
    interferenceTest { |input, target|
        var state, y, before, steps, totalReplayLoss, idx, replayState, replayTarget;
        y = this.cleanOutputVector(target);
        state = this.buildForward(input);
        before = this.maskedMSE(state[\prediction], y);
        steps = interferenceTestSteps.min(replayCount);
        totalReplayLoss = 0.0;
        steps.do({ |i|
            idx = ((i * 1103515245) + adamStep).abs % replayCount.max(1);
            replayTarget = this.cleanOutputVector(replayTargets[idx]);
            replayState = this.buildForward(replayInputs[idx]);
            totalReplayLoss = totalReplayLoss + this.maskedMSE(replayState[\prediction], replayTarget);
        });
        interferenceScore = if(steps > 0, { ((totalReplayLoss / steps) - before) / (before + 0.000001) }, { 0.0 });
        ^(lossBefore: before, lossAfter: before, replayConflict: interferenceScore,
            interferenceScore: interferenceScore, probeSteps: steps, destructive: false,
            prediction: state[\prediction].copy, target: y.copy);
    }
    parameters { ^parameterCount; }
    generationDiversity { ^generationDiversity; }
    generationNovelty { ^generationNovelty; }
    memoryCountValue { ^memoryCount; }
    replayCountValue { ^replayCount; }
    trajectoryCountValue { ^trajectoryCount; }
    saveArchive { |path|
        var finalPath;
        finalPath = path.asString.standardizePath;
        this.writeArchive(finalPath);
        ("HPtransformerRT V30.0 saved: " ++ finalPath).postln;
        ^finalPath;
    }
    *readArchive { |path|
        var finalPath, model;
        finalPath = path.asString.standardizePath;
        if(File.exists(finalPath).not, { Error("Transformer archive not found: " ++ finalPath).throw; });
        model = Object.readArchive(finalPath);
        if(model.isKindOf(HPtransformerRT).not, { Error("Archive is not an HPtransformerRT: " ++ finalPath).throw; });
        ("HPtransformerRT V30.0 restored: " ++ finalPath).postln;
        ^model;
    }

    // ============================================================
    // TARGETED INFERENCE SNAPSHOTS
    // No optimizer, replay, gradients or learning history copied.
    // ============================================================

    copySnapshotValue { |value|
        if(value.isNil, { ^nil });
        if(value.isArray, {
            ^value.collect({ |item| this.copySnapshotValue(item) });
        });
        if(value.isKindOf(List), {
            ^value.collect({ |item| this.copySnapshotValue(item) }).as(List);
        });
        if(value.isKindOf(IdentityDictionary) or: { value.isKindOf(Event) }, {
            var result;
            result = IdentityDictionary.new;
            value.keysValuesDo({ |key, item|
                result[key] = this.copySnapshotValue(item);
            });
            ^result;
        });
        ^value;
    }

    exportInferenceSnapshot {
        ^(
            snapshotVersion: 1,
            structure: (
                inputSize: inputSize,
                outputSize: outputSize,
                windowSize: windowSize,
                longMemorySize: longMemorySize,
                hiddenSize: hiddenSize,
                numHeads: numHeads,
                headSize: headSize,
                numExperts: numExperts,
                trajectoryMemorySize: trajectoryMemorySize
            ),
            weights: (
                inputProjection: this.copySnapshotValue(inputProjection),
                inputBias: this.copySnapshotValue(inputBias),
                outputProjection: this.copySnapshotValue(outputProjection),
                outputBias: this.copySnapshotValue(outputBias),
                qWeights: this.copySnapshotValue(qWeights),
                kWeights: this.copySnapshotValue(kWeights),
                vWeights: this.copySnapshotValue(vWeights),
                qBias: this.copySnapshotValue(qBias),
                kBias: this.copySnapshotValue(kBias),
                vBias: this.copySnapshotValue(vBias),
                outputAttentionWeight: this.copySnapshotValue(outputAttentionWeight),
                outputAttentionBias: this.copySnapshotValue(outputAttentionBias),
                featureGateLogits: this.copySnapshotValue(featureGateLogits),
                routerWeights: this.copySnapshotValue(routerWeights),
                routerBias: this.copySnapshotValue(routerBias),
                expertW1: this.copySnapshotValue(expertW1),
                expertB1: this.copySnapshotValue(expertB1),
                expertW2: this.copySnapshotValue(expertW2),
                expertB2: this.copySnapshotValue(expertB2),
                positionBias: this.copySnapshotValue(positionBias)
            ),
            memory: (
                memoryCount: memoryCount,
                longMemory: this.copySnapshotValue(longMemory),
                longMemoryImportance: this.copySnapshotValue(longMemoryImportance),
                longMemoryAge: this.copySnapshotValue(longMemoryAge),
                longMemoryUsage: this.copySnapshotValue(longMemoryUsage),
                longMemorySurprise: this.copySnapshotValue(longMemorySurprise),
                trajectoryCount: trajectoryCount,
                trajectoryMemory: this.copySnapshotValue(trajectoryMemory)
            ),
            parameters: this.runtimeConfig
        );
    }

    loadInferenceSnapshot { |snapshot|
        var structure, weights, memory, settings;
        if(snapshot.isNil, { Error("Inference snapshot is nil").throw });
        if(snapshot[\snapshotVersion] != 1, {
            Error("Unsupported inference snapshot version").throw;
        });
        structure = snapshot[\structure];
        if(structure[\inputSize] != inputSize, { Error("Snapshot inputSize mismatch").throw });
        if(structure[\outputSize] != outputSize, { Error("Snapshot outputSize mismatch").throw });
        if(structure[\windowSize] != windowSize, { Error("Snapshot windowSize mismatch").throw });
        if(structure[\hiddenSize] != hiddenSize, { Error("Snapshot hiddenSize mismatch").throw });
        if(structure[\numHeads] != numHeads, { Error("Snapshot numHeads mismatch").throw });
        if(structure[\headSize] != headSize, { Error("Snapshot headSize mismatch").throw });
        if(structure[\numExperts] != numExperts, { Error("Snapshot numExperts mismatch").throw });

        weights = snapshot[\weights];
        inputProjection = weights[\inputProjection];
        inputBias = weights[\inputBias];
        outputProjection = weights[\outputProjection];
        outputBias = weights[\outputBias];
        qWeights = weights[\qWeights];
        kWeights = weights[\kWeights];
        vWeights = weights[\vWeights];
        qBias = weights[\qBias];
        kBias = weights[\kBias];
        vBias = weights[\vBias];
        outputAttentionWeight = weights[\outputAttentionWeight];
        outputAttentionBias = weights[\outputAttentionBias];
        featureGateLogits = weights[\featureGateLogits];
        routerWeights = weights[\routerWeights];
        routerBias = weights[\routerBias];
        expertW1 = weights[\expertW1];
        expertB1 = weights[\expertB1];
        expertW2 = weights[\expertW2];
        expertB2 = weights[\expertB2];
        positionBias = weights[\positionBias];

        memory = snapshot[\memory];
        memoryCount = memory[\memoryCount].asInteger.clip(0, longMemorySize);
        longMemory = memory[\longMemory];
        longMemoryImportance = memory[\longMemoryImportance];
        longMemoryAge = memory[\longMemoryAge];
        longMemoryUsage = memory[\longMemoryUsage];
        longMemorySurprise = memory[\longMemorySurprise];
        trajectoryCount = memory[\trajectoryCount].asInteger.clip(0, trajectoryMemorySize);
        trajectoryMemory = memory[\trajectoryMemory].as(List);

        settings = snapshot[\parameters];
        settings.keysValuesDo({ |key, value|
            if(key != \torusMask or: { value.notNil }, {
                this.setParameter(key, value, false);
            });
        });
        ^this;
    }

    exportGenerationState {
        ^(
            shortMemory: this.copySnapshotValue(shortMemory),
            generationHistory: this.copySnapshotValue(generationHistory),
            generationDriftState: this.copySnapshotValue(generationDriftState),
            lastObservedVelocity: this.copySnapshotValue(lastObservedVelocity),
            lastInput: this.copySnapshotValue(lastInput),
            lastPrediction: this.copySnapshotValue(lastPrediction),
            lastDelta: this.copySnapshotValue(lastDelta),
            generationDiversity: generationDiversity,
            generationNovelty: generationNovelty,
            generationMinDistance: generationMinDistance,
            generationMeanDistance: generationMeanDistance,
            generationPressure: generationPressure,
            localDiversity: localDiversity,
            localContractionPressure: localContractionPressure,
            localContractionCorrection: localContractionCorrection,
            autoTuneCounter: autoTuneCounter,
            autoTuneNoveltyEMA: autoTuneNoveltyEMA,
            autoTuneDiversityEMA: autoTuneDiversityEMA,
            autoTuneLastError: autoTuneLastError,
            autoTuneAdjustmentCount: autoTuneAdjustmentCount
        );
    }

    loadGenerationState { |state|
        if(state.isNil, { ^this });
        shortMemory = state[\shortMemory].as(List);
        generationHistory = state[\generationHistory].as(List);
        generationDriftState = state[\generationDriftState];
        lastObservedVelocity = state[\lastObservedVelocity];
        lastInput = state[\lastInput];
        lastPrediction = state[\lastPrediction];
        lastDelta = state[\lastDelta];
        generationDiversity = state[\generationDiversity];
        generationNovelty = state[\generationNovelty];
        generationMinDistance = state[\generationMinDistance];
        generationMeanDistance = state[\generationMeanDistance];
        generationPressure = state[\generationPressure];
        localDiversity = state[\localDiversity];
        localContractionPressure = state[\localContractionPressure];
        localContractionCorrection = state[\localContractionCorrection];
        autoTuneCounter = state[\autoTuneCounter];
        autoTuneNoveltyEMA = state[\autoTuneNoveltyEMA];
        autoTuneDiversityEMA = state[\autoTuneDiversityEMA];
        autoTuneLastError = state[\autoTuneLastError];
        autoTuneAdjustmentCount = state[\autoTuneAdjustmentCount];
        ^this;
    }


    // ============================================================
    // UNIFIED SINGLE-CLASS RCU API
    // One public HPtransformerRT object, two internal HPtransformerRT runtimes.
    // No second class and no full-object copy.
    // ============================================================

    markUnifiedRuntimeOnly {
        unifiedRuntimeRole = true;
        unifiedCurrentRuntime = nil;
        unifiedPendingRuntime = nil;
        ^this;
    }

    makeUnifiedRuntime {
        var cfg, runtime;
        cfg = this.config;
        runtime = HPtransformerRT.new(
            inputSize: cfg[\inputSize],
            outputSize: cfg[\outputSize],
            windowSize: cfg[\windowSize],
            longMemorySize: cfg[\longMemorySize],
            hiddenSize: cfg[\hiddenSize],
            numHeads: cfg[\numHeads],
            headSize: cfg[\headSize],
            numExperts: cfg[\numExperts],
            trajectoryMemorySize: trajectoryMemorySize,
            torusMask: torusMask
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
            unifiedTrainingVersion = 0;
            unifiedPublishedVersion = 0;
            unifiedSnapshotPending = false;
            unifiedLearningEnabled = true;
            unifiedGenerationEnabled = true;
        });
        ^this;
    }

    disableUnifiedRCU {
        unifiedCurrentRuntime = nil;
        unifiedPendingRuntime = nil;
        unifiedSnapshotPending = false;
        ^this;
    }

    unifiedRCUEnabled {
        ^unifiedCurrentRuntime.notNil;
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

    publishNow {
        this.prepareSnapshot;
        ^this.commitSnapshot;
    }

    discardPendingSnapshot {
        unifiedSnapshotPending = false;
        ^this;
    }

    trainingVersion { ^unifiedTrainingVersion; }
    publishedVersion { ^unifiedPublishedVersion; }
    snapshotPending { ^unifiedSnapshotPending; }

    enableLearning { unifiedLearningEnabled = true; ^true; }
    disableLearning { unifiedLearningEnabled = false; ^false; }
    enableGeneration { unifiedGenerationEnabled = true; ^true; }
    disableGeneration { unifiedGenerationEnabled = false; ^false; }

    // Shared, persistent parameter update.
    setParameter { |name, value, post=true|
        var result;
        result = this.setParameterLocal(name, value, post);
        if(unifiedCurrentRuntime.notNil and: { unifiedRuntimeRole.not }, {
            unifiedCurrentRuntime.setParameterLocal(name, value, false);
            unifiedPendingRuntime.setParameterLocal(name, value, false);
        });
        ^result;
    }

    setTrainingParameter { |name, value, post=true|
        ^this.setParameterLocal(name, value, post);
    }

    setTrainingParameters { |settings, post=true|
        settings.keysValuesDo({ |key, value|
            this.setParameterLocal(key, value, post);
        });
        ^this.runtimeConfig;
    }

    setRuntimeParameter { |name, value, post=true|
        this.enableUnifiedRCU;
        ^unifiedCurrentRuntime.setParameterLocal(name, value, post);
    }

    setRuntimeParameters { |settings, post=true|
        this.enableUnifiedRCU;
        settings.keysValuesDo({ |key, value|
            unifiedCurrentRuntime.setParameterLocal(key, value, post);
        });
        ^unifiedCurrentRuntime.runtimeConfig;
    }

    unifiedRuntimeStatus {
        this.enableUnifiedRCU;
        ^unifiedCurrentRuntime.status;
    }

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
        if(routine.notNil, {
            routine.stop;
            unifiedParameterMorphs.removeAt(key);
        });
        ^this;
    }

    stopAllMorphs {
        unifiedParameterMorphs.keysValuesDo({ |key, routine|
            if(routine.notNil, { routine.stop });
        });
        unifiedParameterMorphs.clear;
        ^this;
    }

    morphParameter { |name, target, duration=1.0, steps=50, clock|
        var key, start, count, routine;
        key = name.asSymbol;
        start = this.getParameter(key);
        count = steps.asInteger.max(1);
        if(start.isNumber.not or: { target.isNumber.not }, {
            Error("morphSharedParameter requires a numeric parameter").throw;
        });
        this.stopMorph(key);
        routine = Routine({
            count.do({ |index|
                this.setParameter(
                    key,
                    start + ((target.asFloat - start) * ((index + 1) / count)),
                    false
                );
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
        if(start.isNumber.not or: { target.isNumber.not }, {
            Error("morphRuntimeParameter requires a numeric parameter").throw;
        });
        this.stopMorph(morphKey);
        routine = Routine({
            count.do({ |index|
                this.setRuntimeParameter(
                    key,
                    start + ((target.asFloat - start) * ((index + 1) / count)),
                    false
                );
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
        if(start.isNumber.not or: { target.isNumber.not }, {
            Error("morphTrainingParameter requires a numeric parameter").throw;
        });
        this.stopMorph(morphKey);
        routine = Routine({
            count.do({ |index|
                this.setTrainingParameter(
                    key,
                    start + ((target.asFloat - start) * ((index + 1) / count)),
                    false
                );
                (duration.asFloat.max(0.0) / count).wait;
            });
            unifiedParameterMorphs.removeAt(morphKey);
        });
        unifiedParameterMorphs[morphKey] = routine;
        routine.play(clock ? AppClock);
        ^routine;
    }

}
