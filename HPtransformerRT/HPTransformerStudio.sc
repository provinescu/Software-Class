/*
HPTransformerStudio V8.1.1 for HPtransformerRT V30.0 - SuperCollider 3.14
High-contrast GUI update: white labels on dark panels, black text on white fields.
New preset families: focused attention, specialized experts, precise trajectory, prudent RT V30.
*/

HPTransformerStudio : Object {
    var <transformer, <window, <refreshRoutine;

    *new { |transformer|
        ^super.new.init(transformer)
    }

    init { |aTransformer|
        transformer = aTransformer;
        if(transformer.isNil) {
            transformer = HPtransformerRT.new;
        };
        ^this
    }

    front {
        if(window.notNil) {
            window.front;
            ^this
        };
        this.build;
        ^this
    }

    close {
        if(window.notNil) {
            window.close;
        };
        ^this
    }

    isOpen {
        ^window.notNil
    }

    build {
        // HPTransformer Studio Pro V8.1.1 CONFIGURATION SETS - HPtransformerRT V30.0 - SuperCollider 3.14
        // Fenetre unique : Dashboard, Controls, Generation, Graphs, Heatmaps,
        // Generation, Memoire, Surprise, AutoTune et MetaLearn Presets, Snapshots RCU, Logs.
        var w, pages, pageButtons, activePage, routine, running=true, rate=0.35;
        var uiScale=0.86, uiRect, scrollView, uiRoot;
        var graphDrawScale=0.82, graphRect, heatDrawScale=0.82, heatPoint, heatRect;
        var statusText, graphView, heatView, autoText, metaText, rcuText, logView;
        var genPresetText, memoryPresetText, surprisePresetText;
        var generationLiveText, memoryLiveText, surpriseLiveText;
        var oscStatusText, oscEventsView, oscHostField, oscInPortBox, oscOutPortBox;
        var oscInputPathField, oscOutputPathField, oscModeMenu, oscLearnButton, oscSendButton;
        var oscRunning=false, oscLearning=true, oscSendOutput=true, oscDef=nil, oscTarget=nil;
        var oscMode=0, oscOutputPath="/hptransformer/output";
        var oscInCount=0, oscOutCount=0, oscErrorCount=0, oscInRate=0.0, oscOutRate=0.0;
        var oscLastInCount=0, oscLastOutCount=0, oscLastRateTime, oscLastInput=nil, oscLastOutput=nil;
        var oscEventLines=List.new, oscStart, oscStop, oscRecordEvent, oscUpdateStatus;
        var logs=List.new, loss=List.new, surprise=List.new, entropy=List.new;
        var memRecall=List.new, trajRecall=List.new, lastStatus, widgets=IdentityDictionary.new;
        var lossEMA=List.new,surpriseEMAPlot=List.new,entropyEMA=List.new,memRecallEMA=List.new,trajRecallEMA=List.new;
        var graphSmoothAlpha=0.10,lossScaleMode=1,metricScaleMode=1;
        var lossScaleMenu,metricScaleMenu,smoothingBox,addGraphSample,clearGraphs;
        var showPage, addLog, addSlider, button, title, trim, refresh, drawLine;
        var styleButton, buttonColorForText, navIdleColor, navActiveColor;
        var exportCSV, exportLogs, exportHeat, loadSettings, savePreset, loadPreset;
        var generalNames, generalPresets, autoNames, autoPresets, metaNames, metaPresets;
        var diagnosticPreset, safeReturnPreset, oscCalibrationPreset, lowCpuPreset;
        var genNames, genPresets, memoryNames, memoryPresets, surpriseNames, surprisePresets;
        var setNames, setPresets, setDescriptions, setMenu, setDescriptionView;
        var applyConfigurationSet, exportCurrentSet;
        var generalMenu, autoMenu, metaMenu, seedField, countBox, outputView;
        var genMenu, memoryMenu, surpriseMenu;
        var torusMaskField, torusMaskStatus, applyTorusMask, formatTorusMask;
        var morphParamMenu,morphParamNames,morphParamSymbols,morphTargetBox,morphDurationBox,morphStepsBox,morphScopeMenu,morphStatus;
        var refreshMorphParameterMenu,selectedMorphParameter;
        var startMorph,stopSelectedMorph,refreshMorphStatus;
        var manualParamsEditor, manualParamsStatus;
        var applyManualParameters, loadRuntimeIntoEditor, formatRuntimeConfig;
        var saveCompleteSession, loadCompleteSession, restoreGuiFromSession;
        
        oscLastRateTime=Main.elapsedTime;
        // Mode compact pour ecran 13 pouces. Toutes les vues Qt utilisent la meme echelle.
        uiRect={|x,y,width,height| Rect(x*uiScale,y*uiScale,width*uiScale,height*uiScale)};
        // Les dessins Pen utilisent les coordonnees locales du UserView et doivent etre mis a l'echelle separement.
        graphRect={|x,y,width,height| Rect(x*graphDrawScale,y*graphDrawScale,width*graphDrawScale,height*graphDrawScale)};
        heatPoint={|x,y| Point(x*heatDrawScale,y*heatDrawScale)};
        heatRect={|x,y,width,height| Rect(x*heatDrawScale,y*heatDrawScale,width*heatDrawScale,height*heatDrawScale)};
        
        addLog={|msg| var x=Date.localtime.stamp++"  "++msg; logs.add(x);
         while({logs.size>800},{logs.removeAt(0)}); if(logView.notNil,{logView.string_(logs.join(Char.nl))}) };
        title={|p,text,x,y,wid=500| StaticText(p,uiRect.(x,y,wid,24)).string_(text)
         .font_(Font.default.boldVariant.size_(15)).stringColor_(Color.cyan(0.85)) };
        navIdleColor=Color.grey(0.24);
        navActiveColor=Color.blue(0.58);
        buttonColorForText={|text|
         var lower=text.asString.toLower;
         if(lower.contains("off") or:{lower.contains("arreter")} or:{lower.contains("desactiver")},{Color.red(0.52)},{
          if(lower.contains("on") or:{lower.contains("demarrer")} or:{lower.contains("activer")},{Color.green(0.42)},{
           if(lower.contains("reset") or:{lower.contains("discard")} or:{lower.contains("effacer")},{Color(0.82,0.37,0.12)},{
            if(lower.contains("export") or:{lower.contains("sauver")} or:{lower.contains("charger")},{Color(0.38,0.25,0.70)},{
             if(lower.contains("publish") or:{lower.contains("commit")} or:{lower.contains("prepare")},{Color(0.08,0.55,0.62)},{Color.blue(0.42)})
            })
           })
          })
         })
        };
        styleButton={|view,text,color|
         view.states_([[text,Color.white,color ? buttonColorForText.(text)]]);
         view.font_(Font.default.boldVariant.size_(11));
         view
        };
        button={|p,text,x,y,wid=170,fun| var view;
         view=Button(p,uiRect.(x,y,wid,34)); styleButton.(view,text,nil); view.action_(fun); view };
        
        addSlider={|p,label,param,spec,x,y,wid=550,decimals=6| var z=EZSlider(p,wid@28,label,spec,{|v|
         transformer.setParameter(param,v.value,false); addLog.(param.asString++" = "++v.value.asString)},
         transformer.getParameter(param),false,145,75);
         z.labelView.stringColor_(Color.white);
         z.numberView.background_(Color.white).stringColor_(Color.black).decimals_(decimals);
         z.sliderView.background_(Color.grey(0.28));
         z.view.bounds_(uiRect.(x,y,wid,28)); widgets[param]=z; z };
        trim={|list| while({list.size>900},{list.removeAt(0)}) };
        loadSettings={|settings,label| settings.keysValuesDo({|k,v| if(transformer.isRuntimeParameter(k),{
         transformer.setParameter(k,v,false)})}); widgets.keysValuesDo({|k,v| if(v.isKindOf(EZSlider),{
         v.value_(transformer.getParameter(k))})}); addLog.("Preset applique: "++label) };
        formatTorusMask={|mask|if(mask.isNil,{""},{mask.collect({|item|if(item==true,{"1"},{"0"})}).join(",")})};
        applyTorusMask={|text|var expectedSize,parts,mask;
         expectedSize=(transformer.config[\outputSize]?1).asInteger.max(1);
         parts=text.asString.split($,).collect({|item|item.stripWhiteSpace.toLower});
         if(parts.size!=expectedSize,{
          torusMaskStatus.string_("Erreur : "++expectedSize++" valeurs requises.");
          addLog.("ERREUR torusMask: "++expectedSize++" valeurs requises")
         },{
          mask=parts.collect({|item|(item=="1")or:{item=="true"}or:{item=="on"}or:{item=="oui"}});
          transformer.setParameter(\torusMask,mask,false);
          torusMaskField.string_(formatTorusMask.(transformer.getParameter(\torusMask)));
          torusMaskStatus.string_("Masque actif : "++transformer.getParameter(\torusMask).asCompileString++Char.nl++"1 = torique ; 0 = lineaire");
          addLog.("torusMask applique: "++mask.asCompileString)
         })
        };
        
        // Presets operationnels
        diagnosticPreset=(learningRate:0.00010,protectionStrength:0.40,replayRate:0.0,memoryRetrievalGain:0.0,memoryRecallSize:1,trajectoryRetrievalGain:0.0,trajectoryRecallSize:1,trajectoryExplorationGain:0.0,attentionTemperature:0.50,routerTemperature:1.10,deltaScale:0.16,diversityNoiseGain:0.0,diversityRepulsionGain:0.0,generationWindowSize:4,autoTuneEnabled:false,metaLearnEnabled:false);
        safeReturnPreset=(learningRate:0.00016,gradientClip:0.35,surpriseThreshold:0.10,surpriseGain:1.0,protectionStrength:0.38,replayRate:0.08,replayBatchSize:1,memoryRetrievalGain:0.16,memoryWriteThreshold:0.12,memoryRecallSize:2,trajectoryRetrievalGain:0.07,trajectoryRecallSize:1,trajectoryExplorationGain:0.001,attentionTemperature:0.82,routerTemperature:1.10,deltaScale:0.20,diversityNoiseGain:0.00005,diversityRepulsionGain:0.0006,generationWindowSize:6,autoTuneEnabled:false,metaLearnEnabled:false);
        oscCalibrationPreset=(learningRate:0.00010,protectionStrength:0.40,replayRate:0.0,memoryRetrievalGain:0.04,memoryRecallSize:1,trajectoryRetrievalGain:0.02,trajectoryRecallSize:1,trajectoryExplorationGain:0.0,attentionTemperature:0.55,routerTemperature:1.10,deltaScale:0.12,diversityNoiseGain:0.0,diversityRepulsionGain:0.0,generationWindowSize:2,autoTuneEnabled:false,metaLearnEnabled:false);
        lowCpuPreset=(learningRate:0.00012,gradientClip:0.35,protectionStrength:0.28,replayRate:0.0,replayBatchSize:1,memoryRetrievalGain:0.06,memoryRecallSize:1,trajectoryRetrievalGain:0.03,trajectoryRecallSize:1,trajectoryExplorationGain:0.0005,attentionTemperature:0.75,routerTemperature:1.10,deltaScale:0.18,diversityNoiseGain:0.0,diversityRepulsionGain:0.0002,generationWindowSize:2,autoTuneEnabled:false,metaLearnEnabled:false);
        // Presets generaux supplementaires
        generalNames=["Equilibre","Apprentissage rapide","Stable","Exploration","Memoire forte",
         "Conservateur","Creatif","Replay intensif","Faible latence","Torus doux","Convergence fine",
         "Attention focalisee","Experts specialises","Trajectoire precise","RT V30 prudent"];
        generalPresets=[
         (learningRate:0.00035,attentionTemperature:1.0,routerTemperature:1.10,replayRate:0.08,protectionStrength:0.16,memoryRetrievalGain:0.10,trajectoryExplorationGain:0.0045,diversityNoiseGain:0.00035),
         (learningRate:0.0008,surpriseGain:1.6,replayRate:0.12,protectionStrength:0.10,gradientClip:0.75),
         (learningRate:0.00018,replayRate:0.18,protectionStrength:0.35,attentionTemperature:0.85,routerTemperature:1.10,diversityNoiseGain:0.0001),
         (attentionTemperature:1.5,routerTemperature:1.10,trajectoryExplorationGain:0.015,diversityNoiseGain:0.003,diversityRepulsionGain:0.008,diversityAdaptiveGain:1.2),
         (memoryRetrievalGain:0.25,memoryWriteThreshold:0.06,memoryRecallSize:5,replayRate:0.18,protectionStrength:0.28),
         (learningRate:0.00010,replayRate:0.22,protectionStrength:0.48,gradientClip:0.35),
         (attentionTemperature:1.8,routerTemperature:1.10,trajectoryExplorationGain:0.022,diversityNoiseGain:0.006,localDiversityGain:0.12),
         (replayRate:0.30,replayBatchSize:4,replayPriorityMix:0.82,protectionStrength:0.32),
         (memoryRecallSize:1,trajectoryRecallSize:1,replayBatchSize:1,generationWindowSize:4),
         (attentionTemperature:0.95,routerTemperature:1.10,deltaScale:0.20,diversityNoiseGain:0.00015,trajectoryExplorationGain:0.002),
         (learningRate:0.00008,gradientClip:0.20,replayRate:0.12,protectionStrength:0.40),
         (attentionTemperature:0.55,routerTemperature:1.15,trajectoryRetrievalTemperature:0.30,residualScale:0.34,expertScale:0.24,diversityNoiseGain:0.00010),
         (attentionTemperature:0.90,routerTemperature:0.65,trajectoryRetrievalTemperature:0.30,expertScale:0.32,expertBalanceStrength:0.004,headSpecializationStrength:0.004),
         (attentionTemperature:0.90,routerTemperature:1.10,trajectoryRetrievalTemperature:0.16,trajectoryRecallSize:2,trajectoryRetrievalGain:0.14,trajectoryVelocityGain:0.070,trajectoryAccelerationGain:0.012),
         (learningRate:0.00025,gradientClip:0.60,attentionTemperature:0.85,routerTemperature:1.10,trajectoryRetrievalTemperature:0.25,residualScale:0.30,expertScale:0.22,deltaScale:0.25,replayRate:0.08,protectionStrength:0.18,trajectoryExplorationGain:0.0025,diversityNoiseGain:0.00020,diversityRepulsionGain:0.0012,autoTuneEnabled:false,metaLearnEnabled:false)
        ];
        autoNames=["Auto Equilibre","Auto Diversite","Auto Nouveaute","Auto Doux","Auto Reactif","Auto Minimal"];
        autoPresets=[
         (autoTuneEnabled:true,autoTuneInterval:8,autoTuneStrength:0.20,autoTuneSmoothing:0.90,autoTuneTargetNovelty:0.18,autoTuneTargetDiversity:0.12),
         (autoTuneEnabled:true,autoTuneInterval:6,autoTuneStrength:0.30,autoTuneTargetNovelty:0.16,autoTuneTargetDiversity:0.24,autoTuneNoiseMax:0.015),
         (autoTuneEnabled:true,autoTuneInterval:6,autoTuneStrength:0.32,autoTuneTargetNovelty:0.30,autoTuneTargetDiversity:0.14,autoTuneTemperatureMax:3.2),
         (autoTuneEnabled:true,autoTuneInterval:16,autoTuneStrength:0.08,autoTuneSmoothing:0.97,autoTuneNoiseMax:0.003),
         (autoTuneEnabled:true,autoTuneInterval:2,autoTuneStrength:0.45,autoTuneSmoothing:0.75,autoTuneExplorationMax:0.045),
         (autoTuneEnabled:false,autoTuneStrength:0.0,diversityNoiseGain:0.0,trajectoryExplorationGain:0.0005)
        ];
        metaNames=["Meta Equilibre","Meta Plasticite","Meta Stabilite","Meta Memoire","Meta Anti-interference","Meta Lent"];
        metaPresets=[
         (metaLearnEnabled:true,metaLearnInterval:16,metaLearnStrength:0.12,metaLearnSmoothing:0.95,metaLearnTargetError:0.015,metaLearnTargetRecall:0.50),
         (metaLearnEnabled:true,metaLearnInterval:8,metaLearnStrength:0.25,metaLearnTargetError:0.025,metaLearnTargetSurprise:0.14,metaLearnProtectionMin:0.03),
         (metaLearnEnabled:true,metaLearnInterval:16,metaLearnStrength:0.18,metaLearnTargetError:0.010,metaLearnProtectionMin:0.20,metaLearnProtectionMax:0.80),
         (metaLearnEnabled:true,metaLearnInterval:12,metaLearnStrength:0.18,metaLearnTargetRecall:0.72,metaLearnRetrievalGainMax:0.50,replayRate:0.16),
         (metaLearnEnabled:true,metaLearnInterval:8,metaLearnStrength:0.22,metaLearnTargetInterference:-0.05,metaLearnReplayRateMax:0.45,metaLearnProtectionMax:1.0),
         (metaLearnEnabled:true,metaLearnInterval:32,metaLearnStrength:0.05,metaLearnSmoothing:0.985)
        ];
        
        
        // Presets specialises Generation
        genNames=["Generation equilibree","Generation creative","Generation stable","Generation expansive",
         "Generation minimaliste","Generation dynamique","Generation memoire","Generation torique"];
        genPresets=[
         (attentionTemperature:1.0,routerTemperature:1.10,deltaScale:0.30,trajectoryExplorationGain:0.0045,diversityNoiseGain:0.00035,diversityRepulsionGain:0.0016,localDiversityGain:0.055),
         (attentionTemperature:1.75,routerTemperature:1.10,deltaScale:0.42,trajectoryExplorationGain:0.020,diversityNoiseGain:0.005,diversityRepulsionGain:0.010,diversityAdaptiveGain:1.4),
         (attentionTemperature:0.72,routerTemperature:1.10,deltaScale:0.18,trajectoryExplorationGain:0.001,diversityNoiseGain:0.00005,diversityRepulsionGain:0.0008,localDiversityGain:0.025),
         (attentionTemperature:1.30,routerTemperature:1.10,deltaScale:0.55,trajectoryExplorationGain:0.012,diversityRadius:0.035,diversityMaxCorrection:0.050),
         (attentionTemperature:0.60,routerTemperature:1.10,deltaScale:0.12,trajectoryExplorationGain:0.0005,diversityNoiseGain:0.0,diversityRepulsionGain:0.0002),
         (attentionTemperature:1.20,routerTemperature:1.10,deltaScale:0.38,trajectoryVelocityGain:0.090,trajectoryAccelerationGain:0.018,trajectoryExplorationGain:0.008),
         (attentionTemperature:0.95,routerTemperature:1.10,memoryRetrievalGain:0.28,trajectoryRetrievalGain:0.16,generationWindowSize:8,diversityNoiseGain:0.0002),
         (attentionTemperature:1.05,routerTemperature:1.10,deltaScale:0.28,diversityRadius:0.020,trajectoryExplorationGain:0.006,localDiversityGain:0.070)
        ];
        // Presets specialises Memoire
        memoryNames=["Memoire equilibree","Memoire profonde","Memoire selective","Memoire rapide",
         "Replay fort","Anti-oubli","Trajectoire forte","Memoire legere"];
        memoryPresets=[
         (memoryWriteThreshold:0.10,memoryRetrievalGain:0.10,memoryRetrievalTemperature:0.30,trajectoryRetrievalTemperature:0.30,memoryDecay:0.996,memoryRecallSize:3,replayRate:0.08),
         (memoryWriteThreshold:0.04,memoryRetrievalGain:0.32,memoryRetrievalTemperature:0.18,trajectoryRetrievalTemperature:0.22,memoryDecay:0.999,memoryRecallSize:6,replayRate:0.18),
         (memoryWriteThreshold:0.24,memoryRetrievalGain:0.18,memoryRetrievalTemperature:0.12,memoryRecallSize:2,replayRate:0.06),
         (memoryWriteThreshold:0.08,memoryRetrievalGain:0.14,memoryRetrievalTemperature:0.55,memoryRecallSize:2,replayRate:0.04),
         (replayRate:0.32,replayBatchSize:4,replayPriorityMix:0.86,replayUniformMix:0.14,protectionStrength:0.32),
         (memoryDecay:0.9995,memoryUsageDecay:0.9998,memoryConsolidationRate:0.008,protectionStrength:0.48,replayRate:0.22),
         (trajectoryRecallSize:3,trajectoryRetrievalGain:0.24,trajectoryVelocityGain:0.10,trajectoryAccelerationGain:0.02,trajectoryDecay:0.985),
         (memoryWriteThreshold:0.20,memoryRetrievalGain:0.05,memoryRecallSize:1,replayRate:0.02,protectionStrength:0.08)
        ];
        // Presets specialises Surprise / plasticite
        surpriseNames=["Surprise equilibree","Tres plastique","Prudent","Evenements rares",
         "Adaptation rapide","Adaptation lente","Anti-interference","Exploration surprise"];
        surprisePresets=[
         (surpriseThreshold:0.06,surpriseGain:1.10,adaptationFastRate:1.35,adaptationSlowRate:0.40,protectionStrength:0.16),
         (surpriseThreshold:0.025,surpriseGain:2.30,adaptationFastRate:2.40,adaptationSlowRate:0.65,learningRate:0.00065,protectionStrength:0.08),
         (surpriseThreshold:0.16,surpriseGain:1.05,adaptationFastRate:0.90,adaptationSlowRate:0.22,protectionStrength:0.42),
         (surpriseThreshold:0.30,surpriseGain:2.80,adaptationFastRate:2.10,adaptationSlowRate:0.18,memoryWriteThreshold:0.18),
         (surpriseThreshold:0.04,surpriseGain:1.85,adaptationFastRate:2.00,adaptationSlowRate:0.55,gradientClip:1.10),
         (surpriseThreshold:0.10,surpriseGain:1.08,adaptationFastRate:0.75,adaptationSlowRate:0.15,learningRate:0.00012),
         (surpriseThreshold:0.08,surpriseGain:1.20,adaptationFastRate:1.05,adaptationSlowRate:0.28,protectionStrength:0.55,replayRate:0.24),
         (surpriseThreshold:0.045,surpriseGain:1.70,adaptationFastRate:1.70,trajectoryExplorationGain:0.014,diversityNoiseGain:0.002)
        ];
        
        // Jeux complets coordonnes : apprentissage + generation + memoire + surprise + AutoTune + MetaLearn.
        setNames=[
         "Studio Equilibre","Apprentissage Continu","Performance Stable","Improvisation Creative",
         "Memoire Narrative","Adaptation Rapide","Anti-Oubli Fort","Exploration Torique",
         "Faible Latence OSC","Installation Autonome","Analyse / Prediction","Generation Pure"
        ];
        setDescriptions=[
         "Point de depart polyvalent. Apprentissage modere, generation equilibree, AutoTune doux et MetaLearn prudent.",
         "Pour un flux continu qui doit etre appris progressivement tout en conservant une generation stable.",
         "Priorite a la stabilite, a la protection et au replay. Exploration et bruit faibles.",
         "Generation variee avec temperature, exploration, diversite et AutoTune plus reactifs.",
         "Favorise la memoire longue, le rappel, la trajectoire et la coherence sur des sequences etendues.",
         "Reagit vite aux changements de dynamique. Plasticite et surprise plus fortes, protection moderee.",
         "Protection, replay et consolidation eleves pour limiter l'interference et l'oubli catastrophique.",
         "Toutes les sorties sont toriques. Convient aux phases, angles et parametres cycliques normalises.",
         "Reduit le travail par evenement pour privilegier le debit OSC et la reactivite de l'interface.",
         "AutoTune et MetaLearn actifs avec des valeurs douces pour un fonctionnement autonome de longue duree.",
         "Apprentissage suspendu, faible exploration et generation deterministe pour evaluer la prediction.",
         "Generation active sans apprentissage externe. Diversite moderee et memoire utilisee pour la continuite."
        ];
        setPresets=[
         (learningRate:0.00035,gradientClip:0.75,surpriseThreshold:0.06,surpriseGain:1.10,protectionStrength:0.16,replayRate:0.08,memoryRetrievalGain:0.10,memoryWriteThreshold:0.10,trajectoryRetrievalGain:0.07,trajectoryExplorationGain:0.0045,attentionTemperature:1.0,routerTemperature:1.10,diversityNoiseGain:0.00035,diversityRepulsionGain:0.0016,autoTuneEnabled:true,autoTuneInterval:12,autoTuneStrength:0.10,metaLearnEnabled:true,metaLearnInterval:24,metaLearnStrength:0.06),
         (learningRate:0.00040,gradientClip:0.70,surpriseThreshold:0.055,surpriseGain:1.25,adaptationFastRate:1.50,adaptationSlowRate:0.42,protectionStrength:0.20,replayRate:0.12,memoryRetrievalGain:0.14,memoryWriteThreshold:0.08,trajectoryRetrievalGain:0.09,attentionTemperature:1.0,routerTemperature:1.10,diversityNoiseGain:0.0003,autoTuneEnabled:true,autoTuneInterval:12,autoTuneStrength:0.10,metaLearnEnabled:true,metaLearnInterval:20,metaLearnStrength:0.08),
         (learningRate:0.00016,gradientClip:0.35,surpriseThreshold:0.12,surpriseGain:1.02,protectionStrength:0.48,replayRate:0.24,memoryRetrievalGain:0.20,memoryWriteThreshold:0.10,memoryDecay:0.999,memoryUsageDecay:0.9995,trajectoryExplorationGain:0.001,attentionTemperature:0.75,routerTemperature:1.10,diversityNoiseGain:0.00005,autoTuneEnabled:false,metaLearnEnabled:true,metaLearnInterval:28,metaLearnStrength:0.05),
         (learningRate:0.00030,surpriseThreshold:0.045,surpriseGain:1.45,protectionStrength:0.12,replayRate:0.06,memoryRetrievalGain:0.09,trajectoryRetrievalGain:0.10,trajectoryExplorationGain:0.020,attentionTemperature:1.70,routerTemperature:1.10,deltaScale:0.42,diversityNoiseGain:0.005,diversityRepulsionGain:0.010,diversityAdaptiveGain:1.40,localDiversityGain:0.12,autoTuneEnabled:true,autoTuneInterval:4,autoTuneStrength:0.32,autoTuneTargetNovelty:0.28,autoTuneTargetDiversity:0.24,metaLearnEnabled:false),
         (learningRate:0.00028,protectionStrength:0.32,replayRate:0.20,replayBatchSize:3,memoryRetrievalGain:0.32,memoryWriteThreshold:0.05,memoryRetrievalTemperature:0.16,trajectoryRetrievalTemperature:0.22,memoryDecay:0.9995,memoryConsolidationRate:0.04,memoryUsageDecay:0.9997,memoryRecallSize:6,trajectoryRecallSize:3,trajectoryRetrievalGain:0.24,trajectoryVelocityGain:0.09,attentionTemperature:0.90,routerTemperature:1.10,diversityNoiseGain:0.00015,autoTuneEnabled:false,metaLearnEnabled:true,metaLearnInterval:16,metaLearnStrength:0.12,metaLearnTargetRecall:0.70),
         (learningRate:0.00072,gradientClip:1.00,surpriseThreshold:0.025,surpriseGain:2.20,adaptationFastRate:2.30,adaptationSlowRate:0.62,protectionStrength:0.10,replayRate:0.10,memoryWriteThreshold:0.07,trajectoryExplorationGain:0.010,attentionTemperature:1.15,routerTemperature:1.10,autoTuneEnabled:true,autoTuneInterval:5,autoTuneStrength:0.20,metaLearnEnabled:true,metaLearnInterval:8,metaLearnStrength:0.22,metaLearnTargetError:0.025),
         (learningRate:0.00014,gradientClip:0.30,surpriseThreshold:0.09,surpriseGain:1.10,adaptationFastRate:0.95,adaptationSlowRate:0.20,protectionStrength:0.72,replayRate:0.34,replayBatchSize:4,replayPriorityMix:0.90,replayUniformMix:0.10,memoryRetrievalGain:0.28,memoryWriteThreshold:0.06,memoryDecay:0.9997,memoryConsolidationRate:0.06,attentionTemperature:0.80,routerTemperature:1.10,autoTuneEnabled:false,metaLearnEnabled:true,metaLearnInterval:10,metaLearnStrength:0.18,metaLearnProtectionMax:1.2,metaLearnReplayRateMax:0.50),
         (learningRate:0.00032,attentionTemperature:1.25,routerTemperature:1.10,deltaScale:0.34,trajectoryRetrievalGain:0.12,trajectoryExplorationGain:0.014,diversityRadius:0.025,diversityNoiseGain:0.002,diversityRepulsionGain:0.007,localDiversityGain:0.09,autoTuneEnabled:true,autoTuneInterval:6,autoTuneStrength:0.25,autoTuneTargetNovelty:0.24,autoTuneTargetDiversity:0.20,metaLearnEnabled:false),
         (learningRate:0.00025,gradientClip:0.55,surpriseThreshold:0.07,surpriseGain:1.10,protectionStrength:0.14,replayRate:0.025,replayBatchSize:1,memoryRecallSize:1,memoryRetrievalGain:0.06,trajectoryRecallSize:1,trajectoryRetrievalGain:0.04,generationWindowSize:4,trajectoryExplorationGain:0.002,attentionTemperature:0.95,routerTemperature:1.10,diversityNoiseGain:0.0001,autoTuneEnabled:false,metaLearnEnabled:false),
         (learningRate:0.00030,gradientClip:0.65,surpriseThreshold:0.065,surpriseGain:1.20,protectionStrength:0.26,replayRate:0.16,memoryRetrievalGain:0.18,memoryWriteThreshold:0.08,trajectoryRetrievalGain:0.10,trajectoryExplorationGain:0.006,attentionTemperature:1.05,routerTemperature:1.10,diversityNoiseGain:0.0006,autoTuneEnabled:true,autoTuneInterval:12,autoTuneStrength:0.12,autoTuneSmoothing:0.96,metaLearnEnabled:true,metaLearnInterval:24,metaLearnStrength:0.08,metaLearnSmoothing:0.98),
         (learningRate:0.00010,gradientClip:0.25,protectionStrength:0.40,replayRate:0.0,memoryRetrievalGain:0.16,trajectoryRetrievalGain:0.08,trajectoryExplorationGain:0.0,attentionTemperature:0.55,routerTemperature:1.10,diversityNoiseGain:0.0,diversityRepulsionGain:0.0,autoTuneEnabled:false,metaLearnEnabled:false),
         (learningRate:0.00020,protectionStrength:0.24,replayRate:0.0,memoryRetrievalGain:0.22,trajectoryRetrievalGain:0.16,trajectoryVelocityGain:0.08,trajectoryExplorationGain:0.009,attentionTemperature:1.25,routerTemperature:1.10,deltaScale:0.36,diversityNoiseGain:0.0015,diversityRepulsionGain:0.005,localDiversityGain:0.08,autoTuneEnabled:true,autoTuneInterval:8,autoTuneStrength:0.18,metaLearnEnabled:false)
        ];
        
        applyConfigurationSet={|index|
         var settings,name,size;
         settings=setPresets[index];name=setNames[index];
         loadSettings.(settings,name);
         // Reglages d'etat qui ne sont pas de simples parametres.
         if(index==10,{transformer.disableLearning;transformer.enableGeneration});
         if(index==11,{transformer.disableLearning;transformer.enableGeneration});
         if(index!=10 and:{index!=11},{transformer.enableLearning;transformer.enableGeneration});
         // Le jeu torique adapte automatiquement le masque a outputSize.
         if(index==7,{
          size=(transformer.config[\outputSize]?1).asInteger.max(1);
          transformer.setParameter(\torusMask,Array.fill(size,{true}),false);
          torusMaskField.string_(formatTorusMask.(transformer.getParameter(\torusMask)));
         });
         setDescriptionView.string_(
          "Configuration active : "++name++Char.nl++Char.nl++
          setDescriptions[index]++Char.nl++Char.nl++
          "Parametres appliques :"++Char.nl++settings.asCompileString
         );
         addLog.("Jeu complet applique: "++name)
        };
        
        exportCurrentSet={
         Dialog.savePanel({|path|var fp,data;if(path.notNil,{
          fp=if(path.endsWith(".hptset"),{path},{path++".hptset"});
          data=(name:"Configuration utilisateur",parameters:transformer.runtimeConfig,torusMask:transformer.getParameter(\torusMask),learningEnabled:transformer.unifiedRCUStatus[\learningEnabled],generationEnabled:transformer.unifiedRCUStatus[\generationEnabled]);
          data.writeArchive(fp);addLog.("Jeu complet exporte: "++fp)
         })})
        };
        
        w=Window("HPTransformer Studio Pro V8.1.1 - Compact 13 pouces scrollable - graphes, heatmaps et menu morphing final",uiRect.(35,35,1320,860)).background_(Color.grey(0.13));
        scrollView=ScrollView(w,uiRect.(0,0,1320,860)).hasBorder_(false).autohidesScrollers_(true);
        // CompositeView explicite recommande pour fixer une surface de contenu plus grande.
        uiRoot=CompositeView(scrollView,uiRect.(0,0,1360,980)).background_(Color.grey(0.13));
        StaticText(uiRoot,uiRect.(5,965,5,5)).string_("");
        pages=IdentityDictionary.new; pageButtons=IdentityDictionary.new;
        [
         [\dashboard,"Dashboard"],[\controls,"Controls"],[\manualParams,"Parametres manuels"],[\morphing,"Morphing"],[\generation,"Generation Live"],[\memoryLive,"Memoire Live"],
         [\surpriseLive,"Surprise Live"],[\osc,"OSC / Temps reel"],[\sets,"Jeux complets"],[\graphs,"Graphes"],
         [\heatmaps,"Heatmaps"],[\genPresets,"Generation Presets"],[\memoryPresets,"Memoire Presets"],
         [\surprisePresets,"Surprise Presets"],[\auto,"AutoTune Presets"],[\meta,"MetaLearn Presets"],
         [\rcu,"Snapshots RCU"],[\logs,"Logs"]
        ].do({|pair,i| var b,p; b=Button(uiRoot,uiRect.(10+(i%5*260),8+(i.div(5)*34),250,28));
         styleButton.(b,pair[1],navIdleColor);
         b.action_({showPage.(pair[0])}); pageButtons[pair[0]]=b;
         p=CompositeView(uiRoot,uiRect.(0,150,1320,780)).background_(Color.grey(0.17)).visible_(false); pages[pair[0]]=p });
        showPage={|name| activePage=name; pages.keysValuesDo({|k,p|p.visible_(k==name)});
         pageButtons.keysValuesDo({|k,b| var label=b.states[0][0]; styleButton.(b,label,if(k==name,{navActiveColor},{navIdleColor}))});
         if(name==\graphs,{graphView.refresh}); if(name==\heatmaps,{heatView.refresh}) };
        
        // Dashboard
        title.(pages[\dashboard],"ETAT GENERAL",20,15); statusText=TextView(pages[\dashboard],uiRect.(20,50,760,620))
         .editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        title.(pages[\dashboard],"PRESETS GENERAUX",820,15); generalMenu=PopUpMenu(pages[\dashboard],uiRect.(820,50,450,30)).items_(generalNames);
        button.(pages[\dashboard],"Appliquer",820,95,210,{loadSettings.(generalPresets[generalMenu.value],generalNames[generalMenu.value])});
        button.(pages[\dashboard],"Sauver preset...",1060,95,210,{savePreset.value});
        button.(pages[\dashboard],"Charger preset...",820,140,210,{loadPreset.value});
        button.(pages[\dashboard],"Reset Learning",1060,140,210,{transformer.resetLearning;addLog.("Reset Learning")});
        button.(pages[\dashboard],"Reset Memory",820,185,210,{transformer.resetMemory;addLog.("Reset Memory")});
        button.(pages[\dashboard],"Reset All",1060,185,210,{transformer.resetAll;addLog.("Reset All")});
        button.(pages[\dashboard],"Learning ON",820,250,210,{transformer.enableLearning;addLog.("Learning ON")});
        button.(pages[\dashboard],"Learning OFF",1060,250,210,{transformer.disableLearning;addLog.("Learning OFF")});
        button.(pages[\dashboard],"Generation ON",820,295,210,{transformer.enableGeneration;addLog.("Generation ON")});
        button.(pages[\dashboard],"Generation OFF",1060,295,210,{transformer.disableGeneration;addLog.("Generation OFF")});
        button.(pages[\dashboard],"Diagnostic neutre",820,350,210,{transformer.stopAllMorphs;transformer.disableAutoTune;transformer.disableMetaLearning;loadSettings.(diagnosticPreset,"Diagnostic neutre");transformer.disableLearning;transformer.enableGeneration});
        button.(pages[\dashboard],"Retour securise",1060,350,210,{transformer.stopAllMorphs;transformer.disableAutoTune;transformer.disableMetaLearning;loadSettings.(safeReturnPreset,"Retour securise");transformer.enableLearning;transformer.enableGeneration;rate=0.35});
        button.(pages[\dashboard],"Calibration OSC",820,395,210,{transformer.stopAllMorphs;transformer.disableAutoTune;transformer.disableMetaLearning;loadSettings.(oscCalibrationPreset,"Calibration OSC");transformer.disableLearning;transformer.enableGeneration});
        button.(pages[\dashboard],"Faible charge CPU",1060,395,210,{transformer.stopAllMorphs;transformer.disableAutoTune;transformer.disableMetaLearning;loadSettings.(lowCpuPreset,"Faible charge CPU");transformer.disableLearning;transformer.enableGeneration;rate=0.75});
        
        // Controls
        title.(pages[\controls],"APPRENTISSAGE",20,15); title.(pages[\controls],"MEMOIRE ET GENERATION",680,15);
        addSlider.(pages[\controls],"Learning rate",\learningRate,ControlSpec(0.00001,0.005,\exp),20,50,590);
        addSlider.(pages[\controls],"Gradient clip",\gradientClip,ControlSpec(0.01,5,\exp),20,88,590);
        addSlider.(pages[\controls],"Surprise threshold",\surpriseThreshold,ControlSpec(0,1,\lin),20,126,590);
        addSlider.(pages[\controls],"Surprise gain",\surpriseGain,ControlSpec(0,5,\lin),20,164,590);
        addSlider.(pages[\controls],"Protection",\protectionStrength,ControlSpec(0,2,\lin),20,202,590);
        addSlider.(pages[\controls],"Replay rate",\replayRate,ControlSpec(0,0.5,\lin),20,240,590);
        addSlider.(pages[\controls],"Memory gain",\memoryRetrievalGain,ControlSpec(0,1,\lin),680,50,600);
        addSlider.(pages[\controls],"Write threshold",\memoryWriteThreshold,ControlSpec(0,1,\lin),680,88,600);
        addSlider.(pages[\controls],"Attention temperature",\attentionTemperature,ControlSpec(0.1,5,\exp),680,126,600);
        addSlider.(pages[\controls],"Router temperature",\routerTemperature,ControlSpec(0.1,5,\exp),680,164,600);
        addSlider.(pages[\controls],"Trajectory temperature",\trajectoryRetrievalTemperature,ControlSpec(0.05,2,\exp),680,202,600);
        addSlider.(pages[\controls],"Trajectory retrieval",\trajectoryRetrievalGain,ControlSpec(0,1,\lin),680,240,600);
        addSlider.(pages[\controls],"Exploration",\trajectoryExplorationGain,ControlSpec(0,0.05,\lin),680,278,600);
        addSlider.(pages[\controls],"Diversity noise",\diversityNoiseGain,ControlSpec(0,0.02,\lin),680,316,600);
        addSlider.(pages[\controls],"Diversity repulsion",\diversityRepulsionGain,ControlSpec(0,0.05,\lin),680,354,600);
        // TorusMask par dimension de sortie.
        title.(pages[\controls],"TORUS MASK",20,410,400);
        StaticText(pages[\controls],uiRect.(20,450,245,24)).string_("Masque 1/0 separe par virgules :").stringColor_(Color.white);
        torusMaskField=TextField(pages[\controls],uiRect.(270,447,430,30)).string_(formatTorusMask.(transformer.getParameter(\torusMask))).background_(Color.white).stringColor_(Color.black).action_({|field|applyTorusMask.(field.string)});
        button.(pages[\controls],"Appliquer TorusMask",720,445,190,{applyTorusMask.(torusMaskField.string)});
        button.(pages[\controls],"Tout torique",925,445,150,{var n,m;n=(transformer.config[\outputSize]?1).asInteger.max(1);m=Array.fill(n,{true});torusMaskField.string_(formatTorusMask.(m));applyTorusMask.(torusMaskField.string)});
        button.(pages[\controls],"Tout lineaire",1090,445,170,{var n,m;n=(transformer.config[\outputSize]?1).asInteger.max(1);m=Array.fill(n,{false});torusMaskField.string_(formatTorusMask.(m));applyTorusMask.(torusMaskField.string)});
        torusMaskStatus=TextView(pages[\controls],uiRect.(20,495,1240,145)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black).font_(Font.default.size_(12));
        torusMaskStatus.string_("Masque actif : "++transformer.getParameter(\torusMask).asCompileString++Char.nl++"1 = dimension torique (bouclage 0..1) ; 0 = dimension lineaire (limitee 0..1)");
        
        // Parametres manuels
        // Syntaxe volontairement restreinte: nom = nombre|true|false.
        // Aucun code arbitraire n'est interprete par cette page.
        formatRuntimeConfig={
         transformer.runtimeParameterNames.collect({|key|
          key.asString++" = "++transformer.getParameter(key).asCompileString
         }).join(Char.nl)
        };
        loadRuntimeIntoEditor={
         manualParamsEditor.string_(formatRuntimeConfig.value);
         manualParamsStatus.string_("Configuration runtime chargee dans l'editeur. Modifiez uniquement les lignes necessaires.")
        };
        applyManualParameters={|text|
         var lines,applied=List.new,errors=List.new;
         lines=text.asString.split(Char.nl);
         lines.do({|rawLine,lineIndex|
          var line,separator,keyText,valueText,key,value,firstChar,validStart;
          line=rawLine.stripWhiteSpace;
          if(line.notEmpty and:{line.beginsWith("//").not} and:{line.beginsWith("#").not},{
           separator=line.find("=");
           if(separator.isNil,{
            errors.add("Ligne "++(lineIndex+1)++": separateur '=' manquant")
           },{
            keyText=line.copyRange(0,separator-1).stripWhiteSpace;
            valueText=line.copyRange(separator+1,line.size-1).stripWhiteSpace;
            key=keyText.asSymbol;
            if(transformer.isRuntimeParameter(key).not,{
             errors.add("Ligne "++(lineIndex+1)++": parametre inconnu "++keyText)
            },{
             value=if(valueText.toLower=="true",{true},{
              if(valueText.toLower=="false",{false},{
               firstChar=if(valueText.notEmpty,{valueText[0]},{nil});
               validStart=firstChar.notNil and:{firstChar.isDecDigit or:{firstChar==$-} or:{firstChar==$+} or:{firstChar==$.}};
               if(validStart,{valueText.asFloat},{nil})
              })
             });
             if(value.isNil,{
              errors.add("Ligne "++(lineIndex+1)++": valeur invalide "++valueText)
             },{
              {transformer.setParameter(key,value,false)}.try({|error|
               errors.add("Ligne "++(lineIndex+1)++": "++error.asString);nil
              });
              if(errors.detect({|item|item.beginsWith("Ligne "++(lineIndex+1)++":")}).isNil,{
               applied.add(key.asString++" = "++transformer.getParameter(key).asCompileString)
              })
             })
            })
           })
          })
         });
         widgets.keysValuesDo({|key,view|
          if(view.isKindOf(EZSlider),{view.value_(transformer.getParameter(key))})
         });
         manualParamsStatus.string_(
          "PARAMETRES APPLIQUES ("++applied.size++")"++Char.nl++
          if(applied.isEmpty,{"Aucun"},{applied.join(Char.nl)})++Char.nl++Char.nl++
          "ERREURS ("++errors.size++")"++Char.nl++
          if(errors.isEmpty,{"Aucune"},{errors.join(Char.nl)})
         );
         if(applied.notEmpty,{addLog.("Parametres manuels appliques: "++applied.join(", "))});
         if(errors.notEmpty,{addLog.("ERREURS parametres manuels: "++errors.join(" | "))});
         graphView.refresh;
        };
        // Sauvegarde/restauration complete du moteur et de l'etat operationnel de la GUI.
        // Les objets Qt, OSCdef, NetAddr et Routine ne sont pas archives directement.
        // Leur configuration est capturee puis reconstruite proprement.
        restoreGuiFromSession={|session|
         var guiState,graphState,logState,learningEnabled,generationEnabled,savedPage;
         guiState=session[\gui] ? ();
         graphState=session[\graphs] ? ();
         logState=session[\logs] ? ();
         oscHostField.string_(guiState[\oscHost] ? "127.0.0.1");
         oscInPortBox.value_((guiState[\oscInPort] ? 57120).asInteger);
         oscOutPortBox.value_((guiState[\oscOutPort] ? 57130).asInteger);
         oscInputPathField.string_(guiState[\oscInputPath] ? "/hptransformer/input");
         oscOutputPath=(guiState[\oscOutputPath] ? "/hptransformer/output").asString;
         oscOutputPathField.string_(oscOutputPath);
         oscMode=(guiState[\oscMode] ? 0).asInteger.clip(0,3);
         oscModeMenu.value_(oscMode);
         oscLearning=guiState[\oscLearning] ? true;
         oscSendOutput=guiState[\oscSendOutput] ? true;
         oscLearnButton.value_(if(oscLearning,{1},{0}));
         oscSendButton.value_(if(oscSendOutput,{1},{0}));
         rate=(guiState[\refreshRate] ? 0.35).asFloat.max(0.05);
         learningEnabled=session[\learningEnabled] ? true;
         generationEnabled=session[\generationEnabled] ? true;
         if(learningEnabled,{transformer.enableLearning},{transformer.disableLearning});
         if(generationEnabled,{transformer.enableGeneration},{transformer.disableGeneration});
         loss=(graphState[\loss] ? []).as(List);
         surprise=(graphState[\surprise] ? []).as(List);
         entropy=(graphState[\entropy] ? []).as(List);
         memRecall=(graphState[\memoryRecall] ? []).as(List);
         trajRecall=(graphState[\trajectoryRecall] ? []).as(List);
         logs=(logState[\main] ? []).as(List);
         oscEventLines=(logState[\oscEvents] ? []).as(List);
         logView.string_(logs.join(Char.nl));
         oscEventsView.string_(oscEventLines.join(Char.nl));
         widgets.keysValuesDo({|key,view|
          if(view.isKindOf(EZSlider),{view.value_(transformer.getParameter(key))})
         });
         torusMaskField.string_(formatTorusMask.(transformer.getParameter(\torusMask)));
         torusMaskStatus.string_(
          "Masque actif : "++transformer.getParameter(\torusMask).asCompileString++Char.nl++
          "1 = dimension torique (bouclage 0..1) ; 0 = dimension lineaire (limitee 0..1)"
         );
         loadRuntimeIntoEditor.value;
         savedPage=guiState[\activePage] ? \dashboard;
         if(pages[savedPage].isNil,{savedPage=\dashboard});
         showPage.(savedPage);
         graphView.refresh;
         heatView.refresh;
        };
        saveCompleteSession={
         Dialog.savePanel({|path|
          var fp,wasOscRunning,session,ok;
          if(path.notNil,{
           fp=if(path.endsWith(".hptsession"),{path},{path++".hptsession"});
           wasOscRunning=oscRunning;
           transformer.stopAllMorphs;
           if(wasOscRunning,{oscStop.value});
           session=(
            sessionVersion:1,
            savedAt:Date.localtime.stamp,
            transformer:transformer,
            learningEnabled:transformer.unifiedRCUStatus[\learningEnabled],
            generationEnabled:transformer.unifiedRCUStatus[\generationEnabled],
            gui:(
             activePage:activePage,
             refreshRate:rate,
             oscWasRunning:wasOscRunning,
             oscHost:oscHostField.string,
             oscInPort:oscInPortBox.value.asInteger,
             oscOutPort:oscOutPortBox.value.asInteger,
             oscInputPath:oscInputPathField.string,
             oscOutputPath:oscOutputPathField.string,
             oscMode:oscModeMenu.value,
             oscLearning:oscLearning,
             oscSendOutput:oscSendOutput
            ),
            graphs:(
             loss:loss.asArray,
             surprise:surprise.asArray,
             entropy:entropy.asArray,
             memoryRecall:memRecall.asArray,
             trajectoryRecall:trajRecall.asArray
            ),
            logs:(main:logs.asArray,oscEvents:oscEventLines.asArray)
           );
           ok={session.writeArchive(fp);true}.try({|error|
            manualParamsStatus.string_("Erreur sauvegarde session :"++Char.nl++error.asString);
            addLog.("ERREUR sauvegarde session complete: "++error.asString);
            false
           });
           if(wasOscRunning,{oscStart.value});
           if(ok,{
            manualParamsStatus.string_(
             "SESSION COMPLETE SAUVEE"++Char.nl++fp++Char.nl++Char.nl++
             "Moteur, poids, optimiseurs, memoires, runtime, RCU, GUI, OSC, graphes et logs ont ete archives."
            );
            addLog.("Session complete sauvee: "++fp)
           })
          })
         })
        };
        loadCompleteSession={
         Dialog.openPanel({|path|
          var session,loadedTransformer,guiState,restartOsc=false;
          if(path.notNil,{
           transformer.stopAllMorphs;
           if(oscRunning,{oscStop.value});
           session={Object.readArchive(path)}.try({|error|
            manualParamsStatus.string_("Erreur lecture session :"++Char.nl++error.asString);
            addLog.("ERREUR lecture session complete: "++error.asString);
            nil
           });
           if(session.notNil,{
            if(session.respondsTo(\at).not or:{session[\sessionVersion] != 1},{
             manualParamsStatus.string_("Session invalide ou version non prise en charge.");
             addLog.("ERREUR session incompatible: "++path)
            },{
             loadedTransformer=session[\transformer];
             if(loadedTransformer.isKindOf(HPtransformerRT).not,{
              manualParamsStatus.string_("Session invalide : objet HPtransformerRT absent.");
              addLog.("ERREUR moteur absent de la session: "++path)
             },{
              transformer=loadedTransformer;
              guiState=session[\gui] ? ();
              restartOsc=guiState[\oscWasRunning] ? false;
              restoreGuiFromSession.(session);
              lastStatus={transformer.statusSilent}.try;
              if(restartOsc,{oscStart.value});
              manualParamsStatus.string_(
               "SESSION COMPLETE CHARGEE"++Char.nl++path++Char.nl++Char.nl++
               "Date de sauvegarde : "++(session[\savedAt] ? "inconnue")++Char.nl++
               "OSC relance : "++restartOsc
              );
              addLog.("Session complete chargee: "++path)
             })
            })
           })
          })
         })
        };

        title.(pages[\manualParams],"PARAMETRES RUNTIME MANUELS",20,15,700);
        StaticText(pages[\manualParams],uiRect.(20,48,1230,42)).string_(
         "Saisissez une affectation par ligne: nom = valeur. Valeurs admises: nombres, true, false. "++
         "Les lignes // et # sont ignorees. TorusMask reste disponible dans Controls."
        ).stringColor_(Color.white);
        manualParamsEditor=TextView(pages[\manualParams],uiRect.(20,95,730,500))
         .editable_(true).background_(Color.white).stringColor_(Color.black)
         .font_(Font("Monaco",12)).string_(
          "// Exemples de parametres non visibles dans les pages principales"++Char.nl++
          "residualScale = 0.34"++Char.nl++
          "expertScale = 0.27"++Char.nl++
          "gateLearningRate = 0.0008"++Char.nl++
          "replayBatchSize = 1"++Char.nl++
          "memoryRecallSize = 3"++Char.nl++
          "generationWindowSize = 8"++Char.nl++
          "driftGain = 0.45"++Char.nl++
          "autoTuneEnabled = false"++Char.nl++
          "metaLearnEnabled = false"
         );
        title.(pages[\manualParams],"RESULTAT / VALIDATION",780,95,450);
        manualParamsStatus=TextView(pages[\manualParams],uiRect.(780,130,480,465))
         .editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black)
         .font_(Font.default.size_(12)).string_("En attente d'application.");
        button.(pages[\manualParams],"Appliquer le bloc",20,615,210,{
         applyManualParameters.(manualParamsEditor.string)
        });
        button.(pages[\manualParams],"Charger runtime actuel",250,615,220,{
         loadRuntimeIntoEditor.value
        });
        button.(pages[\manualParams],"Effacer l'editeur",490,615,190,{
         manualParamsEditor.string_("");manualParamsStatus.string_("Editeur efface.")
        });
        button.(pages[\manualParams],"Rafraichir statut",780,615,200,{
         manualParamsStatus.string_(transformer.runtimeConfig.asCompileString)
        });
        button.(pages[\manualParams],"Copier exemples",1000,615,200,{
         manualParamsEditor.string_(
          "learningRate = 0.0002"++Char.nl++
          "gradientClip = 0.35"++Char.nl++
          "replayRate = 0.12"++Char.nl++
          "protectionStrength = 0.30"++Char.nl++
          "attentionTemperature = 0.85"++Char.nl++
          "routerTemperature = 1.10"++Char.nl++
          "trajectoryRetrievalTemperature = 0.25"
         )
        });
        button.(pages[\manualParams],"Sauver parametres...",20,660,220,{
         Dialog.savePanel({|path|
          var fp;
          if(path.notNil,{
           fp=if(path.endsWith(".hptpreset"),{path},{path++".hptpreset"});
           transformer.runtimeConfig.writeArchive(fp);
           manualParamsStatus.string_("Parametres runtime sauves :"++Char.nl++fp);
           addLog.("Parametres manuels sauves: "++fp)
          })
         })
        });
        button.(pages[\manualParams],"Charger parametres...",260,660,230,{
         Dialog.openPanel({|path|
          var settings;
          if(path.notNil,{
           settings={Object.readArchive(path)}.try({|error|
            manualParamsStatus.string_("Erreur de chargement :"++Char.nl++error.asString);
            addLog.("ERREUR chargement parametres manuels: "++error.asString);
            nil
           });
           if(settings.notNil,{
            if(settings.respondsTo(\keysValuesDo),{
             loadSettings.(settings,path);
             loadRuntimeIntoEditor.value;
             manualParamsStatus.string_(
              "Parametres charges et interface actualisee :"++Char.nl++path++Char.nl++Char.nl++
              "Les sliders visibles et l'editeur refletent maintenant le runtime actif."
             );
             addLog.("Parametres manuels charges: "++path)
            },{
             manualParamsStatus.string_("Fichier invalide : aucun dictionnaire de parametres trouve.");
             addLog.("ERREUR fichier de parametres invalide: "++path)
            })
           })
          })
         })
        });
        button.(pages[\manualParams],"Exporter le bloc...",510,660,220,{
         Dialog.savePanel({|path|
          var fp,file,header;
          if(path.notNil,{
           fp=if(path.endsWith(".txt"),{path},{path++".txt"});
           header="// HPTransformer Studio - bloc de parametres runtime"++Char.nl++
            "// Format: nom = valeur"++Char.nl++
            "// Exporte le: "++Date.localtime.stamp++Char.nl++Char.nl;
           file=File(fp,"w");
           if(file.isOpen,{
            file.write(header++manualParamsEditor.string);
            file.close;
            manualParamsStatus.string_("Bloc texte exporte :"++Char.nl++fp);
            addLog.("Bloc de parametres exporte: "++fp)
           },{
            manualParamsStatus.string_("Impossible d'ouvrir le fichier :"++Char.nl++fp);
            addLog.("ERREUR export bloc parametres: "++fp)
           })
          })
         })
        });
        button.(pages[\manualParams],"Sauver session...",750,660,220,{
         saveCompleteSession.value
        });
        button.(pages[\manualParams],"Charger session...",990,660,220,{
         loadCompleteSession.value
        });

        // Morphing de parametres
        refreshMorphParameterMenu={
         var currentKey,index;
         currentKey=if(morphParamSymbols.notNil and:{morphParamSymbols.notEmpty},{morphParamSymbols[morphParamMenu.value.clip(0,morphParamSymbols.size-1)]},{nil});
         morphParamSymbols=transformer.runtimeParameterNames.select({|key|
          var value;value={transformer.getParameter(key)}.try;
          value.notNil and:{value.isNumber}
         }).collect(_.asSymbol).sort({|a,b|a.asString.toLower < b.asString.toLower});
         morphParamNames=morphParamSymbols.collect(_.asString);
         if(morphParamNames.isEmpty,{
          morphParamMenu.items_(["Aucun parametre numerique"]).value_(0)
         },{
          index=if(currentKey.notNil,{morphParamSymbols.indexOf(currentKey) ? 0},{morphParamSymbols.indexOf(\temperature) ? 0});
          morphParamMenu.items_(morphParamNames).value_(index)
         })
        };
        selectedMorphParameter={
         if(morphParamSymbols.isNil or:{morphParamSymbols.isEmpty},{nil},{morphParamSymbols[morphParamMenu.value.clip(0,morphParamSymbols.size-1)]})
        };
        refreshMorphStatus={
         var state;
         state=transformer.unifiedRCUStatus;
         morphStatus.string_(
          "Morphs actifs : "++(state[\activeMorphs]?[]).asCompileString++Char.nl++
          "RCU actif : "++state[\enabled]++Char.nl++
          "Version entrainement : "++state[\trainingVersion]++Char.nl++
          "Version publiee : "++state[\publishedVersion]
         )
        };
        startMorph={
         var key,target,duration,steps,scope,current,routine;
         key=selectedMorphParameter.value;
         target=morphTargetBox.value.asFloat;
         duration=morphDurationBox.value.asFloat.max(0.0);
         steps=morphStepsBox.value.asInteger.max(1);
         scope=morphScopeMenu.value;
         if(key.isNil,{
          morphStatus.string_("Erreur : aucun parametre numerique disponible.");addLog.("ERREUR morphing: liste vide")
         },{
          current={transformer.getParameter(key)}.try({|error|morphStatus.string_("Erreur lecture : "++error.asString);nil});
          if(current.isNil or:{current.isNumber.not},{
           morphStatus.string_("Erreur : parametre non numerique : "++key.asString)
          },{
           routine={if(scope==0,{transformer.morphParameter(key,target,duration,steps,AppClock)},{if(scope==1,{transformer.morphRuntimeParameter(key,target,duration,steps,AppClock)},{transformer.morphTrainingParameter(key,target,duration,steps,AppClock)})})}.try({|error|morphStatus.string_("Erreur demarrage morph : "++error.asString);addLog.("ERREUR morphing: "++error.asString);nil});
           if(routine.notNil,{
            morphStatus.string_("MORPH DEMARRE"++Char.nl++"Parametre : "++key.asString++Char.nl++"Depart : "++current++Char.nl++"Cible : "++target++Char.nl++"Duree : "++duration++" s"++Char.nl++"Etapes : "++steps++Char.nl++"Portee : "++["Partagee","Runtime RCU","Entrainement"][scope]);
            addLog.("Morph demarre: "++key.asString++" -> "++target++" en "++duration++" s")
           })
          })
         })
        };













































        stopSelectedMorph={
         var key,scope,morphKey;
         key=selectedMorphParameter.value;
         scope=morphScopeMenu.value;
         if(key.isNil,{morphStatus.string_("Erreur : aucun parametre selectionne.")},{
         morphKey=if(scope==0,{key},{if(scope==1,{("runtime_"++key.asString).asSymbol},{("training_"++key.asString).asSymbol})});
         transformer.stopMorph(morphKey);
         morphStatus.string_("Morph arrete : "++morphKey.asString);
         addLog.("Morph arrete: "++morphKey.asString)
         })
        };
        title.(pages[\morphing],"MORPHING DE PARAMETRES",20,15,700);
        StaticText(pages[\morphing],uiRect.(20,55,1220,42)).string_(
         "Interpolation progressive d'un parametre numerique. La portee Partagee modifie le moteur et les runtimes RCU; "++
         "Runtime RCU agit sur l'inference active; Entrainement agit sur le modele d'apprentissage."
        ).stringColor_(Color.white);
        StaticText(pages[\morphing],uiRect.(20,120,120,24)).string_("Parametre :").stringColor_(Color.white);
        morphParamMenu=PopUpMenu(pages[\morphing],uiRect.(140,117,320,30)).items_(["Chargement..."]).action_({|menu|
         var key,value;key=selectedMorphParameter.value;
         if(key.notNil,{value={transformer.getParameter(key)}.try;if(value.notNil and:{value.isNumber},{morphTargetBox.value_(value.asFloat)});morphStatus.string_("Parametre selectionne : "++key.asString++Char.nl++"Valeur actuelle : "++value.asCompileString)})
        });
        StaticText(pages[\morphing],uiRect.(475,120,70,24)).string_("Cible :").stringColor_(Color.white);
        morphTargetBox=NumberBox(pages[\morphing],uiRect.(545,117,110,30)).value_(1.25).decimals_(6);
        StaticText(pages[\morphing],uiRect.(680,120,105,24)).string_("Duree (s) :").stringColor_(Color.white);
        morphDurationBox=NumberBox(pages[\morphing],uiRect.(785,117,100,30)).value_(4.0).decimals_(2).clipLo_(0.0);
        StaticText(pages[\morphing],uiRect.(910,120,65,24)).string_("Etapes :").stringColor_(Color.white);
        morphStepsBox=NumberBox(pages[\morphing],uiRect.(975,117,90,30)).value_(80).step_(1).clipLo_(1);
        StaticText(pages[\morphing],uiRect.(20,175,120,24)).string_("Portee :").stringColor_(Color.white);
        morphScopeMenu=PopUpMenu(pages[\morphing],uiRect.(140,172,260,30)).items_(["Partagee","Runtime RCU","Entrainement"]).value_(0);
        button.(pages[\morphing],"Demarrer morph",430,170,200,{startMorph.value});
        button.(pages[\morphing],"Arreter ce morph",650,170,200,{stopSelectedMorph.value});
        button.(pages[\morphing],"Arreter tous les morphs",870,170,230,{
         transformer.stopAllMorphs;
         morphStatus.string_("Tous les morphs ont ete arretes.");
         addLog.("Tous les morphs arretes")
        });
        button.(pages[\morphing],"Actualiser etat",20,230,200,{refreshMorphStatus.value});
        button.(pages[\morphing],"Actualiser la liste",240,230,200,{refreshMorphParameterMenu.value;addLog.("Liste morphing actualisee")});
        morphStatus=TextView(pages[\morphing],uiRect.(20,280,1240,360)).editable_(false)
         .background_(Color(0.96,0.97,0.98)).stringColor_(Color.black).font_(Font.default.size_(13))
         .string_("Aucun morph lance depuis cette page." );
        refreshMorphParameterMenu.value;
        StaticText(pages[\morphing],uiRect.(460,230,790,35)).string_(
         "Exemples numeriques : attentionTemperature, routerTemperature, learningRate, replayRate, protectionStrength, residualScale, expertScale, deltaScale."
        ).stringColor_(Color.white);

        // Generation
        title.(pages[\generation],"GENERATEUR MANUEL",20,15); seedField=TextField(pages[\generation],uiRect.(20,55,900,30))
         .string_("0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5"); countBox=NumberBox(pages[\generation],uiRect.(940,55,90,30)).value_(32);
        button.(pages[\generation],"Generer",1050,53,200,{var seed,res;seed=seedField.string.split($,).collect(_.stripWhiteSpace.asFloat);
         res={transformer.generate(seed,countBox.value.asInteger.max(1))}.try({|e|addLog.("Erreur generation: "++e)});
         if(res.notNil,{outputView.string_(res.asCompileString);addLog.("Generation terminee")})});
        outputView=TextView(pages[\generation],uiRect.(20,105,800,585)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        title.(pages[\generation],"ETAT GENERATION",850,105,400,24);
        generationLiveText=TextView(pages[\generation],uiRect.(850,140,420,550)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // Memoire Live
        title.(pages[\memoryLive],"MEMOIRE LONGUE ET REPLAY",20,15);
        addSlider.(pages[\memoryLive],"Retrieval gain",\memoryRetrievalGain,ControlSpec(0,1,\lin),20,55,590);
        addSlider.(pages[\memoryLive],"Write threshold",\memoryWriteThreshold,ControlSpec(0,1,\lin),20,95,590);
        addSlider.(pages[\memoryLive],"Retrieval temperature",\memoryRetrievalTemperature,ControlSpec(0.05,2,\exp),20,135,590);
        addSlider.(pages[\memoryLive],"Memory decay",\memoryDecay,ControlSpec(0.90,1,\lin),20,175,590);
        addSlider.(pages[\memoryLive],"Consolidation",\memoryConsolidationRate,ControlSpec(0,0.25,\lin),20,215,590);
        addSlider.(pages[\memoryLive],"Usage decay",\memoryUsageDecay,ControlSpec(0.90,1,\lin),20,255,590);
        addSlider.(pages[\memoryLive],"Replay rate",\replayRate,ControlSpec(0,0.5,\lin),20,295,590);
        addSlider.(pages[\memoryLive],"Protection",\protectionStrength,ControlSpec(0,2,\lin),20,335,590);
        button.(pages[\memoryLive],"Reset Memory",20,400,190,{transformer.resetMemory;addLog.("Reset Memory")});
        button.(pages[\memoryLive],"Replay Memory",225,400,190,{{transformer.replayMemory}.try;addLog.("Replay manuel")});
        button.(pages[\memoryLive],"Status vers Post",430,400,190,{transformer.status.postln});
        title.(pages[\memoryLive],"MONITEUR MEMOIRE",660,15,500);
        memoryLiveText=TextView(pages[\memoryLive],uiRect.(660,55,610,610)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // Surprise Live
        title.(pages[\surpriseLive],"SURPRISE, PLASTICITE ET STABILITE",20,15);
        addSlider.(pages[\surpriseLive],"Surprise threshold",\surpriseThreshold,ControlSpec(0,1,\lin),20,55,590);
        addSlider.(pages[\surpriseLive],"Surprise gain",\surpriseGain,ControlSpec(0,5,\lin),20,95,590);
        addSlider.(pages[\surpriseLive],"Fast adaptation",\adaptationFastRate,ControlSpec(0,4,\lin),20,135,590);
        addSlider.(pages[\surpriseLive],"Slow adaptation",\adaptationSlowRate,ControlSpec(0,2,\lin),20,175,590);
        addSlider.(pages[\surpriseLive],"Gradient clip",\gradientClip,ControlSpec(0.01,5,\exp),20,215,590);
        addSlider.(pages[\surpriseLive],"Protection",\protectionStrength,ControlSpec(0,2,\lin),20,255,590);
        addSlider.(pages[\surpriseLive],"Prediction loss",\predictionLossWeight,ControlSpec(0,1,\lin),20,295,590);
        addSlider.(pages[\surpriseLive],"Direction loss",\directionLossWeight,ControlSpec(0,1,\lin),20,335,590);
        button.(pages[\surpriseLive],"MetaLearn ON",20,400,190,{transformer.enableMetaLearning;addLog.("MetaLearn ON")});
        button.(pages[\surpriseLive],"MetaLearn OFF",225,400,190,{transformer.disableMetaLearning;addLog.("MetaLearn OFF")});
        button.(pages[\surpriseLive],"Reset Learning",430,400,190,{transformer.resetLearning;addLog.("Reset Learning")});
        title.(pages[\surpriseLive],"MONITEUR SURPRISE",660,15,500);
        surpriseLiveText=TextView(pages[\surpriseLive],uiRect.(660,55,610,610)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // OSC / Temps reel
        // Entrees acceptees : /hptransformer/input, /learn, /predict, /generate selon le chemin choisi.
        title.(pages[\osc],"OSC / TEMPS REEL",20,15);
        StaticText(pages[\osc],uiRect.(20,55,110,24)).string_("Hote sortie :");
        oscHostField=TextField(pages[\osc],uiRect.(130,53,180,28)).string_("127.0.0.1");
        StaticText(pages[\osc],uiRect.(330,55,90,24)).string_("Port entree :");
        oscInPortBox=NumberBox(pages[\osc],uiRect.(425,53,90,28)).value_(57120).step_(1);
        StaticText(pages[\osc],uiRect.(535,55,90,24)).string_("Port sortie :");
        oscOutPortBox=NumberBox(pages[\osc],uiRect.(630,53,90,28)).value_(57130).step_(1);
        StaticText(pages[\osc],uiRect.(740,55,95,24)).string_("Mode :");
        oscModeMenu=PopUpMenu(pages[\osc],uiRect.(835,53,180,28)).items_(["Learn + Generate","Learn only","Predict only","Generate only"]).value_(0).action_({|menu|oscMode=menu.value});
        
        StaticText(pages[\osc],uiRect.(20,95,110,24)).string_("Chemin entree :");
        oscInputPathField=TextField(pages[\osc],uiRect.(130,93,300,28)).string_("/hptransformer/input");
        StaticText(pages[\osc],uiRect.(455,95,110,24)).string_("Chemin sortie :");
        oscOutputPathField=TextField(pages[\osc],uiRect.(565,93,300,28)).string_("/hptransformer/output").action_({|field|oscOutputPath=field.string});
        
        button.(pages[\osc],"Demarrer OSC",20,140,180,{oscStart.value});
        button.(pages[\osc],"Arreter OSC",215,140,180,{oscStop.value});
        oscLearnButton=Button(pages[\osc],uiRect.(410,140,210,34)).states_([
         ["Apprentissage externe OFF",Color.white,Color.red(0.52)],["Apprentissage externe ON",Color.white,Color.green(0.42)]]).font_(Font.default.boldVariant.size_(11)).value_(1).action_({|b|
         oscLearning=(b.value==1);addLog.("Apprentissage OSC "++if(oscLearning,{"ON"},{"OFF"}))});
        oscSendButton=Button(pages[\osc],uiRect.(635,140,210,34)).states_([
         ["Envoi sorties OFF",Color.white,Color.red(0.52)],["Envoi sorties ON",Color.white,Color.green(0.42)]]).font_(Font.default.boldVariant.size_(11)).value_(1).action_({|b|
         oscSendOutput=(b.value==1);addLog.("Sorties OSC "++if(oscSendOutput,{"ON"},{"OFF"}))});
        button.(pages[\osc],"Effacer evenements",860,140,190,{oscEventLines.clear;oscEventsView.string_("")});
        button.(pages[\osc],"Test sortie",1065,140,180,{
         if(oscTarget.notNil,{oscTarget.sendMsg(oscOutputPath,0.5,0.5,0.5,0.5);oscOutCount=oscOutCount+1;oscRecordEvent.("TEST OUT")})});
        
        title.(pages[\osc],"ETAT ET DEBIT",20,200);
        oscStatusText=TextView(pages[\osc],uiRect.(20,235,420,420)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        title.(pages[\osc],"EVENEMENTS RECUS / EMIS",470,200,700);
        oscEventsView=TextView(pages[\osc],uiRect.(470,235,790,420)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        oscRecordEvent={|text| var line;line=Date.localtime.stamp++"  "++text.asString;oscEventLines.add(line);
         while({oscEventLines.size>250},{oscEventLines.removeAt(0)});
         {if(oscEventsView.notNil,{oscEventsView.string_(oscEventLines.join(Char.nl))})}.defer};
        
        oscStop={
         if(oscDef.notNil,{oscDef.free;oscDef=nil});
         oscRunning=false;
         oscRecordEvent.("OSC arrete");addLog.("Interface OSC arretee");
        };
        
        oscStart={
         var inPort,outPort,host,inputPath;
         oscStop.value;
         inPort=oscInPortBox.value.asInteger.clip(1024,65535);
         outPort=oscOutPortBox.value.asInteger.clip(1024,65535);
         host=oscHostField.string;
         inputPath=oscInputPathField.string;
         oscMode=oscModeMenu.value;
         oscOutputPath=oscOutputPathField.string;
         thisProcess.openUDPPort(inPort);
         oscTarget=NetAddr(host,outPort);
         oscDef=OSCdef(\hpTransformerStudioOSC,{|msg,time,address,recvPort|
          var input,output,mode;
          input=msg.copyRange(1,msg.size-1).collect({|value|value.asFloat});
          oscInCount=oscInCount+1;oscLastInput=input.copy;mode=oscMode;
          oscRecordEvent.("IN "++address.ip++":"++recvPort++" "++input.asCompileString);
          output={
           if((mode==0 or:{mode==1}) and:{oscLearning},{transformer.learnEvent(input)});
           if(mode==0,{transformer.generateStep(input)},
            {if(mode==2,{transformer.predict(input)},{if(mode==3,{transformer.generateStep(input)},{nil})})})
          }.try({|error|oscErrorCount=oscErrorCount+1;oscRecordEvent.("ERREUR "++error.asString);nil});
          if(output.notNil,{oscLastOutput=output.copy;
           if(oscSendOutput and:{oscTarget.notNil},{oscTarget.sendMsg(oscOutputPath,*output);oscOutCount=oscOutCount+1;
            oscRecordEvent.("OUT "++output.asCompileString)})});
         },inputPath,recvPort:inPort);
         oscRunning=true;oscLastRateTime=Main.elapsedTime;oscLastInCount=oscInCount;oscLastOutCount=oscOutCount;
         oscRecordEvent.("OSC actif sur "++inPort++" -> "++host++":"++outPort);
         addLog.("Interface OSC demarree");
        };
        
        oscUpdateStatus={
         var now,elapsed;
         now=Main.elapsedTime;
         if(oscLastRateTime.isNil,{oscLastRateTime=now});
         elapsed=(now-oscLastRateTime).max(0.001);
         oscInRate=(oscInCount-oscLastInCount)/elapsed;
         oscOutRate=(oscOutCount-oscLastOutCount)/elapsed;
         oscLastInCount=oscInCount;
         oscLastOutCount=oscOutCount;
         oscLastRateTime=now;
         oscStatusText.string_((
          running:oscRunning,listenPort:oscInPortBox.value.asInteger,
          destination:(oscHostField.string++":"++oscOutPortBox.value.asInteger),
          inputPath:oscInputPathField.string,outputPath:oscOutputPath,
          learningExternal:oscLearning,sendOutput:oscSendOutput,mode:["Learn + Generate","Learn only","Predict only","Generate only"][oscMode],
          totalIn:oscInCount,totalOut:oscOutCount,errors:oscErrorCount,
          incomingPerSecond:oscInRate.round(0.01),outgoingPerSecond:oscOutRate.round(0.01),
          lastInput:oscLastInput,lastOutput:oscLastOutput
         ).asCompileString);
        };
        
        // Jeux complets coordonnes
        title.(pages[\sets],"JEUX COMPLETS APPRENTISSAGE / GENERATION",20,15,800);
        setMenu=PopUpMenu(pages[\sets],uiRect.(20,55,540,32)).items_(setNames).value_(0).action_({|menu|
         setDescriptionView.string_(setDescriptions[menu.value])
        });
        button.(pages[\sets],"Appliquer le jeu",580,53,210,{applyConfigurationSet.(setMenu.value)});
        button.(pages[\sets],"Exporter jeu actuel",805,53,210,{exportCurrentSet.value});
        button.(pages[\sets],"Sauver preset runtime",1030,53,220,{savePreset.value});
        setDescriptionView=TextView(pages[\sets],uiRect.(20,110,1230,475)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black).font_(Font.default.size_(12));
        setDescriptionView.string_(setDescriptions[0]);
        StaticText(pages[\sets],uiRect.(20,605,1230,55)).string_(
         "Conseil : commencez par Studio Equilibre. Utilisez Performance Stable pour la restitution, Improvisation Creative pour explorer, et Faible Latence OSC pour les flux rapides."
        ).stringColor_(Color.white).font_(Font.default.boldVariant.size_(12));
        
        // Graphes
        title.(pages[\graphs],"SUIVI TEMPS REEL",20,15);
        graphView=UserView(pages[\graphs],uiRect.(20,50,1270,590)).background_(Color.white).clearOnRefresh_(true);
        addGraphSample={|rawList,emaList,value|
         var v,previous;
         if(value.notNil and:{value.isNumber},{
          v=value.asFloat;
          if(v.isNaN.not,{
           rawList.add(v);
           previous=if(emaList.isEmpty,{v},{emaList.last});
           emaList.add((previous*(1.0-graphSmoothAlpha))+(v*graphSmoothAlpha));
          })
         })
        };
        clearGraphs={
         [loss,surprise,entropy,memRecall,trajRecall,lossEMA,surpriseEMAPlot,entropyEMA,memRecallEMA,trajRecallEMA].do(_.clear);
         lastStatus=nil;
         graphView.refresh;
         addLog.("Graphes rafraichis: historiques brut et lisse effaces")
        };
        drawLine={|data,smoothData,rect,color,label,maxFixed,target=nil|
         var a,s,minVal,maxVal,range,plotRect,xFor,yFor,lastValue,meanValue,rawColor;
         a=data.asArray.collect({|v|v.asFloat});
         s=smoothData.asArray.collect({|v|v.asFloat});
         plotRect=Rect(rect.left+8,rect.top+44,rect.width-16,rect.height-58);
         Pen.fillColor=Color.white;Pen.fillRect(rect);
         Pen.strokeColor=Color.grey(0.72);Pen.width=1;Pen.strokeRect(rect);
         Pen.stringAtPoint(label,Point(rect.left+8,rect.top+7),Font.default.boldVariant.size_(12),Color.black);
         if(a.isEmpty,{
          Pen.stringAtPoint("En attente de donnees",Point(rect.left+8,rect.top+27),Font.default.size_(11),Color.grey(0.35))
         },{
          lastValue=a.last;meanValue=a.mean;
          if(maxFixed.notNil,{minVal=0.0;maxVal=maxFixed.asFloat},{
           minVal=(a++s).minItem.min(0.0);maxVal=(a++s).maxItem.max(0.000001);
           if((maxVal-minVal).abs<0.000001,{maxVal=minVal+0.000001})
          });
          range=(maxVal-minVal).max(0.000001);
          xFor={|index,size|plotRect.left+(if(size>1,{index/(size-1)},{0.5})*plotRect.width)};
          yFor={|v|plotRect.bottom-(((v-minVal)/range).clip(0,1)*plotRect.height)};
          Pen.stringAtPoint(
           "act "++lastValue.round(0.000001)++"  moy "++meanValue.round(0.000001),
           Point(rect.right-195,rect.top+7),Font.default.size_(9),Color.black
          );
          Pen.stringAtPoint(
           "min "++a.minItem.round(0.000001)++"  max "++a.maxItem.round(0.000001)++"  echelle "++maxVal.round(0.000001),
           Point(rect.left+8,rect.bottom-15),Font.default.size_(8),Color.grey(0.35)
          );
          Pen.strokeColor=Color.grey(0.90);Pen.width=1;
          [0.25,0.50,0.75].do({|fraction|
           var gy;gy=plotRect.bottom-(plotRect.height*fraction);
           Pen.moveTo(Point(plotRect.left,gy));Pen.lineTo(Point(plotRect.right,gy));Pen.stroke
          });
          if(target.notNil and:{target>=minVal} and:{target<=maxVal},{
           Pen.strokeColor=Color(0.2,0.55,0.2,0.65);Pen.width=1;
           Pen.moveTo(Point(plotRect.left,yFor.(target)));Pen.lineTo(Point(plotRect.right,yFor.(target)));Pen.stroke;
           Pen.stringAtPoint("cible "++target,Point(plotRect.left+4,yFor.(target)-14),Font.default.size_(8),Color(0.1,0.45,0.1))
          });
          rawColor=Color(color.red,color.green,color.blue,0.28);
          if(a.size>1,{
           Pen.strokeColor=rawColor;Pen.width=1;
           Pen.moveTo(Point(xFor.(0,a.size),yFor.(a[0])));
           (1..a.size-1).do({|index|Pen.lineTo(Point(xFor.(index,a.size),yFor.(a[index])))});Pen.stroke
          });
          if(s.size>1,{
           Pen.strokeColor=color;Pen.width=3;
           Pen.moveTo(Point(xFor.(0,s.size),yFor.(s[0])));
           (1..s.size-1).do({|index|Pen.lineTo(Point(xFor.(index,s.size),yFor.(s[index])))});Pen.stroke;
           Pen.fillColor=color;Pen.fillOval(Rect(xFor.(s.size-1,s.size)-4,yFor.(s.last)-4,8,8))
          });
          Pen.stringAtPoint("brut",Point(rect.right-78,rect.bottom-31),Font.default.size_(8),rawColor);
          Pen.stringAtPoint("EMA",Point(rect.right-42,rect.bottom-31),Font.default.boldVariant.size_(8),color)
         })
        };



























        graphView.drawFunc_({
         var lossMax,metricMax;
         Pen.fillColor=Color.white;Pen.fillRect(graphView.bounds.moveTo(0,0));
         lossMax=[nil,0.10,1.0][lossScaleMode];
         metricMax=[nil,1.0,2.0][metricScaleMode];
         drawLine.(loss,lossEMA,graphRect.(15,20,390,260),Color(0.85,0.10,0.10),"LOSS",lossMax,0.02);
         drawLine.(surprise,surpriseEMAPlot,graphRect.(435,20,390,260),Color(0.90,0.48,0.0),"SURPRISE",metricMax,nil);
         drawLine.(entropy,entropyEMA,graphRect.(855,20,390,260),Color(0.0,0.48,0.72),"ENTROPY",metricMax,nil);
         drawLine.(memRecall,memRecallEMA,graphRect.(225,330,390,260),Color(0.0,0.58,0.22),"MEMORY RECALL",metricMax,nil);
         drawLine.(trajRecall,trajRecallEMA,graphRect.(655,330,390,260),Color(0.18,0.28,0.88),"TRAJECTORY RECALL",metricMax,nil)
        });
        button.(pages[\graphs],"Exporter CSV",20,650,170,{exportCSV.value});
        button.(pages[\graphs],"Rafraichir graphes",205,650,190,{clearGraphs.value});
        StaticText(pages[\graphs],uiRect.(415,656,75,22)).string_("Loss :").stringColor_(Color.white);
        lossScaleMenu=PopUpMenu(pages[\graphs],uiRect.(475,650,145,28)).items_(["Auto","0..0.10","0..1.00"]).value_(1).action_({|menu|lossScaleMode=menu.value;graphView.refresh});
        StaticText(pages[\graphs],uiRect.(635,656,95,22)).string_("Autres :").stringColor_(Color.white);
        metricScaleMenu=PopUpMenu(pages[\graphs],uiRect.(705,650,145,28)).items_(["Auto","0..1.00","0..2.00"]).value_(1).action_({|menu|metricScaleMode=menu.value;graphView.refresh});
        StaticText(pages[\graphs],uiRect.(870,656,90,22)).string_("EMA alpha :").stringColor_(Color.white);
        smoothingBox=NumberBox(pages[\graphs],uiRect.(955,650,90,28)).value_(graphSmoothAlpha).decimals_(3).clipLo_(0.001).clipHi_(1.0).action_({|box|
         graphSmoothAlpha=box.value.asFloat.clip(0.001,1.0);
         [lossEMA,surpriseEMAPlot,entropyEMA,memRecallEMA,trajRecallEMA].do(_.clear);
         [[loss,lossEMA],[surprise,surpriseEMAPlot],[entropy,entropyEMA],[memRecall,memRecallEMA],[trajRecall,trajRecallEMA]].do({|pair|
          pair[0].do({|value|var previous;previous=if(pair[1].isEmpty,{value},{pair[1].last});pair[1].add((previous*(1.0-graphSmoothAlpha))+(value*graphSmoothAlpha))})
         });graphView.refresh
        });
        StaticText(pages[\graphs],uiRect.(1060,656,230,22)).string_("clair = brut | fonce = tendance EMA").stringColor_(Color.white);












        // Heatmaps
        title.(pages[\heatmaps],"EXPERTS / HEADS / FEATURE GATES",20,15);
        heatView=UserView(pages[\heatmaps],uiRect.(20,50,1270,570)).background_(Color.white).clearOnRefresh_(true);
        heatView.drawFunc_({|view|
         var e,h,f,barW,barMax,baseY,cellW,cellH,vv,maxCols;
         Pen.fillColor=Color.white; Pen.fillRect(view.bounds.moveTo(0,0));
         Pen.stringAtPoint("UTILISATION DES EXPERTS",heatPoint.(30,18),Font.default.boldVariant.size_(12),Color.black);
         Pen.stringAtPoint("ACTIVITE DES HEADS",heatPoint.(680,18),Font.default.boldVariant.size_(12),Color.black);
         Pen.stringAtPoint("FEATURE GATES",heatPoint.(30,315),Font.default.boldVariant.size_(12),Color.black);
         if(lastStatus.isNil,{
          Pen.stringAtPoint("En attente de donnees du transformeur",heatPoint.(30,52),Font.default.size_(12),Color.grey(0.35))
         },{
          e=lastStatus[\expertUsageEMA] ? (lastStatus[\expertUsage] ? []); h=lastStatus[\headActivity]?[]; f=lastStatus[\headFeatureUsage]?[];
          barMax=190; baseY=270;
          if(e.isEmpty,{Pen.stringAtPoint("Aucune donnee expertUsage",heatPoint.(30,52),Font.default.size_(11),Color.grey(0.35))},{
           barW=(570/e.size.max(1)).clip(22,90);
           e.do({|v,i|vv=v.asFloat.clip(0,1);Pen.fillColor=Color(0.88,0.18,0.18);Pen.fillRect(heatRect.(30+(i*barW),baseY-(vv*barMax),barW-6,(vv*barMax).max(2)));Pen.stringAtPoint(i.asString,heatPoint.(34+(i*barW),baseY+5),Font.default.size_(9),Color.black);Pen.stringAtPoint(vv.round(0.001).asString,heatPoint.(32+(i*barW),baseY-(vv*barMax)-15),Font.default.size_(8),Color.black)})
          });
          if(h.isEmpty,{Pen.stringAtPoint("Aucune donnee headActivity",heatPoint.(680,52),Font.default.size_(11),Color.grey(0.35))},{
           barW=(540/h.size.max(1)).clip(22,90);
           h.do({|v,i|vv=v.asFloat.clip(0,1);Pen.fillColor=Color(0.15,0.38,0.88);Pen.fillRect(heatRect.(680+(i*barW),baseY-(vv*barMax),barW-6,(vv*barMax).max(2)));Pen.stringAtPoint(i.asString,heatPoint.(684+(i*barW),baseY+5),Font.default.size_(9),Color.black);Pen.stringAtPoint(vv.round(0.001).asString,heatPoint.(682+(i*barW),baseY-(vv*barMax)-15),Font.default.size_(8),Color.black)})
          });
          if(f.isEmpty,{Pen.stringAtPoint("Aucune donnee headFeatureUsage",heatPoint.(30,350),Font.default.size_(11),Color.grey(0.35))},{
           maxCols=(f.collect({|row|row.size}).maxItem?1).max(1); cellW=(1180/maxCols).clip(16,42); cellH=(225/f.size.max(1)).clip(16,42);
           f.do({|row,y|row.do({|v,x|vv=v.asFloat.clip(0,1);Pen.fillColor=Color(1.0-vv,1.0-(vv*0.45),1.0-vv);Pen.fillRect(heatRect.(30+(x*cellW),350+(y*cellH),cellW-2,cellH-2));Pen.strokeColor=Color.grey(0.75);Pen.width=1;Pen.strokeRect(heatRect.(30+(x*cellW),350+(y*cellH),cellW-2,cellH-2));if((cellW>=28)and:{cellH>=22},{Pen.stringAtPoint(vv.round(0.01).asString,heatPoint.(32+(x*cellW),354+(y*cellH)),Font.default.size_(7),Color.black)})})})
          })
         })
        });
        exportHeat={Dialog.savePanel({|path|var img,fp;if(path.notNil,{fp=if(path.endsWith(".png"),{path},{path++".png"});img=Image.new((1270*heatDrawScale).asInteger,(610*heatDrawScale).asInteger);img.draw({heatView.drawFunc.value(heatView)});img.write(fp);img.free;addLog.("Heatmap exportee: "++fp)})})};
        button.(pages[\heatmaps],"Exporter heatmaps PNG",20,635,230,{exportHeat.value});
        // Generation presets
        title.(pages[\genPresets],"PRESETS GENERATION",20,15);
        genMenu=PopUpMenu(pages[\genPresets],uiRect.(20,55,500,30)).items_(genNames);
        button.(pages[\genPresets],"Appliquer Generation",540,53,220,{loadSettings.(genPresets[genMenu.value],genNames[genMenu.value])});
        button.(pages[\genPresets],"Generer un test",780,53,190,{var x,r;x=Array.fill((transformer.config[\inputSize]?8),{0.5});r=transformer.generate(x,16);genPresetText.string_(r.asCompileString);addLog.("Test generation preset")});
        button.(pages[\genPresets],"Reset generation",990,53,210,{transformer.resetLearning;addLog.("Etat generation reinitialise")});
        addSlider.(pages[\genPresets],"Attention temperature",\attentionTemperature,ControlSpec(0.1,5,\exp),20,115,600);
        addSlider.(pages[\genPresets],"Router temperature",\routerTemperature,ControlSpec(0.1,5,\exp),20,155,600);
        addSlider.(pages[\genPresets],"Trajectory temperature",\trajectoryRetrievalTemperature,ControlSpec(0.05,2,\exp),20,195,600);
        addSlider.(pages[\genPresets],"Delta scale",\deltaScale,ControlSpec(0,1,\lin),20,235,600);
        addSlider.(pages[\genPresets],"Exploration",\trajectoryExplorationGain,ControlSpec(0,0.05,\lin),20,275,600);
        addSlider.(pages[\genPresets],"Diversity noise",\diversityNoiseGain,ControlSpec(0,0.02,\lin),20,315,600);
        addSlider.(pages[\genPresets],"Diversity repulsion",\diversityRepulsionGain,ControlSpec(0,0.05,\lin),20,355,600);
        genPresetText=TextView(pages[\genPresets],uiRect.(660,115,590,470)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // Memory presets
        title.(pages[\memoryPresets],"PRESETS MEMOIRE",20,15);
        memoryMenu=PopUpMenu(pages[\memoryPresets],uiRect.(20,55,500,30)).items_(memoryNames);
        button.(pages[\memoryPresets],"Appliquer Memoire",540,53,220,{loadSettings.(memoryPresets[memoryMenu.value],memoryNames[memoryMenu.value])});
        button.(pages[\memoryPresets],"Reset Memory",780,53,190,{transformer.resetMemory;addLog.("Reset Memory")});
        button.(pages[\memoryPresets],"Status Memory",990,53,210,{memoryPresetText.string_(lastStatus.asCompileString)});
        addSlider.(pages[\memoryPresets],"Retrieval gain",\memoryRetrievalGain,ControlSpec(0,1,\lin),20,115,600);
        addSlider.(pages[\memoryPresets],"Write threshold",\memoryWriteThreshold,ControlSpec(0,1,\lin),20,155,600);
        addSlider.(pages[\memoryPresets],"Retrieval temperature",\memoryRetrievalTemperature,ControlSpec(0.05,2,\exp),20,195,600);
        addSlider.(pages[\memoryPresets],"Memory decay",\memoryDecay,ControlSpec(0.90,1,\lin),20,235,600);
        addSlider.(pages[\memoryPresets],"Replay rate",\replayRate,ControlSpec(0,0.5,\lin),20,275,600);
        memoryPresetText=TextView(pages[\memoryPresets],uiRect.(660,115,590,470)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // Surprise presets
        title.(pages[\surprisePresets],"PRESETS SURPRISE ET PLASTICITE",20,15);
        surpriseMenu=PopUpMenu(pages[\surprisePresets],uiRect.(20,55,500,30)).items_(surpriseNames);
        button.(pages[\surprisePresets],"Appliquer Surprise",540,53,220,{loadSettings.(surprisePresets[surpriseMenu.value],surpriseNames[surpriseMenu.value])});
        button.(pages[\surprisePresets],"MetaLearn ON",780,53,190,{transformer.enableMetaLearning;addLog.("MetaLearn ON depuis Surprise")});
        button.(pages[\surprisePresets],"Reset Learning",990,53,210,{transformer.resetLearning;addLog.("Reset Learning")});
        addSlider.(pages[\surprisePresets],"Surprise threshold",\surpriseThreshold,ControlSpec(0,1,\lin),20,115,600);
        addSlider.(pages[\surprisePresets],"Surprise gain",\surpriseGain,ControlSpec(0,5,\lin),20,155,600);
        addSlider.(pages[\surprisePresets],"Fast adaptation",\adaptationFastRate,ControlSpec(0,4,\lin),20,195,600);
        addSlider.(pages[\surprisePresets],"Slow adaptation",\adaptationSlowRate,ControlSpec(0,2,\lin),20,235,600);
        addSlider.(pages[\surprisePresets],"Protection",\protectionStrength,ControlSpec(0,2,\lin),20,275,600);
        surprisePresetText=TextView(pages[\surprisePresets],uiRect.(660,115,590,470)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // AutoTune presets
        title.(pages[\auto],"PRESETS AUTOTUNE",20,15);autoMenu=PopUpMenu(pages[\auto],uiRect.(20,55,500,30)).items_(autoNames);
        button.(pages[\auto],"Appliquer AutoTune",540,53,210,{loadSettings.(autoPresets[autoMenu.value],autoNames[autoMenu.value])});
        button.(pages[\auto],"Activer",770,53,150,{transformer.enableAutoTune;addLog.("AutoTune ON")});button.(pages[\auto],"Desactiver",935,53,150,{transformer.disableAutoTune;addLog.("AutoTune OFF")});
        button.(pages[\auto],"Reset",1100,53,150,{transformer.resetAutoTune;addLog.("AutoTune reset")});
        addSlider.(pages[\auto],"Strength",\autoTuneStrength,ControlSpec(0,1,\lin),20,115,600);addSlider.(pages[\auto],"Smoothing",\autoTuneSmoothing,ControlSpec(0,0.999,\lin),20,155,600);
        addSlider.(pages[\auto],"Target novelty",\autoTuneTargetNovelty,ControlSpec(0,1,\lin),20,195,600);addSlider.(pages[\auto],"Target diversity",\autoTuneTargetDiversity,ControlSpec(0,1,\lin),20,235,600);
        autoText=TextView(pages[\auto],uiRect.(660,115,590,470)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // MetaLearn presets
        title.(pages[\meta],"PRESETS METALEARN",20,15);metaMenu=PopUpMenu(pages[\meta],uiRect.(20,55,500,30)).items_(metaNames);
        button.(pages[\meta],"Appliquer MetaLearn",540,53,210,{loadSettings.(metaPresets[metaMenu.value],metaNames[metaMenu.value])});
        button.(pages[\meta],"Activer",770,53,150,{transformer.enableMetaLearning;addLog.("MetaLearn ON")});button.(pages[\meta],"Desactiver",935,53,150,{transformer.disableMetaLearning;addLog.("MetaLearn OFF")});
        button.(pages[\meta],"Reset",1100,53,150,{transformer.resetMetaLearning;addLog.("MetaLearn reset")});
        addSlider.(pages[\meta],"Strength",\metaLearnStrength,ControlSpec(0,1,\lin),20,115,600);addSlider.(pages[\meta],"Smoothing",\metaLearnSmoothing,ControlSpec(0,0.999,\lin),20,155,600);
        addSlider.(pages[\meta],"Target error",\metaLearnTargetError,ControlSpec(0,1,\lin),20,195,600);addSlider.(pages[\meta],"Target recall",\metaLearnTargetRecall,ControlSpec(0,1,\lin),20,235,600);
        metaText=TextView(pages[\meta],uiRect.(660,115,590,470)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        
        // RCU
        title.(pages[\rcu],"SNAPSHOTS RCU",20,15);rcuText=TextView(pages[\rcu],uiRect.(20,55,700,550)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        button.(pages[\rcu],"Activer RCU",760,55,210,{transformer.enableUnifiedRCU;addLog.("RCU active")});button.(pages[\rcu],"Desactiver RCU",990,55,210,{transformer.disableUnifiedRCU;addLog.("RCU desactive")});
        button.(pages[\rcu],"Prepare",760,105,210,{addLog.("Prepare v"++transformer.prepareSnapshot)});button.(pages[\rcu],"Commit",990,105,210,{addLog.("Commit v"++transformer.commitSnapshot)});
        button.(pages[\rcu],"Publish",760,155,210,{addLog.("Publish v"++transformer.publishNow)});button.(pages[\rcu],"Discard",990,155,210,{transformer.discardPendingSnapshot;addLog.("Pending discard")});
        button.(pages[\rcu],"Sauver archive",760,230,210,{Dialog.savePanel({|p|if(p.notNil,{transformer.saveArchive(p);addLog.("Archive sauvee")})})});
        
        // Logs
        title.(pages[\logs],"LOGS",20,15);logView=TextView(pages[\logs],uiRect.(20,55,1270,610)).editable_(false).background_(Color(0.96,0.97,0.98)).stringColor_(Color.black);
        exportLogs={Dialog.savePanel({|p|var f,fp;if(p.notNil,{fp=if(p.endsWith(".txt"),{p},{p++".txt"});f=File(fp,"w");f.write(logs.join(Char.nl));f.close;addLog.("Logs exportes")})})};
        button.(pages[\logs],"Exporter logs",20,685,190,{exportLogs.value});button.(pages[\logs],"Effacer logs",225,685,190,{logs.clear;logView.string_("")});
        
        savePreset={Dialog.savePanel({|p|if(p.notNil,{var fp=if(p.endsWith(".hptpreset"),{p},{p++".hptpreset"});transformer.runtimeConfig.writeArchive(fp);addLog.("Preset sauve: "++fp)})})};
        loadPreset={Dialog.openPanel({|p|if(p.notNil,{var x=Object.readArchive(p);if(x.respondsTo(\keysValuesDo),{loadSettings.(x,p)})})})};
        // Style global haute lisibilite macOS / Qt.
        pages.keysValuesDo({|pageName,pageView|pageView.children.do({|child|
         if(child.isKindOf(TextView),{child.background_(Color(0.96,0.97,0.98));child.stringColor_(Color.black);child.font_(Font.default.size_(12));child.refresh});
         if(child.isKindOf(StaticText),{child.stringColor_(Color.white)});
         if(child.isKindOf(TextField),{child.background_(Color.white);child.stringColor_(Color.black)});
         if(child.isKindOf(NumberBox),{child.background_(Color.white);child.stringColor_(Color.black)});
         if(child.isKindOf(PopUpMenu),{child.background_(Color.white);child.stringColor_(Color.black)})
        })});
        
        [statusText,outputView,generationLiveText,memoryLiveText,surpriseLiveText,oscStatusText,oscEventsView,genPresetText,memoryPresetText,surprisePresetText,autoText,metaText,rcuText,logView,torusMaskStatus,setDescriptionView,manualParamsStatus,morphStatus].do({|view|if(view.notNil,{view.background_(Color(0.96,0.97,0.98));view.stringColor_(Color.black);view.font_(Font.default.size_(12));view.refresh})});
        exportCSV={Dialog.savePanel({|p|var f,n,fp;if(p.notNil,{fp=if(p.endsWith(".csv"),{p},{p++".csv"});f=File(fp,"w");f.write("index,loss,surprise,entropy,memoryRecall,trajectoryRecall\n");
         n=loss.size.min(surprise.size).min(entropy.size).min(memRecall.size).min(trajRecall.size);n.do({|i|f.write([i,loss[i],surprise[i],entropy[i],memRecall[i],trajRecall[i]].join(",")++"\n")});f.close;addLog.("CSV exporte")})})};
        
        refresh={lastStatus={if(transformer.respondsTo(\statusSilent),{transformer.statusSilent},{transformer.status})}.try;if(lastStatus.notNil,{addGraphSample.(loss,lossEMA,lastStatus[\loss]);
         addGraphSample.(surprise,surpriseEMAPlot,lastStatus[\surpriseEMA]);
         addGraphSample.(entropy,entropyEMA,lastStatus[\entropy]);
         addGraphSample.(memRecall,memRecallEMA,lastStatus[\memoryRecall]);
         addGraphSample.(trajRecall,trajRecallEMA,lastStatus[\trajectoryRecall]);
         [loss,surprise,entropy,memRecall,trajRecall,lossEMA,surpriseEMAPlot,entropyEMA,memRecallEMA,trajRecallEMA].do(trim);statusText.string_(lastStatus.asCompileString);
         autoText.string_(transformer.autoTuneStatus.asCompileString);metaText.string_(transformer.metaLearnStatus.asCompileString);rcuText.string_(transformer.unifiedRCUStatus.asCompileString);
         genPresetText.string_((generationDiversity:(lastStatus[\generationDiversity]?0),generationNovelty:(lastStatus[\generationNovelty]?0),trajectoryRecall:(lastStatus[\trajectoryRecall]?0),attentionTemperature:transformer.getParameter(\attentionTemperature),routerTemperature:transformer.getParameter(\routerTemperature),trajectoryTemperature:transformer.getParameter(\trajectoryRetrievalTemperature)).asCompileString);
         memoryPresetText.string_((memoryCount:(lastStatus[\memoryCount]?0),replayCount:(lastStatus[\replayCount]?0),memoryRecall:(lastStatus[\memoryRecall]?0),memoryNovelty:(lastStatus[\memoryNovelty]?0),writeScore:(lastStatus[\memoryWriteScore]?0)).asCompileString);
         surprisePresetText.string_((surprise:(lastStatus[\surprise]?0),surpriseEMA:(lastStatus[\surpriseEMA]?0),errorEMA:(lastStatus[\errorEMA]?0),learningRate:(lastStatus[\learningRate]?0),protection:(lastStatus[\protectionScalar]?0)).asCompileString);
         generationLiveText.string_((
          generationDiversity:(lastStatus[\generationDiversity]?0), generationNovelty:(lastStatus[\generationNovelty]?0),
          generationPressure:(lastStatus[\generationPressure]?0), trajectoryRecall:(lastStatus[\trajectoryRecall]?0),
          trajectoryNovelty:(lastStatus[\trajectoryNovelty]?0), attentionTemperature:transformer.getParameter(\attentionTemperature),routerTemperature:transformer.getParameter(\routerTemperature),trajectoryTemperature:transformer.getParameter(\trajectoryRetrievalTemperature),
          exploration:transformer.getParameter(\trajectoryExplorationGain), noise:transformer.getParameter(\diversityNoiseGain)
         ).asCompileString);
         memoryLiveText.string_((
          memoryCount:(lastStatus[\memoryCount]?0), replayCount:(lastStatus[\replayCount]?0),
          memoryRecall:(lastStatus[\memoryRecall]?0), memoryNovelty:(lastStatus[\memoryNovelty]?0),
          memoryWriteScore:(lastStatus[\memoryWriteScore]?0), replayLoss:(lastStatus[\replayLoss]?0),
          protectionScalar:(lastStatus[\protectionScalar]?0), memoryImportance:(lastStatus[\memoryImportance]?[]),
          memoryAge:(lastStatus[\memoryAge]?[]), memoryUsage:(lastStatus[\memoryUsage]?[])
         ).asCompileString);
         surpriseLiveText.string_((
          surprise:(lastStatus[\surprise]?0), surpriseEMA:(lastStatus[\surpriseEMA]?0),
          errorEMA:(lastStatus[\errorEMA]?0), loss:(lastStatus[\loss]?0), outputLoss:(lastStatus[\outputLoss]?0),
          deltaLoss:(lastStatus[\deltaLoss]?0), directionLoss:(lastStatus[\directionLoss]?0),
          learningRate:(lastStatus[\learningRate]?0), protectionScalar:(lastStatus[\protectionScalar]?0),
          interferenceScore:(lastStatus[\interferenceScore]?0)
         ).asCompileString);
         oscUpdateStatus.value;
         if(activePage==\graphs,{graphView.refresh});if(activePage==\heatmaps,{heatView.refresh})})};
        routine=Routine({while({running},{{refresh.value}.defer;rate.wait})}).play(AppClock);
        w.onClose_({running=false;oscStop.value;if(routine.notNil,{routine.stop});window=nil;refreshRoutine=nil});showPage.(\dashboard);addLog.("Studio Pro V8.1.1 compact 13 pouces ouvert - echelle "++uiScale);window=w;refreshRoutine=routine;scrollView.visibleOrigin_(Point(0,0));w.front;
    }
}

