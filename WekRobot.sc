// A Software by Herve Provini

WekRobot {

	classvar < s, sender, mfccData, flagStreamMFCC, numPreset, lastNumPreset, menuWek, lastTimeWekPreset, timeWekPreset, listeWekPreset, flagWTP;

	var keyboardShortCut, keyboardTranslate, keyboardTranslateBefore, setupKeyboardShortCut, keyboard, keyVolume, windowKeyboard, keyboardVolume, fonctionShortCut, windowVST, flagVST, flagMC=0, widthMC=2.0, orientationMC=0.5, numberAudioIn;

	*new {arg path="~/Documents/WekRobot/", ni=26, o=2, r=2, f=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0, name="WekRobot", wek=6448, wekPort=57120, scPort=57110;

		^super.new.init(name, path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, wek, wekPort, scPort);

	}

	init {arg name, path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, wek, wekPort, scPort;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		~nompathdata=PathName.new(path).pathOnly;

		// Verify path
		if(File.exists(~nompathdata).not) {systemCmd("mkdir" + ~nompathdata)};
		if(File.exists(thisProcess.platform.recordingsDir).not) {systemCmd("mkdir" + thisProcess.platform.recordingsDir.quote)};

		//Server.default = s = Server(name,NetAddr("localhost", scPort.asInteger), Server.default.options);
		thisProcess.openUDPPort(wekPort.asInteger); // Sender Port Wekinator + Enter Port change 6448 to an another for example 6449

		s = Server.default;
		s.options.memSize = 2**20;
		s.options.inDevice_(devIn);
		s.options.outDevice_(devOut);
		numberAudioIn = ni;
		s.options.numInputBusChannels_(numberAudioIn);
		s.options.numOutputBusChannels_(o);
		s.options.hardwareBufferSize_(size);
		~numberAudioOut=o;
		~recChannels = r;
		~flagRecording = 'off';
		s.recChannels_(~recChannels);
		~headerFormat = "aiff";
		s.recHeaderFormat_(~headerFormat);
		~sampleFormat = "float";
		s.recSampleFormat_(~sampleFormat);
		~startChannelAudioOut = 0;
		~switchAudioOut = f;
		flagMC = flag;
		widthMC = wid;
		orientationMC = ori;

		thisProcess.openUDPPort(NetAddr.langPort);

		Safety(s);
		//s.makeGui;

		// Open Wekinator
		sender.free;
		sender = NetAddr.new("127.0.0.1", wek);// Wekinator
		Pipe.new("open -a Wekinator", "r").close;

		~samplePourAnalyse = Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff";
		~listeSamplePourAnalyse = [];
		~listeNameSamplePourAnalyse = [];

		// MIDI
		MIDIClient.init;
		// Connect first device by default
		MIDIIn.connect(0, 0);
		~midiOut = MIDIOut(0).latency = 0.01;
		~midiOut.allNotesOff(0);

		~helpWekRobot="
Single Commandes:

esc	or SpaveBar					System on/off
alt + b							Write active buffer sound (Active Window Instrument)
alt + d							De-Synchro duration all Instr
e								Switch Algo Analyse
F				 				Loop sound file analyser on
alt + f							Loop sound file analyse off
ctrl + f		 				Load and Add sound file analyse
g								Load new synth, sound, path for data environment
G								Load new sound environment
alt + g							Load new synth environment
j								New Synchro (nextBar)
alt + l / alt + L				Load instrument multi / single
alt + q/ alt + Q				Load sequences / sequence
alt + ctrl + q / alt + ctrl + Q	Save sequences / sequence
alt + r							Start Recording
alt + shift + r					Pause Recording on/off
ctrl + alt + r					Stop Recording
alt + s / alt + S				Save instrument multi / single
alt + v							Clear Control Panel
alt + V							Reset FX (After Audio Change)
alt + w / ctrl + alt + w		Windows next / previous
W / alt + W						Display control panel / scores
y / ctrl + y					InstrumentWork next / previous

Commandes follow by a numerical key:

(0,..9 ; shift 0,..9 ; alt 0,..9 ; alt + shift 0,..9)
- all instruments ; _ all part or seq

b								Write buffer sound
alt + b							Write buffer sound (Active Window Instrument)
d								De-Synchro duration instr
f								Switch Sound File for Analyze
h / ctrl + h					Score recording on / off
i / ctrl + i					Fhz Amp Duration input next /previous
alt + i							Fhz Amp Duration input off
k / ctrl + k					Score play / stop
l / L							Load instrument multi / single
ctrl + l / L					Load instrument multi / single (nextBar)
m / alt + m						Freeze buffer on / off
M / alt + M						Freeze buffers on / off
o / ctrl + o					Fhz Amp Duration output next /previous
alt + o							Fhz Amp Duration output off
p / ctrl + p					Instrument play/ stop
alt + p							Instrument play (nextBar)
shift + p						Play sample synchro (Tempo)
alt + shift + p					Play sample synchro (Pitch)
q / Q							Load sequences / sequence
ctrl + q / ctrl + Q				Save sequences / sequence
s / S							Save multi / save single (instruments)
t								Copy (Active Window Instrument)
u / ctrl + u					Score load / save
v / V							Clear multi / single (instruments)
w								Display instrument
z								Set sample (Tempo)
alt + z							Set sample (Pitch)

//////////////////////////////////////////////////////////////////////////////////////////

ShortCut for Keyboard Panel:

<						Keyboard Transpose down.
>						Keyboard Transpose up.

ysxdcvgbhnjm,l.e-		Musical Keys.
";

		// Custom menu for WekRobot

		~menuFile = Menu(
			MenuAction("Load",
				{Dialog.openPanel({ arg paths;
					~samplePourAnalyse=paths;
					s.bind{
						~listeSamplePourAnalyse.do({arg buffer; buffer.free});
						~listeSamplePourAnalyse = [];
						~listeNameSamplePourAnalyse = [];
						~bufferanalysefile.free;
						~synthPlayFile.set(\trig, 0);
						s.sync;
						~synthPlayFile.run(false);
						s.sync;
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
								~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2);
								s.sync;
							},
							{Post << "Loading sound for analyze" << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1]);
								s.sync;
						});
						~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
						~file.close;
						s.sync;
						~synthPlayFile.set(\bufferplay, ~bufferanalysefile.bufnum);
						s.sync;
						if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File IN'}, {~synthPlayFile.run(true);
							s.sync;~synthPlayFile.set('trig', 1);s.sync});
			}},{"cancelled".postln})}),
			MenuAction("Loop On", {~synthPlayFile.set('loop', 1)}),
			MenuAction("Loop Off", {~synthPlayFile.set('loop', 0)}),
			MenuAction("Restart for Analyze on Grid Tempo", {s.bind{
				~synthPlayFile.set('trig', 0);
				s.sync;
				~synthPlayFile.run(false);
				s.sync;
				~tempoSystem.schedAbs(~tempoSystem.nextBar, {~synthPlayFile.run(true);~synthPlayFile.set('trig', 1);nil});
				s.sync;
			}});
		);
		MainMenu.register(~menuFile.title_("File for Analyze"), "WekRobotTools");


		~menuSynchro = MenuAction("Synchro on Grid Tempo", {
			if(~startsysteme.value == 1, {
				~nombreinstrument.do({arg i;
					if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
						~tempoSystem.schedAbs(~tempoSystem.nextBar, {
							~routineinstrument.wrapAt(i).stop;
							// Set MIDI Off
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
								~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
								});
							});
							~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
							~routineinstrument.wrapAt(i).play(quant: Quant.new(1));
							~routineMetronome.play(quant: Quant.new(1)); nil});
				})});
			});
		});
		MainMenu.register(~menuSynchro, "WekRobotTools");

		~menuRec = Menu(

			MenuAction("Start", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
				~fonctionRecOn.value;
			}),
			MenuAction("Stop", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Off")});//  Send Synchro Rec Off
				~fonctionRecOff.value;
			}),
			MenuAction("Pause", {
				if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});//  Send Synchro Rec Pause
				~fonctionRecPause.value;
			});
		);
		MainMenu.register(~menuRec.title_("Recording"), "WekRobotTools");

		~menuBuf = MenuAction("Save Buffer Sound (Active Window Instrument)", {~listebuffer.wrapAt(~soncontrol.wrapAt(~instrumentactuel).value).write(~nompathdata++"son"+(~soncontrol.wrapAt(~instrumentactuel).value+1).asString++".aiff")});
		MainMenu.register(~menuBuf, "WekRobotTools");

		~menuAudio = Menu(
			MenuAction.new("Stereo", {~switchAudioOut=0; this.initSynthDef}),
			MenuAction.new("MultiSpeaker", {~switchAudioOut=2; this.initSynthDef}),
			MenuAction.new("Rotate2", {~switchAudioOut=1; this.initSynthDef}),
			MenuAction.new("Ambisonic", {~switchAudioOut=3; this.initSynthDef});
		);
		MainMenu.register(~menuAudio.title_("Audio"), "WekRobotTools");

		~menuFX  =MenuAction("Reset FX (After Audio Change)",{
			~saveloaddatasinstr.valueAction=12;
			s.bind{
				~fonctionResetFX.value;
				s.sync;
				this.initSynthDef;
				s.bind;
			};
			s.queryAllNodes;
		});
		MainMenu.register(~menuFX, "WekRobotTools");



		~menuMidi = Menu(
			MenuAction("Init", {
				MIDIClient.init;
				if(MIDIClient.externalSources != [ ], {
					// Connect first device by default
					MIDIIn.connect(0, 0);
					~midiOut = MIDIOut(0);
					//midiOut.connect(0);
					16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
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
						16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})});
					});
				});
			).title_("Setting"),
			Menu(
				MenuAction("On", {~flagMidiOut = 'on'}),
				MenuAction("Off", {~flagMidiOut = 'off'});
			).title_("MIDI Out");
		);
		MainMenu.register(~menuMidi.title_("MIDI"), "WekRobotTools");

		menuWek = Menu(
			MenuAction("Wek In Port",
				{
					SCRequestString("6448", "Wek In Port", {arg index, port;
						port = index.asInteger;
						sender.free;
						sender = NetAddr.new("127.0.0.1", port);// Wekinator
					});
			}),
			MenuAction("Wek Send Port",
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
		MainMenu.register(menuWek.title_("Wekinator"), "WekRobotTools");

		~menuShortCut = MenuAction("ShortCuts", {
			//Document.new("ShortCuts for WekRobot", ~helpWekRobot);
			TextView().name_("ShortCuts for WekRobot").string_(~helpWekRobot).front;
		});
		MainMenu.register(~menuShortCut, "WekRobotTools");


		~fonctionResetFX={
			// Free FX
			~nombreinstrument.do({arg i;
				~geffet.at(i).deepFree;
				~geffet.at(i).free;
				~buseffetsPresynth.at(i).free;
				~buseffetsPostsynth.at(i).free;
			});
			// Reset variables FX
			~geffet=[];// groupe des effets
			~buseffetsPresynth=[];// liste des bus effetsPres
			~buseffetsPostsynth=[];// liste des bus audio effetsPost
			~listeeffetsPresynth=[];// liste des effetsPres
			~listeeffetsPostsynth=[];// liste des effetsPost
			~listenodeeffetsPresynth=[];// liste des nodes des effetsPres
			~listenodeeffetsPostsynth=[];// liste des nodes des effetsPost
			// ReAlloc FX
			~nombreinstrument.do({arg i;var x,y,z,w,n1,n2,g;
				~geffet=~geffet.add(Group.new(~gsynth.at(i), \addAfter));// group des effets
				~buseffetsPresynth=~buseffetsPresynth.add(Bus.audio(s, 2));
				~buseffetsPostsynth=~buseffetsPostsynth.add(Bus.audio(s, 2));
				~listenodeeffetsPresynth=~listenodeeffetsPresynth.add(0);
				~listenodeeffetsPostsynth=~listenodeeffetsPostsynth.add(0);
				~lasteffetsPreindex=~lasteffetsPreindex.add(0);
				~lasteffetsPostindex=~lasteffetsPostindex.add(0);
				~dataseffetsPres=[];~fxPre.size.do({arg i;var datas;datas=[0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5];~dataseffetsPres=~dataseffetsPres.add(datas)});
				~synthcontrolvieweffetsPredatas=~synthcontrolvieweffetsPredatas.add(~dataseffetsPres);
				~datasonoff=[];~fxPre.size.do({arg i;var datas=0;~datasonoff=~datasonoff.add(datas)});
				~synthonoffeffetsPredatas=~synthonoffeffetsPredatas.add(~datasonoff);
				~dataspaneffetsPres=[];~fxPre.size.do({arg i;var datas=0;~dataspaneffetsPres=~dataspaneffetsPres.add(datas)});
				~synthpaneffetsPrestepdatas=~synthpaneffetsPrestepdatas.add(~dataspaneffetsPres);
				~datasampeffetsPres=[];x=[];
				~fxPre.size.do({arg i;var datas;datas=(-12);~datasampeffetsPres=~datasampeffetsPres.add(datas);
					x=x.add(0)});
				~synthampeffetsPrestepdatas=~synthampeffetsPrestepdatas.add(~datasampeffetsPres);
				~randomValueEffetPre=~randomValueEffetPre.add(x);
				~randomValueEffetPanPre=~randomValueEffetPanPre.add(x.copy);
				~dataseffetsPosts=[];x=[];
				~fxPost.size.do({arg i;var datas;datas=[0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5];~dataseffetsPosts=~dataseffetsPosts.add(datas);
					x=x.add(0)});
				~synthcontrolvieweffetsPostdatas=~synthcontrolvieweffetsPostdatas.add(~dataseffetsPosts);
				~randomValueEffetPost=~randomValueEffetPost.add(x);
				~randomValueEffetPanPost=~randomValueEffetPanPost.add(x.copy);
				~datasonoff=[];~fxPost.size.do({arg i;var datas=0;~datasonoff=~datasonoff.add(datas)});
				~synthonoffeffetsPostdatas=~synthonoffeffetsPostdatas.add(~datasonoff);
				~dataspaneffetsPosts=[];~fxPost.size.do({arg i;var datas=0;~dataspaneffetsPosts=~dataspaneffetsPosts.add(datas)});
				~synthpaneffetsPoststepdatas=~synthpaneffetsPoststepdatas.add(~dataspaneffetsPosts);
				~datasampeffetsPosts=[];~fxPost.size.do({arg i;var datas;datas=(-12);~datasampeffetsPosts=~datasampeffetsPosts.add(datas)});
				~synthampeffetsPoststepdatas=~synthampeffetsPoststepdatas.add(~datasampeffetsPosts);
				~listeeffetsPresynth=~listeeffetsPresynth.add([]);
				~listeeffetsPostsynth=~listeeffetsPostsynth.add([]);
				// Cr�e les effetsPres  � choix pour les synth (� maper ensuite pour les activer avec GUI)
				~nombreEffetsPre.do({arg ii;
					~listeeffetsPresynth.wrapPut(i,~listeeffetsPresynth.wrapAt(i).add(Synth.newPaused(~fxPre.wrapAt(ii),['out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut,'in',~buseffetsPresynth.wrapAt(i).index,'buseffetsPost',~buseffetsPostsynth.wrapAt(i).index],~geffet.wrapAt(i),\addToTail)));
				});
				s.sync;
				~nombreEffetsPost.do({arg ii;
					~listeeffetsPostsynth.wrapPut(i,~listeeffetsPostsynth.wrapAt(i).add(Synth.newPaused(~fxPost.wrapAt(ii),['out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut,'in',~buseffetsPostsynth.wrapAt(i).index],~geffet.wrapAt(i),\addToTail)));
					s.sync;
				});
			});
		};

		~fonctionRecOn={
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
			if(~flagRecording == 'off', {
				~flagRecording = 'on';
				s.bind{
					s.recChannels_(~recChannels);
					s.sync;
					s.recHeaderFormat_(~headerFormat);
					s.sync;
					s.recSampleFormat_(~sampleFormat);
					s.sync;
					s.prepareForRecord("~/Music/SuperCollider Recordings/".standardizePath ++ "WekRobot_" ++ Date.localtime.stamp ++ "." ++ ~headerFormat);
					s.sync;
					s.record;
					s.sync;
				};
			});
		};

		~fonctionRecOff={
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Off")});// Send Synchro Rec On
			~flagRecording = 'off';
			s.stopRecording;
		};

		~fonctionRecPause={
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec Pause")});// Send Synchro Rec On
			if(~startsysteme.value == 1, {
				if(~flagRecording == 'on', {s.pauseRecording; ~flagRecording = 'pause'},{s.record; ~flagRecording = 'on'});
			});
		};

		~switchMenuAudioOut.value;

		s.waitForBoot({

			// Init Synth
			this.initSynthDef(true);

			/////////////////////////// Datas users ///////////////////

			// DATAS GENERALES
			~midicanalin=["Midi In 1","Midi In 2","Midi In 3","Midi In 4","Midi In 5","Midi In 6","Midi In 7","Midi In 8","Midi In 9","Midi In 10","Midi In 11","Midi In 12","Midi In 13","Midi In 14","Midi In 15","Midi In 16"];
			~choixdatasinfreq=["Fhz In Off","Fhz In Audio","Fhz In Midi","Fhz In Chaos","Fhz In Neural","Fhz In Genetic","Fhz In CreatePhrase1","Fhz In CreatePhrase2"];
			~choixdatasinamp=["Amp In Off","Amp In Audio","Amp In Midi","Amp In Chaos","Amp In Neural","Amp In Genetic","Amp In CreatePhrase1","Amp In CreatePhrase2"];
			~choixdatasinduree=["Dur In Off","Dur In Audio","Dur In Midi","Dur In Chaos","Dur In Neural","Dur In Genetic","Dur In CreatePhrase1","Dur In CreatePhrase2"];
			~algorithmesmusicauxfreq=["Fhz Out","Fhz Out Neurone","Fhz Out  Neurone2","Fhz Out Genetic","Fhz Out Probability","Fhz Out CreatePhrase1","Fhz Out CreatePhrase2"];
			~algorithmesmusicauxamp=["Amp Out","Amp Out Neurone","Amp Out  Neurone2","Amp Out Genetic","Amp Out Probability","Amp Out CreatePhrase1","Amp Out CreatePhrase2"];
			~algorithmesmusicauxduree=["Dur Out","Dur Out Neurone","Dur Out  Neurone2","Dur Out Genetic","Dur Out Probability","Dur Out CreatePhrase1","Dur Out CreatePhrase2"];
			~canalout=[];
			32.do({arg i;var x;
				i=i+1;x="Audio Out"+i.asString;~canalout=~canalout.add(x.asSymbol);
			});
			~instrumentsOSchoiceControl=[
				"User Operating System",
				"Load Instrument",
				"Load Instruments",
				"Save Instrument",
				"Save Instruments",
				"Load Sequence",
				"Load Sequences",
				"Save Sequence",
				"Save Sequences",
				"Load Scores",
				"Save Scores",
				"Clear Instrument",
				"Clear Instruments",
				"Clear Control Panel",
				"Freeze Buffer On",
				"Freeze Buffer Off",
				"Freeze Buffers On",
				"Freeze Buffers Off",
				"Load File for Analyse",
				"New Synth Environment",
				"New Sound Environment",
				"New Synth, Sound, Path for Data Environment",
				"OSC Off",
				"OSC Master",
				"OSC Slave",
				"OSC Setting",
			];
			~instrumentsOSchoiceInstrument=[
				"User Operating System",
				"Load Instrument",
				"Load Instruments",
				"Save Instrument",
				"Save Instruments",
				"Load Sequence",
				"Load Sequences",
				"Save Sequence",
				"Save Sequences",
				"Load Scores",
				"Save Scores",
				"Clear Instrument",
				"Clear Instruments",
				"Clear Control Panel",
				"Freeze Buffer On",
				"Freeze Buffer Off",
				"Freeze Buffers On",
				"Freeze Buffers Off",
				"Load File for Analyse",
				"New Synth Environment",
				"New Sound Environment",
				"New Synth, Sound, Path for Data Environment",
				"OSC Off",
				"OSC Master",
				"OSC Slave",
				"OSC Setting",
			];

			~singleinstrument=["Load Single"];~multiinstruments=["Load Multi"];~sequenceliste=["Load Sequences"];
			40.do({arg i, f;i=i+1;f="instrument " ++ i.asString;~singleinstrument=~singleinstrument.add(f);
				f="instruments"+i.asString;~multiinstruments=~multiinstruments.add(f);
				f="sequence" + i.asString;~sequenceliste=~sequenceliste.add(f);
				f="sequences"+i.asString;~sequenceliste=~sequenceliste.add(f)});
			~nombreinstrument=6; // NOMBRE INSTRUMENT TOTAL
			~listeinstruments=[];
			~nombreinstrument.do({arg i;~listeinstruments=~listeinstruments.add("InstrumentWork"+(i+1).asString)});
			~listewindow=[];
			~nombreBeatsBare=1; // Nombre de beats par mesure
			~tempoSystem = TempoClock.default;
			~tempoSystem.schedAbs(~tempoSystem.nextBar, {~tempoSystem.beatsPerBar_(~nombreBeatsBare);nil});
			~tempochaos=16;
			~temponeurone=64;
			~tempogenetique=16;
			~tempoautomation=24;// 1/24 de seconde pour automation des controls instruments
			~tempoalgorithmes=16;// tempo des autres algorithmes....
			~tempopartitions=100;// tempo des partitions....
			~paraAlgoAnalyseAudio=[[0.5, 0.5], [0.5, 0.5], [0.5, 0.5], [0.5, 0.5]];
			~instrumentactuel=0;
			~differencefreq=[];
			~differenceamp=[];
			~differenceduree=[];
			~dureeaccord=[];
			~maxaccord=[];
			~listedatassize=[];
			~dureeanalysemax=[];
			~dureeanalysesil=[];
			~canalmidiin=[];// canal  pour datas midi in
			~canalaudioout=[];// canal sortie audio out
			~listemidifreq=[];
			~listemidiamp=[];
			~listemididuree=[];
			~listechaosfreq=[];
			~listechaosamp=[];
			~listechaosduree=[];
			~listeneuronefreq=[];
			~listeneuroneamp=[];
			~listeneuroneduree=[];
			~listegenetiquefreq=[];
			~listegenetiqueamp=[];
			~listegenetiqueduree=[];
			~listealgo1freq=[];
			~listealgo1amp=[];
			~listealgo1duree=[];
			~listealgo2freq=[];
			~listealgo2amp=[];
			~listealgo2duree=[];
			~listeinfreq=[];
			~listeinamp=[];
			~listeinduree=[];
			~listeoutneuronefreq=[];
			~listeoutneuroneamp=[];
			~listeoutneuroneduree=[];
			~instanceNeurones=[];
			~instanceGenetiques=[];
			~instanceAlgo1=[];
			~instanceAlgo2=[];
			~instanceChaosF=[];
			~instanceChaosA=[];
			~instanceChaosD=[];
			~flagFreqAlgo=[];
			~flagAmpAlgo=[];
			~flagDureeAlgo=[];
			~flagneuronefreq=[];
			~flagneuroneamp=[];
			~flagneuroneduree=[];
			~flagoutneuronefreq=[];
			~flagoutneuroneamp=[];
			~flagoutneuroneduree=[];
			~flaggenetiquefreq=[];
			~flaggenetiqueamp=[];
			~flaggenetiqueduree=[];
			~neuronemode=[];
			~neuroneapprentissage=[];
			~neuronetemperature=[];
			~genetiquemode=[];
			~genetiquecroisement=[];
			~genetiquemutation=[];
			~differenceparents=[];
			~wavetable=[];
			~flagalgoinstrneuronefreq=[];
			~flagalgoinstrneuroneamp=[];
			~flagalgoinstrneuroneduree=[];
			~busfreqsynth=[];//  liste des bus control freq
			~busfreqRatesynth=[];//  liste des bus control freqRate
			~busampsynth=[];// liste des bus control amp
			~busoffsetplaysampler=[];// offset des samples
			~buscontrolsynth=[];// bus des parametres synth
			~loopplaysamplerdatas=[];
			~buspansynthLo=[];
			~buspansynthHi=[];
			~numFhzBand = [];
			~rangeSynthBand = [];
			~flagBandSynth = [];
			~flagSynthBand = [];
			~bandFHZ = [];
			~lastTimeBand = [];
			~tuning = [];
			~tuningIndex = [];
			~degrees = [];
			~root = [];
			~scale = [];
			~flagScaling = [];
			~flagByPassSynth = [];

			~routineinstrument=[];// liste des instruments � jouer par les scheduler
			~levelenvsynth=[];// liste level envelope instruments
			~timeenvsynth=[];// liste time envelope instruments

			// Datas musicales � jouer par les instruments
			~playinstrument=[];// on off playing instruments
			~flagfreq=[];
			~flagamp=[];
			~flagduree=[];
			~freq=[];
			~amp=[];
			~ampPre=[];
			~ampPost=[];
			~duree=[];
			~listeaudiofreq=[];
			~listeaudioamp=[];
			~listeaudioduree=[];
			~listeofffreq=[];
			~listeoffamp=[];
			~listeoffduree=[];
			~indatasfreqinstrument=[];
			~indatasampinstrument=[];
			~indatasdureeinstrument=[];
			~algoinstrumentfreq=[];
			~algoinstrumentamp=[];
			~algoinstrumentduree=[];
			// GUI number box pour affichage de datas SliderRange
			~windowactive='control panel';
			~sequencesbutton=[];
			~startbutton=[];
			~soncontrol=[];
			~soncontrolfft=[];
			~sonout=[];
			~synthcontrol=[];
			~loopbutton=[];
			~reversebutton=[];
			~sampleroffsetcontrol=[];
			~sampleroffsetcontrolValue=[];
			~recsamplebutton=[];
			~looprecsamplebutton=[];
			~recsamplebuttondatas=[];
			~looprecsamplebuttondatas=[];
			~synthfreqrange=[];
			~synthfreqstep=[];
			~synthamprange=[];
			~sendFXPre=[];
			~sendFXPost=[];
			~synthsliderenvelope=[];
			~syntheffetsPrecontrol=[];
			~synthonoffeffetsPre=[];
			~synthonoffeffetsPredatas=[];
			~synthpaneffetsPrestep=[];
			~synthpaneffetsPrestepdatas=[];
			~synthampeffetsPrestep=[];
			~synthampeffetsPrestepdatas=[];
			~synthcontrolvieweffetsPre=[];
			~synthcontrolvieweffetsPredatas=[];
			~syntheffetsPostcontrol=[];
			~synthonoffeffetsPost=[];
			~synthonoffeffetsPostdatas=[];
			~synthpaneffetsPoststep=[];
			~synthpaneffetsPoststepdatas=[];
			~synthampeffetsPoststep=[];
			~synthampeffetsPoststepdatas=[];
			~synthcontrolvieweffetsPost=[];
			~synthcontrolvieweffetsPostdatas=[];
			~dureerange=[];
			~dureestep=[];
			~dureeMul=[];
			~synthpancontrol=[];
			~synthpancontrolValue=[];
			~reversesampledatas=[];
			~synthcontrolviewparametres=[];
			~synthcontrolviewparametresdatas=[];
			~synthcontrolviewlevels=[];
			~synthcontrolviewlevelsdatas=[];
			~canalmidiinview=[];
			~choixdatasinfreqview=[];
			~choixdatasinampview=[];
			~choixdatasindureeview=[];
			~choixalgoviewfreq=[];
			~choixalgoviewamp=[];
			~choixalgoviewduree=[];
			~quantizationview=[];
			~quantization=[];
			~kchaosviewfreq=[];
			~kchaosviewamp=[];
			~kchaosviewduree=[];
			~neuroneviewapprentissage=[];
			~neuroneviewtemperature=[];
			~neuroneviewmode=[];
			~genetiqueviewcroisement=[];
			~genetiqueviewmutation=[];
			~genetiqueviewmode=[];
			~datasrecbutton=[];
			~saveloaddatasinstrument=[];
			~loadsingleinstrument=[];
			~loadmultiinstruments=[];
			~datassaveloadinstrument=[];
			~initneurones=[];
			~initpatternneurones=[];
			~initgenetiques=[];
			~datasinstruments=[];
			~datassizeinstrument=[];
			~differencefreqinstrument=[];
			~differenceampinstrument=[];
			~differencedureeinstrument=[];
			~dureeaccordinstrument=[];
			~dureeanalysemaxinstrument=[];
			~dureeanalysesilence=[];
			~synthparametresnumber=[];
			~lastcontrolindex=[];
			~synthenvelopenumberx=[];
			~synthenvelopenumbery=[];
			~lastenvelopevalue=[];
			~lastenvelopeindex=[];
			~algoparametresnumberfreq=[];
			~lastcontrolalgoindexfreq=[];
			~algoparametresnumberamp=[];
			~lastcontrolalgoindexamp=[];
			~algoparametresnumberduree=[];
			~lastcontrolalgoindexduree=[];
			~syntheffetsPrenumber=[];
			~lasteffetsPreindex=[];
			~syntheffetsPostnumber=[];
			~lasteffetsPostindex=[];
			~maxaccordinstrument=[];
			~automationinstrument=[];
			~automationeffetpre=[];
			~automationeffetpost=[];
			~automationeffetpanpre=[];
			~automationeffetpanpost=[];
			~automationparametres=[];
			~randomValueSynth=[];
			~randomValueEffetPre=[];
			~randomValueEffetPost=[];
			~randomValueEffetPanPre=[];
			~randomValueEffetPanPost=[];
			~randomValueParametreSynth=[];
			~amplitudegeneraleinstrument=0;
			~recpartbutton=[];
			~playpartbutton=[];
			~looppartbutton=[];
			~flagrecpart=[];
			~flagplaypart=[];
			~flaglooppart=[];
			~dureerecpart=[];
			~dureeplaypart=[];
			~partitionsbutton=[];
			~midiButton=[];
			~numberBand = [];
			~synthBand0 = [];
			~synthBand1 = [];
			~synthBand2 = [];
			~synthBand3 = [];
			~synthBand4 = [];
			~synthBand5 = [];
			~synthBand6 = [];
			~synthBand7 = [];
			~synthBand8 = [];
			~synthBand9 = [];
			~synthBand10 = [];
			~synthBand11 = [];
			~synthBand12 = [];
			~buttonSynthBand = [];
			~byPassSynth = [];
			~listTuning = [];
			~rootChoice = [];
			~displayDegrees = [];
			~partitionsliste=["Load Scores"];
			~listeplaypart=[];
			~listerecpart=[];
			~pointeurplaypart=[];
			~freqMidi=[];
			~canalMidiOutInstr=[];
			~displayRecLevel = [];
			~numRecLevel1 = [];
			~numRecLevel2 = [];
			40.do({arg i, f;~flagrecpart=~flagrecpart.add(0);~flagplaypart=~flagplaypart.add(0);~flaglooppart=~flaglooppart.add(0);~dureerecpart=~dureerecpart.add(0);~dureeplaypart=~dureeplaypart.add(0);~pointeurplaypart=~pointeurplaypart.add(0);
				i=i+1;f="score " ++ i.asString;~partitionsliste=~partitionsliste.add(f);
				~listeplaypart=~listeplaypart.add([]);
				~listerecpart=~listerecpart.add([]);
			});
			~flagSynchro='off';
			~flagTempoAnalyze='off';
			~ffttext=[];
			~flagMidiOut = 'off';
			~canalAudioInInstr = [];
			~setAudioInstr = [];
			flagVST = 'off';
			flagStreamMFCC = 'off';
			numPreset = 0;
			lastNumPreset = 0;
			timeWekPreset = 4;
			flagWTP = 'on';
			40.do({arg i; listeWekPreset = listeWekPreset.add(i+1)});

			// Keyboard
			keyboardTranslateBefore = 0;
			keyVolume = 12.neg.dbamp;

			~flagAlgorithm = 0;

			// run the soft
			this.run;

		});

	}

	run {

		// OSCFunc Score
		OSCFunc.newMatching({arg msg, time, addr, recvPort;

			var array, cmd = 'on', number, file, item = 0;

			msg.removeAt(0);
			msg.postcs;

			while({cmd != nil},
				{
					cmd = msg[item].postln;
					if(cmd == 'all' or: {cmd == 'wekrobot'},
						{
							cmd = msg[item+1].postln;
							// Preset
							if(cmd == 'preset',
								{
									number = msg[item+2].asInteger.postln;
									{
										~fonctionLoadInstruments.value(number);
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

		s.bind{

			// FONCTION POUR LOADER REPERTOIRE

			~initAllSound={arg path, file;
				if(File.exists(path++file), {file=File(path++file,"r");~sounds=file.readAllString.interpret.soloArray;file.close},{~sounds=
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
				if(File.exists(path++file), {file=File(path++file,"r");~listSynth=file.readAllString.interpret;file.close},{~listSynth=[
					"PlayBuf",
					"BufRd",
					"TGrains",
					"LoopBuf",
					"GrainBuf",
					"HPplayBuf",
					"HPplayBufVibrato",
					"HPbufRd",
					"HPtGrains",
					"HPplayBufMedianLeakDC",
					"SampleResonz",
					"Synthesizer",
					"FreqShift",
					"PlayBufSquiz",
					"WaveLoss",
					"Warp1",
					"Granulation",
					"Toupie",
					"Elastique",
					"RandElastique",
					"RandKlankSample",
					"DjScratch",
					"LiquidFilter",
					"Centroid",
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

					"MdaPiano",
					"Piano Resonz",
					"Piano Synthesizer",
					"Piano FreqShift",
					"Piano Squiz",
					"Piano WaveLoss",
					"Piano LocalMax",
					"Piano MagSmear",
					"Piano RandComb",
					"Piano MagShift",
					"Piano BinScramble",
					"Piano BinShift",
					"Piano MagSquared",
					"Piano MagNoise",
					"Piano MagClip",
					"Piano MagBelow",
					"Piano MagAbove",
					"Piano Diffuser",
					"Piano ConformalMap",
					"Piano BrickWall",
					"Piano RectComb",
					"Piano MagSmooth",
					"Piano Compander",
					"Piano Max",
					"Piano Min",
					"Piano Add",
					"Piano Mul",
					"Piano MagDiv",
					"Piano RandWipe",
					"Piano BinWipe",
					"Piano CopyPhase",
					"Piano RectComb2",
					"Piano Morph",
					"Piano Convolution",

					"PlayBufAdd",
					"KlankPlayBuf",
					"Spring*Buf",
					"Piano+Sample",
					"String+Sample",

					"Osc",
					"VOsc",
					"VOsc3",
					"VarSaw",
					"Pulse",
					"Blip",
					"Formant",
					"Guitare",
					"CordeAnalogique",
					"Klang",
					"Klank",
					"Klank2",
					"Spring",
					"Gendy3",
					"PianoSynth",
					"PianoPercu",
					"BowedString",
					"StruckString",
					"PianoPrepare",
				];
				file=File(~nompathdata++"List Synth.scd","w");file.write("~listSynth="++~listSynth.asCompileString);file.close});
			};
			~initAllSynth.value(~nompathdata, "List Synth.scd");

			~initAllEffet={arg path, file;
				if(File.exists(path++"List FXpre.scd"), {file=File(path++"List FXpre.scd","r");~fxPre=file.readAllString.interpret;file.close},{~fxPre=[
					"AllpassC_pre",
					"FreeVerb_pre",
					"GVerb_pre",
					"JPverb_pre",
					"DelayC_pre",
					"DelayCtempo_pre",
					"CombC_pre",
					"HPF_pre",
					"LPF_pre",
					"BPF_pre",
					"BRF_pre",
					"RHPF_pre",
					"RLPF_pre",
					"Resonz_pre",
					"Ringz_pre",
					"Formlet_pre",
					"FOS_pre",
					"SOS_pre",
					"TwoPole_pre",
					"Median_pre",
					"LeakDC_pre",
					"Median+LeakDC_pre",
					"PitchShift_pre",
					"WarpDelay_pre",
					"RandRateDelay_pre",
					"PV_Cutoff_pre",
				];
				file=File(~nompathdata++"List FXpre.scd","w");file.write("~fxPre="++~fxPre.asCompileString);file.close;
				});
				if(File.exists(path++"List FXpost.scd"), {file=File(path++"List FXpost.scd","r");~fxPost=file.readAllString.interpret;file.close},{~fxPost=[
					"AllpassC_post",
					"FreeVerb_post",
					"GVerb_post",
					"JPverb_post",
					"DelayC_post",
					"DelayCtempo_post",
					"CombC_post",
					"HPF_post",
					"LPF_post",
					"BPF_post",
					"BRF_post",
					"RHPF_post",
					"RLPF_post",
					"Resonz_post",
					"Ringz_post",
					"Formlet_post",
					"FOS_post",
					"SOS_post",
					"TwoPole_post",
					"Median_post",
					"LeakDC_post",
					"Median+LeakDC_post",
					"PitchShift_post",
					"WarpDelay_post",
					"RandRateDelay_post",
				];
				file=File(~nompathdata++"List FXpost.scd","w");file.write("~fxPost="++~fxPost.asCompileString);file.close;
				});
			};
			~initAllEffet.value(~nompathdata);

			~groupeAnalyse=Group.new(s,\addToTail);
			~groupeBuffer=Group.new(s,\addToTail);// group des buffer son sample

			~nombreEffetsPre=~fxPre.size;
			~nombreEffetsPost=~fxPost.size;
			~gsynth=[];// groupe des synth
			~geffet=[];// groupe des effets
			~buseffetsPresynth=[];// liste des bus effetsPres
			~buseffetsPostsynth=[];// liste des bus audio effetsPost
			~gsynth=[];// groupe des synth
			~listesynth=[];// liste des synth;
			~listeeffetsPresynth=[];// liste des effetsPres
			~listeeffetsPostsynth=[];// liste des effetsPost
			~listenodeeffetsPresynth=[];// liste des nodes des effetsPres
			~listenodeeffetsPostsynth=[];// liste des nodes des effetsPost

			// Creation buffers pour Vosc et Vosc3 Attention doivent etre en premier !!!!!
			8.do({ arg i;
				var n, a, b;
				b=Buffer.alloc(s, 1024);
				s.sync;
				// generate array of harmonic amplitudes
				n = (i+1)**2;
				a = Array.fill(i, 0) ++ [0.5, 1, 0.5];
				s.sync;
				// fill table
				b.sine1(a);
				s.sync;
			});

			~busFileIn=Bus.audio(s);

			// Buffers pour AudioIn
			~initAllBuffer={
				~nombrebuffer=~sounds.size;
				// Init les liste des sons pour display
				~displaySons1=[];
				~displaySons2=[];
				~sounds.size.do({arg i;
					~displaySons1=~displaySons1.add(PathName(~sounds.wrapAt(i)).fileName);
					~displaySons2=~displaySons2.add(PathName(~sounds.wrapAt(i)).fileName);
				});
				~listebuffer=[];// liste des buffers
				~listebufferTampon=[];// liste des buffers Tampon
				~looprecordingValue=[];
				~flagBufferFreeze=[];
				~bufferTampon=[];
				~bufferAddTampon=[];
				~listesamplein=[];// liste des SamplerIn (buffer)
				~listesampleinAudio=[];// liste des SamplerIn (audio)
				~listesampleinFile=[];// liste des SamplerIn (file)
				~busreclevel=[];

				// init les synth buffer pour les sons sampler
				~nombrebuffer.do({arg i;
					~looprecordingValue=~looprecordingValue.add(0);
					~busreclevel=~busreclevel.add(Bus.control(s, 2));// 1 controls reclevel1 sample
					~file = SoundFile.new;
					s.sync;
					~file.openRead(~sounds.wrapAt(i).standardizePath);
					s.sync;
					if(~file.numChannels == 1,
						{	~listebuffer=~listebuffer.add(Buffer.read(s, ~sounds.wrapAt(i).standardizePath, action: {Post << i << ~sounds.wrapAt(i).standardizePath << " Finished" << Char.nl}));
							s.sync;
						},
						{~rawData= FloatArray.newClear(~file.numFrames * 2);
							s.sync;
							~file.readData(~rawData);
							s.sync;
							~rawData = Array.newFrom(~rawData);
							s.sync;
							Post << "Loading stereo sound" << " " << ~sounds.wrapAt(i).standardizePath << Char.nl;
							s.sync;
							~rawData = ~rawData.unlace(2).sum / 2;
							s.sync;
							~listebuffer=~listebuffer.add(Buffer.loadCollection(s, ~rawData, 1, action: {Post << i << ~sounds.wrapAt(i).standardizePath << " Finished" << Char.nl}));
							s.sync;
					});
					//~listebuffer.wrapPut(i, ~listebuffer.wrapAt(i).normalize(1.0));
					~file.close;
					s.sync;
					~listesampleinAudio=~listesampleinAudio.add(Synth.newPaused("SampleIn",['in', ~canalAudioIn, 'buffer',~listebuffer.wrapAt(i).bufnum], ~groupeBuffer,\addToTail).map('reclevel1',~busreclevel.wrapAt(i).index,'reclevel2', ~busreclevel.wrapAt(i).index + 1));
					s.sync;
					~listesampleinFile=~listesampleinFile.add(Synth.newPaused("FileIn", ['buffer',~listebuffer.wrapAt(i).bufnum,  'in', ~busFileIn.index],~groupeBuffer,\addToTail).map('reclevel1',~busreclevel.wrapAt(i).index, 'reclevel2', ~busreclevel.wrapAt(i).index + 1));
					s.sync;
				});

				// Set buffer tampon
				~nombreinstrument.do({arg instr; var listebuffer;
					~looprecordingValue=~looprecordingValue.add(0);
					~flagBufferFreeze=~flagBufferFreeze.add('Freeze buffer off');
					~bufferTampon=~bufferTampon.add(nil);
					~bufferAddTampon=~bufferAddTampon.add(nil);
					listebuffer=[];
					~nombrebuffer.do({arg buffer;
						// Init buffer tampon
						~file = SoundFile.new;
						s.sync;
						~file.openRead(~sounds.wrapAt(buffer).standardizePath);
						s.sync;
						if(~file.numChannels == 1,
							{Post << "Loading mono sound" << " " << ~sounds.wrapAt(buffer).standardizePath << Char.nl;
								s.sync;
								listebuffer=listebuffer.add(Buffer.read(s, ~sounds.wrapAt(buffer).standardizePath));
								s.sync;
							},
							{~rawData= FloatArray.newClear(~file.numFrames * 2);
								s.sync;
								~file.readData(~rawData);
								s.sync;
								~rawData = Array.newFrom(~rawData);
								s.sync;
								Post << "Loading stereo sound" << " " << ~sounds.wrapAt(buffer).standardizePath << Char.nl;
								s.sync;
								~rawData = ~rawData.unlace(2).sum / 2;
								s.sync;
								listebuffer=listebuffer.add(Buffer.loadCollection(s, ~rawData, 1));
								s.sync;
						});
						//listebuffer.wrapPut(buffer, ~listebuffer.wrapAt(buffer).normalize(1.0));
						~file.close;
						s.sync;
					});
					~listebufferTampon = ~listebufferTampon.add(listebuffer);
				});
			};
			~initAllBuffer.value;
			~listesamplein=~listesampleinAudio;//Init sample avec audioIn

			// Par Instruments
			~listeSynthID=[];
			~numerobuffer=[];// numero du buffer� jouer par l'instrument
			~numerobufferAdd=[];// numero buffer pour fft
			~nombreinstrument.do({arg i;var x,y,z,w,n1,n2,g;
				~listeSynthID=~listeSynthID.add(nil);
				//init les bus audio et control
				~gsynth=~gsynth.add(Group.new(s,\addToTail));// group des synth
				~geffet=~geffet.add(Group.new(s,\addToTail));// group des effets
				~buseffetsPresynth=~buseffetsPresynth.add(Bus.audio(s, 2));
				~buseffetsPostsynth=~buseffetsPostsynth.add(Bus.audio(s, 2));
				~busf=[];12.do({~busf=~busf.add(Bus.control(s))});// accords a 12 sons max
				~busfreqsynth=~busfreqsynth.add(~busf);
				~busfr=[];12.do({~busfr=~busfr.add(Bus.control(s))});// accords a 12 sons max
				~busfreqRatesynth=~busfreqRatesynth.add(~busfr);
				~busampsynth=~busampsynth.add(Bus.control(s));
				~buspansynthLo=~buspansynthLo.add(Bus.control(s));
				~buspansynthHi=~buspansynthHi.add(Bus.control(s));
				~busoffsetplaysampler=~busoffsetplaysampler.add(Bus.control(s));
				~buscontrolsynth=~buscontrolsynth.add(Bus.control(s,10));// 10 controls
				~wavetable=~wavetable.add(Buffer.alloc(s, 1024));// creation wavetable pour Osc
				s.sync;
			});

			~groupeMasterFX=Group.new(s, \addToTail);
			// Buffer read file pour analyse
			~file = SoundFile.new;
			s.sync;
			~file.openRead(~samplePourAnalyse);
			s.sync;
			if(~file.numChannels == 1,
				{~rawData= FloatArray.newClear(~file.numFrames * 2);
					s.sync;
					~file.readData(~rawData);
					s.sync;
					Post << "Loading mono sound" << " " << ~samplePourAnalyse << Char.nl;
					s.sync;
					~rawData = Array.newFrom(~rawData).stutter(2) / 2;
					s.sync;
					~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2);
					s.sync;
				},
				{Post << "Loading stereo sound" << " " << ~samplePourAnalyse << Char.nl;
					s.sync;
					~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1]);
					s.sync;
			});
			//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
			~file.close;
			s.sync;

			"Please Wait... Loading WekRobot... Sending SynthDef on Server... Loading Sounds...".postln;

			// OSC Synth Analyse
			~canalAudioIn = 0;
			//Creation Synth play File
			~synthPlayFile=Synth.newPaused("WekRobot Play File",
				[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index, \volume, 0.0], ~groupeAnalyse, \addToHead);
			s.sync;
			// Creation du SynthDef "AudioBeatFollow" pour suivre beat
			~audioIn=Synth.newPaused("OSC WekRobot Onsets",
				['in', ~canalAudioIn, \seuil, 0.5 ], ~groupeAnalyse, \addToTail);
			s.sync;
			// Creation du SynthDef "AudioFileFollow" pour analyse file
			~audioFile=Synth.newPaused("OSC WekRobot File Onsets",
				[\bufferplay, ~bufferanalysefile.bufnum, 'busFileIn', ~busFileIn.index], ~groupeAnalyse, \addToTail);
			s.sync;
			// Creation Synth Tempo
			~tempoFile=Synth.newPaused("OSC WekRobot Tempo FileIn",
				['busFileIn', ~busFileIn.index, \lock, 0], ~groupeAnalyse, \addToTail);
			s.sync;
			~tempoIn=Synth.newPaused("OSC WekRobot Tempo AudioIn",
				['in', ~canalAudioIn, \lock, 0], ~groupeAnalyse, \addToTail);
			s.sync;
			// VST
			~synthVST = Synth.newPaused("VST Plugin", [\xFade, 0.5, \gainIn, 0.5], ~groupeMasterFX, \addToHead);
			~fxVST = VSTPluginController(~synthVST);
			s.sync;
			// Creation MasterFX
			~masterFX = Synth.new("MasterFX", [\limit, 0.8], ~groupeMasterFX, \addToTail);
			s.sync;

			// Initialisation des listes datas
			~recsamplebuttondatas=[];~looprecsamplebuttondatas=[];~synthcontrolviewlevelsdatas=[];
			~sounds.size.do({arg i;~recsamplebuttondatas=~recsamplebuttondatas.add(0);
				~looprecsamplebuttondatas=~looprecsamplebuttondatas.add(0);
				~synthcontrolviewlevelsdatas=~synthcontrolviewlevelsdatas.add([1,0])});
			~numeroSynth=[];
			// Par Instruments
			~nombreinstrument.do({arg i;
				var x,y,z,w,n1,n2,g, b=[];
				//init les variables de jeux indispensables
				~differencefreq=~differencefreq.add(0.5);// intervalle minimum entre 2 freq (1 ton)
				~differenceamp=~differenceamp.add((1));// intervalle maximum entre 2 amp (4db)
				~differenceduree=~differenceduree.add(1/16);// intervalle minimum entre 2 durees analyse
				~dureeaccord=~dureeaccord.add(1/12);// intervalle duree minimum pour accord (1/16 seconde)
				~maxaccord=~maxaccord.add(6);// accord maximum 6 notes
				~listedatassize=~listedatassize.add(24);// 24 evenements maximum dans listes datas
				~dureeanalysemax=~dureeanalysemax.add(4);// 4 secondes de silence
				~dureeanalysesil=~dureeanalysesil.add(4);// 4 secondes de silence
				~listesynth=~listesynth.add("PlayBuf");
				~numeroSynth=~numeroSynth.add(0);
				~dureeMul=~dureeMul.add(1.0);
				~listenodeeffetsPresynth=~listenodeeffetsPresynth.add(0);
				~listenodeeffetsPostsynth=~listenodeeffetsPostsynth.add(0);
				~lastcontrolindex=~lastcontrolindex.add(0);
				~lastcontrolalgoindexfreq=~lastcontrolalgoindexfreq.add(0);
				~lastcontrolalgoindexamp=~lastcontrolalgoindexamp.add(0);
				~lastcontrolalgoindexduree=~lastcontrolalgoindexduree.add(0);
				~lastenvelopevalue=~lastenvelopevalue.add(0);
				~lastenvelopeindex=~lastenvelopeindex.add(0);
				~lasteffetsPreindex=~lasteffetsPreindex.add(0);
				~lasteffetsPostindex=~lasteffetsPostindex.add(0);
				~flagfreq=~flagfreq.add(0);
				~flagamp=~flagamp.add(0);
				~flagduree=~flagduree.add(0);
				~freq=~freq.add([0]);
				~amp=~amp.add(0);
				~ampPre=~ampPre.add(0);
				~ampPost=~ampPost.add(0);
				~duree=~duree.add(1);
				~playinstrument=~playinstrument.add("off");
				~numerobuffer=~numerobuffer.add(0);
				~numerobufferAdd=~numerobufferAdd.add(0);
				~indatasfreqinstrument=~indatasfreqinstrument.add("Fhz In Off");
				~indatasampinstrument=~indatasampinstrument.add("Amp In Off");
				~indatasdureeinstrument=~indatasdureeinstrument.add("Dur In Off");
				~algoinstrumentfreq=~algoinstrumentfreq.add("Fhz Out");
				~algoinstrumentamp=~algoinstrumentamp.add("Amp Out");
				~algoinstrumentduree=~algoinstrumentduree.add("Dur Out");
				~quantization=~quantization.add(100);
				~canalmidiin=~canalmidiin.add(0);
				~canalaudioout=~canalaudioout.add(0);
				~listemidifreq=~listemidifreq.add([]);
				~listemidiamp=~listemidiamp.add([]);
				~listemididuree=~listemididuree.add([]);
				~listeaudiofreq=~listeaudiofreq.add([]);
				~listeaudioamp=~listeaudioamp.add([]);
				~listeaudioduree=~listeaudioduree.add([]);
				~listeofffreq=~listeofffreq.add([]);
				~listeoffamp=~listeoffamp.add([]);
				~listeoffduree=~listeoffduree.add([]);
				~listechaosfreq=~listechaosfreq.add([]);
				~listechaosamp=~listechaosamp.add([]);
				~listechaosduree=~listechaosduree.add([]);
				~listeneuronefreq=~listeneuronefreq.add([]);
				~listeneuroneamp=~listeneuroneamp.add([]);
				~listeneuroneduree=~listeneuroneduree.add([]);
				~listegenetiquefreq=~listegenetiquefreq.add([]);
				~listegenetiqueamp=~listegenetiqueamp.add([]);
				~listegenetiqueduree=~listegenetiqueduree.add([]);
				~listealgo1freq=~listealgo1freq.add([]);
				~listealgo1amp=~listealgo1amp.add([]);
				~listealgo1duree=~listealgo1duree.add([]);
				~listealgo2freq=~listealgo2freq.add([]);
				~listealgo2amp=~listealgo2amp.add([]);
				~listealgo2duree=~listealgo2duree.add([]);
				~listeinfreq=~listeinfreq.add([]);
				~listeinamp=~listeinamp.add([]);
				~listeinduree=~listeinduree.add([]);
				~listeoutneuronefreq=~listeoutneuronefreq.add([]);
				~listeoutneuroneamp=~listeoutneuroneamp.add([]);
				~listeoutneuroneduree=~listeoutneuroneduree.add([]);
				~flagFreqAlgo=~flagFreqAlgo.add(0);
				~flagAmpAlgo=~flagAmpAlgo.add(0);
				~flagDureeAlgo=~flagDureeAlgo.add(0);
				~freqMidi=~freqMidi.add(0);
				~canalMidiOutInstr=~canalMidiOutInstr.add(-1);
				~instanceAlgo1=~instanceAlgo1.add(HPclassProbabilite1.new);
				~instanceAlgo2=~instanceAlgo2.add(HPclassProbabilite2.new);
				~instanceChaosF=~instanceChaosF.add(HPclassChaos.new(3.852, 1.0.rand));
				~instanceChaosA=~instanceChaosA.add(HPclassChaos.new(3.852, 1.0.rand));
				~instanceChaosD=~instanceChaosD.add(HPclassChaos.new(3.852, 1.0.rand));
				~couchecachee = 9;
				~instanceNeurones=~instanceNeurones.add(HPclassNeurones.new(3, ~couchecachee, 3));// Init poids et seuils
				~flagneuronefreq=~flagneuronefreq.add(0);
				~flagneuroneamp=~flagneuroneamp.add(0);
				~flagneuroneduree=~flagneuroneduree.add(0);
				~flagoutneuronefreq=~flagoutneuronefreq.add(0);
				~flagoutneuroneamp=~flagoutneuroneamp.add(0);
				~flagoutneuroneduree=~flagoutneuroneduree.add(0);
				~neuronemode=~neuronemode.add('off');
				~neuroneapprentissage=~neuroneapprentissage.add(0.5);
				~neuronetemperature=~neuronetemperature.add(0.5);
				~flagalgoinstrneuronefreq=~flagalgoinstrneuronefreq.add("NeuroneAlgo1");
				~flagalgoinstrneuroneamp=~flagalgoinstrneuroneamp.add("NeuroneAlgo1");
				~flagalgoinstrneuroneduree=~flagalgoinstrneuroneduree.add("NeuroneAlgo1");
				~instanceGenetiques=~instanceGenetiques.add(HPclassGenetiques.new);// init instance genetiques
				~genetiquemode=~genetiquemode.add('off');
				~genetiquecroisement=~genetiquecroisement.add(0.5);
				~genetiquemutation=~genetiquemutation.add(0.05);
				~flaggenetiquefreq=~flaggenetiquefreq.add(0);
				~flaggenetiqueamp=~flaggenetiqueamp.add(0);
				~flaggenetiqueduree=~flaggenetiqueduree.add(0);
				~differenceparents=~differenceparents.add(0.0);
				~datassaveloadinstrument=~datassaveloadinstrument.add([]);
				~dataseffetsPres=[];~fxPre.size.do({arg i;var datas;datas=[0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5];~dataseffetsPres=~dataseffetsPres.add(datas)});
				~synthcontrolvieweffetsPredatas=~synthcontrolvieweffetsPredatas.add(~dataseffetsPres);
				~datasonoff=[];~fxPre.size.do({arg i;var datas=0;~datasonoff=~datasonoff.add(datas)});
				~synthonoffeffetsPredatas=~synthonoffeffetsPredatas.add(~datasonoff);
				~dataspaneffetsPres=[];~fxPre.size.do({arg i;var datas=0;~dataspaneffetsPres=~dataspaneffetsPres.add(datas)});
				~synthpaneffetsPrestepdatas=~synthpaneffetsPrestepdatas.add(~dataspaneffetsPres);
				~datasampeffetsPres=[];x=[];
				~fxPre.size.do({arg i;var datas;datas=(-12);~datasampeffetsPres=~datasampeffetsPres.add(datas);
					x=x.add(0)});
				~synthampeffetsPrestepdatas=~synthampeffetsPrestepdatas.add(~datasampeffetsPres);
				~randomValueEffetPre=~randomValueEffetPre.add(x);
				~randomValueEffetPanPre=~randomValueEffetPanPre.add(x.copy);
				~dataseffetsPosts=[];x=[];
				~fxPost.size.do({arg i;var datas;datas=[0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5];~dataseffetsPosts=~dataseffetsPosts.add(datas);
					x=x.add(0)});
				~synthcontrolvieweffetsPostdatas=~synthcontrolvieweffetsPostdatas.add(~dataseffetsPosts);
				~randomValueEffetPost=~randomValueEffetPost.add(x);
				~randomValueEffetPanPost=~randomValueEffetPanPost.add(x.copy);
				~datasonoff=[];~fxPost.size.do({arg i;var datas=0;~datasonoff=~datasonoff.add(datas)});
				~synthonoffeffetsPostdatas=~synthonoffeffetsPostdatas.add(~datasonoff);
				~dataspaneffetsPosts=[];~fxPost.size.do({arg i;var datas=0;~dataspaneffetsPosts=~dataspaneffetsPosts.add(datas)});
				~synthpaneffetsPoststepdatas=~synthpaneffetsPoststepdatas.add(~dataspaneffetsPosts);
				~datasampeffetsPosts=[];~fxPost.size.do({arg i;var datas;datas=(-12);~datasampeffetsPosts=~datasampeffetsPosts.add(datas)});
				~synthampeffetsPoststepdatas=~synthampeffetsPoststepdatas.add(~datasampeffetsPosts);
				~dataspara=[]; x=[]; y=[]; z=[]; w=[];
				~listSynth.size.do({arg i;var datas;datas=[0,0,0,0,0,0,0,0,0,0];~dataspara=~dataspara.add(datas);
					x=x.add(0);y=y.add([0.0, 1.0, 1.0, 0.85, 0.75, 0.75, 0.5,0.0]);z=z.add([0.0, 0.015625, 0.125, 0.25, 0.5, 0.75, 0.9, 1.0]);w=w.add([-0.1, 0.1])});
				~synthcontrolviewparametresdatas=~synthcontrolviewparametresdatas.add(~dataspara);
				~randomValueSynth=~randomValueSynth.add(x);
				~randomValueParametreSynth=~randomValueParametreSynth.add(x.copy);
				~levelenvsynth=~levelenvsynth.add(y);
				~timeenvsynth=~timeenvsynth.add(z);
				~synthpancontrolValue=~synthpancontrolValue.add(w);
				~datasloop=[];x=[];
				~sounds.size.do({arg i;var datas;datas=0;~datasloop=~datasloop.add(datas); x=x.add(0)});
				~loopplaysamplerdatas=~loopplaysamplerdatas.add(~datasloop);
				~sampleroffsetcontrolValue=~sampleroffsetcontrolValue.add(x);
				~datasreverse=[];
				~sounds.size.do({arg i;var datas;datas=1;~datasreverse=~datasreverse.add(datas)});
				~reversesampledatas=~reversesampledatas.add(~datasreverse);
				~listeeffetsPresynth=~listeeffetsPresynth.add([]);
				~listeeffetsPostsynth=~listeeffetsPostsynth.add([]);
				// Cr�e les effetsPres  � choix pour les synth (� maper ensuite pour les activer avec GUI)
				~nombreEffetsPre.do({arg ii;
					~listeeffetsPresynth.wrapPut(i,~listeeffetsPresynth.wrapAt(i).add(Synth.newPaused(~fxPre.wrapAt(ii),['out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut,'in',~buseffetsPresynth.wrapAt(i).index,'buseffetsPost',~buseffetsPostsynth.wrapAt(i).index],~geffet.wrapAt(i),\addToTail)));
					s.sync;
				});
				~nombreEffetsPost.do({arg ii;
					~listeeffetsPostsynth.wrapPut(i,~listeeffetsPostsynth.wrapAt(i).add(Synth.newPaused(~fxPost.wrapAt(ii),['out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut,'in',~buseffetsPostsynth.wrapAt(i).index],~geffet.wrapAt(i),\addToTail)));
					s.sync;
				});
				// band
				~numFhzBand = ~numFhzBand.add(3);
				~rangeSynthBand = ~rangeSynthBand.add([1, 2, 3]); // 3 bands
				~flagBandSynth = ~flagBandSynth.add([0,1,1,1,0,0,0,0,0,0,0,0,0]);
				~flagSynthBand = ~flagSynthBand.add('off');
				~bandFHZ = ~bandFHZ.add([[0, 127], [0.0, 42.33], [42.33, 84.66], [84.66, 127.0] ]);
				~lastTimeBand = ~lastTimeBand.add([Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime]);// 12 band total
				// Tuning
				~tuning = ~tuning.add(Tuning.et12);
				~tuningIndex = ~tuningIndex.add(0);
				~degrees = ~degrees.add(Tuning.et12.semitones);
				~root = ~root.add(0);
				~scale = ~scale.add(Scale.new(((Tuning.et12.semitones + 0)%Tuning.et12.size).sort, Tuning.et12.size, Tuning.et12););
				~flagScaling = ~flagScaling.add('off');
				// Audio In Rec
				~canalAudioInInstr = ~canalAudioInInstr.add(0);
				~flagByPassSynth = ~flagByPassSynth.add(1);
			});
		};

		// Le programme commence vraiment ici !!!!!!!!

		// Routine Metronome
		~routineMetronome = Tdef('Metronome', {loop({
			{~metronomeGUI.value_(~tempoSystem.beatInBar+1)}.defer;
			(1/24 * ~tempoSystem.tempo).wait;
		})});

		// Algorithme de modification instrument par automation
		~routineautomation=Tdef(\automation, {
			var string, c, datas, valeurs, level, duree, x;
			loop({
				{~nombreinstrument.do({arg i;var synth, algo;
					if(~startbutton.wrapAt(i).value == 1, {
						if(~randomValueSynth.wrapAt(i).wrapAt(~synthcontrol.wrapAt(i).value) == 1, {
							// Automation des controls synth
							synth=~synthcontrol.wrapAt(i).value;
							if(1.0.sum3rand < 0,{
								valeurs=~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth).value;c=[];
								for(1,10,{arg control;var val;
									val=valeurs.wrapAt(control-1)+(0.1.bilinrand)%1.0;c=c.add(val)});
								~buscontrolsynth.wrapAt(i).set(c.wrapAt(0), c.wrapAt(1), c.wrapAt(2), c.wrapAt(3), c.wrapAt(4), c.wrapAt(5), c.wrapAt(6), c.wrapAt(7), c.wrapAt(8), c.wrapAt(9));
								if(~synthcontrol.wrapAt(i).value == (synth), {~synthcontrolviewparametres.wrapAt(i).value_(c)});
								datas=~synthcontrolviewparametresdatas.wrapAt(i).value;
								datas.wrapPut(synth,c);
								~synthcontrolviewparametresdatas.wrapPut(i,datas.value);
							});
							// setup formes d'ondes pour Osc
							~wavetable.wrapAt(i).sine2([1,2,3,4,5,6,7,8,9,10],~synthcontrolviewparametres.wrapAt(i).value);
						});
						// Automation des parametres instruments
						if(~randomValueParametreSynth.wrapAt(i).wrapAt(~synthcontrol.wrapAt(i).value) == 1, {
							// Automation des offsets samples
							if(1.0.sum3rand < 0,{
								~sampleroffsetcontrol.wrapAt(i).value=~sampleroffsetcontrol.wrapAt(i).value+(0.1.bilinrand)%1.0;
								~sampleroffsetcontrolValue.wrapAt(i).wrapPut(~soncontrol.wrapAt(i).value, ~sampleroffsetcontrol.wrapAt(i).value);
								if(~reversebutton.wrapAt(i).value == 0,
									{~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~busoffsetplaysampler.wrapAt(i).set(1-~sampleroffsetcontrol.wrapAt(i).value)})
							});
							// Automation des pan instr
							if(1.0.sum3rand < 0.75.neg,{
								x=[rrand(-1.0, 1.0), rrand(-1.0, 1.0)];
								~synthpancontrol.wrapAt(i).value_(x);
								~synthpancontrolValue.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, x.copy);
							});
							// reverse sample
							if(1.0.sum3rand < 0.75.neg,{
								~reversebutton.wrapAt(i).value_(abs(1.rand2));
								if(~reversebutton.wrapAt(i).value == 0,
									{~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),1));~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),-1));~busoffsetplaysampler.wrapAt(i).set(1-~sampleroffsetcontrol.wrapAt(i).value)});
							});
							// envelope
							if(1.0.sum3rand < 0.75.neg,{
								level=[0.0, 1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand, 0.0];
								~levelenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, level);
								duree=[0.0]++[1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand, 1.0.rand].sort++[1.0];
								~timeenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, duree);
								~synthsliderenvelope.wrapAt(i).value=[duree, level];
							});
						});
						// Automation des controls effetsPre _pre
						for(1,~fxPre.size,{arg effetsPre;
							if(~randomValueEffetPre.wrapAt(i).wrapAt(effetsPre - 1) == 1, {
								if(1.0.sum3rand < 0,{
									if(~synthonoffeffetsPredatas.wrapAt(i).wrapAt(effetsPre - 1) == 1,{
										valeurs=~synthcontrolvieweffetsPredatas.wrapAt(i).wrapAt(effetsPre-1).value;c=[];
										for(1,8,{arg control;var val;
											val=(valeurs.wrapAt(control-1)+(0.1.bilinrand)).clip(0.0625, 0.9375);c=c.add(val)});
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control1,c.wrapAt(0).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control2,c.wrapAt(1).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control3,c.wrapAt(2).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control4,c.wrapAt(3).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control5,c.wrapAt(4).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control6,c.wrapAt(5).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control7,c.wrapAt(6).value);
										~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre-1).set(\control8,c.wrapAt(7).value);
										if(~listenodeeffetsPresynth.wrapAt(i).value == (effetsPre-1), {~synthcontrolvieweffetsPre.wrapAt(i).value_(c)});
										datas=~synthcontrolvieweffetsPredatas.wrapAt(i).value;
										datas.wrapPut(effetsPre-1,c);
										~synthcontrolvieweffetsPredatas.wrapPut(i,datas.value);
									});
								});
							});
							// pan pre
							if(~randomValueEffetPanPre.wrapAt(i).wrapAt(effetsPre - 1) == 1, {
								if(1.0.sum3rand < 0,{
									if(~synthonoffeffetsPredatas.wrapAt(i).wrapAt(effetsPre - 1) == 1,{
										valeurs=~synthpaneffetsPrestepdatas.wrapAt(i).wrapAt(effetsPre-1);
										valeurs=valeurs+(0.15.bilinrand)+1;valeurs=valeurs%2.0;valeurs=valeurs-1.0;
										if(~listenodeeffetsPresynth.wrapAt(i).value == (effetsPre-1), {~synthpaneffetsPrestep.wrapAt(i).valueAction_(valeurs)});
										~synthpaneffetsPrestepdatas.wrapAt(i).wrapPut(effetsPre - 1, valeurs);
									});
								});
							});
						});
						// Automation des controls effetsPost
						for(1,~fxPost.size,{arg effetsPost;
							if(~randomValueEffetPost.wrapAt(i).wrapAt(effetsPost - 1) == 1, {
								if(1.0.sum3rand < 0,{
									if(~synthonoffeffetsPostdatas.wrapAt(i).wrapAt(effetsPost-1) == 1,{
										valeurs=~synthcontrolvieweffetsPostdatas.wrapAt(i).wrapAt(effetsPost-1).value;c=[];
										for(1,8,{arg control;var val;
											val=(valeurs.wrapAt(control-1)+(0.1.bilinrand)).clip(0.0625, 0.9375);c=c.add(val)});
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control1,c.wrapAt(0).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control2,c.wrapAt(1).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control3,c.wrapAt(2).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control4,c.wrapAt(3).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control5,c.wrapAt(4).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control6,c.wrapAt(5).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control7,c.wrapAt(6).value);
										~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost-1).set(\control8,c.wrapAt(7).value);
										if(~listenodeeffetsPostsynth.wrapAt(i).value == (effetsPost-1), {~synthcontrolvieweffetsPost.wrapAt(i).value_(c)});
										datas=~synthcontrolvieweffetsPostdatas.wrapAt(i).value;
										datas.wrapPut(effetsPost-1,c);
										~synthcontrolvieweffetsPostdatas.wrapPut(i,datas.value);
									});
								});
							});
							// pan post
							if(~randomValueEffetPanPost.wrapAt(i).wrapAt(effetsPost - 1) == 1, {
								if(1.0.sum3rand < 0,{
									if(~synthonoffeffetsPostdatas.wrapAt(i).wrapAt(effetsPost - 1) == 1,{
										valeurs=~synthpaneffetsPoststepdatas.wrapAt(i).wrapAt(effetsPost-1);
										valeurs=valeurs+(0.15.bilinrand)+1;valeurs=valeurs%2.0;valeurs=valeurs-1.0;
										if(~listenodeeffetsPostsynth.wrapAt(i).value == (effetsPost-1), {~synthpaneffetsPoststep.wrapAt(i).valueAction_(valeurs)});
										~synthpaneffetsPoststepdatas.wrapAt(i).wrapPut(effetsPost - 1, valeurs);
									});
								});
							});
						});
					});
				});
				}.defer;
				(~tempoautomation.reciprocal * ~tempoSystem.tempo).wait;
			});
		});

		// OSC Settings
		~serverAdresse = s.addr; // Adresse Server -> NetAddr(0.0.0.0, 0)
		~masterAppAddr = NetAddr.localAddr;
		~slaveAppAddr = NetAddr.localAddr;
		~oscStateFlag = 'off';

		~ardourOSC = NetAddr("127.0.0.1", 3819);// define NetAddr on local machine with Ardour's port number

		// OSC pour analyse tempo
		~oscTempoIn = OSCFunc.newMatching({arg msg, time, addr, recvPort;
			if(~flagTempoAnalyze == 'on' and: {~oscStateFlag != 'slave'} and: {~flagEntreeMidi == 'off'}, {
				{~tempoprocessinstrument.valueAction = msg.wrapAt(3) * 60 }.defer;
			});
		}, '/WekRobot_Analyse_Tempo', ~serverAdresse);
		// Fonction OSC
		~initOSCresponder = {
			~oscHPtempo = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~flagTempoAnalyze == 'on' and: {~oscStateFlag == 'slave'} and: {~entreemode != 'Midi IN'}, {
					{~tempoprocessinstrument.valueAction = msg.wrapAt(1)}.defer;
				});
			}, '/HPtempo', ~masterAppAddr);
			// OSC synchroStart slave
			~oscHPstart = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{~startsysteme.valueAction = msg.wrapAt(1)}.defer;
				});
			}, '/HPstart',  ~masterAppAddr);
			// OSC synchroBare slave
			~oscHPbare = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{~nombreBeats.valueAction = msg.wrapAt(1)}.defer;
				});
			}, '/HPbare', ~masterAppAddr);
			// OSC synchroRec slave
			~oscHPrec = OSCFunc.newMatching({arg msg, time, addr, recvPort;
				if(~oscStateFlag == 'slave', {
					{
						if(msg.wrapAt(1) == 'Rec On', {~fonctionRecOn.value});// Recording On
						if(msg.wrapAt(1) == 'Rec Off', {~fonctionRecOff.value});// Recording Off
						if(msg.wrapAt(1) == 'Rec Pause', {~fonctionRecPause.value});// Recording Pause
					}.defer;
				});
			}, '/HPbare', ~masterAppAddr);
		};
		~initOSCresponder.value;// Init OSC Responder

		~lastTimeAudio=Main.elapsedTime;// Init time analyse
		lastTimeWekPreset = Main.elapsedTime;
		~flagEntreeMode='Audio IN';
		~freqbefore=[];~ampbefore=[];~dureebefore=[];~freqtampon=[];~amptampon=[];~listeaudiofreq=[];~listeaudioamp=[];~listeaudioduree=[];~lastDureeInstrAudio=[];
		~nombreinstrument.do({arg instr;	~freqbefore=~freqbefore.add(0);~ampbefore=~ampbefore.add(0);~dureebefore=~dureebefore.add(0);~freqtampon=~freqtampon.add(nil);~amptampon=~amptampon.add(nil);~listeaudiofreq=~listeaudiofreq.add([]);~listeaudioamp=~listeaudioamp.add([]);~listeaudioduree=~listeaudioduree.add([]);~lastDureeInstrAudio=~lastDureeInstrAudio.add(~lastTimeAudio)});

		// DATA WIKI OUT
		OSCFunc.newMatching({arg msg, time, addr, recvPort;
			var wekOut, file, preset, v, p;

			wekOut = msg[1..];
			{
			// Preset
			numPreset = wekOut[0].asInteger.clip(1, 40);
			if(flagWTP == 'on' and: {numPreset != lastNumPreset and: {listeWekPreset.includes(numPreset)} and: {(time - lastTimeWekPreset) > timeWekPreset}},
				// load new preset
				{
						if(File.exists(~nompathdata++"instruments"+numPreset.asInteger.asString++".scd"),
							{
								lastNumPreset = numPreset;
								lastTimeWekPreset = time;
								~fonctionLoadInstruments.value(numPreset.asInteger);
						});
			});
			}.defer(0);
		},'/wek/outputs');

		// Analyse AudioIn
		~oscAudioIn = OSCFunc.newMatching({arg msg, time, addr, recvPort;
			var freq=0, amp=0, duree=0, flag, synth, flagBand, mfcc;
			if(~flagEntreeMode == 'Audio IN' or: {~flagEntreeMode == 'File IN'},
				{// Normalise
					duree = time - ~lastTimeAudio;
					freq=msg.wrapAt(3);
					freq=freq.clip(0,1);
					amp=msg.wrapAt(4).clip(0.001, 1.0);
					~nombreinstrument.do({arg instr;
						flagBand = 0;
						if(~dureeanalysesil.wrapAt(instr) <= duree  or: {duree >= ~dureeanalysemax.wrapAt(instr)}, // ici duree silence
							{~listeaudiofreq.wrapPut(instr,[]);~listeaudioamp.wrapPut(instr,[]);~listeaudioduree.wrapPut(instr,[]);~freqtampon.wrapPut(instr,nil);~amptampon.wrapPut(instr,nil);~freqbefore.wrapPut(instr,0);~ampbefore.wrapPut(instr,0);~dureebefore.wrapPut(instr,0);~lastTimeAudio = time;~lastDureeInstrAudio.wrapPut(instr, time);
								(~numFhzBand + 1).do({arg b;
									synth = ~lastTimeBand.at(instr); synth.put(b, time);
									~lastTimeBand.put(instr, synth);
								});
								// Set MIDI Off
								if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(instr).value >= 0}, {
									~freqMidi.wrapAt(instr).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(instr), ~freqMidi.wrapAt(instr).wrapAt(index), 0);
										if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(instr), ~freqMidi.wrapAt(instr).wrapAt(index), 0)});
								})});
						});
						if(~listeaudiofreq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr) or: {~listeaudioamp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr)} or: {~listeaudioduree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr)} or: {~dureeanalysemax.wrapAt(instr) <= duree},
							{~listeaudiofreq.wrapPut(instr,[]);~listeaudioamp.wrapPut(instr,[]);~listeaudioduree.wrapPut(instr,[])});
						if(~flagAlgorithm != 4,
							{
								if(abs(freq * 127 - (~freqbefore.wrapAt(instr) * 127)) >= ~differencefreq.wrapAt(instr) and: {abs(amp.ampdb - ~ampbefore.wrapAt(instr).ampdb) >= ~differenceamp.wrapAt(instr)} and: {abs(duree - ~lastTimeAudio) >= ~differenceduree.wrapAt(instr)} and: {duree >= ~differenceduree.wrapAt(instr)},
									{
										if(~freqtampon.wrapAt(instr) !=nil and: {~amptampon.wrapAt(instr) != nil},
											{
												if(~flagSynthBand.at(instr) == 'on' and: {~flagBandSynth.at(instr).sum > 0}, {
													for(1, ~numFhzBand.at(instr), {arg b;
														if(~flagBandSynth.at(instr).at(b) == 1,
															{
																if((time - ~lastTimeBand.at(instr).at(b)) <= ~dureeanalysesil.wrapAt(instr),
																	{
																		if((~freqtampon.wrapAt(instr) * 127) > ~bandFHZ.at(instr).wrapAt(b).at(0) and: {(~freqtampon.wrapAt(instr) * 127) < ~bandFHZ.at(instr).wrapAt(b).at(1)},
																			{~listeaudiofreq.wrapPut(instr,~listeaudiofreq.wrapAt(instr).add(~freqtampon.wrapAt(instr)));
																				~listeaudioamp.wrapPut(instr,~listeaudioamp.wrapAt(instr).add(~amptampon.wrapAt(instr)));
																				~listeaudioduree.wrapPut(instr,~listeaudioduree.wrapAt(instr).add(duree));
																				~freqbefore.wrapPut(instr, ~freqtampon.wrapAt(instr));~ampbefore.wrapPut(instr, ~amptampon.wrapAt(instr));~dureebefore.wrapPut(instr, ~duree.wrapAt(instr));
																				synth = ~lastTimeBand.at(instr); synth.put(b, time);
																				~lastTimeBand.put(instr, synth);
																		});
																	},
																	{
																		flagBand = flagBand + 1;
																		synth = ~lastTimeBand.at(instr); synth.put(b, time);
																		~lastTimeBand.put(instr, synth);
																});
														});
													});
													// Test Silence Band
													if(flagBand > 0, {
														~listeaudiofreq.wrapPut(instr,[]);~listeaudioamp.wrapPut(instr,[]);~listeaudioduree.wrapPut(instr,[]);
														/*~freqtampon.wrapPut(instr,nil);~amptampon.wrapPut(instr,nil);
														~freqbefore.wrapPut(instr,0);~ampbefore.wrapPut(instr,0);~dureebefore.wrapPut(instr,0);
														~lastTimeAudio = time;~lastDureeInstrAudio.wrapPut(instr, time);*/
													});
												},
												{
													~listeaudiofreq.wrapPut(instr,~listeaudiofreq.wrapAt(instr).add(~freqtampon.wrapAt(instr)));
													~listeaudioamp.wrapPut(instr,~listeaudioamp.wrapAt(instr).add(~amptampon.wrapAt(instr)));
													~listeaudioduree.wrapPut(instr,~listeaudioduree.wrapAt(instr).add(duree));
													~freqbefore.wrapPut(instr, ~freqtampon.wrapAt(instr));~ampbefore.wrapPut(instr, ~amptampon.wrapAt(instr));~dureebefore.wrapPut(instr, ~duree.wrapAt(instr));
												});
										});
										~freqtampon.wrapPut(instr,freq);~amptampon.wrapPut(instr,amp);~lastTimeAudio = time;~lastDureeInstrAudio.wrapPut(instr, time);
								});
							},
							// Keyboard
							{
								if(~freqtampon.wrapAt(instr) !=nil and: {~amptampon.wrapAt(instr) != nil},
									{
										if(~flagSynthBand.at(instr) == 'on' and: {~flagBandSynth.at(instr).sum > 0}, {
											for(1, ~numFhzBand.at(instr), {arg b;
												if(~flagBandSynth.at(instr).at(b) == 1,
													{
														if((time - ~lastTimeBand.at(instr).at(b)) <= ~dureeanalysesil.wrapAt(instr),
															{
																if((~freqtampon.wrapAt(instr) * 127) > ~bandFHZ.at(instr).wrapAt(b).at(0) and: {(~freqtampon.wrapAt(instr) * 127) < ~bandFHZ.at(instr).wrapAt(b).at(1)},
																	{~listeaudiofreq.wrapPut(instr,~listeaudiofreq.wrapAt(instr).add(~freqtampon.wrapAt(instr)));
																		~listeaudioamp.wrapPut(instr,~listeaudioamp.wrapAt(instr).add(~amptampon.wrapAt(instr)));
																		~listeaudioduree.wrapPut(instr,~listeaudioduree.wrapAt(instr).add(duree));
																		~freqbefore.wrapPut(instr, ~freqtampon.wrapAt(instr));~ampbefore.wrapPut(instr, ~amptampon.wrapAt(instr));~dureebefore.wrapPut(instr, ~duree.wrapAt(instr));
																		synth = ~lastTimeBand.at(instr); synth.put(b, time);
																		~lastTimeBand.put(instr, synth);
																});
															},
															{
																flagBand = flagBand + 1;
																synth = ~lastTimeBand.at(instr); synth.put(b, time);
																~lastTimeBand.put(instr, synth);
														});
												});
											});
											// Test Silence Band
											if(flagBand > 0, {
												~listeaudiofreq.wrapPut(instr,[]);~listeaudioamp.wrapPut(instr,[]);~listeaudioduree.wrapPut(instr,[]);
												/*~freqtampon.wrapPut(instr,nil);~amptampon.wrapPut(instr,nil);
												~freqbefore.wrapPut(instr,0);~ampbefore.wrapPut(instr,0);~dureebefore.wrapPut(instr,0);
												~lastTimeAudio = time;~lastDureeInstrAudio.wrapPut(instr, time);*/
											});
										},
										{
											~listeaudiofreq.wrapPut(instr,~listeaudiofreq.wrapAt(instr).add(~freqtampon.wrapAt(instr)));
											~listeaudioamp.wrapPut(instr,~listeaudioamp.wrapAt(instr).add(~amptampon.wrapAt(instr)));
											~listeaudioduree.wrapPut(instr,~listeaudioduree.wrapAt(instr).add(duree));
											~freqbefore.wrapPut(instr, ~freqtampon.wrapAt(instr));~ampbefore.wrapPut(instr, ~amptampon.wrapAt(instr));~dureebefore.wrapPut(instr, ~duree.wrapAt(instr));
										});
								});
								~freqtampon.wrapPut(instr,freq);~amptampon.wrapPut(instr,amp);~lastTimeAudio = time;~lastDureeInstrAudio.wrapPut(instr, time);
						});
					});
					// WEKINATOR
					mfcc = msg[5..];
					//Sender
					sender.sendMsg("/wek/inputs", *mfcc[0..]);

					if(flagStreamMFCC != 'wek',
						{
							sender.sendMsg("/wekinator/control/outputs", numPreset.asFloat);
					});
				},
				{
					{
						~audioDisplay.string_(msg.wrapAt(4).value.round(0.01)).stringColor_(Color.red(msg.wrapAt(3).value, 1));
					}.defer;
			});
		}, '/WekRobot_Analyse_Audio', ~serverAdresse);

		// Midi In on
		~flagEntreeMidi='off';
		~freqbeforeMidi=[];~ampbeforeMidi=[];~dureebeforeMidi=[];~freqtamponMidi=[]; ~amptamponMidi=[]; ~listemidifreq=[]; ~listemidiamp=[];~listemididuree=[];~lastDureeInstrMidi=[];
		~nombreinstrument.do({arg instr;
			~freqbeforeMidi=~freqbeforeMidi.add(0);~ampbeforeMidi=~ampbeforeMidi.add(0);~dureebeforeMidi=~dureebeforeMidi.add(0);~freqtamponMidi=~freqtamponMidi.add([]);~amptamponMidi=~amptamponMidi.add([]);~listemidifreq=~listemidifreq.add([]);~listemidiamp=~listemidiamp.add([]);~listemididuree=~listemididuree.add([]);~lastDureeInstrMidi=~lastDureeInstrMidi.add(Main.elapsedTime)});
		~lastTimeMidi=[]; 16.do({arg i; ~lastTimeMidi=~lastTimeMidi.add(Main.elapsedTime)});//Init duree canaux
		~fonctionOSCMidiIn={
			~oscMidiIn=MIDIdef.noteOn(\midiNoteOn, {arg amp, freq, canal, src;
				// Normalise
				s.bind{
					~groupeAnalyse.set(\note, freq.midicps, \amp, amp / 127, \trigger, 1);
					s.sync;
					~groupeAnalyse.set(\note, freq.midicps, \amp, 0, \trigger, 0);
					s.sync;
				};
			}, (0..127), ~canalmidiin.asInteger);
			~oscMidiIn;
		};

		// Chaos in
		~routinechaosin=Tdef(\analyseChaos, {
			loop({
				~nombreinstrument.do({arg instr;var freq, amp, duree;
					freq = ~instanceChaosF.wrapAt(instr).next;
					amp = ~instanceChaosA.wrapAt(instr).next;
					duree = ~instanceChaosD.wrapAt(instr).next;
					if(~listechaosfreq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listechaosfreq.wrapPut(instr,[])});
					~listechaosfreq.wrapPut(instr,~listechaosfreq.wrapAt(instr).add(freq));
					if(~listechaosamp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listechaosamp.wrapPut(instr,[])});
					~listechaosamp.wrapPut(instr,~listechaosamp.wrapAt(instr).add(amp));
					if(~listechaosduree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listechaosduree.wrapPut(instr,[])});
					~listechaosduree.wrapPut(instr,~listechaosduree.wrapAt(instr).add(duree));
				});
				(~tempochaos.reciprocal * ~tempoSystem.tempo).wait;
			});
		});


		// Neurone calcul
		~routineneuronein=Tdef(\analyseNeurone, {
			loop({
				~nombreinstrument.do({arg instr;var freq, amp, duree, datasin, datasout, freqout, ampout, dureeout;
					datasin = [];datasout=[];
					if(~flagneuronefreq.wrapAt(instr) >= ~listeinfreq.wrapAt(instr).size, {~flagneuronefreq.wrapPut(instr,0);if(~flagalgoinstrneuronefreq.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuronefreq.wrapPut(instr,0)})});
					freq=~listeinfreq.wrapAt(instr).wrapAt(~flagneuronefreq.wrapAt(instr).value);
					if(~flagoutneuronefreq.wrapAt(instr) >= ~listeoutneuronefreq.wrapAt(instr).size, {~flagoutneuronefreq.wrapPut(instr,0);if(~flagalgoinstrneuronefreq.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuronefreq.wrapPut(instr,0)})});
					freqout=~listeoutneuronefreq.wrapAt(instr).wrapAt(~flagoutneuronefreq.wrapAt(instr).value);
					if(~flagneuroneamp.wrapAt(instr) >= ~listeinamp.wrapAt(instr).size, {~flagneuroneamp.wrapPut(instr,0);if(~flagalgoinstrneuroneamp.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuroneamp.wrapPut(instr,0)})});
					amp=~listeinamp.wrapAt(instr).wrapAt(~flagneuroneamp.wrapAt(instr).value);
					if(~flagoutneuroneamp.wrapAt(instr) >= ~listeoutneuroneamp.wrapAt(instr).size, {~flagoutneuroneamp.wrapPut(instr,0);if(~flagalgoinstrneuroneamp.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuroneamp.wrapPut(instr,0)})});
					ampout=~listeoutneuroneamp.wrapAt(instr).wrapAt(~flagoutneuroneamp.wrapAt(instr).value);
					if(~flagneuroneduree.wrapAt(instr) >= ~listeinduree.wrapAt(instr).size, {~flagneuroneduree.wrapPut(instr,0);if(~flagalgoinstrneuroneduree.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuroneduree.wrapPut(instr,0)})});
					duree=~listeinduree.wrapAt(instr).wrapAt(~flagneuroneduree.wrapAt(instr).value);
					if(~flagoutneuroneduree.wrapAt(instr) >= ~listeoutneuroneduree.wrapAt(instr).size, {~flagoutneuroneduree.wrapPut(instr,0);if(~flagalgoinstrneuroneduree.wrapAt(instr).value != "NeuroneAlgo2",{~flagoutneuroneduree.wrapPut(instr,0)})});
					dureeout=~listeoutneuroneduree.wrapAt(instr).wrapAt(~flagoutneuroneduree.wrapAt(instr).value);
					if(freq != nil and: {amp != nil} and: {duree != nil}, {datasin=datasin.add(freq);datasin=datasin.add(amp);datasin=datasin.add(duree);
						if(~flagalgoinstrneuronefreq.wrapAt(instr).value != "NeuroneAlgo2",{datasout=datasout.add(freq)},{if(freqout != nil, {datasout=datasout.add(freqout)},{datasout=datasout.add(freq)})});
						if(~flagalgoinstrneuroneamp.wrapAt(instr).value != "NeuroneAlgo2",{datasout=datasout.add(amp)},{if(ampout != nil, {datasout=datasout.add(ampout)},{datasout=datasout.add(amp)})});
						if(~flagalgoinstrneuroneduree.wrapAt(instr).value != "NeuroneAlgo2",{datasout=datasout.add(duree)},{if(dureeout != nil, {datasout=datasout.add(dureeout)},{datasout=datasout.add(duree)})});
						# freq, amp, duree = ~instanceNeurones.wrapAt(instr).next(datasin, datasout, ~neuronemode.wrapAt(instr),~neuroneapprentissage.wrapAt(instr),~neuronetemperature.wrapAt(instr));
						// Verify if array
						if(amp.isArray or: {duree.isArray}, {amp=amp.blendAt(1);duree=duree.blendAt(1)});
						if(~listeneuronefreq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listeneuronefreq.wrapPut(instr,[])});
						~listeneuronefreq.wrapPut(instr,~listeneuronefreq.wrapAt(instr).add(freq));
						~flagneuronefreq.wrapPut(instr,~flagneuronefreq.wrapAt(instr)+1);
						~flagoutneuronefreq.wrapPut(instr,~flagoutneuronefreq.wrapAt(instr)+1);
						if(~listeneuroneamp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listeneuroneamp.wrapPut(instr,[])});
						~listeneuroneamp.wrapPut(instr,~listeneuroneamp.wrapAt(instr).add(amp));
						~flagneuroneamp.wrapPut(instr,~flagneuroneamp.wrapAt(instr)+1);
						~flagoutneuroneamp.wrapPut(instr,~flagoutneuroneamp.wrapAt(instr)+1);
						if(~listeneuroneduree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listeneuroneduree.wrapPut(instr,[])});
						~listeneuroneduree.wrapPut(instr,~listeneuroneduree.wrapAt(instr).add(duree));
						~flagneuroneduree.wrapPut(instr,~flagneuroneduree.wrapAt(instr)+1);
						~flagoutneuroneduree.wrapPut(instr,~flagoutneuroneduree.wrapAt(instr)+1)},{~listeneuronefreq.wrapPut(instr,[]);~listeneuroneamp.wrapPut(instr,[]);~listeneuroneduree.wrapPut(instr,[])});
				});
				(~temponeurone.reciprocal * ~tempoSystem.tempo).wait;
			});
		});

		// Genetique calcul
		~routinegenetiquein=Tdef(\analyseGenetique, {
			var normalisation;
			loop({
				~nombreinstrument.do({arg instr;var freq, amp, duree, populations, enfants;
					populations=[];normalisation=[[ 0, 1], [ 0, 1], [0, ~dureeanalysemax.wrapAt(instr)]];
					if(~flaggenetiquefreq.wrapAt(instr) >= ~listeinfreq.wrapAt(instr).size, {~flaggenetiquefreq.wrapPut(instr,0)});
					freq=~listeinfreq.wrapAt(instr).wrapAt(~flaggenetiquefreq.wrapAt(instr).value);
					if(~flaggenetiqueamp.wrapAt(instr) >= ~listeinamp.wrapAt(instr).size, {~flaggenetiqueamp.wrapPut(instr,0)});
					amp=~listeinamp.wrapAt(instr).wrapAt(~flaggenetiqueamp.wrapAt(instr).value);
					if(~flaggenetiqueduree.wrapAt(instr) >= ~listeinduree.wrapAt(instr).size, {~flaggenetiqueduree.wrapPut(instr,0)});
					duree=~listeinduree.wrapAt(instr).wrapAt(~flaggenetiqueduree.wrapAt(instr).value);
					if(freq != nil and: {amp != nil} and: {duree != nil}, {
						populations=populations.add(freq);populations=populations.add(amp);populations=populations.add(duree);
						enfants = ~instanceGenetiques.wrapAt(instr).next(populations, ~genetiquemode.wrapAt(instr), ~genetiquecroisement.wrapAt(instr), ~genetiquemutation.wrapAt(instr), ~differenceparents.wrapAt(instr), normalisation);
						if(~listegenetiquefreq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listegenetiquefreq.wrapPut(instr,[])});
						~listegenetiquefreq.wrapPut(instr,~listegenetiquefreq.wrapAt(instr).add(enfants.wrapAt(0)));
						~flaggenetiquefreq.wrapPut(instr,~flaggenetiquefreq.wrapAt(instr)+1);
						if(~listegenetiqueamp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listegenetiqueamp.wrapPut(instr,[])});
						~listegenetiqueamp.wrapPut(instr,~listegenetiqueamp.wrapAt(instr).add(enfants.wrapAt(1)));
						~flaggenetiqueamp.wrapPut(instr,~flaggenetiqueamp.wrapAt(instr)+1);
						if(~listegenetiqueduree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listegenetiqueduree.wrapPut(instr,[])});
						~listegenetiqueduree.wrapPut(instr,~listegenetiqueduree.wrapAt(instr).add(enfants.wrapAt(2)));
						~flaggenetiqueduree.wrapPut(instr,~flaggenetiqueduree.wrapAt(instr)+1)},{~listegenetiquefreq.wrapPut(instr,[]);~listegenetiqueamp.wrapPut(instr,[]);~listegenetiqueduree.wrapPut(instr,[])});
				});
				(~tempogenetique.reciprocal * ~tempoSystem.tempo).wait;
			});
		});

		// Analyse model1 des datas audio
		~routinealgorithmes=Tdef(\analyseAlgorithmes, {
			loop({
				~nombreinstrument.do({arg instr;var freq=0, amp=0, duree=0, lastfreq, lastamp, lastduree;
					// Algo 1
					if(~listeinfreq.wrapAt(instr).size >=2 and: {~listeinamp.wrapAt(instr).size >=2} and: {~listeinduree.wrapAt(instr).size >=2}, {
						// Prend evenement suivant
						if(~flagFreqAlgo.wrapAt(instr) >= ~listeinfreq.wrapAt(instr).size, {~flagFreqAlgo.wrapPut(instr, 0)});
						lastfreq=~listeinfreq.wrapAt(instr).wrapAt(~flagFreqAlgo.wrapAt(instr));
						~flagFreqAlgo.wrapPut(instr, ~flagFreqAlgo.wrapAt(instr)+1);
						if(~flagAmpAlgo.wrapAt(instr) >= ~listeinamp.wrapAt(instr).size, {~flagAmpAlgo.wrapPut(instr, 0)});
						lastamp=~listeinamp.wrapAt(instr).wrapAt(~flagAmpAlgo.wrapAt(instr));
						~flagAmpAlgo.wrapPut(instr, ~flagAmpAlgo.wrapAt(instr)+1);
						if(~flagDureeAlgo.wrapAt(instr) >= ~listeinduree.wrapAt(instr).size, {~flagDureeAlgo.wrapPut(instr, 0)});
						lastduree=~listeinduree.wrapAt(instr).wrapAt(~flagDureeAlgo.wrapAt(instr));
						~flagDureeAlgo.wrapPut(instr, ~flagDureeAlgo.wrapAt(instr)+1);
						# freq, amp, duree = ~instanceAlgo1.wrapAt(instr).next(lastfreq, lastamp, lastduree);
						if(~listealgo1freq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo1freq.wrapPut(instr,[])});
						~listealgo1freq.wrapPut(instr,~listealgo1freq.wrapAt(instr).add(freq));
						if(~listealgo1amp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo1amp.wrapPut(instr,[])});
						~listealgo1amp.wrapPut(instr,~listealgo1amp.wrapAt(instr).add(amp));
						if(~listealgo1duree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo1duree.wrapPut(instr,[])});
						~listealgo1duree.wrapPut(instr,~listealgo1duree.wrapAt(instr).add(duree));
						// Algo 2
						# freq, amp, duree = ~instanceAlgo2.wrapAt(instr).next(lastfreq, lastamp, lastduree);
						if(~listealgo2freq.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo2freq.wrapPut(instr,[])});
						~listealgo2freq.wrapPut(instr,~listealgo2freq.wrapAt(instr).add(freq));
						if(~listealgo2amp.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo2amp.wrapPut(instr,[])});
						~listealgo2amp.wrapPut(instr,~listealgo2amp.wrapAt(instr).add(amp));
						if(~listealgo2duree.wrapAt(instr).size >= ~listedatassize.wrapAt(instr), {~listealgo2duree.wrapPut(instr,[])});
						~listealgo2duree.wrapPut(instr,~listealgo2duree.wrapAt(instr).add(duree));
					},
					{~listealgo1freq.wrapPut(instr,[]);~listealgo1amp.wrapPut(instr,[]);~listealgo1duree.wrapPut(instr,[]);
						~listealgo2freq.wrapPut(instr,[]);~listealgo2amp.wrapPut(instr,[]);~listealgo2duree.wrapPut(instr,[])});
				});
				(~tempoalgorithmes.reciprocal * ~tempoSystem.tempo).wait;
			});
		});

		// set datas playing
		~setdatasplaying={arg i, flagPlay, listeoutfreq, listeoutamp, listeoutduree, ampProba;
			var freq, amp=0, duree=0, flagAccords='off';
			freq=[];
			if(listeoutfreq.size == 0 or: {listeoutamp.size == 0} or: {listeoutduree.size == 0} or: {flagPlay == 'off'},
				{~playinstrument.wrapPut(i,"off")},{~playinstrument.wrapPut(i,"on");
					if(~algoinstrumentfreq.wrapAt(i) != ~algorithmesmusicauxfreq.wrapAt(4),{
						// non-probabilite
						if(~flagfreq.wrapAt(i).value >= listeoutfreq.size, {~flagfreq.wrapPut(i,0)});
						freq=freq.add((listeoutfreq.wrapAt(~flagfreq.wrapAt(i))*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo) + ~synthfreqrange.wrapAt(i).lo +~synthfreqstep.wrapAt(i).value).midicps).flat;
						~flagfreq.wrapPut(i,~flagfreq.wrapAt(i)+1)},{
						// probabilite
						freq=freq.add((listeoutfreq.wrapAt(listeoutfreq.size.rand)*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo) + ~synthfreqrange.wrapAt(i).lo +~synthfreqstep.wrapAt(i).value).midicps).flat});
					if(~algoinstrumentamp.wrapAt(i) != ~algorithmesmusicauxamp.wrapAt(4),{
						// non-probabilite
						if(~flagamp.wrapAt(i).value >= listeoutamp.size, {~flagamp.wrapPut(i,0)});
						amp=amp+(listeoutamp.wrapAt(~flagamp.wrapAt(i))* (~synthamprange.wrapAt(i).hi.dbamp - ~synthamprange.wrapAt(i).lo.dbamp) + ~synthamprange.wrapAt(i).lo.dbamp);
						~flagamp.wrapPut(i,~flagamp.wrapAt(i)+1)},{
						// probabilite
						ampProba = listeoutamp.wrapAt(listeoutamp.size.rand)*(~synthamprange.wrapAt(i).hi.dbamp - ~synthamprange.wrapAt(i).lo.dbamp) + ~synthamprange.wrapAt(i).lo.dbamp;
						amp=amp+ampProba;
					});
					if(~algoinstrumentduree.wrapAt(i) != ~algorithmesmusicauxduree.wrapAt(4),{
						// non-probabilite
						if(~flagduree.wrapAt(i).value >= listeoutduree.size, {~flagduree.wrapPut(i,0)});
						duree=listeoutduree.wrapAt(~flagduree.wrapAt(i));
						~flagduree.wrapPut(i,~flagduree.wrapAt(i)+1)},{
						// probabilite
						duree=listeoutduree.wrapAt(listeoutduree.size.rand)});
					// test accord
					while({duree < ~dureeaccord.wrapAt(i) and: {freq.size < ~maxaccord.wrapAt(i)} and: {~flagfreq.wrapAt(i).value < listeoutfreq.size} and: {~flagamp.wrapAt(i).value < listeoutamp.size} and: {~flagduree.wrapAt(i).value < listeoutduree.size}}, {if(~algoinstrumentfreq.wrapAt(i) != ~algorithmesmusicauxfreq.wrapAt(4),{
						// non-probabilite
						if(~flagfreq.wrapAt(i).value >= listeoutfreq.size, {~flagfreq.wrapPut(i,0)});
						freq=freq.add((listeoutfreq.wrapAt(~flagfreq.wrapAt(i))*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo) + ~synthfreqrange.wrapAt(i).lo +~synthfreqstep.wrapAt(i).value).midicps).flat;
						~flagfreq.wrapPut(i,~flagfreq.wrapAt(i)+1)},{
						// probabilite
						freq=freq.add((listeoutfreq.wrapAt(listeoutfreq.size.rand)*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo) + ~synthfreqrange.wrapAt(i).lo +~synthfreqstep.wrapAt(i).value).midicps).flat});
					if(~algoinstrumentamp.wrapAt(i) != ~algorithmesmusicauxamp.wrapAt(4),{
						// non-probabilite
						if(~flagamp.wrapAt(i).value >= listeoutamp.size, {~flagamp.wrapPut(i,0)});
						if(listeoutamp.wrapAt(~flagamp.wrapAt(i)) >= 0.001, {
							amp=amp+(listeoutamp.wrapAt(~flagamp.wrapAt(i))*(~synthamprange.wrapAt(i).hi.dbamp - ~synthamprange.wrapAt(i).lo.dbamp) + ~synthamprange.wrapAt(i).lo.dbamp);
						},
						{amp = amp + 0.0});
						~flagamp.wrapPut(i,~flagamp.wrapAt(i)+1)},{
						// probabilite
						if(listeoutamp.wrapAt(~flagamp.wrapAt(i)) >= 0.001, {
							amp=amp+(listeoutamp.wrapAt((listeoutamp.size.rand))*(~synthamprange.wrapAt(i).hi.dbamp - ~synthamprange.wrapAt(i).lo.dbamp) + ~synthamprange.wrapAt(i).lo.dbamp);
						},
						{amp = amp + 0.0});
					});
					if(~algoinstrumentduree.wrapAt(i) != ~algorithmesmusicauxduree.wrapAt(4),{
						// non-probabilite
						if(~flagduree.wrapAt(i).value >= listeoutduree.size, {~flagduree.wrapPut(i,0)});
						duree=listeoutduree.wrapAt(~flagduree.wrapAt(i));
						~flagduree.wrapPut(i,~flagduree.wrapAt(i)+1)},{
						// probabilite
						duree=listeoutduree.wrapAt(listeoutduree.size.rand)});
					flagAccords='on';
					});
			});
			// normalise et quantize note amplitude et duree
			duree = duree / ~dureeanalysemax.at(i);
			duree = duree * (~dureerange.wrapAt(i).hi - ~dureerange.wrapAt(i).lo)  + ~dureerange.wrapAt(i).lo;
			if(duree < ~quantization.wrapAt(i).value.reciprocal, {duree=~quantization.wrapAt(i).value.reciprocal},
				{
					// Quanta Music
					duree=duree.floor + ((duree.frac*~quantization.wrapAt(i).value+0.5).floor / ~quantization.wrapAt(i).value)});
			duree = (duree  * ~dureeMul.wrapAt(i).value).max(0.01);
			// normalise amp
			if(flagAccords == 'on', {amp=amp/freq.size;
			});
			// sortie algorithme
			if(~playinstrument.wrapAt(i) == "off", {freq = []; amp =[]});
			[freq, amp, duree];
		};

		// Definition des schedulers des instruments
		~nombreinstrument.do({arg i;
			~routineinstrument=~routineinstrument.add(Tdef(("instrument"++(i+1).asString).asSymbol, {
				var freq, amp=0, ampPre=0, ampPost=0, duree, listeoutfreq, listeoutamp, listeoutduree, time, flagInstrPlay, octave, ratio, degre, difL, difH, pos=~scale.at(i).degrees.size - 1;
				loop({
					// Choix des datas in freq
					switch(~indatasfreqinstrument.wrapAt(i).value,
						~choixdatasinfreq.wrapAt(0), {~listeinfreq.wrapPut(i,~listeofffreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(1), {~listeinfreq.wrapPut(i,~listeaudiofreq.wrapAt(i));~listeofffreq.wrapPut(i,~listeaudiofreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(2), {~listeinfreq.wrapPut(i,~listemidifreq.wrapAt(i));~listeofffreq.wrapPut(i,~listemidifreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(3), {~listeinfreq.wrapPut(i,~listechaosfreq.wrapAt(i));~listeofffreq.wrapPut(i,~listechaosfreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(4), {~listeinfreq.wrapPut(i,~listeneuronefreq.wrapAt(i));~listeofffreq.wrapPut(i,~listeneuronefreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(5), {~listeinfreq.wrapPut(i,~listegenetiquefreq.wrapAt(i));~listeofffreq.wrapPut(i,~listegenetiquefreq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(6), {~listeinfreq.wrapPut(i,~listealgo1freq.wrapAt(i));~listeofffreq.wrapPut(i,~listealgo1freq.wrapAt(i))},
						~choixdatasinfreq.wrapAt(7), {~listeinfreq.wrapPut(i,~listealgo2freq.wrapAt(i));~listeofffreq.wrapPut(i,~listealgo2freq.wrapAt(i))});
					// Choix des datas in amp
					switch(~indatasampinstrument.wrapAt(i).value,
						~choixdatasinamp.wrapAt(0), {~listeinamp.wrapPut(i,~listeoffamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(1), {~listeinamp.wrapPut(i,~listeaudioamp.wrapAt(i));~listeoffamp.wrapPut(i,~listeaudioamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(2), {~listeinamp.wrapPut(i,~listemidiamp.wrapAt(i));~listeoffamp.wrapPut(i,~listemidiamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(3), {~listeinamp.wrapPut(i,~listechaosamp.wrapAt(i));~listeoffamp.wrapPut(i,~listechaosamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(4), {~listeinamp.wrapPut(i,~listeneuroneamp.wrapAt(i));~listeoffamp.wrapPut(i,~listeneuroneamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(5), {~listeinamp.wrapPut(i,~listegenetiqueamp.wrapAt(i));~listeoffamp.wrapPut(i,~listegenetiqueamp.wrapAt(i))},
						~choixdatasinamp.wrapAt(6), {~listeinamp.wrapPut(i,~listealgo1amp.wrapAt(i));~listeoffamp.wrapPut(i,~listealgo1amp.wrapAt(i))},
						~choixdatasinamp.wrapAt(7), {~listeinamp.wrapPut(i,~listealgo2amp.wrapAt(i));~listeoffamp.wrapPut(i,~listealgo2amp.wrapAt(i))});
					// Choix des datas in duree
					switch(~indatasdureeinstrument.wrapAt(i).value,
						~choixdatasinduree.wrapAt(0), {~listeinduree.wrapPut(i,~listeoffduree.wrapAt(i))},
						~choixdatasinduree.wrapAt(1), {~listeinduree.wrapPut(i,~listeaudioduree.wrapAt(i));~listeoffduree.wrapPut(i,~listeaudioduree.wrapAt(i))},
						~choixdatasinduree.wrapAt(2), {~listeinduree.wrapPut(i,~listemididuree.wrapAt(i));~listeoffduree.wrapPut(i,~listemididuree.wrapAt(i))},
						~choixdatasinduree.wrapAt(3), {~listeinduree.wrapPut(i,~listechaosduree.wrapAt(i));~listeoffduree.wrapPut(i,~listechaosduree.wrapAt(i))},
						~choixdatasinduree.wrapAt(4), {~listeinduree.wrapPut(i,~listeneuroneduree.wrapAt(i));~listeoffduree.wrapPut(i,~listeneuroneduree.wrapAt(i))},
						~choixdatasinduree.wrapAt(5), {~listeinduree.wrapPut(i,~listegenetiqueduree.wrapAt(i));~listeoffduree.wrapPut(i,~listegenetiqueduree.wrapAt(i))},
						~choixdatasinduree.wrapAt(6), {~listeinduree.wrapPut(i,~listealgo1duree.wrapAt(i));~listeoffduree.wrapPut(i,~listealgo1duree.wrapAt(i))},
						~choixdatasinduree.wrapAt(7), {~listeinduree.wrapPut(i,~listealgo2duree.wrapAt(i));~listeoffduree.wrapPut(i,~listealgo2duree.wrapAt(i))});
					// Choix algo freq
					switch(~algoinstrumentfreq.wrapAt(i).value,
						~algorithmesmusicauxfreq.wrapAt(0), {listeoutfreq=~listeinfreq.wrapAt(i)},
						~algorithmesmusicauxfreq.wrapAt(1), {listeoutfreq=~listeneuronefreq.wrapAt(i)},
						~algorithmesmusicauxfreq.wrapAt(2), {listeoutfreq=~listeneuronefreq.wrapAt(i)},
						~algorithmesmusicauxfreq.wrapAt(3), {listeoutfreq=~listegenetiquefreq.wrapAt(i)},
						~algorithmesmusicauxfreq.wrapAt(4), {listeoutfreq=~listeinfreq.wrapAt(i).copy},
						~algorithmesmusicauxfreq.wrapAt(5), {listeoutfreq=~listealgo1freq.wrapAt(i)},
						~algorithmesmusicauxfreq.wrapAt(6), {listeoutfreq=~listealgo2freq.wrapAt(i)}
					);
					// Choix algo amp
					switch(~algoinstrumentamp.wrapAt(i).value,
						~algorithmesmusicauxamp.wrapAt(0), {listeoutamp=~listeinamp.wrapAt(i)},
						~algorithmesmusicauxamp.wrapAt(1), {listeoutamp=~listeneuroneamp.wrapAt(i)},
						~algorithmesmusicauxamp.wrapAt(2), {listeoutamp=~listeneuroneamp.wrapAt(i)},
						~algorithmesmusicauxamp.wrapAt(3), {listeoutamp=~listegenetiqueamp.wrapAt(i)},
						~algorithmesmusicauxamp.wrapAt(4), {listeoutamp=~listeinamp.wrapAt(i).copy},
						~algorithmesmusicauxamp.wrapAt(5), {listeoutamp=~listealgo1amp.wrapAt(i)},
						~algorithmesmusicauxamp.wrapAt(6), {listeoutamp=~listealgo2amp.wrapAt(i)}
					);
					// Choix algo duree
					switch(~algoinstrumentduree.wrapAt(i).value,
						~algorithmesmusicauxduree.wrapAt(0), {listeoutduree=~listeinduree.wrapAt(i)},
						~algorithmesmusicauxduree.wrapAt(1), {listeoutduree=~listeneuroneduree.wrapAt(i)},
						~algorithmesmusicauxduree.wrapAt(2), {listeoutduree=~listeneuroneduree.wrapAt(i)},
						~algorithmesmusicauxduree.wrapAt(3), {listeoutduree=~listegenetiqueduree.wrapAt(i)},
						~algorithmesmusicauxduree.wrapAt(4), {listeoutduree=~listeinduree.wrapAt(i).copy},
						~algorithmesmusicauxduree.wrapAt(5), {listeoutduree=~listealgo1duree.wrapAt(i)},
						~algorithmesmusicauxduree.wrapAt(6), {listeoutduree=~listealgo2duree.wrapAt(i)}
					);
					// Test si silence
					time = Main.elapsedTime;
					flagInstrPlay='on';
					if((time - ~lastDureeInstrAudio.wrapAt(i)) >= ~dureeanalysesil.wrapAt(i) and: {~indatasfreqinstrument.wrapAt(i).value == "Fhz In Audio" or: {~indatasampinstrument.wrapAt(i).value == "Amp In Audio"} or: {~indatasdureeinstrument.wrapAt(i).value == "Dur In Audio"}},
						{flagInstrPlay='off';
							// Set MIDI Off
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
								~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
								});
							});
						}, {if((time - ~lastDureeInstrMidi.wrapAt(i)) >= ~dureeanalysesil.wrapAt(i) and: {~indatasfreqinstrument.wrapAt(i).value == "Fhz In Midi" or: {~indatasampinstrument.wrapAt(i).value == "Amp In Midi"} or: {~indatasdureeinstrument.wrapAt(i).value == "Dur In Midi"}},
							{flagInstrPlay='off';
								// Set MIDI Off
								if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
									~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
										if(flagVST =='on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
									});
								});
					})});
					// set les datas calculees par les algorithmes pour playing
					# freq, amp, duree = ~setdatasplaying.value(i, flagInstrPlay, listeoutfreq, listeoutamp, listeoutduree);
					~duree.wrapPut(i,duree);
					if(~playinstrument.wrapAt(i) != "off",{
						if(~flagBufferFreeze.wrapAt(i) == 'Freeze buffer on', {
							// Copy buffers
							if(~looprecordingValue.wrapAt(~numerobuffer.wrapAt(i)).value == 1 ,{
								~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).copyData(~listebufferTampon.wrapAt(i).wrapAt(~numerobuffer.wrapAt(i)));
							});
							if(~looprecordingValue.wrapAt(~numerobufferAdd.wrapAt(i)).value == 1 ,{
								~listebuffer.wrapAt(~numerobufferAdd.wrapAt(i)).copyData(~listebufferTampon.wrapAt(i).wrapAt(~numerobufferAdd.wrapAt(i)));
							});
							~bufferTampon.wrapPut(i, ~listebufferTampon.wrapAt(i).wrapAt(~numerobuffer.wrapAt(i)));
							~bufferAddTampon.wrapPut(i, ~listebufferTampon.wrapAt(i).wrapAt(~numerobufferAdd.wrapAt(i)));
						},
						{~bufferTampon.wrapPut(i, ~listebuffer.wrapAt(~numerobuffer.wrapAt(i)));
							~bufferAddTampon.wrapPut(i, ~listebuffer.wrapAt(~numerobufferAdd.wrapAt(i)))};
						);
						// Set Tuning and Scaling
						if(~flagScaling.at(i) != 'off', {
							freq = freq.collect({arg item, index;
								pos = 0;
								octave = item.cpsoct.round(0.001);
								ratio = octave.frac;
								octave = octave.floor;
								degre = (ratio * ~tuning.at(i).size + 0.5).floor;
								(~scale.at(i).degrees.size - 1).do({arg d;
									difL=abs(degre - ~scale.at(i).degrees.at(d));
									difH=abs(degre - ~scale.at(i).degrees.at(d+1));
									if(degre >= ~scale.at(i).degrees.at(d) and: {degre <= ~scale.at(i).degrees.at(d+1)},
										{if(difL <= difH, {pos = d},{pos = d + 1})});
								});
								item = ~scale.at(i).degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
							});
						});
						// Set MIDI Off
						if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
							~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
								if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
							});// MIDI OFF
							// Reset MIDI OUT
							~freqMidi.wrapPut(i, []);
							// MidiOut
							~freqMidi.wrapPut(i, (freq.cpsmidi + 0.5).floor);// Liste freqMidi agents
						});
						// Accords
						~freq.wrapPut(i,freq);
						amp = amp * ~amplitudegeneraleinstrument.dbamp;
						~amp.wrapPut(i,amp);
						~ampPre.wrapPut(i, ~sendFXPre.wrapAt(i).value.dbamp);
						~ampPost.wrapPut(i, ~sendFXPost.wrapAt(i).value.dbamp);
						~busampsynth.wrapAt(i).set(~amp.wrapAt(i));
						~buspansynthLo.wrapAt(i).set(~synthpancontrol.wrapAt(i).lo);
						~buspansynthHi.wrapAt(i).set(~synthpancontrol.wrapAt(i).hi);
						// Play Synth
						~freq.wrapAt(i).size.do({arg ii;var freqRate;
							// Send MIDI On
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
								~midiOut.noteOn(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(ii), ~amp.wrapAt(i) * 127);// Send note MIDI ON
								if(flagVST == 'on', {~fxVST.midi.noteOn(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(ii), ~amp.wrapAt(i) * 127)});
							});
							~busfreqsynth.wrapAt(i).wrapAt(ii).set(~freq.wrapAt(i).wrapAt(ii));
							freqRate=(~freq.wrapAt(i).wrapAt(ii).cpsmidi - 48).midicps;
							~busfreqRatesynth.wrapAt(i).wrapAt(ii).set(freqRate);
							// Creation du synth pour jouer l'evenement musical
							~listeSynthID.wrapPut(i,Synth.new(~listesynth.wrapAt(i),['out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut,'duree',~duree.wrapAt(i),'controlenvlevel1',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(0),'controlenvlevel2',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(1),'controlenvlevel3',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(2),'controlenvlevel4',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(3),'controlenvlevel5',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(4),'controlenvlevel6',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(5),'controlenvlevel7',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(6),'controlenvlevel8',~levelenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(7),'controlenvtime1',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(1) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(0),'controlenvtime2',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(2)- ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(1),'controlenvtime3',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(3) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(2),'controlenvtime4',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(4) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(3),'controlenvtime5',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(5) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(4),'controlenvtime6',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(6) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(5),'controlenvtime7',~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(7) - ~timeenvsynth.wrapAt(i).wrapAt(~numeroSynth.wrapAt(i).value).wrapAt(6),'buseffetsPost',~buseffetsPostsynth.wrapAt(i).index,'buseffetsPre',~buseffetsPresynth.wrapAt(i).index,'loop',~loopplaysamplerdatas.wrapAt(i).wrapAt(~numerobuffer.wrapAt(i)),'loop2',~loopplaysamplerdatas.wrapAt(i).wrapAt(~numerobufferAdd.wrapAt(i)),'buffer', ~bufferTampon.wrapAt(i).bufnum,'buffer2', ~bufferAddTampon.wrapAt(i).bufnum,'reverse',~reversesampledatas.wrapAt(i).wrapAt(~numerobuffer.wrapAt(i)),'reverse2',~reversesampledatas.wrapAt(i).wrapAt(~numerobufferAdd.wrapAt(i)),'wavetable',~wavetable.wrapAt(i).bufnum,'wavetable2',0,'ampPre',~ampPre.wrapAt(i), 'ampPost',~ampPost.wrapAt(i), 'byPass', 1 - ~flagByPassSynth.wrapAt(i)],~gsynth.wrapAt(i)).mapn('freq',~busfreqsynth.wrapAt(i).wrapAt(ii).index,1,'freqRate', ~busfreqRatesynth.wrapAt(i).wrapAt(ii).index,1,'amp',~busampsynth.wrapAt(i).index,1,'panLo',~buspansynthLo.wrapAt(i).index,1,'panHi',~buspansynthHi.wrapAt(i).index,1,'pos', ~busoffsetplaysampler.wrapAt(i).index,1,'controls',~buscontrolsynth.wrapAt(i).index, 10));
							// set canal audio out effets
							~nombreEffetsPre.do({arg ii;
								~listeeffetsPresynth.wrapAt(i).wrapAt(ii).set('out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut)});
							~nombreEffetsPost.do({arg ii;
								~listeeffetsPostsynth.wrapAt(i).wrapAt(ii).set('out',~canalaudioout.wrapAt(i) + ~startChannelAudioOut)});
						});
					});
					~duree.wrapAt(i).wait;
				});
			});
			);
		});

		this.creationGUI;// create Windows

		~foncseq={arg i, item; var file, datas, datasfile;
			if(item.value == 0 ,{nil},
				{if(item.value.odd, {item=item.value + 1 / 2; item = item.asInteger;
					if(File.exists(~nompathdata++"sequence"+item.value.asString++".scd"),{
						file=File(~nompathdata++"sequence"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
						~listeaudiofreq.wrapPut(i,datas.wrapAt(0)/127);~listeaudioamp.wrapPut(i,datas.wrapAt(1).dbamp);~listeaudioduree.wrapPut(i,datas.wrapAt(2));
						~listemidifreq.wrapPut(i,datas.wrapAt(3)/127);~listemidiamp.wrapPut(i,datas.wrapAt(4).dbamp);~listemididuree.wrapPut(i,datas.wrapAt(5));
						~listechaosfreq.wrapPut(i,datas.wrapAt(6)/127);~listechaosamp.wrapPut(i,datas.wrapAt(7).dbamp);~listechaosduree.wrapPut(i,datas.wrapAt(8));
						~listeoutneuronefreq.wrapPut(i,datas.wrapAt(9)/127);~listeoutneuroneamp.wrapPut(i,datas.wrapAt(10).dbamp);~listeoutneuroneduree.wrapPut(i,datas.wrapAt(11));
						~listegenetiquefreq.wrapPut(i,datas.wrapAt(12)/127);~listegenetiqueamp.wrapPut(i,datas.wrapAt(13).dbamp);~listegenetiqueduree.wrapPut(i,datas.wrapAt(14));
						~listealgo1freq.wrapPut(i,datas.wrapAt(15)/127);~listealgo1amp.wrapPut(i,datas.wrapAt(16).dbamp);~listealgo1duree.wrapPut(i,datas.wrapAt(17));
						~listealgo2freq.wrapPut(i,datas.wrapAt(18)/127);~listealgo2amp.wrapPut(i,datas.wrapAt(19).dbamp);~listealgo2duree.wrapPut(i,datas.wrapAt(20));
						~listeofffreq.wrapPut(i,datas.wrapAt(21)/127);~listeoffamp.wrapPut(i,datas.wrapAt(22).dbamp);~listeoffduree.wrapPut(i,datas.wrapAt(23));
				})},
				{item=item.value / 2; item = item.asInteger;
					if(File.exists(~nompathdata++"sequences"+(item.value).asString++".scd"),{file=File(~nompathdata++"sequences"+(item.value).asString++".scd","r");datas=file.readAllString.interpret;file.close;for(0, ~nombreinstrument-1, {arg ii;
						~listeaudiofreq.wrapPut(ii,datas.wrapAt(ii*24+0)/127);~listeaudioamp.wrapPut(ii,datas.wrapAt(ii*24+1).dbamp);~listeaudioduree.wrapPut(ii,datas.wrapAt(ii*24+2));
						~listemidifreq.wrapPut(ii,datas.wrapAt(ii*24+3)/127);~listemidiamp.wrapPut(ii,datas.wrapAt(ii*24+4).dbamp);~listemididuree.wrapPut(ii,datas.wrapAt(ii*24+5));
						~listechaosfreq.wrapPut(ii,datas.wrapAt(ii*24+6)/127);~listechaosamp.wrapPut(ii,datas.wrapAt(ii*24+7).dbamp);~listechaosduree.wrapPut(ii,datas.wrapAt(ii*24+8));
						~listeoutneuronefreq.wrapPut(ii,datas.wrapAt(ii*24+9)/127);~listeoutneuroneamp.wrapPut(ii,datas.wrapAt(ii*24+10).dbamp);~listeoutneuroneduree.wrapPut(ii,datas.wrapAt(ii*24+11));
						~listegenetiquefreq.wrapPut(ii,datas.wrapAt(ii*24+12)/127);~listegenetiqueamp.wrapPut(ii,datas.wrapAt(ii*24+13).dbamp);~listegenetiqueduree.wrapPut(ii,datas.wrapAt(ii*24+14));
						~listealgo1freq.wrapPut(ii,datas.wrapAt(ii*24+15)/127);~listealgo1amp.wrapPut(ii,datas.wrapAt(ii*24+16).dbamp);~listealgo1duree.wrapPut(ii,datas.wrapAt(ii*24+17));
						~listealgo2freq.wrapPut(ii,datas.wrapAt(ii*24+18)/127);~listealgo2amp.wrapPut(ii,datas.wrapAt(ii*24+19).dbamp);~listealgo2duree.wrapPut(ii,datas.wrapAt(ii*24+20));
						~listeofffreq.wrapPut(ii,datas.wrapAt(ii*24+21)/127);~listeoffamp.wrapPut(ii,datas.wrapAt(ii*24+22).dbamp);~listeoffduree.wrapPut(ii,datas.wrapAt(ii*24+23));
			})})})});
		};

		~foncpart={arg i, item;
			var file, datas, datasfile;
			item = item.value.asInteger;
			if(item == 0 ,{nil},{if(File.exists(~nompathdata++"score"+item.asString++".scd"),{file=File(~nompathdata++"score"+item.asString++".scd","r");datas=file.readAllString.interpret;file.close;
				~listeplaypart.wrapPut(item-1,datas);
				~flagplaypart.wrapPut(item-1,0);
				~dureeplaypart.wrapPut(item-1,0);
			})});
			//~saveloaddatasinstrument.wrapAt(i).value=0;
		};

		~slcinstruments={arg i,item;
			var file, datas, datasfile=[], addrM, addrS;
			switch(item.value,
				{0}, {nil},
				{3}, {Dialog.savePanel({arg path;
					datasfile=[];
					datasfile=datasfile.add(~fonctionsavedatasinstrument.value(i));
					file=File(path++".scd","w");file.write(datasfile.value.asCompileString);file.close;~listewindow.wrapAt(i).name="WekRobot by HP Instrument"+(i+1).asString+path.asString},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{1}, {Dialog.openPanel({ arg paths;
					file=File(paths, "r");datas=file.readAllString.interpret;file.close;
					~fonctionloaddatasinstrument.value(i, datas.wrapAt(0),'on');~listewindow.wrapAt(i).name="WekRobot by HP Instrument"+(i+1).asString+paths.asString},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{4}, {Dialog.savePanel({arg path;
					datasfile=[];
					for(0, ~nombreinstrument-1, {arg ii;datasfile=datasfile.add(~fonctionsavedatasinstrument.value(ii));~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+path.asString});
					datasfile=datasfile.add([~savecontrolpanel.value]);
					file=File(path++".scd","w");file.write(datasfile.value.asCompileString);file.close},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{2}, {Dialog.openPanel({ arg paths;
					file=File(paths, "r");datas=file.readAllString.interpret;file.close;
					for(0, ~nombreinstrument-1, {arg ii;
						~fonctionloaddatasinstrument.value(ii, datas.wrapAt(ii),'on');~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+paths.asString});
					//+ Load datas Control Panel
					~readcontrolpanel.value(datas.last.at(0));
				},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{11}, {if(File.exists(~nompathdata++"clear instrument.scd"),{file=File(~nompathdata++"clear instrument.scd", "r");datas=file.readAllString.interpret;file.close;~fonctionloaddatasinstrument.value(i, datas.wrapAt(0),'off');~listewindow.wrapAt(i).name="WekRobot by HP Instrument"+(i+1).asString+"Clear Instrument"});~saveloaddatasinstrument.wrapAt(i).value=0},
				{12}, {if(File.exists(~nompathdata++"clear instruments.scd"),{file=File(~nompathdata++"clear instruments.scd", "r");datas=file.readAllString.interpret;file.close;
					16.do({arg canal; ~midiOut.allNotesOff(canal)});
					for(0, ~nombreinstrument-1, {arg ii;
						~fonctionloaddatasinstrument.value(ii, datas.wrapAt(ii),'on');~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+"Clear Instruments"})});~saveloaddatasinstrument.wrapAt(i).value=0},
				{5}, {Dialog.openPanel({ arg paths;
					file=File(paths, "r");datas=file.readAllString.interpret;file.close;
					~listeaudiofreq.wrapPut(i,datas.wrapAt(0)/127);~listeaudioamp.wrapPut(i,datas.wrapAt(1).dbamp);~listeaudioduree.wrapPut(i,datas.wrapAt(2));
					~listemidifreq.wrapPut(i,datas.wrapAt(3)/127);~listemidiamp.wrapPut(i,datas.wrapAt(4).dbamp);~listemididuree.wrapPut(i,datas.wrapAt(5));
					~listechaosfreq.wrapPut(i,datas.wrapAt(6)/127);~listechaosamp.wrapPut(i,datas.wrapAt(7).dbamp);~listechaosduree.wrapPut(i,datas.wrapAt(8));
					~listeoutneuronefreq.wrapPut(i,datas.wrapAt(9)/127);~listeoutneuroneamp.wrapPut(i,datas.wrapAt(10).dbamp);~listeoutneuroneduree.wrapPut(i,datas.wrapAt(11));
					~listegenetiquefreq.wrapPut(i,datas.wrapAt(12)/127);~listegenetiqueamp.wrapPut(i,datas.wrapAt(13).dbamp);~listegenetiqueduree.wrapPut(i,datas.wrapAt(14));
					~listealgo1freq.wrapPut(i,datas.wrapAt(15)/127);~listealgo1amp.wrapPut(i,datas.wrapAt(16).dbamp);~listealgo1duree.wrapPut(i,datas.wrapAt(17));
					~listealgo2freq.wrapPut(i,datas.wrapAt(18)/127);~listealgo2amp.wrapPut(i,datas.wrapAt(19).dbamp);~listealgo2duree.wrapPut(i,datas.wrapAt(20));
					~listeofffreq.wrapPut(i,datas.wrapAt(21)/127);~listeoffamp.wrapPut(i,datas.wrapAt(22).dbamp);~listeoffduree.wrapPut(i,datas.wrapAt(23))},{"cancelled".postln});
				~saveloaddatasinstrument.wrapAt(i).value=0},
				{6}, {Dialog.openPanel({ arg paths;
					file=File(paths, "r");datas=file.readAllString.interpret;file.close;
					for(0, ~nombreinstrument-1, {arg ii;
						~listeaudiofreq.wrapPut(ii,datas.wrapAt(ii*24+0)/127);~listeaudioamp.wrapPut(ii,datas.wrapAt(ii*24+1).dbamp);~listeaudioduree.wrapPut(ii,datas.wrapAt(ii*24+2));
						~listemidifreq.wrapPut(ii,datas.wrapAt(ii*24+3)/127);~listemidiamp.wrapPut(ii,datas.wrapAt(ii*24+4).dbamp);~listemididuree.wrapPut(ii,datas.wrapAt(ii*24+5));
						~listechaosfreq.wrapPut(ii,datas.wrapAt(ii*24+6)/127);~listechaosamp.wrapPut(ii,datas.wrapAt(ii*24+7).dbamp);~listechaosduree.wrapPut(ii,datas.wrapAt(ii*24+8));
						~listeoutneuronefreq.wrapPut(ii,datas.wrapAt(ii*24+9)/127);~listeoutneuroneamp.wrapPut(ii,datas.wrapAt(ii*24+10).dbamp);~listeoutneuroneduree.wrapPut(ii,datas.wrapAt(ii*24+11));
						~listegenetiquefreq.wrapPut(ii,datas.wrapAt(ii*24+12)/127);~listegenetiqueamp.wrapPut(ii,datas.wrapAt(ii*24+13).dbamp);~listegenetiqueduree.wrapPut(ii,datas.wrapAt(ii*24+14));
						~listealgo1freq.wrapPut(ii,datas.wrapAt(ii*24+15)/127);~listealgo1amp.wrapPut(ii,datas.wrapAt(ii*24+16).dbamp);~listealgo1duree.wrapPut(ii,datas.wrapAt(ii*24+17));
						~listealgo2freq.wrapPut(ii,datas.wrapAt(ii*24+18)/127);~listealgo2amp.wrapPut(ii,datas.wrapAt(ii*24+19).dbamp);~listealgo2duree.wrapPut(ii,datas.wrapAt(ii*24+20));
						~listeofffreq.wrapPut(ii,datas.wrapAt(ii*24+21)/127);~listeoffamp.wrapPut(ii,datas.wrapAt(ii*24+22).dbamp);~listeoffduree.wrapPut(ii,datas.wrapAt(ii*24+23));
					});
				},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{7}, {Dialog.savePanel({ arg path;datasfile=[];
					datasfile=datasfile.add(~listeaudiofreq.wrapAt(i)*127);datasfile=datasfile.add(~listeaudioamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeaudioduree.wrapAt(i));
					datasfile=datasfile.add(~listemidifreq.wrapAt(i)*127);datasfile=datasfile.add(~listemidiamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listemididuree.wrapAt(i));
					datasfile=datasfile.add(~listechaosfreq.wrapAt(i)*127);datasfile=datasfile.add(~listechaosamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listechaosduree.wrapAt(i));
					datasfile=datasfile.add(~listeoutneuronefreq.wrapAt(i)*127);datasfile=datasfile.add(~listeoutneuroneamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoutneuroneduree.wrapAt(i));
					datasfile=datasfile.add(~listegenetiquefreq.wrapAt(i)*127);datasfile=datasfile.add(~listegenetiqueamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listegenetiqueduree.wrapAt(i));
					datasfile=datasfile.add(~listealgo1freq.wrapAt(i)*127);datasfile=datasfile.add(~listealgo1amp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo1duree.wrapAt(i));
					datasfile=datasfile.add(~listealgo2freq.wrapAt(i)*127);datasfile=datasfile.add(~listealgo2amp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo2duree.wrapAt(i));
					datasfile=datasfile.add(~listeofffreq.wrapAt(i)*127);datasfile=datasfile.add(~listeoffamp.wrapAt(i).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoffduree.wrapAt(i));
					file=File(path++".scd","w");file.write(datasfile.value.asCompileString);file.close},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{8}, {Dialog.savePanel({arg path;datasfile=[];
					for(0, ~nombreinstrument-1, {arg ii;
						datasfile=datasfile.add(~listeaudiofreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeaudioamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeaudioduree.wrapAt(ii));
						datasfile=datasfile.add(~listemidifreq.wrapAt(ii)*127);datasfile=datasfile.add(~listemidiamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listemididuree.wrapAt(ii));
						datasfile=datasfile.add(~listechaosfreq.wrapAt(ii)*127);datasfile=datasfile.add(~listechaosamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listechaosduree.wrapAt(ii));
						datasfile=datasfile.add(~listeoutneuronefreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeoutneuroneamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoutneuroneduree.wrapAt(ii));
						datasfile=datasfile.add(~listegenetiquefreq.wrapAt(ii)*127);datasfile=datasfile.add(~listegenetiqueamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listegenetiqueduree.wrapAt(ii));
						datasfile=datasfile.add(~listealgo1freq.wrapAt(ii)*127);datasfile=datasfile.add(~listealgo1amp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo1duree.wrapAt(ii));
						datasfile=datasfile.add(~listealgo2freq.wrapAt(ii)*127);datasfile=datasfile.add(~listealgo2amp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo2duree.wrapAt(ii));
						datasfile=datasfile.add(~listeofffreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeoffamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoffduree.wrapAt(ii));
					});
					file=File(path++".scd","w");file.write(datasfile.value.asCompileString);file.close},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{13}, {if(File.exists(~nompathdata++"clear control panel.scd"),{file=File(~nompathdata++"clear control panel.scd", "r");datas=file.readAllString.interpret;file.close;~readcontrolpanel.value(datas.at(0))});~saveloaddatasinstrument.wrapAt(i).value=0},
				{9}, {Dialog.openPanel({ arg paths;
					file=File(paths, "r");datas=file.readAllString.interpret;file.close;
					40.do({arg i;~listeplaypart.wrapPut(i,datas.wrapAt(i));~dureeplaypart.wrapPut(i,0);~pointeurplaypart.wrapPut(i,0)})},{"cancelled".postln});
				~saveloaddatasinstrument.wrapAt(i).value=0},
				{10}, {Dialog.savePanel({ arg path;datasfile=[];
					40.do({arg i;datasfile=datasfile.add(~listerecpart.wrapAt(i))})
					;file=File(path++".scd","w");file.write(datasfile.value.asCompileString);file.close},{"cancelled".postln});~saveloaddatasinstrument.wrapAt(i).value=0},
				{14}, {//set freeze buffer on
					~flagBufferFreeze.wrapPut(i, 'Freeze buffer on');
				},
				{15}, {//set freeze buffer off
					~flagBufferFreeze.wrapPut(i, 'Freeze buffer off');
				},
				{16}, {//set freeze buffers on
					~nombreinstrument.do({arg instr;
						~flagBufferFreeze.wrapPut(instr, 'Freeze buffer on');
					});
				},
				{17}, {//set freeze buffers off
					~nombreinstrument.do({arg instr;
						~flagBufferFreeze.wrapPut(instr, 'Freeze buffer off');
					});
				},
				// Load file pour analyse
				{18}, {Dialog.openPanel({ arg paths;
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
						~file = SoundFile.new;
						s.sync;
						~file.openRead(~samplePourAnalyse);
						s.sync;
						if(~file.numChannels == 1,
							{~rawData= FloatArray.newClear(~file.numFrames * 2);
								s.sync;
								~file.readData(~rawData);
								s.sync;
								Post << "Loading mono sound" << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~rawData = Array.newFrom(~rawData).stutter(2) / 2;
								s.sync;
								~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2);
								s.sync;
							},
							{Post << "Loading stereo sound" << " " << ~samplePourAnalyse << Char.nl;
								s.sync;
								~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1]);
								s.sync;
						});
						//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
						~file.close;
						s.sync;
						~synthPlayFile.set(\bufferplay, ~bufferanalysefile.bufnum);
						if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File IN'}, {~synthPlayFile.run(true);
							s.sync;~synthPlayFile.set('trig', 1);s.sync});
					};
				},{"cancelled".postln})},
				{19}, {
					//New synth environment
					"No Operate".postln;
				},
				{20}, {
					//New sound environment
					"Not Operate".postln;
				},
				{21}, {
					// New all environment
					"No Operate".postln;
				},
				// OSC Off
				{22}, {~oscStateFlag='off';~stateOSCdisplay.string = "OSC Off"},
				//OSC Master
				{23}, {~oscStateFlag='master';~stateOSCdisplay.string = "OSC Master"},
				//OSC Slave
				{24}, {~oscStateFlag='slave';~stateOSCdisplay.string = "OSC Slave"},
				// OSC Settings
				{25}, {
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
			});
			//~saveloaddatasinstrument.wrapAt(i).value=0;
		};

		this.createScore;// score fonction

		this.shortCuts;//raccourcis clavier

		~cmdperiodfunc = {
			if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
			MIDIIn.disconnect;
			MIDIdef.freeAll;
			~listewindow.do({arg w; w.close});
			windowVST.close;
			ProxySpace.clearAll;
		};

		//Load partitions
		~loadpartitions={arg f, d;// Pour charger les parts en fonction du path
			40.do({arg i;
				if(File.exists(~nompathdata++"score"+(i+1).asString++".scd"),
					{f=File(~nompathdata++"score"+(i+1).asString++".scd", "r");d=f.readAllString.interpret;f.close;
						~listeplaypart.wrapPut(i,d);~listerecpart.wrapPut(i,d);
						~flagplaypart.wrapPut(i,0);~dureeplaypart.wrapPut(i,0);},{~listeplaypart.wrapPut(i,[])})});
		};

		// Load last work
		~loadlastwork={arg f, d;// Pour charger last work en fonction du path
			if(File.exists(~nompathdata++"last work.scd"), {f=File(~nompathdata++"last work.scd", "r");d=f.readAllString.interpret;f.close;
				//+ Load datas Control Panel
				~readcontrolpanel.value(d.last.at(0));
				for(0, ~nombreinstrument-1, {arg ii;
					~fonctionloaddatasinstrument.value(ii, d.wrapAt(ii),'on')});
			},
			{if(File.exists(~nompathdata++"clear instruments.scd"), {f=File(~nompathdata++"clear instruments.scd", "r");d=f.readAllString.interpret;f.close;
				//+ Load datas Control Panel
				~readcontrolpanel.value(d.last.at(0));
				for(0, ~nombreinstrument-1, {arg ii;
					~fonctionloaddatasinstrument.value(ii, d.wrapAt(ii),'on')});
			});
			// Save last work
			d=[];
			for(0, ~nombreinstrument-1, {arg ii;
				d=d.add(~fonctionsavedatasinstrument.value(ii))});
			d=d.add([~savecontrolpanel.value]);
			f=File(~nompathdata++"last work.scd","w");f.write(d.value.asCompileString);f.close;
			});
		};

		CmdPeriod.doOnce(~cmdperiodfunc);

		~wg.front;

		~createClearFiles={arg f, d;
			// Create clear control panel
			f=File(~nompathdata++"clear control panel.scd","w");f.write([~savecontrolpanel.value].asCompileString);f.close;
			// Create clear instrument
			d=[];
			d=d.add(~fonctionsavedatasinstrument.value(0));
			f=File(~nompathdata++"clear instrument.scd","w");f.write(d.value.asCompileString);f.close;
			//Create clear instruments
			d=[];
			for(0, ~nombreinstrument-1, {arg ii;d=d.add(~fonctionsavedatasinstrument.value(ii))});
			d=d.add([~savecontrolpanel.value]);
			f=File(~nompathdata++"clear instruments.scd","w");f.write(d.value.asCompileString);f.close;
		};

		~createClearFiles.value;
		~loadpartitions.value;
		//~loadlastwork.value;

		~audioIn.value.run(false); ~audioFile.value.run(false); ~synthPlayFile.value.run(false);
		~fonctionOSCMidiIn.value;

		//TdefAllGui(12);
		s.queryAllNodes;

	}

	kill {
		// Kill instance of Class
		var datasfile=[], file;
		if(~flagRecording == 'on', {s.stopRecording;~bufferRecording.close;~bufferRecording.free});
		// Close partitions rec
		40.do({arg i;var p;
			if(~flagrecpart.wrapAt(i)==1,{p=~listerecpart.wrapAt(i);p=p.add(~dureerecpart.wrapAt(i));p=p.add(""++$\r);p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');~listerecpart.wrapPut(i ,p);~listeplaypart.wrapPut(i,p)});if(~listeplaypart.wrapAt(i).size >= 1, {file=File(~nompathdata++"score"+(i+1).asString++".scd","w");file.write(~listeplaypart.wrapAt(i).value.asCompileString);file.close});
		});
		//// Save last work
		~menuWekRobot.remove;
		~nombreinstrument.do({arg i; ~listewindow.wrapAt(i).close});
		~wg.close;~wp.close;~windowMasterFX.close;windowKeyboard.close;
		if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
		MIDIIn.disconnect;
		~serverAdresse.disconnect;
		if(~masterAppAddr != nil, {~masterAppAddr.disconnect});
		~slaveAppAddr.disconnect;
		~ardourOSC.disconnect;
		~oscAudioIn.free;
		~oscTempoIn.free;
		~oscMidiIn.free;
		~oscHPtempo.free;
		~oscHPstart.free;
		~oscHPbare.free;
		~oscHPrec.free;
		Tdef.removeAll;
		s.freeAll;
	}

	creationGUI {

		////////////////////////// Window VST ///////////////////////////////
		windowVST = Window.new("VST Stereo", Rect(710, 650, 320, 80), scroll: true);
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

		////////////////////////// Window Keyboard ///////////////////////////////
		windowKeyboard = Window.new("Keyboard", Rect(10, 25, 625, 130), scroll: true);
		windowKeyboard.view.decorator = FlowLayout(windowKeyboard.view.bounds);
		windowKeyboard.front;
		// Setup ShortCut
		setupKeyboardShortCut = Button(windowKeyboard, Rect(0, 0, 105, 20));
		setupKeyboardShortCut.states = [["Keyboard Shortcut", Color.green], ["System Shortcut", Color.red]];
		setupKeyboardShortCut.action = {arg shortcut;
			if(shortcut.value == 1, {keyboardShortCut.value(windowKeyboard);
				forBy(60 + keyboardTranslate.value, 76 + keyboardTranslate.value, 1, {arg note; keyboard.setColor(note, Color.blue)})},
			{
				~fonctionShortCuts.value(windowKeyboard);
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

		// PARTITIONS PANEL
		~wp =Window("WekRobot by HP Scores", Rect(475, 275, 555, 465), scroll: true);
		~wp.alpha=1.0;
		~wp.front;
		~wp.view.decorator = FlowLayout(~wp.view.bounds);
		40.do({arg i;
			~recpartbutton = ~recpartbutton.add(Button(~wp,Rect(0,0,85,18)));
			~recpartbutton.wrapAt(i).states = [["Rec"+(i+1).asString+"Off", Color.black,  Color.green(0.8, 0.25)],["Rec"+(i+1).asString+"On", Color.white, Color.red(0.8, 0.25)]];
			~recpartbutton.wrapAt(i).action = {|view| var p;
				if(view.value == 1, {~tempoSystem.schedAbs(~tempoSystem.beats, {
					{~dureerecpart.wrapPut(i, 0);~listerecpart.wrapPut(i,[]);~flagrecpart.wrapPut(i,view.value)}.defer;nil})},
				{p=~listerecpart.wrapAt(i);p=p.add(~dureerecpart.wrapAt(i));p=p.add(""++$\r);p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');p=p.add('end');~listeplaypart.wrapPut(i,p)});
			};
			~playpartbutton = ~playpartbutton.add(Button(~wp,Rect(0,0,85,18)));
			~playpartbutton.wrapAt(i).states = [["Play"+(i+1).asString+"Off", Color.black, Color.grey],["Play"+(i+1).asString+"On", Color.white, Color.red(0.8, 0.25)]];
			~playpartbutton.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~playpartbutton",view.value);
				if(view.value == 0, {~flagplaypart.wrapPut(i,view.value);~dureeplaypart.wrapPut(i,0);~pointeurplaypart.wrapPut(i,0)},
					{~tempoSystem.schedAbs(~tempoSystem.beats, {
						{~flagplaypart.wrapPut(i,view.value);~dureeplaypart.wrapPut(i,0);~pointeurplaypart.wrapPut(i,0)}.defer;nil});
				});
			};
			~looppartbutton = ~looppartbutton.add(Button(~wp,Rect(0,0,85,18)));
			~looppartbutton.wrapAt(i).states = [["Loop"+(i+1).asString+"Off", Color.black, Color.white],["Loop"+(i+1).asString+"On", Color.white, Color.red(0.8, 0.25)]];
			~looppartbutton.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~looppartbutton",view.value);~flaglooppart.wrapPut(i,view.value)};
		});
		~playpartbutton.wrapAt(0).focus;

		// MasterFX
		~windowMasterFX = Window.new("MasterFX", Rect(0, 50, 260, 60), scroll: true);
		~windowMasterFX.view.decorator = FlowLayout(~windowMasterFX.view.bounds);
		~windowMasterFXLimit =EZSlider(~windowMasterFX, 245 @ 18, "LimitOut",\db,
			{|ez| ~masterFX.set(\limit, ez.value.dbamp);~writepartitions.value(nil,'masterFX','off',"~windowMasterFXLimit",ez.value)},-3,labelWidth: 60,numberWidth: 40);
		~windowMasterFX.view.decorator.nextLine;
		~windowMasterFXPostAmp = EZSlider(~windowMasterFX, 245 @ 18, "PostAmp", \db,
			{|ez| ~masterFX.set(\postAmp, ez.value.dbamp);~writepartitions.value(nil,'masterFX','off',"~windowMasterFXPostAmp",ez.value)}, 0,labelWidth: 60,numberWidth: 40);
		~windowMasterFX.view.decorator.nextLine;
		~windowMasterFX.front;

		// Windows instruments
		~nombreinstrument.do({arg i;
			var w;
			w = Window("WekRobot by HP Instrument"+(i+1).asString, Rect(i*20, 200 - (i*33.33), 905, 525), scroll: true);
			w.alpha=1.0;w.front;
			w.view.decorator = FlowLayout(w.view.bounds);
			StaticText(w, Rect(0, 0, 890, 12)).string_("SYSTEM PARAMETERS").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			w.view.decorator.nextLine;
			// Bouton start stop playing
			~startbutton = ~startbutton.add(Button(w,Rect(0,0,50,18)));
			~startbutton.wrapAt(i).states = [["Play Off", Color.black,  Color.green(0.8, 0.25)],["Play On", Color.white, Color.red(0.8, 0.25)]
			];
			~startbutton.wrapAt(i).action = {arg view; var duree;
				~writepartitions.value(i,'normal','off',"~startbutton",view.value);
				if(~flagSynchro == 'on', {duree = ~tempoSystem.nextBar}, {duree=~tempoSystem.beats});
				if(~startsysteme.value==1 and: {view.value == 1}, {
					~tempoSystem.schedAbs(duree, {
						~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
						~routineinstrument.wrapAt(i).play(quant: Quant.new(1)); nil});
				},
				{~routineinstrument.wrapAt(i).stop;~duree.wrapPut(i, 0);
					// Set MIDI Off
					if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
						~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
							if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
						});
					});
				});
			};
			~startbutton.wrapAt(i).focus;
			// Set canal audio de sortie
			~sonout=~sonout.add(PopUpMenu(w,Rect(0,0,90,18)).items = ~canalout);
			~sonout.wrapAt(i).action = {arg canal;~writepartitions.value(i,'normal','off',"~sonout",canal.value);~canalaudioout.wrapPut(i,canal.value)};
			// Midi
			~canalmidiinview=~canalmidiinview.add(PopUpMenu(w,Rect(0,0,90,18)).items = ~midicanalin);
			~canalmidiinview.wrapAt(i).action = { arg canal; ~writepartitions.value(i,'normal','off',"~canalmidiinview",canal.value);~canalmidiin.wrapPut(i, canal.value);
				NoteOnResponder.removeAll;~fonctionOSCMidiIn.value};
			//Chaos F
			~kchaosviewfreq=~kchaosviewfreq.add(EZSlider(w, 200 @ 18, "Chaos Fhz",ControlSpec(2.96, 4, \exp),
				{|ez| ~writepartitions.value(i,'normal','off',"~kchaosviewfreq",ez.value);~instanceChaosF.wrapAt(i).init(ez.value)},
				3.852,labelWidth: 75,numberWidth: 50));
			//Chaos A
			~kchaosviewamp=~kchaosviewamp.add(EZSlider(w, 200 @ 18, "Chaos Amp",ControlSpec(1, 4, \exp),
				{|ez| ~writepartitions.value(i,'normal','off',"~kchaosviewamp",ez.value);~instanceChaosA.wrapAt(i).init(ez.value)},
				3.852,labelWidth: 75,numberWidth: 50));
			// chaos d
			~kchaosviewduree=~kchaosviewduree.add(EZSlider(w, 200 @ 18, "Chaos Dur",ControlSpec(3, 4, \exp, 0),
				{|ez| ~writepartitions.value(i,'normal','off',"~kchaosviewdur",ez.value);~instanceChaosD.wrapAt(i).init(ez.value)},
				3.852,labelWidth: 75,numberWidth: 50));
			w.view.decorator.nextLine;
			StaticText(w, Rect(0, 0, 890, 12)).string_("FILTER ANALYSE").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			w.view.decorator.nextLine;
			// inter fhz
			~differencefreqinstrument = ~differencefreqinstrument.add(EZSlider(w, 200 @ 18, "Dist Freq",ControlSpec(0.0, 12.0, \lin, 0.0),
				{|ez| ~writepartitions.value(i,'normal','off',"~differencefreqinstrument",ez.value);~differencefreq.wrapPut(i,ez.value)},
				0.5,labelWidth: 65,numberWidth: 40));
			// diff amp
			~differenceampinstrument = ~differenceampinstrument.add(EZSlider(w, 200 @ 18, "Dist Amp",ControlSpec(0.0, 60.0, \lin, 0.0),
				{|ez| ~writepartitions.value(i,'normal','off',"~differenceampinstrument",ez.value);~differenceamp.wrapPut(i,ez.value)},
				1,labelWidth: 65,numberWidth: 40));
			// diff duree
			~differencedureeinstrument = ~differencedureeinstrument.add(EZSlider(w, 200 @ 18, "Dist Dur",ControlSpec(0.01, 16, \exp, 0),
				{|ez| ~writepartitions.value(i,'normal','off',"~differencedureeinstrument",ez.value);~differenceduree.wrapPut(i,ez.value)},
				0.0625,labelWidth: 65,numberWidth: 40));
			~dureeanalysesilence = ~dureeanalysesilence.add(EZSlider(w, 200 @ 18, "Memory Time",ControlSpec(0.01, 3600, \exp),
				{|ez| ~writepartitions.value(i,'normal','off',"~dureeanalysesilence",ez.value);~dureeanalysesil.wrapPut(i,ez.value)},
				4,labelWidth: 65,numberWidth: 35));
			w.view.decorator.nextLine;
			~dureeanalysemaxinstrument = ~dureeanalysemaxinstrument.add(EZSlider(w, 200 @ 18, "Max Dur",ControlSpec(0.01, 60, \exp),
				{|ez| ~writepartitions.value(i,'normal','off',"~dureeanalysemaxinstrument",ez.value);~dureeanalysemax.wrapPut(i,ez.value);
					if(ez.value > ~dureeanalysesil.wrapAt(i), {~dureeanalysesil.wrapPut(i, ez.value);~dureeanalysesilence.wrapAt(i).value_(ez.value)})},
				4, false, 65, 35));
			// Chords
			~maxaccordinstrument = ~maxaccordinstrument.add(EZSlider(w, 200 @ 18, "Chord",ControlSpec(0, 12, \lin, 1),
				{|ez| ~writepartitions.value(i,'normal','off',"~maxaccordinstrument",ez.value);~maxaccord.wrapPut(i,ez.value)},
				6,labelWidth: 65,numberWidth: 20));
			~dureeaccordinstrument = ~dureeaccordinstrument.add(EZSlider(w, 200 @ 18, "ChordDur",ControlSpec(0.01, 1.0, \exp),
				{|ez| ~writepartitions.value(i,'normal','off',"~dureeaccordinstrument",ez.value);~dureeaccord.wrapPut(i,ez.value)},
				(1/12),labelWidth: 65,numberWidth: 35));
			// Datas
			~datassizeinstrument = ~datassizeinstrument.add(EZSlider(w, 200 @ 18, "MaxDatas",ControlSpec(1, 256, \lin, 1),
				{|ez| ~writepartitions.value(i,'normal','off',"~datassizeinstrument",ez.value);~listedatassize.wrapPut(i,ez.value)},
				24,labelWidth: 65,numberWidth: 25));
			w.view.decorator.nextLine;
			StaticText(w, Rect(0, 0, 890, 12)).string_("ALGORITHM PARAMETERS").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			w.view.decorator.nextLine;
			//neurone button
			~neuroneviewmode = ~neuroneviewmode.add(Button(w,Rect(0,0,115,18)));
			~neuroneviewmode.wrapAt(i).states = [["Neurone Rec Off", Color.black,  Color.green(0.8, 0.25)],["Neurone Rec On", Color.white, Color.red(0.8, 0.25)]];
			~neuroneviewmode.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~neuroneviewmode",view.value);if (view.value == 1, {~neuronemode.wrapPut(i,'rec')},
				{~neuronemode.wrapPut(i,'off')})};
			// Init Neurones
			~initneurones = ~initneurones.add(Button(w,Rect(0,0,115,18)));
			~initneurones.wrapAt(i).states = [["Init Weight/Thresh", Color.black,  Color.green(0.8, 0.25)],["Init Weight/Thresh", Color.white, Color.red(0.8, 0.25)]];
			~initneurones.wrapAt(i).action = {|view| var x,y;
				~writepartitions.value(i,'normal','off',"~initneurones",view.value);
				if (view.value == 1, {~instanceNeurones.wrapAt(i).init(3, ~couchecachee, 3)},{nil});
				~initneurones.wrapAt(i).value=0};
			// Rec patterns Neurones out pour algo Neurone2
			~initpatternneurones = ~initpatternneurones.add(Button(w,Rect(0,0,115,18)));
			~initpatternneurones.wrapAt(i).states = [["Rec Pattern", Color.black,  Color.green(0.8, 0.25)],["Rec Pattern", Color.white, Color.red(0.8, 0.25)]];
			~initpatternneurones.wrapAt(i).action = {|view|
				~writepartitions.value(i,'normal','off',"~initpatternneurones",view.value);
				if (view.value == 1, {~listeoutneuronefreq.wrapPut(i,~listeinfreq.wrapAt(i));~listeoutneuroneamp.wrapPut(i,~listeinamp.wrapAt(i));~listeoutneuroneduree.wrapPut(i,~listeinduree.wrapAt(i));
					~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0)},{nil});
				~initpatternneurones.wrapAt(i).value=0};
			// neurone apprentissage
			~neuroneviewapprentissage=~neuroneviewapprentissage.add(EZSlider(w, 275 @ 18, "Neuron Learning", ControlSpec(0 , 1, \lin,0),
				{|ez| ~writepartitions.value(i,'normal','off',"~neuroneviewapprentissage",ez.value);~neuroneapprentissage.wrapPut(i,ez.value)},
				0.5,labelWidth: 130,numberWidth: 40));
			// neurone temperature
			~neuroneviewtemperature=~neuroneviewtemperature.add(EZSlider(w, 250 @ 18, "Neuron Temperature", ControlSpec(0.1 , 1, \lin,0),
				{|ez| ~writepartitions.value(i,'normal','off',"~neuroneviewtemperature",ez.value);~neuronetemperature.wrapPut(i,ez.value)},
				0.5,labelWidth: 125,numberWidth: 40));
			w.view.decorator.nextLine;
			// genetic button
			~genetiqueviewmode = ~genetiqueviewmode.add(Button(w,Rect(0,0,110,18)));
			~genetiqueviewmode.wrapAt(i).states = [["Genetic Rec Off", Color.black,  Color.green(0.8, 0.25)],["GeneticRec On", Color.white, Color.red(0.8, 0.25)]
			];
			~genetiqueviewmode.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~genetiqueviewmode",view.value);if (view.value == 1, {~genetiquemode.wrapPut(i,'rec')},
				{~genetiquemode.wrapPut(i,'off')})};
			// Init Genetiques
			~initgenetiques = ~initgenetiques.add(Button(w,Rect(0,0,125,18)));
			~initgenetiques.wrapAt(i).states = [["Init Algo Population", Color.black,  Color.green(0.8, 0.25)],["Init Algo Population", Color.white, Color.red(0.8, 0.25)]];
			~initgenetiques.wrapAt(i).action = {|view| var x;~writepartitions.value(i,'normal','off',"~initgenetiques",view.value);if (view.value == 1, {~instanceGenetiques.wrapAt(i).init(3,~listedatassize.wrapAt(i)); ~instanceAlgo2.wrapAt(i).init(s: ~listedatassize.wrapAt(i))},{nil});
				~initgenetiques.wrapAt(i).value=0};
			// genetic croisement
			~genetiqueviewcroisement=~genetiqueviewcroisement.add(EZSlider(w, 250 @ 18, "Genetic Crossing", ControlSpec(0 , 1, \lin,0),
				{|ez| ~writepartitions.value(i,'normal','off',"~genetiqueviewcroisement",ez.value);~genetiquecroisement.wrapPut(i,ez.value)},
				0.5,labelWidth: 115,numberWidth: 40));
			// genetic mutation
			~genetiqueviewmutation=~genetiqueviewmutation.add(EZSlider(w, 250 @ 18, "Genetic Mutation", ControlSpec(0 , 1, \lin,0),
				{|ez| ~writepartitions.value(i,'normal','off',"~genetiqueviewmutation",ez.value);~genetiquemutation.wrapPut(i,ez.value)},
				0.05,labelWidth: 100,numberWidth: 40));
			w.view.decorator.nextLine;
			// SET FHZ IN
			~choixdatasinfreqview=~choixdatasinfreqview.add(PopUpMenu(w,Rect(0,0,140,18)).items = ~choixdatasinfreq);
			~choixdatasinfreqview.wrapAt(i).action = {arg in;
				~writepartitions.value(i,'normal','off',"~choixdatasinfreqview",in.value);
				~indatasfreqinstrument.wrapPut(i,~choixdatasinfreq.wrapAt(in.value))};
			// SET AMP IN
			~choixdatasinampview=~choixdatasinampview.add(PopUpMenu(w,Rect(0,0,140,18)).items = ~choixdatasinamp);
			~choixdatasinampview.wrapAt(i).action = {arg in;
				~writepartitions.value(i,'normal','off',"~choixdatasinampview",in.value);
				~indatasampinstrument.wrapPut(i,~choixdatasinamp.wrapAt(in.value))};
			// SET DUREE IN
			~choixdatasindureeview=~choixdatasindureeview.add(PopUpMenu(w,Rect(0,0,140,18)).items = ~choixdatasinduree);
			~choixdatasindureeview.wrapAt(i).action = {arg in;
				~writepartitions.value(i,'normal','off',"~choixdatasindureeview",in.value);
				~indatasdureeinstrument.wrapPut(i,~choixdatasinduree.wrapAt(in.value))};
			// Algo freq
			~choixalgoviewfreq=~choixalgoviewfreq.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~algorithmesmusicauxfreq);
			~choixalgoviewfreq.wrapAt(i).action = {arg algo;	~writepartitions.value(i,'normal','off',"~choixalgoviewfreq",algo.value);~algoinstrumentfreq.wrapPut(i,~algorithmesmusicauxfreq.wrapAt(algo.value));
				if(algo.value == 1, {~flagalgoinstrneuronefreq.wrapPut(i,"NeuroneAlgo1")});
				if(algo.value == 2, {~flagalgoinstrneuronefreq.wrapPut(i,"NeuroneAlgo2")})};
			// Algo amp
			~choixalgoviewamp=~choixalgoviewamp.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~algorithmesmusicauxamp);
			~choixalgoviewamp.wrapAt(i).action = {arg algo;
				~writepartitions.value(i,'normal','off',"~choixalgoviewamp",algo.value);~algoinstrumentamp.wrapPut(i,~algorithmesmusicauxamp.wrapAt(algo.value));
				if(algo.value == 1, {~flagalgoinstrneuroneamp.wrapPut(i,"NeuroneAlgo1")});
				if(algo.value == 2, {~flagalgoinstrneuroneamp.wrapPut(i,"NeuroneAlgo2")})};
			// Algo duree
			~choixalgoviewduree=~choixalgoviewduree.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~algorithmesmusicauxduree);
			~choixalgoviewduree.wrapAt(i).action = {arg algo;	~writepartitions.value(i,'normal','off',"~choixalgoviewduree",algo.value);~algoinstrumentduree.wrapPut(i,~algorithmesmusicauxduree.wrapAt(algo.value));
				if(algo.value == 1, {~flagalgoinstrneuroneduree.wrapPut(i,"NeuroneAlgo1")});
				if(algo.value == 2, {~flagalgoinstrneuroneduree.wrapPut(i,"NeuroneAlgo2")})};
			w.view.decorator.nextLine;
			StaticText(w, Rect(0, 0, 890, 12)).string_("SYNTHESIZEUR").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			w.view.decorator.nextLine;
			// Set Synth
			~synthcontrol=~synthcontrol.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~listSynth);
			~synthcontrol.wrapAt(i).action = { arg synth;
				~numeroSynth.wrapPut(i, synth.value);
				~writepartitions.value(i,'normal','off',"~synthcontrol",synth.value);
				~listesynth.wrapPut(i,~listSynth.wrapAt(synth.value));
				~synthcontrolviewparametres.wrapAt(i).value_(~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).value);
				~automationinstrument.wrapAt(i).valueAction_(~randomValueSynth.wrapAt(i).wrapAt(synth.value).value);
				~automationparametres.wrapAt(i).valueAction_(~randomValueParametreSynth.wrapAt(i).wrapAt(synth.value).value);
				~synthsliderenvelope.wrapAt(i).valueAction_([~timeenvsynth.wrapAt(i).wrapAt(synth.value), ~levelenvsynth.wrapAt(i).wrapAt(synth.value)]);
				~synthpancontrol.wrapAt(i).valueAction_(~synthpancontrolValue.wrapAt(i).wrapAt(synth.value));
				~buscontrolsynth.wrapAt(i).set(~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(0), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(1), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(2), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(3), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(4), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(5), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(6), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(7), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(8), ~synthcontrolviewparametresdatas.wrapAt(i).wrapAt(synth.value).wrapAt(9))};
			// Set Son Sampler
			~soncontrol=~soncontrol.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~displaySons1);
			~soncontrol.wrapAt(i).action = {arg son;~writepartitions.value(i,'normal','off',"~soncontrol",son.value);~numerobuffer.wrapPut(i,son.value);
				~recsamplebutton.wrapAt(i).value=~recsamplebuttondatas.wrapAt(son.value);
				~looprecsamplebutton.wrapAt(i).value=~looprecsamplebuttondatas.wrapAt(son.value);
				~looprecordingValue.wrapPut(son.value, ~looprecsamplebuttondatas.wrapAt(son.value));
				~loopbutton.wrapAt(i).value=~loopplaysamplerdatas.wrapAt(i).wrapAt(son.value);
				if(~reversesampledatas.wrapAt(i).wrapAt(son.value) == 1 ,{~reversebutton.wrapAt(i).value=0},{~reversebutton.wrapAt(i).value=1});
				~synthcontrolviewlevels.wrapAt(i).value_(~synthcontrolviewlevelsdatas.wrapAt(son.value));
				~sampleroffsetcontrol.wrapAt(i).valueAction_(~sampleroffsetcontrolValue.wrapAt(i).wrapAt(son.value));
				~numRecLevel1.at(i).value = ~synthcontrolviewlevelsdatas.wrapAt(son.value).value.at(0);
				~numRecLevel2.at(i).value = ~synthcontrolviewlevelsdatas.wrapAt(son.value).value.at(1);
			};
			// Text
			StaticText(w, Rect(0,0, 75, 18)).string_(" <- Sample1").stringColor_(Color.yellow);
			// rec sample
			~recsamplebutton = ~recsamplebutton.add(Button(w,Rect( 0, 0, 58, 18)));
			~recsamplebutton.wrapAt(i).states = [["Rec off", Color.black,  Color.green(0.8, 0.25)],["Rec on", Color.white, Color.red(0.8, 0.25)]];
			~recsamplebutton.wrapAt(i).action = {|view| var level;
				~writepartitions.value(i,'rec sample',~numerobuffer.wrapAt(i),"~recsamplebutton",view.value);
				level=~synthcontrolviewlevels.wrapAt(i).value;
				if(~flagEntreeMode =='Audio IN', {
					//~listesamplein = ~listesampleinAudio;
					~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~canalAudioInInstr.at(i), \run, view.value, \trigger,  view.value,\loop,  ~looprecsamplebutton.wrapAt(i).value);
				},
				{
					//~listesampleinFile.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~busFileIn.index);
					//~listesamplein = ~listesampleinFile;
					~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~busFileIn.index, \run, view.value, \trigger,  view.value,\loop,  ~looprecsamplebutton.wrapAt(i).value)
				});
				~busreclevel.wrapAt(~numerobuffer.wrapAt(i)).set(level.wrapAt(0).value, level.wrapAt(1).value);
				// Copy buffer
				if(view.value == 1 ,{~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).copyData(~listebufferTampon.wrapAt(i).wrapAt(~numerobuffer.wrapAt(i)))});
				~nombreinstrument.do({arg instr;
					if(~numerobuffer.wrapAt(instr) == ~numerobuffer.wrapAt(i),
						{
							level=~synthcontrolviewlevels.wrapAt(i).value;
							if(~flagEntreeMode =='Audio IN', {
								//~listesamplein = ~listesampleinAudio;
								~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~canalAudioInInstr.at(i), \run, view.value, \trigger,  view.value,\loop,  ~looprecsamplebutton.wrapAt(i).value);
							},
							{
								//~listesampleinFile.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~busFileIn.index);
								//~listesamplein = ~listesampleinFile;
								~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~busFileIn.index, \run, view.value, \trigger,  view.value,\loop,  ~looprecsamplebutton.wrapAt(i).value);
							});
							~busreclevel.wrapAt(~numerobuffer.wrapAt(instr)).set(level.wrapAt(0).value, level.wrapAt(1).value);
							~recsamplebutton.wrapAt(instr).value=~recsamplebutton.wrapAt(i).value});
				});
				~recsamplebuttondatas.wrapPut(~numerobuffer.wrapAt(i).value,view.value);
			};
			// loop rec sample
			~looprecsamplebutton = ~looprecsamplebutton.add(Button(w,Rect( 0, 0, 60, 18)));
			~looprecsamplebutton.wrapAt(i).states = [["LoopR off", Color.black,  Color.green(0.8, 0.25)],["LoopR on", Color.white, Color.red(0.8, 0.25)]];
			~looprecsamplebutton.wrapAt(i).action = {|view| ~writepartitions.value(i,'loop rec sample',~numerobuffer.wrapAt(i),"~looprecsamplebutton",view.value);~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\loop, view.value);
				~looprecordingValue.wrapPut(~numerobuffer.wrapAt(i), view.value);
				~nombreinstrument.do({arg instr;
					if(~numerobuffer.wrapAt(instr) == ~numerobuffer.wrapAt(i),{~listesamplein.wrapAt(~numerobuffer.wrapAt(instr)).set(\loop, view.value);
						~looprecsamplebutton.wrapAt(instr).value=~looprecsamplebutton.wrapAt(i).value})});
				~looprecsamplebuttondatas.wrapPut(~numerobuffer.wrapAt(i).value,view.value);
			};
			// loop sample
			~loopbutton = ~loopbutton.add(Button(w,Rect(0, 0, 60, 18)));
			~loopbutton.wrapAt(i).states = [["LoopP off", Color.black,  Color.green(0.8, 0.25)],["LoopP on", Color.white, Color.red(0.8, 0.25)]];
			~loopbutton.wrapAt(i).action = {|view| ~writepartitions.value(i,'loop sample',~numerobuffer.wrapAt(i),"~loopbutton",view.value);~loopplaysamplerdatas.wrapPut(i,~loopplaysamplerdatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),view.value))};

			// set reverse sample
			~reversebutton = ~reversebutton.add(Button(w,Rect(0, 0, 60, 18)));
			~reversebutton.wrapAt(i).states = [["->", Color.black,  Color.green(0.8, 0.25)],["<-", Color.white, Color.red(0.8, 0.25)]];
			~reversebutton.wrapAt(i).action = {|view|
				~writepartitions.value(i,'reverse sample',~numerobuffer.wrapAt(i),"~reversebutton",view.value);
				if(view.value == 0, {~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),1));~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),-1));~busoffsetplaysampler.wrapAt(i).set(1-~sampleroffsetcontrol.wrapAt(i).value)})};
			// Text
			StaticText(w, Rect(0,0, 75, 18)).string_(" Sample2 ->").stringColor_(Color.yellow);
			// Set buffer pour FFT
			~soncontrolfft=~soncontrolfft.add(PopUpMenu(w,Rect(0,0,150,18)).items = ~displaySons2);
			~soncontrolfft.wrapAt(i).action = {arg son;~writepartitions.value(i,'normal','off',"~soncontrolfft",son.value);~numerobufferAdd.wrapPut(i,son.value)};
			w.view.decorator.nextLine;
			StaticText(w, Rect(0, 0, 100, 12)).string_("SYNTH").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10)).align_(\right);
			// number box synth
			~synthparametresnumber=~synthparametresnumber.add(NumberBox(w,Rect(0,0,50,12)));
			~synthparametresnumber.wrapAt(i).action={arg nombre;var c, datas;
				~writepartitions.value(i,'normal','off',"~synthparametresnumber",nombre.value);
				c=~synthcontrolviewparametres.wrapAt(i).value;
				c.wrapPut(~lastcontrolindex.wrapAt(i),nombre.value);
				~buscontrolsynth.wrapAt(i).set(c.wrapAt(0), c.wrapAt(1), c.wrapAt(2), c.wrapAt(3), c.wrapAt(4), c.wrapAt(5), c.wrapAt(6), c.wrapAt(7), c.wrapAt(8), c.wrapAt(9));
				~synthcontrolviewparametres.wrapAt(i).value=c;
				datas=~synthcontrolviewparametresdatas.wrapAt(i).value;
				datas.wrapPut(~synthcontrol.wrapAt(i).value,c);
				~synthcontrolviewparametresdatas.wrapPut(i,datas.value)};
			StaticText(w, Rect(0, 0, 135, 12)).string_("Envelope").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10)).align_(\right);
			// Controles envelope
			~synthenvelopenumberx=~synthenvelopenumberx.add(NumberBox(w,Rect(0,0,46,12)));
			~synthenvelopenumberx.wrapAt(i).action={arg nombre;var val, index;
				~writepartitions.value(i,'normal','off',"~synthenvelopenumberx",nombre.value);
				val=~lastenvelopevalue.wrapAt(i);index=~lastenvelopeindex.wrapAt(i);
				val.wrapPut(0,val.wrapAt(0).wrapPut(index,nombre.value));
				// Version avec envelope unique
				~listSynth.size.do({arg synth; ~timeenvsynth.wrapAt(i).wrapPut(synth, val.wrapAt(0))});
				//// Version avec envelope pour chaque synth
				//~timeenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, val.wrapAt(0));
				~synthsliderenvelope.wrapAt(i).value=val};
			~synthenvelopenumbery=~synthenvelopenumbery.add(NumberBox(w,Rect(0,0,46,12)));
			~synthenvelopenumbery.wrapAt(i).action={arg nombre;var val, index, string;
				~writepartitions.value(i,'normal','off',"~synthenvelopenumbery",nombre.value);
				val=~lastenvelopevalue.wrapAt(i);index=~lastenvelopeindex.wrapAt(i);
				val.wrapPut(1,val.wrapAt(1).wrapPut(index,nombre.value));
				// Version avec envelope unique
				~listSynth.size.do({arg synth; ~levelenvsynth.wrapAt(i).wrapPut(synth,val.wrapAt(1))});
				//// Version avec envelope pour chaque synth
				//~levelenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value,val.wrapAt(1));
				~synthsliderenvelope.wrapAt(i).value=val};
			~displayRecLevel = ~displayRecLevel.add(StaticText(w, Rect(0, 0, 50, 12)).string_("    LEVEL").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10)));
			~numRecLevel1 = ~numRecLevel1.add(NumberBox(w,Rect(0,0,35,12)).minDecimals_(5).maxDecimals_(5));
			~numRecLevel1.wrapAt(i).action={arg level;
				var levels;
				~writepartitions.value(i,'normal','off',"~numRecLevel1",level.value);
				levels = ~synthcontrolviewlevels.wrapAt(i).value;
				levels.put(0, level.value);
				~synthcontrolviewlevels.wrapAt(i).valueAction_(levels);
			};
			~numRecLevel2 = ~numRecLevel2.add(NumberBox(w,Rect(0,0,35,12)).minDecimals_(5).maxDecimals_(5));
			~numRecLevel2.wrapAt(i).action={arg level;
				var levels;
				~writepartitions.value(i,'normal','off',"~numRecLevel2",level.value);
				levels = ~synthcontrolviewlevels.wrapAt(i).value;
				levels.put(1, level.value);
				~synthcontrolviewlevels.wrapAt(i).valueAction_(levels);
			};
			StaticText(w, Rect(0, 0, 80, 12)).string_("Pre FX").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10)).align_(\right);
			~syntheffetsPrenumber=~syntheffetsPrenumber.add(NumberBox(w,Rect(0,0,50,12)).minDecimals_(4).maxDecimals_(4));
			~syntheffetsPrenumber.wrapAt(i).action={arg nombre;var datas;
				~writepartitions.value(i,'normal','off',"~syntheffetsPrenumber",nombre.value);
				switch(~lasteffetsPreindex.wrapAt(i),
					{0}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control1,nombre.value)},
					{1}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control2,nombre.value)},
					{2}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control3,nombre.value)},
					{3}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control4,nombre.value)},
					{4}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control5,nombre.value)},
					{5}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control6,nombre.value)},
					{6}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control7,nombre.value)},
					{7}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control8,nombre.value)}
				);
				datas=~synthcontrolvieweffetsPredatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
				datas.wrapPut(~lasteffetsPreindex.wrapAt(i).value,nombre.value);
				~synthcontrolvieweffetsPre.wrapAt(i).value=datas;
				~dataseffetsPres=~synthcontrolvieweffetsPredatas.wrapAt(i).value;
				~dataseffetsPres.wrapPut(~listenodeeffetsPresynth.wrapAt(i).value,~synthcontrolvieweffetsPre.wrapAt(i).value);
				~synthcontrolvieweffetsPredatas.wrapPut(i,~dataseffetsPres.value)};
			StaticText(w, Rect(0, 0, 150, 12)).string_("Post FX").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10)).align_(\right);
			~syntheffetsPostnumber=~syntheffetsPostnumber.add(NumberBox(w,Rect(0,0,50,12)).minDecimals_(4).maxDecimals_(4));
			~syntheffetsPostnumber.wrapAt(i).action={arg nombre;var datas;
				~writepartitions.value(i,'normal','off',"~syntheffetsPostnumber",nombre.value);
				switch(~lasteffetsPostindex.wrapAt(i),
					{0}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control1,nombre.value)},
					{1}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control2,nombre.value)},
					{2}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control3,nombre.value)},
					{3}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control4,nombre.value)},
					{4}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control5,nombre.value)},
					{5}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control6,nombre.value)},
					{6}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control7,nombre.value)},
					{7}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control8,nombre.value)}
				);
				datas=~synthcontrolvieweffetsPostdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
				datas.wrapPut(~lasteffetsPostindex.wrapAt(i).value,nombre.value);
				~synthcontrolvieweffetsPost.wrapAt(i).value=datas;
				~dataseffetsPosts=~synthcontrolvieweffetsPostdatas.wrapAt(i).value;
				~dataseffetsPosts.wrapPut(~listenodeeffetsPostsynth.wrapAt(i).value,~synthcontrolvieweffetsPost.wrapAt(i).value);
				~synthcontrolvieweffetsPostdatas.wrapPut(i,~dataseffetsPosts.value)};
			w.view.decorator.nextLine;
			// parametres synth
			~synthcontrolviewparametres=~synthcontrolviewparametres.add(MultiSliderView(w, Rect(0, 0, 205, 100)));
			~synthcontrolviewparametres.wrapAt(i).action = {arg control;var c, datas;
				~writepartitions.value(i,'synth control',~synthcontrol.wrapAt(i).value,"~synthcontrolviewparametres",control.value);
				c=~synthcontrolviewparametres.wrapAt(i).value;
				c.wrapPut(control.index,control.currentvalue);
				~buscontrolsynth.wrapAt(i).set(c.wrapAt(0), c.wrapAt(1), c.wrapAt(2), c.wrapAt(3), c.wrapAt(4), c.wrapAt(5), c.wrapAt(6), c.wrapAt(7), c.wrapAt(8), c.wrapAt(9));
				datas=~synthcontrolviewparametresdatas.wrapAt(i).value;
				datas.wrapPut(~synthcontrol.wrapAt(i).value,~synthcontrolviewparametres.wrapAt(i).value);
				~synthcontrolviewparametresdatas.wrapPut(i,datas.value);
				~synthparametresnumber.wrapAt(i).value=control.currentvalue.round(0.00001);
				~lastcontrolindex.wrapPut(i,control.index);
				~wavetable.wrapAt(i).sine2([1,2,3,4,5,6,7,8,9,10],~synthcontrolviewparametres.wrapAt(i).value)};
			~synthcontrolviewparametres.wrapAt(i).value_([0,0,0,0,0,0,0,0,0,0]);
			~synthcontrolviewparametres.wrapAt(i).xOffset_(4);
			~synthcontrolviewparametres.wrapAt(i).thumbSize_(16);
			~synthcontrolviewparametres.wrapAt(i).strokeColor_(Color.cyan);
			~synthcontrolviewparametres.wrapAt(i).fillColor_(Color(0, 0.25, 0.5));
			~synthcontrolviewparametres.wrapAt(i).drawLines(false);
			~synthcontrolviewparametres.wrapAt(i).elasticMode_(1);
			// envelope view
			~synthsliderenvelope =  ~synthsliderenvelope.add(EnvelopeView(w, Rect(0, 0, 205, 100))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.fillColor_(Color(0, 0.25, 0.5))
				.strokeColor_(Color.cyan)
				.drawRects_(true)
				.thumbSize_(20)
				.step_(0.01)
				.gridOn_(true)
				.value_([[0.0, 0.015625, 0.125, 0.25, 0.5, 0.75, 0.9, 1.0],[0.0, 1.0, 1.0, 0.85, 0.75, 0.75, 0.5,0.0]])
				//.curves_(5)
				.action_({arg env;var val;
					~writepartitions.value(i,'envelope','off',"~synthsliderenvelope",env.value);
					val=env.value;~lastenvelopevalue.wrapPut(i,val);~lastenvelopeindex.wrapPut(i,env.index);
					~synthenvelopenumberx.wrapAt(i).value=val.wrapAt(0).wrapAt(env.index).value;
					~synthenvelopenumbery.wrapAt(i).value=val.wrapAt(1).wrapAt(env.index).value;
					// Version avec envelope unique
					~listSynth.size.do({arg synth; ~levelenvsynth.wrapAt(i).wrapPut(synth, val.wrapAt(1));~timeenvsynth.wrapAt(i).wrapPut(synth, val.wrapAt(0))});
					//// Version avec envelope pour chaque synth
					//~levelenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value,val.wrapAt(1));~timeenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, val.wrapAt(0));
			}));
			//rec level
			~synthcontrolviewlevels=~synthcontrolviewlevels.add(MultiSliderView(w, Rect(0, 0, 50, 100)));
			~synthcontrolviewlevels.wrapAt(i).action = {arg control;var string, c;
				~writepartitions.value(i,'rec level',~numerobuffer.wrapAt(i),"~synthcontrolviewlevels",control.value);
				c=~synthcontrolviewlevels.wrapAt(i).value;
				c.wrapPut(control.index,control.currentvalue);
				~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\run, ~recsamplebutton.wrapAt(i).value,\loop, ~looprecsamplebutton.wrapAt(i).value);
				~busreclevel.wrapAt(~numerobuffer.wrapAt(i)).set(control.value.wrapAt(0).value, control.value.wrapAt(1).value);
				~numRecLevel1.wrapAt(i).value_(control.value.wrapAt(0).value);
				~numRecLevel2.wrapAt(i).value_(control.value.wrapAt(1).value);
				//~displayRecLevel.wrapAt(i).string_("     LEVEL" + control.value.at(0).round(0.001).asString + control.value.at(1).round(0.001).asString);
				~nombreinstrument.do({arg instr;
					if(~numerobuffer.wrapAt(instr) == ~numerobuffer.wrapAt(i),{~busreclevel.wrapAt(~numerobuffer.wrapAt(instr)).set(control.value.wrapAt(0).value, control.value.wrapAt(1).value);
						~synthcontrolviewlevels.wrapAt(instr).value=~synthcontrolviewlevels.wrapAt(i).value})});
				~synthcontrolviewlevelsdatas.wrapPut(~numerobuffer.wrapAt(i).value, control.value)};
			~synthcontrolviewlevels.wrapAt(i).value_([1,0]);
			~synthcontrolviewlevels.wrapAt(i).xOffset_(4);
			~synthcontrolviewlevels.wrapAt(i).thumbSize_(16);
			~synthcontrolviewlevels.wrapAt(i).strokeColor_(Color.cyan);
			~synthcontrolviewlevels.wrapAt(i).fillColor_(Color.blue);
			~synthcontrolviewlevels.wrapAt(i).drawLines(false);
			~synthcontrolviewlevels.wrapAt(i).elasticMode_(1);
			~synthcontrolviewlevels.wrapAt(i).step_(0.00001);
			//effet pre slider
			~synthcontrolvieweffetsPre=~synthcontrolvieweffetsPre.add(MultiSliderView(w, Rect(0, 0, 205, 100)));
			~synthcontrolvieweffetsPre.wrapAt(i).action = {arg control;
				if(~automationeffetpre.wrapAt(i).value != 1 ,{~writepartitions.value(i,'effet_pre',~listenodeeffetsPresynth.wrapAt(i).value,"~synthcontrolvieweffetsPre",control.value)});
				switch(control.index,
					{0}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control1,control.currentvalue)},
					{1}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control2,control.currentvalue)},
					{2}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control3,control.currentvalue)},
					{3}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control4,control.currentvalue)},
					{4}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control5,control.currentvalue)},
					{5}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control6,control.currentvalue)},
					{6}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control7,control.currentvalue)},
					{7}, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\control8,control.currentvalue)}
				);
				~dataseffetsPres=~synthcontrolvieweffetsPredatas.wrapAt(i).value;
				~dataseffetsPres.wrapPut(~listenodeeffetsPresynth.wrapAt(i).value,~synthcontrolvieweffetsPre.wrapAt(i).value);
				~synthcontrolvieweffetsPredatas.wrapPut(i,~dataseffetsPres.value);
				~lasteffetsPreindex.wrapPut(i,control.index);~syntheffetsPrenumber.wrapAt(i).value=control.currentvalue;
			};
			~synthcontrolvieweffetsPre.wrapAt(i).value_([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5]);
			~synthcontrolvieweffetsPre.wrapAt(i).xOffset_(4);
			~synthcontrolvieweffetsPre.wrapAt(i).thumbSize_(21);
			~synthcontrolvieweffetsPre.wrapAt(i).strokeColor_(Color.cyan);
			~synthcontrolvieweffetsPre.wrapAt(i).fillColor_(Color(0, 0.25, 0.5));
			~synthcontrolvieweffetsPre.wrapAt(i).drawLines(false);
			~synthcontrolvieweffetsPre.wrapAt(i).elasticMode_(1);
			// effet post slider
			~synthcontrolvieweffetsPost=~synthcontrolvieweffetsPost.add(MultiSliderView(w, Rect(0, 0, 205, 100)));
			~synthcontrolvieweffetsPost.wrapAt(i).action = {arg control;
				if(~automationeffetpost.wrapAt(i).value != 1 ,{~writepartitions.value(i,'effet_post',~listenodeeffetsPostsynth.wrapAt(i).value,"~synthcontrolvieweffetsPost",control.value)});
				switch(control.index,
					{0}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control1,control.currentvalue)},
					{1}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control2,control.currentvalue)},
					{2}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control3,control.currentvalue)},
					{3}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control4,control.currentvalue)},
					{4}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control5,control.currentvalue)},
					{5}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control6,control.currentvalue)},
					{6}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control7,control.currentvalue)},
					{7}, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\control8,control.currentvalue)}
				);
				~dataseffetsPosts=~synthcontrolvieweffetsPostdatas.wrapAt(i).value;
				~dataseffetsPosts.wrapPut(~listenodeeffetsPostsynth.wrapAt(i).value,~synthcontrolvieweffetsPost.wrapAt(i).value);
				~synthcontrolvieweffetsPostdatas.wrapPut(i,~dataseffetsPosts.value);
				~lasteffetsPostindex.wrapPut(i,control.index);~syntheffetsPostnumber.wrapAt(i).value=control.currentvalue};
			~synthcontrolvieweffetsPost.wrapAt(i).value_([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5]);
			~synthcontrolvieweffetsPost.wrapAt(i).xOffset_(4);
			~synthcontrolvieweffetsPost.wrapAt(i).thumbSize_(21);
			~synthcontrolvieweffetsPost.wrapAt(i).strokeColor_(Color.cyan);
			~synthcontrolvieweffetsPost.wrapAt(i).fillColor_(Color(0, 0.25, 0.5));
			~synthcontrolvieweffetsPost.wrapAt(i).drawLines(false);
			~synthcontrolvieweffetsPost.wrapAt(i).elasticMode_(1);
			w.view.decorator.nextLine;
			// Automation controls instrument
			~automationinstrument = ~automationinstrument.add(Button(w,Rect(0,0,205,18)));
			~automationinstrument.wrapAt(i).states = [["Random Synth Off", Color.black,  Color.green(0.8, 0.25)],["Random Synth On", Color.white, Color.red(0.8, 0.25)]];
			~automationinstrument.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationinstrument",view.value);
				~randomValueSynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, view.value)};
			// Automation parametres instrument
			~automationparametres = ~automationparametres.add(Button(w,Rect(0,0,205,18)));
			~automationparametres.wrapAt(i).states = [["Random Instrument Off", Color.black,  Color.green(0.8, 0.25)],["Random Instrument On", Color.white, Color.red(0.8, 0.25)]];
			~automationparametres.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationparametres",view.value);
				~randomValueParametreSynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, view.value)};
			StaticText(w, Rect(0, 0, 52, 12)).string_("").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			// EFFET PRE
			~syntheffetsPrecontrol= ~syntheffetsPrecontrol.add(PopUpMenu(w,Rect(0,0,161,18)).items = ~fxPre);
			~syntheffetsPrecontrol.wrapAt(i).action = {arg effetsPre;
				~writepartitions.value(i,'normal','off',"~syntheffetsPrecontrol",effetsPre.value);
				~listenodeeffetsPresynth.wrapPut(i,effetsPre.value);
				~synthcontrolvieweffetsPre.wrapAt(i).value=~synthcontrolvieweffetsPredatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);~synthonoffeffetsPre.wrapAt(i).value=~synthonoffeffetsPredatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
				~synthpaneffetsPrestep.wrapAt(i).value=~synthpaneffetsPrestepdatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
				~synthampeffetsPrestep.wrapAt(i).value=~synthampeffetsPrestepdatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
				~automationeffetpre.wrapAt(i).valueAction_(~randomValueEffetPre.wrapAt(i).wrapAt(effetsPre.value).value);
				~automationeffetpanpre.wrapAt(i).valueAction_(~randomValueEffetPanPre.wrapAt(i).wrapAt(effetsPre.value).value);
			};
			~synthonoffeffetsPre = ~synthonoffeffetsPre.add(Button(w,Rect( 0,0,40,18)));
			~synthonoffeffetsPre.wrapAt(i).states = [["Off", Color.black,  Color.green(0.8, 0.25)],["On", Color.white, Color.red(0.8, 0.25)]
			];
			~synthonoffeffetsPre.wrapAt(i).action = {|view| var datas;
				~writepartitions.value(i,'onoff_pre',~listenodeeffetsPresynth.wrapAt(i).value,"~synthonoffeffetsPre",view.value);
				if (view.value == 1, {~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).run(true)},
					{~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).run(false)});
				datas=~synthonoffeffetsPredatas.wrapAt(i).value;
				datas.wrapPut(~listenodeeffetsPresynth.wrapAt(i).value,view.value);
				~synthonoffeffetsPredatas.wrapPut(i,datas.value)};
			// EFFET POST
			~syntheffetsPostcontrol= ~syntheffetsPostcontrol.add(PopUpMenu(w,Rect(0,0,160,18)).items = ~fxPost);
			~syntheffetsPostcontrol.wrapAt(i).action = {arg effetsPost;~writepartitions.value(i,'normal','off',"~syntheffetsPostcontrol",effetsPost.value);~listenodeeffetsPostsynth.wrapPut(i,effetsPost.value);
				~synthcontrolvieweffetsPost.wrapAt(i).value=~synthcontrolvieweffetsPostdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);~synthonoffeffetsPost.wrapAt(i).value=~synthonoffeffetsPostdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
				~synthpaneffetsPoststep.wrapAt(i).value=~synthpaneffetsPoststepdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
				~synthampeffetsPoststep.wrapAt(i).value=~synthampeffetsPoststepdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
				~automationeffetpost.wrapAt(i).valueAction_(~randomValueEffetPost.wrapAt(i).wrapAt(effetsPost.value).value);
				~automationeffetpanpost.wrapAt(i).valueAction_(~randomValueEffetPanPost.wrapAt(i).wrapAt(effetsPost.value).value)};
			~synthonoffeffetsPost = ~synthonoffeffetsPost.add(Button(w,Rect( 0,0,40,18)));
			~synthonoffeffetsPost.wrapAt(i).states = [["Off", Color.black,  Color.green(0.8, 0.25)],["On", Color.white, Color.red(0.8, 0.25)]
			];
			~synthonoffeffetsPost.wrapAt(i).action = {|view| var datas;
				~writepartitions.value(i,'onoff_post',~listenodeeffetsPostsynth.wrapAt(i).value,"~synthonoffeffetsPost",view.value);
				if (view.value == 1, {~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).run(true)},
					{~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).run(false)});
				~datas=~synthonoffeffetsPostdatas.wrapAt(i).value;
				~datas.wrapPut(~listenodeeffetsPostsynth.wrapAt(i).value,view.value);
				~synthonoffeffetsPostdatas.wrapPut(i,~datas.value)};
			w.view.decorator.nextLine;
			// Offset sample
			~sampleroffsetcontrol = ~sampleroffsetcontrol.add(EZSlider(w, 204 @ 18, "OffSet",ControlSpec(0, 1, \lin, 0),
				{|ez|
					if(~randomValueSynth.wrapAt(i).wrapAt(~synthcontrol.wrapAt(i).value).value != 1, {~writepartitions.value(i,'normal','off',"~sampleroffsetcontrol",ez.value)});
					if(~reversebutton.wrapAt(i).value == 0, {~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~busoffsetplaysampler.wrapAt(i).set(1-~sampleroffsetcontrol.wrapAt(i).value)});
					~sampleroffsetcontrolValue.wrapAt(i).wrapPut(~soncontrol.wrapAt(i).value, ez.value);
				},
				0,labelWidth: 40,numberWidth: 35));
			// Panoramique de l'instrument
			~synthpancontrol = ~synthpancontrol.add(EZRanger(w, 205 @ 18, "Pan", \bipolar,
				{|ez| ~writepartitions.value(i,'normal','off',"~synthpancontrol",ez.value);
					~buspansynthLo.wrapAt(i).set(ez.lo);
					~buspansynthHi.wrapAt(i).set(ez.hi);
					// Version avec pan unique
					~listSynth.size.do({arg synth; ~synthpancontrolValue.wrapAt(i).wrapPut(synth, [ez.lo, ez.hi])});
					//// Version avec pan pour chaque synth
					//~synthpancontrolValue.wrapAt(i).wrapPut(~numeroSynth.wrapAt(i).value, [ez.lo, ez.hi]);
			}, [-0.1, 0.1], false, 42, 35));
			StaticText(w, Rect(0, 0, 52, 12)).string_("").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			// PRE pan
			~synthpaneffetsPrestep = ~synthpaneffetsPrestep.add(EZSlider(w, 205 @ 18, "Pan_pre", \bipolar,
				{|ez| ~writepartitions.value(i,'pan_pre',~listenodeeffetsPresynth.wrapAt(i).value,"~synthpaneffetsPrestep",ez.value);~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\pan, ez.value);
					~dataspaneffetsPres=~synthpaneffetsPrestepdatas.wrapAt(i).value;
					~dataspaneffetsPres.wrapPut(~listenodeeffetsPresynth.wrapAt(i).value,ez.value);
					~synthpaneffetsPrestepdatas.wrapPut(i,~dataspaneffetsPres.value)},
				0,labelWidth: 60,numberWidth: 50));
			// Post Pan
			~synthpaneffetsPoststep = ~synthpaneffetsPoststep.add(EZSlider(w, 205 @ 18, "Pan_post", \bipolar,
				{|ez| ~writepartitions.value(i,'pan_post',~listenodeeffetsPostsynth.wrapAt(i).value,"~synthpaneffetsPoststep",ez.value);~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\pan, ez.value);
					~dataspaneffetsPosts=~synthpaneffetsPoststepdatas.wrapAt(i).value;
					~dataspaneffetsPosts.wrapPut(~listenodeeffetsPostsynth.wrapAt(i).value,ez.value);
					~synthpaneffetsPoststepdatas.wrapPut(i,~dataspaneffetsPosts.value)},
				0,labelWidth: 60,numberWidth: 50));
			w.view.decorator.nextLine;
			// Datas frequence
			~synthfreqrange=~synthfreqrange.add(EZRanger(w, 205 @ 18, "Freq", ControlSpec(0, 127, \lin, 0),
				{|ez| var freq, freqRate;~writepartitions.value(i,'normal','off',"~synthfreqrange",ez.value);freq=~freq.wrapAt(i).cpsmidi / 127;freq=(freq * (ez.hi - ez.lo) + ez.lo + ~synthfreqstep.wrapAt(i).value).midicps;
					freqRate=(freq.cpsmidi - 48).midicps;
					freq.size.do({arg ii;~busfreqsynth.wrapAt(i).wrapAt(ii).set(freq.wrapAt(ii))});
					freqRate.size.do({arg ii;~busfreqRatesynth.wrapAt(i).wrapAt(ii).set(freqRate.wrapAt(ii))})},
				[0, 127], false, 40, 35));
			// step fhz
			~synthfreqstep = ~synthfreqstep.add(EZSlider(w, 205 @ 18, "Trsl Fhz", ControlSpec(-127, 127, \lin, 0),
				{|ez| var freq, freqRate;~writepartitions.value(i,'normal','off',"~synthfreqstep",ez.value);freq=~freq.wrapAt(i).cpsmidi / 127;
					freq=(freq*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo) + ~synthfreqrange.wrapAt(i).lo + ez.value).midicps;
					freqRate=(freq.cpsmidi - 48).midicps;
					freq.size.do({arg ii;~busfreqsynth.wrapAt(i).wrapAt(ii).set(freq.wrapAt(ii))});
					freqRate.size.do({arg ii;~busfreqRatesynth.wrapAt(i).wrapAt(ii).set(freqRate.wrapAt(ii))})},
				0,labelWidth: 42,numberWidth: 35));
			StaticText(w, Rect(0, 0, 50, 10)).string_("").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			// pre AMP
			~synthampeffetsPrestep = ~synthampeffetsPrestep.add(EZSlider(w, 205 @ 18, "Amp_pre", ControlSpec(-inf, 12, \db),
				{|ez| ~writepartitions.value(i,'amp_pre',~listenodeeffetsPresynth.wrapAt(i).value,"~synthampeffetsPrestep",ez.value);~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\amp, ez.value.dbamp);
					~datasampeffetsPres=~synthampeffetsPrestepdatas.wrapAt(i).value;
					~datasampeffetsPres.wrapPut(~listenodeeffetsPresynth.wrapAt(i).value,ez.value);
					~synthampeffetsPrestepdatas.wrapPut(i,~datasampeffetsPres.value)},
				-12,labelWidth: 60,numberWidth: 50));
			// post amp
			~synthampeffetsPoststep = ~synthampeffetsPoststep.add(EZSlider(w, 205 @ 18, "Amp_post", ControlSpec(-inf, 12, \db),
				{|ez| ~writepartitions.value(i,'amp_post',~listenodeeffetsPostsynth.wrapAt(i).value,"~synthampeffetsPoststep",ez.value);~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\amp, ez.value.dbamp);
					~datasampeffetsPosts=~synthampeffetsPoststepdatas.wrapAt(i).value;
					~datasampeffetsPosts.wrapPut(~listenodeeffetsPostsynth.wrapAt(i).value,ez.value);
					~synthampeffetsPoststepdatas.wrapPut(i,~datasampeffetsPosts.value)},
				-12,labelWidth: 60,numberWidth: 50));
			w.view.decorator.nextLine;
			// Datas amplitude
			~synthamprange=~synthamprange.add(EZRanger(w, 205 @ 18, "Amp", \db,
				{|ez| ~writepartitions.value(i,'normal','off',"~synthamprange",ez.value);~busampsynth.wrapAt(i).set(~amp.wrapAt(i).value * (ez.hi.dbamp - ez.lo.dbamp) + ez.lo.dbamp)},
				[-inf, 0], false, 40, 35));
			// Send FX Pre
			~sendFXPre = ~sendFXPre.add(EZSlider(w, 128 @ 18, "FXpre", ControlSpec(-inf, 12, \db),
				{|ez| ~writepartitions.value(i,'normal','off',"~sendFXPre",ez.value);
					~ampPre.put(i, ez.value.dbamp);
					~gsynth.at(i).setn(\ampPre, ez.value.dbamp);
				},
				-120,labelWidth: 33,numberWidth: 25));
			// Send FX Post
			~sendFXPost = ~sendFXPost.add(EZSlider(w, 128 @ 18, "FXpost", ControlSpec(-inf, 12, \db),
				{|ez| ~writepartitions.value(i,'normal','off',"~sendFXPost",ez.value);
					~ampPost.put(i, ez.value.dbamp);
					~geffet.at(i).setn(\ampPost, ez.value.dbamp);
					~gsynth.at(i).setn(\ampPost, ez.value.dbamp);
				},
				-120,labelWidth: 33,numberWidth: 25));
			//StaticText(w, Rect(0, 0, 50, 10)).string_("").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			// Automation pan effets_pre
			~automationeffetpanpre = ~automationeffetpanpre.add(Button(w,Rect(0,0,205,18)));
			~automationeffetpanpre.wrapAt(i).states = [["Random Pan Effect_Pre Off", Color.black,  Color.green(0.8, 0.25)],["Random Pan Effect_Pre On", Color.white, Color.red(0.8, 0.25)]];
			~automationeffetpanpre.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationeffetpanpre",view.value);
				~randomValueEffetPanPre.wrapAt(i).wrapPut(~syntheffetsPrecontrol.wrapAt(i).value, view.value)};
			// Automation pan effets_post
			~automationeffetpanpost = ~automationeffetpanpost.add(Button(w,Rect(0,0,205,18)));
			~automationeffetpanpost.wrapAt(i).states = [["Random Pan Effect_Post Off", Color.black,  Color.green(0.8, 0.25)],["Random Pan Effect_Post On", Color.white, Color.red(0.8, 0.25)]];
			~automationeffetpanpost.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationeffetpanpost",view.value);
				~randomValueEffetPanPost.wrapAt(i).wrapPut(~syntheffetsPostcontrol.wrapAt(i).value, view.value)};
			w.view.decorator.nextLine;
			// Datas duree
			~dureerange= ~dureerange.add(EZRanger(w, 205 @ 18, "Dur", ControlSpec(0, 60, \lin, 0),
				{|ez| ~writepartitions.value(i,'normal','off',"~dureerange",ez.value)},
				[0, 4],labelWidth: 40,numberWidth: 35));
			~dureestep=~dureestep.add(EZSliderTempo(w, 205 @ 18, "Stretch", ControlSpec(-16, 64, \lin, 0),
				{|ez| var tempo;
					if(ez.value >= 1.neg and: {ez.value < 1}, {ez.value = 1.0});
					if(ez.value < 1, {tempo = ez.value.reciprocal.neg},{tempo = ez.value});
					~dureeMul.wrapPut(i, tempo);
					~writepartitions.value(i,'special','off',"~dureestep", tempo)},
				1,labelWidth: 42,numberWidth: 35));
			StaticText(w, Rect(0, 0, 50, 10)).string_("").stringColor_(Color.yellow).font_(Font("Georgia-BoldItalic", 10));
			// Automation controls effets_pre
			~automationeffetpre = ~automationeffetpre.add(Button(w,Rect(0,0,205,18)));
			~automationeffetpre.wrapAt(i).states = [["Random Effect_Pre Off", Color.black,  Color.green(0.8, 0.25)],["Random Effect_Pre On", Color.white, Color.red(0.8, 0.25)]];
			~automationeffetpre.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationeffetpre",view.value);
				~randomValueEffetPre.wrapAt(i).wrapPut(~syntheffetsPrecontrol.wrapAt(i).value, view.value)};
			// Automation controls effets_post
			~automationeffetpost = ~automationeffetpost.add(Button(w,Rect(0,0,205,18)));
			~automationeffetpost.wrapAt(i).states = [["Random Effect_Post Off", Color.black,  Color.green(0.8, 0.25)],["Random Effect_Post On", Color.white, Color.red(0.8, 0.25)]];
			~automationeffetpost.wrapAt(i).action = {|view| ~writepartitions.value(i,'normal','off',"~automationeffetpost",view.value);
				~randomValueEffetPost.wrapAt(i).wrapPut(~syntheffetsPostcontrol.wrapAt(i).value, view.value)};
			w.view.decorator.nextLine;
			~quantizationview=~quantizationview.add(EZSlider(w, 205 @ 18, "Quanta", ControlSpec(0.01, 100, \exp, 0),
				{|ez| ~writepartitions.value(i,'normal','off',"~quantizationview",ez.value);
					~tempoSystem.schedAbs(~tempoSystem.beats, {~quantization.wrapPut(i, ez.value);nil})},
				100,labelWidth: 42,numberWidth: 35));

			//Band + Tunes
			// Number FhzBand 87
			~numberBand = ~numberBand.add(EZKnob(w, 110 @ 18, "FhzBand", ControlSpec(1, 12, \lin, 1),
				{|ez| var band, array, range;
					~writepartitions.value(i,'normal','off',"~numberBand", ez.value);
					band = ez.value;
					~numFhzBand.put(i, band);
					array = Array.fill(band, {arg i; [127 / band * i, 127 / band * i + (127 / band )]});
					array = array.reverse;
					array = array.add([0, 127]);
					array = array.reverse;
					~bandFHZ.put(i, array);
					range = [];
					for(0, band,
						{arg index;
							if(index != 0, {w.view.children.at(88 + index).enabled_(true); range = range.add(index)});// Band active
					});
					~rangeSynthBand.put(i, range);
					if(band < 12, {
						for(band + 1, 12,
							{arg index;
								w.view.children.at(88 + index).enabled_(false);
								w.view.children.at(88 + index).valueAction_(0);
						});
					});
					~lastTimeBand.put(i, [Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime]);// 12 band total
					~listeaudiofreq.wrapPut(i,[]);~listeaudioamp.wrapPut(i,[]);~listeaudioduree.wrapPut(i,[]);~freqtampon.wrapPut(i,nil);~amptampon.wrapPut(i,nil);~freqbefore.wrapPut(i,0);~ampbefore.wrapPut(i,0);~dureebefore.wrapPut(i,0);~lastTimeAudio = Main.elapsedTime;~lastDureeInstrAudio.wrapPut(i, Main.elapsedTime);
					~rangeBand.at(i).value = array.round(2);
			}, 12, layout: \horz);
			);
			// SynthBand 88 to 98
			// Band 0 to 12
			~synthBand0 = ~synthBand0.add(Button.new(w, 16 @ 18).
				states_([["0", Color.green], ["0", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand0",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(0, band.value));
				});
			);
			~synthBand1 = ~synthBand1.add(Button.new(w, 16 @ 18).
				states_([["1", Color.green], ["1", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand1",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(1, band.value));
				});
			);
			~synthBand2 = ~synthBand2.add(Button.new(w, 16 @ 18).
				states_([["2", Color.green], ["2", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand2",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(2, band.value));
				});
			);
			~synthBand3 = ~synthBand3.add(Button.new(w, 16 @ 18).
				states_([["3", Color.green], ["3", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand3",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(3, band.value));
				});
			);
			~synthBand4 = ~synthBand4.add(Button.new(w, 16 @ 18).
				states_([["4", Color.green], ["4", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand4",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(4, band.value));
				});
			);
			~synthBand5 = ~synthBand5.add(Button.new(w, 16 @ 18).
				states_([["5", Color.green], ["5", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand5",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(5, band.value));
				});
			);
			~synthBand6 = ~synthBand6.add(Button.new(w, 16 @ 18).
				states_([["6", Color.green], ["6", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand6",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(6, band.value));
				});
			);
			~synthBand7 = ~synthBand7.add(Button.new(w, 16 @ 18).
				states_([["7", Color.green], ["7", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand7",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(7, band.value));
				});
			);
			~synthBand8 = ~synthBand8.add(Button.new(w, 16 @ 18).
				states_([["8", Color.green], ["8", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand8",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(8, band.value));
				});
			);
			~synthBand9 = ~synthBand9.add(Button.new(w, 16 @ 20).
				states_([["9", Color.green], ["9", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand9",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(9, band.value));
				});
			);
			~synthBand10 = ~synthBand10.add(Button.new(w, 16 @ 18).
				states_([["10", Color.green], ["10", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand10",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(10, band.value));
				});
			);
			~synthBand11 = ~synthBand11.add(Button.new(w, 16 @ 18).
				states_([["11", Color.green], ["11", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand11",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(11, band.value));
				});
			);
			~synthBand12 = ~synthBand12.add(Button.new(w, 16 @ 18).
				states_([["12", Color.green], ["12", Color.red]]).
				action_({arg band;
					~writepartitions.value(i,'normal','off',"~synthBand12",band.value);
					~flagBandSynth.put(i, ~flagBandSynth.at(i).put(12, band.value));
				});
			);

			// flagFhzBand  99
			~buttonSynthBand = ~buttonSynthBand.add(Button(w, Rect(0, 0, 40, 18))
				.states_([["On", Color.green], ["Off", Color.red]])
				.action = {arg flag;
					~writepartitions.value(i,'normal','off',"~buttonSynthBand",flag.value);
					if(flag.value == 0,
						{~flagSynthBand.put(i, 'off');
							w.view.children.at(87).enabled_(false);
							//Band
							w.view.children.at(88).enabled_(false);
							w.view.children.at(89).enabled_(false);
							w.view.children.at(90).enabled_(false);
							w.view.children.at(91).enabled_(false);
							w.view.children.at(92).enabled_(false);
							w.view.children.at(93).enabled_(false);
							w.view.children.at(94).enabled_(false);
							w.view.children.at(95).enabled_(false);
							w.view.children.at(96).enabled_(false);
							w.view.children.at(97).enabled_(false);
							w.view.children.at(98).enabled_(false);
							w.view.children.at(99).enabled_(false);
							w.view.children.at(100).enabled_(false);
						},
						{~flagSynthBand.put(i, 'on');
							w.view.children.at(87).enabled_(true);
							for(0, ~numFhzBand.at(i),
								{arg index;
									if(index != 0, {w.view.children.at(88 + index).enabled_(true)});
							});
							if(~numFhzBand.at(i) < 12, {
								for(~numFhzBand.at(i) + 1, 12,
									{arg index;
										w.view.children.at(88 + index).enabled_(false);
								});
							});
					});
				};
			);
			//Range Band 100
			~rangeBand = ~rangeBand.add(EZText(w, Rect(0, 0, 260, 20), "Range Band",
				{arg range;
					~writepartitions.value(i,'normal','off',"~rangeBand",range.value);
					~bandFHZ.put(i, range.value)},
				[[0, 127], [0, 42], [42, 84], [84, 127] ], true, 60);
			);
			w.view.decorator.nextLine;
			StaticText(w, Rect(0,0, 50, 18)).string_("Tuning").stringColor_(Color.white).font_(Font("Georgia-BoldItalic", 10));
			// Tuning Analyze 101
			~listTuning = ~listTuning.add(PopUpMenu(w, Rect(0, 0, 130, 18)).
				items_(["No Scale", "- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"]).
				action = {arg item;
					~writepartitions.value(i,'normal','off',"~listTuning",item.value);
					~tuningIndex.put(i, item.value);
					// Setup GUI Value
					w.view.children.at(105).enabled_(true);
					w.view.children.at(106).enabled_(true);
					switch(item.value,
						// No Scale
						0, {~flagScaling.put(i, 'off');
							// Setup GUI Value
							w.view.children.at(105).enabled_(false);
							w.view.children.at(106).enabled_(false);
						},
						// Tempered
						1, {nil},
						// Chromatic
						2, {~degrees.put(i, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11])},
						// Whole Tone 1
						3, {~degrees.put(i, [0, 2, 4, 6, 8, 10])},
						// Major
						4, {~degrees.put(i, [0, 2, 4, 5, 7, 9, 11])},
						// Minor
						5, {~degrees.put(i, [0, 2, 3, 5, 7, 8, 10])},
						// Diminued
						6, {~degrees.put(i, [0, 2, 3, 5, 6, 8, 9, 11])},
						// Octatonic 1
						7, {~degrees.put(i, [0, 1, 3, 4, 6, 7, 9, 10])},
						// Octatonic 2
						8, {~degrees.put(i, [0, 2, 3, 5, 6, 8, 9, 11])},
						// Nonatonique
						9, {~degrees.put(i, [0, 2, 3, 4, 6, 7, 8, 10, 11])},
						// Messian 4
						10, {~degrees.put(i, [0, 1, 2, 5, 6, 7, 8, 11])},
						// Messian 5
						11, {~degrees.put(i, [0, 1, 5, 6, 7, 11])},
						// Messian 6
						12, {~degrees.put(i, [0, 2, 4, 5, 6, 8, 10, 11])},
						// Messian 7
						13, {~degrees.put(i, [0, 1, 2, 3, 5, 6, 7, 8, 9, 11])},
						// Bi-Pentaphonic
						14, {~degrees.put(i, [0, 1, 2, 4, 5, 6, 7, 9, 10, 11])},
						// Major Pentatonic
						15, {~degrees.put(i, [0, 2, 4, 7, 9])},
						// Minor Pentatonic
						16, {~degrees.put(i, [0, 3, 5, 7, 10])},
						// Blues
						17, {~degrees.put(i, [0, 3, 5, 6, 7, 10])},
						// Asavari
						18, {~degrees.put(i, [0, 2, 3, 5, 7, 8, 10])},
						// Bhairava
						19, {~degrees.put(i, [0, 1, 4, 5, 7, 8, 11])},
						// Bhairavi
						20, {~degrees.put(i, [0, 1, 3, 5, 7, 8, 10])},
						// Bilaval
						21, {~degrees.put(i, [0, 2, 4, 5, 7, 9, 11])},
						// Kafi
						22, {~degrees.put(i, [0, 2, 3, 5, 7, 9, 10])},
						// Kalyan
						23, {~degrees.put(i, [0, 2, 4, 6, 7, 9, 11])},
						// Khammaj
						24, {~degrees.put(i, [0, 2, 4, 5, 7, 9, 10])},
						// Marava
						25, {~degrees.put(i, [0, 1, 4, 6, 7, 9, 11])},
						// Pooravi
						26, {~degrees.put(i, [0, 1, 4, 6, 7, 8, 11])},
						// Todi
						27, {~degrees.put(i, [0, 1, 3, 6, 7, 8, 11])},
						// Indian Shrutis
						28, {nil},
						// 22tet
						29, {~degrees.put(i, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21])},
						// 12tet
						30, {~degrees.put(i, [0, 2, 4, 6, 7, 9, 11, 13, 15, 16, 19, 20])},
						// Asavari
						31, {~degrees.put(i, [0, 4, 6, 9, 13, 15, 19])},
						// Bhairava
						32, {~degrees.put(i, [0, 2, 7, 9, 13, 15, 20])},
						// Bhairavi
						33, {~degrees.put(i, [0, 3, 5, 9, 13, 15, 18])},
						// Bilaval
						34, {~degrees.put(i, [0, 4, 7, 9, 13, 16, 20])},
						// Kafi
						35, {~degrees.put(i, [0, 4, 6, 9, 13, 16, 19])},
						// Kalyan
						36, {~degrees.put(i, [0, 4, 7, 11, 13, 16, 20])},
						// Khammaj
						37, {~degrees.put(i, [0, 4, 7, 9, 13, 16, 19])},
						// Marava
						38, {~degrees.put(i, [0, 2, 7, 11, 13, 16, 20])},
						// Pooravi
						39, {~degrees.put(i, [0, 2, 7, 11, 13, 15, 20])},
						// Todi
						40, {~degrees.put(i, [0, 2, 6, 11, 13, 15, 20])}
					);
					if(item.value > 1 and: {item.value < 28}, {~tuning.put(i, Tuning.et12); ~scale.put(i, Scale.new(((~degrees.at(i) + ~root.at(i))%~tuning.at(i).size).sort, ~tuning.at(i).size, ~tuning.at(i)));
						~flagScaling.put(i,'on');
						// Setup GUI Value
						w.view.children.at(106).children.at(1).valueAction = ~degrees.at(i);
					});
					if(item.value > 28, {~tuning.put(i, Tuning.sruti); ~scale.put(i, Scale.new(((~degrees.at(i) + ~root.at(i))%~tuning.at(i).size).sort, ~tuning.at(i).size, ~tuning.at(i)));
						~flagScaling.put(i, 'on');
						// Setup GUI Value
						w.view.children.at(106).children.at(1).valueAction = ~degrees.at(i);
					});
				};
			);

			// Root 102
			~rootChoice = ~rootChoice.add(EZKnob(w, 80 @ 18, "Root", ControlSpec(0, 21, \lin, 1),
				{|ez|
					~writepartitions.value(i,'normal','off',"~rootChoice", ez.value);
					~root.put(i, ez.value); ~scale.put(i, Scale.new(((~degrees.at(i) + ~root.at(i))%~tuning.at(i).size).sort, ~tuning.at(i).size, ~tuning.at(i)))}, 0, layout: \horz, labelWidth: 30);
			);
			// Degrees 103
			~displayDegrees = ~displayDegrees.add(EZText(w, Rect(0, 0, 475, 18), "Degrees",
				{arg string;
					~writepartitions.value(i,'normal','off',"~displayDegrees", string.value);
					~degrees.put(i, string.value); ~scale.put(i, Scale.new(((~degrees.at(i) + ~root.at(i))%~tuning.at(i).size).sort, ~tuning.at(i).size, ~tuning.at(i)))},
				[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true);
			);
			// Audio In Instrument // 104
			~setAudioInstr = ~setAudioInstr.add(PopUpMenu(w, Rect(0,0,100,18)).items_(["AudioIn Rec 1", "AudioIn Rec 2", "AudioIn Rec 3", "AudioIn Rec 4", "AudioIn Rec 5", "AudioIn Rec 6", "AudioIn Rec 7", "AudioIn Rec 8", "AudioIn Rec 9", "AudioIn Rec 10", "AudioIn Rec 11", "AudioIn Rec 12", "AudioIn Rec 13", "AudioIn Rec 14", "AudioIn Rec 15", "AudioIn Rec 16", "AudioIn Rec 17", "AudioIn Rec 18", "AudioIn Rec 19", "AudioIn Rec 20", "AudioIn Rec 21", "AudioIn Rec 22", "AudioIn Rec 23", "AudioIn Rec 24", "AudioIn Rec 25", "AudioIn Rec 26", "AudioIn Rec 27", "AudioIn Rec 28", "AudioIn Rec 29", "AudioIn Rec 30", "AudioIn Rec 31", "AudioIn Rec 32"]).
				action = {|view|
					~writepartitions.value(i,'normal','off',"~setAudioInstr", view.value);
					~canalAudioInInstr.put(i, view.value);
					if(~flagEntreeMode =='Audio IN', {~listesampleinAudio.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~canalAudioInInstr.at(i));
						~listesamplein = ~listesampleinAudio;
					},
					{~listesampleinFile.wrapAt(~numerobuffer.wrapAt(i)).set(\in, ~busFileIn.index);
						~listesamplein = ~listesampleinFile;
					});
			});
			w.view.decorator.nextLine;
			// Init tuning et band
			~listTuning.at(i).valueAction_(0);
			~numberBand.at(i).valueAction_(3);
			~synthBand1.at(i).valueAction_(1);
			~synthBand2.at(i).valueAction_(1);
			~synthBand3.at(i).valueAction_(1);
			~buttonSynthBand.at(i).valueAction_(1);
			~buttonSynthBand.at(i).valueAction_(0);

			// OS I/O
			// save load datas
			~saveloaddatasinstrument = ~saveloaddatasinstrument.add(PopUpMenu(w,Rect(0, 0, 150, 18)).background_(Color.grey(0.5, 0.8)).items = ~instrumentsOSchoiceInstrument);
			~saveloaddatasinstrument.wrapAt(i).action = {arg item;
				~writepartitions.value(i,'normal','off',"~saveloaddatasinstrument",item.value);
				~slcinstruments.value(i,item);
				~saveloaddatasinstrument.wrapAt(i).value=0;
			};
			//  load single instrument
			~loadsingleinstrument = ~loadsingleinstrument.add(PopUpMenu(w,Rect(0, 0, 100, 18)).background_(Color.grey(0.5, 0.8)).items = ~singleinstrument);
			~loadsingleinstrument.wrapAt(i).action = {|item| var file, datas, datasfile;
				~writepartitions.value(i,'normal','off',"~loadsingleinstrument",item.value);
				if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instrument"+item.value.asString++".scd"),{
					~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
					file=File(~nompathdata++"instrument"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;~fonctionloaddatasinstrument.value(i, datas.wrapAt(0),'on');~listewindow.wrapAt(i).name="WekRobot by HP Instrument"+(i+1).asString+"instrument"+item.value.asString++".scd"})});
				//~loadsingleinstrument.wrapAt(i).valueAction=0;
			};
			//  load multi instruments
			~loadmultiinstruments = ~loadmultiinstruments.add(PopUpMenu(w,Rect(0, 0, 100, 18)).background_(Color.grey(0.5, 0.8)).items = ~multiinstruments);
			~loadmultiinstruments.wrapAt(i).action = {|item| var file, datas, datasfile;
				~writepartitions.value(i,'normal','off',"~loadmultiinstruments",item.value);
				if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instruments"+item.value.asString++".scd"),{file=File(~nompathdata++"instruments"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
					//+ Load datas Control Panel
					~readcontrolpanel.value(datas.last.at(0));
					for(0, ~nombreinstrument-1, {arg ii;
						~flagfreq.wrapPut(ii,0);~flagamp.wrapPut(ii,0);~flagduree.wrapPut(ii,0);~flagneuronefreq.wrapPut(ii,0);~flagneuroneamp.wrapPut(ii,0);~flagneuroneduree.wrapPut(ii,0);~flagoutneuronefreq.wrapPut(ii,0);~flagoutneuroneamp.wrapPut(ii,0);~flagoutneuroneduree.wrapPut(ii,0);~flaggenetiquefreq.wrapPut(ii,0);~flaggenetiqueamp.wrapPut(ii,0);~flaggenetiqueduree.wrapPut(ii,0);~duree.wrapPut(ii, 0);
						~fonctionloaddatasinstrument.value(ii, datas.wrapAt(ii),'on');~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+"instruments"+item.value.asString++".scd"})})});
				~loadmultiinstruments.wrapAt(i).value=0;
			};
			// Load Sequences musicales
			~sequencesbutton=~sequencesbutton.add(PopUpMenu(w,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ~sequenceliste);
			~sequencesbutton.wrapAt(i).action={arg item;~writepartitions.value(i,'normal','off',"~sequencesbutton",item.value);~foncseq.value(i,item);
				~sequencesbutton.wrapAt(i).value=0};
			// Load Partitions
			~partitionsbutton=~partitionsbutton.add(PopUpMenu(w,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ~partitionsliste);
			~partitionsbutton.wrapAt(i).action={arg item;~writepartitions.value(i,'normal','off',"~partitionbutton",item.value);~foncpart.value(i,item);
				~partitionsbutton.wrapAt(i).value=0};
			// Switch Canal MIDi OUT
			~midibutton=~midibutton.add(PopUpMenu(w,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ["MIDI out off", "MIDI out 1", "MIDI out 2", "MIDI out 3", "MIDI out 4", "MIDI out 5", "MIDI out 6", "MIDI out 7", "MIDI out 8", "MIDI out 9", "MIDI out 10", "MIDI out 11", "MIDI out 12", "MIDI out 13", "MIDI out 14", "MIDI out 15", "MIDI out 16"]);
			~midibutton.wrapAt(i).action={arg canal;~writepartitions.value(i,'normal','off',"~midibutton",canal.value);~foncpart.value(i, canal.value);
				if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal.value - 1); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal).value -1})})});
				~canalMidiOutInstr.wrapPut(i, canal.value - 1);
			};

			// Sound Synth ByPass
			~byPassSynth = ~byPassSynth.add(Button(w, Rect(0, 0, 100, 18))
				.states_([["ByPass On", Color.green], ["ByPass Off", Color.red]])
				.action = {arg flag;
					~writepartitions.value(i,'normal','off',"~byPassSynth", flag.value);
					~flagByPassSynth.put(i, flag.value);
				};
			);

			// stop the sound when window closes
			//w.onClose = {~routineinstrument.wrapAt(i).stop};
			// Setup Font View
			w.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Helvetica", 11);
				});
			});
			~listewindow=~listewindow.add(w);

			w.front;
			~startbutton.at(i).hasFocus;

		});

		// Control Panel
		~wg =Window("WekRobot by HP Control", Rect(0, 125, 305, 465), scroll: true);
		~wg.alpha=1.0;
		~wg.front;
		~wg.view.decorator = FlowLayout(~wg.view.bounds);
		// Systeme start stop playing
		~startsysteme = Button(~wg,Rect(0,0,100,18));
		~startsysteme.states = [["System Off", Color.black,  Color.green(0.8, 0.25)],["System On", Color.white, Color.red(0.8, 0.25)]];
		~startsysteme.action = {|view|
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPstart', view.value)});// Send OSC pour synchro app
			if (view.value == 1, {
				if(~oscStateFlag == 'master', {~ardourOSC.sendMsg('/ardour/transport_play')});// transport play
				~tempoSystem.stop;//Stop tempo general
				// Creation tempo general
				~tempoSystem = TempoClock.default = TempoClock.new(1, 0, Main.elapsedTime);
				~tempoSystem.tempo_(~tempoprocessinstrument.value / 60);
				~tempoSystem.schedAbs(~tempoSystem.nextBar, {~tempoSystem.beatsPerBar_(~nombreBeatsBare);nil});
				// set ON les routines de calcul
				~listesamplein = ~listesampleinAudio;
				~nombrebuffer.do({arg i;
					~listesampleinAudio.wrapAt(i).run(true);//Init sampler
					~listesampleinFile.wrapAt(i).set(\triggerRec, 1);
					~listesamplein.wrapAt(i).set(\run, ~recsamplebuttondatas.wrapAt(i).value, \loop,  ~looprecsamplebuttondatas.wrapAt(i).value.value);
				});
				~nombreinstrument.do({arg i;
					if(~startbutton.wrapAt(i).value == 1, {
						~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
						~routineinstrument.wrapAt(i).play(quant: Quant.new(1))})});
				~routineMetronome.play(quant: Quant.new(1));
				~routinepartitions.play(quant: Quant.new(1));
				if(~calculationchaos.value == 1 , {~routinechaosin.play(quant: Quant.new(1))});
				if(~calculationneurone.value == 1 , {~routineneuronein.play(quant: Quant.new(1))});
				if(~calculationgenetique.value == 1 , {~routinegenetiquein.play(quant: Quant.new(1))});
				if(~calculationalgorithmes.value == 1 , {~routinealgorithmes.play(quant: Quant.new(1))});
				if(~calculationautomation.value == 1 , {~routineautomation.play(quant: Quant.new(1))});
				// Start Recording
				if(~flagRecording == 'on', {
					~tempoSystem.schedAbs(~tempoSystem.nextBar, {s.record;nil});
				});
				// Setup AudioIn
				if(~calculationaudio.value == 0, {// set ON les routines de calcul audio
					~listesamplein = ~listesampleinAudio;
					~audioIn.run(true);~audioFile.run(false);~synthPlayFile.run(false);
					~tempoIn.value.run(true);~tempoFile.value.run(false);
					~nombrebuffer.do({arg i;
						~listesampleinFile.wrapAt(i).set(\triggerRec, 0);
						~listesampleinFile.wrapAt(i).run(false);
						~listesampleinAudio.wrapAt(i).run(true);
						~listesamplein.wrapAt(i).set(\run, ~recsamplebuttondatas.wrapAt(i).value, \loop,  ~looprecsamplebuttondatas.wrapAt(i).value.value);
					});
					~flagEntreeMode='Audio IN';
					~volumeFileIn.enabled_(false);
					~offsetFileIn.enabled_(false);
				},
				{
					// set ON les routines de calcul file
					~listesamplein = ~listesampleinFile;
					~audioIn.run(false);~audioFile.run(true);
					~tempoIn.value.run(false);~tempoFile.value.run(true);
					~tempoSystem.schedAbs(~tempoSystem.nextBar, {~synthPlayFile.run(true); nil});
					~synthPlayFile.set(\trig, 1);
					~nombrebuffer.do({arg i;
						~listesampleinAudio.wrapAt(i).run(false);
						~listesampleinFile.wrapAt(i).run(true);
						~listesampleinFile.wrapAt(i).set(\triggerRec, 1);
						~listesamplein.wrapAt(i).set(\run, ~recsamplebuttondatas.wrapAt(i).value, \loop,  ~looprecsamplebuttondatas.wrapAt(i).value.value);
					});
					~flagEntreeMode='File IN';
					~volumeFileIn.enabled_(true);
					~offsetFileIn.enabled_(true);
				});
			},
			{if(~oscStateFlag == 'master', {~ardourOSC.sendMsg('/ardour/transport_stop')});// transport stop
				// Set off le systeme
				~audioIn.run(false);~audioFile.run(false);~synthPlayFile.run(false);
				~tempoIn.value.run(false);~tempoFile.value.run(false);
				~nombrebuffer.do({arg i;
					~listesampleinAudio.wrapAt(i).run(false);//Init sampler
					~listesampleinFile.wrapAt(i).run(false);//Init sampler
				});
				~routinepartitions.stop;
				~nombreinstrument.do({arg i;
					~routineinstrument.wrapAt(i).stop;~duree.wrapPut(i, 0)});
				~routineMetronome.stop;
				~routineautomation.stop;
				~routinechaosin.stop;
				~routineneuronein.stop;
				~routinegenetiquein.stop;
				~routinealgorithmes.stop;
				~metronomeGUI.value_(0);
				~tempoSystem.clear;
				if(~flagMidiOut == 'on', {16.do({arg canal; ~midiOut.allNotesOff(canal); if(flagVST == 'on', {~fxVST.midi.allNotesOff(canal)})})});//MIDI setup off
		})};
		~startsysteme.focus;
		// Calculation Audio start stop playing
		~calculationaudio = PopUpMenu(~wg,Rect(0,0,60,18)).items=["AudioIn","FileIn"];
		~calculationaudio.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationaudio",view.value);if(~startsysteme.value == 1, {
			~nombreinstrument.do({arg instr;
				~listeaudiofreq.wrapPut(instr,[]);~listeaudioamp.wrapPut(instr,[]);
				~listeaudioduree.wrapPut(instr,[]);~lastDureeInstrAudio.wrapPut(instr, Main.elapsedTime)});
			if(view.value == 0, {
				~setAudioInSoft.enabled(false);
				// set ON les routines de calcul audio
				~listesamplein = ~listesampleinAudio;
				~audioIn.run(true);~audioFile.run(false);~synthPlayFile.run(false);
				~tempoIn.value.run(true);~tempoFile.value.run(false);
				~nombrebuffer.do({arg i;
					~listesampleinFile.wrapAt(i).set(\triggerRec, 0);
					~listesampleinFile.wrapAt(i).run(false);
					~listesampleinAudio.wrapAt(i).run(true);
					~listesamplein.wrapAt(i).set(\run, ~recsamplebuttondatas.wrapAt(i).value, \loop,  ~looprecsamplebuttondatas.wrapAt(i).value.value);
				});
				~flagEntreeMode='Audio IN';
				~volumeFileIn.enabled_(false);
				~offsetFileIn.enabled_(false);
			});
			if(view.value == 1, {
				~setAudioInSoft.enabled(true);
				// set ON les routines de calcul file
				~listesamplein = ~listesampleinFile;
				s.bind{
					~audioIn.run(false);~audioFile.run(true);
					~tempoIn.value.run(false);~tempoFile.value.run(true);
					s.sync;
					~tempoSystem.schedAbs(~tempoSystem.nextBar, {~synthPlayFile.run(true);nil});
					s.sync;
					~synthPlayFile.set(\trig, 1);
					s.sync;
					~nombrebuffer.do({arg i;
						~listesampleinAudio.wrapAt(i).run(false);
						~listesampleinFile.wrapAt(i).run(true);
						~listesampleinFile.wrapAt(i).set(\triggerRec, 1);
						~listesampleinFile.wrapAt(i).set(\in, ~busFileIn.index);
						~listesamplein.wrapAt(i).set(\run, ~recsamplebuttondatas.wrapAt(i).value, \loop,  ~looprecsamplebuttondatas.wrapAt(i).value.value);
					});
					s.sync};
				~flagEntreeMode='File IN';
				~volumeFileIn.enabled_(true);
				~offsetFileIn.enabled_(true);
			});
		},
		{s.bind{
			~synthPlayFile.set(\trig, 0);
			s.sync;
			~audioIn.run(false);~audioFile.run(false);~synthPlayFile.run(false);
			~tempoIn.value.run(false);~tempoFile.value.run(false);
			s.sync;
			~nombrebuffer.do({arg i;
				~listesampleinAudio.wrapAt(i).run(false);//Init sampler
				~listesampleinFile.wrapAt(i).set(\triggerRec, 0);
				~listesampleinFile.wrapAt(i).run(false);//Init sampler
			});
			s.sync};
		})};
		// Calculation midi start stop playing
		~calculationmidi = Button(~wg,Rect(0,0,100,18));
		~calculationmidi.states = [["MIDIIn Off", Color.black,  Color.green(0.8, 0.25)],["MIDIIn On", Color.white, Color.red(0.8, 0.25)]];
		~calculationmidi.action = {|view|
			~writepartitions.value(nil,'control panel normal','off',"~calculationmidi",view.value);
			if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
				MIDIIn.connect;
				~nombreinstrument.do({arg instr;
					~listemidifreq.wrapPut(instr,[]);~listemidiamp.wrapPut(instr,[]);
					~listemididuree.wrapPut(instr,[])});
				~flagEntreeMidi='on';
			},
			{//
				MIDIIn.disconnect;
				~flagEntreeMidi='off';
		})};
		~wg.view.decorator.nextLine;
		// algo analyse
		~algoAnalyse = PopUpMenu(~wg,Rect(0,0,120,18)).background_(Color.grey(0.5, 0.8)).stringColor_(Color.white).items=["Onsets","Pitch","Pitch2","KeyTrack","Keyboard", "MIDI"];
		~algoAnalyse.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~algoAnalyse",view.value);
			~flagAlgorithm = view.value;
			~seuilanalyse.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(0));
			if(view.value == 0, {~filtreanalyse.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(1))});
			if(view.value == 1, {~filtreanalyse.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(1))});
			if(view.value == 2, {~filtreanalyse.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(1))});
			if(view.value == 3, {~filtreanalyse.valueAction_(~paraAlgoAnalyseAudio.wrapAt(view.value).wrapAt(1))});
			switch(view.value,
				0, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot File Onsets",
							[\busFileIn, ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot Onsets",
							[\in, ~canalAudioIn, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN', {~audioIn.value.run(true);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN', {~audioFile.value.run(true);~synthPlayFile.run(true);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				},
				1, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot File Pitch",
							[\busFileIn, ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot Pitch",
							[\in, ~canalAudioIn, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~audioFile.value.run(false);~synthPlayFile.run(false);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.run(true);~audioIn.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				},
				2, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot File Pitch2",
							[\busFileIn, ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot Pitch2",
							[\in, ~canalAudioIn, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~audioFile.value.run(false);~synthPlayFile.run(false);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.run(true);~audioIn.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				},
				3, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot File KeyTrack",
							[\busFileIn, ~busFileIn.index, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot KeyTrack",
							[\in, ~canalAudioIn, \filtre, ~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapAt(1), \seuil, ~seuilanalyse.value], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~audioFile.value.run(false);~synthPlayFile.run(false);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.run(true);~audioIn.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				},
				4, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot Keyboard File", [\trigger, 0], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot Keyboard", [\trigger, 0], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~audioFile.value.run(false);~synthPlayFile.run(false);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.run(true);~audioIn.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				},
				5, {
					s.bind{
						~audioIn.value.run(false); ~audioFile.value.run(false);~synthPlayFile.run(false);
						~tempoIn.value.run(false);~tempoFile.value.run(false);
						s.sync;
						~audioIn.free;
						s.sync;
						~audioFile.free;
						s.sync;
						~audioFile=Synth.newPaused("OSC WekRobot MIDI File", [\trigger, 0], ~groupeAnalyse, \addToTail);
						s.sync;
						~audioIn=Synth.newPaused("OSC WekRobot MIDI", [\trigger, 0], ~groupeAnalyse, \addToTail);
						s.sync;
						if(~flagEntreeMode == 'Audio IN' and: {~startsysteme.value == 1}, {~audioIn.value.run(true);~audioFile.value.run(false);~synthPlayFile.run(false);~tempoIn.value.run(true);~tempoFile.value.run(false)});
						s.sync;
						if(~flagEntreeMode == 'File IN' and: {~startsysteme.value == 1}, {~audioFile.value.run(true);~synthPlayFile.run(true);~audioIn.value.run(false);~tempoIn.value.run(false);~tempoFile.value.run(true)});
						s.sync;
					};
				}
			);
		};
		~tempoButton = Button(~wg,Rect(10,10, 150, 18)).states=[["Tempo Analyze Off", Color.black, Color.green(0.8, 0.25)],["Tempo Analyze On", Color.black, Color.red(0.8, 0.25)]];
		~tempoButton.action = {arg view;
			~writepartitions.value(nil,'control panel normal','off',"~tempoButton",view.value);
			if (view.value == 1,
				{~flagTempoAnalyze = 'on';~tempoprocessinstrument.enabled_(false)},
				{~flagTempoAnalyze = 'off';~tempoprocessinstrument.valueAction = 60;~tempoprocessinstrument.enabled_(true)});
		};
		~wg.view.decorator.nextLine;
		~volumeFileIn=EZSlider(~wg, 145 @ 18, "File LevelOut", ControlSpec(-inf, 6, \db, 0),
			{|ez| ~writepartitions.value(nil,'control panel normal','off',"~volumeFileIn",ez.value);
				~synthPlayFile.set(\volume, ez.value.dbamp)}, -inf, labelWidth: 75, numberWidth: 35);
		~offsetFileIn=EZSlider(~wg, 140 @ 18, "File offset", ControlSpec(0, 1, \lin, 0),
			{|ez| ~writepartitions.value(nil,'control panel normal','off',"~offsetFileIn",ez.value);
				s.bind{
					~synthPlayFile.set(\trig, -1);
					s.sync;
					~synthPlayFile.set(\offset, ez.value);
					s.sync;
					~synthPlayFile.set(\trig, 1);
					s.sync};
		}, 0, labelWidth: 55, numberWidth: 35);

		~choiceFilter = PopUpMenu(~wg, Rect(0, 0, 75, 18)).
		items_(["Off", "LoPass", "HiPass"]).
		action = {|filter|
			~writepartitions.value(nil,'control panel normal','off',"~choiceFilter",filter.value);
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
		~hzFilter = EZSlider(~wg, 170 @ 18, "HzPass", \freq,
			{|ez|
				~writepartitions.value(nil,'control panel normal','off',"~hzFilter",ez.value);
				~groupeAnalyse.set(\hzPass, ez.value)}, 440,labelWidth: 50, numberWidth: 50);
		~hzFilter.enabled_(false);

		~seuilanalyse=EZSlider(~wg, 250 @ 18, "Thresh Analyse", ControlSpec(0.01, 1, \lin), {|ez|
			~writepartitions.value(nil,'control panel normal','off',"~seuilanalyse",ez.value);
			~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapPut(0, ez.value);
			~audioIn.set(\seuil,ez.value);~audioFile.set(\seuil,ez.value)}, 0.5, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~filtreanalyse=EZSlider(~wg, 250 @ 18, "Filter Analyse", ControlSpec(0.01, 1, \lin), {|filtre|
			~writepartitions.value(nil,'control panel normal','off',"Filtre Analyse Audio",filtre.value);
			~paraAlgoAnalyseAudio.wrapAt(~algoAnalyse.value).wrapPut(1, filtre.value);~audioIn.set(\filtre, filtre.value);~audioFile.set(\filtre, filtre.value);
		}, 0.5, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		// Calculation chaos start stop playing
		~calculationchaos = Button(~wg,Rect(0,0,85,18));
		~calculationchaos.states = [["Chaos Off", Color.black,  Color.green(0.8, 0.25)],["Chaos On", Color.white, Color.red(0.8, 0.25)]];
		~calculationchaos.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationchaos",view.value);if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
			~nombreinstrument.do({arg instr;
				~listechaosfreq.wrapPut(instr,[]);~listechaosamp.wrapPut(instr,[]);
				~listechaosduree.wrapPut(instr,[])});
			~routinechaosin.play(quant: Quant.new(1));
			~tempoprocesschaos.enabled_(true);
		},
		{
			~routinechaosin.stop;
			~tempoprocesschaos.enabled_(false);
		})};
		// Calculation neurone start stop playing
		~calculationneurone = Button(~wg,Rect(0,0,85,18));
		~calculationneurone.states = [["Neural Off", Color.black,  Color.green(0.8, 0.25)],["Neural On", Color.white, Color.red(0.8, 0.25)]];
		~calculationneurone.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationneurone",view.value);if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
			~nombreinstrument.do({arg instr;
				~listeneuronefreq.wrapPut(instr,[]);~listeneuroneamp.wrapPut(instr,[]);
				~listeneuroneduree.wrapPut(instr,[])});
			~routineneuronein.play(quant: Quant.new(1));
			~tempoprocessneurone.enabled_(true);
		},
		{~routineneuronein.stop;
			~tempoprocessneurone.enabled_(false);
		})};
		// Calculation genetique start stop playing
		~calculationgenetique = Button(~wg,Rect(0,0,85,18));
		~calculationgenetique.states = [["Genetic Off", Color.black,  Color.green(0.8, 0.25)],["Genetic On", Color.white, Color.red(0.8, 0.25)]];
		~calculationgenetique.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationgenetique",view.value);if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
			~nombreinstrument.do({arg instr;
				~listegenetiquefreq.wrapPut(instr,[]);~listegenetiqueamp.wrapPut(instr,[]);
				~listegenetiqueduree.wrapPut(instr,[])});
			~routinegenetiquein.play(quant: Quant.new(1));
			~tempoprocessgenetique.enabled_(true);
		},
		{~routinegenetiquein.stop;
			~tempoprocessgenetique.enabled_(false);
		})};
		// Calculation algorithmes start stop playing
		~calculationalgorithmes= Button(~wg,Rect(0,0,85,18));
		~calculationalgorithmes.states = [["Algo Off", Color.black,  Color.green(0.8, 0.25)],["Algo On", Color.white, Color.red(0.8, 0.25)]];
		~calculationalgorithmes.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationalgorithmes",view.value);if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
			~nombreinstrument.do({arg instr;
				~listealgo1freq.wrapPut(instr,[]);~listealgo1amp.wrapPut(instr,[]);~listealgo1duree.wrapPut(instr,[]);~listealgo2freq.wrapPut(instr,[]);~listealgo2amp.wrapPut(instr,[]);~listealgo2duree.wrapPut(instr,[])});
			~routinealgorithmes.play(quant: Quant.new(1));
			~tempoprocessalgorithmes.enabled_(true);
		},
		{~routinealgorithmes.stop;
			~tempoprocessalgorithmes.enabled_(false);
		})};
		// Calculation automation start stop playing
		~calculationautomation = Button(~wg,Rect(0,0,85,18));
		~calculationautomation.states = [["Random Off", Color.black,  Color.green(0.8, 0.25)],["Random On", Color.white, Color.red(0.8, 0.25)]];
		~calculationautomation.action = {|view| ~writepartitions.value(nil,'control panel normal','off',"~calculationautomation",view.value);if(~startsysteme.value == 1 and: {view.value == 1}, {// set ON les routines de calcul
			~routineautomation.play(quant: Quant.new(1));
			~tempoprocessautomation.enabled_(true);
		},
		{~routineautomation.stop;
			~tempoprocessautomation.enabled_(false);
		})};
		// Affichage Metronome
		~metronomeGUI = EZNumber(~wg, 110@18, "Metronome", ControlSpec(0, 64, \lin, 0), {arg view; nil}, 0, false, labelWidth:70, numberWidth:40);
		~wg.view.decorator.nextLine;
		~amplitudegenerale=EZSlider(~wg, 250 @ 18, "Volume", ControlSpec(-inf, 12, \db), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~amplitudegenerale",ez.value);~amplitudegeneraleinstrument=ez.value}, 0, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocessinstrument=EZSlider(~wg, 250 @ 18, "Tempo", ControlSpec(1, 960, \exp, 0), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocessinstrument", ez.value);
			~tempoSystem.tempo_(ez.value / 60);
			// Send tempo to slave soft
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPtempo', ez.value)})}, 60, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~nombreBeats=EZSlider(~wg, 250 @ 18, "Beats / Bare", ControlSpec(1, 64, \exp, 0), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~nombreBeats", ez.value);~nombreBeatsBare=ez.value;
			// Send Bare to slave soft
			if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPbare', ez.value)});
			~tempoSystem.schedAbs(~tempoSystem.nextBar, {~tempoSystem.beatsPerBar_(~nombreBeatsBare); nil})}, 1, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocesschaos=EZSlider(~wg, 250 @ 18, "ChaosIn speed", ControlSpec(1, 100, \lin,1), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocesschaos",ez.value);~tempochaos=ez.value;
		}, 16, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocessneurone=EZSlider(~wg, 250 @ 18, "NeuroneIn Speed", ControlSpec(1, 100, \lin,1), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocessneurone",ez.value);~temponeurone=ez.value}, 64, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocessgenetique=EZSlider(~wg, 250 @ 18, "GenetiqueIn Speed", ControlSpec(1, 100, \lin,1), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocessgenetique",ez.value);~tempogenetique=ez.value}, 16, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocessalgorithmes=EZSlider(~wg, 250 @ 18, "Algorithm Speed", ControlSpec(1, 100, \lin,1), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocessalgorithmes",ez.value);~tempoalgorithmes=ez.value}, 16, labelWidth: 115,numberWidth:50);
		~wg.view.decorator.nextLine;
		~tempoprocessautomation=EZSlider(~wg, 250 @ 18, "Automation Speed", ControlSpec(1, 100, \lin,1), {|ez| ~writepartitions.value(nil,'control panel normal','off',"~tempoprocessautomation",ez.value);~tempoautomation=ez.value}, 24, labelWidth: 115,numberWidth:50);
		// instrument en cours
		~instractu=PopUpMenu(~wg,Rect(0,0, 145, 18)).background_(Color.grey(0.5, 0.8)).items = ~listeinstruments;
		~instractu.action={arg item;~writepartitions.value(nil,'control panel normal','off',"~instractu",item.value);~instrumentactuel=item.value.asInteger};
		// LSC
		~saveloaddatasinstr =PopUpMenu(~wg,Rect(0, 0, 140, 18)).background_(Color.grey(0.5, 0.8)).items = ~instrumentsOSchoiceControl;
		~saveloaddatasinstr.action = {arg item;~writepartitions.value(nil,'control panel normal','off',"~saveloaddatasinstr",item.value);~slcinstruments.value(~instrumentactuel,item);
			~saveloaddatasinstr.value=0};
		~wg.view.decorator.nextLine;
		// load sequences
		~sequencespanel=PopUpMenu(~wg,Rect(0,0, 140, 18)).background_(Color.grey(0.5, 0.8)).items = ~sequenceliste;
		~sequencespanel.action={arg item;~writepartitions.value(nil,'control panel normal','off',"~sequencespanel",item.value);~foncseq.value(~instrumentactuel,item);
			//~sequencespanel.valueAction=0;
		};
		// Load partitions
		~partitionspanel=PopUpMenu(~wg,Rect(0,0, 140, 18)).background_(Color.grey(0.5, 0.8)).items = ~partitionsliste;
		~partitionspanel.action={arg item;~writepartitions.value(nil,'control panel normal','off',"~partitionspanel",item.value);~foncpart.value(~instrumentactuel,item);
			//~partitionspanel.valueAction=0;
		};
		~wg.view.decorator.nextLine;
		// load instruments
		~loadinstrument = PopUpMenu(~wg,Rect(0,0, 140, 18)).background_(Color.grey(0.5, 0.8)).items = ~singleinstrument;
		~loadinstrument.action = {|item| var file, datas, datasfile, duree;
			~writepartitions.value(nil,'control panel normal','off',"~loadinstrument",item.value);
			if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instrument"+item.value.asString++".scd"),{
				file=File(~nompathdata++"instrument"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
				~listewindow.wrapAt(~instrumentactuel).name="WekRobot by HP Instrument"+(~instrumentactuel+1).asString+~nompathdata+"instrument"+item.value.asString++".scd";
				if(~flagSynchro == 'on', {duree = ~tempoSystem.nextBar - 0.1}, {duree=~tempoSystem.beats});
				if(~startsysteme.value==1, {
					~tempoSystem.schedAbs(duree, {
						~routineinstrument.wrapAt(~instrumentactuel).stop;
						// Set MIDI Off
						if(~flagMidiOut == 'on', {
							~freqMidi.wrapAt(~instrumentactuel).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(~instrumentactuel), ~freqMidi.wrapAt(~instrumentactuel).wrapAt(index), 0);
								if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(~instrumentactuel), ~freqMidi.wrapAt(~instrumentactuel).wrapAt(index), 0)});
							});
						});
						~flagfreq.wrapPut(~instrumentactuel, 0);~flagamp.wrapPut(~instrumentactuel, 0);~flagduree.wrapPut(~instrumentactuel, 0);~flagneuronefreq.wrapPut(~instrumentactuel, 0);~flagneuroneamp.wrapPut(~instrumentactuel, 0);~flagneuroneduree.wrapPut(~instrumentactuel, 0);~flagoutneuronefreq.wrapPut(~instrumentactuel, 0);~flagoutneuroneamp.wrapPut(~instrumentactuel, 0);~flagoutneuroneduree.wrapPut(~instrumentactuel, 0);~flaggenetiquefreq.wrapPut(~instrumentactuel, 0);~flaggenetiqueamp.wrapPut(~instrumentactuel, 0);~flaggenetiqueduree.wrapPut(~instrumentactuel, 0);
						{~fonctionloaddatasinstrument.value(~instrumentactuel, datas.wrapAt(0),'on')}.defer;
						~routineinstrument.wrapAt(~instrumentactuel).play(quant: Quant.new(1)); nil});
				});
			})});
			//~loadinstrument.valueAction=0;
		};
		~loadinstruments = PopUpMenu(~wg,Rect(0,0,140,18)).background_(Color.grey(0.5, 0.8)).items = ~multiinstruments;
		~loadinstruments.action = {|item| var file, datas, datasfile, duree;
			~writepartitions.value(nil,'control panel normal','off',"~loadinstruments",item.value);
			if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instruments"+item.value.asString++".scd"),{file=File(~nompathdata++"instruments"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
				for(0, ~nombreinstrument-1, {arg ii;
					~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+~nompathdata+"instruments"+item.value.asString++".scd";
				});
				if(~flagSynchro == 'on', {duree = ~tempoSystem.nextBar - 0.1}, {duree=~tempoSystem.beats});
				if(~startsysteme.value==1, {
					~tempoSystem.schedAbs(duree, {
						//+ Load datas Control Panel
						{~readcontrolpanel.value(datas.last.at(0))}.defer;
						for(0, ~nombreinstrument-1, {arg ii;
							~routineinstrument.wrapAt(ii).stop;
							// Set MIDI Off
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(ii).value >= 0}, {
								~freqMidi.wrapAt(ii).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(ii), ~freqMidi.wrapAt(ii).wrapAt(index), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(ii), ~freqMidi.wrapAt(ii).wrapAt(index), 0)});
								});
							});
							~flagfreq.wrapPut(ii,0);~flagamp.wrapPut(ii,0);~flagduree.wrapPut(ii,0);~flagneuronefreq.wrapPut(ii,0);~flagneuroneamp.wrapPut(ii,0);~flagneuroneduree.wrapPut(ii,0);~flagoutneuronefreq.wrapPut(ii,0);~flagoutneuroneamp.wrapPut(ii,0);~flagoutneuroneduree.wrapPut(ii,0);~flaggenetiquefreq.wrapPut(ii,0);~flaggenetiqueamp.wrapPut(ii,0);~flaggenetiqueduree.wrapPut(ii,0);
							{~fonctionloaddatasinstrument.value(ii, datas.wrapAt(ii),'on')}.defer;
							~routineinstrument.wrapAt(ii).play(quant: Quant.new(1))}); nil});
				});
			})});
			//~loadinstruments.valueAction=0;
		};
		~wg.view.decorator.nextLine;
		// OSC state display
		~stateOSCdisplay = StaticText(~wg, Rect(0, 0, 140, 18)).string_("OSC Off");
		~stateOSCdisplay.align = \center;
		~stateOSCdisplay.stringColor_(Color.black(1, 1)).font_(Font("Georgia", 12));
		~stateOSCdisplay.background = Color.grey;
		~listewindow=~listewindow.add(~wg);
		~volumeFileIn.enabled_(false);
		~offsetFileIn.enabled_(false);
		~tempoprocesschaos.enabled_(false);
		~tempoprocessneurone.enabled_(false);
		~tempoprocessgenetique.enabled_(false);
		~tempoprocessalgorithmes.enabled_(false);
		~tempoprocessautomation.enabled_(false);

		// Audio In Canals
		~setAudioInSoft = PopUpMenu(~wg,Rect(0,0,100,18)).items=["AudioIn 1", "AudioIn 2", "AudioIn 3", "AudioIn 4", "AudioIn 5", "AudioIn 6", "AudioIn 7", "AudioIn 8", "AudioIn 9", "AudioIn 10", "AudioIn 11", "AudioIn 12", "AudioIn 13", "AudioIn 14", "AudioIn 15", "AudioIn 16", "AudioIn 17", "AudioIn 18", "AudioIn 19", "AudioIn 20", "AudioIn 21", "AudioIn 22", "AudioIn 23", "AudioIn 24", "AudioIn 25", "AudioIn 26", "AudioIn 27", "AudioIn 28", "AudioIn 29", "AudioIn 30", "AudioIn 31", "AudioIn 32"];
		~setAudioInSoft.action = {|view|
			~writepartitions.value(nil,'control panel normal','off',"~setAudioInSoft", view.value);
			~canalAudioIn = view.value;
			~groupeAnalyse.set(\in, view.value);
		};

		// Audio In Display (for testing)
		~audioDisplay = StaticText(~wg, Rect(0, 0, 40, 18)).string_("Audio").background_(Color.white(1, 1));

		// Wekinator
		Button(~wg, Rect(0, 0, 70, 18)).states_([["WekRec On", Color.magenta], ["WekRec Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {sender.sendMsg("/wekinator/control/stopRecording")},
				1, {sender.sendMsg("/wekinator/control/startRecording");
					~wg.view.children.at(36).valueAction = 0;// run
				}
			);
		});
		Button(~wg, Rect(0, 0, 70, 18)).states_([["WekTrain On", Color.magenta]]).action_({|view|
			sender.sendMsg("/wekinator/control/train");
			~wg.view.children.at(34).valueAction = 0;// run
			~wg.view.children.at(36).valueAction = 0;// run
		});
		Button(~wg, Rect(0, 0, 70, 18)).states_([["WekRun On", Color.magenta], ["WekRun Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {flagStreamMFCC = 'off'; sender.sendMsg("/wekinator/control/stopRunning");
				},
				1, {flagStreamMFCC = 'wek'; sender.sendMsg("/wekinator/control/startRunning");
					~wg.view.children.at(34).valueAction = 0;// run
				}
			);
		});
		Button(~wg, Rect(0, 0, 45, 18)).states_([["WTP On", Color.magenta], ["WTP Off", Color.red]]).action_({|view|
			switch(view.value,
				0, {flagWTP = 'off'},
				1, {flagWTP = 'on'});
		}).valueAction_(1);
		NumberBox(~wg, 25 @ 18).value_(4).action_({|ez| timeWekPreset = ez.value});

		~listewindow=~listewindow.add(~windowMasterFX);
		~listewindow=~listewindow.add(~wp);
		~listewindow=~listewindow.add(windowKeyboard);
		~listewindow=~listewindow.add(windowVST);

		// Setup Font View
		~listewindow.do({arg window;
			window.view.do({arg view;
				view.children.do({arg subView;
					subView.font = Font("Helvetica", 10);
				});
			});
		});

		~fonctionLoadInstrument = {arg item;
			var file, datas, datasfile, duree;
			~writepartitions.value(nil,'control panel normal','off',"~loadinstrument",item.value);
			if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instrument"+item.value.asString++".scd"),{
				file=File(~nompathdata++"instrument"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
				~listewindow.wrapAt(~instrumentactuel).name="WekRobot by HP Instrument"+(~instrumentactuel+1).asString+~nompathdata+"instrument"+item.value.asString++".scd";
				if(~flagSynchro == 'on', {duree = ~tempoSystem.nextBar - 0.1}, {duree=~tempoSystem.beats});
				//if(~startsysteme.value==1, {
				~tempoSystem.schedAbs(duree, {
					~routineinstrument.wrapAt(~instrumentactuel).stop;
					// Set MIDI Off
					if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(~instrumentactuel).value >= 0}, {
						~freqMidi.wrapAt(~instrumentactuel).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(~instrumentactuel), ~freqMidi.wrapAt(~instrumentactuel).wrapAt(index), 0);
							if(flagVST == 'on', {~fxVST.noteOff(~canalMidiOutInstr.wrapAt(~instrumentactuel), ~freqMidi.wrapAt(~instrumentactuel).wrapAt(index), 0)});
						});
					});
					~flagfreq.wrapPut(~instrumentactuel, 0);~flagamp.wrapPut(~instrumentactuel, 0);~flagduree.wrapPut(~instrumentactuel, 0);~flagneuronefreq.wrapPut(~instrumentactuel, 0);~flagneuroneamp.wrapPut(~instrumentactuel, 0);~flagneuroneduree.wrapPut(~instrumentactuel, 0);~flagoutneuronefreq.wrapPut(~instrumentactuel, 0);~flagoutneuroneamp.wrapPut(~instrumentactuel, 0);~flagoutneuroneduree.wrapPut(~instrumentactuel, 0);~flaggenetiquefreq.wrapPut(~instrumentactuel, 0);~flaggenetiqueamp.wrapPut(~instrumentactuel, 0);~flaggenetiqueduree.wrapPut(~instrumentactuel, 0);
					{~fonctionloaddatasinstrument.value(~instrumentactuel, datas.wrapAt(0),'on')}.defer;
					~routineinstrument.wrapAt(~instrumentactuel).play(quant: Quant.new(1)); nil});
				//});
			})});
		};

		~fonctionLoadInstruments = {arg item;
			var file, datas, datasfile, duree;
			~writepartitions.value(nil,'control panel normal','off',"~loadinstruments",item.value);
			if(item.value ==0 ,{nil},{if(File.exists(~nompathdata++"instruments"+item.value.asString++".scd"),
				{
					numPreset = item.value; lastNumPreset = item.value;
					file=File(~nompathdata++"instruments"+item.value.asString++".scd","r");datas=file.readAllString.interpret;file.close;
					for(0, ~nombreinstrument-1, {arg ii;
						~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+~nompathdata+"instruments"+item.value.asString++".scd";
					});
					if(~flagSynchro == 'on', {duree = ~tempoSystem.nextBar - 0.1}, {duree=~tempoSystem.beats});
					//if(~startsysteme.value==1, {
					~tempoSystem.schedAbs(duree, {
						//+ Load datas Control Panel
						{~readcontrolpanel.value(datas.last.at(0))}.defer;
						for(0, ~nombreinstrument-1, {arg ii;
							~routineinstrument.wrapAt(ii).stop;
							// Set MIDI Off
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(ii).value >= 0}, {
								~freqMidi.wrapAt(ii).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(ii), ~freqMidi.wrapAt(ii).wrapAt(index), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(ii), ~freqMidi.wrapAt(ii).wrapAt(index), 0)});
								});
							});
							~flagfreq.wrapPut(ii,0);~flagamp.wrapPut(ii,0);~flagduree.wrapPut(ii,0);~flagneuronefreq.wrapPut(ii,0);~flagneuroneamp.wrapPut(ii,0);~flagneuroneduree.wrapPut(ii,0);~flagoutneuronefreq.wrapPut(ii,0);~flagoutneuroneamp.wrapPut(ii,0);~flagoutneuroneduree.wrapPut(ii,0);~flaggenetiquefreq.wrapPut(ii,0);~flaggenetiqueamp.wrapPut(ii,0);~flaggenetiqueduree.wrapPut(ii,0);
							{~fonctionloaddatasinstrument.value(ii, datas.wrapAt(ii),'on')}.defer;
							~routineinstrument.wrapAt(ii).play(quant: Quant.new(1))}); nil});
					//});
			})});
		};

		~wg.front;

	}

	shortCuts {
		// Keyboard Fonction pour toutes les Views
		~fonctionShortCuts = {arg window;
			~listewindow.do({arg w;
				w.view.keyDownAction = {arg view,char,modifiers,unicode,keycode, numinstr;
					//[char, modifiers,unicode,keycode].postln;
					// Touches pave numerique
					if(modifiers==2097152 and: {unicode==48} and: {keycode==82},{numinstr=9;~keysfonction.value(10,10,9)});
					if(modifiers==2097152 and: {unicode==49} and: {keycode==83},{numinstr=0;~keysfonction.value(1,1,0)});
					if(modifiers==2097152 and: {unicode==50} and: {keycode==84},{numinstr=1;~keysfonction.value(2,2,1)});
					if(modifiers==2097152 and: {unicode==51} and: {keycode==85},{numinstr=2;~keysfonction.value(3,3,2)});
					if(modifiers==2097152 and: {unicode==52} and: {keycode==86},{numinstr=3;~keysfonction.value(4,4,3)});
					if(modifiers==2097152 and: {unicode==53} and: {keycode==87},{numinstr=4;~keysfonction.value(5,5,4)});
					if(modifiers==2097152 and: {unicode==54} and: {keycode==88},{numinstr=5;~keysfonction.value(6,6,5)});
					if(modifiers==2097152 and: {unicode==55} and: {keycode==89},{numinstr=6;~keysfonction.value(7,7,6)});
					if(modifiers==2097152 and: {unicode==56} and: {keycode==91},{numinstr=7;~keysfonction.value(8,8,7)});
					if(modifiers==2097152 and: {unicode==57} and: {keycode==92},{numinstr=8;~keysfonction.value(9,9,8)});
					if(modifiers==2228224 and: {unicode==49} and: {keycode==83},{numinstr=0;~keysfonction.value(11,11,11)});
					if(modifiers==2228224 and: {unicode==50} and: {keycode==84},{numinstr=1;~keysfonction.value(12,12,12)});
					if(modifiers==2228224 and: {unicode==51} and: {keycode==85},{numinstr=2;~keysfonction.value(13,13,13)});
					if(modifiers==2228224 and: {unicode==52} and: {keycode==86},{numinstr=3;~keysfonction.value(14,14,14)});
					if(modifiers==2228224 and: {unicode==53} and: {keycode==87},{numinstr=4;~keysfonction.value(15,15,15)});
					if(modifiers==2228224 and: {unicode==54} and: {keycode==88},{numinstr=5;~keysfonction.value(16,16,16)});
					if(modifiers==2228224 and: {unicode==55} and: {keycode==89},{numinstr=6;~keysfonction.value(17,17,17)});
					if(modifiers==2228224 and: {unicode==56} and: {keycode==91},{numinstr=7;~keysfonction.value(18,18,18)});
					if(modifiers==2228224 and: {unicode==57} and: {keycode==92},{numinstr=8;~keysfonction.value(19,19,19)});
					if(modifiers==2228224 and: {unicode==48} and: {keycode==82},{numinstr=9;~keysfonction.value(20,20,20)});
					if(modifiers==2621440 and: {unicode==49} and: {keycode==83},{numinstr=0;~keysfonction.value(21,21,21)});
					if(modifiers==2621440 and: {unicode==50} and: {keycode==84},{numinstr=1;~keysfonction.value(22,22,22)});
					if(modifiers==2621440 and: {unicode==51} and: {keycode==85},{numinstr=2;~keysfonction.value(23,23,23)});
					if(modifiers==2621440 and: {unicode==52} and: {keycode==86},{numinstr=3;~keysfonction.value(24,24,24)});
					if(modifiers==2621440 and: {unicode==53} and: {keycode==87},{numinstr=4;~keysfonction.value(25,25,25)});
					if(modifiers==2621440 and: {unicode==54} and: {keycode==88},{numinstr=5;~keysfonction.value(26,26,26)});
					if(modifiers==2621440 and: {unicode==55} and: {keycode==89},{numinstr=6;~keysfonction.value(27,27,27)});
					if(modifiers==2621440 and: {unicode==56} and: {keycode==91},{numinstr=7;~keysfonction.value(28,28,28)});
					if(modifiers==2621440 and: {unicode==57} and: {keycode==92},{numinstr=8;~keysfonction.value(29,29,29)});
					if(modifiers==2621440 and: {unicode==48} and: {keycode==82},{numinstr=9;~keysfonction.value(30,30,30)});
					if(modifiers==2752512 and: {unicode==49} and: {keycode==83},{numinstr=0;~keysfonction.value(31,31,31)});
					if(modifiers==2752512 and: {unicode==50} and: {keycode==84},{numinstr=1;~keysfonction.value(32,32,32)});
					if(modifiers==2752512 and: {unicode==51} and: {keycode==85},{numinstr=2;~keysfonction.value(33,33,33)});
					if(modifiers==2752512 and: {unicode==52} and: {keycode==86},{numinstr=3;~keysfonction.value(34,34,34)});
					if(modifiers==2752512 and: {unicode==53} and: {keycode==87},{numinstr=4;~keysfonction.value(35,35,35)});
					if(modifiers==2752512 and: {unicode==54} and: {keycode==88},{numinstr=5;~keysfonction.value(36,36,36)});
					if(modifiers==2752512 and: {unicode==55} and: {keycode==89},{numinstr=6;~keysfonction.value(37,37,37)});
					if(modifiers==2752512 and: {unicode==56} and: {keycode==91},{numinstr=7;~keysfonction.value(38,38,38)});
					if(modifiers==2752512 and: {unicode==57} and: {keycode==92},{numinstr=8;~keysfonction.value(39,39,39)});
					if(modifiers==2752512 and: {unicode==48} and: {keycode==82},{numinstr=9;~keysfonction.value(40,40,40)});
					// Touches clavier numerique
					if(modifiers==0 and: {unicode==49} and: {keycode==18},{numinstr=0;~keysfonction.value(1,1,0)});
					if(modifiers==0 and: {unicode==50} and: {keycode==19},{numinstr=1;~keysfonction.value(2,2,1)});
					if(modifiers==0 and: {unicode==51} and: {keycode==20},{numinstr=2;~keysfonction.value(3,3,2)});
					if(modifiers==0 and: {unicode==52} and: {keycode==21},{numinstr=3;~keysfonction.value(4,4,3)});
					if(modifiers==0 and: {unicode==53} and: {keycode==23},{numinstr=4;~keysfonction.value(5,5,4)});
					if(modifiers==0 and: {unicode==54} and: {keycode==22},{numinstr=5;~keysfonction.value(6,6,5)});
					if(modifiers==0 and: {unicode==55} and: {keycode==26},{numinstr=6;~keysfonction.value(7,7,6)});
					if(modifiers==0 and: {unicode==56} and: {keycode==28},{numinstr=7;~keysfonction.value(8,8,7)});
					if(modifiers==0 and: {unicode==57} and: {keycode==25},{numinstr=8;~keysfonction.value(9,9,8)});
					if(modifiers==0 and: {unicode==48} and: {keycode==29},{numinstr=9;~keysfonction.value(10,10,9)});
					if(modifiers==131072 and: {unicode==43} and: {keycode==18},{numinstr=0;~keysfonction.value(11,11,11)});
					if(modifiers==131072  and: {unicode==34} and: {keycode==19},{numinstr=1;~keysfonction.value(12,12,12)});
					if(modifiers==131072  and: {unicode==42} and: {keycode==20},{numinstr=2;~keysfonction.value(13,13,13)});
					if(modifiers==131072  and: {unicode==231} and: {keycode==21},numinstr=3;{~keysfonction.value(14,14,14)});
					if(modifiers==131072  and: {unicode==37} and: {keycode==23},{numinstr=4;~keysfonction.value(15,15,15)});
					if(modifiers==131072  and: {unicode==38} and: {keycode==22},{numinstr=5;~keysfonction.value(16,16,16)});
					if(modifiers==131072 and: {unicode==47} and: {keycode==26},{numinstr=6;~keysfonction.value(17,17,17)});
					if(modifiers==131072 and: {unicode==40} and: {keycode==28},{numinstr=7;~keysfonction.value(18,18,18)});
					if(modifiers==131072 and: {unicode==41} and: {keycode==25},{numinstr=8;~keysfonction.value(19,19,19)});
					if(modifiers==131072 and: {unicode==61} and: {keycode==29},{numinstr=9;~keysfonction.value(20,20,20)});
					if(modifiers==524288 and: {unicode==177} and: {keycode==18},{numinstr=0;~keysfonction.value(21,21,21)});
					if(modifiers==524288 and: {unicode==8220} and: {keycode==19},{numinstr=1;~keysfonction.value(22,22,22)});
					if(modifiers==524288 and: {unicode==35} and: {keycode==20},{numinstr=2;~keysfonction.value(23,23,23)});
					if(modifiers==524288 and: {unicode==199} and: {keycode==21},{numinstr=3;~keysfonction.value(24,24,24)});
					if(modifiers==524288 and: {unicode==91} and: {keycode==23},{numinstr=4;~keysfonction.value(25,25,25)});
					if(modifiers==524288 and: {unicode==93} and: {keycode==22},{numinstr=5;~keysfonction.value(26,26,26)});
					if(modifiers==524288 and: {unicode==124} and: {keycode==26},{numinstr=6;~keysfonction.value(27,27,27)});
					if(modifiers==524288 and: {unicode==123} and: {keycode==28},{numinstr=7;~keysfonction.value(28,28,28)});
					if(modifiers==524288 and: {unicode==125} and: {keycode==25},{numinstr=8;~keysfonction.value(29,29,29)});
					if(modifiers==524288 and: {unicode==8800} and: {keycode==29},{numinstr=9;~keysfonction.value(30,30,30)});
					if(modifiers==655360 and: {unicode==8734} and: {keycode==18},{numinstr=0;~keysfonction.value(31,31,31)});
					if(modifiers==655360 and: {unicode==8221} and: {keycode==19},{numinstr=1;~keysfonction.value(32,32,32)});
					if(modifiers==655360 and: {unicode==8249} and: {keycode==20},{numinstr=2;~keysfonction.value(33,33,33)});
					if(modifiers==655360 and: {unicode==8260} and: {keycode==21},{numinstr=3;~keysfonction.value(34,34,34)});
					if(modifiers==655360 and: {unicode==91} and: {keycode==23},{numinstr=4;~keysfonction.value(35,35,35)});
					if(modifiers==655360 and: {unicode==93} and: {keycode==22},{numinstr=5;~keysfonction.value(36,36,36)});
					if(modifiers==655360 and: {unicode==92} and: {keycode==26},{numinstr=6;~keysfonction.value(37,37,37)});
					if(modifiers==655360 and: {unicode==210} and: {keycode==28},{numinstr=7;~keysfonction.value(38,38,38)});
					if(modifiers==655360 and: {unicode==212} and: {keycode==25},{numinstr=8;~keysfonction.value(39,39,39)});
					if(modifiers==655360 and: {unicode==218} and: {keycode==29},{numinstr=9;~keysfonction.value(40,40,40)});
					// all instruments key -
					if(char==$-,{~keysfonction.value(1,~nombreinstrument)});
					// all seq part etc... key alt=
					if(char==$_,{~keysfonction.value(1,40)});
					// Control Panel
					//~commande=nil;
					// key W -> window control panel
					if(modifiers==131072 and: {unicode==87} and: {keycode==13},{~windowactive='control panel';~wg.front;~commande=nil;
						~writepartitions.value(nil,'key','off',"~windowactive='control panel';~wg.front",nil)});
					// key altW -> window partitions
					if(modifiers==655360 and: {unicode==87} and: {keycode==13},{~windowactive='partitions';~wp.front;~commande=nil;
						~writepartitions.value(nil,'key','off',"~windowactive='partitions';~wp.front",nil)});
					// Key "altw" -> affichage windows ->
					if(modifiers==524288 and: {unicode==119} and: {keycode==13},{if(~windowactive == 'control panel',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 1';~listewindow.wrapAt(0).front",nil);~windowactive='instrument 1';~listewindow.wrapAt(0).front;~instractu.valueAction=0},
						{if(~windowactive == 'instrument 1',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 2';~listewindow.wrapAt(1).front",nil);~windowactive='instrument 2';~listewindow.wrapAt(1).front;~instractu.valueAction=1},
							{if(~windowactive == 'instrument 2',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 3';~listewindow.wrapAt(2).front",nil);~windowactive='instrument 3';~listewindow.wrapAt(2).front;~instractu.valueAction=2},
								{if(~windowactive == 'instrument 3',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 4';~listewindow.wrapAt(3).front",nil);~windowactive='instrument 4';~listewindow.wrapAt(3).front;~instractu.valueAction=3},
									{if(~windowactive == 'instrument 4',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 5';~listewindow.wrapAt(4).front",nil);~windowactive='instrument 5';~listewindow.wrapAt(4).front;~instractu.valueAction=4},
										{if(~windowactive == 'instrument 5',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 6';~listewindow.wrapAt(5).front",nil);~windowactive='instrument 6';~listewindow.wrapAt(5).front;~instractu.valueAction=5},
											{if(~windowactive == 'instrument 6',{~writepartitions.value(nil,'key','off',"~windowactive='partitions';~listewindow.wrapAt(6).front",nil);~windowactive='partitions';~listewindow.wrapAt(6).front;~commande=nil},
												{if(~windowactive=='partitions',{~writepartitions.value(nil,'key','off',"~windowactive='control panel';~listewindow.wrapAt(7).front",nil);~windowactive='control panel';~listewindow.wrapAt(7).front;~commande=nil},{~commande=nil})})})})})})})})});
					// Key "ctrlaltw" -> affichage windows <-
					if(modifiers==786432 and: {unicode==23} and: {keycode==13},{if(~windowactive == 'control panel',
						{~writepartitions.value(nil,'key','off',"~windowactive='partitions';~listewindow.wrapAt(6).front",nil);~windowactive='partitions';~wp.front;~commande=nil},
						{if(~windowactive == 'instrument 1',{~writepartitions.value(nil,'key','off',"~windowactive='control panel';~wg.front",nil);~windowactive='control panel';~wg.front},
							{if(~windowactive == 'instrument 2',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 1';~listewindow.wrapAt(0).front",nil);~windowactive='instrument 1';~listewindow.wrapAt(0).front;~instractu.valueAction=0},
								{if(~windowactive == 'instrument 3',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 2';~listewindow.wrapAt(1).front",nil);~windowactive='instrument 2';~listewindow.wrapAt(1).front;~instractu.valueAction=1},
									{if(~windowactive == 'instrument 4',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 3';~listewindow.wrapAt(2).front",nil);~windowactive='instrument 3';~listewindow.wrapAt(2).front;~instractu.valueAction=2},
										{if(~windowactive == 'instrument 5',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 4';~listewindow.wrapAt(3).front",nil);~windowactive='instrument 4';~listewindow.wrapAt(3).front;~instractu.valueAction=3},
											{if(~windowactive == 'instrument 6',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 5';~listewindow.wrapAt(4).front",nil);~windowactive='instrument 5';~listewindow.wrapAt(4).front;~instractu.valueAction=4;~commande=nil},
												{if(~windowactive=='partitions',{~writepartitions.value(nil,'key','off',"~windowactive='instrument 6';~listewindow.wrapAt(5).front",nil);~windowactive='instrument 6';~listewindow.wrapAt(5).front;~instractu.valueAction=5},{~commande=nil})})})})})})})})});
					// key esc or SpaceBar-> All System on/off
					if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}},{~writepartitions.value(nil,'key','off',"~wg.front",nil);~startsysteme.valueAction=(~startsysteme.value-1).abs;~wg.front;~commande=nil});
					// key y -> next instrumentwork
					if(modifiers==0 and: {unicode==121} and: {keycode==6},{~instractu.valueAction=~instractu.value+1;~commande=nil});
					// key ctrl y -> next instrumentwork
					if(modifiers==262144 and: {unicode==25} and: {keycode==6},{~instractu.valueAction=~instractu.value-1;~commande=nil});
					// Instruments
					// Key altd -> desynchronise all instruments
					if(modifiers==524288 and: {unicode==100} and: {keycode==2},{~writepartitions.value(nil,'key','off',"
~tempoSystem.schedAbs(~tempoSystem.beats, {
{~nombreinstrument.do({arg i;
if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
~routineinstrument.wrapAt(i).stop;
if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
});
~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
~routineinstrument.wrapAt(i).play(quant: Quant.new(1));
~routineMetronome.play(quant: Quant.new(1))});
})}.defer; nil})", nil);
					~tempoSystem.schedAbs(~tempoSystem.beats, {
						{~nombreinstrument.do({arg i;
							if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
								~routineinstrument.wrapAt(i).stop;
								// Set MIDI Off
								if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
									~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
										if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
									});
								});
								~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
								~routineinstrument.wrapAt(i).play(quant: Quant.new(1));
								~routineMetronome.play(quant: Quant.new(1))});
					})}.defer; nil});
					~commande=nil});
					// key d -> desynchronise instrument
					if(modifiers==0 and: {unicode==100} and: {keycode==2},{~commande='desynchronise'});
					// key p -> play l'instrument
					if(modifiers==0 and: {unicode==112} and: {keycode==35},{~commande='play'});
					// key alt+p -> play l'instrument synchro sur grille
					if(modifiers==524288 and: {unicode==112} and: {keycode==35},{~commande='play synchro'});
					// key shift+p -> play l'instrument sampler au bon pitch et duree par rapport tempogeneral et beats
					if(modifiers==131072 and: {unicode==80} and: {keycode==35},{~commande='play samplesynchro'});
					// key alt+shift+p -> play l'instrument sampler au bon tempo et duree par rapport au pitch low
					if(modifiers==655360 and: {unicode==80} and: {keycode==35},{~commande='play samplepitch'});
					// key z -> set l'instrument sampler au bon pitch et duree par rapport tempogeneral et beats
					if(modifiers==0 and: {unicode==122} and: {keycode==16}, {~commande='set samplesynchro'});
					// key alt z -> set l'instrument sampler au bon tempo et duree par rapport au pitch low
					if(modifiers==524288 and: {unicode==122} and: {keycode==16},{~commande='set samplepitch'});
					// key ctrlp -> stop l'instrument
					if(modifiers==262144 and: {unicode==16} and: {keycode==35},{~commande='stop'});
					// key k play partition
					if(modifiers==0 and: {unicode==107} and: {keycode==40},{~commande='play partition'});
					// key ctrlk stop play partition
					if(modifiers==262144 and: {unicode==11} and: {keycode==40},{~commande='stop play partition'});
					// key h rec partition
					if(modifiers==0 and: {unicode==104} and: {keycode==4},{~commande='rec partition'});
					// key ctrlh stop rec partition
					if(modifiers==262144 and: {unicode==8} and: {keycode==4},{~commande='stop rec partition'});
					// key w -> window instrument
					if(modifiers==0 and: {unicode==119} and: {keycode==13},{~commande='affiche'});
					// key l -> load instruments
					if(modifiers==0 and: {unicode==108} and: {keycode==37},{~commande='loadmulti'});
					// key L -> load instrument
					if(modifiers==131072 and: {unicode==76} and: {keycode==37},{~commande='loadsingle'});
					// key ctrl l -> load instruments synchro
					if(modifiers==262144 and: {unicode==12} and: {keycode==37},{~commande='loadmulti synchro'});
					// key ctrl L -> load instrument synchro
					if(modifiers==393216 and: {unicode==12} and: {keycode==37},{~commande='loadsingle synchro'});
					// key s -> save instruments
					if(modifiers==0 and: {unicode==115} and: {keycode==1},{~commande='savemulti'});
					// key S -> save instrument
					if(modifiers==131072 and: {unicode==83} and: {keycode==1},{~commande='savesingle'});
					// key alts -> save instruments
					if(modifiers==524288 and: {unicode==115} and: {keycode==1},{~saveloaddatasinstr.valueAction=4;~commande=nil});
					// key altS -> save instrument
					if(modifiers==655360 and: {unicode==83} and: {keycode==1},{~saveloaddatasinstr.valueAction=3;~commande=nil});
					// key v -> clear instruments
					if(modifiers==0 and: {unicode==118} and: {keycode==9},{~saveloaddatasinstr.valueAction=12;~commande=nil});
					// key V -> clear instrument
					if(modifiers==131072 and: {unicode==86} and: {keycode==9},{~commande='clear'});
					// alt v-> clear control panel
					if(modifiers==524288 and: {unicode==118} and: {keycode==9},{~saveloaddatasinstr.valueAction=13;~commande=nil});
					// alt V-> Reset FX after audio change
					if(modifiers==655360 and: {unicode==86} and: {keycode==9},{~saveloaddatasinstr.valueAction=12;
						s.bind{
							~fonctionResetFX.value;
							s.sync;
							this.initSynthDef;
							s.bind;
						};
						s.queryAllNodes;
						~commande=nil});
					// key altl -> load multi
					if(modifiers==524288 and: {unicode==108} and: {keycode==37},{~saveloaddatasinstr.valueAction=2;~commande=nil});
					// key altL -> load single
					if(modifiers==655360 and: {unicode==76} and: {keycode==37},{~saveloaddatasinstr.valueAction=1;~commande=nil});
					// key t -> copie instrument en cour
					if(modifiers==0 and: {unicode==116} and: {keycode==17},{~commande='copie'});
					// key i -> next F A D input
					if(modifiers==0 and: {unicode==105} and: {keycode==34},{~commande='fadinnext'});
					// key ctrli -> previous F A D input
					if(modifiers==262144 and: {unicode==9} and: {keycode==34},{~commande='fadinprevious'});
					// key alti -> F A D input off
					if(modifiers==524288 and: {unicode==105} and: {keycode==34},{~commande='fadinoff'});
					// key o -> next F A D output
					if(modifiers==0 and: {unicode==111} and: {keycode==31},{~commande='fadoutnext'});
					// key ctrlo -> previous F A D output
					if(modifiers==262144 and: {unicode==15} and: {keycode==31},{~commande='fadoutprevious'});
					// key alto -> F A D output off
					if(modifiers==524288 and: {unicode==111} and: {keycode==31},{~commande='fadoutoff'});
					// key b -> write buffer sound
					if(modifiers==0 and: {unicode==98} and: {keycode==11},{~commande='writebuffer'});
					// key altb -> write buffer sound
					if(modifiers==524288 and: {unicode==98} and: {keycode==11},{~writepartitions.value(nil,'key','off',"~listebuffer.wrapAt(~soncontrol.wrapAt(~instrumentactuel).value).write(~nompathdata++'son'.asString+(~soncontrol.wrapAt(~instrumentactuel).value+1).asString)",nil);
						~listebuffer.wrapAt(~soncontrol.wrapAt(~instrumentactuel).value).write(~nompathdata++"son"+(~soncontrol.wrapAt(~instrumentactuel).value+1).asString++".aiff");~commande=nil});
					// key q -> load sequences
					if(modifiers==0 and: {unicode==113} and: {keycode==12},{~commande='loadsequences'});
					// key Q -> load sequence
					if(modifiers==131072 and: {unicode==81} and: {keycode==12},{~commande='loadsequence'});
					// key ctrlq -> save sequences
					if(modifiers==262144 and: {unicode==17} and: {keycode==12},{~commande='savesequences'});
					// key ctrlQ -> save sequence
					if(modifiers==393216 and: {unicode==17} and: {keycode==12},{~commande='savesequence'});
					// key altq -> load sequences
					if(modifiers==524288 and: {unicode==113} and: {keycode==12},{~saveloaddatasinstrument.wrapAt(~instrumentactuel).valueAction = 6;~commande=nil});
					// key altQ -> load sequence
					if(modifiers==655360 and: {unicode==81} and: {keycode==12},{~saveloaddatasinstrument.wrapAt(~instrumentactuel).valueAction = 5;~commande=nil});
					// key altctrlq -> save sequences
					if(modifiers==786432 and: {unicode==17} and: {keycode==12},{~saveloaddatasinstrument.wrapAt(~instrumentactuel).valueAction = 8;~commande=nil});
					// key altctrlQ -> save sequence
					if(modifiers==917504 and: {unicode==17} and: {keycode==12},{~saveloaddatasinstrument.wrapAt(~instrumentactuel).valueAction = 7;~commande=nil});
					// key u -> load partition
					if(modifiers==0 and: {unicode==117} and: {keycode==32},{~commande='loadpartition'});
					// key ctrl u -> save partition
					if(modifiers==262144 and: {unicode==21} and: {keycode==32},{~commande='savepartition'});
					//key e Switch Algo Analyse
					if(modifiers==0 and: {unicode==101} and: {keycode==14}, {
						if(~algoAnalyse.value == 0, {~algoAnalyse.valueAction_(1)}, {
							if(~algoAnalyse.value == 1, {~algoAnalyse.valueAction_(2)}, {
								if(~algoAnalyse.value == 2, {~algoAnalyse.valueAction_(0)})})});
					});
					// key ctrl f -> Load and Add File for Analyze
					if(modifiers==262144 and: {unicode==6} and: {keycode==3}, {
						~writepartitions.value(nil,'key','off',"Dialog.openPanel({ arg paths;
~samplePourAnalyse=paths;
s.bind{
~synthPlayFile.set(\trig, 0);
s.sync;
~file = SoundFile.new;
s.sync;
~file.openRead(~samplePourAnalyse);
s.sync;
if(~file.numChannels == 1,
{~rawData= FloatArray.newClear(~file.numFrames * 2);
s.sync;
~file.readData(~rawData);
s.sync;
Post << 'Loading mono sound '.asString <<  ~samplePourAnalyse << Char.nl;
s.sync;
~rawData = Array.newFrom(~rawData).stutter(2) / 2;
s.sync;
~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2);
s.sync;
},
{Post << 'Loading stereo sound '.asString  <<  ~samplePourAnalyse << Char.nl;
s.sync;
~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1]);
s.sync;
});
//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
~file.close;
s.sync;
~synthPlayFile.set(\bufferplay, ~bufferanalysefile.bufnum);
s.sync;
~synthPlayFile.set(\trig, 1);
s.sync}},{'cancelled'.asString.postln})",nil);
						Dialog.openPanel({ arg paths;
							~samplePourAnalyse=paths;
							s.bind{
								~synthPlayFile.set(\trig, 0);
								s.sync;
								~synthPlayFile.run(false);
								s.sync;
								~file = SoundFile.new;
								s.sync;
								~file.openRead(~samplePourAnalyse);
								s.sync;
								if(~file.numChannels == 1,
									{~rawData= FloatArray.newClear(~file.numFrames * 2);
										s.sync;
										~file.readData(~rawData);
										s.sync;
										Post << "Loading mono sound" << " " << ~samplePourAnalyse << Char.nl;
										s.sync;
										~rawData = Array.newFrom(~rawData).stutter(2) / 2;
										s.sync;
										~bufferanalysefile=Buffer.loadCollection(s, ~rawData, 2);
										s.sync;
									},
									{Post << "Loading stereo sound" << " " << ~samplePourAnalyse << Char.nl;
										s.sync;
										~bufferanalysefile=Buffer.readChannel(s, ~samplePourAnalyse, channels: [0, 1]);
										s.sync;
								});
								//~bufferanalysefile = ~bufferanalysefile.normalize(1.0);
								~file.close;
								s.sync;
								~synthPlayFile.set(\bufferplay, ~bufferanalysefile.bufnum);
								s.sync;
								if(~startsysteme.value == 1 and: {~flagEntreeMode == 'File IN'}, {~synthPlayFile.run(true);
									s.sync;~synthPlayFile.set('trig', 1);s.sync});
							};
							~listeSamplePourAnalyse = ~listeSamplePourAnalyse.add(~bufferanalysefile);
							~listeNameSamplePourAnalyse = ~listeNameSamplePourAnalyse.add(paths.asString);
					},{"cancelled".postln})});
					// key f -> load audio file pour fileIn analyse
					if(modifiers==0 and: {unicode==102} and: {keycode==3},
						{
							~commande='Switch File for Analyze';
					});
					// key F
					if(modifiers==131072 and: {unicode==70} and: {keycode==3}, {
						~synthPlayFile.set(\loop, 1)});
					// key alt + F
					if(modifiers==655360 and: {unicode==70} and: {keycode==3}, {
						~synthPlayFile.set(\loop, 0)});
					// key g
					if(modifiers==0 and: {unicode==103} and: {keycode==5}, {~slcinstruments.value(~instrumentactuel, 21)});
					// key alt g
					if(modifiers==524288 and: {unicode==103} and: {keycode==5}, {~slcinstruments.value(~instrumentactuel, 19)});
					// key G
					if(modifiers==131072 and: {unicode==71} and: {keycode==5}, {~slcinstruments.value(~instrumentactuel, 20)});
					if(unicode==109 and: {keycode==46}, {~commande='freezeon'});
					// key M freeze buffers on
					if(modifiers==131072 and: {unicode==77} and: {keycode==46}, {
						~writepartitions.value(nil,'key','off',"~nombreinstrument.do({arg instr;
~flagBufferFreeze.wrapPut(instr, 'Freeze buffer on');
})",nil);
						~nombreinstrument.do({arg instr;
							~flagBufferFreeze.wrapPut(instr, 'Freeze buffer on');
					})});
					// key alt+M freeze buffers off
					if(modifiers==655360 and: {unicode==77} and: {keycode==46}, {
						~writepartitions.value(nil,'key','off',"~nombreinstrument.do({arg instr;
~flagBufferFreeze.wrapPut(instr, 'Freeze buffer off');
})",nil);
						~nombreinstrument.do({arg instr;
							~flagBufferFreeze.wrapPut(instr, 'Freeze buffer off');
					})});
					//key j New Synchro sur la grille temporelle
					if(modifiers==0 and: {unicode==106} and: {keycode==38}, {
						~writepartitions.value(nil,'key','off',"if(~startsysteme.value == 1, {
{~nombreinstrument.do({arg i;
if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
~tempoSystem.schedAbs(~tempoSystem.nextBar, {
~routineinstrument.wrapAt(i).stop;
if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
});
~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
~routineinstrument.wrapAt(i).play(quant: Quant.new(1));
~routineMetronome.play(quant: Quant.new(1)); nil});
})})}.defer;
})",nil);
						{~nombreinstrument.do({arg i;
							if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
								~tempoSystem.schedAbs(~tempoSystem.nextBar, {
									~routineinstrument.wrapAt(i).stop;
									// Set MIDI Off
									if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
										~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
											if(flagVST == 'on', {~fxVST.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
										});
									});
									~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
									~routineinstrument.wrapAt(i).play(quant: Quant.new(1));
									~routineMetronome.play(quant: Quant.new(1)); nil});
						})})}.defer;
					});
					// Key alt + r -> Start Recording
					if(modifiers==524288 and: {unicode==114} and: {keycode==15}, {
						if(~oscStateFlag == 'master', {~slaveAppAddr.sendMsg('/HPrec', "Rec On")});// Send Synchro Rec On
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
			}});
		};

		// Evaluation des commandes racourcis clavier
		~keysfonction={arg low,high, numinstr, f, d;
			//[low, high, numinstr].postln;
			// play synchro
			if(~commande=='play synchro',{if(high <= ~nombreinstrument, {
				~writepartitions.value(nil,'key','off',"~flagSynchro='on'",nil);
				for(low,high,{arg i;i=i-1;
					~flagSynchro='on';
					~startbutton.wrapAt(i).valueAction=0;~startbutton.wrapAt(i).valueAction=1});
				~writepartitions.value(nil,'key','off',"~flagSynchro='off'",nil);
				~flagSynchro='off'});
			});
			// play
			if(~commande=='play',{if(high <= ~nombreinstrument, {
				for(low,high,{arg i;i=i-1;
					~startbutton.wrapAt(i).valueAction=0;~startbutton.wrapAt(i).valueAction=1})});
			});
			// play sample au bon pitch et duree par rapport au tempogeneral
			if(~commande=='play samplesynchro',{if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var duree, tempo, pitch; i=i-1;
				duree = ~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).duration;
				tempo = 60 / (duree / ~nombreBeatsBare);pitch = ((~tempoprocessinstrument.value / tempo) * 60.midicps).cpsmidi;
				~synthfreqrange.wrapAt(i).value_([pitch, pitch]); ~synthfreqstep.wrapAt(i).value=0.0;
				~dureerange.wrapAt(i).value_([1, 1]); ~dureestep.wrapAt(i).valueAction_(~nombreBeatsBare); ~quantization.wrapPut(i, 1.0);
				~startbutton.wrapAt(i).valueAction=0;~startbutton.wrapAt(i).valueAction=1})});
			});
			// play sample au bon tempo et duree par rapport au pitch low
			if(~commande=='play samplepitch',{if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var duree, tempo, pitch, newTempo; i=i-1;
				duree = ~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).duration;
				tempo = 60 / (duree / ~nombreBeatsBare);
				pitch = ~synthfreqrange.wrapAt(i).lo;
				newTempo = (pitch.midicps / 60.midicps) * tempo;
				~tempoprocessinstrument.valueAction=newTempo;
				~synthfreqrange.wrapAt(i).value_([pitch, pitch]); ~synthfreqstep.wrapAt(i).value=0.0;
				~dureerange.wrapAt(i).value_([1, 1]); ~dureestep.wrapAt(i).valueAction_(~nombreBeatsBare); ~quantization.wrapPut(i, 1.0);
				~startbutton.wrapAt(i).valueAction=0;~startbutton.wrapAt(i).valueAction=1})});
			});
			// set sample au bon pitch et duree par rapport au tempogeneral
			if(~commande=='set samplesynchro',{if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var duree, tempo, pitch; i=i-1;
				duree = ~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).duration;
				tempo = 60 / (duree / ~nombreBeatsBare);pitch = ((~tempoprocessinstrument.value / tempo) * 60.midicps).cpsmidi;
				~synthfreqrange.wrapAt(i).value_([pitch, pitch]); ~synthfreqstep.wrapAt(i).value=0.0;
				~dureerange.wrapAt(i).value_([1, 1]); ~dureestep.wrapAt(i).valueAction_(~nombreBeatsBare); ~quantization.wrapPut(i, 1.0)})});
			});
			// set sample au bon tempo et duree par rapport au pitch low
			if(~commande=='set samplepitch',{if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var duree, tempo, pitch, newTempo; i=i-1;
				duree = ~listebuffer.wrapAt(~numerobuffer.wrapAt(i)).duration;
				tempo = 60 / (duree / ~nombreBeatsBare);
				pitch = ~synthfreqrange.wrapAt(i).lo;
				newTempo = (pitch.midicps / 60.midicps) * tempo;
				~tempoprocessinstrument.valueAction=newTempo;
				~synthfreqrange.wrapAt(i).value_([pitch, pitch]); ~synthfreqstep.wrapAt(i).value=0.0;
				~dureerange.wrapAt(i).value_([1, 1]); ~dureestep.wrapAt(i).valueAction_(~nombreBeatsBare); ~quantization.wrapPut(i, 1.0)})});
			});
			//stop
			if(~commande=='stop',{if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~startbutton.wrapAt(i).valueAction = 0})});
			});
			//copie
			if(~commande=='copie',{if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~writepartitions.value(nil,'key','off',"~fonctionloaddatasinstrument.value("++numinstr.value.asString++",~fonctionsavedatasinstrument.value(~instrumentactuel))",nil);
				~fonctionloaddatasinstrument.value(i,~fonctionsavedatasinstrument.value(~instrumentactuel),'on')})});
			});
			// window
			if(~commande == 'affiche' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~writepartitions.value(nil,'key','off',"~listewindow.wrapAt("++numinstr.value.asString++").front;~instractu.valueAction="++numinstr.value.asString,nil);
				~windowactive="instrument"+(i+1).asString;~listewindow.wrapAt(i).front;~instractu.valueAction=i})});
			});
			// clear instrument
			if(~commande == 'clear' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~saveloaddatasinstrument.wrapAt(i).valueAction=11})});
			});
			//desynchronise instrument
			if(~commande=='desynchronise',{if(high <= ~nombreinstrument, {
				~tempoSystem.schedAbs(~tempoSystem.beats, {
					{for(low,high,{arg i;i=i-1;
						if(~startsysteme.value==1 and: {~startbutton.wrapAt(i).value == 1}, {
							~routineinstrument.wrapAt(i).stop;
							// Set MIDI Off
							if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
								~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
									if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
								});
							});
							~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);~duree.wrapPut(i, 0);
							~routineinstrument.wrapAt(i).play(quant: Quant.new(1))},
						{~routineinstrument.wrapAt(i).stop;~duree.wrapPut(i, 0)})})}.defer; nil});
			});
			});
			// next F A D input
			if(~commande == 'fadinnext' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var index;
				i=i-1;
				index = ~choixdatasinfreqview.wrapAt(i).value;
				if(index >= 7, {index = -1});
				~choixdatasinfreqview.wrapAt(i).valueAction=index+1;
				index = ~choixdatasinampview.wrapAt(i).value;
				if(index >= 7, {index = -1});
				~choixdatasinampview.wrapAt(i).valueAction=index+1;
				index = ~choixdatasindureeview.wrapAt(i).value;
				if(index >= 7, {index = -1});
				~choixdatasindureeview.wrapAt(i).valueAction=index+1;
			})});
			});
			// previous F A D input
			if(~commande == 'fadinprevious' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var index;
				i=i-1;
				index = ~choixdatasinfreqview.wrapAt(i).value;
				if(index <= 0, {index = 8});
				~choixdatasinfreqview.wrapAt(i).valueAction=index-1;
				index = ~choixdatasinampview.wrapAt(i).value;
				if(index <= 0, {index = 8});
				~choixdatasinampview.wrapAt(i).valueAction=index-1;
				index = ~choixdatasindureeview.wrapAt(i).value;
				if(index <= 0, {index = 8});
				~choixdatasindureeview.wrapAt(i).valueAction=index-1;
			})});
			});
			// F A D input off
			if(~commande == 'fadinoff' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~choixdatasinfreqview.wrapAt(i).valueAction=0;~choixdatasinampview.wrapAt(i).valueAction=0;~choixdatasindureeview.wrapAt(i).valueAction=0})});
			});
			// next F A D output
			if(~commande == 'fadoutnext' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var index;
				i=i-1;
				index = ~choixalgoviewfreq.wrapAt(i).value;
				if(index >= 6, {index = -1});
				~choixalgoviewfreq.wrapAt(i).valueAction=index+1;
				index = ~choixalgoviewamp.wrapAt(i).value;
				if(index >= 6, {index = -1});
				~choixalgoviewamp.wrapAt(i).valueAction=index+1;
				index = ~choixalgoviewduree.wrapAt(i).value;
				if(index >= 6, {index = -1});
				~choixalgoviewduree.wrapAt(i).valueAction=index+1;
			})});
			});
			// previous F A D output
			if(~commande == 'fadoutprevious' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;
				var index;
				i=i-1;
				index = ~choixalgoviewfreq.wrapAt(i).value;
				if(index <= 0, {index = 7});
				~choixalgoviewfreq.wrapAt(i).valueAction=index-1;
				index = ~choixalgoviewamp.wrapAt(i).value;
				if(index <= 0, {index = 7});
				~choixalgoviewamp.wrapAt(i).valueAction=index-1;
				index = ~choixalgoviewduree.wrapAt(i).value;
				if(index <= 0, {index = 7});
				~choixalgoviewduree.wrapAt(i).valueAction=index-1;
			})});
			});
			// F A D output off
			if(~commande == 'fadoutoff' , {if(high <= ~nombreinstrument, {for(low,high,{arg i;i=i-1;
				~choixalgoviewfreq.wrapAt(i).valueAction=0;~choixalgoviewamp.wrapAt(i).valueAction=0;~choixalgoviewduree.wrapAt(i).valueAction=0})});
			});
			//load single
			if(~commande=='loadsingle',{if(high <= 40, {for(low,high,{arg i;
				~fonctionLoadInstrument.value(i)})});
			});
			//load multi
			if(~commande=='loadmulti',{if(high <= 40, {for(low,high,{arg i;
				~fonctionLoadInstruments.value(i)})});
			});
			//load single synchro
			if(~commande=='loadsingle synchro',{if(high <= 40, {
				~writepartitions.value(nil,'key','off',"~flagSynchro='on'",nil);
				for(low,high,{arg i;
					~flagSynchro='on';
					~fonctionLoadInstrument.value(i)});
				~writepartitions.value(nil,'key','off',"~flagSynchro='off'",nil);
				~flagSynchro='off'});
			});
			//load multi synchro
			if(~commande=='loadmulti synchro',{if(high <= 40, {
				~writepartitions.value(nil,'key','off',"~flagSynchro='on'",nil);
				for(low,high,{arg i;
					~flagSynchro='on';
					~fonctionLoadInstruments.value(i)});
				~writepartitions.value(nil,'key','off',"~flagSynchro='off'",nil);
				~flagSynchro='off'});
			});
			//write buffer sound
			if(~commande=='writebuffer',{if(high <= ~sounds.size, {
				for(low,high,{arg i;Dialog.savePanel({ arg path;
					~listebuffer.wrapAt(i - 1).write(path,sampleFormat:"int16")},{"cancelled".postln})})});
			});
			//load sequence
			if(~commande=='loadsequence',{if(high <= 40, {for(low,high,{arg i;
				~sequencesbutton.wrapAt(~instrumentactuel).valueAction = i})});
			});
			//load sequences
			if(~commande=='loadsequences',{if(high <= 40, {for(low,high,{arg i;
				~sequencesbutton.wrapAt(~instrumentactuel).valueAction = i+40})});
			});
			//save sequence
			if(~commande=='savesequence',{if(high <= 40, {
				for(low,high,{arg i;
					var datasfile=[], file;
					datasfile=datasfile.add(~listeaudiofreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listeaudioamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeaudioduree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listemidifreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listemidiamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listemididuree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listechaosfreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listechaosamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listechaosduree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listeoutneuronefreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listeoutneuroneamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoutneuroneduree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listegenetiquefreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listegenetiqueamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listegenetiqueduree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listealgo1freq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listealgo1amp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo1duree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listealgo2freq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listealgo2amp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo2duree.wrapAt(~instrumentactuel));
					datasfile=datasfile.add(~listeofffreq.wrapAt(~instrumentactuel)*127);datasfile=datasfile.add(~listeoffamp.wrapAt(~instrumentactuel).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoffduree.wrapAt(~instrumentactuel));
					file=File(~nompathdata++"sequence"+i.asString++".scd","w");file.write(datasfile.value.asCompileString);file.close})});
			});
			//save sequences
			if(~commande=='savesequences',{if(high <= 40, {for(low,high,{arg i;
				var datasfile=[], file;
				for(0, ~nombreinstrument-1, {arg ii;
					datasfile=datasfile.add(~listeaudiofreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeaudioamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeaudioduree.wrapAt(ii));
					datasfile=datasfile.add(~listemidifreq.wrapAt(ii)*127);datasfile=datasfile.add(~listemidiamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listemididuree.wrapAt(ii));
					datasfile=datasfile.add(~listechaosfreq.wrapAt(ii)*127);datasfile=datasfile.add(~listechaosamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listechaosduree.wrapAt(ii));
					datasfile=datasfile.add(~listeoutneuronefreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeoutneuroneamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoutneuroneduree.wrapAt(ii));
					datasfile=datasfile.add(~listegenetiquefreq.wrapAt(ii)*127);datasfile=datasfile.add(~listegenetiqueamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listegenetiqueduree.wrapAt(ii));
					datasfile=datasfile.add(~listealgo1freq.wrapAt(ii)*127);datasfile=datasfile.add(~listealgo1amp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo1duree.wrapAt(ii));
					datasfile=datasfile.add(~listealgo2freq.wrapAt(ii)*127);datasfile=datasfile.add(~listealgo2amp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listealgo2duree.wrapAt(ii));
					datasfile=datasfile.add(~listeofffreq.wrapAt(ii)*127);datasfile=datasfile.add(~listeoffamp.wrapAt(ii).replace(0, 0.000001).ampdb);datasfile=datasfile.add(~listeoffduree.wrapAt(ii));
				});
				file=File(~nompathdata++"sequences"+i.asString++".scd","w");file.write(datasfile.value.asCompileString);file.close})});
			});
			//save instrument
			if(~commande=='savesingle',{if(high <= 40, {for(low,high,{arg i;
				var datasfile=[], file;
				datasfile=datasfile.add(~fonctionsavedatasinstrument.value(~instrumentactuel));
				file=File(~nompathdata++"instrument"+i.asString++".scd","w");file.write(datasfile.value.asCompileString);file.close;
				~listewindow.wrapAt(~instrumentactuel).name="WekRobot by HP Instrument"+(~instrumentactuel+1).asString+~nompathdata+"instrument"+i.asString++".scd"})});
			});
			//save instruments
			if(~commande=='savemulti',{if(high <= 40, {for(low,high,{arg i;
				var datasfile=[], file;
				for(0, ~nombreinstrument-1, {arg ii;datasfile=datasfile.add(~fonctionsavedatasinstrument.value(ii));
					~listewindow.wrapAt(ii).name="WekRobot by HP Instrument"+(ii+1).asString+~nompathdata+"instruments"+i.asString++".scd"});
				datasfile=datasfile.add([~savecontrolpanel.value]);
				file=File(~nompathdata++"instruments"+i.asString++".scd","w");file.write(datasfile.value.asCompileString);file.close})});
			});
			// play partition
			if(~commande=='play partition',{if(high <= 40, {for(low,high,{arg i;i=i-1;
				~playpartbutton.wrapAt(i).valueAction=0;~playpartbutton.wrapAt(i).valueAction=1})});
			});
			// stop play partition
			if(~commande=='stop play partition',{if(high <= 40, {for(low,high,{arg i;i=i-1;
				~playpartbutton.wrapAt(i).valueAction=0})});
			});
			// rec partition
			if(~commande=='rec partition',{if(high <= 40, {for(low,high,{arg i;i=i-1;
				~recpartbutton.wrapAt(i).valueAction=0;~recpartbutton.wrapAt(i).valueAction=1})});
			});
			// stop rec partition
			if(~commande=='stop rec partition',{if(high <= 40, {for(low,high,{arg i;i=i-1;
				~recpartbutton.wrapAt(i).valueAction=0})});
			});
			// write partition
			if(~commande=='savepartition',{if(high <= 40, {for(low,high,{arg i;var file;i=i-1;
				file=File(~nompathdata++"score"+(i+1).asString++".scd","w");file.write(~listeplaypart.wrapAt(i).value.asCompileString);file.close})});
			});
			//load partition
			if(~commande=='loadpartition',{if(high <= 40, {for(low,high,{arg i;var file;i=i-1;
				if(File.exists(~nompathdata++"score"+i.asString++".scd"),
					{f=File(~nompathdata++"score"+(i+1).asString++".scd", "r");d=f.readAllString.interpret;f.close;
						~listeplaypart.wrapPut(i,d)},{~listeplaypart.wrapPut(i,[])})})});
			});
			//freeze buffer on
			if(~commande=='freezeon',{if(high <= 40, {for(low,high,{arg i;
				i=i-1;
				~flagBufferFreeze.wrapPut(i, 'Freeze buffer on')})});
			});
			//freeze buffer off
			if(~commande=='freezeoff',{if(high <= 40, {for(low,high,{arg i;
				i=i-1;
				~flagBufferFreeze.wrapPut(i, 'Freeze buffer off')})});
			});
			//Switch File for Analyze
			if(~commande=='Switch File for Analyze', {if(high <= 40, {for(low,high,{arg i;
				i=i-1;
				if(~listeSamplePourAnalyse.at(i) != nil, {
					s.bind{
						~synthPlayFile.set(\trig, 0);
						s.sync;
						~audioFile.set(\trigger, 0);
						s.sync;
						~synthPlayFile.set(\bufferplay, ~listeSamplePourAnalyse.at(i));
						~audioFile.set(\bufferplay, ~listeSamplePourAnalyse.at(i));
						~synthPlayFile.set(\trig, 1);
						s.sync;
						~audioFile.set(\trigger, 1);
						s.sync;
					};
				}, {"cancelled".postln});
			})});
			});
			~commande='nil';// evite problemes avec les views
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

		// Save datas instruments
		~fonctionsavedatasinstrument={arg i;var datas, x, y;
			~datassaveloadinstrument.wrapPut(i,[]);
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixdatasinfreqview.wrapAt(i).value.copy));//0
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixdatasinampview.wrapAt(i).value.copy));//1
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixdatasindureeview.wrapAt(i).value.copy));//2
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~soncontrol.wrapAt(i).value.copy));//3
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrol.wrapAt(i).value.copy));//4
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixalgoviewfreq.wrapAt(i).value.copy));//5
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~canalmidiinview.wrapAt(i).value.copy));//6
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~reversebutton.wrapAt(i).value.copy));//7
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~loopbutton.wrapAt(i).value.copy));//8
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~recsamplebutton.wrapAt(i).value.copy));//9
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~looprecsamplebutton.wrapAt(i).value.copy));//10
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolviewparametres.wrapAt(i).value.copy));//11
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolviewlevels.wrapAt(i).value.copy));//12
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~sampleroffsetcontrol.wrapAt(i).value.copy));//13
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpancontrol.wrapAt(i).value.copy));//14
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthfreqrange.wrapAt(i).value.copy));//15
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthfreqstep.wrapAt(i).value.copy));//16
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthamprange.wrapAt(i).value.copy));//17
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~sendFXPre.wrapAt(i).value.copy));//18
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~kchaosviewfreq.wrapAt(i).value.copy));//19
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~kchaosviewamp.wrapAt(i).value.copy));//20
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~kchaosviewduree.wrapAt(i).value.copy));//21
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~neuroneviewmode.wrapAt(i).value.copy));//22
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~neuroneviewapprentissage.wrapAt(i).value.copy));//23
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~neuroneviewtemperature.wrapAt(i).value.copy));//24
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~genetiqueviewmode.wrapAt(i).value.copy));//25
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~genetiqueviewcroisement.wrapAt(i).value.copy));//26
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~genetiqueviewmutation.wrapAt(i).value.copy));//27
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureerange.wrapAt(i).hi.copy));//28
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureerange.wrapAt(i).lo.copy));//29
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureestep.wrapAt(i).value.copy));//30
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~quantizationview.wrapAt(i).value.copy));//31
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthonoffeffetsPredatas.wrapAt(i).value.copy));//32
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpaneffetsPrestepdatas.wrapAt(i).value.copy));//33
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthampeffetsPrestepdatas.wrapAt(i).value.copy));//34
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolvieweffetsPredatas.wrapAt(i).copy));//35
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~syntheffetsPrecontrol.wrapAt(i).value.copy));//36
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthonoffeffetsPre.wrapAt(i).value.copy));//37
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpaneffetsPrestep.wrapAt(i).value.copy));//38
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthampeffetsPrestep.wrapAt(i).value.copy));//39
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthonoffeffetsPostdatas.wrapAt(i).value.copy));//40
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpaneffetsPoststepdatas.wrapAt(i).value.copy));//41
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthampeffetsPoststepdatas.wrapAt(i).value.copy));//42
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolvieweffetsPostdatas.wrapAt(i).copy));//43
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~syntheffetsPostcontrol.wrapAt(i).value.copy));//44
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthonoffeffetsPost.wrapAt(i).value.copy));//45
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpaneffetsPoststep.wrapAt(i).value.copy));//46
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthampeffetsPoststep.wrapAt(i).value.copy));//47
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthsliderenvelope.wrapAt(i).value.asFloat.copy));//48
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolviewparametresdatas.wrapAt(i).copy));//49
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~startbutton.wrapAt(i).value.copy));//50
			# x, y = ~instanceNeurones.wrapAt(i).save;
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(x));//51
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(y));//52
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~recsamplebuttondatas.value.copy));//53
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~looprecsamplebuttondatas.value.copy));//54
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthcontrolviewlevelsdatas.value.copy));//55
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~datassizeinstrument.wrapAt(i).value.copy));//56
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~differencefreqinstrument.wrapAt(i).value.copy));//57
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~differenceampinstrument.wrapAt(i).value.copy));//58
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~differencedureeinstrument.wrapAt(i).value.copy));//59
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureeaccordinstrument.wrapAt(i).value.copy));//60
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureeanalysemaxinstrument.wrapAt(i).value.copy));//61
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~maxaccordinstrument.wrapAt(i).value.copy));//62
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationinstrument.wrapAt(i).value.copy));//63
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~loopplaysamplerdatas.wrapAt(i).value.copy));//64
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~reversesampledatas.wrapAt(i).value.copy));//65
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationeffetpre.wrapAt(i).value.copy));//66
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationeffetpost.wrapAt(i).value.copy));//67
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationparametres.wrapAt(i).value.copy));//68
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~sonout.wrapAt(i).value.copy));//69
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~soncontrolfft.wrapAt(i).value.copy));//70
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~dureeanalysesilence.wrapAt(i).value.copy));//71
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixalgoviewamp.wrapAt(i).value.copy));//72
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~choixalgoviewduree.wrapAt(i).value.copy));//73
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~flagBufferFreeze.wrapAt(i).copy));//74
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueSynth.wrapAt(i).copy));//75
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueParametreSynth.wrapAt(i).copy));//76
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueEffetPre.wrapAt(i).copy));//77
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueEffetPost.wrapAt(i).copy));//78
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~levelenvsynth.wrapAt(i).copy));//79
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~timeenvsynth.wrapAt(i).copy));//80
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~numeroSynth.wrapAt(i).copy));//81
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~synthpancontrolValue.wrapAt(i).copy));//82
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~sampleroffsetcontrolValue.wrapAt(i).copy));//83
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~paraAlgoAnalyseAudio.copy));//84
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationeffetpanpre.wrapAt(i).value.copy));//85
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~automationeffetpanpost.wrapAt(i).value.copy));//86
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueEffetPanPre.wrapAt(i).copy));//87
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~randomValueEffetPanPost.wrapAt(i).copy));//88
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~quantization.wrapAt(i).copy));//89
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~canalMidiOutInstr.wrapAt(i).copy + 1));//90
			// Band
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~numFhzBand.wrapAt(i).copy));//91
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~bandFHZ.wrapAt(i).copy));//92
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~flagSynthBand.wrapAt(i).copy));//93
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~flagBandSynth.wrapAt(i).copy));//94
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~rangeSynthBand.wrapAt(i).copy));//95
			// Tuning
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~tuning.wrapAt(i).copy));//96
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~scale.wrapAt(i).copy));//97
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~root.wrapAt(i).copy));//98
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~degrees.wrapAt(i).copy));//99
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~tuningIndex.wrapAt(i).copy));//100
			//AudioRecIn
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~canalAudioInInstr.wrapAt(i).copy));//101
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~sendFXPost.wrapAt(i).value.copy));//102
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(~byPassSynth.wrapAt(i).value.copy));//103
			//Musique 104
			datas=[];
			datas=datas.add(~listeaudiofreq.wrapAt(i)*127.copy);datas=datas.add(~listeaudioamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listeaudioduree.wrapAt(i).copy);
			datas=datas.add(~listemidifreq.wrapAt(i)*127.copy);datas=datas.add(~listemidiamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listemididuree.wrapAt(i).copy);datas=datas.add(~listechaosfreq.wrapAt(i)*127.copy);datas=datas.add(~listechaosamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listechaosduree.wrapAt(i).copy);
			datas=datas.add(~listeoutneuronefreq.wrapAt(i)*127.copy);datas=datas.add(~listeoutneuroneamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listeoutneuroneduree.wrapAt(i).copy);
			datas=datas.add(~listegenetiquefreq.wrapAt(i)*127.copy);datas=datas.add(~listegenetiqueamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listegenetiqueduree.wrapAt(i).copy);
			datas=datas.add(~listealgo1freq.wrapAt(i)*127.copy);datas=datas.add(~listealgo1amp.wrapAt(i).replace(0, 0.000001).ampdb);datas=datas.add(~listealgo1duree.wrapAt(i).copy);datas=datas.add(~listealgo2freq.wrapAt(i)*127.copy);datas=datas.add(~listealgo2amp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listealgo2duree.wrapAt(i).copy);
			datas=datas.add(~listeofffreq.wrapAt(i)*127.copy);datas=datas.add(~listeoffamp.wrapAt(i).replace(0, 0.000001).ampdb.copy);datas=datas.add(~listeoffduree.wrapAt(i).copy);
			~datassaveloadinstrument.wrapPut(i,~datassaveloadinstrument.wrapAt(i).add(datas.value));//104
			// sortie fonction
			~datassaveloadinstrument.wrapAt(i).value;
		};

		// Load datas instruments
		~fonctionloaddatasinstrument={arg i, datas, flag;
			var freq, level, tampon;
			~choixdatasinfreqview.wrapAt(i).valueAction=datas.wrapAt(0);~indatasfreqinstrument.wrapPut(i,~choixdatasinfreq.wrapAt(datas.wrapAt(0).value));
			~choixdatasinampview.wrapAt(i).valueAction=datas.wrapAt(1);~indatasampinstrument.wrapPut(i,~choixdatasinamp.wrapAt(datas.wrapAt(1).value));
			~choixdatasindureeview.wrapAt(i).valueAction=datas.wrapAt(2);~indatasdureeinstrument.wrapPut(i,~choixdatasinduree.wrapAt(datas.wrapAt(2).value));
			~soncontrol.wrapAt(i).valueAction_(datas.wrapAt(3).value);~numerobuffer.wrapPut(i,datas.wrapAt(3).value);
			~soncontrolfft.wrapAt(i).valueAction=datas.wrapAt(70).value;~numerobufferAdd.wrapPut(i,datas.wrapAt(70).value);
			~synthcontrol.wrapAt(i).valueAction=datas.wrapAt(4).value;~listesynth.wrapPut(i,~listSynth.wrapAt(datas.wrapAt(4).value));
			~choixalgoviewfreq.wrapAt(i).valueAction=datas.wrapAt(5).value;~algoinstrumentfreq.wrapPut(i,~algorithmesmusicauxfreq.wrapAt(datas.wrapAt(5).value));
			~choixalgoviewamp.wrapAt(i).valueAction=datas.wrapAt(72).value;~algoinstrumentamp.wrapPut(i,~algorithmesmusicauxamp.wrapAt(datas.wrapAt(72).value));
			~choixalgoviewduree.wrapAt(i).valueAction=datas.wrapAt(73).value;~algoinstrumentduree.wrapPut(i,~algorithmesmusicauxduree.wrapAt(datas.wrapAt(73).value));
			~canalmidiinview.wrapAt(i).valueAction=datas.wrapAt(6).value;~canalmidiin.wrapPut(i,datas.wrapAt(6).value);
			~reversebutton.wrapAt(i).valueAction=datas.wrapAt(7).value;
			~loopbutton.wrapAt(i).valueAction=datas.wrapAt(8).value;
			if(flag != 'off' ,{~recsamplebutton.wrapAt(i).valueAction_(datas.wrapAt(9).value);
				~looprecsamplebutton.wrapAt(i).valueAction_(datas.wrapAt(10).value);
				~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\run, ~recsamplebutton.wrapAt(i).value,\loop, ~looprecsamplebutton.wrapAt(i).value);
				if(~reversebutton.wrapAt(i).value == 0, {~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),1));~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~reversesampledatas.wrapPut(i,~reversesampledatas.wrapAt(i).wrapPut(~numerobuffer.wrapAt(i),1.neg));~busoffsetplaysampler.wrapAt(i).set(1 - ~sampleroffsetcontrol.wrapAt(i).value)});
				~nombreinstrument.do({arg instr;
					if(instr != i, {
						if(~numerobuffer.wrapAt(instr) == ~numerobuffer.wrapAt(i),{level=~synthcontrolviewlevels.wrapAt(i).value;~listesamplein.wrapAt(~numerobuffer.wrapAt(instr)).set(\run, ~recsamplebutton.wrapAt(i).value,\loop, ~looprecsamplebutton.wrapAt(i).value);
							~busreclevel.wrapAt(~numerobuffer.wrapAt(instr)).set(level.wrapAt(0).value, level.wrapAt(1).value);
							~recsamplebutton.wrapAt(instr).valueAction=~recsamplebutton.wrapAt(i).value;
							~looprecsamplebutton.wrapAt(instr).valueAction=~looprecsamplebutton.wrapAt(i).value})});
				});
			});
			~synthcontrolviewparametres.wrapAt(i).valueAction_(datas.wrapAt(11).value);
			~buscontrolsynth.wrapAt(i).set(datas.wrapAt(11).wrapAt(0).value, datas.wrapAt(11).wrapAt(1).value, datas.wrapAt(11).wrapAt(2).value, datas.wrapAt(11).wrapAt(3).value, datas.wrapAt(11).wrapAt(4).value, datas.wrapAt(11).wrapAt(5).value, datas.wrapAt(11).wrapAt(6).value, datas.wrapAt(11).wrapAt(7).value, datas.wrapAt(11).wrapAt(8).value, datas.wrapAt(11).wrapAt(9).value);
			~wavetable.wrapAt(i).sine2([1,2,3,4,5,6,7,8,9,10],~synthcontrolviewparametres.wrapAt(i).value);
			tampon=datas.wrapAt(49);while({tampon.size < ~listSynth.size} , {tampon=tampon.add([0,0,0,0,0,0,0,0,0,0])});
			~synthcontrolviewparametresdatas.wrapPut(i,tampon);
			~synthcontrolviewlevels.wrapAt(i).valueAction_(datas.wrapAt(12).value);~busreclevel.wrapAt(~numerobuffer.wrapAt(i)).set(datas.wrapAt(12).wrapAt(0).value, datas.wrapAt(12).wrapAt(1).value);
			~sampleroffsetcontrol.wrapAt(i).value=datas.wrapAt(13).value; if(~reversebutton.wrapAt(i).value == 0, {~busoffsetplaysampler.wrapAt(i).set(~sampleroffsetcontrol.wrapAt(i).value)},{~busoffsetplaysampler.wrapAt(i).set(1-~sampleroffsetcontrol.wrapAt(i).value)});
			~synthpancontrol.wrapAt(i).value_(datas.wrapAt(14).value);
			~buspansynthLo.wrapAt(i).set(datas.wrapAt(14).wrapAt(0));
			~buspansynthHi.wrapAt(i).set(datas.wrapAt(14).wrapAt(1));
			~synthfreqrange.wrapAt(i).value_(datas.wrapAt(15).value);
			~synthfreqstep.wrapAt(i).value=datas.wrapAt(16).value;
			freq=~freq.wrapAt(i).cpsmidi / 127;freq=(freq*(~synthfreqrange.wrapAt(i).hi - ~synthfreqrange.wrapAt(i).lo)  + ~synthfreqrange.wrapAt(i).lo +~synthfreqstep.wrapAt(i).value).midicps;
			freq.size.do({arg ii;~busfreqsynth.wrapAt(i).wrapAt(ii).set(freq.wrapAt(ii))});
			~synthamprange.wrapAt(i).value_(datas.wrapAt(17).value);
			~sendFXPre.wrapAt(i).valueAction_(datas.wrapAt(18).value);
			/*~busampsynth.wrapAt(i).set(~amp.wrapAt(i).value*(~synthamprange.wrapAt(i).hi.dbamp - ~synthamprange.wrapAt(i).lo.dbamp) + ~synthamprange.wrapAt(i).lo.dbamp);*/
			~kchaosviewfreq.wrapAt(i).value=datas.wrapAt(19).value;~instanceChaosF.wrapAt(i).init(datas.wrapAt(19).value);
			~kchaosviewamp.wrapAt(i).value=datas.wrapAt(20).value;~instanceChaosA.wrapAt(i).init(datas.wrapAt(20).value);
			~kchaosviewduree.wrapAt(i).value=datas.wrapAt(21).value;~instanceChaosD.wrapAt(i).init(datas.wrapAt(21).value);
			~neuroneviewmode.wrapAt(i).value=datas.wrapAt(22).value;if (datas.wrapAt(22).value == 1, {~neuronemode.wrapPut(i,'rec')},{~neuronemode.wrapPut(i,'off')});
			~neuroneviewapprentissage.wrapAt(i).value=datas.wrapAt(23).value;~neuroneapprentissage.wrapPut(i,datas.wrapAt(23).value);
			~neuroneviewtemperature.wrapAt(i).value=datas.wrapAt(24).value;~neuronetemperature.wrapPut(i,datas.wrapAt(24).value);
			~genetiqueviewmode.wrapAt(i).value=datas.wrapAt(25).value;if (datas.wrapAt(25).value == 1, {~genetiquemode.wrapPut(i,'rec')},{~genetiquemode.wrapPut(i,'off')});
			~genetiqueviewcroisement.wrapAt(i).value=datas.wrapAt(26).value;~genetiquecroisement.wrapPut(i,datas.wrapAt(26).value);
			~genetiqueviewmutation.wrapAt(i).value=datas.wrapAt(27).value;~genetiquemutation.wrapPut(i,datas.wrapAt(27).value);
			~dureerange.wrapAt(i).hi_(datas.wrapAt(28).value);
			~dureerange.wrapAt(i).lo_(datas.wrapAt(29).value);
			~dureestep.wrapAt(i).valueAction_(datas.wrapAt(30).value);
			~quantizationview.wrapAt(i).value=datas.wrapAt(31).value;~quantization.wrapPut(i, datas.wrapAt(31).value);
			tampon=datas.wrapAt(32);while({tampon.size < ~fxPre.size} , {tampon=tampon.add(0)});
			~synthonoffeffetsPredatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(33);while({tampon.size < ~fxPre.size} , {tampon=tampon.add(0)});
			~synthpaneffetsPrestepdatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(34);while({tampon.size < ~fxPre.size} , {tampon=tampon.add(-12)});
			~synthampeffetsPrestepdatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(35);
			while({tampon.size < ~fxPre.size} , {tampon=tampon.add([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5])});
			~synthcontrolvieweffetsPredatas.wrapPut(i,tampon);

			//~synthcontrolvieweffetsPre.wrapAt(i).value_(tampon.value);

			~syntheffetsPrecontrol.wrapAt(i).value_(datas.wrapAt(36).value);
			~listenodeeffetsPresynth.wrapPut(i,datas.wrapAt(36).value);
			~synthonoffeffetsPre.wrapAt(i).valueAction=datas.wrapAt(37).value;
			~synthpaneffetsPrestep.wrapAt(i).value=datas.wrapAt(38).value;
			~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\pan, datas.wrapAt(38).value);
			~synthampeffetsPrestep.wrapAt(i).value=datas.wrapAt(39).value;~listeeffetsPresynth.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value).set(\amp, datas.wrapAt(39).value.dbamp);
			~synthcontrolvieweffetsPre.wrapAt(i).value=~synthcontrolvieweffetsPredatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);~synthonoffeffetsPre.wrapAt(i).valueAction=~synthonoffeffetsPredatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
			~synthpaneffetsPrestep.wrapAt(i).value=~synthpaneffetsPrestepdatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
			~synthampeffetsPrestep.wrapAt(i).value=~synthampeffetsPrestepdatas.wrapAt(i).wrapAt(~listenodeeffetsPresynth.wrapAt(i).value);
			~fxPre.size.do({arg effetsPre;
				if(~synthonoffeffetsPredatas.wrapAt(i).wrapAt(effetsPre).value==1,{~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).run(true)},{~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).run(false)});
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control1,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(0).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control2,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(1).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control3,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(2).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control4,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(3).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control5,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(4).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control6,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(5).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control7,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(6).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\control8,datas.wrapAt(35).wrapAt(effetsPre).wrapAt(7).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\pan, datas.wrapAt(33).wrapAt(effetsPre).value);
				~listeeffetsPresynth.wrapAt(i).wrapAt(effetsPre).set(\amp, datas.wrapAt(34).wrapAt(effetsPre).value.dbamp);
			});
			tampon=datas.wrapAt(40);while({tampon.size < ~fxPost.size} , {tampon=tampon.add(0)});
			~synthonoffeffetsPostdatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(41);while({tampon.size < ~fxPost.size} , {tampon=tampon.add(0)});
			~synthpaneffetsPoststepdatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(42);while({tampon.size < ~fxPost.size} , {tampon=tampon.add(-12)});
			~synthampeffetsPoststepdatas.wrapPut(i,tampon.value);
			tampon=datas.wrapAt(43);while({tampon.size < ~fxPost.size} , {tampon=tampon.add([0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5])});
			~synthcontrolvieweffetsPostdatas.wrapPut(i,tampon);
			~syntheffetsPostcontrol.wrapAt(i).value_(datas.wrapAt(44).value);~listenodeeffetsPostsynth.wrapPut(i,datas.wrapAt(44).value);
			~synthonoffeffetsPost.wrapAt(i).valueAction=datas.wrapAt(45).value;
			~synthpaneffetsPoststep.wrapAt(i).value=datas.wrapAt(46).value;~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\pan, datas.wrapAt(46).value);
			~synthampeffetsPoststep.wrapAt(i).value=datas.wrapAt(47).value;~listeeffetsPostsynth.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value).set(\amp, datas.wrapAt(47).value.dbamp);
			~synthcontrolvieweffetsPost.wrapAt(i).value=~synthcontrolvieweffetsPostdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);~synthonoffeffetsPost.wrapAt(i).valueAction=~synthonoffeffetsPostdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
			~synthpaneffetsPoststep.wrapAt(i).value=~synthpaneffetsPoststepdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
			~synthampeffetsPoststep.wrapAt(i).value=~synthampeffetsPoststepdatas.wrapAt(i).wrapAt(~listenodeeffetsPostsynth.wrapAt(i).value);
			~fxPost.size.do({arg effetsPost;
				if(~synthonoffeffetsPostdatas.wrapAt(i).wrapAt(effetsPost).value==1,{~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).run(true)},{~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).run(false)});
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control1,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(0).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control2,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(1).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control3,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(2).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control4,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(3).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control5,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(4).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control6,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(5).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control7,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(6).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\control8,datas.wrapAt(43).wrapAt(effetsPost).wrapAt(7).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\pan, datas.wrapAt(41).wrapAt(effetsPost).value);
				~listeeffetsPostsynth.wrapAt(i).wrapAt(effetsPost).set(\amp, datas.wrapAt(42).wrapAt(effetsPost).value.dbamp);
			});
			~synthsliderenvelope.wrapAt(i).value=datas.wrapAt(48).value.asFloat;
			~levelenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, datas.wrapAt(48).wrapAt(1).value.asFloat);~timeenvsynth.wrapAt(i).wrapPut(~synthcontrol.wrapAt(i).value, datas.wrapAt(48).wrapAt(0).value.asFloat);
			~instanceNeurones.wrapAt(i).load(datas.wrapAt(51).value, datas.wrapAt(52).value);
			if(flag != 'off' ,{tampon=datas.wrapAt(53);while({tampon.size < ~sounds.size} , {tampon=tampon.add(0)});
				~recsamplebuttondatas=tampon;
				tampon=datas.wrapAt(54);while({tampon.size < ~sounds.size} , {tampon=tampon.add(0)});
				~looprecsamplebuttondatas=tampon;
				tampon=datas.wrapAt(55);while({tampon.size < ~sounds.size} , {tampon=tampon.add([1,0])});
				~synthcontrolviewlevelsdatas=tampon;
			});
			~datassizeinstrument.wrapAt(i).value=datas.wrapAt(56).value;
			~listedatassize.wrapPut(i,datas.wrapAt(56).value);
			~differencefreqinstrument.wrapAt(i).value=datas.wrapAt(57).value;
			~differencefreq.wrapPut(i,datas.wrapAt(57).value);
			~differenceampinstrument.wrapAt(i).value=datas.wrapAt(58).value;
			~differenceamp.wrapPut(i,datas.wrapAt(58).value);
			~differencedureeinstrument.wrapAt(i).value=datas.wrapAt(59).value;
			~differenceduree.wrapPut(i,datas.wrapAt(59).value);
			~dureeaccordinstrument.wrapAt(i).value=datas.wrapAt(60).value;
			~dureeaccord.wrapPut(i,datas.wrapAt(60).value);
			~dureeanalysemaxinstrument.wrapAt(i).value=datas.wrapAt(61).value;
			~dureeanalysemax.wrapPut(i,datas.wrapAt(61).value);
			~maxaccordinstrument.wrapAt(i).value=datas.wrapAt(62).value;
			~maxaccord.wrapPut(i,datas.wrapAt(62).value);
			~automationinstrument.wrapAt(i).valueAction=datas.wrapAt(63).value;
			tampon=datas.wrapAt(64);while({tampon.size < ~sounds.size} , {tampon=tampon.add(0)});
			~loopplaysamplerdatas.wrapPut(i,tampon);
			tampon=datas.wrapAt(65);while({tampon.size < ~sounds.size} , {tampon=tampon.add(1)});
			~reversesampledatas.wrapPut(i,tampon);
			~automationeffetpre.wrapAt(i).valueAction=datas.wrapAt(66).value;
			~automationeffetpost.wrapAt(i).valueAction=datas.wrapAt(67).value;
			~automationparametres.wrapAt(i).valueAction=datas.wrapAt(68).value;
			~sonout.wrapAt(i).valueAction=datas.wrapAt(69).value;~canalaudioout.wrapPut(i,datas.wrapAt(69).value);
			~dureeanalysesilence.wrapAt(i).value=datas.wrapAt(71).value;
			~dureeanalysesil.wrapPut(i,datas.wrapAt(71).value);
			~sounds.size.do({arg son;
				~listesamplein.wrapAt(son).set(\run, ~recsamplebuttondatas.wrapAt(son).value, \trigger,   ~recsamplebuttondatas.wrapAt(son).value,\loop, ~looprecsamplebuttondatas.wrapAt(son).value);
				~busreclevel.wrapAt(son).set(~synthcontrolviewlevelsdatas.wrapAt(son).wrapAt(0).value, ~synthcontrolviewlevelsdatas.wrapAt(son).wrapAt(1).value)});
			~flagBufferFreeze.wrapPut(i, datas.wrapAt(74));
			~randomValueSynth.wrapPut(i,datas.wrapAt(75).value);
			~randomValueParametreSynth.wrapPut(i,datas.wrapAt(76).value);
			~randomValueEffetPre.wrapPut(i,datas.wrapAt(77).value);
			~randomValueEffetPost.wrapPut(i,datas.wrapAt(78).value);
			~levelenvsynth.wrapPut(i,datas.wrapAt(79).value);
			~timeenvsynth.wrapPut(i,datas.wrapAt(80).value);
			~numeroSynth.wrapPut(i,datas.wrapAt(81).value);
			~synthpancontrolValue.wrapPut(i, datas.wrapAt(82).value);
			~sampleroffsetcontrolValue.wrapPut(i, datas.wrapAt(83).value);
			~paraAlgoAnalyseAudio=datas.wrapAt(84).value;
			~automationeffetpanpre.wrapAt(i).valueAction=datas.wrapAt(85).value;
			~automationeffetpanpost.wrapAt(i).valueAction=datas.wrapAt(86).value;
			~randomValueEffetPanPre.wrapPut(i,datas.wrapAt(87).value);
			~randomValueEffetPanPost.wrapPut(i,datas.wrapAt(88).value);
			~quantization.wrapPut(i,datas.wrapAt(89).value);
			~canalMidiOutInstr.wrapPut(i,datas.wrapAt(90).value - 1);
			~midibutton.wrapAt(i).value = (datas.wrapAt(90).value);
			// Band
			~numFhzBand.wrapPut(i,datas.wrapAt(91).value);
			~numberBand.wrapAt(i).valueAction_(datas.wrapAt(91).value);
			//~bandFHZ.wrapPut(i,datas.wrapAt(92).value);
			~flagSynthBand.wrapPut(i,datas.wrapAt(93).value);
			~flagBandSynth.wrapPut(i,datas.wrapAt(94).value);
			~rangeSynthBand.wrapPut(i,datas.wrapAt(95).value);
			if(datas.wrapAt(93).value == 'on', {~buttonSynthBand.wrapAt(i).valueAction_(1)},
				{~buttonSynthBand.wrapAt(i).valueAction_(0)});
			for(0, ~numFhzBand.at(i), {arg index;
				if(index == 0, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand0.at(i).value = 0},
					{~synthBand0.at(i).value = 1})});
				if(index == 1, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand1.at(i).value = 0},
					{~synthBand1.at(i).value = 1})});
				if(index == 2, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand2.at(i).value = 0},
					{~synthBand2.at(i).value = 1})});
				if(index == 3, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand3.at(i).value = 0},
					{~synthBand3.at(i).value = 1})});
				if(index == 4, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand4.at(i).value = 0},
					{~synthBand4.at(i).value = 1})});
				if(index == 5, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand5.at(i).value = 0},
					{~synthBand5.at(i).value = 1})});
				if(index == 6, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand6.at(i).value = 0},
					{~synthBand6.at(i).value = 1})});
				if(index == 7, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand7.at(i).value = 0},
					{~synthBand7.at(i).value = 1})});
				if(index == 8, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand8.at(i).value = 0},
					{~synthBand8.at(i).value = 1})});
				if(index == 9, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand9.at(i).value = 0},
					{~synthBand9.at(i).value = 1})});
				if(index == 10, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand10.at(i).value = 0},
					{~synthBand10.at(i).value = 1})});
				if(index == 11, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand11.at(i).value = 0},
					{~synthBand11.at(i).value = 1})});
				if(index == 12, {if(~flagBandSynth.at(i).at(index) == 0, {~synthBand12.at(i).value = 0},
					{~synthBand12.at(i).value = 1})});
			});
			~bandFHZ.wrapPut(i,datas.wrapAt(92).value);
			~rangeBand.wrapAt(i).valueAction_(datas.wrapAt(92).value);
			~lastTimeBand.put(i, [Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime, Main.elapsedTime]);// 12 band total
			~listeaudiofreq.wrapPut(i,[]);~listeaudioamp.wrapPut(i,[]);~listeaudioduree.wrapPut(i,[]);~freqtampon.wrapPut(i,nil);~amptampon.wrapPut(i,nil);~freqbefore.wrapPut(i,0);~ampbefore.wrapPut(i,0);~dureebefore.wrapPut(i,0);~lastTimeAudio = Main.elapsedTime;~lastDureeInstrAudio.wrapPut(i, Main.elapsedTime);
			// Tuning
			~tuning.wrapPut(i,datas.wrapAt(96).value);
			~scale.wrapPut(i,datas.wrapAt(97).value);
			~root.wrapPut(i,datas.wrapAt(98).value);
			~degrees.wrapPut(i,datas.wrapAt(99).value);
			~tuningIndex.wrapPut(i,datas.wrapAt(100).value);
			~rootChoice.wrapAt(i).valueAction_(datas.wrapAt(98).value);
			~listTuning.wrapAt(i).valueAction = datas.wrapAt(100).value;
			~displayDegrees.wrapAt(i).valueAction = datas.wrapAt(99).value;
			//Audio In Rec
			~canalAudioInInstr.put(i, datas.wrapAt(101).value);
			~setAudioInstr.wrapAt(i).valueAction = datas.wrapAt(101).value;
			~sendFXPost.wrapAt(i).valueAction_(datas.wrapAt(102).value);
			~byPassSynth.wrapAt(i).valueAction_(datas.wrapAt(103).value);
			// datas musique 104
			~listeaudiofreq.wrapPut(i,datas.wrapAt(104).wrapAt(0)/127);~listeaudioamp.wrapPut(i,datas.wrapAt(104).wrapAt(1).dbamp);~listeaudioduree.wrapPut(i,datas.wrapAt(104).wrapAt(2));
			~listemidifreq.wrapPut(i,datas.wrapAt(104).wrapAt(3)/127);~listemidiamp.wrapPut(i,datas.wrapAt(104).wrapAt(4).dbamp);~listemididuree.wrapPut(i,datas.wrapAt(104).wrapAt(5));
			~listechaosfreq.wrapPut(i,datas.wrapAt(104).wrapAt(6)/127);~listechaosamp.wrapPut(i,datas.wrapAt(104).wrapAt(7).dbamp);~listechaosduree.wrapPut(i,datas.wrapAt(104).wrapAt(8));
			~listeoutneuronefreq.wrapPut(i,datas.wrapAt(104).wrapAt(9)/127);~listeoutneuroneamp.wrapPut(i,datas.wrapAt(104).wrapAt(10).dbamp);~listeoutneuroneduree.wrapPut(i,datas.wrapAt(104).wrapAt(11));
			~listegenetiquefreq.wrapPut(i,datas.wrapAt(104).wrapAt(12)/127);~listegenetiqueamp.wrapPut(i,datas.wrapAt(104).wrapAt(13).dbamp);~listegenetiqueduree.wrapPut(i,datas.wrapAt(104).wrapAt(14));
			~listealgo1freq.wrapPut(i,datas.wrapAt(104).wrapAt(15)/127);~listealgo1amp.wrapPut(i,datas.wrapAt(104).wrapAt(16).dbamp);~listealgo1duree.wrapPut(i,datas.wrapAt(104).wrapAt(17));
			~listealgo2freq.wrapPut(i,datas.wrapAt(104).wrapAt(18)/127);~listealgo2amp.wrapPut(i,datas.wrapAt(104).wrapAt(19).dbamp);~listealgo2duree.wrapPut(i,datas.wrapAt(104).wrapAt(20));
			if(datas.wrapAt(104).wrapAt(21) != nil, {~listeofffreq.wrapPut(i,datas.wrapAt(104).wrapAt(21)/127);~listeoffamp.wrapPut(i,datas.wrapAt(104).wrapAt(22).dbamp);~listeoffduree.wrapPut(i,datas.wrapAt(104).wrapAt(23))},
				{~listeofffreq.wrapPut(i,[]);~listeoffamp.wrapPut(i,[]);~listeoffduree.wrapPut(i,[])});
			//
			~startbutton.wrapAt(i).valueAction=datas.wrapAt(50).value;if(datas.wrapAt(50).value == 1 and: {~startsysteme.value == 1}, {~flagfreq.wrapPut(i,0);~flagamp.wrapPut(i,0);~flagduree.wrapPut(i,0);~flagneuronefreq.wrapPut(i,0);~flagneuroneamp.wrapPut(i,0);~flagneuroneduree.wrapPut(i,0);~flagoutneuronefreq.wrapPut(i,0);~flagoutneuroneamp.wrapPut(i,0);~flagoutneuroneduree.wrapPut(i,0);~flaggenetiquefreq.wrapPut(i,0);~flaggenetiqueamp.wrapPut(i,0);~flaggenetiqueduree.wrapPut(i,0);
			},{~routineinstrument.wrapAt(i).stop;~duree.wrapPut(i, 0);
				// Set MIDI Off
				if(~flagMidiOut == 'on' and: {~canalMidiOutInstr.wrapAt(i).value >= 0}, {
					~freqMidi.wrapAt(i).size.do({arg index; ~midiOut.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0);
						if(flagVST == 'on', {~fxVST.midi.noteOff(~canalMidiOutInstr.wrapAt(i), ~freqMidi.wrapAt(i).wrapAt(index), 0)});
					});
				});
			});
		};

		// Read Datas Control Panel
		~readcontrolpanel={arg d;
			~seuilanalyse.value=d.wrapAt(1);
			~filtreanalyse.value=d.wrapAt(2);
			~canalmidiin=d.wrapAt(4);
			~tempoprocesschaos.value=d.wrapAt(5);
			~tempoprocessneurone.value=d.wrapAt(6);
			~tempoprocessgenetique.value=d.wrapAt(7);
			~tempoprocessautomation.value=d.wrapAt(8);
			~tempoprocessalgorithmes.value=d.wrapAt(9);
			~tempoprocessinstrument.valueAction=d.wrapAt(10);
			~nombreBeats.valueAction=d.wrapAt(11);
			~tempochaos=d.wrapAt(5);
			~temponeurone=d.wrapAt(6);
			~tempogenetique=d.wrapAt(7);
			~tempoautomation=d.wrapAt(8);
			~tempoalgorithmes=d.wrapAt(9);
			~choiceFilter.valueAction=d.wrapAt(14);
			~hzFilter.valueAction=d.wrapAt(15);
		};

		// Save Control Panel
		~savecontrolpanel={arg d;
			d=d.add(~algoAnalyse.value);
			d=d.add(~seuilanalyse.value);
			d=d.add(~filtreanalyse.value);
			d=d.add(~amplitudegeneraleinstrument.value);
			d=d.add(~canalmidiin);
			d=d.add(~tempoprocesschaos.value);
			d=d.add(~tempoprocessneurone.value);
			d=d.add(~tempoprocessgenetique.value);
			d=d.add(~tempoprocessautomation.value);
			d=d.add(~tempoprocessalgorithmes.value);
			d=d.add(~tempoprocessinstrument.value);
			d=d.add(~nombreBeats.value);
			d=d.add(~volumeFileIn.value);
			d=d.add(~offsetFileIn.value);
			d=d.add(~choiceFilter.value);
			d=d.add(~hzFilter.value);
			d=d.add(~canalAudioIn.value);
			d.value;
		};

		setupKeyboardShortCut.valueAction_(1);
		setupKeyboardShortCut.valueAction_(0);
		~fonctionShortCuts.value;
	}

	createScore {
		// PARTITIONS LECTURE ET TIMING (1/24)
		~routinepartitions=Tdef(\partitions, {
			var duree,instr,commande,numero,code,valeur,datas,level;
			loop({
				{40.do({arg i;
					// Evaluation des durees partitions rec
					if(~flagrecpart.wrapAt(i) == 1, {~dureerecpart.wrapPut(i,~dureerecpart.wrapAt(i) + 1)},{nil});
					// play part
					if(~flagplaypart.wrapAt(i) == 1,{~dureeplaypart.wrapPut(i,~dureeplaypart.wrapAt(i) - 1);
						if(~dureeplaypart.wrapAt(i) <= 0, {// lire commande partition a executer
							if(~listeplaypart.wrapAt(i).size != 0, {
								instr=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)).asInteger;commande=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+1);numero=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+2);code=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+3);valeur=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+4);
								duree=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+5);
								if(code=='end',{if(~flaglooppart.wrapAt(i)==0,{~flagplaypart.wrapPut(i,0);~playpartbutton.wrapAt(i).valueAction=0},{~pointeurplaypart.wrapPut(i,0);
									instr=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)).asInteger;commande=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+1);numero=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+2);code=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+3);valeur=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+4);
									duree=~listeplaypart.wrapAt(i).wrapAt(~pointeurplaypart.wrapAt(i)+5);
								})});
								if(commande=='normal',{code.interpret.wrapAt(instr).valueAction=valeur});
								if(commande=='special',{code.interpret.wrapAt(instr).value=valeur.asFloat});
								if(commande=='synth control',{if(~synthcontrol.wrapAt(instr).value == numero, {code.interpret.wrapAt(instr).value=valeur.asFloat});
									~buscontrolsynth.wrapAt(instr).set(valeur.wrapAt(0), valeur.wrapAt(1), valeur.wrapAt(2), valeur.wrapAt(3), valeur.wrapAt(4), valeur.wrapAt(5), valeur.wrapAt(6), valeur.wrapAt(7), valeur.wrapAt(8), valeur.wrapAt(9));datas=~synthcontrolviewparametresdatas.wrapAt(instr).value;
									datas.wrapPut(numero,~synthcontrolviewparametres.wrapAt(instr).value);
									~synthcontrolviewparametresdatas.wrapPut(instr,datas.value);~wavetable.wrapAt(instr).sine2([1,2,3,4,5,6,7,8,9,10],~synthcontrolviewparametres.wrapAt(instr).value)});
								if(commande=='rec level',{if(~numerobuffer.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur});
									~listesamplein.wrapAt(numero).set(\run, ~recsamplebutton.wrapAt(instr).value,\loop, ~looprecsamplebutton.wrapAt(instr).value);~busreclevel.wrapAt(numero).set(valeur.wrapAt(0).value, valeur.wrapAt(1).value);
									~nombreinstrument.do({arg i;
										if(~numerobuffer.wrapAt(i) == numero,{~busreclevel.wrapAt(~numerobuffer.wrapAt(i)).set(valeur.wrapAt(0).value, valeur.wrapAt(1).value);~synthcontrolviewlevels.wrapAt(i).value=~synthcontrolviewlevels.wrapAt(instr).value})});
									~synthcontrolviewlevelsdatas.wrapPut(numero,valeur)});
								if(commande=='envelope',{code.interpret.wrapAt(instr).value=valeur.asFloat;~levelenvsynth.wrapAt(instr).wrapPut(~synthcontrol.wrapAt(instr).value,valeur.wrapAt(1));~timeenvsynth.wrapAt(instr).wrapPut(~synthcontrol.wrapAt(instr).value,valeur.wrapAt(0))});
								if(commande=='algo freq',{if(~choixalgoviewfreq.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat})});
								if(commande=='algo amp',{if(~choixalgoviewamp.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat})});
								if(commande=='algo duree',{if(~choixalgoviewduree.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat})});
								if(commande=='effet_pre',{if(~listenodeeffetsPresynth.wrapAt(instr).value == numero, {code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control1,valeur.wrapAt(0));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control2,valeur.wrapAt(1));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control3,valeur.wrapAt(2));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control4,valeur.wrapAt(3));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control5,valeur.wrapAt(4));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control6,valeur.wrapAt(5));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control7,valeur.wrapAt(6));~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\control8,valeur.wrapAt(7));
									~datas=~synthcontrolvieweffetsPredatas.wrapAt(instr).value;~datas.wrapPut(numero,~synthcontrolvieweffetsPre.wrapAt(instr).value);~synthcontrolvieweffetsPredatas.wrapPut(instr,~datas.value)});
								if(commande=='effet_post',{if(~listenodeeffetsPostsynth.wrapAt(instr).value == numero, {code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control1,valeur.wrapAt(0));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control2,valeur.wrapAt(1));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control3,valeur.wrapAt(2));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control4,valeur.wrapAt(3));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control5,valeur.wrapAt(4));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control6,valeur.wrapAt(5));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control7,valeur.wrapAt(6));~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\control8,valeur.wrapAt(7));
									~datas=~synthcontrolvieweffetsPostdatas.wrapAt(instr).value;~datas.wrapPut(numero,~synthcontrolvieweffetsPost.wrapAt(instr).value);~synthcontrolvieweffetsPostdatas.wrapPut(instr,~datas.value)});
								if(commande=='control panel normal' or: {commande=='masterFX'},{code.interpret.valueAction=valeur});
								if(commande=='key',{code.interpret});
								if(commande=='reverse sample',{if(~numerobuffer.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).valueAction=valeur});
									if(valeur == 0, {~reversesampledatas.wrapPut(instr,~reversesampledatas.wrapAt(instr).wrapPut(numero,1));~busoffsetplaysampler.wrapAt(instr).set(~sampleroffsetcontrol.wrapAt(instr).value)},{~reversesampledatas.wrapPut(instr,~reversesampledatas.wrapAt(instr).wrapPut(numero,-1));~busoffsetplaysampler.wrapAt(instr).set(1-~sampleroffsetcontrol.wrapAt(instr).value)})});
								if(commande=='loop sample',{if(~numerobuffer.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).valueAction=valeur});~loopplaysamplerdatas.wrapPut(instr,~loopplaysamplerdatas.wrapAt(instr).wrapPut(numero,valeur))});
								if(commande=='rec sample',{if(~numerobuffer.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).valueAction=valeur});level=~synthcontrolviewlevels.wrapAt(instr).value;~listesamplein.wrapAt(numero).set(\run, valeur, \trigger,  valeur,\loop, ~looprecsamplebutton.wrapAt(instr).value);
									~busreclevel.wrapAt(numero).set(level.wrapAt(0).value, level.wrapAt(1).value);
									~nombreinstrument.do({arg i;if(~numerobuffer.wrapAt(i) == numero,{level=~synthcontrolviewlevels.wrapAt(instr).value;~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\run, valeur, \trigger,  valeur,\loop, ~looprecsamplebutton.wrapAt(instr).value);
										~busreclevel.wrapAt(numero).set(level.wrapAt(0).value, level.wrapAt(1).value);
										~recsamplebutton.wrapAt(i).value=~recsamplebutton.wrapAt(instr).value})});
									~recsamplebuttondatas.wrapPut(numero,valeur)});
								if(commande=='loop rec sample',{if(~numerobuffer.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).valueAction=valeur});~listesamplein.wrapAt(numero).set(\loop, valeur);
									~nombreinstrument.do({arg i;
										if(~numerobuffer.wrapAt(i) == numero,{~listesamplein.wrapAt(~numerobuffer.wrapAt(i)).set(\loop, valeur);
											~looprecsamplebutton.wrapAt(i).value=~looprecsamplebutton.wrapAt(instr).value})});
									~looprecsamplebuttondatas.wrapPut(numero,valeur)});
								if(commande=='onoff_pre',{if(~listenodeeffetsPresynth.wrapAt(instr).value == numero, {code.interpret.wrapAt(instr).valueAction=valeur});
									if (valeur == 1, {~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).run(true)},
										{~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).run(false)});
									datas=~synthonoffeffetsPredatas.wrapAt(instr).value;
									datas.wrapPut(numero,valeur);
									~synthonoffeffetsPredatas.wrapPut(instr,datas.value)});
								if(commande=='onoff_post',{
									if(~listenodeeffetsPostsynth.wrapAt(instr).value == numero, {code.interpret.wrapAt(instr).valueAction=valeur});
									if (valeur == 1, {~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).run(true)},
										{~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).run(false)});
									datas=~synthonoffeffetsPostdatas.wrapAt(instr).value;
									datas.wrapPut(numero,valeur);
									~synthonoffeffetsPostdatas.wrapPut(instr,datas.value)});
								if(commande=='pan_pre',{if(~listenodeeffetsPresynth.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\pan, valeur);
									~dataspaneffetsPres=~synthpaneffetsPrestepdatas.wrapAt(instr).value;
									~dataspaneffetsPres.wrapPut(numero,valeur);
									~synthpaneffetsPrestepdatas.wrapPut(instr,~dataspaneffetsPres.value)});
								if(commande=='pan_post',{if(~listenodeeffetsPostsynth.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\pan, valeur);
									~dataspaneffetsPosts=~synthpaneffetsPoststepdatas.wrapAt(instr).value;
									~dataspaneffetsPosts.wrapPut(numero,valeur);
									~synthpaneffetsPoststepdatas.wrapPut(instr,~dataspaneffetsPosts.value)});
								if(commande=='amp_pre',{if(~listenodeeffetsPresynth.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPresynth.wrapAt(instr).wrapAt(numero).set(\amp, valeur.dbamp);
									~datasampeffetsPres=~synthampeffetsPrestepdatas.wrapAt(instr).value;
									~datasampeffetsPres.wrapPut(numero,valeur);
									~synthampeffetsPrestepdatas.wrapPut(instr,~datasampeffetsPres.value)});
								if(commande=='amp_post',{if(~listenodeeffetsPostsynth.wrapAt(instr).value==numero,{code.interpret.wrapAt(instr).value=valeur.asFloat});
									~listeeffetsPostsynth.wrapAt(instr).wrapAt(numero).set(\amp, valeur.dbamp);
									~datasampeffetsPosts=~synthampeffetsPoststepdatas.wrapAt(instr).value;
									~datasampeffetsPosts.wrapPut(numero,valeur);
									~synthampeffetsPoststepdatas.wrapPut(instr,~datasampeffetsPosts.value)});
								~dureeplaypart.wrapPut(i,duree);~pointeurplaypart.wrapPut(i,~pointeurplaypart.wrapAt(i)+7)},
							{~flagplaypart.wrapPut(i,0);~dureeplaypart.wrapPut(i,0);~playpartbutton.wrapAt(i).valueAction=0})});
					});
				});
				}.defer;
				(~tempopartitions.reciprocal).wait;
			});
		}
		);

		// WRITE PARTITIONS
		~writepartitions={arg instr, commande, numero, code, valeur;
			var part;
			40.do({arg i;
				if(~flagrecpart.wrapAt(i)==1,{
					if(~listerecpart.wrapAt(i)==[],{part=~listerecpart.wrapAt(i);part=part.add(instr.asSymbol);part=part.add(commande);part=part.add(numero);part=part.add(code);part=part.add(valeur);~listerecpart.wrapPut(i,part);~dureerecpart.wrapPut(i,0)},
						{part=~listerecpart.wrapAt(i);part=part.add(~dureerecpart.wrapAt(i));part=part.add(""++$\r);part=part.add(instr.asSymbol);part=part.add(commande);part=part.add(numero);part=part.add(code);part=part.add(valeur);~listerecpart.wrapPut(i,part);~dureerecpart.wrapPut(i,0)});
				});
			});
		};

		//Load partitions
		~loadpartitions={arg f, d;// Pour charger les parts en fonction du path
			40.do({arg i;
				if(File.exists(~nompathdata++"score"+(i+1).asString++".scd"),
					{f=File(~nompathdata++"score"+(i+1).asString++".scd", "r");d=f.readAllString.interpret;f.close;
						~listeplaypart.wrapPut(i,d);~listerecpart.wrapPut(i,d);
						~flagplaypart.wrapPut(i,0);~dureeplaypart.wrapPut(i,0);},{~listeplaypart.wrapPut(i,[])})});
		};

	}

	initSynthDef {arg flag=true, outAmbisonic, w, x, y, z;

		if(flag == true, {

			// Liste des instruments pour WekRobotMusique version 2010

			// Audio In pour analyse

			SynthDef("OSC WekRobot Onsets",
				{arg in = 0, seuil=0.125, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, inputFilter, array;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);// \rcomplex
					# freqin, hasfreqin = Tartini.kr(inputFilter, filtre, 2048, 1024, 512, 0.5);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot Pitch",
				{arg in = 0, seuil=0.125, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, inputFilter, array;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
					# freqin, hasfreqin = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot Pitch2",
				{arg in = 0, seuil=0.25, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, fft2, inputFilter, harmonic, percussive, array;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					fft2 = FFT(LocalBuf(512, 1), inputFilter);
					harmonic = FFT(LocalBuf(512, 1), inputFilter);
					percussive = FFT(LocalBuf(512, 1), inputFilter);
					#harmonic, percussive = MedianSeparation(fft2, harmonic, percussive, 512, 5, 1, 2, 1);
					detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
					# freqin, hasfreqin = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot KeyTrack",
				{arg in = 0, seuil=0.125, filtre=0.5;
					var input, detect, freqin, ampin, key, array;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);// \rcomplex
					key = KeyTrack.kr(FFT(Buffer.alloc(s, 4096, 1), input), (filtre * 2).clip(0, 2));
					if(key < 12, freqin = (key + 60).midicps, freqin = (key - 12 + 60).midicps);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			// Keyboard Audio
			SynthDef("OSC WekRobot Keyboard",
				{arg in=0,note=60, amp=0.5, trigger = 0;
					var array, input;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					note = note.cpsmidi / 127;
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					SendReply.kr(trigger, '/WekRobot_Analyse_Audio', values: [note, amp] ++ array, replyID: 1);
			}).send(s);

			// MIDI Audio
			SynthDef("OSC WekRobot MIDI",
				{arg in=0,note=60, amp=0.5, trigger = 0;
					var array, input;
					input= Mix(Limiter.ar(SoundIn.ar(in)));
					note = note.cpsmidi / 127;
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					SendReply.kr(trigger, '/WekRobot_Analyse_Audio', values: [note, amp] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot File Onsets",
				{arg bufferplay, busFileIn, seuil=0.125, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, inputFilter, array;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);// \rcomplex
					# freqin, hasfreqin = Tartini.kr(inputFilter,filtre, 2048, 1024, 512, 0.5);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Convertir hertz en midi puis entre (0 et 1) !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot File Pitch",
				{arg bufferplay, busFileIn,  seuil=0.125, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, inputFilter, array;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);// \rcomplex
					# freqin, hasfreqin = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot File Pitch2",
				{arg bufferplay, busFileIn,  seuil=0.25, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
					var input, detect, freqin, hasfreqin, ampin, fft2, inputFilter, harmonic, percussive, array;
					input = In.ar(busFileIn);
					inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
					fft2 = FFT(LocalBuf(512, 1), inputFilter);
					harmonic = FFT(LocalBuf(512, 1), inputFilter);
					percussive = FFT(LocalBuf(512, 1), inputFilter);
					#harmonic, percussive = MedianSeparation(fft2, harmonic, percussive, 512, 5, 1, 2, 1);
					detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
					# freqin, hasfreqin = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("OSC WekRobot File KeyTrack",
				{arg bufferplay, busFileIn,  seuil=0.125, filtre=0.5;
					var input, detect, freqin, ampin, key, array;
					input = In.ar(busFileIn);
					detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);// \rcomplex
					key = KeyTrack.kr(FFT(Buffer.alloc(s, 4096, 1), input), (filtre * 2).clip(0, 2));
					if(key < 12, freqin = (key + 60).midicps, freqin = (key - 12 + 60).midicps);
					ampin = A2K.kr(Amplitude.ar(input));
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					freqin=(freqin.cpsmidi)/127;// Normalisation !!!!!
					SendReply.kr(detect, '/WekRobot_Analyse_Audio', values: [freqin, ampin] ++ array, replyID: 1);
			}).send(s);

			SynthDef("SampleIn",
				{arg in = 0, buffer, offset=0, run=0, loop=0, trigger=0, reclevel1=1, reclevel2=0;
					var samplein;
					samplein=Mix(Limiter.ar(SoundIn.ar(in)));
					RecordBuf.ar(samplein, buffer, offset, reclevel1, reclevel2, run, loop, trigger);
			}).send(s);

			SynthDef("FileIn",
				{arg in=[0, 1], buffer, bufferPlay, offset=0, run=0, loop=0, trigger=0, reclevel1=1, reclevel2=0;
					var fileIn;
					fileIn=In.ar(in);
					RecordBuf.ar(fileIn, buffer, offset, reclevel1, reclevel2, run, loop, trigger);
			}).send(s);

			// Synth lecture file pour analyse AudioIn
			SynthDef("WekRobot Play File",
				{arg out=0, bufferplay, busFileIn, trig=0, offset=0, loop=1, volume=0;
					var input;
					input=PlayBuf.ar(2, bufferplay, BufRateScale.kr(bufferplay), trig, BufFrames.kr(bufferplay)*offset , loop);
					Out.ar(out, input * volume);
					Out.ar(busFileIn, Mix(input));
			}).send(s);

			// Keyboard
			SynthDef("OSC WekRobot Keyboard File",
				{arg in=0, note=60, amp=0.5, trigger = 0;
					var input, array;
					input = In.ar(in);
					note = note.cpsmidi / 127;
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					SendReply.kr(trigger, '/WekRobot_Analyse_Audio', values: [note, amp] ++ array, replyID: 1);
			}).send(s);

			// MIDI
			SynthDef("OSC WekRobot MIDI File",
				{arg in=0, note=60, amp=0.5, trigger = 0;
					var input, array;
					input = In.ar(in);
					note = note.cpsmidi / 127;
					array = MFCC.kr(FFT(LocalBuf(1024, 1), input));// 13 a 40 Bands
					SendReply.kr(trigger, '/WekRobot_Analyse_Audio', values: [note, amp] ++ array, replyID: 1);
			}).send(s);

			// Synth MasterFX
			SynthDef("MasterFX",
				{arg out, limit=0.8, postAmp=1.0;
					ReplaceOut.ar(out, Limiter.ar(LeakDC.ar(In.ar(0, ~numberAudioOut) * postAmp), limit));
			}).send(s);

			// Analyse Tempo AudioIn
			SynthDef("OSC WekRobot Tempo AudioIn",
				{arg in = 0, lock=0;
					var trackb,trackh,trackq,tempo, source;
					source = Mix(Limiter.ar(SoundIn.ar(in)));
					#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(LocalBuf(1024, 1), source), lock);
					SendReply.kr(trackb, '/WekRobot_Analyse_Tempo', values: [tempo], replyID: [1]);
			}).send(s);

			// Analyse Tempo FileIn
			SynthDef("OSC WekRobot Tempo FileIn",
				{arg busFileIn, lock=0;
					var trackb,trackh,trackq,tempo, source;
					source = Limiter.ar(In.ar(busFileIn));
					#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(LocalBuf(1024, 1), source), lock);
					SendReply.kr(trackb, '/WekRobot_Analyse_Tempo', values: [tempo], replyID: [1]);
			}).send(s);

			// Synth VST
			SynthDef("VST Plugin",
				{arg out=0, xFade=0.5, panLo=0, panHi=0, gainIn=0.5, bpm=1;
					var signal, chain, ambisonic;
					bpm = if(bpm > 1, bpm.reciprocal, bpm);
					signal = Mix(In.ar(0, ~numberAudioOut)) * gainIn;
					chain = Mix(VSTPlugin.ar(signal, ~numberAudioOut));
					chain = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(chain, Rand(panLo, panHi)),
							// Pan 2
							Pan2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), bpm))),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, chain, Rand(panLo, panHi), 1, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), bpm), 1, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(chain, chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), bpm)),
								// Ambisonic
								(ambisonic = PanB2.ar(chain, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), bpm));
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					XOut.ar(out, xFade, chain);
			}).add;

			// Modele de definition de synth !!!!!

			SynthDef("PlayBuf",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)
					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					//main = Limiter.ar(main, 1.0, 0.01);
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("SinOsc",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls=#[0,0,0,0,0,0,0,0,0,0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=SinOsc.ar(freq);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("MdaPiano",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls=#[0,0,0,0,0,0,0,0,0,0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Synthesizer",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls=#[0,0,0,0,0,0,0,0,0,0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					osc = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					main = if(freq < 64.5.midicps , RLPF.ar(osc, XLine.ar(63.5.midicps*controls.at(0)+27.5, freq, duree*controls.at(2)), 0.333), RHPF.ar(osc, XLine.ar(127.midicps*controls.at(1)+27.5, freq, duree*controls.at(2)), 0.333));
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Resonz",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls=#[0,0,0,0,0,0,0,0,0,0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)
					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					osc = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					if(freqRate.abs >= 1.0 , main=Resonz.ar(osc, XLine.ar(127.midicps*controls.at(0)+21.midicps, 55*controls.at(1) + 21.midicps, duree*controls.at(2))), main=Resonz.ar(osc, XLine.ar(55*controls.at(0)+21.midicps, 127.midicps*controls.at(1) + 21.midicps, duree*controls.at(2))));
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Squiz",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls=#[0,0,0,0,0,0,0,0,0,0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Squiz.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controls.at(0) * 10, controls.at(1) * 10);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano WaveLoss",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=WaveLoss.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controls.at(0) * 40, 40, abs(controls.at(0)*2-1));
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano FreqShift",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Mix(FreqShift.ar(Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8))), controls.at(0) * 512, controls.at(1) * 2pi));
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagShift",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagShift(main, controls.at(0) * 4, controls.at(1) * 128 - 64);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano LocalMax",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_LocalMax(main, controls.at(0)*64);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagSmear",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmear(main, controls.at(0)*64);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano RandComb",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RandComb(main, controls.at(0),  LFNoise2.kr(controls.at(1)*64));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano BinShift",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinShift(main, controls.at(0)*4,  controls.at(1)*256 - 128);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano BinScramble",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BinScramble(main, controls.at(0), controls.at(1), LFNoise2.kr(controls.at(2).reciprocal));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano BrickWall",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_BrickWall(main, controls.at(0)*2 - 1);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano ConformalMap",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_ConformalMap(main, controls.at(0)*2 - 1, controls.at(1)*2 - 1);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Diffuser",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Diffuser(main, Trig1.kr(LFNoise2.kr(controls.at(0)*100), (controls.at(1)*100).reciprocal));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagAbove",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagAbove(main, controls.at(0)*64);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagBelow",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagBelow(main, controls.at(0)*64);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagClip",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagClip(main, controls.at(0)*16);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagNoise",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagNoise(main);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagSquared",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSquared(main);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano RectComb",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RectComb(main, controls.at(0) * 32, controls.at(1), controls.at(2));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagSmooth",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_MagSmooth(main, controls.at(0));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Compander",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_Compander(main, controls.at(0)*64, controls.at(1)*10, controls.at(2)*10);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano RandComb",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, osc, recBuf, ambisonic;
					// Set Rate Freq
					freqRate=2**freqRate.cpsoct;
					freqRate=freqRate * reverse;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(main, buffer, 0, 1, 0);
					// Main Synth
					main = FFT(LocalBuf(1024, 1), main);
					main = PV_RandComb(main, controls.at(0),  LFNoise2.kr(controls.at(1)*64));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Max",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_Max(fft1, fft2);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Min",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_Min(fft1, fft2);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano MagDiv",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_MagDiv(fft1, fft2, controls.at(1)+0.0001);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Mul",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_Mul(fft1, fft2);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Add",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_Add(fft1, fft2);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano RandWipe",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_RandWipe(fft1, fft2, controls.at(1), LFNoise2.kr(controls.at(2).reciprocal));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano BinWipe",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_BinWipe(fft1, fft2, controls.at(1)*2 - 1);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano CopyPhase",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_CopyPhase(fft1, fft2);
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano RectComb2",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_RectComb2(fft1, fft2, controls.at(1) * 32, controls.at(2), controls.at(3));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Morph",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					fft1 = FFT(LocalBuf(1024, 1), in1);
					fft2 = FFT(LocalBuf(1024, 1), in2);
					main=PV_Morph(fft1, fft2, controls.at(1));
					main= IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano Convolution",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, dureesample, in1, in2, fft1, fft2, recBuf, rate, ambisonic;
					// Set Rate Freq
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Synth
					in1 = Mix(MdaPiano.ar(freq, gate: 1, vel: 127 * amp, hard: amp.min(0.8)));
					// RecordBuf
					recBuf=RecordBuf.ar(in1, buffer2, 0, 1, 0);
					//pos = if(controls.at(1).value <= 0.01 , pos, Logistic.kr(controls.at(1)*4, 1, Rand(0, 1)));
					// Sample
					in2=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, BufFrames.kr(buffer)*controls.at(0), loop);
					// Main Synth
					main=Convolution.ar(in1, in2, 1024);
					//main = Limiter.ar(main, 1.0, 0.01);
					//
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);


			//////////////////////////////////// SYNTH ////////////////////////////////////////

			SynthDef("Guitare",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, pluck, ambisonic;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: duree, levelScale: 1, doneAction: 2);
					// Sample
					pluck = BrownNoise.ar(Decay.kr(HPZ1.kr(Impulse.kr(controls.at(0)*1000))), controls.at(1));
					main = CombL.ar(pluck, freq.reciprocal, freq.reciprocal, duree);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("VarSaw",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=VarSaw.ar(freq, Rand(0,1), controls.at(0));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Pulse",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Pulse.ar(freq,controls.at(0));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Osc",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost,  freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Osc.ar(wavetable,freq);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("VOsc",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0, buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=VOsc.ar(wavetable2+(controls.at(0)*6.999),freq);// bufnum=0+controls.at(0)*6.999
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("VOsc3",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0, buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1/3, doneAction: 2);
					// Synth
					main=VOsc3.ar(wavetable2+(controls.at(0)*6.999),freq,freq+(freq*controls.at(1)),freq-(freq*controls.at(2)));// bufnum=0+controls.at(0)*6.999
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("TGrains",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0, buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=Mix(TGrains.ar(2, Impulse.kr(controls.at(0)*100), buffer, BufRateScale.kr(buffer)*rate, BufDur.kr(buffer)*pos, (controls.at(1)*duree)/(controls.at(0)*100), 0.0, 1, controls.at(2)*4));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Klang",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, arrayf, arraya, arrayd, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					arrayf=[freq,freq*controls.at(0),freq*controls.at(1)*2,freq*controls.at(2)*3,freq*controls.at(3)*4,freq*controls.at(4)*5,freq*controls.at(5)*6,freq*controls.at(6)*7,freq*controls.at(7)*8,freq*controls.at(8)*9,freq*controls.at(9)*10];
					arraya=Array.fill(11,{1/11});
					arrayd=Array.fill(11,{0});
					main=Mix(DynKlang.ar(`[arrayf,arraya,arrayd],1,0));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("BufRd",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=BufRd.ar(1,buffer,Phasor.ar(trigger,BufRateScale.kr(buffer)*rate,BufFrames.kr(buffer)*pos,BufFrames.kr(buffer)*controls.at(0),BufFrames.kr(buffer)*controls.at(1)), loop, 2);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Spring",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, inforce, k, d, outforce, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1/6, doneAction: 2);
					// Synth
					inforce = LFPulse.ar(controls.at(0) * duree);
					k = controls.at(1) * 20;
					d = controls.at(2) * 0.001;
					outforce = Spring.ar(inforce, k, d);
					outforce = outforce * freq + freq;
					//main = SinOsc.ar(freq, 0, 0.5);
					main = PMOsc.ar(freq, outforce, Line.kr(controls.at(3), controls.at(4) * 2pi));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Formant",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Formant.ar(freq,(controls.at(0)*127).midicps,(controls.at(1)*127).midicps);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Blip",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Blip.ar(freq, Line.kr(50*controls.at(0)+1,50*controls.at(1)+1,duree*controls.at(2)));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Gendy3",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main = Gendy3.ar(controls.at(0) * 6, controls.at(1) * 6, controls.at(2) * 0.1, controls.at(3) * 0.1, freq, controls.at(4) * 0.1, controls.at(5) * 0.1);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Klank",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1/10, doneAction: 2);
					// Synth
					main=DynKlank.ar(`[[1,controls.at(1),controls.at(2)*2,controls.at(3)*3,controls.at(4)*4,controls.at(5)*5,controls.at(6)*6,controls.at(7)*7,controls.at(8)*8,controls.at(9)*9], nil,[1,1,1,1,1,1,1,1,1,1]],Dust2.ar(controls.at(0)*100), freq);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Klank2",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, ambisonic, envelope;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1/10, doneAction: 2);
					// Synth
					main=DynKlank.ar(`[[1,controls.at(1),controls.at(2)*2,controls.at(3)*3,controls.at(4)*4,controls.at(5)*5,controls.at(6)*6,controls.at(7)*7,controls.at(8)*8,controls.at(9)*9], nil, [1,1,1,1,1,1,1,1,1,1]],ClipNoise.ar(controls.at(0)*0.1), freq);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Spring*Buf",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, inforce, damp, freqspring, dureesample, ambisonic;
					rate=2**(freq.cpsmidi+(controls.at(5)*8-4*12)).midicps.cpsoct*reverse;
					dureesample=(1/rate)*BufDur.kr(buffer);dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					// Synth
					inforce=LFPulse.ar(controls.at(3)*100);
					damp=controls.at(4)*duree/10;
					freqspring=controls.at(5)*1000;
					main=SinOsc.ar(Spring.ar(inforce,duree/10/controls.at(6),damp)*freqspring)*in;
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PlayBufAdd",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope1, envelope2, rate1, rate2, in1, in2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(7)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(6)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope1=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1 + controls.at(8), doneAction: 2);
					envelope2=EnvGen.ar(Env.perc(controls.at(5),1,1,controls.at(6)*8-4),timeScale: dureesample, levelScale: controls.at(7), doneAction: 0);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(0), controls.at(1)) * envelope1;
					in2=HPplayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(4), loop2, controls.at(2), controls.at(3)) * envelope2;
					main=in1+in2;
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), 1),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), 1, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * 1,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("KlankPlayBuf",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, rate, dureesample, in, ambisonic;
					rate=2**(freq.cpsmidi+(controls.at(0)*8-4*12)).midicps.cpsoct*reverse;
					dureesample=(1/rate)*BufDur.kr(buffer);dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: dureesample, levelScale: 1/10, doneAction: 2);
					// Synth
					in = HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, Dust2.kr(0*100), BufFrames.kr(buffer)*pos, loop, 0.33, 0.5);
					main=DynKlank.ar(`[[1,controls.at(1),controls.at(2)*2,controls.at(3)*3,controls.at(4)*4,controls.at(5)*5,controls.at(6)*6,controls.at(7)*7,controls.at(8)*8], nil, 1/freq.size], in*0.1, freq);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PianoPercu",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, rate, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Klank.ar(`[[controls.at(4)+1, controls.at(5)/2+1, controls.at(6)/3+1,1-controls.at(7),1-(controls.at(8)/2),1-(controls.at(9)/3)]],Decay2.ar(Impulse.ar(duree/1000,0,0.025),controls.at(1)/10,controls.at(2),LFNoise2.ar(12000*controls.at(0))),freq,0,controls.at(3));
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("BowedString",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, rate, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Klank.ar(`[Array.series(12, freq, freq),Array.geom(12,1,rrand(controls.at(0),controls.at(1))),Array.fill(12, {rrand(1,3)})], BrownNoise.ar(0.007) * LFNoise1.kr(duree/16));
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("StruckString",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, rate, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					main=Klank.ar(`[Array.series(16, freq, freq),Array.geom(16,1,rrand(controls.at(3),controls.at(4))),Array.fill(16, {rrand(0.1,2.5)})],Decay2.ar(Impulse.ar(duree/1000),controls.at(1)/100,controls.at(2)/100,BrownNoise.ar(controls.at(0)/2)));
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);


			SynthDef("PianoSynth",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope,listefreq, delaytime, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					listefreq=listefreq.add(1/freq);listefreq=listefreq.add(1/(freq-(controls.at(3)*8)));listefreq=listefreq.add(1/(freq+(controls.at(4)*8)));
					main=Mix.ar(Array.fill(3, {arg i;delaytime=listefreq.wrapAt(i);
						CombL.ar(Decay2.ar(Impulse.ar(duree/1000),controls.at(1)/10,controls.at(2),LFNoise2.ar(3000*controls.at(0))),0.01,delaytime,duree,mul: 0.6)}));
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PianoPrepare",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope,listefreq, delaytime, pp, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					listefreq=listefreq.add(1/freq);listefreq=listefreq.add(1/(freq-(controls.at(5)*8)));listefreq=listefreq.add(1/(freq+(controls.at(6)*8)));
					pp=Decay2.ar(Impulse.ar(duree/1000),controls.at(0)*duree,controls.at(1)*duree);
					pp=Resonz.ar(pp,controls.at(2)*800,0.4);
					pp=LPF.ar(pp,controls.at(3)*12000+1);
					pp=Integrator.ar(pp,0.99);
					pp=HPF.ar(pp,controls.at(4)*400);
					main=Mix.ar(Array.fill(3, {arg i;delaytime=listefreq.wrapAt(i);
						CombL.ar(pp,0.01,delaytime,duree,mul: 0.6)}));
					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Piano+Sample",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope1, envelope2, listefreq, rate, in1, in2, delaytime, dureesample, ambisonic;
					rate=2**(freq.cpsmidi+(controls.at(6)*8-4*12)).midicps.cpsoct*reverse2;
					dureesample=(1/rate)*BufDur.kr(buffer);dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					// envelope (identique pour chaque sample !!!!!)

					envelope1=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: duree, levelScale: controls.at(5), doneAction: 2);
					envelope2=EnvGen.ar(Env.linen(controls.at(7),controls.at(8),controls.at(9),1,4),timeScale: dureesample, levelScale: 1, doneAction: 0);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in2=HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, 0, loop, 0.33, 0.5) * envelope2;
					listefreq=listefreq.add(1/freq);listefreq=listefreq.add(1/(freq-(controls.at(3)*8)));listefreq=listefreq.add(1/(freq+(controls.at(4)*8)));
					in1=Mix.ar(Array.fill(3, {arg i;delaytime=listefreq.wrapAt(i);
						CombL.ar(Decay2.ar(Impulse.ar(duree/1000),controls.at(1)/10,controls.at(2),LFNoise2.ar(3000*controls.at(0))),0.01,delaytime,duree,envelope1)}));
					main=in1+in2;
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), 1),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), 1, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * 1,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("String+Sample",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope1, envelope2, listefreq, rate, in1, in2, delaytime, dureesample, ambisonic;
					rate=2**(freq.cpsmidi+(controls.at(6)*8-4*12)).midicps.cpsoct*reverse2;
					dureesample=(1/rate)*BufDur.kr(buffer);dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					// envelope (identique pour chaque sample !!!!!)

					envelope1=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: controls.at(5), doneAction: 2);
					envelope2=EnvGen.ar(Env.linen(controls.at(7),controls.at(8),controls.at(9),1,4),timeScale: dureesample, levelScale: 1, doneAction: 0);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in2=HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, 0, 0, loop2, 0.33, 0.5) * envelope2;
					listefreq=listefreq.add(1/freq);listefreq=listefreq.add(1/(freq-(controls.at(3)*8)));listefreq=listefreq.add(1/(freq+(controls.at(4)*8)));
					in1=Klank.ar(`[Array.series(16, freq, freq),Array.geom(16,1,rrand(controls.at(3),controls.at(4))),Array.fill(16, {rrand(0.1,2.5)})],Decay2.ar(Impulse.ar(duree/1000),controls.at(1)/100,controls.at(2)/100,BrownNoise.ar(controls.at(0)/2))) * envelope1;
					main=in1+in2;
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), 1),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), 1, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * 1,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), 1);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("CordeAnalogique",
				{// listes de arguments (identique pour chaque synth !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque synth !!!!!)
					var main, envelope, fc, osc, ambisonic;
					// envelope (identique pour chaque synth !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4),timeScale: duree, levelScale: 1, doneAction: 2);
					// Synth
					fc = LinExp.kr(LFNoise1.kr(Rand(0.015625, duree)), -1,1,500,2000);
					osc = Mix.fill(8, {LFSaw.ar(freq * [Rand(0.99,1.01),Rand(0.99,1.01)], 0, amp) }).distort * 0.2;
					main= Mix(RLPF.ar(osc, fc, 0.1));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), duree), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("LoopBuf",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=LoopBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, BufFrames.kr(buffer)*controls.at(0), BufFrames.kr(buffer)*controls.at(1));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("SampleResonz",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, debut, fin, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(0), controls.at(1));
					main=Resonz.ar(main, XLine.ar(12544*controls.at(2)+27.5, 110*controls.at(3) + 27.5, controls.at(4)*dureesample));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PlayBufSquiz",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=Squiz.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(0), controls.at(1)), controls.at(2) * 10, controls.at(3) * 10);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("WaveLoss",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=WaveLoss.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(0), controls.at(1)), controls.at(2) * 40, 40, abs(controls.at(3) * 2 - 1));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("FreqShift",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=Mix(FreqShift.ar(HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(0), controls.at(1)), controls.at(2) * 512, controls.at(3) * 2pi));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Synthesizer",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, osc, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					osc =HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(3), controls.at(4));
					if(freq < 64.5.midicps , main = RLPF.ar(osc, XLine.ar(63.5.midicps*controls.at(5)+27.5, freq, duree*controls.at(6)), 0.333), main = RHPF.ar(osc, XLine.ar(127.midicps*controls.at(7)+27.5, freq, duree*controls.at(8)), 0.333));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Warp1",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, pointer, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					//pointer = if(reverse > 0, Line.kr(0, pos, dureesample), Line.kr(1, pos, dureesample));
					main=Warp1.ar(1, buffer, pos, rate, controls.at(0) + 0.01, -1, controls.at(1)*15+1, controls.at(2));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("HPplayBuf",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(3), controls.at(4));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("HPplayBufMedianLeakDC",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=LeakDC.ar(Median.ar(controls.at(5) * 30 + 1, HPplayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop, controls.at(3), controls.at(4))), controls.at(6));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("HPplayBufVibrato",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=HPplayBuf.ar(1, buffer, SinOsc.kr(controls.at(6)*dureesample.reciprocal*10, mul: controls.at(6), add: rate), trigger, BufFrames.kr(buffer)*pos, loop, controls.at(3), controls.at(4));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PlayBufSquiz",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=Squiz.ar(PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop), controls.at(0)* 10, controls.at(1) * 10);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("HPtGrains",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0, buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=Mix(TGrains.ar(2, Impulse.kr(controls.at(0)*100), buffer, BufRateScale.kr(buffer)*rate, BufDur.kr(buffer)*pos, (controls.at(1)*duree)/(controls.at(2)*100), 0.0, 1));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("HPbufRd",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0,  buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=BufRd.ar(1,buffer,Phasor.ar(trigger,BufRateScale.kr(buffer)*rate,BufFrames.kr(buffer)*pos,BufFrames.kr(buffer)*controls.at(0),BufFrames.kr(buffer)*controls.at(1)), rate, loop);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_BinScramble",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main=PV_BinScramble(main,controls.at(0),controls.at(1), LFNoise2.kr(controls.at(2)*100));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_BinShift",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, fft, in, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					fft=FFT(LocalBuf(1024, 1), in);
					main=PV_BinShift(fft, controls.at(0)*4, controls.at(1)*256-128);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_ConformalMap",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, in2, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					fft=FFT(LocalBuf(1024, 1), in);
					main=PV_ConformalMap(fft, controls.at(0)*2-1, controls.at(1)*2-1);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_LocalMax",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					fft=FFT(LocalBuf(1024, 1), in);
					main=PV_LocalMax(fft, controls.at(0)*64);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagSmear",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					fft=FFT(LocalBuf(1024, 1), in);
					main=PV_MagSmear(fft, controls.at(0)*64);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_RandComb",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main=PV_RandComb(main,controls.at(0), LFNoise2.kr(controls.at(1)*64));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagShift",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main=PV_MagShift(main,controls.at(0)*4, controls.at(1)*128-64);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagSquared",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagSquared(main);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagNoise",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagNoise(main);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagClip",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagClip(main, controls.at(0)*16);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagAbove",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagAbove(main, controls.at(0)*64);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagBelow",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagBelow(main, controls.at(0)*64);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Diffuser",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_Diffuser(main, Trig1.kr(LFNoise2.kr(controls.at(0)*100), (controls.at(1)*100).reciprocal));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_BrickWall",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_BrickWall(main, controls.at(0)*2 - 1);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_RectComb",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_RectComb(main, controls.at(0) * 32, controls.at(1), controls.at(2));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagSmooth",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_MagSmooth(main, controls.at(0));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Compander",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main=FFT(LocalBuf(1024, 1), in);
					main = PV_Compander(main, controls.at(0)*64, controls.at(1)*10, controls.at(2)*10);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Cutoff",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, in, fft, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					in=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main = FFT(LocalBuf(1024, 1), in);
					main = PV_Cutoff(main, controls.at(0) * 2 - 1);
					main = IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Mul",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(1)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(2)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_Mul(fft1, fft2);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Convolution",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					main=Convolution.ar(in1, in2, 1024);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Max",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_Max(fft1, fft2);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Min",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_Min(fft1, fft2);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Add",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(1)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(2)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_Add(fft1, fft2);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_MagDiv",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_MagDiv(fft1, fft2, controls.at(1)+0.0001);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_BinWipe",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_BinWipe(fft1, fft2, controls.at(1)*4 - 1);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_CopyPhase",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_CopyPhase(fft1, fft2);
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_RectComb2",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_RectComb2(fft1, fft2, controls.at(1) * 32, controls.at(2), controls.at(3));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_Morph",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(3)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(4)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_Morph(fft1, fft2, controls.at(1));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("PV_RandWipe",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate1, rate2, in1, in2, fft1, fft2, dureesample1, dureesample2, dureesample, ambisonic;
					rate1=(2**(freqRate.cpsmidi+(controls.at(5)*8-4*12)).midicps.cpsoct*reverse);
					rate2=(2**(freqRate.cpsmidi+(controls.at(6)*8-4*12)).midicps.cpsoct*reverse2);
					dureesample1=BufDur.kr(buffer)/rate1.abs;dureesample1=dureesample1+(loop*(duree-dureesample1));dureesample1=clip2(duree,dureesample1);
					dureesample2=BufDur.kr(buffer2)/rate2.abs;dureesample2=dureesample2+(loop2*(duree-dureesample2));dureesample2=clip2(duree,dureesample2);
					dureesample=clip2(dureesample1,dureesample2);
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample Attention les buffers doivent avoir la meme longueur !!!!!!!!!!!
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate1, trigger, BufFrames.kr(buffer)*pos, loop);
					in2=PlayBuf.ar(1, buffer2, BufRateScale.kr(buffer2)*rate2, trigger, BufFrames.kr(buffer2)*controls.at(0), loop2);
					fft1=FFT(LocalBuf(1024, 1), in1);
					fft2=FFT(LocalBuf(1024, 1), in2);
					main=PV_RandWipe(fft1, fft2, controls.at(1), LFNoise2.kr(controls.at(2)*100));
					main=IFFT(main);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Granulation",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, ambisonic;
					local = LocalBuf(4096, 1).clear;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					main = BufRd.ar(1, local, Phasor.ar(0, controls.at(3) * 4 - 2, 0, BufFrames.kr(local)), 1);
					BufWr.ar(DelayC.ar(in1, 1, controls.at(4)), local, Phasor.ar(0, controls.at(5) * 4 - 2, 0, BufFrames.kr(local)), 1);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Toupie",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, in2, ambisonic;
					local = LocalBuf(4096, 1).clear;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					in1=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					in2 = BufRd.ar(1, local, Phasor.ar(0, controls.at(3) * 2, 0, BufFrames.kr(local)), 1);
					main = in1 + in2;
					BufWr.ar(main, local, Phasor.ar(0, controls.at(4) * 2, 0, BufFrames.kr(local)), 1);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Elastique",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, in2, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, Impulse.kr(controls.at(0)*100), BufFrames.kr(buffer)*pos, loop);
					main = CombC.ar(main, 0.1, Line.kr(controls.at(1).clip(0.01, 0.99)/100, controls.at(2).clip(0.01, 0.99)/100, controls.at(3).clip(0.01, 1.0)*dureesample));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("RandElastique",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, in2, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, Impulse.kr(controls.at(0)*100), BufFrames.kr(buffer)*pos, loop);
					main = CombC.ar(main, 0.1, Line.kr(Rand(controls.at(1).clip(0.01, 0.99), controls.at(2).clip(0.01, 0.99))/100, Rand(controls.at(3).clip(0.01, 0.99), controls.at(4).clip(0.01, 0.99))/100, controls.at(5).clip(0.01, 1.0)*dureesample));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("RandKlankSample",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, in2, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, Impulse.kr(controls.at(0)*100), BufFrames.kr(buffer)*pos, loop);
					main = DynKlank.ar(`[[Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186),Rand(55, 4186)], nil, [0.17,0.17,0.17,0.17,0.17,0.17]], main, controls.at(1), controls.at(2), controls.at(3));
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("DjScratch",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, in1, in2, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					main = BufRd.ar(1, buffer, Phasor.ar(Dust.kr(dureesample.reciprocal), rate, BufFrames.kr(buffer)* controls.at(0), BufFrames.kr(buffer)* controls.at(1) ).lag(controls.at(2))*LFNoise2.kr(controls.at(3)).sign, 1);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("LiquidFilter",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					var source, effet, post;
					var formantfreqs, formantamps, formantbandwidths; //data for formants
					formantfreqs= [0.3, 0.6, 1, 1.6, 2.3, 2.6, 3, 3.3]; //centre frequencies of formants
					formantamps= ([0 , -3, -6, -12, -18, -24, -30, -36]).dbamp; //peaks of formants
					formantbandwidths=[40, 80, 120, 160, 200, 240, 280, 320];  //bandwidths
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					source=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					effet= Mix(RHPF.ar(source,  formantfreqs*freq*controls.at(0), formantbandwidths/(formantfreqs*freq*controls.at(1).clip(0.01, 1.0))*(controls.at(2).clip(0.01, 1.0) / 33), 1, 0));
					post = BBandPass.ar(effet, LFNoise2.kr(dureesample)+1*4186, controls.at(3).clip(0.01, 1.0));
					main = Limiter.ar(post, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("Centroid",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass = 1,  panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, local, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, trigger, BufFrames.kr(buffer)*pos, loop);
					local =FFT(LocalBuf(1024, 1), main);
					local = SpecCentroid.kr(local);
					main = MidEQ.ar(main, local.unipolar, controls.at(3).clip(0.01, 1.0)*512/local.unipolar, controls.at(4).clip(0.01, 1.0)-0.5*128);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			SynthDef("GrainBuf",
				{// listes de arguments (identique pour chaque sample !!!!!)
					arg out=0, buseffetsPre, buseffetsPost, freq=0, freqRate=0, amp=0, ampPre=0, ampPost=0, byPass=0, panLo=0, panHi=0, pos=0,  trigger=1, duree=1.0, loop=0, reverse=0, loop2=0, reverse2=0, wavetable, wavetable2=0,  buffer, buffer2,  gate=1, controlenvlevel1=0, controlenvlevel2=0.3, controlenvlevel3=1.0, controlenvlevel4=0.75, controlenvlevel5=0.5, controlenvlevel6=0.33, controlenvlevel7=0.1, controlenvlevel8=0, controlenvtime1=0.001, controlenvtime2=0.01, controlenvtime3=0.25, controlenvtime4=0.33, controlenvtime5=0.5, controlenvtime6=0.75, controlenvtime7=1.0, controls = #[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
					// liste des variables (identique pour chaque sample !!!!!)
					var main, envelope, rate, dureesample, ambisonic;
					rate=2**freqRate.cpsoct;
					dureesample=BufDur.kr(buffer)/rate.abs;dureesample=dureesample+(loop*(duree-dureesample));dureesample=clip2(duree,dureesample);
					rate = rate * reverse;
					// envelope (identique pour chaque sample !!!!!)

					envelope=EnvGen.ar(Env.new([controlenvlevel1,controlenvlevel2,controlenvlevel3,controlenvlevel4,controlenvlevel5,controlenvlevel6,controlenvlevel7,controlenvlevel8],[controlenvtime1,controlenvtime2,controlenvtime3,controlenvtime4,controlenvtime5,controlenvtime6,controlenvtime7].normalizeSum,4), gate, timeScale: dureesample, levelScale: 1, doneAction: 2);
					// Sample
					pos = if(controls.at(1).value <= 0.5 , pos, Logistic.kr(controls.at(2) * 4, 1, Rand(0, 1)));
					// Sample
					trigger = Impulse.kr(controls.at(0)*100);
					main=PlayBuf.ar(1, buffer, BufRateScale.kr(buffer)*rate, Impulse.kr(controls.at(3)*100), BufFrames.kr(buffer)*pos, loop);
					main=GrainBuf.ar(1, Dust.kr(100*controls.at(4)), controls.at(5)*0.1, buffer, rate, pos, 4, 0, -1, 512);
					//main = Limiter.ar(main, 1.0, 0.01);

					// Switch Audio Out
					main = if(~switchAudioOut == 0,
						if(flagMC == 0,
							// Pan 1
							Pan2.ar(main, Rand(panLo, panHi), envelope),
							// Pan 2
							Pan2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope)),
						if(~switchAudioOut == 2,
							if(flagMC == 0,
								// PanAz 1
								PanAz.ar(~numberAudioOut, main, Rand(panLo, panHi), envelope, widthMC, orientationMC),
								// PanAz 2
								PanAz.ar(~numberAudioOut, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope, widthMC, orientationMC)),
							if(~switchAudioOut == 1,
								// Rotate2
								Rotate2.ar(main, main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample)) * envelope,
								// Ambisonic
								(ambisonic = PanB2.ar(main, Line.kr(Rand(panLo, panHi), Rand(panLo, panHi), dureesample), envelope);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPre, Mix(main) * amp * ampPre);
					Out.ar(buseffetsPost, Mix(main) * amp * ampPost * byPass);
					Out.ar(out, main * amp * byPass);
			}).send(s);

			// Model de definition des Effets _pre

			SynthDef("Ringz_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.125, control2=0.125, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Ringz.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5*0.1,control6*0.1,control7*0.1,control8*0.1], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("TwoPole_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.125, control2=0.125, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(TwoPole.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5,control6,control7,control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PitchShift_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.1, control2=0.1, control3=0.1, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(PitchShift.ar(ineffet, 0.3, [control1, control2, control3, control4, control5, control6]*4.0, control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("AllpassC_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(AllpassC.ar(ineffet, 0.2, [control1,control2/2,control3/3,control4/4], [control5*30,control6*30,control7*30,control8*30], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("CombC_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(CombC.ar(ineffet, 0.2, [control1/100,control2/200,control3/300,control4/400], [control5*16,control6*16,control7*16,control8*16], amp/4*0.6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DelayC_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(DelayC.ar(ineffet, 4.0, [control1*4.0,control2*4.0,control3*4.0,control4*4.0,control5*4.0,control6*4.0,control7*4.0,control8*4.0], amp));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DelayCtempo_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0.60, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, tempo, beat, duree;
					tempo = (control7*60).floor;
					beat=(control8*4).floor;
					duree = 60/tempo*beat;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(DelayC.ar(ineffet, 10.0, [control1*duree,control2*duree,control3*duree,control4*duree,control5*duree,control6*duree], amp));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BPF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(BPF.ar(ineffet, [control1*3000+27.5,control2*3000+3000,control3*3000+6000,control4*3000+9000], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BRF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(BRF.ar(ineffet, [control1*3000+27.5,control2*3000+3000,control3*3000+6000,control4*3000+9000], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Formlet_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Formlet.ar(ineffet,[control1*300,control2*300+300,control3*300+600,control4*300+900,control5*300+1200,control6*300+1500], control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FOS_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(FOS.ar(ineffet, [control1,control2,control3,control4,control5,control6], control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("HPF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(HPF.ar(ineffet,[control1*4186+312, control2*4186+312, control3*4186+312, control4*4186+312, control5*8372+312, control6*8372+312, control7*8372+312, control8*8372+312], amp/8));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LPF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(LPF.ar(ineffet, [control1*500+27.5, control2*500+27.5, control3*500+27.5, control4*500+27.5, control5*1000+27.5, control6*1000+27.5, control7*1000+27.5, control8*1000+27.5], amp/8));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Resonz_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Resonz.ar(ineffet, [control1*1000,control2*1000+1000,control3*1000+2000,control4*1000+3000], [control5,control6,control7,control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RHPF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(RHPF.ar(ineffet, [control1*4186+312, control2*4186+312, control3*4186+312, control4*4186+312], [control5, control6, control7, control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RLPF_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(RLPF.ar(ineffet, [control1*1000+27.5, control2*1000+27.5, control3*1000+27.5, control4*1000+27.5], [control5, control6, control7, control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("SOS_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=SOS.ar(ineffet, control1, control2, control3, control4*(-1.0), control5*(-1.0), amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Median.ar(control1 * 30 + 1, ineffet, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LeakDC_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=LeakDC.ar(ineffet, control1, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median+LeakDC_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=LeakDC.ar(Median.ar(control1 * 30 + 1 ,ineffet, amp), control2);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FreeVerb_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=FreeVerb.ar(ineffet, control1, control2, control3, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("GVerb_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					#left, right =GVerb.ar(ineffet, (control1*300).clip(1, 300), (control5*100).clip(0.01, 100), control6, control7, 15, control2, control3, control4, 300, amp);
					effet=Mix(left,right);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("JPverb_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet = Mix(JPverb.ar(ineffet, control1 * 60, control2, control3 * 5, control4, control5, control6, control7 *5900 + 100, control8 * 9000 + 1000)) * amp;
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("WarpDelay_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right, local, localBuf;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(ineffet * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0, loop: 0, trigger: Impulse.kr(control1));
					// effet
					effet = Warp1.ar(1, localBuf, control2, control3*4, control4, -1, control5*16, control6);// + ou - local;
					effet = Limiter.ar(effet, 1.0, 0.01);
					LocalOut.ar(DelayC.ar(effet, 1, control7, control8));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RandRateDelay_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.0625, control2=0.5, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right, local, localBuf, buffer, rate;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					buffer = RecordBuf.ar(ineffet, localBuf, 0, 1, 0);
					rate = control1*4;
					RecordBuf.ar(ineffet * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf, 0, 1, 0, loop: 0, trigger: Impulse.kr(control1), preLevel: 0.333);
					// effet
					effet = PlayBuf.ar(1, localBuf, LFNoise2.kr(control2)+rate, Dust.kr(control3), Logistic.kr(control4/2+3.5, 100, Rand(0, 1))* BufFrames.kr(localBuf), 1, 0.05, 0.1) + local * amp / 2;
					effet = Mix(effet);
					effet = Limiter.ar(effet, 1.0, 0.01);
					LocalOut.ar(DelayC.ar(effet, 1.0, control5/1000, control6));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PV_Cutoff_pre",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, buseffetsPost, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet = FFT(LocalBuf(1024, 1), ineffet);
					effet = PV_Cutoff(effet, control1 * 2 - 1);
					effet = IFFT(effet);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(buseffetsPost,  Mix(effet) * ampPost);
					Out.ar(out, effet);
			}).send(s);

			// Model de definition des Effets _post

			SynthDef("Ringz_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.125, control2=0.125, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Ringz.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5*0.1,control6*0.1,control7*0.1,control8*0.1], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("TwoPole_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.125, control2=0.125, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(TwoPole.ar(ineffet, [control1*500,control2*500+500,control3*500+1000,control4*500+1500], [control5,control6,control7,control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("PitchShift_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.1, control2=0.1, control3=0.1, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree du effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(PitchShift.ar(ineffet, 0.3, [control1, control2, control3, control4, control5, control6]*4.0, control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("AllpassC_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.0625, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(AllpassC.ar(ineffet, 0.2, [control1,control2/2,control3/3,control4/4], [control5*30,control6*30,control7*30,control8*30], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("CombC_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.03, control2=0.5, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(CombC.ar(ineffet, 0.2, [control1/100,control2/200,control3/300,control4/400], [control5*16,control6*16,control7*16,control8*16], amp/4*0.6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DelayC_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(DelayC.ar(ineffet, 4.0, [control1*4.0,control2*4.0,control3*4.0,control4*4.0,control5*4.0,control6*4.0,control7*4.0,control8*4.0], amp));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("DelayCtempo_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0.60, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, tempo, beat, duree;
					tempo = (control7*60).floor;
					beat=(control8*4).floor;
					duree = 60/tempo*beat;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(DelayC.ar(ineffet, 10.0, [control1*duree,control2*duree,control3*duree,control4*duree,control5*duree,control6*duree], amp));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BPF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(BPF.ar(ineffet, [control1*3000+27.5,control2*3000+3000,control3*3000+6000,control4*3000+9000], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("BRF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(BRF.ar(ineffet, [control1*3000+27.5,control2*3000+3000,control3*3000+6000,control4*3000+9000], [control5+0.001,control6+0.001,control7+0.001,control8+0.001], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Formlet_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Formlet.ar(ineffet,[control1*300,control2*300+300,control3*300+600,control4*300+900,control5*300+1200,control6*300+1500], control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FOS_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(FOS.ar(ineffet, [control1,control2,control3,control4,control5,control6], control7, control8, amp/6));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("HPF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(HPF.ar(ineffet,  [control1*4186+312, control2*4186+312, control3*4186+312, control4*4186+312, control5*8372+312, control6*8372+312, control7*8372+312, control8*8372+312], amp/8));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LPF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(LPF.ar(ineffet, [control1*500+27.5, control2*500+27.5, control3*500+27.5, control4*500+27.5, control5*1000+27.5, control6*1000+27.5, control7*1000+27.5, control8*1000+27.5], amp/8));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Resonz_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(Resonz.ar(ineffet, [control1*1000,control2*1000+1000,control3*1000+2000,control4*1000+3000], [control5,control6,control7,control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RHPF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(RHPF.ar(ineffet, [control1*4186+312, control2*4186+312, control3*4186+312, control4*4186+312], [control5, control6, control7, control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RLPF_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Mix(RLPF.ar(ineffet, [control1*1000+27.5, control2*1000+27.5, control3*1000+27.5, control4*1000+27.5], [control5, control6, control7, control8], amp/4));
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("SOS_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=SOS.ar(ineffet, control1, control2, control3, control4*(-1.0), control5*(-1.0), amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=Median.ar(control1 * 30 + 1, ineffet, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("LeakDC_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=LeakDC.ar(ineffet, control1, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("Median+LeakDC_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=LeakDC.ar(Median.ar(control1 * 30 + 1, ineffet, amp), control2);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("FreeVerb_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0, control3=0, control4=0, control5=0, control6=0, control7=0, control8=0, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet=FreeVerb.ar(ineffet, control1, control2, control3, amp);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("GVerb_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0.1, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					#left, right=GVerb.ar(ineffet, (control1*300).clip(1, 300), (control5*100).clip(0.01, 100), control6, control7, 15, control2, control3, control4, 300, amp);
					effet=Mix(left,right);
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("JPverb_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0.1, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					// effet
					effet = Mix(JPverb.ar(ineffet, control1 * 60, control2, control3 * 5, control4, control5, control6, control7 *5900 + 100, control8 * 9000 + 1000)) * amp;
					effet = Limiter.ar(effet, 1.0, 0.01);
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("WarpDelay_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0.1, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right, local, localBuf;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					RecordBuf.ar(ineffet * EnvGen.kr(Env.perc(0.1,0.9,1,-5), Impulse.kr(control1), levelScale: amp, timeScale: control1.reciprocal), localBuf,0, 1, 0, loop: 0, trigger: Impulse.kr(control1), preLevel: 0.333);
					// effet
					effet = Warp1.ar(1, localBuf, control2, control3*4, control4, -1, control5*16, control6);// + ou - local;
					effet = Limiter.ar(effet, 1.0, 0.01);
					LocalOut.ar(DelayC.ar(effet, 1, control7, control8));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

			SynthDef("RandRateDelay_post",
				{// listes de arguments (identique pour chaque effet !!!!!)
					arg out=0, in, control1=0.3, control2=0.1, control3=0.1, control4=0.1, control5=0.1, control6=0.1, control7=0.1, control8=0.1, pan=0, amp=0, ampPre=0, ampPost=0;
					// liste des variables (identique pour chaque effet !!!!)
					var ineffet, effet, ambisonic, left, right, local, localBuf, buffer, rate;
					// son en entree de la effet + controles
					ineffet=Limiter.ar(Mix.new(In.ar(in,2)), 1.0, 0.01);
					local = LocalIn.ar(1);
					localBuf = LocalBuf(s.sampleRate, 1).clear;
					buffer = RecordBuf.ar(ineffet, localBuf, 0, 1, 0);
					rate = control1*4;
					// effet
					effet = PlayBuf.ar(1, localBuf, LFNoise2.kr(control2)+rate, Dust.kr(control3), Logistic.kr(control4/2+3.5, 100, Rand(0, 1))* BufFrames.kr(localBuf), 1, 0.05, 0.1) + local * amp / 2;
					effet = Mix(effet);
					effet = Limiter.ar(effet, 1.0, 0.01);
					LocalOut.ar(DelayC.ar(effet, 1.0, control5/1000, control6));
					// Switch Audio Out
					effet = if(~switchAudioOut == 0,
						// Pan
						Pan2.ar(effet, pan),
						if(~switchAudioOut == 2,
							// PanAz
							PanAz.ar(~numberAudioOut, effet, pan, 1, widthMC, orientationMC),
							if(~switchAudioOut == 1,
								// Rotate2 v1
								Rotate2.ar(effet, effet, pan),
								// Ambisonic v1
								(ambisonic = PanB2.ar(effet, pan);
									DecodeB2.ar(~numberAudioOut, ambisonic[0], ambisonic[1], ambisonic[2])))));
					// Out
					Out.ar(out, effet);
			}).send(s);

		},{"SynthDef Init Cancelled".postln});
	}

}
