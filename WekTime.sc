// A Software by Herve Provini Time

WekTime {

	classvar  < s, sender, dimIn, choiceFilter, choiceFX, flagStreamMFCC, mfccData, numPreset, lastNumPreset, lastTimeWekPreset, timeWekPreset, listeWekPreset, timeWekData, lastTimeWekData, flagWTD, flagWTP, numSequencer, wekBPM;

	var pathWekTime, numberAudioOut, recChannels, groupeSynth, listeGroupSynth, listeGroupDolby, numberSynth, sequencer, windowControlGUI, cmdperiodfunc, listeBusInFilter, listeBusInFX, listeBusOutFX, listeBusInDolby, listeBuffer, listeSoundFile, fonctionLoadSample, synthLimiter, typeSequencer, listeOctave, listeActiveJitterOctave, listeJitterOctave, listeDemiTon, listeActiveJitterDemiTon, listeJitterDemiTon, listeCent, listeActiveJitterCent, listeJitterCent, listeAmp, listeActiveJitterAmp, listeJitterAmp, listeJitterWaveForm, listeStartPos, listeLenght, listeReverse, changeChoiceTrigger, densityBPM, indexSequence, listeEnvelopeSynth, listeFilters, listeFX, listeCtrl1Filter, listeActiveJitterCtrl1Filter, listeCtrl2Filter, listeActiveJitterCtrl2Filter;

	var listeCtrl3Filter, listeActiveJitterCtrl3Filter, listeActiveJitterPanX, listeActiveJitterPanY, listeCtrl1FX, listeCtrl2FX, listeCtrl3FX, listeCtrl4FX, listeCtrl5FX, listeGUIpanner, helpWekTime, menuWekTime, fonctionRecOn, fonctionRecOff, fonctionRecPause, flagRecording, bufferRecording, headerFormat, sampleFormat, formatRecordingMenu, sampleFormatRecordingMenu, fonctionUserOperatingSystem, fonctionLoadPreset, fonctionSavePreset, fonctionShortCut, fonctionCommandes, commande, bufferAndSoundFile, listeGUIsoundFile, listeActiveJitterWavePos, listeJitterVolumeFilter, listeActiveJitterVolumeFilter;

	var listeJitterCtrl1Filter, listeJitterCtrl2Filter, listeJitterCtrl3Filter, listeJitterVolumeFX, listeActiveJitterVolumeFX,  listeJitterCtrl1FX, listeActiveJitterCtrl1FX, listeJitterCtrl2FX, listeActiveJitterCtrl2FX,  listeJitterCtrl3FX, listeActiveJitterCtrl3FX, listeJitterCtrl4FX, listeActiveJitterCtrl4FX, listeJitterCtrl5FX, listeActiveJitterCtrl5FX,  listeVolumeFilter, listeVolumeFX, listePanX, listePanY, listeJitterPanX, listeJitterPanY, listeMuteSynth, listeSoloSynth, choiceTypeSequencer, numberMaxStepSequencer, numberStepSequencer, listeSynthStepSequencer;

	var listeWeightSynth, file, fonctionSetupSliders, modeMIDIOSC, bendMIDI, changeChoiceMIDI, requestSynthesizerSource, requestSynthesizerTarget, fonctionCopySourceSynth, fonctionCopyTargetSynth, synthSource, synthTarget, copySynthMenu, choiceCanalMIDI, canalMIDI, lastDureeMIDI, menuMIDI, choiceTypeSynthDef, changeChoiceSynthDef, typeSynthDef, scalingTuningMenu, scale, tuning, flagScaling, degrees, root, startSystem, synthAudioIn;

	var hprec1, hprec2, synthAudioRec,  listeBufferAudioRec, busAudioIn, groupeAudioRec, listeGroupAudioRec, synthFileIn, fonctionLoadFileForAnalyse, bufferFile, listeActiveAudioRec, serverAdresse, masterAppAddr, slaveAppAddr, ardourOSC, synthOSConset, synthOSCpitch, synthOSCpitch2, synthOSCkeytrack, synthOSCkeyboard, timeOSC, chordDureeOSC, maxDureeOSC, flagOSC, windowExternalControlGUI, userOperatingSystem, userOSchoiceControl, fhzFilter, ampFilter, durFilter, setupKeyboardShortCut, windowKeyboard, keyboardShortCut, keyboardTranslate, keyboard, keyVolume, keyboardTranslateBefore, keyboardVolume;
	var freqBefore, ampBefore, dureeBefore, flagKeyboard, indexWindows, listeWindows, activateOSC, oscHPtempo, oscHPstart, oscHPrec, oscState, oscStateFlag, initOSCresponder, audioFileText, switchOSCfreq, switchOSCamp, switchOSCdur, ampMIDIOSC, fonctionCollectFolders, foldersToScanAll, foldersToScanPreset, foldersToScanSynthesizer, flagAutomation, lastValue1Automation, lastValue2Automation, lastNumberChoiceConfig, fonctionAutomationPreset;

	var lastTimeAutomation, thresholdAutomation, lastTime, typeAudio, midiOut, choiceCanalMidiOut, flagMidiOut, freqMidi, synthCanalMidiOut, listeFileAnalyze, listeNameFileAnalyze, listeFlagDureeSynth, loopSample, sampleMenu, loopMenu, typeMasterOut, menuFile, menuRecording, menuPreset, menuSynth, menuHelp, menuAlgo, menuScale, menuOSC, busOSCfreq, busOSCamp, busOSCduree, busOSCtempo, busOSCflatness, busOSCflux, busOSCenergy, busOSCcentroid, tempoOSC, oscTempo, flagTempo, synthOSCFFT, fonctionInitBand, numFhzBand, lastTimeBand, bandFHZ, fonctionBand, flagIndexBand;
	var rangeNumFhzBand, listeDataBand, flagMIDI, listeGroupFX, listeGroupFilter, listeBusSynth, listeLoop, listeRecLevel, listePreLevel, audioDisplay, autoRoot, bpmDisplay, ambitusFreq, windowVST, flagVST, groupeLimiter, widthMC, orientationMC, numberAudioIn, rangeFFT, rangeBand;

	*new	{arg path="~/Documents/WekTime/", ni=26, numberOut=2, numberRec=2, format=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0, name="WekTime", wek=6448, wekPort=57120, scPort=57110;

		^super.new.init(name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag=0, wek, wekPort, scPort);

	}

	init	{arg name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag=0, wek, wekPort, scPort;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		~pathWekTime = PathName.new(path).pathOnly;

		// Verify Folder
		if(File.exists(~pathWekTime).not) {systemCmd("mkdir" + ~pathWekTime)};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		numberAudioOut = numberOut;
		recChannels = numberRec;
		numberAudioIn = ni;
		widthMC = wid;
		orientationMC = ori;

		// Setup Server Options
		//Server.default = s = Server(name,NetAddr("localhost", scPort), Server.default.options);
		thisProcess.openUDPPort(wekPort.asInteger); // Sender Port Wekinator + Enter Port change 6448 to an another for example 6449

		s = Server.default;
		s.options.memSize = 2 ** 20;
		s.options.inDevice_(devIn);
		s.options.outDevice_(devOut);
		recChannels = numberRec;
		s.recChannels_(numberRec);
		s.options.numInputBusChannels_(numberAudioIn);
		s.options.numOutputBusChannels_(numberOut);
		s.options.hardwareBufferSize_(size);
		typeMasterOut = switch(format,
			0, {"Stereo"},
			1, {"Rotate2"},
			2, {"MultiSpeaker"},
			3, {"Ambisonic"},
			4, {"Dolby5.1"},
		);// Type Format stereo, ambisonic, etc...

		thisProcess.openUDPPort(NetAddr.langPort);

		Safety(s);
		//s.makeGui;

		// Open Wekinator
		sender.free;
		sender = NetAddr.new("127.0.0.1", wek);// Wekinator
		Pipe.new("open -a Wekinator", "r").close;

		// Run the Soft
		this.run;

	}

	run	{

		// OSCFunc Score
		OSCFunc.newMatching({arg msg, time, addr, recvPort;

			var array, cmd = 'on', number, file, item = 0;

			msg.removeAt(0);
			msg.postcs;

			while({cmd != nil},
				{
					cmd = msg[item].postln;
					if(cmd == 'all' or: {cmd == 'wektime'},
						{
							cmd = msg[item+1].postln;
							// Preset
							if(cmd == 'preset',
								{
									number = msg[item+2].asInteger.postln;
									{
										if(File.exists(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd"), {
											file=File(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd","r");
											fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on'); file.close;
											windowControlGUI.name="Time a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Preset" + number.asString}, {"cancelled".postln});
									}.defer;
							});
							// Stop
							if(cmd == 'stop', {
								{
									windowExternalControlGUI.view.children.at(0).valueAction_(0);
								}.defer;
							});
							// Start
							if(cmd == 'start', {
								{
									windowExternalControlGUI.view.children.at(0).valueAction_(1);
								}.defer;
							});
							// Wekinator Start
							if(cmd  == 'wekrun', {
								flagStreamMFCC = 'wek'; sender.sendMsg("/wekinator/control/startRunning");
								{
									windowExternalControlGUI.view.children.at(32).valueAction = 0;// rec
									windowExternalControlGUI.view.children.at(34).value = 1;// run
								}.defer;
							});
							//Wekinator Stop
							if(cmd == 'wekstop', {
								flagStreamMFCC = 'off'; sender.sendMsg("/wekinator/control/stopRunning");
								{
									windowExternalControlGUI.view.children.at(34).value = 0;// run
								}.defer;
							});
					});
					item = item + 3;
					cmd = msg[item];
			});
		}, \score, recvPort: NetAddr.langPort);

		fonctionCollectFolders = {
			// Collect all Preset
			foldersToScanAll = PathName.new(~pathWekTime).files.collect{ |path| var file;
				file = path.fileName;
				if(file.find("Preset") == 0 or: {file.find("preset") == 0} or: {file.find("Synthesizer") == 0} or: {file.find("synthesizer") == 0}, {file});
			};
			foldersToScanAll = foldersToScanAll.reject({arg item; item == nil});
			// Collect preset
			foldersToScanPreset = foldersToScanAll.collect{ |file|
				if(file.find("Preset") == 0 or: {file.find("preset") == 0}, {file});
			};
			foldersToScanPreset = foldersToScanPreset.reject({arg item; item == nil});
			// Collect Synthesizer
			foldersToScanSynthesizer = foldersToScanAll.collect{ |file|
				if(file.find("Synthesizer") == 0 or: {file.find("synthesizer") == 0}, {file});
			};
			foldersToScanSynthesizer = foldersToScanSynthesizer.reject({arg item; item == nil});
		};
		fonctionCollectFolders.value;

		// Help
		helpWekTime="
Single commandes:

esc	or SpaceBar			System on/off.
alt + r					Start Recording.
ctrl + alt + r			Stop Recording.
R						Pause Recording.
t						Switch Type Sequencer.
m						Switch MIDI Mode.
i						Init Preset.
ctrl + f				Load and Add File for AudioIn.
w / ctrl + w			Switch Windows.
k						New Environment.
z						Load Random Preset.
Z						Load Random Synthesizer.
u						Switch Automation Preset on/off.
h						Switch Source In.
q						Switch Algo Analyze.

Commandes follow by a numerical key (0,..9 ; shift 0,..9 ; alt 0,..9 ; alt + shift 0,..9):

l			 			Load Preset + Control.
alt+l                   Load Preset.
s				 		Save Preset.
L			 			Load Synthesizer.
S				 		Save Synthesizer.
o						Set Synthesizer Source.
a						Set Synthesizer Target and Copy Source on Target.
f						Switch File for Analyze.

";

		// INIT VARIABLES
		numberSynth = 4;
		numberStepSequencer = numberMaxStepSequencer = 48;
		listeSynthStepSequencer = [];
		listeWeightSynth = [];
		listeGroupSynth = [];
		listeGroupFX = [];
		listeGroupFilter = [];
		listeGroupDolby = [];
		listeGroupAudioRec = [];
		typeSequencer = 'Rand';
		changeChoiceTrigger = [
			'Rand',
			'Seq',
			'WeightS',
			'WeightP',
		];
		densityBPM = [30, 120] / 60;
		wekBPM = [30, 120];
		indexSequence = 0;
		changeChoiceMIDI = [
			'Translate',
			'Note',
		];
		choiceCanalMIDI = [
			'MIDI IN 1',
			'MIDI IN 2',
			'MIDI IN 3',
			'MIDI IN 4',
			'MIDI IN 5',
			'MIDI IN 6',
			'MIDI IN 7',
			'MIDI IN 8',
			'MIDI IN 9',
			'MIDI IN 10',
			'MIDI IN 11',
			'MIDI IN 12',
			'MIDI IN 13',
			'MIDI IN 14',
			'MIDI IN 15',
			'MIDI IN 16',
		];
		changeChoiceSynthDef = [
			'TGrains',
			'Warp1',
			'BufRd',
			'LoopBuf',
			'PlayBuf',
			'PlayBufMedianLeakDC',
			'Waveloss',
			'DelayHarmonic',
			'Synth HPshiftDown',
			'Synth MagShift',
			'Synth LocalMax',
			'Synth RandComb',
			'Synth BinShift',
			'Synth BinScramble',
			'Synth BrickWall',
			'Synth ConformalMap',
			'Synth Diffuser',
			'Synth MagAbove',
			'Synth MagBelow',
			'Synth MagClip',
			'Synth MagSquared',
			'Synth RectComb',
			'Synth MagSmooth',
			'Synth Compander',
			'Synth SpectralEnhance',
			'Synth MagStretch',
			'Synth MagShift+Stretch',
			'Synth Cutoff',
			'SinOsc',
			'SawSynth',
			'CombSynth',
			'MdaPiano',
			'Guitare',
			'StringSynth',
			'Gendy3',
			'Blip',
			'DynKlang',
			'Klang',
			'Formant',
			'FM',
			'Ring',
			'AnalogKick',
			'AnalogSnare',
			'AnalogHiHat',
			'SOSkick',
			'SOSsnare',
			'SOShats',
			'SOStom',
		];
		userOSchoiceControl = [
			'UserOperatingSystem',
			'Load Preset+Control',
			'Load Preset',
			'Save Preset',
			'Load Synthesizer',
			'Save Synthesizer',
			'Set Synth Source',
			'Copy on Synth Target',
		];
		hprec1 = 0.5;
		hprec2 = 0.5;
		modeMIDIOSC = [];
		ampMIDIOSC = 0;
		bendMIDI = 0;
		canalMIDI = 0;
		synthCanalMidiOut = [];
		lastDureeMIDI = Main.elapsedTime;
		flagRecording = 'off';
		synthSource = 0;
		synthTarget = 1;
		tuning = Tuning.et12;
		degrees = tuning.semitones;
		root = 0;
		scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning);
		flagScaling = 'off';
		timeOSC = Main.elapsedTime;
		chordDureeOSC = 0.0625;
		maxDureeOSC = 4.0;
		flagOSC = 0;
		fhzFilter = 0.5;
		ampFilter = 1;
		durFilter = 0.03125;
		flagKeyboard = 'off';
		flagAutomation = 'off';
		indexWindows = 0;
		keyboardTranslateBefore = 0;
		keyVolume = 12.neg.dbamp;
		lastValue1Automation = 5;
		lastValue2Automation = 5;
		lastNumberChoiceConfig = nil;
		lastTimeAutomation = Main.elapsedTime;
		thresholdAutomation = 2;
		flagMidiOut = 'off';
		flagMIDI = 0;
		freqMidi = [];
		listeFileAnalyze=[];
		listeNameFileAnalyze=[];
		listeFlagDureeSynth = [];
		loopSample = [];
		tempoOSC = 1;
		flagTempo = 0;
		numFhzBand = 3; // Nombre de band de fhz (+1 pour all data) pour trier dans les synth index=0 pour all index=1 pour badnnum 1 etc...
		bandFHZ = Array.fill(numFhzBand, {arg i; [127 / numFhzBand * i, 127 / numFhzBand * i + (127 / numFhzBand )]}).midicps;
		bandFHZ = bandFHZ.reverse;
		bandFHZ = bandFHZ.add([0, 127].midicps);
		bandFHZ = bandFHZ.reverse;
		listeDataBand =[];
		lastTimeBand = [];
		listeBusSynth = [];
		autoRoot = 'off';
		ambitusFreq = [0, 127];
		flagVST = 'off';
		~channelsSynth = [0, 0, 0, 0];
		rangeFFT = [0, 1];
		dimIn = 13;// 13 bands MFCC
		flagStreamMFCC = 'off';
		numPreset = 0;
		lastNumPreset = 0;
		timeWekPreset = 4;
		timeWekData = 0.0625;
		flagWTD = 'on';
		flagWTP = 'on';
		40.do({arg i; listeWekPreset = listeWekPreset.add(i+1)});
		numSequencer = 0;

		// Musical Data
		numberSynth.do({arg synth;
			var listeStep=[];
			listeOctave = listeOctave.add(0);
			listeActiveJitterOctave = listeActiveJitterOctave.add(0);
			listeJitterOctave = listeJitterOctave.add(0.1);
			listeDemiTon = listeDemiTon.add(0);
			listeActiveJitterDemiTon = listeActiveJitterDemiTon.add(0);
			listeJitterDemiTon = listeJitterDemiTon.add(0.1);
			listeCent = listeCent.add(0);
			listeActiveJitterCent = listeActiveJitterCent.add(0);
			listeJitterCent = listeJitterCent.add(0.1);
			listeAmp = listeAmp.add(-6);
			listeActiveJitterAmp = listeActiveJitterAmp.add(0);
			listeJitterAmp = listeJitterAmp.add(0.1);
			// Filter
			listeVolumeFilter = listeVolumeFilter.add(-6);
			listeJitterVolumeFilter = listeJitterVolumeFilter.add(0.1);
			listeActiveJitterVolumeFilter = listeActiveJitterVolumeFilter.add(0);
			listeCtrl1Filter = listeCtrl1Filter.add(440);
			listeJitterCtrl1Filter = listeJitterCtrl1Filter.add(0.1);
			listeActiveJitterCtrl1Filter = listeActiveJitterCtrl1Filter.add(0);
			listeCtrl2Filter = listeCtrl2Filter.add(0.5);
			listeJitterCtrl2Filter = listeJitterCtrl2Filter.add(0.1);
			listeActiveJitterCtrl2Filter = listeActiveJitterCtrl2Filter.add(0);
			listeCtrl3Filter = listeCtrl3Filter.add(0.5);
			listeJitterCtrl3Filter = listeJitterCtrl3Filter.add(0.1);
			listeActiveJitterCtrl3Filter = listeActiveJitterCtrl3Filter.add(0);
			// FX
			listeVolumeFX = listeVolumeFX.add(-6);
			listeJitterVolumeFX = listeJitterVolumeFX.add(0.1);
			listeActiveJitterVolumeFX = listeActiveJitterVolumeFX.add(0);
			listeCtrl1FX = listeCtrl1FX.add(0.03);
			listeJitterCtrl1FX = listeJitterCtrl1FX.add(0.1);
			listeActiveJitterCtrl1FX = listeActiveJitterCtrl1FX.add(0);
			listeCtrl2FX = listeCtrl2FX.add(0.3);
			listeJitterCtrl2FX = listeJitterCtrl2FX.add(0.1);
			listeActiveJitterCtrl2FX = listeActiveJitterCtrl2FX.add(0);
			listeCtrl3FX = listeCtrl3FX.add(0.3);
			listeJitterCtrl3FX = listeJitterCtrl3FX.add(0.1);
			listeActiveJitterCtrl3FX = listeActiveJitterCtrl3FX.add(0);
			listeCtrl4FX = listeCtrl4FX.add(0.3);
			listeJitterCtrl4FX = listeJitterCtrl4FX.add(0.1);
			listeActiveJitterCtrl4FX = listeActiveJitterCtrl4FX.add(0);
			listeCtrl5FX = listeCtrl5FX.add(0.3);
			listeJitterCtrl5FX = listeJitterCtrl5FX.add(0.1);
			listeActiveJitterCtrl5FX = listeActiveJitterCtrl5FX.add(0);
			// Panner
			listePanX = listePanX.add(0);
			listeActiveJitterPanX = listeActiveJitterPanX.add(0);
			listeJitterPanX = listeJitterPanX.add(0.1);
			listePanY = listePanY.add(0);
			listeActiveJitterPanY = listeActiveJitterPanY.add(0);
			listeJitterPanY = listeJitterPanY.add(0.1);
			// Synth Activation
			listeMuteSynth = listeMuteSynth.add(0);
			listeSoloSynth = listeSoloSynth.add(0);
			// Sequencer
			numberMaxStepSequencer.do({listeStep=listeStep.add(0)});
			listeSynthStepSequencer = listeSynthStepSequencer.add(listeStep);
			listeWeightSynth = listeWeightSynth.add(0.5);
			//Switch OSC
			switchOSCfreq = switchOSCfreq.add(1);
			switchOSCamp = switchOSCamp.add(0);
			switchOSCdur = switchOSCdur.add(0);
			// Choix SynthDef
			typeSynthDef = typeSynthDef.add('TGrains');
			synthCanalMidiOut = synthCanalMidiOut.add(-1);
			freqMidi = freqMidi.add(0);
			flagIndexBand = flagIndexBand.add([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
			rangeNumFhzBand = rangeNumFhzBand.add([]);
			listeFlagDureeSynth = listeFlagDureeSynth.add('Seq');
			modeMIDIOSC = modeMIDIOSC.add('Translate');
			loopSample = loopSample.add(0);
			listeBusSynth = listeBusSynth.add(0);
			choiceFilter = choiceFilter.add(0);
			choiceFX = choiceFX.add(0);
		});

		listeFilters = [
			'ByPass',
			'LPF',
			'HPF',
			'RLPF',
			'RHPF',
			'BPF',
			'BRF',
			'Ringz',
			'Resonz',
			'CombC',
			'Formlet',
			'FreqShift',
			'PitchShift',
			'PV_MagFreeze',
			'PV_MagNoise',
			'PV_MagClip',
			'PV_MagSmooth',
			'PV_Diffuser',
			'PV_BrickWall',
			'PV_LocalMax',
			'PV_MagSquared',
			'PV_MagBelow',
			'PV_MagAbove',
			'PV_RandComb',
			'PV_MagShift',
			'PV_BinScramble',
			'PV_BinShift',
			'PV_RectComb',
			'PV_ConformalMap',
			'PV_Compander',
			'PV_SpectralEnhance',
			'PV_MagStretch',
			'PV_MagShift+Stretch',
			'DJ_FX',
			'WaveLoss',
			'Median',
			'LeakDC',
			'Median+leakDC',
			'PV_Cutoff',
			'Delay',
			'Warp+Delay'
		];
		listeFX = [
			'ByPass',
			'AllpassC',
			'FreeVerb',
			'GVerb',
			'JPverb',
			'CombC',
			'DelayC',
			'WarpDelay',
		];

		// Fonctions Recording
		fonctionRecOn={
			if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
			if(flagRecording == 'off', {
				flagRecording = 'on';
				s.bind{
					s.prepareForRecord("~/Music/SuperCollider Recordings/".standardizePath ++ "WekTime_" ++ Date.localtime.stamp ++ ".aiff");
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

		// Fonction User Operating System
		fonctionUserOperatingSystem = {arg item, window;
			var data;
			item.value.switch(
				0, {nil},
				// Load Preset + control
				1, {
					Dialog.openPanel({ arg paths;
						var f;
						f=File(paths,"r");
						fonctionLoadPreset.value(f.readAllString.interpret, windowControlGUI, 'on');
						f.close;
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  PathName.new(paths).fileName;
						s.queryAllNodes;"".postln;
					}, {"cancelled".postln});
				},
				// Load Preset
				2, {
					Dialog.openPanel({ arg paths;
						var f;
						f=File(paths,"r");
						fonctionLoadPreset.value(f.readAllString.interpret, windowControlGUI,'off');
						f.close;
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  PathName.new(paths).fileName;
						s.queryAllNodes;"".postln;
					}, {"cancelled".postln});
				},
				// Save Preset
				3, {
					Dialog.savePanel({arg p;
						var f;
						f=File(p ++ ".scd", "w");
						f.write(fonctionSavePreset.value(windowControlGUI).asCompileString);
						f.close;
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + PathName.new(p).fileName},
					{"cancelled".postln});
				},
				// Load Synthesizer
				4, {
					SCRequestString("1", "Synthesizer Target", {arg target;
						// Verify if valid Synthesizer
						if(target.asInteger >= 1 or: {target.asInteger <= numberSynth}, {synthTarget = target.asInteger - 1;
							Dialog.openPanel({ arg paths;
								var f;
								f=File(paths,"r");
								fonctionCopyTargetSynth.value(f.readAllString.interpret, synthTarget);
								f.close;
								windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + PathName.new(paths).fileName;

								s.queryAllNodes;"".postln;
							}, {"cancelled".postln});
						}, {synthTarget = nil; "cancelled".postln});
					});
				},
				// Save Synthesizer
				5, {
					SCRequestString("1", "Synthesizer Source", {arg source;
						// Verify if valid Synthesizer
						if(source.asInteger >= 1 or: {source.asInteger <= numberSynth}, {synthSource = source.asInteger - 1;
							Dialog.savePanel({arg p;
								var f;
								f=File(p ++ ".scd", "w");
								f.write(fonctionCopySourceSynth.value(synthSource).asCompileString);
								f.close;
								windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + PathName.new(p).fileName},
							{"cancelled".postln});
						}, {synthSource = nil; "cancelled".postln});
					});
				}
			)
		};

		// Fonction Load Preset
		fonctionLoadPreset = {arg data, window, flag;
			var a, b, c, d, e, f, control;

			// Smooth Release
			listeGroupSynth.do({arg g; g; g.release(4)});
			listeGroupFilter.do({arg f; f.release(4)});
			listeGroupFX.do({arg x; x.release(4)});

			// load Buffer Synth and Setup Sliders Sample
			numberSynth.do({arg synth;
				var item1, item2;
				item1 = (synth * 93 + 19) + (numberSynth * numberMaxStepSequencer + 14);
				item2 = (synth * 93 + 35) + (numberSynth * numberMaxStepSequencer + 14);
				bufferAndSoundFile = fonctionLoadSample.value(data.at(item1).at(1), listeGroupSynth.at(synth), listeBuffer.at(synth), listeBufferAudioRec.at(synth));
				listeBuffer.put(synth, bufferAndSoundFile.at(0));
				listeSoundFile.put(synth, bufferAndSoundFile.at(1));
				listeBufferAudioRec.put(synth, bufferAndSoundFile.at(2));
				listeStartPos.put(synth, data.at(item2).at(1).at(0));
				listeLenght.put(synth, data.at(item2).at(1).at(1));
				s.ping(2, 1, {
					{
						listeGUIsoundFile.at(synth).currentSelection_(0);
						listeGUIsoundFile.at(synth).soundfile_(listeSoundFile.at(synth)).read(0,
							listeSoundFile.at(synth).numFrames).refresh;
						if(listeActiveJitterWavePos.at(synth).value == 0, {listeGUIsoundFile.at(synth).setSelectionColor(0, Color.new(0.985, 0.841, 0))},{listeGUIsoundFile.at(synth).setSelectionColor(0, Color.red)});
					}.defer;
				});
			});
			// SetUp Views
			window.view.children.do({arg view, item;
				if(item != ((0  * 93 + 9) + (numberSynth * numberMaxStepSequencer + 14)) and: {item != ((1  * 93 + 9) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((2  * 93 + 9) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((3  * 93 + 9) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((0  * 93 + 35) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((1  * 93 + 35) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((2  * 93 + 35) + (numberSynth * numberMaxStepSequencer + 14))} and: {item != ((3  * 93 + 35) + (numberSynth * numberMaxStepSequencer + 14))},
					{
						// QCompositeView
						if(data.at(item).at(0) == "a View" or: {data.at(item).at(0) == "a CompositeView"} or: {data.at(item).at(0) == "a QCompositeView"} or: {data.at(item).at(0) == "a QView"} or: {data.at(item).at(0) == "a SCCompositeView"}, {
							if(data.at(item).at(1) == "a QStaticText" or: {data.at(item).at(1) == "a SCStaticText"} or: {data.at(item).at(1) == "a StaticText"} or: {data.at(item).at(1) == nil}, {view.children.at(1).valueAction_(data.at(item).at(2).at(1))});
							if(data.at(item).at(1) == "a QSlider" or: {data.at(item).at(1) == "a SCSlider"} or: {data.at(item).at(1) == "a Slider"}, {view.children.at(2).valueAction_(data.at(item).at(2).at(2))});
							if(data.at(item).at(1) == "a QRangeSlider" or: {data.at(item).at(1) == "a SCRangeSlider"} or: {data.at(item).at(1) == "a RangeSlider"}, {view.children.at(2).activeLo_(0); view.children.at(2).activeHi_(1); view.children.at(2).activeLo_(data.at(item).at(2).at(2).at(0)); view.children.at(2).activeHi_(data.at(item).at(2).at(2).at(1))});
							if(data.at(item).at(1) == "a QKnob" or: {data.at(item).at(1) == "a SCKnob"} or: {data.at(item).at(1) == "a Knob"}, {view.children.at(2).valueAction_(data.at(item).at(2).at(2))});
						});
						// StaticText
						if(data.at(item).at(0)  == "a QStaticText" or: {data.at(item).at(0)  == "a SCStaticText"} or: {data.at(item).at(0)  == "a StaticText"},
							{view.string_(data.at(item).at(1));
						});
						// QPopUpMenu + QButton
						if(data.at(item).at(0)  == "a QPopUpMenu" or: {data.at(item).at(0) == "a QEnvelopeView"} or: {data.at(item).at(0) == "a QButton"} or: {data.at(item).at(0)  == "a SCPopUpMenu"} or: {data.at(item).at(0) == "a SCEnvelopeView"} or: {data.at(item).at(0) == "a SCButton"} or: {data.at(item).at(0)  == "a PopUpMenu"} or: {data.at(item).at(0) == "an EnvelopeView"} or: {data.at(item).at(0) == "a Button"},
							{view.valueAction_(data.at(item).at(1))});
						// QSoundFileView
						if(data.at(item).at(0)  == "a QSoundFileView" or: {data.at(item).at(0)  == "a SCSoundFileView"} or: {data.at(item).at(0)  == "a SoundFileView"},
							{view.setSelection(0, data.at(item).at(1).at(0));
						});
						// QSlider2D (special case)
						if(data.at(item).at(0)  == "a QSlider2D" or: {data.at(item).at(0)  == "a SCSlider2D"} or: {data.at(item).at(0)  == "a Slider2D"},
							{view.setXYActive(data.at(item).at(1), data.at(item).at(2))});
						// NumberBox
						if(data.at(item).at(0)  == "a NumberBox" or: {data.at(item).at(0) == "a QNumberBox"} or: {data.at(item).at(0) == "a SCNumberBox"},
							{view.valueAction_(data.at(item).at(1))});
				});
			});
			//Control + NumFhzBand
			control = data.last;
			if(flag == 'on', {
				//windowExternalControlGUI.view.children.at(2).valueAction_(control.at(0));
				windowExternalControlGUI.view.children.at(7).valueAction_(control.at(1));
				//windowExternalControlGUI.view.children.at(10).children.at(2).valueAction_(control.at(2));
				//windowExternalControlGUI.view.children.at(11).children.at(2).valueAction_(control.at(3));
				windowExternalControlGUI.view.children.at(12).valueAction_(control.at(4));
				windowExternalControlGUI.view.children.at(13).children.at(2).valueAction_(control.at(5));
				windowExternalControlGUI.view.children.at(14).children.at(2).valueAction_(control.at(6));
				windowExternalControlGUI.view.children.at(15).children.at(2).valueAction_(control.at(7));
				windowExternalControlGUI.view.children.at(16).children.at(2).valueAction_(control.at(8));
				windowExternalControlGUI.view.children.at(17).children.at(2).valueAction_(control.at(9));
				windowExternalControlGUI.view.children.at(18).children.at(2).valueAction_(control.at(10));
				windowExternalControlGUI.view.children.at(19).children.at(2).valueAction_(control.at(11));
				windowExternalControlGUI.view.children.at(20).children.at(2).valueAction_(control.at(12));
				windowExternalControlGUI.view.children.at(21).children.at(2).valueAction_(control.at(13));
			});
			// Init Band
			windowExternalControlGUI.view.children.at(22).children.at(2).valueAction_(control.at(14));
			// Init Range FFT
			rangeFFT = control.at(15);
			windowExternalControlGUI.view.children.at(27).children.at(2).activeLo = control.at(15).at(0);
			windowExternalControlGUI.view.children.at(27).children.at(2).activeHi = control.at(15).at(1);
			// Rand Band
			bandFHZ = control.at(16);
			rangeBand.value = bandFHZ;
			bandFHZ = bandFHZ.midicps;
			// HP Controls
			hprec1 = control.at(17).at(0);
			windowExternalControlGUI.view.children.at(30).valueAction = hprec1;
			hprec2 = control.at(17).at(1);
			windowExternalControlGUI.view.children.at(31).valueAction = hprec2;
		};

		// Fonction Save Preset
		fonctionSavePreset = {arg window;
			var data=[], synth=0, control=[], range=[];
			// Save views values
			window.view.children.do({arg view, index;
				var arrayData=[], subArrayData=[], subType=nil;
				// QCompositeView
				arrayData=[];
				if(view.asString == "a View" or: {view.asString == "a CompositeView"} or: {view.asString == "a QCompositeView"} or: {view.asString == "a QView"} or: {view.asString == "a SCCompositeView"},
					{view.children.do({arg subView;
						if(subView.asString == "a StaticText" or: {subView.asString == "a SCStaticText"} or: {subView.asString == "a QStaticText"} or: {subView.asString == "a TextField"} or: {subView.asString == "a SCTextField"} or: {subView.asString == "a QTextField"}, {arrayData = arrayData.add(subView.string)});
						if(subView.asString == "a QSlider" or: {subView.asString == "a SCSlider"} or: {subView.asString == "a Slider"}, {arrayData=arrayData.add(subView.value); subType = subView.asString});
						if(subView.asString == "a QRangeSlider" or: {subView.asString == "a SCRangeSlider"} or: {subView.asString == "a RangeSlider"}, {subArrayData=subArrayData.add(subView.lo);subArrayData=subArrayData.add(subView.hi); arrayData=arrayData.add(subArrayData); subType = subView.asString});
						if(subView.asString == "a QNumberBox" or: {subView.asString == "a SCNumberBox"} or: {subView.asString == "a NumberBox"}, {arrayData=arrayData.add(subView.value)});
						if(subView.asString == "a Knob" or: {subView.asString == "a QKnob"} or: {subView.asString == "a SCKnob"}, {arrayData=arrayData.add(subView.value); subType = subView.asString});
					});
					data = data.add([view.asString, subType, arrayData]);
				});
				// StaticText
				if(view.asString == "a QStaticText" or: {view.asString == "a SCStaticText"} or: {view.asString == "a StaticText"} or: {view.asString == "a SCTextField"} or: {view.asString == "a TextField"} or: {view.asString == "a QTextField"},
					{data = data.add([view.asString, view.string])});
				// QPopUpMenu + QButton
				if(view.asString == "a QPopUpMenu" or: {view.asString == "a QEnvelopeView"} or: {view.asString == "a QButton"} or: {view.asString == "a SCPopUpMenu"} or: {view.asString == "a SCEnvelopeView"} or: {view.asString == "a SCButton"} or: {view.asString == "a PopUpMenu"} or: {view.asString == "an EnvelopeView"} or: {view.asString == "a Button"},
					{data = data.add([view.asString, view.value])});
				// SoundFileView
				if(view.asString == "a QSoundFileView" or: {view.asString == "a SCSoundFileView"} or: {view.asString == "a SoundFileView"},
					{data = data.add([view.asString, view.selection(0)]);
						// Update synth
						synth = synth + 1});
				// QSlider2D (Special Case)
				if(view.asString == "a QSlider2D" or: {view.asString == "a SCSlider2D"} or: {view.asString == "a Slider2D"},
					{data = data.add([view.asString, view.x, view.y])});
				// NumberBox
				if(view.asString  == "a NumberBox" or: {view.asString == "a QNumberBox"} or: {view.asString== "a SCNumberBox"},
					{data = data.add([view.asString, view.value])});
			});
			// Control + NumFhzBand
			control = [];
			control = control.add(windowExternalControlGUI.view.children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(7).value);
			control = control.add(windowExternalControlGUI.view.children.at(10).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(11).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(12).value);
			control = control.add(windowExternalControlGUI.view.children.at(13).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(14).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(15).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(16).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(17).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(18).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(19).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(20).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(21).children.at(2).value);
			control = control.add(windowExternalControlGUI.view.children.at(22).children.at(2).value);// Band
			// Range FFT
			range = range.add(windowExternalControlGUI.view.children.at(27).children.at(2).lo);
			range = range.add(windowExternalControlGUI.view.children.at(27).children.at(2).hi);
			control = control.add(range);
			control = control.add(bandFHZ.cpsmidi.round(2));
			control = control.add([hprec1, hprec2]);
			data = data.add(control);
			// Sortie Data
			data.value;
		};

		// Fonction Load Sample
		fonctionLoadSample={arg p, group, buffer, buffer2;
			var f, d, b, b2;
			s.bind{
				buffer.free;
				s.sync;
				buffer2.free;
				s.sync;
				f = SoundFile.new;
				s.sync;
				f.openRead(p);
				s.sync;
				if(f.numChannels == 1, {
					Post << "Loading mono sound ->" << p << Char.nl;
					s.sync;
					b=Buffer.read(s, p, action: {arg buf; Post << "Finished" << Char.nl});
					s.sync;
				}, {
					d = FloatArray.newClear(f.numFrames * 2);
					s.sync;
					f.readData(d);
					s.sync;
					Post << "Loading stereo sound and convert to mono ->" << p << Char.nl;
					s.sync;
					d = d.unlace(2).sum / 2;
					s.sync;
					b = Buffer.loadCollection(s, d, 1, {"Loaded".postln});
					s.sync;
				});
				f.close;
				s.sync;
				b2 = Buffer.alloc(s, f.numFrames, 1, {"Finisch".postln});
				s.sync;
			};
			[b, f, b2] ;// sortie buffer et SoundFile
		};

		//Fonction Load file for analyze
		fonctionLoadFileForAnalyse={arg p;
			var f, d;
			s.bind{
				f = SoundFile.new;
				s.sync;
				f.openRead(p);
				s.sync;
				if(f.numChannels == 1,
					{d= FloatArray.newClear(f.numFrames * 2);
						s.sync;
						f.readData(d);
						s.sync;
						Post << "Loading sound mono for analyze" << p << Char.nl;
						s.sync;
						d = Array.newFrom(d).stutter(2) / 2;
						s.sync;
						bufferFile=Buffer.loadCollection(s, d, 2, action: {arg buf; Post << "Finished" << Char.nl});
						s.sync;
					},
					{Post << "Loading sound stereo for analyze" << p << Char.nl;
						s.sync;
						bufferFile=Buffer.readChannel(s, p, channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
						s.sync;
				});
				f.close;
				s.sync;
				synthFileIn.set(\bufferFile, bufferFile);
				s.sync;
			};
			bufferFile.value;
		};

		// Init numFhzBand
		fonctionInitBand = {arg band;
			bandFHZ = Array.fill(numFhzBand, {arg i; [127 / numFhzBand * i, 127 / numFhzBand * i + (127 / numFhzBand )]}).midicps;
			bandFHZ = bandFHZ.reverse;
			bandFHZ = bandFHZ.add([0, 127].midicps);
			bandFHZ = bandFHZ.reverse;
			// Init Array
			lastTimeBand = [];
			listeDataBand = [];
			(numFhzBand + 1).do({arg i;
				lastTimeBand = lastTimeBand.add(Main.elapsedTime);
				listeDataBand = listeDataBand.add([]);
			});
			for(0, numFhzBand,
				{arg index;
					numberSynth.do({arg synth;
						windowControlGUI.view.children.at((synth * 93 + 21 + index) + (numberSynth * numberMaxStepSequencer + 14)).enabled_(true);
					});
			});
			if(numFhzBand < 12, {
				for(numFhzBand + 1, 12,
					{arg index;
						numberSynth.do({arg synth;
							windowControlGUI.view.children.at((synth * 93 + 21 + index) + (numberSynth * numberMaxStepSequencer + 14)).enabled_(false);
							windowControlGUI.view.children.at((synth * 93 + 21 + index) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(0);
						});
				});
			});
		};

		// Init fhzBand for Synth
		fonctionBand = {arg band, synth;
			var range = [];
			for(0, numFhzBand,
				{arg index;
					if(flagIndexBand.at(synth).at(index) == 1, {
						range = range.add(index);
					});
			});
			rangeNumFhzBand.put(synth, range);
		};

		// Fonction Automation Preset
		fonctionAutomationPreset = {arg flux, flatness;
			var newValue1, newValue2, file, number=lastNumberChoiceConfig, compteur=0, time;
			{
				time = Main.elapsedTime;
				newValue1 = flux.log2.abs;
				newValue2 = flatness.log2.abs;
				if(flagAutomation == 'on' and: {(time - lastTimeAutomation).abs > maxDureeOSC}, {
					if((newValue1 - lastValue1Automation).abs > thresholdAutomation and: {(newValue2 - lastValue2Automation).abs > thresholdAutomation},
						{
							while({number == lastNumberChoiceConfig and: {compteur <= 40}}, {number = rrand(0, foldersToScanPreset.size - 1); compteur = compteur + 1});
							if(number != nil, {
								lastNumberChoiceConfig = number;
								if(File.exists(~pathWekTime ++ foldersToScanPreset.at(number)),
									{file=File(~pathWekTime ++ foldersToScanPreset.at(number),"r");
										fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on'); file.close;
										windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + foldersToScanPreset.at(number);
								}, {"cancelled".postln});
							});
							lastTimeAutomation = time;
					});
				});
				lastValue1Automation = newValue1;
				lastValue2Automation = newValue2;
			}.defer;
		};

		// Run Soft
		s.waitForBoot({

			// OSC Setting
			serverAdresse = s.addr; // Adresse Server -> NetAddr(0.0.0.0, 0)
			masterAppAddr = NetAddr.localAddr;
			slaveAppAddr = NetAddr.localAddr;
			oscStateFlag = 'off';

			ardourOSC = NetAddr("127.0.0.1", 3819);// Ardour's port number

			// Init Fonction OSC
			initOSCresponder = {
				oscHPtempo = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					{windowControlGUI.view.children.at(1).children.at(1).value_(msg.wrapAt(1)); windowControlGUI.view.children.at(1).children.at(3).value_(msg.wrapAt(1))}.defer;
					densityBPM=[msg.wrapAt(1) / 60, msg.wrapAt(1) / 60];
				}, '/HPtempo', masterAppAddr);
				// OSC synchroStart slave
				oscHPstart = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					if(oscStateFlag == 'slave', {
						{startSystem.valueAction = msg.wrapAt(1)}.defer;
					});
				}, '/HPstart', masterAppAddr);
				// OSC synchroRec slave
				oscHPrec = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					if(oscStateFlag == 'slave', {
						{
							if(msg.wrapAt(1) == 'Rec On', {fonctionRecOn.value});// Recording On
							if(msg.wrapAt(1) == 'Rec Off', {fonctionRecOff.value});// Recording Off
							if(msg.wrapAt(1) == 'Rec Pause', {fonctionRecPause.value});// Recording Pause
						}.defer;
					});
				}, '/HPrec', masterAppAddr);
			};
			initOSCresponder.value;

			// MIDI
			MIDIClient.init;
			// Connect first device by default
			MIDIIn.connect(0, 0);
			midiOut = MIDIOut(0).latency = 0.01;
			midiOut.allNotesOff(0);

			// Init SynthDef
			this.initSynthDef;

			// Group
			groupeAudioRec=Group.new(s, \addToTail);
			groupeSynth=Group.new(s, \addToTail);
			groupeLimiter = Group.new(s, \addToTail);

			// Bus Audio In pour FileIn et FileRec
			busAudioIn = Bus.audio(s, 1);

			// Bus OSC Data
			busOSCfreq = Bus.control(s, 1);
			busOSCamp = Bus.control(s, 1);
			busOSCduree = Bus.control(s, 1);
			busOSCtempo = Bus.control(s, 1);
			busOSCflux = Bus.control(s, 1);
			busOSCflatness = Bus.control(s, 1);
			busOSCenergy = Bus.control(s, 1);
			busOSCcentroid = Bus.control(s, 1);

			// Synthesizer
			numberSynth.do({arg synth;
				// Group
				listeGroupAudioRec=listeGroupAudioRec.add(Group.new(groupeAudioRec, \addToTail));
				listeGroupSynth=listeGroupSynth.add(Group.new(groupeSynth, \addToTail));
				listeGroupDolby=listeGroupDolby.add(Group.new(listeGroupSynth.at(synth)), \addToTail);
				listeGroupFX=listeGroupFX.add(Group.new(listeGroupSynth.at(synth)), \addToTail);
				listeGroupFilter=listeGroupFilter.add(Group.new(listeGroupSynth.at(synth)), \addToTail);
				// Bus
				listeBusInFilter=listeBusInFilter.add(Bus.audio(s, 1));
				listeBusInFX=listeBusInFX.add(Bus.audio(s, 1));
				listeBusOutFX=listeBusOutFX.add(Bus.audio(s, 1));
				listeBusInDolby=listeBusInDolby.add(Bus.audio(s, 1));
				// Load Sample
				bufferAndSoundFile = fonctionLoadSample.value(Platform.resourceDir.postln +/+ "sounds/a11wlk01-44_1.aiff", listeGroupSynth.at(synth), nil, nil);
				// Setup Data Sample
				listeBuffer=listeBuffer.add(bufferAndSoundFile.at(0));
				listeSoundFile=listeSoundFile.add(bufferAndSoundFile.at(1));
				listeBufferAudioRec=listeBufferAudioRec.add(bufferAndSoundFile.at(2));
				listeJitterWaveForm = listeJitterWaveForm.add(0.1);
				listeStartPos = listeStartPos.add(0);
				listeLenght = listeLenght.add(listeBuffer.at(synth).numFrames / 3);
				listeActiveJitterWavePos = listeActiveJitterWavePos.add(0);
				listeReverse = listeReverse.add(1);
				listeEnvelopeSynth = listeEnvelopeSynth.add([[0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0],[0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125]]);
				listeActiveAudioRec = listeActiveAudioRec.add(0);
				listeLoop = listeLoop.add(1);
				listeRecLevel = listeRecLevel.add(1);
				listePreLevel = listePreLevel.add(0);
				// Create Synth AudioRec
				synthAudioRec = synthAudioRec.add(Synth.new("AudioRec", [\busIn, listeBusSynth.at(synth), \bufferAudioRec, listeBufferAudioRec.at(synth).bufnum, \recLevel, listeRecLevel.at(synth), \preLevel, listePreLevel.at(synth), \loop, listeLoop.at(synth)], listeGroupAudioRec.at(synth), \addToTail));
				// Synth ByPassFilter
				Synth.new("ByPassFilter", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth)], listeGroupFilter.at(synth), \addToTail);
				// Synth ByPassFX
				Synth.new("ByPassFX", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth)], listeGroupFX.at(synth), \addToTail);
				// Dolby5.1
				Synth.new(typeMasterOut, [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5, \panY, 0.5], listeGroupDolby.at(synth), \addToTail);
			});
			// Create Synth OSC
			synthOSCkeyboard = Synth.newPaused("WekTime Keyboard", [\busIn, busAudioIn], groupeAudioRec, \addToHead);
			synthOSCkeytrack = Synth.newPaused("WekTime Keytrack", [\busIn, busAudioIn], groupeAudioRec, \addToHead);
			synthOSCpitch = Synth.newPaused("WekTime Pitch", [\busIn, busAudioIn], groupeAudioRec, \addToHead);
			synthOSCpitch2 = Synth.newPaused("WekTime Pitch2", [\busIn, busAudioIn], groupeAudioRec, \addToHead);
			synthOSConset = Synth.newPaused("WekTime Onset", [\busIn, busAudioIn], groupeAudioRec, \addToHead);
			synthOSCFFT = Synth.new("WekTime FFT", [\busIn, busAudioIn, \speed, 24], groupeAudioRec, \addToHead);

			// Init FileIn
			fonctionLoadFileForAnalyse.value(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");

			// Create Synth FileIn
			synthFileIn = Synth.newPaused("FileIn", [\bufferFile, bufferFile, \busIn, busAudioIn], groupeAudioRec, \addToHead);

			// Create Synth AudioIn
			synthAudioIn = Synth.new("AudioIn", [\in, 0, \busIn, busAudioIn], groupeAudioRec, \addToHead);

			// Creation Compander + Limiter
			synthLimiter = Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);

			// VST
			~synthVST = Synth.newPaused("VST"+ typeMasterOut, [\xFade, 0.5, \panLo, 0, \panHi, 0, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
			~fxVST = VSTPluginController(~synthVST);

			freqBefore=0; ampBefore=0; dureeBefore=0; lastTime = Main.elapsedTime; lastTimeWekPreset = Main.elapsedTime; lastTimeWekData = Main.elapsedTime;

			// DATA WIKI OUT
			OSCFunc.newMatching({arg msg, time, addr, recvPort;

				var wekOut;

				wekOut = msg[1..];

				{
					if(flagWTD == 'on' and: {(time - lastTimeWekData) > timeWekData}, {
						switch(wekOut[0].clip(0, 3),
							0, {typeSequencer = 'Rand'},
							1, {typeSequencer = 'Seq'; indexSequence = 0},
							2, {typeSequencer = 'WeightS'},
							3, {typeSequencer = 'WeightP'});
						windowControlGUI.view.children.at(0).value_(wekOut[0]);
						//densityBPM = wekOut[1..2].abs.clip(1, 60000) / 1000);
						windowControlGUI.view.children.at(1).children.at(1).value_(wekOut[1].abs.clip(1, 60000));
						windowControlGUI.view.children.at(1).children.at(3).value_(wekOut[2].abs.clip(1, 60000));

						windowControlGUI.view.children.at(1).children.at(2).lo_(wekOut[1].abs.clip(1, 60000) / 1000
						);
						windowControlGUI.view.children.at(1).children.at(2).hi_(wekOut[2].abs.clip(1, 60000) / 1000
						);
						numberStepSequencer = wekOut[3].clip(1, 48);
						windowControlGUI.view.children.at(2).children.at(2).value_(numberStepSequencer);
						ambitusFreq = wekOut[4..5].clip(0, 127);
						windowControlGUI.view.children.at(3).children.at(1).value_(ambitusFreq[0]);
						windowControlGUI.view.children.at(3).children.at(3).value_(ambitusFreq[1]);
						windowControlGUI.view.children.at(3).children.at(2).lo_(ambitusFreq[0] / 127);
						windowControlGUI.view.children.at(3).children.at(2).hi_(ambitusFreq[1] / 127);
						rangeFFT = wekOut[6..7].clip(0, 1);
						windowExternalControlGUI.view.children.at(27).children.at(2).lo_(rangeFFT[0]);
						windowExternalControlGUI.view.children.at(27).children.at(2).hi_(rangeFFT[1]);
						windowExternalControlGUI.view.children.at(27).children.at(1).value = rangeFFT[0];
						windowExternalControlGUI.view.children.at(27).children.at(3).value = rangeFFT[1];
						//Weight Synth
						listeWeightSynth = wekOut[8..11].clip(0, 1);
						4.do({arg i;
							windowControlGUI.view.children.at(i * 93 + 207).children.at(2).value_(listeWeightSynth[i]);
						});

						//synth
						changeChoiceSynthDef.do({arg item, i;// 12 a 15
							if(wekOut[12].clip(0, changeChoiceSynthDef.size - 1).asInteger == i, {
								typeSynthDef.put(0, item);
								windowControlGUI.view.children.at(0 * 93 + 208).value_(i.asInteger);
							});
							if(wekOut[13].clip(0, changeChoiceSynthDef.size - 1).asInteger == i, {
								typeSynthDef.put(1, item);
								windowControlGUI.view.children.at(1 * 93 + 208).value_(i.asInteger);
							});
							if(wekOut[14].clip(0, changeChoiceSynthDef.size - 1).asInteger == i, {
								typeSynthDef.put(2, item);
								windowControlGUI.view.children.at(2 * 93 + 208).value_(i.asInteger);
							});
							if(wekOut[15].clip(0, changeChoiceSynthDef.size - 1).asInteger == i, {
								typeSynthDef.put(3, item);
								windowControlGUI.view.children.at(3 * 93 + 208).value_(i.asInteger);
							});
						});
						// duree synth
						['Seq', 'Pitch', 'Grain'].do({arg item, i;// 16 a 19
							if(wekOut[16].clip(0, 2).asInteger == i, {
								listeFlagDureeSynth.put(0, item;
									windowControlGUI.view.children.at(0 * 93 + 210).value_(i.asInteger);
							)});
							if(wekOut[17].clip(0, 2).asInteger == i, {
								listeFlagDureeSynth.put(1, item);
								windowControlGUI.view.children.at(1 * 93 + 210).value_(i.asInteger);
							});
							if(wekOut[18].clip(0, 2).asInteger == i, {
								listeFlagDureeSynth.put(2, item);
								windowControlGUI.view.children.at(2 * 93 + 210).value_(i.asInteger);
							});
							if(wekOut[19].clip(0, 2).asInteger == i, {
								listeFlagDureeSynth.put(3, item);
								windowControlGUI.view.children.at(3 * 93 + 210).value_(i.asInteger);
							});
						});
						// mode synth 20 a 23
						if(wekOut[20] <= 0.5,
							{modeMIDIOSC.put(0, 'Translate');
								windowControlGUI.view.children.at(0 * 93 + 211).value_(0);
							},
							{modeMIDIOSC.put(0, 'Note');
								windowControlGUI.view.children.at(0 * 93 + 211).value_(1);
						});
						if(wekOut[21] <= 0.5,
							{modeMIDIOSC.put(1, 'Translate');
								windowControlGUI.view.children.at(1 * 93 + 211).value_(0);
							},
							{modeMIDIOSC.put(1, 'Note');
								windowControlGUI.view.children.at(1 * 93 + 211).value_(1);
						});
						if(wekOut[22] <= 0.5,
							{modeMIDIOSC.put(2, 'Translate');
								windowControlGUI.view.children.at(2 * 93 + 211).value_(0);
							},
							{modeMIDIOSC.put(2, 'Note');
								windowControlGUI.view.children.at(2 * 93 + 211).value_(1);
						});
						if(wekOut[23] <= 0.5,
							{modeMIDIOSC.put(3, 'Translate');
								windowControlGUI.view.children.at(3 * 93 + 211).value_(0);
							},
							{modeMIDIOSC.put(3, 'Note');
								windowControlGUI.view.children.at(3 * 93 + 211).value_(1);
						});
						// loop sample 24 a 27
						if(wekOut[24] <= 0.5,
							{loopSample.put(0, 0);
								windowControlGUI.view.children.at(0 * 93 + 212).value_(0);
							},
							{loopSample.put(0, 1);
								windowControlGUI.view.children.at(0 * 93 + 212).value_(1);
						});
						if(wekOut[25] <= 0.5,
							{loopSample.put(1, 0);
								windowControlGUI.view.children.at(1 * 93 + 212).value_(0);
							},
							{loopSample.put(1, 1);
								windowControlGUI.view.children.at(1 * 93 + 212).value_(1);
						});
						if(wekOut[26] <= 0.5,
							{loopSample.put(2, 0);
								windowControlGUI.view.children.at(2 * 93 + 212).value_(0);
							},
							{loopSample.put(2, 1);
								windowControlGUI.view.children.at(2 * 93 + 212).value_(1);
						});
						if(wekOut[27] <= 0.5,
							{loopSample.put(3, 0);
								windowControlGUI.view.children.at(3 * 93 + 212).value_(0);
							},
							{loopSample.put(3, 1);
								windowControlGUI.view.children.at(3 * 93 + 212).value_(1);
						});
						4.do({arg i;
							//Octave (247)
							listeOctave.put(i, wekOut[28+i].clip(-4, 4));//28
							windowControlGUI.view.children.at(i * 93 + 247).children.at(2).valueAction_(wekOut[28+i].clip(-4, 4));
							//Demiton (250)
							listeDemiTon.put(i, wekOut[32+i].clip(-12, 12));//32
							windowControlGUI.view.children.at(i * 93 + 250).children.at(2).valueAction_(wekOut[32+i].clip(-12, 12));
							//Cent (253)
							listeCent.put(i, wekOut[36+i].clip(-100, 100));//36
							windowControlGUI.view.children.at(i * 93 + 253).children.at(2).valueAction_(wekOut[36+i].clip(-100, 100));
							//Filter (260)
							choiceFilter.put(i, wekOut[40+i].clip(0, (listeFilters.size - 1)).asInteger);
							windowControlGUI.view.children.at(i * 93 + 260).value_(wekOut[40+i].clip(0, 40).asInteger);//40 Filter
							//CtrlFilter (264)
							listeCtrl1Filter.put(i, wekOut[44+i].clip(20, 12544));//44
							windowControlGUI.view.children.at(i * 93 + 264).children.at(2).valueAction_(wekOut[44+i].clip(20, 12544));
							listeCtrl2Filter.put(i, wekOut[48+i].clip(0.01, 100) / 100);//48
							windowControlGUI.view.children.at(i * 93 + 267).children.at(2).valueAction_(wekOut[48+i].clip(0.01, 100));
							listeCtrl3Filter.put(i, wekOut[52+i].clip(0.01, 100) / 100);//52
							windowControlGUI.view.children.at(i * 93 + 270).children.at(2).valueAction_(wekOut[52+i].clip(0.01, 100));
							//FX (274)
							choiceFX.put(i, wekOut[56+i].clip(0, (listeFX.size - 1)).asInteger);
							windowControlGUI.view.children.at(i * 93 + 274).value_(wekOut[56+i].clip(0, 7).asInteger);//56 FX
							//CtrlFX (278)
							listeCtrl1FX.put(i, wekOut[60+i].clip(0.01, 100) / 100);//60
							windowControlGUI.view.children.at(i * 93 + 278).children.at(2).valueAction_(wekOut[60+i].clip(0.01, 100));
							listeCtrl2FX.put(i, wekOut[64+i].clip(0.01, 100) / 100);//64
							windowControlGUI.view.children.at(i * 93 + 281).children.at(2).valueAction_(wekOut[64+i].clip(0.01, 100));
							listeCtrl3FX.put(i, wekOut[68+i].clip(0.01, 100) / 100);//68
							windowControlGUI.view.children.at(i * 93 + 284).children.at(2).valueAction_(wekOut[68+i].clip(0.01, 100));
							listeCtrl4FX.put(i, wekOut[72+i].clip(0.01, 100) / 100);//72
							windowControlGUI.view.children.at(i * 93 + 287).children.at(2).valueAction_(wekOut[72+i].clip(0.01, 100));
							listeCtrl5FX.put(i, wekOut[76+i].clip(0.01, 100) / 100);//76
							windowControlGUI.view.children.at(i * 93 + 290).children.at(2).valueAction_(wekOut[76+i].clip(0.01, 100));
						});
						lastTimeWekData = time;
					});
					// Preset 80
					numPreset = wekOut[80].asInteger.clip(1, 40);// Number Preset
					if(flagWTP  == 'on' and: {numPreset != lastNumPreset and: {listeWekPreset.includes(numPreset)} and: {(time - lastTimeWekPreset) > timeWekPreset}},
						// load new preset
						{
							if(File.exists(~pathWekTime ++ "Preset" + numPreset.asInteger.asString ++ ".scd"),
								{
									lastNumPreset = numPreset;
									lastTimeWekPreset = time;
									file=File(~pathWekTime ++ "Preset" + numPreset.asInteger.asString ++ ".scd","r");
									fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on'); file.close;
									windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Preset" + numPreset.asInteger.asString});
					});
				}.defer(0);
			},'/wek/outputs');

			// DATA WEK IN + OSC Data FFT
			OSCFunc.newMatching({arg msg, time, addr, recvPort, centroid=0, energy=0, flux=0, flatness=0, bpm=0;
				var data=[], newRoot, mfcc;
				if(msg[2] == 1,
					{
						centroid = msg.at(3);
						energy = msg.at(4);
						flux = msg.at(5);
						flatness = msg.at(6);
						bpm = msg.at(7);
						// Normalize
						flux = flux * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0);
						flatness = flatness * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0);
						centroid = (centroid / 12544 * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0) * 12544).clip(20, 12544);
						energy = (energy / 12544 * (rangeFFT.at(1) - rangeFFT.at(0)) + rangeFFT.at(0) * 12544).clip(20, 12544);
						if(flagTempo == 1, {tempoOSC = bpm;
							{bpmDisplay.string_(bpm.round(0.01)).stringColor_(Color.red)}.defer},
						{tempoOSC = 1;
							{bpmDisplay.string_("BPM").stringColor_(Color.red)}.defer
						});
						if(autoRoot == 'on', {
							newRoot = energy;
							newRoot = newRoot.cpsoct;
							newRoot = (newRoot.frac * tuning.size + 0.5).floor;
							// Setup GUI Value
							{windowControlGUI.view.children.at(7).children.at(2).valueAction_(newRoot)}.defer;
						});
						// Set Bus Flux
						busOSCtempo.set(bpm);
						busOSCflux.set(flux);
						busOSCflatness.set(flatness);
						busOSCenergy.set(energy);
						busOSCcentroid.set(centroid);
						// Setup Automation Preset
						fonctionAutomationPreset.value(flux, flatness);
					},
					{
						mfcc = msg[3..];
						mfccData = mfcc;// For MidiOn
				});

			}, '/WekTime_FFT_Data', serverAdresse);

			// OSC Data
			OSCFunc.newMatching({arg msg, time, addr, recvPort;
				var freq, octave, oct, ratio, degre, difL, difH, pos=scale.degrees.size - 1, demiTon, cent, amp, duree, indexNumFhzBand, data=[], mfcc, tamponSynth, tamponDurSynth, tamponModSynth;
				if(flagOSC == 1 and: {flagKeyboard == 'off'},
					{
						// Music
						freq=msg.at(3);
						amp=msg.at(4);
						ampMIDIOSC = amp.ampdb;
						duree = (time - lastTime).clip(0.01, maxDureeOSC);

						if(abs(freq.cpsmidi - freqBefore.cpsmidi) >= fhzFilter and: {abs(amp.ampdb - ampBefore.ampdb) >= ampFilter} and: {abs(duree - lastTime) >= durFilter} and: {duree >= durFilter}, {

							// Set Ambitus Freq
							freq = freq.cpsmidi / 127 * (ambitusFreq.at(1) - ambitusFreq.at(0)) + ambitusFreq.at(0);
							// Setup Freq with Scaling and Tuning
							if(flagScaling != 'off', {
								oct = freq / 12;
								ratio = (oct.frac * 12).round(0.1);
								oct = oct.floor;
								pos = scale.degrees.indexOfEqual(ratio);
								if(pos == nil,
									{
										pos = scale.degrees.indexOfGreaterThan(ratio);
										if(pos == nil,
											{
												pos = scale.degrees.last;
											},
											{
												pos = scale.degrees.at(pos);
											}
										);
									},
									{
										pos = scale.degrees.at(pos);
									}
								);
								freq = (oct * 12 + pos).midicps;
								octave = (freq.cpsmidi / 12).floor;
								demiTon = ((freq.cpsmidi / 12).frac * 12 + 0.5).floor;
								cent = 0.5;
							},
							{
								octave = (freq.cpsmidi / 12).floor;
								demiTon = ((freq.cpsmidi / 12).frac * 12 + 0.5).floor;
								cent = freq.frac;
							});

							// Set Bus OSC
							busOSCfreq.set(freq);
							busOSCamp.set(amp);
							busOSCduree.set(duree);

							// Dispatch Band FHZ
							for(1, numFhzBand, {arg i;

								if(freq > bandFHZ.at(i).at(0) and: {freq < bandFHZ.at(i).at(1)}, {
									// Add Data
									if(numberSynth > listeDataBand.at(i).size,
										{
											if(duree < chordDureeOSC and: {listeDataBand.at(i).size < numberSynth},
												{
													listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
													lastTimeBand.put(i, time);
												},
												{
													listeDataBand.put(i, []);
													listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
													lastTimeBand.put(i, time);
											});

										},
										{
											listeDataBand.put(i, []);
											listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
											lastTimeBand.put(i, time);
									});
								},
								{
									if(i <= numFhzBand, {
										if((time - lastTimeBand.at(i)) > maxDureeOSC, {
											listeDataBand.put(i, []);
											lastTimeBand.put(i, time);
										});
									});
								});
							});

							// All Data Band = 0
							if((time - lastTimeBand.at(0)) > maxDureeOSC, {
								listeDataBand.put(0, []);
								listeDataBand.put(0, listeDataBand.at(0).add(([freq.cpsmidi, octave, demiTon, cent])));
								lastTimeBand.put(0, time);
							},
							{
								if(numberSynth > listeDataBand.at(0).size,
									{
										if(duree < chordDureeOSC and: {listeDataBand.at(0).size < numberSynth},
											{
												listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
												lastTimeBand.put(0, time);
											},
											{
												listeDataBand.put(0, []);
												listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
												lastTimeBand.put(0, time);
										});
									},
									{
										listeDataBand.put(0, []);
										listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
								});

							});

							// Setup Sliders
							{
								numberSynth.do({arg synth;
									if(switchOSCfreq.at(synth) == 1 and: {modeMIDIOSC.at(synth) == 'Note'}, {
										// New Band
										indexNumFhzBand = rangeNumFhzBand.at(synth).choose;
										if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
										if(listeDataBand.at(indexNumFhzBand) != [],
											{
												// Octave
												windowControlGUI.view.children.at((synth * 93 + 41) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(1) / 8);
												// DemiTon
												windowControlGUI.view.children.at((synth * 93 + 44) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(2) / 24 + 0.5);
												// Cent
												windowControlGUI.view.children.at((synth * 93 + 47) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(3));
										});
									});
									if(switchOSCamp.at(synth) == 1, {
										// AMP
										windowControlGUI.view.children.at((synth * 93 + 50) + (numberSynth * numberMaxStepSequencer + 14)).children.at(2).valueAction_(ampMIDIOSC);
									});
								});
								// Duree BPM
								if(switchOSCdur.includes(1),
									{densityBPM=[duree.reciprocal, duree.reciprocal]},
									{densityBPM=[windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60]});
							}.defer;
							freqBefore=freq;ampBefore=amp;dureeBefore=duree; lastTime = time;
						});
						timeOSC = Main.elapsedTime;
						// Audio Display (For testing)
						{
							audioDisplay.string_(amp.round(0.01)).stringColor_(Color.red(amp.value, 1));
						}.defer;
						// WEKINATOR
						mfcc = msg[6..];
						//Sender
						sender.sendMsg("/wek/inputs", *mfcc[0..]);

						if(flagStreamMFCC != 'wek',
							{
								// Send control outputs for wekinator 81 data

								tamponSynth = [];
								//synth
								typeSynthDef.do({arg n, s;
									changeChoiceSynthDef.do({arg item, i;
										if(n == item, {tamponSynth = tamponSynth.add(i.asFloat)});// 12 a 15
									});
								});
								tamponDurSynth = [];
								// duree synth
								listeFlagDureeSynth.do({arg n, s;
									['Seq', 'Pitch', 'Grain'].do({arg item, i;
										if(n == item, {tamponDurSynth = tamponDurSynth.add(i.asFloat)});// 16 19
									});
								});
								tamponModSynth = [];
								// mode synth
								modeMIDIOSC.do({arg n, s;
									changeChoiceMIDI.do({arg item, i;
										if(n == item, {tamponModSynth = tamponModSynth.add(i.asFloat)});// 20 23
									});
								});

								data = [
									numSequencer,//0
									wekBPM[0],
									wekBPM[1],
									numberStepSequencer,
									ambitusFreq[0],
									ambitusFreq[1],
									rangeFFT[0],
									rangeFFT[1],
									/*//Weight Synth
									listeWeightSynth.do({arg weight, i;//8 11
									data = data.add(weight);
									});*/
									listeWeightSynth[0],
									listeWeightSynth[1],
									listeWeightSynth[2],
									listeWeightSynth[3],
									/*//synth
									typeSynthDef.do({arg n, s;
									changeChoiceSynthDef.do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 12 a 15
									});
									});*/
									tamponSynth[0],
									tamponSynth[1],
									tamponSynth[2],
									tamponSynth[3],
									/*// duree synth
									listeFlagDureeSynth.do({arg n, s;
									['Seq', 'Pitch', 'Grain'].do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 16 19
									});
									});*/
									tamponDurSynth[0],
									tamponDurSynth[1],
									tamponDurSynth[2],
									tamponDurSynth[3],
									/*// mode synth
									modeMIDIOSC.do({arg n, s;
									changeChoiceMIDI.do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 20 23
									});
									});*/
									tamponModSynth[0],
									tamponModSynth[1],
									tamponModSynth[2],
									tamponModSynth[3],
									/*// loop sample
									loopSample.do({arg loop, i;
									data = data.add(loop);//24 27
									});*/
									loopSample[0],
									loopSample[1],
									loopSample[2],
									loopSample[3],
									/*//Octave
									listeOctave.do({arg oct, i;//28 31
									data = data.add(oct);
									});*/
									listeOctave[0],
									listeOctave[1],
									listeOctave[2],
									listeOctave[3],
									/*//SemiTone
									listeDemiTon.do({arg demi, i;//32 35
									data = data.add(demi);
									});*/
									listeDemiTon[0],
									listeDemiTon[1],
									listeDemiTon[2],
									listeDemiTon[3],
									/*//Cent
									listeCent.do({arg cent, i;//36 39
									data = data.add(cent);
									});*/
									listeCent[0],
									listeCent[1],
									listeCent[2],
									listeCent[3],
									/*// Filter
									choiceFilter.do({arg filt, i;//40 43
									tampon = tampon.add(filt * (listeFilters.size - 1));
									});*/
									choiceFilter[0] * (listeFilters.size - 1),
									choiceFilter[1] * (listeFilters.size - 1),
									choiceFilter[2] * (listeFilters.size - 1),
									choiceFilter[3] * (listeFilters.size - 1),
									/*//Ctrl filter
									listeCtrl1Filter.do({arg ctrl, i;//44 47
									data = data.add(ctrl);
									});*/
									listeCtrl1Filter[0],
									listeCtrl1Filter[1],
									listeCtrl1Filter[2],
									listeCtrl1Filter[3],
									/*listeCtrl2Filter.do({arg ctrl, i;//48 51
									data = data.add(ctrl * 100);
									});*/
									listeCtrl2Filter[0] * 100,
									listeCtrl2Filter[1] * 100,
									listeCtrl2Filter[2] * 100,
									listeCtrl2Filter[3] * 100,
									/*listeCtrl3Filter.do({arg ctrl, i;//52 55
									data = data.add(ctrl * 100);
									});*/
									listeCtrl3Filter[0] * 100,
									listeCtrl3Filter[1] * 100,
									listeCtrl3Filter[2] * 100,
									listeCtrl3Filter[3] * 100,
									/*//FX
									choiceFX.do({arg fx, i;//56 59
									data = data.add(fx * (listeFX.size - 1));
									});*/
									choiceFX[0] * (listeFX.size - 1),
									choiceFX[1] * (listeFX.size - 1),
									choiceFX[2] * (listeFX.size - 1),
									choiceFX[3] * (listeFX.size - 1),
									/*// Ctrl FX
									listeCtrl1FX.do({arg ctrl, i;//60 63
									data = data.add(ctrl * 100);
									});*/
									listeCtrl1FX[0] * 100,
									listeCtrl1FX[1] * 100,
									listeCtrl1FX[2] * 100,
									listeCtrl1FX[3] * 100,
									/*listeCtrl2FX.do({arg ctrl, i;//64 67
									data = data.add(ctrl * 100);
									});*/
									listeCtrl2FX[0] * 100,
									listeCtrl2FX[1] * 100,
									listeCtrl2FX[2] * 100,
									listeCtrl2FX[3] * 100,
									/*listeCtrl3FX.do({arg ctrl, i;//68 71
									data = data.add(ctrl * 100);
									});*/
									listeCtrl3FX[0] * 100,
									listeCtrl3FX[1] * 100,
									listeCtrl3FX[2] * 100,
									listeCtrl4FX[3] * 100,
									/*listeCtrl4FX.do({arg ctrl, i;//72 75
									data = data.add(ctrl * 100);
									});*/
									listeCtrl4FX[0] * 100,
									listeCtrl4FX[1] * 100,
									listeCtrl4FX[2] * 100,
									listeCtrl4FX[3] * 100,
									/*listeCtrl5FX.do({arg ctrl, i;//76 79
									data = data.add(ctrl * 100);
									});*/
									listeCtrl5FX[0] * 100,
									listeCtrl5FX[1] * 100,
									listeCtrl5FX[2] * 100,
									listeCtrl5FX[3] * 100,
									// preset
									numPreset.asFloat,//80
								];

								// Sender
								sender.sendMsg("/wekinator/control/outputs", *data[0..]);
						});
				}, {nil});
			}, '/WekTime_OSC_Data', serverAdresse);

			// OSC Data
			OSCFunc.newMatching({arg msg, time, addr, recvPort;
				var freq, octave, oct, ratio, degre, difL, difH, pos=scale.degrees.size - 1, demiTon, cent, amp, duree, indexNumFhzBand, data, mfcc, tamponSynth, tamponDurSynth, tamponModSynth;
				if(flagKeyboard == 'on',
					{
						// Music
						freq=msg.at(3);
						amp=msg.at(4);
						ampMIDIOSC = amp.ampdb;
						duree = msg.at(5).clip(0.01, maxDureeOSC);

						// Set Ambitus Freq
						freq = freq.cpsmidi / 127 * (ambitusFreq.at(1) - ambitusFreq.at(0)) + ambitusFreq.at(0);
						// Setup Freq with Scaling and Tuning
						if(flagScaling != 'off', {
							oct = freq / 12;
							ratio = (oct.frac * 12).round(0.1);
							oct = oct.floor;
							pos = scale.degrees.indexOfEqual(ratio);
							if(pos == nil,
								{
									pos = scale.degrees.indexOfGreaterThan(ratio);
									if(pos == nil,
										{
											pos = scale.degrees.last;
										},
										{
											pos = scale.degrees.at(pos);
										}
									);
								},
								{
									pos = scale.degrees.at(pos);
								}
							);
							freq = (oct * 12 + pos).midicps;
						});

						octave = (freq.cpsmidi / 12).floor;
						demiTon = ((freq.cpsmidi / 12).frac * 12 + 0.5).floor;
						cent = 0.5;

						// Set Bus OSC
						busOSCfreq.set(freq);
						busOSCamp.set(amp);
						busOSCduree.set(duree);

						// Dispatch Band FHZ
						for(1, numFhzBand, {arg i;

							if(freq > bandFHZ.at(i).at(0) and: {freq < bandFHZ.at(i).at(1)}, {
								// Add Data
								if(numberSynth > listeDataBand.at(i).size,
									{
										if(duree < chordDureeOSC and: {listeDataBand.at(i).size < numberSynth},
											{
												listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
												lastTimeBand.put(i, time);
											},
											{
												listeDataBand.put(i, []);
												listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
												lastTimeBand.put(i, time);
										});

									},
									{
										listeDataBand.put(i, []);
										listeDataBand.put(i, listeDataBand.at(i).add([freq.cpsmidi, octave, demiTon, cent]));
										lastTimeBand.put(i, time);
								});
							},
							{
								if(i <= numFhzBand, {
									if((time - lastTimeBand.at(i)) > maxDureeOSC, {
										listeDataBand.put(i, []);
										lastTimeBand.put(i, time);
									});
								});
							});
						});

						// All Data Band = 0
						if((time - lastTimeBand.at(0)) > maxDureeOSC, {
							listeDataBand.put(0, []);
							listeDataBand.put(0, listeDataBand.at(0).add(([freq.cpsmidi, octave, demiTon, cent])));
							lastTimeBand.put(0, time);
						},
						{
							if(numberSynth > listeDataBand.at(0).size,
								{
									if(duree < chordDureeOSC and: {listeDataBand.at(0).size < numberSynth},
										{
											listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
											lastTimeBand.put(0, time);
										},
										{
											listeDataBand.put(0, []);
											listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
											lastTimeBand.put(0, time);
									});
								},
								{
									listeDataBand.put(0, []);
									listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
							});

						});

						// Setup Sliders
						{
							numberSynth.do({arg synth;
								if(switchOSCfreq.at(synth) == 1 and: {modeMIDIOSC.at(synth) == 'Note'}, {
									// New Band
									indexNumFhzBand = rangeNumFhzBand.at(synth).choose;
									if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
									if(listeDataBand.at(indexNumFhzBand) != [],
										{
											// Octave
											windowControlGUI.view.children.at((synth * 93 + 41) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(1) / 8);
											// DemiTon
											windowControlGUI.view.children.at((synth * 93 + 44) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(2) / 24 + 0.5);
											// Cent
											windowControlGUI.view.children.at((synth * 93 + 47) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(3));
									});
								});
								if(switchOSCamp.at(synth) == 1, {
									// AMP
									windowControlGUI.view.children.at((synth * 93 + 50) + (numberSynth * numberMaxStepSequencer + 14)).children.at(2).valueAction_(ampMIDIOSC);
								});
							});
							// Duree BPM
							if(switchOSCdur.includes(1), {densityBPM=[duree.reciprocal, duree.reciprocal]}, {densityBPM=[windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60]});
						}.defer;
						//timeOSC = Main.elapsedTime;
						// WEKINATOR
						sender.sendMsg("/wek/inputs", *mfccData[0..]);
						if(flagStreamMFCC != 'wek',
							{
								// Send control outputs for wekinator 81 data

								tamponSynth = [];
								//synth
								typeSynthDef.do({arg n, s;
									changeChoiceSynthDef.do({arg item, i;
										if(n == item, {tamponSynth = tamponSynth.add(i.asFloat)});// 12 a 15
									});
								});
								tamponDurSynth = [];
								// duree synth
								listeFlagDureeSynth.do({arg n, s;
									['Seq', 'Pitch', 'Grain'].do({arg item, i;
										if(n == item, {tamponDurSynth = tamponDurSynth.add(i.asFloat)});// 16 19
									});
								});
								tamponModSynth = [];
								// mode synth
								modeMIDIOSC.do({arg n, s;
									changeChoiceMIDI.do({arg item, i;
										if(n == item, {tamponModSynth = tamponModSynth.add(i.asFloat)});// 20 23
									});
								});

								data = [
									numSequencer,//0
									wekBPM[0],
									wekBPM[1],
									numberStepSequencer,
									ambitusFreq[0],
									ambitusFreq[1],
									rangeFFT[0],
									rangeFFT[1],
									/*//Weight Synth
									listeWeightSynth.do({arg weight, i;//8 11
									data = data.add(weight);
									});*/
									listeWeightSynth[0],
									listeWeightSynth[1],
									listeWeightSynth[2],
									listeWeightSynth[3],
									/*//synth
									typeSynthDef.do({arg n, s;
									changeChoiceSynthDef.do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 12 a 15
									});
									});*/
									tamponSynth[0],
									tamponSynth[1],
									tamponSynth[2],
									tamponSynth[3],
									/*// duree synth
									listeFlagDureeSynth.do({arg n, s;
									['Seq', 'Pitch', 'Grain'].do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 16 19
									});
									});*/
									tamponDurSynth[0],
									tamponDurSynth[1],
									tamponDurSynth[2],
									tamponDurSynth[3],
									/*// mode synth
									modeMIDIOSC.do({arg n, s;
									changeChoiceMIDI.do({arg item, i;
									if(n == item, {tampon = tampon.add(i.asFloat)});// 20 23
									});
									});*/
									tamponModSynth[0],
									tamponModSynth[1],
									tamponModSynth[2],
									tamponModSynth[3],
									/*// loop sample
									loopSample.do({arg loop, i;
									data = data.add(loop);//24 27
									});*/
									loopSample[0],
									loopSample[1],
									loopSample[2],
									loopSample[3],
									/*//Octave
									listeOctave.do({arg oct, i;//28 31
									data = data.add(oct);
									});*/
									listeOctave[0],
									listeOctave[1],
									listeOctave[2],
									listeOctave[3],
									/*//SemiTone
									listeDemiTon.do({arg demi, i;//32 35
									data = data.add(demi);
									});*/
									listeDemiTon[0],
									listeDemiTon[1],
									listeDemiTon[2],
									listeDemiTon[3],
									/*//Cent
									listeCent.do({arg cent, i;//36 39
									data = data.add(cent);
									});*/
									listeCent[0],
									listeCent[1],
									listeCent[2],
									listeCent[3],
									/*// Filter
									choiceFilter.do({arg filt, i;//40 43
									tampon = tampon.add(filt * (listeFilters.size - 1));
									});*/
									choiceFilter[0] * (listeFilters.size - 1),
									choiceFilter[1] * (listeFilters.size - 1),
									choiceFilter[2] * (listeFilters.size - 1),
									choiceFilter[3] * (listeFilters.size - 1),
									/*//Ctrl filter
									listeCtrl1Filter.do({arg ctrl, i;//44 47
									data = data.add(ctrl);
									});*/
									listeCtrl1Filter[0],
									listeCtrl1Filter[1],
									listeCtrl1Filter[2],
									listeCtrl1Filter[3],
									/*listeCtrl2Filter.do({arg ctrl, i;//48 51
									data = data.add(ctrl * 100);
									});*/
									listeCtrl2Filter[0] * 100,
									listeCtrl2Filter[1] * 100,
									listeCtrl2Filter[2] * 100,
									listeCtrl2Filter[3] * 100,
									/*listeCtrl3Filter.do({arg ctrl, i;//52 55
									data = data.add(ctrl * 100);
									});*/
									listeCtrl3Filter[0] * 100,
									listeCtrl3Filter[1] * 100,
									listeCtrl3Filter[2] * 100,
									listeCtrl3Filter[3] * 100,
									/*//FX
									choiceFX.do({arg fx, i;//56 59
									data = data.add(fx * (listeFX.size - 1));
									});*/
									choiceFX[0] * (listeFX.size - 1),
									choiceFX[1] * (listeFX.size - 1),
									choiceFX[2] * (listeFX.size - 1),
									choiceFX[3] * (listeFX.size - 1),
									/*// Ctrl FX
									listeCtrl1FX.do({arg ctrl, i;//60 63
									data = data.add(ctrl * 100);
									});*/
									listeCtrl1FX[0] * 100,
									listeCtrl1FX[1] * 100,
									listeCtrl1FX[2] * 100,
									listeCtrl1FX[3] * 100,
									/*listeCtrl2FX.do({arg ctrl, i;//64 67
									data = data.add(ctrl * 100);
									});*/
									listeCtrl2FX[0] * 100,
									listeCtrl2FX[1] * 100,
									listeCtrl2FX[2] * 100,
									listeCtrl2FX[3] * 100,
									/*listeCtrl3FX.do({arg ctrl, i;//68 71
									data = data.add(ctrl * 100);
									});*/
									listeCtrl3FX[0] * 100,
									listeCtrl3FX[1] * 100,
									listeCtrl3FX[2] * 100,
									listeCtrl4FX[3] * 100,
									/*listeCtrl4FX.do({arg ctrl, i;//72 75
									data = data.add(ctrl * 100);
									});*/
									listeCtrl4FX[0] * 100,
									listeCtrl4FX[1] * 100,
									listeCtrl4FX[2] * 100,
									listeCtrl4FX[3] * 100,
									/*listeCtrl5FX.do({arg ctrl, i;//76 79
									data = data.add(ctrl * 100);
									});*/
									listeCtrl5FX[0] * 100,
									listeCtrl5FX[1] * 100,
									listeCtrl5FX[2] * 100,
									listeCtrl5FX[3] * 100,
									// preset
									numPreset.asFloat,//80
								];

								// Sender
								sender.sendMsg("/wekinator/control/outputs", *data[0..]);
						});
				}, {nil});
			}, '/WekTime_Keyboard_Data', serverAdresse);

			// Setup MIDI Responder
			// NoteOn
			MIDIdef.noteOn(\midiNoteOn, {arg amp, freq, canal, src;
				var octave, oct, ratio, degre, difL, difH, pos=scale.degrees.size - 1, demiTon, cent, duree, indexNumFhzBand, time = Main.elapsedTime, data,  tamponSynth, tamponDurSynth, tamponModSynth;
				if(canal == canalMIDI and: {flagMIDI == 1}, {

					amp = amp / 127;
					ampMIDIOSC = amp.ampdb;
					duree = (time - lastDureeMIDI).clip(0.01, maxDureeOSC);

					// Set Ambitus Freq
					freq = freq / 127 * (ambitusFreq.at(1) - ambitusFreq.at(0)) + ambitusFreq.at(0);
					octave = (freq / 12).floor;
					demiTon = ((freq / 12).frac * 12 + 0.5).floor;
					cent = 0.5;

					// Set Bus OSC
					busOSCfreq.set(freq);
					busOSCamp.set(amp);
					busOSCduree.set(duree);

					// Dispatch Band FHZ
					for(1, numFhzBand, {arg i;

						if(freq.midicps > bandFHZ.at(i).at(0) and: {freq.midicps < bandFHZ.at(i).at(1)}, {
							// Add Data
							if(numberSynth > listeDataBand.at(i).size,
								{
									if(duree < chordDureeOSC and: {listeDataBand.at(i).size < numberSynth},
										{
											listeDataBand.put(i, listeDataBand.at(i).add([freq, octave, demiTon, cent]));
											lastTimeBand.put(i, time);
										},
										{
											listeDataBand.put(i, []);
											listeDataBand.put(i, listeDataBand.at(i).add([freq, octave, demiTon, cent]));
											lastTimeBand.put(i, time);
									});

								},
								{
									listeDataBand.put(i, []);
									listeDataBand.put(i, listeDataBand.at(i).add([freq, octave, demiTon, cent]));
									lastTimeBand.put(i, time);
							});
						},
						{
							if(i <= numFhzBand, {
								if((time - lastTimeBand.at(i)) > maxDureeOSC, {
									listeDataBand.put(i, []);
									lastTimeBand.put(i, time);
								});
							});
						});
					});

					// All Data Band = 0
					if((time - lastTimeBand.at(0)) > maxDureeOSC, {
						listeDataBand.put(0, []);
						listeDataBand.put(0, listeDataBand.at(0).add(([freq.cpsmidi, octave, demiTon, cent])));
						lastTimeBand.put(0, time);
					},
					{
						if(numberSynth > listeDataBand.at(0).size,
							{
								if(duree < chordDureeOSC and: {listeDataBand.at(0).size < numberSynth},
									{
										listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
										lastTimeBand.put(0, time);
									},
									{
										listeDataBand.put(0, []);
										listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
										lastTimeBand.put(0, time);
								});
							},
							{
								listeDataBand.put(0, []);
								listeDataBand.put(0, listeDataBand.at(0).add([freq.cpsmidi, octave, demiTon, cent]));
						});

					});

					// Setup Sliders
					{
						numberSynth.do({arg synth;
							if(switchOSCfreq.at(synth) == 1 and: {modeMIDIOSC.at(synth) == 'Note'}, {
								// New Band
								indexNumFhzBand = rangeNumFhzBand.at(synth).choose;
								if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
								if(listeDataBand.at(indexNumFhzBand) != [],
									{
										// Octave
										windowControlGUI.view.children.at((synth * 93 + 41) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(1) / 8);
										// DemiTon
										windowControlGUI.view.children.at((synth * 93 + 44) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(2) / 24 + 0.5);
										// Cent
										windowControlGUI.view.children.at((synth * 93 + 47) + (numberSynth * numberMaxStepSequencer + 14)).children.at(1).valueAction_(listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(3));
								});
							});
							if(switchOSCamp.at(synth) == 1, {
								// AMP
								windowControlGUI.view.children.at((synth * 93 + 50) + (numberSynth * numberMaxStepSequencer + 14)).children.at(2).valueAction_(ampMIDIOSC);
							});
						});
						// Duree BPM
						if(switchOSCdur.includes(1), {densityBPM=[duree.reciprocal, duree.reciprocal]}, {densityBPM=[windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60]});
					}.defer;
					lastDureeMIDI = time;
					// WEKINATOR
					//Sender
					sender.sendMsg("/wek/inputs", *mfccData[0..]);
					if(flagStreamMFCC != 'wek',
						{
							// Send control outputs for wekinator 81 data

							tamponSynth = [];
							//synth
							typeSynthDef.do({arg n, s;
								changeChoiceSynthDef.do({arg item, i;
									if(n == item, {tamponSynth = tamponSynth.add(i.asFloat)});// 12 a 15
								});
							});
							tamponDurSynth = [];
							// duree synth
							listeFlagDureeSynth.do({arg n, s;
								['Seq', 'Pitch', 'Grain'].do({arg item, i;
									if(n == item, {tamponDurSynth = tamponDurSynth.add(i.asFloat)});// 16 19
								});
							});
							tamponModSynth = [];
							// mode synth
							modeMIDIOSC.do({arg n, s;
								changeChoiceMIDI.do({arg item, i;
									if(n == item, {tamponModSynth = tamponModSynth.add(i.asFloat)});// 20 23
								});
							});

							data = [
								numSequencer,//0
								wekBPM[0],
								wekBPM[1],
								numberStepSequencer,
								ambitusFreq[0],
								ambitusFreq[1],
								rangeFFT[0],
								rangeFFT[1],
								/*//Weight Synth
								listeWeightSynth.do({arg weight, i;//8 11
								data = data.add(weight);
								});*/
								listeWeightSynth[0],
								listeWeightSynth[1],
								listeWeightSynth[2],
								listeWeightSynth[3],
								/*//synth
								typeSynthDef.do({arg n, s;
								changeChoiceSynthDef.do({arg item, i;
								if(n == item, {tampon = tampon.add(i.asFloat)});// 12 a 15
								});
								});*/
								tamponSynth[0],
								tamponSynth[1],
								tamponSynth[2],
								tamponSynth[3],
								/*// duree synth
								listeFlagDureeSynth.do({arg n, s;
								['Seq', 'Pitch', 'Grain'].do({arg item, i;
								if(n == item, {tampon = tampon.add(i.asFloat)});// 16 19
								});
								});*/
								tamponDurSynth[0],
								tamponDurSynth[1],
								tamponDurSynth[2],
								tamponDurSynth[3],
								/*// mode synth
								modeMIDIOSC.do({arg n, s;
								changeChoiceMIDI.do({arg item, i;
								if(n == item, {tampon = tampon.add(i.asFloat)});// 20 23
								});
								});*/
								tamponModSynth[0],
								tamponModSynth[1],
								tamponModSynth[2],
								tamponModSynth[3],
								/*// loop sample
								loopSample.do({arg loop, i;
								data = data.add(loop);//24 27
								});*/
								loopSample[0],
								loopSample[1],
								loopSample[2],
								loopSample[3],
								/*//Octave
								listeOctave.do({arg oct, i;//28 31
								data = data.add(oct);
								});*/
								listeOctave[0],
								listeOctave[1],
								listeOctave[2],
								listeOctave[3],
								/*//SemiTone
								listeDemiTon.do({arg demi, i;//32 35
								data = data.add(demi);
								});*/
								listeDemiTon[0],
								listeDemiTon[1],
								listeDemiTon[2],
								listeDemiTon[3],
								/*//Cent
								listeCent.do({arg cent, i;//36 39
								data = data.add(cent);
								});*/
								listeCent[0],
								listeCent[1],
								listeCent[2],
								listeCent[3],
								/*// Filter
								choiceFilter.do({arg filt, i;//40 43
								tampon = tampon.add(filt * (listeFilters.size - 1));
								});*/
								choiceFilter[0] * (listeFilters.size - 1),
								choiceFilter[1] * (listeFilters.size - 1),
								choiceFilter[2] * (listeFilters.size - 1),
								choiceFilter[3] * (listeFilters.size - 1),
								/*//Ctrl filter
								listeCtrl1Filter.do({arg ctrl, i;//44 47
								data = data.add(ctrl);
								});*/
								listeCtrl1Filter[0],
								listeCtrl1Filter[1],
								listeCtrl1Filter[2],
								listeCtrl1Filter[3],
								/*listeCtrl2Filter.do({arg ctrl, i;//48 51
								data = data.add(ctrl * 100);
								});*/
								listeCtrl2Filter[0] * 100,
								listeCtrl2Filter[1] * 100,
								listeCtrl2Filter[2] * 100,
								listeCtrl2Filter[3] * 100,
								/*listeCtrl3Filter.do({arg ctrl, i;//52 55
								data = data.add(ctrl * 100);
								});*/
								listeCtrl3Filter[0] * 100,
								listeCtrl3Filter[1] * 100,
								listeCtrl3Filter[2] * 100,
								listeCtrl3Filter[3] * 100,
								/*//FX
								choiceFX.do({arg fx, i;//56 59
								data = data.add(fx * (listeFX.size - 1));
								});*/
								choiceFX[0] * (listeFX.size - 1),
								choiceFX[1] * (listeFX.size - 1),
								choiceFX[2] * (listeFX.size - 1),
								choiceFX[3] * (listeFX.size - 1),
								/*// Ctrl FX
								listeCtrl1FX.do({arg ctrl, i;//60 63
								data = data.add(ctrl * 100);
								});*/
								listeCtrl1FX[0] * 100,
								listeCtrl1FX[1] * 100,
								listeCtrl1FX[2] * 100,
								listeCtrl1FX[3] * 100,
								/*listeCtrl2FX.do({arg ctrl, i;//64 67
								data = data.add(ctrl * 100);
								});*/
								listeCtrl2FX[0] * 100,
								listeCtrl2FX[1] * 100,
								listeCtrl2FX[2] * 100,
								listeCtrl2FX[3] * 100,
								/*listeCtrl3FX.do({arg ctrl, i;//68 71
								data = data.add(ctrl * 100);
								});*/
								listeCtrl3FX[0] * 100,
								listeCtrl3FX[1] * 100,
								listeCtrl3FX[2] * 100,
								listeCtrl4FX[3] * 100,
								/*listeCtrl4FX.do({arg ctrl, i;//72 75
								data = data.add(ctrl * 100);
								});*/
								listeCtrl4FX[0] * 100,
								listeCtrl4FX[1] * 100,
								listeCtrl4FX[2] * 100,
								listeCtrl4FX[3] * 100,
								/*listeCtrl5FX.do({arg ctrl, i;//76 79
								data = data.add(ctrl * 100);
								});*/
								listeCtrl5FX[0] * 100,
								listeCtrl5FX[1] * 100,
								listeCtrl5FX[2] * 100,
								listeCtrl5FX[3] * 100,
								// preset
								numPreset.asFloat,//80
							];

							// Sender
							sender.sendMsg("/wekinator/control/outputs", *data[0..]);
					});
				});
			}, (0..127), nil);

			// NoteOff
			MIDIdef.noteOff(\midiNoteOff, {arg amp, freq, canal, src;

				if(canal == canalMIDI and: {flagMIDI == 1}, {
					for(1, numFhzBand, {arg i;
						if(freq.midicps > bandFHZ.at(i).at(0) and: {freq.midicps < bandFHZ.at(i).at(1)}, {
							listeDataBand.put(i, []);
							lastTimeBand.put(i, Main.elapsedTime);
						});
					});
					listeDataBand.put(0, []);
					lastTimeBand.put(0, Main.elapsedTime);
				});
			}, (0..127), nil);

			// PitchBend
			MIDIdef.bend(\midiBend, {arg bend, canal, src;
				if(canal == canalMIDI, {
					bendMIDI = bend - 8192 / 672;
				});
			});

			// Sequencer
			sequencer = Tdef("Sequencer", {
				var octave, demiTon, cent, oct, ratio, degre, difL, difH, pos=scale.degrees.size - 1, freq, freqRate, rate, reverse, amp, duree, dureeSynth, startPos, endPos, buffer, listeDeferBuffer=[], listeJitterStartPos=[], envLevel, envDuree, volumeFilter, ctrl1Filter, ctrl2Filter, ctrl3Filter, volumeFX, ctrl1FX, ctrl2FX, ctrl3FX, ctrl4FX, ctrl5FX, panX, panY, out, synth, flagSolo, listeSynthActif, midiOscFreq, varNumSynth, varWeightSynth, indexNumFhzBand, freqToMidi, freqSynth, weight;
				// Setup Variable Liste StartPos Synth
				numberSynth.do({arg synth; listeJitterStartPos = listeJitterStartPos.add(0); listeDeferBuffer = listeDeferBuffer.add(nil)});
				// Loop Sequencer
				loop({
					flagSolo = 'off'; listeSynthActif =[]; varNumSynth=[]; varWeightSynth=[];
					// Verify timing OSC
					if((Main.elapsedTime - timeOSC) >= maxDureeOSC and: {flagOSC == 1}, {
						// Init Array
						lastTimeBand = [];
						listeDataBand = [];
						(numFhzBand + 1).do({arg i;
							lastTimeBand = lastTimeBand.add(Main.elapsedTime);
							listeDataBand = listeDataBand.add([]);
						});
					});
					// New Duree
					duree = rrand(densityBPM.at(0), densityBPM.at(1)).reciprocal;
					// Setup Mute Solo
					numberSynth.do({arg synth; if(listeSoloSynth.at(synth).value == 1, {flagSolo = 'on'})});
					// Set active synth for choice playing
					numberSynth.do({arg synth; if(listeMuteSynth.at(synth).value != 1 and: {flagSolo != 'on' or:{listeSoloSynth.at(synth).value == 1}}, {varNumSynth=varNumSynth.add(synth); varWeightSynth = varWeightSynth.add(listeWeightSynth.at(synth))},{varWeightSynth = varWeightSynth.add(0)})});
					// Choice Sequencer Synth
					switch(typeSequencer,
						'Rand', {listeSynthActif = listeSynthActif.add(varNumSynth.wrapAt(rrand(0, numberSynth - 1)))},
						'Seq', {numberSynth.do({arg synth; if(listeSynthStepSequencer.at(synth).wrapAt(indexSequence) == 1, {listeSynthActif = listeSynthActif.add(synth)})}); indexSequence = indexSequence + 1;if(indexSequence >= numberStepSequencer, {indexSequence = 0})},
						'WeightS', {
							listeSynthActif = listeSynthActif.add(varWeightSynth.normalizeSum.windex);
						},
						'WeightP', {
							weight = rrand(0.0, 1.0);
							numberSynth.do({arg synth; if(listeWeightSynth.at(synth) >= weight, {listeSynthActif = listeSynthActif.add(synth)})});
					});
					// Check MIDI
					if(listeSynthActif != [nil],
						{
							// Setup MusicData + Filter + FX + Panner + Update Waveform
							listeSynthActif.do({arg synth;
								// Set Trigger Recbuf
								if(listeLoop.at(synth).value == 2, {
									s.bind(
										synthAudioRec.at(synth).set(\trigger, 1);
										s.sync;
										synthAudioRec.at(synth).set(\trigger, 0);
										s.sync;
									)
								});
								if(listeBuffer.at(synth).numFrames != nil and: {synth >= 0} and: {synth < numberSynth} and: {flagSolo != 'on' or:{listeSoloSynth.at(synth).value == 1} and:{listeMuteSynth.at(synth).value != 1}}, {
									// Choose FHZ Band
									// New Band
									indexNumFhzBand = rangeNumFhzBand.at(synth).choose;
									if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
									if(listeActiveAudioRec.at(synth) == 0, {buffer = listeBuffer.at(synth)}, {buffer = listeBufferAudioRec.at(synth)});
									// Frame Grain
									startPos =  listeStartPos.at(synth) / buffer.numFrames;
									endPos =  listeLenght.at(synth) / buffer.numFrames;
									if(listeActiveJitterWavePos.at(synth) == 1, {
										startPos = startPos + (0.5 * listeJitterWaveForm.at(synth) * rrand(-1.0, 1.0));
										startPos = startPos.clip(0.0, 1.0);
										endPos = startPos + endPos;
										endPos = endPos.clip(0.0, 1.0);
									}, {
										startPos = startPos;
										endPos = startPos + endPos;
										endPos = endPos.clip(0.0, 1.0);
									});
									listeJitterStartPos.put(synth, startPos);
									listeDeferBuffer.put(synth, buffer);
									// Update Display Grain Waveform
									{
										if(listeActiveJitterWavePos.at(synth) == 1, {
											listeGUIsoundFile.at(synth).setSelection(0, [listeJitterStartPos.at(synth) * listeDeferBuffer.at(synth).numFrames, listeLenght.at(synth)])});
									}.defer;
									// Envelope
									envLevel = listeEnvelopeSynth.at(synth).at(0);
									envDuree = listeEnvelopeSynth.at(synth).at(1);
									// Octave
									if(listeActiveJitterOctave.at(synth) == 1, {
										octave = listeOctave.at(synth)  + ((5 * listeJitterOctave.at(synth) * rrand(-1.0, 1.0)) + 0.5).floor;
										octave = octave.clip(-5, 5);
									}, {octave = listeOctave.at(synth)});
									// DemiTon
									if(listeActiveJitterDemiTon.at(synth) == 1, {
										demiTon = listeDemiTon.at(synth) + ((12 * listeJitterDemiTon.at(synth) * rrand(-1.0, 1.0)) + 0.5).floor;
										demiTon= demiTon.clip(-12, 12);
									}, {demiTon = listeDemiTon.at(synth)});
									// Cent
									if(listeActiveJitterCent.at(synth) == 1, {
										cent = listeCent.at(synth) + ((100 * listeJitterCent.at(synth) * rrand(-1.0, 1.0)) + 0.5).floor;
										cent = cent.clip(-100, 100);
									}, {cent = listeCent.at(synth)});
									// Amp
									if(listeActiveJitterAmp.at(synth) == 1, {
										amp = listeAmp.at(synth) + (40 * listeJitterAmp.at(synth) * rrand(-1.0, 1.0));
										amp = amp.clip(-inf, 0);
									}, {amp = listeAmp.at(synth)});
									// Volume Filter
									if(listeActiveJitterVolumeFilter.at(synth) == 1, {
										volumeFilter = listeVolumeFilter.at(synth) + (40 * listeJitterVolumeFilter.at(synth) * rrand(-1.0, 1.0));
										volumeFilter = volumeFilter.clip(-inf, 0)}, {volumeFilter = listeVolumeFilter.at(synth)});
									// Ctrl1 Filter
									if(listeActiveJitterCtrl1Filter.at(synth) == 1, {
										ctrl1Filter = listeCtrl1Filter.at(synth) * (10 ** (2 * listeJitterCtrl1Filter.at(synth) * rrand(-1.0, 1.0)));
										ctrl1Filter = ctrl1Filter.clip(20, 12544);
									}, {ctrl1Filter = listeCtrl1Filter.at(synth)});
									// Ctrl2 Filter
									if(listeActiveJitterCtrl2Filter.at(synth) == 1, {
										ctrl2Filter = listeCtrl2Filter.at(synth) * (10 ** (2 * listeJitterCtrl2Filter.at(synth) * rrand(-1.0, 1.0)));
										ctrl2Filter = ctrl2Filter.clip(0.01, 1);
									}, {ctrl2Filter = listeCtrl2Filter.at(synth)});
									// Ctrl3 Filter
									if(listeActiveJitterCtrl3Filter.at(synth) == 1, {
										ctrl3Filter = listeCtrl3Filter.at(synth) * (10 ** (2 * listeJitterCtrl3Filter.at(synth) * rrand(-1.0, 1.0)));
										ctrl3Filter = ctrl3Filter.clip(0.01, 1);
									}, {ctrl3Filter = listeCtrl3Filter.at(synth)});
									// Setup Data Synth Filter
									listeGroupFilter.at(synth).set(\ctrl1, ctrl1Filter, \ctrl2, ctrl2Filter, \ctrl3, ctrl3Filter, \vol, volumeFilter.dbamp);
									// Volume FX
									if(listeActiveJitterVolumeFX.at(synth) == 1, {
										volumeFX = listeVolumeFX.at(synth) + (40 * listeJitterVolumeFX.at(synth) * rrand(-1.0, 1.0));
										volumeFX = volumeFX.clip(-inf, 0)}, {volumeFX = listeVolumeFX.at(synth)});
									// Ctrl1 FX
									if(listeActiveJitterCtrl1FX.at(synth) == 1, {
										ctrl1FX = listeCtrl1FX.at(synth) * (10 ** (2 * listeJitterCtrl1FX.at(synth) * rrand(-1.0, 1.0)));
										ctrl1FX = ctrl1FX.clip(0.001, 1);
									}, {ctrl1FX = listeCtrl1FX.at(synth)});
									// Ctrl2 FX
									if(listeActiveJitterCtrl2FX.at(synth) == 1, {
										ctrl2FX = listeCtrl2FX.at(synth) * (10 ** (2 * listeJitterCtrl2FX.at(synth) * rrand(-1.0, 1.0)));
										ctrl2FX = ctrl2FX.clip(0.001, 1);
									}, {ctrl2FX = listeCtrl2FX.at(synth)});
									// Ctrl3 FX
									if(listeActiveJitterCtrl3FX.at(synth) == 1, {
										ctrl3FX = listeCtrl3FX.at(synth) * (10 ** (2 * listeJitterCtrl3FX.at(synth) * rrand(-1.0, 1.0)));
										ctrl3FX = ctrl3FX.clip(0.001, 1);
									}, {ctrl3FX = listeCtrl3FX.at(synth)});
									// Ctrl4 FX
									if(listeActiveJitterCtrl4FX.at(synth) == 1, {
										ctrl4FX = listeCtrl4FX.at(synth) * (10 ** (2 * listeJitterCtrl4FX.at(synth) * rrand(-1.0, 1.0)));
										ctrl4FX = ctrl4FX.clip(0.001, 1);
									}, {ctrl4FX = listeCtrl4FX.at(synth)});
									// Ctr4l5 FX
									if(listeActiveJitterCtrl5FX.at(synth) == 1, {
										ctrl5FX = listeCtrl5FX.at(synth) * (10 ** (2 * listeJitterCtrl5FX.at(synth) * rrand(-1.0, 1.0)));
										ctrl5FX = ctrl5FX.clip(0.001, 1);
									}, {ctrl5FX = listeCtrl5FX.at(synth)});
									// Setup Data Synth FX
									listeGroupFX.at(synth).set(\ctrl1, ctrl1FX, \ctrl2, ctrl2FX, \ctrl3, ctrl3FX, \ctrl4, ctrl4FX, \ctrl5, ctrl5FX, \vol, volumeFX.dbamp);
									// Jitter Panner X
									{
										if(listeActiveJitterPanX.at(synth) == 1, {
											panX = listePanX.at(synth) + (2 * listeJitterPanX.at(synth) * rrand(-1.0, 1.0));
											panX = panX.clip(-1, 1);
										},{panX = listePanX.at(synth)});
										// Jitter Panner Y
										if(listeActiveJitterPanY.at(synth) == 1, {
											panY = listePanY.at(synth) + (2 * listeJitterPanY.at(synth) * rrand(-1.0, 1.0));
											panY = panY.clip(-1, 1);},{panY = listePanY.at(synth)});
										// Setup Panner
										listeGroupDolby.at(synth).set(\panX, panX, \panY, panY);
									}.defer;
									// Re-Check MIDI
									if(listeDataBand.at(indexNumFhzBand) == [] or: {modeMIDIOSC.at(synth) == 'Note'},
										{midiOscFreq = 12.neg},
										{
											if(switchOSCfreq.at(synth) == 1,
												{midiOscFreq = listeDataBand.at(indexNumFhzBand).wrapAt(synth).at(0);// - 60
													midiOscFreq = 12 * (midiOscFreq / 12).frac;
												},
												{midiOscFreq = 12.neg});
											if(switchOSCamp.at(synth) == 1,
												{amp = ampMIDIOSC},
												{nil});
									});
									// Setup Freq with Scaling and Tuning
									freq = demiTon + (cent / 100) + (octave * 12 + 60) + midiOscFreq + bendMIDI;
									if(flagScaling != 'off', {
										oct = freq / 12;
										ratio = (oct.frac * 12).round(0.1);
										oct = oct.floor;
										pos = scale.degrees.indexOfEqual(ratio);
										if(pos == nil,
											{
												pos = scale.degrees.indexOfGreaterThan(ratio);
												if(pos == nil,
													{
														pos = scale.degrees.last;
													},
													{
														pos = scale.degrees.at(pos);
													}
												);
											},
											{
												pos = scale.degrees.at(pos);
											}
										);
										freq = oct * 12 + pos;
									});
									// Set Rate
									freqToMidi = freq.floor;
									freqSynth = (freq + 12).midicps;
									freqRate = (freq - 36).midicps;
									rate = 2**freqRate.cpsoct * listeReverse.at(synth);
									// Set MIDI Off
									if(flagMidiOut == 'on' and: {synthCanalMidiOut.wrapAt(synth).value >= 0}, {
										midiOut.noteOff(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), 0);
										if(flagVST == 'on', {~fxVST.midi.noteOff(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), 0)});
										// Reset MIDI OUT
										freqMidi.wrapPut(synth, freqToMidi+12);
										// Send MIDI On
										midiOut.noteOn(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), amp.dbamp * 127);
										if(flagVST == 'on', {~fxVST.midi.noteOn(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), amp.dbamp * 127)});
									});
									// Duree Synth
									switch(listeFlagDureeSynth.at(synth),
										'Seq', {dureeSynth = duree},
										'Pitch', {dureeSynth = abs(endPos - startPos) * (buffer.numFrames / s.sampleRate) * rate.abs.reciprocal},
										'Grain', {dureeSynth = abs(endPos - startPos) * (buffer.numFrames / s.sampleRate)}
									);
									// Playing  A Grain
									Synth.new(typeSynthDef.at(synth),[
										\out, listeBusInFilter.at(synth), \buffer, buffer.bufnum, \freq, freqSynth, \rate, rate, \amp, amp.dbamp, \duree, dureeSynth, \startPos, startPos, \endPos, endPos,
										\envLevel1, envLevel.at(0), \envLevel2, envLevel.at(1), \envLevel3, envLevel.at(2), \envLevel4, envLevel.at(3), \envLevel5, envLevel.at(4), \envLevel6, envLevel.at(5), \envLevel7, envLevel.at(6), \envLevel8, envLevel.at(7),
										\envTime1, envDuree.at(0), \envTime2, envDuree.at(1), \envTime3, envDuree.at(2), \envTime4, envDuree.at(3), \envTime5, envDuree.at(4), \envTime6, envDuree.at(5), \envTime7, envDuree.at(6), \loop, loopSample.at(synth), \hp1, hprec1, \hp2, hprec2], listeGroupSynth.at(synth), \addToHead).map(\oscFreq, busOSCfreq, \oscAmp, busOSCamp, \oscDuree, busOSCduree, \oscTempo, busOSCtempo, \oscFlux, busOSCflux, \oscFlatness, busOSCflatness, \oscEnergy, busOSCenergy, \oscCentroid, busOSCcentroid);
									//});
								}, {if(flagMidiOut == 'on' and: {synthCanalMidiOut.wrapAt(synth).value >= 0}, {midiOut.noteOff(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(synthCanalMidiOut.wrapAt(synth), freqMidi.wrapAt(synth), 0)});
								})});
							});
					});
					(duree * tempoOSC.reciprocal).wait});
			});

			// Create GUI windows
			this.createGUI;

			// Init Preset System
			file=File(~pathWekTime ++ "Init Preset" ++ ".scd", "w");
			file.write(fonctionSavePreset.value(windowControlGUI).asCompileString);
			file.close;

			cmdperiodfunc = {
				s.bind{
					if(flagRecording == 'on', {
						fonctionRecOff.value;
					});
					windowExternalControlGUI.close;
					windowControlGUI.close;
					windowKeyboard.close;
					windowVST.close;
					Tdef.removeAll;
					if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal);
						if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});
					MIDIIn.disconnect;
					MIDIdef.freeAll;
					//s.freeAll;
				};
				menuWekTime.remove;
				//s.quit;
			};

			CmdPeriod.doOnce(cmdperiodfunc);

		});

	}

	// GUI windows + Menu Add

	createGUI  {

		// Menu WekTime
		menuFile = Menu(
			MenuAction("Load File for Analyze",
				{Dialog.openPanel({ arg path;
					listeFileAnalyze.do({arg buffer; buffer.free});
					listeFileAnalyze=[];
					listeNameFileAnalyze=[];
					bufferFile.free;
					fonctionLoadFileForAnalyse.value(path);
					// Setup GUI Value
					windowExternalControlGUI.view.children.at(2).string = "FileIn :" + PathName.new(path).fileName},{"cancelled".postln})}),
			Menu(
				MenuAction("On", {synthFileIn.set('loop', 1)}),
				MenuAction("Off", {synthFileIn.set('loop', 0)});
			).title_("Loop");
		);
		MainMenu.register(menuFile.title_("File for Analyze"), "WekTimeTools");

		formatRecordingMenu = Menu(
			MenuAction("Channels Synth Out", {
				SCRequestString("[1, 1, 1, 1]", "Channels", {arg strg;
					~channelsSynth = strg.value.interpret; ~channelsSynth = ~channelsSynth.max(1) - 1;
					//this.initSynthDef;
					groupeLimiter.freeAll;
					~synthVST = Synth.newPaused("VST"+typeMasterOut, [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
					~fxVST = VSTPluginController(~synthVST);
					synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
					numberSynth.do({arg synth;
						listeGroupDolby.at(synth).freeAll;
						listeGroupDolby = listeGroupDolby.add(
							Synth.new(typeMasterOut, [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5, \panY, 0.5], listeGroupDolby.at(synth), \addToTail);
						)
					});
				});
			}),
			MenuAction("Stereo",
				{
					groupeLimiter.freeAll;
					~synthVST = Synth.newPaused("VST Stereo", [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
					~fxVST = VSTPluginController(~synthVST);
					synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
					numberSynth.do({arg synth;
						listeGroupDolby.at(synth).freeAll;
						listeGroupDolby = listeGroupDolby.add(
							Synth.new("Stereo", [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5], listeGroupDolby.at(synth), \addToTail);
						)
					});
					windowVST.name = "VST Stereo";
			}),
			MenuAction("Dolby5.1",
				{
					groupeLimiter.freeAll;
					~synthVST = Synth.newPaused("VST Dolby5.1", [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
					~fxVST = VSTPluginController(~synthVST);
					synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
					numberSynth.do({arg synth;
						listeGroupDolby.at(synth).freeAll;
						listeGroupDolby = listeGroupDolby.add(
							Synth.new("Dolby5.1", [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5], listeGroupDolby.at(synth), \addToTail);
						)
					});
					windowVST.name = "Dolby5.1";
			}),
			Menu(
				MenuAction("Channels",
					{
						SCRequestString("2", "Channels", {arg recChannels;
							numberAudioOut = recChannels.asInteger;
							s.recChannels_(recChannels.asInteger);
							s.options.numOutputBusChannels_(recChannels.asInteger);
							groupeLimiter.freeAll;
							~synthVST = Synth.newPaused("VST MultiSpeaker", [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
							~fxVST = VSTPluginController(~synthVST);
							synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
							numberSynth.do({arg synth;
								listeGroupDolby.at(synth).freeAll;
								listeGroupDolby = listeGroupDolby.add(
									Synth.new("MultiSpeaker", [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5], listeGroupDolby.at(synth), \addToTail);
								)
							});
							windowVST.name = "MultiSpeaker";
						});
				});
			).title_("MultiSpeaker"),
			MenuAction("Rotate2",
				{
					groupeLimiter.freeAll;
					~synthVST = Synth.newPaused("VST Rotate2", [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
					~fxVST = VSTPluginController(~synthVST);
					synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
					numberSynth.do({arg synth;
						listeGroupDolby.at(synth).freeAll;
						listeGroupDolby = listeGroupDolby.add(
							Synth.new("Rotate2", [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5], listeGroupDolby.at(synth), \addToTail);
						)
					});
					windowVST.name = "Rotate2";
			}),
			Menu(
				MenuAction("Channels",
					{
						SCRequestString("2", "Channels", {arg recChannels;
							numberAudioOut = recChannels.asInteger;
							s.recChannels_(recChannels.asInteger);
							s.options.numOutputBusChannels_(recChannels.asInteger);
							~synthVST = Synth.newPaused("VST Ambisonic", [\xFade, 0.5, \gainIn, 0.5], groupeLimiter, \addToTail).map(\bpm, busOSCtempo);
							~fxVST = VSTPluginController(~synthVST);
							synthLimiter= Synth.new("MasterFX", [\thresh, 0.1, \slopeBelow, 1, \slopAbove, 0.5, \limiter, 0.8], groupeLimiter, \addToTail);
							numberSynth.do({arg synth;
								listeGroupDolby.at(synth).freeAll;
								listeGroupDolby = listeGroupDolby.add(
									Synth.new("Ambisonic", [\out, ~channelsSynth.at(synth), \in, listeBusInDolby.at(synth), \panX, 0.5, \panY, 0.5], listeGroupDolby.at(synth), \addToTail);
								)
							});
							windowVST.name = "Ambisonic";
						});
				});
			).title_("Ambisonic"),
		);
		MainMenu.register(formatRecordingMenu.title_("Audio"), "WekTimeTools");

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
		MainMenu.register(menuRecording.title_("Recording"), "WekTimeTools");

		menuPreset = Menu(
			MenuAction("Load Preset+Control", {
				fonctionUserOperatingSystem.value(1, windowControlGUI, 'on');
			}),
			MenuAction("Load Preset", {
				fonctionUserOperatingSystem.value(2, windowControlGUI, 'off');
			}),
			MenuAction("Save Preset",{
				fonctionUserOperatingSystem.value(3, windowControlGUI);
			});
		);
		MainMenu.register(menuPreset.title_("Preset"), "WekTimeTools");

		menuSynth = Menu(
			MenuAction("Load Synthesizer", {
				fonctionUserOperatingSystem.value(4, windowControlGUI);
			}),
			MenuAction("Save Synthesizer",{
				fonctionUserOperatingSystem.value(5, windowControlGUI);
			}),
			Menu(
				MenuAction("Set Synth Source",
					{SCRequestString("1", "Source", {arg source;
						// Verify if valid Synthesizer
						if(source.asInteger >= 1 or: {source.asInteger <= numberSynth}, {synthSource = source.asInteger - 1}, {synthSource = nil; "cancelled".postln});
					});
				}),
				MenuAction("Set Synth Target", {
					SCRequestString("2", "Target", {arg target;
						// Verify if valid Synthesizer
						if(target.asInteger >= 1 or: {target.asInteger <= numberSynth}, {synthTarget = target.asInteger - 1; fonctionCopyTargetSynth.value(fonctionCopySourceSynth.value(synthSource), synthTarget)}, {synthTarget = nil; "cancelled".postln});
					});
				});
			).title_("Copy Synthesizer");
		);
		MainMenu.register(menuSynth.title_("Synthesizer"), "WekTimeTools");

		menuMIDI = Menu(
			MenuAction("Init", {
				MIDIClient.init;
				if(MIDIClient.externalSources != [ ], {
					// Connect first device by default
					MIDIIn.connect(0, 0);
					midiOut = MIDIOut(0);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
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
						16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
					});
				});
			).title_("Setting");
		);
		MainMenu.register(menuMIDI.title_("Midi"), "WekTimeTools");

		menuOSC = Menu(
			Menu(
				MenuAction("OSC NetAddr", {var addrM, addrS;
					// Set OSC Addresse et Port Master
					addrM=NetAddr.localAddr;
					addrS=NetAddr.localAddr;
					slaveAppAddr.disconnect;
					oscHPtempo.free;
					oscHPstart.free;
					oscHPrec.free;
					SCRequestString(addrM.ip, "Enter the NetAddr of Master App", {arg strg; addrM=strg;
						SCRequestString(NetAddr.langPort.asString, "Enter the Port of Master App", {arg strg; addrM=NetAddr(addrM, strg.asInteger); masterAppAddr = addrM;
							// Set OSC Addresse et Port Slave
							SCRequestString(addrS.ip, "Enter the NetAddr of Slave App", {arg strg; addrS=strg;
								SCRequestString(NetAddr.langPort.asString, "Enter the Port of Slave App", {arg strg; addrS=NetAddr(addrS, strg.asInteger); slaveAppAddr = addrS;
									initOSCresponder.value;
								});
							});
						});
					});
				});
			).title_("Set NetAddr"),
			Menu(
				MenuAction("OSC Master", {oscStateFlag='master'; "OSC Master".postln}),
				MenuAction("OSC Slave", {oscStateFlag='slave'; "OSC Slave".postln}),
				MenuAction("OSC Off", {oscStateFlag='off'; "OSC Off".postln});
			).title_("OSC Setting");
		);
		MainMenu.register(menuOSC.title_("OSC"), "WekTimeTools");

		menuAlgo = Menu(
			MenuAction("Wek In Port",
				{
					SCRequestString("6448", "Wek In Port", {arg index, port;
						port = index.asInteger;
						sender.free;
						sender = NetAddr.new("127.0.0.1", port);// Wekinator
					});
			}),
			MenuAction("Wek Out Port",
				{
					SCRequestString("57120", "Wek Out Port", {arg index, port;
						port = index.asInteger;
						thisProcess.openUDPPort(port);
						thisProcess.openPorts.postcs;
					});
			}),
			MenuAction("List
Preset Wek",
				{
					SCRequestString("[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40]", "listeWekPreset", {arg index;
						listeWekPreset = index.interpret;
					});
			});
		);
		MainMenu.register(menuAlgo.title_("Wekinator"), "WekTimeTools");

		menuHelp = MenuAction("Help ShortCut",
			{
				//Document.new("ShortCut for WekTime", helpWekTime);
				TextView().name_("ShortCut for WekTime").string_(helpWekTime).front;
		});
		MainMenu.register(menuHelp, "WekTimeTools");

		// Fonction ShortCut
		fonctionShortCut = {arg window;
			window.view.keyDownAction = {arg view,char,modifiers,unicode, keycode;
				var number;
				// [char,modifiers,unicode,keycode].postln;
				// Touches pave numerique
				if(modifiers==2097152 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(window, commande, 1)});
				if(modifiers==2097152 and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(window, commande, 2)});
				if(modifiers==2097152 and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(window, commande, 3)});
				if(modifiers==2097152 and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(window, commande, 4)});
				if(modifiers==2097152 and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(window, commande, 5)});
				if(modifiers==2097152 and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(window, commande, 6)});
				if(modifiers==2097152 and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(window, commande, 7)});
				if(modifiers==2097152 and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(window, commande, 8)});
				if(modifiers==2097152 and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(window, commande, 9)});
				if(modifiers==2097152 and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(window, commande, 10)});
				if(modifiers==2228224 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(window, commande, 11)});
				if(modifiers==2228224and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(window, commande, 12)});
				if(modifiers==2228224and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(window, commande, 13)});
				if(modifiers==2228224and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(window, commande, 14)});
				if(modifiers==2228224and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(window, commande, 15)});
				if(modifiers==2228224and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(window, commande, 16)});
				if(modifiers==2228224and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(window, commande, 17)});
				if(modifiers==2228224and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(window, commande, 18)});
				if(modifiers==2228224and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(window, commande, 19)});
				if(modifiers==2228224and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(window, commande, 20)});
				if(modifiers==2621440 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(window, commande, 21)});
				if(modifiers==2621440 and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(window, commande, 22)});
				if(modifiers==2621440 and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(window, commande, 23)});
				if(modifiers==2621440 and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(window, commande, 24)});
				if(modifiers==2621440 and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(window, commande, 25)});
				if(modifiers==2621440 and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(window, commande, 26)});
				if(modifiers==2621440 and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(window, commande, 27)});
				if(modifiers==2621440 and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(window, commande, 28)});
				if(modifiers==2621440 and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(window, commande, 29)});
				if(modifiers==2621440 and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(window, commande, 30)});
				if(modifiers==2752512and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(window, commande, 31)});
				if(modifiers==2752512and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(window, commande, 32)});
				if(modifiers==2752512and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(window, commande, 33)});
				if(modifiers==2752512and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(window, commande, 34)});
				if(modifiers==2752512and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(window, commande, 35)});
				if(modifiers==2752512and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(window, commande, 36)});
				if(modifiers==2752512and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(window, commande, 37)});
				if(modifiers==2752512and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(window, commande, 38)});
				if(modifiers==2752512and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(window, commande, 39)});
				if(modifiers==2752512and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(window, commande, 40)});
				// Touches clavier numerique
				if(modifiers==0 and: {unicode==49} and: {keycode==18},{fonctionCommandes.value(window, commande, 1)});
				if(modifiers==0 and: {unicode==50} and: {keycode==19},{fonctionCommandes.value(window, commande, 2)});
				if(modifiers==0 and: {unicode==51} and: {keycode==20},{fonctionCommandes.value(window, commande, 3)});
				if(modifiers==0 and: {unicode==52} and: {keycode==21},{fonctionCommandes.value(window, commande, 4)});
				if(modifiers==0 and: {unicode==53} and: {keycode==23},{fonctionCommandes.value(window, commande, 5)});
				if(modifiers==0 and: {unicode==54} and: {keycode==22},{fonctionCommandes.value(window, commande, 6)});
				if(modifiers==0 and: {unicode==55} and: {keycode==26},{fonctionCommandes.value(window, commande, 7)});
				if(modifiers==0 and: {unicode==56} and: {keycode==28},{fonctionCommandes.value(window, commande, 8)});
				if(modifiers==0 and: {unicode==57} and: {keycode==25},{fonctionCommandes.value(window, commande, 9)});
				if(modifiers==0 and: {unicode==48} and: {keycode==29},{fonctionCommandes.value(window, commande, 10)});
				if(modifiers==131072 and: {unicode==43} and: {keycode==18},{fonctionCommandes.value(window, commande, 11)});
				if(modifiers==131072 and: {unicode==34} and: {keycode==19},{fonctionCommandes.value(window, commande, 12)});
				if(modifiers==131072 and: {unicode==42} and: {keycode==20},{fonctionCommandes.value(window, commande, 13)});
				if(modifiers==131072 and: {unicode==231} and: {keycode==21},{fonctionCommandes.value(window, commande, 14)});
				if(modifiers==131072 and: {unicode==37} and: {keycode==23},{fonctionCommandes.value(window, commande, 15)});
				if(modifiers==131072 and: {unicode==38} and: {keycode==22},{fonctionCommandes.value(window, commande, 16)});
				if(modifiers==131072 and: {unicode==47} and: {keycode==26},{fonctionCommandes.value(window, commande, 17)});
				if(modifiers==131072 and: {unicode==40} and: {keycode==28},{fonctionCommandes.value(window, commande, 18)});
				if(modifiers==131072 and: {unicode==41} and: {keycode==25},{fonctionCommandes.value(window, commande, 19)});
				if(modifiers==131072 and: {unicode==61} and: {keycode==29},{fonctionCommandes.value(window, commande, 20)});
				if(modifiers==524288 and: {unicode==177} and: {keycode==18},{fonctionCommandes.value(window, commande, 21)});
				if(modifiers==524288 and: {unicode==8220} and: {keycode==19},{fonctionCommandes.value(window, commande, 22)});
				if(modifiers==524288 and: {unicode==35} and: {keycode==20},{fonctionCommandes.value(window, commande, 23)});
				if(modifiers==524288 and: {unicode==199} and: {keycode==21},{fonctionCommandes.value(window, commande, 24)});
				if(modifiers==524288 and: {unicode==91} and: {keycode==23},{fonctionCommandes.value(window, commande, 25)});
				if(modifiers==524288 and: {unicode==93} and: {keycode==22},{fonctionCommandes.value(window, commande, 26)});
				if(modifiers==524288 and: {unicode==124} and: {keycode==26},{fonctionCommandes.value(window, commande, 27)});
				if(modifiers==524288 and: {unicode==123} and: {keycode==28},{fonctionCommandes.value(window, commande, 28)});
				if(modifiers==524288 and: {unicode==125} and: {keycode==25},{fonctionCommandes.value(window, commande, 29)});
				if(modifiers==524288 and: {unicode==8800} and: {keycode==29},{fonctionCommandes.value(window, commande, 30)});
				if(modifiers==655360 and: {unicode==8734} and: {keycode==18},{fonctionCommandes.value(window, commande, 31)});
				if(modifiers==655360 and: {unicode==8221} and: {keycode==19},{fonctionCommandes.value(window, commande, 32)});
				if(modifiers==655360 and: {unicode==8249} and: {keycode==20},{fonctionCommandes.value(window, commande, 33)});
				if(modifiers==655360 and: {unicode==8260} and: {keycode==21},{fonctionCommandes.value(window, commande, 34)});
				if(modifiers==655360 and: {unicode==91} and: {keycode==23},{fonctionCommandes.value(window, commande, 35)});
				if(modifiers==655360 and: {unicode==93} and: {keycode==22},{fonctionCommandes.value(window, commande, 36)});
				if(modifiers==655360 and: {unicode==92} and: {keycode==26},{fonctionCommandes.value(window, commande, 37)});
				if(modifiers==655360 and: {unicode==210} and: {keycode==28},{fonctionCommandes.value(window, commande, 38)});
				if(modifiers==655360 and: {unicode==212} and: {keycode==25},{fonctionCommandes.value(window, commande, 39)});
				if(modifiers==655360 and: {unicode==218} and: {keycode==29},{fonctionCommandes.value(window, commande, 40)});
				// key esc or SpaceBar-> All System on/off
				if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}},{if(windowExternalControlGUI.view.children.at(0).value == 1,{windowExternalControlGUI.view.children.at(0).valueAction_(0)},{windowExternalControlGUI.view.children.at(0).valueAction_(1)});
				});
				// key l -> load Preset
				if(char == $l, {commande = 'Load Preset+Control';
				});
				// key alt+l -> load Preset+Control
				if(modifiers==524288 and: {unicode==108} and: {keycode==37},
					{commande = 'Load Preset';
				});
				// key s -> save Preset
				if(char == $s,
					{commande='Save Preset';
				});
				// key L -> load Synth
				if(char == $L, {commande = 'Load Synth';
				});
				// key S -> save Synth
				if(char == $S,
					{commande='Save Synth';
				});
				// Key alt + r -> Start Recording
				if(modifiers==524288 and: {unicode==114} and: {keycode==15}, {
					fonctionRecOn.value;
				});
				// Key ctrl + alt + r -> Stop Recording
				if(modifiers==786432 and: {unicode==18} and: {keycode==15}, {
					fonctionRecOff.value;
				});
				// Key R -> Pause Recording
				if(modifiers==131072 and: {unicode==82} and: {keycode==15}, {
					fonctionRecPause.value;
				});
				// Key t -> Switch Mode Sequencer
				if(char == $t, {
					switch(choiceTypeSequencer.value,
						0, {choiceTypeSequencer.valueAction_(1)},
						1, {choiceTypeSequencer.valueAction_(2)},
						2, {choiceTypeSequencer.valueAction_(3)},
						3, {choiceTypeSequencer.valueAction_(0)},
					);
				});
				// Key i -> Init Preset
				if(char == $i, {
					// Init Systeme
					if(File.exists(~pathWekTime ++ "Init Preset" ++ ".scd"), {
						file=File(~pathWekTime ++ "Init Preset" ++ ".scd","r");
						fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on');
						file.close;
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Init Preset";
					}, {"Canceled".postln});
				});
				// Key o -> Set Source Synth
				if(char == $o, {commande = 'Source Synth';
				});
				// Key a -> Set Target Synth and Copy Source on Target
				if(char == $a, {commande = 'Target Synth';
				});
				// Key f -> Switch file for AudioIn
				if(char == $f, {commande = 'Switch File for Analyze';
				});
				// Key ctrl + f -> Load and Add file for AudioIn
				if(modifiers==262144 and: {unicode==6} and: {keycode==3}, {
					Dialog.openPanel({ arg paths;
						listeFileAnalyze = listeFileAnalyze.add(fonctionLoadFileForAnalyse.value(paths));
						audioFileText.string_(PathName.new(paths).fileName.asString);
						listeNameFileAnalyze = listeNameFileAnalyze.add(PathName.new(paths).fileName.asString)
				},{"cancelled".postln})});
				// key w -> affichage windows ->
				if(char == $w, {indexWindows=indexWindows+1;
					if(indexWindows > (listeWindows.size - 1), {indexWindows=0});
					listeWindows.at(indexWindows).front;
				});
				// Key ctrlw -> affichage windows <-
				if(unicode==23 and: {keycode==13},{indexWindows=indexWindows-1;
					if(indexWindows < 0, {indexWindows=listeWindows.size - 1});
					listeWindows.at(indexWindows).front;
				});
				//key k New Environment
				if(char == $k, {
					FileDialog.new({arg path;
						~pathWekTime  = path.at(0).asString ++"/";
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  ~pathWekTime.asString;
						fonctionCollectFolders.value;
					}, fileMode: 2);
				});
				// Key z -> load Preset aleatoire
				if(char == $z, {
					number = rrand(0, foldersToScanPreset.size - 1);
					if(File.exists(~pathWekTime ++ foldersToScanPreset.at(number)),
						{file=File(~pathWekTime ++ foldersToScanPreset.at(number),"r");
							windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + foldersToScanPreset.at(number);
							numPreset = number.value; lastNumPreset = number.value;
							fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on'); file.close}, {"cancelled".postln});
				});
				// Key Z -> load Synthesizer aleatoire
				if(char == $Z, {
					number = rrand(0, foldersToScanSynthesizer.size - 1);
					if(File.exists(~pathWekTime ++ foldersToScanSynthesizer.at(number)),
						{file=File(~pathWekTime ++ foldersToScanSynthesizer.at(number),"r");
							windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " + foldersToScanSynthesizer.at(number);
							fonctionCopyTargetSynth.value(file.readAllString.interpret, synthTarget); file.close}, {"cancelled".postln});
				});
				// Key h -> Switch source In.
				if(char == $h,  {if(windowExternalControlGUI.view.children.at(5).value >= 1, {windowExternalControlGUI.view.children.at(5).valueAction_(0)},
					{windowExternalControlGUI.view.children.at(5).valueAction_(windowExternalControlGUI.view.children.at(5).value + 1)});
				});
				// Key q -> Switch algo.
				if(char == $q,  {if(windowExternalControlGUI.view.children.at(7).value >= 6, {windowExternalControlGUI.view.children.at(7).valueAction_(0)},
					{windowExternalControlGUI.view.children.at(7).valueAction_(windowExternalControlGUI.view.children.at(7).value + 1)});
				});
				// Key u -> Switch Automation
				if(char == $u,  {
					if(windowExternalControlGUI.view.children.at(8).value == 0 , {windowExternalControlGUI.view.children.at(8).valueAction_(1); windowExternalControlGUI.view.children.at(9).enabled_(true)}, {windowExternalControlGUI.view.children.at(8).valueAction_(0); windowExternalControlGUI.view.children.at9.enabled_(false)});
				});
			};
		};

		// Fonction Commandes
		fonctionCommandes = {arg window, commandeExecute, number;
			var file;
			// Save Preset
			if(commandeExecute == 'Save Preset',{
				windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Preset" + number.asString;
				file=File(~pathWekTime ++ "Preset" + number.asString ++ ".scd", "w");
				file.write(fonctionSavePreset.value(windowControlGUI).asCompileString);
				file.close;
			});
			//load Preset
			if(commandeExecute == 'Load Preset',{
				if(File.exists(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd"), {
					numPreset = number.value; lastNumPreset = number.value;
					file=File(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd","r");
					fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'off'); file.close;
					windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Preset" + number.asString}, {"cancelled".postln});
			});
			//load Preset+Control
			if(commandeExecute == 'Load Preset+Control',{
				if(File.exists(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd"), {
					numPreset = number.value; lastNumPreset = number.value;
					file=File(~pathWekTime ++ "Preset" + number.value.asString ++ ".scd","r");
					fonctionLoadPreset.value(file.readAllString.interpret, windowControlGUI, 'on'); file.close;
					windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Preset" + number.asString}, {"cancelled".postln});
			});
			// Save Synth
			if(commandeExecute == 'Save Synth',{
				SCRequestString("1", "Synthesizer Source", {arg source;
					// Verify if valid Synthesizer
					if(source.asInteger >= 1 or: {source.asInteger <= numberSynth}, {synthSource = source.asInteger - 1;
						windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Synthesizer" + number.asString;
						file=File(~pathWekTime ++ "Synthesizer" + number.asString ++ ".scd", "w");
						file.write(fonctionCopySourceSynth.value(synthSource).asCompileString);
						file.close}, {synthSource = nil; "cancelled".postln});
				});
			});
			//load Synth
			if(commandeExecute == 'Load Synth',{
				SCRequestString("1", "Synthesizer Target", {arg target;
					// Verify if valid Synthesizer
					if(target.asInteger >= 1 or: {target.asInteger <= numberSynth}, {synthTarget = target.asInteger - 1;
						if(File.exists(~pathWekTime ++ "Synthesizer" + number.value.asString ++ ".scd"), {
							file=File(~pathWekTime ++ "Synthesizer" + number.value.asString ++ ".scd","r");
							fonctionCopyTargetSynth.value(file.readAllString.interpret, synthTarget); file.close;
							windowControlGUI.name="WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production" + " | " +  "Synthesizer" + number.asString}, {"cancelled".postln});
					}, {synthTarget = nil; "cancelled".postln});
				});
			});
			//Set Source
			if(commandeExecute == 'Source Synth',{synthSource = number - 1});
			//Set Target and Copy
			if(commandeExecute == 'Target Synth',{synthTarget = number - 1; fonctionCopyTargetSynth.value(fonctionCopySourceSynth.value(synthSource), synthTarget)});
			// Switch File for Analyze
			if(commandeExecute == 'Switch File for Analyze', {
				if(listeFileAnalyze.at(number - 1) != nil, {
					s.bind{
						synthFileIn.set(\trigger, 0);
						synthFileIn.set(\bufferFile, listeFileAnalyze.at(number - 1));
						s.sync;
						synthFileIn.set(\trigger, 1);
						s.sync;
					};
					audioFileText.string_(listeNameFileAnalyze.at(number - 1));
				}, {"cancelled".postln});
			});
			commande = nil;
		};

		// ShortCuts for Keyboard
		keyboardShortCut = {arg window;
			var lastNote=60;
			// Down
			window.view.keyDownAction = {arg view,char,modifiers,unicode,keycode;
				// [char,modifiers,unicode,keycode].postln;
				// Key <- delete === Kill all notes
				if(modifiers == 0 and: {unicode == 8} and: {keycode == 51}, {
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
					synthOSCkeyboard.set(\trigger, 0);
					keyboard.setColor(lastNote, Color.blue);
				});
				// Translate
				// Key / -> Keyboard transpose down
				if(char == $/,  {keyboardTranslate.valueAction_(keyboardTranslate.value - 1);
				});
				// Key / -> Keyboard transpose up
				if(char == $*,  {keyboardTranslate.valueAction_(keyboardTranslate.value + 1);
				});
				// Musical keys
				if(char == $y, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (60 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 60 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(60 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $s, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (61 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 61 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(61 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $x, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (62 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 62 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(62 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $d, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (63 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 63 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(63 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $c, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (64 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 64 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(64 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $v, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (65 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 65 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(65 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $g, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (66 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 66 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(66 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $b, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (67 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 67 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(67 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $h, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (68 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 68 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(68 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $n, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (69 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 69 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(69 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $j, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (70 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 70 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(70 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $m, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (71 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 71 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(71 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $,, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (72 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 72 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(72 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $l, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (73 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 73 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(73 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $., {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (74 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 74 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(74 + keyboardTranslate.value, Color.red);
					};
				});
				// Key é
				if(modifiers == 0 and: {unicode == 233} and: {keycode == 41}, {
					/*if(char == $�, {*/
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (75 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 75 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(75 + keyboardTranslate.value, Color.red);
					};
				});
				if(char == $-, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthOSCkeyboard.set(\freq, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthOSCkeyboard.set(\freq, (76 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 76 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(76 + keyboardTranslate.value, Color.red);
					};
				});
			};
		};

		//////////////////////////// GUI ////////////////////////////////////

		//// Window Keyboard ////
		windowKeyboard = Window.new("Keyboard", Rect(300, 0, 1024, 175), scroll: true);
		windowKeyboard.view.decorator = FlowLayout(windowKeyboard.view.bounds);
		windowKeyboard.front;
		// Keyboard Translate
		keyboardTranslate = EZSlider(windowKeyboard, 250 @ 20, "Translate", ControlSpec(-36, 31, \lin, 1),
			{|ez| forBy(60 + keyboardTranslateBefore, 76 + keyboardTranslateBefore, 1, {arg note; keyboard.removeColor(note)});
				forBy(60 + ez.value, 76 + ez.value, 1, {arg note; keyboard.setColor(note, Color.blue)});
				keyboardTranslateBefore = ez.value;
		}, 0,labelWidth: 75,numberWidth: 50);
		// Keyboard volume
		keyboardVolume = EZSlider(windowKeyboard, 250 @ 20, "Volume", \db,
			{|ez| keyVolume = ez.value.dbamp}, -12,labelWidth: 75,numberWidth: 50);
		// Setup ShortCut
		setupKeyboardShortCut = Button(windowKeyboard, Rect(0, 0, 200, 20));
		setupKeyboardShortCut.states = [["Musical Keyboard Shortcut", Color.black,  Color.red(0.8, 0.25)],["System Shortcut", Color.white, Color.green(0.8, 0.25)]];
		setupKeyboardShortCut.action = {arg shortcut;
			if(shortcut.value == 0, {keyboardShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.setColor(note, Color.blue)})}, {fonctionShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.removeColor(note)});
			});
		};
		windowKeyboard.view.decorator.nextLine;
		// Keyboard Keys
		keyboard = MIDIKeyboard.new(windowKeyboard, Rect(5, 5, 1000, 140), 7, 24);
		forBy(60, 76, 1, {arg note; keyboard.setColor(note, Color.blue)});
		// Action Down
		keyboard.keyDownAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			synthOSCkeyboard.set(\freq, note, \amp, keyVolume, \trigger, 1);
		});
		// Action Up
		keyboard.keyUpAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			// Init Array
			lastTimeBand = [];
			listeDataBand = [];
			(numFhzBand + 1).do({arg i;
				lastTimeBand = lastTimeBand.add(Main.elapsedTime);
				listeDataBand = listeDataBand.add([]);
			});
			synthOSCkeyboard.set(\trigger, 0);
		});
		// Action Track
		keyboard.keyTrackAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			s.bind{
				synthOSCkeyboard.set(\freq, note, \amp, keyVolume, \trigger, 1);
				s.sync;
				synthOSCkeyboard.set(\freq, note, \amp, 0, \trigger, 0);
				s.sync;
				// Init Array
				lastTimeBand = [];
				listeDataBand = [];
				(numFhzBand + 1).do({arg i;
					lastTimeBand = lastTimeBand.add(Main.elapsedTime);
					listeDataBand = listeDataBand.add([]);
				});
			};
		});
		setupKeyboardShortCut.focus;

		windowKeyboard.onClose_({nil});

		keyboardShortCut.value(windowKeyboard);

		// External window osc and midi controls
		windowExternalControlGUI = Window("WekTime Controls", Rect(815, 650, 500, 270), scroll: true);
		windowExternalControlGUI.view.background_(Color.new255(32,40,52));
		windowExternalControlGUI.alpha=1.0;
		windowExternalControlGUI.view.decorator = FlowLayout(windowExternalControlGUI.view.bounds);

		///////////////////////////////////////////////////////////////////////////////////////////////

		// Systeme start stop playing
		startSystem = Button(windowExternalControlGUI,Rect(5, 5, 100, 20)).states_([["System On", Color.black,  Color.green],["System Off", Color.white, Color.red]]).focus.action = {|view|
			if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPstart', view.value)});// Send Synchro Start
			switch(view.value,
				0, {if(flagRecording == 'on', {s.pauseRecording}); sequencer.stop;
					if(oscStateFlag == 'master', {ardourOSC.sendMsg('/ardour/transport_stop')});// transport stop
					synthFileIn.run(false);
					if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal);
						if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});
				},
				1, {if(flagRecording == 'on', {s.record}); sequencer.play;
					if(oscStateFlag == 'master', {ardourOSC.sendMsg('/ardour/transport_play')});// transport play
					typeAudio.valueAction_(typeAudio.value);
			});
			indexSequence = 0;
		};


		// User Operating System
		userOperatingSystem = PopUpMenu(windowExternalControlGUI, Rect(0, 0, 200, 20)).font_(Font( "Palatino-BoldItalic", 12)).items = userOSchoiceControl;
		userOperatingSystem.action = {arg item; fonctionUserOperatingSystem.value(item.value);
			if(item.value == 5, {SCRequestString("1", "Synthesizer Source", {arg source;
				// Verify if valid Synthesizer
				if(source.asInteger >= 1 or: {source.asInteger <= numberSynth}, {synthSource = source.asInteger - 1}, {synthSource = nil; "cancelled".postln});
			})});
			if(item.value == 6, {SCRequestString("2", "Synthesizer Target", {arg target;
				// Verify if valid Synthesizer
				if(target.asInteger >= 1 or: {target.asInteger <= numberSynth}, {synthTarget = target.asInteger - 1; fonctionCopyTargetSynth.value(fonctionCopySourceSynth.value(synthSource), synthTarget)}, {synthTarget = nil; "cancelled".postln});
			})});
			userOperatingSystem.value_(0)};

		// TempoOSC
		oscTempo = Button(windowExternalControlGUI,Rect(5, 5, 75, 20)).states_([["OSCtempo Off", Color.green,  Color.black],["OSCtempo On", Color.red, Color.black]]).focus.action = {|view|
			if(view.value == 0, {flagTempo = 0}, {flagTempo = 1});
		};

		audioFileText = StaticText(windowExternalControlGUI, Rect(0, 0, 105, 20)).string_(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10)).align_(\right);

		windowExternalControlGUI.view.decorator.nextLine;

		// Setup Canal Midi
		PopUpMenu(windowExternalControlGUI, Rect(0, 0, 75, 20)).background_(Color.grey(0.75, 0.25)).stringColor_(Color.black).items_(choiceCanalMIDI).action = {arg item;
			canalMIDI = item.value;
			// Init Array
			lastTimeBand = [];
			listeDataBand = [];
			(numFhzBand + 1).do({arg i;
				lastTimeBand = lastTimeBand.add(Main.elapsedTime);
				listeDataBand = listeDataBand.add([]);
			});
		};

		// Setup AudioIn/FileIn
		typeAudio = PopUpMenu(windowExternalControlGUI, Rect(0, 0, 65, 20)).background_(Color.grey(0.75, 0.25)).stringColor_(Color.black).items_(['AudioIn', 'FileIn']).action = {arg item;
			if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});
			if(item.value == 0, {synthAudioIn.run(true); synthFileIn.run(false);
				numberSynth.do({arg synth;
					synthAudioRec.at(synth).free;
					s.bind;
					synthAudioRec.put(synth, (Synth.new("AudioRec", [\busIn, listeBusSynth.at(synth), \bufferAudioRec, listeBufferAudioRec.at(synth).bufnum, \recLevel, listeRecLevel.at(synth), \preLevel, listePreLevel.at(synth), \loop, listeLoop.at(synth)], listeGroupAudioRec.at(synth), \addToTail)));
					s.bind;
				});
			}, {synthFileIn.run(true); synthAudioIn.run(false);  synthFileIn.set(\offset, 0); synthFileIn.set(\trigger, 1);
				numberSynth.do({arg synth;
					synthAudioRec.at(synth).free;
					s.bind;
					synthAudioRec.put(synth, (Synth.new("FileRec", [\busIn, busAudioIn, \bufferAudioRec, listeBufferAudioRec.at(synth).bufnum, \recLevel, listeRecLevel.at(synth), \preLevel, listePreLevel.at(synth), \loop, listeLoop.at(synth)], listeGroupAudioRec.at(synth), \addToTail)));
					s.bind;
				});
			});
			lastTime = Main.elapsedTime;
		};

		// Setup Bus synthAnalyzeAudioIn
		PopUpMenu(windowExternalControlGUI, Rect(0, 0, 60, 20)).items_(['Bus 1', 'Bus 2', 'Bus 3', 'Bus 4', 'Bus 5', 'Bus 6', 'Bus 7', 'Bus 8', 'Bus 9', 'Bus 10', 'Bus 11', 'Bus 12', 'Bus 13', 'Bus 14', 'Bus 15', 'Bus 16', 'Bus 17', 'Bus 18', 'Bus 19', 'Bus 20', 'Bus 21', 'Bus 22', 'Bus 23', 'Bus 24', 'Bus 25', 'Bus 26', 'Bus 27', 'Bus 28', 'Bus 29', 'Bus 30', 'Bus 31', 'Bus 32']).action = {arg item;
			groupeAudioRec.set(\in, item.value, \busIn, busAudioIn.index);
			synthFileIn.set(\busIn, busAudioIn.index);
		};

		// Setup Algo Audio
		PopUpMenu(windowExternalControlGUI, Rect(0, 0, 70, 20)).background_(Color.grey(0.75, 0.25)).stringColor_(Color.black).items_(['Algo Off', 'Onset', 'Pitch', 'Pitch2', 'KeyTrack', 'Keyboard', 'MIDI']).action = {arg item;
			if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});
			switch(item.value,
				0, {synthOSConset.run(false); synthOSCpitch.run(false); synthOSCkeytrack.run(false); synthOSCkeyboard.run(false); synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 0; flagMIDI = 0; densityBPM = [windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60];
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				1, {synthOSConset.run(true); synthOSCpitch.run(false); synthOSCkeytrack.run(false); synthOSCkeyboard.run(false); synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 1;flagMIDI = 0;
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				2, {synthOSConset.run(false); synthOSCpitch.run(true); synthOSCkeytrack.run(false); synthOSCkeyboard.run(false); synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 1;flagMIDI = 0;
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				3, {synthOSConset.run(false); synthOSCpitch.run(false); synthOSCpitch2.run(true); synthOSCkeytrack.run(false); synthOSCkeyboard.run(false); synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 1; flagMIDI = 0;
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				4, {synthOSConset.run(false); synthOSCpitch.run(false); synthOSCkeytrack.run(true); synthOSCkeyboard.run(false);  synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 1; flagMIDI = 0;
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				5, {synthOSConset.run(false); synthOSCpitch.run(false); synthOSCkeytrack.run(false); synthOSCkeyboard.run(true);  synthOSCFFT.run(true); flagKeyboard='on'; flagOSC = 0; flagMIDI = 0; densityBPM = [windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60];
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				},
				6, {synthOSConset.run(false); synthOSCpitch.run(false); synthOSCkeytrack.run(false); synthOSCkeyboard.run(false); synthOSCFFT.run(true); flagKeyboard='off'; flagOSC = 0; flagMIDI = 1; densityBPM = [windowControlGUI.view.children.at(1).children.at(1).value / 60, windowControlGUI.view.children.at(1).children.at(3).value / 60];
					// Init Array
					lastTimeBand = [];
					listeDataBand = [];
					(numFhzBand + 1).do({arg i;
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
						listeDataBand = listeDataBand.add([]);
					});
				}
			);
			lastTime = Main.elapsedTime;
		};

		// Automation Preset and Synthesizer
		Button(windowExternalControlGUI,Rect(0, 0, 80, 20)).states_([["Automation Off", Color.new(0.1, 0.8, 0.9, 1),  Color.grey(0.75, 0.25)],["Automation On", Color.new(0.1, 0.8, 0.9, 1), Color.red]]).action = {|view|
			switch(view.value,
				0, {flagAutomation = 'off'; windowExternalControlGUI.view.children.at(9).enabled_(false);
				},
				1, {flagAutomation = 'on'; windowExternalControlGUI.view.children.at(9).enabled_(true);
			});
		};

		// Threshold Automation
		EZSlider(windowExternalControlGUI, Rect(0, 0, 105, 20), "Thresh", ControlSpec(0, 10, \lin, 0),
			{|ez| thresholdAutomation = ez.value}, 2, false, 35, 25).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0)).enabled_(false);

		windowExternalControlGUI.view.decorator.nextLine;

		// VolumeFileIn
		EZSlider(windowExternalControlGUI, Rect(0, 0, 125, 20), "FileVol", ControlSpec(-120, 12, \lin, 0),
			{|ez| synthFileIn.set(\volume, ez.value.dbamp)}, -inf, false, 30, 30).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8));

		// Offset FileIn
		EZSlider(windowExternalControlGUI, Rect(0, 0, 125, 20), "FileOffset", \unipolar,
			{|ez|
				s.bind{
					synthFileIn.set(\trigger, -1);
					s.sync;
					synthFileIn.set(\offset, ez.value);
					s.sync;
					synthFileIn.set(\trigger, 1);
					s.sync};
		}, 0, false, 45, 25).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8));

		// Filter for Algo Audio
		PopUpMenu(windowExternalControlGUI,Rect(0, 0, 60, 20)).
		items_(["Off", "LoPass", "HiPass"]).
		action = {|filter|
			if(filter.value == 0, {
				groupeAudioRec.setn(\ampInput, 1, \ampLoPass, 0, \ampHiPass, 0);
				windowExternalControlGUI.view.children.at(13).enabled_(false);
			});
			if(filter.value == 1, {
				groupeAudioRec.setn(\ampInput, 0, \ampLoPass, 1, \ampHiPass, 0);
				windowExternalControlGUI.view.children.at(13).enabled_(true);
			});
			if(filter.value == 2, {
				groupeAudioRec.setn(\ampInput, 0, \ampLoPass, 0, \ampHiPass, 1);
				windowExternalControlGUI.view.children.at(13).enabled_(true);
			});
		};
		EZSlider(windowExternalControlGUI, 165 @ 20, "HzPass", \freq,
			{|ez| groupeAudioRec.set(\hzPass, ez.value)}, 440,labelWidth: 40, numberWidth: 50);
		windowExternalControlGUI.view.children.at(13).enabled_(false);

		windowExternalControlGUI.view.decorator.nextLine;

		// Thresh
		EZSlider(windowExternalControlGUI, 240 @ 20, "Thresh", \unipolar, {|ez| groupeAudioRec.set(\seuil, ez.value)}, 0.5, labelWidth: 55,numberWidth: 40).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		// Filter
		EZSlider(windowExternalControlGUI, 240 @ 20, "Filter",\unipolar, {|ez| groupeAudioRec.set(\filtre, ez.value)}, 0.5, labelWidth: 55,numberWidth: 40).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		windowExternalControlGUI.view.decorator.nextLine;

		// Max Time
		EZSlider(windowExternalControlGUI, 240 @ 20, "Chord Time", ControlSpec(0.01, 1, \exp, 0),
			{|ez| chordDureeOSC = ez.value}, 0.0625, labelWidth: 90, numberWidth: 50).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));
		// Time Memory
		EZSlider(windowExternalControlGUI, 240 @ 20, "Time Memory", ControlSpec(0.01666, 3600, \exp, 0),
			{|ez| maxDureeOSC = ez.value}, 4, labelWidth: 90, numberWidth: 50).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		windowExternalControlGUI.view.decorator.nextLine;

		// Filter Fhz
		EZSlider(windowExternalControlGUI, 160 @ 20, "Fhz Filter", ControlSpec(0, 12, \lin, 0),
			{|ez| fhzFilter = ez.value}, 0.5, labelWidth: 60, numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));
		// Filter Amp
		EZSlider(windowExternalControlGUI, 160 @ 20, "Amp Filter", ControlSpec(0, 60, \lin, 0.1),
			{|ez| ampFilter = ez.value}, 1, labelWidth: 70, numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));
		// Filter Dur
		EZSlider(windowExternalControlGUI, 160 @ 20, "Dur Filter", ControlSpec(0.01, 16, \exp, 0),
			{|ez| durFilter = ez.value}, 0.03125, labelWidth: 60, numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		windowExternalControlGUI.view.decorator.nextLine;

		// Speed FFT OSC Data
		EZSlider(windowExternalControlGUI, 150 @ 20, "Speed FFT", ControlSpec(0.01, 100, \exp, 0),
			{|ez| groupeAudioRec.set(\speed, ez.value)}, 24, labelWidth: 50, numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		// Num Band FHZ
		EZSlider(windowExternalControlGUI, 200 @ 20, "NumFhzBand", ControlSpec(1, 12, \lin, 1),
			{arg band; numFhzBand = band.value.asInteger;
				// Init Band for each synth
				fonctionInitBand.value(numFhzBand);
				rangeBand.value = bandFHZ.cpsmidi.round(2);
		}, numFhzBand).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		// MIDI OOU on / off
		Button(windowExternalControlGUI,Rect(0, 0, 75, 20)).states_([["MIDI Out Off", Color.new(0.1, 0.8, 0.9, 1),  Color.grey(0.75, 0.25)],["MIDI Out On", Color.new(0.1, 0.8, 0.9, 1), Color.red]]).action = {|view|
			switch(view.value,
				0, {flagMidiOut = 'off'; 16.do({arg canal; midiOut.allNotesOff(canal);
					if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
				},
				1, {flagMidiOut = 'on'; 16.do({arg canal; midiOut.allNotesOff(canal);
					if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
				}
			)
		};

		windowExternalControlGUI.view.decorator.nextLine;

		// Limit
		EZSlider(windowExternalControlGUI, Rect(0, 0, 160, 20), "Limiter", \db,
			{|ez| groupeLimiter.set(\limit, ez.value.dbamp)}, -3, labelWidth: 40,numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8));

		// PostAmp
		EZSlider(windowExternalControlGUI, Rect(0, 0, 160, 20), "PostAmp", \db,
			{|ez| groupeLimiter.set(\postAmp, ez.value.dbamp)}, 0, labelWidth: 40,numberWidth: 35).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8));

		// Audio In Display (for testing)
		audioDisplay = StaticText(windowExternalControlGUI, Rect(0, 0, 50, 20)).string_("Audio").background_(Color.white).stringColor_(Color.red);

		//Normalize FFT
		EZRanger(windowExternalControlGUI , 490 @ 15, "Range FFT", \unipolar,
			{|ez| rangeFFT = ez.value}, [0, 1], labelWidth: 64).setColors(knobColor: Color.new(0.582, 0, 0), sliderColor: Color.grey, stringColor: Color.new(0.985, 0.701, 0)).setColors(Color.grey(0.3), Color.magenta);

		// Set Range FHZband
		rangeBand = EZText(windowExternalControlGUI, Rect(0, 0, 490, 20), "Range Band",
			{arg range; bandFHZ = range.value.midicps},
			[[0, 127], [0.0, 42.33], [42.33, 84.66], [84.66, 127.0] ], true).setColors(textBackground: Color.grey, stringColor: Color.new(0.985, 0.701, 0));

		// HP Records Flatness
		StaticText(windowExternalControlGUI, Rect(0, 0, 35, 20)).string_("RecCtrl").stringColor_(Color.new(0.985, 0.701, 0));

		NumberBox(windowExternalControlGUI, Rect(0, 0, 40, 20)).background_(Color.red).typingColor_(Color.white).clipLo_(0).clipHi_(1).decimals_(4).
		action = {arg num; hprec1 = num.value; listeGroupSynth.do({arg group; group.set(\hp1, num.value)})};

		NumberBox(windowExternalControlGUI, Rect(0, 0, 40, 20)).background_(Color.red).typingColor_(Color.white).clipLo_(0).clipHi_(1).decimals_(4).
		action = {arg num; hprec2 = num.value; listeGroupSynth.do({arg group; group.set(\hp2, num.value)})};

		// Wekinator
		Button(windowExternalControlGUI, Rect(0, 0, 65, 20)).states_([["WekRec On", Color.magenta], ["WekRec Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {sender.sendMsg("/wekinator/control/stopRecording")},
				1, {sender.sendMsg("/wekinator/control/startRecording");
					windowExternalControlGUI.view.children.at(34).valueAction = 0;// run
				}
			);
		});
		Button(windowExternalControlGUI, Rect(0, 0, 65, 20)).states_([["WekTrain On", Color.magenta]]).action_({|view|
			sender.sendMsg("/wekinator/control/train");
			windowExternalControlGUI.view.children.at(32).valueAction = 0;// rec
			windowExternalControlGUI.view.children.at(34).valueAction = 0;// run
		});
		Button(windowExternalControlGUI, Rect(0, 0, 65, 20)).states_([["WekRun On", Color.magenta], ["WekRun Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {flagStreamMFCC = 'off'; sender.sendMsg("/wekinator/control/stopRunning");
				},
				1, {flagStreamMFCC = 'wek'; sender.sendMsg("/wekinator/control/startRunning");
					windowExternalControlGUI.view.children.at(32).valueAction = 0;// rec
				}
			);
		});
		Button(windowExternalControlGUI, Rect(0, 0, 45, 20)).states_([["WTD On", Color.magenta], ["WTD Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {flagWTD = 'off'},
				1, {flagWTD = 'on'});
		}).valueAction_(1);
		NumberBox(windowExternalControlGUI, 25 @ 20).value_(0.0625).action_({|ez| timeWekData = ez.value});

		Button(windowExternalControlGUI, Rect(0, 0, 45, 20)).states_([["WTP On", Color.magenta], ["WTP Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {flagWTP = 'off'},
				1, {flagWTP = 'on'});
		}).valueAction_(1);
		NumberBox(windowExternalControlGUI, 25 @ 20).value_(4).action_({|ez| timeWekPreset = ez.value});

		windowExternalControlGUI.onClose_({nil});

		/////////////////////////////////////////////////////////// SYNTH ////////////////////////////////////////////////////////////////

		// Creation Window GUI
		windowControlGUI = Window("WekTime a Interactive and Organizer Musical Software by Provinescu's Software Production", Rect(0, 800, 1290, 800), scroll: true);
		windowControlGUI.view.background_(Color.new255(32,40,52));
		windowControlGUI.alpha=1.0;
		windowControlGUI.front;

		// Choice
		choiceTypeSequencer = PopUpMenu(windowControlGUI, Rect(5, 5, 70, 20)).background_(Color.magenta).stringColor_(Color.black).items_(changeChoiceTrigger).action = {arg item;
			numSequencer = item.value;
			switch(item.value,
				0, {typeSequencer = 'Rand'},
				1, {typeSequencer = 'Seq'; indexSequence = 0},
				2, {typeSequencer = 'WeightS'},
				3, {typeSequencer = 'WeightP'});
		};

		// Frequence BPM
		EZRanger(windowControlGUI, Rect(75, 5, 225, 20), "BPM", ControlSpec(1, 60000, \exp, 0),
			{|ez| densityBPM=ez.value / 60; wekBPM = ez.value;
				if(oscStateFlag == 'master', {slaveAppAddr.sendMsg('/HPtempo', ez.value)});//Send Synchro Tempo
		}, [30, 120], false, 30, 40).setColors(knobColor: Color.new(0.582, 0, 0), stringColor:  Color.red(0.8, 0.8)).setColors(Color.grey(0.3), Color.magenta);

		// Step Trigger
		EZSlider(windowControlGUI, Rect(305, 5, 90, 20), "Step", ControlSpec(1, numberMaxStepSequencer, \exp, 1),
			{|ez| numberStepSequencer = ez.value;
				//indexSequence = 0;
				//if(indexSequence >= numberMaxStepSequencer, {indexSequence = 0});
		}, numberMaxStepSequencer, false, 20, 25).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8)).setColors(Color.grey(0.3), Color.magenta);

		// Ambitus Freq
		EZRanger(windowControlGUI, Rect(395, 5, 180, 20), "Ambitus", ControlSpec(0, 127, \lin, 0),
			{|ez| ambitusFreq = ez.value}, [0, 127], false, 40, 30).setColors(knobColor: Color.new(0.582, 0, 0), stringColor:  Color.red(0.8, 0.8)).setColors(Color.grey(0.3), Color.magenta);

		// BPM In Display
		bpmDisplay = StaticText(windowControlGUI, Rect(580, 5, 30, 20)).string_("BPM").background_(Color.white).stringColor_(Color.red);

		// Auto Root
		Button(windowControlGUI, Rect(615, 5, 105, 20))
		.background_(Color.white).
		states_([["Auto On", Color.green, Color.black(0.75, 0.25)],["Auto Off", Color.red(1, 1), Color.black(0.75, 0.25)]]).
		action_({arg etat; if(etat.value == 0, {autoRoot = 'off'; /*windowControlGUI.view.children.at(7).children.at(2).valueAction_(0)*/}, {autoRoot = 'on'})});

		// Tuning
		PopUpMenu(windowControlGUI, Rect(725, 5, 130, 20)).
		items_(["No Scale", "- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"]).
		action = {arg item;
			// Setup GUI Value
			windowControlGUI.view.children.at(7).children.at(1).valueAction_(12);
			windowControlGUI.view.children.at(7).children.at(1).valueAction_(0);
			windowControlGUI.view.children.at(7).enabled_(true);
			windowControlGUI.view.children.at(8).enabled_(true);
			windowControlGUI.view.children.at(5).enabled_(true);
			switch(item.value,
				// No Scale
				0, {flagScaling = 'off';
					// Setup GUI Value
					windowControlGUI.view.children.at(7).children.at(1).valueAction_(12);
					windowControlGUI.view.children.at(7).children.at(1).valueAction_(0);
					windowControlGUI.view.children.at(7).enabled_(false);
					windowControlGUI.view.children.at(8).enabled_(false);
					windowControlGUI.view.children.at(5).valueAction_(0);
					windowControlGUI.view.children.at(5).enabled_(false);
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
				windowControlGUI.view.children.at(8).children.at(1).valueAction = degrees.asString;
			});
			if(item.value > 28, {tuning = Tuning.sruti; scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning); flagScaling = 'on';
				// Setup GUI Value
				windowControlGUI.view.children.at(8).children.at(1).valueAction = degrees.asString;
			});
		};
		// Root
		EZKnob(windowControlGUI, Rect(860, 5, 80, 20), "Root", ControlSpec(0, 21, \lin, 1),
			{|ez| root = ez.value; scale=Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)}, 0, layout: \horz, labelWidth: 30).setColors(stringColor: Color.red(0.8, 0.8));
		// Degrees
		EZText(windowControlGUI, Rect(945, 5, 340, 20), "Degrees",
			{arg string; degrees = string.value; scale=Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)},
			degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true).setColors(stringColor: Color.red(0.8, 0.8));

		// Init No Scale
		windowControlGUI.view.children.at(7).children.at(1).valueAction_(12);
		windowControlGUI.view.children.at(7).children.at(1).valueAction_(0);
		windowControlGUI.view.children.at(7).enabled_(false);
		windowControlGUI.view.children.at(8).enabled_(false);
		windowControlGUI.view.children.at(5).enabled_(false);

		// Step Sequencer
		StaticText(windowControlGUI, Rect(5, 25, 1260, 20)).string_("Step Sequencer").stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\center);

		numberSynth.do({arg synth;
			StaticText(windowControlGUI, Rect(5, synth * 25 + 50, 50, 20)).string_("Synth"+(synth+1).asString).stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\left);
			numberMaxStepSequencer.do({arg step;
				Button(windowControlGUI, Rect(step * 25 + 5 + 50, synth * 25 + 50, 20, 20))
				.background_(Color.black).
				states_([["O ", Color.new(0.1, 0.8, 0.9, 1),  Color.black(0.75, 0.25)],["X", Color.red(1, 1), Color.black(0.75, 0.25)]]).
				action_({arg etat; if(etat.value == 0, {listeSynthStepSequencer.at(synth).put(step, 0)}, {listeSynthStepSequencer.at(synth).put(step, 1)})});
			});
		});

		// Sequencer
		StaticText(windowControlGUI, Rect(5, numberSynth * 25 + 50, 1260, 20)).string_("Synthesizer").stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\center);

		//
		////////// Display GUI for each Synthesizer ////////////
		//

		numberSynth.do({arg synth;

			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 75, 130, 20), "Weight", ControlSpec(0, 1, \lin, 0),
				{|ez| listeWeightSynth.put(synth, ez.value)}, 0.5,labelWidth: 38, numberWidth: 40).setColors(knobColor: Color.new(0.582, 0, 0), stringColor:  Color.red(0.8, 0.8));

			// Choice SynthDef
			choiceTypeSynthDef = PopUpMenu(windowControlGUI, Rect(synth * 315 + 135, numberSynth * 25 + 75, 100, 20)).background_(Color.magenta).stringColor_(Color.black).items_(changeChoiceSynthDef).action = {arg item;
				typeSynthDef.put(synth, changeChoiceSynthDef.wrapAt(item.value));
			};

			// Bus In
			PopUpMenu(windowControlGUI, Rect(synth * 315 + 240, numberSynth * 25 + 75, 70, 20)).items_(['Bus 1', 'Bus 2', 'Bus 3', 'Bus 4', 'Bus 5', 'Bus 6', 'Bus 7', 'Bus 8', 'Bus 9', 'Bus 10', 'Bus 11', 'Bus 12', 'Bus 13', 'Bus 14', 'Bus 15', 'Bus 16', 'Bus 17', 'Bus 18', 'Bus 19', 'Bus 20', 'Bus 21', 'Bus 22', 'Bus 23', 'Bus 24', 'Bus 25', 'Bus 26', 'Bus 27', 'Bus 28', 'Bus 29', 'Bus 30', 'Bus 31', 'Bus 32']).action = {arg item;
				listeBusSynth.put(synth, item.value);
				synthAudioRec.at(synth).free;
				if(typeAudio.value == 0, {
					synthAudioRec.put(synth, Synth.new("AudioRec", [\busIn, listeBusSynth.at(synth), \bufferAudioRec, listeBufferAudioRec.at(synth).bufnum, \recLevel, listeRecLevel.at(synth), \preLevel, listePreLevel.at(synth), \loop, listeLoop.at(synth)], listeGroupAudioRec.at(synth), \addToTail));
				},
				{
					synthAudioRec.put(synth, (Synth.new("FileRec", [\busIn, busAudioIn, \bufferAudioRec, listeBufferAudioRec.at(synth).bufnum, \recLevel, listeRecLevel.at(synth), \preLevel, listePreLevel.at(synth), \loop, listeLoop.at(synth)], listeGroupAudioRec.at(synth), \addToTail)))
				});
			};

			// Type Sequencer
			PopUpMenu(windowControlGUI, Rect(synth * 315 + 35, numberSynth * 25 + 100, 60, 20)).background_(Color.magenta).stringColor_(Color.black).
			items_(["Seq", "Pitch", "Grain"]).
			action = {arg item;
				switch(item.value,
					0, {listeFlagDureeSynth.put(synth, 'Seq')},
					1, {listeFlagDureeSynth.put(synth, 'Pitch')},
					2, {listeFlagDureeSynth.put(synth, 'Grain')}
				);
			};

			// Setup MidiOSC
			PopUpMenu(windowControlGUI, Rect(synth * 315 + 100, numberSynth * 25 + 100, 80, 20)).background_(Color.magenta).stringColor_(Color.black).items_(changeChoiceMIDI).action = {arg item;
				modeMIDIOSC.put(synth, changeChoiceMIDI.wrapAt(item.value));
			};

			Button(windowControlGUI, Rect(synth * 315 + 185, numberSynth * 25 + 100, 100, 20)).states_([["Smp Loop On", Color.magenta,  Color.green(0.8, 0.25)],["Smp Loop Off", Color.magenta, Color.red(0.8, 0.25)]]).action = {|view|
				switch(view.value,
					0, {loopSample.put(synth, 0)},
					1, {loopSample.put(synth, 1)}
				);
			};

			// Mute Synth
			Button(windowControlGUI, Rect(synth * 315 + 75, numberSynth * 25 + 125, 50, 20))
			.background_(Color.grey).
			states_([["Mute", Color.new(0.1, 0.8, 0.9, 1),  Color.grey(0.75, 0.25)],["Mute", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			action_({arg etat; if(etat.value == 0, {listeMuteSynth.put(synth, 0)}, {listeMuteSynth.put(synth, 1); if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal);
				if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})})})});

			// Solo Synth
			Button(windowControlGUI, Rect(synth * 315 + 135, numberSynth * 25 + 125, 50, 20))
			.background_(Color.grey).
			states_([["Solo", Color.new(0.1, 0.8, 0.9, 1),  Color.grey(0.75, 0.25)],["Solo", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			action_({arg etat; if(etat.value == 0, {listeSoloSynth.put(synth, 0)}, {numberSynth.do({arg i; listeSoloSynth.put(i, 0);
				if(i != synth, {windowControlGUI.view.children.at((i * 93 + 8) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(0)})});
			listeSoloSynth.put(synth, 1); if(flagMidiOut == 'on', {16.do({arg canal; midiOut.allNotesOff(canal);
				if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})})})});

			// Buffer sampler
			Button(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 150, 90, 20)).
			states_([["Load Sample", Color.new(0.985, 0.701, 0), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view|
				Dialog.openPanel({arg paths;
					var selectionValues;
					// Setup startPos and lenght
					selectionValues = listeGUIsoundFile.at(synth).selection(0) / listeBuffer.at(synth).numFrames;
					bufferAndSoundFile = fonctionLoadSample.value(paths, listeGroupSynth.at(synth), listeBuffer.at(synth), listeBufferAudioRec.at(synth));
					listeBuffer.put(synth, bufferAndSoundFile.at(0));
					listeSoundFile.put(synth, bufferAndSoundFile.at(1));
					listeBufferAudioRec.put(synth, bufferAndSoundFile.at(2));
					s.ping(2.0, 1.0, {
						{
							listeGUIsoundFile.at(synth).soundfile_(listeSoundFile.at(synth)).read(0,
								listeSoundFile.at(synth).numFrames).refresh;
							listeGUIsoundFile.at(synth).currentSelection_(0);
							listeGUIsoundFile.at(synth).setSelection(0, selectionValues * listeSoundFile.at(synth).numFrames);
						}.defer;
					});
					// Refresh name soundFile
					windowControlGUI.view.children.at((synth * 93 + 19) + (numberSynth * numberMaxStepSequencer + 14)).string_(paths.asString);
				}, {"cancelled".postln});
			});

			// Reverse Sample
			Button(windowControlGUI, Rect(synth * 315 + 95, numberSynth * 25 + 150, 90, 20))
			.background_(Color.grey).
			states_([["Reverse Off", Color.new(0.1, 0.8, 0.9, 1),  Color.grey(0.75, 0.25)],["Reverse On", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			action_({arg etat; if(etat.value == 0, {listeReverse.put(synth, 1)}, {listeReverse.put(synth, 1.neg)})});

			// Loop Rec
			Button(windowControlGUI, Rect(synth * 315 + 190, numberSynth * 25 + 150, 10, 20)).
			states_([ ["l", Color.white, Color.black], ["L", Color.black, Color.red], ["L", Color.white, Color.blue]]).
			action_({ arg butt;
				listeLoop.put(synth, butt.value);
				if(butt.value == 2, {synthAudioRec.at(synth).set(\loop, 0)}, {synthAudioRec.at(synth).set(\loop, butt.value)});
			});

			// recLevel
			StaticText(windowControlGUI, Rect(synth * 315 + 205, numberSynth * 25 + 150, 10, 20)).
			string_("R").stringColor = Color.yellow;
			NumberBox(windowControlGUI, Rect(synth * 315 + 220, numberSynth * 25 + 150, 35, 20)).minDecimals_(4).maxDecimals_(4).
			action = {arg val;
				val = val.value;
				if(val < 0, {val = 0});
				if(val > 1, {val = 1});
				listeRecLevel.put(synth, val);
				synthAudioRec.at(synth).set(\recLevel, val);
			};

			// preLevel
			StaticText(windowControlGUI, Rect(synth * 315 + 260, numberSynth * 25 + 150, 10, 20)).
			string_("P").stringColor = Color.yellow;
			NumberBox(windowControlGUI, Rect(synth * 315 + 275, numberSynth * 25 + 150, 35, 20)).minDecimals_(4).maxDecimals_(4).
			action = {arg val;
				val = val.value;
				if(val < 0, {val = 0});
				if(val > 1, {val = 1});
				listePreLevel.put(synth, val);
				synthAudioRec.at(synth).set(\preLevel, val);
			};

			// Switch Freq OSC
			Button(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 175, 15, 20))
			.background_(Color.grey).
			states_([["f", Color.white,  Color.grey(0.75, 0.25)],["F", Color.red(0.8, 0.8), Color.grey(0.75, 0.25)]]).
			action_({arg etat; switchOSCfreq.put(synth, etat.value)}).valueAction_(1);

			// Switch Amp OSC
			Button(windowControlGUI, Rect(synth * 315 + 20, numberSynth * 25 + 175, 15, 20))
			.background_(Color.grey).
			states_([["a", Color.white,  Color.grey(0.75, 0.25)],["A", Color.red(0.8, 0.8), Color.grey(0.75, 0.25)]]).
			action_({arg etat; switchOSCamp.put(synth, etat.value)}).valueAction_(0);

			// Switch Duree OSC
			Button(windowControlGUI, Rect(synth * 315 + 35, numberSynth * 25 + 175, 15, 20))
			.background_(Color.grey).
			states_([["d", Color.white,  Color.grey(0.75, 0.25)],["D", Color.red(0.8, 0.8), Color.grey(0.75, 0.25)]]).
			action_({arg etat;
				numberSynth.do({arg i; switchOSCdur.put(i, etat.value);
					windowControlGUI.view.children.at((i  * 93 + 18) + (numberSynth * numberMaxStepSequencer + 14)).value_(etat.value);
					if(etat.value == 1, {windowControlGUI.view.children.at(1).enabled_(false)}, {windowControlGUI.view.children.at(1).enabled_(true)});
				});
			});

			// Text Buffer
			StaticText(windowControlGUI, Rect(synth * 315 + 50, numberSynth * 25 + 175, 260, 20)).string_(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10)).align_(\right);

			// SynthBand
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 200, 40, 20)).string = "Band";// 20

			// Band 0 to 12
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (0 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["0", Color.green], ["0", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(0, band.value)); fonctionBand.value(0, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (1 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["1", Color.green], ["1", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(1, band.value)); fonctionBand.value(1, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (2 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["2", Color.green], ["2", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(2, band.value)); fonctionBand.value(2, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (3 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["3", Color.green], ["3", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(3, band.value)); fonctionBand.value(3, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (4 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["4", Color.green], ["4", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(4, band.value)); fonctionBand.value(4, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (5 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["5", Color.green], ["5", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(5, band.value)); fonctionBand.value(5, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (6 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["6", Color.green], ["6", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(6, band.value)); fonctionBand.value(6, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (7 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["7", Color.green], ["7", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(7, band.value)); fonctionBand.value(7, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (8 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["8", Color.green], ["8", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(8, band.value)); fonctionBand.value(8, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (9 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["9", Color.green], ["9", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(9, band.value)); fonctionBand.value(9, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (10 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["10", Color.green], ["10", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(10, band.value)); fonctionBand.value(10, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (11 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["11", Color.green], ["11", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(11, band.value)); fonctionBand.value(11, synth)});
			Button.new(windowControlGUI, Rect(synth * 315 + 45 + (12 * 20), numberSynth * 25 + 200, 15, 20)).
			states_([["12", Color.green], ["12", Color.red]]).
			action_({arg band; flagIndexBand.put(synth, flagIndexBand.at(synth).put(12, band.value)); fonctionBand.value(12, synth)});

			// Active AudioIn/Sample
			Button(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 225, 20, 60)).
			states_([["S", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["A", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveAudioRec.put(synth, view.value);
			});

			// Display Waveform
			listeGUIsoundFile = listeGUIsoundFile.add(SoundFileView(windowControlGUI, Rect(synth*315+30, numberSynth * 25 + 225, 260, 60)));
			listeGUIsoundFile.at(synth).
			soundfile_(listeSoundFile.at(synth)).
			read(0, listeSoundFile.at(synth).numFrames).
			gridOn_(false).
			//gridResolution_(0.1).
			timeCursorOn_(false).
			timeCursorColor_(Color.white).
			background_(Color.new(0.1, 0.8, 0.9, 1)).
			waveColors_([Color.grey, Color.grey]).
			setSelectionColor(0, Color.new(0.985, 0.701, 0)).
			setSelectionStart(0, 0).
			setSelectionSize(0, listeSoundFile.at(synth).numFrames / 3).
			currentSelection_(0).
			mouseUpAction_{
				listeGUIsoundFile.at(synth).currentSelection_(0);
				listeStartPos.put(synth, listeGUIsoundFile.at(synth).selections(0).at(0).at(0));
				listeLenght.put(synth, listeGUIsoundFile.at(synth).selections(0).at(0).at(1));
			};

			// Active Jitter WaveForm
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 225, 20, 60)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterWavePos.put(synth, view.value);
				if(view.value == 0, {listeGUIsoundFile.at(synth).setSelectionColor(0, Color.new(0.985, 0.701, 0)); listeGUIsoundFile.at(synth).setSelection(0, [listeStartPos.at(synth), listeLenght.at(synth)])},{listeGUIsoundFile.at(synth).setSelectionColor(0, Color.red)});
			});

			// Jitter WaveForm
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 290, 310, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterWaveForm.put(synth, ez.value / 100)}, 10,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Envelope
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 315, 315, 20)).string_("Envelope").stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\center);

			EnvelopeView(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 340, 310, 60)).background_(Color.grey).
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
				listeEnvelopeSynth.put(synth, arrayEnv);
			};

			// Music Controls
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 405, 315, 20)).string_("Music Controls").stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\center);

			// Octave
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 430, 310, 20), "Octave", ControlSpec(-4, 4, \lin, 0),
				{|ez| listeOctave.put(synth, ez.value)}, 0,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8)).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Octave
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 455, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterOctave.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter Octave
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 455, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterOctave.put(synth, view.value)});

			// DemiTon
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 480, 310, 20), "DemiTon", ControlSpec(-12, 12, \lin, 0),
				{|ez| listeDemiTon.put(synth, ez.value)}, 0,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.red(0.8, 0.8)).setColors(Color.grey(0.3), Color.magenta);

			// Jitter DemiTon
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 505, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterDemiTon.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter DemiTon
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 505, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterDemiTon.put(synth, view.value)});

			// Cent
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 530, 310, 20), "Cent", ControlSpec(-100, 100, \lin, 0),
				{|ez| listeCent.put(synth, ez.value)}, 0,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.582, 0, 0), sliderBackground: Color.grey, stringColor:  Color.new(0.582, 0, 0)).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Cent
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 555, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCent.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter Cent
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 555, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCent.put(synth, view.value)});

			// Amp
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 580, 310, 20), "Amp", \db,
				{|ez| listeAmp.put(synth, ez.value)}, -6,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.1, 0.8, 0.9, 1), sliderBackground: Color.grey, stringColor:  Color.new(0.1, 0.8, 0.9, 1));

			// Jitter Amp
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 605, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterAmp.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter Amp
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 605, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterAmp.put(synth, view.value)});

			// Filter
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 630, 315, 20)).string_("Filter").stringColor_(Color.magenta).font_(Font("Georgia", 14)).align_(\center);

			// Choice Filter
			PopUpMenu(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 650, 310, 20)).background_(Color.grey).stringColor_(Color.magenta).items_(listeFilters).action = {arg item;
				choiceFilter.put(synth, item.value / (listeFilters.size - 1));
				switch(item.value,
					// ByPass
					0, {listeGroupFilter.at(synth).release(1); Synth.new("ByPassFilter", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// LPF
					1, {listeGroupFilter.at(synth).release(1); Synth.new("LPF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// HPF
					2, {listeGroupFilter.at(synth).release(1); Synth.new("HPF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// RLPF
					3, {listeGroupFilter.at(synth).release(1); Synth.new("RLPF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "RQ", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// RHPF
					4, {listeGroupFilter.at(synth).release(1); Synth.new("RHPF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "RQ", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// BPF
					5, {listeGroupFilter.at(synth).release(1); Synth.new("BPF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "RQ", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// BRF
					6, {listeGroupFilter.at(synth).release(1); Synth.new("BRF", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "RQ", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Ringz
					7, {listeGroupFilter.at(synth).release(1); Synth.new("Ringz", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "Decay", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Resonz
					8, {listeGroupFilter.at(synth).release(1); Synth.new("Resonz", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "RQ", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// CombCFilter
					9, {listeGroupFilter.at(synth).release(1); Synth.new("CombCFilter", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Delay", "Jitter %", "X", "Decay", "Jitter %", "X", "OctLow", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Formlet
					10, {listeGroupFilter.at(synth).release(1); Synth.new("Formlet", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Freq", "Jitter %", "X", "Attack", "Jitter %", "X", "Decay", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// FreqShift
					11, {listeGroupFilter.at(synth).release(1); Synth.new("FreqShift", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Shift", "Jitter %", "X", "Phase", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PitchShift
					12, {listeGroupFilter.at(synth).release(1); Synth.new("PitchShift", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Pitch", "Jitter %", "X", "PitchDsp", "Jitter %", "X", "TimeDsp", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagFreeze
					13, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagFreeze", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Pitch", "Jitter %", "X", "Freeze", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagNoise
					14, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagNoise", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagClip
					15, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagClip", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Thresh", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagSmooth
					16, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagSmooth", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Factor", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_Diffuser
					17, {listeGroupFilter.at(synth).release(1); Synth.new("PV_Diffuser", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Trigger1", "Jitter %", "X", "Trigger2", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_BrickWall
					18, {listeGroupFilter.at(synth).release(1); Synth.new("PV_BrickWall", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Wipe", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_LocalMax
					19, {listeGroupFilter.at(synth).release(1); Synth.new("PV_LocalMax", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Thresh", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagSquared
					20, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagSquared", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagBelow
					21, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagBelow", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Thresh", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagAbove
					22, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagAbove", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Thresh", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_RandComb
					23, {listeGroupFilter.at(synth).release(1); Synth.new("PV_RandComb", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Wipe", "Jitter %", "X", "Trigger", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagShift
					24, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagShift", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Stretch", "Jitter %", "X", "Shift", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_BinScramble
					25, {listeGroupFilter.at(synth).release(1); Synth.new("PV_BinScramble", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Wipe", "Jitter %", "X", "Width", "Jitter %", "X", "Trigger", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_BinShift
					26, {listeGroupFilter.at(synth).release(1); Synth.new("PV_BinShift", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Stretch", "Jitter %", "X", "Shift", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_RectComb
					27, {listeGroupFilter.at(synth).release(1); Synth.new("PV_RectComb", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Teeth", "Jitter %", "X", "Phase", "Jitter %", "X", "Width", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_ConformalMap
					28, {listeGroupFilter.at(synth).release(1); Synth.new("PV_ConformalMap", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Real", "Jitter %", "X", "Imag", "Jitter %", "X", "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_Compander
					29, {listeGroupFilter.at(synth).release(1); Synth.new("PV_Compander", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Thresh", "Jitter %", "X", "SlopeA", "Jitter %", "X", "SlopeB", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_SpectralEnhance
					30, {listeGroupFilter.at(synth).release(1); Synth.new("PV_SpectralEnhance", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Partials", "Jitter %", "X", "Ratio", "Jitter %", "X", "Strength", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagStretch
					31, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagStretch", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Stretch", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_MagShift+Stretch
					32, {listeGroupFilter.at(synth).release(1); Synth.new("PV_MagShift+Stretch", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Stretch", "Jitter %", "X", "Shift", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// DJ_FX
					33, {listeGroupFilter.at(synth).release(1); Synth.new("DJ_FX", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Rate", "Jitter %", "X", "Noise", "Jitter %", "X", "Delay", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// WaveLoss
					34, {listeGroupFilter.at(synth).release(1); Synth.new("WaveLoss", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Lenght", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},// Median
					35, {listeGroupFilter.at(synth).release(1); Synth.new("Median", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Lenght", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// LeakDC
					36, {listeGroupFilter.at(synth).release(1); Synth.new("LeakDC", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Coef", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Median+LeakDC
					37, {listeGroupFilter.at(synth).release(1); Synth.new("Median+LeakDC", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Lenght", "Jitter %", "X", "Coef", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// PV_Cutoff
					38, {listeGroupFilter.at(synth).release(1); Synth.new("PV_Cutoff", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Cutoff", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Delay
					39, {listeGroupFilter.at(synth).release(1); Synth.new("Delay", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Delay", "Jitter %", "X", "off", "Jitter %", "X", "off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// Warp+Delay
					40, {listeGroupFilter.at(synth).release(1); Synth.new("Warp+Delay", [\out, listeBusInFX.at(synth), \in, listeBusInFilter.at(synth),\ctrl1, listeCtrl1Filter.at(synth), \ctrl2, listeCtrl2Filter.at(synth), \ctrl3, listeCtrl3Filter.at(synth)], listeGroupFilter.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 58, 59, 60, 61, 62, 63, 64, 65, 66 ], ["Trigger", "Jitter %", "X", "Pitch", "Jitter %", "X", "Delay", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
				);
			};

			// Volume Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 675, 310, 20), "Dry Wet", \db,
				{|ez| listeVolumeFilter.put(synth, ez.value)}, -6,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.1, 0.8, 0.9, 1), sliderBackground: Color.grey, stringColor:  Color.new(0.1, 0.8, 0.9, 1));

			// Jitter Volume Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 700, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterVolumeFilter.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter Volume Filter
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 700, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterVolumeFilter.put(synth, view.value)});

			// Ctrl1 Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 720, 310, 20), "Off", ControlSpec(20, 12544, \exp, 0),
				{|ez| listeCtrl1Filter.put(synth, ez.value)}, 440,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.985, 0.701, 0), sliderBackground: Color.black, stringColor:  Color.new(0.985, 0.701, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl1 Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 745, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl1Filter.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl1 Filter
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 745, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl1Filter.put(synth, view.value)}).enabled_(true);

			// Ctrl2 Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 765, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl2Filter.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 10,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.985, 0.701, 0), sliderBackground: Color.black, stringColor:  Color.new(0.985, 0.701, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl2 Jitter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 790, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl2Filter.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Actvive Jitter Ctrl2 Filter
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 790, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl2Filter.put(synth, view.value)}).enabled_(true);

			// Ctrl3 Filter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 810, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl3Filter.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 10,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.985, 0.701, 0), sliderBackground: Color.black, stringColor:  Color.new(0.985, 0.701, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl3 Jitter
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 835, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl3Filter.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Actvive Jitter Ctrl3 Filter
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 835, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl3Filter.put(synth, view.value)}).enabled_(true);

			// FX
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 850, 315, 20)).string_("FX").stringColor_(Color.magenta).font_(Font("Georgia", 14)).align_(\center);

			// Choice FX
			PopUpMenu(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 875, 310, 20)).background_(Color.grey).stringColor_(Color.magenta).items_(listeFX).action = {arg item;
				choiceFX.put(synth, item.value / (listeFX.size - 1));
				switch(item.value,
					// ByPass
					0, {listeGroupFX.at(synth).release(1); Synth.new("ByPassFX", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X",  "Off", "Jitter %", "X"], [false, false, false, false, false, false, false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// AllPassC
					1, {listeGroupFX.at(synth).release(1); Synth.new("AllpassC", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Delay", "Jitter %", "X", "Decay", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X",  "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// FreeVerb
					2, {listeGroupFX.at(synth).release(1); Synth.new("FreeVerb", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Room", "Jitter %", "X", "Damp", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X",  "Off", "Jitter %", "X"], [true, true, true, true, true, true, false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// GVerb
					3, {listeGroupFX.at(synth).release(1); Synth.new("GVerb", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Room", "Jitter %", "X", "RevTime", "Jitter %", "X", "Damping", "Jitter %", "X", "TailLevel", "Jitter %", "X",  "RefLevel", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// JPverb
					4, {listeGroupFX.at(synth).release(1); Synth.new("JPverb", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["RevTime", "Jitter %", "X", "Damping", "Jitter %", "X", "EarlyDiff", "Jitter %", "X", "ModDepth", "Jitter %", "X",  "ModFreq", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// CombCFX
					5, {listeGroupFX.at(synth).release(1); Synth.new("CombCFX", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Delay", "Jitter %", "X", "Decay", "Jitter %", "X", "OctLow", "Jitter %", "X", "Off", "Jitter %", "X",  "Off", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// DelayC
					6, {listeGroupFX.at(synth).release(1); Synth.new("DelayC", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Delay", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X", "Off", "Jitter %", "X",  "Off", "Jitter %", "X"], [true, true, true, false, false, false, false, false, false, false, false, false, false, false, false], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					},
					// WarpDelay
					7, {listeGroupFX.at(synth).release(1); Synth.new("WarpDelay", [\out, listeBusInDolby.at(synth), \in, listeBusInFX.at(synth), \ctrl1, listeCtrl1FX.at(synth), \ctrl2, listeCtrl2FX.at(synth), \ctrl3, listeCtrl3FX.at(synth), \ctrl4, listeCtrl4FX.at(synth), \ctrl5, listeCtrl5FX.at(synth)], listeGroupFX.at(synth), \addToTail);
						fonctionSetupSliders.value(synth, [ 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86 ], ["Trigger", "Jitter %", "X", "Pitch", "Jitter %", "X", "WinSize", "Jitter %", "X", "Overlap", "Jitter %", "X",  "Delay", "Jitter %", "X"], [true, true, true, true, true, true, true, true, true, true, true, true, true, true, true], ['EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button', 'EZSlider', 'EZSlider', 'Button']);
					}
				);
			};

			// Volume FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 900, 310, 20), "Dry Wet", \db,
				{|ez| listeVolumeFX.put(synth, ez.value)}, -6,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(0.1, 0.8, 0.9, 1), sliderBackground: Color.grey, stringColor:  Color.new(0.1, 0.8, 0.9, 1));

			// Jitter Volume FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 925, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterVolumeFX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter Volume FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 925, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterVolumeFX.put(synth, view.value)});

			// Ctrl1 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 945, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl1FX.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 0.3,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(1, 0.299, 0), sliderBackground: Color.black, stringColor:  Color.new(1, 0.299, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl1 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 970, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl1FX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl1 FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 970, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl1FX.put(synth, view.value)}).enabled_(true);

			// Ctrl2 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 990, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl2FX.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 0.3,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(1, 0.299, 0), sliderBackground: Color.black, stringColor:  Color.new(1, 0.299, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl2 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1015, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl2FX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl2 FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1015, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl2FX.put(synth, view.value)}).enabled_(true);

			// Ctrl3 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1035, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl3FX.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 0.3,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(1, 0.299, 0), sliderBackground: Color.black, stringColor:  Color.new(1, 0.299, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl3 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1060, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl3FX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl3 FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1060, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl3FX.put(synth, view.value)}).enabled_(true);

			// Ctrl4 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1085, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl4FX.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 0.3,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(1, 0.299, 0), sliderBackground: Color.black, stringColor:  Color.new(1, 0.299, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl4 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1110, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl4FX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl4 FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1110, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl4FX.put(synth, view.value)}).enabled_(true);

			// Ctrl5 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1130, 310, 20), "Off", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeCtrl5FX.put(synth, abs(ez.value / 100).clip(0.0001, 1))}, 0.3,labelWidth: 50, numberWidth: 50).setColors(knobColor: Color.new(1, 0.299, 0), sliderBackground: Color.black, stringColor:  Color.new(1, 0.299, 0)).enabled_(true).setColors(Color.grey(0.3), Color.magenta);

			// Jitter Ctrl5 FX
			EZSlider(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1155, 285, 15), "Jitter % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterCtrl5FX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1)).enabled_(true);

			// Active Jitter Ctrl5 FX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1155, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterCtrl5FX.put(synth, view.value)}).enabled_(true);

			// Panner
			StaticText(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1175, 315, 20)).string_("Panner").stringColor_(Color.new(0.985, 0.701, 0)).font_(Font("Georgia", 14)).align_(\center);

			listeGUIpanner = listeGUIpanner.add(Slider2D(windowControlGUI, Rect(synth * 315 + 5, numberSynth * 25 + 1200, 100, 60)).
				background_(Color.grey).
				knobColor_(Color.new(0.582, 0, 0)).
				x_(0.5).
				y_(0.5).
				action_({arg slider; var x, y;
					x = \bipolar.asSpec.map(slider.x);
					y = \bipolar.asSpec.map(slider.y);
					listePanX.put(synth, x);
					listePanY.put(synth, y);
					listeGroupDolby.at(synth).set(\panX, x, \panY, y);
				});
			);

			// Jitter PanX
			EZSlider(windowControlGUI, Rect(synth * 315 + 105, numberSynth * 25 + 1209, 185, 15), "JitterX % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterPanX.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter PanX
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1209, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterPanX.put(synth, view.value)});

			// Jitter PanY
			EZSlider(windowControlGUI, Rect(synth * 315 + 105, numberSynth * 25 + 1233, 185, 15), "JitterY % ", ControlSpec(0.01, 100, \exp, 0),
				{|ez| listeJitterPanY.put(synth, ez.value / 100)}, 10, false, 50, 35).setColors(knobColor: Color.new(0.1, 0.3, 1, 1), sliderBackground: Color.black, stringColor:  Color.new(0.1, 0.3, 1, 1));

			// Active Jitter PanY
			Button(windowControlGUI, Rect(synth * 315 + 295, numberSynth * 25 + 1233, 20, 15)).
			states_([["O", Color.new(0.1, 0.8, 0.9, 1), Color.grey(0.75, 0.25)], ["X", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]]).
			background_(Color.grey).
			action_({|view| listeActiveJitterPanY.put(synth, view.value)});

			// Canal MIDi OUT
			choiceCanalMidiOut = PopUpMenu(windowControlGUI, Rect(synth * 315 + 195, numberSynth * 25 + 125, 100, 20))
			.background_(Color.grey).items_(["Midi Out Off", "Midi Out 1", "Midi Out 2", "Midi Out 3", "Midi Out 4", "Midi Out 5", "Midi Out 6", "Midi Out 7", "Midi Out 8", "Midi Out 9", "Midi Out 10", "Midi Out 11", "Midi Out 12", "Midi Out 13", "Midi Out 14", "Midi Out 15", "Midi Out 16"]).
			action_({arg midi;
				midiOut.allNotesOff(synthCanalMidiOut.wrapAt(synth));
				synthCanalMidiOut.wrapPut(synth, midi.value - 1);
			});
		});

		// Blank line
		StaticText(windowControlGUI, Rect(5, numberSynth * 25 + 1258, 1200, 20)).string_(" ").stringColor_(Color.white(1.0, 1.0)).font_(Font("Georgia", 14)).align_(\center);

		// Init Switch OSC and others
		numberSynth.do({arg synth;
			windowControlGUI.view.children.at((synth * 93 + 16) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(1);
			windowControlGUI.view.children.at((synth * 93 + 17) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(0);
			windowControlGUI.view.children.at((synth * 93 + 18) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(0);
			windowControlGUI.view.children.at((synth * 93 + 20) + (numberSynth * numberMaxStepSequencer + 14)).stringColor_(Color.red);
			windowControlGUI.view.children.at((synth * 93 + 11) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(1);
			windowControlGUI.view.children.at((synth * 93 + 13) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(1);
			windowControlGUI.view.children.at((synth * 93 + 15) + (numberSynth * numberMaxStepSequencer + 14)).valueAction_(0);
		});

		//Init numFhzBand
		fonctionInitBand.value;

		////////////////////////// Window VST ///////////////////////////////
		windowVST = Window.new("VST Stereo", Rect(1000, 540, 320, 80), scroll: true);
		windowVST.view.decorator = FlowLayout(windowVST.view.bounds);
		Button(windowVST, Rect(0, 0, 75, 20)).
		states_([["Run On", Color.green], ["Run Off", Color.red]]).
		action = {arg shortcut;
			switch (shortcut.value,
				0, {~synthVST.run(false); flagVST = 'off'},
				1, {~synthVST.run(true); flagVST ='on'};
			);
		};
		Button(windowVST, Rect(0, 0, 50, 20)).
		states_([["Browse", Color.white]]).
		action = {arg shortcut;
			~fxVST.browse;
		};
		Button(windowVST, Rect(0, 0, 50, 20)).
		states_([["Editor", Color.white]]).
		action = {arg shortcut;
			~fxVST.editor;
		};
		Button(windowVST, Rect(0, 0, 50, 20)).
		states_([["GUI", Color.white]]).
		action = {arg shortcut;
			~fxVST.gui;
		};
		Button(windowVST, Rect(0, 0, 50, 20)).
		states_([["Close", Color.white]]).
		action = {arg shortcut;
			~synthVST.free;
			~fxVST.close;
			// New VST
			~synthVST = Synth.newPaused("VST"+ typeMasterOut, [\xFade, 0.5, \panLo, 0, \panHi, 0, \gainIn, 0.5], groupeLimiter, \addToHead).map(\bpm, busOSCtempo);
			~fxVST = VSTPluginController(~synthVST);
		};
		EZKnob(windowVST, 150 @ 25, "xFade", \unipolar,
			{|ez| groupeLimiter.set(\xFade, ez.value)}, 0.5, layout: \horz);
		EZKnob(windowVST, 150 @ 25, "Gain In", \unipolar,
			{|ez| groupeLimiter.set(\gainIn, ez.value)}, 0.5, layout: \horz);
		EZRanger(windowVST , 300 @ 20, "Pan", \bipolar,
			{|ez| groupeLimiter.set(\panLo, ez.value.at(0), \panHi, ez.value.at(1))}, [0, 0], labelWidth: 40, numberWidth: 40);
		windowVST.name = "VST" + typeMasterOut;
		windowVST.view.children.at(0).focus;
		windowVST.front;
		windowControlGUI.front;

		listeWindows=listeWindows.add(windowVST);

		// Pour Acceder a un control precis
		// windowControlGUI.view.children.at((synth * 93 + numeroDuControl) + (numberSynth * numberMaxStepSequencer + 14)).children.at(numero de la view)_(valeur);

		// Setup Sliders
		fonctionSetupSliders = {arg synth, numSlider, valueSlider, visible, type;
			numSlider.size.do({arg index;
				var slider;
				slider = windowControlGUI.view.children.at((synth * 93 + numSlider.at(index)) + (numberSynth * numberMaxStepSequencer + 14));
				//// Is Actif ?
				//slider.enabled_(visible.at(index));
				// Set Views
				if(type.at(index) == 'EZSlider', {
					slider.children.at(0).string_(valueSlider.at(index));
					if(visible.at(index), {slider.children.at(1)}, {slider.children.at(1)});
				});
			});
		};

		// Fonction Copy Source
		fonctionCopySourceSynth = {arg synthSource;
			var data=[], sliderStart, sliderEnd, view;
			// Copy Source on Target
			// Setup Source
			sliderStart = (synthSource  * 93) + (numberSynth * numberMaxStepSequencer + 15);
			sliderEnd = (synthSource  * 93 + 91) + (numberSynth * numberMaxStepSequencer + 15);
			// Copy Data Source
			for(sliderStart, sliderEnd, {arg index;
				var arrayData=[], subArrayData=[], subType=nil;
				view = windowControlGUI.view.children.at(index);
				// QCompositeView
				arrayData=[];
				if(view.asString == "a View" or: {view.asString == "a CompositeView"} or: {view.asString == "a QCompositeView"} or: {view.asString == "a QView"} or: {view.asString == "a SCCompositeView"} or: {view.asString == "a SCView"},
					{view.children.do({arg subView;
						if(subView.asString == "a QStaticText" or: {subView.asString == "a SCStaticText"} or: {subView.asString == "a StaticText"}, {arrayData = arrayData.add(subView.string)});
						if(subView.asString == "a QSlider" or: {subView.asString == "a SCSlider"} or: {subView.asString == "a Slider"}, {arrayData=arrayData.add(subView.value); subType = 	subView.asString});
						if(subView.asString == "a QRangeSlider" or: {subView.asString == "a SCRangeSlider"} or: {subView.asString == "a RangeSlider"}, {subArrayData=subArrayData.add		(subView.lo);subArrayData=subArrayData.add(subView.hi); arrayData=arrayData.add		(subArrayData); subType = subView.asString});
						if(subView.asString == "a QNumberBox" or: {subView.asString == "a SCNumberBox"} or: {subView.asString == "a NumberBox"}, {arrayData=arrayData.add(subView.value)});
					});
					data = data.add([view.asString, subType, arrayData]);
				});
				// StaticText
				if(view.asString == "a QStaticText" or: {view.asString == "a SCStaticText"} or: {view.asString == "a StaticText"},
					{data = data.add([view.asString, view.string])});
				// QPopUpMenu + QButton
				if(view.asString == "a QPopUpMenu" or: {view.asString == "a QEnvelopeView"} or: {view.asString == "a QButton"} or: {view.asString == "a SCPopUpMenu"} or: {view.asString == "a SCEnvelopeView"} or: {view.asString == "a SCButton"} or: {view.asString == "a PopUpMenu"} or: {view.asString == "an EnvelopeView"} or: {view.asString == "a Button"},
					{data = data.add([view.asString, view.value])});
				// SoundFileView
				if(view.asString == "a QSoundFileView" or: {view.asString == "a SCSoundFileView"} or: {view.asString == "a SoundFileView"},
					{data = data.add([view.asString, view.selection(0)]);
				});
				// QSlider2D (Special Case)
				if(view.asString == "a QSlider2D" or: {view.asString == "a SCSlider2D"} or: {view.asString == "a Slider2D"},
					{data = data.add([view.asString, view.x, view.y])});
				// NumberBox
				if(view.asString   == "a NumberBox" or: {view.asString  == "a QNumberBox"} or: {view.asString == "a SCNumberBox"},
					{data = data.add([view.asString, view.value])});
			});
			data.value;
		};


		// Fonction Copy on Target
		fonctionCopyTargetSynth = {arg data, synthTarget;
			var sliderStart, sliderEnd, view, item1, item2;
			// Setup Target Wave
			item1 = 18;// String position in data
			item2 = 34;// Wave position in data
			bufferAndSoundFile = fonctionLoadSample.value(data.at(item1).at(1), listeGroupSynth.at(synthTarget), listeBuffer.at(synthTarget), listeBufferAudioRec.at(synthTarget));
			listeBuffer.put(synthTarget, bufferAndSoundFile.at(0));
			listeSoundFile.put(synthTarget, bufferAndSoundFile.at(1));
			listeBufferAudioRec.put(synthTarget, bufferAndSoundFile.at(2));
			listeGUIsoundFile.at(synthTarget).setSelection(0, data.at(item2).at(1));
			listeStartPos.put(synthTarget, data.at(item2).at(1).at(0));
			listeLenght.put(synthTarget, data.at(item2).at(1).at(1));
			s.ping(2, 1, {
				{
					listeGUIsoundFile.at(synthTarget).currentSelection_(0);
					listeGUIsoundFile.at(synthTarget).soundfile_(listeSoundFile.at(synthTarget)).read(0,
						listeSoundFile.at(synthTarget).numFrames).refresh;
					if(listeActiveJitterWavePos.at(synthTarget).value == 0, {listeGUIsoundFile.at(synthTarget).setSelectionColor(0, Color.new(0.985, 0.701, 0))},{listeGUIsoundFile.at(synthTarget).setSelectionColor(0, Color.red)});
				}.defer
			});
			// Setup Target
			sliderStart = (synthTarget  * 93) + (numberSynth * numberMaxStepSequencer + 15);
			sliderEnd = (synthTarget  * 93 + 91) + (numberSynth * numberMaxStepSequencer + 15);
			// Copy Data Source
			for(sliderStart, sliderEnd, {arg view, index;
				view = windowControlGUI.view.children.at(view);
				if(index.value != 8 and: {index.value != 34}, {
					// QCompositeView
					if(data.at(index).at(0) == "a View" or: {data.at(index).at(0) == "a CompositeView"} or: {data.at(index).at(0) == "a QCompositeView"} or: {data.at(index).at(0) == "a QView"} or: {data.at(index).at(0) == "a SCCompositeView"} or: {data.at(index).at(0) == "a SCView"}, {
						if(data.at(index).at(1) == "a QStaticText" or: {data.at(index).at(1) == "a SCStaticText"} or: {data.at(index).at(1) == "a StaticText"}, {view.children.at(1).valueAction_(data.at(index).at(2).at(1))});
						if(data.at(index).at(1) == "a QSlider" or: {data.at(index).at(1) == "a SCSlider"} or: {data.at(index).at(1) == "a Slider"}, {view.children.at(2).valueAction_(data.at(index).at(2).at(2))});
						if(data.at(index).at(1) == "a QRangeSlider" or: {data.at(index).at(1) == "a SCRangeSlider"} or: {data.at(index).at(1) == "a RangeSlider"}, {view.children.at(2).activeLo_(data.at(index).at	(2).at(2).at(0)); view.children.at(2).activeHi_(data.at(index).at(2).at(2).at(1))});
					});
					// StaticText
					if(data.at(index).at(0)  == "a QStaticText" or: {data.at(index).at(0)  == "a SCStaticText"} or: {data.at(index).at(0)  == "a StaticText"},
						{view.string_(data.at(index).at(1));
					});
					// QPopUpMenu + QButton
					if(data.at(index).at(0)  == "a QPopUpMenu" or: {data.at(index).at(0) == "a QEnvelopeView"} or: 	{data.at(index).at(0) == "a QButton"} or: {data.at(index).at(0)  == "a SCPopUpMenu"} or: {data.at(index).at(0) == "a SCEnvelopeView"} or: {data.at(index).at(0) == "a SCButton"} or: {data.at(index).at(0)  == "a PopUpMenu"} or: {data.at(index).at(0) == "an EnvelopeView"} or: {data.at(index).at(0) == "a Button"},
						{view.valueAction_(data.at(index).at(1))});
					// QSlider2D (special case)
					if(data.at(index).at(0)  == "a QSlider2D" or: {data.at(index).at(0)  == "a SCSlider2D"} or: {data.at(index).at(0)  == "a Slider2D"},
						{view.setXYActive(data.at(index).at(1), data.at(index).at(2))});
					// NumberBox
					if(data.at(index).at(0)    == "a NumberBox" or: {data.at(index).at(0)  == "a QNumberBox"} or: {data.at(index).at(0) == "a SCNumberBox"},
						{view.valueAction_(data.at(index).at(1))});
				});
			});
		};

		windowControlGUI.onClose_({nil});

		windowExternalControlGUI.front;

		// Setup ShortCuts on Window GUI
		listeWindows = listeWindows.add(windowExternalControlGUI);
		listeWindows = listeWindows.add(windowControlGUI);
		listeWindows = listeWindows.add(windowKeyboard);
		listeWindows.do({arg window; fonctionShortCut.value(window)});
		keyboardShortCut.value(windowKeyboard);
		listeWindows.do({arg window; fonctionShortCut.value(window);
			window.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Helvetica", 10);
				});
			});
		});

		// Setup Font View
		listeWindows.do({arg window;
			window.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Futura", 10);
				});
			});
		});

	}

	// SynthDef

	initSynthDef  {

		//////////////////////// SYNTH  WekTime//////////////////////

		/*BufRateScale.kr(buffer) * rate*/

		SynthDef('TGrains',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos ));
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = Mix(HPtGrains.ar(2, trig, buffer, rate, BufDur.kr(buffer) * abs(endPos - startPos / 2 + startPos), duree, 0, amp, hp1, hp2)) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('Warp1',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain;
				duree = duree * rate.abs;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = Warp1.ar(1, buffer, startPos, rate, duree, -1, 8, oscFlatness) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('BufRd',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPbufRd.ar(1, buffer, Phasor.ar(0, rate, BufFrames.kr(buffer) * startPos, BufFrames.kr(buffer) * endPos), 1, loop, hp1, hp2) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('LoopBuf',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain;
				duree = if(rate < 1, duree * rate.abs.reciprocal, duree * rate);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = LoopBuf.ar(1, buffer, rate, 1, BufFrames.kr(buffer) * startPos, BufFrames.kr(buffer) * startPos, BufFrames.kr(buffer) * endPos) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('PlayBuf',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('PlayBufMedianLeakDC',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				oscFlatness = (oscFlatness * 30 + 1).clip(1, 31);
				oscFlux = oscFlux.clip(0.01, 0.995);
				chain = LeakDC.ar(Median.ar(oscFlatness, HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope), oscFlux);
				Out.ar(out, chain);
		}).add;

		SynthDef('Waveloss',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = WaveLoss.ar(chain, oscFlux * 20 + 20, oscFlatness * 40 + 40);
				Out.ar(out, chain);
		}).add;

		SynthDef('DelayHarmonic',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig, phase, envDel, del, maxDel = 0.05, inputSig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				inputSig = HPplayBuf.ar(1, buffer, 1, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				rate = (freq.cpsmidi - 60).midiratio - 1 / maxDel;
				phase = LFSaw.ar(rate.neg, [1, 0]).range(0, maxDel);
				envDel = SinOsc.ar(rate, [3pi/2, pi/2]).range(0, 1).sqrt;
				del = DelayC.ar(inputSig, maxDel, phase) * envDel;
				chain = del.sum;
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth HPshiftDown',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_HPshiftDown(chain, oscFlux.clip(0.001, 1) * 32);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagShift',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagShift(chain, oscFlux.clip(0.001, 1) * 4, oscFlatness.clip(0.001, 1) * 128 - 64);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth LocalMax',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_LocalMax(chain, oscFlux*64);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth RandComb',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_RandComb(chain, oscFlux,  LFNoise2.kr(oscFlatness*64));
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth BinShift',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_BinShift(chain, oscFlux * 4, oscFlatness * 256);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth BinScramble',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				oscDuree = if(oscDuree > 1, oscDuree.reciprocal, oscDuree);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_BinScramble(chain, oscFlux,  oscFlatness, LFNoise2.kr(oscDuree.reciprocal));
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth BrickWall',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_BrickWall(chain, oscFlux*2 - 1);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth ConformalMap',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_ConformalMap(chain, oscFlux*2 - 1, oscFlatness*2 - 1);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth Diffuser',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise2.kr(oscFlux*100), (oscFlatness*100).reciprocal));
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagAbove',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagAbove(chain, oscFlux * 1024);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagBelow',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagBelow(chain,  oscFlux * 1024);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagClip',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagClip(chain, oscFlux * 1024);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagSquared',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagSquared(chain);
				chain= IFFT(chain) * 0.01;
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth RectComb',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				oscDuree = if(oscDuree > 1, oscDuree.reciprocal, oscDuree);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_RectComb(chain, oscFlux * 1024, oscFlatness);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagSmooth',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagSmooth(chain, oscFlux);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth Compander',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				oscDuree = if(oscDuree > 1, oscDuree.reciprocal, oscDuree);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_Compander(chain, 256 * oscFlux.clip(0.1, 1), (oscFlatness * 5).clip(2, 5), oscDuree);
				chain= IFFT(chain);
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth SpectralEnhance',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				oscDuree = if(oscDuree > 1, oscDuree.reciprocal, oscDuree);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_SpectralEnhance(chain, (oscFlux * 8 + 0.5).floor, oscFlatness * 4 + 1);
				chain= IFFT(chain) * 0.125;
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagStretch',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagShift(chain, oscFlux * 256);
				chain= IFFT(chain) * 0.125;
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth MagShift+Stretch',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagShift(chain, oscFlux * 256, oscFlatness * 128);
				chain= IFFT(chain) * 0.125;
				Out.ar(out, chain);
		}).add;

		SynthDef('Synth Cutoff',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer).reciprocal * abs(endPos - startPos));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2) * envelope;
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_Cutoff(chain, oscFlux * 2 - 1);
				chain= IFFT(chain) * 0.125;
				Out.ar(out, chain);
		}).add;

		SynthDef('SinOsc',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = SinOsc.ar(freq, 0, envelope);
				Out.ar(out, chain);
		}).add;

		SynthDef('SawSynth',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = LFSaw.ar(freq, 0, 0.5);
				//chain = RLPF.ar(chain, oscEnergy, (oscFlatness * 10).clip(0.01, 1), envelope);
				chain = chain * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('CombSynth',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux = 0.5, oscFlatness, oscEnergy, oscCentroid = 440, hp1=0.5, hp2=0.5;
				var envelope, chain, trig;
				trig = Impulse.kr(BufDur.kr(buffer) * abs(endPos - startPos ));
				startPos = if(rate < 0, endPos, startPos);
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = HPplayBuf.ar(1, buffer, rate, trig, BufFrames.kr(buffer) * startPos, loop, hp1, hp2);
				chain = CombC.ar(chain, 0.2, oscFlatness.clip(0.001, 1), (oscFlux * 100).clip(1, 10), 0.25);
				//chain = Decimator.ar(chain, oscFlatness * 48000, oscFlux * 24, mul: envelope);
				//chain = Decimator.ar(chain, oscFlatness * 96000, oscFlatness * 24, mul: envelope);
				chain = chain * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('MdaPiano',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)) * envelope;
				//DetectSilence.ar(chain, 0.01, doneAction:2);
				Out.ar(out, chain);
		}).add;

		SynthDef('Guitare',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree.reciprocal)), oscDuree));
				chain = CombC.ar(chain, 0.2, freq.reciprocal.clip(0.001, 1), (oscFlatness * 100).clip(1, 10)) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('StringSynth',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain, fc, osc;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				fc = LinExp.kr(LFNoise1.kr(Rand(oscFlux, oscDuree)), -1,1,500,2000);
				osc = Mix.fill(8, {LFSaw.ar(freq * [Rand(0.99,1.01), Rand(0.99,1.01)], 0, amp) }).distort * 0.2;
				chain= RLPF.ar(osc, fc, (oscFlatness * 10).clip(0, 1), envelope);
				Out.ar(out, chain);
		}).add;

		SynthDef('Gendy3',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp / 2, 0, duree, 2);
				// Synth
				//chain = Gendy3.ar(Rand(0, 6), duree, oscFlatness, oscFlux, freq, oscCentroid / 12544, oscEnergy / 12544, mul: envelope);
				chain = Gendy3.ar(Rand(0, 6), Rand(0, 6), oscFlatness, oscFlux, freq, amp / 100, duree / 100, mul: envelope);
				Out.ar(out, chain);
		}).add;

		SynthDef('Blip',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp / 2, 0, duree, 2);
				// Synth
				//chain = Blip.ar(XLine.kr(freq, 27.5, duree), 64, envelope, 0);
				chain = Blip.ar(freq, Line.kr(1, 64 * oscFlatness.clip(1, 64), duree), envelope, 0);
				Out.ar(out, chain);
		}).add;

		SynthDef('DynKlang',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy=440, oscCentroid = 440;
				var envelope, chain, arrayF,arrayA,arrayD;
				arrayF=[freq, Rand(oscEnergy, oscCentroid), Rand(oscEnergy, oscCentroid), Rand(oscCentroid, oscEnergy), Rand(oscCentroid, oscEnergy)];
				//arrayF=[freq, freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66)];
				arrayA = Array.fill(5,{1/5});
				arrayD = Array.fill(5,{0});
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = DynKlang.ar(`[arrayF, arrayA, arrayD], 1, 0) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('Klang',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid = 440;
				var envelope, chain, arrayF, arrayA, arrayD;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				arrayF=[freq, Rand(oscEnergy, oscCentroid), Rand(oscEnergy, oscCentroid), Rand(oscCentroid, oscEnergy), Rand(oscCentroid, oscEnergy)];
				//arrayF=[freq, freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66), freq * Rand(0.33, 1.66)];
				arrayA = Array.fill(5,{1/5});
				arrayD = Array.fill(5,{0});
				// Synth
				chain = Klang.ar(`[arrayF, arrayA, arrayD]);
				chain = chain * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('Formant',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid = 440;
				var envelope, chain;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				chain = Formant.ar(freq, oscEnergy.lag, oscCentroid.lag) * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('FM',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid = 440;
				var envelope, chain, modPartial, carPartial, index, mod, car;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				modPartial = 4 - oscFlux.log10.abs.ceil.lag;// + flux;
				carPartial = 4 - oscFlatness.log10.abs.ceil.lag;// + flatness;
				index = 4 - oscFlux.log10.abs.ceil.lag;
				mod =SinOsc.ar(freq * modPartial, 0 , freq * index * LFNoise1.kr((oscCentroid / oscEnergy).abs.lag).abs);
				car = SinOsc.ar(freq * carPartial + mod);
				chain = car * 0.5 * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('Ring',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid = 440;
				var envelope, chain, mod, car;
				// Envelope
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7],'sine'), 1.0, amp, 0, duree, 2);
				// Synth
				oscEnergy = (oscEnergy / 8372 * 4186).clip(50, 4186).lag;
				mod =SinOsc.ar(oscEnergy / 10);
				car = SinOsc.ar(freq, 0, mod);
				chain = car * 0.5 * envelope;
				Out.ar(out, chain);
		}).add;

		SynthDef('AnalogKick',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain, env, env2, env3, pch, osc, noise;
				// Synth
				env = EnvGen.kr(Env.perc(0, 0.20, 1, -4), doneAction:2);
				env2 = EnvGen.kr(Env.new([60,3,0],[0.08,0.16],[-18,-5]));
				env3 = EnvGen.kr(Env.new([0.8,0],[0.10],[-10]));
				pch = (freq+env2).midicps;
				osc = SinOsc.ar(pch, 0, env);
				noise = BPF.ar(WhiteNoise.ar(env3), 200, 2);
				chain = osc+noise * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('AnalogSnare',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain, env, env2, env3, pch, osc, noise;
				// Synth
				env = EnvGen.kr(Env.perc(0, 0.05, 1, -5));
				env2 = EnvGen.kr(Env.new([56,3,0],[0.08,0.1],[-20,-5]));
				env3 = EnvGen.kr(Env.new([0.5,0,0.5,0],[0.001,0.001,0.16],[0,-3,-8]), doneAction:2);
				pch = (freq+env2).midicps;
				osc = SinOsc.ar(pch, 0, env);
				noise = BPF.ar(WhiteNoise.ar(env3), 9000, 2);
				chain = osc+noise * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('AnalogHiHat',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, chain, env,  pch, osc, noise, decay=0.05;
				// Synth
				env = EnvGen.kr(Env.perc(0, decay, 1, -6), doneAction:2);
				pch = (freq+SinOsc.ar(320, 0, 2000));
				osc = SinOsc.ar(pch, 0, 0.5);
				noise = WhiteNoise.ar;
				chain = osc + noise;
				chain = BPF.ar(chain, 12000, 0.3, env) * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('SOSkick',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, mod_freq = 5, mod_index = 5,
				decay = 0.4,
				beater_noise_level = 0.025;
				var pitch_contour, drum_osc, drum_lpf, drum_env;
				var beater_source, beater_hpf, beater_lpf, lpf_cutoff_contour, beater_env;
				var chain;
				// Synth
				pitch_contour = Line.kr(freq*2, freq, 0.02);
				drum_osc = PMOsc.ar(pitch_contour,
					mod_freq,
					mod_index/1.3,
					mul: 1,
					add: 0);
				drum_lpf = LPF.ar(in: drum_osc, freq: 1000, mul: 1, add: 0);
				drum_env = drum_lpf * EnvGen.ar(Env.perc(0.005, decay), 1.0, doneAction: 2);
				beater_source = WhiteNoise.ar(beater_noise_level);
				beater_hpf = HPF.ar(in: beater_source, freq: 500, mul: 1, add: 0);
				lpf_cutoff_contour = Line.kr(6000, 500, 0.03);
				beater_lpf = LPF.ar(in: beater_hpf, freq: lpf_cutoff_contour, mul: 1, add: 0);
				beater_env = beater_lpf * EnvGen.ar(Env.perc, 1.0, doneAction: 2);
				chain = Mix.new([drum_env, beater_env]) * 2 * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('SOSsnare',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, decay = 0.1, drum_mode_level = 0.25,
				snare_level = 40, snare_tightness = 1000;
				var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix,
				drum_mode_env;
				var snare_noise, snare_brf_1, snare_brf_2, snare_brf_3, snare_brf_4,
				snare_reson;
				var snare_env;
				var chain;
				// Synth
				drum_mode_env = EnvGen.ar(Env.perc(0.005, decay), 1.0, doneAction: 2);
				drum_mode_sin_1 = SinOsc.ar(freq*0.53, 0, drum_mode_env * 0.5);
				drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
				drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.85),
					184,
					0.5/1.3,
					mul: drum_mode_env*5,
					add: 0);
				drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2,
					drum_mode_pmosc]) * drum_mode_level;

				// choose either noise source below
				//	snare_noise = Crackle.ar(2.01, 1);
				snare_noise = LFNoise0.ar(12544, 0.1);
				snare_env = EnvGen.ar(Env.perc(0.005, decay), 1.0, doneAction: 2);
				snare_brf_1 = BRF.ar(in: snare_noise, freq: 8000, mul: 0.5, rq: 0.1);
				snare_brf_2 = BRF.ar(in: snare_brf_1, freq: 5000, mul: 0.5, rq: 0.1);
				snare_brf_3 = BRF.ar(in: snare_brf_2, freq: 3600, mul: 0.5, rq: 0.1);
				snare_brf_4 = BRF.ar(in: snare_brf_3, freq: 2000, mul: snare_env, rq: 0.0001);
				snare_reson = Resonz.ar(snare_brf_4, snare_tightness, mul: snare_level) ;
				chain = Mix.new([drum_mode_mix, snare_reson]) * 5 * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('SOShats',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, dur = 0.1;
				var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
				var initial_bpf_contour, initial_bpf, initial_env;
				var body_hpf, body_env;
				var cymbal_mix;
				var chain;
				// Synth
				root_cymbal_square = Pulse.ar(freq, 0.5, mul: 1);
				root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
					[freq*1.34, freq*2.405, freq*3.09, freq*1.309],
					[310/1.3, 26/0.5, 11/3.4, 0.72772],
					mul: 1,
					add: 0);
				root_cymbal = Mix.new(root_cymbal_pmosc);

				initial_bpf_contour = Line.kr(15000, 9000, 0.1);
				initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
				initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);

				body_env = EnvGen.ar(Env.perc(0.005, dur, 1, -2), 1.0, doneAction: 2);
				body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, dur),
					mul: body_env, add: 0);

				chain = Mix.new([initial_bpf, body_hpf]) * amp;
				Out.ar(out, chain);
		}).add;

		SynthDef('SOStom',
			{arg out, buffer, freq, rate, amp, duree, startPos, endPos,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125, loop=0,
				oscFreq, oscAmp, oscDuree, oscTempo, oscFlux, oscFlatness, oscEnergy, oscCentroid;
				var envelope, decay = 0.4, drum_mode_level = 0.25, drum_timbre = 1.0;
				var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix,
				drum_mode_env;
				var stick_noise, stick_env;
				var drum_reson, tom_mix;
				var chain;
				// Synth
				drum_mode_env = EnvGen.ar(Env.perc(0.005, decay), 1.0, doneAction: 2);
				drum_mode_sin_1 = SinOsc.ar(freq*0.8, 0, drum_mode_env * 0.5);
				drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
				drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.9),
					freq*0.85,
					drum_timbre/1.3,
					mul: drum_mode_env*5,
					add: 0);
				drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2,
					drum_mode_pmosc]) * drum_mode_level;

				stick_noise = Crackle.ar(2.01, 1);
				stick_env = EnvGen.ar(Env.perc(0.005, 0.01), 1.0) * 3;

				chain = Mix.new([drum_mode_mix, stick_env]) * 2 * amp;
				Out.ar(out, chain);
		}).add;

		////////////////////////////// Filters ///////////////////////

		// ByPassFilter
		SynthDef('ByPassFilter',
			{arg out, in, gate = 1;
				var chain;
				chain = In.ar(in, 1);
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, In.ar(in, 1));
		}).add;

		// LPF
		SynthDef('LPF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain  = Mix(LPF.ar(signal, ctrl1, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// HPF
		SynthDef('HPF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(HPF.ar(signal, ctrl1, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// RLPF
		SynthDef('RLPF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain  = Mix(RLPF.ar(signal, ctrl1, abs(ctrl2 - 1), vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// RHPF
		SynthDef('RHPF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(RHPF.ar(signal, ctrl1, abs(ctrl2 - 1), vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// BPF
		SynthDef('BPF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(BPF.ar(signal, ctrl1, ctrl2, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// BRF
		SynthDef('BRF',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(BRF.ar(signal, ctrl1, ctrl2, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Ringz
		SynthDef('Ringz',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(Ringz.ar(signal, ctrl1, ctrl2 * 4, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Resonz
		SynthDef('Resonz',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(Resonz.ar(signal, ctrl1, abs(ctrl2 - 1), vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// CombCFilter
		SynthDef('CombCFilter',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(CombC.ar(signal, 0.1, ctrl1 / 12544 / 10, ctrl2 * 4 * (ctrl3 - 0.5 + 0.001).sign, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Formlet
		SynthDef('Formlet',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(Formlet.ar(signal, ctrl1, ctrl2, ctrl3 * 4, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// FreqShift
		SynthDef('FreqShift',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(FreqShift.ar(signal, ctrl1 / 12544 * 5000, ctrl2 * 2pi, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PitchShift
		SynthDef('PitchShift',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(PitchShift.ar(signal, 0.1, ctrl1 / 12544 * 4 + 0.08, ctrl2, ctrl3, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagFreeze
		SynthDef('PV_MagFreeze',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1), buffer=LocalBuf(s.sampleRate, 1).clear;
				RecordBuf.ar(signal, buffer, recLevel: 1, preLevel: 0.333, loop: 1);
				chain = PlayBuf.ar(1, buffer, (ctrl1 / 12544 * 4).clip(0.25, 4), 1, loop: 1);
				chain = FFT(LocalBuf(1024, 1), chain);
				chain = PV_MagFreeze(chain, SinOsc.kr(ctrl2.clip(0.0625, 1)));
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagNoise
		SynthDef('PV_MagNoise',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagNoise(chain);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagClip
		SynthDef('PV_MagClip',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagClip(chain, ( 1 - (ctrl1 / 12544)) * 16);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagSmooth
		SynthDef('PV_MagSmooth',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagSmooth(chain, ctrl1 / 12544);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_Diffuser
		SynthDef('PV_Diffuser',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise2.kr(ctrl1 / 12544 * 100), (ctrl2 * 100).reciprocal));
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_BrickWall
		SynthDef('PV_BrickWall',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_BrickWall(chain, ctrl1 / 12544 * 2 - 1);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_LocalMax
		SynthDef('PV_LocalMax',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_LocalMax(chain, ctrl1 / 12544 * 64);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagSquared
		SynthDef('PV_MagSquared',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagSquared(chain);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (ctrl1 / 12544 * (vol - 1)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagBelow
		SynthDef('PV_MagBelow',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagBelow(chain, (1 - (ctrl1 / 12544)) * 64);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagAbove
		SynthDef('PV_MagAbove',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagAbove(chain, ctrl1 / 12544 * 64);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_RandComb
		SynthDef('PV_RandComb',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_RandComb(chain, ctrl1 / 12544, LFNoise2.kr(ctrl2 * 100));
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagShift',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagShift(chain, ctrl1 / 12544 * 4, ctrl2 * 128 - 64);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_BinScramble
		SynthDef('PV_BinScramble',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_BinScramble(chain, ctrl1 / 12544, ctrl2, LFNoise2.kr((1 - ctrl3).reciprocal));
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_BinShift
		SynthDef('PV_BinShift',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_BinShift(chain, ctrl1 / 12544 * 4, ctrl2 * 256 - 64);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_RectComb
		SynthDef('PV_RectComb',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_RectComb(chain, ctrl1 / 12544 * 32, ctrl2, ctrl3);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_ConformalMap
		SynthDef('PV_ConformalMap',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_ConformalMap(chain, ctrl1 / 12544 * 2 - 1, ctrl2 * 2 - 1);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_Compander
		SynthDef('PV_Compander',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_Compander(chain, ctrl1 / 12544 * 64, ctrl2 * 10, ctrl3 * 10);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_SpectralEnhance
		SynthDef('PV_SpectralEnhance',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_SpectralEnhance(chain, (ctrl1 / 12544 * 100 + 1).floor, ctrl2 * 4 + 1, ctrl3);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagStretch',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagShift(chain, (ctrl1 / 12544).log.abs.clip(0.25, 4));
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_MagShift
		SynthDef('PV_MagShift+Stretch',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_MagShift(chain, (ctrl1 / 12544).log.abs.clip(0.25, 4), ctrl2 - 0.5 * 128);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// DJ_FX
		SynthDef('DJ_FX',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1), buffer=LocalBuf(s.sampleRate, 1).clear, local=LocalIn.ar(1);
				RecordBuf.ar(signal, buffer,  recLevel: 1, preLevel: 0.333, loop: 1);
				chain = Mix(HPplayBuf.ar(1, buffer, (ctrl1 / 12544 * 4) + LFNoise2.kr(ctrl2.reciprocal), 1, 0, loop: 1) + (local * 0.5));
				LocalOut.ar(DelayC.ar(chain, 1, ctrl3.clip(0.01, 1)));
				chain = Mix(chain  * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// WaveLoss
		SynthDef('WaveLoss',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain  = Mix(WaveLoss.ar(signal, ctrl1 / 12544 * 40, 40, abs(ctrl2 * 2 - 1), vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Median
		SynthDef('Median',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(Median.ar(ctrl1 / 12544 * 30 + 1, signal, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// LeakDC
		SynthDef('LeakDC',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(LeakDC.ar(signal, ctrl1 / 12544, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Median+LeakDC
		SynthDef('Median+LeakDC',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(LeakDC.ar(Median.ar(ctrl1 / 12544 * 30 + 1, signal, vol, signal * (1 - vol)), ctrl2));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// PV_Cutoff
		SynthDef('PV_Cutoff',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = FFT(LocalBuf(1024, 1), signal);
				chain = PV_Cutoff(chain, ctrl1 / 12544 * 2 - 1);
				chain= IFFT(chain);
				chain = Mix(chain * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Delay
		SynthDef('Delay',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, local, signal=In.ar(in, 1);
				local = LocalIn.ar(1) + signal;
				chain = Mix(DelayC.ar(local, 5.0, ctrl1 / 12544 * 5.0, vol, signal * (1 - vol)));
				LocalOut.ar(chain);
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// Warp+Delay
		SynthDef('Warp+Delay',
			{arg out, in, ctrl1, ctrl2, ctrl3, vol, gate = 1;
				var chain, signal=In.ar(in, 1), buffer=LocalBuf(s.sampleRate * 4, 1).clear;
				LocalIn.ar(1).clear;
				RecordBuf.ar(signal, buffer,  recLevel: 1, preLevel: 0.333, loop: 1);
				chain = Mix(Warp1.ar(1, buffer, TRand.kr(0, 1, Dust.kr((ctrl1 / 12544 * 64).clip(0.0625, 64))), (ctrl2 * 8).clip(0.125, 8), 0.2, -1, 8, 0, 1, vol, signal * (1 - vol)));
				LocalOut.ar(DelayC.ar(chain, 4, ctrl3.clip(0.01, 4)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		////////////////////////////// FX ///////////////////////

		// ByPassFX
		SynthDef('ByPassFX',
			{arg out, in, gate = 1;
				var chain;
				chain = In.ar(in, 1);
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// AllpassC
		SynthDef('AllpassC',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1), local;
				local = LocalIn.ar(1) + signal;
				chain = Mix(AllpassC.ar(local, 0.1, ctrl1, ctrl2 * 4, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				LocalOut.ar(chain);
				Out.ar(out, chain);
		}).add;

		// FreeVerb
		SynthDef('FreeVerb',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(FreeVerb.ar(signal, 1.0, ctrl1, ctrl2, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// GVerb
		SynthDef('GVerb',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, spread = 15, drylevel = 0.01, inputbw = 0.5, maxroomsize = 300, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(GVerb.ar(signal, (ctrl1 * 300).clip(1, 300), (ctrl2 * 100).clip(0.01, 100), ctrl3.clip(0.01, 1), inputbw, spread, drylevel, ctrl5.clip(0.01, 1), ctrl4.clip(0.01, 1), maxroomsize, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// JPverb
		SynthDef('JPverb',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(JPverb.ar(signal, ctrl1 * 60, ctrl2, ctrl3 , ctrl4, ctrl5 * 10));
				chain = Mix(chain  * vol + (signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// CombCFX
		SynthDef('CombCFX',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate =1;
				var chain, signal=In.ar(in, 1);
				chain = Mix(CombC.ar(signal, 0.1, ctrl1 / 10, ctrl2 * 4 * (ctrl3 - 0.5 + 0.001).sign, vol, signal * (1 - vol)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// DelayC
		SynthDef('DelayC',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, local, gate = 1;
				var chain, signal=In.ar(in, 1);
				local = LocalIn.ar(1) + signal;
				chain = Mix(DelayC.ar(local, 5.0, ctrl1 * 5.0, vol, signal * (1 - vol)));
				LocalOut.ar(chain);
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		// WarpDelay
		SynthDef('WarpDelay',
			{arg out, in, ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, vol, gate = 1;
				var chain, signal=In.ar(in, 1), buffer=LocalBuf(s.sampleRate * 4, 1).clear;
				LocalIn.ar(1).clear;
				RecordBuf.ar(signal, buffer,  recLevel: 1, preLevel: 0.333, loop: 1);
				chain = Mix(Warp1.ar(1, buffer, TRand.kr(0, 1, Dust.kr((ctrl1 * 64).clip(0.0625, 64))), (ctrl2 * 8).clip(0.125, 8), ctrl3.clip(0.01, 1), -1, (ctrl4 * 16).clip(1, 16), 0, 1, vol, signal * (1 - vol)));
				LocalOut.ar(DelayC.ar(chain, 4, ctrl5.clip(0.01, 4)));
				chain = chain * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);
				Out.ar(out, chain);
		}).add;

		/////////////////////////// Dolby5.1 or Stereo //////////////////////

		// Dolby 5.1
		SynthDef("Dolby5.1",
			{arg out=0, in, panX, panY;
				var signal, chain, front, center, lfe, rear;
				signal = In.ar(in, 1);
				//// FL FR Center LFE RL RR -> [0, 1, 2, 3, 4, 5]
				front = Pan2.ar(signal, panX, panY + 1 / 2);
				rear = Pan2.ar(signal, panX, 1 - (panY + 1 / 2));
				center = ((panX*panX) + (panY*panY))**0.5;
				center = 1 - center.clip(0, 1);
				center = signal * center;
				lfe = LPF.ar(signal, 80);
				Out.ar(out, front);
				Out.ar(out+2, center);
				Out.ar(out+3, lfe);
				Out.ar(out+4, rear);
		}).add;

		// Stereo
		SynthDef("Stereo",
			{arg out=0, in, panX, panY;
				var signal, chain;
				signal = In.ar(in, 1);
				chain = Pan2.ar(signal, panX, 1);
				Out.ar(out, chain);
		}).add;

		// MultiSpeaker
		SynthDef("MultiSpeaker",
			{arg out=0, in, panX, panY;
				var signal, chain;
				signal = In.ar(in, 1);
				chain = PanAz.ar(numberAudioOut, signal, panX, 1, widthMC, orientationMC);
				Out.ar(out, chain);
		}).add;

		// Rotate2
		SynthDef("Rotate2",
			{arg out=0, in, panX, panY;
				var signal, chain;
				signal = In.ar(in, 1);
				chain = Rotate2.ar(signal, signal, panX);
				Out.ar(out, chain);
		}).add;

		// Ambisonic
		SynthDef("Ambisonic",
			{arg out=0, in, panX, panY;
				var signal, chain, ambisonic;
				signal = In.ar(in, 1);
				ambisonic = PanB2.ar(signal, panX);
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				Out.ar(out, chain);
		}).add;

		////////////////////////////// Pre + Post Production //////////////////////

		// Synth OSC Onset
		SynthDef("WekTime Onset",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasFreq, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0,
				inputFilter, array;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				array = MFCC.kr(fft, dimIn);// 13 a 40 Bands
				detect = Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);
				# freqIn, hasFreq = Tartini.kr(inputFilter, filtre, 2048, 1024, 512, 0.5);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/WekTime_OSC_Data', values: [freqIn, ampIn, timeIn]++array, replyID: 1);
		}).add;

		// Synth OSC Pitch
		SynthDef("WekTime Pitch",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasFreq, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0,
				inputFilter, array;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				array = MFCC.kr(fft, dimIn);// 13 a 40 Bands
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);// \complex
				# freqIn, hasFreq = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/WekTime_OSC_Data', values: [freqIn, ampIn, timeIn]++array, replyID: 1);
		}).add;

		// Synth OSC Pitch2
		SynthDef("WekTime Pitch2",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, centroid=0, flatness=0.0, fft, fft2, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0, inputFilter, harmonic, percussive, array;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				array = MFCC.kr(FFT(fft, input), dimIn);// 13 a 40 Bands
				harmonic = FFT(LocalBuf(512, 1), inputFilter);
				percussive = FFT(LocalBuf(512, 1), inputFilter);
				#harmonic, percussive = MedianSeparation(fft, harmonic, percussive, 512, 5, 1, 2, 1);
				detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
				# freqIn, hasfreqIn = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/WekTime_OSC_Data', values: [freqIn, ampIn, timeIn]++array, replyID: 1);
		}).add;

		// Synth OSC Keytrack
		SynthDef("WekTime Keytrack",
			{arg busIn, seuil=0.5, filtre=0.5;
				var input, detect, freqIn, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0, key=0, array;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				array = MFCC.kr(fft, dimIn);// 13 a 40 Bands
				detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
				key = KeyTrack.kr(FFT(LocalBuf(4096, 1), input), (filtre * 4).clip(0, 4));
				if(key < 12, freqIn = (key + 60).midicps, freqIn = (key - 12 + 60).midicps);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/WekTime_OSC_Data', values: [freqIn, ampIn, timeIn]++array, replyID: 1);
		}).add;

		// Synth OSC Keyboard
		SynthDef("WekTime Keyboard",
			{arg busIn, seuil=0, filtre=0, freq=0, amp=0, trigger=0;
				var input, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0;
				timeIn = Timer.kr(trigger);
				SendReply.kr(trigger, '/WekTime_Keyboard_Data', values: [freq, amp, timeIn], replyID: 1);
		}).add;

		// Synth OSC FFT
		SynthDef("WekTime FFT",
			{arg busIn, speed=24, seuil=0.5, filtre=0.5;
				var input, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, bpm=60, flux=0, array, detect;
				input = In.ar(busIn);
				detect = Onsets.kr(FFT(LocalBuf(512, 1), input), seuil, \rcomplex);// \complex
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				array = MFCC.kr(fft, dimIn);// 13 a 40 Bands
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input));
				SendReply.kr(Impulse.kr(speed), '/WekTime_FFT_Data', values: [centroid.clip(20, 12544), energy.clip(20, 12544), flux.clip(0.0001, 1), flatness.clip(0.0001, 1), bpm], replyID: 1);
				SendReply.kr(detect, '/WekTime_FFT_Data', values: array, replyID: 2);
		}).add;

		// Synth AudioIn
		SynthDef("AudioIn",
			{arg in=0, busIn;
				Out.ar(busIn, Mix(SoundIn.ar(in)));// Out bus recording for synth
		}).add;

		// Synth FileIn
		SynthDef("FileIn",
			{arg out=0, bufferFile, busIn, trigger=1, offset=0, loop=1, volume=0;
				var file;
				file = PlayBuf.ar(2, bufferFile, BufRateScale.kr(bufferFile), trigger, BufFrames.kr(bufferFile) * offset, loop);
				Out.ar(busIn, Mix(file));// Send busIn
				Out.ar(out, file * volume);// File out
		}).add;

		// Synth AudioRec
		SynthDef("AudioRec",
			{arg busIn, bufferAudioRec, recLevel=1, preLevel=0, loop=1, trigger=1;
				RecordBuf.ar(SoundIn.ar(busIn), bufferAudioRec, offset: 0, recLevel: recLevel, preLevel: preLevel, run: 1, loop: loop, trigger: trigger, doneAction: 0);
		}).add;

		// Synth FileRec
		SynthDef("FileRec",
			{arg busIn, bufferAudioRec, recLevel=1, preLevel=0, loop=1, trigger=1;
				RecordBuf.ar(In.ar(busIn), bufferAudioRec, offset: 0, recLevel: recLevel, preLevel: preLevel, run: 1, loop: loop, trigger: trigger, doneAction: 0);
		}).add;

		// Synth Post Production
		SynthDef("MasterFX",
			{arg out=0, limit = 0.8, postAmp = 1.0;
				var chain, in;
				in = LeakDC.ar(In.ar(0, numberAudioOut));
				chain = Limiter.ar(in * postAmp, limit);
				ReplaceOut.ar(out, chain);
		}).add;

		//////////////// VST Plugin //////////////////////

		// Synth VST
		SynthDef("VST Stereo",
			{arg out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST Rotate2",
			{arg out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				//chain = Rotate2.ar(chain, chain, LFSaw.kr(dur, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Impulse.kr(bpm)), add: panLo));
				chain = Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST MultiSpeaker",
			{arg out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
				var signal, chain;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				chain = PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm).lag(bpm.reciprocal + 1)), 1, widthMC, orientationMC);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		//Synth  VST
		SynthDef("VST Ambisonic",
			{arg out=0, xFade=0.5, panLo=1.neg, panHi=1, bpm=1, gainIn=0.5;
				var signal, chain, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				//ambisonic = PanB.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)) * pi, TRand.kr(0.5.neg, 0.5, Impulse.kr(bpm)) * pi);
				ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
				chain = DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		// Synth VST
		SynthDef("VST Dolby5.1",
			{arg out=0, xFade=0.5, panLo=0, panHi=0, bpm=1, gainIn=0.5;
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

}
