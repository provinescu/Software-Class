// A Software by Herve Provini

Matrix {

	classvar  < s;

	var <> synthAnalyzeIn, busAnalyze, synthAudioIn, synthFileIn, bufferPlayFile, busFileIn, groupeAnalyse, groupeSynth, groupeMasterFX, oscMusicalData, serverAdresse, busIn, busFX, busOSC, fonctionSynthDef, cmdperiodfunc, listeGroupeSynth, masterFX, initSynthDef, createGUI, windowMasterFX, windowMasterFXLimit, windowMasterFXPostAmp, menuMatrix, bufferFile, synthAnalyseOnsets, synthAnalysePitch, synthAnalysePitch2, fonctionRecOn, fonctionRecOff, fonctionRecPause, flagRecording, windowControl, startSystem, switchAudioIn, algoAnalyse, volumeFileIn, offsetFileIn, seuilAnalyse, filtreAnalyse, fonctionLoadFileForAnalyse, parametresAnalyse, choiceSynth, addNewSynth, listeWindowSynth, fonctionWindowSynth, displayOSC, fonctionLoadSample, sourceIn, listeBusInOut, listeBusFX, sendBusIn, userOperatingSystem, listeGroupeSynthID, fonctionUserOperatingSystem, fonctionLoadSynthesizer, fonctionSaveSynthesizer, fonctionAddSynthFX, textFileAnalyze, fonctionLoadPreset, fonctionSavePreset, fonctionLoadControl, fonctionSaveControl, userOSchoiceInstrument, userOSchoiceControl, fonctionTdefControls, fonctionTdefMusicData, listeWindows, indexWindows, fonctionShortCut, fonctionCommandes, pathMatrix, system , bpmSlider, bpmOnOff, flagSystemBPM, commande, oscStateflag, masterAppAddr, slaveAppAddr, ardourOSC, oscHPtempo, oscHPstart, oscHPrec, oscState, oscTempoMaster, initOSCresponder, numberAudioOut, systemBPM, helpMatrix, fonctionOSCsynth, oscMusicData, listeDataOSC, freqBefore, dureeBefore, ampBefore, signalBuffer, timeMaximum, timeMemory, fhzFilter, ampFilter, durFilter, fhzFiltreGUI, ampFiltreGUI, durFiltreGUI, fonctionTdefOSCdata, tdefOSCdata, dureeOSCdata, chordDuree, chordSize, chordTimeSlider, chordSizeSlider, changeChoiceSynth, flagDataOSC, sliderDataOSC, recChannels, windowControlSynth, controlFreqSlider, controlFreqTranSlider, controlAmpSlider, controlDureeSlider, controlDureeTranSlider, controlQuantaSlider, fonctionSaveControlSynth, fonctionLoadControlSynth, previousFreqTrans, previousDureeTrans, previousDureeQuanta, previousFreq, previousDuree, previousAmp, previousPan, controlPanSlider, switchMenuAudioOut, switchAudioOut, windowKeyboard, keyboard, keyboardTranslate, synthKeyboard, flagKeyboard, oscKeyboardData, keyboardShortCut, setupKeyboardShortCut, musicAppAddr, startChannelAudioOut=0, switchChanelAudioOut, keyboardTranslateBefore=0, headerFormat, sampleFormat, formatRecordingMenu, headerRecordingMenu, sampleFormatRecordingMenu, algoChangePresetMenu, algoChangeMenu, varChangeMenu, midiKeyboard, oscMIDIdata, switchCanalMIDI, canalMIDI, foldersToScanAll, foldersToScanPreset, foldersToScanSynthesizer, fonctionAutomationPreset, lastMeanProbaPresetFlux=0, lastMeanProbaPresetFlatness=0, midiMenu, synthAnalyseKeyTrack, lastTimeAutomationPreset, lastNumberChoiceConfig, fonctionCollectFolders, flagCollectFolders, limitTemps, variableChange, algoChange, onOffSynth, onOffSynthValue, fluxOnFly, flatnessOnFly, keyboardVolume, keyVolume, freqMIDI, ampMIDI, dureeMIDI, lastTimeMIDI, tempoMIDI, freqCentroidMIDI, flatnessMIDI, energyMIDI, fluxMIDI, lastTimeAnalyse, midiOut, listeFileAnalyze, listeNameFileAnalyze, indexDataMusic, listeAlgorithm, flagMemory, numFhzBand, bandFHZ, lastTimeBand, menuMIDI, menuFile, menuRecording, menuOSC, menuAudio, menuAlgo, menuHelp, fonctionInitBand, freqTampon, ampTampon, windowVST, flagVST, flagMC=0, widthMC=2.0, orientationMC=0.5;

	*new	{arg path="~/Documents/Matrix/", o=2, r=2, f=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0;

		^super.new.init(path, o, r, f, devIn, devOut, size, wid, ori, flag);

	}

	init	{arg path, o, r, f, devIn, devOut, size, wid, ori, flag;

		//// Setup GUI style
		//GUI.qt;
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		pathMatrix = PathName.new(path).pathOnly;

		// Verify Path
		if(File.exists(pathMatrix).not) {systemCmd("mkdir" + pathMatrix)};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		numberAudioOut = o;
		recChannels = r;
		switchAudioOut = f;// Type Format stereo, ambisonic, etc...

		s = Server.default;

		s.options.memSize = 2**20;
		s.options.numWireBufs = 128;
		s.options.inDevice = devIn;
		s.options.outDevice = devOut;
		/*s.options.numBuffers = 1024 * 256; // increase this if you need to load more samples
		s.options.memSize = 8192 * 32; // increase this if you get “alloc failed” messages
		s.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message “too many nodes”*/
		//s.options.device = "StreamDrums LoopBack";// use a specific soundcard
		//s.options.device = "JackRouter";// use Jack if is available
		//s.options.sampleRate = nil;// use the currently selected samplerate of the select hardware
		s.options.numInputBusChannels_(20);
		s.recChannels_(recChannels);
		s.options.numOutputBusChannels_(numberAudioOut);
		s.options.hardwareBufferSize_(size);
		headerFormat = "aiff";
		s.recHeaderFormat_(headerFormat);
		sampleFormat = "float";
		s.recSampleFormat_(sampleFormat);
		flagMC = flag;
		widthMC = wid;
		orientationMC = ori;

		// Safety Limiter
		//s.options.safetyClipThreshold = 1.26; // Testing
		Safety(s);
		//Safety(s).enabled;
		//Safety.setLimit(1.neg.dbamp);

		// MIDI INIT
		// Connect first device by default
		MIDIClient.init;
		MIDIIn.connect(0, 0);
		midiOut = MIDIOut(0).latency = 0.01;
		midiOut.connect(0);

		this.run;// Run the Soft

	}

	run	{

		fonctionCollectFolders = {
			flagCollectFolders = 'on';
			// Collect all Preset
			foldersToScanAll = PathName.new(pathMatrix).files.collect{ |path| var file;
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
			flagCollectFolders = 'off';
		};
		fonctionCollectFolders.value;

		// Init variables
		listeGroupeSynth=[];
		flagRecording='off';
		switchAudioOut=0;
		listeBusInOut=[];
		listeBusFX=[];
		listeGroupeSynthID=[];
		listeWindows=[];
		indexWindows=0;
		flagSystemBPM=0;
		commande=nil;
		oscTempoMaster = 1;
		signalBuffer = 12;
		timeMaximum = 4;
		timeMemory = 4;
		fhzFilter = 0;
		ampFilter = 0;
		durFilter = 0.03125;
		dureeOSCdata = Main.elapsedTime;
		lastTimeAutomationPreset = Main.elapsedTime;
		limitTemps = 0;
		parametresAnalyse = [[0.5, 0.5], [0.5, 0.5], [0.5, 0.5], [0.5, 0.5]];
		chordDuree = 0.0833;
		chordSize = 3;
		flagDataOSC = 'on';
		canalMIDI = 0;
		lastMeanProbaPresetFlux = 0;
		lastMeanProbaPresetFlatness = 0;
		lastNumberChoiceConfig = nil;
		flagCollectFolders = 'off';
		variableChange="Flux";
		algoChange="OnFly";
		onOffSynth = 0;
		onOffSynthValue = [2.0, 2.0];//flux, flatness
		fluxOnFly = 0;
		flatnessOnFly = 0;
		keyVolume = 12.neg.dbamp;
		listeFileAnalyze=[];
		listeNameFileAnalyze=[];
		listeAlgorithm = ["Default", "Probability", "Euclide", "Genetic", "Kohonen", "Neural"];
		flagMemory = 'off';
		numFhzBand = 3; // Nombre de band de fhz (+1 pour all data) pour trier dans les synth index=0 pour all index=1 pour badnnum 1 etc...
		// bandFhz pour test dans OSC analyze 108-24 = 84 -->> range piano
		bandFHZ = Array.fill(numFhzBand, {arg i; 84 / numFhzBand * i + 24 + (84 / numFhzBand )}).midicps;
		bandFHZ = bandFHZ.add(127.midicps);
		bandFHZ = bandFHZ.reverse;
		bandFHZ = bandFHZ.add(0.midicps);
		bandFHZ = bandFHZ.reverse;
		listeDataOSC=[];
		indexDataMusic = [];
		lastTimeBand = [];
		// Init Array
		(numFhzBand + 1).do({arg i;
			listeDataOSC = listeDataOSC.add([]);
			indexDataMusic = indexDataMusic.add(0);
			lastTimeBand = lastTimeBand.add(Main.elapsedTime);
		});
		flagVST = 'off';

		choiceSynth = [
			'Add a New Synthesizer or FX',
			//////// Synthese witout sample
			'SYNTHESE (',
			'SinOsc',
			'SinOscVibrato',
			'SynthFM',
			'SynthBusFM',
			'Formant',
			'SawSynth',
			'Guitare',
			'Blip',
			/*"Osc",
			"VOsc",
			"VOsc3",*/
			'VarSaw',
			'Pulse',
			'Klang',
			'Klank',
			'Klank2',
			'Gendy3',
			'Spring',
			//////// Sampler one sample
			'SAMPLER 1 BUFFER (',
			'SamplerSynth',
			'LoopBuf',
			'GrainBuf',
			'HPbufRd',
			'HPplayBuf',
			'HPplayBufBusFM',
			'HPplayBufVibrato',
			'HPtGrains',
			'Trig1PlayBuf',
			'HPplayBufResonz',
			'Synthesizer',
			'HPplayBufMedianLeakDC',
			'FreqShift',
			'PitchShift',
			'Squiz',
			'WaveLoss',
			'Warp1',
			'Granulation',
			'Toupie',
			'Elastic',
			'RandomElastic',
			'RandKlankSample',
			'DjScratch',
			'LiquidFilter',
			//////// FFT 1 buffer
			'FFT 1 BUFFER (',
			'PV_HPshiftDown',
			//'PV_HPecartType',
			'PV_HPfiltre',
			'PV_MagNoise',
			'PV_MagClip',
			'PV_MagSmooth',
			'PV_MagSmear',
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
			'PV_Cutoff',
			//////// FFT 2 buffer
			'FFT 2 BUFFER (',
			'PV_Max',
			'PV_Min',
			'PV_Add',
			'PV_Mul',
			'PV_MagDiv',
			'PV_CopyPhase',
			'PV_RandWipe',
			'PV_BinWipe',
			'PV_RectComb2',
			'PV_Morph',
			'Convolution',
			'Concat',
			//////// Piano + FFT Piano
			'PIANO (',
			'MdaPiano',
			'Piano Synthesizer',
			'Piano Resonz',
			'Piano FreqShift',
			'Piano PitchShift',
			'Piano Squiz',
			'Piano Waveloss',
			'Piano PV_HPshiftDown',
			//'Piano PV_HPecartType',
			//'Piano PV_HPfiltre',
			'Piano PV_MagNoise',
			'Piano PV_MagClip',
			'Piano PV_MagSmooth',
			'Piano PV_MagSmear',
			'Piano PV_Diffuser',
			'Piano PV_BrickWall',
			'Piano PV_LocalMax',
			'Piano PV_MagSquared',
			'Piano PV_MagBelow',
			'Piano PV_MagAbove',
			'Piano PV_RandComb',
			'Piano PV_MagShift',
			'Piano PV_BinScramble',
			'Piano PV_BinShift',
			'Piano PV_RectComb',
			'Piano PV_ConformalMap',
			'Piano PV_Compander',
			//////// FFT Piano + Sample
			'FFT PIANO + SAMPLE (',
			'Piano PV_Max',
			'Piano PV_Min',
			'Piano PV_Add',
			'Piano PV_Mul',
			'Piano PV_MagDiv',
			'Piano PV_CopyPhase',
			'Piano PV_RandWipe',
			'Piano PV_BinWipe',
			'Piano PV_RectComb2',
			'Piano PV_Morph',
			'Piano Convolution',
			//////// FX
			'FX (',
			'AllpassC',
			'FreeVerb',
			'GVerb',
			'JPverb',
			'DelayC',
			'Trig1',
			'BPF',
			'BRF',
			'MidEQ',
			'TwoPole',
			'CombC',
			'RHPF',
			'RLPF',
			'Ringz',
			'Formlet',
			'Resonz',
			'DynKlank',
			'Median',
			'LeakDC',
			'Median+LeakDC',
			'PitchShiftFX',
			'WarpDelay',
			'DJ_FX',
			'PV_MagFreeze',
			'PV_PlayBuf',
			'PV_BinPlayBuf',
			'PV_HPshiftDownFX',
			//'PV_HPecartTypeFX',
			'PV_HPfiltreFX',
			'PV_MagNoiseFX',
			'PV_MagClipFX',
			'PV_MagSmoothFX',
			'PV_MagSmearFX',
			'PV_DiffuserFX',
			'PV_BrickWallFX',
			'PV_LocalMaxFX',
			'PV_MagSquaredFX',
			'PV_MagBelowFX',
			'PV_MagAboveFX',
			'PV_RandCombFX',
			'PV_MagShiftFX',
			'PV_BinScrambleFX',
			'PV_BinShiftFX',
			'PV_RectCombFX',
			'PV_ConformalMapFX',
			'PV_CompanderFX',
			'PV_SpectralEnhanceFX',
			'PV_MagStretchFX',
			'PV_MagShift+StretchFX',
			'ConvolutionFX',
			'PV_CutoffFX',
			//////// SYNTH + FX Special
			'SPECIAL SYNTH + FX (',
			'SpecialFX',
			//////// END SYNTH AND FX
			'END (',
		];

		changeChoiceSynth = choiceSynth.copy;
		changeChoiceSynth.put(0, 'Change Synthesizer or FX');

		userOSchoiceInstrument=[
			"User Operating System",
			"Load and Add Synth",//  follow by a number",
			"Load and Close Synth",//	 follow by a number",
			"Load and Add Preset",//  follow by a number",
			"Load and Close Preset",//	      follow by a number",
			"Save Synth",//	      follow by a number",
			"Save Preset",//	      follow by a number",
			"Copy Synth",
			"Copy Preset",
			"Close All Synth Window",
		];
		userOSchoiceControl=[
			"User Operating System",
			"Load and Add Synth",//  follow by a number",
			"Load and Close Synth",//	      follow by a number",
			"Load and Add Preset",//  follow by a number",
			"Load and Close Preset",//	      follow by a number",
			"Save Synth",//	      follow by a number (",
			"Save Preset",//	      follow by a number",
			"Copy Synth",
			"Copy Preset",
			"Close All Synth Window",
		];

		helpMatrix="
Single commandes:

esc	or SpaceBar			System on/off.
b						Switch recording buffer data OSC on/off.
alt + c					Copy Preset.
C						Copy Synthesizer.
d				 		Temporal Synchronizing Synthesizer.
f				 		Load sound file for analyser.
F				 		Sound file analyser  Loop On.
alt + f					Sound file analyse Loop Off.
ctrl + f		 		Load and Add sound file for analyse.
h						Switch Source In.
i						Close all synthesizer.
alt + i					Clear musical data (OSC data).
k						New Environment.
m						Switch Algo Tempo.
N						Add a New Synthesizer (SinOsc by default).
o						Switch Automation Preset on/off.
P						Play all Synthesizer.
alt + P					Stop all Synthesizer.
p						Play Synthesizer on front.
alt + p					Stop Synthesizer on front.
q						Switch Algo Analyze.
ctrl + alt + r			Start Recording.
R						Switch Pause Recording on/off.
ctrl + alt + r			Stop Recording.
w / ctrl + w			Windows navigation.
alt + w					Window Control Panel.
y						Display NodesTree.
z						Load Random Preset.
Z						Load Random Synthesizer.
Commandes follow by a numerical key (0,..9 ; shift 0,..9 ; alt 0,..9 ; alt + shift 0,..9):

l			 			Load Preset.
L						Load Synthesizer.
ctrl + l		     	Load preset without close others synthesizer.
ctrl + L			    Load Synthesizer without close others synthesizer.
J						Save OSCmusicData.
j						Load OSCmusicData.
s				 		Save preset.
S				 		Save synthesizer.
f						Switch Sound File for Analyze.


Keyboard Commandes:

y ... -					Musical keys.
/  *						Keyboard transpose down / up.
";

		///////////////////////// Menu Matrix /////////////////////////////////

		menuFile = Menu(
			MenuAction("Load File for Analyze",
				{Dialog.openPanel({ arg path;
					listeFileAnalyze.do({arg buffer; buffer.free});
					listeFileAnalyze=[];
					listeNameFileAnalyze=[];
					bufferFile.free;
					fonctionLoadFileForAnalyse.value(path);
					// Setup GUI Value
					windowControl.view.children.at(6).string = "FileIn :" + PathName.new(path).fileName},{"cancelled".postln})}),
			Menu(
				MenuAction("On", {synthFileIn.set('loop', 1)}),
				MenuAction("Off", {synthFileIn.set('loop', 0)});
			).title_("Loop");
		);
		MainMenu.register(menuFile.title_("File for Analyze"), "MatrixTools");

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
		MainMenu.register(menuRecording.title_("Recording"), "MatrixTools");

		menuAudio= Menu(
			MenuAction("Recording Channels", {
				SCRequestString("2", "Channels", {arg strg; recChannels = strg.asInteger;
					this.initSynthDef;
				});
			}),
			MenuAction("Number Audio Out Channels", {
				SCRequestString("2", "Channels", {arg strg; numberAudioOut = strg.asInteger;
					s.options.numOutputBusChannels_(numberAudioOut);
					this.initSynthDef;
					// Creation MasterFX
					masterFX.free;
					masterFX = Synth.new("MasterFX", [\limit, 0.8], groupeMasterFX, \addToTail)});
			}),
			MenuAction("Start Channel Audio Out", {
				SCRequestString("0", "Channel", {arg strg; startChannelAudioOut = strg.asInteger});
			}),
			Menu(
				MenuAction("Stereo", {
					switchAudioOut=0; this.initSynthDef}),
				MenuAction("MultiSpeaker", {
					switchAudioOut=2; this.initSynthDef}),
				MenuAction("Rotate2", {
					switchAudioOut=1; this.initSynthDef}),
				MenuAction("Ambisonic", {
					switchAudioOut=3; this.initSynthDef})
			).title_("Format");
		);
		MainMenu.register(menuAudio.title_("Audio"), "MatrixTools");

		menuAlgo = Menu(
			Menu(
				MenuAction("Flux", {
					variableChange = "Flux"; windowControl.view.children.at(34).value_(onOffSynthValue.at(0));
				}),
				MenuAction("Flatness", {
					variableChange="Flatness"; windowControl.view.children.at(34).value_(onOffSynthValue.at(1));
				});
			).title_("Variable"),
			Menu(
				MenuAction("Probability", {
					algoChange="Probability";
				}),
				MenuAction("OnFly", {
					algoChange="OnFly";
				});
			).title_("Algorithm");
		);
		MainMenu.register(menuAlgo.title_("Automation"), "MatrixTools");

		menuMIDI = Menu(
			MenuAction("Init", {
				MIDIClient.init;
				if(MIDIClient.externalSources != [ ], {
					// Connect first device by default
					MIDIIn.connect(0, 0);
					midiOut = MIDIOut(0);
					//midiOut.connect(0);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)});

					});
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
						16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)});

						});
					});
				});
			).title_("Setting");
		);
		MainMenu.register(menuMIDI.title_("Midi"), "MatrixTools");


		menuOSC = Menu(
			MenuAction("Setting", {var addrM, addrS;
				// Set OSC Addresse et Port Master
				addrM=NetAddr.localAddr;
				addrS=NetAddr.localAddr;
				slaveAppAddr.disconnect;
				/*oscHPtempo.free;
				oscHPstart.free;
				oscHPrec.free;*/
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
			MenuAction("OSC Master", {oscStateflag='master';
				"NewDensity is OSC now !".postln;
			}),
			MenuAction("OSC Slave", {oscStateflag='slave';
				"NewDensity is OSC SLAVE now !".postln;
			}),
			MenuAction("OSC Off", {oscStateflag='off';
				"OSC is OFF now !".postln;
			});
		);
		MainMenu.register(menuOSC.title_("OSC"), "MatrixTools");

		menuHelp = MenuAction("ShortCut", {
			//Document.new("ShortCut for Matrix", helpMatrix);
			TextView().name_("ShortCut for Matrix").string_(helpMatrix).front;
		});
		MainMenu.register(menuHelp, "MatrixTools");

		fonctionUserOperatingSystem = {arg item, window;
			var data, tampon, dataControlSynth;
			item.value.switch(
				0, {nil},
				// Load Synthesizer and add
				1, {
					Dialog.openPanel({ arg paths;
						var f, tampon;
						tampon = listeDataOSC;
						f=File(paths,"r");
						data = f.readAllString.interpret;
						f.close;
						//Document.listener.string="";
						s.queryAllNodes;
						listeWindows.at(3).front;
						indexWindows=3;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
						/*// Init Band for Synth
						fonctionInitBand.value(numFhzBand);*/
					},
					{"cancelled".postln});
				},
				// Load Synthesizer and Close Others
				2, {
					Dialog.openPanel({ arg paths;
						var f, tampon, data, dataControlSynth;
						tampon = listeDataOSC;
						fonctionUserOperatingSystem.value(9);
						f=File(paths,"r");
						data = f.readAllString.interpret;
						f.close;
						//Document.listener.string="";
						s.queryAllNodes;
						listeWindows.at(3).front;
						indexWindows=3;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
						/*// Init Band for Synth
						fonctionInitBand.value(numFhzBand);*/
					},
					{"cancelled".postln});
				},
				// Load Preset and add
				3, {
					Dialog.openPanel({ arg paths;
						var f, tampon;
						tampon = listeDataOSC;
						f=File(paths,"r");
						fonctionLoadPreset.value(f.readAllString.interpret);
						f.close;
						windowControl.name="Matrix Control" + " | "+ PathName.new(paths).fileName;
						//Document.listener.string="";
						s.queryAllNodes;
						listeWindows.at(3).front;
						indexWindows=3;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
					}, {"cancelled".postln});
				},
				// Load Preset and Close Others
				4, {
					Dialog.openPanel({ arg paths;
						var f, tampon;
						tampon = listeDataOSC;
						fonctionUserOperatingSystem.value(9);
						f=File(paths,"r");
						fonctionLoadPreset.value(f.readAllString.interpret);
						f.close;
						windowControl.name="Matrix Control" + " | " + PathName.new(paths).fileName;
						//Document.listener.string="";
						s.queryAllNodes;
						listeWindows.at(3).
						front;indexWindows=3;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
					},
					{"cancelled".postln});
				},
				// Save Synthesizer
				5, {
					if(window.name.containsStringAt(0, "Matrix Control").not and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not},
						{
							Dialog.savePanel({arg path;
								var name, pathonly, fileName, f;
								path = PathName.new(path);
								pathonly = path.pathOnly;
								name = path.fileName;
								name = "synthesizer"+name;
								path = pathonly++name;
								fileName = PathName.new(path).fileName;
								path = PathName.new(path).fullPath;
								f=File(path ++ ".scd", "w");
								data = fonctionSaveSynthesizer.value(window);
								data = data.add(fonctionSaveControlSynth.value(windowControlSynth));// Save ControlSynth Panel
								data = data.add(fonctionSaveControl.value(windowControl));// Save Control Panel
								data = data.add(listeDataOSC.value);//Save OSCmusicData
								f.write(data.asCompileString);
								f.close;
							},
							{"cancelled".postln});
					});
				},
				// Save Preset
				6, {
					Dialog.savePanel({arg path;
						var name, pathonly, fileName, f;
						path = PathName.new(path);
						pathonly = path.pathOnly;
						name = path.fileName;
						name = "preset"+name;
						path = pathonly++name;
						fileName = PathName.new(path).fileName;
						path = PathName.new(path).fullPath;
						f=File(path ++ ".scd", "w");
						f.write(fonctionSavePreset.value(listeWindowSynth).asCompileString);
						f.close;
						windowControl.name="Matrix Control" + " | " + fileName},
					{"cancelled".postln});
				},
				// Copy Synthesizer
				7, {
					if(window.name.containsStringAt(0, "Matrix Control").not and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not},
						{
							tampon = listeDataOSC;
							data = fonctionSaveSynthesizer.value(window);
							fonctionLoadSynthesizer.value(data);
							//Document.listener.string="";
							s.queryAllNodes;
							listeDataOSC = tampon;
							/*// Init Band for Synth
							fonctionInitBand.value(numFhzBand);*/
					});
				},
				// Copy Preset
				8, {
					data = fonctionSavePreset.value(listeWindowSynth);
					fonctionLoadPreset.value(data);
					//Document.listener.string="";
					// Setup Font View Synth
					listeWindowSynth.do({arg window;
						window.view.do({arg view;
							view.children.do({arg subView;
								subView.font = Font("Helvetica", 9);
							});
						});
					});
					s.queryAllNodes;
				},
				// Close All Windows
				9, {
					listeWindowSynth.do({arg window; window.close});
				}
			)
		};

		// Fonction pour Recording
		fonctionRecOn={
			if(oscStateflag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
			if(flagRecording == 'off', {
				flagRecording = 'on';
				s.bind{
					s.recChannels_(recChannels);
					s.sync;
					s.recHeaderFormat_(headerFormat);
					s.sync;
					s.recSampleFormat_(sampleFormat);
					s.sync;
					s.prepareForRecord("~/Music/SuperCollider Recordings/".standardizePath ++ "Matrix_" ++ Date.localtime.stamp ++ "." ++ headerFormat);
					s.sync;
					s.record;
					s.sync;
				};
			});
		};

		fonctionRecOff={
			if(oscStateflag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec On
			flagRecording = 'off';
			s.stopRecording;
		};

		fonctionRecPause={
			if(oscStateflag == 'master', {slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec On
			if(startSystem.value == 1, {
				if(flagRecording == 'on', {s.pauseRecording; flagRecording = 'pause'},{s.record; flagRecording = 'on'});
			});
		};

		fonctionLoadSample={arg p, synth, buffer;
			var f, d, b;
			s.bind{
				buffer.free;
				s.sync;
				f = SoundFile.new;
				s.sync;
				f.openRead(p);
				s.sync;
				if(f.numChannels == 1, {
					Post << "Loading mono sound" << p << Char.nl;
					s.sync;
					b=Buffer.read(s, p);
					s.sync;
				}, {
					d = FloatArray.newClear(f.numFrames * 2);
					s.sync;
					f.readData(d);
					s.sync;
					Post << "Loading stereo sound" << p << Char.nl;
					s.sync;
					d = d.unlace(2).sum / 2;
					s.sync;
					b = Buffer.loadCollection(s, d, 1);
					s.sync;
				});
				f.close;
				s.sync;
			};
			b;// sortie buffer
		};

		fonctionSaveSynthesizer = {arg window;
			var data=[];
			// Save views values
			window.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[], subType;
				// StaticText
				if(item == 2 or: {item == 8} or: {item == 12} or: {item == 16} or: {item == 22} or: {item == 32} or: {item == 37} or: {item == 45} or: {item == 47} or:{item == 50} or:{item == 65},
					{data = data.add(view.string)});
				// EZNumber
				if(item == 9 or: {item == 10},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)}, 		{arrayData=arrayData.add	(subView.value)})});
					data = data.add(arrayData)});
				// EZSlider
				arrayData=[];
				if(item == 13 or: {item == 14} or: {item == 15} or: {item == 23} or: {item == 24} or: {item == 25} or: {item == 33} or: {item == 34} or: {item == 35} or: {item == 40} or: {item == 43} or: {item == 44} or: {item == 53} or: {item == 56}  or:{item == 80},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)},
							{arrayData=arrayData.add(subView.value)})});
					data = data.add(arrayData)});
				// EZRanger
				arrayData=[];
				if(item == 38 or: {item == 39} or: {item == 41} or: {item == 42} or: {item == 52} or: {item == 55},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)});
						if(subItem == 1 or: {subItem == 3}, {arrayData=arrayData.add(subView.value)});
						if(subItem == 2, {subArrayData=subArrayData.add		(subView.lo);subArrayData=subArrayData.add(subView.hi);
							arrayData=arrayData.add(subArrayData)})});
					data = data.add(arrayData)});
				// All others Sliders
				if(item == 0 or: {item == 1} or: {item == 3} or: {item == 4} or: {item == 5} or: {item == 6} or: {item == 7} or: {item == 11}
					or: {item == 17} or: {item == 18} or: {item == 19} or: {item == 20} or: {item == 21} or: {item == 26}
					or: {item == 27} or: {item == 28} or: {item == 29} or: {item == 30} or: {item == 31} or: {item == 36}
					or: {item == 46} or: {item == 48} or: {item == 49} or: {item == 51} or: {item == 54} or: {item == 57} or: {item == 58} or: {item == 59} or: {item == 60} or: {item == 61} or: {item == 62} or: {item == 63}  or: {item == 64} or:{item == 66} or:{item == 67} or:{item == 68} or:{item == 69} or:{item == 70} or:{item == 71} or:{item == 72} or:{item == 73} or:{item == 74} or:{item == 75} or:{item == 76} or:{item == 77} or:{item == 78} or:{item == 79} or:{item == 81},
					{data = data.add(view.value)});
				// Composite View (Degress)
				if(item == 82,
					{
						view.children.do({arg subView;
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
			});
			data=data.add(window.name);// Nom du Synthesizer
			// Sortie Fonction Save Synthesizer
			data.value;
		};

		fonctionLoadSynthesizer = {arg data;
			var name, index, buffer1, buffer2, canalIn, timeBuf1, timeBuf2;
			// Set name Synthesizer or FX
			name = data.last.split($[).at(0);
			choiceSynth.do({arg synth, item; if(name == synth.asString, {index = item})});
			// Set Buffer 1 et 2
			buffer1 = data.at(22);
			buffer2 = data.at(32);
			canalIn = data.at(4);
			timeBuf1 = data.at(20);
			timeBuf2 = data.at(30);
			fonctionAddSynthFX.value(index, buffer1, buffer2, canalIn, timeBuf1, timeBuf2);
			listeWindowSynth.last.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[];
				// StaticText
				if(item == 2 or: {item == 8} or: {item == 12} or: {item == 16} or: {item == 37} or: {item == 45} or: {item == 47} or:{item == 50} or:{item == 65},	{nil});
				// Load Buffer One and Two
				if(item == 22 or: {item == 32},
					{
						view.string_(data.at(item));
				});
				// EZSlider
				arrayData=[];
				if(item == 13 or: {item == 14} or: {item == 15} or: {item == 23} or: {item == 24} or: {item == 25} or: {item == 33} or: {item == 34} or: {item == 35} or: {item == 40} or: {item == 43} or: {item == 44} or: {item == 53} or: {item == 56} or:{item == 80},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {nil},
							{subView.valueAction_(data.at(item).at(subItem).value)})});
				});
				// EZRanger
				arrayData=[];
				if(item == 38 or: {item == 39} or: {item == 41} or: {item == 42} or: {item == 52} or: {item == 55},
					{
						view.children.do({arg subView, subItem;
							if(subItem == 0, {nil});
							if(subItem == 1 or: {subItem == 3}, {subView.valueAction_(data.at(item).at(subItem).value)});
							if(subItem == 2, {subView.activeHi = data.at(item).at(subItem).at(1).value;
								subView.activeLo = data.at(item).at(subItem).at(0).value;
							});
						});
				});
				// Play
				if(item == 0, {
					if(data.at(item).value == 0, {
						s.bind{
							view.valueAction_(1);
							s.sync;
							view.valueAction_(0);
							s.sync;
						};
					}, {view.valueAction_(1)});
				});
				// User Operation + nodes
				if(item == 1 or: {item == 9} or: {item == 10} or: {item == 11}, {nil});
				// Load Sample 1 et 2
				if(item == 18 or: {item == 28}, {nil});
				// Duree sample 1 et 2
				if(item == 20 or: {item == 30}, {
					//view.valueAction_(data.at(item).value);
				});
				//Bus In no evaluation
				if(item == 4, {view.value_(data.at(item))});
				// All others Sliders
				if(item == 0 or: {item == 3} or: {item == 5} or: {item == 6} or: {item == 7}
					or: {item == 17} or: {item == 19} or: {item == 21} or: {item == 26}
					or: {item == 27} or: {item == 29} or: {item == 31} or: {item == 36}
					or: {item == 46} or: {item == 48} or: {item == 49} or: {item == 51} or: {item == 54} or: {item == 57} or: {item == 58} or: {item == 59} or: {item == 60} or: {item == 61} or: {item == 62} or: {item == 64} or:{item == 66} or:{item == 67} or:{item == 68} or:{item == 69} or:{item == 70} or:{item == 71} or:{item == 72} or:{item == 73} or:{item == 74} or:{item == 75} or:{item == 76} or:{item == 77} or:{item == 78} or:{item == 79} or:{item == 81},
					{view.valueAction_(data.at(item))});
				// MIDI
				if(item == 63, {view.valueAction_(0); view.valueAction_(data.at(item))});
				// Degrees
				if(item == 82, {view.children.at(1).valueAction_(data.at(item).at(2).at(1))});
			});
			// Setup Font View Synth
			listeWindowSynth.do({arg window;
				window.view.do({arg view;
					view.children.do({arg subView;
						subView.font = Font("Helvetica", 9);
					});
				});
			});
		};

		fonctionSavePreset = {arg listeWindow;
			var data=[], orderListeWindow=[];
			// Trier les synth dans le bon ordre
			listeGroupeSynthID.do({arg id;
				listeGroupeSynth.do({arg synth, item;
					if(synth.nodeID == id, {orderListeWindow = orderListeWindow.add	(listeWindow.at(item))});
				});
			});
			orderListeWindow.do({arg window;
				data = data.add(fonctionSaveSynthesizer.value(window))}); // Synth
			data = data.add(fonctionSaveControlSynth.value(windowControlSynth));// Save ControlSynth Panel
			data = data.add(fonctionSaveControl.value(windowControl));// Save Control Panel
			data = data.add(listeDataOSC.value);//Save OSCmusicData
			// Sortie Fonction Save Preset
			data.value;
		};

		fonctionLoadPreset = {arg preset, dataControlSynth, tampon;
			tampon = preset.last;// Load OSCmusicData
			preset.remove(preset.last);// Remove OSCmusicData
			fonctionLoadControl.value(windowControl, preset.last);//Load Control Panel
			preset.remove(preset.last);// Remove control panel
			dataControlSynth = preset.last; // ControlSynth Panel
			fonctionLoadControlSynth.value(windowControlSynth, preset.last);//Load ControlSynth Panel
			preset.remove(preset.last);// Remove controlSynth panel
			preset.do({arg data; fonctionLoadSynthesizer.value(data)});// Load Synthesizer
			listeDataOSC = tampon;
			/*// Init Band for Synth
			fonctionInitBand.value(numFhzBand);*/
		};

		fonctionSaveControl = {arg window;
			var data=[];
			window.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[];
				// StaticText + TextView
				if(item == 0 or: {item == 3} or: {item == 4} or: {item == 9} or: {item == 10} or: {item == 29} or: {item == 31} or: {item == 32},
					{data = data.add(view.string)});
				// EZSlider
				arrayData=[];
				if(item == 7 or: {item == 16} or: {item == 17} or: {item == 18} or: {item == 19} or: {item == 20} or: {item == 21} or: {item == 22} or: {item == 23} or: {item == 24} or: {item == 25} or: {item == 26} or: {item == 27} or: {item == 28} or: {item == 35},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)},
							{arrayData=arrayData.add(subView.value)})});
					data = data.add(arrayData)});
				// All others Sliders
				if(item == 1 or: {item == 2} or: {item == 5} or: {item == 6} or: {item == 8} or: {item == 11} or: {item == 12} or: {item == 13} or: {item == 14} or: {item == 15} or: {item == 30} or: {item == 33} or: {item == 34},
					{data = data.add(view.value)});
			});
			// Sortie Fonction Save Control
			data.value;
		};

		fonctionLoadControl = {arg window, data=[];
			windowControl.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[];
				// StaticText
				if(item == 0 or: {item == 3} or: {item == 4} or: {item == 9} or: {item == 10} or: {item == 29} or: {item == 31} or: {item == 32}, // or: {item == 30} or: {item == 33} or: {item == 34},
					{nil});
				// EZSlider
				arrayData=[];
				if(item == 21 or: {item == 22} or: {item == 23} or: {item == 24} or: {item == 25} or: {item == 26} or: {item == 27} or: {item == 28}  or: {item == 35} or: {item == 7} or: {item == 16} or: {item == 17} or: {item == 18} or: {item == 34},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {nil},
							{subView.valueAction_(data.at(item).at(subItem).value)})});
				});
				// All others Sliders
				if(item == 13 or: {item == 8} or: {item == 14} or: {item == 15},
					{view.valueAction_(data.at(item).value)});
				// No Action
				if(item == 1 or: {item == 2} or: {item == 5} or: {item == 6} or: {item == 11} or: {item == 12} or: {item == 19} or: {item == 20} or: {item == 30} or: {item == 33},
					{nil});
			});
		};

		fonctionSaveControlSynth = {arg window;
			var data=[];
			window.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[];
				// EZSlider
				arrayData=[];
				if(item == 2 or: {item == 5} or: {item == 6},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)},
							{arrayData=arrayData.add(subView.value)})});
					data = data.add(arrayData)});
				// EZRanger
				arrayData=[];
				if(item == 0 or: {item == 1} or: {item == 3} or: {item == 4},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {arrayData=arrayData.add(subView.string)});
						if(subItem == 1 or: {subItem == 3}, {arrayData=arrayData.add(subView.value)});
						if(subItem == 2, {subArrayData=subArrayData.add		(subView.lo);subArrayData=subArrayData.add(subView.hi);
							arrayData=arrayData.add(subArrayData)})});
					data = data.add(arrayData)});
			});
			// Sortie Fonction Save Control
			data.value;
		};

		fonctionLoadControlSynth = {arg window, data=[];
			window.view.children.do({arg view, item;
				var arrayData=[], subArrayData=[];
				// EZSlider
				arrayData=[];
				if(item == 2 or: {item == 5} or: {item == 6},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {nil},
							{subView.valueAction_(data.at(item).at(subItem).value)})});
				});
				// EZRanger
				arrayData=[];
				if(item == 0 or: {item == 1} or: {item == 3} or: {item == 4},
					{view.children.do({arg subView, subItem;
						if(subItem == 0, {nil});
						if(subItem == 1 or: {subItem == 3}, {subView.valueAction_(data.at(item).at(subItem).value)});
						if(subItem == 2, {subView.range(data.at(item).at(subItem).value)})});
				});
			});
		};

		fonctionAddSynthFX = {arg item, buffer1, buffer2, canalIn, timeBuf1, timeBuf2;
			// New Group
			listeGroupeSynth=listeGroupeSynth.add(Group.new(groupeSynth, \addToTail));
			listeGroupeSynthID = listeGroupeSynthID.add(listeGroupeSynth.at(listeGroupeSynth.size - 1).nodeID);
			// New Window
			listeWindowSynth=listeWindowSynth.add(fonctionWindowSynth.value(choiceSynth.at(item.value), listeGroupeSynth.at(listeGroupeSynth.size - 1), item.value, buffer1, buffer2, canalIn, timeBuf1, timeBuf2));
			// Init Band for Synth
			fonctionInitBand.value(numFhzBand);
			//Document.listener.string="";
			// Setup Font View Synth
			listeWindowSynth.do({arg window;
				window.view.do({arg view;
					view.children.do({arg subView;
						subView.font = Font("Helvetica", 9);
					});
				});
			});
			s.queryAllNodes;
		};

		fonctionInitBand = {arg band;
			// bandFhz pour test dans OSC analyze 108-24 = 84 -->> range piano
			bandFHZ = Array.fill(numFhzBand, {arg i; 84 / numFhzBand * i + 24 + (84 / numFhzBand )}).midicps;
			bandFHZ = bandFHZ.add(127.midicps);
			bandFHZ = bandFHZ.reverse;
			bandFHZ = bandFHZ.add(0.midicps);
			bandFHZ = bandFHZ.reverse;
			listeDataOSC=[];
			indexDataMusic = [];
			lastTimeBand = [];
			// Init Array
			(numFhzBand + 1).do({arg i;
				listeDataOSC = listeDataOSC.add([]);
				indexDataMusic = indexDataMusic.add(0);
				lastTimeBand = lastTimeBand.add(Main.elapsedTime);
			});
			for(0, numFhzBand,
				{arg index;
					listeWindowSynth.do({arg w;
						w.view.children.at(66 + index).enabled_(true);
					});
			});
			if(numFhzBand < 12, {
				for(numFhzBand + 1, 12,
					{arg index;
						listeWindowSynth.do({arg w;
							w.view.children.at(66 + index).enabled_(false);
							w.view.children.at(66 + index).valueAction_(0);
						});
				});
			});
		};

		// Run Soft
		s.waitForBoot({

			this.initSynthDef;// Init SynthDef
			this.createGUI;// Create GUI windows

			systemBPM = TempoClock.default;

			// Bus Audio pour Analyze
			busAnalyze = Bus.audio(s, 1);

			// Bus In + FX
			32.do({arg index;
				listeBusInOut = listeBusInOut.add(Bus.audio(s, 1));
				listeBusFX = listeBusFX.add(Bus.audio(s, 1));
			});
			s.sync;

			// Bus OSC Data (freq, amp, duree, tempo, freqCentroid, flatness, energy, flux)
			// Init Bus Array max 12
			(12 + 1).do({arg i;
				busOSC = busOSC.add(Bus.control(s, 8));
			});
			s.sync;

			// Group
			groupeAnalyse=Group.new(s, \addToTail);
			groupeSynth=Group.new(s, \addToTail);
			groupeMasterFX=Group.new(s, \addToTail);

			//Fonction Load file for analyze
			fonctionLoadFileForAnalyse={arg p;
				var f, d;
				s.bind{
					synthFileIn.set(\trigger, 0);
					s.sync;
					//synthFileIn.run(false);
					//s.sync;
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
							bufferFile=Buffer.loadCollection(s, d, 2);
							s.sync;
						},
						{Post << "Loading sound stereo for analyze" << p << Char.nl;
							s.sync;
							bufferFile=Buffer.readChannel(s, p, channels: [0, 1]);
							s.sync;
					});
					f.close;
					s.sync;
					synthFileIn.set(\bufferplay, bufferFile);
					s.sync;
					//synthFileIn.run(true);
					//s.sync;
					synthFileIn.set(\trigger, 1);
					s.sync;
					//textFileAnalyze.string_(PathName.new(p).fileName);
					textFileAnalyze.string_(p);
				};
				bufferFile.value;
			};

			fonctionLoadFileForAnalyse.value(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");

			s.bind{

				// Synth Analyze In
				synthAnalyzeIn=Synth.newPaused("Matrix AnalyzeIn",
					[\in, 0, 'busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				//// Synth AudioIn
				//synthAudioIn=Synth.new("Matrix AudioIn",
				// [\in, 0, 'busIn', listeBusInOut.at(0)], groupeAnalyse, \addToTail);
				//s.sync;

				// Synth play file
				synthFileIn=Synth.newPaused("Matrix Play File",
					[\bufferplay, bufferFile, 'busIn', listeBusInOut.at(0), 'busAnalyze', busAnalyze.index, \volume, 0.0], groupeAnalyse, \addToTail);
				s.sync;

				// Synth audio analyze Pitch1
				synthAnalysePitch = Synth.newPaused("OSC Matrix Pitch",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// Synth audio analyze Pitch2
				synthAnalysePitch2 = Synth.newPaused("OSC Matrix Pitch2",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// Synth audio analyze Onsets
				synthAnalyseOnsets = Synth.newPaused("OSC Matrix Onsets",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// Synth audio analyze KeyTrack
				synthAnalyseKeyTrack = Synth.newPaused("OSC Matrix KeyTrack",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// Synth Keyboard
				synthKeyboard = Synth.newPaused("OSC Matrix Keyboard",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// MIDI Keyboard
				midiKeyboard = Synth.newPaused("OSC Matrix MIDI",
					['busAnalyze', busAnalyze.index], groupeAnalyse, \addToTail);
				s.sync;

				// Creation MasterFX
				masterFX = Synth.new("MasterFX", [\limit, 0.8], groupeMasterFX, \addToTail);
				s.sync;

				// VST
				~synthVST = Synth.newPaused("VST Plugin", [\xFade, 0.5, \gainIn, 0.5], groupeMasterFX, \addToHead).map(\freq, busOSC.at(0));// Attention map bus systemBPM not a bus !!!!!!
				~fxVST = VSTPluginController(~synthVST);
				s.sync
			};

			//// Init Audio In
			//startSystem.valueAction_(1);
			//switchAudioIn.valueAction_(1);
			//switchAudioIn.valueAction_(0);
			//startSystem.valueAction_(0);

			// OSC Setting
			serverAdresse = s.addr; // Adresse Server -> NetAddr(0.0.0.0, 0)
			masterAppAddr = NetAddr.localAddr;
			slaveAppAddr = NetAddr.localAddr;
			musicAppAddr = NetAddr.localAddr;

			oscStateflag = 'off';

			ardourOSC = NetAddr("127.0.0.1", 3819);// define NetAddr on local machine with Ardour's port number

			freqBefore=0; ampBefore=0; dureeBefore=0; freqMIDI; ampMIDI; dureeMIDI; lastTimeMIDI = 0; tempoMIDI=0; freqCentroidMIDI=0; flatnessMIDI=0; energyMIDI=0; fluxMIDI=0; freqTampon = nil; ampTampon = nil; lastTimeAnalyse = Main.elapsedTime;

			// OSC pour Audio et File
			oscMusicData = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				var freq, amp, duree, tempo, freqCentroid=0, flatness=0, energy=0, flux=0, musicData=[];
				if(msg.at(2) == 8 and: {flagKeyboard == 'off'},
					{
						// Music
						freq=msg.at(3);
						amp=msg.at(4);
						duree = time - lastTimeAnalyse;
						tempo = msg.at(6);
						// Info spectral sur le son
						freqCentroid = msg.at(7);
						flatness = msg.at(8);
						energy = msg.at(9);
						flux = msg.at(10);
						fluxOnFly = flux;
						flatnessOnFly = flatness;
						// Set BPM
						if(flagSystemBPM == 0, {tempo = 1});
						if(flagSystemBPM == 1, {{bpmSlider.valueAction_(tempo * 60)}.defer});
						if(flagSystemBPM == 2, {tempo = systemBPM.tempo});
						if(flagSystemBPM == 3, {tempo = oscTempoMaster});
						// Set Bus OSC
						if(flagDataOSC == 'on', {busOSC.at(0).set(freq, amp, duree, tempo, freqCentroid, flatness, energy, flux)});
						//Analyze Data
						if(duree > timeMemory or: {duree > timeMaximum} and: {flagDataOSC == 'on'}, {
							(numFhzBand + 1).do({arg i; indexDataMusic.put(i, 0);listeDataOSC.put(i, []); lastTimeBand.put(i, time)});
							freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil; lastTimeAnalyse = time;
						});
						if(abs(freq.cpsmidi - freqBefore.cpsmidi) >= fhzFilter and: {abs(amp.ampdb - ampBefore.ampdb) >= ampFilter} and: {abs(duree - lastTimeAnalyse) >= durFilter} and: {duree >= durFilter},
							{
								if(freqTampon !=nil and: {ampTampon != nil},
									{
										// Send Music to Instruments
										musicAppAddr.sendMsg('/NewMusic', [freq, amp, duree, tempo, freqCentroid, flatness, energy, flux].asSymbol);
										if(flagDataOSC == 'on', {
											musicData=musicData.add(freqTampon);musicData=musicData.add(ampTampon);musicData=musicData.add(duree);
											musicData=musicData.add(tempo);musicData=musicData.add(freqCentroid);musicData=musicData.add(flatness);
											musicData=musicData.add(energy);musicData=musicData.add(flux);
											freqBefore = freqTampon; ampBefore = ampTampon; dureeBefore = duree;
											// Set All Data
											if(signalBuffer > listeDataOSC.at(0).size,
												{
													listeDataOSC.put(0, listeDataOSC.at(0).add(musicData));
													lastTimeBand.put(0, time);
												},
												{
													if(listeDataOSC.at(0).size <= indexDataMusic.at(0), {indexDataMusic.put(0, 0)});
													listeDataOSC.put(0, listeDataOSC.at(0).wrapPut(indexDataMusic.at(0), musicData));
													indexDataMusic.put(0, indexDataMusic.at(0) + 1);
													lastTimeBand.put(0, time);
											});
											//
											// Dispatch Band FHZ
											//
											for(1, numFhzBand, {arg i;
												if(musicData.at(0) > bandFHZ.at(i-1) and: {musicData.at(0) < bandFHZ.at(i)}, {
													// Buses
													busOSC.at(i).set(freq, amp, duree, tempo, freqCentroid, flatness, energy, flux);
													// Add Data
													if(signalBuffer > listeDataOSC.at(i).size,
														{
															listeDataOSC.put(i, listeDataOSC.at(i).add(musicData));
															lastTimeBand.put(i, time);
														},
														{
															if(listeDataOSC.at(i).size <= indexDataMusic.at(i), {indexDataMusic.put(i, 0)});
															listeDataOSC.put(i, listeDataOSC.at(i).wrapPut(indexDataMusic.at(i), musicData));
															indexDataMusic.put(i, indexDataMusic.at(i) + 1);
															lastTimeBand.put(i, time);
													});
												},
												{
													if(i <= numFhzBand, {
														if((time - lastTimeBand.at(i)) > timeMemory, {
															indexDataMusic.put(i, 0);
															listeDataOSC.put(i, []);
															lastTimeBand.put(i, time);
														});
													});
												});
											});
											freqBefore = freq; ampBefore = amp; dureeBefore = duree;
											lastTimeAnalyse = time;
										});
								});
								freqTampon = freq; ampTampon = amp; lastTimeAnalyse = time;
								{
									// Setup Automation Preset
									fonctionAutomationPreset.value(listeDataOSC.at(0), freqCentroid, flatness, energy, flux);
									// Display Data on Control Panel
									displayOSC.setString("Fhz:" + freq.asString + "\n" ++ "Amp:" + amp.asString + "\n" ++ "Dur:" + duree.asString + "\n" ++ "Bpm:" + (tempo * 60).asString + "\n" ++ "Flx:" + flux.asString + "\n" ++ "Fla:" +  flatness.asString + "\n" ++ "Fhc:" + freqCentroid.asString + "\n" ++ "Fhe:" + energy.asString + "\n", 0, 500);
								}.defer;
						});
						dureeOSCdata = Main.elapsedTime;
					},
					{nil});
			}, '/Matrix_Musical_Data', serverAdresse);

			// OSC pour Keyboard
			oscKeyboardData = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				var freq, amp, duree, tempo, freqCentroid=0, flatness=0, energy=0, flux=0, musicData=[];
				if(msg.at(2) == 8 and: {flagKeyboard == 'on'},
					{
						// Music
						freq=msg.at(3);
						amp=msg.at(4);
						duree = time - lastTimeAnalyse;
						tempo = msg.at(6);
						// Info spectral sur le son
						freqCentroid = msg.at(7);
						flatness = msg.at(8);
						energy = msg.at(9);
						flux = msg.at(10);
						fluxOnFly = flux;
						flatnessOnFly = flatness;
						// Set BPM
						if(flagSystemBPM == 0, {tempo = 1});
						if(flagSystemBPM == 1, {{bpmSlider.valueAction_(tempo * 60)}.defer});
						if(flagSystemBPM == 2, {tempo = systemBPM.tempo});
						if(flagSystemBPM == 3, {tempo = oscTempoMaster});
						// Set Bus OSC
						if(flagDataOSC == 'on', {busOSC.at(0).set(freq, amp, duree, tempo, freqCentroid, flatness, energy, flux)});
						//Analyze Data
						if(duree > timeMemory or: {duree > timeMaximum} and: {flagDataOSC == 'on'}, {
							(numFhzBand + 1).do({arg i; indexDataMusic.put(i, 0);listeDataOSC.put(i, []); lastTimeBand.put(i, time)});
							freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil; lastTimeAnalyse = time;
						});
						if(freqTampon !=nil and: {ampTampon != nil},
							{
								// Send Music to Instruments
								musicAppAddr.sendMsg('/NewMusic', [freq, amp, duree, tempo, freqCentroid, flatness, energy, flux].asSymbol);
								if(flagDataOSC == 'on', {
									musicData=musicData.add(freqTampon);musicData=musicData.add(ampTampon);musicData=musicData.add(duree);
									musicData=musicData.add(tempo);musicData=musicData.add(freqCentroid);musicData=musicData.add(flatness);
									musicData=musicData.add(energy);musicData=musicData.add(flux);
									freqBefore = freqTampon; ampBefore = ampTampon; dureeBefore = duree;
									// Set All Data
									if(signalBuffer > listeDataOSC.at(0).size,
										{
											listeDataOSC.put(0, listeDataOSC.at(0).add(musicData));
											lastTimeBand.put(0, time);
										},
										{
											if(listeDataOSC.at(0).size <= indexDataMusic.at(0), {indexDataMusic.put(0, 0)});
											listeDataOSC.put(0, listeDataOSC.at(0).wrapPut(indexDataMusic.at(0), musicData));
											indexDataMusic.put(0, indexDataMusic.at(0) + 1);
											lastTimeBand.put(0, time);
									});
									//
									// Dispatch Band FHZ
									//
									for(1, numFhzBand, {arg i;
										if(musicData.at(0) > bandFHZ.at(i-1) and: {musicData.at(0) < bandFHZ.at(i)}, {
											// Buses
											busOSC.at(i).set(freq, amp, duree, tempo, freqCentroid, flatness, energy, flux);
											// Add Data
											if(signalBuffer > listeDataOSC.at(i).size,
												{
													listeDataOSC.put(i, listeDataOSC.at(i).add(musicData));
													lastTimeBand.put(i, time);
												},
												{
													if(listeDataOSC.at(i).size <= indexDataMusic.at(i), {indexDataMusic.put(i, 0)});
													listeDataOSC.put(i, listeDataOSC.at(i).wrapPut(indexDataMusic.at(i), musicData));
													indexDataMusic.put(i, indexDataMusic.at(i) + 1);
													lastTimeBand.put(i, time);
											});
										},
										{
											if(i <= numFhzBand, {
												if((time - lastTimeBand.at(i)) > timeMemory, {
													indexDataMusic.put(i, 0);
													listeDataOSC.put(i, []);
													lastTimeBand.put(i, time);
												});
											});
										});
									});
									freqBefore = freq; ampBefore = amp; dureeBefore = duree;
									lastTimeAnalyse = time;
								});
						});
						freqTampon = freq; ampTampon = amp; lastTimeAnalyse = time;
						{
							// Setup Automation Preset
							fonctionAutomationPreset.value(listeDataOSC.at(0), freqCentroid, flatness, energy, flux);
							// Display Data on Control Panel
							displayOSC.setString("Fhz:" + freq.asString + "\n" ++ "Amp:" + amp.asString + "\n" ++ "Dur:" + duree.asString + "\n" ++ "Bpm:" + (tempo * 60).asString + "\n" ++ "Flx:" + flux.asString + "\n" ++ "Fla:" +  flatness.asString + "\n" ++ "Fhc:" + freqCentroid.asString + "\n" ++ "Fhe:" + energy.asString + "\n", 0, 500);
						}.defer;
						dureeOSCdata = Main.elapsedTime;
					},
					{nil});
			}, '/Matrix_Keyboard_Data', serverAdresse);

			// OSC pour MIDI-IN
			oscMIDIdata = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(msg.at(2) == 5 and: {flagKeyboard == 'on'},
					{
						// SetUp Env Var
						tempoMIDI = msg.at(3);
						freqCentroidMIDI = msg.at(4);
						flatnessMIDI = msg.at(5);
						energyMIDI = msg.at(6);
						fluxMIDI = msg.at(7);
						fluxOnFly = fluxMIDI;
						flatnessOnFly = flatnessMIDI;
						// Set BPM
						if(flagSystemBPM == 0, {tempoMIDI = 1});
						if(flagSystemBPM == 1, {{bpmSlider.valueAction_(tempoMIDI * 60)}.defer});
						if(flagSystemBPM == 2, {tempoMIDI = systemBPM.tempo});
						if(flagSystemBPM == 3, {tempoMIDI = oscTempoMaster});
						{
							// Display Data on Control Panel
							displayOSC.setString("Fhz:" + freqMIDI.asString + "\n" ++ "Amp:" + ampMIDI.asString + "\n" ++ "Dur:" + dureeMIDI.asString + "\n" ++ "Bpm:" + (tempoMIDI * 60).asString + "\n" ++ "Flx:" + fluxMIDI.asString + "\n" ++ "Fla:" +  flatnessMIDI.asString + "\n" ++ "Fhc:" + freqCentroidMIDI.asString + "\n" ++ "Fhe:" + energyMIDI.asString + "\n", 0, 500);
						}.defer;
						dureeOSCdata = Main.elapsedTime;
					},
					{nil});
			}, '/Matrix_MIDI_Data', serverAdresse);

			// Setup MIDI Responder
			// NoteOn
			MIDIdef.noteOn(\midiNoteOn, {arg amp, freq, canal, src;
				var duree=0, time=Main.elapsedTime, musicData=[];
				if(canal == canalMIDI, {
					duree = time - lastTimeMIDI;
					s.bind{
						midiKeyboard.set(\trigger, 0);
						s.sync;
						midiKeyboard.set(\trigger, 1);
						s.sync;
					};
					// Musical Data
					freqMIDI = freq.midicps;
					ampMIDI = amp / 127;
					dureeMIDI =  duree;
					// Set Bus OSC
					if(flagDataOSC == 'on', {busOSC.at(0).set(freqMIDI, ampMIDI, dureeMIDI, tempoMIDI, freqCentroidMIDI, flatnessMIDI, energyMIDI, fluxMIDI)});
					//Analyze Data
					if(dureeMIDI > timeMemory or: {dureeMIDI > timeMaximum} and: {flagDataOSC == 'on'}, {
						(numFhzBand + 1).do({arg i; indexDataMusic.put(i, 0);listeDataOSC.put(i, []); lastTimeBand.put(i, time)});
						freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil; lastTimeMIDI = Main.elapsedTime;
					});
					if(freqTampon !=nil and: {ampTampon != nil},
						{
							// Send Music to Instruments
							musicAppAddr.sendMsg('/NewMusic', [freqMIDI, ampMIDI, dureeMIDI, tempoMIDI, freqCentroidMIDI, flatnessMIDI, energyMIDI, fluxMIDI].asSymbol);
							if(flagDataOSC == 'on', {
								musicData=musicData.add(freqTampon);musicData=musicData.add(ampTampon);musicData=musicData.add(dureeMIDI);
								musicData=musicData.add(tempoMIDI);musicData=musicData.add(freqCentroidMIDI);musicData=musicData.add(flatnessMIDI);
								musicData=musicData.add(energyMIDI);musicData=musicData.add(fluxMIDI);
								freqBefore = freqTampon; ampBefore = ampTampon; dureeBefore = dureeMIDI;
								// Set All Data
								if(signalBuffer > listeDataOSC.at(0).size,
									{
										listeDataOSC.put(0, listeDataOSC.at(0).add(musicData));
										lastTimeBand.put(0, time);
									},
									{
										if(listeDataOSC.at(0).size <= indexDataMusic.at(0), {indexDataMusic.put(0, 0)});
										listeDataOSC.put(0, listeDataOSC.at(0).wrapPut(indexDataMusic.at(0), musicData));
										indexDataMusic.put(0, indexDataMusic.at(0) + 1);
										lastTimeBand.put(0, time);
								});
								//
								// Dispatch Band FHZ
								//
								for(1, numFhzBand, {arg i;
									if(musicData.at(0) > bandFHZ.at(i-1) and: {musicData.at(0) < bandFHZ.at(i)}, {
										// Buses
										busOSC.at(i).set(freqMIDI, ampMIDI, dureeMIDI, tempoMIDI, freqCentroidMIDI, flatnessMIDI, energyMIDI, fluxMIDI);
										// Add Data
										if(signalBuffer > listeDataOSC.at(i).size,
											{
												listeDataOSC.put(i, listeDataOSC.at(i).add(musicData));
												lastTimeBand.put(i, time);
											},
											{
												if(listeDataOSC.at(i).size <= indexDataMusic.at(i), {indexDataMusic.put(i, 0)});
												listeDataOSC.put(i, listeDataOSC.at(i).wrapPut(indexDataMusic.at(i), musicData));
												indexDataMusic.put(i, indexDataMusic.at(i) + 1);
												lastTimeBand.put(i, time);
										});
									},
									{
										if(i <= numFhzBand, {
											if((time - lastTimeBand.at(i)) > timeMemory, {
												indexDataMusic.put(i, 0);
												listeDataOSC.put(i, []);
												lastTimeBand.put(i, time);
											});
										});
									});
								});
								freqBefore=freqMIDI;ampBefore=ampMIDI;dureeBefore=dureeMIDI;lastTimeMIDI = time;
							});
					});
					freqTampon = freqMIDI; ampTampon = ampMIDI; lastTimeMIDI = time;
					// Setup Automation Preset
					fonctionAutomationPreset.value(listeDataOSC.at(0), freqCentroidMIDI, flatnessMIDI, energyMIDI, fluxMIDI);
				});
			}, (0..127), nil);

			// Fonction Tdef watch OSCdata
			fonctionTdefOSCdata={
				tdefOSCdata = Tdef("WatchOSCdata", {
					loop({
						// Watch musicdata
						if((Main.elapsedTime - dureeOSCdata) > timeMemory and: {flagDataOSC == 'on'}, {
							// Init Array
							listeDataOSC = [];
							indexDataMusic = [];
							(numFhzBand + 1).do({arg i;
								listeDataOSC = listeDataOSC.add([]);
								indexDataMusic = indexDataMusic.add(0);
							});
						});
						(1/100 * systemBPM.tempo).wait;
					});
				});
			};
			fonctionTdefOSCdata.value;

			// Init Fonction OSC
			initOSCresponder = {
				oscHPtempo = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					{
						if(bpmOnOff.value == 3 and: {oscStateflag == 'slave'},// and: {~entreemode != 'Midi IN'},
							{
								bpmSlider.valueAction = msg.wrapAt(1);
								oscTempoMaster = msg.wrapAt(1) / 60;
						});
					}.defer
				}, '/HPtempo', masterAppAddr);
				// OSC synchroStart slave
				oscHPstart = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					if(oscStateflag == 'slave', {
						{startSystem.valueAction = msg.wrapAt(1)}.defer;
					});
				}, '/HPstart', masterAppAddr);
				// OSC synchroRec slave
				oscHPrec = OSCFunc.newMatching({arg msg, time, addr, recvPort;
					if(oscStateflag == 'slave', {
						{
							if(msg.wrapAt(1) == 'Rec On', {fonctionRecOn.value});// Recording On
							if(msg.wrapAt(1) == 'Rec Off', {fonctionRecOff.value});// Recording Off
							if(msg.wrapAt(1) == 'Rec Pause', {fonctionRecPause.value});// Recording Pause
						}.defer;
					});
				}, '/HPrec', masterAppAddr);
			};
			initOSCresponder.value;

			// Brightness of signal = freqCentroid ( freq en hertz)
			// Entropie du son  = flatness (0 -> sinus | 1 -> whiteNoise)
			// Freq max energie spectral = energy (freq en hertz)
			// Flux spectral = flux (0 - > - de changement | 1 + de changement)
			// Flatness > 0 | Flux <= 1

			fonctionAutomationPreset = {arg dataMusicFFT, freqCentroid, flatness, energy, flux;
				var meanProbaPresetFlux=0, meanProbaPresetFlatness=0, file, number=lastNumberChoiceConfig, newTime, compteur=0, musicData, q1A, medianeA, q3A, ecartqA, ecartsemiqA, q1U, medianeU, q3U, ecartqU, ecartsemiqU, valuesFlux=0, valuesFlatness=0, seuil, variableTemps=0;
				newTime = Main.elapsedTime;
				musicData = dataMusicFFT.flop;// Setup Array
				if(musicData.size >= 1 and: {musicData != [ [  ] ]}, {
					if(variableChange == "Flux", {variableTemps = flux; seuil = onOffSynthValue.at(0)}, {variableTemps = flatness; seuil = onOffSynthValue.at(1)});
					meanProbaPresetFlux = flux.log2.abs; meanProbaPresetFlatness = flatness.log2.abs; valuesFlux = musicData.at(7).log2.abs; valuesFlatness = musicData.at(5).log2.abs;
					# q1U, medianeU, q3U, ecartqU, ecartsemiqU = valuesFlux.quartiles;
					# q1A, medianeA, q3A, ecartqA, ecartsemiqA = valuesFlatness.quartiles;
					if(algoChange == "Probability", {
						if(onOffSynth == 1 and: {flagCollectFolders == 'off'}
							and: {meanProbaPresetFlux < (q1U - ecartqU) or: {meanProbaPresetFlux > (q3U + ecartqU)}} and: {meanProbaPresetFlatness < (q1A - ecartqA) or: {meanProbaPresetFlatness > (q3A + ecartqA)}} and: {abs(newTime - lastTimeAutomationPreset) > limitTemps},
							{
								while({number == lastNumberChoiceConfig and: {compteur <= 40}}, {number = rrand(0, foldersToScanAll.size - 1); compteur = compteur + 1});
								lastTimeAutomationPreset = newTime;
								limitTemps = variableTemps.log2.abs * 6 + 6;
								if(number != nil, {
									lastNumberChoiceConfig = number;
									if(File.exists(pathMatrix ++ foldersToScanAll.wrapAt(number)),
										{listeWindowSynth.do({arg window; window.close});
											file=File(pathMatrix ++ foldersToScanAll.wrapAt(number),"r");
											windowControl.name="Matrix Control" + " | " + foldersToScanAll.wrapAt(number);
											if(foldersToScanAll.wrapAt(number).find("Preset") == 0 or: {foldersToScanAll.wrapAt(number).find("preset") == 0}, {fonctionLoadPreset.value(file.readAllString.interpret)}, {fonctionLoadSynthesizer.value(file.readAllString.interpret);
												// Init Band for Synth
												fonctionInitBand.value(numFhzBand);
											});
											file.close;listeWindows.at(3).front;indexWindows=3}, {"cancelled".postln});
								});
						});
					},
					{
						if(onOffSynth == 1 and: {flagCollectFolders == 'off'}
							and: {abs(newTime - lastTimeAutomationPreset) > limitTemps}
							and: {abs(meanProbaPresetFlux - lastMeanProbaPresetFlux) > seuil and: {abs(meanProbaPresetFlatness - lastMeanProbaPresetFlatness) > seuil}}, {
								while({number == lastNumberChoiceConfig and: {compteur <= 40}}, {number = rrand(0, foldersToScanAll.size - 1); compteur = compteur + 1});
								lastTimeAutomationPreset = newTime;
								limitTemps = variableTemps.log2.abs * 6 + 6;
								if(number != nil, {
									lastNumberChoiceConfig = number;
									if(File.exists(pathMatrix ++ foldersToScanAll.wrapAt(number)),
										{listeWindowSynth.do({arg window; window.close});
											file=File(pathMatrix ++ foldersToScanAll.wrapAt(number),"r");
											windowControl.name="Matrix Control" + " | " + foldersToScanAll.wrapAt(number);
											if(foldersToScanAll.wrapAt(number).find("Preset") == 0 or: {foldersToScanAll.wrapAt(number).find("preset") == 0}, {fonctionLoadPreset.value(file.readAllString.interpret)}, {fonctionLoadSynthesizer.value(file.readAllString.interpret);
												// Init Band for Synth
												fonctionInitBand.value(numFhzBand);
											});
											file.close;listeWindows.at(3).front;indexWindows=3}, {"cancelled".postln});
								});
						});
					});
					lastMeanProbaPresetFlux = meanProbaPresetFlux;
					lastMeanProbaPresetFlatness = meanProbaPresetFlatness;
				});
			};

			// Setup Font View
			listeWindows.do({arg window;
				window.view.do({arg view;
					view.children.do({arg subView;
						subView.font = Font("Helvetica", 10);
					});
				});
			});

			cmdperiodfunc = {
				// MIDI OFF
				16.do({arg canal; midiOut.allNotesOff(canal);
					if(flagVST == 'on', {~fxVST.midi.noteOff(canal)});

				});
				MIDIIn.disconnect;
				MIDIdef.freeAll;
				windowControl.close;
				windowVST.close;
			};

			CmdPeriod.doOnce(cmdperiodfunc);

			// Init Audio In
			startSystem.valueAction_(1);
			switchAudioIn.valueAction_(1);
			switchAudioIn.valueAction_(0);
			startSystem.valueAction_(0);

		});

	}

	// GUI windows + Menu add

	createGUI  {

		// Fonction ShortCut
		fonctionShortCut = {arg window;
			window.view.keyDownAction = {arg view,char,modifiers,unicode, keycode;
				var file, number, tampon;
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
				// key w -> affichage windows ->
				if(modifiers==0 and: {unicode==119} and: {keycode==13}, {indexWindows=indexWindows+1;
					if(indexWindows > (listeWindows.size - 1), {indexWindows=0});
					listeWindows.at(indexWindows).front;
				});
				// Key ctrlw -> affichage windows <-
				if(unicode==23 and: {keycode==13},{indexWindows=indexWindows-1;
					if(indexWindows < 0, {indexWindows=listeWindows.size - 1});
					listeWindows.at(indexWindows).front;
				});
				// Key alt + w -> affichage control panel
				if(modifiers==524288 and: {unicode==119} and: {keycode==13},{
					listeWindows.at(3).front;indexWindows=3;
				});
				// key esc or SpaceBar-> All System on/off
				if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}},{if(windowControl.view.children.at(1).value == 1,{windowControl.view.children.at(1).valueAction_(0)},{windowControl.view.children.at(1).valueAction_(1)});
				});
				//key k New Environment
				if(char == $k, {
					//windowControl.view.children.at(33).valueAction_(0);
					//fonctionUserOperatingSystem.value(9);
					flagCollectFolders = 'on';
					FileDialog.new({arg path;
						pathMatrix  = path.at(0).asString ++"/";
						/*pathMatrix= PathName.new(paths);
						pathMatrix = pathMatrix.pathOnly;*/
						windowControl.name="Matrix Control" + " | " + pathMatrix.asString;
						fonctionCollectFolders.value;
					}, fileMode: 2);
				});
				// key m -> Switch Algo Tempo�
				if(char == $m, {if(bpmOnOff.value == 0, {bpmOnOff.valueAction_(1)},
					{if(bpmOnOff.value == 1, {bpmOnOff.valueAction_(2)}, 		{if(bpmOnOff.value == 2, {bpmOnOff.valueAction_(3)},
						{if(bpmOnOff.value == 3, {bpmOnOff.valueAction_(0)}, {nil});
					});
					});
				});
				});
				// key q -> Switch Algo Analyze
				if(char == $q, {if(windowControl.view.children.at(14).value == 0, {windowControl.view.children.at(14).valueAction_(1)},
					{if(windowControl.view.children.at(14).value == 1, {windowControl.view.children.at(14).valueAction_(2)}, {if(windowControl.view.children.at(14).value == 2,			{windowControl.view.children.at(14).valueAction_(3)},{if(windowControl.view.children.at(14).value == 3,{windowControl.view.children.at(14).valueAction_(0)})});
					});
				})});
				// key o -> Automation Preset on/off
				if(char == $o, {
					if(windowControl.view.children.at(33).value == 0 , {windowControl.view.children.at(33).valueAction_(1)}, {windowControl.view.children.at(33).valueAction_(0)});
				});
				// key l -> load Preset
				if(modifiers==0 and: {unicode==108} and: {keycode==37}, {commande = 'Load Preset';
				});
				// key ctrl l -> load Preset without close others windows
				if(modifiers==262144 and: {unicode==12} and: {keycode==37}, {commande = 'Load + Add Preset';
				});
				// key L-> load Synthesizer
				if(modifiers==131072 and: {unicode==76} and: {keycode==37}, {commande='Load Synthesizer';
				});
				// key ctrl L-> load Synthesizer without close others windows
				if(modifiers==393216 and: {unicode==12} and: {keycode==37}, {commande='Load + Add Synthesizer';
				});
				// key s -> save Preset
				if(modifiers==0 and: {unicode==115} and: {keycode==1},
					{commande='Save Preset';
				});
				// key S -> save Synthesizer
				if(modifiers==131072 and: {unicode==83} and: {keycode==1} and: {window.name.containsStringAt(0, "Matrix Control").not} and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not}, {commande='Save Synthesizer';
				});
				// key i -> Close All Synthesizer
				if(modifiers==0 and: {unicode==105} and: {keycode==34}, {fonctionUserOperatingSystem.value(9);
					// Reset Master Control Sliders
					controlFreqSlider.valueAction_([0, 127]);
					previousFreq = [0, 127];
					controlFreqTranSlider.valueAction_(0);
					previousFreqTrans = 0;
					controlDureeSlider.valueAction_([0, 1]);
					previousDuree = [0, 1];
					controlDureeTranSlider.valueAction_(1);
					previousDureeTrans = 1;
					controlQuantaSlider.valueAction_(0);
					previousDureeQuanta = 0;
					previousPan = [-1, 1];
					controlPanSlider.valueAction_([-1, 1]);
					previousAmp = [-inf, 0];
					controlAmpSlider.valueAction_([-inf, 0]);
				});
				// key alt + i -> Clear musical data
				if(modifiers==524288 and: {unicode==108} and: {keycode==37},{
					// Init Array
					listeDataOSC=[];
					indexDataMusic = [];
					lastTimeBand = [];
					// Init Array
					(numFhzBand + 1).do({arg i;
						listeDataOSC = listeDataOSC.add([]);
						indexDataMusic = indexDataMusic.add(0);
						lastTimeBand = lastTimeBand.add(Main.elapsedTime);
					});
				});
				// key f -> Switch File for Analyze
				if(modifiers==0 and: {unicode==102} and: {keycode==3}, {
					commande = 'Switch File for Analyze';
				});
				// key alt + f -> Lecture une fois file for analyze
				if(modifiers==524288 and: {unicode==102} and: {keycode==3}, {
					synthFileIn.set(\loop, 0);
				});
				// key F -> Lecture boucle file for analyze
				if(modifiers==131072 and: {unicode==70} and: {keycode==3}, {
					synthFileIn.set(\loop, 1);
				});
				// key ctrl f -> Load and Add file for analyze
				if(modifiers==262144 and: {unicode==6} and: {keycode==3}, {
					Dialog.openPanel({ arg paths;
						listeFileAnalyze = listeFileAnalyze.add(fonctionLoadFileForAnalyse.value(paths));
						listeNameFileAnalyze = listeNameFileAnalyze.add(PathName.new(paths).fileName);
					},{"cancelled".postln});
				});
				// key alt + c -> Copy Preset
				if(modifiers==524288 and: {unicode==99} and: {keycode==8}, {
					fonctionUserOperatingSystem.value(8);
				});
				// key C -> Copy Synthesizer
				if(modifiers==131072 and: {unicode==67} and: {keycode==8} and: {window.name.containsStringAt(0, "Matrix Control").not} and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not}, {
					fonctionUserOperatingSystem.value(7, window);
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
				// Key d -> Synchro Synthesizer
				if(char == $d, {
					if(startSystem.value == 1, {
						listeGroupeSynth.do({arg synth, index;
							if(listeWindowSynth.at(index).view.children.at(0).value == 1, {
								listeWindowSynth.at(index).view.children.at(0).valueAction_(0);
								listeWindowSynth.at(index).view.children.at(0).valueAction_(1);
							});
						});
					});
				});
				// Key P -> Play all Synth
				if(modifiers==131072 and: {unicode==80} and: {keycode==35}, {
					listeGroupeSynth.do({arg synth, index; listeWindowSynth.at(index).view.children.at(0).valueAction_(1)});
				});
				// Key alt + P -> Stop all Synth
				if(modifiers==655360 and: {unicode==80} and: {keycode==35}, {
					listeGroupeSynth.do({arg synth, index; listeWindowSynth.at(index).view.children.at(0).valueAction_(0)});
				});
				// Key p -> Play Synth on front
				if(modifiers==0 and: {unicode==112} and: {keycode==35} and: {window.name.containsStringAt(0, "Matrix Control").not} and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not}, {
					window.view.children.at(0).valueAction_(1);
				});
				// Key alt + p -> Stop Synth on front
				if(modifiers==524288 and: {unicode==112} and: {keycode==35} and: {window.name.containsStringAt(0, "Matrix Control").not} and: {window.name.containsStringAt(0, "MasterFX").not} and: {window.name.containsStringAt(0, "Master Sliders Music Control Synthesizer and FX").not}, {
					window.view.children.at(0).valueAction_(0);
				});
				// Key y -> Display NodesTree
				if(char == $y, {
					//Document.listener.string="";
					s.queryAllNodes;
				});
				// key N -> Add a new synth or fx
				if(char == $N, {
					tampon = listeDataOSC;
					fonctionAddSynthFX.value(2, "Nil", "Nil", 0, 4.0, 4.0);// Fonction Add SinOsc by default
					listeDataOSC = tampon;
				});
				// Key b -> switch recording buffer data OSC on/off
				if(char == $b,  {
					if(windowControl.view.children.at(13).value == 0 , {windowControl.view.children.at(13).valueAction_(1)}, {windowControl.view.children.at(13).valueAction_(0)});
				});
				// Key J -> Save OSCmusicData
				if(char == $J,  {commande='Save OSCmusicData';
				});
				// Key j -> Load OSCmusicData
				if(char == $j,  {commande='Load OSCmusicData';
				});
				// Key z -> load Preset aleatoire
				if(char == $z, {
					number = rrand(0, foldersToScanPreset.size - 1);
					if(File.exists(pathMatrix ++ foldersToScanPreset.at(number)),
						{listeWindowSynth.do({arg window; window.close});
							file=File(pathMatrix ++ foldersToScanPreset.at(number),"r");
							windowControl.name="Matrix Control" + " | " + foldersToScanPreset.at(number);
							fonctionLoadPreset.value(file.readAllString.interpret);
							file.close;listeWindows.at(3).front;indexWindows=3}, {"cancelled".postln});
				});
				// Key Z -> load Synthesizer aleatoire
				if(char == $Z, {
					number = rrand(0, foldersToScanSynthesizer.size - 1);
					if(File.exists(pathMatrix ++ foldersToScanSynthesizer.at(number)),
						{listeWindowSynth.do({arg window; window.close});
							file=File(pathMatrix ++ foldersToScanSynthesizer.at(number),"r");
							windowControl.name="Matrix Control" + " | " + foldersToScanSynthesizer.at(number);
							fonctionLoadSynthesizer.value(file.readAllString.interpret);
							file.close;listeWindows.at(3).front;indexWindows=3;
							// Init Band for Synth
							fonctionInitBand.value(numFhzBand);
					}, {"cancelled".postln});
				});
				// Key h -> Switch source In.
				if(char == $h,  {if(windowControl.view.children.at(11).value >= 3, {windowControl.view.children.at(11).valueAction_(0)},
					{windowControl.view.children.at(11).valueAction_(windowControl.view.children.at(11).value + 1)});
				});
			};
		};

		// Fonction Commandes
		fonctionCommandes = {arg window, commandeExecute, number;
			var file, data, dataControlSynth, tampon;
			// Save Preset
			if(commandeExecute == 'Save Preset',{
				windowControl.name="Matrix Control" + " | " + "Preset" + number.asString;
				file=File(pathMatrix ++ "Preset" + number.asString ++ ".scd", "w");
				file.write(fonctionSavePreset.value(listeWindowSynth).asCompileString);
				file.close;
			});
			//load Preset
			if(commandeExecute == 'Load Preset',{
				if(File.exists(pathMatrix ++ "Preset" + number.value.asString ++ ".scd"),
					{fonctionUserOperatingSystem.value(9);
						windowControl.name="Matrix Control" + " | " + "Preset" + number.asString;
						file=File(pathMatrix ++ "Preset" + number.value.asString ++ ".scd", "r");
						fonctionLoadPreset.value(file.readAllString.interpret);
						file.close;listeWindows.at(3).front;indexWindows=3;
				}, {"cancelled".postln});
			});
			//load Preset without close others windows
			if(commandeExecute == 'Load + Add Preset',{
				if(File.exists(pathMatrix ++ "Preset" + number.value.asString ++ ".scd"),
					{windowControl.name="Matrix Control" + " | " + "Preset" + number.asString;
						file=File(pathMatrix ++ "Preset" + number.value.asString ++ ".scd", "r");
						fonctionLoadPreset.value(file.readAllString.interpret);
						file.close;listeWindows.at(3).front;indexWindows=3}, {"cancelled".postln});
			});
			// Save Synthesizer
			if(commandeExecute == 'Save Synthesizer', {
				windowControl.name="Matrix Control" + " | " + "Synthesizer" + number.asString;
				data = fonctionSaveSynthesizer.value(window);
				data = data.add(fonctionSaveControlSynth.value(windowControlSynth));// Save ControlSynth Panel
				data = data.add(fonctionSaveControl.value(windowControl));// Save Control Panel
				data = data.add(listeDataOSC.value);//Save OSCmusicData
				file=File(pathMatrix ++ "Synthesizer" + number.asString ++ ".scd", "w");
				file.write(data.asCompileString);
				file.close;
			});
			//load Synthesizer
			if(commandeExecute == 'Load Synthesizer', {
				if(File.exists(pathMatrix ++ "Synthesizer" + number.value.asString ++ ".scd"),
					{fonctionUserOperatingSystem.value(9);
						windowControl.name="Matrix Control" + " | " + "Synthesizer" + number.asString;
						file=File(pathMatrix ++ "Synthesizer" + number.value.asString ++ ".scd", "r");
						data = file.readAllString.interpret;
						file.close;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
						/*// Init Band for Synth
						fonctionInitBand.value(numFhzBand);*/
				}, {"cancelled".postln});
			});
			//load Synthesizer without close others windows
			if(commandeExecute == 'Load + Add Synthesizer', {
				if(File.exists(pathMatrix ++ "Synthesizer" + number.value.asString ++ ".scd"),
					{windowControl.name="Matrix Control" + " | " + "Synthesizer" + number.asString;
						file=File(pathMatrix ++ "Synthesizer" + number.value.asString ++ ".scd", "r");
						data = file.readAllString.interpret;
						file.close;
						tampon = data.last;// Load OSCmusicData
						data.remove(data.last);// Remove OSCmusicData
						fonctionLoadControl.value(windowControl, data.last);//Load Control Panel
						data.remove(data.last);// Remove control panel
						dataControlSynth = data.last; // ControlSynth Panel
						fonctionLoadControlSynth.value(windowControlSynth, data.last);//Load ControlSynth Panel
						data.remove(data.last);// Remove controlSynth panel
						fonctionLoadSynthesizer.value(data);
						listeDataOSC = tampon;
						/*// Init Band for Synth
						fonctionInitBand.value(numFhzBand);*/
				}, {"cancelled".postln});
			});
			//Save OSCmusicData
			if(commandeExecute == 'Save OSCmusicData', {
				file=File(pathMatrix ++ "OSCmusicData" + number.asString ++ ".scd", "w");
				file.write(listeDataOSC.asCompileString);
				file.close;
			});
			//Load OSCmusicData
			if(commandeExecute == 'Load OSCmusicData', {
				if(File.exists(pathMatrix ++ "OSCmusicData" + number.value.asString ++ ".scd"),
					{file=File(pathMatrix ++ "OSCmusicData" + number.value.asString ++ ".scd", "r");
						listeDataOSC = file.readAllString.interpret;
						file.close}, {"cancelled".postln});
			});
			//Switch file for Analyze
			if(commandeExecute == 'Switch File for Analyze', {
				if(listeFileAnalyze.at(number - 1) != nil, {
					s.bind{
						synthFileIn.set(\trigger, 0);
						synthFileIn.set(\bufferplay, listeFileAnalyze.at(number - 1));
						s.sync;
						synthFileIn.set(\trigger, 1);
						s.sync;
					};
					textFileAnalyze.string_(listeNameFileAnalyze.at(number - 1));
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (60 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (61 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (62 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (63 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (64 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (65 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (66 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (67 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (68 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (69 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (70 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (71 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (72 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (73 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (74 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 74 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(74 + keyboardTranslate.value, Color.red);
					};
				});
				// Key �
				if(modifiers==256 and: {unicode==233} and: {keycode==41}, {
					s.bind{
						keyboard.removeColor(lastNote);
						s.sync;
						keyboard.setColor(lastNote, Color.blue);
						s.sync;
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (75 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						synthKeyboard.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						synthKeyboard.set(\note, (76 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
						lastNote = 76 + keyboardTranslate.value;
						s.sync;
						keyboard.setColor(76 + keyboardTranslate.value, Color.red);
					};
				});
			};
		};

		////////////////////////// Window VST ///////////////////////////////
		windowVST = Window.new("VST Stereo", Rect(40, 500, 320, 80), scroll: true);
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
			~fxVST.close;
		};
		EZKnob(windowVST, 150 @ 25, "xFade", \unipolar,
			{|ez| groupeMasterFX.set(\xFade, ez.value)}, 0.5, layout: \horz);
		EZKnob(windowVST, 150 @ 25, "Gain In", \unipolar,
			{|ez| groupeMasterFX.set(\gainIn, ez.value)}, 0.5, layout: \horz);
		EZRanger(windowVST , 300 @ 20, "Pan", \bipolar,
			{|ez| groupeMasterFX.set(\panLo, ez.value.at(0), \panHi, ez.value.at(1))}, [0, 0], labelWidth: 40, numberWidth: 40);
		windowVST.view.children.at(0).focus;
		windowVST.front;

		////////////////////////// Window Keyboard ///////////////////////////////
		windowKeyboard = Window.new("Keyboard", Rect(0, 800, 1024, 175), scroll: true);
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
		setupKeyboardShortCut.states = [["Musical Keyboard Shortcut", Color.black,  Color.red(0.8, 0.25)],["System Shortcut", Color.yellow, Color.green(0.8, 0.25)]];
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
		windowKeyboard.onClose_({
			oscKeyboardData.remove;
			listeWindowSynth.remove(windowKeyboard);
		});
		listeWindows = listeWindows.add(windowKeyboard);
		keyboardShortCut.value(windowKeyboard);

		////////////////////////// Window MasterFX ///////////////////////////////
		windowMasterFX = Window.new("MasterFX", Rect(40, 600, 255, 55), scroll: true);
		windowMasterFX.view.decorator = FlowLayout(windowMasterFX.view.bounds);
		windowMasterFXLimit =EZSlider(windowMasterFX, 245 @ 18, "LimitOut",\db,
			{|ez| masterFX.set(\limit, ez.value.dbamp)},-3,labelWidth: 60,numberWidth: 40);
		windowMasterFX.view.decorator.nextLine;
		windowMasterFXPostAmp = EZSlider(windowMasterFX, 245 @ 18, "PostAmp", \db,
			{|ez| masterFX.set(\postAmp, ez.value.dbamp)}, 0,labelWidth: 60,numberWidth: 40);
		windowMasterFX.front;
		windowMasterFX.onClose_({
			masterFX.free;
			groupeMasterFX.free;
			listeWindowSynth.remove(windowMasterFX);
		});
		listeWindows = listeWindows.add(windowMasterFX);
		fonctionShortCut.value(windowMasterFX);

		////// Fonction Window for controling all sliders windows instruments /////
		windowControlSynth = Window("Master Sliders Music Control Synthesizer and FX", Rect(300, 500, 400, 175), scroll: true);
		windowControlSynth.alpha=1.0;
		windowControlSynth.front;
		windowControlSynth.view.decorator = FlowLayout(windowControlSynth.view.bounds);
		// Pan
		previousPan = [-1, 1];
		controlPanSlider = EZRanger(windowControlSynth, 390 @ 20, "Pan", \bipolar, {|ez| var valLo, valHi;
			valLo = (ez.value.at(0) - previousPan.at(0));
			valHi = (ez.value.at(1) - previousPan.at(1));
			previousPan = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(38).children.do({arg subView, subItem;
					if(subItem == 2, {subView.activeLo_(subView.lo + valLo); subView.activeHi_(subView.hi + valHi)})
				});
			});
		},[-1, 1],labelWidth: 85, numberWidth: 50);
		// Freq
		previousFreq = [0, 127];
		controlFreqSlider = EZRanger(windowControlSynth, 390 @ 20, "Freq", ControlSpec(0, 127, \lin, 0), {|ez| var valLo, valHi;
			valLo = (ez.value.at(0) - previousFreq.at(0) / 127).clip(-1, 1);
			valHi = (ez.value.at(1) - previousFreq.at(1) / 127).clip(-1, 1);
			previousFreq = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(39).children.do({arg subView, subItem;
					if(subItem == 2, {subView.activeLo_(subView.lo + valLo); subView.activeHi_(subView.hi + valHi)})
				});
			});
		},[0, 127],labelWidth: 85, numberWidth: 50);
		// Freq T
		previousFreqTrans = 0;
		controlFreqTranSlider=EZSlider(windowControlSynth, 390 @ 20, "FreqTranspose", ControlSpec(-127, 127, \lin, 0), {|ez| var val;
			val = (ez.value - previousFreqTrans / 127).clip(-1, 1);
			previousFreqTrans = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(40).children.do({arg subView, subItem;
					if(subItem == 1, {subView.valueAction_(subView.value + (val / 2))});
				});
			});
		}, 0, labelWidth: 85, numberWidth: 50);
		windowControlSynth.view.decorator.nextLine;
		// Amp
		previousAmp = [-inf, 0];
		controlAmpSlider = EZRanger(windowControlSynth, 390 @ 20, "Amp", \db, {|ez| var valLo, valHi;
			valLo = (ez.value.at(0).dbamp - previousAmp.at(0).dbamp);
			valHi = (ez.value.at(1).dbamp - previousAmp.at(1).dbamp);
			previousAmp = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(41).children.do({arg subView, subItem;
					if(subItem == 2, {subView.activeLo_(subView.lo + valLo); subView.activeHi_(subView.hi + valHi)})
				});
			});
		},[-inf, 0],labelWidth: 85, numberWidth: 50);
		// Duree
		previousDuree = [0, 1];
		controlDureeSlider = EZRanger(windowControlSynth, 390 @ 20, "Dur", \unipolar,
			{|ez| var valLo, valHi;
				valLo = (ez.value.at(0) - previousDuree.at(0)).clip(-1, 1);
				valHi = (ez.value.at(1) - previousDuree.at(1)).clip(-1, 1);
				previousDuree = ez.value;
				listeWindowSynth.do({|window|
					window.value.view.children.at(42).children.do({arg subView, subItem;
						if(subItem == 2, {subView.activeLo_(subView.lo + valLo); subView.activeHi_(subView.hi + valHi)})
					});
				});
		},[0, 1],labelWidth: 85, numberWidth: 50);
		// Duree T
		previousDureeTrans = 1;
		controlDureeTranSlider=EZSliderTempo(windowControlSynth, 390 @ 20, "Dur Stretch", ControlSpec(-100, 100, \lin, 0), {|ez| var val;
			val = (ez.value - previousDureeTrans / 100).clip(-1, 1);
			previousDureeTrans = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(43).children.do({arg subView, subItem;
					if(subItem == 1, {subView.valueAction_(subView.value + (val / 2))});
				});
			});
		}, 1, labelWidth: 85, numberWidth: 50);
		// Quantization
		previousDureeQuanta = 0;
		controlQuantaSlider=EZSlider(windowControlSynth, 390 @ 20, "Quant",ControlSpec(-100, 100, \lin, 0), {|ez| var val;
			val = (ez.value - previousDureeQuanta / 100).clip(-1, 1);
			previousDureeQuanta = ez.value;
			listeWindowSynth.do({|window|
				window.value.view.children.at(44).children.do({arg subView, subItem;
					if(subItem == 1, {subView.valueAction_(subView.value + (val / 2))});
				});
			});
		}, 0, labelWidth: 85, numberWidth: 50);
		windowControlSynth.view.decorator.nextLine;

		windowControlSynth.onClose_({
			listeWindows.remove(windowControlSynth);
		});

		listeWindows = listeWindows.add(windowControlSynth);
		fonctionShortCut.value(windowControlSynth);

		////////////////////////////// Control Panel //////////////////////
		windowControl =Window("Matrix Control", Rect(300, 0, 410, 505), scroll: true);
		windowControl.alpha=1.0;
		windowControl.front;
		windowControl.view.decorator = FlowLayout(windowControl.view.bounds);
		StaticText(windowControl, Rect(0, 0, 400, 12)).string_("Matrix a User Interface for Organizing Sounds by Provinescu Software Production").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		// Systeme start stop playing
		startSystem = Button(windowControl,Rect(0, 0, 125, 20));
		startSystem.states = [["System Off", Color.black,  Color.green(0.8, 0.25)],["System On", Color.black, Color.red(0.8, 0.25)]];
		startSystem.action = {|view|
			if(oscStateflag == 'master', {slaveAppAddr.sendMsg('/HPstart', view.value)});// Send Synchro Start
			s.bind{
				if(view.value == 1, {
					freqBefore=0; ampBefore=0; dureeBefore=0; freqMIDI; ampMIDI; dureeMIDI; tempoMIDI=0; freqCentroidMIDI=0; flatnessMIDI=0; energyMIDI=0; fluxMIDI=0; freqTampon = nil; ampTampon = nil; lastTimeAnalyse = Main.elapsedTime; lastTimeMIDI = Main.elapsedTime;
					tdefOSCdata.play;
					s.sync;
					flagCollectFolders = 'on';
					if(oscStateflag == 'master', {ardourOSC.sendMsg('/transport_play')});// transport play
					s.sync;
					if(switchAudioIn.value == 0, {
						synthFileIn.run(false);
						s.sync;
						synthAnalyzeIn.run(true);
						s.sync;
						synthKeyboard.run(false);
						s.sync;
						midiKeyboard.run(false);
						s.sync;
						16.do({arg canal; midiOut.allNotesOff(canal);
							if(flagVST == 'on', {~fxVST.midi.noteOff(canal)});

						});
						MIDIIn.disconnect;
					});
					if(switchAudioIn.value == 1, {
						synthAnalyzeIn.run(false);
						s.sync;
						synthFileIn.run(true);
						s.sync;
						synthFileIn.set(\trigger, 0);
						s.sync;
						synthFileIn.set(\trigger, 1);
						s.sync;
						synthKeyboard.run(false);
						s.sync;
						midiKeyboard.run(false);
						s.sync;
						16.do({arg canal; midiOut.allNotesOff(canal);
							if(flagVST =='on', {~fxVST.midi.noteOff(canal)});

						});
						MIDIIn.disconnect;
					});
					if(switchAudioIn.value == 2, {
						synthAnalyzeIn.run(false);
						s.sync;
						synthFileIn.run(false);
						s.sync;
						synthKeyboard.run(true);
						s.sync;
						midiKeyboard.run(false);
						s.sync;
						16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
						});
						MIDIIn.disconnect;
					});
					if(switchAudioIn.value == 3, {
						synthAnalyzeIn.run(false);
						s.sync;
						synthFileIn.run(false);
						s.sync;
						synthKeyboard.run(false);
						s.sync;
						midiKeyboard.run(true);
						s.sync;
						16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
						});
						MIDIIn.connect;
					});
					if(algoAnalyse.value == 0, {
						synthAnalysePitch.run(false);
						s.sync;
						synthAnalysePitch2.run(false);
						s.sync;
						synthAnalyseKeyTrack.run(false);
						s.sync;
						synthAnalyseOnsets.run(true);
						s.sync;
					});
					if(algoAnalyse.value == 1, {
						synthAnalyseOnsets.run(false);
						s.sync;
						synthAnalyseKeyTrack.run(false);
						s.sync;
						synthAnalysePitch.run(true);
						s.sync;
						synthAnalysePitch2.run(false);
						s.sync;
					});
					if(algoAnalyse.value == 2, {
						synthAnalyseOnsets.run(false);
						s.sync;
						synthAnalyseKeyTrack.run(false);
						s.sync;
						synthAnalysePitch.run(false);
						s.sync;
						synthAnalysePitch2.run(true);
						s.sync;
					});
					if(algoAnalyse.value == 3, {
						synthAnalyseOnsets.run(false);
						s.sync;
						synthAnalyseKeyTrack.run(true);
						s.sync;
						synthAnalysePitch.run(false);
						s.sync;
						synthAnalysePitch2.run(false);
						s.sync;
					});
					if(flagRecording == 'on', {s.record});
					s.sync;
					flagCollectFolders = 'off';
					listeWindowSynth.do({arg window;
						window.view.children.at(0).valueAction = (window.view.children.at(0).value - 1).abs;
						s.sync;
						window.view.children.at(0).valueAction = (window.view.children.at(0).value - 1).abs});
					s.sync;
				}, {
					tdefOSCdata.stop;
					s.sync;
					flagCollectFolders = 'on';
					synthFileIn.run(false);
					s.sync;
					synthAnalyzeIn.run(false);
					s.sync;
					synthAnalyseOnsets.run(false);
					s.sync;
					synthAnalysePitch.run(false);
					s.sync;
					synthAnalysePitch2.run(false);
					s.sync;
					synthAnalyseKeyTrack.run(false);
					s.sync;
					synthKeyboard.run(false);
					s.sync;
					midiKeyboard.run(false);
					s.sync;
					if(flagRecording == 'on', {s.pauseRecording});
					s.sync;
					listeWindowSynth.do({arg window;
						window.view.children.at(0).valueAction = (window.view.children.at(0).value - 1).abs;
						s.sync;
						window.view.children.at(0).valueAction = (window.view.children.at(0).value - 1).abs});
					s.sync;
					if(oscStateflag == 'master', {ardourOSC.sendMsg('/transport_stop')});// transport stop
					s.sync;
					//if(oscStateflag == 'master', {ardourOSC.sendMsg('/goto_start')});// transport start
					//s.sync;
					//listeDataOSC=[];
					// s.sync;
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
					});
					MIDIIn.disconnect;
				});
			};
		};
		startSystem.focus;
		// User Operating System
		userOperatingSystem=PopUpMenu(windowControl, Rect(0, 0, 200, 20)).font_(Font( "Palatino-BoldItalic", 12)).items = userOSchoiceControl;
		userOperatingSystem.action = {arg item; fonctionUserOperatingSystem.value(item.value, windowControl); userOperatingSystem.value_(0)};
		oscState = StaticText(windowControl, Rect(0, 0, 55, 20)).background_(Color.grey(0.5, 0.8)).string_("OSC Off").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		StaticText(windowControl, Rect(0, 0, 400, 12)).string_("Audio In / Send Audio Bus / BPM System").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		// Source In
		sourceIn = PopUpMenu(windowControl,Rect(0, 0, 70, 20)).items = ["Az In 1", "Az In 2", "Az In 3", "Az In 4", "Az In 5", "Az In 6", "Az In 7", "Az In 8", "Az In 9", "Az In 10", "Az In 11", "Az In 12", "Az In 13", "Az In 14", "Az In 15", "Az In 16", "Az In 17", "Az In 18", "Az In 19", "Az In 20", "Az In 21", "Az In 22", "Az In 23", "Az In 24", "Az In 25", "Az In 26", "Az In 27", "Az In 28", "Az In 29", "Az In 30", "Az In 31", "Az In 32"];
		sourceIn.action = {arg in;
			synthAnalyzeIn.set(\in, in.value, \busAnalyze, busAnalyze.index);
		};
		// Audio In
		sendBusIn = PopUpMenu(windowControl,Rect(0, 0, 83, 20)).items = ["File->Bus 1", "File->Bus 2", "File->Bus 3", "File->Bus 4", "File->Bus 5", "File->Bus 6", "File->Bus 7", "File->Bus 8", "File->Bus 9", "File->Bus 10", "File->Bus 11", "File->Bus 12", "File->Bus 13", "File->Bus 14", "File->Bus 15", "File->Bus 16", "File->Bus 17", "File->Bus 18", "File->Bus 19", "File->Bus 20", "File->Bus 21", "File->Bus 22", "File->Bus 23", "File->Bus 24", "File->Bus 25", "File->Bus 26", "File->Bus 27", "File->Bus 28", "File->Bus 29", "File->Bus 30", "File->Bus 31", "File->Bus 32"];
		sendBusIn.action = {arg in;
			synthFileIn.set(\busIn, listeBusInOut.at(in.value));
		};
		sendBusIn.stringColor = Color.white;
		// BPM System
		bpmSlider=EZSlider(windowControl, Rect(0, 0, 150, 20), "BPM", ControlSpec(1, 960, \exp, 0),
			{|ez| if(oscStateflag == 'master', {slaveAppAddr.sendMsg('/HPtempo', ez.value)});//Send Synchro Tempo
				systemBPM.schedAbs(systemBPM.beats, {systemBPM.tempo_(ez.value / 60)})}, 60, labelWidth: 30,numberWidth: 45);
		bpmSlider.enabled_(false);
		// BPM On / Off
		bpmOnOff = PopUpMenu(windowControl,Rect(0, 0, 75, 20)).items = ["BPM Nil", "BPM Algo", "BPM On", "BPM OSC"];
		bpmOnOff.action = {|view| flagSystemBPM = view.value;
			if(view.value == 0, {bpmSlider.enabled_(false); bpmSlider.valueAction_(60)});
			if(view.value == 1, {bpmSlider.enabled_(true); bpmSlider.valueAction_(60)});
			if(view.value == 2, {bpmSlider.enabled_(true); bpmSlider.valueAction_(60)});
			if(view.value == 3, {bpmSlider.enabled_(false); bpmSlider.valueAction_(60)});
		};
		windowControl.view.decorator.nextLine;
		// Algorithme
		StaticText(windowControl, Rect(0, 0, 400, 12)).string_("Algorithm").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		textFileAnalyze = StaticText(windowControl, Rect(0, 0, 390, 12)).string_("a11wlk01-44_1.aiff").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		// Calculation Audio start stop playing
		switchAudioIn = PopUpMenu(windowControl,Rect(0, 0, 125, 20)).background_(Color.grey(0.8, 0.25)).stringColor_(Color.red(0.8, 0.75)).items=["AudioIn On","FileIn On","Keyboard","MIDI IN"];
		switchAudioIn.action = {|view|
			if(startSystem.value == 1, {
				if(view.value == 0, {
					synthFileIn.run(false);
					synthAnalyzeIn.run(true);
					synthKeyboard.run(false);
					midiKeyboard.run(false);
					flagKeyboard = 'off';
					//listeDataOSC=[];
					switchCanalMIDI.enabled_(false);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
					});
					MIDIIn.disconnect;
				});
				if(view.value == 1, {
					synthAnalyzeIn.run(false);
					synthFileIn.run(true);
					s.bind{
						synthFileIn.set(\trigger, 0);
						s.sync;
						synthFileIn.set(\trigger, 1);
						s.sync;
					};
					synthKeyboard.run(false);
					midiKeyboard.run(false);
					flagKeyboard = 'off';
					//listeDataOSC=[];
					switchCanalMIDI.enabled_(false);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
					});
					MIDIIn.disconnect;
				});
				if(view.value == 2, {
					//synthAnalyzeIn.run(false);
					//synthFileIn.run(false);
					flagKeyboard = 'on';
					synthKeyboard.run(true);
					midiKeyboard.run(false);
					//listeDataOSC=[];
					switchCanalMIDI.enabled_(false);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
					});
					MIDIIn.disconnect;
				});
				if(view.value == 3, {
					//synthAnalyzeIn.run(false);
					//synthFileIn.run(false);
					flagKeyboard = 'on';
					synthKeyboard.run(false);
					midiKeyboard.run(true);
					//listeDataOSC=[];
					switchCanalMIDI.enabled_(true);
					16.do({arg canal; midiOut.allNotesOff(canal); if(flagVST =='on', {~fxVST.midi.noteOff(canal)});
					});
					MIDIIn.connect;
				});
			});
		};
		// Switch Canal MIDI
		switchCanalMIDI = PopUpMenu(windowControl,Rect(0, 0, 100, 20)).background_(Color.grey(0.5, 0.8)).stringColor_(Color.black).items=["MIDI in 1", "MIDI in 2", "MIDI in 3", "MIDI in 4", "MIDI in 5", "MIDI in 6", "MIDI in 7", "MIDI in 8", "MIDI in 9", "MIDI in 10", "MIDI in 11", "MIDI in 12", "MIDI in 13", "MIDI in 14", "MIDI in 15", "MIDI in 16"];
		switchCanalMIDI.action = {|view|
			canalMIDI = view.value;
		};
		switchCanalMIDI.enabled_(false);
		// Flag Data OSC
		sliderDataOSC = Button(windowControl,Rect(0, 0, 20, 20));
		sliderDataOSC.states = [["@", Color.black,  Color.red(0.8, 0.25)],["!", Color.black, Color.green(0.8, 0.25)]];
		sliderDataOSC.action = {|view|
			if(view.value == 0 ,{flagDataOSC = 'on'},{flagDataOSC = 'off'});
		};
		// algo analyse
		algoAnalyse = PopUpMenu(windowControl,Rect(0, 0, 125, 20)).background_(Color.grey(0.5, 0.8)).stringColor_(Color.blue(0.8, 0.75)).items=["Algo Onsets","Algo Pitch","Algo Pitch2","Algo KeyTrack"];
		algoAnalyse.action = {|view|
			seuilAnalyse.valueAction_(parametresAnalyse.at(view.value).at(0));
			filtreAnalyse.valueAction_(parametresAnalyse.at(view.value).at(1));
			if(startSystem.value == 1, {
				if(view.value == 0, {
					synthAnalysePitch.run(false);
					synthAnalysePitch2.run(false);
					synthAnalyseKeyTrack.run(false);
					synthAnalyseOnsets.run(true);
				});
				if(view.value == 1, {
					synthAnalyseOnsets.run(false);
					synthAnalyseKeyTrack.run(false);
					synthAnalysePitch.run(true);
					synthAnalysePitch2.run(false);
				});
				if(view.value == 2, {
					synthAnalyseOnsets.run(false);
					synthAnalyseKeyTrack.run(false);
					synthAnalysePitch.run(false);
					synthAnalysePitch2.run(true);
				});
				if(view.value == 3, {
					synthAnalyseOnsets.run(false);
					synthAnalyseKeyTrack.run(true);
					synthAnalysePitch.run(false);
					synthAnalysePitch2.run(false);
				});
			});
		};
		windowControl.view.decorator.nextLine;
		PopUpMenu(windowControl,Rect(0, 0, 75, 20)).
		items_(["Off", "LoPass", "HiPass"]).
		action = {|filter|
			if(filter.value == 0, {
				groupeAnalyse.setn(\ampInput, 1, \ampLoPass, 0, \ampHiPass, 0);
				windowControl.view.children.at(16).enabled_(false);
			});
			if(filter.value == 1, {
				groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 1, \ampHiPass, 0);
				windowControl.view.children.at(16).enabled_(true);
			});
			if(filter.value == 2, {
				groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 0, \ampHiPass, 1);
				windowControl.view.children.at(16).enabled_(true);
			});
		};
		EZSlider(windowControl, 315 @ 20, "HzPass", \freq,
			{|ez| groupeAnalyse.set(\hzPass, ez.value)}, 440,labelWidth: 50, numberWidth: 50);
		windowControl.view.children.at(16).enabled_(false);
		windowControl.view.decorator.nextLine;
		// Thresh
		seuilAnalyse=EZSlider(windowControl, 190 @ 20, "Thresh", \unipolar, {|ez| parametresAnalyse.at(algoAnalyse.value).put(0, ez.value);
			synthAnalyseOnsets.set(\seuil, ez.value); synthAnalysePitch.set(\seuil, ez.value); synthAnalysePitch2.set(\seuil, ez.value); synthAnalyseKeyTrack.set(\seuil, ez.value)}, 0.5, labelWidth: 55,numberWidth: 40);
		// Filter
		filtreAnalyse=EZSlider(windowControl, 190 @ 20, "Filter",\unipolar, {|ez|
			parametresAnalyse.at(algoAnalyse.value).put(1, ez.value);
			synthAnalyseOnsets.set(\filtre, ez.value); synthAnalysePitch.set(\filtre, ez.value); synthAnalysePitch2.set(\filtre, ez.value); synthAnalyseKeyTrack.set(\filtre, ez.value)}, 0.5, labelWidth: 55,numberWidth: 40);
		windowControl.view.decorator.nextLine;
		// Volume File out
		volumeFileIn=EZSlider(windowControl, 190 @ 20, "File Level", ControlSpec(-inf, 12, \db, 0),
			{|ez| synthFileIn.set(\volume, ez.value.dbamp)}, -inf, labelWidth: 55, numberWidth: 40);
		// file offset
		offsetFileIn=EZSlider(windowControl, 190 @ 20, "File Offset", ControlSpec(0, 1, \lin, 0),
			{|ez|
				s.bind{
					synthFileIn.set(\trigger, -1);
					s.sync;
					synthFileIn.set(\offset, ez.value);
					s.sync;
					synthFileIn.set(\trigger, 1);
					s.sync};
		}, 0, labelWidth: 55, numberWidth: 40);
		windowControl.view.decorator.nextLine;
		// Signal Buffer
		offsetFileIn=EZSlider(windowControl, 385 @ 20, "Signal Buffer", ControlSpec(1, 256, \exp, 1),
			{|ez| signalBuffer = ez.value}, 12, labelWidth: 80, numberWidth: 40);
		windowControl.view.decorator.nextLine;
		// Max Time
		offsetFileIn=EZSlider(windowControl, 190 @ 20, "Time Maximum", ControlSpec(1, 60, \exp, 0),
			{|ez| timeMaximum = ez.value}, 4, labelWidth: 85, numberWidth: 40);
		// Time Memory
		offsetFileIn=EZSlider(windowControl, 190 @ 20, "Time Memory", ControlSpec(0.01666, 3600, \exp, 0),
			{|ez| timeMemory = ez.value}, 4, labelWidth: 85, numberWidth: 40);
		windowControl.view.decorator.nextLine;
		// Filter Fhz
		fhzFiltreGUI=EZSlider(windowControl, 125 @ 20, "Freq Filter", ControlSpec(0, 12, \lin, 0),
			{|ez| fhzFilter = ez.value}, 0, labelWidth: 50, numberWidth: 30);
		// Filter Amp
		ampFiltreGUI=EZSlider(windowControl, 125 @ 20, "Amp Filter", ControlSpec(0, 60, \lin, 0.1),
			{|ez| ampFilter = ez.value}, 0, labelWidth: 55, numberWidth: 30);
		// Filter Dur
		durFiltreGUI=EZSlider(windowControl, 125 @ 20, "Dur Filter", ControlSpec(0.01, 16, \exp, 0),
			{|ez| durFilter = ez.value}, 0.03125, labelWidth: 50, numberWidth: 30);
		windowControl.view.decorator.nextLine;
		// Chord Time
		chordTimeSlider=EZSlider(windowControl, 190 @ 20, "Chord Time", ControlSpec(0.01, 1, \exp, 0),
			{|ez| chordDuree = ez.value}, 0.0833, labelWidth: 75, numberWidth: 40);
		// Chord Size
		chordSizeSlider=EZSlider(windowControl, 190 @ 20, "Chord Size", ControlSpec(0, 12, \lin, 1),
			{|ez| chordSize = ez.value}, 3, labelWidth: 75, numberWidth: 40);
		windowControl.view.decorator.nextLine;
		StaticText(windowControl, Rect(0, 0, 400, 12)).string_("Synthesizer + FX").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		//Add a New Synth or FX
		addNewSynth=PopUpMenu(windowControl, Rect(0, 0, 385, 20)).font_(Font( "Palatino-BoldItalic", 12)).items = choiceSynth;
		addNewSynth.action = {arg item, tampon;
			tampon = listeDataOSC;
			if(item.value != 1, {fonctionAddSynthFX.value(item.value, "Nil", "Nil", 0, 4.0, 4.0)});// Fonction Add Synth or FX
			s.bind{
				listeWindowSynth.last.view.children.at(0).valueAction_(1);// Synth Play On
				s.sync;
				listeWindowSynth.last.view.children.at(0).valueAction_(0);// Synth Play Off
				s.sync;
			};
			listeDataOSC = tampon;
			addNewSynth.value=0;
		};
		windowControl.view.decorator.nextLine;
		// Display OSC
		StaticText(windowControl, Rect(0, 0, 400, 12)).string_("Display OSC Message").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
		windowControl.view.decorator.nextLine;
		displayOSC = TextView(windowControl, Rect(0, 0, 390, 95));
		windowControl.view.decorator.nextLine;
		// Auto Preset
		Button(windowControl,Rect(0, 0, 105, 20)).states_([["Automation Preset Off", Color.black,  Color.green(0.8, 0.25)],["Automation Preset On", Color.black, Color.red(0.8, 0.25)]]).action = {|view| onOffSynth = view.value};
		// % Change Peset
		NumberBox(windowControl,Rect(0, 0, 30, 20)).action_({|view| if(variableChange == "Flux", {onOffSynthValue.put(0, view.value)},{onOffSynthValue.put(1, view.value)})}).step_(0.01).clipLo_(0).clipHi_(10).scroll_step_(0.01).value_(onOffSynthValue.at(0));
		// NumFhzBand
		EZSlider(windowControl, 200 @ 20, "NumFhzBand", ControlSpec(1, 12, \lin, 1),
			{arg band; numFhzBand = band.value.asInteger;
				// Init Band for each synth
				fonctionInitBand.value(numFhzBand);
		}, numFhzBand);
		// On Close
		windowControl.onClose_({
			s.bind{
				listeWindowSynth.do({arg w; w.close});
				s.sync;
				windowMasterFX.close;
				s.sync;
				windowControlSynth.close;
				s.sync;
				windowKeyboard.close;
				s.sync;
				listeWindowSynth.remove(windowControl);
				s.sync;
				oscMusicData.free;
				s.sync;
				oscKeyboardData.free;
				s.sync;
				oscMIDIdata.free;
				s.sync;
				oscHPtempo.free;
				s.sync;
				oscHPstart.free;
				s.sync;
				oscHPrec.free;
				s.sync;
				tdefOSCdata.free;
				s.sync;
				tdefOSCdata.free;
				s.sync;
				serverAdresse.disconnect;
				s.sync;
				menuMatrix.remove;
				s.sync;
			};
			s.quit;
		});
		listeWindows = listeWindows.add(windowControl);
		fonctionShortCut.value(windowControl);

		/////////////////////////////////
		////// Fonction Window for controling Synth + FX (listeGroupeSynth) ///////
		///////////////////

		fonctionWindowSynth={arg name, groupe, synthNumber, bufferOne, bufferTwo, canalIn, timeBuf1, timeBuf2;
			var windowSynth, startStop, sourceOut, sendBusOut, sendBusFX, sendLocalBuf, panSlider, freqSlider, freqTranSlider, ampSlider, dureeSlider, dureeTranSlider, quantaSlider, moveNodeAfter, moveNodeBefore, controlsAntiClick, controlsNode, startAutomationSynthControls, jitterAutomationSynthControls, tempoAutomationSynthControls, startAutomationSynthMusicData, tempoAutomationSynthMusicData, jitterAutomationMusicData, tdefControls, tdefMusicData, switchBufferOne, textBufferOne, switchBufferTwo, textBufferTwo, loopBufferOne, loopBufferTwo, switchBufferOneAction, reverseBufferOneAction, reverseBufferTwoAction, knobPreLevel1, knobPostLevel1, knobRecOn1, knobOffset1, knobPreLevel2, knobPostLevel2, knobRecOn2, knobOffset2,
			switchBufferTwoAction, sourceBusIn, sourceBusOut, sourceFXin, sourceFXout, synthRec, userOperatingSystemSynth, windowView=[], envelopeSynth, tdefSynthesizer, bufferRecording1, bufferRecording2, changeSynth, fonctionEnabledSlider, fonctionEnabledControls, fonctionSynthTdefFX, synthAndFX=nil, recBuffer1, recBuffer2, automationSliderFreq, automationSliderDur, automationSliderSynth, automationNumberSynth, automationSliderBuffer, durSampleOneSlider, durSampleTwoSlider,
			freq=0, amp=0, duree=0.01, dureeTdef=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0, indexMusicData=9999, compteurChord=0, listeFreq=[], listeAmp=[],
			onOff, loop1, loop2, levelOut, levelFX, levelLocal, panLo, panHi, fhzLo, fhzHi, fhzT, dbLo, dbHi, durLo, durHi, durM, quanta, ctrlHP, ctrlSynth, ctrlBuffer=[1, 0, 1, 0, 1, 0, 1, 0, 1], flagAmp, out, busIn, busOut, busFXin, busFXout,
			envLevel, envDuree=[0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125], envTime, switch1, switch2, modeSynth, flagModeSynth='Tdef', octave, ratio, degre, difL, difH, pos, flagAccord = 'off', lastFreqMidi = [], instrCanalMidiOut, canalMIDIinstr=0, midiFreq, midiAmp, menuAlgorithm, stringAlgorithm, newFreq=[], newAmp=[], newDuree=[], listeDataAlgo=[[], [], []], q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie, distances, maxTraining, kohonenF, kohonenA, kohonenD, geneticF, geneticA, geneticD, freqGen = [], ampGen = [], durGen = [], calculNewMusic, neuralFAD, freqNeu=[], ampNeu=[], durNeu=[], flagMidiOut = 'off';
			var indexNumFhzBand, guiNumFhzBand, flagBand, rangeNumFhzBand, flagIndexBand;
			var scale, tuning, degrees, root, flagScaling, flagRoot, fonctionBand;

			flagBand = 'on';
			flagIndexBand = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
			rangeNumFhzBand = [];
			for(0, numFhzBand,
				{arg index;
					rangeNumFhzBand = rangeNumFhzBand.add(index);
			});
			indexNumFhzBand = rangeNumFhzBand.choose;

			// Tuning
			tuning = Tuning.et12;
			degrees = tuning.semitones;
			root = 0;
			scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning);
			flagScaling = 'off';
			pos=scale.degrees.size - 1;
			flagRoot = 0;

			// For Kohonen
			kohonenF = HPclassKohonen.new(1,127,1);
			kohonenA = HPclassKohonen.new(1,127,1);
			kohonenD = HPclassKohonen.new(1,127,1);
			// For Genetic
			geneticF = HPclassGenetiques.new(1, 64);
			geneticA = HPclassGenetiques.new(1, 64);
			geneticD = HPclassGenetiques.new(1, 64);
			// For Neural
			neuralFAD = HPNeuralNet.new(3, 1, [5], 3);

			// Init fhzBand
			fonctionBand = {arg band;
				rangeNumFhzBand = [];
				for(0, numFhzBand,
					{arg index;
						if(flagIndexBand.at(index) == 1, {
							rangeNumFhzBand = rangeNumFhzBand.add(index);
						});
				});
			};

			windowSynth = Window(name ++ "[" ++ groupe.nodeID.asString ++ "]", Rect(rrand(700, 1000), 0, 410, 835), scroll: true);
			windowSynth.alpha=1.0;
			windowSynth.front;
			windowSynth.view.decorator = FlowLayout(windowSynth.view.bounds);
			// StartStop Synth
			startStop = Button(windowSynth,Rect(0, 0, 50, 20)).states=[["Stop", Color.black, Color.green(0.8, 0.25)],["Start", Color.black, Color.red(0.8, 0.25)]];
			startStop.action = {arg view;
				if(view.value == 1, {
					if(startSystem.value == 1, {
						groupe.run(true);
						indexMusicData = 9999; if(flagModeSynth == 'Tdef', {tdefSynthesizer.play});
						if(startAutomationSynthControls.value == 1, {tdefControls.play});
						if(startAutomationSynthMusicData.value == 1, {tdefMusicData.play});
					},
					{
						if(flagModeSynth == 'Tdef', {tdefSynthesizer.reset; tdefSynthesizer.stop}); indexMusicData = 9999;
						groupe.run(false);
						if(startAutomationSynthControls.value == 1, {tdefControls.stop});
						if(startAutomationSynthMusicData.value == 1, {tdefMusicData.stop})});
				},
				{
					if(flagModeSynth == 'Tdef', {tdefSynthesizer.reset; tdefSynthesizer.stop});
					indexMusicData = 9999;
					groupe.run(false);
					if(startAutomationSynthControls.value == 1, {tdefControls.stop});
					if(startAutomationSynthMusicData.value == 1, {tdefMusicData.stop});
					// MIDI OFF
					midiOut.allNotesOff(canalMIDIinstr);
					if(flagVST == 'on', {
						~fxVST.midi.noteOff(canalMIDIinstr)});
				});
				onOff = view.value;
			};
			startStop.focus;
			// User Operating System
			userOperatingSystemSynth=PopUpMenu(windowSynth, Rect(0, 0, 125, 20)).font_(Font( "Palatino-BoldItalic", 12)).items = userOSchoiceInstrument;
			userOperatingSystemSynth.action = {arg item; fonctionUserOperatingSystem.value(item.value, windowSynth); userOperatingSystemSynth.value_(0)};
			windowSynth.view.decorator.nextLine;
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Audio Out / Bus Audio In / Send Bus Audio Out / Bus FX In / Send Bus FX Out").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			windowSynth.view.decorator.nextLine;
			// Source Out
			sourceOut = PopUpMenu(windowSynth,Rect(0, 0, 60, 20)).items = ["Out 1", "Out 2", "Out 3", "Out 4", "Out 5", "Out 6", "Out 7", "Out 8", "Out 9", "Out 10", "Out 11", "Out 12", "Out 13", "Out 14", "Out 15", "Out 16", "Out 17", "Out 18", "Out 19", "Out 20", "Out 21", "Out 22", "Out 23", "Out 24", "Out 25", "Out 26", "Out 27", "Out 28", "Out 29", "Out 30", "Out 31", "Out 32", "Off"];
			sourceOut.action = {arg view;
				if(view.value == 16, {groupe.set(\flagAmpOnOff, 0); flagAmp = 0},{groupe.set(\out, view.value); groupe.set(\flagAmpOnOff, 1); flagAmp = 1; out = view.value});
			};
			// Source Bus In
			sourceBusIn = PopUpMenu(windowSynth,Rect(0, 0, 70, 20)).items = ["Bus In 1", "Bus In 2", "Bus In 3", "Bus In 4", "Bus In 5", "Bus In 6", "Bus In 7", "Bus In 8", "Bus In 9", "Bus In 10", "Bus In 11", "Bus In 12", "Bus In 13", "Bus In 14", "Bus In 15", "Bus In 16", "Bus In 17", "Bus In 18", "Bus In 19", "Bus In 20", "Bus In 21", "Bus In 22", "Bus In 23", "Bus In 24", "Bus In 25", "Bus In 26", "Bus In 27", "Bus In 28", "Bus In 29", "Bus In 30", "Bus In 31", "Bus In 32"];
			sourceBusIn.action = {arg in;
				s.bind{
					synthRec.free;
					s.sync;
					bufferRecording1.free;
					s.sync;
					bufferRecording2.free;
					s.sync;
					busIn = in.value;
					// New RecBuffer Recording
					synthRec = Synth.new("Matrix AudioIn",
						[\in, busIn.value, 'busIn', listeBusInOut.at(busIn.value)], groupe, \addToTail);
					s.sync;
					bufferRecording1 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(busIn.value), \buffer, recBuffer1.bufnum, \offset, ctrlBuffer.at(3), \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \run, ctrlBuffer.at(2), \loop, loop1, \trigger, 0], groupe, \addToTail);
					s.sync;
					bufferRecording2 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(busIn.value), \buffer, recBuffer2.bufnum, \offset, ctrlBuffer.at(8), \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \run, ctrlBuffer.at(7), \loop, loop2, \trigger, 0], groupe, \addToTail);
					s.sync;
					groupe.set(\busIn, listeBusInOut.at(in.value));
					s.sync;
				};
			};
			// Source Bus Out
			sourceBusOut = PopUpMenu(windowSynth,Rect(0, 0, 88, 20)).items = ["Send Bus 1", "Send Bus 2", "Send Bus 3", "Send Bus 4", "Send Bus 5", "Send Bus 6", "Send Bus 7", "Send Bus 8", "Send Bus 9", "Send Bus 10", "Send Bus 11", "Send Bus 12", "Send Bus 13", "Send Bus 14", "Send Bus 15", "Send Bus 16", "Send Bus 17", "Send Bus 18", "Send Bus 19", "Send Bus 20", "Send Bus 21", "Send Bus 22", "Send Bus 23", "Send Bus 24", "Send Bus 25", "Send Bus 26", "Send Bus 27", "Send Bus 28", "Send Bus 29", "Send Bus 30", "Send Bus 31", "Send Bus 32"];
			sourceBusOut.action = {arg out;
				groupe.set(\busOut, listeBusInOut.at(out.value)); busOut = out.value;
			};
			// Source FX IN
			sourceFXin= PopUpMenu(windowSynth,Rect(0, 0, 70, 20)).items = ["FX In 1", "FX In 2", "FX In 3", "FX In 4", "FX In 5", "FX In 6", "FX In 7", "FX In 8", "FX In 9", "FX In 10", "FX In 11", "FX In 12", "FX In 13", "FX In 14", "FX In 15", "FX In 16", "FX In 17", "FX In 18", "FX In 19", "FX In 20", "FX In 21", "FX In 22", "FX In 23", "FX In 24", "FX In 25", "FX In 26", "FX In 27", "FX In 28", "FX In 29", "FX In 30", "FX In 31", "FX In 32"];
			sourceFXin.action = {arg in;
				groupe.set(\busFXin, listeBusFX.at(in.value)); busFXin = in.value;
			};
			// Source FX OUT
			sourceFXout= PopUpMenu(windowSynth,Rect(0, 0, 88, 20)).items = ["Send FX 1", "Send FX 2", "Send FX 3", "Send FX 4", "Send FX 5", "Send FX 6", "Send FX 7", "Send FX 8", "Send FX 9", "Send FX 10", "Send FX 11", "Send FX 12", "Send FX 13", "Send FX 14", "Send FX 15", "Send FX 16", "Send FX 17", "Send FX 18", "Send FX 19", "Send FX 20", "Send FX 21", "Send FX 22", "Send FX 23", "Send FX 24", "Send FX 25", "Send FX 26", "Send FX 27", "Send FX 28", "Send FX 29", "Send FX 30", "Send FX 31", "Send FX 32"];
			sourceFXout.action = {arg out;
				groupe.set(\busFXout, listeBusFX.at(out.value)); busFXout = out.value;
			};
			windowSynth.view.decorator.nextLine;
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Synthesizer Connection").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			// Node Move After
			moveNodeAfter = EZNumber(windowSynth, 120 @ 20, "MoveAfter", ControlSpec(masterFX.nodeID+1, 999999, \lin, 1), {|node| var indexMoi, indexLui, moi;
				if(node.value.asInteger != groupe.nodeID and: {listeGroupeSynthID.includes(node.value.asInteger)}, {
					if(node.value.asInteger >= listeGroupeSynthID.maxItem,
						{groupe.moveToTail(groupeSynth)}, {
							listeGroupeSynth.do({arg synth, index; if(synth.nodeID == node.value.asInteger,
								{groupe.moveAfter(synth)})})});
					// Re-order listeGroupeSynthID
					indexMoi = listeGroupeSynthID.indexOf(groupe.nodeID); indexLui = listeGroupeSynthID.indexOf(node.value.asInteger);
					moi = listeGroupeSynthID.at(indexMoi);
					listeGroupeSynthID.removeAt(indexMoi);
					if(indexLui <= 0, {listeGroupeSynthID.insert(indexLui + 1, moi)},	{if(indexMoi >= (listeGroupeSynthID.size - 1), {listeGroupeSynthID.insert(indexLui + 1, moi)},{listeGroupeSynthID.insert(indexLui, moi)})});
					//Document.listener.string="";
					s.queryAllNodes});
				if(node.value.asInteger < listeGroupeSynthID.minItem, {moveNodeAfter.value_(listeGroupeSynthID.minItem)});
				if(node.value.asInteger > listeGroupeSynthID.maxItem, {moveNodeAfter.value_(listeGroupeSynthID.maxItem)});
			}, masterFX.nodeID+1, labelWidth: 68, numberWidth: 52);
			// Node Move Before
			moveNodeBefore = EZNumber(windowSynth, 120 @ 20, "MoveBefore", ControlSpec(masterFX.nodeID+1, 999999, \lin, 1), {|node| var indexMoi, indexLui, moi;
				if(node.value.asInteger != groupe.nodeID and: {listeGroupeSynthID.includes(node.value.asInteger)}, {
					if(node.value.asInteger <= listeGroupeSynthID.minItem, {groupe.moveToHead(groupeSynth)}, {
						listeGroupeSynth.do({arg synth, index; if(synth.nodeID == node.value.asInteger,
							{groupe.moveBefore(synth)})})});
					// Re-order listeGroupeSynthID
					indexMoi = listeGroupeSynthID.indexOf(groupe.nodeID); indexLui = listeGroupeSynthID.indexOf(node.value.asInteger);
					moi = listeGroupeSynthID.at(indexMoi);
					listeGroupeSynthID.removeAt(indexMoi);
					if(indexLui <= 0, {listeGroupeSynthID.insert(indexLui, moi)},	{if(indexMoi >= (listeGroupeSynthID.size - 1), {listeGroupeSynthID.insert(indexLui, moi)},{listeGroupeSynthID.insert(indexLui - 1, moi)})});
					//Document.listener.string="";
					s.queryAllNodes});
				if(node.value < listeGroupeSynthID.minItem, {moveNodeBefore.value_(listeGroupeSynthID.minItem)});
				if(node.value > listeGroupeSynthID.maxItem, {moveNodeBefore.value_(listeGroupeSynthID.maxItem)});
			}, masterFX.nodeID+1, labelWidth: 68, numberWidth: 52).setColors(numTypingColor: Color.red);
			//Change Synthesizer or FX
			changeSynth=PopUpMenu(windowSynth, Rect(0, 0, 140, 20)).font_(Font( "Palatino-BoldItalic", 10)).stringColor_(Color.black).items = changeChoiceSynth;
			changeSynth.action = {arg item;
				s.bind{
					if(item.value != 1, {
						tdefSynthesizer.clear;
						s.sync;
						tdefSynthesizer.remove;
						s.sync;
						//tdefSynthesizer.free;
						//s.sync;
						if(synthAndFX != nil, {synthAndFX.free});
						s.sync;
						synthNumber = item.value;
						name = changeChoiceSynth.at(synthNumber).asString; windowSynth.name = name ++ "[" ++ groupe.nodeID.asString ++ "]";
						s.sync;
						fonctionEnabledSlider.value(startAutomationSynthMusicData.value);// Setup Sliders
						s.sync;
						//fonctionEnabledControls.value;// Setup Controls Synth
						//s.sync;
						fonctionSynthTdefFX.value;// Setup SynthFX
						s.sync;
						//Document.listener.string="";
						s.queryAllNodes;
						s.sync;
						ctrlSynth=controlsNode.value;
						s.sync;
						changeSynth.value=0;
						if(startStop.value == 1, {startStop.valueAction_(0); s.sync; startStop.valueAction_(1); s.sync});
					});
				};
			};
			windowSynth.view.decorator.nextLine;
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Bus Audio + FX Send Level").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			// send Bus Out
			sendBusOut = EZSlider(windowSynth, 390 @ 20, "Send Bus", \db,
				{|ez| groupe.set(\levelBusOut, ez.value.dbamp); levelOut = ez.value.dbamp}, -inf, false, 100, 50);
			windowSynth.view.decorator.nextLine;
			// sendBusFX
			sendBusFX = EZSlider(windowSynth, 390 @ 20, "Send Bus FX", \db,
				{|ez| groupe.set(\levelBusFX, ez.value.dbamp); levelFX = ez.value.dbamp}, -inf, false, 100, 50);
			windowSynth.view.decorator.nextLine;
			// sendLocalBuf
			sendLocalBuf = EZSlider(windowSynth, 390 @ 20, "Local In", \db,
				{|ez| groupe.set(\levelLocalIn, ez.value.dbamp); levelLocal = ez.value.dbamp}, -inf, false, 100, 50);
			windowSynth.view.decorator.nextLine;
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Buffer Audio for SynthDef").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			windowSynth.view.decorator.nextLine;
			// Button BufferOne Action
			switchBufferOneAction= Button(windowSynth, Rect(0, 0, 20, 20)).states=[["O", Color.black, Color.green(0.8, 0.25)],["X", Color.black, Color.red(0.8, 0.25)]];
			switchBufferOneAction.action = {|view| groupe.set(\switchBuffer1, view.value); switch1 = view.value};
			// Buffer sampler one
			switchBufferOne = Button(windowSynth, Rect(0, 0, 100, 20)).states=[["Load Buffer I", Color.red(0.8, 0.6), Color.grey(0.75, 0.25)]];
			switchBufferOne.action = {|view|
				Dialog.openPanel({ arg paths;
					bufferOne = fonctionLoadSample.value(paths, groupe, bufferOne);
					//textBufferOne.string_(PathName.new(paths).fileName);
					textBufferOne.string_(paths);
					groupe.set(\bufferOne, bufferOne);
				},{"cancelled".postln});
			};
			// Loop Sample one
			loopBufferOne = Button(windowSynth,Rect(0, 0, 20, 20)).states = [["!", Color.black,  Color.green(0.8, 0.25)],["@", Color.black, Color.red(0.8, 0.25)]];
			loopBufferOne.action = {|view|
				if(view.value == 0, {groupe.set(\loopOne, 0); loop1 = 0},{groupe.set(\loopOne, 1); loop1 = 1});
			};
			// Duree Sample One
			durSampleOneSlider = NumberBox(windowSynth, 50 @ 20).minDecimals_(4).maxDecimals_(4);
			durSampleOneSlider.action = {|time|
				if(time.value < 0.01, {time.value = 0.01});
				if(time.value > 60, {time.value = 60});
				s.bind{
					recBuffer1.free;
					s.sync;
					recBuffer1 = Buffer.alloc(s, s.sampleRate * time.value, 1);
					s.sync;
					/*bufferRecording1.free;
					s.sync;
					bufferRecording1 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(busIn.value), \buffer, recBuffer1.bufnum, \offset, ctrlBuffer.at(3), \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \run,  ctrlBuffer.at(2), \loop, loop1, \trigger, 0], groupe, \addToHead);
					s.sync;*/
				};
			};
			durSampleOneSlider.value_(timeBuf1);
			durSampleOneSlider.step_(0.01);
			// Button Reverse one
			reverseBufferOneAction= Button(windowSynth, Rect(0, 0, 20, 20)).states=[["->", Color.black, Color.green(0.8, 0.25)],["<-", Color.black, Color.red(0.8, 0.25)]];
			reverseBufferOneAction.action = {|view| if(view.value == 0 , {groupe.set(\reverse1, 1); ctrlBuffer.put(4, 1)}, {groupe.set(\reverse1, 1.neg); ctrlBuffer.put(4, 1.neg)})};
			// Text Buffer One
			textBufferOne = StaticText(windowSynth, Rect(0, 0, 155, 20)).string_("Nil").stringColor_(Color.yellow).font_(Font("Georgia", 8)).align_(\center);
			windowSynth.view.decorator.nextLine;
			// Knob for recording buffer one
			knobOffset1 = EZSlider(windowSynth, 150 @ 15, "Offset", ControlSpec(0, 1, \lin, 0), {|ez| groupe.set(\offset1, ez.value); ctrlBuffer.put(3, ez.value)}, 0, labelWidth: 40, numberWidth: 30);
			knobPreLevel1 = EZSlider(windowSynth, 95 @ 15, "Pre", ControlSpec(0, 1, \lin, 0), {|ez| bufferRecording1.set(\preLevel, ez.value); ctrlBuffer.put(0, ez.value)}, 1, labelWidth: 30, numberWidth: 30).view.children.at(2).decimals = 4;
			knobPostLevel1 = EZSlider(windowSynth, 95 @ 15, "Post", ControlSpec(0, 1, \lin, 0), {|ez| bufferRecording1.set(\postLevel, ez.value); ctrlBuffer.put(1, ez.value)}, 0, labelWidth: 30, numberWidth: 30).view.children.at(2).decimals = 4;
			knobRecOn1 = Button(windowSynth, Rect(0, 0, 40, 16)).states=[["Rec", Color.black, Color.green(0.8, 0.25)],["Rec", Color.black, Color.red(0.8, 0.25)]];
			knobRecOn1.action = {|view| bufferRecording1.set(\loop, view.value); ctrlBuffer.put(2, view.value)};
			windowSynth.view.decorator.nextLine;
			// Button BufferTwo Action
			switchBufferTwoAction= Button(windowSynth, Rect(0, 0, 20, 20)).states=[["O", Color.black, Color.green(0.8, 0.25)],["X", Color.black, Color.red(0.8, 0.25)]];
			switchBufferTwoAction.action = {|view| groupe.set(\switchBuffer2, view.value); switch2 = view.value};
			// Buffer sampler two
			switchBufferTwo = Button(windowSynth, Rect(0, 0, 100, 20)).states=[["Load Buffer II", Color.blue(0.8, 0.6), Color.grey(0.75, 0.25)]];
			switchBufferTwo.action = {|view|
				Dialog.openPanel({ arg paths;
					bufferTwo = fonctionLoadSample.value(paths, groupe, bufferTwo);
					//textBufferTwo.string_(PathName.new(paths).fileName);
					textBufferTwo.string_(paths);
					groupe.set(\bufferTwo, bufferTwo);
				},{"cancelled".postln});
			};
			// Loop Sample Two
			loopBufferTwo = Button(windowSynth,Rect(0, 0, 20, 20)).states = [["!", Color.black,  Color.green(0.8, 0.25)],["@", Color.black, Color.red(0.8, 0.25)]];
			loopBufferTwo.action = {|view|
				if(view.value == 0, {groupe.set(\loopTwo, 0); loop2 = 0},{groupe.set(\loopTwo, 1); loop2 = 1});
			};
			// Duree Sample Two
			durSampleTwoSlider = NumberBox(windowSynth, 50 @ 20).minDecimals_(4);
			durSampleTwoSlider.action = {|time|
				if(time.value < 0.01, {time.value = 0.01});
				if(time.value > 60, {time.value = 60});
				s.bind{
					recBuffer2.free;
					s.sync;
					recBuffer2 = Buffer.alloc(s, s.sampleRate * time.value, 1);
					s.sync;
					/*bufferRecording2.free;
					s.sync;
					bufferRecording2 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(busIn.value), \buffer, recBuffer2.bufnum, \offset, ctrlBuffer.at(8), \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \run, ctrlBuffer.at(7), \loop, loop2, \trigger, 0], groupe, \addToHead);
					s.sync;*/
				};
			};
			durSampleTwoSlider.value_(timeBuf2);
			durSampleTwoSlider.step_(0.01);
			// Button Reverse two
			reverseBufferTwoAction= Button(windowSynth, Rect(0, 0, 20, 20)).states=[["->", Color.black, Color.green(0.8, 0.25)],["<-", Color.black, Color.red(0.8, 0.25)]];
			reverseBufferTwoAction.action = {|view| if(view.value == 0 , {groupe.set(\reverse2, 1); ctrlBuffer.put(9, 1)}, {groupe.set(\reverse2, 1.neg); ctrlBuffer.put(9, 1.neg)})};
			// Text Buffer Two
			textBufferTwo = StaticText(windowSynth, Rect(0, 0, 155, 20)).string_("Nil").stringColor_(Color.yellow).font_(Font("Georgia", 8)).align_(\right);
			windowSynth.view.decorator.nextLine;
			// Knob for recording buffer two
			knobOffset2 = EZSlider(windowSynth, 150 @ 15, "Offset", ControlSpec(0, 1, \lin, 0), {|ez| groupe.set(\offset2, ez.value); ctrlBuffer.put(8, ez.value)}, 0, labelWidth: 40, numberWidth: 30);
			knobPreLevel2 = EZSlider(windowSynth, 95 @ 15, "Pre", ControlSpec(0, 1, \lin, 0), {|ez| bufferRecording2.set(\preLevel, ez.value); ctrlBuffer.put(5, ez.value)}, 1, labelWidth: 30, numberWidth: 30);
			knobPostLevel2 = EZSlider(windowSynth, 95 @ 15, "Post", ControlSpec(0, 1, \lin, 0), {|ez| bufferRecording2.set(\postLevel, ez.value); ctrlBuffer.put(6, ez.value)}, 0, labelWidth: 30, numberWidth: 30);
			knobRecOn2 = Button(windowSynth, Rect(0, 0, 40, 16)).states=[["Rec", Color.black, Color.green(0.8, 0.25)],["Rec", Color.black, Color.red(0.8, 0.25)]];
			knobRecOn2.action = {|view| bufferRecording2.set(\loop, view.value); ctrlBuffer.put(7, view.value)};
			windowSynth.view.decorator.nextLine;

			// Load and Set Buffer 1 et 2
			s.bind{
				if(bufferOne != "Nil", {bufferOne = fonctionLoadSample.value(bufferOne, listeGroupeSynth.at(listeGroupeSynth.size - 1), nil)},{bufferOne = Buffer.alloc(s, s.sampleRate * timeBuf1, 1)});
				s.sync;
				if(bufferTwo != "Nil", {bufferTwo = fonctionLoadSample.value(bufferTwo, listeGroupeSynth.at(listeGroupeSynth.size - 1), nil)},{bufferTwo = Buffer.alloc(s, s.sampleRate * timeBuf2, 1)});
				s.sync;
				recBuffer1 = Buffer.alloc(s, s.sampleRate * timeBuf1, 1);
				s.sync;
				recBuffer2 = Buffer.alloc(s, s.sampleRate * timeBuf2, 1);
				s.sync;
				// New RecBuffer Recording
				synthRec = Synth.new("Matrix AudioIn",
					[\in, canalIn, 'busIn', listeBusInOut.at(canalIn)], groupe, \addToTail);
				s.sync;
				bufferRecording1 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(canalIn), \buffer, recBuffer1.bufnum, \offset, ctrlBuffer.at(3), \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \run, ctrlBuffer.at(2), \loop, loop1, \trigger, 0], groupe, \addToTail);
				s.sync;
				bufferRecording2 = Synth.new("RecBuffer", [\busIn, listeBusInOut.at(canalIn), \buffer, recBuffer2.bufnum, \offset, ctrlBuffer.at(8), \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \run, ctrlBuffer.at(7), \loop, loop2, \trigger, 0], groupe, \addToTail);
				s.sync;
			};

			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Musical Data").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			// Pan
			panSlider=EZRanger(windowSynth, 390 @ 20, "Pan", \bipolar,
				{|ez| groupe.set(\panLo, ez.lo);groupe.set(\panHi, ez.hi); panLo = ez.lo; panHi = ez.hi}, [-0.1, 0.1], false, 100, 50);
			windowSynth.view.decorator.nextLine;
			// Freq
			freqSlider = EZRanger(windowSynth, 390 @ 20, "Freq", ControlSpec(0, 127, \lin, 0),
				{|ez| groupe.set(\freqLo, ez.lo);groupe.set(\freqHi, ez.hi); fhzLo = ez.lo; fhzHi = ez.hi},[0, 127],labelWidth: 100, numberWidth: 50);
			// Freq T
			freqTranSlider=EZSlider(windowSynth, 390 @ 20, "Freq Transpose", ControlSpec(-127, 127, \lin, 0), {|ez| groupe.set(\freqT, ez.value); fhzT = ez.value}, 0, labelWidth: 100, numberWidth: 50);
			windowSynth.view.decorator.nextLine;
			// Amp
			ampSlider = EZRanger(windowSynth, 390 @ 20, "Amp", \db,
				{|ez| groupe.set(\ampLo, ez.lo.dbamp);groupe.set(\ampHi, ez.hi.dbamp); dbLo = ez.lo.dbamp; dbHi = ez.hi.dbamp},[-inf, 0],labelWidth: 100, numberWidth: 50);
			windowSynth.view.decorator.nextLine;
			// Duree
			dureeSlider = EZRanger(windowSynth, 390 @ 20, "Dur", ControlSpec(0, 60, \lin, 0),
				{|ez| groupe.set(\durLo, ez.lo);groupe.set(\durHi, ez.hi); durLo = ez.lo; durHi = ez.hi},[0, 4],labelWidth: 100, numberWidth: 50);
			// Duree T
			dureeTranSlider=EZSliderTempo(windowSynth, 390 @ 20, "Dur Stretch", ControlSpec(-100, 100, \lin, 0), {|ez|
				if(ez.value <= 0, {groupe.set(\durM, ez.value.abs.reciprocal); durM = ez.value.abs.reciprocal},
					{groupe.set(\durM, ez.value); durM = ez.value});
			}, 1, labelWidth: 100, numberWidth: 50);
			// Quantization
			quantaSlider=EZSlider(windowSynth, 390 @ 20, "Quant", ControlSpec(1, 100, \lin, 1),
				{|ez| systemBPM.schedAbs(systemBPM.beats, {groupe.set(\quanta, ez.value)}); quanta = ez.value}, 100, labelWidth: 100, numberWidth: 50);
			windowSynth.view.decorator.nextLine;
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Envelope Synthesizer").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			// Envelope
			envelopeSynth = EnvelopeView(windowSynth, Rect(0, 0, 390, 75));
			envelopeSynth.drawLines_(true);
			envelopeSynth.selectionColor_(Color.red);
			envelopeSynth.fillColor_(Color(0, 0.25, 0.5));
			envelopeSynth.strokeColor_(Color.cyan);
			envelopeSynth.drawRects_(true);
			envelopeSynth.step_(0.001);
			envelopeSynth.gridOn_(true);
			envelopeSynth.resize_(8);
			envelopeSynth.thumbSize_(24);
			envelopeSynth.value_([[0.0, 0.015625, 0.125, 0.375, 0.625, 0.75, 0.875, 1.0], [0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0]]);
			//envelopeSynth.curves_(5);
			envelopeSynth.gridColor_(Color.grey);
			envelopeSynth.action={arg env;
				var time;
				time = env.value.at(0);
				envDuree.size.do({arg i; envDuree.put(i, abs(time.wrapAt(i+1) - time.wrapAt(i)))});
				envLevel = env.value.at(1);
				groupe.set('envLevel', envLevel.at(0), 'envLevel2', envLevel.at(1), 'envLevel3', envLevel.at(2), 'envLevel4', envLevel.at(3), 'envLevel5', envLevel.at(4),  'envLevel6', envLevel.at(5),  'envLevel7', envLevel.at(6),  'envLevel8', envLevel.at(7), 'envTime1', envDuree.at(0), 'envTime2', envDuree.at(1), 'envTime3', envDuree.at(2), 'envTime4', envDuree.at(3), 'envTime5', envDuree.at(4), 'envTime6', envDuree.at(5), 'envTime7', envDuree.at(6));
			};
			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("AntiClicks (HPplugins)        /        Synthesizeur Controls").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			// Controls antiClick Synth (plugins hp)
			controlsAntiClick=MultiSliderView(windowSynth, Rect(5, 0, 50, 85)).value_([0.33, 0.5])
			.fillColor_(Color.blue)
			.strokeColor_(Color.yellow)
			.xOffset_(4)
			.thumbSize_(19)
			.elasticMode_(1);
			controlsAntiClick.action={|controls| groupe.set(\ctrlHP1, controls.value.at(0), \ctrlHP2, controls.value.at(1)); ctrlHP = controls.value};
			// Controls Nodes
			controlsNode=MultiSliderView(windowSynth, Rect(10, 0, 335, 85)).value_([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5])
			.fillColor_(Color.blue)
			.strokeColor_(Color.yellow)
			.xOffset_(5)
			.thumbSize_(22)
			.elasticMode_(1);
			controlsNode.action={|controls| groupe.set(\ctrl1, controls.value.at(0), \ctrl2, controls.value.at(1), \ctrl3, controls.value.at(2), \ctrl4, controls.value.at(3), \ctrl5, controls.value.at(4), \ctrl6, controls.value.at(5), \ctrl7, controls.value.at(6), \ctrl8, controls.value.at(7), \ctrl9, controls.value.at(8), \ctrl10, controls.value.at(9), \ctrl11, controls.value.at(10), \ctrl12, controls.value.at(11)); ctrlSynth = controls.value};
			windowSynth.view.decorator.nextLine;

			StaticText(windowSynth, Rect(0, 0, 400, 11)).string_("Automation Synthesizer").stringColor_(Color.yellow).font_(Font("Georgia", 10)).align_(\center);
			windowSynth.view.decorator.nextLine;
			// AutomationControls start stop playing
			startAutomationSynthControls = Button(windowSynth,Rect(0, 0, 100, 20));
			startAutomationSynthControls.states = [["Controls Off", Color.black,  Color.green(0.8, 0.25)],["Controls On", Color.black, Color.red(0.8, 0.25)]];
			startAutomationSynthControls.action = {|view| if(view.value == 0, {tdefControls.stop; ctrlSynth=controlsNode.value; jitterAutomationSynthControls.enabled_(false); tempoAutomationSynthControls.enabled_(false)}, {ctrlSynth=controlsNode.value; tdefControls.play; jitterAutomationSynthControls.enabled_(true); tempoAutomationSynthControls.enabled_(true)});
			};
			// Tempo AutomationControls Synth
			tempoAutomationSynthControls=EZRanger(windowSynth, 180 @ 20, "Tempo", ControlSpec(16.reciprocal, 100.0, \exp, 0), {|tempo| }, [12, 24], labelWidth: 40, numberWidth: 40);
			// Jitter AutomationControls Synth
			jitterAutomationSynthControls=EZSlider(windowSynth, 100 @ 20, "Jitter", ControlSpec(0.0, 1.0, \lin, 0), {|jitter| }, 0.1, labelWidth: 30, numberWidth: 30);
			windowSynth.view.decorator.nextLine;
			// AutomationSynthMusicData start stop playing
			startAutomationSynthMusicData = Button(windowSynth,Rect(0, 0, 100, 20));
			startAutomationSynthMusicData.states = [["MusicData Off", Color.black,  Color.green(0.8, 0.25)],["MusicData On", Color.black, Color.red(0.8, 0.25)]];
			startAutomationSynthMusicData.action = {|view| if(view.value == 0, {tdefMusicData.stop; tempoAutomationSynthMusicData.enabled_(false); jitterAutomationMusicData.enabled_(false); windowSynth.view.children.at(80).children.at(2).valueAction_(0)}, {tdefMusicData.play; tempoAutomationSynthMusicData.enabled_(true);jitterAutomationMusicData.enabled_(true)}); fonctionEnabledSlider.value(view.value);
			};
			// Tempo AutomationSynthMusicData Synth
			tempoAutomationSynthMusicData = EZRanger(windowSynth, 180 @ 20, "Tempo", ControlSpec(60.reciprocal, 100.0, \exp, 0), {|tempo| }, [0.125, 1.0], labelWidth: 40, numberWidth: 40);
			// Jitter AutomationMusic Synth
			jitterAutomationMusicData=EZSlider(windowSynth, 100 @ 20, "Jitter", ControlSpec(0.0, 1.0, \lin, 0), {|jitter| }, 0.1, labelWidth: 30, numberWidth: 30);
			windowSynth.view.decorator.nextLine;
			// Auto Freq
			automationSliderFreq = Button(windowSynth,Rect(0, 0, 35, 20)).states = [["Freq", Color.black,  Color.green(0.8, 0.25)],["Freq", Color.black, Color.red(0.8, 0.25)]];
			automationSliderFreq.action = {|view| };
			// Auto Dur
			automationSliderDur= Button(windowSynth,Rect(0, 0, 35, 20)).states = [["Dur", Color.black,  Color.green(0.8, 0.25)],["Dur", Color.black, Color.red(0.8, 0.25)]];
			automationSliderDur.action = {|view| };
			// Auto Buffer
			automationSliderBuffer= Button(windowSynth,Rect(0, 0, 35, 20)).states = [["CtrlBuf", Color.black,  Color.green(0.8, 0.25)],["CtrlBuf", Color.black, Color.red(0.8, 0.25)]];
			automationSliderBuffer.action = {|view| };
			// Auto Synth
			automationSliderSynth = Button(windowSynth,Rect(0, 0, 35, 20)).states = [["Synth", Color.black,  Color.green(0.8, 0.25)],["Synth", Color.black, Color.red(0.8, 0.25)]];
			automationSliderSynth.action = {|view| };
			// % Change Synth
			automationNumberSynth = NumberBox(windowSynth,Rect(0, 0, 25, 20)).minDecimals_(4);
			automationNumberSynth.action = {|view|};
			automationNumberSynth.step_(0.01); automationNumberSynth.clipLo_(0); automationNumberSynth.clipHi_(10); automationNumberSynth.scroll_step_(0.01); automationNumberSynth.value_(onOffSynthValue.at(0));
			automationSliderFreq.enabled_(false); automationSliderDur.enabled_(false); automationSliderSynth.enabled_(false); automationNumberSynth.enabled_(false); automationSliderBuffer.enabled_(false);
			// Mode Synth or Pattern
			//modeSynth = Button(windowSynth,Rect(0, 0, 45, 20)).states = [["Synth @", Color.red,  Color.grey(0.8, 0.25)],["Synth !", Color.blue, Color.grey(0.8, 0.25)]];
			modeSynth = PopUpMenu(windowSynth,Rect(0, 0, 55, 20)).background_(Color.grey(0.8, 0.25)).stringColor_(Color.red).items=["Tdef","OnFly1", "OnFly2"];
			modeSynth.action = {|view| switch(view.value,
				0, {flagModeSynth = 'Tdef'},
				1, {flagModeSynth = 'OnFly1'},
				2, {flagModeSynth = 'OnFly2'}
			);
			changeSynth.valueAction = (changeChoiceSynth.indexOf(name.asSymbol));// Reset Mode Synth
			};
			//Instr MIDI OUT
			instrCanalMidiOut = PopUpMenu(windowSynth,Rect(0, 0, 75, 20)).background_(Color.grey(0.5, 0.8)).stringColor_(Color.yellow).items=["MIDI out Off","MIDI out 1", "MIDI out 2", "MIDI out 3", "MIDI out 4", "MIDI out 5", "MIDI out 6", "MIDI out 7", "MIDI out 8", "MIDI out 9", "MIDI out 10", "MIDI out 11", "MIDI out 12", "MIDI out 13", "MIDI out 14", "MIDI out 15", "MIDI out 16"];
			instrCanalMidiOut.action = {|view|
				if(view.value == 0, {flagMidiOut = 'off'}, {flagMidiOut = 'on'});
				// MIDI OFF
				midiOut.allNotesOff(canalMIDIinstr);
				if(flagVST == 'on', {
					~fxVST.midi.noteOff(canalMIDIinstr)});
				canalMIDIinstr = view.value - 1;
			};
			instrCanalMidiOut.valueAction_(0);
			//Menu Algorithm
			menuAlgorithm = PopUpMenu(windowSynth,Rect(0, 0, 70, 20)).background_(Color.grey(0.5, 0.8)).stringColor_(Color.yellow).items=listeAlgorithm;
			menuAlgorithm.action = {|algo| stringAlgorithm = listeAlgorithm.at(algo.value)};
			menuAlgorithm.valueAction_(0);

			// SynthBand
			StaticText(windowSynth, 25 @ 20).string = "Band"; // 65
			// Band 0 to 12
			Button.new(windowSynth, 25 @ 20).
			states_([["0", Color.green], ["0", Color.red]]).
			action_({arg band; flagIndexBand.put(0, band.value); fonctionBand.value(0)}); // 66 all data
			Button.new(windowSynth, 25 @ 20).
			states_([["1", Color.green], ["1", Color.red]]).
			action_({arg band; flagIndexBand.put(1, band.value); fonctionBand.value(1)});
			Button.new(windowSynth, 25 @ 20).
			states_([["2", Color.green], ["2", Color.red]]).
			action_({arg band; flagIndexBand.put(2, band.value); fonctionBand.value(2)});
			Button.new(windowSynth, 25 @ 20).
			states_([["3", Color.green], ["3", Color.red]]).
			action_({arg band; flagIndexBand.put(3, band.value); fonctionBand.value(3)});
			Button.new(windowSynth, 25 @ 20).
			states_([["4", Color.green], ["4", Color.red]]).
			action_({arg band; flagIndexBand.put(4, band.value); fonctionBand.value(4)});
			Button.new(windowSynth, 25 @ 20).
			states_([["5", Color.green], ["5", Color.red]]).
			action_({arg band; flagIndexBand.put(5, band.value); fonctionBand.value(5)});
			Button.new(windowSynth, 25 @ 20).
			states_([["6", Color.green], ["6", Color.red]]).
			action_({arg band; flagIndexBand.put(6, band.value); fonctionBand.value(6)});
			Button.new(windowSynth, 25 @ 20).
			states_([["7", Color.green], ["7", Color.red]]).
			action_({arg band; flagIndexBand.put(7, band.value); fonctionBand.value(7)});
			Button.new(windowSynth, 25 @ 20).
			states_([["8", Color.green], ["8", Color.red]]).
			action_({arg band; flagIndexBand.put(8, band.value); fonctionBand.value(8)});
			Button.new(windowSynth, 25 @ 20).
			states_([["9", Color.green], ["9", Color.red]]).
			action_({arg band; flagIndexBand.put(9, band.value); fonctionBand.value(9)});
			Button.new(windowSynth, 25 @ 20).
			states_([["10", Color.green], ["10", Color.red]]).
			action_({arg band; flagIndexBand.put(10, band.value); fonctionBand.value(10)});
			Button.new(windowSynth, 25 @ 20).
			states_([["11", Color.green], ["11", Color.red]]).
			action_({arg band; flagIndexBand.put(11, band.value); fonctionBand.value(11)});
			Button.new(windowSynth, 25 @ 20).
			states_([["12", Color.green], ["12", Color.red]]).
			action_({arg band; flagIndexBand.put(12, band.value); fonctionBand.value(12)});// 78

			// Tuning
			PopUpMenu(windowSynth, Rect(0, 0, 95, 20)).
			items_(["No Scale", "- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"]).
			action = {arg item;
				// Setup GUI Value
				windowSynth.view.children.at(80).children.at(1).valueAction_(12);
				windowSynth.view.children.at(80).children.at(1).valueAction_(0);
				windowSynth.view.children.at(80).enabled_(true);
				windowSynth.view.children.at(82).enabled_(true);
				switch(item.value,
					// No Scale
					0, {
						flagScaling = 'off';
						// Setup GUI Value
						windowSynth.view.children.at(80).children.at(1).valueAction_(12);
						windowSynth.view.children.at(80).children.at(1).valueAction_(0);
						windowSynth.view.children.at(80).enabled_(false);
						windowSynth.view.children.at(82).enabled_(false);
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
					windowSynth.view.children.at(82).children.at(1).valueAction = degrees.asString;
				});
				if(item.value > 28, {tuning = Tuning.sruti; scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning); flagScaling = 'on';
					// Setup GUI Value
					windowSynth.view.children.at(82).children.at(1).valueAction = degrees.asString;
				});
			};
			// Root
			EZSlider(windowSynth, 95 @ 20, "Root", ControlSpec(0, 21, \lin, 1),
				{|ez| root = ez.value; scale = Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)}, 0, labelWidth: 20, numberWidth: 20);
			// Auto Root
			Button(windowSynth,Rect(0, 0, 20, 20)).
			states_([["!", Color.black, Color.green(0.8, 0.25)],["@", Color.black, Color.red(0.8, 0.25)]]).
			action_({arg view; flagRoot = view.value;
				if(view.value == 0, {windowSynth.view.children.at(80).children.at(2).valueAction_(0)});
			});
			// Degrees
			EZText(windowSynth, Rect(0, 0, 390, 20), "Degrees",
				{arg string; degrees = string.value; scale=Scale.new(((degrees + root)%tuning.size).sort, tuning.size, tuning)},
				degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true);
			windowSynth.view.children.at(80).enabled_(false);
			windowSynth.view.children.at(82).enabled_(false);

			for(0, numFhzBand,
				{arg index;
					windowSynth.view.children.at(66 + index).enabled_(true);

			});
			if(numFhzBand < 12, {
				for(numFhzBand + 1, 12,
					{arg index;
						windowSynth.view.children.at(66 + index).enabled_(false);
						windowSynth.view.children.at(66 + index).valueAction_(0);
				});
			});

			// Fonction AutoControls
			fonctionTdefControls = {arg window, groupe, newTempo=24;
				Tdef("AutoSynthCtrl"++groupe.nodeID.asString, {
					loop({
						// Update controls
						{
							if(window.isClosed  == false, {
								if(window.view.children.at(0).value == 1 and: {window.view.children.at(51).value == 1}, {
									// Controls Synth
									ctrlSynth = (window.view.children.at(49).value + [window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0), window.view.children.at(53).children.at(1).value * rrand(-1.0, 1.0)]).clip(0.001, 0.99);
									groupe.set(\ctrl1, ctrlSynth.at(0), \ctrl2, ctrlSynth.at(1), \ctrl3, ctrlSynth.at(2), \ctrl4, ctrlSynth.at(3), \ctrl5, ctrlSynth.at(4), \ctrl6, ctrlSynth.at(5), \ctrl7, ctrlSynth.at(6), \ctrl8, ctrlSynth.at(7), \ctrl9, ctrlSynth.at(8), \ctrl10, ctrlSynth.at(9), \ctrl11, ctrlSynth.at(10), \ctrl12, ctrlSynth.at(11));
								});
								newTempo = rrand(window.view.children.at(52).children.at(1).value, window.view.children.at(52).children.at(3).value);
							});
						}.defer;
						(newTempo.reciprocal * systemBPM.tempo).wait;
					});
				});
			};

			// Fonction AutoMusicData
			fonctionTdefMusicData={arg window, groupe, newTempo;
				var musicData, q1Freq, medianeFreq, q3Freq, ecartqFreq, ecartsemiqFreq, varianceFreq, ecarttypeFreq, dissymetrieFreq, freq,
				//q1Amp, medianeAmp, q3Amp, ecartqAmp, ecartsemiqAmp, varianceAmp, ecarttypeAmp, dissymetrieAmp, amp,
				q1Duree, medianeDuree, q3Duree, ecartqDuree, ecartsemiqDuree, varianceDuree, ecarttypeDuree, dissymetrieDuree,
				q1FreqCentroid, medianeFreqCentroid, q3FreqCentroid, ecartqFreqCentroid, ecartsemiqFreqCentroid,
				varianceFreqCentroid, ecarttypeFreqCentroid, dissymetrieFreqCentroid,
				q1Flatness, medianeFlatness, q3Flatness, ecartqFlatness, ecartsemiqFlatness,
				varianceFlatness, ecarttypeFlatness, dissymetrieFlatness, meanFlatness,
				q1Energy, medianeEnergy, q3Energy, ecartqEnergy, ecartsemiqEnergy,
				varianceEnergy, ecarttypeEnergy, dissymetrieEnergy,
				q1Flux, medianeFlux, q3Flux, ecartqFlux, ecartsemiqFlux,
				varianceFlux, ecarttypeFlux, dissymetrieFlux, meanFlux,
				ctrlBuffer, windowName, synthNumber, newSynth, compteur, meanProbaPreset=0, lastMeanProbaPreset=0, q1, q3, ecartsemiq, seuilInt, seuilFrac, newQuantaDur=0, newRoot;
				Tdef("AutoMusicSynth"++groupe.nodeID.asString, {
					loop({
						// Update musicdata
						{
							if(window.isClosed  == false, {
								if(window.view.children.at(0).value == 1 and: {window.view.children.at(54).value == 1}, {
									musicData = listeDataOSC.at(indexNumFhzBand).flop;// Setup Array
									if(musicData.at(0).size >= 1, {
										freq = musicData.at(0).soloArray;
										//Probabilite Freq
										# q1Freq, medianeFreq, q3Freq, ecartqFreq, ecartsemiqFreq = freq.quartiles;
										dissymetrieFreq = freq.dissymetrie;
										// Automation Root
										if(window.view.children.at(79).value != 0 and: {flagRoot == 1},
											{
												newRoot = medianeFreq.cpsoct;
												newRoot = (newRoot.frac * tuning.size + 0.5).floor;	window.view.children.at(80).children.at(2).valueAction_(newRoot);
										});
										// Set Freq Sliders
										if(window.view.children.at(57).value == 1, {
											window.view.children.at(39).children.do({arg subView, subItem;
												if(subItem == 2, {subView.activeLo_((q1Freq + rand2(ecartsemiqFreq * window.view.children.at(56).children.at(1).value)).cpsmidi /127); subView.activeHi_((q3Freq + rand2(ecartsemiqFreq * window.view.children.at(56).children.at(1).value)).cpsmidi / 127)});
											});
											/*window.view.children.at(40).children.do({arg subView, subItem;
											if(subItem == 1, {subView.valueAction_(rand(medianeFreq.cpsmidi * window.view.children.at(56).children.at(1).value * rrand(0, 1) / 127).clip(0, 127) * dissymetrieFreq.sign + 0.5)});
											});*/
										});
										//Probabilite Duree
										# q1Duree, medianeDuree, q3Duree, ecartqDuree, ecartsemiqDuree = musicData.at(2).quartiles;
										dissymetrieDuree=musicData.at(2).dissymetrie;
										newQuantaDur = ((ecartsemiqDuree.reciprocal+0.5).floor / (ecartqDuree.reciprocal+0.5).floor + 0.5).floor * (ecartqDuree.reciprocal+0.5).floor;
										newQuantaDur;
										// Set Duree Sliders
										if(window.view.children.at(58).value == 1, {
											window.view.children.at(42).children.do({arg subView, subItem;
												if(subItem == 2, {subView.activeLo_(q1Duree + rand2(ecartsemiqDuree * window.view.children.at(56).children.at(1).value) * timeMaximum / 60); subView.activeHi_(q3Duree + rand2(ecartsemiqDuree * window.view.children.at(56).children.at(1).value) * timeMaximum / 60)});
											});
											/*window.view.children.at(43).children.do({arg subView, subItem;
											if(subItem == 1, {subView.valueAction_(ecartqDuree + rand(window.view.children.at(56).children.at(1).value) / timeMaximum * dissymetrieDuree.sign / 50 + 0.5)});
											});
											window.view.children.at(44).children.do({arg subView, subItem;
											if(subItem == 1 and: {newQuantaDur > 0 and: {newQuantaDur <= 100}}, {subView.valueAction_((newQuantaDur - 1) / 100)});
											});*/
										});
										// Probabilite sur signal FreqCentroid
										# q1FreqCentroid, medianeFreqCentroid, q3FreqCentroid, ecartqFreqCentroid, ecartsemiqFreqCentroid = musicData.at(4).quartiles;
										dissymetrieFreqCentroid=musicData.at(4).dissymetrie;
										// Probabilite sur signal Energy
										# q1Energy, medianeEnergy, q3Energy, ecartqEnergy, ecartsemiqEnergy = musicData.at(6).quartiles;
										ecarttypeEnergy=musicData.at(6).ecartType;
										dissymetrieEnergy=musicData.at(6).dissymetrie;
										// Setup ctrlBuffer Buffer
										if(window.view.children.at(59).value == 1, {
											// Rec 1 on / off
											window.view.children.at(26).valueAction_((0.5 + ecarttypeEnergy * 	dissymetrieEnergy.sign).clip(0.0, 1.0));
											// Offset Buffer 1
											window.view.children.at(23).children.do({arg subView, subItem;
												if(subItem == 0, {nil},
													{subView.valueAction_((subView.value + (rrand(0.0, 0.3) * 	dissymetrieEnergy.sign))%1.0)})});
											// Reverse 1 on / off
											window.view.children.at(21).valueAction_((0.5 + ecarttypeEnergy * 	dissymetrieEnergy.sign).clip(0.0, 1.0));
											// Rec 2 on / off
											window.view.children.at(36).valueAction_((0.5 + ecarttypeEnergy * 	dissymetrieEnergy.sign * rrand(-1, 1)).clip(0.0, 1.0));
											// Offset Buffer 2
											window.view.children.at(33).children.do({arg subView, subItem;
												if(subItem == 0, {nil},
													{subView.valueAction_((subView.value + (rrand(0.0, 0.3) * 	dissymetrieEnergy.sign * rrand(-1.0, 1.0)))%1.0)})});
											// Reverse 2 on / off
											window.view.children.at(31).valueAction_((0.5 + ecarttypeEnergy * 	dissymetrieEnergy.sign * rrand(-1.0, 1.0)).clip(0.0, 1.0));
										});
										// Probabilite sur signal Flux
										# q1Flux, medianeFlux, q3Flux, ecartqFlux, ecartsemiqFlux = musicData.at(7).log10.abs.quartiles;
										meanFlux = musicData.at(7).mean;
										dissymetrieFlux=musicData.at(7).dissymetrie;
										// Probabilite sur signal Flatness
										# q1Flatness, medianeFlatness, q3Flatness, ecartqFlatness, ecartsemiqFlatness = musicData.at(5).log10.abs.quartiles;
										meanFlatness = musicData.at(5).mean;
										dissymetrieFlatness=musicData.at(5).dissymetrie;
										if(variableChange == "Flux", {meanProbaPreset = fluxOnFly.log10.abs; q1 = q1Flux; q3 = q3Flux; ecartsemiq = ecartsemiqFlux; seuilInt = 1; seuilFrac = window.view.children.at(61).value}, {meanProbaPreset = flatnessOnFly.log10.abs; q1 = q1Flatness; q3 = q3Flatness; ecartsemiq = ecartsemiqFlatness; seuilInt = 0; seuilFrac = window.view.children.at(61).value});
										// Setup Change Synth or FX
										if(algoChange == "Probability", {
											if(window.view.children.at(60).value == 1, {
												if(meanProbaPreset < (q1 - ecartsemiq) or: {meanProbaPreset > (q3 + ecartsemiq)},
													{windowName = window.name.split($[).at(0);
														synthNumber = nil; compteur = 0;
														choiceSynth.do({arg synth, item; if(windowName == synth.asString, {synthNumber = item; newSynth = item})});
														while({synthNumber == newSynth and: {compteur < 3}},
															{compteur = compteur + 1;
																// Synthese
																if(synthNumber > choiceSynth.indexOf('SYNTHESE (') and: {synthNumber < choiceSynth.indexOf	('SAMPLER 1 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('SYNTHESE (') + 1, choiceSynth.indexOf('SAMPLER 1 BUFFER (') - 1)});
																// Sample One Buffer
																if(synthNumber > choiceSynth.indexOf('SAMPLER 1 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('FFT 1 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('SAMPLER 1 BUFFER (') + 1, choiceSynth.indexOf('FFT 1 BUFFER (') - 1)});
																// FFT One Buffer
																if(synthNumber > choiceSynth.indexOf('FFT 1 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('FFT 2 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('FFT 1 BUFFER (') + 1, choiceSynth.indexOf('FFT 2 BUFFER (') - 1)});
																// FFT Two Buffer
																if(synthNumber > choiceSynth.indexOf('FFT 2 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('PIANO (')}, {newSynth = rrand(choiceSynth.indexOf('FFT 2 BUFFER (') + 1, choiceSynth.indexOf('PIANO (') - 1)});
																// Piano
																if(synthNumber > choiceSynth.indexOf('PIANO (') and: {synthNumber < choiceSynth.indexOf('FFT PIANO + SAMPLE (')}, {newSynth = rrand(choiceSynth.indexOf('PIANO (') + 1, choiceSynth.indexOf('FFT PIANO + SAMPLE (') - 1)});
																// FFT Piano + Sample
																if(synthNumber > choiceSynth.indexOf('FFT PIANO + SAMPLE (') and: {synthNumber < 	choiceSynth.indexOf('FX (')}, {newSynth = rrand(choiceSynth.indexOf('FFT PIANO + SAMPLE (') + 1, choiceSynth.indexOf('FX (') - 1)});
																// FX
																if(synthNumber > choiceSynth.indexOf('FX (') and: {synthNumber < choiceSynth.indexOf	('SPECIAL SYNTH + FX (')}, {newSynth = rrand(choiceSynth.indexOf('FX (') + 1, choiceSynth.indexOf('SPECIAL SYNTH + FX (') - 1)});
																// FX SPECIAL
																if(synthNumber > choiceSynth.indexOf('SPECIAL SYNTH + FX (') and: {synthNumber < 	choiceSynth.indexOf('END (')}, {newSynth = rrand(choiceSynth.indexOf('SPECIAL SYNTH + FX (') + 1, choiceSynth.indexOf('END (') - 1)});
														});
														//listeDataOSC=[];// Prevent more change
														window.view.children.at(11).valueAction_(newSynth);
												});
											});
										},
										{
											if(window.view.children.at(60).value == 1, {
												if(abs(meanProbaPreset.floor - lastMeanProbaPreset.floor) >= seuilInt and: {(meanProbaPreset - lastMeanProbaPreset).abs >= seuilFrac},
													{windowName = window.name.split($[).at(0);
														synthNumber = nil; compteur = 0;
														choiceSynth.do({arg synth, item; if(windowName == synth.asString, {synthNumber = item; newSynth = item})});
														while({synthNumber == newSynth and: {compteur < 3}},
															{compteur = compteur + 1;
																// Synthese
																if(synthNumber > choiceSynth.indexOf('SYNTHESE (') and: {synthNumber < choiceSynth.indexOf	('SAMPLER 1 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('SYNTHESE (') + 1, choiceSynth.indexOf('SAMPLER 1 BUFFER (') - 1)});
																// Sample One Buffer
																if(synthNumber > choiceSynth.indexOf('SAMPLER 1 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('FFT 1 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('SAMPLER 1 BUFFER (') + 1, choiceSynth.indexOf('FFT 1 BUFFER (') - 1)});
																// FFT One Buffer
																if(synthNumber > choiceSynth.indexOf('FFT 1 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('FFT 2 BUFFER (')}, {newSynth = rrand(choiceSynth.indexOf('FFT 1 BUFFER (') + 1, choiceSynth.indexOf('FFT 2 BUFFER (') - 1)});
																// FFT Two Buffer
																if(synthNumber > choiceSynth.indexOf('FFT 2 BUFFER (') and: {synthNumber < 	choiceSynth.indexOf('PIANO (')}, {newSynth = rrand(choiceSynth.indexOf('FFT 2 BUFFER (') + 1, choiceSynth.indexOf('PIANO (') - 1)});
																// Piano
																if(synthNumber > choiceSynth.indexOf('PIANO (') and: {synthNumber < choiceSynth.indexOf('FFT PIANO + SAMPLE (')}, {newSynth = rrand(choiceSynth.indexOf('PIANO (') + 1, choiceSynth.indexOf('FFT PIANO + SAMPLE (') - 1)});
																// FFT Piano + Sample
																if(synthNumber > choiceSynth.indexOf('FFT PIANO + SAMPLE (') and: {synthNumber < 	choiceSynth.indexOf('FX (')}, {newSynth = rrand(choiceSynth.indexOf('FFT PIANO + SAMPLE (') + 1, choiceSynth.indexOf('FX (') - 1)});
																// FX
																if(synthNumber > choiceSynth.indexOf('FX (') and: {synthNumber < choiceSynth.indexOf	('SPECIAL SYNTH + FX (')}, {newSynth = rrand(choiceSynth.indexOf('FX (') + 1, choiceSynth.indexOf('SPECIAL SYNTH + FX (') - 1)});
																// FX SPECIAL
																if(synthNumber > choiceSynth.indexOf('SPECIAL SYNTH + FX (') and: {synthNumber < 	choiceSynth.indexOf('END (')}, {newSynth = rrand(choiceSynth.indexOf('SPECIAL SYNTH + FX (') + 1, choiceSynth.indexOf('END (') - 1)});
														});
														//listeDataOSC=[];// Prevent more change
														window.view.children.at(11).valueAction_(newSynth);
												});
											});
										});
										lastMeanProbaPreset = meanProbaPreset;
									});
								},
								{
									nil;
								}
								);
								newTempo = rrand(window.view.children.at(55).children.at(1).value, window.view.children.at(55).children.at(3).value);
							});
						}.defer;
						(newTempo.reciprocal * systemBPM.tempo).wait;
					});
				});
			};

			// New Tdef Automation
			tdefControls = fonctionTdefControls.value(windowSynth, groupe, rrand(tempoAutomationSynthControls.lo, tempoAutomationSynthControls.hi));
			tdefMusicData = fonctionTdefMusicData.value(windowSynth, groupe, rrand(tempoAutomationSynthMusicData.lo, tempoAutomationSynthMusicData.hi));

			// Set Sliders enabled or disabled
			fonctionEnabledSlider = {arg flagAuto=0;
				// Reset SLiders
				switchBufferOne.enabled_(true); switchBufferOneAction.enabled_(true); loopBufferOne.enabled_(true); durSampleOneSlider.enabled_(true); textBufferOne.enabled_(true);switchBufferTwo.enabled_(true); loopBufferTwo.enabled_(true); switchBufferTwoAction.enabled_(true); loopBufferTwo.enabled_(true); sendLocalBuf.enabled_(true); durSampleTwoSlider.enabled_(true); textBufferTwo.enabled_(true); controlsAntiClick.enabled_(true); freqSlider.enabled_(true); freqTranSlider.enabled_(true); dureeSlider.enabled_(true); dureeTranSlider.enabled_(true); quantaSlider.enabled_(true);envelopeSynth.enabled_(true);
				reverseBufferOneAction.enabled_(true); knobOffset1.enabled_(true); knobPreLevel1.enabled_(true); knobPostLevel1.enabled_(true); knobRecOn1.enabled_(true); reverseBufferTwoAction.enabled_(true);
				knobOffset2.enabled_(true); knobPreLevel2.enabled_(true); knobPostLevel2.enabled_(true); knobRecOn2.enabled_(true);
				if(flagAuto == 1, {
					automationSliderFreq.enabled_(true); automationSliderDur.enabled_(true); automationSliderSynth.enabled_(true); automationNumberSynth.enabled_(true);automationSliderBuffer.enabled_(true)}, {
					automationSliderFreq.enabled_(false); automationSliderDur.enabled_(false); automationSliderSynth.enabled_(false); automationNumberSynth.enabled_(false);automationSliderBuffer.enabled_(false)});
				// Synthese
				if(synthNumber > choiceSynth.indexOf('SYNTHESE (') and: {synthNumber < choiceSynth.indexOf('SAMPLER 1 BUFFER (')}, {switchBufferOne.enabled_(false); switchBufferOneAction.enabled_(false);  loopBufferOne.enabled_(false); durSampleOneSlider.enabled_(false); textBufferOne.enabled_(false); switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); controlsAntiClick.enabled_(false); sendLocalBuf.enabled_(false);  automationSliderBuffer.enabled_(false);
					reverseBufferOneAction.enabled_(false);
					knobOffset1.enabled_(false);
					knobPreLevel1.enabled_(false);
					knobPostLevel1.enabled_(false);
					knobRecOn1.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// Sample One Buffer
				if(synthNumber > choiceSynth.indexOf('SAMPLER 1 BUFFER (') and: {synthNumber < choiceSynth.indexOf('FFT 1 BUFFER (')}, {switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); sendLocalBuf.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// FFT One Buffer
				if(synthNumber > choiceSynth.indexOf('FFT 1 BUFFER (') and: {synthNumber < choiceSynth.indexOf('FFT 2 BUFFER (')}, {switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); sendLocalBuf.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// FFT Two Buffer
				if(synthNumber > choiceSynth.indexOf('FFT 2 BUFFER (') and: {synthNumber < choiceSynth.indexOf('PIANO (')}, {sendLocalBuf.enabled_(false);
				});
				// Piano
				if(synthNumber > choiceSynth.indexOf('PIANO (') and: {synthNumber < choiceSynth.indexOf('FFT PIANO + SAMPLE (')}, {switchBufferOne.enabled_(false); switchBufferOneAction.enabled_(false);  loopBufferOne.enabled_(false); durSampleOneSlider.enabled_(false); textBufferOne.enabled_(false);switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); controlsAntiClick.enabled_(false); sendLocalBuf.enabled_(false); automationSliderBuffer.enabled_(false);
					reverseBufferOneAction.enabled_(false);
					knobOffset1.enabled_(false);
					knobPreLevel1.enabled_(false);
					knobPostLevel1.enabled_(false);
					knobRecOn1.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// FFT Piano + Sample
				if(synthNumber > choiceSynth.indexOf('FFT PIANO + SAMPLE (') and: {synthNumber < choiceSynth.indexOf('FX (')}, {switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); sendLocalBuf.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// FX
				if(synthNumber > choiceSynth.indexOf('FX (') and: {synthNumber < choiceSynth.indexOf('SPECIAL SYNTH + FX (')}, {switchBufferOne.enabled_(false); switchBufferOneAction.enabled_(false);  loopBufferOne.enabled_(false); textBufferOne.enabled_(false);switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); freqSlider.enabled_(false); freqTranSlider.enabled_(false);
					//dureeSlider.enabled_(false); dureeTranSlider.enabled_(false);
					quantaSlider.enabled_(false); envelopeSynth.enabled_(false);
					//startAutomationSynthMusicData.enabled_(false);
					//tempoAutomationSynthMusicData.enabled_(false);
					//jitterAutomationMusicData.enabled_(false);
					automationSliderFreq.enabled_(false); automationSliderDur.enabled_(false);
					//automationSliderSynth.enabled_(false);
					//automationNumberSynth.enabled_(false);
					automationSliderBuffer.enabled_(false);
					reverseBufferOneAction.enabled_(false);
					knobOffset1.enabled_(false);
					knobPreLevel1.enabled_(false);
					knobPostLevel1.enabled_(false);
					knobRecOn1.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
				// FX SPECIAL
				if(synthNumber > choiceSynth.indexOf('SPECIAL SYNTH + FX ('), {switchBufferOne.enabled_(false); switchBufferOneAction.enabled_(false);  loopBufferOne.enabled_(false); textBufferOne.enabled_(false);switchBufferTwo.enabled_(false); loopBufferTwo.enabled_(false); switchBufferTwoAction.enabled_(false); loopBufferTwo.enabled_(false); durSampleTwoSlider.enabled_(false); textBufferTwo.enabled_(false); automationSliderBuffer.enabled_(false);
					reverseBufferOneAction.enabled_(false);
					knobOffset1.enabled_(false);
					knobPreLevel1.enabled_(false);
					knobPostLevel1.enabled_(false);
					knobRecOn1.enabled_(false);
					reverseBufferTwoAction.enabled_(false);
					knobOffset2.enabled_(false);
					knobPreLevel2.enabled_(false);
					knobPostLevel2.enabled_(false);
					knobRecOn2.enabled_(false);
				});
			};
			fonctionEnabledSlider.value;

			// Set Sliders enabled or disabled
			fonctionEnabledControls = {
				controlsNode.valueAction_([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
				knobOffset1.valueAction_(0);
				knobPreLevel1.valueAction_(1);
				knobPostLevel1.valueAction_(0);
				knobRecOn1.valueAction_(1);
				//reverseBufferOneAction.valueAction_(0);
				//reverseBufferTwoAction.valueAction_(0);
				knobOffset2.valueAction_(0);
				knobPreLevel2.valueAction_(1);
				knobPostLevel2.valueAction_(0);
				knobRecOn2.valueAction_(1);
			};
			fonctionEnabledControls.value;

			// Setup Variables Synth
			//onOff=startStop.value;
			out=sourceOut.value; if(out == 16, {flagAmp=0}, {flagAmp=1});
			busIn=sourceBusIn.value; busFXin=sourceFXin.value;
			busOut=sourceBusOut.value; busFXout=sourceFXout.value;
			switch1=switchBufferOneAction.value; switch2=switchBufferTwoAction.value; loop1=loopBufferOne.value; loop2=loopBufferTwo.value;
			levelOut=sendBusOut.value.dbamp; levelFX=sendBusFX.value.dbamp; levelLocal=sendLocalBuf.value.dbamp;
			panLo=panSlider.lo; panHi=panSlider.hi;
			fhzLo=freqSlider.lo; fhzHi=freqSlider.hi; fhzT=freqTranSlider.value;
			dbLo=ampSlider.lo.dbamp; dbHi=ampSlider.hi.dbamp;
			durLo=dureeSlider.lo; durHi=dureeSlider.hi; durM=dureeTranSlider.value; if(durM < 0, {durM = durM.abs.reciprocal});
			quanta=quantaSlider.value;
			ctrlHP=controlsAntiClick.value; ctrlSynth=controlsNode.value;
			ctrlBuffer=[1, 0, 1, 0, 1, 1, 0, 1, 0, 1];
			envLevel=envelopeSynth.value.at(1); envDuree==[0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125]; envTime=envelopeSynth.value.at(0); envDuree.size.do({arg i; envDuree.put(i, abs(envTime.wrapAt(i+1) - envTime.wrapAt(i)))});

			// Calcul New Music
			calculNewMusic = {arg data;
				indexMusicData = 0;
				listeDataAlgo = data;
				// Normalize NewList
				newFreq = listeDataAlgo.at(0).cpsmidi / 127;
				newAmp = listeDataAlgo.at(1);
				newDuree = listeDataAlgo.at(2) / timeMaximum;
				switch(stringAlgorithm,
					"Default", {}, // No Action
					"Probability", {
						// Duree Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ = newDuree.quartiles;
						ecartType = newDuree.ecartType;
						cv = ecartType / newDuree.mean;
						dissymetrie = newDuree.dissymetrie;
						// Transpose
						if(coin(cv.frac), {newDuree = newDuree + (ecartType * dissymetrie.sign);
						});
						// Compress or Expand
						if(coin(cv.frac), {newDuree = newDuree * cv},
							{
								newDuree = newDuree + (ecartSemiQ * dissymetrie.sign);
						});
						newDuree = newDuree.clip(0, 1);
						// Amp Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ = newAmp.quartiles;
						ecartType = newAmp.ecartType;
						cv = ecartType / newAmp.mean;
						dissymetrie = newAmp.dissymetrie;
						// Transpose
						if(coin(cv.frac), {newAmp = newAmp + (ecartType * dissymetrie.sign);
						});
						// Compress or Expand
						if(coin(cv.frac), {newAmp = newAmp * cv},
							{
								newAmp = newAmp + (ecartSemiQ * dissymetrie.sign);
						});
						newAmp = newAmp.clip(0, 1);
						// Freq Transformation
						# q1, mediane, q3, ecartQ, ecartSemiQ = newFreq.quartiles;
						ecartType = newFreq.ecartType;
						cv = ecartType / newFreq.mean;
						dissymetrie = newFreq.dissymetrie;
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
								newFreq = newFreq + (ecartSemiQ * dissymetrie.sign);
							});
						});
						newFreq = newFreq.clip(0, 1);
					},
					"Euclide", {
						// Distances Euclidiennes 3D -> Vecteur 1D
						distances = sqrt(newFreq.pow(2) + newAmp.pow(2) + newDuree.pow(2));
						// Probabilite sur Distances
						# q1, mediane, q3, ecartQ, ecartSemiQ = distances.quartiles;
						ecartType = distances.ecartType;
						cv = ecartType / distances.mean;
						dissymetrie = distances.dissymetrie;
						// Freq
						newFreq = distances;
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
								newFreq = newFreq + (ecartSemiQ * dissymetrie.sign);
							});
						});
						newFreq = newFreq.clip(0, 1);
						// Amp
						newAmp = newAmp * distances;
						// Transpose
						if(coin(cv.frac), {newAmp = newAmp + (ecartType * dissymetrie.sign);
						});
						// Compress expand
						if(coin(cv.frac), {
							newAmp = newAmp * cv;
						},
						{
							newAmp = newAmp + (ecartSemiQ * dissymetrie.sign)
						});
						newAmp = newAmp.clip(0, 1);
						// Duree
						newDuree = newDuree / distances;
						// Transpose
						if(coin(cv.frac), {newDuree = newDuree + (ecartType * dissymetrie.sign);
						});
						// Compress expand
						if(coin(cv.frac), {
							newDuree = newDuree * cv;
						},
						{
							newDuree = newDuree + (ecartSemiQ * dissymetrie.sign)
						});
						newDuree = newDuree.clip(0, 1);
					},
					"Genetic", {
						freqGen = [];
						ampGen = [];
						durGen = [];
						// Calculation algo new musical pattern
						newFreq.size.do({arg i, f, a, d;
							# f = geneticF.next([newFreq.at(i)], 'rec', 0.5, flatness, flux);
							freqGen = freqGen.add(f);
							# a = geneticA.next([newAmp.at(i)], 'rec', 0.5, flatness, flux);
							ampGen = ampGen.add(a);
							# d = geneticD.next([newDuree.at(i)], 'rec', 0.5, flatness, flux);
							durGen = durGen.add(d);
						});
						newFreq = freqGen;
						newAmp = ampGen;
						newDuree = durGen;
					},
					"Kohonen", {
						// Training Kohonen Freq
						maxTraining = newFreq.size * 10;
						if(maxTraining > 640, {maxTraining = 640});
						maxTraining.do({arg i; kohonenF.training(newFreq.wrapAt(i).asArray, i+1, maxTraining, 1)});
						// Calculate Kohonen Freq
						newFreq = newFreq.collect({arg item, index, z;
							item = kohonenF.training(item.asArray, 1, 1, 0);
							item = item.at(0).at(1);// Vecteur
							if(item < 0, {item = 0});
							if(item > 127, {item = 127});
							item = item / 127;
						});
						// Training Kohonen Amp
						maxTraining.do({arg i; kohonenA.training(newAmp.wrapAt(i).asArray, i+1, maxTraining, 1)});
						// Calculate Kohonen Amp
						newAmp = newAmp.collect({arg item, index;
							item = kohonenA.training(item.asArray, 1, 1, 0);
							item = item.at(0).at(1);// Vecteur
							if(item < 0, {item = 0});
							if(item > 127, {item = 127});
							item = item / 127;
						});
						// Training Kohonen Duree
						maxTraining.do({arg i; kohonenD.training(newDuree.wrapAt(i).asArray, i+1, maxTraining, 1)});
						// Calculate Kohonen Duree
						newDuree = newDuree.collect({arg item, index;
							item = kohonenD.training(item.asArray, 1, 1, 0);
							item = item.at(0).at(1);// Vecteur
							if(item < 0, {item = 0});
							if(item > 127, {item = 127});
							item = item / 127;
						});
					},
					"Neural", {
						freqNeu = [];
						ampNeu = [];
						durNeu = [];
						// Training Neural
						maxTraining = newFreq.size * 24;
						if(maxTraining > 640, {maxTraining = 640});
						maxTraining.do({arg i; neuralFAD.next([newFreq.wrapAt(i).asArray, newAmp.wrapAt(i).asArray, newDuree.wrapAt(i).asArray])});
						// Calculate Neural
						newFreq.size.do({arg i, f, a, d;
							# f, a, d = neuralFAD.next([newFreq.wrapAt(i).asArray, newAmp.wrapAt(i).asArray, newDuree.wrapAt(i).asArray]);
							// Freq
							freqNeu = freqNeu.add(f.at(0));
							// Amp
							ampNeu = ampNeu.add(a.at(0));
							// Duree
							durNeu = durNeu.add(d.at(0));
						});
						newFreq = freqNeu;
						newAmp = ampNeu;
						newDuree = durNeu;
					}
				);
				newFreq = (newFreq * 127).midicps;
				//newAmp
				newDuree = newDuree * timeMaximum;
				[newFreq, newAmp, newDuree].value;
			};

			// New Tdef or FX
			fonctionSynthTdefFX = {
				if(synthNumber > choiceSynth.indexOf('FX ('), {
					// FX Synth (special treatment)
					//// Set Music Data
					freq = (freq.cpsmidi / 127 * (fhzHi - fhzLo) + fhzLo + fhzT).midicps;
					// Setup Freq with Scaling and Tuning
					if(flagScaling != 'off', {
						octave = freq.cpsoct.round(0.001);
						ratio = octave.frac;
						octave = octave.floor;
						degre = (ratio * tuning.size + 0.5).floor;
						(scale.degrees.size - 1).do({arg i;
							difL=abs(degre - scale.degrees.at(i));
							difH=abs(degre - scale.degrees.at(i+1));
							if(degre >= scale.degrees.at(i) and: {degre <= scale.degrees.at(i+1)},
								{if(difL <= difH, {pos = i},{pos = i+1})});
						});
						freq = scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
					});
					//Reset 1 Trigger Recording
					bufferRecording1.set(\trigger, 1, \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \loop, loop1, \run, ctrlBuffer.at(2));
					bufferRecording2.set(\trigger, 1, \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \loop, loop2, \run, ctrlBuffer.at(7));
					synthAndFX = Synth.new(name,[
						\out, out + startChannelAudioOut, \busIn, listeBusInOut.at(busIn), \busOut, listeBusInOut.at(busOut), \busFXout, listeBusFX.at(busFXout), \busFXin, listeBusFX.at(busFXin), \bufferOne, bufferOne.bufnum, \bufferTwo, bufferTwo.bufnum, \loopOne, loop1, \loopTwo, loop2, \switchBuffer1, switch1, \switchBuffer2, switch2, \recBuffer1, recBuffer1, \recBuffer2, recBuffer2,
						\offset1, ctrlBuffer.at(3), \offset2, ctrlBuffer.at(8), \reverse1, ctrlBuffer.at(4), \reverse2, ctrlBuffer.at(9),
						\freq, freq, \amp, amp, \duree, duree, \tempo, tempo.reciprocal, \freqCentroid, freqCentroid, \flatness, flatness, \energy, energy, \flux, flux,
						\levelBusOut, levelOut, \levelBusFX, levelFX, \levelLocalIn, levelLocal,
						\panLo, panLo, \panHi, panHi, \freqLo, fhzLo, \freqHi, fhzHi, \freqT, fhzT, \ampLo, dbLo, \ampHi, dbHi, \durLo, durLo, \durHi, durHi, \durM, durM, \quanta, quanta, \flagAmpOnOff, flagAmp,
						\ctrlHP1, ctrlHP.at(0), \ctrlHP2, ctrlHP.at(1),
						\ctrl1, ctrlSynth.at(0), \ctrl2, ctrlSynth.at(1), \ctrl3, ctrlSynth.at(2), \ctrl4, ctrlSynth.at(3), \ctrl5, ctrlSynth.at(4), \ctrl6, ctrlSynth.at(5), \ctrl7, ctrlSynth.at(6), \ctrl8, ctrlSynth.at(7), \ctrl9, ctrlSynth.at(8), \ctrl10, ctrlSynth.at(9), \ctrl11, ctrlSynth.at(10), \ctrl12, ctrlSynth.at(11),
						\envLevel1, envLevel.at(0), \envLevel2, envLevel.at(1), \envLevel3, envLevel.at(2), \envLevel4, envLevel.at(3), \envLevel5, envLevel.at(4), \envLevel6, envLevel.at(5), \envLevel7, envLevel.at(6), \envLevel8, envLevel.at(7),
						\envTime1, envDuree.at(0), \envTime2, envDuree.at(1), \envTime3, envDuree.at(2), \envTime4, envDuree.at(3), \envTime5, envDuree.at(4), \envTime6, envDuree.at(5), \envTime7, envDuree.at(6)], groupe, \addToTail).map(\freq, busOSC.at(indexNumFhzBand));
				}, {
					// New Tdef Synth or Normal Synth
					synthAndFX = nil;
					if(flagModeSynth == 'Tdef',
						// ModeTdef
						{
							tdefSynthesizer = Tdef((name ++ groupe.nodeID.asString).asSymbol, {
								loop({
									// Setup Data Music
									if(listeDataOSC.at(indexNumFhzBand).size != 0 and: {onOff == 1}, {
										if(indexMusicData >= (listeDataAlgo.at(0).size - 1) or: {indexMusicData >= (listeDataOSC.at(indexNumFhzBand).size - 1)} ,
											{
												// Calcul new music
												// New Band
												indexNumFhzBand = rangeNumFhzBand.choose;
												if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
												if(listeDataOSC.at(indexNumFhzBand).size != 0, {
													flagBand = 'on';
													#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
												},
												{
													flagBand = 'off';
												});
										});
										if(flagBand == 'on', {
											#freq, amp, duree, tempo, freqCentroid, flatness, energy, flux = listeDataOSC.at(indexNumFhzBand).at(indexMusicData);
											listeFreq = listeFreq.add(newFreq.at(indexMusicData));
											listeAmp = listeAmp.add(newAmp.at(indexMusicData));
											duree = newDuree.at(indexMusicData);
											indexMusicData = indexMusicData + 1;
											while({duree < chordDuree and: {newFreq.size > indexMusicData} and: {listeFreq.size < chordSize}},
												{
													listeFreq = listeFreq.add(newFreq.at(indexMusicData));
													listeAmp = listeAmp.add(newAmp.at(indexMusicData));
													duree = newDuree.at(indexMusicData);
													indexMusicData = indexMusicData + 1;
											});
											// Set Music Data
											duree = duree / timeMaximum * (durHi - durLo) + durLo;
											duree = duree.floor + ((duree.frac * quanta + 0.5).floor / quanta);
											duree = if(duree <= 0, quanta.reciprocal, duree);
											dureeTdef = (duree * durM).max(0.01);
											duree = (dureeTdef * tempo.reciprocal).max(0.01);
											//Reset 1 Trigger Recording
											bufferRecording1.set(\trigger, 1, \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \loop, ctrlBuffer.at(2), \run, 1);
											bufferRecording2.set(\trigger, 1, \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \loop, ctrlBuffer.at(7), \run, 1);
											listeFreq = listeFreq.soloArray;
											listeAmp = listeAmp.soloArray;
											// MIDI OFF
											if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {lastFreqMidi.do({arg freq; midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
												if(flagVST == 'on', {~fxVST.midi.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});
											})});
											lastFreqMidi=[];
											// Playing
											listeFreq.size.do({arg index;
												freq = listeFreq.at(index);
												freq = (freq.cpsmidi / 127 * (fhzHi - fhzLo) + fhzLo + fhzT).midicps;
												// Setup Freq with Scaling and Tuning
												if(flagScaling != 'off', {
													octave = freq.cpsoct.round(0.001);
													ratio = octave.frac;
													octave = octave.floor;
													degre = (ratio * tuning.size + 0.5).floor;
													(scale.degrees.size - 1).do({arg i;
														difL=abs(degre - scale.degrees.at(i));
														difH=abs(degre - scale.degrees.at(i+1));
														if(degre >= scale.degrees.at(i) and: {degre <= scale.degrees.at(i+1)},
															{if(difL <= difH, {pos = i},{pos = i+1})});
													});
													freq = scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
												});
												// SETUP MIDI OFF
												lastFreqMidi = lastFreqMidi.add(freq);
												amp = listeAmp.at(index);
												// MIDI OUT
												if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {
													midiFreq = (freq.cpsmidi + 0.5).floor; midiAmp = listeAmp.at(index) * (dbHi - dbLo) + dbLo * 127;
													midiOut.noteOn(canalMIDIinstr, midiFreq, midiAmp);
													if(flagVST == 'on', {~fxVST.midi.noteOn(canalMIDIinstr, midiFreq, midiAmp)});
												});
												// Synth
												Synth.new(name,[
													\out, out + startChannelAudioOut, \busIn, listeBusInOut.at(busIn), \busOut, listeBusInOut.at(busOut), \busFXout, listeBusFX.at(busFXout), \busFXin, listeBusFX.at(busFXin), \bufferOne, bufferOne.bufnum, \bufferTwo, bufferTwo.bufnum, \loopOne, loop1, \loopTwo, loop2, \switchBuffer1, switch1, \switchBuffer2, switch2, \recBuffer1, recBuffer1, \recBuffer2, recBuffer2,
													\offset1, ctrlBuffer.at(3), \offset2, ctrlBuffer.at(8), \reverse1, ctrlBuffer.at(4), \reverse2, ctrlBuffer.at(9),
													// \freq, busOSC, // controls in realtime
													\freq, freq, \amp, amp, \duree, duree, \tempo, tempo.reciprocal, \freqCentroid, freqCentroid, \flatness, flatness, \energy, energy, \flux, flux,
													\levelBusOut, levelOut, \levelBusFX, levelFX, \levelLocalIn, levelLocal,
													\panLo, panLo, \panHi, panHi, \freqLo, fhzLo, \freqHi, fhzHi, \freqT, fhzT, \ampLo, dbLo, \ampHi, dbHi, \durLo, durLo, \durHi, durHi, \durM, durM, \quanta, quanta, \flagAmpOnOff, flagAmp,
													\ctrlHP1, ctrlHP.at(0), \ctrlHP2, ctrlHP.at(1),
													\ctrl1, ctrlSynth.at(0), \ctrl2, ctrlSynth.at(1), \ctrl3, ctrlSynth.at(2), \ctrl4, ctrlSynth.at(3), \ctrl5, ctrlSynth.at(4), \ctrl6, ctrlSynth.at(5), \ctrl7, ctrlSynth.at(6), \ctrl8, ctrlSynth.at(7), \ctrl9, ctrlSynth.at(8), \ctrl10, ctrlSynth.at(9), \ctrl11, ctrlSynth.at(10), \ctrl12, ctrlSynth.at(11),
													\envLevel1, envLevel.at(0), \envLevel2, envLevel.at(1), \envLevel3, envLevel.at(2), \envLevel4, envLevel.at(3), \envLevel5, envLevel.at(4), \envLevel6, envLevel.at(5), \envLevel7, envLevel.at(6), \envLevel8, envLevel.at(7),
													\envTime1, envDuree.at(0), \envTime2, envDuree.at(1), \envTime3, envDuree.at(2), \envTime4, envDuree.at(3), \envTime5, envDuree.at(4), \envTime6, envDuree.at(5), \envTime7, envDuree.at(6)], groupe, \addToTail);
											});
											// Reset variables
											listeFreq = [];
											listeAmp = [];
											//Reset 0 Trigger Recording
											bufferRecording1.set(\trigger, 0);
											bufferRecording2.set(\trigger, 0);
										},
										{
											flagBand = 'on';
										});
									},
									{
										// MIDI OFF
										if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
											if(flagVST == 'on', {~fxVST.midi.noteOn(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});

										});
										lastFreqMidi=[]; dureeTdef = quanta.reciprocal;
										indexMusicData = 9999;
										newFreq = [];
										newAmp = [];
										newDuree = [];
										listeFreq = [];
										listeAmp = [];
										listeDataAlgo = [[], [], []];
										flagBand = 'on';
										// New Band
										indexNumFhzBand = rangeNumFhzBand.choose;
										if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
										if(listeDataOSC.at(indexNumFhzBand).size != 0, {
											flagBand = 'on';
											#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
										},
										{
											flagBand = 'off';
										});
									});
									dureeTdef.wait;
								});
							});
						},
						// Mode OnFly1
						{
							if(flagModeSynth == 'OnFly1', {
								tdefSynthesizer = OSCFunc.newMatching({arg msg, time, addr, recvPort;
									var music;
									if(onOff == 1, {
										music = msg[1].asString.interpret;
										#freq, amp, duree, tempo, freqCentroid, flatness, energy, flux = music;
										// Calcul new music
										if(listeDataOSC.at(indexNumFhzBand).size != 0 and: {onOff == 1}, {
											if(indexMusicData >= (listeDataAlgo.at(0).size - 1) or: {indexMusicData >= (listeDataOSC.at(indexNumFhzBand).size - 1)} ,
												{// Calcul new music
													// New Band
													indexNumFhzBand = rangeNumFhzBand.choose;
													if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
													if(listeDataOSC.at(indexNumFhzBand).size != 0, {
														flagBand = 'on';
														#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
													},
													{
														flagBand = 'off';
													});
											});
											if(flagBand == 'on', {
												#freq, amp, duree, tempo, freqCentroid, flatness, energy, flux = listeDataOSC.at(indexNumFhzBand).at(indexMusicData);
												listeFreq = listeFreq.add(newFreq.at(indexMusicData));
												listeAmp = listeAmp.add(newAmp.at(indexMusicData));
												duree = newDuree.at(indexMusicData);
												indexMusicData = indexMusicData + 1;
												while({duree < chordDuree and: {newFreq.size > indexMusicData} and: {listeFreq.size < chordSize}},
													{
														listeFreq = listeFreq.add(newFreq.at(indexMusicData));
														listeAmp = listeAmp.add(newAmp.at(indexMusicData));
														duree = newDuree.at(indexMusicData);
														indexMusicData = indexMusicData + 1;
												});
												// Set Music Data
												duree = duree / timeMaximum * (durHi - durLo) + durLo;
												duree = duree.floor + ((duree.frac * quanta + 0.5).floor / quanta);
												duree = if(duree <= 0, quanta.reciprocal, duree);
												dureeTdef = (duree * durM).max(0.01);
												duree = (dureeTdef * tempo.reciprocal).max(0.01);
												//Reset 1 Trigger Recording
												bufferRecording1.set(\trigger, 1, \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \loop, ctrlBuffer.at(2), \run, 1);
												bufferRecording2.set(\trigger, 1, \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \loop, ctrlBuffer.at(7), \run, 1);
												listeFreq = listeFreq.soloArray;
												listeAmp = listeAmp.soloArray;
												// MIDI OFF
												if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {lastFreqMidi.do({arg freq; midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
													if(flagVST == 'on', {~fxVST.midi.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});
												})});
												lastFreqMidi=[];
												// Playing
												listeFreq.size.do({arg index;
													freq = listeFreq.at(index);
													freq = (freq.cpsmidi / 127 * (fhzHi - fhzLo) + fhzLo + fhzT).midicps;
													// Setup Freq with Scaling and Tuning
													if(flagScaling != 'off', {
														octave = freq.cpsoct.round(0.001);
														ratio = octave.frac;
														octave = octave.floor;
														degre = (ratio * tuning.size + 0.5).floor;
														(scale.degrees.size - 1).do({arg i;
															difL=abs(degre - scale.degrees.at(i));
															difH=abs(degre - scale.degrees.at(i+1));
															if(degre >= scale.degrees.at(i) and: {degre <= scale.degrees.at(i+1)},
																{if(difL <= difH, {pos = i},{pos = i+1})});
														});
														freq = scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
													});
													// SETUP MIDI OFF
													lastFreqMidi = lastFreqMidi.add(freq);
													//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
													//amp = amp * (dbHi - dbLo) + dbLo;
													amp = listeAmp.at(index);
													// MIDI OUT
													if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {
														midiFreq = (freq.cpsmidi + 0.5).floor; midiAmp = listeAmp.at(index) * (dbHi - dbLo) + dbLo * 127;
														midiOut.noteOn(canalMIDIinstr, midiFreq, midiAmp);
														if(flagVST == 'on', {~fxVST.midi.noteOn(canalMIDIinstr, midiFreq, midiAmp)});
													});
													// Synth
													Synth.new(name,[
														\out, out + startChannelAudioOut, \busIn, listeBusInOut.at(busIn), \busOut, listeBusInOut.at(busOut), \busFXout, listeBusFX.at(busFXout), \busFXin, listeBusFX.at(busFXin), \bufferOne, bufferOne.bufnum, \bufferTwo, bufferTwo.bufnum, \loopOne, loop1, \loopTwo, loop2, \switchBuffer1, switch1, \switchBuffer2, switch2, \recBuffer1, recBuffer1, \recBuffer2, recBuffer2,
														\offset1, ctrlBuffer.at(3), \offset2, ctrlBuffer.at(8), \reverse1, ctrlBuffer.at(4), \reverse2, ctrlBuffer.at(9),
														// \freq, busOSC, // controls in realtime
														\freq, freq, \amp, amp, \duree, duree, \tempo, tempo.reciprocal, \freqCentroid, freqCentroid, \flatness, flatness, \energy, energy, \flux, flux,
														\levelBusOut, levelOut, \levelBusFX, levelFX, \levelLocalIn, levelLocal,
														\panLo, panLo, \panHi, panHi, \freqLo, fhzLo, \freqHi, fhzHi, \freqT, fhzT, \ampLo, dbLo, \ampHi, dbHi, \durLo, durLo, \durHi, durHi, \durM, durM, \quanta, quanta, \flagAmpOnOff, flagAmp,
														\ctrlHP1, ctrlHP.at(0), \ctrlHP2, ctrlHP.at(1),
														\ctrl1, ctrlSynth.at(0), \ctrl2, ctrlSynth.at(1), \ctrl3, ctrlSynth.at(2), \ctrl4, ctrlSynth.at(3), \ctrl5, ctrlSynth.at(4), \ctrl6, ctrlSynth.at(5), \ctrl7, ctrlSynth.at(6), \ctrl8, ctrlSynth.at(7), \ctrl9, ctrlSynth.at(8), \ctrl10, ctrlSynth.at(9), \ctrl11, ctrlSynth.at(10), \ctrl12, ctrlSynth.at(11),
														\envLevel1, envLevel.at(0), \envLevel2, envLevel.at(1), \envLevel3, envLevel.at(2), \envLevel4, envLevel.at(3), \envLevel5, envLevel.at(4), \envLevel6, envLevel.at(5), \envLevel7, envLevel.at(6), \envLevel8, envLevel.at(7),
														\envTime1, envDuree.at(0), \envTime2, envDuree.at(1), \envTime3, envDuree.at(2), \envTime4, envDuree.at(3), \envTime5, envDuree.at(4), \envTime6, envDuree.at(5), \envTime7, envDuree.at(6)], groupe, \addToTail);
												});
												// Reset variables
												listeFreq = [];
												listeAmp = [];
												//Reset 0 Trigger Recording
												bufferRecording1.set(\trigger, 0);
												bufferRecording2.set(\trigger, 0);
											},
											{
												flagBand = 'on';
											});
										},
										{
											// MIDI OFF
											if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
												if(flagVST == 'on', {~fxVST.midi.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});
											});
											lastFreqMidi=[]; dureeTdef = quanta.reciprocal;
											indexMusicData = 9999;
											newFreq = [];
											newAmp = [];
											newDuree = [];
											listeFreq = [];
											listeAmp = [];
											listeDataAlgo = [[], [], []];
											flagBand = 'on';
											// New Band
											indexNumFhzBand = rangeNumFhzBand.choose;
											if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
											if(listeDataOSC.at(indexNumFhzBand).size != 0, {
												flagBand = 'on';
												#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
											},
											{
												flagBand = 'off';
											});
										});
									});
								}, '/NewMusic', musicAppAddr);
							},
							// Mode OnFly2
							{
								tdefSynthesizer = OSCFunc.newMatching({arg msg, time, addr, recvPort;
									var music;
									if(onOff == 1, {
										music = msg[1].asString.interpret;
										#freq, amp, duree, tempo, freqCentroid, flatness, energy, flux = music;
										// Set FHZ Band
										for(1, numFhzBand, {arg i;
											if(freq > bandFHZ.at(i-1) and: {freq < bandFHZ.at(i)}, {
												if(i == indexNumFhzBand or: {indexNumFhzBand == 0}, {
													// Calcul new music
													if(listeDataOSC.at(indexNumFhzBand).size != 0 and: {onOff == 1}, {
														if(indexMusicData >= (listeDataAlgo.at(0).size - 1) or: {indexMusicData >= (listeDataOSC.at(indexNumFhzBand).size - 1)} ,
															{// Calcul new music
																// New Band
																indexNumFhzBand = rangeNumFhzBand.choose;
																if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
																if(listeDataOSC.at(indexNumFhzBand).size != 0, {
																	flagBand = 'on';
																	#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
																},
																{
																	flagBand = 'off';
																});
														});
														if(flagBand == 'on', {
															#freq, amp, duree, tempo, freqCentroid, flatness, energy, flux = listeDataOSC.at(indexNumFhzBand).at(indexMusicData);
															listeFreq = listeFreq.add(newFreq.at(indexMusicData));
															listeAmp = listeAmp.add(newAmp.at(indexMusicData));
															duree = newDuree.at(indexMusicData);
															indexMusicData = indexMusicData + 1;
															while({duree < chordDuree and: {newFreq.size > indexMusicData} and: {listeFreq.size < chordSize}},
																{
																	listeFreq = listeFreq.add(newFreq.at(indexMusicData));
																	listeAmp = listeAmp.add(newAmp.at(indexMusicData));
																	duree = newDuree.at(indexMusicData);
																	indexMusicData = indexMusicData + 1;
															});
															// Set Music Data
															duree = duree / timeMaximum * (durHi - durLo) + durLo;
															duree = duree.floor + ((duree.frac * quanta + 0.5).floor / quanta);
															duree = if(duree <= 0, quanta.reciprocal, duree);
															dureeTdef = (duree * durM).max(0.01);
															duree = (dureeTdef * tempo.reciprocal).max(0.01);
															//Reset 1 Trigger Recording
															bufferRecording1.set(\trigger, 1, \preLevel, ctrlBuffer.at(0), \postLevel, ctrlBuffer.at(1), \loop, ctrlBuffer.at(2), \run, 1);
															bufferRecording2.set(\trigger, 1, \preLevel, ctrlBuffer.at(5), \postLevel, ctrlBuffer.at(6), \loop, ctrlBuffer.at(7), \run, 1);
															listeFreq = listeFreq.soloArray;
															listeAmp = listeAmp.soloArray;
															// MIDI OFF
															if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {lastFreqMidi.do({arg freq; midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
																if(flagVST == 'on', {~fxVST.midi.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});
															})});
															lastFreqMidi=[];
															// Playing
															listeFreq.size.do({arg index;
																freq = listeFreq.at(index);
																freq = (freq.cpsmidi / 127 * (fhzHi - fhzLo) + fhzLo + fhzT).midicps;
																// Setup Freq with Scaling and Tuning
																if(flagScaling != 'off', {
																	octave = freq.cpsoct.round(0.001);
																	ratio = octave.frac;
																	octave = octave.floor;
																	degre = (ratio * tuning.size + 0.5).floor;
																	(scale.degrees.size - 1).do({arg i;
																		difL=abs(degre - scale.degrees.at(i));
																		difH=abs(degre - scale.degrees.at(i+1));
																		if(degre >= scale.degrees.at(i) and: {degre <= scale.degrees.at(i+1)},
																			{if(difL <= difH, {pos = i},{pos = i+1})});
																	});
																	freq = scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
																});
																// SETUP MIDI OFF
																lastFreqMidi = lastFreqMidi.add(freq);
																//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
																//amp = amp * (dbHi - dbLo) + dbLo;
																amp = listeAmp.at(index);
																// MIDI OUT
																if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {
																	midiFreq = (freq.cpsmidi + 0.5).floor; midiAmp = listeAmp.at(index) * (dbHi - dbLo) + dbLo * 127;
																	midiOut.noteOn(canalMIDIinstr, midiFreq, midiAmp);
																	if(flagVST == 'on', {												~fxVST.midi.noteOn(canalMIDIinstr, midiFreq, midiAmp)});
																});
																// Synth
																Synth.new(name,[
																	\out, out + startChannelAudioOut, \busIn, listeBusInOut.at(busIn), \busOut, listeBusInOut.at(busOut), \busFXout, listeBusFX.at(busFXout), \busFXin, listeBusFX.at(busFXin), \bufferOne, bufferOne.bufnum, \bufferTwo, bufferTwo.bufnum, \loopOne, loop1, \loopTwo, loop2, \switchBuffer1, switch1, \switchBuffer2, switch2, \recBuffer1, recBuffer1, \recBuffer2, recBuffer2,
																	\offset1, ctrlBuffer.at(3), \offset2, ctrlBuffer.at(8), \reverse1, ctrlBuffer.at(4), \reverse2, ctrlBuffer.at(9),
																	// \freq, busOSC, // controls in realtime
																	\freq, freq, \amp, amp, \duree, duree, \tempo, tempo.reciprocal, \freqCentroid, freqCentroid, \flatness, flatness, \energy, energy, \flux, flux,
																	\levelBusOut, levelOut, \levelBusFX, levelFX, \levelLocalIn, levelLocal,
																	\panLo, panLo, \panHi, panHi, \freqLo, fhzLo, \freqHi, fhzHi, \freqT, fhzT, \ampLo, dbLo, \ampHi, dbHi, \durLo, durLo, \durHi, durHi, \durM, durM, \quanta, quanta, \flagAmpOnOff, flagAmp,
																	\ctrlHP1, ctrlHP.at(0), \ctrlHP2, ctrlHP.at(1),
																	\ctrl1, ctrlSynth.at(0), \ctrl2, ctrlSynth.at(1), \ctrl3, ctrlSynth.at(2), \ctrl4, ctrlSynth.at(3), \ctrl5, ctrlSynth.at(4), \ctrl6, ctrlSynth.at(5), \ctrl7, ctrlSynth.at(6), \ctrl8, ctrlSynth.at(7), \ctrl9, ctrlSynth.at(8), \ctrl10, ctrlSynth.at(9), \ctrl11, ctrlSynth.at(10), \ctrl12, ctrlSynth.at(11),
																	\envLevel1, envLevel.at(0), \envLevel2, envLevel.at(1), \envLevel3, envLevel.at(2), \envLevel4, envLevel.at(3), \envLevel5, envLevel.at(4), \envLevel6, envLevel.at(5), \envLevel7, envLevel.at(6), \envLevel8, envLevel.at(7),
																	\envTime1, envDuree.at(0), \envTime2, envDuree.at(1), \envTime3, envDuree.at(2), \envTime4, envDuree.at(3), \envTime5, envDuree.at(4), \envTime6, envDuree.at(5), \envTime7, envDuree.at(6)], groupe, \addToTail);
															});
															// Reset variables
															listeFreq = [];
															listeAmp = [];
															//Reset 0 Trigger Recording
															bufferRecording1.set(\trigger, 0);
															bufferRecording2.set(\trigger, 0);
														},
														{
															flagBand = 'on';
														});
													},
													{
														// MIDI OFF
														if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {midiOut.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0);
															if(flagVST == 'on', {~fxVST.midi.noteOff(canalMIDIinstr, (freq.cpsmidi + 0.5).floor, 0)});

														});
														lastFreqMidi=[]; dureeTdef = quanta.reciprocal;
														indexMusicData = 9999;
														newFreq = [];
														newAmp = [];
														newDuree = [];
														listeFreq = [];
														listeAmp = [];
														listeDataAlgo = [[], [], []];
														flagBand = 'on';
														// New Band
														indexNumFhzBand = rangeNumFhzBand.choose;
														if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
														if(listeDataOSC.at(indexNumFhzBand).size != 0, {
															flagBand = 'on';
															#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
														},
														{
															flagBand = 'off';
														});
													});
												},
												{// New Band
													indexNumFhzBand = rangeNumFhzBand.choose;
													if(indexNumFhzBand == nil, {indexNumFhzBand = 0});
													if(listeDataOSC.at(indexNumFhzBand).size != 0, {
														flagBand = 'on';
														#newFreq, newAmp, newDuree = calculNewMusic.value(listeDataOSC.at(indexNumFhzBand).flop);
													},
													{
														flagBand = 'off';
													});
												});
											});
										});
									});
								}, '/NewMusic', musicAppAddr);

							});
					});
				});
			};
			fonctionSynthTdefFX.value;
			if(synthAndFX != nil, {synthAndFX.run(true)});

			windowSynth.onClose_({
				s.bind{
					// MIDI OFF
					if(flagMidiOut == 'on' and: {canalMIDIinstr >= 0}, {midiOut.allNotesOff(canalMIDIinstr);
						if(flagVST =='on', {~fxVST.midi.allNotesOff(canalMIDIinstr)});
					});
					s.sync;
					tdefSynthesizer.clear;
					s.sync;
					tdefSynthesizer.remove;
					s.sync;
					//tdefSynthesizer.free;
					//s.sync;
					tdefControls.clear;
					s.sync;
					tdefControls.remove;
					s.sync;
					tdefMusicData.clear;
					s.sync;
					tdefMusicData.remove;
					s.sync;
					bufferOne.free;
					s.sync;
					bufferTwo.free;
					s.sync;
					recBuffer1.free;
					s.sync;
					recBuffer2.free;
					s.sync;
					listeWindowSynth.remove(windowSynth);
					s.sync;
					listeGroupeSynth.remove(groupe);
					s.sync;
					listeGroupeSynthID.remove(groupe.nodeID);
					s.sync;
					listeWindows.remove(windowSynth);
					s.sync;
					bufferRecording1.free;
					s.sync;
					bufferRecording2.free;
					s.sync;
					synthRec.free;
					s.sync;
					groupe.free;
					s.sync;
					//Document.listener.string="";
					//s.sync;
					s.queryAllNodes;"".postln;
					s.sync;
				};
			});

			listeWindows = listeWindows.add(windowSynth);
			fonctionShortCut.value(windowSynth);

			// Sortie Fonction Window
			windowSynth;
		};

		windowControl.front;
		windowControl.view.children.at(0).focus;

	}

	// SynthDef

	initSynthDef  {

		// Matrix Audio Analyze Onsets
		SynthDef("OSC Matrix Onsets",
			{arg busAnalyze, seuil=0.5, filtre=0.5, lock=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0,
				inputFilter;
				input = In.ar(busAnalyze);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);
				# freqIn, hasfreqIn = Tartini.kr(inputFilter, filtre, 2048, 1024, 512, 0.5);
				ampIn = A2K.kr(Amplitude.ar(input));
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				SendReply.kr(detect, '/Matrix_Musical_Data', values: [freqIn, ampIn, timeIn, tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5, 6, 7, 8]);
		}).add;

		// Matrix Audio Analyze Pitch
		SynthDef("OSC Matrix Pitch",
			{arg busAnalyze, seuil=0.5, filtre=0, lock=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0,
				inputFilter;
				input = In.ar(busAnalyze);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
				# freqIn, hasfreqIn = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Matrix_Musical_Data', values: [freqIn, ampIn, timeIn, tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5, 6, 7, 8]);
		}).add;

		// Matrix Audio Analyze Pitch2
		SynthDef("OSC Matrix Pitch2",
			{arg busAnalyze, seuil=0.5, filtre=0, lock=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, centroid=0, flatness=0.0, fft, fft2, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0, inputFilter, harmonic, percussive;
				input = In.ar(busAnalyze);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				fft = FFT(LocalBuf(512, 1), inputFilter);
				harmonic = FFT(LocalBuf(512, 1), inputFilter);
				percussive = FFT(LocalBuf(512, 1), inputFilter);
				#harmonic, percussive = MedianSeparation(fft, harmonic, percussive, 512, 5, 1, 2, 1);
				detect= Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
				# freqIn, hasfreqIn = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				fft2 = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft2);
				flatness =  SpecFlatness.kr(fft2);
				energy =  SpecPcile.kr(fft2);
				flux =  FFTFlux.kr(fft2);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Matrix_Musical_Data', values: [freqIn, ampIn, timeIn, tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5, 6, 7, 8]);
		}).add;

		// Matrix Audio Analyze KeyTrack
		SynthDef("OSC Matrix KeyTrack",
			{arg busAnalyze, seuil=0.5, filtre=1, lock=0;
				var input, detect, freqIn, ampIn, centroid=0, flatness=0.0, fft, energy=0, timeIn=0, trackB, trackH, trackQ, tempo=60, flux=0, key;
				input = In.ar(busAnalyze);
				detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
				key = KeyTrack.kr(FFT(Buffer.alloc(s, 4096, 1), input), (filtre * 2).clip(0, 2));
				if(key < 12, freqIn = (key + 60).midicps, freqIn = (key - 12 + 60).midicps);
				ampIn = A2K.kr(Amplitude.ar(input));
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(detect);
				SendReply.kr(detect, '/Matrix_Musical_Data', values: [freqIn, ampIn, timeIn, tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5, 6, 7, 8]);
		}).add;

		// Matrix Keyboard
		SynthDef("OSC Matrix Keyboard",
			{arg busAnalyze, seuil=0.5, filtre=0, lock=0, note=0, amp=0, trigger=0;
				var input, detect, freqIn, hasfreqIn, ampIn, centroid, flatness, fft, energy, timeIn=0, trackB, trackH, trackQ, tempo, flux;
				input = In.ar(busAnalyze);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(trigger);
				SendReply.kr(trigger, '/Matrix_Keyboard_Data', values: [note, amp, timeIn, tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5, 6, 7, 8]);
		}).add;

		// Matrix MIDI
		SynthDef("OSC Matrix MIDI",
			{arg busAnalyze, seuil=0.5, filtre=0, lock=0, trigger=0;
				var input, centroid, flatness, fft, energy, trackB, trackH, trackQ, tempo, flux;
				input = In.ar(busAnalyze);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, tempo = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				SendReply.kr(trigger, '/Matrix_MIDI_Data', values: [tempo, centroid, flatness.max(-100), energy, flux.max(-100)], replyID: [1, 2, 3, 4, 5]);
		}).add;

		// Synth pour analyse AudioIn send audio -> busIn
		SynthDef("Matrix AudioIn",
			{arg in=0, busIn;
				var input;
				input=Mix(SoundIn.ar(in));
				Out.ar(busIn, input); // Bus In
		}).add;

		// Synth pour analyse AudioIn send audio -> busAnalyze
		SynthDef("Matrix AnalyzeIn",
			{arg in=0, busAnalyze;
				var input;
				input=Mix(SoundIn.ar(in));
				Out.ar(busAnalyze, input); // Bus Analyze In
		}).add;

		// Synth lecture file pour analyse AudioIn
		SynthDef("Matrix Play File",
			{arg out=0, bufferplay, busIn, busAnalyze, trigger=0, offset=0, loop=1, volume=0;
				var input;
				input=PlayBuf.ar(2, bufferplay, BufRateScale.kr(bufferplay), trigger, BufFrames.kr(bufferplay)*offset , loop);
				Out.ar(busIn, Mix(input)); // Bus In
				Out.ar(busAnalyze, Mix(input)); // Bus Analyze In
				Out.ar(out, input * volume); // Amp File Out
		}).add;

		// Synth MasterFX
		SynthDef("MasterFX",
			{arg out=0, limit=0.8, postAmp=1.0;
				ReplaceOut.ar(out, Limiter.ar(LeakDC.ar(In.ar(0, numberAudioOut) * postAmp), limit));// Limiter on variable numberAudioOut = (2 a x)
		}).add;

		// Buffer Synth Recording
		SynthDef("RecBuffer",
			{arg out, busIn, buffer, offset=0, preLevel=1, postLevel=0, run=1, loop=1, trigger=0;
				var in;
				in = In.ar(busIn);
				RecordBuf.ar(in, buffer, offset, preLevel, postLevel, run, loop, trigger);
		}).add;

		// Synth VST
		SynthDef("VST Plugin",
			{arg out=0, xFade=0.5, panLo=0, panHi=0, gainIn=0.5, freq, amp, duree, bpm=1, freqCentroid, flatness, energy, flux;
				var signal, chain, ambisonic;
				bpm = if(bpm > 1, bpm.reciprocal, bpm);
				signal = Mix(In.ar(0, numberAudioOut)) * gainIn;
				chain = Mix(VSTPlugin.ar(signal, numberAudioOut));
				//chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1)),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm).lag(bpm.reciprocal + 1)), 1, widthMC, orientationMC);,
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1)),
							// Ambisonic
							(
								ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
				))));
				// Out
				XOut.ar(out, xFade, chain);
		}).add;

		/////////////////////////// SYNTH  PLAYBUF////////////////////////

		SynthDef('GrainBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3 <= 0, ctrl3, Logistic.kr(ctrl3 * 4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = GrainBuf.ar(1, Dust.kr(ctrl1 * 100), ctrl1 * 0.1, buffer, BufRateScale.kr(buffer) * rate * reverse1, ctrl2, 4, 0, -1, 512);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('LoopBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3 <= 0, ctrl3, Logistic.kr(ctrl3 * 4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = LoopBuf.ar(1, buffer, rate* reverse1, 1, BufFrames.kr(buffer) * offset1, BufFrames.kr(buffer) * ctrl1, BufFrames.kr(buffer) * ctrl2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		// SynthDef with 1 Sample or Buffer
		SynthDef('HPbufRd',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, buffer, rate, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Main Synth
				chain = HPbufRd.ar(1, buffer, Phasor.ar(1, BufRateScale.kr(buffer) * rate * reverse1, BufFrames.kr(buffer) * offset1, BufFrames.kr(buffer)), BufRateScale.kr(buffer) * reverse1, loopOne, ctrlHP1, ctrlHP2);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPplayBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, localBuf;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPplayBufMedianLeakDC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, localBuf;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = LeakDC.ar(Median.ar(ctrl2 * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2)), ctrl3);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPplayBufBusFM',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var in, chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Set Direct AudioIn (levelLocalIn)
				in = In.ar(busIn);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1 + SinOsc.kr(in * 128 * ctrl2, mul: 128 * ctrl3), Impulse.kr(ctrl1 * 100), BufFrames.kr(bufferOne) * offset1, loopOne, ctrlHP1, ctrlHP2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPplayBufVibrato',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//ctrl1 = if(ctrl1.value <= 0 , ctrl1.value, Logistic.kr(ctrl1*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, SinOsc.kr(ctrl2 * dureeSample.reciprocal * 10, mul: ctrl3, add: BufRateScale.kr(buffer) * rate * reverse1), Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPtGrains',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl4 = if(ctrl4.value <= 0 , ctrl4.value, Logistic.kr(ctrl4*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = Mix(HPtGrains.ar(2, Impulse.kr(ctrl1*100), buffer, BufRateScale.kr(buffer) * rate * reverse1, BufDur.kr(buffer) * ctrl4, (duree*ctrl2)/(ctrl3*100), 0, 1, ctrlHP1, ctrlHP2));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Trig1PlayBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, localBuf;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Trig1.kr(Impulse.kr(ctrl1 * 100), ctrl2), BufFrames.kr(buffer) * ctrl3, loopOne, ctrlHP1, ctrlHP2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('HPplayBufResonz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl4 = if(ctrl4.value <= 0 , ctrl4.value, Logistic.kr(ctrl4*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = Resonz.ar(chain, XLine.ar(108.midicps*ctrl2+1.midicps, 108.midicps*ctrl3 + 1.midicps, dureeSample*ctrl4));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Synthesizer',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				rate = rate.clip(0.1, 1.0);
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl7 = if(ctrl7.value <= 0 , ctrl7.value, Logistic.kr(ctrl7*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				freq = freq.clip(20, 20000);
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = if(freq < 64.5.midicps , RLPF.ar(chain, XLine.ar(108.midicps*ctrl2+1.midicps, 108.midicps*ctrl3 + 1.midicps, dureeSample*ctrl4), 0.333), RHPF.ar(chain, XLine.ar(108.midicps*ctrl5+1.midicps, 108.midicps*ctrl6 + 1.midicps, dureeSample*ctrl7), 0.333));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Squiz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0.0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = Squiz.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2), ctrl2 * 10, ctrl3 * 10);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('WaveLoss',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0.01 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = WaveLoss.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2), ctrl2 * 40, 40, abs(ctrl3 * 2 - 1));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('FreqShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = FreqShift.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2), ctrl2 * 1024 - 512, ctrl3 * 2pi);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Warp1',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, pointer;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl4 = if(ctrl4.value <= 0.0 , ctrl4.value, Logistic.kr(ctrl4*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				//pointer = if(reverse1 > 0, Line.kr(ctrl4, ctrl5, dureeSample), Line.kr(ctrl5, ctrl4, dureeSample));
				//chain = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer), 0, BufFrames.kr(buffer) * offset1, 1, ctrlHP1, ctrlHP2);
				//RecordBuf.ar(chain, buffer);
				chain = Warp1.ar(1, buffer, offset1, BufRateScale.kr(buffer) * rate * reverse1, ctrl1 + 0.01, -1, ctrl2 * 15 + 1, ctrl3, 2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Granulation',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, local, in1;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set offset
				offset1 = if(ctrl2.value <= 0.0 , offset1, Logistic.kr(ctrl2 + 3, ctrl3 * 100, Rand(0, 1)));
				// Set Buffer
				local = LocalBuf(4096, 1).clear;
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				in1 = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = HPbufRd.ar(1, local, Phasor.ar(0, ctrl4 + 1, 0, BufFrames.kr(local)), 1, ctrlHP1, ctrlHP2);
				BufWr.ar(in1 + chain * 0.5, local, Phasor.ar(0, ctrl5 + 0.001, 0, BufFrames.kr(local)), 1);
				// chain = Limiter.ar(chain + in1, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Toupie',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, local, in1, in2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set offset
				offset1 = if(ctrl2.value <= 0 , offset1, Logistic.kr(ctrl2 + 3 , ctrl3 * 100, Rand(0, 1)));
				// Set Buffer
				local = LocalBuf(4096, 1).clear;
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				in1=HPplayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer)* offset1,loopOne, ctrlHP1, ctrlHP2) * envelope;
				in2 = HPbufRd.ar(1, local, Phasor.ar(0, ctrl4+1, 0, BufFrames.kr(local)), 1, ctrlHP1, ctrlHP2);
				chain = in1 + in2 * 0.5;
				BufWr.ar(chain, local, Phasor.ar(0, ctrl5+0.01, 0, BufFrames.kr(local)), 1);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Elastic',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set offset
				offset1 = if(ctrl2.value <= 0 , offset1, Logistic.kr(ctrl2 + 3, ctrl3 * 10, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer)*offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = CombC.ar(chain, 0.1, Line.kr(ctrl4.clip(0.01, 0.99)/100, ctrl5.clip(0.01, 0.99)/100, ctrl6.clip(0.01, 1.0)*dureeSample), 1, 0.5);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('RandomElastic',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set offset
				offset1 = if(ctrl2.value <= 0 , offset1, Logistic.kr(ctrl2 + 3, ctrl3 * 100, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer)*offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = CombC.ar(chain, 0.1, Line.kr(Rand(ctrl2.clip(0.01, 0.99), ctrl3.clip(0.01, 0.99))/100, Rand(ctrl4.clip(0.01, 0.99), ctrl5.clip(0.01, 0.99))/100, ctrl6.clip(0.01, 1.0)*dureeSample), 1, 0.5);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('RandKlankSample',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer)*offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = DynKlank.ar(`[[Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], chain, ctrl2, ctrl3, ctrl4);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('DjScratch',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl1 = if(ctrl1.value <= 0 , ctrl1.value, Logistic.kr(ctrl1*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain = HPbufRd.ar(1, buffer, Phasor.ar(Dust.kr(dureeSample.reciprocal), BufRateScale.kr(buffer) * rate * reverse1, BufFrames.kr(buffer)* offset1, BufFrames.kr(buffer)* ctrl1 ).lag(ctrl2) * LFNoise2.kr(ctrl3).sign, 1, ctrlHP1, ctrlHP2);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('LiquidFilter',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				var formantfreqs, formantamps, formantbandwidths; //data for formants
				formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
				formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
				formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl4 = if(ctrl4.value <= 0 , ctrl4.value, Logistic.kr(ctrl4*4, 1, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain =  HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain= Mix(RHPF.ar(chain,  formantfreqs * freq * ctrl2, formantbandwidths / (formantfreqs * freq * ctrl3.clip(0.01, 1.0)) * (ctrl4.clip(0.01, 1.0) / 33), 0.5));
				chain = BBandPass.ar(chain, LFNoise2.kr(dureeSample) + 1 * 1000, ctrl5.clip(0.01, 1.0), 1);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PitchShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, local;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				// Set offset
				ctrl3 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2 + 3, ctrl3 * 100, Rand(0, 1)));
				// Set Buffer
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = Mix(PitchShift.ar(chain, 0.2, [ctrl4, ctrl5, ctrl6, ctrl7, ctrl8, ctrl9, ctrl10].clip(0.01, 1) * 8, ctrl11, ctrl12, 1));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('SamplerSynth',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample, local, recHead, inputSig;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Set Data
				buffer = LocalBuf(s.sampleRate * BufDur.kr(bufferOne), 1);
				inputSig = In.ar(busIn);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				recHead = Phasor.ar(0, BufRateScale.kr(buffer), 0, BufFrames.kr(buffer), 0);
				// Rec Buffer
				BufWr.ar(inputSig, buffer, recHead, 1);
				// Play Buffer
				chain = BufRd.ar(1, buffer,  Phasor.ar(0, BufRateScale.kr(buffer) * rate, offset1, recHead, offset1), 1, loopOne) * envelope;
				chain = Median.ar(3, chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), 1),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), 1, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), 1, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * 1,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), 1);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//////////////////////// FFT with 1 Sample /////////////////////////

		SynthDef('PV_HPshiftDown',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_HPshiftDown(chain, ctrl1 * 32);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Cutoff',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_Cutoff(chain, ctrl1 * 2 - 1);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//SynthDef('PV_HPecartType',
		//{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
		//freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
		//levelBusOut=0, levelBusFX=0, levelLocalIn=0,
		//switchBuffer1=0, switchBuffer2=0,
		//panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
		//ctrlHP1=0.33, ctrlHP2=0.5,
		//ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
		//envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
		//envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
		//var chain, rate, buffer, envelope, ambisonic, dureeSample;
		//// Set FHZ
		//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
		//// Set AMP
		//amp = amp * (ampHi - ampLo) + ampLo;
		//// Set DUREE
		//dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
		//dureeSample = dureeSample + (loopOne * (duree - dureeSample));
		//dureeSample = clip2(duree, dureeSample);
		//// Envelope
		////envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
		//envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
		////// Set offset
		////ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
		//buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
		//// Synth
		//chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
		//chain = FFT(LocalBuf(2048, 1), chain);
		//chain = PV_HPecartType(chain, ctrl1 * 10 + 1);
		//chain= IFFT(chain) * 3;
		//// chain = Limiter.ar(chain, 1.0, 0.01);
		//// Switch Audio Out
		//chain = if(switchAudioOut == 0,
		//if(flagMC == 0,
		//// Pan v1
		//Pan2.ar(chain, Rand(panLo, panHi), envelope),
		//// Pan v2
		//Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
		//if(switchAudioOut == 2,
		//if(flagMC == 0,
		//// PanAz v1
		//PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
		//// PanAz v2
		//PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
		//if(switchAudioOut == 1,
		//// Rotate2 v1
		//Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
		//// Ambisonic v1
		//(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
		//DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
		//// Out
		//OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
		//OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
		//Out.ar(out, chain * flagAmpOnOff);
		//}).add;

		SynthDef('PV_HPfiltre',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(512, 1), chain);
				chain = PV_HPfiltre(chain, 4, ctrl1 * 256 + 4);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagShift(chain, ctrl1 * 4, ctrl2 * 128 -64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_LocalMax',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_LocalMax(chain, ctrl1 * 64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSmear',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSmear(chain, ctrl1 * 64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RandComb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_RandComb(chain, ctrl1, LFNoise2.kr(ctrl2 * 64));
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BinShift(chain, ctrl1 * 4, ctrl2 * 256 -64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinScramble',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl4 = if(ctrl4.value <= 0 , ctrl4.value, Logistic.kr(ctrl4*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BinScramble(chain, ctrl1, ctrl2, LFNoise2.kr(dureeSample.reciprocal));
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BrickWall',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BrickWall(chain, ctrl1 * 2 - 1);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Diffuser',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise2.kr(ctrl1*100), (ctrl2*100).reciprocal));
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagAbove',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagAbove(chain, ctrl1 * 64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagBelow',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagBelow(chain, ctrl1 * 64);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagClip',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagClip(chain, ctrl1 * 16);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagNoise',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagNoise(chain);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSquared',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSquared(chain);
				chain= IFFT(chain) * 0.01;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RectComb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_RectComb(chain, ctrl1 * 32, ctrl2, ctrl3);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSmooth',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSmooth(chain, ctrl1);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_ConformalMap',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_ConformalMap(chain, ctrl1 * 2 - 1, ctrl2 * 2 - 1);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Compander',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_Compander(chain, 80*ctrl1.clip(0.1, 1), (ctrl2*5).clip(2, 5), ctrl3);
				chain= IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_SpectralEnhance',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_SpectralEnhance(chain, (ctrl1*8+0.5).floor, ctrl2*4+1, ctrl3);
				chain= IFFT(chain) * 0.125;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagStretch',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagShift(chain, (ctrl1 * 4).clip(0.25, 4));
				chain= IFFT(chain) * 0.125;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagShift+Stretch',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, dureeSample;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				chain = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 1, BufFrames.kr(buffer) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagShift(chain, (ctrl1 * 4).clip(0.25, 4), ctrl2 - 0.5 * 128);
				chain= IFFT(chain) * 0.125;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		////////////////////// FFT with 2 Sample ///////////////////////////

		SynthDef('PV_Max',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Max(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Min',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Min(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagDiv',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_MagDiv(fft1, fft2, ctrl1 + 0.0001);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Mul',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.01;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Div',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Div(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Add',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RandWipe',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_RandWipe(fft1, fft2, ctrl1, LFNoise2.kr(ctrl2.reciprocal));
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinWipe',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_BinWipe(fft1, fft2, ctrl1 * 2 - 1);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_CopyPhase',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_CopyPhase(fft1, fft2);
				chain = IFFT(chain)* 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RectComb2',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_RectComb2(fft1, fft2, ctrl1 * 32, ctrl2, ctrl3);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_Morph',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				fft1 = FFT(LocalBuf(2048, 1), chain1);
				fft2 = FFT(LocalBuf(2048, 1), chain2);
				chain = PV_Morph(fft1, fft2, ctrl1);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Convolution',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, 0, BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, 0, BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				chain = Convolution.ar(chain1, chain2, 2048) * 0.01;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Concat',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer1, buffer2, envelope, ambisonic, chain1, chain2, dureeSample, fft1, fft2;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, dureeSample, 2);
				//// Set offset
				//ctrl3 = if(ctrl3.value <= 0 , ctrl3.value, Logistic.kr(ctrl3*4, 1, Rand(0, 1)));
				// Select In
				buffer1 = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				buffer2 = if(switchBuffer2.value > 0, bufferTwo, recBuffer2);
				// Synth
				chain1 = HPplayBuf.ar(1, buffer1, BufRateScale.kr(buffer1) * rate * reverse1, Impulse.kr(ctrl1 * 100), BufFrames.kr(buffer1) * offset1, loopOne, ctrlHP1, ctrlHP2);
				chain2 = HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2) * rate * reverse2, Impulse.kr(ctrl2 * 100), BufFrames.kr(buffer2) * offset2, loopTwo, ctrlHP1, ctrlHP2);
				chain = Concat.ar(chain2, chain1, (ctrl3 * 4).clip(1, 4), ctrl4.clip(0.01, 1.0), (ctrl5 * 4).clip(1, 4), ctrl6.clip(0.01,0.5), ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, mul: 0.5);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		///////////////////////////// Piano ///////////////////////////////////

		SynthDef('MdaPiano',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano Synthesizer',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				freq = freq.clip(20,20000);
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				chain = if(freq < 64.5.midicps , RLPF.ar(chain, XLine.ar(63.5.midicps*ctrl1+27.5, freq, duree*ctrl3), 0.333), RHPF.ar(chain, XLine.ar(127.midicps*ctrl2+27.5, freq, duree*ctrl3), 0.333));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano Resonz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				chain = if(freq >= 60.midicps , Resonz.ar(chain, XLine.ar(127.midicps*ctrl1+21.midicps, 55*ctrl2 + 21.midicps, duree*ctrl3)), Resonz.ar(chain, XLine.ar(55*ctrl1+21.midicps, 127.midicps*ctrl2 + 21.midicps, duree*ctrl3)));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano Squiz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Squiz.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5)), ctrl1 * 10, ctrl2 *10);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano Waveloss',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = WaveLoss.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5)), ctrl1 * 40, 40, abs(ctrl2 * 2 - 1));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano FreqShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = FreqShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5)), ctrl1 * 1024 - 512, ctrl2 * 2pi);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PitchShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = PitchShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5)), 0.2, ctrl1.clip(0.01, 1) * 8, ctrl2, ctrl3, 1);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		/////////////////////////// FFT Piano ////////////////////////////////

		SynthDef('Piano PV_HPshiftDown',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_HPshiftDown(chain, ctrl1 * 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//SynthDef('Piano PV_HPecartType',
		//{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
		//freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
		//levelBusOut=0, levelBusFX=0, levelLocalIn=0,
		//switchBuffer1=0, switchBuffer2=0,
		//panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
		//ctrlHP1=0.33, ctrlHP2=0.5,
		//ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
		//envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
		//envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
		//var chain, envelope, ambisonic;
		//// Set Music Data
		////freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
		////rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
		//amp = amp * (ampHi - ampLo) + ampLo;
		//// Envelope
		////envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
		//envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
		//// Synth
		//chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
		//RecordBuf.ar(chain, bufferOne);
		//chain = FFT(LocalBuf(2048, 1), chain);
		//chain = PV_HPecartType(chain, ctrl1 * 10);
		//chain = IFFT(chain) * 3;
		//// chain = Limiter.ar(chain, 1.0, 0.01);
		//// Switch Audio Out
		//chain = if(switchAudioOut == 0,
		//if(flagMC == 0,
		//// Pan v1
		//Pan2.ar(chain, Rand(panLo, panHi), envelope),
		//// Pan v2
		//Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
		//if(switchAudioOut == 2,
		//if(flagMC == 0,
		//// PanAz v1
		//PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
		//// PanAz v2
		//PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
		//if(switchAudioOut == 1,
		//// Rotate2 v1
		//Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
		//// Ambisonic v1
		//(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
		//DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
		//// Out
		//OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
		//OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
		//Out.ar(out, chain * flagAmpOnOff);
		//}).add;

		SynthDef('Piano PV_MagShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagShift(chain, ctrl1 * 4, ctrl2 * 128 - 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_LocalMax',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_LocalMax(chain, ctrl1 * 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagSmear',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSmear(chain, ctrl1 * 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_RandComb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_RandComb(chain, ctrl1, LFNoise2.kr(ctrl2 * 64));
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_BinShift',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BinShift(chain, ctrl1 * 4, ctrl2 * 256 - 128);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_BinScramble',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BinScramble(chain, ctrl1, ctrl2,  LFNoise2.kr(ctrl3.reciprocal));
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_BrickWall',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_BrickWall(chain, ctrl1 * 2 - 1);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_ConformalMap',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_ConformalMap(chain, ctrl1 * 2 - 1, ctrl2 * 2 - 1);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Diffuser',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise2.kr(ctrl1*100), (ctrl2*100).reciprocal));
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagAbove',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagAbove(chain, ctrl1 * 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagBelow',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagBelow(chain, ctrl1 * 64);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagClip',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagClip(chain, ctrl1 * 16);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagNoise',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagNoise(chain);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagSquared',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSquared(chain);
				chain = IFFT(chain) * 0.1;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_RectComb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_RectComb(chain, ctrl1 * 32, ctrl2, ctrl3);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagSmooth',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_MagSmooth(chain, ctrl1);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Compander',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, duree, 2);
				// Synth
				chain = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(chain, bufferOne);
				chain = FFT(LocalBuf(2048, 1), chain);
				chain = PV_Compander(chain, 80*ctrl1.clip(0.1, 1), (ctrl2*5).clip(2, 5), ctrl3);
				chain = IFFT(chain);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//////////////////////FFT Piano + Sample ////////////////

		SynthDef('Piano PV_Max',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_Max(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Min',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer, local;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_Min(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_MagDiv',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_MagDiv(fft1, fft2, ctrl1 + 0.0001);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Mul',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_Mul(fft1, fft2);
				chain = IFFT(chain) * 0.1;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Add',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_Add(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_RandWipe',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_RandWipe(fft1, fft2, ctrl1, LFNoise2.kr(ctrl2.reciprocal));
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_BinWipe',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_BinWipe(fft1, fft2, ctrl1 * 2 - 1);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_CopyPhase',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_CopyPhase(fft1, fft2);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_RectComb2',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_RectComb2(fft1, fft2, ctrl1 * 32, ctrl2, ctrl3);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano PV_Morph',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, fft1, fft2, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				fft1 = FFT(LocalBuf(2048, 1), in1);
				fft2 = FFT(LocalBuf(2048, 1), in2);
				chain = PV_Morph(fft1, fft2, ctrl1);
				chain = IFFT(chain) * 0.5;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Piano Convolution',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2, offset1, offset2, reverse1, reverse2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic, rate, dureeSample, in1, in2, buffer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				dureeSample = if(switchBuffer1.value > 0, BufDur.kr(bufferOne) / BufRateScale.kr(bufferOne) * rate, BufDur.kr(recBuffer1) / BufRateScale.kr(recBuffer1) * rate);
				dureeSample = dureeSample + (loopOne * (duree - dureeSample));
				dureeSample = clip2(duree, dureeSample);
				// Envelope
				//envTime1 = if(envTime1 > dureeSample, 1.0, envTime1 * dureeSample.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, 1, 0, dureeSample, 2);
				buffer = if(switchBuffer1.value > 0, bufferOne, recBuffer1);
				// Synth
				in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp - 0.5));
				RecordBuf.ar(in1, LocalBuf(s.sampleRate * BufDur.kr(buffer), 1));
				in2 = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate * reverse1, 0, offset1 * BufFrames.kr(buffer), loopOne) * amp;
				chain = Convolution(in1, in2, 2048) * 0.1;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//////////////////////// SynthDef Synthese ///////////////////////////

		SynthDef('SinOsc',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = SinOsc.ar(freq);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('SynthFM',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(SinOsc.ar(freq + SinOsc.ar(250*ctrl1, mul: 250 * ctrl2), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(500*ctrl3, mul: 500 * ctrl4), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(750*ctrl5, mul: 750 * ctrl6), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(1000*ctrl7, mul: 1000 * ctrl8), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(1250*ctrl9, mul: 1250 * ctrl10), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(1500*ctrl11, mul: 1500 * ctrl12), mul: amp / 6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('SynthBusFM',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var in, chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Set Direct AudioIn (levelLocalIn)
				in = In.ar(busIn);
				// Synth
				chain = Mix(SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl1, mul: 1000 * ctrl2), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl3, mul: 1000 * ctrl4), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl5, mul: 1000 * ctrl6), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl7, mul: 1000 * ctrl8), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl9, mul: 1000 * ctrl10), mul: amp / 6) + SinOsc.ar(freq + SinOsc.ar(in * 1000 * ctrl11, mul: 1000 * ctrl2), mul: amp / 6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('SinOscVibrato',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(SinOsc.ar(SinOsc.kr(ctrl1*8, mul: Line.kr(0, ctrl2*100, ctrl3*duree), add: freq), mul: amp / 4) + SinOsc.ar(SinOsc.kr(ctrl4*16, mul: Line.kr(0, ctrl5*100, ctrl6*duree), add: freq), mul: amp / 4) + SinOsc.ar(SinOsc.kr(ctrl7*24, mul: Line.kr(0, ctrl8*100, ctrl9*duree), add: freq), mul: amp / 4) + SinOsc.ar(SinOsc.kr(ctrl10*32, mul: Line.kr(0, ctrl11*100, ctrl12*duree), add: freq), mul: amp / 4));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Formant',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(Formant.ar(freq, LFNoise0.kr(ctrl1.reciprocal) * (ctrl2 * 127).midicps, LFNoise0.kr(duree.reciprocal)*(ctrl3 * 127).midicps, amp / 4) + Formant.ar(freq, LFNoise0.kr(ctrl4.reciprocal) * (ctrl5 * 127).midicps, LFNoise0.kr(duree.reciprocal)*(ctrl6 * 127).midicps, amp / 4) + Formant.ar(freq, LFNoise0.kr(ctrl7.reciprocal) * (ctrl8 * 127).midicps, LFNoise0.kr(duree.reciprocal)*(ctrl9 * 127).midicps, amp / 4) + Formant.ar(freq, LFNoise0.kr(ctrl10.reciprocal) * (ctrl11 * 127).midicps, LFNoise0.kr(duree.reciprocal)*(ctrl12 * 127).midicps, amp / 4));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Guitare',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl1 * 24)), ctrl2)) + BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl3 * 24)), ctrl4)) + BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl5 * 24)), ctrl6)) + BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl7 * 24)), ctrl8)) + BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl9 * 24)), ctrl10)) + BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*ctrl11 * 24)), ctrl12)));
				chain = CombL.ar(chain, freq.reciprocal, freq.reciprocal, duree, amp);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('SawSynth',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, rate, buffer, envelope, ambisonic, reverse, dureeSample, pointer;
				//// Set FHZ
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				freq = freq.clip(20,20000);
				chain = Saw.ar(freq, 0.5);
				chain = RHPF.ar(chain, Line.kr(ctrl1*4000, ctrl2*4000, duree*ctrl4), ctrl3, 1, RLPF.ar(chain, Line.kr(ctrl5*2000, ctrl7*2000, duree*ctrl8), ctrl6));
				//chain = BBandPass.ar(chain, ctrl1 * 4186 + 27.5, ctrl2 + 0.001);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Gendy3',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(Gendy3.ar(ctrl1 * 6, ctrl2 * 6, ctrl3 * 0.1, ctrl4 * 0.1, freq, ctrl5 * 0.1, ctrl6 * 0.1));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Klang',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(Klang.ar(`[[ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6, ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12] * 4186 + 32.703195662575, [amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12, amp / 12], nil], freq));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Blip',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(Blip.ar(freq, Line.kr(50 * ctrl1 + 1,50 * ctrl2 + 1, duree * ctrl3), 0.5));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('VarSaw',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(VarSaw.ar(freq, ctrl1, ctrl2, 0.5));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Pulse',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(Pulse.ar(freq, ctrl1, 0.5));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Klank',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(DynKlank.ar(`[[ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6, ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12] * 4186 + 32.703195662575, amp / 12, nil], Dust2.ar(duree.reciprocal * 100), freq));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Klank2',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				chain = Mix(DynKlank.ar(`[[ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6, ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12] * 4186 + 32.703195662575, amp / 12, nil], Impulse.ar(duree.reciprocal * 64), freq));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Spring',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, envelope, inforce, k, d, outforce, ambisonic;
				// Set Music Data
				//freq = (freq.cpsmidi / 127 * (freqHi - freqLo) + freqLo + freqT).midicps;
				//rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				amp = amp * (ampHi - ampLo) + ampLo;
				// Envelope
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), 1, amp, 0, duree, 2);
				// Synth
				inforce = LFPulse.ar(ctrl1 * duree);
				k = ctrl2 * 20;
				d = ctrl3 * 0.001;
				outforce = Spring.ar(inforce, k, d);
				outforce = outforce * freq + freq;
				//main = SinOsc.ar(freq, 0, 0.5);
				chain = PMOsc.ar(freq, outforce, Line.kr(ctrl4, ctrl5 * 2pi), 0, 0.25);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					if(flagMC == 0,
						// Pan v1
						Pan2.ar(chain, Rand(panLo, panHi), envelope),
						// Pan v2
						Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
					if(switchAudioOut == 2,
						if(flagMC == 0,
							// PanAz v1
							PanAz.ar(numberAudioOut, chain, Rand(panLo, panHi), envelope, widthMC, orientationMC),
							// PanAz v2
							PanAz.ar(numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
						if(switchAudioOut == 1,
							// Rotate2 v1
							Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
							// Ambisonic v1
							(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;


		//////////////////////////// FX //////////////////////////////////////

		SynthDef('AllpassC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(AllpassC.ar(in, 0.2, [ctrl1,ctrl2/2,ctrl3/3,ctrl4/4,ctrl1,ctrl5/5,ctrl6/6], [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12]*30, amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('FreeVerb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FreeVerb.ar(in, ctrl1, ctrl2, ctrl3, amp);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('GVerb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, left, right, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				# left, right = GVerb.ar(in, (ctrl1*300).clip(1, 300), (ctrl5*100).clip(15, 100), ctrl6.clip(0.01, 1), ctrl7.clip(0.01, 1), 15, ctrl2.clip(0.01, 1), ctrl3.clip(0.01, 1), ctrl4.clip(0.01, 1), 300, amp);
				chain = Mix(left, right);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('JPverb',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, left, right, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(JPverb.ar(in, t60: ctrl1 * 60, damp: ctrl2, size: ctrl3 * 300, earlyDiff: ctrl4, modDepth: ctrl5, modFreq: ctrl6 * 10, low: ctrl7, mid: ctrl8, high: ctrl9, lowcut: ctrl10, highcut: ctrl11));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('DelayC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(DelayC.ar(in, 4.0, [ctrl1*4.0,ctrl2*4.0,ctrl3*4.0,ctrl4*4.0,ctrl5*4.0,ctrl6*4.0,ctrl7*4.0,ctrl8*4.0,ctrl9*4.0,ctrl10*4.0,ctrl11*4.0,ctrl12*4.0].clip(0.01, 4.0), amp / 6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Trig1',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = in * EnvGen.kr(Env.perc, Trig1.kr(Impulse.kr(ctrl1 * 100), ctrl2), 1, 0, ctrl3);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('BPF',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(BPF.ar(in,[ctrl1*1000+27.5,ctrl2*1000+750,ctrl3*1000+1500,ctrl4*1000+2250,ctrl5*1000+3000,ctrl6*1000+3750], [ctrl7,ctrl8,ctrl9,ctrl10,ctrl11,ctrl12]+0.001, amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('BRF',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(BRF.ar(in,[ctrl1*1000+27.5,ctrl2*1000+750,ctrl3*1000+1500,ctrl4*1000+2250,ctrl5*1000+3000,ctrl6*1000+3750], [ctrl7,ctrl8,ctrl9,ctrl10,ctrl11,ctrl12]+0.001, amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('MidEQ',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(MidEQ.ar(in, [ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6]*4186+27.5, 0.5, [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12]*48-24, amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('TwoPole',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(TwoPole.ar(in, [ctrl1*500,ctrl2*500+500,ctrl3*500+1000,ctrl4*500+1500,ctrl5*500+2000,ctrl6*500+2500], [ctrl7,ctrl8,ctrl9,ctrl10,ctrl11,ctrl12], amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Median',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(Median.ar(ctrl1 * 30 + 1, in, amp));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('LeakDC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(LeakDC.ar(in, ctrl1, amp));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Median+LeakDC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(LeakDC.ar(Median.ar(ctrl1 * 30 + 1, in, amp), ctrl2));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('CombC',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(CombC.ar(in, 0.2, [ctrl1,ctrl2/2,ctrl3/3,ctrl4/4,ctrl1,ctrl5/5,ctrl6/6].clip(0.01, 1) / 100, [ctrl7*4, ctrl8*4, ctrl9*4, ctrl10*4, ctrl11*4, ctrl12*4].clip(0.0001, 4), amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('RHPF',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				/*chain = Mix(RHPF.ar(in, [ctrl1*4186+320.24370022528, ctrl2*4186+320.24370022528, ctrl3*4186+320.24370022528, ctrl4*4186+320.24370022528, ctrl5*4186+320.24370022528, ctrl6*4186+320.24370022528], [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12], amp/6));*/
				chain = Mix(RHPF.ar(in, [ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6] * 108.midicps + 50, [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12], amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('RLPF',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(RLPF.ar(in, [ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6] * 108.midicps + 50, [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12], amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Ringz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(Ringz.ar(in, [ctrl1*500,ctrl2*500+500,ctrl3*500+1000,ctrl4*500+1500,ctrl5*500+2000,ctrl6*500+2500], [ctrl7,ctrl8,ctrl9,ctrl10, ctrl11, ctrl12] * 0.1, amp/6));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Formlet',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(Formlet.ar(in, [ctrl1*300,ctrl2*300+300,ctrl3*300+600,ctrl4*300+900,ctrl5*300+1200,ctrl6*300+1500,ctrl7*300+1800,ctrl8*300+2100,ctrl9*300+2400,ctrl10*300+2700], ctrl11, ctrl12, amp/10));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('Resonz',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(Resonz.ar(in, [ctrl1*1000,ctrl2*1000+1000,ctrl3*1000+2000,ctrl4*1000+3000,ctrl5*1000+4000,ctrl6*1000+5000], [ctrl7,ctrl8, ctrl9, ctrl10, ctrl11, ctrl12], amp));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('DynKlank',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(DynKlang.ar(`[[ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6] * 4186 + 37, [amp / 6, amp / 6, amp / 6, amp / 6, amp / 6, amp / 6], [ctrl7, ctrl8, ctrl9, ctrl10, ctrl11, ctrl12]], in));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PitchShiftFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = Mix(PitchShift.ar(in, 0.1, [ctrl1, ctrl2, ctrl3, ctrl4, ctrl5, ctrl6, ctrl7, ctrl8, ctrl9, ctrl10].clip(0.01, 1) * 8, ctrl11, ctrl12, amp));
				// chain = Limiter.ar(chain, 1.0, 0.01);
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('WarpDelay',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, local, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				local = LocalIn.ar(1);
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				RecordBuf.ar(in, bufferOne, loop: 1, preLevel: 0.333);
				//FX
				chain = Warp1.ar(1, bufferOne, ctrl1 * BufFrames.kr(bufferOne), ctrl2*4, ctrl3.clip(0.01, 1), -1, (ctrl4*16).clip(1, 16), ctrl5, 2, mul: amp);
				// chain = Limiter.ar(chain, 1.0, 0.01);
				LocalOut.ar(DelayC.ar(chain, 4, ctrl6, ctrl7));
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('DJ_FX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, local, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				local = LocalIn.ar(1);
				// Set inFX + Direct AudioIn (levelLocalIn)l+
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				RecordBuf.ar(in, bufferOne, 0, 1, 0.333, 1, 1);
				//FX
				chain = HPplayBuf.ar(1, bufferOne, LFNoise2.kr(ctrl2.reciprocal) + (ctrl1*4), Dust.kr(ctrl3.reciprocal), Logistic.kr(ctrl4 / 2 + 3.5, 100, Rand(0, 1)) * BufFrames.kr(bufferOne), 1, ctrlHP1, ctrlHP2) + local * amp;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				LocalOut.ar(DelayC.ar(chain, 4, ctrl5/1000, ctrl6));
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagFreeze',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=1, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, local, reverse, buffer, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set Reverse sample
				reverse = if(ctrl4.value >= 0.5, 1.neg, 1);
				local = LocalBuf(s.sampleRate * BufDur.kr(bufferOne), 1);
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				RecordBuf.ar(in, bufferOne, loop: 1);
				chain = HPplayBuf.ar(1, bufferOne, (ctrl1 * 2).clip(0.0625, 2.0) * reverse, 1.0, ctrl3 * BufFrames.kr(bufferOne), 1, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(2048, 1), chain);
				PV_RecordBuf(chain, local, 0, 1, 1);
				chain = PV_MagFreeze(chain, SinOsc.kr(ctrl2 * duree.reciprocal));
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = chain * amp;
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_PlayBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, local, reverse, buffer, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set Reverse sample
				reverse = if(ctrl4.value >= 0.5, 1.neg, 1);
				local = LocalBuf(s.sampleRate * BufDur.kr(bufferOne), 1);
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				RecordBuf.ar(in, bufferOne, loop: 1);
				chain = HPplayBuf.ar(1, bufferOne, (ctrl1 * 2).clip(0.0625, 2.0) * reverse, 1.0, 0, 1, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(512, 1), chain);
				PV_RecordBuf(chain, local, 0, 1, 1);
				chain = PV_PlayBuf(chain, local, ctrl2, ctrl3 * BufFrames.kr(local), 1, 1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = chain * amp;
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinPlayBuf',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, local, reverse, buffer, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set Reverse sample
				reverse = if(ctrl7.value >= 0.5, 1.neg, 1);
				local = LocalBuf(s.sampleRate * BufDur.kr(bufferOne), 1);
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				RecordBuf.ar(in, bufferOne, loop: 1);
				chain = HPplayBuf.ar(1, bufferOne, (ctrl1 * 2).clip(0.0625, 2.0) * reverse, 1.0, 0, 1, ctrlHP1, ctrlHP2);
				chain = FFT(LocalBuf(512, 1), chain);
				PV_RecordBuf(chain, local, 0, 1, 1);
				chain = PV_BinPlayBuf(chain, local, ctrl2, ctrl6 * BufFrames.kr(local), ctrl3 * 16, ctrl4 * 8 + 1, ctrl5 * 63 + 1, 1, 1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = chain * amp;
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_HPshiftDownFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_HPshiftDown(chain, ctrl1 * 64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_CutoffFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_Cutoff(chain, ctrl1 * 2 - 1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		//SynthDef('PV_HPecartTypeFX',
		//{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
		//freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
		//levelBusOut=0, levelBusFX=0, levelLocalIn=0,
		//switchBuffer1=0, switchBuffer2=0,
		//panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
		//ctrlHP1=0.33, ctrlHP2=0.5,
		//ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
		//envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
		//envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
		//var chain, in, ambisonic;
		//// Set AMP
		//amp = amp * (ampHi - ampLo) + ampLo;
		//// Set inFX + Direct AudioIn (levelLocalIn)
		//in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
		////FX
		//chain = FFT(LocalBuf(2048, 1), in);
		//chain = PV_HPecartType(chain, ctrl1 * 10 + 1);
		//chain= IFFT(chain) * 3;
		//chain = chain * amp;
		//// Switch Audio Out
		//chain = if(switchAudioOut == 0,
		//// Pan
		//Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
		//if(switchAudioOut == 2,
		//// PanAz
		//PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
		//if(switchAudioOut == 1,
		//// Rotate2
		//Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
		//// Ambisonic
		//(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
		//DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
		//// Out
		//OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
		//OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
		//Out.ar(out, chain * flagAmpOnOff);
		//}).add;

		SynthDef('PV_HPfiltreFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(512, 1), in);
				chain = PV_HPfiltre(chain, ctrl1 * 256 + 4, ctrl2 * 256 + 4);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagNoiseFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagNoise(chain);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagClipFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagClip(chain, ctrl1 * 16);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSmoothFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagSmooth(chain, ctrl1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSmearFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagSmear(chain, ctrl1 * 64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_DiffuserFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_Diffuser(chain, Trig1.kr(LFNoise2.kr(ctrl1*100), (ctrl2*100).reciprocal));
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BrickWallFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_BrickWall(chain, ctrl1 * 2 - 1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_LocalMaxFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_LocalMax(chain, ctrl1 * 64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagSquaredFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagSquared(chain);
				chain= IFFT(chain) * 0.015625;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagBelowFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagBelow(chain, ctrl1 * 64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagAboveFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagAbove(chain, ctrl1 * 64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RandCombFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_RandComb(chain, ctrl1, LFNoise2.kr(ctrl2 * 64));
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagShiftFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagShift(chain, ctrl1 * 4, ctrl2 * 128 -64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinScrambleFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_BinScramble(chain, ctrl1, ctrl2, LFNoise2.kr(duree.reciprocal));
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_BinShiftFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_BinShift(chain, ctrl1 * 4, ctrl2 * 256 -64);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_RectCombFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_RectComb(chain, ctrl1 * 32, ctrl2, ctrl3);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_ConformalMapFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_ConformalMap(chain, ctrl1 * 2 - 1, ctrl2 * 2 - 1);
				chain= IFFT(chain);
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_CompanderFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_Compander(chain, ctrl1 * 64, ctrl2 * 10, ctrl3 * 10);
				chain= IFFT(chain) * 0.125;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_SpectralEnhanceFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_SpectralEnhance(chain, (ctrl1 * 10 + 0.5).floor, ctrl2 * 4+1, ctrl3 * 10);
				chain= IFFT(chain) * 0.125;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagStretchFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagShift(chain, (ctrl1 * 4).clip(0.25, 4));
				chain= IFFT(chain) * 0.125;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('PV_MagShift+StretchFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				chain = FFT(LocalBuf(2048, 1), in);
				chain = PV_MagShift(chain, (ctrl1 * 4).clip(0.25, 4), ctrl2 - 0.5 *128);
				chain= IFFT(chain) * 0.125;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		SynthDef('ConvolutionFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, buffer, trig, ambisonic;
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set inFX + Direct AudioIn (levelLocalIn)
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn));
				//FX
				buffer = LocalBuf(s.sampleRate, 1).clear;
				trig = Dust.kr(duree.reciprocal);
				RecordBuf.ar(in, buffer, Saw.kr(freq).abs, trigger: trig);
				chain = Convolution2L.ar(in, buffer, trig * tempo, 2048) * 0.1;
				chain = chain * amp;
				// Switch Audio Out
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;

		////////////////////////////// Synth + FX SPECIAL ///////////////////////

		SynthDef('SpecialFX',
			{arg out=0, busIn, busOut, busFXout, busFXin, bufferOne, bufferTwo, loopOne=0, loopTwo=0, recBuffer1, recBuffer2,
				freq=0, amp=0, duree=0.01, tempo=1, freqCentroid=0, flatness=0, energy=0, flux=0,
				levelBusOut=0, levelBusFX=0, levelLocalIn=0,
				switchBuffer1=0, switchBuffer2=0,
				panLo=0.1.neg, panHi=0.1, freqLo=0, freqHi=127, freqT=0, ampLo=0, ampHi=1, durLo=0, durHi=1, durM=1, quanta=100, flagAmpOnOff=1,
				ctrlHP1=0.33, ctrlHP2=0.5,
				ctrl1=0.25, ctrl2=0.25, ctrl3=0.25, ctrl4=0.25, ctrl5=0.25, ctrl6=0.25, ctrl7=0.25, ctrl8=0.25, ctrl9=0.25, ctrl10=0.25, ctrl11=0.25, ctrl12=0.25,
				envLevel1=0.0, envLevel2=1.0, envLevel3=1.0, envLevel4=0.75, envLevel5=0.75, envLevel6=0.5, envLevel7=0.5, envLevel8=0.0,
				envTime1=0.015625, envTime2=0.109375, envTime3=0.25, envTime4=0.25, envTime5=0.125, envTime6=0.125, envTime7=0.125;
				var chain, in, rate, local, trig, envelope, reverse, ambisonic;
				// Set FHZ
				rate = 2**((freq.cpsmidi - 48).midicps).cpsoct;// Rate freq - 48
				// Set AMP
				amp = amp * (ampHi - ampLo) + ampLo;
				// Set DUREE
				duree = duree * (durHi - durLo) + durLo;
				duree = duree.floor + ((duree.frac * quanta + 0.5).floor / quanta);
				duree = if(duree <= 0, quanta.reciprocal, duree);
				duree = (duree * durM * tempo.reciprocal).max(0.01);
				// Trig + Envelope
				trig = Impulse.kr(duree.reciprocal);
				//envTime1 = if(envTime1 > duree, 1.0, envTime1 * duree.reciprocal);
				envelope = EnvGen.ar(Env.new([envLevel1,envLevel2,envLevel3,envLevel4,envLevel5,envLevel6,envLevel7,envLevel8],[envTime1,envTime2,envTime3,envTime4,envTime5,envTime6,envTime7].normalizeSum,'sine'), trig, amp, 0, duree, 0);
				// Set Reverse sample
				reverse = if(ctrl6.value >= 0.5, 1.neg, 1);
				ctrl2 = if(reverse < 0, 1 - ctrl2, ctrl2);
				// Set offset
				ctrl2 = if(ctrl2.value <= 0 , ctrl2.value, Logistic.kr(ctrl2*4, 1, Rand(0, 1)));
				// Set inFX + Direct AudioIn (levelLocalIn)
				local = LocalIn.ar(1);
				in = Mix(In.ar(busFXin) + (In.ar(busIn) * levelLocalIn)  + (local * flatness));
				RecordBuf.ar(in, bufferOne, 0, 1, 0, 1, 1);
				//FX
				chain = HPplayBuf.ar(1, bufferOne, BufRateScale.kr(bufferOne) * rate * reverse + SinOsc.kr(flux), 1.0,  flatness * BufFrames.kr(bufferOne), 1, ctrlHP1, ctrlHP2) * envelope;
				// chain = Limiter.ar(chain, 1.0, 0.01);
				LocalOut.ar(DelayC.ar(chain, 4, tempo, tempo.reciprocal));
				// Switch Audio Out
				chain = chain * amp;
				chain = if(switchAudioOut == 0,
					// Pan
					Pan2.ar(chain, TRand.kr(panLo, panHi, Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal))),
					if(switchAudioOut == 2,
						// PanAz
						PanAz.ar(numberAudioOut, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo), 1, widthMC, orientationMC),
						if(switchAudioOut == 1,
							// Rotate2
							Rotate2.ar(chain, chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo)),
							// Ambisonic
							(ambisonic = PanB2.ar(chain, LFSaw.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal, mul: TRand.kr(abs(panHi - panLo), abs(panHi - panLo), Dust.kr((duree * (durHi - durLo) + durLo * durM * tempo.reciprocal).reciprocal)), add: panLo));
								DecodeB2.ar(numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
				// Out
				OffsetOut.ar(busOut, Mix(chain * levelBusOut.value));// Send Bus Out Mono
				OffsetOut.ar(busFXout, Mix(chain * levelBusFX.value));// Send Bus FX Mono
				Out.ar(out, chain * flagAmpOnOff);
		}).add;
	}

}
