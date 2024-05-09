// A Software by Herve Provini

Agents {


	classvar  <> s;

	var keyboardShortCut, keyboardTranslate, keyboardTranslateBefore, setupKeyboardShortCut, keyboard, keyVolume, windowKeyboard, keyboardVolume, fonctionShortCut, windowVST, flagVST, numberAudioIn, rangeBand;

	*new	{arg path="~/Documents/Agents/", ni=26, o=2, r=2, f=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0, name="Agents", wek=6448, wekPort=57120, scPort=57110;

		^super.new.init(name, path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, scPort);

	}

	init	{arg name, path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, scPort;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		~nompathdata=PathName.new(path).pathOnly;

		// Verify path
		if(File.exists(~nompathdata).not) {systemCmd("mkdir" + ~nompathdata)};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		numberAudioIn = ni;
		~numberAudioOut = o;
		~recChannels = r;
		~switchAudioOut = f;// Type Format stereo, ambisonic, etc...

		//Server.default = s = Server(name,NetAddr("localhost", scPort), Server.default.options);

		s = Server.default;

		s.options.memSize = 2**20;
		s.options.inDevice_(devIn);
		s.options.outDevice_(devOut);
		s.options.numInputBusChannels_(numberAudioIn);
		s.options.numOutputBusChannels_(~numberAudioOut);
		s.options.hardwareBufferSize_(size);
		~headerFormat = "aiff";
		~sampleFormat = "float";
		~startChannelAudioOut = 0;
		~flagMC = flag;
		~widthMC = wid;
		~orientationMC = ori;

		thisProcess.openUDPPort(NetAddr.langPort);

		Safety(s);
		//s.makeGui;

		~samplePourAnalyse = Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff";
		~listeSamplePourAnalyse = [];
		~listeNameSamplePourAnalyse = [];

		// MIDI
		MIDIClient.init;
		// Connect first device by default
		MIDIIn.connect(0, 0);
		~midiOut = MIDIOut(0).latency = 0.01;
		~midiOut.allNotesOff(0);

		// Collect all Preset
		~foldersToScanAll = PathName.new(~nompathdata).files.collect{ |path| var file;
			file = path.fileName;
			if(file.find("preset") == 0 or: {file.find("Preset") == 0}, {file});
		};
		~foldersToScanAll = ~foldersToScanAll.reject({arg item; item == nil});

		// Collect preset
		~foldersToScanPreset = ~foldersToScanAll.collect{ |file|
			if(file.find("preset ") == 0 or: {file.find("Preset ") == 0}, {file});
		};
		~foldersToScanPreset = ~foldersToScanPreset.reject({arg item; item == nil});

		~nomFenetre = "Control Panel"+~nompathdata.asString;

		~helpHPAgents="
Single commandes:

esc	or SpaceBar			System on/off.
a						Display datas on/off.
b						Switch loopMusic on/off.
d				 		Reset start sequences agents.
alt + d					Synchro agents on Temporal Grid (nextBar).
ctrl + e / alt + e		Reset FX_Group / Reset Verb_Group.
E						nil.
e				 		Elitist mode on/off.
ctrl + f				Load and Add sound file for analyser.
F				 		Loop sound file analyser on.
alt + f					Loop sound file analyse Off.
ctrl + f		 		Restart sound file analyse on Temporal Grid (nextBar).
h				 		Switch source in (audio - file - midi).
i				 		Init agents.
alt + i			 		Init preset.
ctrl + alt + i			Init control.
j						Virtual space tore on/off.
J						Flock agents on/off.
alt + J					Shared music on/off.
k						New synth, sounds, FX, path for data environment.
alt + k					New synth environment.
K						New sound environment.
ctrl + k				New FX environment.
m			 			Music on/off.
alt + m					Tempo analyze on/off.
ctrl + n				Add a new agent.
o			 			Automation on/off.
q				 		Switch algo analyze.
alt + r					Start recording.
shift + alt + r			Switch pause recording on/off.
ctrl + alt + r			Stop recording.
ctrl + s			 	Save buffer sound (playing in panel synthesizer).
ctrl + t				Save preset with genome+sequence and date+time.
ctrl + u				Stop recording score.
v				 		View virtual space on/off.
alt + v			 		Init virtual space.
w / ctrl + w			Display windows.
y						Mean state system on/off.
z				 		Random load preset.
/*alt + z				Random load preset with genome.
alt + Z					Random load preset with genome and sequence.*/
ctrl + z				Stop all score.

Commandes follow by a numerical key (0,..9 ; shift 0,..9 ; alt 0,..9 ; alt + shift 0,..9):

g						Switch activate genes.
l			 			Load preset.
f						Switch file for analyze.
alt + l					Load preset with genome.
alt + L					Load preset with genome+sequence.
ctrl + l				Load preset synchro on Temporal Grid (nextBar).
/*ctrl + alt + l		Load preset with genome on Temporal Grid (nextBar).
ctrl + alt + L			Load preset with genome+sequence on Temporal Grid (nextBar).*/
L			 			Load control panel.
N						Add a copy of an agent.
p / ctrl + p			Play (loop off) / Stop score.
P						Play score (loop on).
ctrl + alt + p          Play step score (loop off).
ctrl + shift + alt + p  Play step score(loop on).
enter                   Next event step score.
alt + p					Switch synthDef.
s				 		Save preset.
alt + s				    Save preset with genome.
alt + S					Save preset with genome+sequence.*/
S				 		Save control panel.
t			 			Automation: 1. Init agents.
2. Random load preset.
3. Sliders control.
4. Genes music.
5. Genes synth.
6. Sliders synth
u						Start recording score.
alt + u			 		Switch samples.

//////////////////////////////////////////////////////////////////////////////////////////

ShortCut for Keyboard Panel:

<						Keyboard Transpose down.
>						Keyboard Transpose up.

ysxdcvgbhnjm,l.e-		Musical Keys.
";

		~helpHPgenomesequenceEditor="
Single commandes:

alt + x / X				Agent next/previous.
alt + c / C				Agent-Copy next/previous.
alt + y					Copy genome.
Y						Copy sequence Fhz-Amp-Time.
G                       Init Genome Agent (solo).
";

		// Custom menu for Agents

		~menuFile = Menu(
			MenuAction("Load",
				{Dialog.openPanel({ arg paths, file;
					~samplePourAnalyse=paths;
					s.bind{
						~listeSamplePourAnalyse.do({arg buffer; buffer.free});
						~listeSamplePourAnalyse = [];
						~listeNameSamplePourAnalyse = [];
						~bufferanalysefile.free;
						s.sync;
						~synthPlayFile.set('trig', 0);
						s.sync;
						~synthPlayFile.run(false);
						s.sync;
						file = SoundFile.new;
						s.sync;
						file.openRead(~samplePourAnalyse);
						s.sync;
						if(file.numChannels == 1,
							{~rawData= FloatArray.newClear(file.numFrames * 2);
								s.sync;
								file.readData(~rawData);
								s.sync;
								Post << "Loading sound for analyze" << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~rawData = Array.newFrom(~rawData).stutter(2) / 2;
								s.sync;
								~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2, action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
							},
							{Post << "Loading sound for analyze" << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse,  channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
						});
						//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
						file.close;
						s.sync;
						~sounds.size.do({arg i; ~recFiles.wrapAt(i).set('in', ~busFileIn.index)});
						~agents.do({arg i; ~synthRecFileAgents.wrapAt(i).set('in', ~busFileIn.index)});
						~textFileAnalyze.string_(paths.asString);
						if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File'}, {
							~synthPlayFile.run(true);
							~synthPlayFile.set(\trig, 0);
							s.sync;
							~audioFile.set(\trigger, 0);
							s.sync;
							~synthPlayFile.set(\trig, 1);
							s.sync;
							~audioFile.set(\trigger, 1);
							s.sync;
						});
					};
			},{"cancelled".postln})}),
			Menu(
				MenuAction("Loop On", {~synthPlayFile.set('loop', 1)}),
				MenuAction("Loop Off", {~synthPlayFile.set('loop', 0)});
			).title_("Loop"),
			MenuAction("Synchro on Temporal Grid (Tempo BPM / BeatsPerBar)", {
				if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File'}, {
					s.bind{
						~synthPlayFile.set('trig', 0);
						s.sync;
						~synthPlayFile.run(false);
						s.sync;
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~synthPlayFile.run(true);~synthPlayFile.set('trig', 1);nil});
						s.sync;
					};
			})});
		);
		MainMenu.register(~menuFile.title_("File for Analyze"), "AgentsTools");

		~menuFX = MenuAction("Reset FX Group", {
			~groupeSynthAgents.freeAll;
			~groupeEffets.freeAll;
			~busEffetsAudio.free;
			~busEffetsAudio=Bus.audio(s, 2);
			~listSynthEffets=[];
			~audioOutEffets=[];
			~playSynthEffets=[];
			~controlsSynthEffets=[];
			~panSynthEffets=[];
			~jitterPanSynthEffets=[];
			~jitterControlsSynthEffets=[];
			~ampSynthEffets=[];
			~automationPanEffets=[];
			~automationControlsEffets=[];
			~automationSpeedEffets = [];
			~listeFXTime = [];
			~listEffets.size.do({arg i;
				~listSynthEffets=~listSynthEffets.add(Synth.newPaused(~listEffets.wrapAt(i).asString,['in', ~busEffetsAudio.index, 'busverb', ~busVerbAudio.index, 'amp', -12.dbamp, 'pan', 0.0, 'control1', 0.5,  'control2', 0.5,  'control3', 0.5,  'control4', 0.5,  'control5', 0.5,  'control6', 0.5,  'control7', 0.5,  'control8', 0.5], ~groupeEffets, \addToTail));
				~audioOutEffets=~audioOutEffets.add(0);
				~controlsSynthEffets=~controlsSynthEffets.add([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
				~playSynthEffets=~playSynthEffets.add(0);
				~panSynthEffets=~panSynthEffets.add(0.0);
				~jitterPanSynthEffets=~jitterPanSynthEffets.add(0.1);
				~jitterControlsSynthEffets=~jitterControlsSynthEffets.add(0.1);
				~ampSynthEffets=~ampSynthEffets.add(-3);
				~automationPanEffets=~automationPanEffets.add(0);
				~automationControlsEffets=~automationControlsEffets.add(0);
				~automationSpeedEffets = ~automationSpeedEffets.add(24);
				~listeFXTime = ~listeFXTime.add(24.reciprocal);
			});
			~listSynthEffets.size.do({arg effet;
				~listSynthEffets.wrapAt(effet).set('busverb', ~busVerbAudio.index, 'amp', ~ampSynthEffets.wrapAt(effet).dbamp, 'pan', ~panSynthEffets.wrapAt(effet), 'control1', ~controlsSynthEffets.wrapAt(effet).wrapAt(0),  'control2', ~controlsSynthEffets.wrapAt(effet).wrapAt(1),  'control3', ~controlsSynthEffets.wrapAt(effet).wrapAt(2),  'control4', ~controlsSynthEffets.wrapAt(effet).wrapAt(3),  'control5', ~controlsSynthEffets.wrapAt(effet).wrapAt(4),  'control6', ~controlsSynthEffets.wrapAt(effet).wrapAt(5),  'control7', ~controlsSynthEffets.wrapAt(effet).wrapAt(6),  'control8', ~controlsSynthEffets.wrapAt(effet).wrapAt(7));
			});
		});
		MainMenu.register(~menuFX, "AgentsTools");

		~menuRecording = Menu(
			MenuAction("Start", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
				~fonctionRecOn.value;
			};
			),
			MenuAction("Stop", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec Off
				~fonctionRecOff.value;
			};
			),
			MenuAction("Pause", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec Pause
				~fonctionRecPause.value;
			};
			),
			MenuAction("Save Buffer Sound (Playing in Panel Synthesizer)", {
				Dialog.savePanel({arg path;
					~bufferSonsInstruments.write(path++".aiff","AIFF","float")},{"cancelled".postln})};
			);
		);
		MainMenu.register(~menuRecording.title_("Recording"), "AgentsTools");

		~menuAudio = Menu(
			//Menu(
			MenuAction("Stereo", {~switchAudioOut=0; this.initSynthDef}
			),
			MenuAction("MultiSpeaker", {~switchAudioOut=2; this.initSynthDef}
			),
			MenuAction("Rotate2", {~switchAudioOut=1; this.initSynthDef};
			),
			MenuAction("Ambisonic", {~switchAudioOut=3; this.initSynthDef};
			);
			//).title_("Audio Out");
		);
		MainMenu.register(~menuAudio.title_("Audio"), "AgentsTools");

		~menuAlgo = Menu(
			Menu(
				MenuAction("Flux", {~variableChange="Flux"; ~changeSynth.value_(~valueSynthChange.wrapAt(0))};
				),
				MenuAction("Flatness", {~variableChange="Flatness"; ~changeSynth.value_(~valueSynthChange.wrapAt(1))};
				),
			).title_("Variable"),
			Menu(
				MenuAction("Probability", {~algoChange="Probability"};
				),
				MenuAction("OnFly", {~algoChange="OnFly"};
				);
			).title_("Analyze");
		);
		MainMenu.register(~menuAlgo.title_("Algorithm"), "AgentsTools");


		~menuMidi = Menu(
			MenuAction("Init", {
				MIDIClient.init;
				if(MIDIClient.externalSources != [ ], {
					// Connect first device by default
					MIDIIn.connect(0, 0);
					~midiOut = MIDIOut(0);
					//midiOut.connect(0);
					16.do({arg canal; ~midiOut.allNotesOff(canal)});
				}, {"Warning no MIDI Devices Connected".postln});
			}),
			Menu(
				MenuAction("In", {
					SCRequestString("0", "Device", {arg index, port;
						port = index.asInteger;
						MIDIIn.connect(port, MIDIClient.sources.wrapAt(port));
					});
				}),
				MenuAction("Out", {
					SCRequestString("0", "Device", {arg index, port;
						port = index.asInteger;
						~midiOut = MIDIOut(port);
						//midiOut.connect(port);
						16.do({arg canal; ~midiOut.allNotesOff(canal)});
					});
				});
			).title_("Setting"),
			Menu(
				MenuAction("On", {~userOperatingSystem.valueAction_(22)};
				),
				MenuAction("Off", {~userOperatingSystem.valueAction_(23)};
				),
			).title_("MIDI Out"),
			Menu(
				MenuAction("Control 1", {SCRequestString("100", "Control 1", {arg strg; ~controlMIDIone = strg.asInteger});
				}),
				MenuAction("Control 2", {SCRequestString("101", "Control 2", {arg strg; ~controlMIDItwo = strg.asInteger});
				}),
				MenuAction("Control 3", {SCRequestString("102", "Control 3", {arg strg; ~controlMIDIthree = strg.asInteger});
				}),
			).title_("Synth Controls"),
		);
		MainMenu.register(~menuMidi.title_("MIDI"), "AgentsTools");

		~menuSoft = Menu(
			MenuAction("Genome Number Editor", {HPgenomeEditorNumber.new};
			),
			MenuAction("Genome Slider Editor", {HPgenomeEditorSlider.new};
			),
			MenuAction("Genome AllInOne Editor", {HPgenomeEditorAllInOne.new};
			),
			MenuAction("Sequence Editor", {HPsequenceEditor.new};
			),
			MenuAction("Score Editor", {HPscoreEditor.new};
			),
			MenuAction("Live Coding Editor", {HPliveCoding.new};
			),
		);
		MainMenu.register(~menuSoft.title_("Soft"), "AgentsTools");

		~menuKeys = Menu(
			MenuAction("List ShortCuts Agents", {
				//Document.new("ShortCut for Agents", ~helpHPAgents);
				TextView().name_("ShortCut for Agents").string_(~helpHPAgents).front
			};
			),
			MenuAction("List ShortCuts Genome + Sequence Editor", {
				//Document.new("ShortCuts for Genome + Sequence Editor", ~helpHPgenomesequenceEditor)};
				TextView().name_("List ShortCuts Genome + Sequence Editor").string_(~helpHPgenomesequenceEditor).front;
			});
		);
		MainMenu.register(~menuKeys.title_("ShortCuts"), "AgentsTools");

		~fonctionRecOn={
			if(~oscStateflag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
			if(~flagRecording == 'off', {
				~flagRecording = 'on';
				s.bind{
					s.recChannels_(~recChannels);
					s.sync;
					s.recHeaderFormat_(~headerFormat);
					s.sync;
					s.recSampleFormat_(~sampleFormat);
					s.sync;
					s.prepareForRecord("~/Music/SuperCollider Recordings/".standardizePath ++ "Agents_" ++ Date.localtime.stamp ++ "." ++ ~headerFormat);
					s.sync;
					s.record;
					s.sync;
				};
			});
		};

		~fonctionRecOff={
			if(~oscStateflag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec On
			~flagRecording = 'off';
			s.stopRecording;
		};

		~fonctionRecPause={
			if(~oscStateflag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec On
			if(~startsysteme.value == 1, {
				if(~flagRecording == 'on', {s.pauseRecording; ~flagRecording = 'pause'},{s.record; ~flagRecording = 'on'});
			});
		};

		~fonctionUserOperatingSystem = {arg item, file, addrM, addrS;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load File Pour Analyse
				1, {Dialog.openPanel({ arg paths;
					~samplePourAnalyse=paths;
					s.bind{
						~listeSamplePourAnalyse.do({arg buffer; buffer.free});
						~listeSamplePourAnalyse = [];
						~listeNameSamplePourAnalyse = [];
						~bufferanalysefile.free;
						s.sync;
						~synthPlayFile.set('trig', 0);
						s.sync;
						~synthPlayFile.run(false);
						s.sync;
						file = SoundFile.new;
						s.sync;
						file.openRead(~samplePourAnalyse);
						s.sync;
						if(file.numChannels == 1,
							{~rawData= FloatArray.newClear(file.numFrames * 2);
								s.sync;
								file.readData(~rawData);
								s.sync;
								Post << "Loading sound for analyze " << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~rawData = Array.newFrom(~rawData).stutter(2) / 2;
								s.sync;
								~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2, action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
							},
							{Post << "Loading sound for analyze " << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse,  channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
						});
						//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
						file.close;
						s.sync;
						~sounds.size.do({arg i; ~recFiles.wrapAt(i).set('in', ~busFileIn.index)});
						~agents.do({arg i; ~synthRecFileAgents.wrapAt(i).set('in', ~busFileIn.index)});
						~textFileAnalyze.string_(paths.asString);
						if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File'}, {~synthPlayFile.run(true);
							s.sync;
							~synthPlayFile.set('trig', 1);
							s.sync;

						});
					};
				},{"cancelled".postln})},
				// Load Control Panel
				2, {Dialog.openPanel({ arg paths;
					var file;
					file=File(paths,"r");
					~wp.name=~nomFenetre+paths;
					~loadMonde.value(file);
					file.close},{"cancelled".postln})},
				// Load Preset
				3, {Dialog.openPanel({ arg paths;
					var file;
					file=File(paths,"r");
					~wp.name=~nomFenetre+paths;
					~loadUnivers.value(file, 'on', 'on');
					file.close},{"cancelled".postln})},
				// Do Nothing
				4, {nil},
				// Do Nothing
				5, {nil},
				// Load Genome
				6, {Dialog.openPanel({ arg paths;
					var file;
					file=File(paths,"r");
					~genomes = file.readAllString.interpret;
					file.close},{"cancelled".postln})},
				// Load Sequences
				7, {Dialog.openPanel({ arg paths;
					var file, seq;
					file=File(paths,"r");
					seq = file.readAllString.interpret;file.close;
					~listeagentfreq=seq.wrapAt(0);~listeagentamp=seq.wrapAt(1);~listeagentduree=seq.wrapAt(2)},{"cancelled".postln})},
				// Commande Save Control Panel
				8, {Dialog.savePanel({arg path; var file;
					~wp.name=~nomFenetre+path;
					file=File(path++".scd","w");file.write(~foncSaveMonde.value.asCompileString);file.close},{"cancelled".postln})},
				// Commande Save Preset
				9, {Dialog.savePanel({arg path;
					var name, pathonly, fileName;
					path = PathName.new(path);
					pathonly = path.pathOnly;
					name = path.fileName;
					name = "preset"+name;
					path = pathonly++name;
					fileName = PathName.new(path).fileName;
					path = PathName.new(path).fullPath;
					~wp.name=~nomFenetre + fileName;
					file=File(path++".scd","w");file.write(~foncSaveUnivers.value(~foncSaveMonde.value, 'on', 'on').value.asCompileString);file.close},{"cancelled".postln})},
				// Do Nothing
				10, {nil},
				// Do Nothing
				11, {nil},
				// Save Genome
				12, {Dialog.savePanel({arg path; var file;
					file=File(path++".scd","w");file.write(~genomes.asCompileString);file.close},{"cancelled".postln})},
				// Save Sequences
				13, {Dialog.savePanel({arg path; var file;
					file=File(path++".scd","w");file.write([~listeagentfreq,~listeagentamp,~listeagentduree].asCompileString);file.close},{"cancelled".postln})},
				// New synth environment
				14, {
					//Init Path
					Dialog.openPanel({ arg paths;
						var file="List Synth.scd", p;
						p = PathName.new(paths);
						file = p.fileName;
						p = p.pathOnly;
						s.bind{
							~initAllSynth.value(p, file);
							s.sync;
							~synthDefInstrMenu.items_(~listSynth);
							s.sync;
							{~geneSynthRangerLow.items_(~listSynth);
								~geneSynthRangerLow.valueAction_(~geneSynthRangerLow.value.clip2(~listSynth.size-1));
								~geneSynthRangerHigh.items_(~listSynth);
								~geneSynthRangerHigh.valueAction_(~geneSynthRangerHigh.value.clip2(~listSynth.size-1));
								if(~flagHPgenomeEditor == 'on', {~ag34.items_(~listSynth);
									~ag34.valueAction_(~ag34.value.clip2(~listSynth.size-1))});
							}.defer;
							s.sync;
							~listeWindows.do({arg w; w.do({arg v; v.refresh})});
							s.sync;
							//Document.listener.string="";
							s.sync;
							s.queryAllNodes;
							s.sync};
				})},
				// New sound environment
				15, {
					//Init Path
					Dialog.openPanel({ arg paths;
						var p, file="List Sounds.scd";
						p = PathName.new(paths);
						file = p.fileName;
						p = p.pathOnly;
						s.bind{
							~groupeBuffer.freeAll;
							s.sync;
							~bufferSons.do({arg buf; buf.free});
							s.sync;
							~initAllSound.value(p, file);
							s.sync;
							~initAllBuffer.value;
							s.sync;
							~soundsInstrMenu.items_(~displaySons);
							s.sync;
							{~geneSampleRangerLow.items_(~displaySons);
								~geneSampleRangerLow.valueAction_(~geneSampleRangerLow.value.clip2(~sounds.size-1));
								~geneSampleRangerHigh.items_(~displaySons);
								~geneSampleRangerHigh.valueAction_(~geneSampleRangerHigh.value.clip2(~sounds.size-1));
								if(~flagHPgenomeEditor == 'on', {~ag14.items_(~displaySons);
									~ag14.valueAction_(~ag14.value.clip2(~sounds.size-1))});
							}.defer;
							s.sync;
							if(~flagEntreeMode == 'Audio', {~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(true);~recFiles.wrapAt(i).run(false)});
								~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).run(false)});~listesamplein=~recSamples},
							{~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(false);~recFiles.wrapAt(i).run(true)});
								~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(false);~synthRecFileAgents.wrapAt(i).run(true)});~listesamplein=~recFiles});
							s.sync;
							~listeWindows.do({arg w; w.do({arg v; v.refresh})});
							s.sync;
							//Document.listener.string="";
							s.sync;
							s.queryAllNodes;
							s.sync};
				})},
				// New FX environment
				16, {
					//Init Path
					Dialog.openPanel({ arg paths;
						var p, file="List FX.scd";
						p = PathName.new(paths);
						file = p.fileName;
						p = p.pathOnly;
						s.bind{
							~groupeEffets.freeAll;
							s.sync;
							~initAllEffet.value(p, file);
							s.sync;
							~initSynthEffets.value;
							s.sync;
							~effetsInstrMenu.items_(~listEffets);
							s.sync;
							~sourceOutEffets.valueAction=0;
							~effetsInstrMenu.valueAction_(0);
							~playEffetsButton.valueAction_(0);
							~controlsEffetsMenu.valueAction_([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
							~panEffets.valueAction_(0);
							~jitterPanEffets.valueAction_(0.1);
							~ampEffets.valueAction_(-3);
							~randomPanEffets.value_(0);
							~randomControlsEffets.value_(0);
							~speedEffets.valueAction_(24);
							s.sync;
							~listeWindows.do({arg w; w.do({arg v; v.refresh})});
							s.sync;
							//Document.listener.string="";
							s.sync;
							s.queryAllNodes;
							s.sync};
				})},
				// New all environment
				17, {
					//Init Path
					FileDialog.new({arg path;
						var p, fileSynth="List Synth.scd",  fileSound="List Sounds.scd", fileFX="List FX.scd";
						p = ~nompathdata = path.at(0).asString ++"/";
						/*~nompathdata = PathName.new(paths);
						p = ~nompathdata = ~nompathdata.pathOnly;*/
						~nomFenetre = "Control Panel"+~algoMusic+~nompathdata.asString;
						~wp.name=~nomFenetre;
						s.bind{
							// Synth
							~initAllSynth.value(p, fileSynth);
							s.sync;
							~synthDefInstrMenu.items_(~listSynth);
							s.sync;
							{~geneSynthRangerLow.items_(~listSynth);
								~geneSynthRangerLow.valueAction_(~geneSynthRangerLow.value.clip2(~listSynth.size-1));
								~geneSynthRangerHigh.items_(~listSynth);
								~geneSynthRangerHigh.valueAction_(~geneSynthRangerHigh.value.clip2(~listSynth.size-1));
								if(~flagHPgenomeEditor == 'on', {~ag34.items_(~listSynth);
									~ag34.valueAction_(~ag34.value.clip2(~listSynth.size-1))});
							}.defer;
							s.sync;
							// Sounds
							s.sync;
							~bufferSons.do({arg buf; buf.free});
							s.sync;
							~recSamples.do({arg synth; synth.free});
							s.sync;
							~recFiles.do({arg synth; synth.free});
							s.sync;
							~initAllSound.value(p, fileSound);
							s.sync;
							~initAllBuffer.value;
							s.sync;
							~soundsInstrMenu.items_(~displaySons);
							s.sync;
							{~geneSampleRangerLow.items_(~displaySons);
								~geneSampleRangerLow.valueAction_(~geneSampleRangerLow.value.clip2(~sounds.size-1));
								~geneSampleRangerHigh.items_(~displaySons);
								~geneSampleRangerHigh.valueAction_(~geneSampleRangerHigh.value.clip2(~sounds.size-1));
								if(~flagHPgenomeEditor == 'on', {~ag14.items_(~displaySons);
									~ag14.valueAction_(~ag14.value.clip2(~sounds.size-1))});
							}.defer;
							s.sync;
							if(~flagEntreeMode == 'Audio', {~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(true);~recFiles.wrapAt(i).run(false)});
								~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).run(false)});~listesamplein=~recSamples},
							{~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(false);~recFiles.wrapAt(i).run(true)});
								~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(false);~synthRecFileAgents.wrapAt(i).run(true)});~listesamplein=~recFiles});
							s.sync;
							// FX
							~groupeEffets.freeAll;
							s.sync;
							~initAllEffet.value(p, fileFX);
							s.sync;
							~initSynthEffets.value;
							s.sync;
							~effetsInstrMenu.items_(~listEffets);
							s.sync;
							~sourceOutEffets.valueAction=0;
							~effetsInstrMenu.valueAction_(0);
							~playEffetsButton.valueAction_(0);
							~controlsEffetsMenu.valueAction_([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
							~panEffets.valueAction_(0);
							~jitterPanEffets.valueAction_(0.1);
							~ampEffets.valueAction_(-3);
							~randomPanEffets.value_(0);
							~randomControlsEffets.value_(0);
							~speedEffets.valueAction_(24);
							s.sync;
							//Document.listener.string="";
							s.queryAllNodes;
							s.sync;
							~listeWindows.do({arg w; w.do({arg v; v.refresh})});
							s.sync;
							// Load if exists file SynthDef and SynthDefFX adding synth and fx
							if(File.exists(~nompathdata++"List SynthDef.scd"),{file=File(~nompathdata++"List SynthDef.scd", "r");file.readAllString.interpret;file.close;"Adding File SynthDef !".postln},{"No Adding File SynthDef !".postln});
							s.sync;
							if(File.exists(~nompathdata++"List SynthDefFX.scd"),{file=File(~nompathdata++"List SynthDefFX.scd", "r");file.readAllString.interpret;file.close;"Adding File SynthDefFX !".postln},{"No Adding File SynthDefFX !".postln});
							s.sync;
						};
						// Collect all Preset
						~foldersToScanAll = PathName.new(~nompathdata).files.collect{ |path| var file;
							file = path.fileName;
							if(file.find("preset") == 0, {file});
						};
						~foldersToScanAll = ~foldersToScanAll.reject({arg item; item == nil});
						// Collect preset
						~foldersToScanPreset = ~foldersToScanAll.collect{ |file|
							if(file.find("preset ") == 0, {file});
						};
						~foldersToScanPreset = ~foldersToScanPreset.reject({arg item; item == nil});
				}, fileMode: 2)},
				// OSC Off
				18, {~oscStateFlag='off';~stateOSC.string = "OSC Off"},
				// OSC Master
				19, {~oscStateFlag='master';~stateOSC.string = "OSC Master"},
				// OSC Slave
				20, {~oscStateFlag='slave';~stateOSC.string = "OSC Slave"},
				// OSC Setting
				21, {
					// Set OSC Addresse et Port Master
					addrM=NetAddr.localAddr;
					addrS=NetAddr.localAddr;
					~slaveAppAddr.disconnect;
					~oscHPtempo.free;
					~oscHPstart.free;
					~oscHPrec.free;
					SCRequestString(addrM.ip, "Enter the NetAddr of Master App", {arg strg; addrM=strg;
						SCRequestString(NetAddr.langPort.asString, "Enter the Port of Master App", {arg strg; addrM=NetAddr(addrM, strg.asInteger); ~masterAppAddr = addrM;
							// Set OSC Addresse et Port Slave
							SCRequestString(addrS.ip, "Enter the NetAddr of Slave App", {arg strg; addrS=strg;
								SCRequestString(NetAddr.langPort.asString, "Enter the Port of Slave App", {arg strg; addrS=NetAddr(addrS, strg.asInteger); ~slaveAppAddr = addrS;
									~initOSCresponder.value;
								});
							});
						});
					});
				},
				// Midi
				22, {~flagMidiOut = 'on';if(~geneMidiOutButton.value == 0, {~canalMidiOutSlider.enabled_(true)},{~canalMidiOutSlider.enabled_(false)});~geneMidiOutButton.enabled_(true)},
				23, {~flagMidiOut = 'off';~canalMidiOutSlider.enabled_(false);~geneMidiOutButton.enabled_(false);
					16.do({arg canal; ~midiOut.allNotesOff(canal)});
				},
				// Add Agent
				24, {
					if(~agents < ~maximumagents, {~initagents.value(~agents, nil, nil, nil, 'init', [], [], [], 0, 0, 0);~agents=~agents + 1});
				}
			);
		};

		s.waitForBoot({

			// Init Synth
			this.initSynthDef(true);

			// Load if file SynthDef and SynthDefFX adding synth and fx
			if(File.exists(~nompathdata++"List Synth adding.scd"),{f=File(~nompathdata++"List Synth adding.scd", "r");f.readAllString.interpret;f.close;"Adding File Synth !".postln},{"No Adding File Synth !".postln});
			~listSynthAdd.postcs;
			if(File.exists(~nompathdata++"List FX adding.scd"),{f=File(~nompathdata++"List FX adding.scd", "r");f.readAllString.interpret;f.close;"Adding File FX !".postln},{"No Adding File FX !".postln});
			~listFXAdd.postcs;

			// INIT ALL SYSTEM

			~ardourOSC = NetAddr("127.0.0.1", 3819);// define NetAddr on local machine with Ardour's port number

			// Setup Default start init values
			// Audio In
			~tempoData = ~tempoagents = 24;
			~tempoVirtual=24;
			~tempoMusic=60;
			~flagTempoAnalyze = 'off';
			~flagScoreRecordGUI = 'on';
			~quantaMusic=100;
			~nombreBeatsBare = 1;
			~tempoMusicPlay = TempoClock.default;
			~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~tempoMusicPlay.beatsPerBar_(~nombreBeatsBare);nil});
			~canalMidiIn=0;
			~canalMidiOut=1.neg;
			~canauxMidiInOut=[0, 0];
			~canalMidiOutAgent=[];
			~controlMIDIone = 100;
			~controlMIDItwo = 101;
			~controlMIDIthree = 102;
			~differencefreq=0;// interval minimum entre 2 freq
			~differenceamp= 0;// interval minimum entre 2 amp
			~differenceduree=0.03125;// interval minimum entre 2 durees analyse
			~dureeaccord=0.0625;// interval min pour accord
			~maxaccord=3;// accord maximum notes
			~listedatasizein=12;// evenements max data audioIn et MidiIn
			~dureeanalysemax=4.0;//secondes
			~tempsmaxsignal=4.0;// secondes
			~indexwindow=0;//control panel = position 0 dans la liste
			~dureeChangeConfig=[16, 64];// vie par config si automation
			~valueSynthChange = [2.0, 2.0];// flux, flatness
			~choixChangeConfig=[1, 40];//choix des config pour change
			~flagRecording = 'off';
			// Agents
			~dureemaximumvie=3600.0;// moyenne en seconde
			~dureeVieAgents=60.0;
			~vitessegeneration=1.0;//vitesse des generations
			~trancheage=[0.18, 0.85];
			~jeunesse=~dureeVieAgents - (~trancheage.wrapAt(0) * ~dureeVieAgents);
			~vieilliesse=~dureeVieAgents - (~trancheage.wrapAt(1) * ~dureeVieAgents);
			~distanceagents=~dat=0.25;
			~distancesignaux=~dst=0.25;
			~mutation=0.15;
			~croisement=0.5;
			~learning=0.075;
			~naissance=0.5;
			~listedatamusiqueagents=12;
			~startpopulation=6;
			~agents=6;
			~maximumagents=12;
			~maximumabsoluagents=64;// Maximum absolu d'agents
			~maximumenfants=3;
			~vitesseagents=1.0;//granulation temporelle de l'espace
			~deviance=~dvt=0.0625;// % deviance signaux agents voisins
			~paraAlgoAnalyseAudio=[[0.5, 0.5], [0.5, 0.5], [0.5, 0.5]];
			~musicOutAgents=[];
			// Init liste score pour tdef
			~listeRoutinePlayingScore=[];
			~listeFlagRoutinePlayingScore=[];
			~indexStepScore=0;
			~flagStepScore='off';
			~score=nil;
			~numberStepScore=nil;
			40.do({arg i; ~listeRoutinePlayingScore=~listeRoutinePlayingScore.add([]);
				~listeFlagRoutinePlayingScore=~listeFlagRoutinePlayingScore.add([]);
			});
			~commande='nil';
			~commandeScore='nil';
			~commandeExecute='nil';
			~lastTimeAutomationPreset = Main.elapsedTime;
			~lastNumberChoiceConfig = nil;
			~lastMeanProbaPresetFlux = 0;
			~lastMeanProbaPresetFlatness = 0;
			~variableChange="Flux";
			~algoChange="OnFly";
			~dataFFTanalyze = [[], []];
			~limitTemps = 0;
			~tuning = Tuning.et12;
			~degrees = ~tuning.semitones;
			~root = 0;
			~scale = Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning);
			~flagScaling = 'off';
			~flagAlgoAnalyze == 0;
			flagVST = 'off';

			//////////////////////////////////////////////////////////

			~entreemode='Audio';// Par defaut entree audio
			~keyCodeSourceIn=0;
			~flagEntreeMode='Audio';
			~listemodeentree=['Audio','File','Midi', 'Off'];
			~sourceOutAgents=[];
			32.do({arg i;var x;
				i=i+1;x="Out"+i.asString;~sourceOutAgents=~sourceOutAgents.add(x.asSymbol);
			});
			~sourceOutAgents = ~sourceOutAgents.add("Off");
			~audioInLR=[];
			32.do({arg i; ~audioInLR=~audioInLR.add(i+1);
			});
			~timeenvelope=[0.015625, 0.109375, 0.25, 0.25, 0.125, 0.125, 0.125];
			~levelenvelope=[0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0];
			~levelsValues=[1.0, 0.0];
			~listeWindows=[];
			~indexwindow=0;//control panel = position 0 dans la liste
			~compteurAnalyse=0;
			~listefreq=[];
			~listeamp=[];
			~listeduree=[];
			~listeID=[];
			~routineMusic=[];
			~listeIDTdefAgents=[];
			~userOSchoice=[
				"User Operating System",
				"Load File for Analyse",
				"Load Control	",
				"Load Preset",
				"Load Preset+Genome (Not Operate)",
				"Load Preset+Genome+Sequence (Not Operate)",
				"Load Genome",
				"Load Sequence",
				"Save Control	",
				"Save Preset",
				"Save Preset+Genome (Not Operate)",
				"Save Preset+Genome+Sequence (Not Operate)",
				"Save Genome",
				"Save Sequence",
				"New Synth Environment	",
				"New Sound Environment",
				"New FX Environment",
				"New Synth, Sounds, FX, Path for Data Environment",
				"OSC Off",
				"OSC Master",
				"OSC Slave",
				"OSC Setting",
				"MIDI -> Out On",
				"MIDI -> Out Off",
				"Add a New Agent",
			];

			// Monde virtuel
			~mx=200;
			~my=200;
			~point=10;
			~matrix=[1, 0, 0, 1, 0, -25];//coordonnees systeme
			~angleX=22 / 360 * 2pi;
			~angleY=45 / 360 * 2pi;
			~angleZ=16 / 360 * 2pi;
			~focalDistance=150;
			// Set routine et flags pour tore no tore flock no flock
			~flagTore='off';
			~flagFlock='off';
			~flagSharedMusic='off';
			~flagElitiste='off';

			// Agents
			~flagloop='off';
			~vies=[];
			~ages=[];
			~enfants=[];
			~fitnessInne=[];
			~fitnessAcquis=[];
			~listeagentfreq=[];
			~listeagentamp=[];
			~listeagentduree=[];
			~listeagentID=[];
			~genomes=[];
			~agentspositionx=[];
			~agentspositiony=[];
			~agentspositionz=[];
			~dureesmusique=[];
			~freqMidi=[];
			~flagCompteurPlayingAgents=[];
			~voisins=[];
			~signaux=[];
			~voisinsAffichage=[];
			~signauxAffichage=[];
			~couleurs=[];
			~fitnessAcquisvoisins=[];
			~voisinsOkCopulation=[];
			~flagplayagent=[];
			~compteur=[];
			~bufferDataAgents=[];
			~chordMaxAgents=[];
			~chordDurAgents=[];
			~dureeNextAccouplement=[];
			~liensParents=[];
			// Flag on/off genes Synth-Agents
			~flagGeneFreq='off';
			~flagGeneTransFreq='off';
			~flagGeneAmp='off';
			~flagGeneDuree='off';
			~flagGeneMulDuree='off';
			~flagGenePan='off';
			~flagGeneBuffer='off';
			~flagGeneSample='off';
			~flagGeneReverse='off';
			~flagGeneLoop='off';
			~flagGeneOffset='off';
			~flagGeneEnvLevel='off';
			~flagGeneEnvDuree='off';
			~flagGeneSynth='off';
			~flagGeneControl='off';
			~flagGeneOut='off';
			~flagGeneMidi='off';
			~flagGeneInput='off';
			~flagGeneLoopMusic='off';
			~flagGeneBufferMusic='off';
			~flagGeneChordMax='off';
			~flagGeneChordDur='off';
			// Flag on/off Automation
			~flagInitAutomation='off';
			~flagUniversAutomation='off';
			~flagMondesAgentsAutomation='off';
			~flagMondesMusiqueAutomation='off';
			~flagGenesSAutomation='off';
			~flagGenesMAutomation='off';
			~flagSynthAgentsAutomation='off';
			~flagSynthMusiqueAutomation='off';
			~flagRootAutomation='off';
			~flagAS='SoundIn';
			~antiClick=[0.5, 0.5];
			~geneSamplePopUpLow=0;
			~geneSamplePopUpHigh=~displaySons.size - 1;
			~geneSynthPopUpLow=0;
			~geneSynthPopUpHigh=~listSynth.size - 1;
			~geneInputPopUpLow=0;
			~geneInputPopUpHigh=~audioInLR.size - 1;
			~flagAmpSynth = 'off';
			~flagMidiOut = 'off';
			~kohonenF = [];
			~kohonenA = [];
			~kohonenD = [];
			~algoMusic = "Default";
			~flagGeneAlgorithm='off';
			~listeAlgorithm = ["Default", "Probability", "Euclide", "Genetic", "Kohonen", "Neural"];
			~geneticF = [];
			~geneticA = [];
			~geneticD = [];
			~neuralFAD = [];
			~agentsBand = [];
			//FHZ Band System
			~numFhzBand = 3; // Nombre de band de fhz (+1 pour all data) pour trier dans les synth index=0 pour all index=1 pour badnnum 1 etc...
			~bandFHZ = Array.fill(~numFhzBand, {arg i; [127 / ~numFhzBand * i, 127 / ~numFhzBand * i + (127 / ~numFhzBand )]});
			~bandFHZ = ~bandFHZ.reverse;
			~bandFHZ = ~bandFHZ.add([0, 127]);
			~bandFHZ = ~bandFHZ.reverse;
			~flagGeneBand = 'off';
			~flagSynthBand = 'off';
			~flagBandGenes = [1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0];
			~flagBandSynth = [1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0];
			~rangeSynthBand = [0, 1, 2, 3]; // Band active

			// Keyboard
			keyboardTranslateBefore = 0;
			keyVolume = 12.neg.dbamp;

			// run the soft
			this.run;

		});

	}

	run {

		//s.bind{

		~initAllSound={arg path, file;
			// Load les files sons
			if(File.exists(path++file), {file=File(path++file,"r");~sounds=file.readAllString.interpret.soloArray;file.close},
				{~sounds=
					[
						//Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff"
						// Array Voice Sound
						Platform.resourceDir  +/+ "sounds/Voice/voix.aiff",
						Platform.resourceDir  +/+ "sounds/Voice/Choeur.aiff",
						// Array String Sound
						Platform.resourceDir  +/+ "sounds/String/Contrebasse1.wav",
						Platform.resourceDir  +/+ "sounds/String/Contrebasse2.wav",
						Platform.resourceDir  +/+ "sounds/String/Violoncelle1.wav",
						Platform.resourceDir  +/+ "sounds/String/Alto1.wav",
						Platform.resourceDir  +/+ "sounds/String/Alto2.wav",
						Platform.resourceDir  +/+ "sounds/String/Violon1.wav",
						Platform.resourceDir  +/+ "sounds/String/cordes.aiff",
						Platform.resourceDir  +/+ "sounds/String/String1.wav",
						Platform.resourceDir  +/+ "sounds/String/String2.wav",
						Platform.resourceDir  +/+ "sounds/String/String3.wav",
						Platform.resourceDir  +/+ "sounds/String/ContrebasseST1.wav",
						Platform.resourceDir  +/+ "sounds/String/AltoST1.wav",
						Platform.resourceDir  +/+ "sounds/String/ViolonST1.wav",
						Platform.resourceDir  +/+ "sounds/String/StringST1.wav",
						Platform.resourceDir  +/+ "sounds/String/AltoST1.wav",
						Platform.resourceDir  +/+ "sounds/String/ViolonST1.wav",
						Platform.resourceDir  +/+ "sounds/String/ContrebassePizz1.wav",
						Platform.resourceDir  +/+ "sounds/String/ContrebassePizz2.wav",
						Platform.resourceDir  +/+ "sounds/String/pizzacato.aiff",
						Platform.resourceDir  +/+ "sounds/String/ViolonPizz1.wav",
						// Array Woodwind Sound
						Platform.resourceDir  +/+ "sounds/Woodwind/clarinette basse.aiff",
						Platform.resourceDir  +/+ "sounds/Woodwind/clarinette.aiff",
						Platform.resourceDir  +/+ "sounds/Woodwind/hautbois.aiff",
						Platform.resourceDir  +/+ "sounds/Woodwind/flute.aiff",
						// Array Brass Sound
						Platform.resourceDir  +/+ "sounds/Brass/Tuba Sustain P.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Tuba Sustain Soft.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Tuba Sustain MF.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Tuba Sustain F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Trombone Sustain MF.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Trombone Sustain F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Cor Sustain MF.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Cor Sustain F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/French Horn Sustain MF.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/French Horn Sustain F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Tuba Attack F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Trombone Attack F.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/Trombone Attack FFF.aiff",
						Platform.resourceDir  +/+ "sounds/Brass/French Horn Attack F.aiff",
						// Array Keyboard Sound
						Platform.resourceDir  +/+ "sounds/Keyboard/piano m.aiff",
						Platform.resourceDir  +/+ "sounds/Keyboard/piano f.aiff",
						Platform.resourceDir  +/+ "sounds/Keyboard/fender rhode.aiff",
						Platform.resourceDir  +/+ "sounds/Keyboard/glockenspiel.aiff",
						// Array Percussion Sound
						Platform.resourceDir  +/+ "sounds/Percussion/HPbassdrum.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/HPsnare.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/HPsnareRoll.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/HPrimshot.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/tom.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/tomHigh.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/HPhihat.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/CymbaleLow.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/bloc chinois.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/tambourin.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/crapeau.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/crecelle.aiff",
						Platform.resourceDir  +/+ "sounds/Percussion/batonDePluie.aiff",
						// Array Special Sound
						Platform.resourceDir  +/+ "sounds/Special/MicroSound1.aiff",
						Platform.resourceDir  +/+ "sounds/Special/MicroSine.aiff",
						Platform.resourceDir  +/+ "sounds/Special/MicroSaw.aiff",
						Platform.resourceDir  +/+ "sounds/Special/MicroSquare.aiff",
						Platform.resourceDir  +/+ "sounds/Special/MicroNoise.aiff",
						Platform.resourceDir  +/+ "sounds/Special/Pluck.aiff",
						Platform.resourceDir  +/+ "sounds/Special/RissetPercu.aiff",
						Platform.resourceDir  +/+ "sounds/Special/CordeMetal.aiff",
						Platform.resourceDir  +/+ "sounds/Special/Roach.wav",
						Platform.resourceDir  +/+ "sounds/Special/Synth 1.wav",
						Platform.resourceDir  +/+ "sounds/Special/Synth 2.wav",
						// Free Buffer different time
						Platform.resourceDir  +/+ "sounds/Buffer/100milliseconde.wav",
						Platform.resourceDir  +/+ "sounds/Buffer/64samples.wav",
						Platform.resourceDir  +/+ "sounds/Buffer/128samples.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/256samples.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/512samples.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/1024samples.wav",
						Platform.resourceDir  +/+ "sounds/Buffer/2048samples.wav",
						Platform.resourceDir  +/+ "sounds/Buffer/4096samples.wav",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:2.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:4.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:8.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:16.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:32.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample1:64.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample2.aiff",
						Platform.resourceDir  +/+ "sounds/Buffer/sample4.aiff"
					];
					file=File(~nompathdata++"List Sounds.scd","w");file.write("~sounds="++~sounds.asCompileString);file.close});
		};
		~initAllSound.value(~nompathdata, "List Sounds.scd");

		~initAllSynth={arg path, file;
			if(File.exists(path++file), {file=File(path++file,"r");~listSynth=file.readAllString.interpret;file.close},
				{~listSynth=[
					// Sampler
					"PlayBuf",
					"PlayBuf2",
					"BufRd",
					"BufRd2",
					"TGrains",
					"LoopBuf",
					"GrainBuf",
					//"SamplerSynth",
					"HPplayBuf",
					"HPplayBufVibrato",
					"HPplayBuf2",
					"HPbufRd",
					"HPbufRd2",
					"HPtGrains",
					"HPplayBufMedianLeakDC",
					"SampleResonz",
					"Synthesizer",
					"FreqShift",
					"PitchShift",
					"PlayBufSquiz",
					"WaveLoss",
					"Warp0",
					"Warp1",
					"Granulation1",
					"Granulation2",
					"Toupie",
					"Elastique",
					"RandElastique",
					"RandKlankSample",
					"DjScratch",
					"LiquidFilter",
					"PV_HPshiftDown",
					//"PV_HPecartType",
					"PV_MagNoise",
					"PV_MagClip",
					"PV_MagSmooth",
					"PV_MagSmear",
					"PV_Diffuser",
					"PV_BrickWall",
					"PV_LocalMax",
					"PV_MagSquared",
					"PV_MagBelow",
					"PV_MagAbove",
					"PV_RandComb",
					"PV_MagShift",
					"PV_BinScramble",
					"PV_BinShift",
					"PV_RectComb",
					"PV_ConformalMap",
					"PV_Compander",
					"PV_SpectralEnhance",
					"PV_MagStretch",
					"PV_MagShift+Stretch",
					"PV_Cutoff",
					"PV_Max",
					"PV_Min",
					"PV_Add",
					"PV_Mul",
					"PV_MagDiv",
					"PV_CopyPhase",
					"PV_RandWipe",
					"PV_BinWipe",
					"PV_RectComb2",
					"PV_Morph",
					"Convolution",

					// Synth
					"SinOsc",
					"SinOscVibrato",
					"FMsynth",
					"SawSynth",
					"Formant",
					"Guitare",
					"Blip",
					/*"Osc",
					"VOsc",
					"VOsc3",*/
					"VarSaw",
					"Pulse",
					"Klang",
					"Klank",
					"Klank2",
					"Gendy3",
					"Spring",

					// Piano
					"MdaPiano",
					"Piano Resonz",
					"Piano Synthesizer",
					"Piano FreqShift",
					"Piano PitchShift",
					"Piano Squiz",
					"Piano WaveLoss",
					"Piano PV_HPshiftDown",
					//"Piano PV_HPecartType",
					"Piano PV_MagNoise",
					"Piano PV_MagClip",
					"Piano PV_MagSmooth",
					"Piano PV_MagSmear",
					"Piano PV_Diffuser",
					"Piano PV_BrickWall",
					"Piano PV_LocalMax",
					"Piano PV_MagSquared",
					"Piano PV_MagBelow",
					"Piano PV_MagAbove",
					"Piano PV_RandComb",
					"Piano PV_MagShift",
					"Piano PV_BinScramble",
					"Piano PV_BinShift",
					"Piano PV_RectComb",
					"Piano PV_ConformalMap",
					"Piano PV_Compander",
					"Piano PV_Max",
					"Piano PV_Min",
					"Piano PV_Add",
					"Piano PV_Mul",
					"Piano PV_MagDiv",
					"Piano PV_CopyPhase",
					"Piano PV_RandWipe",
					"Piano PV_BinWipe",
					"Piano PV_RectComb2",
					"Piano PV_Morph",
					"Piano Convolution",

					// OnFly
					"SynthOnFly",
				];
				file=File(~nompathdata++"List Synth.scd","w");file.write("~listSynth="++~listSynth.asCompileString);file.close});
			~listSynth = ~listSynth ++ ~listSynthAdd;
			~audioOutSynth=[];
			~controlsSynth=[];
			~valueJitterControlsSynth = 0.1;
			~valueRandomControlsSynth = 0;
			~automationControlsSynth=[];
			~automationJitterControlsSynth=[];
			~controlsValues=[0.5, 0.5, 0.5];
			~listSynth.size.do({arg i;
				~audioOutSynth=~audioOutSynth.add(0);
				~controlsSynth=~controlsSynth.add(~controlsValues);
				~automationControlsSynth=~automationControlsSynth.add(0);
				~automationJitterControlsSynth=~automationJitterControlsSynth.add(0.1);
			});
		};
		~initAllSynth.value(~nompathdata, "List Synth.scd");
		~synthInstruments=~listSynth.wrapAt(0);
		// Init liste synth pour affichage special GUI
		~listeNoPlugHP=~listeWithoutSample++[
			"PlayBuf",
			"PlayBuf2",
			"BufRd",
			"BufRd2",
			"TGrains",
			"LoopBuf",
			"GrainBuf",
		];
		~listeWithoutSample=[
			"SinOsc",
			"SinOscVibrato",
			"FMsynth",
			"SawSynth",
			"Formant",
			"Guitare",
			"Blip",
			/*"Osc",
			"VOsc",
			"VOsc3",*/
			"VarSaw",
			"Pulse",
			"Klang",
			"Klank",
			"Klank2",
			"Gendy3",
			"Spring",
			"MdaPiano",
			"Piano Resonz",
			"Piano Synthesizer",
			"Piano FreqShift",
			"Piano PitchShift",
			"Piano Squiz",
			"Piano WaveLoss",
			"Piano PV_HPshiftDown",
			//"Piano PV_HPecartType",
			"Piano PV_MagNoise",
			"Piano PV_MagClip",
			"Piano PV_MagSmooth",
			"Piano PV_MagSmear",
			"Piano PV_Diffuser",
			"Piano PV_BrickWall",
			"Piano PV_LocalMax",
			"Piano PV_MagSquared",
			"Piano PV_MagBelow",
			"Piano PV_MagAbove",
			"Piano PV_RandComb",
			"Piano PV_MagShift",
			"Piano PV_BinScramble",
			"Piano PV_BinShift",
			"Piano PV_RectComb",
			"Piano PV_ConformalMap",
			"Piano PV_Compander",
		];
		~listeWith1Sample=[
			"PlayBuf",
			"PlayBuf2",
			"BufRd",
			"BufRd2",
			"TGrains",
			"LoopBuf",
			"GrainBuf",
			//"SamplerSynth",
			"HPplayBuf",
			"HPplayBufVibrato",
			"HPplayBuf2",
			"HPbufRd",
			"HPbufRd2",
			"HPtGrains",
			"HPplayBufMedianLeakDC",
			"SampleResonz",
			"Synthesizer",
			"FreqShift",
			"PitchShift",
			"PlayBufSquiz",
			"WaveLoss",
			"Warp0",
			"Warp1",
			"Granulation1",
			"Granulation2",
			"Toupie",
			"Elastique",
			"RandElastique",
			"RandKlankSample",
			"DjScratch",
			"LiquidFilter",
			"PV_HPshiftDown",
			//"PV_HPecartType",
			"PV_MagNoise",
			"PV_MagClip",
			"PV_MagSmooth",
			"PV_MagSmear",
			"PV_Diffuser",
			"PV_BrickWall",
			"PV_LocalMax",
			"PV_MagSquared",
			"PV_MagBelow",
			"PV_MagAbove",
			"PV_RandComb",
			"PV_MagShift",
			"PV_BinScramble",
			"PV_BinShift",
			"PV_RectComb",
			"PV_ConformalMap",
			"PV_Compander",
			"PV_SpectralEnhance",
			"PV_MagStretch",
			"PV_MagShift+Stretch",
			"PV_Cutoff",
			"Piano PV_Max",
			"Piano PV_Min",
			"Piano PV_Add",
			"Piano PV_Mul",
			"Piano PV_MagDiv",
			"Piano PV_CopyPhase",
			"Piano PV_RandWipe",
			"Piano PV_BinWipe",
			"Piano PV_RectComb2",
			"Piano PV_Morph",
			"Piano Convolution",
		];
		~listeWith2Sample=[
			"PV_Max",
			"PV_Min",
			"PV_Add",
			"PV_Mul",
			"PV_MagDiv",
			"PV_CopyPhase",
			"PV_RandWipe",
			"PV_BinWipe",
			"PV_RectComb2",
			"PV_Morph",
			"Convolution",
		];

		~initAllEffet={arg path, file;
			if(File.exists(path++file), {file=File(path++file,"r");~listEffets=file.readAllString.interpret;file.close},
				{~listEffets=[
					"DelayC",
					"CombC",
					"MidEQ",
					"BPF",
					"BRF",
					"RHPF",
					"RLPF",
					"Ringz",
					"Formlet",
					"Resonz",
					"TwoPole",
					"FOS",
					"Median",
					"LeakDC",
					"Median+LeakDC",
					"DynKlank",
					"PitchShiftFX",
					"LivePlayBuf",
					"LiveWarp",
					"WarpDelay",
					"DJ_FX",
					"PV_MagFreeze",
					"PV_PlayBuf",
					"PV_BinPlayBuf",
					"PV_HPshiftDownFX",
					//"PV_HPecartTypeFX",
					"PV_HPfiltreFX",
					"PV_MagNoiseFX",
					"PV_MagClipFX",
					"PV_MagSmoothFX",
					"PV_MagSmearFX",
					"PV_DiffuserFX",
					"PV_BrickWallFX",
					"PV_LocalMaxFX",
					"PV_MagSquaredFX",
					"PV_MagBelowFX",
					"PV_MagAboveFX",
					"PV_RandCombFX",
					"PV_MagShiftFX",
					"PV_BinScrambleFX",
					"PV_BinShiftFX",
					"PV_RectCombFX",
					"PV_ConformalMapFX",
					"PV_CompanderFX",
					"PV_SpectralEnhanceFX",
					"PV_MagStretchFX",
					"PV_MagShift+StretchFX",
					"PV_CutoffFX",
					"ConvolutionFX",
					"FXonFly",
				];
				file=File(~nompathdata++"List FX.scd","w");file.write("~listEffets="++~listEffets.asCompileString);file.close});
			~listEffets = ~listEffets ++ ~listFXAdd;
		};
		~initAllEffet.value(~nompathdata, "List FX.scd");

		~initAllVerb={arg path, file;
			if(File.exists(path++file), {file=File(path++file,"r");~listVerb=file.readAllString.interpret;file.close},
				{~listVerb=[
					"AllpassC",
					"FreeVerb",
					"GVerb",
					"SpinReverb",
					"JPverb",
				];
				file=File(~nompathdata++"List Verb.scd","w");file.write("~listVerb="++~listVerb.asCompileString);file.close});
		};
		~initAllVerb.value(~nompathdata, "List Verb.scd");

		// Buffer sons et autres
		~groupeAnalyse=Group.new(s, \addToTail);
		~groupeBuffer=Group.new(s, \addToTail);
		~groupeSynthRecAgents=Group.new(s, \addToTail);
		~groupeSynthAgents=Group.new(s, \addToTail);
		~groupeEffets=Group.new(s, \addToTail);
		~groupeVerb=Group.new(s, \addToTail);
		~groupeMasterFX=Group.new(s, \addToTail);
		~flagFreqSamples='off';
		~busFileIn=Bus.audio(s, 1);
		~busEffetsAudio=Bus.audio(s, 2);
		~busVerbAudio=Bus.audio(s, 2);
		s.sync;
		//Buffer file analyse
		~bufferAudioAgents=[];
		~bufferFileAgents=[];
		~synthRecAudioAgents=[];
		~synthRecFileAgents=[];
		if(~samplePourAnalyse != nil, {
			~file = SoundFile.new;
			s.sync;
			~file.openRead(~samplePourAnalyse);
			s.sync;
			if(~file.numChannels == 1,
				{~rawData= FloatArray.newClear(~file.numFrames * 2);
					s.sync;
					~file.readData(~rawData);
					s.sync;
					Post << "Loading sound for analyze" << " " << ~samplePourAnalyse << Char.nl;
					s.sync;
					~rawData = Array.newFrom(~rawData).stutter(2) / 2;
					s.sync;
					~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2, action: {arg buf; Post << "Finished" << Char.nl});
					s.sync;
				},
				{Post << "Loading sound for analyze" << " " << ~samplePourAnalyse << Char.nl;
					s.sync;
					~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
					s.sync;
			});
			//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
			~file.close;
		});
		s.sync;

		// Synth sample pour agents
		~initAllBuffer={arg file;
			var rawData;
			// Init sons
			~recSamples=[];
			~recFiles=[];
			~displaySons=[];
			~bufferSons=[];
			~recSamplesButtonSons=[];
			~recSamplesLoopSons=[];
			~recSamplesLevelsSons=[];
			~flagFreqSamplesSons=[];
			~loopSynthSons=[];
			~posSamplesSons=[];
			~reverseSynthSons=[];
			~audioInputSons=[];
			~sounds.size.do({arg i;
				~displaySons=~displaySons.add(PathName(~sounds.wrapAt(i)).fileName);//Set son affichage
				s.sync;
				file = SoundFile.new;
				s.sync;
				file.openRead(~sounds.wrapAt(i).standardizePath);
				s.sync;
				if(file.numChannels == 1,
					{Post << "Loading mono sound" << " " << ~sounds.wrapAt(i).standardizePath << Char.nl;
						~bufferSons=~bufferSons.add(Buffer.read(s, ~sounds.wrapAt(i).standardizePath, action: {Post << "Finished" << Char.nl}));
						s.sync;
					},
					{rawData= FloatArray.newClear(file.numFrames * 2);
						s.sync;
						file.readData(rawData);
						s.sync;
						rawData = Array.newFrom(rawData);
						s.sync;
						Post << "Loading stereo sound" << " " << ~sounds.wrapAt(i).standardizePath << Char.nl;
						s.sync;
						rawData = rawData.unlace(2).sum / 2;
						s.sync;
						~bufferSons=~bufferSons.add(Buffer.loadCollection(s, rawData, 1, {Post << "Finished" << Char.nl}));
						s.sync;
				});
				//~bufferSons.wrapPut(i, ~bufferSons.wrapAt(i).normalize(1.0));
				file.close;
				s.sync;
				~recSamplesButtonSons=~recSamplesButtonSons.add(0);
				~recSamplesLoopSons=~recSamplesLoopSons.add(0);
				~recSamplesLevelsSons=~recSamplesLevelsSons.add([1, 0]);
				~flagFreqSamplesSons=~flagFreqSamplesSons.add('off');
				~loopSynthSons=~loopSynthSons.add(0);
				~posSamplesSons=~posSamplesSons.add(0);
				~reverseSynthSons=~reverseSynthSons.add(0);
				~audioInputSons=~audioInputSons.add(0);
				~recSamples=~recSamples.add(Synth.newPaused("RecSampleIn",[\in, 0, 'buffer',~bufferSons.wrapAt(i).bufnum,'run',0,'loop',0],~groupeBuffer, \addToTail));
				s.sync;
				//File IN
				~recFiles=~recFiles.add(Synth.newPaused("RecFileIn",['buffer',~bufferSons.wrapAt(i).bufnum, 'in', ~busFileIn.index , 'run',0,'loop',0],~groupeBuffer, \addToTail));
			});
			s.sync;
			"Please Wait... Loading Agents... Sending SynthDef on Server... Loading Sounds...".postln;
		};
		~initAllBuffer.value;
		~listesamplein=~recSamples;//Init sample avec audioIn

		~bufferSonsInstruments=~bufferSons.wrapAt(0);// Init son pour debut
		~soundsPositions=0;

		~initSynthEffets={
			// Effets
			~listSynthEffets=[];
			~audioOutEffets=[];
			~playSynthEffets=[];
			~controlsSynthEffets=[];
			~panSynthEffets=[];
			~jitterPanSynthEffets=[];
			~jitterControlsSynthEffets=[];
			~ampSynthEffets=[];
			~automationPanEffets=[];
			~automationControlsEffets=[];
			~automationSpeedEffets = [];
			~listeFXTime = [];
			~listEffets.size.do({arg i;
				~listSynthEffets=~listSynthEffets.add(Synth.newPaused(~listEffets.wrapAt(i).asString,['in', ~busEffetsAudio.index, 'busverb', ~busVerbAudio.index, 'amp', 12.neg.dbamp, 'pan', 0.0, 'control1', 0.5,  'control2', 0.5,  'control3', 0.5,  'control4', 0.5,  'control5', 0.5,  'control6', 0.5,  'control7', 0.5,  'control8', 0.5], ~groupeEffets, \addToTail));
				s.sync;
				~audioOutEffets=~audioOutEffets.add(0);
				~controlsSynthEffets=~controlsSynthEffets.add([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
				~playSynthEffets=~playSynthEffets.add(0);
				~panSynthEffets=~panSynthEffets.add(0.0);
				~jitterPanSynthEffets=~jitterPanSynthEffets.add(0.1);
				~jitterControlsSynthEffets=~jitterControlsSynthEffets.add(0.1);
				~ampSynthEffets=~ampSynthEffets.add(-3);
				~automationPanEffets=~automationPanEffets.add(0);
				~automationControlsEffets=~automationControlsEffets.add(0);
				~automationSpeedEffets = ~automationSpeedEffets.add(24);
				~listeFXTime = ~listeFXTime.add(24.reciprocal);
			});
		};
		~initSynthEffets.value;

		~initSynthVerb={
			// Verb
			~listSynthVerb=[];
			~audioOutVerb=[];
			~playSynthVerb=[];
			~controlsSynthVerb=[];
			~panSynthVerb=[];
			~jitterPanSynthVerb=[];
			~jitterControlsSynthVerb=[];
			~ampSynthVerb=[];
			~automationPanVerb=[];
			~automationControlsVerb=[];
			~automationSpeedVerb = [];
			~listeVerbTime = [];
			~listVerb.size.do({arg i;
				~listSynthVerb=~listSynthVerb.add(Synth.newPaused(~listVerb.wrapAt(i).asString,['in', ~busVerbAudio.index, 'amp', 12.neg.dbamp, 'pan', 0.0, 'control1', 0.5,  'control2', 0.5,  'control3', 0.5,  'control4', 0.5,  'control5', 0.5,  'control6', 0.5,  'control7', 0.5,  'control8', 0.5], ~groupeVerb, \addToTail));
				s.sync;
				~audioOutVerb=~audioOutVerb.add(0);
				~controlsSynthVerb=~controlsSynthVerb.add([0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]);
				~playSynthVerb=~playSynthVerb.add(0);
				~panSynthVerb=~panSynthVerb.add(0.0);
				~jitterPanSynthVerb=~jitterPanSynthVerb.add(0.1);
				~jitterControlsSynthVerb=~jitterControlsSynthVerb.add(0.1);
				~ampSynthVerb=~ampSynthVerb.add(-3);
				~automationPanVerb=~automationPanVerb.add(0);
				~automationControlsVerb=~automationControlsVerb.add(0);
				~automationSpeedVerb=~automationSpeedVerb.add(24);
				~listeVerbTime = ~listeVerbTime.add(24.reciprocal);
			});
		};
		~initSynthVerb.value;

		// Creation Synth Analyse
		~audioFile=Synth.newPaused("OSC Agents File Onsets",
			['busFileIn', ~busFileIn.index], ~groupeAnalyse, \addToTail);
		s.sync;
		~audioIn=Synth.newPaused("OSC Agents Onsets",
			[\seuil, 0.5 ], ~groupeAnalyse, \addToTail);
		s.sync;
		// Creation Synth Tempo
		~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
			['busFileIn', ~busFileIn.index], ~groupeAnalyse, \addToTail);
		s.sync;
		~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
			[\lock, 0], ~groupeAnalyse, \addToTail);
		s.sync;
		// Creation MasterFX
		~masterFX = Synth.new("MasterFX", [\limit, 0.8], ~groupeMasterFX, \addToTail);
		s.sync;
		// VST
		~synthVST = Synth.newPaused("VST Plugin", [\xFade, 0.5, \gainIn, 0.5], ~groupeMasterFX, \addToHead);
		~fxVST = VSTPluginController(~synthVST);
		s.sync;
		//};

		// Creation synthDef analyse
		~creationSynthDefAnalyze={arg algo;
			s.bind{
				~audioIn.value.run(false);~tempoIn.value.run(false); ~audioFile.value.run(false);~tempoFile.value.run(false);
				s.sync;
				~audioIn.free; ~audioFile.free;~tempoFile.free;~tempoIn.free;
				s.sync;
				switch(algo,
					0, {~audioFile=Synth.newPaused("OSC Agents File Onsets",
						['busFileIn', ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilInSlider.value], ~groupeAnalyse, \addToTail);
					s.sync;
					~audioIn=Synth.newPaused("OSC Agents Onsets",
						[\seuil, ~seuilInSlider.value, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1)], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
						['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
						[\lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					if(~flagEntreeMode == 'Audio' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					if(~flagEntreeMode == 'File' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.value.run(true);~tempoFile.value.run(true)});
					s.sync;
					},
					1, {~audioFile=Synth.newPaused("OSC Agents File Pitch",
						[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilInSlider.value], ~groupeAnalyse, \addToTail);
					s.sync;
					~audioIn=Synth.newPaused("OSC Agents Pitch",
						[\seuil, ~seuilInSlider.value, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1)], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
						['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
						[\lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					if(~flagEntreeMode == 'Audio' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					s.sync;
					if(~flagEntreeMode == 'File' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.value.run(true);~tempoFile.value.run(true)});
					s.sync;
					},
					2, {~audioFile=Synth.newPaused("OSC Agents File Pitch2",
						[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilInSlider.value], ~groupeAnalyse, \addToTail);
					s.sync;
					~audioIn=Synth.newPaused("OSC Agents Pitch2",
						[\seuil, ~seuilInSlider.value, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1)], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
						['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
						[\lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					if(~flagEntreeMode == 'Audio' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					s.sync;
					if(~flagEntreeMode == 'File' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.value.run(true);~tempoFile.value.run(true)});
					s.sync;
					},
					3, {~audioFile=Synth.newPaused("OSC Agents File KeyTrack",
						[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilInSlider.value], ~groupeAnalyse, \addToTail);
					s.sync;
					~audioIn=Synth.newPaused("OSC Agents KeyTrack",
						[\seuil, ~seuilInSlider.value, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1)], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
						['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
						[\lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					if(~flagEntreeMode == 'Audio' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					s.sync;
					if(~flagEntreeMode == 'File' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.value.run(true);~tempoFile.value.run(true)});
					s.sync;
					},
					4, {~audioFile=Synth.newPaused("OSC Agents File Keyboard",
						['busFileIn', ~busFileIn.index, \trigger, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~audioIn=Synth.newPaused("OSC Agents Keyboard",
						[\trigger, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoFile=Synth.newPaused("OSC Agents Tempo FileIn",
						['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					~tempoIn=Synth.newPaused("OSC Agents Tempo AudioIn",
						[\lock, 0], ~groupeAnalyse, \addToTail);
					s.sync;
					if(~flagEntreeMode == 'Audio' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					s.sync;
					if(~flagEntreeMode == 'File' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.value.run(true);~tempoFile.value.run(true)});
					s.sync;
					}
				);
				~audioIn.setn(\in, ~audioInputAnalyzeButton.value);
				s.sync;
				~tempoIn.setn(\in, ~audioInputAnalyzeButton.value);
				s.sync;
			};
		};

		//Creation Synth play File
		~synthPlayFile=Synth.newPaused("Agents Play File",
			[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index, \volume, 0], ~groupeAnalyse, \addToHead);

		// OSC Setting
		~serverAdresse = s.addr; // Adresse Server -> NetAddr(0.0.0.0, 0)
		~masterAppAddr = NetAddr.localAddr;
		~slaveAppAddr = NetAddr.localAddr;
		~oscStateFlag = 'off';

		// OSCFunc Score
		OSCFunc.newMatching({arg msg, time, addr, recvPort;

			var array, cmd = 'on', number, file, item = 0;

			msg.removeAt(0);
			msg.postcs;

			while({cmd != nil},
				{
					cmd = msg[item].postln;
					if(cmd == 'all' or: {cmd == 'agents'},
						{
							cmd = msg[item+1].postln;
							// Preset
							if(cmd == 'preset',
								{
									number = msg[item+2].asInteger.postln;
									{
										if(File.exists(~nompathdata++"preset"+number.value.asString++".scd"),
											{
												file=File(~nompathdata++"preset"+number.value.asString++".scd","r");
												~loadUnivers.value(file, 'on', 'on');
												~wp.name=~nomFenetre+~algoMusic+"preset"+number.value.asString++".scd";
												file.close;
										});
									}.defer;
							});
							// Stop
							if(cmd == 'stop', {
							{
							~startsysteme.valueAction_(0);
							}.defer;
							});
							// Start
							if(cmd == 'start', {
							{
							~startsysteme.valueAction_(1);
							}.defer;
							});
					});
					item = item + 3;
					cmd = msg[item];
			});
		}, \score, recvPort: NetAddr.langPort);

		// OSC analyse tempo
		~oscTempoIn = OSCFunc.newMatching({arg msg, time, addr, recvPort;
			if(~flagTempoAnalyze == 'on' and: {~oscStateFlag != 'slave'} and: {~entreemode != 'Midi'}, {
				// 0.5 de marge pour changer le tempo
				if((~tempoMusic / 60 - msg.wrapAt(3)).abs >= 0.5, {
					~tempoMusic=msg.wrapAt(3) * 60;
					~tempoMusic = ~tempoMusic.ceil;
					{~tempoSlider.valueAction = ~tempoMusic}.defer;
				});
		})}, '/Agents_Analyse_Tempo', ~serverAdresse);
		// Fonction OSC
		~initOSCresponder = {
			~oscHPtempo = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~flagTempoAnalyze == 'on' and: {~oscStateFlag == 'slave'} and: {~entreemode != 'Midi'}, {
					{~tempoSlider.valueAction = msg.wrapAt(1)}.defer;
			})}, '/HPtempo', ~masterAppAddr);
			// OSC synchroStart slave
			~oscHPstart = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{~startsysteme.valueAction = msg.wrapAt(1)}.defer;
			})}, '/HPstart', ~masterAppAddr);
			// OSC synchroBare slave
			~oscHPbare = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{~nombreBeats.valueAction = msg.wrapAt(1)}.defer;
			})}, '/HPbare', ~masterAppAddr);
			// OSC synchroRec slave
			~oscHPrec = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{
						if(msg.wrapAt(1) == 'Rec On', {~fonctionRecOn.value});// Recording On
						if(msg.wrapAt(1) == 'Rec Off', {~fonctionRecOff.value});// Recording Off
						if(msg.wrapAt(1) == 'Rec Pause', {~fonctionRecPause.value});// Recording Pause
					}.defer;
			})}, '/HPrec', ~masterAppAddr);
		};
		~initOSCresponder.value;// Init OSC Responder

		~lastTimeAnalyse=Main.elapsedTime;// Init time analyse
		~freqBefore=0; ~ampBefore=0; ~dureeBefore=0; ~freqTampon=nil; ~amptampon=nil;~freqCentroid=0;~flatness=0;~energy=0; ~flux=0;

		// Analyse AudioIn
		~oscAudioIn = OSCFunc.newMatching({arg msg, time, addr, recvPort;
			var freq=0, amp=0, duree=0;
			if(msg.wrapAt(2) == 6 and: {~entreemode == 'Audio' or: {~entreemode == 'File'}},
				{
					duree = time - ~lastTimeAnalyse;
					freq=msg.wrapAt(3);
					freq=freq.clip(0,1);
					amp=msg.wrapAt(4).clip(0.001, 1.0);
					// Info spectral sur le son
					~freqCentroid = msg.wrapAt(5);
					~flatness = msg.wrapAt(6);
					~energy = msg.wrapAt(7);
					~flux = msg.wrapAt(8);
					if(~dataFFTanalyze.wrapAt(0).size >= ~listedatasizein, {~dataFFTanalyze = [[], []]});
					~dataFFTanalyze.put(0, ~dataFFTanalyze.wrapAt(0).add(~flux));
					~dataFFTanalyze.put(1, ~dataFFTanalyze.wrapAt(1).add(~flatness));
					if(duree > ~dureeanalysemax,
						{
							~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~compteurAnalyse=0;
							~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,
								{
									~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset});
							});
							~lastTimeAnalyse = time; ~freqBefore=0; ~ampBefore=0; ~dureeBefore=0; ~freqTampon=nil; ~amptampon=nil;
					});
					if(~flagAlgoAnalyze.value != 4, {
						if(abs(freq*127 - (~freqBefore*127)) >= ~differencefreq and: {abs(amp.ampdb - ~ampBefore.ampdb) >= ~differenceamp} and: {abs(duree - ~lastTimeAnalyse) >= ~differenceduree} and: {duree >= ~differenceduree},
							{if(~freqTampon !=nil and: {~ampTampon != nil},
								{if(~listefreq.size <= ~listedatasizein,
									{~listefreq=~listefreq.add(~freqTampon);~listeamp=~listeamp.add		(~ampTampon);~listeduree=~listeduree.add(duree);~listeID=~listeID.add(1.0.rand);
										~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
									},
									{
										if(~compteurAnalyse >= ~listedatasizein, {~compteurAnalyse=0;
											~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})});
										});
										//if(~compteurAnalyse < ~listefreq.size,
										//{
										~listefreq.wrapPut(~compteurAnalyse,~freqTampon);
										~listeamp.wrapPut(~compteurAnalyse,~ampTampon);
										~listeduree.wrapPut(~compteurAnalyse,duree);
										~listeID.wrapPut(~compteurAnalyse, 1.0.rand);
										~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
										//});
										~compteurAnalyse=~compteurAnalyse+1});
							});
							~freqTampon=freq;~ampTampon=amp;~lastTimeAnalyse=time});
					},
					{
						if(~freqTampon !=nil and: {~ampTampon != nil},
							{if(~listefreq.size <= ~listedatasizein,
								{~listefreq=~listefreq.add(~freqTampon);~listeamp=~listeamp.add		(~ampTampon);~listeduree=~listeduree.add(duree);~listeID=~listeID.add(1.0.rand);
									~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
								},
								{
									if(~compteurAnalyse >= ~listedatasizein, {~compteurAnalyse=0;
										~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})});
									});
									//if(~compteurAnalyse < ~listefreq.size,
									//{
									~listefreq.wrapPut(~compteurAnalyse,~freqTampon);
									~listeamp.wrapPut(~compteurAnalyse,~ampTampon);
									~listeduree.wrapPut(~compteurAnalyse,duree);
									~listeID.wrapPut(~compteurAnalyse, 1.0.rand);
									~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
									//});
									~compteurAnalyse=~compteurAnalyse+1});
						});
						~freqTampon=freq;~ampTampon=amp;~lastTimeAnalyse=time});
					{
						// Setup Automation Preset
						~fonctionAutomationPreset.value(~freqCentroid, ~flatness, ~energy, ~flux);
					}.defer;
				},
				{duree=time - ~lastTimeAnalyse;// duree reel
					if(duree > ~tempsmaxsignal , // ici duree silence
						{~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[]; ~lastTimeAnalyse=time;~freqBefore=0;~ampBefore=0;~dureeBefore=0;~freqTampon=nil;~ampTampon=nil;~compteurAnalyse=0;
							~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})}); ~lastTimeAnalyse=time})});
		}, '/Agents_Analyse_Audio', ~serverAdresse);

		// Analyse Midi-IN
		~fonctionOSCMidiIn={
			~oscMidiIn = MIDIdef.noteOn(\midiNoteOn, {arg amp, freq, canal, src;
				var duree=0, time=Main.elapsedTime;
				duree = time - ~lastTimeAnalyse;
				// Normalise
				freq=freq/127;
				amp=amp/127;
				amp=amp.clip(0.001, 1.0);
				if(duree > ~dureeanalysemax,
					{~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~compteurAnalyse=0;
						~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})});
						~lastTimeAnalyse=time;~freqBefore=0;~ampBefore=0;~dureeBefore=0;~freqTampon=nil;~ampTampon=nil;
				});
				if(~freqTampon !=nil and: {~ampTampon != nil},
					{if(~listefreq.size <= ~listedatasizein,
						{~listefreq=~listefreq.add(~freqTampon);~listeamp=~listeamp.add		(~ampTampon);~listeduree=~listeduree.add(duree);~listeID=~listeID.add(1.0.rand);
							~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
						},
						{
							if(~compteurAnalyse >= ~listedatasizein, {~compteurAnalyse=0;
								~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})});
							});
							~listefreq.wrapPut(~compteurAnalyse,~freqTampon);
							~listeamp.wrapPut(~compteurAnalyse,~ampTampon);
							~listeduree.wrapPut(~compteurAnalyse,duree);
							~listeID.wrapPut(~compteurAnalyse, 1.0.rand);
							~freqBefore=~freqTampon;~ampBefore=~ampTampon;~dureeBefore=duree;
							~compteurAnalyse=~compteurAnalyse+1});
				});
				~freqTampon=freq;~ampTampon=amp;~lastTimeAnalyse=time;
				if(duree > ~tempsmaxsignal , // ici duree silence
					{~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[]; ~lastTimeAnalyse=time;~freqBefore=0;~ampBefore=0;~dureeBefore=0;~freqTampon=nil;~ampTampon=nil;~compteurAnalyse=0;
						~agents.do({arg agent; ~listeagentID.wrapPut(agent, []);if(~listeagentfreq.wrapAt(agent).size != 0 ,{~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset})}); ~lastTimeAnalyse=time});
			}, (0..127), ~canalMidiIn.asInteger);
			~oscMidiIn;
		};
		~fonctionOSCMidiIn.value;

		// MIDI Control Responder
		MIDIdef.cc(\controls, {arg val, num, chan, src;
			var controls;
			{
				controls = ~controlsSynthMenu.value;
				if(chan == ~canalMidiIn, {
					if(~controlMIDIone == num, {controls.put(0, val/127)});
					if(~controlMIDItwo == num, {controls.put(1, val/127)});
					if(~controlMIDIthree == num, {controls.put(2, val/127)});
				});
				~controlsSynthMenu.valueAction_(controls);
			}.defer;
		}, (0..127), ~canalMidiIn.asInteger);

		// Brightness of signal = freqCentroid ( freq en hertz)
		// Entropie du son  = flatness (0 -> sinus | 1 -> whiteNoise)
		// Freq max energie spectral = energy (freq en hertz)
		// Flux spectral = flux (0 - > - de changement | 1 + de changement)
		// Flatness > 0 | Flux <= 1

		// Automation Preset
		~fonctionAutomationPreset = {arg freqCentroid, flatness, energy, flux;
			var meanProbaPresetFlux, meanProbaPresetFlatness, file, number=~lastNumberChoiceConfig, newTime, compteur=0, flagPreset=[], flag, seuil, valuesFlux, valuesFlatness, q1A, medianeA, q3A, ecartqA, ecartsemiqA, q1U, medianeU, q3U, ecartqU, ecartsemiqU, variableTemps;
			newTime = Main.elapsedTime;
			if(~variableChange == "Flux", {variableTemps = flux; seuil = ~valueSynthChange.wrapAt(0)}, {variableTemps = flatness; seuil = ~valueSynthChange.wrapAt(1)});
			meanProbaPresetFlux = flux.log2.abs; meanProbaPresetFlatness = flatness.log2.abs; valuesFlux = ~dataFFTanalyze.wrapAt(0).log2.abs; valuesFlatness = ~dataFFTanalyze.wrapAt(1).log2.abs;
			# q1U, medianeU, q3U, ecartqU, ecartsemiqU = valuesFlux.quartiles;
			# q1A, medianeA, q3A, ecartqA, ecartsemiqA = valuesFlatness.quartiles;
			if(~algoChange == "Probability", {
				if(~startAutomation.value == 1 and: {~universAutomation.value == 1}
					and: {abs(newTime - ~lastTimeAutomationPreset) > ~limitTemps} and: {meanProbaPresetFlux < (q1U - ecartqU) or: {meanProbaPresetFlux > (q3U + ecartqU)}} and: {meanProbaPresetFlatness < (q1A - ecartqA) or: {meanProbaPresetFlatness > (q3A + ecartqA)}}, {
						while({number == ~lastNumberChoiceConfig and: {compteur <= (~choixChangeConfig.wrapAt(1)-~choixChangeConfig.wrapAt(0) + 1)}
						}, {number = rrand(~choixChangeConfig.wrapAt(0), ~choixChangeConfig.wrapAt(1)); compteur = compteur + 1});
						// Collect all Preset
						~foldersToScanAll = PathName.new(~nompathdata).files.collect{ |path| var file;
							file = path.fileName;
							if(file.find("preset") == 0 or: {file.find("Preset") == 0}, {file});
						};
						~foldersToScanAll = ~foldersToScanAll.reject({arg item; item == nil});
						// Collect preset
						~foldersToScanPreset = ~foldersToScanAll.collect{ |file|
							if(file.find("preset ") == 0 or: {file.find("Preset ") == 0}, {file});
						};
						~lastTimeAutomationPreset = newTime;
						~lastNumberChoiceConfig = number;
						~lastTimePreset = 0;
						~limitTemps = variableTemps.log2.abs * 6 + 6;
						// Choice type Preset
						flagPreset = flagPreset.add(~foldersToScanPreset.size);
						flagPreset = flagPreset.normalizeSum;
						flag = flagPreset.windex;
						// Load Preset
						if(number != nil, {
							if(flag == 0, {if(File.exists(~nompathdata++~foldersToScanPreset.wrapAt(number)), {file=File(~nompathdata++~foldersToScanPreset.wrapAt(number),"r");
								~wp.name=~nomFenetre + ~foldersToScanPreset.wrapAt(number);
								~loadUnivers.value(file);file.close}, {"cancelled".postln})});
						});
				});
			},
			{
				if(~startAutomation.value == 1 and: {~universAutomation.value == 1}
					and: {abs(newTime - ~lastTimeAutomationPreset) > ~limitTemps} and: {abs(meanProbaPresetFlux - ~lastMeanProbaPresetFlux) > seuil and: {abs(meanProbaPresetFlatness - ~lastMeanProbaPresetFlatness) > seuil}}, {
						while({number == ~lastNumberChoiceConfig and: {compteur <= (~choixChangeConfig.wrapAt(1)-~choixChangeConfig.wrapAt(0) + 1)}
						}, {number = rrand(~choixChangeConfig.wrapAt(0), ~choixChangeConfig.wrapAt(1)); compteur = compteur + 1});
						// Collect all Preset
						~foldersToScanAll = PathName.new(~nompathdata).files.collect{ |path| var file;
							file = path.fileName;
							if(file.find("preset") == 0 or: {file.find("Preset") == 0}, {file});
						};
						~foldersToScanAll = ~foldersToScanAll.reject({arg item; item == nil});
						// Collect preset
						~foldersToScanPreset = ~foldersToScanAll.collect{ |file|
							if(file.find("preset ") == 0 or: {file.find("Preset ") == 0}, {file});
						};
						~lastTimeAutomationPreset = newTime;
						~lastNumberChoiceConfig = number;
						~lastTimePreset = 0;
						~limitTemps = variableTemps.log2.abs * 6 + 6;
						// Choice type Preset
						flagPreset = flagPreset.add(~foldersToScanPreset.size);
						flagPreset = flagPreset.normalizeSum;
						flag = flagPreset.windex;
						// Load Preset
						if(number != nil, {
							if(flag == 0, {if(File.exists(~nompathdata++~foldersToScanPreset.wrapAt(number)), {file=File(~nompathdata++~foldersToScanPreset.wrapAt(number),"r");
								~wp.name=~nomFenetre + ~foldersToScanPreset.wrapAt(number);
								~loadUnivers.value(file);file.close}, {"cancelled".postln})});
						});
				});
			});
			~lastMeanProbaPresetFlux = meanProbaPresetFlux;
			~lastMeanProbaPresetFlatness = meanProbaPresetFlatness;
		};

		// Create all GUI Panel
		this.keyboardPanel;
		this.fxPanel;
		this.spacePanel;
		this.synthPanel;
		this.genesPanel;
		this.automationPanel;
		this.effetsPanel;
		this.verbPanel;
		this.controlPanel;
		this.shortCuts;// Raccourcis clavier
		this.windowsPanel;// init shortcut font etc...

		~wp.view.children.at(0).focus;
		~wp.front;

		// Init
		~synthBand0.valueAction=1;
		~synthBand1.valueAction=1;
		~synthBand2.valueAction=1;
		~synthBand3.valueAction=1;
		~geneBand0.valueAction=1;
		~geneBand1.valueAction=1;
		~geneBand2.valueAction=1;
		~geneBand3.valueAction=1;
		~numberBand.valueAction = 3;
		~buttonGeneBand.valueAction = 0;
		~buttonSynthBand.valueAction = 0;

		// Setup distance 3D
		~distanceagents=~distanceagents * sqrt(1+1+~dureeanalysemax.squared);// Init debut
		~distancesignaux=~distancesignaux * sqrt(1+1+~dureeanalysemax.squared);// Init debut
		~deviance=~deviance * sqrt(1+1+~dureeanalysemax.squared);// Init debut

		// Creation Tdef Agents
		~createTdefAgent ={arg agent, idAgent;
			var tdefAgent;
			tdefAgent = Tdef(("MusicAgent"++idAgent.asString).asSymbol, {
				loop({
					if(~listeagentfreq.wrapAt(agent).size != 0 and: {~flagplayagent.wrapAt(agent) != 'off'}, {~agentsmusique.value(agent);~musicOutAgents.wrapPut(agent, 1)},{~musicOutAgents.wrapPut(agent, 0)});
					~dureesmusique.wrapAt(agent).wait;
				});
			});
			tdefAgent.value;
		};

		// init agents ou creation agents
		~initagents={arg agent, genome, fitnessInne, fitnessAcquis, mode='init', listeF, listeA, listeD, x, y, z, papa=0, maman=0;
			var dureeBuffer, sourceIn, envTime, listBand, trigger;
			~vies=~vies.add(~dureeVieAgents);
			~ages=~ages.add('enfant');
			~enfants=~enfants.add(0);
			~dureesmusique=~dureesmusique.add(~quantaMusic.reciprocal);// set duree musicale a 0 pour jouer musique
			~freqMidi=~freqMidi.add([0]);
			~flagCompteurPlayingAgents=~flagCompteurPlayingAgents.add(0);
			~listeagentID=~listeagentID.add([]);
			~voisins=~voisins.add([]);
			~signaux=~signaux.add([]);
			~voisinsAffichage=~voisinsAffichage.add(0);
			~signauxAffichage=~signauxAffichage.add(0);
			~fitnessAcquisvoisins=~fitnessAcquisvoisins.add([]);
			~voisinsOkCopulation=~voisinsOkCopulation.add([]);
			~flagplayagent=~flagplayagent.add('on');
			~compteur=~compteur.add(0);
			~musicOutAgents=~musicOutAgents.add(0);
			~canalMidiOutAgent=~canalMidiOutAgent.add(~canalMidiOut);
			~couleurs=~couleurs.add([0, 0, 1]);//color blue -> jeune
			// For Kohonen
			~kohonenF = ~kohonenF.add(HPclassKohonen.new(1,127,1));
			~kohonenA = ~kohonenA.add(HPclassKohonen.new(1,127,1));
			~kohonenD = ~kohonenD.add(HPclassKohonen.new(1,127,1));
			// For Genetic
			~geneticF = ~geneticF.add(HPclassGenetiques.new(1, 127));
			~geneticA = ~geneticA.add(HPclassGenetiques.new(1, 127));
			~geneticD = ~geneticD.add(HPclassGenetiques.new(1, 127));
			// For Neural
			~neuralFAD = ~neuralFAD.add(HPNeuralNet.new(3, 1, [9], 3));
			// Creation Genome
			if(mode == 'init', {
				genome=[];
				47.do({arg gene; genome=genome.add(~createOrmutationGenesAgents.value(gene))});// fonction create genomes
				envTime = genome.copyRange(26, 33).sort;
				forBy(26, 33, 1 , {arg i; genome.wrapPut(i, envTime.wrapAt(i-26))});
				~genomes=~genomes.add(genome);
				~fitnessInne=~fitnessInne.add(0);
				~fitnessAcquis=~fitnessAcquis.add(0);
				~agentspositionx=~agentspositionx.add(~dureeanalysemax.rand);
				~agentspositiony=~agentspositiony.add(1.0.rand);
				~agentspositionz=~agentspositionz.add(1.0.rand);
				~listeagentfreq=~listeagentfreq.add([]);
				~listeagentamp=~listeagentamp.add([]);
				~listeagentduree=~listeagentduree.add([]);
				~liensParents = ~liensParents.add([ ]);// liens parents famille
			},
			{~genomes=~genomes.add(genome);
				~fitnessInne=~fitnessInne.add(fitnessInne);
				~fitnessAcquis=~fitnessAcquis.add(fitnessAcquis);
				~agentspositionx=~agentspositionx.add((x+~deviance.rand2)%~dureeanalysemax);
				~agentspositiony=~agentspositiony.add((y+~deviance.rand2)%1.0);
				~agentspositionz=~agentspositionz.add((z+~deviance.rand2)%1.0);
				~listeagentfreq=~listeagentfreq.add(listeF);
				~listeagentamp=~listeagentamp.add(listeA);
				~listeagentduree=~listeagentduree.add(listeD);
				~liensParents = ~liensParents.add(~liensParents.wrapAt(papa)++~liensParents.wrapAt(maman)++[~listeIDTdefAgents.wrapAt(papa), ~listeIDTdefAgents.wrapAt(maman)]);// liens parents famille
			});
			if(~flagGeneBufferMusic == 'off', {~bufferDataAgents=~bufferDataAgents.add(~listedatamusiqueagents)}, {~bufferDataAgents=~bufferDataAgents.add((~genomes.wrapAt(agent).wrapAt(42) * 256).floor.clip(1, 256))});
			if(~flagGeneChordMax == 'off', {~chordMaxAgents=~chordMaxAgents.add(~maxaccord)}, {~chordMaxAgents=~chordMaxAgents.add((~genomes.wrapAt(agent).wrapAt(43) * 12).floor.clip(1, 12))});
			if(~flagGeneChordDur == 'off', {~chordDurAgents=~chordDurAgents.add(~dureeaccord)}, {~chordDurAgents=~chordDurAgents.add(~genomes.wrapAt(agent).wrapAt(44))});
			~dureeNextAccouplement=~dureeNextAccouplement.add(Main.elapsedTime);// flag pour duree copulation
			//Init buffer agents
			dureeBuffer = (s.sampleRate * (~genomes.wrapAt(agent).wrapAt(13)*~tempsmaxsignal).max(0.001));
			~bufferAudioAgents=~bufferAudioAgents.add(Buffer.alloc(s, dureeBuffer));
			~bufferFileAgents=~bufferFileAgents.add(Buffer.alloc(s, dureeBuffer));
			//Set dur Trigger
			trigger = exprand(~tempsmaxsignal, ~dureeVieAgents).reciprocal;
			// source Input
			if(~flagGeneInput == 'off', {sourceIn = ~audioInputSons.wrapAt(~soundsPositions)},{sourceIn = ~genomes.wrapAt(agent).wrapAt(40)});
			~synthRecAudioAgents=~synthRecAudioAgents.add(Synth.new("RecBufferAudioIn",[\in, sourceIn, 'buffer',~bufferAudioAgents.wrapAt(agent).bufnum,'run', 1,'loop', 0, \trigger, trigger, \reclevel1, ~levelsValues.wrapAt(0), \reclevel2, ~levelsValues.wrapAt(1)],~groupeSynthRecAgents, \addToTail));
			~synthRecFileAgents=~synthRecFileAgents.add(Synth.new("RecBufferFileIn",['buffer',~bufferFileAgents.wrapAt(agent).bufnum, 'in', ~busFileIn.index, 'run', 1,'loop', 0, \trigger, trigger, \reclevel1, ~levelsValues.wrapAt(0), \reclevel2, ~levelsValues.wrapAt(1)],~groupeSynthRecAgents, \addToTail));
			NodeWatcher.register(~synthRecAudioAgents.wrapAt(agent));
			NodeWatcher.register(~synthRecFileAgents.wrapAt(agent));
			// PROCESSUS MUSIQUE
			~listeIDTdefAgents=~listeIDTdefAgents.add(Main.elapsedTime);
			~routineMusic=~routineMusic.add(~createTdefAgent.value(agent, ~listeIDTdefAgents.wrapAt(agent)));
			{if(~startsysteme.value == 1 and: {~musique.value == 1}, {~routineMusic.wrapAt(agent).play(quant: ~quantaMusic.reciprocal)})}.defer;
			// Init Band
			listBand = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
			~rangeSynthBand.do({arg band;
				if(~flagBandSynth.wrapAt(band) == 1,
					{
						if(0.5.coin, {listBand.put(band, 1)});
				});
			});
			~agentsBand = ~agentsBand.add(listBand);
		};

		// Mort agents
		~mortagents={arg agent;
			agent = agent.clip(0, ~agents - 1);// security size
			// Set MIDI Off
			if(~flagMidiOut == 'on' and: {~canalMidiOutAgent.wrapAt(agent) >= 0}, {
				~freqMidi.wrapAt(agent).size.do({arg index; ~midiOut.noteOff(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), 0);
					if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), 0)});
				});// MIDI OFF
			});
			~vies.removeAt(agent);
			~ages.removeAt(agent);
			~enfants.removeAt(agent);
			~dureesmusique.removeAt(agent);
			~freqMidi.removeAt(agent);
			~flagCompteurPlayingAgents.removeAt(agent);
			~listeagentfreq.removeAt(agent);
			~listeagentamp.removeAt(agent);
			~listeagentduree.removeAt(agent);
			~listeagentID.removeAt(agent);
			~agentspositionx.removeAt(agent);
			~agentspositiony.removeAt(agent);
			~agentspositionz.removeAt(agent);
			~genomes.removeAt(agent);
			~fitnessInne.removeAt(agent);
			~fitnessAcquis.removeAt(agent);
			~voisins.removeAt(agent);
			~signaux.removeAt(agent);
			~voisinsAffichage.removeAt(agent);
			~signauxAffichage.removeAt(agent);
			~fitnessAcquisvoisins.removeAt(agent);
			~voisinsOkCopulation.removeAt(agent);
			~couleurs.removeAt(agent);
			~flagplayagent.removeAt(agent);
			~compteur.removeAt(agent);
			~musicOutAgents.removeAt(agent);
			~canalMidiOutAgent.removeAt(agent);
			~listeIDTdefAgents.removeAt(agent);
			~bufferDataAgents.removeAt(agent);
			~chordMaxAgents.removeAt(agent);
			~chordDurAgents.removeAt(agent);
			~dureeNextAccouplement.removeAt(agent);
			~liensParents.removeAt(agent);
			~kohonenF.removeAt(agent);
			~kohonenA.removeAt(agent);
			~kohonenD.removeAt(agent);
			~geneticF.removeAt(agent);
			~geneticA.removeAt(agent);
			~geneticD.removeAt(agent);
			~neuralFAD.removeAt(agent);
			~agentsBand.removeAt(agent);
			// Free buffers
			~bufferAudioAgents.wrapAt(agent).free;
			~bufferFileAgents.wrapAt(agent).free;
			if(~synthRecAudioAgents.wrapAt(agent).isPlaying, {~synthRecAudioAgents.wrapAt(agent).free});
			if(~synthRecFileAgents.wrapAt(agent).isPlaying, {~synthRecFileAgents.wrapAt(agent).free});
			//Kill buffers
			~bufferAudioAgents.removeAt(agent);
			~bufferFileAgents.removeAt(agent);
			~synthRecAudioAgents.removeAt(agent);
			~synthRecFileAgents.removeAt(agent);
			//Remove Tdef
			~routineMusic.wrapAt(agent).stop;
			~routineMusic.wrapAt(agent).remove;
			~routineMusic.removeAt(agent);
		};

		//Mutation genes vieillissement
		~mutationgenes={arg agent;
			var gene, genome, envTime;
			gene = rrand(0, 3);
			~genomes.wrapAt(agent).wrapPut(gene, ~createOrmutationGenesAgents.value(gene));// Bio.1
			gene = rrand(4, 46);
			~genomes.wrapAt(agent).wrapPut(gene, ~createOrmutationGenesAgents.value(gene));// Music
			genome = ~genomes.wrapAt(agent);
			envTime = genome.copyRange(26, 33).sort;envTime.wrapPut(0, 0.0);envTime.wrapPut(7, 1.0);
			forBy(26, 33, 1 , {arg i; genome.wrapPut(i, envTime.wrapAt(i-26))});
			~genomes.wrapPut(agent, genome);
		};

		// Fonction Create or Mutation genes Agents
		~createOrmutationGenesAgents={arg gene;
			var valeur, listBand;
			valeur = gene.switch(
				// Bio
				0, {rrand(~geneVieillissement.lo, ~geneVieillissement.hi)},
				1, {rrand(~geneDeplacement.lo, ~geneDeplacement.hi)},
				2, {rrand(~geneVision.lo, ~geneVision.hi)},
				3, {rrand(~geneAudition.lo, ~geneAudition.hi)},
				// Music
				4, {rrand(~geneFreqRanger.lo, ~geneFreqRanger.hi) / 127},
				5, {rrand(~geneFreqRanger.lo, ~geneFreqRanger.hi) / 127},
				6, {rrand(~geneTransFreqRanger.lo, ~geneTransFreqRanger.hi) + 127 / 254},
				7, {rrand(~geneAmpRanger.lo.dbamp, ~geneAmpRanger.hi.dbamp)},
				8, {rrand(~geneAmpRanger.lo.dbamp, ~geneAmpRanger.hi.dbamp)},
				9, {rrand(~geneDureeRanger.lo, ~geneDureeRanger.hi)},
				10, {rrand(~geneDureeRanger.lo, ~geneDureeRanger.hi)},
				11, {rrand(~geneMulDureeRanger.lo, ~geneMulDureeRanger.hi) + 16 / 80},
				12, {rrand(~genePanRanger.lo, ~genePanRanger.hi) + 1 / 2},
				13, {rrand(~geneBufferRanger.lo, ~geneBufferRanger.hi) / ~tempsmaxsignal},
				14, {rrand(~geneSamplePopUpLow.value, ~geneSamplePopUpHigh.value)  / (~sounds.size - 1).max(1)},
				15, {rrand(~geneReverseRanger.lo, ~geneReverseRanger.hi) + 1 / 2},
				16, {rrand(~geneLoopRanger.lo, ~geneLoopRanger.hi) + 1 / 2},
				17, {rrand(~geneOffsetRanger.lo, ~geneOffsetRanger.hi)},
				18, {0.0},// Level start env = 0
				19, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				20, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				21, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				22, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				23, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				24, {rrand(~geneEnvLevelRanger.lo, ~geneEnvLevelRanger.hi)},
				25, {0.0},// Level end env = 0
				26, {0.0},// Duree start env = 0
				27, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				28, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				29, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				30, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				31, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				32, {rrand(~geneEnvDureeRanger.lo, ~geneEnvDureeRanger.hi)},
				33, {1.0},// Duree end env = 1
				34, {rrand(~geneSynthPopUpLow.value, ~geneSynthPopUpHigh.value)  / (~listSynth.size - 1)},
				35, {rrand(~geneControlsRanger.lo, ~geneControlsRanger.hi)},
				36, {rrand(~geneControlsRanger.lo, ~geneControlsRanger.hi)},
				37, {rrand(~geneControlsRanger.lo, ~geneControlsRanger.hi)},
				38, {rrand(~geneAudioOutRanger.lo, ~geneAudioOutRanger.hi) / (~sourceOutAgents.size)},
				39, {rrand(~geneMidiOutRanger.lo.asInteger, ~geneMidiOutRanger.hi.asInteger) / 16},
				40, {rrand(~geneInputPopUpLow.value, ~geneInputPopUpHigh.value)},
				41, {rrand(~geneLoopMusicRanger.lo, ~geneLoopMusicRanger.hi) + 1 / 2},
				42, {rrand(~geneBufferMusicRanger.lo, ~geneBufferMusicRanger.hi) / 256},
				43, {rrand(~geneChordMaxRanger.lo, ~geneChordMaxRanger.hi) / 12},
				44, {rrand(~geneChordDurRanger.lo, ~geneChordDurRanger.hi)},
				45, {rrand(~geneAlgorithm.lo, ~geneAlgorithm.hi) / (~listeAlgorithm.size - 1).max(1)},
				46, {
					// Init Band
					listBand = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					~rangeSynthBand.do({arg band;
						if(~flagBandGenes.wrapAt(band) == 1,
							{
								if(0.5.coin, {listBand.put(band, 1)});
						});
					});
					listBand;
				},
			);
			valeur;
		};

		//Update distance agents vs. voisins TORE
		~voisinsagentsTore={arg agent;
			var voisins=[], distance, ax, ay, az;
			ax = ~agentspositionx.wrapAt(agent);
			ay = ~agentspositiony.wrapAt(agent);
			az = ~agentspositionz.wrapAt(agent);
			distance = ~genomes.wrapAt(agent).wrapAt(2) * ~distanceagents;
			~agents.do({arg voisin;
				var x, y, z, diff1, diff2, difference;
				if(agent != voisin, {
					x=sqrdif(ax , ~agentspositionx.wrapAt(voisin));
					y=sqrdif(ay , ~agentspositiony.wrapAt(voisin));
					z=sqrdif(az , ~agentspositionz.wrapAt(voisin));
					diff1=x%~dureeanalysemax + y%1.0 + z%1.0;
					diff2=x.neg%~dureeanalysemax + y.neg%1.0 + z.neg%1.0;
					if(diff1 <= diff2, {difference = diff1}, {difference = diff2});
					if(sqrt(difference) <= distance, {voisins=voisins.add(voisin)});
				});
			});
			voisins.value;
		};

		//Update distance agents vs. voisins NO TORE
		~voisinsagentsNoTore={arg agent;
			var voisins=[], distance, ax, ay, az;
			ax = ~agentspositionx.wrapAt(agent);
			ay = ~agentspositiony.wrapAt(agent);
			az = ~agentspositionz.wrapAt(agent);
			distance = ~genomes.wrapAt(agent).wrapAt(2) * ~distanceagents;
			~agents.do({arg voisin;
				if(agent != voisin and: {sqrt(sqrdif(ax, ~agentspositionx.wrapAt(voisin)) + sqrdif(ay, ~agentspositiony.wrapAt(voisin)) + sqrdif(az , ~agentspositionz.wrapAt(voisin))) <= distance}, {voisins=voisins.add(voisin)});
			});
			voisins.value;
		};

		//Update distance agents vs. signaux TORE
		~signauxvoisinsTore={arg agent;
			var signaux=[], distance, ax, ay, az, geneFHZ, fhz;
			ax = ~agentspositionx.wrapAt(agent);
			ay = ~agentspositiony.wrapAt(agent);
			az = ~agentspositionz.wrapAt(agent);
			distance = ~genomes.wrapAt(agent).wrapAt(3) * ~distancesignaux;
			~listefreq.size.do({arg signal;
				var x, y, z, diff1, diff2, difference;
				fhz = ~listefreq.wrapAt(signal) * 127;
				x=sqrdif(ax , ~listeduree.wrapAt(signal));
				y=sqrdif(ay , ~listefreq.wrapAt(signal));
				z=sqrdif(az , ~listeamp.wrapAt(signal));
				diff1=x%~dureeanalysemax + y%1.0 + z%1.0;
				diff2=x.neg%~dureeanalysemax + y.neg%1.0 + z.neg%1.0;
				if(diff1 <= diff2, {difference = diff1}, {difference = diff2});
				if(sqrt(difference) <= distance, {signaux=signaux.add(signal);
					if(~flagSynthBand == 'on' and: {~agentsBand.wrapAt(agent).wrapAt(0) == 0} or: {{~flagGeneBand == 'on' and: {~genomes.wrapAt(agent).wrapAt(46).wrapAt(0) == 0}}}.value,
						{
							if(~flagGeneBand == 'on', {geneFHZ = ~genomes.wrapAt(agent).wrapAt(46)}, {geneFHZ = ~agentsBand.wrapAt(agent)});
							//
							for(1, ~numFhzBand, {arg i;
								if(fhz > ~bandFHZ.wrapAt(i).at(0) and: {fhz < ~bandFHZ.wrapAt(i).at(1)} and: {geneFHZ.wrapAt(i) == 1}, {
									// Add Data en fonction des bandes activent chez l'agent
									if(~listeagentID.wrapAt(agent).includes(~listeID.wrapAt(signal)).not,
										{~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)));
											if(~listeagentfreq.wrapAt(agent).size < ~bufferDataAgents.wrapAt(agent),
												{~listeagentfreq.wrapPut(agent,~listeagentfreq.wrapAt(agent).add(~listefreq.wrapAt(signal)));
													~listeagentamp.wrapPut(agent,~listeagentamp.wrapAt(agent).add(~listeamp.wrapAt(signal)));
													~listeagentduree.wrapPut(agent,~listeagentduree.wrapAt(agent).add(~listeduree.wrapAt(signal)))},
												{if(~compteur.wrapAt(agent) >= ~bufferDataAgents.wrapAt(agent), {~compteur.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset});
													if(~compteur.wrapAt(agent) < ~listeagentfreq.wrapAt(agent).size,
														{~listeagentfreq.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listefreq.wrapAt(signal));
															~listeagentamp.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeamp.wrapAt(signal));
															~listeagentduree.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeduree.wrapAt(signal));
															~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)))});
													~compteur.wrapPut(agent, ~compteur.wrapAt(agent)+1)});
									});
								});
							});
						},
						{
							if(~listeagentID.wrapAt(agent).includes(~listeID.wrapAt(signal)).not,
								{~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)));
									if(~listeagentfreq.wrapAt(agent).size < ~bufferDataAgents.wrapAt(agent),
										{~listeagentfreq.wrapPut(agent,~listeagentfreq.wrapAt(agent).add(~listefreq.wrapAt(signal)));
											~listeagentamp.wrapPut(agent,~listeagentamp.wrapAt(agent).add(~listeamp.wrapAt(signal)));
											~listeagentduree.wrapPut(agent,~listeagentduree.wrapAt(agent).add(~listeduree.wrapAt(signal)))},
										{if(~compteur.wrapAt(agent) >= ~bufferDataAgents.wrapAt(agent), {~compteur.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset});
											if(~compteur.wrapAt(agent) < ~listeagentfreq.wrapAt(agent).size,
												{~listeagentfreq.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listefreq.wrapAt(signal));
													~listeagentamp.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeamp.wrapAt(signal));
													~listeagentduree.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeduree.wrapAt(signal));
													~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)))});
											~compteur.wrapPut(agent, ~compteur.wrapAt(agent)+1)});
							});
					});
				});
			});
			signaux.value;
		};

		//Update distance agents vs. signaux NO TORE
		~signauxvoisinsNoTore={arg agent;
			var signaux=[], distance, ax, ay, az, geneFHZ, fhz;
			ax = ~agentspositionx.wrapAt(agent);
			ay = ~agentspositiony.wrapAt(agent);
			az = ~agentspositionz.wrapAt(agent);
			distance = ~genomes.wrapAt(agent).wrapAt(3) * ~distancesignaux;
			~listefreq.size.do({arg signal;
				var regle='true';
				fhz = ~listefreq.wrapAt(signal) * 127;
				if({~flagSynthBand == 'on' and: {~agentsBand.wrapAt(agent).wrapAt(0) == 0}}.value or: {{~flagGeneBand == 'on' and: {~genomes.wrapAt(agent).wrapAt(46).wrapAt(0) == 0}}}.value,
					{
						if(sqrt(sqrdif(ax , ~listeduree.wrapAt(signal)) + sqrdif(ay , ~listefreq.wrapAt(signal)) + sqrdif(az , ~listeamp.wrapAt(signal))) <= distance,
							{
								if(~flagGeneBand == 'on',
									{geneFHZ = ~genomes.wrapAt(agent).wrapAt(46)}, {geneFHZ = ~agentsBand.wrapAt(agent)});
								for(1, ~numFhzBand, {arg i;
									if(fhz > ~bandFHZ.wrapAt(i).at(0) and: {fhz < ~bandFHZ.wrapAt(i).at(1)} and: {geneFHZ.wrapAt(i) == 1}, {
										signaux=signaux.add(signal);
										if(~listeagentID.wrapAt(agent).includes(~listeID.wrapAt(signal)).not,
											{~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)));
												if(~listeagentfreq.wrapAt(agent).size < ~bufferDataAgents.wrapAt(agent),
													{~listeagentfreq.wrapPut(agent,~listeagentfreq.wrapAt(agent).add(~listefreq.wrapAt(signal)));
														~listeagentamp.wrapPut(agent,~listeagentamp.wrapAt(agent).add(~listeamp.wrapAt(signal)));
														~listeagentduree.wrapPut(agent,~listeagentduree.wrapAt(agent).add(~listeduree.wrapAt(signal)))},
													{if(~compteur.wrapAt(agent) >= ~bufferDataAgents.wrapAt(agent), {~compteur.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset});
														if(~compteur.wrapAt(agent) < ~listeagentfreq.wrapAt(agent).size,
															{~listeagentfreq.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listefreq.wrapAt(signal));
																~listeagentamp.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeamp.wrapAt(signal));
																~listeagentduree.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeduree.wrapAt(signal));
																~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)))});
														~compteur.wrapPut(agent, ~compteur.wrapAt(agent)+1)});
										});
									});
								});
						});
					},
					{
						if(sqrt(sqrdif(ax , ~listeduree.wrapAt(signal)) + sqrdif(ay , ~listefreq.wrapAt(signal)) + sqrdif(az , ~listeamp.wrapAt(signal))) <= distance,
							{signaux=signaux.add(signal);
								if(~listeagentID.wrapAt(agent).includes(~listeID.wrapAt(signal)).not,
									{~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)));
										if(~listeagentfreq.wrapAt(agent).size < ~bufferDataAgents.wrapAt(agent),
											{~listeagentfreq.wrapPut(agent,~listeagentfreq.wrapAt(agent).add(~listefreq.wrapAt(signal)));
												~listeagentamp.wrapPut(agent,~listeagentamp.wrapAt(agent).add(~listeamp.wrapAt(signal)));
												~listeagentduree.wrapPut(agent,~listeagentduree.wrapAt(agent).add(~listeduree.wrapAt(signal)))},
											{if(~compteur.wrapAt(agent) >= ~bufferDataAgents.wrapAt(agent), {~compteur.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset});
												if(~compteur.wrapAt(agent) < ~listeagentfreq.wrapAt(agent).size,
													{~listeagentfreq.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listefreq.wrapAt(signal));
														~listeagentamp.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeamp.wrapAt(signal));
														~listeagentduree.wrapAt(agent).wrapPut(~compteur.wrapAt(agent), ~listeduree.wrapAt(signal));
														~listeagentID.wrapPut(agent,~listeagentID.wrapAt(agent).add(~listeID.wrapAt(signal)))});
												~compteur.wrapPut(agent, ~compteur.wrapAt(agent)+1)});
								});
						});
				});
			});
			signaux.value;
		};

		//Update fitnessAcquis agents vs. voisins
		~sortfitnessAcquis={arg agent, voisins;
			var voisinsfitnessAcquis=[], fitnessAgent;
			if(~flagElitiste == 'on', {//Elitiste
				fitnessAgent = ~fitnessAcquis.wrapAt(agent);
				voisins.do({arg voisin;
					if(~fitnessAcquis.wrapAt(voisin) >= fitnessAgent,
						{voisinsfitnessAcquis=voisinsfitnessAcquis.add(voisin)});
			})},
			{// Non elitiste
				voisins.do({arg voisin; voisinsfitnessAcquis=voisinsfitnessAcquis.add(voisin)});
			});
			voisinsfitnessAcquis.value;
		};

		//Update fitness/age/enfant agents vs. voisins pour copulation
		~sortVoisinsCopulation={arg agent, voisins;
			var voisinsOkcopulation=[], fitnessAgent;
			if(~flagElitiste == 'on', {//Elitiste
				fitnessAgent = ~fitnessInne.wrapAt(agent);
				voisins.do({arg voisin;
					if(~fitnessInne.wrapAt(voisin) >= fitnessAgent and: {~enfants.wrapAt(voisin) < ~maximumenfants} and: {~ages.wrapAt(voisin) == 'adult'} and: {~liensParents.wrapAt(agent).includesAny(~liensParents.wrapAt(voisin)) == false},
						{voisinsOkcopulation=voisinsOkcopulation.add(voisin)})})},
			{// Non-Elitiste
				voisins.do({arg voisin;
					if(~enfants.wrapAt(voisin) < ~maximumenfants and: {~ages.wrapAt(voisin) == 'adult'} and: {~liensParents.wrapAt(agent).includesAny(~liensParents.wrapAt(voisin)) == false},
						{voisinsOkcopulation=voisinsOkcopulation.add(voisin)})});
			});
			voisinsOkcopulation.value;
		};

		//Update apprentissage agents
		~apprentissageagents={arg agent, voisin;
			var gene, valeur, positionLow, positionHigh, longueur, index=0, genome, envTime;
			gene=rrand(4, 46);// Music
			if(~mutation >= 1.0.rand, {valeur=~createOrmutationGenesAgents.value(gene)},{valeur=~genomes.wrapAt(voisin).wrapAt(gene)});
			~genomes.wrapAt(agent).wrapPut(gene,valeur);
			genome = ~genomes.wrapAt(agent);
			envTime = genome.copyRange(26, 33).sort;envTime.wrapPut(0, 0.0);envTime.wrapPut(7, 1.0);
			forBy(26, 33, 1 , {arg i; genome.wrapPut(i, envTime.wrapAt(i-26))});
			~genomes.wrapPut(agent, genome);
			//Musique
			if(~flagSharedMusic=='on', {if(~listeagentfreq.wrapAt(voisin).size >= 1
				and: {~listeagentfreq.wrapAt(agent).size < ~bufferDataAgents.wrapAt(agent)}, {
					~flagplayagent.wrapPut(agent, 'new');~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset;
					positionHigh=~listeagentfreq.wrapAt(voisin).size.rand;
					positionLow = (~listeagentfreq.wrapAt(voisin).size - positionHigh).rand;
					positionHigh=positionHigh + positionLow;
					longueur = positionHigh - positionLow + 1;
					longueur.do({arg i;i=i+positionLow;
						if(~mutation > 1.0.rand, {
							~listeagentfreq.wrapPut(agent, ~listeagentfreq.wrapAt(agent).add(1.0.rand))},{~listeagentfreq.wrapPut(agent, ~listeagentfreq.wrapAt(agent).add(~listeagentfreq.wrapAt(voisin).wrapAt(i)))});
						// No mutation sur amplitude
						~listeagentamp.wrapPut(agent, ~listeagentamp.wrapAt(agent).add(~listeagentamp.wrapAt(voisin).wrapAt(i)));
						if(~mutation > 1.0.rand, {
							~listeagentduree.wrapPut(agent, ~listeagentduree.wrapAt(agent).add(~dureeanalysemax.rand))},{~listeagentduree.wrapPut(agent, ~listeagentduree.wrapAt(agent).add(~listeagentduree.wrapAt(voisin).wrapAt(i)))})})});
			});
		};

		// Accouplement agents
		~accouplement={arg agent, voisin;
			var genome1=[], genome2=[], genome, fitnessInne, fitnessAcquis, listeF=[], listeA=[], listeD=[], listeS=0, x, y, z, envTime, actualTime=Main.elapsedTime;
			// Genome
			if(((actualTime - ~dureeNextAccouplement.wrapAt(agent))  * ~vitessegeneration) >= ~vitessegeneration and: {((actualTime - ~dureeNextAccouplement.wrapAt(voisin)) * ~vitessegeneration) >= (~dureeVieAgents / 60 * ~vitessegeneration)},
				{~genomes.wrapAt(agent).size.do({arg gene;
					if( ~croisement > 1.0.rand,
						{if(~mutation > 1.0.rand,
							{genome1=genome1.add(~createOrmutationGenesAgents.value(gene))}, {genome1=genome1.add(~genomes.wrapAt(voisin).wrapAt(gene))});
						if(~mutation > 1.0.rand,
							{genome2=genome2.add(~createOrmutationGenesAgents.value(gene))}, {genome2=genome2.add(~genomes.wrapAt(agent).wrapAt(gene))});
						},
						{if(~mutation > 1.0.rand,
							{genome1=genome1.add(~createOrmutationGenesAgents.value(gene))}, {genome1=genome1.add(~genomes.wrapAt(agent).wrapAt(gene))});
						if(~mutation > 1.0.rand,
							{genome2=genome2.add(~createOrmutationGenesAgents.value(gene))}, {genome2=genome2.add(~genomes.wrapAt(voisin).wrapAt(gene))});
						};
					);
				});
				if(1.0.rand.coin, {genome=genome2},{genome=genome1});
				envTime = genome.copyRange(26, 33).sort;envTime.wrapPut(0, 0.0);envTime.wrapPut(7, 1.0);
				forBy(26, 33, 1 , {arg i; genome.wrapPut(i, envTime.wrapAt(i-26))});
				~enfants.wrapPut(agent, ~enfants.wrapAt(agent) + 1);~enfants.wrapPut(voisin, ~enfants.wrapAt(voisin) + 1);
				fitnessInne=(~fitnessInne.wrapAt(agent) + ~fitnessInne.wrapAt(voisin)) / 2;
				fitnessAcquis=(~fitnessAcquis.wrapAt(agent) + ~fitnessAcquis.wrapAt(voisin)) / 2;
				// choix du parent pour new emplacement enfant
				if(1.0.rand.coin, {x=~agentspositionx.wrapAt(voisin);y=~agentspositiony.wrapAt(voisin);z=~agentspositionz.wrapAt(voisin)},{x=~agentspositionx.wrapAt(agent);y=~agentspositiony.wrapAt(agent);z=~agentspositionz.wrapAt(agent)});
				//creation enfant
				~initagents.value(~agents, genome, fitnessInne, fitnessAcquis, 'create', listeF, listeA, listeD, x, y, z, agent, voisin);
				~agents=~agents + 1;// nouveau-ne additionne
				~dureeNextAccouplement.wrapPut(agent, actualTime);// flag pour duree copulation
				~dureeNextAccouplement.wrapPut(voisin, actualTime);// flag pour duree copulation
			});
		};

		//Moving agents dans l'espace TORE
		~movingagentsTore={arg agent, voisins, signaux;
			var meansx=0, meansy=0, meansz=0, difx=0, dify=0, difz=0, signalchoisi, agentchoisi;
			// Signaux voisins
			if(signaux.size > 0,
				{if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[0, 1, 1])});// bleu ciel
					# meansx, meansy, meansz = ~routineSignauxFlock.value(agent, voisins, signaux);
					if(~agentspositionx.wrapAt(agent) > meansx ,{difx=(~agentspositionx.wrapAt(agent) - meansx) * 1.neg},{difx=meansx - ~agentspositionx.wrapAt(agent)});
					if(~agentspositiony.wrapAt(agent) > meansy ,{dify=(~agentspositiony.wrapAt(agent) - meansy) * 1.neg},{dify=meansy - ~agentspositiony.wrapAt(agent)});
					if(~agentspositionz.wrapAt(agent) > meansz ,{difz=(~agentspositionz.wrapAt(agent) - meansz) * 1.neg},{difz=meansz - ~agentspositionz.wrapAt(agent)});
					~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((difx * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difx).rand2) * ~vitesseagents)%~dureeanalysemax);
					~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((dify * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*dify).rand2) * ~vitesseagents)%1.0);
					~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((difz * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difz).rand2) * ~vitesseagents)%1.0);
				},
				// Voisins voisins
				{if(voisins.size > 0,
					{if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[1, 0, 1])});// magenta
						# meansx, meansy, meansz = ~routineAgentsFlock.value(agent, voisins, signaux);
						if(~agentspositionx.wrapAt(agent) > meansx ,{difx=(~agentspositionx.wrapAt(agent) - meansx) * 1.neg},{difx=meansx - ~agentspositionx.wrapAt(agent)});
						if(~agentspositiony.wrapAt(agent) > meansy ,{dify=(~agentspositiony.wrapAt(agent) - meansy) * 1.neg},{dify=meansy - ~agentspositiony.wrapAt(agent)});
						if(~agentspositionz.wrapAt(agent) > meansz ,{difz=(~agentspositionz.wrapAt(agent) - meansz) * 1.neg},{difz=meansz - ~agentspositionz.wrapAt(agent)});
						~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((difx * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difx).rand2) * ~vitesseagents)%~dureeanalysemax);
						~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((dify * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*dify).rand2) * ~vitesseagents)%1.0);
						~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((difz * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difz).rand2) * ~vitesseagents)%1.0);
					},
					// Alone in the world
					{~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents)%~dureeanalysemax);
						~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents)%1.0);
						~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents)%1.0);
				});
			});
			// Anothers agents here ????
			~agents.do({arg index;
				if(index != agent,{if(abs(~agentspositionx.wrapAt(agent) - ~agentspositionx.wrapAt(index)) <= 0.0078740157480315 and: {abs(~agentspositiony.wrapAt(agent) - ~agentspositiony.wrapAt(index)) <= 0.0078740157480315} and: {abs(~agentspositionz.wrapAt(agent) - ~agentspositionz.wrapAt(index)) <= 0.0078740157480315},
					{~agentspositionx.wrapPut(agent, (~agentspositionx.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1)))%~dureeanalysemax);
						~agentspositiony.wrapPut(agent, (~agentspositiony.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1)))%1.0);
						~agentspositionz.wrapPut(agent, (~agentspositionz.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1)))%1.0)})});
			});
		};

		//Moving agents dans l'espace NO TORE
		~movingagentsNoTore={arg agent, voisins, signaux;
			var meansx=0, meansy=0, meansz=0, difx=0, dify=0, difz=0, signalchoisi, agentchoisi;
			// Signaux voisins
			if(signaux.size > 0,
				{if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[0, 1, 1])});// bleu ciel
					# meansx, meansy, meansz = ~routineSignauxFlock.value(agent, voisins, signaux);
					if(~agentspositionx.wrapAt(agent) > meansx ,{difx=(~agentspositionx.wrapAt(agent) - meansx) * 1.neg},{difx=meansx - ~agentspositionx.wrapAt(agent)});
					if(~agentspositiony.wrapAt(agent) > meansy ,{dify=(~agentspositiony.wrapAt(agent) - meansy) * 1.neg},{dify=meansy - ~agentspositiony.wrapAt(agent)});
					if(~agentspositionz.wrapAt(agent) > meansz ,{difz=(~agentspositionz.wrapAt(agent) - meansz) * 1.neg},{difz=meansz - ~agentspositionz.wrapAt(agent)});
					~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((difx * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difx).rand2) * ~vitesseagents));
					~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((dify * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*dify).rand2) * ~vitesseagents));
					~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((difz * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difz).rand2) * ~vitesseagents));
				},
				// Voisins voisins
				{if(voisins.size > 0,
					{if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[1, 0, 1])});// magenta
						# meansx, meansy, meansz = ~routineAgentsFlock.value(agent, voisins, signaux);
						if(~agentspositionx.wrapAt(agent) > meansx ,{difx=(~agentspositionx.wrapAt(agent) - meansx) * 1.neg},{difx=meansx - ~agentspositionx.wrapAt(agent)});
						if(~agentspositiony.wrapAt(agent) > meansy ,{dify=(~agentspositiony.wrapAt(agent) - meansy) * 1.neg},{dify=meansy - ~agentspositiony.wrapAt(agent)});
						if(~agentspositionz.wrapAt(agent) > meansz ,{difz=(~agentspositionz.wrapAt(agent) - meansz) * 1.neg},{difz=meansz - ~agentspositionz.wrapAt(agent)});
						~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((difx * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difx).rand2) * ~vitesseagents));
						~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((dify * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*dify).rand2) * ~vitesseagents));
						~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((difz * ~genomes.wrapAt(agent).wrapAt(1) + (~deviance*difz).rand2) * ~vitesseagents));
					},
					// Alone in the world
					{~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents));
						~agentspositiony.wrapPut(agent, ~agentspositiony.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents));
						~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent) + ((~genomes.wrapAt(agent).wrapAt(1)*sign(0.5.rand2) + (~deviance*~genomes.wrapAt(agent).wrapAt(1)).rand2) * ~vitesseagents));
				});
			});
			// No Tore
			if(~agentspositionx.wrapAt(agent) < 0, {~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent).abs)});
			if(~agentspositionx.wrapAt(agent) > ~dureeanalysemax, {~agentspositionx.wrapPut(agent, ~dureeanalysemax - ~agentspositionx.wrapAt(agent)%~dureeanalysemax)});
			if(~agentspositiony.wrapAt(agent) < 0, {~agentspositiony.wrapPut(agent,  ~agentspositiony.wrapAt(agent).abs)});
			if(~agentspositiony.wrapAt(agent) > 1.0, {~agentspositiony.wrapPut(agent, 1.0 - ~agentspositiony.wrapAt(agent)%1.0)});
			if(~agentspositionz.wrapAt(agent) < 0, {~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent).abs)});
			if(~agentspositionz.wrapAt(agent) > 1.0, {~agentspositionz.wrapPut(agent, 1.0 - ~agentspositionz.wrapAt(agent)%1.0)});
			// Anothers agents here ????
			~agents.do({arg index;
				if(index != agent,{if({(abs(~agentspositionx.wrapAt(agent) - ~agentspositionx.wrapAt(index)) <= 0.0078740157480315) && (abs(~agentspositiony.wrapAt(agent) - ~agentspositiony.wrapAt(index)) <= 0.0078740157480315) && (abs(~agentspositionz.wrapAt(agent) - ~agentspositionz.wrapAt(index)) <= 0.0078740157480315)}.value,
					{~agentspositionx.wrapPut(agent, (~agentspositionx.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1))));
						~agentspositiony.wrapPut(agent, (~agentspositiony.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1))));
						~agentspositionz.wrapPut(agent, (~agentspositionz.wrapAt(agent) + (~deviance.rand2 * ~vitesseagents * ~genomes.wrapAt(agent).wrapAt(1))))})});
			});
			// No Tore
			if(~agentspositionx.wrapAt(agent) < 0, {~agentspositionx.wrapPut(agent, ~agentspositionx.wrapAt(agent).abs)});
			if(~agentspositionx.wrapAt(agent) > ~dureeanalysemax, {~agentspositionx.wrapPut(agent, ~dureeanalysemax - ~agentspositionx.wrapAt(agent)%~dureeanalysemax)});
			if(~agentspositiony.wrapAt(agent) < 0, {~agentspositiony.wrapPut(agent,  ~agentspositiony.wrapAt(agent).abs)});
			if(~agentspositiony.wrapAt(agent) > 1.0, {~agentspositiony.wrapPut(agent, 1.0 - ~agentspositiony.wrapAt(agent)%1.0)});
			if(~agentspositionz.wrapAt(agent) < 0, {~agentspositionz.wrapPut(agent, ~agentspositionz.wrapAt(agent).abs)});
			if(~agentspositionz.wrapAt(agent) > 1.0, {~agentspositionz.wrapPut(agent, 1.0 - ~agentspositionz.wrapAt(agent)%1.0)});
		};

		// Routines signaux No Flock
		~movingsignauxNoFlock={arg agent, voisins, signaux;
			var signalchoisi, meansx=0, meansy=0, meansz=0;
			signalchoisi=rrand(0,signaux.size - 1);
			meansx=~listeduree.wrapAt(signalchoisi);
			meansy=~listefreq.wrapAt(signalchoisi);
			meansz=~listeamp.wrapAt(signalchoisi);
			[meansx, meansy, meansz].value;
		};
		// Routines signaux Flock
		~movingsignauxFlock={arg agent, voisins, signaux;
			var signalchoisi, meansx=0, meansy=0, meansz=0;
			signaux.size.do({arg signal;
				meansx=meansx+~listeduree.wrapAt(signal);
				meansy=meansy+~listefreq.wrapAt(signal);
				meansz=meansz+~listeamp.wrapAt(signal);
			});
			meansx=meansx/signaux.size;meansy=meansy/signaux.size;meansz=meansz/signaux.size;
			[meansx, meansy, meansz].value;
		};
		// Routines agents No Flock
		~movingagentsNoFlock={arg agent, voisins, signaux;
			var agentchoisi, meansx=0, meansy=0, meansz=0;
			agentchoisi=rrand(0,voisins.size - 1);
			meansx=~agentspositionx.wrapAt(agentchoisi);
			meansy=~agentspositiony.wrapAt(agentchoisi);
			meansz=~agentspositionz.wrapAt(agentchoisi);
			[meansx, meansy, meansz].value
		};
		// Routines agents Flock
		~movingagentsFlock={arg agent, voisins, signaux;
			var agentchoisi, meansx=0, meansy=0, meansz=0;
			voisins.size.do({arg voisin;
				meansx=meansx+~agentspositionx.wrapAt(voisin);
				meansy=meansy+~agentspositiony.wrapAt(voisin);
				meansz=meansz+~agentspositionz.wrapAt(voisin);
			});
			meansx=meansx/voisins.size;meansy=meansy/voisins.size;meansz=meansz/voisins.size;
			[meansx, meansy, meansz].value;
		};

		// Playing musique agents
		~agentsmusique={arg agent;
			var freq=[], freqRate=[], amp=[], ampReal, duree=0, compteuraccord=0, reverse=[], bufferSon, bufferSon2, freqLow, freqRange, freqTrans, ampRange, ampLow, dureeRange, dureeLow, dureeTempo, envLevel=[], envDuree=[], timeEnv=[], pan, offset, synth, indexSynth=0, loopSample, reverseSample, audioOut, indexOut, controlF, controlA, controlD, testLoop, sourceInAgent, flagInput = 0, octave, ratio, degre, difL, difH, pos=~scale.degrees.size - 1, q1, mediane, q3, ecartQ, ecartSemiQ, ecartType, cv, dissymetrie,  transOctave, transTranspose, transCompExpAdd, transCompExpMul, newDuree=[], newFreq=[], newAmp=[], distances, maxTraining, numAlgo, sourceAlgorithm;
			if(~flagGeneLoopMusic == 'on', {if(~genomes.wrapAt(agent).wrapAt(41) <= 0.5, {testLoop='off'},{testLoop='on'})},{if(~flagloop == 'on', {testLoop='on'},{testLoop='off'})});
			if(~flagplayagent.wrapAt(agent) == 'new' and: {testLoop != 'on'}, {~flagplayagent.wrapPut(agent, 'on')});
			if(~flagCompteurPlayingAgents.wrapAt(agent) >= ~listeagentfreq.wrapAt(agent).size, {
				if(testLoop == 'on', {
					~flagCompteurPlayingAgents.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'on')}, {
					~listeagentfreq.wrapPut(agent, []);~listeagentamp.wrapPut(agent, []);~listeagentduree.wrapPut(agent, []);~dureesmusique.wrapPut(agent, ~quantaMusic.reciprocal);~routineMusic.wrapAt(agent).reset;
					~flagCompteurPlayingAgents.wrapPut(agent, 0);~flagplayagent.wrapPut(agent, 'new')})});
			// Choose Algo (Default / Probabilite / Kohonen / ...)
			if(~flagGeneAlgorithm == 'on', {numAlgo = (~genomes.wrapAt(agent).wrapAt(45).value * 4).floor; sourceAlgorithm = ~listeAlgorithm.wrapAt(numAlgo)},{sourceAlgorithm = ~algoMusic.value});
			if(~listeagentfreq.wrapAt(agent) != [] and: ~listeagentamp.wrapAt(agent) != [] and: ~listeagentduree.wrapAt(agent) != [],
				{
					switch(sourceAlgorithm,
						"Default", {nil}, // Do nothing
						"Probability", {
							// Freq Transformation
							newFreq = ~listeagentfreq.wrapAt(agent);
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
							newFreq = newFreq.mod(1);
							~listeagentfreq.wrapPut(agent, newFreq);
							// Amp Transformation
							newAmp = ~listeagentamp.wrapAt(agent);
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
							newAmp = newAmp.mod(1);
							~listeagentamp.wrapPut(agent, newAmp);
							// Duree Transformation
							newDuree = ~listeagentduree.wrapAt(agent);// / ~dureeanalysemax;
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
							newDuree = newDuree.mod(~dureeanalysemax);
							~listeagentduree.wrapPut(agent, newDuree);
						},
						"Euclide", {
							newFreq = ~listeagentfreq.wrapAt(agent);
							newAmp = ~listeagentamp.wrapAt(agent);
							newDuree = ~listeagentduree.wrapAt(agent) / ~dureeanalysemax;
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
							newFreq = newFreq.mod(1);
							~listeagentfreq.wrapPut(agent, newFreq);
							// Amp
							if(coin(cv.frac), {newAmp = newAmp * distances}, {newAmp = newAmp / distances});
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
							newAmp = newAmp.mod(1);
							~listeagentamp.wrapPut(agent, newAmp);
							// Duree
							if(coin(cv.frac), {newDuree = newDuree / distances},{newDuree = newDuree * distances});
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
							newDuree = newDuree.mod(1) * ~dureeanalysemax;
							~listeagentduree.wrapPut(agent, newDuree);
						},
						"Genetic", {
							// Calculation algo new musical pattern
							~listeagentfreq.wrapAt(agent).size.do({arg i, f, a, d;
								# f = ~geneticF.wrapAt(agent).next([~listeagentfreq.wrapAt(agent).wrapAt(i)], 1, ~flatness, ~flux);
								newFreq = newFreq.add(f);
								# a = ~geneticA.wrapAt(agent).next([~listeagentamp.wrapAt(agent).wrapAt(i)], 1, ~flatness, ~flux);
								newAmp = newAmp.add(a);
								# d = ~geneticD.wrapAt(agent).next([~listeagentduree.wrapAt(agent).wrapAt(i) / ~dureeanalysemax], 1, ~flatness, ~flux);
								newDuree = newDuree.add(d * ~dureeanalysemax);
							});
							~listeagentfreq.wrapPut(agent, newFreq);
							~listeagentamp.wrapPut(agent, newAmp);
							~listeagentduree.wrapPut(agent, newDuree);
						},
						"Kohonen", {
							newFreq = ~listeagentfreq.wrapAt(agent) * 127;
							newAmp = ~listeagentamp.wrapAt(agent) * 127;
							newDuree = ~listeagentduree.wrapAt(agent) / ~dureeanalysemax * 127;
							// Training Kohonen Freq
							maxTraining = newFreq.size;
							maxTraining.do({arg i; ~kohonenF.wrapAt(agent).training(newFreq.wrapAt(i).asArray, i+1, maxTraining, 1)});
							// Calculate Kohonen Freq
							newFreq = newFreq.collect({arg item, index, z;
								item = ~kohonenF.wrapAt(agent).training(item.asArray, 1, 1, 1);
								item = item.wrapAt(0).wrapAt(1);// Vecteur
								//item = item.mod(127);
								item = item / 127;
							});
							~listeagentfreq.wrapPut(agent, newFreq);
							// Training Kohonen Amp
							maxTraining.do({arg i; ~kohonenA.wrapAt(agent).training(newAmp.wrapAt(i).asArray, i+1, maxTraining, 1)});
							// Calculate Kohonen Amp
							newAmp = newAmp.collect({arg item, index;
								item = ~kohonenA.wrapAt(agent).training(item.asArray, 1, 1, 1);
								item = item.wrapAt(0).wrapAt(1);// Vecteur
								//item = item.mod(127);
								item = item / 127;
							});
							~listeagentamp.wrapPut(agent, newAmp);
							// Training Kohonen Duree
							maxTraining.do({arg i; ~kohonenD.wrapAt(agent).training(newDuree.wrapAt(i).asArray, i+1, maxTraining, 1)});
							// Calculate Kohonen Duree
							newDuree = newDuree.collect({arg item, index;
								item = ~kohonenD.wrapAt(agent).training(item.asArray, 1, 1, 1);
								item = item.wrapAt(0).wrapAt(1);// Vecteur
								//item = item.mod(127);
								item = item / 127 * ~dureeanalysemax;
							});
							~listeagentduree.wrapPut(agent, newDuree);
						},
						"Neural", {
							//maxTraining = ~listeagentfreq.wrapAt(agent).size;
							// Calculation algo new musical pattern
							~listeagentfreq.wrapAt(agent).size.do({arg i, f, a, d;
								# f, a, d = ~neuralFAD.wrapAt(agent).next([~listeagentfreq.wrapAt(agent).wrapAt(i), ~listeagentamp.wrapAt(agent).wrapAt(i), ~listeagentduree.wrapAt(agent).wrapAt(i) / ~dureeanalysemax], nil, 1, 0.5, 0.5);
								newFreq = newFreq.add(f);
								newAmp = newAmp.add(a);
								newDuree = newDuree.add(d * ~dureeanalysemax);
							});
							~listeagentfreq.wrapPut(agent, newFreq);
							~listeagentamp.wrapPut(agent, newAmp);
							~listeagentduree.wrapPut(agent, newDuree);
						},
					);
					//playing agent
					~couleurs.wrapPut(agent,[1, 1, 1]);// blanc
					while({duree < ~chordDurAgents.wrapAt(agent) and: {~listeagentfreq.wrapAt(agent).size > ~flagCompteurPlayingAgents.wrapAt(agent)} and: {~chordMaxAgents.wrapAt(agent) > compteuraccord}},
						{freq=freq.add(~listeagentfreq.wrapAt(agent).wrapAt(~flagCompteurPlayingAgents.wrapAt(agent)) * 127);
							amp=amp.add(~listeagentamp.wrapAt(agent).wrapAt(~flagCompteurPlayingAgents.wrapAt(agent)));
							duree =~listeagentduree.wrapAt(agent).wrapAt(~flagCompteurPlayingAgents.wrapAt(agent));
							~flagCompteurPlayingAgents.wrapPut(agent, ~flagCompteurPlayingAgents.wrapAt(agent) + 1);
							compteuraccord=compteuraccord + 1;
					});
					// Set parametres genes Synth on/off
					// Freq
					if(~flagGeneFreq == 'off',
						{freqRange = (~freqInstr.hi - ~freqInstr.lo);
							freqLow = ~freqInstr.lo},
						{freqRange = (~genomes.wrapAt(agent).wrapAt(4) - ~genomes.wrapAt(agent).wrapAt(5)).abs * 127;
							freqLow = if(~genomes.wrapAt(agent).wrapAt(4) < ~genomes.wrapAt(agent).wrapAt(5), {~genomes.wrapAt(agent).wrapAt(4)},{~genomes.wrapAt(agent).wrapAt(5)}) * 127});
					if(~flagGeneTransFreq == 'off',
						{freqTrans = ~freqTransposeInstr.value},
						{freqTrans = ~genomes.wrapAt(agent).wrapAt(6) * 254 - 127});
					//Amp
					if(~flagGeneAmp == 'off',
						{ampRange = (~ampInstr.hi.dbamp - ~ampInstr.lo.dbamp);
							ampLow = ~ampInstr.lo.dbamp},
						{ampRange = (~genomes.wrapAt(agent).wrapAt(7) - ~genomes.wrapAt(agent).wrapAt(8)).abs;
							ampLow = if(~genomes.wrapAt(agent).wrapAt(7) < ~genomes.wrapAt(agent).wrapAt(8), {~genomes.wrapAt(agent).wrapAt(7)},{~genomes.wrapAt(agent).wrapAt(8)})});
					//Duree
					if(~flagGeneDuree == 'off',
						{dureeRange = (~dureeInstr.hi - ~dureeInstr.lo);
							dureeLow = ~dureeInstr.lo},
						{dureeRange = (~genomes.wrapAt(agent).wrapAt(9) - ~genomes.wrapAt(agent).wrapAt(10)).abs;
							dureeLow = if(~genomes.wrapAt(agent).wrapAt(9) < ~genomes.wrapAt(agent).wrapAt(10), {~genomes.wrapAt(agent).wrapAt(9)},{~genomes.wrapAt(agent).wrapAt(10)})});
					if(~flagGeneMulDuree == 'off',
						{dureeTempo = ~dureeTempoinstr.value},
						{dureeTempo = ~genomes.wrapAt(agent).wrapAt(11) * 80 - 16});
					// Set good number / tempo
					if(dureeTempo < 1 and: {dureeTempo >= 1.neg}, {dureeTempo = 1});
					if(dureeTempo < 1.neg, {dureeTempo = dureeTempo.reciprocal.neg});
					//Pan
					if(~flagGenePan == 'off',
						{pan=~panInstr.value},
						{pan= ~genomes.wrapAt(agent).wrapAt(12) * 2 - 1;pan = [pan, pan]});
					// Buffer + sample
					if(~flagGeneBuffer == 'off',
						{// Sample
							if(~flagGeneSample == 'off',
								{bufferSon = ~bufferSonsInstruments;
									bufferSon2=~bufferSons.wrapAt((~genomes.wrapAt(agent).wrapAt(14)*~sounds.size - 1).ceil.clip(0, ~sounds.size - 1));
									//Reverse sample
									if(~flagGeneReverse == 'off',
										{reverseSample=~reverseSynthSons.wrapAt(~soundsPositions);if(reverseSample == 1, {reverseSample=1.neg},{reverseSample=1})},
										{reverseSample= (~genomes.wrapAt(agent).wrapAt(15) * 2 - 1).sign});
									//Loop sample
									if(~flagGeneLoop == 'off',
										{loopSample=~loopSynthSons.wrapAt(~soundsPositions)},
										{loopSample = (~genomes.wrapAt(agent).wrapAt(16) * 2 - 1).sign});
									//Offset
									if(~flagGeneOffset == 'off',
										{offset=~posSamplesSons.wrapAt(~soundsPositions);
											if(reverseSample == 1.neg, {offset=1 - offset})},
										{offset=  ~genomes.wrapAt(agent).wrapAt(17);
											if(reverseSample == 1.neg, {offset=1 - offset})})},
								{bufferSon =  ~bufferSons.wrapAt((~genomes.wrapAt(agent).wrapAt(14)*~sounds.size-1).ceil.clip(0,~sounds.size-1));
									bufferSon2= ~bufferSonsInstruments;
									//Reverse sample
									if(~flagGeneReverse == 'off',
										{reverseSample=~reverseSynthSons.wrapAt(~soundsPositions);if(reverseSample == 1, {reverseSample=1.neg},{reverseSample=1})},
										{reverseSample= (~genomes.wrapAt(agent).wrapAt(15) * 2 - 1).sign});
									//Loop sample
									if(~flagGeneLoop == 'off',
										{loopSample=~loopSynthSons.wrapAt(~soundsPositions)},
										{loopSample = (~genomes.wrapAt(agent).wrapAt(16) * 2 - 1).sign});
									//Offset
									if(~flagGeneOffset == 'off',
										{offset=~posSamplesSons.wrapAt(~soundsPositions);
											if(reverseSample == 1.neg, {offset=1 - offset})},
										{offset=  ~genomes.wrapAt(agent).wrapAt(17);
											if(reverseSample == 1.neg, {offset=1 - offset})})})},
						// Buffers
						{if(~flagEntreeMode=='Audio', {bufferSon =  ~bufferAudioAgents.wrapAt(agent);
							bufferSon2= ~bufferSonsInstruments},{bufferSon =  ~bufferFileAgents.wrapAt(agent);
							bufferSon2= ~bufferSonsInstruments});
						//Reverse sample
						if(~flagGeneReverse == 'off',
							{reverseSample=~reverseSynthSons.wrapAt(~soundsPositions);if(reverseSample == 1, {reverseSample=1.neg},{reverseSample=1})},
							{reverseSample= (~genomes.wrapAt(agent).wrapAt(15) * 2  - 1).sign});
						//Loop sample
						if(~flagGeneLoop == 'off',
							{loopSample=~loopSynthSons.wrapAt(~soundsPositions)},
							{loopSample = (~genomes.wrapAt(agent).wrapAt(16) * 2 - 1).sign});
						//Offset
						if(~flagGeneOffset == 'off',
							{offset=~posSamplesSons.wrapAt(~soundsPositions);
								if(reverseSample == 1.neg, {offset=1 - offset})},
							{offset=  ~genomes.wrapAt(agent).wrapAt(17);
								if(reverseSample == 1.neg, {offset=1 - offset})})});
					// Synth
					if(~flagGeneSynth == 'off',
						{synth = ~synthInstruments},
						{synth =  ~listSynth.wrapAt((~genomes.wrapAt(agent).wrapAt(34)*~listSynth.size - 1).ceil.clip(0, ~listSynth.size - 1))});
					indexSynth = ~listSynth.detectIndex({arg item, i; item==synth});
					if(indexSynth == nil, {indexSynth=0});
					// Controls Synth
					if(~flagGeneControl == 'off',
						{controlF = ~controlsSynth.wrapAt(indexSynth).wrapAt(0); controlA = ~controlsSynth.wrapAt(indexSynth).wrapAt(1); controlD = ~controlsSynth.wrapAt(indexSynth).wrapAt(2);
							// Random Controls Synth
							if(~automationControlsSynth.wrapAt(indexSynth) == 1,
								{
									controlF = (controlF + (~automationJitterControlsSynth.wrapAt(indexSynth) * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
									controlA = (controlA + (~automationJitterControlsSynth.wrapAt(indexSynth) * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
									controlD = (controlD + (~automationJitterControlsSynth.wrapAt(indexSynth) * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
							});
						},
						{controlF = ~genomes.wrapAt(agent).wrapAt(35); controlA = ~genomes.wrapAt(agent).wrapAt(36); controlD = ~genomes.wrapAt(agent).wrapAt(37);
							// Random Controls Synth
							if(~valueRandomControlsSynth == 1,
								{
									controlF = (controlF + (~valueJitterControlsSynth * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
									controlA = (controlA + (~valueJitterControlsSynth * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
									controlD = (controlD + (~valueJitterControlsSynth * rrand(1.0.neg, 1.0))).clip(0.01, 0.99);
							});
					});
					// sourceOut
					if(~flagGeneOut == 'off',
						{indexOut=~listSynth.detectIndex({arg item, i; item==synth});if(indexOut == nil, {indexOut=0});
							audioOut = ~audioOutSynth.wrapAt(indexOut)},
						{audioOut =  ~genomes.wrapAt(agent).wrapAt(38)*~sourceOutAgents.size - 1});
					// source Input
					if(~flagGeneInput == 'off',
						{~synthRecAudioAgents.wrapAt(agent).setn(\in, ~audioInLR.wrapAt(~audioInputSons.wrapAt(~soundsPositions)) - 1); sourceInAgent = ~audioInLR.wrapAt(~audioInputSons.wrapAt(~soundsPositions)) - 1},
						{~synthRecAudioAgents.wrapAt(agent).setn(\in, ~audioInLR.wrapAt(~genomes.wrapAt(agent).wrapAt(40)) - 1); sourceInAgent = ~audioInLR.wrapAt(~genomes.wrapAt(agent).wrapAt(40)) - 1});
					// test if File In
					if(~flagEntreeMode == 'File', {sourceInAgent = ~busFileIn.index; flagInput = 1});
					//envelope
					if(~flagGeneEnvLevel == 'off',
						{envLevel=~levelenvelope},
						{envLevel=~genomes.wrapAt(agent).copyRange(18, 25)});
					if(~flagGeneEnvDuree == 'off',
						{envDuree = ~timeenvelope},
						{timeEnv = envDuree = ~genomes.wrapAt(agent).copyRange(26, 33).sort;
							~timeenvelope.size.do({arg i; envDuree.wrapPut(i, abs(timeEnv.wrapAt(i+1) - timeEnv.wrapAt(i)))});
					});
					//Freq
					freq = freq / 127 * freqRange + freqLow + freqTrans;
					if(~flagScaling != 'off', {
						freq = freq.collect({arg item, index;
							pos = 0;
							octave = item.midicps.cpsoct.round(0.001);
							ratio = octave.frac;
							octave = octave.floor;
							degre = (ratio * ~tuning.size + 0.5).floor;
							(~scale.degrees.size - 1).do({arg i;
								difL=abs(degre - ~scale.degrees.wrapAt(i));
								difH=abs(degre - ~scale.degrees.wrapAt(i+1));
								if(degre >= ~scale.degrees.wrapAt(i) and: {degre <= ~scale.degrees.wrapAt(i+1)},
									{if(difL <= difH, {pos = i},{pos = i+1})});
							});
							item = ~scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
							item = item.cpsmidi;
						});
					});
					freqRate=freq - 48 + ~root;
					freqRate=freqRate.clip(-127, 136);
					// Setup Canal Midi Out
					if(~flagGeneMidi == 'off',
						{~canalMidiOutAgent.wrapPut(agent, ~canalMidiOut)},
						{~canalMidiOutAgent.wrapPut(agent, ~genomes.wrapAt(agent).wrapAt(39)* 16 - 1)});
					// Set MIDI Off
					if(~flagMidiOut == 'on' and: {~canalMidiOutAgent.wrapAt(agent) >= 0}, {
						~freqMidi.wrapAt(agent).size.do({arg index; ~midiOut.noteOff(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), 0);
							if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), 0)});
						});// MIDI OFF
						// Reset MIDI OUT
						~freqMidi.wrapPut(agent, []);
						~freqMidi.wrapPut(agent, (freq + 0.5).floor);// Liste freqMidi agents
					});
					//Set fhz
					freq=freq.midicps.flat;
					freqRate=freqRate.midicps.flat;
					//Amp
					amp = amp * ampRange + ampLow;
					if(~flagAmpSynth == 'off', {ampReal = 1.0}, {ampReal = 0.0});
					// Duree
					duree = duree / ~dureeanalysemax * dureeRange + dureeLow;
					// Quanta Music
					duree=duree.floor + ((duree.frac*~quantaMusic+0.5).floor / ~quantaMusic);
					if(duree == 0, {duree = ~quantaMusic.reciprocal});
					duree = (duree * dureeTempo).max(0.025);
					~dureesmusique.wrapPut(agent, duree);// Duree pour playing
					duree = (duree * ~tempoMusicPlay.tempo.reciprocal).max(0.025);// duree pour synth
					// Set data for playing
					freq = freq.soloArray;
					amp = amp.soloArray;
					// Playing
					freq.size.do({arg index;var a, ar=0;
						// Amp
						a = amp.wrapAt(index);
						ar = ampReal;
						// Send MIDI On
						if(~flagMidiOut == 'on' and: {~canalMidiOutAgent.wrapAt(agent) >= 0}, {
							~midiOut.noteOn(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), a*127);// Send note MIDI ON
							if(flagVST == 'on', {~fxVST.midi.noteOn(~canalMidiOutAgent.wrapAt(agent), ~freqMidi.wrapAt(agent).wrapAt(index), a*127)});
						});
						// Synth
						Synth.new(synth, ['out', audioOut + ~startChannelAudioOut, 'buseffets', ~busEffetsAudio.index, 'busverb', ~busVerbAudio.index,'freq', freq.wrapAt(index), 'rate', freqRate.wrapAt(index), 'amp', a, 'ampreal', ar, 'duree', duree, 'panLo', pan.wrapAt(0), 'panHi', pan.wrapAt(1), 'offset', offset, 'loop', loopSample, 'reverse', reverseSample,  'buffer', bufferSon.bufnum,  'buffer2', bufferSon2.bufnum, 'controlF', controlF, 'controlA', controlA, 'controlD', controlD, 'antiClick1', ~antiClick.wrapAt(0), 'antiClick2', ~antiClick.wrapAt(1),'controlenvlevel1', envLevel.wrapAt(0), 'controlenvlevel2', envLevel.wrapAt(1), 'controlenvlevel3', envLevel.wrapAt(2), 'controlenvlevel4', envLevel.wrapAt(3), 'controlenvlevel5', envLevel.wrapAt(4),  'controlenvlevel6', envLevel.wrapAt(5),  'controlenvlevel7', envLevel.wrapAt(6),  'controlenvlevel8', envLevel.wrapAt(7), 'controlenvtime1', envDuree.wrapAt(0), 'controlenvtime2', envDuree.wrapAt(1), 'controlenvtime3', envDuree.wrapAt(2), 'controlenvtime4', envDuree.wrapAt(3), 'controlenvtime5', envDuree.wrapAt(4), 'controlenvtime6', envDuree.wrapAt(5), 'controlenvtime7', envDuree.wrapAt(6), 'in', sourceInAgent, 'flag', flagInput], ~groupeSynthAgents, \addToTail);//play
					});
			});
		};

		// PROCESSUS AGENTS
		~routineAgents=Tdef(\ProcessSystem, {
			var flagDead='off', time;
			loop({
				flagDead='off';
				// Evaluation des agents et traitement....
				// Phase 1 update Time-Vie
				~agents.do({arg agent; var elapsedTime;
					~signaux.wrapPut(agent, []);
					~voisins.wrapPut(agent, []);
					~signauxAffichage.wrapPut(agent, 0);
					~voisinsAffichage.wrapPut(agent, 0);
					~vies.wrapPut(agent, ~vies.wrapAt(agent) - (~tempoagents.value.reciprocal * ~genomes.wrapAt(agent).wrapAt(0) * ~vitessegeneration));
					if(~vies.wrapAt(agent) >= ~jeunesse,
						{~ages.wrapPut(agent, 'young');if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[0, 0, 1])})},// couleur bleu
						{if(~vies.wrapAt(agent) >= ~vieilliesse,
							{~ages.wrapPut(agent, 'adult');if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[0, 1, 0])})},//couleur vert
							{if(~vies.wrapAt(agent) > 0,
								{~ages.wrapPut(agent, 'old');if(~flagplayagent.wrapAt(agent) != 'on', {~couleurs.wrapPut(agent,[1, 1, 0])});// couleur jaune pale
									if((~mutation * ~tempoagents.value.reciprocal * ~vitessegeneration) >= 1.0.rand, {~mutationgenes.value(agent)})},
								{~ages.wrapPut(agent, 'dead')});
						});
					});
				});
				// Update population mort;
				~agents.do({arg agent;
					if(~ages.wrapAt(agent) == 'dead', {flagDead='on';
						~mortagents.value(agent);~agents=~agents - 1});
				});
				// Update Tdef agents;
				if(flagDead == 'on', {
					~agents.do({arg agent;
						~routineMusic.wrapPut(agent, ~createTdefAgent.value(agent, ~listeIDTdefAgents.wrapAt(agent)))});
				});
				//Phase 2 voisinage agents et signaux
				~agents.do({arg agent;
					~voisins.wrapPut(agent, ~routineAgentsAgents.value(agent));
					~signaux.wrapPut(agent, ~routineSignauxAgents.value(agent));
					~voisinsAffichage.wrapPut(agent, ~voisins.wrapAt(agent).size);
					~signauxAffichage.wrapPut(agent, ~signaux.wrapAt(agent).size);
					~fitnessAcquisvoisins.wrapPut(agent, ~sortfitnessAcquis.value(agent, ~voisins.wrapAt(agent)));
					~voisinsOkCopulation.wrapPut(agent, ~sortVoisinsCopulation.value(agent, ~voisins.wrapAt(agent)));
				});
				//Phase 3 apprentissage et accouplement
				{
					~agents.do({arg agent;
						if(~voisins.wrapAt(agent).size > 0,
							// Apprentissage
							{if(~fitnessAcquisvoisins.wrapAt(agent).size != 0 and: {(~learning * ~tempoagents.value.reciprocal * ~vitessegeneration) > 1.0.rand},
								{~apprentissageagents.value(agent, ~fitnessAcquisvoisins.wrapAt(agent).choose)});
							// Reproduction
							if(~agents < ~maximumagents and: {~voisinsOkCopulation.wrapAt(agent).size != 0} and: {~enfants.wrapAt(agent) < ~maximumenfants} and: {~ages.wrapAt(agent) == 'adult'} and: {~naissance > 1.0.rand},
								{~accouplement.value(agent,~voisinsOkCopulation.wrapAt(agent).choose)});
							},
							{if(~signaux.wrapAt(agent).size <= 0 ,
								{~fitnessInne.wrapPut(agent, (~fitnessInne.wrapAt(agent) - (~genomes.wrapAt(agent).wrapAt(0) * ~vitessegeneration)).thresh(0))});
						});
					});
				}.defer;
				time = Main.elapsedTime;// Set timing
				//Phase 4 Update fitnessInne playing, mouvement et liens parentes
				~agents.do({arg agent;
					// Geno
					~fitnessInne.wrapPut(agent, ~fitnessInne.wrapAt(agent) + ~voisins.wrapAt(agent).size + ~signaux.wrapAt(agent).size);
					//Pheno
					~fitnessAcquis.wrapPut(agent, ~compteur.wrapAt(agent) / ~listedatamusiqueagents);
					//Moving
					~routineMoveAgents.value(agent, ~voisins.wrapAt(agent), ~signaux.wrapAt(agent));
					// Liens Parents
					~liensParents.wrapAt(agent).do({arg lien, index;
						if(((time - lien) * ~vitessegeneration * ~tempoagents.value.reciprocal) >= (~dureeVieAgents * ~genomes.wrapAt(agent).wrapAt(0) * ~vitessegeneration * ~tempoagents.value.reciprocal), {~liensParents.wrapAt(agent).removeAt(index)});// Remove liens parents trop anciens
					});
				});
				// Test si silence
				if((time - ~lastTimeAnalyse) > ~tempsmaxsignal,
					{~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~compteurAnalyse=0});
				(~tempoagents.value.reciprocal * ~tempoMusicPlay.tempo).wait;
			});
		});

		// PROCESSUS UPDATE VIEW VIRTUAL
		~routineVirtual=Tdef(\VirtualWorld, {
			loop({
				// Update viewworld monde virtuel
				{~wm.refresh}.defer;
				(~tempoVirtual.reciprocal * ~tempoMusicPlay.tempo).wait;
			});
		});

		// PROCESSUS UPDATE AFFICHAGE DATAS
		~routineData=Tdef(\HiddenState, {
			loop({
				// Update affiche etats des agents
				{var genomes=nil, genBio=nil, musicOut=0;
					~viewpopulation.string = "Crew"+~agents.asString;
					~viewin.string = "Signal IN"+~compteurAnalyse;
					if(~agents > 0,{~agents.do({arg agent; musicOut = musicOut + ~musicOutAgents.wrapAt(agent)}); musicOut.round(0.001).asString});
					~viewout.string = "Music OUT"+musicOut;
					if(~genomes.size != 0, {genomes = ~genomes.mean.round(0.001);
						if(genomes.size != nil, {genBio = genomes.copyRange(0, 3).asString;
						});
					});
					~viewgenomes.string = "Genome (Bio)" + genBio + " " + "Fitness (Inborn + gain)"+~fitnessInne.normalize.mediane.asString +" "++~fitnessAcquis.normalize.mediane.asString;
					~viewagesagents.string = ~ages.mediane.asString;
					~viewviesagents.string = "Life"+~vies.mediane.round(0.001).asString;
					~viewenfantsagents.string = "Children"+~enfants.mediane.round(0.001).asString;
					~viewvoisins.string = "Neighboring"+~voisinsAffichage.mediane.round(0.01).asString;
					~viewsignaux.string = "Signal"+~signauxAffichage.mediane.round(0.01).asString;
				}.defer;
				(~tempoData.reciprocal * ~tempoMusicPlay.tempo).wait;
			});
		});

		// Automation Effets
		~routineAutomationEffets=Tdef(\AutoEffet, {
			// Random Effets
			loop({
				~listEffets.size.do({arg i;var val;
					if(~playSynthEffets.wrapAt(i) == 1, {
						if(~listeFXTime.at(i).value <= 0.01,
							{
								// Pan
								if(rrand(0.0, 1.0) <= 0.25, {
									if(~automationPanEffets.wrapAt(i) == 1, {
										val = (~panSynthEffets.wrapAt(i) + (~jitterPanSynthEffets.wrapAt(i) * rrand(-1.0, 1.0))).clip(-1.0, 1.0);
										~listSynthEffets.wrapAt(i).set('pan', val)});
								});
								// Controls
								if(~automationControlsEffets.wrapAt(i) == 1, {
									val = (~controlsSynthEffets.wrapAt(i) + (~jitterControlsSynthEffets.wrapAt(i) * rrand(-1.0, 1.0))).clip(0.05, 0.99);
									~listSynthEffets.wrapAt(i).set(\control1, val.wrapAt(0), \control2, val.wrapAt(1), \control3, val.wrapAt(2), \control4, val.wrapAt(3), \control5, val.wrapAt(4), \control6, val.wrapAt(5), \control7, val.wrapAt(6), \control8, val.wrapAt(7));
								});
								// Reset Time
								~listeFXTime.put(i, ~automationSpeedEffets.at(i).value.reciprocal);
							},
							{
								// Dec time effet
								~listeFXTime.put(i, ~listeFXTime.at(i) - 0.01);
						});
					});
				});
				100.reciprocal.wait;
			});
		});

		// Automation Verb
		~routineAutomationVerb=Tdef(\AutoVerb, {
			// Random Verb
			loop({
				~listVerb.size.do({arg i;var val;
					if(~playSynthVerb.wrapAt(i) == 1, {
						if(~listeVerbTime.at(i).value <= 0.01,
							{
								// Pan
								if(rrand(0.0, 1.0) <= 0.25, {
									if(~automationPanVerb.wrapAt(i) == 1, {
										val = (~panSynthVerb.wrapAt(i) + (~jitterPanSynthVerb.wrapAt(i) * rrand(-1.0, 1.0))).clip(-1.0, 1.0);
										~listSynthVerb.wrapAt(i).set('pan', val)});
								});
								// Controls
								if(~automationControlsVerb.wrapAt(i) == 1, {
									val = (~controlsSynthVerb.wrapAt(i) + (~jitterControlsSynthVerb.wrapAt(i) * rrand(-1.0, 1.0))).clip(0.05, 0.99);
									~listSynthVerb.wrapAt(i).set(\control1, val.wrapAt(0), \control2, val.wrapAt(1), \control3, val.wrapAt(2), \control4, val.wrapAt(3), \control5, val.wrapAt(4), \control6, val.wrapAt(5), \control7, val.wrapAt(6), \control8, val.wrapAt(7));
								});
								// Reset Time
								~listeVerbTime.put(i, ~automationSpeedVerb.at(i).value.reciprocal);
							},
							{
								// Dec time effet
								~listeVerbTime.put(i, ~listeVerbTime.at(i) - 0.01);
						});
					});
				});
				100.reciprocal.wait;
			});
		});

		// PROCESSUS AUTOMATION
		//  Version avec statistique sur les data ou signaux
		~routineAutomation=Tdef(\Automation, {
			var lastActifsAgents=9999, lastSignauxIn=9999, lastq1Freq=0, lastmedianeFreq=0, lastq3Freq=0, lastecartqFreq=0, lastecartsemiqFreq=0, lastvarianceFreq=0, lastecarttypeFreq=0, lastdissymetrieFreq=0, lastq1Amp=0, lastmedianeAmp=0, lastq3Amp=0, lastecartqAmp=0, lastecartsemiqAmp=0, lastvarianceAmp=0, lastecarttypeAmp=0, lastdissymetrieAmp=0, lastq1Duree=0, lastmedianeDuree=0, lastq3Duree=0, lastecartqDuree=0, lastecartsemiqDuree=0, lastvarianceDuree=0, lastecarttypeDuree=0, lastdissymetrieDuree=0, dureeLastConfig=0, dureeMaxPlayConfig=rrand(~dureeChangeConfig.wrapAt(0), ~dureeChangeConfig.wrapAt(1)), newTempoAutomation=1, lastFitness=0, lastVoisinsAffichage=0, lastSignauxAffichage=0,
			listeFreq=[], listeAmp=[], listeDuree=[];
			var actifsAgents=0, signauxIn=0, numberFile=0, flagFile='off', file, compteurEssai=0, q1Freq=0, medianeFreq=0, q3Freq=0, ecartqFreq=0, ecartsemiqFreq=0, varianceFreq=0, ecarttypeFreq=0, dissymetrieFreq=0, q1Amp=0, medianeAmp=0, q3Amp=0, ecartqAmp=0, ecartsemiqAmp=0, varianceAmp=0, ecarttypeAmp=0, dissymetrieAmp=0, q1Duree=0, medianeDuree=0, q3Duree=0, ecartqDuree=0, ecartsemiqDuree=0, varianceDuree=0, ecarttypeDuree=0, dissymetrieDuree=0, valueTierAgents=0, meanDureeBuffer=[], meanDureeAgents=0, fitness=0, voisinsAffichage=0, signauxAffichage=0,  meanSynthAgents=[], x, y, lastFlatness = 0, newQuantaDur=100;
			loop({
				actifsAgents=0; signauxIn=0; numberFile=0; flagFile='off'; file; compteurEssai=0; q1Freq=0; medianeFreq=0; q3Freq=0; ecartqFreq=0; ecartsemiqFreq=0; varianceFreq=0; ecarttypeFreq=0; dissymetrieFreq=0; q1Amp=0; medianeAmp=0; q3Amp=0; ecartqAmp=0; ecartsemiqAmp=0; varianceAmp=0; ecarttypeAmp=0; dissymetrieAmp=0; q1Duree=0; medianeDuree=0; q3Duree=0; ecartqDuree=0; ecartsemiqDuree=0;varianceDuree=0; ecarttypeDuree=0; dissymetrieDuree=0; newQuantaDur = 100; valueTierAgents=0; meanDureeBuffer=[]; meanDureeAgents=0; fitness=0; voisinsAffichage=0; signauxAffichage=0;  meanSynthAgents=[];
				listeFreq=[]; listeAmp=[]; listeDuree=[];
				// New Root
				if(~flagRootAutomation == 'on', {
					{
						if(~flagScaling == 'on', {~rootChoice.valueAction_((~freqCentroid.cpsoct.frac * 12).ceil.mod(12).floor)});
					}.defer;
					~scale = Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning);
				});
				// Analyse agents
				~agents.do({arg agent;
					actifsAgents = actifsAgents + ~musicOutAgents.wrapAt(agent);
					meanDureeBuffer=meanDureeBuffer.add(~genomes.wrapAt(agent).wrapAt(13));
					meanSynthAgents=meanSynthAgents.add(~genomes.wrapAt(agent).wrapAt(34));
					listeFreq=listeFreq++~listeagentfreq.wrapAt(agent);
					listeAmp=listeAmp++~listeagentamp.wrapAt(agent);
					listeDuree=listeDuree++~listeagentduree.wrapAt(agent);
				});
				meanDureeAgents=~listeagentduree.flat.mediane;
				meanDureeBuffer=meanDureeBuffer.mediane;
				meanSynthAgents= meanSynthAgents.flat.mean;
				signauxIn=~listefreq.size;
				// Switch probability on agents or soundin
				if(~flagAS == 'SoundIn', {
					// Probabilite sur signaux
					//Probabilite Freq
					# q1Freq, medianeFreq, q3Freq, ecartqFreq, ecartsemiqFreq = ~listefreq.quartiles;
					varianceFreq=~listefreq.variance;
					ecarttypeFreq=~listefreq.ecartType;
					dissymetrieFreq=~listefreq.dissymetrie*63.5;
					//Probabilite Amp
					# q1Amp, medianeAmp, q3Amp, ecartqAmp, ecartsemiqAmp = ~listeamp.quartiles;
					varianceAmp=~listeamp.variance;
					ecarttypeAmp=~listeamp.ecartType;
					dissymetrieAmp=~listeamp.dissymetrie;
					//Probabilite Duree
					# q1Duree, medianeDuree, q3Duree, ecartqDuree, ecartsemiqDuree = ~listeduree.quartiles;
					varianceDuree=~listeduree.variance;
					ecarttypeDuree=~listeduree.ecartType;
					dissymetrieDuree=~listeduree.dissymetrie*~dureeanalysemax;
					newQuantaDur = ((ecartsemiqDuree.reciprocal+0.5).floor / (ecartqDuree.reciprocal+0.5).floor + 0.5).floor *  (ecartqDuree.reciprocal+0.5).floor;
				}, {
					// Probabilite sur agents
					//Probabilite Freq
					# q1Freq, medianeFreq, q3Freq, ecartqFreq, ecartsemiqFreq = listeFreq.quartiles;
					varianceFreq=listeFreq.variance;
					ecarttypeFreq=listeFreq.ecartType;
					dissymetrieFreq=listeFreq.dissymetrie*63.5;
					//Probabilite Amp
					# q1Amp, medianeAmp, q3Amp, ecartqAmp, ecartsemiqAmp = listeAmp.quartiles;
					varianceAmp=listeAmp.variance;
					ecarttypeAmp=listeAmp.ecartType;
					dissymetrieAmp=listeAmp.dissymetrie;
					//Probabilite Duree
					# q1Duree, medianeDuree, q3Duree, ecartqDuree, ecartsemiqDuree = listeDuree.quartiles;
					varianceDuree=listeDuree.variance;
					ecarttypeDuree=listeDuree.ecartType;
					dissymetrieDuree=listeDuree.dissymetrie*~dureeanalysemax;
					newQuantaDur = ((ecartsemiqDuree.reciprocal+0.5).floor / (ecartqDuree.reciprocal+0.5).floor + 0.5).floor *  (ecartqDuree.reciprocal+0.5).floor;
				});
				// Mean Agents
				valueTierAgents=(~agents/3).ceil;
				// Setup pour variables monde
				fitness = ~fitnessInne.normalize.mediane;
				voisinsAffichage = ~voisinsAffichage.mediane;
				signauxAffichage=~signauxAffichage.mediane;
				// Brightness of signal = ~freqCentroid ( freq en hertz)
				// Entropie du son  = ~flatness (0 -> sinus | 1 -> whiteNoise)
				// Freq max energie spectral = ~energy (freq en hertz)
				////////////////////////// Automation processus
				{
					////////////////////////// Genes Musique Agents
					if(~genesMAutomation.value == 1, {
						// Freq
						if(abs(varianceFreq - lastvarianceFreq) <= 0.0025,
							{~geneFreqButton.valueAction_(1)},{~geneFreqButton.valueAction_(0)});
						if(abs(ecartsemiqFreq - lastecartsemiqFreq) <= 0.0025, {~geneTransFreqButton.valueAction_(1)},{~geneTransFreqButton.valueAction_(0)});
						//// Amp
						//if(abs(ecartsemiqAmp - lastecartsemiqAmp) <= 0.0025, {~geneAmpButton.valueAction_(1)},{~geneAmpButton.valueAction_(0)});
						// Duree
						if(abs(varianceDuree - lastvarianceDuree) <= 0.0025, {~geneDureeButton.valueAction_(1)},{~geneDureeButton.valueAction_(0)});
						if(abs(ecartsemiqDuree - lastecartsemiqDuree) <= 0.0025, {~geneMulDureeButton.valueAction_(1)},{~geneMulDureeButton.valueAction_(0)});
					});
					///////////////////////// Genes Synth Agents
					if(~genesSAutomation.value == 1, {
						// Loop Buffers Agents
						if(meanDureeAgents <= meanDureeBuffer, {~geneLoopButton.valueAction_(1)},{if(0.125.coin, {~geneLoopButton.valueAction_(1)},{~geneLoopButton.valueAction_(0)})});
						//Synth Agents
						if(dissymetrieFreq.sign != lastdissymetrieFreq.sign, {~geneSynthButton.valueAction_(1)},{~geneSynthButton.valueAction_(0)});
						////envelope Duree Agents
						//if(dissymetrieDuree.sign != lastdissymetrieDuree.sign, {~geneEnvDureeButton.valueAction_(1)},{~geneEnvDureeButton.valueAction_(0)});
						////envelope Amp Agents
						//if(dissymetrieAmp.sign != lastdissymetrieAmp.sign, {~geneEnvLevelButton.valueAction_(1)},{~geneEnvLevelButton.valueAction_(0)});
						// Pan Agents
						if(actifsAgents >= valueTierAgents, {if(0.75.coin, {~genePanButton.valueAction_(1)},{if(0.125.coin, {~genePanButton.valueAction_(1)},{~genePanButton.valueAction_(0)})})}, {if(0.125.coin, {~genePanButton.valueAction_(1)},{~genePanButton.valueAction_(0)})});
						// Offset Buffers Agents
						if(meanDureeBuffer >= (~tempsmaxsignal/3), {if(0.75.coin, {~geneOffsetButton.valueAction_(1)},{if(0.125.coin, {~geneOffsetButton.valueAction_(1)},{~geneOffsetButton.valueAction_(0)})})}, {if(0.125.coin, {~geneOffsetButton.valueAction_(1)},{~geneOffsetButton.valueAction_(0)})});
						// Reverse Buffers Agents
						if(meanDureeAgents >= (~tempsmaxsignal/3), {if(0.75.coin, {~geneReverseButton.valueAction_(1)},{if(0.125.coin, {~geneReverseButton.valueAction_(1)},{~geneReverseButton.valueAction_(0)})})}, {if(0.125.coin, {~geneReverseButton.valueAction_(1)},{~geneReverseButton.valueAction_(0)})});
					});
					//////////////////// Monde agents sliders
					if(~mondesAgentsAutomation.value == 1, {
						// Voisinage agents vs agents
						if(~voisinsAffichage.mediane <= 0.5 and: {lastVoisinsAffichage <= 0.5}, {~distanceAgentsSlider.valueAction=~distanceAgentsSlider.value+rrand(0, ~distanceAgentsSlider.value)}, {~distanceAgentsSlider.valueAction=rrand(0.015625, 0.5)});
						// Voisinage agents vs signaux
						if(~signauxAffichage.mediane <= 0.5 and: {lastSignauxAffichage <= 0.5}, {~distanceSignauxSlider.valueAction=~distanceSignauxSlider.value+rrand(0, ~distanceSignauxSlider.value)}, {~distanceSignauxSlider.valueAction=rrand(0.015625, 0.5)});
						// Deviance agents vs agents
						if(voisinsAffichage >= valueTierAgents and: {lastVoisinsAffichage >= valueTierAgents}, {~devianceSlider.valueAction=~devianceSlider.value+rrand(0, ~devianceSlider.value)}, {~devianceSlider.valueAction=rrand(0.015625, 0.125)});
					});
					//////////////////// Monde Musique sliders
					if(~mondesMusiqueAutomation.value == 1, {
						// Loop Music ????
						if(actifsAgents <= valueTierAgents and: {lastActifsAgents <= valueTierAgents}, {if(0.75.coin, {~loopseq.valueAction_(1)},{if(0.125.coin, {~loopseq.valueAction_(1)},{~loopseq.valueAction_(0)})})}, {if(signauxIn != 0, {if(0.5.coin, {~loopseq.valueAction_(1)},{~loopseq.valueAction_(0)})})});
						// Sheared musique agents vs agents
						if(actifsAgents <= valueTierAgents and: {fitness <= 0.9} and: {lastFitness <= 0.9}, {if(0.75.coin, {~sharedMusicButton.valueAction_(1)},{if(0.125.coin, {~sharedMusicButton.valueAction_(1)},{~sharedMusicButton.valueAction_(0)})})}, {if(0.125.coin, {~sharedMusicButton.valueAction_(1)},{~sharedMusicButton.valueAction_(0)})});
					});
					/////////////////////////////// Synth musique sliders
					if(~synthMusiqueAutomation.value == 1, {
						// Freq Synth
						if(abs(ecartqFreq - lastecartqFreq) >= 0.0025, {
							x=[q1Freq, lastq1Freq].mediane;
							y=[q3Freq, lastq3Freq].mediane;
							~freqInstr.valueAction_([x, y]*127);
						});
						// Transpose Freq Synth
						if(abs(ecartsemiqFreq - lastecartsemiqFreq) >= 0.0025, {
							~freqTransposeInstr.valueAction_((ecartsemiqFreq*60)*[dissymetrieFreq, lastdissymetrieFreq].mean.sign);
						});
						// Duree Synth
						if(abs(ecartqDuree - lastecartqDuree) >= 0.0025, {
							x=[q1Duree, lastq1Duree].mediane * ~dureeanalysemax;
							y=[q3Duree, lastq3Duree].mediane * ~dureeanalysemax;
							~dureeInstr.valueAction_([x, y]);
						});
						// Stretch Duree Synth
						if(abs(ecartsemiqDuree - lastecartsemiqDuree) >= 0.0025, {
							~dureeTempoinstr.value=(ecartqDuree * ~dureeanalysemax * ~tempoMusicPlay.tempo) * [dissymetrieDuree, lastdissymetrieDuree].mean.sign});
						// Quanta Duree Synth
						if(newQuantaDur > 0 and: {newQuantaDur <= 100}, {~quantaMusicSlider.value=newQuantaDur});
					});
					/////////////////////////////// Synth agents sliders
					if(~synthAgentsAutomation.value == 1, {
						// Change synth
						if(0.666.coin, {~synthDefInstrMenu.valueAction_((meanSynthAgents * (~geneSynthRangerHigh.value - ~geneSynthRangerLow.value) + ~geneSynthRangerLow.value).ceil.clip(~geneSynthRangerLow.value, ~geneSynthRangerHigh.value))},{~synthDefInstrMenu.valueAction_((1.0.rand * (~geneSynthRangerHigh.value - ~geneSynthRangerLow.value) + ~geneSynthRangerLow.value + 0.5).floor.clip(~geneSynthRangerLow.value, ~geneSynthRangerHigh.value))});
						// Loop samples
						if(actifsAgents <= valueTierAgents and: {lastActifsAgents <= valueTierAgents}, {if(0.75.coin, {~loopSynthButton.valueAction_(1)},{if(0.125.coin, {~loopSynthButton.valueAction_(1)},{~loopSynthButton.valueAction_(0)})})}, {if(0.125.coin, {~loopSynthButton.valueAction_(1)},{~loopSynthButton.valueAction_(0)})});
						// Reverse samples
						if(actifsAgents > valueTierAgents and: {lastActifsAgents > valueTierAgents}, {if(0.75.coin, {~reverseSynthButton.valueAction_(1)},{if(0.125.coin, {~reverseSynthButton.valueAction_(1)},{~reverseSynthButton.valueAction_(0)})})}, {if(0.125.coin, {~reverseSynthButton.valueAction_(1)},{~reverseSynthButton.valueAction_(0)})});
					});
					//////////////////////////// Init agents
					if(~initAutomation.value == 1, {
						// Agents mort -->> Init agents
						if(~agents <= 1  and: {0.5.coin}, {~initsysteme.valueAction_(1)});
						////////////////////// Zero music agents
						if(actifsAgents <= 0 and: {lastActifsAgents == 0}, {~initsysteme.valueAction_(1);lastActifsAgents=9999},{lastActifsAgents = actifsAgents});
					}, {lastActifsAgents = actifsAgents});
					// Update variables
					lastq1Freq=q1Freq; lastmedianeFreq=medianeFreq; lastq3Freq=q3Freq; lastecartqFreq=ecartqFreq; lastecartsemiqFreq=ecartsemiqFreq; lastvarianceFreq=varianceFreq; lastecarttypeFreq=ecarttypeFreq; lastdissymetrieFreq=dissymetrieFreq;
					lastq1Amp=q1Amp; lastmedianeAmp=medianeAmp; lastq3Amp=q3Amp; lastecartqAmp=ecartqAmp; lastecartsemiqAmp=ecartsemiqAmp; lastvarianceAmp=varianceAmp; lastecarttypeAmp=ecarttypeAmp; lastdissymetrieAmp=dissymetrieAmp;
					lastq1Duree=q1Duree; lastmedianeDuree=medianeDuree; lastq3Duree=q3Duree; lastecartqDuree=ecartqDuree; lastecartsemiqDuree=ecartsemiqDuree; lastvarianceDuree=varianceDuree; lastecarttypeDuree=ecarttypeDuree; lastdissymetrieDuree=dissymetrieDuree;
					lastFitness=fitness;lastVoisinsAffichage=voisinsAffichage;lastSignauxAffichage=signauxAffichage;
				}.defer;
				// Set new tempo automation
				newTempoAutomation=rrand(~densite.value.wrapAt(0), ~densite.value.wrapAt(1));
				(newTempoAutomation.reciprocal * ~tempoMusicPlay.tempo).wait;
			});
		});
		// Routine Metronome + Time
		~routineMetronome = Tdef('Metronome', {arg hms, hour, minute, second;
			loop({
				{~metronomeGUI.value_(~tempoMusicPlay.beatInBar+1);
					hms = Main.elapsedTime - ~timeElapsedStart;
					hour = hms / 3600;~hourElapsed.value = hour.floor;
					minute = hour.frac * 60;~minuteElapsed.value = minute.floor;
					second = minute.frac * 60;~secondElapsed.value = second.round(0.01)}.defer;
				(~tempoagents.reciprocal * ~tempoMusicPlay.tempo).wait;
		})});

		// Stop programme
		~cmdperiodfunc = {arg file;
			if(~flagRecording == 'on', {
				s.stopRecording;~bufferRecording.close;~bufferRecording.free});
			~wp.close;~wm.close;~wcm.close;~wi.close;~we.close;~wg.close;~wad.close;~wv.close;~windowMasterFX.close; windowKeyboard.close;
			if(~flagHPgenomeEditor == 'on', {~wEditor.close;~routineGenome.remove});
			if(~flagHPsequenceEditor == 'on', {~wSequence.close;~routineSequence.remove});
			if(~flagHPscoreEditor == 'on', {~wScore.close;~routineScore.clear});
			if(~flagHPliveCoding == 'on', {~wCoding.close});
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
			MIDIIn.disconnect;
			~serverAdresse.disconnect;
			if(~masterAppAddr != nil, {~masterAppAddr.disconnect});
			~slaveAppAddr.disconnect;
			~oscAudioIn.remove;
			~oscTempoIn.remove;
			~oscMidiIn.remove;
			~oscHPtempo.remove;
			~oscHPstart.remove;
			~oscHPbare.remove;
			~oscHPrec.remove;
			Tdef.removeAll;
			MIDIdef.freeAll;
			~menuAgents.remove;// remove custom menu
			windowVST.close;
			ProxySpace.clearAll;
			//s.quit;
		};

		CmdPeriod.doOnce(~cmdperiodfunc);

		~wp.front;

		~routineAgentsAgents=~voisinsagentsNoTore;
		~routineSignauxAgents=~signauxvoisinsNoTore;
		~routineMoveAgents=~movingagentsNoTore;
		~routineSignauxFlock=~movingsignauxNoFlock;
		~routineAgentsFlock=~movingagentsNoFlock;

		s.bind{arg file;
			// Create Init Monde Agents
			Post << "Init Control Panel" <<  Char.nl;
			file=File(~nompathdata++"init control panel.scd","w");
			file.write(~foncSaveMonde.value.asCompileString);file.close;
			s.sync;
			Post << "Init Preset" <<  Char.nl;
			// Create Init Preset Agents
			file=File(~nompathdata++"init preset.scd","w");
			file.write(~foncSaveUnivers.value(~foncSaveMonde.value, 'on', 'on').value.asCompileString);file.close;
			s.sync;
			~initsysteme.valueAction_(1);
		};

		s.queryAllNodes;

	}

	// Creation ALL GUI

	fxPanel {

		// MasterFX
		~windowMasterFX = Window.new("MasterFX", Rect(500, 0, 135, 80), scroll: true);
		~windowMasterFX.view.decorator = FlowLayout(~windowMasterFX.view.bounds);
		~windowMasterFXLimit =EZKnob(~windowMasterFX, 60 @ 70, "LimitOut",\db,
			{|ez| ~masterFX.set(\limit, ez.value.dbamp);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~windowMasterFXLimit", ez.value)})},-3, labelWidth: 60, unitWidth: 0, layout: 'vert');
		~windowMasterFXPostAmp = EZKnob(~windowMasterFX, 60 @ 70, "PostAmp", \db,
			{|ez| ~masterFX.set(\postAmp, ez.value.dbamp);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~windowMasterFXPostAmp", ez.value)})}, 0, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~windowMasterFX.front;
	}

	spacePanel {

		// Window monde virtuel
		~wm = Window("Virtual Space", Rect(280, 130, ~mx + 5, ~my + 5), scroll: true);
		~wm.view.background_ (Color.black);
		StaticText(~wm, Rect(0, 0, 40, 10)).string_("Signal").stringColor_(Color.red);// red
		StaticText(~wm, Rect(45, 0, 40, 10)).string_("Playing").stringColor_(Color.new(1, 1, 1));// white
		StaticText(~wm, Rect(90, 0, 40, 10)).string_("Young").stringColor_(Color.new(0,0,1));// bleu fonce
		StaticText(~wm, Rect(135, 0, 30, 10)).string_("Adult").stringColor_(Color.new(0, 1, 0));// Green
		StaticText(~wm, Rect(165, 0, 30, 10)).string_("Old").stringColor_(Color.new(1, 1, 0));// yellow
		StaticText(~wm, Rect(0, 12, 40, 10)).string_("Neighbour").stringColor_(Color.new(1, 0, 1));// magenta
		StaticText(~wm, Rect(45, 12, 40, 10)).string_("Hearing").stringColor_(Color.new(0, 1, 1));// bleu clair
		~wm.drawFunc = {
			var signal, matriceX, matriceY, matriceZ, vecteur, x, y, z, o1, o2, o3, o4, o5, o6, o7, o8, agent;
			Pen.matrix = ~matrix;//Tranformation coordonnees du monde
			// Affiche signal
			~listefreq.size.do({arg i;
				vecteur=[~listeduree.wrapAt(i) / ~dureeanalysemax, ~listefreq.wrapAt(i), ~listeamp.wrapAt(i)];
				matriceX=Pen.mRotateX(~angleX);
				matriceY=Pen.mRotateY(~angleY);
				matriceZ=Pen.mRotateZ(~angleZ);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
				z=vecteur[2];
				x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
				y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
				signal = Rect(x * ~mx, ~my - (y * ~my), ~point * z + 1, ~point * z + 1);
				Color.red.alpha_(1.0).set;
				Pen.fillOval(signal);
			});
			// Affiche agents
			~agents.do({arg i;
				vecteur=[~agentspositionx.wrapAt(i) / ~dureeanalysemax, ~agentspositiony.wrapAt(i), ~agentspositionz.wrapAt(i)];
				matriceX=Pen.mRotateX(~angleX);
				matriceY=Pen.mRotateY(~angleY);
				matriceZ=Pen.mRotateZ(~angleZ);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
				vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
				z=vecteur[2];
				x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
				y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
				agent = Rect(x * ~mx, ~my - (y * ~my), ~point * z + 1, ~point * z + 1);
				Color.new(~couleurs.wrapAt(i).wrapAt(0), ~couleurs.wrapAt(i).wrapAt(1), ~couleurs.wrapAt(i).wrapAt(2), 1.0).set;
				Pen.fillOval(agent);
			});
			//  origine 0 0 0
			vecteur=[0, 0, 0];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o1 = Point(x * ~mx, ~my - (y * ~my));
			// Display origine
			Color.yellow.(0.8, 0.25).set;
			Pen.fillRect(Rect(x * ~mx, ~my - (y * ~my), 3, 3));
			//  origine 1 0 0
			vecteur=[1, 0, 0];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o2 = Point(x * ~mx, ~my - (y * ~my));
			//  origine 1 1 0
			vecteur=[1, 1, 0];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o3 = Point(x * ~mx, ~my - (y * ~my));
			//  origine 0 1 0
			vecteur=[0, 1, 0];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o4= Point(x * ~mx, ~my - (y * ~my));
			//  origine 0 0 1
			vecteur=[0, 0, 1];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o5= Point(x * ~mx, ~my - (y * ~my));
			//  origine 0 1 1
			vecteur=[0, 1, 1];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o6= Point(x * ~mx, ~my - (y * ~my));
			//  origine 1 1 1
			vecteur=[1, 1, 1];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o7= Point(x * ~mx, ~my - (y * ~my));
			//  origine 1 0 1
			vecteur=[1, 0, 1];
			matriceX=Pen.mRotateX(~angleX);
			matriceY=Pen.mRotateY(~angleY);
			matriceZ=Pen.mRotateZ(~angleZ);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceX);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceY);
			vecteur=Pen.vectorMatrixMul(vecteur, matriceZ);
			z=vecteur[2];
			x=vecteur[0] * ~focalDistance / (z * ~point + ~mx);
			y=vecteur[1] * ~focalDistance / (z * ~point + ~my);
			o8= Point(x * ~mx, ~my - (y * ~my));
			// Affiche cadre
			Pen.strokeColor = Color.grey(0.8, 0.25);
			Pen.moveTo(o2);
			Pen.lineTo(o8);
			Pen.lineTo(o5);
			Pen.lineTo(o6);
			Pen.lineTo(o7);
			Pen.lineTo(o8);
			Pen.moveTo(o4);
			Pen.lineTo(o6);
			Pen.moveTo(o3);
			Pen.lineTo(o7);
			Pen.stroke;
			Pen.strokeColor = Color.yellow(0.8, 0.25);
			Pen.moveTo(o5);
			Pen.lineTo(o1);
			Pen.lineTo(o2);
			Pen.lineTo(o3);
			Pen.lineTo(o4);
			Pen.lineTo(o1);
			Pen.stroke;
		};
		~wm.front;

		//Control monde virtuel
		~wcm = Window("Control Virtual Space", Rect(0, 130, 325, 170), scroll: true);
		~wcm.view.decorator = FlowLayout(~wcm.view.bounds);
		~zoomXSlider=EZKnob(~wcm,60 @ 75, "ZoomX",ControlSpec(0, 10, \lin, 0),
			{|ez| ~matrix.wrapPut(0,ez.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~zoomXSlider", ez.value)})},1, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~zoomYSlider=EZKnob(~wcm,60 @ 75, "ZoomY",ControlSpec(0, 10, \lin, 0),
			{|ez| ~matrix.wrapPut(3,ez.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~zoomYSlider", ez.value)})},1, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~translateXSlider=EZKnob(~wcm,60 @ 75, "TranslateX",ControlSpec(~mx.neg*~zoomXSlider.value, ~mx*~zoomXSlider.value, \lin, 0),
			{|ez| ~matrix.wrapPut(4,ez.value*~zoomXSlider.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~translateXSlider", ez.value)})},0, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~translateYSlider=EZKnob(~wcm,60 @ 75, "TranslateY",ControlSpec(~my.neg*~zoomYSlider.value, ~my*~zoomYSlider.value, \lin, 0),
			{|ez| ~matrix.wrapPut(5,ez.value.neg*~zoomYSlider.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~translateYSlider", ez.value)})}, 25, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~shearingXSlider=EZKnob(~wcm,60 @ 75, "ShearingX",ControlSpec(-2, 2, \lin, 0),
			{|ez| ~matrix.wrapPut(1,ez.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~shearingXSlider", ez.value)})},0, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~shearingYSlider=EZKnob(~wcm,60 @ 75, "ShearingY",ControlSpec(-2, 2, \lin, 0),
			{|ez| ~matrix.wrapPut(2,ez.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~shearingYSlider", ez.value)})},0, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~angleXSlider=EZKnob(~wcm,60 @ 75, "AngleX",ControlSpec(360.neg, 360, \lin, 0),
			{|ez| ~angleX = ez.value / 360 * 2pi;if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~angleXSlider", ez.value)})}, 22, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~angleYSlider=EZKnob(~wcm,60 @ 75, "AngleY",ControlSpec(360.neg, 360, \lin, 0),
			{|ez| ~angleY = ez.value / 360 * 2pi;if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~angleYSlider", ez.value)})}, 45, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~angleZSlider=EZKnob(~wcm,60 @ 75, "AngleZ",ControlSpec(360.neg, 360, \lin, 0),
			{|ez| ~angleZ = ez.value / 360 * 2pi;if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~angleZSlider", ez.value)})}, 16, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~focalDistanceSlider=EZKnob(~wcm,60 @ 75, "Focus",ControlSpec(0, 500, \lin, 0),
			{|ez| ~focalDistance = ez.value;if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~focalDistanceSlider", ez.value)})},150, labelWidth: 60,unitWidth: 0, layout: 'vert');
		~wcm.front;
	}

	genesPanel {

		//Activation genomes Agents-Synth
		~wg = Window("Activate Genes", Rect(1030, 800, 355, 675), scroll: true);
		~wg.view.decorator = FlowLayout(~wg.view.bounds);
		// Bio
		StaticText(~wg, Rect(0, 0, 300, 15)).string_("Biological Genome").stringColor_(Color.white).font_(Font("Georgia", 12));
		~wg.view.decorator.nextLine;
		~geneVieillissement=EZRanger(~wg, 300 @ 18, "Ageing", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneVieillissement", view.value)})}, [0, 1], false, 125, 40);
		~geneDeplacement=EZRanger(~wg, 300 @ 18, "Moving", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneDeplacement", view.value)})}, [0, 1], false, 125, 40);
		~geneVision=EZRanger(~wg, 300 @ 18, "Vision", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneVision", view.value)})}, [0, 1], false, 125, 40);
		~geneAudition=EZRanger(~wg, 300 @ 18, "Hearing", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAudition", view.value)})}, [0, 1], false, 125, 40);
		// Music
		StaticText(~wg, Rect(0, 0, 300, 15)).string_("Musical Genome").stringColor_(Color.white).font_(Font("Georgia", 12));
		~wg.view.decorator.nextLine;
		~geneFreqRanger=EZRanger(~wg, 300 @ 18, "Frequency", ControlSpec(0, 127, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneFreqRanger", view.value)})}, [24, 108], false, 125, 40);
		~geneFreqButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneFreqButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneFreqButton", action.value)});
			if(action.value == 1 ,{~flagGeneFreq='on';~freqInstr.enabled_(false);~geneFreqRanger.enabled_(true)},{~flagGeneFreq='off';~freqInstr.enabled_(true);~geneFreqRanger.enabled_(false)});
		};
		~geneFreqButton.focus;
		~geneFreqRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneTransFreqRanger=EZRanger(~wg, 300 @ 18, "Translate Frequency", ControlSpec(-127, 127, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneTransFreqRanger", view.value)})}, [-12, 12], false, 125, 40);
		~geneTransFreqButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneTransFreqButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneTransFreqButton", action.value)});
			if(action.value == 1 ,{~flagGeneTransFreq='on';~freqTransposeInstr.enabled_(false);~geneTransFreqRanger.enabled_(true)},{~flagGeneTransFreq='off';~freqTransposeInstr.enabled_(true);~geneTransFreqRanger.enabled_(false)});
		};
		~geneTransFreqRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneAmpRanger=EZRanger(~wg, 300 @ 18, "Amplitude", ControlSpec(-inf, 0, \db, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAmpRanger", view.value)})}, [-inf, 0], false, 125, 40);
		~geneAmpButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneAmpButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAmpButton", action.value)});
			if(action.value == 1 ,{~flagGeneAmp='on';~ampInstr.enabled_(false);~geneAmpRanger.enabled_(true)},{~flagGeneAmp='off';~ampInstr.enabled_(true);~geneAmpRanger.enabled_(false)});
		};
		~geneAmpRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneDureeRanger=EZRanger(~wg, 300 @ 18, "Duration", ControlSpec(0, 60, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneDureeRanger", view.value)})}, [0, 4], false, 125, 40);
		~geneDureeButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneDureeButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneDureeButton", action.value)});
			if(action.value == 1 ,{~flagGeneDuree='on';~dureeInstr.enabled_(false);~geneDureeRanger.enabled_(true)},{~flagGeneDuree='off';~dureeInstr.enabled_(true);~geneDureeRanger.enabled_(false)});
		};
		~geneDureeRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneMulDureeRanger=EZRanger(~wg, 300 @ 18, "Stretch Duration", ControlSpec(-16, 64, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneMulDureeRanger", view.value)})}, [-2, 4], false, 125, 40);
		~geneMulDureeButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneMulDureeButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneMulDureeButton", action.value)});
			if(action.value == 1 ,{~flagGeneMulDuree='on';~dureeTempoinstr.enabled_(false);~geneMulDureeRanger.enabled_(true)},{~flagGeneMulDuree='off';~dureeTempoinstr.enabled_(true);~geneMulDureeRanger.enabled_(false)});
		};
		~geneMulDureeRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~genePanRanger=EZRanger(~wg, 300 @ 18, "Pan", ControlSpec(-1, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~genePanRanger", view.value)})}, [-1, 1], false, 125, 40);
		~genePanButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~genePanButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~genePanButton", action.value)});
			if(action.value == 1 ,{~flagGenePan='on';~panInstr.enabled_(false);~genePanRanger.enabled_(true)},{~flagGenePan='off';~panInstr.enabled_(true);~genePanRanger.enabled_(false)});
		};
		~genePanRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneBufferRanger=EZRanger(~wg, 300 @ 18, "Time Buffer Sound", ControlSpec(0.01, ~tempsmaxsignal, \exp, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBufferRanger", view.value)})}, [0.015625, ~tempsmaxsignal], false, 125, 40);
		~geneBufferRanger.view.children.at(1).decimals = 4;
		~geneBufferRanger.view.children.at(3).decimals = 4;
		~geneBufferButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneBufferButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBufferButton", action.value)});
			if(action.value == 1 ,{~flagGeneBuffer='on';
				~geneInputButton.enabled_(true);
				if(~flagGeneInput=='on', {~audioInputButton.enabled_(false);~geneInputRangerLow.enabled_(true);~geneInputRangerHigh.enabled_(true)});
				~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).set(\triggerRec, 1)});
				~geneSampleButton.valueAction_(0);~geneBufferRanger.enabled_(true);~evaluationSynthAgent.value(nil, ~synthInstruments)},
			{~flagGeneBuffer='off';
				~audioInputButton.enabled_(true);~geneInputRangerLow.enabled_(false);~geneInputRangerHigh.enabled_(false);~geneInputButton.enabled_(false);
				~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(false); ~synthRecFileAgents.wrapAt(i).set(\triggerRec, 0); ~synthRecFileAgents.wrapAt(i).run(false)});
				~geneBufferRanger.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneBufferRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneSampleRangerLow=PopUpMenu(~wg,Rect(0,0, 148, 18)).items = ~displaySons;
		~geneSampleRangerLow.action = { arg item; ~geneSamplePopUpLow = item.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSampleRangerLow", item.value)})};
		~geneSampleRangerHigh=PopUpMenu(~wg,Rect(0,0, 148, 18)).items = ~displaySons;
		~geneSampleRangerHigh.action = { arg item; ~geneSamplePopUpHigh = item.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSampleRangerHigh", item.value)})};
		~geneSampleButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneSampleButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSampleButton", action.value)});
			if(action.value == 1 ,{~flagGeneSample='on';~geneBufferButton.valueAction_(0);~geneSampleRangerLow.enabled_(true);~geneSampleRangerHigh.enabled_(true);~evaluationSynthAgent.value(nil, ~synthInstruments)},{~flagGeneSample='off';~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneSampleRangerLow.font = Font("Geneva", 10);
		~geneSampleRangerHigh.font = Font("Geneva", 10);
		~geneSampleRangerLow.valueAction_(0);
		~geneSampleRangerHigh.valueAction_(~displaySons.size-1);
		~geneSampleRangerLow.enabled_(false);
		~geneSampleRangerHigh.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneReverseRanger=EZRanger(~wg, 300 @ 18, "Reverse Sample", ControlSpec(-1, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneReverseRanger", view.value)})}, [-1, 1], false, 125, 40);
		~geneReverseButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneReverseButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneReverseButton", action.value)});
			if(action.value == 1 ,{~flagGeneReverse='on';~geneReverseRanger.enabled_(true);~evaluationSynthAgent.value(nil, ~synthInstruments)},{~flagGeneReverse='off';~geneReverseRanger.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneReverseRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneLoopRanger=EZRanger(~wg, 300 @ 18, "Loop Sample", ControlSpec(-1, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneLoopRanger", view.value)})}, [-1, 1], false, 125, 40);
		~geneLoopButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneLoopButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneLoopButton", action.value)});
			if(action.value == 1 ,{~flagGeneLoop='on';~geneLoopRanger.enabled_(true);~evaluationSynthAgent.value(nil, ~synthInstruments)},{~flagGeneLoop='off';~geneLoopRanger.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneLoopRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneOffsetRanger=EZRanger(~wg, 300 @ 18, "Offset Sample", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneOffsetRanger", view.value)})}, [0, 1], false, 125, 40);
		~geneOffsetButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneOffsetButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneOffsetButton", action.value)});
			if(action.value == 1 ,{~flagGeneOffset='on';~geneOffsetRanger.enabled_(true);~evaluationSynthAgent.value(nil, ~synthInstruments)},{~flagGeneOffset='off';~geneOffsetRanger.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneOffsetRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneEnvLevelRanger=EZRanger(~wg, 300 @ 18, "Envelope Level", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneEnvLevelRanger", view.value)})}, [0.1, 0.9], false, 125, 40);
		~geneEnvLevelButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneEnvLevelButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneEnvLevelButton", action.value)});
			if(action.value == 1 ,{~flagGeneEnvLevel='on';~geneEnvLevelRanger.enabled_(true);if(~flagGeneEnvDuree == 'on', {~envelopeSynthMenu.enabled_(false)})},{~flagGeneEnvLevel='off';~envelopeSynthMenu.enabled_(true);~geneEnvLevelRanger.enabled_(false)});
		};
		~geneEnvLevelRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneEnvDureeRanger=EZRanger(~wg, 300 @ 18, "Envelope Duration", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneEnvDureeRanger", view.value)})}, [0.125, 0.75], false, 125, 40);
		~geneEnvDureeButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneEnvDureeButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneEnvDureeButton", action.value)});
			if(action.value == 1 ,{~flagGeneEnvDuree='on';~geneEnvDureeRanger.enabled_(true);if(~flagGeneEnvLevel == 'on', {~envelopeSynthMenu.enabled_(false)})},{~flagGeneEnvDuree='off';~envelopeSynthMenu.enabled_(true);~geneEnvDureeRanger.enabled_(false)});
		};
		~geneEnvDureeRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneSynthRangerLow=PopUpMenu(~wg,Rect(0,0, 148, 18)).items = ~listSynth;
		~geneSynthRangerLow.action = { arg item; ~geneSynthPopUpLow = item.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSynthRangerLow", item.value)})};
		~geneSynthRangerHigh=PopUpMenu(~wg,Rect(0,0, 148, 18)).items = ~listSynth;
		~geneSynthRangerHigh.action = { arg item; ~geneSynthPopUpHigh = item.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSynthRangerHigh", item.value)})};
		~geneSynthButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)], ["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneSynthButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneSynthButton", action.value)});
			if(action.value == 1 ,{~flagGeneSynth='on';~synthDefInstrMenu.enabled_(false);~geneSynthRangerLow.enabled_(true);~geneSynthRangerHigh.enabled_(true);
				~evaluationSynthAgent.value(nil, ~synthInstruments)},{~flagGeneSynth='off';~synthDefInstrMenu.enabled_(true);~geneSynthRangerLow.enabled_(false);~geneSynthRangerHigh.enabled_(false);~evaluationSynthAgent.value(nil, ~synthInstruments)});
		};
		~geneSynthRangerLow.font = Font("Geneva", 10);
		~geneSynthRangerHigh.font = Font("Geneva", 10);
		~geneSynthRangerLow.valueAction_(0);
		~geneSynthRangerHigh.valueAction_(~listSynth.size-1);
		~geneSynthRangerLow.enabled_(false);
		~geneSynthRangerHigh.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneControlsRanger=EZRanger(~wg, 300 @ 18, "Control Synthesizer", ControlSpec(0, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneControlsRanger", view.value)})}, [0, 1], false, 125, 40);
		~geneControlsButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneControlsButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneControlsButton", action.value)});
			if(action.value == 1 ,{~flagGeneControl='on';~controlsSynthMenu.enabled_(false);~geneControlsRanger.enabled_(true)},{~flagGeneControl='off';~controlsSynthMenu.enabled_(true);~geneControlsRanger.enabled_(false)});
		};
		~geneControlsRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneAudioOutRanger=EZRanger(~wg, 300 @ 18, "Audio Out", ControlSpec(1, ~sourceOutAgents.size, \lin, 1), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAudioOutRanger", view.value)})}, [1, 1], false, 125, 40);
		~geneAudioOutButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneAudioOutButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAudioOutButton", action.value)});
			if(action.value == 1 ,{~flagGeneOut='on';~sourceOutSlider.enabled_(false);~geneAudioOutRanger.enabled_(true)},{~flagGeneOut='off';~sourceOutSlider.enabled_(true);~geneAudioOutRanger.enabled_(false)});
		};
		~geneAudioOutRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneMidiOutRanger=EZRanger(~wg, 300 @ 18, "Midi Out", ControlSpec(0, 16, \lin, 1), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneMidiOutRanger", view.value)})}, [1, 16], false, 125, 40);
		~geneMidiOutButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneMidiOutButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneMidiOutButton", action.value)});
			if(action.value == 1 ,{~flagGeneMidi='on';~canalMidiOutSlider.enabled_(false);~geneMidiOutRanger.enabled_(true);
				~agents.do({arg agent; ~canalMidiOutAgent.wrapPut(agent, ~genomes.wrapAt(agent).wrapAt(39) * 16 - 1)})},{~flagGeneMidi='off';~canalMidiOutSlider.enabled_(true);~geneMidiOutRanger.enabled_(false)});
		};
		~geneMidiOutButton.enabled_(false);// Attention si MIDI -> Out on alors comment this line
		~geneMidiOutRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		StaticText(~wg, Rect(0, 0, 52, 15)).string_("Audio In").stringColor_(Color.white);
		~geneInputRangerLow=PopUpMenu(~wg,Rect(0,0, 120, 18)).items = ~audioInLR;
		~geneInputRangerLow.action = { arg item; ~geneInputPopUpLow = item.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneInputRangerLow", item.value)})};
		~geneInputRangerHigh=PopUpMenu(~wg,Rect(0,0, 120, 18)).items = ~audioInLR;
		~geneInputRangerHigh.action = { arg item; ~geneInputPopUpHigh = item.value;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneInputRangerHigh", item.value)})};
		~geneInputButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneInputButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneInputButton", action.value)});
			if(action.value == 1 ,{~flagGeneInput='on';
				if(~flagGeneBuffer=='on', {~audioInputButton.enabled_(false)});
				~geneInputRangerLow.enabled_(true);~geneInputRangerHigh.enabled_(true)},{~flagGeneInput='off';~audioInputButton.enabled_(true);
				~geneInputRangerLow.enabled_(false);~geneInputRangerHigh.enabled_(false)});
		};
		~geneInputRangerLow.valueAction_(0);
		~geneInputRangerHigh.valueAction_(~audioInLR.size-1);
		~geneInputRangerLow.enabled_(false);
		~geneInputRangerHigh.enabled_(false);
		~geneInputButton.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneLoopMusicRanger=EZRanger(~wg, 300 @ 18, "Loop Buffer Music", ControlSpec(-1, 1, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneLoopMusicRanger", view.value)})}, [-1, 1], false, 125, 40);
		~geneLoopMusicButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneLoopMusicButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneLoopMusicButton", action.value)});
			if(action.value == 1 ,{~flagGeneLoopMusic='on';~geneLoopMusicRanger.enabled_(true);~loopseq.enabled_(false)},{~flagGeneLoopMusic='off';~geneLoopMusicRanger.enabled_(false);~loopseq.enabled_(true)});
		};
		~geneLoopMusicRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneBufferMusicRanger=EZRanger(~wg, 300 @ 18, "Buffer Music", ControlSpec(1, 256, \exp, 1), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBufferMusicRanger", view.value)})}, [6, 24], false, 125, 40);
		~geneBufferMusicButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneBufferMusicButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBufferMusicButton", action.value)});
			if(action.value == 1 ,{~flagGeneBufferMusic='on';~geneBufferMusicRanger.enabled_(true);~dataAgentsSlider.enabled_(false);
				~agents.do({arg agent;~bufferDataAgents.wrapPut(agent, (~genomes.wrapAt(agent).wrapAt(42) * 256).floor.clip(1, 256))});
			},{~flagGeneBufferMusic='off';~geneBufferMusicRanger.enabled_(false);~dataAgentsSlider.enabled_(true);
				~agents.do({arg agent;~bufferDataAgents.wrapPut(agent, ~listedatamusiqueagents)});
			});
		};
		~geneBufferMusicRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneChordMaxRanger=EZRanger(~wg, 300 @ 18, "Chord Max", ControlSpec(1, 12, \exp, 1), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneChordMaxRanger", view.value)})}, [3, 6], false, 125, 40);
		~geneChordMaxButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneChordMaxButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneChordMaxButton", action.value)});
			if(action.value == 1 ,{~flagGeneChordMax='on';~geneChordMaxRanger.enabled_(true);~notesAccordsSlider.enabled_(false);
				~agents.do({arg agent;~chordMaxAgents.wrapPut(agent, (~genomes.wrapAt(agent).wrapAt(43) * 12).floor.clip(1, 12))});
			},{~flagGeneChordMax='off';~geneChordMaxRanger.enabled_(false);~notesAccordsSlider.enabled_(true);
				~agents.do({arg agent;~chordMaxAgents.wrapPut(agent, ~maxaccord)});
			});
		};
		~geneChordMaxRanger.enabled_(false);
		~wg.view.decorator.nextLine;
		~geneChordDurRanger=EZRanger(~wg, 300 @ 18, "Chord Dur", ControlSpec(0.01, 1, \exp, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneChordDurRanger", view.value)})}, [0.0625, 0.0833], false, 125, 40);
		~geneChordDurButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneChordDurButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneChordDurButton", action.value)});
			if(action.value == 1 ,{~flagGeneChordDur='on';~geneChordDurRanger.enabled_(true);~dureeAccordsSlider.enabled_(false);
				~agents.do({arg agent;~chordDurAgents.wrapPut(agent, ~genomes.wrapAt(agent).wrapAt(44))});
			},{~flagGeneChordDur='off';~geneChordDurRanger.enabled_(false);~dureeAccordsSlider.enabled_(true);
				~agents.do({arg agent;~chordDurAgents.wrapPut(agent, ~dureeaccord)});
			});
		};
		~geneChordDurRanger.enabled_(false);
		// Algo Choice
		StaticText(~wg, Rect(0, 0, 46, 15)).string_("Algorithm").stringColor_(Color.white);
		~geneAlgorithm=EZRanger(~wg, 250 @ 18, "Algorithm", ControlSpec(0, ~listeAlgorithm.size - 1, \lin, 1), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAlgorithm", view.value)})}, [0, ~listeAlgorithm.size - 1], false, 70, 40);
		~geneAlgorithmButton=Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~geneAlgorithmButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneAlgorithmButton", action.value)});
			if(action.value == 1 ,{~flagGeneAlgorithm='on';~geneAlgorithm.enabled_(true); ~choiceAlgoData.enabled_(false)}, {~flagGeneAlgorithm='off';~geneAlgorithm.enabled_(false); ~choiceAlgoData.enabled_(true)});
		};
		~geneAlgorithm.enabled_(false);
		~wg.view.decorator.nextLine;

		// GeneBand
		StaticText(~wg, 40 @ 20).string_("FzBand").stringColor_(Color.white).font = Font("Helvetica", 8);// 57
		// Band 0 to 12
		~geneBand0 = Button.new(~wg, 16 @ 20).
		states_([["0", Color.green], ["0", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(0, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand0", band.value)});
		}); // all Band 58
		~geneBand1 = Button.new(~wg, 16 @ 20).
		states_([["1", Color.green], ["1", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(1, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand1", band.value)});
		});
		~geneBand2 = Button.new(~wg, 16 @ 20).
		states_([["2", Color.green], ["2", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(2, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand2", band.value)});
		});
		~geneBand3 = Button.new(~wg, 16 @ 20).
		states_([["3", Color.green], ["3", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(3, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand3", band.value)});
		});
		~geneBand4 = Button.new(~wg, 16 @ 20).
		states_([["4", Color.green], ["4", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(4, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand4", band.value)});
		});
		~geneBand5 = Button.new(~wg, 16 @ 20).
		states_([["5", Color.green], ["5", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(5, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand5", band.value)});
		});
		~geneBand6 = Button.new(~wg, 16 @ 20).
		states_([["6", Color.green], ["6", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(6, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand6", band.value)});
		});
		~geneBand7 = Button.new(~wg, 16 @ 20).
		states_([["7", Color.green], ["7", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(7, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand7", band.value)});
		});
		~geneBand8 = Button.new(~wg, 16 @ 20).
		states_([["8", Color.green], ["8", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(8, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand8", band.value)});
		});
		~geneBand9 = Button.new(~wg, 16 @ 20).
		states_([["9", Color.green], ["9", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(9, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand9", band.value)});
		});
		~geneBand10 = Button.new(~wg, 16 @ 20).
		states_([["10", Color.green], ["10", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(10, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand10", band.value)});
		});
		~geneBand11 = Button.new(~wg, 16 @ 20).
		states_([["11", Color.green], ["11", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(11, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand11", band.value)});
		});
		~geneBand12 = Button.new(~wg, 16 @ 20).
		states_([["12", Color.green], ["12", Color.red]]).font_(Font("Helvetica", 10)).
		action_({arg band; ~flagBandGenes.put(12, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~geneBand12", band.value)});
		});

		~buttonGeneBand = Button(~wg,Rect(0, 0, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~buttonGeneBand.action = {arg flag;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~buttonGeneBand", flag.value)});
			if(flag.value == 0,
				{~flagGeneBand = 'off';
					~geneBand0.enabled_(false);
					~geneBand1.enabled_(false);
					~geneBand2.enabled_(false);
					~geneBand3.enabled_(false);
					~geneBand4.enabled_(false);
					~geneBand5.enabled_(false);
					~geneBand6.enabled_(false);
					~geneBand7.enabled_(false);
					~geneBand8.enabled_(false);
					~geneBand9.enabled_(false);
					~geneBand10.enabled_(false);
					~geneBand11.enabled_(false);
					~geneBand12.enabled_(false);
					~wp.view.children.wrapAt(37).enabled_(true);
					if(~buttonSynthBand.value == 1, {
						for(0, ~numFhzBand,
							{arg index;
								~wp.view.children.wrapAt(24 + index).enabled_(true);
						});
						if(~numFhzBand < 12, {
							for(~numFhzBand + 1, 12,
								{arg index;
									~wp.view.children.wrapAt(24 + index).enabled_(false);
							});
						});
					});
				},
				{~flagGeneBand = 'on';
					~buttonSynthBand.valueAction = 0;
					for(0, ~numFhzBand,
						{arg index;
							~wg.view.children.wrapAt(58 + index).enabled_(true);
					});
					if(~numFhzBand < 12, {
						for(~numFhzBand + 1, 12,
							{arg index;
								~wg.view.children.wrapAt(58 + index).enabled_(false);
						});
					});
					~synthBand0.enabled_(false);
					~synthBand1.enabled_(false);
					~synthBand2.enabled_(false);
					~synthBand3.enabled_(false);
					~synthBand4.enabled_(false);
					~synthBand5.enabled_(false);
					~synthBand6.enabled_(false);
					~synthBand7.enabled_(false);
					~synthBand8.enabled_(false);
					~synthBand9.enabled_(false);
					~synthBand10.enabled_(false);
					~synthBand11.enabled_(false);
					~synthBand12.enabled_(false);
					~wp.view.children.wrapAt(37).enabled_(false);
			});
		};
		~wg.front;
	}

	synthPanel {

		//Synth Panel
		~wi = Window("Synthesizer", Rect(490, 120, 530, 210), scroll: true);
		~wi.view.decorator = FlowLayout(~wi.view.bounds);
		~wi.alpha=1.0;~wi.front;
		// Out
		~sourceOutSlider=PopUpMenu(~wi,Rect(0,0, 55, 18)).items = ~sourceOutAgents;
		~sourceOutSlider.action = {arg out;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~sourceOutSlider", out.value)});
			~audioOutSynth.wrapPut(~synthDefInstrMenu.value,  out.value);
		};
		// SynthDef
		~synthDefInstrMenu=PopUpMenu(~wi,Rect(0,0, 150, 18)).items = ~listSynth;
		~synthDefInstrMenu.action = {arg synth;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthDefInstrMenu", synth.value)});
			~synthInstruments=~listSynth.wrapAt(synth.value);
			if(~audioOutSynth.wrapAt(synth.value) == nil, {~audioOutSynth=~audioOutSynth.add(~sourceOutSlider.value);
				~controlsSynth = ~controlsSynth.add(~controlsSynthMenu.value);
			},{~sourceOutSlider.value_(~audioOutSynth.wrapAt(synth.value));
				~controlsSynthMenu.value_(~controlsSynth.wrapAt(synth.value))});
			~randomControlsSynth.value_(~automationControlsSynth.wrapAt(synth.value));
			~jitterControlsSynth.value_(~automationJitterControlsSynth.wrapAt(synth.value));
			~evaluationSynthAgent.value(synth, ~synthInstruments);
		};
		~synthDefInstrMenu.font = Font("Geneva", 11);
		~synthDefInstrMenu.focus;
		// Sons
		~soundsInstrMenu=PopUpMenu(~wi,Rect(0,0, 170, 18)).items = ~displaySons;
		~soundsInstrMenu.action = {arg son;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~soundsInstrMenu", son.value)});
			~soundsPositions=son.value;
			~bufferSonsInstruments=~bufferSons.wrapAt(son.value);
			if(~recSamplesButtonSons.wrapAt(son.value) == nil, {~recSamplesButtonSons=~recSamplesButtonSons.add(~recSamplesButton.value);
				~recSamplesLoopSons=~recSamplesLoopSons.add(~recSamplesLoopButton.value);
				~recSamplesLevelsSons=~recSamplesLevelsSons.add(~recSamplesLevelsMenu.value);
				~loopSynthSons=~loopSynthSons.add(~loopSynthButton.value);
				~posSamplesSons=~posSamplesSons.add(~posSamplesInstr.value);
				~reverseSynthSons=~reverseSynthSons.add(~reverseSynthButton.value);
				~audioInputSons=~audioInputSons.add(~audioInputButton.value);
				~flagFreqSamplesSons=~flagFreqSamplesSons.add(~flagFreqSamples);
				~flagFreqSamples=~flagFreqSamplesSons.wrapAt(son.value)},
			{~recSamplesButton.value_(~recSamplesButtonSons.wrapAt(son.value));
				~recSamplesLoopButton.value_(~recSamplesLoopSons.wrapAt(son.value));
				~recSamplesLevelsMenu.value_(~recSamplesLevelsSons.wrapAt(son.value));
				~loopSynthButton.value_(~loopSynthSons.wrapAt(son.value));
				~posSamplesInstr.value_(~posSamplesSons.wrapAt(son.value));
				~reverseSynthButton.value_(~reverseSynthSons.wrapAt(son.value));
				~audioInputButton.value_(~audioInputSons.wrapAt(son.value));
				~flagFreqSamples=~flagFreqSamplesSons.wrapAt(son.value)});
			~valRec1.value = ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(0);
			~valRec2.value = ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(1);
			~agents.do({arg agent;
				~synthRecAudioAgents.wrapAt(agent).set(\reclevel1, ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(0), \reclevel2, ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(1));
				~synthRecAudioAgents.wrapAt(agent).setn(\in, ~audioInLR.wrapAt(~audioInputSons.wrapAt(son.value)));
				~synthRecFileAgents.wrapAt(agent).set(\reclevel1, ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(0), \reclevel2, ~recSamplesLevelsSons.wrapAt(son.value).wrapAt(1));
			});
		};
		~soundsInstrMenu.font = Font("Geneva", 11);
		StaticText(~wi, Rect(0, 0, 55, 20)).string_("Rec In").stringColor_(Color.white).font_(Font("Georgia", 8));
		// Input canal for rec sound
		~audioInputButton=PopUpMenu(~wi,Rect(0, 0, 70, 18)).items = ~audioInLR;
		~audioInputButton.action = {arg input;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~audioInputButton", input.value)});
			~audioInputSons.wrapPut(~soundsInstrMenu.value, input.value);
			~recSamples.wrapAt(~soundsInstrMenu.value).setn(\in, input.value);
			~agents.do({arg agent;
				~synthRecAudioAgents.wrapAt(agent).setn(\in, input.value)});
		};
		~audioInputButton.value_(0);
		StaticText(~wi, Rect(0, 0, 368, 10)).string_("Envelope").stringColor_(Color.white).font_(Font("Georgia", 8));
		StaticText(~wi, Rect(0, 0, 33, 10)).string_("NoClic").stringColor_(Color.white).font_(Font("Georgia", 8));
		StaticText(~wi, Rect(0, 0, 55, 10)).string_("SynthCtrl").stringColor_(Color.white).font_(Font("Georgia", 8));
		StaticText(~wi, Rect(0, 0, 45, 10)).string_("RecCtrl").stringColor_(Color.white).font_(Font("Georgia", 8));
		~wi.view.decorator.nextLine;

		~envelopeSynthMenu=EnvelopeView(~wi, Rect(0, 0, 375, 50));
		~envelopeSynthMenu.drawLines_(true);
		~envelopeSynthMenu.selectionColor_(Color.red);
		~envelopeSynthMenu.fillColor_(Color(0, 0.25, 0.5));
		~envelopeSynthMenu.strokeColor_(Color.cyan);
		~envelopeSynthMenu.drawRects_(true);
		~envelopeSynthMenu.step_(0.001);
		~envelopeSynthMenu.gridOn_(true);
		~envelopeSynthMenu.resize_(8);
		~envelopeSynthMenu.thumbSize_(16);
		~envelopeSynthMenu.value_([[0.0, 0.015625, 0.125, 0.375, 0.625, 0.75, 0.875, 1.0], [0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0]]);
		//~envelopeSynthMenu.curves_(3);
		~envelopeSynthMenu.gridColor_(Color.grey);
		~envelopeSynthMenu.action={arg env;var time;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~envelopeSynthMenu", env.value)});
			~levelenvelope=env.value.wrapAt(1);
			time=env.value.wrapAt(0);
			~timeenvelope.size.do({arg i; ~timeenvelope.wrapPut(i, abs(time.wrapAt(i+1) - time.wrapAt(i)))});
		};
		// Controls antiClick Synth (plugins hp)
		~controlsAntiClickMenu=MultiSliderView(~wi, Rect(0, 0, 25, 50));
		~controlsAntiClickMenu.value_([0.5, 0.5]);
		~controlsAntiClickMenu.fillColor_(Color.black);
		~controlsAntiClickMenu.strokeColor_(Color.cyan);
		~controlsAntiClickMenu.xOffset_(2);
		~controlsAntiClickMenu.thumbSize_(8);
		~controlsAntiClickMenu.elasticMode_(1);
		~controlsAntiClickMenu.step_(0.01);
		~controlsAntiClickMenu.action={arg antiClick; ~antiClick=~controlsAntiClickMenu.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~controlsAntiClickMenu", antiClick.value)})};
		// Controls Synth
		~controlsSynthMenu=MultiSliderView(~wi, Rect(0, 0, 55, 50));
		~controlsSynthMenu.value_([0, 0, 0]);
		~controlsSynthMenu.fillColor_(Color(0, 0.25, 0.5));
		~controlsSynthMenu.strokeColor_(Color.cyan);
		~controlsSynthMenu.xOffset_(2);
		~controlsSynthMenu.thumbSize_(14);
		~controlsSynthMenu.elasticMode_(1);
		~controlsSynthMenu.action={arg controls;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~controlsSynthMenu", controls.value)});
			~controlsValues=controls.value.clip(0.01, 1.0);
			~controlsSynth.wrapPut(~synthDefInstrMenu.value, controls.value);
		};
		// Rec Levels
		~recSamplesLevelsMenu=MultiSliderView(~wi, Rect(0, 0, 40, 50));
		~recSamplesLevelsMenu.value_([1, 0]);
		~recSamplesLevelsMenu.strokeColor_(Color.cyan);
		~recSamplesLevelsMenu.fillColor_(Color.blue);
		~recSamplesLevelsMenu.xOffset_(2);
		~recSamplesLevelsMenu.thumbSize_(15);
		~recSamplesLevelsMenu.elasticMode_(1);
		~recSamplesLevelsMenu.action={arg levels;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~recSamplesLevelsMenu", levels.value)});
			~levelsValues=levels.value;
			~listesamplein.wrapAt(~soundsInstrMenu.value).set(\reclevel1, levels.value.wrapAt(0), \reclevel2, levels.value.wrapAt(1));
			~recSamplesLevelsSons.wrapPut(~soundsInstrMenu.value, levels.value);
			~agents.do({arg agent;
				~synthRecAudioAgents.wrapAt(agent).set(\reclevel1, levels.value.wrapAt(0), \reclevel2, levels.value.wrapAt(1));
				~synthRecFileAgents.wrapAt(agent).set(\reclevel1, levels.value.wrapAt(0), \reclevel2, levels.value.wrapAt(1));
			});
			~valRec1.value = ~levelsValues.wrapAt(0);
			~valRec2.value = ~levelsValues.wrapAt(1);
		};
		~wi.view.decorator.nextLine;
		// REC
		~recSamplesButton=Button(~wi,Rect(10, 10, 50, 18)).states=[["Rec On", Color.black, Color.green(0.8, 0.25)],["Rec Off", Color.black, Color.red(0.8, 0.25)]];
		~recSamplesButton.action = {arg rec;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~recSamplesButton", rec.value)});
			~listesamplein.wrapAt(~soundsInstrMenu.value).set(\run, rec.value, \trigger, rec.value);
			~recSamplesButtonSons.wrapPut(~soundsInstrMenu.value, rec.value);
			if(rec.value == 1 , {~flagFreqSamplesSons.wrapPut(~soundsInstrMenu.value, 'on'); ~flagFreqSamples='on'});
		};
		~recSamplesLoopButton=Button(~wi,Rect(10, 10, 75, 18)).states=[["LoopR On", Color.black, Color.green(0.8, 0.25)],["LoopR Off", Color.black, Color.red(0.8, 0.25)]];
		~recSamplesLoopButton.action = {arg loop;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~recSamplesLoopButton", loop.value)});
			~listesamplein.wrapAt(~soundsInstrMenu.value).set(\loop, loop.value, \trigger, loop.value);
			~recSamplesLoopSons.wrapPut(~soundsInstrMenu.value, loop.value);
		};
		~loopSynthButton=Button(~wi,Rect(10, 10, 75, 18)).states=[["LoopP On", Color.black, Color.green(0.8, 0.25)],["LoopP Off", Color.black, Color.red(0.8, 0.25)]];
		~loopSynthButton.action = {arg loop;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~loopSynthButton", loop.value)});
			~loopSynthSons.wrapPut(~soundsInstrMenu.value, loop.value);
		};
		~reverseSynthButton=Button(~wi,Rect(10, 10, 75, 18)).states=[["Reverse On", Color.black, Color.green(0.8, 0.25)],["Reverse Off", Color.black, Color.red(0.8, 0.25)]];
		~reverseSynthButton.action = {arg reverse;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~reverseSynthButton", reverse.value)});
			~reverseSynthSons.wrapPut(~soundsInstrMenu.value, reverse.value);
		};
		// Amp Synth on/off
		~ampSynth = Button(~wi,Rect(10,10, 150, 18)).states=[["Bypass Amp Synth->FX On", Color.black, Color.green(0.8, 0.25)],["Bypass Amp Synth->FX Off", Color.black, Color.red(0.8, 0.25)]];
		~ampSynth.action = {arg view; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~ampSynth", view.value)});
			if(view.value == 0 , {~flagAmpSynth = 'off'},{~flagAmpSynth = 'on'});
		};
		// Display value recbutton 1
		~valRec1 = NumberBox(~wi, Rect(0, 0, 35, 18)).minDecimals_(4).maxDecimals_(4);
		~valRec1.value = 1;
		~valRec1.action = {arg num;
			var levels;
			levels = ~recSamplesLevelsMenu.value;
			levels.put(0, num.value);
			~recSamplesLevelsMenu.valueAction_(levels);
		};
		// Display value recbutton 2
		~valRec2 = NumberBox(~wi, Rect(0, 0, 35, 18)).minDecimals_(4).maxDecimals_(4);
		~valRec2.value = 0;
		~valRec2.action = {arg num;
			var levels;
			levels = ~recSamplesLevelsMenu.value;
			levels.put(1, num.value);
			~recSamplesLevelsMenu.valueAction_(levels);
		};
		// PAN
		~panInstr=EZRanger(~wi, 200 @ 18, "Pan", \bipolar,
			{|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~panInstr", view.value)})}, [-0.1, 0.1], false, 25, 30);
		// offset sample
		~posSamplesInstr=EZKnob(~wi, 125 @ 18, "Offset", ControlSpec(0, 1, \lin, 0), {arg pos; ~posSamplesSons.wrapPut(~soundsInstrMenu.value, pos.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~posSamplesInstr", pos.value)})}, 0, labelWidth: 50,unitWidth: 0, layout: 'horz');
		// Jitter controls synth
		~jitterControlsSynth=EZKnob(~wi, 125 @ 18, "JitterCtrls", ControlSpec(0, 1, \lin, 0), {arg view; ~valueJitterControlsSynth = view.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~jitterControlsSynth", view.value)});
			~automationJitterControlsSynth.wrapPut(~synthDefInstrMenu.value, view.value);
		}, 0.1, labelWidth: 60,unitWidth: 0, layout: 'horz');
		// Automation controls synth
		~randomControlsSynth = Button(~wi,Rect(10,10, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~randomControlsSynth.action = {arg view; ~valueRandomControlsSynth = view.value;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~randomControlsSynth", view.value)});
			~automationControlsSynth.wrapPut(~synthDefInstrMenu.value, view.value);
		};
		~wi.view.decorator.nextLine;
		// FREQ
		~freqInstr=EZRanger(~wi, 200 @ 18, "Fhz", ControlSpec(0, 127, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~freqInstr", view.value)})}, [0, 127], false, 25, 30);
		// Translate
		~freqTransposeInstr=EZKnob(~wi, 125 @ 18, "Translate", ControlSpec(-127, 127, \lin, 0), {arg tran; tran.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~freqTransposeInstr", tran.value)})}, 0, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~wi.view.decorator.nextLine;
		// AMP
		~ampInstr=EZRanger(~wi, 200 @18, "Amp", \db,
			{|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~ampInstr", view.value)})}, [-inf, 0], false, 25, 30);
		~wi.view.decorator.nextLine;
		// DUREE
		~dureeInstr=EZRanger(~wi, 200 @ 18, "Dur", ControlSpec(0, 60, \lin, 0), {|view| if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dureeInstr", view.value)})}, [0, 4], false, 25, 30);
		// Stretch
		~dureeTempoinstr=EZKnob(~wi, 125 @ 18, "Stretch", ControlSpec(-16, 64, \lin, 0), {arg tempo; tempo.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dureeTempoinstr", tempo.value)})}, 1, labelWidth: 55,unitWidth: 0, layout: 'horz');
		// Quant
		~quantaMusicSlider=EZKnob(~wi, 125 @ 18, "Quant",ControlSpec(1, 100, \exp, 0),
			{|ez| ~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {~quantaMusic=ez.value;nil});
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~quantaMusicSlider", ez.value)})},~quantaMusic, labelWidth: 35,unitWidth: 0, layout: 'horz');

		// Evaluation Synthetiseur Agents pour affichage sliders
		~evaluationSynthAgent={arg synth, synthInstrument;
			var valuetest=false;
			// test if gene synth activate
			if(~geneSynthButton.value == 1 ,
				{~soundsInstrMenu.enabled_(true);~recSamplesLevelsMenu.enabled_(true);
					~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true);
					~recSamplesLevelsMenu.enabled_(true);
					if(~geneBufferButton.value == 1, {~geneBufferRanger.enabled_(true)},
						{~geneBufferRanger.enabled_(false);
							if(~geneSampleButton.value == 1, {~geneSampleRangerLow.enabled_(true);~geneSampleRangerHigh.enabled_(true)},{~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false)})});
					if(~geneLoopButton.value == 1, {~loopSynthButton.enabled_(false);~geneLoopRanger.enabled_(true)},{~loopSynthButton.enabled_(true);~geneLoopRanger.enabled_(false)});
					if(~geneReverseButton.value == 1, {~reverseSynthButton.enabled_(false);~geneReverseRanger.enabled_(true)},{~reverseSynthButton.enabled_(true);~geneReverseRanger.enabled_(false)});
					if(~geneOffsetButton.value == 1, {~posSamplesInstr.enabled_(false);~geneOffsetRanger.enabled_(true)},{~posSamplesInstr.enabled_(true);~geneOffsetRanger.enabled_(false)});
					~controlsAntiClickMenu.enabled_(true);
				},
				// test synth without sample
				{valuetest=false;~listeWithoutSample.do({arg item; if(synthInstrument == item, {valuetest=true})});
					if(valuetest, {~soundsInstrMenu.enabled_(false);~recSamplesLevelsMenu.enabled_(false);
						~recSamplesButton.enabled_(false);~recSamplesLoopButton.enabled_(false);~loopSynthButton.enabled_(false);~reverseSynthButton.enabled_(false);~posSamplesInstr.enabled_(false);
						~geneBufferRanger.enabled_(false);~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false);~geneReverseRanger.enabled_(false);~geneLoopRanger.enabled_(false);~geneOffsetRanger.enabled_(false);
						~controlsAntiClickMenu.enabled_(false)},
					// test synth with one sample
					{valuetest=false;~listeWith1Sample.do({arg item; if(synthInstrument == item, {valuetest=true})});
						if(valuetest, {if(~geneBufferButton.value == 1, {~geneBufferRanger.enabled_(true);~soundsInstrMenu.enabled_(false);~recSamplesButton.enabled_(false);~recSamplesLoopButton.enabled_(false)},
							{~geneBufferRanger.enabled_(false);~soundsInstrMenu.enabled_(true);~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true);
								if(~geneSampleButton.value == 1, {~geneSampleRangerLow.enabled_(true);~geneSampleRangerHigh.enabled_(true);~soundsInstrMenu.enabled_(false);~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true)},{~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false);~soundsInstrMenu.enabled_(true);~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true)})});
						~recSamplesLevelsMenu.enabled_(true);
						if(~geneLoopButton.value == 1, {~loopSynthButton.enabled_(false);~geneLoopRanger.enabled_(true)},{~loopSynthButton.enabled_(true);~geneLoopRanger.enabled_(false)});
						if(~geneReverseButton.value == 1, {~reverseSynthButton.enabled_(false);~geneReverseRanger.enabled_(true)},{~reverseSynthButton.enabled_(true);~geneReverseRanger.enabled_(false)});
						if(~geneOffsetButton.value == 1, {~posSamplesInstr.enabled_(false);~geneOffsetRanger.enabled_(true)},{~posSamplesInstr.enabled_(true);~geneOffsetRanger.enabled_(false)});
						// test not plugin HP
						valuetest=false;~listeNoPlugHP.do({arg item; if(synthInstrument == item, {valuetest=true})});
						if(valuetest, {~controlsAntiClickMenu.enabled_(false)},{~controlsAntiClickMenu.enabled_(true)})},
						// test synth with two sample
						{valuetest=false;~listeWith2Sample.do({arg item; if(synthInstrument == item, {valuetest=true})});
							if(valuetest, {~soundsInstrMenu.enabled_(true);~recSamplesLevelsMenu.enabled_(true);
								~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true);
								~recSamplesLevelsMenu.enabled_(true);
								if(~geneBufferButton.value == 1, {~geneBufferRanger.enabled_(true)},
									{~geneBufferRanger.enabled_(false);
										if(~geneSampleButton.value == 1, {~geneSampleRangerLow.enabled_(true);~geneSampleRangerHigh.enabled_(true);},{~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false)})});
								if(~geneLoopButton.value == 1, {~loopSynthButton.enabled_(false);~geneLoopRanger.enabled_(true)},{~loopSynthButton.enabled_(true);~geneLoopRanger.enabled_(false)});
								if(~geneReverseButton.value == 1, {~reverseSynthButton.enabled_(false);~geneReverseRanger.enabled_(true)},{~reverseSynthButton.enabled_(true);~geneReverseRanger.enabled_(false)});
								if(~geneOffsetButton.value == 1, {~posSamplesInstr.enabled_(false);~geneOffsetRanger.enabled_(true)},{~posSamplesInstr.enabled_(true);~geneOffsetRanger.enabled_(false)});
								// test not plugin HP
								valuetest=false;~listeNoPlugHP.do({arg item; if(synthInstrument == item, {valuetest=true})});
								if(valuetest, {~controlsAntiClickMenu.enabled_(false)},{~controlsAntiClickMenu.enabled_(true)})},
							// Others SynthDef
							{~soundsInstrMenu.enabled_(true);~recSamplesLevelsMenu.enabled_(true);
								~recSamplesButton.enabled_(true);~recSamplesLoopButton.enabled_(true);
								~recSamplesLevelsMenu.enabled_(true);
								if(~geneBufferButton.value == 1, {~geneBufferRanger.enabled_(true)},
									{~geneBufferRanger.enabled_(false);
										if(~geneSampleButton.value == 1, {~geneSampleRangerLow.enabled_(true);~geneSampleRangerHigh.enabled_(true);},{~geneSampleRangerLow.enabled_(false);~geneSampleRangerHigh.enabled_(false)})});
								if(~geneLoopButton.value == 1, {~loopSynthButton.enabled_(false);~geneLoopRanger.enabled_(true)},{~loopSynthButton.enabled_(true);~geneLoopRanger.enabled_(false)});
								if(~geneReverseButton.value == 1, {~reverseSynthButton.enabled_(false);~geneReverseRanger.enabled_(true)},{~reverseSynthButton.enabled_(true);~geneReverseRanger.enabled_(false)});
								if(~geneOffsetButton.value == 1, {~posSamplesInstr.enabled_(false);~geneOffsetRanger.enabled_(true)},{~posSamplesInstr.enabled_(true);~geneOffsetRanger.enabled_(false)});
								// test not plugin HP
								valuetest=false;~listeNoPlugHP.do({arg item; if(synthInstrument == item, {valuetest=true})});
								if(valuetest, {~controlsAntiClickMenu.enabled_(false)},{~controlsAntiClickMenu.enabled_(true)});
			})})})});
		};
	}

	effetsPanel {

		// Panel Effets
		~we = Window("FX", Rect(1030, 5, 300, 190), scroll: true);
		~we.view.decorator = FlowLayout(~we.view.bounds);
		~we.alpha=1.0;~we.front;
		// Out
		~sourceOutEffets=PopUpMenu(~we,Rect(0,0, 65, 18)).items = ~sourceOutAgents;
		~sourceOutEffets.action = {arg out;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~sourceOutEffets", out.value)});
			~audioOutEffets.wrapPut(~effetsInstrMenu.value, out.value);
			~listSynthEffets.size.do({arg effet;
				~listSynthEffets.wrapAt(effet).set('out', ~audioOutEffets.wrapAt(effet));
			});
		};
		// Effets
		~effetsInstrMenu=PopUpMenu(~we,Rect(0,0, 160, 18)).items = ~listEffets;
		~effetsInstrMenu.action = {arg effet;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~effetsInstrMenu", effet.value)});
			if(~playSynthEffets.wrapAt(effet.value) == nil,
				{~playSynthEffets=~playSynthEffets.add(~playEffetsButton.value);
					~controlsSynthEffets=~controlsSynthEffets.add(~controlsEffetsMenu.value);
					~panSynthEffets=~panSynthEffets.add(~panEffets.value);
					~jitterPanSynthEffets=~jitterPanSynthEffets.add(~jitterPanEffets.value);
					~jitterControlsSynthEffets=~jitterControlsSynthEffets.add(~jitterControlsEffets.value);
					~ampSynthEffets=~ampSynthEffets.add(~ampEffets.value);
					~audioOutEffets=~audioOutEffets.add(~sourceOutEffets.value);
					~automationSpeedEffets=~automationSpeedEffets.add(~speedEffets.value);
				}, {~playEffetsButton.value_(~playSynthEffets.wrapAt(effet.value));
					~controlsEffetsMenu.value_(~controlsSynthEffets.wrapAt(effet.value));
					~panEffets.value_(~panSynthEffets.wrapAt(effet.value));
					~jitterPanEffets.value_(~jitterPanSynthEffets.wrapAt(effet.value));
					~jitterControlsEffets.value_(~jitterControlsSynthEffets.wrapAt(effet.value));
					~ampEffets.value_(~ampSynthEffets.wrapAt(effet.value));
					~sourceOutEffets.value_(~audioOutEffets.wrapAt(effet.value));
					~speedEffets.value_(~automationSpeedEffets.wrapAt(effet.value));
			});
			~randomPanEffets.value_(~automationPanEffets.wrapAt(effet.value));
			~randomControlsEffets.value_(~automationControlsEffets.wrapAt(effet.value));
		};
		~effetsInstrMenu.focus;
		~playEffetsButton=Button(~we,Rect(10, 10, 50, 18)).states=[["Play On", Color.black, Color.green(0.8, 0.25)],["Play Off", Color.black, Color.red(0.8, 0.25)]];
		~playEffetsButton.action = {arg play;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~playEffetsButton", play.value)});
			~playSynthEffets.wrapPut(~effetsInstrMenu.value, play.value);
			if(play.value == 1, {~listSynthEffets.wrapAt(~effetsInstrMenu.value).run(true)},{~listSynthEffets.wrapAt(~effetsInstrMenu.value).run(false)});
		};
		~we.view.decorator.nextLine;
		~controlsEffetsMenu=MultiSliderView(~we, Rect(0, 0, 200, 75));
		~controlsEffetsMenu.drawLines_(false);
		~controlsEffetsMenu.fillColor_(Color(0, 0.25, 0.5));
		~controlsEffetsMenu.strokeColor_(Color.cyan);
		~controlsEffetsMenu.drawRects_(true);
		~controlsEffetsMenu.xOffset_(8);
		~controlsEffetsMenu.thumbSize_(16);
		~controlsEffetsMenu.value_([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5]);
		~controlsEffetsMenu.elasticMode_(1);
		~controlsEffetsMenu.action={arg controls;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~controlsEffetsMenu", controls.value)});
			~controlsSynthEffets.wrapPut(~effetsInstrMenu.value, controls.value);
			~listSynthEffets.wrapAt(~effetsInstrMenu.value).set(\control1, controls.value.wrapAt(0), \control2, controls.value.wrapAt(1), \control3, controls.value.wrapAt(2), \control4, controls.value.wrapAt(3), \control5, controls.value.wrapAt(4), \control6, controls.value.wrapAt(5), \control7, controls.value.wrapAt(6), \control8, controls.value.wrapAt(7));
		};
		// Jitter controls Effets
		~jitterControlsEffets=EZKnob(~we, 55 @ 75, "JitterCtrls", ControlSpec(0, 1, \lin, 0), {arg view; ~jitterControlsSynthEffets.wrapPut(~effetsInstrMenu.value, view.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~jitterControlsEffets", view.value)})}, 0.1, labelWidth: 55,unitWidth: 0, layout: 'vert');
		// Random Effets
		~randomControlsEffets= Button(~we,Rect(10,10, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~randomControlsEffets.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~randomControlsEffets", view.value)});
			~automationControlsEffets.wrapPut(~effetsInstrMenu.value, view.value);
		};
		~we.view.decorator.nextLine;
		// AMP
		~ampEffets=EZKnob(~we, 55 @ 75, "Amp",ControlSpec(-inf, 12, \db, 0.001),
			{|ez| ~listSynthEffets.wrapAt(~effetsInstrMenu.value).set('amp', ez.value.dbamp); ~ampSynthEffets.wrapPut(~effetsInstrMenu.value, ez.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~ampEffets", ez.value)})}, -12, labelWidth: 50,unitWidth: 0, layout: 'vert');
		// PAN
		~panEffets=EZKnob(~we, 55 @ 75, "Pan",ControlSpec(-1, 1, \lin, 0.001),
			{|ez| ~listSynthEffets.wrapAt(~effetsInstrMenu.value).set('pan', ez.value);~panSynthEffets.wrapPut(~effetsInstrMenu.value, ez.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~panEffets", ez.value)})},0.0, labelWidth: 40,unitWidth: 0, layout: 'vert');
		// Jitter Pan Effets
		~jitterPanEffets=EZKnob(~we, 55 @ 75, "JitterPan", ControlSpec(0, 1, \lin, 0), {arg view;
			~jitterPanSynthEffets.wrapPut(~effetsInstrMenu.value, view.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~jitterPanEffets", view.value)})}, 0.1, labelWidth: 40,unitWidth: 0, layout: 'vert');
		// Random Pan
		~randomPanEffets= Button(~we,Rect(10,10, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~randomPanEffets.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~randomPanEffets", view.value)});
			~automationPanEffets.wrapPut(~effetsInstrMenu.value, view.value);
			if(view.value == 0, {~listSynthEffets.wrapAt(~effetsInstrMenu.value).set('pan', ~panSynthEffets.wrapAt(~effetsInstrMenu.value))});
		};
		// Speed Autoamtion Effets
		~speedEffets = EZKnob(~we, 55 @ 75, "Speed",ControlSpec(0.01, 100, \exp, 0.01),
			{|view| ~automationSpeedEffets.wrapPut(~effetsInstrMenu.value, view.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~speedEffets", view.value)});
				~listeFXTime.put(~effetsInstrMenu.value, view.value.reciprocal);
		},24, labelWidth: 40,unitWidth: 0, layout: 'vert');
	}

	verbPanel {

		// Panel verb
		~wv = Window("Verb", Rect(1000, 100, 300, 190), scroll: true);
		~wv.view.decorator = FlowLayout(~wv.view.bounds);
		~wv.alpha=1.0;~wv.front;
		// Out
		~sourceOutVerb=PopUpMenu(~wv,Rect(0,0, 65, 18)).items = ~sourceOutAgents;
		~sourceOutVerb.action = {arg out;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~sourceOutVerb", out.value)});
			~audioOutVerb.wrapPut(~verbInstrMenu.value, out.value);
			~listSynthVerb.size.do({arg verb;
				~listSynthVerb.wrapAt(verb).set('out', ~audioOutVerb.wrapAt(verb));
			});
		};
		// Verb
		~verbInstrMenu=PopUpMenu(~wv,Rect(0,0, 160, 18)).items = ~listVerb;
		~verbInstrMenu.action = {arg verb;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~verbInstrMenu", verb.value)});
			if(~playSynthVerb.wrapAt(verb.value) == nil,
				{~playSynthVerb=~playSynthVerb.add(~playVerbButton.value);
					~controlsSynthVerb=~controlsSynthVerb.add(~controlsVerbMenu.value);
					~panSynthVerb=~panSynthVerb.add(~panVerb.value);
					~jitterPanSynthVerb=~jitterPanSynthVerb.add(~jitterPanVerb.value);
					~jitterControlsSynthVerb=~jitterControlsSynthVerb.add(~jitterControlsVerb.value);
					~ampSynthVerb=~ampSynthVerb.add(~ampVerb.value);
					~audioOutVerb=~audioOutVerb.add(~sourceOutVerb.value);
					~automationSpeedVerb=~automationSpeedVerb.add(~speedVerb.value);
				}, {~playVerbButton.value_(~playSynthVerb.wrapAt(verb.value));
					~controlsVerbMenu.value_(~controlsSynthVerb.wrapAt(verb.value));
					~panVerb.value_(~panSynthVerb.wrapAt(verb.value));
					~jitterPanVerb.value_(~jitterPanSynthVerb.wrapAt(verb.value));
					~jitterControlsVerb.value_(~jitterControlsSynthVerb.wrapAt(verb.value));
					~ampVerb.value_(~ampSynthVerb.wrapAt(verb.value));
					~sourceOutVerb.value_(~audioOutVerb.wrapAt(verb.value));
					~speedVerb.value_(~automationSpeedVerb.wrapAt(verb.value));
			});
			~randomPanVerb.value_(~automationPanVerb.wrapAt(verb.value));
			~randomControlsVerb.value_(~automationControlsVerb.wrapAt(verb.value));
		};
		~verbInstrMenu.focus;
		~playVerbButton=Button(~wv,Rect(10, 10, 50, 18)).states=[["Play On", Color.black, Color.green(0.8, 0.25)],["Play Off", Color.black, Color.red(0.8, 0.25)]];
		~playVerbButton.action = {arg play;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~playVerbButton", play.value)});
			~playSynthVerb.wrapPut(~verbInstrMenu.value, play.value);
			if(play.value == 1, {~listSynthVerb.wrapAt(~verbInstrMenu.value).run(true)},{~listSynthVerb.wrapAt(~verbInstrMenu.value).run(false)});
		};
		~wv.view.decorator.nextLine;
		~controlsVerbMenu=MultiSliderView(~wv, Rect(0, 0, 200, 75));
		~controlsVerbMenu.drawLines_(false);
		~controlsVerbMenu.fillColor_(Color(0, 0.25, 0.5));
		~controlsVerbMenu.strokeColor_(Color.cyan);
		~controlsVerbMenu.drawRects_(true);
		~controlsVerbMenu.xOffset_(8);
		~controlsVerbMenu.thumbSize_(16);
		~controlsVerbMenu.value_([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5]);
		~controlsVerbMenu.elasticMode_(1);
		~controlsVerbMenu.action={arg controls;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~controlsVerbMenu", controls.value)});
			~controlsSynthVerb.wrapPut(~verbInstrMenu.value, controls.value);
			~listSynthVerb.wrapAt(~verbInstrMenu.value).set(\control1, controls.value.wrapAt(0), \control2, controls.value.wrapAt(1), \control3, controls.value.wrapAt(2), \control4, controls.value.wrapAt(3), \control5, controls.value.wrapAt(4), \control6, controls.value.wrapAt(5), \control7, controls.value.wrapAt(6), \control8, controls.value.wrapAt(7));
		};
		// Jitter controls Verb
		~jitterControlsVerb=EZKnob(~wv, 55 @ 75, "JitterCtrls", ControlSpec(0, 1, \lin, 0), {arg view; ~jitterControlsSynthVerb.wrapPut(~verbInstrMenu.value, view.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~jitterControlsVerb", view.value)})}, 0.1, labelWidth: 55,unitWidth: 0, layout: 'vert');
		// Random Verb
		~randomControlsVerb= Button(~wv,Rect(10,10, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~randomControlsVerb.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~randomControlsVerb", view.value)});
			~automationControlsVerb.wrapPut(~verbInstrMenu.value, view.value);
		};
		~wv.view.decorator.nextLine;
		// Amp
		~ampVerb=EZKnob(~wv, 55 @ 75, "Amp", \db,
			{|ez| ~listSynthVerb.wrapAt(~verbInstrMenu.value).set('amp', ez.value.dbamp); ~ampSynthVerb.wrapPut(~verbInstrMenu.value, ez.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~ampVerb", ez.value)})}, -12, labelWidth: 50,unitWidth: 0, layout: 'vert');
		// PAN
		~panVerb=EZKnob(~wv, 55 @ 75, "Pan",ControlSpec(-1, 1, \lin, 0.001),
			{|ez| ~listSynthVerb.wrapAt(~verbInstrMenu.value).set('pan', ez.value);~panSynthVerb.wrapPut(~verbInstrMenu.value, ez.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~panVerb", ez.value)})},0.0, labelWidth: 40,unitWidth: 0, layout: 'vert');
		// Jitter Pan Verb
		~jitterPanVerb=EZKnob(~wv, 55 @ 75, "JitterPan", ControlSpec(0, 1, \lin, 0), {arg view;
			~jitterPanSynthVerb.wrapPut(~verbInstrMenu.value, view.value);if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~jitterPanVerb", view.value)})}, 0.1, labelWidth: 40,unitWidth: 0, layout: 'vert');
		// Random Pan
		~randomPanVerb= Button(~wv,Rect(10,10, 25, 18)).states=[["On", Color.black, Color.green(0.8, 0.25)],["Off", Color.black, Color.red(0.8, 0.25)]];
		~randomPanVerb.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~randomPanVerb", view.value)});
			~automationPanVerb.wrapPut(~verbInstrMenu.value, view.value);
			if(view.value == 0, {~listSynth
				.wrapAt(~verbInstrMenu.value).set('pan', ~panSynthVerb.wrapAt(~verbInstrMenu.value))});
		};
		// Speed Autoamtion Verb
		~speedVerb = EZKnob(~wv, 55 @ 75, "Speed",ControlSpec(0.01, 100, \exp, 0.01),
			{|view| ~automationSpeedVerb.wrapPut(~verbInstrMenu.value, view.value); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~speedVerb", view.value)});
				~listeVerbTime.put(~verbInstrMenu.value, 24.reciprocal);
		},24, labelWidth: 40,unitWidth: 0, layout: 'vert');

	}

	controlPanel {

		// Control Panel Agents
		~wp = Window(~nomFenetre, Rect(0, 800, 1030, 525), scroll: true);
		~wp.view.decorator = FlowLayout(~wp.view.bounds);
		~wp.alpha=1.0;
		~wp.front;
		// OS USER OPERATING SYSTEM
		~userOperatingSystem = PopUpMenu(~wp,Rect(0, 0, 300, 20)).background_(Color.grey(0.5, 0.8)).items = ~userOSchoice;
		~userOperatingSystem.action={arg item, file, addrM, addrS;
			//if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~userOperatingSystem", item.value)});
			~fonctionUserOperatingSystem.value(item, file, addrM, addrS);
			~userOperatingSystem.value=0;
		};
		~hourElapsed = NumberBox(~wp, 20@20).stringColor=Color.red(1);
		~hourElapsed.enabled_(false);
		~minuteElapsed = NumberBox(~wp, 20@20).stringColor=Color.blue(1);
		~minuteElapsed.enabled_(false);
		~secondElapsed = NumberBox(~wp, 35@20).stringColor=Color.black(1);
		~secondElapsed.enabled_(false);
		StaticText(~wp, Rect(0, 0, 500, 20)).string_("Agents by Provinescu's Software Production").stringColor_(Color.white).font_(Font("Georgia", 12)).align = \right;
		~wp.view.decorator.nextLine;
		StaticText(~wp, Rect(10,10,500,20)).string_("System Agents").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		~startsysteme = Button(~wp,Rect(10,10,75,20)).states=[["System On", Color.black, Color.green(0.8, 0.25)],["System Off", Color.black, Color.red(0.8, 0.25)]];
		~startsysteme.action = {arg view;
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPstart', view.value)});// Send Synchro Start
			if (view.value == 1, {
				~tempoMusicPlay.stop;// Stop le tempo general
				// Creation tempo general
				~tempoMusicPlay = TempoClock.default = TempoClock.new(1, 0, Main.elapsedTime);
				~tempoMusicPlay.tempo_(~tempoSlider.value / 60);
				~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~tempoMusicPlay.beatsPerBar_(~nombreBeatsBare);nil});
				~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~compteurAnalyse=0;~freqBefore=0;
				~ampBefore=0; ~dureeBefore=0; ~freqTampon=nil; ~amptampon=nil;
				switch(~entreemode,
					'Audio', {~audioFile.value.run(false);~tempoFile.value.run(false);~synthPlayFile.value.run(false);
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~audioIn.value.run(true);~tempoIn.value.run(true);nil});
						~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(true);~recFiles.wrapAt(i).run(false)});
						~listesamplein=~recSamples;
						~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).run(false)});
						MIDIIn.disconnect;
					},
					'File', {~audioIn.value.run(false);~tempoIn.value.run(false);
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~audioFile.value.run(true);~tempoFile.value.run(true);~synthPlayFile.value.run(true);~synthPlayFile.set('trig', 1);nil});
						~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(false); ~recFiles.wrapAt(i).run(true)});
						~listesamplein=~recFiles;
						~agents.do({arg i; ~synthRecAudioAgents.wrapAt(i).run(false);~synthRecFileAgents.wrapAt(i).run(true)});
						MIDIIn.disconnect;
					},
					'Midi', {~audioIn.value.run(false);~audioFile.value.run(false);~synthPlayFile.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(false);
						MIDIIn.connect;
					},
					'Off', {~audioIn.value.run(false);~audioFile.value.run(false);~synthPlayFile.value.run(false);
						MIDIIn.disconnect;
					}
				);
				// Set on Routines
				~routineAgents.play(quant: Quant.new(~nombreBeatsBare));
				if(~musique.value==1,{~agents.do({arg agent; ~routineMusic.wrapAt(agent).play(quant: Quant.new(~nombreBeatsBare))})});
				if(~viewworld.value==1,{~routineVirtual.play(quant: Quant.new(~nombreBeatsBare))});
				if(~affichage.value==1,{~routineData.play(quant: Quant.new(~nombreBeatsBare))});
				if(~startAutomation.value==1,{~routineAutomation.play(quant: Quant.new(~nombreBeatsBare))});
				~routineMetronome.play(quant: Quant.new(~nombreBeatsBare));
				// Setup Recording on
				if(~flagRecording == 'on', {s.record});
				//Start ScoreEdit
				if(~flagHPscorePlaying == 'on', {~routineScore.value(~scorePlaying).play(quant: Quant.new(~nombreBeatsBare))});
				//Start Scores
				40.do({arg i;
					~listeRoutinePlayingScore.wrapAt(i).size.do({arg ii;
						if(~listeFlagRoutinePlayingScore.wrapAt(i).wrapAt(ii) == 'on', {~listeRoutinePlayingScore.wrapAt(i).wrapAt(ii).play(quant: Quant.new(~nombreBeatsBare))})});
				});
				~timeElapsedStart = Main.elapsedTime;
				if(~oscStateFlag == 'master', {~ardourOSC.sendMsg('/ardour/transport_play')});// transport play
				~routineAutomationEffets.play(quant: Quant.new(~nombreBeatsBare));
				~routineAutomationVerb.play(quant: Quant.new(~nombreBeatsBare));
			},
			{// Stop les Tdef
				~synthPlayFile.set('trig', 0);
				~sounds.size.do({arg i; ~recSamples.wrapAt(i).run(false);~recFiles.wrapAt(i).run(false)});
				~routineVirtual.stop;~routineData.stop;~routineAutomation.stop;~routineAgents.stop;
				~routineMetronome.stop;~metronomeGUI.value_(0);
				~audioIn.value.run(false);~tempoIn.value.run(false);~audioFile.value.run(false);~tempoFile.value.run(false);~synthPlayFile.value.run(false);
				~agents.do({arg agent; ~routineMusic.wrapAt(agent).stop;~musicOutAgents.wrapPut(agent , 0);~synthRecAudioAgents.wrapAt(agent).run(false);~synthRecFileAgents.wrapAt(agent).run(false);
					~flagCompteurPlayingAgents.wrapPut(agent, 0);
					//~listeagentfreq.wrapPut(agent, []);~listeagentamp.wrapPut(agent, []);~listeagentduree.wrapPut(agent, []);~listeagentID.wrapPut(agent, []);~compteur.wrapPut(agent,0);
				});
				//~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~compteurAnalyse=0;~freqBefore=0; ~ampBefore=0; ~dureeBefore=0; ~freqTampon=nil; ~amptampon=nil;
				if(~flagRecording == 'on', {s.pauseRecording});
				~tempoMusicPlay.clear;
				//Stop ScoreEdit
				if(~flagHPscorePlaying == 'on', {~routineScore.value(~scorePlaying).stop});
				//Stop Scores
				40.do({arg i;
					~listeRoutinePlayingScore.wrapAt(i).size.do({arg ii;
						if(~listeFlagRoutinePlayingScore.wrapAt(i).wrapAt(ii) == 'on', {~listeRoutinePlayingScore.wrapAt(i).wrapAt(ii).stop})});
				});
				if(~oscStateFlag == 'master', {~ardourOSC.sendMsg('/ardour/transport_stop')});// transport stop
				//if(~oscStateFlag == 'master', {~ardourOSC.sendMsg('/ardour/goto_start')});// transport start
				~routineAutomationEffets.stop;
				~routineAutomationVerb.stop;
			});
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
		};
		~startsysteme.focus;
		~musique = Button(~wp,Rect(10,10,75,20)).states=[["Music On", Color.black, Color.green(0.8, 0.25)],["Music Off", Color.black, Color.red(0.8, 0.25)]];
		~musique.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~musique", view.value)});
			if (view.value == 1, {
				if(~startsysteme.value == 1, {~agents.do({arg agent; ~flagCompteurPlayingAgents.wrapPut(agent, 0);
					~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {~routineMusic.wrapAt(agent).play(quant: Quant.new(1));nil});
			})})},
			{~agents.do({arg agent; ~routineMusic.wrapAt(agent).stop;~musicOutAgents.wrapPut(agent , 0)})});
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
		};
		~viewworld = Button(~wp,Rect(10,10,100,20)).states=[["View Virtual On", Color.black, Color.green(0.8, 0.25)],["View Virtual Off", Color.black, Color.red(0.8, 0.25)]];
		~viewworld.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~viewworld", view.value)});
			if (view.value == 1,
				{if(~startsysteme.value == 1, {~routineVirtual.play(quant: Quant.new(1))})},
				{~routineVirtual.stop});
		};
		~affichage = Button(~wp,Rect(10,10, 100,20)).states=[["State System On", Color.black, Color.green(0.8, 0.25)],["State System Off", Color.black, Color.red(0.8, 0.25)]];
		~affichage.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~affichage", view.value)});
			if (view.value == 1,
				{if(~startsysteme.value == 1, {~routineData.play(quant: Quant.new(1))})},
				{~routineData.stop});
		};
		~tempoButton = Button(~wp,Rect(10,10, 150, 20)).states=[["Tempo Analyze On", Color.black, Color.green(0.8, 0.25)],["Tempo Analyze Off", Color.black, Color.red(0.8, 0.25)]];
		~tempoButton.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~tempoButton", view.value)});
			if (view.value == 1,
				{~flagTempoAnalyze = 'on';~tempoSlider.enabled_(false)},
				{~flagTempoAnalyze = 'off';~tempoSlider.valueAction = 60;~tempoSlider.enabled_(true)});
		};
		~tempoAgentsSlider=EZKnob(~wp, 150 @ 20, "Tempo System",ControlSpec(1, 100, \lin, 1),
			{|ez| ~tempoData = ~tempoagents=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~tempoAgentsSlider", ez.value)})},~tempoagents, labelWidth: 85,unitWidth: 0, layout: 'horz');
		StaticText(~wp, Rect(0, 0, 100, 20)).string_("SoundIn for Analyze").stringColor_(Color.white).font_(Font("Georgia", 10));
		~audioInputAnalyzeButton=PopUpMenu(~wp,Rect(0, 0, 70, 18)).items = ~audioInLR;
		~audioInputAnalyzeButton.action = {arg input;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~audioInputAnalyzeButton", input.value)});
			~audioIn.setn(\in, input.value);
			~tempoIn.setn(\in, input.value);
		};
		~audioInputAnalyzeButton.value_(0);
		// Affichage Metronome
		~metronomeGUI = EZNumber(~wp, 105@20, "Metronome", ControlSpec(0, 64, \lin, 0), {arg view; nil}, 0, false, labelWidth:75, numberWidth:40);
		~wp.view.decorator.nextLine;
		StaticText(~wp, Rect(10,10,500,20)).string_("Analyze Input Signal").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~textFileAnalyze = StaticText(~wp, Rect(0, 0, 500, 20)).string_("a11wlk01-44_1.aiff").stringColor_(Color.white).font_(Font("Georgia", 10)).align_(\center);
		~wp.view.decorator.nextLine;
		~entree = PopUpMenu(~wp,Rect(10,10,75,20)).items=~listemodeentree;
		~entree.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~entree", view.value)});
			~entreemode=~listemodeentree.wrapAt(view.value);
			switch(~entreemode,
				'Audio', {~audioFile.value.run(false);~tempoFile.value.run(false);~synthPlayFile.value.run(false);~keyCodeSourceIn=0;
					if(~startsysteme.value == 1, {~audioIn.value.run(true);~tempoIn.value.run(true)});
					//~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
					~listesamplein=~recSamples;
					~sounds.size.do({arg i;
						~recSamples.wrapAt(i).run(true);~recFiles.wrapAt(i).run(false);
						~listesamplein.wrapAt(i).set(\run, ~recSamplesButtonSons.wrapAt(i).value,\loop, ~recSamplesLoopSons.wrapAt(i).value,\trigger, ~recSamplesButtonSons.wrapAt(i).value,\reclevel1, ~recSamplesLevelsSons.wrapAt(i).wrapAt(0),\reclevel2, ~recSamplesLevelsSons.wrapAt(i).wrapAt(1));
					});
					~agents.do({arg i;
						~synthRecAudioAgents.wrapAt(i).run(true);~synthRecFileAgents.wrapAt(i).run(false);
						~synthRecAudioAgents.wrapAt(i).set(\reclevel1, ~levelsValues.wrapAt(0), \reclevel2, ~levelsValues.wrapAt(1));
					});
					~flagEntreeMode='Audio';
					MIDIIn.disconnect;
					~canalMidiInSlider.enabled_(false);~offsetFileIn.enabled_(false);
					~volumeFileIn.enabled_(false)},
				'File', {~audioIn.value.run(false);~tempoIn.value.run(false);~keyCodeSourceIn=1;
					if(~startsysteme.value == 1, {~audioFile.value.run(true);~tempoFile.value.run(true);~synthPlayFile.value.run(true);~synthPlayFile.set(\volume, ~volumeFileIn.value.dbamp)});
					//~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
					~listesamplein=~recFiles;
					~sounds.size.do({arg i;
						~recSamples.wrapAt(i).run(false);~recFiles.wrapAt(i).run(true);
						~listesamplein.wrapAt(i).set(\run, ~recSamplesButtonSons.wrapAt(i).value,\loop, ~recSamplesLoopSons.wrapAt(i).value,\trigger, ~recSamplesButtonSons.wrapAt(i).value,\reclevel1, ~recSamplesLevelsSons.wrapAt(i).wrapAt(0),\reclevel2, ~recSamplesLevelsSons.wrapAt(i).wrapAt(1));
					});
					~agents.do({arg i;
						~synthRecAudioAgents.wrapAt(i).run(false);~synthRecFileAgents.wrapAt(i).run(true);
						~synthRecFileAgents.wrapAt(i).set(\reclevel1, ~levelsValues.wrapAt(0), \reclevel2, ~levelsValues.wrapAt(1));
					});
					~flagEntreeMode='File';
					MIDIIn.disconnect;
					~canalMidiInSlider.enabled_(false);~offsetFileIn.enabled_(true);
					~volumeFileIn.enabled_(true)},
				'Midi', {MIDIIn.connect;~keyCodeSourceIn=2;
					~canalMidiInSlider.enabled_(true);~tempoSlider.valueAction = 60;
					//~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
				},
				'Off',{
					//~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
					~keyCodeSourceIn=3;
					MIDIIn.disconnect;
					~canalMidiInSlider.enabled_(false)}
			);
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
		};
		~canalMidiInSlider=PopUpMenu(~wp, 70 @ 20).items=["MIin 1", "MIin 2", "MIin 3", "MIin 4", "MIin 5", "MIin 6", "MIin 7", "MIin 8", "MIin 9", "MIin 10", "MIin 11", "MIin 12", "MIin 13", "MIin 14", "MIin 15", "MIin 16"];
		~canalMidiInSlider.action={arg canal;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~canalMidiInSlider", canal.value)});
			~canalMidiIn=canal.value; ~canauxMidiInOut.wrapPut(0, canal.value);
			NoteOnResponder.removeAll;~fonctionOSCMidiIn.value};
		~canalMidiInSlider.enabled_(false);
		~canalMidiOutSlider=PopUpMenu(~wp, 70 @ 20).items=["MIout Off", "MIout 1", "MIout 2", "MIout 3", "MIout 4", "MIout 5", "MIout 6", "MIout 7", "MIout 8", "MIout 9", "MIout 10", "MIout 11", "MIout 12", "MIout 13", "MIout 14", "MIout 15", "MIout 16"];
		~canalMidiOutSlider.action={arg canal;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~canalMidiOutSlider", canal.value)});
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});
			~canalMidiOut=canal.value - 1; ~canauxMidiInOut.wrapPut(1, canal.value - 1);
			~agents.do({arg agent; ~canalMidiOutAgent.wrapPut(agent, canal.value - 1)});
		};
		~canalMidiOutSlider.enabled_(false);// Attention si MIDI on alors comment this line
		~algoAnalyse = PopUpMenu(~wp,Rect(10, 10, 100, 20)).items=["Onsets","Pitch", "Pitch2", "KeyTrack", "Keyboard"];
		~algoAnalyse.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~algoAnalyse", view.value)});
			~seuilInSlider.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(0));
			~filtreInSlider.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(1));
			~creationSynthDefAnalyze.value(view.value);
			~flagAlgoAnalyze = view.value;
		};
		~choiceFilter = PopUpMenu(~wp,Rect(0, 0, 75, 20)).
		items_(["Off", "LoPass", "HiPass"]).
		action = {|filter|
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~choiceFilter", filter.value)});
			if(filter.value == 0, {
				~groupeAnalyse.setn(\ampInput, 1, \ampLoPass, 0, \ampHiPass, 0);
				~hzFilter.enabled_(false);
			});
			if(filter.value == 1, {
				~groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 1, \ampHiPass, 0);
				~hzFilter.enabled_(true);
			});
			if(filter.value == 2, {
				~groupeAnalyse.setn(\ampInput, 0, \ampLoPass, 0, \ampHiPass, 1);
				~hzFilter.enabled_(true);
			});
		};
		~hzFilter = EZKnob(~wp, 150 @ 20, "FHzPass", \freq,
			{|ez|
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~hzFilter", ez.value)});
				~groupeAnalyse.set(\hzPass, ez.value)},
			440, labelWidth: 50,unitWidth: 0, layout: 'horz');
		~hzFilter.enabled_(false);
		// Number FhzBand 28
		~numberBand = EZKnob(~wp, 110 @ 20, "FhzBand", ControlSpec(1, 12, \lin, 1),
			{|ez|
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~numberBand", ez.value)});
				~numFhzBand = ez.value;
				~bandFHZ = Array.fill(~numFhzBand, {arg i; [127 / ~numFhzBand * i, 127 / ~numFhzBand * i + (127 / ~numFhzBand )]});
				~bandFHZ = ~bandFHZ.reverse;
				~bandFHZ = ~bandFHZ.add([0, 127]);
				~bandFHZ = ~bandFHZ.reverse;
				~rangeSynthBand =[];
				for(0, ~numFhzBand,
					{arg index;
						~rangeSynthBand = ~rangeSynthBand.add(index); // Band active
						~wp.view.children.wrapAt(24 + index).enabled_(true);
						~wg.view.children.wrapAt(58 + index).enabled_(true);// Gene
				});
				if(~numFhzBand < 12, {
					for(~numFhzBand + 1, 12,
						{arg index;
							~wp.view.children.wrapAt(24 + index).enabled_(false);
							~wp.view.children.wrapAt(24 + index).valueAction_(0);
							~wg.view.children.wrapAt(58 + index).enabled_(false);// Gene
							~wg.view.children.wrapAt(58 + index).valueAction_(0);// gene
					});
				});
				rangeBand.value = ~bandFHZ.round(2);
		}, 12, layout: \horz);
		// SynthBand
		// Band 0 to 12
		~synthBand0 = Button.new(~wp, 16 @ 20).
		states_([["0", Color.green], ["0", Color.red]]).
		action_({arg band; ~flagBandSynth.put(0, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand0", band.value)});
		}); // all Band 29
		~synthBand1 = Button.new(~wp, 16 @ 20).
		states_([["1", Color.green], ["1", Color.red]]).
		action_({arg band; ~flagBandSynth.put(1, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand1", band.value)});
		});
		~synthBand2 = Button.new(~wp, 16 @ 20).
		states_([["2", Color.green], ["2", Color.red]]).
		action_({arg band; ~flagBandSynth.put(2, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand2", band.value)});
		});
		~synthBand3 = Button.new(~wp, 16 @ 20).
		states_([["3", Color.green], ["3", Color.red]]).
		action_({arg band; ~flagBandSynth.put(3, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand3", band.value)});
		});
		~synthBand4 = Button.new(~wp, 16 @ 20).
		states_([["4", Color.green], ["4", Color.red]]).
		action_({arg band; ~flagBandSynth.put(4, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand4", band.value)});
		});
		~synthBand5 = Button.new(~wp, 16 @ 20).
		states_([["5", Color.green], ["5", Color.red]]).
		action_({arg band; ~flagBandSynth.put(5, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand5", band.value)});
		});
		~synthBand6 = Button.new(~wp, 16 @ 20).
		states_([["6", Color.green], ["6", Color.red]]).
		action_({arg band; ~flagBandSynth.put(6, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand6", band.value)});
		});
		~synthBand7 = Button.new(~wp, 16 @ 20).
		states_([["7", Color.green], ["7", Color.red]]).
		action_({arg band; ~flagBandSynth.put(7, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand7", band.value)});
		});
		~synthBand8 = Button.new(~wp, 16 @ 20).
		states_([["8", Color.green], ["8", Color.red]]).
		action_({arg band; ~flagBandSynth.put(8, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand8", band.value)});
		});
		~synthBand9 = Button.new(~wp, 16 @ 20).
		states_([["9", Color.green], ["9", Color.red]]).
		action_({arg band; ~flagBandSynth.put(9, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand9", band.value)});
		});
		~synthBand10 = Button.new(~wp, 16 @ 20).
		states_([["10", Color.green], ["10", Color.red]]).
		action_({arg band; ~flagBandSynth.put(10, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand10", band.value)});
		});
		~synthBand11 = Button.new(~wp, 16 @ 20).
		states_([["11", Color.green], ["11", Color.red]]).
		action_({arg band; ~flagBandSynth.put(11, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand11", band.value)});
		});
		~synthBand12 = Button.new(~wp, 16 @ 20).
		states_([["12", Color.green], ["12", Color.red]]).
		action_({arg band; ~flagBandSynth.put(12, band.value);
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthBand12", band.value)});
		});

		// flagFhzBand 42
		~buttonSynthBand = Button(~wp, Rect(0, 0, 40, 20))
		.states_([["On", Color.green], ["Off", Color.red]])
		.action = {arg flag;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~buttonSynthBand", flag.value)});
			if(flag.value == 0,
				{~flagSynthBand = 'off';
					//~numberBand.enabled_(false);
					~synthBand0.enabled_(false);
					~synthBand1.enabled_(false);
					~synthBand2.enabled_(false);
					~synthBand3.enabled_(false);
					~synthBand4.enabled_(false);
					~synthBand5.enabled_(false);
					~synthBand6.enabled_(false);
					~synthBand7.enabled_(false);
					~synthBand8.enabled_(false);
					~synthBand9.enabled_(false);
					~synthBand10.enabled_(false);
					~synthBand11.enabled_(false);
					~synthBand12.enabled_(false);
				},
				{~flagSynthBand = 'on';
					~buttonGeneBand.valueAction = 0;
					//~numberBand.enabled_(true);
					for(0, ~numFhzBand,
						{arg index;
							~wp.view.children.wrapAt(24 + index).enabled_(true);
					});
					if(~numFhzBand < 12, {
						for(~numFhzBand + 1, 12,
							{arg index;
								~wp.view.children.wrapAt(24 + index).enabled_(false);
						});
					});
			});
		};
		~wp.view.decorator.nextLine;
		~seuilInSlider=EZKnob(~wp, 80 @ 80, "Thresh In", \unipolar, {|seuil|  if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~seuilInSlider", seuil.value)});
			~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapPut(0, seuil.value);~audioIn.set(\seuil, seuil.value);
			~audioFile.set(\seuil, seuil.value)}, 0.5, labelWidth: 55,unitWidth: 0, layout: 'vert');
		~filtreInSlider=EZKnob(~wp, 80 @ 80, "Filter In", \unipolar, {|filtre|
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~filtreInSlider", filtre.value)});
			~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapPut(1, filtre.value);
			~audioIn.set(\filtre, filtre.value);~audioFile.set(\filtre, filtre.value)}, 0.5, labelWidth: 40,unitWidth: 0, layout: 'vert');
		~audioFreqSlider=EZKnob(~wp, 80 @ 80, "Filter Fhz",ControlSpec(0, 12, \lin, 0.01),
			{|ez| ~differencefreq=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~audioFreqSlider", ez.value)})},~differencefreq, labelWidth: 50,unitWidth: 0, layout: 'vert');
		~audioAmpSlider=EZKnob(~wp, 80 @ 80, "Filter Amp",ControlSpec(0, 60, \lin, 0.1),
			{|ez| ~differenceamp=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~audioAmpSlider", ez.value)})},~differenceamp, labelWidth: 55,unitWidth: 0, layout: 'vert');
		~audiodureeSlider=EZKnob(~wp, 80 @ 80, "Filter Dur",ControlSpec(0.01, 16, \exp, 0),
			{|ez| ~differenceduree=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~audiodureeSlider", ez.value)})},~differenceduree, labelWidth: 55,unitWidth: 0, layout: 'vert');

		~dureeMaximumSlider=EZKnob(~wp, 80 @ 80, "Dur Max",ControlSpec(1, 60, \exp, 0),
			{|ez| ~dureeanalysemax=ez.value;~distanceagents=~distanceAgentsSlider.value * sqrt(1+1+~dureeanalysemax.squared); ~distancesignaux=~distanceSignauxSlider.value * sqrt(1+1+~dureeanalysemax.squared); ~deviance=~devianceSlider.value * sqrt(1+1+~dureeanalysemax.squared); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dureeMaximumSlider", ez.value)})},~dureeanalysemax, labelWidth: 55,unitWidth: 0, layout: 'vert');
		~dureeSilenceSlider=EZKnob(~wp, 80 @ 80, "Memory Time",ControlSpec(1, 3600, \exp, 0),
			{|ez| ~tempsmaxsignal=ez.value;
				~geneBufferRanger.controlSpec_(ControlSpec(0.01, ez.value, \exp, 0));
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dureeSilenceSlider", ez.value)})},~tempsmaxsignal, labelWidth: 45,unitWidth: 0, layout: 'vert');
		~dataSignauxSlider=EZKnob(~wp, 80 @ 80, "Signal buffer",ControlSpec(1, 64, \exp, 1),
			{|ez| ~listedatasizein=ez.value;~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];~agents.do({arg agent; ~listeagentID.wrapPut(agent, [])}); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dataSignauxSlider", ez.value)})},~listedatasizein, labelWidth: 70,unitWidth: 0, layout: 'vert');
		~volumeFileIn=EZKnob(~wp, 80 @ 80, "Level FileIn", ControlSpec(-120, 12, \lin, 0),
			{|ez| ~synthPlayFile.set(\volume, ez.value.dbamp); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~volumeFileIn", ez.value)})}, -inf, labelWidth: 70,unitWidth: 0, layout: 'vert');
		~volumeFileIn.enabled_(false);
		~offsetFileIn=EZKnob(~wp, 80 @ 80, "Offset FileIn", ControlSpec(0, 1, \lin, 0),
			{|ez|
				s.bind{
					~synthPlayFile.set(\trig, -1);
					s.sync;
					~synthPlayFile.set(\offset, ez.value);
					s.sync;
					~synthPlayFile.set(\trig, 1);
					s.sync};
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~offsetFileIn", ez.value)})}, 0, labelWidth: 70,unitWidth: 0, layout: 'vert');
		~offsetFileIn.enabled_(false);
		StaticText(~wp, Rect(10,10,25,20)).string_("Algo").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~choiceAlgoData = PopUpMenu(~wp,Rect(0, 0, 100, 20)).
		items_(~listeAlgorithm).
		action = {|algo|
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~choiceAlgoData", algo.value)});
			~algoMusic = ~listeAlgorithm.wrapAt(algo.value);
		};
		~wp.view.decorator.nextLine;
		StaticText(~wp, Rect(10,10,500,20)).string_("Crew").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		~initsysteme = Button(~wp,Rect(10,10,75,20)).states=[["Init Crew", Color.red, Color.grey]];
		~initsysteme.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~initsysteme", 1)});
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
			~foncInitAgents.value;
		};
		~foncInitAgents={
			// Free all Buffers Agents
			~groupeSynthRecAgents.freeAll;
			~agents.do({arg a;
				~bufferAudioAgents.wrapAt(a).free;
				~bufferFileAgents.wrapAt(a).free;
			});
			//Remove Tdef Agents
			if(~routineMusic.size >= 1, {~routineMusic.do({arg routine; routine.stop;routine.remove})});
			~routineMusic=[];
			~vies=[];
			~ages=[];
			~enfants=[];
			~fitnessInne=[];
			~fitnessAcquis=[];
			~listeagentfreq=[];
			~listeagentamp=[];
			~listeagentduree=[];
			~listeagentID=[];
			~genomes=[];
			~agentspositionx=[];
			~agentspositiony=[];
			~agentspositionz=[];
			~dureesmusique=[];
			~freqMidi=[];
			~flagCompteurPlayingAgents=[];
			~voisins=[];
			~signaux=[];
			~voisinsAffichage=[];
			~signauxAffichage=[];
			~couleurs=[];
			~fitnessAcquisvoisins=[];
			~voisinsOkCopulation=[];
			~flagplayagent=[];
			~compteur=[];
			~bufferAudioAgents=[];
			~bufferFileAgents=[];
			~synthRecAudioAgents=[];
			~synthRecFileAgents=[];
			~musicOutAgents=[];
			~canalMidiOutAgent=[];
			~listeIDTdefAgents=[];
			~bufferDataAgents=[];
			~chordMaxAgents=[];
			~chordDurAgents=[];
			~liensParents=[];
			~kohonenF = [];
			~kohonenA = [];
			~kohonenD = [];
			~agentsBand = [];
			~agents=~startpopulation;
			// initialisation pour debut processus
			16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
			~agents.do({arg agent; ~initagents.value(agent, nil, nil, nil, 'init', [], [], [], 0, 0, 0)});
		};
		~populationInitialeSlider=EZKnob(~wp, 150 @ 20, "Starting Crew",ControlSpec(1, ~maximumabsoluagents, \lin, 1),
			{|ez| ~startpopulation=ez.value.asInteger; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~populationInitialeSlider", ez.value)})},~startpopulation, labelWidth: 90,unitWidth: 0, layout: 'horz');
		~agentsMaximumSlider=EZKnob(~wp, 150 @ 20, "Maximum Agent",ControlSpec(1, ~maximumabsoluagents, \lin, 1),
			{|ez| ~maximumagents=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~agentsMaximumSlider", ez.value)})},~maximumagents, labelWidth: 90,unitWidth: 0, layout: 'horz');
		~vieAgentsSlider=EZKnob(~wp, 150 @ 20, "Life Duration",ControlSpec(1, ~dureemaximumvie, \exp, 1),
			{|ez| ~dureeVieAgents=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~vieAgentsSlider", ez.value)})}, 60,  labelWidth: 90,unitWidth: 0, layout: 'horz');
		~generationAgentsSlider=EZKnob(~wp, 150 @ 20, "Inertia Generation",ControlSpec(0.001, 100, \exp, 0.01),
			{|ez| ~vitessegeneration=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~generationAgentsSlider", ez.value)});
				~agesAgentsSlider.valueAction_(~trancheage.value);
		},~vitessegeneration,  labelWidth: 90,unitWidth: 0, layout: 'horz');
		~agesAgentsSlider=EZRanger(~wp, 300 @ 20, "Age Reproduction",ControlSpec(0, 1, \lin, 0),
			{|ez| ~trancheage=ez.value;~jeunesse=~dureeVieAgents - (~trancheage.wrapAt(0) * ~dureeVieAgents);~vieilliesse=~dureeVieAgents - (~trancheage.wrapAt(1) * ~dureeVieAgents); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~agesAgentsSlider", ez.value)})},~trancheage,  labelWidth: 100, unitWidth: 0);

		~wp.view.decorator.nextLine;
		StaticText(~wp, Rect(10,10,500,20)).string_("Musical").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		~loopseq = Button(~wp,Rect(10, 10, 100, 20)).states=[["Loop Music On", Color.black, Color.green(0.8, 0.25)],["Loop Music Off", Color.black, Color.red(0.8, 0.25)]];
		~loopseq.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~loopseq", view.value)});
			if (view.value == 1,
				{~flagloop='on'},{~flagloop='off'});
		};
		~dataAgentsSlider=EZKnob(~wp, 125 @ 20, "Music Buffer",ControlSpec(1, 256, \lin, 1),
			{|ez| ~listedatamusiqueagents=ez.value;
				~agents.do({arg agent;
					//~listeagentfreq.wrapPut(agent, []);~listeagentamp.wrapPut(agent, []);~listeagentduree.wrapPut(agent, []);
					~listeagentID.wrapPut(agent, []);~compteur.wrapPut(agent,0);~bufferDataAgents.wrapPut(agent, ~listedatamusiqueagents);
				});
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dataAgentsSlider", ez.value)})}, ~listedatamusiqueagents, labelWidth: 65,unitWidth: 0, layout: 'horz');
		~notesAccordsSlider=EZKnob(~wp, 125 @ 20, "Chord Max",ControlSpec(1, 12, \lin, 1),
			{|ez| ~maxaccord=ez.value;~agents.do({arg agent;~chordMaxAgents.wrapPut(agent, ~maxaccord)}); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~notesAccordsSlider", ez.value)})},~maxaccord, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~dureeAccordsSlider=EZKnob(~wp, 125 @ 20, "Chord Time",ControlSpec(0.01, 1, \exp, 0.000001),
			{|ez| ~dureeaccord=ez.value;~agents.do({arg agent;~chordDurAgents.wrapPut(agent, ~dureeaccord)}); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~dureeAccordsSlider", ez.value)})},~dureeaccord, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~tempoSlider=EZKnob(~wp, 100 @ 20, "BPM",ControlSpec(1, 960, \exp, 0),
			{|ez| if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPtempo', ez.value)});//Send Synchro Tempo
				~tempoMusic=ez.value;~tempoMusicPlay.tempo_(ez.value / 60); ~groupeMasterFX.set(\bpm, ez.value / 60);
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~tempoSlider", ez.value)});
		},~tempoMusic,  labelWidth: 30,unitWidth: 0, layout: 'horz');
		~nombreBeats=EZKnob(~wp, 100 @ 20, "Bar",ControlSpec(1, 64, \exp, 0),
			{|ez| if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPbare', ez.value)});// Send Synchro Bar
				~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {~nombreBeatsBare=ez.value; ~tempoMusicPlay.beatsPerBar_(~nombreBeatsBare);nil}); if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~nombreBeats", ez.value)})}, ~nombreBeatsBare,  labelWidth: 25,unitWidth: 0, layout: 'horz');
		~wp.view.decorator.nextLine;

		StaticText(~wp, Rect(10,10,500,20)).string_("Spatial (Virtual Space)").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		~toreButton=Button(~wp,Rect(0, 0, 100, 20)).states=[["Toric Space On", Color.black, Color.green(0.8, 0.25)],["Toric Space Off", Color.black, Color.red(0.8, 0.25)]];
		~toreButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~toreButton", action.value)});
			if(action.value == 1 ,{~flagTore='on';
				~routineAgentsAgents=~voisinsagentsTore;
				~routineSignauxAgents=~signauxvoisinsTore;
				~routineMoveAgents=~movingagentsTore},
			{~flagTore='off';
				~routineAgentsAgents=~voisinsagentsNoTore;
				~routineSignauxAgents=~signauxvoisinsNoTore;
				~routineMoveAgents=~movingagentsNoTore});
		};
		~flockButton=Button(~wp,Rect(0, 0, 100, 20)).states=[["Agent Flock On", Color.black, Color.green(0.8, 0.25)],["Agent Flock Off", Color.black, Color.red(0.8, 0.25)]];
		~flockButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~flockButton", action.value)});
			if(action.value == 1 ,{~flagFlock='on';
				~routineSignauxFlock=~movingsignauxFlock;
				~routineAgentsFlock=~movingagentsFlock},{~flagFlock='off';
				~routineSignauxFlock=~movingsignauxNoFlock;
				~routineAgentsFlock=~movingagentsNoFlock});
		};
		~distanceAgentsSlider=EZKnob(~wp, 160 @ 20, "%Distance Agent",ControlSpec(0.001, 1, \exp, 0),
			{|ez| ~distanceagents=ez.value * sqrt(1+1+~dureeanalysemax.squared);~dat=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~distanceAgentsSlider", ez.value)})},~distanceagents, labelWidth: 100,unitWidth: 0, layout: 'horz');
		~distanceSignauxSlider=EZKnob(~wp, 160 @ 20, "%Distance Signal",ControlSpec(0.001, 1, \exp, 0),
			{|ez| ~distancesignaux=ez.value * sqrt(1+1+~dureeanalysemax.squared);~dst=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~distanceSignauxSlider", ez.value)})},~distancesignaux, labelWidth: 100,unitWidth: 0, layout: 'horz');
		~devianceSlider=EZKnob(~wp, 160 @ 20, "%Deviance",ControlSpec(0.001, 1, \exp, 0), {|ez| ~deviance=ez.value * sqrt(1+1+~dureeanalysemax.squared);~dvt=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~devianceSlider", ez.value)})},~deviance, labelWidth: 100,unitWidth: 0, layout: 'horz');
		~vitesseAgentsSlider=EZKnob(~wp, 160 @ 20, "Inertia Agent",ControlSpec(0.00001, 64, \exp, 0),
			{|ez| ~vitesseagents=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~vitesseAgentsSlider", ez.value)})},~vitesseagents, labelWidth: 100,unitWidth: 0, layout: 'horz');
		~wp.view.decorator.nextLine;

		StaticText(~wp, Rect(10,10, 500, 20)).string_("Genetic Algorithm").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		~elitisteButton=Button(~wp,Rect(10, 10, 125, 20)).states=[["Elitist Reproduction On", Color.black, Color.green(0.8, 0.25)],["Elitist Reproduction Off", Color.black, Color.red(0.8, 0.25)]];
		~elitisteButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~elitisteButton", action.value)});
			if(action.value == 1 ,{~flagElitiste='on'},{~flagElitiste='off'});
		};
		~sharedMusicButton=Button(~wp,Rect(10, 10, 125, 20)).states=[["Shared Music Agent On", Color.black, Color.green(0.8, 0.25)],["Shared Music Agent Off", Color.black, Color.red(0.8, 0.25)]];
		~sharedMusicButton.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~sharedMusicButton", action.value)});
			if(action.value == 1 ,{~flagSharedMusic='on'},{~flagSharedMusic='off'});
		};
		~enfantsAgentsSlider=EZKnob(~wp, 120 @ 20, "Children",ControlSpec(0, ~maximumabsoluagents, \lin, 1),
			{|ez| ~maximumenfants=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~enfantsAgentsSlider", ez.value)})},~maximumenfants, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~naissanceAgentsSlider=EZKnob(~wp, 120 @ 20, "%Birth",ControlSpec(0, 1, \lin, 0),
			{|ez| ~naissance=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~naissanceAgentsSlider", ez.value)})},~naissance, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~croisementAgentsSlider=EZKnob(~wp, 120 @ 20, "%Crossover",ControlSpec(0, 1, \lin, 0),
			{|ez| ~croisement=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~croisementAgentsSlider", ez.value)})},~croisement, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~mutationAgentsSlider=EZKnob(~wp, 120 @ 20, "%Mutation",ControlSpec(0.001, 1, \exp, 0),
			{|ez| ~mutation=ez.value; if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~mutationAgentsSlider", ez.value)})},~mutation, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~learningAgentsSlider=EZKnob(~wp, 120 @ 20, "%Shared",ControlSpec(0, 1, \lin, 0),
			{|ez| ~learning=ez.value;if(~learning == 0, {~sharedMusicButton.enabled_(false)},{~sharedMusicButton.enabled_(true)});
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~learningAgentsSlider", ez.value)})},~learning, labelWidth: 60,unitWidth: 0, layout: 'horz');
		~wp.view.decorator.nextLine;

		StaticText(~wp, Rect(10,10, 150, 20)).string_("Tuning").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~wp.view.decorator.nextLine;
		// Tuning Analyze
		~listTuning = PopUpMenu(~wp, Rect(0, 0, 110, 20)).
		items_(["No Scale", "- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"]).
		action = {arg item;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~listTuning", item.value)});
			// Setup GUI Value
			~rootChoice.valueAction_(12);
			~rootChoice.valueAction_(0);
			~rootChoice.enabled_(true);
			~displayDegrees.enabled_(true);
			switch(item.value,
				// No Scale
				0, {~flagScaling = 'off';
					// Setup GUI Value
					~rootChoice.valueAction_(12);
					~rootChoice.valueAction_(0);
					~rootChoice.enabled_(false);
					~displayDegrees.enabled_(false);
				},
				// Tempered
				1, {nil},
				// Chromatic
				2, {~degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]},
				// Whole Tone 1
				3, {~degrees =  [0, 2, 4, 6, 8, 10]},
				// Major
				4, {~degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Minor
				5, {~degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Diminued
				6, {~degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Octatonic 1
				7, {~degrees =  [0, 1, 3, 4, 6, 7, 9, 10]},
				// Octatonic 2
				8, {~degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Nonatonique
				9, {~degrees =  [0, 2, 3, 4, 6, 7, 8, 10, 11]},
				// Messian 4
				10, {~degrees =  [0, 1, 2, 5, 6, 7, 8, 11]},
				// Messian 5
				11, {~degrees =  [0, 1, 5, 6, 7, 11]},
				// Messian 6
				12, {~degrees =  [0, 2, 4, 5, 6, 8, 10, 11]},
				// Messian 7
				13, {~degrees =  [0, 1, 2, 3, 5, 6, 7, 8, 9, 11]},
				// Bi-Pentaphonic
				14, {~degrees =  [0, 1, 2, 4, 5, 6, 7, 9, 10, 11]},
				// Major Pentatonic
				15, {~degrees =  [0, 2, 4, 7, 9]},
				// Minor Pentatonic
				16, {~degrees =  [0, 3, 5, 7, 10]},
				// Blues
				17, {~degrees =  [0, 3, 5, 6, 7, 10]},
				// Asavari
				18, {~degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Bhairava
				19, {~degrees =  [0, 1, 4, 5, 7, 8, 11]},
				// Bhairavi
				20, {~degrees =  [0, 1, 3, 5, 7, 8, 10]},
				// Bilaval
				21, {~degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Kafi
				22, {~degrees =  [0, 2, 3, 5, 7, 9, 10]},
				// Kalyan
				23, {~degrees =  [0, 2, 4, 6, 7, 9, 11]},
				// Khammaj
				24, {~degrees =  [0, 2, 4, 5, 7, 9, 10]},
				// Marava
				25, {~degrees =  [0, 1, 4, 6, 7, 9, 11]},
				// Pooravi
				26, {~degrees =  [0, 1, 4, 6, 7, 8, 11]},
				// Todi
				27, {~degrees =  [0, 1, 3, 6, 7, 8, 11]},
				// Indian Shrutis
				28, {nil},
				// 22tet
				29, {~degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]},
				// 12tet
				30, {~degrees =  [0, 2, 4, 6, 7, 9, 11, 13, 15, 16, 19, 20]},
				// Asavari
				31, {~degrees =  [0, 4, 6, 9, 13, 15, 19]},
				// Bhairava
				32, {~degrees =  [0, 2, 7, 9, 13, 15, 20]},
				// Bhairavi
				33, {~degrees =  [0, 3, 5, 9, 13, 15, 18]},
				// Bilaval
				34, {~degrees =  [0, 4, 7, 9, 13, 16, 20]},
				// Kafi
				35, {~degrees =  [0, 4, 6, 9, 13, 16, 19]},
				// Kalyan
				36, {~degrees =  [0, 4, 7, 11, 13, 16, 20]},
				// Khammaj
				37, {~degrees =  [0, 4, 7, 9, 13, 16, 19]},
				// Marava
				38, {~degrees =  [0, 2, 7, 11, 13, 16, 20]},
				// Pooravi
				39, {~degrees =  [0, 2, 7, 11, 13, 15, 20]},
				// Todi
				40, {~degrees =  [0, 2, 6, 11, 13, 15, 20]}
			);
			if(item.value > 1 and: {item.value < 28}, {~tuning = Tuning.et12; ~scale = Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning); ~flagScaling = 'on';
				// Setup GUI Value
				~displayDegrees.valueAction = ~degrees;
			});
			if(item.value > 28, {~tuning = Tuning.sruti; ~scale = Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning); ~flagScaling = 'on';
				// Setup GUI Value
				~displayDegrees.valueAction = ~degrees;
			});
		};

		// Root
		~rootChoice = EZKnob(~wp, 80 @ 20, "Root", ControlSpec(0, 21, \lin, 1),
			{|ez|
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~rootChoice", ez.value)});
				~root = ez.value; ~scale=Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning)}, 0, layout: \horz, labelWidth: 30);
		// Degrees
		~displayDegrees = EZText(~wp, Rect(0, 0, 415, 20), "Degrees",
			{arg string;
				if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~displayDegrees", string.value)});
				~degrees = string.value; ~scale=Scale.new(((~degrees + ~root)%~tuning.size).sort, ~tuning.size, ~tuning)},
			~degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true);
		//Range Band
		rangeBand = EZText(~wp, Rect(0, 0, 400, 20), "Range Band",
			{arg range; ~bandFHZ = range.value},
			[[0, 127], [0.0, 42.33], [42.33, 84.66], [84.66, 127.0] ], true, 70);
		~wp.view.decorator.nextLine;

		StaticText(~wp, Rect(10,10, 150, 20)).string_("Mean State System").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 12));
		~viewgenomes = StaticText(~wp, Rect(10,10, 1000, 20)).string_(~genomes.mean.asString).stringColor_(Color.yellow);
		~viewpopulation = StaticText(~wp, Rect(10,10, 125, 20)).string_("Crew"+~agents.asString).stringColor_(Color.yellow);
		~viewagesagents = StaticText(~wp, Rect(10,10, 80, 20)).string_("Age"+~ages.mediane.asString).stringColor_(Color.yellow);
		~viewviesagents = StaticText(~wp, Rect(10,10, 90, 20)).string_("Life"+~vies.mediane.asString).stringColor_(Color.yellow);
		~viewenfantsagents = StaticText(~wp, Rect(10,10, 100, 20)).string_("Children"+~enfants.mediane.asString).stringColor_(Color.yellow);
		~viewvoisins = StaticText(~wp, Rect(10,10, 113, 20)).string_("Neightboring").stringColor_(Color.yellow);
		~viewsignaux = StaticText(~wp, Rect(10,10, 113, 20)).string_("Signal").stringColor_(Color.yellow);
		~viewout = StaticText(~wp, Rect(10,10, 100, 20)).string_("Music OUT").stringColor_(Color.yellow);
		~viewin = StaticText(~wp, Rect(10,10, 100, 20)).string_("Signal IN"+~compteurAnalyse.asString).stringColor_(Color.yellow);
		~stateOSC = StaticText(~wp, Rect(10, 100, 150, 20)).string_("OSC Off").stringColor_(Color.yellow);
		~stateOSC.align = \center;
		~stateOSC.stringColor_(Color.blue).font_(Font("Georgia", 12));
		~stateOSC.background = Color.grey;
	}

	automationPanel {

		//Automation et Densite
		~wad = Window("Automation Density", Rect(595, 10, 415, 85), scroll: true);
		~wad.view.decorator = FlowLayout(~wad.view.bounds);
		~startAutomation = Button(~wad, Rect(0, 0, 125, 20)).states=[["All Automation On", Color.black, Color.green(0.8, 0.25)],["All Automation Off", Color.black, Color.red(0.8, 0.25)]];
		~startAutomation.action = {arg view;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~startAutomation", view.value)});
			if (view.value == 1 and: {~startsysteme.value == 1}, {~routineAutomation.play(quant: Quant.new(1))},{~routineAutomation.stop});
		};
		~startAutomation.focus;
		// Choix agents or SoundIn
		~choixAS = Button(~wad, Rect(0, 0, 75, 20)).states=[["SoundIn", Color.blue, Color.grey(0.8, 0.25)],["Agents", Color.blue, Color.grey(0.8, 0.25)]];
		~choixAS.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~choixAS", action.value)});
			if(action.value == 1, {~flagAS='Agents'}, {~flagAS='SoundIn'})};
		// Densite
		~densite=EZRanger(~wad, 165@20, "Time", ControlSpec(3600.reciprocal, 60, \exp, 0), {|valeur|  if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~densite", valeur.value)})}, [0.25, 0.5], labelWidth: 30, numberWidth: 30);
		~wad.view.decorator.nextLine;
		// Init
		~initAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["AgentI On", Color.black, Color.green(0.8, 0.25)],["AgentI Off", Color.black, Color.red(0.8, 0.25)]];
		~initAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~initAutomation", action.value)});
			if(action.value == 1, {~flagInitAutomation='on'}, {~flagInitAutomation='off'})};
		// Univers
		~universAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["PresetL On", Color.black, Color.green(0.8, 0.25)],["PresetL Off", Color.black, Color.red(0.8, 0.25)]];
		~universAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~universAutomation", action.value)});
			if(action.value == 1, {~flagUniversAutomation='on'}, {~flagUniversAutomation='off'})};
		// % Change Synth
		~changeSynth = NumberBox(~wad, Rect(0, 0, 50, 20));
		~changeSynth.action = {|view| if(~variableChange == "Flux", {~valueSynthChange.put(0,  view.value)}, {~valueSynthChange.put(1,  view.value)})};
		~changeSynth.step_(0.01); ~changeSynth.clipLo_(0); ~changeSynth.clipHi_(10); ~changeSynth.scroll_step_(0.01); ~changeSynth.value_(~valueSynthChange.wrapAt(0));
		// Monde Agents
		~mondesAgentsAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["ControlA On", Color.black, Color.green(0.8, 0.25)],["ControlA Off", Color.black, Color.red(0.8, 0.25)]];
		~mondesAgentsAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~mondesAgentsAutomation", action.value)});
			if(action.value == 1, {~flagMondesAgentsAutomation='on'}, {~flagMondesAgentsAutomation='off'})};
		// Monde Musique
		~mondesMusiqueAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["ControlM On", Color.black, Color.green(0.8, 0.25)],["ControlM Off", Color.black, Color.red(0.8, 0.25)]];
		~mondesMusiqueAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~mondesMusiqueAutomation", action.value)});
			if(action.value == 1, {~flagMondesMusiqueAutomation='on'}, {~flagMondesMusiqueAutomation='off'})};
		~wad.view.decorator.nextLine;
		// Genes Synth
		~genesSAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["GenesA On", Color.black, Color.green(0.8, 0.25)],["GenesA Off", Color.black, Color.red(0.8, 0.25)]];
		~genesSAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~genesSAutomation", action.value)});
			if(action.value == 1, {~flagGenesSAutomation='on'}, {~flagGenesSAutomation='off'})};
		// Genes Musique
		~genesMAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["GenesM On", Color.black, Color.green(0.8, 0.25)],["GenesM Off", Color.black, Color.red(0.8, 0.25)]];
		~genesMAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~genesMAutomation", action.value)});
			if(action.value == 1, {~flagGenesMAutomation='on'}, {~flagGenesMAutomation='off'})};
		// Synth agents
		~synthAgentsAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["SynthA On", Color.black, Color.green(0.8, 0.25)],["SynthA Off", Color.black, Color.red(0.8, 0.25)]];
		~synthAgentsAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthAgentsAutomation", action.value)});
			if(action.value == 1, {~flagSynthAgentsAutomation='on'}, {~flagSynthAgentsAutomation='off'})};
		// Synth musique
		~synthMusiqueAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["SynthM On", Color.black, Color.green(0.8, 0.25)],["SynthM Off", Color.black, Color.red(0.8, 0.25)]];
		~synthMusiqueAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~synthMusiqueAutomation", action.value)});
			if(action.value == 1, {~flagSynthMusiqueAutomation='on'}, {~flagSynthMusiqueAutomation='off'})};
		// Root Automation
		~rootAutomation = Button(~wad, Rect(0, 0, 75, 20)).states=[["Root On", Color.black, Color.green(0.8, 0.25)],["Root Off", Color.black, Color.red(0.8, 0.25)]];
		~rootAutomation.action = {arg action;
			if(~flagScoreRecordGUI == 'on', {~fonctionRecordScore.value("~rootAutomation", action.value)});
			if(action.value == 1, {~flagRootAutomation='on'}, {~flagRootAutomation='off'; /*~rootChoice.valueAction_(0)*/})};
		~wad.front;
	}

	keyboardPanel {

		////////////////////////// Window Keyboard ///////////////////////////////
		windowKeyboard = Window.new("Keyboard", Rect(10, 25, 625, 130), scroll: true);
		windowKeyboard.view.decorator = FlowLayout(windowKeyboard.view.bounds);
		windowKeyboard.front;
		// Setup ShortCut
		setupKeyboardShortCut = Button(windowKeyboard, Rect(0, 0, 105, 20));
		setupKeyboardShortCut.states = [["System Shortcut", Color.green], ["Keyboard Shortcut", Color.red]];
		setupKeyboardShortCut.action = {arg shortcut;
			if(shortcut.value == 1, {keyboardShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.setColor(note, Color.blue)})},
			{~fonctionShortCuts.value(windowKeyboard);
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
			~groupeAnalyse.set(\note, note, \amp, keyVolume, \trigger, 1);
		});
		// Action Up
		keyboard.keyUpAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			~groupeAnalyse.set(\note, note, \amp, 0, \trigger, 0);
		});
		// Action Track
		keyboard.keyTrackAction_({arg note;
			note = note.value + keyboardTranslate.value;
			note = note.midicps;
			s.bind{
				~groupeAnalyse.set(\note, note, \amp, keyVolume, \trigger, 1);
				s.sync;
				~groupeAnalyse.set(\note, note, \amp, 0, \trigger, 0);
				s.sync;
			};
		});
		windowKeyboard.front;
		setupKeyboardShortCut.focus;

		////////////////////////// Window VST ///////////////////////////////
		windowVST = Window.new("VST Stereo", Rect(700, 50, 320, 80), scroll: true);
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
			~synthVST = Synth.newPaused("VST Plugin", [\xFade, 0.5, \gainIn, 0.5], ~groupeMasterFX, \addToHead);
			~fxVST = VSTPluginController(~synthVST);
		};
		EZKnob(windowVST, 150 @ 25, "xFade", \unipolar,
			{|ez| ~groupeMasterFX.set(\xFade, ez.value)}, 0.5, layout: \horz);
		EZKnob(windowVST, 150 @ 25, "Gain In", \unipolar,
			{|ez| ~groupeMasterFX.set(\gainIn, ez.value)}, 0.5, layout: \horz);
		EZRanger(windowVST , 300 @ 20, "Pan", \bipolar,
			{|ez| ~groupeMasterFX.set(\panLo, ez.value.at(0), \panHi, ez.value.at(1))}, [0, 0], labelWidth: 40, numberWidth: 40);
		windowVST.view.children.at(0).focus;
		windowVST.front;

	}

	shortCuts {

		// Keyboard Fonction pour toutes les Views
		~fonctionShortCuts = {arg window;
			window.view.keyDownAction = {arg view,char,modifiers,unicode, keycode;
				if(view.asString != "a SCTextView", {
					// Write score (pas ctrl+u -> stop recording)
					if(unicode != 21 and: {keycode != 32}, {
						if(unicode != 0, {
							~fonctionRecordScore.value("~evaluationKeyDown", [char.asString,modifiers,unicode, keycode, 'on']);
						});
					});ˆ
					~flagScoreRecordGUI = 'off'; // Set off pour score
					~evaluationKeyDown.value(char,modifiers,unicode, keycode, 'off')});
			};
			~evaluationKeyDown={arg char,modifiers,unicode, keycode, flagScore='off';
				var datafile=[], file, flagFile='off', numberFile, compteurEssai=0, geneAgent, genomeCopy, numinstr;
				if(flagScore == 'on', {~commandeExecute=~commandeScore},{~commandeExecute=~commande});
				// [char,modifiers,unicode,keycode].postln;
				// Touches pave numerique
				if(modifiers==2097152 and: {unicode==49} and: {keycode==83},{numinstr=0;~keysfonction.value(~commandeExecute, 1, flagScore)});
				if(modifiers==2097152 and: {unicode==50} and: {keycode==84},{numinstr=1;~keysfonction.value(~commandeExecute, 2, flagScore)});
				if(modifiers==2097152 and: {unicode==51} and: {keycode==85},{numinstr=2;~keysfonction.value(~commandeExecute, 3, flagScore)});
				if(modifiers==2097152 and: {unicode==52} and: {keycode==86},{numinstr=3;~keysfonction.value(~commandeExecute, 4, flagScore)});
				if(modifiers==2097152 and: {unicode==53} and: {keycode==87},{numinstr=4;~keysfonction.value(~commandeExecute, 5, flagScore)});
				if(modifiers==2097152 and: {unicode==54} and: {keycode==88},{numinstr=5;~keysfonction.value(~commandeExecute, 6, flagScore)});
				if(modifiers==2097152 and: {unicode==55} and: {keycode==89},{numinstr=6;~keysfonction.value(~commandeExecute, 7, flagScore)});
				if(modifiers==2097152 and: {unicode==56} and: {keycode==91},{numinstr=7;~keysfonction.value(~commandeExecute, 8, flagScore)});
				if(modifiers==2097152 and: {unicode==57} and: {keycode==92},{numinstr=8;~keysfonction.value(~commandeExecute, 9, flagScore)});
				if(modifiers==2097152 and: {unicode==48} and: {keycode==82},{numinstr=9;~keysfonction.value(~commandeExecute, 10, flagScore)});
				if(modifiers==2228224 and: {unicode==49} and: {keycode==83},{~keysfonction.value(~commandeExecute, 11, flagScore)});
				if(modifiers==2228224 and: {unicode==50} and: {keycode==84},{~keysfonction.value(~commandeExecute, 12, flagScore)});
				if(modifiers==2228224 and: {unicode==51} and: {keycode==85},{~keysfonction.value(~commandeExecute, 13, flagScore)});
				if(modifiers==2228224 and: {unicode==52} and: {keycode==86},{~keysfonction.value(~commandeExecute, 14, flagScore)});
				if(modifiers==2228224 and: {unicode==53} and: {keycode==87},{~keysfonction.value(~commandeExecute, 15, flagScore)});
				if(modifiers==2228224 and: {unicode==54} and: {keycode==88},{~keysfonction.value(~commandeExecute, 16, flagScore)});
				if(modifiers==2228224 and: {unicode==55} and: {keycode==89},{~keysfonction.value(~commandeExecute, 17, flagScore)});
				if(modifiers==2228224 and: {unicode==56} and: {keycode==91},{~keysfonction.value(~commandeExecute, 18, flagScore)});
				if(modifiers==2228224 and: {unicode==57} and: {keycode==92},{~keysfonction.value(~commandeExecute, 19, flagScore)});
				if(modifiers==2228224 and: {unicode==48} and: {keycode==82},{~keysfonction.value(~commandeExecute, 20, flagScore)});
				if(modifiers==2621440 and: {unicode==49} and: {keycode==83},{~keysfonction.value(~commandeExecute, 21, flagScore)});
				if(modifiers==2621440 and: {unicode==50} and: {keycode==84},{~keysfonction.value(~commandeExecute, 22, flagScore)});
				if(modifiers==2621440 and: {unicode==51} and: {keycode==85},{~keysfonction.value(~commandeExecute, 23, flagScore)});
				if(modifiers==2621440 and: {unicode==52} and: {keycode==86},{~keysfonction.value(~commandeExecute, 24, flagScore)});
				if(modifiers==2621440 and: {unicode==53} and: {keycode==87},{~keysfonction.value(~commandeExecute, 25, flagScore)});
				if(modifiers==2621440 and: {unicode==54} and: {keycode==88},{~keysfonction.value(~commandeExecute, 26, flagScore)});
				if(modifiers==2621440 and: {unicode==55} and: {keycode==89},{~keysfonction.value(~commandeExecute, 27, flagScore)});
				if(modifiers==2621440 and: {unicode==56} and: {keycode==91},{~keysfonction.value(~commandeExecute, 28, flagScore)});
				if(modifiers==2621440 and: {unicode==57} and: {keycode==92},{~keysfonction.value(~commandeExecute, 29, flagScore)});
				if(modifiers==2621440 and: {unicode==48} and: {keycode==82},{~keysfonction.value(~commandeExecute, 30, flagScore)});
				if(modifiers==2752512 and: {unicode==49} and: {keycode==83},{~keysfonction.value(~commandeExecute, 31, flagScore)});
				if(modifiers==2752512 and: {unicode==50} and: {keycode==84},{~keysfonction.value(~commandeExecute, 32, flagScore)});
				if(modifiers==2752512 and: {unicode==51} and: {keycode==85},{~keysfonction.value(~commandeExecute, 33, flagScore)});
				if(modifiers==2752512 and: {unicode==52} and: {keycode==86},{~keysfonction.value(~commandeExecute, 34, flagScore)});
				if(modifiers==2752512 and: {unicode==53} and: {keycode==87},{~keysfonction.value(~commandeExecute, 35, flagScore)});
				if(modifiers==2752512 and: {unicode==54} and: {keycode==88},{~keysfonction.value(~commandeExecute, 36, flagScore)});
				if(modifiers==2752512 and: {unicode==55} and: {keycode==89},{~keysfonction.value(~commandeExecute, 37, flagScore)});
				if(modifiers==2752512 and: {unicode==56} and: {keycode==91},{~keysfonction.value(~commandeExecute, 38, flagScore)});
				if(modifiers==2752512 and: {unicode==57} and: {keycode==92},{~keysfonction.value(~commandeExecute, 39, flagScore)});
				if(modifiers==2752512 and: {unicode==48} and: {keycode==82},{~keysfonction.value(~commandeExecute, 40, flagScore)});
				// Touches clavier numerique
				if(modifiers==0 and: {unicode==49} and: {keycode==18},{numinstr=0;~keysfonction.value(~commandeExecute, 1, flagScore)});
				if(modifiers==0 and: {unicode==50} and: {keycode==19},{numinstr=1;~keysfonction.value(~commandeExecute, 2, flagScore)});
				if(modifiers==0 and: {unicode==51} and: {keycode==20},{numinstr=2;~keysfonction.value(~commandeExecute, 3, flagScore)});
				if(modifiers==0 and: {unicode==52} and: {keycode==21},{numinstr=3;~keysfonction.value(~commandeExecute, 4, flagScore)});
				if(modifiers==0 and: {unicode==53} and: {keycode==23},{numinstr=4;~keysfonction.value(~commandeExecute, 5, flagScore)});
				if(modifiers==0 and: {unicode==54} and: {keycode==22},{numinstr=5;~keysfonction.value(~commandeExecute, 6, flagScore)});
				if(modifiers==0 and: {unicode==55} and: {keycode==26},{numinstr=6;~keysfonction.value(~commandeExecute, 7, flagScore)});
				if(modifiers==0 and: {unicode==56} and: {keycode==28},{numinstr=7;~keysfonction.value(~commandeExecute, 8, flagScore)});
				if(modifiers==0 and: {unicode==57} and: {keycode==25},{numinstr=8;~keysfonction.value(~commandeExecute, 9, flagScore)});
				if(modifiers==0 and: {unicode==48} and: {keycode==29},{numinstr=9;~keysfonction.value(~commandeExecute, 10, flagScore)});
				if(modifiers==131072 and: {unicode==43} and: {keycode==18},{numinstr=0;~keysfonction.value(~commandeExecute, 11, flagScore)});
				if(modifiers==131072 and: {unicode==34} and: {keycode==19},{numinstr=1;~keysfonction.value(~commandeExecute, 12, flagScore)});
				if(modifiers==131072 and: {unicode==42} and: {keycode==20},{numinstr=2;~keysfonction.value(~commandeExecute, 13, flagScore)});
				if(modifiers==131072 and: {unicode==231} and: {keycode==21},{numinstr=3;~keysfonction.value(~commandeExecute, 14, flagScore)});
				if(modifiers==131072 and: {unicode==37} and: {keycode==23},{numinstr=4;~keysfonction.value(~commandeExecute, 15, flagScore)});
				if(modifiers==131072 and: {unicode==38} and: {keycode==22},{numinstr=5;~keysfonction.value(~commandeExecute, 16, flagScore)});
				if(modifiers==131072 and: {unicode==47} and: {keycode==26},{numinstr=6;~keysfonction.value(~commandeExecute, 17, flagScore)});
				if(modifiers==131072 and: {unicode==40} and: {keycode==28},{numinstr=7;~keysfonction.value(~commandeExecute, 18, flagScore)});
				if(modifiers==131072 and: {unicode==41} and: {keycode==25},{numinstr=8;~keysfonction.value(~commandeExecute, 19, flagScore)});
				if(modifiers==131072 and: {unicode==61} and: {keycode==29},{numinstr=9;~keysfonction.value(~commandeExecute, 20, flagScore)});
				if(modifiers==524288 and: {unicode==177} and: {keycode==18},{numinstr=0;~keysfonction.value(~commandeExecute, 21, flagScore)});
				if(modifiers==524288 and: {unicode==8220} and: {keycode==19},{numinstr=1;~keysfonction.value(~commandeExecute, 22, flagScore)});
				if(modifiers==524288 and: {unicode==35} and: {keycode==20},{numinstr=2;~keysfonction.value(~commandeExecute, 23, flagScore)});
				if(modifiers==524288 and: {unicode==199} and: {keycode==21},{numinstr=3;~keysfonction.value(~commandeExecute, 24, flagScore)});
				if(modifiers==524288 and: {unicode==91} and: {keycode==23},{numinstr=4;~keysfonction.value(~commandeExecute, 25, flagScore)});
				if(modifiers==524288 and: {unicode==93} and: {keycode==22},{numinstr=5;~keysfonction.value(~commandeExecute, 26, flagScore)});
				if(modifiers==524288 and: {unicode==124} and: {keycode==26},{numinstr=6;~keysfonction.value(~commandeExecute, 27, flagScore)});
				if(modifiers==524288 and: {unicode==123} and: {keycode==28},{numinstr=7;~keysfonction.value(~commandeExecute, 28, flagScore)});
				if(modifiers==524288 and: {unicode==125} and: {keycode==25},{numinstr=8;~keysfonction.value(~commandeExecute, 29, flagScore)});
				if(modifiers==524288 and: {unicode==8800} and: {keycode==29},{numinstr=9;~keysfonction.value(~commandeExecute, 30, flagScore)});
				if(modifiers==655360 and: {unicode==8734} and: {keycode==18},{numinstr=0;~keysfonction.value(~commandeExecute, 31, flagScore)});
				if(modifiers==655360 and: {unicode==8221} and: {keycode==19},{numinstr=1;~keysfonction.value(~commandeExecute, 32, flagScore)});
				if(modifiers==655360 and: {unicode==8249} and: {keycode==20},{numinstr=2;~keysfonction.value(~commandeExecute, 33, flagScore)});
				if(modifiers==655360 and: {unicode==8260} and: {keycode==21},{numinstr=3;~keysfonction.value(~commandeExecute, 34, flagScore)});
				if(modifiers==655360 and: {unicode==91} and: {keycode==23},{numinstr=4;~keysfonction.value(~commandeExecute, 35, flagScore)});
				if(modifiers==655360 and: {unicode==93} and: {keycode==22},{numinstr=5;~keysfonction.value(~commandeExecute, 36, flagScore)});
				if(modifiers==655360 and: {unicode==92} and: {keycode==26},{numinstr=6;~keysfonction.value(~commandeExecute, 37, flagScore)});
				if(modifiers==655360 and: {unicode==210} and: {keycode==28},{numinstr=7;~keysfonction.value(~commandeExecute, 38, flagScore)});
				if(modifiers==655360 and: {unicode==212} and: {keycode==25},{numinstr=8;~keysfonction.value(~commandeExecute, 39, flagScore)});
				if(modifiers==655360 and: {unicode==218} and: {keycode==29},{numinstr=9;~keysfonction.value(~commandeExecute, 40, flagScore)});
				// key w -> affichage windows ->
				if(modifiers==0 and: {unicode==119} and: {keycode==13}, {~indexwindow=~indexwindow+1;
					if(~indexwindow > (~listeWindows.size - 1), {~indexwindow=0});
					~listeWindows.wrapAt(~indexwindow).front});
				// Key ctrlw -> affichage windows <-
				if(modifiers==262144 and: {unicode==23} and: {keycode==13},{~indexwindow=~indexwindow-1;
					if(~indexwindow < 0, {~indexwindow=~listeWindows.size - 1});
					~listeWindows.wrapAt(~indexwindow).front});
				// key esc or SpaceBar-> All System on/off
				if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}},{if(~startsysteme.value == 1,{~startsysteme.valueAction_(0)},{~startsysteme.valueAction_(1)})});
				// key m-> musique on/off
				if(modifiers==0 and: {unicode==109} and: {keycode==46},{
					if(~musique.value == 1,{~musique.valueAction_(0)},{~musique.valueAction_(1)})});
				// key alt m-> tempo analyze on/off
				if(modifiers==524288 and: {unicode==109} and: {keycode==46},{
					if(~tempoButton.value == 1,{~tempoButton.valueAction_(0)},{~tempoButton.valueAction_(1)})});
				// key y-> affichage genomes... on/off
				if(modifiers==0 and: {unicode==121} and: {keycode==6},{
					if(~affichage.value == 1,{~affichage.valueAction_(0)},{~affichage.valueAction_(1)})});
				// key v-> display view virtual space on/off
				if(modifiers==0 and: {unicode==118} and: {keycode==9},{
					if(~viewworld.value == 1,{~viewworld.valueAction_(0)},{~viewworld.valueAction_(1)})});
				// key i -> init systeme
				if(modifiers==0 and: {unicode==105} and: {keycode==34},{~initsysteme.valueAction_(1)});
				// ctrl i -> init all systeme Monde
				if(modifiers==262144 and: {unicode==9} and: {keycode==34},{
					if(File.exists(~nompathdata++"init control panel.scd"),{file=File(~nompathdata++"init control panel.scd","r");
						~wp.name=~nomFenetre+"init control panel.scd";
						~loadMonde.value(file);file.close});
				});
				// alt i -> init all systeme Univers
				if(modifiers==524288 and: {unicode==105} and: {keycode==34},{
					if(File.exists(~nompathdata++"init preset.scd"),{file=File(~nompathdata++"init preset.scd","r");
						~wp.name=~nomFenetre+"init preset.scd";
						~loadUnivers.value(file, 'on', 'on');file.close;
						~tempoSlider.valueAction_(60);
					});
				});
				// key h-> switch source in pour analyse ->
				if(modifiers==0 and: {unicode==104} and: {keycode==4},{~keyCodeSourceIn = ~keyCodeSourceIn + 1;
					if(~keyCodeSourceIn > 3, {~keyCodeSourceIn=0});
					~entree.valueAction=~keyCodeSourceIn});
				// key ctrl h-> switch source in pour analyse <-
				if(modifiers==262144 and: {unicode==8} and: {keycode==4},{~keyCodeSourceIn = ~keyCodeSourceIn - 1;
					if(~keyCodeSourceIn < 0, {~keyCodeSourceIn=3});
					~entree.valueAction=~keyCodeSourceIn});
				// key L-> load Control Panel
				if(modifiers==131072 and: {unicode==76} and: {keycode==37},{~commandeExecute='load Control Panel'});
				// key S -> save Control Panel
				if(modifiers==131072 and: {unicode==83} and: {keycode==1},{~commandeExecute='save Control Panel'});
				// key l -> load Preset
				if(modifiers==0 and: {unicode==108} and: {keycode==37},
					{~commandeExecute='load Preset'});
				// key s -> save Preset
				if(modifiers==0 and: {unicode==115} and: {keycode==1},
					{~commandeExecute='save Preset'});
				// Key ctrl L  -> load Preset synchro on grid
				if(modifiers==393216 and: {unicode==12} and: {keycode==37},
					{~commandeExecute='load Preset synchro'});
				// key b -> switch loopmusic on/off
				if(modifiers==0 and: {unicode==98} and: {keycode==11}, {if(~loopseq.value == 1,{~loopseq.valueAction_(0)},{~loopseq.valueAction_(1)})});
				// key f -> load audio file pour fileIn analyse
				if(modifiers==0 and: {unicode==102} and: {keycode==3},
					{
						~commandeExecute='Switch File for Analyze';
				});
				// key F -> Boucle lecture file analyse on
				if(modifiers==131072 and: {unicode==70} and: {keycode==3}, {~synthPlayFile.set('loop', 1)});
				// key alt F -> Lecture une fois file analyse on
				if(modifiers==655360 and: {unicode==70} and: {keycode==3}, {~synthPlayFile.set('loop', 0)});
				// key ctrl f -> Load and Add File for Analyze
				if(modifiers==262144 and: {unicode==6} and: {keycode==3}, {Dialog.openPanel({ arg paths;
					~samplePourAnalyse=paths;
					s.bind{
						//~bufferanalysefile.free;
						s.sync;
						~synthPlayFile.set('trig', 0);
						s.sync;
						~synthPlayFile.run(false);
						s.sync;
						file = SoundFile.new;
						s.sync;
						file.openRead(~samplePourAnalyse);
						s.sync;
						if(file.numChannels == 1,
							{~rawData= FloatArray.newClear(file.numFrames * 2);
								s.sync;
								file.readData(~rawData);
								s.sync;
								Post << "Loading sound for analyze " << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~rawData = Array.newFrom(~rawData).stutter(2) / 2;
								s.sync;
								~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2, action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
							},
							{Post << "Loading sound for analyze " << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse,  channels: [0, 1], action: {arg buf; Post << "Finished" << Char.nl});
								s.sync;
						});
						//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
						file.close;
						s.sync;
						~sounds.size.do({arg i; ~recFiles.wrapAt(i).set('in', ~busFileIn.index)});
						~agents.do({arg i; ~synthRecFileAgents.wrapAt(i).set('in', ~busFileIn.index)});
						~textFileAnalyze.string_(PathName.new(paths).fileName);
						~listeSamplePourAnalyse = ~listeSamplePourAnalyse.add(~bufferanalysefile);
						~listeNameSamplePourAnalyse = ~listeNameSamplePourAnalyse.add(PathName.new(paths).fileName);
						if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File'}, {
							~synthPlayFile.run(true);
							~synthPlayFile.set(\trig, 0);
							s.sync;
							~audioFile.set(\trigger, 0);
							s.sync;
							~synthPlayFile.set(\bufferplay, ~listeSamplePourAnalyse.last);
							~audioFile.set(\bufferplay, ~listeSamplePourAnalyse.last);
							~synthPlayFile.set(\trig, 1);
							s.sync;
							~audioFile.set(\trigger, 1);
							s.sync;
						});
					};
				},{"cancelled".postln});
				});
				// key ctrl+s -> save buffer sons
				if(modifiers==262144 and: {unicode==19} and: {keycode==1},
					{// Commande Save Buffer
						Dialog.savePanel({arg path;
							~bufferSonsInstruments.write(path++".aiff","AIFF","int16")},{"cancelled".postln});
				});
				// key alt d -> Init duree play music synchro sur grille
				if(modifiers==524288 and: {unicode==100} and: {keycode==2},
					{if(~startsysteme.value == 1 and: {~musique.value == 1}, {
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {
							~agents.do({arg agent;
								~routineMusic.wrapAt(agent).stop; ~flagCompteurPlayingAgents.wrapPut(agent, 0);
								~routineMusic.wrapAt(agent).play(quant: Quant.new(1));
							});
							{if(~viewworld.value==1,{~routineVirtual.play(quant: Quant.new(1))});
								if(~affichage.value==1,{~routineData.play(quant: Quant.new(1))});
								if(~startAutomation.value==1,{~routineAutomation.play(quant: Quant.new(1))});
								~routineMetronome.play(quant: Quant.new(~nombreBeatsBare))}.defer;
							nil});
					});
				});
				// key d -> Init Sequences
				if(modifiers==0 and: {unicode==100} and: {keycode==2},
					{~agents.do({arg agent; ~flagCompteurPlayingAgents.wrapPut(agent, 0)});
				});
				// key altv -> Init transformation virtual world
				if(modifiers==524288 and: {unicode==118} and: {keycode==9},
					{~matrix=[1, 0, 0, 1, 0, -25];//coordonnees systeme
						~zoomXSlider.value=~matrix.wrapAt(0);
						~zoomYSlider.value=~matrix.wrapAt(3);
						~translateXSlider.value=~matrix.wrapAt(4);
						~translateYSlider.valueAction=~matrix.wrapAt(5).neg;
						~shearingXSlider.value=~matrix.wrapAt(1);
						~shearingYSlider.value=~matrix.wrapAt(2);
						~angleX=22 / 360 * 2pi;
						~angleY=45 / 360 * 2pi;
						~angleZ=16 / 360 * 2pi;
						~focalDistance=150;
						~angleXSlider.value=22;
						~angleYSlider.value=45;
						~angleZSlider.value=16;
						~focalDistanceSlider.value=150;
				});
				// key ctrl e -> Reset groupe des effets
				if(modifiers==262144 and: {unicode==5} and: {keycode==14}, {
					//~groupeSynthAgents.freeAll;
					// Effets
					~groupeEffets.freeAll;
					/*~busEffetsAudio.free;
					~busEffetsAudio=Bus.audio(s, 1);*/
					~listSynthEffets=[];
					~audioOutEffets=[];
					~playSynthEffets=[];
					~controlsSynthEffets=[];
					~panSynthEffets=[];
					~jitterPanSynthEffets=[];
					~jitterControlsSynthEffets=[];
					~ampSynthEffets=[];
					~automationPanEffets=[];
					~automationControlsEffets=[];
					~automationSpeedEffets = [];
					~listEffets.size.do({arg i;
						~listSynthEffets=~listSynthEffets.add(Synth.newPaused(~listEffets.wrapAt(i).asString,['in', ~busEffetsAudio.index, 'busverb', ~busVerbAudio.index, 'amp', 12.neg.dbamp, 'pan', 0.0, 'control1', 0.25,  'control2', 0.25,  'control3', 0.25,  'control4', 0.25,  'control5', 0.25,  'control6', 0.25,  'control7', 0.25,  'control8', 0.25], ~groupeEffets, \addToTail));
						~audioOutEffets=~audioOutEffets.add(0);
						~controlsSynthEffets=~controlsSynthEffets.add([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25]);
						~playSynthEffets=~playSynthEffets.add(0);
						~panSynthEffets=~panSynthEffets.add(0.0);
						~jitterPanSynthEffets=~jitterPanSynthEffets.add(0.1);
						~jitterControlsSynthEffets=~jitterControlsSynthEffets.add(0.1);
						~ampSynthEffets=~ampSynthEffets.add(-3);
						~automationPanEffets=~automationPanEffets.add(0);
						~automationControlsEffets=~automationControlsEffets.add(0);
						~automationSpeedEffets = ~automationSpeedEffets.add(24);
						~listeFXTime = ~listeFXTime.add(24.reciprocal);
					});
					~listSynthEffets.size.do({arg effet;
						~listSynthEffets.wrapAt(effet).set('out', ~audioOutEffets.wrapAt(effet) + ~startChannelAudioOut, 'amp', ~ampSynthEffets.wrapAt(effet).dbamp, 'pan', ~panSynthEffets.wrapAt(effet), 'control1', ~controlsSynthEffets.wrapAt(effet).wrapAt(0),  'control2', ~controlsSynthEffets.wrapAt(effet).wrapAt(1),  'control3', ~controlsSynthEffets.wrapAt(effet).wrapAt(2),  'control4', ~controlsSynthEffets.wrapAt(effet).wrapAt(3),  'control5', ~controlsSynthEffets.wrapAt(effet).wrapAt(4),  'control6', ~controlsSynthEffets.wrapAt(effet).wrapAt(5),  'control7', ~controlsSynthEffets.wrapAt(effet).wrapAt(6),  'control8', ~controlsSynthEffets.wrapAt(effet).wrapAt(7));
					});
					~sourceOutEffets.valueAction=0;
					~effetsInstrMenu.valueAction_(0);
					~playEffetsButton.valueAction_(0);
					~controlsEffetsMenu.valueAction_([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25]);
					~panEffets.valueAction_(0);
					~jitterPanEffets.valueAction_(0.1);
					~ampEffets.valueAction_(-3);
					~randomPanEffets.value_(0);
					~randomControlsEffets.value_(0);
					~speedEffets.valueAction_(24);
				});
				// key E -> Reset groupe des effets + Stop all effets
				if(modifiers==131072 and: {unicode==69} and: {keycode==14}, {
					nil;
				});
				// key alt e -> Kill groupe des Verb
				if(modifiers==524288 and: unicode==101 and: {keycode== 14}, {
					// Verb
					~groupeVerb.freeAll;
					~listSynthVerb=[];
					~audioOutVerb=[];
					~playSynthVerb=[];
					~controlsSynthVerb=[];
					~panSynthVerb=[];
					~jitterPanSynthVerb=[];
					~jitterControlsSynthVerb=[];
					~ampSynthVerb=[];
					~automationPanVerb=[];
					~automationControlsVerb=[];
					~automationSpeedVerb = [];
					~listeVerbTime = [];
					~listVerb.size.do({arg i;
						~listSynthVerb=~listSynthVerb.add(Synth.newPaused(~listVerb.wrapAt(i).asString,['in', ~busVerbAudio.index, 'amp', 12.neg.dbamp, 'pan', 0.0, 'control1', 0.25,  'control2', 0.25,  'control3', 0.25,  'control4', 0.25,  'control5', 0.25,  'control6', 0.25,  'control7', 0.25,  'control8', 0.25], ~groupeVerb, \addToTail));
						~audioOutVerb=~audioOutVerb.add(0);
						~controlsSynthVerb=~controlsSynthVerb.add([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25]);
						~playSynthVerb=~playSynthVerb.add(0);
						~panSynthVerb=~panSynthVerb.add(0.0);
						~jitterPanSynthVerb=~jitterPanSynthVerb.add(0.1);
						~jitterControlsSynthVerb=~jitterControlsSynthVerb.add(0.1);
						~ampSynthVerb=~ampSynthVerb.add(-3);
						~automationPanVerb=~automationPanVerb.add(0);
						~automationControlsVerb=~automationControlsVerb.add(0);
						~automationSpeedVerb=~automationSpeedVerb.add(24);
						~listeVerbTime = ~listeVerbTime.add(24.reciprocal);
					});
					~listSynthVerb.size.do({arg verb;
						~listSynthVerb.wrapAt(verb).set('out', ~audioOutVerb.wrapAt(verb) + ~startChannelAudioOut, 'amp', ~ampSynthVerb.wrapAt(verb).dbamp, 'pan', ~panSynthVerb.wrapAt(verb), 'control1', ~controlsSynthVerb.wrapAt(verb).wrapAt(0),  'control2', ~controlsSynthVerb.wrapAt(verb).wrapAt(1),  'control3', ~controlsSynthVerb.wrapAt(verb).wrapAt(2),  'control4', ~controlsSynthVerb.wrapAt(verb).wrapAt(3),  'control5', ~controlsSynthVerb.wrapAt(verb).wrapAt(4),  'control6', ~controlsSynthVerb.wrapAt(verb).wrapAt(5),  'control7', ~controlsSynthVerb.wrapAt(verb).wrapAt(6),  'control8', ~controlsSynthVerb.wrapAt(verb).wrapAt(7));
					});
					~sourceOutVerb.valueAction=0;
					~verbInstrMenu.valueAction_(0);
					~playVerbButton.valueAction_(0);
					~controlsVerbMenu.valueAction_([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25]);
					~panVerb.valueAction_(0);
					~jitterPanVerb.valueAction_(0.1);
					~ampVerb.valueAction_(-3);
					~randomPanVerb.value_(0);
					~randomControlsVerb.value_(0);
					~speedVerb.valueAction_(24);
				});
				// key e -> Mode elitiste on/off
				if(modifiers==0 and: {unicode==101} and: {keycode==14}, {
					if(~elitisteButton.value == 1,{~elitisteButton.valueAction_(0)},{~elitisteButton.valueAction_(1)})});
				// key g -> Switch genes
				if(modifiers==0 and: {unicode==103} and: {keycode==5},
					{~commandeExecute='switch genes'});
				// key alt + p -> Switch SynthDef
				if(modifiers==524288 and: {unicode==112} and: {keycode==35},
					{~commandeExecute='switch synthDef'});
				// key alt + u -> Switch Samples
				if(modifiers==524288 and: {unicode==117} and: {keycode==32},
					{~commandeExecute='switch samples'});
				// key o> Automation on/off
				if(modifiers==0 and: {unicode==111} and: {keycode==31}, {
					if(~startAutomation.value == 1,{~startAutomation.valueAction_(0)},{~startAutomation.valueAction_(1)})});
				// key t-> Comande Automation
				if(modifiers==0 and: {unicode==116} and: {keycode==17}, {~commandeExecute='switch automation'});
				// key j -> Tore on/off
				if(modifiers==0 and: {unicode==106} and: {keycode==38}, {
					if(~toreButton.value == 1,{~toreButton.valueAction_(0)},{~toreButton.valueAction_(1)})});
				// key J -> Flock on/off
				if(modifiers==131072 and: {unicode==74} and: {keycode==38}, {
					if(~flockButton.value == 1,{~flockButton.valueAction_(0)},{~flockButton.valueAction_(1)})});
				// key alt + J -> SharedMusic on/off
				if(modifiers==655360 and: {unicode==74} and: {keycode==38}, {
					if(~sharedMusicButton.value == 1,{~sharedMusicButton.valueAction_(0)},{~sharedMusicButton.valueAction_(1)})});
				// key ctrl n-> Add a single agent
				if(modifiers==262144 and: {unicode==14} and: {keycode==45}, {
					if(~agents < ~maximumagents, {
						~initagents.value(~agents, nil, nil, nil, 'init', [], [], [], 0, 0, 0);
						~agents=~agents + 1;// nouveau-ne additionne
				})});
				// key N -> Add a copy of an agent
				if(modifiers==131072 and: {unicode==78} and: {keycode==45}, {~commandeExecute='Add copy agent'});
				//key alt + k
				if(modifiers==524288 and: {unicode==107} and: {keycode==40}, {~userOperatingSystem.valueAction_(14)});
				//key K
				if(modifiers==131072 and: {unicode==75} and: {keycode==40}, {~userOperatingSystem.valueAction_(15)});
				//key k
				if(modifiers==0 and: {unicode==107} and: {keycode==40}, {~userOperatingSystem.valueAction_(17)});
				//key ctrl + k
				if(modifiers==262144 and: {unicode==11} and: {keycode==40},{~userOperatingSystem.valueAction_(16)});
				// Key z -> load Preset aleatoire
				if(modifiers==0 and: {unicode==122} and: {keycode==16}, {
					while({flagFile=='off' and: {compteurEssai <= (~choixChangeConfig.wrapAt(1)-~choixChangeConfig.wrapAt(0)+1)}}, {
						if(~foldersToScanPreset == [], {
							// Collect all Preset
							~foldersToScanAll = PathName.new(~nompathdata).files.collect{ |path| var file;
								file = path.fileName;
								if(file.find("preset") == 0 or: {file.find("Preset") == 0}, {file});
							};
							~foldersToScanAll = ~foldersToScanAll.reject({arg item; item == nil});
							// Collect preset
							~foldersToScanPreset = ~foldersToScanAll.collect{ |file|
								if(file.find("preset ") == 0 or: {file.find("Preset ") == 0}, {file});
							};
						});
						numberFile=rrand(0, ~foldersToScanPreset.size - 1);compteurEssai=compteurEssai+1;
						if(File.exists(~nompathdata++~foldersToScanPreset.wrapAt(numberFile)), {file=File(~nompathdata++~foldersToScanPreset.wrapAt(numberFile),"r");
							~wp.name=~nomFenetre + ~foldersToScanPreset.wrapAt(numberFile);
							~loadUnivers.value(file, 'on', 'on');file.close;flagFile='on';
							~foldersToScanPreset.removeAt(numberFile);
					})});
				});
				// key q -> Switch Algo Analyse
				if(modifiers==0 and: {unicode==113} and: {keycode==12}, {
					if(~algoAnalyse.value == 0, {~algoAnalyse.valueAction_(1)}, {
						if(~algoAnalyse.value == 1, {~algoAnalyse.valueAction_(2)}, {
							if(~algoAnalyse.value == 2, {~algoAnalyse.valueAction_(3)}, {
								if(~algoAnalyse.value == 3, {~algoAnalyse.valueAction_(4)}, {
									if(~algoAnalyse.value == 4, {~algoAnalyse.valueAction_(0)});
								});
							});
						});
					});
				});
				// Key alt + r -> Start Recording
				if(modifiers==524288 and: {unicode==114} and: {keycode==15}, {
					if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg( '/HPrec', "Rec On")});// Send Synchro Rec On
					~fonctionRecOn.value;
				});
				// Key shift + alt + r -> Pause Recording
				if(modifiers==655360 and: {unicode==82} and: {keycode==15}, {
					if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec Pause
					~fonctionRecPause.value;
				});
				// Key ctrl + alt + r -> Stop Recording
				if(modifiers==786432 and: {unicode==18} and: {keycode==15}, {
					if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec Off
					~fonctionRecOff.value;
				});
				// Key ctrl + t -> Save preset+genome+sequence avec date et time
				if(modifiers==262144 and: {unicode==20} and: {keycode==17}, {~wp.name=~nomFenetre+"preset+genome+sequence"+Date.localtime.stamp.asString++".scd";
					file=File(~nompathdata++"preset+genome+sequence"+Date.localtime.stamp.asString++".scd","w");file.write(~foncSaveUnivers.value(~foncSaveMonde.value, 'on', 'on').value.asCompileString);file.close;
				});
				// key p -> play score loop off
				if(modifiers==0 and: {unicode==112} and: {keycode==35},
					{~commandeExecute='play score loop off'});
				// key P -> play score loop on
				if(modifiers==131072 and: {unicode==80} and: {keycode==35},
					{~commandeExecute='play score loop on'});
				// key ctrl + p -> stop score
				if(modifiers==262144 and: {unicode==16} and: {keycode==35},
					{~commandeExecute='stop score'});
				// key ctrl + z -> stop all score
				if(modifiers==262144 and: {unicode==26} and: {keycode==16}, {
					"Stop all score".postln;
					40.do({arg i;
						~listeRoutinePlayingScore.wrapAt(i).size.do({arg ii;
							~listeRoutinePlayingScore.wrapAt(i).wrapAt(ii).stop;
							~listeRoutinePlayingScore.wrapAt(i).wrapAt(ii).remove;
						});
						~listeRoutinePlayingScore.wrapPut(i, []);
						~listeFlagRoutinePlayingScore.wrapPut(i, []);
					});
					~score=nil; ~numberStepScore=nil;
				});
				// key u -> start record score
				if(modifiers==0 and: {unicode==117} and: {keycode==32},
					{~commandeExecute='start record score'});
				// key ctrl + u -> stop record score
				if(modifiers==262144 and: {unicode==21} and: {keycode==32},
					{if(~flagRecordScore == 'on', {
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {~fonctionRecordScore.value("End Score", [nil]);// end score
							// write file score
							file=File(~nompathdata++"score"+~numeroScore.asString++".scd","w");
							file.write(~scoreRecording.asCompileString);file.close;~flagRecordScore='off';
							("Stop recording score"+~numeroScore.asString).postln;nil});
					});
				});
				// key ctrl + alt + p score step by step loop off
				if(modifiers==786432 and: {unicode==16} and: {keycode==35},
					{~commandeExecute='play score step loop off'});
				// key ctrl + alt + shift + p score step by step loop on
				if(modifiers==917504 and: {unicode==16} and: {keycode==35},
					{~commandeExecute='play score step loop on'});
				// key enter score step next event
				if(modifiers==0 and: {unicode==13} and: {keycode==36},
					{~scoreStep.value(~score, ~indexStepScore, ~flagStepScore, ~numberStepScore)});
				// Genome Editor
				if(~flagHPgenomeEditor == 'on',
					{// key alt + x next ~agentEditor
						if(modifiers==524288 and: {unicode==120} and: {keycode==7}, {~agentNumber.valueAction_(~agentEditor + 1)});
						//key X previous ~agentEditor
						if(modifiers==131072 and: {unicode==88} and: {keycode==7}, {~agentNumber.valueAction_(~agentEditor - 1)});
						// key alt + c next ~agentEditor for copy
						if(modifiers==524288 and: {unicode==99} and: {keycode==8}, {~agentCopy.valueAction_(~agentCopy.value + 1)});
						//key C previous ~agentEditor for copy
						if(modifiers==131072 and: {unicode==67} and: {keycode==8}, {~agentCopy.valueAction_(~agentCopy.value - 1)});
						//key alt + y copy genome
						if(modifiers==524288 and: {unicode==121} and: {keycode==6}, {
							if(~agentEditor > 0 and: {~agentEditor <= ~agents},
								{~genomes.wrapPut(~agentCopy.value - 1, ~genomes.wrapAt(~agentEditor-1).copy)});
						});
						//key G  init genome (solo)
						if(modifiers==131072 and: {unicode==71} and: {keycode==5}, {
							~genomeInit = [];
							47.do({arg gene; ~genomeInit=~genomeInit.add(~createOrmutationGenesAgents.value(gene))});
							~envEdit = ~genomeInit.copyRange(26, 33).sort;
							forBy(26, 33, 1 , {arg i; ~genomeInit.wrapPut(i, ~envEdit.wrapAt(i-26))});
							if(~agentEditor > 0 and: {~agentEditor <= ~agents},
								{~genomes.wrapPut(~agentEditor - 1, ~genomeInit)});
						});
				});
				// Sequence Editor
				if(~flagHPsequenceEditor == 'on',
					{// key alt + x next ~agentSequence
						if(modifiers==524288 and: {unicode==120} and: {keycode==7}, {~agentNumberSeq.valueAction_(~agentSequence + 1)});
						//key X previous ~agentSequence
						if(modifiers==131072 and: {unicode==88} and: {keycode==7},{~agentNumberSeq.valueAction_(~agentSequence - 1)});
						// key alt + c next ~agentSequence for copy
						if(modifiers==524288 and: {unicode==99} and: {keycode==8}, {~agentCopySequence.valueAction_(~agentCopySequence.value + 1)});
						//key C previous ~agentSequence for copy
						if(modifiers==131072 and: {unicode==67} and: {keycode==8}, {~agentCopySequence.valueAction_(~agentCopySequence.value - 1)});
						//key shift + y copy sequence fhz amp time
						if(modifiers==131072 and: {unicode==89} and: {keycode==6}, {
							if(~agentSequence > 0 and: {~agentSequence <= ~agents },
								{~listeagentfreq.wrapPut(~agentCopySequence.value - 1, ~listeagentfreq.wrapAt(~agentSequence-1).copy);
									~listeagentamp.wrapPut(~agentCopySequence.value - 1, ~listeagentamp.wrapAt(~agentSequence-1).copy);
									~listeagentduree.wrapPut(~agentCopySequence.value - 1, ~listeagentduree.wrapAt(~agentSequence-1).copy)});
						});
				});
				if(flagScore == 'on', {~commandeScore=~commandeExecute},{~commande=~commandeExecute});
				~flagScoreRecordGUI = 'on';// Set on pour score
			};
		};
		//~~fonctionShortCuts.value;

		// Evaluation des commandes racourcis clavier
		~keysfonction={arg commande, number, flagScore;
			var datafile=[], file;
			//load Control
			if(commande=='load Control Panel',{
				if(File.exists(~nompathdata++"control panel"+number.value.asString++".scd"),{file=File(~nompathdata++"control panel"+number.value.asString++".scd","r");
					~wp.name=~nomFenetre+"control panel"+~algoMusic+number.value.asString++".scd";
					~loadMonde.value(file);file.close});
			});
			//load Preset
			if(commande=='load Preset',{
				if(File.exists(~nompathdata++"preset"+number.value.asString++".scd"),{file=File(~nompathdata++"preset"+number.value.asString++".scd","r");
					~loadUnivers.value(file, 'on', 'on');
					~wp.name=~nomFenetre+~algoMusic+"preset"+number.value.asString++".scd";
					file.close});
			});
			//load Preset synchro
			if(commande=='load Preset synchro',{
				if(File.exists(~nompathdata++"preset"+number.value.asString++".scd"),{file=File(~nompathdata++"preset"+number.value.asString++".scd","r");
					~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar - 0.1, {{~flagScoreRecordGUI = 'off';
						~loadUnivers.value(file, 'on', 'on');
						~wp.name=~nomFenetre+~algoMusic+"preset"+number.value.asString++".scd";
						file.close; ~flagScoreRecordGUI = 'on'}.defer;nil})});
			});
			//save
			if(commande=='save Control Panel',{
				~wp.name=~nomFenetre+"control panel"+~algoMusic+number.value.asString++".scd";
				file=File(~nompathdata++"control panel"+number.value.asString++".scd","w");file.write(~foncSaveMonde.value.asCompileString);file.close;
			});
			//save Preset
			if(commande=='save Preset',{
				~wp.name=~nomFenetre+"preset"+number.value.asString++".scd";
				file=File(~nompathdata++"preset"+number.value.asString++".scd","w");file.write(~foncSaveUnivers.value(~foncSaveMonde.value, 'on', 'on').value.asCompileString);file.close;
			});
			// Switch Genes
			if(commande=='switch genes', {
				if(number == 1, {
					if(~flagGeneFreq=='on', {~geneFreqButton.valueAction_(0)},{~geneFreqButton.valueAction_(1)});
				});
				if(number == 2, {
					if(~flagGeneTransFreq=='on', {~geneTransFreqButton.valueAction_(0)},{~geneTransFreqButton.valueAction_(1)});
				});
				if(number == 3, {
					if(~flagGeneAmp=='on', {~geneAmpButton.valueAction_(0)},{~geneAmpButton.valueAction_(1)});
				});
				if(number == 4, {
					if(~flagGeneDuree=='on', {~geneDureeButton.valueAction_(0)},{~geneDureeButton.valueAction_(1)});
				});
				if(number == 5, {
					if(~flagGeneMulDuree=='on', {~geneMulDureeButton.valueAction_(0)},{~geneMulDureeButton.valueAction_(1)});
				});
				if(number == 6, {
					if(~flagGenePan=='on', {~genePanButton.valueAction_(0)},{~genePanButton.valueAction_(1)});
				});
				if(number == 7, {
					if(~flagGeneBuffer=='on', {~geneBufferButton.valueAction_(0)},{~geneBufferButton.valueAction_(1)});
				});
				if(number == 8, {
					if(~flagGeneSample=='on', {~geneSampleButton.valueAction_(0)},{~geneSampleButton.valueAction_(1)});
				});
				if(number == 9, {
					if(~flagGeneReverse=='on', {~geneReverseButton.valueAction_(0)},{~geneReverseButton.valueAction_(1)});
				});
				if(number == 10, {
					if(~flagGeneLoop=='on', {~geneLoopButton.valueAction_(0)},{~geneLoopButton.valueAction_(1)});
				});
				if(number == 11, {
					if(~flagGeneOffset=='on', {~geneOffsetButton.valueAction_(0)},{~geneOffsetButton.valueAction_(1)});
				});
				if(number == 12, {
					if(~flagGeneEnvLevel=='on', {~geneEnvLevelButton.valueAction_(0)},{~geneEnvLevelButton.valueAction_(1)});
				});
				if(number == 13, {
					if(~flagGeneEnvDuree=='on', {~geneEnvDureeButton.valueAction_(0)},{~geneEnvDureeButton.valueAction_(1)});
				});
				if(number == 14, {
					if(~flagGeneSynth=='on', {~geneSynthButton.valueAction_(0)},{~geneSynthButton.valueAction_(1)});
				});
				if(number == 15, {
					if(~flagGeneControl=='on', {~geneControlsButton.valueAction_(0)},{~geneControlsButton.valueAction_(1)});
				});
				if(number == 16, {
					if(~flagGeneOut=='on', {~geneAudioOutButton.valueAction_(0)},{~geneAudioOutButton.valueAction_(1)});
				});
				if(number == 17, {
					if(~flagGeneMidi=='on', {~geneMidiOutButton.valueAction_(0)},{~geneMidiOutButton.valueAction_(1)});
				});
				if(number == 18, {
					if(~flagGeneInput=='on', {~geneInputButton.valueAction_(0)},{~geneInputButton.valueAction_(1)});
				});
				if(number == 19, {
					if(~flagGeneLoopMusic=='on', {~geneLoopMusicButton.valueAction_(0)},{~geneLoopMusicButton.valueAction_(1)});
				});
				if(number == 20, {
					if(~flagGeneBufferMusic=='on', {~geneBufferMusicButton.valueAction_(0)},{~geneBufferMusicButton.valueAction_(1)});
				});
				if(number == 21, {
					if(~flagGeneChordMax=='on', {~geneChordMaxButton.valueAction_(0)},{~geneChordMaxButton.valueAction_(1)});
				});
				if(number == 22, {
					if(~flagGeneChordDur=='on', {~geneChordDurButton.valueAction_(0)},{~geneChordDurButton.valueAction_(1)});
				});
				if(number == 23, {
					if(~flagGeneChordDur=='on', {~geneChordDurButton.valueAction_(0)},{~geneChordDurButton.valueAction_(1)});
				});
				if(number == 24, {
					if(~flagGeneChordDur=='on', {~geneChordDurButton.valueAction_(0)},{~geneChordDurButton.valueAction_(1)});
				});
			});
			// Switch SynthDef
			if(commande=='switch synthDef', {
				~synthDefInstrMenu.valueAction_(number-1);
			});
			// Switch Samples
			if(commande=='switch samples', {
				~soundsInstrMenu.valueAction_(number-1);
			});
			// Switch Automation
			if(commande=='switch automation', {
				if(number == 1, {
					if(~flagInitAutomation=='on', {~initAutomation.valueAction_(0)},{~initAutomation.valueAction_(1)});
				});
				if(number == 2, {
					if(~flagUniversAutomation=='on', {~universAutomation.valueAction_(0)},{~universAutomation.valueAction_(1)});
				});
				if(number == 3, {
					if(~flagMondesAgentsAutomation=='on', {~mondesAgentsAutomation.valueAction_(0)},{~mondesAgentsAutomation.valueAction_(1)});
				});
				if(number == 4, {
					if(~flagMondesMusiqueAutomation=='on', {~mondesMusiqueAutomation.valueAction_(0)},{~mondesMusiqueAutomation.valueAction_(1)});
				});
				if(number == 5, {
					if(~flagGenesSAutomation=='on', {~genesSAutomation.valueAction_(0)},{~genesSAutomation.valueAction_(1)});
				});
				if(number == 6, {
					if(~flagGenesMAutomation=='on', {~genesMAutomation.valueAction_(0)},{~genesMAutomation.valueAction_(1)});
				});
				if(number == 7, {
					if(~flagSynthAgentsAutomation=='on', {~synthAgentsAutomation.valueAction_(0)},{~synthAgentsAutomation.valueAction_(1)});
				});
				if(number == 8, {
					if(~flagSynthMusiqueAutomation=='on', {~synthMusiqueAutomation.valueAction_(0)},{~synthMusiqueAutomation.valueAction_(1)});
				});
				if(number == 9, {
					if(~flagRootAutomation=='on', {~flagRootAutomation.valueAction_(0)},{~flagRootAutomation.valueAction_(1)});
				});
			});
			// Start record score
			if(commande == 'start record score' and:{~flagRecordScore != 'on'}, {
				if(~startsysteme.value == 1, {
					~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {~scoreRecording=[];
						~numeroScore=number;
						~flagRecordScore='on';
						~lastTimeRecordScore=~tempoMusicPlay.beats;
						("Start recording score"+number.asString).postln;
						nil})},
				{~scoreRecording=[];
					~numeroScore=number;
					~flagRecordScore='on';
					~lastTimeRecordScore=~tempoMusicPlay.beats;
					("Start recording score"+number.asString).postln});
			});
			// Play score loop off
			if(commande == 'play score loop off' and:{~flagRecordScore != 'on'}, {
				if(File.exists(~nompathdata++"score"+number.asString++".scd"),{file=File(~nompathdata++"score"+number.asString++".scd","r");
					datafile=file.readAllString.interpret;file.close;
					//ici fonction init et playing score
					if(~startsysteme.value == 1, {
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {file=~listeFlagRoutinePlayingScore.wrapAt(number-1); file=file.add('on');
							~listeFlagRoutinePlayingScore.wrapPut(number-1, file);
							file=~listeRoutinePlayingScore.wrapAt(number-1); file=file.add(~routinePlayingScore.value(datafile, number, ~listeFlagRoutinePlayingScore.wrapAt(number-1).size, 'off'));
							~listeRoutinePlayingScore.wrapPut(number-1, file);
							~listeRoutinePlayingScore.wrapAt(number-1).last.play;
							nil})},
					{file=~listeFlagRoutinePlayingScore.wrapAt(number-1); file=file.add('on');
						~listeFlagRoutinePlayingScore.wrapPut(number-1, file);
						file=~listeRoutinePlayingScore.wrapAt(number-1); file=file.add(~routinePlayingScore.value(datafile, number, ~listeFlagRoutinePlayingScore.wrapAt(number-1).size, 'off'));
						~listeRoutinePlayingScore.wrapPut(number-1, file)});
				});
			});
			// Play score loop on
			if(commande == 'play score loop on' and:{~flagRecordScore != 'on'}, {
				if(File.exists(~nompathdata++"score"+number.asString++".scd"),{file=File(~nompathdata++"score"+number.asString++".scd","r");
					datafile=file.readAllString.interpret;file.close;
					//ici fonction init et playing score
					if(~startsysteme.value == 1, {
						~tempoMusicPlay.schedAbs(~tempoMusicPlay.beats, {file=~listeFlagRoutinePlayingScore.wrapAt(number-1); file=file.add('on');
							~listeFlagRoutinePlayingScore.wrapPut(number-1, file);
							file=~listeRoutinePlayingScore.wrapAt(number-1); file=file.add(~routinePlayingScore.value(datafile, number, ~listeFlagRoutinePlayingScore.wrapAt(number-1).size, 'on'));
							~listeRoutinePlayingScore.wrapPut(number-1, file);
							~listeRoutinePlayingScore.wrapAt(number-1).last.play;
							nil})},
					{file=~listeFlagRoutinePlayingScore.wrapAt(number-1); file=file.add('on');
						~listeFlagRoutinePlayingScore.wrapPut(number-1, file);
						file=~listeRoutinePlayingScore.wrapAt(number-1); file=file.add(~routinePlayingScore.value(datafile, number, ~listeFlagRoutinePlayingScore.wrapAt(number-1).size, 'on'));
						~listeRoutinePlayingScore.wrapPut(number-1, file)});
				});
			});
			// Play score step loop off
			if(commande == 'play score step loop off' and:{~flagRecordScore != 'on'}, {
				if(File.exists(~nompathdata++"score"+number.asString++".scd"),{file=File(~nompathdata++"score"+number.asString++".scd","r");
					datafile=file.readAllString.interpret;file.close;
					("Play Step Score"+number.asString++"."+"(Loop"+~flagStepScore.asString++")").postln;
					//ici fonction init et playing score
					~indexStepScore=0;~flagStepScore='off';~score=datafile;~numberStepScore=number;
				});
			});
			// Play score step loop on
			if(commande == 'play score step loop on' and:{~flagRecordScore != 'on'}, {
				if(File.exists(~nompathdata++"score"+number.asString++".scd"),{file=File(~nompathdata++"score"+number.asString++".scd","r");
					datafile=file.readAllString.interpret;file.close;
					("Play Step Score"+number.asString++"."+"(Loop"+~flagStepScore.asString++")").postln;
					~indexStepScore=0;~flagStepScore='on';~score=datafile;~numberStepScore=number;
				});
			});
			// Stop score
			if(commande == 'stop score' and:{~flagRecordScore != 'on'}, {
				~listeRoutinePlayingScore.wrapAt(number-1).size.do({arg i;
					~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(i).stop;
					~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(i).remove;
					("Stop score"+number.asString++"."++(i+1).asString).postln;
				});
				~listeRoutinePlayingScore.wrapPut(number-1, []);
				~listeFlagRoutinePlayingScore.wrapPut(number-1, []);
				~score=nil; ~numberStepScore=nil;
			});
			// Add a copy of this agent
			if(commande == 'Add copy agent', {
				if(~agents < ~maximumagents, {~initagents.value(~agents, nil, nil, nil, 'init', [], [], [], 0, 0, 0);// nouveau-ne additionne
					~agents=~agents + 1});
				// Copy genome
				~genomes.wrapPut(~agents.size-1, ~genomes.wrapAt(number-1).copy);
				// Copy sequences
				~listeagentfreq.wrapPut(~agents.size-1, ~listeagentfreq.wrapAt(number-1).copy);
				~listeagentamp.wrapPut(~agents.size-1, ~listeagentamp.wrapAt(number-1).copy);
				~listeagentduree.wrapPut(~agents.size-1, ~listeagentduree.wrapAt(number-1).copy);
			});
			// Switch File for Analyze
			if(commande == 'Switch File for Analyze', {
				if(~listeSamplePourAnalyse.wrapAt(number - 1) != nil, {
					s.bind{
						~synthPlayFile.set(\trig, 0);
						s.sync;
						~audioFile.set(\trigger, 0);
						s.sync;
						~synthPlayFile.set(\bufferplay, ~listeSamplePourAnalyse.wrapAt(number - 1));
						~audioFile.set(\bufferplay, ~listeSamplePourAnalyse.wrapAt(number - 1));
						~synthPlayFile.set(\trig, 1);
						s.sync;
						~audioFile.set(\trigger, 1);
						s.sync;
					};
					~textFileAnalyze.string_(~listeNameSamplePourAnalyse.wrapAt(number - 1));
				}, {"cancelled".postln});
			});
			~commande='nil';~commandeExecute='nil';
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
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (60 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 60 + keyboardTranslate.value;
					keyboard.setColor(60 + keyboardTranslate.value, Color.red);
				});
				if(char == $s, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (61 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 61 + keyboardTranslate.value;
					keyboard.setColor(61 + keyboardTranslate.value, Color.red);
				});
				if(char == $x, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (62 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 62 + keyboardTranslate.value;
					keyboard.setColor(62 + keyboardTranslate.value, Color.red);
				});
				if(char == $d, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (63 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 63 + keyboardTranslate.value;
					keyboard.setColor(63 + keyboardTranslate.value, Color.red);
				});
				if(char == $c, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (64 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 64 + keyboardTranslate.value;
					keyboard.setColor(64 + keyboardTranslate.value, Color.red);
				});
				if(char == $v, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (65 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 65 + keyboardTranslate.value;
					keyboard.setColor(65 + keyboardTranslate.value, Color.red);
				});
				if(char == $g, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (66 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 66 + keyboardTranslate.value;
					keyboard.setColor(66 + keyboardTranslate.value, Color.red);
				});
				if(char == $b, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (67 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 67 + keyboardTranslate.value;
					keyboard.setColor(67 + keyboardTranslate.value, Color.red);
				});
				if(char == $h, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (68 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 68 + keyboardTranslate.value;
					keyboard.setColor(68 + keyboardTranslate.value, Color.red);
				});
				if(char == $n, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (69 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 69 + keyboardTranslate.value;
					keyboard.setColor(69 + keyboardTranslate.value, Color.red);
				});
				if(char == $j, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (70 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 70 + keyboardTranslate.value;
					keyboard.setColor(70 + keyboardTranslate.value, Color.red);
				});
				if(char == $m, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (71 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 71 + keyboardTranslate.value;
					keyboard.setColor(71 + keyboardTranslate.value, Color.red);
				});
				if(char == $,, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (72 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 72 + keyboardTranslate.value;
					keyboard.setColor(72 + keyboardTranslate.value, Color.red);
				});
				if(char == $l, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (73 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 73 + keyboardTranslate.value;
					keyboard.setColor(73 + keyboardTranslate.value, Color.red);
				});
				if(char == $., {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (74 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
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
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (75 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 75 + keyboardTranslate.value;
					keyboard.setColor(60 + keyboardTranslate.value, Color.red);
				});
				if(char == $-, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					s.bind{
						~groupeAnalyse.set(\note, lastNote.midicps, \amp, 0, \trigger, 0);
						s.sync;
						~groupeAnalyse.set(\note, (76 + keyboardTranslate.value).midicps, \amp, keyVolume, \trigger, 1);
						s.sync;
					};
					lastNote = 76 + keyboardTranslate.value;
					keyboard.setColor(76 + keyboardTranslate.value, Color.red);
				});
			};
		};

		// Save Monde
		~foncSaveMonde={arg datafile=[];
			datafile=datafile.add([~tempoButton.value, ~tempoagents]);//0
			datafile=datafile.add(~algoAnalyse.value);//1
			datafile=datafile.add(~tempoMusic);//2
			datafile=datafile.add(~differencefreq);//3
			datafile=datafile.add(~differenceamp);//4
			datafile=datafile.add(~differenceduree);//5
			datafile=datafile.add(~dureeaccord);//6
			datafile=datafile.add(~maxaccord);//7
			datafile=datafile.add(~listedatasizein);//8
			datafile=datafile.add(~dureeanalysemax);//9
			datafile=datafile.add(~tempsmaxsignal);//10
			datafile=datafile.add(~dureeVieAgents);//11
			datafile=datafile.add(~vitessegeneration);//12
			datafile=datafile.add(~trancheage);//13
			datafile=datafile.add(~dat);//14
			datafile=datafile.add(~dst);//15
			datafile=datafile.add(~mutation);//16
			datafile=datafile.add(~croisement);//17
			datafile=datafile.add(~learning);//18
			datafile=datafile.add(~naissance);//19
			datafile=datafile.add(~listedatamusiqueagents);//20
			datafile=datafile.add(~startpopulation);//21
			datafile=datafile.add(~maximumagents);//22
			datafile=datafile.add(~maximumenfants);//23
			datafile=datafile.add(~vitesseagents);//24
			datafile=datafile.add(~dvt);//25
			datafile=datafile.add(~canauxMidiInOut);//26
			datafile=datafile.add(~choiceFilter.value);//27
			datafile=datafile.add(~hzFilter.value);//28
			datafile=datafile.add(~seuilInSlider.value);//29
			datafile=datafile.add(~filtreInSlider.value);//30
			datafile=datafile.add(~flagloop);//31
			datafile=datafile.add(~flagTore);//32
			datafile=datafile.add(~flagFlock);//33
			datafile=datafile.add(~flagSharedMusic);//34
			datafile=datafile.add(~flagElitiste);//35
			datafile=datafile.add(~paraAlgoAnalyseAudio);//36
			datafile=datafile.add(~nombreBeatsBare);//37
			datafile=datafile.add(~volumeFileIn.value);//38
			datafile=datafile.add(~offsetFileIn.value);//39
			datafile=datafile.add("<- WorldGenome / UniversGenome ->");//40
			datafile.value;
		};

		// Save Preset
		~foncSaveUnivers={arg datafile, flagGenome='on', flagSequence='on';//herite de foncSave monde
			datafile=datafile.add(~matrix);//41
			datafile=datafile.add(~angleX);//42
			datafile=datafile.add(~angleY);//43
			datafile=datafile.add(~angleZ);//44
			datafile=datafile.add(~focalDistance);//45
			datafile=datafile.add(~flagGeneFreq);//46
			datafile=datafile.add(~flagGeneTransFreq);//47
			datafile=datafile.add(~flagGeneAmp);//48
			datafile=datafile.add(~flagGeneDuree);//49
			datafile=datafile.add(~flagGeneMulDuree);//50
			datafile=datafile.add(~flagGenePan);//51
			datafile=datafile.add(~flagGeneBuffer);//52
			datafile=datafile.add(~flagGeneSample);//53
			datafile=datafile.add(~flagGeneReverse);//54
			datafile=datafile.add(~flagGeneLoop);//55
			datafile=datafile.add(~flagGeneOffset);//56
			datafile=datafile.add(~flagGeneEnvLevel);//57
			datafile=datafile.add(~flagGeneEnvDuree);//58
			datafile=datafile.add(~flagGeneSynth);//59
			datafile=datafile.add(~flagGeneControl);//60
			datafile=datafile.add(~flagGeneOut);//61
			datafile=datafile.add(~flagGeneMidi);//62
			datafile=datafile.add(~flagGeneInput);//63
			datafile=datafile.add(~flagGeneLoopMusic);//64
			datafile=datafile.add(~flagGeneBufferMusic);//65
			datafile=datafile.add(~flagGeneChordMax);//66
			datafile=datafile.add(~flagGeneChordDur);//67
			datafile=datafile.add(~audioOutSynth);//68
			datafile=datafile.add(~synthInstruments);//69
			datafile=datafile.add(~recSamplesButtonSons);//70
			datafile=datafile.add(~recSamplesLoopSons);//71
			datafile=datafile.add(~recSamplesLevelsSons);//72
			datafile=datafile.add(~flagFreqSamplesSons);//73
			datafile=datafile.add(~loopSynthSons);//74
			datafile=datafile.add(~reverseSynthSons);//75
			datafile=datafile.add(~audioInputSons);//76
			datafile=datafile.add(~envelopeSynthMenu.value);//77
			datafile=datafile.add(~recSamplesLevelsMenu.value);//78
			datafile=datafile.add(~synthDefInstrMenu.value);//79
			datafile=datafile.add(~soundsInstrMenu.value);//80
			datafile=datafile.add(~recSamplesButton.value);//81
			datafile=datafile.add(~recSamplesLoopButton.value);//82
			datafile=datafile.add(~posSamplesSons);//83
			datafile=datafile.add(~loopSynthButton.value);//84
			datafile=datafile.add(~reverseSynthButton.value);//85
			datafile=datafile.add(~audioInputButton.value);//86
			datafile=datafile.add(~panInstr.value);//87
			datafile=datafile.add(~freqInstr.value);//88
			datafile=datafile.add(~freqTransposeInstr.value);//89
			datafile=datafile.add(~ampInstr.value);//90
			datafile=datafile.add(~dureeInstr.value);//91
			datafile=datafile.add(~dureeTempoinstr.value);//92
			datafile=datafile.add(~audioOutEffets);//93
			datafile=datafile.add(~playSynthEffets);//94
			datafile=datafile.add(~controlsSynthEffets);//95
			datafile=datafile.add(~panSynthEffets);//96
			datafile=datafile.add(~ampSynthEffets);//97
			datafile=datafile.add(~sourceOutEffets.value);//98
			datafile=datafile.add(~effetsInstrMenu.value);//99
			datafile=datafile.add(~playEffetsButton.value);//100
			datafile=datafile.add(~controlsEffetsMenu.value);//101
			datafile=datafile.add(~panEffets.value);//102
			datafile=datafile.add(~ampEffets.value);//103
			datafile=datafile.add(~startAutomation.value);//104
			datafile=datafile.add(~flagInitAutomation);//105
			datafile=datafile.add(~flagUniversAutomation);//106
			datafile=datafile.add(~flagMondesAgentsAutomation);//107
			datafile=datafile.add(~flagMondesMusiqueAutomation);//108
			datafile=datafile.add(~flagGenesMAutomation);//109
			datafile=datafile.add(~flagGenesSAutomation);//110
			datafile=datafile.add(~flagSynthMusiqueAutomation);//111
			datafile=datafile.add(~flagSynthAgentsAutomation);//112
			datafile=datafile.add(~densite.value);//113
			datafile=datafile.add(~controlsSynth);//114
			datafile=datafile.add(~antiClick);//115
			datafile=datafile.add(~flagGeneControl);//116
			datafile=datafile.add(~quantaMusic);//117
			datafile=datafile.add(~automationPanEffets);//118
			datafile=datafile.add(~automationControlsEffets);//119
			datafile=datafile.add(~automationControlsSynth);//120
			datafile=datafile.add(~geneFreqRanger.value);//121
			datafile=datafile.add(~geneTransFreqRanger.value);//122
			datafile=datafile.add(~geneAmpRanger.value);//123
			datafile=datafile.add(~geneDureeRanger.value);//124
			datafile=datafile.add(~geneMulDureeRanger.value);//125
			datafile=datafile.add(~genePanRanger.value);//126
			datafile=datafile.add(~geneBufferRanger.value);//127
			datafile=datafile.add([~geneSampleRangerLow.value, ~geneSampleRangerHigh.value]);//128
			datafile=datafile.add(~geneReverseRanger.value);//129
			datafile=datafile.add(~geneLoopRanger.value);//130
			datafile=datafile.add(~geneOffsetRanger.value);//131
			datafile=datafile.add(~geneEnvLevelRanger.value);//132
			datafile=datafile.add(~geneEnvDureeRanger.value);//133
			datafile=datafile.add([~geneSynthRangerLow.value, ~geneSynthRangerHigh.value]);//134
			datafile=datafile.add(~geneControlsRanger.value);//135
			datafile=datafile.add(~geneAudioOutRanger.value);//136
			datafile=datafile.add(~geneMidiOutRanger.value);//137
			datafile=datafile.add([~geneInputRangerLow.value, ~geneInputRangerHigh.value]);//138
			datafile=datafile.add(~geneLoopMusicRanger.value);//139
			datafile=datafile.add(~geneBufferMusicRanger.value);//140
			datafile=datafile.add(~geneChordMaxRanger.value);//141
			datafile=datafile.add(~geneChordDurRanger.value);//142
			datafile=datafile.add(~geneVieillissement.value);//143
			datafile=datafile.add(~geneDeplacement.value);//144
			datafile=datafile.add(~geneVision.value);//145
			datafile=datafile.add(~geneAudition.value);//146
			datafile=datafile.add(~ampSynth.value);//147
			datafile=datafile.add(~jitterPanEffets.value);//148
			datafile=datafile.add(~jitterPanSynthEffets);//149
			datafile=datafile.add(~jitterControlsSynthEffets);//150
			datafile=datafile.add(~automationJitterControlsSynth);//151
			// Tuning Array
			datafile=datafile.add([~listTuning.value, ~rootChoice.value, ~displayDegrees.value, ~flagScaling.value]);//152
			//Algorithm
			datafile=datafile.add(~flagGeneAlgorithm.value);//153
			datafile=datafile.add(~geneAlgorithm.value);//154
			datafile=datafile.add(~algoMusic.value);//155
			//Band
			datafile=datafile.add(~numberBand.value);//156
			datafile=datafile.add(~buttonSynthBand.value);//157
			datafile=datafile.add(~flagSynthBand.value);//158
			datafile=datafile.add(~rangeSynthBand.value);//159
			datafile=datafile.add(~flagBandSynth.value);//160
			datafile=datafile.add(~synthBand0.value);//161
			datafile=datafile.add(~synthBand1.value);//162
			datafile=datafile.add(~synthBand2.value);//163
			datafile=datafile.add(~synthBand3.value);//164
			datafile=datafile.add(~synthBand4.value);//165
			datafile=datafile.add(~synthBand5.value);//166
			datafile=datafile.add(~synthBand6.value);//167
			datafile=datafile.add(~synthBand7.value);//168
			datafile=datafile.add(~synthBand8.value);//169
			datafile=datafile.add(~synthBand9.value);//170
			datafile=datafile.add(~synthBand10.value);//171
			datafile=datafile.add(~synthBand11.value);//172
			datafile=datafile.add(~synthBand12.value);//173
			// Gene
			datafile=datafile.add(~buttonGeneBand.value);//174
			datafile=datafile.add(~flagBandGenes.value);//175
			datafile=datafile.add(~geneBand0.value);//176
			datafile=datafile.add(~geneBand1.value);//177
			datafile=datafile.add(~geneBand2.value);//178
			datafile=datafile.add(~geneBand3.value);//179
			datafile=datafile.add(~geneBand4.value);//180
			datafile=datafile.add(~geneBand5.value);//181
			datafile=datafile.add(~geneBand6.value);//182
			datafile=datafile.add(~geneBand7.value);//183
			datafile=datafile.add(~geneBand8.value);//184
			datafile=datafile.add(~geneBand9.value);//185
			datafile=datafile.add(~geneBand10.value);//186
			datafile=datafile.add(~geneBand11.value);//187
			datafile=datafile.add(~geneBand12.value);//188
			// Verb
			datafile=datafile.add(~audioOutVerb);//189
			datafile=datafile.add(~playSynthVerb);//190
			datafile=datafile.add(~controlsSynthVerb);//191
			datafile=datafile.add(~panSynthVerb);//192
			datafile=datafile.add(~ampSynthVerb);//193
			datafile=datafile.add(~sourceOutVerb.value);//194
			datafile=datafile.add(~verbInstrMenu.value);//195
			datafile=datafile.add(~playVerbButton.value);//196
			datafile=datafile.add(~controlsVerbMenu.value);//197
			datafile=datafile.add(~panVerb.value);//198
			datafile=datafile.add(~ampVerb.value);//199
			datafile=datafile.add(~automationPanVerb);//200
			datafile=datafile.add(~automationControlsVerb);//201
			datafile=datafile.add(~jitterPanVerb.value);//202
			datafile=datafile.add(~jitterPanSynthVerb);//203
			datafile=datafile.add(~jitterControlsSynthVerb);//204
			datafile=datafile.add(~speedEffets.value);//205
			datafile=datafile.add(~automationSpeedEffets);//206
			datafile=datafile.add(~speedVerb.value);//207
			datafile=datafile.add(~automationSpeedVerb);//208
			datafile=datafile.add(~flagRootAutomation);//209
			// Range Band
			datafile=datafile.add(rangeBand.value);//210
			// + Genome
			if(flagGenome == 'on', {datafile=datafile.add(~genomes)});//211
			// + Sequence
			if(flagSequence == 'on', {datafile=datafile.add([~listeagentfreq,~listeagentamp,~listeagentduree])});//212
			datafile.value;
		};

		//Load Monde
		~loadMonde={arg file;
			var datafile=nil;
			datafile=file.readAllString.interpret;file.close;
			//~groupeSynthAgents.freeAll;
			~tempoAgentsSlider.valueAction=~tempoagents=datafile.wrapAt(0).wrapAt(1);
			~tempoButton.valueAction=datafile.wrapAt(0).wrapAt(0);
			~tempoSlider.valueAction=~tempoMusic=datafile.wrapAt(2);
			~audioFreqSlider.value=~differencefreq=datafile.wrapAt(3);
			~audioAmpSlider.value=~differenceamp=datafile.wrapAt(4);
			~audiodureeSlider.value=~differenceduree=datafile.wrapAt(5);
			~dureeAccordsSlider.value=~dureeaccord=datafile.wrapAt(6);
			~notesAccordsSlider.value=~maxaccord=datafile.wrapAt(7);
			~dataSignauxSlider.value=~listedatasizein=datafile.wrapAt(8);
			~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
			~agents.do({arg agent; ~listeagentID.wrapPut(agent, [])});
			~compteurAnalyse=0;
			~dureeMaximumSlider.value=~dureeanalysemax=datafile.wrapAt(9);
			~dureeSilenceSlider.value=~tempsmaxsignal=datafile.wrapAt(10);
			~vieAgentsSlider.value=~dureeVieAgents=datafile.wrapAt(11);
			~generationAgentsSlider.value=~vitessegeneration=datafile.wrapAt(12);
			~agesAgentsSlider.value=~trancheage=datafile.wrapAt(13);
			~jeunesse=~dureeVieAgents - (~trancheage.wrapAt(0) * ~dureeVieAgents);
			~vieilliesse=~dureeVieAgents - (~trancheage.wrapAt(1) * ~dureeVieAgents);
			~distanceAgentsSlider.value=~distanceagents=datafile.wrapAt(14);~dat=datafile.wrapAt(14);
			~distanceSignauxSlider.value=~distancesignaux=datafile.wrapAt(15);~dst=datafile.wrapAt(15);
			~mutationAgentsSlider.value=~mutation=datafile.wrapAt(16);
			~croisementAgentsSlider.value=~croisement=datafile.wrapAt(17);
			~learningAgentsSlider.value=~learning=datafile.wrapAt(18);
			if(~learning == 0, {~sharedMusicButton.enabled_(false)},{~sharedMusicButton.enabled_(true)});
			~naissanceAgentsSlider.value=~naissance=datafile.wrapAt(19);
			~dataAgentsSlider.value=~listedatamusiqueagents=datafile.wrapAt(20);
			~populationInitialeSlider.value=~startpopulation=datafile.wrapAt(21);
			~agentsMaximumSlider.value=~maximumagents=datafile.wrapAt(22);
			~enfantsAgentsSlider.value=~maximumenfants=datafile.wrapAt(23);
			~vitesseAgentsSlider.value=~vitesseagents=datafile.wrapAt(24);
			~devianceSlider.value=~deviance=datafile.wrapAt(25);~dvt=datafile.wrapAt(25);
			~canalMidiInSlider.valueAction=~canalMidiIn=datafile.wrapAt(26).wrapAt(0);
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
			~canalMidiOutSlider.valueAction=~canalMidiOut=datafile.wrapAt(26).wrapAt(1) + 1;
			~canauxMidiInOut=datafile.wrapAt(26);
			~choiceFilter.valueAction=datafile.wrapAt(27);
			~hzFilter.valueAction=datafile.wrapAt(28);
			~seuilInSlider.value=datafile.wrapAt(29);// Seuil
			~filtreInSlider.value=datafile.wrapAt(30);// Filtre
			~flagloop=datafile.wrapAt(31);if(~flagloop=='on',{~loopseq.valueAction_(1)},{~loopseq.valueAction_(0)});
			~flagTore=datafile.wrapAt(32);if(~flagTore=='on',{~toreButton.valueAction_(1)},{~toreButton.valueAction_(0)});
			~flagFlock=datafile.wrapAt(33);if(~flagFlock=='on',{~flockButton.valueAction_(1)},{~flockButton.valueAction_(0)});
			~flagSharedMusic=datafile.wrapAt(34);if(~flagSharedMusic=='on',{~sharedMusicButton.valueAction_(1)},{~sharedMusicButton.valueAction_(0)});
			~flagElitiste=datafile.wrapAt(35);if(~flagElitiste=='on',{~elitisteButton.valueAction_(1)},{~elitisteButton.valueAction_(0)});
			~paraAlgoAnalyseAudio=datafile.wrapAt(36);
			~nombreBeats.valueAction_(datafile.wrapAt(37));
			//~volumeFileIn.valueAction_(datafile.wrapAt(38));
			//~offsetFileIn.valueAction_(datafile.wrapAt(39));
			// Data 40 -> info
			//~initsysteme.valueAction_(1);
			~algoAnalyse.value=datafile.wrapAt(1);
		};

		// Load Preset
		~loadUnivers={arg file, flagGenome='on', flagSequence='on';
			var datafile=nil, sequence, loVal, hiVal, allVal;
			datafile=file.readAllString.interpret;file.close;
			~tempoAgentsSlider.valueAction=~tempoagents=datafile.wrapAt(0).wrapAt(1);
			~tempoButton.valueAction=datafile.wrapAt(0).wrapAt(0);
			~tempoSlider.valueAction=~tempoMusic=datafile.wrapAt(2);
			~audioFreqSlider.value=~differencefreq=datafile.wrapAt(3);
			~audioAmpSlider.value=~differenceamp=datafile.wrapAt(4);
			~audiodureeSlider.value=~differenceduree=datafile.wrapAt(5);
			~dureeAccordsSlider.value=~dureeaccord=datafile.wrapAt(6);
			~notesAccordsSlider.value=~maxaccord=datafile.wrapAt(7);
			~dataSignauxSlider.value=~listedatasizein=datafile.wrapAt(8);
			~listefreq=[];~listeamp=[];~listeduree=[];~listeID=[];
			~agents.do({arg agent; ~listeagentID.wrapPut(agent, [])});
			~compteurAnalyse=0;
			~dureeMaximumSlider.value=~dureeanalysemax=datafile.wrapAt(9);
			~dureeSilenceSlider.valueAction_(~tempsmaxsignal=datafile.wrapAt(10));
			~vieAgentsSlider.value=~dureeVieAgents=datafile.wrapAt(11);
			~generationAgentsSlider.value=~vitessegeneration=datafile.wrapAt(12);
			~agesAgentsSlider.value=~trancheage=datafile.wrapAt(13);
			~jeunesse=~dureeVieAgents - (~trancheage.wrapAt(0) * ~dureeVieAgents);
			~vieilliesse=~dureeVieAgents - (~trancheage.wrapAt(1) * ~dureeVieAgents);
			~distanceAgentsSlider.value=~distanceagents=datafile.wrapAt(14);~dat=datafile.wrapAt(14);
			~distanceSignauxSlider.value=~distancesignaux=datafile.wrapAt(15);~dst=datafile.wrapAt(15);
			~mutationAgentsSlider.value=~mutation=datafile.wrapAt(16);
			~croisementAgentsSlider.value=~croisement=datafile.wrapAt(17);
			~learningAgentsSlider.value=~learning=datafile.wrapAt(18);
			if(~learning == 0, {~sharedMusicButton.enabled_(false)},{~sharedMusicButton.enabled_(true)});
			~naissanceAgentsSlider.value=~naissance=datafile.wrapAt(19);
			~dataAgentsSlider.value=~listedatamusiqueagents=datafile.wrapAt(20);
			~populationInitialeSlider.value=~startpopulation=datafile.wrapAt(21);
			~agentsMaximumSlider.value=~maximumagents=datafile.wrapAt(22);
			~enfantsAgentsSlider.value=~maximumenfants=datafile.wrapAt(23);
			~vitesseAgentsSlider.value=~vitesseagents=datafile.wrapAt(24);
			~devianceSlider.value=~deviance=datafile.wrapAt(25);~dvt=datafile.wrapAt(25);
			~canalMidiInSlider.valueAction=~canalMidiIn=datafile.wrapAt(26).wrapAt(0);
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
			~canalMidiOutSlider.valueAction=~canalMidiOut=datafile.wrapAt(26).wrapAt(1) + 1;
			~canauxMidiInOut=datafile.wrapAt(26);
			~choiceFilter.valueAction=datafile.wrapAt(27);
			~hzFilter.valueAction=datafile.wrapAt(28);
			~seuilInSlider.value=datafile.wrapAt(29);// Seuil
			~filtreInSlider.value=datafile.wrapAt(30);// Filtre
			~flagloop=datafile.wrapAt(31);if(~flagloop=='on',{~loopseq.valueAction_(1)},{~loopseq.valueAction_(0)});
			~flagTore=datafile.wrapAt(32);if(~flagTore=='on',{~toreButton.valueAction_(1)},{~toreButton.valueAction_(0)});
			~flagFlock=datafile.wrapAt(33);if(~flagFlock=='on',{~flockButton.valueAction_(1)},{~flockButton.valueAction_(0)});
			~flagSharedMusic=datafile.wrapAt(34);if(~flagSharedMusic=='on',{~sharedMusicButton.valueAction_(1)},{~sharedMusicButton.valueAction_(0)});
			~flagElitiste=datafile.wrapAt(35);if(~flagElitiste=='on',{~elitisteButton.valueAction_(1)},{~elitisteButton.valueAction_(0)});
			~paraAlgoAnalyseAudio=datafile.wrapAt(36);
			~nombreBeats.valueAction_(datafile.wrapAt(37));
			//~volumeFileIn.valueAction_(datafile.wrapAt(38));
			//~offsetFileIn.valueAction_(datafile.wrapAt(39));
			// startpopup
			// Data 40 -> info
			~algoAnalyse.valueAction=datafile.wrapAt(1);
			~flagGeneFreq=datafile.wrapAt(46);
			~flagGeneTransFreq=datafile.wrapAt(47);
			~flagGeneAmp=datafile.wrapAt(48);
			~flagGeneDuree=datafile.wrapAt(49);
			~flagGeneMulDuree=datafile.wrapAt(50);
			~flagGenePan=datafile.wrapAt(51);
			~flagGeneBuffer=datafile.wrapAt(52);
			~flagGeneSample=datafile.wrapAt(53);
			~flagGeneReverse=datafile.wrapAt(54);
			~flagGeneLoop=datafile.wrapAt(55);
			~flagGeneOffset=datafile.wrapAt(56);
			~flagGeneEnvLevel=datafile.wrapAt(57);
			~flagGeneEnvDuree=datafile.wrapAt(58);
			~flagGeneSynth=datafile.wrapAt(59);
			~flagGeneControl=datafile.wrapAt(60);
			~flagGeneOut=datafile.wrapAt(61);
			~flagGeneMidi=datafile.wrapAt(62);
			~flagGeneInput=datafile.wrapAt(63);
			~flagGeneLoopMusic=datafile.wrapAt(64);
			~flagGeneBufferMusic=datafile.wrapAt(65);
			~flagGeneChordMax=datafile.wrapAt(66);
			~flagGeneChordDur=datafile.wrapAt(67);
			if(~flagGeneFreq=='on', {~geneFreqButton.valueAction=1},{~geneFreqButton.valueAction=0});
			if(~flagGeneTransFreq=='on', {~geneTransFreqButton.valueAction=1},{~geneTransFreqButton.valueAction=0});
			if(~flagGeneAmp=='on', {~geneAmpButton.valueAction=1},{~geneAmpButton.valueAction=0});
			if(~flagGeneDuree=='on', {~geneDureeButton.valueAction=1},{~geneDureeButton.valueAction=0});
			if(~flagGeneMulDuree=='on', {~geneMulDureeButton.valueAction=1},{~geneMulDureeButton.valueAction=0});
			if(~flagGenePan=='on', {~genePanButton.valueAction=1},{~genePanButton.valueAction=0});
			if(~flagGeneBuffer=='on', {~geneBufferButton.valueAction=1},{~geneBufferButton.valueAction=0});
			if(~flagGeneSample=='on', {~geneSampleButton.valueAction=1},{~geneSampleButton.valueAction=0});
			if(~flagGeneReverse=='on', {~geneReverseButton.valueAction=1},{~geneReverseButton.valueAction=0});
			if(~flagGeneLoop=='on', {~geneLoopButton.valueAction=1},{~geneLoopButton.valueAction=0});
			if(~flagGeneOffset=='on', {~geneOffsetButton.valueAction=1},{~geneOffsetButton.valueAction=0});
			if(~flagGeneEnvLevel=='on', {~geneEnvLevelButton.valueAction=1},{~geneEnvLevelButton.valueAction=0});
			if(~flagGeneEnvDuree=='on', {~geneEnvDureeButton.valueAction=1},{~geneEnvDureeButton.valueAction=0});
			if(~flagGeneSynth=='on', {~geneSynthButton.valueAction=1},{~geneSynthButton.valueAction=0});
			if(~flagGeneControl=='on', {~geneControlsButton.valueAction=1},{~geneControlsButton.valueAction=0});
			if(~flagGeneOut=='on', {~geneAudioOutButton.valueAction=1},{~geneAudioOutButton.valueAction=0});
			if(~flagGeneMidi=='on', {~geneMidiOutButton.valueAction=1},{~geneMidiOutButton.valueAction=0});
			if(~flagGeneInput=='on', {~geneInputButton.valueAction=1},{~geneInputButton.valueAction=0});
			if(~flagGeneLoopMusic=='on', {~geneLoopMusicButton.valueAction=1},{~geneLoopMusicButton.valueAction=0});
			if(~flagGeneBufferMusic=='on', {~geneBufferMusicButton.valueAction=1},{~geneBufferMusicButton.valueAction=0});
			if(~flagGeneChordMax=='on', {~geneChordMaxButton.valueAction=1},{~geneChordMaxButton.valueAction=0});
			if(~flagGeneChordDur=='on', {~geneChordDurButton.valueAction=1},{~geneChordDurButton.valueAction=0});
			~automationControlsSynth=datafile.wrapAt(120);
			~automationJitterControlsSynth=datafile.wrapAt(151);
			~audioOutSynth=datafile.wrapAt(68);
			~synthInstruments=datafile.wrapAt(69);
			~recSamplesButtonSons=datafile.wrapAt(70);
			~recSamplesLoopSons=datafile.wrapAt(71);
			~recSamplesLevelsSons=datafile.wrapAt(72);
			~flagFreqSamplesSons=datafile.wrapAt(73);
			~loopSynthSons=datafile.wrapAt(74);
			~reverseSynthSons=datafile.wrapAt(75);
			~audioInputSons=datafile.wrapAt(76);
			~timeenvelope.size.do({arg i; ~timeenvelope.wrapPut(i, abs(datafile.wrapAt(77).wrapAt(0).wrapAt(i+1) - datafile.wrapAt(77).wrapAt(0).wrapAt(i)))});
			~levelenvelope=datafile.wrapAt(77).wrapAt(1);
			~envelopeSynthMenu.valueAction=datafile.wrapAt(77);
			~recSamplesLevelsMenu.valueAction=datafile.wrapAt(78);
			~synthDefInstrMenu.valueAction = datafile.wrapAt(79);
			~randomControlsSynth.value_(~automationControlsSynth.wrapAt(datafile.wrapAt(79)));
			~jitterControlsSynth.value_(~automationJitterControlsSynth.wrapAt(datafile.wrapAt(79)));
			~sourceOutSlider.valueAction = ~audioOutSynth.wrapAt(datafile.wrapAt(79));
			~soundsInstrMenu.valueAction=datafile.wrapAt(80);
			~recSamplesButton.valueAction=datafile.wrapAt(81);
			~recSamplesLoopButton.valueAction=datafile.wrapAt(82);
			~posSamplesSons=datafile.wrapAt(83);
			~posSamplesInstr.valueAction_(~posSamplesSons.wrapAt(datafile.wrapAt(80)));
			~loopSynthButton.valueAction=datafile.wrapAt(84);
			~reverseSynthButton.valueAction=datafile.wrapAt(85);
			~audioInputButton.valueAction=datafile.wrapAt(86);
			~sounds.size.do({arg son;
				~listesamplein.wrapAt(son).set(\run, ~recSamplesButtonSons.wrapAt(son), \trigger, ~recSamplesButtonSons.wrapAt(son),\loop, ~recSamplesLoopSons.wrapAt(son), \reclevel1, ~recSamplesLevelsSons.wrapAt(son).wrapAt(0), \reclevel2, ~recSamplesLevelsSons.wrapAt(son).wrapAt(1));
				~recSamples.wrapAt(son).setn(\in, ~audioInLR.wrapAt(~audioInputSons.wrapAt(son)) - 1)});
			~panInstr.valueAction_(datafile.wrapAt(87));
			~freqInstr.valueAction_(datafile.wrapAt(88));
			~freqTransposeInstr.valueAction=datafile.wrapAt(89);
			~ampInstr.valueAction_(datafile.wrapAt(90));
			~dureeInstr.valueAction_(datafile.wrapAt(91));
			~dureeTempoinstr.value=datafile.wrapAt(92);
			~audioOutEffets=datafile.wrapAt(93);
			~playSynthEffets=datafile.wrapAt(94);
			~controlsSynthEffets=datafile.wrapAt(95);
			~panSynthEffets=datafile.wrapAt(96);
			~jitterPanSynthEffets=datafile.wrapAt(149);
			~jitterControlsSynthEffets=datafile.wrapAt(150);
			~ampSynthEffets=datafile.wrapAt(97);
			~automationPanEffets=datafile.wrapAt(118);
			~automationControlsEffets=datafile.wrapAt(119);
			~automationSpeedEffets=datafile.wrapAt(206);
			~listSynthEffets.size.do({arg effet;
				~listSynthEffets.wrapAt(effet).set('out', ~audioOutEffets.wrapAt(effet), 'amp', ~ampSynthEffets.wrapAt(effet).dbamp, 'pan', ~panSynthEffets.wrapAt(effet), 'control1', ~controlsSynthEffets.wrapAt(effet).wrapAt(0),  'control2', ~controlsSynthEffets.wrapAt(effet).wrapAt(1),  'control3', ~controlsSynthEffets.wrapAt(effet).wrapAt(2),  'control4', ~controlsSynthEffets.wrapAt(effet).wrapAt(3),  'control5', ~controlsSynthEffets.wrapAt(effet).wrapAt(4),  'control6', ~controlsSynthEffets.wrapAt(effet).wrapAt(5),  'control7', ~controlsSynthEffets.wrapAt(effet).wrapAt(6),  'control8', ~controlsSynthEffets.wrapAt(effet).wrapAt(7));
				if(~playSynthEffets.wrapAt(effet) == 1, {~listSynthEffets.wrapAt(effet).run(true)},{~listSynthEffets.wrapAt(effet).run(false)});
			});
			~sourceOutEffets.valueAction=datafile.wrapAt(98);
			~effetsInstrMenu.valueAction_(datafile.wrapAt(99));
			~playEffetsButton.valueAction_(datafile.wrapAt(100));
			~controlsEffetsMenu.valueAction_(datafile.wrapAt(101));
			~panEffets.valueAction_(datafile.wrapAt(102));
			~ampEffets.valueAction_(datafile.wrapAt(103));
			~jitterPanEffets.valueAction_(datafile.wrapAt(148));
			~randomPanEffets.value_(~automationPanEffets.wrapAt(datafile.wrapAt(99)));
			~randomControlsEffets.value_(~automationControlsEffets.wrapAt(datafile.wrapAt(99)));
			~speedEffets.valueAction_(datafile.wrapAt(205));
			/*~startAutomation.valueAction_(datafile.wrapAt(104));
			~flagInitAutomation=datafile.wrapAt(103);if(~flagInitAutomation=='on',{~initAutomation.valueAction_(1)},{~initAutomation.valueAction_(0)});
			~flagUniversAutomation=datafile.wrapAt(106);if(~flagUniversAutomation=='on',{~universAutomation.valueAction_(1)},{~universAutomation.valueAction_(0)});
			~flagMondesAgentsAutomation=datafile.wrapAt(107);if(~flagMondesAgentsAutomation=='on',{~mondesAgentsAutomation.valueAction_(1)},{~mondesAgentsAutomation.valueAction_(0)});
			~flagMondesMusiqueAutomation=datafile.wrapAt(108);if(~flagMondesMusiqueAutomation=='on',{~mondesMusiqueAutomation.valueAction_(1)},{~mondesMusiqueAutomation.valueAction_(0)});
			~flagGenesMAutomation=datafile.wrapAt(109);if(~flagGenesMAutomation=='on',{~genesMAutomation.valueAction_(1)},{~genesMAutomation.valueAction_(0)});
			~flagGenesSAutomation=datafile.wrapAt(110);if(~flagGenesSAutomation=='on',{~genesSAutomation.valueAction_(1)},{~genesSAutomation.valueAction_(0)});
			~flagSynthMusiqueAutomation=datafile.wrapAt(111);if(~flagSynthMusiqueAutomation=='on',{~synthMusiqueAutomation.valueAction_(1)},{~synthMusiqueAutomation.valueAction_(0)});
			~flagSynthAgentsAutomation=datafile.wrapAt(112);if(~flagSynthAgentsAutomation=='on',{~synthAgentsAutomation.valueAction_(1)},{~synthAgentsAutomation.valueAction_(0)});*/
			~densite.valueAction_(datafile.wrapAt(113));
			~controlsSynth=datafile.wrapAt(114);
			~controlsSynthMenu.valueAction_(~controlsSynth.wrapAt(datafile.wrapAt(79)));
			~antiClick=datafile.wrapAt(115);
			~controlsAntiClickMenu.valueAction_(datafile.wrapAt(115));
			~flagGeneControl=datafile.wrapAt(116);
			if(~flagGeneControl=='on', {~geneControlsButton.valueAction_(1)},{~geneControlsButton.valueAction_(0)});
			~quantaMusicSlider.valueAction=~quantaMusic=datafile.wrapAt(117);
			~automationPanEffets=datafile.wrapAt(118);
			~automationControlsEffets=datafile.wrapAt(119);
			~automationControlsSynth=datafile.wrapAt(120);
			~geneFreqRanger.valueAction=datafile.wrapAt(121);
			~geneTransFreqRanger.valueAction=datafile.wrapAt(122);
			~geneAmpRanger.valueAction=datafile.wrapAt(123);
			~geneDureeRanger.valueAction=datafile.wrapAt(124);
			~geneMulDureeRanger.valueAction=datafile.wrapAt(125);
			~genePanRanger.valueAction=datafile.wrapAt(126);
			~geneBufferRanger.valueAction=datafile.wrapAt(127);
			~geneSampleRangerLow.valueAction=datafile.wrapAt(128).wrapAt(0);
			~geneSampleRangerHigh.valueAction=datafile.wrapAt(128).wrapAt(1);
			~geneReverseRanger.valueAction=datafile.wrapAt(129);
			~geneLoopRanger.valueAction=datafile.wrapAt(130);
			~geneOffsetRanger.valueAction=datafile.wrapAt(131);
			~geneEnvLevelRanger.valueAction=datafile.wrapAt(132);
			~geneEnvDureeRanger.valueAction=datafile.wrapAt(133);
			~geneSynthRangerLow.valueAction=datafile.wrapAt(134).wrapAt(0);
			~geneSynthRangerHigh.valueAction=datafile.wrapAt(134).wrapAt(1);
			~geneControlsRanger.valueAction=datafile.wrapAt(135);
			// Test Chanels Audio Out
			allVal = datafile.wrapAt(136);
			loVal = allVal.wrapAt(0);
			if(loVal > ~sourceOutAgents.size, {loVal = ~sourceOutAgents.size});
			hiVal = allVal.wrapAt(1);
			if(hiVal > ~sourceOutAgents.size, {hiVal = ~sourceOutAgents.size});
			allVal = [loVal, hiVal];
			~geneAudioOutRanger.valueAction = allVal;
			~geneAudioOutRanger.valueAction = datafile.wrapAt(136);
			// MIDI
			~geneMidiOutRanger.valueAction=datafile.wrapAt(137);
			// Test Chanels Audio In
			loVal = datafile.wrapAt(138).wrapAt(0);
			if(loVal >= ~audioInLR.size, {loVal = ~audioInLR.size - 1});
			~geneInputRangerLow.valueAction_(loVal);
			hiVal = datafile.wrapAt(138).wrapAt(1);
			if(hiVal >= ~audioInLR.size, {hiVal = ~audioInLR.size - 1});
			~geneInputRangerHigh.valueAction_(hiVal);
			~geneInputRangerLow.valueAction=datafile.wrapAt(138).wrapAt(0);
			//~geneInputRangerHigh.valueAction=datafile.wrapAt(138).wrapAt(1);
			~geneLoopMusicRanger.valueAction=datafile.wrapAt(139);
			~geneBufferMusicRanger.valueAction=datafile.wrapAt(140);
			~geneChordMaxRanger.valueAction=datafile.wrapAt(141);
			~geneChordDurRanger.valueAction=datafile.wrapAt(142);
			~geneVieillissement.valueAction=datafile.wrapAt(143);
			~geneDeplacement.valueAction=datafile.wrapAt(144);
			~geneVision.valueAction=datafile.wrapAt(145);
			~geneAudition.valueAction=datafile.wrapAt(146);
			~ampSynth.valueAction_(datafile.wrapAt(147));
			// Tuning Array 152
			~listTuning.valueAction = datafile.wrapAt(152).wrapAt(0);
			~rootChoice.valueAction = datafile.wrapAt(152).wrapAt(1);
			~displayDegrees.valueAction = datafile.wrapAt(152).wrapAt(2);
			~flagScaling = datafile.wrapAt(152).wrapAt(3);
			//Algorithm
			~flagGeneAlgorithm = datafile.wrapAt(153);
			~geneAlgorithm.lo = datafile.wrapAt(154).wrapAt(0);
			~geneAlgorithm.hi = datafile.wrapAt(154).wrapAt(1);
			~algoMusic = datafile.wrapAt(155);
			~listeAlgorithm.do({arg item, index; if(item == ~algoMusic, {~choiceAlgoData.value = index})});
			if(~flagGeneAlgorithm =='on', {~geneAlgorithmButton.valueAction=1},{~geneAlgorithmButton.valueAction=0});
			// Band
			~numberBand.valueAction = datafile.wrapAt(156);
			~buttonSynthBand.valueAction = datafile.wrapAt(157);
			~flagSynthBand = datafile.wrapAt(158);
			~rangeSynthBand = datafile.wrapAt(159);
			~flagBandSynth = datafile.wrapAt(160);
			~synthBand0.valueAction = datafile.wrapAt(161);
			~synthBand1.valueAction = datafile.wrapAt(162);
			~synthBand2.valueAction = datafile.wrapAt(163);
			~synthBand3.valueAction = datafile.wrapAt(164);
			~synthBand4.valueAction = datafile.wrapAt(165);
			~synthBand5.valueAction = datafile.wrapAt(166);
			~synthBand6.valueAction = datafile.wrapAt(167);
			~synthBand7.valueAction = datafile.wrapAt(168);
			~synthBand8.valueAction = datafile.wrapAt(169);
			~synthBand9.valueAction = datafile.wrapAt(170);
			~synthBand10.valueAction = datafile.wrapAt(171);
			~synthBand11.valueAction = datafile.wrapAt(172);
			~synthBand12.valueAction = datafile.wrapAt(173);
			// Gene
			~buttonGeneBand.valueAction = datafile.wrapAt(174);
			~flagBandGenes = datafile.wrapAt(175);
			~geneBand0.valueAction = datafile.wrapAt(176);
			~geneBand1.valueAction = datafile.wrapAt(177);
			~geneBand2.valueAction = datafile.wrapAt(178);
			~geneBand3.valueAction = datafile.wrapAt(179);
			~geneBand4.valueAction = datafile.wrapAt(180);
			~geneBand5.valueAction = datafile.wrapAt(181);
			~geneBand6.valueAction = datafile.wrapAt(182);
			~geneBand7.valueAction = datafile.wrapAt(183);
			~geneBand8.valueAction = datafile.wrapAt(184);
			~geneBand9.valueAction = datafile.wrapAt(185);
			~geneBand10.valueAction = datafile.wrapAt(186);
			~geneBand11.valueAction = datafile.wrapAt(187);
			~geneBand12.valueAction = datafile.wrapAt(188);
			// Verb
			~audioOutVerb=datafile.wrapAt(189);
			~playSynthVerb=datafile.wrapAt(190);
			~controlsSynthVerb=datafile.wrapAt(191);
			~panSynthVerb=datafile.wrapAt(192);
			~jitterPanSynthVerb=datafile.wrapAt(203);
			~jitterControlsSynthVerb=datafile.wrapAt(204);
			~ampSynthVerb=datafile.wrapAt(193);
			~automationPanVerb=datafile.wrapAt(200);
			~automationControlsVerb=datafile.wrapAt(201);
			~automationSpeedVerb=datafile.wrapAt(208);
			~listSynthVerb.size.do({arg verb;
				~listSynthVerb.wrapAt(verb).set('out', ~audioOutVerb.wrapAt(verb), 'amp', ~ampSynthVerb.wrapAt(verb).dbamp, 'pan', ~panSynthVerb.wrapAt(verb), 'control1', ~controlsSynthVerb.wrapAt(verb).wrapAt(0),  'control2', ~controlsSynthVerb.wrapAt(verb).wrapAt(1),  'control3', ~controlsSynthVerb.wrapAt(verb).wrapAt(2),  'control4', ~controlsSynthVerb.wrapAt(verb).wrapAt(3),  'control5', ~controlsSynthVerb.wrapAt(verb).wrapAt(4),  'control6', ~controlsSynthVerb.wrapAt(verb).wrapAt(5),  'control7', ~controlsSynthVerb.wrapAt(verb).wrapAt(6),  'control8', ~controlsSynthVerb.wrapAt(verb).wrapAt(7));
				if(~playSynthVerb.wrapAt(verb) == 1, {~listSynthVerb.wrapAt(verb).run(true)},{~listSynthVerb.wrapAt(verb).run(false)});
			});
			~sourceOutVerb.valueAction=datafile.wrapAt(194);
			~verbInstrMenu.valueAction_(datafile.wrapAt(195));
			~playVerbButton.valueAction_(datafile.wrapAt(196));
			~controlsVerbMenu.valueAction_(datafile.wrapAt(197));
			~panVerb.valueAction_(datafile.wrapAt(198));
			~ampVerb.valueAction_(datafile.wrapAt(199));
			~jitterPanVerb.valueAction_(datafile.wrapAt(202));
			~randomPanVerb.value_(~automationPanVerb.wrapAt(datafile.wrapAt(195)));
			~randomControlsVerb.value_(~automationControlsVerb.wrapAt(datafile.wrapAt(195)));
			~speedEffets.valueAction_(datafile.wrapAt(205));
			~speedVerb.valueAction_(datafile.wrapAt(207));
			~flagRootAutomation=datafile.wrapAt(209);if(~flagRootAutomation=='on',{~rootAutomation.valueAction_(1)},{~rootAutomation.valueAction_(0)});
			rangeBand.valueAction = datafile.wrapAt(210);
			// + Genome (211) + Sequence (212)
			if(flagGenome == 'on' and: {datafile.wrapAt(211).size != 0}, {
				~genomes=datafile.wrapAt(211);// Load Genomes
				~startpopulation = ~agents = ~genomes.size;
				~foncInitAgents.value;
				~genomes=datafile.wrapAt(211); // Load Genomes again for INIT
				if(flagSequence == 'on' and: {datafile.wrapAt(211).wrapAt(0).size != 0}, {sequence=datafile.wrapAt(212);~listeagentfreq=sequence.wrapAt(0);~listeagentamp=sequence.wrapAt(1);~listeagentduree=sequence.wrapAt(2)}, {flagSequence='off'})},
			{~foncInitAgents.value});
		};

		// Fonctions Score
		~fonctionRecordScore = {arg commande, valeur;var newTime, time;
			if(~flagRecordScore == 'on', {
				newTime=~tempoMusicPlay.beats;
				time = abs(newTime - ~lastTimeRecordScore);
				if(commande != "~evaluationKeyDown", {
					// write commande et valeur en cours
					~scoreRecording=~scoreRecording.add([0, commande, valeur]);
					// write duree ecoulee dans avant dernier evenement
					if(~scoreRecording.size > 1, {~scoreRecording.wrapAt(~scoreRecording.size - 2).wrapPut(0, time)});
					~lastTimeRecordScore=newTime;
				}, {// ctrl+u and esc and spacebar
					if(valeur.wrapAt(0) != 21.asAscii and: {valeur.wrapAt(0) != 27.asAscii}, {
						// write commande et valeur en cours
						~scoreRecording=~scoreRecording.add([0, commande, valeur]);
						// write duree ecoulee dans avant-dernier evenement
						if(~scoreRecording.size > 1, {~scoreRecording.wrapAt(~scoreRecording.size - 2).wrapPut(0, time)});
						~lastTimeRecordScore=newTime;
					});
				});
				Post << "Score: "<< ~numeroScore << Char.nl;
				Post << "Commande: "<< commande << Char.nl;
				Post << "Value: "<< valeur << Char.nl;
				Post << "Time: "<< time << Char.nl;
			});
		};

		~routinePlayingScore = {arg score, number, pos, flagLoop;
			var time=0.04167 / ~tempoMusicPlay.tempo.reciprocal, cmd, val;
			Tdef(("score"+number.asString+Date.localtime.asString).asSymbol,
				{Routine{arg inval;
					score.do({arg item;
						time = item.wrapAt(0);
						cmd = item.wrapAt(1);
						val = item.wrapAt(2);
						Post << "Score: "<< number << " Loop " << flagLoop << Char.nl;
						Post << "Commande: "<< cmd << Char.nl;
						Post << "Value: "<< val << Char.nl;
						Post << "Time: "<< time << Char.nl;
						//Post.nl;
						if(time < 0.015625, {time = 0.04167 * ~tempoMusicPlay.tempo});// Prevention pour timing trop court affichage GUI
						if(~listeFlagRoutinePlayingScore.wrapAt(number-1).wrapAt(pos-1) == 'on', {
							if(cmd != "End Score", {
								if(cmd == "~evaluationKeyDown", {{cmd.interpret.value(val.wrapAt(0), val.wrapAt(1),val.wrapAt(2),val.wrapAt(3), val.wrapAt(4))}.defer},
									{if(cmd == "~validGenomeAll",{~genomes=val; {~fonctionUpdateGenomeAll.value}.defer},
										{if(cmd ==
											"~validSeqFreqAmpDur",{~listeagentfreq=val.wrapAt(0);~listeagentamp=val.wrapAt(1);~listeagentduree=val.wrapAt(2); {~fonctionUpdateSequence.value(~agentSequence)}.defer},
											{if(cmd == "Special Cmd", {val.interpret},
												{{cmd.interpret.valueAction_(val)}.defer})})})})},
							{if(flagLoop == 'on', {
								("Loop score"+number.asString++"."++pos.asString).postln;
								time.yieldAndReset},
							{
								~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(pos-1).stop;
								~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(pos-1).remove;
							})});
						},
						{~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(pos-1).stop;
							~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(pos-1).remove;
							thisThread.stop;
							thisThread.remove;
						});
						time.yield;
					});
					~listeRoutinePlayingScore.wrapAt(number-1).size.do({arg i;
						~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(i).stop;
						~listeRoutinePlayingScore.wrapAt(number-1).wrapAt(i).remove;
						("Stop score"+number.asString++"."++(i+1).asString).postln;
					});
					~listeRoutinePlayingScore.wrapPut(number-1, []);
					~listeFlagRoutinePlayingScore.wrapPut(number-1, []);
				}.play;
				};
			);
		};

		~scoreStep={arg score, index, flag, number; //~score, ~indexStepScore, ~flagStepScore;
			var event, time, cmd, val;
			if(index >= (score.size - 1),
				{
					if(flag == 'on', {
						Post << "Score: "<< number << " Loop " << flag << Char.nl;
						~indexStepScore=0; index = 0;
					},
					{
						Post << "Score: "<< number << " Stop " << Char.nl;
						~score=nil; score = nil; ~numberStepScore=nil; number=nil;
			})});
			if(score != nil,
				{
					event = score.at(index);
					time = event.wrapAt(0);
					cmd = event.wrapAt(1);
					val = event.wrapAt(2);
					Post << "Score: "<< number << " Loop " << flag << Char.nl;
					Post << "Commande: "<< cmd << Char.nl;
					Post << "Value: "<< val << Char.nl;
					Post << "Time: "<< time << Char.nl;
					if(cmd != "End Score", {
						if(cmd == "~evaluationKeyDown", {cmd.interpret.value(val.wrapAt(0), val.wrapAt(1),val.wrapAt(2),val.wrapAt(3), val.wrapAt(4))},
							{if(cmd == "~validGenomeAll",{~genomes=val; {~fonctionUpdateGenomeAll.value}.defer},
								{if(cmd ==
									"~validSeqFreqAmpDur",{~listeagentfreq=val.wrapAt(0);~listeagentamp=val.wrapAt(1);~listeagentduree=val.wrapAt(2); {~fonctionUpdateSequence.value(~agentSequence)}.defer},
									{if(cmd == "Special Cmd", {val.interpret},
										{{cmd.interpret.valueAction_(val)}.defer})})})});
					});
					~indexStepScore = ~indexStepScore + 1;
			});
		};

	}

	windowsPanel {

		~listeWindows=~listeWindows.add(~wp);
		~listeWindows=~listeWindows.add(~wi);
		~listeWindows=~listeWindows.add(~we);
		~listeWindows=~listeWindows.add(~wg);
		~listeWindows=~listeWindows.add(~wad);
		~listeWindows=~listeWindows.add(~wcm);
		~listeWindows=~listeWindows.add(~wm);
		~listeWindows=~listeWindows.add(~wv);
		~listeWindows=~listeWindows.add(~windowMasterFX);
		~listeWindows=~listeWindows.add(windowKeyboard);
		~listeWindows=~listeWindows.add(windowVST);
		~listeWindows.do({arg window; ~fonctionShortCuts.value(window)});

		// Setup Font View
		~listeWindows.do({arg window;
			window.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Helvetica", 11);
					if(subView.asString.containsi("PopUpMenu"), {subView.stringColor = Color.white});
				});
			});
		});
		// Virtual Space
		~wm.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 8);
			});
		});
	}

	initSynthDef {arg flag=true, f, w, x, y, z;

		if(flag == true, {

			// New Analyse Audio
			SynthDef("OSC Agents Onsets",
				{arg in=0,  seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, centroid, flatness, fft, energy, flux,
					inputFilter;
					input= SoundIn.ar(in);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);// \rcomplex
					# freqin, hasfreqin = Tartini.kr(inputFilter, filtre, 2048, 1024, 512, 0.5);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse Audio  2
			SynthDef("OSC Agents Pitch",
				{arg in=0, seuil=0.5, filtre=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin=0, ampin, centroid, flatness, fft, energy, flux,
					inputFilter;
					input= SoundIn.ar(in);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
					# freqin, hasfreqin = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse Audio  3
			SynthDef("OSC Agents Pitch2",
				{arg in=0, seuil=0.5, filtre=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin=0, ampin, centroid, flatness, fft, fft2, energy, flux,
					inputFilter, harmonic, percussive;
					input= SoundIn.ar(in);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					fft2 = FFT(LocalBuf(512, 1), inputFilter);
					harmonic = FFT(LocalBuf(512, 1), inputFilter);
					percussive = FFT(LocalBuf(512, 1), inputFilter);
					#harmonic, percussive = MedianSeparation(fft2, harmonic, percussive, 512, 5, 1, 2, 1);
					detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
					# freqin, hasfreqin = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse Audio  4
			SynthDef("OSC Agents KeyTrack",
				{arg in=0, seuil=0.5, filtre=1;
					var input, detect, freqin, ampin, centroid, flatness, fft, energy, key, flux;
					input= SoundIn.ar(in);
					detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
					key = KeyTrack.kr(FFT(Buffer.alloc(s, 4096, 1), input), (filtre * 2).clip(0, 2));
					if(key < 12, freqin = (key + 60).midicps, freqin = (key - 12 + 60).midicps);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// Keyboard
			SynthDef("OSC Agents Keyboard",
				{arg in=0, note=60, amp=0.5, trigger = 0;
					var input, centroid, flatness, fft, energy, key, flux;
					input= SoundIn.ar(in);
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					note = note.cpsmidi / 127;
					SendReply.kr(trigger, '/Agents_Analyse_Audio', values: [note, amp, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).add;

			// New Analyse File
			SynthDef("OSC Agents File Onsets",
				{arg  busFileIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var detect, freqin, hasfreqin, ampin, input, centroid, flatness, fft, energy, flux,
					inputFilter;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);// \rcomplex
					# freqin, hasfreqin = Tartini.kr(inputFilter, filtre, 2048, 1024, 512, 0.5);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse File version 2
			SynthDef("OSC Agents File Pitch",
				{arg  busFileIn, seuil=0.5, filtre=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var detect, freqin, hasfreqin=0, ampin, input, centroid, flatness, fft, energy, flux,
					inputFilter;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
					# freqin, hasfreqin = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median:1, peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse File version 3
			SynthDef("OSC Agents File Pitch2",
				{arg  busFileIn, seuil=0.5, filtre=0, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var detect, freqin, hasfreqin=0, ampin, input, centroid, flatness, fft, fft2, energy, flux,
					inputFilter, harmonic, percussive;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					fft2 = FFT(LocalBuf(512, 1), inputFilter);
					harmonic = FFT(LocalBuf(512, 1), inputFilter);
					percussive = FFT(LocalBuf(512, 1), inputFilter);
					#harmonic, percussive = MedianSeparation(fft2, harmonic, percussive, 512, 5, 1, 2, 1);
					detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
					# freqin, hasfreqin = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// New Analyse File version 4
			SynthDef("OSC Agents File KeyTrack",
				{arg  busFileIn, seuil=0.5, filtre=1;
					var detect, freqin, ampin, input, centroid, flatness, fft, energy, key, flux;
					input = In.ar(busFileIn);
					detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
					key = KeyTrack.kr(FFT(Buffer.alloc(s, 4096, 1), input), (filtre * 2).clip(0, 2));
					if(key < 12, freqin = (key + 60).midicps, freqin = (key - 12 + 60).midicps);
					ampin = A2K.kr(Amplitude.ar(input));
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/Agents_Analyse_Audio', values: [freqin, ampin, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).send(s);

			// Keyboard file
			SynthDef("OSC Agents File Keyboard",
				{arg busFileIn, note=60, amp=0.5, trigger = 0;
					var input, centroid, flatness, fft, energy, key, flux;
					input = In.ar(busFileIn);
					fft = FFT(LocalBuf(1024, 1), input);
					centroid = SpecCentroid.kr(fft);
					flatness =  SpecFlatness.kr(fft);
					energy =  SpecPcile.kr(fft);
					flux =  FFTFlux.kr(fft);
					note = note.cpsmidi / 127;
					SendReply.kr(trigger, '/Agents_Analyse_Audio', values: [note, amp, centroid, flatness.clip(0.0001, 1), energy, flux.clip(0.0001, 1)], replyID: [1, 2, 3, 4, 5, 6]);
			}).add;

			// Synth pour records samples
			SynthDef("RecSampleIn",
				{arg in=0, buffer, offset=0, run=1, loop=1, trigger=0, reclevel1=1, reclevel2=0;
					var samplein;
					samplein=SoundIn.ar(in);
					RecordBuf.ar(samplein, buffer, offset, reclevel1, reclevel2, run, loop, trigger);
			}).send(s);

			// Synth pour records files
			SynthDef("RecFileIn",
				{arg in=0, buffer, offset=0, run=1, loop=1, trigger=0, reclevel1=1, reclevel2=0;
					var fileIn;
					fileIn=In.ar(in);
					RecordBuf.ar(fileIn, buffer, offset, reclevel1, reclevel2, run, loop, trigger);
			}).send(s);

			// Synth pour records Buffer
			SynthDef("RecBufferAudioIn",
				{arg in=0, buffer, offset=0, run=1, loop=0, trigger=0, reclevel1=1, reclevel2=0;
					var samplein;
					samplein=SoundIn.ar(in);
					trigger = Trig1.kr(Dust.kr(trigger), BufDur.kr(buffer));
					RecordBuf.ar(samplein, buffer, offset, reclevel1, reclevel2, 1, 0, trigger);
			}).send(s);

			// Synth pour records Buffer file
			SynthDef("RecBufferFileIn",
				{arg in=0, buffer, offset=0, run=1, loop=0, trigger=0, reclevel1=1, reclevel2=0;
					var fileIn;
					fileIn=In.ar(in);
					trigger = Trig1.kr(Dust.kr(trigger), BufDur.kr(buffer));
					RecordBuf.ar(fileIn, buffer, offset, reclevel1, reclevel2, 1, 0, trigger);
			}).send(s);

			// Synth lecture file pour analyse AudioIn
			SynthDef("Agents Play File",
				{arg out=0, bufferplay, busFileIn, trig=0, offset=0, loop=1, volume=0;
					var input;
					input=PlayBuf.ar(2, bufferplay, BufRateScale.kr(bufferplay), trig, BufFrames.kr(bufferplay)*offset , loop);
					Out.ar(out, input * volume); // Amp File Out
					Out.ar(busFileIn, Mix(input)); // Pour recordings buffers agents
			}).send(s);

			// Synth MasterFX
			SynthDef("MasterFX",
				{arg out=0, limit=0.8, postAmp=1.0;
					ReplaceOut.ar(out, Limiter.ar(LeakDC.ar(In.ar(0, ~numberAudioOut) * postAmp), limit));
			}).send(s);

			// Analyse Tempo AudioIn
			SynthDef("OSC Agents Tempo AudioIn",
				{arg in=0, lock=0;
					var trackb,trackh,trackq,tempo, source;
					source = SoundIn.ar(in);
					#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(LocalBuf(1024, 1), source, 0.5, 1), lock);
					SendReply.kr(trackb, '/Agents_Analyse_Tempo', values: [tempo], replyID: [1]);
			}).send(s);

			// Analyse Tempo FileIn
			SynthDef("OSC Agents Tempo FileIn",
				{arg busFileIn, lock=0;
					var trackb,trackh,trackq,tempo, source;
					source = In.ar(busFileIn);
					#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(LocalBuf(1024, 1), source, 0.5, 1), lock);
					SendReply.kr(trackb, '/Agents_Analyse_Tempo', values: [tempo], replyID: [1]);
			}).send(s);

			// Synth VST
			SynthDef("VST Plugin",
				{arg out=0, xFade=0.5, panLo=0, panHi=0, gainIn=0.5, bpm=1;
					var signal, chain, ambisonic;
					bpm = if(bpm > 1, bpm.reciprocal, bpm);
					signal = Mix(In.ar(0, ~numberAudioOut)) * gainIn;
					chain = Mix(VSTPlugin.ar(signal, ~numberAudioOut));
					//chain = Pan2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
					chain = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1)),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm).lag(bpm.reciprocal + 1)), 1, ~widthMC, ~orientationMC);,
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(chain, chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1)),
								// Ambisonic
								(
									ambisonic = PanB2.ar(chain, TRand.kr(panLo, panHi, Impulse.kr(bpm)).lag(bpm.reciprocal + 1));
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2]);
					))));
					// Out
					XOut.ar(out, xFade, chain);
			}).add;

			///////////////// SYNTHDEF With PlayBuf//////

			SynthDef("PlayBuf",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					//offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset, loop);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PlayBuf2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					// Main Synth
					main=PlayBuf.ar(1, buffer,  BufRateScale.kr(buffer) * rate, Impulse.kr(controlF * 100), BufFrames.kr(buffer)*offset, loop);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("TGrains",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					// Main Synth
					main=Mix(TGrains.ar(2, Impulse.kr(controlF*100), buffer, BufRateScale.kr(buffer) * rate, BufDur.kr(buffer)*offset, (duree*controlD)/(controlF*100), 0.0, amp));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("BufRd",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main=BufRd.ar(1,buffer,Phasor.ar(0, BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer)*offset, BufFrames.kr(buffer)*controlA, BufFrames.kr(buffer)*controlD), loop);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("BufRd2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, offset2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					offset2 = if(controlD.value <= 0.01 , Rand(0, 1), Logistic.kr(controlD*4, 1, Rand(0, 1)));
					// Main Synth
					offset2 = (controlF+controlA).clip(0, 1);
					main=BufRd.ar(1,buffer, Phasor.ar(Impulse.kr(controlF*100), BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer)*offset, BufFrames.kr(buffer)*offset2, BufFrames.kr(buffer)*controlF), BufRateScale.kr(buffer) * rate, loop);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("GrainBuf",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, offset2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					offset = if(offset <= 0, Logistic.kr(controlD*4, 1, Rand(0, 1)), offset);
					main=GrainBuf.ar(1, Dust.kr(100*controlF), controlA*0.1, buffer, BufRateScale.kr(buffer) * rate, offset, 4, 0, -1, 512);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("LoopBuf",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=LoopBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate,  1, BufFrames.kr(buffer)*offset, BufFrames.kr(buffer)*controlF, BufFrames.kr(buffer)*controlA);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPplayBuf",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					//offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main = HPplayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop, antiClick1, antiClick2);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPplayBufMedianLeakDC",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					//offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main = LeakDC.ar(Median.ar(controlF * 30 + 1, HPplayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop, antiClick1, antiClick2)), controlA);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPplayBufVibrato",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main = HPplayBuf.ar(1,buffer, SinOsc.kr(controlA*dureesample.reciprocal*10, mul: controlD, add: rate), 0, BufFrames.kr(buffer)*offset,loop, antiClick1, antiClick2);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPtGrains",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					// Main Synth
					main=Mix(HPtGrains.ar(2, Impulse.kr(controlF*100), buffer, BufRateScale.kr(buffer) * rate, BufDur.kr(buffer)*offset, (duree*controlD)/(controlF*100), 0.0, amp, antiClick1, antiClick2));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPbufRd",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					// Main Synth
					main = HPbufRd.ar(1, buffer,Phasor.ar(0, BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer)*offset, BufFrames.kr(buffer)*controlA, BufFrames.kr(buffer)*controlD), BufRateScale.kr(buffer) * rate, loop, antiClick1, antiClick2);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPplayBuf2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					// Main Synth
					main = HPplayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, Impulse.kr(controlF*100), BufFrames.kr(buffer)*offset, loop, antiClick1, antiClick2);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("HPbufRd2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, offset2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					offset2 = if(controlD.value <= 0.01 , Rand(0, 1), Logistic.kr(controlD*4, 1, Rand(0, 1)));
					main = HPbufRd.ar(1,buffer, Phasor.ar(Impulse.kr(controlF*100), BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer)*offset, BufFrames.kr(buffer)*offset2, BufFrames.kr(buffer)*controlF), BufRateScale.kr(buffer) * rate, loop, antiClick1, antiClick2);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("SampleResonz",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, osc, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					osc = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					if(rate.abs >= 1.0 , main=Resonz.ar(osc, XLine.ar(127.midicps*controlF+24.midicps, 55*controlA + 24.midicps, duree*controlD)), main=Resonz.ar(osc, XLine.ar(55*controlF+24.midicps, 127.midicps*controlA + 24.midicps, duree*controlD)));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Synthesizer",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, osc, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate = rate.clip(0.1, 1.0);
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					freq = freq.clip(20, 12544);
					osc = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					if(freq < 64.5.midicps , main = RLPF.ar(osc, XLine.ar(63.5.midicps*controlF+27.5, freq, duree*controlD), 0.333), main = RHPF.ar(osc, XLine.ar(127.midicps*controlA+27.5, freq, duree*controlD), 0.333));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PlayBufSquiz",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Squiz.ar(PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset, loop), controlF * 10, controlA * 10);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("WaveLoss",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=WaveLoss.ar(PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset, loop), controlF * 40, 40, abs(controlA*2-1));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("FreqShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Mix(FreqShift.ar(PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset, loop), controlF * 1024 - 512, controlA * 2pi));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PitchShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Mix(PitchShift.ar(PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset, loop), 0.2, controlF*4, controlA, controlD, 1));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Warp1",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, pointer, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					//pointer = if(reverse > 0, Line.kr(offset, controlF, dureesample), Line.kr(controlF, offset, dureesample));
					//main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer), 1);
					//RecordBuf.ar(main, buffer, 0, 1, 0);
					main = Warp1.ar(1, buffer, offset, BufRateScale.kr(buffer) * rate, controlF + 0.01, -1, controlA * 15 + 1, controlD);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Warp0",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					//main = Warp0.ar(1, buffer, 1, BufRateScale.kr(buffer) * rate, controlF * duree / 2, -1, controlA * 7 + 1);
					main = Warp1.ar(1, buffer, controlD, BufRateScale.kr(buffer) * rate, controlF * duree / 2, -1, controlA * 15 + 1);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			/////////////////// SynthDef with PV ////////////////////

			SynthDef("PV_HPshiftDown",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer,  buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = HPplayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop, antiClick1, antiClick2);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_HPshiftDown(main, controlF*32);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagShift(main, controlF * 4, controlA * 128 - 64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_LocalMax",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_LocalMax(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagSmear",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmear(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_RandComb",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RandComb(main, controlF,  LFNoise2.kr(controlA*64));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_BinShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinShift(main, controlF*4,  controlA*256 - 128);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_BinScramble",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinScramble(main, controlF,  controlA, LFNoise2.kr(controlD.reciprocal));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_BrickWall",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BrickWall(main, controlF*2 - 1);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_ConformalMap",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_ConformalMap(main, controlF*2 - 1, controlA*2 - 1);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Diffuser",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Diffuser(main, Trig1.kr(LFNoise2.kr(controlF*100), (controlA*100).reciprocal));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagAbove",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagAbove(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagBelow",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagBelow(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagClip",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagClip(main, controlF*16);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagNoise",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagNoise(main);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagSquared",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSquared(main);
					main= IFFT(main) * 0.01;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_RectComb",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RectComb(main, controlF * 32, controlA, controlD);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagSmooth",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmooth(main, controlF);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Compander",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Compander(main, 80*controlF.clip(0.1, 1), (controlA*5).clip(2, 5), controlD);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_SpectralEnhance",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_SpectralEnhance(main, (controlF*8+0.5).floor, controlA*4+1, controlD);
					main= IFFT(main) * 0.125;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagStretch",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagShift(main, controlF.clip(0.25, 4));
					main= IFFT(main) * 0.125;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagShift+Stretch",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagShift(main, controlF.clip(0.25, 4), controlA - 0.5 * 128);
					main= IFFT(main) * 0.125;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Cutoff",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Cutoff(main, controlF * 2 - 1);
					main= IFFT(main) * 0.125;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Max",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Max(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Min",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Min(fft1, fft2);
					main=IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_MagDiv",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_MagDiv(fft1, fft2, controlF+0.0001);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Mul",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Mul(fft1, fft2);
					main= IFFT(main) * 0.1;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Div",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Div(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Add",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Add(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_RandWipe",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_RandWipe(fft1, fft2, controlF, LFNoise2.kr(controlA.reciprocal));
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_BinWipe",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_BinWipe(fft1, fft2, controlF*2 - 1);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_CopyPhase",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_CopyPhase(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_RectComb2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024),in1);
					fft2=FFT(LocalBuf(1024),in2);
					main=PV_RectComb2(fft1, fft2, controlF * 32, controlA, controlD);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("PV_Morph",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Morph(fft1, fft2, controlF);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Convolution",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					in2=PlayBuf.ar(1,buffer2,BufRateScale.kr(buffer2) * rate, 0, BufFrames.kr(buffer2)*0,loop);
					main=Convolution.ar(in1, in2, 1024) * 0.1;
					//main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			///////////////// SYNTH ////////////////////

			SynthDef("SinOsc",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, fc, osc, a, b, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main= SinOsc.ar(freq, 0, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);}).send(s);

			SynthDef("FMsynth",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, fc, osc, a, b, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = SinOsc.ar(freq+SinOsc.ar(500*controlF, mul:1000*controlA), 0, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("SawSynth",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, fc, osc, a, b, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					freq = freq.clip(20,12544);
					main = Saw.ar(freq, 0.5);
					main = RHPF.ar(main, Line.kr(controlF*4000, freq, duree*controlD), controlA, 0.5, RLPF.ar(main, Line.kr(controlF*2000, freq, duree*controlD), controlA));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("SinOscVibrato",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, fc, osc, a, b, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = SinOsc.ar(SinOsc.kr(controlF*16, mul: Line.kr(0, controlA*100, controlD*duree), add: freq), 0, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Formant",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, dureesamplein, fft, in, dureesample, delay, filtreFreq=[], ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					main = Formant.ar(freq, LFNoise0.kr(controlD.reciprocal)*(controlA*127).midicps, LFNoise0.kr(duree.reciprocal)*(controlF*127).midicps, 0.5);
					// main = Limiter.ar(main, 0.33, 0.01);
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Guitare",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth Guitare
					pluck = BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(duree*controlF*24)), controlA));
					main = CombL.ar(pluck, freq.reciprocal, freq.reciprocal, duree);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Klang",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth Guitare
					main = Klang.ar(`[[controlF, controlA, controlD] * 4186 + 32.703195662575, [amp / 3, amp / 3, amp / 3], nil], freq);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Klank",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = DynKlank.ar(`[[controlF, controlA, controlD] * 4186 + 32.703195662575, [amp / 3, amp / 3, amp / 3], nil], Dust2.ar(duree.reciprocal * 100), freq);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Klank2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = DynKlank.ar(`[[Rand(32.7, 4186), Rand(32.7, 4186), Rand(32.7, 4186)] * controlF, [amp / 3, amp / 3, amp / 3], nil], Impulse.ar(duree.reciprocal * controlD * 64), freq);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Blip",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Blip.ar(freq, Line.kr(50 * controlF + 1,50 * controlA + 1, duree * controlD), 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Pulse",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Pulse.ar(freq, controlF, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("VarSaw",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = VarSaw.ar(freq, controlF, controlA, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Gendy3",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Gendy3.ar(controlF * 6, 4, controlA * 0.1, controlD * 0.1, freq, controlA * 0.1, controlD * 0.1, mul: 0.25);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Spring",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var main, envelope, pluck, ambisonic, k, d, inforce, outforce;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					//main = PMOsc.ar(freq, Spring.ar(LFPulse.ar(controlF * duree), duree / 10 / controlD, controlA * duree / 10) * controlF * 1000 + freq, 0.5);
					inforce = LFPulse.ar(controlF * duree);
					k = controlA * 20;
					d = controlD * 0.001;
					outforce = Spring.ar(inforce, k, d);
					outforce = outforce * freq + freq;
					//main = SinOsc.ar(freq, 0, 0.5);
					main = PMOsc.ar(freq, outforce, Line.kr(0, duree * 2pi), 0, 0.25);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			///////////////// SYNTHDEF PIANO//////

			SynthDef("MdaPiano",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, main, envelope, pluck;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth Piano
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano Synthesizer",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, osc;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					freq = freq.clip(20,12544);
					osc = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					if(freq < 64.5.midicps , main = RLPF.ar(osc, XLine.ar(63.5.midicps*controlF+55, freq, duree*controlD), 0.333), main = RHPF.ar(osc, XLine.ar(127.midicps*controlA+55, freq, duree*controlD), 0.333));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano Resonz",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, osc;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					osc = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					main = if(rate.abs >= 1.0 , Resonz.ar(osc, XLine.ar(127.midicps*controlF+24.midicps, 55*controlA + 24.midicps, duree*controlD)), Resonz.ar(osc, XLine.ar(55*controlF+24.midicps, 127.midicps*controlA + 24.midicps, duree*controlD)));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano Squiz",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Squiz.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controlF * 10, controlA * 10);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano WaveLoss",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=WaveLoss.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controlF* 40, 40, abs(controlF*2-1));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano FreqShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Mix(FreqShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controlF * 1024 - 512, controlA * 2pi));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PitchShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main=Mix(PitchShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), 0.2, controlF*4, controlA, controlD, 1));


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			/////////////////// SynthDef PIANO with PV ////////////////////

			SynthDef("Piano PV_HPshiftDown",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer,  buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_HPshiftDown(main, controlF*32);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagShift(main, controlF * 4, controlA * 128 - 64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_LocalMax",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_LocalMax(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagSmear",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmear(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_RandComb",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RandComb(main, controlF,  LFNoise2.kr(controlA*64));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_BinShift",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinShift(main, controlF*4,  controlA*256 - 128);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_BinScramble",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinScramble(main, controlF,  controlA, LFNoise2.kr(controlD.reciprocal));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_BrickWall",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BrickWall(main, controlF*2 - 1);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_ConformalMap",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_ConformalMap(main, controlF*2 - 1, controlA*2 - 1);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Diffuser",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Diffuser(main, Trig1.kr(LFNoise2.kr(controlF*100), (controlA*100).reciprocal));
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagAbove",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagAbove(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagBelow",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagBelow(main, controlF*64);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagClip",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagClip(main, controlF*16);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagNoise",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagNoise(main);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagSquared",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSquared(main);
					main= IFFT(main) * 0.1;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_RectComb",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RectComb(main, controlF * 32, controlA, controlD);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagSmooth",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmooth(main, controlF);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Compander",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Compander(main, 80*controlF.clip(0.1, 1), (controlA*5).clip(2, 5), controlD);
					main= IFFT(main);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Max",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Max(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Min",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Min(fft1, fft2);
					main=IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_MagDiv",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_MagDiv(fft1, fft2, controlF+0.0001);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Mul",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Mul(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Add",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Add(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_RandWipe",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_RandWipe(fft1, fft2, controlF, LFNoise2.kr(controlA.reciprocal));
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_BinWipe",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_BinWipe(fft1, fft2, controlF*2 - 1);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_CopyPhase",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// enveloperate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_CopyPhase(fft1, fft2);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_RectComb2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_RectComb2(fft1, fft2, controlF * 32, controlA, controlD);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano PV_Morph",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					fft1=FFT(LocalBuf(1024, 1),in1);
					fft2=FFT(LocalBuf(1024, 1),in2);
					main=PV_Morph(fft1, fft2, controlF);
					main= IFFT(main) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Piano Convolution",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var ambisonic, dureesample, envelope, main, in1, in2, fft1, fft2, rate2, recBuf;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: duree, levelScale: 1.0, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2);
					// Main Synth
					in2=PlayBuf.ar(1,buffer, BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*0, loop) * amp;
					main=Convolution.ar(in1, in2, 1024) * 0.5;


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Granulation1",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, local, ambisonic;
					local = LocalBuf(4096, 1).clear;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(Rand(3, 4), 1, Rand(0, 1)));
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop)  * envelope;
					main = BufRd.ar(1, local, Phasor.ar(0, controlF+1, 0, BufFrames.kr(local)), 1);
					BufWr.ar(DelayC.ar(in1, 1.0, controlD/100), local, Phasor.ar(0, controlA+0.001, 0, BufFrames.kr(local)), 1);
					// main = Limiter.ar(main+in1, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Granulation2",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, local, ambisonic;
					local = LocalBuf(4096, 1).clear;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop);
					main = BufRd.ar(1, local, Phasor.ar(0, controlA.neg, 0, BufFrames.kr(local)), 1);
					BufWr.ar(in1 + main * 0.5, local, Phasor.ar(0, controlD, 0, BufFrames.kr(local)), 1);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Toupie",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, main, in1, in2, fft1, fft2, rate2, local, ambisonic;
					local = LocalBuf(s.sampleRate, 1).clear;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					offset = if(controlF.value <= 0.01 , offset, Logistic.kr(controlF*4, 1, Rand(0, 1)));
					in1=PlayBuf.ar(1,buffer,BufRateScale.kr(buffer) * rate, 0, BufFrames.kr(buffer)*offset,loop) * envelope;
					in2 = BufRd.ar(1, local, Phasor.ar(0, controlA+1, 0, BufFrames.kr(local)), 1);
					main = in1 + in2 * 0.5;
					BufWr.ar(main, local, Phasor.ar(0, controlD+0.001, 0, BufFrames.kr(local)), 1);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("Elastique",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, recordBuf, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = CombC.ar(main, 0.1, Line.kr(controlF.clip(0.01, 0.99)/100, controlA.clip(0.01, 0.99)/100, controlD.clip(0.01, 1.0)*dureesample), 1, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("RandElastique",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, recordBuf, ambisonic, main;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = CombC.ar(main, 0.1, Line.kr(Rand(controlF.clip(0.01, 0.99), controlA.clip(0.01, 0.99))/100, Rand(controlF.clip(0.01, 0.99), controlA.clip(0.01, 0.99))/100, controlD.clip(0.01, 1.0)*dureesample), 1, 0.5);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("RandKlankSample",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, recordBuf, main, local1, local2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					main = DynKlank.ar(`[[Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186)], 0.01, [0.16, 0.16, 0.16, 0.16, 0.16, 0.16]], main, controlF, controlD, controlA);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("DjScratch",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, recordBuf, main, local1, local2, ambisonic;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					main = BufRd.ar(1, buffer, Phasor.ar(Dust.kr(dureesample.reciprocal), BufRateScale.kr(buffer) * rate, BufFrames.kr(buffer)* controlF, BufFrames.kr(buffer)* controlA ).lag(controlD)*LFNoise2.kr(controlD).sign, 1);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("LiquidFilter",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main;
					var source, effet, post;
					var formantfreqs, formantamps, formantbandwidths; //data for formants
					formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
					formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
					formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					// Main Synth
					source = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 1.0, BufFrames.kr(buffer)*offset, loop);
					effet= Mix(RHPF.ar(source,  formantfreqs*freq*controlF.clip(0.01, 1), formantbandwidths/(formantfreqs*freq*controlF.clip(0.01, 1.0)), 0.5));
					main = BBandPass.ar(effet, LFNoise2.kr(controlA)+1*4186, controlD.clip(0.1, 1.0), 1);


					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			SynthDef("SynthOnFly",
				{arg out=0, buseffets, busverb, freq=0, rate=0, amp=0,  ampreal=0, duree=1.0, panLo=0, panHi=0, offset=0, loop=0, reverse=1, buffer, buffer2,
					antiClick1=0.33, antiClick2=0.5, controlF=0.5, controlA=0.5, controlD=0.5,
					controlenvlevel1=0.0, controlenvlevel2=1.0, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.75, controlenvlevel6=0.5, controlenvlevel7=0.5, controlenvlevel8=0.0,  controlenvtime1=0.015625, controlenvtime2=0.109375, controlenvtime3=0.25, controlenvtime4=0.25, controlenvtime5=0.125, controlenvtime6=0.125, controlenvtime7=0.125;
					var dureesample, envelope, ambisonic, main, local = 0, source = 0;
					// Set Rate Freq
					rate=2**rate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate=rate * reverse;
					// envelope

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,'sine'), 1.0, timeScale: dureesample, levelScale: 1.0, doneAction: 2);
					offset = if(controlA.value <= 0.01 , offset, Logistic.kr(controlA*4, 1, Rand(0, 1)));
					// Main Synth
					source = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer) * rate, 0.0, BufDur.kr(buffer)*offset, loop, antiClick1, antiClick2);
					local = Mix(LocalIn.ar(1) + source);
					local = DelayN.ar(local, 1.0, controlD);
					LocalOut.ar(local);
					main = local;
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(~flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(~flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, ~widthMC, ~orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, ~widthMC, ~orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffets, Mix(main) * amp);
					Out.ar(busverb, Mix(main) * amp * ampreal);
					Out.ar(out, main * amp * ampreal);
			}).send(s);

			///////////////////////////////////////////////////////////////////

			// Effets pour systeme Agents

			SynthDef("CombC",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(CombC.ar(ineffet, 0.2, [control1/100,control2/200,control3/300,control4/400], [control5*4,control6*4,control7*4,control8*4], amp/4 * 0.6));
					//
					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DelayC",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(DelayC.ar(ineffet, 4.0, [control1*4.0,control2*4.0,control3*4.0,control4*4.0,control5*4.0,control6*4.0,control7*4.0,control8*4.0], amp/8));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BPF",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(BPF.ar(ineffet, [control1*1000+27.5,control2*1000+500,control3*1000+1000,control4*1000+1500], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BRF",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(BRF.ar(ineffet,[control1*1000+27.5,control2*1000+1000,control3*1000+2000,control4*1000+3000], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RHPF",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(RHPF.ar(ineffet, [control1*4186+320.24370022528, control2*4186+320.24370022528, control3*4186+320.24370022528, control4*4186+320.24370022528], [control5, control6, control7, control8], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RLPF",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(RLPF.ar(ineffet, [control1*320.24370022528+27.5, control2*320.24370022528+27.5, control3*320.24370022528+27.5, control4*320.24370022528+27.5], [control5, control6, control7, control8], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PitchShiftFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(PitchShift.ar(ineffet, 0.1,[control1, control2, control3, control4, control5, control6]*4.0, control7, control8, amp/6));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Ringz",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(Ringz.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5*0.1,control6*0.1,control7*0.1,control8*0.1], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Formlet",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(Formlet.ar(ineffet, [control1*300,control2*300+300,control3*300+600,control4*300+900,control5*300+1200,control6*300+1500], control7, control8, amp/6));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Resonz",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(Resonz.ar(ineffet, [control1*500,control2*1000+1000,control3*1000+2000,control4*1000+3000], [control5,control6, control7, control8], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("TwoPole",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(TwoPole.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5,control6,control7,control8], amp/4));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FOS",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(FOS.ar(ineffet, [control1,control2,control3,control4,control5,control6], control7, control8, amp/6));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median",
				{arg out = 0, in, busverb, control1=1, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Median.ar(control1 * 30 + 1, ineffet, amp);

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LeakDC",
				{arg out = 0, in, busverb, control1=0.995, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=LeakDC.ar(ineffet, control1, amp);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median+LeakDC",
				{arg out = 0, in, busverb, control1=0.995, control2=1, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=LeakDC.ar(Median.ar(control1 * 30 + 1, ineffet, amp), control2);

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("MidEQ",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(MidEQ.ar(ineffet, [control1, control2, control3, control4]*4186+27.5, 0.5, [control5, control6, control7, control8]*48-24, amp/2));

					effet = effet * amp;
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DynKlank",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet=Mix(DynKlank.ar(`[[control1, control2, control3, control4]*4186+37, [amp / 4, amp /4, amp /4, amp / 4] / 4, [control5, control6, control7, control8]], ineffet));
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LiveWarp",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf, buffer, local, rate;
					// Input
					ineffet=Mix(In.ar(in, 2));
					localBuf = LocalBuf(s.sampleRate, 1);
					buffer = RecordBuf.ar(ineffet, localBuf, 0, 1, 0);
					local = LocalIn.ar(1);
					rate = control1*4;
					//if(rate < 0.5 , BufRateScale.kr(buffer) * rate, BufRateScale.kr(buffer) * rate, * 20 - 9);
					// effet
					effet = Warp1.ar(1, localBuf, control2, BufRateScale.kr(buffer) * rate, control3, -1, control4*16, control5, 2);// + ou - local;
					//effet = effet * EnvGen.kr(Env.sine(1,1), Impulse.kr(control1*16+0.0625), levelScale: amp);
					//effet = effet * EnvGen.kr(Env.perc(0.05, 1, 1, -5), Impulse.kr(control1*16+0.0625));
					effet = Mix(effet);
					LocalOut.ar(effet);
					//LocalOut.ar(DelayC.ar(effet, 4, control7, control8));
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LivePlayBuf",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf, buffer, local, rate;
					// Input
					ineffet=Mix(In.ar(in, 2));
					localBuf = LocalBuf(s.sampleRate, 1);
					buffer = RecordBuf.ar(ineffet, localBuf, 0, 1, 0);
					local = LocalIn.ar(1);
					rate = control3*4;
					//if(rate < 0.5 , BufRateScale.kr(buffer) * rate, BufRateScale.kr(buffer) * rate, * 20 - 9);
					// effet
					effet = PlayBuf.ar(1, localBuf, BufRateScale.kr(buffer) * rate, Impulse.kr(control1*64+0.0625), 0, 1, control4, control5);// + ou - local;
					effet = effet * EnvGen.kr(Env.sine(1,1), Impulse.kr(control2*64+0.0625), levelScale: amp);
					//effet = effet * EnvGen.kr(Env.perc(0.05, 1, 1, -5), Impulse.kr(control2*64+0.0625));
					effet = Mix(effet);

					LocalOut.ar(effet);
					//LocalOut.ar(DelayC.ar(effet, 4, control4, amp));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("WarpDelay",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf, buffer, local, rate;
					// Input
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(Mix(In.ar(in, 2)) * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0.333, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = Warp1.ar(1, localBuf, control2, control3*4, control4, -1, control5*16, control6, 2);// + ou - local;
					LocalOut.ar(DelayC.ar(effet, 4, control7, control8));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DJ_FX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf, buffer, local, rate;
					// Input
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(Mix(In.ar(in, 2)) * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0.333, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = PlayBuf.ar(1, localBuf, LFNoise2.kr(control2.reciprocal) + (control3*4), Dust.kr(control4.reciprocal), Logistic.kr(control5 / 2 + 3.5, 100, Rand(0, 1)) * BufFrames.kr(localBuf), 1, 0.333, 0.5) + local * amp;

					LocalOut.ar(DelayC.ar(effet, 4, control6, control7));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagFreeze",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf;
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(Mix(In.ar(in, 2)) * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0.333, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = PlayBuf.ar(1, localBuf, (control2 * 2).clip(0.0625, 2.0), 1.0, control3 * BufFrames.kr(localBuf), 1);
					effet = FFT(LocalBuf(1024, 1), effet);
					effet = PV_MagFreeze(effet, SinOsc.kr(control2 * control4.reciprocal));
					effet= IFFT(effet);

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_PlayBuf",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf;
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(Mix(In.ar(in, 2)) * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0.333, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = PlayBuf.ar(1, localBuf, (control1 * 2).clip(0.0625, 2.0), 1.0, 0, 1);
					effet = FFT(LocalBuf(512, 1), effet);
					effet = PV_PlayBuf(effet, localBuf, control2, control3 * BufFrames.kr(localBuf), 1, 1);
					effet= IFFT(effet);

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_BinPlayBuf",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf;
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(Mix(In.ar(in, 2)) * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0.333, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = PlayBuf.ar(1, localBuf, (control1 * 2).clip(0.0625, 2.0), 1.0, 0, 1);
					effet = FFT(LocalBuf(1024, 1), effet);
					effet = PV_BinPlayBuf(effet, localBuf, control2, control6 * BufFrames.kr(localBuf), control3 * 16, control4 * 8 + 1, control5 * 63 + 1, 1, 1);
					effet= IFFT(effet);

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_HPshiftDownFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_HPshiftDown(effet, control1*32);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_HPfiltreFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_HPfiltre(effet, control1 * 32 + 1, control2 * 32 + 1);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagNoiseFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagNoise(effet);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagClipFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagClip(effet, control1 * 16);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagSmoothFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagSmooth(effet, control1);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagSmearFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagSmear(effet, control1*64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_DiffuserFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_Diffuser(effet, Trig1.kr(LFNoise2.kr(control1*100), (control2*100).reciprocal));
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_BrickWallFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_BrickWall(effet, control1 * 2 - 1);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_LocalMaxFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_LocalMax(effet, control1*64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagSquaredFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagSquared(effet);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagBelowFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagBelow(effet, control1*64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagAboveFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagAbove(effet, control1*64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_RandCombFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_RandComb(effet, control1*64, LFNoise2.kr(control2 * 64));
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagShiftFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagShift(effet, control1 * 4, control2 * 128 - 64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_BinScrambleFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_BinScramble(effet, control1, control2, LFNoise2.kr(control2.reciprocal));
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_BinShiftFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_BinShift(effet, control1 * 4, control2 * 256 - 64);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_RectCombFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_RectComb(effet, control1*32, control2, control3);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_ConformalMapFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_ConformalMap(effet, control1 * 2 - 1, control2 * 2 -1);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_CompanderFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_Compander(effet, control1 * 64, control2 * 10, control3 * 10);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_SpectralEnhanceFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagShift(effet, (control1 * 4).clip(0.25, 4));
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagShift+StretchFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagShift(effet, (control1 * 4).clip(0.25, 4), control2 - 0.5 * 128);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_CutoffFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_Cutoff(effet, control1 * 2 - 1);
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_MagStretchFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_MagShift(effet, (control1 * 4).clip(0.25, 4));
					effet = IFFT(effet);
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("ConvolutionFX",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, buffer, trig, ambisonic;
					// Input
					ineffet=Mix(In.ar(in, 2));
					buffer = LocalBuf(s.sampleRate, 1).clear;
					trig = Dust.kr(control1 * 64 + 0.01);
					RecordBuf.ar(ineffet, buffer, Saw.kr(control2 * 16 + 0.01).abs, 1, 0, trigger: trig);
					// effet
					effet=Mix(Convolution2L.ar(ineffet, buffer, trig * control3, 1024));
					effet = effet * amp;

					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FXonFly",
				{arg out = 0, in, busverb, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp=0.0;
					var ineffet, effet, ambisonic, localBuf, buffer, local,rate;
					ineffet=Mix(In.ar(in, 2));
					localBuf = LocalBuf(s.sampleRate, 1);
					buffer = RecordBuf.ar(ineffet, localBuf, 0, 1, 0);
					local = LocalIn.ar(1);
					rate = control1*4;
					// effet
					effet = PlayBuf.ar(1, localBuf, LFNoise2.kr(control2)+(BufRateScale.kr(buffer) * rate), Dust.kr(control3), Logistic.kr(control4/2+3.5, 100, Rand(0, 1))* BufFrames.kr(localBuf), 1, 0.05, 0.1) + local * amp / 2;
					effet = Mix(effet) * amp;

					LocalOut.ar(DelayC.ar(effet, 4, control5/1000, control6));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(busverb, Mix(effet));
					Out.ar(out, effet);
			}).send(s);

			/////////////////////////////////////
			// Reverb
			/////////////////////////////////////

			SynthDef("AllpassC",
				{arg out=0, in, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp = (-3).dbamp;
					var inverb, verb, ambisonic;
					// Input
					inverb=Mix(In.ar(in, 1));
					// verb
					verb=Mix(AllpassC.ar(inverb, 0.2, [control1,control2/2,control3/3,control4/4], [control5, control6, control7, control8]*30));

					// Switch Audio Out
					verb = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(verb, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, verb, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(verb, verb, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(verb, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, verb * amp);
			}).send(s);

			SynthDef("FreeVerb",
				{arg out=0, in, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp = (-3).dbamp;
					var inverb, verb, ambisonic;
					// Input
					inverb=Mix(In.ar(in, 1));
					// verb
					verb = FreeVerb.ar(inverb, control1, control2, control3);

					// Switch Audio Out
					verb = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(verb, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, verb, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(verb, verb, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(verb, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, verb * amp);
			}).send(s);

			SynthDef("GVerb",
				{arg out=0, in, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp = (-3).dbamp;
					var inverb, verb, ambisonic, left, right;
					// Input
					inverb=Mix(In.ar(in, 1));
					// verb
					#left, right = GVerb.ar(inverb, (control1*300).clip(1, 300), (control5*100).clip(0.01, 100), control6, control7, 15, control2, control3, control4, 300);
					verb = Mix(left,right);

					// Switch Audio Out
					verb = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(verb, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, verb, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(verb, verb, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(verb, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, verb * amp);
			}).send(s);

			SynthDef("JPverb",
				{arg out=0, in, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp = (-3).dbamp;
					var inverb, verb, ambisonic, left, right;
					// Input
					inverb=Mix(In.ar(in, 1));
					// verb
					verb = Mix(JPverb.ar(inverb, control1 * 60, control2, control3 * 5, control4, control5, control6, control7 *5900 + 100, control8 * 9000 + 1000));

					// Switch Audio Out
					verb = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(verb, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, verb, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(verb, verb, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(verb, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, verb * amp);
			}).send(s);

			SynthDef("SpinReverb",
				{arg out=0, in, control1=0.03125, control2=0.0625, control3=0.125, control4=0.25, control5=0.25, control6=0.25, control7=0.25, control8=0.25, pan=0, amp = (-3).dbamp;
					var inverb, verb, ambisonic, left, right;
					// Input
					inverb=Mix(In.ar(in, 1));
					// verb
					#left, right = GVerb.ar(inverb, 300 - (control1*300), control5*100, control6, control7, 15, control2, control3, control4, 300);
					verb = Mix(left,right);

					pan = LFSaw.kr(control8, mul: pan.sign);
					// Switch Audio Out
					verb = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(verb, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, verb, pan, 1, ~widthMC, ~orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(verb, verb, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(verb, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, verb * amp);
			}).send(s);


		},{"SynthDef Init Cancelled".postln});
	}
}
