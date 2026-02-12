/*// NewAlgo

Density {

	classvar <> s, kohonenF, kohonenA, kohonenD, geneticF, geneticA, geneticD, neuralFAD, chanelsMidi;

	var midiOut, tempoClock, groupeAnalyse, groupeRecBuffer, groupeSynth, groupeFX, groupeMasterOut, groupeVerb, busAnalyzeIn, busRecAudioIn, synthAudioIn, synthFileIn, synthAnalyseFFT, synthAnalyseOnsets, synthAnalysePitch, synthAnalysePitch2, synthAnalyseKeyTrack, synthKeyboard, synthMIDI, synthAnalyzeAudioIn, synthRecAudioIn, windowEar, startSystem, switchSourceIn, switchAnalyze, typeAlgoAnalyze, canalMIDI, windowKeyboard, keyboardTranslate, keyboardTranslateBefore, keyboardVolume, keyboard, windowPlotterData, refreshDisplayDataMusic, plotterDataGUI, windowPlotterFFT, refreshDisplayFFT, windowLimiter, listeWindows, initSynthDef, numberAudioOut, cmdperiodfunc, bufferFile, fonctionLoadFileForAnalyse, keyVolume, plotterData, plotterFFT, plotterFFTGUI, createGUI, oscFFT, displayAnalyzeFFT, displayAnalyzeMusic, serveurAdresse;
	var lastTime, oscMusic,  windowGVerb, tuning, degrees, root, scale, flagScaling, typeMasterOut, rangeDBintruments, rangeFreqintruments, quantizationDuree, stretchDuree, rangeDureeintruments, freqFiltreGUI, ampFiltreGUI, durFiltreGUI, dureeMaximumAnalyze, fhzFilter, ampFilter, dureeFilter, flagAlgoAnalyze, plotDataFFT, plotDataMusic, userBPM, setupKeyboardShortCut, fonctionShortCut, keyboardShortCut, shortCutCommande, fonctionShortCutCommande, listeFileAnalyze, listeMasterOut, listeNameFileAnalyze, formatRecordingMenu, recChannels, midiMenu, helpDensity, flagMidiOut, masterAppAddr, slaveAppAddr, oscStateFlag, ardourOSC, indexWindows, pathData, oscMenu, globalDensity, fonctionLoadPreset, fonctionSavePreset, fonctionCollectFolders, foldersToScanAll, foldersToScanPreset, stringFormat, busSynthInOut, listeBuffer, fonctionLoadSoundOrchestra, playInstruments, windowGlobal, pathSound, soundOrchestra, soundMenu, fxMenu, synthMenu, fxOrchestra, synthOrchestra, listeBusOff, maximumInstruments;
	var listeDataInstruments, buildSynth, midiOutLo, midiOutHi, panSynthHi, panSynthLo, busOSCflux, busOSCflatness, busOSCcentroid, busOSCenergy, busOSCbpm, computeAlgoFilterDataMusic, envelopeSynth, maximumData,  algoMenu, ctrlHP1, ctrlHP2, fadeFX, loopSound, reverseSound, offsetSound, flagSampler, memoryTime, dureeAnalyzeOSCMusic, watchSilence, fonctionRecPause, fonctionRecOff, fonctionRecOn, headerFormat, sampleFormat, flagRecording, indexInstrumentX, indexInstrumentY, jitterIndexInstrumentX, jitterIndexInstrumentY, displayInstrument, displaySound, indexSoundX, indexSoundY, jitterIndexSoundX, jitterIndexSoundY, displayFX, indexFXX, indexFXY, jitterIndexFXX, jitterIndexFXY, dureeSample, recLevel, preLevel, loopRec, flagRec, gVerb, freeVerb, allPass, flagRoot, flagBPM, oldTempo, flagChord, menuHelp, menuFile, menuPreset, menuInitAll, menuAudio, menuOSC, menuMIDI;
	var menuRecording, jpVerb, groupeLimiter, menuAlgo, sliderAlgorithm, listAlgorithm, algoLo, algoHi, displayAlgo, jitterControls,numFhzBand, bandFHZ, dataFlux, dataFlatness, dataCentroid, dataEnergy, dataBPM, dataFreq, dataAmp, dataDuree, indexDataFlux, indexDataFlatness, indexDataCentroid, indexDataEnergy, indexDataBPM, indexDataFreq, indexDataAmp, indexDataDuree, memoryDataFlux, memoryDataFlatness,	memoryDataCentroid, memoryDataEnergy, memoryDataBPM, memoryDataFreq, memoryDataAmp, memoryDataDuree, busOSCfreq, busOSCamp, busOSCduree, memoryMusic, flagMemory, flagFhzBand;
	var sliderSynthBand, rangeSynthBand, numIndexSynthBand, displayIndex, flagBand, fonctionBand, displayMIDI, midiRange, freqBefore, ampBefore, dureeBefore, freqTampon, ampTampon, lastTimeAnalyse, menuVST, synthVST, fxVST, groupeVST, windowVST, flagVST, flagRecSound, widthMC, orientationMC, slaveAppAddr, numberAudioIn, channelsSynth, channelsVerb, rangeFFT, rangeBand, loopMusic;

	*new {arg path = "~/Documents/Density/", ni = 2, numberOut=2, numberRec=2, format=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0, name="Density", wek=6448, wekPort=57120, scPort=57110;

		^super.new.init(name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag, scPort);

	}

	init {arg name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag, scPort;

		//// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		pathData = PathName.new(path).pathOnly;
		pathSound = "/Applications/SuperCollider.app/Contents/Resources/";

		// MIDI INIT
		// Connect first device by default
		MIDIClient.init;
		MIDIIn.connect(0, 0);
		midiOut = MIDIOut(0).latency = 0.01;
		midiOut.connect(0);
		16.do({arg canal; midiOut.allNotesOff(canal)});

		// Verify Path
		if(File.exists(pathData).not) {systemCmd("mkdir" + pathData)};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		numberAudioIn = ni;
		numberAudioOut = numberOut;
		recChannels = numberRec;
		typeMasterOut = switch(format,
			0, {"Stereo"},
			1, {"Rotate2"},
			2, {"MultiSpeaker"},
			3, {"Ambisonic"},
			4, {"Dolby5.1"},
		);// Type Format stereo, ambisonic, etc...

		//Server.default = s = Server(name,NetAddr("localhost", scPort), Server.default.options);

		s = Server.default;
		s.options.memSize = 2**20;
		s.options.inDevice_(devIn);
		s.options.outDevice_(devOut);
		s.options.numInputBusChannels_(numberAudioIn);
		s.options.hardwareBufferSize_(size);
		s.options.numOutputBusChannels_(numberAudioOut);
		s.recChannels_(recChannels);
		s.options.safetyClipThreshold = 1;// Pour test
		widthMC = wid;
		orientationMC = ori;

		thisProcess.openUDPPort(NetAddr.langPort);

		Safety(s);
		//s.makeGui;

		headerFormat = "aiff";
		sampleFormat = "float";

		// Init
		typeAlgoAnalyze = 0;
		plotterData = [[0], [0], [0]];
		plotterFFT = [[0], [0], [0], [0], [0]];
		tuning = Tuning.et12;
		degrees = tuning.semitones;
		root = 0;
		scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning);
		keyVolume = 12.neg.dbamp;
		canalMIDI = 0;
		rangeDBintruments = [-12.dbamp, -3.dbamp];
		rangeFreqintruments = [0, 127];
		quantizationDuree = 100;
		keyboardTranslateBefore = 0;
		stretchDuree = 1;
		rangeDureeintruments = [0, 4];
		dureeMaximumAnalyze = 4;
		fhzFilter = 0;
		ampFilter = 0;
		dureeFilter = 0.01;
		freqBefore = 60.midicps;
		ampBefore = 0;
		flagAlgoAnalyze = 'on';
		flagScaling = 'off';
		flagRoot = 'off';
		flagBPM = 'off';
		oldTempo = 1;
		flagMidiOut = 'off';
		listeFileAnalyze=[];
		listeNameFileAnalyze=[];
		indexWindows = 0;
		userBPM = 1;
		globalDensity = 0.5;
		maximumInstruments = 6; // Warning for Init
		listeBusOff = []; maximumInstruments.do({arg i; listeBusOff = listeBusOff.add(i)});
		listeDataInstruments = [];
		midiOutLo = 1;
		midiOutHi = 16;
		midiRange = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16];
		panSynthLo = 0;
		panSynthHi = 0;
		listeBuffer = [];
		envelopeSynth = [[0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0], [0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125]];
		maximumData = 24;
		ctrlHP1 = 0.5;
		ctrlHP2 = 0.5;
		fadeFX = 0.5;
		loopSound = 0;
		reverseSound = 1;
		offsetSound = 0;
		loopRec = 0;
		flagRec = 0;
		flagSampler = "Sampler+Sound";
		memoryTime = 6;
		dureeAnalyzeOSCMusic = Main.elapsedTime;
		flagRecording = 'off';
		indexInstrumentX = 0.5;
		indexInstrumentY = 0.5;
		jitterIndexInstrumentX = 0.5;
		jitterIndexInstrumentY = 0.5;
		indexSoundX = 0.5;
		indexSoundY = 0.5;
		jitterIndexSoundX = 0.5;
		jitterIndexSoundY = 0.5;
		indexFXX = 0.5;
		indexFXY = 0.5;
		jitterIndexFXX = 0.5;
		jitterIndexFXY = 0.5;
		dureeSample = 1;
		recLevel = 1;
		preLevel = 0;
		flagChord = 'off';
		listAlgorithm = ['Default', 'Probability', 'Euclide', 'Genetic','Kohonen', 'Neural'];
		displayAlgo = "";
		displayIndex = "";
		displayMIDI = "";
		algoLo = 0;
		/*algoHi = listAlgorithm.size - 1;*/
		algoHi = 0;
		jitterControls = 0;
		flagMemory = 'off';
		flagFhzBand = 'off';
		numFhzBand = 3; // Nombre de band de fhz (+1 pour all data) pour trier dans les synth index=0 pour all index=1 pour badnnum 1 etc...
		bandFHZ = Array.fill(numFhzBand, {arg i; [127 / numFhzBand * i, 127 / numFhzBand * i + (127 / numFhzBand )]}).midicps;
		bandFHZ = bandFHZ.reverse;
		bandFHZ = bandFHZ.add([0, 127].midicps);
		bandFHZ = bandFHZ.reverse;
		dataFlux = [];
		dataFlatness = [];
		dataCentroid = [];
		dataEnergy = [];
		dataBPM = [];
		indexDataFlux = [];
		indexDataFlatness = [];
		indexDataCentroid = [];
		indexDataEnergy = [];
		indexDataBPM = [];
		memoryDataFlux = [];
		memoryDataFlatness = [];
		memoryDataCentroid = [];
		memoryDataEnergy = [];
		memoryDataBPM = [];
		dataFreq = [];
		dataAmp = [];
		dataDuree = [];
		indexDataFreq = [];
		indexDataAmp = [];
		indexDataDuree = [];
		memoryDataFreq = [];
		memoryDataAmp = [];
		memoryDataDuree = [];
		lastTime = [];
		// Init Array
		(numFhzBand + 1).do({arg i;
			dataFlux = dataFlux.add([]);
			dataFlatness = dataFlatness.add([]);
			dataCentroid = dataCentroid.add([]);
			dataEnergy = dataEnergy.add([]);
			dataBPM = dataBPM.add([]);
			indexDataFlux = indexDataFlux.add(0);
			indexDataFlatness = indexDataFlatness.add(0);
			indexDataCentroid = indexDataCentroid.add(0);
			indexDataEnergy = indexDataEnergy.add(0);
			indexDataBPM = indexDataBPM.add(0);
			memoryDataFlux = memoryDataFlux.add([]);
			memoryDataFlatness = memoryDataFlatness.add([]);
			memoryDataCentroid = memoryDataCentroid.add([]);
			memoryDataEnergy = memoryDataEnergy.add([]);
			memoryDataBPM = memoryDataBPM.add([]);
			dataFreq = dataFreq.add([]);
			dataAmp = dataAmp.add([]);
			dataDuree = dataDuree.add([]);
			indexDataFreq = indexDataFreq.add(0);
			indexDataAmp = indexDataAmp.add(0);
			indexDataDuree = indexDataDuree.add(0);
			memoryDataFreq = memoryDataFreq.add([]);
			memoryDataAmp = memoryDataAmp.add([]);
			memoryDataDuree = memoryDataDuree.add([]);
			lastTime = lastTime.add(Main.elapsedTime);// Init Time for Analyze;
		});
		rangeSynthBand = [0, 1, 2, 3]; // Band active
		numIndexSynthBand = 0;
		flagBand = [1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0];
		flagVST = 'off';
		flagRecSound = 'off';
		12.do({arg i; channelsSynth = channelsSynth.add(0)});// Channels Synth Ouput en fonction des numFhzBand
		channelsVerb = 0; // Verb ouput channel
		rangeFFT = [0.0, 1.0];
		loopMusic = 1;
		chanelsMidi =  [1,1,2,3,4,5,6,7,8,9,10,11,12];// 13 value 12 band et start for no bands
		// For Kohonen
		kohonenF = HPclassKohonen.new(1,127,1);
		kohonenA = HPclassKohonen.new(1,127,1);
		kohonenD = HPclassKohonen.new(1,127,1);
		// For Genetic
		geneticF = HPclassGenetiques.new(1, 127);
		geneticA = HPclassGenetiques.new(1, 127);
		geneticD = HPclassGenetiques.new(1, 127);
		// For Neural
		neuralFAD = HPNeuralNet.new(3, 1, [9], 3);

		// Audio Out
		listeMasterOut = [
			"Stereo",
			"Rotate2",
			"MultiSpeaker",
			"Ambisonic",
			"Dolby5.1",
		];

		// Orchestra
		synthOrchestra = [
			// Piano
			[
				"MdaPiano",
				"PianoRF",
				"PianoResonz",
				"PianoFreqShift",
			],
			// Piano + PV
			[
				"PianoPV_Add",
				"PianoPV_Mul",
				"PianoPV_MagDiv",
				"PianoPV_BinWipe",
				"PianoPV_RectComb2",
			],
			// Synth
			[
				"SinOsc",
				"Formant",
				"FM",
				"Ring",
				"Saw",
				"Gendy",
				"AnalogString",
			],
			// Synth Stream
			[
				"SinOscStream",
				"FormantStream",
				"FMStream",
				"RingStream",
				"SawStream",
				"GendyStream",
				"AnalogStringStream",
			],
			// Synth Stream Env
			[
				"SinOscStreamEnv",
				"FormantStreamEnv",
				"FMStreamEnv",
				"RingStreamEnv",
				"SawStreamEnv",
				"GendyStreamEnv",
				"AnalogStringStreamEnv",
			],
			// PreBuf
			[
				"PlayBufPreBuf",
				"PlayBufRFPreBuf",
				"PlayBufResonzPreBuf",
				"PlayBufKlankPreBuf",
				"PlayBufLiquidPreBuf",
				"PlayBufElasticPreBuf",
				"TGrainsPreBuf",
				"Warp1PreBuf",
				"MedianPreBuf",
				"LeakDCPreBuf",
				"MedianLeakDCPreBuf",
			],
			// PreBuf Stream
			[
				"PlayBufStreamPreBuf",
				"PlayBufKlankStreamPreBuf",
				"PlayBufLiquidStreamPreBuf",
				"PlayBufElasticStreamPreBuf",
				"TGrainsStreamPreBuf",
				"Warp1StreamPreBuf",
				"GranularStreamPreBuf",
				"DjScratchStreamPreBuf",
				"MedianStreamPreBuf",
				"LeakDCStreamPreBuf",
				"MedianLeakDCStreamPreBuf",
			],
			// PreBuf StreamEnv
			[
				"PlayBufStreamPreBufEnv",
				"PlayBufKlankStreamPreBufEnv",
				"PlayBufLiquidStreamPreBufEnv",
				"PlayBufElasticStreamPreBufEnv",
				"Warp1StreamPreBufEnv",
			],
			// Postbuf
			[
				"BufRdPostBuf",
				"BufRdRFPostBuf",
				"BufRdResonzPostBuf",
				"BufRdKlankPostBuf",
				"BufRdLiquidPostBuf",
				"BufRdElasticPostBuf",
				"Warp1PostBuf",
				"GranularPostBuf",
				"MedianPostBuf",
				"LeakDCPostBuf",
				"MedianLeakDCPostBuf",
			],
			// Postbuf Stream
			[
				"BufRdStreamPostBuf",
				"BufRdKlankStreamPostBuf",
				"BufRdLiquidStreamPostBuf",
				"BufRdElasticStreamPostBuf",
				"Warp1StreamPostBuf",
				"GranularStreamPostBuf",
				"DjScratchStreamPostBuf",
				"MedianStreamPostBuf",
				"LeakDCStreamPostBuf",
				"MedianLeakDCStreamPostBuf",
			],
			// Postbuf StreamEnv
			[
				"BufRdStreamPostBufEnv",
				"BufRdKlankStreamPostBufEnv",
				"BufRdLiquidStreamPostBufEnv",
				"BufRdElasticStreamPostBufEnv",
				"Warp1StreamPostBufEnv",
				"GranularStreamPostBufEnv",
				"DjScratchStreamPostBufEnv",
			],
			// PreBuf + PostBuf PV
			[
				"PV_AddPrePostBuf",
				"PV_MulPrePostBuf",
				"PV_MagDivPrePostBuf",
				"PV_BinWipePrePostBuf",
				"PV_RectComb2PrePostBuf",
			],
			// PreBuf + PostBuf PV Stream
			[
				"PV_AddStreamPrePostBuf",
				"PV_MulStreamPrePostBuf",
				"PV_MagDivStreamPrePostBuf",
				"PV_BinWipeStreamPrePostBuf",
				"PV_RectComb2StreamPrePostBuf",
			],
			// PreBuf + PostBuf PV StreamEnv
			[
				"PV_AddStreamPrePostBufEnv",
				"PV_MulStreamPrePostBufEnv",
				"PV_MagDivStreamPrePostBufEnv",
				"PV_BinWipeStreamPrePostBufEnv",
				"PV_RectComb2StreamPrePostBufEnv",
			],
			// DelayHarmonic
			[
				"DelayHarmonic",
				"RFDelayHarmonic",
				"ResonzDelayHarmonic",
				"KlankDelayHarmonic",
				"LiquidDelayHarmonic",
				"ElasticDelayHarmonic",
				"MedianDelayHarmonic",
				"LeakDCDelayHarmonic",
				"MedianLeakDCDelayHarmonic",
			],
			// Postbuf Stream DelayHarmonic
			[
				"StreamDelayHarmonic",
				"KlankStreamDelayHarmonic",
				"LiquidStreamDelayHarmonic",
				"ElasticStreamDelayHarmonic",
				"MedianStreamDelayHarmonic",
				"LeakDCStreamDelayHarmonic",
				"MedianLeakDCStreamDelayHarmonic",
			],
			// DelayHarmonic StreamEnv
			[
				"StreamDelayHarmonicEnv",
				"KlankStreamDelayHarmonicEnv",
				"LiquidStreamDelayHarmonicEnv",
				"ElasticStreamDelayHarmonicEnv",
			],
			// PreBuf + DelayHarmonic PV
			[
				"PV_AddPreDelayHarmonic",
				"PV_MulPreDelayHarmonic",
				"PV_MagDivPreDelayHarmonic",
				"PV_BinWipePreDelayHarmonic",
				"PV_RectComb2PreDelayHarmonic",
			],
			// PreBuf + DelayHarmonic PV Stream
			[
				"PV_AddStreamPreDelayHarmonic",
				"PV_MulStreamPreDelayHarmonic",
				"PV_MagDivStreamPreDelayHarmonic",
				"PV_BinWipeStreamPreDelayHarmonic",
				"PV_RectComb2StreamPreDelayHarmonic",
			],
			// PreBuf + DelayHarmonic PV StreamEnv
			[
				"PV_AddStreamPreDelayHarmonicEnv",
				"PV_MulStreamPreDelayHarmonicEnv",
				"PV_MagDivStreamPreDelayHarmonicEnv",
				"PV_BinWipeStreamPreDelayHarmonicEnv",
				"PV_RectComb2StreamPreDelayHarmonicEnv",
			],
			// Special
			[
				"Silent",
				"SynthSampler",
				"GranulatorPreBuf",
				"GranulatorPostBuf",
			],
			// Special Stream
			[
				"SilentStream",
				"SynthSamplerStream",
				"GranulatorStreamPreBuf",
				"GranulatorStreamPostBuf",
			],
			// Special StreamEnv
			[
				"SynthSamplerStreamEnv",
			],
		];

		// FX
		fxOrchestra = [
			// Verb + Delay
			[
				"AllpassC",
				"DelayC",
			],
			// Filtre (Low High)
			[
				"BPF",
				"BRF",
				"MoogFF",
				"LPF",
				"RLPF",
				"BLowPass4",
				"HPF",
				"RHPF",
				"BHiPass4",
				"Median",
				"LeakDC",
				"Median+LeakDC",
			],
			// Filtre Resonant
			[
				"Ringz",
				"Resonz",
				"Formlet",
				"CombC",
			],
			// PV
			[
				//"PV_HPshiftDown",
				"PV_MagNoise",
				"PV_MagClip",
				"PV_MagSmooth",
				//"PV_MagSmear",
				"PV_Diffuser",
				"PV_BrickWall",
				"PV_LocalMax",
				"PV_MagSquared",
				"PV_MagBelow",
				"PV_MagAbove",
				"PV_RandComb",
				"PV_MagShift",
				"PV_MagStretch",
				"PV_MagShift+Stretch",
				"PV_BinScramble",
				"PV_BinShift",
				"PV_RectComb",
				"PV_ConformalMap",
				"PV_Compander",
				"PV_SpectralEnhance",
				"PV_MagStretch",
				"PV_MagShift+Stretch",
				"PV_MagFreeze",
				"PV_Cutoff",
			],
			// FX
			[
				"PitchShift",
				"FreqShift",
				"Convolution",
			],
		];

		// Sounds
		soundOrchestra = [
			// Voice
			[
				pathSound +/+ "sounds/Voice/voix.aiff",
				pathSound +/+ "sounds/Voice/Choeur.aiff",
			],
			// String
			[
				pathSound +/+ "sounds/String/Contrebasse1.wav",
				pathSound +/+ "sounds/String/Contrebasse2.wav",
				pathSound +/+ "sounds/String/Violoncelle1.wav",
				pathSound +/+ "sounds/String/Alto1.wav",
				pathSound +/+ "sounds/String/Alto2.wav",
				pathSound +/+ "sounds/String/Violon1.wav",
				pathSound +/+ "sounds/String/cordes.aiff",
				pathSound +/+ "sounds/String/String1.wav",
				pathSound +/+ "sounds/String/String2.wav",
				pathSound +/+ "sounds/String/String3.wav",
				pathSound +/+ "sounds/String/ContrebasseST1.wav",
				pathSound +/+ "sounds/String/AltoST1.wav",
				pathSound +/+ "sounds/String/ViolonST1.wav",
				pathSound +/+ "sounds/String/StringST1.wav",
				pathSound +/+ "sounds/String/AltoST1.wav",
				pathSound +/+ "sounds/String/ViolonST1.wav",
				pathSound +/+ "sounds/String/ContrebassePizz1.wav",
				pathSound +/+ "sounds/String/ContrebassePizz2.wav",
				pathSound +/+ "sounds/String/pizzacato.aiff",
				pathSound +/+ "sounds/String/ViolonPizz1.wav",
			],
			// Woodwind
			[
				pathSound +/+ "sounds/Woodwind/clarinette basse.aiff",
				pathSound +/+ "sounds/Woodwind/clarinette.aiff",
				pathSound +/+ "sounds/Woodwind/hautbois.aiff",
				pathSound +/+ "sounds/Woodwind/flute.aiff",
			],
			// Brass
			[
				pathSound +/+ "sounds/Brass/Tuba Sustain P.aiff",
				pathSound +/+ "sounds/Brass/Tuba Sustain Soft.aiff",
				pathSound +/+ "sounds/Brass/Tuba Sustain MF.aiff",
				pathSound +/+ "sounds/Brass/Tuba Sustain F.aiff",
				pathSound +/+ "sounds/Brass/Trombone Sustain MF.aiff",
				pathSound +/+ "sounds/Brass/Trombone Sustain F.aiff",
				pathSound +/+ "sounds/Brass/Cor Sustain MF.aiff",
				pathSound +/+ "sounds/Brass/Cor Sustain F.aiff",
				pathSound +/+ "sounds/Brass/French Horn Sustain MF.aiff",
				pathSound +/+ "sounds/Brass/French Horn Sustain F.aiff",
				pathSound +/+ "sounds/Brass/Tuba Attack F.aiff",
				pathSound +/+ "sounds/Brass/Trombone Attack F.aiff",
				pathSound +/+ "sounds/Brass/Trombone Attack FFF.aiff",
				pathSound +/+ "sounds/Brass/French Horn Attack F.aiff",
			],
			// Keyboard
			[
				pathSound +/+ "sounds/Keyboard/piano m.aiff",
				pathSound +/+ "sounds/Keyboard/piano f.aiff",
				pathSound +/+ "sounds/Keyboard/fender rhode.aiff",
				pathSound +/+ "sounds/Keyboard/glockenspiel.aiff",
			],
			// Percussion
			[
				pathSound +/+ "sounds/Percussion/HPbassdrum.aiff",
				pathSound +/+ "sounds/Percussion/HPsnare.aiff",
				pathSound +/+ "sounds/Percussion/HPsnareRoll.aiff",
				pathSound +/+ "sounds/Percussion/HPrimshot.aiff",
				pathSound +/+ "sounds/Percussion/tom.aiff",
				pathSound +/+ "sounds/Percussion/tomHigh.aiff",
				pathSound +/+ "sounds/Percussion/HPhihat.aiff",
				pathSound +/+ "sounds/Percussion/CymbaleLow.aiff",
				pathSound +/+ "sounds/Percussion/bloc chinois.aiff",
				pathSound +/+ "sounds/Percussion/tambourin.aiff",
				pathSound +/+ "sounds/Percussion/crapeau.aiff",
				pathSound +/+ "sounds/Percussion/crecelle.aiff",
				pathSound +/+ "sounds/Percussion/batonDePluie.aiff",
			],
			// Special
			[
				pathSound +/+ "sounds/Special/MicroSound1.aiff",
				pathSound +/+ "sounds/Special/MicroSine.aiff",
				pathSound +/+ "sounds/Special/MicroSquare.aiff",
				pathSound +/+ "sounds/Special/MicroSaw.aiff",
				pathSound +/+ "sounds/Special/MicroNoise.aiff",
				pathSound +/+ "sounds/Special/Pluck.aiff",
				pathSound +/+ "sounds/Special/RissetPercu.aiff",
				pathSound +/+ "sounds/Special/CordeMetal.aiff",
				pathSound +/+ "sounds/Special/Roach.wav",
				pathSound +/+ "sounds/Special/Synth 1.wav",
				pathSound +/+ "sounds/Special/Synth 2.wav",
			],
		];

		//Write Init Synth Sounds FX
		if(File.exists(pathData ++ "Synth.scd").not) {
			File(pathData ++ "Synth.scd", "w").write(synthOrchestra.asCompileString).close};
		if(File.exists(pathData ++ "Sounds.scd").not) {
			File(pathData ++ "Sounds.scd", "w").write(soundOrchestra.asCompileString).close};
		if(File.exists(pathData ++ "FX.scd").not) {
			File(pathData ++ "FX.scd", "w").write(fxOrchestra.asCompileString).close};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		// Init Synth Sounds FX
		synthOrchestra = File(pathData ++ "Synth.scd","r").readAllString.interpret;
		soundOrchestra = File(pathData ++ "Sounds.scd","r").readAllString.interpret;
		fxOrchestra = File(pathData ++ "FX.scd","r").readAllString.interpret;

		// Collect Folders
		fonctionCollectFolders = {
			// Collect all Preset
			foldersToScanAll = PathName.new(pathData).files.collect{ |path| var file;
				file = path.fileName;
				if(file.find("Preset") == 0 or: {file.find("preset") == 0}, {file});
			};
			foldersToScanAll = foldersToScanAll.reject({arg item; item == nil});
			// Collect preset
			foldersToScanPreset = foldersToScanAll.collect{ |file|
				if(file.find("Preset") == 0 or: {file.find("preset") == 0}, {file});
			};
			foldersToScanPreset = foldersToScanPreset.reject({arg item; item == nil});
		};
		fonctionCollectFolders.value;

		// Load Preset
		fonctionLoadPreset = {arg allData, index=0;
			var bpm, autoRoot, flagPlay=0;
			// Load All Windows
			listeWindows.do({arg window, data;
				data = allData.at(index);
				window.view.children.do({arg view, item;
					// Check BPM
					// Setup GUI Value
					if(item == 34, {bpm = data.at(item).at(2).at(2)});
					if(item == 65, {autoRoot = data.at(item).at(1)});
					// Check Sliders don't touch
					// Setup GUI Value
					if(index == 0 and: {item == 0 or: {item == 1} or: {item == 2} or: {item == 3} or: {item == 4} or: {item == 5} or: {item == 6} or: {item == 7}} or: {item == 8} or: {item == 9}, {nil},
						{
							// View or CompositeView
							if(data.at(item).at(0) == "a View" or: {data.at(item).at(0) == "a CompositeView"} or: {data.at(item).at(0) == "a QView"} or: {data.at(item).at(0) == "a SCCompositeView"}, {
								if(data.at(item).at(1) == "a StaticText" or: {data.at(item).at(1) == "a QStaticText"} or: {data.at(item).at(1) == "a SCStaticText"}, {view.children.at(1).valueAction_(data.at(item).at(2).at(1))});
								if(data.at(item).at(1) == "a TextField" or: {data.at(item).at(1) == "a QTextField"} or: {data.at(item).at(1) == "a SCTextField"}, {view.children.at(1).valueAction_(data.at(item).at(2).at(1))});
								if(data.at(item).at(1) == "a Slider" or: {data.at(item).at(1) == "a QSlider"} or: {data.at(item).at(1) == "a SCSlider"}, {view.children.at(2).valueAction_(data.at(item).at(2).at(2))});
								if(data.at(item).at(1) == "a RangeSlider" or: {data.at(item).at(1) == "a QRangeSlider"} or: {data.at(item).at(1) == "a SCRangeSlider"}, {view.children.at(2).activeLo_(0); view.children.at(2).activeHi_(1);
									view.children.at(2).activeLo_(data.at(item).at(2).at(2).at(0)); view.children.at(2).activeHi_(data.at(item).at(2).at(2).at(1))});
								if(data.at(item).at(1) == "a Knob" or: {data.at(item).at(1) == "a QKnob"} or: {data.at(item).at(1) == "a SCKnob"}, {view.children.at(2).valueAction_(data.at(item).at(2).at(2))});
								if(data.at(item).at(0)  == "a UserView" or: {data.at(item).at(0)  == "an UserView"} or: {data.at(item).at(0) == "a QUserView"} or: {data.at(item).at(0) == "a SCUserView"},
									{nil});
							});
							// StaticText
							if(data.at(item).at(0)  == "a StaticText" or: {data.at(item).at(0) == "a QStaticText"} or: {data.at(item).at(0) == "a SCStaticText"},
								{view.string_(data.at(item).at(1));
							});
							// TextView
							if(data.at(item).at(0)  == "a TextView" or: {data.at(item).at(0) == "a QTextView"} or: {data.at(item).at(0) == "a SCTextView"},
								{view.string = (data.at(item).at(1));
							});
							// QPopUpMenu + QButton + EnvelopeView
							if(data.at(item).at(0)  == "a PopUpMenu" or: {data.at(item).at(0) == "a EnvelopeView"} or: {data.at(item).at(0) == "an EnvelopeView"} or: 	{data.at(item).at(0) == "a Button"} or: {data.at(item).at(0) == "a QPopUpMenu"} or: {data.at(item).at(0) == "a QEnvelopeView"} or: 	{data.at(item).at(0) == "a QButton"} or: {data.at(item).at(0) == "a SCPopUpMenu"} or: {data.at(item).at(0) == "a SCEnvelopeView"} or: {data.at(item).at(0) == "a SCButton"},
								{view.valueAction_(data.at(item).at(1))});
							// NumberBox
							if(data.at(item).at(0)  == "a NumberBox" or: {data.at(item).at(0) == "a QNumberBox"} or: {data.at(item).at(0) == "a SCNumberBox"},
								{view.valueAction_(data.at(item).at(1))});
							// QSoundFileView
							if(data.at(item).at(0)  == "a SoundFileView" or: {data.at(item).at(0) == "a QSoundFileView"} or: {data.at(item).at(0) == "a SCSoundFileView"},
								{view.setSelection(0, data.at(item).at(1).at(0));
							});
							// Q2DSlider (special case)
							if(data.at(item).at(0)  == "a Slider2D" or: {data.at(item).at(0) == "a QSlider2D"} or: {data.at(item).at(0) == "a SCSlider2D"},
								{view.setXYActive(data.at(item).at(1), data.at(item).at(2))});
							// UserView
							if(data.at(item).at(0)  == "a UserView" or: {data.at(item).at(0)  == "an UserView"} or: {data.at(item).at(0) == "a QUserView"} or: {data.at(item).at(0) == " a SCUserView"},
								{nil});
							// MultiSliderView
							if(data.at(item).at(0) == "a MultiSliderView" or: {data.at(item).at(0)  == "a QMultiSliderView"} or: {data.at(item).at(0) == "a SCMultiSliderView"},
								{view.valueAction = data.at(item).at(1)});
					});
				});
				index = index + 1;
			});
			// SET GOOD BPM
			// Setup GUI Value
			windowEar.view.children.at(34).children.at(2).valueAction_(bpm);
			// Setup AutoRoot
			windowEar.view.children.at(65).valueAction_(autoRoot);
			// synthOrchestra
			synthOrchestra = allData.at(index);
			index = index + 1;
			// soundOrchestra
			if(allData.at(index) != soundOrchestra, {
				"... LOAD SOUNDORCHESTRA... !!!".postln;
				soundOrchestra = allData.at(index);
				s.bind{
					if(startSystem.value == 0, {flagPlay = 0}, {startSystem.valueAction_(0); flagPlay = 1});
					s.sync;
					fonctionLoadSoundOrchestra.value(soundOrchestra);
					s.sync;
					if(flagPlay == 1, {startSystem.valueAction_(1)});
					s.sync;
				};
			}, {soundOrchestra = allData.at(index)});
			index = index + 1;
			// fxOrchestra
			fxOrchestra = allData.at(index);
			index = index + 1;
			// MemoryData
			memoryDataFlux = allData.at(index).at(0);
			memoryDataFlatness = allData.at(index).at(1);
			memoryDataCentroid = allData.at(index).at(2);
			memoryDataEnergy = allData.at(index).at(3);
			memoryDataBPM = allData.at(index).at(4);
			memoryDataFreq = allData.at(index).at(5);
			memoryDataAmp = allData.at(index).at(6);
			memoryDataDuree = allData.at(index).at(7);
			// Data
			dataFlux = allData.at(index).at(0);
			dataFlatness = allData.at(index).at(1);
			dataCentroid = allData.at(index).at(2);
			dataEnergy = allData.at(index).at(3);
			dataBPM = allData.at(index).at(4);
			dataFreq = allData.at(index).at(5);
			dataAmp = allData.at(index).at(6);
			dataDuree = allData.at(index).at(7);
		};

		// Save Preset
		fonctionSavePreset = {arg windows, data=[], allData=[];
			// All Windows
			windows.do({arg window;
				data=[];
				// Save views values
				window.view.children.do({arg view, arrayData=[], subArrayData=[], subType=nil;
					// View or CompositeView
					arrayData=[]; subArrayData=[];
					if(view.asString == "a View" or: {view.asString == "a CompositeView"} or: {view.asString == "a QCompositeView"} or: {view.asString == "a QView"} or: {view.asString == "a SCCompositeView"},
						{view.children.do({arg subView;
							if(subView.asString == "a StaticText" or: {subView.asString == "a SCStaticText"} or: {subView.asString == "a QStaticText"}, {arrayData = arrayData.add(subView.string)});
							if(subView.asString == "a TextField" or: {subView.asString == "a SCTextField"} or: {subView.asString == "a QTextField"}, {arrayData = arrayData.add(subView.string); subType = subView.asString});
							if(subView.asString == "a Slider" or: {subView.asString == "a SCSlider"} or: {subView.asString == "a QSlider"}, {arrayData=arrayData.add(subView.value); subType = 	subView.asString});
							if(subView.asString == "a RangeSlider" or: {subView.asString == "a SCRangeSlider"} or: {subView.asString == "a QRangeSlider"}, {subArrayData=subArrayData.add(subView.lo);subArrayData=subArrayData.add(subView.hi); arrayData=arrayData.add(subArrayData); subType = subView.asString});
							if(subView.asString == "a Knob" or: {subView.asString == "a SCKnob"} or: {subView.asString == "a QKnob"}, {subArrayData=subArrayData.add(subView.value); arrayData=arrayData.add(subArrayData); subType = subView.asString});
							if(subView.asString == "a NumberBox" or: {subView.asString == "a SCNumberBox"} or: {subView.asString == "a QNumberBox"}, {arrayData=arrayData.add(subView.value)});
							if(subView.asString == "a UserView" or: {subView.asString == "an UserView"} or: {subView.asString == "a SCUserView"} or: {subView.asString == "a QUserView"},
								{data = data.add([subView.asString, nil])});
						});
						data = data.add([view.asString, subType, arrayData]);
					});
					// StaticText
					if(view.asString == "a StaticText" or: {view.asString == "a QStaticText"} or: {view.asString == "a SCStaticText"},
						{data = data.add([view.asString, view.string])});
					// TextView
					if(view.asString == "a TextView" or: {view.asString == "a SCTextView"} or: {view.asString == "a QTextView"},
						{data = data.add([view.asString, view.string])});
					// PopUpMenu + Button
					if(view.asString == "a PopUpMenu" or: {view.asString == "a EnvelopeView"} or: {view.asString == "an EnvelopeView"} or: {view.asString == "a Button"} or: {view.asString == "a SCPopUpMenu"} or: {view.asString == "a QPopUpMenu"} or: {view.asString == "a SCEnvelopeView"} or: {view.asString == "a QEnvelopeView"} or: {view.asString == "a SCButton"} or: {view.asString == "a QButton"},
						{data = data.add([view.asString, view.value])});
					// NumberBox
					if(view.asString == "a NumberBox" or: {view.asString == "a QNumberBox"} or: {view.asString == "a SCNumberBox"},
						{data = data.add([view.asString, view.value])});
					// SoundFileView
					if(view.asString == "a SoundFileView" or: {view.asString == "a SCSoundFileView"} or: {view.asString == "a QSoundFileView"},
						{data = data.add([view.asString, view.selection(0)])});
					// 2DSlider (Special Case)
					if(view.asString == "a Slider2D" or: {view.asString == "a SCSlider2D"} or: {view.asString == "a QSlider2D"},
						{data = data.add([view.asString, view.x, view.y])});
					// UserView
					if(view.asString == "a UserView" or: {view.asString == "an UserView"} or: {view.asString == "a SCUserView"} or: {view.asString == "a QUserView"},
						{data = data.add([view.asString, nil])});
					// MultiSliderView
					if(view.asString == "a MultiSliderView" or: {view.asString == "a SCMultiSliderView"} or: {view.asString == "a QMultiSliderView"},
						{data = data.add([view.asString, view.value])});
				});
				// Add Window Data to allData Preset
				allData = allData.add(data);
			});
			// Add synthOrchestra soundOrchestra fxOrchestra memoryData
			allData = allData.add(synthOrchestra);
			allData = allData.add(soundOrchestra);
			allData = allData.add(fxOrchestra);
			allData = allData.add([memoryDataFlux, memoryDataFlatness, memoryDataCentroid, memoryDataEnergy, memoryDataBPM, memoryDataFreq, memoryDataAmp, memoryDataDuree]);
			// Sortie Fonction Save Preset
			allData.value;
		};

		//Fonction Load file for analyze
		fonctionLoadFileForAnalyse = {arg p, f, d;
			s.bind{
				f = SoundFile.new;
				f.openRead(p);
				if(f.numChannels == 1,
					{d= FloatArray.newClear(f.numFrames * 2);
						f.readData(d);
						Post << "Loading sound mono for analyze" << p << Char.nl;
						d = Array.newFrom(d).stutter(2) / 2;
						bufferFile=Buffer.loadCollection(s, d, 2, action: {arg buf; Post << "Finished" << Char.nl});
					},
					{Post << "Loading sound stereo for analyze" << p << Char.nl;
						bufferFile=Buffer.readChannel(s, p, channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
				});
				f.close;
				synthFileIn.set(\trigger, 0);
				s.sync;
				synthFileIn.set(\bufferplay, bufferFile);
				s.sync;
				synthFileIn.set(\trigger, 1);
				s.sync;
			};
			bufferFile.value;
		};

		// Fonction Load Sounds for Sampler
		fonctionLoadSoundOrchestra = {arg listeSound;
			// Free Buffer
			listeBuffer.soloArray.do({arg buffer; buffer.free});
			s.sync;
			listeBuffer=[] ;
			listeSound.do({arg arraySound, file, rawData, collect=[];
				arraySound.do({arg path;
					path = PathName.new(path);
					path = path.fileName;//Name of soundFile
					path = "mdfind -name" + path;
					path = Pipe.new(path, "r");
					rawData = path.getLine;// get the first line
					path.close;
					path = rawData;// New Path
					file = SoundFile.new;
					s.sync;
					file.openRead(path);
					s.sync;
					if(file.numChannels == 1,
						{Post << "Loading mono sound" << " " << path << Char.nl;
							collect = collect.add(Buffer.read(s, path, action: {arg buf; Post << "Finished" << Char.nl}));
							s.sync;
						},
						{rawData= FloatArray.newClear(file.numFrames * 2);
							s.sync;
							file.readData(rawData);
							s.sync;
							rawData = Array.newFrom(rawData);
							s.sync;
							Post << "Loading stereo sound" << " " << path << Char.nl;
							rawData = rawData.unlace(2).sum / 2;
							s.sync;
							collect = collect.add(Buffer.loadCollection(s, rawData, 1, action: {arg buf; Post << "Finished" << Char.nl}).path = path);
							s.sync;
					});
					file.close;
					s.sync;
				});
				listeBuffer = listeBuffer.add(collect);
			});
		};

		// Fonction pour Recording
		fonctionRecOn={
			if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
			if(flagRecording == 'off', {
				flagRecording = 'on';
				s.bind{
					s.recChannels_(recChannels);
					s.sync;
					s.prepareForRecord("~/Music/SuperCollider Recordings/".standardizePath ++ "Density_" ++ Date.localtime.stamp ++ ".aiff");
					s.sync;
					s.record;
					s.sync;
				};
			});
		};

		fonctionRecOff={
			if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec On
			flagRecording = 'off';
			s.stopRecording;
		};

		fonctionRecPause={
			if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec On
			if(startSystem.value == 1, {
				if(flagRecording == 'on', {s.pauseRecording; flagRecording = 'pause'},{s.record; flagRecording = 'on'});
			});
		};

		fonctionBand = {arg band;
			rangeSynthBand = [];
			for(0, numFhzBand,
				{arg index;
					if(flagBand.at(index) == 1, {
						rangeSynthBand = rangeSynthBand.add(index);
					});
			});
		};

		// Run the Soft
		this.run;

	}

	// Soft Density

	run {

		"Please Wait... Loading Density...".postln;

		// OSCFunc Score
		OSCFunc.newMatching({arg msg, time, addr, recvPort;

			var array, cmd = 'on', number, file, item = 0;

			msg.removeAt(0);
			msg.postcs;

			while({cmd != nil},
				{
					cmd = msg[item].postln;
					if(cmd == 'all' or: {cmd == 'density'},
						{
							cmd = msg[item+1].postln;
							// Preset
							if(cmd == 'preset',
								{
									number = msg[item+2].asInteger.postln;
									{
										if(File.exists(pathData ++ "Preset" + number.value.asString ++ ".scd"), {
											listeDataInstruments.do({arg data, index;
												data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
											});
											windowEar.name = "Density" + " | " + "Preset" + number.asString;
											file=File(pathData ++ "Preset" + number.value.asString ++ ".scd", "r");
											fonctionLoadPreset.value(file.readAllString.interpret);
											file.close;
										}, {"cancelled".postln});
									}.defer;
							});
							// Stop
							if(cmd == 'stop', {
								{
									startSystem.valueAction_(0);
								}.defer;
							});
							// Start
							if(cmd == 'start', {
								{
									startSystem.valueAction_(1);
								}.defer;
							});
					});
					item = item + 3;
					cmd = msg[item];
			});
		}, \score, recvPort: NetAddr.langPort);

		// Run Soft
		s.waitForBoot({

			// Init SynthDef
			this.initSynthDef;
			s.sync;

			// Init Tempo System
			tempoClock = TempoClock.new;
			tempoClock.schedAbs(tempoClock.nextBar,{tempoClock.beatsPerBar_(1)});

			"Please Wait... Loading Density... Sending SynthDef on s... Loading Sounds...".postln;

			// Load Sound Orchestra
			fonctionLoadSoundOrchestra.value(soundOrchestra);
			s.sync;
			// Load file for analyze
			fonctionLoadFileForAnalyse.value(pathSound +/+ "sounds/a11wlk01-44_1.aiff");
			s.sync;

			// OSC Setting
			serveurAdresse = s.addr; // Adresse s -> NetAddr(0.0.0.0, 0)
			masterAppAddr = NetAddr.localAddr;
			slaveAppAddr = NetAddr.localAddr;
			oscStateFlag = 'off';

			ardourOSC = NetAddr("127.0.0.1", 3819);// Ardour's port number

			// Group
			groupeAnalyse = ParGroup.new(s, \addToTail);
			groupeRecBuffer = ParGroup.new(s, \addToTail);
			groupeSynth = ParGroup.new(s, \addToTail);
			groupeFX = ParGroup.new(s, \addToTail);
			groupeMasterOut = ParGroup.new(s, \addToTail);
			groupeVerb = ParGroup.new(s, \addToTail);
			groupeVST = ParGroup.new(s, \addToTail);
			groupeLimiter = ParGroup.new(s, \addToTail);

			// Bus OSC Data
			busAnalyzeIn = Bus.audio(s, 1);
			busRecAudioIn = Bus.audio(s, 1);

			// Bus OSC Data
			// Init Bus Array max 12
			(12 + 1).do({arg i;
				busOSCflux = busOSCflux.add(Bus.control(s, 1));
				busOSCflatness = busOSCflatness.add(Bus.control(s, 1));
				busOSCcentroid = busOSCcentroid.add(Bus.control(s, 1));
				busOSCenergy = busOSCenergy.add(Bus.control(s, 1));
				busOSCbpm = busOSCbpm.add(Bus.control(s, 1));
				busOSCfreq = busOSCfreq.add(Bus.control(s, 1));
				busOSCamp = busOSCamp.add(Bus.control(s, 1));
				busOSCduree = busOSCduree.add(Bus.control(s, 1));
			});

			// Create Bus pour instruments (max 32)
			32.do({arg i; busSynthInOut = busSynthInOut.add(Bus.audio(s, 1))});
			s.sync;

			// Synth AudioIn
			synthAudioIn = Synth.newPaused("Density AudioIn",
				[\in, 0, 'busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Synth play file
			synthFileIn = Synth.newPaused("Density FileIn",
				[\bufferplay, bufferFile, 'busIn', busAnalyzeIn, \busRec, busRecAudioIn, \volume, 0], groupeAnalyse, \addToTail);
			s.sync;

			// Synth audio analyze FFT
			synthAnalyseFFT = Synth.newPaused("OSC Density FFT",
				['busIn', busAnalyzeIn, \speed, 24], groupeAnalyse, \addToTail); // 24 * each second
			s.sync;

			// Synth audio analyze Onsets
			synthAnalyseOnsets = Synth.newPaused("OSC Density Onsets",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Synth audio analyze Pitch
			synthAnalysePitch = Synth.newPaused("OSC Density Pitch",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Synth audio analyze Pitch Version 2
			synthAnalysePitch2 = Synth.newPaused("OSC Density Pitch2",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Synth audio analyze KeyTrack
			synthAnalyseKeyTrack = Synth.newPaused("OSC Density KeyTrack",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Synth Keyboard
			synthKeyboard = Synth.newPaused("OSC Density Keyboard",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// MIDI Keyboard
			synthMIDI = Synth.newPaused("OSC Density MIDI",
				['busIn', busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Create synth RecAudioBus for Analyze
			synthAnalyzeAudioIn = Synth.newPaused("AnalyzeAudioIn", [\in, 0, \busIn, busAnalyzeIn], groupeAnalyse, \addToTail);
			s.sync;

			// Create synth RecAudioBus for Buffer Synth
			synthRecAudioIn = Synth.newPaused("RecAudioIn", [\in, 0, \busIn, busRecAudioIn], groupeAnalyse, \addToTail);
			s.sync;

			//Init EndProcessing

			gVerb = Synth.newPaused("GVerb" + typeMasterOut, [\out, channelsVerb, \xFade, 0, \panLo, 0, \panHi, 0, \drylevel, 0, \earlylevel, 0, \taillevel, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			freeVerb = Synth.newPaused("FreeVerb" + typeMasterOut, [\out, channelsVerb, \xFade, 0, \panLo, 0, \panHi, 0, \drylevel, 0, \earlylevel, 0, \taillevel, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			allPass = Synth.newPaused("Allpass" + typeMasterOut, [\out, channelsVerb, \xFade, 0, \panLo, 0, \panHi, 0, \drylevel, 0, \earlylevel, 0, \taillevel, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			jpVerb = Synth.newPaused("JPverb" + typeMasterOut, [\out, channelsVerb, \xFade, 0, \panLo, 0, \panHi, 0, \drylevel, 0, \earlylevel, 0, \taillevel, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
			s.sync;

			synthVST = Synth.newPaused("VST"+ typeMasterOut, [\xFade, 0.5, \panLo, 0, \panHi, 0, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
			fxVST = VSTPluginController(synthVST);
			s.sync;

			// Init GUI
			this.createGUI;
			s.sync;

			// Init Start GUI
			// Setup GUI Value
			windowEar.view.children.at(7).enabled_(false);// Offset file
			windowEar.view.children.at(8).enabled_(false);// db file
			windowEar.view.children.at(12).enabled_(false);
			windowEar.view.children.at(16).enabled_(false);
			windowEar.view.children.at(17).enabled_(false);
			windowEar.view.children.at(65).enabled_(false);
			windowEar.view.children.at(84).valueAction_(0);// No Tuning
			windowEar.view.children.at(85).enabled_(false);
			windowEar.view.children.at(86).enabled_(false);
			windowEar.view.children.at(70).valueAction_(1);
			windowEar.view.children.at(71).valueAction_(1);
			windowEar.view.children.at(72).valueAction_(1);
			windowEar.view.children.at(73).valueAction_(1);
			windowEar.view.children.at(70).enabled_(false);
			windowEar.view.children.at(71).enabled_(false);
			windowEar.view.children.at(72).enabled_(false);
			windowEar.view.children.at(73).enabled_(false);
			windowEar.view.children.at(74).enabled_(false);
			windowEar.view.children.at(75).enabled_(false);
			windowEar.view.children.at(76).enabled_(false);
			windowEar.view.children.at(77).enabled_(false);
			windowEar.view.children.at(78).enabled_(false);
			windowEar.view.children.at(79).enabled_(false);
			windowEar.view.children.at(80).enabled_(false);
			windowEar.view.children.at(81).enabled_(false);
			windowEar.view.children.at(82).enabled_(false);

			windowEar.name = "Density" + typeMasterOut;
			windowGVerb.name = "Reverb" + typeMasterOut;
			windowVST.name = "VST" + typeMasterOut;

			// Flux spectral = flux (0 - de changement | 1 + de changement)
			// Entropie du son  = flatness (0 sinus | 1 whiteNoise)
			// (Flatness > 0 | Flux <= 1)
			// Brightness of signal = centroid ( freq en hertz)
			// Freq max energie spectral = energy (freq en hertz)

			//  OSC Data FFT
			oscFFT = OSCFunc.newMatching({arg msg, time, addr, recvPort, centroid=0, flatness=0, energy=0, flux=0, bpm=0;
				var data;
				if(msg.at(2) == 5,
					{
						flux = msg.at(3);
						flatness = msg.at(4);
						centroid = msg.at(5);
						energy = msg.at(6);
						bpm = msg.at(7);
						// Normalize
						flux = flux * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0);
						flatness = flatness * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0);
						centroid = (centroid / 12544 * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0) * 12544).clip(20, 12544);
						energy = (energy / 12544 * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0) * 12544).clip(20, 12544);
						// Set Bus Flux
						busOSCflux.at(0).set(flux);
						if(maximumData > dataFlux.at(0).size,
							{
								dataFlux.put(0, dataFlux.at(0).add(flux));
							},
							{
								if(dataFlux.at(0).size <= indexDataFlux.at(0), {indexDataFlux.put(0, 0)});
								dataFlux.put(0, dataFlux.at(0).wrapPut(indexDataFlux.at(0), flux));
								indexDataFlux.put(0, indexDataFlux.at(0) + 1);
						});
						// Set BusFlatness
						busOSCflatness.at(0).set(flatness);
						if(maximumData > dataFlatness.at(0).size,
							{
								dataFlatness.put(0, dataFlatness.at(0).add(flatness));
							},
							{
								if(dataFlatness.at(0).size <= indexDataFlatness.at(0), {indexDataFlatness.put(0, 0)});
								dataFlatness.put(0, dataFlatness.at(0).wrapPut(indexDataFlatness.at(0), flatness));
								indexDataFlatness.put(0, indexDataFlatness.at(0) + 1);
						});
						// Set Bus Centroid
						busOSCcentroid.at(0).set(energy);
						if(maximumData > dataCentroid.at(0).size,
							{
								dataCentroid.put(0, dataCentroid.at(0).add(centroid));
							},
							{
								if(dataCentroid.at(0).size <= indexDataCentroid.at(0), {indexDataCentroid.put(0, 0)});
								dataCentroid.put(0, dataCentroid.at(0).wrapPut(indexDataCentroid.at(0), centroid));
								indexDataCentroid.put(0, indexDataCentroid.at(0) + 1);
						});
						// Set Bus Energy
						busOSCenergy.at(0).set(energy);
						if(maximumData > dataEnergy.at(0).size,
							{
								dataEnergy.put(0, dataEnergy.at(0).add(energy));
							},
							{
								if(dataEnergy.at(0).size <= indexDataEnergy.at(0), {indexDataEnergy.put(0, 0)});
								dataEnergy.put(0, dataEnergy.at(0).wrapPut(indexDataEnergy.at(0), energy));
								indexDataEnergy.put(0, indexDataEnergy.at(0) + 1);
						});
						// Set Bus BPM
						busOSCbpm.at(0).set(bpm);
						if(maximumData > dataBPM.at(0).size,
							{
								dataBPM.put(0, dataBPM.at(0).add(bpm));
							},
							{
								if(dataBPM.at(0).size <= indexDataBPM.at(0), {indexDataBPM.put(0, 0)});
								dataBPM.put(0, dataBPM.at(0).wrapPut(indexDataBPM.at(0), bpm));
								indexDataBPM.put(0, indexDataBPM.at(0) + 1);
						});
						// Plot Data
						plotDataFFT.value(flux, flatness, centroid, energy, bpm);
				}, {nil});
			}, '/Density_FFT_Data', serveurAdresse);

			freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
			(numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});

			// OSC Music Data
			oscMusic = OSCFunc.newMatching({arg msg, time, addr, recvPort, freq=0, amp=0, timer=1, duree=0;
				var data, dureeDisplay, indexFhz, indexBand, freqNew, ampNew, freqStream, ampStream, dureeStream;
				// Music
				if(msg.at(2) == 3,
					{
						indexFhz = 0;
						indexBand = 0;
						freq=msg.at(3);
						amp=msg.at(4);
						timer = msg.at(5); // Duree de l'algo
						duree =  time - lastTime.at(0);
						dureeDisplay = time - lastTime.at(0);
						// Setup Data
						if(duree > dureeMaximumAnalyze or: {duree > memoryTime}, {
							freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
							(numFhzBand + 1).do({arg i; lastTime.put(i, time)});
						},
						{
							if(abs(freq.cpsmidi - freqBefore.cpsmidi) >= fhzFilter and: {abs(amp.ampdb - ampBefore.ampdb) >= ampFilter} and: {abs(duree - lastTime.at(0)) >= dureeFilter}, //and: {duree >= dureeFilter},
								{
									if(freqTampon !=nil and: {ampTampon != nil},
										{
											freqNew = freqTampon; ampNew = ampTampon;
											freqBefore = freqTampon; ampBefore = ampTampon;
											lastTime.put(0, time);
											// Set All Data
											// Freq
											busOSCfreq.at(0).set(freqNew);
											if(maximumData > dataFreq.at(0).size,
												{
													dataFreq.put(0, dataFreq.at(0).add(freqNew));
												},
												{
													if(dataFreq.at(0).size <= indexDataFreq.at(0), {indexDataFreq.put(0, 0)});
													dataFreq.put(0, dataFreq.at(0).wrapPut(indexDataFreq.at(0), freqNew));
													indexDataFreq.put(0, indexDataFreq.at(0) + 1);
											});
											// Amp
											// Set Bus
											busOSCamp.at(0).set(ampNew);
											// Add DataAmp
											if(maximumData > dataAmp.at(0).size,
												{
													dataAmp.put(0, dataAmp.at(0).add(ampNew));
												},
												{
													if(dataAmp.at(0).size <= indexDataAmp.at(0), {indexDataAmp.put(0, 0)});
													dataAmp.put(0, dataAmp.at(0).wrapPut(indexDataAmp.at(0), ampNew));
													indexDataAmp.put(0, indexDataAmp.at(0) + 1);
											});
											// Duree
											// Set Bus
											busOSCduree.at(0).set(duree);
											// Add DataDuree
											if(maximumData > dataDuree.at(0).size,
												{
													dataDuree.put(0, dataDuree.at(0).add(duree));
												},
												{
													if(dataDuree.at(0).size <= indexDataDuree.at(0), {indexDataDuree.put(0, 0)});
													dataDuree.put(0, dataDuree.at(0).wrapPut(indexDataDuree.at(0), duree));
													indexDataDuree.put(0, indexDataDuree.at(0) + 1);
											});
											//
											// Dispatch Band FHZ
											//
											for(1, numFhzBand, {arg i;
												if(freqNew > bandFHZ.at(i).at(0) and: {freqNew < bandFHZ.at(i).at(1)}, {
													duree = time - lastTime.at(i);
													if(duree <= dureeMaximumAnalyze and: {duree < memoryTime}, {
														indexFhz = i;
														indexBand = i;
														// Set Buses
														// Freq
														busOSCfreq.at(i).set(freqNew);
														// Add Data
														if(maximumData > dataFreq.at(i).size,
															{
																dataFreq.put(i, dataFreq.at(i).add(freqNew));
															},
															{
																if(dataFreq.at(i).size <= indexDataFreq.at(i), {indexDataFreq.put(i, 0)});
																dataFreq.put(i, dataFreq.at(i).wrapPut(indexDataFreq.at(i), freqNew));
																indexDataFreq.put(i, indexDataFreq.at(i) + 1);
														});
														// Amp
														// Set Bus
														busOSCamp.at(i).set(ampNew);
														// Add DataAmp
														if(maximumData > dataAmp.at(i).size,
															{
																dataAmp.put(i, dataAmp.at(i).add(ampNew));
															},
															{
																if(dataAmp.at(i).size <= indexDataAmp.at(i), {indexDataAmp.put(i, 0)});
																dataAmp.put(i, dataAmp.at(i).wrapPut(indexDataAmp.at(i), ampNew));
																indexDataAmp.put(i, indexDataAmp.at(i) + 1);
														});
														// Duree
														// Set Bus
														duree = time - lastTime.at(i);
														if(duree > dureeMaximumAnalyze  or: {duree > memoryTime},
															{
																freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
																(numFhzBand + 1).do({arg i; lastTime.put(i, time)});
															},
															{
																busOSCduree.at(i).set(duree);
																// Add DataDuree
																if(maximumData > dataDuree.at(i).size,
																	{
																		dataDuree.put(i, dataDuree.at(i).add(duree));
																	},
																	{
																		if(dataDuree.at(i).size <= indexDataDuree.at(i), {indexDataDuree.put(i, 0)});
																		dataDuree.put(i, dataDuree.at(i).wrapPut(indexDataDuree.at(i), duree));
																		indexDataDuree.put(i, indexDataDuree.at(i) + 1);
																});
																lastTime.put(i, time);
														});
														// Set Bus
														// FLux
														busOSCflux.at(i).setSynchronous(busOSCflux.at(0).getSynchronous);
														// Flatness
														busOSCflatness.at(i).setSynchronous(busOSCflatness.at(0).getSynchronous);
														// Centroid
														busOSCcentroid.at(i).setSynchronous(busOSCcentroid.at(0).getSynchronous);
														// Energy
														busOSCenergy.at(i).setSynchronous(busOSCenergy.at(0).getSynchronous);
														// BPM
														busOSCbpm.at(i).setSynchronous(busOSCbpm.at(0).getSynchronous);
														// ADD DATA FFT
														// Flux
														if(maximumData > dataFlux.at(i).size,
															{
																dataFlux.put(i, dataFlux.at(i).add((busOSCflux.at(0).getSynchronous)));
															},
															{
																if(dataFlux.at(i).size <= indexDataFlux.at(i), {indexDataFlux.put(i, 0)});
																dataFlux.put(i, dataFlux.at(i).wrapPut(indexDataFlux.at(i), (busOSCflux.at(0).getSynchronous)));
																indexDataFlux.put(i, indexDataFlux.at(i) + 1);
														});
														// Flatness
														if(maximumData > dataFlatness.at(i).size,
															{
																dataFlatness.put(i, dataFlatness.at(i).add((busOSCflatness.at(0).getSynchronous)));
															},
															{
																if(dataFlatness.at(i).size <= indexDataFlatness.at(i), {indexDataFlatness.put(i, 0)});
																dataFlatness.put(i, dataFlatness.at(i).wrapPut(indexDataFlatness.at(i), (busOSCflatness.at(0).getSynchronous)));
																indexDataFlatness.put(i, indexDataFlatness.at(i) + 1);
														});
														// Centroid
														if(maximumData > dataCentroid.at(i).size,
															{
																dataCentroid.put(i, dataCentroid.at(i).add((busOSCcentroid.at(0).getSynchronous)));
															},
															{
																if(dataCentroid.at(i).size <= indexDataCentroid.at(i), {indexDataCentroid.put(i, 0)});
																dataCentroid.put(i, dataCentroid.at(i).wrapPut(indexDataCentroid.at(i), (busOSCcentroid.at(0).getSynchronous)));
																indexDataCentroid.put(i, indexDataCentroid.at(i) + 1);
														});
														// Energy
														if(maximumData > dataEnergy.at(i).size,
															{
																dataEnergy.put(i, dataEnergy.at(i).add((busOSCenergy.at(0).getSynchronous)));
															},
															{
																if(dataEnergy.at(i).size <= indexDataEnergy.at(i), {indexDataEnergy.put(i, 0)});
																dataEnergy.put(i, dataEnergy.at(i).wrapPut(indexDataEnergy.at(i), (busOSCenergy.at(0).getSynchronous)));
																indexDataEnergy.put(i, indexDataEnergy.at(i) + 1);
														});
														// BPM
														if(maximumData > dataBPM.at(i).size,
															{
																dataBPM.put(i, dataBPM.at(i).add(busOSCbpm.at(0).getSynchronous));
															},
															{
																if(dataBPM.at(i).size <= indexDataBPM.at(i), {indexDataBPM.put(i, 0)});
																dataBPM.put(i, dataBPM.at(i).wrapPut(indexDataBPM.at(i), busOSCbpm.at(0).getSynchronous));
																indexDataBPM.put(i, indexDataBPM.at(i) + 1);
														});
													},
													{
														// Init Band at(i)
														// Init Array
														dataFlux.put(i, []);
														dataFlatness.put(i, []);
														dataCentroid.put(i, []);
														dataEnergy.put(i, []);
														dataBPM.put(i, []);
														indexDataFlux.put(i, 0);
														indexDataFlatness.put(i, 0);
														indexDataCentroid.put(i, 0);
														indexDataEnergy.put(i, 0);
														indexDataBPM.put(i, 0);
														dataFreq.put(i, []);
														dataAmp.put(i, []);
														dataDuree.put(i, []);
														indexDataFreq.put(i, 0);
														indexDataAmp.put(i, 0);
														indexDataDuree.put(i, 0);
														lastTime.put(i, Main.elapsedTime);// Init Time
														freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
													});
												},
												{
													// Init Band at(i)
													// Init Array
													if(i <= numFhzBand, {
														duree = time - lastTime.at(i);
														if(duree > dureeMaximumAnalyze or: {duree > memoryTime}, {
															dataFlux.put(i, []);
															dataFlatness.put(i, []);
															dataCentroid.put(i, []);
															dataEnergy.put(i, []);
															dataBPM.put(i, []);
															indexDataFlux.put(i, 0);
															indexDataFlatness.put(i, 0);
															indexDataCentroid.put(i, 0);
															indexDataEnergy.put(i, 0);
															indexDataBPM.put(i, 0);
															dataFreq.put(i, []);
															dataAmp.put(i, []);
															dataDuree.put(i, []);
															indexDataFreq.put(i, 0);
															indexDataAmp.put(i, 0);
															indexDataDuree.put(i, 0);
															lastTime.put(i, Main.elapsedTime);// Init Time
															freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
														});
													});
													indexFhz = 0;
												});
											});
											// Plot Data Music
											plotDataMusic.value(freq, amp, dureeDisplay);
											// Evaluate for each Instrument
											//dataInstr = [bus, time, dureeInstrument, buffer, recBuffer, synth, synthMidi, canalMidi, fx, masterOut, noteOff, dureeBPM, dataMusicTransform, z1, z2, z3, z4, z5, z6, z7, algorithm, indexBandFhz]
											listeDataInstruments.do({arg dataInstr, index;
												if(dataInstr.at(5).asString.containsi("EventStreamPlayer").not and: {dataInstr.at(5).isPlaying == true},
													{
														if(dataInstr.at(21) == indexBand or: {dataInstr.at(21) == 0},
															{
																# freqStream, ampStream, dureeStream = computeAlgoFilterDataMusic.value(busOSCfreq.at(dataInstr.at(21)).getSynchronous.asArray.cpsmidi / 127, busOSCamp.at(dataInstr.at(21)).getSynchronous.asArray, busOSCduree.at(dataInstr.at(21)).getSynchronous.asArray / dureeMaximumAnalyze, dataInstr.at(12), dataInstr.at(13), dataInstr.at(14), dataInstr.at(15), dataInstr.at(16), dataInstr.at(17), dataInstr.at(18), dataInstr.at(19), dataInstr.at(20));
																dataInstr.at(5).set(\freq, freqStream);
																dataInstr.at(5).set(\amp, ampStream);
																// Pour New Synth Stream avec EnvGen
																dataInstr.at(5).set(\dur, dureeStream);
																// MIDI OUT
																if(flagMidiOut == 'on', {
																	// Set MIDI Off
																	midiOut.noteOff(dataInstr.at(7), dataInstr.at(10), 0);
																	if(flagVST == 'on', {fxVST.midi.noteOff(dataInstr.at(7), dataInstr.at(10), 0)});
																	// Reset MIDI OUT
																	listeDataInstruments.at(index).wrapPut(10, freqStream.flat.at(0).cpsmidi);
																	// Send MIDI On
																	midiOut.noteOn(dataInstr.at(7), freqStream.flat.at(0).cpsmidi, ampStream.at(0) * 127);
																	if(flagVST == 'on', {fxVST.midi.noteOn(dataInstr.at(7), freqStream.flat.at(0).cpsmidi, ampStream.at(0) * 127)});
																});
														});
												});
											});
									});
									freqTampon = freq; ampTampon = amp; lastTime.put(0, time);
							}, {nil});
						});
						dureeAnalyzeOSCMusic = Main.elapsedTime;
				});
			}, '/Density_Music_Data', serveurAdresse);

			// Setup MIDI Responder
			// NoteOn
			MIDIdef.noteOn(\midiNoteOn, {arg amp, freq, canal, src;
				if(canal == canalMIDI, {
					freq = freq.midicps;
					amp = amp / 127;
					s.bind{
						synthMIDI.set(\trigger, 0);
						s.sync;
						synthMIDI.set(\note, freq, \amp, amp, \trigger, 1);
						s.sync;
					};
				});
			}, (0..127), nil);

			/////////////// AlgoCompo + Setup Range and Filter Data Music ////////////////////////
			computeAlgoFilterDataMusic = {arg freq, amp, duree, data, z1, z2, z3, z4, z5, z6, z7, algorithm;
				var music, fft, octave, position = 0, ratio, degre, newFreq=[], newAmp=[], newDuree=[], chordFreq=[], chordAmp=[], chordDuree=[], q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie, distances=[], dureeChord, maxTraining=0, flux, flatness, centroid, energy, bpm, listF=[], listA=[], listD=[], freqNeu=[], ampNeu=[], durNeu=[];
				// DataMusicTransform [fft, freq, amp, duree]
				// [[flux, flatness, centroid, energy, bpm], [q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie], ...]
				// Choix de l'algorythme Probability
				freq = freq.flat;
				amp = amp.flat;
				duree = duree.flat;
				switch(algorithm,
					'Default', {
						/*// FFT
						# flux, flatness, centroid, energy, bpm = data.at(0);*/
						// NewDuree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(3);
						if(flagChord == 'on', {
							// Check Duree for Chords
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];
									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						newDuree = newDuree.mod(1);
						//Set Range newDuree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization newDuree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						// newFreq Transformation
						newFreq = newFreq.mod(1);
						// Setup Range newFreq
						newFreq = newFreq.collect({arg note, index;
							note.asArray.collect({arg chord, index;
								/*if(chord < 0, {chord = chord.abs});
								if(chord > 1, {chord = 1 - (chord - 1)});*/
								chord = chord * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
								chord.midicps;
							});
						});
						// Setup newFreq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// newAmp Transformation
						//# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(2);
						// Set Range newAmp
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					},
					'Probability', {
						/*// FFT
						# flux, flatness, centroid, energy, bpm = data.at(0);*/
						// NewDuree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(3);
						if(flagChord == 'on', {
							// Check Duree for Chords
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];
									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						// Transpose
						if(coin(cv.frac), {newDuree = newDuree + (ecartType * dissymetrie.sign);
						}, {
							// Compress expand
							if(coin(cv.frac), {
								newDuree = newDuree * cv;
							},
							{
								//newDuree = newDuree + (ecartSemiQ * dissymetrie.sign)
								newDuree = ecartSemiQ / newDuree;
							});
						});
						newDuree = newDuree.mod(1);
						//Set Range newDuree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization newDuree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						// newFreq Transformation
						//Change Octave or Transpose
						if(coin(cv.frac), {
							newFreq = newFreq + (dissymetrie.floor * (12/127));
						}, {
							// Transpose
							if(coin(cv.frac), {
								newFreq = newFreq + (ecartType * dissymetrie);
							});
						});
						// Compress or Expand
						if(coin(cv.frac), {
							newFreq = newFreq * cv;
						},
						{
							// Expand
							if(coin(cv.frac), {
								newFreq = ecartSemiQ / newFreq;
							});
						});
						newFreq = newFreq.mod(1);
						// Setup Range newFreq
						newFreq = newFreq.collect({arg note, index;
							note.asArray.collect({arg chord, index;
								/*if(chord < 0, {chord = chord.abs});
								if(chord > 1, {chord = 1 - (chord - 1)});*/
								chord = chord * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
								chord.midicps;
							});
						});
						// Setup newFreq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// newAmp Transformation
						// Set Range newAmp
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					},
					'Kohonen', {
						/*// FFT
						# flux, flatness, centroid, energy, bpm = data.at(0);*/
						// Training Kohonen Duree
						maxTraining.do({arg i; kohonenD.training(duree.wrapAt(i).asArray * 127, i+1, maxTraining, 1)});
						// Calculate Kohonen Duree
						duree = duree.collect({arg item, index;
							item = kohonenD.training(item.asArray, 1, 1, 1);
							item.at(0).at(1) / 127;// Vecteur
						});
						// Duree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(3);
						if(flagChord == 'on', {
							// Check Duree for Chords
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];
									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						// Set Range Duree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization Duree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						// Freq Transformation
						// Training Kohonen Freq
						maxTraining = newFreq.size;
						maxTraining.do({arg i; kohonenF.training(newFreq.wrapAt(i).asArray * 127, i+1, maxTraining, 1)});
						// Calculate Kohonen Freq
						newFreq = newFreq.collect({arg item, index, z;
							item = kohonenF.training(item.asArray, 1, 1, 1);
							item.at(0).at(1) / 127;// Vecteur
						});
						// Setup Range Freq
						newFreq = newFreq.collect({arg item, index;
							if(item < 0, {item = 0});
							if(item > 1, {item = 1});
							item = item * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
							item.midicps;
						});
						// Setup Freq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// Amp Transformation
						// Training Kohonen Amp
						maxTraining.do({arg i; kohonenA.training(newAmp.wrapAt(i).asArray * 127, i+1, maxTraining, 1)});
						// Calculate Kohonen Amp
						newAmp = newAmp.collect({arg item, index;
							item = kohonenA.training(item.asArray, 1, 1, 1);
							item.at(0).at(1) / 127;// Vecteur
						});
						// Set Range Amp
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					},
					'Genetic', {
						// FFT
						# flux, flatness, centroid, energy, bpm = data.at(0);
						// Freq Transformation
						// Calculation algo new musical pattern
						freq.size.do({arg i, f, a, d;
							# f = geneticF.next([freq.at(i)], 1, flatness, flux);
							listF = listF.add(f);
							# a = geneticA.next([amp.at(i)], 1,  flatness, flux);
							listA = listA.add(a);
							# d = geneticD.next([duree.at(i)], 1, flatness, flux);
							listD = listD.add(d);
						});
						freq = listF;
						amp = listA;
						duree = listD;
						// Duree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(3);
						// Set Duree
						// Check Duree for Chords
						if(flagChord == 'on', {
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];
									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						// Set Range Duree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization Duree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						// Setup Range Freq
						newFreq = newFreq * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
						newFreq = newFreq.midicps;
						// Setup Freq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// Set Range Amp
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					},
					'Euclide', {
						// Distances Euclidiennes 3D -> Vecteur 1D
						distances = sqrt(freq.pow(2) + amp.pow(2) + duree.pow(2));
						// Probabilite sur Distances
						# q1, mediane, q3, ecartQ, ecartSemiQ = distances.quartiles;
						ecartType = distances.ecartType;
						cv = ecartType / distances.mean;
						dissymetrie = distances.dissymetrie;
						dureeChord = q1;
						// Set new data
						freq = distances;
						// Freq
						//Change Octave or Transpose
						if(coin(cv.frac), {
							freq = freq + (dissymetrie.floor * (12/127));
						}, {
							// Transpose
							if(coin(cv.frac), {
								freq = freq + (ecartType * dissymetrie);
							});
						});
						// Compress or Expand
						if(coin(cv.frac), {
							freq = freq * cv;
						},
						{
							// Expand
							if(coin(cv.frac), {
								freq = ecartSemiQ / freq;
							});
						});
						freq = freq.mod(1);
						// Amp
						if(coin(cv.frac), {amp = amp * distances}, {amp = amp / distances});
						// Transpose
						if(coin(cv.frac), {amp = amp + (ecartType * dissymetrie.sign);
						});
						// Compress expand
						if(coin(cv.frac), {
							amp = amp * cv;
						},
						{
							amp = amp + (ecartSemiQ * dissymetrie.sign)
						});
						amp = amp.mod(1);
						// Duree
						if(coin(cv.frac), {duree = duree / distances},{duree = duree * distances});
						// Transpose
						if(coin(cv.frac), {duree = duree + (ecartType * dissymetrie.sign);
						});
						// Compress expand
						if(coin(cv.frac), {
							duree = duree * cv;
						},
						{
							duree = duree + (ecartSemiQ * dissymetrie.sign)
						});
						duree = duree.abs.mod(1);
						// Set Duree
						// Check Duree for Chords
						if(flagChord == 'on', {
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];

									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						// Set Range Duree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization Duree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						// Setup Range Freq
						newFreq = newFreq * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
						newFreq = newFreq.midicps;
						// Setup Freq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// Set Range Amp
						newAmp = newAmp / listeDataInstruments.size.max(1);
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					},
					'Neural', {
						freqNeu=[];
						ampNeu=[];
						durNeu=[];
						// Freq Transformation
						// Training Neural
						//maxTraining = freq.size;
						// Calculate Neural Freq Amp Duree
						freq.size.do({arg i, f, a, d;
							# f, a, d = neuralFAD.next([freq.wrapAt(i).asArray, amp.wrapAt(i).asArray, duree.wrapAt(i).asArray], nil, 1, 0.5, 0.5);
							// Freq
							freqNeu = freqNeu.add((f.at(0) *  abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0)).midicps);
							// Amp
							ampNeu = ampNeu.add(a.at(0));
							// Duree
							durNeu = durNeu.add(d.at(0));
						});
						freq = freqNeu;
						amp = ampNeu;
						duree = durNeu;
						// Duree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie = data.at(3);
						// Duree Transformation
						if(flagChord == 'on', {
							// Check Duree for Chords
							duree.do({arg duree, index, newFHZ;
								newFHZ = freq.at(index);
								if(duree <= q1, {
									chordFreq=chordFreq.add(newFHZ);
									chordAmp=chordAmp.add(amp.at(index));
									chordDuree=chordDuree.add(q1);
								}, {
									if(chordFreq == [], {
										newFreq = newFreq.add(newFHZ);
										newAmp = newAmp.add(amp.at(index));
										newDuree = newDuree.add(duree);
									}, {
										chordFreq=chordFreq.add(newFHZ);
										chordAmp=chordAmp.add(amp.at(index));
										newFreq = newFreq.add(chordFreq);
										newAmp = newAmp.add(chordAmp.mediane);
										newDuree= newDuree.add(duree);
										chordFreq = [];
									});
								});
							});
							if(newFreq.size == 0, {
								newFreq = chordFreq;
								newAmp = chordAmp;
								newDuree= chordDuree;
							});
						}, {
							// No Chord
							newFreq = freq;
							newAmp = amp;
							newDuree = duree;
						});
						// Set Range Duree
						newDuree = newDuree * (rangeDureeintruments.at(1) - rangeDureeintruments.at(0)) + rangeDureeintruments.at(0);
						// Quantization Duree
						newDuree = newDuree.floor + ((newDuree.frac*quantizationDuree + 0.5).floor / quantizationDuree);
						newDuree.do({arg item, index;
							if(item <= 0, {item = quantizationDuree.reciprocal});
							newDuree.put(index, item);
						});
						/*// Setup Range Freq
						freq = freq.collect({arg item, index;
						item = item * abs(rangeFreqintruments.at(1) - rangeFreqintruments.at(0)) + 	rangeFreqintruments.at(0);
						item.midicps;
						});*/
						// Setup Freq with Scaling and Tuning
						if(flagScaling == 'on', {
							newFreq = newFreq.collect({arg item, index;
								item.asArray.collect({arg note, index;
									octave = (note.cpsmidi / 12);
									ratio = (octave.frac * 12).round(0.1);
									octave = octave.floor;
									position = scale.degrees.indexOfEqual(ratio);
									if(position == nil,
										{
											position = scale.degrees.indexOfGreaterThan(ratio);
											if(position == nil,
												{
													position = scale.degrees.last;
												},
												{
													position = scale.degrees.at(position);
												}
											);
										},
										{
											position = scale.degrees.at(position);
										}
									);
									note = (octave * 12 + position).midicps;
								});
							});
						});
						// Amp Transformation
						newAmp = newAmp * abs(rangeDBintruments.at(1) - rangeDBintruments.at(0)) + rangeDBintruments.at(0);
					}
				);
				// Out
				[newFreq, newAmp, newDuree];
			};

			/////////////////// Build New dataInstruments //////////////////////
			buildSynth = {arg indexBandFhz;
				var bus, recBuffer, dureeInstrument, synth, masterOut, fx, fxName, synthMidi, freq, amp, duree, time, pattern, patternMidi, dureeStretchBPM, synthName, panx, pany, canalMidi, envelopeLevel, envelopeTime, buffer, busRec, indexX, indexY, soundName, flux, flatness, centroid, energy, bpm, dataMusicTransform, q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie, z1, z2, z3, z4, z5, z6, z7, algorithm, offset, rootEnergy, newRevSound, patternVST, synthMidiVST, newFreq, newAmp, newDur;
				// Probability
				// Flux
				/*flux = (13.287712379549 - fft.at(0).mediane.log2.abs / 13.287712379549).clip(0, 1);
				// Flatness
				flatness = (13.287712379549 - fft.at(1).mediane.log2.abs / 13.287712379549).clip(0, 1);*/
				if(flagMemory == 'off', {
					flux = dataFlux.at(indexBandFhz);
					flatness = dataFlatness.at(indexBandFhz);
					centroid = dataCentroid.at(indexBandFhz);
					energy = rootEnergy = dataEnergy.at(indexBandFhz);
					bpm = dataBPM.at(indexBandFhz);
					freq = dataFreq.at(indexBandFhz);
					amp = dataAmp.at(indexBandFhz);
					duree = dataDuree.at(indexBandFhz);
				},
				{
					flux = memoryDataFlux.at(indexBandFhz);
					flatness = memoryDataFlatness.at(indexBandFhz);
					centroid = memoryDataCentroid.at(indexBandFhz);
					energy = rootEnergy = memoryDataEnergy.at(indexBandFhz);
					bpm = memoryDataBPM.at(indexBandFhz);
					freq = memoryDataFreq.at(indexBandFhz);
					amp = memoryDataAmp.at(indexBandFhz);
					duree = memoryDataDuree.at(indexBandFhz);
				});
				flux = flux.mediane;
				flux = flux + rrand(jitterControls.neg, jitterControls);
				flux = flux.mod(1);
				// Flatness
				flatness = flatness.mediane;
				flatness = flatness + rrand(jitterControls.neg, jitterControls);
				flatness = flatness.mod(1);
				// Centroid
				centroid = centroid.mediane;
				centroid = centroid + rrand(jitterControls.neg * 12543, jitterControls * 12543);
				centroid = centroid.mod(12544);
				// Energy
				energy = energy.mediane;
				energy = energy + rrand(jitterControls.neg * 12543, jitterControls * 12543);
				energy = energy.mod(12544);
				// BPM
				bpm = bpm.mediane;
				/*bpm = bpm + rrand(jitterControls.neg, jitterControls);
				bpm = bpm.mod(1);*/
				dataMusicTransform = dataMusicTransform.add([flux, flatness, centroid, energy, bpm]);
				// Freq
				freq = freq.cpsmidi / 127;
				# q1, mediane, q3, ecartQ, ecartSemiQ = freq.quartiles;
				ecartType = freq.ecartType;
				cv = ecartType / freq.mean;
				dissymetrie = freq.dissymetrie;
				dataMusicTransform = dataMusicTransform.add([q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie]);
				// Amp
				# q1, mediane, q3, ecartQ, ecartSemiQ = amp.quartiles;
				ecartType = amp.ecartType;
				cv = ecartType / amp.mean;
				dissymetrie = amp.dissymetrie;
				dataMusicTransform = dataMusicTransform.add([q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie]);
				// Duree
				duree = duree / dureeMaximumAnalyze;
				# q1, mediane, q3, ecartQ, ecartSemiQ = duree.quartiles;
				ecartType = duree.ecartType;
				cv = ecartType / duree.mean;
				dissymetrie = duree.dissymetrie;
				dataMusicTransform = dataMusicTransform.add([q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie]);
				/*// For Kohonen
				kohonenF = HPclassKohonen.new(1,127,1);
				kohonenA = HPclassKohonen.new(1,127,1);
				kohonenD = HPclassKohonen.new(1,127,1);
				// For Genetic
				geneticF = HPclassGenetiques.new(1, 127);
				geneticA = HPclassGenetiques.new(1, 127);
				geneticD = HPclassGenetiques.new(1, 127);
				// For Neural
				neuralFAD = HPNeuralNet.new(3, 1, [9], 3);*/
				// Choose Algorithm
				algorithm = rrand(algoLo, algoHi);
				algorithm = listAlgorithm.at(algorithm);
				displayAlgo = algorithm.asString;
				displayIndex = indexBandFhz.asString;
				//////////////////////// COMPUTE ALGO /////////////////////////////
				# newFreq, newAmp, newDur = computeAlgoFilterDataMusic.value(freq, amp, duree, dataMusicTransform, z1, z2, z3, z4, z5, z6, z7, algorithm);
				///////////////////////////////////////////////////////////////////
				// Synth
				indexX = (indexInstrumentX + (0.5 * rrand(jitterIndexInstrumentX.neg, jitterIndexInstrumentX))).clip(0, 1);
				indexY = (indexInstrumentY + (0.5 * rrand(jitterIndexInstrumentY.neg, jitterIndexInstrumentY))).clip(0, 1);
				synthName = synthOrchestra.at((indexX * (synthOrchestra.size - 1) + 0.5).floor);
				synthName = synthName.at((indexY * (synthName.size - 1) + 0.5).floor);
				// Panoramic
				panx = rrand(panSynthLo, panSynthHi); pany = rrand(panSynthLo, panSynthHi);
				// Canal MIDI
				if(indexBandFhz == 0,
					{canalMidi = chanelsMidi[0] - 1},
					{canalMidi = chanelsMidi[indexBandFhz] - 1};
				);
				displayMIDI = (canalMidi + 1).asString;
				// Envelope
				envelopeLevel = envelopeSynth.at(0);
				envelopeTime = envelopeSynth.at(1);
				// New Free Bus
				if(listeBusOff.size == 0,
					{listeBusOff = []; maximumInstruments.do({arg i; listeBusOff = listeBusOff.add(i)})
				});
				bus = listeBusOff.at(0); listeBusOff.remove(bus);
				// Duree Instrument
				dureeInstrument = newDur.sum;
				// DureeStretchBPM
				dureeStretchBPM =  newDur.sum * globalDensity.reciprocal;
				// Buffer
				if(flagSampler == "Sampler+Sound", {
					if(coin(0.5), {
						buffer = Buffer.alloc(s, s.sampleRate * dureeSample, 1).path = "Buffer for Sampler";
						s.sync;
						recBuffer = Synth.new("RecBufferSynth", [\in, busRecAudioIn, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
						//busRec = [0,1,2,3,4,5,6,7].scramble.choose;
						/*recBuffer = Synth.new("RecBufferSynth", [\in, busRec, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
						s.sync;*/
						// For testing if playing or not
						NodeWatcher.register(recBuffer, true);
						soundName = "-> Audio In";
						// Normalize
						//if(synthName.containsi("Buf"), {newAmp = newAmp.max(0.9)});
					},
					{
						indexX = (indexSoundX + (0.5 * rrand(jitterIndexSoundX.neg, jitterIndexSoundX))).clip(0, 1);
						indexY = (indexSoundY + (0.5 * rrand(jitterIndexSoundY.neg, jitterIndexSoundY))).clip(0, 1);
						buffer = listeBuffer.at((indexX * (listeBuffer.size - 1) + 0.5).floor);
						//buffer = buffer.at((indexY * (buffer.size - 1) + 0.5).floor);
						soundName = soundOrchestra.at((indexX * (soundOrchestra.size - 1) + 0.5).floor);
						soundName = soundName.at((indexY * (soundName.size - 1) + 0.5).floor);
						soundName = PathName.new(soundName).fileName;
						if(flagRecSound == 'off', {
							buffer = buffer.at((indexY * (buffer.size - 1) + 0.5).floor);
							recBuffer = nil;
						},
						{
							buffer = buffer.at((indexY * (buffer.size - 1) + 0.5).floor).copy.path = "Buffer for Sampler";
							s.sync;
							recBuffer = Synth.new("RecBufferSynth", [\in, busRecAudioIn, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
							NodeWatcher.register(recBuffer, true);
						});
					};
					);
				},
				{
					if(flagSampler == "Sampler", {
						buffer = Buffer.alloc(s, s.sampleRate * dureeSample, 1).path = "Buffer for Sampler";
						s.sync;
						recBuffer = Synth.new("RecBufferSynth", [\in, busRecAudioIn, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
						//busRec = [0,1,2,3,4,5,6,7].scramble.choose;
						/*recBuffer = Synth.new("RecBufferSynth", [\in, busRec, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
						s.sync;*/
						// For testing if playing or not
						NodeWatcher.register(recBuffer, true);
						soundName = "-> Audio In";
						// Normalize
						//if(synthName.containsi("Buf"), {amp = amp.max(0.9)});
					},
					{
						indexX = (indexSoundX + (0.5 * rrand(jitterIndexSoundX.neg, jitterIndexSoundX))).clip(0, 1);
						indexY = (indexSoundY + (0.5 * rrand(jitterIndexSoundY.neg, jitterIndexSoundY))).clip(0, 1);
						buffer = listeBuffer.at((indexX * (listeBuffer.size - 1) + 0.5).floor);
						buffer = buffer.at((indexY * (buffer.size - 1) + 0.5).floor);
						soundName = soundOrchestra.at((indexX * (soundOrchestra.size - 1) + 0.5).floor);
						soundName = soundName.at((indexY * (soundName.size - 1) + 0.5).floor);
						soundName = PathName.new(soundName).fileName;
						if(flagRecSound == 'off', {
							recBuffer = nil;
						},
						{
							buffer = Buffer.alloc(s, buffer.numFrames, 1).path = "Buffer for Sampler";
							s.sync;
							recBuffer = Synth.new("RecBufferSynth", [\in, busRecAudioIn, \buffer, buffer, \level1, recLevel, \level2, preLevel, \loopRec, loopRec], groupeRecBuffer, \addToTail);
							NodeWatcher.register(recBuffer, true);
						});
					};
					);
				};
				);
				//Offset
				offset = offsetSound + rrand(jitterControls.neg, jitterControls);
				offset = offset.mod(1);
				// Reverse Proba
				if(rrand(0.0, 1.0) >= jitterControls, {newRevSound = reverseSound},
					{newRevSound = 1.neg});
				// Setup New Root
				if(flagRoot == 'on', {
					root = rootEnergy.mediane; // FFT -> Energy
					root = root.cpsoct;
					root = (root.frac * tuning.size + 0.5).floor;
					// Setup GUI Value
					{windowEar.view.children.at(85).children.at(2).valueAction_(root);}.defer;
				});
				// MASTEROUT
				masterOut = Synth.new(typeMasterOut, [
					\in, busSynthInOut.at(bus),
					\out, channelsSynth.at(indexBandFhz),
					\panX, panx,
					\panY, pany,
					\durSynth, dureeInstrument
				], groupeMasterOut, \addToTail).map(
					\bpm, busOSCbpm.at(indexBandFhz));
				// For testing if playing or not
				NodeWatcher.register(masterOut, true);
				// FX
				indexX = (indexFXX + (0.5 * rrand(jitterIndexFXX.neg, jitterIndexFXX))).clip(0, 1);
				indexY = (indexFXY + (0.5 * rrand(jitterIndexFXY.neg, jitterIndexFXY))).clip(0, 1);
				fxName = fxOrchestra.at((indexX * (fxOrchestra.size - 1) + 0.5).floor);
				fxName = fxName.at((indexY * (fxName.size - 1) + 0.5).floor);
				fx = Synth.new(fxName, [
					\in, busSynthInOut.at(bus),
					\out, busSynthInOut.at(bus),
					\xFade, fadeFX,
					\dur, 1,
					\durSynth, dureeInstrument
				], groupeFX, \addToTail).map(
					\flux, busOSCflux.at(indexBandFhz),
					\flatness, busOSCflatness.at(indexBandFhz),
					\centroid, busOSCcentroid.at(indexBandFhz),
					\energy, busOSCenergy.at(indexBandFhz),
					\bpm, busOSCbpm.at(indexBandFhz));
				// For testing if playing or not
				NodeWatcher.register(fx, true);
				// SETUP SYNTH STEPS OR STREAM
				if(synthName.containsi("Stream").not,
					{
						// SYNTH STEPS
						pattern  = PmonoArtic(
							synthName,
							\in, busRecAudioIn,
							\out, busSynthInOut.at(bus),
							\buffer, buffer,
							\loop, Pfuncn({
								if(flagRec == 1, {
									recBuffer.set(\trigger, 1);
									s.sync;
									recBuffer.set(\trigger, 0);
									s.sync;
								});
								loopSound}, inf),
							\offset, Pfuncn({if(newRevSound == 1.neg, {offset = (1 - offset)}, {offset}); offset}, inf),
							\reverse, Pfuncn({newRevSound}, inf),
							\freq, Pseq(newFreq, loopMusic),
							\amp, Pseq(newAmp, loopMusic),
							\dur, Pseq(newDur, loopMusic),
							\durSynth, dureeInstrument,
							\durSample, dureeSample,
							//\legato,  0.5,
							\ctrlHP1, Pfuncn({ctrlHP1}, inf),
							\ctrlHP2, Pfuncn({ctrlHP2}, inf),
							\stretch, Pfuncn({stretchDuree}, inf),
							\flux, (busOSCflux.at(indexBandFhz)).asMap,
							\flatness, (busOSCflatness.at(indexBandFhz)).asMap,
							\centroid, (busOSCcentroid.at(indexBandFhz)).asMap,
							\energy, (busOSCenergy.at(indexBandFhz)).asMap,
							\bpm, (busOSCbpm.at(indexBandFhz)).asMap,
							//\gate, 1,
							\level1, recLevel,
							\level2, preLevel,
							\envLevel1, envelopeLevel.at(0),
							\envLevel2, envelopeLevel.at(1),
							\envLevel3, envelopeLevel.at(2),
							\envLevel4, envelopeLevel.at(3),
							\envLevel5, envelopeLevel.at(4),
							\envLevel6, envelopeLevel.at(5),
							\envLevel7, envelopeLevel.at(6),
							\envLevel8, envelopeLevel.at(7),
							\envTime1, envelopeTime.at(0),
							\envTime2, envelopeTime.at(1),
							\envTime3, envelopeTime.at(2),
							\envTime4, envelopeTime.at(3),
							\envTime5, envelopeTime.at(4),
							\envTime6, envelopeTime.at(5),
							\envTime7, envelopeTime.at(6),
							\loopRec, loopRec,
							\group, groupeSynth,
							\addAction, 1);
						// MIDI
						if(flagMidiOut == 'on', {
							patternMidi  = Pbind(
								\type, \midi,
								\midicmd, \noteOn,
								\midiout, midiOut,
								\chan, canalMidi,
								\freq, Pseq(newFreq, loopMusic),
								\amp, Pseq(newAmp, loopMusic),
								\dur, Pseq(newDur, loopMusic),
								\stretch, Pfuncn({stretchDuree}, inf),
								\group, groupeSynth,
								\addAction, 1);
							// VST Instrument
							if(flagVST == 'on', {patternVST  = Pbind(
								\type, \vst_midi,
								\vst, fxVST,
								\midicmd, \noteOn,
								\midiout, midiOut,
								\chan, canalMidi,
								\freq, Pseq(newFreq, loopMusic),
								\amp, Pseq(newAmp, loopMusic),
								\dur, Pseq(newDur, loopMusic),
								\stretch, Pfuncn({stretchDuree}, inf),
								\group, groupeSynth,
								\addAction, 1);
							});
						}, {synthMidi = nil; synthMidiVST = nil});
						//Play Synth next Beat on BPM
						synth = pattern.play(quant: Quant(quantizationDuree.reciprocal));
						//Play SynthMidi next Beat on BPM
						if(patternMidi != nil, {synthMidi = patternMidi.play(quant: Quant(quantizationDuree.reciprocal))});
						if(patternVST != nil, {synthMidiVST = patternVST.play(quant: Quant(quantizationDuree.reciprocal))});
					},
					{
						// SYNTH STREAM
						synth = Synth.new(synthName, [
							\in, busRecAudioIn,
							\out, busSynthInOut.at(bus),
							\buffer, buffer,
							\loop, 1, // Loop sound for streaming
							\offset, offset,
							\reverse, newRevSound,
							\freq, newFreq.at(0),
							\amp, newAmp.at(0),
							\dur, newDur.at(0),
							\durSynth, dureeInstrument,
							\durSample, dureeSample,
							\ctrlHP1, ctrlHP1,
							\ctrlHP2, ctrlHP2,
							\gate, 1,
							\level1, recLevel,
							\level2, preLevel,
							\envLevel1, envelopeLevel.at(0),
							\envLevel2, envelopeLevel.at(1),
							\envLevel3, envelopeLevel.at(2),
							\envLevel4, envelopeLevel.at(3),
							\envLevel5, envelopeLevel.at(4),
							\envLevel6, envelopeLevel.at(5),
							\envLevel7, envelopeLevel.at(6),
							\envLevel8, envelopeLevel.at(7),
							\envTime1, envelopeTime.at(0),
							\envTime2, envelopeTime.at(1),
							\envTime3, envelopeTime.at(2),
							\envTime4, envelopeTime.at(3),
							\envTime5, envelopeTime.at(4),
							\envTime6, envelopeTime.at(5),
							\envTime7, envelopeTime.at(6)
						], groupeSynth, \addToHead).map(
							\flux, busOSCflux.at(indexBandFhz),
							\flatness, busOSCflatness.at(indexBandFhz),
							\centroid, busOSCcentroid.at(indexBandFhz),
							\energy, busOSCenergy.at(indexBandFhz),
							\bpm, busOSCbpm.at(indexBandFhz));
						// For testing if playing or not
						NodeWatcher.register(synth, true);
						// MIDI
						synthMidi = nil; synthMidiVST = nil;
				});
				// Time Start Synth
				time = Main.elapsedTime;
				// Set List Data Instruments
				listeDataInstruments = listeDataInstruments.add([bus, time, dureeInstrument, buffer, recBuffer, synth, synthMidi, canalMidi, fx, masterOut, newFreq.flat.at(0).cpsmidi, dureeStretchBPM, dataMusicTransform, z1, z2, z3, z4, z5, z6, z7, algorithm, indexBandFhz, synthMidiVST]);
				// Display for GUI
				{
					// Synth
					displayInstrument.string = (indexInstrumentX.asStringPrec(2) + indexInstrumentY.asStringPrec(2) + synthName);
					// Sound
					displaySound.string = (indexSoundX.asStringPrec(2) + indexSoundY.asStringPrec(2) + PathName.new(soundName).fileName);
					// FX
					displayFX.string = (indexFXX.asStringPrec(2) + indexFXY.asStringPrec(2) + fxName);
				}.defer;
				// Display Analyze Music
				{displayAnalyzeMusic.string = ("ADD NEW SYNTHESIZER  " + "Instruments:" + listeDataInstruments.size + "  Algo: " + displayAlgo + "  FhzBand: " + displayIndex + "  M" ++ displayMIDI)}.defer;
			};

			// Tdef Player
			playInstruments = Tdef("Player", {
				var flag, lastTime;
				lastTime = Main.elapsedTime;
				loop({arg time, indexBandFhz;
					// Time
					time = Main.elapsedTime;
					// Check Instruments (data = [bus, time, dureeInstrument, buffer, recBuffer, synth, synthMidi, canalMidi, fx, masterOut, freq.flat.at(0).cpsmidi, dureeStretchBPM, dataMusicTransform, z1, z2, z3, z4, z5, z6, z7, algorithm, indexBandFhz, synthMidiVST])
					listeDataInstruments.do({arg data, index, tempo;
						var bpm;
						if(flagBPM == 'on', {
							if(flagMemory == 'on', {bpm = memoryDataBPM.at(0)}, {bpm = dataBPM.at(0)});
							if(bpm.size >=3, {
								tempo = bpm.mediane;
								if((oldTempo - tempo).abs > 0.2, {
									{
										// Setup GUI Value
										windowEar.view.children.at(34).children.at(2).valueAction_(tempo * 60)}.defer;
									oldTempo = tempo;
								});
							});
						});
						if((time - data.at(1)) > data.at(11) /*or: {data.at(5).asString.containsi("EventStreamPlayer") and: {data.at(5).streamHasEnded}}*/, {
							// Kill Synth
							if(data.at(5).asString.containsi("EventStreamPlayer"),
								{data.at(5).stop; data.at(5).free},
								{if(data.at(5).defName.asString.containsi("Env"), {data.at(5).free});
							});
							// Kill Synth Midi
							if(data.at(6) != nil, {data.at(6).stop; data.at(22).stop});
							// Free RecBuffer
							if(data.at(4).isPlaying == true, {data.at(4).free});
							// Free Buffer
							if(data.at(3).path == "Buffer for Sampler", {data.at(3).free});
							// Kill FX
							if(data.at(8).isPlaying == true, {data.at(8).release});
							// Kill masterOut
							if(data.at(9).isPlaying == true, {data.at(9).release});
							// Set MIDI Off (Stream Synth)
							if(data.at(6) == nil, {
								if(flagMidiOut == 'on', {midiOut.noteOff(data.at(7), data.at(10), 0)});
								if(flagVST == 'on', {fxVST.midi.noteOff(data.at(7), data.at(10), 0)});
							});
							listeDataInstruments.removeAt(index);
							listeBusOff = listeBusOff.add(data.at(0));
						});
					});
					// ALGO CHOOSE numFhzBand !!!!! + Choose SynthBand
					// Build new Instrument
					if(maximumInstruments > listeDataInstruments.size and: {(time - lastTime) >= (1 - globalDensity)}, {
						// With Band
						if(flagFhzBand == 'on' and: {rangeSynthBand.size != 0}, {
							if(numIndexSynthBand >= rangeSynthBand.size, {numIndexSynthBand = 0});
							indexBandFhz = rangeSynthBand.at(numIndexSynthBand);
							numIndexSynthBand = numIndexSynthBand + 1;
							if(flagMemory == 'on', {
								if(memoryDataFreq.at(indexBandFhz) != [] and: {rrand(0.0, 1.0) < globalDensity},
									{
										buildSynth.value(indexBandFhz)});
							},
							{
								if(dataFreq.at(indexBandFhz) != [] and: {rrand(0.0, 1.0) < globalDensity},
									{
										buildSynth.value(indexBandFhz)});
							});
						},
						// Build new Instrument without Band
						{
							indexBandFhz = 0;
							if(flagMemory == 'on', {
								if(memoryDataFreq.at(indexBandFhz) != [] and: {rrand(0.0, 1.0) < globalDensity},
									{buildSynth.value(indexBandFhz)});
							},
							{
								if(dataFreq.at(indexBandFhz) != [] and: {rrand(0.0, 1.0) < globalDensity},
									{buildSynth.value(indexBandFhz)});
							});
						});
						lastTime = time;
					});
					// Waiting Time
					//(1 - globalDensity + 0.01).wait;
					quantizationDuree.reciprocal.wait;
				});
			});

			// Tdef watch Silence
			watchSilence = Tdef("WatchSilence", {
				loop({
					// Watch musicdata
					if((Main.elapsedTime - dureeAnalyzeOSCMusic) > memoryTime,
						{
							dataFlux = [];
							dataFlatness = [];
							dataCentroid = [];
							dataEnergy = [];
							dataBPM = [];
							indexDataFlux = [];
							indexDataFlatness = [];
							indexDataCentroid = [];
							indexDataEnergy = [];
							indexDataBPM = [];
							memoryDataFlux = [];
							memoryDataFlatness = [];
							memoryDataCentroid = [];
							memoryDataEnergy = [];
							memoryDataBPM = [];
							dataFreq = [];
							dataAmp = [];
							dataDuree = [];
							indexDataFreq = [];
							indexDataAmp = [];
							indexDataDuree = [];
							memoryDataFreq = [];
							memoryDataAmp = [];
							memoryDataDuree = [];
							lastTime = [];
							// Init Array
							(numFhzBand + 1).do({arg i;
								dataFlux = dataFlux.add([]);
								dataFlatness = dataFlatness.add([]);
								dataCentroid = dataCentroid.add([]);
								dataEnergy = dataEnergy.add([]);
								dataBPM = dataBPM.add([]);
								indexDataFlux = indexDataFlux.add(0);
								indexDataFlatness = indexDataFlatness.add(0);
								indexDataCentroid = indexDataCentroid.add(0);
								indexDataEnergy = indexDataEnergy.add(0);
								indexDataBPM = indexDataBPM.add(0);
								memoryDataFlux = memoryDataFlux.add([]);
								memoryDataFlatness = memoryDataFlatness.add([]);
								memoryDataCentroid = memoryDataCentroid.add([]);
								memoryDataEnergy = memoryDataEnergy.add([]);
								memoryDataBPM = memoryDataBPM.add([]);
								dataFreq = dataFreq.add([]);
								dataAmp = dataAmp.add([]);
								dataDuree = dataDuree.add([]);
								indexDataFreq = indexDataFreq.add(0);
								indexDataAmp = indexDataAmp.add(0);
								indexDataDuree = indexDataDuree.add(0);
								memoryDataFreq = memoryDataFreq.add([]);
								memoryDataAmp = memoryDataAmp.add([]);
								memoryDataDuree = memoryDataDuree.add([]);
								lastTime = lastTime.add(Main.elapsedTime);
							});
							freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
							numIndexSynthBand = 0;
							dureeAnalyzeOSCMusic = Main.elapsedTime;
							// Display Analyze Music
							{displayAnalyzeMusic.string = ("SILENT (NO DATA)  " + "  Instruments:" + listeDataInstruments.size)}.defer;
					});
					// Waiting Time
					//(1 - globalDensity + 0.01).wait;
					quantizationDuree.reciprocal.wait;
				});
			});

			// Tdef Memory
			memoryMusic = Tdef("Memory", {
				loop({
					// Memory Setup
					if(flagMemory == 'on',
						{
							// Init copy Array
							(numFhzBand + 1).do({arg i;
								memoryDataFlux.put(i, dataFlux.at(i).copy);
								memoryDataFlatness.put(i, dataFlatness.at(i).copy);
								memoryDataCentroid.put(i, dataCentroid.at(i).copy);
								memoryDataEnergy.put(i, dataEnergy.at(i).copy);
								memoryDataBPM.put(i, dataBPM.at(i).copy);
								memoryDataFreq.put(i, dataFreq.at(i).copy);
								memoryDataAmp.put(i, dataAmp.at(i).copy);
								memoryDataDuree.put(i, dataDuree.at(i).copy);
							});
					});
					{displayAnalyzeMusic.string = ("DATA MEMORY COPY")}.defer;
					// Waiting
					memoryTime.wait;
				});
			});

			// End Seveur Wait For Boot
		});

		// Fonction Plotter FFT
		plotDataFFT = {arg flux, flatness, centroid, energy, bpm;
			var analyzeData;
			{
				// Setup GUI Value
				if(windowPlotterFFT.view.children.at(0).value == 1, {
					analyzeData = [(flux * 10000).log2.clip(0, 13), (flatness * 10000).log2.clip(0, 13), centroid, energy, bpm * 60];
					if(plotterFFT.at(0).size > 1024, {plotterFFTGUI.value = [[(flux * 10000).log2.clip(0, 13)], [(flatness * 10000).log2.clip(0, 13)], [centroid], [energy]]; plotterFFT = [[(flux * 10000).log2.clip(0, 13)], [(flatness * 10000).log2.clip(0, 13)], [centroid], [energy], [bpm * 60]]},
						{plotterFFT.size.do({arg index; plotterFFT.put(index, plotterFFT.at(index).add(analyzeData.at(index)))});
							plotterFFTGUI.value = plotterFFT;
					});
				});
				// Display Analyze FFT
				displayAnalyzeFFT.string = ("Flux:" + flux.asStringPrec(4)  + "    Flatness:" + flatness.asStringPrec(4) + "    Centroid:" + centroid.asStringPrec(4) + "    Energy:" + energy.asStringPrec(4) + "    BPM:" + (bpm * 60).asStringPrec(4));
			}.defer;
		};

		// Plotter Data Music
		plotDataMusic = {arg freq, amp, duree;
			var analyzeData;
			{
				// Setup GUI Value
				if(windowPlotterData.view.children.at(0).value == 1, {
					analyzeData = [freq.cpsmidi, amp.ampdb, duree];
					if(plotterData.at(0).size > 1024, {plotterData =[[freq.cpsmidi], [amp.ampdb], [duree]]},
						{plotterData.size.do({arg index; plotterData.put(index, plotterData.at(index).add(analyzeData.at(index)))});
							plotterDataGUI.value = plotterData;
					});
				});
				// Display Analyze Music
				displayAnalyzeMusic.string = ("Freq:" + freq.cpsmidi.asStringPrec(4)  +  "  Amp:" + amp.ampdb.asStringPrec(4) + "  Duree:" + duree.asStringPrec(4) + "  Instruments:" + listeDataInstruments.size + "  Data:" +  dataFreq.at(0).size + "  Index:" + indexDataFreq.at(0) + "  Algo: " + displayAlgo + "  FhzBand: " + displayIndex + "  M" ++ displayMIDI);
			}.defer;
		};

		cmdperiodfunc = {
			if(flagVST == 'on', {16.do({arg canal; midiOut.allNotesOff(canal); fxVST.midi.allNotesOff(canal)})});
			listeWindows.do({arg w; w.close});
			windowVST.close;
			//s.quit;
		};

		CmdPeriod.doOnce(cmdperiodfunc);

	}

	createGUI {

		// Help
		helpDensity = "
Single commandes:

esc	or SpaceBar			System on/off.
q / ctrl + q			Switch Algorithm Analyze.
h						Switch Source IN.
i						Init Synth.
ctrl+i					Init System.
alt+i					Reset System.
ctrl + f				Load and Add File for Analyze.
w / ctrl + w			Switch Window.
z						Load Random Preset.
k                       New Environment.
a                       Init Genetic
shift + a               Init Kohonen
alt + a                 Init Neural

Commandes follow by a numerical key (0,..9 ; shift 0,..9 ; alt 0,..9 ; alt + shift 0,..9):

l			 			Load Preset.
s				 		Save Preset.
f						Switch File for analyze.


//////////////////////////////////////////////////////////////////////////////////////////


ShortCut for Keyboard Panel:

<						Keyboard Transpose down.
>						Keyboard Transpose up.

ysxdcvgbhnjm,l.e-		Musical Keys.

";

		// ///////////////////////// Menu Density /////////////////////////////////

		menuFile = Menu(
			MenuAction("Load File for Analyze",
				{Dialog.openPanel({ arg path;
					listeFileAnalyze.do({arg buffer; buffer.free});
					listeFileAnalyze=[];
					listeNameFileAnalyze=[];
					bufferFile.free;
					fonctionLoadFileForAnalyse.value(path);
					// Setup GUI Value
					windowEar.view.children.at(6).string = "FileIn :" + PathName.new(path).fileName},{"cancelled".postln})}),
			Menu(
				MenuAction("On", {synthFileIn.set('loop', 1)}),
				MenuAction("Off", {synthFileIn.set('loop', 0)});
			).title_("Loop");
		);
		MainMenu.register(menuFile.title_("File for Analyze"), "DensityTools");

		menuPreset = Menu(
			MenuAction("Load Preset", {Dialog.openPanel({ arg path, file;
				file = File(path,"r");
				fonctionLoadPreset.value(file.readAllString.interpret);
				file.close;
				windowEar.name="Density" + typeMasterOut + " | " + PathName.new(path).fileName;
			},
			{"cancelled".postln})}),
			MenuAction("Save Preset",{Dialog.savePanel({arg path, name, pathonly, fileName, file;
				path = PathName.new(path);
				pathonly = path.pathOnly;
				name = path.fileName;
				//name = "preset" + name;
				path = pathonly ++ name;
				fileName = PathName.new(path).fileName;
				path = PathName.new(path).fullPath;
				file = File(path ++ ".scd", "w");
				file.write(fonctionSavePreset.value(listeWindows).asCompileString);
				file.close;
				windowEar.name = "Density" + typeMasterOut + " | " + fileName;
			}, {"cancelled".postln})});
		);
		MainMenu.register(menuPreset.title_("Preset"), "DensityTools");

		menuInitAll = Menu(
			MenuAction("Init All", {arg file;
				//Init Orchestra Sounds FX
				s.bind{
					file = File(pathData ++ "Synth.scd","r");
					synthOrchestra = file.readAllString.interpret;
					file.close;
					s.sync;
					file = File(pathData ++ "Sounds.scd","r");
					soundOrchestra = file.readAllString.interpret;
					file.close;
					fonctionLoadSoundOrchestra.value(soundOrchestra);
					s.sync;
					file = File(pathData ++ "FX.scd","r");
					fxOrchestra = file.readAllString.interpret;
					file.close;
					s.sync;
				};
			}),
			Menu(
				MenuAction("Load", {Dialog.openPanel({arg path, file;
					file = File(path,"r");
					synthOrchestra = file.readAllString.interpret;
					file.close;
				},
				{"cancelled".postln});
				}),
				MenuAction("Edit/Save", {arg window, text, file, comUnix;
					window = Document.new("Synth Edit/Save", synthOrchestra.asCompileString).front.onClose = {text = window.string.asCompileString.interpret; synthOrchestra = text.interpret};
				});
			).title_("Synth"),
			Menu(
				MenuAction("Load", {Dialog.openPanel({arg path, file;
					file = File(path,"r");
					soundOrchestra = file.readAllString.interpret;
					file.close;
					s.bind{
						fonctionLoadSoundOrchestra.value(soundOrchestra);
						s.sync;
					};
				},
				{"cancelled".postln});
				}),
				MenuAction("Edit/Save", {arg window, text;
					window = Document.new("Sounds Edit/Save", soundOrchestra.asCompileString).front.onClose = {text = window.string.asCompileString.interpret; soundOrchestra = text.interpret};
					s.bind{
						fonctionLoadSoundOrchestra.value(soundOrchestra);
						s.sync;
					};
				});
			).title_("Sound"),
			Menu(
				MenuAction("Load", {Dialog.openPanel({arg path, file;
					file = File(path,"r");
					fxOrchestra = file.readAllString.interpret;
					file.close;
				},
				{"cancelled".postln});
				}),
				MenuAction("Edit/Save", {arg window, text;
					window = Document.new("FX Edit/Save", fxOrchestra.asCompileString).front.onClose = {text = window.string.asCompileString.interpret; fxOrchestra = text.interpret};
				});
			).title_("FX")
		);
		MainMenu.register(menuInitAll.title_("Synth/Sound/FX"), "DensityTools");

		menuRecording = Menu(
			MenuAction("Start Recording", {
				fonctionRecOn.value;
			}),
			MenuAction("Stop Recording", {
				fonctionRecOff.value;
			}),
			MenuAction("Switch Pause Recording On/Off", {
				fonctionRecPause.value;
			});
		);
		MainMenu.register(menuRecording.title_("Recording"), "DensityTools");

		menuAudio = Menu(
			MenuAction("Channels FhzBand Out", {
				SCRequestString("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", "Channels", {arg strg;
					channelsSynth = strg.value.interpret; channelsSynth = channelsSynth.max(1) - 1;
				});
			}),
			MenuAction("Channels Reverb Out", {
				SCRequestString("1", "Channels", {arg strg;
					channelsVerb = strg.value.asInteger; channelsVerb = channelsVerb.max(1) - 1;
					groupeVerb.set(\out, channelsVerb.value);
				});
			}),
			MenuAction("Stereo", {recChannels = 2; numberAudioOut = 2;
				s.recChannels_(recChannels);
				s.options.numInputBusChannels_(20);
				//s.options.numOutputBusChannels_(8);
				typeMasterOut = listeMasterOut.at(0);
				startSystem.valueAction_(0);
				groupeVerb.freeAll;
				groupeLimiter.freeAll;
				groupeVST.freeAll;
				this.initSynthDef;
				gVerb = Synth.new("GVerb Stereo", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				freeVerb = Synth.newPaused("FreeVerb Stereo", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				allPass = Synth.newPaused("Allpass Stereo", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				jpVerb = Synth.newPaused("JPverb Stereo", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				synthVST = Synth.newPaused("VST Stereo", [\out, channelsVerb, \xFade, 0.5, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
				fxVST = VSTPluginController(synthVST);
				// Setup GUI Value
				windowGVerb.view.children.at(0).valueAction_(0);
				Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
				// Setup GUI Value
				windowEar.view.children.at(5).children.at(2).valueAction_(0.8);
				windowGVerb.name = "Reverb Stereo"; windowEar.name = "Reverb Stereo"; windowVST.name = "VST Stereo";
			}),
			MenuAction("Rotate2", {recChannels = 2; numberAudioOut = 2;
				s.recChannels_(recChannels);
				s.options.numInputBusChannels_(20);
				//s.options.numOutputBusChannels_(8);
				typeMasterOut = listeMasterOut.at(1);
				startSystem.valueAction_(0);
				groupeVerb.freeAll;
				groupeLimiter.freeAll;
				groupeVST.freeAll;
				this.initSynthDef;
				Synth.new("GVerb Rotate2", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				freeVerb = Synth.newPaused("FreeVerb Rotate2", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				allPass = Synth.newPaused("Allpass Rotate2", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				jpVerb = Synth.newPaused("JPverb Rotate2", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				synthVST = Synth.newPaused("VST Rotate2", [\out, channelsVerb, \xFade, 0.5, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
				fxVST = VSTPluginController(synthVST);
				// Setup GUI Value
				windowGVerb.view.children.at(0).valueAction_(0);
				Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
				// Setup GUI Value
				windowEar.view.children.at(5).children.at(2).valueAction_(0.8);
				windowGVerb.name = "Reverb Rotate2"; windowEar.name = "Reverb Rotate2"; windowVST.name = "VST Rotate2";
			}),
			MenuAction("MultiSpeaker", {
				SCRequestString("2", "Channels", {arg strg; recChannels = strg.asInteger; numberAudioOut = strg.asInteger;
					s.recChannels_(recChannels);
					s.options.numInputBusChannels_(20);
					//s.options.numOutputBusChannels_(8);
					typeMasterOut = listeMasterOut.at(2);
					startSystem.valueAction_(0);
					groupeVerb.freeAll;
					groupeLimiter.freeAll;
					groupeVST.freeAll;
					this.initSynthDef;
					Synth.new("GVerb MultiSpeaker", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					freeVerb = Synth.newPaused("FreeVerb MultiSpeaker", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					allPass = Synth.newPaused("Allpass MultiSpeaker", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					jpVerb = Synth.newPaused("JPverb MultiSpeaker", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					synthVST = Synth.newPaused("VST MultiSpeaker", [\out, channelsVerb, \xFade, 0.5, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
					fxVST = VSTPluginController(synthVST);
					// Setup GUI Value
					windowGVerb.view.children.at(0).valueAction_(0);
					Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
					// Setup GUI Value
					windowEar.view.children.at(5).children.at(2).valueAction_(0.8);
					windowGVerb.name = "Reverb MultiSpeaker"; windowEar.name = "Reverb MultiSpeaker"; windowVST.name = "VST MultiSpeaker";
				});
			}),
			MenuAction("Ambisonic", {
				SCRequestString("2", "Channels", {arg strg; recChannels = strg.asInteger; numberAudioOut = strg.asInteger;
					s.recChannels_(recChannels);
					s.options.numInputBusChannels_(20);
					//s.options.numOutputBusChannels_(8);
					typeMasterOut = listeMasterOut.at(3);
					startSystem.valueAction_(0);
					groupeVerb.freeAll;
					groupeLimiter.freeAll;
					groupeVST.freeAll;
					this.initSynthDef;
					Synth.new("GVerb Ambisonic", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					freeVerb = Synth.newPaused("FreeVerb Ambisonic", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					allPass = Synth.newPaused("Allpass Ambisonic", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					jpVerb = Synth.newPaused("JPverb Ambisonic", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
					synthVST = Synth.newPaused("VST Ambisonic", [\out, channelsVerb, \xFade, 0.5, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
					fxVST = VSTPluginController(synthVST);
					// Setup GUI Value
					windowGVerb.view.children.at(0).valueAction_(0);
					Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
					// Setup GUI Value
					windowEar.view.children.at(5).children.at(2).valueAction_(0.8);
					windowGVerb.name = "Reverb Ambisonic"; windowEar.name = "Reverb Ambisonic"; windowVST.name = "VST Ambisonic";
				});
			}),
			MenuAction("Dolby5.1", {recChannels = 6; numberAudioOut = 6;
				s.recChannels_(recChannels);
				s.options.numInputBusChannels_(20);
				//s.options.numOutputBusChannels_(8);
				typeMasterOut = listeMasterOut.at(4);
				startSystem.valueAction_(0);
				groupeVerb.freeAll;
				groupeLimiter.freeAll;
				groupeVST.freeAll;
				this.initSynthDef;
				Synth.new("GVerb Dolby5.1", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				freeVerb = Synth.newPaused("FreeVerb Dolby5.1", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				allPass = Synth.newPaused("Allpass Dolby5.1", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				jpVerb = Synth.newPaused("JPverb Dolby5.1", [\out, channelsVerb, \xFade, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
				synthVST = Synth.newPaused("VST Dolby5.1", [\out, channelsVerb, \xFade, 0.5, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
				fxVST = VSTPluginController(synthVST);
				// Setup GUI Value
				windowGVerb.view.children.at(0).valueAction_(0);
				Synth.new("SynthLimiter", [\limit, 0.8], groupeLimiter, \addToTail);
				// Setup GUI Value
				windowEar.view.children.at(5).children.at(2).valueAction_(0.8);
				windowGVerb.name = "Reverb Dolby5.1"; windowEar.name = "Reverb Dolby5.1"; windowVST.name = "VST Dolby5.1";
			});
		);
		MainMenu.register(menuAudio.title_("Audio"), "DensityTools");

		menuMIDI = Menu(
			MenuAction("Init", {
				MIDIClient.init;
				if(MIDIClient.externalSources != [ ], {
					// Connect first device by default
					MIDIIn.connect(0, 0);
					midiOut = MIDIOut(0);
					//midiOut.connect(0);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {fxVST.midi.allNotesOff(canal)})});
				}, {"Warning no MIDI Devices Connected".postln});
			}),
			Menu(
				MenuAction("Connect IN", {
					SCRequestString("0", "Device", {arg index, port;
						port = index.asInteger;
						MIDIIn.connect(port, MIDIClient.sources.at(port));
					});
				}),
				MenuAction("Connect Out", {
					SCRequestString("0", "Device", {arg index, port;
						port = index.asInteger;
						midiOut = MIDIOut(port);
						//midiOut.connect(port);
						16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {fxVST.midi.allNotesOff(canal)})});
					});
				});
			).title_("Setting");
		);
		MainMenu.register(menuMIDI.title_("Midi"), "DensityTools");

		menuOSC = Menu(
			MenuAction("Setting", {var addrM, addrS;
				// Set OSC Addresse et Port Master
				addrM=NetAddr.localAddr;
				addrS=NetAddr.localAddr;
				slaveAppAddr.disconnect;
				SCRequestString(addrM.ip, "Enter the NetAddr of Master App", {arg strg; addrM=strg;
					SCRequestString(NetAddr.langPort.asString, "Enter the Port of Master App", {arg strg; addrM=NetAddr(addrM, strg.asInteger); masterAppAddr = addrM;
						// Set OSC Addresse et Port Slave
						SCRequestString(addrS.ip, "Enter the NetAddr of Slave App", {arg strg; addrS=strg;
							SCRequestString(NetAddr.langPort.asString, "Enter the Port of Slave App", {arg strg; addrS=NetAddr(addrS, strg.asInteger); slaveAppAddr = addrS;
								//initOSCresponder.value;
							});
						});
					});
				});
			}),
			MenuAction("OSC Master", {oscStateFlag='master';
				"Density is OSC now !".postln;
			}),
			MenuAction("OSC Slave", {oscStateFlag='slave';
				"Density is OSC SLAVE now !".postln;
			}),
			MenuAction("OSC Off", {oscStateFlag='off';
				"OSC is OFF now !".postln;
			});
		);
		MainMenu.register(menuOSC.title_("OSC"), "DensityTools");

		menuAlgo = Menu(
			MenuAction("Not Activate", {nil}),
		);
		MainMenu.register(menuAlgo.title_("Algorithm"), "DensityTools");

		menuHelp = MenuAction("Help ShortCut", {
			//Document.new("ShortCut for Density", helpDensity);
			TextView().name_("ShortCut for Density").string_(helpDensity).front;
		});
		MainMenu.register(menuHelp, "DensityTools");

		// Fonction ShortCut
		fonctionShortCut = {arg window;
			window.view.keyDownAction = {arg view,char,modifiers,unicode, keycode, number, file;
				number = nil;
				//[char,modifiers,unicode,keycode].postln;
				// Touches pave numerique
				if(modifiers==2097152 and: {unicode==49} and: {keycode==83},{number = 1});
				if(modifiers==2097152 and: {unicode==50} and: {keycode==84},{number = 2});
				if(modifiers==2097152 and: {unicode==51} and: {keycode==85},{number = 3});
				if(modifiers==2097152 and: {unicode==52} and: {keycode==86},{number = 4});
				if(modifiers==2097152 and: {unicode==53} and: {keycode==87},{number = 5});
				if(modifiers==2097152 and: {unicode==54} and: {keycode==88},{number = 6});
				if(modifiers==2097152 and: {unicode==55} and: {keycode==89},{number = 7});
				if(modifiers==2097152 and: {unicode==56} and: {keycode==91},{number = 8});
				if(modifiers==2097152 and: {unicode==57} and: {keycode==92},{number = 9});
				if(modifiers==2097152 and: {unicode==48} and: {keycode==82},{number = 10});
				if(modifiers==2228224 and: {unicode==49} and: {keycode==83},{number = 11});
				if(modifiers==2228224 and: {unicode==50} and: {keycode==84},{number = 12});
				if(modifiers==2228224 and: {unicode==51} and: {keycode==85},{number = 13});
				if(modifiers==2228224 and: {unicode==52} and: {keycode==86},{number = 14});
				if(modifiers==2228224 and: {unicode==53} and: {keycode==87},{number = 15});
				if(modifiers==2228224 and: {unicode==54} and: {keycode==88},{number = 16});
				if(modifiers==2228224 and: {unicode==55} and: {keycode==89},{number = 17});
				if(modifiers==2228224 and: {unicode==56} and: {keycode==91},{number = 18});
				if(modifiers==2228224 and: {unicode==57} and: {keycode==92},{number = 19});
				if(modifiers==2228224 and: {unicode==48} and: {keycode==82},{number = 20});
				if(modifiers==2621440 and: {unicode==49} and: {keycode==83},{number = 21});
				if(modifiers==2621440 and: {unicode==50} and: {keycode==84},{number = 22});
				if(modifiers==2621440 and: {unicode==51} and: {keycode==85},{number = 23});
				if(modifiers==2621440 and: {unicode==52} and: {keycode==86},{number = 24});
				if(modifiers==2621440 and: {unicode==53} and: {keycode==87},{number = 25});
				if(modifiers==2621440 and: {unicode==54} and: {keycode==88},{number = 26});
				if(modifiers==2621440 and: {unicode==55} and: {keycode==89},{number = 27});
				if(modifiers==2621440 and: {unicode==56} and: {keycode==91},{number = 28});
				if(modifiers==2621440 and: {unicode==57} and: {keycode==92},{number = 29});
				if(modifiers==2621440 and: {unicode==48} and: {keycode==82},{number = 30});
				if(modifiers==2752512 and: {unicode==49} and: {keycode==83},{number = 31});
				if(modifiers==2752512 and: {unicode==50} and: {keycode==84},{number = 32});
				if(modifiers==2752512 and: {unicode==51} and: {keycode==85},{number = 33});
				if(modifiers==2752512 and: {unicode==52} and: {keycode==86},{number = 34});
				if(modifiers==2752512 and: {unicode==53} and: {keycode==87},{number = 35});
				if(modifiers==2752512 and: {unicode==54} and: {keycode==88},{number = 36});
				if(modifiers==2752512 and: {unicode==55} and: {keycode==89},{number = 37});
				if(modifiers==2752512 and: {unicode==56} and: {keycode==91},{number = 38});
				if(modifiers==2752512 and: {unicode==57} and: {keycode==92},{number = 39});
				if(modifiers==2752512 and: {unicode==48} and: {keycode==82},{number = 40});
				// Touches clavier numerique
				if(modifiers==0 and: {unicode==49} and: {keycode==18},{number = 1});
				if(modifiers==0 and: {unicode==50} and: {keycode==19},{number = 2});
				if(modifiers==0 and: {unicode==51} and: {keycode==20},{number = 3});
				if(modifiers==0 and: {unicode==52} and: {keycode==21},{number = 4});
				if(modifiers==0 and: {unicode==53} and: {keycode==23},{number = 5});
				if(modifiers==0 and: {unicode==54} and: {keycode==22},{number = 6});
				if(modifiers==0 and: {unicode==55} and: {keycode==26},{number = 7});
				if(modifiers==0 and: {unicode==56} and: {keycode==28},{number = 8});
				if(modifiers==0 and: {unicode==57} and: {keycode==25},{number = 9});
				if(modifiers==0 and: {unicode==48} and: {keycode==29},{number = 10});
				if(modifiers==131072 and: {unicode==43} and: {keycode==18},{number = 11});
				if(modifiers==131072 and: {unicode==34} and: {keycode==19},{number = 12});
				if(modifiers==131072 and: {unicode==42} and: {keycode==20},{number = 13});
				if(modifiers==131072 and: {unicode==231} and: {keycode==21},{number = 14});
				if(modifiers==131072 and: {unicode==37} and: {keycode==23},{number = 15});
				if(modifiers==131072 and: {unicode==38} and: {keycode==22},{number = 16});
				if(modifiers==131072 and: {unicode==47} and: {keycode==26},{number = 17});
				if(modifiers==131072 and: {unicode==40} and: {keycode==28},{number = 18});
				if(modifiers==131072 and: {unicode==41} and: {keycode==25},{number = 19});
				if(modifiers==131072 and: {unicode==61} and: {keycode==29},{number = 20});
				if(modifiers==524288 and: {unicode==177} and: {keycode==18},{number = 21});
				if(modifiers==524288 and: {unicode==8220} and: {keycode==19},{number = 22});
				if(modifiers==524288 and: {unicode==35} and: {keycode==20},{number = 23});
				if(modifiers==524288 and: {unicode==199} and: {keycode==21},{number = 24});
				if(modifiers==524288 and: {unicode==91} and: {keycode==23},{number = 25});
				if(modifiers==524288 and: {unicode==93} and: {keycode==22},{number = 26});
				if(modifiers==524288 and: {unicode==124} and: {keycode==26},{number = 27});
				if(modifiers==524288 and: {unicode==123} and: {keycode==28},{number = 28});
				if(modifiers==524288 and: {unicode==125} and: {keycode==25},{number = 29});
				if(modifiers==524288 and: {unicode==8800} and: {keycode==29},{number = 30});
				if(modifiers==655360 and: {unicode==8734} and: {keycode==18},{number = 31});
				if(modifiers==655360 and: {unicode==8221} and: {keycode==19},{number = 32});
				if(modifiers==655360 and: {unicode==8249} and: {keycode==20},{number = 33});
				if(modifiers==655360 and: {unicode==8260} and: {keycode==21},{number = 34});
				if(modifiers==655360 and: {unicode==91} and: {keycode==23},{number = 35});
				if(modifiers==655360 and: {unicode==93} and: {keycode==22},{number = 36});
				if(modifiers==655360 and: {unicode==92} and: {keycode==26},{number = 37});
				if(modifiers==655360 and: {unicode==210} and: {keycode==28},{number = 38});
				if(modifiers==655360 and: {unicode==212} and: {keycode==25},{number = 39});
				if(modifiers==655360 and: {unicode==218} and: {keycode==29},{number = 40});
				// Execute Commande
				if(shortCutCommande != nil and: {number != nil}, {
					fonctionShortCutCommande.value(number);
				});
				// key esc or SpaceBar-> All System on/off
				if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}}, {
					if(startSystem.value == 0, {startSystem.valueAction_(1)}, {startSystem.valueAction_(0)});
				});
				// key q -> Switch Algo Analyze
				if(modifiers==0 and: {unicode==113} and: {keycode==12}, {
					if(typeAlgoAnalyze >= 5, {switchAnalyze.valueAction_(0)}, {switchAnalyze.valueAction_(switchAnalyze.value + 1)});
				});
				// key ctrl + q -> Switch Algo Analyze
				if(modifiers==262144 and: {unicode==17} and: {keycode==12}, {
					if(typeAlgoAnalyze <= 0, {switchAnalyze.valueAction_(4)}, {switchAnalyze.valueAction_(switchAnalyze.value - 1)});
				});
				// Key h -> Switch source In.
				if(modifiers==0 and: {unicode==104} and: {keycode==4},  {if(switchSourceIn.value >= 1, {switchSourceIn.valueAction_(0)},
					{switchSourceIn.valueAction_(switchSourceIn.value + 1)});
				});
				// key i -> Init Synth
				if(modifiers == 0 and: {unicode==105} and: {keycode==34}, {
					listeDataInstruments.do({arg data, index;
						data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
					});
				});
				// Key alt + i -> Init all system
				if(modifiers == 524288 and: {unicode==105} and: {keycode==34},{
					listeDataInstruments.do({arg data, index;
						data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
					});
					groupeRecBuffer.freeAll;
					groupeSynth.freeAll;
					groupeFX.freeAll;
					groupeMasterOut.freeAll;
					dataFlux = [];
					dataFlatness = [];
					dataCentroid = [];
					dataEnergy = [];
					dataBPM = [];
					indexDataFlux = [];
					indexDataFlatness = [];
					indexDataCentroid = [];
					indexDataEnergy = [];
					indexDataBPM = [];
					memoryDataFlux = [];
					memoryDataFlatness = [];
					memoryDataCentroid = [];
					memoryDataEnergy = [];
					memoryDataBPM = [];
					dataFreq = [];
					dataAmp = [];
					dataDuree = [];
					indexDataFreq = [];
					indexDataAmp = [];
					indexDataDuree = [];
					memoryDataFreq = [];
					memoryDataAmp = [];
					memoryDataDuree = [];
					lastTime = [];
					// Init Array
					(numFhzBand + 1).do({arg i;
						dataFlux = dataFlux.add([]);
						dataFlatness = dataFlatness.add([]);
						dataCentroid = dataCentroid.add([]);
						dataEnergy = dataEnergy.add([]);
						dataBPM = dataBPM.add([]);
						indexDataFlux = indexDataFlux.add(0);
						indexDataFlatness = indexDataFlatness.add(0);
						indexDataCentroid = indexDataCentroid.add(0);
						indexDataEnergy = indexDataEnergy.add(0);
						indexDataBPM = indexDataBPM.add(0);
						memoryDataFlux = memoryDataFlux.add([]);
						memoryDataFlatness = memoryDataFlatness.add([]);
						memoryDataCentroid = memoryDataCentroid.add([]);
						memoryDataEnergy = memoryDataEnergy.add([]);
						memoryDataBPM = memoryDataBPM.add([]);
						dataFreq = dataFreq.add([]);
						dataAmp = dataAmp.add([]);
						dataDuree = dataDuree.add([]);
						indexDataFreq = indexDataFreq.add(0);
						indexDataAmp = indexDataAmp.add(0);
						indexDataDuree = indexDataDuree.add(0);
						memoryDataFreq = memoryDataFreq.add([]);
						memoryDataAmp = memoryDataAmp.add([]);
						memoryDataDuree = memoryDataDuree.add([]);
						lastTime = lastTime.add(Main.elapsedTime);
					});
					freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
					numIndexSynthBand = 0;
					if(File.exists(pathData ++ "Init Preset.scd"), {
						windowEar.name = "Density" + " | " + "Init Preset";
						file=File(pathData ++ "Init Preset.scd", "r");
						fonctionLoadPreset.value(file.readAllString.interpret);
						file.close;
					}, {"cancelled".postln});
				});
				// Key ctrl + i -> Reset Synth + FX + MasterOut ...
				if(modifiers==262144 and: {unicode==9} and: {keycode==34},{
					listeDataInstruments.do({arg data, index;
						data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
					});
					dataFlux = [];
					dataFlatness = [];
					dataCentroid = [];
					dataEnergy = [];
					dataBPM = [];
					indexDataFlux = [];
					indexDataFlatness = [];
					indexDataCentroid = [];
					indexDataEnergy = [];
					indexDataBPM = [];
					memoryDataFlux = [];
					memoryDataFlatness = [];
					memoryDataCentroid = [];
					memoryDataEnergy = [];
					memoryDataBPM = [];
					dataFreq = [];
					dataAmp = [];
					dataDuree = [];
					indexDataFreq = [];
					indexDataAmp = [];
					indexDataDuree = [];
					memoryDataFreq = [];
					memoryDataAmp = [];
					memoryDataDuree = [];
					lastTime = [];
					// Init Array
					(numFhzBand + 1).do({arg i;
						dataFlux = dataFlux.add([]);
						dataFlatness = dataFlatness.add([]);
						dataCentroid = dataCentroid.add([]);
						dataEnergy = dataEnergy.add([]);
						dataBPM = dataBPM.add([]);
						indexDataFlux = indexDataFlux.add(0);
						indexDataFlatness = indexDataFlatness.add(0);
						indexDataCentroid = indexDataCentroid.add(0);
						indexDataEnergy = indexDataEnergy.add(0);
						indexDataBPM = indexDataBPM.add(0);
						memoryDataFlux = memoryDataFlux.add([]);
						memoryDataFlatness = memoryDataFlatness.add([]);
						memoryDataCentroid = memoryDataCentroid.add([]);
						memoryDataEnergy = memoryDataEnergy.add([]);
						memoryDataBPM = memoryDataBPM.add([]);
						dataFreq = dataFreq.add([]);
						dataAmp = dataAmp.add([]);
						dataDuree = dataDuree.add([]);
						indexDataFreq = indexDataFreq.add(0);
						indexDataAmp = indexDataAmp.add(0);
						indexDataDuree = indexDataDuree.add(0);
						memoryDataFreq = memoryDataFreq.add([]);
						memoryDataAmp = memoryDataAmp.add([]);
						memoryDataDuree = memoryDataDuree.add([]);
						lastTime = lastTime.add(Main.elapsedTime);
					});
					freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
					numIndexSynthBand = 0;
				});
				// key ctrl + f -> Load File for Analyze
				if(modifiers==262144 and: {unicode==6} and: {keycode==3}, {
					Dialog.openPanel({arg path;
						listeFileAnalyze = listeFileAnalyze.add(fonctionLoadFileForAnalyse.value(path));
						// Setup GUI Value
						windowEar.view.children.at(6).string = "File In :" + PathName.new(path).fileName;
						listeNameFileAnalyze = listeNameFileAnalyze.add(PathName.new(path).fileName)},{"cancelled".postln})});
				// key f -> Switch buffer for Analyze
				if(modifiers==0 and: {unicode==102} and: {keycode==3},
					{shortCutCommande = 'Switch File for Analyze';
				});
				// key w -> affichage windows ->
				if(modifiers==0 and: {unicode==119} and: {keycode==13}, {indexWindows=indexWindows+1;
					if(indexWindows > (listeWindows.size - 1), {indexWindows=0});
					listeWindows.at(indexWindows).front;
				});
				// Key ctrl + w -> affichage windows <-
				if(modifiers==262144 and: {unicode==23} and: {keycode==13},{indexWindows=indexWindows-1;
					if(indexWindows < 0, {indexWindows=listeWindows.size - 1});
					listeWindows.at(indexWindows).front;
				});
				// key s -> save Preset
				if(modifiers==0 and: {unicode==115} and: {keycode==1},
					{shortCutCommande = 'Save Preset';
				});
				// key l -> load Preset
				if(modifiers==0 and: {unicode==108} and: {keycode==37}, {shortCutCommande = 'Load Preset';
				});
				// Key z -> load Preset aleatoire
				if(char == $z, {number = rrand(0, foldersToScanPreset.size - 1);
					if(File.exists(pathData ++ foldersToScanPreset.at(number)),
						{file=File(pathData ++ foldersToScanPreset.at(number),"r");
							windowEar.name = "Density" + typeMasterOut + " | " + foldersToScanPreset.at(number);
							fonctionLoadPreset.value(file.readAllString.interpret);
							file.close}, {"cancelled".postln});});
				//key k
				if(char == $k, {
					FileDialog.new({arg path;
						pathData = path.at(0).asString ++"/";
						windowEar.name="Density" + " | " + pathData.asString;
						fonctionCollectFolders.value;
					}, fileMode: 2);
				});
				//key a // init genetic
				if(char == $a, {
					// For Genetic
					geneticF = HPclassGenetiques.new(1, 127);
					geneticA = HPclassGenetiques.new(1, 127);
					geneticD = HPclassGenetiques.new(1, 127);
				});
				//key shift+a // init Kohonen
				if(modifiers == 131072 and: {unicode==65} and: {keycode==0}, {
					// For Kohonen
					kohonenF = HPclassKohonen.new(1,127,1);
					kohonenA = HPclassKohonen.new(1,127,1);
					kohonenD = HPclassKohonen.new(1,127,1);
				});
				//key alt+a // init Neural
				if(modifiers == 524288 and: {unicode==97} and: {keycode==0}, {
					// For Neural
					neuralFAD = HPNeuralNet.new(3, 1, [9], 3);
				});
			};
		};

		// Fonction Commandes
		fonctionShortCutCommande = {arg number, file;
			if(shortCutCommande == 'Save Preset', {
				windowEar.name = "Density" + " | " + "Preset" + number.asString;
				file = File(pathData ++ "Preset" + number.asString ++ ".scd", "w");
				file.write(fonctionSavePreset.value(listeWindows).asCompileString);
				file.close;
			});
			if(shortCutCommande == 'Load Preset', {
				if(File.exists(pathData ++ "Preset" + number.value.asString ++ ".scd"), {
					listeDataInstruments.do({arg data, index;
						data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
					});
					windowEar.name = "Density" + " | " + "Preset" + number.asString;
					file=File(pathData ++ "Preset" + number.value.asString ++ ".scd", "r");
					fonctionLoadPreset.value(file.readAllString.interpret);
					file.close;
				}, {"cancelled".postln});
			});
			if(shortCutCommande == 'Switch File for Analyze', {
				if(listeFileAnalyze.at(number - 1) != nil, {
					s.bind{
						synthFileIn.set(\trigger, 0);
						synthFileIn.set(\bufferplay, listeFileAnalyze.at(number - 1));
						s.sync;
						synthFileIn.set(\trigger, 1);
						s.sync;
					};
					// Setup GUI Value
					windowEar.view.children.at(6).string = "File In :" + listeNameFileAnalyze.at(number - 1);
				}, {"cancelled".postln});
			});
			shortCutCommande = nil;
		};

		// ShortCuts for Keyboard
		keyboardShortCut = {arg window, lastNote=60;
			// Down
			window.view.keyDownAction = {arg view,char,modifiers,unicode,keycode;
				// [char,modifiers,unicode,keycode].postln;
				// Translate
				// Key / -> Keyboard transpose down
				if(char == $<,  {keyboardTranslate.valueAction_(keyboardTranslate.value - 1);
				});
				// Key / -> Keyboard transpose up
				if(char == $>,  {keyboardTranslate.valueAction_(keyboardTranslate.value + 1);
				});
				// Musical keys
				if(char == $y, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (60 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 60 + keyboardTranslate.value;
					keyboard.setColor(60 + keyboardTranslate.value, Color.red);
				});
				if(char == $s, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (61 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 61 + keyboardTranslate.value;
					keyboard.setColor(61 + keyboardTranslate.value, Color.red);
				});
				if(char == $x, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (62 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 62 + keyboardTranslate.value;
					keyboard.setColor(62 + keyboardTranslate.value, Color.red);
				});
				if(char == $d, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (63 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 63 + keyboardTranslate.value;
					keyboard.setColor(63 + keyboardTranslate.value, Color.red);
				});
				if(char == $c, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (64 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 64 + keyboardTranslate.value;
					keyboard.setColor(64 + keyboardTranslate.value, Color.red);
				});
				if(char == $v, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (65 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 65 + keyboardTranslate.value;
					keyboard.setColor(65 + keyboardTranslate.value, Color.red);
				});
				if(char == $g, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (66 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 66 + keyboardTranslate.value;
					keyboard.setColor(66 + keyboardTranslate.value, Color.red);
				});
				if(char == $b, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (67 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 67 + keyboardTranslate.value;
					keyboard.setColor(67 + keyboardTranslate.value, Color.red);
				});
				if(char == $h, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (68 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 68 + keyboardTranslate.value;
					keyboard.setColor(68 + keyboardTranslate.value, Color.red);
				});
				if(char == $n, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (69 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 69 + keyboardTranslate.value;
					keyboard.setColor(69 + keyboardTranslate.value, Color.red);
				});
				if(char == $j, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (70 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 70 + keyboardTranslate.value;
					keyboard.setColor(70 + keyboardTranslate.value, Color.red);
				});
				if(char == $m, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (71 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 71 + keyboardTranslate.value;
					keyboard.setColor(71 + keyboardTranslate.value, Color.red);
				});
				if(char == $,, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (72 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 72 + keyboardTranslate.value;
					keyboard.setColor(72 + keyboardTranslate.value, Color.red);
				});
				if(char == $l, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (73 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 73 + keyboardTranslate.value;
					keyboard.setColor(73 + keyboardTranslate.value, Color.red);
				});
				if(char == $., {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (74 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 74 + keyboardTranslate.value;
					keyboard.setColor(74 + keyboardTranslate.value, Color.red);
				});
				// Key �
				if(modifiers==0 and: {unicode==233} and: {keycode==41}, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (75 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 75 + keyboardTranslate.value;
					keyboard.setColor(60 + keyboardTranslate.value, Color.red);
				});
				if(char == $-, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (76 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 76 + keyboardTranslate.value;
					keyboard.setColor(76 + keyboardTranslate.value, Color.red);
				});
			};
		};

		//////////////////////////////// GUI //////////////////////////////////

		////////////////////////// Window VST ///////////////////////////////
		windowVST = Window.new("VST Stereo", Rect(710, 650, 320, 80), scroll: true);
		windowVST.view.decorator = FlowLayout(windowVST.view.bounds);
		Button(windowVST, Rect(0, 0, 50, 20)).
		states_([["Run On", Color.green], ["Run Off", Color.red]]).
		action = {arg shortcut;
			switch (shortcut.value,
				0, {synthVST.run(false); flagVST = 'off'},
				1, {synthVST.run(true); flagVST = 'on'};
			);
		};
		Button(windowVST, Rect(0, 0, 60, 20)).
		states_([["Browse", Color.white]]).
		action = {arg shortcut;
			fxVST.browse;
		};
		Button(windowVST, Rect(0, 0, 40, 20)).
		states_([["Editor", Color.white]]).
		action = {arg shortcut;
			fxVST.editor;
		};
		Button(windowVST, Rect(0, 0, 30, 20)).
		states_([["GUI", Color.white]]).
		action = {arg shortcut;
			fxVST.gui;
		};
		Button(windowVST, Rect(0, 0, 40, 20)).
		states_([["Close", Color.white]]).
		action = {arg shortcut;
			synthVST.free;
			fxVST.close;
			// New VST
			synthVST = Synth.newPaused("VST"+ typeMasterOut, [\xFade, 0.5, \panLo, 0, \panHi, 0, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
			fxVST = VSTPluginController(synthVST);
		};
		PopUpMenu(windowVST,Rect(0, 0, 70, 20)).items_(["Out 1", "Out 2", "Out 3", "Out 4", "Out 5", "Out 6", "Out 7", "Out 8", "Out 9", "Out 10", "Out 11", "Out 12", "Out 13", "Out 14", "Out 15", "Out 16", "Out 17", "Out 18", "Out 19", "Out 20", "Out 21", "Out 22", "Out 23", "Out 24", "Out 25", "Out 26", "Out 27", "Out 28", "Out 29", "Out 30", "Out 31", "Out 32"]).
		action = {arg ez;
			synthVST.set(\out, ez.value);
		};
		EZKnob(windowVST, 150 @ 25, "xFade", \unipolar,
			{|ez| groupeVST.set(\xFade, ez.value)}, 0.5, layout: \horz);
		EZKnob(windowVST, 150 @ 25, "Gain In", \unipolar,
			{|ez| groupeVST.set(\gainIn, ez.value)}, 0.5, layout: \horz);
		EZRanger(windowVST , 300 @ 20, "Pan", \bipolar,
			{|ez| groupeVST.set(\panLo, ez.value.at(0), \panHi, ez.value.at(1))}, [0, 0], labelWidth: 40, numberWidth: 40);
		windowVST.view.children.at(0).focus;
		windowVST.onClose_({groupeVST.free});
		windowVST.front;
		windowVST.view.do({arg view;
					view.children.do({arg subView;
						subView.font = Font("Helvetica", 10);
					});
				});
		fonctionShortCut.value(windowVST);

		////////////////////////// Window Keyboard ///////////////////////////////
		windowKeyboard = Window.new("Keyboard", Rect(600, 25, 625, 130), scroll: true);
		windowKeyboard.view.decorator = FlowLayout(windowKeyboard.view.bounds);
		windowKeyboard.front;
		// Setup ShortCut
		setupKeyboardShortCut = Button(windowKeyboard, Rect(0, 0, 105, 20));
		setupKeyboardShortCut.states = [["System Shortcut", Color.green], ["Keyboard Shortcut", Color.red]];
		setupKeyboardShortCut.action = {arg shortcut;
			if(shortcut.value == 1, {keyboardShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.setColor(note, Color.blue)})}, {fonctionShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.removeColor(note)});
			});
		};
		// Keyboard Translate
		keyboardTranslate = EZKnob(windowKeyboard, 130 @ 20, "Translate", ControlSpec(-36, 31, \lin, 1),
			{|ez| forBy(60 + keyboardTranslateBefore, 76 + keyboardTranslateBefore, 1, {arg note; keyboard.removeColor(note)});
				forBy(60 + ez.value, 76 + ez.value, 1, {arg note; keyboard.setColor(note, Color.blue)});
				keyboardTranslateBefore = ez.value;
		}, 0, layout: \horz);
		// Keyboard volume
		keyboardVolume = EZKnob(windowKeyboard, 130 @ 20, "Volume", \db,
			{|ez| keyVolume = ez.value.dbamp}, -9, layout: \horz);
		windowKeyboard.view.decorator.nextLine;
		// Keyboard Keys
		keyboard = MIDIKeyboard.new(windowKeyboard, Rect(5, 5, 600, 100), 7, 24);
		//forBy(60, 76, 1, {arg note; keyboard.setColor(note, Color.blue)});
		// Action Down
		keyboard.keyDownAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			synthKeyboard.set(\note, note, \amp, keyVolume, \trigger, 1);
		});
		// Action Up
		keyboard.keyUpAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			synthKeyboard.set(\note, note, \amp, 0, \trigger, 0);
		});
		// Action Track
		keyboard.keyTrackAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			s.bind{
				synthKeyboard.set(\note, note, \amp, keyVolume, \trigger, 1);
				s.sync;
				synthKeyboard.set(\note, note, \amp, 0, \trigger, 0);
				s.sync;
			};
		});
		setupKeyboardShortCut.focus;
		windowKeyboard.onClose_({nil});

		////// Window Plotter Data /////
		windowPlotterData = Window("Analyze [Freq | Amp | Duree]", Rect(710, 800, 515, 220), scroll: true);
		windowPlotterData.alpha=1.0;
		windowPlotterData.front;
		windowPlotterData.view.decorator = FlowLayout(windowPlotterData.view.bounds);
		// Display ON / OFF
		Button(windowPlotterData, Rect(0, 0, 100, 20)).states_([["Display On", Color.green], ["Display Off", Color.red]]).action_({|view| });
		// Refresh Display
		refreshDisplayDataMusic = Button(windowPlotterData,Rect(0, 0, 100, 20));
		refreshDisplayDataMusic.states = [["Refresh Plotter"]];
		refreshDisplayDataMusic.action = {|view| plotterDataGUI.value = [[0], [0], [0]]; plotterData = [[0], [0], [0]];
		};
		// Pbind Data Loop
		Button(windowPlotterData, Rect(0, 0, 150, 15)).
		states_([["Pbind Data Loop On", Color.green], ["Pbind Data Loop Off", Color.red]]).
		action = {arg val;
			switch (val.value,
				0, {loopMusic = 1},
				1, {loopMusic = inf}
			);
		};
		// Plotter
		plotterDataGUI = Plotter("Analyze Music", Rect(0, 0, 500, 180), windowPlotterData).plotMode_(\steps);
		plotterDataGUI.value = [[0], [0], [0]];
		windowPlotterData.onClose_({
		});
		refreshDisplayDataMusic.focus;

		////// Window Plotter FFT /////
		windowPlotterFFT = Window("Analyze [Flux | Flatness | Centroid | Energy | BPM]", Rect(710, 275, 515, 345), scroll: true);
		windowPlotterFFT.alpha=1.0;
		windowPlotterFFT.front;
		windowPlotterFFT.view.decorator = FlowLayout(windowPlotterFFT.view.bounds);
		// Display ON / OFF
		Button(windowPlotterFFT, Rect(0, 0, 100, 15)).states_([["Display On", Color.green], ["Display Off", Color.red]]).action_({|view| });
		// Refresh Display
		refreshDisplayFFT = Button(windowPlotterFFT,Rect(0, 0, 100, 15));
		refreshDisplayFFT.states = [["Refresh Plotter"]];
		refreshDisplayFFT.action = {|view| plotterFFTGUI.value = [[0], [0], [0], [0], [0]]; plotterFFT = [[0], [0], [0], [0], [0]];
		};
		EZKnob(windowPlotterFFT, 120 @ 15, "Speed", ControlSpec(-100, 100, \lin, 0.01),
			{|ez| if(ez.value < 0,
				{groupeAnalyse.set(\speed, ez.value.abs.reciprocal)},
				{groupeAnalyse.set(\speed, ez.value)});
		}, 24, layout: \horz);
		// Range FFT
		EZRanger(windowPlotterFFT , 500 @ 15, "Range FFT", \unipolar,
			{|ez| rangeFFT = ez.value}, [0, 1], labelWidth: 65);
		// Plotter
		plotterFFTGUI = Plotter("Analyze FFT", Rect(0, 0, 500, 300), windowPlotterFFT).plotMode_(\steps);
		plotterFFTGUI.value = [[0], [0], [0], [0], [0]];
		windowPlotterFFT.onClose_({
		});
		refreshDisplayFFT.focus;

		////////////////////////// Window GVerb ///////////////////////////////
		windowGVerb = Window.new("Reverb Stereo", Rect(620, 110, 600, 160), scroll: true);
		windowGVerb.view.decorator = FlowLayout(windowGVerb.view.bounds);
		PopUpMenu(windowGVerb, Rect(0, 0, 100, 20)).
		items_(["Reverb Off", "BathRoom", "Living Room", "Church", "Cathedral", "Canyon", "FreeVerb", "Allpass", "JPverb"]).
		action_({arg verb;
			switch(verb.value,
				//No Reverb
				0, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(0.01);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(0.01);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.5);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.5);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-inf.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-inf.dbamp);
					windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					windowGVerb.view.children.at(8).children.at(1).valueAction_
					(0);
					windowGVerb.view.children.at(8).children.at(3).valueAction_(0);
					groupeVerb.set(\panLo, 0);
					groupeVerb.set(\panHi, 0);
				},
				//BathRoom
				1, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(5/300);		windowGVerb.view.children.at(2).children.at(1).valueAction_(0.6/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.62);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.48);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-5.5.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-6.5.dbamp);
					//windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				//Living Room
				2, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(16/300);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(1.24/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.1);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.95);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-7.5.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-8.5.dbamp);
					//windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				//Church
				3, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(80/300);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(4.85/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.41);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.19);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-4.5.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-5.5.dbamp);
					//windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				//Cathedral
				4, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(243/300);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(1/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.1);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.34);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-5.5.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-4.5.dbamp);
					//windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				//Canyon
				5, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(true); freeVerb.run(false); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(300/300);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(103/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.43);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.51);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-13.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-10.dbamp);
					//windowGVerb.view.children.at(7).children.at(1).valueAction_(0);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				// FreeVerb
				6, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(false);
					windowGVerb.view.children.at(2).enabled_(false);
					windowGVerb.view.children.at(3).enabled_(false);
					windowGVerb.view.children.at(4).enabled_(false);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(false); freeVerb.run(true); allPass.run(false); jpVerb.run(false);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(0.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-1.dbamp);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				// Allpass
				7, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(false);
					windowGVerb.view.children.at(2).enabled_(false);
					windowGVerb.view.children.at(3).enabled_(false);
					windowGVerb.view.children.at(4).enabled_(false);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(false); freeVerb.run(false); allPass.run(true); jpVerb.run(false);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-3.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(-12.dbamp);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},
				// jpVerb
				8, {
					// Setup GUI Value
					windowGVerb.view.children.at(1).enabled_(true);
					windowGVerb.view.children.at(2).enabled_(true);
					windowGVerb.view.children.at(3).enabled_(true);
					windowGVerb.view.children.at(4).enabled_(true);
					windowGVerb.view.children.at(5).enabled_(true);
					windowGVerb.view.children.at(6).enabled_(true);
					gVerb.run(false); freeVerb.run(false); allPass.run(false); jpVerb.run(true);
					windowGVerb.view.children.at(1).children.at(1).valueAction_(16/300);
					windowGVerb.view.children.at(2).children.at(1).valueAction_(1.24/150);
					windowGVerb.view.children.at(3).children.at(1).valueAction_(0.1);
					windowGVerb.view.children.at(4).children.at(1).valueAction_(0.2);
					windowGVerb.view.children.at(5).children.at(1).valueAction_(-12.dbamp);
					windowGVerb.view.children.at(6).children.at(1).valueAction_(0.5);
					groupeVerb.set(\xFade, windowGVerb.view.children.at(7).children.at(1).value);
					groupeVerb.set(\panLo, windowGVerb.view.children.at(8).children.at(1).value);
					groupeVerb.set(\panHi, windowGVerb.view.children.at(8).children.at(3).value);
				},

			)
		});
		windowGVerb.view.decorator.nextLine;
		EZKnob(windowGVerb, 80 @ 80, "RoomSize", ControlSpec(0, 300, \lin),
			{|ez| groupeVerb.set(\roomsize, ez.value)}, 1, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "RevTime", ControlSpec(0, 150, \lin),
			{|ez| groupeVerb.set(\revtime, ez.value)}, 1, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "Damping", \unipolar,
			{|ez| groupeVerb.set(\inputbw, ez.value)}, 0.5, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "InputBW", \unipolar,
			{|ez| groupeVerb.set(\inputbw, ez.value)}, 0.5, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "EarlyLevel", \db,
			{|ez| groupeVerb.set(\earlylevel, ez.value.dbamp)}, -inf, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "TailLevel", \db,
			{|ez| groupeVerb.set(\taillevel, ez.value.dbamp)}, -inf, layout: \vert2);
		EZKnob(windowGVerb, 80 @ 80, "Dry/Wet", \unipolar,
			{|ez| groupeVerb.set(\xFade, ez.value)}, 0, layout: \vert2);
		EZRanger(windowGVerb , 550 @ 20, "Pan", \bipolar,
			{|ez| groupeVerb.set(\panLo, ez.value.at(0)); groupeVerb.set(\panHi, ez.value.at(1))}, [0, 0], labelWidth: 40, numberWidth: 40);
		// Setup GUI Value
		windowGVerb.view.children.at(0).focus;
		windowGVerb.onClose_({groupeVerb.free});
		windowGVerb.front;

		/////// Density Ear Panel
		windowEar = Window("Density" + typeMasterOut, Rect(0, 800, 715, 850), scroll: true);
		windowEar.alpha=1.0;
		windowEar.front;
		windowEar.view.decorator = FlowLayout(windowEar.view.bounds);
		// Audio
		StaticText(windowEar, Rect(0, 0, 500, 15)).string_("AUDIO").stringColor_(Color.yellow);
		windowEar.view.decorator.nextLine;

		// Start System
		startSystem = Button(windowEar,Rect(0, 0, 100, 20));
		startSystem.states = [["Start", Color.green], ["Stop", Color.red]];
		startSystem.action = {arg start, varTampon;
			switch (start.value,
				0, {
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {fxVST.midi.allNotesOff(canal)})});
					groupeRecBuffer.freeAll;
					groupeSynth.freeAll;
					groupeFX.freeAll;
					groupeMasterOut.freeAll;
					synthAnalyseFFT.run(false);
					synthAnalyseOnsets.run(false);
					synthAnalysePitch.run(false);
					synthAnalysePitch2.run(false);
					synthAnalyseKeyTrack.run(false);
					synthKeyboard.run(false);
					synthMIDI.run(false);
					synthAudioIn.run(false);
					synthFileIn.run(false);
					synthAnalyzeAudioIn.run(false);
					synthRecAudioIn.run(false);
					playInstruments.stop;
					watchSilence.stop;
					memoryMusic.stop;
					dataFlux = [];
					dataFlatness = [];
					dataCentroid = [];
					dataEnergy = [];
					dataBPM = [];
					indexDataFlux = [];
					indexDataFlatness = [];
					indexDataCentroid = [];
					indexDataEnergy = [];
					indexDataBPM = [];
					memoryDataFlux = [];
					memoryDataFlatness = [];
					memoryDataCentroid = [];
					memoryDataEnergy = [];
					memoryDataBPM = [];
					dataFreq = [];
					dataAmp = [];
					dataDuree = [];
					indexDataFreq = [];
					indexDataAmp = [];
					indexDataDuree = [];
					memoryDataFreq = [];
					memoryDataAmp = [];
					memoryDataDuree = [];
					lastTime = [];
					// Init Array
					(numFhzBand + 1).do({arg i;
						dataFlux = dataFlux.add([]);
						dataFlatness = dataFlatness.add([]);
						dataCentroid = dataCentroid.add([]);
						dataEnergy = dataEnergy.add([]);
						dataBPM = dataBPM.add([]);
						indexDataFlux = indexDataFlux.add(0);
						indexDataFlatness = indexDataFlatness.add(0);
						indexDataCentroid = indexDataCentroid.add(0);
						indexDataEnergy = indexDataEnergy.add(0);
						indexDataBPM = indexDataBPM.add(0);
						memoryDataFlux = memoryDataFlux.add([]);
						memoryDataFlatness = memoryDataFlatness.add([]);
						memoryDataCentroid = memoryDataCentroid.add([]);
						memoryDataEnergy = memoryDataEnergy.add([]);
						memoryDataBPM = memoryDataBPM.add([]);
						dataFreq = dataFreq.add([]);
						dataAmp = dataAmp.add([]);
						dataDuree = dataDuree.add([]);
						indexDataFreq = indexDataFreq.add(0);
						indexDataAmp = indexDataAmp.add(0);
						indexDataDuree = indexDataDuree.add(0);
						memoryDataFreq = memoryDataFreq.add([]);
						memoryDataAmp = memoryDataAmp.add([]);
						memoryDataDuree = memoryDataDuree.add([]);
						lastTime = lastTime.add(Main.elapsedTime);
					});
					freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
					numIndexSynthBand = 0;
					if(oscStateFlag == 'master', {ardourOSC.sendMsg('/ardour/transport_stop')});// transport play
				},
				1, {
					switchSourceIn.valueAction_(switchSourceIn.value);
					switchAnalyze.valueAction_(typeAlgoAnalyze);
					synthAnalyseFFT.run(true);
					// Setup GUI Value
					//windowEar.view.children.at(2).enabled_(true);
					playInstruments.play;
					watchSilence.play;
					memoryMusic.play;
					if(oscStateFlag == 'master', {ardourOSC.sendMsg('/ardour/transport_play')});// transport play
				}
			);
		};
		// Init SourceIn
		switchSourceIn = PopUpMenu(windowEar,Rect(0, 0, 75, 20));
		switchSourceIn.items_(["Audio", "File"]);
		switchSourceIn.action = {|source|
			switch (source.value,
				0, {synthAudioIn.run(true); synthFileIn.run(false); synthAnalyzeAudioIn.run(true); synthRecAudioIn.run(true);
					lastTime = []; (numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});
					// Setup GUI Value
					windowEar.view.children.at(7).enabled_(false);
					windowEar.view.children.at(8).enabled_(false);
					//synthKeyboard.run(false); synthMIDI.run(false);
				},
				1, {synthAudioIn.run(false); synthFileIn.run(true); synthAnalyzeAudioIn.run(false); synthRecAudioIn.run(false);
					lastTime = []; (numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});
					// Setup GUI Value
					windowEar.view.children.at(7).enabled_(true);
					windowEar.view.children.at(8).enabled_(true);
					//synthKeyboard.run(false); synthMIDI.run(false);
				}
			);
		};
		// Setup Bus synthAnalyzeAudioIn
		PopUpMenu(windowEar, Rect(0, 0, 150, 20)).items_(['Analyze Bus 1', 'Analyze Bus 2', 'Analyze Bus 3', 'Analyze Bus 4', 'Analyze Bus 5', 'Analyze Bus 6', 'Analyze Bus 7', 'Analyze Bus 8', 'Analyze Bus 9', 'Analyze Bus 10', 'Analyze Bus 11', 'Analyze Bus 12', 'Analyze Bus 13', 'Analyze Bus 14', 'Analyze Bus 15', 'Analyze Bus 16', 'Analyze Bus 17', 'Analyze Bus 18', 'Analyze Bus 19', 'Analyze Bus 20', 'Analyze Bus 21', 'Analyze Bus 22', 'Analyze Bus 23', 'Analyze Bus 24', 'Analyze Bus 25', 'Analyze Bus 26', 'Analyze Bus 27', 'Analyze Bus 28', 'Analyze Bus 29', 'Analyze Bus 30', 'Analyze Bus 31', 'Analyze Bus 32']).action = {arg item;
			synthAnalyzeAudioIn.set(\in, item.value, \busIn, busAnalyzeIn.index);
			synthAudioIn.set(\in, item.value, \busIn, busAnalyzeIn.index);
			synthAnalyseFFT.set(\busIn, busAnalyzeIn.index);
		};
		// Setup Bus synthRecAudioIn
		PopUpMenu(windowEar, Rect(0, 0, 150, 20)).items_((['Recording Bus 1', 'Recording Bus 2', 'Recording Bus 3', 'Recording Bus 4', 'Recording Bus 5', 'Recording Bus 6', 'Recording Bus 7', 'Recording Bus 8', 'Recording Bus 9', 'Recording Bus 10', 'Recording Bus 11', 'Recording Bus 12', 'Recording Bus 13', 'Recording Bus 14', 'Recording Bus 15', 'Recording Bus 16', 'Recording Bus 17', 'Recording Bus 18', 'Recording Bus 19', 'Recording Bus 20', 'Recording Bus 21', 'Recording Bus 22', 'Recording Bus 23', 'Recording Bus 24', 'Recording Bus 25', 'Recording Bus 26', 'Recording Bus 27', 'Recording Bus 28', 'Recording Bus 29', 'Recording Bus 30', 'Recording Bus 31', 'Recording Bus 32'])).action = {arg item;
			synthRecAudioIn.set(\in, item.value, \busIn, busRecAudioIn.index);
		};
		// Limiter
		EZKnob(windowEar, 150 @ 20, "Limiter",\db,
			{|ez| groupeLimiter.set(\limit, ez.value.dbamp)}, -3, layout: \horz);
		windowEar.view.decorator.nextLine;

		// Text File In
		StaticText(windowEar, Rect(0, 0, 250, 20)).string_("File In : a11wlk01-44_1.aiff").stringColor_( Color.new255(154, 205, 50));
		// Offset FileIn
		EZKnob(windowEar, 150 @ 20, "Offset", \unipolar,
			{|ez|
				s.bind{
					synthFileIn.set(\trigger, -1);
					s.sync;
					synthFileIn.set(\offset, ez.value);
					s.sync;
					synthFileIn.set(\trigger, 1);
					s.sync};
		}, 0, layout: \horz);
		// File DB
		EZKnob(windowEar, 150 @ 20, "db", \db,
			{|ez| synthFileIn.set(\volume, ez.value.dbamp)}, -inf, layout: \horz);
		windowEar.view.decorator.nextLine;

		// MIDI
		StaticText(windowEar, Rect(0, 0, 500, 15)).string_("MIDI").stringColor_(Color.yellow);
		windowEar.view.decorator.nextLine;
		// Setup Canal Midi
		PopUpMenu(windowEar, Rect(0, 0, 150, 20)).items_(['MIDI IN 1', 'MIDI IN 2', 'MIDI IN 3', 'MIDI IN 4', 'MIDI IN 5', 'MIDI IN 6', 'MIDI IN 7', 'MIDI IN 8', 'MIDI IN 9', 'MIDI IN 10', 'MIDI IN 11', 'MIDI IN 12', 'MIDI IN 13', 'MIDI IN 14', 'MIDI IN 15', 'MIDI IN 16']).action = {arg item;
			canalMIDI = item.value;
		};
		// Midi Out
		Button(windowEar,Rect(0, 0, 100, 20)).
		states_([["Chanels Midi Out On", Color.green], ["Chanels Midi Out Off", Color.red]]).
		action_({arg midi;
			if(midi.value == 0, {flagMidiOut = 'off'; windowEar.view.children.at(12).enabled_(false)},
				{flagMidiOut = 'on'; windowEar.view.children.at(12).enabled_(true)}
		)};
		);
		// Chanels MIDI OUT
		EZText(windowEar, Rect(0, 0, 300, 20), "Chanels",
			{arg string; chanelsMidi = string.value},
			chanelsMidi =  [1,1,2,3,4,5,6,7,8,9,10,11,12], true);//13 value 12 band et au debut pour all chanels
		windowEar.view.decorator.nextLine;

		// Analyze
		StaticText(windowEar, Rect(0, 0, 500, 15)).string_("ALGORITHM ANALYZE AUDIO").stringColor_(Color.yellow);
		windowEar.view.decorator.nextLine;
		switchAnalyze = PopUpMenu(windowEar,Rect(0, 0, 90, 20));
		switchAnalyze.items_(["Onsets", "Pitch", "Pitch2", "KeyTrack", "Keyboard", "MIDI"]);
		switchAnalyze.action = {|analyze|
			typeAlgoAnalyze = analyze.value;
			switch (analyze.value,
				0, {synthAnalyseOnsets.run(true); synthAnalysePitch.run(false); synthAnalysePitch2.run(false); synthAnalyseKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false); flagAlgoAnalyze = 'on';
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(true);
					windowEar.view.children.at(21).enabled_(true);
					windowEar.view.children.at(22).enabled_(true);
					windowEar.view.children.at(23).enabled_(true);
					windowEar.view.children.at(24).enabled_(true);
				},
				1, {synthAnalyseOnsets.run(false); synthAnalysePitch.run(true); synthAnalysePitch2.run(false); synthAnalyseKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false); flagAlgoAnalyze = 'on';
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(true);
					windowEar.view.children.at(21).enabled_(true);
					windowEar.view.children.at(22).enabled_(true);
					windowEar.view.children.at(23).enabled_(true);
					windowEar.view.children.at(24).enabled_(true);
				},
				2, {synthAnalyseOnsets.run(false); synthAnalysePitch.run(false); synthAnalysePitch2.run(true); synthAnalyseKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false); flagAlgoAnalyze = 'on';
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(true);
					windowEar.view.children.at(21).enabled_(true);
					windowEar.view.children.at(22).enabled_(true);
					windowEar.view.children.at(23).enabled_(true);
					windowEar.view.children.at(24).enabled_(true);
				},
				3, {synthAnalyseOnsets.run(false); synthAnalysePitch.run(false); synthAnalysePitch2.run(false); synthAnalyseKeyTrack.run(true); synthKeyboard.run(false); synthMIDI.run(false); flagAlgoAnalyze = 'on';
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(true);
					windowEar.view.children.at(21).enabled_(true);
					windowEar.view.children.at(22).enabled_(true);
					windowEar.view.children.at(23).enabled_(true);
					windowEar.view.children.at(24).enabled_(true);
				},
				4, {synthAnalyseOnsets.run(false); synthAnalysePitch.run(false); synthAnalysePitch2.run(false); synthAnalyseKeyTrack.run(false); synthKeyboard.run(true); synthMIDI.run(false); flagAlgoAnalyze = 'off';
					freqFiltreGUI.valueAction_(0);
					ampFiltreGUI.valueAction_(0);
					durFiltreGUI.valueAction_(0.01);
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(false);
					windowEar.view.children.at(21).enabled_(false);
					windowEar.view.children.at(22).enabled_(false);
					windowEar.view.children.at(23).enabled_(false);
					windowEar.view.children.at(24).enabled_(false);
				},
				5, {synthAnalyseOnsets.run(false); synthAnalysePitch.run(false); synthAnalysePitch2.run(false); synthAnalyseKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(true); flagAlgoAnalyze = 'off';
					freqFiltreGUI.valueAction_(0);
					ampFiltreGUI.valueAction_(0);
					durFiltreGUI.valueAction_(0.01);
					// Setup GUI Value
					windowEar.view.children.at(20).enabled_(false);
					windowEar.view.children.at(21).enabled_(false);
					windowEar.view.children.at(22).enabled_(false);
					windowEar.view.children.at(23).enabled_(false);
					windowEar.view.children.at(24).enabled_(false);
				}
			);
		};
		// Algo parametres
		PopUpMenu(windowEar,Rect(0, 0, 75, 20)).
		items_(["Off", "LoPass", "HiPass"]).
		action = {|filter|
			if(filter.value == 0, {
				groupeAnalyse.setn(\ampInput, 1, \ampLoPass, 0, \ampHiPass, 0);
				// Setup GUI Value
				windowEar.view.children.at(16).enabled_(false);
			});
			if(filter.value == 1, {
				groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 1, \ampHiPass, 0);
				// Setup GUI Value
				windowEar.view.children.at(16).enabled_(true);
			});
			if(filter.value == 2, {
				groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 0, \ampHiPass, 1);
				// Setup GUI Value
				windowEar.view.children.at(16).enabled_(true);
			});
		};
		EZKnob(windowEar, 150 @ 20, "FhzPass", \freq,
			{|ez| groupeAnalyse.set(\hzPass, ez.value)}, 440, layout: \horz);

		// Number FhzBand
		EZKnob(windowEar, 150 @ 20, "FhzBand", ControlSpec(1, 12, \lin, 1),
			{|ez| numFhzBand = ez.value;
				listeDataInstruments.do({arg data, index;
					data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
				});
				groupeRecBuffer.freeAll;
				groupeSynth.freeAll;
				groupeFX.freeAll;
				groupeMasterOut.freeAll;
				//listeDataInstruments = []; listeBusOff = [];
				//maximumInstruments.do({arg i; listeBusOff = listeBusOff.add(i)});
				dataFlux = [];
				dataFlatness = [];
				dataCentroid = [];
				dataEnergy = [];
				dataBPM = [];
				indexDataFlux = [];
				indexDataFlatness = [];
				indexDataCentroid = [];
				indexDataEnergy = [];
				indexDataBPM = [];
				memoryDataFlux = [];
				memoryDataFlatness = [];
				memoryDataCentroid = [];
				memoryDataEnergy = [];
				memoryDataBPM = [];
				dataFreq = [];
				dataAmp = [];
				dataDuree = [];
				indexDataFreq = [];
				indexDataAmp = [];
				indexDataDuree = [];
				memoryDataFreq = [];
				memoryDataAmp = [];
				memoryDataDuree = [];
				lastTime = [];
				bandFHZ = Array.fill(numFhzBand, {arg i; [127 / numFhzBand * i, 127 / numFhzBand * i + (127 / numFhzBand )]}).midicps;
				bandFHZ = bandFHZ.reverse;
				bandFHZ = bandFHZ.add([0, 127].midicps);
				bandFHZ = bandFHZ.reverse;
				// Init Array
				(numFhzBand + 1).do({arg i;
					dataFlux = dataFlux.add([]);
					dataFlatness = dataFlatness.add([]);
					dataCentroid = dataCentroid.add([]);
					dataEnergy = dataEnergy.add([]);
					dataBPM = dataBPM.add([]);
					indexDataFlux = indexDataFlux.add(0);
					indexDataFlatness = indexDataFlatness.add(0);
					indexDataCentroid = indexDataCentroid.add(0);
					indexDataEnergy = indexDataEnergy.add(0);
					indexDataBPM = indexDataBPM.add(0);
					memoryDataFlux = memoryDataFlux.add([]);
					memoryDataFlatness = memoryDataFlatness.add([]);
					memoryDataCentroid = memoryDataCentroid.add([]);
					memoryDataEnergy = memoryDataEnergy.add([]);
					memoryDataBPM = memoryDataBPM.add([]);
					dataFreq = dataFreq.add([]);
					dataAmp = dataAmp.add([]);
					dataDuree = dataDuree.add([]);
					indexDataFreq = indexDataFreq.add(0);
					indexDataAmp = indexDataAmp.add(0);
					indexDataDuree = indexDataDuree.add(0);
					memoryDataFreq = memoryDataFreq.add([]);
					memoryDataAmp = memoryDataAmp.add([]);
					memoryDataDuree = memoryDataDuree.add([]);
					lastTime = lastTime.add(Main.elapsedTime);
				});
				for(0, numFhzBand,
					{arg index;
						windowEar.view.children.at(70 + index).enabled_(true);
				});
				if(numFhzBand < 12, {
					for(numFhzBand + 1, 12,
						{arg index;
							windowEar.view.children.at(70 + index).enabled_(false);
							windowEar.view.children.at(70 + index).valueAction_(0);
					});
				});
				freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
				numIndexSynthBand = 0;
				rangeBand.value = bandFHZ.cpsmidi.round(2);
		}, 3, layout: \horz);
		// flagFhzBand
		Button(windowEar,Rect(0, 0, 90, 20))
		.states_([["FhzBand On", Color.green], ["FhzBand Off", Color.red]])
		.action = {arg flag;
			if(flag.value == 0,
				{flagFhzBand = 'off';
					windowEar.view.children.at(17).enabled_(false);
					windowEar.view.children.at(70).enabled_(false);
					windowEar.view.children.at(71).enabled_(false);
					windowEar.view.children.at(72).enabled_(false);
					windowEar.view.children.at(73).enabled_(false);
					windowEar.view.children.at(74).enabled_(false);
					windowEar.view.children.at(75).enabled_(false);
					windowEar.view.children.at(76).enabled_(false);
					windowEar.view.children.at(77).enabled_(false);
					windowEar.view.children.at(78).enabled_(false);
					windowEar.view.children.at(79).enabled_(false);
					windowEar.view.children.at(80).enabled_(false);
					windowEar.view.children.at(81).enabled_(false);
					windowEar.view.children.at(82).enabled_(false);
					numIndexSynthBand = 0;
				},
				{flagFhzBand = 'on';
					windowEar.view.children.at(17).enabled_(true);
					for(0, numFhzBand,
						{arg index;
							windowEar.view.children.at(70 + index).enabled_(true);
					});
					if(numFhzBand < 12, {
						for(numFhzBand + 1, 12,
							{arg index;
								windowEar.view.children.at(70 + index).enabled_(false);
						});
					});
					numIndexSynthBand = 0;
				}
		)};
		// FlagMemory on off
		Button(windowEar,Rect(0, 0, 105, 20))
		.states_([["MemoryData On", Color.green], ["MemoryData Off", Color.red]])
		.action = {arg flag;
			memoryMusic.stop;
			if(flag.value == 0,
				{
					flagMemory = 'off';
					memoryDataFlux = [];
					memoryDataFlatness = [];
					memoryDataCentroid = [];
					memoryDataEnergy = [];
					memoryDataBPM = [];
					memoryDataFreq = [];
					memoryDataAmp = [];
					memoryDataDuree = [];
					memoryDataFlux = [];
					// Init Array
					(numFhzBand + 1).do({arg i;
						memoryDataFlux = memoryDataFlux.add([]);
						memoryDataFlatness = memoryDataFlatness.add([]);
						memoryDataCentroid = memoryDataCentroid.add([]);
						memoryDataEnergy = memoryDataEnergy.add([]);
						memoryDataBPM = memoryDataBPM.add([]);
						memoryDataFreq = memoryDataFreq.add([]);
						memoryDataAmp = memoryDataAmp.add([]);
						memoryDataDuree = memoryDataDuree.add([]);
					});
				},
				{flagMemory = 'on'});
			memoryMusic.play;
		};
		windowEar.view.decorator.nextLine;

		// Threshold
		EZKnob(windowEar, 80 @ 80, "Threshold", \unipolar,
			{|ez| groupeAnalyse.set(\seuil, ez.value)}, 0.5, layout: \vert2);
		// Filter
		EZKnob(windowEar, 80 @ 80, "Filter", \unipolar,
			{|ez| groupeAnalyse.set(\filtre, ez.value)}, 0.5, layout: \vert2);
		// Filter FHZ
		freqFiltreGUI=EZKnob(windowEar, 80 @ 80, "Fhz", ControlSpec(0, 12, \lin, 0.01),
			{|ez| fhzFilter = ez.value}, fhzFilter, layout: \vert2);
		// Filter AMP
		ampFiltreGUI=EZKnob(windowEar, 80 @ 80, "Amp", ControlSpec(0, 12, \lin, 0.01),
			{|ez| ampFilter = ez.value}, ampFilter, layout: \vert2);
		// Filter DUR
		durFiltreGUI=EZKnob(windowEar, 80 @ 80, "Duree", ControlSpec(0.01, 60, \exp, 0),
			{|ez| dureeFilter = ez.value}, dureeFilter, layout: \vert2);
		// Time Maximum Music
		EZKnob(windowEar, 80 @ 80, "Max Duree", ControlSpec(0.01666, 3600, \exp, 0),
			{|ez| dureeMaximumAnalyze = ez.value}, dureeMaximumAnalyze,layout: \vert2);
		// Memory Time
		EZKnob(windowEar, 80 @ 80, "MemoryTime", ControlSpec(0.01666, 3600, \exp, 0),
			{|ez| memoryTime = ez.value;
				memoryMusic.stop;
				memoryMusic.play;
		}, 6, layout: \vert2);
		// Data Maximum
		EZKnob(windowEar, 80 @ 80, "Max Data", ControlSpec(1, 255, \lin, 1),
			{|ez|
				maximumData = ez.value;
				dataFlux = [];
				dataFlatness = [];
				dataCentroid = [];
				dataEnergy = [];
				dataBPM = [];
				indexDataFlux = [];
				indexDataFlatness = [];
				indexDataCentroid = [];
				indexDataEnergy = [];
				indexDataBPM = [];
				memoryDataFlux = [];
				memoryDataFlatness = [];
				memoryDataCentroid = [];
				memoryDataEnergy = [];
				memoryDataBPM = [];
				dataFreq = [];
				dataAmp = [];
				dataDuree = [];
				indexDataFreq = [];
				indexDataAmp = [];
				indexDataDuree = [];
				memoryDataFreq = [];
				memoryDataAmp = [];
				memoryDataDuree = [];
				lastTime = [];
				// Init Array
				(numFhzBand + 1).do({arg i;
					dataFlux = dataFlux.add([]);
					dataFlatness = dataFlatness.add([]);
					dataCentroid = dataCentroid.add([]);
					dataEnergy = dataEnergy.add([]);
					dataBPM = dataBPM.add([]);
					indexDataFlux = indexDataFlux.add(0);
					indexDataFlatness = indexDataFlatness.add(0);
					indexDataCentroid = indexDataCentroid.add(0);
					indexDataEnergy = indexDataEnergy.add(0);
					indexDataBPM = indexDataBPM.add(0);
					memoryDataFlux = memoryDataFlux.add([]);
					memoryDataFlatness = memoryDataFlatness.add([]);
					memoryDataCentroid = memoryDataCentroid.add([]);
					memoryDataEnergy = memoryDataEnergy.add([]);
					memoryDataBPM = memoryDataBPM.add([]);
					dataFreq = dataFreq.add([]);
					dataAmp = dataAmp.add([]);
					dataDuree = dataDuree.add([]);
					indexDataFreq = indexDataFreq.add(0);
					indexDataAmp = indexDataAmp.add(0);
					indexDataDuree = indexDataDuree.add(0);
					memoryDataFreq = memoryDataFreq.add([]);
					memoryDataAmp = memoryDataAmp.add([]);
					memoryDataDuree = memoryDataDuree.add([]);
					lastTime = lastTime.add(Main.elapsedTime);
				});
				freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
				numIndexSynthBand = 0
		}, 24,layout: \vert2);
		windowEar.view.decorator.nextLine;
		// Synth
		StaticText(windowEar, Rect(0, 0, 500, 15)).string_("SYNTHESIZER").stringColor_(Color.yellow);
		windowEar.view.decorator.nextLine;
		// Range Freq
		EZRanger(windowEar , 550 @ 20, "Range FHZ", ControlSpec(0, 127, \lin, 0.1),
			{|ez| rangeFreqintruments = ez.value}, [0, 127], labelWidth: 100, numberWidth: 50);
		windowEar.view.decorator.nextLine;
		// Range Amplitude
		EZRanger(windowEar , 550 @ 20, "Range Amp", \db,
			{|ez| rangeDBintruments = ez.value.dbamp}, [-12, -3], labelWidth: 100,numberWidth: 50);
		windowEar.view.decorator.nextLine;
		// Range Duree
		EZRanger(windowEar , 550 @ 20, "Range Duree", ControlSpec(0, 60, \lin, 0),
			{|ez| rangeDureeintruments = ez.value}, [0, dureeMaximumAnalyze], labelWidth: 100,numberWidth: 50);
		windowEar.view.decorator.nextLine;
		// Stretch Duree
		EZKnob(windowEar, 80 @ 80, "Stretch", ControlSpec(0.0167, 60, \exp, 0),
			{|ez| stretchDuree = ez.value}, 1, layout: \vert2);
		// Quantization
		EZKnob(windowEar, 80 @ 80, "Quantization", ControlSpec(1, 100, \lin, 1),
			{|ez| quantizationDuree = ez.value}, 100, layout: \vert2);
		// BPM
		EZKnob(windowEar, 80 @ 80, "BPM System", ControlSpec(7.5, 480, \exp, 1),
			{|ez| userBPM = ez.value / 60; TempoClock.default.tempo = userBPM; userBPM = userBPM.reciprocal}, 60, layout: \vert2);
		// FX
		EZKnob(windowEar, 80 @ 80, "FX",\unipolar,
			{|ez| fadeFX = ez.value; groupeFX.set(\xFade, ez.value)}, 0.5, layout: \vert2);
		// Maximum Instrument
		EZKnob(windowEar, 80 @ 80, "Max Synth", ControlSpec(0, 32, \lin, 1),
			{|ez| maximumInstruments = ez.value;
				listeDataInstruments.do({arg data, index;
					data = data.put(11, 0); data = data.put(2, 0); listeDataInstruments.put(index, data);
				});
		}, maximumInstruments, layout: \vert2);
		// Global Density
		EZKnob(windowEar, 80 @ 80, "Density", ControlSpec(0, 100, \lin, 0.01),
			{|ez| globalDensity = ez.value / 100;
				dataFlux = [];
				dataFlatness = [];
				dataCentroid = [];
				dataEnergy = [];
				dataBPM = [];
				indexDataFlux = [];
				indexDataFlatness = [];
				indexDataCentroid = [];
				indexDataEnergy = [];
				indexDataBPM = [];
				memoryDataFlux = [];
				memoryDataFlatness = [];
				memoryDataCentroid = [];
				memoryDataEnergy = [];
				memoryDataBPM = [];
				dataFreq = [];
				dataAmp = [];
				dataDuree = [];
				indexDataFreq = [];
				indexDataAmp = [];
				indexDataDuree = [];
				memoryDataFreq = [];
				memoryDataAmp = [];
				memoryDataDuree = [];
				lastTime = [];
				// Init Array
				(numFhzBand + 1).do({arg i;
					dataFlux = dataFlux.add([]);
					dataFlatness = dataFlatness.add([]);
					dataCentroid = dataCentroid.add([]);
					dataEnergy = dataEnergy.add([]);
					dataBPM = dataBPM.add([]);
					indexDataFlux = indexDataFlux.add(0);
					indexDataFlatness = indexDataFlatness.add(0);
					indexDataCentroid = indexDataCentroid.add(0);
					indexDataEnergy = indexDataEnergy.add(0);
					indexDataBPM = indexDataBPM.add(0);
					memoryDataFlux = memoryDataFlux.add([]);
					memoryDataFlatness = memoryDataFlatness.add([]);
					memoryDataCentroid = memoryDataCentroid.add([]);
					memoryDataEnergy = memoryDataEnergy.add([]);
					memoryDataBPM = memoryDataBPM.add([]);
					dataFreq = dataFreq.add([]);
					dataAmp = dataAmp.add([]);
					dataDuree = dataDuree.add([]);
					indexDataFreq = indexDataFreq.add(0);
					indexDataAmp = indexDataAmp.add(0);
					indexDataDuree = indexDataDuree.add(0);
					memoryDataFreq = memoryDataFreq.add([]);
					memoryDataAmp = memoryDataAmp.add([]);
					memoryDataDuree = memoryDataDuree.add([]);
					lastTime = lastTime.add(Main.elapsedTime);
				});
				freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
				numIndexSynthBand = 0
		}, 50, layout: \vert2);
		windowEar.view.decorator.nextLine;

		// Envelope
		EnvelopeView(windowEar, 250 @ 70).background_(Color.grey).
		drawLines_(true).
		selectionColor_(Color.red).fillColor_(Color(0, 0.25, 0.5)).
		strokeColor_(Color.cyan).
		drawRects_(true).
		step_(0.01).
		gridOn_(true).
		resize_(8).
		thumbSize_(16).
		value_([[0.0, 0.015625, 0.125, 0.375, 0.625, 0.75, 0.875, 1.0], [0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0]]).
		gridColor_(Color.grey).
		action={arg env;
			var time, envDuree=[0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125], arrayEnv;
			time = env.value.at(0);
			envDuree.size.do({arg i; envDuree.put(i, abs(time.wrapAt(i+1) - time.wrapAt(i)))});
			arrayEnv = [env.value.at(1), envDuree];
			envelopeSynth = arrayEnv;
		};
		// HP Threshold
		EZKnob(windowEar, 80 @ 80, "Threshold", ControlSpec(0, 1, \lin, 0.0001),
			{|ez| ctrlHP1 = ez.value; groupeSynth.set(\ctrlHP1, ctrlHP1)}, 0.5, layout: \vert2).view.children.at(2).decimals = 4;
		// HP Sensitivity
		EZKnob(windowEar, 80 @ 80, "Sensitivity", ControlSpec(0.01, 1, \lin, 0.0001),
			{|ez| ctrlHP2 = ez.value; groupeSynth.set(\ctrlHP2, ctrlHP2)}, 0.5, layout: \vert2).view.children.at(2).decimals = 4;
		// Offset Sound
		EZKnob(windowEar, 80 @ 80, "Offset", \unipolar,
			{|ez| offsetSound = ez.value}, 0, layout: \vert2);
		// Duree Sample
		EZKnob(windowEar, 80 @ 80, "DurSample", ControlSpec(0.0001, 8, \exp, 0.0001),
			{|ez| dureeSample = ez.value}, 1, layout: \vert2).view.children.at(2).minDecimals_(5).maxDecimals_(5);
		// Rec/Pre Level Sample
		MultiSliderView(windowEar, Rect(0, 0, 35, 70)).value_([1, 0]).action={arg levels;
			recLevel = levels.value.at(0); preLevel = levels.value.at(1); groupeRecBuffer.set(\level1, recLevel, \level2, preLevel); groupeSynth.set(\level1, recLevel, \level2, preLevel); windowEar.view.children.at(44).value = recLevel; windowEar.view.children.at(45).value = preLevel};
		// Display value recbutton 1
		NumberBox(windowEar, Rect(0, 0, 35, 18)).minDecimals_(4).maxDecimals_(4).action = {arg num;
			recLevel = num.value;
			windowEar.view.children.at(43).valueAction_([recLevel, preLevel]);
		};
		// Display value recbutton 2
		NumberBox(windowEar, Rect(0, 0, 35, 18)).minDecimals_(4).maxDecimals_(4).action = {arg num;
			preLevel = num.value;
			windowEar.view.children.at(43).valueAction_([recLevel, preLevel]);
		};
		windowEar.view.children.at(43).valueAction_([recLevel, preLevel]);// Init
		windowEar.view.decorator.nextLine;
		// Range Pan
		EZRanger(windowEar , 250 @ 20, "Pan", \bipolar,
			{|ez| panSynthLo = ez.value.at(0); panSynthHi = ez.value.at(1)}, [0, 0], labelWidth: 25,numberWidth: 40);
		// Loop PlaySound
		Button(windowEar,Rect(0, 0, 70, 20)).
		states_([["Ploop On", Color.green], ["Ploop Off", Color.red]]).
		action_({arg loop; if(loop.value == 0, {loopSound = 0}, {loopSound = 1});
		});
		// Loop RecSound
		Button(windowEar,Rect(0, 0, 70, 20)).
		states_([["Rloop On", Color.green], ["Rloop Off", Color.red], ["Rloop Off", Color.blue]]).
		action_({arg loop;
			if(loop.value == 0, {loopRec = 0; flagRec = 0});
			if(loop.value == 1, {loopRec = 1; flagRec = 0});
			if(loop.value == 2, {loopRec = 0; flagRec = 1});
		});
		// Reverse Sound
		Button(windowEar,Rect(0, 0, 80, 20)).
		states_([["Reverse On", Color.green], ["Reverse Off", Color.red]]).
		action_({arg rev; if (rev.value == 0, {reverseSound = 1},{reverseSound = 1.neg});
		});
		// Set Sampler or Sound
		PopUpMenu(windowEar,Rect(0, 0, 125, 20)).items_(["Sampler+Sound", "Sampler", "Sound"]).
		action = {|setup|
			switch (setup.value,
				0, {flagSampler = "Sampler+Sound"},
				1, {flagSampler = "Sampler"},
				2, {flagSampler = "Sound"}
			);
		};
		// RecSound on off
		Button(windowEar,Rect(0, 0, 75, 20)).
		states_([["RecSnd On", Color.green], ["RecSnd Off", Color.red]]).
		action_({arg loop; if(loop.value == 0, {flagRecSound = 'off'}, {flagRecSound = 'on'})});
		windowEar.view.decorator.nextLine;
		// Display Instrument
		displayInstrument = StaticText(windowEar, Rect(0, 0, 250, 40));
		// Display Sounds
		displaySound = StaticText(windowEar, Rect(0, 0, 240, 40));
		displayFX = StaticText(windowEar, Rect(0, 0, 200, 40));
		windowEar.view.decorator.nextLine;
		// Jitter Y Instrument
		EZSlider(windowEar, Rect(0, 0, 25, 100), "JitY", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexInstrumentY = ez.value / 100}, jitterIndexInstrumentY * 100, false, 40, 35, layout:\vert);
		// Choice Instrument
		Slider2D(windowEar, Rect(0, 0, 200, 100)).
		x_(0.5).
		y_(0.5).
		action_({arg slider, indexX, indexY, instrumentName;
			indexInstrumentX=slider.x;
			indexInstrumentY=slider.y;
			// Display Instrument
			indexX = (indexInstrumentX + rrand(jitterIndexInstrumentX.neg, jitterIndexInstrumentX)).clip(0, 1);
			indexY = (indexInstrumentY + rrand(jitterIndexInstrumentY.neg, jitterIndexInstrumentY)).clip(0, 1);
			instrumentName = synthOrchestra.at((indexX * (synthOrchestra.size - 1) + 0.5).floor);
			instrumentName = instrumentName.at((indexY * (instrumentName.size - 1) + 0.5).floor);
			displayInstrument.string = (indexInstrumentX.asStringPrec(2) + indexInstrumentY.asStringPrec(2) + instrumentName);
		});
		// Jitter Y Sound
		EZSlider(windowEar, Rect(0, 0, 25, 100), "JitY", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexSoundY = ez.value / 100}, jitterIndexSoundY * 100, false, 40, 35, layout:\vert);
		// Choice Sounds
		Slider2D(windowEar, Rect(0, 0, 200, 100)).
		x_(0.5).
		y_(0.5).
		action_({arg slider, indexX, indexY, buffer, soundName;
			indexSoundX=slider.x;
			indexSoundY=slider.y;
			indexX = (indexSoundX + rrand(jitterIndexSoundX.neg, jitterIndexSoundX)).clip(0, 1);
			indexY = (indexSoundY + rrand(jitterIndexSoundY.neg, jitterIndexSoundY)).clip(0, 1);
			buffer = listeBuffer.at((indexX * (listeBuffer.size - 1) + 0.5).floor);
			buffer = buffer.at((indexY * (buffer.size - 1) + 0.5).floor);
			soundName = soundOrchestra.at((indexX * (soundOrchestra.size - 1) + 0.5).floor);
			soundName = soundName.at((indexY * (soundName.size - 1) + 0.5).floor);
			displaySound.string = (indexSoundX.asStringPrec(2) +  indexSoundY.asStringPrec(2) + PathName.new(soundName).fileName);
		});
		// Jitter Y FX
		EZSlider(windowEar, Rect(0, 0, 25, 100), "JitY", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexFXY = ez.value / 100}, jitterIndexFXY * 100, false, 40, 35, layout:\vert);
		// Choice FX
		Slider2D(windowEar, Rect(0, 0, 200, 100)).
		x_(0.5).
		y_(0.5).
		action_({arg slider, indexX, indexY, fxName;
			indexFXX=slider.x;
			indexFXY=slider.y;
			indexX = (indexFXX + rrand(jitterIndexFXX.neg, jitterIndexFXX)).clip(0, 1);
			indexY = (indexFXY + rrand(jitterIndexFXY.neg, jitterIndexFXY)).clip(0, 1);
			fxName = fxOrchestra.at((indexX * (fxOrchestra.size - 1) + 0.5).floor);
			fxName = fxName.at((indexY * (fxName.size - 1) + 0.5).floor);
			displayFX.string = (indexFXX.asStringPrec(2) + indexFXY.asStringPrec(2) + fxName);
		});
		windowEar.view.decorator.nextLine;
		// Jitter X Instrument
		EZSlider(windowEar, Rect(0, 0, 230, 20), "JitX", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexInstrumentX = ez.value / 100}, jitterIndexInstrumentX * 100, false, 35, 35);
		// Jitter X Sound
		EZSlider(windowEar, Rect(0, 0, 230, 20), "JitX", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexSoundX = ez.value / 100}, jitterIndexSoundX * 100, false, 35, 35);
		// Jitter X FX
		EZSlider(windowEar, Rect(0, 0, 230, 20), "JitX", ControlSpec(0, 100, \lin, 0),
			{|ez| jitterIndexFXX = ez.value / 100}, jitterIndexFXX * 100, false, 35, 35);
		windowEar.view.decorator.nextLine;
		// Jitter Controls
		EZKnob(windowEar , 150 @ 20, "JitterCtrl", \unipolar,
			{|ez| jitterControls = ez.value}, 0, layout: \horz);
		// Automation Root
		Button(windowEar, Rect(0, 0, 90, 20)).states_([["AutoRoot On", Color.green], ["AutoRoot Off", Color.red]]).action_({|view|
			if(view.value == 1, {flagRoot = 'on';
				windowEar.view.children.at(85).enabled_(false);
			}, {flagRoot = 'off';
				// Setup GUI Value
				windowEar.view.children.at(85).enabled_(true);
				/*windowEar.view.children.at(85).children.at(1).valueAction_(12);
				windowEar.view.children.at(85).children.at(1).valueAction_(0);*/
			};
			);
		});
		// Automation BPM
		Button(windowEar, Rect(0, 0, 90, 20)).states_([["AutoBPM On", Color.green], ["AutoBPM Off", Color.red]]).action_({|view|
			if(view.value == 1, {
				flagBPM = 'on';
				// Setup GUI Value
				windowEar.view.children.at(34).enabled_(false)
			},
			{flagBPM = 'off';
				// Setup GUI Value
				windowEar.view.children.at(34).enabled_(true);
				windowEar.view.children.at(34).children.at(2).valueAction_(60);
			});
		});
		// Chord On / Off
		Button(windowEar, Rect(0, 0, 80, 20)).states_([["Chord On", Color.green], ["Chord Off", Color.red]]).action_({|view|
			if(view.value == 1, {flagChord = 'on'}, {flagChord = 'off'});
		});
		// Algorithm
		sliderAlgorithm = EZRanger(windowEar , 200 @ 20, "Algorithm", ControlSpec(0, listAlgorithm.size - 1, \lin, 1),
			{|ez| algoLo = ez.value.at(0).asInteger; algoHi = ez.value.at(1).asInteger}, [0, 0], labelWidth: 60,numberWidth: 25);
		windowEar.view.decorator.nextLine;

		// SynthBand
		StaticText(windowEar, 60 @ 20).string = "FhzBand"; // 67
		// Band 0 to 12
		Button.new(windowEar, 40 @ 20).
		states_([["0", Color.green], ["0", Color.red]]).
		action_({arg band; flagBand.put(0, band.value); fonctionBand.value(0)}); // 68 all data
		Button.new(windowEar, 40 @ 20).
		states_([["1", Color.green], ["1", Color.red]]).
		action_({arg band; flagBand.put(1, band.value); fonctionBand.value(1)});
		Button.new(windowEar, 40 @ 20).
		states_([["2", Color.green], ["2", Color.red]]).
		action_({arg band; flagBand.put(2, band.value); fonctionBand.value(2)});
		Button.new(windowEar, 40 @ 20).
		states_([["3", Color.green], ["3", Color.red]]).
		action_({arg band; flagBand.put(3, band.value); fonctionBand.value(3)});
		Button.new(windowEar, 40 @ 20).
		states_([["4", Color.green], ["4", Color.red]]).
		action_({arg band; flagBand.put(4, band.value); fonctionBand.value(4)});
		Button.new(windowEar, 40 @ 20).
		states_([["5", Color.green], ["5", Color.red]]).
		action_({arg band; flagBand.put(5, band.value); fonctionBand.value(5)});
		Button.new(windowEar, 40 @ 20).
		states_([["6", Color.green], ["6", Color.red]]).
		action_({arg band; flagBand.put(6, band.value); fonctionBand.value(6)});
		Button.new(windowEar, 40 @ 20).
		states_([["7", Color.green], ["7", Color.red]]).
		action_({arg band; flagBand.put(7, band.value); fonctionBand.value(7)});
		Button.new(windowEar, 40 @ 20).
		states_([["8", Color.green], ["8", Color.red]]).
		action_({arg band; flagBand.put(8, band.value); fonctionBand.value(8)});
		Button.new(windowEar, 40 @ 20).
		states_([["9", Color.green], ["9", Color.red]]).
		action_({arg band; flagBand.put(9, band.value); fonctionBand.value(9)});
		Button.new(windowEar, 40 @ 20).
		states_([["10", Color.green], ["10", Color.red]]).
		action_({arg band; flagBand.put(10, band.value); fonctionBand.value(10)});
		Button.new(windowEar, 40 @ 20).
		states_([["11", Color.green], ["11", Color.red]]).
		action_({arg band; flagBand.put(11, band.value); fonctionBand.value(11)});
		Button.new(windowEar, 40 @ 20).
		states_([["12", Color.green], ["12", Color.red]]).
		action_({arg band; flagBand.put(12, band.value); fonctionBand.value(12)});// 79
		windowEar.view.decorator.nextLine;

		// Tuning Analyze
		StaticText(windowEar, Rect(0, 0, 50, 15)).string_("TUNING").stringColor_(Color.yellow);
		windowEar.view.decorator.nextLine;
		PopUpMenu(windowEar, Rect(0, 0, 130, 20)).
		items_(["No Scale", "- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"]).
		action = {arg item;
			// Setup GUI Value
			//windowEar.view.children.at(64).valueAction_(0);
			windowEar.view.children.at(85).children.at(1).valueAction_(12);
			windowEar.view.children.at(85).children.at(1).valueAction_(0);
			windowEar.view.children.at(65).enabled_(true);
			windowEar.view.children.at(85).enabled_(true);
			windowEar.view.children.at(86).enabled_(true);
			switch(item.value,
				// No Scale
				0, {flagScaling = 'off';
					// Setup GUI Value
					windowEar.view.children.at(65).valueAction_(0);
					windowEar.view.children.at(65).enabled_(false);
					windowEar.view.children.at(85).children.at(1).valueAction_(12);
					windowEar.view.children.at(85).children.at(1).valueAction_(0);
					windowEar.view.children.at(85).enabled_(false);
					windowEar.view.children.at(86).enabled_(false);
				},
				// Tempered
				1, {nil},
				// Chromatic
				2, {degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]},
				// Whole Tone 1
				3, {degrees =  [0, 2, 4, 6, 8, 10]},
				// Major
				4, {degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Minor
				5, {degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Diminued
				6, {degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Octatonic 1
				7, {degrees =  [0, 1, 3, 4, 6, 7, 9, 10]},
				// Octatonic 2
				8, {degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Nonatonique
				9, {degrees =  [0, 2, 3, 4, 6, 7, 8, 10, 11]},
				// Messian 4
				10, {degrees =  [0, 1, 2, 5, 6, 7, 8, 11]},
				// Messian 5
				11, {degrees =  [0, 1, 5, 6, 7, 11]},
				// Messian 6
				12, {degrees =  [0, 2, 4, 5, 6, 8, 10, 11]},
				// Messian 7
				13, {degrees =  [0, 1, 2, 3, 5, 6, 7, 8, 9, 11]},
				// Bi-Pentaphonic
				14, {degrees =  [0, 1, 2, 4, 5, 6, 7, 9, 10, 11]},
				// Major Pentatonic
				15, {degrees =  [0, 2, 4, 7, 9]},
				// Minor Pentatonic
				16, {degrees =  [0, 3, 5, 7, 10]},
				// Blues
				17, {degrees =  [0, 3, 5, 6, 7, 10]},
				// Asavari
				18, {degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Bhairava
				19, {degrees =  [0, 1, 4, 5, 7, 8, 11]},
				// Bhairavi
				20, {degrees =  [0, 1, 3, 5, 7, 8, 10]},
				// Bilaval
				21, {degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Kafi
				22, {degrees =  [0, 2, 3, 5, 7, 9, 10]},
				// Kalyan
				23, {degrees =  [0, 2, 4, 6, 7, 9, 11]},
				// Khammaj
				24, {degrees =  [0, 2, 4, 5, 7, 9, 10]},
				// Marava
				25, {degrees =  [0, 1, 4, 6, 7, 9, 11]},
				// Pooravi
				26, {degrees =  [0, 1, 4, 6, 7, 8, 11]},
				// Todi
				27, {degrees =  [0, 1, 3, 6, 7, 8, 11]},
				// Indian Shrutis
				28, {nil},
				// 22tet
				29, {degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]},
				// 12tet
				30, {degrees =  [0, 2, 4, 6, 7, 9, 11, 13, 15, 16, 19, 20]},
				// Asavari
				31, {degrees =  [0, 4, 6, 9, 13, 15, 19]},
				// Bhairava
				32, {degrees =  [0, 2, 7, 9, 13, 15, 20]},
				// Bhairavi
				33, {degrees =  [0, 3, 5, 9, 13, 15, 18]},
				// Bilaval
				34, {degrees =  [0, 4, 7, 9, 13, 16, 20]},
				// Kafi
				35, {degrees =  [0, 4, 6, 9, 13, 16, 19]},
				// Kalyan
				36, {degrees =  [0, 4, 7, 11, 13, 16, 20]},
				// Khammaj
				37, {degrees =  [0, 4, 7, 9, 13, 16, 19]},
				// Marava
				38, {degrees =  [0, 2, 7, 11, 13, 16, 20]},
				// Pooravi
				39, {degrees =  [0, 2, 7, 11, 13, 15, 20]},
				// Todi
				40, {degrees =  [0, 2, 6, 11, 13, 15, 20]}
			);
			if(item.value > 1 and: {item.value < 28}, {tuning = Tuning.et12; scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning); flagScaling = 'on';
				// Setup GUI Value
				windowEar.view.children.at(86).children.at(1).valueAction = degrees.asString;
			});
			if(item.value > 28, {tuning = Tuning.sruti; scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning); flagScaling = 'on';
				// Setup GUI Value
				windowEar.view.children.at(86).children.at(1).valueAction = degrees.asString;
			});
		};
		// Root
		EZKnob(windowEar, 80 @ 20, "Root", ControlSpec(0, 21, \lin, 1),
			{|ez| root = ez.value; scale=Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)}, 0, layout: \horz, labelWidth: 30);
		// Degrees
		EZText(windowEar, Rect(0, 0, 475, 20), "Degrees",
			{arg string; degrees = string.value; scale=Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)},
			degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true);
		// Set Range FHZband
		rangeBand = EZText(windowEar, Rect(0, 0, 705, 20), "Range Band",
			{arg range; bandFHZ = range.value.midicps},
			[[0, 127], [0.0, 42.33], [42.33, 84.66], [84.66, 127.0] ], true, 80);
		// Display Data Analyze
		displayAnalyzeMusic = StaticText(windowEar, Rect(0, 0, 705, 15)).stringColor = Color.new255(127, 255, 212);
		// Display Data Analyze
		displayAnalyzeFFT = StaticText(windowEar, Rect(0, 0, 700, 15)).stringColor = Color.new255(127, 255, 212);
		windowEar.onClose_({
			"No Action".postln;
		});
		startSystem.focus;

		// Init shortCuts
		windowEar.front;
		startSystem.focus;
		listeWindows=listeWindows.add(windowEar);
		listeWindows=listeWindows.add(windowGVerb);
		listeWindows=listeWindows.add(windowPlotterData);
		listeWindows=listeWindows.add(windowPlotterFFT);
		listeWindows=listeWindows.add(windowKeyboard);
		//listeWindows=listeWindows.add(windowVST);
		listeWindows.do({arg window; fonctionShortCut.value(window);
			window.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Helvetica", 10);
				});
			});
		});

		// Init Preset
		//Post << "Init Preset" <<  Char.nl;
		File(pathData++"Init Preset.scd","w").write(fonctionSavePreset.value(listeWindows).asCompileString).close;

	}

	// SynthDef

	initSynthDef {

		// Density Audio Analyze FFT
		SynthDef("OSC Density FFT",
			{arg busIn, speed=24;
				var fft, input, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=60, lock=0;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				SendReply.kr(Impulse.kr(speed), '/Density_FFT_Data', values: [flux.clip(0.0001, 1), flatness.clip(0.0001, 1), centroid.clip(20, 12544), energy.clip(20, 12544), bpm], replyID: [1, 2, 3, 4, 5]);
		}).add;

		// Density Audio Analyze Onsets
		SynthDef("OSC Density Onsets",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0,
				inputFilter;
				input = In.ar(busIn);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);
				# freqIn, hasfreqIn = Tartini.kr(inputFilter);//, filtre, 2048, 1024, 512, 0.5);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Density_Music_Data', values: [freqIn, ampIn, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Density Audio Analyze Pitch
		SynthDef("OSC Density Pitch",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0,
				inputFilter;
				input = In.ar(busIn);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
				# freqIn, hasfreqIn = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Density_Music_Data', values: [freqIn, ampIn, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Pitch v2
		SynthDef("OSC Density Pitch2",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0, inputFilter, fft, harmonic, percussive;
				input = In.ar(busIn);
				ampInput = if(ampLoPass < 1, 1, if(ampHiPass < 0, 1, 0));
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				fft = FFT(LocalBuf(512, 1), inputFilter);
				harmonic = FFT(LocalBuf(512, 1), inputFilter);
				percussive = FFT(LocalBuf(512, 1), inputFilter);
				#harmonic, percussive = MedianSeparation(fft, harmonic, percussive, 512, 5, 1, 2, 1);
				detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
				# freqIn, hasfreqIn = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Density_Music_Data', values: [freqIn, ampIn, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Density Audio Analyze KeyTrack
		SynthDef("OSC Density KeyTrack",
			{arg busIn, seuil=0.5, filtre=0.5;
				var input, detect, freqIn, ampIn, timeIn=0, key;
				input = In.ar(busIn);
				detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
				key = KeyTrack.kr(FFT(LocalBuf(4096, 1), input), (filtre * 4).clip(0, 4));
				if(key < 12, freqIn = (key + 60).midicps, freqIn = (key - 12 + 60).midicps);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Density_Music_Data', values: [freqIn, ampIn, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Density Keyboard
		SynthDef("OSC Density Keyboard",
			{arg busIn, note=60, amp=0.5, trigger=0;
				var input, timeIn=0;
				input = In.ar(busIn);
				timeIn = Timer.kr(trigger);
				SendReply.kr(trigger, '/Density_Music_Data', values: [note, amp, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Density MIDI
		SynthDef("OSC Density MIDI",
			{arg busIn, note=60, amp=0.5, trigger=0;
				var input, timeIn=0;
				input = In.ar(busIn);
				timeIn = Timer.kr(trigger);
				SendReply.kr(trigger, '/Density_Music_Data', values: [note, amp, timeIn], replyID: [1, 2, 3]);
		}).add;

		// Synth pour analyse AudioIn send audio -> busIn
		SynthDef("Density AudioIn",
			{arg in=0, busIn=0;
				var input;
				input=Mix(SoundIn.ar(in));
				Out.ar(busIn, input); // Bus File In
		}).add;

		// Synth lecture file pour analyse AudioIn
		SynthDef("Density FileIn",
			{arg out=0, bufferplay, busIn=0, busRec=0, trigger=0, offset=0, loop=1, volume=0;
				var input;
				input=PlayBuf.ar(2, bufferplay, BufRateScale.kr(bufferplay), trigger, BufFrames.kr(bufferplay) * offset , loop);
				Out.ar(busIn, Mix(input)); // Bus File In
				Out.ar(busRec, Mix(input)); // Bus Rec
				Out.ar(out, input * volume); // Amp File Out
		}).add;

		///////////////// SYNTH AUDIO IN / RECORDS /////////////////////

		// Synth AnalyzeAudioIn
		SynthDef("AnalyzeAudioIn",
			{arg in=0, busIn;
				Out.ar(busIn, Mix(SoundIn.ar(in)));
		}).add;

		// Synth RecAudioIn
		SynthDef("RecAudioIn",
			{arg in=0, busIn;
				Out.ar(busIn, Mix(SoundIn.ar(in)));
		}).add;

		// Buffer Rec for Synth
		SynthDef("RecBufferSynth",
			{arg in=0, buffer, level1=1, level2=0, loopRec=0, trigger=1;
				RecordBuf.ar(In.ar(in), buffer, offset: 0, recLevel: level1, preLevel: level2, run: 1, loop: loopRec, trigger: trigger, doneAction: 0);
		}).add;

		////////////////////////////////////////////////////////////

		// Vibrato
		//SinOsc.kr(flux.log.abs, mul: flatness, add: BufRateScale.kr(buffer) * rate)
		// Envelope
		//envelope = EnvGen.kr(Env.adsr(0.05, 0.3, 0.666, 0.05, 1, -4, 0), gate, amp, 0, dur, 2);

		/////////////////////////// PIANO ////////////////////////

		SynthDef("MdaPiano",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				// Play
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoRF",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, line1, line2;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 8372 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				line1 = if(Rand(0, 1) < 0.5, XLine.ar(energy, freq, dur), XLine.ar(freq, energy, dur));
				line2 = if(Rand(0, 1) < 0.5, XLine.kr(centroid, freq, dur), XLine.kr(freq, centroid, dur));
				// Play
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				chain = if(freq < 64.5.midicps , RLPF.ar(chain, line1, 0.333), RHPF.ar(chain, line2, 0.333));
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoResonz",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, line1, line2;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 8372 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				line1 = if(Rand(0, 1) < 0.5, XLine.ar(energy, freq, dur), XLine.ar(freq, energy, dur));
				line2 = if(Rand(0, 1) < 0.5, XLine.kr(centroid, freq, dur), XLine.kr(freq, centroid, dur));
				// Play
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				chain = if(freq >= 60.midicps , Resonz.ar(chain, line1), Resonz.ar(chain, line2));
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoFreqShift",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				// Play
				chain = FreqShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))) * envelope, flatness * 1024 - 512, flux * 2pi);
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////////// PIANO + PV ////////////////////////

		SynthDef("PianoPV_Add",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, in1, in2, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// MdaPiano
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				// Play Buffer
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoPV_Mul",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, in1, in2, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// MdaPiano
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				// Play Buffer
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoPV_MagDiv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, in1, in2, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// MdaPiano
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				// Play Buffer
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoPV_BinWipe",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, in1, in2, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// MdaPiano
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				// Play Buffer
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PianoPV_RectComb2",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, in1, in2, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// MdaPiano
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
				// Play Buffer
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////////// SYNTH ////////////////////////

		SynthDef("SinOsc",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				chain = SinOsc.ar(freq, 0, 0.5) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Formant",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				chain = Formant.ar(freq, LFNoise0.kr(flux.log.abs) * centroid, LFNoise0.kr(flatness.log2.abs) * energy, 0.5) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("FM",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, mod, car, carPartial = 1, modPartial = 1, index = 3;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186);
				centroid = (centroid / 12544 * 8372).clip(50, 8372);
				flux = flux.clip(0.001, 1.0);
				flatness = flatness.clip(0.001, 1.0);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				modPartial = 1;//4 - flux.log10.abs.ceil;// + flux;
				carPartial = 4 - flatness.log10.abs.ceil.lag(durSynth);// + flatness;
				index = 4 - flux.log10.abs.ceil.lag(durSynth);
				mod =SinOsc.ar(freq * modPartial, 0 , freq * index * LFNoise1.kr((centroid / energy).abs).abs);
				car = SinOsc.ar(freq * carPartial + mod);
				chain = car * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Ring",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, car, mod;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				mod =SinOsc.ar(energy / 10);
				car = SinOsc.ar(freq, 0, mod);
				chain = car * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("AnalogString",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, fc, osc;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				fc = LinExp.kr(LFNoise1.kr(flux), -1, 1, energy, centroid);
				osc = Mix.fill(8, {LFSaw.ar(freq * [Rand(0.99,1.01),Rand(0.99,1.01)], 0, 1) }).distort * 0.5;
				chain= RLPF.ar(osc, fc, flatness) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Gendy",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				chain= Gendy1.ar(Rand(2, 6), 3, flatness, flux, energy, freq, flux, flatness) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Saw",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play
				freq = freq.clip(20,12544);
				chain = Mix(RHPF.ar(Saw.ar(freq, 0.25), energy, flatness) + RLPF.ar(Saw.ar(freq, 0.25), centroid, flatness)) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////////// SYNTH STREAM ////////////////////////

		SynthDef("SinOscStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = SinOsc.ar(freq, 0, 0.5) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("FormantStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, line;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(freq, energy, durSynth), XLine.kr(energy, freq, durSynth));
				// Play
				chain = Formant.ar(freq, line, energy) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("FMStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, mod, car, carPartial = 1, modPartial = 1, index = 3;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				modPartial = 1;//4 - flux.log10.abs.ceil;// + flux;
				carPartial = 4 - flatness.log10.abs.ceil;// + flatness;
				index = 4 - flux.log10.abs.ceil;
				mod =SinOsc.ar(freq * modPartial, 0 , freq * index * LFNoise1.kr((centroid / energy).abs).abs);
				car = SinOsc.ar(freq * carPartial + mod);
				chain = car * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("RingStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, car, mod;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				mod =SinOsc.ar(energy / 10);
				car = SinOsc.ar(freq, 0, mod);
				chain = car * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("SawStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = Saw.ar(freq, 0.25) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("AnalogStringStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, fc, osc;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				fc = LinExp.kr(LFNoise1.kr(flux), -1, 1, energy, centroid);
				osc = Mix.fill(8, {LFSaw.ar(freq * [Rand(0.99,1.01), Rand(0.99,1.01)], 0, 1) }).distort * 0.5;
				chain= RLPF.ar(osc, fc, flatness) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GendyStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain= Gendy1.ar(Rand(2, 6), 3, flatness, flux, freq, centroid) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////////// NEW SYNTH STREAM WITH EnvGen////////////////////////

		SynthDef("SinOscStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = SinOsc.ar(freq, 0, 0.5) * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("FormantStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, line;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(centroid, energy, durSynth), XLine.kr(energy, centroid, durSynth));
				// Play
				chain = Formant.ar(freq, line, 0.5) * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("FMStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, mod, car, carPartial = 1, modPartial = 1, index = 3;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				modPartial = 1;//4 - flux.log10.abs.ceil;// + flux;
				carPartial = 4 - flatness.log10.abs.ceil;// + flatness;
				index = 4 - flux.log10.abs.ceil;
				mod =SinOsc.ar(freq * modPartial, 0 , freq * index * LFNoise1.kr((centroid / energy).abs).abs);
				car = SinOsc.ar(freq * carPartial + mod);
				chain = car * 0.5 * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("RingStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, car, mod;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				mod =SinOsc.ar(energy / 10);
				car = SinOsc.ar(freq, 0, mod);
				chain = car * 0.5 * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("SawStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = Saw.ar(freq, 0.25) * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("AnalogStringStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, fc, osc;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				fc = LinExp.kr(LFNoise1.kr(flux), -1, 1, energy, centroid);
				osc = Mix.fill(8, {LFSaw.ar(freq * [Rand(0.99,1.01), Rand(0.99,1.01)], 0, 1) }).distort * 0.5;
				chain= RLPF.ar(osc, fc, flatness) * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GendyStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain= Gendy1.ar(Rand(2, 6), 3, flatness, flux, freq, centroid) * envelope * gate * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////// SAMPLER PREBUFFER ///////////////////////

		SynthDef("PlayBufPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufRFPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.ar(flatness * centroid, energy * flatness, durSynth), XLine.ar(energy * flatness, centroid * flatness, durSynth));
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = if(freq < 64.5.midicps, RLPF.ar(chain, line, 0.333), RHPF.ar(chain, line, 0.333));
				chain = chain  * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufResonzPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.ar(energy, centroid, durSynth), XLine.ar(centroid, energy, durSynth));
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Resonz.ar(chain, line);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufKlankPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, dur);
				chain = chain  * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufLiquidPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * 4186, flatness, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufElasticPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, dur), XLine.kr(flux, flatness, dur));
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("TGrainsPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, trigger;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Normalize
				flux = flux.clip(0.01, 1).lag;
				flatness = flatness.clip(0.01, 1).lag;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Trigger
				trigger = Dust.kr(flatness * 100);
				offset = offset + flux;
				offset = offset.mod(1);
				// Play Buffer
				chain = Mix(HPtGrains.ar(2, trigger, buffer, BufRateScale.kr(buffer) * rate, BufDur.kr(buffer) * offset, flux.max(0.1), seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1PreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				//chain = Warp1.ar(1, buffer, LFSaw.kr(rate, 1,0.5,0.5), BufRateScale.kr(buffer) * rate, flatness.log10.abs, -1, energy.log2.abs, flux) * envelope;
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness.log10.abs, -1, energy.log2.abs, flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = LeakDC.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2), flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=0, offset=0, reverse=1,
				freq=440, amp=0, dur = 1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2)), flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM PREBUFFER//////////////////////////

		SynthDef("PlayBufStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufKlankStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufLiquidStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufElasticStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("TGrainsStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, trigger;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag;
				flux = flux.clip(0.01, 1.0).lag;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Trigger
				trigger = Dust.kr(flux * 100);
				offset = offset + flatness;
				offset = offset.mod(1);
				// Play Buffer
				chain = Mix(TGrains.ar(2, trigger, buffer, BufRateScale.kr(buffer) * rate, BufDur.kr(buffer) * offset, dur)) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1StreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				//chain = Warp1.ar(1, buffer, LFSaw.kr(rate, 1,0.5,0.5), BufRateScale.kr(buffer) * rate, flatness.log10.abs, -1, energy.log2.abs, flux) * envelope;
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness.log10.abs, -1, energy.log2.abs, flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranularStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, trigger;
				// Normalize
				flatness = flatness.clip(0.01, 1).lag;
				flux = flux.clip(0.01, 1).lag;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				trigger = Dust2.kr(flux.reciprocal);
				chain = BufRd.ar(1, buffer, Phasor.ar(trigger, BufRateScale.kr(buffer) * rate, TRand.kr(0.0, offset, trigger), BufFrames.kr(buffer),  BufFrames.kr(buffer) * TRand.kr(0, offset, trigger)), 1) * envelope * amp;
				// Outl1
				Out.ar(out, chain);
		}).add;

		SynthDef("DjScratchStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, trigger;
				// Normalize
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				trigger = LFNoise0.kr(rate / flatness);
				chain = BufRd.ar(1, buffer, Phasor.ar(trigger, BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer) * TRand.kr(0.0, 1.0, trigger), BufFrames.kr(buffer), BufFrames.kr(buffer) * offset).lag(durSynth), 1) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = LeakDC.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2), flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2)), flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM PREBUFFER WITH EnvGen//////////////////////////

		SynthDef("PlayBufStreamPreBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				/*chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, Trig1.kr(Impulse.kr(durSynth), dur), BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope;*/
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufKlankStreamPreBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufLiquidStreamPreBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PlayBufElasticStreamPreBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1StreamPreBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, ctrlHP1=0.5, ctrlHP2=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, line;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				// Play Buffer
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness.log10.abs, -1, energy.log2.abs, flux);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER POSTBUFFER //////////////////////////

		SynthDef("BufRdPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdRFPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.ar(flatness * centroid, energy * flatness, durSynth), XLine.ar(energy * flatness, centroid * flatness, durSynth));
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = if(freq < 64.5.midicps, RLPF.ar(chain, line, 0.333), RHPF.ar(chain, line, 0.333));
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdResonzPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.ar(energy, centroid, durSynth), XLine.ar(centroid, energy, durSynth));
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Resonz.ar(chain, line);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdKlankPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, dur);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdLiquidPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdElasticPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, dur), XLine.kr(flux, flatness, dur));
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1PostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness, -1, energy.log2.abs, flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranularPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, trigger;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flatness = flatness.clip(0.001, 1).lag;
				// Trigger
				trigger = Dust2.kr(flatness.reciprocal);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(trigger, rate, BufFrames.kr(buffer) * TRand.kr(0, offset, trigger),  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(trigger, rate, recHead * TRand.kr(0, offset, trigger), BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = LeakDC.ar(HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2), flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2)), flux) * envelope;
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM POSTBUFFER//////////////////////////

		SynthDef("BufRdStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdKlankStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdLiquidStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy * flatness, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdElasticStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1StreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness, -1, energy.log2.abs, flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranularStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, trigger;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				trigger = Dust2.kr(flatness.reciprocal);
				// Normalize
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(trigger, rate, BufFrames.kr(buffer) * TRand.kr(-1.0, 1.0, trigger).sign, BufFrames.kr(buffer) * TRand.kr(0.0, offset, trigger), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(trigger, rate, recHead, BufFrames.kr(buffer) * TRand.kr(-1.0, 1.0, trigger).sign, BufFrames.kr(buffer) * TRand.kr(0.0, offset, trigger), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("DjScratchStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, trigger;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset).lag(flatness.log10.abs/3),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset).lag(flatness.log10.abs/3)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = LeakDC.ar(HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2)) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope;
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, HPbufRd.ar(1, buffer, playHead, seuil: ctrlHP1, sensibilite: ctrlHP2)), flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM POSTBUFFER WITH EnvGen//////////////////////////

		SynthDef("BufRdStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdKlankStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdLiquidStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				/// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = Mix(RHPF.ar(chain, formantfreqs * energy * flatness, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("BufRdElasticStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1StreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness, -1, energy.log2.abs, flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranularStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, trigger;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				trigger = Dust2.kr(flatness.reciprocal);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(trigger, rate, BufFrames.kr(buffer) * TRand.kr(-1.0, 1.0, trigger).sign, BufFrames.kr(buffer) * TRand.kr(0.0, offset, trigger), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(trigger, rate, recHead, BufFrames.kr(buffer) * TRand.kr(-1.0, 1.0, trigger).sign, BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("DjScratchStreamPostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, trigger;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset).lag(flatness.log10.abs/3),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset).lag(flatness.log10.abs/3)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				chain = HPbufRd.ar(1, buffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		///////////////////////// PV Prebuf + PostBuf ////////////////////////

		SynthDef("PV_AddPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipePrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2PrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// Stream PV Prebuf + PostBuf ////////////////////

		SynthDef("PV_AddStreamPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulStreamPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivStreamPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipeStreamPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2StreamPrePostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// Stream PV Prebuf + PostBuf WITH EnvGen////////////////////

		SynthDef("PV_AddStreamPrePostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), flatness);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulStreamPrePostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivStreamPrePostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipeStreamPrePostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2StreamPrePostBufEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, postBuffer, inputSig, rate, envelope, recHead=0, playHead=0, in1, in2, fft1, fft2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(Trig1.kr(Impulse.kr(flux * 100), flatness), rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				in2 = HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2);
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		//////////////////////////////////// SPECIAL ////////////////////////////////////////////

		SynthDef("Silent",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				// Play
				chain = SinOsc.ar(freq, 0, 0,5) * envelope * amp;
				//// Out
				//Out.ar(out, chain);
		}).add;

		SynthDef("SilentStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope;
				// Envelope

				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = SinOsc.ar(freq, 0, 0.5) * envelope * amp;
				//// Out
				//Out.ar(out, chain);
		}).add;

		SynthDef("SynthSampler",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envA, envS, envR, recHead=0, playHead=0, postBuffer, inputSig;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envA = EnvGen.kr(Env.perc, gate, 1, 0, dur / 4, 0);
				envS = EnvGen.kr(Env.new([0, 0, 1, 1, 0, 0],[0.05, 0.15, 0.2, 0.4, 0.2], 'sine'), gate, 1, 0, dur, 0);
				envR = EnvGen.kr(Env.new([0, 1, 1, 0],[0.5, 0.25, 0.25], 'sine'), gate, 1, 0, dur, 2);
				// Play
				chain = Mix(RHPF.ar(Saw.ar(freq, 0.25), energy.lag(durSynth), flux.lag(durSynth)) + RLPF.ar(Saw.ar(freq, 0.25), centroid.lag(durSynth), flatness.lag(durSynth))) * envA * amp;
				chain = chain + (HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2) * envS * amp);
				chain = chain + (HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envR* amp);
				// Out
				Out.ar(out, Mix(chain));
		}).add;

		SynthDef("SynthSamplerStream",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, envA, envS, envR, recHead=0, playHead=0, postBuffer, inputSig;
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envA = EnvGen.kr(Env.perc, gate, 1, 0, durSynth / 4, 0);
				envS = EnvGen.kr(Env.new([0, 0, 1, 1, 0, 0],[0.05, 0.15, 0.2, 0.4, 0.2], 'sine'), gate, 1, 0, durSynth, 0);
				envR = EnvGen.kr(Env.new([0, 1, 1, 0],[0.5, 0.25, 0.25], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = Mix(RHPF.ar(Saw.ar(freq, 0.25), energy.lag(durSynth), flux.lag(durSynth)) + RLPF.ar(Saw.ar(freq, 0.25), centroid.lag(durSynth), flatness.lag(durSynth))) * envA * amp;
				chain = chain + (HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envS * amp);
				chain = chain + (HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envR * amp);
				// Out
				Out.ar(out, Mix(chain));
		}).add;

		SynthDef("SynthSamplerStreamEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, envelope, envA, envS, envR, recHead=0, playHead=0, postBuffer, inputSig;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), flatness);
				// Buffer
				postBuffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(gate, rate, BufFrames.kr(postBuffer) * offset,  BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset),
					// rate > 1
					Phasor.ar(gate, rate, recHead, BufFrames.kr(postBuffer), BufFrames.kr(postBuffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, BufFrames.kr(postBuffer));
				);
				// RecBuffer
				BufWr.ar(inputSig, postBuffer, recHead);
				// Envelope
				envA = EnvGen.kr(Env.perc, gate, 1, 0, durSynth / 4, 0);
				envS = EnvGen.kr(Env.new([0, 0, 1, 1, 0, 0],[0.05, 0.15, 0.2, 0.4, 0.2], 'sine'), gate, 1, 0, durSynth, 0);
				envR = EnvGen.kr(Env.new([0, 1, 1, 0],[0.5, 0.25, 0.25], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play
				chain = Mix(RHPF.ar(Saw.ar(freq, 0.25), energy.lag(durSynth), flux.lag(durSynth)) + RLPF.ar(Saw.ar(freq, 0.25), centroid.lag(durSynth), flatness.lag(durSynth))) * envA * amp * Trig1.kr(Impulse.kr(flux * 100), flatness);
				chain = chain + (HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, Trig1.kr(Impulse.kr(flux * 100), flatness), BufFrames.kr(buffer) * offset, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envS * amp);
				chain = chain + (HPbufRd.ar(1, postBuffer, playHead, 1, seuil: ctrlHP1, sensibilite: ctrlHP2) * envR * amp);
				chain = chain;
				// Out
				Out.ar(out, Mix(chain));
		}).add;

		SynthDef("GranulatorPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, playHead, trig, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				trig = Dust.kr(bpm);
				playHead = Phasor.ar(trig, rate * BufRateScale.kr(buffer));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Synth
				//chain = BufRd.ar(1, buffer, playHead, loop: 0) * envelope;
				chain = Warp1.ar(1, buffer, playHead, rate, windowSize: flux, overlaps: 8, windowRandRatio: flatness) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranulatorStreamPreBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, playHead, trig, envelope;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				trig = Dust.kr(bpm);
				playHead = Phasor.ar(trig, rate * BufRateScale.kr(buffer));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), trig, 1, 0, dur, 2);
				// Synth
				//chain = BufRd.ar(1, buffer, playHead, loop: 0) * envelope;
				chain = Warp1.ar(1, buffer, playHead, rate, windowSize: flux, overlaps: 8, windowRandRatio: flatness) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranulatorPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, trig, envelope, inputSig, recHead=0, playHead=0;
				trig = Dust.kr(bpm);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(trig, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(trig, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Synth
				//chain = BufRd.ar(1, buffer, playHead, loop: 0) * envelope;
				chain = Warp1.ar(1, buffer, playHead, rate, windowSize: flux, overlaps: 8, windowRandRatio: flatness) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("GranulatorStreamPostBuf",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, trig, envelope, inputSig, recHead=0, playHead=0;
				trig = Dust.kr(bpm);
				// Buffer
				buffer = LocalBuf(s.sampleRate * durSample, 1).clear;
				inputSig = In.ar(in);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(trig, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(trig, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// RecBuffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), trig, 1, 0, dur, 2);
				// Synth
				//chain = BufRd.ar(1, buffer, playHead, loop: 0) * envelope;
				chain = Warp1.ar(1, buffer, playHead, rate, windowSize: flux, overlaps: 8, windowRandRatio: flatness) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

				/////////////////////// SAMPLER DelayHarmonic //////////////////////////

		SynthDef("DelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("RFDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del, line;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				line = if(Rand(0, 1) < 0.5, XLine.ar(flatness * centroid, energy * flatness, durSynth), XLine.ar(energy * flatness, centroid * flatness, durSynth));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = if(freq < 64.5.midicps, RLPF.ar(chain, line, 0.333), RHPF.ar(chain, line, 0.333));
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("ResonzDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del, line;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				line = if(Rand(0, 1) < 0.5, XLine.ar(energy, centroid, durSynth), XLine.ar(centroid, energy, durSynth));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = Resonz.ar(chain, line);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("KlankDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, dur);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LiquidDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = Mix(RHPF.ar(chain, formantfreqs * energy, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("ElasticDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del, line;
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, dur), XLine.kr(flux, flatness, dur));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("Warp1DelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, maxDel=0.05, phase, envDel, del;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Set play and rec head pour recording
				playHead = if(rate <= 1, Phasor.ar(0, rate, BufFrames.kr(buffer) * offset,  BufFrames.kr(buffer), BufFrames.kr(buffer) * offset),
					// rate > 1
					Phasor.ar(0, rate, recHead, BufFrames.kr(buffer), BufFrames.kr(buffer) * offset)
				);
				recHead = if(rate <= 1, Phasor.ar(0, 1, 0, BufFrames.kr(buffer)),
					// rate > 1
					Phasor.ar(0, 1, 0, playHead);
				);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				chain = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, flatness, -1, energy.log2.abs, flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = LeakDC.ar(chain, flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, maxDel=0.05, phase, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, chain), flux) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM DelayHarmonic//////////////////////////

		SynthDef("StreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope, phase, maxDel=0.05, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("KlankStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, phase, maxDel=0.05, envDel, del;
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LiquidStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, phase, maxDel=0.05, envDel, del;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = Mix(RHPF.ar(chain, formantfreqs * energy * flatness, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("ElasticStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, phase, maxDel=0.05, envDel, del, line;
				// Normalize
				flux = flux.clip(0.001, 1.0).lag(durSynth);
				flatness = flatness.clip(0.001, 1.0).lag(durSynth);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope, phase, maxDel=0.05, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = Median.ar(flatness * 30 + 1, chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LeakDCStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope, phase, maxDel=0.05, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = LeakDC.ar(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("MedianLeakDCStreamDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope, phase, maxDel=0.05, envDel, del;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				// Play Buffer
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, chain), flux) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// SAMPLER STREAM DelayHarmonicFER WITH EnvGen//////////////////////////

		SynthDef("StreamDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, recHead=0, playHead=0, envelope, phase, maxDel=0.05, envDel, del;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("KlankStreamDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, phase, maxDel=0.05, envDel, del;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = DynKlank.ar(`[[Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid),Rand(energy, centroid)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, 1, 0, durSynth);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("LiquidStreamDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, phase, maxDel=0.05, envDel, del;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain = Mix(RHPF.ar(chain, formantfreqs * energy * flatness, formantbandwidths / (formantfreqs * energy)));
				chain = BBandPass.ar(chain, LFNoise1.kr(flux) + 1 * centroid, flatness, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("ElasticStreamDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, inputSig, rate, envelope, recHead=0, playHead=0, line, phase, maxDel=0.05, envDel, del;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				line = if(Rand(0, 1) < 0.5, XLine.kr(flatness, flux, durSynth), XLine.kr(flux, flatness, durSynth));
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				chain =  CombC.ar(chain, 0.1, line, 1);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		///////////////////////// PV Prebuf + DelayHarmonic ////////////////////////

		SynthDef("PV_AddPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipePreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2PreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, amp, 0, dur, 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, loop, seuil: ctrlHP1, sensibilite: ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// Stream PV Prebuf + DelayHarmonic ////////////////////

		SynthDef("PV_AddStreamPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulStreamPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivStreamPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipeStreamPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2StreamPreDelayHarmonic",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		/////////////////////// Stream PV Prebuf + DelayHarmonic WITH EnvGen////////////////////

		SynthDef("PV_AddStreamPreDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				gate = Trig1.kr(Impulse.kr(flux * 100), flatness);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MulStreamPreDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_MagDivStreamPreDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_MagDiv(fft1, fft2, flatness);
				chain = IFFT(chain) * 0.5 * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_BinWipeStreamPreDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_BinWipe(fft1, fft2, flatness.log10.abs - 1);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		SynthDef("PV_RectComb2StreamPreDelayHarmonicEnv",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				centroid = (centroid / 12544 * 8372).clip(50, 8372).lag(durSynth);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				gate = Trig1.kr(Impulse.kr(flux * 100), dur);
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth.max(1), 2);
				// Play Buffer
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				rate2 = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				inputSig = In.ar(in);
				phase = LFSaw.ar(rate2.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate2, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				in2 = del.sum;
				// FFT
				fft1 = FFT(LocalBuf(1024, 1), in1);
				fft2 = FFT(LocalBuf(1024, 1), in2);
				chain = PV_RectComb2(fft1, fft2, flux.log10.abs * 16, flux, flatness.log10.abs / 2);
				chain = IFFT(chain) * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		////////////////////////// EXPERIMENTAL SYNTH /////////////////////////////////////////////7

		// Experimental Synth
		SynthDef("ExperimentSynth1",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186);
				centroid = (centroid / 12544 * 8372).clip(50, 8372);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, dur, 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, gate, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  HPF.ar(chain, energy, 1, LPF.ar(chain, centroid, 1));
				chain = CombC.ar(chain, 0.2, flux, flatness);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		// Experimental Synth Stream
		SynthDef("ExperimentSynthStream1",
			{arg in=0, out=0, buffer, gate=1, loop=1, offset=0, reverse=1,
				freq=440, amp=0, dur=1, durSynth=1.0, durSample=1,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, ctrlHP1=0.5, ctrlHP2=0.5, level1=1, level2=0,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,  envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain,  inputSig, rate, envelope, in1, in2, fft1, fft2, phase, maxDel=0.05, envDel, del, rate2;
				// Normalize
				flux = flux.clip(0.01, 1.0);
				flatness = flatness.clip(0.1, 0.5);
				energy = (energy / 8372 * 4186).clip(50, 4186);
				centroid = (centroid / 12544 * 8372).clip(50, 8372);
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct * reverse;
				//gate = Impulse.kr(dur.reciprocal);
				// Envelope
				envelope = EnvGen.kr(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7], 'sine'), gate, 1, 0, durSynth, 2);
				// Play Buffer
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate.lag(bpm), 1, BufFrames.kr(buffer) * offset, 1, ctrlHP1, ctrlHP2);
				chain =  HPF.ar(chain, energy, 1, LPF.ar(chain, centroid, 1));
				chain = CombC.ar(chain, 0.2, flux, flatness);
				chain = chain * envelope * amp;
				// Out
				Out.ar(out, chain);
		}).add;

		//////////////////////////// FX //////////////////////////////////////

		SynthDef('AllpassC',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				//FX
				3.do({arg i; in = AllpassC.ar(in, 1, Rand(0.01, 1), Rand(0.1, 1))}) * envelope;
				chain = Mix(in);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('DelayC',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				//FX
				chain = Mix(DelayC.ar(in, 4, [Rand(0.01, 4), Rand(0.01, 4), Rand(0.01, 4)])) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('BPF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = BPF.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('BRF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = BRF.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('MoogFF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = MoogFF.ar(in, centroid, flatness*4) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('RLPF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = RLPF.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('BLowPass4',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = BLowPass4.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('RHPF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = RHPF.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('BHiPass4',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = BHiPass4.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Median',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				//FX
				chain = Median.ar(flatness * 30 + 1, in) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('LeakDC',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				//FX
				chain = LeakDC.ar(in, flux) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Median+LeakDC',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				//FX
				chain = LeakDC.ar(Median.ar(flatness * 30 + 1, in), flux) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Ringz',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = Ringz.ar(in, centroid, flux, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('HPF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = HPF.ar(in, centroid) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('LPF',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = LPF.ar(in, centroid) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Resonz',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = Resonz.ar(in, centroid, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Formlet',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186);
				//FX
				chain = Formlet.ar(in, centroid, flux, flatness, 0.25) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('CombC',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = CombC.ar(in, 0.2, centroid.reciprocal, flatness, 1) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagNoise
		SynthDef('PV_MagNoise',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagNoise(chain);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagClip
		SynthDef('PV_MagClip',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagClip(chain, (1 - flatness) * 16);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagSmooth
		SynthDef('PV_MagSmooth',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagSmooth(chain, flatness);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_Diffuser
		SynthDef('PV_Diffuser',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise1.kr(flux * 100), (flatness * 100).reciprocal));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_BrickWall
		SynthDef('PV_BrickWall',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_BrickWall(chain, flatness * 200 - 100 / 100);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_LocalMax
		SynthDef('PV_LocalMax',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_LocalMax(chain, flatness * 64);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagSquared
		SynthDef('PV_MagSquared',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagSquared(chain);
				chain= IFFT(chain) * 0.25 * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagBelow
		SynthDef('PV_MagBelow',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagBelow(chain, flatness * 10);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagAbove
		SynthDef('PV_MagAbove',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagAbove(chain, flatness * 64);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_RandComb
		SynthDef('PV_RandComb',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_RandComb(chain, flatness, LFNoise1.kr(flux * 100));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagShift',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagShift(chain, 1, flux - 0.5 * 128);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagStretch',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagShift(chain, flatness.log.abs.clip(0.25, 4));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagShift+Stretch',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagShift(chain, flatness.log.abs.clip(0.25, 4), flux - 0.5 * 128);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_BinScramble
		SynthDef('PV_BinScramble',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_BinScramble(chain, flatness, flux, LFNoise1.kr(flux.reciprocal));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_BinShift
		SynthDef('PV_BinShift',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_BinShift(chain, flatness.log.abs.clip(0.25, 4), flatness - 0.5 * 128);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_RectComb
		SynthDef('PV_RectComb',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_RectComb(chain, flatness * 32, flux, flatness);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_ConformalMap
		SynthDef('PV_ConformalMap',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_ConformalMap(chain, flatness * 2 - 1, flux * 2 - 1);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_Compander
		SynthDef('PV_Compander',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_Compander(chain, flatness * 50, flux.log.abs, flatness.log10);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_SpectralEnhance
		SynthDef('PV_SpectralEnhance',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_SpectralEnhance(chain, flux.log2.abs.floor, flatness * 4 + 1, flatness.log2.abs);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagFreeze
		SynthDef('PV_MagFreeze',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, buffer=LocalBuf(s.sampleRate, 1).clear, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				RecordBuf.ar(in, buffer, recLevel: 1, preLevel: 0, loop: 1);
				chain = PlayBuf.ar(1, buffer, flux.log.abs.clip(0.25, 4), 1, loop: 1);
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagFreeze(chain, SinOsc.kr(flatness.log10.abs.reciprocal));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagFreeze
		SynthDef('PV_Cutoff',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, buffer=LocalBuf(s.sampleRate, 1).clear, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = (flux * 2 - 1).clip(-0.99, 0.4);
				flatness = flatness.clip(0.01, 1.0);
				RecordBuf.ar(in, buffer,  recLevel: 1, preLevel: 0, loop: 1);
				chain = PlayBuf.ar(1, buffer, 1, 1, loop: 1);
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_Cutoff(chain, flux);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('PitchShift',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = PitchShift.ar(in, 1, (centroid.cpsoct - 4).clip(0, 8), flux, flatness) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('FreqShift',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				centroid = (centroid / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				chain = FreqShift.ar(in, energy, flux * 2pi) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		SynthDef('Convolution',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, buffer, trig, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				energy = (energy / 8372 * 4186).clip(50, 4186).lag(durSynth);
				//FX
				buffer = LocalBuf(s.sampleRate * 1, 1).clear;
				trig = Impulse.kr(bpm);
				RecordBuf.ar(in, buffer, Saw.kr(energy).abs, trigger: trig);
				chain = Convolution2.ar(in, buffer, trig, 1024) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagStretch
		SynthDef('PV_MagStretch',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagShift(chain, flatness.log.abs.clip(0.25, 4));
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// PV_MagStretch
		SynthDef('PV_MagShift+Stretch',
			{arg in=0, out=0, gate=0.5, xFade=0.5,
				flux=0.5, flatness=0.5, centroid=440, energy=440, bpm=1, durSynth;
				var chain, envelope;
				in = In.ar(in);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				// Normalize
				flux = flux.clip(0.01, 1.0).lag(durSynth);
				flatness = flatness.clip(0.01, 1.0).lag(durSynth);
				chain = FFT(LocalBuf(1024, 1), in);
				chain = PV_MagShift(chain, flatness.log.abs.clip(0.25, 4), flux - 0.5 * 128);
				chain= IFFT(chain) * envelope;
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		/////////////////////////// Stereo Dolby5.1 Ambisonic etc...//////////////////////

		// Stereo
		SynthDef('Stereo',
			{arg in=0, out=0, gate=1, panX=0, panY=0, bpm=1, durSynth;
				var signal, chain, envelope;
				signal = In.ar(in, 1);
				// Pan Stereo
				//chain = Pan2.ar(signal, LFSaw.kr(bpm), 1) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				chain = Pan2.ar(signal, TRand.kr(panX, panY, Impulse.kr(bpm)).lag(durSynth)) * envelope;
				// Out Stereo
				Out.ar(out,  chain);
		}).add;

		// Rotate2
		SynthDef('Rotate2',
			{arg in=0, out=0, gate=1, panX=0, panY=0, bpm=1, durSynth;
				var signal, chain, envelope;
				signal = In.ar(in, 1);
				// Pan Rotate2
				//chain = Rotate2.ar(signal, signal, LFSaw.kr(dur.reciprocal)) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				chain = Rotate2.ar(signal, signal, TRand.kr(panX, panY, Impulse.kr(bpm)).lag(durSynth)) * envelope;
				// Out Stereo
				Out.ar(out,  chain);
		}).add;

		// MultiSpeaker
		SynthDef('MultiSpeaker',
			{arg in=0, out=0, gate=1, panX=0, panY=0, bpm=1, durSynth;
				var signal, chain, envelope;
				signal = In.ar(in, 1);
				// Pan MultiSpeaker
				//chain = PanAz.ar(numberAudioOut, signal, TRand.kr(panX, panY, Impulse.kr(dur.reciprocal)), 1, widthMC, orientationMC) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				chain = PanAz.ar(numberAudioOut, signal, TRand.kr(panX, panY, Impulse.kr(bpm).lag(durSynth)), 1, widthMC, orientationMC) * envelope;
				// Out Stereo
				Out.ar(out,  chain);
		}).add;

		// Ambisonic
		SynthDef('Ambisonic',
			{arg in=0, out=0, gate=1, panX=0, panY=0, bpm=1, durSynth;
				var signal, chain, ambisonic, envelope;
				signal = In.ar(in, 1);
				// Pan Ambisonic
				//ambisonic = PanB2.ar(signal, TRand.kr(panX, panY, Impulse.kr(dur.reciprocal))) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				//ambisonic = PanB.ar(signal, panX, panY * 0.5 * pi);
				//ambisonic = PanB.ar(signal, TRand.kr(panX, panY, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				ambisonic = PanB2.ar(signal, TRand.kr(panX, panY, Impulse.kr(bpm)).lag(durSynth)) * envelope;
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out Stereo
				Out.ar(out,  chain);
		}).add;

		// Dolby 5.1
		SynthDef('Dolby5.1',
			{arg in=0, out=0, gate=1, panX=0, panY=0, bpm=1, durSynth;
				var signal, chain, front, center, lfe, rear, envelope;
				signal = In.ar(in, 1);
				//// FL FR Center LFE RL RR -> [0, 1, 2, 3, 4, 5]
				//front = Pan2.ar(signal, LFSaw.kr(dur.reciprocal), LFSaw.kr(dur.reciprocal) + 1 / 2) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				//rear = Pan2.ar(signal, LFSaw.kr(dur.reciprocal), 1 - (LFSaw.kr(dur.reciprocal) + 1 / 2)) * EnvGen.kr(Env.linen(0.05, durSynth - 0.1, 0.05, 1, \sine), gate, 1, 0, 1, 2);
				// Envelope
				envelope = EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				front = Pan2.ar(signal, panX, panY + 1 / 2) * envelope;
				rear = Pan2.ar(signal, panX, 1 - (panY + 1 / 2))  * envelope;
				center = ((panX*panX) + (panY*panY))**0.5  * envelope;
				center = 1 - center.clip(0, 1) * envelope;
				center = signal * center  * envelope;
				lfe = LPF.ar(signal, 80) * envelope;
				Out.ar(out, front);
				Out.ar(out+2, center);
				Out.ar(out+3, lfe);
				Out.ar(out+4, rear);
		}).add;

		//////////////// Reverb PROCESSING /////////////

		// Synth GVerb
		SynthDef("GVerb Stereo",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.25118864315096, taillevel=0.12589254117942, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(GVerb.ar(signal, roomsize, revtime, damping, inputbw, spread, drylevel, earlylevel, taillevel,roomsize + 1));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth GVerb
		SynthDef("GVerb Rotate2",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(GVerb.ar(signal, roomsize, revtime, damping, inputbw, spread, drylevel, earlylevel, taillevel, roomsize + 1));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth GVerb
		SynthDef("GVerb MultiSpeaker",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(GVerb.ar(signal, roomsize, revtime, damping, inputbw, spread, drylevel, earlylevel, taillevel, roomsize + 1));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth GVerb
		SynthDef("GVerb Ambisonic",
			{arg in=0, out=0, xFade=0.5, panLo=1.neg, panHi=1,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(GVerb.ar(signal, roomsize, revtime, damping, inputbw, spread, drylevel, earlylevel, taillevel, roomsize + 1));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth GVerb
		SynthDef("GVerb Dolby5.1",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain, front, center, lfe, rear, posX, posY;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				posX = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				posY = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(GVerb.ar(signal, roomsize, revtime, damping, inputbw, spread, drylevel, earlylevel, taillevel, roomsize + 1));
				front = Pan2.ar(chain, posX);
				rear = Pan2.ar(chain, posY);
				center = ((posX*posX) + (posY*posY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				// Out
				XOut.ar(out, xFade, front);
				XOut.ar(out+2, xFade, center);
				XOut.ar(out+3, xFade, lfe);
				XOut.ar(out+4, xFade, rear);
		}).add;

		/////////////    FreeVerb    ///////////////////////

		// Synth FreeVerb
		SynthDef("FreeVerb Stereo",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.25118864315096, taillevel=0.12589254117942, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(FreeVerb.ar(signal, 1, room: earlylevel, damp: taillevel));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth FreeVerb
		SynthDef("FreeVerb Rotate2",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(FreeVerb.ar(signal, 1, room: earlylevel, damp: taillevel));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth FreeVerb
		SynthDef("FreeVerb MultiSpeaker",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(FreeVerb.ar(signal, 1, room: earlylevel, damp: taillevel));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		//Synth  FreeVerb
		SynthDef("FreeVerb Ambisonic",
			{arg in=0, out=0, xFade=0.5, panLo=1.neg, panHi=1,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(FreeVerb.ar(signal, 1, room: earlylevel, damp: taillevel));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth FreeVerb
		SynthDef("FreeVerb Dolby5.1",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain, front, center, lfe, rear, posX, posY;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				posX = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				posY = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(FreeVerb.ar(signal, 1, room: earlylevel, damp: taillevel));
				front = Pan2.ar(chain, posX);
				rear = Pan2.ar(chain, posY);
				center = ((posX*posX) + (posY*posY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				// Out
				XOut.ar(out, xFade, front);
				XOut.ar(out+2, xFade, center);
				XOut.ar(out+3, xFade, lfe);
				XOut.ar(out+4, xFade, rear);
		}).add;

		/////////////    Allpass    ///////////////////////

		// Synth Allpass
		SynthDef("Allpass Stereo",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.25118864315096, taillevel=0.12589254117942, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(AllpassC.ar(signal, 0.2, delaytime: [earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4)], decaytime: [taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60)], mul: 0.25));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth Allpass
		SynthDef("Allpass Rotate2",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(AllpassC.ar(signal, 0.2, delaytime: [earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4)], decaytime: [taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60)], mul: 0.25));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth Allpass
		SynthDef("Allpass MultiSpeaker",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(AllpassC.ar(signal, 0.2, delaytime: [earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4)], decaytime: [taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60)], mul: 0.25));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		//Synth  Allpass
		SynthDef("Allpass Ambisonic",
			{arg in=0, out=0, xFade=0.5, panLo=1.neg, panHi=1,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(AllpassC.ar(signal, 0.2, delaytime: [earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4)], decaytime: [taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60)], mul: 0.25));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth Allpass
		SynthDef("Allpass Dolby5.1",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=0.5, spread=15, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0, front, center, lfe, rear, posX, posY;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				posX = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				posY = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(AllpassC.ar(signal, 0.2, delaytime: [earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4), earlylevel / Rand(1, 4)], decaytime: [taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60), taillevel * Rand(1, 60)], mul: 0.25));
				front = Pan2.ar(chain, posX);
				rear = Pan2.ar(chain, posY);
				center = ((posX*posX) + (posY*posY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				// Out
				XOut.ar(out, xFade, front);
				XOut.ar(out+2, xFade, center);
				XOut.ar(out+3, xFade, lfe);
				XOut.ar(out+4, xFade, rear);
		}).add;

		/////////////    JPverb    ///////////////////////

		// Synth JPverb
		SynthDef("JPverb Stereo",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=1, spread=0.5, drylevel=0, earlylevel=0.25118864315096, taillevel=0.12589254117942, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(JPverb.ar(signal, t60: revtime / 2.5, damp: damping, size: inputbw * 5, earlyDiff: earlylevel, modDepth: spread, modFreq: spread * 10));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth JPverb
		SynthDef("JPverb Rotate2",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=1, spread=0.5, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(JPverb.ar(signal, t60: revtime / 2.5, damp: damping, size: inputbw * 5, earlyDiff: earlylevel, modDepth: spread, modFreq: spread * 10));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth JPverb
		SynthDef("JPverb MultiSpeaker",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=1, spread=0.5, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(JPverb.ar(signal, t60: revtime / 2.5, damp: damping, size: inputbw * 5, earlyDiff: earlylevel, modDepth: spread, modFreq: spread * 10));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		//Synth  JPverb
		SynthDef("JPverb Ambisonic",
			{arg in=0, out=0, xFade=0.5, panLo=1.neg, panHi=1,
				roomsize=10, revtime=3, damping=0.5, inputbw=1, spread=0.5, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(JPverb.ar(signal, t60: revtime / 2.5, damp: damping, size: inputbw * 5, earlyDiff: earlylevel, modDepth: spread, modFreq: spread * 10));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth JPverb
		SynthDef("JPverb Dolby5.1",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0,
				roomsize=10, revtime=3, damping=0.5, inputbw=1, spread=0.5, drylevel=0, earlylevel=0.7, taillevel=0.5, bpm=1;
				var signal, chain=0, front, center, lfe, rear, posX, posY;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				posX = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				posY = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				signal = Mix(In.ar(0, numberAudioOut));
				chain = Mix(JPverb.ar(signal, t60: revtime / 2.5, damp: damping, size: inputbw * 5, earlyDiff: earlylevel, modDepth: spread, modFreq: spread * 10));
				front = Pan2.ar(chain, posX);
				rear = Pan2.ar(chain, posY);
				center = ((posX*posX) + (posY*posY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				// Out
				XOut.ar(out, xFade, front);
				XOut.ar(out+2, xFade, center);
				XOut.ar(out+3, xFade, lfe);
				XOut.ar(out+4, xFade, rear);
		}).add;

		//////////////// Final PROCESSING /////////////

		// Synth SynthLimiter
		SynthDef("SynthLimiter",
			{arg out=0, limit=0.8;
				ReplaceOut.ar(out, Limiter.ar(LeakDC.ar(In.ar(0, numberAudioOut)), limit));// Limiter on variable numberAudioOut = (2 a x)
		}).add;

		//////////////// VST Plugin //////////////////////

		// Synth VST
		SynthDef("VST Stereo",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST Rotate2",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST MultiSpeaker",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Dust.kr(bpm)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		//Synth  VST
		SynthDef("VST Ambisonic",
			{arg in=0, out=0, xFade=0.5, panLo=1.neg, panHi=1, bpm=1, gainIn=0.5;
				var signal, chain, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Dust.kr(bpm)));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST Dolby5.1",
			{arg in=0, out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain, front, center, lfe, rear, posX, posY;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				posX = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				posY = TRand.kr(panLo, panHi, Impulse.kr(bpm));
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				front = Pan2.ar(chain, posX);
				rear = Pan2.ar(chain, posY);
				center = ((posX*posX) + (posY*posY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				// Out
				XOut.ar(out, xFade, front);
				XOut.ar(out+2, xFade, center);
				XOut.ar(out+3, xFade, lfe);
				XOut.ar(out+4, xFade, rear);
		}).add;

	}

}*/

