// A Software by Herve Provini

LiveCode {

	classvar <> s;

	var pathData, midiOut, numberAudioIn, numberAudioOut, recChannels, typeMasterOut, widthMC, orientationMC, winCode, editCode, validCode, menuCode, runCode, synth;
	var groupSynth, groupAnalyze, busAnalyzeIn, busRecAudioIn, synthAudioIn, synthFileIn, synthAnalyzeOnsets, synthAnalyzePitch, synthAnalyzePitch2, synthAnalyzeKeyTrack, synthKeyboard, synthMIDI, synthAnalyzeAudioIn, synthRecAudioIn, bufferFile, fonctionLoadFileForAnalyze;
	var rangeFlux, rangeFlatness, rangeCentroid, rangeEnergy, busOSCflux, busOSCflatness, busOSCcentroid, busOSCenergy, busOSCbpm, busOSCfreq, busOSCamp, busOSCduree;
	var windowPlotterData, refreshDisplayDataMusic, plotterDataGUI, plotterData, displayAnalyzeFFT, plotDataMusic;
	var dataFlux, dataFlatness, dataCentroid, dataEnergy, dataBPM, indexDataFlux, indexDataFlatness, indexDataCentroid, indexDataEnergy, indexDataBPM, maximumData;
	var synthVST, fxVST, groupeVST, flagVST, cmdperiodfunc, canalMIDI, oscMusicFFT, lastTime, freqBefore, ampBefore, dureeBefore, freqTampon, ampTampon, numFhzBand, bandFHZ, switchSourceIn, switchAnalyze, typeAlgoAnalyze, numIndexSynthBand, indexDataDuree, indexDataFreq, indexDataAmp;
	var dataFreq, dataAmp, dataDuree, serveurAdresse, groupeMasterOut,groupeVST, groupeLimiter, groupeVerb;
	var gVerb, allPass, jpVerb, freeVerb, channelsVerb, verb, synthMasterOut, busSynthInOut;

	*new {arg path = "~/Documents/LiveCode/", ni = 10, numberOut=2, numberRec=2, format=0, devIn="Built-in Microph", devOut="HDMI"/*"Built-in Output"*/, size = 512, wid=2.0, ori=0.5, flag=0, name="LiveCode", wek=6448, wekPort=57120, scPort=57110;

		^super.new.init(name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag, scPort);

	}

	init {arg name, path, ni, numberOut, numberRec, format, devIn, devOut, size, wid, ori, flag, scPort;

		//// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system
		MainMenu.initBuiltInMenus;

		pathData = PathName.new(path).pathOnly;

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
		serveurAdresse = s.addr;
		s.options.memSize = 2**20;
		s.options.inDevice_(devIn);
		s.options.outDevice_(devOut);
		s.options.numInputBusChannels_(numberAudioIn);
		s.options.hardwareBufferSize_(size);
		s.options.numOutputBusChannels_(numberAudioOut);
		s.recChannels_(recChannels);
		widthMC = wid;
		orientationMC = ori;

		thisProcess.openUDPPort(NetAddr.langPort);

		Safety(s);
		//s.makeGui;

		s.waitForBoot({

			// Init Synth
			this.initSynthDef;
			this.createGUI;

			// run the soft
			this.run;

			cmdperiodfunc = {
			Window.closeAll;
		};

		CmdPeriod.doOnce(cmdperiodfunc);

		});

	}

	run {

		//Fonction Load file for analyze
		fonctionLoadFileForAnalyze = {arg p, f, d;
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
		fonctionLoadFileForAnalyze.value("/Applications/SuperCollider.app/Contents/Resources/sounds/a11wlk01.wav");

		// Init Data
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
		maximumData = 24;
		flagVST = 'off';
		canalMIDI = 1;
		numFhzBand = 12;
		numFhzBand = 3; // Nombre de band de fhz (+1 pour all data) pour trier dans les synth index=0 pour all index=1 pour badnnum 1 etc...
		bandFHZ = Array.fill(numFhzBand, {arg i; [127 / numFhzBand * i, 127 / numFhzBand * i + (127 / numFhzBand )]}).midicps;
		bandFHZ = bandFHZ.reverse;
		bandFHZ = bandFHZ.add([0, 127].midicps);
		bandFHZ = bandFHZ.reverse;
		numIndexSynthBand = 0;
		indexDataFreq = [];
		indexDataAmp = [];
		indexDataDuree = [];
		dataFreq = [];
		dataAmp = [];
		dataDuree = [];
		typeAlgoAnalyze = 0;
		rangeFlux = [0,1];
		rangeFlatness = [0,1];
		rangeCentroid = [0,1];
		rangeEnergy = [0,1];
		plotterData = [[0], [0], [0], [0], [0], [0],[0], [0]];


		// Group
		groupAnalyze = ParGroup.new(s, \addToTail);
		groupSynth = ParGroup.new(s, \addToTail);
		groupeMasterOut = ParGroup.new(s, \addToTail);
		groupeVerb = ParGroup.new(s, \addToTail);
		groupeVST = ParGroup.new(s, \addToTail);
		groupeLimiter = ParGroup.new(s, \addToTail);

		// Bus OSC Data
		busAnalyzeIn = Bus.audio(s, 1);
		busRecAudioIn = Bus.audio(s, 1);
		busSynthInOut = Bus.audio(s, 1);


		// Bus OSC Data
		// Init Bus Array max 12
		(numFhzBand + 1).do({arg i;
			busOSCflux = busOSCflux.add(Bus.control(s, 1));
			busOSCflatness = busOSCflatness.add(Bus.control(s, 1));
			busOSCcentroid = busOSCcentroid.add(Bus.control(s, 1));
			busOSCenergy = busOSCenergy.add(Bus.control(s, 1));
			busOSCbpm = busOSCbpm.add(Bus.control(s, 1));
			busOSCfreq = busOSCfreq.add(Bus.control(s, 1));
			busOSCamp = busOSCamp.add(Bus.control(s, 1));
			busOSCduree = busOSCduree.add(Bus.control(s, 1));
			// Init Array
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
			dataFreq = dataFreq.add([]);
			dataAmp = dataAmp.add([]);
			dataDuree = dataDuree.add([]);
			indexDataFreq = indexDataFreq.add(0);
			indexDataAmp = indexDataAmp.add(0);
			indexDataDuree = indexDataDuree.add(0);
			lastTime = lastTime.add(Main.elapsedTime);// Init Time for Analyze;
		});

		// Synth AudioIn
		synthAudioIn = Synth.newPaused("LiveCode AudioIn",
			[\in, 0, 'busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Synth play file
		synthFileIn = Synth.newPaused("LiveCode FileIn",
			[\bufferplay, bufferFile, 'busIn', busAnalyzeIn, \busRec, busRecAudioIn, \volume, 0], groupAnalyze, \addToTail);
		s.sync;

		// Synth audio analyze Onsets
		synthAnalyzeOnsets = Synth.newPaused("OSC LiveCode Onsets",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Synth audio analyze Pitch
		synthAnalyzePitch = Synth.newPaused("OSC LiveCode Pitch",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Synth audio analyze Pitch Version 2
		synthAnalyzePitch2 = Synth.newPaused("OSC LiveCode Pitch2",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Synth audio analyze KeyTrack
		synthAnalyzeKeyTrack = Synth.newPaused("OSC LiveCode KeyTrack",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Synth Keyboard
		synthKeyboard = Synth.newPaused("OSC LiveCode Keyboard",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// MIDI Keyboard
		synthMIDI = Synth.newPaused("OSC LiveCode MIDI",
			['busIn', busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Create synth RecAudioBus for Analyze
		synthAnalyzeAudioIn = Synth.newPaused("AnalyzeAudioIn", [\in, 0, \busIn, busAnalyzeIn], groupAnalyze, \addToTail);
		s.sync;

		// Create synth RecAudioBus for Buffer Synth
		synthRecAudioIn = Synth.newPaused("RecAudioIn", [\in, 0, \busIn, busRecAudioIn], groupAnalyze, \addToTail);
		s.sync;

		//Init EndProcessing

		 synthMasterOut = Synth.newPaused(typeMasterOut, [\in, busSynthInOut, \out, 0], groupeMasterOut, \addToTail);

		//gVerb = Synth.newPaused("GVerb" + typeMasterOut, [\out, channelsVerb, \xFade, 0, \panLo, 0, \panHi, 0, \drylevel, 0, \earlylevel, 0, \taillevel, 0], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			//freeVerb = Synth.newPaused("FreeVerb" + typeMasterOut, [\out, channelsVerb, \xFade, 0.5, \panLo, 0, \panHi, 0, \drylevel, 0.9, \earlylevel, 0.5, \taillevel, 0.9], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			verb = /*allPass =*/ Synth.newPaused("Allpass" + typeMasterOut, [\out, channelsVerb, \xFade, 0.33, \panLo, 0, \panHi, 0, \drylevel, 0.5, \earlylevel, 0.6, \taillevel, 0.8], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

			//jpVerb = Synth.newPaused("JPverb" + typeMasterOut, [\out, channelsVerb, \xFade, 0.5, \panLo, 0, \panHi, 0, \drylevel, 0.5, \earlylevel, 0.5, \taillevel, 0.5], groupeVerb, \addToHead).map(\bpm, busOSCbpm.at(0));
			s.sync;

		Synth.new("SynthLimiter", [\limit, -3.dbamp], groupeLimiter, \addToTail);
		s.sync;

		synthVST = Synth.newPaused("VST"+ typeMasterOut, [\xFade, 0.5, \panLo, 0, \panHi, 0, \gainIn, 0.5], groupeVST, \addToTail).map(\bpm, busOSCbpm.at(0));
		fxVST = VSTPluginController(synthVST);
		s.sync;

		//  OSC Data FFT
		oscMusicFFT = OSCFunc.newMatching({arg msg, time, addr, recvPort, freq=0, amp=0, timer=1, duree=0, centroid=0, flatness=0, energy=0, flux=0, bpm=0;
			var data;
			freq = msg.at(3);
			amp = msg.at(4);
			timer = msg.at(5);
			duree = msg.at(5);
			bpm = msg.at(6);
			centroid = msg.at(7);
			energy = msg.at(8);
			flux = msg.at(9);
			flatness = msg.at(10);
			// Normalize
			flux = flux * (rangeFlux.at(1) - rangeFlux.at(0)) + rangeFlux.at(0);
			flatness = flatness * (rangeFlatness.at(1) - rangeFlatness.at(0)) + rangeFlatness.at(0);
			centroid = (centroid / 12544 * (rangeCentroid.at(1) - rangeCentroid.at(0)) + rangeCentroid.at(0) * 12544).clip(20, 12544);
			energy = (energy / 12544 * (rangeEnergy.at(1) - rangeEnergy.at(0)) + rangeEnergy.at(0) * 12544).clip(20, 12544);
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
			plotDataMusic.value(freq, amp, duree, bpm, centroid, energy, flux, flatness);
			groupSynth.setn(\freq, freq, \amp, amp, \dur, duree, \bpm, bpm, \centroid, centroid, \energy, energy, \flux, flux, \flatness, flatness);
			groupeVerb.setn(\panLo, flux * 2 - 1, \panHi, flux * 2 - 1);
		}, '/LiveCode_Music_Data', serveurAdresse);

		freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
		(numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});

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

		// Fonction Plotter Data Music
		plotDataMusic = {arg freq, amp, duree, bpm, centroid, energy, flux, flatness;
			var analyzeData;
			{
				// Setup GUI Value
				analyzeData = [freq, amp, duree, bpm, centroid, energy, (flux * 10000).log2.clip(0, 13), (flatness * 10000).log2.clip(0, 13)];
				if(plotterData.at(0).size > 1024, {plotterData =[[freq], [amp], [duree], [bpm], [centroid], [energy], [(flux * 10000).log2.clip(0, 13)], [(flatness * 10000).log2.clip(0, 13)]]},
					{plotterData.size.do({arg index; plotterData.put(index, plotterData.at(index).add(analyzeData.at(index)))});
						plotterDataGUI.value = plotterData;
				});
				/*// Display Analyze Music
				displayAnalyzeMusic.string = ("Freq:" + freq.asStringPrec(4)  +  "  Amp:" + amp.asStringPrec(4) + "  Duree:" + duree.asStringPrec(4) + "  Instruments:" + listeDataInstruments.size + "  Data:" +  dataFreq.at(0).size + "  Index:" + indexDataFreq.at(0) + "  Algo: " + displayAlgo + "  FhzBand: " + displayIndex + "  M" ++ displayMIDI);
				// Display Analyze FFT
				displayAnalyzeFFT.string = ("Flux:" + flux.asStringPrec(4)  + "    Flatness:" + flatness.asStringPrec(4) + "    Centroid:" + centroid.asStringPrec(4) + "    Energy:" + energy.asStringPrec(4) + "    BPM:" + (bpm.asFloat).asStringPrec(4));*/
			}.defer;
		};

	}

	createGUI {

		//Coding
		winCode = Window("LiveCode", Rect(0, 1024, 800, 500));
		winCode.view.decorator = FlowLayout(winCode.view.bounds);
		StaticText(winCode, Rect(0, 0, 500, 24)).string_("LiveCode").stringColor_(Color.yellow);
		winCode.view.decorator.nextLine;
		// Load Coding
		menuCode = PopUpMenu(winCode,Rect(0, 0, 100, 20)).background_(Color.grey(0.5, 0.8)).items = ["User Menu", "Load Code", "Save Code"];
		menuCode.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Coding
				1, {Dialog.openPanel({ arg paths;
					editCode.open(paths);
					editCode.string.interpret;
					winCode.name="LiveCode" + PathName(paths).fileName;
				},{"cancelled".postln})},
				// Save Coding
				2, {Dialog.savePanel({arg path; var file;
					winCode.name="LiveCode"+path;
					file=File(path++".scd","w");
					file.write(editCode.string);file.close;
				},{"cancelled".postln})}
			);
			menuCode.value_(0);
		};
		menuCode.focus;
		// Run Code
		runCode = Button(winCode,Rect(0, 0, 100, 20)).states=[["Run On", Color.green], ["Run Off", Color.red]];
		runCode.action = {arg start;
			switch (start.value,
				0, {
					groupSynth.freeAll;
					synthAnalyzeOnsets.run(false);
					synthAnalyzePitch.run(false);
					synthAnalyzePitch2.run(false);
					synthAnalyzeKeyTrack.run(false);
					synthKeyboard.run(false);
					synthMIDI.run(false);
					synthAudioIn.run(false);
					synthFileIn.run(false);
					synthAnalyzeAudioIn.run(false);
					synthRecAudioIn.run(false);
					verb.run(false);
					synthMasterOut.run(false);
					TempoClock.default.clear;
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
					dataFreq = [];
					dataAmp = [];
					dataDuree = [];
					indexDataFreq = [];
					indexDataAmp = [];
					indexDataDuree = [];
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
						dataFreq = dataFreq.add([]);
						dataAmp = dataAmp.add([]);
						dataDuree = dataDuree.add([]);
						indexDataFreq = indexDataFreq.add(0);
						indexDataAmp = indexDataAmp.add(0);
						indexDataDuree = indexDataDuree.add(0);
						lastTime = lastTime.add(Main.elapsedTime);
					});
					freqBefore=0; ampBefore=0; dureeBefore=0; freqTampon = nil; ampTampon = nil;
					numIndexSynthBand = 0;
				},
				1, {
					synth = Synth.new("LiveCode", [nil], groupSynth, \addToTail);
					switchSourceIn.valueAction_(switchSourceIn.value);
					switchAnalyze.valueAction_(typeAlgoAnalyze);
					synthAnalyzeAudioIn.run(true);
					synthRecAudioIn.run(true);
					verb.run(true);
					synthMasterOut.run(true);
				}
		)};
		winCode.view.decorator.nextLine;
		StaticText(winCode, Rect(0, 0, 35, 20)).string_("Code").stringColor_(Color.yellow);
		// Evaluate Coding
		validCode = Button(winCode,Rect(0, 0, 100, 20)).states=[["Evaluate Code", Color.white, Color.blue]];
		validCode.action = {arg view;
			editCode.string.interpret;
		};
		// Choice Input
		switchSourceIn = PopUpMenu(winCode,Rect(0, 0, 100, 20)).background_(Color.grey(0.5, 0.8)).items = ["Audio", "File"];
		switchSourceIn.action={|source|
			switch (source.value,
				0, {synthAudioIn.run(true); synthFileIn.run(false); synthAnalyzeAudioIn.run(true); synthRecAudioIn.run(true);
					lastTime = []; (numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});
				},
				1, {synthAudioIn.run(false); synthFileIn.run(true); synthAnalyzeAudioIn.run(false); synthRecAudioIn.run(false);
					lastTime = []; (numFhzBand + 1).do({arg i; lastTime = lastTime.add(Main.elapsedTime)});
				}
			);
		};
		switchAnalyze = PopUpMenu(winCode,Rect(0, 0, 90, 20));
		switchAnalyze.items_(["Onsets", "Pitch", "Pitch2", "KeyTrack", "Keyboard", "MIDI"]);
		switchAnalyze.action = {|analyze|
			typeAlgoAnalyze = analyze.value;
			switch (analyze.value,
				0, {synthAnalyzeOnsets.run(true); synthAnalyzePitch.run(false); synthAnalyzePitch2.run(false); synthAnalyzeKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false);
				},
				1, {synthAnalyzeOnsets.run(false); synthAnalyzePitch.run(true); synthAnalyzePitch2.run(false); synthAnalyzeKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false);
				},
				2, {synthAnalyzeOnsets.run(false); synthAnalyzePitch.run(false); synthAnalyzePitch2.run(true); synthAnalyzeKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(false);
				},
				3, {synthAnalyzeOnsets.run(false); synthAnalyzePitch.run(false); synthAnalyzePitch2.run(false); synthAnalyzeKeyTrack.run(true); synthKeyboard.run(false); synthMIDI.run(false);
				},
				4, {synthAnalyzeOnsets.run(false); synthAnalyzePitch.run(false); synthAnalyzePitch2.run(false); synthAnalyzeKeyTrack.run(false); synthKeyboard.run(true); synthMIDI.run(false);
				},
				5, {synthAnalyzeOnsets.run(false); synthAnalyzePitch.run(false); synthAnalyzePitch2.run(false); synthAnalyzeKeyTrack.run(false); synthKeyboard.run(false); synthMIDI.run(true);
				}
			);
		};
		winCode.view.decorator.nextLine;
		//Code Window
		editCode = TextView(winCode, Rect(0, 0, 790, 400));
		editCode.hasVerticalScroller_(true);
		editCode.hasHorizontalScroller_(true);
		editCode.autohidesScrollers_(true);
		editCode.resize_(5);
		editCode.setFont(Font("Courier", 24), 0, 20);
		editCode.string_("SynthDef('LiveCode'.asString, { |in=0, out=0, freq=440, amp=0.1, dur=1, bpm=1, centroid=440, energy=440, flux=0.5, flatness=0.5|
var sig, trig;
sig = Saw.ar(freq, amp.max(0.1));
trig = Impulse.kr(dur.reciprocal);
sig = sig * EnvGen.kr(Env.perc, trig, 1, 0, dur);// doneAction: Done.freeSelf
sig = Pan2.ar(sig, flux*2-1);
Out.ar(0, sig);
}).add;
");
		editCode.string.interpret;
		winCode.onClose_({nil});
		winCode.front;

		////// Window Plotter Data /////
		windowPlotterData = Window("Freq | Amp | Duree | BPM | Centroid | Energy | Flux | Flatness", Rect(805, 800, 510, 520), scroll: true);
		windowPlotterData.alpha=1.0;
		windowPlotterData.front;
		windowPlotterData.view.decorator = FlowLayout(windowPlotterData.view.bounds);
		// Refresh Display
		refreshDisplayDataMusic = Button(windowPlotterData,Rect(0, 0, 100, 20));
		refreshDisplayDataMusic.states = [["Refresh Plotter"]];
		refreshDisplayDataMusic.action = {|view| plotterDataGUI.value = [[0], [0], [0],[0],[0],[0],[0],[0]]; plotterData = [[0], [0], [0],[0],[0],[0],[0],[0]]};

		// Range FFT
		EZRanger(windowPlotterData , 500 @ 20, "Centroid", \unipolar,
			{|ez| rangeCentroid = ez.value}, [0, 1], labelWidth: 65).setColors(Color.grey(0.3), Color.magenta);
		// Range FFT
		EZRanger(windowPlotterData , 500 @ 20, "Energy", \unipolar,
			{|ez| rangeEnergy = ez.value}, [0, 1], labelWidth: 65).setColors(Color.grey(0.3), Color.magenta);
		// Range FFT
		EZRanger(windowPlotterData , 500 @ 20, "Flux", \unipolar,
			{|ez| rangeFlux = ez.value}, [0, 1], labelWidth: 65).setColors(Color.grey(0.3), Color.magenta);
		// Range FFT
		EZRanger(windowPlotterData , 500 @ 20, "Flatness", \unipolar,
			{|ez| rangeFlatness = ez.value}, [0, 1], labelWidth: 65).setColors(Color.grey(0.3), Color.magenta);
		// Plotter
		plotterDataGUI = Plotter("Analyze Data", Rect(0, 0, 500, 390), windowPlotterData).plotMode_(\steps);
		plotterDataGUI.value = [[0], [0], [0],[0],[0],[0],[0],[0]];
		refreshDisplayDataMusic.focus;

		/*//Setup Font
		winCode.view.do({arg view;
		view.children.do({arg subView;
		subView.font = Font("Helvetica", 10);
		});
		});*/

	}

	initSynthDef {

		// LiveCode Audio Analyze Onsets
		SynthDef("OSC LiveCode Onsets",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0, inputFilter;
				var fft, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \power);
				# freqIn, hasfreqIn = Tartini.kr(inputFilter);//, filtre, 2048, 1024, 512, 0.5);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				array = [freqIn, ampIn, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(detect, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// LiveCode Audio Analyze Pitch
		SynthDef("OSC LiveCode Pitch",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0, inputFilter;
				var fft, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				detect= Onsets.kr(FFT(LocalBuf(512, 1), inputFilter), seuil, \rcomplex);
				# freqIn, hasfreqIn = Pitch.kr(inputFilter, minFreq: 32, maxFreq: 4186, median: 1, peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				array = [freqIn, ampIn, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(detect, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// Pitch v2
		SynthDef("OSC LiveCode Pitch2",
			{arg busIn, seuil=0.5, filtre=0.5, hzPass=440, ampInput = 1, ampLoPass = 0,  ampHiPass = 0;
				var input, detect, freqIn, hasfreqIn, ampIn, timeIn=0, inputFilter, fft, harmonic, percussive;
				var fft2, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				ampInput = if(ampLoPass < 1, 1, if(ampHiPass < 0, 1, 0));
				inputFilter = LPF.ar(input, hzPass, ampLoPass, HPF.ar(input, hzPass, ampHiPass, input * ampInput));
				fft2 = FFT(LocalBuf(1024, 1), inputFilter);
				harmonic = FFT(LocalBuf(512, 1), inputFilter);
				percussive = FFT(LocalBuf(512, 1), inputFilter);
				#harmonic, percussive = MedianSeparation(fft2, harmonic, percussive, 512, 5, 1, 2, 1);
				detect = Onsets.kr(FFT(LocalBuf(512, 1), IFFT(percussive)), seuil, \power);
				# freqIn, hasfreqIn = Pitch.kr(IFFT(harmonic), peakThreshold: filtre);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				array = [freqIn, ampIn, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(detect, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// LiveCode Audio Analyze KeyTrack
		SynthDef("OSC LiveCode KeyTrack",
			{arg busIn, seuil=0.5, filtre=0.5;
				var input, detect, freqIn, ampIn, timeIn=0, key;
				var fft, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				detect= Onsets.kr(FFT(LocalBuf(512, 1), input), seuil);
				key = KeyTrack.kr(FFT(LocalBuf(4096, 1), input), (filtre * 4).clip(0, 4));
				if(key < 12, freqIn = (key + 60).midicps, freqIn = (key - 12 + 60).midicps);
				ampIn = A2K.kr(Amplitude.ar(input));
				timeIn = Timer.kr(detect);
				array = [freqIn, ampIn, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(detect, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// LiveCode Keyboard
		SynthDef("OSC LiveCode Keyboard",
			{arg busIn, note=60, amp=0.5, trigger=0;
				var input, timeIn=0;
				var fft, detect, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(trigger);
				array = [note.midicps, amp, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(trigger, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// LiveCode MIDI
		SynthDef("OSC LiveCode MIDI",
			{arg busIn, note=60, amp=0.5, trigger=0;
				var input, timeIn=0;
				var fft, detect, centroid=440, flatness=0.5, energy=440, flux=0.5,
				trackB, trackH, trackQ, bpm=1, lock=0, array, array2;
				input = In.ar(busIn);
				fft = FFT(LocalBuf(1024, 1), input);
				centroid = SpecCentroid.kr(fft);
				flatness =  SpecFlatness.kr(fft);
				energy =  SpecPcile.kr(fft);
				flux =  FFTFlux.kr(fft);
				# trackB,trackH,trackQ, bpm = BeatTrack.kr(FFT(LocalBuf(1024, 1), input), lock);
				timeIn = Timer.kr(trigger);
				array = [note.midicps, amp, timeIn, bpm, centroid, energy, flux, flatness];
				SendReply.kr(trigger, '/LiveCode_Music_Data', values: array, replyID: 1);
		}).add;

		// Synth pour Analyze AudioIn send audio -> busIn
		SynthDef("LiveCode AudioIn",
			{arg in=0, busIn=0;
				var input;
				input=Mix(SoundIn.ar(in));
				Out.ar(busIn, input); // Bus File In
		}).add;

		// Synth lecture file pour Analyze AudioIn
		SynthDef("LiveCode FileIn",
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

}

