// New Soft

HPdroneMIDI {


	classvar  <> server;

	var cmdperiodfunc, evaluationKeyDown, midiOut, listeWindows, windowKeyboard, canalMidi, keyVolume, keyboardTranslate, keyboard, keyboardShortCut, keyboardVolume, keyboardTranslateBefore, lastNote, groupSynth, synthButt, silentBut, typeMasterOut, numberAudioOut, recChannels, groupeMasterOut, midiSlider;

	*new {arg path = "~/Documents/HPSoft/", numberOut=2, numberRec=2, format="Stereo", devIn="Built-in Microph", devOut="Built-in Output", size=256;

		^super.new.init(path, numberOut, numberRec, format, devIn, devOut, size);

	}

	init {arg path, numberOut, numberRec, format, devIn, devOut, size;

		QtGUI.palette = QPalette.dark;// light / system

		server = Server.default;

		server.waitForBoot({

			// MIDI INIT
			// Connect first device by default
			MIDIClient.init;
			MIDIIn.connect(0, 0);
			midiOut = MIDIOut(0).latency = 0.01;
			midiOut.connect(0);
			16.do({arg canal; midiOut.allNotesOff(canal)});

			numberAudioOut = numberOut;
			recChannels = numberRec;
			typeMasterOut = format;// Type Format stereo, ambisonic, etc...

			server = Server.default;

			server.options.memSize = 2**20;ˆ
			server.options.inDevice = devIn;
			server.options.outDevice = devOut;
			server.options.hardwareBufferSize_(size);
			server.options.numInputBusChannels_(20);
			// Safety Limiter
			//server.options.safetyClipThreshold = 1.26; // Testing
			Safety(server);
			//Safety(server).enabled;
			//Safety.setLimit(1.neg.dbamp);

			// Init Synth
			this.initSynthDef;

			// run the soft
			this.run;

		});

	}

	run {

		//MIDI
		canalMidi = 0;
		keyVolume = 45;
		keyboardTranslate = 0;
		keyboardTranslateBefore = 0;
		lastNote = 60;

		groupSynth = ParGroup.new(server, \addToTail);
		groupeMasterOut = ParGroup.new(server, \addToTail);

		this.creationGUI;

		// Stop programme
		cmdperiodfunc = {
			//listeWindows.do({arg w; w.close});
			16.do({arg canal; midiOut.allNotesOff(canal)});
			groupSynth.release(2);
			Window.closeAll;
			//server.quit;
		};

		CmdPeriod.doOnce(cmdperiodfunc);

	}

	creationGUI {

		// ShortCuts for Keyboard
		keyboardShortCut = {arg window, lastNote=60;
			// Down
			window.view.keyDownAction = {arg view,char,modifiers,unicode,keycode;
				// [char,modifiers,unicode,keycode].postln;
				// key esc or SpaceBar-> All System on/off
				if(unicode==27 and: {keycode==53} or: {unicode == 32 and: {keycode == 49}}, {
					16.do({arg canal; midiOut.allNotesOff(canal)});
					groupSynth.release(2);
				});
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
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (60 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (60 + keyboardTranslate.value));
					};
					lastNote = 60 + keyboardTranslate.value;
					keyboard.setColor(60 + keyboardTranslate.value, Color.red);
				});
				if(char == $s, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (61 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (61 + keyboardTranslate.value));
					};
					lastNote = 61 + keyboardTranslate.value;
					keyboard.setColor(61 + keyboardTranslate.value, Color.red);
				});
				if(char == $x, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (62 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (62 + keyboardTranslate.value));
					};
					lastNote = 62 + keyboardTranslate.value;
					keyboard.setColor(62 + keyboardTranslate.value, Color.red);
				});
				if(char == $d, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (63 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (63 + keyboardTranslate.value));
					};
					lastNote = 63 + keyboardTranslate.value;
					keyboard.setColor(63 + keyboardTranslate.value, Color.red);
				});
				if(char == $c, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (64 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (64 + keyboardTranslate.value));
					};
					lastNote = 64 + keyboardTranslate.value;
					keyboard.setColor(64 + keyboardTranslate.value, Color.red);
				});
				if(char == $v, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (65 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (65 + keyboardTranslate.value));
					};
					lastNote = 65 + keyboardTranslate.value;
					keyboard.setColor(65 + keyboardTranslate.value, Color.red);
				});
				if(char == $g, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (66 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (66 + keyboardTranslate.value));
					};
					lastNote = 66 + keyboardTranslate.value;
					keyboard.setColor(66 + keyboardTranslate.value, Color.red);
				});
				if(char == $b, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (67 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (67 + keyboardTranslate.value));
					};
					lastNote = 67 + keyboardTranslate.value;
					keyboard.setColor(67 + keyboardTranslate.value, Color.red);
				});
				if(char == $h, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (68 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (68 + keyboardTranslate.value));
					};
					lastNote = 68 + keyboardTranslate.value;
					keyboard.setColor(68 + keyboardTranslate.value, Color.red);
				});
				if(char == $n, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (69 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (69 + keyboardTranslate.value));
					};
					lastNote = 69 + keyboardTranslate.value;
					keyboard.setColor(69 + keyboardTranslate.value, Color.red);
				});
				if(char == $j, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (70 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (70 + keyboardTranslate.value));
					};
					lastNote = 70 + keyboardTranslate.value;
					keyboard.setColor(70 + keyboardTranslate.value, Color.red);
				});
				if(char == $m, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (71 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (71 + keyboardTranslate.value));
					};
					lastNote = 71 + keyboardTranslate.value;
					keyboard.setColor(71 + keyboardTranslate.value, Color.red);
				});
				if(char == $,, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (72 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (72 + keyboardTranslate.value));
					};
					lastNote = 72 + keyboardTranslate.value;
					keyboard.setColor(72 + keyboardTranslate.value, Color.red);
				});
				if(char == $l, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (73 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (73 + keyboardTranslate.value));
					};
					lastNote = 73 + keyboardTranslate.value;
					keyboard.setColor(73 + keyboardTranslate.value, Color.red);
				});
				if(char == $., {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (74 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (74 + keyboardTranslate.value));
					};
					lastNote = 74 + keyboardTranslate.value;
					keyboard.setColor(74 + keyboardTranslate.value, Color.red);
				});
				// Key �
				if(modifiers==0 and: {unicode==233} and: {keycode==41}, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (75 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (75 + keyboardTranslate.value));
					};
					lastNote = 75 + keyboardTranslate.value;
					keyboard.setColor(75 + keyboardTranslate.value, Color.red);
				});
				if(char == $-, {
					keyboard.removeColor(lastNote);
					keyboard.setColor(lastNote, Color.blue);
					server.bind{
						midiOut.noteOff(canalMidi, lastNote, 0);
						server.sync;
						midiOut.noteOn(canalMidi, (76 + keyboardTranslate.value), keyVolume);
						server.sync;
						groupSynth.set(\freq, (76 + keyboardTranslate.value));
					};
					lastNote = 76 + keyboardTranslate.value;
					keyboard.setColor(76 + keyboardTranslate.value, Color.red);
				});
			};
		};

		// Creation ALL GUI

		////////////////////////// Window Keyboard ///////////////////////////////
		windowKeyboard = Window.new("Keyboard", Rect(200, 25, 1030, 180), scroll: true);
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
			{|ez| keyVolume = ez.value.dbamp * 127; groupSynth.set(\amp, keyVolume / 127)}, -9,labelWidth: 75,numberWidth: 50);
		synthButt = Button(windowKeyboard, Rect(10, 10, 100, 20));
		synthButt.states_([
			["Drone On", Color.black, Color.white],
			["Drone Off", Color.yellow, Color.red]
		]);
		synthButt.action_({arg butt;
			switch(butt.value,
				0, {groupSynth.release(2)},
				1, {Synth.new("Drone",
					[\freq, 60, \amp, keyVolume / 127], groupSynth, \addToTail)}
			);
		});
		//StaticText(windowKeyboard, Rect(10, 10, 200, 20)).string_("Esc or Space Bar Silent").stringColor_(Color.red);
		silentBut = Button(windowKeyboard, Rect(10, 10, 100, 20));
		silentBut.states_([
			["Silent", Color.black, Color.yellow],
			["Silent", Color.black, Color.yellow]
		]);
		silentBut.action_({arg butt;
			switch(butt.value,
				0, {groupSynth.release(2); 16.do({arg canal; midiOut.allNotesOff(canal)})},
				1, {groupSynth.release(2); synthButt.valueAction_(0); 16.do({arg canal; midiOut.allNotesOff(canal)})}
			);
		});
		// Canal Midi
		midiSlider = EZSlider(windowKeyboard, 250 @ 20, "MIDI", ControlSpec(1, 16, \lin, 1),
			{|ez| midiOut.allNotesOff(canalMidi); canalMidi = ez.value - 1}, 1,labelWidth: 75,numberWidth: 50);
		// Keyboard Keys
		keyboard = MIDIKeyboard.new(windowKeyboard, Rect(5, 5, 1000, 140), 7, 24);
		forBy(60, 76, 1, {arg note; keyboard.setColor(note, Color.blue)});
		// Action Down
		keyboard.keyDownAction_({arg note;
			midiOut.noteOff(canalMidi, lastNote);
			note = note.value + keyboardTranslate.value;
			lastNote = note;
			midiOut.noteOn(canalMidi, note, keyVolume);
			groupSynth.set(\freq, note, \amp, keyVolume / 127);
		});
		// Action Up
		keyboard.keyUpAction_({arg note;
			/*note = note.value + keyboardTranslate.value;
			midiOut.noteOn(canalMidi, note, 0);*/
		});
		// Action Track
		keyboard.keyTrackAction_({arg note;
			note = note.value + keyboardTranslate.value;
			server.bind{
				midiOut.noteOn(canalMidi, note, keyVolume);
				server.sync;
				midiOut.noteOff(canalMidi, lastNote);
				server.sync;
				lastNote = note;
				groupSynth.set(\freq, note, \amp, keyVolume / 127);
			};
		});
		// Color Keyborad
		windowKeyboard.onClose_({nil});

		// Init shortCuts
		listeWindows=listeWindows.add(windowKeyboard);
		listeWindows.do({arg window; keyboardShortCut.value(window)});

	}

	initSynthDef {

		// Vibrato
		//SinOSC.kr(flatness.log.abs, mul: flatness, add: BufRateScale.kr(buffer) * rate)

		SynthDef("Drone", { |freq = 60, amp = 0.8, dur = 1, gate = 1|
			var sig, arrayFreq, trig, seq, dFreq, verb, vibrato;
			arrayFreq = [freq, freq + 5, freq + 7,  freq + 10, freq - 5, freq - 7,  freq - 10];
			seq = Drand(arrayFreq, inf);
			trig = Trig1.kr(Dust2.kr(24), dur);
			dFreq = Demand.kr(trig, 0, seq).lag;
			vibrato = Vibrato.kr(dFreq.midicps, 0.1, 0.02);
			sig = CombC.ar(Dust2.ar(24), 0.2, dFreq.midicps.reciprocal, 1.0, amp, LFSaw.ar(vibrato, 0, amp / 12));
			//sig = CombC.ar(Dust2.ar(6), 0.2, freq.midicps.reciprocal, 1.0, amp);
			sig = sig * EnvGen.kr(Env.adsr, gate, timeScale: dur, doneAction: Done.freeSelf);
			//sig = sig * EnvGen.kr(Env.cutoff(1), gate, doneAction: Done.freeSelf);// =  no envelope
			//sig = Mix(JPverb.ar(sig, 3));
			Out.ar(0, sig ! 2);
		}).add;

	}

}